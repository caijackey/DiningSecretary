package com.fg114.main.app.data;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseData {

	private String id = "";
	private String parentId = "";
	private String name = "";
	//数量
	private int num;
	private int rank;	
	
	// 是否选中
	private boolean selectTag;
	
	
	public boolean isSelectTag() {
		return selectTag;
	}

	public void setSelectTag(boolean selectTag) {
		this.selectTag = selectTag;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isBlank() {
		if (this.id == null || "".equals(this.id)) {
			return true;
		}
		return false;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static BaseData toBean(JSONObject jObj) {
		
		BaseData data = new BaseData();
		try {
			if (jObj.has("id")) {
				data.setId(jObj.getString("id"));
			}
			if (jObj.has("name")) {
				data.setName(jObj.getString("name"));
			}
			if (jObj.has("parentId")) {
				data.setParentId(jObj.getString("parentId"));
			}
			if (jObj.has("rank")) {
				data.setRank(jObj.getInt("rank"));
			}
			if (jObj.has("num")) {
				data.setNum(jObj.getInt("num"));
			}
			if (jObj.has("selectTag")) {
				data.setSelectTag(jObj.getBoolean("selectTag"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return data;
	}
}
