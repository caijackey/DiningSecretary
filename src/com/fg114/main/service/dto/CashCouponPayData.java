package com.fg114.main.service.dto;

import java.util.List;


/**
 * 评论数据
 * @author qianjiefeng
 *
 */
public class CashCouponPayData  {
	
	//标题
	public String title;
	//面额     xx元
	public double couponValue;
	//最大购买数量
	public int maxBuyNum;
	//单价
	public double unitPrice;
	//是否显示秘币 
	public boolean canShowUserPointNumTag;
	//用户秘币数量
	public int userPointNum;
	//一个秘币的价值  
	public double onePointPrice;
	//帐户余额    0元  隐藏
	public double userRemainMoney;

	//是否显示电话预订按钮
	public boolean canShowBookTelBtnTag;
	//预订电话
	public String bookTel;
	
	//礼品卡列表
	public List<GiftCardData> cardList;
	
	//支付方式列表
	public List<PayTypeData> payTypeList;
	


	
	
}
