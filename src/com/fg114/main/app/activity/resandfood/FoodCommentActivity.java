package com.fg114.main.app.activity.resandfood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.*;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.task.*;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 菜品评论列表
 * @author xu jianjun, 2012-01-06
 *
 */
public class FoodCommentActivity extends MainFrameActivity {
	
	private static final String TAG = "FoodCommentActivity";
	
	//传入参数
	private	String foodId;	//菜品id
	private	String foodName;	//菜品名称
	private int fromPage;
	
	//控制变量
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int pageNo = 1;

	//界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView lvCommentList;
	private FoodCommentListAdapter adapter;
	private ResFoodCommentListDTO commentList;
	//任务
	private GetFoodCommentListTask getFoodCommentListTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		String[] idAndName=bundle.getStringArray(Settings.BUNDLE_KEY_ID);
		foodId = idAndName[0];
		foodName = idAndName[1];
		
		//初始化界面
		initComponent();
		
		//检查网络是否连通
        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
        if (!isNetAvailable) {
        	//没有网络的场合，去提示页
        	Bundle bund = new Bundle();
        	bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
	    	ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.getRbComment().setChecked(true);
		//取得评论
		resetTask();
		pageNo = 1;
		isLast = true;
        executeGetFoodCommentListTask();
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
		
		//设置标题
		this.getTvTitle().setText(foodName);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setText(R.string.text_button_comment);
		this.setFunctionLayoutGone();
		this.getMenuLayout().setVisibility(View.GONE);
		//内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.food_comment, null);
		lvCommentList = (ListView) contextView.findViewById(R.id.food_comment_list);
		adapter = new FoodCommentListAdapter(FoodCommentActivity.this);
		
		//控件初始化
		this.getBtnOption().setOnClickListener(new OnClickListener() {
			/**
			 * 去评论提交页
			 * @param v
			 */
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showUserLoginDialogWhenFoodComment(FoodCommentActivity.this,
						new Runnable() {
							@Override
							public void run() {
								Bundle bundle = new Bundle();
								String[] idArray = {foodId, foodName};
								bundle.putStringArray(Settings.BUNDLE_KEY_ID, idArray);
								int[] tags =new int[]{0,0,0,0};
								
								bundle.putIntArray(Settings.BUNDLE_DISH_TAG, tags);
								ActivityUtil.jump(FoodCommentActivity.this, 
										FoodCommentSubmitActivity.class, 
										0,
										bundle);
							}
						}, 0);
			}
		});
		

		adapter.setList(null, false);
		lvCommentList.setAdapter(adapter);
		lvCommentList.setOnScrollListener(new OnScrollListener() {
			
			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//Log.e("!!!!!!!","onScrollStateChanged,isRefreshFoot="+isRefreshFoot+",scrollState="+scrollState);
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && isRefreshFoot) {
				
					if(isLast == false){
						executeGetFoodCommentListTask();
					}
				}			
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//Log.e("!!!!!!!","onScroll,firstVisibleItem="+firstVisibleItem+",visibleItemCount="+visibleItemCount+",totalItemCount="+totalItemCount);
				if(firstVisibleItem + visibleItemCount == totalItemCount) {
					//当到达列表尾部时
					isRefreshFoot=true;
				}else{
					isRefreshFoot=false;
				}
			}
		});

		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		//((ViewGroup)this.getMainLayout().findViewById(R.id.dishbase_main_layout)).addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);	
	}
	
	/**
	 * 获得评论详细
	 */
	private void executeGetFoodCommentListTask() {
		
		if (isTaskSafe) {
			//线程安全的场合
			if(isLast == false){
				//线程安全且不是最后一页的场合，获得评论列表
				pageNo = pageNo + 1;
			}
			//设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}
		
		if (DEBUG) Log.d(TAG, "now page no is：" + pageNo);
		
		//创建任务
		getFoodCommentListTask = new GetFoodCommentListTask(
											null, 
											this,
											foodId,
											Settings.DEFAULT_RES_FOOD_PAGE_SIZE,
											pageNo);
									
		//执行任务
		getFoodCommentListTask.execute(new Runnable() {
			
			@Override
			public void run() {
				
				commentList = getFoodCommentListTask.dto;
				if (commentList != null) {
					isLast = commentList.pgInfo.lastTag;	
					adapter.addList(commentList.getList(), isLast);
					
					//需要将最新评论数据带到菜品列表页去更新显示，这里把数据放在全局
					if(adapter.getList()!=null && adapter.getList().size()>0){
						RestaurantFoodListActivity.recentCommentData=adapter.getList().get(0);
						//RestaurantFoodListActivity.recentCommentData.foodId=foodId;
						//RestaurantFoodListActivity.recentCommentData.totalCommentNum=0;
					}	
					
				}
				
						
				
				//设置线程安全
				isTaskSafe = true;
			}
		},
		new Runnable() {
			@Override
			public void run() {
				//设置线程安全
				isTaskSafe = true;
			}
		});
	}
	
	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		
		if (getFoodCommentListTask != null) {
			getFoodCommentListTask.cancel(true);
			adapter.setList(null, false);
			lvCommentList.setAdapter(adapter);
			//设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}
}
