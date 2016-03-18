package com.fg114.main.service.dto;

import java.util.List;

/**
 * 报名表单
 *
 */
public class CouponApplyFormData {
	//输入框列表
	public List<CouponApplyInputData> inputList;
	
	//是否显示分享
	public boolean showSharePanelTag;
	//分享提示
	public String shareHint;
}
