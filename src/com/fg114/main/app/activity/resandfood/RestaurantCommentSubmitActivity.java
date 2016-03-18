package com.fg114.main.app.activity.resandfood;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SelectMultiplePictureActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.data.ImageData;
import com.fg114.main.app.listener.OnProcessPictureListener;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.view.MySpinner;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.PostCommentAndPicturesTask;
import com.fg114.main.service.task.ProcessPictureTask;

import com.fg114.main.service.task.WebboTask;
import com.fg114.main.speech.asr.OnFinishListener;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.speech.asr.RecognitionResult;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboContentBuilder;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.FriendSelectionActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;
import com.fg114.main.weibo.dto.User;

/**
 * 餐厅评论界面
 * 
 * 1：从餐馆评论页进去 2：从订单页进去 3：从随手拍进去 菜品 4：从随手拍进去 餐馆 Settings.FROM_TAG
 * 
 * 回复 需要Settings.BUNDLE_REST_COMMENT_DATA
 * 
 * @author zhangyifan
 */
public class RestaurantCommentSubmitActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantCommentSubmitActivity";
	private boolean debug = false;
	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 66; // 图片压缩率
	private static final int MAX_SIDE_OF_BITMAP = 700; // 图片的最大边长限制
	public static String[] pictureUuids; // 图片的uuid，全局的目的是为了: 图片上传不成功时可以重试剩下的
	public static int index = -1; // 记录当前将要放入pictureUuid数组的索引

	// 传入参数
	private String restaurantId; // 餐厅信息
	private String orderId; // 订单信息
	private long postTag;
	private String foodId;

	// 画面变量
	boolean likeType = true;
	boolean isLikeTypeSelected = false;
	int tasteNum = 0;
	int envNum = 0;
	int serviceNum = 0;
	String content;
	String restaurantName;
	double longitude;
	double latitude;

	// 界面组件
	private LayoutInflater mInflater;
	private LinearLayout scoreLayout;
	private LinearLayout commentLayout;
	private RadioGroup rgLikeType;
	private View contextView;
	private RatingBar tasteRatingBar;
	private RatingBar envRatingBar;
	private RatingBar serviceRatingBar;
	private EditText etComment;
	private Button uploadPicture;
	private LinearLayout uploadPictureLayout;
	private Button btSubmit;
	private Button atSinaWeibo;
	private Button commentVoiceInputButton;
	private LinearLayout imagesLayout;
	private LinearLayout messageLayout;
	private TextView extraMessage;
	private ToggleButton chbShareToSina;
	private ToggleButton chbShareToTX;

	private Handler mUploadHandler = new Handler();
	private Uri tempPath;
	// 任务
	private String sharePicUrl = "";
	private String commentUuid = "";
	private UserInfoDTO infoDTO;
	private boolean isSINAWBbinding;
	private boolean isQQWBbinding;
	private CommentData commentData;
	private String commentId;

	
	private ArrayList<String> imageDataList = new ArrayList<String>();
	//添加或者修改图片时，临时图片存放目录
	String targetPicPath = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY + File.separator;
	// 记录拍照的文件名生成因子，每次一个新进入页面都重新从1开始，为了防止上传的临时图片泛滥，后生成的图片会重用以前的文件名
	int fileNameCount = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// 回复
		if (bundle.containsKey(Settings.BUNDLE_REST_COMMENT_DATA)) {
			commentData = (CommentData) bundle.getSerializable(Settings.BUNDLE_REST_COMMENT_DATA);
		}

		if (commentData != null) {
			// ----------------------------
			OpenPageDataTracer.getInstance().enterPage("餐厅评论回复表单", "");
			// ----------------------------
		} else {
			// ----------------------------
			OpenPageDataTracer.getInstance().enterPage("餐厅评论表单", "");
			// ----------------------------
		}

		if (bundle.containsKey(Settings.FROM_TAG)) {
			postTag = bundle.getLong(Settings.FROM_TAG);
		} else {
			DialogUtil.showToast(RestaurantCommentSubmitActivity.this, "软件出现点小错误");
			finish();
			return;
		}

		if (bundle.containsKey(Settings.BUNDLE_KEY_ID)) {
			restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		} else {
			DialogUtil.showToast(RestaurantCommentSubmitActivity.this, "软件出现点小错误");
			finish();
			return;
		}

		if (bundle.containsKey(Settings.BUNDLE_FOOD_ID)) {
			foodId = bundle.getString(Settings.BUNDLE_FOOD_ID);
		}

		if (bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		}

		if (bundle.containsKey(Settings.BUNDLE_REST_NAME)) {
			restaurantName = bundle.getString(Settings.BUNDLE_REST_NAME);
		}

		// 缓存数据获得
		RestInfoData info = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);
		longitude = info.longitude;
		latitude = info.latitude;
		if (info.name != null) {
			restaurantName = info.name;
		}
		infoDTO = SessionManager.getInstance().getUserInfo(this);
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
		if (commentData != null) {
			// ----------------------------
			OpenPageDataTracer.getInstance().enterPage("餐厅评论回复表单", "");
			// ----------------------------
		} else {
			// ----------------------------
			OpenPageDataTracer.getInstance().enterPage("餐厅评论表单", "");
			// ----------------------------
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		infoDTO = SessionManager.getInstance().getUserInfo(this);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		reloadPicture();
		// --------------------------------------------------------------
		// 如果未登录，显示登录按钮
		if (!SessionManager.getInstance().isUserLogin(this)) {
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText("登录");
			this.getBtnOption().setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bund = new Bundle();
					ActivityUtil.jump(RestaurantCommentSubmitActivity.this, UserLoginActivity.class, 0, bund);
				}
			});
		} else {
			this.getBtnOption().setVisibility(View.INVISIBLE);
		}

		if (infoDTO.isSinaBindTag() && !infoDTO.isSinaWeiboExpired()) {
			// 绑定没有过期
			chbShareToSina.setBackgroundResource(R.drawable.sina_check_weibo);
			chbShareToSina.setChecked(false);
			isSINAWBbinding = true;

		} else {
			// 绑定无效
			chbShareToSina.setBackgroundResource(R.drawable.sina_web_check);
			isSINAWBbinding = false;
		}
		if (infoDTO.isQqBindTag() && !infoDTO.isQQWeiboExpired()) {
			chbShareToTX.setBackgroundResource(R.drawable.tx_check_weibo);
			chbShareToTX.setChecked(false);
			isQQWBbinding = true;
		} else {
			// 绑定无效
			chbShareToTX.setBackgroundResource(R.drawable.tx_web_check);
			isQQWBbinding = false;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		recycle();
		PostCommentAndPicturesTask.pictureUuids = null;// 清除uuids
	}

	// 防止用户误操作
	@Override
	public void finish() {
		content = etComment.getText().toString().trim();
		if (!CheckUtil.isEmpty(content)) {
			// 提示是否放弃评论内容
			DialogUtil.showAlert(RestaurantCommentSubmitActivity.this, true, getString(R.string.text_dialog_comment_finish), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finishThis();
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

		} else {
			finishThis();
		}

	}

	private void finishThis() {
		super.finish();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(restaurantName);
		this.getBtnGoBack().setText("返回");
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_comment_submit, null);
		// ---评分层
		scoreLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_comment_score_layout);
		// ---评论内容层
		commentLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_comment_comment_layout);
		// ---
		rgLikeType = (RadioGroup) contextView.findViewById(R.id.restaurant_comment_like_type);
		tasteRatingBar = (RatingBar) contextView.findViewById(R.id.taste_ratingBar);
		envRatingBar = (RatingBar) contextView.findViewById(R.id.environment_ratingBar);
		serviceRatingBar = (RatingBar) contextView.findViewById(R.id.service_ratingBar);
		etComment = (EditText) contextView.findViewById(R.id.restaurant_comment_submit_etComment);
		uploadPicture = (Button) contextView.findViewById(R.id.restaurant_comment_submit_upload_image);
		btSubmit = (Button) contextView.findViewById(R.id.restaurant_comment_submit_btnUpLoad);
		imagesLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_comment_submit_images_layout);
		messageLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_comment_submit_message_mask);
		extraMessage = (TextView) contextView.findViewById(R.id.upload_message_extra);
		chbShareToSina = (ToggleButton) contextView.findViewById(R.id.restaurant_comment_submit_chkShareSina);
		chbShareToTX = (ToggleButton) contextView.findViewById(R.id.restaurant_comment_submit_chkShareTX);
		uploadPictureLayout = (LinearLayout) contextView.findViewById(R.id.restaurant_comment_submit_upload_image_layout);

		if (commentData != null) {
			uploadPictureLayout.setVisibility(View.GONE);
		}

		// TODO
		chbShareToSina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isSINAWBbinding) {
					// 如果sina绑定了就开始分享
					// chbShareToSina.setChecked(true);
				} else {
					// 开始绑定
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) { // 绑定成功
								isSINAWBbinding = true;
							} else {
								isSINAWBbinding = false;
								// doTest_sina();
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).requestWeiboShare(null);
				}
			}
		});

		chbShareToTX.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 绑定微博
				if (isQQWBbinding) {
					// 腾讯微博绑定了
					// Toast.makeText(RestaurantCommentSubmitActivity.this,
					// "true", 500).show();
				} else {
					// Toast.makeText(RestaurantCommentSubmitActivity.this,
					// "false", 500).show();
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) {
								isQQWBbinding = true;
							} else {
								isQQWBbinding = false;
								// doTest_tencent();
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO).requestWeiboShare(null);
				}

			}
		});

		// 先隐藏评分层，评论提交成功后隐藏评论层，显示评分层，进行评分。即，分两步来
		scoreLayout.setVisibility(View.GONE);
		commentLayout.setVisibility(View.VISIBLE);
		// 语音录入 ------------------------added by xu jianjun, 2011-12-15

		commentVoiceInputButton = (Button) contextView.findViewById(R.id.restaurant_comment_voice_button);
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {
			eng.bindButtonAndEditText(commentVoiceInputButton, etComment);
		}
		// 设置是否喜欢事件
		rgLikeType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String tag = group.findViewById(group.getCheckedRadioButtonId()).getTag().toString();
				likeType = Integer.parseInt(tag) == 1 ? true : false;
				isLikeTypeSelected = true;
			}
		});

		// 口味
		tasteRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				tasteNum = (int) rating;
			}
		});
		// 环境
		envRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				envNum = (int) rating;
			}
		});
		// 服务
		serviceRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				serviceNum = (int) rating;
			}
		});

		// 获取评分列表
		ArrayList<String> scoreList = new ArrayList<String>();
		String[] scoreArray = getResources().getStringArray(R.array.score_list);
		// 获取XML中定义的评分数组
		for (int i = 0; i < scoreArray.length; i++) {
			scoreList.add(scoreArray[i]);
		}
		// 提交按钮
		btSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 回复
				if (commentData != null) {
					if (checkInput()==false) {
						DialogUtil.showToast(RestaurantCommentSubmitActivity.this, "请输入评价内容");
						return;
					}
					executePostCommentReplyTask();
				} else if (scoreLayout.getVisibility() == View.GONE) {
					//判读是否输入文本，如果有文本直接提交
					//如果没有文本，判断是否有图片，如果有图片会出提示
					if(checkInput()==true){
						try {
							executePostCommentTask();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else{
						//没有文本判断是否有图片
						if(imagesLayout.getChildCount() ==0 ){
							DialogUtil.showToast(RestaurantCommentSubmitActivity.this, "请输入评价内容");
							return;
						}
						
						DialogUtil.showAlert(RestaurantCommentSubmitActivity.this, true, "提示", "您没有输入任何评价，确定要提交吗？", "确认", "返回", new DialogInterface.OnClickListener() {
							// 无评价有图点击确认继续
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									executePostCommentTask();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, new DialogInterface.OnClickListener() {
							// 返回
							@Override
							public void onClick(DialogInterface dialog, int which) {
									
							}
						});							
					}
										
					//图片改成8张之前，必须填写评价才能提交
					/*try {
						executePostCommentTask();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				} else {
					executePostCommentScoreTask();
				}
			}
		});
		// at新浪微博好友选择页面
		/*
		 * atSinaWeibo.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { //
		 * 逻辑：如果“分享新浪微博”没打勾，则帮它打勾，如果打勾了，则直接跳转到“选择新浪好友页面” // 首先检查是否登录，如果未登录则先去登录
		 * if (!chbShareToSina.isChecked()) { chbShareToSina.setChecked(true);
		 * // 判断，如果已经绑定了，直接跳转到选择好友 UserInfoDTO userInfo =
		 * SessionManager.getInstance().getUserInfo(
		 * RestaurantCommentSubmitActivity.this); if (userInfo.isSinaBindTag()
		 * && !userInfo.isSinaWeiboExpired()) { Bundle bundle = new Bundle();
		 * ActivityUtil.jump(RestaurantCommentSubmitActivity.this,
		 * FriendSelectionActivity.class, 0, bundle); } } else { UserInfoDTO
		 * userInfo = SessionManager.getInstance().getUserInfo(
		 * RestaurantCommentSubmitActivity.this); if (userInfo.isSinaBindTag()
		 * && !userInfo.isSinaWeiboExpired()) { Bundle bundle = new Bundle();
		 * ActivityUtil.jump(RestaurantCommentSubmitActivity.this,
		 * FriendSelectionActivity.class, 0, bundle); } } } });
		 */
		// 上传按钮
		uploadPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (imagesLayout.getChildCount() >= 8) {
					DialogUtil.showToast(RestaurantCommentSubmitActivity.this, "一次评论最多只能上传8张图片!");
					return;
				}
				
				//如果文本有焦点必须先清除，否则键盘会挡住选择图片的
				//InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				//imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
				ViewUtils.hideSoftInput(RestaurantCommentSubmitActivity.this, etComment);
				//本地上传多张图片调用
				takeBatchPicture();
				//本地单张图片调用
//				capturePicture();
			}
		});

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void capturePicture() {
		if (ActivityUtil.checkMysoftStage(this)) {
//			Bundle data=new Bundle();
//			data.putInt(SelectMultiplePictureActivity.KEY_MAX_ALLOWED_COUNT, 8);
			
			ButtonPanelUtil pan = new ButtonPanelUtil();
			pan.showUploadPanel(this.contextView, this, null);
			pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener() {

				@Override
				public void onGetUri(Uri uri) {
					tempPath = uri;

				}
			});
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		String imagePath = null;
//		if ((requestCode == Settings.CAMERAIMAGE || requestCode == Settings.LOCALIMAGE)) {
//
//			// 先检查路径是否正确
//			String path = null;
//			if (data != null && data.getData() != null) {
//				path = parseImgPath(data.getData());
//			} else if (tempPath != null) {
//				path = parseImgPath(tempPath);
//			}
//
//			try {
//				if (CheckUtil.isEmpty(path)) {
//					DialogUtil.showToast(this, "没有选择任何图片");
//					return;
//				}
//				// 如果未拍照或选择了空图片
//				if (new File(path).length() == 0) {
//					getContentResolver().delete(tempPath, null, null);
//					return;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			// 如果是本地上传，data不为null
//			if (data != null && data.getData() != null) {
//				// imagePath = parseImgPath(data.getData());
////				addPicture(data.getData());
////				imageDataList.add(parseImgPath(data.getData()));
//				addMorePicture(parseImgPath(data.getData()));
//				
//
//			} else {
//				// 否则是拍照上传，判断拍照图片是否有效
//				try {
//					imagePath = parseImgPath(tempPath);
//					if (!CheckUtil.isEmpty(imagePath)) {
//						// 如果未拍照文件无效或者文件长度为０
//						File f = new File(imagePath);
//						if (f.exists()) {
//							if (f.length() == 0) {
//								// f.delete();
//								getContentResolver().delete(tempPath, null, null);
//							} else {
////								addPicture(tempPath);	
////								imageDataList.add(parseImgPath(tempPath));
//								addMorePicture(parseImgPath(tempPath));
//								tempPath = null;
//							}
//						}
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			return;
//		}
		// 如果是＠新浪好友页面回来的
		if (data != null && data.getSerializableExtra("sinaUser") != null) {

			User user = (User) data.getSerializableExtra("sinaUser");
			insertAtSinaWeiboUser(user);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void insertAtSinaWeiboUser(User user) {
		// 保存文本的选择当前状态
		final Editable text = etComment.getText();
		final int start = etComment.getSelectionStart();
		final int end = etComment.getSelectionEnd();
		final boolean isFocused = etComment.isFocused();
		final boolean hasSelection = etComment.hasSelection();

		String result = "@" + user.getName() + " ";
		if (user == null || CheckUtil.isEmpty(user.getName())) {

			return;
		}
		// 没有焦点时，获取焦点，并且自动设置为追加模式。
		if (!isFocused) {
			etComment.requestFocus();
		}
		// ----
		// 插入模式
		int oldLength = etComment.getText().length();
		int newLength = oldLength + result.length();
		int realInsertedLength = result.length();
		text.insert(end, result);
		etComment.setText(text);
		// 真正插入的字符数
		int newRealLength = etComment.getText().length();
		if (newRealLength != newLength) {

			realInsertedLength = newRealLength - oldLength;
		}
		// 光标重新定位
		etComment.setSelection(end + realInsertedLength);

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
					path = cursor.getString(index);
					cursor.close();
				}
			}
		}
		return path;
	}

	private FileInputStream input=null;
	private String prepareImageData(InputStream[] inputStream) throws IOException {
		Bitmap bmp = null;
		String imgSizeList = "";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		int currentSize = 0;
		int count = imagesLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			ImageView imageView = (ImageView) imagesLayout.getChildAt(i);
			bmp = (Bitmap) ((Object[]) imageView.getTag())[0];
			// 获得数据流
			// bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out);
			
			Object[] tag = (Object[]) imageView.getTag();
			String tempPath= ActivityUtil.getGPSPicturePath((String) tag[1]);
			out.write(bufferStreamForByte(tempPath));
			imgSizeList = imgSizeList + (out.size() - currentSize) + ((i == count - 1) ? "" : ";");
			currentSize = out.size();
		}
		inputStream[0] = new ByteArrayInputStream(out.toByteArray());
		return imgSizeList;
	}

	// 把临时的路径上的图片转换成io流的形式
	public static byte[] bufferStreamForByte(String spec) {
		byte[] content = null;
		try {
			BufferedInputStream bis = null;
			ByteArrayOutputStream out = null;
			try {
				FileInputStream input = new FileInputStream(spec);
				bis = new BufferedInputStream(input, 1024);
				byte[] bytes = new byte[1024];
				int len;
				out = new ByteArrayOutputStream();
				while ((len = bis.read(bytes)) > 0) {
					out.write(bytes, 0, len);
				}
				bis.close();
				content = out.toByteArray();
			} finally {
				if (bis != null)
					bis.close();
				if (out != null)
					out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private SimpleData getJson() {
		String json = "{	\"uuid\":\"1111\",	\"restUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"msg\":\"xxxxxxxx\",	\"errorCode\":\"101\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
		SimpleData dto = JsonUtils.fromJson(json, SimpleData.class);
		return dto;
	}

	/**
	 * 提交评论内容 第一次提交
	 * 
	 * @throws IOException
	 */
	private void executePostCommentTask() throws IOException {

		/*if (!checkInput()) {
			DialogUtil.showToast(this, "请输入评价内容");
			return;
		}*/
		SimpleData dto = null;
		ViewUtils.hideSoftInput(this, etComment);
		ServiceRequest request = new ServiceRequest(API.postComment);
		request.addData("restId", restaurantId);// 餐馆ID
		request.addData("picId", "");
		request.addData("postTag", postTag);// 1：从餐馆评论页进去 2：从订单页进去 3：从随手拍进去 菜品
											// 4：从随手拍进去 餐馆
		String foodIds = postTag == 3 ? foodId : "";
		request.addData("foodId", foodIds);// 菜品id 如果 postTag:3 foodId不能为空
											// postTag:其他 为空 postTag:其他 为空

		String orderIds = postTag == 2 ? orderId : "";
		request.addData("orderId", orderIds);// 订单id 如果 postTag:2 orderId不能为空
												// postTag:其他 为空
		String imgSizeList = "";
		InputStream[] pic = new InputStream[1];
		imgSizeList = prepareImageData(pic);
		try {
			if (pic[0] == null || pic[0].available() == 0) {// 由于后台问题，如果没有图片的时候，至少要放一个字节
				pic[0] = new ByteArrayInputStream(new byte[] { (byte) 0 });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		request.addData(pic[0]);// picId为uploadImage2返回的图片或者为空
		request.addData("imgSizeList", imgSizeList);// picId为uploadImage2返回的图片或者为空
		// picId可以是多个 111;222;333
		request.addData("overallNum", 0);// 总体评价 0为未选择
		request.addData("tasteNum", 0);// 口味 0为未选择
		request.addData("envNum", 0);// 环境 0为未选择
		request.addData("serviceNum", 0);// 服务 0为未选择
		request.addData("detail", content);// 评论内容 postTag=3时传递图片描述 可以为空
											// postTag=其他 不能为空
		request.addData("shareTo", getShareString());// 分享到微博 sina:1;qq:0
														// 当前只有sina:1 或者
														// sina:0
														// 如果客户端这个字段传递为空
														// ，容错处理为不分享到任何平台
		// -----
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----
		CommonTask.request(request, "正在提交，请稍后...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
				DialogUtil.showAlert(RestaurantCommentSubmitActivity.this, true, "信息", dto.getMsg(), "给餐厅打个分吧", "返回", new DialogInterface.OnClickListener() {
					// 评分
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 第二步：提交评分
						// 隐藏评论层，隐藏键盘，显示评分层，清空评论内容
						commentId = dto.getUuid();
						// Log.e(TAG, commentId + "");
						etComment.setText("");
						scoreLayout.setVisibility(View.VISIBLE);
						commentLayout.setVisibility(View.GONE);
						// recycle();
					}
				}, new DialogInterface.OnClickListener() {
					// 返回
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 回复成功后，设置全局标志，当返回餐厅详情时要刷新评论列表
						Settings.NEED_REFRESH_REST_COMMENT = true;
						Settings.COMMENT_RES_ID = restaurantId;
						
						
						etComment.setText("");
						finishThis();
					}
				});
			};

			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
				etComment.setText("");
				DialogUtil.showToast(RestaurantCommentSubmitActivity.this, message + "");
				finishThis();

			};
		});
	}

	public String[] pictureUrls; // 图片的url地址，公开为public供分享微薄时选用
	volatile boolean deletionIsComplished = true;

	/**
	 * 提交评分
	 */
	private void executePostCommentScoreTask() {
		// Log.e(TAG, "commentUuid:" + commentUuid + " likeType:" + likeType +
		// " tasteNum:" + tasteNum + "serviceNum:"
		// + serviceNum);
		if (!checkInputScore()) {
			return;
		}
		SimpleData dto = null;
		ServiceRequest request = new ServiceRequest(API.postCommentScore);
		request.addData("commentId", commentId);// 评论id
		request.addData("likeTag", likeType);// 是否喜欢
		request.addData("tasteNum", tasteNum);// 口味 0为未选择
		request.addData("envNum", envNum);// 环境 0为未选择
		request.addData("serviceNum", serviceNum);// 服务 0为未选择
		CommonTask.request(request, "正在提交，请稍后...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				DialogUtil.showToast(getApplicationContext(), "提交评分成功！");
				// 评论成功后，设置全局标志，当返回餐厅详情时要刷新评论列表
				Settings.NEED_REFRESH_REST_COMMENT = true;
				Settings.COMMENT_RES_ID = restaurantId;

				finishThis();
			};

			protected void onError(int code, String message) {
				DialogUtil.showToast(RestaurantCommentSubmitActivity.this, message);
			};
		});
	}

	/**
	 * 提交回复
	 */
	private void executePostCommentReplyTask() {

		/*if (!checkInput()) {
			DialogUtil.showToast(this, "请输入评价内容");
			return;
		}*/
		SimpleData dto = null;
		ViewUtils.hideSoftInput(this, etComment);
		// uploadPicture
		ServiceRequest request = new ServiceRequest(API.postCommentReply);
		request.addData("postTag", postTag);// 提交类型 1:添加 2:更新
		request.addData("commentId", commentData.uuid);// 评论id
		String replyId = postTag == 1 ? "" : commentData.uuid;
		request.addData("replyId", replyId);// 回复的id postTag:1时为空 postTag:2时为空
											// 对应的回复id
		request.addData("detail", content);// 内容 1~1000字
		request.addData("shareTo", getShareString());
		// -----
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----
		CommonTask.request(request, "", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
				// 回复成功后，设置全局标志，当返回餐厅详情时要刷新评论列表
				Settings.NEED_REFRESH_REST_COMMENT = true;
				Settings.COMMENT_RES_ID = restaurantId;
				
				
				etComment.setText("");
				recycle();
				finish();
				return;
			};

			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				etComment.setText("");
				finishThis();

			};
		});
	}

	// 设置遮罩层的附加文字
	public void setUploadMessage(final String msg) {
		mUploadHandler.post(new Runnable() {

			@Override
			public void run() {
				extraMessage.setText(msg);
			}
		});
	}

	// 显示遮罩层
	public void showUploadMask() {
		mUploadHandler.post(new Runnable() {

			@Override
			public void run() {
				messageLayout.setVisibility(View.VISIBLE);
				messageLayout.requestFocus();
			}
		});
	}

	// 隐藏遮罩层
	public void hideUploadMask() {
		mUploadHandler.post(new Runnable() {

			@Override
			public void run() {
				messageLayout.setVisibility(View.GONE);
				extraMessage.setText("");
			}
		});
	}

	/**
	 * check
	 */
	private boolean checkInput() {
		content = etComment.getText().toString().trim();
		if (CheckUtil.isEmpty(content)) {
			//ViewUtils.setError(etComment, "请输入评价内容");	
			etComment.requestFocus();
			return false;
		}
		content = etComment.getText().toString();
		// if (content.length() < 10) {
		// ViewUtils.setError(etComment, "点评请至少输入10个字");
		// etComment.requestFocus();
		// return false;
		// }
		if (content.length() > 200) {
			content=content.substring(0, 200);
			etComment.setText(content);
			//ViewUtils.setError(etComment, "您点评内容过长，点评不能超过200字");
			//etComment.requestFocus();
			//return false;
		}
		return true;
	}

	// 检查评分输入合法性
	private boolean checkInputScore() {
		// 全部不填，或者全部都填，只针对评分

		if (!isLikeTypeSelected) {
			DialogUtil.showToast(this, "请选择是否喜欢");
			return false;
		}

		if (tasteNum == 0) {
			DialogUtil.showToast(this, "请选择口味");
			return false;
		}

		if (envNum == 0) {
			DialogUtil.showToast(this, "请选择环境");
			return false;
		}

		if (serviceNum == 0) {
			DialogUtil.showToast(this, "请选择服务");
			return false;
		}

		return true;
	}

