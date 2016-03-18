package com.fg114.main.app.activity;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.service.dto.PushMsgDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.task.UserRegistTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;

/**
 * 消息推送
 * 
 * @author chenguojin
 * 
 */
public class PushCommonActivity extends Activity {

	// 传入参数获得
	// private int fromPage; // 显示内容
	Bundle bundle;
	PushMsgDTO msgData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setActivityId(Settings.ABOUT_US_ACTIVITY);
		setContentView(R.layout.pushcommonlayout);
		// // 获得传入参数
        Settings.Is_Push_Notification_to_activity=true;
        
		bundle = getIntent().getExtras();
		msgData = (PushMsgDTO)bundle.getSerializable(Settings.BUNDLE_KEY_ID);
		if(msgData==null){
			finish();
			return;			
		}

		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningTaskInfo> tasksInfo = activityManager
				.getRunningTasks(1);
		
		if (tasksInfo.get(0).numActivities > 1) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {							
					handlePushMessage(PushCommonActivity.this, msgData);
					PushCommonActivity.this.finish();
				}
			});
		} else {
			PushCommonActivity.this.finish();
			ActivityUtil.jumpNotForResult(PushCommonActivity.this,
					IndexActivity.class, bundle, false);
		}

	}
	
	//处理推送消息
	public static void handlePushMessage(Activity context, PushMsgDTO msgData){
		// -----
		OpenPageDataTracer.getInstance().addEvent("推送消息",msgData.getAdvUrl());
		// -----
		//类别  1:广告链接  2：本地连接  3:普通链接（需要跳系统浏览器）
		//普通链接跳转到webview页面, 本地链接使用url处理器
		if (msgData.getTypeTag() == 1) {
			// 广告链接，使用内嵌的WebView打开
			Bundle bd = new Bundle();
			bd.putString(Settings.BUNDLE_KEY_WEB_URL, msgData.getAdvUrl());
			bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, msgData.getTitle());
			ActivityUtil.jump(context, SimpleWebViewActivity.class, 0, bd, false);

		} else if (msgData.getTypeTag() == 2) {
			// 本地链接，跳转本地界面
			URLExecutor.execute(msgData.getAdvUrl(), context, 0);
		} else if (msgData.getTypeTag() == 3) {
			// 普通链接，使用系统浏览器打开
			ActivityUtil.jumbToWeb(context, msgData.getAdvUrl());
		}
	}

}
