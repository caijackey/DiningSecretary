package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.weibo.AuthUrls;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 获取微博授权的url
 * @deprecated
 * @author xujianjun,2012-08-06
 * 
 */
public class GetAuthURLTask extends BaseTask {

	WeiboUtil weibo;
	public AuthUrls urls;
	public GetAuthURLTask(String preDialogMessage, Context context, WeiboUtil weibo) {
		super(preDialogMessage, context);
		this.weibo=weibo;
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			urls=weibo.getAuthUrls();
		}catch(Exception e){
			jp.setRe(505);
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