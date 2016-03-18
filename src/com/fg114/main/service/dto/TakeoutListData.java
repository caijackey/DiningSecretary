package com.fg114.main.service.dto;


public class TakeoutListData  {

	//外卖餐厅id
	public String uuid;
	//是否开业中
	public boolean openTag;
	//图片url
	public String picUrl;
	//餐厅名称
	public String name;
	//是否有赠品
	public boolean haveGiftTag;

	//餐厅地址
	public String address;
	//经度
	public double longitude; 
	//纬度
	public double latitude;
	
	//总体评价
	public double overallNum;
	//状态名称
	public String stateName;
	//状态颜色
	public String stateColor;
	
	//起送价格
	public String sendLimitPrice;
	//送达时间
	public String sendReachMins;
	//距离
	public String distanceMeter;

}
