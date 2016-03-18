package com.fg114.main.app.activity.resandfood;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.service.dto.UploadData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.UploadImageTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.FriendSelectionActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;
import com.fg114.main.weibo.dto.User;

/**
 * 餐厅图片上传界面
 * 
 * @author xujianjun,2012-10
 * 
 */
public class RestaurantUploadActivity extends MainFrameActivity {

	private static final String TAG = RestaurantUploadActivity.class.getSimpleName();

	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 70; // 图片压缩率

	public static boolean goingToOriginalPage; // 标志直接返回最初页

	// 传入参数
	private String mImageUri; // 文件位置
	private int fromPage;

	// 界面组件
	private LayoutInflater inflater;
	private View contextView;
	private ImageView ivUploadPic;
	private EditText comments;
	private Button btnUploadToFood;
	private Button btnUploadToRestaurant;
	private Button btnUploadToFood1;
	private Button btnUploadToRestaurant1;
	private EditText etSelectFood;
	private LinearLayout btnSelectFoodLayout;
	private Button btnSelectCommentTemplet;
	private Button btnUpload;
	private ProgressBar pbBar;
	private LinearLayout editLayout;
	private LinearLayout layoutPriceAndUnit;
	private EditText price;
	private Spinner unit;
	private Button voiceButton;
	private CheckBox chbShareToSina;
	private CheckBox chbShareToTencent;
	private Button atSinaWeibo;

	private Bitmap mBitmap;
	private Handler mUploadHandler = new Handler();
	private Thread mUploadThread;
	private UploadImageTask uploadImageTask;

	private String uploadType = Settings.UPLOAD_TYPE_RESTAURANT;
	private String restId; // 餐厅ID
	private String restName;
	private String foodId;
	private String foodName;
	private String mCommont;
	private boolean isReCapture; // 是否是再拍一张
	private Uri tempPath;
	private LinearLayout tabLayout0;
	private LinearLayout tabLayout1;
	

//	// 获取上传初始数据，秘币奖励信息等
//	private GetUploadDataTask task;

	//
	private Animation mAnim;

	//
	private boolean isSinaWeiboBinded;
	private boolean isTencentWeiboBinded;
	private UploadData data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();

		if (Settings.UPLOAD_TYPE_FOOD.equals(bundle.getString(Settings.BUNDLE_UPLOAD_TYPE))) {
			uploadType = Settings.UPLOAD_TYPE_FOOD;
		} else {
			uploadType = Settings.UPLOAD_TYPE_RESTAURANT;
		}

