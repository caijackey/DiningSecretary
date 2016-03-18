package com.fg114.main.app.service;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.params.*;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.usercenter.APPSettingActivity;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;



/**
 * 软件更新服务
 * @author wufucheng
 */
public class UpdateService extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = UpdateService.class.getName();
	private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
	private static final int NOTIFICATION_ID = UpdateService.class.getName().hashCode();

	private static final String ACTION_START = "com.xiaomishu.task.update.START";
	private static final String ACTION_STOP = "com.xiaomishu.task.update.STOP";
	private static final String ACTION_FINISH = "com.xiaomishu.task.update.FINISH";
	private static final String ACTION_FAIL = "com.xiaomishu.task.update.FAIL";

	// 下载服务的配置
	private static final boolean AUTO_INSTALL = true; // 是否在下载完成后自动安装
	private static final int MAX_AUTO_RETRY_TIMES = 10; // 下载未完整的最大重试次数，超过最大重试次数将显示为下载失败

	// 下载配置
	private static final String DOWNLOAD_DIR = "/"+Settings.IMAGE_CACHE_DIRECTORY+"/download/"; // 下载目录
//	private static final String DOWNLOAD_DIR = "/download/"; // 下载目录
	private static final int BUFFER_SIZE = 1024 * 10;
	private static final int CONNECT_TIMEOUT = 1000 * 20;
	private static final int READ_TIMEOUT = 1000 * 60;

	// 下载状态
	private final static int DOWNLOAD_START = 0; // 开始
	private final static int DOWNLOAD_DOING = 1; // 正在下载
	private final static int DOWNLOAD_COMPLETE = 2; // 下载完成
	private final static int DOWNLOAD_FAIL = 3; // 下载失败
	private final static int DOWNLOAD_INCOMPLETE = 4; // 下载的文件不完整
	private final static int DOWNLOAD_CANCEL = 5; // 下载被取消

	private static Intent mStartDownloadIntent; // 开始下载的Intent，服务开始时传入，失败重试时用到
	private AtomicBoolean mCanDownload = new AtomicBoolean(true); // 是否能执行下载
	private NotificationManager mNotificationManager;
	private RemoteViews mRemoteViews;
	private boolean mDownloadStarted; // 下载是否已开始
	private String mAppName = ""; // 软件名称
	private int mMaxRetryTimes; // 下载未完整的重试次数
	private String apkSize;
	

	// 处理更新的Handler
	private Handler mUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_START:
				// 开始下载
				showNotification(DOWNLOAD_START, 0, null);
				break;
			case DOWNLOAD_DOING:
				// 正在下载
				showNotification(DOWNLOAD_DOING, msg.arg1, null);
				break;
			case DOWNLOAD_COMPLETE:
				// 下载完成
				if (msg.obj == null || !(msg.obj instanceof File)) {
					return;
				}
				File updateFile = (File) msg.obj;
				// 点击安装
				Uri uri = Uri.fromFile(updateFile);
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(uri, APK_MIME_TYPE);
				installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				if (AUTO_INSTALL) {
					startActivity(installIntent);
					clearNotification();
				} else {
					showNotification(DOWNLOAD_COMPLETE, 100, installIntent);
				}
				actionFinish(UpdateService.this);
				break;
			case DOWNLOAD_FAIL:
				// 下载失败
				showNotification(DOWNLOAD_FAIL, 0, mStartDownloadIntent);
				actionFail(UpdateService.this);
				break;
			case DOWNLOAD_INCOMPLETE:
				// 下载的文件不完整
				mDownloadStarted = false;
				clearNotification();
				startService(mStartDownloadIntent);
				break;
			case DOWNLOAD_CANCEL:
				// 下载被取消
				mDownloadStarted = false;
				clearNotification();
				break;
			default:
				actionStop(UpdateService.this);
				break;
			}
		}
	};

	/**
	 * 下载更新的Runnable
	 * @author wufucheng
	 */
	class UpdateRunnable implements Runnable {

		private File mFileLocal; // 下载的文件
		private String mUrl; // 下载的url

		public UpdateRunnable(File localFile, String url) {
			mFileLocal = localFile;
			mUrl = url;
		}

		public void run() {
			try {
				if (mFileLocal == null || TextUtils.isEmpty(mUrl)) {
					mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAIL);
					return;
				}
				if (!mFileLocal.exists()) {
					File dir = mFileLocal.getParentFile();
					if (!dir.exists()) {
						dir.mkdirs();
					}
					mFileLocal.createNewFile();
				}
				long downloadSize = downloadUpdateFile(mUrl, mFileLocal);
				
				
				
					if (downloadSize == Integer.parseInt(apkSize)) {
						// 下载成功
						Log.e(TAG, "下载成功>>>>>>>>>>");
						mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(DOWNLOAD_COMPLETE, mFileLocal));
					} else {
						// 下载失败
						Log.e(TAG, "下载失败>>>>>>>>>>>");
						mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAIL);
					}
			} catch (Exception e) {
				e.printStackTrace();
				// 下载失败
				log(e);
				if (String.valueOf(DOWNLOAD_INCOMPLETE).equals(e.getMessage())) {
					mUpdateHandler.sendEmptyMessage(DOWNLOAD_INCOMPLETE);
				} else if (String.valueOf(DOWNLOAD_CANCEL).equals(e.getMessage())) {
					mUpdateHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
				} else {
					mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAIL);
				}
			}
		}
	}

	/**
	 * 启动服务
	 * @param context
	 * @param url 下载的url
	 * @param saveName 本地保存的文件名
	 * @param appName 软件名称
	 */
	public static void actionStart(Context context, String url,String appName) {
		try {
			Bundle extras = new Bundle();
			extras.putString(Settings.BUNDLE_UPDATE_URL, url);
			extras.putString(Settings.BUNDLE_UPDATE_APP_NAME, appName);
			actionStart(context, extras);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	/**
	 * 启动服务
	 * @param context
	 * @param extras
	 */
	public static void actionStart(Context context, Bundle extras) {
		try {
			Intent intent = new Intent(context, UpdateService.class);
			intent.setAction(ACTION_START);
			intent.putExtras(extras);
			mStartDownloadIntent = intent;
			context.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	/**
	 * 停止服务
	 * @param context
	 */
	public static void actionStop(Context context) {
		try {
			Intent intent = new Intent(context, UpdateService.class);
			intent.setAction(ACTION_STOP);
			context.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	/**
	 * 下载完成
	 * @param context
	 */
	public static void actionFinish(Context context) {
		try {
			Intent intent = new Intent(context, UpdateService.class);
			intent.setAction(ACTION_FINISH);
			context.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	/**
	 * 下载失败
	 * @param context
	 */
	public static void actionFail(Context context) {
		try {
				Intent intent = new Intent(context, UpdateService.class);
				intent.setAction(ACTION_FAIL);
				context.startService(intent);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	@Override
	public void onCreate() {
		try {
			super.onCreate();
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mRemoteViews = new RemoteViews(getPackageName(), R.layout.frame_download_notification);
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Message msg=new Message();
		msg.what=200;
		if (intent == null) {
			log("Intent is null");
			stopDownload(true);
			clearNotification();
			stopSelf();
			return;
		}
		if (intent.getAction().equals(ACTION_START)) {
			if (mDownloadStarted) {
				// 不重复开始下载
				APPSettingActivity.handler.sendMessage(APPSettingActivity.handler.obtainMessage(200,5,1));
				return;
			}else{
				DialogUtil.showToast(getApplicationContext(), "开始下载更新...");
			}
			
			msg.arg1 = 0;
			
			startDownload(intent);
		} else if (intent.getAction().equals(ACTION_STOP)) {
			msg.arg1 = 1;
			stopDownload(true);
			stopSelf();
			mDownloadStarted = false;
		} else if (intent.getAction().equals(ACTION_FINISH)) {
			msg.arg1 = 2;
			stopSelf();
			mDownloadStarted = false;
		} else if (intent.getAction().equals(ACTION_FAIL)) {
			msg.arg1 = 1;
			stopDownload(false);
			stopSelf();
			mDownloadStarted = false;
		}
		
		APPSettingActivity.handler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
		mDownloadStarted = false;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void startDownload(Intent intent) {
		try {
			// 获取传值
			if (intent == null || intent.getExtras() == null || !intent.getExtras().containsKey(Settings.BUNDLE_UPDATE_URL)) {
				return;
			}
			
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(ContextUtil.getContext());
			if (!isNetAvailable) {
				DialogUtil.showToast(ContextUtil.getContext(), "未连接网络");
				return ;
			}
			
			String url = intent.getStringExtra(Settings.BUNDLE_UPDATE_URL);
			mAppName = intent.getStringExtra(Settings.BUNDLE_UPDATE_APP_NAME);
			if (TextUtils.isEmpty(url)) {
				return;
			}
			if (mAppName == null) {
				mAppName = "";
			}
//			if (TextUtils.isEmpty(saveName)) {
//				saveName = mAppName + "_newVersion.apk";
//			}
			apkSize = String.valueOf(computeApkFileSize(url));
			String apkVersionHash=String.valueOf(Settings.gVersionChkDTO.newVersion.hashCode());
			String saveName =  apkSize+"_"+apkVersionHash+"_newVersion.apk";
			mDownloadStarted = true;
			// 创建文件
			File updateFile = null;
			if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
				updateFile = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR, saveName);
			} else {
				updateFile = new File(getFilesDir().getPath() + DOWNLOAD_DIR, saveName);
			}

			mUpdateHandler.sendEmptyMessage(DOWNLOAD_START);
			// 开启一个新的线程下载
			new Thread(new UpdateRunnable(updateFile, url)).start();
		} catch (Exception e) {
			e.printStackTrace();
			log(e);
			mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAIL);
		}
	}

	private void stopDownload(boolean clearNotification) {
		mCanDownload.set(false);
		if (clearNotification) {
			clearNotification();
		}
	}

	private void clearNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}

	private long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
		long downloadProgress = 0;
		long currentTotalSize = 0;
		long updateFileTotalSize = 0;
		DefaultHttpClient client = null;
		DefaultHttpClient client_test = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			client = new DefaultHttpClient();
			client_test = new DefaultHttpClient();
			HttpGet request = new HttpGet(downloadUrl);
			HttpGet request_test = new HttpGet(downloadUrl);

			setHttpClientParams(client);
			setHttpClientParams(client_test);

			long pos = saveFile.length();

			HttpResponse response_test = client_test.execute(request_test);
			// 获取需要下载文件的大小
			updateFileTotalSize = response_test.getEntity().getContentLength();
			if (pos != 0 && updateFileTotalSize == pos) {
				// 已下载的文件大小和获取到的文件大小相同时认为之前已下载完成
				return updateFileTotalSize;
			} else if (pos != 0 && pos < updateFileTotalSize) {
				// 设置下载的数据位置XX字节到XX字节 没有下载完成
				Header header_size = new BasicHeader("RANGE", "bytes=" + pos + "-"); // Range区分大小写
				request.addHeader(header_size);
				currentTotalSize = pos;
			}
			HttpResponse response = client.execute(request);
			is = response.getEntity().getContent();

			fos = new FileOutputStream(saveFile, true);
			byte buffer[] = new byte[BUFFER_SIZE];
			int readsize = 0;
			mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(DOWNLOAD_DOING, 0, 0));
			while ((readsize = is.read(buffer)) > 0) {
				if (!mCanDownload.get()) {
					throw new Exception(String.valueOf(DOWNLOAD_CANCEL));
				}
				fos.write(buffer, 0, readsize);
				currentTotalSize += readsize;
				long currentProgress = currentTotalSize * 100 / updateFileTotalSize;
				log("readsize=" + readsize + ", currentTotalSize=" + currentTotalSize + ", downloadCount=" + downloadProgress + ", currentProgress=" + currentProgress);
				if (downloadProgress == 0 || (currentProgress - 1) >= downloadProgress) {
					downloadProgress = currentProgress;
					mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(DOWNLOAD_DOING, (int) (currentTotalSize * 100 / updateFileTotalSize), 0));
					APPSettingActivity.handler.sendMessage(APPSettingActivity.handler.obtainMessage(200,4,(int) (currentTotalSize * 100 / updateFileTotalSize)));
				}
			}
			fos.flush();
			log("downloadUpdateFile end---------------");
			if (currentTotalSize < updateFileTotalSize) {
				if (mMaxRetryTimes >= MAX_AUTO_RETRY_TIMES) {
					mMaxRetryTimes = 0;
					throw new Exception(String.valueOf(DOWNLOAD_FAIL));
				}
				mMaxRetryTimes++;
				throw new Exception(String.valueOf(DOWNLOAD_INCOMPLETE));
			}
			return currentTotalSize;
		} catch (Exception e) {
			e.printStackTrace();
			return currentTotalSize;
		}finally {
			if (fos != null) {
				fos.close();
			}
			if (is != null) {
				is.close();
			}
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
			if (client_test != null) {
				client_test.getConnectionManager().shutdown();
			}
		}
	}

	private void setHttpClientParams(DefaultHttpClient client) {
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
		client.setParams(params);
	}

	private void showNotification(int state, int progress, Intent intent) {
		Notification localNotification = new Notification(R.drawable.icon, null, 0);
		if (state == DOWNLOAD_START) {
			localNotification.tickerText = "准备下载";
			localNotification.when = System.currentTimeMillis();
			Intent stopIntent = new Intent(this, UpdateService.class);
			stopIntent.setAction(ACTION_STOP);
			localNotification.flags = (Notification.FLAG_ONGOING_EVENT | localNotification.flags);
			localNotification.contentView = mRemoteViews;
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_pbDownload, View.VISIBLE);
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_tvContent, View.VISIBLE);
			localNotification.contentView.setProgressBar(R.id.frame_download_notification_pbDownload, 100, progress, true);
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvTitle, "正在准备下载(点击取消)");
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvContent, "0%");
			localNotification.contentIntent = PendingIntent.getService(this, 0, stopIntent, 0);
		} else if (state == DOWNLOAD_DOING) {
			localNotification.tickerText = "开始下载";
			localNotification.when = System.currentTimeMillis();
			Intent stopIntent = new Intent(this, UpdateService.class);
			stopIntent.setAction(ACTION_STOP);
			localNotification.contentView = mRemoteViews;
			localNotification.flags = (Notification.FLAG_ONGOING_EVENT | localNotification.flags);
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_pbDownload, View.VISIBLE);
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_tvContent, View.VISIBLE);
			localNotification.contentView.setProgressBar(R.id.frame_download_notification_pbDownload, 100, progress, false);
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvTitle, "正在下载(点击取消)");
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvContent, progress + "%");
			localNotification.contentIntent = PendingIntent.getService(this, 0, stopIntent, 0);
		} else if (state == DOWNLOAD_COMPLETE) {
			localNotification.tickerText = "下载完成";
			localNotification.when = System.currentTimeMillis();
			Intent installIntent = null;
			if (intent != null) {
				installIntent = intent;
			} else {
				installIntent = new Intent(this, UpdateService.class);
				installIntent.setAction(ACTION_STOP);
			}
			localNotification.contentView = mRemoteViews;
			localNotification.flags = (Notification.FLAG_AUTO_CANCEL | localNotification.flags);
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_pbDownload, View.INVISIBLE);
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvTitle, "下载完成");
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvContent, mAppName + "下载完成，点击安装");
//			localNotification.defaults = Notification.DEFAULT_SOUND;// 铃声提醒
			localNotification.contentIntent = PendingIntent.getActivity(this, 0, installIntent, 0);
		} else if (state == DOWNLOAD_FAIL) {
			localNotification.tickerText = "下载失败";
			localNotification.when = System.currentTimeMillis();
			Intent failIntent = null;
			if (intent != null) {
				failIntent = intent;
			} else {
				failIntent = new Intent(this, UpdateService.class);
				failIntent.setAction(ACTION_STOP);
			}
			localNotification.contentView = mRemoteViews;
			localNotification.flags = (Notification.FLAG_AUTO_CANCEL | localNotification.flags);
			localNotification.contentView.setViewVisibility(R.id.frame_download_notification_pbDownload, View.INVISIBLE);
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvTitle, "下载失败");
			localNotification.contentView.setTextViewText(R.id.frame_download_notification_tvContent, mAppName + "下载失败，可点击重试");
			localNotification.defaults = Notification.DEFAULT_SOUND; // 铃声提醒
			localNotification.contentIntent = PendingIntent.getService(this, 0, failIntent, 0);
		}
		mNotificationManager.notify(NOTIFICATION_ID, localNotification);
	}

	/**
	 * 记录日志
	 * @param msg
	 */
	private static void log(String msg) {
		if (DEBUG) {
			LogUtils.logD(TAG, msg);
		}
	}

	/**
	 * 记录错误
	 * @param tr
	 */
	private static void log(Throwable tr) {
		LogUtils.logE(TAG, tr);
	}
	
	// 得到APK文件的大小
	private long computeApkFileSize(String downloadUrl) {
		DefaultHttpClient client = null;
		try {
			client = new DefaultHttpClient();
			HttpGet request = new HttpGet(downloadUrl);
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 1000 * 20);
			HttpConnectionParams.setSoTimeout(params, 1000 * 60);
			client.setParams(params);
			HttpResponse response = client.execute(request);
			// 获取需要下载文件的大小
			long fileSize = response.getEntity().getContentLength();
			return fileSize;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
