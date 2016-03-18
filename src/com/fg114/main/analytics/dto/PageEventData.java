package com.fg114.main.analytics.dto;

import java.util.List;

import com.fg114.main.util.JsonUtils;

/**
 * @author qianjiefeng
 *
 */
public class PageEventData  {
	//事件名称  
	public String n="";
	//开始时间，是long数据的字符串
	public String st="";
	//结束时间，是long数据的字符串
	public String et="";
	//备注
	public String me;
	
	public String toString(){
		return JsonUtils.toJson(this);
	}
}
