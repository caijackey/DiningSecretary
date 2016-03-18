package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 评论数据
 * @author qianjiefeng
 *
 */
public class CommentReplyData  {
	//评论id
	private String uuid;
	//评论人
	private String userName;
	//评论人小头像url
	private String userSmallPicUrl;
	//评论人大头像url
	private String userPicUrl;
	//评论时间
	private long createTime;
	//评论内容
	private String detail;
	
	
	
	//get,set-------------------------------------------------------------------
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserSmallPicUrl() {
		return userSmallPicUrl;
	}
	public void setUserSmallPicUrl(String userSmallPicUrl) {
		this.userSmallPicUrl = userSmallPicUrl;
	}
	public String getUserPicUrl() {
		return userPicUrl;
	}
	public void setUserPicUrl(String userPicUrl) {
		this.userPicUrl = userPicUrl;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CommentReplyData toBean(JSONObject jObj) {
		
		CommentReplyData dto = new CommentReplyData();

		try {
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("userName")) {
				dto.setUserName(jObj.getString("userName"));
			}
			if (jObj.has("userSmallPicUrl")) {
				dto.setUserSmallPicUrl(jObj.getString("userSmallPicUrl"));
			}
			if (jObj.has("userPicUrl")) {
				dto.setUserPicUrl(jObj.getString("userPicUrl"));
			}
			if (jObj.has("createTime")) {
				dto.setCreateTime(jObj.getLong("createTime"));
			}
			if (jObj.has("detail")) {
				dto.setDetail(jObj.getString("detail"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}

	
	
}
