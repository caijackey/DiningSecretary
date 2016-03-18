package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.location.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import com.baidu.mapapi.*;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.map.TextItem;
import com.baidu.mapapi.map.TextOverlay;
import com.baidu.mapapi.map.Symbol.Color;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.fg114.main.R;
import com.fg114.main.app.*;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.location.LocBaidu;
import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.task.PostErrorReportTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

public class MyMapActivity extends MainFrameActivity {

	private int fromPage;
	private Fg114Application app;

	private MapView mMapView;
	private MyLocationOverlay mLocationOverlay; // 定位图层
	private boolean mIsFirstLoad = true;
	private boolean isRunning = true;
	private int mMode = Settings.Baidu_Empty;

	private LinearLayout llBg;

	// private ResInfo2Data restaurantInfo;

	private GeoPoint mGpChoose;

	private PostErrorReportTask postErrorReportTask;

	private ErrorReportTypeData error;
	private GeoPoint resLoc = null;

	// 餐厅信息
	private String mRestId = "";
	private String mRestName = "";
	private double mRestLongitude;
	private double mRestLatitude;
	private int mTypeTag = 1; // 报错类型

	// 初始化地图上的标记资源
	private Drawable marker; // 得到需要标在地图上的资源
	private Drawable marker1; // 得到需要标在地图上的资源

	// 覆盖物容器
	Overlay items;
	OverlayItem oldItem;
	OverlayItem newItem;
	OverlayItem restItem;
	// 文字容器
	TextOverlay textItems;
	TextItem oldText = new TextItem();
	TextItem newText = new TextItem();
	TextItem restText = new TextItem();
	// 颜色
	Symbol.Color red;
	Symbol.Color white;
	Symbol.Color blue;
	{
		red = new Symbol().new Color();
		red.red = 255;
		red.green = 0;
		red.blue = 0;
		red.alpha = 150;
		//
		white = new Symbol().new Color();
		white.red = 255;
		white.green = 255;
		white.blue = 255;
		white.alpha = 255;
		//
		blue = new Symbol().new Color();
		blue.red = 0;
		blue.green = 0;
		blue.blue = 255;
		blue.alpha = 150;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		getMainLayout().addView(View.inflate(this, R.layout.my_map, null));

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle.containsKey(Settings.BUNDLE_KEY_ID)) {
			mRestId = bundle.getString(Settings.BUNDLE_KEY_ID);
		}
		if (bundle.containsKey(Settings.BUNDLE_REST_NAME)) {
			mRestName = bundle.getString(Settings.BUNDLE_REST_NAME);
		}
		if (bundle.containsKey(Settings.BUNDLE_REST_LONGITUDE)) {
			mRestLongitude = bundle.getDouble(Settings.BUNDLE_REST_LONGITUDE);
		}
		if (bundle.containsKey(Settings.BUNDLE_REST_LATITUDE)) {
			mRestLatitude = bundle.getDouble(Settings.BUNDLE_REST_LATITUDE);
		}
		if (bundle.containsKey("typeTag")) {
			mTypeTag = bundle.getInt("typeTag");
		}

		mMode = bundle.getInt(Settings.BUNDLE_BAIDU_MODE, Settings.Baidu_Empty);

		error = (ErrorReportTypeData) bundle.getSerializable("ErrorReportTypeData");
		if (error == null) {
			// 如果错误类型数据未设置，本页面则成为“信息反馈”页面，需要调用信息反馈的Task
			DialogUtil.showToast(this, "对不起，暂时无法提交此报错信息！");
			this.finish();
			return;
		}

