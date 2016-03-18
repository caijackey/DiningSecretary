package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * json包装对象
 * @author qianjiefeng
 *
 */
public class JsonPack {
	//请求是否成功     约定  200：成功  其他：异常
	private int code = 200;
	//异常信息
	private String message = "";
	//对象 可以为null
	private JSONObject value = null;
	// 调用的url
	private String url = "";
	
	//是否需要更新用户信息
	private boolean needUpdateUserInfoTag;
	
	//用户信息
	private UserInfoDTO userInfo;
	
	//get,set-------------------------------------------------------------------
	public int getRe() {
		return code;
	}
	public void setRe(int re) {
		this.code = re;
	}
	public String getMsg() {
		return message;
	}
	public void setMsg(String msg) {
		this.message = msg;
	}
	public JSONObject getObj() {
		return value;
	}
	public void setObj(JSONObject obj) {
		this.value = obj;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isNeedUpdateUserInfoTag() {
		return needUpdateUserInfoTag;
	}
	public void setNeedUpdateUserInfoTag(boolean needUpdateUserInfoTag) {
		this.needUpdateUserInfoTag = needUpdateUserInfoTag;
	}
	public UserInfoDTO getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfoDTO userInfo) {
		this.userInfo = userInfo;
	}
}
