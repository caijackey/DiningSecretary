package com.fg114.main.app.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.usercenter.ShareToWeiXinActivity;
import com.fg114.main.app.activity.usercenter.ShareToWeiboActivity;
import com.fg114.main.service.dto.ShareInfoData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;

/**
 * 简单网页载入，浏览页面
 * 
 * @author xujianjun,2012-06-26
 * 
 */
public class SimpleWebViewActivity extends MainFrameActivity {

	// 传入参数获得
	private int fromPage; // 返回页面

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private WebView mWebView;
	// 网页顶部进度条
	private ProgressBar progressBar = null;
	private ShareInfoData shareInfoData = null;

	// 下方控制条
	private Button btBack; // 后退按钮
	private Button btForward; // 前进按钮
	private Button btRefresh; // 刷新按钮
	private Button btStop; // 停止按钮
	private Button btReturn; // 退出webview返回前面的activity
	private ProgressBar progressBarDown; // 下面控制条的progressbar

	private String weburl;
	private String title;
	private boolean shouldOverrideUrlLoading = true;
	// 需要隐藏本身的title，使用web页里面的导航和标题
	private boolean needHideTitle;
	public static boolean needCallFinishLogin = false;
	private boolean hasLogined;

	private String token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) {
			DialogUtil.showToast(this, "没有可浏览的页面！");
			this.finish();
			return;
		}

		// 取出要显示的weburl地址
		weburl = bundle.getString(Settings.BUNDLE_KEY_WEB_URL);
		title = bundle.getString(Settings.BUNDLE_KEY_WEB_TITLE);
		needHideTitle = bundle.getBoolean(Settings.BUNDLE_KEY_WEB_HIDE_TITLE, false);
		if (bundle.containsKey("shouldOverrideUrlLoading")) {
			shouldOverrideUrlLoading = bundle.getBoolean("shouldOverrideUrlLoading", true);
		}
		// -----
		if (ActivityUtil.isTestDev(ContextUtil.getContext())) {
			Settings.requestLog.insert(0, "##### Web页请求 " + CalendarUtil.getDateTimeString() + " #####\n" + weburl + "\n##############################\n");
		}
		// -----
		if (CheckUtil.isEmpty(weburl)) {
			DialogUtil.showToast(this, "指定页面未找到！");
			this.finish();
			return;
		}

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		hasLogined = SessionManager.getInstance().isUserLogin(this);
		// 当登录成功后回调javascript1里面的doFinishLogin方法
		if (hasLogined && needCallFinishLogin) {
			token = SessionManager.getInstance().getUserInfo(SimpleWebViewActivity.this).getToken();
			mWebView.loadUrl("javascript:doFinishLogin(\"" + token + "\")");
//			mWebView.loadUrl("javascript:document.title");
			needCallFinishLogin = false;
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("订餐小秘书");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		if (getLastActivityClass() == IndexActivity.class) {
			this.getBtnGoBack().setText(R.string.text_button_goto_index);
		}
		if (!CheckUtil.isEmpty(title)) {
			this.getTvTitle().setText(title);
		}
		if (needHideTitle) {
			getTitleLayout().setVisibility(View.GONE);
		}
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.simple_webview, null);
		progressBar = (ProgressBar) contextView.findViewById(R.id.simple_webview_progress_bar);

		// 控制条按钮
		progressBarDown = (ProgressBar) contextView.findViewById(R.id.webview_progress_down);
		btBack = (Button) contextView.findViewById(R.id.webview_back_bt);
		btForward = (Button) contextView.findViewById(R.id.webview_forward_bt);
		btRefresh = (Button) contextView.findViewById(R.id.webview_refresh_bt);
		btStop = (Button) contextView.findViewById(R.id.webview_stop_bt);
		btReturn = (Button) contextView.findViewById(R.id.webview_return_bt);
		btBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				mWebView.goBack();
			}
		});
		btForward.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				mWebView.goForward();
			}
		});
		btRefresh.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				mWebView.loadUrl(weburl);
			}
		});
		btStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				mWebView.stopLoading();
				progressBarDown.setVisibility(View.INVISIBLE);
				btStop.setVisibility(View.INVISIBLE);
				btRefresh.setVisibility(View.VISIBLE);
			}
		});
		btReturn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				finish();
			}
		});

		mWebView = (WebView) contextView.findViewById(R.id.simple_webview);
		// mWebView.getSettings().setAppCacheEnabled(true);
		// mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		mWebView.getSettings().setJavaScriptEnabled(true);// 允许使用js
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				progressBarDown.setVisibility(View.INVISIBLE);
				btStop.setVisibility(View.INVISIBLE);
				btRefresh.setVisibility(View.VISIBLE);
			
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setProgress(1);

				// 页面加载判断下方控制条是否可以点击back或者forward
				btBack.setEnabled(mWebView.canGoBack());
				btForward.setEnabled(mWebView.canGoForward());
				progressBarDown.setVisibility(View.VISIBLE);
				btStop.setVisibility(View.VISIBLE);
				btRefresh.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				DialogUtil.showAlert(SimpleWebViewActivity.this, "载入时发生错误:" + errorCode, description);
			}

			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				return super.shouldOverrideKeyEvent(view, event);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				// 替换返回按钮事件测试用xms跳转用
