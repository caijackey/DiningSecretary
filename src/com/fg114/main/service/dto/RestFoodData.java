package com.fg114.main.service.dto;

/**
 * 菜品对象
 * @author qianjiefeng
 *
 */
public class RestFoodData  {
	//特色菜ID 
	public String uuid;
	//特色菜名称
	public String name;
	//价格
	public String price;
	//人气 
	public int hotNum;
	//介绍   可以为null
	public String intro;
	//单位
	public String unit;
	//图片url
	public String picUrl;
	//菜品大图片
	public String picOriginalUrl;
    //小菜系id
	public String smallStyleId;
    //小菜系名称
	public String smallStyleName;
	
	//评论总数
	public int totalCommentNum;
    //评论(最新的一条评论)
	public ResFoodCommentData commentData;


	//get,set-------------------------------------------------------------------
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
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public int getHotNum() {
		return hotNum;
	}
	public void setHotNum(int hotNum) {
		this.hotNum = hotNum;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getPicOriginalUrl() {
		return picOriginalUrl;
	}
	public void setPicOriginalUrl(String picOriginalUrl) {
		this.picOriginalUrl = picOriginalUrl;
	}
	public String getSmallStyleId() {
		return smallStyleId;
	}
	public void setSmallStyleId(String smallStyleId) {
		this.smallStyleId = smallStyleId;
	}
	public String getSmallStyleName() {
		return smallStyleName;
	}
	public void setSmallStyleName(String smallStyleName) {
		this.smallStyleName = smallStyleName;
	}
	public int getTotalCommentNum() {
		return totalCommentNum;
	}
	public void setTotalCommentNum(int totalCommentNum) {
		this.totalCommentNum = totalCommentNum;
	}
	public ResFoodCommentData getCommentData() {
		return commentData;
	}
	public void setCommentData(ResFoodCommentData commentData) {
		this.commentData = commentData;
	}

	
	
}
