package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####订单列表####
 * 
 */

public class TakeoutOrderList2DTO extends BaseDTO {

	// 需要提示的订单数量
	public int hintOrderNum;

	// 列表
	public List<TakeoutOrderHintData2> list;

}
