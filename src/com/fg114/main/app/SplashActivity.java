package com.fg114.main.app;

import java.util.concurrent.atomic.AtomicLong;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.fg114.main.R;
import com.fg114.main.app.activity.*;
import com.fg114.main.app.activity.Mdb.MdbResListActivity;
import com.fg114.main.app.activity.order.MdbOrderDetailActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayBuyPaymentActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayMyOrderActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.task.SyncUserInfoTask;

import static com.fg114.main.app.Fg114Application.*;

/**
 * 闪屏
 * 
 * @author wufucheng
 * 
 */
public class SplashActivity extends Activity {

	// private static final String TAG = SplashActivity.class.getSimpleName();

	private static final int mSleepTime = 800; // 闪屏停留的时间
	private CheckVersionTask mCheckVersionTask;
	private VersionChkDTO mVersionChkDTO;
	private Thread mThread;
	private MyImageView image;
	private Animation fade_in;
	//private AtomicLong time=new AtomicLong(SystemClock.elapsedRealtime());

	/**
	 * 跳转至首页线程
	 */
	private Runnable mToIndexRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				// 固定时间后跳转至首页
				Thread.sleep(mSleepTime);
				splashHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				splashHandler.sendEmptyMessage(0);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//time.set(SystemClock.elapsedRealtime());
		// fade_in=AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.fade_in);
		processAlipayData(savedInstanceState);
		setContentView(R.layout.splash);
		image = (MyImageView) findViewById(R.id.splash_image);
		
		//新版屏蔽闪屏广告功能
		image.setVisibility(View.GONE);
		executeSoftwareCommonDataTask();
		new Thread(mToIndexRunnable).start();
		
