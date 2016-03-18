package com.fg114.main.service.dto;

import java.util.List;

/**
 * 数据
 *
 */
public class MdbFreeOrderInfoData {
	//是否显示订单提示面板
	public boolean showOrderHintPanelTag;
	//订单提示信息
	public MdbFreeOrderHintData orderHintData;
	
	
	//是否需要展开更多预订信息面板
	public boolean needOpenMoreReserveInfoPanelTag;
	//更多预订信息列表
	public List<OrderMoreReserveInfoData> moreReserveInfoList;
	
	//功能提示 标红
	public String funcHint;
	
	
	//是否显示验证码面板
	public boolean showValidCodePanelTag;
	//验证码提示 标红
	public String validCodePanelHint;
	//验证码图片url
	public String validCodePicUrl;
	//验证码
	public String validCode;
	
	
	//是否显示抽奖面板
	public boolean showDrawPanelTag;
	//抽奖面板提示 标红
	public String drawPanelHint;
	//是否可以抽奖
	public boolean canDrawTag;
	//抽奖号码
	public String drawCode;
	//抽奖wapUrl
	public String drawWapUrl;
	//抽奖按钮名称
	public String drawBtnName;
	
	
	//是否显示献花面板
	public boolean showFlowerPanelTag;
	//献花面板提示 标红
	public String flowerPanelHint;
	//是否可以献花
	public boolean canFlowerTag;
	
	
	//是否显示上传分享图片面板
	public boolean showUploadSharePicPanelTag;
	//上传分享图片面板提示 标红
	public String uploadSharePicHint;
	//分享图片url
	public String uploadSharePicUrl;
	//是否可以上传图片
	public boolean canUploadSharePicTag;
	
	
	//是否可以撤销订单
	public boolean canCancelTag;
	//是否可以修改订单
	public boolean canEditTag;
	
	
	//是否需要倒计时刷新
	public boolean needCountDownRefreshTag;
	//倒计时秒数
	public int countDownSec;
}
