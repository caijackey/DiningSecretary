package com.fg114.main.app.activity.usercenter;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.service.dto.UserCashCouponData;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 现金劵,套餐详情
 * 
 * @author chenguojin
 * 
 */
public class UserCashDetailActivity extends MainFrameActivity {

	// 传入参数获得
	private int fromPage; // 返回页面
	private UserCashCouponData mCash;

	// 界面组件

	private View contextView;
	private TextView mResName;
	private TextView mCrashTicket;
	private TextView mSerailNum;
	private TextView mTicketPwd;
	private LinearLayout mLayout;
    private TextView mtypeName;
    private TextView mtypePwd;    
    private TextView mtypeUseInfo;//使用信息
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
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setText(getString(R.string.text_layout_res_info));
		this.getBtnOption().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
        		bundle.putString(Settings.BUNDLE_REST_ID, mCash.getRestId());
        		String[] nameAndLogoUrl = {mCash.getRestName(), ""};
        		bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
        		bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
        		ActivityUtil.jump(UserCashDetailActivity.this, 
        				RestaurantDetailMainActivity.class, 
        				0, bundle);				
			}
		});
		
		// 内容部分
		contextView = View.inflate(this,R.layout.user_crash_check, null);
		mLayout = (LinearLayout) contextView.findViewById(R.id.user_crash_check_layout);
		mResName = (TextView) contextView.findViewById(R.id.user_crash_check_resname);	
		mCrashTicket = (TextView) contextView.findViewById(R.id.user_crash_check_crashticket);	
		mSerailNum = (TextView) contextView.findViewById(R.id.user_crash_check_serialnum);	
		mTicketPwd = (TextView) contextView.findViewById(R.id.user_crash_check_ticketpwd);	
		mtypeName=(TextView) contextView.findViewById(R.id.user_center_accountManagement_text);
		mtypePwd=(TextView) contextView.findViewById(R.id.user_cash_password_text);
		mtypeUseInfo=(TextView) contextView.findViewById(R.id.user_cash_use_info);
		
		//根据优惠套餐和现金卷不同分别设置不同内容
		setContentByType(mCash.getTypeTag());
		
		
		
		mLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
        		bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, mCash);
        		
        		ActivityUtil.jump(UserCashDetailActivity.this, 
        				UserCashIntroduceActivity.class, 
        				0, bundle);	
			}
		});
		
//		this.setBtnCallGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}
	//根基现金卷和优惠套餐分别设置不同的参数
	private void setContentByType(int typeTag)
	{
		//现金卷
		if(typeTag==1)
		{
			this.getTvTitle().setText(getString(R.string.text_title_user_crash));
			mResName.setText(mCash.getRestName());
			mtypeName.setText(R.string.text_title_user_crash);
			mtypePwd.setText(R.string.text_title_cash_pwd);
			mtypeUseInfo.setText(R.string.text_title_cash_use_info);
			
		}
		else
		{
			this.getTvTitle().setText(getString(R.string.text_title_discount_title));
			mResName.setText(mCash.getRestName()+"-"+mCash.getName());
			mtypeName.setText(R.string.text_title_discount_package);
			mtypePwd.setText(R.string.text_title_discount_pwd);
			mtypeUseInfo.setText(R.string.text_title_discount_use_info);
		}
		
		mCrashTicket.setText(mCash.getCouponValue()+"元");
		mSerailNum.setText(mCash.getSerialNum());
		mTicketPwd.setText(mCash.getCouponPwd());
	}

}
