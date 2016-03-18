package com.fg114.main.service.dto;

import java.util.List;


public class TakeoutListDTO extends BaseDTO {
	
	//列表 
	public List<TakeoutListData> list;
	//餐馆类别列表  uuid    name   selectTag 
	public List<CommonTypeDTO> typeList;
	//起送类别列表 uuid    name   selectTag 
	public List<CommonTypeDTO> sendLimitList;
	
	//转换后的中心点gps
	public double chineseLon;
	public double chineseLat;
	
}
