package com.fg114.main.service.dto;

public class KqReqData {
	// 支付订单信息
	public KqReqOrderData payOrder;
	// 订单签名
	public String orderSign;
	// 查询签名
	public String querySign;
	// 商户的快钱会员编号（11位）
	public String mebCode;
	// 商户编号（15位）
	public String merchantId;
	// 用户在合作商户的用户名
	public String partnerUserId;
	// 快钱服务器地址
	public String iosUrl;
	public String javaUrl;
}
