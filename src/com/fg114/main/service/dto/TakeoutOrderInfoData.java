package com.fg114.main.service.dto;

import java.util.List;



/**
 * @author qianjiefeng
 *
 */
public class TakeoutOrderInfoData  {
	
    //需要提醒的订单数量
	public int hintOrderNum;
	
	//订单提示信息
	public TakeoutOrderHintData orderHintData;
	
	//菜单信息
	public String menuInfo;
	
	//收货地址  uuid:uuid name:地址  phone:电话
	public CommonTypeDTO userReceiveAdressData;
	
	
	//是否可以打电话
	public boolean canTelTag;
    // 餐馆信息中的电话号码列表
	public List<RestTelInfo> telList;
	
    //是否可以催一下
    public boolean canUrgeTag;
	//确认送达按钮  0:无  1：确认送达  2：点评一下  3：查看评论
	public int operateBtnTag;
	
}





