package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;

import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;


import com.fg114.main.app.view.WaterFallScrollView;
import com.fg114.main.app.view.WaterFallBaseView.onWaterFallClickListener;
import com.fg114.main.app.view.WaterFallScrollView.WaterFallOption;

import com.fg114.main.service.dto.PgInfo;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestPicListDTO;

import com.fg114.main.service.dto.ResPicListDTO;
import com.fg114.main.service.task.ErrorLogTask;
import com.fg114.main.service.task.GetNewResPicTask;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class RestaurantPicActivity extends MainFrameActivity implements WaterFallScrollView.OnScrollListener
{
	private static final String TAG = "RestaurantPicActivity";
	// 传入参数
	private String restaurantId; // 餐厅ID
	public static int THREE_COLUMN=3;//
	public static int TWO_COLUMN=2;
	private RestInfoData restaurantInfo;
	// 画面变量
	private boolean isTaskSafe = true;
	private int picViewTag = Settings.STATUTE_IMAGE_EVN;
	private boolean isFirst = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int pageNo = 1;
	private int mFlag=1;  //显示3列图片或2列内容标志位 

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private RadioGroup rgGroup;
	private RadioButton rbAll;
	private RadioButton rbEvn;
	private RadioButton rbFood;
	private RadioButton rbOther;
	private RadioButton rbUserUpload;
	private WaterFallScrollView waterFall_Scroll;
	private LinearLayout waterFall_Container;
	private ViewGroup uploadPic;
	private LinearLayout mLinearInfo;
	private TextView mFirstTv;
	private TextView mSecondTv;
	private Button mUpLoadPicBtn;
	private Display display;
	private int ScreenWidth;
    private ViewGroup loadingInfo;
	private GetNewResPicTask getNewResPicTask;

	private PgInfo pgInfo;
	private volatile int startIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅图片列表", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		picViewTag = bundle.getInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_EVN);
		display = getWindowManager().getDefaultDisplay();
		ScreenWidth = display.getWidth(); 
		// 获得缓存的餐厅信息
		restaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);
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
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅图片列表", "");
		//----------------------------
	}
	
	private void initWaterFallLayout(View ContentView)
	{

		// 1 初始化waterfall
		waterFall_Scroll = (WaterFallScrollView) ContentView.findViewById(R.id.new_image_waterFall_scrollview);
		waterFall_Scroll.setOnWaterFallClickListener(onwaterFallListener);
		// 2 初始化显示容器
		waterFall_Container = (LinearLayout) ContentView.findViewById(R.id.new_image_waterfall_container);
		// 3,设置滚动监听
		waterFall_Scroll.setOnScrollListener(this);
		WaterFallOption fallOption = new WaterFallOption(waterFall_Container, ScreenWidth, TWO_COLUMN);
		// 5,提交更改,实现android瀑布流
		waterFall_Scroll.commitWaterFall(fallOption, waterFall_Scroll);
			
	}

	public void resetData(int mflag)
	{
		if(mflag==Settings.IMAGE_FOOD_STYLE)
		{
			
			waterFall_Scroll.resetScrollData();
			WaterFallOption fallOption = new WaterFallOption(waterFall_Container, ScreenWidth, TWO_COLUMN);
			// 5,提交更改,实现android瀑布流
			waterFall_Scroll.commitWaterFall(fallOption, waterFall_Scroll);
		}
		else
		{
			
			waterFall_Scroll.resetScrollData();
			WaterFallOption fallOption = new WaterFallOption(waterFall_Container, ScreenWidth, THREE_COLUMN);
			// 5,提交更改,实现android瀑布流
			waterFall_Scroll.commitWaterFall(fallOption, waterFall_Scroll);
		}
		waterFall_Scroll.setVisibility(View.VISIBLE);
		mLinearInfo.setVisibility(View.GONE);
	}
	
	private void initComponent()
	{

		// 设置标题栏
		this.getTvTitle().setText(R.string.text_title_restaurant_image);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
//		this.getBtnOption().setText("有奖传图");
		this.getBtnOption().setText("有奖传图");
		this.setFunctionLayoutGone();

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.res_pic_flow, null);
		rgGroup = (RadioGroup) contextView.findViewById(R.id.new_image_list_group);
		uploadPic=(ViewGroup) contextView.findViewById(R.id.flow_content_upload_pic);
		loadingInfo=(ViewGroup) contextView.findViewById(R.id.res_pic_loading);
		mLinearInfo=(LinearLayout) contextView.findViewById(R.id.flow_content_info_layout);
		mFirstTv=(TextView) contextView.findViewById(R.id.flow_content_infoTv);
		mSecondTv=(TextView) contextView.findViewById(R.id.flow_content_infoTv_bottom);
		mUpLoadPicBtn=(Button) contextView.findViewById(R.id.flow_content_info_btn);
		
		// 初始化瀑布流容器
	    initWaterFallLayout(contextView);
	    
		rgGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(RadioGroup group, int checkId)
			{
				// -----
				OpenPageDataTracer.getInstance().addEvent("类别切换按钮");
				// -----
				if (checkId == rbUserUpload.getId()) {
					// 查看全部的场合
					rbUserUpload.setTextColor(getResources().getColor(R.color.text_color_white));
					rbEvn.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbFood.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbOther.setTextColor(getResources().getColor(R.color.text_color_gray));
					
					picViewTag = Settings.STATUTE_IMAGE_UPLOAD;
					mFlag=Settings.IMAGE_OTHER_REST; //标志位 其他图片区
				} else if (checkId == rbEvn.getId()) {
					// 环境图片的场合
					rbUserUpload.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbEvn.setTextColor(getResources().getColor(R.color.text_color_white));
					rbFood.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbOther.setTextColor(getResources().getColor(R.color.text_color_gray));
					
					picViewTag = Settings.STATUTE_IMAGE_EVN;
					mFlag=Settings.IMAGE_OTHER_REST;
				} else if (checkId == rbFood.getId()) {
					// 菜式图片的场合
					rbUserUpload.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbEvn.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbFood.setTextColor(getResources().getColor(R.color.text_color_white));
					rbOther.setTextColor(getResources().getColor(R.color.text_color_gray));
					
					picViewTag = Settings.STATUTE_IMAGE_FOOD;
					mFlag=Settings.IMAGE_FOOD_STYLE;  //标志位  菜式区
				} else if (checkId == rbOther.getId()) {
					// 其他图片的场合
					rbUserUpload.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbEvn.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbFood.setTextColor(getResources().getColor(R.color.text_color_gray));
					rbOther.setTextColor(getResources().getColor(R.color.text_color_white));
					
					picViewTag = Settings.STATUTE_IMAGE_OTHER;
					mFlag=Settings.IMAGE_OTHER_REST;
				} 
				pageNo = 1;
				isFirst = true;
				isLast = true;
				startIndex = 1;
				pgInfo = null;
				resetData(mFlag);
				executeGetResPicList(mFlag);
				
			}
		});

		rbUserUpload = (RadioButton) contextView.findViewById(R.id.new_image_list_userUpload);
		rbEvn = (RadioButton) contextView.findViewById(R.id.new_image_list_evn);
		rbFood = (RadioButton) contextView.findViewById(R.id.new_image_list_food);
		rbOther = (RadioButton) contextView.findViewById(R.id.new_image_list_other);
		
		if (picViewTag == Settings.STATUTE_IMAGE_UPLOAD) {
			rbUserUpload.performClick();
		} else if (picViewTag == Settings.STATUTE_IMAGE_EVN) {
			rbEvn.performClick();
		} else if (picViewTag == Settings.STATUTE_IMAGE_FOOD) {
			rbFood.performClick();
		} else if (picViewTag == Settings.STATUTE_IMAGE_OTHER) {
			rbOther.performClick();
		} 
		
		this.getBtnOption().setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 1000);
