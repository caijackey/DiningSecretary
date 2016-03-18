package com.fg114.main.app.activity.resandfood;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.data.UploadDataPack;
import com.fg114.main.app.view.LineView;
import com.fg114.main.service.dto.UploadData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.UploadImageTask;
import com.fg114.main.service.task.WebboTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.util.DialogUtil.GeneralCallback;
import com.fg114.main.weibo.WeiboContentBuilder;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.FriendSelectionActivity;
import com.fg114.main.weibo.activity.MediatorActivity;
import com.fg114.main.weibo.dto.User;

/**
 * 图片上传最终确认界面
 * 
 * @author xujianjun, 2012-09-28
 * 
 */
public class RestaurantUploadConfirmActivity extends MainFrameActivity {

	private static final String TAG = RestaurantUploadConfirmActivity.class.getSimpleName();
	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 70; // 图片压缩率

	// 传入参数
	private String restId; // 餐厅ID
	private String restName;
	private String foodId;
	private String foodName;
	private String mImageUri; // 文件位置
	private Bitmap mBitmap; // 文件位置
	private int fromPage;

	// 界面组件
	private LayoutInflater inflater;
	private View contextView;

	private ImageView commentImage;
	private TextView commentText;
	private TextView commentMibiText;
	private LineView deleteLineComment;
	
	private ImageView uploadImage;
	private TextView uploadText;
	private TextView uploadMibiText;
	private LineView deleteLineUpload;
	
	private ImageView gradeImage;
	private TextView gradeText;
	private TextView gradeMibiText;
	private LineView deleteLineGrade;
	
	
	
	private Button confirmButton;

	private UploadDataPack uploadData;

	private Thread mUploadThread;
	private UploadImageTask uploadImageTask;

	private boolean submitAfterLogin; // 是否登录后直接提交
	int mibi = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		uploadData = SessionManager.uploadData;

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

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏

		getBtnGoBack().setText(R.string.text_button_back);
		getBtnGoBack().setVisibility(View.INVISIBLE);
		getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = inflater.inflate(R.layout.restaurant_upload_confirm, null);

		// ---
		commentImage = (ImageView) contextView.findViewById(R.id.upload_confirm_result_comment_image);
		uploadImage = (ImageView) contextView.findViewById(R.id.upload_confirm_result_upload_image);
		gradeImage = (ImageView) contextView.findViewById(R.id.upload_confirm_result_grade_image);
		//评论结果
		commentText = (TextView) contextView.findViewById(R.id.upload_confirm_result_comment_text);
		commentMibiText = (TextView) contextView.findViewById(R.id.upload_confirm_result_comment_mibi_text);
		deleteLineComment = (LineView) contextView.findViewById(R.id.delete_line_comment);
		//上传结果
		uploadText = (TextView) contextView.findViewById(R.id.upload_confirm_result_upload_text);
		uploadMibiText = (TextView) contextView.findViewById(R.id.upload_confirm_result_upload_mibi_text);
		deleteLineUpload = (LineView) contextView.findViewById(R.id.delete_line_upload);
		//打分结果
		gradeText = (TextView) contextView.findViewById(R.id.upload_confirm_result_grade_text);
		gradeMibiText = (TextView) contextView.findViewById(R.id.upload_confirm_result_grade_mibi_text);
		deleteLineGrade = (LineView) contextView.findViewById(R.id.delete_line_grade);
		
		//
		confirmButton = (Button) contextView.findViewById(R.id.upload_confirm);

