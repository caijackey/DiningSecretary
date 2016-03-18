package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 推送内容DTO
 * @author qianjiefeng
 *
 */
public class PushMsgPackDTO implements Serializable {
	
	//类别标志  
	private int typeTag;  //1:就餐前提醒  2：就餐后提醒   3:升级提醒
	//消息
	private PushMsgDTO msg;
	

	//get,set-------------------------------------------------------------------
	public int getTypeTag() {
		return typeTag;
	}
	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}
	public PushMsgDTO getMsg() {
		return msg;
	}
	public void setMsg(PushMsgDTO msg) {
		this.msg = msg;
	}

	
	
	

}
