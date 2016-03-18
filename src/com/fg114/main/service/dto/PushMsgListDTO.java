package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.baidu.android.pushservice.PushConstants;


public class PushMsgListDTO {
	
	//列表
	private List<PushMsgPackDTO> list = new ArrayList<PushMsgPackDTO>();
	
	//下次访问间隔秒数 
	private long nextVisitSeconds;

	//get,set-------------------------------------------------------------------
	public List<PushMsgPackDTO> getList() {
		return list;
	}

	public void setList(List<PushMsgPackDTO> list) {
		this.list = list;
	}

	public long getNextVisitSeconds() {
		return nextVisitSeconds;
	}

	public void setNextVisitSeconds(long nextVisitSeconds) {
		this.nextVisitSeconds = nextVisitSeconds;
	}

	
	
}
