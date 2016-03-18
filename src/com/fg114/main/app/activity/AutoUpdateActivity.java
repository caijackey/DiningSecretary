package com.fg114.main.app.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.service.UpdateService;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;

public class AutoUpdateActivity extends Activity {

	private static final String TAG = "AutoUpdateActivity";
	private static final boolean DEBUG = Settings.DEBUG;

	private ProgressDialog pBar; // 更新进度条
	private Handler handler = new Handler();
	private Thread downloadThread;
	private boolean downloadOffOn = true;

	// 传入参数
	private String url;
	private String appName = "订餐小秘书";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auto_update);

		// 获得传入参数
		Bundle bunde = this.getIntent().getExtras();
		url = bunde.getString(Settings.BUNDLE_KEY_CONTENT);
		if (bunde.containsKey(Settings.BUNDLE_UPDATE_APP_NAME)) {
			appName = bunde.getString(Settings.BUNDLE_UPDATE_APP_NAME);
		}

		// 更新程序
		// doNewVersionUpdate();

		try {
			UpdateService.actionStart(ContextUtil.getContext(), url, appName);
		} catch (Exception e) {
			e.printStackTrace();
			DialogUtil.showToast(this, "更新出错，请重试");
		} finally {
			finish();
		}
	}

	/**
	 * 下载新版本
	 * 
	 * @deprecated
	 */
	private void doNewVersionUpdate() {
		pBar = new ProgressDialog(AutoUpdateActivity.this);
		pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 设置ProgressDialog 的进度条是否不明确
		pBar.setIndeterminate(false);
		pBar.setCancelable(false);
		pBar.setButton("取消下载", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadOffOn = false;
				AutoUpdateActivity.this.finish();
			}
		});
		pBar.show();
		downFile(url);
	}

	/**
	 * 下载模块
	 * 
	 * @deprecated
	 */
	void downFile(final String url) {
		/**
		 * 下载的线程
		 * 
		 * @deprecated
		 */
		downloadThread = new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;

				FileOutputStream fileOutputStream = null;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					int length = (int) entity.getContentLength();
					notifyMaxProgress(length);

					if (DEBUG)
						Log.d(TAG, "length：" + length);
					InputStream is = entity.getContent();
					if (is != null) {

						File file = new File(Environment.getExternalStorageDirectory(), Settings.UPDATE_SAVENAME);
						fileOutputStream = new FileOutputStream(file);

						byte[] buf = new byte[1024];
						int ch = -1;
						int inc = 0;
						int total = 0;
						while (downloadOffOn && (ch = is.read(buf)) != -1) {
							// Log.d("dfs", ch+ "");
							fileOutputStream.write(buf, 0, ch);
							inc += ch;
							total += ch;
							if (inc > 1024 * 50) {
								pBar.incrementProgressBy(inc);
								inc = 0;
							}
						}
						fileOutputStream.flush();
						if (downloadOffOn) {
							if (total != length) {
								// Log.d("下载不成功!准备重试","total="+total+",length="+length);
								notifyError("升级时发生网络异常，升级包下载失败。请先检查您的网络环境，然后在用户中心重新尝试升级。");
							} else {
								// 下载结束，通知安装
								notifyDownloadCompleted();
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
					notifyError("升级时发生异常，请检查您的网络状况和SD卡存储空间是否足够。");
				} catch (Exception e) {
					e.printStackTrace();
					notifyError("更新异常");
				} finally {
					try {
						if (fileOutputStream != null) {
							fileOutputStream.close();
						}
					} catch (Exception e) {
						notifyError("更新异常");
					}
				}
			}
		};
		downloadThread.start();
	}

	/**
	 * 通知进度最大值,并显示进度
	 * 
	 * @deprecated
	 */
	private void notifyMaxProgress(final int size) {
		handler.post(new Runnable() {
			public void run() {
				pBar.setMax(size);
			}
		});
	}

	/**
	 * 下载结束，通知UI线程
	 * 
	 * @deprecated
	 */
	private void notifyDownloadCompleted() {
		handler.post(new Runnable() {
			public void run() {
				pBar.cancel();
				update();
				finish();
			}
		});
	}

	/**
	 * 报错
	 * 
	 * @deprecated
	 */
	private void notifyError(final String msg) {
		handler.post(new Runnable() {
			public void run() {
				try {
					DialogUtil.showAlert(AutoUpdateActivity.this, false, msg, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							AutoUpdateActivity.this.finish();
							dialog.cancel();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 运行新apk，安装
	 * 
	 * @deprecated
	 */
	private void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), Settings.UPDATE_SAVENAME)), "application/vnd.android.package-archive");
		startActivity(intent);
	}
}
