package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用类别DTO
 * @author qianjiefeng
 *
 */
public class InviteSmsInfoDTO  {
	//用户姓名
	public String inviterName;
	//餐厅名称
	public String restName;
	//餐厅地址
	public String restAddress;
	//就餐时间
	public String dinnerDate;
	//经度
	public double longitude; 
	//纬度
	public double latitude;
	//地标关键词 例如 大楼;地铁
	public String placeKey;  
	//地标半径  （米）
	public int placeRadius;  
	//地标数量
	public int placeNum;  
	//模板列表  4个模板  商务  情侣 亲人 朋友
	public List<InviteSmsTempletData> templetList = new ArrayList<InviteSmsTempletData>();
}
