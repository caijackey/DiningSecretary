package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 提交评论
 * @author zhangyifan
 *
 */
public class AddFriendTask extends BaseTask {

	public ChkDTO dto;
	
	private String token;
	private String friendName;
	private String friendTel;
	
	public AddFriendTask(
					String preDialogMessage, 
					Context context,
					String token,
					String friendName,
					String friendTel) {
		super(preDialogMessage, context);
		this.token = token;
		this.friendName = friendName;
		this.friendTel = friendTel;
	}

	@Override
	public JsonPack getData() throws Exception {


		return A57HttpApiV3.getInstance().addFriend(
												ActivityUtil.getVersionName(context), 
												ActivityUtil.getDeviceId(context),
												"",
												token,
												friendName,
												friendTel);
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
