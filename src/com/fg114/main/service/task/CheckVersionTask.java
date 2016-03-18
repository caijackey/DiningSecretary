package com.fg114.main.service.task;

import android.content.Context;
import android.os.SystemClock;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 版本检查
 * @author zhangyifan
 *
 */
public class CheckVersionTask extends BaseTask {

	public VersionChkDTO dto;
	
	private boolean isFirstOpen;
	
	public CheckVersionTask(Context context, boolean isFirstOpen) {
		super(context);
		this.isFirstOpen = isFirstOpen;
	}

	@Override
	public JsonPack getData() throws Exception {
		return A57HttpApiV3.getInstance().chkVersion(
									ActivityUtil.getVersionName(context), 
									isFirstOpen,
									Settings.CLIENT_TYPE, 
									Settings.SELL_CHANNEL_NUM, 
									ActivityUtil.getDeviceId(context));
	}
	
	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = JsonUtils.fromJson(result.getObj().toString(),VersionChkDTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
