package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;



public class ResFoodCommentData {
	
	//ID 
	public String uuid;
	//评论人
	public String userName;
	//评论人头像
	public String userPicUrl;
	//评论时间
	public long createTime;
	//评论内容
	public String detail;
	//喜欢类型  1:喜欢  2:一般 3:不喜欢
	public int likeTypeTag;
	//喜欢类型名称
	public String likeTypeName;

	
}
