package com.fg114.main.app.activity.resandfood;

import java.net.*;
import java.text.*;
import java.util.*;

import org.json.*;

import android.content.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.PopupWindow.OnDismissListener;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.*;
import com.fg114.main.app.activity.*;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.location.*;
import com.fg114.main.app.view.*;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;
import com.fg114.main.util.OpenPageDataManager.OpenPageData;

/**
 * 餐厅和美食列表界面
 * 
 * @author zhangyifan
 * 
 */
public class ResAndFoodListActivity extends MainFrameActivity {

	private static final String TAG = "ResAndFoodListActivity";

	private static final String TAG_TYPE_SORT = "sort";
	private static final String TAG_TYPE_AVG = "avg";

	private static final String TAG_TYPE_CHANNEL = "channel";

	// 传入参数
	private int fromPage; // 返回页面
	// private int fromPage2; //来自页面2
	private String leftGoBackBtn = ""; // 返回按钮内容
	private int mListType;
	private String restName;// 餐厅ID

	// 缓存数据
	private Filter filter; // 查询条件

	// 画面变量
	private String title;
	private boolean haveGpsTag = true;
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;
	private String key = "";

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private LinearLayout showKeyLayout;
	private Button btnShowKey;
	private ListView lvResAndFood;
	private RestListAdapter adapter;

	private Button btFirst;
	private Button btChannel;
	private Button btSort;
	private List<RfTypeDTO> mDistanceList = new ArrayList<RfTypeDTO>();
	private List<RfTypeDTO> mSortList = new ArrayList<RfTypeDTO>();
	private List<RfTypeListDTO> mAreaList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeListDTO> mChannelList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeListDTO> mTopList = new ArrayList<RfTypeListDTO>();
	private String selectedMainTopName = "";
	private String selectedSubTopName = "";

	// 任务
	private GetRestListTask getRestListTask;

	// 使用基站位置定位
	private boolean mIsUseCellLoc = false;

	private boolean mHasTop; // 是否有榜单

	private ViewGroup dropdownAnchor;

	private boolean hasSelectedMainTop;

