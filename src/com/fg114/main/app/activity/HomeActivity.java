package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.Mdb.MdbResListActivity;
import com.fg114.main.app.activity.mealcombo.MealComboListActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.activity.order.MyShortMessageOrderListActivity;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.activity.resandfood.RecommandRestaurantSubmitActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;
import com.fg114.main.app.activity.resandfood.RestaurantImageDetailOfFoodActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.top.TopListActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.adapter.AdvertisementAdapter;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.AutoCompleteRestSuggestAdapter;
import com.fg114.main.app.adapter.IndexWaterFallAdapter;
import com.fg114.main.app.adapter.RecommendRestAdapter;
import com.fg114.main.app.adapter.RestListAdapter;
import com.fg114.main.app.adapter.RestaurantSearchAdapter;
import com.fg114.main.app.adapter.UsedHistorySuggestListAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.MainMenuListInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.DragLoadingView;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MyWaterFallView;
import com.fg114.main.app.view.OrderSelectionWheelView;
import com.fg114.main.app.view.SelectionListView;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.app.view.WaterFallScrollView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.app.view.WaterFallBaseView.onWaterFallClickListener;
import com.fg114.main.app.view.WaterFallScrollView.WaterFallOption;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.service.dto.ChatMsgChkData;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CityListDTO;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MainPageInfoPack4DTO;
import com.fg114.main.service.dto.MainPageInfoPackDTO;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.OrderHintPackData;
import com.fg114.main.service.dto.PgInfo;
import com.fg114.main.service.dto.RestListDTO;
import com.fg114.main.service.dto.RestListData;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.service.dto.PushMsgDTO;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.service.dto.RestPicListDTO;
import com.fg114.main.service.dto.RestRecomListDTO;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.service.dto.RestSearchSuggestListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.SoftwareCommonData;
import com.fg114.main.service.dto.SuggestResultData;
import com.fg114.main.service.dto.UsedHistorySuggestListDTO;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.GetNewResPicTask;
import com.fg114.main.service.task.GetRestSearchSuggestListTask;
import com.fg114.main.service.task.GetUsedHistorySuggestListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.Rotate3dAnimation;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 主页
 * 
 * @author zhangyifan
 * 
 */
public class HomeActivity extends MainFrameActivity {

	private static final boolean debug = false;
	private static final String TAG = "HomeActivity";
	private static final boolean DEBUG = Settings.DEBUG;
	public static final int CAMERAIMAGE = 9999; // 拍照上传
	public static final int LOCALIMAGE = 9998; // 本地上传
	// 进度提示框
	private static ProgressDialog progressDialog = null;
	// 本地缓存数据
	private MainMenuListInfo mainMenuListInfo;
	private CityInfo cityInfo;
	private long popTimeStamp;
	private long dialogTimeStamp;
	// private RestListDTO restListDTO;
	// 城市变化控制变量--------------
	private String cityId;
	// -----------------------------
	// 缓存数据
	private Filter filter; // 查询条件

	// 内存变量
	// private PopReadyBroadcast mBroadcast = new PopReadyBroadcast();
	private List<View> buttonList = new ArrayList<View>();

	// 广告组件
	private ViewFlow advViewFlow;
	private CircleFlowIndicator advCircleIndicator;
	// private ImageView advCloseButton;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。

	// --
	private ViewGroup mVgSearch;
	private ViewGroup mBtVoice;

	// 根据位置更新城市线程
	private Thread mUpdateCityThread;
	private AsyncTask<Void, Void, Integer> task;

	// 拍照上传保存路径
	private Uri takePhotoUri;
	// ---
	private Handler mhander = new Handler();
	private boolean First = true;

	// ------------------------------------------
	private static final String TAG_TYPE_SORT = "sort";
	private static final String TAG_TYPE_AVG = "avg";
	private int mSelectedDistance;
	private int mSelectedSort;
	private int mSelectedRegion;
	private int mSelectedDistrict;
	private int mSelectedMainMenu;
	private int mSelectedSubMenu;
	private int mSelectedMainTop;
	private int mSelectedSubTop;

	private LinearLayout dropdownAnchor;
	private Button btFirst;
	private Button btChannel;
	private Button btSort;
	private List<RfTypeDTO> mDistanceList = new ArrayList<RfTypeDTO>();
	private List<RfTypeDTO> mSortList = new ArrayList<RfTypeDTO>();
	private List<RfTypeListDTO> mAreaList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeListDTO> mChannelList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeListDTO> mTopList = new ArrayList<RfTypeListDTO>();

	// ---
	private ViewGroup index_top_bar;
	private View switchButton1;
	private ImageView switch_button1_iv;
	private TextView switch_button1_tv;
	private View switchButton2;
	private ImageView switch_button2_iv;
	private TextView switch_button2_tv;
	private View recommendButton;
	private ListView top_list_view;
	private RecommendRestAdapter topRestAdapter;
	private IndexWaterFallAdapter waterFallAdapter;
	// private ViewGroup switch_button_layout;
	private ViewGroup index_3d_layout;
	private MyWaterFallView waterfall_view;
	private ViewGroup index_rest_select;
	private ViewGroup recommend_button_layout;
	private ViewGroup index_listview_layout;
	// private TextView mpTitle;
	// private TextView mpHint;
	private ViewGroup waterfall_loading;
	private ViewGroup index_top_container;
	// ------------------------------------------
	private boolean haveGpsTag;
	private View contentView;
	private ListView restListView; // 最新
	private ListView restTopListView; // 精选
	private Boolean isRestTopList;// 是否是精选
	// ---
	private DragLoadingView dragview_rest_list;
	private DragLoadingView dragview_top_rest_list;
	private DragLoadingView dragview_recom_rest_list;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("首页", "");
		// ----------------------------
		
		contentView = View.inflate(this, R.layout.home, null);
		// this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT);
		setContentView(contentView);
		// 获得缓存数据
		filter = SessionManager.getInstance().getFilter();

