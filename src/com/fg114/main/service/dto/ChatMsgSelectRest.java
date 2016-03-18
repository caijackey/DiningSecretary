package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgSelectRest  {
	//标题
	String title = "";
	//内容
	String detail = "";
	//餐馆列表
	List<RealTimeTableRestData> list = new ArrayList<RealTimeTableRestData>();
	
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
	public List<RealTimeTableRestData> getList() {
		return list;
	}
	public void setList(List<RealTimeTableRestData> list) {
		this.list = list;
	}

	
}
