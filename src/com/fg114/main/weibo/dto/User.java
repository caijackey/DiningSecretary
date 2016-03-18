package com.fg114.main.weibo.dto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 微博用户类
 * 
 * @author xujianjun,2012-03-22
 * 
 */
public class User implements Serializable {
	
	protected boolean isRecentPerson;
	// 首字母
	protected String firstLetter = "";
	// 拼音
	protected String pinyin = "";
	// 昵称
	protected String name = "";
	//账户
	protected String account="";
	// 头像url
	protected String picUrl = "";


	// -----
	public String getFirstLetter() {
		return firstLetter;
	}

	public void setFirstLetter(String firstLetter) {
		this.firstLetter = firstLetter;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public boolean isRecentPerson() {
		return isRecentPerson;
	}

	public void setRecentPerson(boolean isRecentPerson) {
		this.isRecentPerson = isRecentPerson;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

}
