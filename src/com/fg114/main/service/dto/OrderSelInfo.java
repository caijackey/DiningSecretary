package com.fg114.main.service.dto;

import java.util.List;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class OrderSelInfo  {
	
	//最大选择天数
	//本地默认值 90
	//默认选择为当天
	private int maxDayNum=90;
	
	//最大选择的人数  
	//默认值  49;
	//默认选择4
	private int maxPeopleNum=49;

	public int getMaxDayNum() {
		return maxDayNum>1?maxDayNum:90;
	}

	public void setMaxDayNum(int maxDayNum) {
		this.maxDayNum = maxDayNum;
	}

	public int getMaxPeopleNum() {
		return maxPeopleNum>1?maxPeopleNum:49;
	}

	public void setMaxPeopleNum(int maxPeopleNum) {
		this.maxPeopleNum = maxPeopleNum;
	}
		
	//时间  
	//本地默认值 11:00 ~ 20:30
	//默认选择 <18:00  为 18:15  >=18:00 为当前时间+15分钟   补上最近刻钟的分钟     如果超出20:30  就设置为下一天                 比如 18:35 为 19:00  
	
	//房间  
	//本地默认值   0:只订大厅  1：只订包房  2：优先大厅  3：优先包房
	//默选择第一项
	
}
