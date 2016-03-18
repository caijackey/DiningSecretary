package com.fg114.main.service.dto;

/**
 * @author qianjiefeng
 *
 */
public class MainPageInfoPackDTO  {
	//气泡信息
	public BubbleHintData bubbleHintData=new BubbleHintData();
	
	//首页消息
	public MainPageMsgListDTO mainPageMsgListDTO=new MainPageMsgListDTO();
	
	//订单提示信息
	public OrderHintPackData orderHintPackData=new OrderHintPackData();
	
	//精选餐馆推荐列表
	public RestRecomListDTO topRestRecomListDTO=new RestRecomListDTO();
	//餐馆推荐列表
	public RestRecomListDTO restRecomListDTO=new RestRecomListDTO();
	//餐厅推荐的提示
	public RestRecomAddHintData restRecomAddHintData=new RestRecomAddHintData();
	
	//下次访问时间间隔  秒
	public int nextQueryInterval;
	
	//是否是餐馆推荐样式
	public boolean recomStyleTag;
	
	//餐馆列表  selectRegionName  selectMainMenuTypeName  selectSortName 分别为三个按钮的名称
	public RestListDTO restListDTO;
	
	//找餐厅按钮功能 是否是附近搜索
	public boolean findRestBtnToNearTag;
	

}



