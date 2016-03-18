package com.fg114.main.app.activity.usercenter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;

import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.Mdb.MdbResListActivity;
import com.fg114.main.app.activity.order.FastBookingActivity;
import com.fg114.main.app.activity.order.MdbOrderListActivity;
import com.fg114.main.app.activity.order.MyShortMessageOrderListActivity;
import com.fg114.main.app.activity.order.NewOrderListAcitivy;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity.TakeAwayAdvertisementAdapter;
import com.fg114.main.app.adapter.AdvertisementImgAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.view.CircleFlowIndicator;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.ViewFlow;
import com.fg114.main.service.dto.BubbleHintData;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MainPageInfoPackDTO;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.ResFoodData3;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserCenterInfoDTO;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.AddDebugAccountTask;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.service.task.VerifyTestUserTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.TrackTool;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.xiaomishu.extension.baidu.push.PushMessageReceiver;

/**
 * 用户中心
 * 
 * @author lijian
 * 
 */
public class UserCenterActivity extends MainFrameActivity {

	private static final String TAG = "UserCenterActivity";

	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 80; // 图片压缩率
	// 广告组件
	private ViewFlow advViewFlow;
	private CircleFlowIndicator advCircleIndicator;
	private View top_view;
	// private ImageView advCloseButton;
	private Thread playAdvertisement;
	private volatile long playCoolingTime; // 自动播放广告的冷却时间，当被touch时，设置一个未来时间，在此冷却时间前，广告不会自动播放。
	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private MyImageView userImg;// 用户的图片
	private TextView userName;// 用户姓名
	// private TextView userAuthority;// 用户级别
	private View order_mail_layout;// 用户邮件布局
	private TextView userMailNumber;// 用户信息的数量
	private LinearLayout userOrderNumberLayout;// 用户订单数量布局
	private TextView userOrderNumber;// 用户信息的数量
	// private TextView myMainPageNumber;// 推荐的评论数量
	private Button userLoginBnt;// 用户登录按钮
	private View userMessageModify;// 用户修改信息按钮
	private RelativeLayout myOrder;// 我的订单按钮
	private RelativeLayout myCash;// 现金卷
	private RelativeLayout myIntergral;// 积分兑换
	private RelativeLayout myMenu;// 我的菜单
	private RelativeLayout myCollect;// 我的收藏
	private RelativeLayout myBrowsingHistory;// 浏览历史
	private RelativeLayout my_mdb;// 免单宝
	private View my_mibi;// 秘币
	private View my_balance;// 余额
	private View fast_book_layout;// 快捷预定
	private View my_reward;// 今日奖励
	private View my_comment;// 我的点评
	private View my_photo;// 我的相册
	private View my_recommand;// 我的推荐
	private TextView user_hint;
	// private RelativeLayout userLayoutMessage;// 用户信息框
	// private RelativeLayout myMainPage;// 我的主页
	private ImageButton userLevel;// 用户级别
	private UserInfoDTO userInfo2DTO;// 用户信息对象
	private ProgressBar progress_bar;
	private ImageView myuser_arrow;
	private TextView balance_num;
	private TextView mibi_num;
	private TextView order_mail_num;
	private TextView recom_comment_number;
	private TextView today_reward_number;
	private TextView takeout_order_number;

	private View my_takeaway;
	private View my_groupbuy;
	private View registration_activities;
	// private RelativeLayout my_takeout_order;// 外卖订单按钮
	// private RelativeLayout collect_cash;// 收藏餐厅按钮
	// private TextView user_takeout_order_number;// 外卖订单数量

	private List<MainPageAdvData> advList;

	private Button bntTest;

	private boolean hasLogined;// 判断是否登录

