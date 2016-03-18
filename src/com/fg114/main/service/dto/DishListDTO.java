package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品列表DTO
 * @author qianjiefeng
 *
 */
public class DishListDTO  {
	//列表
	public List<DishData> list = new ArrayList<DishData>();
	//类别
	public CommonTypeDTO typeDTO;
	
	
	
	
	
	
	// -------------------------- 本地使用 
	//列表起点Index
	public int startIndex = 0;
	//列表终点Index
	public int endIndex = 0;
}
