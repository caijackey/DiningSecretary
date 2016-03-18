package com.fg114.main.service.dto;
/**
 * 团购面板数据
 *
 */
public class CouponPanelData {
	//uuid
    public String uuid;
	//团购名称
	public String couponName;
    //按钮名称
	public String btnName;
    //团购提示 标红
	public String couponHint;
    //是否是倒计时
	public boolean countDownTag;
    //倒计时提示
	public String countDownHint;
    //剩余秒数
	public long remainSeconds;	
}
