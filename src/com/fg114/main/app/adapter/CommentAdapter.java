package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentDetailActivity;
import com.fg114.main.app.view.CommentImageHorizontalScrollView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 评论列表适配器
 * 
 * @author zhangyifan
 * 
 */
public class CommentAdapter extends BaseAdapter {

	private static final String TAG = "CommentAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<CommentData> list = null;
	public Set<MyImageView> viewList = new HashSet<MyImageView>();;
	private LayoutInflater mInflater = null;
	private Context context;
	private String restaurantId; // 餐厅ID，跳转到评论回复时需要传递到下一页

	public CommentAdapter(Context c) {
		this(c, null);
	}

	public CommentAdapter(Context c, String restaurantId) {
		super();
		this.context = c;
		this.restaurantId = restaurantId;
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
		TextView tvLikeOrDislike;
		TextView tvTasteRating;
		TextView tvEnvironmentRating;
		TextView tvSerivceRating;
		TextView tvCommentContent;
		ProgressBar pbBar;
		TextView tvMsg;
		MyImageView mUserPicture;
		LinearLayout mStarLayout;// 评分栏
		LinearLayout mPhotoLayout;// 来自随手拍
		//4.1.4及以前版本是固定3张图片，后面改成动态增加图片
		CommentImageHorizontalScrollView imageHorizontalScrollView;
		
		TextView mNoRate;
		TextView replyButton;
		ImageView replyClientIcon;
		TextView replyClient;
		LinearLayout replyLayout;
		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			//餐厅详情和评论列表使用同一个adapter，但布局文件不一样
			if(MainFrameActivity.getCurrentTopActivity().getClass()==RestaurantCommentActivity.class){				
				convertView = mInflater.inflate(R.layout.list_item_comment2, null);
			}else{ 
				convertView = mInflater.inflate(R.layout.list_item_comment1, null);
			}
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_msgLayout);
			holder.tvUserName = (TextView) convertView.findViewById(R.id.list_item_comment_tvUser);
			holder.mUserPicture = (MyImageView) convertView.findViewById(R.id.list_item_comment_userphoto);
			holder.mStarLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_rbStarlayout);
			holder.tvSendTime = (TextView) convertView.findViewById(R.id.list_item_comment_tvTime);
			holder.tvLikeOrDislike = (TextView) convertView.findViewById(R.id.list_item_comment_he_likes);
			holder.tvTasteRating = (TextView) convertView.findViewById(R.id.list_item_comment_tvTaste);
			holder.tvEnvironmentRating = (TextView) convertView.findViewById(R.id.list_item_comment_tvEnvironment);
			holder.tvSerivceRating = (TextView) convertView.findViewById(R.id.list_item_comment_tvSerivce);
			holder.tvCommentContent = (TextView) convertView.findViewById(R.id.list_item_comment_tvComment);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.list_item_comment_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.list_item_comment_pBar);
			holder.imageHorizontalScrollView = (CommentImageHorizontalScrollView) convertView.findViewById(R.id.list_item_comment_imageScrollView);
			
			holder.mPhotoLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_photolayout);
			holder.mNoRate = (TextView) convertView.findViewById(R.id.list_item_comment_tvNoRate);
			// --
			holder.replyLayout = (LinearLayout) convertView.findViewById(R.id.list_item_comment_replyLayout);
			holder.replyButton = (TextView) convertView.findViewById(R.id.list_item_comment_replyButton);
			holder.replyClientIcon = (ImageView)convertView.findViewById(R.id.list_item_comment_replyClientIcon);
			holder.replyClient = (TextView) convertView.findViewById(R.id.list_item_comment_replyClient);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.imageHorizontalScrollView.clearImageData();
		}
		holder.imageHorizontalScrollView.setVisibility(View.GONE);
		// 列表内容设置
		final CommentData data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
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
			// 设置评论人
			if ("".equals(data.userName.trim())) {
				holder.tvUserName.setText(R.string.text_null_hanzi);
			} else {
				holder.tvUserName.setText(data.userName.trim());
			}
			// 添加用户头像
			viewList.add(holder.mUserPicture);
			holder.mUserPicture.setImageByUrl(data.userSmallPicUrl, true, position, ScaleType.CENTER_CROP);
			
			//设置喜欢，不喜欢
			if(data.likeTag){
				holder.tvLikeOrDislike.setText("他喜欢");
				holder.tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.attention_recommen_restaurant_2), null, null, null);			
			}else{
				holder.tvLikeOrDislike.setText("他不喜欢");
				holder.tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.attention_recommen_restaurant_1), null, null, null);
			}
			// 添加底部菜品图片
			List<CommentPicData> picDatas = data.picList;
			//如果imagelistgroup里面有图片就不需要添加，防止添加多次
			if (picDatas.size() > 0 && holder.imageHorizontalScrollView.getImageCount()==0) {
				holder.imageHorizontalScrollView.setVisibility(View.VISIBLE);
				if (picDatas.size() > 8) {
					picDatas = picDatas.subList(0, 8);
				}
				holder.imageHorizontalScrollView.setImageData(picDatas);
			} 

			// 设置评论时间
			if (data.createTime > 0) {
				holder.tvSendTime.setText(ConvertUtil.convertLongToDateString(data.createTime, ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}

			// 是否有评分
			if (data.gradeTag) {
				holder.mStarLayout.setVisibility(View.VISIBLE);
				holder.mPhotoLayout.setVisibility(View.GONE);

				// 设置口味
				holder.tvTasteRating.setText(String.valueOf((int) data.tasteNum));
				// 设置环境
				holder.tvEnvironmentRating.setText(String.valueOf((int) data.envNum));
				// 设置服务
				holder.tvSerivceRating.setText(String.valueOf((int) data.serviceNum));
			} else {
				holder.mStarLayout.setVisibility(View.GONE);
				holder.mPhotoLayout.setVisibility(View.VISIBLE);
				holder.mNoRate.setText(data.noGradeIntro);
			}

			// 设置评论内容
			if (TextUtils.isEmpty(data.detail))
				holder.tvCommentContent.setText(R.string.text_layout_dish_no_comment);
			else
				holder.tvCommentContent.setText(data.detail);

			// ----如果有id, 显示回复按钮
			if (restaurantId != null) {
				holder.replyLayout.setVisibility(View.VISIBLE);
			} else {
				holder.replyLayout.setVisibility(View.GONE);
			}
			//点击“我要回复”进入评论详细页
			holder.replyLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bundle = new Bundle();
					bundle.putSerializable(Settings.BUNDLE_REST_COMMENT_DATA, data);
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					ActivityUtil.jump(context, RestaurantCommentDetailActivity.class, 0, bundle);

				}
			});

			holder.replyButton.setText("回复 (" + data.replyNum + ")");
			holder.replyClient.setText(data.clientName);
			if (CheckUtil.isEmpty(data.clientName)) {
				holder.replyClientIcon.setVisibility(View.INVISIBLE);
				holder.replyClient.setVisibility(View.INVISIBLE);
			} else {
				holder.replyClientIcon.setVisibility(View.VISIBLE);
				holder.replyClient.setVisibility(View.VISIBLE);
			}
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<CommentData> getList() {
		return list;
	}

	public void setList(List<CommentData> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void setList(List<CommentData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<CommentData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<CommentData> list, boolean isLast) {
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
	private List<CommentData> createMsgDataToList(List<CommentData> list, boolean isLast) {

		CommentData msgData = new CommentData();
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
				msg = "发表评论，与大家分享餐厅信息，立即获得最高150秘币";
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

	private class callPicView implements OnClickListener {
		private View parentView;
		private String url;

		public callPicView(View parentView, String url) {
			this.parentView = parentView;
			this.url = url;
		}

		@Override
		public void onClick(View v) {
			DialogUtil.createImageViewPanel((Activity) context, this.parentView, url);
		}

	}
}
