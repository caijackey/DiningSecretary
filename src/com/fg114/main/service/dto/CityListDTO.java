package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class CityListDTO extends BaseDTO {
	//列表
	private List<CityData> list = new ArrayList<CityData>();

	
	
	//get,set-------------------------------------------------------------------

	public List<CityData> getList() {
		return list;
	}

	public void setList(List<CityData> list) {
		this.list = list;
	}
	
	
	
}
