package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.adapter.SearchFoodListAdapter2;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.SortedFoodListDTO;
import com.fg114.main.service.dto.SortedFoodListData;
import com.fg114.main.service.dto.SortedFoodSubListData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.GetSortedResFoodListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.MyThreadPool;
import com.fg114.main.util.RightDrawableOnTouchListener;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.activity.FriendSelectionActivity;
import com.fg114.main.weibo.adapter.WeiboFriendSelectionAdapter;
import com.fg114.main.weibo.dto.User;
import com.fg114.main.weibo.task.GetFriendsListTask;

/**
 * 随手拍菜品选择页面
 * 
 * @author chenguojin
 * 
 * recode by xujianjun 2012-09-14
 * 
 */
public class RestaurantSearchFoodActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantSearchFoodActivity";

	// 传入参数
	private int fromPage; // 返回页面
	private String restaurantId; // 菜品所属餐厅id
	private String restaurantName; // 菜品所属餐厅名
	private String key = "";
	// private String mImageUri;
	private String mFoodName = "";// 菜品的名字

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView mListView;
	private SearchFoodListAdapter2 adapter;
	private EditText etSearch;
	// private Button btnSearch;
	private Button mUploadBtn;

	public static final int CAMERAIMAGE = 9999; // 拍照上传
	public static final int LOCALIMAGE = 9998; // 本地上传

	// 任务
	private GetSortedResFoodListTask task;

	// 保证单线程性的池
	private static MyThreadPool searchThread=new MyThreadPool(1, -1, 50);;

	//暂无信息
	private TextView noInfo;
	//弹出字母层提示
	private TextView overlay;
	//所有菜品列表　
	private List<ResFoodData> list=new ArrayList<ResFoodData>();
	
	// 提示字母
	private String hintLetter;
	// 首字母提示是否可见
	private boolean needHintLetter = true; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID);
		restaurantName = bundle.getString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME);

		if (bundle.containsKey(Settings.BUNDLE_UPLOAD_FOOD_NAME)) {
			mFoodName = bundle.getString(Settings.BUNDLE_UPLOAD_FOOD_NAME);
		}
		
		if(CheckUtil.isEmpty(restaurantId)){
			finish();
		}
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();

		executeTask();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 第一次的朦皮
		//DialogUtil.showVeilPictureOnce(this, R.drawable.new_image_search_food, "ShowOnceVeil_RestaurantSearchFoodActivity");

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void finish() {
		SharedprefUtil.save(this, Settings.BUNDLE_FOODANDRES_COMMENT, "");
		super.finish();
		resetTask();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("填写菜名");
		this.getBtnGoBack().setText(R.string.text_button_cancel);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_search_food, null);
		mListView = (ListView) contextView.findViewById(R.id.restaurant_search_food_listview);
		// btnSearch = (Button) contextView.findViewById(R.id.index_btnSearch);
		etSearch = (EditText) contextView.findViewById(R.id.restaurant_search_etSearchbox);
		mUploadBtn = (Button) contextView.findViewById(R.id.restaurant_search_food_upload);
		overlay = (TextView) contextView.findViewById(R.id.pop_letter_layer);
		noInfo = (TextView) contextView.findViewById(R.id.no_info);
		
		//--初始不显示
		noInfo.setVisibility(View.INVISIBLE);
		overlay.setVisibility(View.INVISIBLE);
		//--
		
		this.getBtnOption().setVisibility(View.INVISIBLE);
		
		ViewUtils.setClearable(etSearch);

		// 上传图片按钮
		mUploadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				String str = etSearch.getText().toString().trim();
				if (TextUtils.isEmpty(str)) {
					DialogUtil.showToast(RestaurantSearchFoodActivity.this, "请输入菜名");
				} else {
//					Bundle bundle = new Bundle();
//					bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.RESTAURANT_SEARCH_FOOD_ACTIVITY);
//					// bundle.putString(Settings.BUNDLE_KEY_CONTENT,
//					// mImageUri);// 图片地址
//					bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);// 餐厅ID
//					bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, restaurantName);// 餐厅名称
//					bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_FOOD);
//					bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME, str);// 菜品名称
//
//					ActivityUtil.jump(RestaurantSearchFoodActivity.this, RestaurantUploadActivity.class, Settings.RESTAURANT_SEARCH_FOOD_ACTIVITY, bundle);
//					
					foodSelectionDone("",str);
				}
			}
		});

		// 环境按钮
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 环境图片页面
				Bundle bundle = new Bundle();
				// 图片地址
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);// 餐厅ID
				bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_RESTAURANT);
				bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, restaurantName);// 餐厅名称

				ActivityUtil.jump(RestaurantSearchFoodActivity.this, RestaurantUploadActivity.class, 0, bundle);
			}
		});

