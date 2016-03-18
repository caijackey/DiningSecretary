package com.fg114.main.service.dto;

import java.util.List;

/**
 * 餐馆详情对象
 * 
 * @author qianjiefeng
 * 
 */
public class TakeoutInfoData {

	// 餐馆id
	public String uuid;
	// 是否已收藏
	public boolean favTag;

	// 餐馆 图片url
	public String picUrl;
	// 餐馆名称
	public String name;
	// 总体评价
	public double overallNum;
	// 起送价格
	public int sendLimitPrice;
	//用来在餐厅详情显示用
	public String sendLimitPriceHint; 
	// 送达时间
	public int sendReachMins;

	// 地址
	public String address;
	// 经度
	public double longitude;
	// 纬度
	public double latitude;
	// 营业时间
	public String openTimeInfo;
	// 餐馆信息中的电话号码列表
	public List<RestTelInfo> telList;

	// 点评数量
	public int totalCommentNum;
	// 点评
	public TakeoutCommentData commentData;

	//送餐时间列表   uuid:uuid  name:时间     比如 16:00   16:15  
	public List<CommonTypeDTO> sendTimeList;
	
	//状态名称
	public String stateName;
	//状态颜色
	public String stateColor;
    
    //餐厅介绍
	public String detail;
    //菜系类型
	public String menuType;

    
    //是否可以下单
	public boolean canOrderTag;
    //不能下单的提示
	public String hintForCanNotOrder;
    //是否是特约商户
	public boolean vipTag;
    //是否显示确认页面按钮
	public boolean canShowConfrimBtnTag;
    //不能确认的提示
	public String hintForCanNotConfirm;


}
