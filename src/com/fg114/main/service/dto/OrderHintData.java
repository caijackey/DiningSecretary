package com.fg114.main.service.dto;

import java.util.List;


/**
 * @author qianjiefeng
 *
 */
public class OrderHintData  {
	//订单id
	public String orderId;
	
	//餐馆id
	public String restId;
	//餐厅名
	public String restName;
	//图标url
	public String iconUrl;
	
	//现金券id
	public String cashCouponId;
	
	//就餐时间  格式为 06月10日 18:50
	public String reserveTime;
	//人数  格式为  4人
	public String peopleNum;
	//房间类型  
	public String roomTypeName;
	
	//显示的就餐人姓名
	public String eaterName;
	//显示的就餐人手机号
	public String eaterTel;
	
	//状态id  1:订单处理中  2:订单成功  3：订单失败 4：退订成功    5:等待就餐
	//不同的状态 文字颜色会不一样
	public int statusTag;
	//状态名称   xxxxx<font color=#000000>xxx</font>xxx  
	public String statusName;
	//餐馆地址
	public String restAddress;

	//功能按钮列表
	//如果是列表页调用  btnList设置为null
	//首页和订单详情页调用  btnList需要根据不同的状态设置不同的值
	public List<OrderHintBtnData> btnList;
}
