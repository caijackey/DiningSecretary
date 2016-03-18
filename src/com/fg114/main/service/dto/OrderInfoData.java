package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * @author qianjiefeng
 *
 */
public class OrderInfoData  {
    //未就餐订单数量
	public int hintOrderNum;
	
	//上一个活跃订单id
	public String prevOrderId;
	//下一个活跃订单id
	public String nextOrderId;

	
	//订单id
	public String orderId;
	//订单提示信息
	public OrderHintData orderHintData;
	//折扣提示 需要标红   xxxxx<font color=#000000>xxx</font>xxx  
	public String discountHint;
	
	
	//是否显示金额面板
	public boolean showPricePanelTag;
	//上传小票提示
	public String receiptHint;
	//是否可以上传小票
	public boolean canUploadReceiptTag;
	//小票url
	public String receiptUrl;
	public String bigReceiptUrl;
	//上传小票提示
	public String inputPriceHint;
	//是否可以输入金额
	public boolean canInputPriceTag;
	//金额,秘币提示  需要标红  xxxxx<font color=#000000>xxx</font>xxx
	public String priceHint;
	//是否可以金额报错
	public boolean canReportPriceErrorTag;
	
	
	//是否显示评论面板
	public boolean showCommentPanelTag;
	//评论提示
	public String commentHint;
	//是否可以评论
	public boolean canCommentTag;
	//评论列表 
	public List<CommentData> commentList=new ArrayList<CommentData>();

	
	//是否可以修改订单
	public boolean canEditTag;
	//是否可以撤销订单
	public boolean canCancelTag;

	
	//就餐时间 (下单时选择的预订时间)
	public long reserveTime;
	//就餐人数
	public int peopleNum;
    //房间类型  0:只订大厅  1:只订包房  2:优先订大厅  3:优先订包房
	public int roomTypeTag;

	public String bookerName;//姓名
	public int bookerSexTag;//性别    约定  1：先生  0：女士   
	public String bookerTel;//手机号   
	
	public String memo;//备注
	
	public boolean forOtherTag;//是否为他人订餐
	public String eaterName;//姓名
	public int eaterSexTag;//性别       约定  1：先生  0：女士   
	public String eaterTel;//手机号    

}





