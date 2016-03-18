package com.fg114.main.app.activity.usercenter;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.app.activity.HotDistrictActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.RestRecomListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

public class OpinionErronReportActivity extends MainFrameActivity {
	private View contextView;
	private LayoutInflater mInflater;
	private View opinion_error_view;
	private EditText opinion_error_memo;
	private Button resandfood_problem;
	private Button takeaway_problem;
	private Button mibi_problem;
	private Button bug_problem;
	private Button other_problem;
	private Button opinion_error_memo_btvoice;
	private EditText error_report_email;
	private int typeTag = 0;
	private Button error_report_sumbit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("意见反馈", "");
		// ----------------------------
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
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("意见反馈", "");
		// ----------------------------
	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("意见反馈");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.opinion_erron_report_act, null);
		opinion_error_view = contextView.findViewById(R.id.opinion_error_view);
		opinion_error_memo = (EditText) contextView.findViewById(R.id.opinion_error_memo);
		resandfood_problem = (Button) contextView.findViewById(R.id.resandfood_problem);
		takeaway_problem = (Button) contextView.findViewById(R.id.takeaway_problem);
		mibi_problem = (Button) contextView.findViewById(R.id.mibi_problem);
		bug_problem = (Button) contextView.findViewById(R.id.bug_problem);
		other_problem = (Button) contextView.findViewById(R.id.other_problem);
		opinion_error_memo_btvoice = (Button) contextView.findViewById(R.id.opinion_error_memo_btvoice);
		error_report_email = (EditText) contextView.findViewById(R.id.error_report_email);
		error_report_sumbit=(Button) contextView.findViewById(R.id.error_report_sumbit);

		resandfood_problem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (typeTag == 1) {
					resandfood_problem.setBackgroundResource(R.drawable.huikuang);
					resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
					typeTag = 0;
				} else {
					resandfood_problem.setBackgroundResource(R.drawable.redkuang);
					resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_red_3));
					typeTag = 1;
				}
				takeaway_problem.setBackgroundResource(R.drawable.huikuang);
				takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				mibi_problem.setBackgroundResource(R.drawable.huikuang);
				mibi_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				bug_problem.setBackgroundResource(R.drawable.huikuang);
				bug_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				other_problem.setBackgroundResource(R.drawable.huikuang);
				other_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));

			}
		});
		takeaway_problem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				resandfood_problem.setBackgroundResource(R.drawable.huikuang);
				resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				if (typeTag == 2) {
					typeTag = 0;
					takeaway_problem.setBackgroundResource(R.drawable.huikuang);
					takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				} else {
					typeTag = 2;
					takeaway_problem.setBackgroundResource(R.drawable.redkuang);
					takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_red_3));
				}
				mibi_problem.setBackgroundResource(R.drawable.huikuang);
				mibi_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				bug_problem.setBackgroundResource(R.drawable.huikuang);
				bug_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				other_problem.setBackgroundResource(R.drawable.huikuang);
				other_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
			}
		});
		mibi_problem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				resandfood_problem.setBackgroundResource(R.drawable.huikuang);
				resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				takeaway_problem.setBackgroundResource(R.drawable.huikuang);
				takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				if (typeTag == 3) {
					typeTag = 0;
					mibi_problem.setBackgroundResource(R.drawable.huikuang);
					mibi_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				} else {
					typeTag = 3;
					mibi_problem.setBackgroundResource(R.drawable.redkuang);
					mibi_problem.setTextColor(getResources().getColor(R.color.text_color_red_3));
				}
				bug_problem.setBackgroundResource(R.drawable.huikuang);
				bug_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				other_problem.setBackgroundResource(R.drawable.huikuang);
				other_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
			}
		});
		bug_problem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				resandfood_problem.setBackgroundResource(R.drawable.huikuang);
				resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				takeaway_problem.setBackgroundResource(R.drawable.huikuang);
				takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				mibi_problem.setBackgroundResource(R.drawable.huikuang);
				mibi_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));				
				if (typeTag == 4) {
					typeTag = 0;
					bug_problem.setBackgroundResource(R.drawable.huikuang);
					bug_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				} else {
					typeTag = 4;
					bug_problem.setBackgroundResource(R.drawable.redkuang);
					bug_problem.setTextColor(getResources().getColor(R.color.text_color_red_3));
				}
				other_problem.setBackgroundResource(R.drawable.huikuang);
				other_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
			}
		});
		other_problem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				resandfood_problem.setBackgroundResource(R.drawable.huikuang);
				resandfood_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				takeaway_problem.setBackgroundResource(R.drawable.huikuang);
				takeaway_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				mibi_problem.setBackgroundResource(R.drawable.huikuang);
				mibi_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				bug_problem.setBackgroundResource(R.drawable.huikuang);
				bug_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				if (typeTag == 5) {
					typeTag = 0;
					other_problem.setBackgroundResource(R.drawable.huikuang);
					other_problem.setTextColor(getResources().getColor(R.color.text_color_deep_gray));
				} else {
					typeTag = 5;
					other_problem.setBackgroundResource(R.drawable.redkuang);
					other_problem.setTextColor(getResources().getColor(R.color.text_color_red_3));
				}
			}
		});
		opinion_error_memo_btvoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.showVoiceDialogForSearch(OpinionErronReportActivity.this, 8, new ActivityUtil.OnRecognizedFinishListener() {

					@Override
					public void onRecognizedFinish(String text) {
						// -----
						OpenPageDataTracer.getInstance().addEvent("语音按钮");
						// -----
						String s = opinion_error_memo.getText().toString();
						opinion_error_memo.setText(s + text);
						
					}
				});
			}
		});
		
		error_report_sumbit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("提交按钮");
				// -----
				
				if (CheckUtil.isEmpty(opinion_error_memo.getText().toString())) {
					DialogUtil.showToast(OpinionErronReportActivity.this, "请输入意见反馈");
					return;
				} 
				if(!SessionManager.getInstance().isUserLogin(OpinionErronReportActivity.this)&&CheckUtil.isEmpty(error_report_email.getText().toString())){
					DialogUtil.showToast(OpinionErronReportActivity.this, "请输入Email或者登录");
					return;
				}
				executePostErrorReportTask();
				
			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void executePostErrorReportTask() {
		ServiceRequest request = new ServiceRequest(API.postAppFeedback);
		request.addData("typeTag", typeTag);// 0:未选择 1：订餐厅问题 2：叫外卖问题 3：秘币问题 4：软件Bug问题
										// 5：其他问题
		request.addData("detail", opinion_error_memo.getText().toString());
		request.addData("email", error_report_email.getText().toString()+"");
		CommonTask.request(request, "", new CommonTask.TaskListener<SimpleData>() {
			@Override
			protected void onSuccess(SimpleData dto) {
				DialogUtil.showToast(OpinionErronReportActivity.this, dto.getMsg());
				finish();
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}
}
