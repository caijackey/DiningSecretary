package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####餐厅####
 *
 */
public class MdbRestInfoData {
	//餐馆id  
	public String uuid;
	//餐馆名称 
	public String restName;
	//是否已收藏
	public boolean favTag;
	
	
	//图片列表 
	public List<CommonPicData> picList;
	//订餐按钮名称
	public String orderBtnName;
	
	
	//免单公告
	public String freeNotice;
	//免单公告wapUrl
	public String freeNoticeWapUrl;
	
	
	//消费提示标题
	public String hintTitle;
	//消费提示内容
	public String hintDetail;
	
	
	//特色菜数量
	public int totalSpecialFoodNum;
	//特色菜列表  固定最多5个
	public List<RestFoodData> specialFoodList;
	
	
	//经度
	public double longitude; 
	//纬度
	public double latitude;
	
	//百度经度
	public double bdLon;
	//百度纬度
	public double bdLat;
	//地址 
	public String address;
    // 餐馆信息中的电话号码列表
	public List<RestTelInfo> telList;
	
    
    //餐厅详情
	public String restDetail;
	
	
    //分享信息
	public ShareInfoData shareInfo;
}
