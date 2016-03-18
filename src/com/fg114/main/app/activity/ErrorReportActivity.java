package com.fg114.main.app.activity;


import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;


import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.PostErrorReportTask;
import com.fg114.main.service.task.PostFeedBackTask;
import com.fg114.main.speech.asr.OnFinishListener;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.speech.asr.RecognitionResult;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import android.text.method.*;
import android.text.*;

/**
 * 错误提交
 * @author wufucheng
 *
 */
public class ErrorReportActivity extends MainFrameActivity {
	
	private static final String TAG = "ErrorReportActivity";
	
	//传入参数
	private int fromPage = 0;
	
	//界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private TextView messageOnTop;
	private TextView messageOnBottom;
	private EditText multipleLineText;
	private EditText singleLineText;
	private EditText emailText;
	private EditText etName;
	private Button btnClear;
	private Button btnSubmit;
	private Button voiceButtonSingleLine;
	private Button voiceButtonMultipleLine;
	private int typeTag;
	private String uuid; //如果设置了，表示是餐厅或者菜品的uuid
	
	//ui field
	private String name;		//用户名
	private String email;		//电邮或手机号
	private String errorInfo;	//错误信息
	
	//控制整个页面是否是反馈页面
	private boolean isFeedBack=false;
	 
	/* 
	 * ErrorReportTypeData
	 * 其中的错误功能类型：
		 * 1:默认  
		 * 2:单行文本框  
		 * 3:输入多行文本和email 	
		 * 10:商户信息错误 
		 * 11:地图纠正
	 * keyboardTypeTag: //1:默认   2:数字
	 * 注意：本页整合了两个功能：２和３，根据该标志来显示不同的控件
	 * added by xujianjun, 2011-12-20
	 */
	private ErrorReportTypeData error;
	
	//Task
	private PostErrorReportTask postErrorReportTask;
	private PostFeedBackTask postFeedBackTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		uuid=bundle.getString(Settings.UUID);
		error=(ErrorReportTypeData)bundle.getSerializable("ErrorReportTypeData");
		typeTag=bundle.getInt("typeTag"); //大类标志    1：餐馆  2：菜系，3:外卖
		
		if(error==null){
			//如果错误类型数据未设置，本页面则成为“信息反馈”页面，需要调用信息反馈的Task
			/*DialogUtil.showToast(this, "错误类型未设置！");
			this.finish();*/
			isFeedBack=true;
			error=new ErrorReportTypeData();
			error.setTypeId("12");//12:默认——其他错误报告
			error.setTypeName("建议");
			error.setFuncTag(3);
			error.setInputBoxTitle("别为了软件中不好的体验而放弃我们，给我们建议吧，我们会变得更好。");
		}
		if(typeTag<0){
			isFeedBack=true;
		}
		//初始化界面
		initComponent();
		//检查网络是否连通
        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
        if (!isNetAvailable) {
        	//没有网络的场合，去提示页
        	Bundle bund = new Bundle();
        	bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
	    	ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
        }
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		
		//设置标题栏
		this.getTvTitle().setText(error.getTypeName());
		this.getBtnGoBack().setText(R.string.text_button_back);
		if (getLastActivityClass() == IndexActivity.class) {
			this.getBtnGoBack().setText(R.string.text_button_goto_index);
		}
		//内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.error_report, null);
		messageOnTop=(TextView) contextView.findViewById(R.id.message_on_top);
		messageOnBottom=(TextView) contextView.findViewById(R.id.message_on_bottom);
		multipleLineText = (EditText) contextView.findViewById(R.id.multipleLine_text);
		singleLineText = (EditText) contextView.findViewById(R.id.singleLine_text);
		emailText = (EditText) contextView.findViewById(R.id.error_report_email);
		etName = (EditText) contextView.findViewById(R.id.error_report_etName);
		voiceButtonSingleLine=(Button) contextView.findViewById(R.id.voice_recognition_button_singleLine);
		voiceButtonMultipleLine=(Button) contextView.findViewById(R.id.voice_recognition_button_multipleLine);
		btnClear = (Button) contextView.findViewById(R.id.error_report_btnClear);		
		LinearLayout mutipleLine=(LinearLayout)contextView.findViewById(R.id.multipleLine);
		LinearLayout singleLine=(LinearLayout)contextView.findViewById(R.id.singleLine);
		
		//
		if(isFeedBack){
			multipleLineText.setHint("请输入反馈信息，支持语音输入哦！");
		}
		
