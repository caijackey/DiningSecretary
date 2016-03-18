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
import com.fg114.main.app.activity.MainFrameActivity;
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
public class AutoCompleteAdapter extends BaseAdapter {

	private static final String TAG = "AutoCompleteAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	public static boolean isRecomRest;

	private List<CommonTypeDTO> list = null;

	private LayoutInflater mInflater = null;
	private Context context;
	public boolean isReset = true; // 如果是第一次使用addlist添加数据需要设置为true
	// 是否显示添加餐厅按钮
	private boolean mIsShowAddResBtn = false;

	public AutoCompleteAdapter(Context c) {
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
		RelativeLayout infoLayout;

		TextView tvKey;
		TextView tvResultNum;
		// 消息提示部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnAddNewRes;
		Button btnRetry;
		TextView hideMsg;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_auto_complete, null);
			holder.infoLayout = (RelativeLayout) convertView.findViewById(R.id.autoComplete_infoLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.autoComplete_msgLayout);
			holder.tvKey = (TextView) convertView.findViewById(R.id.autoComplete_tvKey);
			holder.tvResultNum = (TextView) convertView.findViewById(R.id.autoComplete_tvNum);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.autoComplete_tvMsg);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.autoComplete_pBar);
			holder.btnAddNewRes = (Button) convertView.findViewById(R.id.autoComplete_btnAddNewRes);
			holder.btnRetry = (Button) convertView.findViewById(R.id.autoComplete_btnRetry);
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
			if (context.getString(R.string.text_info_loading).equals(data.getName())) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(data.getName());
			holder.tvMsg.setVisibility(View.VISIBLE);
			
			holder.btnAddNewRes.setText(context.getString(R.string.text_button_add_res));
			// 处理添加餐厅按钮
			if (mIsShowAddResBtn) {
				if (data.getMemo() != null) {
					holder.hideMsg.setVisibility(View.VISIBLE);
					holder.hideMsg.setText(data.getMemo());
				} else {
					holder.hideMsg.setVisibility(View.GONE);
				}
				String strAddRes = holder.btnAddNewRes.getText().toString();
				int start = strAddRes.indexOf("添加餐厅");
				if (start > -1) {
					ViewUtils.setSpan(context, holder.btnAddNewRes, strAddRes, start + "添加餐厅".length(), strAddRes.length(), R.color.text_color_red);
				}

				holder.btnAddNewRes.setVisibility(View.VISIBLE);
				holder.btnAddNewRes.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						ActivityUtil.jumpNotForResult(context, AddOrUpdateResActivity.class, new Bundle(), false);
					}
				});

			} else {
				holder.btnAddNewRes.setVisibility(View.GONE);
				holder.btnAddNewRes.setOnClickListener(null);
			}
			// //
			// 推荐餐厅过来的，要特殊显示-----------------------最后这里做调整-------------------------------------
			if (isRecomRest && mIsShowAddResBtn) {

				holder.btnAddNewRes.setText("如果餐厅不在上列，请点此先添加餐厅");
				holder.tvMsg.setVisibility(View.GONE);
				holder.hideMsg.setVisibility(View.VISIBLE);
				
				if (context.getString(R.string.text_info_no_found_by_key).equals(data.getMemo())) {
					data.setMemo("无结果");
				}
				holder.hideMsg.setText(data.getMemo());
				// --
				String btnText=holder.btnAddNewRes.getText().toString();
				int start = btnText.indexOf("请点此先添加餐厅");
				if (start > -1) {
					ViewUtils.setSpan(context, holder.btnAddNewRes, btnText, start , btnText.length(), R.color.text_color_red);
				}
			}

		} else {

			holder.infoLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);

			// 设置关键字
			ViewUtils.setHightlightKeywords(holder.tvKey, data.getName());

			// String text = data.getName();
			// text = text.replace(((AutoCompleteActivity)
			// context).etAutoComplete.getText().toString(), "<b>" +
			// ((AutoCompleteActivity)
			// context).etAutoComplete.getText().toString() + "</b>");
			// ViewUtils.setHightlightKeywords(holder.tvKey, text);

			// 设置结果数量
			if (data.getNum() > 0) {
				holder.tvResultNum.setVisibility(View.VISIBLE);
				holder.tvResultNum.setText("约" + data.getNum() + "个结果");
			} else {
				holder.tvResultNum.setVisibility(View.GONE);
			}
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
				dto.setName(this.context.getString(R.string.text_info_no_search_history));
			} else {
				dto.setName(this.context.getString(R.string.text_info_no_found_by_key));
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
				mIsShowAddResBtn = false;
			} else {

				msg = context.getString(R.string.text_layout_res_not_found);
				String ChannelId = SessionManager.getInstance().getFilter().getChannelId();
				if (ChannelId.equals("1")) {
					mIsShowAddResBtn = true;
					msgData.setMemo(context.getString(R.string.text_info_no_found_by_key));
				} else {
					msg = context.getString(R.string.text_info_no_found_by_key);
					mIsShowAddResBtn = false;
				}

			}
		} else if (isLast == false) {

			// 不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
			mIsShowAddResBtn = false;
		} else if (list.size() > 0 && isLast == true) {

			String ChannelId = SessionManager.getInstance().getFilter().getChannelId();
			if (ChannelId.equals("1")) {
				msg = context.getString(R.string.text_layout_res_not_found);
				mIsShowAddResBtn = true;
			} else {
				msg = "";
				mIsShowAddResBtn = false;
			}

		} else {
			mIsShowAddResBtn = false;
		}

		msgData.setName(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}
}
