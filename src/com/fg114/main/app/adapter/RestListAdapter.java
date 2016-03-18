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
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;


/**
 * 餐厅列表适配器
 * @author zhangyifan
 *
 */
public class RestListAdapter extends BaseAdapter {

	private static final String TAG = "RestListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	
	private List<RestListData> list = new ArrayList<RestListData>();
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

	public RestListAdapter(Context c,View.OnClickListener retryButtonListener) {
		this(c,retryButtonListener,false);	
	}
	public RestListAdapter(Context c,View.OnClickListener retryButtonListener,boolean isTopList) {
		this(c,retryButtonListener,isTopList,true);	
	}
	public RestListAdapter(Context c,View.OnClickListener retryButtonListener,boolean isTopList,boolean showPicture){
		super();
		this.context = c;
		this.showPicture=showPicture;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.retryButtonListener=retryButtonListener;
		this.isTopList=isTopList;
	}
	public RestListAdapter(Context c) {
		this(c,null,false);		
	}
	public RestListAdapter(Context c,boolean isTopList) {
		this(c,null,isTopList);		
	}
	public RestListAdapter(Context c,boolean isTopList,boolean showPicture) {
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
		RelativeLayout layout;
		//餐厅部分
		LinearLayout resLayout;
		MyImageView ivResLogo;
		TextView tvResName;
		
		ImageView res_food_list_item_promotion_icon_mibi;
		TextView res_food_list_item_promotion_mibi;
		TextView res_food_list_item_promotion_discount;
		TextView res_food_list_item_promotion_coupon;
		
		RatingBar rbResStarGrade;
		TextView tvResPrice;
		TextView tvResCookingStyle;
		TextView tvResDistance;
		ViewGroup restLogoLayout;
		//消息部分
		LinearLayout msgLayout;
		ProgressBar pbBar;
		TextView tvMsg;
		Button btnAddNewRes;
		Button btnRetry;
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
		    convertView = mInflater.inflate(R.layout.list_item_restaurant_and_food, null);
		    holder.layout = (RelativeLayout) convertView.findViewById(R.id.res_food_list_item_layout);
		    holder.resLayout = (LinearLayout) convertView.findViewById(R.id.res_food_list_item_resLayout);
		    holder.ivResLogo = (MyImageView) convertView.findViewById(R.id.res_food_list_item_ivResLogo);
		    holder.tvResName = (TextView) convertView.findViewById(R.id.res_food_list_item_tvResName);
		    
		    holder.res_food_list_item_promotion_icon_mibi = (ImageView) convertView.findViewById(R.id.res_food_list_item_promotion_icon_mibi);
		    holder.res_food_list_item_promotion_mibi = (TextView) convertView.findViewById(R.id.res_food_list_item_promotion_mibi);
		    holder.res_food_list_item_promotion_discount = (TextView) convertView.findViewById(R.id.res_food_list_item_promotion_discount);
		    holder.res_food_list_item_promotion_coupon = (TextView) convertView.findViewById(R.id.res_food_list_item_promotion_coupon);
		    
		    holder.rbResStarGrade = (RatingBar) convertView.findViewById(R.id.res_food_list_item_rbStar);
		    holder.tvResPrice = (TextView) convertView.findViewById(R.id.res_food_list_item_tvResPerCapital);
		    holder.tvResCookingStyle = (TextView) convertView.findViewById(R.id.res_food_list_item_tvResDishType);
		    holder.tvResDistance = (TextView) convertView.findViewById(R.id.res_food_list_item_tvResDistance);
		    holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.res_food_list_item_msgLayout);
		    holder.pbBar = (ProgressBar) convertView.findViewById(R.id.res_food_list_item_pBar);
		    holder.tvMsg = (TextView) convertView.findViewById(R.id.res_food_list_item_tvMsg);
		    holder.btnAddNewRes = (Button) convertView.findViewById(R.id.res_food_list_item_btnAddNewRes);
		    holder.btnRetry = (Button) convertView.findViewById(R.id.res_food_list_item_btnRetry);
		    holder.restLogoLayout = (ViewGroup) convertView.findViewById(R.id.res_food_list_item_restLogo_layout);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
	   
		
		
		//列表内容设置
		RestListData data = list.get(position);

		if (data.iconTag!=Settings.CONTRL_ITEM_ID) {
			//餐馆的场合
			holder.layout.setVisibility(View.VISIBLE);
			holder.resLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);
			//设置餐馆图片
			viewList.add(holder.ivResLogo);
			if(showPicture){
				holder.ivResLogo.setImageByUrl(data.picUrl,
					true,
					position,
					ScaleType.CENTER_CROP);
			}
			else{
				holder.restLogoLayout.setVisibility(View.GONE);
			}
			
			
			//设置餐馆名字
			if("".equals(data.restName)){
				holder.tvResName.setText(R.string.text_null_hanzi);
			}else{
				holder.tvResName.setText(data.restName);
			}
			
