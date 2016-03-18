package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.*;

import com.fg114.main.app.view.ItemData;

/**
 * 通用类别DTO
 * @author qianjiefeng
 *
 */
public class RfTypeDTO implements ItemData{

	public String u = "";
	public String n = "";
	private boolean s;

	// 自用属性
	private String m = ""; // 备注
	private String firstLetter;
	private String parentId;
	
	//get,set-------------------------------------------------------------------
	public String getUuid() {
		return u;
	}
	public void setUuid(String u) {
		this.u = u;
	}
	public String getName() {
		return n;
	}
	public void setName(String n) {
		this.n = n;
	}
	public boolean isSelectTag() {
		return s;
	}
	public void setSelectTag(boolean s) {
		this.s = s;
	}
	public String getMemo() {
		return m;
	}
	public void setMemo(String memo) {
		this.m = memo;
	}
	
	public static RfTypeDTO toBean(JSONObject jObj) {
		RfTypeDTO dto = new RfTypeDTO();
		try {
			if (jObj.has("u")) {
				dto.setUuid(jObj.getString("u"));
			}
			if (jObj.has("n")) {
				dto.setName(jObj.getString("n"));
			}
			if (jObj.has("s")) {
				dto.setSelectTag(jObj.getBoolean("s"));
			}
			if (jObj.has("m")) {
				dto.setMemo(jObj.getString("m"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}
	
	@Override
	public RfTypeDTO clone() {
		RfTypeDTO newDto = new RfTypeDTO();
		newDto.setUuid(getUuid());
		newDto.setName(getName());
		newDto.setSelectTag(isSelectTag());
		newDto.setMemo(getMemo());
		return newDto;
	}
	@Override
	public String getParentId() {
		return this.parentId;
	}
	@Override
	public void setParentId(String parentId) {
		this.parentId=parentId;
	}
	@Override
	public List<? extends ItemData> getList() {
		return null;
	}
	@Override
	public void setList(List<? extends ItemData> list) {
		
	}
	@Override
	public void setIsNeedGroupBy(boolean needGroupBy) {
		
	}
	@Override
	public boolean isNeedGroupBy() {
		return false;
	}
	@Override
	public void setFirstLetter(String firstLetter) {
		this.firstLetter=firstLetter;			
	}

	@Override
	public String getFirstLetter() {
		return this.firstLetter;
	}
}