//				 if (url.equals("xms://do/close")) 
//				 url = "xms://toprestdetail/84/AESH10000271";
				if (!shouldOverrideUrlLoading && !url.equals(weburl)) {
					ActivityUtil.jumbToWeb(SimpleWebViewActivity.this, url);
					return true;
				}
				try {
					if (URLExecutor.execute(url, SimpleWebViewActivity.this, 0)) {
						return true;
					}
					if (url.toLowerCase().endsWith(".apk")) {
						ActivityUtil.jumbToWeb(SimpleWebViewActivity.this, url);
						return true;
					}

					// 分享页面 // xms://do/needShare/{uuid}
					// xms://do/needShare/sms;email;weibo;weixin;pengyouquan/{uuid}
					if (url != null && url.startsWith("xms://do/needShare/")) {
						try {
							url = URLDecoder.decode(url, "UTF-8");
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						int start = 19;
						String temp = url.substring(start);
						if (CheckUtil.isEmpty(temp)) {
							return true;
						}
						String[] data = temp.split("/");

						if (data.length == 1) {
							// xms://do/needShare/{uuid}
							String[] dynamicshareDetail = new String[5];
							dynamicshareDetail[0] = "短信分享";
							dynamicshareDetail[1] = "邮件分享";
							dynamicshareDetail[2] = "分享到微博";
							dynamicshareDetail[3] = "分享到微信";
							dynamicshareDetail[4] = "分享到微信朋友圈";
							executeGetWapShareInfo(data[0], dynamicshareDetail);
						}

						if (data.length == 2) {
							String[] shareData = data[0].split(";");
							if (shareData.length != 0) {
								String[] dynamicshareDetail = new String[shareData.length];
								for (int m = 0; m < shareData.length; m++) {
									if (shareData[m].equals("sms")) {
										dynamicshareDetail[m] = "短信分享";
									} else if (shareData[m].equals("email")) {
										dynamicshareDetail[m] = "邮件分享";
									} else if (shareData[m].equals("weibo")) {
										dynamicshareDetail[m] = "分享到微博";
									} else if (shareData[m].equals("weixin")) {
										dynamicshareDetail[m] = "分享到微信";
									} else if (shareData[m].equals("pengyouquan")) {
										dynamicshareDetail[m] = "分享到微信朋友圈";
									}
								}
								executeGetWapShareInfo(data[1], dynamicshareDetail);
							}
						}

						return true;
					}

					// 关闭页面
					if (url.equals("xms://do/close")) {
						finish();
						return true;
					}

				} catch (Exception e) {
					e.printStackTrace();
					return super.shouldOverrideUrlLoading(view, url);
				}
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
				if (CheckUtil.isEmpty(SimpleWebViewActivity.this.title)) {
					getTvTitle().setText(title);
				}
			}

		});
		mWebView.loadUrl(weburl);

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// 获取wap分享
	private void executeGetWapShareInfo(String uuid, final String[] dynamicshareDetail) {
		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getWapShareInfo);
		request.addData("uuid", uuid);

		if (dynamicshareDetail == null) {
			return;
		}
		CommonTask.request(request, new CommonTask.TaskListener<ShareInfoData>() {
			@Override
			protected void onSuccess(ShareInfoData dto) {
				shareInfoData = dto;
				showShareDialog(5, dynamicshareDetail);
			}

			protected void onError(int code, String message) {
				// doTest_confirm();
				super.onError(code, message);
			}

			private void doTest_confirm() {
				String json = "{\"shareSmsDetail\":\"shareSmsDetail\",\"shareEmailDetail\":\"shareEmailDetail\",\"shareWeiboDetail\":\"shareWeiboDetail\",\"shareWeixinIconUrl\":\"shareWeixinIconUrl\",\"shareWeixinDetailUrl\":\"shareWeixinDetailUrl\",\"shareWeixinDetail\":\"shareWeixinDetail\",\"shareWeixinName\":\"shareWeixinName\",\"shareWeiboUuid\":\"shareWeiboUuid\"}";
				ShareInfoData data = JsonUtils.fromJson(json, ShareInfoData.class);
				onSuccess(data);

			}
		});
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return shareInfoData == null ? "" : shareInfoData.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return shareInfoData == null ? "" : shareInfoData.shareWeiboUuid;
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

	private String[] dynamicshareDetail;
	private int typeTag;

	// 动态添加选择列表
	// 1:分享餐厅 2：软件分享 3：推荐分享 4:外卖分享 5:wap分享6:秘币分享
	protected void showShareDialog(int typeTag, String[] dynamicshareDetail) {
		this.typeTag = typeTag;
		this.dynamicshareDetail = dynamicshareDetail;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(dynamicshareDetail, DynamicshareRes).setTitle("分享").setNegativeButton("取消", null).create();
		builder.show();
	}

	// 动态分享按钮功能
	protected DialogInterface.OnClickListener DynamicshareRes = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			// String uuid = typeTag == 3 ? getRecomRestaurantId() :
			// getRestaurantId();
			String uuid = "";
			String weixinName = "";
			switch (typeTag) {
			case 1:
				// 1:分享餐厅
				uuid = getRestaurantId();
				weixinName = getRestaurantName();
				break;
			case 2:
				// 2：软件分享
				uuid = "";
				weixinName = getRestaurantName();
				break;
			case 3:
				// 3：推荐分享
				uuid = getRecomRestaurantId();
				weixinName = getRestaurantName();
				break;
			case 4:
				// 4:外卖分享
				uuid = getWeiboUuid();
				weixinName = getWeixinName();
				break;
			case 5:
				// 5:wap分享
				uuid = getWeiboUuid();
				weixinName = getWeixinName();
				break;
			case 6:
				// 6:秘币分享
				uuid = getWeiboUuid();
				weixinName = getWeixinName();
				break;
			default: // 其它分享
				uuid = getRestaurantId();
				weixinName = getRestaurantName();
				break;
			}

			jumpShare(which, uuid, weixinName);

			mWebView.loadUrl("javascript:doSelectShareType('" + doSelectShareType + "')");
		}
	};

	private String doSelectShareType = "";

	private void jumpShare(int i, String uuid, String weixinName) {
		if (dynamicshareDetail[i].equals("短信分享")) {
			// ----
			OpenPageDataTracer.getInstance().addEvent("分享-短信", uuid);
			// -----
			doSelectShareType = "sms";

			try {
				String info = makeSMSinfo();
				ActivityUtil.sendSMS(SimpleWebViewActivity.this, "", info);
			} catch (Exception e) {
				DialogUtil.showToast(SimpleWebViewActivity.this, "对不起，暂时无法分享");
			}
		} else if (dynamicshareDetail[i].equals("邮件分享")) {
			// ----
			OpenPageDataTracer.getInstance().addEvent("分享-邮件", uuid);
			// -----
			doSelectShareType = "email";
			try {
				String emailInfo = makeEmailInfo();
				ActivityUtil.callEmail((Activity) SimpleWebViewActivity.this, "", "看看这家餐厅怎么样", emailInfo);
			} catch (Exception e) {
				DialogUtil.showToast(SimpleWebViewActivity.this, "对不起，暂时无法分享");
			}
		} else if (dynamicshareDetail[i].equals("分享到微博")) {
			// ----
			OpenPageDataTracer.getInstance().addEvent("分享-微博", uuid);
			// -----
			doSelectShareType = "weibo";
			String weiboInfo = makeWeiboInfo();
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_KEY_WEIBO_DETAIL, weiboInfo);
			bundle.putString(Settings.BUNDLE_REST_ID, uuid);
			bundle.putInt(Settings.BUNDLE_KEY_TYPE, typeTag);// 1:分享餐厅
																// 2：软件分享
																// 3：推荐分享
																// 4:外卖分享
																// 5:wap分享

			ActivityUtil.jump(SimpleWebViewActivity.this, ShareToWeiboActivity.class, 0, bundle);
		} else if (dynamicshareDetail[i].equals("分享到微信")) {
			// ----
			OpenPageDataTracer.getInstance().addEvent("分享-微信", uuid);
			// -----
			doSelectShareType = "weixin";
			String info = "";
			if (CheckUtil.isEmpty(makeWeiXinInfo())) {
				info = makeWeiboInfo() + "【来自小秘书客户端】http://www.xiaomishu.com/o/app";
			} else {
				info = makeWeiXinInfo();
			}
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_KEY_SHARE_DETAIL, info);
			bundle.putString(Settings.BUNDLE_REST_ID, uuid);
			bundle.putString(Settings.BUNDLE_REST_NAME, weixinName);
			bundle.putString(Settings.BUNDLE_REST_IMAGE_URL, getRestaurantUrl() != null ? getRestaurantUrl().trim() : getRestaurantUrl()); // 送给微信的时候，要trim一下
			bundle.putString(Settings.BUNDLE_REST_LINK_URL, getRestaurantLinkUrl() != null ? getRestaurantLinkUrl().trim() : getRestaurantLinkUrl());// 送给微信的时候，要trim一下
			bundle.putInt(Settings.BUNDLE_KEY_TYPE, 1); // 1分享到微信,
														// 2//分享到微信朋友圈
			ActivityUtil.jump(SimpleWebViewActivity.this, ShareToWeiXinActivity.class, 0, bundle);
		} else if (dynamicshareDetail[i].equals("分享到微信朋友圈")) {
			// ----
			OpenPageDataTracer.getInstance().addEvent("分享-朋友圈", uuid);
			// -----
			doSelectShareType = "pengyouquan";
			String infoP = "";
			if (CheckUtil.isEmpty(makeWeiXinInfo())) {
				infoP = makeWeiboInfo() + "【来自小秘书客户端】http://www.xiaomishu.com/o/app";
			} else {
				infoP = makeWeiXinInfo();
			}
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_KEY_SHARE_DETAIL, infoP);
			bundle.putString(Settings.BUNDLE_REST_ID, uuid);
			bundle.putString(Settings.BUNDLE_REST_NAME, weixinName);
			bundle.putString(Settings.BUNDLE_REST_IMAGE_URL, getRestaurantUrl() != null ? getRestaurantUrl().trim() : getRestaurantUrl());// 送给微信的时候，要trim一下
			bundle.putString(Settings.BUNDLE_REST_LINK_URL, getRestaurantLinkUrl() != null ? getRestaurantLinkUrl().trim() : getRestaurantLinkUrl());// 送给微信的时候，要trim一下
			bundle.putInt(Settings.BUNDLE_KEY_TYPE, 2);// 1分享到微信,
														// 2//分享到微信朋友圈
			ActivityUtil.jump(SimpleWebViewActivity.this, ShareToWeiXinActivity.class, 0, bundle);
		}

	}

	// 如果是隐藏title的，使用内部导航
	// void goBack() {
	// if (needHideTitle) {
	// getTitleLayout().setVisibility(View.GONE)
	// }
	// }
}
