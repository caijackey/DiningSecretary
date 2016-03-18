package com.fg114.main.weibo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.SoftwareCommonData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver.WeiboAuthResultObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.task.SinaWeiboSSOBindTask;
import com.fg114.main.weibo.task.SinaWeiboSSOLoginTask;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;
import com.weibo.sdk.android.util.Utility;

/**
 * 新浪微博SSO登录
 * 
 */
public class SinaSSOAuthActivity extends MainFrameActivity {
	
	private static final String TAG = "SinaSSOAuthActivity";
	private static final boolean DEBUG = Settings.DEBUG;
	public static WeiboUtil currentWeiboUtil;

	public boolean isLogin;

	private SsoHandler mSsoHandler;
	private String appKey;
	private String redirectUrl;

	// 传入参数获得
	private int fromPage; // 返回页面

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	// 微博登陆任务
	private SinaWeiboSSOLoginTask weiboLoginTask;
	private SinaWeiboSSOBindTask weiboBindTask;
	private Weibo weibo;
	private ProgressBar progressBar;
	private LinearLayout webview_controlpanel; //下面控制条的
	
	
	private WebView mWebView;
	private boolean mAuthSuccess;
	private AuthDialogListener authListener = new AuthDialogListener();
	//
	private boolean isSuccessful = false; // 指示本次授权登录（或绑定）是否成功
	private boolean needHint = true; // 返回时是否要提示

	private UserInfoDTO infoDTO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null || currentWeiboUtil == null) {
			DialogUtil.showToast(this, "没有所需的数据！");
			this.finish();
			return;
		}
		isLogin = bundle.getBoolean(Settings.BUNDLE_KEY_IS_LOGIN);

		SoftwareCommonData softCommonData = SessionManager.getInstance().getSoftwareCommonData();

		
		
		appKey = softCommonData.getSinaAppKey();