		restId = bundle.getString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID);
		restName = bundle.getString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME);
		foodId = bundle.getString(Settings.BUNDLE_UPLOAD_FOOD_ID);
		foodName = bundle.getString(Settings.BUNDLE_UPLOAD_FOOD_NAME);
		// mCommont = SharedprefUtil.get(this,
		// Settings.BUNDLE_FOODANDRES_COMMENT, "");//不取历史评论
		setResult(Settings.uploadPictureOrignalActivityId);

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
		initPageState();
		executeGetDataTask();
		
	}

	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { //返回按钮事件 this.finish(); } return
	 * super.onKeyDown(keyCode, event);
	 * 
	 * }
	 */
	@Override
	public void finish() {
		SharedprefUtil.save(RestaurantUploadActivity.this, Settings.BUNDLE_FOODANDRES_COMMENT, comments.getText().toString().trim());
		if (comments.getText().toString().trim().length() > 0) {
			// 提示还未上传
			DialogUtil.showAlert(this, true, getString(R.string.text_info_has_not_upload), new DialogInterface.OnClickListener() {
				/**
				 * 确定按钮事件
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (uploadImageTask != null && !uploadImageTask.isCancelled()) {
						uploadImageTask.cancel(true);
					}
					recycle();
					setResult(fromPage);
					RestaurantUploadActivity.super.finish();
					dialog.dismiss();
				}
			}, new DialogInterface.OnClickListener() {
				/**
				 * 取消按钮事件
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		} else {
			recycle();
			setResult(fromPage);
			RestaurantUploadActivity.super.finish();
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏

		getBtnGoBack().setText(R.string.text_button_back);
		getBtnOption().setVisibility(View.VISIBLE);

		// 内容部分
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = inflater.inflate(R.layout.restaurant_upload, null);

		ivUploadPic = (ImageView) contextView.findViewById(R.id.upload_ivUploadPic);
		comments = (EditText) contextView.findViewById(R.id.upload_comments);
		pbBar = (ProgressBar) contextView.findViewById(R.id.upload_pbBar);
		btnUploadToFood = (Button) contextView.findViewById(R.id.upload_to_food0);
		btnUploadToRestaurant = (Button) contextView.findViewById(R.id.upload_to_restaurant0);
		btnUploadToFood1 = (Button) contextView.findViewById(R.id.upload_to_food1);
		btnUploadToRestaurant1 = (Button) contextView.findViewById(R.id.upload_to_restaurant1);
		etSelectFood = (EditText) contextView.findViewById(R.id.upload_select_food);
		btnSelectFoodLayout = (LinearLayout) contextView.findViewById(R.id.upload_select_food_layout);
		btnSelectCommentTemplet = (Button) contextView.findViewById(R.id.upload_select_comment_templet);
		btnUpload = (Button) contextView.findViewById(R.id.upload_btnUpLoad);
		// ---
		editLayout = (LinearLayout) contextView.findViewById(R.id.upload_edit_layout);
		layoutPriceAndUnit = (LinearLayout) contextView.findViewById(R.id.upload_layout_price_and_unit);
		price = (EditText) contextView.findViewById(R.id.upload_price);
		unit = (Spinner) contextView.findViewById(R.id.upload_unit);
		voiceButton = (Button) contextView.findViewById(R.id.upload_voice_button);
		chbShareToSina = (CheckBox) contextView.findViewById(R.id.upload_chkShareSina);
		chbShareToTencent = (CheckBox) contextView.findViewById(R.id.upload_chkShareTencent);
		mAnim = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
		atSinaWeibo = (Button) contextView.findViewById(R.id.upload_atsina_weibo);
		tabLayout0 = (LinearLayout) contextView.findViewById(R.id.layout_tab_0);
		tabLayout1 = (LinearLayout) contextView.findViewById(R.id.layout_tab_1);
		//
		comments.setText(mCommont);

		// 绑定语音按钮----------
		RecognitionEngine.getEngine(this).bindButtonAndEditText(voiceButton, comments);

		// 设置Spinner样式-------
		ArrayAdapter<String> unitAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_flexible_simple_gray_bg, getResources().getStringArray(
				R.array.upload_unit_array));
		unitAdapter.setDropDownViewResource(R.layout.spinner_item_simple_dropdown);
		unit.setAdapter(unitAdapter);
		
		// 绑定新浪微博
		chbShareToSina.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isChecked && !isSinaWeiboBinded) { // 新浪微博未被绑定或者绑定已经过期
					CommonObservable.getInstance().addObserver(
							new CommonObserver.WeiboAuthResultObserver(
									new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

										@Override
										public void onComplete(boolean isSuccessful) {
											if (isSuccessful) {
												// 绑定成功
												SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
												
												//设置微博状态
												setShareState();
											} 
										}
									}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).requestWeiboShare(null);
				}
			}
		});
		// 绑定腾讯微博
		chbShareToTencent.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked && !isTencentWeiboBinded) {// 腾讯微博未被绑定或者绑定已经过期
					CommonObservable.getInstance().addObserver(
							new CommonObserver.WeiboAuthResultObserver(
									new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

										@Override
										public void onComplete(boolean isSuccessful) {
											if (isSuccessful) {
												// 绑定成功
												SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
												//设置微博状态
												setShareState();
											} 
										}
									}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO).requestWeiboShare(null);
				}
			}
		});

		// 切换图片类型
		btnUploadToFood.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {
					return;
				}
				uploadType = Settings.UPLOAD_TYPE_FOOD;
				adjustPageState();
			}
		});
		btnUploadToFood1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				btnUploadToFood.performClick();
			}
		});
		btnUploadToRestaurant.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (Settings.UPLOAD_TYPE_RESTAURANT.equals(uploadType)) {
					return;
				}
				uploadType = Settings.UPLOAD_TYPE_RESTAURANT;
				adjustPageState();
			}
		});
		btnUploadToRestaurant1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				btnUploadToRestaurant.performClick();
			}
		});
		// 选择菜品名称
		btnSelectFoodLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID, restId);
				bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, restName);
				bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME, foodName);
				ActivityUtil.jump(RestaurantUploadActivity.this, RestaurantSearchFoodActivity.class, 0, bundle);
			}
		});
		// --选择评论模版
		btnSelectCommentTemplet.setOnClickListener(new OnClickListener() {

			String[] items = new String[] {};

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 模版选中后，追加到评论
				if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {// 菜图
					if (data != null && data.getFoodDescribeList() != null) {
						items = data.getFoodDescribeList().toArray(new String[0]);
					}
				} else {
					if (data != null && data.getEnvDescribeList() != null) {
						items = data.getEnvDescribeList().toArray(new String[0]);
					}
				}
				// -----
				DialogUtil.showListDialog(RestaurantUploadActivity.this, "选择一个评论", items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						comments.setText(items[which]);
						// 光标重新定位
						comments.setSelection(comments.getText().length());
					}
				});

			}
		});
		// --
		getBtnOption().setText("确定");
		getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				btnUpload.performClick();
			}
		});
		// 确定
		btnUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				
				foodName=etSelectFood.getText().toString();
				if (!checkInput()) {
					return;
				}
				// 校正数据
				if (!Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {
					foodId = "";
					foodName = "";
					price.setText("");
					unit.setSelection(0);
				}
				SessionManager.uploadData.uploadType = uploadType;
				SessionManager.uploadData.uploadData = (data == null ? new UploadData() : data);
				SessionManager.uploadData.restId = restId;
				SessionManager.uploadData.restName = restName;
				SessionManager.uploadData.foodId = foodId;
				SessionManager.uploadData.foodName = foodName;

				SessionManager.uploadData.price = price.getText().toString();
				SessionManager.uploadData.unit = unit.getSelectedItem().toString();
				SessionManager.uploadData.comment = comments.getText().toString();
				SessionManager.uploadData.shareString = getShareString();

				Bundle bundle = new Bundle();

				if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {// 到菜品评分
					ActivityUtil.jump(RestaurantUploadActivity.this, RestaurantUploadGradeFoodActivity.class, 0, bundle);

				} else {// 到环境评分
					ActivityUtil
							.jump(RestaurantUploadActivity.this, RestaurantUploadGradeRestaurantActivity.class, 0, bundle);
				}

			}

		});

		
		// at新浪微博好友选择页面
		atSinaWeibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 逻辑：如果“分享新浪微博”没打勾，则帮它打勾，如果打勾了，则直接跳转到“选择新浪好友页面”
				// 首先检查是否登录，如果未登录则先去登录
				if (!chbShareToSina.isChecked()) {
					chbShareToSina.setChecked(true);
					// 判断，如果已经绑定了，直接跳转到选择好友
					UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(RestaurantUploadActivity.this);
					if (userInfo.isSinaBindTag() && !userInfo.isSinaWeiboExpired()) {
						Bundle bundle = new Bundle();
						ActivityUtil.jump(RestaurantUploadActivity.this, FriendSelectionActivity.class, 0, bundle);
					}
				} else {
					UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(RestaurantUploadActivity.this);
					if (userInfo.isSinaBindTag() && !userInfo.isSinaWeiboExpired()) {
						Bundle bundle = new Bundle();
						ActivityUtil.jump(RestaurantUploadActivity.this, FriendSelectionActivity.class, 0, bundle);
					}
				}
			}
		});

		setFunctionLayoutGone();
		getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private boolean checkInput() {
		if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {
			if (CheckUtil.isEmpty(foodName)) {
				DialogUtil.showToast(this, "请输入菜品名称!");
				return false;
			}
			String p = price.getText().toString();
			if (p.startsWith(".") || p.endsWith(".")) {
				DialogUtil.showToast(this, "菜品价格输入有误!");
				return false;
			}
			if (!CheckUtil.isEmpty(p)) {
				double dprice = Double.parseDouble(p);
				if (dprice < 0 || dprice > 1000000) {
					DialogUtil.showToast(this, "菜品价格输入错误，价格范围应在零到一百万之间!");
					return false;
				}
			}
		}
		// SessionManager.uploadData.price=price.getText().toString();
		// SessionManager.uploadData.unit=unit.getSelectedItem().toString();
		// SessionManager.uploadData.comment=comments.getText().toString();
		// SessionManager.uploadData.shareString=getShareString();
		return true;
	}

	private void capturePicture() {
		if (ActivityUtil.checkMysoftStage(RestaurantUploadActivity.this)) {
			ButtonPanelUtil pan = new ButtonPanelUtil();
			pan.showUploadPanel(this.contextView, RestaurantUploadActivity.this, null);
			pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener() {

				@Override
				public void onGetUri(Uri uri) {
					tempPath = uri;
					// mFoodId = "0";
				}
			});
		}
	}

	// 载入图片
	private void loadPicture() {
		recycle();
		mImageUri = Settings.uploadPictureUri; // 获取上传图片路径
		// ---------------------
		ContentResolver contentResolver = this.getContentResolver();
		InputStream picStream = null; // 图片流
		if (mImageUri == null || mImageUri.equals("")) {
			DialogUtil.showToast(this, "没有选择任何图片");
			super.finish();
			return;
		}
		try {
			picStream = new FileInputStream(mImageUri);
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			if (picStream.available() == 0) {
				DialogUtil.showToast(this, "图片数据无效");
				super.finish();
				return;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取图片的宽和高
			BitmapFactory.decodeStream(picStream, null, options); // 此时返回bm为空
			options.inJustDecodeBounds = false;

			if (options.outWidth < 0 || options.outHeight < 0) {
				// 选中的是非图像文件
				DialogUtil.showToast(getBaseContext(), "上传图片的类型必须是GIF,PNG,JPG格式");
				super.finish();
				return;
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
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			picStream = new FileInputStream(mImageUri);
			mBitmap = BitmapFactory.decodeStream(picStream, null, options);
			int DefDpi = getResources().getDisplayMetrics().densityDpi;
			android.view.ViewGroup.LayoutParams imagelay = ivUploadPic.getLayoutParams();
			if (DefDpi == DisplayMetrics.DENSITY_LOW) {
				imagelay.height = (int) UnitUtil.dip2px(120);

			} else if (DefDpi == DisplayMetrics.DENSITY_MEDIUM) {
				imagelay.height = (int) UnitUtil.dip2px(170);
			} else {
				imagelay.height = (int) UnitUtil.dip2px(210);

			}
			
			ivUploadPic.setLayoutParams(imagelay);
			ivUploadPic.postInvalidate();
			ivUploadPic.setImageBitmap(mBitmap);
		} catch (Exception e) {
			DialogUtil.showToast(this, getString(R.string.text_info_upload_cant_show));
			Log.e("载入图片出错", "" + e.getMessage(), e);
			super.finish();
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

	// 调整初始化页面显示状态
	private void initPageState() {
		if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {
			layoutPriceAndUnit.setVisibility(View.VISIBLE);
			btnSelectFoodLayout.setVisibility(View.VISIBLE);
		} else {
			layoutPriceAndUnit.setVisibility(View.GONE);
			btnSelectFoodLayout.setVisibility(View.GONE);
		}
	}

	// 调整页面显示状态
	private void adjustPageState() {
		if (Settings.UPLOAD_TYPE_FOOD.equals(uploadType)) {
			layoutPriceAndUnit.setVisibility(View.VISIBLE);
			btnSelectFoodLayout.setVisibility(View.VISIBLE);
			getTvTitle().setText(restName);
			editLayout.startAnimation(mAnim);
			tabLayout0.setVisibility(View.VISIBLE);
			tabLayout1.setVisibility(View.GONE);
			
			etSelectFood.setText(foodName); // 设置菜品名称
			// 第一次进入时的朦皮，只有菜品时显示，（38版去掉）
			//DialogUtil.showVeilPictureOnce(this, R.drawable.new_upload_picture, "ShowOnceVeil_RestaurantUploadActivity");

		} else {
			layoutPriceAndUnit.setVisibility(View.GONE);
			btnSelectFoodLayout.setVisibility(View.GONE);
			// foodName = "";
			getTvTitle().setText(restName);
			editLayout.startAnimation(mAnim);
			tabLayout0.setVisibility(View.GONE);
			tabLayout1.setVisibility(View.VISIBLE);
			
		}

	}

	@Override
	protected void onResume() {
		if(RestaurantUploadActivity.goingToOriginalPage){
			RestaurantUploadActivity.goingToOriginalPage = false;
			comments.setText("");
		}
		super.onResume();

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// 设置微博的状态
		this.setShareState();

		// 载入图片
		
		loadPicture();
		//Log.d("uploadActivity------",Settings.uploadPictureOrignalActivityClazz+"");
	}

	@Override
	protected void onPause() {
		super.onPause();
		recycle();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 直接
		if (RestaurantUploadActivity.goingToOriginalPage) {
			RestaurantUploadActivity.goingToOriginalPage = false;
			super.finish();
			return;
		}
		if (data != null && data.getSerializableExtra("sinaUser") != null) {

			User user = (User) data.getSerializableExtra("sinaUser");
			insertAtSinaWeiboUser(user);
			return;
		}
		// 选择菜名页回来的
		if (data != null) {
			String foodId = data.getStringExtra(Settings.BUNDLE_UPLOAD_FOOD_ID);// 菜品ID
			String foodName = data.getStringExtra(Settings.BUNDLE_UPLOAD_FOOD_NAME);// 菜品名称
			this.foodId = foodId;
			this.foodName = foodName;
			etSelectFood.setText(foodName);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void insertAtSinaWeiboUser(User user) {
		// 保存文本的选择当前状态
		final Editable text = comments.getText();
		final int start = comments.getSelectionStart();
		final int end = comments.getSelectionEnd();
		final boolean isFocused = comments.isFocused();
		final boolean hasSelection = comments.hasSelection();

		if (user == null || CheckUtil.isEmpty(user.getName())) {

			return;
		}
		String result = "@" + user.getName() + " ";
		// 没有焦点时，获取焦点，并且自动设置为追加模式。
		if (!isFocused) {
			comments.requestFocus();
		}
		// ----
		// 插入模式
		int oldLength = comments.getText().length();
		int newLength = oldLength + result.length();
		int realInsertedLength = result.length();
		text.insert(end, result);
		comments.setText(text);
		// 真正插入的字符数
		int newRealLength = comments.getText().length();
		if (newRealLength != newLength) {

			realInsertedLength = newRealLength - oldLength;
		}
		// 光标重新定位
		comments.setSelection(end + realInsertedLength);

	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		if (mBitmap != null) {
			// 回收内存
			ivUploadPic.setImageBitmap(null);
			mBitmap.recycle();
			System.gc();
		}
	}

	/**
	 * 构建分享参数
	 * 
	 * @return
	 */
	private String getShareString() {
		StringBuffer sbShare = new StringBuffer();
		if (chbShareToSina.isChecked()) {
			sbShare.append("sina:1");
		} else {
			sbShare.append("sina:0");
		}
		
		sbShare.append(";");
		
		if (chbShareToTencent.isChecked()){
			sbShare.append("qq:1");
		}else{
			sbShare.append("qq:0");
		}
		return sbShare.toString();
	}

	/**
	 * 获取上传图片所需数据
	 */
	private void executeGetDataTask() {
		ServiceRequest request = new ServiceRequest(API.getUploadData);
		CommonTask.request(request, new CommonTask.TaskListener<UploadData>(){
			@Override
			protected void onSuccess(UploadData dto) {
				data = dto;
				adjustPageState();
			}
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				finish();
			}
//			@Override
//			protected void onCancel() {
//				super.onCancel();
//				recycle();
//				finish();
//			}
		});
	}

	/**
	 * 设置分享到微博的状态
	 */
	protected void setShareState() {
		
		UserInfoDTO userInfoDto = SessionManager.getInstance().getUserInfo(this);
		
		// 设置分享到新浪微博的状态
		if (userInfoDto != null && userInfoDto.isSinaBindTag()
				&& !userInfoDto.isSinaWeiboExpired()
				&& SessionManager.getInstance().isUserLogin(this)) {
			isSinaWeiboBinded = true;
			chbShareToSina.setChecked(true);
		} else {
			isSinaWeiboBinded = false;
			chbShareToSina.setChecked(false);
		}
		
		// 设置分享到腾讯微博的状态
		if (userInfoDto != null && userInfoDto.isQqBindTag()
				&& !userInfoDto.isQQWeiboExpired()
				&& SessionManager.getInstance().isUserLogin(this)) {
			isTencentWeiboBinded = true;
			chbShareToTencent.setChecked(true);
		} else {
			isTencentWeiboBinded = false;
			chbShareToTencent.setChecked(false);
		}
	}
}
