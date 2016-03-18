package com.fg114.main.service.task;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResPromoData;
import com.fg114.main.service.dto.RoomState;
import com.fg114.main.service.dto.RoomStateAndOrderInfo;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 获得房间状态和订单信息
 * @author xujianjun,2013-03-02
 *
 */
public class GetRestRoomStateAndOrderInfoTask extends BaseTask {

	public RoomStateAndOrderInfo dto;
	
	private String resId;
	private long selectTime = 0;
	
	public GetRestRoomStateAndOrderInfoTask(
					String preDialogMessage, 
					Context context,
					String resId,
					long selectTime) {
		super(preDialogMessage, context);
		this.resId = resId;
		this.selectTime = selectTime;
	}

	@Override
	public JsonPack getData() throws Exception {


		//获得餐馆页面数据  返回ResInfo3Data
		return  A57HttpApiV3.getInstance().getRestRoomStateAndOrderInfo(
				resId, //餐馆id
				selectTime //当前选择的预订时间
				);
//		return getTestData();

	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = RoomStateAndOrderInfo.toBean(result.getObj());
		}
		closeProgressDialog();
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
	private JsonPack getTestData() throws JSONException{
		//--测试数据
		SystemClock.sleep(1000);
		String roomName;
		JsonPack jp=new JsonPack();
		jp.setRe(200);
		JSONObject obj=new JSONObject("{\"canJoinAirPlanePromotionTag\":\"true\",\"apId\":\"110\",\"apTitle\":\"我要参加吉祥三宝\",\"apPlaceHolder\":\"输入你的宝号\",\"apDetail\":\"参加活动就有大奖，大弟弟你呀快快来，小弟弟你呀莫躲开！\",\"roomState\":{hallTag:0,hallName:\"没有大厅了\",roomTag:2,roomName:\"有包房（紧张）\"}}");
		jp.setObj(obj);
		//---
		return jp;
		
	}
}
