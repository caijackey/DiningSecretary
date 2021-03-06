package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.service.dto.SortedFoodListDTO;
import com.fg114.main.service.dto.SortedFoodListData;
import com.fg114.main.service.dto.SortedFoodSubListData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.dto.User;


/**
 * 添加测试帐户
 * @author xujianjun,2013-05-16
 *
 */
public class AddDebugAccountTask extends BaseTask {

	//public SimpleData dto;
	private String debugName="";
	private String debugPwd="";
	public AddDebugAccountTask(
					String preDialogMessage, 
					Context context,
					String debugName,
					String debugPwd
					){
		super(preDialogMessage, context);
		this.debugName=debugName;
		this.debugPwd=debugPwd;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		JsonPack jp= new JsonPack();
		jp= ServiceRequest.addDebugAccount(debugName,debugPwd);
		//jp.setMsg(message);
		
		//----测试数据
		//JsonPack jp=getTestData();
		//-----------

		return jp;
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if(result!=null){
			DialogUtil.showToast(context, result.getMsg());
		}
		//dto=JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
	}
	
	@Override
	protected void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}
	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
	
	private JsonPack getTestData() throws JSONException{
		//---------------------------测试数据
		SystemClock.sleep(500);
		String data="{\"msg\":\"邀请码正确！\"}";
		JsonPack result=new JsonPack();
		result.setObj(new JSONObject(data));
		//-----------------------------------
		return result;
	}

	 
}
