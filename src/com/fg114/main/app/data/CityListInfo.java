package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 本地缓存城市列表
 * @author zhangyifan
 *
 */
public class CityListInfo extends BaseInfo {
	public List<CityInfo> cityList = new ArrayList<CityInfo>();
	//gps有没有定位到城市
	private boolean gpsLocatedTag;
	//gps定位的城市id  可以为null
	private String gpsCityId;
	//gps定位的城市name  可以为null
	private String gpsCityName;
	
	public CityListInfo() {}

	public List<CityInfo> getCityList() {
		return cityList;
	}

	public void setCityList(List<CityInfo> cityList) {
		this.cityList = cityList;
	}

	public boolean isGpsLocatedTag() {
		return gpsLocatedTag;
	}

	public void setGpsLocatedTag(boolean gpsLocatedTag) {
		this.gpsLocatedTag = gpsLocatedTag;
	}

	public String getGpsCityId() {
		return gpsCityId;
	}

	public void setGpsCityId(String gpsCityId) {
		this.gpsCityId = gpsCityId;
	}

	public String getGpsCityName() {
		return gpsCityName;
	}

	public void setGpsCityName(String gpsCityName) {
		this.gpsCityName = gpsCityName;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CityListInfo toBean(JSONObject jObj) {
		
		CityListInfo info = new CityListInfo();
		
		try {
			if (jObj.has("cityList")) {
				List<CityInfo> cityList = new ArrayList<CityInfo>();
				JSONArray jsonArray = jObj.getJSONArray("cityList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						cityList.add(
								CityInfo.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setCityList(cityList);
			}
			if (jObj.has("gpsLocatedTag")) {
				info.setGpsLocatedTag(jObj.getBoolean("gpsLocatedTag"));
			}
			if (jObj.has("gpsCityId")) {
				info.setGpsCityId(jObj.getString("gpsCityId"));
			}
			if (jObj.has("gpsCityName")) {
				info.setGpsCityName(jObj.getString("gpsCityName"));
			}
			if (jObj.has("lastUpdateTime")) {
				info.setLastUpdateTime(jObj.getLong("lastUpdateTime"));
			}
			if (jObj.has("timestamp")) {
				info.setTimestamp(jObj.getLong("timestamp"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return info;
	}
}
