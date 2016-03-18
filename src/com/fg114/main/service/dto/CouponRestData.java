package com.fg114.main.service.dto;

import java.util.List;
/**
 * 适用餐厅
 *
 */
public class CouponRestData {
	//餐厅id
	public String restId;
	//餐厅名称
	public String restName;
	//餐厅地址
	public String restAddress;
	//距离
	public String distanceMeter;
    //电话号码列表
	public List<RestTelInfo> telList;
}
