package com.fg114.main.service.dto;

/**
 * @author qianjiefeng
 *
 */
public class SuggestResultData  {
	
	//餐馆id
	public String restId;
	//名称
	public String restName;
	//图标标志  0:无图标  1：券  2：惠  3：币 4：币(高亮)
	public int iconTag;
	//图标标题  比如：75折券   15%
	public String iconTitle;
	
	//历史类别   1:电话  2：收藏  3:下单  4：历史  5:推荐
	public int hisTypeTag;
	
	//状态  1:可以下单  2：可以打电话  3：不能下单也不能打电话
	public int stateTag;
	//餐厅电话 显示
	public String restTelForShow;
	//餐厅电话 拨打
	public String restTelForCall;
	
	//下单选择信息
	public OrderSelInfo orderSelInfo;
	
	//房间类型信息
	public RoomTypeInfoData roomTypeInfoData=new RoomTypeInfoData();
}
