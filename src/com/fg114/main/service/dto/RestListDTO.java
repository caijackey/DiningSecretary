package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 餐馆，菜品列表DTO
 * @author qianjiefeng
 *
 */
public class RestListDTO extends BaseDTO {
	//餐馆列表 
	public List<RestListData> list=new ArrayList<RestListData>();
	
	//地域,商区列表
	public List<RfTypeListDTO> regionList=new ArrayList<RfTypeListDTO>();
	//菜系类别列表  大类 小类
	public List<RfTypeListDTO> menuTypeList=new ArrayList<RfTypeListDTO>();
	//榜单类别列表  大类和小类
	public List<RfTypeListDTO> topRestTypeList=new ArrayList<RfTypeListDTO>();
	//排序列表 可以为null
	public List<RfTypeDTO> sortList=new ArrayList<RfTypeDTO>();
	//人均列表 可以为null
	public List<RfTypeDTO> avgList=new ArrayList<RfTypeDTO>();

	public String selectRegionName;
	public String selectDistrictName;
	public String selectMainMenuTypeName;
	public String selectSubMenuTypeName;
	public String selectMainTopRestTypeName;
	public String selectSubTopRestTypeName;
	public String selectSortName;

}