		setView();
		// ---
		setFunctionLayoutGone();
		getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//		loadPicture();
		if (submitAfterLogin) {
			executeUploadImageTask();
			submitAfterLogin = false;
		}
		Log.d("uploadActivity------",Settings.uploadPictureOrignalActivityClazz+"");
	}

	@Override
	protected void onPause() {
		super.onPause();
		recycle();
		Log.e("onPause onPause onPause","onPause onPause");
	}

	private void setView() {
		getTvTitle().setText("获得秘币");
		// getTvTitle().setText(uploadData.restName);

		// 菜图片
		if (Settings.UPLOAD_TYPE_FOOD.equals(uploadData.uploadType)) {

			// 打分图标
			if (uploadData.foodScore == 0) {
				gradeImage.setImageResource(R.drawable.restaurant_upload_confirm_cross_mark);
				
				gradeText.setText("未对菜进行打分");
				gradeMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForGradeFood() + "</font>秘币"));
				deleteLineGrade.setVisibility(View.VISIBLE);
			} else {
				//mibi += uploadData.uploadData.getPointNumForGradeFood();
				gradeImage.setImageResource(R.drawable.restaurant_upload_confirm_check_mark);
				
				gradeText.setText("成功对菜进行打分");
				gradeMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForGradeFood() + "</font>秘币"));
				deleteLineGrade.setVisibility(View.GONE);
			}
		}// 环境图片没有打分
		else {
			// 打分图标
			if (uploadData.serviceNum == 0 || uploadData.envNum == 0 || uploadData.tasteNum == 0) {
				gradeImage.setImageResource(R.drawable.restaurant_upload_confirm_cross_mark);
				
				gradeText.setText("未对餐厅进行打分");
				gradeMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForGradeRest() + "</font>秘币"));
				deleteLineGrade.setVisibility(View.VISIBLE);
				
			} else {
				//mibi += uploadData.uploadData.getPointNumForGradeRest();
				gradeImage.setImageResource(R.drawable.restaurant_upload_confirm_check_mark);
				
				gradeText.setText("成功对餐厅进行打分");
				gradeMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForGradeRest() + "</font>秘币"));
				deleteLineGrade.setVisibility(View.GONE);
			}
		}

		// 评论
		if (CheckUtil.isEmpty(uploadData.comment)) {
			commentImage.setImageResource(R.drawable.restaurant_upload_confirm_cross_mark);
			
			commentText.setText("未输入点评内容");
			commentMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForComment() + "</font>秘币"));
			deleteLineComment.setVisibility(View.VISIBLE);
		} else {
			//mibi += uploadData.uploadData.getPointNumForComment();
			commentImage.setImageResource(R.drawable.restaurant_upload_confirm_check_mark);
			
			commentText.setText("成功发表点评");
			commentMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForComment() + "</font>秘币"));
			deleteLineComment.setVisibility(View.GONE);
		}
		// 上传
		uploadImage.setImageResource(R.drawable.restaurant_upload_confirm_check_mark);
		//mibi += uploadData.uploadData.getPointNumForUploadPic();
		
		uploadText.setText("成功上传图片");
		uploadMibiText.setText(Html.fromHtml("<font color=\"#FF0000\">+" + uploadData.uploadData.getPointNumForUploadPic() + "</font>秘币"));
		deleteLineUpload.setVisibility(View.GONE);

		// 提交
		confirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				submit();
			}

		});
	}

	// 提交
	private void submit() {
		// 如果未登录，提示: 是否登录，可得秘币
//		String message = "登录将获得 " + mibi + " 秘币";
		String message = "登录后才能获得秘币";
		if (!SessionManager.getInstance().isUserLogin(RestaurantUploadConfirmActivity.this)) {
			DialogUtil.showComfire(RestaurantUploadConfirmActivity.this, "请登录", message, new String[] { "立即登录", "不登录 继续" }, new Runnable() {
				@Override
				public void run() {// 登录
					DialogUtil.showUserLoginDialog(RestaurantUploadConfirmActivity.this, new Runnable() {

						@Override
						public void run() {
							submitAfterLogin = true;
						}
					}, 0);
				}
			}, new Runnable() {
				@Override
				public void run() {
					executeUploadImageTask();
				}
			});
		} else {
			executeUploadImageTask();
		}
	}

	/**
	 * 返回按钮事件
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 上传图片
	 */
	private void executeUploadImageTask() {
		String tempPath = ActivityUtil.getGPSPicturePath(Settings.uploadPictureUri);
		InputStream in=null;
		try {
			in = new FileInputStream(tempPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获得文件名
//		String fileName = new File(Settings.uploadPictureUri).getName();
//		Log.v("TAG", Settings.uploadPictureUri);

		// 获得数据流
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		mBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
		
		// 转输入流
//		InputStream in = new ByteArrayInputStream(baos.toByteArray());
		//Log.d("executeUploadImageTask","baos="+baos.size());
		// 创建任务
		uploadImageTask = new UploadImageTask("正在提交数据，请稍候...", RestaurantUploadConfirmActivity.this, uploadData, in);

		uploadImageTask.setCanCancel(false);
		// 执行任务，提交成功后，隐藏提交按钮
		uploadImageTask.execute(new Runnable() {
			@Override
			public void run() {// 提交成功

				// 隐藏提交按钮
				confirmButton.setVisibility(View.GONE);
				// 评论成功后，设置全局标志，当返回餐厅详情时要刷新评论列表
				Settings.NEED_REFRESH_REST_COMMENT = true;
				Settings.COMMENT_RES_ID = restId;
				Settings.NEED_REFRESH_FOOD_PICTURE_DETAIL=true; //需要刷新菜品图片详情页
				// -------------------------
				// 提交成功，显示确认框
				DialogUtil.showAlert(RestaurantUploadConfirmActivity.this, true, "提交成功", uploadImageTask.msg, "确定", "再拍一张",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								RestaurantUploadActivity.goingToOriginalPage = true;
//								setResult(Settings.uploadPictureOrignalActivityId);
								returnToActivity(Settings.uploadPictureOrignalActivityClazz);
								finish();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 再拍一张，如果拍照成功，到上传图片页; 如果取消拍照，直接回到最初页
								if (ActivityUtil.checkMysoftStage(RestaurantUploadConfirmActivity.this)) {

									ButtonPanelUtil pan = new ButtonPanelUtil();
									pan.showUploadPanel(contextView, RestaurantUploadConfirmActivity.this, null);
									pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener() {

										@Override
										public void onGetUri(Uri uri) {
											takePhotoUri = uri;
										}
									});
									pan.setOnCancelListener(new Runnable() {

										@Override
										public void run() {
											// 取消再拍一张时也返回最初页
											RestaurantUploadActivity.goingToOriginalPage = true;
//											setResult(Settings.uploadPictureOrignalActivityId);
											
											returnToActivity(Settings.uploadPictureOrignalActivityClazz);
											finish();
										}
									});
								}
							}
						});

			}
		}, new Runnable() {
			// 失败时可以重试，或者取消（回到最初页）
			@Override
			public void run() {
				DialogUtil.showAlert(RestaurantUploadConfirmActivity.this, true, "请选择", "提交时发生错误，您可以：", "重试", "取消",

				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						executeUploadImageTask();
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消再拍一张时也返回最初页
						RestaurantUploadActivity.goingToOriginalPage = true;
//						setResult(Settings.uploadPictureOrignalActivityId);
						
						returnToActivity(Settings.uploadPictureOrignalActivityClazz);
						finish();
					}
				});
			}
		});

	}

	// 载入图片
	private void loadPicture() {
		recycle();
		mImageUri = Settings.uploadPictureUri; // 获取上传图片路径
		// ---------------------
		InputStream picStream = null; // 图片流
		if (mImageUri == null || mImageUri.equals("")) {
			DialogUtil.showToast(this, "没有选择任何图片");
			super.finish();
			return;
		}
		try {
			picStream = new FileInputStream(mImageUri);
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
			String tempPath = ActivityUtil.getGPSPicturePath(mImageUri);
			picStream = new FileInputStream(tempPath);
			mBitmap = BitmapFactory.decodeStream(picStream, null, options);
			
			//Log.d("options"+scale,options.outHeight+","+options.outWidth);

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == Settings.CAMERAIMAGE || requestCode == Settings.LOCALIMAGE)) {
			boolean isValid = false;
			if (data != null && data.getData() != null) {
				Settings.uploadPictureUri = parseImgPath(data.getData());
				isValid = true;
			} else {
				// 判断拍照图片是否有效
				try {
					String temp = parseImgPath(takePhotoUri);
					if (!CheckUtil.isEmpty(temp)) {
						// 如果未拍照
						File f = new File(temp);
						if (f.exists()) {
							if (f.length() == 0) {
								// f.delete();
								getContentResolver().delete(takePhotoUri, null, null);
							} else {
								Settings.uploadPictureUri = temp; // 更新Setting
								isValid = true;
								takePhotoUri = null;
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 如果是有效的重拍，跳转到图片上传页面，如果是无效重拍（例如取消拍摄），直接返回最初页
			if (isValid) {
//				ActivityUtil.jump(this, RestaurantUploadActivity.class, 0, new Bundle(),true);
				returnToActivity(RestaurantUploadActivity.class);
			} else {
//				setResult(Settings.uploadPictureOrignalActivityId);
				RestaurantUploadActivity.goingToOriginalPage = true;
				
				returnToActivity(Settings.uploadPictureOrignalActivityClazz);
				finish();
			}
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);

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

	/**
	 * 回收内存
	 */
	private void recycle() {
		if (mBitmap != null) {
			// 回收内存
			mBitmap.recycle();
			System.gc();
		}
	}
}
