package com.fg114.main.service.dto;

import java.util.List;

/**
 * 餐馆详情对象
 * @author qianjiefeng
 *
 */
public class RestInfoData  {
	//餐馆id  
	public String uuid;
	//餐馆名称 
	public String name;
	//是否已收藏
	public boolean favTag;
	
	//餐馆 图片url 
	public String restPicUrl;
	//餐馆 图片url (用于榜单)
	public String restPicUrlForTopRest;
	//餐馆图片数量
	public int restPicNum;
	
	//人均
	public String avgPrice;
	//口味 
	public String tasteNum;
	//环境
	public String envNum;
	//服务 
	public String serviceNum;
	
	//是否可下单
	public boolean canBookingTag;
    //预订电话   返回这个餐厅的电话
	public String telForBooking;
	
	//预定按钮名称
	public String bookingBtnName;
	
    //餐馆电话 用于编辑
	public String telForEdit;
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
    // 餐馆信息中的电话号码列表
	public List<RestTelInfo> telList;
    //营业时间
	public String openTimeInfo;
	//菜系 
	public String menuTypeInfo;
	//交通路线
	public String trafficLine;
	//公交信息 
	public String busInfo;
	//消费方式 
	public String consumeType;	
	//餐厅介绍
    public String detail;
    //榜单推荐理由
    public String topRestReason;
	//停车地图url
	public String parkingPicUrl;

    //优惠列表
	public List<RestPromoData> promoList;
	//现金券列表
	public List<RestPromoData> couponList;
	//套餐列表
	public List<RestPromoData> mealComboList;

	
	//特色菜数量
	public int totalSpecialFoodNum;
	//特色菜列表  固定最多5个
	public List<RestFoodData> specialFoodList;
	
	
	//吃货荐店数量
	public int totalRecomNum;
	//吃货荐店 
	public RestRecomPicData recomData;
	
	//从这家餐厅附近查找  主菜系id: parentId  子菜系id:uuid name:名称
	public List<CommonTypeDTO> searchMenuTypeList;

    
	//所属城市id （在报错中会用到）
	public String cityId;
	//所属地域
	public String regionId;
	public String regionName;
	//所属商区
	public String districtId;
	public String districtName;
	//所属主菜单
	public String mainMenuId;
	public String mainMenuName;

	//预订折扣信息  （在分享中会用到）
	public String ydzkDetail;
	//现金券信息
	public String xjqDetail;
	//促销信息
	public String cxDetail;
	//主站链接
	public String linkUrl;
	
	//提示 可以支持标红
	public String hlHint;
	//提示url ,点击后要执行的url
	public String hlUrl;
	
	//标签列表 uuid,num,name
	public List<CommonTypeDTO> labelList;
	
	//是否能打标签
	public boolean canAddLabelTag;
	
	//分享信息
	public ShareInfoData shareInfo;
	
	
}
