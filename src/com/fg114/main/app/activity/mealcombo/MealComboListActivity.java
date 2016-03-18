package com.fg114.main.app.activity.mealcombo;

import java.net.*;
import java.text.*;
import java.util.*;

import org.json.*;

import android.content.*;
import android.graphics.Rect;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.PopupWindow.OnDismissListener;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.*;
import com.fg114.main.app.activity.*;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.MealComboFilter;
import com.fg114.main.app.location.*;
import com.fg114.main.app.view.*;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;

/**
 * 特惠套餐列表界面
 * 
 * @author xujianjun,2012-07-23
 * 
 */
public class MealComboListActivity extends MainFrameActivity
{

	private static final String TAG = "MealComboListActivity";

	// 传入参数
	private int fromPage; // 返回页面

	// 缓存数据
	private MealComboFilter filter; // 查询条件
	// 画面变量
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;

	private int mSelectedSort;
	private int mSelectedRegion;
	private int mSelectedMenu;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView listView;
	private MealComboListAdapter adapter;
	private DragLoadingView dragview_rest_list;

	private Button firstButton;
	private Button btSort;
	private Button btMenu;

	private List<RfTypeDTO> mSortList = new ArrayList<RfTypeDTO>();
	private List<RfTypeDTO> regionList = new ArrayList<RfTypeDTO>();
	private List<RfTypeDTO> menuList = new ArrayList<RfTypeDTO>();

	private boolean isMealCombo=false; //默认显示的是现金券列表
	private boolean isQuickJump=false; //是否是首页附近套餐提醒快速跳转过来的
	
	//广告
	private List<MainPageAdvData> advList;
	private View new_meal_combo_adv_layout;
	private ViewFlow advViewFlowimg;
	private CircleFlowIndicator advimgCircleIndicator;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。

	// 任务
	private GetMealComboListTask task;
	
	
	//当前位置刷新后回调接口
	Runnable run=new Runnable()
	{
		
		@Override
		public void run()
		{
			resetTask();
			// 设为第一页
			startIndex = 1;
			isLast = true;
			// 获得查询结果
			executeTask();
			
		}
	};

	private ViewGroup dropdownAnchor;

	private boolean needHideBackButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("现金券列表", "");
		//----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
			needHideBackButton=bundle.getBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON,false);
		}
