package com.fg114.main.app;

import java.io.*;
import java.util.*;

import org.apache.http.impl.client.*;

import android.app.*;
import android.content.*;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.frontia.FrontiaApplication;
import com.baidu.mapapi.*;
import com.baidu.mapapi.map.MKEvent;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.*;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.*;
import com.fg114.main.wxapi.WeixinUtils;
import com.google.xiaomishujson.Gson;

/**
 * Application
 * 
 * @author zhangyifan
 */
public class Fg114Application extends Application {

	public static long start = System.currentTimeMillis();
	private static final String TAG = "Fg114Application";
	private static final boolean DEBUG = Settings.DEBUG;

	private static final long UPDATE_CITY_INTERVAL = 1000 * 60 * 60 * 24;
	private static final long UPDATE_GET_MAIN_INFO = 1000 * 60 * 30;

	public static CrashHandler crashHandler;

	// 获取位置管理服务
	public static DefaultHttpClient mHttpClient;

	// 拨号时间
	public static long callTime;
	// 当前使用的小秘书号码
	public static String super57PhoneNumber;

	// 图文界面状态
	public static boolean isActive = false;
	// 监听超级小秘书端线程状态
	public static boolean isServerListenerThreadStop = true;

	// 超级小秘书房间信息
	public static String roomId;
	public static String clientId;
	public static String clientName;
	public static String roomKey;

	// 是否需要重新查询订单数量
	public static boolean isNeedUpdate = false;
	// public static Timer mMainPagePackDTOTimer;
	// private TimerTask mMainPageInfoPackDTOTask;

