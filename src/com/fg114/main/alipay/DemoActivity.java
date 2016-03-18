/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 */

package com.fg114.main.alipay;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fg114.main.alipay.AliPayUtils;
import com.fg114.main.util.UnionpayUtils;
import com.fg114.main.util.ViewUtils;

import static com.fg114.main.alipay.AliPayUtils.*;

public class DemoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout contentView=new LinearLayout(this);
		Button button=new Button(this);
		button.setText("启动支付");
		contentView.addView(button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				//UnionpayUtils.doPay(DemoActivity.this, UnionpayUtils.testXml);
				AliPayUtils.doPay(DemoActivity.this,getOrderInfo(),
					new AliPayUtils.AliPayListener() {
					
					@Override
					public void onPayFinish(final boolean isSuccessful, final String message) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if(isSuccessful){
									Toast.makeText(DemoActivity.this, message, Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(DemoActivity.this, message, Toast.LENGTH_SHORT).show();
								}
							}
						});
						
					}
				});
			}
		});
		setContentView(contentView);

	}


	String getOrderInfo() {
		String strOrderInfo = "partner=" + "\"小秘书霸王菜\"";
		strOrderInfo += "&";
		strOrderInfo += "seller=" + "\"大众小菜提供\"";
		strOrderInfo += "&";
		strOrderInfo += "out_trade_no=" + "\"ORD000001234\"";
		strOrderInfo += "&";
		strOrderInfo += "subject=" + "\"标题\"";
		strOrderInfo += "&";
		strOrderInfo += "body=" + "\"内容是啥\"";
		strOrderInfo += "&";
		strOrderInfo += "total_fee=" + "\""
				+ 0.01 + "\"";
		strOrderInfo += "&";
		strOrderInfo += "notify_url=" + "\""
				+ "http://notify.java.jpxx.org/index.jsp" + "\"";
		
		return strOrderInfo;
	}
}