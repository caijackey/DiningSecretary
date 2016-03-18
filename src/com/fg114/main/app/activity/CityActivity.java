package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.adapter.CityAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.CityListInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CityListDTO;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.util.DialogUtil.DialogEventListener;
import com.fg114.main.util.SessionManager;

/**
 * 城市选择
 * 
 * @author zhangyifan
 * 
 */
public class CityActivity extends MainFrameActivity {

	private static final String TAG = "CityActivity";
	private static final String ZERO = "0";
	private int fromPage; // 返回页面

	// 本地缓存数据
	private CityListInfo cityListInfo;
	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private LinearLayout gpsLayout;
	private Button btnGpsCity;
	private ListView lvCities;
	private CityAdapter cityAdapter;
	private EditText mSearchCity;
	private LinearLayout llMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("热门城市", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// 初始化界面
		initComponent();
		getCityList();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("热门城市", "");
		// ----------------------------
	}

	@Override
	public void finish() {
		super.finish();
		ActivityUtil.overridePendingTransition(this, 0, R.anim.frame_anim_to_bottom);
		CityInfo cityInfo = SessionManager.getInstance().getCityInfo(this);
		if (cityInfo == null || CheckUtil.isEmpty(cityInfo.getId())) {
			CityInfo defaultCityInfo = new CityInfo();
			defaultCityInfo.setId(Settings.DEFAULT_CITY_ID);
			defaultCityInfo.setName(Settings.DEFAULT_CITY_NAME);
			defaultCityInfo.setPhone(Settings.DEFAULT_CITY_PHONE_SH);
			SessionManager.getInstance().setCityInfo(this, defaultCityInfo);
			Fg114Application.isNeedUpdate = true; // 为了广告在首页能及时更新，每城市广告数据是不一样的
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(getString(R.string.text_title_city_select));
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("更多城市");
		this.getBtnGoBack().setText(R.string.text_button_goto_index);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.city_select_list, null);
		gpsLayout = (LinearLayout) contextView.findViewById(R.id.city_select_list_gpsLayout);
		btnGpsCity = (Button) contextView.findViewById(R.id.city_select_list_btnGps);
		llMsg = (LinearLayout) contextView.findViewById(R.id.city_select_list_msgLayout);
		btnGpsCity.setText("正在定位您所在的城市...");
		btnGpsCity.setEnabled(false);
		btnGpsCity.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 存入缓存
				ViewUtils.preventViewMultipleClick(v, 1000);
				CityInfo gpsCity = null;
				String id = cityListInfo.getGpsCityId();
				CityListDTO cityListDTO = SessionManager.getInstance().getCityListDTO(CityActivity.this);
				// 从列表中获得城市信息
				for (CityData city : cityListDTO.getList()) {
					if (city.getCityId().equals(id)) {

						// if (DEBUG) Log.d(TAG, "selected city:" +
						// info.getName());

						// gpsCity = info;
						gpsCity = new CityInfo();
						gpsCity.setId(city.getCityId());
						gpsCity.setName(city.getCityName());
						gpsCity.setPhone(city.getPhone());
						gpsCity.setTimestamp(System.currentTimeMillis());

						SessionManager.getInstance().setCityInfo(CityActivity.this, gpsCity);
						CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
						ActivityUtil.jump(CityActivity.this, IndexActivity.class, 0, new Bundle(), true);
						Fg114Application.isNeedUpdate = true; // 为了广告在首页能及时更新，每城市广告数据是不一样的
						break;
					}
				}
				// 本页面关闭
				finish();
			}
		});
		lvCities = (ListView) contextView.findViewById(R.id.city_select_list_listview);
		lvCities.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<CityInfo> list = ((CityAdapter) arg0.getAdapter()).getList();
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择行");
				// -----
				if (list != null) {
					CityInfo city = list.get(index);
					if (city != null) {
						if (DEBUG)
							Log.d(TAG, "selected city:" + city.getName());

						if (city.getId().equals(ZERO)) {
							// 点击更多事件
							Bundle bundle = new Bundle();
							ActivityUtil.jump(CityActivity.this, CityMoreActivity.class, 0, bundle);
						} else {
							CityInfo cityInfo = new CityInfo();
							cityInfo.setId(city.getId());
							cityInfo.setName(city.getName());
							cityInfo.setPhone(city.getPhone());

							// 存入缓存
							SessionManager.getInstance().setCityInfo(CityActivity.this, cityInfo);
							CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
							ActivityUtil.jump(CityActivity.this, IndexActivity.class, 0, new Bundle(), true);
							Fg114Application.isNeedUpdate = true; // 为了广告在首页能及时更新，每城市广告数据是不一样的
							// 本页面关闭
							finish();
						}

					}
				}
			}
		});
		// 更多热门城市按钮
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("更多城市按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(CityActivity.this, CityMoreActivity.class, 0, bundle);
			}
		});

		mSearchCity = (EditText) contextView.findViewById(R.id.city_select_list_etSearch);
		// 初始化搜索框
		mSearchCity.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				ActivityUtil.jump(CityActivity.this, CitySearchActivity.class, 0, bundle);
			}
		});

		setFunctionLayoutGone();
