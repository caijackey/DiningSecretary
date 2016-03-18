package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.util.HanziUtil;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 获得更多城市列表
 * @author xujianjun
 *
 */
public class CityMoreAdapter extends BaseAdapter {

	private Context context;
	private List<CityData> cityList=new ArrayList<CityData>();

	private class ViewHolder {
		TextView letterTitle;
		TextView cityName;
	}
	public CityMoreAdapter(Context context) {
		this.context = context;		
	}

	public CityMoreAdapter(Context context, List<CityData> list) {
		this.context = context;		
		if(list!=null) cityList = list;
	}

	@Override
	public int getCount() {
		return cityList.size();
	}

	@Override
	public Object getItem(int position) {
		return cityList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.w("WWW",position+"");
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(this.context,R.layout.list_item_city_more, null);
			holder = new ViewHolder();
			holder.letterTitle = (TextView) convertView.findViewById(R.id.city_more_letter_title);
			holder.cityName = (TextView) convertView.findViewById(R.id.city_more_city_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//设置数据
		holder.letterTitle.setText(HanziUtil.getFirst(((CityData)getItem(position)).getFirstLetter()).toUpperCase());
		holder.cityName.setText(((CityData)getItem(position)).getCityName());
		if (isFirstItemInThisGroup(position)) {
			holder.letterTitle.setVisibility(View.VISIBLE);
		} else {
			holder.letterTitle.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	public void setList(List<CityData> list) {
		if(list==null){
			list=new ArrayList<CityData>();
		}
		this.cityList=list;
		notifyDataSetChanged();
	}
	public List<CityData> getList() {
		if(this.cityList==null){
			this.cityList=new ArrayList<CityData>();
		}
		return this.cityList;		
	}
	//判断当前位置是否是分组中的第一个
	private boolean isFirstItemInThisGroup(int position){
		try{
			if(position<=0||position>cityList.size()){
				return true;
			}
			return !HanziUtil.getFirst(
					((CityData)this.getItem(position)).getFirstLetter()
					).equals(
					HanziUtil.getFirst(((CityData)this.getItem(position-1)).getFirstLetter()));
		}
		catch(Exception ex){
			return false;
		}
	}
	
}
