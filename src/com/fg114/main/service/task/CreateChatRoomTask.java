package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.*;
import android.os.*;
import android.util.Log;

import com.fg114.main.app.*;
import com.fg114.main.app.location.*;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.util.*;

/**
 * 获得语音订餐消息列表
 * @author wufucheng
 *
 */
public class CreateChatRoomTask extends BaseTask {
	
	private static final String TAG = CreateChatRoomTask.class.getSimpleName();
	public static final boolean DEBUG = Settings.DEBUG;

	public ChatRoomCreateData dto;
	
	public CreateChatRoomTask(
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
		
		JsonPack jpResult = A57HttpApiV3.getInstance().createChatRoom( 
				SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken(),
				SessionManager.getInstance().getLastChatMsgId(),
				SessionManager.getInstance().getCreateChatRoomTimestamp());
		
		// 测试数据
//		JsonPack jpResult = new JsonPack();
//		jpResult.setRe(200);
//		ChatRoomCreateData data = new ChatRoomCreateData();
//		data.setClientRefreshInterval(30 * 1000);
//		jpResult.setObj(new JSONObject(JsonUtils.toJson(data)));
		
		if (jpResult.getRe() == 200) {
			dto = JsonUtils.fromJson(jpResult.getObj().toString(), ChatRoomCreateData.class);
			SessionManager.getInstance().setCreateChatRoomTimestamp(dto.getTimestamp());
			
			if (dto.getMsgListDto() != null && dto.getMsgListDto().getList() != null && dto.getMsgListDto().getList().size() > 0) {
				
				//存储最后一条成功接收的"系统"消息Id，用于下一次向服务器确认号码
				if(dto.getMsgListDto().getList().get(dto.getMsgListDto().getList().size()-1)!=null){
					String uuid=dto.getMsgListDto().getList().get(dto.getMsgListDto().getList().size()-1).getUuid();
					if(uuid!=null){
						ValueCacheUtil.getInstance(context).remove(Settings.KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID, Settings.KEY_CHAT_MSG_LIST);
						ValueCacheUtil.getInstance(context).add(Settings.KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID, Settings.KEY_CHAT_MSG_LIST, uuid);
					}
				}
			}
		}
		
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
