package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜品对象
 * @author qianjiefeng
 *
 */
public class ResFoodData  {
	//特色菜ID 
	private String uuid;
	//特色菜名称
	private String name;
	//价格
	private String price;
	//人气 
	private int hotNum;
	//介绍   可以为null
	private String intro;
	//菜品大图片
	private String picOriginalUrl;
	//菜品图片
	private String picUrl; 
	//菜品图片
	private String unit; 
	
    //小菜系id
    private String smallStyleId;
    //小菜系名称
    private String smallStyleName;
    
    //首字母
    private String firstLetter;

	
	//get,set-------------------------------------------------------------------
    
    

	public String getPicOriginalUrl() {
		return picOriginalUrl;
	}
	public String getSmallStyleName() {
		return smallStyleName;
	}
	public void setSmallStyleName(String smallStyleName) {
		this.smallStyleName = smallStyleName;
	}
	public String getSmallStyleId() {
		return smallStyleId;
	}
	public void setSmallStyleId(String smallStyleId) {
		this.smallStyleId = smallStyleId;
	}
	public void setPicOriginalUrl(String picOriginalUrl) {
		this.picOriginalUrl = picOriginalUrl;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public int getHotNum() {
		return hotNum;
	}
	public void setHotNum(int hotNum) {
		this.hotNum = hotNum;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public String getFirstLetter() {
		return firstLetter;
	}
	public void setFirstLetter(String firstLetter) {
		this.firstLetter = firstLetter;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResFoodData toBean(JSONObject jObj) {
		
		ResFoodData dto = new ResFoodData();

		try {

			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("price")) {
				dto.setPrice(jObj.getString("price"));
			}
			if (jObj.has("hotNum")) {
				dto.setHotNum(jObj.getInt("hotNum"));
			}
			if (jObj.has("intro")) {
				dto.setIntro(jObj.getString("intro"));
			}
			if (jObj.has("picUrl")) {
				dto.setPicUrl(jObj.getString("picUrl"));
			}
			if (jObj.has("picOriginalUrl")) {
				dto.setPicOriginalUrl(jObj.getString("picOriginalUrl"));
			}
			if (jObj.has("unit")) {
				dto.setUnit(jObj.getString("unit"));
			}
			if (jObj.has("smallStyleId")) {
				dto.setSmallStyleId(jObj.getString("smallStyleId"));
			}
			if (jObj.has("smallStyleName")) {
				dto.setSmallStyleName(jObj.getString("smallStyleName"));
			}
			if (jObj.has("firstLetter")) {
				dto.setFirstLetter(jObj.getString("firstLetter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}

}
