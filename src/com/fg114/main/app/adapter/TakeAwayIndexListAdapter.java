package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.RestListData;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.service.dto.TakeoutListData;
import com.fg114.main.service.dto.TakeoutTypeData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;


/**
 * 外卖首页外卖类别适配器
 * @author sunquan,2014-03-31
 *
 */
public class TakeAwayIndexListAdapter extends BaseAdapter {

	private static final String TAG = "TakeAwaySearchRestListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<TakeoutTypeData> list = new ArrayList<TakeoutTypeData>();
	private Context context;

	public TakeAwayIndexListAdapter(Context c,List<TakeoutTypeData> typelist){
		super();
		this.context = c;
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

		public MyImageView takeaway_kinds;
		public TextView takeaway_kinds_name;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context,R.layout.list_item_takeaway_index, null);

			//--
			holder.takeaway_kinds = (MyImageView) convertView.findViewById(R.id.takeaway_kinds);
			holder.takeaway_kinds_name = (TextView) convertView.findViewById(R.id.text_takeaway_name);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//列表内容设置
		TakeoutTypeData data = list.get(position);

		if (!(""+Settings.CONTRL_ITEM_ID).equals(data.uuid)) {

			//设置餐馆图片
			holder.takeaway_kinds.setImageByUrl(data.iconUrl,true,0,ScaleType.FIT_XY);
			//外卖隐藏餐厅图片
			//holder.rest_logo.setVisibility(View.GONE);

			//设置餐馆名字
			if("".equals(data.name)){
				holder.takeaway_kinds_name.setText(R.string.text_null_hanzi);
			}else{
				holder.takeaway_kinds_name.setText(data.name);
			}
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<TakeoutTypeData> getList() {
		return list;
	}
	public void setList(List<TakeoutTypeData> list) {
		// list序列化
		this.list = (ArrayList<TakeoutTypeData>) list;
		notifyDataSetChanged();
	}

}