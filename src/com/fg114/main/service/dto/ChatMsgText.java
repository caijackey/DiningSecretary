package com.fg114.main.service.dto;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgText  {
	
	String detail = "";
	
	// 自用属性
	private int dataTypeTag;
	private long selectTime;
	private String resId = "";
	private String resName = "";
	private String roomText = "";

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getDataTypeTag() {
		return dataTypeTag;
	}

	public void setDataTypeTag(int dataTypeTag) {
		this.dataTypeTag = dataTypeTag;
	}

	public long getSelectTime() {
		return selectTime;
	}

	public void setSelectTime(long selectTime) {
		this.selectTime = selectTime;
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

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}
}
