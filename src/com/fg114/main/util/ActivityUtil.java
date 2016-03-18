package com.fg114.main.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.SplashActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwaySearchRestListActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.service.dto.ResFoodData3;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.speech.asr.OnFinishListener;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.speech.asr.RecognitionResult;
import com.fg114.main.weibo.WeiboContentBuilder;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;

/**
 * 窗体操作
 * 
 * @author zhaozuoming
 * 
 */
public class ActivityUtil {

	private static final String TAG = "ActivityUtil";
	private static final boolean DEBUG = Settings.DEBUG;

	private static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	private static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
	private static final String MIME_TYPE_TEXT = "text/plain";
	private static final String MIME_TYPE_EMAIL = "message/rfc822";

	/** Network type is unknown */
	public static final int NETWORK_TYPE_UNKNOWN = 0;
	/** Current network is GPRS */
	public static final int NETWORK_TYPE_GPRS = 1;
	/** Current network is EDGE */
	public static final int NETWORK_TYPE_EDGE = 2;
	/** Current network is UMTS */
	public static final int NETWORK_TYPE_UMTS = 3;
	/** Current network is CDMA: Either IS95A or IS95B */
	public static final int NETWORK_TYPE_CDMA = 4;
	/** Current network is EVDO revision 0 */
	public static final int NETWORK_TYPE_EVDO_0 = 5;
	/** Current network is EVDO revision A */
	public static final int NETWORK_TYPE_EVDO_A = 6;
	/** Current network is 1xRTT */
	public static final int NETWORK_TYPE_1xRTT = 7;
	/** Current network is HSDPA */
	public static final int NETWORK_TYPE_HSDPA = 8;
	/** Current network is HSUPA */
	public static final int NETWORK_TYPE_HSUPA = 9;
	/** Current network is HSPA */
	public static final int NETWORK_TYPE_HSPA = 10;
	/** Current network is iDen */
	public static final int NETWORK_TYPE_IDEN = 11;
	/** Current network is EVDO revision B */
	public static final int NETWORK_TYPE_EVDO_B = 12;
	/** Current network is LTE */
	public static final int NETWORK_TYPE_LTE = 13;
	/** Current network is eHRPD */
	public static final int NETWORK_TYPE_EHRPD = 14;
	/** Current network is HSPA+ */
	public static final int NETWORK_TYPE_HSPAP = 15;

	/** No phone radio. */
	public static final int PHONE_TYPE_NONE = 0;
	/** Phone radio is GSM. */
	public static final int PHONE_TYPE_GSM = 1;
	/** Phone radio is CDMA. */
	public static final int PHONE_TYPE_CDMA = 2;
	/** Phone is via SIP. */
	public static final int PHONE_TYPE_SIP = 3;

	private static boolean mCancelThread;

	/**
	 * 窗体跳转
	 * 
	 * @param old
	 * @param cls
	 */
	public static void jump(Context old, Class<?> cls, int requestCode, Bundle mBundle) {
		jump(old, cls, requestCode, mBundle, false);
	}

	/**
	 * 窗体跳转
	 * 
	 * @param old
	 * @param cls
	 */
	public static void jump(Context old, Class<?> cls, int requestCode, Bundle mBundle, boolean clearTop) {
		Intent intent = new Intent();
		intent.setClass(old, cls);
		if (mBundle != null) {
			intent.putExtras(mBundle);
		}

		Activity activity = (Activity) old;
		if (clearTop) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		activity.startActivityForResult(intent, requestCode);
		ActivityUtil.overridePendingTransition(activity, R.anim.right_slide_in, R.anim.right_slide_out);

	}

	public static void jump(Context old, Class<?> cls, int requestCode, Bundle mBundle, boolean clearTop, int enterAnim, int exitAnim) {
		Intent intent = new Intent();
		intent.setClass(old, cls);
		if (mBundle != null) {
			intent.putExtras(mBundle);
		}

		Activity activity = (Activity) old;
		if (clearTop) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		activity.startActivityForResult(intent, requestCode);
		// activity.overridePendingTransition(R.anim.right_slide_in,
		// R.anim.right_slide_out);
		ActivityUtil.overridePendingTransition(activity, enterAnim, exitAnim);
	}

	public static void jump(Context old, Class<?> cls, int requestCode) {
		jump(old, cls, requestCode, null);
	}

	public static void jumpNotForResult(Context old, Class<?> cls, Bundle mBundle, boolean clearTop) {
		Intent intent = new Intent();
		intent.setClass(old, cls);
		if (mBundle != null) {
			intent.putExtras(mBundle);
		}

		Activity activity = (Activity) old;
		if (clearTop) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		activity.startActivity(intent);
		// activity.overridePendingTransition(R.anim.right_slide_in,
		// R.anim.right_slide_out);
		ActivityUtil.overridePendingTransition(activity, R.anim.right_slide_in, R.anim.right_slide_out);
	}

	/**
	 * 返回
	 * 
	 * @param old
	 * @param intent
	 */
	public static void back(Context old, Intent intent) {
		Activity activity = (Activity) old;
		activity.setResult(Activity.RESULT_OK, intent);
		activity.finish();
	}

