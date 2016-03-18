package com.fg114.main.service.dto;


/**
 * @author qianjiefeng
 *
 */
public class OrderHintBtnData  {
	//代驾:http://m.xiaomishu.com/appwap/drive
	//按钮类别    
	//1:邀请朋友  2:带我去餐厅 3:预点菜  4:购买现金券  5:叫出租  6:叫代驾  7:菜式列表  8:传小票   9：点评  10:去看看点评
	//11:内部wap  12:外部wap  13:餐厅推荐   14:添加到日历
	public int typeTag;
	//按钮名称    xxxxx<font color=#000000>xxx</font>xxx
	public String name;
																				
	//当后台传的时候，icon显示此Url，否则如果本地有定义（typeTag 1~10）显示本地；本地没有定义时，不显示icon
	public String iconUrl;
	//按钮动作url
	public String btnActionUrl;
	
	//按钮是否需要闪烁
	public boolean needFlashTag;
	//是否可用
	public boolean enableTag;
	
	//提示  typeTag:8 时有用
	public String hint;

	
}
