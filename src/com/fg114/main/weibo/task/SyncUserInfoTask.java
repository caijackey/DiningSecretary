package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.UserInfo;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 同步用户信息
 * 
 * @author xujianjun,2012-08-07
 * 
 */
public class SyncUserInfoTask extends BaseTask {

	public UserInfo userInfo;
	private String userToken;
	private boolean isDumb; //如果是true,表示不弹出任何提示
	
	public SyncUserInfoTask(String preDialogMessage, Context context, boolean isDumb) {
		super(preDialogMessage, context);
		this.userToken=SessionManager.getInstance().getUserInfo(context).getToken();
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			userInfo=WeiboUtil.syncUserInfo(userToken);
			
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
		if(!isDumb){	
			DialogUtil.showToast(context, result.getMsg());
		}
	}
	@Override
	protected void onPostExecute(JsonPack result) {
		closeProgressDialog();
		super.onPostExecute(result);
	}
}