package com.fg114.main.util;

import java.io.*;
import java.lang.Thread.*;
import java.lang.reflect.*;
import java.util.*;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.location.Loc;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.task.ErrorLogTask;

import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.os.*;
import android.text.format.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

/**
 * 处理程序崩溃，可上报崩溃信息
 * 
 * @author wufucheng
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	private static final String TAG = CrashHandler.class.getName();

	private static final String CRASH_REPORTER_EXTENSION = ".cr"; // 错误报告文件的扩展名
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";

	private static CrashHandler mCrashHandler; // CrashHandler实例
	private Thread.UncaughtExceptionHandler mDefaultHandler; // 系统默认的UncaughtException处理类
	private Context mContext; // 程序的Context对象
	private Properties mDeviceCrashInfo = new Properties(); // 使用Properties来保存设备的信息和错误堆栈信息

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (mCrashHandler == null) {
			mCrashHandler = new CrashHandler();
		}
		return mCrashHandler;
	}

	/**
	 * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			handleException(ex);
			if (mDefaultHandler != null) {
				mDefaultHandler.uncaughtException(thread, ex);
			}
		} catch (Exception e) {

		}

		// if (!handleException(ex) && mDefaultHandler != null) {
		// // 如果用户没有处理则让系统默认的异常处理器来处理
		// mDefaultHandler.uncaughtException(thread, ex);
		// } else {
		// // Sleep后结束程序
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// LogUtils.logE(TAG, "Error : ", e);
		// }
		// android.os.Process.killProcess(android.os.Process.myPid());
		// System.exit(10);
		// }
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		try {

			// boolean isFromCrash = SharedprefUtil.getBoolean(mContext,
			// Settings.KEY_IS_FROM_CRASH, false);
			// if (isFromCrash) {
			// // 如果上次奔溃过，重置标志位
			// SharedprefUtil.saveBoolean(mContext, Settings.KEY_IS_FROM_CRASH,
			// false);
			// return false;
			// }
			// SharedprefUtil.saveBoolean(mContext, Settings.KEY_IS_FROM_CRASH,
			// true);

			if (ex == null) {
				LogUtils.logW(TAG, "handleException --- ex==null");
				// return true;
			} else {
				final String msg = ex.getLocalizedMessage();
				if (msg == null) {
					// return true;
				}
			}
			// 使用Toast来显示异常信息
			// new Thread() {
			// @Override
			// public void run() {
			// Looper.prepare();
			// // Toast toast = Toast.makeText(mContext,
			// mContext.getString(R.string.info_crash) + "\r\n" + msg,
			// Toast.LENGTH_LONG);
			// Toast toast = Toast.makeText(mContext,
			// mContext.getString(R.string.info_crash), Toast.LENGTH_LONG);
			// toast.setGravity(Gravity.CENTER, 0, 0);
			// toast.show();
			// Looper.loop();
			// }
			// }.start();
			if (ex == null) {
				ex = new Exception("Exception is null");
			}
			// else {
			// if (ex.getLocalizedMessage() == null) {
			// ex = new Exception(ex.toString());
			// }
			// }
			// 收集设备信息
			collectCrashDeviceInfo(mContext);
			// 保存错误报告文件
			saveCrashInfoToFile(ex);
			// 发送错误报告到服务器
			// sendCrashReportsToServer(mContext);
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
	 */
	public void sendPreviousReportsToServer() {
		// Log.e("CrashHandler", "sendPreviousReportsToServer");
		if (ActivityUtil.isNetWorkAvailable(mContext)) {
			sendCrashReportsToServer(mContext);
		}
	}

	/**
	 * 把错误报告发送给服务器,包含新产生的和以前没发送的.
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer(Context ctx) {
		try {
			String[] crFiles = getCrashReportFiles(ctx);
			if (crFiles != null && crFiles.length > 0) {
				TreeSet<String> sortedFiles = new TreeSet<String>();
				sortedFiles.addAll(Arrays.asList(crFiles));
				for (String fileName : sortedFiles) {
					if (ActivityUtil.isNetWorkAvailable(mContext)) {
						File cr = new File(ctx.getFilesDir(), fileName);
						IOUtils.writeTestInfo(ctx, "log_Report_Error.txt", "开始上传崩溃日志===="+Loc.getFormatDateStr(System.currentTimeMillis()));
						postReport(cr);
						cr.delete();// 删除已发送的报告
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void postReport(final File file) {
		try {
			if (ActivityUtil.isNetWorkAvailable(mContext)) {
				// Log.e("CrashHandler", "postReport");
				final byte[] data = IOUtils.readBytes(file);
				new Thread() {
					@Override
					public void run() {
						try {
							A57HttpApiV3.getInstance().errorLog(ActivityUtil.getVersionName(mContext), Settings.DEV_ID, Settings.DEV_ID, file.getName(),
							// Base64.encodeToString(data, Base64.DEFAULT));
									new String(data, "UTF-8"));
						} catch (Exception e) {
							// e.printStackTrace();
							// if (e != null) {
							// // 保存错误报告文件
							// saveCrashInfoToFile(e);
							// }
						} catch (OutOfMemoryError e) {
							IOUtils.writeTestInfo(mContext, "log_Report_Error.txt", "上传崩溃日志失败");
							ActivityUtil.saveOutOfMemoryError(e);
						}
					}
				}.start();
			}
		} catch (Exception e) {
			// e.printStackTrace();
			// if (e != null) {
			// // 保存错误报告文件
			// saveCrashInfoToFile(e);
			// }
		}
	}

	/**
	 * 获取错误报告文件名
	 * 
	 * @param ctx
	 * @return
	 */
	private String[] getCrashReportFiles(Context ctx) {
		File filesDir = ctx.getFilesDir();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

	/**
	 * 收集程序崩溃的设备信息
	 * 
	 * @param ctx
	 */
	private void collectCrashDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);

			if (pi != null) {
				mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
				mDeviceCrashInfo.put("CurrentPage", Settings.CURRENT_PAGE);
				mDeviceCrashInfo.put("SDK", "" + Build.VERSION.SDK);
//				mDeviceCrashInfo.put("CHANNEL_NUM", "" +com.fg114.main.app.Settings.SELL_CHANNEL_NUM);
				
			}

			// 收集ip和dns信息
			if (ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
				mDeviceCrashInfo.put("IP", ActivityUtil.getLocalIpAddress());
				mDeviceCrashInfo.put("DNS", ActivityUtil.getDnsInfo());
			}
			//
			mDeviceCrashInfo.put("Settings.BuildVersion", Settings.BuildVersion);

			// 收集网络信息
			mDeviceCrashInfo.put("NetworkInfo", ActivityUtil.getNetworkInfo());

		} catch (NameNotFoundException e) {
			LogUtils.logE(TAG, "Error while collect package info", e);
		}
		// 使用反射来收集设备信息，在Build类中包含各种设备信息,
		// 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
		try {
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
					LogUtils.logD(TAG, field.getName() + " : " + field.get(null));
				} catch (Exception e) {
					LogUtils.logE(TAG, "Error while collect crash info", e);
				}
			}
		} catch (Exception e) {
			LogUtils.logE(TAG, "field = null", e);
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return
	 */
	private String saveCrashInfoToFile(Throwable ex) {
		if (ex == null || mDeviceCrashInfo == null) {
			return null;
		}
		// StringBuffer sb = new StringBuffer();
		String result = "";
		try {
			Writer info = new StringWriter();
			PrintWriter printWriter = new PrintWriter(info);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			result = info.toString();
			printWriter.close();
		} catch (Exception e) {
			if (e != null) {
				result = e.toString() + "----->org_result=" + result;
			}
		}
		if (ex.getLocalizedMessage() == null) {
			mDeviceCrashInfo.put("EXEPTION", ex.toString());
		} else {
			mDeviceCrashInfo.put("EXEPTION", ex.getLocalizedMessage());
		}
		mDeviceCrashInfo.put(STACK_TRACE, result);
		// sb.append(ex.getLocalizedMessage()).append("\r\n");
		// sb.append(result);
		try {
			// Time t = new Time("GMT+8");
			// t.setToNow(); // 取得系统时间
			// int date = t.year * 10000 + (t.month + 1) * 100 + t.monthDay;
			// int time = t.hour * 10000 + t.minute * 100 + t.second;
			long millis = System.currentTimeMillis();
			String date = ConvertUtil.convertLongToDateString(millis, "yyyyMMdd");
			String time = ConvertUtil.convertLongToDateString(millis, "HHmmss");

			// 修正版
			String fileName = "crash-" + date + "-" + time + CRASH_REPORTER_EXTENSION;

			FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);

			// byte[] data = sb.toString().getBytes("UTF-8");
			// trace.write(data);

			mDeviceCrashInfo.store(trace, "");

			trace.flush();
			trace.close();
			return fileName;
		} catch (Exception e) {
			LogUtils.logE(TAG, "an error occured while writing report file...", e);
		}
		return null;
	}

	public void saveException(Throwable ex) {
		saveException(ex, "");
	}

	public void saveException(Throwable ex, String msg) {
		if (ex == null) {
			return;
		}
		mDeviceCrashInfo.clear();
		if (!CheckUtil.isEmpty(msg)) {
			mDeviceCrashInfo.put("ExMSG", msg);
		}
		// 收集设备信息
		collectCrashDeviceInfo(mContext);
		// 保存错误报告文件
		saveCrashInfoToFile(ex);
	}
}
