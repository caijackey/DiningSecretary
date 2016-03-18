package com.fg114.main.service.dto;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author qianjiefeng
 *
 */
public class UserAccountInOutData {
	// uuid
	private String uuid;
	//创建时间
	private long createTime;
	//描述
	private String description;
	//类别名称
	private String typeName;
	//帐户余额
	private String remainMoney;

	//get,set-------------------------------------------------------------------
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getRemainMoney() {
		return remainMoney;
	}
	public void setRemainMoney(String remainMoney) {
		this.remainMoney = remainMoney;
	}

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static UserAccountInOutData toBean(JSONObject jObj) {
		
		UserAccountInOutData dto = new UserAccountInOutData();
		
		try {
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("createTime")) {
				dto.setCreateTime(jObj.getLong("createTime"));
			}
			if (jObj.has("description")) {
				dto.setDescription(jObj.getString("description"));
			}
			if (jObj.has("typeName")) {
				dto.setTypeName(jObj.getString("typeName"));
			}
			if (jObj.has("remainMoney")) {
				dto.setRemainMoney(jObj.getString("remainMoney"));
			}
			
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
	
}
