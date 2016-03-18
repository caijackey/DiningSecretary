package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.fg114.main.service.dto.RealTimeTableRestData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;


/**
 * 餐厅列表适配器
 * @author zhangyifan
 *
 */
public class RealTimeResAdapter extends BaseAdapter {

	private static final String TAG = RealTimeResAdapter.class.getSimpleName();
	private static final boolean DEBUG = Settings.DEBUG;
	
	private List<RealTimeTableRestData> list = new ArrayList<RealTimeTableRestData>();
	public Set<MyImageView> viewList = new HashSet<MyImageView>();
	
	private LayoutInflater mInflater = null;
	private Context context;
	private View.OnClickListener retryButtonListener;
	
	// 是否显示添加餐厅按钮
	private boolean mIsShowAddResBtn = false;
	// 是否显示重试按钮
	private boolean mIsShowRetryBtn = false;
	//是否显示文本信息
	private boolean showMessage=true;
	
	//是否是榜单页面
	private boolean isTopList=false;
	
	//是否显示图片
	public boolean showPicture=true;
	
	private RoomTypeButtonListener mRoomTypeButtonListener;
	
	public boolean simpleMode = false;
	
	public interface RoomTypeButtonListener {
		public void onClickFirst(View view, RealTimeTableRestData realTimeTableRestData);
		public void onClickSecond(View view, RealTimeTableRestData realTimeTableRestData);
		public void onClickRes(View view, RealTimeTableRestData realTimeTableRestData);
	}

