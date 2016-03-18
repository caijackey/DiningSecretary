package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.app.data.BaseInfo;

/**
 * 餐馆详情对象
 * 
 * @author qianjiefeng 第２版
 */
public class ResInfo2Data extends BaseInfo implements Serializable{
	// 餐馆id
	private String uuid;
	// 餐馆名称
	private String name;
	//营业时间
	private String openTimeInfo="-";
	// 是否可下单
	private boolean canBookingTag;
	//餐馆电话
	private String telForBooking = "";
	// 餐馆 图片url
	private String resPicUrl;
	// 是否有7折优惠
	private boolean haveDayDayDiscountTag;
	// 总体评价
	private double overallNum;
	// 口味
	private double tasteNum;
	// 环境
	private double envNum;
	// 服务
	private double serviceNum;
	// 近期订单数
	private int recentBillNum;
	// 人均消费
	private String avgPrice;
	// 网友印象 可以为null
	private String friendImpress;
	// 菜系 可以为null
	private String menuTypeInfo;
	// 地址 可以为null
	private String address;
	// 交通路线 可以为null
	private String trafficLine;
	// 公交信息 可以为null
	private String busInfo;
	// 消费方式 可以为null
	private String consumeType;
	// 停车地图url 可以为null
	private String parkingPicUrl;
	// 停车说明 可以为null
	private String parkingIntro;
	// 特色菜列表 固定最多5个
	private List<ResFoodData> specialFoodList = new ArrayList<ResFoodData>();
	// 特色菜数量
	private int totalSpecialFoodNum;
	// 经度
	private double longitude;
	// 纬度
	private double latitude;
	// 预订折扣信息 可以为null
	private String ydzkDetail;
	// 现金券信息 可以为null
	private String xjqDetail;
	// 促销信息 可以为null
	private String cxDetail;
	// 主站链接
	private String linkUrl;

	// tel 餐厅电话
	private String tel;

	// listTelInfo 餐厅电话的列表，区号，分机号等是分开的。
	private List<ResTelInfo> lstTelInfo = new ArrayList<ResTelInfo>();

	// //是否有折扣
	// private boolean haveDiscountTag;
	// 折扣信息
	private String discountName;
	// //是否有积分
	// private boolean havePointTag;

	// 所属城市id
	private String cityId;
	// 所属地域
	private String regionId;
	private String regionName;
	// 所属商区
	private String districtId;
	private String districtName;
	// 所属商主菜单
	private String mainMenuId;
	private String mainMenuName;

	// 是否可以进入新的点餐页面
	private boolean canDishTag;

	// 促销列表
	private List<ResPromoData> promoList = new ArrayList<ResPromoData>();
	// 现金券列表
	private List<ResPromoData> couponList = new ArrayList<ResPromoData>();
	
	//v3.1.34
	//折扣信息(在线预订)
	private String onlineDiscountName = "";
	
	//v3.1.35
	//套餐列表
	private List<ResPromoData> mealComboList = new ArrayList<ResPromoData>();
	
	//指示当前的数据是否超时，供缓存逻辑使用，默认值true为了兼容以前的程序
	public boolean isExpired=true;
	
	//营业时间
	public String getOpenTimeInfo() {
		return openTimeInfo;
	}
	public void setOpenTimeInfo(String openTimeInfo) {
		this.openTimeInfo = openTimeInfo;
	}
	// get,set-------------------------------------------------------------------
	
	public String getId() {
		return uuid;
	}
	public List<ResPromoData> getMealComboList() {
		return mealComboList;
	}
	public void setMealComboList(List<ResPromoData> mealComboList) {
		this.mealComboList = mealComboList;
	}
	public void setId(String id) {
		this.uuid = id;
	}
	public String getTel() {
		return tel;
	}

	public boolean isCanDishTag() {
		return canDishTag;
	}

