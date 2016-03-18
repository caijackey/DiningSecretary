package com.fg114.main.service.dto;

public class WeiXinPayData {
	//微信支付：只开通正式签名  appid=“wxc7622bad94e7ab25”是正式签名appid
	public String partnerId;
	public String prepayId;
	public String nonceStr;
	public String timeStamp;
	public String packageValue;
	public String sign;
}
