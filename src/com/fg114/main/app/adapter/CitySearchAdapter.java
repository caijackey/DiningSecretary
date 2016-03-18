package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.util.CheckUtil;

/**
 * 关键字提示列表适配器
 * 
 * @author zhangyifan
 * 
 */
public class CitySearchAdapter extends BaseAdapter {

	private static final String TAG = "CitySearchAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<CommonTypeDTO> list = null;

	private LayoutInflater mInflater = null;
	private Context context;

	public CitySearchAdapter(Context c) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		LinearLayout infoLayout;
		LinearLayout msgLayout;
		TextView tvKey;
		TextView tvMsg;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :"
					+ convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_city_search,
					null);
			holder.infoLayout = (LinearLayout) convertView
					.findViewById(R.id.autoComplete_infoLayout);
			holder.msgLayout = (LinearLayout) convertView
					.findViewById(R.id.autoComplete_msgLayout);
			holder.tvKey = (TextView) convertView
					.findViewById(R.id.autoComplete_tvKey);
			holder.tvMsg = (TextView) convertView
					.findViewById(R.id.autoComplete_tvMsg);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		CommonTypeDTO data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ID).equals(data.getUuid())) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.tvMsg.setText(data.getName());
		} else if (!CheckUtil.isEmpty(data.getName())) {
			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置关键字
			holder.tvKey.setText(data.getName());
		} else {
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.GONE);
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<CommonTypeDTO> getList() {
		return list;
	}

	@SuppressWarnings("unchecked")
	public void setList(List<CommonTypeDTO> list) {
		if (list == null) {
			list = new ArrayList<CommonTypeDTO>();
		}

		CommonTypeDTO dto = new CommonTypeDTO();
		dto.setUuid(String.valueOf(Settings.CONTRL_ITEM_ID));
		// //复制列表
		if (list.size() == 0) {
			dto.setName(this.context
					.getString(R.string.text_info_no_search_result));
		}
		// 复制参数列表
		this.list = (List<CommonTypeDTO>) ((ArrayList<CommonTypeDTO>) list)
				.clone();
		this.list.add(dto);
		notifyDataSetChanged();
	}
}
