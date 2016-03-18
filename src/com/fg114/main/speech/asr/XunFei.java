package com.fg114.main.speech.asr;

import java.util.ArrayList;

import com.fg114.main.app.Settings;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * 表示讯飞语音识别引擎
 * 
 * @author xu jianjun, 2011-12-12
 * 
 */

class XunFei extends RecognitionEngine {

	Context context;
	// 小秘书在讯飞注册的appid

	// String appidString =
	// "appid=4e96c353,server_url=http://demo.voicecloud.cn/index.htm"; //
	// String engine = "secretary"; //
	// ----------------------------------------------------------------
	// String appidString = "appid=4fc2d4c8"; // 测试语音优化后的测试appid
	// String engine = "secretary"; // 测试时的engine
	// ---------------------------------------------------------------
	// String engine="sms"; //engine
	// String appidString = "appid=4e96c353"; //正式appid
	// ---------------------------------------------------------------
//	 String engine="secretary"; //engine　优化后的正式版
//	 String appidString = "appid=4fc2d4c8,server_url=http://demo.voicecloud.cn/index.htm"; //　优化后的正式版
	
	//每次闪屏页里会将后台配置好的讯飞设置参数写到settings里（如果有），然后在这里生效
	String engine = Settings.XF_ENGINE_NAME; // engine
	String appidString = Settings.XF_Params; // 正式appid
	// ---------------------------------------------------------------
	// 识别结果
	ArrayList<RecognitionResult> results = new ArrayList<RecognitionResult>();

	// 
	XunFei() {
	}

	XunFei(Context context) {
		this.context = context;
	}

	@Override
	public void start(Bundle config, OnFinishListener finishListner) {

		this.setOnFinishListener(finishListner);
		this.results.clear();
		// 启动识别对话框
		final RecognizerDialog isrDialog = new RecognizerDialog(this.context, appidString);
		isrDialog.setEngine(engine, null, null); // 自由识别
//		final RecognizerDialog isrDialog = new RecognizerDialog(this.context, "server_url=http://demo.voicecloud.cn/index.htm,appid=4fc2d4c8");
//		isrDialog.setEngine("secretary", null, null); // 自由识别
		isrDialog.setListener(recoListener);
		isrDialog.show();
	}

	public XunFei getInstance(Context context) {

		return new XunFei(context);
	}

	// ///////识别事件处理
	private RecognizerDialogListener recoListener = new RecognizerDialogListener() {
		@Override
		public void onResults(ArrayList<RecognizerResult> results, boolean isLast) {

			// 追加结果
			for (RecognizerResult r : results) {

				// 去除识别结果中的标点符号：中文逗号,句号,问号
				r.text = r.text.replaceAll("[，。？]", "");
				if (r.text.length() == 0) {
					continue; // 空字符串，不加入结果集
				}

				XunFei.this.results.add(new RecognitionResult(r.text, r.confidence / (double) 100));
			}

		}

		@Override
		public void onEnd(SpeechError error) {
			try {

				// 识别失败
				if (error != null) {
					Log.d("识别事件onEnd()", "识别失败" + error.getMessage());
					error.printStackTrace();
					return;
				}
				// 识别成功
				// 没有事件处理器，或者没有识别结果
				if (XunFei.this.finishListener == null || results.size() <= 0) {
					return;
				}

				// 不需手工选择结果
				if (!XunFei.this.isManualSelectionOn || results.size() == 1) {
					// 直接返回结果，默认选中第一项
					XunFei.this.finishListener.onFinish(results, 0);
					return;

				}
				// 需手工选择结果，显示选择对话框
				XunFei.this.showSelectionDialog(XunFei.this.context, "您说的是？", results);
			} catch (Exception ex) {

				Log.e("XunFei.onEnd()", ex.getMessage(), ex);
			}

		}
	};

}
