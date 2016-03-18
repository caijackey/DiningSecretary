package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @author Administrator
 *@deprecated
 */
public class TakeoutRestListData implements Parcelable {

	//ID 
	private String uuid = "";
	//名称
	private String name = "";
	//外送时间
	private String openTime = "";
	//起送 
	private String sendLimit = "";
	//电话
	private String phone = "";
	//距离  
	private String distanceMeter;
	//过去已经预订过的
	private boolean haveCallTag;
	//经度
	private double longitude; 
	//纬度
	private double latitude;
	
	public TakeoutRestListData()
	{
		
	}
	public TakeoutRestListData(Parcel source)
	{
		this.uuid=source.readString();
		this.name=source.readString();
		this.openTime=source.readString();
		this.sendLimit=source.readString();
		this.phone=source.readString();
		this.distanceMeter=source.readString();
		this.latitude=source.readDouble();
		this.longitude=source.readDouble();
	}
	
	//get,set-------------------------------------------------------------------
	
	public String getUuid() {
		return uuid;
	}
	public boolean isHaveCallTag() {
		return haveCallTag;
	}
	public void setHaveCallTag(boolean haveCallTag) {
		this.haveCallTag = haveCallTag;
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
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDistanceMeter() {
		return distanceMeter;
	}
	public void setDistanceMeter(String distanceMeter) {
		this.distanceMeter = distanceMeter;
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
	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(uuid);
		dest.writeString(name);
		dest.writeString(openTime);
		dest.writeString(sendLimit);
		dest.writeString(phone);
		dest.writeString(distanceMeter);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		
	}
	public static final Parcelable.Creator<TakeoutRestListData> CREATOR = new Parcelable.Creator<TakeoutRestListData>() { 

	     @Override 
	     public TakeoutRestListData createFromParcel(Parcel source) { 
	             return new TakeoutRestListData(source); 
	     } 

	     @Override 
	     public TakeoutRestListData[] newArray(int size) { 
	             return new TakeoutRestListData[size]; 
	     } 

	};
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static TakeoutRestListData toBean(JSONObject jObj) {

		TakeoutRestListData dto = new TakeoutRestListData();

		try {

			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("openTime")) {
				dto.setOpenTime(jObj.getString("openTime"));
			}
			if (jObj.has("sendLimit")) {
				dto.setSendLimit(jObj.getString("sendLimit"));
			}

			if (jObj.has("phone")) {
				dto.setPhone(jObj.getString("phone"));
			}

			if (jObj.has("distanceMeter")) {
				dto.setDistanceMeter(jObj.getString("distanceMeter"));
			}
			if (jObj.has("haveCallTag")) {
				dto.setHaveCallTag(jObj.getBoolean("haveCallTag"));
			}
			if (jObj.has("longitude")) {
				dto.setLongitude(jObj.getDouble("longitude"));
			}
			if (jObj.has("latitude")) {
				dto.setLatitude(jObj.getDouble("latitude"));
			}
		} catch (JSONException e) {
			e.printStackTrace();

		}
		return dto;
	}
}
