package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.search.*;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.SelectPOIActivity.MySearchListener;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.SelectPOIAdapter;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.app.location.LocBaidu;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 搜索POI信息，利用百度SDK
 * 
 * @author xujianjun,2013-11-15
 */
public class SelectPOIActivity extends MainFrameActivity {

	private String keyword = "";
	private volatile long timestamp = 0;
	private Fg114Application app;

	// 本地缓存数据
	private List<CommonTypeDTO> historyList;

	// 界面组件
	private EditText etAutoComplete;
	private ListView lvAutoComplete;
	private SelectPOIAdapter adapter;
	private TakeAwayFilter filter = SessionManager.getInstance().getTakeAwayFilter();
	private Button voiceSearchButton;
	private boolean isTaskSafe = true;

	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 0;
	
	//是否是相同城市
	private boolean isSameCity=true;
	// 任务
	private Handler searchHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg == null || msg.obj == null) {
				return;
			}
			long t = (Long) msg.obj;
			// 时间戳已失效
			if (t != timestamp) {
				return;
			}
			// 时间戳与当前时间戳相同时则执行搜索
			adapter.isReset = true;
			// 重新还原列表数据
			executeGetSuggestKeywordListTask(keyword, t);
		}
	};

	private View button_locate_me;
	private TextView text_locate_me;
	private MKSearch mMKSearch;
	private MySearchListener searchListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (Fg114Application) getApplication();
		try {
			// 初始化百度地图
			app.initBaidu();
		} catch (Exception e) {
			try {
				// 初始化百度地图
				app.initBaidu();
			} catch (Exception e2) {
				ActivityUtil.saveException(e, "init baidu api fail");
				Settings.gBaiduAvailable = false;
			}
		}

		setContentView(R.layout.select_poi);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖选择地理位置", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle.containsKey(Settings.BUNDLE_KEY_KEYWORD)) {
			keyword = bundle.getString(Settings.BUNDLE_KEY_KEYWORD);
		}
		if (bundle.containsKey(Settings.BUNDLE_issameCity)) {
			isSameCity = bundle.getBoolean(Settings.BUNDLE_issameCity);
		}
		
		// 获得搜索历史记录
		historyList = SessionManager.getInstance().getListManager().getPoiSearchHistoryListInfo(this).getResList();

		initComponent();

		etAutoComplete.requestFocus();
		InputMethodManager imm = (InputMethodManager) SelectPOIActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

		mMKSearch = new MKSearch();
		searchListener = new MySearchListener();
		mMKSearch.init(Fg114Application.getInstance().mBMapMan, searchListener);
	}

	@Override
	protected void onRestart() {
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖选择地理位置", "");
		// ----------------------------
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.activity_enter, R.anim.activity_enter);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != resultCode) {
			this.setResult(resultCode, data);
			this.finish();
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// channelSpinner = (Spinner)
		// this.findViewById(R.id.auto_complete_channelSpinner);
		etAutoComplete = (EditText) this.findViewById(R.id.etSearchbox);
		button_locate_me = (View) this.findViewById(R.id.button_locate_me);
		text_locate_me = (TextView) this.findViewById(R.id.text_locate_me);
		lvAutoComplete = (ListView) this.findViewById(R.id.listview);
		voiceSearchButton = (Button) this.findViewById(R.id.btVoice);
		ViewUtils.setClearable(etAutoComplete);

		if(isSameCity){
			button_locate_me.setVisibility(View.VISIBLE);
		}else{
			button_locate_me.setVisibility(View.GONE);
		}
		// 搜索框事件
		etAutoComplete.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				// 显示关键字列表
				keyword = etAutoComplete.getText().toString().trim();
				startIndex = 0;
				if (TextUtils.isEmpty(keyword)) {
					// 清空关键字时立即处理
					executeGetSuggestKeywordListTask(keyword, 0);
				} else {
					timestamp = SystemClock.elapsedRealtime();
					// 关键字改变后延时一定时间再开始搜索，用户连续较快输入时不重复多次搜索
					searchHandler.sendMessageDelayed(searchHandler.obtainMessage(0, timestamp), 150);
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

		etAutoComplete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("搜索按钮");
				// -----
			}
		});

		// 分页加载
		lvAutoComplete.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 线程安全且不是最后一页的场合，获得站内信息列表
						executeGetSuggestKeywordListTask(keyword, 0);
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

		// 按回车返回
		etAutoComplete.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					return searchByKey();
				}
				return false;
			}
		});

		adapter = new SelectPOIAdapter(this);
		adapter.setList(historyList, true);
		lvAutoComplete.setAdapter(adapter);
		lvAutoComplete.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				int index = arg2;
				List<CommonTypeDTO> list = ((SelectPOIAdapter) arg0.getAdapter()).getList();
				CommonTypeDTO data = list.get(index);
				// 控制项或者消息项的场合
				if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.getUuid()) || String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.getUuid())) {
					// 控制项表示清空历史记录
					if (data.getName().equals(getString(R.string.text_button_clear))) {
						// 清空历史记录的场合
						SessionManager.getInstance().getListManager().removeAllPoiSearchHistoryInfo(SelectPOIActivity.this);
						historyList.clear();
						adapter.setList(historyList, true);
					}
				} else {
					// 保存为搜索历史
					CommonTypeDTO historyDto = data.clone();
					SessionManager.getInstance().getListManager().addPoiSearchHistoryInfo(SelectPOIActivity.this, historyDto);
					// 完成选择位置
					selectLoacationDone(historyDto.getGpsType(), historyDto.getLongitude(), historyDto.getLatitude(), historyDto.getName());
				}
			}
		});

		// 设置语音识别按钮事件-----added by xujianjun, 2011-12-13
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {

			eng.bindButtonAndEditText(voiceSearchButton, etAutoComplete, 0, null, new Runnable() {

				@Override
				public void run() {
				}
			});
		}

		if (!TextUtils.isEmpty(keyword)) {
			etAutoComplete.setText(keyword);
		}

		refreshLocationButton();
	}

	// 刷新定位按钮状态
	private void refreshLocationButton() {
		boolean isLocated = false;
		String address = "点击定位到当前位置";
		if (LocBaidu.currentLocation != null) {
			BDLocation loc = LocBaidu.currentLocation;
			if (loc.hasAddr()) {
				address = loc.getDistrict() + loc.getStreet();
			} else {
				address = loc.getLongitude() + "," + loc.getLatitude();
			}
			isLocated = true;
		}
		text_locate_me.setText(address);
		// 逻辑：如果定位成功，则点击后返回列表页并且选择当前位置。如果没有定位成功，点击后尝试重新定位
		if (isLocated) {
			button_locate_me.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					ViewUtils.preventViewMultipleClick(view, 2000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("定位当前位置按钮");
					// -----
					if (LocBaidu.currentLocation != null) {
						BDLocation loc = LocBaidu.currentLocation;
						selectLoacationDone(3, loc.getLongitude(), loc.getLatitude(), text_locate_me.getText().toString());
					}
				}
			});
		} else {
			button_locate_me.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					ViewUtils.preventViewMultipleClick(view, 2000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("定位当前位置按钮");
					// -----
					text_locate_me.setText("正在定位...");
					button_locate_me.postDelayed(new Runnable() {

						@Override
						public void run() {
							refreshLocationButton();
						}
					}, 2000);
				}
			});
		}

	}

	// 选择一个位置，gpsType，1:原生；2:百度；3:google
	void selectLoacationDone(int gpsType, double longitude, double latitude, String poiName) {
		filter.setSelectedPoi(true);
		filter.setPoiName(poiName);
		filter.setLatitude(latitude);
		filter.setLongitude(longitude);
		filter.setGpsTypeTag(gpsType);
		setResult(999);
		finish();
	}

	/**
	 * 获得关键字提示
	 */

	private void executeGetSuggestKeywordListTask(String key, final long time) {
		// 关键字是空，显示搜索历史
		if (CheckUtil.isEmpty(key)) {
			adapter.setList(historyList, true);
			isTaskSafe = true;
			isLast = true;
			startIndex = 0;
			button_locate_me.setVisibility(View.VISIBLE);
			return;
		} else {
			// 搜索关键字的时候隐藏定位按钮
			button_locate_me.setVisibility(View.GONE);
		}

		// 搜索框文字改变触发搜索，则不控制线程安全，并行发送请求
		// 此种情况可以通过startIndex进行判定
		if (startIndex != 0) {
			if (isTaskSafe) {
				// 设置线程不安全
				this.isTaskSafe = false;
			} else {
				return;
			}
		}
		// 是重新搜索（是新关键字，非分页加载）
		if (adapter.isReset) {
			isLast = false;
			startIndex = 0;
			adapter.addList(new ArrayList<CommonTypeDTO>(), isLast);
		}
		// ---
		// 调用百度搜索poi
		String cityName = SessionManager.getInstance().getCityInfo(this).getName();
		int pageCapacity = 30;

		if (!useCache(cityName, key, startIndex, pageCapacity)) {
			if (startIndex == 0) {
				// 注意，MKSearchListener只支持一个，以最后一次设置为准
				// Log.d("去新搜索页",""+startIndex);
				searchListener.setParam(cityName, key, startIndex, pageCapacity);
				mMKSearch.setPoiPageCapacity(pageCapacity);
				mMKSearch.poiSearchInCity(cityName, key);
			} else {
				// Log.d("去其他页",""+startIndex);
				searchListener.setParam(cityName, key, startIndex, pageCapacity);
				mMKSearch.setPoiPageCapacity(pageCapacity);
				mMKSearch.poiSearchInCity(cityName, key);
				mMKSearch.goToPoiPage(startIndex);
			}
		}
	}

	// 尝试使用缓存的搜索结果
	private boolean useCache(String cityName, String keyword, int pageIndex, int pageCapacity) {
		try {
			// 以下是缓存逻辑，缓存时间24小时
			// 先构造缓存关键字
			StringBuilder sbKey = new StringBuilder();
			sbKey.append(cityName).append('|');
			sbKey.append(keyword).append('|');
			sbKey.append(pageIndex).append('|');
			sbKey.append(pageCapacity).append('|');
			String key = sbKey.toString();
			String dir = getClass().getName();
			ValueObject vo = ValueCacheUtil.getInstance(this).get(dir, key);
			JsonPack jp = new JsonPack();
			if (vo != null && !vo.isExpired()) {
				// 命中
				// Log.d("命中命中命中","命中");
				CommonTypeListDTO dto = JsonUtils.fromJson(vo.getValue(), CommonTypeListDTO.class);
				success(dto);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void saveCache(CommonTypeListDTO dto) {
		try {
			// --缓存逻辑
			// 如果结果正确，存入缓存（不能是缓存里的数据）
			if (dto != null && dto.getList() != null && dto.getList().size() > 0) {
				// ---
				StringBuilder sbKey = new StringBuilder();
				sbKey.append(dto.getCityName()).append('|');
				sbKey.append(dto.getKeyword()).append('|');
				sbKey.append(dto.getCurrentPageIndex()).append('|');
				sbKey.append(dto.getPageCapacity()).append('|');
				String key = sbKey.toString();
				String dir = getClass().getName();
				// ---
				ValueCacheUtil.getInstance(this).remove(dir, key);
				ValueCacheUtil.getInstance(this).add(dir, key, JsonUtils.toJson(dto), "0", "-", 60 * 24); // 24小时
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean searchByKey() {
		return false;
	}

	// 数据返回成功
	void success(CommonTypeListDTO dto) {
		if (dto != null) {
			int totalPage = dto.getTotalPage();
			int currentPageIndex = dto.getCurrentPageIndex();
			String keyword = dto.getKeyword();
			String cityName = dto.getCityName();
			// ---
			if (CheckUtil.isEmpty(keyword)) {
				adapter.setList(historyList, true);
			} else {
				// Log.d("--------",totalPage+","+currentPageIndex);
				isLast = (totalPage - 1 == currentPageIndex);
				startIndex = currentPageIndex + 1;
				adapter.addList(dto.getList(), isLast);
			}
			if (adapter.getList() != null && adapter.getList().size() > 0 && adapter.isReset) {
				lvAutoComplete.setSelection(0);
			}
		}
		isTaskSafe = true;
	}

	// 数据搜索失败
	void fail() {
		isTaskSafe = true;
		isLast = true;
		adapter.addList(new ArrayList<CommonTypeDTO>(), isLast);
	}

	public class MySearchListener implements MKSearchListener {
		private String cityName;
		private String keyword;
		private int startIndex;
		private int pageCapacity;

		public void setParam(String cityName, String keyword, int startIndex, int pageCapacity) {
			this.cityName = cityName;
			this.keyword = keyword;
			this.startIndex = startIndex;
			this.pageCapacity = pageCapacity;
		}

		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			// 返回地址信息搜索结果
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
			// 返回驾乘路线搜索结果
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {

			try {
				// Log.d("---------iError",""+iError);
				// 返回poi搜索结果
				if (iError != 0) {
					// DialogUtil.showToast(getApplicationContext(), "网络超时");
					// 注意，如果没有搜索到结果，会以 错误号!=0 的形式返回
					fail();
					return;
				}
				if (result == null || result.getAllPoi() == null || result.getAllPoi().size() == 0 || type != MKSearch.TYPE_POI_LIST) {
					fail();
					return;
				}
				ArrayList<MKPoiInfo> poiList = result.getAllPoi();
				// 转成CommonTypeListDTO
				CommonTypeListDTO dto = new CommonTypeListDTO();
				for (MKPoiInfo poi : poiList) {
					if (poi.pt == null) {
						continue; // 过滤掉没有坐标的位置
					}
					CommonTypeDTO data = new CommonTypeDTO();
					data.setName(poi.name);
					data.setMemo(poi.address);
					data.setGpsType(2);
					data.setLongitude(poi.pt.getLongitudeE6() / 1E6);
					data.setLatitude(poi.pt.getLatitudeE6() / 1E6);
					dto.getList().add(data);
				}
				// Log.d("####startIndex=" + startIndex, result.getNumPages() +
				// ","+ result.getPageIndex() + "," +
				// result.getCurrentNumPois());
				dto.setCurrentPageIndex(result.getPageIndex());
				dto.setTotalPage(result.getNumPages());
				dto.setPageCapacity(pageCapacity);
				dto.setCityName(cityName);
				dto.setKeyword(keyword);
				saveCache(dto);
				success(dto);
			} catch (Exception e) {
				DialogUtil.showToast(getApplicationContext(), "搜索系统繁忙，请稍后重试");
				e.printStackTrace();
				fail();
			}
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
			// 返回公交搜索结果
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
			// 返回步行路线搜索结果
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			// 返回公交车详情信息搜索结果
		}

		@Override
		public void onGetPoiDetailSearchResult(int i, int j) {
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult mksuggestionresult, int i) {
		}
	}
}
