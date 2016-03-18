package com.fg114.main.service.dto;
/**
 * ####菜品属性####
 *
 */
public class TakeoutMenuPropertyData {
	//属性id
    public String uuid;
    //属性名称
	public String name;
	//价格
	public double price;
	
	//------本地使用
	//该属性是否被选中
	public Boolean isSelected=false;
}
