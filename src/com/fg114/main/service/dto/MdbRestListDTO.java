package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####列表####
 *
 */
public class MdbRestListDTO extends BaseDTO{
	//广告位  320*60
    public List<MainPageAdvData> advList;
    
	//餐馆列表 
    public List<MdbRestListData> list;
	
	//地域,商区列表
    public List<RfTypeListDTO> regionList;
	//菜系类别列表  大类 小类
    public List<RfTypeListDTO> menuTypeList;
	//排序列表 可以为null
    public List<RfTypeDTO> sortList;
	//人均列表 可以为null
    public List<RfTypeDTO> avgList;

	//地域
    public String selectRegionName;
	//商区
    public String selectDistrictName;
	//主菜系
    public String selectMainMenuTypeName;
	//子菜系
    public String selectSubMenuTypeName;
	//排序
    public String selectSortName;
}
