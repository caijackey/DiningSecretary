package com.fg114.main.wxapi;

import android.app.Activity;
import android.content.*;
import android.text.*;
import android.util.Log;

import com.fg114.main.alipay.AliPayUtils.AliPayListener;
import com.fg114.main.app.Settings;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.FileObject;
import com.fg114.main.service.dto.WeiXinPayData;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.*;

/**
 * 微信工具类
 * 
 * @author wufucheng
 */
public class WeixinUtils {
	private static boolean DEBUG = Settings.DEBUG;
	private static String APP_ID = "wxc7622bad94e7ab25";
	// 在微信注册的appId（正式版)

	// private static final String APP_ID = "wx8d86616173916ba4";
	// //appkey:94fe6ddc82b3a0acfcee73ef976a754b
	private static final String APP_ID_TEST = "wx8d86616173916ba4"; // appkey:94fe6ddc82b3a0acfcee73ef976a754b
	// //在微信注册的appId(测试版) 如果是测试版，会在initWeixin里面把APP_ID改成APP_ID_test

	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001; // 微信支持分享到朋友圈的最低版本

	private static IWXAPI api; // IWXAPI是第三方app和微信通信的openapi接口

	/**
	 * 初始化微信api
	 * 
	 * @param context
	 */
	public static void initWeixin(Context context) {
		try {
			// 如果是测试版使用测试版app_id
//			if (DEBUG == true)
//				APP_ID = APP_ID_TEST;
			// 通过WXAPIFactory工厂，获取IWXAPI的实例
			api = WXAPIFactory.createWXAPI(context, APP_ID);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查用户手机端是否可使用微信分享
	 * 
	 * @return
	 */
	public static boolean isWeixinAvailable() {
		try {
			if (api == null) {
				return false;
			}
			return api.isWXAppInstalled();// && api.isWXAppSupportAPI();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 检查用户手机端是否可使用微信支付
	 * 
	 */
	public static boolean isWeixinPay() {
		try {
			if (api == null) {
				return false;
			}
			boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
			return isPaySupported;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * 微信支付
	 * @param ativity
	 * @param weiXinInfo
	 */
	public static void doPay(Activity ativity, String weiXinInfo) {
		if (isWeixinPay()) {
			PayReq req = new PayReq();
			WeiXinPayData weiXinPayData = JsonUtils.fromJson(weiXinInfo, WeiXinPayData.class);
			req.appId = APP_ID;
			req.partnerId = weiXinPayData.partnerId;
			req.prepayId = weiXinPayData.prepayId;
			req.nonceStr = weiXinPayData.nonceStr;
			req.timeStamp = weiXinPayData.timeStamp;
			req.packageValue ="Sign=WXPay";
			req.sign = weiXinPayData.sign;
			api.sendReq(req);
		} else {
			DialogUtil.showAlert(ativity, "提示", "您没有安装微信或者微信版本过低");
			return;
		}
	}

	/**
	 * 注册微信app，注册成功后该应用将显示在微信的app列表中
	 * 
	 * @return
	 */
	public static boolean regWeixin() {
		try {
			if (api == null || !isWeixinAvailable()) {
				return false;
			}
			return api.registerApp(APP_ID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 反注册微信app，成功后将不再显示在微信的app列表中
	 */
	public static void unregWeixin() {
		try {
			if (api == null || !isWeixinAvailable()) {
				return;
			}
			api.unregisterApp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户手机的微信版本是否支持发送到朋友圈
	 * 
	 * @return
	 */
	public static boolean isTimelineSupported() {
		try {
			if (api == null || !isWeixinAvailable()) {
				return false;
			}
			int wxSdkVersion = api.getWXAppSupportAPI();
			return wxSdkVersion >= TIMELINE_SUPPORTED_VERSION;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 打开用户手机端的微信程序
	 * 
	 * @return
	 */
	public static boolean openWeixin() {
		try {
			if (api == null || !isWeixinAvailable()) {
				return false;
			}
			return api.openWXApp();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 处理微信回调
	 * 
	 * @param intent
	 * @param handler
	 * @return
	 */
	public static boolean handleIntent(Intent intent, IWXAPIEventHandler handler) {
		try {
			if (api == null || !isWeixinAvailable()) {
				return false;
			}
			return api.handleIntent(intent, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 处理支付微信回调
	 * 
	 * @param intent
	 * @param handler
	 * @return
	 */
	public static boolean handlePayIntent(Intent intent, IWXAPIEventHandler handler) {
		try {
			if (api == null || !isWeixinPay()) {
				return false;
			}
			return api.handleIntent(intent, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 分享文字内容到微信
	 * 
	 * @param text
	 * @return
	 */
	public static boolean sendText(String text) {
		try {
			if (api == null || !isWeixinAvailable() || TextUtils.isEmpty(text)) {
				return false;
			}
			// 初始化一个WXTextObject对象
			WXTextObject textObj = new WXTextObject();
			textObj.text = text;
			// 用WXTextObject对象初始化一个WXMediaMessage对象
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = textObj;
			// 发送文本类型的消息时，title字段不起作用
			// msg.title = "Will be ignored";
			msg.description = text;
			// 构造一个Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneSession;
			// 调用api接口发送数据到微信

			return api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 分享内容到微信
	 * 
	 * @return
	 */
	public static boolean sendTextAndPicture(String title, String description, String imageUrl, String webUrl) {
		try {
			if (api == null || !isWeixinAvailable() || TextUtils.isEmpty(description)) {
				return false;

			}
			// 初始化一个WXTextObject对象
			// ----
			byte[] b = new byte[] {};
			FileObject f = FileCacheUtil.getInstance().get("MyImageView", imageUrl);
			if (f != null) {
				b = f.getContent();
			}
			// ----
			WXWebpageObject obj = new WXWebpageObject();
			obj.webpageUrl = webUrl;
			// ----
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = obj;
			// 发送文本类型的消息时，title字段不起作用
			// msg.title = "Will be ignored";
			msg.title = title;
			msg.description = description;
			msg.thumbData = b;
			// 构造一个Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("TextAndPicture"); // transaction字段用于唯一标识一个请求
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneSession;
			// 调用api接口发送数据到微信
			return api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 分享内容到微信
	 */
	public static boolean sendFriendTextAndPicture(String title, String description, String imageUrl, String webUrl) {
		try {
			if (api == null || !isWeixinAvailable() || TextUtils.isEmpty(description)) {
				return false;
			}
			// 初始化一个WXTextObject对象
			// ----
			byte[] b = new byte[] {};
			FileObject f = FileCacheUtil.getInstance().get("MyImageView", imageUrl);
			if (f != null) {

				b = f.getContent();
			}
			// ----
			WXWebpageObject obj = new WXWebpageObject();
			obj.webpageUrl = webUrl;
			// ----
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = obj;
			// 发送文本类型的消息时，title字段不起作用
			// msg.title = "Will be ignored";
			msg.title = title;
			msg.description = description;
			msg.thumbData = b;
			// 构造一个Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("friendTextAndPicture"); // transaction字段用于唯一标识一个请求
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
			// 调用api接口发送数据到微信
			return api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 发送到微信朋友圈
	 * 
	 * @param text
	 * @return
	 */
	public static boolean sendFriendText(String text) {

		try {
			if (api == null || !isWeixinAvailable() || TextUtils.isEmpty(text)) {
				return false;
			}
			// 初始化一个WXTextObject对象
			WXTextObject textObj = new WXTextObject();
			textObj.text = text;
			// 用WXTextObject对象初始化一个WXMediaMessage对象
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = textObj;
			// 发送文本类型的消息时，title字段不起作用
			// msg.title = "Will be ignored";
			msg.description = text;
			// 构造一个Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
			// 调用api接口发送数据到微信
			return api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * 分享网页到微信
	 * 
	 * @param url
	 * @param title
	 * @param desc
	 * @return
	 */
	public static boolean sendWebpage(String url, String title, String desc) {
		try {
			if (api == null || !isWeixinAvailable() || TextUtils.isEmpty(url)) {
				return false;
			}
			WXWebpageObject webpage = new WXWebpageObject();
			webpage.webpageUrl = url;

			WXMediaMessage msg = new WXMediaMessage(webpage);
			msg.title = title;
			msg.description = desc;

			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("webpage");
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneSession;
			return api.sendReq(req);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

}
