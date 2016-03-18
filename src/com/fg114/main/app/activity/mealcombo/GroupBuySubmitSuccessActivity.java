package com.fg114.main.app.activity.mealcombo;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.order.MyNewTakeAwayOrderDetailActivity;
import com.fg114.main.app.activity.order.NewMyOrderDetailActivity;
import com.fg114.main.app.activity.order.OrderSubmitSuccessActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayRestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayBuyOrderInterceptActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.OrderStateHorizaontalScrollView;
import com.fg114.main.service.dto.CommonOrderStateData;
import com.fg114.main.service.dto.CouponOrderStateData;
import com.fg114.main.service.dto.OrderStateInfoData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;

/**
 * 团购订单状态提示
 * @author dengxiangyu
 *
 */
public class GroupBuySubmitSuccessActivity extends MainFrameActivity {
	private String orderId;

	private CommonOrderStateData data=new CommonOrderStateData();
	private AtomicBoolean isCanTask = new AtomicBoolean(true);

	private LayoutInflater mInflater;
	private View contextView;
	private MyImageView group_buy_icon_url;
	private TextView group_buy_title;
	private TextView group_buy_msg;
	private volatile Button group_buy_order_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		}
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购订单状态提示", "");
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

		executeOrderSubmitSuccess();

		
	}

	@Override
	public void onRestart() {
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购订单状态提示", "");
		// ----------------------------
		finish();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		returnToActivity(Settings.groupBuyActivityClazz);
		super.finish();
	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("提示");
		this.getBtnGoBack().setVisibility(View.GONE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.group_buy_order_submit_success_act, null);
		group_buy_icon_url = (MyImageView) contextView.findViewById(R.id.group_buy_icon_url);
		group_buy_title = (TextView) contextView.findViewById(R.id.group_buy_title);
		group_buy_msg = (TextView) contextView.findViewById(R.id.group_buy_msg);
		group_buy_order_bt = (Button) contextView.findViewById(R.id.group_buy_order_bt);

		// 自动更新等待时间
		// ----
		
		
		group_buy_order_bt.setBackgroundResource(R.drawable.gray_bt02);
		group_buy_order_bt.setClickable(false);
		
		group_buy_order_bt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----------------
				OpenPageDataTracer.getInstance().addEvent("继续按钮");
				// -----------------
				Bundle bd = new Bundle();
				bd.putString(Settings.BUNDLE_KEY_WEB_URL, data.orderDetailWapUrl);
				bd.putBoolean(Settings.BUNDLE_KEY_WEB_HIDE_TITLE, true);
				ActivityUtil.jump(GroupBuySubmitSuccessActivity.this, SimpleWebViewActivity.class, 0, bd);
//				if(data!=null){
//				ActivityUtil.jumpToWebNoParam(data.orderDetailWapUrl, "", true, null);
//				}
//				GroupBuySubmitSuccessActivity.this.finish();

			}
		});
		
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void executeOrderSubmitSuccess() {
		ServiceRequest request = new ServiceRequest(API.getCouponOrderState2);
		request.addData("orderId", orderId);
		// --------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// --------------

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<CommonOrderStateData>() {

			@Override
			protected void onSuccess(CommonOrderStateData dto) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				autoUpdateWaitTime();
				group_buy_order_bt.setText("5");
				try {
					if (dto != null && dto.succTag) {
						data = dto;
						isCanTask.set(false);
						group_buy_msg.setText(dto.msg);
						group_buy_title.setText(dto.title);
						group_buy_icon_url.setImageByUrl(dto.iconUrl, true, 0, ScaleType.FIT_XY);
						if (!TextUtils.isEmpty(dto.btnName)) {
							group_buy_order_bt.setBackgroundColor(getResources().getColor(R.color.text_color_white));
							group_buy_order_bt.setText(dto.btnName);
						}
						group_buy_order_bt.setBackgroundResource(R.drawable.bg_red_new);
						group_buy_order_bt.setClickable(true);

						
						
						GroupBuySubmitSuccessActivity.this.getBtnGoBack().setVisibility(View.VISIBLE);;
					} else {
						GroupBuySubmitSuccessActivity.this.getBtnGoBack().setVisibility(View.GONE);;
						group_buy_msg.setText(dto.msg);
						group_buy_title.setText(dto.title);
						group_buy_icon_url.setImageByUrl(dto.iconUrl, true, 0, ScaleType.FIT_XY);
						group_buy_msg.postDelayed(new Runnable() {

							@Override
							public void run() {
								executeOrderSubmitSuccess();
							}
						}, 5000); // 延迟五秒再次请求

					}
				} catch (Exception e) {
				}
			}

			protected void onError(int code, String message) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				
				if (data != null) {
					group_buy_msg.setText(data.msg);
					group_buy_title.setText(data.title);
					group_buy_icon_url.setImageByUrl(data.iconUrl, true, 0, ScaleType.FIT_XY);
				}
				group_buy_msg.postDelayed(new Runnable() {

					@Override
					public void run() {
						executeOrderSubmitSuccess();
					}
				}, 5000); // 延迟五秒再次请求
//				 doTest_confirm();
			}
			//
//			 private void doTest_confirm() {
//			 String s =
//			 "{\"succTag\":\"false\",\"iconUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"title\":\"提交成功\",\"msg\":\"详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情详情\",\"btnName\":\"查看详情\",\"orderDetailWapUrl\":\"http://www.baidu.com/\"}";
//			 CouponOrderStateData data = JsonUtils.fromJson(s,
//					 CouponOrderStateData.class);
//			 onSuccess(data);
//			
//			 }
		});
	}

	/**
	 * 开始自动更新等待时间
	 */

	private void autoUpdateWaitTime() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					int waitTime = Integer.parseInt(group_buy_order_bt.getText().toString());
					if (waitTime == 0) {
//						group_buy_order_bt.setBackgroundColor(getResources().getColor(R.color.text_color_black));
						group_buy_order_bt.setText("5");
					} else {
//						group_buy_order_bt.setBackgroundColor(getResources().getColor(R.color.text_color_black));
						group_buy_order_bt.setText(String.valueOf(waitTime - 1));
					}
				} catch (Exception e) {

				}
			}
		};

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (isCanTask.get()) {
						Thread.sleep(1000);
						handler.sendEmptyMessage(0);
						Thread.yield();
					}
				} catch (Exception e) {
					if (Settings.DEBUG)
						Log.e("更新时间出错", e.toString());
				}
			}
		}).start();
	}
	
}
