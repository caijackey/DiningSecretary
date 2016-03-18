package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.order.FastBookingActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantHotsaleActivity;
import com.fg114.main.app.activity.top.TopListActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.AutoCompleteRestSuggestAdapter;
import com.fg114.main.app.adapter.UsedHistorySuggestListAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.view.OrderSelectionWheelView;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.HotRestTypeListDTO;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.service.dto.RestSearchSuggestListDTO;
import com.fg114.main.service.dto.RoomTypeInfoData;
import com.fg114.main.service.dto.SuggestResultData;
import com.fg114.main.service.dto.UsedHistorySuggestListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.AddDebugAccountTask;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.GetRestSearchSuggestListTask;
import com.fg114.main.service.task.GetUsedHistorySuggestListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.TrackTool;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

public class HotDistrictActivity extends MainFrameActivity {

	private static final String TAG = "HotDistrictActivity";

	private static final String KEY_HOT_DISTRICT_LIST = "hot_district_list";
	private static final String KEY_DEFAULT_REST = "KEY_DEFAULT_REST";
	private static final int HOT_DISTRICT_CACHE_TIME = 1 * 24 * 60; // 缓存时间1天，即1*24*60分钟

	private Context mCtx = this;
	//
	private View contentView;
	private LinearLayout latelyBrowseDistrict;
	private TextView tvLatelyBrowse;

	// 商区历史浏览记录缓存
	private CommonTypeListDTO browseHistory;
	private LinearLayout hot_district_layout;
	private LinearLayout hot_district_container;
	private LinearLayout hot_main_menu_layout;
	private LinearLayout hot_main_menu_container;
	private LinearLayout hot_subway_layout;
	private LinearLayout hot_subway_container;

	private boolean needHideBackButton;

	private ViewGroup mVgSearch;

	private Button mBtVoice;

	private TextView test_entry;

	private TextView build_version;

	private LinearLayout top_list_layout;

	private LinearLayout top_list_container;

//	private OrderSelectionWheelView orderInfoWheel;
//	private Button orderSubmit;
//	private ViewGroup fast_booking_search_text_layout;
//	private Button fast_booking_search_right_button;
//	private TextView fast_booking_search_text_view;
//	private ViewGroup fast_booking_top_layout;
//	private ViewGroup fast_booking_bottom_layout;
//	private ImageView button_order_phone_call;
//	private TextView fast_booking_search_order_hint_text;
//	private ImageView button_order_bottom_line_image;
//	private ImageView promotion_icon_mibi;
//	private TextView promotion_mibi;
//	private TextView promotion_discount;
//	private TextView promotion_coupon;
	private TextView change_locbaidu_or_gps;
	protected View res_food_list_item_promotion_icon_mibi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("热门分类", "");
		// ----------------------------

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			needHideBackButton = bundle.getBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, false);
		}

		// 获取屏幕的宽高属性 ，标题栏和状态栏高度
		try {
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

		executeTask();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("热门分类", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();

		Settings.CURRENT_PAGE = getClass().getSimpleName();

		initCityIssues();
//		setDefaultRest();
	}

	// 设置默认餐厅
//	private void setDefaultRest() {
//		// 设置上次餐厅
//		SuggestResultData defaultRest = SessionManager.getInstance().getLastSelectedRest();
//		if (defaultRest == null) {
//			getSuggestRDataAccord2Net();
//		} else {
//			setRestOrderInfo(defaultRest);
//		}
//	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	void initComponent() {
		// 顶部
		getTitleLayout().setVisibility(View.GONE);
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getTvTitle().setText("热门分类");
		if (needHideBackButton) {
			this.getBtnGoBack().setVisibility(View.INVISIBLE);
		}
		// 中间内容区
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.hot_district_layout, null);
		// 顶部搜索
		mVgSearch = (ViewGroup) contentView.findViewById(R.id.index_new_search_bar_rlSearch);
		mBtVoice = (Button) contentView.findViewById(R.id.index_new_search_bar_btVoice);

		// 热门商区
		hot_district_layout = (LinearLayout) contentView.findViewById(R.id.hot_district_layout);
		hot_district_container = (LinearLayout) contentView.findViewById(R.id.hot_district_container);
		// 热门菜系
		hot_main_menu_layout = (LinearLayout) contentView.findViewById(R.id.hot_main_menu_layout);
		hot_main_menu_container = (LinearLayout) contentView.findViewById(R.id.hot_main_menu_container);
		// 热门线路
		hot_subway_layout = (LinearLayout) contentView.findViewById(R.id.hot_subway_layout);
		hot_subway_container = (LinearLayout) contentView.findViewById(R.id.hot_subway_container);
		// 热门榜单
		top_list_layout = (LinearLayout) contentView.findViewById(R.id.top_list_layout);
		top_list_container = (LinearLayout) contentView.findViewById(R.id.top_list_container);

		// 商区浏览记录
		latelyBrowseDistrict = (LinearLayout) contentView.findViewById(R.id.lately_browse_district_layoutId);
		tvLatelyBrowse = (TextView) contentView.findViewById(R.id.tvLatelyBrowse);
		//
		test_entry = (TextView) contentView.findViewById(R.id.test_entry);
		change_locbaidu_or_gps = (TextView) contentView.findViewById(R.id.change_locbaidu_or_gps);
		// ---
		build_version = (TextView) contentView.findViewById(R.id.build_version);

//		orderInfoWheel = (OrderSelectionWheelView) contentView.findViewById(R.id.order_info_selection_wheel);
//		fast_booking_top_layout = (ViewGroup) contentView.findViewById(R.id.fast_booking_top_layout);
//		fast_booking_bottom_layout = (ViewGroup) contentView.findViewById(R.id.fast_booking_bottom_layout);
//		fast_booking_search_text_layout = (ViewGroup) contentView.findViewById(R.id.fast_booking_search_text_layout);
//		fast_booking_search_right_button = (Button) contentView.findViewById(R.id.fast_booking_search_right_button);
//		fast_booking_search_text_view = (TextView) contentView.findViewById(R.id.fast_booking_search_text_view);
		// 优惠四控件
//		promotion_icon_mibi = (ImageView) contentView.findViewById(R.id.promotion_icon_mibi);
//		promotion_mibi = (TextView) contentView.findViewById(R.id.promotion_mibi);
//		promotion_discount = (TextView) contentView.findViewById(R.id.promotion_discount);
//		promotion_coupon = (TextView) contentView.findViewById(R.id.promotion_coupon);
		//
