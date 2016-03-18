package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.service.dto.ParentTopResListTypeData;

/**
 * 本地缓存榜单列表
 * @author zhangyifan
 *
 */
public class TopListInfo extends BaseInfo {
	//列表 
	private List<ParentTopResListTypeData> list = new ArrayList<ParentTopResListTypeData>();
	
	public TopListInfo() {}

	public List<ParentTopResListTypeData> getList() {
		return list;
	}

	public void setList(List<ParentTopResListTypeData> list) {
		this.list = list;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static TopListInfo toBean(JSONObject jObj) {
		
		TopListInfo info = new TopListInfo();
		
		try {
			if (jObj.has("list")) {
				List<ParentTopResListTypeData> list = new ArrayList<ParentTopResListTypeData>();
				JSONArray jsonArray = jObj.getJSONArray("list");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						list.add(
								ParentTopResListTypeData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setList(list);
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
