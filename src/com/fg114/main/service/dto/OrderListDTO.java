package com.fg114.main.service.dto;

import java.util.List;

/**
 * 评论列表DTO
 * @author qianjiefeng
 *
 */
public class OrderListDTO extends BaseDTO {
    //未就餐订单数量
	public int hintOrderNum;

	//列表 
	public List<OrderHintData> list;
	//状态列表
	public List<CommonTypeDTO> statusList;

}
