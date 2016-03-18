package com.fg114.main.app.adapter;

import java.io.Serializable;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RecommendRestaurantGalleryActivity;
import com.fg114.main.app.adapter.RecommendResInfoAdapter.ViewHolder;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.RestRecomInfoData3;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class RestaurantDetailRecommendAdapter extends BaseAdapter {
	private List<RestRecomPicData> picList;
	private RestRecomInfoData3 restRecomInfoData;
	private LayoutInflater mInflater = null;
	private Context context;
	private int screenWidth;
	private int itemPicWidth = 0;

	public RestaurantDetailRecommendAdapter(Context c) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		screenWidth = UnitUtil.getScreenWidthPixels();
		itemPicWidth = (screenWidth - UnitUtil.dip2px(20));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return picList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return picList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.restaurant_detail_recommend_list_item, null);
			holder.rest_picture = (MyImageView) convertView.findViewById(R.id.rest_picture);
			holder.rest_text = (TextView) convertView.findViewById(R.id.rest_text);
			// holder.rest_name=(TextView)
			// convertView.findViewById(R.id.rest_name);
			holder.list_item_dishorderLayout = convertView.findViewById(R.id.list_item_dishorderLayout);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//
		if (CheckUtil.isEmpty(picList.get(position).picUrl)) {
			holder.rest_picture.setImageResource(ImageUtil.loading);
		} else {
			double scale = 1.0000 * itemPicWidth / picList.get(position).picWidth;
			int itemPicHeight = (int) (picList.get(position).picHeight * scale);
			holder.rest_picture.setLayoutParams(new RelativeLayout.LayoutParams(itemPicWidth, itemPicHeight));
			holder.rest_picture.setImageByUrl(picList.get(position).picUrl, true, 0, ScaleType.FIT_XY);
		}

		holder.rest_text.setText(picList.get(position).detail);

		holder.list_item_dishorderLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("推荐图片-点击");
				// -----
                ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_ID, position);
				bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, (Serializable) getList());
				ActivityUtil.jump(context, RecommendRestaurantGalleryActivity.class, 0, bundle);
			}
		});

		convertView.setTag(holder);
		return convertView;
	}

	public static class ViewHolder {
		private MyImageView rest_picture;
		private TextView rest_text;
		// private TextView rest_name;
		private View list_item_dishorderLayout;
	}

	public List<RestRecomPicData> getList() {
		return picList;
	}

	public void setList(RestRecomInfoData3 restRecomInfoData) {
		this.picList = restRecomInfoData.picList;
		this.restRecomInfoData = restRecomInfoData;
		this.notifyDataSetChanged();

	}

}
