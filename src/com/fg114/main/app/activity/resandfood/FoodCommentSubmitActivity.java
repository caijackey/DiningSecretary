package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.service.dto.ResFoodCommentData;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.DishCommentTask;
import com.fg114.main.service.task.WebboTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.DialogUtil.GeneralCallback;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboContentBuilder;
import com.fg114.main.weibo.WeiboUtilFactory;

/**
 * 填写菜品评论
 * 
 * @author xu jianjun, 2012-01-06
 * 
 */
public class FoodCommentSubmitActivity extends MainFrameActivity {

	private static final String TAG = "FoodCommentSubmitActivity";
	private static final int VERYGOOD_SCORE = DishCommentTask.VERYGOOD;
	private static final int GOOD_SCORE = DishCommentTask.GOOD;
	private static final int BAD_SCORE = DishCommentTask.BAD;

	// 传入参数
	// private String mRestaurantId; //餐厅信息
	// private String mOrderId; //订单信息
	private int mFromPage;
	private String mDishId; // 菜品ID
	private String mDishName;// 菜品名字
	private int mInitScore;// 初始化评分
	private int mVeryGoodNum = 0;
	private int mGoodNum = 0;
	private int mBadNum = 0;

	// 画面变量
	// int mOverallNum = 0;
	int tasteNum = 0;
	int envNum = 0;
	int serviceNum = 0;
	String mContent;
	// String restaurantName;
	double longitude;
	double latitude;
	String dishUrl = "";
	private WebboTask mWebboTask;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private EditText etComment;
	private CheckBox chbShareToSina;
	private Button mVeryGoodImage;
	private Button mGoodImage;
	private Button mBadImage;
	private Button btSubmit;
	private Button commentVoiceInputButton;
	// 任务
	private DishCommentTask mDishCommentTask;
	private TextView mUserName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		String[] idArray = bundle.getStringArray(Settings.BUNDLE_KEY_ID);
		int[] initScore = bundle.getIntArray(Settings.BUNDLE_DISH_TAG);

		/**
		 * test code -----------------------
		 */
		// idArray = new String[] { "12", "鲍鱼" };
//		initScore = new int[] { 1, 123, 333, 22 };
		// ----------------------------------------

