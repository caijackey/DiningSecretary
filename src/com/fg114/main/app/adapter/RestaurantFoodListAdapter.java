package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.FoodCommentActivity;
import com.fg114.main.app.activity.resandfood.FoodCommentSubmitActivity;
import com.fg114.main.app.activity.resandfood.FoodInfoActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.resandfood.RestaurantFoodListActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.OrderList2Data;
import com.fg114.main.service.dto.ResFoodCommentData;
import com.fg114.main.service.dto.ResFoodData3;
import com.fg114.main.service.dto.ResFoodList3DTO;
import com.fg114.main.service.dto.TakeoutRestListData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnitUtil;

/**
 * 餐厅菜品适配器
 * 
 * @author xujianjun,2012-08-01
 * 
 */
public class RestaurantFoodListAdapter extends BaseAdapter implements View.OnClickListener {

	private static final String TAG = "RestaurantFoodListAdapter";
	private static final boolean DEBUG = Settings.DEBUG;
	public String restName="餐厅";
	
	//--
	public Set<MyImageView> viewList = new HashSet<MyImageView>();

	private List<ResFoodData3> list = null;
	private LayoutInflater mInflater = null;
	private Context context;
	private View.OnClickListener retryListener;
	//--已展开的菜品数据
	private ResFoodData3 expanedData=null;
	private ViewHolder expanedHolder=null;
	
	public RestaurantFoodListAdapter(Context c, View.OnClickListener retryListener) {
		super();
		this.context = c;
		this.retryListener = retryListener;
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
		// 根view
		View root;
		// 层
		ViewGroup mainLayout;
		ViewGroup expandableLayout;
		ViewGroup msgLayout;
		ViewGroup commentArea;
		ViewGroup detailLayout;
		// 菜品
		MyImageView image;
		ImageView arrow;
		TextView foodName;
		TextView price;
		TextView hotNum;
		Button uploadButton;
		Button reportErrorButton;
		// 评论
		View detailLine;
		TextView detail;
		TextView commentLabel;
		TextView userName;
		TextView commentDate;
		TextView commentContent;
		ImageView commentArrow;
		Button submitButton;
		// 信息区
		ProgressBar pBar;
		TextView message;
		Button retryButton;
		// 数据
		ResFoodData3 data;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_restaurant_food_list, null);
			holder.root = convertView;
			holder.mainLayout = (ViewGroup) convertView.findViewById(R.id.main_layout);
			holder.expandableLayout = (ViewGroup) convertView.findViewById(R.id.expandable_layout);
			holder.msgLayout = (ViewGroup) convertView.findViewById(R.id.msg_layout);
			holder.commentArea = (ViewGroup) convertView.findViewById(R.id.comment_area);
			holder.detailLayout = (ViewGroup) convertView.findViewById(R.id.detail_layout);

			holder.image = (MyImageView) convertView.findViewById(R.id.image);
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
			holder.foodName = (TextView) convertView.findViewById(R.id.food_name);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			holder.hotNum = (TextView) convertView.findViewById(R.id.hot_num);
			holder.uploadButton = (Button) convertView.findViewById(R.id.upload_button);
			holder.reportErrorButton = (Button) convertView.findViewById(R.id.report_error_button);
			holder.detailLine =  convertView.findViewById(R.id.detail_line);
			holder.detail = (TextView) convertView.findViewById(R.id.detail);

			holder.commentLabel = (TextView) convertView.findViewById(R.id.comment_label);
			holder.userName = (TextView) convertView.findViewById(R.id.user_name);
			holder.commentDate = (TextView) convertView.findViewById(R.id.comment_date);
			holder.commentContent = (TextView) convertView.findViewById(R.id.comment_content);
			holder.commentArrow = (ImageView) convertView.findViewById(R.id.comment_arrow);
			holder.submitButton = (Button) convertView.findViewById(R.id.submit_button);

			holder.pBar = (ProgressBar) convertView.findViewById(R.id.pBar);
			holder.message = (TextView) convertView.findViewById(R.id.tvMsg);
			holder.retryButton = (Button) convertView.findViewById(R.id.btnRetry);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 列表内容设置
		holder.data = list.get(position);
		//holder.root.setBackgroundDrawable(new BitmapDrawable((Bitmap)null));

		if (!holder.data.getUuid().equals(String.valueOf(Settings.CONTRL_ITEM_ID))) {
			// 默认是收起状态
			holder.mainLayout.setVisibility(View.VISIBLE);
			holder.expandableLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.GONE);

			
			//holder.data.isExpanded = false;
			holder.mainLayout.setOnClickListener(this);
			holder.mainLayout.setTag(holder);

			holder.detailLayout.setOnClickListener(this);
			holder.detailLayout.setTag(holder);

			// 设置名字
			if ("".equals(holder.data.getName().trim())) {
				holder.foodName.setText(R.string.text_null_hanzi);
			} else {
				holder.foodName.setText(holder.data.getName());
			}
			//--
			viewList.add(holder.image);
			//--
			holder.price.setText(Html.fromHtml("<font color=\"#FF0000\">"+holder.data.getPrice()+"</font>"+holder.data.getUnit()));
			
