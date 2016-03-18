package com.fg114.main.service.dto;

import java.util.List;

/**
 * 热门榜单DTO
 * @author zhaozuoming
 *
 */
public class TopRestListDTO extends BaseDTO {
	
	//列表 
	public List<TopRestListData> list;
	//榜单类别名称
	public String typeName;
	
}
