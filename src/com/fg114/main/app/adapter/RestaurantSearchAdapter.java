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
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchFoodActivity;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.RestListData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 随手拍餐厅适配器
 * 
 * @author chenguojin
 * 
 */
public class RestaurantSearchAdapter extends BaseAdapter {

	private static final String TAG = "RestaurantAndFoodAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<RestListData> list = new ArrayList<RestListData>();
	// public Set<MyImageView> viewList = new HashSet<MyImageView>();

	private LayoutInflater mInflater = null;
	private Context context;
	private View.OnClickListener retryButtonListener;
	private String mImageUrl;

	// 是否显示添加餐厅按钮
	private boolean mIsShowAddResBtn = false;
	// 是否显示重试按钮
	private boolean mIsShowRetryBtn = false;
	// 是否显示文本信息
	private boolean showMessage = true;

	// 由外部来设置是否在没有找到餐厅时可以添加餐厅。（随手拍可以添加，短信邀请选择餐厅时不可以添加）
	public boolean canAddRestaurant = true;

	public RestaurantSearchAdapter(Context c, View.OnClickListener retryButtonListener, String url) {
		super();
		this.context = c;
		this.mImageUrl = url;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.retryButtonListener = retryButtonListener;
	}

	public RestaurantSearchAdapter(Context c) {
		this(c, null, "");
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
		// 餐厅部分
		RelativeLayout mLayout;
		TextView mResturantName;
		TextView mDistance;
		TextView mDeclare;
		// 消息部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnAddNewRes;
		Button btnRetry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_restaurant_search, null);
			holder.mResturantName = (TextView) convertView.findViewById(R.id.list_item_reasturant_reasturantname);
			holder.mDistance = (TextView) convertView.findViewById(R.id.list_item_reasturant_distance);
			holder.mDeclare = (TextView) convertView.findViewById(R.id.list_item_reasturant_declare);
			holder.mLayout = (RelativeLayout) convertView.findViewById(R.id.list_item_dishorderLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.res_food_list_item_msgLayout);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.res_food_list_item_pBar);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.res_food_list_item_tvMsg);
			holder.btnAddNewRes = (Button) convertView.findViewById(R.id.res_food_list_item_btnAddNewRes);
			holder.btnRetry = (Button) convertView.findViewById(R.id.res_food_list_item_btnRetry);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		RestListData restData = list.get(position);

		if (!(Settings.CONTRL_ITEM_ID + "").equals(restData.restId)) {
			holder.mLayout.setVisibility(View.VISIBLE);
			holder.mResturantName.setText(restData.restName);
			holder.mDistance.setText(restData.distance);
			holder.mDeclare.setText(restData.describe);
			holder.msgLayout.setVisibility(View.GONE);
		} else {
			// 消息的场合
			holder.mLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(restData.restName)) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(restData.restName);

			// 处理添加餐厅按钮
			if (mIsShowAddResBtn) {
				String strAddRes = context.getString(R.string.text_button_add_res);
				int start = strAddRes.indexOf("添加餐厅");
				if (start > -1) {
					ViewUtils.setSpan(context, holder.btnAddNewRes, strAddRes, start + "添加餐厅".length(), strAddRes.length(), R.color.text_color_red);
				}

				holder.btnAddNewRes.setVisibility(View.VISIBLE);
				holder.btnAddNewRes.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// ActivityUtil.jumpNotForResult(context,
						// AddOrUpdateResActivity.class, new Bundle(), false);
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, SessionManager.getInstance().getFilter().getKeywords());
						ActivityUtil.jump(context, AddOrUpdateResActivity.class, 0, bundle);

					}
				});
			} else {
				holder.btnAddNewRes.setVisibility(View.GONE);
				holder.btnAddNewRes.setOnClickListener(null);
			}

			// 是否显示文本信息
			if (showMessage) {
				holder.tvMsg.setVisibility(View.VISIBLE);
			} else {
				holder.tvMsg.setVisibility(View.GONE);
			}
			// 是否显示重试按钮
			if (mIsShowRetryBtn) {
				holder.btnRetry.setVisibility(View.VISIBLE);
				holder.btnRetry.setOnClickListener(this.retryButtonListener);
			} else {
				holder.btnRetry.setVisibility(View.GONE);
			}
		}

		convertView.setTag(holder);
		return convertView;
	}

	public List<RestListData> getList() {
		return list;
	}

	public void setList(List<RestListData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<RestListData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<RestListData> list, boolean isLast) {
		// 删除最后一条消息
		this.list.remove(this.list.size() - 1);
		this.list.addAll(createMsgDataToList(list, isLast));
		notifyDataSetChanged();
	}

	/**
	 * 建立最后一条数据
	 * 
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<RestListData> createMsgDataToList(List<RestListData> list, boolean isLast) {
		RestListData msgData = new RestListData();
		msgData.restId = "" + Settings.CONTRL_ITEM_ID; // -1:消息提示
		String msg = "";
		mIsShowRetryBtn = false;
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
				mIsShowAddResBtn = false;
			} else {
				// 没有找到时
				msg = context.getString(R.string.text_info_not_found);

				mIsShowAddResBtn = true;
				msg = context.getString(R.string.text_layout_res_not_found);
				if (this.list.size() > 0) {
					// 如果是网络连接异常没有取到后续数据（前面有数据），
					// 不显示“没有找到餐厅...”文字，只显示重试按钮
					msg = null;
					mIsShowRetryBtn = true;
					mIsShowAddResBtn = false;
				} else if (this.list.size() == 0) { // 如果没有任何数据，并且是短信邀请页面来的，不能添加餐厅，只显示"没有找到您要的餐厅"
					if (!canAddRestaurant) {
						mIsShowAddResBtn = false;
						msg = "没有找到您要的餐厅";
					}
				}

			}
		} else if (isLast == false) {

			// 不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
			mIsShowAddResBtn = false;
		} else if (list.size() > 0 && isLast == true) { // 最后一页时
			msg = null; // 不显示文字
			mIsShowAddResBtn = true;
			// 修正“添加餐厅”按钮显示与否的逻辑，只在没有任何餐厅的时候显示
			if (this.list.size() == 0) {
				msg = ""; // 根本不显示控制项
				mIsShowAddResBtn = false;
			}
		}

		msgData.restName = msg;

		if (!"".equals(msg)) {
			list.add(msgData);
		}
		if (msg == null) {
			showMessage = false;
		} else {
			showMessage = true;
		}

		return list;
	}
}
