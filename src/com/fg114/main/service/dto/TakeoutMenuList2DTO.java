package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####外卖菜单列表####
 *
 */
public class TakeoutMenuList2DTO {
	//类别
	public TakeoutMenuTypeData typeData;
	//列表
	public List<TakeoutMenuData2> list;
	
	// -------------------------- 本地使用 
	//列表起点Index
	public int startIndex = 0;
	//列表终点Index
	public int endIndex = 0;
}
