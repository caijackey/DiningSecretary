package com.fg114.main.analytics.dto;

import java.util.ArrayList;
import java.util.List;

import com.fg114.main.util.JsonUtils;

/**
 * @author qianjiefeng
 *
 */
public class PageStatsPackData  {
	//页面统计列表
	public List<PageStatsData> list=new ArrayList<PageStatsData>();
	public String toString(){
		return JsonUtils.toJson(this);
	}
}
