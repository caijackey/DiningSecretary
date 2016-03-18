package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.*;
import android.os.*;
import android.util.Log;

import com.fg114.main.app.*;
import com.fg114.main.app.location.*;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.util.*;

/**
 * 发送语音订餐消息
 * @author wufucheng
 *
 */
public class CloseChatRoomTask extends BaseTask {
	
	private static final String TAG = CloseChatRoomTask.class.getSimpleName();
	public static final boolean DEBUG = Settings.DEBUG;
	
	public CloseChatRoomTask(
					String preDialogMessage, 
					Context context) {
		super(preDialogMessage, context);
	}

	@Override
	public JsonPack getData() throws Exception {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		} else {
			//继续执行
			if (DEBUG) Log.d(TAG, "looper已存在");
		}
		
		JsonPack jpResult = A57HttpApiV3.getInstance().closeChatRoom(
				SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken());
		
		return jpResult;
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
