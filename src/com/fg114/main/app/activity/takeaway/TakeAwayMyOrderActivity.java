package com.fg114.main.app.activity.takeaway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.baidu.platform.comapi.map.t;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MyNewTakeAwayOrderDetailActivity;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.app.view.DigitalSelector.OnDigitChangeListener;
import com.fg114.main.app.view.TakeawayOrderSelectorWheelView;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutMenuListPack2DTO;
import com.fg114.main.service.dto.TakeoutMenuSelData;
import com.fg114.main.service.dto.TakeoutMenuSelPackDTO;
import com.fg114.main.service.dto.TakeoutPostOrderFormData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.util.ZipUtils;

/**
 * 外卖 我的订单页面 需要传入参数：takeoutId// 外卖餐厅id menuSelPack// // menuSelPack//选择的菜品json
 * 字符串
 * 
 * @author dengxiangyu
 * 
 */
public class TakeAwayMyOrderActivity extends MainFrameActivity {
	// 缓存数据
	private TakeoutPostOrderFormData takeoutPostOrderFormData;
	private String takeoutId;
	private String menuSelPackJson;
	private String receiveAdressId;
	
	private int resultCodeS;

	// 初始化控件
	private TextView takeaway_my_order_hint;
	private TextView takeaway_my_order_name;
	private TextView takeaway_my_order_phone;
	private TextView takeaway_my_order_address;
	private View takeaway_my_order_info_bt;
	private TextView takeaway_my_order_data;
	private RadioButton takeaway_my_order_offlinepay;
	private RadioButton takeaway_my_order_onlinepay;
	private LinearLayout takeaway_my_order_selpack;
	private View takeaway_my_order_data_bt;
	private Button takeaway_my_order_cart_bnt;
	private TextView takeaway_my_order_sum_hint;
	private ImageView takeout_my_order_pbar;
	private View takeaway_my_order_bottom_layout;
	private EditText takeaway_order_memo_et;

	// popupwindow控件
	private View popupwindowView;
	private Button takeaway_order_bnt_cancel;
	private Button takeaway_order_bnt_confirm;
	private TakeawayOrderSelectorWheelView takeaway_order_selection_wheel;
	private PopupWindow PopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖订单表单", takeoutId);
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		takeoutId = bundle.getString(Settings.UUID);
		menuSelPackJson = bundle.getString(Settings.BUNDLE_menuSelPack);
		
