package com.fg114.main.app.activity.usercenter;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;

/**
 * 用户登录
 * 
 * @author zhangyifan
 * 
 */
public class UserLoginActivity extends MainFrameActivity {

	private boolean debug = false;

	// 界面初始化
	private LayoutInflater mInflater;
	private View contextView;
	private LinearLayout loginByPhonenumber;// 手机号码登录
	private LinearLayout loginBySinablog;// 新浪微博账号登录
	private LinearLayout loginByTencent;// 腾讯微博账号登录
	private LinearLayout login_by_invite;//邀请码登录
	private boolean hasLogined;// 判断是否登录

	// 任务
	private UserLoginTask userLoginTask;
	private UserInfoDTO infoDTO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("登录", "");
		// ----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		hasLogined = SessionManager.getInstance().isUserLogin(this);
		// 初始化界面
		initComponent();
	}

	@Override
	public void onRestart() {
		super.onRestart();

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("登录", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		infoDTO = SessionManager.getInstance().getUserInfo(UserLoginActivity.this);
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(R.string.text_title_login);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分

		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.user_login, null);
		loginByPhonenumber = (LinearLayout) contextView.findViewById(R.id.login_by_phonenumber);
		loginBySinablog = (LinearLayout) contextView.findViewById(R.id.login_by_sinablog);
		loginByTencent = (LinearLayout) contextView.findViewById(R.id.login_by_tencent);
		login_by_invite=(LinearLayout) contextView.findViewById(R.id.login_by_invite);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// 手机号码登录
		loginByPhonenumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("手机按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putLong(Settings.FROM_TAG, 1);
				ActivityUtil.jump(UserLoginActivity.this, ValidatePhoneNOActivity.class, 0, bundle);
				finish();
			}
		});
		// 新浪微博账号登录
		loginBySinablog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("新浪按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, true);
				CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {
					@Override
					public void onComplete(boolean isSuccessful) {
						if (isSuccessful) {
							SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
						}
						finish();
						return;
					}
				}));
				SinaSSOAuthActivity.currentWeiboUtil = WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
				ActivityUtil.jump(UserLoginActivity.this, SinaSSOAuthActivity.class, 0, bundle);
			}

		});
		// 腾讯微博账号登录
		loginByTencent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("腾讯按钮");
				// -----
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, true);

				CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

					@Override
					public void onComplete(boolean isSuccessful) {
						if (isSuccessful) {
							SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
						}
						finish();
					}
				}));
				AuthWebActivity.currentWeiboUtil = WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
				ActivityUtil.jump(UserLoginActivity.this, AuthWebActivity.class, 0, bundle);
			}

		});
		
		//邀请码登录
		login_by_invite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("邀请按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(UserLoginActivity.this, InviteRegistrationActivity.class, 0,bundle);
				finish();
				
			}
		});
	}

	
}
