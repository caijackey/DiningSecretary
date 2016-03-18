package com.fg114.main.service.dto;

import java.util.List;

public class OrderStateInfoData {
	//状态图标url
	public String stateIconUrl;
	//状态提示(标红)
	public String stateHint;
	
	//显示操作面板
	public boolean showOperatePanelTag;
	//操作提示(标红)
	public String operateHint;
	//秘币图片列表 name字段存储url
	public List<CommonTypeDTO> coinPicList;
	//秘币wapUrl
	public String coinWapUrl;
	//分享按钮名称
	public String shareBtnName;
	//是否显示分享按钮
	public boolean showShareBtnTag;
	//分享信息
	public ShareInfoData shareInfo;
}
