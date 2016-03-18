package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class ChatMsgListDto extends BaseDTO {
	//列表
	List<ChatMsgData> list = new ArrayList<ChatMsgData>();

	public List<ChatMsgData> getList() {
		return list;
	}

	public void setList(List<ChatMsgData> list) {
		this.list = list;
	}
}
