package com.fg114.main.service.dto;



/**
 * @author qianjiefeng
 *
 */
public class TakeoutOrderHintData  {
	
	//订单id
	public String orderId;
	
	//图标url
	public String iconUrl;

	//餐馆id
	public String takeoutId;
	//餐厅名
	public String takeoutName;
	
	
	//就餐时间  格式为 06月10日 18:50
	public String reserveTime;
	//提示  2份 共30元
	public String hint;
	
	//状态名称   xxxxx<font color=#000000>xxx</font>xxx  
	public String statusName;
	
	//预订信息 标红
	public String reserveInfo;

}
