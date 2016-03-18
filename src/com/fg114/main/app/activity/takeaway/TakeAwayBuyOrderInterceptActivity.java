package com.fg114.main.app.activity.takeaway;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MyNewTakeAwayOrderDetailActivity;
import com.fg114.main.service.dto.CashCouponBuyStateData;
import com.fg114.main.service.dto.CommonOrderStateData;
import com.fg114.main.service.dto.TakeoutOnlinePayStateData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;

/**
 *  需要传进来的值:
 *  orderId
 * 
 * @author dengxiangyu
 * 
 */
public class TakeAwayBuyOrderInterceptActivity extends MainFrameActivity {

	private final String TAG = "ZyCashInterceptActivity";
	private final Context ctx = TakeAwayBuyOrderInterceptActivity.this;

	// 传入参数
	private int fromPage; // 返回页面
	private String orderId;

	// ----
	private TextView tvInterceptDesc;
	private volatile Button btWaitTime;
	private AtomicBoolean isCanTask = new AtomicBoolean(true);
	private CommonOrderStateData data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖在线支付状态", "");
		// ----------------------------
		try {
			// 获得传入参数
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
			}

			// 设置返回页
			this.setResult(fromPage);

			// 初始化界面
			initComponent();

			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
			if (!isNetAvailable) {
				// 没有网络的场合，去提示页
				Bundle bund = new Bundle();
				bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
				ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);

			}

			// ------ 异步获取数据
			getTakeoutOrderOnlinePayStateTask();

		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e(TAG, "", e);
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖在线支付状态", "");
		// ----------------------------
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		
		super.onResume();
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置Header标题栏
		this.getTvTitle().setText("外卖订单处理");
		this.getBtnGoBack().setVisibility(View.INVISIBLE);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = mInflater.inflate(R.layout.cash_coupon_intercept, null);
		tvInterceptDesc = (TextView) contentView.findViewById(R.id.cash_coupon_tvInterceptDesc);
		btWaitTime = (Button) contentView.findViewById(R.id.cash_coupon_btWaitTime);

		// ----
		btWaitTime.setBackgroundResource(R.drawable.gray_bt02);
		btWaitTime.setClickable(false);

		// 自动更新等待时间
		autoUpdateWaitTime();

		this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/*--------------------------------------------------------------------------
	| 监控是否已经购买外卖成功
	--------------------------------------------------------------------------*/
	// 获得外卖购买的状态(就是临时订单状态)，返回OrderCashCouponStateData.msg 返回的提示
	// succTag 是否成功 不成功继续等5秒刷新 成功 显示按钮
	void getTakeoutOrderOnlinePayStateTask() {
		ServiceRequest request = new ServiceRequest(API.getTakeoutOrderOnlinePayState2);
		request.addData("orderId", orderId);
		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		CommonTask.request(request, new CommonTask.TaskListener<CommonOrderStateData>() {
			@Override
			protected void onSuccess(CommonOrderStateData dto) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				try {
					if (dto != null && dto.succTag) {
						data = dto;
						isCanTask.set(false);
						tvInterceptDesc.setText(dto.msg);
						if (!TextUtils.isEmpty(dto.btnName)) {
							btWaitTime.setText(dto.btnName);
						} else {
							btWaitTime.setText("继续");
						}
						btWaitTime.setBackgroundResource(R.drawable.bg_red_new);
						btWaitTime.setClickable(true);
						
						btWaitTime.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View view) {
								ViewUtils.preventViewMultipleClick(view, 1000);
								// ----
								OpenPageDataTracer.getInstance().addEvent("继续按钮");
								// -----
								
								Bundle bundle = new Bundle();
								bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
								ActivityUtil.jump(TakeAwayBuyOrderInterceptActivity.this, MyNewTakeAwayOrderDetailActivity.class, 0, bundle);
								finish();
							}
						});
					} else {
						tvInterceptDesc.setText(dto.msg);
						tvInterceptDesc.postDelayed(new Runnable() {

							@Override
							public void run() {
								getTakeoutOrderOnlinePayStateTask();
							}
						}, 5000); // 延迟五秒再次请求

					}
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}

			};

			@Override
			protected void onError(int code, String message) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				try {
                    if(data!=null){
					tvInterceptDesc.setText(data.msg);
                    }
					tvInterceptDesc.postDelayed(new Runnable() {

						@Override
						public void run() {
							getTakeoutOrderOnlinePayStateTask();
						}
					}, 5000); // 延迟五秒再次请求

				} catch (Exception e) {
					if (Settings.DEBUG)
						Log.e(TAG, "", e);
				}
				
			};

		});
	}

	/**
	 * 开始自动更新等待时间
	 */
	private void autoUpdateWaitTime() {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					int waitTime = Integer.parseInt(btWaitTime.getText().toString());
					if (waitTime == 0) {
						btWaitTime.setText("5");
					} else {
						btWaitTime.setText(String.valueOf(waitTime - 1));
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