		initComponent();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		// 如果不是地图报错，自动移动到当前位置
		if (mMode != Settings.Baidu_Choose_Loc) {
			mMapView.postDelayed(locRun, 1000);
		}
		super.onResume();
	}

	Runnable locRun = new Runnable() {

		@Override
		public void run() {
			if (!isRunning) {
				return;
			}
			Location loc = LocBaidu.loc;
			// 5分钟之内有效
			if (mIsFirstLoad && loc != null && System.currentTimeMillis() - loc.getTime() < 5 * 60 * 1000) {
				GeoPoint pt = new GeoPoint((int) (loc.getLatitude() * 1e6), (int) (loc.getLongitude() * 1e6));
				mMapView.getController().animateTo(pt);
				mIsFirstLoad = false;
			} else {
				mMapView.postDelayed(this, 1000);
			}
		}
	};

	@Override
	protected void onPause() {
		mMapView.onPause();
		isRunning = false;
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
			app = (Fg114Application) getApplication();
			if (app.mBMapMan != null && mMapView != null) {
				mMapView.regMapViewListener(app.mBMapMan, null);
			}
		}
		super.onDestroy();
	}

	private void initComponent() {

		llBg = (LinearLayout) findViewById(R.id.my_map_llBg);
		getTvTitle().setText(R.string.text_title_map);
		getBtnGoBack().setText(R.string.text_button_back);
		getBtnGoBack().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 初始化MapView
		mMapView = (MapView) findViewById(R.id.my_map_mvMain);
		mMapView.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.getController().setZoom(15);// 默认地图放大比例

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		marker = getResources().getDrawable(R.drawable.mapbar_dest); // 得到需要标在地图上的资源
		marker1 = getResources().getDrawable(R.drawable.mapbar_org); // 得到需要标在地图上的资源
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight()); // 为maker定义位置和边界
		marker1.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight()); // 为maker定义位置和边界

		// 标记
		items = new Overlay(marker, mMapView);
		oldItem = new OverlayItem(new GeoPoint(0, 0), "原位置", "原位置");
		newItem = new OverlayItem(new GeoPoint(0, 0), "新位置", "新位置");

		oldItem.setMarker(marker);
		newItem.setMarker(marker1);

		items.addItem(oldItem);
		items.addItem(newItem);
		// 文字
		textItems = new TextOverlay(mMapView);
		oldText.pt = new GeoPoint(0, 0);
		oldText.align = TextItem.ALIGN_CENTER;
		oldText.fontColor = white;
		oldText.bgColor = red;
		oldText.fontSize = UnitUtil.dip2px(12);
		oldText.text = "原位置";

		newText.pt = new GeoPoint(0, 0);
		newText.align = TextItem.ALIGN_BOTTOM;
		newText.fontColor = white;
		newText.bgColor = blue;
		newText.fontSize = UnitUtil.dip2px(12);
		newText.text = "新位置";

		textItems.addText(oldText);
		textItems.addText(newText);
		// ----------------
		switch (mMode) {
		case Settings.Baidu_Empty:
			break;
		case Settings.Baidu_Choose_Loc:
			getTvTitle().setText(R.string.text_title_report_loc);
			getBtnOption().setText(android.R.string.ok);
			getBtnOption().setVisibility(View.VISIBLE);

			getBtnOption().setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (mGpChoose == null) {
						DialogUtil.showToast(MyMapActivity.this, "请选择您提交的新位置");
						return;
					}
					executePostErrorReportTask();
				}
			});

			llBg.setVisibility(View.VISIBLE);
			if (mRestLatitude == 0 || mRestLongitude == 0) {
				resLoc = null;
			} else {
				resLoc = new GeoPoint((int) (mRestLatitude * 1E6), (int) (mRestLongitude * 1E6));
				resLoc = CoordinateConvert.fromGcjToBaidu(resLoc);
			}

			mMapView.getOverlays().clear();
			mMapView.getOverlays().add(mLocationOverlay);
			mMapView.getOverlays().clear();
			mMapView.getOverlays().add(mLocationOverlay);
			mMapView.getOverlays().add(items);
			mMapView.getOverlays().add(textItems);
			drawOldPosition();

			if (((Fg114Application) getApplication()).mBMapMan != null) {
				mMapView.regMapViewListener(((Fg114Application) getApplication()).mBMapMan, new MKMapViewListener() {

					@Override
					public void onMapMoveFinish() {
						drawNewPosition();
					}

					@Override
					public void onClickMapPoi(MapPoi mappoi) {
					}

					@Override
					public void onGetCurrentMap(Bitmap bitmap) {
					}

					@Override
					public void onMapAnimationFinish() {
						drawNewPosition();
					}
				});
			}

			break;
		case Settings.Baidu_Show_Res:
			drawRestPosition();
			break;
		case Settings.Baidu_Show_Route:
			break;
		default:
			break;
		}

	}

	public void drawRestPosition() {
		resLoc = new GeoPoint((int) (mRestLatitude * 1E6), (int) (mRestLongitude * 1E6));
		resLoc = CoordinateConvert.fromGcjToBaidu(resLoc);
		if (resLoc != null) {
			restItem = new OverlayItem(new GeoPoint(0, 0), mRestName, mRestName);
			restItem.setGeoPoint(resLoc);

			restText.pt = resLoc;
			restText.align = TextItem.ALIGN_BOTTOM;
			restText.fontColor = white;
			restText.bgColor = blue;
			restText.fontSize = UnitUtil.dip2px(14);
			restText.text = mRestName;
			//
			items.removeAll();
			textItems.removeAll();
			//
			items.addItem(restItem);
			textItems.addText(restText);
			mMapView.refresh();
			mMapView.getController().animateTo(resLoc);
		}

	}

	public void drawOldPosition() {
		try {
			if (resLoc != null) {
				oldItem.setGeoPoint(resLoc);
				items.updateItem(oldItem);
				oldText.pt = resLoc;
				textItems.removeText(oldText);
				textItems.addText(oldText);
				mMapView.getController().setCenter(resLoc);
				mMapView.refresh();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawNewPosition() {
		try {
			GeoPoint center = mMapView.getMapCenter();
			mGpChoose = baiduToGoogle(center);
			getBtnOption().setClickable(true);
			// ----------
			newItem.setGeoPoint(center);
			newText.pt = center;
			textItems.removeText(newText);
			textItems.addText(newText);
			items.updateItem(newItem);
			mMapView.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Overlay extends ItemizedOverlay<OverlayItem> {

		public Overlay(Drawable marker, MapView context) {
			super(marker, context);
		}

	}

	/**
	 * 提交错误
	 */
	private void executePostErrorReportTask() {
		String name = "";
		if (SessionManager.getInstance().isUserLogin(this)) {
			name = SessionManager.getInstance().getUserInfo(this).getNickName();
		}
		String errorInfo = mRestName + "的餐厅位置有误，新位置：" + mGpChoose.getLatitudeE6() * 1E-6 + "," + mGpChoose.getLongitudeE6() * 1E-6;
		// Log.e("errorInfo", errorInfo);
		postErrorReportTask = new PostErrorReportTask(mTypeTag, error.getFuncTag(), error.getTypeId(), error.getTypeName(), getString(R.string.text_info_loading), this, name, "", mRestId, errorInfo,
				new Runnable() {

					@Override
					public void run() {
						MyMapActivity.this.finish();
					}
				});

		postErrorReportTask.execute();
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
}
