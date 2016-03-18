package com.fg114.main.app.activity.Mdb;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.AdvertisementImgAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.SelectionListView;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MdbRestListDTO;
import com.fg114.main.service.dto.MdbRestListData;
import com.fg114.main.service.dto.RestListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.TakeoutIndexPageData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

public class MdbResListActivity extends MainFrameActivity {

	private boolean haveGpsTag = true;
	private List<MainPageAdvData> advList;
	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private LinearLayout showKeyLayout;
	private Button btnShowKey;
	private ListView lvResAndFood;
	private Button btFirst;
	private Button btChannel;
	private Button btSort;
	private ViewGroup dropdownAnchor;

	// 广告
	private View mdb_res_adv_layout;
	private CircleFlowIndicator advimgCircleIndicator;
	private ViewFlow advViewFlowimg;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。

	private static final String TAG_TYPE_SORT = "sort";
	private static final String TAG_TYPE_AVG = "avg";

	// 查询条件
	private int distanceMeter = 0;
	private String regionId = "";
	private String districtId = "";
	private String mainMenuId = "";
	private String subMenuId = "";
	private int sortTypeTag = 0;
	private int avgTag = 0;

	private List<RfTypeListDTO> mAreaList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeListDTO> mChannelList = new ArrayList<RfTypeListDTO>();
	private List<RfTypeDTO> mSortList = new ArrayList<RfTypeDTO>();
	private List<RfTypeDTO> mDistanceList = new ArrayList<RfTypeDTO>();
	
	private boolean hasLogined;// 判断是否登录

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		LocInfo loc = Loc.getLocImmediately();

