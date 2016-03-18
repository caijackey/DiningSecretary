package com.fg114.main.app.activity;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
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
public class AutoCompleteActivity extends Activity {

	private String keyword = "";

	// 本地缓存数据
	private List<CommonTypeDTO> resHistoryList;
	private List<CommonTypeDTO> foodHistoryList;

	// 界面组件
	// private Spinner channelSpinner;
	private EditText etAutoComplete;
	private TextView btnSearch;
	private ListView lvAutoComplete;
	private AutoCompleteAdapter adapter;

	private Button voiceSearchButton;
	private Button searchAgainButton;
	private boolean isTaskSafe = true;

	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;
	// 标识语音搜索的来源
	// voiceInputTag---点击了哪个语音按钮 0：没有点击 1：首页左上 2：首页左下 3：功能菜单中 4：搜索建议页 5:订餐厅页
	// 6：热门商圈页 7：选择餐厅8：意见反馈
	public static int voiceInputTag = 0;

	// 任务
	private GetSuggestKeywordListTask getSuggestKeywordListTask;

	private AtomicLong mSearchTimestamp = new AtomicLong();

	private Handler searchHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// Log.e("searchHandler", "handleMessage:mCanSearch=" +
			// mSearchTimestamp.get());

			long timstamp = (Long) msg.obj;
			// Log.e("searchHandler", "handleMessage:timstamp=" + timstamp);

			if (mSearchTimestamp.longValue() == timstamp) {
				// 时间戳与当前时间戳相同时则执行搜索
				adapter.isReset = true;
				// 重新还原列表数据
				executeGetSuggestKeywordListTask(keyword);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auto_complete);

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
		resHistoryList = SessionManager.getInstance().getListManager().getSearchHistoryListInfo(this).getResList();
		foodHistoryList = SessionManager.getInstance().getListManager().getSearchHistoryListInfo(this).getFoodList();

		initComponent();

