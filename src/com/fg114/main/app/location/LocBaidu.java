package com.fg114.main.app.location;

import java.util.Observable;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.IOUtils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

public class LocBaidu {

	public static Location loc = null;
	static LocationClient mLocationClient = null;
	static BDLocationListener mLocationListener = null;//create时注册此listener，Destroy时需要Remove
	
	public static BDLocation currentLocation=null;
	
	private static Fg114Application myApp;
	
	public static void init(final Fg114Application app) {
		
		myApp = app;
		
		// 注册定位事件
        mLocationListener = new BDLocationListener (){

			@Override
			public void onReceiveLocation(BDLocation location) {
				//后台中 定位停止
				if(!ActivityUtil.isOnForeground(myApp.getApplicationContext())){
					Loc.sendLocControlMessage(false);
					StringBuffer sb = new StringBuffer();
					sb.append("close gps in baidu thread").append(" -- ");
					sb.append(Loc.getFormatDateStr(System.currentTimeMillis())).append(" \r\n ");
					IOUtils.writeTestInfo(myApp, "log_gps.txt", sb.toString());
					
				}
				if(location != null){	
					currentLocation=location;
					
					Location tloc=new Location("baidu["+location.getLocType()+"]");
					tloc.setLatitude(location.getLatitude());
					tloc.setLongitude(location.getLongitude());
					tloc.setTime(System.currentTimeMillis());
					
					loc = tloc;
					StringBuffer sb = new StringBuffer();
					sb.append("Update baidugps").append(" -- ");
					sb.append(loc.getProvider()).append(" -- ");
					sb.append(Loc.getFormatDateStr(loc.getTime())).append(" -- ");
					sb.append(loc.getLongitude()).append(",").append(loc.getLatitude()).append("\r\n");
					IOUtils.writeTestInfo(myApp, "log_gps.txt", sb.toString());					
					//Log.e("BaiduLoc", "onReceiveLocation" + location.getLatitude() + "," + location.getLongitude() + " " + location.getTime());
				}
			}
//			@Override
//			public void onReceivePoi(BDLocation location) {
//				
//			}
        };
		mLocationClient=new LocationClient(ContextUtil.getContext());
		mLocationClient.registerLocationListener(mLocationListener); 
	}
	
	public static void stop() {
		try {
			mLocationClient.stop();
//			myApp.mBMapMan.stop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void start() { 
		try {
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setIsNeedAddress(true); //设置是否需要地址信息，默认为无地址
			option.setCoorType("gcj02");//返回的定位结果是百度经纬度,默认值gcj02
			option.setScanSpan(7000);//设置发起定位请求的间隔时间为5000ms
			option.SetIgnoreCacheException(true);//设置是否进行异常捕捉
//			option.disableCache(true);//禁止启用缓存定位
//			option.setPoiNumber(5);	//最多返回POI个数	
//			option.setPoiDistance(1000); //poi查询距离		
//			option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息		
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
			mLocationClient.setLocOption(option);
//			myApp.mBMapMan.start();
			mLocationClient.start();
			mLocationClient.requestLocation();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
