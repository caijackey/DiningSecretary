package com.fg114.main.app.activity.takeaway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONObject;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;

import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SelectPOIActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.MyMapActivity;

import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.MainMenuListInfo;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocBaidu;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyPointOverLay;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.MainMenuData;
import com.fg114.main.service.dto.TakeoutList2DTO;
import com.fg114.main.service.dto.TakeoutListDTO;
import com.fg114.main.service.dto.TakeoutListData;
import com.fg114.main.service.dto.TakeoutListData2;
import com.fg114.main.service.dto.TakeoutRestListDTO;
import com.fg114.main.service.dto.TakeoutRestListData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.BaseTask.Callback;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.google.android.maps.MapActivity;

public class TakeAwayRestaurantMapActivity extends MainFrameActivity {
	// 缓存数据
	private TakeAwayFilter filter; // 查询条件

	private View contextView;
	private MapView mMapView;
	private Drawable marker, selectedMarker;
	private View mPopView;
	private ImageButton mLocBtn;
	private List<TakeoutListData2> mTakeoutRestList;
	private List<CommonTypeDTO> mFirstList = new ArrayList<CommonTypeDTO>();
	private List<CommonTypeDTO> mSecondList = new ArrayList<CommonTypeDTO>();

	private LayoutInflater mInflater;
	private MyLocationOverlay mLocationOverlay; // 当前位子定位图层
	private MyPointOverLay myOverlay;

	// 拍照上传保存路径
	private long timstamp;
	private boolean mAnimateToMyLoc = true;

	public static int MAX_DISTANCE = 200;
	public static int MAX_ZOOM = 18;
	public static int MIN_ZOOM = 16;
	private int lastLevel = 18;
	private GeoPoint mCenterPoint;
	private String gpsRectBaidu = "";
	// 当前页的状态
	private CommonTypeDTO mRadioDto;
	private boolean isUserCheck = true;
	private boolean isTaskSafe = true;
	private Fg114Application app;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖选择地理位置", "");
		// ----------------------------
		
		app = Fg114Application.getInstance();
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
		filter = com.fg114.main.util.SessionManager.getInstance().getTakeAwayFilter();
		Bundle bundle = this.getIntent().getExtras();
		initComponent();
		// 逻辑：当从列表页过来的时候，只带中心位置的条件，其他条件不带过来
		filter.setTypeId("");
		filter.setSendLimitId("");
		filter.setKeywords("");