			//设置优惠图标和文字
			//图标标志  0:无图标  1：券  2：惠  3：币 4：币(高亮)
			if(data.iconTag==1){
				holder.res_food_list_item_promotion_icon_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_discount.setVisibility(View.GONE);
				
				holder.res_food_list_item_promotion_coupon.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_coupon.setText(data.iconTitle);
			}else if(data.iconTag==2){
				holder.res_food_list_item_promotion_icon_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_coupon.setVisibility(View.GONE);
				
				holder.res_food_list_item_promotion_discount.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_discount.setText(data.iconTitle);
			}else if(data.iconTag==3){
				holder.res_food_list_item_promotion_discount.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_coupon.setVisibility(View.GONE);
				
				holder.res_food_list_item_promotion_mibi.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_mibi.setText(data.iconTitle);
				holder.res_food_list_item_promotion_icon_mibi.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_1);
			}else if(data.iconTag==4){
				holder.res_food_list_item_promotion_discount.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_coupon.setVisibility(View.GONE);
				
				holder.res_food_list_item_promotion_mibi.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_mibi.setText(data.iconTitle);
				holder.res_food_list_item_promotion_icon_mibi.setVisibility(View.VISIBLE);
				holder.res_food_list_item_promotion_icon_mibi.setImageResource(R.drawable.icon_mibi_2);
			}else{
				holder.res_food_list_item_promotion_icon_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_mibi.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_discount.setVisibility(View.GONE);
				holder.res_food_list_item_promotion_coupon.setVisibility(View.GONE);
			}
			
			//设置餐馆星级
			double overallNum = data.overallNum;
			holder.rbResStarGrade.setMinimumHeight(1);
			holder.rbResStarGrade.setRating((float)overallNum);
			//设置人均消费
			if("".equals(data.avgPrice)){
				holder.tvResPrice.setText(this.context.getResources().getString(R.string.text_null_hanzi));
			}else{
				holder.tvResPrice.setText(data.avgPrice);
			}
			//设置菜系
			if("".equals(data.describe)){
				holder.tvResCookingStyle.setText(this.context.getResources().getString(R.string.text_null_hanzi));
			}else{
				holder.tvResCookingStyle.setText(data.describe);
			}
			//设置距离
			if(!CheckUtil.isEmpty(data.distance)){
				holder.tvResDistance.setVisibility(View.VISIBLE);
				holder.tvResDistance.setText(String.valueOf(data.distance));
			}else{
				holder.tvResDistance.setVisibility(View.INVISIBLE);
			}
		} else {
			//消息的场合
			holder.layout.setVisibility(View.GONE);
			holder.resLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			//设置消息
			if (context.getString(R.string.text_info_loading).equals(data.describe)) {
				//载入的场合
				holder.pbBar.setVisibility(View.VISIBLE);
			} else {
				holder.pbBar.setVisibility(View.GONE);
			}
			holder.tvMsg.setText(data.describe);
			
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

	public List<RestListData> getList() {
		return list;
	}
	
	
	public void setList(List<RestListData> list, boolean isLast, String listType) {
		if (list == null) {
			list = new ArrayList<RestListData>();
		}
		this.list = createMsgDataToList(list, isLast, listType);
		notifyDataSetChanged();
	}
	
	public void addList(List<RestListData> list, boolean isLast,  String listType) {
		//删除最后一条消息
		this.list.remove(this.list.size() - 1);
		this.list.addAll(createMsgDataToList(list, isLast, listType));
		notifyDataSetChanged();
	}
	
	
	/**
	 * 建立最后一条数据
	 * @param listSize
	 * @param pageInfo
	 * @return
	 */
	private List<RestListData> createMsgDataToList(List<RestListData> list, boolean isLast, String listType) {
		RestListData msgData = new RestListData();
		msgData.iconTag=Settings.CONTRL_ITEM_ID; //-1:消息提示
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
					
					if (listType.equals(Settings.STATUTE_CHANNEL_RESTAURANT)&&!isTopList) {
						
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
			if (listType.equals(Settings.STATUTE_CHANNEL_RESTAURANT)) {
				if(!isTopList){ //如果不是榜单，msg置为null，否则msg还是空字符串，实现榜单情况下不显示“添加餐厅按钮”
					msg = null;//不显示文字//"添加小秘书不知道的店，立即获得50秘币"; 
				}
				mIsShowAddResBtn = true;
			}
			else{
				mIsShowAddResBtn = false;
			}
		}
		msgData.describe=msg;
		
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
