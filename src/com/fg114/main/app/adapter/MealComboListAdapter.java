package com.fg114.main.app.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
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
import com.fg114.main.service.dto.CashCouponData;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.MealComboData;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ViewUtils;


/**
 * 特惠套餐列表适配器
 * @author xujianjun,2012-07-23
 *
 */
public class MealComboListAdapter extends BaseAdapter {

	private static final String TAG = "MealComboListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;

	private List<CashCouponData> list = new ArrayList<CashCouponData>();
	public Set<MyImageView> viewList = new HashSet<MyImageView>();

	private LayoutInflater mInflater = null;
	private Context context;
	private View.OnClickListener retryButtonListener;

	// 是否显示重试按钮
	private boolean mIsShowRetryBtn = false;
	//是否显示文本信息
	private boolean showMessage=true;

	public MealComboListAdapter(Context c,View.OnClickListener retryButtonListener){
		super();
		this.context = c;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		ViewGroup mainLayout;
		MyImageView image;
		TextView discountPrice;
		TextView orginalPrice;
		TextView tvState;
		//		TextView description;
		TextView restName;
		TextView remainNum;
		TextView location;
		//消息区
		LinearLayout msgLayout;
		ProgressBar pBar;
		TextView tvMsg;
		TextView tvMsg_2;
		Button btnRetry;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(DEBUG)Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_meal_combo, null);
			holder.mainLayout = (ViewGroup) convertView.findViewById(R.id.main_layout);
			holder.image = (MyImageView) convertView.findViewById(R.id.image);
			holder.discountPrice = (TextView) convertView.findViewById(R.id.discount_price);
			holder.orginalPrice = (TextView) convertView.findViewById(R.id.original_price);
			holder.tvState = (TextView) convertView.findViewById(R.id.state);
			//		    holder.description = (TextView) convertView.findViewById(R.id.description);
			holder.restName = (TextView) convertView.findViewById(R.id.rest_name);
			holder.remainNum = (TextView) convertView.findViewById(R.id.remain_num);
			holder.location = (TextView) convertView.findViewById(R.id.location);
			//消息区
			holder.msgLayout = (LinearLayout) convertView.findViewById(R.id.msg_layout);
			holder.pBar = (ProgressBar) convertView.findViewById(R.id.pBar);
			holder.tvMsg = (TextView) convertView.findViewById(R.id.tvMsg);
			holder.tvMsg_2 = (TextView) convertView.findViewById(R.id.tvMsg_2);
			holder.btnRetry = (Button) convertView.findViewById(R.id.btnRetry);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}



		//列表内容设置
		CashCouponData data = list.get(position);

		if (data.getStateTag()!=Settings.CONTRL_ITEM_ID) {
			//餐馆的场合
			holder.mainLayout.setVisibility(View.VISIBLE);
			holder.msgLayout.setVisibility(View.GONE);
			// 设置餐馆图片
			viewList.add(holder.image);
			//先加载小图
			holder.image.setImageByUrl(data.getSmallPicUrl(), true, position, ScaleType.FIT_XY);

			//---
			holder.discountPrice.setText("¥" + data.getUnitPriceNum());
			holder.orginalPrice.setText("¥" + data.getOldUnitPriceNum());
			ViewUtils.setStrikethrough(holder.orginalPrice);			
			//			holder.description.setText(data.getName() + "　" + data.getShortDescribe());
			holder.restName.setText(data.getRestName());
			holder.remainNum.setText("已售" + data.soldNum+ "张");

			if (TextUtils.isEmpty(data.getPlace())) {
				holder.location.setVisibility(View.GONE);
			} else {
				holder.location.setVisibility(View.VISIBLE);
				holder.location.setText(data.getPlace());
			}


			if (data.getStateTag() == 1) {
				holder.tvState.setText("");
				holder.tvState.setVisibility(View.GONE);
			} else if (data.getStateTag() == 2) {
				holder.tvState.setText("已到期");
				holder.tvState.setVisibility(View.VISIBLE);
			} else if (data.getStateTag() == 3) {
				holder.tvState.setText("已卖完");
				holder.tvState.setVisibility(View.VISIBLE);
			} else if(data.getStateTag() == 4){
				holder.tvState.setText(Html.fromHtml("<font color=\"#0000BB\">即将开始</font>"));
				holder.tvState.setVisibility(View.VISIBLE);
			}else if(data.getStateTag() == 5){
				holder.tvState.setText(Html.fromHtml("<font color=\"#21ab21\">返秘币</font>"));
				holder.tvState.setVisibility(View.VISIBLE);
			}else {
				holder.tvState.setText("");
				holder.tvState.setVisibility(View.GONE);
			}

		} else {
			//消息的场合
			holder.mainLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);

			//载入信息时需要显示进度
			if (context.getString(R.string.text_info_loading).equals(data.getName())) {
				holder.pBar.setVisibility(View.VISIBLE);
			} else {
				holder.pBar.setVisibility(View.GONE);
			}

			//是否显示文本信息
			if(data.getName()!=null){
				holder.tvMsg.setText(data.getName());
				holder.tvMsg_2.setVisibility(View.VISIBLE);
				holder.tvMsg.setVisibility(View.VISIBLE);
			}
			else{
				holder.tvMsg.setVisibility(View.GONE);
				holder.tvMsg_2.setVisibility(View.GONE);
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

	public List<CashCouponData> getList() {
		return list;
	}


	public void setList(List<CashCouponData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<CashCouponData>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<CashCouponData> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<CashCouponData>();
		}
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
	private List<CashCouponData> createMsgDataToList(List<CashCouponData> list, boolean isLast) {
		CashCouponData msgData = new CashCouponData();
		msgData.setStateTag(Settings.CONTRL_ITEM_ID); //-1:消息提示		
		mIsShowRetryBtn=false;

		//---------不是最后一页时，显示“正在载入”-------------
		if(isLast == false){
			msgData.setName(context.getString(R.string.text_info_loading));//消息放入name里
			list.add(msgData);
			return list;
		}

		//---------取的是最后一页的数据，什么都不干-----------
		if(list.size()>0 && isLast==true){			
			return list;
		}

		//----------剩下情况是size=0&&isLast=true
		//先检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络时
			msgData.setName(context.getString(R.string.text_info_net_unavailable));//消息放入name里
			list.add(msgData);
			return list;
		} 

		//有网络，但没有取到后续数据（前面有数据），是网络故障，显示重试按钮，允许重试
		if (this.list.size() > 0) {

			mIsShowRetryBtn = true;
			list.add(msgData);
			return list;
		}
		//有网络，adapter的list中没有数据，说明没有找到任何结果
		msgData.setName(context.getString(R.string.text_info_not_found));//消息放入name里
		list.add(msgData);
		return list;

	}
}
