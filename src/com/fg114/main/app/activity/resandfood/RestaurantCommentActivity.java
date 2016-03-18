package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.CommentAdapter;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.task.GetResCommentListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅评论详细界面 餐厅详情主页面下跳餐厅评论第一个页面
 * 
 * @author zhangyifan
 * 
 */
public class RestaurantCommentActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantCommentActivity";

	// 传入参数
	private String restaurantId; // 餐厅ID
	private String orderId; // orderID

	// 控制变量
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView lvCommentList;
	private CommentAdapter adapter; 
	// 任务
	private GetResCommentListTask getResCommentListTask;
	
	private List<CommentData> commentDataList;

	// 本地缓存数据
	private RestInfoData restaurantInfo;
	
	private Calendar mDefaultBookTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅评论列表","");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		this.bundleData.putString(Settings.BUNDLE_KEY_ID, restaurantId);
		// 获得缓存的餐厅信息
		restaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);
		
		if (bundle.containsKey(Settings.BUNDLE_DEFAULT_BOOK_TIME)) {
			mDefaultBookTime = (Calendar) bundle.getSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME);
		}

		
		
		
		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
	}
	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅评论列表", "");
		// ----------------------------
	}
	@Override
	protected void onResume() {
		super.onResume();
//		this.getRbComment().setChecked(true);
		// 取得评论
		resetTask();
		startIndex = 1;
		isLast = true;
		executeGetResCommentListTask();
	}

	@Override
	public void finish() {
		super.finish();
		resetTask();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		String title = SessionManager.getInstance().getRestaurantInfo(this, restaurantId).name;
		if (TextUtils.isEmpty(title)) {
			title = "餐厅评论";
		}
		this.getTvTitle().setText(title);
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setText(R.string.text_button_comment);
		this.getBtnOption().setOnClickListener(new OnClickListener() {
			/**
			 * 去评论提交页
			 * 
			 * @param v
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我要评论按钮");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putLong(Settings.FROM_TAG, 1);
				ActivityUtil.jump(RestaurantCommentActivity.this, RestaurantCommentSubmitActivity.class, 0, bundle);
				

			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_comment, null);
		lvCommentList = (ListView) contextView.findViewById(R.id.res_comment_lvCommentList);

		adapter = new CommentAdapter(RestaurantCommentActivity.this, restaurantId);
		adapter.setList(null, false);
		lvCommentList.setAdapter(adapter);
		lvCommentList.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
						&& isRefreshFoot) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----

					if (isLast == false) {
						executeGetResCommentListTask();
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
		// 点击进入评论详细页
		lvCommentList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				CommentData data = (CommentData) arg0.getItemAtPosition(arg2);
				if (data!=null&&!String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("选择行");
					// -----
					
					Bundle bundle = new Bundle();
					bundle.putSerializable(Settings.BUNDLE_REST_COMMENT_DATA, data);
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					ActivityUtil.jump(RestaurantCommentActivity.this, RestaurantCommentDetailActivity.class, 0, bundle);
				}
			}
		});
		this.setFunctionLayoutGone();
		// 餐厅频道菜单
//		this.getMenuLayout().setVisibility(View.VISIBLE);
//		this.getMenuGroup().setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkId) {
//				if (checkId == getRbDetail().getId()) {
//					// 去餐厅详细页的场合
//					finish();
//					// overridePendingTransition(R.anim.right_slide_in,
//					// R.anim.right_slide_out);
//					ActivityUtil.overridePendingTransition(RestaurantCommentActivity.this, R.anim.right_slide_in, R.anim.right_slide_out);
//
//				} else if (checkId == getRbComment().getId()) {
//					// 去餐厅评论页的场合
//					return;
//				} else if (checkId == getRbDiscount().getId()) {
//					// 去餐厅折扣页的场合
//					Bundle bundle = new Bundle();
//					bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
//					
//					if (mDefaultBookTime != null) {
//						bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME, mDefaultBookTime);
//					}
//					
//					ActivityUtil.jump(RestaurantCommentActivity.this, RestaurantDiscountActivity.class, 0, bundle);
//					finish();
//					// overridePendingTransition(R.anim.right_slide_in,
//					// R.anim.right_slide_out);
//					ActivityUtil.overridePendingTransition(RestaurantCommentActivity.this, R.anim.right_slide_in, R.anim.right_slide_out);
//				} else if (checkId == getRbUpload().getId()) {
//					getRbComment().setChecked(true);// 将按钮恢复到原来状态
//				} else if (checkId == getRbOther().getId()) {
//					getRbComment().setChecked(true);// 将按钮恢复到原来状态
//					// ActivityUtil.shareRes(RestaurantCommentActivity.this,
//					// restaurantId);
//
//				}
//			}
//		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// 设置Mainframe中的Bundle信息，用于弹出餐厅报错时使用
		if (restaurantInfo != null) {
			bundleData.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
			bundleData.putDouble(Settings.BUNDLE_REST_LONGITUDE, restaurantInfo.longitude);
			bundleData.putDouble(Settings.BUNDLE_REST_LATITUDE, restaurantInfo.latitude);
		}
	}
	@Override
	protected String getRestaurantId() {
		//向父类提供数据
		return restaurantId;
	}
	@Override
	protected String getRestaurantName() {
		//向父类提供数据
		return restaurantInfo==null?"":restaurantInfo.name;	
	}

	/**
	 * 获得餐厅详细
	 */
	private void executeGetResCommentListTask() {

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

		if (DEBUG)
			Log.d(TAG, "now page no is：" + startIndex);

		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		
		
		
		// 创建任务
		getResCommentListTask = new GetResCommentListTask(null, this, restaurantId, CheckUtil.isEmpty(orderId)?"":orderId,startIndex); 

		// 执行任务
		getResCommentListTask.execute(new Runnable() {

			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				CommentListDTO dto = getResCommentListTask.dto;
				if (dto != null) {
					isLast = dto.pgInfo.lastTag;
					startIndex=dto.pgInfo.nextStartIndex;
					commentDataList=dto.list;
					adapter.addList(commentDataList, isLast);
				}

				// 设置线程安全
				isTaskSafe = true;
			}
		}, new Runnable() {
			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 设置线程安全
				isTaskSafe = true;
				isLast = true;
				adapter.addList(new ArrayList<CommentData>(), isLast);
			}
		});
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		recycle();
		if (getResCommentListTask != null) {
			getResCommentListTask.cancel(true);
			adapter.setList(null, false);
			lvCommentList.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		if (adapter != null) {
			Iterator<MyImageView> iterator = adapter.viewList.iterator();
			while (iterator.hasNext()) {
				iterator.next().recycle(true);
			}
			adapter.viewList.clear();
			System.gc();
		}
	}
}
