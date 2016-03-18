package com.fg114.main.app.location;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

/**
 * wifi信息
 * @author zhangyifan
 *
 */
public class WifiInfoManager {

	private WifiManager wifiManager;
	
	public WifiInfoManager(Context context) {
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	/**
	 * 获得所有wifi连接点
	 * @return
	 */
	public List<WifiInfo> dump() {
		if (!this.wifiManager.isWifiEnabled()) {
			return new ArrayList<WifiInfo>();
		}
		//获得当前连接的wifi设备
		android.net.wifi.WifiInfo wifiConnection = this.wifiManager.getConnectionInfo();
		WifiInfo currentWifi = null;
		if (wifiConnection != null) {
			String bssid = wifiConnection.getBSSID();
			int dBm = wifiConnection.getRssi();
			String ssid = wifiConnection.getSSID();
			currentWifi = new WifiInfo(bssid, dBm, ssid);
		}
		//将当前连接的wifi设备放在列表首位
		ArrayList<WifiInfo> allWifiList = new ArrayList<WifiInfo>();
		if (currentWifi != null) {
			allWifiList.add(currentWifi);
		}
		//检测所有wifi设备，并将其他wifi设备放入列表
		List<ScanResult> scanResultList = this.wifiManager.getScanResults();
		for (ScanResult result : scanResultList) {
			WifiInfo scanWifi = new WifiInfo(result);
			if (! scanWifi.equals(currentWifi)){
				allWifiList.add(scanWifi);
			}
		}
		return allWifiList;
	}
	
	/**
	 * 获得wifi开启状态
	 * @return
	 */
	public boolean isWifiEnabled() {
		return this.wifiManager.isWifiEnabled();
	}

	/**
	 * 将所有wifi点设为jsonArray
	 * @return
	 */
	public JSONArray wifiInfoToJSONArray() {
		JSONArray jsonArray = new JSONArray();
		for (WifiInfo wifi : dump()) {
			JSONObject localJSONObject = wifi.wifiInfoToJSON();
			jsonArray.put(localJSONObject);
		}
		return jsonArray;
	}

	/**
	 * 获得wifi管理器
	 * @return
	 */
	public WifiManager getWifiManager() {
		return this.wifiManager;
	}

	public JSONArray wifiTowersToJSONArray() {
		JSONArray jsonArray = new JSONArray();
		for (WifiInfo wifi : dump()) {
			JSONObject localJSONObject = wifi.wifiTowerToJSON();
			jsonArray.put(localJSONObject);
		}
		return jsonArray;
	}

	/**
	 * wifi情报存放类
	 * @author zhangyifan
	 *
	 */
	public class WifiInfo implements Comparable<WifiInfo> {
		//wifi设备的物理地址
        public final String bssid;
        //wifi设备的信号强度
        public final int dBm;
        //wifi设备的ip
        public final String ssid;
        
		public WifiInfo(ScanResult scanresult) {
			this.bssid = scanresult.BSSID;
			this.dBm = scanresult.level;
			this.ssid = scanresult.SSID;
		}

		public WifiInfo(String bssid, int dBm, String ssid) {
			this.bssid = bssid;
			this.dBm = dBm;
			this.ssid = ssid;
		}
        
		/**
		 * 比较信号强度
		 */
		public int compareTo(WifiInfo wifiinfo) {
			return wifiinfo.dBm - this.dBm;
		}

		/**
		 * 是否相等
		 */
		public boolean equals(Object obj) {
			boolean isEquals = false;
			if (obj == this) {
				isEquals = true;
			} else {
				if (obj instanceof WifiInfo) {
					WifiInfo wifiinfo = (WifiInfo) obj;
					int dBm = wifiinfo.dBm;
					if (this.dBm == dBm) {
						String bssid = wifiinfo.bssid;
						if (bssid.equals(this.bssid)) {
							isEquals = true;
						}
					}
				}
			}
			return isEquals;
		}

		public int hashCode() {
			return this.dBm ^ this.bssid.hashCode();
		}
		
		/**
		 * wifi设备情报to Json
		 * @return
		 */
		public JSONObject wifiInfoToJSON() {
			JSONObject jsonobject = new JSONObject();
			try {
				jsonobject.put("mac", this.bssid);
				jsonobject.put("ssid", this.ssid);
				jsonobject.put("dbm", this.dBm);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return jsonobject;
		}
		
		/**
		 * wifiTower情报
		 * @return
		 */
		public JSONObject wifiTowerToJSON() {
			JSONObject jsonobject = new JSONObject();
			try {
				jsonobject.put("mac_address", this.bssid);
				jsonobject.put("signal_strength", this.dBm);
				jsonobject.put("ssid", this.ssid);
				jsonobject.put("age", 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return jsonobject;
		}
	}
}