//	// 添加一张图片
//	private void addPicture(final Uri imagePath) {
//
//		// ---修改添加小图的大小和间距，最早的时候是30，后来图片增加到8张，大小改成26
//		final ImageView img = new ImageView(this);
//		LayoutParams params = new LayoutParams(UnitUtil.dip2px(26), UnitUtil.dip2px(26));
//		params.setMargins(UnitUtil.dip2px(1), UnitUtil.dip2px(2), UnitUtil.dip2px(1), UnitUtil.dip2px(2));
//		// img.setBackgroundColor(0x11000000);
//		img.setBackgroundResource(R.drawable.shape_rectangle_gray_with_border);
//		img.setLayoutParams(params);
//		img.setPadding(UnitUtil.dip2px(2), UnitUtil.dip2px(2), UnitUtil.dip2px(2), UnitUtil.dip2px(2));
//		img.setScaleType(ScaleType.FIT_CENTER);
//		// --
//		Bitmap bmp = getBitmap(parseImgPath(imagePath));
//		if (bmp == null) {
//			return;
//		}
//		img.setTag(new Object[] { scaleBitmap(bmp), imagePath });
//		img.setImageBitmap((Bitmap) ((Object[]) img.getTag())[0]);
//
//		img.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				DialogUtil.showDialog(RestaurantCommentSubmitActivity.this, R.layout.dialog_comment_submit_view_picture, new DialogUtil.DialogEventListener() {
//
//					@Override
//					public void onInit(View contentView, final PopupWindow dialog) {
//						final ImageView image = (ImageView) contentView.findViewById(R.id.dialog_comment_submit_image);
//						image.setImageBitmap((Bitmap) ((Object[]) img.getTag())[0]);
//						// 取消按钮
//						contentView.findViewById(R.id.main_frame_btnGoBack).setOnClickListener(new OnClickListener() {
//
//							@Override
//							public void onClick(View v) {
//								image.setImageBitmap(null); // 只设空，不回收，因为和上一页面使用的是同一个bitmap
//								dialog.dismiss();
//
//							}
//						});
//						// 删除按钮
//						contentView.findViewById(R.id.main_frame_btnOption).setOnClickListener(new OnClickListener() {
//
//							@Override
//							public void onClick(View v) {
//								image.setImageBitmap(null);
//								RestaurantCommentSubmitActivity.this.deletePicture(img);
//								dialog.dismiss();
//							}
//						});
//
//					}
//				});
//
//			}
//		});
//		// imagesLayout.addView(img,UnitUtil.dip2px(50),UnitUtil.dip2px(50));
//		imagesLayout.addView(img, 0);
//	}

	// 删除一张图片
	public void deletePicture(ImageView image) {
		if (image == null || imagesLayout.getChildCount() <= 0) {
			return;
		}
		image.setImageBitmap(null);
		Bitmap bmp = (Bitmap) ((Object[]) image.getTag())[0];
		bmp.recycle();
		image.setTag(null);
		imagesLayout.removeView(image);
		// ---
	}



	
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
			// picStream =
			// contentResolver.openInputStream(Uri.parse(mImageUri));
			picStream = new FileInputStream(path);
			Bitmap bmp = BitmapFactory.decodeStream(picStream, null, options);
			return bmp;
		} catch (Exception e) {
			DialogUtil.showToast(this, getString(R.string.text_info_upload_cant_show));
			// Log.e("载入图片出错", "" + e.getMessage(), e);
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

	private Bitmap scaleBitmap(Bitmap old) {

		// 没有超过边长，直接返回
		int height = old.getHeight();
		int width = old.getWidth();
		if (width <= MAX_SIDE_OF_BITMAP && height <= MAX_SIDE_OF_BITMAP) {
			return old;
		}
		// 按比例缩小到MAX_SIDE_OF_BITMAP

		// 缩放比例
		float scale = 1;
		if (height > width) {

			scale = MAX_SIDE_OF_BITMAP / ((float) height);
		} else {
			scale = MAX_SIDE_OF_BITMAP / ((float) width);
		}
		try {
			return Bitmap.createScaledBitmap(old, (int) (width * scale), (int) (height * scale), false);

		} finally {
			if (old != null) {
				old.recycle();
				old = null;
			}
		}
	}

	private void recycle() {
		int count = imagesLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			ImageView temp = (ImageView) imagesLayout.getChildAt(i);
			Object[] tag = (Object[]) temp.getTag();
			temp.setImageBitmap(null);
			((Bitmap) tag[0]).recycle();
			tag[0] = null;
		}
	}

	private void reloadPicture() {
		int count = imagesLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			ImageView temp = (ImageView) imagesLayout.getChildAt(i);
			Object[] tag = (Object[]) temp.getTag();
			//上传单张图片用
//			Bitmap bmp = scaleBitmap(getBitmap(parseImgPath((Uri) tag[1])));
			//上传多张图片用
			Bitmap bmp = scaleBitmap(getBitmap((String)tag[1]));
			temp.setImageBitmap(bmp);
			tag[0] = bmp;
		}
	}

	/**
	 * 构建分享参数
	 * 
	 * @return
	 */
	// TODO
	private String getShareString() {
		StringBuffer sbShare = new StringBuffer();
		if (chbShareToSina.isChecked()) {
			sbShare.append("sina:1;");
		} else {
			sbShare.append("sina:0;");
		}
		if (chbShareToTX.isChecked()) {
			sbShare.append("qq:1");
		} else {
			sbShare.append("qq:0");
		}

		return sbShare.toString();
	}
	
	
	
	
	//添加图片，批量添加
	private void takeBatchPicture() {
		//控制最大可选择数量
		int maxAllowedCount=8-imageDataList.size();
		Bundle data=new Bundle();
		data.putInt(SelectMultiplePictureActivity.KEY_MAX_ALLOWED_COUNT, maxAllowedCount);
		//
		takeBatchPic(new OnShowUploadImageListener() {
			
			//添加单张
			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				
				if (!CheckUtil.isEmpty(path)) {
					String tempPath = ActivityUtil.getGPSPicturePath(path, targetPicPath + "tempRecommendPic" + fileNameCount++);
					com.fg114.main.app.Settings.uploadPictureUri = "";
					imageDataList.add(tempPath);
					addMorePicture(tempPath);
//					adapter.notifyDataSetChanged();
				}
			}
			//添加批量
			@Override
			public void onGetBatchPic(ArrayList<String[]> picture_data_selected) {
				if (picture_data_selected==null || picture_data_selected.size()==0) {
					return;
				}
				new ProcessPictureTask("正在处理图片...", 
						RestaurantCommentSubmitActivity.this,
						picture_data_selected,
						new OnProcessPictureListener() {
					
					@Override
					public void onProcessPicture(String[] picture_data) {
						String tempPath = ActivityUtil.getGPSPicturePath(picture_data[1], targetPicPath + "tempRecommendPic" + fileNameCount++);
						imageDataList.add(tempPath);
//						addMorePicture(tempPath);
					}
				}).execute(new Runnable() {
					
					@Override
					public void run() {
						//成功后刷新列表
//						adapter.notifyDataSetChanged();
//						list_view.setSelection(imageDataList.size() - 1);
						imagesLayout.removeAllViews();
						for(int i=0;i<imageDataList.size();i++){
							addMorePicture(imageDataList.get(i));
						}
					}
				});
					
			}
		},data);
	}
