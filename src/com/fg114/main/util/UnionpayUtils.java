package com.fg114.main.util;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.webkit.WebView;

import com.unionpay.upomp.bypay.util.*;

/**
 * 银联支付工具类
 * @author wufucheng
 *
 */
public class UnionpayUtils {
	
	// 声明本地包及手机POS包常量
	public static final String MY_PKG = "com.fg114.main";
	public static final String PLUGIN_PKG_PAYMENT = "com.unionpay.upomp.bypay.paymain_com_fg114_main";
	
	// 调用手机POS进行支付
	public static void doPay(Activity activity, String xml) {
		// 初始化手机POS环境
		Utils.setPackageName(MY_PKG);
		UPOMP.init();
		// 设置跳转到手机POS Intent
		Intent intent = new Intent(PLUGIN_PKG_PAYMENT);
		// 填充所携带的Bundle
		Bundle mBundle = new Bundle();
		// XML格式根据协议定义
		mBundle.putString("xml", xml);
		// 将Bundle传递给intent
		intent.putExtras(mBundle);
		Log.e("UnionpayUtils", xml);
		// 使用intent转到手机POS
		activity.startActivity(intent);
	}
	
	public static String getUnionpayResult() {
		return UPOMP.getPayResult();
	}
}
