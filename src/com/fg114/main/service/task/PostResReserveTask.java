package com.fg114.main.service.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 登录
 * 
 * @author zhangyifan
 * 
 */
public class PostResReserveTask extends BaseTask {

	public ChkDTO dto;

	private String token;
	private String resId;
	private String tel;
	private long reserveTime;
	private int peopleNum;
	private String bookerName;
	private int sexTag;
	private int roomTypeTag;
	private String remark;
	private boolean selectApTag;//是否选择了航空活动
	private String apId;//参加的航空活动id 
	private String apNumber;//输入的航空会员号
	// --
	int postTag;// 提交类型 1:添加 2:更新
	public String orderId="";// 订单id 如果postTag=1 为空 postTag=2为修改的订单的id
	public String smsDetail="";// 短信邀请的模板
	

	public PostResReserveTask(String preDialogMessage, Context context, String token, String resId, String tel, long reserveTime, int peopleNum,
			String bookerName, String remark, int sexTag, int roomTypeTag, int postTag, String orderId,boolean selectApTag,String apId,String apNumber) {
		super(preDialogMessage, context);
		this.token = token;
		this.resId = resId;
		this.tel = tel;
		this.reserveTime = reserveTime;
		this.peopleNum = peopleNum;
		this.bookerName = bookerName;
		this.sexTag = sexTag;
		this.roomTypeTag = roomTypeTag;
		this.remark = remark;
		this.postTag = postTag;
		this.orderId = orderId;
		this.selectApTag = selectApTag;
		this.apId = apId;
		this.apNumber = apNumber;
	}

	@Override
	public JsonPack getData() throws Exception {
		/*
		 * return A57HttpApiV3.getInstance().postResReserve(
		 * ActivityUtil.getVersionName(context), token, resId, tel, reserveTime,
		 * peopleNum, bookerName, sexTag, roomTypeTag, remark,
		 * Settings.CLIENT_TYPE, ActivityUtil.getLocalIpAddress(),
		 * Settings.SELL_CHANNEL_NUM, ActivityUtil.getDeviceId(context));
		 */
		return A57HttpApiV3.getInstance().postOrder2(ActivityUtil.getVersionName(context), ActivityUtil.getDeviceId(context), Settings.SELL_CHANNEL_NUM,
				postTag, orderId, token, resId, tel, reserveTime, peopleNum, bookerName, sexTag, roomTypeTag, remark,selectApTag,apId,apNumber);
	}

	@Override
	public void onPreStart() {

	}

	@Override
	public void onStateFinish(JsonPack result) {
		try {

			if (result.getObj() != null) {
				JSONObject obj = result.getObj();
				if (obj != null && obj.has("uuid")) {
					orderId = result.getObj().getString("uuid");
				} else {
					orderId = "";
				}
				if (obj != null && obj.has("smsDetail")) {
					smsDetail = result.getObj().getString("smsDetail");
				} else {
					smsDetail = "";
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
