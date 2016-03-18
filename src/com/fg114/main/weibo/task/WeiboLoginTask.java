package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.UserInfo;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 微博登录
 * 
 * @author xujianjun,2012-08-06
 * 
 */
public class WeiboLoginTask extends BaseTask {

	WeiboUtil weibo;
	public UserInfo userInfo;
	public String code;
	public WeiboLoginTask(String preDialogMessage, Context context, String code,WeiboUtil weibo) {
		super(preDialogMessage, context);
		this.weibo=weibo;
		this.code=code;
	}

	@Override
	public JsonPack getData() throws Exception {
		String cityId=SessionManager.getInstance().getCityInfo(context).getId();
		JsonPack jp=new JsonPack();
		try{
			userInfo=weibo.weiboLogin(cityId, code);	
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