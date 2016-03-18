package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.MainMenuData;

/**
 * 本地缓存主页九宫格列表
 * @author zhangyifan
 *
 */
public class MainMenuListInfo extends BaseInfo {
	public List<MainMenuData> mainMenuList;
	
	public MainMenuListInfo() {}

	public List<MainMenuData> getMainMenuList() {
		return mainMenuList;
	}

	public void setMainMenuList(List<MainMenuData> mainMenuList) {
		this.mainMenuList = mainMenuList;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static MainMenuListInfo toBean(JSONObject jObj) {
		
		MainMenuListInfo info = new MainMenuListInfo();
		
		try {
			
			if (jObj.has("mainMenuList")) {
				List<MainMenuData> mainMenuList = new ArrayList<MainMenuData>();
				JSONArray jsonArray = jObj.getJSONArray("mainMenuList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						mainMenuList.add(
								MainMenuData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setMainMenuList(mainMenuList);
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
