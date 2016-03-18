package com.fg114.main.app.activity.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.IvParameterSpec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import android.test.suitebuilder.TestSuiteBuilder.FailedToCreateTests;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;

import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SendSMSActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.view.MyScrollView;
import com.fg114.main.app.view.NumButton;

import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.InviteSmsInfoDTO;
import com.fg114.main.service.dto.InviteSmsTempletData;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;

import com.fg114.main.service.task.CommonTask;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

import com.fg114.main.util.UnitUtil;
import com.fg114.main.wxapi.WeixinUtils;

public class SelectSMSActivity extends MainFrameActivity {

	private static final int SLEEP = 1000 * 7;

	private static final int INPROGRESS = 1;
	private static final int SUCCESS = 2;
	private static final int CANCEL = 3;
	private AtomicInteger flag = new AtomicInteger(0);
	private int fromPage;

	// 多点查询内容
	private static final String NAME = "地铁站";

	// 界面组件
	private LinearLayout invite_by_sms;
	private LinearLayout invite_by_weixin;
	private TextView  mAddresstv;
	private EditText mResinfoET;
	private RadioGroup mCategoryGroup;
	private ProgressDialog progressDialog;
	private MyScrollView svMain;


	// 界面数据
	private HashMap<String, InviteSmsTempletData> mMaps;
	private List<InviteSmsTempletData> mTypeNames = new ArrayList<InviteSmsTempletData>();// 类别名称列表
	private String mCurrentTypename;
	private int vPadding = 3;
	private int hPadding = 8;
	private String mToken;
	private String resId = "1";
	private String orderId;
	private ResInfo2Data restaurantInfo;
	private InviteSmsInfoDTO dto;
	private String Uuid; // 模板ID

	private static String INVITER = "{Inviter}";
	private static String RESADRESS = "{ResAdress}";
	private static String RESNAME = "{ResName}";
	private static String DATE = "{Date}";
	private static String[] EXCLUDE = new String[] { "()", "[]", "{}" };

	private Map<String, String> FrameWorkMapContent = new HashMap<String, String>(); // 四个模板内容

