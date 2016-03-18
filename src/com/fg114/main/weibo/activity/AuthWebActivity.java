package com.fg114.main.weibo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver.WeiboAuthResultObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.AuthUrls;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.task.WeiboBindTask;
import com.fg114.main.weibo.task.WeiboLoginTask;

/**
 * 腾讯微博帐号登录
 * 
 * @author chengguojin
 * 
 */
public class AuthWebActivity extends MainFrameActivity {

	private static final String TAG = "AuthWebActivity";
	private static final boolean DEBUG = Settings.DEBUG;

	public static WeiboUtil currentWeiboUtil;
	// 传入参数获得
	private int fromPage; // 返回页面

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private WebView mWebView;
	// 进度条
	private ProgressBar progressBar = null;
	private LinearLayout webview_controlpanel; //下面控制条的
	
	private String weburl;
	private String title;

	// 授权url
	private AuthUrls urls;

	// 微博登陆任务
	private WeiboLoginTask weiboLoginTask;
	private WeiboBindTask weiboBindTask;

	// 是否是微博登录
	private boolean isLogin;
	private boolean mAuthSuccess;
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

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		infoDTO = SessionManager.getInstance().getUserInfo(AuthWebActivity.this);
		// 初始化界面
		initComponent();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("腾讯微博授权");
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
		mWebView = (WebView) contextView.findViewById(R.id.simple_webview);
		webview_controlpanel = (LinearLayout) contextView.findViewById(R.id.webview_controlpanel);
		webview_controlpanel.setVisibility(View.INVISIBLE);
		
		mWebView.getSettings().setJavaScriptEnabled(true);// 允许使用js
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (!url.startsWith(urls.redirectUrl)) {
					super.onPageStarted(view, url, favicon);
					return;
				}
				progressBar.setProgress(1);
				processAuthCode(url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				DialogUtil.showAlert(AuthWebActivity.this, "载入时发生错误:" + errorCode, description);

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!url.startsWith(urls.redirectUrl)) {
					return super.shouldOverrideUrlLoading(view, url);
				}
				processAuthCode(url);
				return super.shouldOverrideUrlLoading(view, url);
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
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
					JsPromptResult result) {
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

			/*@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (CheckUtil.isEmpty(AuthWebActivity.this.title)) {
					getTvTitle().setText(title);
					Log.e("title", title);
				}
			}*/

		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mWebView.setVisibility(View.GONE);
		excuteGetUrlsTask();
	}

	/**
	 * 获取url
	 */
	private void excuteGetUrlsTask() {
		try {
			urls = currentWeiboUtil.getAuthUrls();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (urls == null || CheckUtil.isEmpty(urls.authWebUrl) || CheckUtil.isEmpty(urls.redirectUrl)) {
			finish();
			return;
		}
		mWebView.setVisibility(View.VISIBLE);
		mWebView.loadUrl(urls.authWebUrl);
	}

	/**
	 * 执行微博登录task
	 */
	private void excuteWeiboLoginTask(String code) {

		ServiceRequest request = new ServiceRequest(API.bindToWeibo);
		request.addData("typeTag", 2);
		request.addData("code", code);
		CommonTask.request(request, "正在登录，请稍候...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				isSuccessful = true;
				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			protected void onError(int code, String message) {
//				doTest();
				DialogUtil.showToast(AuthWebActivity.this, message);
				finish();

			};
		});
	}

	/**
	 * 执行绑定的task
	 */
	private void excuteWeiboBindTask(final String code, final boolean forceBind) {

		ServiceRequest request = new ServiceRequest(API.bindToWeibo);
		request.addData("typeTag", 2);
		request.addData("code", code);
		CommonTask.request(request, "正在绑定，请稍候...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				isSuccessful = true;
				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			protected void onError(int code, String message) {
				DialogUtil.showToast(AuthWebActivity.this, message);
				finish();

			};
		});

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
			this.getMainLayout().removeView(mWebView);  
			mWebView.removeAllViews();  
			mWebView.stopLoading();
			mWebView.clearCache(true);
			mWebView.clearHistory();
			mWebView.clearFocus();
			mWebView.clearView();
			mWebView.destroy();
		}
	}

	private void processAuthCode(String url) {

		if (mAuthSuccess) {
			return;
		}
		Uri callBackUri = Uri.parse(url);
		String code = callBackUri.getQueryParameter("code"); // 如果没有code，说明在网页里按了“取消”，要返回前一页面
		if (CheckUtil.isEmpty(code)) {
			finish();
			return;
		}
		mAuthSuccess = true;
		mWebView.setVisibility(View.INVISIBLE);
		title = "微博认证";

		// ----------
		// 清cookie
		ActivityUtil.clearCookies(AuthWebActivity.this);
		// 如果是登录，去后台登录
		if (isLogin) {
			excuteWeiboLoginTask(code);
		} else {// 如果是绑定，去后台绑定（先非强制）
			excuteWeiboBindTask(code, false);
		}
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

}
