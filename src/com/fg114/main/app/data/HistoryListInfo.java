package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.service.dto.ResAndFoodData;

/**
 * 本地缓存浏览历史列表
 * @author zhangyifan
 *
 */
public class HistoryListInfo extends BaseInfo {
	public List<ResAndFoodData> historyList;
	
	public HistoryListInfo() {}

	public List<ResAndFoodData> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<ResAndFoodData> historyList) {
		this.historyList = historyList;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static HistoryListInfo toBean(JSONObject jObj) {
		
		HistoryListInfo info = new HistoryListInfo();
		
		try {
			
			if (jObj.has("historyList")) {
				List<ResAndFoodData> historyList = new ArrayList<ResAndFoodData>();
				JSONArray jsonArray = jObj.getJSONArray("historyList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						historyList.add(
								ResAndFoodData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setHistoryList(historyList);
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