//		orderSubmit = (Button) contentView.findViewById(R.id.button_order_submit);

//		button_order_phone_call = (ImageView) contentView.findViewById(R.id.button_order_phone_call);
//		fast_booking_search_order_hint_text = (TextView) contentView.findViewById(R.id.fast_booking_search_order_hint_text);
//		button_order_bottom_line_image = (ImageView) contentView.findViewById(R.id.button_order_bottom_line_image);

//		promotion_icon_mibi.setVisibility(View.GONE);
//		promotion_mibi.setVisibility(View.GONE);
//		promotion_discount.setVisibility(View.GONE);
//		promotion_coupon.setVisibility(View.GONE);
		this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// ---------------------------------------------------------------------------------------------------------
//		// 搜索
//		fast_booking_search_text_layout.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				ViewUtils.preventViewMultipleClick(v, 1000);
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("餐厅搜索输入框");
//				// -----
//				showSearchAnimation();
//
//				fast_booking_top_layout.postDelayed(new Runnable() {
//
//					@Override
//					public void run() {
//						showSearchRestDialog("");
//					}
//				}, 200);
//			}
//
//		});

		// 历史
//		fast_booking_search_right_button.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				ViewUtils.preventViewMultipleClick(v, 1000);
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("历史记录按钮");
//				// -----
//				fast_booking_search_text_layout.performClick();
//			}
//		});
//		orderSubmit.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (fast_booking_search_text_layout.getTag() == null) {
//
//					DialogUtil.showAlert(HotDistrictActivity.this, "提示", "请选择一家餐厅");
//					return;
//				}
//
//				// 跳下单页
//				SuggestResultData data = (SuggestResultData) fast_booking_search_text_layout.getTag();
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("提交订单按钮", data.restId);
//				// -----
//				Bundle bundle = new Bundle();
//				bundle.putString(Settings.BUNDLE_REST_ID, data.restId);
//				bundle.putString(Settings.BUNDLE_REST_NAME, data.restName);
//				bundle.putLong(Settings.BUNDLE_ORDER_TIME, orderInfoWheel.getDateMilliSeconds() + orderInfoWheel.getTimeMilliSeconds());
//				bundle.putLong(Settings.BUNDLE_ORDER_PEOPLE_NUM, orderInfoWheel.getPeopleNum());
//				bundle.putLong(Settings.BUNDLE_ORDER_ROOM_TYPE, orderInfoWheel.getRoomType());
//				ActivityUtil.jump(HotDistrictActivity.this, MyBookRestaurantActivity.class, 0, bundle);
//
//			}
//		});

		// ---------------------------------------------------------------------------------------------
		hot_district_layout.setVisibility(View.GONE);
		hot_main_menu_layout.setVisibility(View.GONE);
		hot_subway_layout.setVisibility(View.GONE);
		top_list_layout.setVisibility(View.GONE);

		build_version.setText(Settings.BuildVersion);
		// ---------------------------------------------------------------------------------------------

		// 添加测试机器入口，长按4秒
		test_entry.setOnTouchListener(new OnTouchListener() {
			long timestamp = 0;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					timestamp = SystemClock.elapsedRealtime();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (SystemClock.elapsedRealtime() - timestamp > 4000) {
						showAddTesterDialog();
					}
				}

				return false;
			}

		});

		// 点击标题选择GPS或百度定位
		if (ActivityUtil.isTestDev(HotDistrictActivity.this)) {
			change_locbaidu_or_gps.setOnClickListener(new View.OnClickListener() {

				private int clickcount;

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					clickcount++;
					if (clickcount >= 7) {
						clickcount = 0;
						//必须先退后台 在启动 
						if (Settings.gBaiduAvailable) {
							Settings.gBaiduAvailable = false;
							DialogUtil.showToast(HotDistrictActivity.this, "GPS定位");
						} else {
							Settings.gBaiduAvailable = true;
							DialogUtil.showToast(HotDistrictActivity.this, "百度定位");
						}
						
					}
				}
			});
		}

		// 初始化搜索框
		mVgSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("关键词搜索输入框");
				// -----
				AutoCompleteAdapter.isRecomRest = false;
				ActivityUtil.jump(HotDistrictActivity.this, AutoCompleteActivity.class, 0, new Bundle());
				overridePendingTransition(R.anim.activity_enter, R.anim.activity_enter);
			}
		});

		mBtVoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.showVoiceDialogForSearch(HotDistrictActivity.this, 1, new ActivityUtil.OnRecognizedFinishListener() {

					@Override
					public void onRecognizedFinish(String text) {
						// -----
						OpenPageDataTracer.getInstance().addEvent("语音按钮", text);
						// -----
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_KEY_KEYWORD, text);
						AutoCompleteActivity.voiceInputTag = 1;
						ActivityUtil.jump(HotDistrictActivity.this, AutoCompleteActivity.class, 0, bundle);
					}
				});
			}
		});

	}

	// ----------第一次进界面 通过网络获取餐厅信息------------------------------
	/*
	 * 获得快捷预订页面信息，返回SuggestResultData getQuickOrderInfo("/getQuickOrderInfo",
	 * new ParamProtocol() ),
	 */
	// ----------------------------------------
//	private void getSuggestRDataAccord2Net() {
//		// --------------
//		OpenPageDataTracer.getInstance().addEvent("页面查询");
//		// --------------
//
//		ServiceRequest request = new ServiceRequest(API.getQuickOrderInfo);
//		CommonTask.request(request, new CommonTask.TaskListener<SuggestResultData>() {
//
//			@Override
//			protected void onSuccess(SuggestResultData dto) {
//				// --------------
//				OpenPageDataTracer.getInstance().endEvent("页面查询");
//				// --------------
//				setRestOrderInfo(dto);
//			}
//
//			@Override
//			protected void onError(int code, String message) {
//				// doTest();
//				// --------------
//				OpenPageDataTracer.getInstance().endEvent("页面查询");
//				// --------------
//			}
//			// @Override
//			// protected void
//			// defineCacheKeyAndTime(CommonTask.TaskListener.CacheKeyAndTime
//			// keyAndTime) {
//			// CityInfo city =
//			// SessionManager.getInstance().getCityInfo(HotDistrictActivity.this);
//			//
//			// keyAndTime.cacheKey = KEY_DEFAULT_REST + "|" + city.getId();
//			// keyAndTime.cacheTimeMinute = HOT_DISTRICT_CACHE_TIME; //
//			// //缓存时间1天，即1*24*60分钟
//			// }
//		});
//	}

