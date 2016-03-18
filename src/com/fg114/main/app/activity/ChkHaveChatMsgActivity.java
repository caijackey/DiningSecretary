//package com.fg114.main.app.activity;
//
//import java.util.List;
//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.ActivityManager.RunningTaskInfo;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.text.InputType;
//import android.text.style.ClickableSpan;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.TextView;
//
//import com.fg114.main.R;
//import com.fg114.main.app.Fg114Application;
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.MainFrameActivity;
//import com.fg114.main.app.activity.chat.ChatMsgListActivity;
//import com.fg114.main.app.activity.order.MyOrderDetailActivity;
//import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
//import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
//import com.fg114.main.service.dto.ChatMsgChkData;
//import com.fg114.main.service.dto.PushMsgDTO;
//import com.fg114.main.service.http.A57HttpApiV3;
//import com.fg114.main.service.task.UserRegistTask;
//import com.fg114.main.util.ActivityUtil;
//import com.fg114.main.util.CheckUtil;
//import com.fg114.main.util.DialogUtil;
//import com.fg114.main.util.SessionManager;
//import com.fg114.main.util.SharedprefUtil;
//import com.fg114.main.util.URLExecutor;
//import com.fg114.main.util.ViewUtils;
//
///**
// * 语音订餐聊天推送消息中转页面 
// * 当程序不在前台
// * @author xujianjun, 2013-01-05
// * 
// */
//public class ChkHaveChatMsgActivity extends Activity {
//
//	// 传入参数获得
//	private Bundle bundle;
//	private ChatMsgChkData mDto;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		// setActivityId(Settings.ABOUT_US_ACTIVITY);
//		setContentView(R.layout.pushcommonlayout);
//		// // 获得传入参数
//
//		bundle = getIntent().getExtras();
//		mDto = (ChatMsgChkData)bundle.getSerializable(Settings.BUNDLE_KEY_ID);
//		if(mDto==null){
//			finish();
//			return;			
//		}
//
//		ActivityManager activityManager = (ActivityManager) this
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		final List<RunningTaskInfo> tasksInfo = activityManager
//				.getRunningTasks(1);
//
//		//如果在前台，询问是否跳转，否则跳首页后跳转
//		if (tasksInfo.get(0).numActivities > 1) {
//			DialogUtil.showComfire(this, "有新的订餐消息", mDto.getTitle(),"去看看",
//					new Runnable() {
//
//						@Override
//						public void run() {							
//							handleMessage(ChkHaveChatMsgActivity.this, mDto);
//							ChkHaveChatMsgActivity.this.finish();
//						}
//					}, 
//					"取消", 
//					new Runnable() {
//
//						@Override
//						public void run() {
//							ChkHaveChatMsgActivity.this.finish();
//						}
//					});
//		} else {
//			ChkHaveChatMsgActivity.this.finish();
//			ActivityUtil.jumpNotForResult(ChkHaveChatMsgActivity.this,
//					IndexActivity.class, bundle, false);
//		}
//
//	}
//	
//	// 处理推送消息
//	public static void handleMessage(Activity context, ChatMsgChkData msgData) {
//		//跳语音订餐页
//		Bundle bd = new Bundle();
//		bd.putSerializable(Settings.BUNDLE_KEY_ID, msgData);
//		ActivityUtil.jump(context, ChatMsgListActivity.class, 0, bd, false);
//
//	}
//
//}
