package com.fg114.main.service.task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.MealComboFilter;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.CashCouponList2DTO;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResAndFoodList2DTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

/**
 * 获得特惠套餐列表
 * 
 * @author xujianjun,2012-07-23
 * 
 */
public class GetMealComboListTask extends BaseTask {

	private static final String TAG = "GetMealComboListTask";
	public static final boolean DEBUG = Settings.DEBUG;

	public CashCouponList2DTO dto;
	//private int distanceMeter=0;// 附近距离 约定： 500 1000米 2000 5000
	
	private int startIndex = 1;
	private int pageSize = 20;
	private boolean isMealCombo ;
	private MealComboFilter mFilter;

	public GetMealComboListTask(String preDialogMessage, Context context, int startIndex,MealComboFilter filter, boolean isMealCombo) {
		super(preDialogMessage, context);
		this.startIndex = startIndex;
		this.mFilter=filter;
		this.isMealCombo=isMealCombo;
	}

	@Override
	public JsonPack getData() throws Exception {

		String cityId = SessionManager.getInstance().getCityInfo(context).getId();
		boolean haveGpsTag = Loc.isGpsAvailable();
		double longitude = 0;
		double latitude = 0;
		//
		if (haveGpsTag) {
			LocInfo myLoc = Loc.getLoc();
			if (myLoc == null || myLoc.getLoc() == null) {
				haveGpsTag = false;
			} else {
				longitude = myLoc.getLoc().getLongitude();
				latitude = myLoc.getLoc().getLatitude();
			}
		}
		JsonPack jp = new JsonPack();
		
		// 以下是“搜索结果缓存”逻辑，搜索结果2分钟缓存，不忽略gps经纬度（精确到），但不忽略haveGpsTag标志
		// 先构造缓存关键字
//		StringBuilder sbKey = new StringBuilder();
//		sbKey.append(ActivityUtil.getVersionName(context)).append('|');
//		sbKey.append(ActivityUtil.getDeviceId(context)).append('|');
//		sbKey.append(cityId).append('|');
//		sbKey.append(haveGpsTag).append('|');
//		sbKey.append(GeoUtils.formatLongLat(longitude)).append('|');
//		sbKey.append(GeoUtils.formatLongLat(latitude)).append('|');
//		sbKey.append(distanceMeter).append('|');
//		sbKey.append(typeTag).append('|');
//		sbKey.append(pageSize).append('|');
//		sbKey.append(pageNo).append('|');
//		String key = sbKey.toString();
//		// 当前逻辑：不缓存
//		ValueObject vo = ValueCacheUtil.getInstance(context).get(Settings.SEARCH_RESULTS, key);
//		
//
//		if (vo != null && !vo.isExpired()) {
//			jp.setRe(200);
//			dto = JsonUtils.fromJson(vo.getValue(), MealComboListDTO.class);
//			return jp;
//		}

//		jp = A57HttpApiV3.getInstance().getMealComboList3(
//				cityId, 
//				haveGpsTag, 
//				longitude, 
//				latitude, 
//				isMealCombo?2:1,
//				mFilter.getDistanceMeter(),
//				mFilter.getRegionId(),
//				mFilter.getMainMenuId(),
//				mFilter.getSortTypeTag(), 
//				pageSize, 
//				pageNo);
		//jp = A57HttpApiV3.getInstance().getMealComboList(cityId, haveGpsTag, longitude, latitude, 0,0, pageSize, pageNo);

//		// 如果结果正确，存入缓存。当前逻辑：不缓存
//		if (jp.getRe() == 200) {
//
//			ValueCacheUtil.getInstance(context).remove(Settings.SEARCH_RESULTS, key);
//			ValueCacheUtil.getInstance(context).add(Settings.SEARCH_RESULTS, key, jp.getObj().toString(), "0", "-", 2); // 2分钟超时
//
//		}
		int sortTypeTag = 0;
		try {
			sortTypeTag = Integer.parseInt(mFilter.getSortTypeTag());
		} catch (Exception e) {
			sortTypeTag = 1;
		}
		jp = ServiceRequest.getCashCouponList2(1, mFilter.getDistanceMeter(), mFilter.getRegionId(), mFilter.getMainMenuId(), sortTypeTag, pageSize, startIndex);
//		dto = MealComboList2DTO.toBean(jp.getObj());
		//Log.e("bug",jp.getObj().toString());
		
		
//		if(dto==null){
//			jp.setRe(400);
//			jp.setMsg("数据转换时发生错误");
//		}
		
		//设置时间戳
//		dto.setListTimestamp(SystemClock.elapsedRealtime()); 
		return jp;

	}

	
	@Override
	public void onPreStart() {

	}

	@Override
	public void onStateFinish(JsonPack result) {

//		Log.e("bug", result.getObj().toString());
		if(result != null && result.getObj() != null){
			dto = JsonUtils.fromJson(result.getObj().toString(), CashCouponList2DTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}

	@Override
	public void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}

}
