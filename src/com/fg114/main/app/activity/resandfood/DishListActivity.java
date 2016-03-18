package com.fg114.main.app.activity.resandfood;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter.AdapterDto;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.DishListDTO;
import com.fg114.main.service.dto.DishListPackDTO;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 需要传入的数据：Settings.UUID ----> //两种情况：1、餐厅页或订单页(uuid为餐厅id)  2：我的菜单(uuid为菜单id)
 * 				  Settings.FROM_TAG  --> //来自哪里  1：餐厅页或订单页  2：我的菜单 	
 * 点餐菜单列表界面
 * @author nieyinyin
 *
 */
public class DishListActivity extends DishBaseActivity {
	
	private static final String TAG = "DishListActivity";
	private final Context mContext = DishListActivity.this;
	
	private DishListPackDTO dto = null;
	private boolean isUserCheck = true;
	
	// --------------------------------------- 菜品列表
	private ListView lvDish;
	private ListViewAdapter <DishData> adapter;
	
	// --------------------------------------- 左边控件
	// 左边的菜品类型ScrollView
	private ScrollView svDishCategory;
	// 左边的菜品类型的RadioGroup
	private RadioGroup rgType;
	// 用于存放菜品类型的RadioButton
	private HashMap<String, RadioButton> mapNumButton = new HashMap<String, RadioButton>();
	
	private TextView mBottomLeftText;
	private TextView mBottomCenterText;
	private Button mBottomBtn;// 我的菜单
	private RelativeLayout mBottomLayout;
	private TextView mBottomRightText; //时价X份
	
	//当没获取到数据时，显示提示
	private TextView tvEmpty;
	private LinearLayout llLoading;
	
	private int fromTag = 1;
	private String uuid = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	  //----------------------------
		OpenPageDataTracer.getInstance().enterPage("预点菜", "");
	  //----------------------------
		
		
		try {
			//获得传入参数
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				uuid = bundle.getString(Settings.UUID);
				fromTag = bundle.getInt(Settings.FROM_TAG); //来自哪里  1：餐厅页或订单页  2：我的菜单
			}
			
			SessionManager.getInstance().getDishDataList().clear();
			
			//初始化界面
			initComponent();
			
