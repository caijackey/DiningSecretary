package com.fg114.main.service.dto;


public class CouponPostResultData  {
	
	
	//银联支付
	public String unionPayXml;
	
	//支付宝支付(客户端)
	public String aliPayInfo;
	
	//支付宝支付(wap)
	public String wapAliPayUrl;
	
	//支付宝信用卡支付(wap)
	public String wapAliPayCreditCardUrl;
	
	//微信支付
	public String weixinInfo;
	
	//其他wap url
	public String wapUrl;
	
	//数据校验是否通过
	public boolean chkPassTag;
	//错误提示
	public String errorHint;
	//订单id  chkPassTag:true 返回
	public  String orderId;	
}
