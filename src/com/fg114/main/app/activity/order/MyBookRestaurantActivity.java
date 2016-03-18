package com.fg114.main.app.activity.order;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.RecommandRestaurantSubmitActivity;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.view.OrderSelectionWheelView;
import com.fg114.main.service.dto.OrderFormData;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderHintData2;
import com.fg114.main.service.dto.OrderInfoData;
import com.fg114.main.service.dto.OrderInfoData2;
import com.fg114.main.service.dto.RoomTypeInfoData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
/**
 * //订单表单：1修改表单  2新预定(BUNDLE_BOOK_ORDER_TAG)
 * @author dengxiangyu
 *
 */
public class MyBookRestaurantActivity extends MainFrameActivity {
	private boolean debug = false;
	private static final String TAG = "MyBookRestaurantActivity";

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private String orderId;
	private String restId;
	private long restTime;
	private long peopleNum;
	private long restType;
	private int  bookResOrderTag=2;//订单表单：1修改表单  2新预定
	private String activityId="";//活动ID 重web页获得
	private String activityDetail="";//活动内容 重web页获得
	private boolean isNewOrder;// true为第一次下单 false为第二次下单
	private boolean haveData = false;
	private String currentTime;// 当前时间
	private String restNameAndAddress;
	private UserInfoDTO userInfoDTO;

	private TextView myOrderMessageTime;
	private TextView myOrderMessagePeopleNum;
	private TextView myOrderMessageRoomType;
	private TextView myOrderAddress;
	private LinearLayout bookerDefaultMessageLayout;
//	private LinearLayout bookerMessageLayout;
//	private TextView bookerMessage;
//	private Button myUserEdit;
	private Button myOrderEdit;
	private ToggleButton bntToggle;
	private LinearLayout eaterLayout;
	private EditText bookerName;
	private Button bookerSex;
	private EditText bookerTel;
	private EditText eaterName;
	private Button eaterSexTag;
	private EditText eaterTel;
	private EditText memo;
	private Button bntSubmit;
	private View activity_bt;
	private TextView order_activity_detail;
	private RelativeLayout parentLayout;
	private OrderSelectionWheelView orderSelectionWheelView;
	private String[] orderPeopleMessage;
	private String[] weekday=new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
	
