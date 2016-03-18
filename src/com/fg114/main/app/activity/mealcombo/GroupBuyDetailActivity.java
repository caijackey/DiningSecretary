package com.fg114.main.app.activity.mealcombo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.RecommendRestaurantGalleryActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MyViewGroup;
import com.fg114.main.app.view.ScrollTopStopView;
import com.fg114.main.app.view.ScrollTopStopView.StayViewListener;
import com.fg114.main.service.dto.CashCouponData;
import com.fg114.main.service.dto.CouponDescribeData;
import com.fg114.main.service.dto.CouponInfoData;
import com.fg114.main.service.dto.CouponLimitRangeData;
import com.fg114.main.service.dto.CouponPicData;
import com.fg114.main.service.dto.CouponRestData;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.service.dto.RestTelInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

public class GroupBuyDetailActivity extends MainFrameActivity {
	// 传入参数
	private String uuid;
	// 缓存数据
	private int typeTag;

	private MyImageView group_buy_image;
	private TextView group_buy_state_name;
	private TextView group_buy_discount_price;
	private TextView group_buy_original_price;
	private TextView group_buy_apply_short_hint;
	private TextView group_buy_discount_price1;
	private TextView group_buy_original_price1;
	private TextView group_buy_apply_short_hint1;
	private Button buy;
	private Button buy1;
	private TextView group_buy_name;
	private LinearLayout limit_layout;
	private TextView limit_title;
	private TextView remain_seconds;
	private TextView limit_hint;
	private LinearLayout takeaway_property_view;
	private LinearLayout other_hint;
	private LinearLayout any_time_refund_hint_layout;
	private ImageView any_time_refund_hint_image;
	private TextView any_time_refund_hint_tv;
	private LinearLayout sold_num_hint_layout;
	private ImageView sold_num_hint_image;
	private TextView sold_num_hint_tv;
	private LinearLayout can_overtime_refund_layout;
	private ImageView can_overtime_refund_image;
	private TextView can_overtime_refund_tv;
	private LinearLayout remain_time_layout;
	private ImageView remain_time_image;
	private TextView remain_time_tv;
	private LinearLayout coupon_describe_list;
	private LinearLayout hint_layout;
	private TextView hint_title;
	private TextView hint_detail;
	private LinearLayout rest_list_layout;
	private LinearLayout for_rests;
	private LinearLayout coupon_detail_layout;
	private TextView coupon_detail_title;
	private TextView coupon_detail;
	private LinearLayout more_layout;
	private ScrollView scroll_layout;

	private long onPauseTime;// 记录退出时间
	private long onResumeTime;// 记录进入时间
	private boolean isOnResume;// 是否在前台
	private long onPauseSurplusTime;// 记录进入后台剩余时间
	private Thread timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		isOnResume = true;
		Bundle bundle = this.getIntent().getExtras();
		if (bundle.containsKey(Settings.UUID)) {
			uuid = bundle.getString(Settings.UUID);
		}
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购详情", uuid);
		// ----------------------------
		
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

