//package com.fg114.main.app.location;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.message.BasicNameValuePair;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.content.Context;
//import android.location.Location;
//import android.location.LocationManager;
//import android.util.Log;
//
//import com.fg114.main.app.Settings;
//import com.fg114.main.service.http.A57HttpApiV3;
//import com.fg114.main.service.task.ErrorLogTask;
//
//public class CellOrWifiLocationUtil {
//	
//	private static final String TAG = "CellOrWifiLocationUtil";
//	private static final boolean DEBUG = Settings.DEBUG;
//	
//	/** 位置过期时间 */
//	private static final long LOCATION_OVER_TIME = 1000 * 60;
//   
//	//调用google gears的方法，该方法调用gears来获取经纬度
//    public static Location callGear(Context context) {

//    	try {
//			Location loc = new Location(LocationManager.NETWORK_PROVIDER);

			// CellInfoManager cellInfo = new CellInfoManager(context);
			// WifiInfoManager wifiInfo = new WifiInfoManager(context);
			
//			CellInfoManager cellInfo = Loc.cellInfo;
//			WifiInfoManager wifiInfo = Loc.wifiInfo;

//			if (cellInfo == null) {
//				Loc.cellInfo = new CellInfoManager(context);
//				cellInfo = Loc.cellInfo;
//			}
//			if (wifiInfo == null) {
//				Loc.wifiInfo = new WifiInfoManager(context);
//				wifiInfo = Loc.wifiInfo;
//			}
//
//    		String radioType = "";
//    		int mmc = 0;
//    		int mnc = 0;
//    		String cellTowers = "";
    		
//    		if (cellInfo.isAvailable) {
//	        	if (cellInfo.getBid() != -1 && cellInfo.getCid() != -1 )  {
//	        		if (cellInfo.isGsm()) {
//		        		radioType = "gsm";
//		            } else if (cellInfo.isCdma()){
//		            	radioType = "cdma";
//		            } else {
//		            	radioType = "wcdma";
//		            }
//		        	
//		        	mmc = cellInfo.getMcc();
//		        	mnc = cellInfo.getMnc();
//		        	cellTowers = cellInfo.cellTowersToJSONArray().toString();
//	        	}
//    		}
//        	String wifiTowers = wifiInfo.wifiTowersToJSONArray().toString();
        	
//        	if (DEBUG) {
//        		StringBuffer str = new StringBuffer();
//        		str.append("\"{");
//        		str.append("version:1.1.0");
//        		str.append(",host:maps.google.com");
//        		str.append(",home_mobile_country_code:");
//        		str.append(mmc);
//        		str.append(",home_mobile_network_code:");
//        		str.append(mnc);
//        		str.append(",radio_type:");
//        		str.append(radioType);
//        		str.append(",request_address:true");
//        		str.append(",cell_towers:");
//        		str.append(cellTowers);
//        		str.append(",wifi_towers:");
//        		str.append(wifiTowers);
//        		str.append("}\"");
//        		ErrorLogTask errorTask = new ErrorLogTask(null, context, "cell location debug", str.toString());
//        		errorTask.execute();
//        	}
//        	HttpPost post;
//        	if (cellInfo.isAvailable) {
//	        	post = A57HttpApiV3.getInstance().mHttpApi.createHttpPostGoogle("http://www.google.com/loc/json", 
//	        			new BasicNameValuePair("version", "1.1.0"),
//	        			new BasicNameValuePair("host", "maps.google.com"),
//	        			new BasicNameValuePair("home_mobile_country_code", String.valueOf(mmc)),
//	        			new BasicNameValuePair("home_mobile_network_code", String.valueOf(mnc)),
//	        			new BasicNameValuePair("radio_type", radioType),
//	        			new BasicNameValuePair("request_address", "true"),
//	        			new BasicNameValuePair("cell_towers", cellTowers),
//	        			new BasicNameValuePair("wifi_towers", wifiTowers));
//        	} else {
//        		post = A57HttpApiV3.getInstance().mHttpApi.createHttpPostGoogle("http://www.google.com/loc/json", 
//	        			new BasicNameValuePair("version", "1.1.0"),
//	        			new BasicNameValuePair("host", "maps.google.com"),
//	        			new BasicNameValuePair("wifi_towers", wifiTowers));
//        	}
                   
//        	DefaultHttpClient client = new DefaultHttpClient();
//        	HttpPost post = new HttpPost("http://www.google.com/loc/json");
//        	JSONObject holder = new JSONObject();
    		
//            holder.put("version", "1.1.0");
//            holder.put("host", "maps.google.com");
//            holder.put("home_mobile_country_code", cellInfo.getMcc());
//            holder.put("home_mobile_network_code", cellInfo.getMnc());
//            if (cellInfo.isGsm()) {
//            	holder.put("radio_type", "gsm");
//            } else if (cellInfo.isCdma()){
//            	holder.put("radio_type", "cdma");
//            } else {
//            	holder.put("radio_type", "wcdma");
//            }
//            holder.put("request_address", true);
//            holder.put("cell_towers", cellInfo.cellTowersToJSONArray());
//            holder.put("wifi_towers", wifiInfo.wifiTowersToJSONArray());
           
//            if (DEBUG) Log.d(TAG, "基站定位参数：" + holder);
            
            //获得结果
//            StringEntity se = new StringEntity(holder.toString());
//            post.setEntity(se);
//            HttpResponse resp = A57HttpApiV3.getInstance().mHttpApi.executeHttpRequest(post);
//            HttpEntity entity = resp.getEntity();
//            BufferedReader br = new BufferedReader(
//                            new InputStreamReader(entity.getContent()));
//            StringBuffer sb = new StringBuffer();
//            String result = br.readLine();
//            while (result != null) {
//            	sb.append(result);
//            	result = br.readLine();
//            }
//            JSONObject data = new JSONObject(sb.toString());
//            data = (JSONObject) data.get("location");
//            loc.setLatitude((Double) data.get("latitude"));
//            loc.setLongitude((Double) data.get("longitude"));
//            loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
//            loc.setTime(System.currentTimeMillis());
//            if (DEBUG) Log.d(TAG, "latitude：" + loc.getLatitude());
//            if (DEBUG) Log.d(TAG, "longitude：" + loc.getLongitude());
//            return loc;
//	    } catch (JSONException e) {
//	    	return null;
//	    } catch (UnsupportedEncodingException e) {
//	    	return null;
//	    } catch (ClientProtocolException e) {
//	    	return null;
//	    } catch (IOException e) {
//	    	return null;
//	    } catch (Exception e) {
//	    	return null;
//	    }
//    }
//    
//    /**
//     * 位置是否过期
//     * @param timeStamp
//     * @return
//     */
//    public static boolean isOverTime(long timeStamp) {
//    	if ((System.currentTimeMillis() - timeStamp) > LOCATION_OVER_TIME) {
//    		return true;
//    	} else {
//    		return false;
//    	}
//    }
//}