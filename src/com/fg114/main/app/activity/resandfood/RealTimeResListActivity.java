//package com.fg114.main.app.activity.resandfood;
//
//import java.net.*;
//import java.text.*;
//import java.util.*;
//
//import org.json.*;
//
//import android.content.*;
//import android.content.DialogInterface.OnCancelListener;
//import android.net.*;
//import android.os.*;
//import android.text.*;
//import android.util.*;
//import android.view.*;
//import android.widget.*;
//
//import com.fg114.main.R;
//import com.fg114.main.analytics.TraceManager;
//import com.fg114.main.app.*;
//import com.fg114.main.app.activity.*;
//import com.fg114.main.app.adapter.RealTimeResAdapter;
//import com.fg114.main.app.data.Filter;
//import com.fg114.main.app.data.RealTimeResFilter;
//import com.fg114.main.app.location.*;
//import com.fg114.main.app.view.*;
//import com.fg114.main.service.dto.JsonPack;
//import com.fg114.main.service.dto.RealTimeTableRestListDTO;
//import com.fg114.main.service.dto.RealTimeTableRestData;
//import com.fg114.main.service.dto.RfTypeDTO;
//import com.fg114.main.service.dto.RfTypeListDTO;
//import com.fg114.main.service.task.*;
//import com.fg114.main.util.*;
//import com.fg114.main.util.OpenPageDataManager.OpenPageData;
//
///**
// * 实时餐位信息的餐厅列表
// * 
// * @author wufucheng
// * 
// */
//public class RealTimeResListActivity extends MainFrameActivity {
//
//	private static final String TAG = "ResAndFoodListActivity";
//
//	private static final String TAG_TYPE_SORT = "sort";
//	private static final String TAG_TYPE_AVG = "avg";
//
//	private static final String TAG_TYPE_CHANNEL = "channel";
//	
//	private static final String TAG_TYPE_NEARBY = "nearby";
//	private static final int DEFAULT_DISTANCE = 5000;
//
//	// 传入参数
//	private int fromPage; // 返回页面
//	// private int fromPage2; //来自页面2
//	private String leftGoBackBtn = ""; // 返回按钮内容
//
//	// 缓存数据
//	private RealTimeResFilter filter = new RealTimeResFilter(); // 查询条件
//
//	// 画面变量
//	private String title;
//	private boolean haveGpsTag = true;
//	private boolean isTaskSafe = true;
//	private boolean isLast = true;
//	private boolean isRefreshFoot = false;
//	private int pageNo = 1;
//	private String key = "";
//
//	// 界面组件
//	private LayoutInflater mInflater;
//	private View contextView;
//	private LinearLayout showKeyLayout;
//	private Button btnShowKey;
//	private ListView lvResAndFood;
//	private RealTimeResAdapter adapter;
//	private ViewGroup vgAd;
//	private TextView tvAd;
//	private ImageView btnCloseAd;
//	// private EditText etSearch;
//
//	private Button btFirst;
//	private Button btChannel;
//	private Button btSort;
//
//	private List<RfTypeDTO> mDistanceList = new ArrayList<RfTypeDTO>();
//	private List<RfTypeDTO> mSortList = new ArrayList<RfTypeDTO>();
//	private List<RfTypeListDTO> mAreaList = new ArrayList<RfTypeListDTO>();
//	private List<RfTypeListDTO> mChannelList = new ArrayList<RfTypeListDTO>();
//	private List<RfTypeListDTO> mTopList = new ArrayList<RfTypeListDTO>();
//	private int mSelectedDistance;
//	private int mSelectedSort;
//	private int mSelectedRegion;
//	private int mSelectedDistrict;
//	private int mSelectedMainMenu;
//	private int mSelectedSubMenu;
//
//	// 任务
//	private GetRealTimeTableRestListTask getResAndFoodTask;
//	
//	//跟踪页面
//	private OpenPageData openPageData=null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		//--
//		openPageData=OpenPageDataManager.getInstance().pageStart("real");
//		// 获得传入参数
//		Bundle bundle = this.getIntent().getExtras();
//		// fromPage2 = bundle.getInt(Settings.BUNDLE_KEY_FROM_PAGE_2);
//		// //只用来区别是否来自AutoCompleteActivity
//		if (bundle.containsKey(Settings.BUNDLE_KEY_LEFT_BUTTON)) {
//			leftGoBackBtn = bundle.getString(Settings.BUNDLE_KEY_LEFT_BUTTON);
//		}
//		if (TextUtils.isEmpty(leftGoBackBtn)) {
//			leftGoBackBtn = getString(R.string.text_button_back);
//		}
//		
//		// 获得缓存数据
//		filter = SessionManager.getInstance().getRealTimeResFilter();
//		
//		if (bundle.containsKey(Settings.BUNDLE_DEFAULT_BOOK_TIME)) {
//			try {
//				Calendar mDefaultBookTime = (Calendar) bundle.getSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME);
//				mDefaultBookTime = SessionManager.getInstance().fixCalendarToPer15(mDefaultBookTime);
//				filter.setSelectTime(mDefaultBookTime.getTimeInMillis());
//			} catch (Exception e) {
//				e.printStackTrace();
//				filter.setSelectTime(SessionManager.getInstance().getDefaultBookTime().getTimeInMillis());
//			}
//		} else {
//			filter.setSelectTime(SessionManager.getInstance().getDefaultBookTime().getTimeInMillis());
//		}
//
//		// 初始化界面
//		initComponent();
//
//		// 检查网络是否连通
//		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
//		if (!isNetAvailable) {
//			// 没有网络的场合，去提示页
//			Bundle bund = new Bundle();
//			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
//			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
//		}
//
//		// 获得位置
//		haveGpsTag = Loc.isGpsAvailable();
//		
////		// 没有实时餐位的城市默认搜索区域为"全部"
////		if (SessionManager.getInstance().hasRealTimeBookByCurrentGpsCity()) {
////			filter.setDistanceMeter(DEFAULT_DISTANCE);
////		} else {
////			filter.setDistanceMeter(0);
////		}
//
//		// 获得查询结果
//		executeTask();
//	}
//	
//	
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		updateTitle();		
//	}
//
//
//
//	@Override
//	public void finish() {
//		super.finish();
//		filter.reset();
//		resetTask();
//	}
//
//	/**
//	 * 初始化
//	 */
//	private void initComponent() {
//
//		// 设置标题栏
//		getBtnOption().setVisibility(View.GONE);
//		getTvTitle().setVisibility(View.GONE);
//		getBtnTitle().setVisibility(View.VISIBLE);
//		updateTitle();
//		getBtnGoBack().setText(leftGoBackBtn);
//
//		// 内容部分
//		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		contextView = mInflater.inflate(R.layout.restaurant_and_food_list, null);
//		showKeyLayout = (LinearLayout) contextView.findViewById(R.id.restrauant_and_food_keyLayout);
//		btnShowKey = (Button) contextView.findViewById(R.id.restrauant_and_food_btnShowKey);
//		lvResAndFood = (ListView) contextView.findViewById(R.id.restrauant_and_food_listview);
//		btFirst = (Button) contextView.findViewById(R.id.restrauant_and_food_btFirst);
//		btChannel = (Button) contextView.findViewById(R.id.restrauant_and_food_btChannel);
//		btSort = (Button) contextView.findViewById(R.id.restrauant_and_food_btSort);
//		//vgAd = (ViewGroup) contextView.findViewById(R.id.restrauant_and_food_advertisement_layout);
//		tvAd = (TextView) contextView.findViewById(R.id.advertisement_text_view);
//		//btnCloseAd = (ImageView) contextView.findViewById(R.id.advertisement_close_button);
//
//		btFirst.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				showFirstFilter();
//			}
//		});
//
//		btChannel.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showChannelFilter();
//			}
//		});
//
//		btSort.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showSortFilter();
//			}
//		});
//
//		getBtnTitle().setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showDateTimePicker();
//				
//				
//			}
//		});
//
//		btnShowKey.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				resetTask();
//
//				// 删除关键字
//				key = "";
////				filter.setKeywords(key);
//				// 设为第一页
//				pageNo = 1;
//				isLast = true;
//				// 获得查询结果
//				executeTask();
//			}
//		});
//
//		// boolean isTop = fromPage == Settings.TOP_LIST_ACTIVITY;
//		// //有重试逻辑的adapter
//		// adapter = new RestaurantAndFoodAdapter(
//		// ResAndFoodListActivity.this,
//		// new View.OnClickListener() {
//		// @Override
//		// public void onClick(View v) {
//		// executeGetResAndFoodTask();
//		// }
//		// }
//		// , isTop);ACTIVITY;
//		// 有重试逻辑的adapter
//		adapter = new RealTimeResAdapter(RealTimeResListActivity.this, new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				executeTask();
//			}
//		});
//		adapter.setRoomTypeButtonListener(new RealTimeResAdapter.RoomTypeButtonListener() {
//			
//			@Override
//			public void onClickFirst(View view, RealTimeTableRestData realTimeTableRestData) {
//				if (realTimeTableRestData == null) {
//					return;
//				}
//				bookRes(view, Settings.ROOM_TYPE_TAG_HALL, realTimeTableRestData.getRestId());
//			}
//			
//			@Override
//			public void onClickSecond(View view, RealTimeTableRestData realTimeTableRestData) {
//				if (realTimeTableRestData == null) {
//					return;
//				}
//				bookRes(view, Settings.ROOM_TYPE_TAG_ROOM, realTimeTableRestData.getRestId());
//			}
//
//			@Override
//			public void onClickRes(View view, RealTimeTableRestData data) {
//				if (data != null && data.getRestId() != null && !data.getRestId().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
//
//					// 加入最近浏览
////					SessionManager.getInstance().getListManager().addHistoryList(RealTimeResListActivity.this, data);
//
//					// 去餐厅详细页面
//					Bundle bundle = new Bundle();
//					bundle.putString(Settings.BUNDLE_REST_ID, data.getRestId());
//					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, title);
//					String[] nameAndLogoUrl = { data.getRestName(), data.getPicUrl() };
//					bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
//					
//					// 日期时间
//					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//					calendar.setTimeInMillis(filter.getSelectTime());
//					bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME, calendar);
//					
//					bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
//					ActivityUtil.jump(RealTimeResListActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
//				}
//			}
//		});
//		// 设置显示图片的标志
//		adapter.showPicture = SessionManager.getInstance().getShowRestPicture(this);
//		
//		adapter.setList(null, false);
//
//		lvResAndFood.setAdapter(adapter);
////		lvResAndFood.setOnItemClickListener(new AbsListView.OnItemClickListener() {
////
////			@Override
////			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
////				int index = arg2;
////				List<RealTimeTableRestData> list = ((RealTimeResAdapter) arg0.getAdapter()).getList();
////				if (list != null) {
////					RealTimeTableRestData data = list.get(index);
////					if (data != null && data.getRestId() != null && !data.getRestId().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
////
////						// 加入最近浏览
//////						SessionManager.getInstance().getListManager().addHistoryList(RealTimeResListActivity.this, data);
////
////						// 去餐厅详细页面
////						Bundle bundle = new Bundle();
////						bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.RES_AND_FOOD_LIST_ACTIVITY);
////						bundle.putString(Settings.BUNDLE_KEY_ID, data.getRestId());
////						bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, title);
////						String[] nameAndLogoUrl = { data.getRestName(), data.getPicUrl() };
////						bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
////						
////						// 日期时间
////						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
////						calendar.setTimeInMillis(filter.getSelectTime());
////						bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME, calendar);
////						
////						ActivityUtil.jump(RealTimeResListActivity.this, RestaurantDetailActivity.class, Settings.RES_AND_FOOD_LIST_ACTIVITY, bundle);
////					}
////				}
////			}
////		});
//		lvResAndFood.setOnScrollListener(new AbsListView.OnScrollListener() {
//
//			/**
//			 * 添加滚动条滚到最底部，加载余下的元素
//			 */
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
////				Log.e("onScrollStateChanged", "scrollState=" + scrollState + ", isRefreshFoot=" + isRefreshFoot);
//				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
//					if (isLast == false) {
//						// 线程安全且不是最后一页的场合，获得餐厅列表
//						executeTask();
//					}
//				}
//			}
//
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//				if (firstVisibleItem + visibleItemCount == totalItemCount) {
//					// 当到达列表尾部时
//					isRefreshFoot = true;
//
//				} else {
//					isRefreshFoot = false;
//				}
////				Log.e("onScroll", "isRefreshFoot=" + isRefreshFoot);
//			}
//		});
//
//		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
//	}
//
//	/**
//	 * 获得餐厅列表
//	 */
//	private void executeTask() {
//
//		if (isTaskSafe) {
//			// 线程安全的场合
//			if (isLast == false) {
//				// 线程安全且不是最后一页的场合，获得餐厅列表
//				pageNo = pageNo + 1;
//			}
//			// 设置线程不安全
//			this.isTaskSafe = false;
//		} else {
//			return;
//		}
//
//		if (DEBUG)
//			Log.d(TAG, "now page no is：" + pageNo);
//
//		setButtonState(false);
//
//		// 创建任务
//		getResAndFoodTask = new GetRealTimeTableRestListTask(null, this, haveGpsTag, pageNo);
//		
//		getResAndFoodTask.setCallBack(new BaseTask.Callback() {
//			
//			@Override
//			public void onNetworkFail(JsonPack result) {
//				adapter.addList(new ArrayList<RealTimeTableRestData>(), false);
//				executeTask();
//			}
//		});
//		//--
//		OpenPageDataManager.getInstance().taskStart(openPageData);
//		// 执行任务
//		getResAndFoodTask.execute(new Runnable() {
//
//			@Override
//			public void run() {
//				
//				RealTimeTableRestListDTO dto2 = getResAndFoodTask.dto;
//				//--
//				OpenPageDataManager.getInstance().taskEnd(openPageData,getResAndFoodTask.getRequestUrl());
//				//--
//				if (dto2 != null) {
//					updateFilter(dto2);
//					
//					isLast = dto2.pgInfo.lastTag;
//					// recycle();
//					// 设置显示图片的标志
//					adapter.showPicture = SessionManager.getInstance().getShowRestPicture(RealTimeResListActivity.this);
//					
//					adapter.addList(dto2.getList(), isLast);
//					
//					if (filter.getDistanceMeter() != 0) {
//						//setLocPanel(true, run);
//					} else {
//						//setLocPanel(false, run);
//					}
//				}
//				//--
//				OpenPageDataManager.getInstance().pageEnd(openPageData);
//				openPageData=null;
//				//--
//				// 设置线程安全
//				isTaskSafe = true;
//			}
//		}, new Runnable() {
//			@Override
//			public void run() {
//				// 设置线程安全
//				isTaskSafe = true;
//				isLast = true;
//				adapter.addList(new ArrayList<RealTimeTableRestData>(), isLast);
//				setButtonState(true);
//			}
//		});
//	}
//
//	/**
//	 * 更新所有过滤器
//	 * 
//	 * @param dto
//	 */
//	private void updateFilter(RealTimeTableRestListDTO dto) {
//		if (dto == null) {
//			return;
//		}
//		setButtonState(false);
//		updateFirstFilter(dto);
//		updateChannelFilter(dto);
//		updateSortFilter(dto);
//		setKeyBar();
//		setButtonState(true);
//	}
//
//	/**
//	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
//	 * 
//	 * @param dto
//	 */
//	private void updateFirstFilter(RealTimeTableRestListDTO dto) {
//		if (pageNo != 1) {
//			return;
//		}
//
////		if (mListType == Settings.RES_AND_FOOD_LIST_TYPE_NEARBY) {
////			mDistanceList = SessionManager.getInstance().getListManager().getDistanceList(this);
////			mSelectedDistance = getPositionInRfTypeDTOList(mDistanceList, String.valueOf(filter.getDistanceMeter()));
////			btFirst.setText(mDistanceList.get(mSelectedDistance).getName());
////		} else {
////			
////		}
//		
//		mAreaList.clear();
//
//		// 全部地域
//		RfTypeListDTO allRegionDto = new RfTypeListDTO();
//		allRegionDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//		allRegionDto.setName("全部地域");
//		
//		// 附近搜索
//		RfTypeListDTO nearbyDto = new RfTypeListDTO();
//		nearbyDto.setUuid(TAG_TYPE_NEARBY);
//		nearbyDto.setName("附近");
//
//		RfTypeDTO emptyDto = new RfTypeDTO();
//		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//		emptyDto.setName("");
//
//		if (dto.getRegionList() == null) {
//			dto.setRegionList(new ArrayList<RfTypeListDTO>());
//		}
//		dto.getRegionList().add(0, allRegionDto);
//		dto.getRegionList().add(0, nearbyDto);
//
//		boolean hasSelectedRegion = false; // 是否存在选中的Region
//		boolean hasSelectedDistrict = false; // 是否存在选中的District
//		String selectedRegionName = "";
//		String selectedDistrictName = "";
//
//		for (RfTypeListDTO ctld : dto.getRegionList()) {
//
//			if (ctld.getList() == null) {
//				ctld.setList(new ArrayList<RfTypeDTO>());
//			}
//			if (ctld.getUuid().equals(String.valueOf(Settings.STATUTE_ALL)) || ctld.getUuid().equals(TAG_TYPE_NEARBY)) {
//				ctld.getList().add(0, emptyDto);
//			} else {
//				// 全部子商区
//				RfTypeDTO allSubDto = new RfTypeDTO();
//				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//				allSubDto.setName("全部" + ctld.getName());
//				ctld.getList().add(0, allSubDto);
//			}
//
//			if (ctld.isSelectTag()) {
//				mSelectedRegion = getPositionInRfTypeListDTOList(dto.getRegionList(), ctld.getUuid());
//				selectedRegionName = ctld.getName();
//				hasSelectedRegion = true;
//				if (ctld.getList().size() > 1) {
//					for (RfTypeDTO ctd : ctld.getList()) {
//						if (ctd.isSelectTag()) {
//							mSelectedDistrict = getPositionInRfTypeDTOList(ctld.getList(), ctd.getUuid());
//							selectedDistrictName = ctd.getName();
//							hasSelectedDistrict = true;
//						}
//					}
//				}
//			}
//
//			mAreaList.add(ctld);
//		}
//		
//		if (filter.getDistanceMeter() != 0) {
//			mSelectedRegion = 0;
//			mSelectedDistrict = 0;
//			btFirst.setText(dto.getRegionList().get(mSelectedRegion).getName());
//		} else {
//			if (!hasSelectedRegion) {
//				// 不存在选中的区域
//				mSelectedRegion = 1;
//				mSelectedDistrict = 0;
//				btFirst.setText(dto.getRegionList().get(mSelectedRegion).getName());
//			} else {
//				if (hasSelectedDistrict) {
//					// 存在选中的子商区，按钮文字为选中的子商区名称
//					btFirst.setText(selectedDistrictName);
//				} else {
//					mSelectedDistrict = 0;
//					// 不存在选中的子商区，按钮文字为选中的大区域名称
//					btFirst.setText(selectedRegionName);
//				}
//			}
//		}
//	}
//
//	/**
//	 * 更新频道，菜系过滤器
//	 * 
//	 * @param dto
//	 */
//	private void updateChannelFilter(RealTimeTableRestListDTO dto) {
//		if (pageNo != 1) {
//			return;
//		}
//
//		mChannelList.clear();
//
//		RfTypeListDTO resDto = new RfTypeListDTO();
//		resDto.setUuid(Settings.STATUTE_CHANNEL_RESTAURANT);
//		resDto.setName("全部菜系");
//		resDto.setMemo(TAG_TYPE_CHANNEL);
//
//		RfTypeDTO emptyDto = new RfTypeDTO();
//		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//		emptyDto.setName("");
//
//		if (dto.getMenuTypeList() == null) {
//			dto.setMenuTypeList(new ArrayList<RfTypeListDTO>());
//		}
//		dto.getMenuTypeList().add(0, resDto);
//
//		boolean hasSelectedMainMenu = false; // 是否存在选中的MainMenu
//		boolean hasSelectedSubMenu = false; // 是否存在选中的SubMenu
//		String selectedMainMenuName = "";
//		String selectedSubMenuName = "";
//
//		for (RfTypeListDTO ctld : dto.getMenuTypeList()) {
//
//			if (ctld.getList() == null) {
//				ctld.setList(new ArrayList<RfTypeDTO>());
//			}
//			if (ctld.getMemo().equals(TAG_TYPE_CHANNEL)) {
//				ctld.getList().add(0, emptyDto);
//			} else {
//				// 全部子菜系
//				RfTypeDTO allSubDto = new RfTypeDTO();
//				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//				allSubDto.setName("全部" + ctld.getName());
//				ctld.getList().add(0, allSubDto);
//			}
//
//			if (ctld.isSelectTag()) {
//				mSelectedMainMenu = getPositionInRfTypeListDTOList(dto.getMenuTypeList(), ctld.getUuid());
//				selectedMainMenuName = ctld.getName();
//				hasSelectedMainMenu = true;
//				if (ctld.getList().size() > 1) {
//					for (RfTypeDTO ctd : ctld.getList()) {
//						if (ctd.isSelectTag()) {
//							mSelectedSubMenu = getPositionInRfTypeDTOList(ctld.getList(), ctd.getUuid());
//							selectedSubMenuName = ctd.getName();
//							hasSelectedSubMenu = true;
//						}
//					}
//				}
//			}
//
//			mChannelList.add(ctld);
//		}
//
//		if (!hasSelectedMainMenu) {
//			// 不存在选中的主菜系
//			mSelectedMainMenu = 0;
//			mSelectedSubMenu = 0;
//			btChannel.setText(dto.getMenuTypeList().get(mSelectedMainMenu).getName());
//		} else {
//			if (hasSelectedSubMenu) {
//				// 存在选中的子菜系，按钮文字为选中的子菜系名称
//				btChannel.setText(selectedSubMenuName);
//			} else {
//				mSelectedSubMenu = 0;
//				// 不存在选中的子菜系，按钮文字为选中的主菜系名称
//				btChannel.setText(selectedMainMenuName);
//			}
//		}
//	}
//
//	/**
//	 * 更新排序过滤器
//	 * 
//	 * @param dto
//	 */
//	private void updateSortFilter(RealTimeTableRestListDTO dto) {
//		if (pageNo != 1) {
//			return;
//		}
//
//		mSortList.clear();
//
//		RfTypeDTO sortDto = new RfTypeDTO();
//		sortDto.setUuid("");
//		sortDto.setName("---选择排序方式---");
//
//		RfTypeDTO avgDto = new RfTypeDTO();
//		avgDto.setUuid("");
//		avgDto.setName("---按人均价格筛选---");
//
//		RfTypeDTO emptyDto = new RfTypeDTO();
//		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
//		emptyDto.setName("");
//
//		if (dto.getSortList() != null && dto.getSortList().size() > 0) {
//			for (RfTypeDTO ctd : dto.getSortList()) {
//				ctd.setMemo(TAG_TYPE_SORT);
//			}
//			mSortList.add(sortDto);
//			mSortList.addAll(dto.getSortList());
//		}
//
//		if (dto.getAvgList() != null && dto.getAvgList().size() > 0) {
//			for (RfTypeDTO ctd : dto.getAvgList()) {
//				ctd.setMemo(TAG_TYPE_AVG);
//			}
//			mSortList.add(avgDto);
//			mSortList.addAll(dto.getAvgList());
//		}
//
//		if (mSortList.size() == 0) {
//			mSortList.add(emptyDto);
//			mSelectedSort = 0;
//		} else {
//			RfTypeDTO selectedDto = null;
//			for (int i = 0; i < mSortList.size(); i++) {
//				if (mSortList.get(i).isSelectTag()) {
//					mSelectedSort = i;
//					selectedDto = mSortList.get(i);
//					break;
//				}
//			}
//			if (selectedDto != null && !TextUtils.isEmpty(selectedDto.getUuid())) {
//				if (selectedDto.getMemo().equals(TAG_TYPE_SORT)) {
//					filter.setSortTypeTag(Integer.parseInt(selectedDto.getUuid()));
//					filter.setAvgTag("0");
//				} else if (selectedDto.getMemo().equals(TAG_TYPE_AVG)) {
//					filter.setSortTypeTag(0);
//					filter.setAvgTag(selectedDto.getUuid());
//				}
//				btSort.setText(mSortList.get(mSelectedSort).getName());
//			} else {
//				mSelectedSort = 1;
//				btSort.setText(mSortList.get(1).getName());
//			}
//		}
//	}
//
//	/**
//	 * 显示第一过滤条件的筛选框
//	 */
//	private void showFirstFilter() {
//		DialogUtil.showTwoWheelsDialog(this, mAreaList, mSelectedRegion, mSelectedDistrict, new DialogUtil.OnWheelSelectedListener() {
//
//			@Override
//			public void onSelected(final RfTypeDTO[] values) {
//				try {
//					if (values == null || values.length != 2 || values[0] == null) {
//						return;
//					}
//					if (values[0].getUuid().equals(TAG_TYPE_NEARBY)) {
//						ActivityUtil.checkNearbyForRealTimeBook(RealTimeResListActivity.this, new Runnable() {
//
//							@Override
//							public void run() {
//								filter.setDistanceMeter(DEFAULT_DISTANCE);
//								filter.setRegionId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSortTypeTag(0);
//								filter.setAvgTag("0");
//								btFirst.setText(values[0].getName());
//								
//								resetTask();
//								// 设为第一页
//								pageNo = 1;
//								isLast = true;
//								// 获得查询结果
//								executeTask();
//							}
//						}, new Runnable() {
//							
//							@Override
//							public void run() {
//								finish();
//							}
//						});
//					} else {
//						if (values[1] == null) {
//							if (filter.getRegionId().equals(values[0].getUuid()) && filter.getDistrictId().equals(String.valueOf(Settings.STATUTE_ALL))) {
//								return;
//							}
//							if (filter.getDistanceMeter() != 0) {
//								// 之前选择的是附近搜索
//								filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSortTypeTag(0);
//								filter.setAvgTag("0");
//							}
//							filter.setDistanceMeter(0);
//							filter.setRegionId(values[0].getUuid());
//							filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
//							btFirst.setText(values[0].getName());
//						} else {
//							if (filter.getRegionId().equals(values[0].getUuid()) && filter.getDistrictId().equals(values[1].getUuid()) && filter.getDistanceMeter() == 0) {
//								return;
//							}
//							if (filter.getDistanceMeter() != 0) {
//								// 之前选择的是附近搜索
//								filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
//								filter.setSortTypeTag(0);
//								filter.setAvgTag("0");
//							}
//							filter.setDistanceMeter(0);
//							filter.setRegionId(values[0].getUuid());
//							filter.setDistrictId(values[1].getUuid());
//							if (values[1].getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
//								btFirst.setText(values[0].getName());
//							} else {
//								btFirst.setText(values[1].getName());
//							}
//						}
//						
//						resetTask();
//						// 设为第一页
//						pageNo = 1;
//						isLast = true;
//						// 获得查询结果
//						executeTask();
//					}
//				} catch (Exception e) {
//					LogUtils.logE(TAG, e);
//				}
//			}
//		});
//	}
//
//	/**
//	 * 显示频道，菜系条件的筛选框
//	 */
//	private void showChannelFilter() {
//		DialogUtil.showTwoWheelsDialog(this, mChannelList, mSelectedMainMenu, mSelectedSubMenu, new DialogUtil.OnWheelSelectedListener() {
//
//			@Override
//			public void onSelected(RfTypeDTO[] values) {
//				if (values == null || values.length != 2) {
//					return;
//				}
//
//				if (values[0].getMemo().equals(TAG_TYPE_CHANNEL)) {
//					if (filter.getMainMenuId().equals(String.valueOf(Settings.STATUTE_ALL))
//							&& filter.getSubMenuId().equals(String.valueOf(Settings.STATUTE_ALL))) {
//						return;
//					}
//					filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL));
//					filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
//					filter.setSortTypeTag(0);
//					filter.setAvgTag("0");
//					btChannel.setText(values[0].getName());
//				} else {
//					if (values[1] == null) {
//						if (filter.getMainMenuId().equals(values[0].getUuid()) && filter.getSubMenuId().equals(String.valueOf(Settings.STATUTE_ALL))) {
//							return;
//						}
//						filter.setMainMenuId(values[0].getUuid());
//						filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
//						btChannel.setText(values[0].getName());
//					} else {
//						if (filter.getMainMenuId().equals(values[0].getUuid()) && filter.getSubMenuId().equals(values[1].getUuid())) {
//							return;
//						}
//						filter.setMainMenuId(values[0].getUuid());
//						filter.setSubMenuId(values[1].getUuid());
//						if (values[1].getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
//							btChannel.setText(values[0].getName());
//						} else {
//							btChannel.setText(values[1].getName());
//						}
//					}
//				}
//
//				resetTask();
//				// 设为第一页
//				pageNo = 1;
//				isLast = true;
//				// 获得查询结果
//				executeTask();
//			}
//		});
//	}
//
//	/**
//	 * 显示排序条件的筛选框
//	 */
//	private void showSortFilter() {
//		DialogUtil.showOneWheelDialog(this, mSortList, mSelectedSort, new DialogUtil.OnWheelSelectedListener() {
//
//			@Override
//			public void onSelected(RfTypeDTO[] values) {
//				if (TextUtils.isEmpty(values[0].getUuid())) {
//					return;
//				}
//				if (values == null || values.length != 1) {
//					return;
//				}
//
//				if (values[0].getMemo().equals(TAG_TYPE_SORT)) {
//					if (filter.getSortTypeTag() == Integer.parseInt(values[0].getUuid()) && filter.getAvgTag().equals("0")) {
//						return;
//					}
//					filter.setSortTypeTag(Integer.parseInt(values[0].getUuid()));
//					filter.setAvgTag("0");
//				} else if (values[0].getMemo().equals(TAG_TYPE_AVG)) {
//					if (filter.getSortTypeTag() == 0 && filter.getAvgTag().equals(values[0].getUuid())) {
//						return;
//					}
//					filter.setSortTypeTag(0);
//					filter.setAvgTag(values[0].getUuid());
//				}
//				btSort.setText(values[0].getName());
//
//				resetTask();
//				// 设为第一页
//				pageNo = 1;
//				isLast = true;
//				// 获得查询结果
//				executeTask();
//			}
//		});
//	}
//
//	
//	//当前位置刷新后回调接口
//	Runnable run=new Runnable()
//	{
//		
//		@Override
//		public void run()
//		{
//			resetTask();
//			// 设为第一页
//			pageNo = 1;
//			isLast = true;
//			// 获得查询结果
//			executeTask();
//			
//		}
//	};
//
//	/**
//	 * 设置筛选按钮的状态
//	 * 
//	 * @param state
//	 */
//	private void setButtonState(boolean state) {
//		btFirst.setClickable(state);
//		btChannel.setClickable(state);
//		btSort.setClickable(state);
//		getBtnTitle().setClickable(state);
//	}
//
//	/**
//	 * 返回指定id项在列表中的位置下标
//	 * 
//	 * @param list
//	 * @param id
//	 * @return
//	 */
//	private int getPositionInRfTypeDTOList(List<RfTypeDTO> list, String id) {
//		if (list == null || list.size() == 0) {
//			return 0;
//		}
//		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i).getUuid().equals(id)) {
//				return i;
//			}
//		}
//		return 0;
//	}
//
//	private int getPositionInRfTypeListDTOList(List<RfTypeListDTO> list, String id) {
//		if (list == null || list.size() == 0) {
//			return 0;
//		}
//		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i).getUuid().equals(id)) {
//				return i;
//			}
//		}
//		return 0;
//	}
//
//	/**
//	 * 设置关键字提示栏
//	 */
//	private void setKeyBar() {
////		key = filter.getKeywords();
////		if (CheckUtil.isEmpty(key)) {
////			showKeyLayout.setVisibility(View.GONE);
////		} else {
////			showKeyLayout.setVisibility(View.VISIBLE);
////			btnShowKey.setText(key);
////		}
//	}
//
//	/**
//	 * 回收内存
//	 */
//	private void recycle() {
//		// 回收内存
//		if (adapter != null) {
//			Iterator<MyImageView> iterator = adapter.viewList.iterator();
//			while (iterator.hasNext()) {
//				iterator.next().recycle(true);
//			}
//			adapter.viewList.clear();
//			System.gc();
//		}
//	}
//
//	/**
//	 * 重设列表内容取得任务
//	 */
//	private void resetTask() {
//		recycle();
//		if (getResAndFoodTask != null) {
//			getResAndFoodTask.cancel(true);
//			// 设置显示图片的标志
//			adapter.showPicture = SessionManager.getInstance().getShowRestPicture(this);
//			adapter.setList(null, false);
//			lvResAndFood.setAdapter(adapter);
//			// 设置线程安全
//			isTaskSafe = true;
//		}
//		System.gc();
//	}
//
//	/**
//	 * 更新标题栏
//	 */
//	private void updateTitle() {
//		getBtnTitle().setText(formatDateTimeString(filter.getSelectTime()));
//	}
//	
//	private void showDateTimePicker() {
//		
//		Calendar calFilter = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//		calFilter.setTimeInMillis(filter.getSelectTime());
//		
//		WheelDateTimePicker picker = WheelDateTimePicker.create();
//		Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//		picker.setStartDate(calNow);
//		
//		Calendar calEndDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//		calEndDate.set(calNow.get(Calendar.YEAR) + 1, 11, 31, 23, 59);
//		picker.setEndDate(calEndDate);
//		
//		picker.showDateTimePickerForXiaomishu(this, calFilter, new WheelDateTimePicker.OnDateTimeSetListener() {
//			
//			@Override
//			public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {
////				String date = year + "年" + monthOfYear + "月" + dayOfMonth + "日" + hourOfDay + "时" + minute + "分";
////				DialogUtil.showToast(RealTimeResListActivity.this, date);
//				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//				cal.set(year, monthOfYear - 1, dayOfMonth, hourOfDay, minute);
//				filter.setSelectTime(cal.getTimeInMillis());
//				updateTitle();
//				
//				resetTask();
//				// 设为第一页
//				pageNo = 1;
//				isLast = true;
//				// 获得查询结果
//				executeTask();
//			}
//		});
//	}
//	
//	private String formatDateTimeString(long time) {
//		try {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
//			return sdf.format(new Date(time)) + " 餐位情况";
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "餐位情况";
//		}
//	}
//	
//	/**
//	 * 去预订
//	 */
//	private void bookRes(View v, int roomType, String restaurantId) {
//		// 防止重复点击
//		ViewUtils.preventViewMultipleClick(v, 1000);
//		// 日期时间
//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//		calendar.setTimeInMillis(filter.getSelectTime());
//		// 去预订页
//		Bundle bundle = new Bundle();
//		bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME, calendar);
//		bundle.putInt(Settings.BUNDLE_DEFAULT_ROOM_TYPE, roomType);
//		bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
//		bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "实时餐位");
//		ActivityUtil.jump(RealTimeResListActivity.this, BookingFromNetActivity.class, 0, bundle);
//	}
//}