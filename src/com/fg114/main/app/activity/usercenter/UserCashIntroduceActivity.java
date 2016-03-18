package com.fg114.main.app.activity.usercenter;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.service.dto.UserCashCouponData;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 现金劵详情
 * 
 * @author chenguojin
 * 
 */
public class UserCashIntroduceActivity extends MainFrameActivity {

	// 传入参数获得
	private int fromPage; // 返回页面
	private UserCashCouponData mCash;
    private LinearLayout mCashIntroduceLayout;
	
	// 界面组件

	private View contextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		mCash = (UserCashCouponData) bundle.get(Settings.BUNDLE_KEY_CONTENT);

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this
				.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT,
					getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class,
					0, bund);
		}

		// 初始化界面
		initComponent();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		if(mCash.getTypeTag()==1)
		{
			this.getTvTitle().setText(getString(R.string.text_title_user_crash));
		}
		else
		{
			this.getTvTitle().setText(getString(R.string.text_title_discount_title));
		}
		
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		contextView = View.inflate(this, R.layout.user_crash_instruction, null);
		TextView text = (TextView) contextView
				.findViewById(R.id.user_crash_instruction_name);// user_crash_instuction_validity
		TextView validity = (TextView) contextView
				.findViewById(R.id.user_crash_instuction_validity);
		TextView prompot = (TextView) contextView
				.findViewById(R.id.user_crash_instuction_prompot);
		TextView useDescription = (TextView) contextView
				.findViewById(R.id.user_cash_useDescription);
		mCashIntroduceLayout=(LinearLayout) contextView.findViewById(R.id.user_cash_instuction_layout);
		validity.setText(ConvertUtil.convertLongToDateString(
				mCash.getUserBeginTime(), "yyyy年MM月dd日")
				+ "-"
				+ ConvertUtil.convertLongToDateString(mCash.getUserEndTime(),
						"yyyy年MM月dd日"));
		//优惠套餐情况
		if(mCash.getTypeTag()==2)
		{
			mCashIntroduceLayout.setVisibility(View.GONE);
		}
		else
		{
			mCashIntroduceLayout.setVisibility(View.VISIBLE);
			prompot.setText(TextUtils.isEmpty(mCash.getUseHint().trim()) ? "无"
					: mCash.getUseHint());
		}
		
		text.setText(mCash.getRestName());
		
		useDescription.setText(mCash.getUseDescription());
		
		// this.setBtnCallGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}

}
