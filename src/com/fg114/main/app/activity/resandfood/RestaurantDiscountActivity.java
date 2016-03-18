package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.mealcombo.GroupBuyDetailActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.view.LineView;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.ResPromoData;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestPromoData;
import com.fg114.main.service.dto.SpecialRestData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.WebboTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboContentBuilder;

/**
 * 餐厅优惠界面
 * 
 * @author recode by xujianjun, 2012-03-01
 * 
 */
public class RestaurantDiscountActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantDiscountActivity";

	// 传入参数
	private String restaurantId;// 餐厅id

	// 缓存数据
	private String restaurantName;
	// private String restaurantAddress;
	private String resUrl;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	LinearLayout mealComboListLayout;
	LinearLayout mealComboList;
	LinearLayout cashListLayout;
	LinearLayout cashList;
	LinearLayout cashListButtonLayout;
	Button cashListButton;
	LinearLayout promotionListLayout;
	LinearLayout promotionList;
	LinearLayout promotionListButtonLayout;
	Button promotionListButton;
	TextView noDiscount;
	private Button btnShareToSina;
	//
	RestInfoData info;

	private WebboTask webboTask;

	// 本地缓存数据
	private RestInfoData restaurantInfo;

	private Calendar mDefaultBookTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅优惠详情", "");
		//----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		Log.e(TAG, restaurantId);
		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		this.bundleData.putString(Settings.BUNDLE_KEY_ID, restaurantId);
		// 获得缓存的餐厅信息
		restaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);

		// 缓存数据获得
		info = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);
		restaurantName = info.name;

		if (bundle.containsKey(Settings.BUNDLE_DEFAULT_BOOK_TIME)) {
			mDefaultBookTime = (Calendar) bundle.getSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME);
		}

		// 初始化界面
		initComponent();
	}
	@Override
	public void onRestart() {
		super.onRestart();
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅优惠详情", "");
		//----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("全部优惠");
		this.getBtnGoBack().setText("餐厅详情");
		// this.getTvTitle().setText(R.string.text_title_all_discount);
		// this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.setFunctionLayoutGone();

		// 内容部分初始化

		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_discount, null);
		mealComboListLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_meal_combo_list_layout);
		mealComboList = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_meal_combo_list);

		cashListLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_cash_list_layout);
		cashList = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_cash_list);
		// cashListButtonLayout = (LinearLayout)
		// contextView.findViewById(R.id.restaurant_discount_cash_list_button_layout);
		// cashListButton = (Button)
		// contextView.findViewById(R.id.restaurant_discount_cash_list_button);

		promotionListLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_promotion_list_layout);
		promotionList = (LinearLayout) contextView.findViewById(R.id.restaurant_discount_promotion_list);
		promotionListButtonLayout = (LinearLayout) contextView
				.findViewById(R.id.restaurant_discount_promotion_list_button_layout);
		promotionListButton = (Button) contextView.findViewById(R.id.restaurant_discount_promotion_list_button);
		noDiscount = (TextView) contextView.findViewById(R.id.restaurant_discount_no_discount);
		btnShareToSina = (Button) contextView.findViewById(R.id.restaurant_discount_btnShareToSina);

		// 构建套餐列表
		buildMealComboList();

		// 构建现金券列表
		buildCashList();

		// 构建促销列表
		buildPromotionList();

		// ---如果没有优惠，显示没有优惠字样
		if (cashListLayout.getVisibility() == View.GONE && promotionListLayout.getVisibility() == View.GONE
				&& mealComboListLayout.getVisibility() == View.GONE) {
			noDiscount.setVisibility(View.VISIBLE);
			btnShareToSina.setVisibility(View.GONE);
		} else {
			noDiscount.setVisibility(View.GONE);
			btnShareToSina.setVisibility(View.GONE); // 分享功能已去掉
		}

		// 餐厅频道菜单
		// this.getMenuGroup().setOnCheckedChangeListener(new
		// OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(RadioGroup group, int checkId) {
		// if (checkId == getRbDetail().getId()) {
		// // 去餐厅详细页的场合
		// finish();
		// // overridePendingTransition(R.anim.right_slide_in,
		// // R.anim.right_slide_out);
		// ActivityUtil.overridePendingTransition(RestaurantDiscountActivity.this,
		// R.anim.right_slide_in, R.anim.right_slide_out);
		// } else if (checkId == getRbComment().getId()) {
		// // 去餐厅评论页的场合
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
		// if (mDefaultBookTime != null) {
		// bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME,
		// mDefaultBookTime);
		// }
		// ActivityUtil.jump(RestaurantDiscountActivity.this,
		// RestaurantCommentActivity.class, 0, bundle);
		// finish();
		// // overridePendingTransition(R.anim.right_slide_in,
		// // R.anim.right_slide_out);
		// ActivityUtil.overridePendingTransition(RestaurantDiscountActivity.this,
		// R.anim.right_slide_in, R.anim.right_slide_out);
		// } else if (checkId == getRbDiscount().getId()) {
		// // 去餐厅折扣页的场合
		// return;
		// } else if (checkId == getRbUpload().getId()) {
		// getRbDiscount().setChecked(true);// 将按钮恢复到原来状态
		// } else if (checkId == getRbOther().getId()) {
		// getRbDiscount().setChecked(true);// 将按钮恢复到原来状态
		// // ActivityUtil.shareRes(RestaurantDiscountActivity.this,
		// restaurantId);
		// }
		// }
		// });

		btnShareToSina.setOnClickListener(new OnClickListener() {
			/**
			 * 分享微博
			 */
			@Override
			public void onClick(View v) {
				// 将按钮禁止，防止快速连续点击
				ViewUtils.preventViewMultipleClick(v, 1000);
				btnShareToSina.post(new Runnable() {

					@Override
					public void run() {
						btnShareToSina.setEnabled(false);
						// ---1200ms后恢复按钮状态
						new Thread(new Runnable() {
							@Override
							public void run() {
								SystemClock.sleep(1200);
								btnShareToSina.post(new Runnable() {
									public void run() {
										btnShareToSina.setEnabled(true);
									}
								});
							}
						}).start();
					}
				});
				// ---
				// // 是否登录
				// DialogUtil.showUserLoginDialog(RestaurantDiscountActivity.this,
				// new Runnable() {
				// @Override
				// public void run() {
				// // 获得用户情报
				// final UserInfoDTO userInfo =
				// SessionManager.getInstance().getUserInfo(RestaurantDiscountActivity.this);
				// if (CheckUtil.isEmpty(userInfo.getSinaToken()) &&
				// CheckUtil.isEmpty(userInfo.getSinaSecret())) {
				// // 等待1秒，否则登录后返回此页面时无法显示提示框
				// SystemClock.sleep(1000);
				// // 未绑定的场合
				// DialogUtil.showAlert(RestaurantDiscountActivity.this, true,
				// getString(R.string.text_dialog_goto_bind_sina),
				// new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// // Bundle bundle = new Bundle();
				// // bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
				// Settings.RESTAURANT_DISCOUNT_ACTIVITY);
				// // ActivityUtil.jump(RestaurantDiscountActivity.this,
				// BindSinaWeiboActivity.class,
				// // Settings.RESTAURANT_DISCOUNT_ACTIVITY, bundle);
				// }
				// }, new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// dialog.cancel();
				// }
				// });
				// } else {
				// Handler handler = new Handler();
				// handler.postDelayed(new Runnable() {
				// @Override
				// public void run() {
				// if (Looper.myLooper() == null) {
				// Looper.prepare();
				// }
				// // 已绑定的场合，提交微博内容
				// //构造微博分享内容
				// WeiboContentBuilder wb=new WeiboContentBuilder();
				// wb.appendText("我想和大家分享\"" + restaurantName + "\"的优惠信息 。");
				// wb.appendUrl(resUrl);
				// wb.appendImportantText("【来自小秘书客户端】");
				// wb.appendUrl("http://www.xiaomishu.com/o/app");
				//
				// String lon = "";
				// String lat = "";
				//
				// ShareToSinaWeibo sinaWeibo =
				// ShareToSinaWeibo.getInstance(RestaurantDiscountActivity.this,
				// userInfo.getSinaToken(),
				// userInfo.getSinaSecret(), null);
				// sinaWeibo.shareToSina(wb.toWeiboString(), "", lon, lat);
				// }
				// }, 100);
				// RestaurantDiscountActivity.this.runOnUiThread(new Runnable()
				// {
				// @Override
				// public void run() {
				// showProgressDialog(getString(R.string.text_info_uploading));
				// }
				// });
				// }
				// }
				// }, 0);
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		if (restaurantInfo != null) {
			// 设置Mainframe中的Bundle信息，用于弹出餐厅报错时使用
			bundleData.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
			bundleData.putDouble(Settings.BUNDLE_REST_LONGITUDE, restaurantInfo.longitude);
			bundleData.putDouble(Settings.BUNDLE_REST_LATITUDE, restaurantInfo.latitude);
		}
	}

	@Override
	protected String getRestaurantId() {
		// 向父类提供数据
		return restaurantId;
	}

	@Override
	protected String getRestaurantName() {
		// 向父类提供数据
		return restaurantInfo == null ? "" : restaurantInfo.name;
	}

	// 根据数据构建套餐列表 TypeTag = 4
	private void buildMealComboList() {

		List<RestPromoData> mealComboList = info.mealComboList;

		if (mealComboList == null || mealComboList.size() == 0) {
			mealComboListLayout.setVisibility(View.GONE);
			return;
		}

		mealComboListLayout.setVisibility(View.VISIBLE);

		for (int i = 0; i < mealComboList.size(); i++) {
			RestPromoData temp = mealComboList.get(i);
			addPromotion4(temp, i, mealComboList.size());
		}

	}

	// 根据数据构建现金巻列表 TypeTag = 3
	private void buildCashList() {

		List<RestPromoData> couponList = info.couponList;

		if (couponList == null || couponList.size() == 0) {
			cashListLayout.setVisibility(View.GONE);
			return;
		}

		cashListLayout.setVisibility(View.VISIBLE);

		for (int i = 0; i < couponList.size(); i++) {
			RestPromoData temp = couponList.get(i);
			addPromotion3(temp, i, couponList.size());
		}

	}

	// 根据数据构建优惠列表
	private void buildPromotionList() {

		List<RestPromoData> promoList = info.promoList;

		if (promoList == null || promoList.size() == 0) {
			promotionListLayout.setVisibility(View.GONE);
			return;
		}
		promotionListLayout.setVisibility(View.VISIBLE);
		for (int i = 0; i < promoList.size(); i++) {
			RestPromoData temp = promoList.get(i);
			//1：券  2：惠  3：币 4：币(高亮)
			if (temp.getTypeTag() == 2 || temp.getTypeTag() == 3|| temp.getTypeTag() == 4) {// 惠,币
				addPromotion1(temp, i, promoList.size());
			} else if (temp.getTypeTag() == 1) {// 返
				addPromotion2(temp, i, promoList.size());
			}
		}
		promotionListButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				jumpToBookFromNetActivity();
			}
		});

	}

	private void jumpToGroupBuyDetailActivity(RestPromoData temp) {
		// -----
		OpenPageDataTracer.getInstance().addEvent("立即购买按钮");
		// -----
		// 跳转到套餐详情
		Bundle bundle = new Bundle();
		bundle.putString(Settings.UUID, temp.getCouponId());
		bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
		ActivityUtil.jump(RestaurantDiscountActivity.this, GroupBuyDetailActivity.class, 0, bundle);

	}

	/**
	 * @deprecated
	 * @param temp
	 */
	private void jumpToCashBuyActivity(ResPromoData temp) {
		SpecialRestData data = new SpecialRestData();
		data.setRestId(info.uuid);
		data.setRestName(info.name);
		data.setCouponId(temp.getCouponId());
		data.setCouponValue(temp.getCouponValue());
		data.setCouponUnitPrice(temp.getCouponUnitPrice());
		data.setCouponUseDescription(temp.getCouponUseDescription());
		data.setCouponUseHint(temp.getCouponUseHint());
		data.setCouponUserBeginTime(temp.getCouponUserBeginTime());
		data.setCouponUserEndTime(temp.getCouponUserEndTime());

		// 跳转到
		Bundle bundle = new Bundle();
		bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, data);

		ActivityUtil.jump(RestaurantDiscountActivity.this, GroupBuyDetailActivity.class, 0, bundle);

	}

	private void jumpToBookFromNetActivity() {

		Log.e(TAG, "uuid:" + info.uuid);

		// 跳转到预订页面
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.BUNDLE_KEY_ID, info.uuid);
		// bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "优惠信息");
		// if (mDefaultBookTime != null) {
		// bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME,
		// mDefaultBookTime);
		// }
		//
		//
		// ActivityUtil.jump(RestaurantDiscountActivity.this,
		// MyBookRestaurantActivity.class, 0, bundle);
		// -----
		OpenPageDataTracer.getInstance().addEvent("立即预订按钮");
		// -----
		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_REST_ID, info.uuid);
		bundle.putString(Settings.BUNDLE_REST_NAME, info.name);
		ActivityUtil.jump(RestaurantDiscountActivity.this, MyBookRestaurantActivity.class, 0, bundle);

	}

	// 套餐
	private void addPromotion4(final RestPromoData temp, int currentPosition, int total) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout buttonLayout = (LinearLayout) item.findViewById(R.id.restaurant_discount_cash_list_button_layout);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		Button buyButton = (Button) item.findViewById(R.id.restaurant_discount_cash_list_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);
		icon.setImageResource(R.drawable.meal_combo);
		price.setVisibility(View.VISIBLE);
		// blockbuttonLayout.setBackgroundResource(currentPosition==0?R.drawable.block_top_button:R.drawable.block_middle_button);
		// item.setPadding(0, 0, 0, UnitUtil.dip2px(10));
		line.setVisibility(View.GONE);
		buttonLayout.setVisibility(View.VISIBLE);

		// arrowRight.setPadding(0, 0, UnitUtil.dip2px(10), 0);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice() + "元");
		title.setText(temp.getTitle());
		content.setText("查看详情并购买");
		blockbuttonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				jumpToGroupBuyDetailActivity(temp);
			}

		});
		buyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				jumpToGroupBuyDetailActivity(temp);
			}
		});
		mealComboListLayout.addView(item);
	}

	// 券
	private void addPromotion3(final RestPromoData temp, int currentPosition, int total) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout buttonLayout = (LinearLayout) item.findViewById(R.id.restaurant_discount_cash_list_button_layout);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		Button buyButton = (Button) item.findViewById(R.id.restaurant_discount_cash_list_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);
		icon.setImageResource(R.drawable.coupon);
		price.setVisibility(View.VISIBLE);
		// blockbuttonLayout.setBackgroundResource(currentPosition==0?R.drawable.block_top_button:R.drawable.block_middle_button);
		// item.setPadding(0, 0, 0, UnitUtil.dip2px(10));
		line.setVisibility(View.GONE);
		buttonLayout.setVisibility(View.VISIBLE);

		// arrowRight.setPadding(0, 0, UnitUtil.dip2px(10), 0);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice() + "元");
		title.setText(temp.getTitle());
		content.setText(getString(R.string.text_layout_res_detail_buy_coupon));
		blockbuttonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				jumpToGroupBuyDetailActivity(temp);
			}

		});
		buyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				jumpToGroupBuyDetailActivity(temp);
			}
		});
		cashList.addView(item);
	}

	// 返
	private void addPromotion2(RestPromoData temp, int currentPosition, int total) {

		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);
		icon.setImageResource(R.drawable.refund);
		price.setVisibility(View.GONE);
		// content.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
		arrowRight.setVisibility(View.GONE);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice());
		title.setText(temp.getTitle());
		title.setTextColor(0xFFFF3300);
		// content.setText(Html.fromHtml(temp.getContent()));
		content.setText(temp.getContent());
		// content.setText(temp.getContent());
		// 校正背景显示-----------------------------------
		int bg = -1;
		if (info.canBookingTag) {
			promotionListButtonLayout.setVisibility(View.VISIBLE);
			if (currentPosition == 0) {
				bg = R.drawable.block_top_bt01;
			} else {
				bg = R.drawable.block_middle_bt01;
			}
		} else {
			promotionListButtonLayout.setVisibility(View.GONE);
			if (total == 1) {
				bg = R.drawable.block_bt01;
			} else if (currentPosition == 0) {
				bg = R.drawable.block_top_bt01;
			} else {
				bg = R.drawable.block_middle_bt01;
			}
		}

		// -----------------------------------------------
		blockbuttonLayout.setBackgroundResource(bg);
		// blockbuttonLayout.setPadding(UnitUtil.dip2px(10),
		// UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));
		promotionList.addView(item);
	}

	// 惠
	private void addPromotion1(RestPromoData temp, int currentPosition, int total) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.discount_nested_layout);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView promotion_mibi_text = (TextView) item.findViewById(R.id.promotion_mibi_text);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);

		//2：惠  3：币 4：币(高亮)
		if (temp.getTypeTag() == 4) {
			// 币高亮
			icon.setImageResource(R.drawable.icon_mibi_2);
			promotion_mibi_text.setVisibility(View.VISIBLE);
			promotion_mibi_text.setText(temp.pct);
		}else if (temp.getTypeTag() == 3) {
			// 币
			icon.setImageResource(R.drawable.icon_mibi_1);
			promotion_mibi_text.setVisibility(View.VISIBLE);
			promotion_mibi_text.setText(temp.pct);
		} else {
			// 惠
			icon.setImageResource(R.drawable.discount);
			// 惠需要把打折的字样高亮放大
			if (!CheckUtil.isEmpty(temp.getTitle())) {
				String s = temp.getTitle().replaceFirst("(\\d+((\\.{0,1}\\d+)|(\\d*))折)",
						"<font color=\"#FF0000\"><big><b>$1</b></big></font>");
				temp.setTitle(s);
			}
		}
		price.setVisibility(View.GONE);
		// content.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
		arrowRight.setVisibility(View.GONE);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice());
		title.setText(Html.fromHtml(temp.getTitle()));
		// content.setText(Html.fromHtml(temp.getContent()));
		content.setText(temp.getContent());

		// 校正背景显示-----------------------------------
		int bg = -1;
		if (info.canBookingTag) {
			promotionListButtonLayout.setVisibility(View.VISIBLE);
			if (currentPosition == 0) {
				bg = R.drawable.block_top_bt01;
			} else {
				bg = R.drawable.block_middle_bt01;
			}
		} else {
			promotionListButtonLayout.setVisibility(View.GONE);
			if (total == 1) {
				bg = R.drawable.block_bt01;
			} else if (currentPosition == 0) {
				bg = R.drawable.block_top_bt01;
			} else {
				bg = R.drawable.block_middle_bt01;
			}
		}

		// -----------------------------------------------
		blockbuttonLayout.setBackgroundResource(bg);
		// blockbuttonLayout.setPadding(UnitUtil.dip2px(10),
		// UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));
		promotionList.addView(item);

	}
}
