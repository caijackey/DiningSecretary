package com.fg114.main.app.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChannelInfo extends BaseInfo {
	
	public List<BaseData> channelList;
	
	public ChannelInfo() {}

	public List<BaseData> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<BaseData> channelList) {
		this.channelList = channelList;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ChannelInfo toBean(JSONObject jObj) {
		
		ChannelInfo info = new ChannelInfo();
		try {
			
			if (jObj.has("channelList")) {
				List<BaseData> channelList = new ArrayList<BaseData>();
				JSONArray jsonArray = jObj.getJSONArray("channelList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i ++) {
						channelList.add(
								BaseData.toBean(
										jsonArray.getJSONObject(i)));
					}
				}
				info.setChannelList(channelList);
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
