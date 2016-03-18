package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.service.dto.OrderList2Data;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;

/**
 * 可发送短信邀请的订单列表适配器
 * 
 * @author xujianjun,2012-05-29
 * 
 */
public class OrderListOfShortMessageAdapter extends BaseAdapter {

	private static final String TAG = "OrderListOfShortMessageAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<OrderList2Data> list = null;
	private LayoutInflater mInflater = null;
	private Context context;

	public OrderListOfShortMessageAdapter(Context c) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		// 餐厅部分
		LinearLayout resLayout;
		TextView tvResName;
		TextView tvOrderState;
		TextView tvReserverTime;
		TextView tvPhoneLable;
		TextView tvPhone;
		// 消息部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_order_of_short_message, null);
			holder.resLayout = (LinearLayout) convertView.findViewById(R.id.order_list_item_orderLayout);
			holder.tvResName = (TextView) convertView.findViewById(R.id.order_list_item_tvResName);
			holder.tvOrderState = (TextView) convertView.findViewById(R.id.order_list_item_tvState);
			holder.tvReserverTime = (TextView) convertView.findViewById(R.id.order_list_item_tvLunchTime);
			holder.tvPhoneLable = (TextView) convertView.findViewById(R.id.order_list_item_tvPhoneLable);
			holder.tvPhone = (TextView) convertView.findViewById(R.id.order_list_item_tvPhone);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.order_list_item_msgLayout);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.order_list_item_pBar);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.order_list_item_tvMsg);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		final OrderList2Data order = list.get(position);

		if (!order.getOrderId().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
			// 餐馆的场合
			holder.resLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置餐馆名字
			if ("".equals(order.getRestName().trim())) {
				holder.tvResName.setText(R.string.text_null_hanzi);
			} else {
				holder.tvResName.setText(order.getRestName());
			}
			// 设置订单状态
			if (order.getStatusTag() == 1 || order.getStatusTag() == 2) {
				// 需要标红
				holder.tvOrderState.setTextColor(context.getResources().getColor(R.color.text_color_red_2));
				holder.resLayout.setBackgroundResource(R.drawable.order_list_red_bt);

			} else {
				holder.tvOrderState.setTextColor(context.getResources().getColor(R.color.text_color_gray));
				holder.resLayout.setBackgroundResource(R.drawable.order_list_bt);
			}
			holder.resLayout.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));
			holder.tvOrderState.setText(order.getStatusName());

			// 设置就餐时间
			holder.tvReserverTime.setText(ConvertUtil.convertLongToDateString(order.getReserveTime(), ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));

			holder.tvPhoneLable.setText(R.string.text_layout_booker_phone);
			holder.tvPhone.setText(order.getBookerTel());
			holder.tvPhone.setTextColor(context.getResources().getColor(R.color.text_color_gray));

		} else {
			// 消息的场合
			holder.resLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(order.getRestName())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(order.getRestName());
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<OrderList2Data> getList() {
		return list;
	}

	public void setList(List<OrderList2Data> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<OrderList2Data>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<OrderList2Data> list, boolean isLast) {
		// 删除最后一条消息
		this.list.remove(this.list.size() - 1);
		this.list.addAll(createMsgDataToList(list, isLast));
		notifyDataSetChanged();
	}

	/**
	 * 建立最后一条数据
	 * 
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<OrderList2Data> createMsgDataToList(List<OrderList2Data> list, boolean isLast) {
		OrderList2Data msgData = new OrderList2Data();
		msgData.setOrderId(String.valueOf(Settings.CONTRL_ITEM_ID)); // -1:消息提示
		String msg = "";
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				// 没有找到时
				msg = "您没有预订成功的订单，您可直接选择餐厅来发送请柬";
			}
		} else {
			if (isLast == false) {
				// 不是在最后一页时
				msg = context.getString(R.string.text_info_loading);
			}
		}
		msgData.setRestName(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}

}
