package com.fg114.main.app.activity;

import java.util.Hashtable;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.chat.XiaomishuChat;
import com.fg114.main.app.activity.mealcombo.MealComboListActivity;
import com.fg114.main.app.activity.order.NewOrderListAcitivy;
import com.fg114.main.app.activity.takeaway.NewTakeAwayIndexActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.PushMsgDTO;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;

/**
 * 首页
 * 
 * @author xujianjun,2013-11-07
 */
public class IndexActivity extends MainFrameActivity {

	private static final boolean debug = false;
	private static final String TAG = "IndexActivity";
	private static final boolean DEBUG = Settings.DEBUG;
	private View contentView;
	private ViewGroup window_container;
	private ViewGroup tab_container;
	private ViewGroup sub_tab_container;
	private View button_switch_city;
	private View button_coupon;
	private View button_order_list;
	private View button_search_rest;
	private View button_takeaway;
	private View button_recommend;
	private View button_user_center;
	// private View button_more;

	// private View button_search_rest_layout;
	// private View button_takeaway_layout;
	// private View button_recommend_layout;
	// private View button_user_center_layout;
	// private View button_more_layout;

	// private TextView order_bubble;
	// private TextView order_bubble_clone;
	private TextView user_center_bubble;
	// ---
	private VersionChkDTO checkVerDTO;
	private volatile long timestamp = 0;
	// 版本升级监听
	private Runnable newVersionRun = new Runnable() {

		@Override
		public void run() {
			dealWithVersionDto();
		}
	};
	private CommonObserver.NewVersionObserver newVersionObserver = new CommonObserver.NewVersionObserver(newVersionRun);

	@Override
	public void onCreate(Bundle savedInstanceState) {
//		openMainPage();
		// 首页重新创建了，说明重新进入了软件，取消“已上传”标志
		OpenPageDataTracer.upTag = false;
		super.onCreate(savedInstanceState);
		contentView = View.inflate(this, R.layout.index, null);
		// this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT);
		setContentView(contentView);

		try {
			// 推送消息处理
			Bundle bundle1 = this.getIntent().getExtras();
			if (bundle1 != null && bundle1.containsKey(Settings.BUNDLE_KEY_ID)) {
				Object data = bundle1.getSerializable(Settings.BUNDLE_KEY_ID);
				if (data != null && data instanceof PushMsgDTO) {
					PushCommonActivity.handlePushMessage(IndexActivity.this, (PushMsgDTO) data);
				} 
//				else if (data != null && data instanceof ChatMsgChkData) {
//					ChkHaveChatMsgActivity.handleMessage(IndexActivity.this, (ChatMsgChkData) data);
//				}
			}
			if(bundle1.containsKey(Settings.isSplashActivity)){
				if(bundle1.getBoolean(Settings.isSplashActivity)){
					executePushMessage();
				}
			}

			// 缓存数据获得
			cityInfo = SessionManager.getInstance().getCityInfo(this);

			// 初始化界面
			initComponent();

			CityInfo info = SessionManager.getInstance().getCityInfo(this);
			if (info != null && !CheckUtil.isEmpty(info.getId())) {
				mUpdateCityThread = SessionManager.getInstance().updateGpsCity(this);
			}

			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络的场合
				DialogUtil.showToast(this, getString(R.string.text_dialog_net_unavailable));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		createShortCut();

		// ---
		boolean isFirst = SharedprefUtil.getBoolean(this, Settings.IS_FRIST, true);
		// 如果是第一次使用，去选城市
		if (isFirst) {
			ActivityUtil.jump(IndexActivity.this, CityActivity.class, 0);
			// 改变第一次登录状态
			SharedprefUtil.saveBoolean(this, Settings.IS_FRIST, false);
		}
		// 注册监听
		CommonObservable.getInstance().addObserver(newVersionObserver);

	}

	// ---
	private boolean isNotified;
	private TextView city_name;

	@Override
	public void onResume() {
		super.onResume();

		// restListView.setVisibility(View.VISIBLE);
		// 测试版提示
		if (A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("http://t")) {
			DialogUtil.showToast(ContextUtil.getContext(), "测试版 ");
		}

		// 重设查询条件
		SessionManager.getInstance().getFilter().reset();
		SessionManager.getInstance().getRealTimeResFilter().reset();
		initCityIssues();
		// 通知版本更新，只通知一次
		if (!isNotified) {
			isNotified = true;
			CommonObservable.getInstance().notifyObservers(CommonObserver.NewVersionObserver.class);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// CommonObservable.getInstance().deleteObserver(newVersionObserver);
	}

	@Override
	public void finish() {
		DialogUtil.showAlert(this, true, getString(R.string.text_info_shutdown), new DialogInterface.OnClickListener() {
			// 确定事件
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 退出程序
				dialog.cancel();
				if (mUpdateCityThread != null) {
					mUpdateCityThread.interrupt();
				}
				Settings.CURRENT_PAGE = "";
				IndexActivity.super.finish();
			}
		}, new DialogInterface.OnClickListener() {
			// 取消事件
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 什么也不做
				dialog.cancel();
			}
		});
	}