//		appKey="674868809";
		redirectUrl = softCommonData.getSinaInterceptUrl();
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		infoDTO = SessionManager.getInstance().getUserInfo(SinaSSOAuthActivity.this);
		// 初始化界面
		initComponent();
	}

	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("新浪微博授权");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				needHint = false;
				finish();
			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.simple_webview, null);
		progressBar = (ProgressBar) contextView.findViewById(R.id.simple_webview_progress_bar);
		webview_controlpanel = (LinearLayout) contextView.findViewById(R.id.webview_controlpanel);
		webview_controlpanel.setVisibility(View.INVISIBLE);

		mWebView = (WebView) contextView.findViewById(R.id.simple_webview);

		mWebView.getSettings().setJavaScriptEnabled(true);// 允许使用js
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (!url.startsWith(redirectUrl)) {
					super.onPageStarted(view, url, favicon);
					return;
				}
				progressBar.setProgress(1);
				handleRedirectUrl(url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				DialogUtil.showAlert(SinaSSOAuthActivity.this, "载入时发生错误:" + errorCode, description);

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!url.startsWith(redirectUrl)) {
					return super.shouldOverrideUrlLoading(view, url);
				}
				handleRedirectUrl(url);
				return true;
			}

		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			/**
			 * 页面关闭
			 */
			@Override
			public void onCloseWindow(WebView window) {
				super.onCloseWindow(window);
				finish();
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				return super.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				return super.onJsPrompt(view, url, message, defaultValue, result);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
				if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				}
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}

		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mWebView.loadUrl("about:blank");
		startAuth();
	}

	private void startAuth() {
		weibo = Weibo.getInstance(appKey, redirectUrl);
		mSsoHandler = new SsoHandler(this, weibo);
		mSsoHandler.authorize(authListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/**
		 * 下面两个注释掉的代码，仅当sdk支持sso时有效，
		 */
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			// {uid=2611015907, expires_in=721689, remind_in=721689,
			// access_token=2.00nVYhqC0NQNYn040ae947f20K_ubl}
			String access_token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			String uid = values.getString("uid");
			// DialogUtil.showToast(SinaSSOAuthActivity.this,
			// "ac="+access_token+",ex="+expires_in+",u="+uid);
			if (isLogin) {
				// 是登录去登录
				excuteWeiboLoginTask(uid, access_token, Long.valueOf(expires_in));
			} else {
				// 是绑定，去绑定
				excuteWeiboBindTask(uid, access_token, Long.valueOf(expires_in));
			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			DialogUtil.showToast(getApplicationContext(), e.getMessage());
			finish();
		}

		@Override
		public void onCancel() {
			DialogUtil.showToast(getApplicationContext(), "授权已取消");
			finish();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			if (e.getStatusCode() == 20130506) {// 见Weibo.startDialog(Context,WeiboParameters,WeiboAuthListener)
				// 没有安装客户端，需要web认证
				mWebView.loadUrl(e.getMessage());
			} else {
				DialogUtil.showToast(getApplicationContext(), e.getMessage());
				finish();
			}
		}
	}

	private void handleRedirectUrl(String url) {
		if (mAuthSuccess) {
			return;
		}
		mAuthSuccess = true;
		// ---
		Bundle values = Utility.parseUrl(url);
		String error = values.getString("error");
		String error_code = values.getString("error_code");

		if (error == null && error_code == null) {
			authListener.onComplete(values);
		} else if (error.equals("access_denied")) {
			// 用户或授权服务器拒绝授予数据访问权限
			authListener.onCancel();
		} else {
			if (error_code == null) {
				authListener.onWeiboException(new WeiboException(error, 0));
			} else {
				authListener.onWeiboException(new WeiboException(error, Integer.parseInt(error_code)));
			}

		}

		// 清cookie
		ActivityUtil.clearCookies(SinaSSOAuthActivity.this);
	}

	/**
	 * 执行绑定的task
	 */
	private void excuteWeiboBindTask(final String uid, final String access_token, final Long expires_in) {

		ServiceRequest request = new ServiceRequest(API.bindToSinaWeiboByAccessToken);
		request.addData("uuid", uid);
		request.addData("accessToken", access_token);
		request.addData("remainSecs", expires_in);
		CommonTask.request(request, "正在登录，请稍候...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				isSuccessful = true;
				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			protected void onError(int code, String message) {
				//				doTest();
				finish();
				return;
			};
		});

	}

	/**
	 * 执行微博登录task
	 */
	private void excuteWeiboLoginTask(String uid, String access_token, Long expires_in) {

		ServiceRequest request = new ServiceRequest(API.bindToSinaWeiboByAccessToken);
		request.addData("uuid", uid);
		request.addData("accessToken", access_token);
		request.addData("remainSecs", expires_in);
		CommonTask.request(request, "正在登录，请稍候...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void v) {
				isSuccessful = true;
				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			protected void onError(int code, String message) {
				//				doTest();
				finish();
				return;
			};
		});

	}

	@Override
	public void finish() {
		super.finish();
		String msg = "微博";
		if (isLogin) {
			msg += "登录";
		} else {
			msg += "绑定";
		}
		if (isSuccessful) {
			msg += "成功";
		} else {
			msg += "失败";
		}
		if (needHint) {
			DialogUtil.showToast(this, msg);
		}
		CommonObservable.getInstance().notifyObservers(WeiboAuthResultObserver.class, isSuccessful);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			needHint = false;
			try {
				if (weiboLoginTask != null) {
					weiboLoginTask.cancel(true);
					weiboLoginTask = null;
				}
				if (weiboBindTask != null) {
					weiboBindTask.cancel(true);
					weiboBindTask = null;
				}
			} catch (Exception e) {
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView.clearCache(true);
			mWebView.clearHistory();
			mWebView.clearFocus();
			mWebView.clearView();
			mWebView.destroy();
		}
	}

	private void doTest() {

		String json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"true\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"false\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
		UserInfoDTO dto = JsonUtils.fromJson(json, UserInfoDTO.class);
		infoDTO.setUuid(dto.getUuid());
		infoDTO.setNickName(dto.getNickName());
		infoDTO.setTel(dto.getTel());
		infoDTO.setToken(dto.getToken());
		infoDTO.setPicUrl(dto.getPicUrl());
		infoDTO.setSexTag(dto.getSexTag());
		infoDTO.setPointNum(dto.getPointNum());
		infoDTO.setLevel(dto.getLevel());
		infoDTO.setSinaBindTag(dto.isSinaBindTag());
		infoDTO.setSinaAccount(dto.getSinaAccount());
		infoDTO.setSinaBindRemainSecs(dto.getSinaBindRemainSecs());
		infoDTO.setSinaBindRemainSecsTimestamp(dto.getSinaBindRemainSecsTimestamp());
		infoDTO.setQqBindTag(dto.isQqBindTag());
		infoDTO.setQqAccount(dto.getQqAccount());
		infoDTO.setQqBindRemainSecs(dto.getQqBindRemainSecs());
		infoDTO.setQqBindRemainSecsTimestamp(dto.getQqBindRemainSecsTimestamp());
		SessionManager.getInstance().setUserInfo(SinaSSOAuthActivity.this, infoDTO);
		isSuccessful = true;
		SessionManager.getInstance().setIsUserLogin(SinaSSOAuthActivity.this, true);
	}
}
