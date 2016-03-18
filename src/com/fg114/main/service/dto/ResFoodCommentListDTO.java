package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;


public class ResFoodCommentListDTO extends BaseDTO {
	//列表 
	private List<ResFoodCommentData> list = new ArrayList<ResFoodCommentData>();

	//get,set-------------------------------------------------------------------
	public List<ResFoodCommentData> getList() {
		return list;
	}

	public void setList(List<ResFoodCommentData> list) {
		this.list = list;
	}

	
	
}
