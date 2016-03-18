package com.fg114.main.service.dto;

/**
 * 实时预订餐厅对象
 * @author qianjiefeng
 *
 */
public class RealTimeTableRestData  {
	//餐馆ID 
	String restId = "";
	//餐馆名称
	String restName = "";
	//图片url
	String picUrl = "";
	//人均
	String avgPrice = "";
	//喜欢百分比
	String likePct = "";
	//距离  
	String distanceMeter = "";
	//折扣
	String discount = "";
	//房间状态
	RoomState roomState = new RoomState();
	
	// 自用属性
	private String description = "";
	
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
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}
	public String getLikePct() {
		return likePct;
	}
	public void setLikePct(String likePct) {
		this.likePct = likePct;
	}
	public String getDistanceMeter() {
		return distanceMeter;
	}
	public void setDistanceMeter(String distanceMeter) {
		this.distanceMeter = distanceMeter;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public RoomState getRoomState() {
		return roomState;
	}
	public void setRoomState(RoomState roomState) {
		this.roomState = roomState;
	}
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
	
}
