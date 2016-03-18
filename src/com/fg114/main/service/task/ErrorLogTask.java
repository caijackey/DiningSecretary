package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 错误提交
 * @author zhangyifan
 *
 */
public class ErrorLogTask extends BaseTask {

	public ChkDTO dto;
	
	private String errorMsg;
	private String errorDescription;
	
	public ErrorLogTask(
					String preDialogMessage, 
					Context context,
					String errorMsg,
					String errorDescription) {
		super(preDialogMessage, context);
		this.errorMsg = errorMsg;
		this.errorDescription = errorDescription;
	}

	@Override
	public JsonPack getData() throws Exception {
		return A57HttpApiV3.getInstance().errorLog(
				ActivityUtil.getVersionName(context), 
				ActivityUtil.getDeviceId(context),
				ActivityUtil.getDeviceId(context),
				errorMsg,
				errorDescription);
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