	private RoomTypeInfoData roomTypeInfoData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单表单", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
				orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
				if (CheckUtil.isEmpty(orderId)) {
					DialogUtil.showToast(this, "请稍后进行确认订单");
					finish();
				}
			}
			if (bundle.containsKey(Settings.BUNDLE_REST_ID)) {
				restId = bundle.getString(Settings.BUNDLE_REST_ID);
			}
			if (bundle.containsKey(Settings.BUNDLE_REST_NAME)) {
				restNameAndAddress = bundle.getString(Settings.BUNDLE_REST_NAME);
			}
			if (bundle.containsKey(Settings.BUNDLE_ORDER_TIME)) {
				restTime = bundle.getLong(Settings.BUNDLE_ORDER_TIME);
			}

			if (bundle.containsKey(Settings.BUNDLE_ORDER_PEOPLE_NUM)) {
				peopleNum = bundle.getLong(Settings.BUNDLE_ORDER_PEOPLE_NUM);
			}
			if (bundle.containsKey(Settings.BUNDLE_ORDER_ROOM_TYPE)) {
				restType = bundle.getLong(Settings.BUNDLE_ORDER_ROOM_TYPE);
			}
			
			if (bundle.containsKey(Settings.BUNDLE_BOOK_ORDER_TAG)) {
				bookResOrderTag = bundle.getInt(Settings.BUNDLE_BOOK_ORDER_TAG);
			}
			
			if (bundle.containsKey(Settings.BUNDLE_Activity_ID)) {
				activityId = bundle.getString(Settings.BUNDLE_Activity_ID);
			}
			if (bundle.containsKey(Settings.BUNDLE_Activity_Detail)) {
				activityDetail = bundle.getString(Settings.BUNDLE_Activity_Detail);
			}

			isNewOrder = CheckUtil.isEmpty(orderId) ? true : false;
			// --

			// Log.e(TAG, "restTime:" + restTime + " peopleNum:" + peopleNum +
			// " restType:" + restType);
			if (restTime != 0 && peopleNum != 0 && restType >= 0) {
				haveData = true;
			}
		}
		userInfoDTO = SessionManager.getInstance().getUserInfo(this);
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
		
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单表单", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		executeGetOrderFormInfoDataTask();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("请确认");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setText("登录");
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnTitle().setVisibility(View.GONE);
		this.getBottomLayout().setVisibility(View.GONE);
		this.setFunctionLayoutGone();

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.book_restaurant, null);

		myOrderMessageTime = (TextView) contextView.findViewById(R.id.my_order_message_time);
		myOrderMessagePeopleNum = (TextView) contextView.findViewById(R.id.my_order_message_peoplenum);
		myOrderMessageRoomType = (TextView) contextView.findViewById(R.id.my_order_message_roomtype);
		myOrderAddress = (TextView) contextView.findViewById(R.id.my_order_address);
		bookerDefaultMessageLayout = (LinearLayout) contextView.findViewById(R.id.booker_default_message_layout);
		myOrderEdit = (Button) contextView.findViewById(R.id.my_order_edit);
		bntToggle = (ToggleButton) contextView.findViewById(R.id.bnt_toggle);
		eaterLayout = (LinearLayout) contextView.findViewById(R.id.eater_layout);
		bookerName = (EditText) contextView.findViewById(R.id.booker_name);
		bookerSex = (Button) contextView.findViewById(R.id.booker_sex);
		bookerTel = (EditText) contextView.findViewById(R.id.booker_tel);
		eaterName = (EditText) contextView.findViewById(R.id.eater_name);
		eaterSexTag = (Button) contextView.findViewById(R.id.eater_sex_tag);
		eaterTel = (EditText) contextView.findViewById(R.id.eater_tel);
		memo = (EditText) contextView.findViewById(R.id.memo);
		bntSubmit = (Button) contextView.findViewById(R.id.bnt_submit);
		parentLayout = (RelativeLayout) contextView.findViewById(R.id.parent_layout);
		activity_bt=contextView.findViewById(R.id.activity_bt);
		order_activity_detail=(TextView)contextView.findViewById(R.id.order_activity_detail);
		bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
		
		// 点击去登录
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("登录按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(MyBookRestaurantActivity.this, UserLoginActivity.class, 0, bundle);
			}
		});
		// this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT);
		// 获得时间

