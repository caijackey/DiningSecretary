package com.fg114.main.app.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.ViewUtils;

public class DishBaseActivity extends MainFrameActivity {
	private TextView mBottomLeftText;// bottom left text
	private TextView mBottomCenterText;// bottom center text
	private Button mBottomBtn;// 确认下单
	private LinearLayout mLayout;
	private TextView mBottomRightText;// bottom right text 时价X份
	
	// 是否能点菜
	protected boolean isCanDish = true;
	protected String resId = "";
	protected int srcPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		
		if (bundle.containsKey(Settings.BUNDLE_REST_ID)) {
			resId = bundle.getString(Settings.BUNDLE_REST_ID);
		}
		srcPage = bundle.getInt(Settings.BUNDLE_DISH_SRC_PAGE);
		
		initData(bundle);

		// 初始化界面
		initComponent();

		// showBottom(dishOrder);
	}
	//处理页面的数据逻辑
	protected void initData(Bundle bundle){
		DishOrderDTO dishOrder = SessionManager.getInstance()
				.getDishOrder(this, resId);
		if (bundle != null) {
			String restId = bundle.getString(Settings.BUNDLE_REST_ID);
			String tableId = bundle.getString(Settings.BUNDLE_TABLE_ID);
			if (!CheckUtil.isEmpty(restId) && !CheckUtil.isEmpty(tableId)) {
				if (CheckUtil.isEmpty(dishOrder.getRestId())) {
					// 以前未下过订单
					dishOrder.setRestId(restId);
					dishOrder.setTableId(tableId);
					SessionManager.getInstance().setDishOrder(this, dishOrder, resId);
					SessionManager.getInstance().setDishListPackDTO(this, "{}", null);
				} else {
					if (!dishOrder.getRestId().equals(restId)) {
						// 当此次的餐厅与上次的不同时
						dishOrder.reset();
						dishOrder.setRestId(restId);
						dishOrder.setTableId(tableId);
						SessionManager.getInstance().setDishOrder(this, dishOrder, resId);
						SessionManager.getInstance().setDishListPackDTO(this, "{}", null);
					} else {
						// 当此次的桌号与上次的不同时
						if (!dishOrder.getTableId().equals(tableId)) {
							dishOrder.reset();
							dishOrder.setTableId(tableId);
							SessionManager.getInstance().setDishOrder(this, dishOrder, resId);
						}
					}
				}

			}
//			else if (!CheckUtil.isEmpty(restId) && CheckUtil.isEmpty(tableId)){
//				// 如果未传过来桌号
//				isCanDish = false;
//				dishOrder.setRestId(restId);
//				dishOrder.setTableId(tableId);
//				SessionManager.getInstance().setDishOrder(this, dishOrder);
//			}
			
			// 设置餐厅名称
			if (!CheckUtil.isEmpty(restId)) {
				String restName = "";
				RestInfoData restInfo = SessionManager.getInstance()
						.getRestaurantInfo(this, resId);
				if (restInfo != null && !TextUtils.isEmpty(restInfo.name)) {
					restName = restInfo.name;
					dishOrder.setRestName(restName);
					SessionManager.getInstance().setDishOrder(this, dishOrder, resId);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		showBottom(SessionManager.getInstance().getDishOrder(this, resId));
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 内容部分
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = inflater.inflate(R.layout.dish_base, null);

		mBottomLeftText = (TextView) contextView
				.findViewById(R.id.dishbase_bottomlefttext);
		mBottomCenterText = (TextView) contextView
				.findViewById(R.id.dishbase__bottomcentertext);
		mLayout = (LinearLayout) contextView
				.findViewById(R.id.dishbase_main_layout);
		mBottomRightText = (TextView) contextView
				.findViewById(R.id.dishbase__bottomrighttext);

		mBottomBtn = (Button) contextView
				.findViewById(R.id.dishbase__bottombtn);

		mBottomBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
//				if (!isCanDish) {
//					// 如果不能点菜
//					DialogUtil.showToast(DishBaseActivity.this, "本功能只支持在餐厅中使用,如果您正在餐厅用餐,请扫描您台卡的二维码");
//					return;
//				}
				
				DishOrderDTO dto = SessionManager.getInstance().getDishOrder(
						DishBaseActivity.this, resId);
				if (dto.getDishDataList() == null
						|| dto.getDishDataList().size() == 0) {
					DialogUtil.showToast(DishBaseActivity.this, "请先选择您要点的菜");
					return;
				}
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID,
						resId);
				bundle.putInt(Settings.BUNDLE_DISH_SRC_PAGE,
						srcPage);
//				ActivityUtil.jump(DishBaseActivity.this,
//						DishOrderActivity.class,
//						0, bundle);
			}
		});

		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}

	/*******************************************
	 * 取组件
	 */
	protected Button getBottomBtn() {
		return mBottomBtn;
	}

	protected LinearLayout getDishBaseLayout() {
		return mLayout;
	}

	protected TextView getBottomLeftText() {
		return mBottomLeftText;
	}

	protected TextView getBottomCenterText() {
		return mBottomCenterText;
	}

	protected TextView getBottomRightText() {
		return mBottomRightText;
	}

	protected void showBottom(DishOrderDTO dishOrder) {
		List<DishData> dishDatas = new ArrayList<DishData>();

		if (dishOrder.getDishDataList().size() > 0) {
			dishDatas = dishOrder.getDishDataList();
		} 
//		else {
//			dishDatas = dishOrder.getDishDataHistoryList();
//		}

		double totle = 0;// 总共的价格
		int totalNum = 0;// 总共的数量
		// int currentNum = 0;// 时价的份数
		boolean isCurrentPrice = false;

		DecimalFormat format = new DecimalFormat("######0.00");

//		for (DishData data : dishDatas) {
//			int num = data.getNum()+data.getOldNum();// 该菜的份数
//			double single = data.getPrice() * num;
//			totalNum += num;
//
//			if (data.isCurrentPriceTag()) {// 是否是时价：是！
//				// currentNum += num;
//				isCurrentPrice = true;
//			} else {
//				totle += single;
//			}
//
//			for (DishProcessTypeDTO type : data.getProcessTypeList()) {// 附加价格
//				if (data.getSelectProcessTypeId() == type.getUuid()) {
//					double price = type.getPrice();
//					if (type.isCalByNumTag()) {// 如果是按份收钱
//						totle += price * num;
//					} else {
//						totle += price;
//					}
//					break;
//				}
//			}
//		}
//
//		mBottomLeftText.setText("已点" + totalNum + "份   共");
//		mBottomCenterText.setText(format.format(totle) + "元");
//		if (isCurrentPrice) {
//			if (!mBottomRightText.isShown())
//				mBottomRightText.setVisibility(View.VISIBLE);
//			mBottomRightText.setText(" + 时价 ");
//		} else {
//			if (mBottomRightText.isShown())
//				mBottomRightText.setVisibility(View.INVISIBLE);
//		}
	}

	public List<DishData> getList(DishOrderDTO dishOrder) {
		ArrayList<DishData> list = new ArrayList<DishData>();
		if (null != dishOrder) {
			List<DishData> newDatas = dishOrder.getDishDataList();
//			List<DishData> oldDatas = dishOrder.getDishDataHistoryList();

			ArrayList<String> typeIds = new ArrayList<String>();
			HashMap<String, ArrayList<DishData>> dishDatas = new HashMap<String, ArrayList<DishData>>();

////			if (newDatas.size() > 0) {
//				for (DishData dishData : newDatas) {
//					String typeId = dishData.getTypeId();
//					ArrayList<DishData> dish;
//					if (!typeIds.contains(typeId)) {
//						typeIds.add(typeId);
//						dish = new ArrayList<DishData>();
//						dishData.setFirst(true);
//						dishDatas.put(typeId, dish);
//					}
//
//					dish = dishDatas.get(typeId);
//					dish.add(dishData);
//				}
////			} else {
//				for (DishData dishData : oldDatas) {
//					String typeId = dishData.getTypeId();
//					ArrayList<DishData> dish;
//					if (!typeIds.contains(typeId)) {
//						typeIds.add(typeId);
//						dish = new ArrayList<DishData>();
//						dishData.setFirst(true);
//						dishDatas.put(typeId, dish);
//					}
//
//					dish = dishDatas.get(typeId);
//					dish.add(dishData);
//				}
//			}

			for (String keys : typeIds) {
				if (!dishDatas.get(keys).isEmpty())
					list.addAll(dishDatas.get(keys));
			}
		}
		return list;
	}

}
