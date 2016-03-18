package com.fg114.main.app.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.data.CityInfo;


/**
 * 城市选择列表适配器
 * @author zhangyifan
 *
 */
public class CityAdapter extends BaseAdapter {

	private static final String TAG = "CityAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	
	private List<CityInfo> list = null;
	
	private LayoutInflater mInflater = null;
	private Context context;

	public CityAdapter(Context c) {
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

	public static class ViewHolder {
		public LinearLayout Layout;		//layout
		public TextView tvCityName;		//城市名
		public ImageView ivArrow;		//箭头符号
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(DEBUG)Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		
		ViewHolder holder = null;
		if (convertView == null) {
		    holder = new ViewHolder();
		    convertView = mInflater.inflate(R.layout.list_item_common, null);
		    holder.Layout = (LinearLayout) convertView.findViewById(R.id.list_item_common_layout);
		    holder.tvCityName = (TextView) convertView.findViewById(R.id.list_item_common_tvName);
		    holder.ivArrow = (ImageView) convertView.findViewById(R.id.list_item_common_ivArrow);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//列表显示效果设置
	    if (list.size() == 1) {
	    	holder.Layout.setBackgroundResource(R.drawable.block_button);
		} else if (position == 0) {
	    	holder.Layout.setBackgroundResource(R.drawable.block_top_button);
	    } else if (position == (list.size() - 1)) {
	    	holder.Layout.setBackgroundResource(R.drawable.block_bottom_button);
	    } else {
	    	holder.Layout.setBackgroundResource(R.drawable.block_middle_button);
	    }
	    CityInfo city = (CityInfo)list.get(position);
		holder.tvCityName.setText(city.getName());
		holder.ivArrow.setVisibility(View.INVISIBLE);
		
		convertView.setTag(holder);
		return convertView;
	}
	
	public List<CityInfo> getList() {
		return list;
	}

	public void setList(List<CityInfo> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}
}
