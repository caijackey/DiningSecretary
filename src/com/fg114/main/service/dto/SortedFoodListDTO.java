package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 榜单列表DTO
 * @author qianjiefeng
 *
 */
public class SortedFoodListDTO  {
	//列表 
	private List<SortedFoodListData> list = new ArrayList<SortedFoodListData>();

	
	//get,set-------------------------------------------------------------------
	public List<SortedFoodListData> getList() {
		return list;
	}

	public void setList(List<SortedFoodListData> list) {
		this.list = list;
	}

	
	
	
}
