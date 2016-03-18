package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.ActivityUtil;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class RoomState  implements Serializable  {
	//大厅  1：有    2：紧张   3：已满   4：不可预定
	int hallTag;
	//大厅名称   1：午市大厅   2：午市大厅(紧张)  3：午市大厅(已满)  4:午市大厅
	String hallName = "";
	String hallNameSuffix = "";
	//包房  同大厅
	int roomTag;
	//包房名称
	String roomName = "";
	String roomNameSuffix = "";
	//只显示提示
	boolean onlyShowHintTag;
	//提示
	String hint = "";
	
	public String getHallNameSuffix() {
		return hallNameSuffix;
	}
	public void setHallNameSuffix(String hallNameSuffix) {
		this.hallNameSuffix = hallNameSuffix;
	}
	public String getRoomNameSuffix() {
		return roomNameSuffix;
	}
	public void setRoomNameSuffix(String roomNameSuffix) {
		this.roomNameSuffix = roomNameSuffix;
	}
	public boolean isOnlyShowHintTag() {
		return onlyShowHintTag;
	}
	public void setOnlyShowHintTag(boolean onlyShowHintTag) {
		this.onlyShowHintTag = onlyShowHintTag;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public int getHallTag() {
		return hallTag;
	}
	public void setHallTag(int hallTag) {
		this.hallTag = hallTag;
	}
	public String getHallName() {
		return hallName;
	}
	public void setHallName(String hallName) {
		this.hallName = hallName;
	}
	public int getRoomTag() {
		return roomTag;
	}
	public void setRoomTag(int roomTag) {
		this.roomTag = roomTag;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static RoomState toBean(JSONObject jObj) {
		
		RoomState dto = new RoomState();

		try {
			

			if (jObj.has("hallTag")) {
				dto.setHallTag(jObj.getInt("hallTag"));
			}
			if (jObj.has("roomTag")) {
				dto.setRoomTag(jObj.getInt("roomTag"));
			}
			if (jObj.has("hallName")) {
				dto.setHallName(jObj.getString("hallName"));
			}
			if (jObj.has("roomName")) {
				dto.setRoomName(jObj.getString("roomName"));
			}
			if (jObj.has("hallNameSuffix")) {
				dto.setHallNameSuffix(jObj.getString("hallNameSuffix"));
			}
			if (jObj.has("roomNameSuffix")) {
				dto.setRoomNameSuffix(jObj.getString("roomNameSuffix"));
			}
			if (jObj.has("onlyShowHintTag")) {
				dto.setOnlyShowHintTag(jObj.getBoolean("onlyShowHintTag"));
			}
			if (jObj.has("hint")) {
				dto.setHint(jObj.getString("hint"));
			}
			
		} catch (JSONException e) {
			ActivityUtil.saveException(e);
			return null;
		}
		return dto;
	}
	
	
}
