package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fg114.main.util.JsonUtils;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class ShRegionListDTO extends BaseDTO {
	//列表
	List<RfTypeListDTO> list = new ArrayList<RfTypeListDTO>();
	
	public List<RfTypeListDTO> getList() {
		return list;
	}
	
	public void setList(List<RfTypeListDTO> list) {
		this.list = list;
	}

	public static ShRegionListDTO toBean(JSONObject jObj) {
		return JsonUtils.fromJson(jObj.toString(), ShRegionListDTO.class);
	}

	@Override
	public ShRegionListDTO clone() {
		ShRegionListDTO newDto = new ShRegionListDTO();
		newDto.needUpdateTag=needUpdateTag;
		newDto.timestamp=timestamp;
		newDto.pgInfo=pgInfo;
		List<RfTypeListDTO> newList = new ArrayList<RfTypeListDTO>();
		for (RfTypeListDTO dto : getList()) {
			newList.add(dto.clone());
		}
		newDto.setList(newList);
		return newDto;
	}
}
