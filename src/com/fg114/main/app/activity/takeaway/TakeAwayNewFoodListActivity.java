package com.fg114.main.app.activity.takeaway;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.HomeActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.adapter.AdvertisementAdapter.ViewHolder;
import com.fg114.main.app.service.UpdateService;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MyViewGroup;
import com.fg114.main.app.view.ParabolaAnimation;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.app.view.DigitalSelector.OnDigitChangeListener;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutInfoData;
import com.fg114.main.service.dto.TakeoutMenuData2;
import com.fg114.main.service.dto.TakeoutMenuList2DTO;
import com.fg114.main.service.dto.TakeoutMenuListPack2DTO;
import com.fg114.main.service.dto.TakeoutMenuPropertyData;
import com.fg114.main.service.dto.TakeoutMenuPropertyTypeData;
import com.fg114.main.service.dto.TakeoutMenuSelData;
import com.fg114.main.service.dto.TakeoutMenuSelPackDTO;
import com.fg114.main.service.dto.TakeoutMenuTypeData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.CommonTask.TaskListener.CacheKeyAndTime;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.google.xiaomishujson.Gson;

/**
 * 需要传入的数据 1、Settings.UUID //takeoutId
 * 
 * @author lijian
 * 
 */
public class TakeAwayNewFoodListActivity extends MainFrameActivity {
	public static TakeAwayNewFoodListActivity takeAwayNewFoodListActivityInstance = null;
	private LayoutInflater mInflater;
	private RelativeLayout contextView;

	// --------------------------------------- 数据
	private TakeoutMenuListPack2DTO takeoutMenuListPack2DTO;
	private List<TakeoutMenuData2> takeoutMenuDataList;
	private double totalPrice;// 本身价格加上配料的价格
	private List<TakeoutMenuPropertyTypeData> propertyTypeList = null;
	private int num = 1;// 菜品数量 在选择配料时用
	private TakeoutMenuData2 takeoutMenuData2;

	// private TakeoutMenuSelPackDTO takeoutMenuSelPackDTO = new
	// TakeoutMenuSelPackDTO();// 购物车

	private List<TakeoutMenuSelData> TakeoutMenuSeList = new ArrayList<TakeoutMenuSelData>();// 购物车列表
	// private List<TakeoutMenuData> list;
	// public static TakeoutInfoData takeoutData;
	// public static CommonTypeDTO userReceiveAdressData;

	// ------------控件---------------------------
	private ScrollView svFoodCategory;// 左边的菜品类型ScrollView
	private RadioGroup rgType;// 左边的菜品类型的RadioGroup
	private ListView lvFood;
	private RelativeLayout mainContentLayout;

	private MyTakeoutAdapter adapter;
	private MySearchTakeoutAdapter searchAdapter;

	// 广告组件
	private ViewFlow advViewFlow;
	private CircleFlowIndicator advCircleIndicator;
	private ImageView close;
	private View top_view;
	// private ImageView advCloseButton;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。

	// 用于存放菜品类型的RadioButton
	private HashMap<String, RadioButton> mapNumButton = new HashMap<String, RadioButton>();

	// 当没获取到数据时，显示提示
	private TextView tvEmpty;

	private TextView tvShoppingHint;
	private Button bntShoppingCart;
	private TextView tvAnimateHint;
	private RelativeLayout relaBottomLayout;
	private Button searchBnt;
	//private RelativeLayout mainLayout;
	private TextView takeaway_shoppint_num_tv;
	private TextView takeaway_shoppint_price_tv;
	private ImageView takewawy_shopping_cart_iv;
	private TextView takeaway_hint;

	// popupWindow中控件
	private TextView shoppingHintDialogTv;
	private Button shoppingCartDialogBnt;
	private View view;
	private TextView takeaway_shoppint_num_dialog_tv;
	private TextView takeaway_shoppint_price_dialog_tv;
	private android.widget.PopupWindow popupWindow;
	private RelativeLayout topLayout;
	private RelativeLayout centerLayout;
	private RelativeLayout buttomLayout;
	private TextView cancelBnt;
	private AutoCompleteTextView searchEdit;
	private Button resetBnt;
	private ListView listView;
	private TextView noDataTv;
	// 属性popupWindow中控件
	private android.widget.PopupWindow attributePopupwindow;
	private View viewProperty;
	private TextView takeaway_property_name;
	private TextView takeaway_property_total_price;
	private ImageView takeaway_property_cannel;
	private LinearLayout takeaway_property_view;
	private DigitalSelector takeaway_property_diaitalselector;
	private Button takeaway_property_bt;

	private int bntShoppingCartX;
	private int bntShoppingCartY;
	private int bntSelectNumX;
	private int bntSelectNumY;

	private boolean isRadioCheck = true;
	private boolean isAnimationRun = false;

	private int fromTag = 1;
	private String uuid = "";
	// private TextView top_hint;
	private int statusBarHeight;
	private List<MainPageAdvData> advList;
	
