package com.fg114.main.service.dto;

import java.util.List;


public class CashCouponList2DTO extends BaseDTO {
	//列表 
	public List<CashCouponData> list;
	
	//地域列表
	public List<CommonTypeDTO> regionList;
	//菜系列
	public List<CommonTypeDTO> menuList;
	//排序列表
	public List<CommonTypeDTO> sortTypeList;
	
	//广告位  320*50
    public List<MainPageAdvData> advList;
	
	// -------------------------- 原谅我，以前很多地方用get方法，暂时不想动他们
	public List<CashCouponData> getList() {
		return list;
	}
	public void setList(List<CashCouponData> list) {
		this.list = list;
	}
	public List<CommonTypeDTO> getRegionList() {
		return regionList;
	}
	public void setRegionList(List<CommonTypeDTO> regionList) {
		this.regionList = regionList;
	}
	public List<CommonTypeDTO> getMenuList() {
		return menuList;
	}
	public void setMenuList(List<CommonTypeDTO> menuList) {
		this.menuList = menuList;
	}
	public List<CommonTypeDTO> getSortTypeList() {
		return sortTypeList;
	}
	public void setSortTypeList(List<CommonTypeDTO> sortTypeList) {
		this.sortTypeList = sortTypeList;
	}
}
