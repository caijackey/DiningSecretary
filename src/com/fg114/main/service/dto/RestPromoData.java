package com.fg114.main.service.dto;


public class RestPromoData  {
	//图标标志  1：券  2：惠  3：币 4：币(高亮)
	public int typeTag;
	//百分比 15%  typeTag=3,4时用到
	public String pct;
	//标题
	public String title;
	//内容
	public String content;
	//现金券id
	public String couponId;
	//现金券面额   
	public String couponValue;
	//现金券价格
	public String couponUnitPrice;
	//现金券折扣 
	public String couponDiscount;
	//温馨提示
	public String couponUseHint;
	//使用开始时间
	public long couponUserBeginTime;
	//使用结束时间
	public long couponUserEndTime;
	

	
	//get,set-------------------------------------------------------------------
	
	
	
	public String getTitle() {
		return title;
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

	
	
}
