package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.view.MyTimePickerDialog;
import com.fg114.main.service.dto.OrderInfoData;
import com.fg114.main.service.dto.RoomState;
import com.fg114.main.service.dto.RoomStateAndOrderInfo;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.GetRestRoomStateAndOrderInfoTask;
import com.fg114.main.service.task.PostResReserveTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 网上订单
 * 
 * @author zhangyifan
 * 
 */
public class BookingFromNetActivity extends MainFrameActivity {

	private static final String[] DAT_OF_WEEK = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

	private static final int DRAWABLE_PADDING = UnitUtil.dip2px(5);

	private static final String TAG = "BookingFromNetActivity";

	private static final int TIME_DIALOG_ID = 0;// 时间Dialog
	private static final int DATE_DIALOG_ID = 1;// 日期Dialog

	// 获取餐位信息的结果
	private static final int GET_ROOM_STATE_SUCCESS = 0; // 成功
	private static final int GET_ROOM_STATE_FAIL = 1; // 失败
	private static final int GET_ROOM_STATE_IN_PROGRESS = 2;// 正在获取
	// 未选择预定位置类型
	private static final int ROOM_TYPE_TAG_INVALID = -99;
	// 特殊餐位状态的文字标识
	// private static final String ROOM_STATE_FEW = "(紧张)";
	// private static final String ROOM_STATE_FULL = "(已满)";
	// private static final String ROOM_STATE_DISABLE = "(不可预订)";

	private boolean isVisibility = false;

	// 本地缓存
	private boolean isLogin;
	private UserInfoDTO userInfo;

	// 传入参数
	private String restaurantId; // 当前餐厅ID
	private int fromPage;
	private String leftGoBackBtn;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	// private EditText etMobilephone; // 手机
	private TextView tvDineDate; // 用餐日期
	private TextView tvDineTime; // 用餐时间
	private EditText etPeopleNum; // 人数
	// private Button btnMore; //填写更多
	// private TextView tvMore;
	// private LinearLayout moreLayout; //更多内容
	private RadioGroup rgGroup; // 座位选择
	private RadioButton rbHall;
	private RadioButton rbRoom;
	private RadioButton rbHallThenRoom;
	private RadioButton rbRoomThenHall;
	private EditText etName; // 姓名
	private Spinner sexSpinner; // 性别
	private Button btnPost; // 提交按钮
	private EditText remark;
	private Button remarkVoiceButton;
	private Button phoneVoiceButton;
	private LinearLayout phoneVoiceButtonLayout;
	private ViewGroup vgProgress;
	private Button btRetry;
	private ViewGroup vgRetry;
	private TextView room_description;
	// 优惠活动
	private LinearLayout promotion_layout;
	private CheckedTextView promotion_title;
	private TextView promotion_description;
	private EditText promotion_number;
	// 画面变量
	private String tel;
	private long reserveTime;
	private int peopleNum;
	private String bookerName;
	private int sexTag;
	private int roomTypeTag = ROOM_TYPE_TAG_INVALID;
	private String remarkText;

	// date and time
	private int mYear = 0;
	private int mMonth = 0;
	private int mDay = 0;
	private int mHour = 0;
	private int mMinute = 0;
	private int mDayOfWeek = Calendar.SUNDAY;
	private String strDate;
	private String strTime;
	// 优惠信息
	private boolean selectApTag;
	private String apId;
	private String apNumber;

	// 是否是编辑定单
	boolean isEdit;
	// 传人的要被编辑的定单数据
	private OrderInfoData orderDetail;

	// 任务
	private PostResReserveTask postResReserveTask;

