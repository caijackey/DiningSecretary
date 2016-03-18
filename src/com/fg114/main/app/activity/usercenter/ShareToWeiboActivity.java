package com.fg114.main.app.activity.usercenter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.ShareToWeiboTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;
import com.fg114.main.wxapi.WeixinUtils;

public class ShareToWeiboActivity extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ToggleButton chbShareToSina;
	private ToggleButton chbShareToTX;
	private UserInfoDTO infoDTO;
	private boolean isSINAWBbinding;
	private boolean isQQWBbinding;

	private EditText etDetail;
	private Button commentVoiceInputButton;

	private ShareToWeiboTask shareToWeiboTask;
	// 页面传入参数
	private int mTypeTag; // 1:分享餐厅 2：软件分享 3：推荐分享
	private String mDetailInfo;
	private String Uuid;

	// BUNDLE_KEY_TYPR
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) {
			DialogUtil.showToast(ContextUtil.getContext(), "请稍后再试...");
			finish();
		}
		mDetailInfo = bundle.getString(Settings.BUNDLE_KEY_WEIBO_DETAIL);
		Uuid = bundle.getString(Settings.BUNDLE_REST_ID);
		mTypeTag = bundle.getInt(Settings.BUNDLE_KEY_TYPE);
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
		refrushUI();
	}

	private void refrushUI() {
		infoDTO = SessionManager.getInstance().getUserInfo(this);
		if (infoDTO.isSinaBindTag() && !infoDTO.isSinaWeiboExpired()) {
			// 绑定没有过期
			chbShareToSina.setBackgroundResource(R.drawable.sina_check_weibo);
			chbShareToSina.setChecked(false);
			isSINAWBbinding = true;

		} else {
			// 绑定无效
			chbShareToSina.setBackgroundResource(R.drawable.sina_web_check);
			isSINAWBbinding = false;
		}
		if (infoDTO.isQqBindTag() && !infoDTO.isQQWeiboExpired()) {
			chbShareToTX.setBackgroundResource(R.drawable.tx_check_weibo);
			chbShareToTX.setChecked(false);
			isQQWBbinding = true;
		} else {
			// 绑定无效
			chbShareToTX.setBackgroundResource(R.drawable.tx_web_check);
			isQQWBbinding = false;
		}
	}

	public void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("微博分享");
		this.getBtnOption().setText(R.string.text_title_share_weibo_title);
		this.getBtnGoBack().setText(R.string.text_button_back);
		getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		this.setFunctionLayoutGone();
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.share_weibo, null);
		etDetail = (EditText) contextView.findViewById(R.id.share_to_weibo_detail);
		commentVoiceInputButton = (Button) contextView.findViewById(R.id.share_to_weibo_detail_voice);
		chbShareToSina = (ToggleButton) contextView.findViewById(R.id.restaurant_comment_submit_chkShareSina);
		chbShareToTX = (ToggleButton) contextView.findViewById(R.id.restaurant_comment_submit_chkShareTX);
		etDetail.setText(mDetailInfo);
		if (mTypeTag == 1 || mTypeTag == 2|| mTypeTag == 3|| mTypeTag == 4|| mTypeTag == 5||mTypeTag==6||mTypeTag==100) {
			chbShareToSina.setVisibility(View.VISIBLE);
			chbShareToTX.setVisibility(View.VISIBLE);
		}

		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {
			eng.bindButtonAndEditText(commentVoiceInputButton, etDetail);
		}
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (chbShareToSina.isChecked() || chbShareToTX.isChecked()) {
					executeShareToWeiboTask();
				} else {
					DialogUtil.showAlert(ShareToWeiboActivity.this, false, "请选择需要分享的微博", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});
				}
			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		chbShareToSina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!isSINAWBbinding) {
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {
						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) {
								isSINAWBbinding = true;
							} else {
								isSINAWBbinding = false;
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).requestWeiboShare(null);
				}
			}
		});

		chbShareToTX.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 绑定微博
				if (!isQQWBbinding) {
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {
						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) {
								isQQWBbinding = true;
							} else {
								isQQWBbinding = false;
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO).requestWeiboShare(null);

				}

			}
		});

	}

	/**
	 * 构建分享参数
	 * 
	 * @return
	 */
	private String getShareString() {
		StringBuffer sbShare = new StringBuffer();
		if (chbShareToSina.isChecked()) {
			sbShare.append("sina:1;");
		} else {
			sbShare.append("sina:0;");
		}
		if (chbShareToTX.isChecked()) {
			sbShare.append("qq:1");
		} else {
			sbShare.append("qq:0");
		}

		return sbShare.toString();
	}

	private void executeShareToWeiboTask() {

		shareToWeiboTask = new ShareToWeiboTask(getString(R.string.text_info_uploading), this, mTypeTag, SessionManager.getInstance().getUserInfo(ShareToWeiboActivity.this).getToken(), Uuid, etDetail
				.getText().toString(), getShareString());
		shareToWeiboTask.execute(new Runnable() {

			@Override
			public void run() {
				shareToWeiboTask.closeProgressDialog();
				if (shareToWeiboTask.code == 0) {
					DialogUtil.showToast(getApplicationContext(), "分享成功");
					finish();
				} else {
					DialogUtil.showToast(getApplicationContext(), shareToWeiboTask.msg);
				}
			}
		});
	}
}
