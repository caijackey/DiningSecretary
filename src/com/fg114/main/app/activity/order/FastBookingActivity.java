package com.fg114.main.app.activity.order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.adapter.AutoCompleteRestSuggestAdapter;
import com.fg114.main.app.adapter.UsedHistorySuggestListAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.view.OrderSelectionWheelView;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.service.dto.RestSearchSuggestListDTO;
import com.fg114.main.service.dto.RoomTypeInfoData;
import com.fg114.main.service.dto.SuggestResultData;
import com.fg114.main.service.dto.UsedHistorySuggestListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.GetRestSearchSuggestListTask;
import com.fg114.main.service.task.GetUsedHistorySuggestListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 快捷预定
 * 
 * @author lijian
 * 
 */
public class FastBookingActivity extends MainFrameActivity {

	private static final String TAG = "FastBookingActivity";
	public static final int CAMERAIMAGE = 9999; // 拍照上传
	public static final int LOCALIMAGE = 9998; // 本地上传
	private OrderSelectionWheelView orderInfoWheel;
	private Button orderSubmit;
	private ViewGroup fast_booking_search_text_layout;
	private Button fast_booking_search_right_button;
	private TextView fast_booking_search_text_view;
	private ViewGroup fast_booking_top_layout;
	private ViewGroup fast_booking_bottom_layout;
	private ImageView button_order_phone_call;
	private TextView fast_booking_search_order_hint_text;
	private ImageView button_order_bottom_line_image;
	private View contentView;

	
	private long DateMilliSeconds;
	private long TimeMilliSeconds;
	private long PeopleNum;
	private long RoomType;
	