	private boolean needHideBackButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("用户中心", "");
		// ----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			needHideBackButton = bundle.getBoolean(Settings.BUNDLE_KEY_NEED_HIDE_BACK_BUTTON, false);
		}
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
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("用户中心", "");
		// ----------------------------
		// ----------------------------
		// OpenPageDataTracer.getInstance().enterPage("用户中心", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {

		super.onResume();
		// Fg114Application.isNeedUpdate = true;
		getUserCenterInfo();

	}

	// -----------------获得用户中心信息-------------------
	/*
	 * 获得用户中心信息，返回UserCenterInfoDTO 并且JsonPack中//needUpdateUserInfoTag=true
	 * //userInfo=用户信息 getUserCenterInfo("/getUserCenterInfo", new
	 * ParamProtocol() ),
	 */
	// ------------------------------------
	private void getUserCenterInfo() {
		ServiceRequest request = new ServiceRequest(API.getUserCenterInfo2);
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		CommonTask.requestMutely(request, new CommonTask.TaskListener<UserCenterInfoDTO>() {

			@Override
			protected void onSuccess(UserCenterInfoDTO dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				MainPageOtherInfoPackDTO mainPageInfoPackDTO = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
				mainPageInfoPackDTO.bubbleHintData = dto.bubbleHintData;
				SessionManager.getInstance().setMainPageOtherInfoPackDTO(mainPageInfoPackDTO);
				CommonObservable.getInstance().notifyObservers(CommonObserver.SystemMessageObserver.class);

				initCityIssues();
				refreshUI();

				// 显示广告
				if (dto.advList != null && dto.advList.size() != 0) {
					top_view.setVisibility(View.VISIBLE);
					advList = dto.advList;
					tryDisplayAdvertisement();
				} else {
					top_view.setVisibility(View.GONE);
				}
			};

			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
			};
		});
	}

	private void refreshUI() {
		userInfo2DTO = SessionManager.getInstance().getUserInfo(UserCenterActivity.this);
		initDate();
	}

	int mailNum = 0;
	int orderNum = 0;
	int recomCommentNum = 0;
	int takeoutOrderNum = 0;
	int todayRewardNum = 0;

	private void initDate() {
		// ------------首页广告-----------------------------------------------------------------------
		advViewFlow.setFlowIndicator(advCircleIndicator);

		// 判断是否已登录并调整界面显示
		hasLogined = SessionManager.getInstance().isUserLogin(this);
		MainPageOtherInfoPackDTO mainPageInfoPackDTO = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
		BubbleHintData bubbleHintData = mainPageInfoPackDTO.bubbleHintData;
		if (bubbleHintData != null) {
			mailNum = bubbleHintData.mailNum;
			orderNum = bubbleHintData.orderNum;
			recomCommentNum = bubbleHintData.recomCommentNum;
			takeoutOrderNum = bubbleHintData.takeoutOrderNum;
			todayRewardNum = bubbleHintData.todayRewardNum;
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (hasLogined) {
					// order_mail_layout.setVisibility(View.VISIBLE);
					userLoginBnt.setVisibility(View.GONE);
					// order_mail_layout.setClickable(true);
					userMessageModify.setClickable(true);
					progress_bar.setVisibility(View.VISIBLE);
					myuser_arrow.setVisibility(View.VISIBLE);
					// userLayoutMessage.setVisibility(View.VISIBLE);
					userName.setVisibility(View.VISIBLE);
					userName.setText(userInfo2DTO.getNickName());
					userImg.setEnabled(true);

					balance_num.setText(userInfo2DTO.getRemainMoney() + "");
					mibi_num.setText(userInfo2DTO.getPointNum() + "");
					order_mail_num.setText(userInfo2DTO.totalMailNum + "");
					progress_bar.setProgress(userInfo2DTO.getNextLevelPct());
					user_hint.setText(userInfo2DTO.getNextLevelHint());
					if (getBackGroundLevel(userInfo2DTO) == 0) {
						userLevel.setVisibility(View.INVISIBLE);
						userLevel.setBackgroundColor(0x00000000);
					} else {
						userLevel.setVisibility(View.VISIBLE);
						userLevel.setBackgroundResource(getBackGroundLevel(userInfo2DTO));
					}
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							userImg.setImageByUrl(userInfo2DTO.getPicUrl(), true, 0, ScaleType.FIT_XY);
						}
					}, 100);
					// 上传图片
					userImg.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							ViewUtils.preventViewMultipleClick(v, 1000);
							uploadUserPicture();
						}
					});

				} else {
					balance_num.setText("0");
					mibi_num.setText("0");
					order_mail_num.setText("0");

					user_hint.setText("登录后可查看个人信息，获得秘币奖励，本地数据云端同步等。");
					// order_mail_layout.setClickable(false);
					userName.setVisibility(View.GONE);
					userLevel.setVisibility(View.GONE);
					progress_bar.setVisibility(View.GONE);
					myuser_arrow.setVisibility(View.GONE);
					userMessageModify.setClickable(false);
					// userMessageModify.setVisibility(View.GONE);
					userLoginBnt.setVisibility(View.VISIBLE);
					userImg.setImageResource(R.drawable.usercenter);
					// userLayoutMessage.setVisibility(View.INVISIBLE);
					userImg.setEnabled(false);
					userImg.setClickable(false);
				}

				if (mailNum > 0) {
					userMailNumber.setText(mailNum + "");
					userMailNumber.setVisibility(View.VISIBLE);
				} else {
					userMailNumber.setVisibility(View.GONE);
				}
				// ---
				// int orderNumTotal = 0;
				// orderNumTotal = orderNum + takeoutOrderNum;
				if (orderNum > 0) {
					userOrderNumber.setText(orderNum + "");
					userOrderNumber.setVisibility(View.VISIBLE);
				} else {
					userOrderNumber.setVisibility(View.GONE);
				}
				if (takeoutOrderNum > 0) {
					takeout_order_number.setText(takeoutOrderNum + "");
					takeout_order_number.setVisibility(View.VISIBLE);
				} else {
					takeout_order_number.setVisibility(View.GONE);
				}

				if (recomCommentNum > 0) {
					recom_comment_number.setText(recomCommentNum + "");
					recom_comment_number.setVisibility(View.VISIBLE);
				} else {
					recom_comment_number.setVisibility(View.GONE);
				}
				if (todayRewardNum > 0) {
					// recom_comment_number.setText(recomCommentNum + "");
					today_reward_number.setVisibility(View.VISIBLE);
				} else {
					today_reward_number.setVisibility(View.GONE);
				}
				// if (recomCommentNum > 0) {
				// myMainPageNumber.setText(recomCommentNum + "");
				// myMainPageNumber.setVisibility(View.VISIBLE);
				// } else {
				// myMainPageNumber.setVisibility(View.GONE);
				// }
				// if (takeoutOrderNum > 0) {
				// user_takeout_order_number.setText(takeoutOrderNum + "");
				// user_takeout_order_number.setVisibility(View.VISIBLE);
				// } else {
				// user_takeout_order_number.setVisibility(View.GONE);
				// }

				// if (orderNum > 0) {
				// user_takeout_order_number.setText(orderNum + "");
				// user_takeout_order_number.setVisibility(View.VISIBLE);
				// } else {
				// user_takeout_order_number.setVisibility(View.GONE);
				// }
			}
		}, 200);
	}

	@Override
	protected void updateSystemMessage() {
		super.updateSystemMessage();
		// refreshUI();
	}

	/**
	 * 上传图片
	 * 
	 * @param imageView
	 */
	public void uploadUserPicture() {

		takePic(new OnShowUploadImageListener() {

			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				if (!CheckUtil.isEmpty(path)) {
					com.fg114.main.app.Settings.uploadPictureUri = "";
					executeUploadUserPictureTask(path);
				}
			}

		}, false);
	}

	private RelativeLayout add_rest;
	private Button testButton;
	private FileInputStream input = null;

	private void executeUploadUserPictureTask(final String path) {
		ServiceRequest request = new ServiceRequest(API.uploadUserPic);
		try {
			String tempPath = ActivityUtil.getGPSPicturePath(path);
			input = new FileInputStream(tempPath);
			request.addData(input);
		} catch (IOException e) {
			DialogUtil.showToast(UserCenterActivity.this, "请稍后再次尝试");
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
		// -----
		OpenPageDataTracer.getInstance().addEvent("头像按钮");
		// -----
		CommonTask.request(request, "正在上传...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				OpenPageDataTracer.getInstance().endEvent("头像按钮");
				// -----
				SessionManager.getInstance().setIsUserLogin(UserCenterActivity.this, true);
				getUserCenterInfo();
				// userImg.setImageByUrl(userInfo2DTO.getPicUrl(), true, 0,
				// ScaleType.CENTER_CROP);
				// userImg.postInvalidate();
				// DialogUtil.showToast(UserCenterActivity.this,
				// userInfo2DTO.getPicUrl());
				// TODO
				// UserCenterActivity.

			};

			protected void onError(int code, String message) {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				OpenPageDataTracer.getInstance().endEvent("头像按钮");
				// -----
				DialogUtil.showToast(UserCenterActivity.this, message);
			};
		});
	}

	// 获取位图
	private Bitmap getBitmap(String path) {

		ContentResolver contentResolver = this.getContentResolver();
		InputStream picStream = null; // 图片流
		if (path == null || path.equals("")) {
			DialogUtil.showToast(this, "图片路径为空!");
			return null;
		}
		try {
			picStream = new FileInputStream(path);
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			if (picStream.available() == 0) {
				DialogUtil.showToast(this, "图片数据无效");
				return null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取图片的宽和高
			BitmapFactory.decodeStream(picStream, null, options); // 此时返回bm为空
			options.inJustDecodeBounds = false;

			if (options.outWidth < 0 || options.outHeight < 0) {
				// 选中的是非图像文件
				DialogUtil.showToast(getBaseContext(), "图片格式无效");
				return null;
			}

			// 计算缩放比
			int scale = 1;
			if (options.outWidth > IMAGE_SIZE || options.outHeight > IMAGE_SIZE) {
				if (options.outWidth >= options.outHeight) {
					scale = (int) Math.ceil(options.outWidth / (float) IMAGE_SIZE);
				} else {
					scale = (int) Math.ceil(options.outHeight / (float) IMAGE_SIZE);
				}
			}
			options.inSampleSize = scale;
			picStream.close();
			picStream = new FileInputStream(path);
			Bitmap bmp = BitmapFactory.decodeStream(picStream, null, options);
			return bmp;
		} catch (Exception e) {
			DialogUtil.showToast(this, getString(R.string.text_info_upload_cant_show));
			e.printStackTrace();
			return null;
		} finally {
			if (picStream != null) {
				try {
					picStream.close();
				} catch (Exception e) {
					DialogUtil.showToast(this, e.getMessage());
				}

			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (playAdvertisement != null) {
			playAdvertisement.interrupt();
		}
	}

	private int getBackGroundLevel(UserInfoDTO infoDTO) {
		int res = 0;
		// 用户级别 1:普通会员 2：白银会员 3：黄金会员 4：铂金会员 5:钻石会员
		switch (infoDTO.getLevel()) {
		case 1:
			res = R.drawable.common;
			break;
		case 2:
			res = R.drawable.silver;
			break;
		case 3:
			res = R.drawable.gold;
			break;
		case 4:
			res = R.drawable.platinum;
			break;
		case 5:
			res = R.drawable.diamond;
			break;

		default:
			break;

		}
		return res;
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("我的");
		this.getBtnGoBack().setText("返回");
		// this.getBtnOption().setBackgroundResource(R.drawable.setting_pic);
		this.getBtnOption().setVisibility(View.VISIBLE);

		Drawable drawable = getResources().getDrawable(R.drawable.setting_pic1);
		this.getBtnOption().setBackgroundDrawable(drawable);
		// this.getBtnOption().setCompoundDrawablesWithIntrinsicBounds(drawable,
		// null, null, null);
		this.getBtnOption().setText("");
		if (needHideBackButton) {
			this.getBtnGoBack().setVisibility(View.INVISIBLE);
		}

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.new_user_center, null);
		userImg = (MyImageView) contextView.findViewById(R.id.user_img);
		userName = (TextView) contextView.findViewById(R.id.user_name);
		// userAuthority = (TextView)
		// contextView.findViewById(R.id.user_authority);
		order_mail_layout = (View) contextView.findViewById(R.id.order_mail_layout);
		userMailNumber = (TextView) contextView.findViewById(R.id.user_mail_number);
		userLoginBnt = (Button) contextView.findViewById(R.id.user_login_bnt);
		userMessageModify = (View) contextView.findViewById(R.id.user_message_modify);
		myOrder = (RelativeLayout) contextView.findViewById(R.id.my_order);
		userOrderNumber = (TextView) contextView.findViewById(R.id.user_order_number);

		my_takeaway = (View) contextView.findViewById(R.id.my_takeaway);
		my_groupbuy = (View) contextView.findViewById(R.id.my_groupbuy);
		// myMainPageNumber = (TextView)
		// contextView.findViewById(R.id.my_main_page_number);
		userOrderNumberLayout = (LinearLayout) contextView.findViewById(R.id.user_order_number_layout);
		registration_activities = (View) contextView.findViewById(R.id.registration_activities);
		myCash = (RelativeLayout) contextView.findViewById(R.id.my_cash);
		myIntergral = (RelativeLayout) contextView.findViewById(R.id.my_intergral);
		myMenu = (RelativeLayout) contextView.findViewById(R.id.my_menu);
		myCollect = (RelativeLayout) contextView.findViewById(R.id.my_collect);
		myBrowsingHistory = (RelativeLayout) contextView.findViewById(R.id.my_browsing_history);
		add_rest = (RelativeLayout) contextView.findViewById(R.id.add_rest);
		myuser_arrow = (ImageView) contextView.findViewById(R.id.myuser_arrow);
		progress_bar = (ProgressBar) contextView.findViewById(R.id.progress_bar);
		my_mibi = (View) contextView.findViewById(R.id.my_mibi);
		my_balance = (View) contextView.findViewById(R.id.my_balance);
		fast_book_layout = (View) contextView.findViewById(R.id.fast_book_layout);
		my_reward = (View) contextView.findViewById(R.id.my_reward);
		my_comment = (View) contextView.findViewById(R.id.my_comment);
		my_photo = (View) contextView.findViewById(R.id.my_photo);
		user_hint = (TextView) contextView.findViewById(R.id.user_hint);
		balance_num = (TextView) contextView.findViewById(R.id.balance_num);
		mibi_num = (TextView) contextView.findViewById(R.id.mibi_num);
		order_mail_num = (TextView) contextView.findViewById(R.id.order_mail_num);
		my_recommand = (View) contextView.findViewById(R.id.my_recommand);
		recom_comment_number = (TextView) contextView.findViewById(R.id.recom_comment_number);
		today_reward_number = (TextView) contextView.findViewById(R.id.today_reward_number);
		takeout_order_number = (TextView) contextView.findViewById(R.id.takeout_order_number);
		my_mdb = (RelativeLayout) contextView.findViewById(R.id.my_mdb);
		// myMainPage = (RelativeLayout)
		// contextView.findViewById(R.id.my_main_page);

		// my_takeout_order = (RelativeLayout)
		// contextView.findViewById(R.id.my_takeout_order);
		// collect_cash = (RelativeLayout)
		// contextView.findViewById(R.id.collect_cash);
		// user_takeout_order_number = (TextView)
		// contextView.findViewById(R.id.user_takeout_order_number);

		bntTest = (Button) contextView.findViewById(R.id.bntTest);

		// userLayoutMessage = (RelativeLayout)
		// contextView.findViewById(R.id.user_layout_message);
		userLevel = (ImageButton) contextView.findViewById(R.id.user_level);

		testButton = (Button) contextView.findViewById(R.id.testButton);

		advViewFlow = (ViewFlow) contextView.findViewById(R.id.viewflow);
		advCircleIndicator = (CircleFlowIndicator) contextView.findViewById(R.id.circle_indicator);
		top_view = (View) contextView.findViewById(R.id.top_view);

		userMailNumber.setVisibility(View.GONE);
		userOrderNumber.setVisibility(View.GONE);
		takeout_order_number.setVisibility(View.GONE);
		// myMainPageNumber.setVisibility(View.GONE);
		// order_mail_layout.setVisibility(View.GONE);
		userLoginBnt.setVisibility(View.GONE);
		recom_comment_number.setVisibility(View.GONE);
		// user_takeout_order_number.setVisibility(View.GONE);

		userLevel.setVisibility(View.INVISIBLE);
		userMessageModify.setClickable(false);
		// userMessageModify.setVisibility(View.GONE);
		userName.setText("");

		// 点击调往APP设置
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("系统设置按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(UserCenterActivity.this, APPSettingActivity.class, 0, bundle);
			}
		});

		// 点击调往账户设置中心
		userMessageModify.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("账户设置按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(UserCenterActivity.this, UserAccessSettingActivity.class, 0, bundle);
			}
		});

		// 调往登录界面
		userLoginBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("登录按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(UserCenterActivity.this, UserLoginActivity.class, 0, bundle);
			}
		});

		// 餐厅订单
		myOrder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅订座按钮");
				// -----
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_FROM_TAG, 0);
				ActivityUtil.jump(UserCenterActivity.this, NewOrderListAcitivy.class, 0, bundle);
			}
		});
		// 我的外卖
		my_takeaway.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("外卖按钮");
				// -----
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_FROM_TAG, 1);
				ActivityUtil.jump(UserCenterActivity.this, NewOrderListAcitivy.class, 0, bundle);
			}
		});

		// 我的团购
		my_groupbuy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("团购券按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("团购券");
				}
			}
		});

		registration_activities.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// TODO Auto-generated method stub
				if (checkLogin()) {
					jumpToWeb("报名活动");
				}
			}
		});

		// 现金卷
		myCash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("现金券按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("现金券");
				}
			}
		});

		// 秘币兑换
		myIntergral.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("秘币兑换按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("秘币兑换");
				}
			}
		});

		// 我的菜单
		myMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我的菜单按钮");
				// -----
				jumpToWeb("我的菜单");
			}
		});
		// 消息
		order_mail_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("消息按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("站内信");
				}
			}
		});
		// 秘币
		my_mibi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("秘币按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("秘币记录");
				}
			}
		});
		// 快捷预定
		fast_book_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("快捷预订按钮");
				// -----
				ActivityUtil.jump(UserCenterActivity.this, FastBookingActivity.class, 0, new Bundle());
			}
		});

		// 今日奖励
		my_reward.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("今日奖励按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("今日奖励");
				}
			}
		});
		// 我的相册
		my_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我的相册按钮");
				// -----
				jumpToWeb("我的相册");
			}
		});
		// 我的推荐
		my_recommand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我的推荐按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("我的推荐");
				}
			}
		});
		// 我的点评
		my_comment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我的点评按钮");
				// -----
				jumpToWeb("我的点评");
			}
		});

		// 余额
		my_balance.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("余额按钮");
				// -----
				if (checkLogin()) {
					jumpToWeb("余额");
				}
			}
		});
		// 我的收藏
		myCollect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我的收藏按钮");
				// -----
				jumpToWeb("我的收藏");
			}
		});

		// 浏览历史
		myBrowsingHistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("浏览历史按钮");
				// -----
				jumpToWeb("浏览历史");
			}
		});
		// 添加餐厅
		add_rest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("添加餐厅按钮");
				// -----
				ActivityUtil.jumpNotForResult(UserCenterActivity.this, AddOrUpdateResActivity.class, new Bundle(), false);
			}
		});

		// 免单宝
		my_mdb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				ActivityUtil.jump(UserCenterActivity.this, MdbOrderListActivity.class, 0, new Bundle());
			}
		});

		// // 我的主页
		// myMainPage.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// // -----
		// OpenPageDataTracer.getInstance().addEvent("我的主页");
		// // -----
		// if (checkLogin()) {
		// jumpToWeb("我的主页");
		// }
		// }
		// });

		// // 外卖订单
		// my_takeout_order.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// // -----
		// OpenPageDataTracer.getInstance().addEvent("外卖订单");
		// // -----
		// Bundle bundle = new Bundle();
		// bundle.putInt(Settings.BUNDLE_FROM_TAG, 1);
		// ActivityUtil.jumpNotForResult(UserCenterActivity.this,
		// OrderListActivity.class, bundle, false);
		// }
		// });

		// // 收藏餐厅
		// collect_cash.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// // -----
		// OpenPageDataTracer.getInstance().addEvent("外卖收藏");
		// // -----
		// // if (checkLogin()) {
		// jumpToWeb("收藏餐厅");
		// // }
		// }
		// });

		advViewFlow.setAdapter(new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>()));

		// ---临时跳转的按钮
		bntTest.setVisibility(View.GONE);
		bntTest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.jumpToWeb("http://15f.78shequ.com/b/m/app_wap/home_recommend.asp", "");
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// 测试机专用
		// *****************************************************************************************
		// ***************************************************************************************************
		// 验证测试入口，长按4秒
		this.getTvTitle().setOnTouchListener(new OnTouchListener() {
			long timestamp = 0;

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					timestamp = SystemClock.elapsedRealtime();
					return false;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (SystemClock.elapsedRealtime() - timestamp > 4000) {
						showVerifyTesterDialog();
					}
				}

				return false;
			}

		});
		// ---
		setTestButton();
		// -----
		testButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("http://t")) {
					// 测试切换到正式
					A57HttpApiV3.getInstance().mApiBaseUrl = A57HttpApiV3.getInstance().mApiBaseUrl.replace("http://t", "http://");
					testButton.setText("当前为正式");
				} else {
					// 正式切换到测试
					A57HttpApiV3.getInstance().mApiBaseUrl = A57HttpApiV3.getInstance().mApiBaseUrl.replace("http://", "http://t");
					testButton.setText("当前为测试");
				}
			}
		});

		// 点击标题7次显示渠道ID，用于测试
		this.getTvTitle().setOnClickListener(new View.OnClickListener() {

			private int clickcount;

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				clickcount++;
				if (clickcount >= 7) {
					clickcount = 0;
					String info = com.fg114.main.app.Settings.SELL_CHANNEL_NUM;
					if (!CheckUtil.isEmpty(TrackTool.getUnicomChannel(info))) {
						info += ",unicom=" + TrackTool.getUnicomChannel(info);
					}
					if (!CheckUtil.isEmpty(TrackTool.getDomobChannel(info))) {
						info += ",domob=" + TrackTool.getDomobChannel(info);
					}
					DialogUtil.showToast(UserCenterActivity.this, info);
				}
			}
		});
	}

	// ---
	private void jumpToWeb(String where) {
		// 现金券：/member/cashcoupon
		// 站内信：/member/inbox
		// 我的菜单：/member/dishmenu
		// 我的收藏：/member/favorites
		// 浏览历史：/member/browse
		// 我的积分：/coin

		String path = "member/browse";
		String title = "浏览历史";
		NameValuePair pair = null;

		if ("现金券".equals(where)) {
			path = "member/cashcoupon";
			title = "现金券";

		} else if ("站内信".equals(where)) {
			path = "member/inbox";
			title = "站内信";

		} else if ("我的菜单".equals(where)) {
			path = "member/dishmenu";
			title = "我的菜单";

		} else if ("我的收藏".equals(where)) {
			path = "member/favorites";
			title = "我的收藏";

		} else if ("浏览历史".equals(where)) {
			path = "member/browse";
			title = "浏览历史";

		} else if ("我的积分".equals(where)) {
			path = "coin";
			title = "我的积分";
		} else if ("我的主页".equals(where)) {
			path = "member/index";
			title = userInfo2DTO.getNickName() + "的主页";
			pair = new BasicNameValuePair("userId", userInfo2DTO.getUuid());
		} else if ("收藏餐厅".equals(where)) {
			path = "member/takeoutfavorites";
			title = "收藏餐厅";
		} else if ("秘币兑换".equals(where)) {
			path = "award";
			title = "秘币兑换";
		} else if ("秘币记录".equals(where)) {
			path = "member/coinlist";
			title = "秘币记录";
		} else if ("余额".equals(where)) {
			path = "member/remainmoney";
			title = "余额";
		} else if ("今日奖励".equals(where)) {
			path = "member/todayreward";
			title = "今日奖励";
		} else if ("我的点评".equals(where)) {
			path = "member/comments";
			title = "我的点评";
		} else if ("我的相册".equals(where)) {
			path = "member/photos";
			title = "我的相册";
		} else if ("我的推荐".equals(where)) {
			path = "member/resrecommend";
			title = userInfo2DTO.getNickName() + "的主页";
			pair = new BasicNameValuePair("userId", userInfo2DTO.getUuid());
		} else if ("团购券".equals(where)) {
			path = "member/couponGroup";
			title = "团购券";
		} else if ("报名活动".equals(where)) {
			path = "member/Active";
			title = "报名活动";

		}
		if (pair != null) {
			ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title, pair);
		} else {
			ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title);
		}
	}

	private boolean checkLogin() {
		if (hasLogined) {
			return true;
		} else {
			DialogUtil.showToast(UserCenterActivity.this, "您还未登录，请先登录!");
			return false;
		}
	}

	// 验证测试机身份
	private void showVerifyTesterDialog() {
		final TextView title = new TextView(this);
		title.setText("验证并切换模式");
		title.setTextColor(0xFFFFFFFF);
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		title.setBackgroundColor(0x99000000);
		title.setGravity(Gravity.CENTER);
		title.setPadding(20, 20, 20, 20);
		// --
		final EditText name = new EditText(this);
		name.setHint("Dname");
		final EditText pass = new EditText(this);
		pass.setHint("Dpassword");
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(title);
		layout.addView(name);
		layout.addView(pass);
		// 创建提示框
		Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setView(layout);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				try {
					String sname = name.getText().toString().trim();
					String spass = pass.getText().toString().trim();
					if (CheckUtil.isEmpty(spass)) {
						DialogUtil.showToast(getApplicationContext(), "密码不能为空！");
						return;
					}
					new VerifyTestUserTask("正在提交，请稍候...", UserCenterActivity.this, sname, spass).execute(new Runnable() {

						@Override
						public void run() {
							ActivityUtil.setIsTestDev(true);
							setTestButton();
							DialogUtil.showToast(getApplicationContext(), "验证成功成功！");
							dialog.dismiss();
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	void setTestButton() {
		if (ActivityUtil.isTestDev(this)) {
			testButton.setVisibility(View.VISIBLE);
		} else {
			testButton.setVisibility(View.INVISIBLE);
		}
		// -----
		if (A57HttpApiV3.getInstance().mApiBaseUrl.toLowerCase().startsWith("http://t")) {
			testButton.setText("当前为测试");
		} else {
			testButton.setText("当前为正式");
		}
	}

	private synchronized void tryDisplayAdvertisement() {
		// 如果有广告则需要显示广告
		if (advList != null && advList.size() > 0) {
			if (advList.size() == 1) {
				advCircleIndicator.setVisibility(View.GONE);
			} else {
				advCircleIndicator.setVisibility(View.VISIBLE);
			}

			// 确保只有一个运行的线程
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			// ---------------------
			advViewFlow.setAdapter(new AdvertisementImgAdapter(this, advList));
			// 广告自动滚动的线程，４秒
			playAdvertisement = new Thread(new Runnable() {
				int i = 0;

				@Override
				public void run() {
					try {
						int count = advViewFlow.getAdapter().getCount();
						while (count > 1) {
							Thread.sleep(4000);
							if (playCoolingTime > System.currentTimeMillis()) {
								continue;
							}
							i = advViewFlow.getSelectedItemPosition();
							i = (i + 1) % count;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									advViewFlow.setSelection(i);

								}
							});
							count = advViewFlow.getAdapter().getCount();
						}
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			});
			playAdvertisement.start();
			// 广告手动滑动
			advViewFlow.setOnTouchListener(advTouchListener);
			// --
		} else {
			// 没有广告时，撤消线程，清除数据
			if (playAdvertisement != null) {
				playAdvertisement.interrupt();
			}
			BaseAdapter adapter = new AdvertisementImgAdapter(this, new ArrayList<MainPageAdvData>());
			advViewFlow.setAdapter(adapter);
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
}
