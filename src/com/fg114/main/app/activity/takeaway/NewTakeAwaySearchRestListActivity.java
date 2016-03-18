package com.fg114.main.app.activity.takeaway;

import java.net.*;
import java.text.*;
import java.util.*;

import org.json.*;

import android.content.*;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.*;
import android.os.*;
import android.provider.Contacts.People;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;

import com.baidu.location.BDLocation;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.*;
import com.fg114.main.app.activity.*;
import com.fg114.main.app.activity.order.NewMyOrderDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.app.location.*;
import com.fg114.main.app.view.*;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;
import com.fg114.main.util.OpenPageDataManager.OpenPageData;
import com.unionpay.upomp.bypay.other.D;

/**
 * 
 * 外卖餐厅搜索列表
 * 
 * @author sunquan,2014-04-08
 * 
 */
public class NewTakeAwaySearchRestListActivity extends MainFrameActivity {

	private static final String TAG = "NewTakeAwaySearchRestListActivity";

	// 缓存数据
	private TakeAwayFilter filter; // 查询条件

	// 0:表示从index进入，这个时候要使用
	private int urlFromAd = 0;
	private String savePosName = "";
	private String saveSendLimitId = "";
	private String saveKeyWords = "";
	private String saveTypeId = "";
	private double saveLatitude = 0;
	private double saveLongitude = 0;

	// 画面变量
	private boolean haveGpsTag = true;
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;
	private String key = "";

	// 界面组件
	private View contextView;
	private LinearLayout showKeyLayout;
	private Button button_show_key;
	private ListView lvRest;
	private NewTakeAwaySearchRestListAdapter adapter;

	private Button btFirst;
	private Button btSecond;
	private Button btThird;
	private List<CommonTypeDTO> mFirstList = new ArrayList<CommonTypeDTO>();
	private List<CommonTypeDTO> mSecondList = new ArrayList<CommonTypeDTO>();

	// 任务
	// private GetRestListTask getRestListTask;
	private ViewGroup dropdownAnchor;
	private boolean needHideBackButton = true;

	private DragLoadingView dragview_rest_list;

	// private boolean isFirst=true;//是否第一次进入