			holder.image.setImageByUrl(holder.data.getPicUrl(), true, position, ScaleType.FIT_XY);
			holder.image.setOnClickListener(this);
			holder.image.setTag(holder);
			
			holder.arrow.setImageResource(R.drawable.arrow_down);

			holder.hotNum.setText("人气：" + holder.data.getHotNum());

			holder.uploadButton.setOnClickListener(this);
			holder.uploadButton.setTag(holder);

			holder.reportErrorButton.setOnClickListener(this);
			holder.reportErrorButton.setTag(holder);

			if(!CheckUtil.isEmpty(holder.data.getIntro())){
				holder.detailLine.setVisibility(View.VISIBLE);
				holder.detailLayout.setVisibility(View.VISIBLE);
				holder.detail.setText(holder.data.getIntro());				
			}else{
				holder.detailLine.setVisibility(View.GONE);
				holder.detailLayout.setVisibility(View.GONE);
			}

			setCommentViews(holder);
			holder.submitButton.setOnClickListener(this);
			holder.submitButton.setTag(holder);
			//--
			setMainLayoutStatus(holder);
			if(holder.data.isExpanded){
				setItemSelected(holder);
			}

		} else {
			// 消息的场合
			holder.mainLayout.setVisibility(View.GONE);
			holder.expandableLayout.setVisibility(View.GONE);
			holder.msgLayout.setVisibility(View.VISIBLE);
			// 设置消息
			if (context.getString(R.string.text_info_loading).equals(holder.data.getName())) {
				// 载入的场合
				holder.pBar.setVisibility(View.VISIBLE);
			} else {
				holder.pBar.setVisibility(View.GONE);
			}
			holder.message.setText(holder.data.getName());
			
			//网络故障，请重试！
			if("网络故障，请重试！".equals(holder.data.getName())){
				holder.message.setVisibility(View.GONE);
				holder.retryButton.setVisibility(View.VISIBLE);
				holder.retryButton.setOnClickListener(retryListener);
			}else{
				holder.message.setVisibility(View.VISIBLE);
				holder.retryButton.setVisibility(View.GONE);
			}			
		}
		
		convertView.setTag(holder);
		
		return convertView;
	}

	public List<ResFoodData3> getList() {
		return list;
	}

	public void setList(List<ResFoodData3> list, boolean isLast) {
		if (list == null) {
			list = new ArrayList<ResFoodData3>();
		}
		this.list = createMsgDataToList(list, isLast);
		notifyDataSetChanged();
	}

	public void addList(List<ResFoodData3> list, boolean isLast) {
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
	private List<ResFoodData3> createMsgDataToList(List<ResFoodData3> list, boolean isLast) {
		ResFoodData3 msgData = new ResFoodData3();
		msgData.setUuid(String.valueOf(Settings.CONTRL_ITEM_ID)); // -1:消息提示
		String msg = "";
		if (list.size() == 0 && isLast == true) {
			// 检查网络是否连通
			boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(context.getApplicationContext());
			if (!isNetAvailable) {
				// 没有网络时
				msg = context.getString(R.string.text_info_net_unavailable);
				
			}else if(this.list.size()>0){
				//如果是网络连接异常没有取到后续数据（列表前面有数据this.list.size()>0），
				//不显示提示文字，只显示重试按钮
				msg="网络故障，请重试！";
					
			}else{
				// 没有找到时
				msg = "没有菜品信息";		
			}
		} else {
			if (isLast == false) {
				// 不是在最后一页时
				msg = context.getString(R.string.text_info_loading);
			}
		}
		msgData.setName(msg);

		if (!"".equals(msg)) {
			list.add(msgData);
		}

		return list;
	}

	//点击事件处理
	@Override
	public void onClick(View v) {
		final ViewHolder holder = (ViewHolder) v.getTag();
		if (holder == null) {
			return;
		}
		// ---
		Bundle bundle;
		switch (v.getId()) {
		
			// 切换展开/收起-------------------------------------
			case R.id.main_layout:
				holder.data.isExpanded=!holder.data.isExpanded;
				setMainLayoutStatus(holder);
				if(holder.data.isExpanded){
					setItemSelected(holder);
				}
				break;
				
			// 有奖传图-------------------------------------
			case R.id.upload_button:
				if (ActivityUtil.checkMysoftStage(context)) {
					
					((RestaurantFoodListActivity) context).mFlag = RestaurantFoodListActivity.ADAPTERFLAG;
					((RestaurantFoodListActivity) context).uploadPicFoodId = holder.data.getUuid();
					((RestaurantFoodListActivity) context).uploadPicFoodName = holder.data.getName();
					
					ButtonPanelUtil pan=new ButtonPanelUtil();
					pan.showUploadPanel(v,(Activity) context, null);
					pan.setOnGetUriListener(
							new ButtonPanelUtil.OnGetUriListener() {
								@Override
								public void onGetUri(Uri uri) {
									((RestaurantFoodListActivity) context).takePhotoUri = uri;									
								}
							});
				}
				break;
				
			// 菜品报错-------------------------------------
			case R.id.report_error_button:
				bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_ERROR_REPORT_TYPE, Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_FOOD);
				bundle.putString(Settings.UUID, holder.data.getUuid());	
				// 这里还要添加可能的错误列表数据，以供选择列表对话框显示
				try {
					DialogUtil.showErrorReportTypeSelectionDialog(context, bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			// 到菜品详情页-------------------------------------
			case R.id.detail_layout:
				bundle = new Bundle();				
				bundle.putStringArray(Settings.BUNDLE_KEY_ID, new String[]{ holder.data.getUuid(), holder.data.getName(), restName});
				bundle.putString(Settings.BUNDLE_FOOD_INFO, holder.data.getIntro());
				ActivityUtil.jump(context, FoodInfoActivity.class,  0, bundle);
				break;
				
			// 到评论列表页-------------------------------------
			case R.id.comment_area:
				bundle = new Bundle();				
				bundle.putStringArray(Settings.BUNDLE_KEY_ID, new String[]{ holder.data.getUuid(), holder.data.getName()});
				ActivityUtil.jump(context, FoodCommentActivity.class,  0, bundle);
				break;
				
			// 到发表评论页-------------------------------------
			case R.id.submit_button:
				DialogUtil.showUserLoginDialogWhenFoodComment((Activity) context,
						new Runnable() {
							@Override
							public void run() {
								Bundle bundle = new Bundle();
								String[] value = { holder.data.getUuid(), holder.data.getName()};
								bundle.putStringArray(Settings.BUNDLE_KEY_ID, value);
								ActivityUtil.jump(context, FoodCommentSubmitActivity.class,  0, bundle);
							}
						},0);
				break;
				
			// 查看大图片-------------------------------------
			case R.id.image:
				DialogUtil.createImageViewPanel((Activity) context,holder.root, holder.data.getPicOriginalUrl());
				break;
		}
	}
	
	//将viewHolder包含的数据设置为选中
	private void setItemSelected(ViewHolder holder) {
		
		if(expanedData!=null && expanedHolder!=null && expanedData!=holder.data ){
			expanedData.isExpanded=false;
			//如果是同一数据，表示选中项目没有移出屏幕
			if(expanedHolder.data==expanedData){
				setMainLayoutStatus(expanedHolder);
			}
		}
		
		holder.data.isExpanded=true;
		expanedData=holder.data;
		expanedHolder=holder;
		
	}

	/**
	 * 设置mainlayout的展开、收起状态
	 */
	private void setMainLayoutStatus(final ViewHolder holder) {
		if (!holder.data.isExpanded) {
			holder.root.setBackgroundResource(R.drawable.middle_list_item_bg);
			holder.mainLayout.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(5));
			holder.expandableLayout.setVisibility(View.GONE);
			holder.arrow.setImageResource(R.drawable.arrow_down);
			
		} else {
			holder.root.setBackgroundResource(R.drawable.bg_expand);
			holder.mainLayout.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(20), UnitUtil.dip2px(10), UnitUtil.dip2px(5));
			holder.expandableLayout.setVisibility(View.VISIBLE);
			holder.arrow.setImageResource(R.drawable.arrow_up);
		}
	}
	//将某菜的最新评论信息更新到列表中
	public void updateRecentComment(ResFoodCommentData recentCommentData) {
		if(list==null||list.size()<=0||expanedData==null){
			return;
		}
		ResFoodCommentData comment=expanedData.getCommentData();
		if(expanedData!=null && expanedHolder!=null && comment!=null){
//			if(recentCommentData.totalCommentNum==-999){
//				expanedData.setTotalCommentNum(expanedData.getTotalCommentNum()+1);
//			}else{
//				expanedData.setTotalCommentNum(recentCommentData.totalCommentNum);
//			}
			expanedData.setCommentData(recentCommentData);
			setCommentViews(expanedHolder);
		}
	}
	//设置评论数据
	private void setCommentViews(ViewHolder holder) {
		// 评论数据
		if (holder.data.getTotalCommentNum() <= 0) {
			holder.userName.setVisibility(View.GONE);
			holder.commentDate.setVisibility(View.GONE);

			holder.commentLabel.setText(Html.fromHtml("评论<br />(0)"));
			holder.commentContent.setText(holder.data.getCommentData().detail);
			holder.commentArea.setOnClickListener(null);
			holder.commentArrow.setVisibility(View.GONE);
		} else {

			holder.userName.setVisibility(View.VISIBLE);
			holder.commentDate.setVisibility(View.VISIBLE);
			
			holder.userName.setText(holder.data.getCommentData().userName);
			holder.commentDate.setText(ConvertUtil.convertLongToDateString(holder.data.getCommentData().createTime,"yyyy-MM-dd HH:mm"));
			holder.commentLabel.setText(Html.fromHtml("评论<br />(" + holder.data.getTotalCommentNum() + ")"));
			holder.commentContent.setText(holder.data.getCommentData().detail);
			holder.commentArea.setOnClickListener(this);
			holder.commentArea.setTag(holder);
			holder.commentArrow.setVisibility(View.VISIBLE);
		}
	}
}
