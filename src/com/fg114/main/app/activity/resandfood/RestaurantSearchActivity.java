package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MyShortMessageOrderListActivity;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.adapter.RestaurantSearchAdapter;
import com.fg114.main.app.data.BaseData;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.location.Loc;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.ResAndFoodList2DTO;
import com.fg114.main.service.dto.RestListDTO;
import com.fg114.main.service.dto.RestListData;
import com.fg114.main.service.task.GetResAndFoodTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.RightDrawableOnTouchListener;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 随手拍餐厅列表
 * 
 * @author chenguojin
 * 
 */
public class RestaurantSearchActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantSearchActivity";

	private static final int DISTANCE = 5000;// 默认条件距离5000m
	private static final String REGIONID = "0";// 默认地域ID：全部
	private static final String DISTRICTID = "0";// 商区ID : 全部商区
	private static final int SORTTYPETAG = 1;// 排序类别：距离

	// 传入参数
	// String mImageUri;
	int mFromPage;


	// 画面变量
	private boolean haveGpsTag = true;
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;
	private String key = "";

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView mListView;
	private RestaurantSearchAdapter adapter;
	private TextView mSearchKey;

	// 任务
	private GetResAndFoodTask getResAndFoodTask;

	// 值true，说明是从可发送短信邀请的订单页面(MyShortMessageOrderListActivity)过来的
	// 值false，正常的随手拍（从首页过来的）
	private boolean isFromShortMessageOrderMesssage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		// 获得来源页标志
		isFromShortMessageOrderMesssage = bundle.getBoolean("isFromShortMessageOrderMesssage", false);
		//

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle jumpBundle = new Bundle();
			jumpBundle.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, jumpBundle);
			return;
		}

		// 获得位置
