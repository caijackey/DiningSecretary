package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ResPromoData  {
	//标题
	private String title;
	//内容
	private String content;
	//现金券id
	private String couponId;
	//现金券面额   
	private String couponValue;
	//现金券价格
	private String couponUnitPrice;
	//现金券折扣 
	private String couponDiscount;
	//温馨提示
	private String couponUseHint;
	//使用开始时间
	private long couponUserBeginTime;
	//使用结束时间
	private long couponUserEndTime;
	//类别  1:惠   2:返  3:券  4:币  5:套餐
	private int typeTag;
	//使用说明
	private String couponUseDescription;

	
	//get,set-------------------------------------------------------------------
	
	public String getTitle() {
		return title;
	}
	public String getCouponUseDescription() {
		return couponUseDescription;
	}
	public void setCouponUseDescription(String couponUseDescription) {
		this.couponUseDescription = couponUseDescription;
	}
	public int getTypeTag() {
		return typeTag;
	}
	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResPromoData toBean(JSONObject jObj) {
		
		ResPromoData dto = new ResPromoData();

		try {
			
			if (jObj.has("title")) {
				dto.setTitle(jObj.getString("title"));
			}
			if (jObj.has("content")) {
				dto.setContent(jObj.getString("content"));
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
			if (jObj.has("couponUseHint")) {
				dto.setCouponUseHint(jObj.getString("couponUseHint"));
			}
			if (jObj.has("couponUserBeginTime")) {
				dto.setCouponUserBeginTime(jObj.getLong("couponUserBeginTime"));
			}
			if (jObj.has("couponUserEndTime")) {
				dto.setCouponUserEndTime(jObj.getLong("couponUserEndTime"));
			}
			if (jObj.has("typeTag")) {
				dto.setTypeTag(jObj.getInt("typeTag"));
			}
			if (jObj.has("couponUseDescription")) {
				dto.setCouponUseDescription(jObj.getString("couponUseDescription"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
	
}
