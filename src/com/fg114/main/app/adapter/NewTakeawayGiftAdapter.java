package com.fg114.main.app.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.ParabolaAnimation;
import com.fg114.main.service.dto.TakeoutMenuData;
import com.fg114.main.service.dto.TakeoutMenuData2;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class NewTakeawayGiftAdapter extends BaseAdapter{
	private List<TakeoutMenuData2> mList = new ArrayList<TakeoutMenuData2>();
	private LayoutInflater adaperInflater;

	public NewTakeawayGiftAdapter(Context context) {
		adaperInflater = LayoutInflater.from(context);
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
		
		
		MyImageView picUrl_img;
		TextView name_tv;
		TextView price_tv;
		RatingBar overallNum_rb;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {
			convertView = adaperInflater.inflate(R.layout.list_item_new_take_away_giftlist, null);
			holder = new ViewHolder();

			holder.picUrl_img = (MyImageView) convertView.findViewById(R.id.newtakeaway_list_item_gift_ivFoodPic);
			holder.name_tv = (TextView) convertView.findViewById(R.id.newtakeaway_list_item_gift_tvName);
			holder.price_tv = (TextView) convertView.findViewById(R.id.newtakeaway_list_item_gift_tvPrice);
			holder.overallNum_rb = (RatingBar) convertView.findViewById(R.id.newtakeaway_list_item_gift_overall_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final TakeoutMenuData2 data = mList.get(position);

		if (TextUtils.isEmpty(data.picUrl)) {
			holder.picUrl_img.setVisibility(View.INVISIBLE);
		} else {
			holder.picUrl_img.setImageByUrl(data.picUrl, true, 0, ScaleType.FIT_XY);
			holder.picUrl_img.setVisibility(View.VISIBLE);
		}		
		
		holder.overallNum_rb.setRating((float) data.overallNum);			
		holder.name_tv.setText(data.name);
		holder.price_tv.setText("￥" + data.price);
		return convertView;
	}

	

	public void setList(List<TakeoutMenuData2> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	public List<TakeoutMenuData2> getList() {
		return mList;

	}
	private String digitalFormatConversion(double price) {
		String parten = "#.#";
		DecimalFormat decimal = new DecimalFormat(parten);
		String showPrice = decimal.format(price);
		if (showPrice.contains("\\.")) {
			String[] arrPrice = showPrice.split("\\.");
			if (!arrPrice[1].equals("0") && !arrPrice[1].equals("00")) {
				showPrice = arrPrice[0] + "." + arrPrice[1];
			} else {
				showPrice = arrPrice[0];
			}
		}
		return showPrice;
	}

}

