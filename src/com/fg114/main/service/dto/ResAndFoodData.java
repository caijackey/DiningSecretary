package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 列表页餐馆，菜品对象
 * @author qianjiefeng
 *
 */
public class ResAndFoodData  {
	//区分标志  约定 1：餐馆  2：美食
	private int tag; 
	//餐馆ID 
	private String resId;
	//菜品ID
	private String foodId;
	//名称 
	private String foodName;
	//餐馆名称
	private String resName;
	//图片url
	private String picUrl;
	//价格
	private String priceNum;
	//所属菜系信息 可以null
	private String menuTypeInfo;
	//人均
	private String avgPrice;
	//总体评价
	private double overallNum;
	//是否有七折优惠
	private boolean haveDayDayDiscountTag;
	//是否是特约商户
	private boolean specialRestTag;
	//距离  约定 -1：没gps
	private String distanceMeter;
	//人气 
	private int hotNum;
	//订单数
	private int orderCount;
	//介绍 可以null
	private String description;
	//天天七折时间段  1:有  0：无 分割符";" 从周一到周七   例子  1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;
	private String daydayDiscountTimeStr; 
	//是否可下单
	private boolean canBookingTag;
	
	// 是否今日推荐
	private boolean todayFoodTag;
	
	//3.1.9
	// 餐厅地址
	private String resAddress;
	 //高额返现
	private boolean gefxTag;
    //现金券
	private boolean xjqTag;
	//惠
	private boolean yhTag;
	
	// 3.1.35
	 //币
	private boolean mbTag;
    //套餐
	private boolean tcTag;

	//get,set-------------------------------------------------------------------
	
