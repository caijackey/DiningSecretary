package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 错误报告类别列表DTO
 * @author qianjiefeng
 *
 */
public class ErrorReportTypeListDTO extends BaseDTO {
	private int typeTag;
	//餐馆错误类别列表
	private List<ErrorReportTypeData> list = new ArrayList<ErrorReportTypeData>();

	
	
	//get,set-------------------------------------------------------------------
	public int getTypeTag() {
		return typeTag;
	}
	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}
	public List<ErrorReportTypeData> getList() {
		return list;
	}
	public void setList(List<ErrorReportTypeData> list) {
		this.list = list;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ErrorReportTypeListDTO toBean(JSONObject jObj) {
		
		ErrorReportTypeListDTO dto = new ErrorReportTypeListDTO();

		try {
			
			if (jObj.has("list")) {
				List<ErrorReportTypeData> list = new ArrayList<ErrorReportTypeData>();
				if (!jObj.isNull("list")) {
					JSONArray jsonArray = jObj.getJSONArray("list");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i ++) {
							list.add(
									ErrorReportTypeData.toBean(
											jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setList(list);
			}
			if (jObj.has("typeTag")) {
				dto.setTypeTag(jObj.getInt("typeTag"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
	
}
