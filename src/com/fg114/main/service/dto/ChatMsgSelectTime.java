package com.fg114.main.service.dto;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgSelectTime  {
	//标题
	String title = "";
	//内容
	String detail = "";
	//所选时间
	long selectTime;
	
	// 自用属性
	// 是否隐藏"确认时间"的按钮
	private boolean hideConfirmButton;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public long getSelectTime() {
		return selectTime;
	}
	public void setSelectTime(long selectTime) {
		this.selectTime = selectTime;
	}
	public boolean isHideConfirmButton() {
		return hideConfirmButton;
	}
	public void setHideConfirmButton(boolean hideConfirmButton) {
		this.hideConfirmButton = hideConfirmButton;
	}
	
	
}
