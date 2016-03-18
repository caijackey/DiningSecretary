package com.fg114.main.app.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.util.CheckUtil;


/**
 * 美食列表简单表示用适配器
 * @author zhangyifan
 *
 */
public class SimpleFoodListAdapter extends BaseAdapter {

	private static final String TAG = "SimpleFoodListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<ResFoodData> list = null;
	
	private LayoutInflater mInflater = null;
	private Context context;

	public SimpleFoodListAdapter(Context c) {
		super();
		this.context = c;
		this.mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	public static class ViewHolder {
		public TextView tvName;			//名称
		public TextView tvPrice;		//价格
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(DEBUG)Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		
		ViewHolder holder = null;
		if (convertView == null) {
		    holder = new ViewHolder();
		    convertView = mInflater.inflate(R.layout.list_item_food_simple, null);
		    holder.tvName = (TextView) convertView.findViewById(R.id.list_item_food_simple_tvName);
		    holder.tvPrice = (TextView) convertView.findViewById(R.id.list_item_food_simple_tvPrice);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ResFoodData data = (ResFoodData)list.get(position);
		holder.tvName.setText(data.getName());
		if(CheckUtil.isEmpty(data.getPrice())){
			holder.tvPrice.setText(R.string.text_null_hanzi);
		} else {
			holder.tvPrice.setText(data.getPrice());
		}
		
		convertView.setTag(holder);
		return convertView;
	}
	
	public void setList(List<ResFoodData> list) {
		this.list = list;
	}

	public List<ResFoodData> getList() {
		return list;
	}
}