			//检查网络是否连通
	        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
	        if (!isNetAvailable) {
	        	//没有网络的场合，去提示页
	        	Bundle bund = new Bundle();
	        	bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
	        	// TODO: 跳至无网提示页 
	        	ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
	        	
	        }
	     
		} catch (Exception e) {
			
		}
		
	}
	@Override
	public void onRestart() {
		super.onRestart();
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("预点菜", "");
		//----------------------------
		
	}
	@Override
	public void finish() {
		super.finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// 需要重新刷新，因为，此时缓存中的“点菜单”可能已经变了 ，因为在购物车中，可以加菜或减菜
		SessionManager.getInstance().updateDishDataListByDishOrder();
		if (SessionManager.getInstance().getDishDataList().size() != 0) {
//			dishListAdapter.setList(SessionManager.getInstance().getDishDataList(), true);
			adapter.setList(SessionManager.getInstance().getDishDataList());
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		
		//设置Header标题栏
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getTvTitle().setText("预点菜算价格");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getBtnOption().setText("菜单");
		getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				finish();
			}
		});
		//内容部分
		LayoutInflater mInflater   = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = mInflater.inflate(R.layout.dish_list, null);
		lvDish 		= (ListView) contextView.findViewById(R.id.dish_list_lvDish);
		svDishCategory 	= (ScrollView) contextView.findViewById(R.id.dish_list_svDishCategoryLeft);
		rgType 		= (RadioGroup) contextView.findViewById(R.id.dish_list_rgType);
		tvEmpty 	= (TextView) contextView.findViewById(R.id.dish_list_tvEmpty);
		llLoading 	= (LinearLayout) contextView.findViewById(R.id.dish_list_llLoadingPrompt);
		
		// ----
		mBottomLayout = (RelativeLayout) contextView.findViewById(R.id.dishbase_main_bottomlayout);
		mBottomLeftText = (TextView) contextView.findViewById(R.id.dishbase_bottomlefttext);
		mBottomCenterText = (TextView) contextView.findViewById(R.id.dishbase__bottomcentertext);
		mBottomRightText = (TextView) contextView.findViewById(R.id.dishbase__bottomrighttext);
		mBottomBtn = (Button) contextView.findViewById(R.id.dishbase__bottombtn);
		
		//设置一些view的显隐 
		llLoading.setVisibility(View.VISIBLE);
		svDishCategory.setVisibility(View.GONE);
		getBottomLayout().setVisibility(View.GONE);
		mBottomLayout.setVisibility(View.GONE);
		lvDish.setVisibility(View.GONE);
		
		adapter = new ListViewAdapter<DishData>(R.layout.list_item_dish_list, 
				new ListViewAdapter.OnAdapterListener<DishData>(){
			@Override
			public void onLoadPage(final ListViewAdapter<DishData> adapter, int startIndex,
					int pageSize) {
				// ----------------------------------- 构造本地测试数据开始
//				String jsonContext = IOUtils.readStringFromAssets(DishListActivity.this, "dish2.txt");
//				dto = JsonUtils.fromJson(jsonContext, DishListPackDTO.class);
//				if (dto == null || dto.list == null || dto.list.size() == 0) {
//					tvEmpty.setVisibility(View.VISIBLE);
//					lvDish.setVisibility(View.GONE);
//					svDishCategory.setVisibility(View.GONE);
//					llLoading.setVisibility(View.GONE);
//					return;
//				}
//				
//				if(dto != null){
//				DishListActivity.this.runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						
//						// 将菜品列表 缓存到本地
//						initDishDataList(dto);
//
////						// 将接口返回的点菜信息覆盖本地缓存
//						cacheDishOrderDTO(dto);
//						
//						// nieyinyin add
//						llLoading.setVisibility(View.GONE);
//						svDishCategory.setVisibility(View.VISIBLE);
//						lvDish.setVisibility(View.VISIBLE);
//						
//						SessionManager.getInstance().updateDishDataListByDishOrder();
//						setTypeScrollView(dto);
//						
//						// 刷新底部
//						showBottom(SessionManager.getInstance().getDishOrder(DishListActivity.this, resId));
//						mBottomLayout.setVisibility(View.VISIBLE);
//
//					}
//				});
//			}
//				AdapterDto<DishData> adapterDto = new AdapterDto<DishData>();
//				adapterDto.setList(SessionManager.getInstance().getDishDataList());
//				adapter.onTaskSucceed(adapterDto);
				// ------------------------------------构造本地测试数据结束
				
				// -----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----
				
				ServiceRequest request = new ServiceRequest(API.getDishList);
				request.addData("fromTag",fromTag);
				request.addData("uuid",uuid);
				CommonTask.request(request, "", new CommonTask.TaskListener<DishListPackDTO>() {

					@Override
					protected void onSuccess(final DishListPackDTO dto) {
						//-----
						OpenPageDataTracer.getInstance().endEvent("页面查询");	
						//-----
						if (dto == null || dto.list == null || dto.list.size() == 0) {
							tvEmpty.setVisibility(View.VISIBLE);
							lvDish.setVisibility(View.GONE);
							svDishCategory.setVisibility(View.GONE);
							llLoading.setVisibility(View.GONE);
							return;
						}
						
						if(dto != null){
							DishListActivity.this.runOnUiThread(new Runnable() {
//
								@Override
								public void run() {
									
									// 将菜品列表 缓存到本地
									initDishDataList(dto);

//									// 将接口返回的点菜信息覆盖本地缓存
									cacheDishOrderDTO(dto);
									
									// nieyinyin add
									llLoading.setVisibility(View.GONE);
									svDishCategory.setVisibility(View.VISIBLE);
									lvDish.setVisibility(View.VISIBLE);
									
									SessionManager.getInstance().updateDishDataListByDishOrder();
									setTypeScrollView(dto);
									
									// 刷新底部
									showBottom(SessionManager.getInstance().getDishOrder(DishListActivity.this, resId));
									mBottomLayout.setVisibility(View.VISIBLE);

								}
							});
						}
						
						//
						AdapterDto<DishData> adapterDto = new AdapterDto<DishData>();
						adapterDto.setList(SessionManager.getInstance().getDishDataList());
						adapter.onTaskSucceed(adapterDto);
					};
				
					@Override
					protected void onError(int code, String message){
						//-----
						OpenPageDataTracer.getInstance().endEvent("页面查询");	
						//-----
						tvEmpty.setVisibility(View.VISIBLE);
						lvDish.setVisibility(View.GONE);
						svDishCategory.setVisibility(View.GONE);
						llLoading.setVisibility(View.GONE);
					}
				});
			}
			@Override
			public void onRenderItem(final ListViewAdapter<DishData> adapter,ViewHolder holder, final DishData data) {
				MyImageView myIv = holder.$myIv(R.id.dish_list_item_ivDishPic);
				 holder.$(R.id.dish_list_item_rlMain).setVisibility(View.VISIBLE);
				 holder.$(R.id.dish_list_item_msgLayout).setVisibility(View.GONE);
				 if(TextUtils.isEmpty(data.getPicUrl())){
						myIv.setVisibility(View.INVISIBLE);
				 }else{
					 myIv.setVisibility(View.VISIBLE);
					 myIv.setImageByUrl(data.getPicUrl(), false, 0, ScaleType.CENTER_CROP);
				 }
				 // 菜品显示大图
				 if(!TextUtils.isEmpty(data.getBigPicUrl())){
					 myIv.setOnClickListener(new OnClickListener() {
							
						 @Override
						 public void onClick(View view) {
							 ViewUtils.preventViewMultipleClick(view, 1000);
							 DialogUtil.createImageViewPanel((Activity)mContext, (View)view.getParent(), data.getBigPicUrl());
						 }
					 });
				 }	else{
					 myIv.setClickable(false);
				 }
				 
				 holder.$tv(R.id.dish_list_item_tvName).setText(data.getName());
				 if (data.isCurrentPriceTag()) {
					 holder.$tv(R.id.dish_list_item_tvPrice).setText("时价");
					}else {
						holder.$tv(R.id.dish_list_item_tvPrice).setText("￥"+data.getPrice());
					}
				// 设置是否此分类的第一项
				 TextView tvTitle = holder.$tv(R.id.dish_list_item_tvTitle);
					if (data.isFirstInList()) {
						tvTitle.setVisibility(View.VISIBLE);
						tvTitle.setText(data.getTypeName());
					}
					else {
						tvTitle.setVisibility(View.GONE);
					}
				 DigitalSelector dg = (DigitalSelector)holder.$(R.id.dish_list_item_amount);
				 dg.setDefaultValue(0);
				 dg.setMinValue(0);
				 dg.setMaxValue(99);
				 dg.setMaxWarning("已到最大值");
				 dg.setMinWarning("");
				 if (data.getNum() > 0) {
					 dg.setDigitalValue(data.getNum());
				 }else {
					 dg.setDigitalValue(0);
				 }
//				 dg.setBackgroundResource(R.drawable.zy_bg_digital1);
				 dg.setOnDigitChangeListener(new DigitalSelector.OnDigitChangeListener() {
				 	
				 	@Override
				 	public void onChange(DigitalSelector selector, int digit, int previousValue) {
				 		
				 		data.setNum(digit);
				 		
				 		//遍历列表更新点菜数据，因为同一菜品可能出现在两个分类里
				 		updateList(data);
				 		
				 		DishOrderDTO dishOrder = SessionManager.getInstance().getDishOrder(DishListActivity.this, resId);
				 		dishOrder.updateDishOrder(data);
				 		SessionManager.getInstance().setDishOrder(DishListActivity.this, dishOrder, resId);
				 		showBottom(dishOrder);
				 		getList(dishOrder);
				 		
				 		adapter.notifyDataSetChanged();
				 	}
				 });
			}
		});
		adapter.setmCtx(mContext);
		View footerView = mInflater.inflate(R.layout.common_foot_view, null);
		/*
		 * 增加footView,作用是：当ListView中有很少的item时，也能让其滑动，从而让
		 *  左边的 "菜品类型"  能够达到联动效果
		 */
		lvDish.addFooterView(footerView);
		adapter.setExistPage(false);
		adapter.setListView(lvDish);

		lvDish.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
