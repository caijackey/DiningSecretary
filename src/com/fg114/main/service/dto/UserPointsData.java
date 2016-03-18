package com.fg114.main.service.dto;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author qianjiefeng
 *
 */
public class UserPointsData {
	
	//积分ID
	private String uuid;
	
	//创建时间
	private long createTime;
	//赢得的秘币数量
	private int gainCoinsNum;
	//当前秘币数量
	private int currentCoinsNum;
	//描述
	private String description;

	
	
	//get,set-------------------------------------------------------------------
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUuid() {
		return this.uuid;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getGainCoinsNum() {
		return gainCoinsNum;
	}
	public void setGainCoinsNum(int gainCoinsNum) {
		this.gainCoinsNum = gainCoinsNum;
	}
	public int getCurrentCoinsNum() {
		return currentCoinsNum;
	}
	public void setCurrentCoinsNum(int currentCoinsNum) {
		this.currentCoinsNum = currentCoinsNum;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static UserPointsData toBean(String str) {

		UserPointsData dto = new UserPointsData();
		try {
			JSONObject jObj = new JSONObject(str);
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("createTime")) {
				dto.setCreateTime(jObj.getLong("createTime"));
			}
			if (jObj.has("gainCoinsNum")) {
				dto.setGainCoinsNum(jObj.getInt("gainCoinsNum"));
			}
			if (jObj.has("currentCoinsNum")) {
				dto.setCurrentCoinsNum(jObj.getInt("currentCoinsNum"));
			}
			if (jObj.has("description")) {
				dto.setDescription(jObj.getString("description"));
			}

		} catch (JSONException e) {
			e.printStackTrace();

		}
		return dto;
	}
	
	
}
