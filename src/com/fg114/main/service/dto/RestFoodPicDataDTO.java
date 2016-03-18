package com.fg114.main.service.dto;

import java.util.List;



public class RestFoodPicDataDTO extends BaseDTO {
	
	//菜品id
	String uuid;
	//菜品名称
	String name;
	//餐厅id
	String restId;
	//餐厅名称
	String restName;
	//好评数量
	int goodNum;
	//中评数量
	int normalNum;
	//差评数量
	int badNum;
	//价格
	String price;
	//单位
	String unit;
	//图片列表
	List<RestGroupPicData> picList;
	//评论列表 
	List<ResFoodCommentData> commentList;
	
	// getters and setters 
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public int getGoodNum() {
		return goodNum;
	}
	public void setGoodNum(int goodNum) {
		this.goodNum = goodNum;
	}
	public int getNormalNum() {
		return normalNum;
	}
	public void setNormalNum(int normalNum) {
		this.normalNum = normalNum;
	}
	public int getBadNum() {
		return badNum;
	}
	public void setBadNum(int badNum) {
		this.badNum = badNum;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public List<RestGroupPicData> getPicList() {
		return picList;
	}
	public void setPicList(List<RestGroupPicData> picList) {
		this.picList = picList;
	}
	public List<ResFoodCommentData> getCommentList() {
		return commentList;
	}
	public void setCommentList(List<ResFoodCommentData> commentList) {
		this.commentList = commentList;
	}
	
	
}