//					dishListAdapter.syncImageLoader.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					//-----
					OpenPageDataTracer.getInstance().addEvent("滚动");	
					//-----
//					dishListAdapter.loadImage();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//					dishListAdapter.syncImageLoader.lock();
					break;

				default:
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				try {
					List<DishData> list = SessionManager.getInstance().getDishDataList();
					if (list.size() < 1) {
						return;
					}
					DishData data = list.get(firstVisibleItem);
					RadioButton btnTypeChecked = (RadioButton) rgType.findViewById(rgType.getCheckedRadioButtonId());
					Log.e("btnTypeChecked", btnTypeChecked+"");
					if (btnTypeChecked == null) {
						return;
					}
					DishListDTO dto = (DishListDTO) btnTypeChecked.getTag();
					Log.e("dto", dto+"");
					if (dto != null) {
						if (!getSpecialType(dto.typeDTO).equals(data.getGroupId())) {
							RadioButton btnType = mapNumButton.get(data.getGroupId());
							if (btnType != null) {
								// 控制垂直SrcollView的自动滑动
								// top to down || down to top
								int btnTypeY = btnType.getTop();
								int scrollY = svDishCategory.getScrollY();
								int dy = btnTypeY - scrollY;
								if(btnTypeY < scrollY)
								{
									svDishCategory.smoothScrollBy(0, dy);
								}
								else
								{
									int yDistance = btnType.getBottom() - scrollY;
									if(yDistance > svDishCategory.getHeight())
									{
										svDishCategory.smoothScrollBy(0, yDistance - svDishCategory.getHeight());
									}
								}
								
								isUserCheck = false;
								btnType.setChecked(true);
								Log.e("onScroll", "isUserCheck-->"+isUserCheck);
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		rgType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			int lastCheckedId=-1;
			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				try {
					if (checkId == -1) {
						return;
					}
					Log.e("isUserCheck", "isUserCheck-->"+isUserCheck);
					if (!isUserCheck) {
						isUserCheck = true;
						return;
					}
					
					RadioButton btnType = (RadioButton) rgType.findViewById(checkId);
					DishListDTO dto = (DishListDTO) btnType.getTag();
					if (dto != null) {
						lvDish.setSelection(dto.startIndex);
//						dishListAdapter.loadImage();
					}
				}
				catch(Exception e) {
					log(TAG, e);
				}
			}
		});
		
		mBottomBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//-----
				OpenPageDataTracer.getInstance().addEvent("已点菜单按钮");	
				//-----
				
				DishOrderDTO dto = SessionManager.getInstance().getDishOrder( mContext, resId );
				if (dto.getDishDataList() == null
						|| dto.getDishDataList().size() == 0) {
					DialogUtil.showToast(mContext, "请先选择您要点的菜");
					return;
				}
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID, resId);
				bundle.putInt("fromTag", fromTag); 	
				bundle.putString(Settings.UUID, uuid);
				ActivityUtil.jump(mContext, DishOrderActivity.class, 0, bundle);
			
			}
		});
		this.getDishBaseLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
	
	/**
	 * 获得餐厅菜品列表
	 */
