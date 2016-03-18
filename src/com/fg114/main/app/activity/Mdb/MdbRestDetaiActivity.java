package com.fg114.main.app.activity.Mdb;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.activity.resandfood.DishListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.app.adapter.AdvertisementImgAdapter;
import com.fg114.main.app.adapter.MdbRestDetailPicAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.EllipsizeText;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.service.dto.CommonPicData;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MdbRestInfoData;
import com.fg114.main.service.dto.MdbRestListDTO;
import com.fg114.main.service.dto.MdbRestListData;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestTelInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

public class MdbRestDetaiActivity extends MainFrameActivity {
	private String uuid;
	// 缓存数据
	private MdbRestInfoData mdbRestInfoData = null;
	private static final String[] MAP_TYPE = { "百度", "高德", "Browser" };

	private List<int[]> startEndPoi = new ArrayList<int[]>();// 特色菜 左右 灰色括号
																// 对应下标数组列表

	private List<CommonPicData> picList;
	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private Button shareFriends;

	private Button mdb_predetermine;

	// 免单公告
	private RelativeLayout mdb_detail_hint_layout;
	private TextView mdb_detail_res_hint_txt;

	// 消费提示
	private LinearLayout mdb_restaurant_discount_layout;
	private LinearLayout mdb_restaurant_discount_list;
	private TextView mdb_detail_restaurant_hint_detail;
	private TextView mdb_detail_restaurant_hint_title;

	// 特色菜
	private RelativeLayout mdb_detail_res_tscLayout;
	private TextView mdb_detail_res_special_food_num;
	private EllipsizeText mdb_detail_res_tvFoodList;
	private LinearLayout mdb_detail_res_dish_order;
	private View mdb_detail_res_tvFoodList_layout;

	// 餐厅基本信息
	private MyImageView mdb_rest_location_map;
	private LinearLayout mdb_detail_res_tvAddress_layout;
	private TextView mdb_detail_res_tvAddress;
	private TextView mdb_detail_res_tvTel;
	// 餐厅介绍
	private LinearLayout mdb_restaurant_introduction;
	private TextView mdb_detail_restaurant_no_introduction;