	/**
	 * 初始化界面
	 */
	private void initComponent() {

		getTitleLayout().setVisibility(View.GONE);
		window_container = (ViewGroup) findViewById(R.id.window_container);
		tab_container = (ViewGroup) findViewById(R.id.tab_container);
		sub_tab_container = (ViewGroup) findViewById(R.id.sub_tab_container);
		// ---
		button_switch_city = (View) findViewById(R.id.button_switch_city);
		city_name = (TextView) findViewById(R.id.city_name);
		button_coupon = (View) findViewById(R.id.button_coupon);
		button_order_list = (View) findViewById(R.id.button_order_list);
		// ---
		button_search_rest = (View) findViewById(R.id.button_search_rest);
		button_takeaway = (View) findViewById(R.id.button_takeaway);
		button_recommend = (View) findViewById(R.id.button_recommend);
		button_user_center = (View) findViewById(R.id.button_user_center);
		// button_more = (View) findViewById(R.id.button_more);
		// ---
		// button_search_rest_layout = (View)
		// findViewById(R.id.button_search_rest_layout);
		// button_takeaway_layout = (View)
		// findViewById(R.id.button_takeaway_layout);
		// button_recommend_layout = (View)
		// findViewById(R.id.button_recommend_layout);
		// button_user_center_layout = (View)
		// findViewById(R.id.button_user_center_layout);
		// button_more_layout = (View) findViewById(R.id.button_more_layout);
		// ---气泡
		// order_bubble = (TextView) findViewById(R.id.order_bubble);
		// order_bubble_clone = (TextView)
		// findViewById(R.id.order_bubble_clone);
		user_center_bubble = (TextView) findViewById(R.id.user_center_bubble);

		// --初始先隐藏
		sub_tab_container.setVisibility(View.GONE);
		//
		sub_tab_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// 拦截点击
			}
		});
		// 找餐厅
		button_search_rest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				XiaomishuChat chat = XiaomishuChat.getInstance(IndexActivity.this);
				chat.showChatDialog();
				
				
//				ViewUtils.preventViewMultipleClick(v, 1000);
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("导航栏找餐厅按钮");
//				// -----
//				setViewSelected(v);
//
//				Bundle bundle = new Bundle();
//				bundle.putBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, true);
//				loadActivity(HotDistrictActivity.class, bundle, false);
			}
		});
		// 叫外卖
		button_takeaway.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏叫外卖按钮");
				// -----
				setViewSelected(v);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, true);
				loadActivity(NewTakeAwayIndexActivity.class, bundle, false);
			}
		});
		// 推荐
		button_recommend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏推荐按钮");
				// -----
				setViewSelected(v);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, true);
				loadActivity(HomeActivity.class, bundle, false);
			}
		});
		// 用户中心
		button_user_center.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏个人中心按钮");
				// -----
				setViewSelected(v);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, true);
				loadActivity(UserCenterActivity.class, bundle, false);
			}
		});
		 // 更多按钮
