package com.fg114.main.service.task;

import java.io.InputStream;
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
public class SendChatMsgVoiceTask extends BaseTask {
	
	private static final String TAG = SendChatMsgVoiceTask.class.getSimpleName();
	public static final boolean DEBUG = Settings.DEBUG;
	
	private InputStream isData;
	
	public SendChatMsgVoiceTask(
					String preDialogMessage, 
					Context context,
					InputStream isData) {
		super(preDialogMessage, context);
		
		this.isData = isData;
	}

	@Override
	public JsonPack getData() throws Exception {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		} else {
			//继续执行
			if (DEBUG) Log.d(TAG, "looper已存在");
		}
		
		JsonPack jpResult = A57HttpApiV3.getInstance().sendChatMsgStream(
				1,
				SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken(),
				isData);
		
		// 测试数据
//		JsonPack jpResult = new JsonPack();
//		jpResult.setRe(200);
		
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
