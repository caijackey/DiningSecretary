package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 榜单类别父类 对象
 * @author qianjiefeng
 *
 */
public class ParentTopResListTypeData  {
	 //大类id
	private String uuid = "";
	//大类名称
	private String name = "";
	//子类列表 
	private List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
	
	
	//get,set-------------------------------------------------------------------
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<CommonTypeDTO> getList() {
		return list;
	}
	public void setList(List<CommonTypeDTO> list) {
		this.list = list;
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
	public static ParentTopResListTypeData toBean(JSONObject jObj) {
		
		ParentTopResListTypeData dto = new ParentTopResListTypeData();

		try {
			
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("list")) {
				List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
				if (!jObj.isNull("list")) {
					JSONArray jsonArray = jObj.getJSONArray("list");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i ++) {
							list.add(
									CommonTypeDTO.toBean(
											jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setList(list);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
	
}
