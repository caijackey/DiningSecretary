package com.fg114.main.service.dto;


public class SimpleData  {
	
	//uuid
	private String uuid = "";	
	//restUrl
	private String restUrl = "";
	//picUrl
	private String picUrl = "";
	//提示信息，用于向用户显示
	private String msg = "";
	//错误代码  默认0     
	//101：新浪微博未绑定   102：微博已过期,过期时更新本地缓存的过期时间sinaBindRemainSecs为0
	private int errorCode;


	//是否成功
	private boolean succTag;
	//需要到点菜页
	private boolean needToDishPageTag;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public boolean isSuccTag() {
		return succTag;
	}

	public void setSuccTag(boolean succTag) {
		this.succTag = succTag;
	}

	public boolean isNeedToDishPageTag() {
		return needToDishPageTag;
	}

	public void setNeedToDishPageTag(boolean needToDishPageTag) {
		this.needToDishPageTag = needToDishPageTag;
	}
	
}
