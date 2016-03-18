package com.fg114.main.analytics.dto;

import java.util.ArrayList;
import java.util.List;

import com.fg114.main.util.JsonUtils;

/**
 * @author qianjiefeng
 *
 */
public class PageStatsData  {
	//页面名称
	public String n="";
	//城市id
	public String ctid="";
	//进入时间
	public String et="";
	//餐馆id
	public String rid="";
	//现金券id
	public String cid="";
	//订单id
	public String oid="";
	
	//已上传过
	public boolean upTag; 
	//是否是进栈
	public boolean inTag=true;

	
	//页面发生的事件
	public List<PageEventData> el=new ArrayList<PageEventData>();
	
	public String toString(){
		return JsonUtils.toJson(this);
	}
}