		// 获取屏幕的宽高属性 ，标题栏和状态栏高度
		try {

			// 缓存数据获得
			cityInfo = SessionManager.getInstance().getCityInfo(this);
			popTimeStamp = SharedprefUtil.getLong(this, Settings.POP_TIME_STAMP, 0);
			dialogTimeStamp = SharedprefUtil.getLong(this, Settings.DIALOG_TIME_STAMP, 0);

			// 获得位置
			haveGpsTag = Loc.isGpsAvailable();
			if (!haveGpsTag) {
				// 没有定位的场合，提示打开
				DialogUtil.showAlert(this, true, getString(R.string.text_dialog_goto_open_gps), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityUtil.gotoSysSetting(HomeActivity.this);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消
						dialog.cancel();
					}
				});
			}

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
				Bundle bund = new Bundle();
				bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
				ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("首页", "");
		// ----------------------------
	}


	/**
	 * 主页的数据初始化
	 */

	private void initIndexDataByMainPageInfoPack() {

		// 初始化数据---------------------------------------
		index_rest_select.setVisibility(View.GONE);
		index_listview_layout.setVisibility(View.GONE);
		recommend_button_layout.setVisibility(View.GONE);
		index_3d_layout.setVisibility(View.GONE);

		wfPageStatus = new PageStatusManager(); // 瀑布流页状态
		topPageStatus = new PageStatusManager(); // 精选页状态
		topRestAdapter.setList(null, false);
		// 瀑布流
		waterFallAdapter = new IndexWaterFallAdapter(this);
		waterfall_view.setAdapter(waterFallAdapter);
		waterfall_view.setOnScrollListener(wfScrollListener);

		// ------------------------------------------------
		contentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				getMainPageData();
			}
		}, 100);

	}

	void getMainPageData() {

		final MainPageInfoPack4DTO data = SessionManager.getInstance().getMainPageInfoPackDTO();
		boolean isFirst = SharedprefUtil.getBoolean(this, Settings.IS_FRIST, true);
		//
		ServiceRequest request = new ServiceRequest(API.getMainPageInfoPack4);
		if (data.mainPageMsgListDTO != null) {
			request.addData("advTimestamp", data.mainPageMsgListDTO.timestamp);
		} else {
			request.addData("advTimestamp", 0);
		}
		request.addData("pageSize", 20);
		request.addData("firstQueryTag", isFirst);
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		setButtonState(false);
		// ------
		CommonTask.request(request, "正在获取主页数据...", new CommonTask.TaskListener<MainPageInfoPack4DTO>() {

			@Override
			protected void onSuccess(MainPageInfoPack4DTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 提取3个列表的第一页数据
				if (dto.recomStyleTag) {// 是推荐餐馆样式
					index_rest_select.setVisibility(View.GONE);
					index_listview_layout.setVisibility(View.GONE);
					recommend_button_layout.setVisibility(View.VISIBLE);
					index_3d_layout.setVisibility(View.VISIBLE);
					// --
					// mpTitle.setText(dto.restRecomAddHintData.mpTitle);
					// mpHint.setText(dto.restRecomAddHintData.mpHint);

					// restListDTO=dto.restListDTO;

					boolean isFirstHint = SharedprefUtil.getBoolean(HomeActivity.this, Settings.IS_FRIST_SHOW_HINT_BUTTON, true);
					// 按钮
					// if
					// (!CheckUtil.isEmpty(dto.restRecomAddHintData.mpTopBtnName))
					// {
					// if(!isFirstHint){
					// switchButton1.setText(dto.restRecomAddHintData.mpTopBtnName);
					// // 精选
					// }
					// switchButton1.setTag(dto.restRecomAddHintData.mpTopBtnName);
					// }else{
					if (!isFirstHint) {
						// switchButton1.setText("精选"); // 精选
						isRestTopList = true;

						switch_button1_iv.setBackgroundResource(R.drawable.home_top_1_red);
						switch_button1_tv.setTextColor(getResources().getColor(R.color.text_color_black));
						switch_button2_iv.setBackgroundResource(R.drawable.home_top_2_gray);
						switch_button2_tv.setTextColor(getResources().getColor(R.color.text_color_gray));
					}

					switchButton1.setTag("精选");
					// }
					// if
					// (!CheckUtil.isEmpty(dto.restRecomAddHintData.mpNewBtnName))
					// {
					// switchButton2.setText(dto.restRecomAddHintData.mpNewBtnName);
					// // 推荐
					// }
					// 更新下缓存里的列表数据
					SessionManager.getInstance().setMainPageInfoPackDTO(dto);
					excuteOrderHintInfoTask();
					// 精选餐馆推荐列表
					executeGetTopRestRecomList(false);
					// 瀑布流餐馆推荐列表
					executeGetRestRecomWaterFallList(false);
				} else {
					index_rest_select.setVisibility(View.VISIBLE);
					index_listview_layout.setVisibility(View.VISIBLE);
					recommend_button_layout.setVisibility(View.GONE);
					index_3d_layout.setVisibility(View.GONE);
					executeGetRestList(false);
				}
			}

			@Override
			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// DialogUtil.showAlert(HomeActivity.this, false, "提示",
				// "获取数据失败！", "重试", "", new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// initIndexDataByMainPageInfoPack();
				// }
				// });

				DialogUtil.showAlert(HomeActivity.this, false, true, "提示", "获取数据失败！", "重试", "", new DialogInterface.OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// 如果是返回 那么弹出dialog询问是否要退出
						if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
							finish();
						}
						return false;
					}

				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						initIndexDataByMainPageInfoPack();
					}
				});

			}

		});
	}

	public static void excuteOrderHintInfoTask(){		
		final MainPageOtherInfoPackDTO data = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
		ServiceRequest request = new ServiceRequest(API.getMainPageOtherInfoPack);
		if (data.orderHintPackData != null) {
			request.addData("orderHintTimestamp", data.orderHintPackData.timestamp);
		} else {
			request.addData("orderHintTimestamp", 0);
		}
		CommonTask.requestMutely(request, new CommonTask.TaskListener<MainPageOtherInfoPackDTO>() {

			@Override
			protected void onSuccess(MainPageOtherInfoPackDTO dto) {
				SessionManager.getInstance().setMainPageOtherInfoPackDTO(data);
				CommonObservable.getInstance().notifyObservers(CommonObserver.SystemMessageObserver.class);
			}

			@Override
			protected void onError(int code, String message) {
//				doTest();
			}

			void doTest() {
				String json = "{\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\"false\"},\"list\":[{\"uuid\":\"1233\",\"name\":\"天府大萝卜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521264\",\"latitude\":\"31.239977\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜2222\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜道德\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜似懂非懂是\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"}],\"typeList\":[{\"uuid\":\"\",\"name\":\"全部\"},{\"uuid\":\"12\",\"name\":\"好吃\"},{\"uuid\":\"23\",\"name\":\"很好\"},{\"uuid\":\"34\",\"name\":\"凉菜好吃\"},{\"uuid\":\"45\",\"name\":\"热茶\"},{\"uuid\":\"56\",\"name\":\"酒水\"},{\"uuid\":\"57\",\"name\":\"不错的\"}]}";
				// TakeoutRestListDTO dto=JsonUtils.fromJson(json,
				// TakeoutRestListDTO.class);
				// onSuccess(dto);
			}
		});
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	@Override
	protected void onCityChanged() {
		super.onCityChanged();
		initCityIssues();
		// initIndexDataByMainPageInfoPack();
	}

	private boolean isInited = false;

	@Override
	public void onResume() {
		if (!isInited) {
			initIndexDataByMainPageInfoPack();
			isInited = true;
		}

		
		super.onResume();
//		KeepAliveService.bindBaiduPush();
		Settings.CURRENT_PAGE = getClass().getSimpleName();

		// 重设查询条件
		SessionManager.getInstance().getFilter().reset();
		SessionManager.getInstance().getRealTimeResFilter().reset();
		initCityIssues();
		// 开新线程清除本地缓存中的过期缓存
		Thread clearCache = new Thread(new Runnable() {
			@Override
			public void run() {
				ValueCacheUtil.getInstance(ContextUtil.getContext()).cleanCache();
				FileCacheUtil.getInstance().maintain();
			}
		});
		clearCache.start();

		// 显示广告
		tryDisplayAdvertisement();
		dealCaptureEvent();// 所有的随手拍都通过此方法判断是否转到选择餐厅

		// //第一次进入时的朦皮，只在有语音预订的时候显示（例如只在上海）
		// DialogUtil.showVeilPictureOnce(this,
		// R.drawable.mask_index,"ShowOnceVeil_IndexActivity");
		// ----------------------

	}

	// 广告是否是被主动关闭过
	private boolean hasAdvertisementBeenClosed() {
		// 通过判断是否是同一天来控制广告位的显隐
		long timeStamp = SessionManager.getInstance().getAdvCloseTimeStamp(HomeActivity.this);
		if (!CalendarUtil.isToday(timeStamp)) {
			return false;
		} else {
			return true;
		}

	}

	private synchronized void tryDisplayAdvertisement() {
		List<MainPageAdvData> advList = SessionManager.getInstance().getMainPageAdvDataList();
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0 && !hasAdvertisementBeenClosed()) {
			if (advList.size() == 1) {
				advCircleIndicator.setVisibility(View.GONE);
			} else {
				advCircleIndicator.setVisibility(View.VISIBLE);
			}

			// 确保只有一个运行的线程
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			// ---------------------
			advViewFlow.setAdapter(new AdvertisementAdapter(this, advList));
			// 广告自动滚动的线程，４秒
			playAdvertisement = new Thread(new Runnable() {
				int i = 0;

				@Override
				public void run() {
					try {
						int count = advViewFlow.getAdapter().getCount();
						while (count > 1) {
							Thread.sleep(4000);
							if (playCoolingTime > System.currentTimeMillis()) {
								continue;
							}
							i = advViewFlow.getSelectedItemPosition();
							i = (i + 1) % count;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									advViewFlow.setSelection(i);

								}
							});
							count = advViewFlow.getAdapter().getCount();
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			});
			playAdvertisement.start();
			// 广告手动滑动
			advViewFlow.setOnTouchListener(advTouchListener);
			// --
		} else {
			// 没有广告时，撤消线程，清除数据
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			BaseAdapter adapter = new AdvertisementAdapter(this, new ArrayList<MainPageAdvData>());
			advViewFlow.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	// 控制自动播放的手势
	OnTouchListener advTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				playCoolingTime = System.currentTimeMillis() + 2000; // 马上冷却
			} else {
				playCoolingTime = System.currentTimeMillis() + 200000; // 几乎不冷却　
			}
			return false;
		}
	};

	@Override
	public void finish() {
		super.finish();
		if (mUpdateCityThread != null) {
			mUpdateCityThread.interrupt();
		}

		Settings.CURRENT_PAGE = "";
		// 页面跳转过程平滑的滑动
		// overridePendingTransition(R.anim.left_slide_in,
		// R.anim.left_slide_out);
		ActivityUtil.overridePendingTransition(this, R.anim.left_slide_in, R.anim.left_slide_out);
	}

	/**
	 * 初始化界面
	 */
	private void initComponent() {

		getTitleLayout().setVisibility(View.GONE);
		index_top_container = (ViewGroup) findViewById(R.id.index_top_container);
		index_top_bar = (ViewGroup) findViewById(R.id.index_top_bar);
		mVgSearch = (ViewGroup) findViewById(R.id.button_search_rest);
		mBtVoice = (ViewGroup) findViewById(R.id.button_voice_search_rest);
		// ------------首页广告-----------------------------------------------------------------------
		advViewFlow = (ViewFlow) findViewById(R.id.viewflow);
		advCircleIndicator = (CircleFlowIndicator) findViewById(R.id.circle_indicator);
		advViewFlow.setFlowIndicator(advCircleIndicator);
		// 筛选控件
		dropdownAnchor = (LinearLayout) findViewById(R.id.index_rest_select);
		btFirst = (Button) findViewById(R.id.index_first_button);
		btChannel = (Button) findViewById(R.id.index_menu_button);
		btSort = (Button) findViewById(R.id.index_third_button);
		restListView = (ListView) findViewById(R.id.index_listview);

		// -------------------------------------------------
		// switch_button_layout = (ViewGroup)
		// findViewById(R.id.switch_button_layout);
		switchButton1 = (View) findViewById(R.id.switch_button1);
		switch_button1_iv = (ImageView) findViewById(R.id.switch_button1_iv);
		switch_button1_tv = (TextView) findViewById(R.id.switch_button1_tv);
		switchButton2 = (View) findViewById(R.id.switch_button2);
		switch_button2_iv = (ImageView) findViewById(R.id.switch_button2_iv);
		switch_button2_tv = (TextView) findViewById(R.id.switch_button2_tv);
		// mpTitle = (TextView) findViewById(R.id.mpTitle);
		// mpHint = (TextView) findViewById(R.id.mpHint);
		//
		index_3d_layout = (ViewGroup) findViewById(R.id.index_3d_layout);
		top_list_view = (ListView) findViewById(R.id.top_list_view);

		waterfall_loading = (ViewGroup) findViewById(R.id.res_pic_loading);
		waterfall_view = (MyWaterFallView) findViewById(R.id.waterfall_view);

		recommendButton = (View) findViewById(R.id.recommend_button);
		// --------------------------------------------------
		index_listview_layout = (ViewGroup) findViewById(R.id.index_listview_layout);
		recommend_button_layout = (ViewGroup) findViewById(R.id.recommend_button_layout);
		index_rest_select = (ViewGroup) findViewById(R.id.index_rest_select); // 筛选条件
		// ---------------------------------------------------
		// 拖动加载，餐厅列表
		dragview_rest_list = (DragLoadingView) findViewById(R.id.dragview_rest_list);
		// 拖动加载，精选餐厅列表
		dragview_top_rest_list = (DragLoadingView) findViewById(R.id.dragview_top_rest_list);
		// 拖动加载，推荐餐厅列表
		dragview_recom_rest_list = (DragLoadingView) findViewById(R.id.dragview_recom_rest_list);

		setupDragViews();
		// ----------------------------------------------------
		// 逻辑：第一次，switchButton1要使用提示图片，以后就默认图片
		boolean isFirstHint = SharedprefUtil.getBoolean(this, Settings.IS_FRIST_SHOW_HINT_BUTTON, true);
		if (isFirstHint) {
			isRestTopList = true;
			switch_button1_iv.setBackgroundResource(R.drawable.home_top_1_red);
			switch_button1_tv.setTextColor(getResources().getColor(R.color.text_color_black));
			switch_button2_iv.setBackgroundResource(R.drawable.home_top_2_gray);
			switch_button2_tv.setTextColor(getResources().getColor(R.color.text_color_gray));
			// switchButton1.setBackgroundResource(R.drawable.bg_button_switch_hint);
			// switchButton1.setText("");
		}
		switchButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("精选荐店按钮");
				// -----
				SharedprefUtil.saveBoolean(HomeActivity.this, Settings.IS_FRIST_SHOW_HINT_BUTTON, false);
				// switchButton1.setBackgroundResource(R.drawable.bg_button_switch);
				// switchButton1.setText((String)switchButton1.getTag());

				// applyRotation(switch_button_layout, 0, 90);

				switch_button1_iv.setBackgroundResource(R.drawable.home_top_1_red);
				switch_button1_tv.setTextColor(getResources().getColor(R.color.text_color_black));
				switch_button2_iv.setBackgroundResource(R.drawable.home_top_2_gray);
				switch_button2_tv.setTextColor(getResources().getColor(R.color.text_color_gray));

				if (!isRestTopList) {
					applyRotation(index_3d_layout, 0, 90);
					isRestTopList = true;
				}
			}
		});
		switchButton2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("最新荐店按钮");
				// -----
				// applyRotation(switch_button_layout, 0, -90);
				switch_button1_iv.setBackgroundResource(R.drawable.home_top_1_gray);
				switch_button1_tv.setTextColor(getResources().getColor(R.color.text_color_gray));
				switch_button2_iv.setBackgroundResource(R.drawable.home_top_2_red);
				switch_button2_tv.setTextColor(getResources().getColor(R.color.text_color_black));

				if (isRestTopList) {
					applyRotation(index_3d_layout, 0, -90);
					isRestTopList = false;
				}
			}
		});

		// 初始化瀑布流容器
		// initWaterFallLayout();
		// --------------------------------------------------
		topRestAdapter = new RecommendRestAdapter(HomeActivity.this, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeGetTopRestRecomList(false);
			}
		});

		topRestAdapter.setList(null, false);
		top_list_view.setAdapter(topRestAdapter);
		top_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<RestRecomPicData> list = ((RecommendRestAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					RestRecomPicData data = list.get(index);
					if (data != null && !("" + Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
						jumpToRestRecommendInfo(data);
					}
				}
			}

		});
		top_list_view.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----
				}
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && topPageStatus.isRefreshFoot) {
					Log.v("TAG", topPageStatus.isLast+"");
					if (topPageStatus.isLast == false) {
						// 线程安全且不是最后一页的场合
						executeGetTopRestRecomList(false);
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					// 当到达列表尾部时
					topPageStatus.isRefreshFoot = true;
				} else {
					topPageStatus.isRefreshFoot = false;
				}
			}
		});

		recommendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我要荐店按钮");
				// -----
				ActivityUtil.jump(HomeActivity.this, RecommandRestaurantSubmitActivity.class, 0, new Bundle());
			}
		});
		// ------------------------------------------------------------------------
		btFirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("地域下拉框");
				// -----
				showFirstFilter();
			}
		});

		btChannel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("菜系下拉框");
				// -----
				showChannelFilter();
			}
		});

		btSort.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("排序下拉框");
				// -----
				showSortFilter();
			}
		});
		advViewFlow.setAdapter(new AdvertisementAdapter(this, new ArrayList<MainPageAdvData>()));

		// 初始化搜索框
		mVgSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("关键词搜索输入框");
				// -----
				AutoCompleteAdapter.isRecomRest = false;
				ActivityUtil.jump(HomeActivity.this, AutoCompleteActivity.class, 0, new Bundle());
				overridePendingTransition(R.anim.activity_enter, R.anim.activity_enter);
			}
		});

		mBtVoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.jump(HomeActivity.this, MdbResListActivity.class, 0,new Bundle());		
