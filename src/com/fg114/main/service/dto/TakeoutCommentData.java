package com.fg114.main.service.dto;

import java.util.List;


/**
 * 评论数据
 * @author qianjiefeng
 *
 */
public class TakeoutCommentData  {
	
	//评论id
	public String uuid;
	//评论人
	public String userName;
	//评论人头像url
	public String userPicUrl;
	//评论时间
	public String createTime;
	
	//总体评价
	public double overallNum;
	
	//评论内容
	public String detail;
	
	//餐厅回复
	public String replyInfo;
	
	//是否显示评分
	public Boolean showOverallNumTag;
	
	
}
