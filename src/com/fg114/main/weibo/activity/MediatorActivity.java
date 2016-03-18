package com.fg114.main.weibo.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.WeiboUtilFactory;

/**
 * 微博功能的中间页。主要封装需要分享微博的逻辑，未登录去登录，然后再看是否绑定，引导用户绑定
 * 本页面没有界面，是中转页，页面中要检查参数是否合法，如果不合法，就将自己关闭
 * 
 * @author xujianjun,2012-08-06
 * 
 */

public class MediatorActivity extends Activity {
	// 记录中间页的状态:
	// 初始请求"request"、去登录"login"、去绑定"bind"
	public static final int STATUS_SHARE_REQUEST = 1; // 开始
	// public static final int STATUS_SHARE_REQUEST_OVER = 10; //结束

	public static final int STATUS_LOGIN_REQUEST = 2; // 去登录
	// public static final int STATUS_LOGIN_REQUEST_OVER = 20; //登录返回

	public static final int STATUS_BIND_REQUEST = 3; // 去绑定微博
	// public static final int STATUS_BIND_REQUEST_OVER = 30; //绑定微博返回

	private static int status = STATUS_SHARE_REQUEST; // 当前状态
	// public static boolean resultSuccess=true; //返回状态

	private int page;
	private boolean isBind = false;
	private boolean isExpired = false;
	private UserInfoDTO infoDTO;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		page = this.getIntent().getExtras().getInt("page");
		status = STATUS_SHARE_REQUEST; // 开始

	}

	public void init(int page) {
		switch (page) {
		// tx
		case 0:
			isBind = infoDTO.isQqBindTag();
			isExpired = infoDTO.isQQWeiboExpired();
			break;
		// sina
		case 1:
			isBind = infoDTO.isSinaBindTag();
			isExpired = infoDTO.isSinaWeiboExpired();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		infoDTO = SessionManager.getInstance().getUserInfo(this);
		init(page);
		super.onResume();
		switch (status) {
		case STATUS_SHARE_REQUEST:
			doRequestShare();
			break;

		case STATUS_LOGIN_REQUEST:
			doRequestLogin();
			break;

		case STATUS_BIND_REQUEST:
			doRequestBind();
			break;
		default:
			finish();
		}

	}

	//
	private void doRequestShare() {
		// 如果没有登录，去登录
		if (!SessionManager.getInstance().isUserLogin(this)) {
			status = STATUS_LOGIN_REQUEST;
			Bundle bund = new Bundle();
			// bund.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
			// Settings.INDEX_ACTIVITY);
			ActivityUtil.jump(this, UserLoginActivity.class, 0, bund);

		} else {

			if (!isBind) {
				doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			} else if (isExpired) {
				doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			} else {
				finish();
			}

			// if
			// (!SessionManager.getInstance().getUserInfo(this).isSinaBindTag())
			// {// 如果未绑定，提示用户去绑定
			// doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			//
			// } else if
			// (SessionManager.getInstance().getUserInfo(this).isSinaWeiboExpired())
			// {// 如果绑定超时，提示去绑定
			// doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			// } else {
			// finish(); // 登录且绑定了直接返回前一页
			// }
			//
			// if
			// (!SessionManager.getInstance().getUserInfo(this).isQqBindTag())
			// {// 如果未绑定，提示用户去绑定
			// doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			//
			// } else if
			// (SessionManager.getInstance().getUserInfo(this).isQQWeiboExpired())
			// {// 如果绑定超时，提示去绑定
			// doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			// } else {
			// finish(); // 登录且绑定了直接返回前一页
			// }
		}
	}

	//
	private void doRequestLogin() {
		// 如果此时未登录，说明登录页没有成功，或者取消了登录，直接返回前一页
		if (!SessionManager.getInstance().isUserLogin(this)) {
			finish();
		} else {

			if (!isBind) {
				doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			} else if (isExpired) {
				doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			} else {
				finish();
			}

			// if
			// (!SessionManager.getInstance().getUserInfo(this).isSinaBindTag())
			// {// 如果未绑定，提示用户去绑定
			// doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			// } else if
			// (SessionManager.getInstance().getUserInfo(this).isSinaWeiboExpired())
			// {// 如果绑定超时，提示去绑定
			// doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			// } else {
			// finish(); // 登录且绑定了直接返回前一页
			// }
			// if
			// (!SessionManager.getInstance().getUserInfo(this).isQqBindTag())
			// {// 如果未绑定，提示用户去绑定
			// doBind("绑定到微博", "您还没有绑定到微博，是否现在去绑定？", "去绑定");
			//
			// } else if
			// (SessionManager.getInstance().getUserInfo(this).isQQWeiboExpired())
			// {// 如果绑定超时，提示去绑定
			// doBind("重新绑定", "您的绑定已过期，是否现在重新绑定？", "重新绑定");
			// } else {
			// finish(); // 登录且绑定了直接返回前一页
			// }
		}
	}

	//
	private void doRequestBind() {
		// 直接返回前一页
		finish();
	}

	private void doBind(String title, String message, String yesButtonText) {
		DialogUtil.showComfire(this, title, message, new String[] { yesButtonText, "不绑定" }, new Runnable() {
			@Override
			public void run() {// 去绑定
				status = STATUS_BIND_REQUEST;
				Bundle bund = new Bundle();
				bund.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);

				switch (page) {
				case 0:
					AuthWebActivity.currentWeiboUtil = WeiboUtilFactory
							.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
					ActivityUtil.jump(MediatorActivity.this, AuthWebActivity.class, 0, bund);
					break;
				case 1:
					SinaSSOAuthActivity.currentWeiboUtil = WeiboUtilFactory
							.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
					ActivityUtil.jump(MediatorActivity.this, SinaSSOAuthActivity.class, 0, bund);
					break;
				default:
					break;
				}

			}
		}, new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});
	}

}
