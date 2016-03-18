package com.fg114.main.service.dto;

import java.util.List;


/**
 * 订单信息
 *
 */
public class OrderInfoData2 {
	//未就餐订单数量
	public int hintOrderNum;
	
	
	//上一个活跃订单id 
	public String prevOrderId;
	//下一个活跃订单id
	public String nextOrderId;

	
	//订单提示信息
	public OrderHintData2 orderHintData;
	
	
	//是否需要展开更多预订信息面板
	public boolean needOpenMoreReserveInfoPanelTag;
	//更多预订信息列表
	public List<OrderMoreReserveInfoData> moreReserveInfoList;
	
	
	//功能提示 标红
	public String funcHint;
	//功能按钮列表
	public List<OrderFuncBtnData> funcBtnList;
	
	
	//是否显示金额面板
	public boolean showPricePanelTag;
	//上传小票提示
	public String receiptHint;
	//是否可以上传小票
	public boolean canUploadReceiptTag;
	//小票小图url
	public String receiptUrl;
	//小票大图url
	public String bigReceiptUrl;
	//上传小票提示
	public String inputPriceHint;
	//是否可以输入金额
	public boolean canInputPriceTag;
	//金额,秘币提示  需要标红  xxxxx<font color=#000000>xxx</font>xxx
	public String priceHint;
	//是否可以金额报错
	public boolean canReportPriceErrorTag;
	
	
	//是否需要弹出确认对话框
	public boolean needOpenConfirmDlgTag;
	//内容
	public String cdMsg;
	//取消按钮名称
	public String cdCancelBtnName;
	//确定按钮名称
	public String cdOkBtnName;
	//动作url
	public String cdActionXmsUrl;
	
	
	//操作提示 标红
	public String operateHint;
	//是否可以撤销订单
	public boolean canCancelTag;
	//是否可以修改订单
	public boolean canEditTag;

	
	//就餐时间 (下单时选择的预订时间)
	public long reserveTime;
	//就餐人数
	public int peopleNum;
    //房间类型  0:只订大厅  1:只订包房  2:优先订大厅  3:优先订包房
	public int roomTypeTag;

	
	//姓名
	public String bookerName;
	//性别    约定  1：先生  0：女士 
	public int bookerSexTag;  
	//手机号   
	public String bookerTel;
	//备注
	public String memo;
	
	
	//是否为他人订餐
	public boolean forOtherTag;
	//姓名
	public String eaterName;
	//性别       约定  1：先生  0：女士   
	public int eaterSexTag;
	//手机号   
	public String eaterTel; 
	
	//活动id
	public String activityId;
	//活动Name
	public String activityName;
}