	private Calendar mDefaultBookTime;
	private int mDefaultRoomType = ROOM_TYPE_TAG_INVALID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);
		Log.e(TAG, "BookingFromNetActivity");

		// 获得缓存数据
		isLogin = SessionManager.getInstance().isUserLogin(this);
		userInfo = SessionManager.getInstance().getUserInfo(this);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		leftGoBackBtn = bundle.getString(Settings.BUNDLE_KEY_LEFT_BUTTON);

		//
		orderDetail = (OrderInfoData) bundle.get(Settings.BUNDLE_ORDER_DETAIL);
		if (orderDetail != null) {
			isEdit = true;
			// restaurantId = orderDetail.getRestId();
		}

		if (bundle.containsKey(Settings.BUNDLE_DEFAULT_BOOK_TIME)) {
			mDefaultBookTime = (Calendar) bundle.getSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME);
		}
		if (bundle.containsKey(Settings.BUNDLE_DEFAULT_ROOM_TYPE)) {
			mDefaultRoomType = bundle.getInt(Settings.BUNDLE_DEFAULT_ROOM_TYPE);
		}

		// 设置返回页
		this.setResult(fromPage);

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();

		// executeGetRoomStateForCanlender();
	}

	@Override
	public void finish() {
		// super.finish();
		// if (!CheckUtil.isEmpty(etMobilephone.getText().toString().trim())
		// || !CheckUtil.isEmpty(etPeopleNum.getText().toString().trim())
		// || !CheckUtil.isEmpty(etName.getText().toString().trim())
		// || !CheckUtil.isEmpty(remark.getText().toString().trim())) {
		if (!CheckUtil.isEmpty(etPeopleNum.getText().toString().trim())
				|| !CheckUtil.isEmpty(etName.getText().toString().trim())
				|| !CheckUtil.isEmpty(remark.getText().toString().trim())) {
			DialogUtil.showAlert(this, true, getString(R.string.text_dialog_order_finish),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							BookingFromNetActivity.super.finish();
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});

		} else {
			BookingFromNetActivity.super.finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 设置短信邀请最初页
		Settings.sendShortMessageOrignalActivityId = fromPage;
		// ------------------------
		// 获得缓存数据
		isLogin = SessionManager.getInstance().isUserLogin(this);
		userInfo = SessionManager.getInstance().getUserInfo(this);

		if (isLogin || isEdit) {
			// 已登录的场合
			this.getBtnOption().setVisibility(View.INVISIBLE);
		} else {
			this.getBtnOption().setVisibility(View.VISIBLE);
		}
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// ///-----------
		// 第一次下订单的提示
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { if
		 * (SharedprefUtil.getBoolean(BookingFromNetActivity
		 * .this,"showFirstAlertWhenBooking",true)||true) {
		 * SystemClock.sleep(500); //Log.i("BookingFromNetActivity","show pop");
		 * BookingFromNetActivity.this.runOnUiThread( new Runnable() {
		 * 
		 * @Override public void run() { try {
		 * 
		 * ImageView iv= new ImageView(BookingFromNetActivity.this);
		 * iv.setBackgroundResource(R.drawable.new_feature_1);
		 * iv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
		 * LayoutParams.FILL_PARENT));
		 * DialogUtil.showPopupWindow(BookingFromNetActivity.this,
		 * contextView,iv, true, null);
		 * //DialogUtil.showPopWindow(BookingFromNetActivity.this,
		 * remarkVoiceButton, "吾吾吾",R.drawable.new_feature_1);
		 * SharedprefUtil.saveBoolean
		 * (BookingFromNetActivity.this,"showFirstAlertWhenBooking",false); }
		 * catch (Exception ex) {
		 * Log.e("BookingFromNetActivity",ex.getMessage(),ex); } } }); } } catch
		 * (Exception ex) { Log.e("BookingFromNetActivity",ex.getMessage(),ex);
		 * }
		 * 
		 * }
		 * 
		 * }).start();
		 */

	}

	/**
	 * 组件初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(R.string.text_title_book);
		this.getBtnGoBack().setText(leftGoBackBtn);
		if (isLogin || isEdit) {
			// 已登录的场合，或者是编辑的时候隐藏登录按钮
			this.getBtnOption().setVisibility(View.INVISIBLE);
		} else {
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText(R.string.text_title_login);
			this.getBtnOption().setOnClickListener(new OnClickListener() {
				/**
				 * 用户登录
				 */
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					DialogUtil.showUserLoginDialog(BookingFromNetActivity.this, new Runnable() {
						@Override
						public void run() {
							isLogin = SessionManager.getInstance().isUserLogin(BookingFromNetActivity.this);
							userInfo = SessionManager.getInstance().getUserInfo(BookingFromNetActivity.this);
							if (isLogin) {
								// 已登录的场合
								// if
								// (!CheckUtil.isEmpty(userInfo.getTel()))
								// {
								// // 已有手机号
								// etMobilephone.setText(userInfo.getTel());
								// etMobilephone.requestFocus();
								// }
//								etMobilephone.setText(SessionManager.getInstance().getCacheBookPhone());
//								etMobilephone.requestFocus();

								// if
								// (!CheckUtil.isEmpty(userInfo.getTrueName()))
								// {
								// // 已有姓名
								// etName.setText(userInfo.getTrueName());
								// }

								if (userInfo.getSexTag() == 1) {
									sexSpinner.setSelection(1);
								} else {
									sexSpinner.setSelection(0);
								}
							}
						}
					}, 0);
				}
			});
		}

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.book_restaurant, null);

		// 获得页面组建
		// etMobilephone = (EditText)
		// contextView.findViewById(R.id.book_restaurant_etPhone);
		// tvDineDate = (TextView)
		// contextView.findViewById(R.id.book_restaurant_tvDate);
		// tvDineTime = (TextView)
		// contextView.findViewById(R.id.book_restaurant_tvTime);
		// etPeopleNum = (EditText)
		// contextView.findViewById(R.id.book_restaurant_etPeopleNum);
		// btnMore = (Button)
		// contextView.findViewById(R.id.book_restaurant_btnShowMore);
		// tvMore = (TextView)
		// contextView.findViewById(R.id.book_restaurant_tvShowMore);
		// moreLayout = (LinearLayout)
		// contextView.findViewById(R.id.book_restaurant_moreLayout);
		// rgGroup = (RadioGroup)
		// contextView.findViewById(R.id.book_restaurant_radioGroup);
		// rbHall = (RadioButton)
		// contextView.findViewById(R.id.book_restaurant_radio1);
		// rbRoom = (RadioButton)
		// contextView.findViewById(R.id.book_restaurant_radio2);
		// rbHallThenRoom = (RadioButton)
		// contextView.findViewById(R.id.book_restaurant_radio3);
		// rbRoomThenHall = (RadioButton)
		// contextView.findViewById(R.id.book_restaurant_radio4);
		// etName = (EditText)
		// contextView.findViewById(R.id.book_restaurant_etName);
		// sexSpinner = (Spinner)
		// contextView.findViewById(R.id.book_restaurant_sexSpinner);
		// btnPost = (Button)
		// contextView.findViewById(R.id.book_restaurant_btnOk);
		// vgProgress = (ViewGroup)
		// contextView.findViewById(R.id.book_restaurant_llProgress);
		// btRetry = (Button)
		// contextView.findViewById(R.id.book_restaurant_btRetry);
		// vgRetry = (ViewGroup)
		// contextView.findViewById(R.id.book_restaurant_flRetry);

		// room_description = (TextView) contextView
		// .findViewById(R.id.book_restaurant_room_description);

		// 语音备注录入 ------------------------added by xu jianjun, 2011-12-13
		// remark = (EditText)
		// contextView.findViewById(R.id.book_restaurant_remark_text);
		// remarkVoiceButton = (Button) contextView
		// .findViewById(R.id.book_restaurant_remark_voice_button);
		// phoneVoiceButton = (Button) contextView
		// .findViewById(R.id.book_restaurant_phone_voice_button);
		// phoneVoiceButtonLayout = (LinearLayout) contextView
		// .findViewById(R.id.book_restaurant_phone_voice_button_layout);
		// ----优惠活动
		// promotion_layout = (LinearLayout) contextView
		// .findViewById(R.id.book_restaurant_promotion_layout);
		// promotion_title = (CheckedTextView) contextView
		// .findViewById(R.id.book_restaurant_promotion_title);
		// promotion_description = (TextView) contextView
		// .findViewById(R.id.book_restaurant_promotion_description);
		// promotion_number = (EditText) contextView
		// .findViewById(R.id.book_restaurant_promotion_number);

		// 绑定语音按钮和结果框--------备注-------------------
		// RecognitionEngine eng = RecognitionEngine.getEngine(this);
		// if (eng != null) {
		// eng.bindButtonAndEditText(remarkVoiceButton, remark);
		// }
		// 绑定语音按钮和结果框--------手机号-------------------
		// RecognitionEngine eng2 = RecognitionEngine.getEngine(this);
		// if (eng2 != null) {
		// eng2.bindButtonAndEditText(phoneVoiceButton, etMobilephone, 0, null);
		// }
		// 控制只能输入数字，删除键。