//		etSearch.setOnTouchListener(new RightDrawableOnTouchListener() {
//			@Override
//			public boolean onDrawableTouch(final MotionEvent event) {
//				etSearch.setText("");
//				return true;
//			}
//		});
		//---
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
//				if (CheckUtil.isEmpty(etSearch.getText().toString())) {
//					etSearch.setCompoundDrawables(null, null, null, null);
//				} else {
//					etSearch.setCompoundDrawablesWithIntrinsicBounds(null, null,
//							RestaurantSearchFoodActivity.this.getResources().getDrawable(R.drawable.super57_history_remove_bt01), null);
//				}
				makeSearch(s.toString());
			}

		});

		


		adapter = new SearchFoodListAdapter2(RestaurantSearchFoodActivity.this);
		setList(null);
		mListView.setAdapter(adapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				int index = position;
				List<ResFoodData> list = ((SearchFoodListAdapter2) parent.getAdapter()).getList();
				if (list != null) {
					ResFoodData data = list.get(index);
					if (!data.getUuid().equals("-1")) {
//						Bundle bundle = new Bundle();
////						bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.RESTAURANT_SEARCH_FOOD_ACTIVITY);
//						// bundle.putString(Settings.BUNDLE_KEY_CONTENT,
//						// mImageUri);// 图片地址
//						bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);// 餐厅ID
//						bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_ID, data.getUuid());// 菜品ID
//						bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME, data.getName());// 菜品名称
//						bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_FOOD);// 上传标志
//						bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, restaurantName);// 餐厅名称
//						ActivityUtil.jump(RestaurantSearchFoodActivity.this, RestaurantUploadActivity.class, 0, bundle);
						foodSelectionDone(data.getUuid(),data.getName());
					}
				}
			}
		});
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				 //Log.i("------->",""+firstVisibleItem+","+visibleItemCount+","+totalItemCount);
				if (needHintLetter ) {
					
					if (RestaurantSearchFoodActivity.this.mListView.getCount() > 0 ) {
						RestaurantSearchFoodActivity.this.hintLetter = 
								HanziUtil.getFirst(((ResFoodData) view.getAdapter().getItem(firstVisibleItem)).getFirstLetter())
								.toUpperCase();
						showPopLetterLayer();
						return;
					}
					// String firstLetter =
					// HanziToPinyinUtil.getAlpha(mFilterLocalList
					// .get(firstVisibleItem).getName());
					/*
					 * String firstLetter =
					 * HanziToPinyinUtil.getFirst(mFilterLocalList
					 * .get(firstVisibleItem).getUuid());
					 */

				}
			}
		});
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 获得菜品列表
	 */
	private void executeTask() {
//		task = new GetSortedResFoodListTask("正在获取菜品数据，请稍候...", this, restaurantId);
//		task.execute(new Runnable() {
//
//			@Override
//			public void run() {
//				task.closeProgressDialog();
//				// 如果没有菜品，显示信息
//				if (task.list != null && task.list.size() == 0) {
//					noInfo.setVisibility(View.VISIBLE);
//					mListView.setVisibility(View.GONE);
//				} else {
//					noInfo.setVisibility(View.GONE);
//					mListView.setVisibility(View.VISIBLE);
//					adapter = new SearchFoodListAdapter2(RestaurantSearchFoodActivity.this);
//					mListView.setAdapter(adapter);
//					setList(task.list);					
//					list = task.list;
//				}
//
//			}
//
//			
//		});
		
		ServiceRequest request  = new ServiceRequest(API.getSortedRestFoodList);
		request.addData("restId",restaurantId);
		CommonTask.request(request, new CommonTask.TaskListener<SortedFoodListDTO>(){
			protected void onSuccess(SortedFoodListDTO dto) {
				if(dto != null){
					if(list == null){
						list = new ArrayList<ResFoodData>();
					}
					List<SortedFoodListData> sortedFoodListData = dto.getList();
					
					//合并菜品
					for(SortedFoodListData foodListdata : sortedFoodListData){
						List<SortedFoodSubListData> sortedFoodList = foodListdata.getList();
						for(SortedFoodSubListData subFoodListdata : sortedFoodList){
							ResFoodData tempFoodData = new ResFoodData();
							tempFoodData.setUuid(subFoodListdata.getUuid());
							tempFoodData.setName(subFoodListdata.getName());
							tempFoodData.setFirstLetter(foodListdata.getFirstLetter());
							list.add(tempFoodData); 
						}
					}
					// ---- 
					if(list.size() == 0){ // 如果没有菜品
						noInfo.setVisibility(View.VISIBLE);
						mListView.setVisibility(View.GONE);
					}else{
						noInfo.setVisibility(View.GONE);
						mListView.setVisibility(View.VISIBLE);
						adapter = new SearchFoodListAdapter2(RestaurantSearchFoodActivity.this);
						mListView.setAdapter(adapter);
						setList(list);					
					}
				}else{
					noInfo.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				}
			};
		});

	}
	private void setList(List<ResFoodData> list) {
		adapter.setList(list);
		if (list== null || list.size() == 0) {
			noInfo.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {
			noInfo.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * 回收内存
	 */
	private void recycle() {

	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		recycle();
		if (task != null) {
			task.cancel(true);
			setList(null);
			mListView.setAdapter(adapter);

		}
		System.gc();
	}

	// 提交一个线程去过滤
	private void makeSearch(final String word) {
		if (word == null) {
			return;
		}

		searchThread.submit(new MyThreadPool.Task() {

			@Override
			public void run() {
				try {
					// 如果是空字符串，则显示全部列表
					if (word.trim().equals("")) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								setList(list);
							}
						});
						return;
					}
					// 否则按照搜索的关键词过滤
					final List<ResFoodData> tempList = new ArrayList<ResFoodData>();
					for (ResFoodData u : list) {
						//

						// if(u.getName()!=null&&u.getName().toUpperCase().contains(word.trim().toUpperCase())){
						// tempList.add(u);
						// }
						if (u.getName() != null && HanziUtil.doesStringMatchKeywords(u.getName().toLowerCase(), word.trim().toLowerCase())) {
							tempList.add(u);
						}
					}
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setList(tempList);
						}
					});

				} catch (Exception e) {
					Log.e(TAG, "error in searchThread.run()", e);
				}
			}
		});

	}
	//---
	private long timestamp=0;
	// 显示字母提示框
	private void showPopLetterLayer() {
		if ("".equals(this.hintLetter)) {
			return; // 不显示空字符
		}
		if (this.overlay != null) {
			this.overlay.setText(this.hintLetter);
			this.overlay.setVisibility(View.VISIBLE);
		}
		final long time=SystemClock.elapsedRealtime();
		timestamp=time;
		this.overlay.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				hidePopLetterLayer(time);
			}
		}, 500);
	}

	// 隐藏字母提示框
	private void hidePopLetterLayer(long time) {

		if (this.overlay != null && timestamp==time) {
			this.overlay.setVisibility(View.INVISIBLE);
		}
	}
	//结束选择
	private void foodSelectionDone(String foodId,String foodName){
		
		Intent intent=new Intent();
		intent.putExtra(Settings.BUNDLE_UPLOAD_FOOD_ID, foodId);// 菜品ID
		intent.putExtra(Settings.BUNDLE_UPLOAD_FOOD_NAME, foodName);// 菜品名称
		setResult(RESULT_OK, intent);
		this.finish();
	}

}
