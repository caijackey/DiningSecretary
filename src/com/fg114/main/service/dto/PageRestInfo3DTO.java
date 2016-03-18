package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 餐厅详情
 *
 */
public class PageRestInfo3DTO extends BaseDTO  implements Serializable{
	//餐馆信息
	public RestInfoData restInfo;
	//推荐信息
	public RestRecomInfoData3 recomInfo;
	//评论数量
	public int totalCommentNum;
	//评论列表
	public List<CommentData> commentList;
	//后台决定的显示方式
	public int showTypeTag;//1:餐厅  2：推荐  3：榜单
	
	//是否显示团购面板
	public boolean showCouponPanelTag;
	//团购面板数据
	public CouponPanelData couponPanelData;
	
}
