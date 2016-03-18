package com.fg114.main.service.dto;

import java.util.*;

import org.json.*;

import com.fg114.main.app.view.ItemData;

/**
 * 通用类别列表DTO
 * @author qianjiefeng
 */
public class RfTypeListDTO implements ItemData{
	public String u = "";// uuid
	public String n = "";// name
	private boolean s;// selectTag
	// 列表
	private List<RfTypeDTO> list = new ArrayList<RfTypeDTO>();

	// 自用属性
	private RfTypeDTO mainDto; // 主dto对象，根据uuid,name,selectTag三个属性构造
	private String m = ""; // 备注
	private boolean needGroupBy;
	private String firstLetter;
	private String parentId;

	// get,set-------------------------------------------------------------------
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

	public List<RfTypeDTO> getList() {
		return list;
	}

	public void setList(List<? extends ItemData> list) {
		this.list = (List<RfTypeDTO>)list;
	}

	public String getMemo() {
		return m;
	}

	public void setMemo(String memo) {
		this.m = memo;
	}

	public RfTypeDTO getMainDto() {
		if (mainDto == null) {
			mainDto = new RfTypeDTO();
			mainDto.setUuid(u);
			mainDto.setName(n);
			mainDto.setSelectTag(s);
			mainDto.setMemo(m);
		}
		return mainDto;
	}

	public void setMainDto(RfTypeDTO mainDto) {
		this.mainDto = mainDto;
	}

	public static RfTypeListDTO toBean(JSONObject jObj) {
		RfTypeListDTO dto = new RfTypeListDTO();
		try {
			if (jObj.has("list")) {
				List<RfTypeDTO> list = new ArrayList<RfTypeDTO>();
				if (!jObj.isNull("list")) {
					JSONArray jsonArray = jObj.getJSONArray("list");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i++) {
							list.add(RfTypeDTO.toBean(jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setList(list);
			}
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
	public RfTypeListDTO clone() {
		RfTypeListDTO newDto = new RfTypeListDTO();
		newDto.setUuid(getUuid());
		newDto.setName(getName());
		newDto.setSelectTag(isSelectTag());
		newDto.setMemo(getMemo());
		List<RfTypeDTO> newList = new ArrayList<RfTypeDTO>();
		for (RfTypeDTO dto : getList()) {
			newList.add(dto.clone());
		}
		newDto.setList(newList);
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
	public void setIsNeedGroupBy(boolean needGroupBy) {
		this.needGroupBy=needGroupBy;
	}

	@Override
	public boolean isNeedGroupBy() {
		return needGroupBy;
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