//	private void showSearchAnimation() {
//		super.getTitleLayout().startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_out_top));
//		fast_booking_top_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_out_top));
//		fast_booking_bottom_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_out_bottom));
//
//	}

	// 真实数据请求
	private void executeTask() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		/**
		 * 注意，浏览记录功能已取消！ 需求：缓存一天
		 */
		ServiceRequest request = new ServiceRequest(API.getHotRestTypeList);
		CommonTask.request(request, new CommonTask.TaskListener<HotRestTypeListDTO>() {
			protected void onSuccess(HotRestTypeListDTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				if (dto == null) {
					return;
				}
				// 加载数据到热门商圈
				try {
					loadDataToHotDistrict(dto.districtDTO == null ? new ArrayList<CommonTypeDTO>() : dto.districtDTO.getList());
					loadDataToHotMainMenu(dto.menuDTO == null ? new ArrayList<CommonTypeDTO>() : dto.menuDTO.getList());
					loadDataToHotSubway(dto.subwayDTO == null ? new ArrayList<CommonTypeDTO>() : dto.subwayDTO.getList());
					loadDataToTopList(dto.topRestTypeDTO == null ? new ArrayList<CommonTypeDTO>() : dto.topRestTypeDTO.getList());
				} catch (Throwable e) {
					e.printStackTrace();
				}

			};

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				IndexActivity.clearTimestamp(HotDistrictActivity.this.getClass().getSimpleName());
			}

			@Override
			protected void defineCacheKeyAndTime(CommonTask.TaskListener.CacheKeyAndTime keyAndTime) {
				CityInfo city = SessionManager.getInstance().getCityInfo(HotDistrictActivity.this);

				keyAndTime.cacheKey = KEY_HOT_DISTRICT_LIST + "|" + city.getId();
				keyAndTime.cacheTimeMinute = HOT_DISTRICT_CACHE_TIME; //
				// 缓存时间1天，即1*24*60分钟
			}

		});
	}

	// -----
	private void loadDataToHotSubway(List<CommonTypeDTO> commonTypeDTOs) {
		// 地铁沿线数据
		String extraItemText = ""; // 地铁沿线没有“更多。。。”
		if (commonTypeDTOs == null) {
			commonTypeDTOs = new ArrayList<CommonTypeDTO>();
		}
		if (commonTypeDTOs.size() == 0) {
			hot_subway_layout.setVisibility(View.GONE);
		} else {
			hot_subway_layout.setVisibility(View.VISIBLE);
		}
		// --
		loadTableLayout(hot_subway_container, commonTypeDTOs, 4, mCtx, extraItemText, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				CommonTypeDTO tag = (CommonTypeDTO) v.getTag();
				if (tag == null) {
					return;
				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("地铁线路按钮", tag.getUuid());
					// -----
					// 设置筛选条件
					// SessionManager.getInstance().getFilter().setSubwayTag(false);
					// SessionManager.getInstance().getFilter().setDistanceMeter(0);
					// SessionManager.getInstance().getFilter().setRegionId("0");
					// SessionManager.getInstance().getFilter().setDistrictId("0");
					// SessionManager.getInstance().getFilter().setMainMenuId(tag.getParentId());
					// SessionManager.getInstance().getFilter().setSubMenuId(tag.getUuid());
					// 去选站台
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_FUNC_NAME, "全部地铁沿线");
					bundle.putString(Settings.BUNDLE_KEY_ID, tag.getUuid());
					ActivityUtil.jump(mCtx, DistrictChoosingActivity.class, 0, bundle);
				}
			}
		});
	}

	private void loadDataToHotMainMenu(List<CommonTypeDTO> commonTypeDTOs) {
		hot_main_menu_layout.setVisibility(View.VISIBLE);
		// 热门菜系数据
		String extraItemText = "更多菜系...";
		if (commonTypeDTOs == null) {
			commonTypeDTOs = new ArrayList<CommonTypeDTO>();
		}
		// --
		loadTableLayout(hot_main_menu_container, commonTypeDTOs, 4, mCtx, extraItemText, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				CommonTypeDTO tag = (CommonTypeDTO) v.getTag();
				if (tag == null) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("更多菜系按钮");
					// -----
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_FUNC_NAME, "全部菜系");
					ActivityUtil.jump(mCtx, DistrictChoosingActivity.class, 0, bundle);

				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("菜系按钮", tag.getUuid());
					// -----
					// 设置筛选条件
					SessionManager.getInstance().getFilter().setSubwayTag(false);
					SessionManager.getInstance().getFilter().setDistanceMeter(0);
					SessionManager.getInstance().getFilter().setRegionId("0");
					SessionManager.getInstance().getFilter().setDistrictId("0");
					SessionManager.getInstance().getFilter().setMainMenuId(tag.getParentId());
					SessionManager.getInstance().getFilter().setSubMenuId(tag.getUuid());
					// 去餐厅美食列表页
					ActivityUtil.jump(mCtx, ResAndFoodListActivity.class, 0, null);
				}
			}
		});
	}

	private void loadDataToHotDistrict(List<CommonTypeDTO> commonTypeDTOs) {
		hot_district_layout.setVisibility(View.VISIBLE);
		// 热门商区数据
		String extraItemText = "更多商区...";
		// 要求把“附近”加在第一个
		if (commonTypeDTOs == null) {
			commonTypeDTOs = new ArrayList<CommonTypeDTO>();
		}
		CommonTypeDTO nearby = new CommonTypeDTO();
		nearby.setUuid("nearby");
		nearby.setName("附近");
		nearby.setMemo("文字要变颜色");
		commonTypeDTOs.add(0, nearby);
		// --
		loadTableLayout(hot_district_container, commonTypeDTOs, 4, mCtx, extraItemText, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				CommonTypeDTO tag = (CommonTypeDTO) v.getTag();
				if (tag == null) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("更多商区按钮");
					// -----
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_FUNC_NAME, "全部商区");
					ActivityUtil.jump(mCtx, DistrictChoosingActivity.class, 0, bundle);

				} else if ("nearby".equals(tag.getUuid())) {// 是附近
					// ----------------------------
					OpenPageDataTracer.getInstance().addEvent("附近按钮");
					// ----------------------------
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "订餐厅");
					bundle.putString(Settings.BUNDLE_FUNC_NAME, "搜索附近餐厅");
					ActivityUtil.isLocExist(HotDistrictActivity.this, Settings.STATUTE_CHANNEL_RESTAURANT, 0, bundle);

				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("商区按钮", tag.getUuid());
					// -----
					// 设置筛选条件
					SessionManager.getInstance().getFilter().setSubwayTag(false);
					SessionManager.getInstance().getFilter().setDistanceMeter(0);
					SessionManager.getInstance().getFilter().setRegionId(tag.getParentId());
					SessionManager.getInstance().getFilter().setDistrictId(tag.getUuid());
					// 去餐厅美食列表页
					ActivityUtil.jump(mCtx, ResAndFoodListActivity.class, 0, null);
				}
			}
		});

	}

	// 设置榜单数据
	private void loadDataToTopList(List<CommonTypeDTO> commonTypeDTOs) {
		CityInfo city = SessionManager.getInstance().getCityInfo(this);
		// 如果当前城市没有榜单，隐藏
		if (SessionManager.getInstance().doesCityHaveMainMenuItem("榜单", city.getId())) {
			top_list_layout.setVisibility(View.VISIBLE);
		} else {
			top_list_layout.setVisibility(View.GONE);
			return;
		}
		// 热门榜单数据
		String extraItemText = "更多榜单...";
		// 要求把“附近”加在第一个
		if (commonTypeDTOs == null) {
			commonTypeDTOs = new ArrayList<CommonTypeDTO>();
		}
		// 热门榜单的Layout容器，里面容有TextView
		loadTableLayout(top_list_container, commonTypeDTOs, 2, mCtx, extraItemText, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				CommonTypeDTO tag = (CommonTypeDTO) v.getTag();
				if (tag == null) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("更多榜单按钮");
					// -----
					Bundle bundle = new Bundle();
					ActivityUtil.jump(mCtx, TopListActivity.class, 0, bundle);

				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("榜单按钮", tag.getUuid());

					// 设置筛选条件
					SessionManager.getInstance().getFilter().setSubwayTag(false);
					SessionManager.getInstance().getFilter().setDistanceMeter(0);
					SessionManager.getInstance().getFilter().setRegionId("");
					SessionManager.getInstance().getFilter().setDistrictId("");
					SessionManager.getInstance().getFilter().setMainTopRestTypeId(tag.getParentId());
					SessionManager.getInstance().getFilter().setSubTopRestTypeId(tag.getUuid());
					// 去热门榜单列表页
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_TYPEID, tag.getUuid());
					ActivityUtil.jump(mCtx, RestaurantHotsaleActivity.class, 0, bundle);
				}
			}
		});

	}

	/**
	 * load the ourselves grid view (为了商区而用) 每个Item
	 * View的Tag中存储的便是dataSources中的每个对象,额外item view 中的Tag 是null
	 * 
	 * @param <E>
	 * @param parent
	 *            parent layout
	 * @param dataSources
	 *            the binding data
	 * @param numColumns
	 *            the number of column
	 * @param context
	 * @param extraItemText
	 *            额外单元格信息,如果extraItemText为null或者为空，则不显示更多信息
	 * @param onItemClickListener
	 */
	@SuppressWarnings("ResourceAsColor")
	<E> void loadTableLayout(LinearLayout parent, List<E> dataSources, int numColumns, Context context, String extraItemText, OnClickListener onItemClickListener) {
		int location = 0; // the location of data

		// compute row numbers
		int numRows = dataSources.size() / numColumns;
		if (dataSources.size() % numColumns != 0) {
			numRows++;
		} else {
			if (!TextUtils.isEmpty(extraItemText)) {
				numRows++;
			}
		}

		// 这里item宽度的获取比较坑，回头再改进吧
		int itemWidth = (UnitUtil.getScreenWidthPixels() - 2 * UnitUtil.dip2px(10)) / numColumns;
		// int itemWidth = parent.getWidth() / numColumns;
		// int itemWidth = UnitUtil.dip2px(75);
		// 160; // 单位px
		int itemHeight = UnitUtil.dip2px(40);
		// int itemHeight = UnitUtil.dip2px(40);
		// 80;
		for (int i = 0; i < numRows; i++) { // add a row
			LinearLayout ll = new LinearLayout(context);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			ll.setLayoutParams(layoutParams);
			for (int j = 0; j < numColumns; j++) { // add numColumns columns
				if (location >= dataSources.size() || dataSources.get(location) == null) {
					if (TextUtils.isEmpty(extraItemText)) {
						break;
					}
					Button btMore;
					if (dataSources.size() % numColumns == 0) { //
						LinearLayout llMore = new LinearLayout(context);
						llMore.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

						btMore = new Button(context);
						btMore.setLayoutParams(new LayoutParams(itemWidth * numColumns, itemHeight));
						llMore.addView(btMore);
						parent.addView(llMore);
					} else {
						btMore = new Button(context);
						btMore.setLayoutParams(new LayoutParams(itemWidth * (numColumns - j), itemHeight)); // 注意这个宽度的设置
						ll.addView(btMore);
					}
					btMore.setText(extraItemText);
					btMore.setBackgroundResource(R.drawable.button_light_color_effect);
					btMore.setGravity(Gravity.CENTER);
					btMore.setPadding(5, 5, 5, 5);
					btMore.setSingleLine(true);

					btMore.setTextColor(R.color.text_color_gray);

					btMore.setOnClickListener(onItemClickListener);
					break;
				}
				Button btItem = new Button(context);
				btItem.setLayoutParams(new LayoutParams(itemWidth, itemHeight));
				btItem.setBackgroundResource(R.drawable.button_light_color_effect);
				btItem.setText(dataSources.get(location).toString());

				try {
					if ("文字要变颜色".equals(((CommonTypeDTO) dataSources.get(location)).getMemo())) {
						btItem.setTextColor(getResources().getColor(R.color.new_text_color_red));
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}

				btItem.setGravity(Gravity.CENTER);
				btItem.setSingleLine(true);
				btItem.setEllipsize(TruncateAt.END);
				btItem.setPadding(5, 5, 5, 5);
				btItem.setTag(dataSources.get(location));
				btItem.setOnClickListener(onItemClickListener);
				ll.addView(btItem);
				if (j + 1 != numColumns) { // control vertical line
					View verticalLine = new View(context);
					verticalLine.setLayoutParams(new LayoutParams(1, itemHeight));
					verticalLine.setBackgroundResource(R.color.border_color_gray);
					ll.addView(verticalLine);
				}
				location++;

			}

			parent.addView(ll); //

			// control transverse line
			if (i + 1 != numRows) {
				// Log.e("transverseLine", i + "----" + numRows);
				View transverseLine = new View(context);
				transverseLine.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1));
				transverseLine.setBackgroundResource(R.color.border_color_gray);
				parent.addView(transverseLine);
			}
		}

	}

	// 添加测试机
	private void showAddTesterDialog() {
		final TextView title = new TextView(this);
		title.setText("添加测试机");
		title.setTextColor(0xFFFFFFFF);
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		title.setBackgroundColor(0x99000000);
		title.setGravity(Gravity.CENTER);
		title.setPadding(20, 20, 20, 20);
		// ---
		final EditText name = new EditText(this);
		name.setHint("Dname");
		final EditText pass = new EditText(this);
		pass.setHint("Dpassword");
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(title);
		layout.addView(name);
		layout.addView(pass);
		// 创建提示框
		Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setView(layout);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				try {
					String sname = name.getText().toString().trim();
					String spass = pass.getText().toString().trim();
					if (CheckUtil.isEmpty(sname) || CheckUtil.isEmpty(spass)) {
						DialogUtil.showToast(getApplicationContext(), "名字和密码不能为空！");
						return;
					}
					new AddDebugAccountTask("正在提交，请稍候...", HotDistrictActivity.this, sname, spass).execute(new Runnable() {

						@Override
						public void run() {
							// DialogUtil.showToast(getApplicationContext(),
							// "提交成功！");
							dialog.dismiss();
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

//	private void setRestOrderInfo(final SuggestResultData data) {
//		if (data == null) {
//			fast_booking_search_text_layout.setTag(null);
//			fast_booking_search_text_view.setText("");
//			promotion_icon_mibi.setVisibility(View.GONE);
//			promotion_mibi.setVisibility(View.GONE);
//			promotion_discount.setVisibility(View.GONE);
//			promotion_coupon.setVisibility(View.GONE);
//			return;
//		}
//		fast_booking_search_text_layout.setTag(data);
//		fast_booking_search_text_view.setText(data.restName);
//		// 优惠信息
//		// 图标标志 0:无图标 1：券 2：惠 3：币 4：币(高亮)
//		if (data.iconTag == 1) {
//			promotion_icon_mibi.setVisibility(View.GONE);
//			promotion_mibi.setVisibility(View.GONE);
//			promotion_discount.setVisibility(View.GONE);
//
//			promotion_coupon.setVisibility(View.VISIBLE);
//			promotion_coupon.setText(data.iconTitle);
//		} else if (data.iconTag == 2) {
//			promotion_icon_mibi.setVisibility(View.GONE);
//			promotion_mibi.setVisibility(View.GONE);
//			promotion_coupon.setVisibility(View.GONE);
//
//			promotion_discount.setVisibility(View.VISIBLE);
//			promotion_discount.setText(data.iconTitle);
//		} else if (data.iconTag == 3) {
//			promotion_discount.setVisibility(View.GONE);
//			promotion_coupon.setVisibility(View.GONE);
//
//			promotion_mibi.setVisibility(View.VISIBLE);
//			promotion_mibi.setText(data.iconTitle);
//			promotion_icon_mibi.setVisibility(View.VISIBLE);
//			promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_1);
//		} else if (data.iconTag == 4) {
//			promotion_discount.setVisibility(View.GONE);
//			promotion_coupon.setVisibility(View.GONE);
//
//			promotion_mibi.setVisibility(View.VISIBLE);
//			promotion_mibi.setText(data.iconTitle);
//			promotion_icon_mibi.setVisibility(View.VISIBLE);
//			promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_2);
//		} else {
//			promotion_icon_mibi.setVisibility(View.GONE);
//			promotion_mibi.setVisibility(View.GONE);
//			promotion_discount.setVisibility(View.GONE);
//			promotion_coupon.setVisibility(View.GONE);
//		}

		//
//		if (data.stateTag == 1) {// 1:可以下单
//
//			if (data.orderSelInfo == null) {
//				data.orderSelInfo = SessionManager.getInstance().getOrderSelInfo();
//			}
//			//
//			if (data.orderSelInfo == null) {
//				data.orderSelInfo = new OrderSelInfo();
//			}
//			//
//			if (data.roomTypeInfoData == null) {
//				data.roomTypeInfoData = new RoomTypeInfoData();
//			}
//			orderInfoWheel.initData(data.orderSelInfo, data.roomTypeInfoData);
//			 orderInfoWheel.setRoomTypeInfoData(data.roomTypeInfoData);
//			// ---
//			orderInfoWheel.setVisibility(View.VISIBLE);
//			orderSubmit.setVisibility(View.VISIBLE);
//			button_order_phone_call.setVisibility(View.GONE);
//			fast_booking_search_order_hint_text.setVisibility(View.GONE);
//			button_order_bottom_line_image.setVisibility(View.GONE);
//
//		} else if (data.stateTag == 2) {// 2：可以打电话
//			orderInfoWheel.setVisibility(View.GONE);
//			orderSubmit.setVisibility(View.GONE);
//			button_order_phone_call.setVisibility(View.VISIBLE);
//			fast_booking_search_order_hint_text.setVisibility(View.VISIBLE);
//			button_order_bottom_line_image.setVisibility(View.VISIBLE);
//
//			button_order_phone_call.setImageResource(R.drawable.button_index_phone_order);
//			fast_booking_search_order_hint_text.setText(data.restTelForShow);
//			button_order_phone_call.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					ViewUtils.preventViewMultipleClick(v, 1000);
//					if (fast_booking_search_text_layout.getTag() == null || CheckUtil.isEmpty(((SuggestResultData) fast_booking_search_text_layout.getTag()).restTelForCall)) {
//						DialogUtil.showToast(getApplication(), "该餐厅没有预订电话");
//					} else {
//						ActivityUtil.callSuper57(HotDistrictActivity.this, ((SuggestResultData) fast_booking_search_text_layout.getTag()).restTelForCall);
//						new Runnable() {
//							public void run() {
//								try {
//									// ----------------------------
//									OpenPageDataTracer.getInstance().addEvent("拨打电话", data.restTelForCall);
//									// ----------------------------
//
//									ServiceRequest.callTel(1, data.restId, data.restTelForCall);
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						}.run();
//					}
//				}
//			});
//
//		} else {// 3：不能下单也不能打电话
//			orderInfoWheel.setVisibility(View.GONE);
//			orderSubmit.setVisibility(View.GONE);
//			button_order_phone_call.setVisibility(View.VISIBLE);
//			fast_booking_search_order_hint_text.setVisibility(View.VISIBLE);
//			button_order_bottom_line_image.setVisibility(View.VISIBLE);
//
//			button_order_phone_call.setImageResource(R.drawable.index_phone_order_02);
//			fast_booking_search_order_hint_text.setTextColor(getResources().getColor(R.color.text_color_gray));
//			fast_booking_search_order_hint_text.setText("该餐厅暂无电话信息");
//			button_order_phone_call.setOnClickListener(null);
//		}
//	}

	void showSearchRestDialog(final String type) {
		DialogUtil.showDialog(this, R.layout.index_pop_search, new DialogUtil.DialogEventListener() {
			PopupWindow dialog;
			private String keyword = "";
			// 本地缓存数据
			private List<SuggestResultData> restSearchSuggestHistoryList;
			// 界面组件
			// private Spinner channelSpinner;
			private EditText etAutoComplete;
			private Button btHistory;
			private Button btCancel;
			private ListView lvAutoComplete;
			private ListView lvHistory;
			private AutoCompleteRestSuggestAdapter adapter;
			private UsedHistorySuggestListAdapter adapterHistory;
			private boolean isTaskSafe = true;
			private boolean isLast = true;
			private boolean isRefreshFoot = false;
			private int startIndex = 1;
			// ---
			private boolean isTaskSafe2 = true;
			private boolean isLast2 = true;
			private boolean isRefreshFoot2 = false;
			private int startIndex2 = 1;

			// 任务
			private GetRestSearchSuggestListTask getSuggestKeywordListTask;

			private AtomicLong mSearchTimestamp = new AtomicLong();

			private Handler searchHandler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					long timstamp = (Long) msg.obj;
					if (mSearchTimestamp.longValue() == timstamp) {
						// 时间戳与当前时间戳相同时则执行搜索
						adapter.isReset = true;
						// 重新还原列表数据
						executeGetSuggestKeywordListTask(keyword);
					}
				}
			};

			@Override
			public void onInit(View contentView, final PopupWindow dialog) {
				this.dialog = dialog;
				restSearchSuggestHistoryList = SessionManager.getInstance().getListManager().getRestSearchSuggestHistoryList();
				etAutoComplete = (EditText) contentView.findViewById(R.id.index_search_text_view);
				btHistory = (Button) contentView.findViewById(R.id.index_search_right_button);
				btCancel = (Button) contentView.findViewById(R.id.index_search_cancel_button);
				lvAutoComplete = (ListView) contentView.findViewById(R.id.auto_complete_listview);
				lvHistory = (ListView) contentView.findViewById(R.id.auto_complete_history_listview);

				contentView.setPadding(0, 0, 0, 0);
				ViewUtils.setClearable(etAutoComplete);
				// 搜索框事件
				etAutoComplete.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

						// 显示关键字列表
						keyword = etAutoComplete.getText().toString().trim();
						startIndex = 1;
						if (TextUtils.isEmpty(keyword)) {
							// 清空关键字时立即处理
							executeGetSuggestKeywordListTask(keyword);
						} else {
							// 关键字改变后延时一定时间再开始搜索，用户连续较快输入时不重复多次搜索
							mSearchTimestamp.set(System.currentTimeMillis());
							searchHandler.sendMessageDelayed(searchHandler.obtainMessage(0, mSearchTimestamp.get()), 10);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});
				// 设置输入过滤
				etAutoComplete.setFilters(new InputFilter[] { new InputFilter() {
					public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
						if (CheckUtil.isInvalidChar(src.toString())) {
							return "";
						}
						return null;
					}
				} });

				// 分页加载
				lvAutoComplete.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
						if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
							if (isLast == false) {
								// 线程安全且不是最后一页的场合，获得站内信息列表
								executeGetSuggestKeywordListTask(keyword);
							}
						}

					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
						if (firstVisibleItem + visibleItemCount == totalItemCount) {
							// 当到达列表尾部时
							isRefreshFoot = true;
						} else {
							isRefreshFoot = false;

						}

					}
				});
				etAutoComplete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// -----
						OpenPageDataTracer.getInstance().addEvent("餐厅搜索输入框");
						// -----
						goSearchRestSuggest();
					}
				});
				// 历史按钮事件
				btHistory.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// -----
						OpenPageDataTracer.getInstance().addEvent("历史记录按钮");
						// -----
						goSearchHistory();
					}
				});

