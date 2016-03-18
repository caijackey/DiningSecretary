package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;

import android.content.*;
import android.os.*;
import android.util.Log;

import com.fg114.main.app.*;
import com.fg114.main.app.location.*;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.util.*;

/**
 * 获得餐厅美食列表
 * @author zhangyifan
 *
 */
public class GetRealTimeTableRestListTask extends BaseTask {
	
	private static final String TAG = GetRealTimeTableRestListTask.class.getSimpleName();
	public static final boolean DEBUG = Settings.DEBUG;

	public RealTimeTableRestListDTO dto;
	
	private boolean haveGpsTag = true;
	private double longitude = 0;
	private double latitude = 0;
	private int pageNo = 1;
	
	public GetRealTimeTableRestListTask(
					String preDialogMessage, 
					Context context,
					boolean haveGpsTag,
					int pageNo) {
		super(preDialogMessage, context);
		
		//获得gps
		this.haveGpsTag = haveGpsTag;
		this.pageNo = pageNo;
	}

	@Override
	public JsonPack getData() throws Exception {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		} else {
			//继续执行
			if (DEBUG) Log.d(TAG, "looper已存在");
		}

		//获得gps
		this.haveGpsTag = Loc.isGpsAvailable();
		if (this.haveGpsTag) {
			LocInfo myLoc = Loc.getLoc();
			if (myLoc == null || myLoc.getLoc()==null) {
//				DialogUtil.showToast(this.context, this.context.getString(R.string.text_info_null_location));
				this.haveGpsTag = false;
			} else {
				this.longitude = myLoc.getLoc().getLongitude();
				this.latitude = myLoc.getLoc().getLatitude();
			}
		}
		
		JsonPack jpSearchResults = A57HttpApiV3.getInstance().getRealTimeTableRestList(
				SessionManager.getInstance().getCityInfo(context).getId(),
				haveGpsTag,
				longitude, 
				latitude, 
				SessionManager.getInstance().getRealTimeResFilter().getDistanceMeter(), 
				SessionManager.getInstance().getRealTimeResFilter().getRegionId(), 
				SessionManager.getInstance().getRealTimeResFilter().getDistrictId(),
				SessionManager.getInstance().getRealTimeResFilter().getMainMenuId(),
				SessionManager.getInstance().getRealTimeResFilter().getSubMenuId(),
				SessionManager.getInstance().getRealTimeResFilter().getSelectTime(),
				SessionManager.getInstance().getRealTimeResFilter().getSortTypeTag(),
				SessionManager.getInstance().getRealTimeResFilter().getAvgTag(),
				Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE,
				pageNo,
				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllRegionListTimestamp(),
				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllMenuTypeListTimestamp());
		
		//由于底层可以返回200并且obj==null的情况，这里做个保护，以免后面抛出空指针异常
		if(jpSearchResults==null||(jpSearchResults.getRe()==200 && jpSearchResults.getObj()==null)){
			jpSearchResults = new JsonPack();
			jpSearchResults.setRe(400); //设置为400后可以重试，见BaseTask
			jpSearchResults.setMsg("获取数据时发生网络异常!");
			return jpSearchResults;
		}
		
		dto = RealTimeTableRestListDTO.toBeanFromSearchResult(jpSearchResults.getObj());
		
		// 测试
//		JsonPack jpSearchResults = A57HttpApiV3.getInstance().getResAndFoodList2(
//				SessionManager.getInstance().getCityInfo(context).getId(),
//				haveGpsTag,
//				longitude, 
//				latitude, 
//				SessionManager.getInstance().getRealTimeResFilter().getDistanceMeter(), 
//				SessionManager.getInstance().getRealTimeResFilter().getRegionId(), 
//				SessionManager.getInstance().getRealTimeResFilter().getDistrictId(),
//				"1",
//				SessionManager.getInstance().getRealTimeResFilter().getMainMenuId(),
//				SessionManager.getInstance().getRealTimeResFilter().getSubMenuId(),
//				"",
//				"",
//				"",
////				SessionManager.getInstance().getRealTimeResFilter().getSelectTime(),
//				SessionManager.getInstance().getRealTimeResFilter().getSortTypeTag(),
//				SessionManager.getInstance().getRealTimeResFilter().getAvgTag(),
//				Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE,
//				pageNo,
//				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllRegionListTimestamp(),
//				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllTopRestTypeListTimestamp(),
//				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllMenuTypeListTimestamp());
//		
//		if(jpSearchResults.getRe()==200){
//			ResAndFoodList2DTO dto1 = ResAndFoodList2DTO.toBeanFromSearchResult(jpSearchResults.getObj());
//			dto = new RealTimeTableRestListDTO();
//			if (dto1.getList().size() > 0) {
//				for (int i=0; i<dto1.getList().size(); i++) {
//					ResAndFoodData dataTmp = dto1.getList().get(i);
//					RealTimeTableRestData data = new RealTimeTableRestData();
//					data.setRestId(dataTmp.getResId());
//					data.setRestName(dataTmp.getResName());
//					data.setPicUrl(dataTmp.getPicUrl());
//					data.setAvgPrice(dataTmp.getAvgPrice());
//					data.setLikePct(String.valueOf((i + 50) % 100) + "%");
//					data.setDistanceMeter(dataTmp.getDistanceMeter());
//					
//					data.getRoomState().setHallTag(i % 4 + 1);
//					if (data.getRoomState().getHallTag() == 1) {
//						data.getRoomState().setHallName("午市大厅");
//					} else if (data.getRoomState().getHallTag() == 2) {
//						data.getRoomState().setHallName("午市大厅(紧张)");
//					} else if (data.getRoomState().getHallTag() == 3) {
//						data.getRoomState().setHallName("午市大厅(已满)");
//					} else if (data.getRoomState().getHallTag() == 4) {
//						data.getRoomState().setHallName("午市大厅");
//					}
//					
//					data.getRoomState().setRoomTag((4 - i % 4));
//					if (data.getRoomState().getRoomTag() == 1) {
//						data.getRoomState().setRoomName("午市包房");
//					} else if (data.getRoomState().getRoomTag() == 2) {
//						data.getRoomState().setRoomName("午市包房(紧张)");
//					} else if (data.getRoomState().getRoomTag() == 3) {
//						data.getRoomState().setRoomName("午市包房(已满)");
//					} else if (data.getRoomState().getRoomTag() == 4) {
//						data.getRoomState().setRoomName("午市包房");
//					}
//					
//					dto.getList().add(data);
//				}
//			}
//			dto.setPgInfo(dto1.getPgInfo());
//			dto.setAllRegionList(dto1.getAllRegionList());
//			dto.setAllRegionListNeedUpdateTag(dto1.isAllRegionListNeedUpdateTag());
//			dto.setAllRegionListTimestamp(dto1.getAllRegionListTimestamp());
//			dto.setAllMenuTypeList(dto1.getAllMenuTypeList());
//			dto.setAllMenuTypeListNeedUpdateTag(dto1.isAllMenuTypeListNeedUpdateTag());
//			dto.setAllMenuTypeListTimestamp(dto1.getAllMenuTypeListTimestamp());
//			dto.setRegionList(dto1.getRegionList());
//			dto.setMenuTypeList(dto1.getMenuTypeList());
//			dto.setMenuTypeList(dto1.getMenuTypeList());
//			dto.setSortList(dto1.getSortList());
//			dto.setAvgList(dto1.getAvgList());
//		}
		
		return jpSearchResults;
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