//		haveGpsTag = Loc.isGpsAvailable();
//		if (!haveGpsTag) {
//			// 没有GPS的场合，去提示页
//			Bundle jumpBundle = new Bundle();
//			jumpBundle.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_gps_unavailable));
//			ActivityUtil.jump(this, ShowErrorActivity.class, 0, jumpBundle);
//			return;
//		}

		// 获得查询结果
		executeGetResAndFoodTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mUpdateCityThread = SessionManager.getInstance().updateGpsCity(this);

	}

	@Override
	public void finish() {
		super.finish();
		if (mUpdateCityThread != null) {
			mUpdateCityThread.interrupt();
		}
		resetTask();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Settings.REQUEST_CODE_GET_KEYWORD) {
			if (data != null) {
				String keyword = data.getStringExtra(Settings.REQUEST_BUNDLE_KEYWORD);
				if (keyword != null) {
					mSearchKey.setText(keyword);
				}
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		this.getTvTitle().setText("选择餐厅");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_search, null);
		mListView = (ListView) contextView.findViewById(R.id.restaurant_search_listview);
		mSearchKey = (TextView) contextView.findViewById(R.id.search_bar_tvKey);
		mSearchKey.setHint(R.string.text_layout_search_recommend_res);

		ViewUtils.setClearable(mSearchKey);

		// if (Settings.uploadPictureUri == null ||
		// Settings.uploadPictureUri.equals("")) {
		// DialogUtil.showToast(this, "没有选择任何图片");
		// super.finish();
		// return;
		// }

		// mSearchKey.setOnTouchListener(new RightDrawableOnTouchListener() {
		// @Override
		// public boolean onDrawableTouch(final MotionEvent event) {
		// mSearchKey.setText("");
		// return true;
		// }
		// });

		contextView.findViewById(R.id.search_bar_rlSearch).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ViewUtils.preventViewMultipleClick(arg0, 1000);
				Bundle bundle = new Bundle();
				ActivityUtil.jump(RestaurantSearchActivity.this, AutoCompleteActivity.class, Settings.REQUEST_CODE_GET_KEYWORD, bundle);
			}
		});

		contextView.findViewById(R.id.search_bar_btVoice).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				ActivityUtil.showVoiceDialogForSearch(RestaurantSearchActivity.this, 7, new ActivityUtil.OnRecognizedFinishListener() {

					@Override
					public void onRecognizedFinish(String text) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_KEY_KEYWORD, text);
						AutoCompleteActivity.voiceInputTag = 7;
						ActivityUtil.jump(RestaurantSearchActivity.this, AutoCompleteActivity.class, Settings.REQUEST_CODE_GET_KEYWORD, bundle);
					}
				});
			}
		});

		mSearchKey.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// if (CheckUtil.isEmpty(mSearchKey.getText().toString())) {
				// mSearchKey.setCompoundDrawables(null, null, null, null);
				// }
				// else {
				// mSearchKey.setCompoundDrawablesWithIntrinsicBounds(null,
				// null,
				// RestaurantSearchActivity.this.getResources().getDrawable(R.drawable.super57_history_remove_bt01),
				// null);
				// }
				key = mSearchKey.getText().toString().trim();
				startIndex = 1;
				isLast = true;
				// 获得查询结果
				resetTask();
				executeGetResAndFoodTask();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		adapter = new RestaurantSearchAdapter(RestaurantSearchActivity.this, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeGetResAndFoodTask();
			}
		}, Settings.uploadPictureUri);
		// 如果是从短信邀请页面来选择餐厅的，就不允许添加餐厅
		//if (getLastActivityClass() == MyShortMessageOrderListActivity.class) {
		//	adapter.canAddRestaurant = false;
		//}
		adapter.canAddRestaurant = false;
		adapter.setList(null, false);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg2;
				List<RestListData> list = ((RestaurantSearchAdapter) arg0.getAdapter()).getList();
				if (list != null) {
					RestListData data = list.get(index);
					if (data != null && !(""+Settings.CONTRL_ITEM_ID).equals(data.restId)) {
						// 如果是从短信邀请订单页面来选餐厅的，跳转到短信邀请模板选择页面
//						if (isFromShortMessageOrderMesssage) {
//							// 短信邀请模板选择页面
//							Bundle bundle = new Bundle();
//							bundle.putString(Settings.BUNDLE_REST_ID, data.getResId());
//							ActivityUtil.jump(RestaurantSearchActivity.this, SelectSMSActivity.class, 0, bundle);
//						} else {
//							// 加入最近浏览
//							SessionManager.getInstance().getListManager().addHistoryList(RestaurantSearchActivity.this, data);
//
//							// 去菜品选择页面
//							Bundle bundle = new Bundle();
//							bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID, data.getResId());
//							bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, data.getResName());
//							ActivityUtil.jump(RestaurantSearchActivity.this, RestaurantUploadActivity.class, 0, bundle);
//						}
						Intent intent=new Intent();
						intent.putExtra(Settings.BUNDLE_REST_ID, data.restId);
						intent.putExtra(Settings.BUNDLE_REST_NAME, data.restName);
						setResult(12345, intent);
						finish();
						
					}
				}
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 线程安全且不是最后一页的场合，获得餐厅列表
						executeGetResAndFoodTask();
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

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}


	/**
	 * 获得餐厅列表
	 */
	private void executeGetResAndFoodTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				//startIndex = startIndex + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}

		// 创建任务
		getResAndFoodTask = new GetResAndFoodTask(null, this, key, startIndex);

		// 执行任务
		getResAndFoodTask.execute(new Runnable() {

			@Override
			public void run() {

				RestListDTO dto = getResAndFoodTask.dto;

				if (dto != null && dto.list!=null) {
					if (dto.list.size() == 0) {
						isLast = true;
					} else {
						isLast = dto.pgInfo.lastTag;
					}
					startIndex=dto.pgInfo.nextStartIndex;
					adapter.addList(dto.list, isLast);
				}

				// 设置线程安全
				isTaskSafe = true;

			}
		}, new Runnable() {
			@Override
			public void run() {
				// 设置线程安全
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<RestListData>(), isLast);
			}
		});
	}



	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		if (getResAndFoodTask != null) {
			getResAndFoodTask.cancel(true);
			adapter.setList(null, false);
			mListView.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}
}
