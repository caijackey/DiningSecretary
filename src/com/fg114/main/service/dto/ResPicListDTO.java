package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.JsonUtils;

/**
 * 图片列表DTO
 * @author qianjiefeng
 *
 */
public class ResPicListDTO extends BaseDTO {
	
	//列表 
	private List<ResPicData> list = new ArrayList<ResPicData>();
	
	//get,set-------------------------------------------------------------------
	
	public List<ResPicData> getList() {
		return list;
	}


	public void setList(List<ResPicData> list) {
		this.list = list;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static ResPicListDTO toBean(JSONObject jObj) {
		
		return JsonUtils.fromJson(jObj.toString(), ResPicListDTO.class);
	}
	
}