	private MainPageInfo3DTO dto;
	// 百度MapAPI的管理类
	public BMapManager mBMapMan = null;
	// 授权Key
	String mStrKey = "B88FA5B7055C3B7D8C6C074AA4E79FB31EBCA790";
	public boolean m_bKeyRight = true; // 授权Key正确，验证通过

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			// DialogUtil.showToast(Fg114Application.this, "您的网络出错啦！");
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				// DialogUtil.showToast(Fg114Application.this, "请输入正确的授权Key！");
				m_bKeyRight = false;
			}
		}

	}

	// --
	private static Fg114Application instance = null;

	public static Fg114Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {

		super.onCreate();
		instance = this;
		DialogUtil.setContext(getApplicationContext());
		// 推送初始化
		FrontiaApplication.initFrontiaApplication(getApplicationContext());
		// 初始化崩溃捕获处理
		crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		// 初始化上下文工具
		ContextUtil.init(this);
		HanziUtil.init(this);

		// 初始化全局变量
		Settings.ASS_PATH = getPackageResourcePath();
		Settings.DEV_ID = ActivityUtil.getDeviceId(this);
		Settings.VERSION_NAME = ActivityUtil.getVersionName(this);
		// 获取渠道号
		ActivityUtil.getChannelId(ContextUtil.getContext());

		// 建立http client
		mHttpClient = AbstractHttpApi.createHttpClient();

		// initBaidu();

		// // 后台服务启动
		// new Handler() {
		//
		// @Override
		// public void handleMessage(Message msg) {
		// KeepAliveService.actionStart(Fg114Application.this);
		// }
		// }.sendEmptyMessageDelayed(0, 5000);
		// // 语音订餐消息服务启动
		// new Handler() {
		//
		// @Override
		// public void handleMessage(Message msg) {
		// ChkHaveChatMsgService.actionStart(Fg114Application.this);
		// }
		// }.sendEmptyMessageDelayed(0, 7000);

		// // 初始化Timer
		// initTimer();

		// 调用一次初始化城市
		SessionManager.getInstance().getCityListDTO(Fg114Application.this);
		{
			// 如果没有城市，先将城市设为默认
			CityInfo cityInfo = SessionManager.getInstance().getCityInfo(this);
			if (cityInfo == null || CheckUtil.isEmpty(cityInfo.getId())) {
				cityInfo.setId(Settings.DEFAULT_CITY_ID);
				cityInfo.setName(Settings.DEFAULT_CITY_NAME);
				cityInfo.setPhone(Settings.DEFAULT_CITY_PHONE_SH);
				SessionManager.getInstance().setCityInfo(this, cityInfo);
			}
		}

		// // 调用一次初始化本地上海区域数据
		// SessionManager.getInstance().getShRegionListDTO();

		// 获取首页信息（广告、订单气泡等）
		// mMainPageInfoPackDTOTask = new TimerTask() {
		//
		// @Override
		// public void run() {
		// if(ActivityUtil.isOnForeground(getApplicationContext()))
		// {
		// Loc.sendLocControlMessage(true);
		// if ( (isNeedUpdate || isTimeToGetMainPage())) {
		//
		// mainPageHandler.sendEmptyMessage(0);
		// isNeedUpdate = false;
		// }
		// }
		// else
		// {
		// Loc.sendLocControlMessage(false);
		// // 触发上传点击流---------------
		// if(OpenPageDataTracer.upTag==false){
		// OpenPageDataTracer.upTag=true;
		// OpenPageDataTracer.getInstance().uploadImmediately();
		// }
		// }
		//
		// }
		// };

		// mMainPagePackDTOTimer.schedule(mMainPageInfoPackDTOTask, 2000, 1000);
		// -------------------------------------------------------------------

		Settings.gBaiduAvailable = true;
		// try {
		// // 初始化百度地图
		// initBaidu();
		// } catch (Exception e) {
		// // try {
		// // // 初始化百度地图
		// // initBaidu();
		// // } catch (Exception e2) {
		// ActivityUtil.saveException(e, "init baidu api fail");
		// Settings.gBaiduAvailable = false;
		// // }
		// }

		if (Settings.gBaiduAvailable) {
			// 初始化百度地图定位
			try {
				LocBaidu.init(this);
			} catch (Exception e) {
				try {
					LocBaidu.init(this);
				} catch (Exception e2) {
					Settings.gBaiduAvailable = false;
				}
			}
		}

		// 位置服务
		Loc.ini(this);

		// 微信
		WeixinUtils.initWeixin(this);
		WeixinUtils.regWeixin();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(20000);
					executeReportErrorTask();// 提交错误提交
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		LocBaidu.stop();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		// 关闭http client
		AbstractHttpApi.shutdownHttpClient(mHttpClient);
		// stopTimer();
	}

	public static boolean isTimeToGetMainPage() {
		return (SystemClock.elapsedRealtime() - mainPageTimestamp) > nextQueryInterval * 1000;
	}

	// private void initTimer() {
	// if (mMainPagePackDTOTimer == null) {
	// mMainPagePackDTOTimer = new Timer();
	// }
	// }
	//
	// private void stopTimer() {
	// if (mMainPagePackDTOTimer != null) {
	// mMainPagePackDTOTimer.cancel();
	// mMainPagePackDTOTimer = null;
	// }
	// }

	// Handler mainPageHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// MainFrameActivity.needCheckAndJumpToOrderInfoPage = false;
	// excuteMainPageInfoPackDTOTask();
	// }
	//
	// };

	// 从服务器获取订单数量信息
	private static long mainPageTimestamp = SystemClock.elapsedRealtime();
	private static int nextQueryInterval = 1000000;

	public static void excuteMainPageInfoPackDTOTask() {
		MainFrameActivity.needCheckAndJumpToOrderInfoPage = false;
		boolean isFirst = SharedprefUtil.getBoolean(instance, Settings.IS_FRIST, true);

		final MainPageInfoPack4DTO data = SessionManager.getInstance().getMainPageInfoPackDTO();
		//
		ServiceRequest request = new ServiceRequest(API.getMainPageInfoPack4);
		if (data.mainPageMsgListDTO != null) {
			request.addData("advTimestamp", data.mainPageMsgListDTO.timestamp);
		} else {
			request.addData("advTimestamp", 0);
		}
		request.addData("pageSize", 14);
		request.addData("firstQueryTag", isFirst);

		CommonTask.requestMutely(request, new CommonTask.TaskListener<MainPageInfoPack4DTO>() {

			@Override
			protected void onSuccess(MainPageInfoPack4DTO dto) {

				mainPageTimestamp = SystemClock.elapsedRealtime();
				nextQueryInterval = dto.nextQueryInterval <= 300 ? 5 * 60 : dto.nextQueryInterval;
				SessionManager.getInstance().setMainPageInfoPackDTO(dto);
				excuteOrderHintInfoTask();
			}

			@Override
			protected void onError(int code, String message) {
				// doTest();
			}

			void doTest() {
				String json = "{\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\"false\"},\"list\":[{\"uuid\":\"1233\",\"name\":\"天府大萝卜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521264\",\"latitude\":\"31.239977\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜2222\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜道德\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜似懂非懂是\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"}],\"typeList\":[{\"uuid\":\"\",\"name\":\"全部\"},{\"uuid\":\"12\",\"name\":\"好吃\"},{\"uuid\":\"23\",\"name\":\"很好\"},{\"uuid\":\"34\",\"name\":\"凉菜好吃\"},{\"uuid\":\"45\",\"name\":\"热茶\"},{\"uuid\":\"56\",\"name\":\"酒水\"},{\"uuid\":\"57\",\"name\":\"不错的\"}]}";
				// TakeoutRestListDTO dto=JsonUtils.fromJson(json,
				// TakeoutRestListDTO.class);
				// onSuccess(dto);
			}
		});

	}

	public static void excuteOrderHintInfoTask() {
		MainPageOtherInfoPackDTO data = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
		ServiceRequest request = new ServiceRequest(API.getMainPageOtherInfoPack);
		if (data.orderHintPackData != null) {
			request.addData("orderHintTimestamp", data.orderHintPackData.timestamp);
		} else {
			request.addData("orderHintTimestamp", 0);
		}
		CommonTask.requestMutely(request, new CommonTask.TaskListener<MainPageOtherInfoPackDTO>() {

			@Override
			protected void onSuccess(MainPageOtherInfoPackDTO dto) {
				SessionManager.getInstance().setMainPageOtherInfoPackDTO(dto);

				// 通知广告、订单数量，消息数量等
				MainFrameActivity.needCheckAndJumpToOrderInfoPage = true;
				CommonObservable.getInstance().notifyObservers(CommonObserver.SystemMessageObserver.class);
			}

			@Override
			protected void onError(int code, String message) {
				// doTest();
			}

			void doTest() {
				String json = "{\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\"false\"},\"list\":[{\"uuid\":\"1233\",\"name\":\"天府大萝卜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521264\",\"latitude\":\"31.239977\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜2222\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜道德\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜似懂非懂是\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"}],\"typeList\":[{\"uuid\":\"\",\"name\":\"全部\"},{\"uuid\":\"12\",\"name\":\"好吃\"},{\"uuid\":\"23\",\"name\":\"很好\"},{\"uuid\":\"34\",\"name\":\"凉菜好吃\"},{\"uuid\":\"45\",\"name\":\"热茶\"},{\"uuid\":\"56\",\"name\":\"酒水\"},{\"uuid\":\"57\",\"name\":\"不错的\"}]}";
				// TakeoutRestListDTO dto=JsonUtils.fromJson(json,
				// TakeoutRestListDTO.class);
				// onSuccess(dto);
			}
		});
	}

	/**
	 * 初始化百度地图api
	 */
	public void initBaidu() {
		// 初始化百度地图管理器
		if (mBMapMan == null) {
			mBMapMan = new BMapManager(getApplicationContext());
			mBMapMan.init(this.mStrKey, new MyGeneralListener());
		}

	}

	/**
	 * 发送错误报告
	 */
	private void executeReportErrorTask() {
		try {
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
			if (!isNetAvailable) {
				return;
			}
			// 发送崩溃日志
			if (Fg114Application.crashHandler != null) {
				// Log.i("executeReportErrorTask", "上传错误报告");
				Fg114Application.crashHandler.sendPreviousReportsToServer();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
