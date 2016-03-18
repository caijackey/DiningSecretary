package com.fg114.main.service.dto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 餐馆图片对象
 * @author qianjiefeng
 *
 */
public class ResPicData  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//图片ID 
	private String uuid = "";
	//图片名称
	private String name = "";
	//缩略图url
	private String smallPicUrl;
	//大图url 
	private String picUrl;
	//是否是环视图
	private boolean isSurround;
	
	//get,set-------------------------------------------------------------------

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public boolean isSurround() {
		return isSurround;
	}
	public void setSurround(boolean isSurround) {
		this.isSurround = isSurround;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSmallPicUrl() {
		return smallPicUrl;
	}
	public void setSmallPicUrl(String smallPicUrl) {
		this.smallPicUrl = smallPicUrl;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResPicData toBean(JSONObject jObj) {
		
		ResPicData dto = new ResPicData();

		try {
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("smallPicUrl")) {
				dto.setSmallPicUrl(jObj.getString("smallPicUrl"));
			}
			if (jObj.has("picUrl")) {
				dto.setPicUrl(jObj.getString("picUrl"));
			}
			if (jObj.has("isSurround")) {
				dto.setSurround(jObj.getBoolean("isSurround"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
}
