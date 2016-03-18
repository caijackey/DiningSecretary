package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 餐馆图片对象
 * @author qianjiefeng
 *
 */
public class RestPicData  implements Serializable{
	
	//为了首页的瀑布流加入这个附加字段，暂时把RestPicData做为包装器
	public RestRecomPicData restRecomPicData;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//查看类别  约定 0:全部  1：环境  2：菜式 3：会员上传  4:其他//0:全部  1：环境  4：菜式 3：会员上传  2:其他
	int picViewTag;

	//图片ID 
	String uuid;
	//是否是环视图
	boolean circleTag;
	//缩略图url
	String smallPicUrl;
	//缩略图宽
	int smallPicWidth;
	//缩略图高
	int smallPicHeight;
	//大图url 
	String picUrl;
	//图片名称
	String name;
	//人气
	int hotNum;
	//是否是组
	boolean groupTag;
	//组中图片数量
	int groupPicNum;
	//是否有评论
	boolean haveCommentTag;
	//评论人头像url
	String commentUserPicUrl;
	//评论人名称
	String commentUserName;
	//评论内容
	String commentDetail;
	//价格
	String price;
	
	// -----------------------------------getters and setters
	public int getPicViewTag() {
		return picViewTag;
	}
	public void setPicViewTag(int picViewTag) {
		this.picViewTag = picViewTag;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public boolean isCircleTag() {
		return circleTag;
	}
	public void setCircleTag(boolean circleTag) {
		this.circleTag = circleTag;
	}
	public String getSmallPicUrl() {
		return smallPicUrl;
	}
	public void setSmallPicUrl(String smallPicUrl) {
		this.smallPicUrl = smallPicUrl;
	}
	public int getSmallPicWidth() {
		return smallPicWidth;
	}
	public void setSmallPicWidth(int smallPicWidth) {
		this.smallPicWidth = smallPicWidth;
	}
	public int getSmallPicHeight() {
		return smallPicHeight;
	}
	public void setSmallPicHeight(int smallPicHeight) {
		this.smallPicHeight = smallPicHeight;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getHotNum() {
		return hotNum;
	}
	public void setHotNum(int hotNum) {
		this.hotNum = hotNum;
	}
	public boolean isGroupTag() {
		return groupTag;
	}
	public void setGroupTag(boolean groupTag) {
		this.groupTag = groupTag;
	}
	public int getGroupPicNum() {
		return groupPicNum;
	}
	public void setGroupPicNum(int groupPicNum) {
		this.groupPicNum = groupPicNum;
	}
	public boolean isHaveCommentTag() {
		return haveCommentTag;
	}
	public void setHaveCommentTag(boolean haveCommentTag) {
		this.haveCommentTag = haveCommentTag;
	}
	public String getCommentUserPicUrl() {
		return commentUserPicUrl;
	}
	public void setCommentUserPicUrl(String commentUserPicUrl) {
		this.commentUserPicUrl = commentUserPicUrl;
	}
	public String getCommentUserName() {
		return commentUserName;
	}
	public void setCommentUserName(String commentUserName) {
		this.commentUserName = commentUserName;
	}
	public String getCommentDetail() {
		return commentDetail;
	}
	public void setCommentDetail(String commentDetail) {
		this.commentDetail = commentDetail;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	
}
