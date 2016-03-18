package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.ResFoodCommentData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ConvertUtil;

/**
 * 菜品评论列表适配器
 * 
 * @author xu jianjun, 2012-01-07
 * 
 */
public class FoodCommentListAdapter extends BaseAdapter {

	private static final String TAG = "FoodCommentAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<ResFoodCommentData> list = null;

	private LayoutInflater mInflater = null;
	private Context context;

	// 评价类型图标
	private int[] likeTypeIds = new int[] { 0, R.drawable.food_comment_like, R.drawable.food_comment_general, R.drawable.food_comment_dislike };

	public FoodCommentListAdapter(Context c) {
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
		LinearLayout msgLayout;
		TextView userName;
		TextView createTime;
		TextView commentContent;
		TextView msg;
		ProgressBar pbBar;
		ImageView likeType;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_food_comment, null);
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.list_item_food_comment_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_food_comment_msgLayout);
			holder.userName = (TextView) convertView.findViewById(R.id.list_item_food_comment_userName);
			holder.createTime = (TextView) convertView.findViewById(R.id.list_item_food_comment_createTime);
			holder.commentContent = (TextView) convertView.findViewById(R.id.list_item_food_comment_detail);
			holder.msg = (TextView) convertView.findViewById(R.id.list_item_food_comment_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.list_item_food_comment_pBar);
			holder.likeType = (ImageView) convertView.findViewById(R.id.list_item_food_comment_likeType);
			/*
			 * // holder.userName.setText("小丘陵");
			 * holder.createTime.setText("2001-01-01"); holder.commentContent
			 * .setText(
			 * "我没有什么话可以评论，无言!我没有什么话可以评论，无言!我没有什么话可以评论，无言!我没有什么话可以评论，无言!我没有什么话可以评论，无言!"
			 * ); holder.msg.setText("没有东西");
			 * holder.pbBar.setVisibility(View.VISIBLE);
			 * holder.msgLayout.setVisibility(View.VISIBLE);
			 */

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ResFoodCommentData data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.msg.setText(data.detail);
			if (context.getString(R.string.text_info_loading).equals(data.detail)) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置评论人
			if ("".equals(data.userName.trim())) {
				holder.userName.setText(R.string.text_null_hanzi);
			} else {
				holder.userName.setText(data.userName.trim());
			}

			// 设置评论时间
			if (data.createTime > 0) {

				holder.createTime.setText(ConvertUtil.convertLongToDateString(data.createTime, ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}
			// 设置评论图标
			try {
				if (data.likeTypeTag < 1) {
					holder.likeType.setVisibility(View.INVISIBLE);
				} else {
					holder.likeType.setVisibility(View.INVISIBLE);
					holder.likeType.setImageResource(likeTypeIds[data.likeTypeTag]);
				}

			} catch (Exception ex) {
			}

			// 设置评论内容
			holder.commentContent.setText(data.detail);
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<ResFoodCommentData> getList() {
		return list;
	}

	public void setList(List<ResFoodCommentData> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void setList(List<ResFoodCommentData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<ResFoodCommentData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<ResFoodCommentData> list, boolean isLast) {
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
	private List<ResFoodCommentData> createMsgDataToList(List<ResFoodCommentData> list, boolean isLast) {

		ResFoodCommentData msgData = new ResFoodCommentData();
		msgData.uuid=String.valueOf(Settings.CONTRL_ITEM_ID); // -1:消息提示
		String msg = "";
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				// 没有找到时
				msg = "暂无任何点评";
			}
		} else {
			if (isLast == false) {
				// 不是在最后一页时
				msg = context.getString(R.string.text_info_loading);
			}
		}
		msgData.detail=msg;

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}
}
