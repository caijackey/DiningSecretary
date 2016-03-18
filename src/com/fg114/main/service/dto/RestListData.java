package com.fg114.main.service.dto;

/**
 * 列表页餐馆，菜品对象
 * @author qianjiefeng
 *
 */
public class RestListData  {
	//图片url
	public String picUrl;
	public TopRestListDTO dto;
	//餐馆id
	public String restId;
	//餐馆名称
	public String restName;

	//图标标志  0:无图标  1：券  2：惠  3：币 4：币(高亮)
	public int iconTag;
	//图标标题  比如：75折券   15%
	public String iconTitle;
	
	//总体评价  0~5
	public double overallNum;
	//人均
	public String avgPrice;
	
	//描述  
	public String describe;
	//距离  
	public String distance;
}
