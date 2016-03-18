package com.fg114.main.service.task;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.service.dto.CityListDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 获得城市列表
 * @author zhangyifan
 *
 */
public class GetUserFriendListTask extends BaseTask {
	
	private static final String TAG = "GetUserFriendListTask";
	private static final boolean DEBUG = Settings.DEBUG;

	public CommonTypeListDTO dto;
	
	private String token = "";
	private long timestamp = 0;
	
	public GetUserFriendListTask(
					String preDialogMessage, 
					Context context,
					String token,
					long timestamp) {
		super(preDialogMessage, context);
		
		//获得gps
		this.token = token;
		this.timestamp = timestamp;
	}

	@Override
	public JsonPack getData() throws Exception {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		} else {
			//继续执行
			if (DEBUG) Log.d(TAG, "looper已存在");
		}
		
		return A57HttpApiV3.getInstance().getUserFriendList(
												ActivityUtil.getVersionName(context), 
												ActivityUtil.getDeviceId(context),
												"", 
												token, 
												timestamp);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = CommonTypeListDTO.toBean(result.getObj());
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
