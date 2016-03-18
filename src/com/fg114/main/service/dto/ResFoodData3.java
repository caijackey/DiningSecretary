package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜品对象
 * @author qianjiefeng
 *
 */
public class ResFoodData3  {
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
	//单位
	private String unit;
	//图片url
	private String picUrl;
	//菜品大图片
	private String picOriginalUrl;
    //小菜系id
	private String smallStyleId;
    //小菜系名称
	private String smallStyleName;
	
	//评论总数
    int totalCommentNum;
    //评论(最新的一条评论)
	ResFoodCommentData commentData;
	
	//在列表显示的时候控制是否展开
	public boolean isExpanded=false;


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
	public String getPicOriginalUrl() {
		return picOriginalUrl;
	}
	public void setPicOriginalUrl(String picOriginalUrl) {
		this.picOriginalUrl = picOriginalUrl;
	}
	public String getSmallStyleId() {
		return smallStyleId;
	}
	public void setSmallStyleId(String smallStyleId) {
		this.smallStyleId = smallStyleId;
	}
	public String getSmallStyleName() {
		return smallStyleName;
	}
	public void setSmallStyleName(String smallStyleName) {
		this.smallStyleName = smallStyleName;
	}
	public int getTotalCommentNum() {
		return totalCommentNum;
	}
	public void setTotalCommentNum(int totalCommentNum) {
		this.totalCommentNum = totalCommentNum;
	}
	public ResFoodCommentData getCommentData() {
		return commentData;
	}
	public void setCommentData(ResFoodCommentData commentData) {
		this.commentData = commentData;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResFoodData3 toBean(JSONObject jObj) {
		
		ResFoodData3 dto = new ResFoodData3();

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

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
	
}