	private boolean isOpenSearch=false;//是否打开搜索页面
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("快捷预订", "");
		// ----------------------------
		super.onCreate(savedInstanceState);
		contentView = View.inflate(this, R.layout.fast_booking, null);
		this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

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

	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("快捷预订", "");
		// ----------------------------
	}

	@Override
	public void onResume() {
		super.onResume();

		Settings.CURRENT_PAGE = getClass().getSimpleName();

		initCityIssues();
		setDefaultRest();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if(orderInfoWheel!=null){
			isOpenSearch=true;
			DateMilliSeconds=orderInfoWheel.getDateMilliSeconds();
			TimeMilliSeconds=orderInfoWheel.getTimeMilliSeconds();
			PeopleNum=orderInfoWheel.getPeopleNum();
			RoomType=orderInfoWheel.getRoomType();
		}
		
	}

	// 设置默认餐厅
	private void setDefaultRest() {
		// 设置上次餐厅
		SuggestResultData defaultRest = SessionManager.getInstance().getLastSelectedRest();
		if (defaultRest == null)
			getSuggestRDataAccord2Net();
		else
			setRestOrderInfo(defaultRest);
	}

	// ----------第一次进界面 通过网络获取餐厅信息------------------------------
	/*
	 * 获得快捷预订页面信息，返回SuggestResultData getQuickOrderInfo("/getQuickOrderInfo",
	 * new ParamProtocol() ),
	 */
	// ----------------------------------------
	private void getSuggestRDataAccord2Net() {
		// --------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// --------------

		ServiceRequest request = new ServiceRequest(API.getQuickOrderInfo);
		CommonTask.request(request, "正在加载数据...", new CommonTask.TaskListener<SuggestResultData>() {

			@Override
			protected void onSuccess(SuggestResultData dto) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				setRestOrderInfo(dto);
			}

			@Override
			protected void onError(int code, String message) {
				// doTest();
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
			}

			private void doTest() {
				String json = "{\"restId\":\"AESH10010846\",\"iconTag\":\"1\",\"restName\":\"御花园酒店\",\"iconTitle\":\"75折券\",\"hisTypeTag\":\"1\",\"stateTag\":\"1\",\"restTelForShow\":\"57575777\",\"restTelForCall\":\"57575777\",\"orderSelInfo\":{\"maxDayNum\":\"90\",\"maxPeopleNum\":\"49\"}}";
				SuggestResultData data = JsonUtils.fromJson(json, SuggestResultData.class);
				onSuccess(data);
			}
		});
	}

	private ImageView promotion_icon_mibi;
	private TextView promotion_mibi;
	private TextView promotion_discount;
	private TextView promotion_coupon;
	protected View res_food_list_item_promotion_icon_mibi;

	/**
	 * 初始化界面
	 */
	private void initComponent() {

		this.getTvTitle().setText("快捷预订");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		orderInfoWheel = (OrderSelectionWheelView) findViewById(R.id.order_info_selection_wheel);
		fast_booking_top_layout = (ViewGroup) findViewById(R.id.fast_booking_top_layout);
		fast_booking_bottom_layout = (ViewGroup) findViewById(R.id.fast_booking_bottom_layout);
		fast_booking_search_text_layout = (ViewGroup) findViewById(R.id.fast_booking_search_text_layout);
		fast_booking_search_right_button = (Button) findViewById(R.id.fast_booking_search_right_button);
		fast_booking_search_text_view = (TextView) findViewById(R.id.fast_booking_search_text_view);
		// 优惠四控件
		promotion_icon_mibi = (ImageView) findViewById(R.id.promotion_icon_mibi);
		promotion_mibi = (TextView) findViewById(R.id.promotion_mibi);
		promotion_discount = (TextView) findViewById(R.id.promotion_discount);
		promotion_coupon = (TextView) findViewById(R.id.promotion_coupon);
		//
		orderSubmit = (Button) findViewById(R.id.button_order_submit);

		button_order_phone_call = (ImageView) findViewById(R.id.button_order_phone_call);
		fast_booking_search_order_hint_text = (TextView) findViewById(R.id.fast_booking_search_order_hint_text);
		button_order_bottom_line_image = (ImageView) findViewById(R.id.button_order_bottom_line_image);

		promotion_icon_mibi.setVisibility(View.GONE);
		promotion_mibi.setVisibility(View.GONE);
		promotion_discount.setVisibility(View.GONE);
		promotion_coupon.setVisibility(View.GONE);
		// ---------------------------------------------------------------------------------------------------------
		// 搜索
		fast_booking_search_text_layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅搜索输入框");
				// -----
				
				if(orderInfoWheel!=null){
					isOpenSearch=true;
					DateMilliSeconds=orderInfoWheel.getDateMilliSeconds();
					TimeMilliSeconds=orderInfoWheel.getTimeMilliSeconds();
					PeopleNum=orderInfoWheel.getPeopleNum();
					RoomType=orderInfoWheel.getRoomType();
				}
				
				showSearchAnimation();

				fast_booking_top_layout.postDelayed(new Runnable() {

					@Override
					public void run() {
						showSearchRestDialog("搜索");
					}
				}, 200);
			}

		});
		// 历史
		fast_booking_search_right_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("历史记录按钮");
				// -----
				if(orderInfoWheel!=null){
					isOpenSearch=true;
					DateMilliSeconds=orderInfoWheel.getDateMilliSeconds();
					TimeMilliSeconds=orderInfoWheel.getTimeMilliSeconds();
					PeopleNum=orderInfoWheel.getPeopleNum();
					RoomType=orderInfoWheel.getRoomType();
				}
				
				showSearchAnimation();

				fast_booking_top_layout.postDelayed(new Runnable() {

					@Override
					public void run() {
						showSearchRestDialog("历史");
					}
				}, 200);
			}
		});
		orderSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (fast_booking_search_text_layout.getTag() == null) {

					DialogUtil.showAlert(FastBookingActivity.this, "提示", "请选择一家餐厅");
					return;
				}

				// 跳下单页
				SuggestResultData data = (SuggestResultData) fast_booking_search_text_layout.getTag();
				// -----
				OpenPageDataTracer.getInstance().addEvent("提交订单按钮", data.restId);
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID, data.restId);
				bundle.putString(Settings.BUNDLE_REST_NAME, data.restName);
				bundle.putLong(Settings.BUNDLE_ORDER_TIME,
				orderInfoWheel.getDateMilliSeconds() + orderInfoWheel.getTimeMilliSeconds());
				bundle.putLong(Settings.BUNDLE_ORDER_PEOPLE_NUM, orderInfoWheel.getPeopleNum());
				bundle.putLong(Settings.BUNDLE_ORDER_ROOM_TYPE, orderInfoWheel.getRoomType());
				ActivityUtil.jump(FastBookingActivity.this, MyBookRestaurantActivity.class, 0, bundle);

			}
		});

	}

	// private void clearAnimation() {
	// if (fast_booking_top_bar.getAnimation() != null) {
	// fast_booking_top_bar.getAnimation().cancel();
	// }
	// if (fast_booking_top_layout.getAnimation() != null) {
	// fast_booking_top_layout.getAnimation().cancel();
	// }
	// if (fast_booking_bottom_layout.getAnimation() != null) {
	// fast_booking_bottom_layout.getAnimation().cancel();
	// }
	//
	// fast_booking_top_bar.clearAnimation();
	// fast_booking_top_layout.clearAnimation();
	// fast_booking_bottom_layout.clearAnimation();
	// }

	private void showSearchAnimation() {
		super.getTitleLayout().startAnimation(
				AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_out_top));
		fast_booking_top_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.index_slide_out_top));
		fast_booking_bottom_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.index_slide_out_bottom));

	}

	private void hideSearchAnimation() {

		contentView.postDelayed(new Runnable() {

			@Override
			public void run() {
				contentView.postInvalidate();
			}
		}, 100);
		super.getTitleLayout().startAnimation(
				AnimationUtils.loadAnimation(getApplicationContext(), R.anim.index_slide_in_top));
		fast_booking_top_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.index_slide_in_top));
		fast_booking_bottom_layout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.index_slide_in_bottom));
	}

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
				restSearchSuggestHistoryList = SessionManager.getInstance().getListManager()
						.getRestSearchSuggestHistoryList();
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
						if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
								&& isRefreshFoot) {
							if (isLast == false) {
								// 线程安全且不是最后一页的场合，获得站内信息列表
								executeGetSuggestKeywordListTask(keyword);
							}
						}

					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
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
				dialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						hideSearchAnimation();
					}
				});
				// 取消按钮
				btCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						close();
					}
				});
				adapter = new AutoCompleteRestSuggestAdapter(FastBookingActivity.this);
				adapter.setList(restSearchSuggestHistoryList, true);

				// --
				lvAutoComplete.setAdapter(adapter);
				lvAutoComplete.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

						int index = arg2;
						List<SuggestResultData> list = ((AutoCompleteRestSuggestAdapter) arg0.getAdapter()).getList();
						SuggestResultData data = list.get(index);

						if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.restId)
								|| String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.restId)) {
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
				adapterHistory = new UsedHistorySuggestListAdapter(FastBookingActivity.this);
				adapterHistory.setList(null, false);
				lvHistory.setAdapter(adapterHistory);
				lvHistory.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						int index = arg2;
						List<SuggestResultData> list = ((UsedHistorySuggestListAdapter) arg0.getAdapter()).getList();
						SuggestResultData data = list.get(index);

						if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.restId)
								|| String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.restId)) {
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
						if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
								&& isRefreshFoot2) {
							if (isLast2 == false) {
								// 线程安全且不是最后一页的场合，获得站内信息列表
								executeGetUsedHistorySuggestListTask();
							}
						}

					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
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
				getSuggestKeywordListTask = new GetRestSearchSuggestListTask(FastBookingActivity.this, startIndex);
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
				final GetUsedHistorySuggestListTask task = new GetUsedHistorySuggestListTask(FastBookingActivity.this);
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
						InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
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
						InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
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

				setRestOrderInfo(data);
				SessionManager.getInstance().setLastSelectedRest(data);
				close();
			}
			//关闭窗口
			private void close() {
				InputMethodManager inputManager = (InputMethodManager) etAutoComplete.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(etAutoComplete.getWindowToken(), 0);
				dialog.dismiss();
			}
		});
	}

	private void setRestOrderInfo(final SuggestResultData data) {
		if (data == null) {
			fast_booking_search_text_layout.setTag(null);
			fast_booking_search_text_view.setText("");
			promotion_icon_mibi.setVisibility(View.GONE);
			promotion_mibi.setVisibility(View.GONE);
			promotion_discount.setVisibility(View.GONE);
			promotion_coupon.setVisibility(View.GONE);
			return;
		}
		fast_booking_search_text_layout.setTag(data);
		fast_booking_search_text_view.setText(data.restName);
		// 优惠信息
		// 图标标志 0:无图标 1：券 2：惠 3：币 4：币(高亮)
		if (data.iconTag == 1) {
			promotion_icon_mibi.setVisibility(View.GONE);
			promotion_mibi.setVisibility(View.GONE);
			promotion_discount.setVisibility(View.GONE);

			promotion_coupon.setVisibility(View.VISIBLE);
			promotion_coupon.setText(data.iconTitle);
		} else if (data.iconTag == 2) {
			promotion_icon_mibi.setVisibility(View.GONE);
			promotion_mibi.setVisibility(View.GONE);
			promotion_coupon.setVisibility(View.GONE);

			promotion_discount.setVisibility(View.VISIBLE);
			promotion_discount.setText(data.iconTitle);
		} else if (data.iconTag == 3) {
			promotion_discount.setVisibility(View.GONE);
			promotion_coupon.setVisibility(View.GONE);

			promotion_mibi.setVisibility(View.VISIBLE);
			promotion_mibi.setText(data.iconTitle);
			promotion_icon_mibi.setVisibility(View.VISIBLE);
			promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_1);
		} else if (data.iconTag == 4) {
			promotion_discount.setVisibility(View.GONE);
			promotion_coupon.setVisibility(View.GONE);

			promotion_mibi.setVisibility(View.VISIBLE);
			promotion_mibi.setText(data.iconTitle);
			promotion_icon_mibi.setVisibility(View.VISIBLE);
			promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_2);
		} else {
			promotion_icon_mibi.setVisibility(View.GONE);
			promotion_mibi.setVisibility(View.GONE);
			promotion_discount.setVisibility(View.GONE);
			promotion_coupon.setVisibility(View.GONE);
		}

		//
		if (data.stateTag == 1) {// 1:可以下单

			if (data.orderSelInfo == null) {
				data.orderSelInfo = SessionManager.getInstance().getOrderSelInfo();
			}
			//
			if (data.orderSelInfo == null) {
				data.orderSelInfo = new OrderSelInfo();
			}
			//
			if (data.roomTypeInfoData == null) {
				data.roomTypeInfoData = new RoomTypeInfoData();
			}
			
			orderInfoWheel.initData(data.orderSelInfo,data.roomTypeInfoData);
			
			if(orderInfoWheel!=null&&isOpenSearch){
				orderInfoWheel.setDateMilliSeconds(DateMilliSeconds);
				orderInfoWheel.setTimeMilliSeconds(TimeMilliSeconds);
				orderInfoWheel.setPeopleNum(PeopleNum);
				orderInfoWheel.setRoomType(RoomType);
			}
//			orderInfoWheel.setRoomTypeInfoData(data.roomTypeInfoData);
//			orderInfoWheel.initData(data.orderSelInfo,data.roomTypeInfoData);
			// ---
			orderInfoWheel.setVisibility(View.VISIBLE);
			orderSubmit.setVisibility(View.VISIBLE);
			button_order_phone_call.setVisibility(View.GONE);
			fast_booking_search_order_hint_text.setVisibility(View.GONE);
			button_order_bottom_line_image.setVisibility(View.GONE);

		} else if (data.stateTag == 2) {// 2：可以打电话
			orderInfoWheel.setVisibility(View.GONE);
			orderSubmit.setVisibility(View.GONE);
			button_order_phone_call.setVisibility(View.VISIBLE);
			fast_booking_search_order_hint_text.setVisibility(View.VISIBLE);
			button_order_bottom_line_image.setVisibility(View.VISIBLE);

			button_order_phone_call.setImageResource(R.drawable.button_index_phone_order);
			fast_booking_search_order_hint_text.setTextColor(getResources().getColor(R.color.text_color_green));
			fast_booking_search_order_hint_text.setText(data.restTelForShow);
			button_order_phone_call.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (fast_booking_search_text_layout.getTag() == null
							|| CheckUtil.isEmpty(((SuggestResultData) fast_booking_search_text_layout.getTag()).restTelForCall)) {
						DialogUtil.showToast(getApplication(), "该餐厅没有预订电话");
					} else {
						ActivityUtil.callSuper57(FastBookingActivity.this,
								((SuggestResultData) fast_booking_search_text_layout.getTag()).restTelForCall);
						new Runnable() {
							public void run() {
								try {
									// ----------------------------
									OpenPageDataTracer.getInstance().addEvent("拨打电话",data.restTelForCall);
									// ----------------------------
									
									ServiceRequest.callTel(1, data.restId, data.restTelForCall);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.run();
					}
				}
			});

		} else {// 3：不能下单也不能打电话
			orderInfoWheel.setVisibility(View.GONE);
			orderSubmit.setVisibility(View.GONE);
			button_order_phone_call.setVisibility(View.VISIBLE);
			fast_booking_search_order_hint_text.setVisibility(View.VISIBLE);
			button_order_bottom_line_image.setVisibility(View.VISIBLE);

			button_order_phone_call.setImageResource(R.drawable.index_phone_order_02);
			fast_booking_search_order_hint_text.setTextColor(getResources().getColor(R.color.text_color_gray));
			fast_booking_search_order_hint_text.setText("该餐厅暂无电话信息");
			button_order_phone_call.setOnClickListener(null);
		}
		
		isOpenSearch=false;
	}

}