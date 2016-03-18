/**
 * 此组件使用方法见RestaurantDetailActivity
 * 此组件适合非listviev里面添加comment
 * 原因是，餐厅详情页合并成左右滑动的页面后，左边底部点击添加评论，键盘出来后整个餐厅详情页面moveup的速度很慢
 * 
 * 请注意，此组件跟CommentAdapter里面部分内容重复， CommentAdapter适合listview里面添加comment
 */
package com.fg114.main.app.view;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantCommentDetailActivity;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.ViewUtils;

public class CommentItem extends LinearLayout {
	private Context context;

	private String restaurantId; // 餐厅ID，跳转到评论回复时需要传递到下一页
	
	public CommentItem(Context context, CommentData data, String resId) {
		super(context);
		this.context=context;
		restaurantId=resId;
		LinearLayout.inflate(context, R.layout.list_item_comment1, this);
		if (data != null)
			setCommentData(data);
	}

	public void setCommentData(final CommentData data) {
		LinearLayout infoLayout = (LinearLayout) this.findViewById(R.id.list_item_comment_infoLayout);
		LinearLayout msgLayout = (LinearLayout) this.findViewById(R.id.list_item_comment_msgLayout);
		TextView tvUserName = (TextView) this.findViewById(R.id.list_item_comment_tvUser);
		MyImageView mUserPicture = (MyImageView) this.findViewById(R.id.list_item_comment_userphoto);
		LinearLayout mStarLayout = (LinearLayout) this.findViewById(R.id.list_item_comment_rbStarlayout);
		TextView tvSendTime = (TextView) this.findViewById(R.id.list_item_comment_tvTime);
		TextView tvLikeOrDislike = (TextView) this.findViewById(R.id.list_item_comment_he_likes);
		TextView tvTasteRating = (TextView) this.findViewById(R.id.list_item_comment_tvTaste);
		TextView tvEnvironmentRating = (TextView) this.findViewById(R.id.list_item_comment_tvEnvironment);
		TextView tvSerivceRating = (TextView) this.findViewById(R.id.list_item_comment_tvSerivce);
		TextView tvCommentContent = (TextView) this.findViewById(R.id.list_item_comment_tvComment);
		TextView tvMsg = (TextView) this.findViewById(R.id.list_item_comment_tvMsg);
		ProgressBar pbBar = (ProgressBar) this.findViewById(R.id.list_item_comment_pBar);
		CommentImageHorizontalScrollView imageHorizontalScrollView = (CommentImageHorizontalScrollView) this.findViewById(R.id.list_item_comment_imageScrollView);

		LinearLayout mPhotoLayout = (LinearLayout) this.findViewById(R.id.list_item_comment_photolayout);
		TextView mNoRate = (TextView) this.findViewById(R.id.list_item_comment_tvNoRate);
		// --
		LinearLayout replyLayout = (LinearLayout) this.findViewById(R.id.list_item_comment_replyLayout);
		TextView replyButton = (TextView) this.findViewById(R.id.list_item_comment_replyButton);
		ImageView replyClientIcon = (ImageView) this.findViewById(R.id.list_item_comment_replyClientIcon);
		TextView replyClient = (TextView) this.findViewById(R.id.list_item_comment_replyClient);

		imageHorizontalScrollView.setVisibility(View.GONE);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
			// 提示信息的场合
			infoLayout.setVisibility(View.GONE);
			msgLayout.setVisibility(View.VISIBLE);
			tvMsg.setText(data.detail);
			if (context.getString(R.string.text_info_loading).equals(data.detail)) {
				// 载入的场合
				pbBar.setVisibility(View.VISIBLE);
			} else {
				pbBar.setVisibility(View.GONE);
			}
		} else {

			infoLayout.setVisibility(View.VISIBLE);
			msgLayout.setVisibility(View.GONE);
			// 设置评论人
			if ("".equals(data.userName.trim())) {
				tvUserName.setText(R.string.text_null_hanzi);
			} else {
				tvUserName.setText(data.userName.trim());
			}
			// 添加用户头像
			mUserPicture.setImageByUrl(data.userSmallPicUrl, true, 0, ScaleType.CENTER_CROP);

			//设置喜欢，不喜欢
			if (data.likeTag) {
				tvLikeOrDislike.setText("他喜欢");
				tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.attention_recommen_restaurant_2), null, null, null);
			} else {
				tvLikeOrDislike.setText("他不喜欢");
				tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.attention_recommen_restaurant_1), null, null, null);
			}
			// 添加底部菜品图片
			List<CommentPicData> picDatas = data.picList;
			//如果imagelistgroup里面有图片就不需要添加，防止添加多次
			if (picDatas.size() > 0 && imageHorizontalScrollView.getImageCount() == 0) {
				imageHorizontalScrollView.setVisibility(View.VISIBLE);
				if (picDatas.size() > 8) {
					picDatas = picDatas.subList(0, 8);
				}
				imageHorizontalScrollView.setImageData(picDatas);
			}

			// 设置评论时间
			if (data.createTime > 0) {
				tvSendTime.setText(ConvertUtil.convertLongToDateString(data.createTime, ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}

			// 是否有评分
			if (data.gradeTag) {
				mStarLayout.setVisibility(View.VISIBLE);
				mPhotoLayout.setVisibility(View.GONE);

				// 设置口味
				tvTasteRating.setText(String.valueOf((int) data.tasteNum));
				// 设置环境
				tvEnvironmentRating.setText(String.valueOf((int) data.envNum));
				// 设置服务
				tvSerivceRating.setText(String.valueOf((int) data.serviceNum));
			} else {
				mStarLayout.setVisibility(View.GONE);
				mPhotoLayout.setVisibility(View.VISIBLE);
				mNoRate.setText(data.noGradeIntro);
			}

			// 设置评论内容
			if (TextUtils.isEmpty(data.detail))
				tvCommentContent.setText(R.string.text_layout_dish_no_comment);
			else
				tvCommentContent.setText(data.detail);

			// ----如果有id, 显示回复按钮
			if (restaurantId != null) {
				replyLayout.setVisibility(View.VISIBLE);
			} else {
				replyLayout.setVisibility(View.GONE);
			}
			//点击“我要回复”进入评论详细页
			replyLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bundle = new Bundle();
					bundle.putSerializable(Settings.BUNDLE_REST_COMMENT_DATA, data);
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					ActivityUtil.jump(context, RestaurantCommentDetailActivity.class, 0, bundle);

				}
			});

			replyButton.setText("回复 (" + data.replyNum + ")");
			replyClient.setText(data.clientName);
			if (CheckUtil.isEmpty(data.clientName)) {
				replyClientIcon.setVisibility(View.INVISIBLE);
				replyClient.setVisibility(View.INVISIBLE);
			} else {
				replyClientIcon.setVisibility(View.VISIBLE);
				replyClient.setVisibility(View.VISIBLE);
			}
		}

	}

}