	private boolean hasSelectedSubTop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅列表", "");
		// ----------------------------
		// --
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// //只用来区别是否来自AutoCompleteActivity
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_KEY_LEFT_BUTTON)) {
			leftGoBackBtn = bundle.getString(Settings.BUNDLE_KEY_LEFT_BUTTON);
		}
		if (TextUtils.isEmpty(leftGoBackBtn)) {
			leftGoBackBtn = getString(R.string.text_button_back);
		}
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE)) {
			mListType = bundle.getInt(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE);
		}
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_REST_NAME)) {
			restName = bundle.getString(Settings.BUNDLE_REST_NAME);
		}

		// 获得缓存数据
		filter = com.fg114.main.util.SessionManager.getInstance().getFilter();

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

		// 获得位置
		haveGpsTag = Loc.isGpsAvailable();

		// 获得查询结果
		executeGetResAndFoodTask();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅列表", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ---
		mHasTop = SessionManager.getInstance().doesCurrentCityHaveMainMenuItem("榜单");
		updateTitle();
		// //第一次进入时的朦皮，只在餐厅的时候显示
		// if("1".equals(SessionManager.getInstance().getFilter().getChannelId())&&mHasTop){
		// DialogUtil.showVeilPictureOnce(this,
		// R.drawable.mask_search_list,"ShowOnceVeil_ResAndFoodListActivity");
		// }
	}

	@Override
	public void finish() {
		super.finish();
		filter.reset();
		resetTask();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		updateTitle();
		getBtnGoBack().setText("返回");

		if (mListType == Settings.RES_AND_FOOD_LIST_TYPE_NEARBY) {
			setLocationLayoutVisibility(View.VISIBLE);
			// 测试定位
			// setLocPanel(true, run);
			boolean isTest = ActivityUtil.isTestDev(this);
			if (isTest) {
				getTvTitle().setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						showMyLoc();
					}
				});

				getBtnTitle().setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						showMyLoc();
						return false;
					}
				});
			}
		}

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_and_food_list, null);
		showKeyLayout = (LinearLayout) contextView.findViewById(R.id.restrauant_and_food_keyLayout);
		dropdownAnchor = (ViewGroup) contextView.findViewById(R.id.restrauant_and_food_topLayout);
		btnShowKey = (Button) contextView.findViewById(R.id.restrauant_and_food_btnShowKey);
		lvResAndFood = (ListView) contextView.findViewById(R.id.restrauant_and_food_listview);
		btFirst = (Button) contextView.findViewById(R.id.restrauant_and_food_btFirst);
		btChannel = (Button) contextView.findViewById(R.id.restrauant_and_food_btChannel);
		btSort = (Button) contextView.findViewById(R.id.restrauant_and_food_btSort);

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

		getBtnTitle().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				// -----
				OpenPageDataTracer.getInstance().addEvent("榜单下拉框");
				// -----
				showTopFilter();
			}
		});

		btnShowKey.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				resetTask();

				// 删除关键字
				key = "";
				filter.setKeywords(key);
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetResAndFoodTask();
			}
		});

		// 有重试逻辑的adapter
		adapter = new RestListAdapter(ResAndFoodListActivity.this, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeGetResAndFoodTask();
			}
		});
		// 设置显示图片的标志
		if ("1".equals(SessionManager.getInstance().getFilter().getChannelId())) {
			adapter.showPicture = SessionManager.getInstance().getShowRestPicture(this);
		} else {
			adapter.showPicture = true;
		}
		adapter.setList(null, false, "1");

		lvResAndFood.setAdapter(adapter);
		lvResAndFood.setOnItemClickListener(new AbsListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<RestListData> list = ((RestListAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					RestListData data = list.get(index);
					// 非控制项，点击时跳转
					if (data != null && data.iconTag != Settings.CONTRL_ITEM_ID) {
						// -----
						OpenPageDataTracer.getInstance().addEvent("选择行", data.restId);
						// -----
						// 加入最近浏览
						// SessionManager.getInstance().getListManager().addHistoryList(ResAndFoodListActivity.this,
						// data);
						// 去餐厅详细页面
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_REST_ID, data.restId);
						bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, title);
						String[] nameAndLogoUrl = { data.restName, data.picUrl };
						bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
						bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
						ActivityUtil.jump(ResAndFoodListActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
					}
				}
			}
		});
		lvResAndFood.setOnScrollListener(new AbsListView.OnScrollListener() {

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
						executeGetResAndFoodTask();
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

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	/**
	 * 获得餐厅列表
	 */
	private void executeGetResAndFoodTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				// startIndex = startIndex + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}

		setButtonState(false);

		// 创建任务
		getRestListTask = new GetRestListTask(null, this, haveGpsTag, startIndex, mIsUseCellLoc);

		getRestListTask.setCallBack(new BaseTask.Callback() {

			@Override
			public void onNetworkFail(JsonPack result) {
				adapter.addList(new ArrayList<RestListData>(), false, SessionManager.getInstance().getFilter().getChannelId());
				executeGetResAndFoodTask();
			}
		});
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		// --
		// 执行任务
		getRestListTask.execute(new Runnable() {

			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				RestListDTO dto2 = getRestListTask.dto2;

				if (dto2 != null) {
					updateFilter(dto2);

					isLast = dto2.pgInfo.lastTag;
					// 设置显示图片的标志
					if ("1".equals(SessionManager.getInstance().getFilter().getChannelId())) {
						adapter.showPicture = SessionManager.getInstance().getShowRestPicture(ResAndFoodListActivity.this);
					} else {
						adapter.showPicture = true;
					}
					adapter.addList(dto2.list, isLast, SessionManager.getInstance().getFilter().getChannelId());
					startIndex = dto2.pgInfo.nextStartIndex;
				}
				// --
				// 设置线程安全
				isTaskSafe = true;

				mIsUseCellLoc = false;
			}
		}, new Runnable() {
			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 设置线程安全
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<RestListData>(), isLast, SessionManager.getInstance().getFilter().getChannelId());
				setButtonState(true);
				mIsUseCellLoc = false;
			}
		});
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
		setButtonState(false);
		updateFirstFilter(dto);
		updateChannelFilter(dto);
		updateSortFilter(dto);
		updateTopFilter(dto);
		setKeyBar();
		setButtonState(true);
	}

	/**
	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(RestListDTO dto) {
		if (startIndex != 1) {
			return;
		}

		mAreaList.clear();

		/*
		 * 新逻辑：一 如果是地铁站搜索则 1)不需要 附近 2)全部区域 变成 全部线路 二 如果是餐厅附近查找 1)只显示餐厅附近距离
		 */

		// 手工添加“附近区域”，放在全部下面

		RfTypeListDTO nearRegionDto = new RfTypeListDTO();
		nearRegionDto.setUuid(Settings.STATUTE_CHANNEL_NEARBY);
		mDistanceList = SessionManager.getInstance().getListManager().getDistanceList(this);
		if (CheckUtil.isEmpty(SessionManager.getInstance().getFilter().getRestId())) {
			nearRegionDto.setName("附近");
		} else {
			nearRegionDto.setName("该餐厅附近");

		}
		nearRegionDto.getList().addAll(mDistanceList);

		//

		// 全部地域
		RfTypeListDTO allRegionDto = new RfTypeListDTO();
		allRegionDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		if (SessionManager.getInstance().getFilter().isSubwayTag()) {
			allRegionDto.setName("全部地铁站");
		} else {
			allRegionDto.setName("全部地域");
		}

		if (dto.regionList == null) {
			dto.regionList = new ArrayList<RfTypeListDTO>();
		}

		
		// -------------
		// 添加“全部”子项
		List all = SelectionListView.mergeAllSubList(dto.regionList);
		RfTypeListDTO all_suball = new RfTypeListDTO();
		all_suball.setUuid(String.valueOf(Settings.STATUTE_ALL));
		if (SessionManager.getInstance().getFilter().isSubwayTag()) {
			all_suball.setName("-- 全部地铁站 --");
		} else {
			all_suball.setName("-- 全部地域 --");
		}
		all_suball.setParentId(allRegionDto.getUuid());
		//
		all.add(0, all_suball);

		allRegionDto.setIsNeedGroupBy(true);
		
		//如果是餐厅附近查找  不要全部地域
		if (!CheckUtil.isEmpty(SessionManager.getInstance().getFilter().getRestId())) {
			all.remove(0);
		}

		allRegionDto.setList(all);

		//
		

		// -------------
		if (!SessionManager.getInstance().getFilter().isSubwayTag()) {
			dto.regionList.add(0, nearRegionDto);
		}
		dto.regionList.add(0, allRegionDto);
		
		//如果是餐厅附近查找  不要全部地域
		if (!CheckUtil.isEmpty(SessionManager.getInstance().getFilter().getRestId())) {
			dto.regionList.remove(0);
		}

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
				selectedRegionName = ctld.getName();
				hasSelectedRegion = true;
				selectedRegion = ctld;

				if (ctld.getList().size() > 1) {
					for (RfTypeDTO ctd : ctld.getList()) {
						if (ctd.isSelectTag()) {
							selectedDistrictName = ctd.getName();
							hasSelectedDistrict = true;
							selectedDistrict = ctd;
						}
					}
				}
			}

			mAreaList.add(ctld);
		}

		// 如果是附近，修正下选择项
		if (filter.getDistanceMeter() != 0) {
			int mSelectedDistance = getPositionInRfTypeDTOList(mDistanceList, String.valueOf(filter.getDistanceMeter()));
			mDistanceList.get(mSelectedDistance).setSelectTag(true);

			nearRegionDto.setSelectTag(true);
			hasSelectedRegion = true;
			hasSelectedDistrict = true;
			selectedDistrictName = mDistanceList.get(mSelectedDistance).getName();

			// --
			selectedRegion = nearRegionDto;
			selectedDistrict = mDistanceList.get(mSelectedDistance);

			setLocationLayoutVisibility(View.VISIBLE);
		} else {
			setLocationLayoutVisibility(View.GONE);
		}

		if (!hasSelectedRegion) {
			// 不存在选中的区域
			allRegionDto.setSelectTag(true);
			btFirst.setText(dto.regionList.get(0).getName());

			selectedRegion = allRegionDto;
			selectedDistrict = null;
		} else {
			if (hasSelectedDistrict) {
				// 存在选中的子商区，按钮文字为选中的子商区名称
				btFirst.setText(selectedDistrictName);
			} else {
				// 不存在选中的子商区，按钮文字为选中的大区域名称
				btFirst.setText(selectedRegionName);
			}
		}
	}

	/**
	 * 更新频道，菜系过滤器
	 * 
	 * @param dto
	 */
	private void updateChannelFilter(RestListDTO dto) {
		if (startIndex != 1) {
			return;
		}

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
		all_suball.setParentId(String.valueOf(Settings.STATUTE_ALL));
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
				selectedMainMenuName = ctld.getName();
				hasSelectedMainMenu = true;
				if (ctld.getList().size() > 1) {
					for (RfTypeDTO ctd : ctld.getList()) {
						if (ctd.isSelectTag()) {
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
			btChannel.setText(dto.menuTypeList.get(0).getName());
		} else {
			if (hasSelectedSubMenu) {
				// 存在选中的子菜系，按钮文字为选中的子菜系名称
				btChannel.setText(selectedSubMenuName);
			} else {
				// 不存在选中的子菜系，按钮文字为选中的主菜系名称
				btChannel.setText(selectedMainMenuName);
			}
		}
	}

	/**
	 * 更新排序过滤器
	 * 
	 * @param dto
	 */
	private void updateSortFilter(RestListDTO dto) {
		if (startIndex != 1) {
			return;
		}

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
		} else {
			RfTypeDTO selectedDto = null;
			int i = 0;
			for (i = 0; i < mSortList.size(); i++) {
				if (mSortList.get(i).isSelectTag()) {
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
				btSort.setText(mSortList.get(i).getName());
			} else {
				btSort.setText(mSortList.get(1).getName());
				mSortList.get(1).setSelectTag(true);
			}
		}
	}

	/**
	 * 更新榜单过滤器
	 * 
	 * @param dto
	 */
	private void updateTopFilter(RestListDTO dto) {
		if (startIndex != 1) {
			return;
		}

		mTopList.clear();

		// RfTypeListDTO allMainTopDto = new RfTypeListDTO();
		// allMainTopDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		// allMainTopDto.setName("全部榜单");

		// RfTypeDTO emptyDto = new RfTypeDTO();
		// emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		// emptyDto.setName("");

		if (dto.topRestTypeList == null) {
			dto.topRestTypeList = new ArrayList<RfTypeListDTO>();
		}
		// dto.topRestTypeList.add(0, allMainTopDto);

		hasSelectedMainTop = false; // 是否存在选中的MainTop
		hasSelectedSubTop = false; // 是否存在选中的SubTop
		for (RfTypeListDTO ctld : dto.topRestTypeList) {

			if (ctld.getList() == null) {
				ctld.setList(new ArrayList<RfTypeDTO>());
			}
			if (ctld.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
				// ctld.getList().add(0, emptyDto);//双列表不用添加子项
			} else {
				// 全部子榜单
				RfTypeDTO allSubDto = new RfTypeDTO();
				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
				allSubDto.setName("全部" + ctld.getName());
				ctld.getList().add(0, allSubDto);
			}

			if (ctld.isSelectTag()) {
				selectedMainTopName = ctld.getName();
				hasSelectedMainTop = true;
				if (ctld.getList().size() > 1) {
					for (RfTypeDTO ctd : ctld.getList()) {
						if (ctd.isSelectTag()) {
							selectedSubTopName = ctd.getName();
							hasSelectedSubTop = true;
						}
					}
				}
			}

			mTopList.add(ctld);
		}

		updateTitle();
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

				if (subData == null) {
					if (filter.getRegionId().equals(mainData.getUuid()) && filter.getDistrictId().equals(String.valueOf(Settings.STATUTE_ALL))) {
						return; // 选择的是同一项
					}
					filter.setRegionId(mainData.getUuid());
					filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
					btFirst.setText(mainData.getName());

				} else {
					//
					boolean changed = false;
					if (filter.getDistanceMeter() != 0 && !mainData.getUuid().equals(String.valueOf(Settings.STATUTE_CHANNEL_NEARBY))) {
						changed = true;
					}

					if (!filter.getRegionId().equals(mainData.getUuid()) || !filter.getDistrictId().equals(subData.getUuid())) {
						changed = true;
					}
					if (!changed) {// 如果条件没有变，不执行动作
						return;
					}
					// 如果是附近 又不是根据餐厅附近瞎选
					if (mainData.getUuid().equals(String.valueOf(Settings.STATUTE_CHANNEL_NEARBY)) && CheckUtil.isEmpty(filter.getRestId())) {
						CityInfo city = SessionManager.getInstance().getCityInfo(getCurrentTopActivity());
						CityInfo gpsCity = SessionManager.getInstance().getGpsCity(getCurrentTopActivity());

						if (gpsCity == null || gpsCity.getId() == null || !gpsCity.getId().equals(city.getId())) {
							DialogUtil.showAlert(getCurrentTopActivity(), "提示", "您不在当前城市，请先切换到所在城市后再选择附近");
							// 取消当前选择
							mainData.setSelectTag(false);
							subData.setSelectTag(false);
							// 回滚到前一选项
							if (selectedRegion != null) {
								selectedRegion.setSelectTag(true);
							}
							if (selectedDistrict != null) {
								selectedDistrict.setSelectTag(true);
							}
							return;
						}
						filter.setDistanceMeter(Integer.parseInt(subData.getUuid()));
						btFirst.setText(subData.getName());
						//
						filter.setRegionId("0");
						filter.setDistrictId("0");
					} else {

						// 如果主列表是“全部”，子列表要取真正的主列表UUID，在子的parentId里
						filter.setRegionId(String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid());
						filter.setDistrictId(subData.getUuid());
						//
						if(!CheckUtil.isEmpty(SessionManager.getInstance().getFilter().getRestId())){
						filter.setDistanceMeter(Integer.parseInt(subData.getUuid()));
						}else{
						filter.setDistanceMeter(0);	
						}
						if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
							btFirst.setText(mainData.getName());
						} else {
							btFirst.setText(subData.getName());
						}
					}
				}

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetResAndFoodTask();
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

				if (subData == null) {
					if (filter.getMainMenuId().equals(mainData.getUuid()) && filter.getSubMenuId().equals(String.valueOf(Settings.STATUTE_ALL))) {
						return;
					}
					filter.setMainMenuId(mainData.getUuid());
					filter.setSubMenuId(String.valueOf(Settings.STATUTE_ALL));
					btChannel.setText(mainData.getName());
				} else {
					if (filter.getMainMenuId().equals(mainData.getUuid()) && filter.getSubMenuId().equals(subData.getUuid())) {
						return;
					}
					filter.setMainMenuId(String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid());
					filter.setSubMenuId(subData.getUuid());
					if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
						btChannel.setText(mainData.getName());
					} else {
						btChannel.setText(subData.getName());
					}
				}
				updateTitle();

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetResAndFoodTask();
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
				btSort.setText(mainData.getName());

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetResAndFoodTask();
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btSort.setSelected(false);
			}
		});

	}

	/**
	 * 显示榜单条件的筛选框
	 */
	private void showTopFilter() {
		if (mTopList == null || mTopList.size() == 0) {
			DialogUtil.showAlert(this, "提示", "您选择的条件下没有榜单类别");
			return;
		}
		DialogUtil.showSelectionLinkListViewDropDown(getTitleLayout(), mTopList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (subData == null) {
					if (filter.getSubTopRestTypeId().equals(String.valueOf(Settings.STATUTE_ALL))) {
						return;
					}
					filter.setMainTopRestTypeId(mainData.getUuid());
					filter.setSubTopRestTypeId(String.valueOf(Settings.STATUTE_ALL));
					getBtnTitle().setText(mainData.getName());
				} else {
					if (filter.getMainTopRestTypeId().equals(mainData.getUuid()) && filter.getSubTopRestTypeId().equals(subData.getUuid())) {
						return;
					}
					filter.setMainTopRestTypeId(subData.getParentId());
					filter.setSubTopRestTypeId(subData.getUuid());
					if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
						if (mainData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
							getBtnTitle().setText("全部榜单");
						} else {
							getBtnTitle().setText(mainData.getName());
						}
					} else {
						getBtnTitle().setText(subData.getName());
					}
				}

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeGetResAndFoodTask();
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
	}

	/**
	 * 设置筛选按钮的状态
	 * 
	 * @param state
	 */
	private void setButtonState(boolean state) {
		btFirst.setClickable(state);
		btChannel.setClickable(state);
		btSort.setClickable(state);
		getBtnTitle().setClickable(state);
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
			return -1;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUuid().equals(id)) {
				return i;
			}
		}
		return -1;
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
			btnShowKey.setText(key);
		}
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		if (adapter != null) {
			Iterator<MyImageView> iterator = adapter.viewList.iterator();
			while (iterator.hasNext()) {
				iterator.next().recycle(true);
			}
			adapter.viewList.clear();
			System.gc();
		}
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		recycle();
		if (getRestListTask != null) {
			getRestListTask.cancel(true);
			// 设置显示图片的标志
			if ("1".equals(SessionManager.getInstance().getFilter().getChannelId())) {
				adapter.showPicture = SessionManager.getInstance().getShowRestPicture(this);
			} else {
				adapter.showPicture = true;
			}
			adapter.setList(null, false, SessionManager.getInstance().getFilter().getChannelId());
			lvResAndFood.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}

	/**
	 * 附近餐厅时更新菜单栏
	 */
	protected void createMenuPanel() {
		if (mListType == Settings.RES_AND_FOOD_LIST_TYPE_NEARBY) {
			btnMenuRefresh.setVisibility(View.VISIBLE);
			btnMenuRefresh.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mIsUseCellLoc = true;

					resetTask();

					// 设为第一页
					startIndex = 1;
					isLast = true;
					// 获得查询结果
					executeGetResAndFoodTask();
				}
			});
		} else {
			btnMenuRefresh.setVisibility(View.GONE);
		}
	}

	/**
	 * 更新标题栏
	 */
	private void updateTitle() {
		if (mHasTop) {
			// 搜餐厅
			getBtnOption().setVisibility(View.GONE);
			getTvTitleLayout().setVisibility(View.GONE);
			getBtnTitle().setVisibility(View.VISIBLE);

			if (!hasSelectedMainTop && !hasSelectedSubTop) {
				title = (filter.getDistanceMeter() > 0 ? "附近餐厅-" : "") + "全部榜单";
			} else {
				if (hasSelectedSubTop) {
					// 存在选中的子菜系，按钮文字为选中的子菜系名称
					title = (filter.getDistanceMeter() > 0 ? "附近-" : "") + selectedSubTopName;
				} else {
					// 不存在选中的子菜系，按钮文字为选中的主菜系名称
					title = (filter.getDistanceMeter() > 0 ? "附近-" : "") + selectedMainTopName;
				}
			}
		} else {
			// 该城市无榜单
			getBtnOption().setVisibility(View.INVISIBLE);
			getTvTitleLayout().setVisibility(View.VISIBLE);
			getBtnTitle().setVisibility(View.GONE);
			if (mListType == Settings.RES_AND_FOOD_LIST_TYPE_NEARBY) {
				title = getString(R.string.text_title_nearby);
			} else {
				title = "餐厅列表";
			}
		}
		getTvTitle().setText(title);
		getBtnTitle().setText(title);
	}

	private void showMyLoc() {
		try {
			// 获得gps
			double longitude = 0;
			double latitude = 0;
			if (Loc.isGpsAvailable()) {
				LocInfo myLoc = Loc.getLoc();
				if (myLoc == null || myLoc.getLoc() == null) {
					DialogUtil.showToast(ResAndFoodListActivity.this, ResAndFoodListActivity.this.getString(R.string.text_info_null_location));
				} else {
					longitude = myLoc.getLoc().getLongitude();
					latitude = myLoc.getLoc().getLatitude();
					// 修正当前位置
					JsonPack jp = A57HttpApiV3.getInstance().googlemap(ActivityUtil.getVersionName(ResAndFoodListActivity.this), ActivityUtil.getDeviceId(ResAndFoodListActivity.this), longitude,
							latitude);
					JSONObject jo = jp.getObj();
					if (jo.has("longitude") && jo.has("latitude")) {
						longitude = jo.getDouble("longitude");
						latitude = jo.getDouble("latitude");
					}
				}
				DecimalFormat decimalFormat = new DecimalFormat("###.########");

				StringBuffer sbParams = new StringBuffer("geo:");
				sbParams.append(decimalFormat.format(latitude));
				sbParams.append(',');
				sbParams.append(decimalFormat.format(longitude));
				sbParams.append("?q=");
				sbParams.append(decimalFormat.format(latitude));
				sbParams.append(',');
				sbParams.append(decimalFormat.format(longitude));
				sbParams.append("(").append(URLEncoder.encode(myLoc.getInfo().trim())).append(")");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(sbParams.toString()));
				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 根据当前类页面的状态刷新列表
	private void refreshList() {
		resetTask();
		// 设为第一页
		startIndex = 1;
		isLast = true;
		// 获得查询结果
		executeGetResAndFoodTask();
	}

	protected void onRefreshToNewAddress() {
		refreshList();
	}
}