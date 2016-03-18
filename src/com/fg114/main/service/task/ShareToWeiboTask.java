package com.fg114.main.service.task;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

import android.content.Context;
import android.util.Log;

public class ShareToWeiboTask extends BaseTask {

	private String mToken;
	private String mUuid;
	private String mDetail;
	private String mShareInfo;
	private int mTypeTag;
	public String msg = "分享成功";
	public int code = 0;

	public ShareToWeiboTask(String preDialogMessage, Context context, int typetag, String token, String uuId,
			String detail, String shareinfo) {
		super(preDialogMessage, context);
		this.mToken = token;
		this.mUuid = uuId;
		this.mDetail = detail;
		this.mShareInfo = shareinfo;
		this.mTypeTag = typetag;
	}

	@Override
	public JsonPack getData() throws Exception {
		return ServiceRequest.shareTo(mTypeTag, mToken, mUuid, mDetail, mShareInfo);
	}

	@Override
	public void onStateFinish(JsonPack result) {
		if (result == null || result.getObj() == null) {
			return;
		}
		SimpleData data = JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
		msg = data.getMsg();
		code = data.getErrorCode();
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());

	}

	@Override
	public void onPreStart() {
		// TODO Auto-generated method stub

	}

}
