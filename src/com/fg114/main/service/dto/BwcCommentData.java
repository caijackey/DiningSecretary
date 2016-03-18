package com.fg114.main.service.dto;

import java.util.List;

/**
 * 列表页餐馆，菜品对象
 * @author qianjiefeng
 *
 */
public class BwcCommentData  {
	//评论人
	public String userName;
	//评论人头像url
	public String userPicUrl;
	//评论时间
	public long createTime;

	//评论内容
	public String detail;
	//评论图片列表   
	public List<CommentPicData> picList;  
	
}
