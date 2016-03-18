package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.service.dto.CommonTypeDTO;

public class SearchHistoryInfo {
	
	public List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
	
	public List<CommonTypeDTO> foodList = new ArrayList<CommonTypeDTO>();
	
	public SearchHistoryInfo() {}

	public List<CommonTypeDTO> getResList() {
		return list;
	}
	public void setResList(List<CommonTypeDTO> list) {
		this.list = list;
	}
	
	public List<CommonTypeDTO> getFoodList() {
		return foodList;
	}
	public void setFoodList(List<CommonTypeDTO> list) {
		this.foodList = list;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static SearchHistoryInfo toBean(JSONObject jObj) {
		
		SearchHistoryInfo info = new SearchHistoryInfo();
		try {
			
			if (jObj.has("list")) {
				List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
				JSONArray jsonArray = jObj.optJSONArray("list");
				if (jsonArray != null && jsonArray.length() > 0) {
					CommonTypeDTO dto = null;
					for (int i = 0; i < jsonArray.length(); i ++) {
						dto = CommonTypeDTO.toBean(jsonArray.getJSONObject(i));
						dto.setNum(-1);
						list.add(dto);
					}
				}
				info.setResList(list);
				
				list = new ArrayList<CommonTypeDTO>();
				jsonArray = jObj.optJSONArray("foodList");
				if (jsonArray != null && jsonArray.length() > 0) {
					CommonTypeDTO dto = null;
					for (int i = 0; i < jsonArray.length(); i ++) {
						dto = CommonTypeDTO.toBean(jsonArray.getJSONObject(i));
						dto.setNum(-1);
						list.add(dto);
					}
				}
				info.setFoodList(list);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}

}
