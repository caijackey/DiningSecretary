package com.fg114.main.service.dto;

import org.json.JSONObject;


//餐厅电话类
public class ResTelInfo{
	//区号
	public String cityPrefix="";
	//是否可拨打
	public int isTelCanCall=0;
	//电话
	public String tel="";
	//分机
	public String branch="";
	public static ResTelInfo toBean(JSONObject jObj) {
		ResTelInfo info = new ResTelInfo();

		try {
			if (jObj.has("cityPrefix")) {
				info.cityPrefix=jObj.getString("cityPrefix");
			}
			if (jObj.has("isTelCanCall")) {
				info.isTelCanCall=jObj.getInt("isTelCanCall");
			}
			if (jObj.has("tel")) {
				info.tel=jObj.getString("tel");
			}
			if (jObj.has("branch")) {
				info.branch=jObj.getString("branch");
			}
			return info;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}