//		etMobilephone.addTextChangedListener(new TextWatcher() {

//			private String oldValue = "";

//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//			}

//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//				if (Pattern.matches("\\d*", s.toString())) {
//					oldValue = s.toString();
//				}
//
//			}

//			@Override
//			public void afterTextChanged(Editable s) {
//				if (!Pattern.matches("\\d*", s.toString())) {
//					Log.e("afterTextChanged", s.toString());
//					// 新增联系人
//					restore(oldValue);
//				}
//			}
//		});

		Calendar calDefault = null;
		if (mDefaultBookTime != null) {
			calDefault = SessionManager.getInstance().fixCalendarToPer15(mDefaultBookTime);
		} else {
			calDefault = SessionManager.getInstance().getDefaultBookTime();
		}
		mYear = calDefault.get(Calendar.YEAR);
		mMonth = calDefault.get(Calendar.MONTH) + 1;
		mDay = calDefault.get(Calendar.DAY_OF_MONTH);
		mHour = calDefault.get(Calendar.HOUR_OF_DAY);
		mMinute = calDefault.get(Calendar.MINUTE);
		mDayOfWeek = calDefault.get(Calendar.DAY_OF_WEEK);

		strDate = getStringDate(mYear, mMonth, mDay);
		strTime = getStringTime(mHour, mMinute);

		tvDineDate.setText(mYear + "-" + getAddZeroTime(mMonth) + "-" + getAddZeroTime(mDay) + " "
				+ DAT_OF_WEEK[mDayOfWeek - 1]);
		tvDineDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 防止重复点击
				ViewUtils.preventViewMultipleClick(tvDineDate, 1000);
				// 设置当前日期
				showMyDialog(DATE_DIALOG_ID);
			}
		});

		tvDineTime.setText(getAddZeroTime(mHour) + ":" + getAddZeroTime(mMinute));
		tvDineTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 防止重复点击
				ViewUtils.preventViewMultipleClick(tvDineTime, 1000);
				// 设置当前时间
				showMyDialog(TIME_DIALOG_ID);
			}
		});

		rgGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				if (checkId == rbHall.getId()) {
					// 只定大厅的场合
					roomTypeTag = Settings.ROOM_TYPE_TAG_HALL;
				} else if (checkId == rbRoom.getId()) {
					// 环境图片的场合
					roomTypeTag = Settings.ROOM_TYPE_TAG_ROOM;
				} else if (checkId == rbHallThenRoom.getId()) {
					// 优先订大厅，包房也可以的场合
					roomTypeTag = Settings.ROOM_TYPE_TAG_HALL_THEN_ROOM;
				} else if (checkId == rbRoomThenHall.getId()) {
					// 优先订包房，大厅也可以的场合
					roomTypeTag = Settings.ROOM_TYPE_TAG_ROOM_THEN_HALL;
				} else {
					roomTypeTag = ROOM_TYPE_TAG_INVALID;
				}
			}
		});
		rgGroup.clearCheck();
		if (mDefaultRoomType != ROOM_TYPE_TAG_INVALID) {
			if (mDefaultRoomType == Settings.ROOM_TYPE_TAG_HALL) {
				rbHall.setChecked(true);
			} else if (mDefaultRoomType == Settings.ROOM_TYPE_TAG_ROOM) {
				rbRoom.setChecked(true);
			} else if (mDefaultRoomType == Settings.ROOM_TYPE_TAG_HALL_THEN_ROOM) {
				rbHallThenRoom.setChecked(true);
			} else if (mDefaultRoomType == Settings.ROOM_TYPE_TAG_ROOM_THEN_HALL) {
				rbRoomThenHall.setChecked(true);
			}
		}

		// 设置性别选择 spinner
		// 获取性别列表
		ArrayList<String> sexList = new ArrayList<String>();
		String[] sexArray = getResources().getStringArray(R.array.sex_list);
		// 获取XML中定义的性别数组
		for (int i = 0; i < sexArray.length; i++) {
			sexList.add(sexArray[i]);
		}
		ArrayAdapter<String> sexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_flexible_simple_gray_bg,
				sexList);
		// 设置下拉菜单的风格
		sexAdapter.setDropDownViewResource(R.layout.spinner_item);
		sexSpinner.setAdapter(sexAdapter);
		sexSpinner.setSelection(1);

		btnPost.setOnClickListener(new OnClickListener() {
			/**
			 * 提交订单
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!checkInput()) {
					return;
				}

				if (isLogin) {
					// 已登录的场合
					executePostResReserveTask();
				} else {
					// 未登录显示对话框
					DialogUtil.showPostOrderDialog(BookingFromNetActivity.this, tel, new Runnable() {

						@Override
						public void run() {
							executePostResReserveTask();
						}
					});
				}
			}
		});

		if (isLogin) {
			// 已登录的场合
			// if (!CheckUtil.isEmpty(userInfo.getTel())) {
			// // 已有手机号
			// etMobilephone.setText(userInfo.getTel());
			// }
//			etMobilephone.setText(SessionManager.getInstance().getCacheBookPhone());

			// if (!CheckUtil.isEmpty(userInfo.getTrueName())) {
			// // 已有姓名
			// //etName.setText(userInfo.getTrueName());
			// }

			if (userInfo.getSexTag() == 1) {
				sexSpinner.setSelection(1);
			} else {
				sexSpinner.setSelection(0);
			}
		} else {
//			etMobilephone.setText(SharedprefUtil.get(this, Settings.ANONYMOUS_TEL, ""));
			etName.setText(SharedprefUtil.get(this, Settings.ANONYMOUS_TRUE_NAME, ""));
		}

		// ---优惠信息显示初始化，编辑的时候，优惠信息不是在orderInfoData里带过来的，
		// 而是取roomState的时候刷出来的，用户必须重新填写

		// -----
		room_description.setVisibility(View.GONE);// 先隐藏
		promotion_layout.setVisibility(View.GONE);// 先隐藏
		promotion_title.setChecked(false);
		promotion_title.setText("");
		promotion_description.setText("");
		promotion_number.setText("");
		// ---
		// 编辑定单的状态，设置显示定单信息
		if (isEdit) {
			btnPost.setText("确认修改");
			setEditData();
		}

		btRetry.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				executeGetRoomStateForCanlender();
			}
		});

		// 点击显示我要参加活动
		promotion_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isVisibility) {
					promotion_number.setVisibility(View.GONE);
					promotion_title.setChecked(false);
					isVisibility = false;
				} else {
					promotion_number.setVisibility(View.VISIBLE);
					promotion_title.setChecked(true);
					isVisibility = true;
				}
			}
		});

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// 恢复到正确的内容
	private void restore(final String oldValue) {
		// 开新线程置空，防止HTC G7程序崩溃的bug
//		etMobilephone.post(new Runnable() {
//			@Override
//			public void run() {
//				etMobilephone.setText(oldValue);
//
//			}
//		});
	}

	private void setEditData() {

		// if (orderDetail == null) {
		// return;
		// }
		// getTvTitle().setText(getString(R.string.text_button_modify_order));
		// //etMobilephone.setText(orderDetail.getBookerTel());
		// etMobilephone.setEnabled(false);
		// etMobilephone.setTextColor(0xFFBBBBBB);
		// phoneVoiceButtonLayout.setVisibility(View.GONE);
		// // --日期时间设置-------------------------------
		// // 获得当前时间
		// Date date = new Date(orderDetail.getReserveTime());
		// Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// c.setTime(date);
		// mYear = c.get(Calendar.YEAR);
		// mMonth = c.get(Calendar.MONTH) + 1;
		// mDay = c.get(Calendar.DAY_OF_MONTH);
		// mHour = c.get(Calendar.HOUR_OF_DAY);
		// mMinute = c.get(Calendar.MINUTE);
		// mDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		//
		// /*
		// * // 比较当前时间是否超过当天18点30分 Calendar cDeadline = Calendar.getInstance();
		// * cDeadline.set(mYear, mMonth - 1, mDay, DEFAULT_HOUR,
		// DEFAULT_MINUTE);
		// * if (c.before(cDeadline)) { // 未超过则设置默认预订时间 mHour = DEFAULT_HOUR;
		// * mMinute = DEFAULT_MINUTE; mDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		// }
		// * else { // 超过则固定增加30分钟 Calendar cNew = (Calendar) c.clone();
		// * cNew.add(Calendar.MINUTE, 30);
		// *
		// * mDay = cNew.get(Calendar.DAY_OF_MONTH); mHour =
		// * cNew.get(Calendar.HOUR_OF_DAY); mMinute =
		// cNew.get(Calendar.MINUTE);
		// * mDayOfWeek = cNew.get(Calendar.DAY_OF_WEEK); }
		// */
		//
		// strDate = getStringDate(mYear, mMonth, mDay);
		// strTime = getStringTime(mHour, mMinute);
		//
		// tvDineDate.setText(mYear + "-" + getAddZeroTime(mMonth) + "-" +
		// getAddZeroTime(mDay) + " "
		// + DAT_OF_WEEK[mDayOfWeek - 1]);
		// tvDineTime.setText(getAddZeroTime(mHour) + ":" +
		// getAddZeroTime(mMinute));
		// // ---------------------------------------------
		// etPeopleNum.setText(orderDetail.getPeopleNum() + "");
		//
		// switch (orderDetail.getRoomTypeTag()) {
		// case Settings.ROOM_TYPE_TAG_HALL:
		// rbHall.setChecked(true);
		// break;
		// case Settings.ROOM_TYPE_TAG_ROOM:
		// rbRoom.setChecked(true);
		// break;
		// case Settings.ROOM_TYPE_TAG_HALL_THEN_ROOM:
		// rbHallThenRoom.setChecked(true);
		// break;
		// case Settings.ROOM_TYPE_TAG_ROOM_THEN_HALL:
		// rbRoomThenHall.setChecked(true);
		// break;
		//
		// }
		//
		// etName.setText(orderDetail.getBookerName());
		// sexSpinner.setSelection(orderDetail.getBookerSexTag());
		//
		// getTvTitle().setText("修改订单-" + orderDetail.getRestName());
		// // 备注
		// remark.setText(orderDetail.getMemo());
		// // ---优惠信息部分，不由dto带过来，由roomState从后台刷出来，用后重新填写
		// // promotion_title.setChecked(true);
		// // promotion_title.setText("参加航空活动");
		// // promotion_description.setText("参加就有机会获得大奖，快来拉");
		// // promotion_number.setText("6789033");

	}

	/**
	 * 提交订单
	 */
	private void executePostResReserveTask() {

		// if (!checkInput()) {
		// return;
		// }
		//
		// // 获得缓存数据
		// isLogin = SessionManager.getInstance().isUserLogin(this);
		// userInfo = SessionManager.getInstance().getUserInfo(this);
		// String token;
		// if (isLogin) {
		// token = userInfo.getToken();
		// } else {
		// token = "";
		// }
		//
		// // 获得选中的餐厅
		// postResReserveTask = new
		// PostResReserveTask(getString(R.string.text_info_loading), this,
		// token, restaurantId, tel, reserveTime, peopleNum, bookerName,
		// remarkText, sexTag,
		// roomTypeTag, isEdit ? 2 : 1, isEdit ? orderDetail.getOrderId() : "",
		// selectApTag,
		// apId, apNumber);
		//
		// postResReserveTask.execute(new Runnable() {
		//
		// @Override
		// public void run() {// 提交成功
		//
		// // --------ga跟踪----------
		// // 新建和修改定单都统计成此类
		// TraceManager.getInstance().enterPage("/shop/" + restaurantId +
		// "/book/success");
		// // ------------------------
		//
		// postResReserveTask.closeProgressDialog();
		// Fg114Application.isNeedUpdate = true;
		// // 订单内容收集
		//
		// // 订单提交成功的场合
		// String tips = "";
		// Calendar cNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		//
		// if (cNow.get(Calendar.HOUR_OF_DAY) >= 12) {
		// Calendar cTime1 =
		// Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// cTime1.set(Calendar.HOUR_OF_DAY, 21);
		// cTime1.set(Calendar.MINUTE, 30);
		//
		// Calendar cTime2 =
		// Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// cTime2.add(Calendar.DAY_OF_MONTH, 1);
		// cTime2.set(Calendar.HOUR_OF_DAY, 9);
		// cTime2.set(Calendar.MINUTE, 0);
		//
		// if (cNow.compareTo(cTime1) >= 0 && cNow.compareTo(cTime2) <= 0) {
		// tips = "现在是下班时间，您本次预订我们会在早上上班时间给您回复！";
		// }
		// } else {
		// Calendar cTime1 =
		// Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// cTime1.set(Calendar.HOUR_OF_DAY, 0);
		// cTime1.set(Calendar.MINUTE, 0);
		//
		// Calendar cTime2 =
		// Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// cTime2.set(Calendar.HOUR_OF_DAY, 9);
		// cTime2.set(Calendar.MINUTE, 0);
		//
		// if (cNow.compareTo(cTime1) >= 0 && cNow.compareTo(cTime2) <= 0) {
		// tips = "现在是下班时间，您本次预订我们会在早上上班时间给您回复！";
		// }
		// }
		//
		// if (isEdit) {
		// // 如果是修改订单，则直接弹出成功信息，然后结束当前页面，返回详情页面
		// DialogUtil.showAlert(BookingFromNetActivity.this, false, "订单修改成功!" +
		// tips,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// BookingFromNetActivity.super.finish();
		// }
		// });
		//
		// } else {
		// // 否则是新订单逻辑
		// boolean isTuan = false;
		// if (peopleNum >= 50) {
		// isTuan = true;
		// }
		// DialogUtil.showBookSuccessDialog(BookingFromNetActivity.this,
		// leftGoBackBtn,
		// tips, isTuan, new Runnable() {
		// @Override
		// public void run() {
		//
		// clearEditText();
		//
		// // 获得返回的订单id
		// String orderId = postResReserveTask.orderId;
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.BUNDLE_KEY_ID, orderId);
		// ActivityUtil.jump(BookingFromNetActivity.this,
		// MyOrderDetailActivity.class,
		// 0, bundle);
		//
		// }
		// },
		//
		// new Runnable() {
		//
		// @Override
		// public void run() {
		//
		// clearEditText();
		//
		// // 跳转到短信邀请模板选择页
		// String orderId = postResReserveTask.orderId;
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
		// bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
		// ActivityUtil.jump(BookingFromNetActivity.this,
		// SelectSMSActivity.class,
		// 0, bundle);
		// // BookingFromNetActivity.super.finish();
		//
		// // String orderId =
		// // postResReserveTask.orderId;
		// // String smsDetail =
		// // postResReserveTask.smsDetail;
		// // Bundle bundle = new Bundle();
		// // bundle.putString(Settings.BUNDLE_ORDER_ID,
		// // orderId);
		// // bundle.putString(Settings.BUNDLE_SMS_DETAIL,
		// // smsDetail);
		// // bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
		// // fromPage);
		// // ActivityUtil.jump(BookingFromNetActivity.this,
		// // SendSMSActivity.class,
		// // Settings.BOOKING_FROM_NET_ACTIVITY,
		// // bundle);
		// // BookingFromNetActivity.super.finish();
		// }
		// }, new Runnable() {
		//
		// @Override
		// public void run() {
		// // clearEditText();
		// BookingFromNetActivity.super.finish();
		// }
		// });
		// }
		//
		// }
		// }, new Runnable() {// 提交失败
		//
		// @Override
		// public void run() {
		// // DialogUtil.showToast(BookingFromNetActivity.this,
		// // dto.getMsg());
		//
		// }
		// });
		/*
		 * postResReserveTask.execute(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * postResReserveTask.closeProgressDialog();
		 * 
		 * //订单内容收集 final ChkDTO dto = postResReserveTask.dto; final String msg
		 * = dto.getMsg(); if (dto != null) { if (dto.isSuccTag()) { if
		 * (!isLogin) { SharedprefUtil.save(BookingFromNetActivity.this,
		 * Settings.ANONYMOUS_TEL, etMobilephone.getText().toString());
		 * SharedprefUtil.save(BookingFromNetActivity.this,
		 * Settings.ANONYMOUS_TRUE_NAME, etName.getText().toString()); }
		 * //订单提交成功的场合
		 * DialogUtil.showBookSuccessDialog(BookingFromNetActivity.this,
		 * leftGoBackBtn, new Runnable() {
		 * 
		 * @Override public void run() { //获得返回的订单id String orderId =
		 * dto.getFieldName(); Bundle bundle = new Bundle();
		 * bundle.putString(Settings.BUNDLE_KEY_ID, orderId);
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage);
		 * ActivityUtil.jump(BookingFromNetActivity.this,
		 * MyOrderDetailActivity.class, Settings.BOOKING_FROM_NET_ACTIVITY,
		 * bundle); BookingFromNetActivity.super.finish(); } },
		 * 
		 * new Runnable() {
		 * 
		 * @Override public void run() { String orderId = dto.getFieldName();
		 * String smsDetail = dto.getMsg(); Bundle bundle = new Bundle();
		 * bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
		 * bundle.putString(Settings.BUNDLE_SMS_DETAIL, smsDetail);
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage);
		 * ActivityUtil.jump(BookingFromNetActivity.this, SendSMSActivity.class,
		 * Settings.BOOKING_FROM_NET_ACTIVITY, bundle);
		 * BookingFromNetActivity.super.finish(); } }, new Runnable() {
		 * 
		 * @Override public void run() { //
		 * BookingFromNetActivity.this.finish();
		 * BookingFromNetActivity.super.finish(); } });
		 * 
		 * // String[] button = {"查看订单", "返回" + leftGoBackBtn, "短信邀请"}; //
		 * DialogUtil.showComfire(BookingFromNetActivity.this, //
		 * "恭喜您，您的订单已经成功提交！", // "您现在可以选择:", // button, // new Runnable() { //
		 * // @Override // public void run() { // //获得返回的订单id // String orderId
		 * = dto.getFieldName(); // Bundle bundle = new Bundle(); //
		 * bundle.putString(Settings.BUNDLE_KEY_ID, orderId); //
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage); //
		 * ActivityUtil.jump(BookingFromNetActivity.this,
		 * MyOrderDetailActivity.class, Settings.BOOKING_FROM_NET_ACTIVITY,
		 * bundle); // } // }, // new Runnable() { // // @Override // public
		 * void run() { // BookingFromNetActivity.this.finish(); // } // }, //
		 * new Runnable() { // // @Override // public void run() { // String
		 * orderId = dto.getFieldName(); // String smsDetail = dto.getMsg(); //
		 * Bundle bundle = new Bundle(); //
		 * bundle.putString(Settings.BUNDLE_ORDER_ID, orderId); //
		 * bundle.putString(Settings.BUNDLE_SMS_DETAIL, smsDetail); //
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage); //
		 * ActivityUtil.jump(BookingFromNetActivity.this, SendSMSActivity.class,
		 * Settings.BOOKING_FROM_NET_ACTIVITY, bundle); // } // });
		 * 
		 * // DialogUtil.showComfire(BookingFromNetActivity.this, // msg, //
		 * "现在您可以选择:", // "查看订单", // new Runnable() { // // @Override // public
		 * void run() { // //获得返回的订单id // String orderId = dto.getFieldName();
		 * // Bundle bundle = new Bundle(); //
		 * bundle.putString(Settings.BUNDLE_KEY_ID, orderId); //
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage); //
		 * ActivityUtil.jump(BookingFromNetActivity.this,
		 * MyOrderDetailActivity.class, Settings.BOOKING_FROM_NET_ACTIVITY,
		 * bundle); // } // }, // "返回" + leftGoBackBtn, // new Runnable() { //
		 * // @Override // public void run() { //
		 * BookingFromNetActivity.this.finish(); // } // });
		 * 
		 * } else { DialogUtil.showToast(BookingFromNetActivity.this,
		 * dto.getMsg()); } } } });
		 */
	}

	/**
	 * check及收集输入信息
	 */
	private boolean checkInput() {

//		tel = etMobilephone.getText().toString().trim();
		if (CheckUtil.isEmpty(tel)) {
			// etMobilephone.setError("请输入预订手机号");
//			ViewUtils.setError(etMobilephone, "请输入预订手机号");
//			etMobilephone.requestFocus();
			return false;
		}
		if (!isLogin) {
//			SharedprefUtil
//					.save(BookingFromNetActivity.this, Settings.ANONYMOUS_TEL, etMobilephone.getText().toString());
			SharedprefUtil.save(BookingFromNetActivity.this, Settings.ANONYMOUS_TRUE_NAME, etName.getText().toString());
		} else {
			SessionManager.getInstance().setCacheBookPhone(
					SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getUuid(), tel);
		}

		String num = etPeopleNum.getText().toString().trim();
		if (CheckUtil.isEmpty(num)) {
			// etPeopleNum.setError("请输入用餐人数");
			ViewUtils.setError(etPeopleNum, "请输入用餐人数");
			etPeopleNum.requestFocus();
			return false;
		} else {
			try {
				peopleNum = Integer.parseInt(num);
			} catch (Exception e) {
				// etPeopleNum.setError("用餐人数填写不正确");
				ViewUtils.setError(etPeopleNum, "用餐人数填写不正确");
				etPeopleNum.requestFocus();
				return false;
			}
			if (peopleNum >= 999 || peopleNum < 1) {
				// etPeopleNum.setError("用餐人数请在1到49人之间");
				ViewUtils.setError(etPeopleNum, "用餐人数请在1到1000人之间");
				etPeopleNum.requestFocus();
				return false;
			}
		}

		bookerName = etName.getText().toString().trim();
		if (CheckUtil.isEmpty(bookerName)) {
			// etName.setError("请输入订餐人姓名");
			ViewUtils.setError(etName, "请输入订餐人姓名");
			etName.requestFocus();
			return false;
		}
		remarkText = remark.getText().toString().trim();
		if (remarkText.length() > 100) {
			DialogUtil.showToast(this, "输入的备注不能超过100字符!");
			return false;
		}

		reserveTime = ConvertUtil.convertDateStringToLong(strDate + strTime, ConvertUtil.DATE_FORMAT_YYYYMMDD000000);

		bookerName = etName.getText().toString().trim();
		sexTag = (int) sexSpinner.getSelectedItemId();

		if (roomTypeTag == ROOM_TYPE_TAG_INVALID) {
			DialogUtil.showToast(this, "您还未选择您的座位要求");
			return false;
		}
		// 优惠信息验证
		selectApTag = promotion_title.isChecked();
		apId = (String) promotion_title.getTag();
		apNumber = promotion_number.getText().toString();
		if (selectApTag) {// 如果选择了优惠活动，则会员号不能为空
			if (CheckUtil.isEmpty(apNumber)) {
				DialogUtil.showToast(this, promotion_number.getHint().toString());
				return false;
			}
		}
		return true;
	}

	/**
	 * 显示对话框的逻辑
	 */
	protected void showMyDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			new MyTimePickerDialog(this, mTimeSetListener, mHour, mMinute, true).show();
			break;
		case DATE_DIALOG_ID:
			// new DatePickerDialog(this, mDateSetListener, mYear, mMonth -
			// 1, mDay).show();
			DialogUtil.showDatePickerDlg(this, mYear, mMonth, mDay, mDateSetListener);
			break;
		}
	}

	/**
	 * 日期监听器
	 */
	private OnDateSetListener mDateSetListener = new OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			// mMonth = monthOfYear + 1;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			strDate = getStringDate(mYear, mMonth, mDay);

			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			calendar.set(mYear, mMonth - 1, mDay);
			mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

			tvDineDate.setText(mYear + "-" + getAddZeroTime(mMonth) + "-" + getAddZeroTime(mDay) + " "
					+ DAT_OF_WEEK[mDayOfWeek - 1]);

			executeGetRoomStateForCanlender();
		}
	};

	/**
	 * 时间监听器
	 */
	private OnTimeSetListener mTimeSetListener = new OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			strTime = getStringTime(mHour, mMinute);
			tvDineTime.setText(getAddZeroTime(mHour) + ":" + getAddZeroTime(mMinute));

			executeGetRoomStateForCanlender();
		}
	};

	/**
	 * 格式化日期
	 */
	private static String getStringDate(int year, int monthOfYear, int dayOfMonth) {
		String strYear = String.valueOf(year);
		String strMonth = String.valueOf(monthOfYear);
		String strDay = String.valueOf(dayOfMonth);
		if (monthOfYear < 10) {
			strMonth = "0" + strMonth;
		}
		if (dayOfMonth < 10) {
			strDay = "0" + strDay;
		}
		return strYear + strMonth + strDay;
	}

	/**
	 * 格式化时间
	 */
	private static String getStringTime(int hourOfDay, int minute) {
		String strHour = String.valueOf(hourOfDay);
		String strMinuyte = String.valueOf(minute);
		String strSecond = "00";
		if (hourOfDay < 10) {
			strHour = "0" + strHour;
		}
		if (minute < 10) {
			strMinuyte = "0" + strMinuyte;
		}
		return strHour + strMinuyte + strSecond;
	}

	/**
	 * 格式化时间
	 */
	private static String getAddZeroTime(int time) {
		String strTime;
		if (time < 10) {
			strTime = "0" + time;
		} else {
			strTime = String.valueOf(time);
		}
		return strTime;
	}

	private void clearEditText() {
//		etMobilephone.setText("");
		etPeopleNum.setText("");
		etName.setText("");
	 	remark.setText("");
	}

	/**
	 * 更新餐位信息
	 */
	private void executeGetRoomStateForCanlender() {
		// --------------------------------------------------
		Calendar orderTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		orderTime.set(mYear, mMonth - 1, mDay, mHour, mMinute, 0);

		// 创建任务
		final GetRestRoomStateAndOrderInfoTask getRoomStateTask = new GetRestRoomStateAndOrderInfoTask(null, this,
				restaurantId, orderTime.getTimeInMillis());

		// 显示加载进度条
		updateRoomState(GET_ROOM_STATE_IN_PROGRESS);

		tvDineDate.setClickable(false);
		tvDineTime.setClickable(false);
		btnPost.setClickable(false);

		// 执行任务
		getRoomStateTask.execute(new Runnable() {

			@Override
			public void run() {
				try {// promotion_layout.setVisibility(View.GONE);//先不显示
					RoomState roomDto = getRoomStateTask.dto.getRoomState();
					if (roomDto == null) {
						updateRoomState(GET_ROOM_STATE_FAIL);
					} else {
						fixRoomStateTag(roomDto);
						setCalendarRoomState(roomDto);
						updateRoomState(GET_ROOM_STATE_SUCCESS);
					}
					resetRoomTypeTagIfNecessary(roomDto);
					resetPromotionInfoIfNecessary(getRoomStateTask.dto);

				} catch (Exception e) {
					setCalendarRoomState(null);
					resetRoomTypeTagIfNecessary(null);
					resetPromotionInfoIfNecessary(null);
					updateRoomState(GET_ROOM_STATE_FAIL);

				} finally {
					tvDineDate.setClickable(true);
					tvDineTime.setClickable(true);
					btnPost.setClickable(true);
				}
			}

		}, new Runnable() {

			@Override
			public void run() {
				setCalendarRoomState(null);
				resetRoomTypeTagIfNecessary(null);
				updateRoomState(GET_ROOM_STATE_FAIL);
				tvDineDate.setClickable(true);
				tvDineTime.setClickable(true);
				btnPost.setClickable(true);
			}
		});
	}

	// 显示日历进度层
	private void updateRoomState(int state) {
		if (state == GET_ROOM_STATE_SUCCESS) {
			rgGroup.setVisibility(View.VISIBLE);
			vgProgress.setVisibility(View.GONE);
			vgRetry.setVisibility(View.GONE);
			promotion_layout.setVisibility(View.VISIBLE);
			room_description.setVisibility(View.VISIBLE);
		} else if (state == GET_ROOM_STATE_IN_PROGRESS) {
			rgGroup.setVisibility(View.GONE);
			vgProgress.setVisibility(View.VISIBLE);
			vgRetry.setVisibility(View.GONE);
			promotion_layout.setVisibility(View.GONE);
			room_description.setVisibility(View.GONE);
		} else if (state == GET_ROOM_STATE_FAIL) {
			rgGroup.setVisibility(View.GONE);
			vgProgress.setVisibility(View.GONE);
			vgRetry.setVisibility(View.VISIBLE);
			promotion_layout.setVisibility(View.GONE);
			room_description.setVisibility(View.GONE);
		}
	}

	// 设置餐位状态
	private void setCalendarRoomState(RoomState roomState) {

		if (roomState == null) {
			roomState = new RoomState();
			roomState.setHallTag(Settings.REAL_TIME_DISABLE);
			roomState.setRoomTag(Settings.REAL_TIME_DISABLE);
			roomState.setHallName("大厅(无法预订)");
			roomState.setRoomName("包房(无法预订)");
		}

		String hallNameSuffix = "";
		if (!TextUtils.isEmpty(roomState.getHallNameSuffix())) {
			hallNameSuffix = roomState.getHallNameSuffix();
		}
		String roomNameSuffix = "";
		if (!TextUtils.isEmpty(roomState.getRoomNameSuffix())) {
			roomNameSuffix = roomState.getRoomNameSuffix();
		}

		// 大厅状态 1：有 2：紧张 3：已满
		// 4：不可预订------------------------------------------------
		switch (roomState.getHallTag()) {
		case Settings.REAL_TIME_ENOUGH:
			// 大厅 有
			rbHall.setText(getString(R.string.text_layout_room_request_hall));
			rbHallThenRoom.setText(getString(R.string.text_layout_room_request_hall_then_room));
			setEnabled(rbHall, true);
			break;
		case Settings.REAL_TIME_FEW:
			// 大厅 紧张
			rbHall.setText(getString(R.string.text_layout_room_request_hall) + hallNameSuffix);
			rbHallThenRoom.setText("优先订大厅" + hallNameSuffix + ",包房也可以");
			setEnabled(rbHall, true);
			break;
		case Settings.REAL_TIME_FULL:
			// 大厅 已满
			rbHall.setText(getString(R.string.text_layout_room_request_hall) + hallNameSuffix);
			rbHallThenRoom.setText("优先订大厅" + hallNameSuffix + ",包房也可以");
			setEnabled(rbHall, false);
			break;
		case Settings.REAL_TIME_DISABLE:
			// 大厅 不可预订
			rbHall.setText(getString(R.string.text_layout_room_request_hall) + hallNameSuffix);
			rbHallThenRoom.setText("优先订大厅" + hallNameSuffix + ",包房也可以");
			setEnabled(rbHall, false);
			break;
		}

		// 包房状态 1：有 2：紧张 3：已满
		// 4：不可预订------------------------------------------------
		switch (roomState.getRoomTag()) {
		case Settings.REAL_TIME_ENOUGH:
			// 包房 有
			rbRoom.setText(getString(R.string.text_layout_room_request_room));
			rbRoomThenHall.setText(getString(R.string.text_layout_room_request_room_then_hall));
			setEnabled(rbRoom, true);
			break;
		case Settings.REAL_TIME_FEW:
			// 包房 紧张
			rbRoom.setText(getString(R.string.text_layout_room_request_room) + roomNameSuffix);
			rbRoomThenHall.setText("优先订包房" + roomNameSuffix + ",大厅也可以");
			setEnabled(rbRoom, true);
			break;
		case Settings.REAL_TIME_FULL:
			// 包房 已满
			rbRoom.setText(getString(R.string.text_layout_room_request_room) + roomNameSuffix);
			rbRoomThenHall.setText("优先订包房" + roomNameSuffix + ",大厅也可以");
			setEnabled(rbRoom, false);
			break;
		case Settings.REAL_TIME_DISABLE:
			// 包房 不可预订
			rbRoom.setText(getString(R.string.text_layout_room_request_room) + roomNameSuffix);
			rbRoomThenHall.setText("优先订包房" + roomNameSuffix + ",大厅也可以");
			setEnabled(rbRoom, false);
			break;
		}

		// 大厅或包房有一个不可预订，则最后两个选项都不可选
		setEnabled(rbHallThenRoom, true);
		setEnabled(rbRoomThenHall, true);
		if (roomState.getHallTag() == Settings.REAL_TIME_FULL || roomState.getHallTag() == Settings.REAL_TIME_DISABLE
				|| roomState.getRoomTag() == Settings.REAL_TIME_FULL
				|| roomState.getRoomTag() == Settings.REAL_TIME_DISABLE) {
			setEnabled(rbHallThenRoom, false);
			setEnabled(rbRoomThenHall, false);
		}
	}

	private void resetRoomTypeTagIfNecessary(RoomState roomState) {
		if (roomState == null) {
			rgGroup.clearCheck();
			return;
		}
		if (roomState.getHallTag() == Settings.REAL_TIME_FULL || roomState.getHallTag() == Settings.REAL_TIME_DISABLE) {
			if (rbHall.isChecked() || rbHallThenRoom.isChecked()) {
				rgGroup.clearCheck();
			}
		}
		if (roomState.getRoomTag() == Settings.REAL_TIME_FULL || roomState.getRoomTag() == Settings.REAL_TIME_DISABLE) {
			if (rbRoom.isChecked() || rbRoomThenHall.isChecked()) {
				rgGroup.clearCheck();
			}
		}

	}

	// 设置优惠信息
	private void resetPromotionInfoIfNecessary(RoomStateAndOrderInfo dto) {

		if (dto != null && dto.isHaveRoomHintTag()) {
			room_description.setVisibility(View.VISIBLE);
			room_description.setText(dto.getRoomHint());
		} else {
			room_description.setVisibility(View.GONE);
			room_description.setText("");
		}

		// 不能参加活动
		if (dto == null || !dto.isCanJoinAirPlanePromotionTag()) {
			promotion_layout.setVisibility(View.GONE);
			promotion_title.setChecked(false);
			promotion_description.setText("");
			promotion_number.setText("");
			return;
		}
		// --
		// 可以参加活动
		promotion_layout.setVisibility(View.VISIBLE);
		promotion_title.setText(dto.getApTitle());
		promotion_title.setTag(dto.getApId());
		promotion_description.setText(dto.getApDetail());
		promotion_title.setChecked(false);// 默认不选中
		promotion_number.setVisibility(View.GONE);
		promotion_number.setText("");// 清空
		promotion_number.setHint(dto.getApPlaceHolder());// 提示

	}

	private void setEnabled(RadioButton button, boolean enabled) {
		button.setClickable(enabled);
		button.setEnabled(enabled);
		if (enabled) {
			button.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.my_radio_box), null);
		} else {
			button.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.radio_box_bt03), null);
			ViewUtils.setStrikethrough(button);
		}
		button.setCompoundDrawablePadding(DRAWABLE_PADDING);
	}

	private void fixRoomStateTag(RoomState dto) {
		if (dto.getHallTag() != Settings.REAL_TIME_ENOUGH && dto.getHallTag() != Settings.REAL_TIME_FEW
				&& dto.getHallTag() != Settings.REAL_TIME_FULL && dto.getHallTag() != Settings.REAL_TIME_DISABLE) {
			dto.setHallTag(Settings.REAL_TIME_DISABLE);
		}
		if (dto.getRoomTag() != Settings.REAL_TIME_ENOUGH && dto.getRoomTag() != Settings.REAL_TIME_FEW
				&& dto.getRoomTag() != Settings.REAL_TIME_FULL && dto.getRoomTag() != Settings.REAL_TIME_DISABLE) {
			dto.setRoomTag(Settings.REAL_TIME_DISABLE);
		}
	}
}
