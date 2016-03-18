package com.fg114.main.app.activity.resandfood;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅信息界面
 * 
 * @author zhangyifan
 * 
 */
public class RestaurantInfoActivity extends MainFrameActivity {

	// 传入参数
	private String restaurantId; // 餐厅ID

	// 本地缓存数据
	private RestInfoData restaurantInfo;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	private TextView tvStyle;
	private TextView tvAddress;
	private TextView tvTraffic;
	private TextView tvConsumeWay;
	private MyImageView ivTrafficMap;
	private TextView tvName;
	private TextView tvOpenTime;
	private TextView res_info_detail;
	private TextView mParkMap;
//	private LinearLayout mLayout;
	private LinearLayout mapLayout;
	private Button location_map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅基本信息", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);

		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		this.bundleData.putString(Settings.BUNDLE_KEY_ID, restaurantId);

		// 获得缓存的城市列表信息
		restaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);

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
		// 设置画面
		setView();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅基本信息", "");
		// ----------------------------
	}

	@Override
	public void finish() {
		recycle();
		super.finish();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("辅助信息");
		// this.getTvTitle().setText(R.string.text_title_restaurant_info);
		this.getBtnGoBack().setText(R.string.text_title_restaurant_detail);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_info, null);
		tvStyle = (TextView) contextView.findViewById(R.id.res_info_tvFoodStyle);
		tvAddress = (TextView) contextView.findViewById(R.id.res_info_tvAddress);
		tvTraffic = (TextView) contextView.findViewById(R.id.res_info_tvTraffic);
		tvConsumeWay = (TextView) contextView.findViewById(R.id.res_info_tvConsumeWay);
		ivTrafficMap = (MyImageView) contextView.findViewById(R.id.res_info_ivParkMap);
		tvName = (TextView) contextView.findViewById(R.id.res_info_tvResName);
		tvOpenTime = (TextView) contextView.findViewById(R.id.res_info_tvResOpenTime);
		mParkMap = (TextView) contextView.findViewById(R.id.res_info_parkMap);
		mapLayout = (LinearLayout) contextView.findViewById(R.id.res_info_ivParkMapLayout);
		res_info_detail = (TextView) contextView.findViewById(R.id.res_info_detail);
		location_map=(Button) contextView.findViewById(R.id.location_map);

		// this.setBtnCallGone();
		// 位置地图
		if (restaurantInfo.latitude > 0 && restaurantInfo.longitude > 0) {
//			GeoPoint gcj = new GeoPoint((int) (restaurantInfo.latitude * 1E6), (int) (restaurantInfo.longitude * 1E6));
//			GeoPoint baidu = CoordinateConvert.fromGcjToBaidu(gcj);
//			String lola = baidu.getLongitudeE6() / 1E6 + "," + baidu.getLatitudeE6() / 1E6;
//			String url = "http://api.map.baidu.com/staticimage?width=500&height=120&center=" + lola + "&markers=" + lola + "&zoom=17&markerStyles=s,A,0xff0000";
			location_map.setVisibility(View.VISIBLE);
			location_map.setOnClickListener(new OnClickListener() {
				// 地图查看
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("地图按钮");
					// -----

					if (!RestaurantDetailActivity.showMap(restaurantInfo, RestaurantInfoActivity.this)) {
						DialogUtil.showToast(RestaurantInfoActivity.this, "无法打开地图模式");
					}
				}
			});
		} else {
			location_map.setVisibility(View.GONE);

		}
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void setView() {

		if (restaurantInfo != null) {
			if (CheckUtil.isEmpty(restaurantInfo.menuTypeInfo)) {
				tvStyle.setText("无");
			} else {
				tvStyle.setText(restaurantInfo.menuTypeInfo);
			}

			if (CheckUtil.isEmpty(restaurantInfo.address)) {
				tvAddress.setText("无");
			} else {
				tvAddress.setText(restaurantInfo.address);
			}

			if (CheckUtil.isEmpty(restaurantInfo.detail)) {
				res_info_detail.setText("无");
			} else {
				res_info_detail.setText(restaurantInfo.detail);
			}
			String trafficInfo = "";
			if (!CheckUtil.isEmpty(restaurantInfo.trafficLine)) {
				trafficInfo = restaurantInfo.trafficLine;
			}
			if (!CheckUtil.isEmpty(restaurantInfo.busInfo)) {
				if (!CheckUtil.isEmpty(trafficInfo)) {
					trafficInfo = trafficInfo + "\t\n" + restaurantInfo.busInfo;
				} else {
					trafficInfo = restaurantInfo.busInfo;
				}
			}
			// trafficInfo = trafficInfo.replace("、", ", ");

			trafficInfo = trafficInfo.replaceAll("、", "、 ");
			trafficInfo = trafficInfo.replaceAll("：", "： ");

			if (CheckUtil.isEmpty(trafficInfo)) {
				tvTraffic.setText("无");
			} else {
				tvTraffic.setText(trafficInfo);
			}

			if (CheckUtil.isEmpty(restaurantInfo.consumeType)) {
				tvConsumeWay.setText("无");
			} else {
				tvConsumeWay.setText(restaurantInfo.consumeType);
			}

			if (CheckUtil.isEmpty(restaurantInfo.parkingPicUrl)) {
				mParkMap.setVisibility(View.GONE);
				ivTrafficMap.setVisibility(View.GONE);
				mapLayout.setVisibility(View.GONE);
			} else {
				ivTrafficMap.setImageByUrl(restaurantInfo.parkingPicUrl, false, 0, ScaleType.FIT_XY);
				mParkMap.setVisibility(View.VISIBLE);
				ivTrafficMap.setVisibility(View.VISIBLE);
				mapLayout.setVisibility(View.VISIBLE);
			}

			if (CheckUtil.isEmpty(restaurantInfo.name)) {
				tvName.setText("无");
			} else {
				tvName.setText(restaurantInfo.name);
			}

			if (CheckUtil.isEmpty(restaurantInfo.openTimeInfo)) {
				tvOpenTime.setText("无");
			} else {
				tvOpenTime.setText(restaurantInfo.openTimeInfo);
			}
			// ivTrafficMap.setTag(restaurantInfo.getParkingPicUrl());
			// ImageUtil.getInstance(this).displayImage(restaurantInfo.getParkingPicUrl(),
			// ivTrafficMap, false);
		}
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		ivTrafficMap.recycle(false);
		System.gc();
	}
	

}
