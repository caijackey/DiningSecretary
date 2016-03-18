package com.fg114.main.service.task;

import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.RestSearchSuggestListDTO;
import com.fg114.main.service.dto.UsedHistorySuggestListDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

/**
 * 获得餐厅历史建议列表
 * @author zhangyifan
 *
 */
public class GetUsedHistorySuggestListTask extends BaseTask {

	public UsedHistorySuggestListDTO dto;
	String key;
	String dir;
	
	public GetUsedHistorySuggestListTask(Context context) {
		super(context);
	}

	@Override
	public JsonPack getData() throws Exception {
		// 以下是缓存逻辑，缓存时间3小时
		// 先构造缓存关键字
		JsonPack jp = new JsonPack();
//		String cityId=SessionManager.getInstance().getCityInfo(context).getId();
//		StringBuilder sbKey = new StringBuilder();
//		sbKey.append(cityId);
//		key = sbKey.toString();
//		dir=getClass().getName();
//		ValueObject vo = ValueCacheUtil.getInstance(context).get(dir, key);
//		if (vo != null && !vo.isExpired()) {
//			//命中
//			jp.setObj(new JSONObject(vo.getValue()));
//		} else {
//			jp = ServiceRequest.getUsedHistorySuggestList();
//			
//			// 如果结果正确，存入缓存
//			if (jp.getRe() == 200) {
//				ValueCacheUtil.getInstance(context).remove(dir, key);
//				ValueCacheUtil.getInstance(context).add(dir, key, jp.getObj().toString(), "0", "-", 10); // 10分钟
//			}
//		}
		jp = ServiceRequest.getUsedHistorySuggestList();
		return jp;
	}
	
	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		
		if (result.getObj() != null) {
			dto =JsonUtils.fromJson(result.getObj().toString(), UsedHistorySuggestListDTO.class);
			if(dto.list==null||dto.list.size()==0){
				//如果没有数据，删除缓存
				ValueCacheUtil.getInstance(context).remove(dir, key);
			}
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