		//绑定百度推送
		String appkey = "fHNMZ9h8ENulzfoIxVoXjdG3";
		// 以apikey的方式登录，一般放在主Activity的onCreate中
        PushManager.startWork(ContextUtil.getContext(), PushConstants.LOGIN_TYPE_API_KEY, appkey);
      
	}
	//处理支付宝钱包跳转过来的登录逻辑
	private void processAlipayData(Bundle savedInstanceState) {
		try {
			Intent intent = getIntent();
			String alipayUserId = intent.getStringExtra("alipay_user_id"); 
			String authCode = intent.getStringExtra("auth_code");
			String appId = intent.getStringExtra("app_id");
			String version = intent.getStringExtra("version");
			String alipayClientVersion = intent.getStringExtra("alipay_client_version");
			if(!CheckUtil.isEmpty(authCode)){
				//用支付宝登录
				ServiceRequest request = new ServiceRequest(API.postAliPayCode);
				request.addData("appId", appId);  //AOP分配给应用的唯一标识
				request.addData("version", version); //API 协议版本
				request.addData("alipayClientVersion", alipayClientVersion);
				request.addData("alipayUserId", alipayUserId);
				request.addData("authCode", authCode); //授权码

				CommonTask.requestMutely(request, new CommonTask.TaskListener<Void>() {

					@Override
					protected void onSuccess(Void dto) {
						UserInfoDTO user=SessionManager.getInstance().getUserInfo(getApplicationContext());
						if(user!=null && !CheckUtil.isEmpty(user.getUuid())){
							//设置支付宝登录成功
							SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
						}
					}

					@Override
					protected void onError(int code, String message) {
						super.onError(code, message);
						DialogUtil.showToast(getApplicationContext(), "支付宝登录失败! "+message);
						//doTest();
					}

					
					void doTest() {
						//String json = "{\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\"false\"},\"list\":[{\"uuid\":\"1233\",\"name\":\"天府大萝卜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521264\",\"latitude\":\"31.239977\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜2222\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜道德\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜似懂非懂是\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"}],\"typeList\":[{\"uuid\":\"\",\"name\":\"全部\"},{\"uuid\":\"12\",\"name\":\"好吃\"},{\"uuid\":\"23\",\"name\":\"很好\"},{\"uuid\":\"34\",\"name\":\"凉菜好吃\"},{\"uuid\":\"45\",\"name\":\"热茶\"},{\"uuid\":\"56\",\"name\":\"酒水\"},{\"uuid\":\"57\",\"name\":\"不错的\"}]}";
						//TakeoutRestListDTO dto=JsonUtils.fromJson(json, TakeoutRestListDTO.class);
						//onSuccess(dto);
					}
				});
			}
//			StringBuilder builder = new StringBuilder("支付宝用户ID：");
//			builder.append(alipayUserId).append("\n").append("auth_code:")
//					.append(authCode).append("\n").append("app_id:").append(appId)
//					.append("\n").append("version:").append(version).append("\n")
//					.append("alipay_client_version:").append(alipayClientVersion)
//					.append("\n");
//			
//			DialogUtil.showToast(getApplicationContext(), builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ActivityUtil.exitApp(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			boolean isShowNew = SharedprefUtil.getBoolean(SplashActivity.this, Settings.IS_SHOW_NEW_FEATURE, true);

			Bundle bundle = new Bundle();
			if (isShowNew) {
				ActivityUtil.jump(SplashActivity.this, NewFeaturesActivity.class, 0, bundle);
				finish();
			} else {
				bundle.putBoolean(Settings.isSplashActivity, true);
				ActivityUtil.jump(SplashActivity.this, IndexActivity.class, 0, bundle);
//				ActivityUtil.jump(SplashActivity.this, MdbResListActivity.class, 0, bundle);
				
				finish();
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 软件通用数据，包括了版本更新DTO
	 */
	private void executeSoftwareCommonDataTask() {
		try {
			// 获取渠道号
			ActivityUtil.getChannelId(ContextUtil.getContext());
			// -------------------------------------------------------------------------
			/* 检查是否从旧版升级而来 */
			// 获得当前版本号
			int ver = ActivityUtil.getVersionCode(getBaseContext());
			// 获得配置中的版本号
			int localVer = SharedprefUtil.getInt(getBaseContext(), Settings.LOCAL_VERSION, ver);
			// 检查版本
			if (localVer < ver) {
				// 版本升级时firstopen为true
				SharedprefUtil.saveBoolean(getBaseContext(), Settings.IS_FRIST_WITH_NET, true);
			}
			SharedprefUtil.saveInt(getBaseContext(), Settings.LOCAL_VERSION, ActivityUtil.getVersionCode(getBaseContext()));
			// --------------------------------------------------------------------------
			// 从闪屏进入主动刷新MainPageInfo数据
			Fg114Application.isNeedUpdate = true;
			// --------------------------------------------------------------------------
			final SoftwareCommonData data = SessionManager.getInstance().getSoftwareCommonData();
			showSplashPic(data);
			// ---
			ServiceRequest request = new ServiceRequest(API.getSoftwareCommonData);
			request.addData("cityListTimestamp", data.getCityDto().timestamp);
			request.addData("errorReportTypeListTimestamp", data.getErrorReportTypeListDto().timestamp);

			CommonTask.requestMutely(request, new CommonTask.TaskListener<SoftwareCommonData>() { 

				@Override
				protected void onSuccess(SoftwareCommonData dto) {
					// 两次广告url相等，则不重复加载
					if (!(data != null && dto != null && TextUtils.equals(data.getSplashPicUrl(), dto.getSplashPicUrl()))) {
						showSplashPic(dto);
					}
					SessionManager.getInstance().setSoftwareCommonData(dto);
					if(ActivityUtil.isDebug()){
						//测试
						Settings.APP_WAP_BASE_URL= "http://m.57hao.com/appwap/";
					}else{
						Settings.APP_WAP_BASE_URL=dto.getWapPageUrl();
					}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 显示首页闪屏广告
	void showSplashPic(final SoftwareCommonData data) {
//		final long start = SystemClock.elapsedRealtime();
//		// Log.d("data.getSplashPicUrl()-------",""+data.getSplashPicUrl());
//		//Thread.dumpStack();
//		if (data == null || CheckUtil.isEmpty(data.getSplashPicUrl())) {
//			return;
//		}
//		image.setVisibility(View.GONE);
//		image.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
//		// "http://www.baidu.com/img/bdlogo.gif"
//		// Log.d("setImageByUrl()-------",""+(SystemClock.elapsedRealtime()-start));
//		image.setImageByUrl(data.getSplashPicUrl(), false, 0, ScaleType.FIT_XY, new Runnable() {
//
//			@Override
//			public void run() {
//				runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						// Log.d("setImageByUrl() done-------",""+(SystemClock.elapsedRealtime()-start));
//						image.setVisibility(View.VISIBLE);
//					}
//				});
//			}
//		});

	}
}
