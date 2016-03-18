package com.fg114.main.cache;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.util.Base64;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.MyThreadPool;
import com.fg114.main.util.MyThreadPool.Task;
import com.fg114.main.util.PatchInputStream;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.MyImageView;

import static com.fg114.main.app.Settings.*;

/*
 * Creator: xu jianjun, 2011-11-10
 * 
 * 提供接口实现"文件"缓存的维护。
 * 此类尽量少抛出异常。
 *
 */

public class FileCacheUtil {

	public static final boolean DEBUG = true;
	// 允许缓存的最大文件数，如果超过了这个值，则自动删除最旧的10%文件。
	private int maxFileNum = Settings.CACHE_FILESYSTEM_FILE_MAX_NUMBER;
	//负责将文件写入磁盘的后台线程池
	private MyThreadPool pool = new MyThreadPool(1, Thread.NORM_PRIORITY - 2, 0, "FileCacheUtil");

	// 阀值，存储缓存中的文件数，在初始化时从数据库中取得。
	private int valve = 0;

	// 默认软件版本
	private String version = "-";

	// 缓存数据库名
	private String dbName = "file_cache.db";

	// 缓存文件所在目录
	private String cachePath;

	// 缓存数据所在目录
	private String databaseCachePath;

	// 加密、解密字符串时使用的密钥
	private final String secretKey = "57575777";

	private Context context = null;
	// 内存缓冲
	public MemoryCache<FileObject> mmCache = new MemoryCache<FileObject>(Settings.CACHE_MEMORY_FILE_TOTAL_SIZE, "MemoryCache-File");// (Settings.CACHE_MEMORY_FILE_TOTAL_SIZE,"MemoryCache-File");

	public boolean enableManualLock = false;

	// 读写锁，同步访问数据库的读写操作。
	// 实践证明，对于sd卡，sqlite 数据库的锁定机制是无效的，并发操作时会出现错误，所以手工加锁。
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

	/**
	 * 表示当缓存的文件是图片时，图片边长的最大值。 如果图片边长尺寸超过了这个值，则自动按比例缩小图片，使边长不超过maxSideOfBitmap。
	 * 缓存的是缩小后的图片。单位是像素值。小于等于０时表示不缩放图片。
	 */
	public static int maxSideOfBitmap = 700;

	private static FileCacheUtil instance;
	//
	BitmapFactory.Options options = new BitmapFactory.Options();
	{
		options.inJustDecodeBounds = true;
	}
	// 写文件到文件系统时的文件锁，防止同时写相同文件
	private static final Object locker = new Object();

	public static FileCacheUtil getInstance() {

		if (instance == null) {
			synchronized (FileCacheUtil.class) {
				if (instance == null) {
					instance = new FileCacheUtil(ContextUtil.getContext());
				}
			}
		}

		// instance.init(context);
		return instance;
	}

	private FileCacheUtil(Context context) {
		this.init(context);
	}

