package com.fg114.main.app.activity;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.SelectionListView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.RfTypeListPackDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;

/**
 * 全部商区选择页面
 * 
 * @author nieyinyin
 */
public class DistrictChoosingActivity extends MainFrameActivity {

	private static final String TAG = "DistrictChoosingActivity";
	/** 地域、商区缓存数据KEY */
	// 热门商区key
	private static final String KEY_REGION_DISTRICT_LIST = "key_region_district_list";
	private static final String KEY_REGION_MENU_LIST = "key_menu_list";
	private static final String KEY_REGION_SUBWAY_LIST = "key_subway_list";
	private static final int LIST_CACHE_TIME = 1 * 24 * 60; // 缓存时间一天，即1*24*60分钟

	private Context mCtx = this;
	private SelectionListView slv;

	private String pageName = "全部商区";
	// 页面类型：1：全部商区 2：全部菜系 3：全部地铁沿线
	private int type = 1;
	private String subwayUuid = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		pageName = bundle.getString(Settings.BUNDLE_FUNC_NAME);
		if (pageName == null) {
			DialogUtil.showToast(this, "非法数据");
			finish();
			return;
		}
		// ---
		if (pageName.equals("全部商区")) {
			type = 1;
		} else if (pageName.equals("全部菜系")) {
			type = 2;
		} else if (pageName.equals("全部地铁沿线")) {
			subwayUuid = bundle.getString(Settings.BUNDLE_KEY_ID);
			type = 3;
		} else {
			DialogUtil.showToast(this, "非法数据");
			finish();
			return;
		}
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage(pageName, "");
		// ----------------------------

		initComponent();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage(pageName, "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	void initComponent() {
		// 顶部
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getTvTitle().setText(pageName);

		LinearLayout contentView = new LinearLayout(mCtx);
		slv = new SelectionListView(mCtx);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		contentView.addView(slv, lp);
		slv.setVisibility(View.GONE);

		slv.setOnSelectedListener(new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {

				if (subData == null) {
					return;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent(type == 1 ? "商区按钮" : (type == 2 ? "菜系按钮" : "地铁按钮"), subData.getParentId() + "," + subData.getUuid());
				// -----
				// 设置筛选条件
				SessionManager.getInstance().getFilter().setSubwayTag(false);
				SessionManager.getInstance().getFilter().setDistanceMeter(0);
				SessionManager.getInstance().getFilter().setRegionId("0");
				SessionManager.getInstance().getFilter().setDistrictId("0");
				SessionManager.getInstance().getFilter().setMainMenuId("0");
				SessionManager.getInstance().getFilter().setSubMenuId("0");
				SessionManager.getInstance().getFilter().setKeywords("");
				if (type == 1) {
					SessionManager.getInstance().getFilter().setRegionId(subData.getParentId()); // 设置地域Id（全部地域uuid为0）
					SessionManager.getInstance().getFilter().setDistrictId(subData.getUuid());// 设置商区Id（全部商区uuid为0）
				} else if (type == 2) {
					SessionManager.getInstance().getFilter().setMainMenuId(subData.getParentId());
					SessionManager.getInstance().getFilter().setSubMenuId(subData.getUuid());
				} else if (type == 3) {
					SessionManager.getInstance().getFilter().setSubwayTag(true);
					SessionManager.getInstance().getFilter().setRegionId(subData.getParentId());
					SessionManager.getInstance().getFilter().setDistrictId(subData.getUuid());
				}

				// 去餐厅美食列表页
				ActivityUtil.jump(mCtx, ResAndFoodListActivity.class, 0, null);
			}
		});
		this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		if (type == 1) {
			loadDistrictData();
		} else if (type == 2) {
			loadMenuData();
		} else if (type == 3) {
			loadSubwayData();
		}
	}

