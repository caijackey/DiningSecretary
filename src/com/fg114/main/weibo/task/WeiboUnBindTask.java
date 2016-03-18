package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.BindToReturnData;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 微博解除绑定
 * 
 * @author xujianjun,2012-08-06
 * 
 */
public class WeiboUnBindTask extends BaseTask {

	WeiboUtil currentWeiboUtil;
	String token;
	
	public WeiboUnBindTask(String preDialogMessage, Context context,WeiboUtil currentWeiboUtil) {
		super(preDialogMessage, context);
		this.currentWeiboUtil=currentWeiboUtil;
		this.token=SessionManager.getInstance().getUserInfo(context).getToken();
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			currentWeiboUtil.unbind(token);	
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