//	private void executeGetDishListTask() {
//		//-----
//				OpenPageDataTracer.getInstance().addEvent("页面查询");	
//		//-----
//		final ZyGetDishListTask task = new ZyGetDishListTask( mContext, uuid, fromTag);
//		task.execute(new Runnable() {
//			
//			@Override
//			public void run() {
//				//-----
//				OpenPageDataTracer.getInstance().endEvent("页面查询");	
//				//-----
//				dto = task.dto;
//				if (dto == null || dto.list == null || dto.list.size() == 0) {
//					tvEmpty.setVisibility(View.VISIBLE);
//					lvDish.setVisibility(View.GONE);
//					svDishCategory.setVisibility(View.GONE);
//					llLoading.setVisibility(View.GONE);
//					return;
//				}
//				
//				if(dto != null){
//					DishListActivity.this.runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							
//							// 将菜品列表 缓存到本地
//							initDishDataList(dto);
//
////							// 将接口返回的点菜信息覆盖本地缓存
//							cacheDishOrderDTO(dto);
//							
//							// nieyinyin add
//							llLoading.setVisibility(View.GONE);
//							svDishCategory.setVisibility(View.VISIBLE);
//							lvDish.setVisibility(View.VISIBLE);
//							
//							SessionManager.getInstance().updateDishDataListByDishOrder();
//							if (SessionManager.getInstance().getDishDataList().size() != 0) {
////								dishListAdapter.setList(SessionManager.getInstance().getDishDataList(), true);
//							}
//							
//							setTypeScrollView(dto);
//							
//							// 刷新底部
//							showBottom(SessionManager.getInstance().getDishOrder(DishListActivity.this, resId));
//							mBottomLayout.setVisibility(View.VISIBLE);
//
//						}
//					});
//				}
//			}
//		}, new Runnable() {
//			
//			@Override
//			public void run() {
//				//-----
//				OpenPageDataTracer.getInstance().endEvent("页面查询");	
//				//-----
//				tvEmpty.setVisibility(View.VISIBLE);
//				lvDish.setVisibility(View.GONE);
//				svDishCategory.setVisibility(View.GONE);
//				llLoading.setVisibility(View.GONE);
//			}
//		});
//	}

	/**
	 * 更新本地缓存菜品列表信息
	 * @param dto
	 */
	private void initDishDataList(DishListPackDTO dto) {
		int index = 0;
		
		// 清空缓存列表
		List<DishData> list = SessionManager.getInstance().getDishDataList();
		list.clear();
		
		for (DishListDTO dld : dto.list) {
			dld.startIndex = index;
			dld.endIndex = index + dld.list.size();
			dld.list.get(0).setFirstInList(true);
//			dld.getList().get(0).setTypeName(dld.getTypeDTO().getName());
			
			/*//使用菜品类别的groupID属性来控制顶部按钮的同步显示*/			
			
			//给每一道菜设置类别，例如：特色菜。			
			for (DishData d : dld.list) 
			{
				d.setGroupId( getSpecialType(dld.typeDTO) ); 
				d.setTypeName(dld.typeDTO.getName());
				d.setTypeId(d.getGroupId());
				d.setNum(d.getSelectedNum());
				d.setFirstInCart(false);
			}

			list.addAll(dld.list);
			index += dld.list.size();
		}
		
		//更新本地缓存点菜列表数据
		SessionManager.getInstance().setDishDataList(list);
	}
	
	/**
	 * 根据不同情况返回一个类型ID
	 * @param type
	 * @return
	 */
	private String getSpecialType(CommonTypeDTO type){

		if(type == null 
				|| type.getUuid() == null 
				|| type.getUuid().equals("")){
			return "N/A"; //构造一个虚拟id
		}else{
			return type.getUuid();
		}
	}
	
	/**
	 * 构造左边SrcollView控件的RadioButton
	 * 并将每一个RadioButton存进HashMap
	 * @param dto
	 */
	private void setTypeScrollView(DishListPackDTO dto) {
		if (dto.list == null || dto.list.size() == 0) {
			rgType.setVisibility(View.GONE);
			return;
		}
		rgType.setVisibility(View.VISIBLE);
		rgType.removeAllViews();
		mapNumButton.clear();
		for (DishListDTO list: dto.list) {
			CommonTypeDTO ctdType = list.typeDTO;
			RadioButton btnType = createNumButton(ctdType.getName(), list);			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
			lp.setMargins(0, 0, 0, 0);
			rgType.addView(btnType, lp);
			rgType.setPadding(0, 0, 0, 0);
			mapNumButton.put(getSpecialType(ctdType), btnType);
		}
		isUserCheck = false;
		((RadioButton) rgType.getChildAt(0)).setChecked(true);
	}
	
	/**
	 * 创建菜品类型RadioButton
	 * @param text
	 * @param dto
	 * @return
	 */
	private RadioButton createNumButton(String text, DishListDTO dto) {
		RadioButton rbType=(RadioButton)View.inflate(this, R.layout.radio_button_dish_list, null);
		Bitmap bmp = null;
		rbType.setButtonDrawable(new BitmapDrawable(bmp));
		rbType.setText(text);
		rbType.setSingleLine(true);
		rbType.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		rbType.setTextColor(getResources().getColor(R.color.text_color_black));
		rbType.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 150);  
		lp.setMargins(0, 0, 0, 0);
		rbType.setLayoutParams(lp);
		rbType.setPadding(0, 0, 0, 0);
		rbType.setTag(dto);
		
		return rbType;
	}
	
	protected void showBottom(DishOrderDTO dishOrder) {
		List<DishData> dishDatas = new ArrayList<DishData>();

		if (dishOrder.getDishDataList().size() > 0) {
			dishDatas = dishOrder.getDishDataList();
		} 

		double totle = 0;// 总共的价格
		int totalNum = 0;// 总共的数量
		boolean isCurrentPrice = false;

		DecimalFormat format = new DecimalFormat("######0.00");

		for (DishData data : dishDatas) {
			int num = data.getNum()+data.getOldNum();// 该菜的份数
			double single = data.getPrice() * num;
			totalNum += num;

			if (data.isCurrentPriceTag()) {// 是否是时价：是！
				isCurrentPrice = true;
			} else {
				totle += single;
			}
		}

		mBottomLeftText.setText(totalNum + "份 ");
		mBottomCenterText.setText("￥"+format.format(totle));
		if (isCurrentPrice) {
			if (!mBottomRightText.isShown())
				mBottomRightText.setVisibility(View.VISIBLE);
			mBottomRightText.setText(" + 时价 ");
		} else {
			if (mBottomRightText.isShown())
				mBottomRightText.setVisibility(View.INVISIBLE);
		}
	}
	
	
	// 将接口返回的 “点菜单” 缓存到本地
	void cacheDishOrderDTO(DishListPackDTO dto){
		DishOrderDTO cacheDishData = SessionManager.getInstance().getDishOrder(mContext, resId);
//		cacheDishData.clearAll();
		cacheDishData.reset();
		if(dto != null && dto.list != null && dto.list.size() > 0){
			List<DishListDTO> dishListDTOs = dto.list;
			for (DishListDTO dishListDTO : dishListDTOs) {
				List<DishData> dishDatas = dishListDTO.list;
				if(dishDatas != null && dishDatas.size() > 0){
					for (DishData dishData : dishDatas) {
						if(dishData.getSelectedNum() > 0){
							cacheDishData.getDishMap().put(dishData.getUuid(), dishData);
							cacheDishData.updateDishOrder(dishData);
						}
					}
				}
			}
			
			SessionManager.getInstance().setDishOrder(mContext, cacheDishData, resId);
		}
	}
	
	/**
	 * 每道菜品可能属于不同的分类
	 * ---> 遍历所有菜品，更新所有分类中与result所代表的菜品数据
	 * @param result
	 */
	private void updateList(DishData result) {
		List<DishData> list = adapter.getmList();
		for (int i = 0; i < list.size(); i++) {
			if (result != null && result.getUuid() != null
					&& result.getUuid().equals(list.get(i).getUuid())) {
				list.set(i, result);

				// 更新分类里面的View，暂时没有实现
//				ViewHolder holder = new ViewHolder();
//				holder.position = i;
//				View view = lvDish.findViewWithTag(holder);
//				if (view != null) {
//					DigitalSelector dishAmountView =  (DigitalSelector) view.findViewById(R.id.dish_list_item_amount);
//					if (result.getNum() > 0) {
//						dishAmountView.setDigitalValue(result.getNum());
//					} else {
//						dishAmountView.setDigitalValue(0);
//					}
//				}
			}
		}
	}
}
