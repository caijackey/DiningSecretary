package com.xiaomishu.extension.baidu.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.PushCommonActivity;
import com.fg114.main.app.location.Loc;
import com.fg114.main.service.dto.PushMsgDTO;
import com.fg114.main.service.dto.PushMsgListDTO;
import com.fg114.main.service.dto.PushMsgPackDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.IOUtils;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends BroadcastReceiver {
	public static final String TAG = PushMessageReceiver.class.getSimpleName();
	AlertDialog.Builder builder;

	private static Handler mhandler;

	public static void setHandler(Handler handler) {
		mhandler = handler;
	}
	
	private static Context context;

	/**
	 * 推送来的消息的队列
	 */
	// public static LinkedBlockingQueue<PushObject> pushMQ = new
	// LinkedBlockingQueue<PushObject>();

	/**
	 * @param context
	 *            Context
	 * @param intent
	 *            接收的intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {
 
		
		try {
			this.context=context;
			PushObject po = new PushObject();
			boolean isSendMsg = false;
			if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
				// 获取消息内容
				po.messageType = PushConstants.ACTION_MESSAGE;
				po.messageMethod = null;
				po.message = intent.getExtras().getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
				isSendMsg = true;
				// pushMQ.put(po);

			} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
				// 处理绑定等方法的返回数据
				final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);
				// 方法返回错误码。若绑定返回错误（非0），则应用将不能正常接收消息。
				// 绑定失败的原因有多种，如网络原因，或access token过期。
				// 请不要在出错时进行简单的startWork调用，这有可能导致死循环。
				// 可以通过限制重试次数，或者在其他时机重新调用来解决。
				int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE, PushConstants.ERROR_SUCCESS);
				String content = "";
				if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
					// 返回内容
					content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
				}
				po.messageType = PushConstants.ACTION_RECEIVE;
				po.messageMethod = method;
				po.errorCode = errorCode;
				po.message = content;
				// pushMQ.put(po);
				isSendMsg = true;

			} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
				// 可选。通知用户点击事件处理
			}
			if (isSendMsg) {
				Message msg = new Message();
				msg.obj = po;
				// mhandler.sendMessage(msg);
				doPush(context, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doPush(Context context, Message msg) {
		try {

			PushObject po = (PushObject) msg.obj;
			if (po == null) {
				return;
			}
			// ----
			if (PushConstants.ACTION_MESSAGE.equals(po.messageType)) {
				doReceived(context, po);// 消息
			} else if (PushConstants.ACTION_RECEIVE.equals(po.messageType)) {
				doBinded(context, po);// 绑定
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doBinded(final Context context, PushObject po) {
		if (PushConstants.METHOD_BIND.equals(po.messageMethod)) {
			if (po.errorCode == 0) {
				String appid = "";
				String channelId = "";
				String userId = "";
				try {
					JSONObject jsonContent = new JSONObject(po.message);
					JSONObject params = jsonContent.getJSONObject("response_params");
					appid = params.getString("appid");
					channelId = params.getString("channel_id");
					userId = params.getString("user_id");
					// ---日志
					String log = "\n------百度推送doBinded成功---------\nappid=" + appid + "\nchannelid" + channelId + "\nuserid" + userId + "\n";
					Settings.requestLog.append(log);
					logBaiduPush(context, log);
					// -----------------------------------------------------------
					ServiceRequest request = new ServiceRequest(API.bindBaiduPush);
					request.addData("appid", appid);// 百度方给我们的应用id
					request.addData("userId", userId);// 唯一区别一个“客户端应用程序”的id，我们应该使用此值与我们系统用户绑定
					request.addData("channelId", channelId);// 唯一区别一个“手机设备”的id
					CommonTask.requestMutely(request, new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							String log = "\n------小秘书推送绑定 API.bindBaiduPush 成功---------\n";
							Settings.requestLog.append(log);
							logBaiduPush(context, log);
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							String log = "\n------小秘书推送绑定 API.bindBaiduPush 失败---------\n" + message + "\n";
							Settings.requestLog.append(log);
							logBaiduPush(context, log);
						}
					});
				} catch (JSONException e) {

					logBaiduPush(context, e);
				}
			}
		}
	}

	private void doReceived(Context context, PushObject po) {
		try {
			PushMsgListDTO dto = PushObject.getPushObject(po.message, PushMsgListDTO.class);
			if (dto != null && dto.getList() != null && dto.getList().size() > 0&&Settings.isOpenPush) {
				for (PushMsgPackDTO pushMsg : dto.getList()) {
					showNotification(context, pushMsg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			logBaiduPush(context, e);
		}
	}

	private void showNotification(Context context, PushMsgPackDTO pushMsg) {
		try {
			Settings.Is_Push_Notification_to_activity = false;

			PushMsgDTO msg = pushMsg.getMsg();
			Notification notification = new Notification();

			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			// | Notification.FLAG_ONGOING_EVENT;

			notification.tickerText = msg.getTitle();
			notification.icon = R.drawable.icon;
			notification.when = System.currentTimeMillis();
			Bundle bundle = new Bundle();
			bundle.putSerializable(Settings.BUNDLE_KEY_ID, msg);
			Intent intent = new Intent(context, PushCommonActivity.class);
			intent.putExtras(bundle);
			PendingIntent pi = PendingIntent.getActivity(context, (int) SystemClock.elapsedRealtime() % 99999, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context, "", msg.getTitle(), pi);
			NotificationManager mNotifMan = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotifMan.notify((int) SystemClock.elapsedRealtime() % 9999999, notification);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void logBaiduPush(Context context, String info) {
		try {

			StringBuffer sb1 = new StringBuffer();
			sb1.append(info).append(" -- ");
			sb1.append(Loc.getFormatDateStr(System.currentTimeMillis())).append(" \r\n ");
			IOUtils.writeTestInfo(context, "log_keep_service.txt", sb1.toString());

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	static void logBaiduPush(Context context, Exception e) {
		try {
			logBaiduPush(context, e.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void clearNotification() {
		try{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		}catch (Exception e) {
			// TODO: handle exception
		}

	}
}
