package com.fg114.main.app.activity.Mdb;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.MdbPostOrderResultData;
import com.fg114.main.service.dto.MdbReceiptChkFormData;
import com.fg114.main.service.dto.MdbRfValidCodeData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 消费核对
 * 
 * @author
 * 
 */
public class MdbConsumeEnsureActivity extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private String orderId;
	private EditText Authnum;
	private EditText Cardnum;
	private MyImageView iv;
	private TextView PosExample;
	private Button ensure_btn;
	private MdbReceiptChkFormData mdbReceiptChkFormData;
	private String authnum;
	private String cardnum;
	private String validCodeUuid;
	private String validCodeNum;
	private EditText mdb_valid_code_num;
	private MyImageView mdb_valid_code_image;
	private View mdb_valid_code_lyout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// 初始化界面
		initComponent();
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		} else {
			executeGetMdbChkFormInfoDataTask();
		}

	}

	private static MdbConsumeEnsureActivity instance = null;

	public static MdbConsumeEnsureActivity getInstance() {
		return instance;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	/**
	 * 获得免单宝核对表单数据
	 */
	// TODO
	private void executeGetMdbChkFormInfoDataTask() {

		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getMdbReceiptChkFormInfo);

		OpenPageDataTracer.getInstance().addEvent("页面查询");

		CommonTask.request(request, "正在获取信息，请等待...", new CommonTask.TaskListener<MdbReceiptChkFormData>() {

			@Override
			protected void onSuccess(MdbReceiptChkFormData dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");

				// 设置列表适配器
				if (dto != null) {
					mdbReceiptChkFormData = dto;
				}
				setView(dto);

			}

			//
			@Override
			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// DialogUtil.showToast(RestaurantDetailActivity.this,
				// message);
				super.onError(code, message);

			}
		});
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("客服电话");
		this.getBtnOption().setVisibility(View.VISIBLE);

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				if (mdbReceiptChkFormData != null) {
					if (!CheckUtil.isEmpty(mdbReceiptChkFormData.helpTel)) {
						ActivityUtil.callSuper57(MdbConsumeEnsureActivity.this, mdbReceiptChkFormData.helpTel);
					}
				}
			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.mdb_consume_ensure, null);
		Authnum = (EditText) contextView.findViewById(R.id.mdb_consume_ensure_Auth_num);
		Cardnum = (EditText) contextView.findViewById(R.id.mdb_consume_ensure_card_num);
		PosExample = (TextView) contextView.findViewById(R.id.mdb_consume_ensure_pos_example);
		ensure_btn = (Button) contextView.findViewById(R.id.mdb_consume_ensure_btn);
		mdb_valid_code_num = (EditText) contextView.findViewById(R.id.mdb_valid_code_num);
		mdb_valid_code_image = (MyImageView) contextView.findViewById(R.id.mdb_valid_code_image);
		mdb_valid_code_lyout = (View) contextView.findViewById(R.id.mdb_valid_code_lyout);

		ensure_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("核对按钮");
				// -----
				authnum = Authnum.getText().toString().trim();
				cardnum = Cardnum.getText().toString().trim();
				validCodeNum = mdb_valid_code_num.getText().toString().trim();
				if (CheckUtil.isEmpty(authnum)) {
					DialogUtil.showToast(MdbConsumeEnsureActivity.this, "授权码不能为空");
					return;
				}
				if (CheckUtil.isEmpty(cardnum)) {
					DialogUtil.showToast(MdbConsumeEnsureActivity.this, "卡号不能为空");
					return;
				}
				if (CheckUtil.isEmpty(validCodeNum) && mdbReceiptChkFormData.needShowValidCodeTag) {
					DialogUtil.showToast(MdbConsumeEnsureActivity.this, "验证码不能为空");
					return;
				}

				PostMdbFreeOrderTask();

			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void setView(final MdbReceiptChkFormData dto) {

		if (dto == null) {
			return;
		}

		this.getTvTitle().setText(dto.pageTitle);
		mdb_valid_code_num.setText("");

		// 下划线
		PosExample.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		PosExample.setText(dto.helpBtnName);

		Authnum.setHint(dto.authNumPlaceHolder);

		Cardnum.setHint(dto.cardNumPlaceHolder);

		ensure_btn.setText(dto.postBtnName);

		if (dto.needShowValidCodeTag) {
			mdb_valid_code_lyout.setVisibility(View.VISIBLE);
		} else {
			mdb_valid_code_lyout.setVisibility(View.GONE);
		}

		PosExample.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DialogUtil.createImageViewPanel(MdbConsumeEnsureActivity.this, MdbConsumeEnsureActivity.this.contextView, dto.helpPicUrl);

			}
		});

		setValidCodeView(dto.validCodeData);

	}

	// 设置验证码View
	private void setValidCodeView(MdbRfValidCodeData data) {
		if (data == null) {
			return;
		}
		validCodeUuid = data.uuid;

		if (data.width != 0 && data.height != 0) {
			double scale = 1.0000 * data.height / data.width;
			// 宽度40dip
			int PicWidth = UnitUtil.dip2px(90);
			int PicHeight = (int) (PicWidth * scale);
			mdb_valid_code_image.setLayoutParams(new LinearLayout.LayoutParams(PicWidth, PicHeight));

		}
		mdb_valid_code_image.setImageByUrl(data.picUrl, false, 0, ScaleType.FIT_XY);

		mdb_valid_code_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				executeGetMdbReceiptChkFormValidCodeInfoTask();
			}
		});
	}

	/**
	 * 获得免单宝核对表单验证码信息数据
	 */
	private void executeGetMdbReceiptChkFormValidCodeInfoTask() {

		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getMdbReceiptChkFormValidCodeInfo);

		OpenPageDataTracer.getInstance().addEvent("页面查询");

		CommonTask.requestMutely(request, new CommonTask.TaskListener<MdbRfValidCodeData>() {

			@Override
			protected void onSuccess(MdbRfValidCodeData dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				setValidCodeView(dto);

			}

			//
			@Override
			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// DialogUtil.showToast(RestaurantDetailActivity.this,
				// message);
				super.onError(code, message);

			}
		});
	}

	/**
	 * 提交订单表单
	 */
	private void PostMdbFreeOrderTask() {

		ServiceRequest request = new ServiceRequest(ServiceRequest.API.postMdbFreeOrder);
		request.addData("authNum", authnum);// 授权号
		request.addData("cardNum", cardnum);// 卡号
		request.addData("validCodeId", validCodeUuid);// 验证码Id
		request.addData("validCodeNum", validCodeNum);// 验证码

		OpenPageDataTracer.getInstance().addEvent("页面查询");

		CommonTask.requestMutely(request, new CommonTask.TaskListener<MdbPostOrderResultData>() {

			@Override
			protected void onSuccess(MdbPostOrderResultData dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");

				if (dto != null) {
					if (dto.succTag) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, dto.orderId);
						ActivityUtil.jump(MdbConsumeEnsureActivity.this, MdbReceiptChkConfirmDataActivity.class, 0, bundle);
					} else {
						DialogUtil.showToast(MdbConsumeEnsureActivity.this, dto.msg);
						setView(dto.formData);
						mdbReceiptChkFormData = dto.formData;
					}
				}

			}

			//
			@Override
			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// DialogUtil.showToast(RestaurantDetailActivity.this,
				// message);
				super.onError(code, message);

			}
		});
	}

}
