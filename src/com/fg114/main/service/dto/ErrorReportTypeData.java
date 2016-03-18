package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 错误报告类别信息
 * 
 * @author qianjiefeng
 * 
 */
public class ErrorReportTypeData implements Serializable {
	//类别ID 
	private String typeId;
	//类别名称
	private String typeName;
	//输入框标题
	private String inputBoxTitle;
	//功能标志
	private int funcTag; //1:默认  2:单行文本框  3:输入多行文本和email      10:商户信息错误 11:地图纠正
	//键盘类型
	private int keyboardTypeTag; //1:默认   2:数字

	//是否需要确认 当 funcTag==1时 用
	private boolean needConfirmTag;
	//确认提示
	private String comfirmHint;

	public boolean isNeedConfirmTag() {
		return needConfirmTag;
	}

	public void setNeedConfirmTag(boolean needConfirmTag) {
		this.needConfirmTag = needConfirmTag;
	}

	public String getComfirmHint() {
		return comfirmHint;
	}

	public void setComfirmHint(String comfirmHint) {
		this.comfirmHint = comfirmHint;
	}

	//get,set-------------------------------------------------------------------
	public String getTypeId() {
		return typeId;
	}

	public int getKeyboardTypeTag() {
		return keyboardTypeTag;
	}

	public void setKeyboardTypeTag(int keyboardTypeTag) {
		this.keyboardTypeTag = keyboardTypeTag;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getInputBoxTitle() {
		return inputBoxTitle;
	}

	public void setInputBoxTitle(String inputBoxTitle) {
		this.inputBoxTitle = inputBoxTitle;
	}

	public int getFuncTag() {
		return funcTag;
	}

	public void setFuncTag(int funcTag) {
		this.funcTag = funcTag;
	}

	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static ErrorReportTypeData toBean(JSONObject jObj) {

		ErrorReportTypeData dto = new ErrorReportTypeData();

		try {

			if (jObj.has("typeId")) {
				dto.setTypeId(jObj.getString("typeId"));
			}
			//
			if (jObj.has("typeName")) {
				dto.setTypeName(jObj.getString("typeName"));
			}
			//
			if (jObj.has("inputBoxTitle")) {
				dto.setInputBoxTitle(jObj.getString("inputBoxTitle"));
			}
			//
			if (jObj.has("funcTag")) {
				dto.setFuncTag(jObj.getInt("funcTag"));
			}
			//
			if (jObj.has("keyboardTypeTag")) {
				dto.setKeyboardTypeTag(jObj.getInt("keyboardTypeTag"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}

}
