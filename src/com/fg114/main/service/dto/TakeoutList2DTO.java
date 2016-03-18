package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####外卖列表####
 *
 */
public class TakeoutList2DTO extends BaseDTO {
	//列表 
	public List<TakeoutListData2> list;
	//餐馆类别列表  uuid    name   selectTag 
	public List<CommonTypeDTO> typeList;
	//起送类别列表 uuid    name   selectTag 
	public List<CommonTypeDTO> sendLimitList;
	
	//纠正的经度
	public double chineseLon;
	//纠正的纬度
	public double chineseLat;
}
