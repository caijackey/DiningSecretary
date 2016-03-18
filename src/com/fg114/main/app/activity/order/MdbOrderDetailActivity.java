package com.fg114.main.app.activity.order;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.Mdb.MdbConsumeEnsureActivity;
import com.fg114.main.app.activity.Mdb.MdbReceiptChkConfirmDataActivity;
import com.fg114.main.app.activity.Mdb.MdbRestDetaiActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayRestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.MdbFreeOrderInfoData;
import com.fg114.main.service.dto.MdbSearchRestListDTO;
import com.fg114.main.service.dto.OrderMoreReserveInfoData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;

public class MdbOrderDetailActivity extends MainFrameActivity {
	public String orderId;
	private String restId;

	// 界面组件
	private ScrollView mdb_scrollview;
	private View mdb_layout_center;
	private LinearLayout mdb_order_list_bt;
	private MyImageView mdb_icon_url;
	private TextView mdb_order_detail_status_name;
	private TextView mdb_order_detail_rest_name;
	private TextView mdb_order_detail_reserve_info;
	private ImageView mdb_san_jiao;

	// 预定信息
	private LinearLayout mdb_more_reserve_info_layout;

	// 功能提示
	private LinearLayout mdb_func_hint_layout;
	private TextView mdb_func_hint;

	// 验证码
	private LinearLayout mdb_valid_code_layout;
	private TextView mdb_valid_code_hint;
	private MyImageView mdb_vaild_code_image;
	private TextView mdb_valid_code;

	// 抽奖码
	private LinearLayout mdb_draw_layout;
	private TextView mdb_draw_hint;
	private TextView mdb_draw_num;
	private Button mdb_draw_bt;

	// 献花
	private LinearLayout mdb_flower_layout;
	private TextView mdb_flower_detail;
	private CheckBox flower_1;
	private CheckBox flower_3;
	private CheckBox flower_5;
	private View can_flower_layout;

	private Button mdb_edit_tag;
	private Button mdb_cancel_tag;

	// 数据
	private boolean OpenMoreReserveInfoPanelTag;
	private boolean needCountDownRefreshTag = false;

