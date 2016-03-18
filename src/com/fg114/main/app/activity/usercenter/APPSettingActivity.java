package com.fg114.main.app.activity.usercenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.service.UpdateService;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

public class APPSettingActivity extends MainFrameActivity {

	private static final String TAG = "APPSettingActivity";

	// 界面组件
	private View contextView;
	private LayoutInflater mInflater;
	private LinearLayout attentionToXMS;// 新浪微博上关注小秘书
	private LinearLayout shareToFriend;// 分享给好友，让更多的人认识我吧
	private LinearLayout updateVersion;// 版本更新
	private LinearLayout userFeedback;// 用户反馈
	private LinearLayout aboutUs;// 关于我们
	private View app_market_line; // 横线
	private LinearLayout appMarket;// 你可能喜欢的应用
	private View members_help;// 会员帮助
	private View mibi_detail;// 秘币细则
	private TextView version_text;
	private Button restartDownLoadAPK;
	private ToggleButton push_bnt_toggle;

	private static final String DOWNLOAD_DIR = "/" + Settings.IMAGE_CACHE_DIRECTORY + "/download/"; // 下载目录
	// private static final String DOWNLOAD_DIR = "/download/"; // 下载目录

	public static volatile Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("App设置", "");
		// ----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
			APPSettingActivity.this.finish();
		}
		// 初始化界面
		initComponent();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("App设置", "");
		// ----------------------------
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("APP设置");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.app_settings, null);
		attentionToXMS = (LinearLayout) contextView.findViewById(R.id.attention_to_xms);
		shareToFriend = (LinearLayout) contextView.findViewById(R.id.share_to_friend);
		updateVersion = (LinearLayout) contextView.findViewById(R.id.update_version);
		version_text = (TextView) contextView.findViewById(R.id.version_text);
		userFeedback = (LinearLayout) contextView.findViewById(R.id.user_feedback);
		aboutUs = (LinearLayout) contextView.findViewById(R.id.about_us);
		restartDownLoadAPK = (Button) contextView.findViewById(R.id.restartDownLoadAPK);
		appMarket = (LinearLayout) contextView.findViewById(R.id.app_market);
		app_market_line = (View) contextView.findViewById(R.id.app_market_line);
		push_bnt_toggle= (ToggleButton) contextView.findViewById(R.id.push_bnt_toggle);
		members_help = contextView.findViewById(R.id.members_help);
		mibi_detail = contextView.findViewById(R.id.mibi_detail);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// 设置是否隐藏“喜欢的应用”
		boolean isNeedHideFavorite = false;
		try {
			isNeedHideFavorite = SessionManager.getInstance().getSoftwareCommonData().isNeedHideFavoriteApp();

		} catch (Exception e) {
		}
		appMarket.setVisibility(isNeedHideFavorite ? View.GONE : View.VISIBLE);
		app_market_line.setVisibility(isNeedHideFavorite ? View.GONE : View.VISIBLE);
		// 新浪微博上关注小秘书
		attentionToXMS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("新浪关注");
				// -----

				Bundle data = new Bundle();
				data.putString(Settings.BUNDLE_KEY_WEB_URL, "http://weibo.cn/cn95171");
				ActivityUtil.jump(APPSettingActivity.this, SimpleWebViewActivity.class, 0, data);
			}
		});
		// // 分享给好友，让更多的人认识我吧
		shareToFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("分享");
				// -----

				showShareDialog(2);
			}
		});

		restartDownLoadAPK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("版本更新");
				// -----
				boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(APPSettingActivity.this.getApplicationContext());
				if (!isNetAvailable) {
					DialogUtil.showToast(getApplicationContext(), "未连接网络");
					return;
				}
				// version_text.setText("有新版本: " +
				// Settings.gVersionChkDTO.newVersion);
				DialogUtil.showAlert(APPSettingActivity.this, true, "", "是否重新下载?", "确定", "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (removeApkFile(apkPath)) {

							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_KEY_CONTENT, Settings.gVersionChkDTO.downloadUrl);
							ActivityUtil.jump(APPSettingActivity.this, AutoUpdateActivity.class, 0, bundle);
						} else {
							DialogUtil.showToast(APPSettingActivity.this, "重新下载失败...");
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
		});

		// 版本更新
		updateVersion.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (Settings.gVersionChkDTO == null || !Settings.gVersionChkDTO.haveNewVersionTag) {
					DialogUtil.showToast(getApplicationContext(), "没有发现新版本");
					return;
				}
				boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(APPSettingActivity.this.getApplicationContext());
				if (!isNetAvailable) {
					DialogUtil.showToast(getApplicationContext(), "未连接网络");
					return;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent("版本更新");
				// -----

				if (isApkLocalExist && apkName.split("_")[0].equals(getApkLocalSize(apkPath))) {// 判断本地APK包是否完整
					Uri uri = Uri.fromFile(new File(apkPath));
					Intent installIntent = new Intent(Intent.ACTION_VIEW);
					installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
					installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(installIntent);
				} else {
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_CONTENT, Settings.gVersionChkDTO.downloadUrl);
					ActivityUtil.jump(APPSettingActivity.this, AutoUpdateActivity.class, 0, bundle);
				}
			}
		});
		// // 用户反馈
		userFeedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("意见反馈");
				// -----

				Bundle data = new Bundle();
				ActivityUtil.jump(APPSettingActivity.this, OpinionErronReportActivity.class, 0, data);
			}
		});
		// // 关于我们
		aboutUs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("关于我们");
				// -----

				ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + "about", "关于我们");
			}
		});
		// // 你可能喜欢的应用
		appMarket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("喜欢的应用");
				// -----

				Bundle data = new Bundle();
				data.putString(Settings.BUNDLE_KEY_WEB_URL, "http://www.xiaomishu.com/o/app/other/Android/" + "?" + A57HttpApiV3.getInstance().getBaseParamsString());
				data.putBoolean("shouldOverrideUrlLoading", false);
				ActivityUtil.jump(APPSettingActivity.this, SimpleWebViewActivity.class, 0, data);
			}
		});

		members_help.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("会员帮助");
				// -----
				if (checkLogin()) {
					jumpToWeb("会员帮助");
				}
			}
		});

		mibi_detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("秘币细则");
				// -----
				if (checkLogin()) {
					jumpToWeb("秘币细则");
				}
			}
		});
		push_bnt_toggle.setChecked(Settings.isOpenPush);
		//推送设置
		push_bnt_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 打开状态
					Settings.isOpenPush=true;
				} else {
					// 关闭状态
					Settings.isOpenPush=false;
				}
			}
		});

		handler = new Handler() {

			public void handleMessage(android.os.Message msg) {

				if (msg.what == 200) {

					if (msg.arg1 == 0) {// 开始
						restartDownLoadAPK.setVisibility(View.GONE);
						version_text.setText("正在下载(版本 " + Settings.gVersionChkDTO.newVersion + ")");
					} else if (msg.arg1 == 1) {
						restartDownLoadAPK.setVisibility(View.VISIBLE);
						version_text.setText("已下载" + computeDownloadPercent() + "% (版本 " + Settings.gVersionChkDTO.newVersion + ")\n(点击继续下载)");
					} else if (msg.arg1 == 2) {// 完成
						restartDownLoadAPK.setVisibility(View.VISIBLE);
						version_text.setText("下载完毕(版本 " + Settings.gVersionChkDTO.newVersion + ")\n(点击安装)");
					} else if (msg.arg1 == 4) {
						int percent = msg.arg2;
						restartDownLoadAPK.setVisibility(View.GONE);
						version_text.setText("已下载" + percent + "% (版本 " + Settings.gVersionChkDTO.newVersion + ")");
					} else if (msg.arg1 == 5) {// 结束
						DialogUtil.showAlert(APPSettingActivity.this, "", "下载中，请稍候！");
					}
				}

			};
		};
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (Settings.gVersionChkDTO != null && Settings.gVersionChkDTO.haveNewVersionTag) {// 需要更新
			isApkLocalExist = apkLocalExist(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR, String.valueOf(Settings.gVersionChkDTO.newVersion.hashCode()));
			if (!isApkLocalExist) {// 在本地没有发现下载最新版本的更新包
				restartDownLoadAPK.setVisibility(View.GONE);
				version_text.setText("有新版本: " + Settings.gVersionChkDTO.newVersion);
			} else {
				if (apkName.split("_")[0].equals(getApkLocalSize(apkPath))) {// 判断本地APK包是否完整
					restartDownLoadAPK.setVisibility(View.VISIBLE);
					version_text.setText("下载完毕(版本 " + Settings.gVersionChkDTO.newVersion + ")\n(点击安装)");
				} else {

					restartDownLoadAPK.setVisibility(View.VISIBLE);
					version_text.setText("已下载" + computeDownloadPercent() + "% (版本 " + Settings.gVersionChkDTO.newVersion + ")\n(点击继续下载)");
				}
			}
		} else {
			restartDownLoadAPK.setVisibility(View.GONE);
			version_text.setText("");
		}
	}

	private static boolean isApkLocalExist = false;

	private static String apkPath;
	private static String apkName;

	// 扫描path目录下已经下载过的APK的信息
	private boolean apkLocalExist(String path, String versionHash) {
		String[] apkLocalMes = new String[2];
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		File[] files = file.listFiles();
		String regEx = "^(\\d+)(_)(-?)(\\d+)(_newVersion.apk)$";
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				Pattern pat = Pattern.compile(regEx);
				Matcher mat = pat.matcher(files[i].getName());
				if (mat.find()) {
					apkName = mat.group();
					apkPath = files[i].toString();
					apkLocalMes = apkName.split("_");
					if (!apkLocalMes[1].equals(versionHash)) {
						// 找到非法的
						removeApkFile(files[i].toString());
					} else {
						apkLocalMes[0] = apkName;
						apkLocalMes[1] = apkPath;
						return true;
					}
				} else {
					removeApkFile(files[i].toString());
				}
			}
		}
		return false;
	}

	// 获得APK包的大小
	private static String getApkLocalSize(String path) {
		File file = new File(path);
		return String.valueOf(file.length());
	}

	private static int computeDownloadPercent() {

		if (isApkLocalExist) {
			float percent = Float.parseFloat(getApkLocalSize(apkPath)) / Float.parseFloat(apkName.split("_")[0]);
			return (int) (percent * 100);
		}
		return 0;

	}

	private boolean removeApkFile(String path) {
		File file = new File(path);
		return file.delete();
	}

	/**
	 * 分享餐厅
	 * 
	 * @param context
	 */
	protected void shareOther() {
		try {
			ActivityUtil.callShare((Activity) this, "订餐小秘书说", getString(R.string.text_info_share_weibo_detail) , "分享");
		} catch (Exception e) {
			DialogUtil.showToast(this, "对不起，暂时无法分享");
		}
	}

	// 拼接短信信息-----------------
	protected String makeSMSinfo() {
		return getString(R.string.text_info_share_weibo_detail);

	}

	protected String makeWeiboInfo() {
		return getString(R.string.text_info_share_weibo_detail);

	}

	// 拼接邮件信息
	protected String makeEmailInfo() {
		return getString(R.string.text_info_share_weibo_detail) ;

	}
	
	protected String makeWeiXinInfo(){
		return getString(R.string.text_info_share_weibo_detail) ;	
	}

	private class ApkInfoDto {
		public String apkPath;
		public long apkCreateTime;
	}

	private boolean checkLogin() {
		if (SessionManager.getInstance().isUserLogin(this)) {
			return true;
		} else {
			DialogUtil.showToast(APPSettingActivity.this, "您还未登录，请先登录!");
			return false;
		}
	}
	
	private void jumpToWeb(String where) {
		String path = "member/browse";
		String title = "浏览历史";
		NameValuePair pair = null;
		
		if ("会员帮助".equals(where)) {
			path = "memberhelp";
			title = "会员帮助";

		} else if ("秘币细则".equals(where)) {
			path = "coindetail";
			title = "秘币细则";

		}
		if (pair != null) {
			ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title, pair);
		} else {
			ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title);
		}
	}
}
