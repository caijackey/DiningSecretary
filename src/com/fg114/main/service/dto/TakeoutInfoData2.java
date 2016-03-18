package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####外卖餐厅详情####
 *
 */
public class TakeoutInfoData2 {
	//餐馆id  
	public String uuid;
	//是否已收藏
	public boolean favTag;
	
	//餐馆图片url 
	public String picUrl;
	//餐馆名称 
	public String name;
	//总体评价
	public double overallNum;
	//状态名称
	public String stateName;
	//状态颜色
	public String stateColor;
	//起送提示(标红)
	public String hint;
	//是否可以下单
	public boolean canOrderTag;
	
	//地址 
	public String address;
	//经度
	public double longitude; 
	//纬度
	public double latitude;
	//百度经度
	public double bdLon;
	//百度纬度
	public double bdLat;
    //电话列表
	public List<RestTelInfo> telList;
    //营业时间
	public String openTimeInfo;

    //餐厅介绍
	public String detail;
    //菜系类型
	public  String menuType;
    //外送须知(标红)
	public String sendHint;
    
	//优惠列表
	public List<MainPageAdvData> advList;
    
    //点评数量
	public int totalCommentNum;
    //点评
	public TakeoutCommentData commentData;

    //餐厅资质列表
	public List<TakeoutCerData> cerList;
    
    //分享信息
	public ShareInfoData shareInfo;
}
