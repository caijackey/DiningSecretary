package com.fg114.main.service.dto;

import java.util.List;

/**
 * @author qianjiefeng
 *
 */
public class OrderList2Data  {
	//订单id
	private String orderId;
	//餐厅名
	private String restName;
	//就餐时间
	private long reserveTime;
	//是否显示就餐金额
	private boolean canShowReservePriceTag;
	//就餐金额 
	private String reservePrice;
	//订餐人手机号
	private String bookerTel;
	//状态名称
	private String statusName;
	//赢得的秘币数量  没有秘币就填 0
	private int gainCoinsNum;
	
	/* 3.1.31 */
	//状态标志 1:订单处理中   2:等待就餐  3:秘币已发放, 1,2状态下特殊颜色显示
    private int statusTag;
    
    /* 3.1.32 */
    //团宴类型名称
	private String partyTypeName;
	//就餐人数
	private int peopleNum;
	//创建时间
	private long createTime;
	
	/* 3.1.33 */
	//餐厅id
	private String restId;
	

	//get,set-------------------------------------------------------------------
	
	public String getOrderId() {
		return orderId;
	}
	public String getRestId() {
		return restId;
	}
	public void setRestId(String restId) {
		this.restId = restId;
	}
	public String getPartyTypeName() {
		return partyTypeName;
	}
	public void setPartyTypeName(String partyTypeName) {
		this.partyTypeName = partyTypeName;
	}
	public int getPeopleNum() {
		return peopleNum;
	}
	public void setPeopleNum(int peopleNum) {
		this.peopleNum = peopleNum;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getStatusTag() {
		return statusTag;
	}
	public void setStatusTag(int statusTag) {
		this.statusTag = statusTag;
	}
	public String getRestName() {
		return restName;
	}
	public void setRestName(String restName) {
		this.restName = restName;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public long getReserveTime() {
		return reserveTime;
	}
	public boolean isCanShowReservePriceTag() {
		return canShowReservePriceTag;
	}
	public void setCanShowReservePriceTag(boolean canShowReservePriceTag) {
		this.canShowReservePriceTag = canShowReservePriceTag;
	}
	public String getReservePrice() {
		return reservePrice;
	}
	public void setReservePrice(String reservePrice) {
		this.reservePrice = reservePrice;
	}
	public int getGainCoinsNum() {
		return gainCoinsNum;
	}
	public void setGainCoinsNum(int gainCoinsNum) {
		this.gainCoinsNum = gainCoinsNum;
	}
	public String getBookerTel() {
		return bookerTel;
	}
	public void setBookerTel(String bookerTel) {
		this.bookerTel = bookerTel;
	}
	public void setReserveTime(long reserveTime) {
		this.reserveTime = reserveTime;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
}