	private String LocCity="";
	private String City="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// isFirst=true;
		// 获得缓存数据
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖列表", "");
		// ----------------------------
		filter = com.fg114.main.util.SessionManager.getInstance().getTakeAwayFilter();

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			// needHideBackButton =
			// bundle.getBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON,
			// false);
			urlFromAd = bundle.getInt(Settings.WAIMAI_BUNDLE_URLFROMAD, 0);
			// 如果是webview ad调用，必须保存数值
			if (urlFromAd == 1) {
				String tempstr = bundle.getString(Settings.WAIMAI_BUNDLE_POSITIONNAME);
				savePosName = tempstr == null ? "" : tempstr;
				tempstr = bundle.getString(Settings.WAIMAI_BUNDLE_KEYWORDS);
				saveKeyWords = tempstr == null ? "" : tempstr;
				tempstr = bundle.getString(Settings.WAIMAI_BUNDLE_TYPEID);
				saveTypeId = tempstr == null ? "" : tempstr;
				tempstr = bundle.getString(Settings.WAIMAI_BUNDLE_SENDLIMITID);
				saveSendLimitId = tempstr == null ? "" : tempstr;
				saveLatitude = bundle.getDouble(Settings.WAIMAI_BUNDLE_LATITUDE, 0);
				saveLongitude = bundle.getDouble(Settings.WAIMAI_BUNDLE_LONGITUDE, 0);
			}

		}
		if (urlFromAd == 0)
			filter.reset();

		if (bundle.containsKey(Settings.UUID)) {
			filter.setTypeId(bundle.getString(Settings.UUID));
		}
		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 尝试定位
		showProgressDialog("获取位置中...");
		contextView.postDelayed(firstLocate, 100);
	}

	// 首次
	int retryTimes = 1;
	Runnable firstLocate = new Runnable() {
		@Override
		public void run() {
			// 默认使用当前位置
			String address = getCurrentAddress();
			if (MainFrameActivity.locatingFailed.equals(address)) {
				getTvTitle().setText(MainFrameActivity.locating);
				// 多等2.5秒，重试一次
				if (retryTimes-- > 0) {
					getTvTitle().postDelayed(this, 2500);
				} else {
					closeProgressDialog();
					DialogUtil.showAlert(getCurrentActivity(), "提示", "没有定位到您的位置，请手动选择您的位置!");
				}
			} else {

				if (urlFromAd == 0 || (urlFromAd == 1 && filter.getPoiName().equals(""))) {
					getTvTitle().setText(address);
					if (LocBaidu.currentLocation != null) {
						BDLocation loc = LocBaidu.currentLocation;
						filter.setPoiName(address);
						filter.setLatitude(loc.getLatitude());
						filter.setLongitude(loc.getLongitude());
						filter.setGpsTypeTag(3);
					}
				} else {
					getTvTitle().setText(filter.getPoiName());
				}

				closeProgressDialog();
				// 获得查询结果
				executeGetRestListTask();
			}
			isSameCity();
		}
	};

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖列表", "");
		// ----------------------------
		// ----------------------------
		// OpenPageDataTracer.getInstance().enterPage("外卖列表", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// ---
		updateTitle();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 只有webview ad跳转调用时才会destroy，必须把positionname, keywords ... 恢复
		if (urlFromAd == 1) {
			filter.setPoiName(savePosName);
			filter.setKeywords(saveKeyWords);
			filter.setSendLimitId(saveSendLimitId);
			filter.setTypeId(saveTypeId);
			filter.setLatitude(saveLatitude);
			filter.setLongitude(saveLongitude);
		}

	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		getBtnGoBack().setText("返回");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.setLocationLayoutVisibility(View.VISIBLE);
		// 内容部分
		contextView = View.inflate(this, R.layout.take_away_rest_search_list, null);
		showKeyLayout = (LinearLayout) contextView.findViewById(R.id.keywords_layout);
		dropdownAnchor = (ViewGroup) contextView.findViewById(R.id.top_condition_layout);
		button_show_key = (Button) contextView.findViewById(R.id.button_show_key);
		lvRest = (ListView) contextView.findViewById(R.id.listview);
		btFirst = (Button) contextView.findViewById(R.id.button_first);
		btSecond = (Button) contextView.findViewById(R.id.button_second);
		btThird = (Button) contextView.findViewById(R.id.button_third);
		dragview_rest_list = (DragLoadingView) contextView.findViewById(R.id.dragview_rest_list);

		btFirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅类别下拉框");
				// -----
				showFirstFilter();
			}
		});

		btSecond.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("送餐类别下拉框");
				// -----
				showSecondFilter();
			}
		});

		btThird.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				// -----
				OpenPageDataTracer.getInstance().addEvent("搜索按钮");
				// -----
				showSearchDialog();
			}
		});

		button_show_key.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetTask();

				// 删除关键字
				key = "";
				filter.setKeywords(key);
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetRestListTask();
			}
		});

		// 有重试逻辑的adapter
		adapter = new NewTakeAwaySearchRestListAdapter(NewTakeAwaySearchRestListActivity.this, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeGetRestListTask();
			}
		});

		adapter.setList(null, false);
		lvRest.setAdapter(adapter);
		lvRest.setOnItemClickListener(new AbsListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<TakeoutListData2> list = ((NewTakeAwaySearchRestListAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					TakeoutListData2 data = list.get(index);
					if (data != null) {
						if (("" + Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
							// 控制项不处理
							return;
						}
						// -----
						OpenPageDataTracer.getInstance().addEvent("选择行", data.uuid);
						// 去点菜页
						Bundle bundle = new Bundle();
						bundle.putString(Settings.UUID, data.uuid);
						bundle.putInt(Settings.FROM_TAG, 1);
						String[] nameAndLogoUrl = { data.name, data.picUrl };
						bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
						ActivityUtil.jump(NewTakeAwaySearchRestListActivity.this, TakeAwayNewFoodListActivity.class, 0, bundle);
					}
				}
			}
		});
		lvRest.setOnScrollListener(new AbsListView.OnScrollListener() {

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
				// Log.e("onScrollStateChanged", "scrollState=" + scrollState +
				// ", isRefreshFoot=" + isRefreshFoot);
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 线程安全且不是最后一页的场合，获得餐厅列表
						executeGetRestListTask();
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
				// Log.e("onScroll", "isRefreshFoot=" + isRefreshFoot);
			}
		});
		// 餐厅列表下拉刷新
		dragview_rest_list.setDragLoadingListener(new DragLoadingView.DragLoadingListener() {

			@Override
			public void onDragReleased() {
				// -----
				OpenPageDataTracer.getInstance().addEvent("下拉刷新");
				// -----

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				executeGetRestListTask();
			}

			@Override
			public boolean isAllowDrag() {

				if (lvRest == null || lvRest.getChildCount() <= 0) {
					return false;
				}
				View v = lvRest.getChildAt(0);
				Rect r = new Rect();
				boolean allow = v.getLocalVisibleRect(r);
				if (r.top == 0) {
					return true;
				}
				return false;
			}
		});
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onRefreshToNewAddress() {
		super.onRefreshToNewAddress();
		String address = getCurrentAddress();
		if (LocBaidu.currentLocation != null) {
			BDLocation loc = LocBaidu.currentLocation;
			filter.setPoiName(address);
			filter.setLatitude(loc.getLatitude());
			filter.setLongitude(loc.getLongitude());
			filter.setGpsTypeTag(3);
			refreshList();
		}
		getTvTitle().setText(address);
	}

	/**
	 * 获得餐厅列表
	 */
	private void executeGetRestListTask() {

		if (isTaskSafe) {
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}
		setButtonState(false);
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		ServiceRequest request = new ServiceRequest(API.getTakeoutList2);
		request.addData("gpsTypeTag", filter.getGpsTypeTag()); // 经纬度类别 1:原生
																// 2：百度
																// 3：中国（google）
		request.addData("longitude", filter.getLongitude()); // 经度 如果为0的话是全城搜索
		request.addData("latitude", filter.getLatitude()); // 纬度 如果为0的话是全城搜索
		request.addData("keywords", filter.getKeywords()); // 搜索关键词
															// 如果不为空，忽略longitude，latitude
		request.addData("typeId", filter.getTypeId()); // 外卖餐厅类别id 默认为空
		request.addData("sendLimitId", filter.getSendLimitId()); // 起送类别id 默认为空
		request.addData("pageSize", 30); // 页面大小
		request.addData("startIndex", startIndex); // 当前页

		CommonTask.request(request, "", new CommonTask.TaskListener<TakeoutList2DTO>() {

			@Override
			protected void onSuccess(TakeoutList2DTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				updateFilter(dto);
				isLast = dto.pgInfo.lastTag;
				adapter.addList(dto.list, isLast);
				startIndex = dto.pgInfo.nextStartIndex;
				// --
				// 设置线程安全
				isTaskSafe = true;
				setButtonState(true);
				dragview_rest_list.reset();
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// doTest();
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 设置线程安全
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<TakeoutListData2>(), isLast);
				setButtonState(true);
				dragview_rest_list.reset();
			}

			// @Override
			// protected void defineCacheKeyAndTime(CacheKeyAndTime keyAndTime)
			// {
			// // 城市
			// String cityId = "";
			// CityInfo city =
			// SessionManager.getInstance().getCityInfo(getApplicationContext());
			// if (city != null) {
			// cityId = city.getId();
			// }
			// StringBuilder sbKey = new StringBuilder();
			// sbKey.append(cityId).append('|');
			// sbKey.append(Settings.VERSION_NAME).append('|');
			// sbKey.append(filter.getGpsTypeTag()).append('|');// gps类型 经纬度类别
			// 1:原生 2：百度 3：google
			// sbKey.append(GeoUtils.formatLongLat(filter.getLongitude())).append('|');//
			// 经度, 如果为0的话是全城搜索
			// sbKey.append(GeoUtils.formatLongLat(filter.getLatitude())).append('|');//
			// 纬度, 如果为0的话是全城搜索
			// sbKey.append(filter.getKeywords()).append('|');// 搜索关键词
			// 如果不为空,忽略longitude，latitude
			// sbKey.append(filter.getTypeId()).append('|');// 外卖餐厅类别id 默认为空
			// sbKey.append(filter.getSendLimitId()).append('|');// 起送类别id 默认为空
			// sbKey.append(30).append('|');
			// sbKey.append(startIndex).append('|');
			// // -------------
			// keyAndTime.cacheKey = sbKey.toString();
			// keyAndTime.cacheTimeMinute = 180;// 3小时
			// }

			void doTest() {
				String json = "{\"pgInfo\":{\"nextStartIndex\":\"1\",\"lastTag\":\""
						+ (System.currentTimeMillis() % 5 == 0 ? true : false)
						+ "\"},\"list\": [{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E10B29K23717\",\"openTag\": \"true\",\"picUrl\": \"http://f3.xiaomishu.com/pic/AESH10002255/small/1bb7fae7-8a46-427d-b833-959e75a3b486.JPG\",\"name\": \"宝燕餐厅\",\"haveGiftTag\": \"true\",\"address\": \"上海市西藏南路1000弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"1.5\",\"stateName\": \"暂不送\",\"stateColor\": \"#00FF00\",\"sendLimitPrice\": \"20元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"E33D06P02470\",\"openTag\": \"true\",\"picUrl\": \"http://f1.xiaomishu.com/pic/AESH10000681/small/bd542b13-1e09-4057-a5bd-94508ef50812.JPG\",\"name\": \"俏江南 仙乐斯广场店\",\"haveGiftTag\": \"false\",\"address\": \"仙乐斯广场店204弄\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.5\",\"stateName\": \"热卖餐厅\",\"stateColor\": \"#0000FF\",\"sendLimitPrice\": \"15元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"},{\"uuid\": \"C19F03N13653\",\"openTag\": \"false\",\"picUrl\": \"http://f3.xiaomishu.com/pic/C36H13J40583/small/4f711318-9cad-4b46-875a-cf2b8db95a59.jpg\",\"name\": \"上海会馆\",\"haveGiftTag\": \"true\",\"address\": \"上海市浦东新区117号\",\"longitude\": \"121.47999\",\"latitude\": \"31.23357\",\"overallNum\": \"3.0\",\"stateName\": \"紧张送货中\",\"stateColor\": \"#FF0000\",\"sendLimitPrice\": \"30元起送\",\"sendReachMins\": \"20分钟送达\",\"distanceMeter\": \"300米\"}],\"typeList\": [{\"uuid\": \"t1\",\"name\": \"中餐厅\",\"selectTag\": \"false\"},{\"uuid\": \"t2\",\"name\": \"西餐厅\",\"selectTag\": \"false\"},{\"uuid\": \"t3\",\"name\": \"川味餐厅\",\"selectTag\": \"true\"},{\"uuid\": \"t4\",\"name\": \"烧烤\",\"selectTag\": \"false\"},{\"uuid\": \"t5\",\"name\": \"日式餐厅\",\"selectTag\": \"false\"},{\"uuid\": \"t6\",\"name\": \"汉堡\",\"selectTag\": \"false\"}],\"sendLimitList\": [{\"uuid\": \"s1\",\"name\": \"十元以下\",\"selectTag\": \"false\"},{\"uuid\": \"s2\",\"name\": \"二十元以下\",\"selectTag\": \"false\"},{\"uuid\": \"s3\",\"name\": \"三十元以下\",\"selectTag\": \"false\"},{\"uuid\": \"s4\",\"name\": \"五十元以下\",\"selectTag\": \"true\"}],\"chineseLon\": \"121.47999\",\"chineseLat\": \"31.23357\"}";
				TakeoutList2DTO dto = JsonUtils.fromJson(json, TakeoutList2DTO.class);
				onSuccess(dto);
			}
		});

	}

	/**
	 * 更新所有过滤器
	 * 
	 * @param dto
	 */
	private void updateFilter(TakeoutList2DTO dto) {
		if (dto == null) {
			return;
		}
		setButtonState(false);
		updateFirstFilter(dto);
		updateSecondFilter(dto);
		setKeyBar();
		setButtonState(true);
	}

	/**
	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(TakeoutList2DTO dto) {
		if (startIndex != 1 || dto == null || dto.typeList == null || dto.typeList.size() == 0) {
			return;
		}
		mFirstList.clear();
		for (CommonTypeDTO data : dto.typeList) {
			if (data.isSelectTag()) {
				btFirst.setText(data.getName()); // 设置按钮名字
			}
			mFirstList.add(data);
		}
	}

	/**
	 * 更新频道，菜系过滤器
	 * 
	 * @param dto
	 */
	private void updateSecondFilter(TakeoutList2DTO dto) {
		if (startIndex != 1) {
			return;
		}
		mSecondList.clear();
		for (CommonTypeDTO data : dto.sendLimitList) {
			if (data.isSelectTag()) {
				btSecond.setText(data.getName()); // 设置按钮名字
			}
			mSecondList.add(data);
		}
	}

	private void showFirstFilter() {
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mFirstList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (subData == null) {
					if (filter.getTypeId().equals(mainData.getUuid())) {
						return; // 选择的是同一项
					}
					filter.setTypeId(mainData.getUuid());
					btFirst.setText(mainData.getName());
				}

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetRestListTask();
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
	private void showSecondFilter() {
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mSecondList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (subData == null) {
					if (filter.getSendLimitId().equals(mainData.getUuid())) {
						return; // 选择的是同一项
					}
					filter.setSendLimitId(mainData.getUuid());
					btSecond.setText(mainData.getName());
				}

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetRestListTask();
			}
		}, new OnDismissListener() {
			@Override
			public void onDismiss() {
				btSecond.setSelected(false);
			}
		});
	}

	/**
	 * 显示排序条件的筛选框
	 */
	private void showSearchDialog() {

		final EditText text = new EditText(ContextUtil.getContext());
		text.setText(filter.getKeywords());
		DialogUtil.showSearchDialog(NewTakeAwaySearchRestListActivity.this, "搜索", text, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newKeyword = text.getText().toString();
				if (CheckUtil.isEmpty(newKeyword)) {
					return;
				}
				makeSearch(newKeyword);
			}

		});

	}

	private void makeSearch(String newKeyword) {
		showKeyLayout.setVisibility(View.VISIBLE);
		CommonTypeDTO data = new CommonTypeDTO();
		// data.setUuid(typeId);
		data.setUuid(""); // 重新搜索的时候，为了刷新顶部类型列表，将类型重置为“全部”
		filter.setKeywords(newKeyword);
		button_show_key.setText(newKeyword);
		// ----
		resetTask();
		// 设为第一页
		startIndex = 1;
		isLast = true;
		// 获得查询结果
		executeGetRestListTask();
	}

	/**
	 * 设置筛选按钮的状态
	 * 
	 * @param state
	 */
	private void setButtonState(boolean state) {
		btFirst.setClickable(state);
		btSecond.setClickable(state);
		btThird.setClickable(state);
		getBtnTitle().setClickable(state);
	}

	/**
	 * 设置关键字提示栏
	 */
	private void setKeyBar() {
		key = filter.getKeywords();
		if (CheckUtil.isEmpty(key)) {
			showKeyLayout.setVisibility(View.GONE);
		} else {
			showKeyLayout.setVisibility(View.VISIBLE);
			button_show_key.setText(key);
		}
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		adapter.setList(null, false);
		lvRest.setAdapter(adapter);
		// 设置线程安全
		isTaskSafe = true;
	}

	/**
	 * 更新标题栏
	 */
	private void updateTitle() {

		getTvTitle().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		getBtnOption().setVisibility(View.VISIBLE);
		getTvTitle().setVisibility(View.VISIBLE);
		getBtnTitle().setVisibility(View.GONE);
		getTvTitleIcon().setVisibility(View.VISIBLE);
		getTvTitle().setPadding(getTvTitle().getPaddingLeft(), getTvTitle().getPaddingTop(), UnitUtil.dip2px(15), getTvTitle().getPaddingBottom());

		// 调整图片大小
		Drawable pic = this.getResources().getDrawable(R.drawable.icon_location_anchor_white);
		pic.setBounds(0, 0, UnitUtil.dip2px(28), UnitUtil.dip2px(28));
		getBtnOption().setCompoundDrawables(null, pic, null, null);
		getBtnOption().setPadding(0, 0, 0, 0);

		LinearLayout.LayoutParams lp = (LayoutParams) getBtnOption().getLayoutParams();
		lp.width = UnitUtil.dip2px(30);
		lp.height = UnitUtil.dip2px(30);
		getBtnOption().setLayoutParams(lp);
		// --
		// 去地图
		getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// 定位不成功，不允许去地图
				if (locatingFailed.equals(getCurrentAddress())) {
					DialogUtil.showAlert(getCurrentTopActivity(), "提示", "当前没有gps，无法进行地图搜索，请先选择其他位置进行搜索!");
					return;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent("地图列表切换按钮");
				// -----
				ActivityUtil.jump(NewTakeAwaySearchRestListActivity.this, TakeAwayRestaurantMapActivity.class, 0, new Bundle());
			}
		});
		if (!LocCity.equals(City)) {
			String selectCityName = SessionManager.getInstance().getCityInfo(NewTakeAwaySearchRestListActivity.this).getName();
			// 定位城市和选择城市 不是同一个
			this.getTvTitle().setText("请选择一个" + selectCityName + "的位置");
		} else {
			getTvTitle().setText(filter.getPoiName());
		}
		getTvTitleLayout().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择地址按钮");
				// -----
				// 点击去选择位置
				ActivityUtil.jump(NewTakeAwaySearchRestListActivity.this, SelectPOIActivity.class, 1, new Bundle());
			}
		});
	}

	// 根据当前类页面的状态刷新列表
	private void refreshList() {
		resetTask();
		// 设为第一页
		startIndex = 1;
		isLast = true;
		// 获得查询结果
		executeGetRestListTask();
	}

	// 定位城市和用户选择 是相同城市
	private void isSameCity() {
		
		CityInfo LocCityInfo = SessionManager.getInstance().getCity(NewTakeAwaySearchRestListActivity.this, filter.getLongitude(), filter.getLatitude());
		if (LocCityInfo != null) {
			LocCity = LocCityInfo.getId();
			City = SessionManager.getInstance().getCityInfo(NewTakeAwaySearchRestListActivity.this).getId();
			if (!LocCity.equals(City)) {
				// isFirst=false;
				this.setLocationLayoutVisibility(View.GONE);
				String selectCityName = SessionManager.getInstance().getCityInfo(NewTakeAwaySearchRestListActivity.this).getName();
				// 定位城市和选择城市 不是同一个
				this.getTvTitle().setText("请选择一个" + selectCityName + "的位置");

				DialogUtil.showAlert(NewTakeAwaySearchRestListActivity.this, false, "您不在当前城市(" + selectCityName + ")，请选择一个" + selectCityName + "的位置进行查询",

				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						OpenPageDataTracer.getInstance().addEvent("选择地址按钮");
						// -----
						// 点击去选择位置
						Bundle bundle = new Bundle();
						bundle.putBoolean(Settings.BUNDLE_issameCity, false);
						ActivityUtil.jump(NewTakeAwaySearchRestListActivity.this, SelectPOIActivity.class, 1, bundle);

						dialog.dismiss();
					}

				});
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 是选择位置过来的
		if (resultCode == 999) {
			getTvTitle().setText(filter.getPoiName());
			refreshList();
		}
	}
}