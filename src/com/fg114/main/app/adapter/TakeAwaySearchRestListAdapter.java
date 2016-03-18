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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.RestListData;
import com.fg114.main.service.dto.TakeoutListData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;


/**
 * 外卖搜索列表适配器
 * @author xujianjun,2013-10-28
 *
 */
public class TakeAwaySearchRestListAdapter extends BaseAdapter {

	private static final String TAG = "TakeAwaySearchRestListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	
	private List<TakeoutListData> list = new ArrayList<TakeoutListData>();
	private Context context;
	private View.OnClickListener retryButtonListener;
	
	// 是否显示重试按钮
	private boolean mIsShowRetryBtn = false;
	//是否显示文本信息
	private boolean showMessage=true;
	
	public TakeAwaySearchRestListAdapter(Context c, View.OnClickListener retryButtonListener){
		super();
		this.context = c;
		this.retryButtonListener=retryButtonListener;
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

		public LinearLayout main_layout;
		public MyImageView rest_logo;
		public TextView rest_name;
		public TextView have_gift;
		public RatingBar rating_bar;
		public TextView rest_status;
		public TextView send_limit_price;
		public TextView send_reach_mins;
		public TextView distance_meter;
		//---
		public LinearLayout msg_layout;
		public TextView message;
		public Button button_retry;
		public ProgressBar progress_bar;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
		    holder = new ViewHolder();
		    convertView = View.inflate(context,R.layout.list_item_take_away_search_rest, null);
		    holder.main_layout = (LinearLayout) convertView.findViewById(R.id.main_layout);
		    //--
		    holder.rest_logo = (MyImageView) convertView.findViewById(R.id.rest_logo);
		    holder.rest_name = (TextView) convertView.findViewById(R.id.rest_name);
		    holder.have_gift = (TextView) convertView.findViewById(R.id.have_gift);
		    holder.rating_bar = (RatingBar) convertView.findViewById(R.id.rating_bar);
		    holder.rest_status = (TextView) convertView.findViewById(R.id.rest_status);
		    holder.send_limit_price = (TextView) convertView.findViewById(R.id.send_limit_price);
		    holder.send_reach_mins = (TextView) convertView.findViewById(R.id.send_reach_mins);
		    holder.distance_meter = (TextView) convertView.findViewById(R.id.distance_meter);
		    //--
		    holder.msg_layout = (LinearLayout) convertView.findViewById(R.id.msg_layout);
		    holder.progress_bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
		    holder.message = (TextView) convertView.findViewById(R.id.message);
		    holder.button_retry = (Button) convertView.findViewById(R.id.button_retry);
		    
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		//列表内容设置
		TakeoutListData data = list.get(position);
		
		if (!(""+Settings.CONTRL_ITEM_ID).equals(data.uuid)) {
			//餐馆的场合
			holder.main_layout.setVisibility(View.VISIBLE);
			holder.msg_layout.setVisibility(View.GONE);
			//设置餐馆图片
			holder.rest_logo.setImageByUrl(data.picUrl,true,position,ScaleType.FIT_XY);
			//外卖隐藏餐厅图片
		    //holder.rest_logo.setVisibility(View.GONE);
			
			//设置餐馆名字
			if("".equals(data.name)){
				holder.rest_name.setText(R.string.text_null_hanzi);
			}else{
				holder.rest_name.setText(data.name);
			}
			
			//设置餐馆星级
			double overallNum = data.overallNum;
			holder.rating_bar.setMinimumHeight(1);
			holder.rating_bar.setRating((float)overallNum);
			//是否有赠品
			if(data.haveGiftTag){
				holder.have_gift.setVisibility(View.VISIBLE);
				holder.have_gift.setText("赠");
			}else{
				holder.have_gift.setVisibility(View.GONE);
			}
			//状态
			if(!CheckUtil.isEmpty(data.stateName)){
				holder.rest_status.setVisibility(View.VISIBLE);
				holder.rest_status.setText(data.stateName);
				try{
					int color=Integer.parseInt(data.stateColor.replace("#", ""),16)|0xFF000000;
					holder.rest_status.setBackgroundColor(color);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				holder.rest_status.setVisibility(View.GONE);
			}
			//设置起送价，时间，距离
			holder.send_limit_price.setText(CheckUtil.isEmpty(data.sendLimitPrice)?"---":data.sendLimitPrice);
			holder.send_reach_mins.setText(CheckUtil.isEmpty(data.sendReachMins)?"---":data.sendReachMins);
			holder.distance_meter.setText(CheckUtil.isEmpty(data.distanceMeter)?"---":data.distanceMeter);
			
		} else {
			//消息的场合
			holder.main_layout.setVisibility(View.GONE);
			holder.msg_layout.setVisibility(View.VISIBLE);
			//设置消息
			if (context.getString(R.string.text_info_loading).equals(data.name)) {
				//载入的场合
				holder.progress_bar.setVisibility(View.VISIBLE);
			} else {
				holder.progress_bar.setVisibility(View.GONE);
			}
			holder.message.setText(data.name);
			
			
			//是否显示文本信息
			if(showMessage){
				holder.message.setVisibility(View.VISIBLE);
			}
			else{
				holder.message.setVisibility(View.GONE);
			}
			//是否显示重试按钮
			if(mIsShowRetryBtn){
				holder.button_retry.setVisibility(View.VISIBLE);
				holder.button_retry.setOnClickListener(this.retryButtonListener);
			}
			else{
				holder.button_retry.setVisibility(View.GONE);
			}
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<TakeoutListData> getList() {
		return list;
	}
	
	
	public void setList(List<TakeoutListData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<TakeoutListData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}
	
	public void addList(List<TakeoutListData> list, boolean isLast) {
		//删除最后一条消息
		this.list.remove(this.list.size() - 1);
		this.list.addAll(createMsgDataToList(list, isLast));
		notifyDataSetChanged();
	}
	
	
	/**
	 * 建立最后一条数据
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<TakeoutListData> createMsgDataToList(List<TakeoutListData> list, boolean isLast) {
		TakeoutListData msgData = new TakeoutListData();
		msgData.uuid=""+Settings.CONTRL_ITEM_ID; //-1:消息提示
		String msg = "";
		mIsShowRetryBtn=false;
		if (list.size() == 0&&isLast == true) {
			//检查网络是否连通
	        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
	        if (!isNetAvailable) {
	        	//没有网络时
	        	msg = context.getString(R.string.text_info_net_unavailable);
			} else {
				//没有找到时
				msg = context.getString(R.string.text_info_not_found);
				if(this.list.size()>0){
					//如果是网络连接异常没有取到后续数据（前面有数据），
					//不显示“没有找到餐厅...”文字，只显示重试按钮
					msg=null; 
					mIsShowRetryBtn=true;
				}
				
			}
		} else if(isLast == false){
			
			//不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
		}
		else if(list.size() > 0&&isLast == true){		
			msg="";
		}
		msgData.name=msg;
		
		if (! "".equals(msg)) {
			list.add(msgData);
		}
		if(msg==null){
			showMessage=false;
		}
		else{
			showMessage=true;
		}
	
		return list;
	}
}
