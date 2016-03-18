package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.SharedprefUtil;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class NewFeaturesActivity extends Activity{
	private NewFeaturePageAdapter adapter;
	private List<View> pageView=new ArrayList<View>();
	private ViewPager viewpager;
	private int currIndex = 0;
	private Bundle mBundle;
	private Integer[] images = { R.drawable.new_feature_1, R.drawable.new_feature_2, R.drawable.new_feature_3, R.drawable.new_feature_4,R.drawable.new_feature_welcome};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_features); 
		SharedprefUtil.saveBoolean(NewFeaturesActivity.this, Settings.IS_SHOW_NEW_FEATURE, false);
		
		mBundle = getIntent().getExtras();
		viewpager=(ViewPager) findViewById(R.id.new_feature_viewpager);
		
		
		// 定义一个布局并设置参数  
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(  
                LinearLayout.LayoutParams.WRAP_CONTENT,  
                LinearLayout.LayoutParams.WRAP_CONTENT);  
  
        // 初始化引导图片列表  
        for (int i = 0; i < images.length; i++) {  
            ImageView iv = new ImageView(this);  
            iv.setLayoutParams(mParams);  
            iv.setImageResource(images[i]);  
            pageView.add(iv);  
        }  

		adapter=new NewFeaturePageAdapter();
		viewpager.setAdapter(adapter);
		viewpager.setOnPageChangeListener(new MyOnPageChangeListener());
		

	}
	private class NewFeaturePageAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pageView.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view==(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(pageView.get(position));

		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			((ViewPager)container).addView(pageView.get(position),0);
			
			return pageView.get(position);
			
		}

	}
//	if (mGallery.getSelectedItemPosition()+1 >= images.length-1) {
//		ActivityUtil.jump(NewFeatureActivity.this, IndexActivity.class, 0, mBundle);
//		finish();
//	}
	private class MyOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
			Log.i("sunquan111111122222", state+"-----");
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			currIndex=arg0;
			Log.i("sunquan1111111", currIndex+"====");
			if(currIndex==pageView.size()-1){
				
				ActivityUtil.jump(NewFeaturesActivity.this, IndexActivity.class, 0, mBundle);
				finish();	
			}
		}
		
	}
}
