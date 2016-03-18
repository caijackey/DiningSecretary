package com.fg114.main.service.dto;

import java.util.List;

/**
 * 表单数据
 *
 */
public class CouponOrderFormData {
	//标题
	public String title;
	//单价
	public double unitPrice;
	//最大购买数量
	public int maxBuyNum;
	//默认手机号码
	public String defaultUserTel;
	//帐户余额  
	public double userRemainMoney;
	//用户秘币数量
	public int userPointNum;
	//一个秘币的价值  
	public double onePointPrice;
	
	
	//是否显示电话预订按钮
	public boolean showTelBtnTag;
	//预订电话
	public String bookTel;
	//是否显示下单按钮
	public boolean showOrderBtnTag;
	
	
	//是否显示收货信息面板
	public boolean showReceiverPanel;
	//默认收货人姓名
	public String defaultReceiverName;
	//默认收货人手机号
	public String defaultReceiverTel;
	//默认收货人地址
	public String defaultReceiverAddress;
	//运费
	public double deliverPrice;
	
	
	//礼品卡列表
	public List<GiftCardData> cardList;
	
	//支付方式列表
	public List<PayTypeData> payTypeList;
}
