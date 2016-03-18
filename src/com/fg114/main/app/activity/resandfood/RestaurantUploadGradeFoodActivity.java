package com.fg114.main.app.activity.resandfood;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.fg114.main.weibo.activity.FriendSelectionActivity;
import com.fg114.main.weibo.dto.User;

/**
 * 餐厅菜品图片上传打分界面
 * 
 * @author xujianjun, 2012-09-28
 * 
 */
public class RestaurantUploadGradeFoodActivity extends MainFrameActivity {

	private static final String TAG = RestaurantUploadGradeFoodActivity.class.getSimpleName();

	// 传入参数
	private String restId; // 餐厅ID
	private String restName;
	private String foodId;
	private String foodName;
	private int fromPage;

	// 界面组件
	private LayoutInflater inflater;
	private View contextView;
	private TextView bonus;
	private Button goodButton;
	private Button generalButton;
	private Button badButton;
	private LineView line; //横线
	private Button skipButton;

	private UploadDataPack uploadData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		
		uploadData=SessionManager.uploadData;

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
		getBtnGoBack().setVisibility(View.VISIBLE);
		getBtnOption().setVisibility(View.INVISIBLE);
		

		// 内容部分
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = inflater.inflate(R.layout.restaurant_upload_grade_food, null);
		
		// ---
		bonus = (TextView) contextView.findViewById(R.id.upload_to_food_bonus);
		goodButton = (Button) contextView.findViewById(R.id.upload_to_food_grade_good);
		generalButton=(Button) contextView.findViewById(R.id.upload_to_food_grade_general);
		badButton=(Button) contextView.findViewById(R.id.upload_to_food_grade_bad);
		line = (LineView) contextView.findViewById(R.id.horizontal_line);		
		skipButton = (Button) contextView.findViewById(R.id.upload_skip);		

		//菜评分，不允许跳过
		line.setVisibility(View.GONE); 
		skipButton.setVisibility(View.GONE);
		//----
		getTvTitle().setText("菜品评分");
//		getTvTitle().setText(uploadData.restName);
		
		bonus.setText(Html.fromHtml("打分可获<font color=\"#FF0000\">"+uploadData.uploadData.getPointNumForGradeFood()+"</font>秘币"));

		//---
		goodButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//打包数据到下一页 	
				nextStep(1);
			}			
		});
		generalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//打包数据到下一页 				
				nextStep(2);
			}			
		});
		badButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//打包数据到下一页 				
				nextStep(3);
			}			
		});
		
		//跳过，未评分
		skipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//打包数据到下一页 				
				nextStep(0);
			}
		});
		//---
		setFunctionLayoutGone();
		getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 下一步
	 * score: 0,1,2,3：未评分，喜欢，一般，不喜欢
	 */
	private void nextStep(int score) {
		Bundle bundle = new Bundle();
		SessionManager.uploadData.foodScore=score;
		ActivityUtil.jump(RestaurantUploadGradeFoodActivity.this, RestaurantUploadConfirmActivity.class,0, bundle);
	}


	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);		
	}




}
