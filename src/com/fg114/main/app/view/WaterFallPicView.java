package com.fg114.main.app.view;

import com.fg114.main.R;
import com.fg114.main.service.dto.ResPicData2;
import com.fg114.main.service.dto.RestPicData;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
/**
 * 餐厅图片界面只含有单纯图片  瀑布流 内容控件
 * @author liwenjie
 *
 */
public class WaterFallPicView extends WaterFallBaseView
{

	private WaterFallImageView ResImage;
	public WaterFallPicView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
	}

	public WaterFallPicView(Context context)
	{
		super(context);
		
	}

	@Override
	public void LoadViewData()
	{
		try {
			ResImage = (WaterFallImageView) getContentView().findViewById(R.id.flow_pic_WaterFallImageView);
			RestPicData data = getDto();
			if (data != null) {
				int width = data.getSmallPicWidth();
				int height = data.getSmallPicHeight();
				int layoutHeight = (height * getItemWidth()) / width;// 调整高度
				setItemHeight(layoutHeight);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getItemWidth(), layoutHeight);
				LinearLayout.LayoutParams lin = new LinearLayout.LayoutParams(getItemWidth(), getItemHeight());
				lin.setMargins(4, 8, 4, 8);
				lin.gravity=Gravity.CENTER;
				this.setLayoutParams(lin);
				ResImage.setLayoutParams(layoutParams);
				ResImage.setMinimumHeight(layoutHeight);
				ResImage.setMinimumWidth(getItemWidth());
				ResImage.setBackgroundResource(R.drawable.bg_index_new_block);
				ResImage.setImageByUrl(data.getSmallPicUrl(), true, this.getId(), ScaleType.FIT_XY);
			}
		} catch (Exception e) {
			Log.e("bug", "dto:"+getDto().getName());
		}
		
	}

	@Override
	protected int getContentResId()
	{
		// TODO Auto-generated method stub
		return R.layout.fall_water_pic;
	}
	
	
}
