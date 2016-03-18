package com.fg114.main.service.dto;

import java.util.List;

/**
 * @author qianjiefeng
 *
 */
public class MainPageInfo3DTO  {
    //未就餐订单数量
	private int orderNum;
	//未读站内信数量
	private int mailNum;
	//广告位  可以为null
	private List<MainPageAdvData> advList;
	//套餐数量，大于0时显示
	private int mealComboNum;
	//是否可以关闭套餐提醒
	private boolean canCloseMealComboTag;
	
	//get,set-------------------------------------------------------------------
	public int getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}

	public int getMailNum() {
		return mailNum;
	}

	public void setMailNum(int mailNum) {
		this.mailNum = mailNum;
	}

	public List<MainPageAdvData> getAdvList() {
		return advList;
	}

	public void setAdvList(List<MainPageAdvData> advList) {
		this.advList = advList;
	}

	public int getMealComboNum() {
		return mealComboNum;
	}

	public void setMealComboNum(int mealComboNum) {
		this.mealComboNum = mealComboNum;
	}

	public boolean isCanCloseMealComboTag() {
		return canCloseMealComboTag;
	}

	public void setCanCloseMealComboTag(boolean canCloseMealComboTag) {
		this.canCloseMealComboTag = canCloseMealComboTag;
	}
}
