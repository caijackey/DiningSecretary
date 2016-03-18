package com.fg114.main.app.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.app.activity.resandfood.RestaurantUploadActivity;
import com.fg114.main.service.dto.UploadData;
import com.fg114.main.service.task.UploadImageTask;
import com.fg114.main.util.SessionManager;

public class UploadDataPack {

	public String uploadType="";
	public UploadData uploadData=null;
	
	public String restName = "";
	public String restId = "";
	
	public String foodId="";
	public String foodName="";	
	
	public String price="";
	public String unit="";
	public int foodScore;
	
	public int overallNum;	//总体评价      0为未选择
	public int tasteNum;	//口味      0为未选择
	public int envNum;		//环境       0为未选择
	public int serviceNum;	//服务      0为未选择
	
	public String comment="";
	public String shareString="";
	public String shareTo;
}
