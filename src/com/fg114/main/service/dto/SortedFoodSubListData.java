package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


public class SortedFoodSubListData  {
    //首字母
	private String firstLetter = "";
	//拼音
	private String pinyin;
	//uuid
	private String uuid;
	//昵称
	private String name;
	
	
	
	//get,set-------------------------------------------------------------------
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
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	
}
