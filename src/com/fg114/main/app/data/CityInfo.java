package com.fg114.main.app.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class CityInfo extends BaseData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String phone;
	
	private long timestamp;
	
	public CityInfo() {}
	
	
	
	public long getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}



	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CityInfo toBean(JSONObject jObj) {
		
		CityInfo data = new CityInfo();
		try {
			if (jObj.has("id")) {
				data.setId(jObj.getString("id"));
			}
			if (jObj.has("name")) {
				data.setName(jObj.getString("name"));
			}
			if (jObj.has("phone")) {
				data.setPhone(jObj.getString("phone"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return data;
	}

}
