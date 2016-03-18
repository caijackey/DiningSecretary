package com.fg114.main.service.dto;
/**
 * ####支付信息####
 *
 */
public class TakeoutOnlinePayResultData {
	//银联支付
	public String unionPayXml;
	
	//支付宝支付(客户端)
	public String aliPayInfo;
	
	//支付宝支付(wap)
	public String wapAliPayUrl;
	
	//支付宝信用卡支付(wap)
	public String wapAliPayCreditCardUrl;
	
	//其他wap url
	public String wapUrl;
	
	 //微信支付
	public String weixinInfo;
	
	//数据校验是否通过
	public boolean chkPassTag;
	//错误提示
	public String errorHint;
}
