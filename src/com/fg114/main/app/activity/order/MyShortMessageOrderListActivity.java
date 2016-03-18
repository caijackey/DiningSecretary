package com.fg114.main.app.activity.order;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.adapter.OrderListOfShortMessageAdapter;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.OrderList2Data;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.task.FindOrdersTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 可发送短信请柬的订单列表界面
 * 
 * @author xujianjun,2012-05-29
 * 
 */
public class MyShortMessageOrderListActivity extends MainFrameActivity {

	private static final String TAG = "MyShortMessageOrderListActivity";

	// 本地缓存
	private boolean isLogin;
	private UserInfoDTO userInfo;
	private int fromPage; // 返回页面
	private boolean isFirstcreate = true;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView lvOrder;
	private OrderListOfShortMessageAdapter adapter; // 普通订单adapter
	private LinearLayout loginButtonLayout;
	private Button btnLongin;
	private LinearLayout selectRestaurantButtonLayout;
	private Button btnSelectRestaurant ;	

	// 变量

	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int pageNo = 1;
	private String mStatusId = "";
	private boolean isFirst = true;

	// 任务
	private FindOrdersTask findOrdersTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();

		// 获得缓存数据
		isLogin = SessionManager.getInstance().isUserLogin(this);
		userInfo = SessionManager.getInstance().getUserInfo(this);

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


		// 刷新订单数量
		Fg114Application.isNeedUpdate = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		//----先隐藏
		loginButtonLayout.setVisibility(View.GONE);
		selectRestaurantButtonLayout.setVisibility(View.GONE);
		// ----
		resetTask();

		// 设为第一页
		pageNo = 1;
		isLast = true;
		// 获得查询结果
		executeFindOrdersTask();

		// 隐藏登录按钮
		isLogin = SessionManager.getInstance()
				.isUserLogin(MyShortMessageOrderListActivity.this);
		userInfo = SessionManager.getInstance()
				.getUserInfo(MyShortMessageOrderListActivity.this);
		if (isLogin) {
			MyShortMessageOrderListActivity.this.getTvTitle().setText(userInfo.getNickName()+"的订单");
			loginButtonLayout.setVisibility(View.GONE);
		}else{
			loginButtonLayout.setVisibility(View.VISIBLE);
		}
		
		//设置短信邀请最初页，以便在短信发送成功后直接退到这一页
		Settings.sendShortMessageOrignalActivityId=0;
		
		// v3.1.41 隐藏选择餐厅的按钮
		//第一次进入时的朦皮
//		DialogUtil.showVeilPictureOnce(this, R.drawable.mask_short_message_order_list,"ShowOnceVeil_MyShortMessageOrderListActivity");
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
		this.getTvTitle().setText("选择订单");
		this.getTvTitle().setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
		this.getBtnGoBack().setText(R.string.text_button_back);		
		
		// v3.1.41 隐藏选择餐厅的按钮
		this.getBtnOption().setVisibility(View.INVISIBLE);
		

