//package com.fg114.main.app.location;
//
//import java.util.Date;
//
//import com.fg114.main.util.IOUtils;
//
//import android.app.Application;
//import android.location.Location;
//import android.location.LocationManager;
//import android.os.Handler;
//
//public class LocGpsThread extends LocBaseThread {
//	
//    public LocGpsThread(Handler handler, Application myApp) {
//		super(handler, myApp,LocationManager.GPS_PROVIDER);
//	}
//    
//	@Override
//	public void watch() throws Exception {
//		//如果没查到gps定位就停顿两分钟
//		boolean needChkTag=true;
//		if (isOkTag==false) {
//		    if (tryNum<=THREAD_GPS_LOOP_NUM) {
//		    	needChkTag=false;
//		    	tryNum++;
//		    } else {
//		    	tryNum=0;
//		    }
//		}
//		
//		Location tmpLoc = null;
//		if (needChkTag) {
//			//开始监听
//			
//			// 将来自Listener的变量置空
//			Loc.tmpGpsLocByListener = null;
//			
//			handler.sendMessage(handler.obtainMessage(Loc.MSG_START_UPDATE)); 
//			//取经纬度
//			int i = 0;
//			long nt = new Date().getTime();
//	    	while (true) {
//				sleep(THREAD_INTERVAL); //1秒
//				
//				// 如果来自Listener的变量不为空，即Listener更新了位置
//				if (Loc.tmpGpsLocByListener != null) {
//					
//					StringBuffer sb = new StringBuffer();
//					sb.append("isOkTag tmpGpsLocByListener").append(" -- ");
//					sb.append(Loc.tmpGpsLocByListener.getProvider()).append(" -- ");
//					sb.append(Loc.getFormatDateStr(Loc.tmpGpsLocByListener.getTime())).append(" -- ");
//					sb.append(Loc.tmpGpsLocByListener.getLongitude()).append(",").append(Loc.tmpGpsLocByListener.getLatitude()).append("\r\n");
//					IOUtils.writeTestInfo(Loc.myApp, "log_gps.txt", sb.toString());
//					
//					isOkTag = true;
//					break;
//				}
//				
////				//成功获取就退出循环
////				tmpLoc = Loc.getAvailableLocByPoviderAndTime(provider,nt);
////				if (tmpLoc!=null) {
////					isOkTag = true;
////					break;
////				}
//				
//				i++;
//				//尝试30次 不成功 退出
//				if (i>=THREAD_GPS_TRY_NUM) {
//					isOkTag = false;							
//					break;
//				}
//	    	}
//	    	//停止监听
//			handler.sendMessage(handler.obtainMessage(Loc.MSG_STOP_UPDATE)); 
//		}
//		
//    	//设置获取的loc
//    	if (isOkTag) {
//    		Loc.setTmpGpsLoc(Loc.tmpGpsLocByListener);
//    	}
//		Loc.isGpsOkTag = isOkTag;
//
//    	//停60秒
//    	sleep(THREAD_GPS_STOP_SLEEP); //60秒
//	} 
//
//}
