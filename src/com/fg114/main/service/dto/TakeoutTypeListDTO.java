package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


public class TakeoutTypeListDTO extends BaseDTO {
	//列表 
	private List<TakeoutTypeData> list = new ArrayList<TakeoutTypeData>();
	
	//get,set-------------------------------------------------------------------
	public List<TakeoutTypeData> getList() {
		return list;
	}

	public void setList(List<TakeoutTypeData> list) {
		this.list = list;
	}
}