	public RealTimeResAdapter(Context c,View.OnClickListener retryButtonListener) {
		this(c,retryButtonListener,false);	
	}
	public RealTimeResAdapter(Context c,View.OnClickListener retryButtonListener,boolean isTopList) {
		this(c,retryButtonListener,isTopList,true);	
	}
	public RealTimeResAdapter(Context c,View.OnClickListener retryButtonListener,boolean isTopList,boolean showPicture){
		super();
		this.context = c;
		this.showPicture=showPicture;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.retryButtonListener=retryButtonListener;
		this.isTopList=isTopList;
	}
	public RealTimeResAdapter(Context c) {
		this(c,null,false);		
	}
	public RealTimeResAdapter(Context c,boolean isTopList) {
		this(c,null,isTopList);		
	}
	public RealTimeResAdapter(Context c,boolean isTopList,boolean showPicture) {
		this(c,null,isTopList,showPicture);		
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
		//餐厅部分
		ViewGroup resLayout;
		MyImageView ivResLogo;
		TextView tvResName;
		TextView tvLike;
		TextView tvAvgPrice;
		TextView tvDiscount;
		ViewGroup vgDiscount;
		TextView tvResDistance;
		Button btFirst;
		Button btSecond;
		//消息部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnAddNewRes;
		Button btnRetry;
		ImageView ivArrow;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(DEBUG)Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		if (convertView == null) {
		    holder = new ViewHolder();
		    convertView = mInflater.inflate(R.layout.list_item_real_time_res, null);
		    holder.resLayout = (ViewGroup) convertView.findViewById(R.id.list_item_real_time_res_rlRes);
		    holder.ivResLogo = (MyImageView) convertView.findViewById(R.id.list_item_real_time_res_ivResLogo);
		    holder.tvResName = (TextView) convertView.findViewById(R.id.list_item_real_time_res_tvResName);
		    holder.tvAvgPrice = (TextView) convertView.findViewById(R.id.list_item_real_time_res_tvAvgPrice);
		    holder.tvLike = (TextView) convertView.findViewById(R.id.list_item_real_time_res_tvLike);
		    holder.tvDiscount = (TextView) convertView.findViewById(R.id.list_item_real_time_res_tvDiscount);
		    holder.vgDiscount = (ViewGroup) convertView.findViewById(R.id.list_item_real_time_res_vgDiscount);
		    holder.tvResDistance = (TextView) convertView.findViewById(R.id.list_item_real_time_res_tvResDistance);
		    holder.btFirst = (Button) convertView.findViewById(R.id.list_item_real_time_res_btFirst);
		    holder.btSecond = (Button) convertView.findViewById(R.id.list_item_real_time_res_btSecond);
		    holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.res_food_list_item_msgLayout);
		    holder.pbBar = (ProgressBar) convertView.findViewById(R.id.res_food_list_item_pBar);
		    holder.tvMsg = (TextView) convertView.findViewById(R.id.res_food_list_item_tvMsg);
		    holder.btnAddNewRes = (Button) convertView.findViewById(R.id.res_food_list_item_btnAddNewRes);
		    holder.btnRetry = (Button) convertView.findViewById(R.id.res_food_list_item_btnRetry);
		    holder.ivArrow = (ImageView) convertView.findViewById(R.id.list_item_real_time_res_ivArrow);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		//列表内容设置
		final RealTimeTableRestData rest = list.get(position);

		if (!rest.getRestId().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
			//餐馆的场合
			holder.resLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);
			//设置餐馆图片
			viewList.add(holder.ivResLogo);
			if(showPicture){
				holder.ivResLogo.setImageByUrl(rest.getPicUrl(),
					true,
					position,
					ScaleType.CENTER_CROP);
			} else{
				holder.ivResLogo.setVisibility(View.GONE);
			}
			
			if (simpleMode) {
				holder.ivArrow.setVisibility(View.GONE);
				holder.resLayout.setBackgroundDrawable(null);
			} else {
				holder.ivArrow.setVisibility(View.VISIBLE);
				holder.resLayout.setBackgroundResource(R.drawable.middle_list_item_bg);
			}
			
			//设置餐馆名字
			if("".equals(rest.getRestName().trim())){
				holder.tvResName.setText(R.string.text_null_hanzi);
			}else{
				holder.tvResName.setText(rest.getRestName());
			}
			
			//设置人均消费
			String label = "人均: ";
			if("".equals(rest.getAvgPrice().trim())){
				holder.tvAvgPrice.setText(label + this.context.getResources().getString(R.string.text_null_hanzi));
			}else{
				holder.tvAvgPrice.setText(label + "¥" + rest.getAvgPrice());
			}
			
			//设置距离
			if(!CheckUtil.isEmpty(rest.getDistanceMeter())){
				holder.tvResDistance.setVisibility(View.VISIBLE);
				holder.tvResDistance.setText(String.valueOf(rest.getDistanceMeter()));
			}else{
				holder.tvResDistance.setVisibility(View.GONE);
			}
			
			if (TextUtils.isEmpty(rest.getDiscount())) {
				 holder.vgDiscount.setVisibility(View.GONE);
			} else {
				holder.vgDiscount.setVisibility(View.VISIBLE);
				holder.tvDiscount.setText(rest.getDiscount());
			}
			
			holder.tvLike.setText(rest.getLikePct());
			
			holder.resLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (mRoomTypeButtonListener != null) {
						mRoomTypeButtonListener.onClickRes(v, rest);
					}
				}
			});
			
			if (rest.getRoomState() == null) {
				setButtonState(holder.btFirst, 0, "");
				setButtonState(holder.btSecond, 0, "");
				holder.btFirst.setOnClickListener(null);
				holder.btSecond.setOnClickListener(null);
			} else {
				String hallText = rest.getRoomState().getHallName();
				if (TextUtils.isEmpty(hallText)) {
					hallText = "大厅";
				}
				setButtonState(holder.btFirst, rest.getRoomState().getHallTag(), hallText);
				
				String roomText = rest.getRoomState().getRoomName();
				if (TextUtils.isEmpty(roomText)) {
					roomText = "包房";
				}
				setButtonState(holder.btSecond, rest.getRoomState().getRoomTag(), roomText);
				
				holder.btFirst.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						if (mRoomTypeButtonListener != null) {
							mRoomTypeButtonListener.onClickFirst(v, rest);
						}
					}
				});
				holder.btSecond.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						if (mRoomTypeButtonListener != null) {
							mRoomTypeButtonListener.onClickSecond(v, rest);
						}
					}
				});
			}
		} else {
			//消息的场合
			holder.resLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			//设置消息
			if (context.getString(R.string.text_info_loading).equals(rest.getDescription())) {
				//载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(rest.getDescription());
			
			//处理添加餐厅按钮
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
						ActivityUtil.jumpNotForResult(context, AddOrUpdateResActivity.class, new Bundle(), false);
					}
				});
			}
			else {
				holder.btnAddNewRes.setVisibility(View.GONE);
				holder.btnAddNewRes.setOnClickListener(null);
			}
			//是否显示文本信息
			if(showMessage){
				holder.tvMsg.setVisibility(View.VISIBLE);
			}
			else{
				holder.tvMsg.setVisibility(View.GONE);
			}
			//是否显示重试按钮
			if(mIsShowRetryBtn){
				holder.btnRetry.setVisibility(View.VISIBLE);
				holder.btnRetry.setOnClickListener(this.retryButtonListener);
			}
			else{
				holder.btnRetry.setVisibility(View.GONE);
			}
		}
		convertView.setTag(holder);
		return convertView;
	}

	public List<RealTimeTableRestData> getList() {
		return list;
	}
	
	
	public void setList(List<RealTimeTableRestData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<RealTimeTableRestData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}
	
	public void addList(List<RealTimeTableRestData> list, boolean isLast) {
		//删除最后一条消息
		this.list.remove(this.list.size() - 1);
		this.list.addAll(createMsgDataToList(list, isLast));
		notifyDataSetChanged();
	}
	
	public void setRoomTypeButtonListener(RoomTypeButtonListener listener) {
		mRoomTypeButtonListener = listener;
	}
	
	
	/**
	 * 建立最后一条数据
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<RealTimeTableRestData> createMsgDataToList(List<RealTimeTableRestData> list, boolean isLast) {
		RealTimeTableRestData msgData = new RealTimeTableRestData();
		msgData.setRestId(String.valueOf(Settings.CONTRL_ITEM_ID)); //-1:消息提示
		String msg = "";
		mIsShowRetryBtn=false;
		if (list.size() == 0&&isLast == true) {
			//检查网络是否连通
	        boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
	        if (!isNetAvailable) {
	        	//没有网络时
	        	msg = context.getString(R.string.text_info_net_unavailable);
	        	mIsShowAddResBtn = false;
			} else {
				//没有找到时
					
					msg = context.getString(R.string.text_info_not_found);
					
					if (!isTopList) {
						
						mIsShowAddResBtn = true;
						msg = context.getString(R.string.text_layout_res_not_found);
					}
					if(this.list.size()>0){
						//如果是网络连接异常没有取到后续数据（前面有数据），
						//不显示“没有找到餐厅...”文字，只显示重试按钮
						msg=null; 
						mIsShowRetryBtn=true;
						mIsShowAddResBtn = false;
					}
				
			}
		} else if(isLast == false){
			
			//不是在最后一页时
			msg = context.getString(R.string.text_info_loading);
			mIsShowAddResBtn = false;
		}
		else if(list.size() > 0&&isLast == true){			
			mIsShowAddResBtn = false;
//			if (listType.equals(Settings.STATUTE_CHANNEL_RESTAURANT)) {
//				if(!isTopList){ //如果不是榜单，msg置为null，否则msg还是空字符串，实现榜单情况下不显示“添加餐厅按钮”
//					msg = null;//不显示文字//"添加小秘书不知道的店，立即获得50秘币"; 
//				}
//				mIsShowAddResBtn = true;
//			}
//			else{
//				mIsShowAddResBtn = false;
//			}
		}
		msgData.setDescription(msg);
		
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
	
	private void setButtonState(Button btBook, int tag, String text) {
		if (tag == Settings.REAL_TIME_ENOUGH) {
//			btBook.getBackground().setLevel(0);
			ViewUtils.setBackgroudResource(btBook, R.drawable.book_button_green);
			btBook.setText(text);
			btBook.setEnabled(true);
			btBook.setClickable(true);
		} else if (tag == Settings.REAL_TIME_FEW) {
//			btBook.getBackground().setLevel(1);
			ViewUtils.setBackgroudResource(btBook, R.drawable.book_button_red);
			btBook.setText(text);
			btBook.setEnabled(true);
			btBook.setClickable(true);
		} else if (tag == Settings.REAL_TIME_FULL) {
//			btBook.getBackground().setLevel(2);
			ViewUtils.setBackgroudResource(btBook, R.drawable.button_gray);
			btBook.setText(text);
			btBook.setEnabled(false);
			btBook.setClickable(false);
		} else if (tag == Settings.REAL_TIME_DISABLE) {
//			btBook.getBackground().setLevel(2);
			ViewUtils.setBackgroudResource(btBook, R.drawable.button_gray);
			btBook.setText(text);
			btBook.setEnabled(false);
			btBook.setClickable(false);
		} else {
			if (TextUtils.isEmpty(text)) {
				btBook.setVisibility(View.GONE);
			} else {
//				btBook.getBackground().setLevel(2);
				ViewUtils.setBackgroudResource(btBook, R.drawable.button_gray);
				btBook.setText(text);
				btBook.setEnabled(false);
				btBook.setClickable(false);
			}
		}
	}
}