		etAutoComplete.requestFocus();
		InputMethodManager imm = (InputMethodManager) AutoCompleteActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

	}

	// private LinearLayout layout;
	// private int width;
	// private int edWidth;
	@Override
	protected void onResume() {
		super.onResume();

		Settings.CURRENT_PAGE = getClass().getSimpleName();
		// new Handler().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// width = layout.getWidth();
		// edWidth = editText.getWidth();
		// Log.e(TAG, "width-->" + width);
		// for (int i = edWidth; i <= width; i++) {
		//
		// editText.setLayoutParams(new LinearLayout.LayoutParams(i,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// }
		// }
		// }, 100);
		// if (fromPage != Settings.RESTAURANT_SEARCH_ACTIVITY) {
		// // 第一次进入时的朦皮
		// DialogUtil.showVeilPictureOnce(this,
		// R.drawable.mask_search_auto_complete,
		// "ShowOnceVeil_AutoCompleteActivity");
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.activity_enter, R.anim.activity_enter);
//		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != resultCode) {
			this.setResult(resultCode, data);
			this.finish();
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// channelSpinner = (Spinner)
		// this.findViewById(R.id.auto_complete_channelSpinner);
		etAutoComplete = (EditText) this.findViewById(R.id.auto_complete_etSearchbox);
		btnSearch = (TextView) this.findViewById(R.id.auto_complete_btnSearch);
		lvAutoComplete = (ListView) this.findViewById(R.id.auto_complete_listview);

		voiceSearchButton = (Button) this.findViewById(R.id.auto_complete_btVoice);
		searchAgainButton = (Button) this.findViewById(R.id.search_again_voice_button);

		// 如果是带关键字过来的，显示重新搜索按钮
		if (!CheckUtil.isEmpty(keyword)) {
			// etAutoComplete.setText(keyword);
			setSearchAgainButtonVisible(true);
		} else {
			setSearchAgainButtonVisible(false);
		}

		ViewUtils.setClearable(etAutoComplete);

		// 搜索框事件
		etAutoComplete.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				// 显示关键字列表
				keyword = etAutoComplete.getText().toString().trim();
				startIndex = 1;
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
		etAutoComplete.setFilters(new InputFilter[] { new InputFilter() {
			public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
				if (CheckUtil.isInvalidChar(src.toString())) {
					return "";
				}
				return null;
			}
		} });
		// 当点击输入框时，恢复原始模式：没有“重新搜索”按钮
		etAutoComplete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				setSearchAgainButtonVisible(false);
			}
		});

		// 分页加载
		lvAutoComplete.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 线程安全且不是最后一页的场合，获得站内信息列表
						executeGetSuggestKeywordListTask(keyword);
					}
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					// 当到达列表尾部时
					isRefreshFoot = true;
				} else {
					isRefreshFoot = false;

				}

			}
		});

		// 按回车返回
		etAutoComplete.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					// 来源页为选择餐厅时
					if (MainFrameActivity.getLastActivityClass() == RestaurantSearchActivity.class) {
						Intent intent = new Intent();
						intent.putExtra(Settings.REQUEST_BUNDLE_KEYWORD, etAutoComplete.getText().toString().trim());
						setResult(Settings.REQUEST_CODE_GET_KEYWORD, intent);
						finish();
						return true;
					}

					return searchByKey();
				}
				return false;
			}
		});
		// 取消按钮事件
		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ViewUtils.preventViewMultipleClick(arg0, 1000);
				// 来源页为选择餐厅时
				// if (MainFrameActivity.getLastActivityClass() ==
				// RestaurantSearchActivity.class) {
				Intent intent = new Intent();
				intent.putExtra(Settings.REQUEST_BUNDLE_KEYWORD, etAutoComplete.getText().toString().trim());
				setResult(Settings.REQUEST_CODE_GET_KEYWORD, intent);
				finish();
				// return;
				// }

				// searchByKey();
			}
		});

		adapter = new AutoCompleteAdapter(this);
		adapter.setList(resHistoryList, true);
		lvAutoComplete.setAdapter(adapter);
		lvAutoComplete.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				int index = arg2;

				List<CommonTypeDTO> list = ((AutoCompleteAdapter) arg0.getAdapter()).getList();

				CommonTypeDTO data = list.get(index);

				if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.getUuid()) || String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.getUuid())) {
					// 控制项或者消息项的场合
					if (data.getName().equals(getString(R.string.text_button_clear))) {
						// 清空历史记录的场合
						SessionManager.getInstance().getListManager().removeAllSearchHistoryInfo(AutoCompleteActivity.this);
						resHistoryList.clear();
						adapter.setList(resHistoryList, true);
						foodHistoryList.clear();
						adapter.setList(foodHistoryList, true);
					}
				} else {

					// 保存为搜索历史
					CommonTypeDTO historyDto = data.clone();
					historyDto.setName(historyDto.getName().replace("<b>", "").replace("</b>", ""));
					SessionManager.getInstance().getListManager().addSearchHistoryInfo(AutoCompleteActivity.this, historyDto, "1");

					// 来源页为选择餐厅时
					if (MainFrameActivity.getLastActivityClass() == RestaurantSearchActivity.class) {
						Intent intent = new Intent();
						intent.putExtra(Settings.REQUEST_BUNDLE_KEYWORD, historyDto.getName());
						setResult(Settings.REQUEST_CODE_GET_KEYWORD, intent);
						finish();
						return;
					}

					// 设置搜索的筛选条件
					SessionManager.getInstance().getFilter().setKeywords(historyDto.getName());

					// 去餐厅列表页
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "搜索");
					// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE_2,
					// Settings.AUTO_COMPLETE_ACTIVITY); // 用来识别是否是从本页过去的
					ActivityUtil.jump(AutoCompleteActivity.this, ResAndFoodListActivity.class, 0, bundle);
					// finish();
				}
			}
		});

		// 设置语音识别按钮事件-----added by xujianjun, 2011-12-13
		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(this);
		if (eng != null) {

			eng.bindButtonAndEditText(voiceSearchButton, etAutoComplete, 0, null, new Runnable() {

				@Override
				public void run() {
					doAfterVoiceSearch();
				}
			});
			// eng.bindButtonAndEditText(searchButton, (EditText)view);
		}
		RecognitionEngine eng2 = RecognitionEngine.getEngine(this);

		if (eng2 != null) {

			eng2.bindButtonAndEditText(searchAgainButton, etAutoComplete, 0, null, new Runnable() {

				@Override
				public void run() {
					doAfterVoiceSearch();
				}

			});
		}

		if (!TextUtils.isEmpty(keyword)) {
			etAutoComplete.setText(keyword);
		}
	}

	/**
	 * 语音搜索完成后
	 */
	private void doAfterVoiceSearch() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setSearchAgainButtonVisible(true);
			}
		});
	}

	/**
	 * 获得关键字提示
	 */

	private void executeGetSuggestKeywordListTask(String key) {
		if (CheckUtil.isEmpty(key)) {
			adapter.setList(resHistoryList, true);

			isTaskSafe = true;
			isLast = true;
			startIndex = 1;
			return;
		}

		// 搜索框文字改变触发搜索，则不控制线程安全，并行发送请求
		// 此种情况可以通过startIndex进行判定
		if (startIndex != 1) {
			if (isTaskSafe) {
				// 设置线程不安全
				this.isTaskSafe = false;
			} else {
				return;
			}
		}

		if (adapter.isReset) {
			isLast = true;
			startIndex = 1;
		}

		if (getSuggestKeywordListTask != null && !getSuggestKeywordListTask.isCancelled()) {
			// 上一个任务还在运行的场合
			getSuggestKeywordListTask.cancel(true);
		}
		getSuggestKeywordListTask = new GetSuggestKeywordListTask(this, startIndex);
		getSuggestKeywordListTask.setKeywords(key);
		getSuggestKeywordListTask.execute(new Runnable() {

			@Override
			public void run() {
				// 发送点击跟踪---------------------------------------发送完后清为０
				AutoCompleteActivity.voiceInputTag = 0;
				// --------------------------------------------------
				CommonTypeListDTO dto = getSuggestKeywordListTask.dto;
				if (dto != null) {
					String key = etAutoComplete.getText().toString().trim();
					if (CheckUtil.isEmpty(key)) {
						adapter.setList(resHistoryList, true);
					} else {
						isLast = dto.pgInfo.lastTag;
						startIndex = dto.pgInfo.nextStartIndex;
						adapter.addList(dto.getList(), isLast);

					}
					if (adapter.getList() != null && adapter.getList().size() > 0 && adapter.isReset) {
						lvAutoComplete.setSelection(0);
					}

				}
				isTaskSafe = true;

			}
		}, new Runnable() {

			@Override
			public void run() {
				// 发送点击跟踪---------------------------------------发送完后清为０
				AutoCompleteActivity.voiceInputTag = 0;
				// --------------------------------------------------
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<CommonTypeDTO>(), isLast);
			}
		});
	}

	/**
	 * 关键在搜索
	 */
	private boolean searchByKey() {
		// 如果按下的是回车键,获得关键字
		String key = etAutoComplete.getText().toString().trim();
		if (CheckUtil.isEmpty(key)) {
			// 当没有输入关键字的场合，提示
			DialogUtil.showToast(AutoCompleteActivity.this, getString(R.string.text_info_please_input_key));
		} else {
			// 设置筛选
			SessionManager.getInstance().getFilter().setKeywords(key);
			// 添加入历史搜索
			CommonTypeDTO dto = new CommonTypeDTO();
			dto.setName(key);
			SessionManager.getInstance().getListManager().addSearchHistoryInfo(AutoCompleteActivity.this, dto, "1");

			// 去搜索结果列表页
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, getString(R.string.text_button_back));
			ActivityUtil.jump(AutoCompleteActivity.this, ResAndFoodListActivity.class, 0, bundle);
			// finish();
		}
		return false;
	}

	void setSearchAgainButtonVisible(boolean isVisible) {
		// 如果是带关键字过来的，显示重新搜索按钮
		if (isVisible) {
			searchAgainButton.setVisibility(View.VISIBLE);
			voiceSearchButton.setVisibility(View.GONE);
		} else {
			searchAgainButton.setVisibility(View.GONE);
			voiceSearchButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void finish() {
		super.finish();
	}
}
