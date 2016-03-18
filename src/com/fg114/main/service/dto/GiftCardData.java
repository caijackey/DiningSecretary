package com.fg114.main.service.dto;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class GiftCardData  {
	//uuid
	private String uuid;
	//名称
	private String name;
	//价格
	private double price;

	
	//get,set-------------------------------------------------------------------
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public static GiftCardData toBean(JSONObject jObj) {
		
		GiftCardData dto = new GiftCardData();

		try {

			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("price")) {
				dto.setPrice(jObj.getInt("price"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			
		}
		return dto;
	}

	
}
