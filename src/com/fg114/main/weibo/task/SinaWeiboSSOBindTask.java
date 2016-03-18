package com.fg114.main.weibo.task;

import android.content.Context;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.BindToReturnData;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.WeiboUtilFactory;


/**
 * 微博绑定
 * 
 * @author xujianjun,2012-08-06
 * 
 */
public class SinaWeiboSSOBindTask extends BaseTask {

	public BindToReturnData returnData;
	private String uid;
	private String access_token;
	private String expires_in;
	private WeiboUtil currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
	
	public SinaWeiboSSOBindTask(String preDialogMessage, Context context,String uid, String  access_token ,String expires_in) {
		super(preDialogMessage, context);
		this.uid=uid;
		this.access_token=access_token;
		this.expires_in=expires_in;
	}

	@Override
	public JsonPack getData() throws Exception {
		JsonPack jp=new JsonPack();
		try{
			returnData=currentWeiboUtil.ssoBindTo(uid, access_token, expires_in);	
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