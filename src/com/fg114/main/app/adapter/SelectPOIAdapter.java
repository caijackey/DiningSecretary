package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoCompleteActivity;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.service.dto.CommonTypeDTO;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 关键字提示列表适配器
 * 
 * @author zhangyifan
 * 
 */
public class SelectPOIAdapter extends BaseAdapter {

	private static final String TAG = "SelectPOIAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<CommonTypeDTO> list = null;

	private LayoutInflater mInflater = null;
	private Context context;
	public boolean isReset = true; // 如果是第一次使用addlist添加数据需要设置为true

	public SelectPOIAdapter(Context c) {
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

	private static class ViewHolder {
		LinearLayout infoLayout;

		TextView name;
		TextView address;
		// 消息提示部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		TextView hideMsg;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_select_poi, null);
			holder.infoLayout = (LinearLayout) convertView.findViewById(R.id.autoComplete_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.autoComplete_msgLayout);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.address = (TextView) convertView.findViewById(R.id.address);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.autoComplete_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.autoComplete_pBar);
			holder.hideMsg = (TextView) convertView.findViewById(R.id.autoComplete_hideMsg);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		CommonTypeDTO data = list.get(position);

		if (String.valueOf(Settings.CONTRL_ITEM_ON_ID).equals(data.getUuid())) {
			// 提示信息的场合
			holder.infoLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			holder.tvMsg.setText(data.getName());

			// 设置消息
			if (text_loading.equals(data.getName())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(data.getName());
			holder.hideMsg.setVisibility(View.GONE);
		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			holder.name.setText(data.getName());
			holder.address.setText(data.getMemo()); 
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<CommonTypeDTO> getList() {
		return list;
	}

	@SuppressWarnings("unchecked")
	public void setList(List<CommonTypeDTO> list, boolean isLocalHistory) {
		if (list == null) {
			list = new ArrayList<CommonTypeDTO>();
		}

		CommonTypeDTO dto = new CommonTypeDTO();
		dto.setUuid(String.valueOf(Settings.CONTRL_ITEM_ID));
		// 复制列表
		if (list.size() == 0) {
			// 列表内容添加
			if (isLocalHistory) {
				// 历史记录列表的场合
				dto.setName("无搜索历史记录");
			} else {
				dto.setName("没有符合条件的记录");
			}
		} else {
			if (isLocalHistory) {
				dto.setName(this.context.getString(R.string.text_button_clear));
			}
		}
		isReset = true;
		// 复制参数列表
		this.list = (List<CommonTypeDTO>) ((ArrayList<CommonTypeDTO>) list).clone();
		this.list.add(dto);
		notifyDataSetChanged();
	}

	public void addList(List<CommonTypeDTO> list, boolean isLast) {
		// 删除最后一条消息
		if (isReset) {
			this.list.clear();
			this.list.addAll(createMsgDataToList(list, isLast));
			isReset = false;
		} else {
			this.list.remove(this.list.size() - 1);
			this.list.addAll(createMsgDataToList(list, isLast));
		}

		notifyDataSetChanged();
	}

	/**
	 * 建立最后一条数据
	 * 
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	final static String text_loading="正在搜索..."; 
	private List<CommonTypeDTO> createMsgDataToList(List<CommonTypeDTO> list, boolean isLast) {
		CommonTypeDTO msgData = new CommonTypeDTO();
		msgData.setUuid(String.valueOf(Settings.CONTRL_ITEM_ON_ID)); // -1:消息提示
		String msg = "";

		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				if(this.list.size()>0){
					msg="";
				}else{
					msg = "没有符合条件的记录";
				}
				
			}
		} else if (isLast == false) {
			// 不是在最后一页时
			msg = text_loading;
		} else if (list.size() > 0 && isLast == true) {
			msg = "";//不显示控制项
		}

		msgData.setName(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}
}
