package com.fg114.main.cache;

import java.util.*;
import java.io.File;
import com.fg114.main.app.*;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.ContextUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.*;
import com.fg114.main.app.Settings;
import static com.fg114.main.app.Settings.*;

/*
 * Creator: xu jianjun, 2011-11-10
 * 
 * 提供接口实现"数据"缓存的维护。
 * 此类尽量少抛出异常。
 *
 */
public class ValueCacheUtil {

	// 缓存的默认更新戳记
	private String updateStamp = "1";

	// 默认软件版本
	private String version = "-";

	// 缓存的默认超时分钟数
	// 目前“值”缓存没有自动清理。所以几乎不用超时，由程序通过updateStamp自己决定更新或者清除。
	private int expireMinute = 365 * 24 * 60;

	// 缓存数据库名
	private String dbName = "value_cache.db";

	// 缓存数据库所在目录
	private String cachePath;

	private Context context = null;

	// 内存缓冲，进一步优化读取速度
	public MemoryCache<ValueObject> mmCache = new MemoryCache<ValueObject>(Settings.CACHE_MEMORY_VALUE_TOTAL_SIZE, "MemoryCache-Value");

	private static ValueCacheUtil instance;

	public static ValueCacheUtil getInstance(Context context) {

		if (instance == null) {
			synchronized (ValueCacheUtil.class) {
				if (instance == null) {
					instance = new ValueCacheUtil(ContextUtil.getContext());
				}
			}
		}

		return instance;
	}

	private ValueCacheUtil(Context context) {
		this.init(context);
	}

