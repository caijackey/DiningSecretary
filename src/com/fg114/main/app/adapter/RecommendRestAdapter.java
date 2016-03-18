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
import android.widget.ImageView.ScaleType;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchFoodActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 推荐页随手拍适配器
 * @author zhaozuoming
 *
 */
public class RecommendRestAdapter extends BaseAdapter {

	private static final String TAG = "RecommendRestAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<RestRecomPicData> list = new ArrayList<RestRecomPicData>();

	private LayoutInflater mInflater = null;
	private Context context;
	private View.OnClickListener retryButtonListener;
	private String mImageUrl;

	// 是否显示重试按钮
	private boolean mIsShowRetryBtn = false;
	// 是否显示文本信息
	private boolean showMessage = true;

	public RecommendRestAdapter(Context c, View.OnClickListener retryButtonListener) {
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.retryButtonListener = retryButtonListener;
	}

	public RecommendRestAdapter(Context c) {
		this(c, null);
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
		LinearLayout mLayout;
		MyImageView picture;
		TextView text;
		TextView nickname_and_time;
		TextView hit_num;
		TextView favorite_num;
		TextView business_location;//商圈位置
		// 消息部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnRetry;
		public TextView rest_name;
	    MyImageView coupon_icon_pic;
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
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_recommend_rest_index, null);
			holder.picture = (MyImageView) convertView.findViewById(R.id.picture);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.rest_name = (TextView) convertView.findViewById(R.id.rest_name);
			holder.nickname_and_time = (TextView) convertView.findViewById(R.id.nickname_and_time);
			holder.hit_num = (TextView) convertView.findViewById(R.id.hit_num);
			holder.favorite_num = (TextView) convertView.findViewById(R.id.favorite_num);
			holder.business_location =(TextView) convertView.findViewById(R.id.business_location);
			holder.coupon_icon_pic=(MyImageView) convertView.findViewById(R.id.coupon_icon_pic);
			
			//
			holder.mLayout = (LinearLayout) convertView.findViewById(R.id.list_item_dishorderLayout);
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.res_food_list_item_msgLayout);
			holder.pbBar = (ProgressBar) convertView.findViewById(R.id.res_food_list_item_pBar);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.res_food_list_item_tvMsg);
			holder.btnRetry = (Button) convertView.findViewById(R.id.res_food_list_item_btnRetry);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		RestRecomPicData restData = list.get(position);

		if (!(Settings.CONTRL_ITEM_ID + "").equals(restData.uuid)) {
			holder.mLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);
	
			holder.rest_name.setText(restData.restName);
			holder.text.setText(restData.title);
			holder.nickname_and_time.setText(restData.userNickName+"　"+restData.createTime);
			holder.hit_num.setText(restData.hitNum+"");
			holder.favorite_num.setText(restData.favNum+"");
			holder.business_location.setText(restData.district+"");
			holder.picture.setImageByUrl(restData.picUrl, true, position, ScaleType.FIT_XY);
			
			if(CheckUtil.isEmpty(restData.couponIconUrl)){
				holder.coupon_icon_pic.setVisibility(View.GONE);
			}else{
				holder.coupon_icon_pic.setVisibility(View.VISIBLE);
				holder.coupon_icon_pic.setImageByUrl(restData.couponIconUrl, true, position, ScaleType.FIT_XY);
			}
			
		} else {
			// 消息的场合
			holder.mLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(restData.title)) {
				// 载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(restData.title);


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

	public List<RestRecomPicData> getList() {
		return list;
	}

	public void setList(List<RestRecomPicData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<RestRecomPicData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<RestRecomPicData> list, boolean isLast) {
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
	private List<RestRecomPicData> createMsgDataToList(List<RestRecomPicData> list, boolean isLast) {
		RestRecomPicData msgData = new RestRecomPicData();
		msgData.uuid = "" + Settings.CONTRL_ITEM_ID; // -1:消息提示
		String msg = "";
		mIsShowRetryBtn = false;
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				// 没有找到时
				msg = "暂无推荐信息";

				if (this.list.size() > 0) {
					// 如果是网络连接异常没有取到后续数据（前面有数据），
					msg = null;
					mIsShowRetryBtn = true;
				} 
			}
		} else if (isLast == false) {
			// 不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
		} else if (list.size() > 0 && isLast == true) { // 最后一页时
			msg = ""; // 根本不显示控制项
		}
		msgData.title = msg;
		
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
