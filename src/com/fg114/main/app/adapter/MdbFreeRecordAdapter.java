package com.fg114.main.app.adapter;

import java.util.List;

import com.fg114.main.R;

import com.fg114.main.service.dto.MdbFreeRecordData;
import com.fg114.main.util.CheckUtil;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MdbFreeRecordAdapter extends BaseAdapter{
	private List<MdbFreeRecordData> freeRecordList;
	private Context context;

	public MdbFreeRecordAdapter(Context c, List<MdbFreeRecordData> freeRecordList) {
		super();
		this.freeRecordList = freeRecordList;
		// this.list = doTest();
		this.context = c;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return freeRecordList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return freeRecordList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public static class ViewHolder {
		public TextView mdb_user_name;
		public TextView mdb_user_num;
		public TextView mdb_user_date;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(this.context,R.layout.mdb_free_record_list_item, null);
			holder.mdb_user_name = (TextView) convertView.findViewById(R.id.mdb_user_name);
			holder.mdb_user_num = (TextView) convertView.findViewById(R.id.mdb_user_num);
			holder.mdb_user_date = (TextView) convertView.findViewById(R.id.mdb_user_date);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final MdbFreeRecordData data = freeRecordList.get(position);
		

		if(!CheckUtil.isEmpty(data.name)){
		holder.mdb_user_name.setText(data.name);
		}
		if(!CheckUtil.isEmpty(data.freeMoney)){
		holder.mdb_user_num.setText(data.freeMoney);
		}
		if(!CheckUtil.isEmpty(data.freeTime)){
		holder.mdb_user_date.setText(data.freeTime);
		}
		
		convertView.setTag(holder);
		return convertView;
	}

}
