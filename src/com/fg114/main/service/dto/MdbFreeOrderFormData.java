package com.fg114.main.service.dto;

import java.util.List;

/**
 * 表单
 *
 */
public class MdbFreeOrderFormData {
	//餐厅名称
	public String restName;
	//金额
	public double payMoney;
	//手机号
	public String userTel;
	
	//是否可以使用余额
	public boolean canUseRemainMoneyTag;
	//帐户余额  
	public double userRemainMoney;
	
	//支付方式列表
	public List<PayTypeData> payTypeList;
	
	//显示红花面板
	public boolean showFlowerPanelTag;
	//免单率
	public String freePct;
	//免单次数
	public String freeNum;
	//免单金额
	public String freeMoney;
	//花朵数量
	public int flowerNum;
	//免单记录列表 
	public List<MdbFreeRecordData> freeRecordList;
}
