package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.BindToReturnData;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 微博发表
 * 
 * @author xujianjun,2012-05-11
 * 
 */
public class PostWeiboShareTask extends BaseTask {

	WeiboUtil currentWeiboUtil;
	String detail;
	
	public PostWeiboShareTask(String preDialogMessage, Context context,String detail, WeiboUtil currentWeiboUtil) {
		super(preDialogMessage, context);
		this.currentWeiboUtil=currentWeiboUtil;
		this.detail=detail;
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			currentWeiboUtil.postWeiboShare(detail);	
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