	private TakeoutMenuSelPackDTO takeout;
	private boolean isclick;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖菜单列表", "");
		// ----------------------------
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
		takeAwayNewFoodListActivityInstance = this;
		try {
			// 获得传入参数
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				uuid = bundle.getString(Settings.UUID);
			}

			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
			if (!isNetAvailable) {
				// 没有网络的场合，去提示页
				Bundle bund = new Bundle();
				bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
				// TODO: 跳至无网提示页
				ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
			}
		} catch (Exception e) {
		}

		// 初始化界面
		initComponent();
		excuteTakeoutTask();

	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖菜单列表", "");
		// ----------------------------
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	@Override
	protected void onResume() {
		if (Settings.NEED_TAKEAWAY_LIST) {
			excuteTakeoutTask();
			Settings.NEED_TAKEAWAY_LIST = false;
		}
		if(isclick){
			excuteTakeoutTaskLike(takeout);
			}
		super.onResume();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 201&&data!=null) {
			String s = data.getExtras().getString(Settings.BUNDLE_menuSelPack);
			TakeoutMenuSelPackDTO takeout = JsonUtils.fromJson(s, TakeoutMenuSelPackDTO.class);

			// 重置界面显示数量
			for (int i = 0; i < takeoutMenuDataList.size(); i++) {
				takeoutMenuDataList.get(i).num = 0;
			}

			// 改变购物车中数量
			if(takeout!=null){
			TakeoutMenuSeList = takeout.list;
			

			for (int i = 0; i < takeout.list.size(); i++) {

				// setTakeoutMenuSelNum2(takeout.list.get(i).dataIdentifer,
				// takeout.list.get(i).num);
				// 改变界面显示数量
				setTakeoutMenuDataList(takeout.list.get(i).uuid, takeout.list.get(i).num);
			}
			adapter.notifyDataSetChanged();

			searchAdapter.notifyDataSetChanged();

			showShoppingMessage();
			}
		}
		
		if (resultCode == 205&&data!=null) {
			String s = data.getExtras().getString(Settings.BUNDLE_menuSelPack);
			 isclick=data.getExtras().getBoolean("isclick");
			
			 takeout = JsonUtils.fromJson(s, TakeoutMenuSelPackDTO.class);

//			}
		}

	}

	// // 广告是否是被主动关闭过
	// private boolean hasAdvertisementBeenClosed() {
	// // 通过判断是否是同一天来控制广告位的显隐
	// long timeStamp =
	// SessionManager.getInstance().getAdvCloseTimeStamp(TakeAwayNewFoodListActivity.this);
	// if (!CalendarUtil.isToday(timeStamp)) {
	// return false;
	// } else {
	// return true;
	// }
	//
	// }

	private synchronized void tryDisplayAdvertisement() {
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0) {
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
			advViewFlow.setAdapter(new TakeAwayAdvertisementAdapter(this, advList));
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
			BaseAdapter adapter = new TakeAwayAdvertisementAdapter(this, new ArrayList<MainPageAdvData>());
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

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置Header标题栏
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("餐厅详情");
		this.setFunctionLayoutGone();

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// ----
				OpenPageDataTracer.getInstance().addEvent("餐厅详情按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, uuid);
				bundle.putInt(Settings.BUNDLE_FROM_TAG, 11);
				ActivityUtil.jump(TakeAwayNewFoodListActivity.this, NewTakeAwayRestaurantDetailActivity.class, 0, bundle);
			}
		});
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = (RelativeLayout) mInflater.inflate(R.layout.take_away_new_food_list, null);
		// ------------首页广告-----------------------------------------------------------------------
		advViewFlow = (ViewFlow) contextView.findViewById(R.id.viewflow);
		advCircleIndicator = (CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator);
		close = (ImageView) contextView.findViewById(R.id.close);
		top_view = (View) contextView.findViewById(R.id.top_view);
		advViewFlow.setFlowIndicator(advCircleIndicator);

		//mainLayout = (RelativeLayout) contextView.findViewById(R.id.takeaway_content_layout);
		lvFood = (ListView) contextView.findViewById(R.id.takeaway_list_food_lv);
		svFoodCategory = (ScrollView) contextView.findViewById(R.id.takeaway_list_foodCategoryLeft_sv);
		rgType = (RadioGroup) contextView.findViewById(R.id.takeaway_list_food_type_rg);
		tvEmpty = (TextView) contextView.findViewById(R.id.takeaway_food_tv);
		mainContentLayout = (RelativeLayout) contextView.findViewById(R.id.takeaway_content_layout);
		relaBottomLayout = (RelativeLayout) contextView.findViewById(R.id.takeaway_bottom_layout);
		tvShoppingHint = (TextView) contextView.findViewById(R.id.takewawy_shopping_hint_tv);
		takeaway_shoppint_num_tv = (TextView) contextView.findViewById(R.id.takeaway_shoppint_num_tv);
		takeaway_shoppint_price_tv = (TextView) contextView.findViewById(R.id.takeaway_shoppint_price_tv);
		bntShoppingCart = (Button) contextView.findViewById(R.id.takewawy_shopping_cart_bnt);
		searchBnt = (Button) contextView.findViewById(R.id.takeaway_list_search_bnt);
		takeaway_hint = (TextView) contextView.findViewById(R.id.takeaway_hint);

		tvAnimateHint = (TextView) contextView.findViewById(R.id.takeaway_animate_tv);
		takewawy_shopping_cart_iv = (ImageView) contextView.findViewById(R.id.takewawy_shopping_cart_iv);

		// 初始化popupWindow里面控件
		initPopupView();

		initAttributePopupView();

		adapter = new MyTakeoutAdapter(this);
		searchAdapter = new MySearchTakeoutAdapter(this);
		svFoodCategory.setVisibility(View.GONE);
		mainContentLayout.setVisibility(View.GONE);
		lvFood.setOnScrollListener(new OnScrollListener() {

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
				try {
					if (takeoutMenuDataList.size() < 1) {
						return;
					}
					TakeoutMenuData2 data = takeoutMenuDataList.get(firstVisibleItem);
					RadioButton btnTypeChecked = (RadioButton) rgType.findViewById(rgType.getCheckedRadioButtonId());
					if (btnTypeChecked == null) {
						return;
					}
					TakeoutMenuList2DTO dto = (TakeoutMenuList2DTO) btnTypeChecked.getTag();
					if (dto != null) {
						if (!getSpecialType(dto.typeData).equals(data.soGroupId)) {
							RadioButton btnType = mapNumButton.get(data.soGroupId);
							if (btnType != null) {
								// 控制垂直SrcollView的自动滑动
								int btnTypeY = btnType.getTop();
								int scrollY = svFoodCategory.getScrollY();
								int dy = btnTypeY - scrollY;
								if (btnTypeY < scrollY) {
									svFoodCategory.smoothScrollBy(0, dy);
								} else {
									int yDistance = btnType.getBottom() - scrollY;
									if (yDistance > svFoodCategory.getHeight()) {
										svFoodCategory.smoothScrollBy(0, yDistance - svFoodCategory.getHeight());
									}
								}
								isRadioCheck = false;
								btnType.setChecked(true);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(TakeAwayNewFoodListActivity.this).inflate(R.layout.common_foot_view, null);

		lvFood.addFooterView(linearLayout);

		rgType.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				try {
					if (checkId == -1) {
						return;
					}
					if (!isRadioCheck) {
						isRadioCheck = true;
						return;
					}
					RadioButton btnType = (RadioButton) rgType.findViewById(checkId);
					final TakeoutMenuList2DTO dto = (TakeoutMenuList2DTO) btnType.getTag();
					if (dto != null) {
						lvFood.setSelection(dto.startIndex);
					}
				} catch (Exception e) {
				}
			}
		});

		// 弹出搜索窗口
		searchBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("搜索按钮");
				// -----
				// 初始化popupWindow里面控件
				// initPopupView();
				// View view =
				// LayoutInflater.from(TakeAwayFoodListActivity.this).inflate(R.layout.takeaway_search_gray_layout,
				// null);

				popupWindow = showPopupwindow(TakeAwayNewFoodListActivity.this, null, view);

				// final RelativeLayout topLayout = (RelativeLayout)
				// view.findViewById(R.id.takeaway_top_layout);
				// final RelativeLayout centerLayout = (RelativeLayout)
				// view.findViewById(R.id.takeaway_center_layout);
				// final RelativeLayout buttomLayout = (RelativeLayout)
				// view.findViewById(R.id.takeaway_bottom_layout);
				// final TextView cancelBnt = (TextView)
				// view.findViewById(R.id.takeaway_search_cancel_bnt);
				// final AutoCompleteTextView searchEdit =
				// (AutoCompleteTextView)
				// view.findViewById(R.id.takeaway_search_edt);
				// // final EditText searchEdit = (EditText)
				// // view.findViewById(R.id.takeaway_search_edt);
				// final Button resetBnt = (Button)
				// view.findViewById(R.id.takeaway_search_reset_bnt);
				// final ListView listView = (ListView)
				// view.findViewById(R.id.takeaway_list_food_lv);
				// final TextView noDataTv = (TextView)
				// view.findViewById(R.id.takeaway_search_no_tv);
				// shoppingHintDialogTv = (TextView)
				// view.findViewById(R.id.takewawy_shopping_hint_tv);
				// shoppingCartDialogBnt = (Button)
				// view.findViewById(R.id.takewawy_shopping_cart_bnt);
				// shoppingCartDialogBnt.setBackgroundDrawable(bntShoppingCart.getBackground());
				// shoppingCartDialogBnt.setPadding(UnitUtil.dip2px(50), 0,
				// UnitUtil.dip2px(10), 0);

				shoppingCartDialogBnt.setVisibility(bntShoppingCart.getVisibility());
				shoppingCartDialogBnt.setText(bntShoppingCart.getText().toString());
				shoppingHintDialogTv.setVisibility(tvShoppingHint.getVisibility());
				shoppingHintDialogTv.setText(tvShoppingHint.getText().toString());

				searchEdit.setText("");
				searchEdit.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// buttomLayout.setVisibility(View.VISIBLE);
						String text = s.toString();
						if (CheckUtil.isEmpty(text)) {
							resetBnt.setVisibility(View.INVISIBLE);
						} else {
							resetBnt.setVisibility(View.VISIBLE);
						}
						List<TakeoutMenuData2> list = searchTakeoutData(text.trim().toLowerCase());
						if (list == null | list.size() == 0 | CheckUtil.isEmpty(text)) {
							// popupWindow.showAtLocation(TakeAwayFoodListActivity.this.getWindow().getDecorView(),
							// Gravity.CENTER | Gravity.CENTER, 0, 0);
							buttomLayout.setVisibility(View.GONE);
							noDataTv.setVisibility(View.GONE);
							listView.setVisibility(View.VISIBLE);
							// if(CheckUtil.isEmpty(text)){
							searchAdapter.setList(list);

							// }

						} else {
							noDataTv.setVisibility(View.GONE);
							listView.setVisibility(View.VISIBLE);
							buttomLayout.setVisibility(View.VISIBLE);
							searchAdapter.setList(list);
							listView.setAdapter(searchAdapter);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});

				resetBnt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						searchEdit.setText("");
						// popupWindow.showAtLocation(TakeAwayFoodListActivity.this.getWindow().getDecorView(),
						// Gravity.CENTER | Gravity.CENTER, 0, 0);
						buttomLayout.setVisibility(View.GONE);
						noDataTv.setVisibility(View.VISIBLE);
						listView.setVisibility(View.VISIBLE);
						List<TakeoutMenuData2> list = searchTakeoutData("");
						searchAdapter.setList(list);
					}
				});

				cancelBnt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// adapter.setList(takeoutMenuDataList);
						// lvFood.setAdapter(adapter);
						popupWindow.dismiss();

					}
				});
			}
		});
		// 关闭广告
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("广告位关闭按钮", "");
				// -----

				top_view.setVisibility(View.GONE);
			}
		});

		advViewFlow.setAdapter(new TakeAwayAdvertisementAdapter(this, new ArrayList<MainPageAdvData>()));

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

	}

	// 请求数据
	private void excuteTakeoutTask() {

		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		// 获得订单列表 ，返回OrderListDTO
		ServiceRequest request = new ServiceRequest(API.getTakeoutMenuList2);
		// request.addData("fromTag", fromTag);// 来自哪里 1：点菜页 2：订单页
		request.addData("takeoutId", uuid);// //餐厅id

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<TakeoutMenuListPack2DTO>() {

			@Override
			protected void onSuccess(TakeoutMenuListPack2DTO dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				if (dto.list == null || dto.list.size() == 0) {
					mainContentLayout.setVisibility(View.GONE);
					svFoodCategory.setVisibility(View.GONE);
					tvEmpty.setVisibility(View.VISIBLE);
					return;
				} else {
					mainContentLayout.setVisibility(View.VISIBLE);
					svFoodCategory.setVisibility(View.VISIBLE);
					tvEmpty.setVisibility(View.GONE);
				}
				if (dto != null) {
					takeoutMenuListPack2DTO = dto;
				}
				// 设置餐厅名称
				if (!CheckUtil.isEmpty(dto.takeoutName)) {
					getTvTitle().setText(dto.takeoutName);
				}

				// 显示广告
				if (dto.advList != null && dto.advList.size() != 0) {
					top_view.setVisibility(View.VISIBLE);
					advList=dto.advList;
					tryDisplayAdvertisement();
				} else {
					top_view.setVisibility(View.GONE);
				}

				// userReceiveAdressData = dto.userReceiveAdressData;
				setTypeScrollView(dto);

				// TODO
				// list = dto.list.get(0).list;
				takeoutMenuDataList = dealWithTakeoutMenuData(dto.list);
				adapter.setList(takeoutMenuDataList);
				lvFood.setAdapter(adapter);
				if (!dto.canOrderTag) {
					bntShoppingCart.setVisibility(View.GONE);
					takeaway_hint.setVisibility(View.VISIBLE);
					takeaway_hint.setText(dto.hintForCanNotOrder);
					takewawy_shopping_cart_iv.setVisibility(View.INVISIBLE);
					takeaway_shoppint_num_tv.setVisibility(View.INVISIBLE);
					takeaway_shoppint_price_tv.setVisibility(View.INVISIBLE);
				} else {
					takeaway_hint.setVisibility(View.GONE);
					showShoppingMessage();
				}
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// doTest_confirm();

				DialogUtil.showToast(ContextUtil.getContext(), message);
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
				finish();
			}

			/*
			 * @Override protected void defineCacheKeyAndTime(CacheKeyAndTime
			 * keyAndTime) { keyAndTime.cacheKey = "TAKE_AWAY_CACHE_DATA_"+uuid;
			 * keyAndTime.cacheTimeMinute = 12 * 60;
			 * 
			 * };
			 */

			private void doTest_confirm() {
				String s = "{\"takeoutId\":\"1111\",\"takeoutName\":\"测试\",\"advList\":[{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"},{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"},{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"}],\"list\":[{\"typeData\":{\"uuid\":\"1\",\"name\":\"1\",\"detail\":\"1111111111111111\",\"favTag\":\"true\"},\"list\":[{\"uuid\":\"1\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[{\"uuid\":\"222\",\"name\":\"属性\",\"list\":[{\"uuid\":\"1\",\"name\":\"属性1\",\"price\":\"2\"},{\"uuid\":\"2\",\"name\":\"属性2\",\"price\":\"2\"},{\"uuid\":\"3\",\"name\":\"属性3\",\"price\":\"3\"}]},{\"uuid\":\"111\",\"name\":\"配料\",\"list\":[{\"uuid\":\"1\",\"name\":\"属性1\",\"price\":\"2\"},{\"uuid\":\"2\",\"name\":\"配料\",\"price\":\"2\"},{\"uuid\":\"3\",\"name\":\"属性3\",\"price\":\"3\"}]}]},{\"uuid\":\"2\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试2\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[]},{\"uuid\":\"3\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试3\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[]}]}],\"sendLimitPrice\":\"100\",\"canOrderTag\":\"true\",\"hintForCanNotOrder\":\"不能下单\"}";
				TakeoutMenuListPack2DTO data = JsonUtils.fromJson(s, TakeoutMenuListPack2DTO.class);
				onSuccess(data);

			}

		});
	}
	
	
	// 请求数据
	private void excuteTakeoutTaskLike(final TakeoutMenuSelPackDTO takeout) {

		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		// 获得订单列表 ，返回OrderListDTO
		ServiceRequest request = new ServiceRequest(API.getTakeoutMenuList2);
		// request.addData("fromTag", fromTag);// 来自哪里 1：点菜页 2：订单页
		request.addData("takeoutId", uuid);// //餐厅id

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<TakeoutMenuListPack2DTO>() {

			@Override
			protected void onSuccess(TakeoutMenuListPack2DTO dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				
				if (dto.list == null || dto.list.size() == 0) {
					mainContentLayout.setVisibility(View.GONE);
					svFoodCategory.setVisibility(View.GONE);
					tvEmpty.setVisibility(View.VISIBLE);
					return;
				} else {
					mainContentLayout.setVisibility(View.VISIBLE);
					svFoodCategory.setVisibility(View.VISIBLE);
					tvEmpty.setVisibility(View.GONE);
				}
				if (dto != null) {
					takeoutMenuListPack2DTO = dto;
				}
				// 设置餐厅名称
				if (!CheckUtil.isEmpty(dto.takeoutName)) {
					getTvTitle().setText(dto.takeoutName);
				}

				// 显示广告
				if (dto.advList != null && dto.advList.size() != 0) {
					top_view.setVisibility(View.VISIBLE);
					advList=dto.advList;
					tryDisplayAdvertisement();
				} else {
					top_view.setVisibility(View.GONE);
				}

				// userReceiveAdressData = dto.userReceiveAdressData;
				setTypeScrollView(dto);

				// TODO
				// list = dto.list.get(0).list;
				takeoutMenuDataList = dealWithTakeoutMenuData(dto.list);
				adapter.setList(takeoutMenuDataList);
				lvFood.setAdapter(adapter);
				if (!dto.canOrderTag) {
					bntShoppingCart.setVisibility(View.GONE);
					takeaway_hint.setVisibility(View.VISIBLE);
					takeaway_hint.setText(dto.hintForCanNotOrder);
					takewawy_shopping_cart_iv.setVisibility(View.INVISIBLE);
					takeaway_shoppint_num_tv.setVisibility(View.INVISIBLE);
					takeaway_shoppint_price_tv.setVisibility(View.INVISIBLE);
				} else {
					takeaway_hint.setVisibility(View.GONE);
					showShoppingMessage();
				}
				
				
				// 重置界面显示数量
				for (int i = 0; i < takeoutMenuDataList.size(); i++) {
					takeoutMenuDataList.get(i).num = 0;
				}
	
				// 改变购物车中数量
				if(takeout!=null){
				TakeoutMenuSeList = takeout.list;
				
	
				for (int i = 0; i < takeout.list.size(); i++) {
	
					// setTakeoutMenuSelNum2(takeout.list.get(i).dataIdentifer,
					// takeout.list.get(i).num);
					// 改变界面显示数量
					setTakeoutMenuDataList(takeout.list.get(i).uuid, takeout.list.get(i).num);
				}
				adapter.notifyDataSetChanged();
	
				searchAdapter.notifyDataSetChanged();
	
				showShoppingMessage();
				
				}
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// doTest_confirm();

				DialogUtil.showToast(ContextUtil.getContext(), message);
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
			}

			/*
			 * @Override protected void defineCacheKeyAndTime(CacheKeyAndTime
			 * keyAndTime) { keyAndTime.cacheKey = "TAKE_AWAY_CACHE_DATA_"+uuid;
			 * keyAndTime.cacheTimeMinute = 12 * 60;
			 * 
			 * };
			 */

			private void doTest_confirm() {
				String s = "{\"takeoutId\":\"1111\",\"takeoutName\":\"测试\",\"advList\":[{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"},{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"},{\"uuid\":\"22\",\"typeTag\":\"1\",\"title\":\"cesguan\",\"advUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"endDate\":\"888888888888888888\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"appName\":\"\",\"appDownloadUrl\":\"\"}],\"list\":[{\"typeData\":{\"uuid\":\"1\",\"name\":\"1\",\"detail\":\"1111111111111111\",\"favTag\":\"true\"},\"list\":[{\"uuid\":\"1\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[{\"uuid\":\"222\",\"name\":\"属性\",\"list\":[{\"uuid\":\"1\",\"name\":\"属性1\",\"price\":\"2\"},{\"uuid\":\"2\",\"name\":\"属性2\",\"price\":\"2\"},{\"uuid\":\"3\",\"name\":\"属性3\",\"price\":\"3\"}]},{\"uuid\":\"111\",\"name\":\"配料\",\"list\":[{\"uuid\":\"1\",\"name\":\"属性1\",\"price\":\"2\"},{\"uuid\":\"2\",\"name\":\"配料\",\"price\":\"2\"},{\"uuid\":\"3\",\"name\":\"属性3\",\"price\":\"3\"}]}]},{\"uuid\":\"2\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试2\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[]},{\"uuid\":\"3\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"测试3\",\"pinyin\":\"ceshi\",\"pinyinCap\":\"cs\",\"price\":\"10\",\"overallNum\":\"5\",\"detail\":\"5555\",\"propertyTypeList\":[]}]}],\"sendLimitPrice\":\"100\",\"canOrderTag\":\"true\",\"hintForCanNotOrder\":\"不能下单\"}";
				TakeoutMenuListPack2DTO data = JsonUtils.fromJson(s, TakeoutMenuListPack2DTO.class);
				onSuccess(data);

			}

		});
	}

	/**
	 * 构造左边SrcollView控件的RadioButton 并将每一个RadioButton存进HashMap
	 * 
	 * @param dto
	 */
	private void setTypeScrollView(TakeoutMenuListPack2DTO dto) {
		if (dto.list == null || dto.list.size() == 0) {
			rgType.setVisibility(View.GONE);
			return;
		}
		rgType.setVisibility(View.VISIBLE);
		rgType.removeAllViews();
		mapNumButton.clear();
		List<TakeoutMenuList2DTO> takeoutMenuListDTOList = dto.list;
		for (int i = 0; i < takeoutMenuListDTOList.size(); i++) {
			TakeoutMenuTypeData ctdType = takeoutMenuListDTOList.get(i).typeData;
			RadioButton btnType = createNumButton(ctdType.name, takeoutMenuListDTOList.get(i));
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 0, 0);
			rgType.addView(btnType, lp);
			rgType.setPadding(0, 0, 0, 0);
			mapNumButton.put(getSpecialType(ctdType), btnType);
		}
		isRadioCheck = false;
		((RadioButton) rgType.getChildAt(0)).setChecked(true);
	}

	/**
	 * 根据不同情况返回一个类型ID
	 * 
	 * @param type
	 * @return
	 */
	private String getSpecialType(TakeoutMenuTypeData type) {

		if (type == null || type.uuid == null || type.uuid.equals("")) {
			return "N/A"; // 构造一个虚拟id
		} else {
			return type.uuid;
		}
	}

	/**
	 * 创建菜品类型RadioButton
	 * 
	 * @param text
	 * @param dto
	 * @return
	 */
	private RadioButton createNumButton(String text, TakeoutMenuList2DTO dto) {
		RadioButton rbType = (RadioButton) View.inflate(this, R.layout.radio_button_dish_list, null);
		Bitmap bmp = null;
		rbType.setButtonDrawable(new BitmapDrawable(bmp));
		rbType.setText(text);
		rbType.setSingleLine(true);
		rbType.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		rbType.setTextColor(getResources().getColor(R.color.text_color_black));
		rbType.setGravity(Gravity.CENTER);
		if (dto.typeData.favTag) {
			Drawable drawable = getResources().getDrawable(R.drawable.takeaway_fav);
			// / 这一步必须要做,否则不会显示.
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			rbType.setCompoundDrawables(drawable, null, null, null);
		}
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 150);
		lp.setMargins(0, 0, 0, 0);
		rbType.setLayoutParams(lp);
		rbType.setPadding(0, 0, 0, 0);
		rbType.setTag(dto);
		return rbType;
	}

	public class MyTakeoutAdapter extends BaseAdapter {

		private List<TakeoutMenuData2> mList = new ArrayList<TakeoutMenuData2>();
		private LayoutInflater adaperInflater;
		private long timeStamp = System.currentTimeMillis();

		public MyTakeoutAdapter(Context context) {
			adaperInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			LinearLayout layoutContent;
			TextView memo_tv;
			MyImageView picUrl_img;
			TextView name_tv;
			TextView price_tv;
			RatingBar overallNum_rb;
			// DigitalSelector dg;
			ImageButton takeaway_list_item_shopping_cart_bt;
			TextView takeaway_list_item_shopping_cart_num;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {
				convertView = adaperInflater.inflate(R.layout.list_item_new_take_away_list, null);
				holder = new ViewHolder();

				holder.layoutContent = (LinearLayout) convertView.findViewById(R.id.layout_content);

				holder.memo_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvTitle);
				holder.picUrl_img = (MyImageView) convertView.findViewById(R.id.takeaway_list_item_ivFoodPic);
				holder.name_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvName);
				holder.price_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvPrice);
				// holder.dg = (DigitalSelector)
				// convertView.findViewById(R.id.takeaway_list_item_amount);
				holder.overallNum_rb = (RatingBar) convertView.findViewById(R.id.takeaway_list_item_overall_num);
				holder.takeaway_list_item_shopping_cart_bt = (ImageButton) convertView.findViewById(R.id.takeaway_list_item_shopping_cart_bt);
				holder.takeaway_list_item_shopping_cart_num = (TextView) convertView.findViewById(R.id.takeaway_list_item_shopping_cart_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TakeoutMenuData2 data = mList.get(position);

			if (TextUtils.isEmpty(data.picUrl)) {
				holder.picUrl_img.setVisibility(View.INVISIBLE);
			} else {
				holder.picUrl_img.setImageByUrl(data.picUrl, true, 0, ScaleType.FIT_XY);
				/*
				 * holder.picUrl_img.setImageByUrl(data.picUrl, true, 0,
				 * ScaleType.FIT_XY, new Runnable() {
				 * 
				 * @Override public void run() { // 菜品显示大图 if
				 * (!TextUtils.isEmpty(data.bigPicUrl)) {
				 * 
				 * holder.picUrl_img.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View view) {
				 * DialogUtil.createImageViewPanel
				 * (TakeAwayFoodListActivity.this, (View) view.getParent(),
				 * data.bigPicUrl); } }); } else {
				 * holder.picUrl_img.setClickable(false); } } });
				 */
				holder.picUrl_img.setVisibility(View.VISIBLE);
			}

			holder.overallNum_rb.setRating((float) data.overallNum);

			if (data.soIsFirst) {
				holder.memo_tv.setVisibility(View.VISIBLE);
				if (data.soTypeMemo.length() > 35) {
					holder.memo_tv.setText(data.soTypeMemo.substring(0, 30) + "...[展开]");
					holder.memo_tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(v, 1000);
							DialogUtil.showAlert(TakeAwayNewFoodListActivity.this, "", data.soTypeMemo);
						}
					});
				} else {
					holder.memo_tv.setText(data.soTypeMemo);
				}
			} else {
				holder.memo_tv.setVisibility(View.GONE);
			}
			holder.name_tv.setText(data.name);
			holder.price_tv.setText("￥" + digitalFormatConversion(data.price));

			if (data.num == 0) {
				holder.takeaway_list_item_shopping_cart_num.setVisibility(View.GONE);
			} else {
				holder.takeaway_list_item_shopping_cart_num.setVisibility(View.VISIBLE);
				holder.takeaway_list_item_shopping_cart_num.setText(data.num + "");
			}

			if (data.num != 0) {
				holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource(R.drawable.shopping_cart_red);
			} else {
				holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource(R.drawable.shopping_cart_gray_bt);
			}

			holder.takeaway_list_item_shopping_cart_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("购物车按钮", "");
					// -----
					// holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource();
					if(takeoutMenuListPack2DTO.canOrderTag){
					if (data.propertyTypeList != null && data.propertyTypeList.size() != 0) {
						// 有属性配料的 需要弹出属性配料选择 选好了 生成一个购物车
						setAttributeDate(data);
						attributePopupwindow = showAttributePopupwindow(TakeAwayNewFoodListActivity.this, null, viewProperty);
					} else {
						if (data.num == 0) {
							// 生成一个购物车对象
							data.num = data.num + 1;
							TakeoutMenuSelData takeoutMenSelDate = new TakeoutMenuSelData();
							takeoutMenSelDate.name = data.name;
							takeoutMenSelDate.uuid = data.uuid;
							takeoutMenSelDate.num = data.num;
							takeoutMenSelDate.price = data.price;
							takeoutMenSelDate.typeTag = 1;
							takeoutMenSelDate.canSelGiftTag = false;
							takeoutMenSelDate.giftTypeId = "";
							takeoutMenSelDate.nameColor = "";
							takeoutMenSelDate.canShowNumTag = true;
							takeoutMenSelDate.canChangeNumTag = true;
							takeoutMenSelDate.selPropertyHint = "";

							takeoutMenSelDate.dataIdentifer = CheckUtil.randomString(10);// 生成一个随机数字符串
							TakeoutMenuSeList.add(takeoutMenSelDate);// 放到购物车列表中

						} else {
							// 不需要生成新的购物车 改变原来购物车的数量
							data.num = data.num + 1;
							setTakeoutMenuSelNum(data.uuid, data.num);

						}

						holder.takeaway_list_item_shopping_cart_num.setVisibility(View.VISIBLE);
						int[] location = new int[2];
						holder.takeaway_list_item_shopping_cart_bt.getLocationOnScreen(location);
						bntSelectNumX = location[0] + UnitUtil.dip2px(0);
						bntSelectNumY = location[1] - UnitUtil.dip2px(75);
						takewawy_shopping_cart_iv.getLocationOnScreen(location);
						bntShoppingCartX = UnitUtil.dip2px(20) + location[0];
						bntShoppingCartY = location[1] - UnitUtil.dip2px(60);
						startTextViewAnimation(digitalFormatConversion(data.price), bntSelectNumX, bntSelectNumY, bntShoppingCartX, bntShoppingCartY, 100);
						if (data.num != 0) {
							holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource(R.drawable.shopping_cart_red);
						} else {
							holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource(R.drawable.shopping_cart_gray_bt);
						}

						setShoppingNum(data.num, data.uuid, takeoutMenuDataList);
						notifyDataSetChanged();
						// holder.takeaway_list_item_shopping_cart_num.setText(data.num
						// + "");
						// SessionManager.getInstance().addDToTakeoutCartCache(uuid,
						// data);

					}
					}
				}
			});

			holder.layoutContent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (!isAnimationRun) {

						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_FOOD_ID, data.uuid);
						bundle.putString(Settings.BUNDLE_menuSelPack, getMenuSelPack());// menuSelPack//选择的菜品json字符串
						ActivityUtil.jump(TakeAwayNewFoodListActivity.this, NewTakeAwayFoodDetailActivity.class, 0, bundle);
					}
				}
			}); 

			return convertView;
		}

		public void setList(List<TakeoutMenuData2> list) {
			this.mList = list;
			notifyDataSetChanged();
		}

		public List<TakeoutMenuData2> getList() {
			return mList;

		}

	}

	private class MySearchTakeoutAdapter extends BaseAdapter {

		private List<TakeoutMenuData2> mList = new ArrayList<TakeoutMenuData2>();
		private LayoutInflater adaperInflater;
		private long timeStamp = System.currentTimeMillis();

		public MySearchTakeoutAdapter(Context context) {
			adaperInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			LinearLayout layoutContent;
			TextView memo_tv;
			MyImageView picUrl_img;
			TextView name_tv;
			TextView price_tv;
			RatingBar overallNum_rb;
			ImageButton takeaway_list_item_shopping_cart_bt;
			TextView takeaway_list_item_shopping_cart_num;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {
				convertView = adaperInflater.inflate(R.layout.list_item_new_take_away_list, null);
				holder = new ViewHolder();

				holder.layoutContent = (LinearLayout) convertView.findViewById(R.id.layout_content);

				holder.memo_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvTitle);
				holder.picUrl_img = (MyImageView) convertView.findViewById(R.id.takeaway_list_item_ivFoodPic);
				holder.name_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvName);
				holder.price_tv = (TextView) convertView.findViewById(R.id.takeaway_list_item_tvPrice);
				holder.overallNum_rb = (RatingBar) convertView.findViewById(R.id.takeaway_list_item_overall_num);
				holder.takeaway_list_item_shopping_cart_bt = (ImageButton) convertView.findViewById(R.id.takeaway_list_item_shopping_cart_bt);
				holder.takeaway_list_item_shopping_cart_num = (TextView) convertView.findViewById(R.id.takeaway_list_item_shopping_cart_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TakeoutMenuData2 data = mList.get(position);

			if (TextUtils.isEmpty(data.picUrl)) {
				holder.picUrl_img.setVisibility(View.INVISIBLE);
			} else {
				holder.picUrl_img.setImageByUrl(data.picUrl, true, 0, ScaleType.FIT_XY);
				/*
				 * holder.picUrl_img.setImageByUrl(data.picUrl, true, 0,
				 * ScaleType.FIT_XY, new Runnable() {
				 * 
				 * @Override public void run() { // 菜品显示大图 if
				 * (!TextUtils.isEmpty(data.bigPicUrl)) {
				 * 
				 * holder.picUrl_img.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View view) {
				 * DialogUtil.createImageViewPanel
				 * (TakeAwayFoodListActivity.this, (View) view.getParent(),
				 * data.bigPicUrl); } }); } else {
				 * holder.picUrl_img.setClickable(false); } } });
				 */
				holder.picUrl_img.setVisibility(View.VISIBLE);
			}

			// if (data.spicyTag) {
			// holder.spicyTag_tv.setVisibility(View.VISIBLE);
			// } else {
			// holder.spicyTag_tv.setVisibility(View.GONE);
			// }

			// if (data.specialPriceTag) {
			// holder.specialPriceTag_tv.setVisibility(View.VISIBLE);
			// } else {
			// holder.specialPriceTag_tv.setVisibility(View.GONE);
			// }

			holder.overallNum_rb.setRating((float) data.overallNum);

			holder.memo_tv.setVisibility(View.GONE);

			holder.name_tv.setText(data.name);
			holder.price_tv.setText("￥" + digitalFormatConversion(data.price));

			if (data.num == 0) {
				holder.takeaway_list_item_shopping_cart_num.setVisibility(View.GONE);
			} else {
				holder.takeaway_list_item_shopping_cart_num.setVisibility(View.VISIBLE);
				holder.takeaway_list_item_shopping_cart_num.setText(data.num + "");
			}

			holder.takeaway_list_item_shopping_cart_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("购物车按钮", "");
					// -----
					// holder.takeaway_list_item_shopping_cart_bt.setBackgroundResource(R.drawable.shopping_cart_red);

					if(takeoutMenuListPack2DTO.canOrderTag){
					if (data.propertyTypeList != null && data.propertyTypeList.size() != 0) {
						setAttributeDate(data);
						attributePopupwindow = showAttributePopupwindow(TakeAwayNewFoodListActivity.this, null, viewProperty);
					} else {
						if (data.num == 0) {
							// 生成一个购物车对象
							data.num = data.num + 1;
							TakeoutMenuSelData takeoutMenSelDate = new TakeoutMenuSelData();
							takeoutMenSelDate.name = data.name;
							takeoutMenSelDate.uuid = data.uuid;
							takeoutMenSelDate.num = data.num;
							takeoutMenSelDate.price = data.price;
							takeoutMenSelDate.typeTag = 1;
							takeoutMenSelDate.canSelGiftTag = false;
							takeoutMenSelDate.giftTypeId = "";
							takeoutMenSelDate.nameColor = "";
							takeoutMenSelDate.canShowNumTag = true;
							takeoutMenSelDate.canChangeNumTag = true;
							takeoutMenSelDate.selPropertyHint = "";

							takeoutMenSelDate.dataIdentifer = CheckUtil.randomString(10);// 生成一个随机数字符串
							TakeoutMenuSeList.add(takeoutMenSelDate);// 放到购物车列表中

						} else {
							// 不需要生成新的购物车 改变原来购物车的数量
							data.num = data.num + 1;
							setTakeoutMenuSelNum(data.uuid, data.num);

						}
						holder.takeaway_list_item_shopping_cart_num.setVisibility(View.VISIBLE);

						setShoppingNum(data.num, data.uuid, takeoutMenuDataList);
						notifyDataSetChanged();
						// holder.takeaway_list_item_shopping_cart_num.setText(data.num
						// + "");
						// SessionManager.getInstance().addDToTakeoutCartCache(uuid,
						// data);
						showShoppingMessage();

					}
					}
				}
			});

			// holder.dg.setDefaultValue(0);
			// holder.dg.setMinValue(0);
			// holder.dg.setMaxValue(99);
			// holder.dg.setMaxWarning("已到最大值");
			// holder.dg.setMinWarning("");
			// if (data.num > 0) {
			// holder.dg.setDigitalValue(data.num);
			// } else {
			// holder.dg.setDigitalValue(0);
			// }
			// DigitalSelector.isEnableLongClick = false;
			// // holder.dg.setBackgroundResource(R.drawable.zy_bg_digital1);
			// if (!takeoutMenuListPack2DTO.canOrderTag) {
			// DigitalSelector.isEnableClick = false;
			// } else {
			// DigitalSelector.isEnableClick = true;
			// holder.dg.setOnDigitChangeListener(new
			// DigitalSelector.OnDigitChangeListener() {
			//
			// @Override
			// public void onChange(DigitalSelector selector, int digit, int
			// previousValue) {
			// DigitalSelector.isEnableClick = false;
			// // int[] location = new int[2];
			// // holder.price_tv.getLocationOnScreen(location);
			// // bntSelectNumX = location[0] + UnitUtil.dip2px(0);
			// // bntSelectNumY = location[1] - UnitUtil.dip2px(75);
			// // bntShoppingCart.getLocationOnScreen(location);
			// // bntShoppingCartX = UnitUtil.dip2px(60) + location[0];
			// // bntShoppingCartY = location[1] - UnitUtil.dip2px(60);
			// data.num = digit;
			// // SessionManager.getInstance().addDToTakeoutCartCache(uuid,
			// // data);
			// // if (selector.getMinus().isPressed()) {
			// // startTextViewAnimation(digitalFormatConversion(data.price),
			// // bntShoppingCartX, bntShoppingCartY, bntSelectNumX,
			// // bntSelectNumY, 100);
			// // }
			// // if (selector.getPlus().isPressed()) {
			// // startTextViewAnimation(digitalFormatConversion(data.price),
			// // bntSelectNumX, bntSelectNumY, bntShoppingCartX,
			// // bntShoppingCartY, 100);
			// // }
			//
			// showShoppingMessage(uuid);
			//
			// }
			// });
			// }

			holder.layoutContent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (!isAnimationRun) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_FOOD_ID, data.uuid);
						bundle.putString(Settings.BUNDLE_menuSelPack, getMenuSelPack());// menuSelPack//选择的菜品json字符串
						ActivityUtil.jump(TakeAwayNewFoodListActivity.this, NewTakeAwayFoodDetailActivity.class, 0, bundle);
					}
				}
			});

			return convertView;
		}

		public void setList(List<TakeoutMenuData2> list) {
			this.mList = list;
			notifyDataSetChanged();
		}

		public List<TakeoutMenuData2> getList() {
			return mList;

		}

	}

	// 购物车动画
	private void startTextViewAnimation(String text, float fromXDelta, float fromYDelta, float toXDelta, float toYDelta, float heightParabola) {
		tvAnimateHint.setText(text);
		Animation animation = new ParabolaAnimation(fromXDelta, fromYDelta, toXDelta, toYDelta, heightParabola);
		animation.setRepeatCount(0);
		animation.setDuration(500);
		animation.setFillAfter(true);
		AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
		animation.setInterpolator(accelerateDecelerateInterpolator);
		tvAnimateHint.clearAnimation();
		tvAnimateHint.startAnimation(animation);

		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				bntShoppingCart.setClickable(false);
				isAnimationRun = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				tvAnimateHint.clearAnimation();
				tvAnimateHint.setText("");
				bntShoppingCart.setClickable(true);
				showShoppingMessage();
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						DigitalSelector.isEnableClick = true;
						isAnimationRun = false;
					}
				}, 100);
			}
		});
	}

	// 购物车操作
	protected void showShoppingMessage() {
		// TakeoutMenuListPack2DTO dto =null;//
		// SessionManager.getInstance().getTakeoutCartCache(uuid);
		// if (dto == null) {
		// tvShoppingHint.setText("还差￥" +
		// digitalFormatConversion(takeoutMenuListPack2DTO.sendLimitPrice) +
		// "起送");
		// tvShoppingHint.setVisibility(View.VISIBLE);
		// if (shoppingCartDialogBnt != null) {
		// shoppingCartDialogBnt.setVisibility(View.GONE);
		// //
		// shoppingCartDialogBnt.setBackgroundResource(R.drawable.shopping_cart_2);
		// // shoppingCartDialogBnt.setPadding(UnitUtil.dip2px(50), 0,
		// // UnitUtil.dip2px(10), 0);
		// }
		// takeaway_shoppint_num_tv.setText("0份");
		// takeaway_shoppint_price_tv.setText("￥0");
		// bntShoppingCart.setVisibility(View.GONE);

		// return;
		// }
		double price = 0;
		int num = 0;
		// List<TakeoutMenuList2DTO> takeoutMenuListDTOList =
		// takeoutMenuListPack2DTO.list;
		if (takeoutMenuListPack2DTO.list == null) {
			return;
		}
		// TakeoutMenuSeList
		for (int i = 0; i < TakeoutMenuSeList.size(); i++) {
			double p = TakeoutMenuSeList.get(i).price;
			if (TakeoutMenuSeList.get(i).propertyTypeList != null) {
				for (int j = 0; j < TakeoutMenuSeList.get(i).propertyTypeList.size(); j++) {
					if (TakeoutMenuSeList.get(i).propertyTypeList.get(j).list != null) {
						for (int k = 0; k < TakeoutMenuSeList.get(i).propertyTypeList.get(j).list.size(); k++) {
							if (TakeoutMenuSeList.get(i).propertyTypeList.get(j).list.get(k).isSelected){
							p += TakeoutMenuSeList.get(i).propertyTypeList.get(j).list.get(k).price;
							}
						}
					}
				}
			}
			
			if(TakeoutMenuSeList.get(i).typeTag==1){
			price += (p * TakeoutMenuSeList.get(i).num);
			}
			if(TakeoutMenuSeList.get(i).typeTag==1){
			num += TakeoutMenuSeList.get(i).num;
			}
		}
		if ((takeoutMenuListPack2DTO.sendLimitPrice - price) > 0 && price != 0) {
			double p=takeoutMenuListPack2DTO.sendLimitPrice - price;
			tvShoppingHint.setText("还差￥" +digitalFormatConversion(p) + "起送");
			tvShoppingHint.setVisibility(View.VISIBLE);
			if (shoppingCartDialogBnt != null) {
				shoppingCartDialogBnt.setVisibility(View.GONE);
			}
			bntShoppingCart.setVisibility(View.GONE);
		} else {
			if (price == 0) {
				double p=takeoutMenuListPack2DTO.sendLimitPrice - price;
				tvShoppingHint.setText("还差￥" + digitalFormatConversion(p) + "起送");
				tvShoppingHint.setVisibility(View.GONE);
				if (shoppingCartDialogBnt != null) {
					shoppingCartDialogBnt.setVisibility(View.GONE);
				}
				bntShoppingCart.setVisibility(View.GONE);
			} else {
				bntShoppingCart.setVisibility(View.VISIBLE);
				if (shoppingCartDialogBnt != null) {
					shoppingCartDialogBnt.setVisibility(View.VISIBLE);
				}
				tvShoppingHint.setVisibility(View.GONE);
			}

		}
		// bntShoppingCart.setPadding(UnitUtil.dip2px(50), 0,
		// UnitUtil.dip2px(10), 0);
		takeaway_shoppint_num_tv.setText(num + "份");
		takeaway_shoppint_price_tv.setText("￥" + digitalFormatConversion(price));
		// bntShoppingCart.setText(num + "份  " + "￥" +
		// digitalFormatConversion(price));
		// ----------------------同步弹出层---------------------------
		if (shoppingHintDialogTv != null) {
			shoppingHintDialogTv.setVisibility(tvShoppingHint.getVisibility());
			shoppingHintDialogTv.setText(tvShoppingHint.getText().toString());
		}

		if (shoppingCartDialogBnt != null) {
			// shoppingCartDialogBnt.setPadding(UnitUtil.dip2px(50), 0,
			// UnitUtil.dip2px(10), 0);
			takeaway_shoppint_num_dialog_tv.setText(takeaway_shoppint_num_tv.getText().toString());
			takeaway_shoppint_price_dialog_tv.setText(takeaway_shoppint_price_tv.getText().toString());

			shoppingCartDialogBnt.setVisibility(bntShoppingCart.getVisibility());
			shoppingCartDialogBnt.setText(bntShoppingCart.getText().toString());

			// shoppingHintDialogTv.setVisibility(View.GONE);
			// shoppingCartDialogBnt.setText(bntShoppingCart.getText().toString());
			shoppingCartDialogBnt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("去买单按钮", "");
					// -----

					if (shoppingCartDialogBnt != null && bntShoppingCart.isClickable()) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.UUID, uuid);
						bundle.putString(Settings.BUNDLE_menuSelPack, getMenuSelPack());// menuSelPack//选择的菜品json字符串
						ActivityUtil.jump(TakeAwayNewFoodListActivity.this, TakeAwayMyOrderActivity.class, 989, bundle);
						if (popupWindow != null) {
							popupWindow.dismiss();
						}
					}
				}
			});
		}

		bntShoppingCart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("去买单按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, uuid);
				bundle.putString(Settings.BUNDLE_menuSelPack, getMenuSelPack());// menuSelPack//选择的菜品json字符串
				ActivityUtil.jump(TakeAwayNewFoodListActivity.this, TakeAwayMyOrderActivity.class, 989, bundle);
			}
		});

	}

	// 本地处理接口数据 已适用于本程序的功能
	private List<TakeoutMenuData2> dealWithTakeoutMenuData(List<TakeoutMenuList2DTO> list) {
		boolean isCacheEmpty = false;
		int index = 0;
		TakeoutMenuListPack2DTO takeoutMenuListPackDTO = null;// SessionManager.getInstance().getTakeoutCartCache(uuid);
		List<TakeoutMenuData2> takeoutMenuDatas = null;
		if (takeoutMenuListPackDTO != null) {
			isCacheEmpty = true;
			takeoutMenuDatas = takeoutMenuListPackDTO.list.get(0).list;
		}

		List<TakeoutMenuData2> list_data = new ArrayList<TakeoutMenuData2>();
		for (int i = 0; i < list.size(); i++) {
			TakeoutMenuList2DTO takeoutMenuListDTO = list.get(i);
			takeoutMenuListDTO.startIndex = index;
			takeoutMenuListDTO.endIndex = takeoutMenuListDTO.list.size() + index;
			List<TakeoutMenuData2> list_TakeoutMenuData = takeoutMenuListDTO.list;

			for (int j = 0; j < list_TakeoutMenuData.size(); j++) {
				TakeoutMenuData2 takeoutMenuData = list_TakeoutMenuData.get(j);
				takeoutMenuData.soTypeMemo = takeoutMenuListDTO.typeData.detail;
				takeoutMenuData.soIsFirst = j == 0 ? true : false;
				takeoutMenuData.soGroupId = getSpecialType(takeoutMenuListDTO.typeData);
				takeoutMenuData.isFavTag = takeoutMenuListDTO.typeData.favTag;
				if (isCacheEmpty) {
					for (int k = 0; k < takeoutMenuDatas.size(); k++) {
						TakeoutMenuData2 data = takeoutMenuDatas.get(k);
						if (takeoutMenuData.uuid.equals(data.uuid)) {
							// takeoutMenuData.selectedNum = data.num;
							takeoutMenuData.num = data.num;
						}
					}
				}
				list_data.add(takeoutMenuData);
			}
			index += takeoutMenuListDTO.list.size();
		}

		return list_data;
	}

	private String digitalFormatConversion(double price) {
//		Log.v("TAG", price+"=dprice");
//		String parten = "#.#";
//		DecimalFormat decimal = new DecimalFormat(parten);
//		String showPrice = decimal.format(price);
//		if (showPrice.contains("\\.")) {
//			String[] arrPrice = showPrice.split("\\.");
//			Log.v("TAG", arrPrice[0]+"=arrPrice[0]"+arrPrice[1]+"=arrPrice[1]");
//			if (!arrPrice[1].equals("0") && !arrPrice[1].equals("00")) {
//				showPrice = arrPrice[0] + "." + arrPrice[1];
//			} else {
//				showPrice = arrPrice[0];
//			}
//		}
//		Log.v("TAG", showPrice+"=showPrice");
		String showPrice="";
		if(price % 1 == 0){
			int i=(int) price;
			showPrice=i+"";
		}else{
			showPrice=price+"";
		}
		BigDecimal bd = new BigDecimal(showPrice);
		bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);  
