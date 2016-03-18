package com.fg114.main.service.dto;

import java.util.List;
/**
 * 订单列表
 *
 */
public class OrderList2DTO extends BaseDTO{
	//未就餐订单数量
	public int hintOrderNum;

	//列表 
	public List<OrderHintData2> list;
	//状态列表 selectTag 标志选择了哪一项
	public List<CommonTypeDTO> statusList;
	//操作列表  uuid和name
	public List<CommonTypeDTO> operateList;
}
