package com.fg114.main.app.adapter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RecommendRestaurantGalleryActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 推荐餐厅适配器
 * 
 * @author lijian
 * 
 */
public class RecommendResInfoAdapter extends BaseAdapter {

	private static final String TAG = "RecommendResAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<RestRecomPicData> list = null;

	private LayoutInflater mInflater = null;
	private Context context;
	// private int leftItemHeight = 0;;
	// private int rightItemHeight = 0;;
	private int itemPicWidth = 0;
	private int screenWidth;
	private List<MyImageViewDefine> arrayList;

	public List<MyImageViewDefine> getArrayList() {
		return arrayList;
	}

	public RecommendResInfoAdapter(Context c) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		screenWidth = UnitUtil.getScreenWidthPixels();
		itemPicWidth = (screenWidth - UnitUtil.dip2px(36) - 4) / 2;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return list.get(0);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public static class ViewHolder {
		private LinearLayout layout_left;
		private LinearLayout layout_right;
		public boolean isDeal = false;
		public String dealDetail = null;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_recom_rest, null);
			holder.layout_left = (LinearLayout) convertView.findViewById(R.id.recom_rest_left);
			holder.layout_right = (LinearLayout) convertView.findViewById(R.id.recom_rest_right);
			adjustScreen(position, holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		convertView.setTag(holder);
		return convertView;
	}

	private void adjustScreen(final int position, final ViewHolder holder) {
		arrayList = new ArrayList<RecommendResInfoAdapter.MyImageViewDefine>();
		for (int i = 0; i < list.size(); i++) {
			final MyImageViewDefine myImageViewDefine = new MyImageViewDefine();
			RestRecomPicData data = list.get(i);
			double scale = 1.0000 * itemPicWidth / data.picWidth;
			int itemPicHeight = (int) (data.picHeight * scale);
			final LinearLayout layoutItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.list_item_recom_rest_detail, null);
			MyImageView imageView = (MyImageView) layoutItem.findViewById(R.id.recom_rest_detail_img);
			TextView textView = (TextView) layoutItem.findViewById(R.id.recom_rest_detail_tv);
			myImageViewDefine.imageView = imageView;
			myImageViewDefine.url = data.picUrl;
			imageView.setLayoutParams(new LinearLayout.LayoutParams(itemPicWidth, itemPicHeight));
			if (data.detail.length() >= 30) {// detail的长度大于30
				textView.setText(data.detail.substring(0, 25) + "...");
			} else {
				textView.setText(data.detail);
			}
			layoutItem.setTag(i);
			textView.measure(0, 0);
			arrayList.add(myImageViewDefine);
			holder.layout_right.measure(0, 0);
			holder.layout_left.measure(0, 0);
			layoutItem.measure(0,0);
			if (holder.layout_left.getMeasuredHeight() <= holder.layout_right.getMeasuredHeight()) {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(UnitUtil.dip2px(10), 0, UnitUtil.dip2px(5), UnitUtil.dip2px(10));
				layoutItem.setLayoutParams(layoutParams);
				holder.layout_left.addView(layoutItem);
				
			} else {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(UnitUtil.dip2px(5), 0, UnitUtil.dip2px(10), UnitUtil.dip2px(10));
				layoutItem.setLayoutParams(layoutParams);
				holder.layout_right.addView(layoutItem);
				
			}

			layoutItem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// ----
					OpenPageDataTracer.getInstance().addEvent("缩略图按钮");
					// -----
					
					Bundle bundle = new Bundle();
					bundle.putInt(Settings.BUNDLE_KEY_ID, (Integer) layoutItem.getTag());
					bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, (Serializable) list);
					ActivityUtil.jump(context, RecommendRestaurantGalleryActivity.class, 0, bundle);
				}
			});
		}
	}

	public List<RestRecomPicData> getList() {
		return list;
	}

	public void setList(List<RestRecomPicData> list) {
		this.list = list;
		this.notifyDataSetChanged();
		
	}

	public class MyImageViewDefine {
		public MyImageView imageView;
		public String url;
		public boolean isImageSetting = false;
	}
}
