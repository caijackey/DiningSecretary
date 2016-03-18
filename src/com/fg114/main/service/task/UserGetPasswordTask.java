package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 找回密码
 * @author chenguojin
 *
 */
public class UserGetPasswordTask extends BaseTask {

	public UserInfoDTO dto;
	
	private String mPhoneNum;
	
	public UserGetPasswordTask(
					String preDialogMessage, 
					Context context,
					String phoneNum) {
		super(preDialogMessage, context);
		this.mPhoneNum = phoneNum;
	}

	@Override
	public JsonPack getData() throws Exception {
		return A57HttpApiV3.getInstance().sendPwdSmsByPhoneNum(
												ActivityUtil.getVersionName(context), 
												ActivityUtil.getDeviceId(context),
												mPhoneNum);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
