package com.fg114.main.service.dto;
/**
 * ####外卖列表数据####
 *
 */
public class TakeoutListData2 {
	//餐厅id
	public String uuid;
	//餐厅名称
	public String name;
	//图片url
	public String picUrl;
	//是否有优惠
	public boolean discountTag;
	//是否可以在线支付
	public boolean onlinePayTag;

	//经度
	public double longitude; 
	//纬度
	public double latitude;

	//总体评价
	public double overallNum;
	//状态名称
	public String stateName;
	//状态背景颜色
	public String stateColor;
	
	//起送价格
	public String sendLimitPrice;
	//送达时间
	public String sendReachMins;
	//距离
	public String distanceMeter;
}
