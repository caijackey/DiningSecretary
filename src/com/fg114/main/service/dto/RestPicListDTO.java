package com.fg114.main.service.dto;

import java.util.List;

/**
 * 图片列表DTO
 * @author qianjiefeng
 *
 */
public class RestPicListDTO extends BaseDTO {
	//列表 
	List<RestPicData> list;

	public List<RestPicData> getList() {
		return list;
	}

	public void setList(List<RestPicData> list) {
		this.list = list;
	}
	
	
}
