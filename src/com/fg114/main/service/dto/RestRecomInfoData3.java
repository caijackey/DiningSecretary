package com.fg114.main.service.dto;

import java.util.List;

/**
 *推荐信息
 *
 */
public class RestRecomInfoData3 {
	//uuid
	public String uuid;
	
	//分享信息
	public ShareInfoData shareInfo;
	//是否已收藏
	public boolean favTag;
	
	//标题
	public String title;
	//创建时间
	public String createTime;
	
	//餐厅id
	public String restId;
	//餐厅名称
	public String restName;
	
	//用户id
	public String userId;
	//用户名称
	public String userNickName;
	//用户头像
	public String userPicUrl;
	//用户是否是vip
	public boolean userIsVipTag;
	
	//图片列表
	public List<RestRecomPicData> picList;
	
	//喜欢数量
	public int totalLikeNum;
	//是否已经喜欢过
	public boolean likedTag;
	
	//我要挑战提示
	public String tryRecomHint;
	
	//相关推荐列表
	public List<RestRecomPicData> relateRecomList;
}
