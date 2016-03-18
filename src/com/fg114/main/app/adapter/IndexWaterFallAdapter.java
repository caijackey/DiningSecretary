package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.util.ViewUtils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 首页瀑布流adapter
 * @author xujianjun,2013-10-20
 *
 */
public class IndexWaterFallAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Context mContext;
	private List<RestRecomPicData> mList = new ArrayList<RestRecomPicData>();

	
	public IndexWaterFallAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	private class ViewHolder {
		public MyImageView picture;
		public TextView description;
		public FrameLayout content_layout;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_waterfall_index_recom_rest, null);
			holder = new ViewHolder();
			holder.picture = (MyImageView) convertView.findViewById(R.id.picture);
			holder.description = (TextView) convertView.findViewById(R.id.description);
			holder.content_layout = (FrameLayout) convertView.findViewById(R.id.content_layout);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RestRecomPicData data = mList.get(position);
		holder.picture.setImageByUrl(data.picUrl, true, position, ScaleType.FIT_XY);
		holder.description.setText(data.title);
		//设置大小
		LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(data.picWidth,data.picHeight);
		convertView.setLayoutParams(lp);
		//
		holder.picture.setTag(data);
		holder.picture.setOnClickListener(listener);
		return convertView;
	}
	
	public void setList(List<RestRecomPicData> list) {
		mList = list;
		notifyDataSetChanged();
	}
	public void addList(List<RestRecomPicData> list) {
		mList.addAll(list);
		notifyDataSetChanged();
	}
	
	View.OnClickListener listener=new OnClickListener() {
		@Override
		public void onClick(View view) {
			ViewUtils.preventViewMultipleClick(view, 1000);
			RestRecomPicData data=(RestRecomPicData) view.getTag();
			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_REST_ID, data.uuid);
			bundle.putInt(Settings.BUNDLE_showTypeTag, 2);
			// -----
			OpenPageDataTracer.getInstance().addEvent("选择行", data.uuid);
			// -----
			ActivityUtil.jump((Activity)mContext, RestaurantDetailMainActivity.class, 0, bundle);
		}
	};
}
