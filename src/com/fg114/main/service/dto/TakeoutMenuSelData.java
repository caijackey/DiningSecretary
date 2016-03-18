package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####菜品选择信息####
 *
 */
public class TakeoutMenuSelData {
	//数据标志 客户端和服务端自动生成的唯一标志
	public String dataIdentifer;
	
	//类别 1:菜品  2：赠品  3：快递费，免快递费，打折等
	public int typeTag;
	//是否可以选择赠品
	public boolean canSelGiftTag;
	//可选赠品类别id(用于赠品选择页查询)
	public String giftTypeId;
	//菜品id
	public String uuid;
	//菜品名称
	public String name;
	//名称颜色 空为默认颜色
	public String nameColor;
	//价格
	public double price;
	//选择数量
	public int num;
	//是否显示数量
	public boolean canShowNumTag;
	//是否可以修改数量
	public boolean canChangeNumTag;
	//属性类别列表
	public List<TakeoutMenuPropertyTypeData> propertyTypeList=null;
	//所选属性描述
	public String selPropertyHint;
	
	//本地使用----------------------------------------
	// 总体评价
	public double overallNum;
	
}
