package com.fg114.main.service.task;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;

/**
 * 注册
 * 
 * @author chenguojin
 * 
 */
public class UserRegistTask extends BaseTask {

	public UserInfoDTO dto;

	private String cityId;
	private String mUserName;
	private String userPwd;
	private boolean mIsPhone;

	public UserRegistTask(String preDialogMessage, Context context,
			String cityId, String userName, String userPwd, boolean isPhone) {
		super(preDialogMessage, context);
		this.cityId = cityId;
		this.mUserName = userName;
		this.userPwd = userPwd;
		this.mIsPhone = isPhone;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		return A57HttpApiV3.getInstance().userReg3(	
				Settings.SELL_CHANNEL_NUM,//渠道号
				cityId,//所属城市ID
				mUserName,//手机号或email
				userPwd,//密码
				mIsPhone);//是否是通过手机号注册
				
//		return A57HttpApiV3.getInstance().userReg2(
//				ActivityUtil.getVersionName(context),
//				ActivityUtil.getDeviceId(context), Settings.SELL_CHANNEL_NUM,
//				cityId, mUserName, userPwd, mIsPhone);
	}

	@Override
	public void onPreStart() {

	}

	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			//dto = UserInfoDTO.toBean(result.getObj());
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		if (result.getRe() == 401 && mIsPhone) {
			DialogUtil.showAlert(context, true,
					getString(R.string.text_info_regist_title),
					getString(R.string.text_info_regist_mess),
					getString(R.string.text_button_cancel),
					getString(R.string.text_layout_find_pwd),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final UserGetPasswordTask task = new UserGetPasswordTask(
									context.getResources().getString(
											R.string.text_info_uploading),
									context, mUserName);
							task.execute(new Runnable() {
								@Override
								public void run() {
									task.closeProgressDialog();
								}
							});
						}
					});
		} else {
			DialogUtil.showToast(context, result.getMsg());
		}
	}
}