//		setTest();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

//	// --显示百度推送日志
//	private int hits = 0;
//
//	private void setTest() {
//		getTvTitle().setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				hits++;
//				if (hits > 10) {
//					DialogUtil.showDialog(CityActivity.this, R.layout.dialog_request_log, new DialogEventListener() {
//
//						@Override
//						public void onInit(View contentView, PopupWindow dialog) {
//							TextView text = (TextView) contentView.findViewById(R.id.log);
//							text.setText(KeepAliveService.bindBaiduPushLog.toString());
//
//						}
//					});
//					hits = 0;
//				}
//			}
//		});
//	}

	private void setAdapter() {
		try {
			cityAdapter = new CityAdapter(this);

			List<CityInfo> list = cityListInfo.getCityList();
			// 添加列表尾部
			CityInfo city = new CityInfo();
			city.setId(ZERO);
			city.setName("更多热门城市");
			list.add(city);
			cityAdapter.setList(list);
			lvCities.setAdapter(cityAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getCityList() {

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				try {
					if (msg.what == 0) {
						// 获得热门城市
						llMsg.setVisibility(View.GONE);
						lvCities.setVisibility(View.VISIBLE);
						setAdapter();
					} else if (msg.what == 1) {
						// 获得所定位的当前城市
						if (cityListInfo != null && cityListInfo.isGpsLocatedTag() && !TextUtils.isEmpty(cityListInfo.getGpsCityId()) && !TextUtils.isEmpty(cityListInfo.getGpsCityName())) {
							btnGpsCity.setText(cityListInfo.getGpsCityName());
							btnGpsCity.setEnabled(true);
						} else {
							btnGpsCity.setText(CityActivity.this.getString(R.string.text_dialog_gps_unlocaled));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		llMsg.setVisibility(View.VISIBLE);
		lvCities.setVisibility(View.GONE);

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// 获得缓存的城市列表信息
					cityListInfo = new CityListInfo();
					CityListDTO cityListDTO = SessionManager.getInstance().getHotCityListDTO(CityActivity.this);
					for (CityData city : cityListDTO.getList()) {
						CityInfo info = new CityInfo();
						info.setId(city.getCityId());
						info.setName(city.getCityName());
						info.setPhone(city.getPhone());
						info.setTimestamp(System.currentTimeMillis());
						cityListInfo.getCityList().add(info);
					}
					handler.sendEmptyMessage(0);

					// 定位所在城市
					if (Loc.isGpsAvailable()) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								int i=0;
								while (true) {
									double longitude = 0;
									double latitude = 0;
									LocInfo myLoc = Loc.getLoc();
									if (myLoc == null || myLoc.getLoc() == null) {
										handler.sendEmptyMessage(1);
									} else {
										longitude = myLoc.getLoc().getLongitude();
										latitude = myLoc.getLoc().getLatitude();
									}
									// 获取定位城市
									final CityInfo cityInfoByGPS = SessionManager.getInstance().getCity(CityActivity.this, longitude, latitude);
									if (cityInfoByGPS == null) {
										handler.sendEmptyMessage(1);
									} else {
										cityListInfo.setGpsLocatedTag(true);
										cityListInfo.setGpsCityId(cityInfoByGPS.getId());
										cityListInfo.setGpsCityName(cityInfoByGPS.getName());
										SessionManager.getInstance().setGpsCity(CityActivity.this, cityInfoByGPS);
										handler.sendEmptyMessage(1);
										break;
									}
									try {
										//5秒内没有获取定位城市,就不获取定位城市,防止陷入死循环			
										if(i>5){
											break;
										}
										i++;
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							
							}
						}).start();
					} else {
						handler.sendEmptyMessage(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
	}
}
