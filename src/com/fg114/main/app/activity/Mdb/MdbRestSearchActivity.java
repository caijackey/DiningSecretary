package com.fg114.main.app.activity.Mdb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MdbOrderListActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.MdbRestInfoData;
import com.fg114.main.service.dto.MdbRestListDTO;
import com.fg114.main.service.dto.MdbRestListData;
import com.fg114.main.service.dto.MdbSearchRestData;
import com.fg114.main.service.dto.MdbSearchRestListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.GetSuggestKeywordListTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 搜索提示
 * 
 * @author zhangyifan
 */
public class MdbRestSearchActivity extends MainFrameActivity {

	private String keyword = "";

	// 界面组件
	private EditText auto_complete_etSearchbox;
	// private TextView btnSearch;
	private ListView auto_complete_listview;

	ListViewAdapter<MdbSearchRestData> adapter;
//	private boolean hasLogined;

	// 任务

	private AtomicLong mSearchTimestamp = new AtomicLong();

	private Handler searchHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// Log.e("searchHandler", "handleMessage:mCanSearch=" +
			// mSearchTimestamp.get());

			long timstamp = (Long) msg.obj;
			// Log.e("searchHandler", "handleMessage:timstamp=" + timstamp);

			if (mSearchTimestamp.longValue() == timstamp) {

				// 重新还原列表数据
				executeGetSuggestKeywordListTask(keyword);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();

		if (bundle != null) {
			if (bundle.containsKey(Settings.BUNDLE_KEY_KEYWORD)) {
				keyword = bundle.getString(Settings.BUNDLE_KEY_KEYWORD);
				// Log.e("AutoCompleteActivity",
				// "AutoCompleteActivity"+keyword);
			}
		}

		// 获得搜索历史记录

		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		auto_complete_etSearchbox.requestFocus();
		InputMethodManager imm = (InputMethodManager) MdbRestSearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

		executeGetnoKeywordListTask();

	}

	// private LinearLayout layout;
	// private int width;
	// private int edWidth;
	@Override
	protected void onResume() {
		super.onResume();
		// 判断是否已登录并调整界面显示
//		hasLogined = SessionManager.getInstance().isUserLogin(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		finish();
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// if (requestCode != resultCode) {
	// this.setResult(resultCode, data);
	// this.finish();
	// }
	// }

	/**
	 * 初始化
	 */
	private void initComponent() {
		// channelSpinner = (Spinner)
		// this.findViewById(R.id.auto_complete_channelSpinner);

		this.getTvTitle().setText("我要付款");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("帮助");

		// 初始化 
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = mInflater.inflate(R.layout.mdb_rest_search_activity, null);
		auto_complete_etSearchbox = (EditText) contextView.findViewById(R.id.auto_complete_etSearchbox);
		auto_complete_listview = (ListView) contextView.findViewById(R.id.auto_complete_listview);

		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + "mdb/help", "帮助");
			}
		});
		ViewUtils.setClearable(auto_complete_etSearchbox);

		// 搜索框事件
		auto_complete_etSearchbox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				// 显示关键字列表
				keyword = auto_complete_etSearchbox.getText().toString().trim();
				if (TextUtils.isEmpty(keyword)) {
					// 清空关键字时立即处理
					executeGetSuggestKeywordListTask(keyword);
				} else {
					// // 关键字改变后延时一定时间再开始搜索，用户连续较快输入时不重复多次搜索
					mSearchTimestamp.set(System.currentTimeMillis());
					searchHandler.sendMessageDelayed(searchHandler.obtainMessage(0, mSearchTimestamp.get()), 10);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		// 设置输入过滤
		auto_complete_etSearchbox.setFilters(new InputFilter[] { new InputFilter() {
			public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
				if (CheckUtil.isInvalidChar(src.toString())) {
					return "";
				}
				return null;
			}
		} });

		
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

	}

	/**
	 * 获得关键字提示
	 */

	private void executeGetSuggestKeywordListTask(String key) {
		if (adapter == null) {
			return;
		}
		ServiceRequest request = new ServiceRequest(API.getMdbSearchRestList);
		request.addData("keywords", key);

		CommonTask.requestMutely(request, new CommonTask.TaskListener<MdbSearchRestListDTO>() {

			@Override
			protected void onSuccess(MdbSearchRestListDTO dto) {
				if (dto.list != null) {
					adapter.setList(dto.list);
				}
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	/**
	 * 获得关键字提示
	 */

	private void executeGetnoKeywordListTask() {

		adapter = new ListViewAdapter<MdbSearchRestData>(R.layout.mdb_rest_search_list_item, new ListViewAdapter.OnAdapterListener<MdbSearchRestData>() {

			@Override
			public void onRenderItem(ListViewAdapter<MdbSearchRestData> adapter, ViewHolder holder, final MdbSearchRestData data) {
				// TODO Auto-generated method stub
				TextView mdb_rest_search_rest_name = holder.$tv(R.id.mdb_rest_search_rest_name);
				TextView mdb_rest_search_rest_district = holder.$tv(R.id.mdb_rest_search_rest_district);
				View mdb_rest_search_list_item_bt = holder.$(R.id.mdb_rest_search_list_item_bt);

				mdb_rest_search_rest_district.setText(data.distance);

				// 设置关键字
				ViewUtils.setHightlightKeywords(mdb_rest_search_rest_name, data.restName);

				mdb_rest_search_list_item_bt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(view, 1000);
//						if (hasLogined) {
//							Bundle bundle = new Bundle();
//							bundle.putString(Settings.UUID, data.restId);
//							bundle.putInt(Settings.BUNDLE_TPYE_TAG,1);
////							ActivityUtil.jump(MdbRestSearchActivity.this, MdbConsumerPaymentActivity.class, 0,bundle);
//						} else {
//							DialogUtil.showToast(MdbRestSearchActivity.this, "您未登录,请先登录");
//							ActivityUtil.jump(MdbRestSearchActivity.this, UserLoginActivity.class, 0);
//						}
					}
				});

			};

			@Override
			public void onLoadPage(final ListViewAdapter<MdbSearchRestData> adapter, int startIndex, int pageSize) {

				ServiceRequest request = new ServiceRequest(API.getMdbNearRestList);

				CommonTask.requestMutely(request, new CommonTask.TaskListener<MdbSearchRestListDTO>() {

					@Override
					protected void onSuccess(MdbSearchRestListDTO dto) {

						ListViewAdapter.AdapterDto<MdbSearchRestData> adapterDto = new ListViewAdapter.AdapterDto<MdbSearchRestData>();
						adapterDto.setList(dto.list);
						adapter.setExistPage(false);
						adapter.onTaskSucceed(adapterDto);

					}

					@Override
					protected void onError(int code, String message) {
						super.onError(code, message);
						finish();
					}
				});

			}

		});
		adapter.setExistPage(true); // 此句代码必须在"adapter.setListView(listview)"之前
		adapter.setmCtx(MdbRestSearchActivity.this); // 若需要用到的Context是Activity，则需要手动设置mCtx，否则默认是Application，注:此句代码也必须在"adapter.setListView(listview)"之前
		adapter.setListView(auto_complete_listview); //
	}

	@Override
	public void finish() {
		super.finish();
	}
}
