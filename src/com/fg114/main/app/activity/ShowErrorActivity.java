package com.fg114.main.app.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 城市选择
 * @author zhangyifan
 *
 */
public class ShowErrorActivity extends MainFrameActivity {
	//传入参数
	private String errorMsg;
	
	//界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private TextView tvMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//获得传入参数
		Bundle bunde = this.getIntent().getExtras();
		errorMsg = bunde.getString(Settings.BUNDLE_KEY_CONTENT);
		
		//初始化界面
		initComponent();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		
		//设置标题栏
		this.getTvTitle().setText(getString(R.string.text_title_show_error));
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		
		//内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.show_error, null);
		tvMsg = (TextView) contextView.findViewById(R.id.show_error_tvMsg);
		tvMsg.setText(errorMsg);
		ViewUtils.setUnderLine(tvMsg, 0, errorMsg.length());
		tvMsg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.gotoWirelessSettings(ShowErrorActivity.this);
				finish();
			}
		});
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
}
