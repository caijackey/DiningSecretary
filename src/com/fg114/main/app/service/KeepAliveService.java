//package com.fg114.main.app.service;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.Calendar;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.baidu.android.pushservice.PushConstants;
//import com.baidu.android.pushservice.PushManager;
//import com.baidu.frontia.FrontiaApplication;
//import com.fg114.main.R;
//import com.fg114.main.analytics.OpenPageDataTracer;
//import com.fg114.main.app.Fg114Application;
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.PushCommonActivity;
//import com.fg114.main.app.data.CityInfo;
//import com.fg114.main.app.location.Loc;
//import com.fg114.main.service.dto.PushMsgDTO;
//import com.fg114.main.service.dto.PushMsgDTO;
//import com.fg114.main.service.dto.PushMsgListDTO;
//import com.fg114.main.service.dto.PushMsgListDTO;
//import com.fg114.main.service.dto.PushMsgPackDTO;
//import com.fg114.main.service.dto.PushMsgPackDTO;
//import com.fg114.main.service.http.ServiceRequest;
//import com.fg114.main.service.http.ServiceRequest.API;
//import com.fg114.main.service.task.CommonTask;
//import com.fg114.main.service.task.PushMessageTask;
//import com.fg114.main.util.ActivityUtil;
//import com.fg114.main.util.CalendarUtil;
//import com.fg114.main.util.CheckUtil;
//import com.fg114.main.util.CommonObservable;
//import com.fg114.main.util.ContextUtil;
//import com.fg114.main.util.CrashHandler;
//import com.fg114.main.util.IOUtils;
//import com.fg114.main.util.SessionManager;
//import com.fg114.main.util.SharedprefUtil;
//import com.xiaomishu.extension.baidu.push.PushMessageReceiver;
//import com.xiaomishu.extension.baidu.push.PushObject;
//
//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.SystemClock;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.widget.SlidingDrawer;
//
//public class KeepAliveService extends Service {
//	// public static final String TAG = "KeepAliveService";
//
//	private static final String HOST = "jasta.dyndns.org";
//	private static final int PORT = 50000;
//
//	private static final String ACTION_START = "START";
//	private static final String ACTION_STOP = "STOP";
//
//
////	private ConnectivityManager mConnMan;
//	private NotificationManager mNotifMan;
//
//
//
//	private boolean mStarted = false;
//
//
//
//	public static StringBuffer bindBaiduPushLog = new StringBuffer(1024 * 100);
//	private static Context myApp;
//
//	// private static int i = 0;
//
//	public static void actionStart(Context ctx) {
//		try {
//			Intent i = new Intent(ctx, KeepAliveService.class);
//			i.setAction(ACTION_START);
//			myApp = ctx;
//			ctx.startService(i);
//		} catch (Exception e) {
//
//		}
//	}
//
//	@Override
//	public void onCreate() {
//		try {
//			
//			
//			// ----------------------------
//			OpenPageDataTracer.getInstance().uploadFileData();
//			// ----------------------------
//			// Log.e("服务：onCreate","onCreate");
//			super.onCreate();
////			mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//			mNotifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//			/*
//			 * If our process was reaped by the system for any reason we need to
//			 * restore our state with merely a call to onCreate. We record the
//			 * last "started" value and restore it here if necessary.
//			 */
//			// handleCrashedService();
//			logBaiduPush("keepalive onCreate");
//			if(myApp==null){
//			logBaiduPush("myApp==null");	
//			}
//			
//		} catch (Exception e) {
//			logBaiduPush(e);
//		}
//
//	}
//
//	@Override
//	public void onDestroy() {
//		try {
//			
//			logBaiduPush("keepalive onDestroy");
//			// Log.e("服务：onDestroy()","onDestroy()");
//			mStarted = false;
//			super.onDestroy();
//		} catch (Exception e) {
//
//		}
//	}
//
//	@Override
//	public void onStart(Intent intent, int startId) {
//		try {
//			// Log.e("服务：onStart()",""+intent.getAction());
//			super.onStart(intent, startId);
//			if (intent.getAction().equals(ACTION_STOP) == true) {
//
//				stopSelf();
//			} else if (intent.getAction().equals(ACTION_START) == true) {
//				start();
//			}
//
//		} catch (Exception e) {
//
//		}
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		// Log.e("服务：onBind()","onBind");
//		return null;
//	}
//
//	private synchronized void start() {
//		try {
//			if (mStarted == true) {
//				return;
//			}
//		
//
//			logBaiduPush("keepalive start");
//			bindBaiduPush(handlePush);
//
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					try {
//						Thread.sleep(10000);
//						executeReportErrorTask();// 提交错误提交
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//				}
//			}).start();
//
//			mStarted = true;
//			logBaiduPush("keepalive start finish");
//
//		} catch (Exception e) {
//			logBaiduPush(e);
//	
//		}
//	}
//
//	public static void bindBaiduPush(Handler handlePush) {
//		try {
//			PushMessageReceiver.setHandler(handlePush);
//			String appkey = "fHNMZ9h8ENulzfoIxVoXjdG3";
//			// 以apikey的方式登录，一般放在主Activity的onCreate中
//	        PushManager.startWork(ContextUtil.getContext(), PushConstants.LOGIN_TYPE_API_KEY, appkey);
//
//
//			 logBaiduPush("起动绑定成功KeepAliveService.bindBaiduPush");
//			
//		} catch (Exception e) {
//			e.printStackTrace();			
//			 logBaiduPush(e);
//		}
//	}
//
//	// 处理消息的Handler
//	public Handler handlePush = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//
//			try {
//
//				PushObject po = (PushObject) msg.obj;
//				if (po == null) {
//					return;
//				}
//				// ----
//				if (PushConstants.ACTION_MESSAGE.equals(po.messageType)) {
//					doReceived(po);// 消息
//				} else if (PushConstants.ACTION_RECEIVE.equals(po.messageType)) {
//					doBinded(po);// 绑定
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		}
//
//		private void doBinded(PushObject po) {
//			if (PushConstants.METHOD_BIND.equals(po.messageMethod)) {
//				if (po.errorCode == 0) {
//					String appid = "";
//					String channelId = "";
//					String userId = "";
//					try {
//						JSONObject jsonContent = new JSONObject(po.message);
//						JSONObject params = jsonContent.getJSONObject("response_params");
//						appid = params.getString("appid");
//						channelId = params.getString("channel_id");
//						userId = params.getString("user_id");
//						// ---日志
//						String log = "\n------百度推送doBinded成功---------\nappid=" + appid + "\nchannelid" + channelId + "\nuserid" + userId + "\n";
//						Settings.requestLog.append(log);
//						logBaiduPush(log);
//						// -----------------------------------------------------------
//						ServiceRequest request = new ServiceRequest(API.bindBaiduPush);
//						request.addData("appid", appid);// 百度方给我们的应用id
//						request.addData("userId", userId);// 唯一区别一个“客户端应用程序”的id，我们应该使用此值与我们系统用户绑定
//						request.addData("channelId", channelId);// 唯一区别一个“手机设备”的id
//						CommonTask.requestMutely(request, new CommonTask.TaskListener<Void>() {
//
//							@Override
//							protected void onSuccess(Void dto) {
//								String log = "\n------小秘书推送绑定 API.bindBaiduPush 成功---------\n";
//								Settings.requestLog.append(log);
//								logBaiduPush(log);
//							}
//
//							@Override
//							protected void onError(int code, String message) {
//								super.onError(code, message);
//								String log = "\n------小秘书推送绑定 API.bindBaiduPush 失败---------\n" + message + "\n";
//								Settings.requestLog.append(log);
//								logBaiduPush(log);
//							}
//						});
//					} catch (JSONException e) {
//					
//						logBaiduPush(e);
//					}
//				}
//			}
//		}
//
//		private void doReceived(PushObject po) {
//			try {
//				PushMsgListDTO dto = PushObject.getPushObject(po.message, PushMsgListDTO.class);
//				if (dto != null && dto.getList() != null && dto.getList().size() > 0) {
//					for (PushMsgPackDTO pushMsg : dto.getList()) {
//						showNotification(pushMsg);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				
//				logBaiduPush(e);
//			}
//		}
//
//	};
//
//	private void showNotification(PushMsgPackDTO pushMsg) {
//		try {
//			PushMsgDTO msg = pushMsg.getMsg();
//			Notification notification = new Notification();
//
//			notification.defaults = Notification.DEFAULT_ALL;
//			notification.flags = Notification.FLAG_AUTO_CANCEL;
//			// | Notification.FLAG_ONGOING_EVENT;
//
//			notification.tickerText = msg.getTitle();
//			notification.icon = R.drawable.icon;
//			notification.when = System.currentTimeMillis();
//			Bundle bundle = new Bundle();
//			bundle.putSerializable(Settings.BUNDLE_KEY_ID, msg);
//			Intent intent = new Intent(this, PushCommonActivity.class);
//			intent.putExtras(bundle);
//			PendingIntent pi = PendingIntent.getActivity(this, (int) SystemClock.elapsedRealtime() % 99999, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//			notification.setLatestEventInfo(this, "", msg.getTitle(), pi);
//
//			mNotifMan.notify((int) SystemClock.elapsedRealtime() % 9999999, notification);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 发送错误报告
//	 */
//	private void executeReportErrorTask() {
//		try {
//			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
//			if (!isNetAvailable) {
//				return;
//			}
//			// 发送崩溃日志
//			if (Fg114Application.crashHandler != null) {
//				Log.i("executeReportErrorTask", "上传错误报告");
//				Fg114Application.crashHandler.sendPreviousReportsToServer();
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
//
//	static void logBaiduPush(String info) {
//		try {
//			
//			StringBuffer sb1 = new StringBuffer();
//			sb1.append(info).append(" -- ");
//			sb1.append(Loc.getFormatDateStr(System.currentTimeMillis())).append(" \r\n ");
//			IOUtils.writeTestInfo(myApp, "log_keep_service.txt", sb1.toString());
//			
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
//	}
//
//	static void logBaiduPush(Exception e) {
//		try {
//		    logBaiduPush(e.getMessage());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
//
//	// private synchronized void keepAlive() {
//	// executePushMessageTask();
//	// new Thread(consumer, "PushMessage consumer").start();
//
//	// Thread b = new Thread(new Runnable() {
//	// public void run() {
//	// while (true) {
//	// try {
//	// PushObject po = PushMessageReceiver.pushMQ.take();
//	//
//	// if (po != null) {
//	// Message msg = new Message();
//	// msg.obj = po;
//	// handlePush.sendMessage(msg);
//	// }
//	// } catch (Exception e) {
//	// e.printStackTrace();
//	// }
//	// }
//	//
//	// }
//	// });
//	// b.start();
//	//
//	//
//	// }
//
//	// // 读取消息并处理
//	// Runnable consumer = new Runnable() {
//	//
//	// @Override
//	// public void run() {
//	// while (true) {
//	// try {
//	// PushObject po = PushMessageReceiver.pushMQ.take();
//	// if (po != null) {
//	// Message msg = new Message();
//	// msg.obj = po;
//	// handlePush.sendMessage(msg);
//	// }
//	// } catch (Exception e) {
//	// e.printStackTrace();
//	// }
//	// }
//	// }
//	// };
//
//	// -----------------
//	// private void startKeepAlives(long time) {
//	// try {
//	// time = time > 0 ? time : KEEP_ALIVE_INTERVAL;// 如果下次循环时间为0，则用默认时间
//	// Intent i = new Intent();
//	// i.setClass(this, KeepAliveService.class);
//	// i.setAction(ACTION_KEEPALIVE);
//	// PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
//	// AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
//	// alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time,
//	// pi);
//	// } catch (Exception e) {
//	// e.printStackTrace();
//	// }
//	// }
//
//	// private void postTask(long time) {
//	// try {
//	// Log.v("TAG", "错误报告");
//	// time = time > 0 ? time : KEEP_ALIVE_INTERVAL;// 如果下次循环时间为0，则用默认时间
//	// Intent i = new Intent();
//	// i.setClass(this, KeepAliveService.class);
//	// i.setAction(ACTION_RECONNECT);
//	// PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
//	// AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
//	// alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time,
//	// pi);
//	// } catch (Exception e) {
//	// e.printStackTrace();
//	// }
//	// }
//
//	// private void stopKeepAlives() {
//	// try {
//	// Intent i = new Intent();
//	// i.setClass(this, KeepAliveService.class);
//	// i.setAction(ACTION_KEEPALIVE);
//	// PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
//	// AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
//	// alarmMgr.cancel(pi);
//	// } catch (Exception e) {
//	//
//	// }
//	// }
//
//	// /**
//	// * 消息推送
//	// */
//	// private void executePushMessageTask() {
//	// try {
//	// boolean isNetAvailable =
//	// ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
//	// if (isNetAvailable) {
//	//
//	// // 发送崩溃日志
//	// if (Fg114Application.crashHandler != null) {
//	// Fg114Application.crashHandler.sendPreviousReportsToServer();
//	// }
//	//
//	// boolean isNeedToGetPushMessage = true;
//	// Calendar cNow = Calendar.getInstance();
//	// int hour = cNow.get(Calendar.HOUR_OF_DAY);
//	// if (hour >= 23 || hour < 8) {
//	// isNeedToGetPushMessage = false;
//	// }
//	// if (!isNeedToGetPushMessage) {
//	// startKeepAlives(MAXIMUM_RETRY_INTERVAL);
//	// //Log.e("KeepAliveService", "isNeedToGetPushMessage is false");
//	// return;
//	// }
//	//
//	// String token = "";
//	// String cityId = "";
//	// CityInfo city =
//	// SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
//	// if (city != null && !CheckUtil.isEmpty(city.getId())) {
//	// cityId = city.getId();
//	// }
//	// if (SessionManager.getInstance().isUserLogin(this)) {
//	// token = SessionManager.getInstance().getUserInfo(this).getToken();
//	// }
//	// mPushMessageTask = new PushMessageTask(this, cityId, token);
//	// mPushMessageTask.setShowError(false);
//	// mPushMessageTask.execute(new Runnable() {
//	// @Override
//	// public void run() {
//	// try {
//	//
//	// mPushMsgListDTO = mPushMessageTask.dto;
//	// if (mPushMsgListDTO.getList().size() > 0) {
//	// for (PushMsgPackDTO pushMsg : mPushMsgListDTO.getList()) {
//	// showNotification(pushMsg);
//	// }
//	// }
//	//
//	// startKeepAlives(mPushMsgListDTO.getNextVisitSeconds() * 1000);
//	// } catch (Exception e) {
//	// startKeepAlives(MAXIMUM_RETRY_INTERVAL);
//	// }
//
//}
