package com.fg114.main.app.activity.resandfood;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.adapter.common.ListViewAdapter.OnAdapterListener;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/*
 * 点菜购物车
 * 
 * @author nieyinyin
 * 
 */
public class DishOrderActivity extends DishBaseActivity {
	private TextView mTopLeftText; // 顶部左侧标题
	private Button mBottomBtn;// 确认下单
	private ListView mListView;// 菜单确认列表
	private Context mContext;
	private Button mLeftTitleBtn;
	private Button mRightTitleBtn;

	private int postTag; //提交类型   1:添加(餐馆详细页进来)   2:更新  
	private String uuid = ""; //uuid  postTag=1: restId   2:dishOrderId;  
	private DishOrderDTO dishOrder;
	private String dishOrderId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("确认选菜", "");
		//----------------------------
		
		mContext = this;
		
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			postTag = bundle.getInt(Settings.FROM_TAG);
			uuid = bundle.getString(Settings.UUID);
			resId = bundle.getString(Settings.BUNDLE_REST_ID);
		}
		
		// 初始化界面
		initComponent();

//		// 检查网络是否连通
//		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable( this.getApplicationContext() );
//		if (!isNetAvailable) {
//			// 没有网络的场合，去提示页
//			Bundle bund = new Bundle();
//			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
//			ActivityUtil.jump(this, ShowErrorActivity.class,0, bund);
//		}
		
	}
	@Override
	public void onRestart() {
		super.onRestart();
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("确认选菜", "");
		//----------------------------
		
	}
	@Override
	protected void onResume() {
		if(!TextUtils.isEmpty(dishOrder.getOrderId())){
			uuid = dishOrderId = dishOrder.getOrderId();
			postTag = 2;
		}
		super.onResume();
	}
	/**
	 * 初始化
	 */
	private void initComponent() {

		dishOrder = SessionManager.getInstance().getDishOrder(mContext, resId);
		
		// 设置标题栏
		this.getTvTitle().setText("购物车");
		
		// ----- 隐藏基类底部的Layout,使用自有的底部Layout
 		this.getBottomLayout().setVisibility(View.VISIBLE);

		mLeftTitleBtn = this.getBtnGoBack();
		mLeftTitleBtn.setText(R.string.text_button_dish_goondish);
		mLeftTitleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				
				finish();
			}
		});
		
		mRightTitleBtn = this.getBtnOption();
		mRightTitleBtn.setText(R.string.text_button_clear_error_info);

		// 内容部分
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = inflater.inflate(R.layout.zy_restaurant_dishorder, null);
		mTopLeftText = (TextView) contextView.findViewById(R.id.restaurant_dishorder_toplefttext);
		mListView = (ListView) contextView.findViewById(R.id.restaurant_dishorder_itemlist);
		
		if (TextUtils.isEmpty(dishOrder.getTableId())) {
			mTopLeftText.setText(R.string.text_layout_dish_unselecttable);
		} else
			mTopLeftText.setText(dishOrder.getTableId());

		final ListViewAdapter<DishData> adapter = new ListViewAdapter<DishData>(R.layout.zy_list_item_dishorder,new OnAdapterListener<DishData>() {
			@Override
			public void onLoadPage(ListViewAdapter<DishData> adapter,
					int startIndex, int pageSize) {
//				AdapterDto<DishData> dto = new AdapterDto<DishData>();
//				dto.setList(getList(dishOrder));
//				adapter.onTaskSucceed(dto);
			}
			
			@Override
			public void onRenderItem(final ListViewAdapter<DishData> adapter,ViewHolder holder, final DishData data) {
				holder.$tv(R.id.dishorder_list_item_foodname).setText(data.getName());
				if (data.isCurrentPriceTag()) {
					holder.$tv(R.id.dishorder_list_item_dishprice).setText("时价");
				} else {
					holder.$tv(R.id.dishorder_list_item_dishprice).setText("￥"+data.getPrice());
				}
				//根据当前的item是否为第一个控制title的显影 
				if (data.isFirstInCart()) {
					holder.$tv(R.id.dishorder_list_item_tvTitle).setVisibility(View.VISIBLE);
					holder.$tv(R.id.dishorder_list_item_tvTitle).setText(data.getTypeName());
				} else {
					holder.$tv(R.id.dishorder_list_item_tvTitle).setVisibility(View.GONE);
				}
//				holder.$tv(R.id.dishorder_list_item_foodnum);
//				holder.$tv(R.id.dishorder_list_item_foodadd);
//				holder.$tv(R.id.dishorder_list_item_dishtype);
				DigitalSelector dg = (DigitalSelector)holder.$(R.id.dishorder_list_item_amountOperation);
				dg.setMinValue(0);
				dg.setMaxValue(99);
				dg.setMaxWarning("已到最大值");
				dg.setMinWarning("");
				dg.setDigitalValue(data.getNum());
//				dg.setBackgroundResource(R.drawable.zy_bg_digital1);
				dg.setOnDigitChangeListener(new DigitalSelector.OnDigitChangeListener() {
					
					@Override
					public void onChange(DigitalSelector selector, int digit, int previousValue) {
						
						final int currentDishAmount = digit;
						data.setNum(currentDishAmount);
						
						DishOrderDTO dishOrder = SessionManager.getInstance().getDishOrder(DishOrderActivity.this, resId);
						dishOrder.updateDishOrder(data);
						SessionManager.getInstance().setDishOrder(DishOrderActivity.this, dishOrder, resId);
						showBottom(dishOrder);
						
						adapter.setList(getList(dishOrder));
					}
				});
			}
		});
		
		adapter.setExistPage(false);
		adapter.setList(getList(dishOrder));
		adapter.setListView(mListView);
		
		// ---- 刷新界面底部
		showBottom(dishOrder);

		// 清空按钮
		mRightTitleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				
				//-----
				OpenPageDataTracer.getInstance().addEvent("清空按钮");	
				//-----
				
				final DishOrderDTO dto = SessionManager.getInstance() .getDishOrder(mContext, resId);

				if (!dto.getDishDataIdList().isEmpty()
						&& dto.getDishDataIdList().size() > 0) {
					DialogUtil.showAlert(mContext, true,
							getString(R.string.text_info_dish_order_clear),
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dto.clearAllExceptFreeDish();
									SessionManager.getInstance().setDishOrder(mContext, dto, resId);
									adapter.setList(getList(SessionManager.getInstance().getDishOrder(mContext, resId)));
									showBottom(SessionManager.getInstance().getDishOrder(mContext, resId));
									finish();
								}
							}, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});

				}
			}
		});

		getBottomLeftBtn().setVisibility(View.VISIBLE);
		getBottomRightBtn().setVisibility(View.GONE);
		getBottomLeftBtn().setText("保存菜单");
		getBottomRightBtn().setText("继续点菜");
		
		// 保存菜单
		getBottomLeftBtn().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DishOrderDTO dto = SessionManager.getInstance().getDishOrder(
						mContext, resId);

				if (dto.getDishDataList().size() > 0) {
					boolean flag = false;
					for (DishData dish : dto.getDishDataList()) {
						if ((dish.getNum()) > 0) {
							flag = true;
							break;
						}
					}

					if (flag) {
						DialogUtil
								.showAlert(
										mContext,
										true,
										"确定保存您的菜单吗?",
										new OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												executePostDishOrderTask();
											}
										}, new OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

											}
										});
					} else {
						DialogUtil.showToast(mContext, " 您还没有添加任何新菜，请先点菜");
					}
				}else{
					DialogUtil.showToast(mContext, " 您还没有添加任何新菜，请先点菜");
				}
			}
		});
		