		messageOnTop.setText(error.getInputBoxTitle());
		//按单行报错或多行报错，控制文本框的显示隐藏, -999:定单价格报错
		switch(error.getFuncTag()){
		
			case 2: //单行
				mutipleLine.setVisibility(View.GONE);
				emailText.setVisibility(View.VISIBLE);
				messageOnBottom.setVisibility(View.GONE);
				singleLine.setVisibility(View.VISIBLE);
				//当是数字类型时，默认键盘是数字
				if(error.getKeyboardTypeTag()==2){
					singleLineText.setKeyListener(new TextKeyListener(TextKeyListener.Capitalize.NONE,false){

						@Override
						public int getInputType() {
							//super.getInputType();
							return android.text.InputType.TYPE_CLASS_NUMBER;
						}
						
					});
				}

				break;
				
			case 3: //多行
				mutipleLine.setVisibility(View.VISIBLE);
				emailText.setVisibility(View.VISIBLE);
				messageOnBottom.setVisibility(View.GONE); //底部文字不要了
				singleLine.setVisibility(View.GONE);				
				break;
				
			case -999: //单行，就餐金额报错
				mutipleLine.setVisibility(View.GONE);
				emailText.setVisibility(View.GONE);
				messageOnBottom.setVisibility(View.GONE);
				singleLine.setVisibility(View.VISIBLE);
				singleLineText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
				
				//当是数字类型时，默认键盘是数字
				if(error.getKeyboardTypeTag()==2){
					singleLineText.setKeyListener(new TextKeyListener(TextKeyListener.Capitalize.NONE,false){

						@Override
						public int getInputType() {
							//super.getInputType();
							return android.text.InputType.TYPE_CLASS_NUMBER;
						}
						
					});
				}

				break;
			default:
				this.finish();				
			
		}
		
		btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				switch(error.getFuncTag()){
				
					case 2: //单行
						if(!singleLineText.getText().toString().equals("")) {
							DialogUtil.showAlert(ErrorReportActivity.this, true, getString(R.string.text_dialog_clear_confirm), 
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											singleLineText.setText("");											
										}
									},
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});
						}
						break;
					case 3: //多行
						if(!multipleLineText.getText().toString().equals("") || !emailText.getText().toString().equals("")) {
							DialogUtil.showAlert(ErrorReportActivity.this, true, getString(R.string.text_dialog_clear_confirm), 
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											multipleLineText.setText("");
											emailText.setText("");
											etName.setText("");
										}
									},
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});
						}				
						break;
					case -999: //就餐金额报错
						if(!singleLineText.getText().toString().equals("")) {
							DialogUtil.showAlert(ErrorReportActivity.this, true, getString(R.string.text_dialog_clear_confirm), 
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											singleLineText.setText("");											
										}
									},
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});
						}
						break;
					default:
						finish();				
					
				}
						
			}
		});
		btnSubmit = (Button) contextView.findViewById(R.id.error_report_btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if(isFeedBack){
					executePostFeedBackTask();
				}
				else{
					executePostErrorReportTask();
				}
				
			}
		});
		
		//绑定语音按钮和单行框---------------------------
		RecognitionEngine eng1=RecognitionEngine.getEngine(this);
		if(eng1!=null){
			eng1.bindButtonAndEditText(voiceButtonSingleLine, singleLineText,0,null);
		}		
		//----------------------------------------------
		//绑定语音按钮和多行框---------------------------
		RecognitionEngine eng2=RecognitionEngine.getEngine(this);
		if(eng2!=null){
			eng2.bindButtonAndEditText(voiceButtonMultipleLine, multipleLineText);
		}		
		//----------------------------------------------
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
	
	private void setLoginState() {
		this.getBtnOption().setVisibility(View.INVISIBLE);
		//如果未登录，显示登录按钮
		if(!SessionManager.getInstance().isUserLogin(this)){
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText("登录");
			this.getBtnOption().setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bund = new Bundle();
			    	ActivityUtil.jump(ErrorReportActivity.this, com.fg114.main.app.activity.usercenter.UserLoginActivity.class, 0, bund);
				}
			});
			//设置默认email
			//emailText.setText(SessionManager.getInstance().getUserInfo(ErrorReportActivity.this).getEmail());
		}
		else{
			//如果登录了，在email框中填入用户email
			//emailText.setText(SessionManager.getInstance().getUserInfo(this).getEmail());
		}
		
	}

	/**
     * 提交错误
     */
	private void executePostErrorReportTask() {
		
		if (!checkInput()) {
			return;
		}
		
		postErrorReportTask = new PostErrorReportTask(
										typeTag,
										error.getFuncTag(),										
										error.getTypeId(),
										error.getTypeName(),
										getString(R.string.text_info_loading), 
										this, 
										name,
										email,
										uuid,
										errorInfo,
										new Runnable() {
											
											@Override
											public void run() {
												ErrorReportActivity.this.finish();												
											}
										});
		
		postErrorReportTask.execute();
	}
	/**
     * 提交反馈信息
     */
	private void executePostFeedBackTask() {
		
		if (!checkInput()) {
			return;
		}
		
		postFeedBackTask = new PostFeedBackTask(
										typeTag,
										error.getFuncTag(),										
										error.getTypeId(),
										error.getTypeName(),
										getString(R.string.text_info_loading), 
										this, 
										name,
										email,
										errorInfo,new Runnable() {
											
											@Override
											public void run() {
												ErrorReportActivity.this.finish();												
											}
										});
		
		postFeedBackTask.execute();
	}	
	/**
	 * check及收集输入信息
	 */
	private boolean checkInput() {
		
		switch(error.getFuncTag()){
		
			case 2: //单行
				//如果登录了，则取用户名
				if(SessionManager.getInstance().isUserLogin(this)){
					etName.setText(SessionManager.getInstance().getUserInfo(this).getNickName());
				}
				name = etName.getText().toString().trim();
				errorInfo = singleLineText.getText().toString().trim();
				email = emailText.getText().toString().trim();
				if(CheckUtil.isEmpty(errorInfo)) {
					if(Settings.ERROR_REPORT_TITLE_ADD_PHONE_IN_REST_DETAIL.equals(this.getTvTitle().getText())
							|| Settings.ERROR_REPORT_TITLE_WRONG_PHONE_NO_IN_REST_DETAIL.equals(this.getTvTitle().getText())){
						//添加餐厅电话，或者电话号码报错时,提示特殊的验证信息
						ViewUtils.setError(singleLineText, getString(R.string.text_info_empty_phone));						
					}else{
						ViewUtils.setError(singleLineText, getString(R.string.text_info_empty_error));						
					}
					singleLineText.requestFocus();
					
					return false;
				}
				break;
			case 3: //多行
				//如果登录了，则取用户名
				if(SessionManager.getInstance().isUserLogin(this)){
					etName.setText(SessionManager.getInstance().getUserInfo(this).getNickName());
				}
				name = etName.getText().toString().trim();
				
				errorInfo = multipleLineText.getText().toString().trim();
				if(CheckUtil.isEmpty(errorInfo)) {
					ViewUtils.setError(multipleLineText, getString(R.string.text_info_empty_error));
					multipleLineText.requestFocus();
					return false;
				}
				if (errorInfo.length() < 5) {
					ViewUtils.setError(multipleLineText, "提交的内容请至少输入5个字");
					multipleLineText.requestFocus();
					return false;
				}
				if (errorInfo.length() > 200) {
					ViewUtils.setError(multipleLineText,"您提交的内容不能超过200字");
					multipleLineText.requestFocus();
					return false;
				}

				email = emailText.getText().toString().trim();
/*				if (!email.equals("")) {
					if (!email
							.matches("\\w+@\\w+\\.(com\\.cn)|\\w+@\\w+\\.(com|cn)")
							&& !email.matches("^[1][3-8]\\d{9}$")) {
						emailText
								.setError(getString(R.string.text_info_invalid_email_phone));
						emailText.requestFocus();
						return false;
					}
				}*/
	
				break;
			case -999: //就餐金额报错
				if(SessionManager.getInstance().isUserLogin(this)){
					etName.setText(SessionManager.getInstance().getUserInfo(this).getNickName());
				}
				name = etName.getText().toString().trim();
				errorInfo = singleLineText.getText().toString().trim();
				if(CheckUtil.isEmpty(errorInfo)) {
					ViewUtils.setError(singleLineText, getString(R.string.text_info_empty_order_price_error));
					singleLineText.requestFocus();
					return false;
				}
				if(!CheckUtil.isDouble(errorInfo)){
					ViewUtils.setError(singleLineText, getString(R.string.text_info_order_price_error));
					singleLineText.requestFocus();
					return false;
				}
				break;
			default:
				this.finish();
	
			}

		return true;
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		setLoginState();
	}

	@Override
	public void finish() {
		ViewUtils.hideSoftInput(this, contextView);
		super.finish();
	}
	
	
}
