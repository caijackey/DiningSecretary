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


public class ValidatePhoneNOActivity extends MainFrameActivity {

	private static final boolean debug = false;
	private LayoutInflater mInflater;
	private View contextView;
	private EditText userPhoneNumber;
	private RelativeLayout getValidateNumber;
	private EditText inputValidateNumber;
	private Button submitBnt;
	private String phoneNumber;// 手机号码
	private LinearLayout validateLayout;
	private TextView countDowmNumber;
	private long typeTag;// 1:登录 2：修改手机号
	private String tel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("验证手机号", "");
		// ----------------------------
		
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras(); 
		if (bundle== null || (bundle.getLong(Settings.FROM_TAG) !=1 && bundle.getLong(Settings.FROM_TAG) !=2)) {
			DialogUtil.showToast(getApplicationContext(), "参数不合法！");
			finish();
			
		}else{
			typeTag = bundle.getLong(Settings.FROM_TAG);
		}
		if (bundle.containsKey(Settings.BUNDLE_KEY_TEL)) {
			tel = bundle.getString(Settings.BUNDLE_KEY_TEL);
		}

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
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("验证手机号", "");
		//----------------------------
	}
	
	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		if (typeTag == 1) {
			this.getTvTitle().setText("验证手机号");
		} else if (typeTag == 2) {
			this.getTvTitle().setText("修改手机号");
		}
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getBtnGoBack().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				
				goBack();
			}
		});
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.validate_phone_number, null);
		userPhoneNumber = (EditText) contextView.findViewById(R.id.user_phone_number);
		getValidateNumber = (RelativeLayout) contextView.findViewById(R.id.get_validate_number);
		inputValidateNumber = (EditText) contextView.findViewById(R.id.input_validate_number);
		submitBnt = (Button) contextView.findViewById(R.id.submit_bnt);
		countDowmNumber = (TextView) contextView.findViewById(R.id.count_dowm_number);
		validateLayout = (LinearLayout) contextView.findViewById(R.id.validate_layout);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		validateLayout.setVisibility(View.GONE);
		userPhoneNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		getValidateNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				phoneNumber = userPhoneNumber.getText().toString();
				if (CheckUtil.isPhone(phoneNumber)) {
					// 开发发送验证短信 Task
					executeSendMsm();
				} else {
					DialogUtil.showToast(ValidatePhoneNOActivity.this, "输入错误，请重新输入");
				}
			}
		});

		submitBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!CheckUtil.isEmpty(inputValidateNumber.getText().toString())) {
					executeSubmit();
				} else {
					DialogUtil.showToast(ValidatePhoneNOActivity.this, "验证码不能为空");
				}
			}
		});

	}

	// 手机号码格式验证
//	private boolean isMobileNO(String mobiles) {
//		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
//		Matcher m = p.matcher(mobiles);
//		return m.matches();
//	}

	boolean isSuccess = false;

	// 开始执行发送短信的task
	private boolean executeSendMsm() {
		
		// -----
		OpenPageDataTracer.getInstance().addEvent("获取验证码按钮");
		// -----
		
		ServiceRequest request = new ServiceRequest(API.sendVerifyCodeToTel);
		tel = userPhoneNumber.getText().toString();
		request.addData("typeTag", typeTag);
		request.addData("tel", tel);
		CommonTask.request(request, "正在获取验证码", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("获取验证码按钮");
				// -----
				
				//逻辑：如果typeTag=2，并且SimpleData.isSuccTag()==false,显示SimpleData.getMsg()，
				//并且询问用户，然后决定是否将typeTag设置成3继续提交
				if(typeTag==2 && dto.isSuccTag()==false){
					DialogUtil.showComfire(ValidatePhoneNOActivity.this, "提示",dto.getMsg() , new String[]{"确定","取消"}, 
							new Runnable() {
								@Override
								public void run() {
									typeTag=3;
									executeSendMsm();
								}
							},new Runnable() {
								@Override
								public void run() {
									
								}
							});
				} else {
					isSuccess = true;
					validateLayout.setVisibility(View.VISIBLE);
					DialogUtil.showToast(ValidatePhoneNOActivity.this, dto.getMsg());
					new CountDownTimer(60000, 1000) {
	
						public void onTick(long millisUntilFinished) {
							userPhoneNumber.setEnabled(false);
							getValidateNumber.setEnabled(false);
							getValidateNumber.setBackgroundResource(R.drawable.bg_new_order);
							countDowmNumber.setText("获取验证码" + " " + millisUntilFinished / 1000);
						}
	
						public void onFinish() {
							userPhoneNumber.setEnabled(true);
							getValidateNumber.setBackgroundResource(R.drawable.bg_new_red);
							getValidateNumber.setEnabled(true);
//							countDowmNumber.setTextColor(getResources().getColor(R.color.background_color_gray));
							countDowmNumber.setText("获取验证码");
						}
					}.start();
				}
			}

			@Override
			protected void onError(int code, String message) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("获取验证码按钮");
				// -----
				
				DialogUtil.showToast(ValidatePhoneNOActivity.this, message);
				userPhoneNumber.setEnabled(true);
				getValidateNumber.setBackgroundResource(R.drawable.bg_green);
				getValidateNumber.setEnabled(true);
				countDowmNumber.setTextColor(getResources().getColor(R.color.background_color_gray));
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
		OpenPageDataTracer.getInstance().addEvent("确认验证码按钮");
		// -----
		
		String tel = userPhoneNumber.getText().toString().trim();
		String code = inputValidateNumber.getText().toString().trim();
		ServiceRequest request = new ServiceRequest(API.verifyTelCode);
		request.addData("typeTag", typeTag);
		request.addData("tel", tel);
		request.addData("code", code);
		CommonTask.request(request, "正在提交", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("确认验证码按钮");
				// -----
				
				SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
				finish();
			};

			@Override
			protected void onError(int code, String message) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("确认验证码按钮");
				// -----
				
				DialogUtil.showToast(ValidatePhoneNOActivity.this, message);
				if (debug) {
					doTest2();
				}
			}
		});

	}

	// ------------------模拟数据开始-------------------------

	private void doTest(String tel) {
		if (("13000000000").equals(tel)) {
			UserInfoDTO infoDTO = SessionManager.getInstance().getUserInfo(ValidatePhoneNOActivity.this);
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
			SessionManager.getInstance().setUserInfo(ValidatePhoneNOActivity.this, infoDTO);
			SessionManager.getInstance().setIsUserLogin(ValidatePhoneNOActivity.this, true);
			isSuccess = true;
			validateLayout.setVisibility(View.VISIBLE);
			DialogUtil.showToast(ValidatePhoneNOActivity.this, "短信：120");
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
			SessionManager.getInstance().setIsUserLogin(ValidatePhoneNOActivity.this, true);
			Bundle bundle = new Bundle();
			ActivityUtil.jump(ValidatePhoneNOActivity.this, UserCenterActivity.class, 0, bundle);
			finish();
		}
	}
	
	// ------------------模拟数据结束-------------------------
	
	void goBack(){
		if(typeTag==1){
			Bundle bundle = new Bundle();
			ActivityUtil.jump(this, UserLoginActivity.class, 0, bundle);
			finish();
		}else{
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		 goBack();
	}
}