	public void setCanDishTag(boolean canDishTag) {
		this.canDishTag = canDishTag;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getDistrictId() {
		return districtId;
	}

	public void setDistrictId(String districtId) {
		this.districtId = districtId;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	public String getMainMenuId() {
		return mainMenuId;
	}

	public void setMainMenuId(String mainMenuId) {
		this.mainMenuId = mainMenuId;
	}

	public String getMainMenuName() {
		return mainMenuName;
	}

	public void setMainMenuName(String mainMenuName) {
		this.mainMenuName = mainMenuName;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getDiscountName() {
		return discountName;
	}

	public void setDiscountName(String discountName) {
		this.discountName = discountName;
	}

	public String getUuid() {
		return uuid;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public String getResLink() {
		return linkUrl;
	}
	public void setResLink(String resLink) {
		this.linkUrl = resLink;
	}
	public String getBusInfo() {
		return busInfo;
	}

	public void setBusInfo(String busInfo) {
		this.busInfo = busInfo;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getYdzkDetail() {
		return ydzkDetail;
	}

	public void setYdzkDetail(String ydzkDetail) {
		this.ydzkDetail = ydzkDetail;
	}

	public String getXjqDetail() {
		return xjqDetail;
	}

	public void setXjqDetail(String xjqDetail) {
		this.xjqDetail = xjqDetail;
	}

	public String getCxDetail() {
		return cxDetail;
	}

	public void setCxDetail(String cxDetail) {
		this.cxDetail = cxDetail;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public int getTotalSpecialFoodNum() {
		return totalSpecialFoodNum;
	}

	public void setTotalSpecialFoodNum(int totalSpecialFoodNum) {
		this.totalSpecialFoodNum = totalSpecialFoodNum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResPicUrl() {
		return resPicUrl;
	}

	public void setResPicUrl(String resPicUrl) {
		this.resPicUrl = resPicUrl;
	}

	public boolean isHaveDayDayDiscountTag() {
		return haveDayDayDiscountTag;
	}

	public void setHaveDayDayDiscountTag(boolean haveDayDayDiscountTag) {
		this.haveDayDayDiscountTag = haveDayDayDiscountTag;
	}

	public double getOverallNum() {
		return overallNum;
	}

	public void setOverallNum(double overallNum) {
		this.overallNum = overallNum;
	}

	public double getTasteNum() {
		return tasteNum;
	}

	public void setTasteNum(double tasteNum) {
		this.tasteNum = tasteNum;
	}

	public double getEnvNum() {
		return envNum;
	}

	public void setEnvNum(double envNum) {
		this.envNum = envNum;
	}

	public double getServiceNum() {
		return serviceNum;
	}

	public void setServiceNum(double serviceNum) {
		this.serviceNum = serviceNum;
	}

	public int getRecentBillNum() {
		return recentBillNum;
	}

	public void setRecentBillNum(int recentBillNum) {
		this.recentBillNum = recentBillNum;
	}

	public String getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}

	public String getFriendImpress() {
		return friendImpress;
	}

	public void setFriendImpress(String friendImpress) {
		this.friendImpress = friendImpress;
	}

	public String getMenuTypeInfo() {
		return menuTypeInfo;
	}

	public void setMenuTypeInfo(String menuTypeInfo) {
		this.menuTypeInfo = menuTypeInfo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTrafficLine() {
		return trafficLine;
	}

	public void setTrafficLine(String trafficLine) {
		this.trafficLine = trafficLine;
	}

	public String getConsumeType() {
		return consumeType;
	}

	public void setConsumeType(String consumeType) {
		this.consumeType = consumeType;
	}

	public String getParkingPicUrl() {
		return parkingPicUrl;
	}

	public void setParkingPicUrl(String parkingPicUrl) {
		this.parkingPicUrl = parkingPicUrl;
	}

	public String getParkingIntro() {
		return parkingIntro;
	}

	public void setParkingIntro(String parkingIntro) {
		this.parkingIntro = parkingIntro;
	}

	public boolean isCanBookingTag() {
		return canBookingTag;
	}

	public void setCanBookingTag(boolean canBookingTag) {
		this.canBookingTag = canBookingTag;
	}

	public List<ResFoodData> getSpecialFoodList() {
		return specialFoodList;
	}

	public void setSpecialFoodList(List<ResFoodData> specialFoodList) {
		this.specialFoodList = specialFoodList;
	}

	public List<ResTelInfo> getListTelInfo() {
		return lstTelInfo;
	}

	public void setListTelInfo(List<ResTelInfo> lstTelInfo) {
		this.lstTelInfo = lstTelInfo;
	}
	//----
	public List<ResPromoData> getPromoList() {
		return promoList;
	}
	public void setPromoList(List<ResPromoData> promoList) {
		this.promoList = promoList;
	}
	public List<ResPromoData> getCouponList() {
		return couponList;
	}
	public void setCouponList(List<ResPromoData> couponList) {
		this.couponList = couponList;
	}

	public String getOnlineDiscountName() {
		return onlineDiscountName;
	}
	public void setOnlineDiscountName(String onlineDiscountName) {
		this.onlineDiscountName = onlineDiscountName;
	}
	
	public String getTelForBooking() {
		return telForBooking;
	}
	public void setTelForBooking(String telForBooking) {
		this.telForBooking = telForBooking;
	}
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static ResInfo2Data toBean(JSONObject jObj) {

		ResInfo2Data dto = new ResInfo2Data();

		try {

			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("openTimeInfo")) {
				dto.setOpenTimeInfo(jObj.getString("openTimeInfo"));
			}
			if (jObj.has("canBookingTag")) {
				dto.setCanBookingTag(jObj.getBoolean("canBookingTag"));
			}
			if (jObj.has("resPicUrl")) {
				dto.setResPicUrl(jObj.getString("resPicUrl"));
			}
			if (jObj.has("haveDayDayDiscountTag")) {
				dto.setHaveDayDayDiscountTag(jObj.getBoolean("haveDayDayDiscountTag"));
			}
			if (jObj.has("overallNum")) {
				dto.setOverallNum(jObj.getDouble("overallNum"));
			}
			if (jObj.has("tasteNum")) {
				dto.setTasteNum(jObj.getDouble("tasteNum"));
			}
			if (jObj.has("envNum")) {
				dto.setEnvNum(jObj.getDouble("envNum"));
			}
			if (jObj.has("serviceNum")) {
				dto.setServiceNum(jObj.getDouble("serviceNum"));
			}
			if (jObj.has("recentBillNum")) {
				dto.setRecentBillNum(jObj.getInt("recentBillNum"));
			}
			if (jObj.has("avgPrice")) {
				dto.setAvgPrice(jObj.getString("avgPrice"));
			}
			if (jObj.has("friendImpress")) {
				dto.setFriendImpress(jObj.getString("friendImpress"));
			}
			if (jObj.has("menuTypeInfo")) {
				dto.setMenuTypeInfo(jObj.getString("menuTypeInfo"));
			}
			if (jObj.has("address")) {
				dto.setAddress(jObj.getString("address"));
			}
			if (jObj.has("trafficLine")) {
				dto.setTrafficLine(jObj.getString("trafficLine"));
			}
			if (jObj.has("busInfo")) {
				dto.setBusInfo(jObj.getString("busInfo"));
			}
			if (jObj.has("consumeType")) {
				dto.setConsumeType(jObj.getString("consumeType"));
			}
			if (jObj.has("parkingPicUrl")) {
				dto.setParkingPicUrl(jObj.getString("parkingPicUrl"));
			}
			if (jObj.has("parkingIntro")) {
				dto.setParkingIntro(jObj.getString("parkingIntro"));
			}
			if (jObj.has("specialFoodList")) {
				List<ResFoodData> specialFoodList = new ArrayList<ResFoodData>();
				if (!jObj.isNull("specialFoodList")) {
					JSONArray jsonArray = jObj.getJSONArray("specialFoodList");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i++) {
							specialFoodList.add(ResFoodData.toBean(jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setSpecialFoodList(specialFoodList);
			}
			if (jObj.has("totalSpecialFoodNum")) {
				dto.setTotalSpecialFoodNum(jObj.getInt("totalSpecialFoodNum"));
			}
			if (jObj.has("longitude")) {
				dto.setLongitude(jObj.getDouble("longitude"));
			}
			if (jObj.has("latitude")) {
				dto.setLatitude(jObj.getDouble("latitude"));
			}
			if (jObj.has("ydzkDetail")) {
				dto.setYdzkDetail(jObj.getString("ydzkDetail"));
			}
			if (jObj.has("xjqDetail")) {
				dto.setXjqDetail(jObj.getString("xjqDetail"));
			}
			if (jObj.has("cxDetail")) {
				dto.setCxDetail(jObj.getString("cxDetail"));
			}
			if (jObj.has("linkUrl")) {
				dto.setLinkUrl(jObj.getString("linkUrl"));
			}
			if (jObj.has("tel")) {
				dto.setTel(jObj.getString("tel"));
			}
			if (jObj.has("discountName")) {
				dto.setDiscountName(jObj.getString("discountName"));
				if (dto.getDiscountName().equals("null")) {
					dto.setDiscountName("");
				}
			}
			if (jObj.has("cityId")) {
				dto.setCityId(jObj.getString("cityId"));
			}
			if (jObj.has("regionId")) {
				dto.setRegionId(jObj.getString("regionId"));
			}
			if (jObj.has("regionName")) {
				dto.setRegionName(jObj.getString("regionName"));
			}
			if (jObj.has("districtId")) {
				dto.setDistrictId(jObj.getString("districtId"));
			}
			if (jObj.has("districtName")) {
				dto.setDistrictName(jObj.getString("districtName"));
			}
			if (jObj.has("mainMenuId")) {
				dto.setMainMenuId(jObj.getString("mainMenuId"));
			}
			if (jObj.has("mainMenuName")) {
				dto.setMainMenuName(jObj.getString("mainMenuName"));
			}
			if (jObj.has("canDishTag")) {
				dto.setCanDishTag(jObj.getBoolean("canDishTag"));
			}
			if (jObj.has("lstTelInfo") && !jObj.isNull("lstTelInfo")) {
				List<ResTelInfo> telInfo = new ArrayList<ResTelInfo>();
				JSONArray jsonArray = jObj.getJSONArray("lstTelInfo");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						telInfo.add(ResTelInfo.toBean(jsonArray.getJSONObject(i)));
					}
				}
				dto.setListTelInfo(telInfo);
			}
			//---
			if (jObj.has("couponList") && !jObj.isNull("couponList")) {
				List<ResPromoData> couponList = new ArrayList<ResPromoData>();
				JSONArray jsonArray = jObj.getJSONArray("couponList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						couponList.add(ResPromoData.toBean(jsonArray.getJSONObject(i)));
					}
				}
				dto.setCouponList(couponList);
			}
			if (jObj.has("promoList") && !jObj.isNull("promoList")) {
				List<ResPromoData> promoList = new ArrayList<ResPromoData>();
				JSONArray jsonArray = jObj.getJSONArray("promoList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						promoList.add(ResPromoData.toBean(jsonArray.getJSONObject(i)));
					}
				}
				dto.setPromoList(promoList);
			}
			if (jObj.has("onlineDiscountName")) {
				dto.setOnlineDiscountName(jObj.getString("onlineDiscountName"));
			}
			if (jObj.has("mealComboList") && !jObj.isNull("mealComboList")) {
				List<ResPromoData> mealComboList = new ArrayList<ResPromoData>();
				JSONArray jsonArray = jObj.getJSONArray("mealComboList");
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						mealComboList.add(ResPromoData.toBean(jsonArray.getJSONObject(i)));
					}
				}
				dto.setMealComboList(mealComboList);
			}
			if (jObj.has("telForBooking")) {
				dto.setTelForBooking(jObj.getString("telForBooking"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}

}
