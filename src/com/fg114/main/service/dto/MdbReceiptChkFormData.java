package com.fg114.main.service.dto;

public class MdbReceiptChkFormData {
	//页面标题
	public String pageTitle;
	
	//授权号提示
	public String authNumPlaceHolder;
	//卡号提示
	public String cardNumPlaceHolder;
	//帮助按钮名称
	public String helpBtnName;
	//帮助图片url
	public String helpPicUrl;
	//帮助电话
	public String helpTel;
	
	//是否需要显示验证码
	public boolean needShowValidCodeTag;
	//验证码
	public MdbRfValidCodeData validCodeData;
	
	//提交按钮名称
	public String postBtnName;
}
