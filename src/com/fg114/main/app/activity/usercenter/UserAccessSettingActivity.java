package com.fg114.main.app.activity.usercenter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.MediatorActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;
import com.fg114.main.weibo.task.WeiboUnBindTask;

/**
 * 用户账号设置
 * 
 * @author zhangyifan
 * 
 */
public class UserAccessSettingActivity extends MainFrameActivity {

	private boolean debug = false;
	private static final String TAG = "UserAccessSettingActivity";

	// 本地缓存数据
	private LayoutInflater mInflater;
	private View contextView;
	private RelativeLayout userNameLayout;// 名称
	private TextView userName;
	private RelativeLayout figureGenderLayout;// 性别
	private Button figureGender;
	private RelativeLayout phoneNumberLayout;// 手机号码
	private TextView phoneNumber;
	private TextView phoneNumberStatus;
	private RelativeLayout sinaBlogLayout;// sina微博
	private TextView sinaBlogName;
	private TextView sinaBlogStatus;
	private RelativeLayout tencentBlogLayout;// 腾讯微博
	private TextView tencentBlogName;
	private TextView tencentBlogStatus;
	private boolean isNeedSINAWBbinding;
	private boolean isNeedQQWBbinding;
	private UserInfoDTO infoDTO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("账户设置", "");
		// ----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		infoDTO = SessionManager.getInstance().getUserInfo(this);
		// 获得本地缓存数据
		initComponent();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("账户设置", "");
		// ----------------------------
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("账户设置");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setText("注销");
		this.getBtnOption().setOnClickListener(new OnClickListener() {
			/**
			 * 注销
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showAlert(UserAccessSettingActivity.this, true, "注销", "确定注销？", "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 确定
								ServiceRequest request = new ServiceRequest(API.logout);
								// -----
								OpenPageDataTracer.getInstance().addEvent("注销按钮");
								// -----
								CommonTask.request(request, "正在注销", new CommonTask.TaskListener<Void>() {

									@Override
									protected void onSuccess(Void dto) {
										// -----
										// OpenPageDataTracer.getInstance().endEvent("注销按钮");
										// -----
										SessionManager.getInstance().setIsUserLogin(UserAccessSettingActivity.this,
												false);
										SessionManager.getInstance().setUserInfo(UserAccessSettingActivity.this,
												new UserInfoDTO());
										DialogUtil.showToast(UserAccessSettingActivity.this, "注销成功");
										finish();
									};

									protected void onError(int code, String message) {
										// -----
										// OpenPageDataTracer.getInstance().endEvent("注销按钮");
										// -----
										// 失败
										DialogUtil.showToast(UserAccessSettingActivity.this, message);

									};
								});
							}

						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 取消
								dialog.dismiss();
							}

						});

			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.account_setting, null);
		userNameLayout = (RelativeLayout) contextView.findViewById(R.id.user_name_layout);
		userName = (TextView) contextView.findViewById(R.id.user_name);
		figureGenderLayout = (RelativeLayout) contextView.findViewById(R.id.figure_gender_layout);
		figureGender = (Button) contextView.findViewById(R.id.figure_gender);
		phoneNumberLayout = (RelativeLayout) contextView.findViewById(R.id.phone_number_layout);
		phoneNumber = (TextView) contextView.findViewById(R.id.phone_number);
		phoneNumberStatus = (TextView) contextView.findViewById(R.id.phone_number_status);
		sinaBlogLayout = (RelativeLayout) contextView.findViewById(R.id.sina_blog_layout);
		sinaBlogName = (TextView) contextView.findViewById(R.id.sina_blog_name);
		sinaBlogStatus = (TextView) contextView.findViewById(R.id.sina_blog_status);
		tencentBlogLayout = (RelativeLayout) contextView.findViewById(R.id.tencent_blog_layout);
		tencentBlogName = (TextView) contextView.findViewById(R.id.tencent_blog_name);
		tencentBlogStatus = (TextView) contextView.findViewById(R.id.tencent_blog_status);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// 更改昵称
		userNameLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("昵称按钮");
				// -----
				showDialog("修改昵称", userName, 0);
			}
		});

		// 更改性别
		figureGender.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 500);
				// 选中true为女
				if (figureGender.isSelected()) {
					figureGender.setSelected(false);
				} else {
					figureGender.setSelected(true);
				}
				executeSendSexTag(figureGender.isSelected() ? 0 : 1);

			}
		});
		// 更改手机号码
		phoneNumberLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("手机按钮");
				// -----
				executeSendPhoneNumber(phoneNumber.getText().toString());

				// showDialog("修改手机号码", phoneNumber, 1);
			}
		});
		// 更改sina
		sinaBlogLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				showDialogBlog("新浪微博", sinaBlogName, sinaBlogStatus);
			}
		});
		// 更改tencent
		tencentBlogLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				showDialogBlog("腾讯微博", tencentBlogName, tencentBlogStatus);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshUI();
	}

	private void refreshUI() {
		infoDTO = SessionManager.getInstance().getUserInfo(this);
		userName.setText(infoDTO.getNickName());
		// true 0 为女
		figureGender.setSelected(infoDTO.getSexTag() == 0 ? true : false);

		if (CheckUtil.isEmpty(infoDTO.getTel())) {
			phoneNumberStatus.setTextColor(getResources().getColor(R.color.text_color_red));
			phoneNumberStatus.setText("绑定手机");
		} else {
			phoneNumberStatus.setTextColor(getResources().getColor(R.color.text_color_gray));
			phoneNumberStatus.setText("更换");
		}
		phoneNumber.setText(infoDTO.getTel());

		// 微博状态判断
		if (infoDTO.isSinaBindTag() && !infoDTO.isSinaWeiboExpired()) {
			// 绑定没有过期
			sinaBlogStatus.setText("取消绑定");
			sinaBlogName.setText(infoDTO.getSinaAccount());
			sinaBlogStatus.setTextColor(getResources().getColor(R.color.text_color_gray));
			isNeedSINAWBbinding = false;

		} else {
			// 绑定无效
			sinaBlogStatus.setText("点击绑定");
			sinaBlogStatus.setTextColor(getResources().getColor(R.color.text_color_red));
			sinaBlogName.setText("新浪微博");
			isNeedSINAWBbinding = true;
		}

		if (infoDTO.isQqBindTag() && !infoDTO.isQQWeiboExpired()) {
			tencentBlogStatus.setText("取消绑定");
			tencentBlogStatus.setTextColor(getResources().getColor(R.color.text_color_gray));
			tencentBlogName.setText(infoDTO.getQqAccount());
			isNeedQQWBbinding = false;
		} else {
			// 绑定无效
			tencentBlogStatus.setText("点击绑定");
			tencentBlogStatus.setTextColor(getResources().getColor(R.color.text_color_red));
			tencentBlogName.setText("腾讯微博");
			isNeedQQWBbinding = true;
		}
	}

	public void showDialogBlog(final String title, final TextView blogName, final TextView blogStatus) {
		switch (blogName.getId()) {
		case R.id.sina_blog_name:
			// sina微博
			if (isNeedSINAWBbinding) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("绑定新浪微博");
				// -----
				// 绑定微博
				Bundle bundle = new Bundle();
				bundle.putBoolean(com.fg114.main.app.Settings.BUNDLE_KEY_IS_LOGIN, false);
				bundle.putInt("page", 1);
				// -----
				// OpenPageDataTracer.getInstance().addEvent("新浪按钮");
				// -----
				CommonObservable.getInstance().addObserver(
						new CommonObserver.WeiboAuthResultObserver(
								new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

									@Override
									public void onComplete(boolean isSuccessful) {
										// -----
										// OpenPageDataTracer.getInstance().endEvent("新浪按钮");
										// -----
										if (isSuccessful) {
											// TODO
											isNeedSINAWBbinding = false;
										} else {
											isNeedSINAWBbinding = true;
											// TODO
											// doTest_sina();

										}
									}
								}));
				SinaSSOAuthActivity.currentWeiboUtil = WeiboUtilFactory
						.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
				ActivityUtil.jump(UserAccessSettingActivity.this, SinaSSOAuthActivity.class, 0, bundle);
				// ActivityUtil.jump(UserAccessSettingActivity.this,
				// MediatorActivity.class, 0, bundle);

			} else {
				// 取消绑定
				DialogUtil.showAlert(UserAccessSettingActivity.this, true, "是否解除与sina微博的绑定", title, "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// -----
								OpenPageDataTracer.getInstance().addEvent("解绑新浪微博");
								// -----
								// 解除绑定
								ServiceRequest request = new ServiceRequest(API.unbindWeibo);
								request.addData("typeTag", 1);// 餐馆ID
								// -----
								// OpenPageDataTracer.getInstance().addEvent("新浪按钮");
								// -----
								CommonTask.request(request, "正在解除与" + title + "绑定",
										new CommonTask.TaskListener<Void>() {

											@Override
											protected void onSuccess(Void dto) {
												// -----
												// OpenPageDataTracer.getInstance().endEvent("新浪按钮");
												// -----
												DialogUtil.showAlert(UserAccessSettingActivity.this, title, "成功解除了与"
														+ title + "绑定", new Runnable() {
													public void run() {
														isNeedSINAWBbinding = true;
														refreshUI();

													}
												});
											};

											protected void onError(int code, String message) {
												// -----
												// OpenPageDataTracer.getInstance().endEvent("新浪按钮");
												// -----
												// 失败
												DialogUtil.showToast(UserAccessSettingActivity.this, message);
												// TODO
												// doTest_Unbind_sina();

											};
										});

								dialog.dismiss();

							}

						}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// -----
								// OpenPageDataTracer.getInstance().endEvent("新浪按钮");
								// -----
								dialog.dismiss();
							}

						});
			}
			break;
		// 腾讯微博
		case R.id.tencent_blog_name:
			if (isNeedQQWBbinding) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("绑定腾讯微博");
				// -----
				
				Bundle bundle = new Bundle();
				bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);

				CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

					@Override
					public void onComplete(boolean isSuccessful) {
						if (isSuccessful) {
							SessionManager.getInstance().setIsUserLogin(ContextUtil.getContext(), true);
							isNeedQQWBbinding = false;
							
						}else{
							isNeedQQWBbinding = true;
						}
						refreshUI();
					}
				}));
				// TX的跳AuthWebActivity
				AuthWebActivity.currentWeiboUtil = WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
				ActivityUtil.jump(UserAccessSettingActivity.this, AuthWebActivity.class, 0, bundle);
				
				
			} else {

				// 取消绑定
				DialogUtil.showAlert(UserAccessSettingActivity.this, true, "是否解除与腾讯微博的绑定", title, "确定", "取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// -----
								OpenPageDataTracer.getInstance().addEvent("解绑腾讯微博");
								// -----
								// 解除绑定
								ServiceRequest request = new ServiceRequest(API.unbindWeibo);
								request.addData("typeTag", 2);// 餐馆ID
								// -----
								// OpenPageDataTracer.getInstance().addEvent("腾讯按钮");
								// -----
								CommonTask.request(request, "正在解除与" + title + "绑定",
										new CommonTask.TaskListener<Void>() {

											@Override
											protected void onSuccess(Void dto) {
												// -----
												// OpenPageDataTracer.getInstance().endEvent("腾讯按钮");
												// -----
												DialogUtil.showAlert(UserAccessSettingActivity.this, title, "成功解除了与"
														+ title + "绑定", new Runnable() {
													public void run() {
														isNeedQQWBbinding = true;

													}
												});
												refreshUI();
											};

											protected void onError(int code, String message) {
												// -----
												// OpenPageDataTracer.getInstance().endEvent("腾讯按钮");
												// -----
												// 失败
												DialogUtil.showToast(UserAccessSettingActivity.this, "噢!与" + title
														+ "解除绑定失败");
												// TODO
												// doTest_Unbind_tencent();

											};
										});

								dialog.dismiss();

							}

						}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// -----
								// OpenPageDataTracer.getInstance().endEvent("腾讯按钮");
								// -----
								dialog.dismiss();
							}

						});
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 昵称
	 * 
	 * @param title
	 * @param type
	 *            0:昵称 1手机号码
	 */

	public void showDialog(final String title, final TextView textView, final int type) {
		DialogUtil.showDialog(UserAccessSettingActivity.this, R.layout.person_name_dialog_layout,
				new DialogUtil.DialogEventListener() {
					@Override
					public void onInit(View contentView, final PopupWindow dialog) {
						TextView person_title = (TextView) contentView.findViewById(R.id.person_title);
						final EditText person_content = (EditText) contentView.findViewById(R.id.person_content);
						Button person_ok = (Button) contentView.findViewById(R.id.person_ok);
						Button person_no = (Button) contentView.findViewById(R.id.person_no);
						person_title.setText(title);
						person_content.setText(textView.getText().toString());
						// 点击保存按钮
						person_ok.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								final String input_message = person_content.getText().toString();
								// 用户输入的信息
								if (TextUtils.isEmpty(input_message)) {
									DialogUtil.showToast(UserAccessSettingActivity.this, "输入的信息不能为空");
								} else if (textView.getText().toString().equals(person_content.getText().toString())) {
									// 输入的信息没有修改
									DialogUtil.showToast(UserAccessSettingActivity.this, "您没有修改信息");
									dialog.dismiss();
								} else {
									// 向服务器提交数据
									switch (type) {
									// 昵称
									case 0:
										if (executeSendNikeNameNumber(input_message)) {
											textView.setText(input_message);
										}
										break;
									// 手机号码
									case 1:
										// if
										// (executeSendPhoneNumber(input_message))
										// {
										// textView.setText(input_message);
										// }
										// executeSendPhoneNumber(input_message);
										break;
									default:
										break;
									}
									dialog.dismiss();
								}
							}
						});
						// 点击取消按钮
						person_no.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								DialogUtil.showToast(UserAccessSettingActivity.this, "您取消了操作");
								dialog.dismiss();
							}
						});
					}
				});
	}

	boolean isNikeNameSuccess = false;

	// 提交修改的昵称
	public boolean executeSendNikeNameNumber(final String message) {
		ServiceRequest request = new ServiceRequest(API.changeUserNickName);
		request.addData("nickName", message);
		CommonTask.request(request, "正在提交...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				infoDTO.setNickName(message);
				refreshUI();
				isNikeNameSuccess = true;
			};

			protected void onError(int code, String message) {
				DialogUtil.showToast(UserAccessSettingActivity.this, message);
				isNikeNameSuccess = false;
				// TODO
				// doTest_NikeName(message);

			};
		});

		return isNikeNameSuccess;
	}

	boolean isPhoneNumberSuccess = false;

	// 提交手机号码
	public void executeSendPhoneNumber(String tel) {
		Bundle bundle = new Bundle();
		bundle.putString(Settings.BUNDLE_KEY_TEL, tel);
		bundle.putLong(Settings.FROM_TAG, 2);
		ActivityUtil.jump(UserAccessSettingActivity.this, ValidatePhoneNOActivity.class, 0, bundle);
	}

	boolean isSexTag = false;

	// 提交性别
	public boolean executeSendSexTag(final int message)

	{

		ServiceRequest request = new ServiceRequest(API.changeUserSex);
		request.addData("sexTag", message);
		// -----
		OpenPageDataTracer.getInstance().addEvent("性别按钮");
		// -----
		CommonTask.request(request, "正在提交...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("性别按钮");
				// -----
				// 1：先生 0：女士
				infoDTO.setSexTag(message);
				refreshUI();
				isSexTag = true;
			};

			protected void onError(int code, String messages) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("性别按钮");
				// -----
				DialogUtil.showToast(UserAccessSettingActivity.this, messages);
				isSexTag = false;
				// TODO
				// doTest_sex(message);

			};
		});
		return isSexTag;
	}

}
