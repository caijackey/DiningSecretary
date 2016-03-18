//package com.fg114.main.app.location;
//
//import java.util.Date;
//
//import com.fg114.main.analytics.OpenPageDataTracer;
//import com.fg114.main.util.ActivityUtil;
//import com.fg114.main.util.IOUtils;
//
//import android.app.Application;
//import android.location.Location;
//import android.location.LocationManager;
//import android.os.Handler;
//import android.util.Log;
//
//public class LocBaiduThread extends LocBaseThread {
//	public static boolean isScreenOn=true;
//	public LocBaiduThread(Handler handler, Application myApp) {
//		super(handler, myApp,"baidu");
//	}
//
//	@Override
//	public void watch() throws Exception {
//		
//		if (ActivityUtil.isOnForeground(Loc.myApp)&&isScreenOn) {
//			//开始监听
//			handler.sendMessage(handler.obtainMessage(Loc.MSG_START_UPDATE)); 
//		}
//		else {
//			//停止监听
//			handler.sendMessage(handler.obtainMessage(Loc.MSG_STOP_UPDATE)); 
//			
//			// 触发上传点击流---------------
//			if(OpenPageDataTracer.upTag==false){
//				OpenPageDataTracer.upTag=true;
//				OpenPageDataTracer.getInstance().uploadImmediately();
//			}
//			// ----------------------------
//		}
//    	
//		//停10秒
//		sleep(1000);
//	} 
//
//
//    
//}
