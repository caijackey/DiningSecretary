package com.fg114.main.service.dto;

import java.util.List;

/**
 * 团购详情
 *
 */
public class CouponInfoData {
	//id
	public String uuid;
	//团购类别  1:普通  2：抢购  3：报名
	public int typeTag;
	//团购名称
	public String name;
	
	//状态名称 
	public String statusName;
	//图片url
	public String picUrl;
	//团购图片列表 
	public List<CouponPicData> picList;
	
	//现价
	public String nowPrice;
	//原价
	public String oldPrice;
	//报名简短提示 标红
	public String applyShortHint;
	//按钮是否可用
	public boolean btnEnabledTag;
	//按钮名称
	public String btnName;
	
	//团购描述列表
	public List<CouponDescribeData> describeList;
	
	
	//抢购标题
	public String limitTitle;
    //剩余秒数
	public long remainSeconds;	
	//抢购提示 标红
	public String limitHint;
	//抢购时间段
	public List<CouponLimitRangeData> limitRangeList;
	
	
	//提示标题
	public String hintTitle;
	//提示内容
	public String hintDetail;
	
	//适用餐厅列表
	public List<CouponRestData> restList;

	//团购详情
	public String couponDetail;
	//团购详情wap页 隐藏标题栏
	public String couponDetailWapUrl;
	
	
	//是否支持随时退款
	public boolean canAnytimeRefundTag;
	//hint
	public String anytimeRefundHint;
	//是否支持过期退款
	public boolean canOvertimeRefundTag;
	//hint
	public String overtimeRefundHint;
	//已售多少张
	public String soldNumHint;
	//剩余时间提示
	public String remainTimeHint;
}
