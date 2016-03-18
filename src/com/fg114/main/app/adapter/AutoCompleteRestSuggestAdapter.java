package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.SuggestResultData;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 关键字提示列表适配器
 * 
 * @author zhangyifan
 * 
 */
public class AutoCompleteRestSuggestAdapter extends BaseAdapter {

	private static final String TAG = "AutoCompleteRestSuggestAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<SuggestResultData> list = null;

	private LayoutInflater mInflater = null;
	private Context context;
	public boolean isReset = true; // 如果是第一次使用addlist添加数据需要设置为true
	// 是否显示添加餐厅按钮
	private boolean mIsShowAddResBtn = false;

	public AutoCompleteRestSuggestAdapter(Context c) {
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
		LinearLayout infoLayout;
		ImageView typeIcon;
		TextView tvRestName;
		// 优惠四控件
		ImageView promotion_icon_mibi;
		TextView promotion_mibi;
		TextView promotion_discount;
		TextView promotion_coupon;
		// 消息提示部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnAddNewRes;
		Button btnRetry;
		TextView hideMsg;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_auto_complete_rest_suggest, null);
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.autoComplete_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.autoComplete_msgLayout);
			holder.typeIcon = (ImageView) convertView.findViewById(R.id.autoComplete_type_icon);
			holder.tvRestName = (TextView) convertView.findViewById(R.id.autoComplete_rest_name);

			// 优惠四控件
			holder.promotion_icon_mibi = (ImageView) convertView.findViewById(R.id.promotion_icon_mibi);
			holder.promotion_mibi = (TextView) convertView.findViewById(R.id.promotion_mibi);
			holder.promotion_discount = (TextView) convertView.findViewById(R.id.promotion_discount);
			holder.promotion_coupon = (TextView) convertView.findViewById(R.id.promotion_coupon);

			holder.tvMsg = (TextView) convertView.findViewById(R.id.autoComplete_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.autoComplete_pBar);
			holder.btnAddNewRes = (Button) convertView.findViewById(R.id.autoComplete_btnAddNewRes);
			holder.btnRetry = (Button) convertView.findViewById(R.id.autoComplete_btnRetry);
			holder.hideMsg = (TextView) convertView.findViewById(R.id.autoComplete_hideMsg);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		SuggestResultData data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.restId)) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.tvMsg.setText(data.restName);

			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(data.restName)) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(data.restName);
			holder.hideMsg.setVisibility(View.GONE);

			holder.btnAddNewRes.setVisibility(View.GONE);
			holder.btnAddNewRes.setOnClickListener(null);

		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置类型图标 1:电话 2：收藏 3:下单 4：历史  5：推荐
			if (data.hisTypeTag == 1) {
				holder.typeIcon.setVisibility(View.VISIBLE);
				holder.typeIcon.setImageResource(R.drawable.icon_phone);
			} else if (data.hisTypeTag == 2) {
				holder.typeIcon.setVisibility(View.VISIBLE);
				holder.typeIcon.setImageResource(R.drawable.icon_favorite);
			} else if (data.hisTypeTag == 3) {
				holder.typeIcon.setVisibility(View.VISIBLE);
				holder.typeIcon.setImageResource(R.drawable.icon_ordered);
			} else if (data.hisTypeTag == 4) {
				holder.typeIcon.setVisibility(View.VISIBLE);
				holder.typeIcon.setImageResource(R.drawable.icon_history);
			} else if (data.hisTypeTag == 5) {
				holder.typeIcon.setVisibility(View.VISIBLE);
				holder.typeIcon.setImageResource(R.drawable.icon_hand);
			} else {
				holder.typeIcon.setVisibility(View.GONE);
			}

			// 设置关键字
			ViewUtils.setHightlightKeywords(holder.tvRestName, data.restName);

			//优惠信息
			//图标标志  0:无图标  1：券  2：惠  3：币 4：币(高亮)
			if(data.iconTag==1){
				holder.promotion_icon_mibi.setVisibility(View.GONE);
				holder.promotion_mibi.setVisibility(View.GONE);
				holder.promotion_discount.setVisibility(View.GONE);
				
				holder.promotion_coupon.setVisibility(View.VISIBLE);
				holder.promotion_coupon.setText(data.iconTitle);
			}else if(data.iconTag==2){
				holder.promotion_icon_mibi.setVisibility(View.GONE);
				holder.promotion_mibi.setVisibility(View.GONE);
				holder.promotion_coupon.setVisibility(View.GONE);
				
				holder.promotion_discount.setVisibility(View.VISIBLE);
				holder.promotion_discount.setText(data.iconTitle);
			}else if(data.iconTag==3){
				holder.promotion_discount.setVisibility(View.GONE);
				holder.promotion_coupon.setVisibility(View.GONE);
				
				holder.promotion_mibi.setVisibility(View.VISIBLE);
				holder.promotion_mibi.setText(data.iconTitle);
				holder.promotion_icon_mibi.setVisibility(View.VISIBLE);
				holder.promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_1);
			}else if(data.iconTag==4){
				holder.promotion_discount.setVisibility(View.GONE);
				holder.promotion_coupon.setVisibility(View.GONE);
				
				holder.promotion_mibi.setVisibility(View.VISIBLE);
				holder.promotion_mibi.setText(data.iconTitle);
				holder.promotion_icon_mibi.setVisibility(View.VISIBLE);
				holder.promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_2);
			}else{
				holder.promotion_icon_mibi.setVisibility(View.GONE);
				holder.promotion_mibi.setVisibility(View.GONE);
				holder.promotion_discount.setVisibility(View.GONE);
				holder.promotion_coupon.setVisibility(View.GONE);
			}

		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<SuggestResultData> getList() {
		return list;
	}

	@SuppressWarnings("unchecked")
	public void setList(List<SuggestResultData> list, boolean isLocalHistory) {
		if (list == null) {
			list = new ArrayList<SuggestResultData>();
		}

		SuggestResultData dto = new SuggestResultData();
		dto.restId = String.valueOf(Settings.CONTRL_ITEM_ID);
		// 复制列表
		if (list.size() == 0) {
			// 列表内容添加
			if (isLocalHistory) {
				// 历史记录列表的场合
				dto.restName = this.context.getString(R.string.text_info_no_search_history);
			} else {
				dto.restName = this.context.getString(R.string.text_info_no_found_by_key);
			}
		} else {
			if (isLocalHistory) {
				dto.restName = this.context.getString(R.string.text_button_clear);
			}
		}
		//如果是本地历史（搜索的关键词的历史），增加标志，以区别点击事件
		if(isLocalHistory){
			for(SuggestResultData his:list){
				his.restId=String.valueOf(Settings.CONTRL_ITEM_HISTORY_ID);
			}
		}
		isReset = true;
		// 复制参数列表
		this.list = (List<SuggestResultData>) ((ArrayList<SuggestResultData>) list).clone();
		this.list.add(dto);
		notifyDataSetChanged();
	}

	public void addList(List<SuggestResultData> list, boolean isLast) {
		// 删除最后一条消息
		if (isReset) {
			this.list.clear();
			this.list.addAll(createMsgDataToList(list, isLast));
			isReset = false;
		} else {
			this.list.remove(this.list.size() - 1);
			this.list.addAll(createMsgDataToList(list, isLast));
		}

		notifyDataSetChanged();
	}

	/**
	 * 建立最后一条数据
	 * 
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<SuggestResultData> createMsgDataToList(List<SuggestResultData> list, boolean isLast) {
		SuggestResultData msgData = new SuggestResultData();
		msgData.restId = String.valueOf(Settings.CONTRL_ITEM_ON_ID); // -1:消息提示
		String msg = "";

		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
				mIsShowAddResBtn = false;
			} else {
				msg = "没有找到相关餐厅";
				// String
				// ChannelId=SessionManager.getInstance().getFilter().getChannelId();
				// if(ChannelId.equals("1"))
				// {
				// mIsShowAddResBtn = true;
				// msgData.discount=context.getString(R.string.text_info_no_found_by_key);
				// }
				// else
				// {
				// msg=context.getString(R.string.text_info_no_found_by_key);
				// mIsShowAddResBtn=false;
				// }
			}
		} else if (isLast == false) {

			// 不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
			mIsShowAddResBtn = false;
		} else if (list.size() > 0 && isLast == true) {// 最后一页

			String ChannelId = SessionManager.getInstance().getFilter().getChannelId();
			if (ChannelId.equals("1")) {
				// msg = context.getString(R.string.text_layout_res_not_found);
				msg = "";
				mIsShowAddResBtn = true;
			} else {
				msg = "";
				mIsShowAddResBtn = false;
			}

		} else {
			mIsShowAddResBtn = false;
		}

		msgData.restName = msg;

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		mIsShowAddResBtn = false;
		return list;
	}
}