		executeTask();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购详情", uuid);
		// ----------------------------
	}

	@Override
	protected void onResume() {

		super.onResume();
		
//		onResumeTime = System.currentTimeMillis();
//		if (!isOnResume && typeTag == 2) {
//			setRemainderTime(onPauseSurplusTime - (onResumeTime - onPauseTime) / 1000);
//		}
		if (!isOnResume) {
			executeTask();
		}
		isOnResume = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
//		onPauseTime = System.currentTimeMillis();
		isOnResume = false;
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View contextView = mInflater.inflate(R.layout.group_buy_detail_activity, null);
		scroll_layout=(ScrollView) contextView.findViewById(R.id.scroll_layout);
		group_buy_image = (MyImageView) contextView.findViewById(R.id.group_buy_image);
		group_buy_state_name = (TextView) contextView.findViewById(R.id.group_buy_state_name);

		group_buy_name = (TextView) contextView.findViewById(R.id.group_buy_name);
		limit_layout = (LinearLayout) contextView.findViewById(R.id.limit_layout);
		limit_title = (TextView) contextView.findViewById(R.id.limit_title);
		remain_seconds = (TextView) contextView.findViewById(R.id.remain_seconds);
		limit_hint = (TextView) contextView.findViewById(R.id.limit_hint);
		takeaway_property_view = (LinearLayout) contextView.findViewById(R.id.takeaway_property_view);
		other_hint = (LinearLayout) contextView.findViewById(R.id.other_hint);
		any_time_refund_hint_layout = (LinearLayout) contextView.findViewById(R.id.any_time_refund_hint_layout);
		any_time_refund_hint_image = (ImageView) contextView.findViewById(R.id.any_time_refund_hint_image);
		any_time_refund_hint_tv = (TextView) contextView.findViewById(R.id.any_time_refund_hint_tv);
		sold_num_hint_layout = (LinearLayout) contextView.findViewById(R.id.sold_num_hint_layout);
		sold_num_hint_image = (ImageView) contextView.findViewById(R.id.sold_num_hint_image);
		sold_num_hint_tv = (TextView) contextView.findViewById(R.id.sold_num_hint_tv);
		can_overtime_refund_layout = (LinearLayout) contextView.findViewById(R.id.can_overtime_refund_layout);
		can_overtime_refund_image = (ImageView) contextView.findViewById(R.id.can_overtime_refund_image);
		can_overtime_refund_tv = (TextView) contextView.findViewById(R.id.can_overtime_refund_tv);
		remain_time_layout = (LinearLayout) contextView.findViewById(R.id.remain_time_layout);
		remain_time_image = (ImageView) contextView.findViewById(R.id.remain_time_image);
		remain_time_tv = (TextView) contextView.findViewById(R.id.remain_time_tv);
		coupon_describe_list = (LinearLayout) contextView.findViewById(R.id.coupon_describe_list);
		hint_layout = (LinearLayout) contextView.findViewById(R.id.hint_layout);
		hint_title = (TextView) contextView.findViewById(R.id.hint_title);
		hint_detail = (TextView) contextView.findViewById(R.id.hint_detail);
		rest_list_layout = (LinearLayout) contextView.findViewById(R.id.rest_list_layout);
		for_rests = (LinearLayout) contextView.findViewById(R.id.for_rests);
		coupon_detail_layout = (LinearLayout) contextView.findViewById(R.id.coupon_detail_layout);
		coupon_detail_title = (TextView) contextView.findViewById(R.id.coupon_detail_title);
		coupon_detail = (TextView) contextView.findViewById(R.id.coupon_detail);
		more_layout = (LinearLayout) contextView.findViewById(R.id.more_layout);

		View group_buy = contextView.findViewById(R.id.group_buy);
		View group_buy2 = contextView.findViewById(R.id.group_buy2);
		group_buy_discount_price = (TextView) group_buy.findViewById(R.id.group_buy_discount_price);
		group_buy_original_price = (TextView) group_buy.findViewById(R.id.group_buy_original_price);
		group_buy_apply_short_hint = (TextView) group_buy.findViewById(R.id.group_buy_apply_short_hint);
		buy = (Button) group_buy.findViewById(R.id.group_buy_bt);
		group_buy_discount_price1 = (TextView) group_buy2.findViewById(R.id.group_buy_discount_price);
		group_buy_original_price1 = (TextView) group_buy2.findViewById(R.id.group_buy_original_price);
		group_buy_apply_short_hint1 = (TextView) group_buy2.findViewById(R.id.group_buy_apply_short_hint);
		buy1 = (Button) group_buy2.findViewById(R.id.group_buy_bt);

		ScrollTopStopView scrollTopStopView = (ScrollTopStopView) contextView.findViewById(R.id.scrollTopStopView);

		scrollTopStopView.setStayView(contextView.findViewById(R.id.group_buy), (ScrollView) contextView.findViewById(R.id.scroll_layout), new StayViewListener() {
			@Override
			public void onStayViewShow() {
				// 从下往上拉的时候回复显示
				contextView.findViewById(R.id.group_buy2).setVisibility(View.VISIBLE);
				// contextView.findViewById(R.id.group_buy).setVisibility(View.GONE);

			}

			@Override
			public void onStayViewGone() {
				// 从上往下拉隐藏布局
				contextView.findViewById(R.id.group_buy2).setVisibility(View.GONE);
				// contextView.findViewById(R.id.group_buy).setVisibility(View.VISIBLE);

			}
		});

		buy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// --------------
				OpenPageDataTracer.getInstance().addEvent("购买按钮");
				// --------------
				if(SessionManager.getInstance().isUserLogin(GroupBuyDetailActivity.this)){
				if (typeTag == 1) {
					Bundle bundle=new Bundle();
					bundle.putString(Settings.UUID, uuid);
					Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
					ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuyPaymentActivity.class, 0,bundle);
				} else if (typeTag == 2) {
					Bundle bundle=new Bundle();
					bundle.putString(Settings.UUID, uuid);
					Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
					ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuyPaymentActivity.class, 0,bundle);
				} else if (typeTag == 3) {
					Bundle bundle=new Bundle();
					bundle.putString(Settings.UUID, uuid);
					Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
					ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuySubmitActivity.class, 0,bundle);
				}
				}else{
					Bundle bundle = new Bundle();
					ActivityUtil.jump(GroupBuyDetailActivity.this, UserLoginActivity.class, 0, bundle);
				}
			}
		});
		buy1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if(SessionManager.getInstance().isUserLogin(GroupBuyDetailActivity.this)){
					// --------------
					OpenPageDataTracer.getInstance().addEvent("购买按钮");
					// --------------
					if (typeTag == 1) {
						Bundle bundle=new Bundle();
						bundle.putString(Settings.UUID, uuid);
						Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
						ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuyPaymentActivity.class, 0,bundle);
					} else if (typeTag == 2) {
						Bundle bundle=new Bundle();
						bundle.putString(Settings.UUID, uuid);
						Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
						ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuyPaymentActivity.class, 0,bundle);
					} else if (typeTag == 3) {
						Bundle bundle=new Bundle();
						bundle.putString(Settings.UUID, uuid);
						Settings.groupBuyActivityClazz = GroupBuyDetailActivity.class;
						ActivityUtil.jump(GroupBuyDetailActivity.this, GroupBuySubmitActivity.class, 0,bundle);
					}
					}else{
						Bundle bundle = new Bundle();
						ActivityUtil.jump(GroupBuyDetailActivity.this, UserLoginActivity.class, 0, bundle);
					}
				
			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void executeTask() {
		ServiceRequest request = new ServiceRequest(API.getCouponInfo);
		request.addData("uuid", uuid);
		// --------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// --------------
		CommonTask.request(request, "数据获取中，请稍候...", new CommonTask.TaskListener<CouponInfoData>() {

			@Override
			protected void onSuccess(CouponInfoData dto) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				scroll_layout.setVisibility(View.VISIBLE);
				setView(dto);
				if (dto != null) {
					typeTag = dto.typeTag;
				}
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				 finish();
//				doTest_cancel();

			}

			void doTest_cancel() {
				String json = "{\"uuid\":\"111\",\"typeTag\":\"1\",\"name\":\"有滋有味千团套餐\",\"statusName\":\"正在进行中\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picList\":[{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},{\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picWidth\":\"100\",\"picHeight\":\"100\",\"detail\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}],\"nowPrice\":\"100\",\"oldPrice\":\"40\",\"applyShortHint\":\"报名简短提示 标红\",\"btnEnabledTag\":\"true\",\"btnName\":\"购买购买\",\"describeList\":[{\"name\":\"描述名字:\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"\"},{\"name\":\"描述名\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"actionXmsUrl\"},{\"name\":\"描述名\",\"detail\":\"detaildetaildetaildetaildetaildetaildetaildetaildetail\",\"actionXmsUrl\":\"actionXmsUrl\"}],\"limitTitle\":\"limitTitle\",\"remainSeconds\":\"30000\",\"limitHint\":\"limitHintlimitHintlimitHint\",\"limitRangeList\":[{\"name\":\"12:00\",\"statusTag\":\"1\"},{\"name\":\"14:00\",\"statusTag\":\"1\"},{\"name\":\"16:00\",\"statusTag\":\"2\"},{\"name\":\"18:00\",\"statusTag\":\"3\"}],\"hintTitle\":\"hintTitlehintTitle\",\"hintDetail\":\"hintDetailhintDetailhintDetailhintDetailhintDetailhintDetailhintDetailhintDetail\",\"restList\":[{\"restId\":\"11\",\"restName\":\"restName\",\"restAddress\":\"restAddress\",\"distanceMeter\":\"200米\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"\",\"branch\":\"\"}]},{\"restId\":\"11\",\"restName\":\"restName\",\"restAddress\":\"restAddress\",\"distanceMeter\":\"200米\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"\",\"branch\":\"\"}]}],\"couponDetail\":\"couponDetailcouponDetailcouponDetailcouponDetail\",\"couponDetailWapUrl\":\"couponDetailWapUrlcouponDetailWapUrl\",\"canAnytimeRefundTag\":\"true\",\"anytimeRefundHint\":\"是否支持随时退款\",\"canOvertimeRefundTag\":\"true\",\"overtimeRefundHint\":\"是否支持过期退款\",\"soldNumHint\":\"200\",\"remainTimeHint\":\"4天20小时30分\"}";
				CouponInfoData data = JsonUtils.fromJson(json, CouponInfoData.class);
				onSuccess(data);

			}
		});
	}


	private void setView(final CouponInfoData data) {
		if (data == null) {
			return;
		}
		// 设置标题
		if (!CheckUtil.isEmpty(data.name)) {
			this.getTvTitle().setText(data.name);
		}

		if (data.typeTag == 1) {
			// 1:普通
			limit_layout.setVisibility(View.GONE);
			other_hint.setVisibility(View.VISIBLE);
			coupon_describe_list.setVisibility(View.GONE);
			hint_layout.setVisibility(View.VISIBLE);
			rest_list_layout.setVisibility(View.VISIBLE);
			coupon_detail_layout.setVisibility(View.VISIBLE);
			group_buy_discount_price.setVisibility(View.VISIBLE);
			group_buy_original_price.setVisibility(View.VISIBLE);
			group_buy_apply_short_hint.setVisibility(View.GONE);
			group_buy_discount_price1.setVisibility(View.VISIBLE);
			group_buy_original_price1.setVisibility(View.VISIBLE);
			group_buy_apply_short_hint1.setVisibility(View.GONE);

			// 其它提示 是否支持随时退款
			if (data.canAnytimeRefundTag) {
				any_time_refund_hint_image.setBackgroundResource(R.drawable.group_buy_yes);
			} else {
				any_time_refund_hint_image.setBackgroundResource(R.drawable.group_by_no);
			}
			any_time_refund_hint_tv.setText(data.anytimeRefundHint);

			// 是否支持过期退款
			if (data.canOvertimeRefundTag) {
				can_overtime_refund_image.setBackgroundResource(R.drawable.group_buy_yes);
			} else {
				can_overtime_refund_image.setBackgroundResource(R.drawable.group_by_no);
			}
			can_overtime_refund_tv.setText(data.overtimeRefundHint);

			sold_num_hint_tv.setText(data.soldNumHint);
			remain_time_tv.setText(data.remainTimeHint);

		} else if (data.typeTag == 2) {
			// 2：抢购
			limit_layout.setVisibility(View.VISIBLE);
			other_hint.setVisibility(View.GONE);
			coupon_describe_list.setVisibility(View.GONE);
			hint_layout.setVisibility(View.VISIBLE);
			rest_list_layout.setVisibility(View.VISIBLE);
			coupon_detail_layout.setVisibility(View.VISIBLE);
			group_buy_discount_price.setVisibility(View.VISIBLE);
			group_buy_original_price.setVisibility(View.VISIBLE);
			group_buy_apply_short_hint.setVisibility(View.GONE);
			group_buy_discount_price1.setVisibility(View.VISIBLE);
			group_buy_original_price1.setVisibility(View.VISIBLE);
			group_buy_apply_short_hint1.setVisibility(View.GONE);

			// 倒计时
			if(data.remainSeconds!=0){
			setRemainderTime(data.remainSeconds);
			}else{
			remain_seconds.setText(Html.fromHtml(getTimeStringFromSeconds(0)));
			}
			if(data.limitHint!=null){
			String s=data.limitHint.replace("\r\n", "<br>");
			limit_hint.setText(Html.fromHtml(s));
			}
			addCouponLimitRangeDataView(data.limitRangeList);
		} else if (data.typeTag == 3) {
			// 3：报名
			limit_layout.setVisibility(View.GONE);
			other_hint.setVisibility(View.GONE);
			coupon_describe_list.setVisibility(View.VISIBLE);
			hint_layout.setVisibility(View.VISIBLE);
			rest_list_layout.setVisibility(View.GONE);
			coupon_detail_layout.setVisibility(View.GONE);
			group_buy_discount_price.setVisibility(View.GONE);
			group_buy_original_price.setVisibility(View.GONE);
			group_buy_apply_short_hint.setVisibility(View.VISIBLE);
			group_buy_discount_price1.setVisibility(View.GONE);
			group_buy_original_price1.setVisibility(View.GONE);
			group_buy_apply_short_hint1.setVisibility(View.VISIBLE);

			addCouponDescribeDataView(data.describeList);
		}
        if(CheckUtil.isEmpty(data.statusName)){
        	group_buy_state_name.setVisibility(View.GONE);
        }else{
        	group_buy_state_name.setVisibility(View.VISIBLE);
        	group_buy_state_name.setText(data.statusName);
        }
		group_buy_image.setImageByUrl(data.picUrl, false, 0, ScaleType.FIT_XY);
		group_buy_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (data.picList != null) {
					// --------------
					OpenPageDataTracer.getInstance().addEvent("团购图片按钮");
					// --------------
					Bundle bundle = new Bundle();
					bundle.putInt(Settings.BUNDLE_KEY_ID, 0);
					bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, (Serializable) getList(data.picList));
					ActivityUtil.jump(GroupBuyDetailActivity.this, RecommendRestaurantGalleryActivity.class, 0, bundle);
				}
			}
		});

		group_buy_discount_price.setText( data.nowPrice);
		group_buy_original_price.setText(data.oldPrice);
		ViewUtils.setStrikethrough(group_buy_original_price);// 设置删除线
		if(data.applyShortHint!=null){
		group_buy_apply_short_hint.setText(Html.fromHtml(data.applyShortHint));
		}
		group_buy_discount_price1.setText(  data.nowPrice);
		group_buy_original_price1.setText(  data.oldPrice);
		ViewUtils.setStrikethrough(group_buy_original_price1);// 设置删除线
		if(data.applyShortHint!=null){
		group_buy_apply_short_hint1.setText(Html.fromHtml(data.applyShortHint));
		}

		if (data.btnEnabledTag) {
			buy.setBackgroundResource(R.drawable.bg_new_red);
			buy.setClickable(true);
			buy1.setBackgroundResource(R.drawable.bg_new_red);
			buy1.setClickable(true);
		} else {
			buy.setBackgroundResource(R.drawable.bg_new_order);
			buy.setClickable(false);
			buy1.setBackgroundResource(R.drawable.bg_new_order);
			buy1.setClickable(false);
		}
		buy.setPadding(UnitUtil.dip2px(10), 0, UnitUtil.dip2px(10), 0);
		buy1.setPadding(UnitUtil.dip2px(10), 0, UnitUtil.dip2px(10), 0);
		buy.setText(data.btnName);
		buy1.setText(data.btnName);

		group_buy_name.setText(data.name);

		if (!CheckUtil.isEmpty(data.limitTitle)) {
			limit_title.setText(data.limitTitle);
		}

		// 活动说明和特别提示
		if (CheckUtil.isEmpty(data.hintTitle)) {
			hint_layout.setVisibility(View.GONE);
		} else {
			hint_layout.setVisibility(View.VISIBLE);
			hint_title.setText(data.hintTitle);
			if (CheckUtil.isEmpty(data.hintDetail)) {
				hint_detail.setText("暂无");
			} else {
				hint_detail.setText(data.hintDetail);
			}
		}

		addCouponRestDataView(data.restList); 

		coupon_detail.setText(data.couponDetail);
		more_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// TODO Auto-generated method stub
				if(!CheckUtil.isEmpty(data.couponDetailWapUrl)){
					// --------------
					OpenPageDataTracer.getInstance().addEvent("团购详情按钮");
					// --------------
				ActivityUtil.jumpToWebNoParam(data.couponDetailWapUrl, "", true, null);
				}
			}
		});

	}

	private List<RestRecomPicData> getList(List<CouponPicData> data) {
		List<RestRecomPicData> restList = new ArrayList<RestRecomPicData>();
		for (int i = 0; i < data.size(); i++) {
			RestRecomPicData restData = new RestRecomPicData();
			restData.bigPicUrl = data.get(i).picUrl;
			restData.picWidth = data.get(i).picWidth;
			restData.picHeight = data.get(i).picHeight;
			restData.detail = data.get(i).detail;
			restList.add(restData);
		}
		return restList;

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
						if (initSeconds == -1) { 
							// 剩余时间为0时，需要重新请求数据，刷新界面
							Thread.sleep(1000);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									executeTask(); //
								}
							});
							break;
						}

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
									remain_seconds.setText(Html.fromHtml(getTimeStringFromSeconds(initSeconds)));

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
		if ("0秒".equals(sb.toString())) {
			return " - -";
		}
		return sb.toString();
	}

	/**
	 * 添加抢购时间段View
	 * 
	 * @param data
	 */
	private void addCouponLimitRangeDataView(List<CouponLimitRangeData> data) {
		if (data == null) {
			return;
		}
		if (takeaway_property_view.getChildCount() != 0) {
			takeaway_property_view.removeAllViews();
		}
		MyViewGroup myViewGroup = new MyViewGroup(this);
		myViewGroup.setWidthPadding(5);
		myViewGroup.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		for (int i = 0; i < data.size(); i++) {
			Button b = new Button(this);
			b.setWidth(UnitUtil.dip2px(80));
			b.setHeight(UnitUtil.dip2px(40));
			// b.setLayoutParams(new
			// LayoutParams(UnitUtil.dip2px(10),UnitUtil.dip2px(10)));
			b.setText(data.get(i).name);
			if (data.get(i).statusTag == 1) {
				// 1:已过期
				b.setBackgroundResource(R.drawable.group_buy_gray_bg);
				// b.setPadding(UnitUtil.dip2px(2), 0, UnitUtil.dip2px(2), 0);
				b.setTextColor(getResources().getColor(R.color.group_buy_color_gray));
				b.setTextSize(20);
				b.setGravity(Gravity.CENTER);
			} else if (data.get(i).statusTag == 2) {
				// 2：进行中
				b.setBackgroundResource(R.drawable.group_buy_green_bg);
				// b.setPadding(UnitUtil.dip2px(2), 0, UnitUtil.dip2px(2), 0);
				b.setTextColor(getResources().getColor(R.color.text_color_white));
				b.setTextSize(20);
				b.setGravity(Gravity.CENTER);
			} else if (data.get(i).statusTag == 3) {
				// 3：未开始
				b.setBackgroundResource(R.drawable.group_buy_green_kuang);
				// b.setPadding(UnitUtil.dip2px(2), 0, UnitUtil.dip2px(2), 0);
				b.setTextColor(getResources().getColor(R.color.text_color_new_green));
				b.setTextSize(20);
				b.setGravity(Gravity.CENTER);
			}
			myViewGroup.addView(b);
		}

		takeaway_property_view.addView(myViewGroup);

	}

	/**
	 * 增加团购描述列表
	 */
	private void addCouponDescribeDataView(final List<CouponDescribeData> data) {
		if (data == null) {
			return;
		}
		if (coupon_describe_list.getChildCount() != 0) {
			coupon_describe_list.removeAllViews();
		}
		for (int i = 0; i < data.size(); i++) {
			LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = mInflater.inflate(R.layout.coupon_describe_item, null);
			TextView coupon_describe_name = (TextView) view.findViewById(R.id.coupon_describe_name);
			TextView coupon_describe_detail = (TextView) view.findViewById(R.id.coupon_describe_detail);
			View coupon_describe_line = (View) view.findViewById(R.id.coupon_describe_line);
			ImageView coupon_describe_arrow = (ImageView) view.findViewById(R.id.coupon_describe_arrow);

			coupon_describe_name.setText(data.get(i).name);
			coupon_describe_detail.setText(data.get(i).detail);
			if (CheckUtil.isEmpty(data.get(i).actionXmsUrl)) {
				coupon_describe_arrow.setVisibility(View.GONE);
			} else {
				coupon_describe_arrow.setVisibility(View.VISIBLE);
				final int j=i;
				view.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						ViewUtils.preventViewMultipleClick(view, 1000);
						// TODO Auto-generated method stub
						URLExecutor.execute(data.get(j).actionXmsUrl, GroupBuyDetailActivity.this, 0);
					}
				});
			}
			if (i == (data.size() - 1)) {
				coupon_describe_line.setVisibility(View.GONE);
			}
			
			coupon_describe_list.addView(view);
		}

	}

	/**
	 * 增加适用餐厅列表
	 */
	private void addCouponRestDataView(final List<CouponRestData> data) {
		if (data == null) {
			return;
		}
		if (for_rests.getChildCount() != 0) {
			for_rests.removeAllViews();
		}
		for (int i = 0; i < data.size(); i++) {
			LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = mInflater.inflate(R.layout.coupon_restdata_item, null);
			TextView rest_name = (TextView) view.findViewById(R.id.rest_name);
			TextView rest_address = (TextView) view.findViewById(R.id.rest_address);
			TextView distance_meter = (TextView) view.findViewById(R.id.distance_meter);
			ImageView tel_image = (ImageView) view.findViewById(R.id.tel_image);
			View coupon_restdata_line = (View) view.findViewById(R.id.coupon_restdata_line);
			View rest_layout=(View) view.findViewById(R.id.rest_layout);

			rest_name.setText(data.get(i).restName);
			rest_address.setText(data.get(i).restAddress);
			distance_meter.setText(data.get(i).distanceMeter);

			final int k=i;
			rest_layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// TODO Auto-generated method stub
					// --------------
					OpenPageDataTracer.getInstance().addEvent("餐厅详情按钮");
					// --------------
					Bundle bundle=new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, data.get(k).restId);
					bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
				    ActivityUtil.jump(GroupBuyDetailActivity.this, RestaurantDetailMainActivity.class, 0,bundle);	
				}
			});
			if (data.get(i).telList != null && data.get(i).telList.size() > 0) {//
				tel_image.setVisibility(View.VISIBLE);
				final List<RestTelInfo> tels = data.get(i).telList;
				// tvTel.setText(restaurantInfo.getTel());

				for (int j = 0; j < tels.size(); j++) {
					final RestTelInfo tempTelInfo = tels.get(j);
					if (CheckUtil.isPhone(tempTelInfo.tel)) {
						final int ii = j;
						if (tempTelInfo.isTelCanCall) {

							tel_image.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View widget) {
									ViewUtils.preventViewMultipleClick(widget, 1000);
									// --------------
									OpenPageDataTracer.getInstance().addEvent("餐厅电话按钮");
									// --------------
									//
									ActivityUtil.callSuper57(GroupBuyDetailActivity.this, tempTelInfo.cityPrefix + tempTelInfo.tel);
									//
									final String userPhone;
									// 确定拨打的用户手机号
									if (SessionManager.getInstance().isUserLogin(GroupBuyDetailActivity.this)) {
										userPhone = SessionManager.getInstance().getUserInfo(GroupBuyDetailActivity.this).getTel();
									} else {
										userPhone = SharedprefUtil.get(GroupBuyDetailActivity.this, Settings.ANONYMOUS_TEL, "");
									}

									// 向后台传拨打数据
									new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												ServiceRequest.callTel(4, uuid,
														"(" + tempTelInfo.cityPrefix + ")" + tempTelInfo.tel
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
						}
					}
				}

			} else {
				// tvTel.setText("暂无");
				// 没有电话时显示“添加号码”按钮
				tel_image.setVisibility(View.GONE);
			}
			if (i == (data.size() - 1)) {
				coupon_restdata_line.setVisibility(View.GONE);
			}
			for_rests.addView(view);
		}
		// for_rests
	}
}