//				dialog.setOnDismissListener(new OnDismissListener() {
//
//					@Override
//					public void onDismiss() {
//						hideSearchAnimation();
//					}
//				});
				// 取消按钮
				btCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						close();
					}
				});
				adapter = new AutoCompleteRestSuggestAdapter(HotDistrictActivity.this);
				adapter.setList(restSearchSuggestHistoryList, true);

				// --
				lvAutoComplete.setAdapter(adapter);
				lvAutoComplete.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

						int index = arg2;
						List<SuggestResultData> list = ((AutoCompleteRestSuggestAdapter) arg0.getAdapter()).getList();
						SuggestResultData data = list.get(index);

						if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.restId) || String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.restId)) {
							// 历史项或者消息项的场合
							if (data.restName.equals(getString(R.string.text_button_clear))) {
								// 清空历史记录的场合
								SessionManager.getInstance().getListManager().removeAllRestSearchSuggestHistory();
								restSearchSuggestHistoryList.clear();
								adapter.setList(restSearchSuggestHistoryList, true);
							}
						} else if (String.valueOf(Settings.CONTRL_ITEM_HISTORY_ID).equals(data.restId)) {
							// 关键词历史项
							etAutoComplete.setText(data.restName);
							etAutoComplete.setSelection(data.restName.length());

						} else {
							// 保存为搜索关键字为历史
							// 添加入历史搜索
							SuggestResultData historyDto = new SuggestResultData();
							historyDto.restName = keyword;
							SessionManager.getInstance().getListManager().addSearchHistoryInfo(historyDto);
							// ----
							// 选中餐厅
							data.restName = data.restName.replace("<b>", "").replace("</b>", "");
							selectRestDone(data);
						}

					}

				});
				// ----------历史
				adapterHistory = new UsedHistorySuggestListAdapter(HotDistrictActivity.this);
				adapterHistory.setList(null, false);
				lvHistory.setAdapter(adapterHistory);
				lvHistory.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						int index = arg2;
						List<SuggestResultData> list = ((UsedHistorySuggestListAdapter) arg0.getAdapter()).getList();
						SuggestResultData data = list.get(index);

						if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.restId) || String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.restId)) {
							return;
						} else {
							selectRestDone(data);
						}
					}
				});
				// 分页加载
				lvHistory.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
						if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot2) {
							if (isLast2 == false) {
								// 线程安全且不是最后一页的场合，获得站内信息列表
								executeGetUsedHistorySuggestListTask();
							}
						}

					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
						if (firstVisibleItem + visibleItemCount == totalItemCount) {
							// 当到达列表尾部时
							isRefreshFoot2 = true;
						} else {
							isRefreshFoot2 = false;

						}

					}
				});
				contentView.postDelayed(new Runnable() {

					@Override
					public void run() {
						// 初始化
						if ("搜索".equals(type)) {
							goSearchRestSuggest();
						} else {
							goSearchHistory();
						}
					}
				}, 500);
			}

			/**
			 * 获得关键字提示
			 */

			private void executeGetSuggestKeywordListTask(String key) {
				if (CheckUtil.isEmpty(key)) {
					adapter.setList(restSearchSuggestHistoryList, true);

					isTaskSafe = true;
					isLast = true;
					startIndex = 1;
					return;
				}
				// 搜索框文字改变触发搜索，则不控制线程安全，并行发送请求
				// 此种情况可以通过startIndex进行判定
				if (startIndex != 1) {
					if (isTaskSafe) {
						// 线程安全的场合
						if (isLast == false) {
							// 线程安全且不是最后一页的场合，获得餐厅列表
							startIndex = startIndex;
						}
						// 设置线程不安全
						this.isTaskSafe = false;
					} else {
						return;
					}
				}

				if (adapter.isReset) {
					isLast = true;
					startIndex = 1;
				}

				if (getSuggestKeywordListTask != null && !getSuggestKeywordListTask.isCancelled()) {
					// 上一个任务还在运行的场合
					getSuggestKeywordListTask.cancel(true);
				}
				getSuggestKeywordListTask = new GetRestSearchSuggestListTask(HotDistrictActivity.this, startIndex);
				getSuggestKeywordListTask.setKeywords(key);
				getSuggestKeywordListTask.execute(new Runnable() {

					@Override
					public void run() {
						RestSearchSuggestListDTO dto = getSuggestKeywordListTask.dto;
						if (dto != null) {
							String key = etAutoComplete.getText().toString().trim();
							if (CheckUtil.isEmpty(key)) {
								adapter.setList(restSearchSuggestHistoryList, true);
							} else {
								isLast = dto.pgInfo.lastTag;
								startIndex = dto.pgInfo.nextStartIndex;
								adapter.addList(dto.list, isLast);

							}
							if (adapter.getList() != null && adapter.getList().size() > 0 && adapter.isReset) {
								lvAutoComplete.setSelection(0);
							}

						}
						isTaskSafe = true;

					}
				}, new Runnable() {

					@Override
					public void run() {
						isTaskSafe = true;
						isLast = true;
						adapter.addList(new ArrayList<SuggestResultData>(), isLast);
					}
				});
			}

			/**
			 * 获得历史数据
			 */

			private void executeGetUsedHistorySuggestListTask() {
				final GetUsedHistorySuggestListTask task = new GetUsedHistorySuggestListTask(HotDistrictActivity.this);
				task.execute(new Runnable() {

					@Override
					public void run() {
						UsedHistorySuggestListDTO dto = task.dto;
						if (dto != null) {
							isLast2 = dto.pgInfo.lastTag;
							startIndex2 = dto.pgInfo.nextStartIndex;
							adapterHistory.addList(dto.list, isLast2);
						}
						isTaskSafe2 = true;

					}
				}, new Runnable() {

					@Override
					public void run() {
						isTaskSafe2 = true;
						isLast2 = true;
						adapterHistory.addList(new ArrayList<SuggestResultData>(), isLast2);
					}
				});
			}

			/**
			 * 去历史页
			 */
			private void goSearchHistory() {
				// 如果已经是历史了，不执行动作
				if (lvHistory.getVisibility() == View.VISIBLE) {
					return;
				}

				etAutoComplete.postDelayed(new Runnable() {

					@Override
					public void run() {
						InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(etAutoComplete.getWindowToken(), 0);
					}
				}, 150);

				lvHistory.setVisibility(View.VISIBLE);
				lvAutoComplete.setVisibility(View.GONE);
				//
				etAutoComplete.setText("");// 清空搜索数据
				startIndex2 = 1;// 重新开始
				isTaskSafe2 = true;
				isLast2 = true;
				executeGetUsedHistorySuggestListTask();
			}

			/**
			 * 搜索
			 */
			private void goSearchRestSuggest() {

				etAutoComplete.postDelayed(new Runnable() {

					@Override
					public void run() {
						InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.showSoftInput(etAutoComplete, InputMethodManager.SHOW_IMPLICIT);
					}
				}, 150);

				// 如果已经是搜索了，不执行动作
				if (lvAutoComplete.getVisibility() == View.VISIBLE) {
					return;
				}

				//
				lvHistory.setVisibility(View.GONE);
				lvAutoComplete.setVisibility(View.VISIBLE);
				adapterHistory.setList(null, false);
				// ---
				startIndex = 1;// 重新开始
				isTaskSafe = true;
				isLast = true;
			}

			// 选择了一个餐厅
			private void selectRestDone(SuggestResultData data) {

//				setRestOrderInfo(data);
				SessionManager.getInstance().setLastSelectedRest(data);
				close();
			}

			// 关闭窗口
			private void close() {
				InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(etAutoComplete.getWindowToken(), 0);
				dialog.dismiss();
			}
		});
	}

