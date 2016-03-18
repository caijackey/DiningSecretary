package com.fg114.main.qmoney;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fg114.main.app.Fg114Application;
import com.fg114.main.qmoney.tools.RSATool;
import com.fg114.main.service.dto.KqReqData;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.JsonUtils;
import com.qmoney.third.OrderInfo;
import com.qmoney.third.PayRequest;
import com.qmoney.tools.CommonUtils;
import com.qmoney.ui.OnRequestListener;
import com.qmoney.ui.PayService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class KqActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);

	}

	private static KqActivity instance = null;

	public static KqActivity getInstance() {
		if (instance == null) {
			instance = new KqActivity();
		}
		return instance;
	}

	@Override
	protected void onStop() {
		CommonUtils.closeDialog();
		super.onStop();
	}

	@Override
	protected void onPause() {
		CommonUtils.closeDialog();
		super.onPause();
	}

	@Override
	protected void onStart() {
		CommonUtils.closeDialog();
		super.onStart();
	}

	@Override
	protected void onResume() {
		CommonUtils.closeDialog();
		super.onResume();
	}



	




	/************************************************************
	 * 监听快钱受理能力的银行数据
	 * 
	 * @credit 快钱支持的受理能力的信用卡的银行 名称
	 * @debit 快钱支持的受理能力的借记卡的银行名称
	 * @HashMap<String, String> credit 快钱支持的受理能力的信用卡的银行 与对应的银行Id
	 * @HashMap<String, String> debit 快钱支持的受理能力的借记卡的银行 与对应的银行Id
	 * 
	 */
	OnRequestListener requestListener = new OnRequestListener() {
		public void onFinish(String[] credit, String[] debit) {
			// 快钱支持的银行名称
		}

		public void onFinish(HashMap<String, String> credit, HashMap<String, String> debit) {

			// 快钱支持的银行名称与银行Id集合

			if (null != credit) {

				Set<Map.Entry<String, String>> set = credit.entrySet();
				Iterator<Map.Entry<String, String>> iterator = set.iterator();

				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					System.out.println(" 键是 = " + entry.getKey() + " 值是 = " + entry.getValue());
				}

			}

			if (null != debit) {

				// TODO

			}

			if (null == credit && debit == null) {

				CommonUtils.closeDialog(); // 关闭对话框

			}
		}
	};

	/**
	 * 接收回调
	 */
	protected void onNewIntent(Intent intent) {
		String orderId = intent.getStringExtra("orderId");
		String payResult = intent.getStringExtra("payResult");
		Log.v("TAG", orderId+"=orderId");
		if (!TextUtils.isEmpty(orderId) && !TextUtils.isEmpty(payResult)) {
			int payResultCode = Integer.parseInt(payResult);
			String payResultStr = "";

			// 1：支付成功 2:支付失败 0： 交易取消
			switch (payResultCode) {
			case 0:
				payResultStr = "交易取消";
				Log.v("TAG", payResultStr);
				break;

			case 1:
				payResultStr = "支付成功";
				Log.v("TAG", payResultStr);
				break;

			case 2:
				payResultStr = "支付失败";
				Log.v("TAG", payResultStr);
				break;

			default:
				break;
				
		
			}

		}

		super.onNewIntent(intent);
	}
}
