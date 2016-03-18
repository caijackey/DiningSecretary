package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author qianjiefeng
 *
 */
public class UserMsgData implements Serializable{
	//uuid
	private String uuid;
	//是否已读
	private boolean readTag;
	//标题   
	private String title; 
	//内容  列表页返回30字  详细页返回全部
	private String detail;
	//创建时间
	private long createTime;
	//发送人id
	private String senderUserId;
	//发送人姓名
	private String senderUserName;
	//发送人小头像url
	private String senderUserSmallPicUrl;
	//接收人id
	private String receiverUserId;
	//接收人姓名
	private String receiverUserName;
	//接收人小头像url
	private String receiverUserSmallPicUrl;
	//是否能回复
	private boolean canReplyTag;
	//类别标志   1：收件箱  2：发件箱
	int typeTag;

	//get,set-------------------------------------------------------------------
	
	public String getUuid() {
		return uuid;
	}
	public int getTypeTag() {
		return typeTag;
	}
	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}
	public String getReceiverUserId() {
		return receiverUserId;
	}
	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}
	public String getReceiverUserName() {
		return receiverUserName;
	}
	public void setReceiverUserName(String receiverUserName) {
		this.receiverUserName = receiverUserName;
	}
	public String getReceiverUserSmallPicUrl() {
		return receiverUserSmallPicUrl;
	}
	public void setReceiverUserSmallPicUrl(String receiverUserSmallPicUrl) {
		this.receiverUserSmallPicUrl = receiverUserSmallPicUrl;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public boolean isReadTag() {
		return readTag;
	}
	public void setReadTag(boolean readTag) {
		this.readTag = readTag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}
	public String getSenderUserName() {
		return senderUserName;
	}
	public void setSenderUserName(String senderUserName) {
		this.senderUserName = senderUserName;
	}
	public String getSenderUserSmallPicUrl() {
		return senderUserSmallPicUrl;
	}
	public void setSenderUserSmallPicUrl(String senderUserSmallPicUrl) {
		this.senderUserSmallPicUrl = senderUserSmallPicUrl;
	}
	public boolean isCanReplyTag() {
		return canReplyTag;
	}
	public void setCanReplyTag(boolean canReplyTag) {
		this.canReplyTag = canReplyTag;
	}
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static UserMsgData toBean(String str) {

		UserMsgData dto = new UserMsgData();
		try {
			JSONObject jObj = new JSONObject(str);
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("createTime")) {
				dto.setCreateTime(jObj.getLong("createTime"));
			}
			if (jObj.has("readTag")) {
				dto.setReadTag(jObj.getBoolean("readTag"));
			}
			if (jObj.has("title")) {
				dto.setTitle(jObj.getString("title"));
				
			}
			if (jObj.has("detail")) {
				dto.setDetail(jObj.getString("detail"));
			}
			if (jObj.has("senderUserId")) {
				dto.setSenderUserId(jObj.getString("senderUserId"));
			}
			if (jObj.has("senderUserName")) {
				dto.setSenderUserName(jObj.getString("senderUserName"));
			}
			if (jObj.has("senderUserSmallPicUrl")) {
				dto.setSenderUserSmallPicUrl(jObj.getString("senderUserSmallPicUrl"));
			}
			if (jObj.has("receiverUserId")) {
				dto.setReceiverUserId(jObj.getString("receiverUserId"));
			}
			if (jObj.has("receiverUserName")) {
				dto.setReceiverUserName(jObj.getString("receiverUserName"));
			}
			if (jObj.has("receiverUserSmallPicUrl")) {
				dto.setReceiverUserSmallPicUrl(jObj.getString("receiverUserSmallPicUrl"));
			}
			if (jObj.has("canReplyTag")) {
				dto.setCanReplyTag(jObj.getBoolean("canReplyTag"));
			}
			if (jObj.has("typeTag")) {
				dto.setTypeTag(jObj.getInt("typeTag"));
			}

		} catch (JSONException e) {
			e.printStackTrace();

		}
		return dto;
	}

	

	
	
}