//	private void hideSearchAnimation() {
//
//		contentView.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				contentView.postInvalidate();
//			}
//		}, 100);
//		super.getTitleLayout().startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_in_top));
//		fast_booking_top_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_in_top));
//		fast_booking_bottom_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_in_bottom));
//	}

	/***********************************************************************
	 * 解决单元格按下去的效果，圆角的问题！暂时没有用这种方式，用了以上的那种方式 (假如哪天按下去的效果是 深颜色的，恐怕需要这种方式)
	 ************************************************************************/
	// /**
	// * load the ourselves grid view (为了商区而用)
	// * 每个Item View的Tag中存储的便是dataSources中的每个对象,额外item view 中的Tag 是null
	// * @param <E>
	// * @param parent parent layout
	// * @param dataSources the binding data
	// * @param numColumns the number of column
	// * @param context
	// * @param extraItemText 额外单元格信息,如果extraItemText为null或者为空，则不显示更多信息
	// * @param onItemClickListener
	// */
	// <E> void loadTableLayout(LinearLayout parent,
	// List<E> dataSources,
	// int numColumns,
	// Context context,
	// String extraItemText,
	// OnClickListener onItemClickListener
	// ){
	// int location = 0; // the location of data
	// int numRows = dataSources.size() / numColumns;
	// numRows++;
	//
	// // 这里item宽度的获取比较坑，回头再改进吧
	// int itemWidth = ( UnitUtil.getScreenWidthPixels() - 2 *
	// UnitUtil.dip2px(10) ) / numColumns;
	// // int itemWidth = parent.getWidth() / numColumns;
	// // int itemWidth = UnitUtil.dip2px(75);
	// //160; // 单位px
	// int itemHeight = UnitUtil.dip2px(40);
	// // int itemHeight = UnitUtil.dip2px(40);
	// //80;
	// for (int i = 0; i < numRows; i++) { // add a row
	// LinearLayout ll = new LinearLayout(context);
	// LayoutParams layoutParams = new
	// LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	// ll.setLayoutParams(layoutParams);
	// for(int j = 0; j < numColumns; j++){ // add numColumns columns
	// if(location >= dataSources.size() || dataSources.get(location) == null){
	// if(TextUtils.isEmpty(extraItemText)){
	// break;
	// }
	// Button btMore;
	// if(dataSources.size() % numColumns == 0){ //
	// LinearLayout llMore = new LinearLayout(context);
	// llMore.setLayoutParams(new
	// LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
	//
	// btMore = new Button(context);
	// btMore.setLayoutParams(new LayoutParams(itemWidth * numColumns,
	// itemHeight));
	// btMore.setBackgroundResource(R.drawable.bg_item_district4);
	// llMore.addView(btMore);
	// parent.addView(llMore);
	// }else{
	// btMore = new Button(context);
	// btMore.setBackgroundResource(R.drawable.bg_item_district3);
	// btMore.setLayoutParams(new LayoutParams(itemWidth * (numColumns - j),
	// itemHeight)); // 注意这个宽度的设置
	// ll.addView(btMore);
	// }
	// btMore.setText(extraItemText);
	// btMore.setGravity(Gravity.CENTER);
	// btMore.setPadding(5, 5, 5, 5);
	// btMore.setSingleLine(true);
	// btMore.setTextColor(R.color.text_color_gray);
	// btMore.setOnClickListener(onItemClickListener);
	// break;
	// }
	// Button btItem = new Button(context);
	// btItem.setLayoutParams(new LayoutParams(itemWidth, itemHeight));
	// // 控制item的背景
	// if(i == 0 && j == 0){
	// btItem.setBackgroundResource(R.drawable.bg_item_district0);
	// }else if(i == 0 && j == numColumns - 1){
	// btItem.setBackgroundResource(R.drawable.bg_item_district1);
	// }else if(i == numRows - 1 && j == 0){
	// btItem.setBackgroundResource(R.drawable.bg_item_district2);
	// }else if(i == numRows - 1 && j == numColumns - 1){
	// btItem.setBackgroundResource(R.drawable.bg_item_district3);
	// }else{
	// btItem.setBackgroundResource(R.drawable.bg_item_district5);
	// }
	// btItem.setText(dataSources.get(location).toString());
	// btItem.setGravity(Gravity.CENTER);
	// btItem.setSingleLine(true);
	// btItem.setPadding(5, 5, 5, 5);
	// btItem.setTag(dataSources.get(location));
	// btItem.setOnClickListener(onItemClickListener);
	// ll.addView(btItem);
	// if(j + 1 != numColumns){ // control vertical line
	// View verticalLine = new View(context);
	// verticalLine.setLayoutParams(new LayoutParams(1, itemHeight));
	// verticalLine.setBackgroundResource(R.color.border_color_gray);
	// ll.addView(verticalLine);
	// }
	// location ++ ;
	//
	// }
	//
	// parent.addView(ll); //
	//
	// if( i + 1 != numRows
	// ||(dataSources.size() % numColumns == 0 &&
	// !TextUtils.isEmpty(extraItemText))){ // control transverse line
	// Log.e("transverseLine", i + "----" + numRows);
	// View transverseLine = new View(context);
	// transverseLine.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
	// 1));
	// transverseLine.setBackgroundResource(R.color.border_color_gray);
	// parent.addView(transverseLine);
	// }
	//
	// // if(i + 1 == numRows && dataSources.size() % numColumns == 0 &&
	// !TextUtils.isEmpty(extraItemText)){
	// // Log.e("transverseLine", i + "----" + numRows);
	// // View transverseLine = new View(context);
	// // transverseLine.setLayoutParams(new
	// LayoutParams(LayoutParams.FILL_PARENT, 1));
	// // transverseLine.setBackgroundResource(R.color.border_color_gray);
	// // parent.addView(transverseLine);
	// // }
	// }
	//
	// }

	// 构造测试数据
	void setTextData() {
		// final List<String> list1 = new ArrayList<String>();
		// // int size = 28;
		// // int size = 29;
		// // int size = 30;
		// int size = 31;
		// // int size = 32;
		// for (int i = 0; i < size; i++) {
		// list1.add("人民广场");
		// }
		// // list1.add("淮海路");
		// // list1.add("人民广场");
		// // list1.add("陆家嘴");
		// // list1.add("徐家汇");
		// // list1.add("南京西路");
		// // list1.add("五角场/大学区");
		// // list1.add("中山公园");
		// // list1.add("静安寺");
		// // list1.add("八佰伴");
		// // list1.add("打浦桥");
		// // list1.add("南京东路");
		// // list1.add("万体馆");
		// // list1.add("虹桥");
		// // list1.add("莘庄");
		// // list1.add("上海火车站");
		// // list1.add("佘山");
		// // list1.add("长寿路");
		// // list1.add("天山");
		// // list1.add("安亭");
		// // list1.add("北外滩");
		// // list1.add("漕河泾/田林");
		// // list1.add("城隍庙");
		// // list1.add("奉贤");
		//
		// String extraItemText = "更多商圈...";
		// loadTableLayout(hotDistrict, list1, 4, this, extraItemText, new
		// View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// DialogUtil.showToast(HotDistrictActivity.this,
		// ((Button)v).getText().toString());
		// }
		// });
		//
		// loadTableLayout(latelyBrowseDistrict, list1, 4, this, null, new
		// View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// DialogUtil.showToast(HotDistrictActivity.this,
		// ((Button)v).getText().toString());
		// }
		// });
		//
		// tvHotDistrict.setVisibility(View.VISIBLE);
		// tvLatelyBrowse.setVisibility(View.VISIBLE);
	}
}
