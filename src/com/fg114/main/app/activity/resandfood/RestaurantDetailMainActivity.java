package com.fg114.main.app.activity.resandfood;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Html.TagHandler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.mealcombo.GroupBuyDetailActivity;
import com.fg114.main.app.adapter.RestaurantDetailRecommendAdapter;
import com.fg114.main.service.dto.CouponPanelData;
import com.fg114.main.service.dto.PageRestInfo3DTO;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestRecomInfoData3;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅详情主页面 // 1:餐厅 2：推荐 3：榜单 推荐餐厅详情页面 是由2个页面组成 分别为 RestaurantDetailMainActivity
 * 和 RestaurantDetailRecommendActivity RestaurantDetailMainActivity中又分为2种页面
 * 普通餐厅详情 和 榜单详情 需要传入参数：餐厅ID 选择页面 再根据后台返回决定跳转页面 榜单id
 * 
 * @author dengxiangyu
 * 
 */
public class RestaurantDetailMainActivity extends MainFrameActivity {
	LocalActivityManager manager = null;
	public static ViewPager pager = null;
	// TextView t1, t2, t3;
	// 缓存数据
	private PageRestInfo3DTO pageRestInfoDTO = null;
	// private String pageRestInfoDTOJson = "";
	private ArrayList<View> list;
	private MyPagerAdapter mPagerAdapter;

	private String restaurantId = "";
	private String uuid = "";
	private int offset = 0;
	private int currIndex = 0;
	// private int bmpW;
	private ImageView cursor;
	private View contextView;
	private Button shareFriends;
	private LinearLayout group_buy;

	private int showTypeTag;
	private String restTypeId = "";// 榜单id
	private boolean isCloseGroupBuy = false;// 关闭团购
	private Thread timer;
	private TextView group_buying_detail;

	private long onPauseTime;// 记录退出时间
	private long onResumeTime;// 记录进入时间
	private boolean isOnResume;// 是否在前台
	private long onPauseSurplusTime;// 记录进入后台剩余时间

	private int shareTag;// 分享tag 0：推荐分享 1：餐厅分享

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获得传入参数
		isOnResume = true;

		Bundle bundle = this.getIntent().getExtras();
		uuid = bundle.getString(Settings.BUNDLE_REST_ID);
		if (bundle.containsKey(Settings.BUNDLE_showTypeTag)) {
			showTypeTag = bundle.getInt(Settings.BUNDLE_showTypeTag);
		}

