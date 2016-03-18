package com.fg114.main.app.activity.Mdb;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MdbOrderDetailActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.service.dto.MdbFreeOrderInfoData;
import com.fg114.main.service.dto.MdbReceiptChkConfirmFormData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

public class MdbReceiptChkConfirmDataActivity extends MainFrameActivity {
	private String orderID = "";// 订单ID
	private String waiterNum = "";// 服务员号码
	private String userTel = "";// 用户手机号
	private TextView confirm_receipt_hint;
	private Button confirm_receipt_bt;
	private EditText waiter_name;
	private EditText waiter_phonenum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		Settings.mdbConsumeActivityClazz = getLastActivityClass();
		if (bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
			orderID = bundle.getString(Settings.BUNDLE_ORDER_ID);
		}

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

		executeGetMdbReceiptChkConfirmFormInfoTask();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private static Class<? extends Activity> mdbConsumeEnsureActivity = MdbConsumeEnsureActivity.class;

	private void initComponent() {
		// this.getTvTitle().setText("订单详情");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = mInflater.inflate(R.layout.mdb_confirm_receipt_activity, null);

		confirm_receipt_hint = (TextView) contextView.findViewById(R.id.confirm_receipt_hint);
		waiter_name = (EditText) contextView.findViewById(R.id.confirm_receipt_waiter_name);
		waiter_phonenum = (EditText) contextView.findViewById(R.id.confirm_receipt_waiter_phonenum);
		confirm_receipt_bt = (Button) contextView.findViewById(R.id.confirm_receipt_bt);

		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

	}

	private void executeGetMdbReceiptChkConfirmFormInfoTask() {
		ServiceRequest request = new ServiceRequest(API.getMdbReceiptChkConfirmFormInfo);
		request.addData("orderId", orderID);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<MdbReceiptChkConfirmFormData>() {

			@Override
			protected void onSuccess(MdbReceiptChkConfirmFormData dto) {
				setView(dto);

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);

			}
		});
	}

	private void completeMdbFreeOrder() {

		ServiceRequest request = new ServiceRequest(API.completeMdbFreeOrder);
		request.addData("orderId", orderID);
		request.addData("waiterNum", waiterNum);
		request.addData("userTel", userTel);

		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				if (dto != null) {
					orderID = dto.getUuid();
				}
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_ORDER_ID, orderID);
				ActivityUtil.jump(MdbReceiptChkConfirmDataActivity.this, MdbOrderDetailActivity.class, 0, bundle);
				MdbReceiptChkConfirmDataActivity.this.finish();
				if (getLastActivityClass() == mdbConsumeEnsureActivity) {
					MdbConsumeEnsureActivity.getInstance().finish();
				}

			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);

			}
		});
	}

	private void setView(MdbReceiptChkConfirmFormData dto) {
		if (dto == null) {
			return;
		}

		confirm_receipt_bt.setText(dto.postBtnName);

		this.getTvTitle().setText(dto.pageTitle);
		if (dto.needEnterWaiterNumTag) {
			waiter_name.setVisibility(View.VISIBLE);
			waiter_name.setHint(dto.waiterNumPlaceHolder);
		} else {
			waiter_name.setVisibility(View.GONE);
		}

		if (!CheckUtil.isEmpty(dto.userTel)) {
			waiter_phonenum.setText(dto.userTel);
		} else {
			waiter_phonenum.setHint(dto.userTelPlaceHolder);
		}

		confirm_receipt_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// TODO Auto-generated method stub
				waiterNum = waiter_name.getText().toString().trim();
				userTel = waiter_phonenum.getText().toString().trim();
				completeMdbFreeOrder();

			}
		});

		if (!CheckUtil.isEmpty(dto.hint)) {
			String s = dto.hint.replace("\r\n", "<br>");
			confirm_receipt_hint.setText(Html.fromHtml(s));
		}
	}
}
