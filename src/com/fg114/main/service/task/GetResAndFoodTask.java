package com.fg114.main.service.task;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResAndFoodList2DTO;
import com.fg114.main.service.dto.RestListDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

/**
 * 获得餐厅美食列表
 * @author zhangyifan
 *
 */
public class GetResAndFoodTask extends BaseTask {
	
	private static final String TAG = "GetResAndFoodTask";
	public static final boolean DEBUG = Settings.DEBUG;

	public RestListDTO dto;
	
	private boolean haveGpsTag = true;
	private double longitude = 0;
	private double latitude = 0;
	//---
	int distanceMeter=0;
	String regionId=""; 
	String districtId=""; 
	String mainMenuId="";
	String subMenuId="";
	String mainTopRestTypeId="";
	String subTopRestTypeId=""; 
	String keywords="";
	int avgTag=0; 
	int sortTypeTag=0; 
	int pageSize=25;
	int startIndex=1;
	
	public GetResAndFoodTask(
					String preDialogMessage, 
					Context context,
					String keywords,
					int startIndex) {
		super(preDialogMessage, context);
		
		//获得gps
		this.keywords = keywords;
		this.startIndex = startIndex;
	}

	@Override
	public JsonPack getData() throws Exception {

		//获得gps
		this.haveGpsTag = Loc.isGpsAvailable();
		if (this.haveGpsTag) {
			LocInfo myLoc = null;
			myLoc = Loc.getLoc();
			if (myLoc == null || myLoc.getLoc()==null) {
				this.haveGpsTag = false;
			} else {
				this.longitude = myLoc.getLoc().getLongitude();
				this.latitude = myLoc.getLoc().getLatitude();
			}
		}

		//以下是“搜索结果缓存”逻辑，搜索结果2分钟缓存，不忽略gps经纬度（精确到），但不忽略haveGpsTag标志
		//先构造缓存关键字
		//DecimalFormat decimalFormat = new DecimalFormat("###.####");
		StringBuilder sbKey=new StringBuilder();
		sbKey.append(ActivityUtil.getVersionName(context)).append('|');
		sbKey.append(ActivityUtil.getDeviceId(context)).append('|');
		sbKey.append(SessionManager.getInstance().getCityInfo(context).getId()).append('|');
		sbKey.append(haveGpsTag).append('|');
		sbKey.append(GeoUtils.formatLongLat(longitude)).append('|');
		sbKey.append(GeoUtils.formatLongLat(latitude)).append('|');
		sbKey.append(distanceMeter).append('|');
		sbKey.append(regionId).append('|');
		sbKey.append(districtId).append('|');
		sbKey.append(mainMenuId).append('|');
		sbKey.append(subMenuId).append('|');
		sbKey.append(keywords).append('|');
		sbKey.append(sortTypeTag).append('|');
		sbKey.append(avgTag).append('|');
		sbKey.append(mainTopRestTypeId).append('|');
		sbKey.append(subTopRestTypeId).append('|');
		sbKey.append(pageSize).append('|');
		sbKey.append(startIndex).append('|');
		String key=sbKey.toString();
		ValueObject vo=ValueCacheUtil.getInstance(context).get(Settings.SEARCH_RESULTS, key);
		JsonPack jpSearchResults = new JsonPack();
		
		
		
		if(vo!=null && !vo.isExpired()){
			jpSearchResults.setObj(new JSONObject(vo.getValue()));
			dto = JsonUtils.fromJson(vo.getValue(), RestListDTO.class);
		}
		else{
			
			jpSearchResults=ServiceRequest.getRestList2(
					distanceMeter, //附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
					regionId, //地域ID ,约定"" ：全部地域                其他：所选地域ID
					districtId,//商区ID,约定""  :全部商区                   其他：所选商区ID
					mainMenuId,//主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
					subMenuId,//子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
					mainTopRestTypeId,//主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
					subTopRestTypeId,//子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
					keywords,
					avgTag,//人均  0：默认
					sortTypeTag,//排序  0：默认
					pageSize,// 页面大小
					startIndex // 当前页
					);
			
			//由于底层可以返回200并且obj==null的情况，这里做个保护，以免后面抛出空指针异常
			if(jpSearchResults==null||(jpSearchResults.getRe()==200 && jpSearchResults.getObj()==null)){
				jpSearchResults = new JsonPack();
				jpSearchResults.setRe(400); //设置为400后可以重试，见BaseTask
				jpSearchResults.setMsg("获取数据时发生网络异常!");
				return jpSearchResults;
			}
			String value=jpSearchResults.getObj().toString();
			dto = JsonUtils.fromJson(value, RestListDTO.class);
			
			//如果结果正确，存入缓存
			if(jpSearchResults.getRe()==200){
				ValueCacheUtil.getInstance(context).remove(Settings.SEARCH_RESULTS, key);
				ValueCacheUtil.getInstance(context).add(Settings.SEARCH_RESULTS, key, value, "0", "-", 2); //2分钟超时
			}
		}
		
		return jpSearchResults;
	}
	
	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}

	@Override
	public void onStateFinish(JsonPack result) {
		
	}

	@Override
	public void onPreStart() {
		
	}
	
	
}
