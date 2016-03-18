package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class TakeoutMenuListPackDTO  {
	
	//列表
	public List<TakeoutMenuListDTO> list = new ArrayList<TakeoutMenuListDTO>();
	//外卖餐厅信息     起送价格:sendLimitPrice   是否可以下单：canOrderTag  是否是特约商户:vipTag  电话：telList  hintForCanNotOrder canShowConfrimBtnTag  hintForCanNotConfirm
	public TakeoutInfoData2 takeoutData;//4.16版本以后使用
//	public TakeoutInfoData takeoutData;
	//用户默认收货地址  uuid:uuid name:地址  phone:电话  
	//为空就是收获地址为空
	public CommonTypeDTO userReceiveAdressData;
	
	//顶部提示 支持html
	public String topHint;
	
}