	// 广告
	private View mdb_res_detail_adv_layout;
	private CircleFlowIndicator advimgCircleIndicator;
	private ViewFlow advViewFlowimg;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(Settings.UUID)) {
				uuid = bundle.getString(Settings.UUID);
			}
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

		// 获得查询结果
		executeGetMdbRstDetailTask();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// resetTask();

		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return mdbRestInfoData.shareInfo == null ? "" : mdbRestInfoData.shareInfo.shareWeiboUuid;
	}

	@Override
	protected String getRestaurantId() {
		return mdbRestInfoData.uuid;
	}

	@Override
	protected String getRestaurantName() {
		return mdbRestInfoData.restName;

	}

	private synchronized void tryDisplayAdvertisement() {
		// List<MainPageAdvData> advList =
		// SessionManager.getInstance().getMainPageAdvDataList();
		// 如果有广告则需要显示广告
		if (picList != null && picList.size() > 0) {
			mdb_res_detail_adv_layout.setVisibility(View.VISIBLE);
			if (picList.size() == 1) {
				advimgCircleIndicator.setVisibility(View.GONE);
			} else {
				advimgCircleIndicator.setVisibility(View.VISIBLE);
			}

			// 确保只有一个运行的线程
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			// ---------------------
			advViewFlowimg.setAdapter(new MdbRestDetailPicAdapter(this, picList));
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

		// 设置标题栏
		this.getTitleLayout().setVisibility(View.VISIBLE);
		this.getTvTitle().setText(R.string.text_title_restaurant_detail);
		// this.getBtnGoBack().setText(leftGoBackBtn);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnTitle().setVisibility(View.GONE);
		shareFriends = new Button(this);
		shareFriends.setBackgroundResource(R.drawable.res_share_friends);
		shareFriends.setWidth(UnitUtil.dip2px(25));
		shareFriends.setHeight(UnitUtil.dip2px(25));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.setMargins(shareFriends.getLeft() - UnitUtil.dip2px(50), shareFriends.getTop(), shareFriends.getRight() + UnitUtil.dip2px(50), shareFriends.getBottom());
		shareFriends.setLayoutParams(lp);
		this.getTitleLayout().addView(shareFriends);

		this.getBtnOption().setBackgroundResource(R.drawable.share2friend);

		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.setMargins(this.getBtnOption().getLeft() - UnitUtil.dip2px(-40), this.getBtnOption().getTop(), this.getBtnOption().getRight() + UnitUtil.dip2px(-40), this.getBtnOption().getBottom());
		this.getBtnOption().setLayoutParams(lps);
		this.getBtnOption().setWidth(UnitUtil.dip2px(25));
		this.getBtnOption().setHeight(UnitUtil.dip2px(25));

		this.getBottomLayout().setVisibility(View.GONE);

		// TODO 收藏
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!MdbRestDetaiActivity.this.getBtnOption().isSelected()) {

					MdbRestDetaiActivity.this.getBtnOption().setSelected(false);
					ServiceRequest request = new ServiceRequest(API.addMdbRestToFav);
					request.addData("restId", uuid);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "收藏中...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
							MdbRestDetaiActivity.this.getBtnOption().setSelected(true);
							DialogUtil.showToast(MdbRestDetaiActivity.this, "收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

						// private void doTest_confirm() {
						// String json =
						// "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
						// SimpleData data = JsonUtils.fromJson(json,
						// SimpleData.class);
						// onSuccess(data);
						//
						// }
					});

				} else {
					MdbRestDetaiActivity.this.getBtnOption().setSelected(true);
					ServiceRequest request = new ServiceRequest(API.delMdbRestFromFav);
					request.addData("restId", uuid);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "取消收藏...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
							MdbRestDetaiActivity.this.getBtnOption().setSelected(false);
							DialogUtil.showToast(MdbRestDetaiActivity.this, "取消收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

						// private void doTest_cancel() {
						// String json =
						// "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"取消收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
						// SimpleData data = JsonUtils.fromJson(json,
						// SimpleData.class);
						// onSuccess(data);
						//
						// }
					});

				}

			}
		});
		shareFriends.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("分享面板-分享");
				// -----

				showShareDialog(100);
			}
		});

		// 初始化
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.mdb_rest_detail_activity, null);

		mdb_res_detail_adv_layout = contextView.findViewById(R.id.mdb_res_detail_adv_layout);
		advViewFlowimg = (ViewFlow) contextView.findViewById(R.id.viewflow_img);
		advimgCircleIndicator = (CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator_img);
		advViewFlowimg.setFlowIndicator(advimgCircleIndicator);

		// 预定
		mdb_predetermine = (Button) contextView.findViewById(R.id.mdb_predetermine);

		mdb_detail_hint_layout = (RelativeLayout) contextView.findViewById(R.id.mdb_detail_hint_layout);
		mdb_detail_res_hint_txt = (TextView) contextView.findViewById(R.id.mdb_detail_res_hint_txt);
		// 提示
		mdb_restaurant_discount_layout = (LinearLayout) contextView.findViewById(R.id.mdb_restaurant_discount_layout);
		mdb_restaurant_discount_list = (LinearLayout) contextView.findViewById(R.id.mdb_restaurant_discount_list);
		mdb_detail_restaurant_hint_detail = (TextView) contextView.findViewById(R.id.mdb_detail_restaurant_hint_detail);
		mdb_detail_restaurant_hint_title = (TextView) contextView.findViewById(R.id.mdb_detail_restaurant_hint_title);

		mdb_detail_res_special_food_num = (TextView) contextView.findViewById(R.id.mdb_detail_res_special_food_num);
		mdb_detail_res_tvFoodList = (EllipsizeText) contextView.findViewById(R.id.mdb_detail_res_tvFoodList);
		mdb_detail_res_dish_order = (LinearLayout) contextView.findViewById(R.id.mdb_detail_res_dish_order);
		mdb_detail_res_tscLayout = (RelativeLayout) contextView.findViewById(R.id.mdb_detail_res_tscLayout);
		mdb_detail_res_tvFoodList_layout= contextView.findViewById(R.id.mdb_detail_res_tvFoodList_layout);

		// 餐厅基本信息
		mdb_rest_location_map = (MyImageView) contextView.findViewById(R.id.mdb_rest_location_map);
		mdb_detail_res_tvAddress_layout = (LinearLayout) contextView.findViewById(R.id.mdb_detail_res_tvAddress_layout);
		mdb_detail_res_tvAddress = (TextView) contextView.findViewById(R.id.mdb_detail_res_tvAddress);
		mdb_detail_res_tvTel = (TextView) contextView.findViewById(R.id.mdb_detail_res_tvTel);

		// 餐厅介绍
		mdb_restaurant_introduction = (LinearLayout) contextView.findViewById(R.id.mdb_restaurant_introduction);
		mdb_detail_restaurant_no_introduction = (TextView) contextView.findViewById(R.id.mdb_detail_restaurant_no_introduction);

		advViewFlowimg.setAdapter(new MdbRestDetailPicAdapter(this, new ArrayList<CommonPicData>()));

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	private void executeGetMdbRstDetailTask() {
		ServiceRequest request = new ServiceRequest(API.getMdbRestInfo);
		request.addData("uuid", uuid);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<MdbRestInfoData>() {

			@Override
			protected void onSuccess(MdbRestInfoData dto) {

				mdbRestInfoData = dto;

				if (dto != null) {

					// 显示广告
					if (dto.picList != null && dto.picList.size() != 0) {
						mdb_res_detail_adv_layout.setVisibility(View.VISIBLE);
						picList = dto.picList;
						tryDisplayAdvertisement();
					} else {
						mdb_res_detail_adv_layout.setVisibility(View.GONE);
					}

					setView(dto);

				}
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				finish();
			}
		});
	}

	private void setView(final MdbRestInfoData dto) {

		if (dto == null) {
			return;
		}
		if (dto.favTag) {
			MdbRestDetaiActivity.this.getBtnOption().setSelected(true);
		} else {
			MdbRestDetaiActivity.this.getBtnOption().setSelected(false);
		}
		
		
		this.getTvTitle().setText(dto.restName);

		if (!CheckUtil.isEmpty(dto.orderBtnName)) {
			mdb_predetermine.setText(dto.orderBtnName);
		} else {
			mdb_predetermine.setText("预 定");
		}

		mdb_detail_res_tvFoodList_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, dto.uuid);
				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_FOOD);
				ActivityUtil.jump(MdbRestDetaiActivity.this, RestaurantPicActivity.class, 0, bundle);
			}
		});
		mdb_predetermine.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID, dto.uuid);
				bundle.putString(Settings.BUNDLE_REST_NAME, dto.restName);
				ActivityUtil.jump(MdbRestDetaiActivity.this, MyBookRestaurantActivity.class, 0, bundle);
			}
		});

		if (CheckUtil.isEmpty(dto.freeNotice)) {
			mdb_detail_hint_layout.setVisibility(View.GONE);
		} else {
			mdb_detail_res_hint_txt.setText(dto.freeNotice);
			mdb_detail_hint_layout.setVisibility(View.VISIBLE);
		}
		mdb_detail_hint_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!CheckUtil.isEmpty(dto.freeNoticeWapUrl)) {
					ActivityUtil.jumpToWebNoParam(dto.freeNoticeWapUrl, "", true, null);
				}
			}
		});

		// 消费提示
		if (CheckUtil.isEmpty(dto.hintTitle)) {
			mdb_restaurant_discount_layout.setVisibility(View.GONE);
		} else {
			mdb_restaurant_discount_layout.setVisibility(View.VISIBLE);
			mdb_detail_restaurant_hint_detail.setText(dto.hintDetail);
			mdb_detail_restaurant_hint_title.setText(dto.hintTitle);
		}
		// 预点菜
		if (dto.totalSpecialFoodNum == 0 || dto.specialFoodList == null || dto.specialFoodList.size() == 0) {
			mdb_detail_res_tscLayout.setVisibility(View.GONE);
		} else {
			mdb_detail_res_tscLayout.setVisibility(View.VISIBLE);
			// 特色菜
			mdb_detail_res_special_food_num.setText("(" + dto.totalSpecialFoodNum + ")");
			// 拼接关键字
			String resFood = initBeforeEllipsize();
			mdb_detail_res_tvFoodList.setStartEndPoi(startEndPoi);
			mdb_detail_res_tvFoodList.setMaxLines(3);
			mdb_detail_res_tvFoodList.setText(resFood);

			mdb_detail_res_dish_order.setOnClickListener(new OnClickListener() {
				// 去餐厅美食页面
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bundle = new Bundle();
					bundle.putString(Settings.UUID, dto.uuid);
					bundle.putInt(Settings.FROM_TAG, 1);
					ActivityUtil.jump(MdbRestDetaiActivity.this, DishListActivity.class, 0, bundle);

				}
			});
		}

		// 位置地图
		if (dto.bdLat > 0 && dto.bdLon > 0) {
			// GeoPoint gcj = new GeoPoint((int) (restaurantInfo.latitude *
			// 1E6), (int) (restaurantInfo.longitude * 1E6));
			// GeoPoint baidu = CoordinateConvert.fromGcjToBaidu(gcj);
			String lola = dto.bdLon + "," + dto.bdLat;
			String url = "http://api.map.baidu.com/staticimage?width=500&height=120&center=" + lola + "&markers=" + lola + "&zoom=17&markerStyles=s,A,0xff0000";
			mdb_rest_location_map.setVisibility(View.VISIBLE);
			mdb_rest_location_map.setImageByUrl(url, true, 0, ScaleType.FIT_XY);
			mdb_rest_location_map.setOnClickListener(new OnClickListener() {
				// 地图查看
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (!showMap(dto, MdbRestDetaiActivity.this)) {
						DialogUtil.showToast(MdbRestDetaiActivity.this, "无法打开地图模式");
					}
				}
			});
		} else {
			mdb_rest_location_map.setVisibility(View.GONE);
		}

		if (dto.address != null) {
			mdb_detail_res_tvAddress.setText(dto.address);
		}

		// 地址切换地图
		mdb_detail_res_tvAddress_layout.setOnClickListener(new OnClickListener() {
			// 去餐厅介绍页面
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-地图");
				// -----

				if (!showMap(dto, MdbRestDetaiActivity.this)) {
					DialogUtil.showToast(MdbRestDetaiActivity.this, "无法打开地图模式");
				}

			}
		});

		// ---
		mdb_detail_res_tvTel.setText("");
		if (dto.telList != null && dto.telList.size() > 0) {//

			final List<RestTelInfo> tels = dto.telList;
			// tvTel.setText(restaurantInfo.getTel());

			for (int i = 0; i < tels.size(); i++) {
				final RestTelInfo tempTelInfo = tels.get(i);
				if (CheckUtil.isPhone(tempTelInfo.tel)) {
					final int ii = i;
					if (tempTelInfo.isTelCanCall) {
						ViewUtils.appendSpanToTextView(mdb_detail_res_tvTel, tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch),
								new ClickableSpan() {

									@Override
									public void onClick(View widget) {

										// -----
										OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-电话按钮", tempTelInfo.tel);
										// -----

										//
										ActivityUtil.callSuper57(MdbRestDetaiActivity.this, tempTelInfo.cityPrefix + tempTelInfo.tel);
										//
										final String userPhone;
										// 确定拨打的用户手机号
										if (SessionManager.getInstance().isUserLogin(MdbRestDetaiActivity.this)) {
											userPhone = SessionManager.getInstance().getUserInfo(MdbRestDetaiActivity.this).getTel();
										} else {
											userPhone = SharedprefUtil.get(MdbRestDetaiActivity.this, Settings.ANONYMOUS_TEL, "");
										}

										// 向后台传拨打数据
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													ServiceRequest.callTel(2, uuid, "(" + tempTelInfo.cityPrefix + ")" + tempTelInfo.tel
															+ (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch));
												} catch (Exception e) {
													e.printStackTrace();
												} finally {
													// DialogUtil.showToast(activity,
													// "Finish call api");
													// callPostOverListener.sendEmptyMessage(HANDLE_POST_OVER);
												}
											}
										}).start();

									}

								});
					} else {
						// 只显示电话和分机号
						mdb_detail_res_tvTel.append(tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch));
					}
					// ViewUtils.setURL(tvTel, position, position+end,
					// "tel:"+tels[i],true);
				}
				// 加逗号
				if (i < tels.size() - 1) {
					mdb_detail_res_tvTel.append("　");
				}
			}

		} else {

			mdb_detail_res_tvTel.setVisibility(View.GONE);

		}

		// 餐厅介绍
		if (CheckUtil.isEmpty(dto.restDetail)) {
			mdb_restaurant_introduction.setVisibility(View.GONE);
		} else {
			mdb_restaurant_introduction.setVisibility(View.VISIBLE);
			mdb_detail_restaurant_no_introduction.setText(dto.restDetail);
		}

	}

	// 在此拼接价格
	public String initBeforeEllipsize() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbRestInfoData.specialFoodList.size(); i++) {
			int[] tempInteger = new int[2];
			sb.append(mdbRestInfoData.specialFoodList.get(i).getName());

			String price = mdbRestInfoData.specialFoodList.get(i).getPrice();

			if (price.equals("不详")) {
				// sb.append("(");
				// tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append("");
				// sb.append(price);
				// sb.append(")");
				// tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			} else if (price.equals("时价")) {
				sb.append("(");
				tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append(price);
				sb.append(")");
				tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			} else {
				sb.append("(");
				tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append("￥ ");
				sb.append(price);
				sb.append(")");
				tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			}
			if (tempInteger[0] != 0 && tempInteger[1] != 0) {
				startEndPoi.add(tempInteger);
			}

			if (i < mdbRestInfoData.specialFoodList.size() - 1) {
				sb.append(",");
			}

		}
		return sb.toString();

	}

	/**
	 * 显示地图模式
	 * 
	 * @return
	 */
	public static boolean showMap(MdbRestInfoData restInfo, Activity activity) {
		// Bundle bundle = new Bundle();
		// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
		// Settings.RESTAURANT_DETAIL_ACTIVITY);
		// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantInfo.getId());
		// ActivityUtil.jump(this, MyMapActivity.class,
		// Settings.RESTAURANT_DETAIL_ACTIVITY, bundle);
		// return true;

		for (String type : MAP_TYPE) {
			if (RestaurantDetailActivity.launchMap(type, restInfo.restName, restInfo.latitude, restInfo.longitude, activity)) {
				return true;
			}
		}
		return false;
	}
}