		// 获得查询结果
		executeGetMdbRstTask(distanceMeter, regionId, districtId, mainMenuId, subMenuId, sortTypeTag, avgTag);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// resetTask();

		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 判断是否已登录并调整界面显示
		hasLogined = SessionManager.getInstance().isUserLogin(this);
	}


	private synchronized void tryDisplayAdvertisement() {
		// List<MainPageAdvData> advList =
		// SessionManager.getInstance().getMainPageAdvDataList();
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0) {
			mdb_res_adv_layout.setVisibility(View.VISIBLE);
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

	private void initComponent() {
		this.getTvTitle().setText("免单宝");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setWidth(UnitUtil.dip2px(25));
		this.getBtnOption().setHeight(UnitUtil.dip2px(25));
		this.getBtnOption().setBackgroundResource(R.drawable.mdb_rest_search);
		this.setLocationLayoutVisibility(View.VISIBLE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.mdb_res_list_activity, null);
		dropdownAnchor = (ViewGroup) contextView.findViewById(R.id.mdb_res_topLayout);
		lvResAndFood = (ListView) contextView.findViewById(R.id.mdb_res_listview);
		btFirst = (Button) contextView.findViewById(R.id.mdb_res_btFirst);
		btChannel = (Button) contextView.findViewById(R.id.mdb_res_btChannel);
		btSort = (Button) contextView.findViewById(R.id.mdb_res_btSort);
		mdb_res_adv_layout = contextView.findViewById(R.id.mdb_res_adv_layout);
		advViewFlowimg = (ViewFlow) contextView.findViewById(R.id.viewflow_img);
		advimgCircleIndicator = (CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator_img);
		advViewFlowimg.setFlowIndicator(advimgCircleIndicator);

		btFirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);

				showFirstFilter();
			}
		});

		btChannel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				v.setSelected(true);

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
		
		this.getBtnOption().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// TODO Auto-generated method stub
				if (hasLogined) {
					ActivityUtil.jump(MdbResListActivity.this, MdbConsumeEnsureActivity.class, 0,new Bundle());
				}else {
					DialogUtil.showToast(MdbResListActivity.this, "您未登录,请先登录");
					ActivityUtil.jump(MdbResListActivity.this, UserLoginActivity.class, 0);
				}
				
			}
		});

		advViewFlowimg.setAdapter(new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>()));

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	private void executeGetMdbRstTask(final int distanceMeter, final String regionId, final String districtId, final String mainMenuId, final String subMenuId, final int sortTypeTag, final int avgTag) {

		ListViewAdapter<MdbRestListData> adapter = new ListViewAdapter<MdbRestListData>(R.layout.mdb_res_list_item, new ListViewAdapter.OnAdapterListener<MdbRestListData>() {

			@Override
			public void onRenderItem(ListViewAdapter<MdbRestListData> adapter, ViewHolder holder, final MdbRestListData data) {
				// TODO Auto-generated method stub
				MyImageView mdb_rest_image = (MyImageView) holder.$iv(R.id.mdb_rest_image);
				TextView mdb_rest_name = holder.$tv(R.id.mdb_rest_name);
				TextView mdb_rest_distance = holder.$tv(R.id.mdb_rest_distance);
				TextView mdb_rest_reason = holder.$tv(R.id.mdb_rest_reason);
				TextView mdb_rest_avg = holder.$tv(R.id.mdb_rest_avg);
				TextView mdb_rest_freeNum = holder.$tv(R.id.mdb_rest_freeNum);
				TextView mdb_icon_pic = holder.$tv(R.id.mdb_icon_pic);
				RatingBar mdb_rest_showFlower = (RatingBar) holder.$(R.id.mdb_rest_showFlower);
				View list_item_dishorderLayout=holder.$(R.id.list_item_dishorderLayout);
                View mdb_rest_detail_flower=holder.$(R.id.mdb_rest_detail_flower);
                View mdb_rest_detail_line=holder.$(R.id.mdb_rest_detail_line);
				if(data.showFlowerTag){
					mdb_rest_detail_flower.setVisibility(View.VISIBLE);
					mdb_rest_detail_line.setVisibility(View.VISIBLE);
					if (!CheckUtil.isEmpty(data.freePct)) {
						mdb_icon_pic.setVisibility(View.VISIBLE);
						
						mdb_icon_pic.setText(data.freePct);
					}
				}else{
					mdb_rest_detail_line.setVisibility(View.GONE);
					mdb_rest_detail_flower.setVisibility(View.GONE);
					mdb_icon_pic.setVisibility(View.GONE);
				}
				if (!CheckUtil.isEmpty(data.picUrl)) {
					mdb_rest_image.setImageByUrl(data.picUrl, false, 0, ScaleType.FIT_XY);
				}
				mdb_rest_name.setText(data.restName);
				mdb_rest_distance.setText(data.distance);
				mdb_rest_reason.setText(data.reason);
				mdb_rest_avg.setText(data.avg);
				

				if (!CheckUtil.isEmpty(data.freeNum)) {
					mdb_rest_freeNum.setText(Html.fromHtml(data.freeNum));
				}

				mdb_rest_showFlower.setRating(data.flowerNum);
				
				list_item_dishorderLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						Bundle bundle=new Bundle();
						bundle.putString(Settings.UUID, data.restId);
						ActivityUtil.jump(MdbResListActivity.this, MdbRestDetaiActivity.class, 0,bundle);
					}
				});

			};

			@Override
			public void onLoadPage(final ListViewAdapter<MdbRestListData> adapter, int startIndex, int pageSize) {

				ServiceRequest request = new ServiceRequest(API.getMdbRestList);
				request.addData("distanceMeter", distanceMeter);
				request.addData("regionId", regionId);
				request.addData("districtId", districtId);
				request.addData("mainMenuId", mainMenuId);
				request.addData("subMenuId", subMenuId);
				request.addData("sortTypeTag", sortTypeTag);
				request.addData("avgTag", avgTag);
				request.addData("pageSize", pageSize);
				request.addData("startIndex", startIndex);

				CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<MdbRestListDTO>() {

					@Override
					protected void onSuccess(MdbRestListDTO dto) {

						ListViewAdapter.AdapterDto<MdbRestListData> adapterDto = new ListViewAdapter.AdapterDto<MdbRestListData>();
						adapterDto.setList(dto.list);
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);

						if (dto != null) {
							

							if (CheckUtil.isEmpty(districtId)&&CheckUtil.isEmpty(regionId)) {
								btFirst.setText("全部地域");
							} else {
								if(!CheckUtil.isEmpty(districtId)){
									btFirst.setText(dto.selectDistrictName);
								}else {
									btFirst.setText(dto.selectRegionName);
								}
								
							}

							if (CheckUtil.isEmpty(subMenuId)&&CheckUtil.isEmpty(mainMenuId)) {
								btChannel.setText("全部菜系");
							} else {
								if(!CheckUtil.isEmpty(subMenuId)){
								btChannel.setText(dto.selectSubMenuTypeName);
								}else{
								btChannel.setText(dto.selectMainMenuTypeName);
								}
							}

							btSort.setText(dto.selectSortName);
							
							updateFilter(dto);

							// 显示广告
							if (dto.advList != null && dto.advList.size() != 0) {
								mdb_res_adv_layout.setVisibility(View.VISIBLE);
								advList=dto.advList;
								tryDisplayAdvertisement();
							} else {
								mdb_res_adv_layout.setVisibility(View.GONE);
							}
							
							
							// 如果是附近，修正下选择项
							if (distanceMeter != 0) {
								btFirst.setText(distanceMeter+"米");

								setLocationLayoutVisibility(View.VISIBLE);
							} else {
								setLocationLayoutVisibility(View.GONE);
							}



						}
					}

					@Override
					protected void onError(int code, String message) {
                      super.onError(code, message);
					}
				});

			}

		});
		adapter.setExistPage(true); // 此句代码必须在"adapter.setListView(listview)"之前
		adapter.setmCtx(MdbResListActivity.this); // 若需要用到的Context是Activity，则需要手动设置mCtx，否则默认是Application，注:此句代码也必须在"adapter.setListView(listview)"之前
		adapter.setListView(lvResAndFood); //
	}

	/**
	 * 更新所有过滤器
	 * 
	 * @param dto
	 */
	private void updateFilter(MdbRestListDTO dto) {
		if (dto == null) {
			return;
		}
		updateFirstFilter(dto);
		updateChannelFilter(dto);
		updateSortFilter(dto);
	}

	/**
	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(MdbRestListDTO dto) {

		mAreaList.clear();

		/*
		 * 新逻辑：一 如果是地铁站搜索则 1)不需要 附近 2)全部区域 变成 全部线路 二 如果是餐厅附近查找 1)只显示餐厅附近距离
		 */

		// 手工添加“附近区域”，放在全部下面

		RfTypeListDTO nearRegionDto = new RfTypeListDTO();
		nearRegionDto.setUuid(Settings.STATUTE_CHANNEL_NEARBY);
		mDistanceList = SessionManager.getInstance().getListManager().getDistanceList(this);
		nearRegionDto.setName("附近");

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

		// 如果是餐厅附近查找 不要全部地域
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

		// 如果是餐厅附近查找 不要全部地域
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
		if (distanceMeter != 0) {
			int mSelectedDistance = getPositionInRfTypeDTOList(mDistanceList, String.valueOf(distanceMeter));
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
	private void updateChannelFilter(MdbRestListDTO dto) {

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
	private void updateSortFilter(MdbRestListDTO dto) {

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
					sortTypeTag = Integer.parseInt(selectedDto.getUuid());
					avgTag = 0;
				} else if (selectedDto.getMemo().equals(TAG_TYPE_AVG)) {
					sortTypeTag = 0;
					avgTag = Integer.parseInt(selectedDto.getUuid());
				}
				btSort.setText(mSortList.get(i).getName());
			} else {
				btSort.setText(mSortList.get(1).getName());
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

				if (subData == null) {
					if (regionId.equals(mainData.getUuid()) && districtId.equals(String.valueOf(Settings.STATUTE_ALL))) {
						return; // 选择的是同一项
					}
					regionId = mainData.getUuid();
					districtId = "";
					btFirst.setText(mainData.getName());

				} else {
					//
					boolean changed = false;
					if (distanceMeter != 0 && !mainData.getUuid().equals(String.valueOf(Settings.STATUTE_CHANNEL_NEARBY))) {
						changed = true;
					}

					if (!regionId.equals(mainData.getUuid()) || !districtId.equals(subData.getUuid())) {
						changed = true;
					}
					if (!changed) {// 如果条件没有变，不执行动作
						return;
					}

					// 如果主列表是“全部”，子列表要取真正的主列表UUID，在子的parentId里
					regionId = String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid();
					districtId = subData.getUuid();
					//

					if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
						btFirst.setText(mainData.getName());
					} else {
						btFirst.setText(subData.getName());
					}

				}
				distanceMeter=Integer.parseInt(subData.getUuid());

				// 获得查询结果
				executeGetMdbRstTask(distanceMeter, regionId, districtId, mainMenuId, subMenuId, sortTypeTag, avgTag);
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
					if (mainMenuId.equals(mainData.getUuid()) && subMenuId.equals(String.valueOf(Settings.STATUTE_ALL))) {
						return;
					}
					mainMenuId = mainData.getUuid();
					subMenuId = String.valueOf(Settings.STATUTE_ALL);
					btChannel.setText(mainData.getName());
				} else {
					if (mainMenuId.equals(mainData.getUuid()) && subMenuId.equals(subData.getUuid())) {
						return;
					}
					mainMenuId = String.valueOf(Settings.STATUTE_ALL).equals(mainData.getUuid()) ? subData.getParentId() : mainData.getUuid();
					subMenuId = subData.getUuid();
					if (subData.getUuid().equals(String.valueOf(Settings.STATUTE_ALL))) {
						btChannel.setText(mainData.getName());
					} else {
						btChannel.setText(subData.getName());
					}
				}
				// 获得查询结果
				executeGetMdbRstTask(distanceMeter, regionId, districtId, mainMenuId, subMenuId, sortTypeTag, avgTag);
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
					if (sortTypeTag == Integer.parseInt(mainData.getUuid()) && avgTag == 0) {
						return;
					}
					sortTypeTag = Integer.parseInt(mainData.getUuid());
					avgTag = 0;
				} else if (mainData.getMemo().equals(TAG_TYPE_AVG)) {
					if (sortTypeTag == 0 && avgTag == Integer.parseInt(mainData.getUuid())) {
						return;
					}
					sortTypeTag = 0;
					avgTag = Integer.parseInt(mainData.getUuid());
				}
				btSort.setText(mainData.getName());

				// 获得查询结果
				executeGetMdbRstTask(distanceMeter, regionId, districtId, mainMenuId, subMenuId, sortTypeTag, avgTag);
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
}