		// 内容部分
		mInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.user_short_message_order_list, null);
		lvOrder = (ListView) contextView
				.findViewById(R.id.user_order_List_listview);
		loginButtonLayout = (LinearLayout) contextView
				.findViewById(R.id.user_order_List_LoginLayout);
		btnLongin = (Button) contextView
				.findViewById(R.id.user_order_List_btnLogin);

		
		btnLongin.setOnClickListener(new OnClickListener() {
			/**
			 * 登录，显示所有订单
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showUserLoginDialog(MyShortMessageOrderListActivity.this,
						new Runnable() {

							@Override
							public void run() {
								resetTask();

								// 设为第一页
								pageNo = 1;
								isLast = true;
								executeFindOrdersTask();

								
							}
						}, 0);
			}
		});
		//---
		selectRestaurantButtonLayout = (LinearLayout) contextView
				.findViewById(R.id.user_order_List_SelectRestaurantButtonLayout);
		btnSelectRestaurant = (Button) contextView
				.findViewById(R.id.user_order_List_btnSelectRestaurant);		
		
		btnSelectRestaurant.setOnClickListener(new OnClickListener() {
			/**
			 * 登录，显示所有订单
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				goSelectRestaurant();
			}
		});
        //普通订单界面适配器
		adapter = new OrderListOfShortMessageAdapter(this);
		adapter.setList(null, false);
		lvOrder.setAdapter(adapter);
		
	    
		lvOrder.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

					int index = arg2;
					List<OrderList2Data> list = ((OrderListOfShortMessageAdapter) arg0
							.getAdapter()).getList();
					if (list != null) {
						OrderList2Data data = list.get(index);
						if (!data.getOrderId().equals(
								String.valueOf(Settings.CONTRL_ITEM_ID))) {
							// 短信邀请模板选择页面
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_ORDER_ID,data.getOrderId());
							bundle.putString(Settings.BUNDLE_REST_ID,data.getRestId());
							ActivityUtil.jump(MyShortMessageOrderListActivity.this,
									SelectSMSActivity.class,
									0, bundle);
						}
					}				
			}
		});

		lvOrder.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
						&& isRefreshFoot) {
					if (isLast == false) {
						// 获得餐厅列表
						executeFindOrdersTask();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					// 当到达列表尾部时
					isRefreshFoot = true;
				} else {
					isRefreshFoot = false;
				}
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}
	
	private void goSelectRestaurant() {
		//借用随手拍页面的选择餐厅页面去选择一个餐厅
		Bundle bundle = new Bundle();
		bundle.putBoolean("isFromShortMessageOrderMesssage",true);

		ActivityUtil.jump(MyShortMessageOrderListActivity.this,
				RestaurantSearchActivity.class,
				0, bundle);
		
	}
	/**
	 * 获得列表
	 */

	private void executeFindOrdersTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				pageNo = pageNo + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}

		if (DEBUG)
			Log.d(TAG, "now page no is：" + pageNo);

		// 获得位置
		isLogin = SessionManager.getInstance().isUserLogin(this);
		userInfo = SessionManager.getInstance().getUserInfo(this);
		String token = "";
		if (isLogin) {
			token = userInfo.getToken();
		} else {
			token = "";
		}
		// 创建任务
		findOrdersTask = new FindOrdersTask(null, this, 3, token, mStatusId, pageNo);
		// 执行任务
		findOrdersTask.execute(new Runnable() {

			@Override
			public void run() {
				OrderListDTO dto = findOrdersTask.dto;

//				if (dto != null) {
//
//					if (dto.getList().size() == 0) {
//						isLast = true;
//					} else {
//						isLast = dto.pgInfo.lastTag;
//					}
//
//					adapter.addList(dto.getList(), isLast);
//					// 如果没有数据，则隐藏listView，只显示请登录（未登录时）或者 选择餐厅（已登录时）的按钮
//					if(isLast && (adapter.getList()!=null&&(adapter.getList().size()==0||String.valueOf(Settings.CONTRL_ITEM_ID).equals(adapter.getList().get(0).getOrderId())))) {
//						lvOrder.setVisibility(View.GONE);
//						//没有列表的时候，按钮层居中并且没有背景
//						loginButtonLayout.setBackgroundColor(0x00ffffff);
//						selectRestaurantButtonLayout.setBackgroundColor(0x00ffffff);
//						//-----------------------------------------------------------
//						//登录状态下，没数据的时候要显示选择餐厅按钮的层
//						if(isLogin){
//							selectRestaurantButtonLayout.setVisibility(View.VISIBLE);
//						}
//						else{
//							selectRestaurantButtonLayout.setVisibility(View.GONE);
//						}
//					} else {
//						//有列表的时候，按钮层有背景
//						loginButtonLayout.setBackgroundColor(getResources().getColor(R.color.background_color_white));
//						selectRestaurantButtonLayout.setBackgroundColor(getResources().getColor(R.color.background_color_white));
//						//-----------------------------------------------------------
//						lvOrder.setVisibility(View.VISIBLE);
//					}
//
//				}

				// 设置线程安全
				isTaskSafe = true;
			}
		}, new Runnable() {
			@Override
			public void run() {
				// 设置线程安全
				isTaskSafe = true;
				adapter.addList(new ArrayList<OrderList2Data>(), isLast);
			}
		});
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		if(findOrdersTask!=null){			
			findOrdersTask.cancel(true);
		}
		adapter.setList(null, false);
		lvOrder.setAdapter(adapter);

		isTaskSafe = true;
		System.gc();
	}
}