		takeoutPostOrderFormData=new TakeoutPostOrderFormData();
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			// TODO: 跳至无网提示页
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();
		excuteTakeoutPostOrderFormInfoTask(false);
	}

	@Override
	protected void onResume() {

		super.onResume();
		
		if(resultCodeS==980){
		excuteTakeoutPostOrderFormInfoTask(true);
		}
	}
	

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖订单表单", takeoutId);
		// ----------------------------
	}

	private void initComponent() {
		this.getTvTitle().setText("完善订单信息");
		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = (RelativeLayout) mInflater.inflate(R.layout.takeaway_my_order_act, null);
		takeaway_my_order_hint = (TextView) contextView.findViewById(R.id.takeaway_my_order_hint);
		takeaway_my_order_name = (TextView) contextView.findViewById(R.id.takeaway_my_order_name);
		takeaway_my_order_phone = (TextView) contextView.findViewById(R.id.takeaway_my_order_phone);
		takeaway_my_order_address = (TextView) contextView.findViewById(R.id.takeaway_my_order_address);
		takeaway_my_order_info_bt = (View) contextView.findViewById(R.id.takeaway_my_order_info_bt);
		takeaway_my_order_data = (TextView) contextView.findViewById(R.id.takeaway_my_order_data);
		takeaway_my_order_offlinepay = (RadioButton) contextView.findViewById(R.id.takeaway_my_order_offlinepay);
		takeaway_my_order_onlinepay = (RadioButton) contextView.findViewById(R.id.takeaway_my_order_onlinepay);
		takeaway_my_order_selpack = (LinearLayout) contextView.findViewById(R.id.takeaway_my_order_selpack);
		takeaway_my_order_data_bt = (View) contextView.findViewById(R.id.takeaway_my_order_data_bt);
		takeaway_my_order_cart_bnt = (Button) contextView.findViewById(R.id.takeaway_my_order_cart_bnt);
		takeaway_my_order_sum_hint = (TextView) contextView.findViewById(R.id.takeaway_my_order_sum_hint);
		takeout_my_order_pbar = (ImageView) contextView.findViewById(R.id.takeout_my_order_pbar);
		takeaway_my_order_bottom_layout = (View) contextView.findViewById(R.id.takeaway_my_order_bottom_layout);
		takeaway_order_memo_et = (EditText) contextView.findViewById(R.id.takeaway_order_memo_et);

		takeaway_my_order_offlinepay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择支付方式按钮");
				// -----
				takeaway_my_order_offlinepay.setTextColor(getResources().getColor(R.color.new_text_color_red));
				takeaway_my_order_onlinepay.setTextColor(getResources().getColor(R.color.text_color_light_gray));
			}
		});
		takeaway_my_order_onlinepay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择支付方式按钮");
				// -----

				takeaway_my_order_onlinepay.setTextColor(getResources().getColor(R.color.new_text_color_red));
				takeaway_my_order_offlinepay.setTextColor(getResources().getColor(R.color.text_color_light_gray));
			}
		});

		this.getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				backActivity();
			}
		});

		initPopupView();
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 
	 * @param refrsh
	 *            是否刷新
	 */
	private void excuteTakeoutPostOrderFormInfoTask(final boolean refrsh) {
		// 获得订单列表 ，返回OrderListDTO
		ServiceRequest request = new ServiceRequest(API.getTakeoutPostOrderFormInfo);
		request.addData("takeoutId", takeoutId);// 外卖餐厅id
		request.addData("menuSelPack", menuSelPackJson);// //
														// menuSelPack//选择的菜品json//
		request.setCanUsePost(true);

		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		CommonTask.request(request, "", new CommonTask.TaskListener<TakeoutPostOrderFormData>() {
			@Override
			protected void onSuccess(TakeoutPostOrderFormData dto) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				if (refrsh) {
					takeoutPostOrderFormData=dto;
//					takeoutPostOrderFormData.menuSelPack = dto.menuSelPack;
//					takeoutPostOrderFormData.hintForSum = dto.hintForSum;
					resetTotal(takeoutPostOrderFormData);
					takeaway_my_order_bottom_layout.setVisibility(View.VISIBLE);
					takeout_my_order_pbar.clearAnimation();
					takeout_my_order_pbar.setVisibility(View.GONE);

				} else {
					takeoutPostOrderFormData = dto;
					if (takeoutPostOrderFormData.userReceiveAdressData != null) {
						takeaway_my_order_phone.setVisibility(View.VISIBLE);
						takeaway_my_order_address.setVisibility(View.VISIBLE);
						receiveAdressId = takeoutPostOrderFormData.userReceiveAdressData.uuid;// 外卖地址ID
					} else {
						takeaway_my_order_name.setText("请添加或选择一个送餐地址");
						takeaway_my_order_phone.setVisibility(View.GONE);
						takeaway_my_order_address.setVisibility(View.GONE);
					}
					setView(takeoutPostOrderFormData);
				}

			}

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// doTest_confirm();
				DialogUtil.showToast(ContextUtil.getContext(), message);
				backActivity();
				finish();
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
			}

			private void doTest_confirm() {
				String s = "{\"hintForSend\":\"外送提示\",\"userReceiveAdressData\":{\"uuid\":\"11\",\"name\":\"外卖名字\",\"address\":\"东方路\",\"tel\":\"13000000000\"},\"sendTimeList\":[{\"uuid\":\"1\",\"name\":\"星期一 14:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 15:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 16:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 17:00\",\"selectTag\":\"true\"}],\"canOnlinePayTag\":\"true\",\"canOfflinePayTag\":\"true\",\"menuSelPack\":{\"list\":[{\"dataIdentifer\":\"android1\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android2\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"false\",\"canChangeNumTag\":\"false\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android3\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"4\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"false\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android1\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"false\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android1\",\"typeTag\":\"2\",\"canSelGiftTag\":\"true\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"赠品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"\"}]},\"canOrderTag\":\"true\",\"hintForSum\":\"共5份总计856\"}";
				TakeoutPostOrderFormData data = JsonUtils.fromJson(s, TakeoutPostOrderFormData.class);
				onSuccess(data);

			}

		});
	}

	/**
	 * 提交订单
	 * 
	 * @param dto
	 */
	private void postTakeoutOrder(String sendTimeId, String memo, String menuSelPack) {
		// 提交外卖订单，返回SimpleData.uuid 为返回的订单id
		// 用post请求
		ServiceRequest request = new ServiceRequest(API.postTakeoutOrder2);
		request.addData("takeoutId", takeoutId);// 外卖餐厅id
		request.addData("sendTimeId", sendTimeId);// 用户期望的送餐时间的id
		request.addData("userReceiveAdressId", receiveAdressId);// 用户收货地址id
		request.addData("memo", memo);// 备注 100字
		request.addData("menuSelPack", menuSelPack);// TakeoutMenuSelPackDTO
													// //menuSelPack//选择的菜品json字符串
		if (takeaway_my_order_onlinepay.isChecked()) {
			// 1：货到付款 2：在线支付
			request.addData("payTypeTag", 2);
		} else {
			request.addData("payTypeTag", 1);
		}
		request.setCanUsePost(true);
		// ----
		OpenPageDataTracer.getInstance().addEvent("立即下单按钮");
		// -----
		CommonTask.request(request, "", new CommonTask.TaskListener<SimpleData>() {
			@Override
			protected void onSuccess(SimpleData dto) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("立即下单按钮");
				// -----

				if (!CheckUtil.isEmpty(dto.getUuid())) {
					if (takeaway_my_order_offlinepay.isChecked()) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, dto.getUuid());
						ActivityUtil.jump(TakeAwayMyOrderActivity.this, MyNewTakeAwayOrderDetailActivity.class, 0, bundle);
						TakeAwayNewFoodListActivity.takeAwayNewFoodListActivityInstance.finish();
						finish();
					}
					if (takeaway_my_order_onlinepay.isChecked()) {
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, dto.getUuid());
						ActivityUtil.jump(TakeAwayMyOrderActivity.this, TakeAwayBuyPaymentActivity.class, 0, bundle);
						TakeAwayNewFoodListActivity.takeAwayNewFoodListActivityInstance.finish();
						finish();
					}
				}
			}

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("立即下单按钮");
				// -----
				// doTest_confirm();
				DialogUtil.showToast(ContextUtil.getContext(), message);
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
			}

			private void doTest_confirm() {
				String s = "{\"hintForSend\":\"外送提示\",\"userReceiveAdressData\":{\"uuid\":\"11\",\"name\":\"外卖名字\",\"address\":\"东方路\",\"tel\":\"13000000000\"},\"sendTimeList\":[{\"uuid\":\"1\",\"name\":\"星期一 14:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 15:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 16:00\",\"selectTag\":\"false\"},{\"uuid\":\"1\",\"name\":\"星期一 17:00\",\"selectTag\":\"true\"}],\"canOnlinePayTag\":\"true\",\"canOfflinePayTag\":\"true\",\"menuSelPack\":{\"list\":[{\"dataIdentifer\":\"android1\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android2\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"false\",\"canChangeNumTag\":\"false\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android3\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"4\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"false\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android1\",\"typeTag\":\"1\",\"canSelGiftTag\":\"false\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"菜品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"false\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"半塘+少冰+大杯+珍珠\"},{\"dataIdentifer\":\"android1\",\"typeTag\":\"2\",\"canSelGiftTag\":\"true\",\"giftTypeId\":\"111\",\"uuid\":\"01\",\"name\":\"赠品1\",\"nameColor\":\"#505F03\",\"price\":\"10\",\"num\":\"1\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[],\"selPropertyHint\":\"\"}]},\"canOrderTag\":\"true\",\"hintForSum\":\"共5份总计856\"}";
				TakeoutPostOrderFormData data = JsonUtils.fromJson(s, TakeoutPostOrderFormData.class);
				// onSuccess(data);

			}

		});
	}

	private void setView(final TakeoutPostOrderFormData dto) {
		if (dto == null) {
			return;
		}
		if (CheckUtil.isEmpty(dto.hintForSend)) {
			takeaway_my_order_hint.setVisibility(View.GONE);
		} else {
			takeaway_my_order_hint.setVisibility(View.VISIBLE);
			takeaway_my_order_hint.setText(Html.fromHtml(dto.hintForSend));
		}

		if (dto.canOnlinePayTag) {
			takeaway_my_order_onlinepay.setVisibility(View.VISIBLE);
		} else {
			takeaway_my_order_onlinepay.setVisibility(View.GONE);
		}

		if (dto.canOfflinePayTag) {
			takeaway_my_order_offlinepay.setVisibility(View.VISIBLE);
			takeaway_my_order_offlinepay.performClick();
		} else {
			takeaway_my_order_offlinepay.setVisibility(View.GONE);
		}

		if (dto.canOrderTag) {
			takeaway_my_order_cart_bnt.setVisibility(View.VISIBLE);
		} else {
			takeaway_my_order_cart_bnt.setVisibility(View.GONE);
		}

		takeaway_my_order_sum_hint.setText(Html.fromHtml(dto.hintForSum));

		// 收货信息
		if (dto.userReceiveAdressData != null) {
			if (!CheckUtil.isEmpty(dto.userReceiveAdressData.name)) {
				takeaway_my_order_name.setText(dto.userReceiveAdressData.name);
			}
			if (!CheckUtil.isEmpty(dto.userReceiveAdressData.tel)) {
				takeaway_my_order_phone.setText(dto.userReceiveAdressData.tel);
			}
			if (!CheckUtil.isEmpty(dto.userReceiveAdressData.address)) {
				takeaway_my_order_address.setText(dto.userReceiveAdressData.address);
			}
		}

		// 点击收货地址
		takeaway_my_order_info_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("选择送餐地址按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, receiveAdressId);
				ActivityUtil.jump(TakeAwayMyOrderActivity.this, NewTakeAwayManageAddressActivity.class, 0, bundle);

			}
		});

		Boolean isSelectTime = false;
		// 设置已经选择的时间
		for (int i = 0; i < dto.sendTimeList.size(); i++) {
			if (dto.sendTimeList.get(i).isSelectTag()) {
				takeaway_my_order_data.setText(dto.sendTimeList.get(i).getName());
				isSelectTime = true;
			}
		}
		// 判断后台是否设置初始位置
		if (!isSelectTime && dto.sendTimeList != null && dto.sendTimeList.size() != 0) {
			takeaway_my_order_data.setText(dto.sendTimeList.get(0).getName());// 设置初始位置
			dto.sendTimeList.get(0).setSelectTag(true);
		}

		// 送餐时间
		takeaway_my_order_data_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("选择送餐时间按钮");
				// -----

				showWheelViewPopup(dto);
			}
		});
		// 确认下单
		takeaway_my_order_cart_bnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// TODO Auto-generated method stub
				String sendTimeId = "";
				for (int j = 0; j < dto.sendTimeList.size(); j++) {
					if (dto.sendTimeList.get(j).isSelectTag()) {
						sendTimeId = dto.sendTimeList.get(j).getUuid();
					}
				}

				String memo = takeaway_order_memo_et.getText().toString();

				String menuSelPack = doMenuSelPack(takeoutPostOrderFormData.menuSelPack);
				// 支付方式
				if (takeaway_my_order_offlinepay.isChecked() || takeaway_my_order_onlinepay.isChecked()) {
					postTakeoutOrder(sendTimeId, memo, menuSelPack);
				} else {
					DialogUtil.showToast(ContextUtil.getContext(), "请选择支付方式");
				}
			}
		});

		// 菜品列表
		if (takeaway_my_order_selpack.getChildCount() != 0) {
			takeaway_my_order_selpack.removeAllViews();
		}
		if (dto.menuSelPack != null) {
			for (int i = 0; i < dto.menuSelPack.list.size(); i++) {
				takeaway_my_order_selpack.addView(getTakeoutMenuSel(dto.menuSelPack.list.get(i)));
			}
		}
	}

	// 重置
	private void resetTotal(TakeoutPostOrderFormData dto) {
		// 菜品列表
		if (takeaway_my_order_selpack.getChildCount() != 0) {
			takeaway_my_order_selpack.removeAllViews();
		}
		if (dto.menuSelPack != null) {
			for (int i = 0; i < dto.menuSelPack.list.size(); i++) {
				takeaway_my_order_selpack.addView(getTakeoutMenuSel(dto.menuSelPack.list.get(i)));
			}
		}

		if(dto.canOrderTag){
			takeaway_my_order_cart_bnt.setVisibility(View.VISIBLE);
		}else{
			takeaway_my_order_cart_bnt.setVisibility(View.GONE);
		}
		takeaway_my_order_sum_hint.setText(Html.fromHtml(dto.hintForSum));
	}

	// 初始化搜索PopupWindow控件
	private void initPopupView() {
		popupwindowView = LayoutInflater.from(TakeAwayMyOrderActivity.this).inflate(R.layout.takeaway_order_data_wheel_view, null);
		takeaway_order_bnt_cancel = (Button) popupwindowView.findViewById(R.id.takeaway_order_bnt_cancel);
		takeaway_order_bnt_confirm = (Button) popupwindowView.findViewById(R.id.takeaway_order_bnt_confirm);
		takeaway_order_selection_wheel = (TakeawayOrderSelectorWheelView) popupwindowView.findViewById(R.id.takeaway_order_selection_wheel);
	}

	// 送餐时间选择
	private void showWheelViewPopup(final TakeoutPostOrderFormData dto) {
		if (dto == null && dto.sendTimeList == null) {
			return;
		}
		PopupWindow = DialogUtil.showPopupwindow(TakeAwayMyOrderActivity.this, null, popupwindowView);

		takeaway_order_selection_wheel.initData(dto.sendTimeList);
		for (int i = 0; i < dto.sendTimeList.size(); i++) {
			if (dto.sendTimeList.get(i).isSelectTag()) {
				takeaway_order_selection_wheel.setCurrentItemByValue(i);// 设置初始位置
				// spinnerOrderStateTag=true;
			}
		}

		takeaway_order_bnt_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PopupWindow.dismiss();
			}
		});
		takeaway_order_bnt_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				int i = (int) takeaway_order_selection_wheel.getCurrentItemValue();
				for (int j = 0; j < dto.sendTimeList.size(); j++) {
					dto.sendTimeList.get(j).setSelectTag(false);
				}
				dto.sendTimeList.get(i).setSelectTag(true);

				takeaway_my_order_data.setText(dto.sendTimeList.get(i).getName());

				PopupWindow.dismiss();
			}
		});
	}

	// 菜品列表item
	private View getTakeoutMenuSel(final TakeoutMenuSelData data) {
		View view = LayoutInflater.from(TakeAwayMyOrderActivity.this).inflate(R.layout.takeaway_my_order_item, null);
		TextView takeaway_order_item_name = (TextView) view.findViewById(R.id.takeaway_order_item_name);
		TextView takeaway_order_item_price = (TextView) view.findViewById(R.id.takeaway_order_item_price);
		TextView takeaway_order_item_propertytype = (TextView) view.findViewById(R.id.takeaway_order_item_propertytype);
		final DigitalSelector takeaway_property_diaitalselector = (DigitalSelector) view.findViewById(R.id.takeaway_property_diaitalselector);
		ImageView takeaway_order_arrow = (ImageView) view.findViewById(R.id.takeaway_order_arrow);

		takeaway_order_arrow.setVisibility(View.GONE);
		// 赠品
		if (data.typeTag == 2) {
			// 是否可以选择赠品
			if (data.canSelGiftTag) {
				takeaway_order_arrow.setVisibility(View.VISIBLE);
				takeaway_property_diaitalselector.setVisibility(View.GONE);

				view.setOnClickListener(new OnClickListener() {
					// 可选赠品类别id(用于赠品选择页查询)
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						// ----
						OpenPageDataTracer.getInstance().addEvent("选择赠品按钮");
						// -----

						Bundle bundle = new Bundle();
						bundle.putString(Settings.UUID, takeoutId);
						bundle.putString(Settings.BUNDLE_typeId, data.giftTypeId);
						bundle.putString(Settings.BUNDLE_KEY_ID, data.dataIdentifer);
						ActivityUtil.jump(TakeAwayMyOrderActivity.this, NewTakeAwayGiftListActivity.class, 0, bundle);
					}
				});
			}
		}

		takeaway_order_item_name.setText(data.name);
		if (!CheckUtil.isEmpty(data.nameColor)) {
			takeaway_order_item_name.setTextColor(Color.parseColor(data.nameColor));
		}

		double price = data.price;
		if (data.propertyTypeList == null) {
			price = price * data.num;
			takeaway_order_item_price.setText("￥" + price);
		} else {
			for (int i = 0; i < data.propertyTypeList.size(); i++) {
				for (int j = 0; j < data.propertyTypeList.get(i).list.size(); j++) {
					price = price + data.propertyTypeList.get(i).list.get(j).price;
				}
			}
			price = price * data.num;
			takeaway_order_item_price.setText("￥" + price);
		}

		if (data.canShowNumTag && data.typeTag == 1) {
			// 是否显示数量
			takeaway_property_diaitalselector.setVisibility(View.VISIBLE);
			takeaway_property_diaitalselector.setMinValue(0);
			takeaway_property_diaitalselector.setValue(data.num);
			if (data.canChangeNumTag) {
				// 是否可以修改数量
				takeaway_property_diaitalselector.getMinus().setVisibility(View.VISIBLE);
				takeaway_property_diaitalselector.getPlus().setVisibility(View.VISIBLE);
			} else {
				takeaway_property_diaitalselector.getMinus().setVisibility(View.INVISIBLE);
				takeaway_property_diaitalselector.getPlus().setVisibility(View.INVISIBLE);
			}

			takeaway_property_diaitalselector.setOnDigitChangeListener(new OnDigitChangeListener() {

				@Override
				public void onChange(DigitalSelector selector, int digit, int previousValue) {
					// TODO Auto-generated method stub
					// ----
					OpenPageDataTracer.getInstance().addEvent("修改菜品数量按钮");
					// -----

					data.num = digit;
					takeaway_property_diaitalselector.setValue(data.num);
					// 请求服务器 重新获取菜品数据
					takeaway_my_order_bottom_layout.setVisibility(View.GONE);
					takeout_my_order_pbar.setVisibility(View.VISIBLE);

					Animation ani = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					ani.setFillAfter(true);
					ani.setDuration(200);
					takeout_my_order_pbar.clearAnimation();
					takeout_my_order_pbar.startAnimation(ani);

					menuSelPackJson = doMenuSelPack(takeoutPostOrderFormData.menuSelPack);
					excuteTakeoutPostOrderFormInfoTask(true);

				}
			});
		} else {
			takeaway_property_diaitalselector.setVisibility(View.GONE);
		}

		if (CheckUtil.isEmpty(data.selPropertyHint)) {
			takeaway_order_item_propertytype.setVisibility(View.GONE);
		} else {
			takeaway_order_item_propertytype.setVisibility(View.VISIBLE);
			takeaway_order_item_propertytype.setText(data.selPropertyHint);
		}
		return view;

	}

	private String doMenuSelPack(TakeoutMenuSelPackDTO menuSelPack) {
		for (int i = 0; i < menuSelPack.list.size(); i++) {
			if (menuSelPack.list.get(i).num == 0) {
				menuSelPack.list.remove(i);
				--i;// 删除了元素，迭代的下标也跟着改变
			}
		}
		String s = JsonUtils.toJson(menuSelPack, null, true, null, null, false);
		return s;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		resultCodeS=resultCode;
		if (resultCode == 200) {
			receiveAdressId = data.getExtras().getString(Settings.UUID);
			String name = data.getExtras().getString(Settings.BUNDLE_TAKEAWAY_NAME);
			String address = data.getExtras().getString(Settings.BUNDLE_TAKEAWAY_ADDRESS);
			String tel = data.getExtras().getString(Settings.BUNDLE_TAKEAWAY_TEL);

			if (!CheckUtil.isEmpty(name)) {
				takeaway_my_order_name.setText(name);
			}
			if (!CheckUtil.isEmpty(tel)) {
				takeaway_my_order_phone.setVisibility(View.VISIBLE);
				takeaway_my_order_phone.setText(tel);
			}
			if (!CheckUtil.isEmpty(address)) {
				takeaway_my_order_address.setVisibility(View.VISIBLE);
				takeaway_my_order_address.setText(address);
			}

			if (CheckUtil.isEmpty(name) && CheckUtil.isEmpty(address) && CheckUtil.isEmpty(tel)) {
				takeaway_my_order_name.setText("请添加或选择一个送餐地址");
				takeaway_my_order_phone.setVisibility(View.GONE);
				takeaway_my_order_address.setVisibility(View.GONE);
			}
		}
		//赠品选择
		if(resultCode == 980){
			String uuid=data.getExtras().getString(Settings.UUID);
			String foodname=data.getExtras().getString(Settings.BUNDLE_FOOD_NAME);
			String dataIdentifer=data.getExtras().getString(Settings.BUNDLE_KEY_ID);
			
			for(int i=0;i<takeoutPostOrderFormData.menuSelPack.list.size();i++){
				if(takeoutPostOrderFormData.menuSelPack.list.get(i).dataIdentifer.equals(dataIdentifer)){
					takeoutPostOrderFormData.menuSelPack.list.get(i).uuid=uuid;
//					Log.v("TAG", takeoutPostOrderFormData.menuSelPack.list.get(i).name);
					takeoutPostOrderFormData.menuSelPack.list.get(i).name=foodname;
//					Log.v("TAG", takeoutPostOrderFormData.menuSelPack.list.get(i).name+"1111");
				}
			}
			menuSelPackJson = doMenuSelPack(takeoutPostOrderFormData.menuSelPack);
			
		}
	}

	private void backActivity() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		if (takeoutPostOrderFormData != null) {
			if (takeoutPostOrderFormData.menuSelPack != null) {
				bundle.putString(Settings.BUNDLE_menuSelPack, doMenuSelPack(takeoutPostOrderFormData.menuSelPack));
			}
		}
		intent.putExtras(bundle);
		TakeAwayMyOrderActivity.this.setResult(201, intent);
		TakeAwayMyOrderActivity.this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
}
