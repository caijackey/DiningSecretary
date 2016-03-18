package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegionListInfo extends BaseInfo {
	
	public List<BaseData> regionList;
	
	public RegionListInfo() {}
	
	
	public List<BaseData> getRegionList() {
		return regionList;
	}


	public void setRegionList(List<BaseData> regionList) {
		this.regionList = regionList;
	}


	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static RegionListInfo toBean(JSONObject jObj) {
		
		RegionListInfo info = new RegionListInfo();
		
		try {
			if (jObj.has("regionList")) {
				List<BaseData> regionList = new ArrayList<BaseData>();
				JSONArray jsonArray = jObj.getJSONArray("regionList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						regionList.add(
								BaseData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setRegionList(regionList);
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