	/**
	 * 添加控件(会删除之前layout所有控件)
	 * 
	 * @param layout
	 * @param view
	 */
	public static void addViewOnly(ViewGroup layout, View view) {
		try {
			if (layout.getChildCount() > 0) {
				layout.removeAllViews();
			}
			layout.addView(view);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public static void runInUIThread(Context context, final Toast toast) {
		final Activity activity = (Activity) context;
		activity.runOnUiThread(new Runnable() {
			public void run() {
				toast.show();
			}
		});
	}

	public static Display getWindowDisplay(Context context) {
		return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}

	/**
	 * 获得设备ID
	 * 
	 * @param context
	 * @return
	 */
	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String id = telephonyManager.getDeviceId();
		String uuid = SessionManager.getInstance().getUUID(context);
		if (CheckUtil.isEmpty(id)) {
			id = uuid;
		}
		uuid = uuid + "|" + id;
		// 同时更新Settings里的DEV_ID，
		// 修复在某些情况下同时出现“双重设备号”的问题，此时如果使用wap页会有问题。(xujianjun)
		Settings.DEV_ID = uuid;
		// -------------------------
		return uuid;
		// return id;
	}

	public static String getRealDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String id = telephonyManager.getDeviceId();
		if (CheckUtil.isEmpty(id)) {
			id = SessionManager.getInstance().getUUID(context);
		}
		return id;
	}

	/**
	 * 获得手机型号
	 */
	public static String getDeviceType() {
		return Build.MODEL;
	}

