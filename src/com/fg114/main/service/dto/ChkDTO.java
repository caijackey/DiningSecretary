package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 校验DTO
 * @author qianjiefeng
 *
 */
public class ChkDTO  {
	//是否成功
	private boolean succTag = false;  //默认为true
	//错误提示
	private String msg = "";
	//那个字段出错
	private String fieldName = "";
	
	//get,set-------------------------------------------------------------------
	public boolean isSuccTag() {
		return succTag;
	}
	public void setSuccTag(boolean succTag) {
		this.succTag = succTag;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ChkDTO toBean(JSONObject jObj) {
		
		ChkDTO dto = new ChkDTO();

		try {
			
			if (jObj.has("succTag")) {
				dto.setSuccTag(jObj.getBoolean("succTag"));
			}
			if (jObj.has("msg")) {
				dto.setMsg(jObj.getString("msg"));
			}
			if (jObj.has("fieldName")) {
				dto.setFieldName(jObj.getString("fieldName"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
}
