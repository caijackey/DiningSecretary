package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HotDistrictInfo extends BaseInfo {
	
	public List<BaseData> hotDistrictList;
	
	public HotDistrictInfo() {}

	public List<BaseData> getHotDistrictList() {
		return hotDistrictList;
	}

	public void setHotDistrictList(List<BaseData> hotDistrictList) {
		this.hotDistrictList = hotDistrictList;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static HotDistrictInfo toBean(JSONObject jObj) {
		
		HotDistrictInfo info = new HotDistrictInfo();
		try {
			
			if (jObj.has("hotDistrictList")) {
				List<BaseData> hotDistrictList = new ArrayList<BaseData>();
				JSONArray jsonArray = jObj.getJSONArray("hotDistrictList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						hotDistrictList.add(
								BaseData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setHotDistrictList(hotDistrictList);
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