//		new BigDecimal(showPrice).stripTrailingZeros();
		return bd.toString();
	}

	private List<TakeoutMenuData2> searchTakeoutData(String keywords) {

		List<TakeoutMenuData2> takeoutList = new ArrayList<TakeoutMenuData2>();
		if (!CheckUtil.isEmpty(keywords)) {
			for (TakeoutMenuData2 data : takeoutMenuDataList) {
				// 先比较首字母
				if (data.pinyinCap.toLowerCase().contains(keywords) && data.isFavTag == false) {
					takeoutList.add(data);
					continue;

				}
				// 比较中文
				if (data.name.toLowerCase().contains(keywords) && data.isFavTag == false) {
					takeoutList.add(data);
					continue;
				}
				// 先比较pinyin
				if (data.pinyin.toLowerCase().contains(keywords) && data.isFavTag == false) {
					takeoutList.add(data);
					continue;
				}

			}
		}

		return takeoutList;

	}

	// 初始化搜索PopupWindow控件
	private void initPopupView() {
		view = LayoutInflater.from(TakeAwayNewFoodListActivity.this).inflate(R.layout.new_takeaway_search_gray_layout, null);

		topLayout = (RelativeLayout) view.findViewById(R.id.takeaway_top_layout);
		centerLayout = (RelativeLayout) view.findViewById(R.id.takeaway_center_layout);
		buttomLayout = (RelativeLayout) view.findViewById(R.id.takeaway_bottom_layout);
		cancelBnt = (TextView) view.findViewById(R.id.takeaway_search_cancel_bnt);
		searchEdit = (AutoCompleteTextView) view.findViewById(R.id.takeaway_search_edt);
		resetBnt = (Button) view.findViewById(R.id.takeaway_search_reset_bnt);
		listView = (ListView) view.findViewById(R.id.takeaway_list_food_lv);
		noDataTv = (TextView) view.findViewById(R.id.takeaway_search_no_tv);
		shoppingHintDialogTv = (TextView) view.findViewById(R.id.takewawy_shopping_hint_tv);
		shoppingCartDialogBnt = (Button) view.findViewById(R.id.takewawy_shopping_cart_bnt);
		takeaway_shoppint_num_dialog_tv = (TextView) view.findViewById(R.id.takeaway_shoppint_num_tv);
		takeaway_shoppint_price_dialog_tv = (TextView) view.findViewById(R.id.takeaway_shoppint_price_tv);

	}

	// 初始化菜品属性PopupWindow控件
	private void initAttributePopupView() {
		viewProperty = LayoutInflater.from(TakeAwayNewFoodListActivity.this).inflate(R.layout.takeaway_attribut_popup_view, null);
		takeaway_property_name = (TextView) viewProperty.findViewById(R.id.takeaway_property_name);
		takeaway_property_total_price = (TextView) viewProperty.findViewById(R.id.takeaway_property_total_price);
		takeaway_property_cannel = (ImageView) viewProperty.findViewById(R.id.takeaway_property_cannel);
		takeaway_property_view = (LinearLayout) viewProperty.findViewById(R.id.takeaway_property_view);
		takeaway_property_diaitalselector = (DigitalSelector) viewProperty.findViewById(R.id.takeaway_property_diaitalselector);
		takeaway_property_bt = (Button) viewProperty.findViewById(R.id.takeaway_property_bt);

		// takeaway_property_cannel.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// attributePopupwindow.dismiss();
		// }
		// });
		//
		// takeaway_property_bt.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// attributePopupwindow.dismiss();
		// adapter.notifyDataSetChanged();
		// if (searchAdapter != null) {
		// searchAdapter.notifyDataSetChanged();
		// }
		//
		// // 有属性配料的 需要弹出属性配料选择 选好了 生成一个购物车
		// showShoppingMessage(uuid);
		// TakeoutMenuSelData takeoutMenSelDate = new TakeoutMenuSelData();
		// takeoutMenSelDate.name = data.name;
		// takeoutMenSelDate.uuid=data.uuid;
		// takeoutMenSelDate.num=data.num;
		// takeoutMenSelDate.dataIdentifer=CheckUtil.randomString(10);//生成一个随机数字符串
		// takeoutMenuSelPackDTO.list.add(takeoutMenSelDate);//放到购物车列表中
		//
		// }
		// });

	}

	/**
	 * 显示菜品属性选择Popupwindow
	 * 
	 * @param context
	 * @param parent
	 * @param child
	 */
	private PopupWindow showAttributePopupwindow(Context context, View parent, View child) {
		if (parent == null) {
			parent = ((Activity) context).getWindow().getDecorView();
		}

		// 灰色背景遮罩
		LinearLayout bgView = new LinearLayout(context);
		bgView.setOrientation(LinearLayout.VERTICAL);
		bgView.setBackgroundColor(0xb5555555);
		bgView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		bgView.setLayoutParams(params);
		final PopupWindow popBg = new PopupWindow(bgView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出层显示的内容
		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popMain.setBackgroundDrawable(new BitmapDrawable());
		popMain.setOutsideTouchable(true);
		popMain.setFocusable(true);
		popMain.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
		popMain.setClippingEnabled(true);

		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popBg.isShowing()) {
					popBg.dismiss();
				}
			}
		});

		popMain.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		return popMain;
	}

	/**
	 * 设置Popupwindow菜品属性
	 */
	private void setAttributeDate(TakeoutMenuData2 data1) {
		if (data1 == null) {
			return;
		}
		takeoutMenuData2 = data1;

		if (takeaway_property_view.getChildCount() != 0) {
			takeaway_property_view.removeAllViews();
		}
		// 重置菜品数量
		num = 1;
		// 本身价格
		totalPrice = takeoutMenuData2.price;
		// 设置菜品名称
		takeaway_property_name.setText(takeoutMenuData2.name);
		propertyTypeList = takeoutMenuData2.propertyTypeList;

		// if (data.num == 0) {
		// data.num = data.num + 1;
		// takeaway_property_total_price.setText("￥" + totalPrice * data.num);
		// } else {
		// takeaway_property_total_price.setText("￥" + totalPrice * data.num);
		// }
		// takeaway_property_diaitalselector.setValue(data.num);

		// 重置总价格 数量 是否选择配料
		takeaway_property_total_price.setText("￥" + totalPrice * num);
		takeaway_property_diaitalselector.setValue(num);
		for (int i = 0; i < propertyTypeList.size(); i++) {
			for (int m = 0; m < propertyTypeList.get(i).list.size(); m++) {
				propertyTypeList.get(i).list.get(m).isSelected = false;
			}
		}

		for (int j = 0; j < propertyTypeList.size(); j++) {
			final MyViewGroup myViewGroup = new MyViewGroup(this);
			myViewGroup.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			// final List<TakeoutMenuPropertyData> listTakeoutMenuPropertyData =
			// propertyTypeList.get(j).list;
			for (int i = 0; i < propertyTypeList.get(j).list.size(); i++) {
				final Button b = new Button(this);
				b.setBackgroundResource(R.drawable.huikuang);
				b.setPadding(20, 0, 20, 0);
				// final TakeoutMenuPropertyData takeoutMenuPropertyData =
				// propertyTypeList.get(j).list.get(i);

				b.setText(propertyTypeList.get(j).list.get(i).name);

				b.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				b.setTextSize(20);
				b.setGravity(Gravity.CENTER);

				final int q = j;
				final int p = i;
				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(view, 1000);
						if (propertyTypeList.get(q).list.get(p).isSelected) {
							b.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
							b.setBackgroundResource(R.drawable.huikuang);
							b.setPadding(20, 0, 20, 0);
							propertyTypeList.get(q).list.get(p).isSelected = false;
						} else {
							for (int m = 0; m < propertyTypeList.get(q).list.size(); m++) {
								propertyTypeList.get(q).list.get(m).isSelected = false;
							}
							for (int m = 0; m < myViewGroup.getChildCount(); m++) {
								Button button = (Button) myViewGroup.getChildAt(m);
								button.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
								button.setBackgroundResource(R.drawable.huikuang);
								button.setPadding(20, 0, 20, 0);

							}

							b.setTextColor(getResources().getColor(R.color.new_text_color_red));
							b.setBackgroundResource(R.drawable.redkuang);
							b.setPadding(20, 0, 20, 0);
							propertyTypeList.get(q).list.get(p).isSelected = true;
						}

						// 重置单价 重新算新的总价
						totalPrice = takeoutMenuData2.price;
						for (int k = 0; k < propertyTypeList.size(); k++) {
							for (int l = 0; l < propertyTypeList.get(k).list.size(); l++) {
								if (propertyTypeList.get(k).list.get(l).isSelected) {
									totalPrice = totalPrice + propertyTypeList.get(k).list.get(l).price;
								}
							}
						}

						num = takeaway_property_diaitalselector.getValue();
						takeaway_property_total_price.setText("￥" + totalPrice * num);

					}
				});
				myViewGroup.addView(b);
			}
			LinearLayout view = new LinearLayout(this);
			LinearLayout.LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 1);
			view.setBackgroundColor(Color.parseColor("#cccccc"));
			view.setLayoutParams(params);

			takeaway_property_view.addView(myViewGroup);
			takeaway_property_view.addView(view);
		}

		// 设置总价格
		takeaway_property_diaitalselector.setOnDigitChangeListener(new OnDigitChangeListener() {

			@Override
			public void onChange(DigitalSelector selector, int digit, int previousValue) {
				// TODO Auto-generated method stub
				num = digit;
				takeaway_property_total_price.setText("￥" + totalPrice * digit);
			}
		});

		// 取消
		takeaway_property_cannel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				attributePopupwindow.dismiss();
			}
		});
		// 确定
		takeaway_property_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 改变该商品总购物数量
				takeoutMenuData2.num = takeoutMenuData2.num + num;
				// attributePopupwindow.dismiss();
				// adapter.notifyDataSetChanged();
				// if (searchAdapter != null) {
				// searchAdapter.notifyDataSetChanged();
				// }
				// showShoppingMessage();
				// 有属性配料的 需要弹出属性配料选择 选好了 生成一个购物车
				TakeoutMenuSelData takeoutMenSelDate = new TakeoutMenuSelData();
				takeoutMenSelDate.name = takeoutMenuData2.name;
				takeoutMenSelDate.uuid = takeoutMenuData2.uuid;

				takeoutMenSelDate.name = takeoutMenuData2.name;
				takeoutMenSelDate.uuid = takeoutMenuData2.uuid;
				takeoutMenSelDate.num = num;
				takeoutMenSelDate.price = takeoutMenuData2.price;
				takeoutMenSelDate.typeTag = 1;
				takeoutMenSelDate.canSelGiftTag = false;
				takeoutMenSelDate.giftTypeId = "";
				takeoutMenSelDate.nameColor = "";
				takeoutMenSelDate.canShowNumTag = true;
				takeoutMenSelDate.canChangeNumTag = true;
				takeoutMenSelDate.selPropertyHint = "";

				List<TakeoutMenuPropertyTypeData> list = new ArrayList<TakeoutMenuPropertyTypeData>();

				for (int i = 0; i < takeoutMenuData2.propertyTypeList.size(); i++) {
					TakeoutMenuPropertyTypeData takeoutMenuPropertyTypeData = new TakeoutMenuPropertyTypeData();
					List<TakeoutMenuPropertyData> list1 = new ArrayList<TakeoutMenuPropertyData>();
					takeoutMenuPropertyTypeData.name = takeoutMenuData2.propertyTypeList.get(i).name;
					takeoutMenuPropertyTypeData.uuid = takeoutMenuData2.propertyTypeList.get(i).uuid;
					for (int j = 0; j < takeoutMenuData2.propertyTypeList.get(i).list.size(); j++) {
						// 被选中才放进购物车
						if (takeoutMenuData2.propertyTypeList.get(i).list.get(j).isSelected) {
							TakeoutMenuPropertyData takeoutMenuPropertyData = new TakeoutMenuPropertyData();
							takeoutMenuPropertyData.name = takeoutMenuData2.propertyTypeList.get(i).list.get(j).name;
							takeoutMenuPropertyData.uuid = takeoutMenuData2.propertyTypeList.get(i).list.get(j).uuid;
							takeoutMenuPropertyData.price = takeoutMenuData2.propertyTypeList.get(i).list.get(j).price;
							takeoutMenuPropertyData.isSelected = takeoutMenuData2.propertyTypeList.get(i).list.get(j).isSelected;
							list1.add(takeoutMenuPropertyData);
						}
					}
					takeoutMenuPropertyTypeData.list = list1;
					list.add(takeoutMenuPropertyTypeData);
				}
				// takeoutMenSelDate.propertyTypeList =
				// takeoutMenuData2.propertyTypeList;
				takeoutMenSelDate.propertyTypeList = list;
				takeoutMenSelDate.dataIdentifer = CheckUtil.randomString(10);// 生成一个随机数字符串
				TakeoutMenuSeList.add(takeoutMenSelDate);// 放到购物车列表中

				setShoppingNum(takeoutMenuData2.num, takeoutMenSelDate.uuid, takeoutMenuDataList);

				showShoppingMessage();
				adapter.notifyDataSetChanged();
				if (searchAdapter != null) {
					searchAdapter.notifyDataSetChanged();
				}
				attributePopupwindow.dismiss();

			}
		});
	}

	/**
	 * 显示弹出层
	 * 
	 * @param context
	 * @param parent
	 *            父View
	 * @param child
	 *            弹出气泡的内容
	 * @param dismissOnTouch
	 *            是否在点击屏幕时消失
	 * @param listener
	 *            弹出层消失的监听
	 * @return
	 */
	private PopupWindow showPopupwindow(Context context, View parent, View child) {

		if (parent == null) {
			parent = ((Activity) context).getWindow().getDecorView();
		}

		// 灰色背景遮罩
		LinearLayout bgView = new LinearLayout(context);
		bgView.setOrientation(LinearLayout.VERTICAL);
		bgView.setBackgroundColor(0xb5555555);
		bgView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		bgView.setLayoutParams(params);
		final PopupWindow popBg = new PopupWindow(bgView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出层显示的内容
		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popMain.setBackgroundDrawable(new BitmapDrawable());
		popMain.setOutsideTouchable(true);
		popMain.setFocusable(true);
		popMain.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
		popMain.setClippingEnabled(true);

		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popBg.isShowing()) {
					popBg.dismiss();
				}
				if (adapter != null) {
					adapter.setList(takeoutMenuDataList);
				}
				if (lvFood != null && adapter != null) {
					lvFood.setAdapter(adapter);
				}

			}
		});

		popMain.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		return popMain;
	}

	/**
	 * 通用列表适配器
	 * 
	 * @author zhangyifan
	 * 
	 */
	public class TakeAwayAdvertisementAdapter extends BaseAdapter {

		private static final String TAG = "TakeAwayAdvertisementAdapter";
		private static final boolean DEBUG = Settings.DEBUG;
		// 最多显示的广告条目
		private int maxShowingCount = 20;
		private List<MainPageAdvData> list;
		private LayoutInflater mInflater;
		private Context context;

		// 一般列表用
		public TakeAwayAdvertisementAdapter(Context c, List<MainPageAdvData> advList) {
			super();
			this.list = advList;
			// this.list = doTest();

			this.context = c;
			mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		private List<MainPageAdvData> doTest() {
			List<MainPageAdvData> advList = new ArrayList<MainPageAdvData>();
			for (int i = 0; i < 3; i++) {
				MainPageAdvData mainPageAdvData = new MainPageAdvData();
				mainPageAdvData.title = "广告条" + i;
				advList.add(mainPageAdvData);
			}
			return advList;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			}
			if (list.size() > maxShowingCount) {
				return maxShowingCount;
			}
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public class ViewHolder {
			public TextView text; // 广告内容
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item_takeawayadvertisement, null);
				holder.text = (TextView) convertView.findViewById(R.id.advertisement_text_view);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final MainPageAdvData data = list.get(position);
			holder.text.setText(data.title);
			convertView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					try {
						// -----
						OpenPageDataTracer.getInstance().addEvent("广告位按钮", data.uuid);
						// -----
						// 广告类别 1:广告链接 2：本地连接 3:普通链接
						// 普通链接跳转到webview页面, 本地链接使用url处理器
						if (data.typeTag == 1) {
							// 广告链接，使用内嵌的WebView打开
							Bundle bd = new Bundle();
							bd.putString(Settings.BUNDLE_KEY_WEB_URL, data.advUrl);
							bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, data.title);
							ActivityUtil.jump(context, SimpleWebViewActivity.class, 0, bd);

						} else if (data.typeTag == 2) {
							// 本地链接，跳转本地界面
							URLExecutor.execute(data.advUrl, context, 0);
						} else if (data.typeTag == 3) {
							// 普通链接，使用系统浏览器打开
							ActivityUtil.jumbToWeb((Activity) context, data.advUrl);
						} else if (data.typeTag == 4) {
							// 软件链接
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_KEY_CONTENT, data.appDownloadUrl);
							bundle.putString(Settings.BUNDLE_UPDATE_APP_NAME, data.appName);
							ActivityUtil.jump(context, AutoUpdateActivity.class, 0, bundle);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			convertView.setTag(holder);
			return convertView;
		}
	}

	// // -----------------测试数据---------------------------
	// private TakeoutMenuListPackDTO test() {
	// TakeoutMenuListPackDTO takeoutMenuListPackDTO = new
	// TakeoutMenuListPackDTO();
	// String json =
	// "{\"list\":[{\"list\":[{\"uuid\":\"1111\",\"name\":\"农家小炒肉\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"1112\",\"name\":\"农家小炒肉\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"1113\",\"name\":\"农家小炒肉\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"1114\",\"name\":\"农家小炒肉\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"1115\",\"name\":\"农家小炒肉\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]}],\"typeDTO\":{\"uuid\":\"a111\",\"parentId\":\"a1111\",\"name\":\"赠可乐\",\"num\":\"2\",\"succTag\":\"true\",\"phone\":\"13000000000\",\"memo\":\"赠可乐,满25元赠可乐一瓶\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"xx\",\"firstLetters\":\"x\",\"firstLetter\":\"x\"}},{\"list\":[{\"uuid\":\"2221\",\"name\":\"番茄鸡蛋\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"2222\",\"name\":\"番茄鸡蛋\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"2223\",\"name\":\"番茄鸡蛋\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"2224\",\"name\":\"番茄鸡蛋\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"2225\",\"name\":\"番茄鸡蛋\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]}],\"typeDTO\":{\"uuid\":\"a222\",\"parentId\":\"a2222\",\"name\":\"面食类\",\"num\":\"2\",\"succTag\":\"true\",\"phone\":\"13000000000\",\"memo\":\"面食类\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"xx\",\"firstLetters\":\"x\",\"firstLetter\":\"x\"}},{\"list\":[{\"uuid\":\"3331\",\"name\":\"红烧牛肉面\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"3332\",\"name\":\"红烧牛肉面\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"3333\",\"name\":\"红烧牛肉面\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"3334\",\"name\":\"红烧牛肉面\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]},{\"uuid\":\"3335\",\"name\":\"红烧牛肉面\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"price\":\"5\",\"currentPriceTag\":\"true\",\"priceUnit\":\"元/份\",\"overallNum\":\"3\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"0\",\"gradeNum\":\"1\",\"numPerct\":\"10\"}]}],\"typeDTO\":{\"uuid\":\"a333\",\"parentId\":\"a3333\",\"name\":\"盖饭类\",\"num\":\"2\",\"succTag\":\"true\",\"phone\":\"13000000000\",\"memo\":\"盖饭类\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"xx\",\"firstLetters\":\"x\",\"firstLetter\":\"x\"}}],\"takeoutData\":{\"uuid\":\"b111\",\"favTag\":\"false\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"name\":\"清蒸牛肉馆\",\"overallNum\":\"2\",\"sendLimitPrice\":\"20\",\"sendReachMins\":\"16\",\"address\":\"东方路117号\",\"longitude\":\"0\",\"latitude\":\"0\",\"openTimeInfo\":\"9:00\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"false\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"}],\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"p111\",\"userName\":\"一枝花\",\"userPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"createTime\":\"2013-11-7\",\"overallNum\":\"5\",\"detail\":\"好吃好吃好吃吃吃\",\"replyInfo\":\"继续吃\"},\"sendTimeList\":[{\"uuid\":\"t111\",\"parentId\":\"t1111\",\"name\":\"16\",\"num\":\"0\",\"succTag\":\"false\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"x\",\"firstLetter\":\"q\"},{\"uuid\":\"t222\",\"parentId\":\"t2222\",\"name\":\"17点\",\"num\":\"0\",\"succTag\":\"false\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"x\",\"firstLetter\":\"q\"},{\"uuid\":\"t333\",\"parentId\":\"t333\",\"name\":\"18点\",\"num\":\"0\",\"succTag\":\"false\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"x\",\"firstLetter\":\"q\"},{\"uuid\":\"t444\",\"parentId\":\"t4444\",\"name\":\"19点\",\"num\":\"0\",\"succTag\":\"false\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"x\",\"firstLetter\":\"q\"}],\"stateName\":\"状态名称\",\"stateColor\":\"状态颜色\",\"detail\":\"餐厅介绍\",\"menuType\":\"菜系类型\",\"canOrderTag\":\"true\",\"hintForCanNotOrder\":\"不能下单的提示\",\"vipTag\":\"true\",\"canShowConfrimBtnTag\":\"true\",\"hintForCanNotConfirm\":\"不能确认的提示\"},\"userReceiveAdressData\":{\"uuid\":\"u111\",\"parentId\":\"t1111\",\"name\":\"东方路117号\",\"num\":\"0\",\"succTag\":\"false\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"x\",\"firstLetter\":\"q\"}}";
	// takeoutMenuListPackDTO = new Gson().fromJson(json,
	// TakeoutMenuListPackDTO.class);
	// return takeoutMenuListPackDTO;
	// }
	/**
	 * 获取购物车JSon字符串
	 */
	private String getMenuSelPack() {
		TakeoutMenuSelPackDTO takeoutMenuSelPackDTO = new TakeoutMenuSelPackDTO();
		takeoutMenuSelPackDTO.list = TakeoutMenuSeList;
		String menuSelPack = JsonUtils.toJson(takeoutMenuSelPackDTO, null, true, null, null, false);

		return menuSelPack;

	}

	/**
	 * 改变购物车的中菜品数量（适用没有属性）
	 */
	private void setTakeoutMenuSelNum(String uuid, int num) {
		for (int i = 0; i < TakeoutMenuSeList.size(); i++) {
			if (TakeoutMenuSeList.get(i).uuid.equals(uuid)) {
				TakeoutMenuSeList.get(i).num = num;
			}
		}
	}

	// /**
	// * 改变购物车的中菜品数量(有属性)
	// */
	// private void setTakeoutMenuSelNum2(String dataIdentifer, int num) {
	// for (int i = 0; i < TakeoutMenuSeList.size(); i++) {
	// if (TakeoutMenuSeList.get(i).dataIdentifer.equals(dataIdentifer)) {
	// TakeoutMenuSeList.get(i).num = num;
	// }
	// }
	// }

	/**
	 * 保持我的最爱中数量准确（适用没有属性）
	 * 
	 * @param num
	 * @param uuid
	 */
	private void setShoppingNum(int num, String uuid, List<TakeoutMenuData2> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).uuid.equals(uuid)) {
				list.get(i).num = num;
			}
		}
	}

	/**
	 * 改变显示数量
	 */
	private void setTakeoutMenuDataList(String uuid, int num) {

		// 重新根据UUID加出购物车数量
		for (int i = 0; i < takeoutMenuDataList.size(); i++) {
			if (takeoutMenuDataList.get(i).uuid.equals(uuid)) {
				takeoutMenuDataList.get(i).num = takeoutMenuDataList.get(i).num + num;
			}
		}
	}

	/**
	 * 关闭Activity
	 */
	public void finishActivity() {
		finish();
	}
}
