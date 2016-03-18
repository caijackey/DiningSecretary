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
import com.fg114.main.service.dto.TakeoutRestListData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;

/**
 * 叫外卖餐厅列表适配器
 * @deprecated
 * @author xujianjun,2012-06-28
 * 
 */
public class TakeAwayRestaurantListAdapter extends BaseAdapter {

	private static final String TAG = "TakeAwayRestaurantListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<TakeoutRestListData> list = null;
	private LayoutInflater mInflater = null;
	private Context context;

	public TakeAwayRestaurantListAdapter(Context c) {
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
		LinearLayout mainLayout;
		TextView tvResName;
		TextView tvServiceTime; //外送时间
		TextView tvMinCount;  //起送
		TextView tvPhone;
		TextView tvDistance; //距离
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
			convertView = mInflater.inflate(R.layout.take_away_restaurant_list_item, null);
			holder.mainLayout = (LinearLayout) convertView.findViewById(R.id.mainLayout);
			holder.tvResName = (TextView) convertView.findViewById(R.id.rest_name);
			holder.tvServiceTime = (TextView) convertView.findViewById(R.id.service_time);
			holder.tvMinCount = (TextView) convertView.findViewById(R.id.min_count);
			holder.tvPhone = (TextView) convertView.findViewById(R.id.phone);
			holder.tvDistance = (TextView) convertView.findViewById(R.id.distance);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.msgLayout);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.message);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		final TakeoutRestListData data = list.get(position);

		if (!data.getUuid().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
			// 餐馆的场合
			holder.mainLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置餐馆名字
			if ("".equals(data.getName().trim())) {
				holder.tvResName.setText(R.string.text_null_hanzi);
			} else {
				holder.tvResName.setText(data.getName());
			}
			//背景，如果是打过电话的，背景加红条
			if(data.isHaveCallTag()){
				holder.mainLayout.setBackgroundResource(R.drawable.order_list_red_bt);
			}else{
				holder.mainLayout.setBackgroundResource(R.drawable.order_list_bt);
			}
			//修正padding
			holder.mainLayout.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));
			// 设置时间
			holder.tvServiceTime.setText(data.getOpenTime());

			holder.tvMinCount.setText(data.getSendLimit());
			holder.tvPhone.setText(data.getPhone());
			holder.tvDistance.setText(data.getDistanceMeter());
			
//			holder.mainLayout.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					Bundle bundle = new Bundle();
//					bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, Settings.TAKE_AWAY_REST_LIST_ACTIVITY);
//					bundle.putString(Settings.BUNDLE_REST_ID, data.getUuid());
//					ActivityUtil.jump(context, TakeAwayRestaurantMenuActivity.class, Settings.TAKE_AWAY_REST_LIST_ACTIVITY, bundle);
//				}
//			});

		} else {
			// 消息的场合
			holder.mainLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(data.getName())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(data.getName());
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<TakeoutRestListData> getList() {
		return list;
	}

	public void setList(List<TakeoutRestListData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<TakeoutRestListData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<TakeoutRestListData> list, boolean isLast) {
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
	private List<TakeoutRestListData> createMsgDataToList(List<TakeoutRestListData> list, boolean isLast) {
		TakeoutRestListData msgData = new TakeoutRestListData();
		msgData.setUuid(String.valueOf(Settings.CONTRL_ITEM_ID)); // -1:消息提示
		String msg = "";
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				// 没有找到时
				msg = "暂无外卖信息";
			}
		} else {
			if (isLast == false) {
				// 不是在最后一页时
				msg = context.getString(R.string.text_info_loading);
			}
		}
		msgData.setName(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}

}
