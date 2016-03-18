package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.R;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.*;

/**
 * 提交错误信息
 * 
 * @author wufucheng
 * 
 */
public class PostErrorReportTask extends BaseTask {

	public ChkDTO dto;

	private String name;
	private String email;
	private String feedback;
	private int typeTag;
	private int funcTag;
	private String typeId;
	private String typeName;
	private String uuid;
	private Runnable runAfterAlert;

	public PostErrorReportTask(int typeTag, int funcTag, String typeId, String typeName, String preDialogMessage, Context context, String name, String email,
			String uuid, String feedback) {
		super(preDialogMessage, context);
		this.name = name;
		this.email = email;
		this.feedback = feedback;
		this.typeTag = typeTag;
		this.funcTag = funcTag;
		this.typeId = typeId;
		this.typeName = typeName;
		this.uuid = uuid;
	}

	public PostErrorReportTask(int typeTag, int funcTag, String typeId, String typeName, String preDialogMessage, Context context, String name, String email,
			String uuid, String feedback, Runnable runAfterAlert) {

		this(typeTag, funcTag, typeId, typeName, preDialogMessage, context, name, email, uuid, feedback);
		this.runAfterAlert = runAfterAlert;

	}

	@Override
	public JsonPack getData() throws Exception {
		/*
		 * return A57HttpApiV3.getInstance().postFeedBack( name, email,
		 * feedback, ActivityUtil.getVersionName(context),
		 * ActivityUtil.getDeviceId(context));
		 */
		if (!"-999".equals(typeId)) {// -999表示就餐金额报错

			return A57HttpApiV3.getInstance().addErrorReport(ActivityUtil.getVersionName(context), ActivityUtil.getDeviceId(context), "restEcName", typeTag,
					typeId, feedback, email, uuid == null ? "" : uuid,
					SessionManager.getInstance().isUserLogin(context) ? SessionManager.getInstance().getUserInfo(context).getToken() : null);
		} else { // 定单价格报错
			return A57HttpApiV3.getInstance().reportOrderWrongPrice(ActivityUtil.getVersionName(context),
					ActivityUtil.getDeviceId(context),
					uuid == null ? "" : uuid, // orderId
					SessionManager.getInstance().isUserLogin(context) ? SessionManager.getInstance().getUserInfo(context).getToken() : null,
					Double.parseDouble(feedback));

		}
	}

	@Override
	public void onPreStart() {
	}

	@Override
	public void onStateFinish(JsonPack result) {
		closeProgressDialog();
		String msg=context.getString(R.string.text_dialog_error_submit_success);
		if("-999".equals(typeId)){
			// -999表示就餐金额报错
			msg="提交成功";
			result.setMsg("就餐金额提交成功，我们会尽快核实。");
		}
		DialogUtil.showAlert(context,msg , result.getMsg(), runAfterAlert);
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
