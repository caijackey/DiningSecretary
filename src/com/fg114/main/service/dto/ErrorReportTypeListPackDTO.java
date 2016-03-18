package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.JsonUtils;

/**
 * 榜单类别父类 对象
 * @author qianjiefeng
 *
 */
public class ErrorReportTypeListPackDTO extends BaseDTO {
	//列表 
	private List<ErrorReportTypeListDTO> list = new ArrayList<ErrorReportTypeListDTO>();
	
	//get,set-------------------------------------------------------------------
	public List<ErrorReportTypeListDTO> getList() {
		return list;
	}

	public void setList(List<ErrorReportTypeListDTO> list) {
		this.list = list;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ErrorReportTypeListPackDTO toBean(JSONObject jObj) {
		
		return JsonUtils.fromJson(jObj.toString(), ErrorReportTypeListPackDTO.class);
	}
	
	
}
