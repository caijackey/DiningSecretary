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
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

/**
 * 获得餐厅搜所关键字建议列表
 * @author zhangyifan
 *
 */
public class GetRestSearchSuggestListTask extends BaseTask {

	public RestSearchSuggestListDTO dto;
	
	private String keywords = "";
	private int startIndex = 1;
	
	public GetRestSearchSuggestListTask(Context context, int startIndex) {
		super(context);
		this.startIndex = startIndex;
	}

	@Override
	public JsonPack getData() throws Exception {
		// 以下是缓存逻辑，缓存时间3小时
		// 先构造缓存关键字
		String cityId=SessionManager.getInstance().getCityInfo(context).getId();
		StringBuilder sbKey = new StringBuilder();
		sbKey.append(cityId).append('|');
		sbKey.append(startIndex).append('|');
		sbKey.append(keywords).append('|');
		String key = sbKey.toString();
		String dir=getClass().getName();
		ValueObject vo = ValueCacheUtil.getInstance(context).get(dir, key);
		JsonPack jp = new JsonPack();
		if (vo != null && !vo.isExpired()) {
			//命中
			jp.setObj(new JSONObject(vo.getValue()));
			dto = JsonUtils.fromJson(jp.getObj().toString(), RestSearchSuggestListDTO.class);
		} else {
			jp = ServiceRequest.getRestSearchSuggestList(
					keywords,
					Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE,
					startIndex);
			
			// 如果结果正确，存入缓存
			if (jp.getRe() == 200 && jp.getObj() != null) {
				dto = JsonUtils.fromJson(jp.getObj().toString(), RestSearchSuggestListDTO.class);
				if (dto != null && dto.list!=null && dto.list.size() > 0) { // 有结果才缓存
					ValueCacheUtil.getInstance(context).remove(dir, key);
					ValueCacheUtil.getInstance(context).add(dir, key, jp.getObj().toString(), "0", "-", 5); // 5分钟
				}
			}
		}
		return jp;
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
	

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	
}
