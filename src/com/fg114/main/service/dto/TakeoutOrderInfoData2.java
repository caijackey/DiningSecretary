package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####订单详情####
 *
 */
public class TakeoutOrderInfoData2 {
	//需要提醒的订单数量
	public int hintOrderNum;
	
	//订单提示信息
	public TakeoutOrderHintData orderHintData;
	
	//是否可以打电话
	public boolean canTelTag;
    // 餐馆信息中的电话号码列表
	public List<RestTelInfo> telList;
    //是否可以点评
	public boolean canCommentTag;

    //提示(标红)
	public String hint;
	
    //选择的菜品
	public TakeoutMenuSelPackDTO menuSelPack;
	
	//收货地址
	public UserTkRaData userReceiveAdressData;
	
    //点评
	public TakeoutCommentData commentData;
	
    //是否可以取消订单
	public boolean canCancelTag;
	//是否需要在线支付
	public boolean needOnlinePayTag;
}
