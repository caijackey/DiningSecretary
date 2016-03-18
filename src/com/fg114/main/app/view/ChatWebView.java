package com.fg114.main.app.view;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.chat.DownloadAsyncTask;
import com.fg114.main.app.activity.chat.XiaomishuChat;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CipherUtils;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;

/**
 * WebView
 * 
 * @author zhangfan caijie
 * 
 */
public class ChatWebView extends WebView {
	private String TAG = ChatWebView.class.getName();

	private Context context;

	// 传入参数获得
	private int fromPage; // 返回页面

	private String token;

	public ChatWebView(Context ctx) {
		super(ctx);
		context = ctx;

		initComponent();
	}

	public ChatWebView(Context ctx, AttributeSet paramAttributeSet) {
		super(ctx, paramAttributeSet);
		context = ctx;

		initComponent();
	}

	public interface ReloadURL {
		void startReloadUrlParmas();
	}

	/**
	 * javascript参数配置
	 */
	private void setUrlParams() {
		// 增加gps信息
		boolean haveGpsTag = Loc.isGpsAvailable();
		double longitude = 0;
		double latitude = 0;
		if (haveGpsTag) {
			LocInfo myLoc = Loc.getLocImmediately();
			if (myLoc == null || myLoc.getLoc() == null) {
				haveGpsTag = false;
			} else {
				longitude = myLoc.getLoc().getLongitude();
				latitude = myLoc.getLoc().getLatitude();
			}
		}
		CityInfo city = SessionManager.getInstance().getCityInfo(
				ContextUtil.getContext());
		String cityId = null;
		if (city == null || TextUtils.isEmpty(city.getId())) {
			cityId = "";
		} else {
			cityId = city.getId();
		}

		// 将 设备号+时间戳 放入请求头
		String content = Settings.DEV_ID + ","
				+ String.valueOf(System.currentTimeMillis());
		String enc = "";
		try {
			enc = CipherUtils.encodeXms(content);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		loadUrl("javascript:initChat('1.0','"
				// + ActivityUtil.getVersionName(context)
				+ Settings.VERSION_NAME
				+ "','"
				+ Settings.DEV_ID
				// + ActivityUtil.getDeviceId(context)
				+ "','" + Build.VERSION.SDK_INT + "','"
				+ ActivityUtil.getDeviceType() + "',"
				+ ActivityUtil.getWindowDisplay(context).getWidth() + ","
				+ ActivityUtil.getWindowDisplay(context).getHeight() + ",'"
				+ SessionManager.getInstance().getUserInfo(context).getToken()
				+ "','" + Settings.SELL_CHANNEL_NUM + "','" + cityId + "',"
				+ haveGpsTag + "," + longitude + "," + latitude + ","
				+ ActivityUtil.isWifi(context) + ",'首页',true,'" + enc
				+ "','1','"
				+ SessionManager.getInstance().getUserInfo(context).getPicUrl()
				+ "')");

	}

	/**
	 * 初始化
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void initComponent() {
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context
				.getApplicationContext());
		if (!isNetAvailable) {
			DialogUtil.showAlert(context, "警告", "网络连接出错，请检查网络后重试！");
		}

		getSettings().setJavaScriptEnabled(true);// 允许使用js
		// 开启alert
		getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		// 开启按钮按下显示
		getSettings().setLightTouchEnabled(true);
		getSettings().setUseWideViewPort(true);

		setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				setUrlParams();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				DialogUtil.showAlert(context, "载入时发生错误:" + errorCode,
						description);
			}

			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				return super.shouldOverrideKeyEvent(view, event);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (CheckUtil.isEmpty(url)) {
					return true;
				}
				// 进入房间成功
				if (url.contains(Settings.CHAT_ENTER_ROOM_SUCCESS_FLAG)) {
					XiaomishuChat.getInstance(context).enableSendMsg();
					return true;
				}
				// 网页需要升级
				if (url.contains(Settings.CHAT_NEED_UPDATE)) {
					String[] strArr = URLDecoder.decode(url)
							.replace(Settings.CHAT_NEED_UPDATE + "/", "")
							.split("/");
					String downloadUrl = strArr[0];
					String version = strArr[1];

					// 后台下载
					DownloadAsyncTask downloadTask = new DownloadAsyncTask(
							new ChatWebView.ReloadURL() {
								@Override
								public void startReloadUrlParmas() {
									XiaomishuChat.getInstance(context)
											.loadWebviewSite("index.htm");
									setUrlParams();
								}
							});
					downloadTask.execute(downloadUrl);

					SharedprefUtil.save(context, "version", version);

					DialogUtil.showToast(context, "需要升级");
					return true;
				}
				// 有新消息
				if (url.contains(Settings.CHAT_HAVE_NEW_MESSAGES)) {
					String number = URLDecoder.decode(url).replace(
							Settings.CHAT_HAVE_NEW_MESSAGES + "/", "");
					DialogRemindFloatWindow.getInstance(context)
							.showMessageNumber(Integer.valueOf(number));
					return true;
				}
				// 根据餐厅链接跳转到餐厅页面
				if (url.contains(Settings.CHAT_JUMP_TO_RESTAURANT)) {
					int start = 17;
					Bundle bundle = new Bundle();
					String restID = url.substring(start);
					try {
						restID = URLDecoder.decode(restID, "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					bundle.putString(Settings.BUNDLE_KEY_ID, restID);
					Intent intent = new Intent(context,
							RestaurantDetailActivity.class);
					intent.putExtras(bundle);
					context.startActivity(intent);
					return true;
				}
				// 需要发GPS信息
				if (url.contains(Settings.CHAT_NEED_SEND_GPS_FLAG)) {
					DialogUtil.showToast(context, "发gps");
					return true;
				}
				// 房间已被服务器强制关闭
				if (url.contains(Settings.CHAT_ROOM_CLOSED_FLAG)) {
					XiaomishuChat.getInstance(context).disableSendMsg();
					if (XiaomishuChat.getInstance(context).getDialogState() != XiaomishuChat.DIALOG_STATE_CLOSING) {
						// 如果对话没有被用户主动关闭，则表示是服务器强制关闭
						XiaomishuChat.getInstance(context)
								.setRoomIsClosedByServer(true);
						DialogUtil.showToast(context, "已被强制关闭");
					}
					DialogUtil.showToast(context, "已退出房间");
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

		});

		setWebChromeClient(new WebChromeClient() {

			/**
			 * 页面关闭
			 */
			@Override
			public void onCloseWindow(WebView window) {
				super.onCloseWindow(window);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				return super.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, JsPromptResult result) {
				return super.onJsPrompt(view, url, message, defaultValue,
						result);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}

		});

	}

	/**
	 * 发消息
	 * 
	 * @param text
	 */
	public void sendMessage(String text) {
		if (CheckUtil.isEmpty(text)) {
			return;
		}
		loadUrl("javascript:sendMsgSendTxt('" + text.replaceAll("'", "\\\\'")
				+ "')");
	}

	/**
	 * 最小化对话框时调用此方法，告诉服务器当前处于最小化状态
	 */
	public void sendMsgMinimizeRoom() {
		loadUrl("javascript:sendMsgMinimizeRoom()");
	}

	/**
	 * 最大化对话框时调用此方法，告诉服务器当前处于最大化状态
	 */
	public void sendMsgMaximizeRoom(String bp) {
		loadUrl("javascript:sendMsgMaximizeRoom('" + bp + "')");
	}

	/**
	 * 退出房间（主动退出，点“退出”按钮）时调用此方法，告诉服务器退出房间
	 */
	public void sendMsgLeaveRoom() {
		loadUrl("javascript:sendMsgLeaveRoom()");
		loadUrl("");
	}

	/**
	 * 退出房间（非主动退出，将APP切出去）时调用此方法，告诉服务器退出房间
	 */
	public void sendMsgCloseApp() {
		loadUrl("javascript:sendMsgCloseApp()");
		loadUrl("");
	}

	/**
	 * 将对话框滚动到底部
	 */
	public void scrollToBottom() {
		loadUrl("javascript:moveToBottom()");
	}

}
