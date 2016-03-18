package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 获得微博状态
 * @author zhangyifan
 *
 */
public class WebboTask extends BaseTask {

	public UserInfoDTO dto;
	
	private String token;
	
	public WebboTask(
					String preDialogMessage, 
					Context context,
					String token) {
		super(preDialogMessage, context);
		this.token = token;
	}

	@Override
	public JsonPack getData() throws Exception {
		return A57HttpApiV3.getInstance().webbo(
				ActivityUtil.getVersionName(context), 
				ActivityUtil.getDeviceId(context),
				token);
	}

	@Override
	public void onPreStart() {
		try {
			if (progressDialog != null) {
				progressDialog.setCancelable(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			//dto = UserInfoDTO.toBean(result.getObj());
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
