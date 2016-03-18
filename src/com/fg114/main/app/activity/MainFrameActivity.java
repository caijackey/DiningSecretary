package com.fg114.main.app.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.order.MyNewTakeAwayOrderDetailActivity;
import com.fg114.main.app.activity.order.NewMyOrderDetailActivity;
import com.fg114.main.app.activity.order.NewOrderListAcitivy;
import com.fg114.main.app.activity.resandfood.RestaurantUploadActivity;
import com.fg114.main.app.activity.usercenter.ShareToWeiXinActivity;
import com.fg114.main.app.activity.usercenter.ShareToWeiboActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.MainMenuListInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocBaidu;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.PushMsgDTO;
import com.fg114.main.service.dto.RestFoodData;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.CommonObserver.ReturnToActivityFinishedObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.DialogUtil.DialogEventListener;
import com.fg114.main.util.IOUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboContentBuilder;
import com.xiaomishu.extension.baidu.push.PushMessageReceiver;

/**
 * 框架页
 * 
 * @author zhangyifan
 * 
 */
public class MainFrameActivity extends ActivityGroup {
	private static Class<? extends Activity> lastActivityClass = null;
	private static Activity currentActivity = null;
	/*
	 * 当框架页面中底部显示拨打5757按钮，并且在内容页面中有edittext时， 该标志表示在框架页面设置滚动view，而不是在原页面添加滚动view
	 */
	public static final int EDITTEXTFLAG = 1;
	/* 该标志是默认值，在原页面中自己设置滚动view */
	public static final int NOEDITTEXTFLAG = 2;

	protected static final boolean DEBUG = Settings.DEBUG;

	protected Bundle bundleData = new Bundle();

	// 功能组件
	private Button btnGoBack;
	private TextView tvTitle;
	private Button btnOption;
	private LinearLayout mainLayout;
	private Button btnTitle;
	// private LinearLayout menuLayout;
	// private RadioGroup menuGroup;
	// private RadioButton rbDetail;
	// private RadioButton rbComment;
	// private RadioButton rbDiscount;
	// private RadioButton rbUpload;
	// private RadioButton rbOther;
	private LinearLayout mTitleLayout;
	// private View mBottomlayout;
	// private LinearLayout mFunctionlayout;
	private ImageView mArraw;

	// 菜单面板
	private View menuPanelView;
	private PopupWindow menuPanelDialog;
	private Button btnMenuGotoIndex;
	private Button btnMenuGotoLogin;
	private Button btnMenuPostError;
	protected Button btnMenuRefresh;

	// 底部
	private Button btLeft; // 底部左边按钮
	private Button btRight; // 底部右边按钮

	// 进度提示框
	private ProgressDialog progressDialog = null;

	protected Thread mUpdateCityThread;

	// 缓存数据
	protected MainMenuListInfo mainMenuListInfo;
	protected CityInfo cityInfo;

	// 拍照上传保存路径
	public Uri takePhotoUri;
	private OnShowUploadImageListener mOnShowUploadImageListener;
	// 是否需要弹出跳转
	public static boolean needCheckAndJumpToOrderInfoPage = false;
	// 是否是重后台进入前台
	public boolean isOnForeground = false;

	// 系统消息
	private Runnable messageRun = new Runnable() {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						updateSystemMessage();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	};
	// 城市改变
	private Runnable cityChangedRun = new Runnable() {

		@Override
		public void run() {
			try {
				onCityChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};
	private CommonObserver.CityChangedObserver cityChangedObserver = new CommonObserver.CityChangedObserver(cityChangedRun);
	private CommonObserver.SystemMessageObserver messageObserver = new CommonObserver.SystemMessageObserver(messageRun);
	private TextView index_frame_order_bubble;
	private TextView index_frame_user_center_bubble;
	private LinearLayout locationLayout;
	private TextView location_address;
	private ImageButton location_refresh;
	private ImageView main_frame_tvTitle_icon;
	private LinearLayout main_frame_tvTitle_layout;
	private LinearLayout btnGoBackContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		currentActivity = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_frame);
		// 组件初始化
		initComponent();
	}

	@Override
	protected void onResume() {
		// Log.d("MainFrame------onResume",returnToClass+", "+this.getClass());
		// XiaomishuChat chat =
		// XiaomishuChat.getInstance(MainFrameActivity.this);
		// DialogRemindFloatWindow floatWindow = DialogRemindFloatWindow
		// .getInstance(MainFrameActivity.this);
		// if (chat.isExitMark())
		// floatWindow.show();

		if (returnToClass != null) {
			// Log.d("MainFrame------",returnToClass+", "+this.getClass());
			if (returnToClass != this.getClass() && this.getClass() != IndexActivity.class) {
				// 没有回到目标页
				finish();
			} else {
				CommonObservable.getInstance().notifyObservers(ReturnToActivityFinishedObserver.class);
				returnToClass = null;
			}
		}
		// 前台 定位 和获取首页数据
		if (ActivityUtil.isOnForeground(getApplicationContext())) {

			Loc.sendLocControlMessage(true);

			if ((Fg114Application.isNeedUpdate || Fg114Application.isTimeToGetMainPage())) {
				Fg114Application.excuteMainPageInfoPackDTOTask();
				Fg114Application.isNeedUpdate = false;
			}

			if (isOnForeground && !Settings.Is_Push_Notification_to_activity) {
				isOnForeground = false;
				executePushMessage();
			}

		}

		super.onResume();
		// --------------
		currentActivity = this;
		Settings.CURRENT_PAGE = this.getClass().getSimpleName();
		initCityIssues();
		updateSystemMessage();
		CommonObservable.getInstance().addObserver(this.getClass().getSimpleName(), messageObserver);
		CommonObservable.getInstance().addObserver(this.getClass().getSimpleName(), cityChangedObserver);

	}