		if (bundle.containsKey(Settings.BUNDLE_REST_TYPE_ID)) {
			restTypeId = bundle.getString(Settings.BUNDLE_REST_TYPE_ID);
		}
		manager = new LocalActivityManager(this, true);
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
			finish();
		} else {
			excuteRestRecomInfo();
		}

		// restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);

		manager.dispatchCreate(savedInstanceState);

		// 初始化界面
		initComponent();

	}

	@Override
	protected void onResume() {

		super.onResume();
		onResumeTime = System.currentTimeMillis();
		if (!isOnResume&&pageRestInfoDTO!=null) {
			isOnResume = true;
			if (pageRestInfoDTO.couponPanelData != null) {
				if (pageRestInfoDTO.couponPanelData.countDownTag) {
					long time = onPauseSurplusTime - (onResumeTime - onPauseTime) / 1000;
					if (time > 0) {
						setRemainderTime(time);
					} else {
						onPauseSurplusTime = 0;
						if (group_buying_detail != null) {
							group_buying_detail.setText(Html.fromHtml(getTimeStringFromSeconds(0)));
						}
					}
				}
			}
		}

		manager.dispatchResume();

		if (pager != null) {
			switch (pager.getCurrentItem()) {
			case 0:
				if (pageRestInfoDTO != null) {
					if (pageRestInfoDTO.showTypeTag == 2) {
						Activity activity1 = manager.getActivity("A");
						if (activity1 != null && activity1 instanceof RestaurantDetailRecommendActivity) {
							((RestaurantDetailRecommendActivity) activity1).invisibleOnScreen1();
						}
					} else {
						Activity activity = manager.getActivity("B");
						if (activity != null && activity instanceof RestaurantDetailActivity) {
							((RestaurantDetailActivity) activity).invisibleOnScreen();
						}
					}
				}
				break;
			case 1:
				Activity activity = manager.getActivity("B");
				if (activity != null && activity instanceof RestaurantDetailActivity) {
					((RestaurantDetailActivity) activity).invisibleOnScreen();
				}
				break;

			default:
				break;
			}
		}
		//
		// if (Settings.NEED_TAG_REST_COMMENT) {
		//
		// Settings.NEED_TAG_REST_COMMENT = false;
		//
		// }
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// Log.v("TAG", "rdm");
	// }
	//
	@Override
	protected void onPause() {
		super.onPause();
		onPauseTime = System.currentTimeMillis();
		isOnResume = false;
		// stopRemainderTime();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initComponent() {

		// this.getBtnGoBack().setText(leftGoBackBtn);
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getTvTitle().setVisibility(View.VISIBLE);

		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnTitle().setVisibility(View.GONE);

		shareFriends = new Button(this);
		shareFriends.setBackgroundResource(R.drawable.res_share_friends);
		shareFriends.setWidth(UnitUtil.dip2px(25));
		shareFriends.setHeight(UnitUtil.dip2px(25));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(shareFriends.getLeft() - UnitUtil.dip2px(50), shareFriends.getTop(), shareFriends.getRight() + UnitUtil.dip2px(50), shareFriends.getBottom());
		shareFriends.setLayoutParams(lp);
		this.getTitleLayout().addView(shareFriends);

		this.getBtnOption().setBackgroundResource(R.drawable.share2friend);

		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lps.setMargins(this.getBtnOption().getLeft() - UnitUtil.dip2px(-40), this.getBtnOption().getTop(), this.getBtnOption().getRight() + UnitUtil.dip2px(-40), this.getBtnOption().getBottom());
		this.getBtnOption().setLayoutParams(lps);
		this.getBtnOption().setWidth(UnitUtil.dip2px(25));
		this.getBtnOption().setHeight(UnitUtil.dip2px(25));

		this.getBottomLayout().setVisibility(View.GONE);

		// 内容部分
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_detail_main_act, null);

		cursor = (ImageView) contextView.findViewById(R.id.cursor);
		pager = (ViewPager) contextView.findViewById(R.id.viewpage);
		group_buy = (LinearLayout) contextView.findViewById(R.id.group_buy);

		// 分享
		shareFriends.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("分享面板-分享");
				// -----
				String[] items = { "推荐分享", "餐厅分享" };
				DialogUtil.showListDialog(RestaurantDetailMainActivity.this, "分享", items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialoginterface, int i) {
						shareTag = i;
						if (i == 0) {
							// 推荐分享
							showShareDialog(3);
						} else if (i == 1) {
							// 餐厅分享
							showShareDialog(1);
						}

					}

				});

				// showShareDialog(3);
				// takepic();
			}
		});

		//收藏
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!RestaurantDetailMainActivity.this.getBtnOption().isSelected()) {

					RestaurantDetailMainActivity.this.getBtnOption().setSelected(false);
					ServiceRequest request = new ServiceRequest(API.addRestToFav);
					request.addData("restId", restaurantId);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "收藏中...", new CommonTask.TaskListener<SimpleData>() {

						@Override
						protected void onSuccess(SimpleData dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
							RestaurantDetailMainActivity.this.getBtnOption().setSelected(true);
							DialogUtil.showToast(RestaurantDetailMainActivity.this, "收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

						private void doTest_confirm() {
							String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
							SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
							onSuccess(data);

						}
					});

				} else {
					RestaurantDetailMainActivity.this.getBtnOption().setSelected(true);
					ServiceRequest request = new ServiceRequest(API.delRestFromFav);
					request.addData("restId", restaurantId);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "取消收藏...", new CommonTask.TaskListener<SimpleData>() {

						@Override
						protected void onSuccess(SimpleData dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
							RestaurantDetailMainActivity.this.getBtnOption().setSelected(false);
							DialogUtil.showToast(RestaurantDetailMainActivity.this, "取消收藏成功");
						}

						@Override
						protected void onError(int code, String message) {
							super.onError(code, message);
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
						}

						private void doTest_cancel() {
							String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"取消收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
							SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
							onSuccess(data);

						}
					});

				}

			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		InitImageView();

	}

	private void initPagerViewer() {

		list = new ArrayList<View>();
		if (pageRestInfoDTO.showTypeTag == 2) {
			cursor.setVisibility(View.VISIBLE);
			this.getTvTitle().setText("推荐详情");
			this.getTvTitle().setPadding(100, 0, 0, 0);

			RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParam.setMargins(0, UnitUtil.dip2px(11), 0, 0);
			pager.setLayoutParams(layoutParam);

			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_REST_ID, uuid);
			// bundle.putString(Settings.BUNDLE_pageRestInfoDTO,
			// pageRestInfoDTOJson);
			bundle.putSerializable(Settings.BUNDLE_pageRestInfoDTO, pageRestInfoDTO);

			list.add(getView("A", RestaurantDetailMainActivity.this, RestaurantDetailRecommendActivity.class, bundle));
			Bundle bundle1 = new Bundle();
			bundle1.putString(Settings.BUNDLE_KEY_ID, restaurantId);
			// bundle1.putString(Settings.BUNDLE_pageRestInfoDTO,
			// pageRestInfoDTOJson);
			bundle1.putSerializable(Settings.BUNDLE_pageRestInfoDTO, pageRestInfoDTO);
			bundle1.putInt(Settings.BUNDLE_TPYE_TAG, pageRestInfoDTO.showTypeTag);

			// bundle1.putString(Settings.BUNDLE_KEY_ID, "C57K22L42380");
			// bundle1.putString(Settings.BUNDLE_REST_TYPE_ID, "28");
			// list.add(getView("B", RestaurantDetailMainActivity.this,
			// RestaurantDetailListActivity.class,bundle1));
			setOnShowUploadImageListener(new OnShowUploadImageListener() {
				public void onGetPic(Bundle bundle) {
					onFinishTakePic(Settings.UPLOAD_TYPE_RESTAURANT, restaurantId, pageRestInfoDTO.restInfo.name);
					Settings.isRestaurantRecommentDetail = false;
				}
			});
			list.add(getView("B", RestaurantDetailMainActivity.this, RestaurantDetailActivity.class, bundle1));

			// ---
			boolean isFirst = SharedprefUtil.getBoolean(this, Settings.IS_FRIST_REST_DETAIL, true);
			// 如果是第一次使用，打开向左滑提示
			if (isFirst) {
				// 改变第一次登录状态
				final View view = LayoutInflater.from(RestaurantDetailMainActivity.this).inflate(R.layout.rest_detail_popupwindow, null);
				this.getTitleLayout().postDelayed(new Runnable() {

					@Override
					public void run() {
						DialogUtil.showPopupWindow(RestaurantDetailMainActivity.this, null, view, true, new PopupWindow.OnDismissListener() {

							@Override
							public void onDismiss() {

							}
						});
					}
				}, 50);

				SharedprefUtil.saveBoolean(this, Settings.IS_FRIST_REST_DETAIL, false);
			}
			if (pageRestInfoDTO.showCouponPanelTag) {
				isCloseGroupBuy = false;
				group_buy.setVisibility(View.VISIBLE);
				addGroupBuy(pageRestInfoDTO.couponPanelData);

			} else {
				isCloseGroupBuy = true;
				group_buy.setVisibility(View.GONE);
			}
		} else {
			Bundle bundle1 = new Bundle();
			bundle1.putString(Settings.BUNDLE_KEY_ID, restaurantId);
			bundle1.putInt(Settings.BUNDLE_TPYE_TAG, pageRestInfoDTO.showTypeTag);
			bundle1.putString(Settings.BUNDLE_REST_TYPE_ID, restTypeId);
			bundle1.putSerializable(Settings.BUNDLE_pageRestInfoDTO, pageRestInfoDTO);
			// bundle1.putString(Settings.BUNDLE_pageRestInfoDTO,
			// pageRestInfoDTOJson);

			// bundle1.putString(Settings.BUNDLE_KEY_ID, "C57K22L42380");
			// bundle1.putString(Settings.BUNDLE_REST_TYPE_ID, "28");
			// list.add(getView("B", RestaurantDetailMainActivity.this,
			// RestaurantDetailListActivity.class,bundle1));
			setOnShowUploadImageListener(new OnShowUploadImageListener() {
				public void onGetPic(Bundle bundle) {
					onFinishTakePic(Settings.UPLOAD_TYPE_RESTAURANT, restaurantId, pageRestInfoDTO.restInfo.name);
					Settings.isRestaurantRecommentDetail = false;
				}
			});
			this.getTitleLayout().setVisibility(View.GONE);
			cursor.setVisibility(View.GONE);
			list.add(getView("B", RestaurantDetailMainActivity.this, RestaurantDetailActivity.class, bundle1));
		}
		mPagerAdapter = new MyPagerAdapter(list);
		pager.setAdapter(mPagerAdapter);

		if (pageRestInfoDTO.showTypeTag == 2) {
			pager.setCurrentItem(0);
		} else {
			pager.setCurrentItem(1);
		}
		pager.setOnPageChangeListener(new MyOnPageChangeListener());

	}

	private void InitImageView() {

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		offset = screenW / 8 + 15;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);
	}

	private View getView(String id, Context old, Class<?> cls, Bundle mBundle) {
		Intent intent = new Intent(old, cls);
		if (mBundle != null) {
			intent.putExtras(mBundle);
		}

		return manager.startActivity(id, intent).getDecorView();
	}

	public class MyPagerAdapter extends PagerAdapter {
		List<View> list = new ArrayList<View>();

		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		// int one = offset * 2 + bmpW;
		// int two = one * 2;

		@Override
		public void onPageSelected(int arg0) {

			// -----
			OpenPageDataTracer.getInstance().addEvent("左右滑动");
			// -----

			// Animation animation = null;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					// animation = new TranslateAnimation(offset, 0, 0, 0);
					// if (!isCloseGroupBuy) {
					// group_buy.setVisibility(View.VISIBLE);
					// }
					cursor.setBackgroundResource(R.drawable.point_tag2);
				}
				break;
			case 1:
				if (currIndex == 0) {
					// animation.setRepeatMode(Animation.REVERSE);
					// animation = new TranslateAnimation(offset, 0, 0, 0);
					// if (!isCloseGroupBuy) {
					// group_buy.setVisibility(View.VISIBLE);
					// }
					cursor.setBackgroundResource(R.drawable.point_tag);
				}
				break;
			}
			currIndex = arg0;
			// animation.setFillAfter(true);
			// animation.setDuration(300);
			// cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	}

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			pager.setCurrentItem(index);
		}
	};

	// 获得餐厅推荐信息，返回RestRecomInfoData
	private void excuteRestRecomInfo() {
		ServiceRequest request = new ServiceRequest(API.getPageRestInfoData3);
		request.addData("uuid", uuid);// typeTag=1,3为restId typeTag=2为recomId
		request.addData("typeTag", showTypeTag);// 1:餐厅 2：推荐 3：榜单
		request.addData("topRestTypeId", restTypeId);// typeTag=3为榜单id
		// -----------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----------------
		CommonTask.request(request, new CommonTask.TaskListener<PageRestInfo3DTO>() {

			@Override
			protected void onSuccess(PageRestInfo3DTO dto) {
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				pageRestInfoDTO = dto;
				// pageRestInfoDTOJson = JsonUtils.toJson(pageRestInfoDTO);

				if (dto.restInfo.favTag) {
					RestaurantDetailMainActivity.this.getBtnOption().setSelected(true);
				} else {
					RestaurantDetailMainActivity.this.getBtnOption().setSelected(false);
				}
				// RestaurantDetailMainActivity.this.getTvTitle().setText(dto.restInfo.name);
				restaurantId = dto.restInfo.uuid;
				uuid = dto.recomInfo.uuid;
				initPagerViewer();
				CommonTask.setCloseProgressDialogDelay(500);
			}

			@Override
			protected void onError(int code, String message) {
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				super.onError(code, message);
				finish();
				// doTest();
			}

			private void doTest() {
				String json = "{\"restInfo\":{\"uuid\":\"123456\",\"name\":\"牛肉粉馆\",\"favTag\":\"true\",\"restPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"restPicNum\":\"25\",\"avgPrice\":\"200\",\"tasteNum\":\"好\",\"envNum\":\"好\",\"serviceNum\":\"好\",\"canBookingTag\":\"true\",\"telForBooking\":\"57575777\",\"telForEdit\":\"57575777\",\"address\":\"浦东大道\",\"longitude\":\"0.00\",\"latitude\":\"0.00\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"},{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"}],\"openTimeInfo\":\"2\",\"menuTypeInfo\":\"湘菜\",\"trafficLine\":\"交通路线\",\"busInfo\":\"公交信息\",\"consumeType\":\"消费方式\",\"parkingPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"promoList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"couponList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"mealComboList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"totalSpecialFoodNum\":\"20\",\"specialFoodList\":[{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}},{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}}],\"cityId\":\"13456\",\"regionId\":\"132456\",\"regionName\":\"浦东新区\",\"districtId\":\"123456\",\"districtName\":\"上海\",\"mainMenuId\":\"123456\",\"mainMenuName\":\"菜单名字\",\"ydzkDetail\":\"预订折扣信息\",\"xjqDetail\":\"现金券信息\",\"cxDetail\":\"促销信息\",\"recomData\":{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"linkUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"recomInfo\":{\"uuid\":\"123456\",\"shareInfo\":{\"shareSmsDetail\":\"shareSmsDetail\",\"shareEmailDetail\":\"shareEmailDetail\",\"shareWeiboDetail\":\"shareWeiboDetail\",\"shareWeixinIconUrl\":\"shareWeixinIconUrl\",\"shareWeixinDetailUrl\":\"shareWeixinDetailUrl\",\"shareWeixinDetail\":\"shareWeixinDetail\",\"shareWeixinName\":\"shareWeixinName\",\"shareWeiboUuid\":\"shareWeiboUuid\"},\"favTag\":\"true\",\"title\":\"title\",\"createTime\":\"2014-12-23\",\"restId\":\"123456\",\"restName\":\"restName\",\"userId\":\"userId\",\"userNickName\":\"userNickName\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"userIsVipTag\":\"true\",\"picList\":[{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"},{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"}],\"totalLikeNum\":\"2000\",\"likedTag\":\"true\",\"tryRecomHint\":\"我要挑战说明\",\"relateRecomList\":[{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}]},\"showTypeTag\":\"1\"}";
				PageRestInfo3DTO dto = JsonUtils.fromJson(json, PageRestInfo3DTO.class);
				onSuccess(dto);
			}

		});
	}

	public PageRestInfo3DTO getPageRestInfo3DTO() {
		return pageRestInfoDTO;
	}

	public void addGroupBuy(final CouponPanelData data) {
		if (data == null) {
			return;
		}
		if (group_buy.getChildCount() != 0) {
			group_buy.removeAllViews();
		}
		View view = LayoutInflater.from(RestaurantDetailMainActivity.this).inflate(R.layout.group_buying_popupwindow, null);
		TextView group_buying_title = (TextView) view.findViewById(R.id.group_buying_title);
		Button group_buying_close = (Button) view.findViewById(R.id.group_buying_close);
		Button group_buying_bt = (Button) view.findViewById(R.id.group_buying_bt);
		TextView group_buying_time_name = (TextView) view.findViewById(R.id.group_buying_time_name);
		group_buying_detail = (TextView) view.findViewById(R.id.group_buying_detail);

		group_buying_title.setText(data.couponName);
		group_buying_bt.setText(data.btnName);
		if (data.countDownTag) {
			group_buying_time_name.setText(data.countDownHint);
			setRemainderTime(data.remainSeconds);
		} else {
			group_buying_time_name.setVisibility(View.GONE);
			if (data.couponHint != null) {
				group_buying_detail.setText(Html.fromHtml(data.couponHint));
			}
		}

		group_buying_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				isCloseGroupBuy = true;
				group_buy.setVisibility(View.GONE);
			}
		});

		group_buying_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 团购购买成功后 返回时 回到该页面
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("团购按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, data.uuid);
				ActivityUtil.jump(RestaurantDetailMainActivity.this, GroupBuyDetailActivity.class, 0, bundle);
			}
		});

		group_buy.addView(view);
	}

	/**
	 * 是否显示团购标签
	 * 
	 * @param b
	 */
	public void setVisibleGroupBuy(boolean b) {
		if (!isCloseGroupBuy) {
			if (b) {
				group_buy.setVisibility(View.VISIBLE);
			} else {
				group_buy.setVisibility(View.GONE);
			}
		} else {
			group_buy.setVisibility(View.GONE);
		}
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareSmsDetail;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareSmsDetail;
		}
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareEmailDetail;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareEmailDetail;
		}

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeiboDetail;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeiboDetail;
		}

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeixinDetail;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeixinDetail;
		}

	}

	@Override
	protected String getRestaurantUrl() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeixinIconUrl;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeixinIconUrl;
		}

	}

	@Override
	protected String getRestaurantLinkUrl() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeixinDetailUrl;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeixinDetailUrl;
		}

	}

	@Override
	protected String getWeixinName() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeixinName;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeixinName;
		}

	}

	@Override
	protected String getWeiboUuid() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.shareInfo == null ? "" : pageRestInfoDTO.recomInfo.shareInfo.shareWeiboUuid;
		} else {
			return pageRestInfoDTO.restInfo.shareInfo == null ? "" : pageRestInfoDTO.restInfo.shareInfo.shareWeiboUuid;
		}

	}

	@Override
	protected String getRestaurantId() {
		return uuid;
	}

	@Override
	protected String getRestaurantName() {
		if (shareTag == 0) {
			return pageRestInfoDTO.recomInfo.restName;
		} else {
			return pageRestInfoDTO.restInfo.name;
		}

	}

	// 开始计时 传入秒数
	private void setRemainderTime(final long remainSeconds) {
		// stopRemainderTime();
		// --
		timer = new Thread(new Runnable() {
			volatile long initSeconds = remainSeconds;
			boolean First = true;

			@Override
			public void run() {
				try {
					while (true) {
						initSeconds--;
						if (initSeconds < 0 || !isOnResume) {
							onPauseSurplusTime = initSeconds;
							break;
						}

						// Log.e("remainderTime.initSeconds"," "+isFirst+" "+initSeconds);
						// 优化，减少设置次数。整分钟的时候才设置，小于一分钟时，每秒都设置
						if (First || initSeconds >= 0) {
							First = false;
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// Log.e("remainderTime.setText","remainderTime.setText "+isFirst+" "+initSeconds);
									group_buying_detail.setText(Html.fromHtml(getTimeStringFromSeconds(initSeconds)));
									// if(initSeconds == 0)
									// { // 即将开始 剩余时间为0时，需要重新请求数据，刷新界面
									// excuteRestRecomInfo(); //
									// }
								}
							});
						}
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			}

		});
		timer.start();
	}

	//
	// // 停止计时
	// private void stopRemainderTime() {
	// if (timer != null) {
	// timer.interrupt();
	// }
	// }

	// 将秒数转换为天时分秒，当时间为0时，返回" - -"
	private String getTimeStringFromSeconds(long seconds) {
		if (seconds < 0) {
			seconds = 0;
		}
		StringBuilder sb = new StringBuilder();
		if (seconds / 86400 > 0) {
			sb.append("<font color=\"#f34747\">");
			sb.append(seconds / 86400);
			sb.append("</font>");
			sb.append(" 天 ");
		}
		if (seconds % 86400 / 3600 >= 0) {
			sb.append("<font color=\"#f34747\">");
			sb.append(seconds % 86400 / 3600);
			sb.append("</font>");
			sb.append(" 时 ");
		}
		if (seconds % 86400 % 3600 / 60 >= 0) {
			sb.append("<font color=\"#f34747\">");
			sb.append(seconds % 86400 % 3600 / 60);
			sb.append("</font>");
			sb.append(" 分 ");
		}
		if (seconds / 86400 == 0 && seconds % 86400 % 3600 % 60 >= 0) {
			sb.append("<font color=\"#f34747\">");
			sb.append(seconds % 86400 % 3600 % 60);
			sb.append("</font>");
			sb.append(" 秒 ");
		}
		// if ("0时0分0秒".equals(sb.toString().trim())) {
		// return " - -";
		// }
		return sb.toString();
	}

	float x1 = 0;
	float x2 = 0;
	float y1 = 0;
	float y2 = 0;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 当手指按下的时候
			x1 = ev.getX();
			y1 = ev.getY();
		}
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			// 当手指离开的时候
			x2 = ev.getX();
			y2 = ev.getY();
			if (y1 - y2 > 70) {
				// 页面向下滑动 隐藏
				hideSubTabMenu();
			} else if (y2 - y1 > 70) {
				// 页面向上滑动 显示
				showSubTabMenu();
			}
		}

		return super.dispatchTouchEvent(ev);
	}

	private void showSubTabMenu() {
		if (group_buy.getVisibility() == View.VISIBLE || isCloseGroupBuy) {
			return;
		}
		Animation in = AnimationUtils.loadAnimation(this, R.anim.index_slide_in_bottom_self);
		// group_buy.clearAnimation();
		in.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// if(order_bubble_clone.getText().toString().equals("0")){
				// order_bubble_clone.setVisibility(View.GONE);
				// }else{
				// order_bubble_clone.setVisibility(View.VISIBLE);
				// }
				group_buy.clearAnimation();
			}
		});
		group_buy.setAnimation(in);
		group_buy.startAnimation(in);
		group_buy.setVisibility(View.VISIBLE);
	}

	private void hideSubTabMenu() {
		if (group_buy.getVisibility() == View.GONE || isCloseGroupBuy) {
			return;
		}
		final Animation out = AnimationUtils.loadAnimation(this, R.anim.index_slide_out_bottom_self);
		// group_buy.clearAnimation();
		out.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// out.setDuration(3000);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// order_bubble_clone.setVisibility(View.GONE);
				group_buy.clearAnimation();
			}
		});
		group_buy.setAnimation(out);
		group_buy.startAnimation(out);
		group_buy.setVisibility(View.GONE);

	}

}
