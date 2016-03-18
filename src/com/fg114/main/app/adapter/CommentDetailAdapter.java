package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.test.UiThreadTest;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantCommentDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.CommentReplyData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.UnitUtil;
import com.google.xiaomishujson.annotations.Until;

/**
 * 评论回复列表适配器
 * 
 * @author xujianjun
 * 
 */
public class CommentDetailAdapter extends BaseAdapter {

	private static final String TAG = "CommentAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<CommentReplyData> list = null;
	public Set<MyImageView> viewList = new HashSet<MyImageView>();

	private LayoutInflater mInflater = null;
	private Context context;

	public CommentDetailAdapter(Context c) {
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
		TextView tvUserName;
		TextView tvSendTime;
		TextView tvCommentContent;
		ProgressBar pbBar;
		TextView tvMsg;
		MyImageView mUserPicture;
		//LinearLayout infoLayoutTail;
		LineView line;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_comment_detail_reply, null);
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_infoLayout);
			//holder.infoLayoutTail = (LinearLayout) convertView.findViewById(R.id.list_item_comment_infoLayout_tail);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_msgLayout);
			holder.line = (LineView) convertView.findViewById(R.id.horizontal_line);
			holder.tvUserName = (TextView) convertView.findViewById(R.id.list_item_comment_tvUser);
			holder.mUserPicture = (MyImageView) convertView.findViewById(R.id.list_item_comment_userphoto);
			holder.tvSendTime = (TextView) convertView.findViewById(R.id.list_item_comment_tvTime);
			holder.tvCommentContent = (TextView) convertView.findViewById(R.id.list_item_comment_tvComment);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.list_item_comment_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.list_item_comment_pBar);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		CommentReplyData data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.getUuid())) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
//			holder.infoLayoutTail.setVisibility(View.GONE);
			holder.line.setVisibility(View.GONE);
			holder.tvMsg.setText(data.getDetail());
			if (context.getString(R.string.text_info_loading).equals(data.getDetail())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}

		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			//分隔线
			if(position<list.size()-1){
				holder.line.setVisibility(View.VISIBLE);
			}else{
				holder.line.setVisibility(View.GONE);
			}
//			//--背景
//			if(position==0){
//				holder.infoLayout.setBackgroundResource(R.drawable.comment_reply_bg_top);
//				holder.infoLayout.setPadding(UnitUtil.dip2px(12), UnitUtil.dip2px(15), UnitUtil.dip2px(15), UnitUtil.dip2px(0));
//			}else{
//				holder.infoLayout.setBackgroundResource(R.drawable.comment_reply_bg_middle);
//				holder.infoLayout.setPadding(UnitUtil.dip2px(12), UnitUtil.dip2px(15), UnitUtil.dip2px(15), UnitUtil.dip2px(0));
//			}
//			//闭合背景
//			if(position==list.size()-1){
//				holder.infoLayoutTail.setVisibility(View.VISIBLE);
//			}else{
//				holder.infoLayoutTail.setVisibility(View.GONE);
//			}			
			//---
			// 设置评论回复人
			if ("".equals(data.getUserName().trim())) {
				holder.tvUserName.setText(R.string.text_null_hanzi);
			} else {
				holder.tvUserName.setText(data.getUserName().trim());
			}
			// 添加用户头像
			viewList.add(holder.mUserPicture);
			holder.mUserPicture.setImageByUrl(data.getUserSmallPicUrl(), true, position, ScaleType.FIT_CENTER);
			

			// 设置评论回复时间
			if (data.getCreateTime() > 0) {
				holder.tvSendTime.setText(ConvertUtil.convertLongToDateString(data.getCreateTime(), ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}

			// 设置评论回复内容
			if (TextUtils.isEmpty(data.getDetail()))
				holder.tvCommentContent.setText(R.string.text_layout_dish_no_comment);
			else
				holder.tvCommentContent.setText(data.getDetail());
			

		}
		
		convertView.setTag(holder);
		return convertView;
	}

	public List<CommentReplyData> getList() {
		return list;
	}

	public void setList(List<CommentReplyData> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void setList(List<CommentReplyData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<CommentReplyData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<CommentReplyData> list, boolean isLast) {
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
	private List<CommentReplyData> createMsgDataToList(List<CommentReplyData> list, boolean isLast) {

		CommentReplyData msgData = new CommentReplyData();
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
				msg = "";
			}
		} else {
			if (isLast == false) {
				// 不是在最后一页时
				msg = context.getString(R.string.text_info_loading);
			}
		}
		msgData.setDetail(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}
}
