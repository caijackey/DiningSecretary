package com.fg114.main.service.dto;

import java.io.Serializable;


public class RestRecomPicData implements Serializable {

	//推荐id
	public String uuid;
	//餐厅名称
	public String restName;
	//图片url
	public String picUrl;
	//图片大图
	public String bigPicUrl;
	//缩略图宽
	public int picWidth;
	//缩略图高
	public int picHeight;
	//标题
	public String title;
	//内容
	public String detail;
	//用户名称
	public String userNickName;
	//创建时间
	public String createTime;
	//收藏数量
	public int favNum;
	//点击数
	public int hitNum;
	//餐厅id
	public String restId;
	//所在商区
	public String district;
	
	//团购角标 100*100
	public String couponIconUrl;
}
