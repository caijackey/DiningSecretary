package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.platform.comapi.map.Projection;
import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayRestaurantMapActivity;
import com.fg114.main.service.dto.TakeoutListData;
import com.fg114.main.service.dto.TakeoutListData2;
import com.fg114.main.service.dto.TakeoutRestListData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ViewUtils;

public class MyPointOverLay extends ItemizedOverlay<OverlayItem> {

	private static final boolean DEBUG = true;
	private List<TakeoutListData2> mTakeoutList;
	private TextView resInfoTv;
	private List<OverlayItem> mOverlayList = new ArrayList<OverlayItem>(); // 路径上的Overlay
	private Drawable mMarker;
	private Drawable mMarkerSelected;
	private View mPopupView;
	private MapView mMapView;
	private Drawable tempDrawable;
	private int selectedPos = 0;
	private String resId;
	private List<GeoPoint> geoList = new ArrayList<GeoPoint>(); // 保存当前一次所有OverlayItem的点集合

	public int getSelectedPos() {
		return selectedPos;
	}

	public void setSelectedPos(int selectedPos) {
		this.selectedPos = selectedPos;
	}

	public MyPointOverLay(final Context ctx, Drawable marker, Drawable markerSelected, View popupView, MapView mapView) {
		super(marker, mapView);
		mMapView = mapView;
		mMarker = marker;
		mMarkerSelected = markerSelected;
		mPopupView = popupView;
		resInfoTv = (TextView) this.mPopupView.findViewById(R.id.show_takeaway_res_tv);
		mPopupView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
//				Bundle bundle = new Bundle();
//				bundle.putString(Settings.BUNDLE_REST_ID, resId);
//				ActivityUtil.jump(ctx, TakeAwayRestaurantMenuActivity.class, 0, bundle);
//				
				// 去点菜页
				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, resId);
				bundle.putInt(Settings.FROM_TAG, 1);
				ActivityUtil.jump(ctx, TakeAwayNewFoodListActivity.class, 0, bundle);
				
			}
		});

	}

	public void setList(List<TakeoutListData2> takeoutList) {
		removeAll();
		mTakeoutList = takeoutList;
		mOverlayList.clear();
		geoList.clear();
		selectedPos = 0;
		if (mTakeoutList != null && mTakeoutList.size() != 0) {
			for (TakeoutListData2 data : mTakeoutList) {
				initGeoPointItemLayout(data);
			}
//			if (mTakeoutList.get(0).isHaveCallTag()) {
//				tempDrawable = mMarkerSelected;
//			} else {
//				tempDrawable = mMarker;
//			}
			tempDrawable = mMarker;
			// 初始时显示最近的一个搜索结果
			mMapView.updateViewLayout(mPopupView, new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, geoList.get(0), 0, -tempDrawable.getIntrinsicHeight() + 3,
					MapView.LayoutParams.BOTTOM_CENTER));
			mPopupView.setVisibility(View.VISIBLE);
			resInfoTv = (TextView) this.mPopupView.findViewById(R.id.show_takeaway_res_tv);
			resInfoTv.setText(mTakeoutList.get(0).name);
			resId = mTakeoutList.get(0).uuid;
			addItem(mOverlayList);
			// drawItems();
		} else {
			mPopupView.setVisibility(View.INVISIBLE);
		}

	}

	public void initGeoPointItemLayout(TakeoutListData2 data) {
		if (data.latitude != 0 && data.longitude != 0) {
			GeoPoint tempPoint = new GeoPoint((int) (data.latitude * 1E6), (int) (data.longitude * 1E6));
			OverlayItem oi = new OverlayItem(tempPoint, "", "");
			// if (data.isHaveCallTag()) {
			// oi.setMarker(mMarkerSelected);
			// } else {
			// oi.setMarker(mMarker);
			// }
			oi.setMarker(mMarker);

			mOverlayList.add(oi);
			geoList.add(tempPoint);
		}

	}

	// //画出所有的mark
	// public void draw(Canvas canvas, MapView mapView) {
	// if (mMapView == null) {
	// mMapView = mapView;
	// }
	// // Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
	// Projection projection = mapView.getProjection();
	// for (int index = 0; index < size(); index++) {
	// OverlayItem overlayItem = getItem(index); // 得到给定索引的item
	// Point point = projection.toPixels(overlayItem.getPoint(), null); //
	// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
	//
	// if (mTakeoutList.get(index).isHaveCallTag()) {
	// //boundCenterBottom(mMarkerSelected);
	// } else {
	// //boundCenterBottom(mMarker);
	// }
	//
	// // 可在此处添加您的绘制代码
	// Paint paintText = new Paint();
	// paintText.setColor(Color.BLUE);
	// paintText.setTextSize(18);
	// canvas.drawText(overlayItem.getTitle(), point.x - 30, point.y,
	// paintText); // 绘制文本
	//
	// }
	// }
	// //画出所有的mark
	// public void drawItems() {
	// // Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
	// Projection projection = mMapView.getProjection();
	// Log.d("drawItems.size",""+size());
	// for (int index = 0; index < size(); index++) {
	// OverlayItem overlayItem = getItem(index); // 得到给定索引的item
	// //this.updateItem(overlayItem);
	// // if (mTakeoutList.get(index).isHaveCallTag()) {
	// // boundCenterBottom(mMarkerSelected);
	// // } else {
	// // boundCenterBottom(mMarker);
	// // }
	// }
	// }

	@Override
	protected boolean onTap(int i) {
		if (mMarker == null || mMarkerSelected == null) {
			return true;
		}
		resInfoTv.setText(mTakeoutList.get(i).name);
		resId = mTakeoutList.get(i).uuid;
		GeoPoint pt = mOverlayList.get(i).getPoint();
		// if (mTakeoutList.get(i).isHaveCallTag()) {
		// tempDrawable = mMarkerSelected;
		// } else {
		// tempDrawable = mMarker;
		// }
		tempDrawable = mMarker;
		// 单击时显示餐厅详情
		mMapView.updateViewLayout(mPopupView, new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pt, 0, -tempDrawable.getIntrinsicHeight() + 3,
				MapView.LayoutParams.BOTTOM_CENTER));
		return true;
	}

}
