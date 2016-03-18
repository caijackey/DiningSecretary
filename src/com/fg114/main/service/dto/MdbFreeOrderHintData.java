package com.fg114.main.service.dto;

/**
 * 订单列表数据
 *
 */
public class MdbFreeOrderHintData {
	//订单id
	public String orderId;
	
	//图标url 40*40
	public String statusIconUrl;
	//状态名称   标红  
	public String statusName;

	//餐馆id
	public String restId;
	//餐厅名
	public String restName;
	
	
	//订单信息 标红
	public String orderHint;
	
	//是否需要去完善订单信息
	public boolean needCompleteOrderTag;
}