//		// 继续点菜
//		getBottomRightBtn().setVisibility(View.GONE);

		// 如果已下单，则隐藏相关按钮
		if (dishOrder.getDishDataList().size() == 0
				&& dishOrder.getDishDataHistoryList().size() > 0) {
			mRightTitleBtn.setVisibility(View.INVISIBLE);
			mBottomBtn.setVisibility(View.INVISIBLE);
		}

		this.getDishBaseLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// -------
	//添加或更新菜单订单，返回SimpleData 
	private void executePostDishOrderTask() {
		
		// -----
		OpenPageDataTracer.getInstance().addEvent("保存菜单按钮");
		// -----
		
		ServiceRequest request = new ServiceRequest(API.postDishOrder);
		request.addData("postTag", postTag);  //提交类型   1:添加(餐馆详细页进来)   2:更新  
		request.addData("uuid", uuid); //uuid  postTag=1: restId   2:dishOrderId; 
		request.addData("dishList", getDishList()); //菜单列表   dishId:份数|dishId:份数 
		CommonTask.request(request, new CommonTask.TaskListener<SimpleData>() {
			@Override
			protected void onSuccess(SimpleData dto) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("保存菜单按钮");
				// -----
				
				postTag = 2; // 提交完成后，保证postTag = 2; 表示，下一次继续保存菜单的时候，是更新
				uuid = dishOrderId = dto.getUuid();  //返回菜单的Id
				dishOrder.setOrderId(dishOrderId);
				SessionManager.getInstance().setDishOrder(mContext, dishOrder, resId);
				
				DialogUtil.showToast(mContext, dto != null ? dto.getMsg() : "保存成功，请至用户中心查看");
				finish();
			}
			
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("保存菜单按钮");
				// -----
			}
		});
	}

	//菜单列表   dishId:份数|dishId:份数
	private String getDishList() {
		String dishList = "";

		List<DishData> dishDatas = SessionManager.getInstance().getDishOrder(mContext, resId).getDishDataList();

		for (int i = 0; i < dishDatas.size(); i++) {
			DishData dishData = dishDatas.get(i);
			String str = dishData.getUuid() + ":"
					+ (dishData.getNum() + dishData.getOldNum());
			if (i == 0) {
				dishList += str;
			} else {
				dishList = dishList + "|" + str;
			}
		}
		return dishList;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		recycle();
		super.finish();
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		System.gc();
	}
}