	@Override
	protected void onPause() {
		// Log.d("MainFrame------onPause",returnToClass+", "+this.getClass());
		super.onPause();
		lastActivityClass = this.getClass();
		CommonObservable.getInstance().deleteObserver(this.getClass().getSimpleName(), messageObserver);
		// if (XiaomishuChat.recallMe != null) {
		// XiaomishuChat.recallMe.hide();
		// }

		// CommonObservable.getInstance().deleteObserver(cityChangedObserver);

	}

	@Override
	protected void onDestroy() {
		// Log.d("MainFrame------onDestroy",returnToClass+", "+this.getClass());
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// Log.d("MainFrame------onStop",returnToClass+", "+this.getClass());
		// 定位停止 在后台
		if (!ActivityUtil.isOnForeground(getApplicationContext())) {
			isOnForeground = true;
			Loc.sendLocControlMessage(false);
			// 触发上传点击流---------------
			if (OpenPageDataTracer.upTag == false) {
				OpenPageDataTracer.upTag = true;
				IOUtils.writeTestInfo(MainFrameActivity.this, "log_OpenPageDataTracer.txt", "进入后台上传点击流\r\n" + CalendarUtil.getDateTimeString() + "---------------------\r\n");
				OpenPageDataTracer.getInstance().uploadImmediately();
			}

			Fg114Application app = (Fg114Application) getApplication();
			if (app.mBMapMan != null) {
				app.mBMapMan.stop();
				app.mBMapMan.destroy();
				app.mBMapMan = null;
			}
			IOUtils.writeTestInfo(MainFrameActivity.this, "log_isOnBackstage.txt", "进入后台\r\n" + CalendarUtil.getDateTimeString() + "---------------------\r\n");
		}
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == Settings.CAMERAIMAGE || requestCode == Settings.LOCALIMAGE)) {
			String path = null;
			if (data != null && data.getData() != null) {
				path = parseImgPath(data.getData());
			} else if (takePhotoUri != null) {
				path = parseImgPath(takePhotoUri);
			} else if (takePhotoUri == null && Settings.RestaurantRecommentDetailUri != null) {
				path = parseImgPath(Settings.RestaurantRecommentDetailUri);
				Settings.RestaurantRecommentDetailUri = null;
			}
			try {
				if (CheckUtil.isEmpty(path)) {
					DialogUtil.showToast(this, "没有选择任何图片或不是重图库中选择图片");
					return;
				}
				// 如果未拍照或选择了空图片
				if (new File(path).length() == 0) {
					getContentResolver().delete(takePhotoUri, null, null);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bundle bundle = new Bundle();
			Settings.uploadPictureUri = path;
			Settings.uploadPictureOrignalActivityId = 0;
			if (mOnShowUploadImageListener != null) {
				mOnShowUploadImageListener.onGetPic(bundle);
			}
			takePhotoUri = null;
		} else if (requestCode == Settings.LOCALIMAGE_BATCH) { // 批量上传图片
			try {

				if (data == null) {
					return;
				}
				ArrayList<String[]> picture_data_selected = (ArrayList<String[]>) data.getSerializableExtra("picture_data_selected");
				if (mOnShowUploadImageListener != null) {
					mOnShowUploadImageListener.onGetBatchPic(picture_data_selected);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void finish() {
		super.finish();
		Settings.CURRENT_PAGE = "";
		ActivityUtil.overridePendingTransition(this, R.anim.left_slide_in, R.anim.left_slide_out);

	}

	/*****************************************************************************
	 * 组件初始化
	 * */
	private void initComponent() {

		btnGoBackContainer = (LinearLayout) findViewById(R.id.main_frame_btnGoBack_container);
		btnGoBack = (Button) findViewById(R.id.main_frame_btnGoBack);
		main_frame_tvTitle_layout = (LinearLayout) findViewById(R.id.main_frame_tvTitle_layout);
		tvTitle = (TextView) findViewById(R.id.main_frame_tvTitle);
		main_frame_tvTitle_icon = (ImageView) findViewById(R.id.main_frame_tvTitle_icon);
		btnOption = (Button) findViewById(R.id.main_frame_btnOption);
		mainLayout = (LinearLayout) findViewById(R.id.main_frame_layout);
		// 定位
		locationLayout = (LinearLayout) findViewById(R.id.main_frame_locationLayout);
		location_address = (TextView) findViewById(R.id.main_frame_location_address);
		location_refresh = (ImageButton) findViewById(R.id.main_frame_location_refresh);
		mTitleLayout = (LinearLayout) findViewById(R.id.main_frame_titlelayout);
		// mBottomlayout = (RelativeLayout)
		// findViewById(R.id.main_frame_llBottom);
		// mFunctionlayout = (LinearLayout) findViewById(R.id.main_frame_fac);
		btnTitle = (Button) findViewById(R.id.main_frame_btnTitle);
		index_frame_order_bubble = (TextView) findViewById(R.id.index_frame_order_bubble);
		index_frame_user_center_bubble = (TextView) findViewById(R.id.index_frame_user_center_bubble);

		// 底部
		btLeft = (Button) findViewById(R.id.frame_btLeft);
		btRight = (Button) findViewById(R.id.frame_btRight);

		// 默认不显示
		locationLayout.setVisibility(View.GONE);
		location_address.setText(getCurrentAddress());
		location_refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 120);
				tryGetNewAddress();
			}
		});

		btLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----------------------------
				OpenPageDataTracer.getInstance().addEvent("导航栏左侧按钮");
				// ----------------------------

				MainPageOtherInfoPackDTO dto = SessionManager.getInstance().getMainPageOtherInfoPackDTO();

				// 是否直接跳转
				if (dto != null && dto.orderHintPackData != null && dto.orderHintPackData.haveOrderHintTag && needCheckAndJumpToOrderInfoPage == false) {
					// Bundle data=new Bundle();
					// data.putString(Settings.BUNDLE_ORDER_ID,dto.orderHintPackData.orderId);
					if (NewMyOrderDetailActivity.class == MainFrameActivity.this.getClass()) {
						finish();
					}
					// 防止循环跳
					if (currentActivity.getClass() != MyNewTakeAwayOrderDetailActivity.class && currentActivity.getClass() == HomeActivity.class
							&& currentActivity.getClass() != NewOrderListAcitivy.class) {
						ActivityUtil.jump(currentActivity, NewMyOrderDetailActivity.class, 0);
					}
				} else {
					if (NewOrderListAcitivy.class == MainFrameActivity.this.getClass()) {
						finish();
					}
					ActivityUtil.jump(currentActivity, NewOrderListAcitivy.class, 0);
				}
			}
		});

		btRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----------------------------
				OpenPageDataTracer.getInstance().addEvent("导航栏右侧按钮");
				// ----------------------------

				ActivityUtil.jump(currentActivity, UserCenterActivity.class, 0);
			}
		});

		// 返回按钮------------------------------------------------
		btnGoBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				finish();
			}
		});

		// mFunctionlayout.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// // ------------------------Dialog方式
		// if (naviDialog != null) {
		// naviDialog.show(currentActivity, mFunctionlayout.getChildAt(0));
		// } else {
		// naviDialog = new NavigationDialog(MainFrameActivity.this);
		// naviDialog.show(currentActivity, mFunctionlayout.getChildAt(0));
		// }
		//
		// }
		// });

	}

	protected RadioButton getMenuGroupUploadButton() {
		// return rbUpload;
		return null;
	}

	protected RadioButton getMenuGroupOtherButton() {
		// return rbOther;
		return null;
	}

	// 拍完照片后的操作
	protected void onFinishTakePic(String uploadType, String restId, String restName) {

		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, uploadType);
		bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID, restId);
		bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, restName);
		bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_ID, "");
		bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME, "");
		// Settings.uploadPictureOrignalActivityId = getActivityId();// 更改
		ActivityUtil.jump(this, RestaurantUploadActivity.class, 0, bundle);
	}

	// ---
	protected String getRestaurantId() {
		return "";
	}

	protected String getRestaurantName() {
		return "";
	}

	protected String getRestaurantUrl() {
		return "";
	}

	protected String getRestaurantLinkUrl() {
		return "";
	}

	// 推荐餐厅ID
	protected String getRecomRestaurantId() {
		return "";
	}

	// // ---
	// PopupWindow.OnDismissListener onDisMissListener = new OnDismissListener()
	// {
	//
	// @Override
	// public void onDismiss() {
	//
	// mFunctionlayout.getBackground().setLevel(0);
	// }
	// };

	/*****************************************************************************
	 * 取组件
	 * */

	public LinearLayout getTitleLayout() {
		return mTitleLayout;
	}

	public Button getBtnGoBack() {
		btnGoBack = (Button) findViewById(R.id.main_frame_btnGoBack);
		btnGoBackContainer = (LinearLayout) findViewById(R.id.main_frame_btnGoBack_container);
		// 由于返回按钮从先前的文字按钮变成了图片按钮（向左的箭头），
		// 为了不修改所有页面，在框架页里使用了下面技巧，返回一个原按钮的替身使操作转发到真实按钮上
		// 同时忽略对按钮文字的设置，从而实现去除按钮文字的目的
		Button btn = new Button(this) {

			@Override
			public void setOnClickListener(OnClickListener l) {
				btnGoBack.setOnClickListener(l);
			}

			@Override
			public void setVisibility(int visibility) {
				btnGoBackContainer.setVisibility(visibility);
			}

		};
		return btn;
	}

	public LinearLayout getMenuLayout() {
		// return menuLayout;
		return null;
	}

	public void setMenuLayout(LinearLayout menuLayout) {
		// this.menuLayout = menuLayout;
	}

	public RadioGroup getMenuGroup() {
		// return menuGroup;
		return null;
	}

	public void setMenuGroup(RadioGroup menuGroup) {
		// this.menuGroup = menuGroup;
	}

	public RadioButton getRbDetail() {
		// return rbDetail;
		return null;
	}

	public void setRbDetail(RadioButton rbDetail) {
		// this.rbDetail = rbDetail;
	}

	public RadioButton getRbComment() {
		// return rbComment;
		return null;
	}

	public void setRbComment(RadioButton rbComment) {
		// this.rbComment = rbComment;
	}

	public RadioButton getRbDiscount() {
		// return rbDiscount;
		return null;
	}

	public void setRbDiscount(RadioButton rbDiscount) {
		// this.rbDiscount = rbDiscount;
	}

	public RadioButton getRbUpload() {
		// return rbUpload;
		return null;
	}

	public void setRbUpload(RadioButton rbUpload) {
		// this.rbUpload = rbUpload;
	}

	public RadioButton getRbOther() {
		// return rbOther;
		return null;
	}

	public void setRbShare(RadioButton rbOther) {
		// this.rbOther = rbOther;
	}

	public LinearLayout getTvTitleLayout() {
		return main_frame_tvTitle_layout;
	}

	public TextView getTvTitle() {
		tvTitle = (TextView) findViewById(R.id.main_frame_tvTitle);
		return tvTitle;
	}

	public ImageView getTvTitleIcon() {
		return main_frame_tvTitle_icon;
	}

	public Button getBtnOption() {
		btnOption = (Button) findViewById(R.id.main_frame_btnOption);
		return btnOption;
	}

	public LinearLayout getMainLayout() {
		mainLayout = (LinearLayout) findViewById(R.id.main_frame_layout);
		return mainLayout;
	}

	protected View getBottomLayout() {
		// mBottomlayout = (RelativeLayout)
		// findViewById(R.id.main_frame_llBottom);
		return new View(this);
	}

	public void setFunctionLayoutGone() {

		// if (mFunctionlayout != null){
		// mFunctionlayout.setVisibility(View.GONE);
		// }
		// if (mBottomlayout != null){
		// mBottomlayout.setVisibility(View.GONE);
		// }
	}

	public Button getBtnTitle() {
		return btnTitle;
	}

	public MainMenuListInfo getMainMenuListInfo() {
		return mainMenuListInfo;
	}

	public void setMainMenuListInfo(MainMenuListInfo mainMenuListInfo) {
		this.mainMenuListInfo = mainMenuListInfo;
	}

	/**
	 * 获取框架底部左边按钮
	 * 
	 * @return btleft
	 */
	public Button getBtleft() {
		return btLeft;
	}

	/**
	 * 获取框架底部右边按钮
	 * 
	 * @return btRight
	 */
	public Button getBtRight() {
		return btRight;
	}

	/**
	 * 打开进度提示
	 */
	public void showProgressDialog(String msg) {
		try {
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(this);
				progressDialog.setTitle("");
				progressDialog.setMessage(msg);
				progressDialog.setIndeterminate(true);
			}
			progressDialog.show();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 关闭进度提示
	 */
	public void closeProgressDialog() {
		try {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			progressDialog = null;
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 构建城市
	 */
	protected void initCityIssues() {
		cityInfo = SessionManager.getInstance().getCityInfo(this);

	}

	protected void showErrorReportDialog() throws Exception {
		Bundle bundle = new Bundle();
		bundle.putString(Settings.UUID, bundleData.getString(Settings.BUNDLE_KEY_ID));
		bundle.putString(Settings.BUNDLE_REST_NAME, bundleData.getString(Settings.BUNDLE_REST_NAME));
		bundle.putDouble(Settings.BUNDLE_REST_LONGITUDE, bundleData.getDouble(Settings.BUNDLE_REST_LONGITUDE));
		bundle.putDouble(Settings.BUNDLE_REST_LATITUDE, bundleData.getDouble(Settings.BUNDLE_REST_LATITUDE));

		// 这里还要添加可能的错误列表数据，以供选择列表对话框显示
		DialogUtil.showErrorReportTypeSelectionDialog(MainFrameActivity.this, bundle);
	}

	/**
	 * 当检测到城市改变的时候，触发此方法
	 */
	protected void onCityChanged() {
		IndexActivity.pageTimestamp.clear();// 使多文档页面失效
		// 因为HomeActivity有自己的刷新逻辑，所以这里不让该页面失效
		// IndexActivity.pageTimestamp.put(HomeActivity.class.getSimpleName(),
		// SystemClock.elapsedRealtime());
	}

	/**
	 * 当更新功能系统消息（订单数量，站内信等）时，会触发此方法
	 */
	protected void updateSystemMessage() {
		// 逻辑：从缓存中取出最新的getMainPageInfoPackDTO
		// 1）更新订单气泡 2）决定是否直接跳转订单详情页（相当于弹出消息的性质）
		MainPageOtherInfoPackDTO dto = SessionManager.getInstance().getMainPageOtherInfoPackDTO();

		// 是否直接跳转（用户中心不跳，防止循环跳，因为用户中心要刷气泡，并不需要跳转）
		if (dto != null && dto.orderHintPackData != null && dto.orderHintPackData.haveOrderHintTag && needCheckAndJumpToOrderInfoPage) {
			needCheckAndJumpToOrderInfoPage = false;
			// if (this.getClass() != UserCenterActivity.class &&
			// this.getClass() != MyOrderDetailActivity.class && this.getClass()
			// != OrderListActivity.class
			// && this.getClass() != MyBookRestaurantActivity.class) {
			// btLeft.performClick();
			// }

			if (this.getClass() == HomeActivity.class && this.getClass() != NewOrderListAcitivy.class) {
				btLeft.performClick();
			}

		}
		// 是否显示气泡
		if (dto != null && dto.bubbleHintData != null && dto.bubbleHintData.orderNum > 0) {
			index_frame_order_bubble.setVisibility(View.VISIBLE);
			index_frame_order_bubble.setText("" + dto.bubbleHintData.orderNum);
		} else {
			index_frame_order_bubble.setVisibility(View.GONE);
			index_frame_order_bubble.setText("0");
		}

		// 右下角的气泡
		if (dto != null && dto.bubbleHintData != null && (dto.bubbleHintData.mailNum + dto.bubbleHintData.recomCommentNum) > 0) {
			index_frame_user_center_bubble.setVisibility(View.VISIBLE);
			index_frame_user_center_bubble.setText("" + (dto.bubbleHintData.mailNum + dto.bubbleHintData.recomCommentNum));
		} else {
			index_frame_user_center_bubble.setVisibility(View.GONE);
			index_frame_user_center_bubble.setText("0");
		}

	}

	// 地理定位逻辑
	String lastLocationName = "";
	public final static String locating = "正在定位...";
	public final static String locatingFailed = "无法获取当前位置";

	protected void setLocationLayoutVisibility(int visibility) {
		locationLayout.setVisibility(visibility);
		// 如果是显示，刷新一下定位信息
		if (View.VISIBLE == visibility) {
			tryGetNewAddress();
		}
	}

	/**
	 * 当刷新地址变化后触发此方法
	 */
	protected void onRefreshToNewAddress() {

	}

	// 刷新地址
	private void tryGetNewAddress() {
		if (location_address.getText().toString().equals(locating)) {
			return;
		}
		lastLocationName = location_address.getText().toString();
		location_address.setText(locating);
		location_address.postDelayed(new Runnable() {

			@Override
			public void run() {
				location_address.setText(getCurrentAddress());
				if (!location_address.getText().toString().equals(lastLocationName)) {
					// 地址有变化，触发事件
					onRefreshToNewAddress();
				}
			}
		}, 2100);

	}

	protected String getCurrentAddress() {
		BDLocation loc = LocBaidu.currentLocation;
		if (loc == null || !(loc.getLatitude() > 0 && loc.getLongitude() > 0)) {
			return locatingFailed;
		} else {
			if (!loc.hasAddr() || (CheckUtil.isEmpty(loc.getDistrict()) && CheckUtil.isEmpty(loc.getStreet()))) {
				// 没有地址，取经纬度显示
				return loc.getLatitude() + "," + loc.getLongitude();
			} else {
				return loc.getDistrict() + loc.getStreet();
			}
			// return loc.getAddrStr();
		}
	}

	// --------------------------------------------
	private void showUploadPanel(boolean isBatch, Bundle data, Activity activity) {
		if (ActivityUtil.checkMysoftStage(this)) {
			ButtonPanelUtil pan = new ButtonPanelUtil(isBatch, data);
			pan.showUploadPanel(mainLayout, this, null);
			pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener() {

				@Override
				public void onGetUri(Uri uri) {
					takePhotoUri = uri;
				}
			});
		}
	}

	/**
	 * 获得路径
	 * 
	 * @param data
	 * @return
	 */
	private String parseImgPath(Uri uri) {
		String path = null;
		if (uri != null) {
			ContentResolver localContentResolver = getContentResolver();
			// 查询图片真实路径
			Cursor cursor = localContentResolver.query(uri, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
					if (index > 0) {
						path = cursor.getString(index);
					}
					cursor.close();
				}
			}
		}
		return path;
	}

	public abstract class OnShowUploadImageListener {

		public void onGetPic(Bundle bundle) {
		}

		/**
		 * 批量上传返回数据说明 存放图片数据的二维表 每行依次：图片id，图片绝对路径，是否选中:1选中，0未选中
		 */
		public void onGetBatchPic(ArrayList<String[]> picture_data_selected) {
		}
	}

	public void setOnShowUploadImageListener(OnShowUploadImageListener listener) {
		mOnShowUploadImageListener = listener;
	}

	// 默认拍照需要在当前城市
	public void takePic(OnShowUploadImageListener listener) {
		takePic(listener, true);
	}

	/**
	 * 批量上传，推荐餐厅时用
	 */
	public void takeBatchPic(OnShowUploadImageListener listener, Bundle data) {
		takePic(listener, false, true, data, this);
	}

	/**
	 * 批量上传，推荐餐厅时用
	 */
	public void takeBatchPic(OnShowUploadImageListener listener) {
		takePic(listener, false, true, null, this);
	}

	public void takePic(OnShowUploadImageListener listener, boolean needInCurrentCity) {
		takePic(listener, needInCurrentCity, false, null, this);
	}

	public void takePic(OnShowUploadImageListener listener, boolean needInCurrentCity, boolean isBatch) {
		takePic(listener, needInCurrentCity, false, null, this);
	}

	// needInCurrentCity需要在当前城市，一般的图片上传不需在当前城市，随手拍需要在当前城市
	public void takePic(OnShowUploadImageListener listener, boolean needInCurrentCity, boolean isBatch, Bundle data, Activity activity) {
		if (needInCurrentCity) {
			if (!Loc.isGpsAvailable()) {
				// 没有定位的场合，提示打开
				DialogUtil.showAlert(this, true, getString(R.string.text_dialog_goto_open_gps), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityUtil.gotoSysSetting(MainFrameActivity.this);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消
						dialog.cancel();
					}
				});
				return;
			}

			// 检查当前城市是否是所在城市
			final CityInfo gpsCityInfo = SessionManager.getInstance().getGpsCity(this);
			if (gpsCityInfo == null || CheckUtil.isEmpty(gpsCityInfo.getId())) {
				DialogUtil.showToast(this, "无法找到您的位置，请稍后再试");
				return;
			}
			if (!gpsCityInfo.getId().equals(cityInfo.getId())) {
				// 获得提示信息
				String orignMsg = "GPS显示您在{0}，无法使用{1}的{2}功能哦，切换城市试试吧~";
				String alertMsg = DialogUtil.fullMsg(orignMsg, gpsCityInfo.getName(), cityInfo.getName(), "随手拍");
				// 当定位成功的城市不是所选城市的场合城市
				DialogUtil.showAlert(this, true, null, alertMsg, "切换", "取消", new DialogInterface.OnClickListener() {// 消息提示框确定按钮

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 将定位的城市设为当前城市
								cityInfo.setId(gpsCityInfo.getId());
								cityInfo.setName(gpsCityInfo.getName());
								cityInfo.setPhone(gpsCityInfo.getPhone());
								SessionManager.getInstance().setCityInfo(MainFrameActivity.this, cityInfo);
								// 切换完后，回首页
								ActivityUtil.jump(MainFrameActivity.this, IndexActivity.class, 0, new Bundle(), true);
								finish();
							}
						}, new DialogInterface.OnClickListener() {// 消息提示框取消按钮

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
				return;
			}
		}

		// 随手拍
		showUploadPanel(isBatch, data, activity);
		mOnShowUploadImageListener = listener;
	}

	/**
	 * 设置分享到新浪微博的状态
	 * 
	 * @param chbShareToSina
	 * @param atSinaWeibo
	 */
	protected void setShareSinaState(CheckBox chbShareToSina, Button atSinaWeibo) {
		if (chbShareToSina == null) {
			return;
		}
		UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(this);
		if (!userInfo.isSinaBindTag()) {
			// 未绑定
			if (chbShareToSina.isChecked()) {
				chbShareToSina.setChecked(false);
			}
			chbShareToSina.setText(R.string.text_layout_share_to_sina);
			if (atSinaWeibo != null) {
				atSinaWeibo.setBackgroundResource(R.drawable.at_unbound_bt);
			}
		} else {
			if (userInfo.isSinaWeiboExpired()) {
				// 已绑定但认证过期
				if (chbShareToSina.isChecked()) {
					chbShareToSina.setChecked(false);
				}
				chbShareToSina.setText(Html.fromHtml(getString(R.string.text_layout_share_to_sina) + " <font color=\"#bf0001\">绑定已过期</font>"));
				if (atSinaWeibo != null) {
					atSinaWeibo.setBackgroundResource(R.drawable.at_unbound_bt);
				}
			} else {
				// 已绑定，认证未过期
				if (!chbShareToSina.isChecked()) {
					chbShareToSina.setChecked(true);
				}
				chbShareToSina.setText(R.string.text_layout_share_to_sina);
				if (atSinaWeibo != null) {
					atSinaWeibo.setBackgroundResource(R.drawable.at_bt);
				}
			}
		}
	}

	protected void showOtherDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(R.array.other_options, otherOptionsListener).setTitle("你想要？").setNegativeButton("取消", null).create();
		builder.show();
	}

	private int typeTag;

	// 1:分享餐厅 2：软件分享 3：推荐分享 4:外卖分享 5:wap分享6:秘币分享100:其他分享
	protected void showShareDialog(int typeTag) {
		this.typeTag = typeTag;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(R.array.share_res_new, shareRes).setTitle("分享").setNegativeButton("取消", null).create();
		builder.show();
	}

	// private String[] dynamicshareDetail;// 动态分享按钮内容
	//
	// // 动态添加选择列表
	// // 1:分享餐厅 2：软件分享 3：推荐分享 4:外卖分享 5:wap分享6:秘币分享
	// protected void showShareDialog(int typeTag, String[] dynamicshareDetail)
	// {
	// this.typeTag = typeTag;
	// this.dynamicshareDetail = dynamicshareDetail;
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setItems(dynamicshareDetail,
	// DynamicshareRes).setTitle("分享").setNegativeButton("取消", null).create();
	// builder.show();
	// }

	// 报错和其他
	protected DialogInterface.OnClickListener otherOptionsListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			dialog.dismiss();
			switch (which) {
			case 0:// 报错
				try {
					showErrorReportDialog();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1: // 分享
				showShareDialog(1);
				break;
			}
		}
	};
	// 分享按钮功能
	protected DialogInterface.OnClickListener shareRes = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			// String uuid = typeTag == 3 ? getRecomRestaurantId() :
			// getRestaurantId();
			String uuid = "";
			String weixinName = "";
			Settings.shareTypeTag = typeTag;
			Settings.shareUuid = getWeiboUuid();
			switch (typeTag) {
			case 1:
				// 1:分享餐厅
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			case 2:
				// 2：软件分享
				uuid = "";
				weixinName = getWeixinName();
				break;
			case 3:
				// 3：推荐分享
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			case 4:
				// 4:外卖分享
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			case 5:
				// 5:wap分享
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			case 6:
				// 6:秘币分享
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			default: // 其它分享
				uuid = getRestaurantId();
				weixinName = getWeixinName();
				break;
			}

			switch (which) {
			case 0:

				// ----
				OpenPageDataTracer.getInstance().addEvent("分享-短信", uuid);
				// -----

				try {
					String info = makeSMSinfo();
					ActivityUtil.sendSMS(MainFrameActivity.this, "", info);
				} catch (Exception e) {
					DialogUtil.showToast(MainFrameActivity.this, "对不起，暂时无法分享");
				}
				break;

			case 1:

				// ----
				OpenPageDataTracer.getInstance().addEvent("分享-邮件", uuid);
				// -----

				try {
					String emailInfo = makeEmailInfo();
					ActivityUtil.callEmail((Activity) MainFrameActivity.this, "", "看看这家餐厅怎么样", emailInfo);
				} catch (Exception e) {
					DialogUtil.showToast(MainFrameActivity.this, "对不起，暂时无法分享");
				}

				break;
			case 2:
				// ----
				OpenPageDataTracer.getInstance().addEvent("分享-微博", uuid);
				// -----

				String weiboInfo = makeWeiboInfo();
				String weiboUuid = "";
				weiboUuid = getWeiboUuid();
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_WEIBO_DETAIL, weiboInfo);
				bundle.putString(Settings.BUNDLE_REST_ID, weiboUuid);
				bundle.putInt(Settings.BUNDLE_KEY_TYPE, typeTag);// 1:分享餐厅
																	// 2：软件分享
																	// 3：推荐分享
																	// 4:外卖分享
																	// 5:wap分享

				ActivityUtil.jump(MainFrameActivity.this, ShareToWeiboActivity.class, 0, bundle);
				break;

			case 3:// 分享到微信

				// ----
				OpenPageDataTracer.getInstance().addEvent("分享-微信", uuid);
				// -----
				Settings.wxTypeTag = 1;

				String info = "";
				if (CheckUtil.isEmpty(makeWeiXinInfo())) {
					info = makeWeiboInfo() + "【来自小秘书客户端】http://www.xiaomishu.com/o/app";
				} else {
					info = makeWeiXinInfo();
				}
				bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_SHARE_DETAIL, info);
				bundle.putString(Settings.BUNDLE_REST_ID, uuid);
				bundle.putString(Settings.BUNDLE_REST_NAME, weixinName);
				bundle.putString(Settings.BUNDLE_REST_IMAGE_URL, getRestaurantUrl() != null ? getRestaurantUrl().trim() : getRestaurantUrl()); // 送给微信的时候，要trim一下
				bundle.putString(Settings.BUNDLE_REST_LINK_URL, getRestaurantLinkUrl() != null ? getRestaurantLinkUrl().trim() : getRestaurantLinkUrl());// 送给微信的时候，要trim一下
				bundle.putInt(Settings.BUNDLE_KEY_TYPE, 1); // 1分享到微信,
															// 2//分享到微信朋友圈
				ActivityUtil.jump(MainFrameActivity.this, ShareToWeiXinActivity.class, 0, bundle);
				break;

			case 4:// 分享到微信朋友圈

				// ----
				OpenPageDataTracer.getInstance().addEvent("分享-朋友圈", uuid);
				// -----
				Settings.wxTypeTag = 2;
				String infoP = "";
				if (CheckUtil.isEmpty(makeWeiXinInfo())) {
					infoP = makeWeiboInfo() + "【来自小秘书客户端】http://www.xiaomishu.com/o/app";
				} else {
					infoP = makeWeiXinInfo();
				}
				bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_SHARE_DETAIL, infoP);
				bundle.putString(Settings.BUNDLE_REST_ID, uuid);
				bundle.putString(Settings.BUNDLE_REST_NAME, weixinName);
				bundle.putString(Settings.BUNDLE_REST_IMAGE_URL, getRestaurantUrl() != null ? getRestaurantUrl().trim() : getRestaurantUrl());// 送给微信的时候，要trim一下
				bundle.putString(Settings.BUNDLE_REST_LINK_URL, getRestaurantLinkUrl() != null ? getRestaurantLinkUrl().trim() : getRestaurantLinkUrl());// 送给微信的时候，要trim一下
				bundle.putInt(Settings.BUNDLE_KEY_TYPE, 2);// 1分享到微信,
															// 2//分享到微信朋友圈
				ActivityUtil.jump(MainFrameActivity.this, ShareToWeiXinActivity.class, 0, bundle);
				break;
			default: // 其它分享
				// shareOther();
				break;
			}
		}
	};

	/**
	 * 分享餐厅
	 * 
	 * @param context
	 */
	protected void shareOther() {
		RestInfoData info = SessionManager.getInstance().getRestaurantInfo(this, getRestaurantId());
		WeiboContentBuilder wb = new WeiboContentBuilder();
		if (info != null && !CheckUtil.isEmpty(info.uuid)) {
			/*
			 * 模板 title= 看看这家餐厅怎么样 Hi 我觉得这家餐厅挺不错的，你觉得怎么样？ [餐厅名] [地址] [推荐菜最多10个]
			 * 优惠预订电话：上海57575777，全国10107777 网站url　http://www.xiaomishu.com/xxxx
			 */
			wb.appendImportantText("Hi\n");
			wb.appendImportantText("我觉得这家餐厅挺不错的，你觉得怎么样？\n");
			wb.appendImportantText(info.name + "\n");
			wb.appendImportantText(info.address + "\n");

			if (info.specialFoodList != null && info.specialFoodList.size() > 0) {
				for (RestFoodData data : info.specialFoodList) {
					wb.appendText(data.getName() + "\n");
				}
			}
			// sbMsg.append("优惠预订电话：上海57575777，全国10107777").append("\n");
			wb.appendImportantText("http://www.xiaomishu.com/shop/" + info.uuid);
			wb.appendImportantText("【来自小秘书客户端】http://www.xiaomishu.com/o/app");
			try {
				ActivityUtil.callShare((Activity) this, "看看这家餐厅怎么样", wb.toWeiboString(), "分享");
			} catch (Exception e) {
				DialogUtil.showToast(this, "对不起，暂时无法分享");
			}
		}
	}

	// 拼接短信信息
	protected String makeSMSinfo() {
		// 刚用小秘书客户端找到这家：xxxxxx餐厅，xxxx路xx号。感觉还不错哟~你看怎样？详情点击：http：// xyxyxyxy
		RestInfoData info = SessionManager.getInstance().getRestaurantInfo(this, getRestaurantId());
		StringBuilder sb = new StringBuilder();
		if (info != null && !CheckUtil.isEmpty(info.uuid)) {
			sb.append("刚用小秘书客户端找到这家：" + info.name + "," + info.address + "。");
			sb.append("感觉还不错哟~你看怎样？详情点击：" + (CheckUtil.isEmpty(info.linkUrl) ? "http://www.xiaomishu.com/" : info.linkUrl));
			sb.append("【来自小秘书客户端】http://www.xiaomishu.com/o/app");
		}

		return sb.toString();

	}

	protected String makeWeiboInfo() {
		RestInfoData info = SessionManager.getInstance().getRestaurantInfo(this, getRestaurantId());
		StringBuilder sb = new StringBuilder();
		if (info != null && !CheckUtil.isEmpty(info.uuid)) {
			sb.append(info.name + ",");
			sb.append("貌似不错，");
			if (info.specialFoodList != null && info.specialFoodList.size() > 0) {
				for (RestFoodData data : info.specialFoodList) {
					sb.append(data.getName() + ",");
				}
				sb.append("看得我好馋啊，");
			}

			sb.append("就在" + info.address + ",");
			sb.append("有没有人凑份？");

		}

		return sb.toString();

	}

	protected String makeWeiXinInfo() {
		String info = makeWeiboInfo() + "【来自小秘书客户端】http://www.xiaomishu.com/o/app";
		return info;
	}

	protected String getWeixinName() {
		String weixinName = "";
		return weixinName;
	}

	protected String getWeiboUuid() {
		String weiboUuid = "";
		return weiboUuid;
	}

	// 拼接邮件信息
	protected String makeEmailInfo() {
		RestInfoData info = SessionManager.getInstance().getRestaurantInfo(this, getRestaurantId());
		StringBuilder sb = new StringBuilder();
		if (info != null && !CheckUtil.isEmpty(info.uuid)) {

			sb.append("Hi\n");
			sb.append("我觉得这家餐厅挺不错的，你觉得怎么样？\n");
			sb.append(info.name + "\n");
			sb.append(info.address + "\n");

			if (info.specialFoodList != null && info.specialFoodList.size() > 0) {
				for (RestFoodData data : info.specialFoodList) {
					sb.append(data.getName() + "\n");
				}
			}
			sb.append("优惠预订电话：上海57575777，全国10107777").append("\n");
			sb.append("http://www.xiaomishu.com/shop/" + info.uuid);
			sb.append("【来自小秘书客户端】http://www.xiaomishu.com/o/app");

		}
		return sb.toString();

	}

	/**
	 * 返回上一个Activity的类
	 * 
	 * @return
	 */
	public static Class<? extends Activity> getLastActivityClass() {
		return lastActivityClass;
	}

	/**
	 * 返回当前Activity的类
	 * 
	 * @return
	 */
	public static Activity getCurrentTopActivity() {
		return currentActivity;
	}

	// ----------------------------------
	// 直接返回到栈中前面某个页面的工具方法
	// ----------------------------------
	private static Class<? extends Activity> returnToClass = null;

	public static void returnToActivity(Class<? extends Activity> clazz) {
		// Log.d("returnToActivity------",clazz+"");
		// Thread.dumpStack();
		returnToClass = clazz;
	}

	/**
	 * 创建Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (ActivityUtil.isTestDev(this)) {
			DialogUtil.showDialog(this, R.layout.dialog_request_log, new DialogEventListener() {

				@Override
				public void onInit(View contentView, PopupWindow dialog) {
					TextView text = (TextView) contentView.findViewById(R.id.log);
					text.setText(Settings.requestLog.toString());
				}
			});
		}
		return false;
	}

	public void executePushMessage() {
		ServiceRequest request = new ServiceRequest(API.getLastNoticeMessage);
		CommonTask.request(request, new CommonTask.TaskListener<PushMsgDTO>() {

			@Override
			protected void onSuccess(final PushMsgDTO dto) {
				if (dto != null) {
					if (dto.getTypeTag() > 0) {
						String cancel = "";
						String ok = "";
						if (CheckUtil.isEmpty(dto.getCancelButtonName())) {
							cancel = "取消";
						} else {
							cancel = dto.getCancelButtonName();
						}
						if (CheckUtil.isEmpty(dto.getOkButtonName())) {
							ok = "去看看";
						} else {
							ok = dto.getOkButtonName();
						}
						DialogUtil.showAlert(MainFrameActivity.this,  true, dto.getTitle(), cancel, ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
//								try {
//									PushMessageReceiver.clearNotification();
//								} catch (Exception e) {
//									// TODO: handle exception
//								}

							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								if (dto.getTypeTag() == 1) {
									// 广告链接，使用内嵌的WebView打开
									Bundle bd = new Bundle();
									bd.putString(Settings.BUNDLE_KEY_WEB_URL, dto.getAdvUrl());
									bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, dto.getTitle());
									ActivityUtil.jump(MainFrameActivity.this, SimpleWebViewActivity.class, 0, bd);

								} else if (dto.getTypeTag() == 2) {
									// 本地链接，跳转本地界面
									URLExecutor.execute(dto.getAdvUrl(), MainFrameActivity.this, 0);
								} else if (dto.getTypeTag() == 3) {
									// 普通链接，使用系统浏览器打开
									ActivityUtil.jumbToWeb(MainFrameActivity.this, dto.getAdvUrl());
								}
//								try {
//									PushMessageReceiver.clearNotification();
//								} catch (Exception e) {
//									// TODO: handle exception
//								}

							}

						});
					}
				}
			}

			@Override
			protected void onError(int code, String message) {

			}
		});
	}
}