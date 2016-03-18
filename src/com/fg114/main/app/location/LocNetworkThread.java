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
//public class LocNetworkThread extends LocBaseThread {
//
//	public LocNetworkThread(Handler handler, Application myApp) {
//		super(handler, myApp,LocationManager.NETWORK_PROVIDER);
//	}
//
//	@Override
//	public void watch() throws Exception {
//		//如果没查到网络定位就停顿两分钟
//		boolean needChkTag=true;
//		if (isOkTag==false) {
//		    if (tryNum<=THREAD_NETWORK_LOOP_NUM) {
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
//			Loc.tmpNetworkLocByListener = null;
//			
//			handler.sendMessage(handler.obtainMessage(Loc.MSG_START_UPDATE)); 
//			
////			sleep(2000); //取网络位置之前先停2秒
//			//取经纬度
//			int i = 0;
//			long nt = new Date().getTime();
//	    	while (true) {
//				sleep(100); //500ms
//				
//				// 如果来自Listener的变量不为空，即Listener更新了位置
//				if (Loc.tmpNetworkLocByListener != null) {
//					
//					StringBuffer sb = new StringBuffer();
//					sb.append("isOkTag tmpNetworkLocByListener").append(" -- ");
//					sb.append(Loc.tmpNetworkLocByListener.getProvider()).append(" -- ");
//					sb.append(Loc.getFormatDateStr(Loc.tmpNetworkLocByListener.getTime())).append(" -- ");
//					sb.append(Loc.tmpNetworkLocByListener.getLongitude()).append(",").append(Loc.tmpNetworkLocByListener.getLatitude()).append("\r\n");
//					IOUtils.writeTestInfo(Loc.myApp, "log_net.txt", sb.toString());
//					
//					isOkTag = true;
//					break;
//				}
//				
////				// 当Network Listener的onLocationChanged被触发时更新locationTime，  若locationTime在nt之后，认为通过Network获取的位置信息是有效的
////				if(Loc.locationTime >= nt) {
////					//成功获取就退出循环
////					tmpLoc = Loc.getAvailableLocByPoviderAndTime(provider,nt);
////					if (tmpLoc!=null) {
////						isOkTag = true;
////						break;
////					}
////				}
//				
//				i++;
//				//尝试10次 不成功 退出
//				if (i>=THREAD_NETWORK_TRY_NUM) {	
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
//    		Loc.setTmpNetworkLoc(Loc.tmpNetworkLocByListener);
//    	}
//    	
//		//停60秒
//		sleep(THREAD_NETWORK_STOP_SLEEP);
//	} 
//
//
//    
//}
