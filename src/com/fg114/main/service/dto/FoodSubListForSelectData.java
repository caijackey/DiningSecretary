package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


public class FoodSubListForSelectData {
	// uuid
	String uuid = "";
	// 名称
	String name = "";
	// 人气
	int hotNum;
	// 价格 -1：不详
	double price;
	// 价格名称
	String priceStr = "";

	// 自用属性
	private transient String typeId = "";

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

	public int getHotNum() {
		return hotNum;
	}

	public void setHotNum(int hotNum) {
		this.hotNum = hotNum;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getPriceStr() {
		return priceStr;
	}

	public void setPriceStr(String priceStr) {
		this.priceStr = priceStr;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof FoodSubListForSelectData) {
			FoodSubListForSelectData data = (FoodSubListForSelectData) o;
			return this.getUuid().equals(data.getUuid());
		}
		return false;
	}
}
