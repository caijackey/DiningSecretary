package com.fg114.main.service.dto;

/**
 * 菜品对象
 * 
 * @author qianjiefeng
 * 
 */
public class DishData {
	
	//--------------------------------------------------------------
	//菜ID 
	private String uuid;
	//菜名称
	private String name;
	//图片url
	private String picUrl;
	//大图片url
	private String bigPicUrl;
	//价格
	private double price;
	//是否是时价
	private boolean currentPriceTag;
	//单位
	private String priceUnit;
	//选择的数量  默认是0  
	private int selectedNum;
	
	
	//--------------------------------------------------------------
	
	// 本地使用------
	// 已点的数量(此次点菜已点的数量)
	private int num = 0;
	// 选择的菜的做法ID
	private String selectProcessTypeId = "";
	// 选择的菜的名称
	private String selectProcessTypeName = "";
	// 是否历史点菜数据
	private boolean isHistory = false;
	// 所属组别，如特色菜，冷菜
	private String groupId = "";

	// 上次点菜的数量
	private int oldNum = 0;
	
	// 菜所属的类别 Id
	private String typeId;
	
	// 控制在菜品列表页类别名称的显隐
	private boolean isFirstInList; //
	
	// 控制购物车中类别名称的显隐
	private boolean isFirstInCart;
	
	// 菜所属类别 名称
	private String typeName;

	// get,set-------------------------------------------------------------------
	
	public void resetOrderData() {
		num = 0;
		selectProcessTypeId = "";
		selectProcessTypeName = "";
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isCurrentPriceTag() {
		return currentPriceTag;
	}

	public void setCurrentPriceTag(boolean currentPriceTag) {
		this.currentPriceTag = currentPriceTag;
	}

	public String getPriceUnit() {
		return priceUnit;
	}

	public void setPriceUnit(String priceUnit) {
		this.priceUnit = priceUnit;
	}

	public int getSelectedNum() {
		return selectedNum;
	}

	public void setSelectedNum(int selectedNum) {
		this.selectedNum = selectedNum;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getSelectProcessTypeId() {
		return selectProcessTypeId;
	}

	public void setSelectProcessTypeId(String selectProcessTypeId) {
		this.selectProcessTypeId = selectProcessTypeId;
	}

	public String getSelectProcessTypeName() {
		return selectProcessTypeName;
	}

	public void setSelectProcessTypeName(String selectProcessTypeName) {
		this.selectProcessTypeName = selectProcessTypeName;
	}

	public boolean isHistory() {
		return isHistory;
	}

	public void setHistory(boolean isHistory) {
		this.isHistory = isHistory;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getOldNum() {
		return oldNum;
	}

	public void setOldNum(int oldNum) {
		this.oldNum = oldNum;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getBigPicUrl() {
		return bigPicUrl;
	}

	public void setBigPicUrl(String bigPicUrl) {
		this.bigPicUrl = bigPicUrl;
	}

	public boolean isFirstInList() {
		return isFirstInList;
	}

	public void setFirstInList(boolean isFirstInList) {
		this.isFirstInList = isFirstInList;
	}

	public boolean isFirstInCart() {
		return isFirstInCart;
	}

	public void setFirstInCart(boolean isFirstInCart) {
		this.isFirstInCart = isFirstInCart;
	}

	
}

