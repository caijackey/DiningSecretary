package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.CityAdapter;
import com.fg114.main.app.adapter.CitySearchAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CityListDTO;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.task.GetSuggestKeywordListTask;
import com.fg114.main.speech.asr.OnFinishListener;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.speech.asr.RecognitionResult;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.RightDrawableOnTouchListener;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 搜索提示
 * 
 * @author zhangyifan
 */
public class CitySearchActivity extends MainFrameActivity {

	private static final boolean DEBUG = Settings.DEBUG;

	// 参数
	private int fromPage;

	// 本地缓存数据
	private List<CommonTypeDTO> resHistoryList;

	// 界面组件
	// private Spinner channelSpinner;
	private EditText etAutoComplete;
	private ListView lvAutoComplete;
	private CitySearchAdapter adapter;
	private Button voiceSearchButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		initComponent();
		etAutoComplete.requestFocus();
		InputMethodManager imm = (InputMethodManager) CitySearchActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Settings.CURRENT_PAGE = getClass().getSimpleName();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		this.getTitleLayout().setVisibility(View.GONE);
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = inflater.inflate(R.layout.city_search, null);
		
		etAutoComplete = (EditText) contextView
				.findViewById(R.id.city_search_etSearchbox);
		lvAutoComplete = (ListView) contextView
				.findViewById(R.id.city_search_listview);

		voiceSearchButton = (Button) contextView
				.findViewById(R.id.city_search_btVoice);

		ViewUtils.setClearable(etAutoComplete);
		// 搜索框事件
		etAutoComplete.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
//				if (etAutoComplete.getText().toString() == null || etAutoComplete.getText().toString().equals("")) {
//					etAutoComplete.setCompoundDrawables(null, null, null, null);
//				}
//				else {
//					etAutoComplete.setCompoundDrawablesWithIntrinsicBounds(null, null, CitySearchActivity.this.getResources().getDrawable(R.drawable.super57_history_remove_bt01), null);
//				}
				
				// 显示关键字列表
				String key = etAutoComplete.getText().toString().trim();
				executeGetCityKeywordListTask(key);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

//		ImageView image = (ImageView) contextView.findViewById(R.id.city_search_mic);
//		image.setVisibility(View.GONE);

		adapter = new CitySearchAdapter(this);
		adapter.setList(resHistoryList);
		lvAutoComplete.setAdapter(adapter);
		lvAutoComplete.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int index = arg2;
				List<CommonTypeDTO> list = ((CitySearchAdapter) arg0
						.getAdapter()).getList();
				if (list != null) {
					CommonTypeDTO city = list.get(index);
					if (city != null
							&& !String.valueOf(Settings.CONTRL_ITEM_ID).equals(
									city.getUuid())) {
						CityInfo cityInfo = new CityInfo();
						cityInfo.setId(city.getUuid());
						cityInfo.setName(city.getName());
						cityInfo.setPhone(city.getPhone());

						// 存入缓存
						SessionManager.getInstance().setCityInfo(
								CitySearchActivity.this, cityInfo);
						Fg114Application.isNeedUpdate=true; //为了广告在首页能及时更新，每城市广告数据是不一样的
						// 本页面关闭
						CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
						ActivityUtil.jump(CitySearchActivity.this, IndexActivity.class, 0, new Bundle(),true);
						finish();
					}
				}
			}
		});

		// 设置语音识别按钮事件-----added by xujianjun, 2011-12-13
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {

			eng.bindButtonAndEditText(voiceSearchButton, etAutoComplete, 0,	null);
			// eng.bindButtonAndEditText(searchButton, (EditText)view);
		}
		// ----------------------------------------------
		// 隐藏语言按钮
		voiceSearchButton.setVisibility(View.VISIBLE);
		
//		etAutoComplete.setOnTouchListener(new RightDrawableOnTouchListener() {
//	        @Override
//	        public boolean onDrawableTouch(final MotionEvent event) {
//	        	etAutoComplete.setText("");
//	        	return true;
//	        }
//	    });
		
		setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 获得关键字提示
	 */
	private void executeGetCityKeywordListTask(String key) {

//		if (CheckUtil.isEmpty(key)) {
//			return;
//		}
		new GetCityTask(key).execute();
	}

	private class GetCityTask extends AsyncTask<String, Integer, CityListDTO> {
		private String mKey;

		public GetCityTask(String keys) {
			mKey = keys;
		}

		@Override
		protected CityListDTO doInBackground(String... params) {
			return SessionManager.getInstance().searchCityByKeyword(
					CitySearchActivity.this, mKey);
		}

		@Override
		protected void onPostExecute(CityListDTO result) {
			resHistoryList = new ArrayList<CommonTypeDTO>();

			for (CityData city : result.getList()) {
				CommonTypeDTO common = new CommonTypeDTO();
				common.setName(city.getCityName());
				common.setPhone(city.getPhone());
				common.setUuid(city.getCityId());
				resHistoryList.add(common);
			}

			adapter.setList(resHistoryList);
		}
	}
}
