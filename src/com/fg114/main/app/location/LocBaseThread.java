//package com.fg114.main.app.location;
//
//import java.util.List;
//
//import com.fg114.main.util.ActivityUtil;
//
//import android.app.ActivityManager;
//import android.app.ActivityManager.RunningAppProcessInfo;
//import android.app.Application;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//public abstract class LocBaseThread extends Thread {
//	public Handler handler;
//    public String provider = "";
//    
//    public boolean isOkTag = true;
//    public int tryNum = 0;
//    
//	public static final int  THREAD_INTERVAL = 1000;
//	public static final int  THREAD_GPS_LOOP_NUM = 2;
//	public static final int  THREAD_GPS_TRY_NUM = 30;
//	public static final int  THREAD_GPS_STOP_SLEEP = 60000;//60分
//	public static final int  THREAD_NETWORK_LOOP_NUM = 2;
//	public static final int  THREAD_NETWORK_TRY_NUM = 100;
//	public static final int  THREAD_NETWORK_STOP_SLEEP = 60000;//60秒
//	
//	private ActivityManager activityManager; 
//    private String packageName; 
//
//
//	public LocBaseThread(Handler handler,Application myApp,String provider) {
//		this.handler = handler;
//		this.activityManager = (ActivityManager) myApp.getSystemService(Context.ACTIVITY_SERVICE); 
//		this.packageName = myApp.getPackageName(); 
//		this.provider = provider;
//		this.isOkTag = true;
//		this.tryNum = 0;
//		start();
//	}
//	
//    public void run(){ 
//    	if (Looper.myLooper() == null) {
//			Looper.prepare();
//		}
//    	
//    	while (true) {
//    		try {
////				if (Loc.lm.isProviderEnabled(provider)==false //过滤没有打开gps or newwrok的情况  
////					|| isAppOnForeground()==false  //过滤在后台
////				) {
////					sleep(THREAD_INTERVAL);//3秒
////					continue;
////				}
//    			
////    			if (ActivityUtil.isOnForeground(Loc.myApp)) {
////    				LocBaidu.start();
////    			}
////    			else {
////    				LocBaidu.stop();
////    			}
//    			
////    			if (isAppOnForeground()==false  //过滤在后台
////    				) {
////    					sleep(THREAD_INTERVAL);//3秒
////    					continue;
////    				}
//				
//				watch();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//    	}
//     } 
//    
//    public abstract void watch() throws Exception;
//    
////    public boolean isAppOnForeground() { 
////        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses(); 
////        if (appProcesses == null) {
////        	return false; 
////        } 
////        for (RunningAppProcessInfo appProcess : appProcesses) { 
////            if (appProcess.processName.equals(packageName) 
////                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) { 
////                return true; 
////            } 
////        } 
////        return false; 
////    } 
//
//    
//}