//		isMealCombo = bundle.getBoolean(Settings.BUNDLE_KEY_IS_MEALCOMBO,false); //是否是套餐
		isQuickJump = bundle.getBoolean(Settings.BUNDLE_KEY_IS_QUICK_JUMP,false); //是否是首页附近套餐提醒快速跳转过来的
		// 获得缓存数据
		filter = new MealComboFilter();
		//是首页附近套餐提醒快速跳转过来的，筛选条件默认为 附近
		if(isQuickJump)
		{
			filter.setDistanceMeter(5000);
			filter.setRegionId("");
		}
		else
		{
			filter.setDistanceMeter(0);
			filter.setRegionId("");
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
		
//		selectedDistanceMeterUuid = 0;// 默认选择5000米
		//selectedDistanceMeterUuid=5000;//默认选择5000米
		resetTask();
		executeTask();

	}
	@Override
	public void onRestart() {
		super.onRestart();
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("现金券列表", "");
		//----------------------------
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// 获得查询结果，每次都刷一下列表
//		executeTask();
	}
	@Override
	protected void onPause()
	{
		super.onPause();
//		resetTask();
		
		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	@Override
	public void finish()
	{
		super.finish();
		filter.reset();
		resetTask();

	}

	
	private synchronized void tryDisplayAdvertisement() {
		// List<MainPageAdvData> advList =
		// SessionManager.getInstance().getMainPageAdvDataList();
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0) {
			new_meal_combo_adv_layout.setVisibility(View.VISIBLE);
			if (advList.size() == 1) {
				advimgCircleIndicator.setVisibility(View.GONE);
			} else {
				advimgCircleIndicator.setVisibility(View.VISIBLE);
			}

			// 确保只有一个运行的线程
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			// ---------------------
			advViewFlowimg.setAdapter(new AdvertisementImgAdapter(this, advList));
			// 广告自动滚动的线程，４秒
			playAdvertisement = new Thread(new Runnable() {
				int i = 0;

				@Override
				public void run() {
					try {
						int count = advViewFlowimg.getAdapter().getCount();
						while (count > 1) {
							Thread.sleep(4000);
							if (playCoolingTime > System.currentTimeMillis()) {
								continue;
							}
							i = advViewFlowimg.getSelectedItemPosition();
							i = (i + 1) % count;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									advViewFlowimg.setSelection(i);

								}
							});
							count = advViewFlowimg.getAdapter().getCount();
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			});
			playAdvertisement.start();
			// 广告手动滑动
			advViewFlowimg.setOnTouchListener(advTouchListener);
			// --
		} else {
			// 没有广告时，撤消线程，清除数据
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}

			AdvertisementImgAdapter adapter = new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>());
			advViewFlowimg.setAdapter(adapter);
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
	private void initComponent()
	{

		
		// 设置标题栏
		
		getBtnGoBack().setText("返回");
		getTvTitle().setText("现金券");
		getBtnOption().setVisibility(View.INVISIBLE);
//		if(needHideBackButton){
			this.getBtnGoBack().setVisibility(View.INVISIBLE);
//		}

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.meal_combo_list, null);
		listView = (ListView) contextView.findViewById(R.id.listview);
		firstButton = (Button) contextView.findViewById(R.id.firstButton);
		btSort = (Button) contextView.findViewById(R.id.thirdButton);
		btMenu = (Button) contextView.findViewById(R.id.menuButton);
		dropdownAnchor = (ViewGroup) contextView.findViewById(R.id.meal_combo_list_llFilter);
		dragview_rest_list=(DragLoadingView) contextView.findViewById(R.id.dragview_rest_list);
		new_meal_combo_adv_layout= contextView.findViewById(R.id.new_meal_combo_adv_layout);
		advViewFlowimg=(ViewFlow) contextView.findViewById(R.id.viewflow_img);
		advimgCircleIndicator=(CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator_img);
		advViewFlowimg.setFlowIndicator(advimgCircleIndicator);
		firstButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("地域下拉框");
				// -----
				showFirstFilter();
			}
		});

		btSort.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("排序下拉框");
				// -----
				showSortFilter();
			}
		});
		
		btMenu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);
				// -----
				OpenPageDataTracer.getInstance().addEvent("菜系下拉框");
				// -----
				showMenuFilter();
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
				refreshList();
			}

			@Override
			public boolean isAllowDrag() {

				if (listView == null || listView.getChildCount() <= 0) {
					return false;
				}
				View v = listView.getChildAt(0);
				Rect r = new Rect();
				boolean allow = v.getLocalVisibleRect(r);
				if (r.top == 0) {
					return true;
				}
				return false;
			}
		});

		// 有重试逻辑的adapter
		adapter = new MealComboListAdapter(this, new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				executeTask();
			}
		});

		adapter.setList(null, false);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AbsListView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				int index = arg2;
				List<CashCouponData> list = ((MealComboListAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					CashCouponData data = list.get(index);
					if (data != null && data.getStateTag()!=Settings.CONTRL_ITEM_ID) {
						//-----
						OpenPageDataTracer.getInstance().addEvent("选择行");	
						//-----
						// 去套餐详细页面
						Bundle bundle = new Bundle();
//						bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, data);
//						bundle.putBoolean(Settings.BUNDLE_KEY_IS_MEALCOMBO, true);
						bundle.putString(Settings.UUID, data.uuid);
						ActivityUtil.jump(MealComboListActivity.this, GroupBuyDetailActivity.class, 0, bundle);
					}
				}
			}
		});

		listView.setOnScrollListener(new AbsListView.OnScrollListener()
		{
			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----
				}
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 线程安全且不是最后一页的场合，获得餐厅列表
						executeTask();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					// 当到达列表尾部时
					isRefreshFoot = true;

				} else {
					isRefreshFoot = false;
				}
			}
		});

		// 先隐藏顶部筛选条件
		//contextView.findViewById(R.id.meal_combo_list_llFilter).setVisibility(View.GONE);

		advViewFlowimg.setAdapter(new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>()));
		
		
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}
	//根据当前类页面的状态刷新列表
	private void refreshList() {
		isTaskSafe = true;
		isLast = true;
		isRefreshFoot = false;
		startIndex = 1;
		adapter.setList(null, false);
		executeTask();
	}
	protected void onRefreshToNewAddress()
	{
		refreshList();
	}
	/**
	 * 获得餐厅列表
	 */
	private void executeTask()
	{

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				//startIndex = startIndex + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}

		// 设置按钮不可点击
		firstButton.setClickable(false);
		btSort.setClickable(false);

		// 创建任务，获取套餐或者现金券列表
		task = new GetMealComboListTask(null, this, startIndex,filter,isMealCombo);

		task.setCallBack(new BaseTask.Callback()
		{

			@Override
			public void onNetworkFail(JsonPack result)
			{
				adapter.addList(new ArrayList<CashCouponData>(), false);
				executeTask();
			}
		});
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		// 执行任务
		task.execute(new Runnable()
		{

			@Override
			public void run()
			{
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				CashCouponList2DTO dto = task.dto;
				getBtnOption().setClickable(true);
				
				if (dto != null) {
					updateFilter(dto);
					isLast = dto.pgInfo.lastTag;
					startIndex=dto.pgInfo.nextStartIndex;
					adapter.addList(dto.getList(), isLast);
//					if(!isMealCombo &&!dto.isHaveMealComboTag()){
//						//现金券，并且没有套餐，隐藏按钮						
//						getBtnOption().setVisibility(View.INVISIBLE);			
//					}
//					else{
//						//套餐
//						getBtnOption().setVisibility(View.VISIBLE);
//					}
//					doGA();

					// 显示广告
					if (dto.advList != null && dto.advList.size() != 0) {
						new_meal_combo_adv_layout.setVisibility(View.VISIBLE);
						advList=dto.advList;
						tryDisplayAdvertisement();
					} else {
						new_meal_combo_adv_layout.setVisibility(View.GONE);
					}
				}

				// 设置线程安全
				isTaskSafe = true;
				firstButton.setClickable(true);
				btSort.setClickable(true);
				
				if (filter.getDistanceMeter() != 0) {
					//setLocPanel(true, run);
				} else {
					//setLocPanel(false, run);
				}
				
				dragview_rest_list.reset();

			}
		}, new Runnable()
		{
			@Override
			public void run()
			{
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 设置线程安全
				getBtnOption().setClickable(true);
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<CashCouponData>(), isLast);
				firstButton.setClickable(true);
				btSort.setClickable(true);
				
				dragview_rest_list.reset();
			}
		});
	}

	/**
	 * 更新所有过滤器
	 * 
	 * @param dto
	 */
	private void updateFilter(CashCouponList2DTO dto)
	{
		if (dto == null) {
			return;
		}

		updateFirstFilter(dto);
		updateMenuFilter(dto);
		updateSortFilter(dto);
	}

	/**
	 * 更新距离过滤器，
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(CashCouponList2DTO dto)
	{
		if (startIndex != 1) {
			return;
		}
		regionList.clear();
		
		//附近区域
		RfTypeDTO nearRegionDto = new RfTypeDTO();
		nearRegionDto.setUuid(Settings.STATUTE_CHANNEL_NEARBY);
		nearRegionDto.setName("附近");
		// 全部地域
		RfTypeDTO allRegionDto = new RfTypeDTO();
		allRegionDto.setUuid(Settings.STATUTE_CHANNEL_ALL_REGION);
		allRegionDto.setName("全部地域");

		RfTypeDTO emptyDto = new RfTypeDTO();
		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		emptyDto.setName("");
		
		regionList.add(nearRegionDto);
		regionList.add(allRegionDto);
		
		if (dto.getRegionList() != null && dto.getRegionList().size() > 0) {
			regionList.addAll(changeComToRf(dto.getRegionList()));
		}

		if (regionList.size() == 0) {
			regionList.add(emptyDto);
			mSelectedRegion = 0;
		} else {
			RfTypeDTO selectedDto = null;
			for (int i = 0; i < regionList.size(); i++) {
				if (regionList.get(i).isSelectTag()) {
					mSelectedRegion = i;
					selectedDto = regionList.get(i);
					break;
				}
			}
			if (selectedDto != null && !TextUtils.isEmpty(selectedDto.getUuid())) {

				filter.setRegionId(selectedDto.getUuid());
				firstButton.setText(regionList.get(mSelectedRegion).getName());
			} else {
				if(filter.getDistanceMeter() != 0)
				{
					mSelectedRegion = 0;
					firstButton.setText(regionList.get(0).getName());
				}
				else
				{
					mSelectedRegion = 1;
					firstButton.setText(regionList.get(1).getName());
				}
				
			}
		}

		

	}
	
	private void updateMenuFilter(CashCouponList2DTO dto)
	{
		if (startIndex != 1) {
			return;
		}
		menuList.clear();
		
		// 全部菜系
		RfTypeDTO allRegionDto = new RfTypeDTO();
		allRegionDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		allRegionDto.setName("全部菜系");

		RfTypeDTO emptyDto = new RfTypeDTO();
		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		emptyDto.setName("");
		
		menuList.add(allRegionDto);
		
		if (dto.getMenuList() != null && dto.getMenuList().size() > 0) {
			menuList.addAll(changeComToRf(dto.getMenuList()));
		}

		if (menuList.size() == 0) {
			menuList.add(emptyDto);
			mSelectedMenu = 0;
		} else {
			RfTypeDTO selectedDto = null;
			for (int i = 0; i < menuList.size(); i++) {
				if (menuList.get(i).isSelectTag()) {
					mSelectedMenu = i;
					selectedDto = menuList.get(i);
					break;
				}
			}
			if (selectedDto != null && !TextUtils.isEmpty(selectedDto.getUuid())) {
				filter.setMainMenuId(selectedDto.getUuid());
				btMenu.setText(menuList.get(mSelectedMenu).getName());
			} else {
				mSelectedMenu = 0;
				btMenu.setText(menuList.get(0).getName());
			}
		}
	}

	/**
	 * 更新类别过滤器
	 * 
	 * @param dto
	 */
	private void updateSortFilter(CashCouponList2DTO dto)
	{

		if (startIndex != 1) {
			return;
		}
		mSortList.clear();

		
		RfTypeDTO emptyDto = new RfTypeDTO();
		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		emptyDto.setName("");

		if (dto.getSortTypeList() != null && dto.getSortTypeList().size() > 0) {

			mSortList.addAll(changeComToRf(dto.getSortTypeList()));
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

				filter.setSortTypeTag(selectedDto.getUuid());
				btSort.setText(mSortList.get(mSelectedSort).getName());
			} else {
				mSelectedSort = 0;
				btSort.setText(mSortList.get(0).getName());
			}
		}
	}
	
	private void showFirstFilter()
	{
		
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, regionList, new OnSelectedListener() {
			
			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (filter.getMainMenuId().equals(mainData.getUuid())) {
					return;
				}

				if(mainData.getUuid().equals(Settings.STATUTE_CHANNEL_NEARBY))
				{
					    filter.setDistanceMeter(5000);
					    filter.setSortTypeTag("");
						filter.setRegionId("");
						filter.setMainMenuId("");
						//是附近，显示定位层
						setLocationLayoutVisibility(View.VISIBLE);
					
				} else {
					setLocationLayoutVisibility(View.GONE);
					
					if(mainData.getUuid().equals(Settings.STATUTE_CHANNEL_ALL_REGION))
					{
						filter.setRegionId("");
						filter.setDistanceMeter(0);
					}
					else
					{
						//表示是从附近和全部区域间切换
						if(filter.getDistanceMeter()!=0)
						{
							filter.setDistanceMeter(0);
							filter.setSortTypeTag("");
							filter.setMainMenuId("");
						}
						
						filter.setRegionId(mainData.getUuid());
					}
				}
				
				
				firstButton.setText(mainData.getName());

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeTask();
			}
		},new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				firstButton.setSelected(false);
			}
		});
	}

	/**
	 * 显示菜系筛选框
	 */
	private void showMenuFilter()
	{
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, menuList, new OnSelectedListener() {
			
			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (filter.getRegionId().equals(mainData.getUuid())) {
					return;
				}

				if(mainData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL)))
				{
					filter.setMainMenuId("");
					
				} else {
					filter.setMainMenuId(mainData.getUuid());
				}
				
				
				btMenu.setText(mainData.getName());

				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeTask();
			}
		},new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				btMenu.setSelected(false);
			}
		});
	}

	/**
	 * 显示类别筛选框
	 */
	private void showSortFilter() {
		DialogUtil.showSelectionListViewDropDown(dropdownAnchor, mSortList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (filter.getSortTypeTag().equals(mainData.getUuid())) {
					return;
				}
				filter.setSortTypeTag(mainData.getUuid());
				btSort.setText(mainData.getName());
				resetTask();
				// 设为第一页
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				executeTask();
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btSort.setSelected(false);
			}
		});

	}

	/**
	 * 回收内存
	 */
	private void recycle()
	{
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
	private void resetTask()
	{
		recycle();
		if (task != null) {
			isTaskSafe = true;
			isLast = true;
			isRefreshFoot = false;
			startIndex = 1;
			task.cancel(true);
			adapter.setList(null, false);
			listView.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}

	// 从id获取name
	private String getNameFromListById(List<RfTypeDTO> list, String id)
	{
		String name = "";
		if (list == null || id == null) {
			return name;
		}
		// ---
		for (RfTypeDTO data : list) {
			if (data != null && id.equals(data.getUuid())) {
				return data.getUuid();
			}
		}
		return name;
	}

	/**
	 * 返回指定id项在列表中的位置下标
	 * 
	 * @param list
	 * @param id
	 * @return
	 */
	private int getPositionInRfTypeDTOList(List<RfTypeDTO> list, String id)
	{
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

	private int getPositionInRfTypeListDTOList(List<RfTypeListDTO> list, String id)
	{
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
	
	public List<RfTypeDTO> changeComToRf(List<CommonTypeDTO> dtoList)
	{
		List<RfTypeDTO> rftypeList=new ArrayList<RfTypeDTO>();
		for(int i=0;i<dtoList.size();i++)
		{
			RfTypeDTO rfDto=new RfTypeDTO();
			rfDto.setName(dtoList.get(i).getName());
			rfDto.setUuid(dtoList.get(i).getUuid());
			rfDto.setSelectTag(dtoList.get(i).isSelectTag());
			rfDto.setMemo(dtoList.get(i).getMemo());
			rftypeList.add(rfDto);
		}
		
		return rftypeList;
		
	}

}