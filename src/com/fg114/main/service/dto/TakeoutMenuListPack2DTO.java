package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####外卖菜单####
 *
 */
public class TakeoutMenuListPack2DTO {
	//外卖餐厅id
	public String takeoutId;
	//外卖餐厅名称
	public String takeoutName;
	//广告位 
	public List<MainPageAdvData> advList;
	//列表
	public List<TakeoutMenuList2DTO> list;
	//起送价格
	public double sendLimitPrice;
    //是否可以下单
	public boolean canOrderTag;
    //不能下单的提示
	public String hintForCanNotOrder;
}
