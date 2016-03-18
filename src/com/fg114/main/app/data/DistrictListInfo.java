package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DistrictListInfo extends BaseInfo {
	
	public List<BaseData> districtList;
	
	public DistrictListInfo() {}
	
	public List<BaseData> getDistrictList() {
		return districtList;
	}

	public void setDistrictList(List<BaseData> districtList) {
		this.districtList = districtList;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static DistrictListInfo toBean(JSONObject jObj) {
		
		DistrictListInfo info = new DistrictListInfo();
		
		try {
			if (jObj.has("districtList")) {
				List<BaseData> districtList = new ArrayList<BaseData>();
				JSONArray jsonArray = jObj.getJSONArray("districtList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						districtList.add(
								BaseData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setDistrictList(districtList);
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