//				jumpToDishList();
				mUpLoadPicBtn.performClick();
//				Bundle bundle = new Bundle();
//				bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.RESTAURANT_PIC_ACTIVITY);
//				bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
//				
//				List<FoodSubListForSelectData> list = SessionManager.getInstance().getSelectedFood(restaurantId);
//				if (list != null && list.size() > 0) {
//					StringBuffer sbIds = new StringBuffer();
//					for (FoodSubListForSelectData subData : list) {
//						sbIds.append(subData.getUuid()).append(",");
//					}
//					sbIds.deleteCharAt(sbIds.length() - 1);
//					bundle.putString(Settings.BUNDLE_SELECTED_IDS, sbIds.toString());
//				}
//				ActivityUtil.jump(RestaurantPicActivity.this, FoodListForSelectActivity.class, Settings.RESTAURANT_PIC_ACTIVITY, bundle);
			}
		});

		//提示信息上传图片
		mUpLoadPicBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("有奖传图按钮");
				// -----
				
				try {
					takePic(new OnShowUploadImageListener() {
						
						@Override
						public void onGetPic(Bundle bundle) {
							
							if(picViewTag==Settings.STATUTE_IMAGE_FOOD)
							{
								onFinishTakePic(Settings.UPLOAD_TYPE_FOOD,restaurantId,restaurantInfo.name);
							}
							else
							{
								onFinishTakePic(Settings.UPLOAD_TYPE_RESTAURANT,restaurantId,restaurantInfo.name);
							}
						}
					},false);
				} catch (Exception ex) {
					Log.e("MainFrameActivity[option button]: Error report",
							ex.getMessage(), ex);
				}
				
				
			}
		});
		/*this.getBtnOption().setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.RESTAURANT_PIC_ACTIVITY);
				bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, getString(R.string.text_button_back));
				bundle.putString(Settings.BUNDLE_KEY_TITLE, restaurantInfo.getName());
				String[] idArray = { restaurantId, "", restaurantInfo.getName()};
				bundle.putStringArray(Settings.BUNDLE_KEY_ID, idArray);
				ActivityUtil.jump(RestaurantPicActivity.this, RestaurantFoodListActivity.class, Settings.RESTAURANT_PIC_ACTIVITY, bundle);
				
			}
		});*/
		
		uploadPic.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				mUpLoadPicBtn.performClick();
			}
		});
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
	//内容区每个item点击事件
	private onWaterFallClickListener<RestPicData> onwaterFallListener = new onWaterFallClickListener<RestPicData>()
	{
				
				@Override
				public void onItemClick(RestPicData dto)
				{
					
					
					// -----
					OpenPageDataTracer.getInstance().addEvent("缩略图");
					// -----
					
					if(mFlag==Settings.IMAGE_FOOD_STYLE)
					{
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_FOOD_ID, dto.getUuid());
						bundle.putString(Settings.BUNDLE_FOOD_NAME, dto.getName());
						ActivityUtil.jump(RestaurantPicActivity.this,RestaurantImageDetailOfFoodActivity.class,
								0, bundle);
					}
					else
					{
						ArrayList<RestPicData> mlist=(ArrayList<RestPicData>) waterFall_Scroll.getTotalDataList();
						int position=0;
						for(int i=0; i<mlist.size();i++)
						{
							if(dto.getPicUrl().equals(mlist.get(i).getPicUrl()))
							{
								position=i;
							}
						}
						Bundle bundle = new Bundle();
	            		bundle.putInt(Settings.BUNDLE_KEY_ID, position);
	            		bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, mlist);
	            		ActivityUtil.jump(RestaurantPicActivity.this, 
	            				RestaurantGalleryActivity.class, 
	            				0, bundle);
					}
					
					
				}
			};   
  
	@Override
	public void finish()
	{
		try {
			// recycle();
			super.finish();
		} catch (Exception e) {
			if (DEBUG) {
				DialogUtil.showToast(this, "error:" + e.getMessage());
			} else {
				ErrorLogTask t = new ErrorLogTask(null, this.getBaseContext(), e.getMessage(), e.getStackTrace().toString());
				t.execute();
			}
		}
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		
		// 为了上传图片结束后，跳转页面服务------------------nieyinyin add 
		Settings.uploadPictureOrignalActivityClazz = RestaurantPicActivity.class;
	}

	private  void executeGetResPicList(final int flag)
	{
		
		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				pageNo = pageNo + 1;  /** 这里的pageNo暂时不能去，因为控件WaterFallScrollView中需要用到这个字段*/
			}
			// 设置线程不安全
		
			setChildEnable(false);
			this.isTaskSafe = false;
			
		} else {
			return;
		}

		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		loadingInfo.setVisibility(View.VISIBLE);
		// 创建任务
		getNewResPicTask = new GetNewResPicTask(null, this, restaurantId, picViewTag, startIndex);
		getNewResPicTask.setCanCancel(true);
		getNewResPicTask.setCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----

				try {
					if (getNewResPicTask != null) {
						getNewResPicTask.cancel(true);
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// 执行任务
		getNewResPicTask.execute(new Runnable()
		{

			@Override
			public void run()
			{

				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				
				RestPicListDTO dto = getNewResPicTask.dto;
				if (dto != null) {
					isLast = dto.pgInfo.lastTag;
					pgInfo = dto.pgInfo;
					startIndex = pgInfo.nextStartIndex;
 
					waterFall_Scroll.addList(dto.getList());

					//Log.e("bug", "TotalNum"+dto.getPgInfo().getTotalNum()+"listsize:"+dto.getList().size());
					// 第一次加载
					waterFall_Scroll.AddItemToContainer(pageNo-1, dto.getList().size(),flag);
					
					// 内存回收
					System.gc();
					// 关闭进度提示
					loadingInfo.setVisibility(View.GONE);
					//表示在第一页的情况下没有数据显示提示框
						
						if(dto.getList().size()==0)
						{
							waterFall_Scroll.setVisibility(View.GONE);
							mLinearInfo.setVisibility(View.VISIBLE);
							setInfoMessage();//设置不同情况下 显示的相关信息
							
						}
				}
				// 设置线程安全
				isTaskSafe = true;
				setChildEnable(true);
			}
		}, new Runnable()
		{
			@Override
			public void run()
			{
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				
				// 设置线程安全
				isTaskSafe = true;
				setChildEnable(true);
			}
		});
	}
	public List<RestPicData> initTestData(ResPicListDTO respicdto)
	{
		List<RestPicData> testList = new ArrayList<RestPicData>();
		Random ran = new Random();
		for (int i = 0; i < respicdto.getList().size(); i++) {
			RestPicData dto = new RestPicData();
			dto.setName("芙蓉香酥粥");
			dto.setSmallPicHeight(ran.nextInt(50)+80);
			dto.setHotNum(i);
			dto.setPicUrl(respicdto.getList().get(i).getPicUrl());
			dto.setSmallPicUrl(respicdto.getList().get(i).getSmallPicUrl());
			dto.setSmallPicWidth(100);
			dto.setCommentDetail("西班牙街头的特色美食,其实就是斗牛士");
			dto.setCommentUserName("莫言");
			dto.setCircleTag(respicdto.getList().get(i).isSurround());
			testList.add(dto);
		}
		return testList;
	}

	@Override
	public void onBottom()
	{
		if(!isLast)
		{
		  executeGetResPicList(mFlag);
		}
		
	}

	public void setChildEnable(boolean canChange)
	{
		rbUserUpload.setClickable(canChange);
		rbEvn.setClickable(canChange);
		rbFood.setClickable(canChange);
		rbOther.setClickable(canChange);
	}
	
	public void setInfoMessage()
	{
		if(picViewTag==Settings.STATUTE_IMAGE_FOOD)
		{
			String first="该餐厅暂时没有菜品数据";
			String second="成为全国第1位添加菜的顾客吧~";
			String btnString="添加菜品";
			mFirstTv.setText(first);
			mSecondTv.setText(second);
			mUpLoadPicBtn.setText(btnString);
		}
		else
		{
			String[] info=new String[]{"环境","", "会员上传","其他"};
			String first="该餐厅暂无"+info[picViewTag-1]+"图片信息";
			String second="立即上传图片获秘币奖励";
			String btnString="上传图片";
			mFirstTv.setText(first);
			mSecondTv.setText(second);
			mUpLoadPicBtn.setText(btnString);
		}
	}
	

	private void jumpToDishList() {
//		DishOrderDTO dishOrder = SessionManager.getInstance().getDishOrder(this, restaurantId);
//		if (dishOrder.getTimeStamp() + Settings.DISH_EXPIRED_TIME < System.currentTimeMillis()) {
//			dishOrder.clearAll();
//			SessionManager.getInstance().setDishOrder(this, dishOrder);
//		}
//		dishOrder.setRestId(restaurantId);
//		SessionManager.getInstance().setDishOrder(this, dishOrder);

//		Bundle bundle = new Bundle();
//		if (CheckUtil.isEmpty(dishOrder.getTableId())) {
//			bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
//			bundle.putString(Settings.BUNDLE_TABLE_ID, "");
//			bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
//		} else {
//			bundle.putString(Settings.BUNDLE_REST_ID, dishOrder.getRestId());
//			bundle.putString(Settings.BUNDLE_TABLE_ID, dishOrder.getTableId());
//			bundle.putString(Settings.BUNDLE_KEY_ID, dishOrder.getRestId());
//		}
//		bundle.putInt(Settings.BUNDLE_DISH_SRC_PAGE, 0);
//		bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
//		ActivityUtil.jump(this, DishListActivity.class, 0, bundle);
		
		Bundle bundle = new Bundle();
		bundle.putString(Settings.UUID, restaurantId);
		bundle.putInt(Settings.FROM_TAG, 1);
		ActivityUtil.jump(this, DishListActivity.class, 0, bundle);
	}

	@Override
	public void onTop() {
		
	}

	@Override
	public void onScroll() {
		
	}

	@Override
	public void onAutoScroll(int l, int t, int oldl, int oldt) {
		
	}
}
