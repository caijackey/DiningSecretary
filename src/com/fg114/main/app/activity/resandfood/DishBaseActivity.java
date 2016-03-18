package com.fg114.main.app.activity.resandfood;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.SessionManager;

/**
 * =========================ZhaoYao=================================
 * 点菜功能基类
 * 统一设定界面底部
 * @author nieyinyin
 *
 */
public class DishBaseActivity extends MainFrameActivity {
	private TextView mBottomLeftText;// bottom left text
	private TextView mBottomCenterText;// bottom center text
	protected Button mBottomLeftBtn;// 立即购买
	protected Button mBottomRightBtn;// 稍后再说 
	private LinearLayout mLayout;
	private TextView mBottomRightText;// bottom right text 时价X份
	private TextView tvBottomCashTicket;
	//整个底部布局
	private View mBottomLayout;
	
	// 是否能点菜
	protected boolean isCanDish = true;
	protected String resId = "";
	protected int srcPage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_REST_ID)) {
			resId = bundle.getString(Settings.BUNDLE_REST_ID);
			srcPage = bundle.getInt("srcPage");
		}
		
		initData(bundle);

		// 初始化界面
		initComponent();

	}
	
	//处理页面的数据逻辑
	protected void initData(Bundle bundle){
		DishOrderDTO dishOrder = SessionManager.getInstance().getDishOrder(this, resId);//取得DishOrder对象 通过判断缓存里面是否有数据 如果没有则返回一个空的对象
		if (bundle != null) {
			String restId = bundle.getString(Settings.BUNDLE_REST_ID);
			String tableId = bundle.getString(Settings.BUNDLE_TABLE_ID);
			if (!CheckUtil.isEmpty(restId) && !CheckUtil.isEmpty(tableId)) {
				if (CheckUtil.isEmpty(dishOrder.getRestId())) {// 以前未下过订单 dishOrder.getRestId() 为空
					dishOrder.setRestId(restId); //设置restId值
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

		mBottomLayout = (View) contextView.findViewById(R.id.dishbase_main_bottomlayout);
		mBottomLeftText = (TextView) contextView
				.findViewById(R.id.dishbase_bottomlefttext);
		mBottomCenterText = (TextView) contextView
				.findViewById(R.id.dishbase__bottomcentertext);
		mLayout = (LinearLayout) contextView
				.findViewById(R.id.dishbase_main_layout);
		mBottomRightText = (TextView) contextView
				.findViewById(R.id.dishbase__bottomrighttext);

		mBottomLeftBtn = (Button) contextView.findViewById(R.id.dishbase__bottomLeftbtn);
		mBottomRightBtn = (Button) contextView.findViewById(R.id.dishbase__bottomRightbtn);
		tvBottomCashTicket = (TextView)contextView.findViewById(R.id.dishbase_bottomCashTicketText);
		
		super.getBottomLayout().setVisibility(View.GONE);
		setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
	}

	/*******************************************
	 * 取组件
	 */
	protected Button getBottomLeftBtn() {
		return mBottomLeftBtn;
	}
	protected Button getBottomRightBtn() {
		return mBottomRightBtn;
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
	
	protected View getBottomLayout(){
		return mBottomLayout;
	}
	
	public TextView getTvBottomCashTicket() {
		return tvBottomCashTicket;
	}

	public void setTvBottomCashTicket(TextView tvBottomCashTicket) {
		this.tvBottomCashTicket = tvBottomCashTicket;
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

		for (DishData data : dishDatas) {
			int num = data.getNum()+data.getOldNum();// 该菜的份数
			double single = data.getPrice() * num;
			totalNum += num;

			if (data.isCurrentPriceTag()) {// 是否是时价：是！
				// currentNum += num;
				isCurrentPrice = true;
			} else {
				totle += single;
			}
			
			//TODO:nieyinyin
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
		}

		mBottomLeftText.setText( totalNum + "份 ");
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

	/**
	 * 此段逻辑抽个时间审视清楚，暂时只是了解了大概
	 * @param dishOrder
	 * @return
	 */
	public List<DishData> getList(DishOrderDTO dishOrder) {
		ArrayList<DishData> list = new ArrayList<DishData>();
		if (null != dishOrder) {
			List<DishData> newDatas = dishOrder.getDishDataList();

			ArrayList<String> typeIds = new ArrayList<String>();
			HashMap<String, ArrayList<DishData>> dishDatas = new HashMap<String, ArrayList<DishData>>();

			for (DishData dishData : newDatas) {
				if(dishData.getNum() == 0){
					continue;
				}
				dishData.setFirstInCart(false);
				String typeId = dishData.getTypeId();
				ArrayList<DishData> dish;
				if (!typeIds.contains(typeId)) {
					typeIds.add(typeId);
					dish = new ArrayList<DishData>();
					dishData.setFirstInCart(true);   //TODO  ----------------------------
					dishDatas.put(typeId, dish);
				}

				dish = dishDatas.get(typeId);
				dish.add(dishData);
			}

			for (String keys : typeIds) {
				if (!dishDatas.get(keys).isEmpty())
					list.addAll(dishDatas.get(keys));
			}
		}
		return list;
	}

	/**
	 * 输出异常信息
	 * @param tag
	 * @param tr
	 */
	protected void log(String tag,Throwable tr) {
		Log.e(tag, tr.getLocalizedMessage() , tr);
	}
	
	/**
	 * 输出调试信息
	 * @param tag
	 * @param msg
	 */
	protected void log(String tag, String msg){
		Log.d(tag, msg);
	}
	
	public interface DishDataChangedListener {
		public void onDishDataChanged(DishData data);
	}
}
