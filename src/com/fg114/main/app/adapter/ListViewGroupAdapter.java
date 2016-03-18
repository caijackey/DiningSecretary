package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fg114.main.R;

import com.fg114.main.app.Settings;
import com.fg114.main.app.view.WaterFallImageView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.service.dto.ResPicData2;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.HanziUtil;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ListViewGroupAdapter extends BaseAdapter {

	private Context mContext;
	private List<ResPicData2> dataList = new ArrayList<ResPicData2>();
	public Set<WaterFallImageView> viewList = new HashSet<WaterFallImageView>();
	private int ItemWidth;

	public ListViewGroupAdapter(Context context, int itemwidth) {
		this.mContext = context;
		this.ItemWidth = itemwidth;
	}

	private class ViewHolder {

		FrameLayout imgFrameMain;
		WaterFallImageView ResImage;
		TextView ResInfoTv;
		TextView HotNumTv;
		TextView PicNumTv;
		MyImageView UserPic;
		TextView UserCommentTv;
		Button MoreInfoBtn;

	}

	@Override
	public int getCount() {

		if (dataList != null) {
			return dataList.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {

		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public synchronized final List<ResPicData2> getDataList() {
		return dataList;
	}

	public synchronized final void setDataList(List<ResPicData2> dataList, boolean isLast) {
		if (dataList == null) {
			dataList = new ArrayList<ResPicData2>();
		}
		this.dataList = dataList;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(this.mContext, R.layout.flow_content_view, null);
			holder = new ViewHolder();
			holder.imgFrameMain = (FrameLayout) convertView.findViewById(R.id.flow_content_frameMain);
			holder.HotNumTv = (TextView) convertView.findViewById(R.id.flow_content_HotNumTv);
			holder.PicNumTv = (TextView) convertView.findViewById(R.id.flow_content_PicNum);
			holder.UserPic = (MyImageView) convertView.findViewById(R.id.flow_content_userPic);
			holder.UserCommentTv = (TextView) convertView.findViewById(R.id.flow_content_user_comment);
			holder.MoreInfoBtn = (Button) convertView.findViewById(R.id.flow_content_more_infoBtn);
			// holder.ResImage = (WaterFallImageView)
			// convertView.findViewById(R.id.flow_content_myimageView);
			holder.ResInfoTv = (TextView) convertView.findViewById(R.id.flow_content_picTextView);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 列表内容设置
		final ResPicData2 data = dataList.get(position);

		int width = dataList.get(position).getSmallPicWidth();
		int height = dataList.get(position).getSmallPicHeight();
		int layoutHeight = (height * ItemWidth) / width;// 调整高度
		android.widget.FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ItemWidth, layoutHeight);
		holder.ResImage.setLayoutParams(layoutParams);
		//
		holder.ResImage.setMinimumHeight(layoutHeight);
		holder.ResImage.setMinimumWidth(ItemWidth);

		holder.ResImage.setPadding(2, 2, 2, 2);
		holder.ResImage.setBackgroundResource(R.drawable.bg_index_new_block);

		holder.ResImage.setImageByUrl(dataList.get(position).getSmallPicUrl(), true, position, ScaleType.FIT_XY);
		// holder.ResImage.setImageResource(R.drawable.bg_booking_res_search);
		viewList.add(holder.ResImage);

		// 设置半透明背景的图片名
		holder.ResInfoTv.setBackgroundResource(R.color.background_color_transparent);
		holder.ResInfoTv.setText(dataList.get(position).getName());
		holder.ResInfoTv.setTextColor(mContext.getResources().getColor(R.color.background_color_white));
		holder.ResInfoTv.setTextSize(18);
		android.widget.FrameLayout.LayoutParams layoutParamsTv = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParamsTv.gravity = Gravity.BOTTOM;
		holder.ResInfoTv.setPadding(2, 2, 2, 2);
		holder.ResInfoTv.setLayoutParams(layoutParamsTv);

		holder.ResInfoTv.setGravity(Gravity.CENTER_VERTICAL);

		holder.HotNumTv.setText(dataList.get(position).getHotNum() + "");
		holder.PicNumTv.setText(dataList.get(position).getGroupPicNum() + "");
		// holder.UserPic.setImageByUrl(dataList.get(position).getUserPic(),
		// true, position, ScaleType.CENTER);
		holder.UserPic.setImageResource(R.drawable.user_center_bt01);
		holder.UserCommentTv.setText(dataList.get(position).getCommentUserName() + ":" + dataList.get(position).getCommentDetail());

		convertView.setTag(holder);

		return convertView;

	}

	public void addList(List<ResPicData2> list, boolean isLast) {
		// 删除最后一条消息

		this.dataList.addAll(list);
		notifyDataSetChanged();
	}

}
