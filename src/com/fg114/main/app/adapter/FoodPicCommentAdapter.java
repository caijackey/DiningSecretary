package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MySurfaceLayout;
import com.fg114.main.service.dto.CommentReplyData;
import com.fg114.main.service.dto.ResFoodCommentData;
import com.fg114.main.service.dto.ResPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.UnitUtil;

/**
 * 菜品图片评论列表适配器
 * 
 * @author xujianjun ,2012-10-18
 */
public class FoodPicCommentAdapter extends BaseAdapter {

	private static final String TAG = "FoodPicCommentAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<ResFoodCommentData> list = new ArrayList<ResFoodCommentData>();

	private boolean isLast = false;

	private Context context;
	private View.OnClickListener uploadPictureListener;
	private LayoutInflater mInflater;

	public FoodPicCommentAdapter(Context c) {
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
		TextView tvLikeType;
		TextView tvSendTime;
		TextView tvCommentContent;
		ProgressBar pbBar;
		TextView tvMsg;
		MyImageView mUserPicture;
		LineView line;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_food_image_detail_comment, null);
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.list_item_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_msgLayout);
			holder.line = (LineView) convertView.findViewById(R.id.horizontal_line);
			holder.mUserPicture = (MyImageView) convertView.findViewById(R.id.list_item_food_image_detail_comment_userphoto);
			holder.tvUserName = (TextView) convertView.findViewById(R.id.list_item_food_image_detail_comment_user_name);
			holder.tvLikeType = (TextView) convertView.findViewById(R.id.list_item_food_image_detail_comment_like_type);
			holder.tvSendTime = (TextView) convertView.findViewById(R.id.list_item_food_image_detail_comment_post_time);
			holder.tvCommentContent = (TextView) convertView.findViewById(R.id.list_item_food_image_detail_comment_content);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.list_item_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.list_item_pBar);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		ResFoodCommentData data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.line.setVisibility(View.GONE);
			holder.tvMsg.setText(data.detail);
			if (context.getString(R.string.text_info_loading).equals(data.detail)) {
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
			
			//---
			// 设置评论回复人
			if ("".equals(data.userName)) {
				holder.tvUserName.setText(R.string.text_null_hanzi);
			} else {
				holder.tvUserName.setText(data.userName);
			}
			// 添加用户头像
			holder.mUserPicture.setImageByUrl(data.userPicUrl, true, position, ScaleType.FIT_CENTER);
			holder.tvLikeType.setText(data.likeTypeName);

			// 设置评论回复时间
			if (data.createTime > 0) {
				holder.tvSendTime.setText(ConvertUtil.convertLongToDateString(data.createTime, ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}

			// 设置评论回复内容
			if (TextUtils.isEmpty(data.detail))
				holder.tvCommentContent.setText(R.string.text_layout_dish_no_comment);
			else
				holder.tvCommentContent.setText(data.detail);
			

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
		if(list == null){
			list = new ArrayList<ResFoodCommentData>();
			msg = "暂无任何菜品评论";
		}else if (list != null && list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				// 没有找到时
				msg = "暂无任何菜品评论";
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
