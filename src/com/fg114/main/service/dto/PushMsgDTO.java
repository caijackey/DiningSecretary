package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 推送内容DTO
 * @author qianjiefeng
 *
 */
public class PushMsgDTO implements Serializable  {
	//广告类别  1:广告链接  2：本地连接  3:普通链接
	int typeTag;
	//广告标题
	String title = "";
	//广告url
	String advUrl = "";
    //确定按钮
	String okButtonName = "";
    //取消按钮
	String cancelButtonName = "";
	
	//get,set-------------------------------------------------------------------
	
	public int getTypeTag() {
		return typeTag;
	}
	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAdvUrl() {
		return advUrl;
	}
	public void setAdvUrl(String advUrl) {
		this.advUrl = advUrl;
	}
	public String getOkButtonName() {
		return okButtonName;
	}
	public void setOkButtonName(String okButtonName) {
		this.okButtonName = okButtonName;
	}
	public String getCancelButtonName() {
		return cancelButtonName;
	}
	public void setCancelButtonName(String cancelButtonName) {
		this.cancelButtonName = cancelButtonName;
	}
	
}
