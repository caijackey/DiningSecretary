/**
 * 用户评论里面用到的显示图片的组件
 * 继承HorizontalScrollView,创建时会添加一个LinearLayout作为图片的容器
 * 在xml文件里面直接引用，然后即可在class里面调用setImageData
 * 可以参考餐厅详情页面list_item_comment1.xml RestaurantCommentDetailActivity
 */

package com.fg114.main.app.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.TakeoutCerData;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;

public class CommentImageHorizontalScrollView extends HorizontalScrollView {
	
	private Context context;
	private LayoutInflater mInflater = null;
	/**
	 * 装图片的容器
	 */
	private LinearLayout imageListGroup;
	
	public CommentImageHorizontalScrollView(Context context) {
		super(context);
		
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public CommentImageHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public CommentImageHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * clear image data
	 */
	public void clearImageData(){
		if(imageListGroup==null) return;
		imageListGroup.removeAllViews();
	}
	/**
	 * set image data
	 */
	public void setImageData(List<CommentPicData> picDatas){
		if(picDatas==null) return;
		if(imageListGroup==null){
			imageListGroup = new LinearLayout(context);
			imageListGroup.setOrientation(LinearLayout.HORIZONTAL);
			LayoutParams pars = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			imageListGroup.setLayoutParams(pars);
		    this.addView(imageListGroup);
		}
		else if(imageListGroup.getChildCount()>0) return;   //防止多次加载
		callPicView listener = null;
		for (int i = 0; i < picDatas.size(); i++) {
			// 添加底部图片
			View imageContain = mInflater.inflate(R.layout.myimageview_name_price, null);
			MyImageView foodPicture = (MyImageView) imageContain.findViewById(R.id.foodpicture);
			TextView foodName = (TextView) imageContain.findViewById(R.id.foodname);
			TextView foodPrice = (TextView) imageContain.findViewById(R.id.foodprice);
			foodPicture.setImageByUrl(picDatas.get(i).smallPicUrl, true, -1, ScaleType.CENTER_CROP);
			listener = new callPicView(this, picDatas.get(i).picUrl);
			foodPicture.setOnClickListener(listener);
			
			if (!CheckUtil.isEmpty(picDatas.get(i).title)) {
				foodName.setVisibility(View.VISIBLE);
				foodName.setText(picDatas.get(i).title);
			} else {
				foodName.setVisibility(View.GONE);
			}

			if (!CheckUtil.isEmpty(picDatas.get(i).priceInfo)) {
				foodPrice.setVisibility(View.VISIBLE);
				foodPrice.setText(picDatas.get(i).priceInfo);
			} else {
				foodPrice.setVisibility(View.GONE);
			}
			imageListGroup.addView(imageContain);
		}
	}
	
	/**
	 * get the image counts
	 */
	public int getImageCount(){
		if(imageListGroup==null) return 0;
		else return imageListGroup.getChildCount();
	}
	
	
	
	private class callPicView implements OnClickListener {
		private View parentView;
		private String url;

		public callPicView(View parentView, String url) {
			this.parentView = parentView;
			this.url = url;
		}

		@Override
		public void onClick(View v) {
			DialogUtil.createImageViewPanel((Activity) context, this.parentView, url);
		}

	}
}
