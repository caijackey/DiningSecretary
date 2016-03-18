package com.fg114.main.app.activity.usercenter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.wxapi.WeixinUtils;

/**
 * 分享到微信
 * 
 * @author xujianjun,2013-01-08
 * 
 */
public class ShareToWeiXinActivity extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	private EditText etDetail;
	private Button commentVoiceInputButton;
	// private int mTypeTag; // 1:分享餐厅 2：软件分享
	// 页面传入参数
	private int fromPage;
	private String mDetailInfo;
	private String Uuid;
	private String restName;
	private String restImageUrl;
	private int shareType;
	private String restLinkUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		Uuid = bundle.getString(Settings.BUNDLE_REST_ID);
		restName = bundle.getString(Settings.BUNDLE_REST_NAME);
		restImageUrl = bundle.getString(Settings.BUNDLE_REST_IMAGE_URL);
		restLinkUrl = bundle.getString(Settings.BUNDLE_REST_LINK_URL);
		mDetailInfo = bundle.getString(Settings.BUNDLE_KEY_SHARE_DETAIL);
		shareType = bundle.getInt(Settings.BUNDLE_KEY_TYPE);
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
		this.getBtnOption().performClick();
	}
	public void initComponent() {

		if (shareType == 1) {
			this.getTvTitle().setText("微信分享");
		} else {
			this.getTvTitle().setText("微信朋友圈分享");

		}

		this.getBtnOption().setText("分享");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.setFunctionLayoutGone();
		
		getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.share_weibo, null);
		etDetail = (EditText) contextView.findViewById(R.id.share_to_weibo_detail);
		commentVoiceInputButton = (Button) contextView.findViewById(R.id.share_to_weibo_detail_voice);

		etDetail.setText(mDetailInfo);
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {
			eng.bindButtonAndEditText(commentVoiceInputButton, etDetail);
		}
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				switch (shareType) {
					case 1:
						if (WeixinUtils.isWeixinAvailable()) {
							//没有餐厅id，说明是软件分享 
							if(CheckUtil.isEmpty(Uuid)){
								WeixinUtils.sendText(etDetail.getText().toString());
							}else{
								WeixinUtils.sendTextAndPicture(restName, etDetail.getText().toString(), restImageUrl, restLinkUrl);
							}
							finish();
						} else {
							DialogUtil.showAlert(ShareToWeiXinActivity.this, "提示", "您的手机没有安装微信!");
						}
						break;
					case 2:
						if (WeixinUtils.isWeixinAvailable()) {
							//没有餐厅id，说明是软件分享 
							if(CheckUtil.isEmpty(Uuid)){
								WeixinUtils.sendFriendText(etDetail.getText().toString());
							}else{
								WeixinUtils.sendFriendTextAndPicture(restName, etDetail.getText().toString(), restImageUrl, restLinkUrl);
							}
							finish();
						} else {
							DialogUtil.showAlert(ShareToWeiXinActivity.this, "提示", "您的手机没有安装微信!");
						}
						break;

					default:
						break;
				}

			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

}