//	private void takePicture(final ImageData data) {
//		
//		takePic(new OnShowUploadImageListener() {
//
//			@Override
//			public void onGetPic(Bundle bundle) {
//				String path = com.fg114.main.app.Settings.uploadPictureUri;
//				if (!CheckUtil.isEmpty(path)) {
//					String tempPath = ActivityUtil.getGPSPicturePath(path, targetPicPath + "tempRecommendPic" + fileNameCount++);
//					com.fg114.main.app.Settings.uploadPictureUri = "";
//					data.imagePath = tempPath;
////					adapter.notifyDataSetChanged();// 刷新list
//				}
//			}
//
//		}, false);
//	}

	// 添加多张图片
	private void addMorePicture(final String imagePath) {

		// ---修改添加小图的大小和间距，最早的时候是30，后来图片增加到8张，大小改成26
		final ImageView img = new ImageView(this);
		LayoutParams params = new LayoutParams(UnitUtil.dip2px(26), UnitUtil.dip2px(26));
		params.setMargins(UnitUtil.dip2px(1), UnitUtil.dip2px(2), UnitUtil.dip2px(1), UnitUtil.dip2px(2));
		// img.setBackgroundColor(0x11000000);
		img.setBackgroundResource(R.drawable.shape_rectangle_gray_with_border);
		img.setLayoutParams(params);
		img.setPadding(UnitUtil.dip2px(2), UnitUtil.dip2px(2), UnitUtil.dip2px(2), UnitUtil.dip2px(2));
		img.setScaleType(ScaleType.FIT_CENTER);
		// --
		Bitmap bmp = getBitmap(imagePath);
		if (bmp == null) {
			return;
		}
		img.setTag(new Object[] { scaleBitmap(bmp), imagePath });
		img.setImageBitmap((Bitmap) ((Object[]) img.getTag())[0]);

		img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogUtil.showDialog(RestaurantCommentSubmitActivity.this, R.layout.dialog_comment_submit_view_picture, new DialogUtil.DialogEventListener() {

					@Override
					public void onInit(View contentView, final PopupWindow dialog) {
						final ImageView image = (ImageView) contentView.findViewById(R.id.dialog_comment_submit_image);
						image.setImageBitmap((Bitmap) ((Object[]) img.getTag())[0]);
						// 取消按钮
						contentView.findViewById(R.id.main_frame_btnGoBack).setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								image.setImageBitmap(null); // 只设空，不回收，因为和上一页面使用的是同一个bitmap
								dialog.dismiss();

							}
						});
						// 删除按钮
						contentView.findViewById(R.id.main_frame_btnOption).setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								image.setImageBitmap(null);
								RestaurantCommentSubmitActivity.this.deletePicture(img);
								imageDataList.remove(imagePath);
								dialog.dismiss();
							}
						});

					}
				});

			}
		});
		// imagesLayout.addView(img,UnitUtil.dip2px(50),UnitUtil.dip2px(50));
		imagesLayout.addView(img, 0);
	}
	

	// --------------------测试数据开始-------------------------
	private void doTest_sina() {
		String json = null;
		if (infoDTO.isQqBindTag()) {
			json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户_sina\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"true\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"true\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
		} else {
			json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户_sina\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"true\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"false\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
		}
		UserInfoDTO dto = JsonUtils.fromJson(json, UserInfoDTO.class);
		infoDTO.setUuid(dto.getUuid());
		infoDTO.setNickName(dto.getNickName());
		infoDTO.setTel(dto.getTel());
		infoDTO.setToken(dto.getToken());
		infoDTO.setPicUrl(dto.getPicUrl());
		infoDTO.setSexTag(dto.getSexTag());
		infoDTO.setPointNum(dto.getPointNum());
		infoDTO.setLevel(dto.getLevel());
		infoDTO.setSinaBindTag(dto.isSinaBindTag());
		infoDTO.setSinaAccount(dto.getSinaAccount());
		infoDTO.setSinaBindRemainSecs(4444444);
		infoDTO.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		infoDTO.setQqBindTag(dto.isQqBindTag());
		infoDTO.setQqAccount(dto.getQqAccount());
		infoDTO.setQqBindRemainSecs(4444444);
		infoDTO.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		SessionManager.getInstance().setUserInfo(RestaurantCommentSubmitActivity.this, infoDTO);
		SessionManager.getInstance().setIsUserLogin(RestaurantCommentSubmitActivity.this, true);
		isSINAWBbinding = true;
	}

	private void doTest_tencent() {
		String json = null;
		if (infoDTO.isSinaBindTag()) {
			json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户_tencent\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"true\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"true\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
		} else {
			json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户_tencent\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"false\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"true\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
		}
		UserInfoDTO dto = JsonUtils.fromJson(json, UserInfoDTO.class);
		infoDTO.setUuid(dto.getUuid());
		infoDTO.setNickName(dto.getNickName());
		infoDTO.setTel(dto.getTel());
		infoDTO.setToken(dto.getToken());
		infoDTO.setPicUrl(dto.getPicUrl());
		infoDTO.setSexTag(dto.getSexTag());
		infoDTO.setPointNum(dto.getPointNum());
		infoDTO.setLevel(dto.getLevel());
		infoDTO.setSinaBindTag(dto.isSinaBindTag());
		infoDTO.setSinaAccount(dto.getSinaAccount());
		infoDTO.setSinaBindRemainSecs(4444444);
		infoDTO.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		infoDTO.setQqBindTag(dto.isQqBindTag());
		infoDTO.setQqAccount(dto.getQqAccount());
		infoDTO.setQqBindRemainSecs(4444444);
		infoDTO.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		SessionManager.getInstance().setUserInfo(RestaurantCommentSubmitActivity.this, infoDTO);
		SessionManager.getInstance().setIsUserLogin(RestaurantCommentSubmitActivity.this, true);
		isQQWBbinding = true;
	}

	// ------------------------------------------------------------

}
