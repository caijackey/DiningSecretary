package com.fg114.main.util;

import android.util.*;

import com.fg114.main.app.*;

/**
 * 日志处理
 * @author wufucheng
 */
public class LogUtils {

	private static final boolean DEBUG = Settings.DEBUG; // 调试标志
	private static final String TAG_DEFAULT = "xiaomishu"; // 默认TAG
	private static final boolean SHOW_ALL_LOG = false; // 是否显示所有Log，为true则不按照TAG条件，只按照DEBUG标志
	private static final String[] PRINT_TAGS = { TAG_DEFAULT }; // 需要输出Log的TAG集合

	public static void logD(String msg) {
		logD(TAG_DEFAULT, msg);
	}

	public static void logD(Class<?> cls, String msg) {
		logD(cls.getName(), msg);
	}

	public static void logD(String tag, String msg) {
		if (isShowLog(tag)) {
			Log.d(tag, msg);
		}
	}

	public static void logE(String msg) {
		logE(TAG_DEFAULT, msg);
	}

	public static void logE(Class<?> cls, String msg) {
		logE(cls.getName(), msg);
	}

	public static void logE(String tag, String msg) {
		if (isShowLog(tag)) {
			Log.e(tag, msg);
		}
	}

	public static void logE(Throwable tr) {
		logE(TAG_DEFAULT, tr);
	}

	public static void logE(String tag, Throwable tr) {
		logE(tag, tr.getLocalizedMessage(), tr);
	}

	public static void logE(String tag, String msg, Throwable tr) {
		if (isShowLog(tag)) {
			Log.e(tag, msg, tr);
		}
	}

	public static void logI(String msg) {
		logI(TAG_DEFAULT, msg);
	}

	public static void logI(Class<?> cls, String msg) {
		logI(cls.getName(), msg);
	}

	public static void logI(String tag, String msg) {
		if (isShowLog(tag)) {
			Log.i(tag, msg);
		}
	}

	public static void logW(String msg) {
		logW(TAG_DEFAULT, msg);
	}

	public static void logW(Class<?> cls, String msg) {
		logW(cls.getName(), msg);
	}

	public static void logW(String tag, String msg) {
		if (isShowLog(tag)) {
			Log.w(tag, msg);
		}
	}

	public static void logV(String msg) {
		logV(TAG_DEFAULT, msg);
	}

	public static void logV(Class<?> cls, String msg) {
		logV(cls.getName(), msg);
	}

	public static void logV(String tag, String msg) {
		if (isShowLog(tag)) {
			Log.v(tag, msg);
		}
	}

	public static void printEx(Exception exception) {
		if (DEBUG) {
			exception.printStackTrace();
		}
	}

	/**
	 * 返回是否需要输出此TAG的日志
	 * @param tag
	 * @return
	 */
	private static boolean isShowLog(String tag) {
		if (SHOW_ALL_LOG) {
			return DEBUG;
		} else {
			return DEBUG && isInTags(tag);
		}
	}

	/**
	 * 返回是否此TAG是否在需要输出的TAG集合内
	 * @param tag
	 * @return
	 */
	private static boolean isInTags(String tag) {
		if (PRINT_TAGS == null || PRINT_TAGS.length == 0) {
			return false;
		}
		for (String aTag : PRINT_TAGS) {
			if (aTag.equals(tag)) {
				return true;
			}
		}
		return false;
	}
}