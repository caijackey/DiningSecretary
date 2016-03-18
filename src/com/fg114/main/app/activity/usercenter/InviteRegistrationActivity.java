package com.fg114.main.app.activity.usercenter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.UserInfo;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;

public class InviteRegistrationActivity extends MainFrameActivity {

	private static final boolean debug = false;
	private LayoutInflater mInflater;
	private View contextView;
	private EditText userPhoneNumber;
	private RelativeLayout getValidateNumber;
	private EditText inputValidateNumber;
	private EditText invitation_code;
	private Button submitBnt;
	private String phoneNumber;// 手机号码
	private String inviteCode;// 邀请码
	private String code;// 验证码
	private TextView countDowmNumber;
	// private long typeTag;// 1:登录 2：修改手机号
	private String tel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("邀请注册", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// if (bundle == null || (bundle.getLong(Settings.FROM_TAG) != 1 &&
		// bundle.getLong(Settings.FROM_TAG) != 2)) {
		// DialogUtil.showToast(getApplicationContext(), "参数不合法！");
		// finish();
		//
		// } else {
		// typeTag = bundle.getLong(Settings.FROM_TAG);
		// }
		// if (bundle.containsKey(Settings.BUNDLE_KEY_TEL)) {
		// tel = bundle.getString(Settings.BUNDLE_KEY_TEL);
		// }

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
		OpenPageDataTracer.getInstance().enterPage("邀请注册", "");
		// ----------------------------
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("邀请注册");

		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		// this.getBtnGoBack().setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // -----
		// OpenPageDataTracer.getInstance().addEvent("返回按钮");
		// // -----
		//
		// goBack();
		// }
		// });
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.invite_registration_act, null);
		userPhoneNumber = (EditText) contextView.findViewById(R.id.invite_registration_phone);
		invitation_code = (EditText) contextView.findViewById(R.id.invitation_code);
		getValidateNumber = (RelativeLayout) contextView.findViewById(R.id.get_registration_code);
		inputValidateNumber = (EditText) contextView.findViewById(R.id.registration_code);
		submitBnt = (Button) contextView.findViewById(R.id.submit_bnt);
		countDowmNumber = (TextView) contextView.findViewById(R.id.count_dowm_number);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// userPhoneNumber.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// }
		// });
		getValidateNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				phoneNumber = userPhoneNumber.getText().toString();
				inviteCode = invitation_code.getText().toString();
				if (CheckUtil.isEmpty(inviteCode)) {
					DialogUtil.showToast(InviteRegistrationActivity.this, "邀请码不能为空");
					return;
				}
				if (!CheckUtil.isCellPhone(phoneNumber)) {
					DialogUtil.showToast(InviteRegistrationActivity.this, "手机号输入错误");
					return;
				}
				// 发送验证短信 Task
				executeSendMsm();

			}
		});

		submitBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				phoneNumber = userPhoneNumber.getText().toString();
				inviteCode = invitation_code.getText().toString();
				code = inputValidateNumber.getText().toString().trim();

				if (CheckUtil.isEmpty(code)) {
					DialogUtil.showToast(InviteRegistrationActivity.this, "验证码不能为空");
					return;
				}
				if (CheckUtil.isEmpty(inviteCode)) {
					DialogUtil.showToast(InviteRegistrationActivity.this, "邀请码不能为空");
					return;
				}
				if (!CheckUtil.isCellPhone(phoneNumber)) {
					DialogUtil.showToast(InviteRegistrationActivity.this, "手机号输入错误");
					return;
				}

				executeSubmit();

			}
		});

	}


	boolean isSuccess = false;

	// 开始执行发送短信的task
	private boolean executeSendMsm() {

		// -----
		OpenPageDataTracer.getInstance().addEvent("获取验证码按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.sendVerifyCodeToTelForInvite);
		request.addData("tel", phoneNumber);
		request.addData("inviteCode", inviteCode);
		CommonTask.request(request, "正在获取验证码", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {

				// -----
				OpenPageDataTracer.getInstance().endEvent("获取验证码按钮");
				// -----

				// if (typeTag == 2 && dto.isSuccTag() == false) {
				// DialogUtil.showComfire(InviteRegistrationActivity.this, "提示",
				// dto.getMsg(), new String[] { "确定", "取消" }, new Runnable() {
				// @Override
				// public void run() {
				// typeTag = 3;
				// executeSendMsm();
				// }
				// }, new Runnable() {
				// @Override
				// public void run() {
				//
				// }
				// });
				// } else {
				isSuccess = true;
				// DialogUtil.showToast(InviteRegistrationActivity.this,
				// dto.getMsg());
				new CountDownTimer(60000, 1000) {

					public void onTick(long millisUntilFinished) {
						userPhoneNumber.setEnabled(false);
						getValidateNumber.setEnabled(false);
						getValidateNumber.setBackgroundResource(R.drawable.bg_new_order);
						countDowmNumber.setText("获取验证码" + " " + millisUntilFinished / 1000);
					}

					public void onFinish() {
						userPhoneNumber.setEnabled(true);
						getValidateNumber.setBackgroundResource(R.drawable.new_green_bg);
						getValidateNumber.setEnabled(true);
						countDowmNumber.setText("获取验证码");
					}
				}.start();
				// }
			}

			@Override
			protected void onError(int code, String message) {

				// -----
				OpenPageDataTracer.getInstance().endEvent("获取验证码按钮");
				// -----

				DialogUtil.showToast(InviteRegistrationActivity.this, message);
				userPhoneNumber.setEnabled(true);
				getValidateNumber.setBackgroundResource(R.drawable.new_green_bg);
				getValidateNumber.setEnabled(true);
				countDowmNumber.setTextColor(getResources().getColor(R.color.text_color_white));
				countDowmNumber.setText("获取验证码失败");
				isSuccess = false;
				// TODO
				if (debug) {
					doTest(tel);
				}
			}
		});

		return isSuccess;
	}

	// 提交数据task
	private void executeSubmit() {

		// -----
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.verifyTelCodeForInvite);
		request.addData("tel", phoneNumber);// 手机号
		request.addData("inviteCode", inviteCode);// 邀请码
		request.addData("verifyCode", code);// 验证码
		CommonTask.request(request, "正在提交", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----

				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			@Override
			protected void onError(int code, String message) {

				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----

				DialogUtil.showToast(InviteRegistrationActivity.this, message);
				if (debug) {
					doTest2();
				}
			}
		});

	}

	// ------------------模拟数据开始-------------------------

	private void doTest(String tel) {
		if (("13000000000").equals(tel)) {
			UserInfoDTO infoDTO = SessionManager.getInstance().getUserInfo(InviteRegistrationActivity.this);
			String json = "{\"uuid\":\"11111\",\"nickName\":\"测试用户_phone\",\"tel\":\"13000000000\",\"token\":\"1111\",\"picUrl\":\"http://upload1.95171.cn/img/zpcy/160_120/1b7361fb-8110-483c-936f-b71d794aa8d9.jpg\",\"sexTag\":\"1\",\"pointNum\":\"100\",\"level\":\"5\",\"sinaBindTag\":\"false\",\"sinaAccount\":\"384850682@qq.com\",\"sinaBindRemainSecs\":\"0\",\"sinaBindRemainSecsTimestamp\":\"0\",\"qqBindTag\":\"false\",\"qqAccount\":\"384850682\",\"qqBindRemainSecs\":\"0\",\"qqBindRemainSecsTimestamp\":\"0\"}";
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
			infoDTO.setSinaBindRemainSecs(4546466);
			infoDTO.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
			infoDTO.setQqBindTag(dto.isQqBindTag());
			infoDTO.setQqAccount(dto.getQqAccount());
			infoDTO.setQqBindRemainSecs(4546466);
			infoDTO.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
			SessionManager.getInstance().setUserInfo(InviteRegistrationActivity.this, infoDTO);
			SessionManager.getInstance().setIsUserLogin(InviteRegistrationActivity.this, true);
			isSuccess = true;
			DialogUtil.showToast(InviteRegistrationActivity.this, "短信：120");
			userPhoneNumber.setEnabled(true);
			getValidateNumber.setBackgroundResource(R.drawable.bg_green);
			getValidateNumber.setEnabled(true);
			countDowmNumber.setTextColor(getResources().getColor(R.color.background_color_gray));
			countDowmNumber.setText("获取验证码");
		}
	}

	private void doTest2() {
		String input_num = inputValidateNumber.getText().toString();
		if (("120").equals(input_num)) {
			SessionManager.getInstance().setIsUserLogin(InviteRegistrationActivity.this, true);
			Bundle bundle = new Bundle();
			ActivityUtil.jump(InviteRegistrationActivity.this, UserCenterActivity.class, 0, bundle);
			finish();
		}
	}

	// ------------------模拟数据结束-------------------------

	// void goBack(){
	// if(typeTag==1){
	// Bundle bundle = new Bundle();
	// ActivityUtil.jump(this, UserLoginActivity.class, 0, bundle);
	// finish();
	// }else{
	// finish();
	// }
	// }

	// @Override
	// public void onBackPressed() {
	// goBack();
	// }
}