	// 初始化
	private void init(Context context) {

		try {

			this.context = context;
			// 决定缓存位置。
			// 注：目前“数据”缓存只存在数据库里，不存在文件里。cachePath只供文件缓存使用。
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// SD卡上的缓存目录
				cachePath = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY;
			} else {
				// 如没有存储卡，则在私有目录中放置缓存
				cachePath = context.getFilesDir().getPath();
			}
			// 暂时只在机身内存里存储
			cachePath = context.getFilesDir().getPath();
			// 建立缓存目录
			File f = new File(cachePath);
			if (!f.exists()) {
				boolean created = f.mkdirs();
				if (!created)
					Log.w("ValueCache", "Can not create the cache directory!" + f.getPath());
			}
			initDatabase();

		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
		}

	}

	// 数据库初始化
	private void initDatabase() {

		SQLiteDatabase db = null;
		try {
			db = getDatabase();
			db.beginTransaction();

			// 创建表
			String sql = "CREATE TABLE IF NOT EXISTS [value_cache] " + "(  " + "  [id] integer PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT FAIL NOT NULL, "
					+ "  [dir] text (512) NOT NULL ,  " + "  [key] text (512) NOT NULL ,  " + "  [value] text (524288) NOT NULL ,  "
					+ "  [update_stamp] text (64) NOT NULL ,  " + "  [save_time] timestamp NOT NULL ,  " + "  [expire_minute] integer NOT NULL ,  "
					+ "  [version] text (16) NOT NULL ,  " + "  [read_count] integer NOT NULL DEFAULT 0 ,  " + "  [read_time] timestamp NOT NULL   " + ")  ";
			db.execSQL(sql);

			// 创建索引 1
			sql = "CREATE UNIQUE INDEX IF NOT EXISTS [value_cache_dir_key] On [value_cache] ( " + "[dir] Collate BINARY , " + "[key] Collate BINARY ) ";
			db.execSQL(sql);

			// 创建索引 2 (建立“最旧最远读取”索引)
			// 注：目前“数据”缓存只使用超时策略，不使用最大条数限制，下面的索引是预留的。
			sql = "CREATE INDEX IF NOT EXISTS [value_cache_seldom_use] On [value_cache] ( " + "[read_time] Collate BINARY ASC, "
					+ "[save_time] Collate BINARY ASC) ";
			db.execSQL(sql);
			db.setTransactionSuccessful();

		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
		} finally {
			if (db != null && db.isOpen()) {
				if (db.inTransaction()) {
					db.endTransaction();
				}
				db.close();
			}
		}

	}

	// 防止数据库在运行时被删除，确保数据库、表的存在（如果不存在，则重新创建和初始化）
	private void makeSureDatabaseUsable() {

		File dbFile = new File(cachePath + File.separator + dbName);

		// 判断数据库文件和表是否都存在
		if (dbFile.exists() && tableExists()) {
			return;
		}
		// 否则，重新初始化
		synchronized (FileCacheUtil.class) {
			if (dbFile.exists() && tableExists()) {
				return;
			}
			this.init(this.context);
		}
	}

	// 判断表是否存在
	private boolean tableExists() {

		SQLiteDatabase db = null;
		Cursor rs = null;
		try {
			db = getDatabase();
			String sql = "SELECT [id], [dir], [key], [value], [update_stamp]," + "[save_time], [expire_minute], [version], [read_count],"
					+ "[read_time] FROM value_cache LIMIT 1";
			rs = db.rawQuery(sql, null);
			return true;

		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return false;
		} finally {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
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
			SQLiteDatabase db =null;
			try{
				if(exist(cachePath + File.separator + dbName)){
					db = SQLiteDatabase.openDatabase(cachePath + File.separator + dbName, null, 0);
				}else{
					db = SQLiteDatabase.openOrCreateDatabase(cachePath + File.separator + dbName, null);
				}
				
			}catch (RuntimeException e) {
				//打开失败则重新创建库
				db = SQLiteDatabase.openOrCreateDatabase(cachePath + File.separator + dbName, null);	
			}
			return db;
		} catch (RuntimeException e) {
			// 发生错误时，instance置空，目的是下次有机会重新初始化单例
			e.printStackTrace();
			instance = null;
			throw e;
		}
	}

	/**
	 * 检查在目录项dir中是否存在键值为key的数据。
	 * 
	 * @param dir
	 *            数据存储的目录
	 * @param key
	 *            数据关键字
	 * @return 存在返回true，否则返回false
	 */
	public boolean exists(String dir, String key) {

		SQLiteDatabase db = null;
		Cursor rs = null;
		try {
			db = getDatabase();
			// 直接判断本地文件系统的缓存，不以内存缓存为依据。
			String sql = "SELECT count(*) AS [count] FROM value_cache WHERE dir=? AND key=?";
			rs = db.rawQuery(sql, new String[] { dir, key });
			if (!rs.moveToNext()) {
				return false;
			}
			if (rs.getInt(rs.getColumnIndex("count")) != 1) {
				return false;
			}
			return true;
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
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
		}
	}

	/**
	 * 在目录项dir中更新一个键值为key的数据value。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * @param updateStamp
	 *            数据的版本戳记
	 * @param version
	 *            软件的版本
	 * @param expireMinute
	 *            指定数据过多少时间失效，分钟数。指定-1可以使缓存永不过期。
	 * @return 成功true，失败返回false。
	 */
	public boolean update(String dir, String key, String value, String updateStamp, String version, int expireMinute) {

		SQLiteDatabase db = null;
		try {
			db = getDatabase();

			// 更新本地之前，总是简单的将内存缓存中的数据删除，
			// 无论后面的更新成功与否，下次get的时候会自动和将数据库中的版本加入内存
			mmCache.remove(CacheableObject.constructIdentity(new String[] { dir, key }));
			//
			ContentValues cv = new ContentValues();
			// cv.put("dir", dir);
			// cv.put("key", key);
			cv.put("value", value);
			if (updateStamp != null) {

				cv.put("update_stamp", updateStamp);
			}

			if (expireMinute >= 0) {

				cv.put("expire_minute", expireMinute);
			} else {

				cv.put("expire_minute", -1);
			}

			if (version != null) {
				cv.put("version", version);
			}

			cv.put("read_count", 0);
			cv.put("read_time", CalendarUtil.getDateTimeString());

			db.update("value_cache", cv, "dir=? AND key=?", new String[] { dir, key });

			return true;
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return false;
		} finally {

			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * 在目录项dir中更新一个键值为key的数据value。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * 
	 * @return 成功true，失败返回false。
	 */
	public boolean update(String dir, String key, String value) {
		return this.update(dir, key, value, this.updateStamp, null, this.expireMinute);
	}

	/**
	 * 在目录项dir中更新一个键值为key的数据value。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * @param updateStamp
	 *            数据的版本戳记
	 * 
	 * @return 成功true，失败返回false。
	 */
	public boolean update(String dir, String key, String value, String updateStamp) {
		return this.update(dir, key, value, updateStamp, null, this.expireMinute);
	}

	/**
	 * 在目录项dir中新增一个键值为key的数据value。 如果缓存中已经存在该值，则返回false。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * @param updateStamp
	 *            数据的版本戳记
	 * @param version
	 *            软件的版本
	 * @param expireMinute
	 *            指定数据过多少时间失效，分钟数。指定-1可以使缓存永不过期。
	 * @return 成功true，失败返回false。
	 */
	public boolean add(String dir, String key, String value, String updateStamp, String version, int expireMinute) {

		SQLiteDatabase db = null;

		try {
			makeSureDatabaseUsable();
			db = getDatabase();
			/*
			 * 因为有自动删除过期数据的功能，所以可能内存中会有幽灵数据（本地数据库中不存在）
			 * 所以在add的时候，主动删除一下内存中的相应数据，以达到同步的目的
			 */
			mmCache.remove(CacheableObject.constructIdentity(new String[] { dir, key }));

			ContentValues cv = new ContentValues();
			cv.put("dir", dir);
			cv.put("key", key);
			cv.put("value", value);
			// ///updateStamp
			if (updateStamp != null) {

				cv.put("update_stamp", updateStamp);
			} else {

				cv.put("update_stamp", "1");
			}
			// ///expireMinute
			if (expireMinute >= 0) {

				cv.put("expire_minute", expireMinute);
			} else {

				cv.put("expire_minute", -1);
			}
			// ///version
			if (version != null) {

				cv.put("version", version);
			} else {

				cv.put("version", this.version);
			}
			cv.put("save_time", CalendarUtil.getDateTimeString());
			cv.put("read_count", 0);
			cv.put("read_time", CalendarUtil.getDateTimeString());

			db.insert("value_cache", null, cv);

			return true;
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return false;
		} finally {

			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * 在目录项dir中新增一个键值为key的数据value。 如果缓存中已经存在该值，则返回false。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * @return 成功true，失败返回false。
	 */
	public boolean add(String dir, String key, String value) {

		return this.add(dir, key, value, updateStamp, null, this.expireMinute);
	}

	/**
	 * 在目录项dir中新增一个键值为key的数据value。 如果缓存中已经存在该值，则返回false。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @param value
	 *            数据
	 * @param updateStamp
	 *            数据的版本戳记
	 * 
	 * @return 成功true，失败返回false。
	 */
	public boolean add(String dir, String key, String value, String updateStamp) {

		return this.add(dir, key, value, updateStamp, null, this.expireMinute);
	}

	/**
	 * 返回目录项dir中键值为key的缓存数据。 首先查找内存缓存，然后查找本地文件系统缓存。如果都没有找到则返回null。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @return 以ValueObject对象，表示一条缓存的数据。如果没有找到缓存，返回null。
	 */
	public ValueObject get(String dir, String key) {

		// 首先，尝试从内存缓存中查找
		ValueObject vo = mmCache.get(CacheableObject.constructIdentity(new String[] { dir, key }));

		if (vo != null) {
			if (DEBUG)
				Log.d("ValueCacheUtil", "found in memeory... get(" + dir + "," + key + ")");
			return vo; // 找到则返回
		} else {
			if (DEBUG)
				Log.d("ValueCacheUtil", "not found in memeory! get(" + dir + "," + key + ")");
		}

		// 再尝试从本地文件系统中查找
		HashMap<String, Object> valueMap = this.getValueMap(dir, key);
		if (valueMap == null) {
			// 如果未找到，返回null
			if (DEBUG)
				Log.d("ValueCacheUtil", "not found in disk db! get(" + dir + "," + key + ")");
			return null;
		} else {
			if (DEBUG)
				Log.d("ValueCacheUtil", "found in disk db! get(" + dir + "," + key + ")");
		}

		ValueObject value = new ValueObject();
		value.id = Integer.parseInt(valueMap.get("id").toString());
		value.dir = dir;
		value.key = key;
		value.value = valueMap.get("value").toString();
		value.expire_minute = Integer.parseInt(valueMap.get("expire_minute").toString());
		value.update_stamp = valueMap.get("update_stamp").toString();
		value.version = valueMap.get("version").toString();
		value.read_count = Integer.parseInt(valueMap.get("read_count").toString());
		value.save_time = CalendarUtil.getDateFromDateTimeString(valueMap.get("save_time").toString());
		value.read_time = CalendarUtil.getDateFromDateTimeString(valueMap.get("read_time").toString());
		// 加入内存缓冲
		mmCache.put(value);
		return value;

	}

	/**
	 * 返回目录项dir中键值为key的缓存数据，使用HashMap包装。如果没有找到数据则返回null。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @return 
	 *         以HashMap形式返回一条缓存的数据，HashMap的key名为表value_cache的字段名。如果没有找到数据则返回null。
	 */
	public HashMap<String, Object> getValueMap(String dir, String key) {

		SQLiteDatabase db = null;
		Cursor rs = null;
		HashMap<String, Object> result = null;

		try {
			db = getDatabase();
			String sql = "SELECT [id], [dir], [key], [value], [update_stamp]," + "[save_time], [expire_minute], [version], [read_count],"
					+ "[read_time] FROM value_cache WHERE dir=? AND key=?";
			rs = db.rawQuery(sql, new String[] { dir, key });

			if (rs.moveToFirst()) {
				result = new HashMap<String, Object>();
				result.put("id", rs.getInt(rs.getColumnIndex("id")));
				result.put("dir", rs.getString(rs.getColumnIndex("dir")));
				result.put("key", rs.getString(rs.getColumnIndex("key")));
				result.put("value", rs.getString(rs.getColumnIndex("value")));
				result.put("update_stamp", rs.getString(rs.getColumnIndex("update_stamp")));
				result.put("save_time", rs.getString(rs.getColumnIndex("save_time")));
				result.put("expire_minute", rs.getInt(rs.getColumnIndex("expire_minute")));
				result.put("version", rs.getString(rs.getColumnIndex("version")));
				result.put("read_count", rs.getInt(rs.getColumnIndex("read_count")));
				result.put("read_time", rs.getString(rs.getColumnIndex("read_time")));

				// 更新读取次数和读取时间
				ContentValues cv = new ContentValues();
				cv.put("read_count", rs.getInt(rs.getColumnIndex("read_count")) + 1);
				cv.put("read_time", CalendarUtil.getDateTimeString());

				db.update("value_cache", cv, "id=?", new String[] { String.valueOf(rs.getInt(rs.getColumnIndex("id"))) });
			}

			return result;
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
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
	 * 删除目录项dir中键值为key的一条数据。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @param key
	 *            数据关键字
	 * @return 删除成功则返回1，否则返回0。
	 */
	public int remove(String dir, String key) {

		SQLiteDatabase db = null;
		try {
			db = getDatabase();

			// 更新本地之前，总是简单的将内存缓存中的数据删除，
			// 无论后面的更新成功与否，下次get的时候会自动和将数据库中的版本加入内存
			mmCache.remove(CacheableObject.constructIdentity(new String[] { dir, key }));
			//
			return db.delete("value_cache", "dir=? AND key=?", new String[] { dir, key });
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * 删除所有目录项为dir的数据。
	 * 
	 * @param dir
	 *            数据存储的目录项
	 * @return 返回被删除的数据的条数。
	 */
	public int removeDir(String dir) {

		SQLiteDatabase db = null;
		try {
			db = getDatabase();
			// 这里应该删除所有的内存缓冲，再议
			return db.delete("value_cache", "dir=?", new String[] { dir });
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}

	/**
	 * 按照规则清理缓存。 规则：根据save_time和expire_time判断，清除所有过期的缓存。
	 * 数据库中expire_minute=-1的是永不超时的。
	 * 
	 * @return 返回被清除的缓存的条数。
	 */
	public int cleanCache() {

		SQLiteDatabase db = null;
		try {
			db = getDatabase();
			// 这里不清除删除的内存缓冲
			String where = "(case when expire_minute >=0 " + " and datetime(save_time,'+'||expire_minute||' minute')"
					+ " < datetime('now','localtime')then 1 else 0 end)=1";
			return db.delete("value_cache", where, null);
		} catch (Exception ex) {
			Log.e("ValueCache", ex.getMessage(), ex);
			return 0;
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
				db = null;
			}
		}
	}
}
