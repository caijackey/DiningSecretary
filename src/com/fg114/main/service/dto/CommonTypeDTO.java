package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.app.view.ItemData;

/**
 * 通用类别DTO
 * @author qianjiefeng
 *
 */
public class CommonTypeDTO  implements ItemData,Serializable {
	
	//类别ID 
	private String uuid = "";
	//父类别ID 
	private String parentId = "";
	//类别名称
	private String name = "";
	//数量
	private int num;
	//定位成功
	private boolean succTag;
	//电话
	private String phone = "";
	//备注   
	//点菜模块，做法的附加价格  放在CommonTypeDTO的memo中
	private String memo = "";
	
	// 是否选中
	private boolean selectTag;
	
	// 是否该类别的第一项
	private boolean isFirst = false;
	//关键词
	private String keywords;
	//首字母
	private String firstLetters;
	private String firstLetter;
	
	//----本地自用属性
	private int gpsType=1;//经纬度类别  1:原生 2：百度 3：google

	private double longitude=0;
	private double latitude=0;
	
	//get,set-------------------------------------------------------------------
	
	
	
	public int getGpsType() {
		return gpsType;
	}
	public void setGpsType(int gpsType) {
		this.gpsType = gpsType;
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
	public String getMemo() {
		return memo;
	}
	public boolean isSelectTag() {
		return selectTag;
	}
	public void setSelectTag(boolean selectTag) {
		this.selectTag = selectTag;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getParentId() {
		return parentId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public boolean isSuccTag() {
		return succTag;
	}
	public void setSuccTag(boolean succTag) {
		this.succTag = succTag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CommonTypeDTO toBean(JSONObject jObj) {
		
		CommonTypeDTO dto = new CommonTypeDTO();
		
		try {

			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("parentId")) {
				dto.setParentId(jObj.getString("parentId"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("num")) {
				dto.setNum(jObj.getInt("num"));
			}
			if (jObj.has("phone")) {
				dto.setPhone(jObj.getString("phone"));
			}
			if (jObj.has("succTag")) {
				dto.setSuccTag(jObj.getBoolean("succTag"));
			}
			if (jObj.has("memo")) {
				dto.setMemo(jObj.getString("memo"));
			}
			if (jObj.has("selectTag")) {
				dto.setSelectTag(jObj.getBoolean("selectTag"));
			}
			if (jObj.has("longitude")) {
				dto.setLongitude(jObj.getDouble("longitude"));
			}
			if (jObj.has("latitude")) {
				dto.setLatitude(jObj.getDouble("latitude"));
			}
			if (jObj.has("gpsType")) {
				dto.setGpsType(jObj.getInt("gpsType"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			
		}
		return dto;
	}
	//Spinner控件用来显示项目用
	public String toString(){
		return this.name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonTypeDTO) {
			return uuid.equals(((CommonTypeDTO) o).getUuid());
		}
		return false;
	}
	
	
	public CommonTypeDTO clone() {
		CommonTypeDTO dto = new CommonTypeDTO();
		dto.setUuid(uuid);
		dto.setParentId(parentId);
		dto.setName(name);
		dto.setNum(num);
		dto.setPhone(phone);
		dto.setSuccTag(succTag);
		dto.setMemo(memo);
		dto.setSelectTag(selectTag);
		dto.setGpsType(gpsType);
		dto.setLongitude(longitude);
		dto.setLatitude(latitude);
		return dto;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getFirstLetters() {
		return firstLetters;
	}
	public void setFirstLetters(String firstLetters) {
		this.firstLetters = firstLetters;
	}
	@Override
	public List<? extends ItemData> getList() {
		return null;
	}
	@Override
	public void setList(List<? extends ItemData> list) {
		
	}
	@Override
	public void setIsNeedGroupBy(boolean needGroupBy) {
		
	}
	@Override
	public boolean isNeedGroupBy() {
		return false;
	}
	@Override
	public void setFirstLetter(String firstLetter) {
		this.firstLetter=firstLetter;			
	}

	@Override
	public String getFirstLetter() {
		return this.firstLetter;
	}
}
