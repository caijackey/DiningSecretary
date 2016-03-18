package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.SendSMSActivity;
import com.fg114.main.service.dto.CommentListDTO;
import com.fg114.main.service.dto.ErrorReportTypeListPackDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 发送短信邀请的task
 * @author xu jian jun ,2011-12-23
 *
 */
public class SendSMSInvitationTask extends BaseTask {

	ErrorReportTypeListPackDTO dto;	
	String friendList;
	String content;
	String orderId;
	String restId = "";
	boolean havePlaceGpsTag; //是否有参照地标gps
	String placeLon; //地标经度  可以为空
	String placeLat; //地标纬度 可以为空
	String cityId;
	
	String placeName;
	String templetId;
	
	public SendSMSInvitationTask(
					String preDialogMessage,
					String friendList,
					String content,
					String orderId,
					String restId,
					boolean havePlaceGpsTag,
					String placeLon,
					String placeLat,
					String placeName,
					String templetId,
					String cityId,
					Context context) {
		super(preDialogMessage, context);
		this.friendList=friendList;
		this.content=content;
		this.orderId=orderId;
		this.restId = restId;
		this.havePlaceGpsTag=havePlaceGpsTag;
		this.placeLon=placeLon;
		this.placeLat=placeLat;
		this.placeName=placeName;
		this.templetId=templetId;
		this.cityId = cityId;
	}

	@Override
	public JsonPack getData() throws Exception {
		
//		return A57HttpApiV3.getInstance().sendSms(
//				ActivityUtil.getVersionName(context), 
//				ActivityUtil.getDeviceId(context),
//				"restEcName",friendList, content, orderId);

		//升级为第2版接口
		return ServiceRequest.sendSms(havePlaceGpsTag, cityId, placeLon,placeLat,placeName,restId,orderId,templetId,friendList,content);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
	}

	@Override
	public void onStateError(JsonPack result) {
		if(result!=null&&!CheckUtil.isEmpty(result.getMsg())){
			DialogUtil.showToast(context, result.getMsg());
		}else{
			DialogUtil.showToast(context, "发送时没有成功，请稍后重试！");
		}
		
	}


}