	// ---
	void loadDistrictData() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		ServiceRequest request = new ServiceRequest(API.getRegionDistrictList2);
		// RfTypeListDTO 中 u n: 地域的uuid,name list :地域里包含的商区列表
		// RfTypeDTO 中 u n c : 商区的uuid,name,首字母
		CommonTask.request(request, new CommonTask.TaskListener<RfTypeListPackDTO>() {
			protected void onSuccess(RfTypeListPackDTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				if (dto == null) {
					finish();
					return;
				}
				List<RfTypeListDTO> listData = dto.list;
				if (listData != null) {
					// 构造全部商区
					RfTypeListDTO allRegion = new RfTypeListDTO();
					allRegion.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allRegion.setName("全部地域");
					allRegion.setSelectTag(true);
					allRegion.setIsNeedGroupBy(true);
					for (RfTypeListDTO item : dto.list) {
						/**
						 * 这个逻辑注意下:
						 * 服务端默认selectTag为true;(通过这个字段告诉客户端“全部地域”哪些商区不显示，
						 * 其实就是为了剔除多余的“其他”和夸地域的商区)
						 * 但是控件
						 * SelectionListView里面是根据selectTag判断item是否为选中状态，所以这里
						 * ，要将所有的selectTag置为false
						 */
						if (item.isSelectTag()) {
							item.setSelectTag(false);
						}
						RfTypeDTO allSubDistrict = new RfTypeDTO();
						allSubDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
						allSubDistrict.setName("全部" + item.getName());
						allSubDistrict.setParentId(item.getUuid());
						item.getList().add(0, allSubDistrict);
						for (RfTypeDTO commonTypeDTO : item.getList()) {
							if (commonTypeDTO.isSelectTag()) {
								commonTypeDTO.setSelectTag(false);
								commonTypeDTO.setParentId(item.getUuid());
								allRegion.getList().add(commonTypeDTO);
							}
						}
					}
					RfTypeDTO allDistrict = new RfTypeDTO();
					allDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allDistrict.setName("-- 全部商区 --");
					allDistrict.setParentId(allRegion.getUuid());
					allRegion.getList().add(0, allDistrict);

					listData.add(0, allRegion);

					// 控制UI显隐
					slv.setData(listData);
					slv.setVisibility(View.VISIBLE);
				}

			};

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
			}

			@Override
			protected void defineCacheKeyAndTime(CommonTask.TaskListener.CacheKeyAndTime keyAndTime) {
				CityInfo city=SessionManager.getInstance().getCityInfo(DistrictChoosingActivity.this);
				keyAndTime.cacheKey = KEY_REGION_DISTRICT_LIST+"|"+city.getId();
				keyAndTime.cacheTimeMinute = LIST_CACHE_TIME; // 缓存时间一天，即1*24*60分钟
			}

		});
	}
	
	void loadMenuData() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		ServiceRequest request = new ServiceRequest(API.getMainMenuSubMenuList);
		// RfTypeListDTO 中 u n: 是uuid,name list :列表
		// RfTypeDTO 中 u n c : 是uuid,name,首字母
		CommonTask.request(request, new CommonTask.TaskListener<RfTypeListPackDTO>() {
			protected void onSuccess(RfTypeListPackDTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				if (dto == null) {
					finish();
					return;
				}
				List<RfTypeListDTO> listData = dto.list;
				if (listData != null) {
					// 构造全部
					RfTypeListDTO allRegion = new RfTypeListDTO();
					allRegion.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allRegion.setName("全部菜系");
					allRegion.setSelectTag(true);
					allRegion.setIsNeedGroupBy(true);
					for (RfTypeListDTO item : dto.list) {
						/**
						 * 这个逻辑注意下:
						 * 服务端默认selectTag为true;(通过这个字段告诉客户端“全部地域”哪些商区不显示，
						 * 其实就是为了剔除多余的“其他”和夸地域的商区)
						 * 但是控件
						 * SelectionListView里面是根据selectTag判断item是否为选中状态，所以这里
						 * ，要将所有的selectTag置为false
						 */
						if (item.isSelectTag()) {
							item.setSelectTag(false);
						}
						RfTypeDTO allSubDistrict = new RfTypeDTO();
						allSubDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
						allSubDistrict.setName("全部" + item.getName());
						allSubDistrict.setParentId(item.getUuid());
						item.getList().add(0, allSubDistrict);
						for (RfTypeDTO commonTypeDTO : item.getList()) {
//							if (commonTypeDTO.isSelectTag()) {
//								commonTypeDTO.setSelectTag(false);
							if(!String.valueOf(Settings.STATUTE_ALL).endsWith(commonTypeDTO.getUuid())){
								commonTypeDTO.setParentId(item.getUuid());
								allRegion.getList().add(commonTypeDTO);
							}
						}
					}
					RfTypeDTO allDistrict = new RfTypeDTO();
					allDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allDistrict.setName("-- 全部菜系 --");
					allDistrict.setParentId(allRegion.getUuid());
					allRegion.getList().add(0, allDistrict);
					listData.add(0, allRegion);

					// 控制UI显隐
					slv.setData(listData);
					slv.setVisibility(View.VISIBLE);
				}

			};

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
			}

			@Override
			protected void defineCacheKeyAndTime(CommonTask.TaskListener.CacheKeyAndTime keyAndTime) {
				CityInfo city=SessionManager.getInstance().getCityInfo(DistrictChoosingActivity.this);
				keyAndTime.cacheKey = KEY_REGION_MENU_LIST+"|"+city.getId();
				keyAndTime.cacheTimeMinute = LIST_CACHE_TIME; // 缓存时间一天，即1*24*60分钟
			}

		});
	}
	void loadSubwayData() {//getSubwayLineStationList
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		ServiceRequest request = new ServiceRequest(API.getSubwayLineStationList);
		// RfTypeListDTO 中 u n: 地域的uuid,name list :地域里包含的商区列表
		// RfTypeDTO 中 u n c : 商区的uuid,name,首字母
		CommonTask.request(request, new CommonTask.TaskListener<RfTypeListPackDTO>() {
			protected void onSuccess(RfTypeListPackDTO dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				if (dto == null) {
					finish();
					return;
				}
				List<RfTypeListDTO> listData = dto.list;
				if (listData != null) {
					// 构造全部商区
					RfTypeListDTO allRegion = new RfTypeListDTO();
					allRegion.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allRegion.setName("全部");
					allRegion.setSelectTag(false);
					allRegion.setIsNeedGroupBy(true);
					for (RfTypeListDTO item : dto.list) {
						/**
						 * 这个逻辑注意下:
						 * 服务端默认selectTag为true;(通过这个字段告诉客户端“全部地域”哪些商区不显示，
						 * 其实就是为了剔除多余的“其他”和夸地域的商区)
						 * 但是控件
						 * SelectionListView里面是根据selectTag判断item是否为选中状态，所以这里
						 * ，要将所有的selectTag置为false
						 */
						if (item.isSelectTag()) {
							item.setSelectTag(false);
						}
						//默认选中前一页传过来的
						if(subwayUuid!=null &&  subwayUuid.equals(item.getUuid())){
							item.setSelectTag(true);
						}
						RfTypeDTO allSubDistrict = new RfTypeDTO();
						allSubDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
						allSubDistrict.setName("全部" + item.getName());
						allSubDistrict.setParentId(item.getUuid());
						item.getList().add(0, allSubDistrict);
						for (RfTypeDTO commonTypeDTO : item.getList()) {
//							if (commonTypeDTO.isSelectTag()) {
//								commonTypeDTO.setSelectTag(false);
							if(!String.valueOf(Settings.STATUTE_ALL).endsWith(commonTypeDTO.getUuid())){
								commonTypeDTO.setParentId(item.getUuid());
								allRegion.getList().add(commonTypeDTO);
							}
						}
					}
					RfTypeDTO allDistrict = new RfTypeDTO();
					allDistrict.setUuid(String.valueOf(Settings.STATUTE_ALL));
					allDistrict.setName("-- 全部地铁站 --");
					allDistrict.setParentId(allRegion.getUuid());
					allRegion.getList().add(0, allDistrict);

					listData.add(0, allRegion);

					// 控制UI显隐
					slv.setData(listData);
					slv.setVisibility(View.VISIBLE);
				}

			};

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
			}

			@Override
			protected void defineCacheKeyAndTime(CommonTask.TaskListener.CacheKeyAndTime keyAndTime) {
				CityInfo city=SessionManager.getInstance().getCityInfo(DistrictChoosingActivity.this);
				keyAndTime.cacheKey = KEY_REGION_SUBWAY_LIST+"|"+city.getId();
				keyAndTime.cacheTimeMinute = LIST_CACHE_TIME; // 缓存时间一天，即1*24*60分钟
			}

		});
	}
}
