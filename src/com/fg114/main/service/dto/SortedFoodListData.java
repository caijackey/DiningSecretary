package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


public class SortedFoodListData  {
    //首字母
	private String firstLetter = "";
	//子类列表 
	private List<SortedFoodSubListData> list = new ArrayList<SortedFoodSubListData>();
	
	
	//get,set-------------------------------------------------------------------
	public String getFirstLetter() {
		return firstLetter;
	}
	public void setFirstLetter(String firstLetter) {
		this.firstLetter = firstLetter;
	}
	public List<SortedFoodSubListData> getList() {
		return list;
	}
	public void setList(List<SortedFoodSubListData> list) {
		this.list = list;
	}


	
}
