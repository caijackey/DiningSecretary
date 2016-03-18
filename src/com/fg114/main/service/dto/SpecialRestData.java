package com.fg114.main.service.dto;

import java.io.Serializable;

import org.json.JSONObject;

import com.fg114.main.util.ActivityUtil;

public class SpecialRestData  implements Serializable{
	private static final long serialVersionUID = 6197581927859592940L;
	
	//餐馆id
	private String restId;
	//餐馆名称
	private String restName;
	//餐馆 图片url 
	private String restPicUrl;
	//现金券id
	private String couponId;
	//现金券面额   
	private String couponValue;
	//现金券价格
	private String couponUnitPrice;
	//现金券折扣 
	private String couponDiscount;
	//使用说明
	private String couponUseDescription;
	//温馨提示
	private String couponUseHint;
	//使用开始时间
	private long couponUserBeginTime;
	//使用结束时间
	private long couponUserEndTime;
	//高额返现地域
	private String gefxRegion;
	//高额返现描述
	private String gefxDescription;
	//最后一条数据提示信息
	private String detail;


	//get,set-------------------------------------------------------------------
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getRestId() {
		return restId;
	}
	public void setRestId(String restId) {
		this.restId = restId;
	}
	public String getRestName() {
		return restName;
	}
	public void setRestName(String restName) {
		this.restName = restName;
	}
	public String getRestPicUrl() {
		return restPicUrl;
	}
	public void setRestPicUrl(String restPicUrl) {
		this.restPicUrl = restPicUrl;
	}
	public String getCouponId() {
		return couponId;
	}
	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}
	public String getCouponValue() {
		return couponValue;
	}
	public void setCouponValue(String couponValue) {
		this.couponValue = couponValue;
	}
	public String getCouponUnitPrice() {
		return couponUnitPrice;
	}
	public void setCouponUnitPrice(String couponUnitPrice) {
		this.couponUnitPrice = couponUnitPrice;
	}
	public String getCouponDiscount() {
		return couponDiscount;
	}
	public void setCouponDiscount(String couponDiscount) {
		this.couponDiscount = couponDiscount;
	}
	public String getCouponUseDescription() {
		return couponUseDescription;
	}
	public void setCouponUseDescription(String couponUseDescription) {
		this.couponUseDescription = couponUseDescription;
	}
	public String getCouponUseHint() {
		return couponUseHint;
	}
	public void setCouponUseHint(String couponUseHint) {
		this.couponUseHint = couponUseHint;
	}
	public long getCouponUserBeginTime() {
		return couponUserBeginTime;
	}
	public void setCouponUserBeginTime(long couponUserBeginTime) {
		this.couponUserBeginTime = couponUserBeginTime;
	}
	public long getCouponUserEndTime() {
		return couponUserEndTime;
	}
	public void setCouponUserEndTime(long couponUserEndTime) {
		this.couponUserEndTime = couponUserEndTime;
	}
	public String getGefxRegion() {
		return gefxRegion;
	}
	public void setGefxRegion(String gefxRegion) {
		this.gefxRegion = gefxRegion;
	}
	public String getGefxDescription() {
		return gefxDescription;
	}
	public void setGefxDescription(String gefxDescription) {
		this.gefxDescription = gefxDescription;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static SpecialRestData toBean(JSONObject jObj) {
		
		SpecialRestData dto = new SpecialRestData();

		try {
			 
			if (jObj.has("restId")) {
				dto.setRestId(jObj.getString("restId"));
			}
			if (jObj.has("restName")) {
				dto.setRestName(jObj.getString("restName"));
			}
			if (jObj.has("restPicUrl")) {
				dto.setRestPicUrl(jObj.getString("restPicUrl"));
			}			
			if (jObj.has("couponId")) {
				dto.setCouponId(jObj.getString("couponId"));
			}
			if (jObj.has("couponValue")) {
				dto.setCouponValue(jObj.getString("couponValue"));
			}
			if (jObj.has("couponUnitPrice")) {
				dto.setCouponUnitPrice(jObj.getString("couponUnitPrice"));
			}
			if (jObj.has("couponDiscount")) {
				dto.setCouponDiscount(jObj.getString("couponDiscount"));
			}
			if (jObj.has("couponUseDescription")) {
				dto.setCouponUseDescription(jObj.getString("couponUseDescription"));
			}
			if (jObj.has("couponUseHint")) {
				dto.setCouponUseHint(jObj.getString("couponUseHint"));
			}
			if (jObj.has("gefxRegion")) {
				dto.setGefxRegion(jObj.getString("gefxRegion"));
			}
			if (jObj.has("gefxDescription")) {
				dto.setGefxDescription(jObj.getString("gefxDescription"));
			}
			if (jObj.has("detail")) {
				dto.setDetail(jObj.getString("detail"));
			}
			if (jObj.has("couponUserBeginTime")) {
				dto.setCouponUserBeginTime(jObj.getLong("couponUserBeginTime"));
			}
			if (jObj.has("couponUserEndTime")) {
				dto.setCouponUserEndTime(jObj.getLong("couponUserEndTime"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			//保存错误信息
			ActivityUtil.saveException(e, "SpecialRestData.toBean Exception! JSONObject= "+(jObj==null? "null":jObj.toString()));
			return null;
		}
		return dto;
	}
}
