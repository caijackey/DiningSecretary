package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.JsonUtils;


public class TakeoutRestListDTO extends BaseDTO {
	//列表 
	private List<TakeoutRestListData> list = new ArrayList<TakeoutRestListData>();
	//类别列表
	private List<CommonTypeDTO> typeList = new ArrayList<CommonTypeDTO>();

	//get,set-------------------------------------------------------------------
	public List<TakeoutRestListData> getList() {
		return list;
	}

	public void setList(List<TakeoutRestListData> list) {
		this.list = list;
	}

	public List<CommonTypeDTO> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<CommonTypeDTO> typeList) {
		this.typeList = typeList;
	}
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static TakeoutRestListDTO toBean(JSONObject jObj) {

		return JsonUtils.fromJson(jObj.toString(), TakeoutRestListDTO.class);
	}
	
	
}
