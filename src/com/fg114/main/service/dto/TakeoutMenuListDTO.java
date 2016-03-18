package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class TakeoutMenuListDTO  {
	
	//列表
	public List<TakeoutMenuData2> list = new ArrayList<TakeoutMenuData2>();//4.1.6版本以后使用
//	public List<TakeoutMenuData> list = new ArrayList<TakeoutMenuData>();
	//类别   uuid :类别id  name:类别名称  memo:类别描述  （用于显示在右侧列表的分类标题栏上）
	public CommonTypeDTO typeDTO;
	
	// -------------------------- 本地使用 
	//列表起点Index
	public int startIndex = 0;
	//列表终点Index
	public int endIndex = 0;
}
