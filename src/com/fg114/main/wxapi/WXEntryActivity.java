package com.fg114.main.wxapi;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.*;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.*;
import com.fg114.main.service.dto.MdbRestListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.*;

/**
 * 接收微信的请求及返回值的Activity
 * 
 * @author wufucheng
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WeixinUtils.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		WeixinUtils.handleIntent(intent, this);
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {

		// Toast.makeText(this, "openid = " + req.openId,
		// Toast.LENGTH_SHORT).show();
		//
		// switch (req.getType()) {
		// case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
		// goToGetMsg();
		// break;
		// case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
		// goToShowMsg((ShowMessageFromWX.Req) req);
		// break;
		// case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
		// Toast.makeText(this, R.string.launch_from_wx,
		// Toast.LENGTH_SHORT).show();
		// break;
		// default:
		// break;
		// }

		ActivityUtil.jumpNotForResult(this, IndexActivity.class, new Bundle(), false);
		finish();
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		// Toast.makeText(this, "openid = " + resp.openId,
		// Toast.LENGTH_SHORT).show();
		//
		// if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
		// Toast.makeText(this, "code = " + ((SendAuth.Resp) resp).code,
		// Toast.LENGTH_SHORT).show();
		// }
		//
		// String result = "";
		//
		switch (resp.errCode) {
		// 发送成功
		case BaseResp.ErrCode.ERR_OK:
			successShareToWinxin();
			break;
		// 发送取消
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			break;
		// 发送被拒绝
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			break;
		// 发送返回
		default:
			break;
		}
		//
		// Toast.makeText(this, result, Toast.LENGTH_LONG).show();

		finish();
	}

	private void successShareToWinxin() {
		ServiceRequest request = new ServiceRequest(API.successShareToWinxin);
		request.addData("wxTypeTag", Settings.wxTypeTag);
		request.addData("shareTypeTag", Settings.shareTypeTag);
		request.addData("shareUuid", Settings.shareUuid);
		CommonTask.requestMutely(request, new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});

	}
}