		if (initScore != null && initScore.length == 4) {
			mInitScore = initScore[0];
			mVeryGoodNum = initScore[1];
			mGoodNum = initScore[2];
			mBadNum = initScore[3];
		}
		mDishId = idArray[0];
		mDishName = idArray[1];
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
	protected void onResume() {
		super.onResume();
		
		if (SessionManager.getInstance().isUserLogin(this)) {
			// 设置用户名
			UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(this);
			if (!CheckUtil.isEmpty(userInfo.getNickName())) {
				mUserName.setText(userInfo.getNickName());
			}
			getBtnOption().setVisibility(View.INVISIBLE);
		}
		
		setShareSinaState(chbShareToSina, null);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void finish() {
		mContent = etComment.getText().toString().trim();
		if (!CheckUtil.isEmpty(mContent)) {
			DialogUtil.showAlert(FoodCommentSubmitActivity.this, true, getString(R.string.text_dialog_comment_finish), new DialogInterface.OnClickListener() {

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

		// if (mIsFinish) {
		// super.finish();
		// }

	}

	private void finishThis() {
		super.finish();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(mDishName);
		this.getBtnGoBack().setText(R.string.text_button_back);
		// this.getBtnOption().setText(R.string.text_button_dish_comment_publish);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.food_comment_submit, null);
		etComment = (EditText) contextView.findViewById(R.id.food_comment_submit_etComment);
		chbShareToSina = (CheckBox) contextView.findViewById(R.id.food_comment_submit_chkShareSina);
		mVeryGoodImage = (Button) contextView.findViewById(R.id.food_comment_submit_verygood);
		mGoodImage = (Button) contextView.findViewById(R.id.food_comment_submit_good);
		mBadImage = (Button) contextView.findViewById(R.id.food_comment_submit_bad);
		mUserName = (TextView) contextView.findViewById(R.id.food_comment_submit_username);
		btSubmit = (Button) contextView.findViewById(R.id.food_comment_submit_btnUpLoad);
		
		contextView.findViewById(R.id.food_comment_submit_likeTag).setVisibility(View.GONE);
		
		mVeryGoodImage.setText(mVeryGoodNum + "");
		mGoodImage.setText(mGoodNum + "");
		mBadImage.setText(mBadNum + "");

		if (!SessionManager.getInstance().isUserLogin(this)) {
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText(R.string.text_button_login);
			this.getBtnOption().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					DialogUtil.showUserLoginDialog(FoodCommentSubmitActivity.this, new Runnable() {
						@Override
						public void run() {
							SystemClock.sleep(1000);
						}
					}, 0);
				}
			});
		}

		// 设置点击事件
		mVeryGoodImage.setOnClickListener(mClick);
		mGoodImage.setOnClickListener(mClick);
		mBadImage.setOnClickListener(mClick);

		// 先隐藏分享微博
		// chkShare.setVisibility(View.GONE);

		// // 如果有初始评分，则默认为初始评分
		if (mInitScore > 0 && mInitScore < 4)
			showScore();

		// 语音录入 ------------------------added by xu jianjun, 2011-12-15

		commentVoiceInputButton = (Button) contextView.findViewById(R.id.food_comment_voice_button);
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {
			eng.bindButtonAndEditText(commentVoiceInputButton, etComment);
		}
		// 获取总分列表
		ArrayList<String> starList = new ArrayList<String>();
		String[] starArray = getResources().getStringArray(R.array.commont_list);
		// 获取XML中定义的评分数组
		for (int i = 0; i < starArray.length; i++) {
			starList.add(starArray[i]);
		}

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
				DialogUtil.showFoodScoreDialog(FoodCommentSubmitActivity.this, new GeneralCallback() {

					@Override
					public void run(Bundle data) {
						executePostCommentTask(data.getInt("score"));
					}
					
				});
				
			}
		});

		chbShareToSina.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).requestWeiboShare(null);
				}
			}
		});

		this.setFunctionLayoutGone();
		//this.getDishBaseLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 定义选择分数的按钮事件
	 */

	private OnClickListener mClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.food_comment_submit_verygood:
				mInitScore = VERYGOOD_SCORE;
				break;
			case R.id.food_comment_submit_good:
				mInitScore = GOOD_SCORE;
				break;
			case R.id.food_comment_submit_bad:
				mInitScore = BAD_SCORE;
				break;

			}
			showScore();
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//this.showBottom(SessionManager.getInstance().getDishOrder(this));
	}

	/**
	 * 显示选择的评分
	 */
	private void showScore() {
		if (mInitScore == VERYGOOD_SCORE) {
			mVeryGoodImage.setSelected(true);
			mGoodImage.setSelected(false);
			mBadImage.setSelected(false);
		} else if (mInitScore == GOOD_SCORE) {
			mVeryGoodImage.setSelected(false);
			mGoodImage.setSelected(true);
			mBadImage.setSelected(false);
		} else if (mInitScore == BAD_SCORE) {
			mVeryGoodImage.setSelected(false);
			mGoodImage.setSelected(false);
			mBadImage.setSelected(true);
		}
	}

	/**
	 * 获得餐厅详细
	 * @param score 
	 */
	private void executePostCommentTask(int score) {

		if (!checkInput()) {
			return;
		}

		String token = "";
		//--构造分享平台字符串
		String shareTo="";
		if(chbShareToSina.isChecked()){
			shareTo="sina:1";
		}else{
			shareTo="sina:0";
		}
		//--
		if (SessionManager.getInstance().isUserLogin(this))
			token = SessionManager.getInstance().getUserInfo(FoodCommentSubmitActivity.this).getToken();
		
		mDishCommentTask = new DishCommentTask(getString(R.string.text_info_uploading), this, mDishId, token, score, mContent,shareTo);

		// 执行任务
		mDishCommentTask.execute(new Runnable() {

			@Override
			public void run() {
				//评论成功后，需要将数据带到菜品列表页去更新显示，这里把数据放在全局
				ResFoodCommentData recentCommentData=new ResFoodCommentData();
//				recentCommentData.totalCommentNum=-999; //表示是发表了一条新评论，菜列表页里总数应该加1
//				recentCommentData.setCreateTime(System.currentTimeMillis());
//				recentCommentData.setDetail(mContent); 
//				recentCommentData.setLikeTypeTag(mInitScore);
//				recentCommentData.setUserName(SessionManager.getInstance().getUserInfo(FoodCommentSubmitActivity.this).getNickName());
//				recentCommentData.setUuid(java.util.UUID.randomUUID().toString());
//				recentCommentData.foodId=mDishId;
				RestaurantFoodListActivity.recentCommentData=recentCommentData;
				//---------------------------------------------------------------------------------
//				if (chkShare.isChecked()) {
//					// 需要分享到微博的场合
//
//					String resId = SessionManager.getInstance().getDishOrder(FoodCommentSubmitActivity.this).getRestId();
//					if (!CheckUtil.isEmpty(resId)) {
//						ResInfo2Data restaurantInfo = SessionManager.getInstance().getRestaurantInfo(FoodCommentSubmitActivity.this, resId);
//						if (restaurantInfo != null && !CheckUtil.isEmpty(restaurantInfo.getId())) {
//							dishUrl = restaurantInfo.getResLink() + "/dish/" + mDishId + "#menu";
//						}
//					}
//					// ---
//					WeiboContentBuilder wb = new WeiboContentBuilder();
//					wb.appendText("我对\"" + mDishName + "\"评论说：" + mContent);
//					wb.appendUrl(dishUrl);
//					wb.appendImportantText("【来自小秘书客户端】");
//					wb.appendUrl("http://www.xiaomishu.com/o/app");
//
//					String lon = "";
//					String lat = "";
//					ShareToSinaWeibo sinaWeibo = ShareToSinaWeibo.getInstance(FoodCommentSubmitActivity.this, mUserInfo.getSinaToken(),
//							mUserInfo.getSinaSecret(), null);
//					sinaWeibo.shareToSina(wb.toWeiboString(), "", lon, lat);
//				}
				
				mDishCommentTask.closeProgressDialog();
				DialogUtil.showToast(FoodCommentSubmitActivity.this, mDishCommentTask.msg);

//				Intent intent = FoodCommentSubmitActivity.this.getIntent();
//				DishCommentData comment = new DishCommentData();
//				comment.setDetail(mContent);
//				comment.setLikeTypeTag(mInitScore);
//				if (SessionManager.getInstance().isUserLogin(FoodCommentSubmitActivity.this)) {
//					comment.setUserName(mUserInfo.getName());
//				} else {
//					comment.setUserName("游客");
//				}
//				comment.setCreateTime(System.currentTimeMillis());
//				intent.putExtra(Settings.BUNDLE_DISH_COMMENT, comment);
//				setResult(Settings.DISH_LIST_ACTIVITY, intent);
				finishThis();
			}
		});
	}

	/**
	 * check
	 */
	private boolean checkInput() {
		// if (mInitScore == 0) {
		// DialogUtil.showToast(this, "请评分");
		// return false;
		// }

		mContent = etComment.getText().toString().trim();
		if (CheckUtil.isEmpty(mContent)) {
			ViewUtils.setError(etComment, "请输入评价内容");
			etComment.requestFocus();
			return false;
		}
		mContent = etComment.getText().toString();
		// if (mContent.length() < 10) {
		// etComment.requestFocus();
		// return false;
		// }
		if (mContent.length() > 1000) {
			ViewUtils.setError(etComment, "您点评内容过长，点评不能超过1000字");
			etComment.requestFocus();
			return false;
		}

		return true;
	}
}
