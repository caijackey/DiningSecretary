package com.fg114.main.app.receiver;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.fg114.main.util.ContextUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
	// 重写onReceive方法
	@Override
	public void onReceive(Context context, Intent intent) {
		// 后边的XXX.class就是要启动的服务
		//Intent service = new Intent(context, XXXclass);
		//context.startService(service);
//		Log.v("TAG", "开机自动服务自动启动.....");
//		// 启动应用，参数为需要自动启动的应用的包名
//		Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
//		context.startActivity(intent);
		
		//绑定百度推送
		String appkey = "fHNMZ9h8ENulzfoIxVoXjdG3";
		// 以apikey的方式登录，一般放在主Activity的onCreate中
        PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, appkey);
	}

}