//				ActivityUtil.showVoiceDialogForSearch(HomeActivity.this, 1, new ActivityUtil.OnRecognizedFinishListener() {
//
//					@Override
//					public void onRecognizedFinish(String text) {
//						// -----
//						OpenPageDataTracer.getInstance().addEvent("语音按钮", text);
//						// -----
//						
//									
//						
//						Bundle bundle = new Bundle();
//						bundle.putString(Settings.BUNDLE_KEY_KEYWORD, text);
//						AutoCompleteActivity.voiceInputTag = 1;
//						ActivityUtil.jump(HomeActivity.this, AutoCompleteActivity.class, 0, bundle);
//					}
//				});
			}
		});

		/**
		 * listview操作
		 */
		restListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int index = position;
				RestListData restData = (RestListData) ((HeaderViewListAdapter) parent.getAdapter()).getItem(index);
				if (restData != null) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("选择行", restData.restId);
					// -----
					// 去餐厅详细页面
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, restData.restId);
//					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "返回");
//					String[] nameAndLogoUrl = { restData.restName, restData.picUrl };
//					bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
					bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
					ActivityUtil.jump(HomeActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
				}
			}
		});

		restListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		// ((ViewGroup)contentView).bringChildToFront(index_top_container);

	}

	private void setupDragViews() {

		// 普通餐厅列表
		dragview_rest_list.setDragLoadingListener(new DragLoadingView.DragLoadingListener() {

			@Override
			public void onDragReleased() {
				// Toast.makeText(HomeActivity.this, "普通加载开始了！",
				// Toast.LENGTH_LONG).show();
				// -----
				OpenPageDataTracer.getInstance().addEvent("下拉刷新");
				// -----
				executeGetRestList(true);
			}

			@Override
			public boolean isAllowDrag() {

				if (restListView == null || restListView.getChildCount() <= 0) {
					return false;
				}
				View v = restListView.getChildAt(0);
				Rect r = new Rect();
				boolean allow = v.getLocalVisibleRect(r);
				if (r.top == 0) {
					return true;
				}
				return false;
			}
		});
		// 精选餐厅列表
		dragview_top_rest_list.setDragLoadingListener(new DragLoadingView.DragLoadingListener() {

			@Override
			public void onDragReleased() {
				// Toast.makeText(HomeActivity.this, "精选餐厅加载开始了！",
				// Toast.LENGTH_LONG).show();
				// -----
				OpenPageDataTracer.getInstance().addEvent("下拉刷新");
				// -----
				topPageStatus = new PageStatusManager(); // 精选页状态重置
				executeGetTopRestRecomList(true);
			}

			@Override
			public boolean isAllowDrag() {

				if (top_list_view == null) {
					return false;
				}
				if (top_list_view.getChildCount() <= 0) {
					return true;
				}
				View v = top_list_view.getChildAt(0);
				Rect r = new Rect();
				boolean allow = v.getLocalVisibleRect(r);
				if (r.top == 0) {
					return true;
				}
				return false;
			}
		});
		// 推荐餐厅列表
		dragview_recom_rest_list.setDragLoadingListener(new DragLoadingView.DragLoadingListener() {

			@Override
			public void onDragReleased() {
				// -----
				OpenPageDataTracer.getInstance().addEvent("下拉刷新");
				// -----
				// Toast.makeText(HomeActivity.this, "推荐餐厅加载开始了！",
				// Toast.LENGTH_LONG).show();
				wfPageStatus = new PageStatusManager(); // 瀑布流页状态重置
				executeGetRestRecomWaterFallList(true);
			}

			@Override
			public boolean isAllowDrag() {

				if (waterfall_view == null) {
					return false;
				}
				if (waterfall_view.getScrollY() == 0) {
					return true;
				}
				return false;
			}
		});

	}

	private void jumpToRestRecommendInfo(RestRecomPicData data) {
		// -----
		OpenPageDataTracer.getInstance().addEvent("选择行", data.uuid);
		// -----
		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_REST_ID, data.uuid);
		bundle.putInt(Settings.BUNDLE_showTypeTag, 2);
		// bundle.putString(Settings.BUNDLE_KEY_ID,restId);
		ActivityUtil.jump(HomeActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
	}

	// --------------------------------------------------------------------------------------------
	class PageStatusManager {
		boolean isRefreshFoot = false;
		boolean isLast = false;
		boolean isTaskSafe = true;
		int startIndex = 1;

	}

	// --
	PageStatusManager wfPageStatus = new PageStatusManager(); // 瀑布流页状态
	PageStatusManager topPageStatus = new PageStatusManager(); // 精选页状态
	MyWaterFallView.OnScrollListener wfScrollListener = new MyWaterFallView.OnScrollListener() {

		@Override
		public void onScroll() {
		}

		@Override
		public void onBottom() {
			// -----
			OpenPageDataTracer.getInstance().addEvent("滚动");
			// -----
			if (!wfPageStatus.isLast) {
				executeGetRestRecomWaterFallList(false);
			}
		}
	};

	/**
	 * 最新推荐餐厅瀑布流列表
	 */
	private void executeGetRestRecomWaterFallList(final boolean isRefresh) {

		if (wfPageStatus.isTaskSafe) {
			// 线程安全的场合
			if (wfPageStatus.isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
			}
			// 设置线程不安全
			// setChildEnable(false);
			wfPageStatus.isTaskSafe = false;

		} else {
			return;
		}

		// 如果是第1页，从MainPageInfoPackDTO里取
		final MainPageInfoPack4DTO data = SessionManager.getInstance().getMainPageInfoPackDTO();
		// 如果是第1页，直接从MainpageInfoPack中读取
		if (!isRefresh && wfPageStatus.startIndex == 1 && data.restRecomListDTO != null && data.restRecomListDTO.list != null && data.restRecomListDTO.list.size() > 0) {
			loadDataToRecomRestList(data.restRecomListDTO, false);
			return;
		}
		waterfall_loading.setVisibility(View.VISIBLE);
		// ------
		ServiceRequest request = new ServiceRequest(API.getRestRecomList2);
		request.addData("topTag", false);// 是否是精选
		request.addData("restId", "");// 餐厅id 可以为空
		request.addData("pageSize", 20);// 页面大小
		request.addData("startIndex", wfPageStatus.startIndex);// 当前页
		// -----
		OpenPageDataTracer.getInstance().addEvent("列表查询", "推荐列表,false");
		// -----
		CommonTask.request(request, "", new CommonTask.TaskListener<RestRecomListDTO>() { // RestRecomPicData
					@Override
					protected void onSuccess(RestRecomListDTO dto) {
						waterfall_loading.setVisibility(View.GONE);
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						loadDataToRecomRestList(dto, isRefresh);
						dragview_recom_rest_list.reset();
					}

					@Override
					protected void onError(int code, String message) {
						waterfall_loading.setVisibility(View.GONE);
						super.onError(code, message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						dragview_recom_rest_list.reset();
						// 设置线程安全
						wfPageStatus.isTaskSafe = true;
					}

				});
	}

	/**
	 * 精选餐厅列表
	 */
	private void executeGetTopRestRecomList(final boolean isRefresh) {

		if (topPageStatus.isTaskSafe) {
			// 线程安全的场合
			if (topPageStatus.isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
			}
			// 设置线程不安全
			// setChildEnable(false);
			topPageStatus.isTaskSafe = false;

		} else {
			return;
		}

		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		// 如果是第1页，从MainPageInfoPackDTO里取
		MainPageInfoPack4DTO data = SessionManager.getInstance().getMainPageInfoPackDTO();
		// 如果是第1页，直接从MainpageInfoPack中读取
		if (!isRefresh && topPageStatus.startIndex == 1 && data.topRestRecomListDTO != null && data.topRestRecomListDTO.list != null && data.topRestRecomListDTO.list.size() > 0) {
			loadDataToTopRestList(data.topRestRecomListDTO, false);
			return;
		}
		//
		ServiceRequest request = new ServiceRequest(API.getRestRecomList2);
		request.addData("topTag", true);// 是否是精选
		request.addData("pageSize", 20);// 页面大小
		request.addData("restId", "");// 餐厅id 可以为空
		request.addData("startIndex", topPageStatus.startIndex);// 当前页
		// -----
		OpenPageDataTracer.getInstance().addEvent("列表查询", "推荐列表,true");
		// -----

		CommonTask.request(request, "", new CommonTask.TaskListener<RestRecomListDTO>() { // RestRecomPicData
					@Override
					protected void onSuccess(RestRecomListDTO dto) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						loadDataToTopRestList(dto, isRefresh);
						dragview_top_rest_list.reset();
					}

					@Override
					protected void onError(int code, String message) {
						super.onError(code, message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						// 设置线程安全
						
						topPageStatus.isTaskSafe = true;
						topPageStatus.isLast = false;
						dragview_top_rest_list.reset();
						topRestAdapter.addList(new ArrayList<RestRecomPicData>(), true);
					}

				});
	}
	

	/*************************************************************/
	ListViewAdapter<RestListData> adapter;

	// ---------------------------------------------------------------------------------------------
	private void executeGetRestList(final boolean isRefresh) {

		adapter = new ListViewAdapter<RestListData>(R.layout.list_item_index, new ListViewAdapter.OnAdapterListener<RestListData>() {
			@Override
			public void onLoadPage(final ListViewAdapter<RestListData> adapter, final int startIndex, int pageSize) {

				MainPageInfoPack4DTO data = SessionManager.getInstance().getMainPageInfoPackDTO();
				// 如果是第1页，直接从MainpageInfoPack中读取
				if (!isRefresh && startIndex == 1 && data.restListDTO != null) {
					loadDataToRestList(adapter, startIndex, data.restListDTO);
					return;
				}
				// 否则是其他页
				ServiceRequest request = new ServiceRequest(API.getMainRestList);
				request.addData("startIndex", startIndex);
				request.addData("pageSize", pageSize);
				// -----
				OpenPageDataTracer.getInstance().addEvent("列表查询", "餐厅列表");
				// -----
				setButtonState(false);
				CommonTask.requestMutely(request, new CommonTask.TaskListener<RestListDTO>() {

					@Override
					protected void onSuccess(RestListDTO dto) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						loadDataToRestList(adapter, startIndex, dto);
						dragview_rest_list.reset();
					}

					@Override
					protected void onError(int code, String message) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("列表查询");
						// -----
						DialogUtil.showToast(getApplicationContext(), message);
						setButtonState(true);
						adapter.onTaskFail();
						dragview_rest_list.reset();
					}

				});

			}

			/**
			 * @param adapter
			 * @param startIndex
			 * @param dto
			 */
			private void loadDataToRestList(final ListViewAdapter<RestListData> adapter, final int startIndex, RestListDTO dto) {
				List<RestListData> restListDataList = dto.list;
				if (restListDataList == null || restListDataList.size() == 0) {
					restListDataList = new ArrayList<RestListData>();
					restListView.setVisibility(View.GONE);
					return;
				}

				ListViewAdapter.AdapterDto<RestListData> adapterDto = new ListViewAdapter.AdapterDto<RestListData>();
				adapterDto.setList(restListDataList);
				adapterDto.setPageInfo(dto.pgInfo);
				adapter.onTaskSucceed(adapterDto);
				// 第一页更新筛选条件
				if (startIndex == 1) {
					updateFilter(dto);
				}
				setButtonState(true);
			}

			@Override
			public void onRenderItem(ListViewAdapter<RestListData> adapter, ViewHolder holder, RestListData data) {
				// 图片url
				MyImageView iconUrl = holder.$myIv(R.id.index_item_pic_url);
				// 餐馆名称
				TextView restName = holder.$tv(R.id.index_item_rest_name);
				ImageView indexListItemPromotionIconMibi = holder.$iv(R.id.index_list_item_promotion_icon_mibi);
				TextView indexListItemPromotionMibi = holder.$tv(R.id.index_list_item_promotion_mibi);
				TextView indexListItemPromotionDiscount = holder.$tv(R.id.index_list_item_promotion_discount);
				TextView indexListItemPromotionCoupon = holder.$tv(R.id.index_list_item_promotion_coupon);

				// 总体评价 0~5
				RatingBar overallNum = (RatingBar) holder.$(R.id.index_item_overall_num);
				// 人均
				TextView avgPrice = holder.$tv(R.id.index_item_food_avg_price);
				// 描述
				TextView describe = holder.$tv(R.id.index_item_describe);
				// 距离
				TextView distance = holder.$tv(R.id.index_item_distance);
				iconUrl.setImageByUrl(data.picUrl, true, 0, ScaleType.CENTER_CROP);
				// 设置餐馆名字
				if ("".equals(data.restName)) {
					restName.setText(R.string.text_null_hanzi);
				} else {
					restName.setText(data.restName);
				}
				if (data.iconTag == 1) {
					indexListItemPromotionIconMibi.setVisibility(View.GONE);
					indexListItemPromotionMibi.setVisibility(View.GONE);
					indexListItemPromotionDiscount.setVisibility(View.GONE);

					indexListItemPromotionCoupon.setVisibility(View.VISIBLE);
					indexListItemPromotionCoupon.setText(data.iconTitle);
				} else if (data.iconTag == 2) {
					indexListItemPromotionIconMibi.setVisibility(View.GONE);
					indexListItemPromotionMibi.setVisibility(View.GONE);
					indexListItemPromotionCoupon.setVisibility(View.GONE);

					indexListItemPromotionDiscount.setVisibility(View.VISIBLE);
					indexListItemPromotionDiscount.setText(data.iconTitle);
				} else if (data.iconTag == 3) {
					indexListItemPromotionDiscount.setVisibility(View.GONE);
					indexListItemPromotionCoupon.setVisibility(View.GONE);

					indexListItemPromotionMibi.setVisibility(View.VISIBLE);
					indexListItemPromotionMibi.setText(data.iconTitle);
					indexListItemPromotionIconMibi.setVisibility(View.VISIBLE);
					indexListItemPromotionIconMibi.setImageResource(R.drawable.icon_mibi_1);
				} else if (data.iconTag == 4) {
					indexListItemPromotionDiscount.setVisibility(View.GONE);
					indexListItemPromotionCoupon.setVisibility(View.GONE);

					indexListItemPromotionMibi.setVisibility(View.VISIBLE);
					indexListItemPromotionMibi.setText(data.iconTitle);
					indexListItemPromotionIconMibi.setVisibility(View.VISIBLE);
					indexListItemPromotionIconMibi.setImageResource(R.drawable.icon_mibi_2);
				} else {
					indexListItemPromotionIconMibi.setVisibility(View.GONE);
					indexListItemPromotionMibi.setVisibility(View.GONE);
					indexListItemPromotionDiscount.setVisibility(View.GONE);
					indexListItemPromotionCoupon.setVisibility(View.GONE);
				}
				overallNum.setProgress((int) data.overallNum);
				avgPrice.setText(data.avgPrice);
				describe.setText(data.describe);
				distance.setText(data.distance);

			};

		});
		adapter.setExistPage(true);
		adapter.setmCtx(HomeActivity.this);
		adapter.setListView(restListView);
	}

	/**
	 * 加载精选餐厅数据
	 */
	private void loadDataToTopRestList(RestRecomListDTO dto, boolean isRefresh) {
		if (dto != null && dto.list != null && dto.list.size() > 0) {
			topPageStatus.isLast = dto.pgInfo.lastTag;			
			topPageStatus.startIndex = dto.pgInfo.nextStartIndex;
			if (isRefresh) {
				topRestAdapter.setList(dto.list, topPageStatus.isLast);
			} else {
				topRestAdapter.addList(dto.list, topPageStatus.isLast);
			}
		} else {
			topRestAdapter.addList(new ArrayList<RestRecomPicData>(), true);
		}
		// 设置线程安全
		topPageStatus.isTaskSafe = true;
		// setChildEnable(true);
	}

	/**
	 * 加载最新推荐餐厅
	 */
	private void loadDataToRecomRestList(RestRecomListDTO dto, boolean isRefresh) {
		if (dto != null && dto.list != null && dto.list.size() > 0) {
			wfPageStatus.isLast = dto.pgInfo.lastTag;
			wfPageStatus.startIndex = dto.pgInfo.nextStartIndex;
			if (isRefresh) {
				waterFallAdapter = new IndexWaterFallAdapter(this);
				waterfall_view.setAdapter(waterFallAdapter);
				waterFallAdapter.setList(dto.list);
			} else {
				waterFallAdapter.addList(dto.list);
			}
		}
		// 设置线程安全
		wfPageStatus.isTaskSafe = true;
		// setChildEnable(true);
	}

	/**
	 * 构建城市
	 */
	protected void initCityIssues() {
		cityInfo = SessionManager.getInstance().getCityInfo(this);
		if (Settings.DEFAULT_CITY_ID.equals(cityInfo.getId())) {
			Fg114Application.super57PhoneNumber = cityInfo.getPhone();
		}
	}

	/**
	 * 退出应用处理
	 */
	private void appExit() {
		// 是否提示
		boolean isSuggestDesktopLink = SharedprefUtil.getBoolean(this, Settings.IS_SUGGEST_DESKTOP_LINK, true);
		if (isSuggestDesktopLink) {
			DialogUtil.showAlert(this, true, getString(R.string.text_info_create_shortcut), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityUtil.setShortCutNew(HomeActivity.this);
					SharedprefUtil.saveBoolean(HomeActivity.this, Settings.IS_SUGGEST_DESKTOP_LINK, false);
					dialog.cancel();
					ActivityUtil.exitApp(HomeActivity.this);
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedprefUtil.saveBoolean(HomeActivity.this, Settings.IS_SUGGEST_DESKTOP_LINK, false);
					dialog.cancel();
					ActivityUtil.exitApp(HomeActivity.this);
				}
			});
		} else {
			ActivityUtil.exitApp(HomeActivity.this);
		}
	}

	// 所有的随手拍都通过此方法判断是否转到选择餐厅
	private void dealCaptureEvent() {

		if (Settings.JUMP_RIGHT_NOW_FOR_CAPTURE_IS_DONE) {
			Bundle bundle = new Bundle();
			ActivityUtil.jump(this, RestaurantSearchActivity.class, 0, bundle);
			Settings.JUMP_RIGHT_NOW_FOR_CAPTURE_IS_DONE = false;
		}
	}

	@Override
	protected void updateSystemMessage() {
		super.updateSystemMessage();
		// 显示广告
		tryDisplayAdvertisement();
	}

	// ---------------------------------------------------------------------------------------------3D翻转动画
	int duration = 300;
	float depthFactor = 1.1f;

	private void applyRotation(ViewGroup container, float start, float end) {
		final float centerX = container.getWidth() / 2.0f;
		final float centerY = container.getHeight() / 2.0f;

		final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY, centerX * depthFactor, true);
		rotation.setDuration(duration);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateDecelerateInterpolator());
		rotation.setAnimationListener(new AniMiddleListener(container, start, end));

		container.startAnimation(rotation);
	}

	private final class AniMiddleListener implements Animation.AnimationListener {
		private float start;
		private float end;
		private ViewGroup container;

		public AniMiddleListener(ViewGroup container, float start, float end) {
			this.start = start;
			this.end = end;
			this.container = container;
		}

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			container.post(new SwapViews(container, start, end));
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	private final class AniEndListener implements Animation.AnimationListener {
		private ViewGroup container;

		public AniEndListener(ViewGroup container) {
			this.container = container;
		}

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			container.clearAnimation();
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	private final class SwapViews implements Runnable {
		private float start;
		private float end;
		private ViewGroup container;

		public SwapViews(ViewGroup container, float start, float end) {
			this.start = start;
			this.end = end;
			this.container = container;
		}

		public void run() {
			final float centerX = container.getWidth() / 2.0f;
			final float centerY = container.getHeight() / 2.0f;
			// --
			Rotate3dAnimation rotation;
			// 第一个view
			View view1 = container.getChildAt(0);
			View view2 = container.getChildAt(1);
			// --
			if (view1.getVisibility() == View.VISIBLE) {
				view1.setVisibility(View.GONE);
				view2.setVisibility(View.VISIBLE);
				rotation = new Rotate3dAnimation(-end, start, centerX, centerY, centerX * depthFactor, false);
				view2.requestFocus();

			} else {
				view1.setVisibility(View.VISIBLE);
				view2.setVisibility(View.GONE);
				rotation = new Rotate3dAnimation(-end, start, centerX, centerY, centerX * depthFactor, false);
				view1.requestFocus();
			}
			// --
			rotation.setDuration(duration);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new AccelerateDecelerateInterpolator());
			rotation.setAnimationListener(new AniEndListener(container));
			// --
			container.startAnimation(rotation);
		}
	}

	/**
	 * 更新所有过滤器
	 * 
	 * @param dto
	 */
	private void updateFilter(RestListDTO dto) {
		if (dto == null) {
			return;
		}
		// selectRegionName selectMainMenuTypeName selectSortName 分别为三个按钮的名称
		btFirst.setText(dto.selectRegionName);
		btChannel.setText(dto.selectMainMenuTypeName);
		btSort.setText(dto.selectSortName);
		//
		setButtonState(false);
		updateFirstFilter(dto);
		updateChannelFilter(dto);
		updateSortFilter(dto);
		setButtonState(true);
	}

	private void setButtonState(boolean state) {
		btFirst.setClickable(state);
		btChannel.setClickable(state);
		btSort.setClickable(state);
	}

	/**
	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(RestListDTO dto) {

		mAreaList.clear();

		// 手工添加“附近区域”，放在
		RfTypeListDTO nearRegionDto = new RfTypeListDTO();
		nearRegionDto.setUuid(Settings.STATUTE_CHANNEL_NEARBY);
		nearRegionDto.setName("附近");
		mDistanceList = SessionManager.getInstance().getListManager().getDistanceList(this);
		nearRegionDto.getList().addAll(mDistanceList);
		//

		// 全部地域
		RfTypeListDTO allRegionDto = new RfTypeListDTO();
		allRegionDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		allRegionDto.setName("全部地域");

		if (dto.regionList == null) {
			dto.regionList = new ArrayList<RfTypeListDTO>();
		}
		// -------------
		// 添加“全部”子项
		List all = SelectionListView.mergeAllSubList(dto.regionList);
		RfTypeListDTO all_suball = new RfTypeListDTO();
		all_suball.setUuid(String.valueOf(Settings.STATUTE_ALL));
		all_suball.setName("-- 全部地域 --");
		all_suball.setParentId(allRegionDto.getUuid());
		//
		all.add(0, all_suball);

		allRegionDto.setIsNeedGroupBy(true);
		allRegionDto.setList(all);

		// -------------
		dto.regionList.add(0, nearRegionDto);
		dto.regionList.add(0, allRegionDto);

		boolean hasSelectedRegion = false; // 是否存在选中的Region
		boolean hasSelectedDistrict = false; // 是否存在选中的District
		String selectedRegionName = "";
		String selectedDistrictName = "";

		for (RfTypeListDTO ctld : dto.regionList) {

			if (ctld.getList() == null) {
				ctld.setList(new ArrayList<RfTypeDTO>());
			}
			if (ctld.getUuid().equals(String.valueOf(Settings.STATUTE_ALL)) || ctld.getUuid().equals(String.valueOf(Settings.STATUTE_CHANNEL_NEARBY))) {
				// "全部"和“附近”不用在子列表添加"全部"
			} else {
				// 为子商区添加“全部”
				RfTypeDTO allSubDto = new RfTypeDTO();
				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
				allSubDto.setName("全部" + ctld.getName());
				ctld.getList().add(0, allSubDto);
			}

			if (ctld.isSelectTag()) {
				mSelectedRegion = getPositionInRfTypeListDTOList(dto.regionList, ctld.getUuid());
				selectedRegionName = ctld.getName();
				hasSelectedRegion = true;
				selectedRegion = ctld;

				if (ctld.getList().size() > 1) {
					for (RfTypeDTO ctd : ctld.getList()) {
						if (ctd.isSelectTag()) {
							mSelectedDistrict = getPositionInRfTypeDTOList(ctld.getList(), ctd.getUuid());
							selectedDistrictName = ctd.getName();
							hasSelectedDistrict = true;
							selectedDistrict = ctd;
						}
					}
				}
			}

			mAreaList.add(ctld);
		}

		// // 如果是附近，修正下选择项
		// if (filter.getDistanceMeter() != 0) {
		// mSelectedRegion = 1;
		// int mSelectedDistance = getPositionInRfTypeDTOList(mDistanceList,
		// String.valueOf(filter.getDistanceMeter()));
		// mDistanceList.get(mSelectedDistance).setSelectTag(true);
		//
		// nearRegionDto.setSelectTag(true);
		// hasSelectedRegion = true;
		// hasSelectedDistrict = true;
		// selectedDistrictName =
		// mDistanceList.get(mSelectedDistance).getName();
		//
		// // --
		// selectedRegion = nearRegionDto;
		// selectedDistrict = mDistanceList.get(mSelectedDistance);
		//
		// setLocationLayoutVisibility(View.VISIBLE);
		// } else {
		// setLocationLayoutVisibility(View.GONE);
		// }

		if (!hasSelectedRegion) {
			// 不存在选中的区域
			mSelectedRegion = 0;
			mSelectedDistrict = 0;
			allRegionDto.setSelectTag(true);
			// btFirst.setText(dto.regionList.get(mSelectedRegion).getName());

			selectedRegion = allRegionDto;
			selectedDistrict = null;
		} else {
			if (hasSelectedDistrict) {
				// 存在选中的子商区，按钮文字为选中的子商区名称
				// btFirst.setText(selectedDistrictName);
			} else {
				mSelectedDistrict = 0;
				// 不存在选中的子商区，按钮文字为选中的大区域名称
				// btFirst.setText(selectedRegionName);
			}
		}
	}

	/**
	 * 更新频道，菜系过滤器
	 * 
	 * @param dto
	 */
	private void updateChannelFilter(RestListDTO dto) {
		mChannelList.clear();

		RfTypeListDTO resDto = new RfTypeListDTO();
		resDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		resDto.setName("全部菜系");

		if (dto.menuTypeList == null) {
			dto.menuTypeList = new ArrayList<RfTypeListDTO>();
		}

		// -------------
		// 添加“全部”子项
		List all = SelectionListView.mergeAllSubList(dto.menuTypeList);
		RfTypeListDTO all_suball = new RfTypeListDTO();
		all_suball.setUuid(String.valueOf(Settings.STATUTE_ALL));
		all_suball.setName("-- 全部菜系 --");
		//
		all.add(0, all_suball);

		resDto.setIsNeedGroupBy(true);
		resDto.setList(all);

		// -------------
		dto.menuTypeList.add(0, resDto);

		boolean hasSelectedMainMenu = false; // 是否存在选中的MainMenu
		boolean hasSelectedSubMenu = false; // 是否存在选中的SubMenu
		String selectedMainMenuName = "";
		String selectedSubMenuName = "";

		for (RfTypeListDTO ctld : dto.menuTypeList) {

			if (ctld.getList() == null) {
				ctld.setList(new ArrayList<RfTypeDTO>());
			}
			if (!ctld.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
				// 为子菜系添加全部
				RfTypeDTO allSubDto = new RfTypeDTO();
				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
				allSubDto.setName("全部" + ctld.getName());
				ctld.getList().add(0, allSubDto);
			}

			if (ctld.isSelectTag()) {
				mSelectedMainMenu = getPositionInRfTypeListDTOList(dto.menuTypeList, ctld.getUuid());
				selectedMainMenuName = ctld.getName();
				hasSelectedMainMenu = true;
				if (ctld.getList().size() > 1) {
					for (RfTypeDTO ctd : ctld.getList()) {
						if (ctd.isSelectTag()) {
							mSelectedSubMenu = getPositionInRfTypeDTOList(ctld.getList(), ctd.getUuid());
							selectedSubMenuName = ctd.getName();
							hasSelectedSubMenu = true;
						}
					}
				}
			}

			mChannelList.add(ctld);
		}

		if (!hasSelectedMainMenu) {
			// 不存在选中的主菜系
			resDto.setSelectTag(true);
			mSelectedMainMenu = 0;
			mSelectedSubMenu = 0;
			// btChannel.setText(dto.menuTypeList.get(mSelectedMainMenu).getName());
		} else {
			if (hasSelectedSubMenu) {
				// 存在选中的子菜系，按钮文字为选中的子菜系名称
				// btChannel.setText(selectedSubMenuName);
			} else {
				mSelectedSubMenu = 0;
				// 不存在选中的子菜系，按钮文字为选中的主菜系名称
				// btChannel.setText(selectedMainMenuName);
			}
		}
	}

	/**
	 * 更新排序过滤器
	 * 
	 * @param dto
	 */
	private void updateSortFilter(RestListDTO dto) {
		mSortList.clear();

		RfTypeDTO sortDto = new RfTypeDTO();
		sortDto.setUuid("");
		sortDto.setName("排序方式");

		RfTypeDTO avgDto = new RfTypeDTO();
		avgDto.setUuid("");
		avgDto.setName("人均价格");

		RfTypeDTO emptyDto = new RfTypeDTO();
		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		emptyDto.setName("默认筛选");

		if (dto.sortList != null && dto.sortList.size() > 0) {
			for (RfTypeDTO ctd : dto.sortList) {
				ctd.setMemo(TAG_TYPE_SORT);
			}
			mSortList.add(sortDto);
			mSortList.addAll(dto.sortList);
		}

		if (dto.avgList != null && dto.avgList.size() > 0) {
			for (RfTypeDTO ctd : dto.avgList) {
				ctd.setMemo(TAG_TYPE_AVG);
			}
			mSortList.add(avgDto);
			mSortList.addAll(dto.avgList);
		}

		if (mSortList.size() == 0) {
			mSortList.add(emptyDto);
			mSelectedSort = 0;
		} else {
			RfTypeDTO selectedDto = null;
			for (int i = 0; i < mSortList.size(); i++) {
				if (mSortList.get(i).isSelectTag()) {
					mSelectedSort = i;
					selectedDto = mSortList.get(i);
					break;
				}
			}
			if (selectedDto != null && !TextUtils.isEmpty(selectedDto.getUuid())) {
				if (selectedDto.getMemo().equals(TAG_TYPE_SORT)) {
					filter.setSortTypeTag(Integer.parseInt(selectedDto.getUuid()));
					filter.setAvgTag("0");
				} else if (selectedDto.getMemo().equals(TAG_TYPE_AVG)) {
					filter.setSortTypeTag(0);
					filter.setAvgTag(selectedDto.getUuid());
				}
				// btSort.setText(mSortList.get(mSelectedSort).getName());
			} else {
				mSelectedSort = 1;
				// btSort.setText(mSortList.get(1).getName());
				mSortList.get(1).setSelectTag(true);
			}
		}
	}

	/**
	 * 显示第一过滤条件的筛选框
	 */
	// 下面两个用于不能选附近时“回滚”到前一选择项
	private ItemData selectedRegion = null;
	private ItemData selectedDistrict = null;

	private void showFirstFilter() {
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mAreaList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
				filter.reset();
				// 取消当前选择
				if (mainData != null && !String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid())) {
					mainData.setSelectTag(false);
				}
				if (subData != null) {
					subData.setSelectTag(false);
				}
				if (mAreaList != null && mAreaList.size() > 0) {
					mAreaList.get(0).setSelectTag(true);
				}
				// -----------------------------------------------------------------------
				if (subData == null) {
					if (filter.getRegionId().equals(mainData.getUuid()) && filter.getDistrictId().equals(String.valueOf(Settings.STATUTE_ALL))) {
						return; // 选择的是同一项
					}
					filter.setRegionId(mainData.getUuid());
					filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
					// btFirst.setText(mainData.getName());
				} else {
					if (filter.getRegionId().equals(mainData.getUuid()) && filter.getDistrictId().equals(subData.getUuid())) {
						return;
					}

					// 如果是附近
					if (mainData.getUuid().equals(String.valueOf(Settings.STATUTE_CHANNEL_NEARBY))) {
						CityInfo city = SessionManager.getInstance().getCityInfo(getCurrentTopActivity());
						CityInfo gpsCity = SessionManager.getInstance().getGpsCity(getCurrentTopActivity());

						if (gpsCity == null || gpsCity.getId() == null || !gpsCity.getId().equals(city.getId())) {
							DialogUtil.showAlert(getCurrentTopActivity(), "提示", "您不在当前城市，请先切换到所在城市后再选择附近");
							// 回滚到前一选项
							// if (selectedRegion != null) {
							// selectedRegion.setSelectTag(true);
							// }
							// if (selectedDistrict != null) {
							// selectedDistrict.setSelectTag(true);
							// }
							return;
						}
						filter.setDistanceMeter(Integer.parseInt(subData.getUuid()));
						// btFirst.setText(subData.getName());
						//
						filter.setRegionId("0");
						filter.setDistrictId("0");
					} else {

						// 如果主列表是“全部”，子列表要取真正的主列表UUID，在子的parentId里
						filter.setRegionId(String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid());
						filter.setDistrictId(subData.getUuid());
						//
						filter.setDistanceMeter(0);
						if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
							// btFirst.setText(mainData.getName());
						} else {
							// btFirst.setText(subData.getName());
						}
					}
				}

				ActivityUtil.jump(HomeActivity.this, ResAndFoodListActivity.class, 0, new Bundle());
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btFirst.setSelected(false);
			}
		});
	}

	/**
	 * 显示频道，菜系条件的筛选框
	 */
	private void showChannelFilter() {
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mChannelList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
				filter.reset();
				// 取消当前选择
				if (mainData != null && !String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid())) {
					mainData.setSelectTag(false);
				}
				if (subData != null) {
					subData.setSelectTag(false);
				}
				if (mChannelList != null && mChannelList.size() > 0) {
					mChannelList.get(0).setSelectTag(true);
				}
				// -----------------------------------------------------------------------
				if (subData == null) {
					// if (filter.getMainMenuId().equals(mainData.getUuid()) &&
					// filter.getSubMenuId().equals(String.valueOf(Settings.STATUTE_ALL)))
					// {
					// return;
					// }
					filter.setMainMenuId(mainData.getUuid());
					filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
					// btChannel.setText(mainData.getName());
				} else {
					// if (filter.getMainMenuId().equals(mainData.getUuid()) &&
					// filter.getSubMenuId().equals(subData.getUuid())) {
					// return;
					// }
					filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid());
					filter.setSubMenuId(subData.getUuid());
					if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
						// btChannel.setText(mainData.getName());
					} else {
						// btChannel.setText(subData.getName());
					}
				}
				ActivityUtil.jump(HomeActivity.this, ResAndFoodListActivity.class, 0, new Bundle());
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btChannel.setSelected(false);
			}
		});
	}

	/**
	 * 显示排序条件的筛选框
	 */
	private void showSortFilter() {

		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mSortList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
				filter.reset();
				// 取消当前选择，始终选择第一项
				if (mainData != null) {
					mainData.setSelectTag(false);
				}
				if (subData != null) {
					subData.setSelectTag(false);
				}
				if (mSortList != null && mSortList.size() > 0) {
					mSortList.get(0).setSelectTag(true);
				}
				// -----------------------------------------------------------------------
				if (mainData.getMemo().equals(TAG_TYPE_SORT)) {
					if (filter.getSortTypeTag() == Integer.parseInt(mainData.getUuid()) && filter.getAvgTag().equals("0")) {
						return;
					}
					filter.setSortTypeTag(Integer.parseInt(mainData.getUuid()));
					filter.setAvgTag("0");
				} else if (mainData.getMemo().equals(TAG_TYPE_AVG)) {
					if (filter.getSortTypeTag() == 0 && filter.getAvgTag().equals(mainData.getUuid())) {
						return;
					}
					filter.setSortTypeTag(0);
					filter.setAvgTag(mainData.getUuid());
				}
				// btSort.setText(mainData.getName());

				ActivityUtil.jump(HomeActivity.this, ResAndFoodListActivity.class, 0, new Bundle());
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btSort.setSelected(false);
			}
		});

	}

	/**
	 * 返回指定id项在列表中的位置下标
	 * 
	 * @param list
	 * @param id
	 * @return
	 */
	private int getPositionInRfTypeDTOList(List<RfTypeDTO> list, String id) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUuid().equals(id)) {
				return i;
			}
		}
		return 0;
	}

	private int getPositionInRfTypeListDTOList(List<RfTypeListDTO> list, String id) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUuid().equals(id)) {
				return i;
			}
		}
		return 0;
	}
}