//		 button_more.setOnClickListener(new OnClickListener() {
//		
//		 @Override
//		 public void onClick(View v) {
//		 ViewUtils.preventViewMultipleClick(v, 300);
//		 // setViewSelected(v);
//		 toggleSubTabMenu();
//		 }
//		 });
		// ------------------------------------------------------------------------------------------------
		// 城市选择按钮
		button_switch_city.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏选择城市按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(IndexActivity.this, CityActivity.class, 0, bundle, false, R.anim.frame_anim_from_bottom, 0);
			}
		});
		// 现金券
		button_coupon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏现金券按钮");
				// -----
				setViewSelected(v);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_IS_QUICK_JUMP, false);
				loadActivity(MealComboListActivity.class, bundle, false);
			}
		});
		// 订单列表页
		button_order_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("导航栏订单中心按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(IndexActivity.this, NewOrderListAcitivy.class, 0, bundle);
			}
		});
		button_recommend.postDelayed(new Runnable() {

			@Override
			public void run() {
				button_recommend.performClick();
			}
		}, 50);

		// ------------------
		syncTabButtonState();
	}

	/**
	 * 根据当前城市，显示不同的按钮
	 */
	private void syncTabButtonState() {
		// 菜单的名称见SessionManager.MENU_NAMES属性
		CityInfo city = SessionManager.getInstance().getCityInfo(this);
		// ---
		if (SessionManager.getInstance().doesCityHaveMainMenuItem("找餐厅", city.getId())) {
			button_search_rest.setVisibility(View.VISIBLE);
			// button_search_rest_layout.setVisibility(View.VISIBLE);
		} else {
			button_search_rest.setVisibility(View.GONE);
			// button_search_rest_layout.setVisibility(View.GONE);
		}
		// --
		if (SessionManager.getInstance().doesCityHaveMainMenuItem("叫外卖", city.getId())) {
			button_takeaway.setVisibility(View.VISIBLE);
			// button_takeaway_layout.setVisibility(View.VISIBLE);
		} else {
			button_takeaway.setVisibility(View.GONE);
			// button_takeaway_layout.setVisibility(View.GONE);
		}
		// --
		if (SessionManager.getInstance().doesCityHaveMainMenuItem("个人中心", city.getId())) {
			button_user_center.setVisibility(View.VISIBLE);
			// button_user_center_layout.setVisibility(View.VISIBLE);
		} else {
			button_user_center.setVisibility(View.GONE);
			// button_user_center_layout.setVisibility(View.GONE);
		}
		// --
		button_switch_city.setVisibility(View.VISIBLE);
		city_name.setText(city.getName());
		// --
		// if (SessionManager.getInstance().doesCityHaveMainMenuItem("现金券",
		// city.getId())) {
		button_coupon.setVisibility(View.VISIBLE);
		// } else {
		// button_coupon.setVisibility(View.GONE);
		// }
		// --
		if (SessionManager.getInstance().doesCityHaveMainMenuItem("订单中心", city.getId())) {
			button_order_list.setVisibility(View.VISIBLE);
		} else {
			button_order_list.setVisibility(View.GONE);
		}
		// adjustOrderNumLayout();
	}

	// 调整订单气泡和订单中心图标的显示，保持同步
	// void adjustOrderNumLayout() {
	// if (button_order_list.getVisibility() == View.GONE) {
	// order_bubble_clone.setVisibility(View.GONE);
	// } else {
	// if (order_bubble_clone.getText().toString().equals("0")) {
	// order_bubble_clone.setVisibility(View.GONE);
	// } else {
	// order_bubble_clone.setVisibility(View.VISIBLE);
	// }
	// }
	//
	// if(sub_tab_container.getVisibility() == View.VISIBLE){
	// order_bubble_clone.setVisibility(View.VISIBLE);
	// }else{
	// order_bubble_clone.setVisibility(View.GONE);
	// }
	// }

	private void setViewSelected(View v) {
		for (int i = 0; i < tab_container.getChildCount(); i++) {
			View tempv = tab_container.getChildAt(i);
			tempv.setSelected(false);
		}
		v.setSelected(true);
	}

	/**
	 * 转换子菜单显示状态
	 */
	private void toggleSubTabMenu() {
		if (sub_tab_container.getVisibility() == View.VISIBLE) {
			hideSubTabMenu();

		} else {
			showSubTabMenu();

		}
	}

	private void showSubTabMenu() {
		if (sub_tab_container.getVisibility() == View.VISIBLE) {
			return;
		}
		Animation in = AnimationUtils.loadAnimation(this, R.anim.index_slide_in_bottom_self);
		in.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				// if(order_bubble_clone.getText().toString().equals("0")){
				// order_bubble_clone.setVisibility(View.GONE);
				// }else{
				// order_bubble_clone.setVisibility(View.VISIBLE);
				// }
			}
		});
		sub_tab_container.clearAnimation();
		sub_tab_container.startAnimation(in);
		sub_tab_container.setVisibility(View.VISIBLE);
	}

	private void hideSubTabMenu() {
		if (sub_tab_container.getVisibility() == View.GONE) {
			return;
		}
		Animation out = AnimationUtils.loadAnimation(this, R.anim.index_slide_out_bottom_self);
		sub_tab_container.clearAnimation();
		out.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				// order_bubble_clone.setVisibility(View.GONE);
				sub_tab_container.clearAnimation();
			}
		});
		sub_tab_container.startAnimation(out);
		sub_tab_container.setVisibility(View.GONE);

	}

	/**
	 * 创建快捷方式
	 */
	private void createShortCut() {
		boolean isSuggestDesktopLink = SharedprefUtil.getBoolean(this, Settings.IS_SUGGEST_DESKTOP_LINK, true);
		if (isSuggestDesktopLink) {
			ActivityUtil.setShortCut(IndexActivity.this);
			SharedprefUtil.saveBoolean(IndexActivity.this, Settings.IS_SUGGEST_DESKTOP_LINK, false);

		}
	}

