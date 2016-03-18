package com.fg114.main.service.dto;

import java.util.List;

/**
 * 菜品对象
 * 
 * @author qianjiefeng
 * 
 */
public class TakeoutMenuData {

	// 菜ID
	public String uuid;
	// 菜名称
	public String name;
	// 描述
	public String detail;
	// 是否辣
	public boolean spicyTag;
	// 是否是特价
	public boolean specialPriceTag;
	// 图片url
	public String picUrl;
	// 大图片url
	public String bigPicUrl;

	// 价格
	public double price;
	// 是否是时价
	public boolean currentPriceTag;
	// 单位
	public String priceUnit;
	// 总体评价
	public double overallNum;

	// 选择的数量 默认是0
	public int selectedNum;

	// 评分列表 在详情页用到
	public List<TakeoutMenuGradeData> gradeList;

	// 全拼
	public String pinyin;
	// 首字母
	public String pinyinCap;

	// -----------------------------------
	// 本地使用
	// uuid :类别id name:类别名称 memo:类别描述 （用于显示在右侧列表的分类标题栏上）

	public int num;// 购买的数量

	public String soTypeId;

	public String soTypeName;

	public String soTypeMemo;

	public boolean soIsFirst = false;// 判断是否是第一个

	public String soGroupId = "";

	public boolean soIsFirstInCart = false;

}
