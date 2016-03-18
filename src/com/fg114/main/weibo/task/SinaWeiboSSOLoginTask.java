package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.UserInfo;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.WeiboUtilFactory;


/**
 * 微博登录
 * 
 * @author xujianjun,2012-08-06
 * 
 */
public class SinaWeiboSSOLoginTask extends BaseTask {

	public UserInfo userInfo;
	private String uid;
	private String access_token;
	private String expires_in;
	private WeiboUtil currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
	
	public SinaWeiboSSOLoginTask(String preDialogMessage, Context context, String uid, String  access_token ,String expires_in) {
		super(preDialogMessage, context);
		this.uid=uid;
		this.access_token=access_token;
		this.expires_in=expires_in;
	}

	@Override
	public JsonPack getData() throws Exception {
		String cityId=SessionManager.getInstance().getCityInfo(context).getId();
		JsonPack jp=new JsonPack();
		try{
			userInfo=currentWeiboUtil.ssoWeiboLogin(uid, access_token, expires_in);
		}catch(Exception e){
			jp.setRe(400);
			jp.setMsg(e.getMessage());
		}
		return jp;
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

	@Override
	protected void onPostExecute(JsonPack result) {
		closeProgressDialog();
		super.onPostExecute(result);
	}
}