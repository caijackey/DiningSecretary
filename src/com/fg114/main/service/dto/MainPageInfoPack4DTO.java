package com.fg114.main.service.dto;

public class MainPageInfoPack4DTO {
	//首页消息
	public MainPageMsgListDTO mainPageMsgListDTO;
			
	//是否是餐馆推荐样式
	public boolean recomStyleTag;
	//精选餐馆推荐列表
	public RestRecomListDTO topRestRecomListDTO;
	//餐馆推荐列表
	public RestRecomListDTO restRecomListDTO;
	//餐馆列表  selectRegionName  selectMainMenuTypeName  selectSortName 分别为三个按钮的名称
	public RestListDTO restListDTO;
	
	//找餐厅按钮功能 是否是附近搜索
	public boolean findRestBtnToNearTag;


	//下次访问时间间隔  秒
	public int nextQueryInterval;
}
