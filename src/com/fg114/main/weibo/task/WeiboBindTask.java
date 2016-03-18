package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.BindToReturnData;
import com.fg114.main.weibo.WeiboUtil;


/**
 * 微博绑定
 * 
 * @author xujianjun,2012-08-06
 * 
 */
public class WeiboBindTask extends BaseTask {

	WeiboUtil weibo;
	public BindToReturnData returnData;
	public String token;
	public String code;
	public boolean forceBind;
	
	public WeiboBindTask(String preDialogMessage, Context context, String code, boolean forceBind, WeiboUtil weibo) {
		super(preDialogMessage, context);
		this.weibo=weibo;
		this.token=SessionManager.getInstance().getUserInfo(context).getToken();
		this.code=code;
		this.forceBind=forceBind;
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			returnData=weibo.bindTo(token, code, forceBind);	
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