package com.fg114.main.service.dto;
/**
 * 功能按钮
 *
 */
public class OrderFuncBtnData {
	//按钮类别   1：添加到日历  2:收藏餐厅 3：分享         100以上：后台自定义
	public int typeTag;
	
	//图标url 40*40
	public String iconUrl;
	//名称    标红
	public String name;
	//动作url
	public String actionXmsUrl;
	
	//按钮是否需要闪烁
	public boolean needFlashTag;
	//是否可用
	public boolean enableTag;
	
	// 传小票提示
	public String uploadReceiptHint;
	
	//分享信息
	public ShareInfoData shareInfoData;
}