	@Override
	protected void onCreate(Bundle arg0) {
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("选择短信模板", "");
		// ----------------------------
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		getMainLayout().addView(View.inflate(this, R.layout.select_sms_res,null));

		// 如果使用地图SDK，请初始化地图Activity
		Bundle bundle = this.getIntent().getExtras();
		resId = bundle.getString(Settings.BUNDLE_REST_ID);
		if (CheckUtil.isEmpty(resId)) {
			DialogUtil.showToast(this, "请选择目标餐厅!");
			finish();
		}
		orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		mToken = SessionManager.getInstance().getUserInfo(this).getToken();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		} else {
			initComponent();
			getInviteSmsInfoTask();
		}

	}
	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("选择短信模板", "");
		// ----------------------------
	}


	// 返回操作
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == -1) {
			InviteSmsTempletData inviteSms = mMaps.get(mCurrentTypename);
			String SMSinfo = replaceKey(inviteSms.detail, INVITER, dto.inviterName);
			SMSinfo = replaceKey(SMSinfo, RESADRESS, dto.restAddress);
			SMSinfo = replaceKey(SMSinfo, RESNAME, dto.restName);
			SMSinfo = replaceKey(SMSinfo, DATE, dto.dinnerDate);
			SMSinfo = replaceKey(SMSinfo, DATE, dto.dinnerDate);
			SMSinfo = excludeKey(SMSinfo);

			mResinfoET.setText(SMSinfo);
			FrameWorkMapContent.clear();

		} else if (requestCode != resultCode) {
			this.setResult(resultCode, data);
			this.finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 初始化界面信息
	private void initComponent() {
		getBtnGoBack().setText("返回");
		getBtnOption().setVisibility(View.INVISIBLE);
		invite_by_sms = (LinearLayout) this.findViewById(R.id.invite_by_sms);
		invite_by_weixin = (LinearLayout) this.findViewById(R.id.invite_by_weixin);
		mAddresstv = (TextView) this.findViewById(R.id.select_sms_res_adTV);
		mResinfoET = (EditText) this.findViewById(R.id.select_sms_resinfoET);
		mCategoryGroup = (RadioGroup) this.findViewById(R.id.select_sms_image_list_group);
		svMain = (MyScrollView) findViewById(R.id.select_sms_svMain);
		getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				finish();
			}
		});


		invite_by_sms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("通过短信邀请");
				// -----
				if (SessionManager.getInstance().isAllowUploadContact()) {
					gotoSendSms();
				} else {
					if (SessionManager.getInstance().isRequestUploadContactOverMaxTime()) {
						gotoSendSms();
					} else {
						String msg = "您愿意导入通讯录快捷使用免费请柬短信吗？";
						DialogUtil.showComfire(SelectSMSActivity.this, null, msg, 
								new String[] {"试试看", "以后再说"}, 
									new Runnable() {
									
									@Override
									public void run() {
										SessionManager.getInstance().setAllowUploadContact(true);
										gotoSendSms();
									}
								}, new Runnable() {
									
									@Override
									public void run() {
										SessionManager.getInstance().setAllowUploadContact(false);
										gotoSendSms();
									}
								});
						SessionManager.getInstance().addRequestUploadContactTimes();
					}
				}
			}
		});
		invite_by_weixin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("通过微信邀请");
				// -----
				if(WeixinUtils.isWeixinAvailable()){
					WeixinUtils.sendText(mResinfoET.getText().toString());
				}else{
					DialogUtil.showAlert(SelectSMSActivity.this, "提示", "你的手机没有安装微信!");
				}
			}
		});

		mCategoryGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				try {

					if (checkedId == -1) {
						return;
					}
					// -----
					OpenPageDataTracer.getInstance().addEvent("选择模版");
					// -----
					FrameWorkMapContent.put(mCurrentTypename, mResinfoET.getText().toString());
					RadioButton btnType = (RadioButton) mCategoryGroup.findViewById(checkedId);
					mCurrentTypename = (String) btnType.getText();

					InviteSmsTempletData inviteSms = mMaps.get(mCurrentTypename);
					Uuid = inviteSms.uuid;
					if (FrameWorkMapContent.containsKey(mCurrentTypename)) {
						mResinfoET.setText(FrameWorkMapContent.get(mCurrentTypename));
					} else {
						String SMSinfo = replaceKey(inviteSms.detail, INVITER, dto.inviterName);
						SMSinfo = replaceKey(SMSinfo, RESADRESS, dto.restAddress);
						SMSinfo = replaceKey(SMSinfo, RESNAME, dto.restName);
						SMSinfo = replaceKey(SMSinfo, DATE, dto.dinnerDate);
						SMSinfo = excludeKey(SMSinfo);

						mResinfoET.setText(SMSinfo);
						FrameWorkMapContent.put(mCurrentTypename, SMSinfo);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		mResinfoET.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disables LockableScrollView when the EditText is touched
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					svMain.setScrollingEnabled(false);
				}

				// Enables LockableScrollView when the EditText is touched
				if (event.getAction() == MotionEvent.ACTION_UP) {
					svMain.setScrollingEnabled(true);
				}
				return false;
			}
		});
	}

	public void getInviteSmsInfoTask() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		ServiceRequest request=new ServiceRequest(API.getInviteSmsInfo);
		request.addData("orderId",orderId);
		request.addData("restId",resId);
		
		CommonTask.request(request,new CommonTask.TaskListener<InviteSmsInfoDTO>(){
			
			@Override
			protected void onSuccess(InviteSmsInfoDTO inviteDto){
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				dto = inviteDto;
				if (dto != null) {
					getTvTitle().setText(dto.restName);
					mAddresstv.setText(dto.restAddress);
					if (mTypeNames.size() == 0) {
						mTypeNames.addAll(dto.templetList);
					}
				}
				setFormworkView();
			}
			@Override
			protected void onError(int code, String message){
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				finish();
			}
		});
	}

	// 短信模板创建
	private void setFormworkView() {

		mCurrentTypename = "";
		FrameWorkMapContent.clear();
		mMaps = new HashMap<String, InviteSmsTempletData>();

		mCategoryGroup.removeAllViews();
		for (int i = 0; i <= mTypeNames.size() - 1; i++) {

			RadioButton btnType = createButton(mTypeNames.get(i).name, i);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(UnitUtil.dip2px(2), UnitUtil.dip2px(0), UnitUtil.dip2px(2), UnitUtil.dip2px(0));
			lp.weight = 1;
			mCategoryGroup.addView(btnType, lp);
			mMaps.put(mTypeNames.get(i).name, mTypeNames.get(i));

		}

		((RadioButton) mCategoryGroup.getChildAt(0)).setChecked(true);
	}

	// 创建模板按钮
	private RadioButton createButton(String text, int i) {
		RadioButton rbType = (RadioButton) View.inflate(this, R.layout.radio_button, null);
		rbType.setText(text);
		rbType.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		rbType.setTextColor(getResources().getColorStateList(R.drawable.take_away_menu_list_type_button_text_color));
		rbType.setPadding(UnitUtil.dip2px(hPadding), UnitUtil.dip2px(vPadding), UnitUtil.dip2px(hPadding), UnitUtil.dip2px(vPadding));
		return rbType;
	}


	public String replaceKey(String input, String key, String value) {
		try {
			if (input.indexOf(key) < 0) {
				return input;
			}
			if (CheckUtil.isEmpty(value)) {
				return input.replace(key, "");
			}
			return input.replace(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return input;
		}
	}

	public String excludeKey(String input) {
		try {
			for (String exclude : EXCLUDE) {
				input = replaceKey(input, exclude, "");
			}
			return input;
		} catch (Exception e) {
			e.printStackTrace();
			return input;
		}
	}

	private void gotoSendSms() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(Settings.BUNDLE_SMS_HavePlaceGpsTag, false);
		bundle.putString(Settings.BUNDLE_SMS_PlaceLon, "");
		bundle.putString(Settings.BUNDLE_SMS_PlaceLat, "");
		bundle.putString(Settings.BUNDLE_REST_ID, resId);
		if (orderId != null) {
			bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
		} else {
			bundle.putString(Settings.BUNDLE_ORDER_ID, "");
		}
		bundle.putString(Settings.BUNDLE_SMS_DETAIL, mResinfoET.getText().toString());
		bundle.putString(Settings.BUNDLE_SMS_PlaceName, "");
		bundle.putString(Settings.BUNDLE_SMS_TempletId, Uuid);

		ActivityUtil.jump(SelectSMSActivity.this, SendSMSActivity.class, 0, bundle);
	}
}
