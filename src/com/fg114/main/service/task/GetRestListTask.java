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
public class GetRestListTask extends BaseTask {
	
	private static final String TAG = "GetRestListTask";
	public static final boolean DEBUG = Settings.DEBUG;

//	public ResAndFoodListDTO dto;
	public RestListDTO dto2;
	
	private boolean haveGpsTag = true;
	private double longitude = 0;
	private double latitude = 0;
	private int startIndex = 1;
	private boolean mIsUseCellLoc = false;
	
	public GetRestListTask(
					String preDialogMessage, 
					Context context,
					boolean haveGpsTag,
					int pageNo,
					boolean isUseCellLoc) {
		super(preDialogMessage, context);
		
		//获得gps
		this.haveGpsTag = haveGpsTag;
		this.startIndex = pageNo;
		mIsUseCellLoc = isUseCellLoc;
	}

	@Override
	public JsonPack getData() throws Exception {

		//获得gps
		this.haveGpsTag = Loc.isGpsAvailable();
		if (this.haveGpsTag) {
			LocInfo myLoc = null;
			if (mIsUseCellLoc) {
				myLoc = Loc.getCellLoc();
				if (myLoc == null || myLoc.getLoc()==null) {
					myLoc = Loc.getLoc();
				}
			}
			else {
				myLoc = Loc.getLoc();
			}
			if (myLoc == null || myLoc.getLoc()==null) {
				this.haveGpsTag = false;
			} else {
				this.longitude = myLoc.getLoc().getLongitude();
				this.latitude = myLoc.getLoc().getLatitude();
				//info = myLoc.getInfo();
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
		sbKey.append(SessionManager.getInstance().getFilter().isSubwayTag()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getDistanceMeter()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getRestId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getRegionId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getDistrictId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getChannelId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getMainMenuId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getSubMenuId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getKeywords()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getSortTypeTag()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getAvgTag()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getMainTopRestTypeId()).append('|');
		sbKey.append(SessionManager.getInstance().getFilter().getSubTopRestTypeId()).append('|');
		sbKey.append(Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE).append('|');
		sbKey.append(startIndex).append('|');
		String key=sbKey.toString();
		ValueObject vo=ValueCacheUtil.getInstance(context).get(Settings.SEARCH_RESULTS, key);
		JsonPack jpSearchResults = new JsonPack();
		
		
		
		if(vo!=null && !vo.isExpired()){
			jpSearchResults.setObj(new JSONObject(vo.getValue()));
			dto2 = JsonUtils.fromJson(vo.getValue(),RestListDTO.class);
			
			// 设置请求的url为"Cache"，用于页面点击追踪统计
			jpSearchResults.setUrl("Cache");
		}
		else{
			jpSearchResults=ServiceRequest.getRestList4(
					SessionManager.getInstance().getFilter().isSubwayTag(), 
					SessionManager.getInstance().getFilter().getDistanceMeter(), 
					SessionManager.getInstance().getFilter().getRestId(),
					SessionManager.getInstance().getFilter().getRegionId(), 
					SessionManager.getInstance().getFilter().getDistrictId(),
					SessionManager.getInstance().getFilter().getMainMenuId(),
					SessionManager.getInstance().getFilter().getSubMenuId(),
					SessionManager.getInstance().getFilter().getMainTopRestTypeId(),
					SessionManager.getInstance().getFilter().getSubTopRestTypeId(),
					SessionManager.getInstance().getFilter().getKeywords(),
					Integer.parseInt(SessionManager.getInstance().getFilter().getAvgTag()),
					SessionManager.getInstance().getFilter().getSortTypeTag(),
					Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE,
					startIndex);
			
			//由于底层可以返回200并且obj==null的情况，这里做个保护，以免后面抛出空指针异常
			if(jpSearchResults==null||(jpSearchResults.getRe()==200 && jpSearchResults.getObj()==null)){
				jpSearchResults = new JsonPack();
				jpSearchResults.setRe(400); //设置为400后可以重试，见BaseTask
				jpSearchResults.setMsg("获取数据时发生网络异常!");
				return jpSearchResults;
			}
			
			dto2 = JsonUtils.fromJson(jpSearchResults.getObj().toString(),RestListDTO.class); 
			
			//如果结果正确，存入缓存
			if(jpSearchResults.getRe()==200){
				ValueCacheUtil.getInstance(context).remove(Settings.SEARCH_RESULTS, key);
				ValueCacheUtil.getInstance(context).add(Settings.SEARCH_RESULTS, key, jpSearchResults.getObj().toString(), "0", "-", 20); //20分钟超时
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