//	// 记录首页打开
//	private void openMainPage() {
//		ServiceRequest request = new ServiceRequest(API.openMainPage);
//		CommonTask.requestMutely(request, new CommonTask.TaskListener<SoftwareCommonData>() {
//			@Override
//			protected void onSuccess(SoftwareCommonData dto) {
//			}
//
//			@Override
//			protected void onError(int code, String message) {
//			}
//		});
//	}

	// ------------
	// 记录页面载入时的时间，毫秒数
	public static Hashtable<String, Long> pageTimestamp = new Hashtable<String, Long>();
	// 毫秒数缺省页面超时毫秒数，30分钟
	long defaultRefreshPeriod = 1800 * 1000;

	/**
	 * @param clazz
	 *            需要跳转到的Activity
	 * @param needReFresh
	 *            跳转的时候是否要刷新
	 */
	void loadActivity(Class<?> clazz, Bundle data, boolean needReFresh) {
		Long last = pageTimestamp.get(clazz.getSimpleName());
		long passedTime = Long.MAX_VALUE;
		if (last != null) {
			passedTime = SystemClock.elapsedRealtime() - last.longValue();
		}
		// ---
		needReFresh = (needReFresh || passedTime > defaultRefreshPeriod);
		// ---
		Intent intent = new Intent();
		intent.setFlags(needReFresh ? Intent.FLAG_ACTIVITY_CLEAR_TOP : Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setClass(getApplicationContext(), clazz);
		intent.putExtras(data);
		// 下面使用随机id，会产生多个activity的实例
		Window win = getLocalActivityManager().startActivity(clazz.getSimpleName(), intent);
		View root = win.getDecorView();
		window_container.removeAllViews();
		window_container.addView(root);
		// 如果是刷新的，更新时间戳
		if (needReFresh) {
			pageTimestamp.put(clazz.getSimpleName(), SystemClock.elapsedRealtime());
		}
	}

	// ---
	public static void clearTimestamp(String className) {
		pageTimestamp.remove(className);
	}

	// 更新功能系统消息（订单数量，站内信等）
	protected void updateSystemMessage() {
		// 逻辑：从缓存中取出最新的getMainPageInfoPackDTO
		// 1）更新订单气泡 2）决定是否直接跳转订单详情页（相当于弹出消息的性质）
		try{
		MainPageOtherInfoPackDTO dto = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
		if (dto == null || dto.bubbleHintData == null) {
			// order_bubble.setVisibility(View.GONE);
			// order_bubble.setText("0");
			// order_bubble_clone.setVisibility(View.GONE);
			// order_bubble_clone.setText("0");
			user_center_bubble.setVisibility(View.GONE);
			user_center_bubble.setText("0");
			return;
		}
		// ---
		// int orderNum = dto.bubbleHintData.orderNum +
		// dto.bubbleHintData.takeoutOrderNum;
		// 是否显示气泡
		// if (orderNum > 0) {
		// order_bubble.setVisibility(View.VISIBLE);
		// order_bubble.setText("" + orderNum);
		// order_bubble_clone.setVisibility(View.VISIBLE);
		// order_bubble_clone.setText("" + orderNum);
		// } else {
		// order_bubble.setVisibility(View.GONE);
		// order_bubble.setText("0");
		// order_bubble_clone.setVisibility(View.GONE);
		// order_bubble_clone.setText("0");
		// }
		int userCenterBubble = dto.bubbleHintData.mailNum + dto.bubbleHintData.recomCommentNum + dto.bubbleHintData.takeoutOrderNum + dto.bubbleHintData.orderNum + dto.bubbleHintData.todayRewardNum;
		// 右下角的气泡
		if (userCenterBubble > 0) {
			user_center_bubble.setVisibility(View.VISIBLE);
//			user_center_bubble.setText("" + userCenterBubble);
		} else {
			user_center_bubble.setVisibility(View.GONE);
//			user_center_bubble.setText("0");
		}
		}catch (Exception e) {
			// TODO: handle exception
			
		}
		// adjustOrderNumLayout();
	}

	@Override
	protected void onCityChanged() {
		super.onCityChanged();
		syncTabButtonState();
		// 城市改变时，要刷新下当前选中的页面，模拟点击下
		for (int i = 0; i < tab_container.getChildCount(); i++) {
			View v = tab_container.getChildAt(i);
			if (v.isSelected()) {
				v.performClick();
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// 这里拦截触摸事件，当子菜单处于显示状态的时候，如果触摸点在更多按钮之外，则主动隐藏子菜单，更多按钮有自己的逻辑
		if (sub_tab_container.getVisibility() == View.VISIBLE) {
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				Rect rMore = new Rect(); // 更多按钮
				// boolean isMoreValid =
				// button_more.getGlobalVisibleRect(rMore);
				// if (isMoreValid && !rMore.contains((int) ev.getRawX(), (int)
				// ev.getRawY())) {
				// hideSubTabMenu();
				// }
			}
		}

		// order_bubble_clone.setVisibility(View.GONE);
		return super.dispatchTouchEvent(ev);
	}

	private void dealWithVersionDto() {
		contentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					checkVerDTO = Settings.gVersionChkDTO;
					if (checkVerDTO != null) {
						checkVersion(checkVerDTO);
						// Settings.gVersionChkDTO = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	/**
	 * 检查版本处理
	 */
	private boolean isShowing = false;

	private synchronized void checkVersion(final VersionChkDTO dto) {

		if (dto != null) {
			if (dto.haveNewVersionTag && !isShowing) {

				// 获得本地更新情报
				boolean isAutoShow = SharedprefUtil.getBoolean(this, Settings.IS_AUTO_SHOW_UPDATE_DIALOG, true);
				String lastUpdateVer = SharedprefUtil.get(this, Settings.UPDATE_VERSION, "");

				// 如果用户曾经选择过“不再提醒”，但是这里发现的是新版本，则在这里恢复自动提醒
				if (!isAutoShow) {
					if (dto.newVersion.equals(lastUpdateVer)) {
						return;
					} else {
						SharedprefUtil.saveBoolean(this, Settings.IS_AUTO_SHOW_UPDATE_DIALOG, true);
					}
				}
				// 当有新版本的场合
				String alertMsg = "";
				isShowing = true;
				if (dto.needForceUpdateTag) {
					// 必须升级的场合
					alertMsg = DialogUtil.fullMsg(getString(R.string.text_dialog_must_update), dto.newVersion, dto.info);
					// SystemClock.sleep(1000);
					DialogUtil.showVerComfire(IndexActivity.this, true, dto.newVersion, alertMsg, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_KEY_CONTENT, dto.downloadUrl);
							ActivityUtil.jump(IndexActivity.this, AutoUpdateActivity.class, 0, bundle);
							isShowing = false;
						}
					});
				} else {
					if (CheckUtil.isEmpty(dto.info)) {
						alertMsg = DialogUtil.fullMsg(getString(R.string.text_dialog_need_update), dto.newVersion, "");
					} else {
						alertMsg = DialogUtil.fullMsg(getString(R.string.text_dialog_need_update), dto.newVersion, "," + dto.info);
					}
					// SystemClock.sleep(1000);
					DialogUtil.showVerComfire(IndexActivity.this, false, dto.newVersion, alertMsg, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_KEY_CONTENT, dto.downloadUrl);
							ActivityUtil.jump(IndexActivity.this, AutoUpdateActivity.class, 0, bundle);
							isShowing = false;
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							isShowing = false;
						}
					});
				}
			}
		}
	}
	
	private void dopush(final PushMsgDTO dto){
		if (dto != null) {
			if (dto.getTypeTag() > 0) {
				String cancel="";
				String ok="";
				if(CheckUtil.isEmpty(dto.getCancelButtonName())){
					cancel="取消";
				}else{
					cancel=dto.getCancelButtonName();
				}
				if(CheckUtil.isEmpty(dto.getOkButtonName())){
					ok="去看看";
				}else{
					ok=dto.getOkButtonName();
				}
				DialogUtil.showAlert(IndexActivity.this, true, dto.getTitle(), cancel, ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
//						try {
//							PushMessageReceiver.clearNotification();
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
						
						
					}
				},new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (dto.getTypeTag() == 1) {
							// 广告链接，使用内嵌的WebView打开
							Bundle bd = new Bundle();
							bd.putString(Settings.BUNDLE_KEY_WEB_URL, dto.getAdvUrl());
							bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, dto.getTitle());
							ActivityUtil.jump(IndexActivity.this, SimpleWebViewActivity.class, 0, bd);

						} else if (dto.getTypeTag() == 2) {
							// 本地链接，跳转本地界面
							URLExecutor.execute( dto.getAdvUrl(), IndexActivity.this, 0);
						} else if (dto.getTypeTag() == 3) {
							// 普通链接，使用系统浏览器打开
							ActivityUtil.jumbToWeb(IndexActivity.this,  dto.getAdvUrl());
						}
//						try {
//							PushMessageReceiver.clearNotification();
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
					}
					
				});
			}
		}
	}
}