		// 起动过程移动到当前位置，下面这里屏蔽自动定位逻辑
		// 新版逻辑是：一进来就定位在查询条件里的poi位置，然后搜索
		mMapView.postDelayed(locateSelectedPosition, 1000);

	}

	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖选择地理位置", "");
		// ----------------------------
		super.onRestart();
	}


	@Override
	protected void onResume() {
		

		mMapView.onResume();
		super.onResume();
		updateTitle();
	}

	// 定位到当前选择的位置
	Runnable locateSelectedPosition = new Runnable() {

		@Override
		public void run() {
			int gpsType = filter.getGpsTypeTag();
			GeoPoint baidu = new GeoPoint((int) (filter.getLatitude() * 1e6), (int) (filter.getLongitude() * 1e6));
			if (gpsType == 1) {// 原生转百度
				baidu = getBaiduGpsFromWgs(filter.getLatitude(), filter.getLongitude());
			} else if (gpsType == 3) {// google转百度
				baidu = getBaiduGpsFromGcj(filter.getLatitude(), filter.getLongitude());
			}
			mMapView.getController().setCenter(baidu);
			mCenterPoint = baidu;
			excuteGetTakeoutListTask();
		}
	};

	private GeoPoint getBaiduGpsFromWgs(double lat, double lon) {

		GeoPoint pt = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
		pt = CoordinateConvert.fromWgs84ToBaidu(pt);
		return pt;
	}

	private GeoPoint getBaiduGpsFromGcj(double lat, double lon) {

		GeoPoint pt = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
		pt = CoordinateConvert.fromGcjToBaidu(pt);
		return pt;
	}

	// int retryTimes = 4;
	// Runnable firstLocate = new Runnable() {
	//
	// @Override
	// public void run() {
	// if (!isRunning) {
	// return;
	// }
	// Location loc = LocBaidu.loc;
	// // 5分钟之内有效
	// if (mIsFirstLoad && loc != null && System.currentTimeMillis() -
	// loc.getTime() < 5 * 60 * 1000) {
	// GeoPoint pt = new GeoPoint((int) (loc.getLatitude() * 1e6), (int)
	// (loc.getLongitude() * 1e6));
	// pt = CoordinateConvert.fromGcjToBaidu(pt);
	// mMapView.getController().setCenter(pt);
	// mIsFirstLoad = false;
	// mCenterPoint = pt;
	// excuteGetTakeoutListTask();
	//
	// } else {
	// if (--retryTimes >= 0) {
	// mMapView.postDelayed(this, 1000);
	// } else {
	// DialogUtil.showToast(getApplicationContext(), "暂时无法获取您的位置!");
	// }
	// }
	// }
	// };

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		// 在后台
		if (!ActivityUtil.isOnForeground(getApplicationContext())) {
			mMapView.destroy();
			if (app.mBMapMan != null && mMapView != null) {
				mMapView.regMapViewListener(app.mBMapMan, null);
			}
			finish();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// 在前台
		if (ActivityUtil.isOnForeground(getApplicationContext())) {
			mMapView.destroy();
			if (app.mBMapMan != null && mMapView != null) {
				mMapView.regMapViewListener(app.mBMapMan, null);
			}
		}
		super.onDestroy();
	}

	class LocateToCurrentGpsPosition implements Runnable {
		private long temp;

		public LocateToCurrentGpsPosition(long mTemp) {
			this.temp = mTemp;
		}

		@Override
		public void run() {
			if (timstamp == temp) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {

							LocationData point = mLocationOverlay.getMyLocation();
							// point =
							// CoordinateConvert.fromWgs84ToBaidu(point);
							mMapView.getController().setCenter(new GeoPoint((int) (point.latitude * 1E6), (int) (point.longitude * 1E6)));
							excuteGetTakeoutListTask();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

			}

		}
	};

	private ViewGroup dropdownAnchor;
	private Button btFirst;
	private Button btSecond;

	private void initComponent() {
		contextView = View.inflate(this, R.layout.show_takeaway_restaurant_map, null);
		// 顶部导航菜单
		dropdownAnchor = (ViewGroup) contextView.findViewById(R.id.top_condition_layout);
		btFirst = (Button) contextView.findViewById(R.id.button_first);
		btSecond = (Button) contextView.findViewById(R.id.button_second);

		// 地图内容部分
		mMapView = (MapView) contextView.findViewById(R.id.show_takeaway_res_map);
		// mMapView.setDrawOverlayWhenZooming(true);
		mLocBtn = (ImageButton) contextView.findViewById(R.id.show_takeaway_locBtn);
		mMapView.getController().setZoom(18);

		mMapView.getController().setCenter(new GeoPoint((int) (31.23357323463603 * 1E6), (int) (121.47999286651611 * 1E6)));
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
		mLocBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// timstamp = SystemClock.currentThreadTimeMillis();
				// new Thread(new LocateToCurrentGpsPosition(timstamp)).start();
				// -----
				OpenPageDataTracer.getInstance().addEvent("定位到所选位置按钮");
				// -----
				mMapView.postDelayed(locateSelectedPosition, 0);

			}
		});

		if (((Fg114Application) getApplication()).mBMapMan != null) {
			mMapView.regMapViewListener(((Fg114Application) getApplication()).mBMapMan, new MKMapViewListener() {
				@Override
				public void onMapMoveFinish() {
					refreshPoi();
				}

				@Override
				public void onClickMapPoi(MapPoi arg0) {
				}

				@Override
				public void onGetCurrentMap(Bitmap arg0) {
				}

				@Override
				public void onMapAnimationFinish() {
					refreshPoi();
				}
			});
		}

		// // 注册定位事件
		// mLocationListener = new LocationListener()
		// {
		// @Override
		// public void onLocationChanged(final Location location)
		// {
		//
		// if (location != null) {
		// if (mAnimateToMyLoc) {
		// loc = location;
		// GeoPoint pt = new GeoPoint((int) (loc.getLatitude() * 1E6), (int)
		// (loc.getLongitude() * 1E6));
		// pt = CoordinateConvert.fromWgs84ToBaidu(pt);
		// mMapView.getController().setCenter(pt);
		// mCenterPoint = pt;
		// getGpsRect();
		// excuteGetCanMoveTakeawayRestListTask();
		// mAnimateToMyLoc = false;
		//
		// }
		// }
		// }
		//
		// @Override
		// public void onProviderDisabled(String provider) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onProviderEnabled(String provider) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onStatusChanged(String provider, int status, Bundle
		// extras) {
		//
		// }
		// };

		marker = getResources().getDrawable(R.drawable.mapbar_org); // 得到需要标在地图上的资源
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight()); // 为maker定义位置和边界
		selectedMarker = getResources().getDrawable(R.drawable.mapbar_dest);
		selectedMarker.setBounds(0, 0, selectedMarker.getIntrinsicWidth(), selectedMarker.getIntrinsicHeight()); // 为maker定义位置和边界
		// 弹出气泡显示 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopView = mInflater.inflate(R.layout.takeaway_res_popwind, null);
		mMapView.addView(mPopView, new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);

		// 添加坐标信息到地图

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(mMapView);
		mMapView.getOverlays().add(mLocationOverlay);
		setMyLocationOverlayData();
		myOverlay = new MyPointOverLay(this, marker, selectedMarker, mPopView, mMapView);
		mMapView.getOverlays().add(myOverlay);
		mMapView.refresh();

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

	}

	//
	private void setMyLocationOverlayData() {
		BDLocation bdlocation = LocBaidu.currentLocation;
		if (bdlocation == null) {
			return;
		}
		LocationData ld = new LocationData();
		GeoPoint pt = CoordinateConvert.fromGcjToBaidu(new GeoPoint((int) (bdlocation.getLatitude() * 1E6), (int) (bdlocation.getLongitude() * 1E6)));
		ld.latitude = pt.getLatitudeE6() / 1E6;
		ld.longitude = pt.getLongitudeE6() / 1E6;
		mLocationOverlay.setData(ld);

	}

	private void refreshPoi() {
		try {
			if (mCenterPoint != null) {
				GeoPoint mTempCenterPoint = mMapView.getMapCenter();
				int tempdistance = (int) GeoUtils.getDistance(mTempCenterPoint.getLatitudeE6() / 1e6, mTempCenterPoint.getLongitudeE6() / 1e6, mCenterPoint.getLatitudeE6() / 1e6,
						mCenterPoint.getLongitudeE6() / 1e6);
				int tempLevel = (int) mMapView.getZoomLevel(); // 当前缩放后的临时地图缩放度
				if (tempLevel < lastLevel) {
					if (tempLevel < MIN_ZOOM) {
						//
						tempLevel = MIN_ZOOM;
						mMapView.getController().setZoom(tempLevel);
						mCenterPoint = mTempCenterPoint;
						mMapView.postDelayed(new Runnable() {
							@Override
							public void run() {
								excuteGetTakeoutListTask();
							}
						}, 1000);
					} else {
						mCenterPoint = mTempCenterPoint;
						mMapView.postDelayed(new Runnable() {

							@Override
							public void run() {
								excuteGetTakeoutListTask();
							}
						}, 1000);// 延迟1秒获得矩形框，防止地图缩放没有完成
						lastLevel = tempLevel;
					}

				} else if (tempLevel == lastLevel) {
					if (tempdistance > MAX_DISTANCE) {
						// -----
						OpenPageDataTracer.getInstance().addEvent("移动地图");
						// -----
						mCenterPoint = mTempCenterPoint;
						excuteGetTakeoutListTask();
					}
				} else {
					lastLevel = tempLevel;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void excuteGetTakeoutListTask() {
		if (isTaskSafe) {
			// 线程安全的场合

			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}
		// 保护按钮不被点击
		setButtonState(false);
		// ----------
		getGpsRectBaidu();
		// ----------
		int gpsType = filter.getGpsTypeTag();
		GeoPoint baidu = new GeoPoint((int) (filter.getLatitude() * 1e6), (int) (filter.getLongitude() * 1e6));

		if (gpsType == 1) {// 原生转百度
			baidu = getBaiduGpsFromWgs(filter.getLatitude(), filter.getLongitude());
		} else if (gpsType == 3) {// google转百度
			baidu = getBaiduGpsFromGcj(filter.getLatitude(), filter.getLongitude());
		}
		// ----
		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getTakeoutListForMap2);
		// 获得外卖餐厅列表 地图，返回TakeoutListDTO，注意返回值中的：转换后的中国经纬度 chineseLon chineseLat
		request.addData("gpsTypeTag", 2);// 经纬度类别 1:原生 2：百度 3：中国（google）
		request.addData("gpsRect", gpsRectBaidu);// gps矩形 规则 左下点+右上点 。例子:
													// 121.495743,31.252139;121.542435,31.217499
		request.addData("longitude", baidu.getLongitudeE6() / 1E6); // 经度
		request.addData("latitude", baidu.getLatitudeE6() / 1E6); // 纬度
		request.addData("typeId", filter.getTypeId());// 外卖餐厅类别id 默认为空
		request.addData("sendLimitId", filter.getSendLimitId());// 起送类别id 默认为空
		request.addData("pageSize", 0);// 页面大小 没用
		request.addData("startIndex", 0);// 当前页 没用

		// ----------
		CommonTask.request(request, "加载数据，请稍候...", new CommonTask.TaskListener<TakeoutList2DTO>() {

			@Override
			protected void onSuccess(TakeoutList2DTO dto) {
				if (dto != null && dto.list != null) {
					if (dto.list.size() == 0) {
						DialogUtil.showToastShort(TakeAwayRestaurantMapActivity.this, "当前区域下没有外卖信息，请继续拖动地图查找！");
					}
					updateFilter(dto);
					mTakeoutRestList = dto.list;
					for (int i = 0; i < mTakeoutRestList.size(); i++) {
						GeoPoint geo = new GeoPoint((int) (mTakeoutRestList.get(i).latitude * 1E6), (int) (mTakeoutRestList.get(i).longitude * 1E6));
						geo = CoordinateConvert.fromGcjToBaidu(geo);
						mTakeoutRestList.get(i).latitude = geo.getLatitudeE6() / 1E6;
						mTakeoutRestList.get(i).longitude = geo.getLongitudeE6() / 1E6;
					}
					showMap();
				}

				isTaskSafe = true;
				setButtonState(true);
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				isTaskSafe = true;
				setButtonState(true);
			}

			@Override
			protected void onRefresh() {
				mTakeoutRestList = new ArrayList<TakeoutListData2>();
				excuteGetTakeoutListTask();
			}

			// void doTest() {
			// String json =
			// "{\"list\":[{\"uuid\":\"123\",\"name\":\"天府大萝卜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521264\",\"latitude\":\"31.239977\"},{\"uuid\":\"123\",\"name\":\"天下大黄瓜\",\"openTime\":\"1313\",\"sendLimit\":\"34243\",\"phone\":\"13241234\",\"distanceMeter\":\"10\",\"haveCallTag\":\"false\",\"longitude\":\"121.521564\",\"latitude\":\"31.239477\"}],\"typeList\":[]}";
			// TakeoutListDTO dto = JsonUtils.fromJson(json,
			// TakeoutListDTO.class);
			// onSuccess(dto);
			// }

		});

	}

	/**
	 * 设置筛选按钮的状态
	 * 
	 * @param state
	 */
	private void setButtonState(boolean state) {
		btFirst.setClickable(state);
		btSecond.setClickable(state);
		getBtnTitle().setClickable(state);
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
		getBtnGoBack().setText("返回");
		getTvTitle().setPadding(getTvTitle().getPaddingLeft(), getTvTitle().getPaddingTop(), UnitUtil.dip2px(15), getTvTitle().getPaddingBottom());

		// 调整图片大小
		Drawable pic = this.getResources().getDrawable(R.drawable.icon_search_list);
		pic.setBounds(0, 0, UnitUtil.dip2px(28), UnitUtil.dip2px(28));
		getBtnOption().setCompoundDrawables(null, pic, null, null);
		getBtnOption().setPadding(0, 0, 0, 0);

		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) getBtnOption().getLayoutParams();
		lp.width = UnitUtil.dip2px(30);
		lp.height = UnitUtil.dip2px(30);
		getBtnOption().setLayoutParams(lp);
		// --
		// 去地图
		getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("地图列表切换按钮");
				// -----
				finish();
			}
		});
		getTvTitle().setText(filter.getPoiName());
		// // 位置信息（不是选择的位置，则使用当前位置）
		// if (!filter.isSelectedPoi()) {
		// String address = getCurrentAddress();
		// if (MainFrameActivity.locatingFailed.equals(address)) {
		// getTvTitle().setText(MainFrameActivity.locating);
		// // 多等2.5秒，重试一次
		// getTvTitle().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// getTvTitle().setText(getCurrentAddress());
		// }
		// }, 2500);
		// } else {
		// getTvTitle().setText(address);
		// }
		// }
		// ----
		getTvTitleLayout().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择地址按钮");
				// -----
				// 点击去选择位置
				ActivityUtil.jump(TakeAwayRestaurantMapActivity.this, SelectPOIActivity.class, 1, new Bundle());
			}
		});
	}

	// 显示地图
	public void showMap() {
		try {
			myOverlay.setList(mTakeoutRestList);
			mMapView.refresh();
		} catch (Exception e) {
			DialogUtil.showToast(this, "刷新地图没有成功，请重试！");
			e.printStackTrace();
		}

	}

	/**
	 * 获得当前地图矩阵信息，百度坐标
	 * 
	 * @param newKeyword
	 */
	public void getGpsRectBaidu() {

		try {
			GeoPoint leftPoint = mMapView.getProjection().fromPixels(mMapView.getLeft(), mMapView.getBottom());
			// leftPoint = baiduToGoogle(leftPoint);
			GeoPoint rightPoint = mMapView.getProjection().fromPixels(mMapView.getRight(), mMapView.getTop());
			// rightPoint = baiduToGoogle(rightPoint);
			gpsRectBaidu = Double.toString(leftPoint.getLongitudeE6() / 1E6) + "," + Double.toString(leftPoint.getLatitudeE6() / 1E6) + ";" + Double.toString(rightPoint.getLongitudeE6() / 1E6) + ","
					+ Double.toString(rightPoint.getLatitudeE6() / 1E6);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 假定Google坐标为pointA，百度坐标为pointB，根据pointB求pointA
	 * 
	 * @param pointB
	 * @return
	 */
	private GeoPoint baiduToGoogle(GeoPoint pointB) {

		try {
			/*
			 * A---A` \ \ \ \ B---B` \ \ C
			 */
			GeoPoint pointC = CoordinateConvert.fromGcjToBaidu(pointB);
			// 计算C到B的偏移
			int offet_C_to_B_Latitude = pointC.getLatitudeE6() - pointB.getLatitudeE6();
			int offet_C_to_B_Longitude = pointC.getLongitudeE6() - pointB.getLongitudeE6();
			// 假定C到B偏移和B到A的偏移相等，推算出"假A"
			GeoPoint pointA_Assume = new GeoPoint(pointB.getLatitudeE6() - offet_C_to_B_Latitude, pointB.getLongitudeE6() - offet_C_to_B_Longitude);
			// 根据"假A"算出"假B"
			GeoPoint pointB_Assume = CoordinateConvert.fromGcjToBaidu(pointA_Assume);
			// 计算"假B"到B的偏移
			int offet_B_Assume_to_B_Latitude = pointB_Assume.getLatitudeE6() - pointB.getLatitudeE6();
			int offet_B_Assume_to_B_Longitude = pointB_Assume.getLongitudeE6() - pointB.getLongitudeE6();
			// 假定"假B"到B的偏移和"假A"到A的偏移相等
			GeoPoint pointA = new GeoPoint(pointA_Assume.getLatitudeE6() - offet_B_Assume_to_B_Latitude, pointA_Assume.getLongitudeE6() - offet_B_Assume_to_B_Longitude);

			// Log.e("GeoPoint", pointA.getLatitudeE6() * 1E-6 + "," +
			// pointA.getLongitudeE6() * 1E-6);

			return pointA;
		} catch (Exception e) {
			return pointB;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 是选择位置过来的
		if (resultCode == 999) {
			getTvTitle().setText(filter.getPoiName());
			mMapView.postDelayed(locateSelectedPosition, 0);
		}
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
		setButtonState(true);
	}

	/**
	 * 更新第一个过滤器，附近时为"距离条件"，搜索餐厅时为"区域条件"
	 * 
	 * @param dto
	 */
	private void updateFirstFilter(TakeoutList2DTO dto) {
		if (dto == null || dto.typeList == null || dto.typeList.size() == 0) {
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
				// 获得查询结果
				excuteGetTakeoutListTask();
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

				// 获得查询结果
				excuteGetTakeoutListTask();
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
				btSecond.setSelected(false);
			}
		});
	}

	@Override
	public void finish() {
		setResult(999);
		super.finish();
	}

}