//		myUserEdit.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("修改预订人信息按钮");
//				// -----
//
//				bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
//				bookerMessageLayout.setVisibility(View.GONE);
//
//			}
//		});

		bookerSex.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				bookerSex.setSelected(!bookerSex.isSelected());
				bookerSex.setTag(bookerSex.isSelected() ? 0 : 1);

				// Log.e(TAG, "bookerSex-->" + bookerSex.getTag());
			}
		});

		eaterSexTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				eaterSexTag.setSelected(!eaterSexTag.isSelected());
				eaterSexTag.setTag(eaterSexTag.isSelected() ? 0 : 1);
				// Log.e(TAG, "eaterSexTag-->" + eaterSexTag.getTag());
			}
		});
		bntToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 打开状态
					eaterLayout.setVisibility(View.VISIBLE);
				} else {
					// 关闭状态
					eaterLayout.setVisibility(View.GONE);
				}
			}
		});

		bntSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				executeSubmitOrderTask();
			}
		});
		
		if(CheckUtil.isEmpty(activityId)){
			activity_bt.setVisibility(View.GONE);
		}else{
			activity_bt.setVisibility(View.VISIBLE);
			order_activity_detail.setText(activityDetail);
		}
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// Log.e(TAG, "isNewOrder:" + isNewOrder + " haveData:" + haveData);


		
		myOrderEdit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				// -----
				OpenPageDataTracer.getInstance().addEvent("修改订单信息按钮");
				// -----
				showOrderDialog(restTime - restTime % 86400000, restTime % 86400000, peopleNum, restType);
			}

		});
		
	}
	Button bntCancel;
	private void showOrderDialog(final long dateWheels, final long timeWheels, final long peopleWheels,
			final long roomTypeWheels) {
		try {
			final Animation animationOut = AnimationUtils.loadAnimation(MyBookRestaurantActivity.this,
					R.anim.order_time_popupwindow_out);
			final Animation animationIn = AnimationUtils.loadAnimation(MyBookRestaurantActivity.this,
					R.anim.order_time_popupwindow_in);
			DialogUtil.showDialogNew(MyBookRestaurantActivity.this, R.layout.dialog_time,
					new DialogUtil.DialogEventListenerNew() {
						@Override
						public void onInit(final View contentView, final com.fg114.main.app.view.PopupWindow dialog) {
							contentView.setAnimation(animationOut);
							final OrderSelectionWheelView orderSelectionWheelView = (OrderSelectionWheelView) contentView
									.findViewById(R.id.order_info_selection_wheel);
							orderSelectionWheelView.setRoomTypeInfoData(roomTypeInfoData);
							Button bntConfirm = (Button) contentView.findViewById(R.id.bnt_confirm);
							bntCancel = (Button) contentView.findViewById(R.id.bnt_cancel);
							orderSelectionWheelView.setDateMilliSeconds(dateWheels);
							orderSelectionWheelView.setTimeMilliSeconds(timeWheels);
							orderSelectionWheelView.setPeopleNum(peopleWheels);
							orderSelectionWheelView.setRoomType(roomTypeWheels);
							bntConfirm.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									ViewUtils.preventViewMultipleClick(v, 1000);
									restTime = orderSelectionWheelView.getDateMilliSeconds()
											+ orderSelectionWheelView.getTimeMilliSeconds();
									peopleNum = orderSelectionWheelView.getPeopleNum();
									restType = orderSelectionWheelView.getRoomType();
									String time = getDateString(restTime);
									myOrderMessageTime.setText(time);
									myOrderMessagePeopleNum.setText(peopleNum + "人  ");
									myOrderMessageRoomType.setText(getRoomType(restType));
									hideOrderDialog(animationIn, contentView, dialog);
								}

								
							});
							bntCancel.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									hideOrderDialog(animationIn, contentView, dialog);
								}
							});						
						}
						//---
						private void hideOrderDialog(final Animation animationIn, final View contentView,final com.fg114.main.app.view.PopupWindow dialog) {
							contentView.setAnimation(animationIn);
							animationIn.start();
							animationIn.setAnimationListener(new MyAnimationListener(dialog, contentView));
						}
					});
			animationOut.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyAnimationListener implements AnimationListener {

		com.fg114.main.app.view.PopupWindow dialog;
		View contentView;

		public MyAnimationListener(com.fg114.main.app.view.PopupWindow dialog, View contentView) {
			this.dialog = dialog;
			this.contentView = contentView;
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			contentView.postDelayed(new Runnable() {

				@Override
				public void run() {
					dialog.dismiss();
				}
			}, 100);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}


	
	private void executeGetOrderFormInfoDataTask() {

		ServiceRequest request = new ServiceRequest(API.getOrderFormInfo);
		request.addData("restId", restId);
		CommonTask.request(request, "正在加载数据...", new CommonTask.TaskListener<OrderFormData>() {

			@Override
			protected void onSuccess(OrderFormData dto) {
				
				if (isNewOrder && !haveData) {
					// Log.e(TAG, "弹出时间对话框");
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							myOrderEdit.performClick();
						}
					}, 200);
					// showDialog(restTime - restTime % 86400000, restTime % 86400000,
					// peopleNum, restType);
				}

				roomTypeInfoData=dto.roomTypeInfoData;
				
				orderSelectionWheelView = new OrderSelectionWheelView(MyBookRestaurantActivity.this);
				// orderSelectionWheelView.setTimeMilliSeconds(value)
//				orderSelectionWheelView.initData(data.orderSelInfo,roomTypeInfoData);
				orderSelectionWheelView.setRoomTypeInfoData(roomTypeInfoData);
				if (haveData) {
					setWheelTime(restTime);
					orderSelectionWheelView.setPeopleNum(peopleNum);
					orderSelectionWheelView.setRoomType(restType);
				}
				restTime = orderSelectionWheelView.getDateMilliSeconds() + orderSelectionWheelView.getTimeMilliSeconds();
				orderPeopleMessage = SessionManager.getInstance().getOrderUserInfo(MyBookRestaurantActivity.this);
				if (isNewOrder) {
					// 第一次下单
					myOrderMessageTime.setText(getDateString(restTime));
					peopleNum = orderSelectionWheelView.getPeopleNum();
					restType = orderSelectionWheelView.getRoomType();
					myOrderMessagePeopleNum.setText(peopleNum + "人  ");
					myOrderMessageRoomType.setText(getRoomType(restType));
					myOrderAddress.setText(restNameAndAddress);

					// 判断缓存信息
					if (!CheckUtil.isEmpty(orderPeopleMessage[0]) && !CheckUtil.isEmpty(orderPeopleMessage[1])
							&& !CheckUtil.isEmpty(orderPeopleMessage[2])) {
						bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
						// Log.e(TAG, orderPeopleMessage[2]+"");
						String sexStr = orderPeopleMessage[2].equals("0") ? "女士" : "先生";
						bookerName.setText(orderPeopleMessage[0]);
						bookerTel.setText(orderPeopleMessage[1]);
						bookerSex.setTag(orderPeopleMessage[2]);
						bookerSex.setSelected(orderPeopleMessage[2].equals("0") ? true : false);
					} else {
						bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
						// 判断是否登录
						if (SessionManager.getInstance().isUserLogin(MyBookRestaurantActivity.this)) {
							if (userInfoDTO != null) {
								bookerName.setText(userInfoDTO.getTrueName());
								bookerTel.setText(userInfoDTO.getTel());
								bookerSex.setTag(userInfoDTO.getSexTag());
								bookerSex.setSelected(userInfoDTO.getSexTag() == 0 ? true : false);
							}
						}
					}
				} else {
					// 修改订单
					bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
					myOrderMessageTime.setText(getDateString(restTime));
					myOrderMessagePeopleNum.setText(peopleNum + "人  ");
					myOrderMessageRoomType.setText(getRoomType(restType));
					executeGetOrderInfoDataTask(orderId);
					
					
				}
			}

			@Override
			protected void onError(int code, String message) {
                super.onError(code, message);
//				DialogUtil.showToast(MyBookRestaurantActivity.this, message);
				finish();
			}
		});
	}
	
	private OrderInfoData2 orderInfoData;
	
	// TODO 加载订单信息
	private void executeGetOrderInfoDataTask(String orderId) {

		ServiceRequest request = new ServiceRequest(API.getOrderInfo2);

		request.addData("queryTypeTag", 1);
		request.addData("orderId", orderId);
		CommonTask.request(request, "正在加载数据...", new CommonTask.TaskListener<OrderInfoData2>() {

			@Override
			protected void onSuccess(OrderInfoData2 dto) {
				orderInfoData = dto;

				setStatusControl(dto);
			}

			@Override
			protected void onError(int code, String message) {

				DialogUtil.showToast(MyBookRestaurantActivity.this, message);
				finish();
			}

			private void doTest() {
				String json = "{\"hintOrderNum\":\"10\",\"prevOrderId\":\"1\",\"nextOrderId\":\"3\",\"orderId\":\"2\",\"orderHintData\":{\"orderId\":\"2\",\"restId\":\"123\",\"restName\":\"餐厅名\",\"iconUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"cashCouponId\":\"123\",\"reserveTime\":\"06月10日 18:50\",\"peopleNum\":\"4人\",\"roomTypeName\":\"房间类型 \",\"eaterName\":\"就餐人姓名\",\"eaterTel\":\"就餐人手机号\",\"statusTag\":\"1\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\",\"btnList\":[{\"typeTag\":\"1\",\"name\":\"按钮名称\",\"needFlashTag\":\"false\"},{\"typeTag\":\"1\",\"name\":\"按钮名称\",\"needFlashTag\":\"false\"}]},\"discountHint\":\"xxxxx<font color=#000000>xxx</font>xxx\",\"showPricePanelTag\":\"true\",\"receiptHint\":\"上传小票提示\",\"canUploadReceiptTag\":\"true\",\"receiptUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"bigReceiptUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"inputPriceHint\":\"上传小票提示\",\"canInputPriceTag\":\"true\",\"priceHint\":\"xxxxx<font color=#000000>xxx</font>xxx\",\"canReportPriceErrorTag\":\"true\",\"showCommentPanelTag\":\"true\",\"commentHint\":\"评论提示\",\"canCommentTag\":\"true\",\"commentList\":[{\"uuid\":\"111\",\"userName\":\"评论人\",\"userSmallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"61335372600000\",\"gradeTag\":\"true\",\"noGradeIntro\":\"没打分的说明\",\"likeTag\":\"true\",\"overallNum\":\"5.0\",\"tasteNum\":\"5.0\",\"envNum\":\"5.0\",\"serviceNum\":\"5.0\",\"detail\":\"评论内容\",\"picList\":[{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"},{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"}],\"replyNum\":\"5\",\"clientName\":\"来自Android客户端\"},{\"uuid\":\"111\",\"userName\":\"评论人\",\"userSmallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"61335372600000\",\"gradeTag\":\"true\",\"noGradeIntro\":\"没打分的说明\",\"likeTag\":\"true\",\"overallNum\":\"5.0\",\"tasteNum\":\"5.0\",\"envNum\":\"5.0\",\"serviceNum\":\"5.0\",\"detail\":\"评论内容\",\"picList\":[{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"},{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"}],\"replyNum\":\"5\",\"clientName\":\"来自Android客户端\"}],\"canEditTag\":\"true\",\"canCancelTag\":\"true\",\"reserveTime\":\"61335372600000\",\"peopleNum\":\"5\",\"roomTypeTag\":\"0\",\"bookerName\":\"张三\",\"bookerSexTag\":\"1\",\"bookerTel\":\"13000000000\",\"memo\":\"备注备注备注备注备注备注备注备注\",\"forOtherTag\":\"true\",\"eaterName\":\"李四\",\"eaterSexTag\":\"1\",\"eaterTel\":\"13000000000\"}";
				OrderInfoData2 data = JsonUtils.fromJson(json, OrderInfoData2.class);
				onSuccess(data);
			}
		});
	}

	private void setStatusControl(OrderInfoData2 dto) {
		bookerDefaultMessageLayout.setVisibility(View.VISIBLE);
		orderId = dto.orderHintData.orderId;
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		calendar.setTimeInMillis(dto.reserveTime);
//		String roomType = null;
//		switch (dto.roomTypeTag) {
//		case 0:
//			roomType = "只订大厅";
//			break;
//		case 1:
//			roomType = "只订包房";
//
//			break;
//		case 2:
//			roomType = "优先订大厅";
//
//			break;
//		case 3:
//			roomType = "优先订包房";
//
//			break;
//
//		default:
//			break;
//		}
		OrderHintData2 hintData = dto.orderHintData;
		restId = hintData.restId;
		restTime = dto.reserveTime;
		peopleNum = dto.peopleNum;
		restType = dto.roomTypeTag;

		// String time = (calendar.get((Calendar.MONTH)) + 1) + "月" +
		// calendar.get(Calendar.DAY_OF_MONTH) + "日" + " "
		// + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
		// calendar.get(Calendar.MINUTE);
		myOrderMessageTime.setText(Html.fromHtml(hintData.reserveInfo));
		myOrderMessagePeopleNum.setText(dto.peopleNum + "人  ");
		myOrderMessageRoomType.setText(getRoomType(restType));
		myOrderMessageRoomType.setTag(dto.roomTypeTag);
		myOrderAddress.setText(dto.orderHintData.restName);
		bookerName.setText(dto.bookerName);
		// Log.e(TAG, "bookerName-->" + dto.bookerName + " bookerTel-->" +
		// dto.bookerTel + " bookerSexTag-->"
		// + dto.bookerSexTag);
		bookerTel.setText(dto.bookerTel);
		bookerSex.setTag(dto.bookerSexTag);
		bookerSex.setSelected(dto.bookerSexTag == 0 ? true : false);
		boolean isSelectEater = dto.eaterSexTag == 1 ? false : true;
		if (dto.forOtherTag) {
			// 打开状态
			bntToggle.setChecked(true);
			eaterLayout.setVisibility(View.VISIBLE);
		} else {
			// 关闭状态
			bntToggle.setChecked(false);
			eaterLayout.setVisibility(View.GONE);
		}
		eaterName.setText(dto.eaterName.toString());
		// Log.e(TAG, dto.eaterName.toString());
		eaterSexTag.setSelected(isSelectEater);
		eaterTel.setText(dto.eaterTel);
		eaterSexTag.setTag(dto.eaterSexTag);
		memo.setText(dto.memo);
		String sexStr = dto.bookerSexTag == 0 ? "女士" : "先生";
		
		activityId=dto.activityId;
		activityDetail=dto.activityName;
		if(CheckUtil.isEmpty(activityId)){
			activity_bt.setVisibility(View.GONE);
		}else{
			activity_bt.setVisibility(View.VISIBLE);
			order_activity_detail.setText(activityDetail);
		}
		
	}

	private String getRoomType(long value) {
		final String[] room = new String[4];
		int roomCount=0;
		if(roomTypeInfoData==null){
			roomTypeInfoData=new RoomTypeInfoData();
		}
		if (roomTypeInfoData.onlyHallTag) {
			room[roomCount]="大厅";
			roomCount++;
		}
		if (roomTypeInfoData.onlyRoomTag) {
			room[roomCount]="包房";
			roomCount++;
		}
		if (roomTypeInfoData.firstHallTag) {
			room[roomCount]="先大厅";
			roomCount++;
		}
		if (roomTypeInfoData.firstRoomTag) {
			room[roomCount]="先包房";
		}
		
		if((int) value==0){
			return room[0];
		}else if((int) value==1){
			return room[1];
		}else if((int) value==2){
			return room[2];
		}else if((int) value==3){
			return room[3];
		}else{
			return room[0];
		}
	}

	// TODO提交订单
	private void executeSubmitOrderTask() {
		ServiceRequest request = new ServiceRequest(API.postOrder2);
		long postTag = isNewOrder ? 1 : 2;
		String orderIdStr = postTag == 1 ? null : orderId;
		request.addData("postTag", postTag);
		request.addData("orderId", orderIdStr);
		request.addData("restId", restId);
		request.addData("reserveTime", restTime);
		request.addData("peopleNum", peopleNum);
		request.addData("roomTypeTag", restType);
		request.addData("activityId", activityId);
		
		String name = null;
		String tel = null;
		// if (bookerDefaultMessageLayout.isShown()) {
		name = bookerName.getText().toString();
		if (CheckUtil.isEmpty(name)) {
			DialogUtil.showToast(MyBookRestaurantActivity.this, "预订人姓名不能为空");
			return;
		}
		request.addData("bookerName", name);
		request.addData("bookerSexTag", Integer.parseInt(bookerSex.getTag().toString()));
		tel = bookerTel.getText().toString();
		if (CheckUtil.isEmpty(tel)) {
			DialogUtil.showToast(MyBookRestaurantActivity.this, "预订人手机号码不能为空");
			return;
		}
		request.addData("bookerTel", tel);
		// }
		// else {
		// if (!CheckUtil.isEmpty(orderPeopleMessage[0]) &&
		// !CheckUtil.isEmpty(orderPeopleMessage[1])
		// && !CheckUtil.isEmpty(orderPeopleMessage[2])) {
		// name = orderPeopleMessage[0];
		// tel = orderPeopleMessage[1];
		// request.addData("bookerName", name);
		// request.addData("bookerSexTag",
		// Long.parseLong(orderPeopleMessage[2]));
		// request.addData("bookerTel", tel);
		// }
		// }

		if (!CheckUtil.isCellPhone(tel)) {
			DialogUtil.showToast(MyBookRestaurantActivity.this, "请输入正确的手机号码");
			return;
		}

		String memos = memo.getText().toString();
		if (CheckUtil.isEmpty(memos)) {
			request.addData("memo", "");
		} else {
			request.addData("memo", memo.getText().toString());
		}
		request.addData("forOtherTag", bntToggle.isChecked());

		if (bntToggle.isChecked()) {
			String eaterNameStr = eaterName.getText().toString();
			String eaterTelStr = eaterTel.getText().toString();
			if (CheckUtil.isEmpty(eaterNameStr)) {
				DialogUtil.showToast(MyBookRestaurantActivity.this, "就餐人姓名不能为空");
				return;
			}
			if (CheckUtil.isEmpty(eaterTelStr)) {
				DialogUtil.showToast(MyBookRestaurantActivity.this, "就餐人手机号码不能为空");
				return;
			}

			if (!CheckUtil.isCellPhone(eaterTelStr)) {
				DialogUtil.showToast(MyBookRestaurantActivity.this, "请输入正确的手机号码");
				return;
			}
			request.addData("eaterName", eaterNameStr);
			request.addData("eaterSexTag", Integer.parseInt(eaterSexTag.getTag().toString()));
			request.addData("eaterTel", eaterTelStr);
		} else {
			request.addData("eaterName", "");
			request.addData("eaterSexTag", 1);
			request.addData("eaterTel", "");

		}
		// Log.e(TAG, "postTag:" + postTag + " orderIdStr:" + orderIdStr +
		// " restId:" + restId + " restTime:" + restTime
		// + " peopleNum:" + peopleNum + " restType:" + restType +
		// " bookerName:" + bookerName + " bookerSexTag:"
		// + Integer.parseInt(bookerSex.getTag().toString()) + " bookerTel:" +
		// bookerTel + " memo:"
		// + memo.getText().toString() + " forOtherTag:" + bntToggle.isChecked()
		// + " eaterName:"
		// + eaterName.getText().toString() + " eaterSexTag" + " eaterTel:" +
		// eaterTel.getText().toString());

		// -----
		if(bookResOrderTag==1){
		OpenPageDataTracer.getInstance().addEvent("修改按钮");
		}else{
		OpenPageDataTracer.getInstance().addEvent("添加按钮");
		}
		// -----
		CommonTask.request(request, "正在提交", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				Fg114Application.isNeedUpdate = true;
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_ORDER_ID, dto.getUuid());
				
				if(bookResOrderTag==1){
					OpenPageDataTracer.getInstance().endEvent("修改按钮");
					bundle.putBoolean(Settings.BUNDLE_forAddTag, false);
				}else{
					OpenPageDataTracer.getInstance().endEvent("添加按钮");
					bundle.putBoolean(Settings.BUNDLE_forAddTag, true);
				}
				
				// -----
				SessionManager.getInstance().setOrderUserInfo(MyBookRestaurantActivity.this,
						bookerName.getText().toString(), bookerTel.getText().toString(), bookerSex.getTag() + "");
//				DialogUtil.showToast(MyBookRestaurantActivity.this, "提交成功");
				
				ActivityUtil.jump(MyBookRestaurantActivity.this, OrderSubmitSuccessActivity.class, 0, bundle, true);
				finish();
			}

			@Override
			protected void onError(int code, String message) {
				// -----
				if(bookResOrderTag==1){
					OpenPageDataTracer.getInstance().endEvent("修改按钮");
					}else{
					OpenPageDataTracer.getInstance().endEvent("添加按钮");
					}
				// -----
				if (debug) {
					doTest();
				}
				DialogUtil.showToast(MyBookRestaurantActivity.this, message);
				// finish();
			}

			private void doTest() {
				String json = "{\"uuid\":\"123\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"\",\"msg\":\"提示信息\",\"errorCode\":\"101\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
				SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
				onSuccess(data);
			}
		});

	}

	private int month;
	private int date;
	private int hour;
	private int minute;

	// 设置默认的时间 第一个值是String类型的时间
	private String getDateString(long restTime) {
		String time = "";
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		c.clear();
		// Date dateTime = new Date(restTime);
		c.setTimeInMillis(restTime);
		// c.setTime(dateTime);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		String day=weekday[c.get(Calendar.DAY_OF_WEEK)-1];
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Date dateTime = c.getTime();
		// SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日  HH:mm");
		// time=formatter.format(dateTime);

		String minuteFormat = (minute == 0 ? "00" : minute) + "";
		time = month + "月" + date + "日" + "  " +day+"  " + hour + ":" + minuteFormat;
		return time;
	}

	// 手机号码格式验证
	// private boolean isMobileNO(String mobiles) {
	// Pattern p =
	// Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
	// Matcher m = p.matcher(mobiles);
	// return m.matches();
	// }

	
	private void setWheelTime(long restTime) {
		orderSelectionWheelView.setDateMilliSeconds(restTime - restTime % 86400000);
		orderSelectionWheelView.setTimeMilliSeconds(restTime % 86400000);
	}


}
