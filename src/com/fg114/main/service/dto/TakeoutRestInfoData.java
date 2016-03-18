package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

public class TakeoutRestInfoData  {

	//ID 
	private String uuid = "";
	//名称
	private String name = "";
	//地址
	private String address = "";
	//外送时间
	private String openTime = "";
	//起送 
	private String sendLimit = "";
	//经度
	private double longitude; 
	//纬度
	private double latitude;
	//菜单类别列表 不能为null  其中 CommonTypeDTO 中的memo存放 价格
	private List<CommonTypeListDTO> foodTypeList = new ArrayList<CommonTypeListDTO>();
	//电话  name存储显示的号码   phone存储拨打的号码
	private List<CommonTypeDTO> phoneList = new ArrayList<CommonTypeDTO>();
	

	//get,set-------------------------------------------------------------------
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
	public String getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	public String getSendLimit() {
		return sendLimit;
	}
	public void setSendLimit(String sendLimit) {
		this.sendLimit = sendLimit;
	}
	public List<CommonTypeListDTO> getFoodTypeList() {
		return foodTypeList;
	}
	public void setFoodTypeList(List<CommonTypeListDTO> foodTypeList) {
		this.foodTypeList = foodTypeList;
	}
	public List<CommonTypeDTO> getPhoneList() {
		return phoneList;
	}
	public void setPhoneList(List<CommonTypeDTO> phoneList) {
		this.phoneList = phoneList;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
}
