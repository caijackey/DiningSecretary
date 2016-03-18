package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 餐馆图片对象
 * @author qianjiefeng
 *
 */
public class ResPicData2 implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//图片ID 
	private String uuid;
	//缩略图url
	private String smallPicUrl;
	//缩略图宽
	private int smallPicWidth;
	//缩略图高
	private int smallPicHeight;
	//大图url 
	private String picUrl;
	//图片名称
	private String name;
	//人气
	private int hotNum;
	//是否是组
	private boolean groupTag;
	//组中图片数量
	private int groupPicNum;
	//是否有评论
	private boolean haveCommentTag;
	//评论人头像url
	private String commentUserPicUrl;
	//评论人名称
	private String commentUserName;
	//评论内容
	private String commentDetail;
	//是否是环视图
	private boolean circleTag;
	
	
	//get,set-------------------------------------------------------------------
	
	public String getUuid() {
		return uuid;
	}
	public boolean isCircleTag() {
		return circleTag;
	}
	public void setCircleTag(boolean circleTag) {
		this.circleTag = circleTag;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	
	
	
}
