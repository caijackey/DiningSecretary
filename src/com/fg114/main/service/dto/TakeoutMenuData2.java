package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####菜品数据####
 *
 */
public class TakeoutMenuData2 {
	//菜品id
    public String uuid;
	//是否已收藏
    public boolean favTag;
	//图片url 列表页用
    public String picUrl;
	//大图片url 详情页用
    public String bigPicUrl;
	//菜品名称
    public String name;
	//全拼
    public String pinyin;
	//首字母
    public String pinyinCap;
	//价格
    public double price;
	//总体评价
    public double overallNum;
	//描述 详细页用
    public String detail;
	//属性类别列表 列表页用
    public List<TakeoutMenuPropertyTypeData> propertyTypeList;
    
    //大图宽度
    public int bigPicWidth;
    //大图高度
    public int bigPicHeight;
    
    
 // -----------------------------------
	// 本地使用
	// uuid :类别id name:类别名称 memo:类别描述 （用于显示在右侧列表的分类标题栏上）

	public int num;// 菜品购买的总数量  注意不是购物车中菜品数量  如果菜品有配料 菜品购买总数量=购物车1中数量+购物车2中数量

	public String soTypeId;

	public String soTypeName;

	public String soTypeMemo;

	public boolean soIsFirst = false;// 判断是否是第一个

	public String soGroupId = "";

	public boolean soIsFirstInCart = false;
	
	public boolean isFavTag=false;//是否是属于我的最爱
}
