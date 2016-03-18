/**
 * 外卖餐厅详情页餐厅资质里面用到的显示图片的组件
 * 继承HorizontalScrollView,创建时会添加一个LinearLayout作为图片的容器
 * 在xml文件里面直接引用，然后即可在class里面调用setImageData
 * @author sunquan 
 */

package com.fg114.main.app.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.service.dto.TakeoutCerData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;

public class CerImageHorizontalScrollView extends HorizontalScrollView {
	
	private Context context;
	private LayoutInflater mInflater = null;
	List<RestPicData> imageList;
	/**
	 * 装图片的容器
	 */
	private LinearLayout imageListGroup;
	
	public CerImageHorizontalScrollView(Context context) {
		super(context);
		
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public CerImageHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public CerImageHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
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
	public void setImageData(List<TakeoutCerData> picDatas){
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
		imageList=new ArrayList<RestPicData>();
		for(int j = 0; j < picDatas.size(); j++){
			RestPicData restPicData=new RestPicData();
			restPicData.setSmallPicUrl(picDatas.get(j).picUrl);
			restPicData.setPicUrl(picDatas.get(j).bigPicUrl);
			imageList.add(restPicData);
		}
		for (int i = 0; i < picDatas.size(); i++) {
			// 添加底部图片
			
			View imageContain = mInflater.inflate(R.layout.myimageview_cer_image, null);
			MyImageView foodPicture = (MyImageView) imageContain.findViewById(R.id.newtakeaway_cerpicture);
			
			foodPicture.setImageByUrl(picDatas.get(i).picUrl, true, -1, ScaleType.CENTER_CROP);
			listener = new callPicView(this, picDatas.get(i).picUrl);
			foodPicture.setOnClickListener(listener);
			final int m=i;
			imageContain.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("餐厅资质按钮 ");
					// -----

					Bundle bundle = new Bundle();
            		bundle.putInt(Settings.BUNDLE_KEY_ID, m);
					bundle.putSerializable(Settings.BUNDLE_KEY_CONTENT, (Serializable) imageList);
            		ActivityUtil.jump(context, RestaurantGalleryActivity.class, 
            				0, bundle);
				}
			});
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
