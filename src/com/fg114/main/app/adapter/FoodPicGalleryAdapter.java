package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.fg114.main.R;
import com.fg114.main.app.Settings;

import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.RestGroupPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.UnitUtil;

/**
 * 菜品图片组列表适配器
 * 
 * @author xujianjun ,2012-10-18
 */
public class FoodPicGalleryAdapter extends BaseAdapter {

	private static final String TAG = "FoodPicGalleryAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	private static final String CONTRL_ITEM_ID = "-1234567";

	private List<RestGroupPicData> list = new ArrayList<RestGroupPicData>();

	private boolean isLast = false;

	private Context context;
	private View.OnClickListener uploadPictureListener;
	private LayoutInflater mInflater;

	public FoodPicGalleryAdapter(Context c) {
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
		RelativeLayout infoLayout;
		LinearLayout msgLayout;
		TextView userName;
		TextView postTime;
		ProgressBar pbBar;
		TextView tvMsg;
		MyImageView picture;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_food_image_detail_gallery, null);
			holder.infoLayout = (RelativeLayout) convertView.findViewById(R.id.list_item_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.list_item_msgLayout);
			//
			holder.picture = (MyImageView) convertView.findViewById(R.id.list_item_food_image_gallery_photo);
			holder.userName = (TextView) convertView.findViewById(R.id.list_item_food_image_gallery_user_name);
			holder.postTime = (TextView) convertView.findViewById(R.id.list_item_food_image_gallery_post_time);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.list_item_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.list_item_pBar);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		float screenDip=UnitUtil.px2dip(UnitUtil.getScreenWidthPixels());
		float gapDip=13; 
		LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) holder.infoLayout.getLayoutParams();
		lp.width=UnitUtil.dip2px(screenDip-gapDip*2-12);
		
		lp.setMargins(UnitUtil.dip2px(gapDip), 0, UnitUtil.dip2px(gapDip), 0);
		holder.infoLayout.setLayoutParams(lp);

		// 列表内容设置
		RestGroupPicData data = list.get(position);

		if (String.valueOf(CONTRL_ITEM_ID).equals(data.getUuid())) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.tvMsg.setText(data.getUploader());
			if (context.getString(R.string.text_info_loading).equals(data.getUploader())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}

		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);
			
			//---
			// 设置上传人
			holder.userName.setText(data.getUploader());
				
			// 设置上传时间
			if (data.getUploadTime() > 0) {
				holder.postTime.setText("上传于"+ConvertUtil.convertLongToDateString(data.getUploadTime(), ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}
			// 设置图片
			holder.picture.setImageByUrl(data.getPicUrl(), true, position, ScaleType.FIT_XY);


		}
		
		convertView.setTag(holder);
		return convertView;
	}

	public List<RestGroupPicData> getList() {
		return list;
	}

	public void setList(List<RestGroupPicData> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void setList(List<RestGroupPicData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<RestGroupPicData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<RestGroupPicData> list, boolean isLast) {
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
	private List<RestGroupPicData> createMsgDataToList(List<RestGroupPicData> list, boolean isLast) {

		RestGroupPicData msgData = new RestGroupPicData();
		msgData.setUuid(String.valueOf(CONTRL_ITEM_ID)); // -1:消息提示
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
		msgData.setUploader(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}
}