	public int getTag() {
		return tag;
	}
	public boolean isMbTag() {
		return mbTag;
	}
	public void setMbTag(boolean mbTag) {
		this.mbTag = mbTag;
	}
	public boolean isTcTag() {
		return tcTag;
	}
	public void setTcTag(boolean tcTag) {
		this.tcTag = tcTag;
	}
	public boolean isYhTag() {
		return yhTag;
	}
	public void setYhTag(boolean yhTag) {
		this.yhTag = yhTag;
	}
	public boolean isGefxTag() {
		return gefxTag;
	}
	public void setGefxTag(boolean gefxTag) {
		this.gefxTag = gefxTag;
	}
	public boolean isXjqTag() {
		return xjqTag;
	}
	public void setXjqTag(boolean xjqTag) {
		this.xjqTag = xjqTag;
	}
	public String getResAddress() {
		return resAddress;
	}
	public void setResAddress(String resAddress) {
		this.resAddress = resAddress;
	}
	public boolean isTodayFoodTag() {
		return todayFoodTag;
	}
	public void setTodayFoodTag(boolean todayFoodTag) {
		this.todayFoodTag = todayFoodTag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	public int getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}
	public String getDaydayDiscountTimeStr() {
		return daydayDiscountTimeStr;
	}
	public void setDaydayDiscountTimeStr(String daydayDiscountTimeStr) {
		this.daydayDiscountTimeStr = daydayDiscountTimeStr;
	}
	public boolean isSpecialRestTag() {
		return specialRestTag;
	}
	public void setSpecialRestTag(boolean specialRestTag) {
		this.specialRestTag = specialRestTag;
	}
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getFoodId() {
		return foodId;
	}
	public void setFoodId(String foodId) {
		this.foodId = foodId;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public String getResName() {
		return resName;
	}
	public void setResName(String resName) {
		this.resName = resName;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getPriceNum() {
		return priceNum;
	}
	public void setPriceNum(String priceNum) {
		this.priceNum = priceNum;
	}
	public String getMenuTypeInfo() {
		return menuTypeInfo;
	}
	public void setMenuTypeInfo(String menuTypeInfo) {
		this.menuTypeInfo = menuTypeInfo;
	}
	public String getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}
	public double getOverallNum() {
		return overallNum;
	}
	public void setOverallNum(double overallNum) {
		this.overallNum = overallNum;
	}
	public boolean isHaveDayDayDiscountTag() {
		return haveDayDayDiscountTag;
	}
	public void setHaveDayDayDiscountTag(boolean haveDayDayDiscountTag) {
		this.haveDayDayDiscountTag = haveDayDayDiscountTag;
	}
	public String getDistanceMeter() {
		return distanceMeter;
	}
	public void setDistanceMeter(String distanceMeter) {
		this.distanceMeter = distanceMeter;
	}
	public int getHotNum() {
		return hotNum;
	}
	public void setHotNum(int hotNum) {
		this.hotNum = hotNum;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isCanBookingTag() {
		return canBookingTag;
	}
	public void setCanBookingTag(boolean canBookingTag) {
		this.canBookingTag = canBookingTag;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResAndFoodData toBean(JSONObject jObj) {
		
		ResAndFoodData data = new ResAndFoodData();
		
		try {
			
			if (jObj.has("tag")) {
				data.setTag(jObj.getInt("tag"));
			}
			if (jObj.has("resId")) {
				data.setResId(jObj.getString("resId"));
			}
			if (jObj.has("foodId")) {
				data.setFoodId(jObj.getString("foodId"));
			}
			if (jObj.has("foodName")) {
				data.setFoodName(jObj.getString("foodName"));
			}
			if (jObj.has("resName")) {
				data.setResName(jObj.getString("resName"));
			}
			if (jObj.has("picUrl")) {
				data.setPicUrl(jObj.getString("picUrl"));
			}
			if (jObj.has("priceNum")) {
				data.setPriceNum(jObj.getString("priceNum"));
			}
			if (jObj.has("menuTypeInfo")) {
				data.setMenuTypeInfo(jObj.getString("menuTypeInfo"));
			}
			if (jObj.has("avgPrice")) {
				data.setAvgPrice(jObj.getString("avgPrice"));
			}
			if (jObj.has("overallNum")) {
				data.setOverallNum(jObj.getDouble("overallNum"));
			}
			if (jObj.has("haveDayDayDiscountTag")) {
				data.setHaveDayDayDiscountTag(jObj.getBoolean("haveDayDayDiscountTag"));
			}
			if (jObj.has("specialRestTag")) {
				data.setSpecialRestTag(jObj.getBoolean("specialRestTag"));
			}
			if (jObj.has("distanceMeter")) {
				data.setDistanceMeter(jObj.getString("distanceMeter"));
			}
			if (jObj.has("hotNum")) {
				data.setHotNum(jObj.getInt("hotNum"));
			}
			if (jObj.has("description")) {
				data.setDescription(jObj.getString("description"));
			}
			if (jObj.has("orderCount")) {
				data.setOrderCount(jObj.getInt("orderCount"));
			}
			if (jObj.has("daydayDiscountTimeStr")) {
				data.setDaydayDiscountTimeStr(jObj.getString("daydayDiscountTimeStr"));
			}
			if (jObj.has("canBookingTag")) {
				data.setCanBookingTag(jObj.getBoolean("canBookingTag"));
			}
			if (jObj.has("todayFoodTag")) {
				data.setTodayFoodTag(jObj.getBoolean("todayFoodTag"));
			}
			if (jObj.has("resAddress")) {
				data.setResAddress(jObj.getString("resAddress"));
			}
			if (jObj.has("gefxTag")) {
				data.setGefxTag(jObj.getBoolean("gefxTag"));
			}
			if (jObj.has("xjqTag")) {
				data.setXjqTag(jObj.getBoolean("xjqTag"));
			}
			if (jObj.has("yhTag")) {
				data.setYhTag(jObj.getBoolean("yhTag"));
			}
			if (jObj.has("mbTag")) {
				data.setMbTag(jObj.getBoolean("mbTag"));
			}
			if (jObj.has("tcTag")) {
				data.setTcTag(jObj.getBoolean("tcTag"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return data;
	}
	
	/**
	 * 复制
	 */
	public ResAndFoodData clone(boolean isNeedDistance) {
		ResAndFoodData newData = new ResAndFoodData();
		newData.setTag(this.getTag());
		newData.setResId(this.getResId());
		newData.setFoodId(this.getFoodId());
		newData.setFoodName(this.getFoodName());
		newData.setResName(this.getResName());
		newData.setPicUrl(this.getPicUrl());
		newData.setPriceNum(this.getPriceNum());
		newData.setMenuTypeInfo(this.getMenuTypeInfo());
		newData.setAvgPrice(this.getAvgPrice());
		newData.setOverallNum(this.getOverallNum());
		newData.setHaveDayDayDiscountTag(this.isHaveDayDayDiscountTag());
		newData.setSpecialRestTag(this.isSpecialRestTag());
		if (isNeedDistance) {
			newData.setDistanceMeter(this.getDistanceMeter());
		} else {
			newData.setDistanceMeter("");
		}
		newData.setHotNum(this.getHotNum());
		newData.setDescription(this.getDescription());
		newData.setOrderCount(this.getOrderCount());
		newData.setDaydayDiscountTimeStr(this.getDaydayDiscountTimeStr());
		newData.setCanBookingTag(this.isCanBookingTag());
		newData.setTodayFoodTag(this.isTodayFoodTag());
		newData.setResAddress(this.getResAddress());
		newData.setGefxTag(this.isGefxTag());
		newData.setXjqTag(this.isXjqTag());
		newData.setYhTag(this.isYhTag());
		newData.setMbTag(isMbTag());
		newData.setTcTag(isTcTag());
		return newData;
	}
}
