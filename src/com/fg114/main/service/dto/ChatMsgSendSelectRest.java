package com.fg114.main.service.dto;

import java.util.List;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgSendSelectRest {
	//餐馆id
	String restId;
	//大厅或包房  1:大厅  2：包房
	int selectRoomTypeTag;
	
	// 自用属性
	private transient String resName = "";
	private transient String roomText = "";
	
	public String getRestId() {
		return restId;
	}
	public void setRestId(String restId) {
		this.restId = restId;
	}
	public int getSelectRoomTypeTag() {
		return selectRoomTypeTag;
	}
	public void setSelectRoomTypeTag(int selectRoomTypeTag) {
		this.selectRoomTypeTag = selectRoomTypeTag;
	}
	public String getResName() {
		return resName;
	}
	public void setResName(String resName) {
		this.resName = resName;
	}
	public String getRoomText() {
		return roomText;
	}
	public void setRoomText(String roomText) {
		this.roomText = roomText;
	}
}