	// 初始化
	private void init(Context context) {

		try {

			this.context = context;
			// 决定缓存位置。
			// 注：“文件”缓存使用数据库管理缓存信息，具体的文件仍然存储在文件系统里。
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// SD卡上的缓存目录
				cachePath = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY;
			} else {
				// 如没有存储卡，则在私有目录中放置缓存
				cachePath = context.getFilesDir().getPath();
			}
			// 数据库只在机身内存里存储
			databaseCachePath = context.getFilesDir().getPath();

			// 建立缓存目录
			File f = new File(cachePath);
			if (!f.exists()) {
				boolean created = f.mkdirs();
				if (!created)
					Log.w("FileCache", "Can not create the cache directory!" + f.getPath());
			}
			initDatabase();
			// 初始化valve
			this.valve = getCacheTotal();

		} catch (Exception ex) {
			Log.e("FileCache", ex.getMessage(), ex);
		}

	}

	// 数据库初始化
	// 注：在数据库中，key存储文件url加密后的字符串;
	// local_file_name存储本地实际缓存的文件名（明文，不包含路径)。
	// cachePath、dir、local_file_name三者加起来可以构造出本地实际文件全路径。
	private void initDatabase() {

		SQLiteDatabase db = null;
		Lock writer = lock.writeLock();
		if (enableManualLock)
			writer.lock();
		try {
			db = getDatabase();
			db.beginTransaction();

			// 创建表
			String sql = "CREATE TABLE IF NOT EXISTS [file_cache] " + "(  " + "  [id] integer PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT FAIL NOT NULL, " + "  [dir] text (512) NOT NULL ,  "
					+ "  [key] text (512) NOT NULL ,  " + "  [local_file_name] text (512) NOT NULL ,  " + "  [save_time] timestamp NOT NULL ,  " + "  [version] text (16) NOT NULL ,  "
					+ "  [read_count] integer NOT NULL DEFAULT 0 ,  " + "  [read_time] timestamp NOT NULL   " + ")  ";
			db.execSQL(sql);

			// 创建索引 1
			sql = "CREATE UNIQUE INDEX IF NOT EXISTS [file_cache_dir_key] On [file_cache] ( " + "[dir] Collate BINARY , " + "[key] Collate BINARY ) ";
			db.execSQL(sql);

			// 创建索引 2 (建立“最旧最远读取”索引)
			// 注：下面的索引为删除最旧的不常用缓存提供优化。
			sql = "CREATE INDEX IF NOT EXISTS [file_cache_seldom_use] On [file_cache] ( " + "[read_time] Collate BINARY ASC, " + "[save_time] Collate BINARY ASC) ";
			db.execSQL(sql);
			db.setTransactionSuccessful();

		} catch (Exception ex) {
			Log.e("FileCache", ex.getMessage(), ex);
		} finally {
			if (db != null && db.isOpen()) {
				if (db.inTransaction()) {
					db.endTransaction();
				}
				db.close();
			}
			if (enableManualLock)
				writer.unlock();
		}

	}

	// 防止数据库在运行时被删除，确保数据库、表的存在（如果不存在，则重新创建和初始化）
	private void makeSureDatabaseUsable() {

		//		File dbFile = new File(databaseCachePath + File.separator + dbName);
		//
		//		// 判断数据库文件和表是否都存在
		//		if (dbFile.exists() && tableExists()) {
		//			return;
		//		}
		//		// 否则，重新初始化
		//		synchronized (FileCacheUtil.class) {
		//			if (dbFile.exists() && tableExists()) {
		//				return;
		//			}
		//			this.init(this.context);
		//		}
	}

	// 判断表是否存在
	private boolean tableExists() {

		SQLiteDatabase db = null;
		Cursor rs = null;
		Lock reader = lock.readLock();
		if (enableManualLock)
			reader.lock();
		try {
			db = getDatabase();
			String sql = "SELECT [id], [dir], [key], [local_file_name]," + "[save_time], [version], [read_count]," + "[read_time] FROM file_cache LIMIT 1";
			rs = db.rawQuery(sql, null);
			return true;

		} catch (Exception ex) {
			Log.e("FileCache", ex.getMessage(), ex);
			return false;
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
			if (enableManualLock)
				reader.unlock();
		}
	}

	//判断数据库是否存在
	public boolean exist(String dbName){
		File dbFile = context.getDatabasePath(dbName);
		if(dbFile.exists() == true){
		return true;
		} 
		else{
		return false;
		}
	}
	
	private synchronized SQLiteDatabase getDatabase() {

		try {

			SQLiteDatabase db = null;
			try {
				if(exist(cachePath + File.separator + dbName)){
					db = SQLiteDatabase.openDatabase(cachePath + File.separator + dbName, null, 0);
				}else{
					db = SQLiteDatabase.openOrCreateDatabase(cachePath + File.separator + dbName, null);
				}
			} catch (RuntimeException e) {
				//打开失败则重新创建库
				db = SQLiteDatabase.openOrCreateDatabase(databaseCachePath + File.separator + dbName, null);
			}
			// db.setLockingEnabled(!enableManualLock);
			// //由于手工加了锁定机制，所以在这里取消数据库级别的锁定，增强性能。
			// db.setLockingEnabled(true);
			return db;
		} catch (RuntimeException e) {
			// 发生错误时，instance置空，目的是下次有机会重新初始化单例
			e.printStackTrace();
			instance = null;
			throw e;
		}
	}

	/**
	 * 检查在目录项dir中是否存在键值为key的文件。
	 * 
	 * @param dir
	 *            文件存储的目录
	 * @param key
	 *            文件关键字（文件的url）
	 * @return 存在返回true，否则返回false
	 */
	public boolean exists(String dir, String key) {

		SQLiteDatabase db = null;
		Cursor rs = null;
		Lock reader = lock.readLock();
		if (enableManualLock)
			reader.lock();
		try {
			db = getDatabase();

			String sql = "SELECT [id], [dir], [key], [local_file_name]," + "[save_time], [version], [read_count]," + "[read_time] FROM file_cache WHERE dir=? AND key=?";

			// String
			// sql="SELECT count(*) AS [count] FROM file_cache WHERE dir=? AND key=?";
			rs = db.rawQuery(sql, new String[] { dir, encrypt(key) });
			if (!rs.moveToFirst()) {
				return false;
			}
			/*
			 * String fullFileName = this.cachePath + File.separator +
			 * rs.getString(rs.getColumnIndex("dir")) + File.separator +
			 * rs.getString(rs.getColumnIndex("local_file_name"));
			 */

			// return (new File(fullFileName)).exists();
			return true; // 这里不判断本地是否有文件，只判断数据库里是否有信息。
		} catch (Exception ex) {
			Log.e("FileCache.exists()", ex.getMessage(), ex);
			return false;
		} finally {

			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
			if (enableManualLock)
				reader.unlock();
		}
	}

	// 缓存维护工具
	private static class ValveAccessor {

		// 增加valve的值，如果valve的值大于maxFileNum则自动清理缓存。
		public static synchronized int increaseValve() {
			FileCacheUtil fc = FileCacheUtil.instance;
			if (fc.valve >= fc.maxFileNum) {
				fc.cleanCache();
				fc.valve = fc.getCacheTotal();
				return fc.valve;
			} else {
				fc.valve++;
				return fc.valve;
			}
		}

		// 减少valve的值。
		public static synchronized int decreaseValve() {
			FileCacheUtil fc = FileCacheUtil.instance;
			fc.valve--;
			return fc.valve;
		}
	}

	/**
	 * 在目录dir中增加一个键值为key的文件。该方法将尝试使用key指定的url来下载文件，并添加进缓存。
	 * 
	 * @param dir
	 *            文件存储的目录名。
	 * @param key
	 *            文件关键字（文件的url）
	 * 
	 * @return 成功返回true，否则返回false
	 */
	public FileObject add(String sdir, String key) {

		return this.add(sdir, key, this.version);

	}

	/**
	 * 在目录dir中增加一个键值为key的文件。
	 * 
	 * @param dir
	 *            文件存储的目录名。
	 * @param key
	 *            文件关键字（文件的url）
	 * @param version
	 *            软件版本号
	 * 
	 * @return 成功返回一个FileObject对象，否则返回null
	 */
	public FileObject add(String sdir, String key, String version) {

		SQLiteDatabase db = null;
		String validkey = "";
		long rowId = -1;
		try {
			makeSureDatabaseUsable();
			validkey = getValidKey(key); //返回一个有效key，是从原key中把域名去掉后的值

			//构造本地文件名，用于保存在磁盘上
			String local_file_name = generateLocalFileName(key);

			// 数据库处理，先插入数据库，后写文件，保证数据库中一定有信息
			ContentValues cv = new ContentValues();
			cv.put("dir", sdir);
			cv.put("key", encrypt(validkey)); // 加密
			cv.put("local_file_name", local_file_name);
			//			if (version != null) {
			//				cv.put("version", version);
			//			} else {
			//				cv.put("version", this.version);
			//			}
			cv.put("version", key);//这里将url暂时保存在version字段中
			cv.put("save_time", CalendarUtil.getDateTimeString());
			cv.put("read_count", 0);
			cv.put("read_time", CalendarUtil.getDateTimeString());

			// -------------------------------------------------------------------------
			Lock writer = lock.writeLock();
			if (enableManualLock)
				writer.lock();
			try {
				db = getDatabase();
				rowId = db.insertOrThrow("file_cache", null, cv);
				// 这里先关闭数据库一次，释放资源，以防下面下载文件时阻塞其他线程
				db.close();
			} finally {
				if (enableManualLock)
					writer.unlock();
			}
			// -------------------------------------------------------------------------
			ValveAccessor.increaseValve();

			// 下载并保存文件，可以失败，只留数据库信息。
			String path = this.cachePath + File.separator + sdir;
			ByteArrayOutputStream bos = downloadFileToDisk(path, local_file_name, key);
			if (bos == null || bos.size() == 0) {
				return null; //失败
			}
			//--------------------------------------------------------------------------
			FileObject fo = new FileObject();
			//file.setId(rowId);
			fo.setDir(sdir);
			fo.setKey(validkey);
			fo.setLocal_file_name(local_file_name);
			//fo.setVersion(local_file_name);
			fo.setRead_count(0);
			//fo.setSave_time(CalendarUtil.getDateFromDateTimeString(fileMap.get("save_time").toString()));
			//fo.setRead_time(CalendarUtil.getDateFromDateTimeString(fileMap.get("read_time").toString()));

			fo.setFullPath(this.cachePath + File.separator + sdir);
			fo.setFullFileName(fo.getFullPath() + File.separator + fo.getLocal_file_name());

			//设置数据
			fo.setContent(bos.toByteArray());
			return fo;

		} catch (SQLiteConstraintException ex) {
			Log.e("SQLiteConstraintException", ex.getMessage());
			return null;
		} catch (Exception ex) {
			Log.e("FileCache.add()", ex.getMessage() + ",sdir=" + sdir + ",key=" + key, ex);
			return null;
		} finally {

			if (db != null && db.isOpen()) { // 确保关闭
				db.close();
				db = null;
			}
		}

	}

	private String generateDirName(String dir, String key) throws Exception {
		key = generateFileNameFromKey(key);
		//文件分散到子目录--------------------------------
		int dirnum = (key.hashCode() >>> 1) % 100;
		dir = dir + File.separator + dirnum; //目录后前加分布数字作为子目录名
		//-----------------------------------------------
		return dir;
	}

	private String generateLocalFileName(String key) throws Exception {
		key = generateFileNameFromKey(key);
		//文件分散到子目录--------------------------------
		int dirnum = (key.hashCode() >>> 1) % 100;
		String local_file_name = dirnum + key; //原文件名前加分布数字
		//-----------------------------------------------
		return local_file_name;
	}

	//返回一个有效key，是从原key中把域名去掉后的值
	private String getValidKey(String key) throws Exception {
		if (key == null) {
			return "";
		}
		int start = key.indexOf("//");
		start = key.indexOf('/', start + 2);
		return key.substring(start);
	}

	public String generateFileNameFromKey(String key) throws Exception {

		String local_file_name = null;
		key = getValidKey(key);//文件名中的域名
		local_file_name = key.replaceAll("[^\\w\\-]", "");
		return local_file_name;
	}

	/*
	 * 阻挡对同一资源并发请求的工具类，对同一资源的请求会被排队
	 */
	public static Scheduler guarder = new Scheduler();

	public static class Scheduler {
		private HashMap<String, String> keys = new HashMap<String, String>(32);

		public synchronized void request(String key) {
			while (keys.containsKey(key)) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			keys.put(key, key);
		}

		public synchronized void release(String key) {
			keys.remove(key);
			this.notifyAll();
		}
	}

	// 直接从内存缓存中取一个对象，没有取到返回null
	public FileObject getFromMemory(String dir, String key) {
		String sdir = "";
		String validKey = "";
		try {
			sdir = generateDirName(dir, key);
			validKey = getValidKey(key);

			// 尝试从内存缓存中查找
			return mmCache.get(CacheableObject.constructIdentity(new String[] { sdir, validKey }));
		} catch (Exception e) {
			Log.e("FileCacheUtil.getFromMemory()", "", e);
			return null;
		}
	}

	//判断是否是个本地文件名
	private boolean isLocalFileName(String key) {
		if (CheckUtil.isEmpty(key)) {
			return false;
		}
		if (key.startsWith("http://") || key.startsWith("https://")) {
			return false;
		} else if (key.startsWith("/")) {
			return true;
		} else {
			return false;
		}
	}

	public FileObject get(String dir, String key) {
		return get(dir, key, false);
	}

	public FileObject get(String dir, String key, boolean isLocalFileThumbnail) {
		String com = "";
		String sdir = "";
		String validkey = "";
		FileObject fo = null;
		//Log.d("get","key="+key);
		try {
			sdir = generateDirName(dir, key);
			validkey = getValidKey(key);//返回一个有效key，是从原key中把域名去掉后的值
			//-------------------------------------
			com = sdir + validkey;
			guarder.request(com);

			// 首先，尝试从内存缓存中查找
			fo = mmCache.get(CacheableObject.constructIdentity(new String[] { sdir, validkey }));
			if (fo != null) {
				return fo; // 找到则返回
			}
			/***************************************************************************************************/
			//本地文件的逻辑：如果key不是个网络url文件名，直接把key作为本地文件名，尝试从本地直接获取
			if (isLocalFileName(key)) {
				//本地文件有两个逻辑：1）需要处理成缩略图 		2）不需要处理成本地缩略图
				//1)需要处理成本地缩略图，例如：浏览相册时，可能遇到的图片都比较大，需要这个逻辑分支，否则内存会溢出
				//2)普通本地文件的逻辑，不需要缩略图
				if ((new File(key)).exists()) {//文件存在，读出来
					fo = new FileObject();
					fo.setId(0);
					fo.setDir(sdir);
					fo.setKey(validkey);
					fo.setLocal_file_name(key);
					fo.setVersion("0");
					fo.setRead_count(0);
					byte[] bytes = getFileBytes(isLocalFileThumbnail ? getThumbnail(key) : key);
					if (bytes.length > 0) {
						fo.setContent(bytes);
						return fo;
					}
				}
			}
			/***************************************************************************************************/
			// 再尝试从本地文件系统中查找
			if (!this.exists(sdir, validkey)) {
				//数据库中没有，则添加
				fo = this.add(sdir, key);

			} else {
				//数据库中有，读取				
				fo = this.getFileObject(sdir, key);
			}

			//无效数据
			if (fo == null || fo.getContent() == null) {
				return null;
			}
			//有效，加入内存缓存
			mmCache.put(fo);
			return fo;

		} catch (Exception e) {
			Log.e("FileCacheUtil.get()", "error in get()!", e);
			return null;
		} finally {
			guarder.release(com);
		}
	}

	// 将文件内容读取到字节数组中
	private byte[] getFileBytes(String fullFileName) {
		FileInputStream fin = null;
		File f = null;
		//		Log.d("读取本地",fullFileName);
		try {
			guarder.request(fullFileName);
			f = new File(fullFileName);
			if (!f.exists() || !f.isFile()) {

				return null;
			}
			long length = f.length();
			byte[] bytes = new byte[(int) length];
			fin = new FileInputStream(fullFileName);
			fin.read(bytes);
			//Log.d("读取结束",fullFileName);
			return bytes;
		} catch (Exception ex) {
			Log.e("FileCacheUtil.getFileBytes()", "error getFileByes(" + fullFileName + ")!", ex);
			return null;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (Exception ex) {
					//					Log.e("FileCacheUtil" + MyImageView.th.get(), "error in fin.close()!", ex);
				}
			}
			guarder.release(fullFileName);
		}
	}

	/**
	 * 返回目录dir中键值为key的文件，使用HashMap包装。如果没有找到则返回null。
	 * 
	 * @param dir
	 *            文件存储的目录
	 * @param key
	 *            文件关键字（文件的url）
	 * @return 返回表示文件数据的FileObject，如果没有找到则返回null。
	 */
	public FileObject getFileObject(String dir, String key) {

		SQLiteDatabase db = null;
		Cursor rs = null;
		FileObject fo = null;
		String validkey = "";
		try {

			validkey = getValidKey(key);//返回一个有效key，是从原key中把域名去掉后的值
			db = getDatabase();
			String sql = "SELECT [id], [dir], [key], [local_file_name]," + "[save_time], [version], [read_count]," + "[read_time] FROM file_cache WHERE dir=? AND key=?";

			// --------------难看的锁定---------------
			Lock writer = lock.writeLock();
			if (enableManualLock)
				writer.lock();
			try {
				rs = db.rawQuery(sql, new String[] { dir, encrypt(validkey) });

				if (!rs.moveToFirst()) {
					return null;
				}
				// 数据库文件信息
				fo = new FileObject();
				fo.setId(rs.getInt(rs.getColumnIndex("id")));
				fo.setDir(rs.getString(rs.getColumnIndex("dir")));
				fo.setKey(decrypt(rs.getString(rs.getColumnIndex("key")))); // 解密
				fo.setLocal_file_name(rs.getString(rs.getColumnIndex("local_file_name")));
				//fo.setSave_time(rs.getString(rs.getColumnIndex("save_time")));
				fo.setVersion(rs.getString(rs.getColumnIndex("version")));
				fo.setRead_count(rs.getInt(rs.getColumnIndex("read_count")));

				fo.setFullPath(this.cachePath + File.separator + fo.getDir());
				fo.setFullFileName(fo.getFullPath() + File.separator + fo.getLocal_file_name());

				//fo.setRead_time(rs.getString(rs.getColumnIndex("read_time")));

				// // 更新读取次数和读取时间（暂时屏蔽，提高性能）
				// ContentValues cv = new ContentValues();
				// cv.put("read_count",
				// rs.getInt(rs.getColumnIndex("read_count")) + 1);
				// cv.put("read_time", CalendarUtil.getDateTimeString());
				////----
				// db.update("file_cache", cv, "id=?", new String[] {
				// result.get("id").toString()});

				// 这里先关闭数据库，释放资源
				rs.close();
				db.close();
			} finally {
				if (enableManualLock)
					writer.unlock();
			}
			// --------------难看的锁定结束---------------

			// 检查文件，如果不存在则尝试重新下载文件
			String path = this.cachePath + File.separator + dir;
			String fileName = fo.getLocal_file_name();
			String url = key;
			String fullFileName = path + File.separator + fileName;
			//文件不存在，去下载
			if (!(new File(fullFileName)).exists()) {
				ByteArrayOutputStream bos = downloadFileToDisk(path, fileName, url);
				//下载失败
				if (bos == null || bos.size() == 0) {
					return null;
				}
				//下载成功
				fo.setContent(bos.toByteArray());

			} else {//文件存在，读出来
				byte[] bytes = getFileBytes(fullFileName);
				fo.setContent(bytes);
			}
			return fo;
		} catch (Exception ex) {
			Log.e("FileCache.getFileObject(...)", "dir=" + dir + ",key=" + key + " ---> " + ex.getMessage(), ex);
			return null;
		} finally {

			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * 删除目录dir中键值为key的一个文件。
	 * 
	 * @param dir
	 *            文件存储的目录
	 * @param key
	 *            文件关键字（文件的url）
	 * @return 删除成功则返回1，否则返回0。
	 */
	public int remove(String dir, String key) {

		SQLiteDatabase db = null;
		Cursor rs = null;

		int count = 0;

		// --------------难看的锁定---------------
		Lock writer = lock.writeLock();
		if (enableManualLock)
			writer.lock();

		try {
			db = getDatabase();

			String sql = "SELECT id,dir,key,local_file_name,save_time,version,read_count,read_time " + "FROM file_cache WHERE dir=? AND key=?";
			rs = db.rawQuery(sql, new String[] { dir, encrypt(key) });
			if (rs.moveToNext()) {

				String fileName = "n" + cachePath + File.separator + rs.getString(rs.getColumnIndex("dir")) + File.separator + rs.getString(rs.getColumnIndex("local_file_name"));
				File file = new File(fileName);
				// 优先删除文件系统中的文件，确保数据库中一定存在文件信息
				//				Log.d("remove" + MyImageView.th.get(), "fileName=" + fileName);
				if (!file.exists() || file.exists() && file.delete()) {
					//					Log.d("removed sucess" + MyImageView.th.get(), "fileName=" + fileName);
					try {
						db.delete("file_cache", "id=?", new String[] { rs.getString(rs.getColumnIndex("id")) });
						count++;
						ValveAccessor.decreaseValve();
					} catch (Exception ex) {
						//						Log.e("FileCache" + MyImageView.th.get(), ex.getMessage(), ex);
					}
				}
			}
			return count;
		} catch (Exception ex) {
			//			Log.e("FileCache" + MyImageView.th.get(), ex.getMessage(), ex);
			return 0;
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
			if (enableManualLock)
				writer.unlock();
		}
	}

	/**
	 * 
	 * @return 返回缓存中的文件数。
	 */
	public int getCacheTotal() {

		SQLiteDatabase db = null;
		Cursor rs = null;

		// --------------难看的锁定---------------
		Lock reader = lock.readLock();
		if (enableManualLock)
			reader.lock();

		try {
			db = getDatabase();
			String sql = "SELECT count(*) AS [count] FROM file_cache";
			rs = db.rawQuery(sql, null);
			rs.moveToNext();
			return rs.getInt(rs.getColumnIndex("count"));

		} catch (Exception ex) {
			Log.e("FileCache", ex.getMessage(), ex);
			return 0;
		} finally {

			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
			if (enableManualLock)
				reader.unlock();
		}
	}

	/**
	 * 按照规则清理缓存。 规则：当缓存中的文件数大于maxFileNum时，清除缓存中一定数量的文件。
	 * 
	 * @return 返回被清除的文件的条数。
	 */

	public int cleanCache() {
		return cleanCache(15);
	}

	public int cleanCache(int howMany) {

		SQLiteDatabase db = null;
		Cursor rs = null;
		int count = 0;

		// --------------难看的锁定---------------
		Lock writer = lock.writeLock();
		if (enableManualLock)
			writer.lock();

		try {
			db = getDatabase();
			//			int top = this.maxFileNum / 10;
			//			if (top <= 0) {
			//
			//				top = 1; // 至少清除一个文件
			//
			//			}
			// 为不至于一次删除太多的文件影响性能，一次最多删除15个文件，所以top固定为15
			int top = howMany;
			String sql = "SELECT " + "id,dir,key,local_file_name,save_time,version,read_count,read_time " + "FROM file_cache ORDER BY read_time ASC,save_time ASC LIMIT " + top;
			rs = db.rawQuery(sql, null);
			if (DEBUG || true)
				Log.d("FileCache", "<!> start to clean " + top + " file(s) in disk cache..." + "[" + this.valve + " files in disk]");
			while (rs.moveToNext()) {

				String fileName = this.cachePath + File.separator + rs.getString(rs.getColumnIndex("dir")) + File.separator + rs.getString(rs.getColumnIndex("local_file_name"));
				File file = new File(fileName);
				// 优先删除文件系统中的文件，确保数据库中一定存在文件信息
				if (!file.exists() || file.exists() && file.delete()) {
					try {
						int n = db.delete("file_cache", "id=?", new String[] { rs.getString(rs.getColumnIndex("id")) });
						if (n > 0) {
							count++;
							ValveAccessor.decreaseValve();
						}

					} catch (Exception ex) {
						Log.e("FileCache", ex.getMessage(), ex);
					}
				}
			}
			if (DEBUG)
				Log.d("FileCache", "<!> " + count + " files were cleaned successfully!" + "[" + this.valve + " files in disk]");
			return count;
		} catch (Exception ex) {
			Log.e("FileCache", ex.getMessage(), ex);
			return 0;
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
			if (enableManualLock)
				writer.unlock();
		}
	}

	/**
	 * 
	 * @param path
	 *            　文件存储的本地目录
	 * @param fileName
	 *            　指定要保存的文件名
	 * @param url
	 *            要下载的文件的url地址
	 * @return 成功返回包含数据的字节数组, 否则返回null
	 */
	public ByteArrayOutputStream downloadFileToDisk(String path, String fileName, String url) {

		InputStream in = null;
		HttpURLConnection conn = null;
		long start = System.currentTimeMillis();
		try {
			long id = start;
			conn = (HttpURLConnection) ((new URL(url)).openConnection());
			start = System.currentTimeMillis();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			start = System.currentTimeMillis();
			in = conn.getInputStream();
			start = System.currentTimeMillis();
			ByteArrayOutputStream bos = saveInputStreamToFile(in, path, fileName);
			return bos;
		} catch (Exception ex) {
			Log.e("FileCache " + start, "Fail to download file from " + url + "\n" + ex.getMessage(), ex);
			return null;
		} finally {

			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
					//					Log.e("FileCache" + MyImageView.th.get(), ex.getMessage(), ex);
				}
			}

			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception ex) {
					//					Log.e("FileCache" + MyImageView.th.get(), ex.getMessage(), ex);
				}
			}
		}
	}

	/**
	 * 为了加快执行速度，本方法将写入磁盘的工作放在线程中。
	 * 将流fileData存入path下的fileName指定的文件。本方法不负责打开和关闭流。 如果文件已经存在，则用新内容替换原文件。
	 * 注意：本方法会判断文件类型，如果是高或者宽大于700像素的图片，则自动按比例缩小到高或者宽等于700像素，防止缓存太大的图片。
	 * 
	 * @param path
	 *            文件要放置的本地目录
	 * @param fileName
	 *            文件名
	 * @return 成功返回包含数据的ByteArrayOutputStream对象，否则返回null
	 */
	static long mark = 0;

	public ByteArrayOutputStream saveInputStreamToFile(InputStream inputStream, final String path, final String fileName) {
		final ByteArrayOutputStream bos;
		try {
			bos = new ByteArrayOutputStream(1024 * 10);
			byte[] buffer = new byte[1024 * 10];
			int count = 0;
			int total = 0;
			//写入数据
			while ((count = inputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, count);
				total += count;
			}
			//Log.d("网络保存到本地","file="+path+"/"+fileName);
			//保存文件工作交给线程池
			pool.submit(new Task() {

				@Override
				public void run() {
					String fullFileName = path + File.separator + fileName;
					//创建目录
					File f = new File(path);
					if (!f.exists()) {
						f.mkdirs();
					}
					//保存文件
					FileOutputStream fout = null;
					try {
						guarder.request(fullFileName);
						fout = new FileOutputStream(fullFileName);
						bos.writeTo(fout);
					} catch (Exception ex) {
						Log.e("FileCache,thread pool", fileName, ex);
					} finally {
						if (fout != null) {
							try {
								fout.close();

							} catch (Exception ex) {
							}
						}
						//检查文件，如果长度为0则是无效文件，删除
						File file = new File(fullFileName);
						if (file.exists() && file.length() == 0) {
							file.delete();
						}
						guarder.release(fullFileName);
					}

				}
			});

			//----
			return bos;

		} catch (Exception ex) {
			Log.e("FileCache,saveInputStreamToFile()", fileName, ex);
			return null;
		}

		// 缓冲读取
		//BufferedInputStream fileData = new BufferedInputStream(fileInputStream, 1024 * 105);
		// fileData.mark(1024 * 50);

		// 如果是图片，并且需要限制边长
		// if (FileCacheUtil.maxSideOfBitmap > 0 && isBitmap(new
		// BufferedInputStream(fileData, 1024 * 20))) {
		// fileData.reset();
		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inJustDecodeBounds = true;
		// BitmapFactory.decodeStream(fileData, null, options);
		// fileData.reset();
		//
		// // 超过最大边长
		// if (options.outHeight > FileCacheUtil.maxSideOfBitmap ||
		// options.outWidth > FileCacheUtil.maxSideOfBitmap) {
		//
		// // 缩放比例
		// float scale = 1;
		// if (options.outHeight > options.outWidth) {
		//
		// scale = FileCacheUtil.maxSideOfBitmap / ((float)
		// options.outHeight);
		// } else {
		// scale = FileCacheUtil.maxSideOfBitmap / ((float)
		// options.outWidth);
		// }
		//
		// options.inJustDecodeBounds = false;
		// options.inSampleSize = 1;
		// Bitmap oldPic = null;
		// Bitmap newPic = null;
		// try {
		// oldPic = BitmapFactory.decodeStream(fileData, null, options);
		// newPic = Bitmap.createScaledBitmap(oldPic, (int)
		// (options.outWidth * scale), (int) (options.outHeight * scale),
		// true);
		//
		// // synchronized (locker) {
		// fout = new FileOutputStream(fullFileName);
		// newPic.compress(CompressFormat.JPEG, 66, fout);
		// fout.close();
		// // }
		// } finally {
		// if (oldPic != null) {
		// oldPic.recycle();
		// }
		// if (newPic != null) {
		// newPic.recycle();
		// }
		// }
		//
		// return true;
		// }
		// }
		// 保存原始文件
		// }

	}

	/**
	 * @param stream
	 *            输入流，用以判断是否是个图片。
	 * @return true是图片，false不是图片。
	 */
	public boolean isBitmap(File file, BitmapFactory.Options options) {
		FileInputStream finput = null;
		try{
			finput=new FileInputStream(file);
			BitmapFactory.decodeStream(finput, null, options);
			if (options.outHeight > 0 && options.outHeight > 0) {
				return true;
			} else {
				return false;
			}
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally{
			if(finput!=null){
				try {
					finput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * 生成图片缩略图，按比例缩小，最大边长不超过maxSideOfBitmap像素
	 */
	public String getThumbnail(String fullFileName) {
		int maxSideOfBitmap = 150;
		String thumbDir = cachePath + File.separator + "thumb";
		String outFullFileName = thumbDir + File.separator + fullFileName.replaceAll("_", "__").replaceAll("/", "_").replaceAll("-", "--").replaceAll("\\.", "-");
		// --
		File dir = new File(thumbDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// --
		File inFile = new File(fullFileName);
		if (!inFile.isFile() || !inFile.exists()) {
			return null;
		}
		outFullFileName = outFullFileName + "_" + inFile.length() + "_" + inFile.lastModified();
		File outFile = new File(outFullFileName);
		if (outFile.isFile() && outFile.exists()) {
			//Log.e("本地缩略图","命中"+outFullFileName);
			return outFullFileName;
		}
		// ---------------------------------
		FileInputStream finput = null;
		FileOutputStream foutput = null;
		Bitmap pic = null;
		try {
			// 缓冲读取
			finput = new FileInputStream(inFile);
			foutput = new FileOutputStream(outFile);
			finput.mark(1024 * 1023);
			// --
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 如果是图片
			if (isBitmap(inFile, options)) {
				float scale = 1;
				// 超过最大边长
				if (options.outHeight > maxSideOfBitmap || options.outWidth > maxSideOfBitmap) {
					// 缩放比例
					if (options.outHeight > options.outWidth) {
						scale = maxSideOfBitmap / ((float) options.outHeight);
					} else {
						scale = maxSideOfBitmap / ((float) options.outWidth);
					}
				}
				options.inJustDecodeBounds = false;
				options.inSampleSize = (int) Math.ceil(1 / scale);
				// --
				pic = BitmapFactory.decodeStream(finput, null, options);
				pic.compress(CompressFormat.JPEG, 66, foutput);
				return outFullFileName;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (outFile != null) {
					outFile.delete();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} finally {
			try {
				if (pic != null) {
					pic.recycle();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// ---
			try {
				if (finput != null) {
					finput.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// ---
			try {
				if (foutput != null) {
					foutput.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 使用key加密一个字符串str
	 * 
	 * @param str
	 *            要被加密的字符串
	 * @param key
	 *            密钥
	 * @return 加密后的字符串
	 */
	public String encrypt(String str, String key) {
		// 出于性能考虑，这里先屏蔽加密功能
		return str;

		/*
		 * try {
		 * 
		 * String byteCharset = "ISO-8859-1"; String encryption = "AES"; byte[]
		 * strBytes = str.getBytes(); byte[] keyBytes =
		 * to256bit(key.getBytes(byteCharset));
		 * 
		 * Cipher cipher = Cipher.getInstance(encryption); SecretKeySpec
		 * securekey = new SecretKeySpec(keyBytes, encryption);
		 * cipher.init(Cipher.ENCRYPT_MODE, securekey);// 设置密钥和加密形式 byte[]
		 * secBytes=cipher.doFinal(strBytes); return
		 * Base64.encode(secBytes,0,secBytes.length); } catch (Exception ex) {
		 * 
		 * Log.e("encrypt", ex.getMessage(), ex); return str;
		 * 
		 * }
		 */

	}

	/**
	 * 加密一个字符串str。使用缺省密钥。
	 * 
	 * @param str
	 *            要被加密的字符串
	 * @return 加密后的字符串
	 */
	public String encrypt(String str) {
		return encrypt(str, this.secretKey);
	}

	/**
	 * 使用key解密字符串str
	 * 
	 * @param str
	 *            要解密的字符串
	 * @param key
	 *            密钥
	 * 
	 * @return 解密后的字符串
	 */
	public String decrypt(String str, String key) {
		// 出于性能考虑，这里先屏蔽解密功能
		return str;
		/*
		 * try { String byteCharset = "ISO-8859-1"; String encryption = "AES";
		 * byte[] strBytes = Base64.decode(str);
		 * 
		 * byte[] keyBytes = to256bit(key.getBytes(byteCharset));
		 * 
		 * Cipher cipher = Cipher.getInstance(encryption); SecretKeySpec
		 * securekey = new SecretKeySpec(keyBytes, encryption);
		 * cipher.init(Cipher.DECRYPT_MODE, securekey);// 设置密钥和解密形式 return new
		 * String(cipher.doFinal(strBytes)); } catch (Exception ex) {
		 * Log.e("decrypt", ex.getMessage(), ex); return str; }
		 */
	}

	/**
	 * 解密字符串str，使用缺省密钥。
	 * 
	 * @param str
	 *            要解密的字符串
	 * 
	 * @return 解密后的字符串
	 */
	public String decrypt(String str) {
		return decrypt(str, this.secretKey);
	}

	// 将传入的bytes补齐到256bit，多退少补
	private byte[] to256bit(byte[] bytes) {
		byte[] result = new byte[32];
		System.arraycopy(bytes, 0, result, 0, bytes.length > 32 ? 32 : bytes.length);
		return result;

	}

	/**
	 * 维护缓存，该方法在适合的地方调用（一般在没有大量图片请求的页面，比如首页）。
	 * 当缓存文件的数量大于一个阀值（小于最大值的某个值，例如最大２０００个文件，阀值定为１9００），进行一次小清理
	 * 目的是，尽可能避免缓存真正达到最大值，避免add的时候进入dump程序（影响用户体验）。
	 */
	public void maintain() {
		int valve = (int) (maxFileNum * 0.9);
		if (this.valve < valve) {
			return;
		}
		//工作交给线程池
		pool.submit(new Task() {
			@Override
			public void run() {
				cleanCache(50);
			}
		});

	}
}