	// 倒计时
	private Thread timer;
	private long onPauseTime;// 记录退出时间
	private long onResumeTime;// 记录进入时间
	private boolean isOnResume = true;// 是否在前台
	private long onPauseSurplusTime;// 记录进入后台剩余时间
	// 上传分享截图
	private View mdb_upload_share_pic_layout;
	private TextView mdb_upload_share_pic_hint;
	private MyImageView mdb_upload_share_pic_image;
	private Button mdb_upload_share_pic_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		}
		if (bundle.containsKey(Settings.BUNDLE_REST_ID)) {
			restId = bundle.getString(Settings.BUNDLE_REST_ID);
		}
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		executeMdbOderDetailTask();
	}

	@Override
	protected void onResume() {

		super.onResume();
		onResumeTime = System.currentTimeMillis();
		if (!isOnResume) {
			isOnResume = true;
			// long time = onPauseSurplusTime - (onResumeTime - onPauseTime) /
			// 1000;
			// if (time > 0) {
			// needCountDownRefresh(time);
			// } else {
			// onPauseSurplusTime = 0;
			executeMdbOderDetailTask();
			// }
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		onPauseTime = System.currentTimeMillis();
		isOnResume = false;
		// stopRemainderTime();
	}

	// private static Class<? extends Activity>
	// mdbConsumeEnsureActivity=MdbConsumeEnsureActivity.class;

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		// if(Settings.mdbConsumeActivityClazz==mdbConsumeEnsureActivity){
		// //returnToActivity(Settings.mdbOrderDetailActivityClazz);
		// }
		try {
			if (timer != null) {
				timer.stop();
				timer.destroy();
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void initComponent() {
		this.getTvTitle().setText("订单详情");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("餐厅详情");
		this.setFunctionLayoutGone();

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


				ViewUtils.preventViewMultipleClick(v, 1000);

				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, restId);
				ActivityUtil.jump(MdbOrderDetailActivity.this, MdbRestDetaiActivity.class, 0, bundle);
			}
		});

		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = mInflater.inflate(R.layout.mdb_order_detail_activity, null);

		mdb_scrollview = (ScrollView) contextView.findViewById(R.id.mdb_scrollview);
		mdb_layout_center = (View) contextView.findViewById(R.id.mdb_layout_center);
		mdb_order_list_bt = (LinearLayout) contextView.findViewById(R.id.mdb_order_list_bt);
		mdb_icon_url = (MyImageView) contextView.findViewById(R.id.mdb_icon_url);
		mdb_order_detail_status_name = (TextView) contextView.findViewById(R.id.mdb_order_detail_status_name);
		mdb_order_detail_rest_name = (TextView) contextView.findViewById(R.id.mdb_order_detail_rest_name);
		mdb_order_detail_reserve_info = (TextView) contextView.findViewById(R.id.mdb_order_detail_reserve_info);
		mdb_san_jiao = (ImageView) contextView.findViewById(R.id.mdb_san_jiao);

		mdb_more_reserve_info_layout = (LinearLayout) contextView.findViewById(R.id.mdb_more_reserve_info_layout);

		mdb_func_hint_layout = (LinearLayout) contextView.findViewById(R.id.mdb_func_hint_layout);
		mdb_func_hint = (TextView) contextView.findViewById(R.id.mdb_func_hint);

		mdb_valid_code_layout = (LinearLayout) contextView.findViewById(R.id.mdb_valid_code_layout);
		mdb_valid_code_hint = (TextView) contextView.findViewById(R.id.mdb_valid_code_hint);
		mdb_vaild_code_image = (MyImageView) contextView.findViewById(R.id.mdb_vaild_code_image);
		mdb_valid_code = (TextView) contextView.findViewById(R.id.mdb_valid_code);

		mdb_draw_layout = (LinearLayout) contextView.findViewById(R.id.mdb_draw_layout);
		mdb_draw_hint = (TextView) contextView.findViewById(R.id.mdb_draw_hint);
		mdb_draw_num = (TextView) contextView.findViewById(R.id.mdb_draw_num);
		mdb_draw_bt = (Button) contextView.findViewById(R.id.mdb_draw_bt);
		can_flower_layout = contextView.findViewById(R.id.can_flower_layout);

		mdb_flower_layout = (LinearLayout) contextView.findViewById(R.id.mdb_flower_layout);
		mdb_flower_detail = (TextView) contextView.findViewById(R.id.mdb_flower_detail);

		mdb_edit_tag = (Button) contextView.findViewById(R.id.mdb_edit_tag);
		mdb_cancel_tag = (Button) contextView.findViewById(R.id.mdb_cancel_tag);

		mdb_upload_share_pic_layout = contextView.findViewById(R.id.mdb_upload_share_pic_layout);
		mdb_upload_share_pic_hint = (TextView) contextView.findViewById(R.id.mdb_upload_share_pic_hint);
		mdb_upload_share_pic_image = (MyImageView) contextView.findViewById(R.id.mdb_upload_share_pic_image);
		mdb_upload_share_pic_bt = (Button) contextView.findViewById(R.id.mdb_upload_share_pic_bt);

		flower_1 = (CheckBox) contextView.findViewById(R.id.flower_1);
		flower_3 = (CheckBox) contextView.findViewById(R.id.flower_3);
		flower_5 = (CheckBox) contextView.findViewById(R.id.flower_5);

		mdb_order_list_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!OpenMoreReserveInfoPanelTag) {
					mdb_more_reserve_info_layout.setVisibility(View.VISIBLE);
					mdb_san_jiao.setBackgroundResource(R.drawable.san_jiao_up);
					OpenMoreReserveInfoPanelTag = true;
				} else {
					mdb_more_reserve_info_layout.setVisibility(View.GONE);
					mdb_san_jiao.setBackgroundResource(R.drawable.san_jiao_down);
					OpenMoreReserveInfoPanelTag = false;
				}
			}
		});

		flower_1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub



				ViewUtils.preventViewMultipleClick(v, 1000);

				postMdbOrderFlowerTask(1);

			}
		});

		flower_3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub


	
				ViewUtils.preventViewMultipleClick(v, 1000);
						postMdbOrderFlowerTask(3);
					

			}
		});

		flower_5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				ViewUtils.preventViewMultipleClick(v, 1000);
						postMdbOrderFlowerTask(5);
					

			}
		});

		// mdb_edit_tag.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.UUID, orderId);
		// bundle.putInt(Settings.BUNDLE_TPYE_TAG, 2);
		// ActivityUtil.jump(MdbOrderDetailActivity.this,
		// MdbConsumerPaymentActivity.class, 0, bundle);
		// }
		// });

		mdb_cancel_tag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showAlert(MdbOrderDetailActivity.this, true, "你确定取消该订单吗？", "取消", "确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						cancelMdbFreeOrderTask();
					}
				});

			}
		});

		mdb_upload_share_pic_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				uploadTicketPicture();
			}
		});

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	private void executeMdbOderDetailTask() {
		ServiceRequest request = new ServiceRequest(API.getMdbFreeOrderInfo);
		request.addData("orderId", orderId);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<MdbFreeOrderInfoData>() {

			@Override
			protected void onSuccess(MdbFreeOrderInfoData dto) {
				setView(dto);
				if (dto != null) {
					OpenMoreReserveInfoPanelTag = dto.needOpenMoreReserveInfoPanelTag;
					if (dto.orderHintData != null) {
						restId = dto.orderHintData.restId;
					}
					needCountDownRefreshTag = dto.needCountDownRefreshTag;
					needCountDownRefresh(dto.countDownSec);
				}

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				finish();
			}
		});
	}

	private void cancelMdbFreeOrderTask() {
		ServiceRequest request = new ServiceRequest(API.cancelMdbFreeOrder);
		request.addData("orderId", orderId);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {

				DialogUtil.showAlert(MdbOrderDetailActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						executeMdbOderDetailTask();
					}
				});

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	private void postMdbOrderFlowerTask(int flowerNum) {
		ServiceRequest request = new ServiceRequest(API.postMdbOrderFlower);
		request.addData("orderId", orderId);
		request.addData("flowerNum", flowerNum);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {

				DialogUtil.showAlert(MdbOrderDetailActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						executeMdbOderDetailTask();
					}
				});

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	private FileInputStream input = null;

	private void uploadMdbOrderSharePic(final String path) {
		ServiceRequest request = new ServiceRequest(API.uploadMdbOrderSharePic);
		try {
			String tempPath = ActivityUtil.getGPSPicturePath(path);
			input = new FileInputStream(tempPath);
			request.addData(input);
		} catch (IOException e) {
			DialogUtil.showToast(MdbOrderDetailActivity.this, "请稍后再次尝试");
			e.printStackTrace();
			if (input != null) {
				try {
					input.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			return;
		}
		request.addData("orderId", orderId);

		CommonTask.request(request, "正在上传...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {

				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				DialogUtil.showAlert(MdbOrderDetailActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						executeMdbOderDetailTask();
					}
				});

			}

			@Override
			protected void onError(int code, String message) {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				super.onError(code, message);
			}
		});
	}

	private void setView(final MdbFreeOrderInfoData dto) {
		if (dto == null) {
			return;
		}

		// 预订信息
		if (dto.moreReserveInfoList != null || dto.moreReserveInfoList.size() != 0) {
			mdb_more_reserve_info_layout.setVisibility(View.VISIBLE);
			if (mdb_more_reserve_info_layout.getChildCount() != 0) {
				mdb_more_reserve_info_layout.removeAllViews();
			}
			List<OrderMoreReserveInfoData> orderMoreReserveInfoData = dto.moreReserveInfoList;

			for (int i = 0; i < orderMoreReserveInfoData.size(); i++) {
				LayoutInflater inflater = LayoutInflater.from(MdbOrderDetailActivity.this);
				View view = inflater.inflate(R.layout.more_reserve_info_item, null);
				TextView name = (TextView) view.findViewById(R.id.name);
				TextView detail = (TextView) view.findViewById(R.id.detail);

				name.setText(orderMoreReserveInfoData.get(i).name);

				detail.setText(orderMoreReserveInfoData.get(i).detail);

				mdb_more_reserve_info_layout.addView(view);
			}

		} else {
			mdb_more_reserve_info_layout.setVisibility(View.GONE);
		}

		if (dto.needOpenMoreReserveInfoPanelTag) {
			mdb_more_reserve_info_layout.setVisibility(View.VISIBLE);
			mdb_san_jiao.setBackgroundResource(R.drawable.san_jiao_up);
			OpenMoreReserveInfoPanelTag = true;
		} else {
			mdb_more_reserve_info_layout.setVisibility(View.GONE);
			mdb_san_jiao.setBackgroundResource(R.drawable.san_jiao_down);
			OpenMoreReserveInfoPanelTag = false;
		}

		// 订单提示面板
		if (dto.showOrderHintPanelTag) {
			mdb_order_list_bt.setVisibility(View.VISIBLE);
		} else {
			mdb_order_list_bt.setVisibility(View.GONE);
		}

		if (dto.orderHintData != null) {
			if (CheckUtil.isEmpty(dto.orderHintData.statusIconUrl)) {
				mdb_icon_url.setVisibility(View.GONE);
			} else {
				mdb_icon_url.setVisibility(View.VISIBLE);
				mdb_icon_url.setImageByUrl(dto.orderHintData.statusIconUrl, false, 0, ScaleType.FIT_XY);
			}
			if (!CheckUtil.isEmpty(dto.orderHintData.statusName)) {
				mdb_order_detail_status_name.setText(Html.fromHtml(dto.orderHintData.statusName));
			}

			mdb_order_detail_rest_name.setText(dto.orderHintData.restName);

			if (!CheckUtil.isEmpty(dto.orderHintData.orderHint)) {
				mdb_order_detail_reserve_info.setText(Html.fromHtml(dto.orderHintData.orderHint));
			}
		} else {
			mdb_order_list_bt.setVisibility(View.GONE);
		}

		if (CheckUtil.isEmpty(dto.funcHint)) {
			mdb_func_hint_layout.setVisibility(View.GONE);
		} else {
			mdb_func_hint_layout.setVisibility(View.VISIBLE);
			mdb_func_hint.setText(Html.fromHtml(dto.funcHint));
		}

		if (dto.showValidCodePanelTag) {
			mdb_valid_code_layout.setVisibility(View.VISIBLE);
		} else {
			mdb_valid_code_layout.setVisibility(View.GONE);
		}

		if (!CheckUtil.isEmpty(dto.validCodePanelHint)) {
			mdb_valid_code_hint.setText(Html.fromHtml(dto.validCodePanelHint));
		}

		if (CheckUtil.isEmpty(dto.validCodePicUrl)) {
			mdb_vaild_code_image.setVisibility(View.GONE);

		} else {
			mdb_vaild_code_image.setVisibility(View.VISIBLE);
			mdb_vaild_code_image.setImageByUrl(dto.validCodePicUrl, false, 0, ScaleType.FIT_XY);
		}

		mdb_valid_code.setText(dto.validCode);

		if (dto.showDrawPanelTag) {
			mdb_draw_layout.setVisibility(View.VISIBLE);
		} else {
			mdb_draw_layout.setVisibility(View.GONE);
		}

		if (!CheckUtil.isEmpty(dto.drawPanelHint)) {
			mdb_draw_hint.setVisibility(View.VISIBLE);
			mdb_draw_hint.setText(Html.fromHtml(dto.drawPanelHint));
		} else {
			mdb_draw_hint.setVisibility(View.GONE);
		}

		// mdb_draw_num.setText(dto.drawCode);

		if (dto.canDrawTag) {
			mdb_draw_bt.setVisibility(View.VISIBLE);
			if (!CheckUtil.isEmpty(dto.drawBtnName)) {
				mdb_draw_bt.setText(dto.drawBtnName);
			} else {
				mdb_draw_bt.setText("去抽奖");
			}

			mdb_draw_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub

					ViewUtils.preventViewMultipleClick(view, 1000);
                   ActivityUtil.jumpToWebNoParam(dto.drawWapUrl, "", true, null);

				}
			});
		} else {
			mdb_draw_bt.setVisibility(View.GONE);
		}

		if (dto.showFlowerPanelTag) {
			mdb_flower_layout.setVisibility(View.VISIBLE);
		} else {
			mdb_flower_layout.setVisibility(View.GONE);
		}
		if (!CheckUtil.isEmpty(dto.flowerPanelHint)) {
			mdb_flower_detail.setText(Html.fromHtml(dto.flowerPanelHint));
		}
		flower_1.setChecked(true);
		flower_3.setChecked(true);
		flower_5.setChecked(true);
		if (dto.canFlowerTag) {
			can_flower_layout.setVisibility(View.VISIBLE);
		} else {
			can_flower_layout.setVisibility(View.GONE);
		}

		if (dto.canEditTag) {
			mdb_edit_tag.setVisibility(View.VISIBLE);
			mdb_edit_tag.setClickable(true);
			mdb_edit_tag.setBackgroundResource(R.drawable.bg_new_red);
		} else {
			mdb_edit_tag.setVisibility(View.GONE);
			mdb_edit_tag.setBackgroundResource(R.drawable.bg_gary_new);
			mdb_edit_tag.setClickable(false);
		}

		if (dto.canCancelTag) {
			mdb_cancel_tag.setVisibility(View.VISIBLE);
			mdb_cancel_tag.setClickable(true);
		} else {
			mdb_cancel_tag.setVisibility(View.GONE);
			mdb_cancel_tag.setClickable(false);
		}

		// 上传分享截图
		if (dto.showUploadSharePicPanelTag) {
			mdb_upload_share_pic_layout.setVisibility(View.VISIBLE);
		} else {
			mdb_upload_share_pic_layout.setVisibility(View.GONE);
		}

		if (CheckUtil.isEmpty(dto.uploadSharePicHint)) {
			mdb_upload_share_pic_hint.setVisibility(View.GONE);
		} else {
			mdb_upload_share_pic_hint.setVisibility(View.VISIBLE);
			mdb_upload_share_pic_hint.setText(Html.fromHtml(dto.uploadSharePicHint));
		}

		if (CheckUtil.isEmpty(dto.uploadSharePicUrl)) {
			mdb_upload_share_pic_image.setVisibility(View.GONE);
		} else {
			mdb_upload_share_pic_image.setVisibility(View.VISIBLE);
			mdb_upload_share_pic_image.setImageByUrl(dto.uploadSharePicUrl, false, 0, ScaleType.FIT_CENTER);
		}

		if (dto.canUploadSharePicTag) {
			mdb_upload_share_pic_bt.setVisibility(View.VISIBLE);
		} else {
			mdb_upload_share_pic_bt.setVisibility(View.GONE);
		}

		mdb_upload_share_pic_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.createImageViewPanel(MdbOrderDetailActivity.this, mdb_upload_share_pic_layout, dto.uploadSharePicUrl);
			}
		});

	}

	private void needCountDownRefresh(final long remainSeconds) {
		if (!needCountDownRefreshTag) {
			return;
		}
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
									executeMdbOderDetailTask(); //
								}
							});
							break;
						}

						if (initSeconds < 0 || !isOnResume) {
							onPauseSurplusTime = initSeconds;
							break;
						}

						Thread.sleep(1000);

					}
				} catch (InterruptedException e) {
				}
			}

		});
		timer.start();
	}

	/**
	 * 上传图片
	 * 
	 * @param imageView
	 */
	public void uploadTicketPicture() {

		takePic(new OnShowUploadImageListener() {

			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				if (!CheckUtil.isEmpty(path)) {
					com.fg114.main.app.Settings.uploadPictureUri = "";
					uploadMdbOrderSharePic(path);
				}
			}

		}, false);
	}
}
