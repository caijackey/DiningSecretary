package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.weibo.dto.User;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 选择菜品列表适配器
 * 
 * @author xujianjun,2012-09-15
 * 
 */
public class SearchFoodListAdapter2 extends BaseAdapter {

	private Context context;
	private List<ResFoodData> list = new ArrayList<ResFoodData>();

	private class ViewHolder {
		TextView letterTitle;
		TextView name;
	}

	public SearchFoodListAdapter2(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.w("WWW",position+"");
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(this.context, R.layout.list_item_search_food_2, null);
			holder = new ViewHolder();
			holder.letterTitle = (TextView) convertView.findViewById(R.id.letter_title);
			holder.name = (TextView) convertView.findViewById(R.id.list_item_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 设置数据
		ResFoodData data = (ResFoodData) getItem(position);

		holder.letterTitle.setText(HanziUtil.getFirst(data.getFirstLetter()).toUpperCase());
		holder.name.setText(data.getName());

		if (isFirstItemInThisGroup(position)) {
			holder.letterTitle.setVisibility(View.VISIBLE);
		} else {
			holder.letterTitle.setVisibility(View.GONE);
		}

		return convertView;
	}

	public void setList(List<ResFoodData> list) {
		if (list == null) {
			list = new ArrayList<ResFoodData>();
		}
		this.list = list;
		notifyDataSetChanged();
	}

	public List<ResFoodData> getList() {
		if (this.list == null) {
			this.list = new ArrayList<ResFoodData>();
		}
		return this.list;
	}

	// 判断当前位置是否是分组中的第一个
	private boolean isFirstItemInThisGroup(int position) {
		try {
			if (position <= 0 || position > list.size()) {
				return true;
			}
			
			return !HanziUtil.getFirst(((ResFoodData) this.getItem(position)).getFirstLetter()).equals(
						HanziUtil.getFirst(((ResFoodData) this.getItem(position - 1)).getFirstLetter()));
			
		} catch (Exception ex) {
			return false;
		}
	}

}