	/**
	 * 获得版本号
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getVersionCode(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			int code = info.versionCode; // 版本号
			// Log.d(TAG, "versionCode="+code+", pkg="+info.packageName);
			return code;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * 获得版本名称
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getVersionName(Context ctx) {
		PackageManager manager = ctx.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return "";
		}
	}

	public static float getPX(Context context, int dipValue) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
	}

	/**
	 * 检测是否连接了网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkAvailable(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (connectivity != null) {
				NetworkInfo[] infoArray = connectivity.getAllNetworkInfo();
				if (infoArray != null) {
					for (NetworkInfo info : infoArray) {
						if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 系统顶部状态栏消息提示
	 * 
	 * @param mContext
	 * @return
	 */
	public static NotificationManager getNotificationManager(Context mContext) {
		return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@SuppressWarnings("rawtypes")
	public static Notification buildNotification(Context mContext, String title, String info, Class jumpClass) {
		Notification notification = new Notification(R.drawable.icon, title, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // 表明在点击后，此通知自动清除，
		Intent intent = new Intent(mContext, jumpClass);
		intent.putExtra("info", info);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(mContext, title, info, contentIntent);
		return notification;
	}

	/**
	 * 拨打电话
	 */
	public static void callSuper57(Context context, String phoneNo) {
		// 已经绑定电话的场合
		String number = "tel:" + phoneNo;
		try {
			// Intent callIntent = new Intent(Intent.ACTION_CALL);
			Intent callIntent = new Intent(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(number));
			context.startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
			if (DEBUG)
				Log.e(TAG, "Call failed", e);
		}
	}

	/**
	 * 去系统设置界面
	 */
	public static void gotoSysSetting(Context context) {
		try {
			Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			context.startActivity(settingsIntent);
		} catch (ActivityNotFoundException e) {
			if (DEBUG)
				Log.e(TAG, "Settings failed", e);
		}
	}

	/**
	 * 去系统无线设置界面
	 */
	public static void gotoWirelessSettings(Context context) {
		try {
			Intent settingsIntent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			context.startActivity(settingsIntent);
		} catch (Exception e) {
			if (DEBUG)
				Log.e(TAG, "Settings failed", e);
		}
	}

	/**
	 * 获得手机Ip
	 * 
	 * @return
	 */
	public static String getLocalIpAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			for (; en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
				for (; enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			if (DEBUG)
				Log.e(TAG, "getLocalIpAddress failed", e);
		}
		return "";
	}

	/**
	 * 检查一下手机设备各项硬件的开启状态
	 */
	public static boolean checkMysoftStage(Context context) {
		/*
		 * 先看手机是否已插入sd卡
		 */
		if (existSDcard()) { // 判断手机SD卡是否存在
			if (!new File("/sdcard").canRead()) {
				// 不可读写的场合
				DialogUtil.showToast(context, context.getString(R.string.text_info_sdcard_cant_write));
				return false;
			}
		} else {
			// 没有sd卡的场合
			DialogUtil.showToast(context, context.getString(R.string.text_info_no_sdcard));
			return false;
		}
		return true;
	}

	public static boolean existSDcard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 通过uri获得文件名
	 * 
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Activity activity, Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * 获得手机分辨率
	 */
	public static DisplayMetrics getWindowsPixels(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 获得手机的宽带和高度像素单位为px
		return dm;
	}

	// /**
	// * 查看内存使用状况
	// * @param clazz
	// */
	// public static void logHeap(Class clazz) {
	// Double allocated = new Double(Debug.getNativeHeapAllocatedSize())/new
	// Double((1048576));
	// Double available = new Double(Debug.getNativeHeapSize())/1048576.0;
	// Double free = new Double(Debug.getNativeHeapFreeSize())/1048576.0;
	// DecimalFormat df = new DecimalFormat();
	// df.setMaximumFractionDigits(2);
	// df.setMinimumFractionDigits(2);
	//
	// Log.d("test", "debug. =================================");
	// Log.d("test", "debug.heap native: allocated " + df.format(allocated) +
	// "MB of " +
	// df.format(available) + "MB (" + df.format(free) + "MB free) in [" +
	// clazz.getName().replaceAll("com.myapp.android.","") + "]");
	// Log.d("test", "debug.memory: allocated: " + df.format(new
	// Double(Runtime.getRuntime().totalMemory()/1048576)) + "MB of " +
	// df.format(new
	// Double(Runtime.getRuntime().maxMemory()/1048576))+ "MB (" + df.format(new
	// Double(Runtime.getRuntime().freeMemory()/1048576)) +"MB free)");
	// System.gc();
	// System.gc();
	// }

	/**
	 * 判断是否是联通wcdma
	 */
	public static boolean isWcdma(Activity activity) {
		// 获得手机SIMType
		TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		int nType = tm.getNetworkType();
		int pType = tm.getPhoneType();
		String nOperator = tm.getNetworkOperator();

		if (nOperator.equals("46001")) {
			// 联通的场合
			if (pType == TelephonyManager.PHONE_TYPE_GSM) {
				// gsm的场合
				if (nType == TelephonyManager.NETWORK_TYPE_HSDPA || nType == TelephonyManager.NETWORK_TYPE_HSUPA || nType == TelephonyManager.NETWORK_TYPE_HSPA) {
					// WCDMA的场合
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断WIFI是否打开
	 */
	public static boolean isWifiOpen(Activity activity) {
		// 获得手机SIMType
		WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		return wm.isWifiEnabled();
	}

	// /**
	// * 判断应用是否已有快捷方式
	// */
	// private static boolean isInstallShortcut(Context context) {
	// boolean flag = false;
	// ContentResolver cr = context.getContentResolver();
	// Uri uri;
	// if(android.os.Build.VERSION.SDK_INT < 8){
	// uri =
	// Uri.parse("content://com.android.launcher.settings/favorites?notify=true");
	// }else{
	// //2.2以上版本
	// uri =
	// Uri.parse("content://com.android.launcher2.settings/favorites?notify=true");
	// }
	// Cursor c = cr.query(uri, new String[] {"title", "iconResource"},
	// "title=?",
	// new String[] {context.getString(R.string.app_name)},
	// null);
	// cr.delete(uri, "title=?", new String[]
	// {context.getString(R.string.app_name)});
	// if (c != null) {
	// if (c.getCount() > 0) {
	// flag = true;
	// }
	// c.close();
	// }
	//
	// return flag;
	//
	// }
	//
	/**
	 * 添加首页快捷方式
	 */
	public static void setShortCut(Context context) {

		boolean isHasDesktopLink = SharedprefUtil.getBoolean(context, Settings.IS_HAS_DESKTOP_LINK, false);
		if (isHasDesktopLink) {
			Log.e(TAG, isHasDesktopLink + "");
			return;
		}

		String appName = context.getString(R.string.app_name);
		// 获得所有已安装应用信息
		boolean flag = false;
		int app_id = -1;
		PackageManager p = context.getPackageManager();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> res = p.queryIntentActivities(i, 0);
		// 获得本应用信息
		for (int k = 0; k < res.size(); k++) {
			if (res.get(k).activityInfo.loadLabel(p).toString().equals(appName)) {
				flag = true;
				app_id = k;
				break;
			}
		}

		if (flag) {
			ActivityInfo ai = res.get(app_id).activityInfo;
			// 快捷方式启动对象
			Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
			shortcutIntent.setClassName(ai.packageName, ai.name);
			shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Intent addShortcut = new Intent(ACTION_ADD_SHORTCUT);
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			// 快捷方式显示名
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
			// 快捷方式icon
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.icon));
			// 不允许重复创建
			addShortcut.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);
			context.sendBroadcast(addShortcut);
			SharedprefUtil.saveBoolean(context, Settings.IS_HAS_DESKTOP_LINK, true);
		}
	}

	/**
	 * 添加快捷方式
	 */
	public static void setShortCutNew(Context context) {

		boolean isHasDesktopLink = SharedprefUtil.getBoolean(context, Settings.IS_HAS_DESKTOP_LINK, false);
		if (isHasDesktopLink) {
			Log.e(TAG, isHasDesktopLink + "");
			return;
		}

		String appName = context.getString(R.string.app_name);
		// 获得所有已安装应用信息
		boolean flag = false;
		int app_id = -1;
		PackageManager p = context.getPackageManager();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> res = p.queryIntentActivities(i, 0);
		// 获得本应用信息
		for (int k = 0; k < res.size(); k++) {
			if (res.get(k).activityInfo.loadLabel(p).toString().equals(appName)) {
				flag = true;
				app_id = k;
				break;
			}
		}

		if (flag) {
			ActivityInfo ai = res.get(app_id).activityInfo;
			// 快捷方式启动对象
			// Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
			// shortcutIntent.setClassName(context.getApplicationContext(),
			// ai.name);
			// shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// Intent addShortcut = new Intent(ACTION_ADD_SHORTCUT);
			Intent addShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
			// addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
			// shortcutIntent);
			// 快捷方式显示名
			// addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
			// 快捷方式icon
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.drawable.icon));
			// 不允许重复创建
			addShortcut.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);
			addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(context.getApplicationContext(), SplashActivity.class));
			context.sendBroadcast(addShortcut);
			SharedprefUtil.saveBoolean(context, Settings.IS_HAS_DESKTOP_LINK, true);
		}
	}

	/**
	 * 退出应用
	 */
	public static void exitApp(Activity activity) {
		Fg114Application app = (Fg114Application) activity.getApplication();
		// app.onTerminate();
		// System.exit(0);

		activity.finish();
	}

	/**
	 * 兼容1.6的画面迁移动画方法
	 * 
	 * @param activity
	 * @param animId1
	 * @param animId2
	 */
	public static void overridePendingTransition(Activity activity, int animId1, int animId2) {
		try {
			Method m = activity.getClass().getSuperclass().getMethod("overridePendingTransition", int.class, int.class);
			m.invoke(activity, animId1, animId2);
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * 判断应用是否已安装
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static boolean isAppInstalled(Context context, String uri) {
		PackageManager manager = context.getPackageManager();
		try {
			manager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 调用分享程序
	 * 
	 * @param activity
	 * @param subject
	 * @param message
	 * @param chooserDialogTitle
	 */
	public static void callShare(Activity activity, String subject, String message, String chooserDialogTitle) throws Exception {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		shareIntent.putExtra(Intent.EXTRA_TEXT, message);
		shareIntent.setType(MIME_TYPE_TEXT);
		Intent intent = Intent.createChooser(shareIntent, chooserDialogTitle);
		activity.startActivity(intent);
	}

	/**
	 * 从SD卡读入
	 * 
	 * @param context
	 * @param strFileName
	 * @return
	 * @throws Exception
	 */
	public static String readFileFromSD(Context context, String strFileName) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return "";
		}

		String strPath = Environment.getExternalStorageDirectory() + "/" + Settings.IMAGE_CACHE_DIRECTORY + "/";
		File fPath = new File(strPath);
		File fFile = new File(strPath + strFileName);
		if (!fPath.exists() || !fFile.exists()) {
			return "";
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fFile);
			byte[] bData = new byte[fis.available()];
			fis.read(bData);
			return new String(bData, "UTF-8");
		} catch (Exception e) {
			return "";
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 写入SD卡
	 * 
	 * @param context
	 * @param strContent
	 * @param strFileName
	 * @return
	 * @throws Exception
	 */
	public static boolean writeFileToSD(Context context, String strContent, String strFileName) throws Exception {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}
		FileOutputStream fos = null;
		try {
			String strPath = Environment.getExternalStorageDirectory() + "/" + Settings.IMAGE_CACHE_DIRECTORY + "/";
			File fPath = new File(strPath);
			File fFile = new File(strPath + strFileName);
			if (!fPath.exists()) {
				fPath.mkdir();
			}
			if (!fFile.exists()) {
				fFile.createNewFile();
			}
			fos = new FileOutputStream(fFile);
			byte[] bytes = strContent.getBytes("UTF-8");
			fos.write(bytes);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * 判断是否模拟器
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isEmulator(Context context) {
		try {
			return Build.MODEL.toLowerCase().indexOf("sdk") > -1 || Build.PRODUCT.toLowerCase().indexOf("sdk") > -1 || Build.BRAND.toLowerCase().indexOf("generic") > -1
					|| "1".equals(SystemPropertiesProxy.get(context, "ro.kernel.qemu"));
		} catch (Exception e) {
			return false;
		}
	}

	public static String getDevString(Context context) {
		try {
			return "MODEL:" + Build.MODEL.toLowerCase() + "|PRODUCT:" + Build.PRODUCT.toLowerCase() + "|BRAND:" + Build.BRAND.toLowerCase() + "|qemu:"
					+ SystemPropertiesProxy.get(context, "ro.kernel.qemu");
		} catch (Exception e) {
			return "";
		}
	}

	// public static void jumpToDishList(Context context, int requestCode,
	// Bundle bundle) {
	// DishOrderDTO dishOrder =
	// SessionManager.getInstance().getDishOrder(context, resId);
	// if (dishOrder.getTimeStamp() + Settings.DISH_EXPIRED_TIME <
	// System.currentTimeMillis()) {
	// dishOrder.reset();
	// SessionManager.getInstance().setDishOrder(context, dishOrder);
	// }
	// // bundle.putInt(Settings.BUNDLE_DISH_SRC_PAGE,
	// Settings.CAPTURE_ACTIVITY);
	// ActivityUtil.jump(context, DishListActivity.class, requestCode, bundle);
	// }

	/**
	 * 从本地选取图片，应处理onActivityResult，示例： protected void onActivityResult(int
	 * requestCode, int resultCode, Intent data) { //获得图片的真实地址 String path =
	 * getPathByUri(this, data.getData()); }
	 * 
	 * @param activity
	 * @param requestCode
	 */
	public static void pickImage(Activity activity, int requestCode) throws Exception {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		// intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 调用拍照程序拍摄图片，返回图片对应的Uri，应处理onActivityResult
	 * ContentResolver的insert方法会默认创建一张空图片，如取消了拍摄，应根据方法返回的Uri删除图片
	 * 
	 * @param activity
	 * @param requestCode
	 * @param fileName
	 * @return
	 */
	// TODO
	public static Uri captureImage(Activity activity, int requestCode, String fileName, String desc) throws Exception {
		// 设置文件参数
		// TODO
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION, desc);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		// 获得uri

		Uri imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		activity.startActivityForResult(intent, requestCode);
		return imageUri;
	}

	/**
	 * 通过地址跳转到网页
	 * 
	 * @param activity
	 * @param url
	 */
	public static void jumbToWeb(Activity activity, String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			activity.startActivity(intent);
			ActivityUtil.overridePendingTransition(activity, R.anim.right_slide_in, R.anim.right_slide_out);
		} catch (Exception e) {
			e.printStackTrace();
			DialogUtil.showToast(activity, "抱歉，无法打开链接");
		}
	}

	/**
	 * 获得应用是否在前台
	 */
	public static boolean isOnForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			if (context.getPackageName().equals(tasksInfo.get(0).topActivity.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static void saveException(Throwable ex) {
		try {
			if (Fg114Application.crashHandler != null) {
				Fg114Application.crashHandler.saveException(ex);
			}
		} catch (Exception e) {

		}
	}

	public static void saveException(Throwable ex, String msg) {
		try {
			if (Fg114Application.crashHandler != null) {
				Fg114Application.crashHandler.saveException(ex, msg);
			}
		} catch (Exception e) {

		}
	}

	public static void saveOutOfMemoryError(OutOfMemoryError e) {
		saveException(e, "Collect OutOfMemoryError");
	}

	public static String getChannelId(Context context) {
		// 3.1.32 改为固定取包里的渠道号文件ch，不再取缓存，使得更新时渠道号为新包内的渠道号
		// 读取渠道号
		BufferedReader br = null;
		try {
			String ch = "1";
			// String ch = SharedprefUtil.get(context, Settings.KEY_CHANNEL_NUM,
			// "");
			// if (ch.equals("")) {
			br = new BufferedReader(new InputStreamReader(context.getAssets().open("ch")));
			ch = br.readLine();
			SharedprefUtil.save(context, Settings.KEY_CHANNEL_NUM, ch);
			Settings.SELL_CHANNEL_NUM = ch;
			// }
			return ch;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void showVoiceDialogForSearch(final Activity activity, final int voiceInputTag, final OnRecognizedFinishListener listener) {
		try {
			RecognitionEngine.getEngine(activity).start(new OnFinishListener() {
				@Override
				public void onFinish(ArrayList<RecognitionResult> results, int selectedIndex) {
					if (results == null || results.size() == 0 || selectedIndex >= results.size()) {
						return;
					}
					if (listener != null) {
						listener.onRecognizedFinish(results.get(selectedIndex).text);
					}
				}
			});
			// 发送点击跟踪
			new Thread(new Runnable() {
				public void run() {
					try {
						String cityId = SessionManager.getInstance().getCityInfo(activity).getId();
						// voiceInputTag---点击了哪个语音按钮 0：没有点击 1：首页左上 2：首页左下
						// 3：功能菜单中 4：搜索建议页 5:订餐厅页 6：热门商圈页 7：选择餐厅8:意见反馈
						A57HttpApiV3.getInstance().clickVoiceInput(cityId, voiceInputTag);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface OnRecognizedFinishListener {
		public void onRecognizedFinish(String text);
	}

	// type 分类：1表示餐厅， 2表示美食，3表示外卖
	public static void isLocExist(final Activity activity, final String type, final int fromPage, final Bundle data) {

		if (!ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
			DialogUtil.showToast(activity, activity.getString(R.string.text_dialog_net_unavailable));
			return;
		}

		mCancelThread = false;

		// 获得位置
		boolean haveGpsTag = Loc.isGpsAvailable();
		if (!haveGpsTag) {
			// 没有定位的场合，提示打开
			DialogUtil.showAlert(activity, true, activity.getString(R.string.text_dialog_goto_open_gps), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityUtil.gotoSysSetting(activity);
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 取消
					dialog.cancel();
				}
			});
			return;
		}

		// 获取GPS信息
		final ProgressDialog dialog = DialogUtil.getProgressDialog(activity, "正在定位，请稍等...", true, 100, null);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mCancelThread = true;
			}
		});

		final Handler handle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				DialogUtil.dismissProgressDialog(dialog);
				if (msg.what != 1) {
					DialogUtil.showToast(activity, "无法找到您的位置，请稍后再试");
					return;
				}
				jumpToNearbyActivity(activity, type, fromPage, data);
			}
		};

		if (!Loc.IsLocExist()) {
			dialog.show();
			AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

				@Override
				protected Integer doInBackground(Void... params) {
					int count = 0;
					while (true) {
						if (mCancelThread) {
							break;
						}
						SystemClock.sleep(1000);
						boolean isExist = Loc.IsLocExist();
						if (isExist) {
							break;
						}
						count++;
						if (count > 9) {
							Loc.getLoc();
							break;
						}
					}
					if (!mCancelThread) {
						LocInfo myLoc = Loc.getLoc();
						if (myLoc != null && myLoc.getLoc() != null) {
							handle.sendEmptyMessage(1);
						} else {
							handle.sendEmptyMessage(0);
						}
					}
					return null;
				}
			};
			task.execute();
		} else {
			handle.sendEmptyMessage(1);
		}
	}

	/**
	 * 去附近餐厅及美食页面处理 检查选择城市与所在城市是否一致
	 */
	private static void jumpToNearbyActivity(final Activity activity, final String type, final int fromPage, final Bundle data) {
		// 检查当前城市是否是所在城市
		final CityInfo gpsCityInfo = SessionManager.getInstance().getGpsCity(activity);
		final CityInfo cityInfo = SessionManager.getInstance().getCityInfo(activity);
		if (gpsCityInfo == null || CheckUtil.isEmpty(gpsCityInfo.getId())) {
			DialogUtil.showToast(activity, "无法找到您的位置，请稍后再试");
			return;
		}
		if (!gpsCityInfo.getId().equals(cityInfo.getId())) {
			// 获得提示信息
			String alertMsg = DialogUtil.fullMsg(activity.getString(R.string.text_dialog_gps_localed), gpsCityInfo.getName());
			if (data.containsKey(Settings.BUNDLE_FUNC_NAME) && !TextUtils.isEmpty(data.getString(Settings.BUNDLE_FUNC_NAME))) {
				String orignMsg = "GPS显示您在{0}，无法使用{1}的{2}功能哦，切换城市试试吧~";
				alertMsg = DialogUtil.fullMsg(orignMsg, gpsCityInfo.getName(), cityInfo.getName(), data.getString(Settings.BUNDLE_FUNC_NAME));
			}

			// 当定位成功的城市不是所选城市的场合城市
			DialogUtil.showAlert(activity, true, null, alertMsg, "切换", "取消", new DialogInterface.OnClickListener() {// 消息提示框确定按钮

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 将定位的城市设为当前城市
							cityInfo.setId(gpsCityInfo.getId());
							cityInfo.setName(gpsCityInfo.getName());
							cityInfo.setPhone(gpsCityInfo.getPhone());
							SessionManager.getInstance().setCityInfo(activity, cityInfo);
							CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
							if (type != Settings.STATUTE_CHANNEL_TAKEAWAY) {

								// 设置查询条件
								SessionManager.getInstance().getFilter().setDistanceMeter(1000);
								SessionManager.getInstance().getFilter().setChannelId(type);
								// 设置传入参数
								data.putInt(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE, Settings.RES_AND_FOOD_LIST_TYPE_NEARBY);
								ActivityUtil.jump(activity, ResAndFoodListActivity.class, fromPage, data);
							} else {
								// 跳转到外卖界面
								// TODO lijian
								// ActivityUtil.jump(activity,
								// TakeAwayRestaurantListActivity.class,
								// fromPage, data);
								ActivityUtil.jump(activity, TakeAwayNewFoodListActivity.class, fromPage, data);
							}

						}
					}, new DialogInterface.OnClickListener() {// 消息提示框取消按钮

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
		} else {
			if (type != Settings.STATUTE_CHANNEL_TAKEAWAY) {
				// 设置查询条件
				SessionManager.getInstance().getFilter().setDistanceMeter(1000);
				SessionManager.getInstance().getFilter().setChannelId(type);
				// 设置传入参数
				data.putInt(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE, Settings.RES_AND_FOOD_LIST_TYPE_NEARBY);
				ActivityUtil.jump(activity, ResAndFoodListActivity.class, fromPage, data);
			} 

		}
	}

	public static String getDnsInfo() {
		String result = "unknown";
		try {
			Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
			Method method = SystemProperties.getMethod("get", new Class[] { String.class });
			ArrayList<String> servers = new ArrayList<String>();
			for (String name : new String[] { "net.dns1", "net.dns2", "net.dns3", "net.dns4", }) {
				String value = (String) method.invoke(null, name);
				if (value != null && !"".equals(value) && !servers.contains(value))
					servers.add(value);
			}
			StringBuffer sb = new StringBuffer();
			for (String s : servers) {
				sb.append(s).append(";");
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getNetworkInfo() {
		String result = "unknown";
		try {
			TelephonyManager telephonyManager = (TelephonyManager) ContextUtil.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			StringBuffer sb = new StringBuffer();
			sb.append("NetworkCountryIso=").append(telephonyManager.getNetworkCountryIso()).append(";");
			sb.append("NetworkOperator=").append(telephonyManager.getNetworkOperator()).append(";");
			sb.append("NetworkOperatorName=").append(telephonyManager.getNetworkOperatorName()).append(";");
			sb.append("NetworkType=").append(getNetworkTypeName(telephonyManager.getNetworkType())).append(";");
			sb.append("PhoneType=").append(getPhoneTypeName(telephonyManager.getPhoneType())).append(";");
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getNetworkTypeName(int type) {
		switch (type) {
		case NETWORK_TYPE_GPRS:
			return "GPRS";
		case NETWORK_TYPE_EDGE:
			return "EDGE";
		case NETWORK_TYPE_UMTS:
			return "UMTS";
		case NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case NETWORK_TYPE_HSPA:
			return "HSPA";
		case NETWORK_TYPE_CDMA:
			return "CDMA";
		case NETWORK_TYPE_EVDO_0:
			return "CDMA - EvDo rev. 0";
		case NETWORK_TYPE_EVDO_A:
			return "CDMA - EvDo rev. A";
		case NETWORK_TYPE_EVDO_B:
			return "CDMA - EvDo rev. B";
		case NETWORK_TYPE_1xRTT:
			return "CDMA - 1xRTT";
		case NETWORK_TYPE_LTE:
			return "LTE";
		case NETWORK_TYPE_EHRPD:
			return "CDMA - eHRPD";
		case NETWORK_TYPE_IDEN:
			return "iDEN";
		case NETWORK_TYPE_HSPAP:
			return "HSPA+";
		default:
			return "UNKNOWN";
		}
	}

	private static String getPhoneTypeName(int type) {
		switch (type) {
		case PHONE_TYPE_NONE:
			return "NONE";
		case PHONE_TYPE_GSM:
			return "GSM";
		case PHONE_TYPE_CDMA:
			return "CDMA";
		case PHONE_TYPE_SIP:
			return "SIP";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * 调用发送电子邮件程序
	 * 
	 * @param activity
	 * @param address
	 * @param subject
	 * @param body
	 */
	public static void callEmail(Activity activity, String address, String subject, String body) throws Exception {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.setType(MIME_TYPE_EMAIL);
		activity.startActivity(intent);
	}

	/**
	 * 发送短信
	 * 
	 * @param context
	 * @param phone
	 *            电话号码
	 * @param content
	 *            短信内容
	 */
	public static void sendSMS(Context context, String phone, String content) throws Exception {
		phone = "smsto:" + phone;
		Uri uri = Uri.parse(phone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", content);
		context.startActivity(intent);
	}

//	public static void checkNearbyForRealTimeBook(final Activity activity, final Runnable callback, final Runnable notSupportRealTimeBookCallBack) {
//
//		if (!ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
//			DialogUtil.showToast(activity, activity.getString(R.string.text_dialog_net_unavailable));
//			return;
//		}
//
//		mCancelThread = false;
//
//		// 获得位置
//		boolean haveGpsTag = Loc.isGpsAvailable();
//		if (!haveGpsTag) {
//			// 没有定位的场合，提示打开
//			DialogUtil.showAlert(activity, true, activity.getString(R.string.text_dialog_goto_open_gps), new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					ActivityUtil.gotoSysSetting(activity);
//				}
//			}, new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// 取消
//					dialog.cancel();
//				}
//			});
//			return;
//		}
//
//		// 获取GPS信息
//		final ProgressDialog dialog = DialogUtil.getProgressDialog(activity, "正在定位，请稍等...", true, 100, null);
//		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				mCancelThread = true;
//			}
//		});
//
//		final Handler handle = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				super.handleMessage(msg);
//				DialogUtil.dismissProgressDialog(dialog);
//				if (msg.what != 1) {
//					DialogUtil.showToast(activity, "无法找到您的位置，请稍后再试");
//					return;
//				}
//				// 检查当前城市是否是所在城市
//				final CityInfo gpsCityInfo = SessionManager.getInstance().getGpsCity(activity);
//				final CityInfo cityInfo = SessionManager.getInstance().getCityInfo(activity);
//				if (gpsCityInfo == null || CheckUtil.isEmpty(gpsCityInfo.getId())) {
//					DialogUtil.showToast(activity, "无法找到您的位置，请稍后再试");
//					return;
//				}
//				if (!gpsCityInfo.getId().equals(cityInfo.getId())) {
//					// 获得提示信息
//					String orignMsg = "GPS显示您在{0}，无法使用{1}的{2}功能哦，切换城市试试吧~";
//					String alertMsg = DialogUtil.fullMsg(orignMsg, gpsCityInfo.getName(), cityInfo.getName(), "搜索附近实时餐位");
//					// 当定位成功的城市不是所选城市的场合城市
//					DialogUtil.showAlert(activity, true, alertMsg, new DialogInterface.OnClickListener() {// 消息提示框确定按钮
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									// 将定位的城市设为当前城市
//									cityInfo.setId(gpsCityInfo.getId());
//									cityInfo.setName(gpsCityInfo.getName());
//									cityInfo.setPhone(gpsCityInfo.getPhone());
//									SessionManager.getInstance().setCityInfo(activity, cityInfo);
//									if (SessionManager.getInstance().hasRealTimeBook()) {
//										if (callback != null) {
//
//										}
//									} else {
//										DialogUtil.showToast(activity, "抱歉，您所在的城市暂时没有开通实时餐位查询");
//										if (notSupportRealTimeBookCallBack != null) {
//											activity.runOnUiThread(notSupportRealTimeBookCallBack);
//										}
//									}
//								}
//							}, new DialogInterface.OnClickListener() {// 消息提示框取消按钮
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									dialog.cancel();
//								}
//							});
//				} else {
//					if (callback != null) {
//						activity.runOnUiThread(callback);
//					}
//				}
//			}
//		};
//
//		if (!Loc.IsLocExist()) {
//			dialog.show();
//			AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
//
//				@Override
//				protected Integer doInBackground(Void... params) {
//					int count = 0;
//					while (true) {
//						if (mCancelThread) {
//							break;
//						}
//						SystemClock.sleep(1000);
//						boolean isExist = Loc.IsLocExist();
//						if (isExist) {
//							break;
//						}
//						count++;
//						if (count > 9) {
//							Loc.getLoc();
//							break;
//						}
//					}
//					if (!mCancelThread) {
//						LocInfo myLoc = Loc.getLoc();
//						if (myLoc != null && myLoc.getLoc() != null) {
//							handle.sendEmptyMessage(1);
//						} else {
//							handle.sendEmptyMessage(0);
//						}
//					}
//					return null;
//				}
//			};
//			task.execute();
//		} else {
//			handle.sendEmptyMessage(1);
//		}
//	}

	public static String getCurrentNetWork() {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) ContextUtil.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] infoArray = connectivity.getAllNetworkInfo();
				if (infoArray != null) {
					for (NetworkInfo info : infoArray) {
						if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
							StringBuffer sbInfo = new StringBuffer();
							if (!TextUtils.isEmpty(info.getTypeName())) {
								sbInfo.append(info.getTypeName());
							}
							if (!TextUtils.isEmpty(info.getSubtypeName())) {
								sbInfo.append(" ").append(info.getSubtypeName());
							}
							if (!TextUtils.isEmpty(info.getExtraInfo())) {
								sbInfo.append(" ").append(info.getExtraInfo());
							}
							if (TextUtils.isEmpty(sbInfo.toString())) {
								sbInfo.append("unknown");
							}
							return sbInfo.toString();
						}
					}
				}
			}
			return "unknown";
		} catch (Exception e) {
			return "unknown";
		}
	}

	/**
	 * 使设备震动
	 */
	public static void vibrate(long milliseconds) {
		try {
			Vibrator vibrator = (Vibrator) ContextUtil.getContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(milliseconds);
		} catch (Exception e) {
			LogUtils.logE(TAG, e);
		}
	}

	/**
	 * 获得Wifi的MAC地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getWifiMAC(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager == null) {
				return "";
			}
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo == null || wifiInfo.getMacAddress() == null) {
				return null;
			}
			return wifiInfo.getMacAddress().replaceAll(":", "");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 判断是否安装了某个软件包
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isSoftwareAvailable(String packageName) {
		return isSoftwareAvailable(packageName, Integer.MIN_VALUE);
	}

	/**
	 * 判断是否安装了某个软件包，并且版本号至少大于minVersionCode
	 * 
	 * @param packageName
	 * @param minVersionCode
	 * @return
	 */
	public static boolean isSoftwareAvailable(String packageName, int minVersionCode) {
		final PackageManager packageManager = ContextUtil.getContext().getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				if (packageName.equals(pinfo.get(i).packageName)) {
					if (pinfo.get(i).versionCode >= minVersionCode) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获取某个软件包的VersionCode
	 * 
	 * @param packageName
	 * @return
	 */
	public static int getSoftwareVersionCode(String packageName) {
		final PackageManager packageManager = ContextUtil.getContext().getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		int versionCode = -1;
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				if (packageName.equals(pinfo.get(i).packageName)) {
					versionCode = pinfo.get(i).versionCode;
					break;
				}
			}
		}
		return versionCode;
	}

	public static void clearCookies(Context context) {
		@SuppressWarnings("unused")
		CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}

	public static String getParamUrl(String baseUrl, NameValuePair... nameValuePairs) {

		String query = URLEncodedUtils.format(AbstractHttpApi.stripNulls(nameValuePairs), HTTP.UTF_8);
		if (baseUrl.contains("?") == true)
			return baseUrl + "&" + query;
		else
			return baseUrl + "?" + query;
	}

	public static String getNoParamUrl(String baseUrl, NameValuePair... nameValuePairs) {
			return baseUrl  ;
	}
	
	public static void jumpToWeb(String baseUrl, String title, NameValuePair... nameValuePairs) {
		jumpToWeb(baseUrl, title, true, nameValuePairs);
	}

	public static void jumpToWeb(String baseUrl, String title, boolean hideTitle, NameValuePair... nameValuePairs) {
		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_KEY_WEB_URL, getParamUrl(baseUrl, nameValuePairs));
		bundle.putString(Settings.BUNDLE_KEY_WEB_TITLE, title);
		bundle.putBoolean(Settings.BUNDLE_KEY_WEB_HIDE_TITLE, hideTitle);
		ActivityUtil.jump(MainFrameActivity.getCurrentTopActivity(), SimpleWebViewActivity.class, 0, bundle);
	}

	public static void jumpToWebNoParam(String baseUrl, String title, boolean hideTitle, NameValuePair... nameValuePairs) {
		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_KEY_WEB_URL, getNoParamUrl(baseUrl, nameValuePairs));
		bundle.putString(Settings.BUNDLE_KEY_WEB_TITLE, title);
		bundle.putBoolean(Settings.BUNDLE_KEY_WEB_HIDE_TITLE, hideTitle);
		ActivityUtil.jump(MainFrameActivity.getCurrentTopActivity(), SimpleWebViewActivity.class, 0, bundle);
	}

	/**
	 * 根据当前的url，返回是否是测试版
	 * 
	 * @return
	 */
	public static boolean isDebug() {
		// 测试版
		if (A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("http://t") || A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("https://t")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isTestDevice = false;

	public static boolean isTestDev(Context context) {
		try {
			boolean isTest = false;
			if (A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("http://t")) {
				isTestDevice = true;
			}
			if(isTestDevice){
				return isTestDevice;
			}
			String devId = ActivityUtil.getRealDeviceId(context);
			for (String s : Settings.TEST_ID) {
				if (s.equalsIgnoreCase(devId)) {
					isTest = true;
					break;
				}
			}
			ApplicationInfo appInfo = ContextUtil.getContext().getPackageManager().getApplicationInfo(ContextUtil.getContext().getPackageName(), PackageManager.GET_SIGNATURES);
			boolean isDebug = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
			return isTestDevice || isTest || isDebug;
		} catch (Exception e) {
			return false;
		}
	}

	public static void setIsTestDev(boolean isTestDevice) {
		ActivityUtil.isTestDevice = isTestDevice;
	}

	private static final int IMAGE_SIZE = 500; // 图片边长限制
	private static final int IMAGE_QUALITY = 85; // 图片压缩率

	// 获得处理过的图片的地址
	public static String getGPSPicturePath(String path) {
		String targetPicPath = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY + File.separator + "pictemp";
		return getGPSPicturePath(path, targetPicPath);
	}

	// 获得处理过的图片的地址
	public static String getGPSPicturePath(String path, String targetPicPath) {
		Context context = ContextUtil.getContext();

		// ContentResolver contentResolver = this.getContentResolver();
		InputStream picStream = null; // 图片流
		if (path == null || path.equals("")) {
			// DialogUtil.showToast(context, "图片路径为空!");
			return null;
		}
		Bitmap bmp = null;
		try {
			picStream = new FileInputStream(path);
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			if (picStream.available() == 0) {
				// DialogUtil.showToast(context, "图片数据无效");
				return null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取图片的宽和高
			BitmapFactory.decodeStream(picStream, null, options); // 此时返回bm为空
			options.inJustDecodeBounds = false;

			if (options.outWidth < 0 || options.outHeight < 0) {
				// 选中的是非图像文件
				// DialogUtil.showToast(context, "图片格式无效");
				return null;
			}

			// 计算缩放比
			int scale = 1;
			if (options.outWidth > IMAGE_SIZE || options.outHeight > IMAGE_SIZE) {
				if (options.outWidth >= options.outHeight) {
					scale = (int) Math.ceil(options.outWidth / (float) IMAGE_SIZE);
				} else {
					scale = (int) Math.ceil(options.outHeight / (float) IMAGE_SIZE);
				}
			}
			options.inSampleSize = scale;
			picStream.close();
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			picStream = new FileInputStream(path);
			bmp = BitmapFactory.decodeStream(picStream, null, options);
			bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, new FileOutputStream(targetPicPath, false));
			setJpegLongLat(targetPicPath, getJpegLongLat(path));
			return targetPicPath;
		} catch (Exception e) {
			e.printStackTrace();
			// DialogUtil.showToast(context, "无法显示选择的文件");
			// Log.e("载入图片出错", "" + e.getMessage(), e);
			return null;
		} finally {
			if (bmp != null) {
				bmp.recycle();
			}
			if (picStream != null) {
				try {
					picStream.close();
				} catch (Exception e) {
					e.printStackTrace();
					// DialogUtil.showToast(context, e.getMessage());
				}

			}
		}
	}

	// 获得图片的经纬度
	private static String[] getJpegLongLat(String filePath) {
		// 保存经度纬度的信息
		String[] logLat = new String[] { "", "" ,""};
		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			String lo = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);// 经度
			logLat[0] = lo;
			String la = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);// 纬度
			logLat[1] = la;
			String time = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);// 时间
			logLat[2] = time;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return logLat;
	}

	// 给图片加经纬度
	private static void setJpegLongLat(String filePath, String[] logLat) {
		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			if (logLat[0] != null)
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, logLat[0]);
			if (logLat[1] != null)
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, logLat[1]);
			if (logLat[2] != null) 
			     exifInterface.setAttribute(ExifInterface.TAG_DATETIME,logLat[2]);
			if (exifInterface != null) {
				exifInterface.saveAttributes();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取图片状态栏高度
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = activity.getResources().getDimensionPixelSize(i5);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}
	
	
	  //判断当前网络是否为wifi
    public static boolean isWifi(Context mContext) {  
 	   ConnectivityManager connectivityManager = 
 		   (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
 	   NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
 	   if (activeNetInfo != null  && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI){  
     	        return true;  
     	 }  
     return false;  
    }
     

}
