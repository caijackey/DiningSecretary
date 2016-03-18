package com.fg114.main.app.view;

import com.fg114.main.util.UnitUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自动换行的布局的实现
 * 
 * @author dengxiangyu
 * 
 */
public class MyViewGroup extends ViewGroup {
	private final static String TAG = "MyViewGroup";
	private int widthPadding=10;
    
	public MyViewGroup(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	protected void onLayout(boolean flag, int i, int j, int k, int l) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int childCount = getChildCount();
		int x = 0;
		int y = 0;
		int row = 0;

		for (int index = 0; index < childCount; index++) {
			final View child = getChildAt(index);
			if (child.getVisibility() != View.GONE) {
				child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
				// 此处增加onlayout中的换行判断，用于计算所需的高度
				int width = child.getMeasuredWidth();
				int height = child.getMeasuredHeight();
				x += (width+UnitUtil.dip2px(widthPadding));
				y = row * height + height;
				if (x > maxWidth) {
					x = width;
					row++;
					y = row * height + height;
				}
			}
		}
		// 设置容器所需的宽度和高度
		setMeasuredDimension(maxWidth, y+UnitUtil.dip2px(10)*(row+1)+UnitUtil.dip2px(10));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		int maxWidth = r - l;
		int x = 0;
		int y = 0;
		int row = 0;
		for (int i = 0; i < childCount; i++) {
			final View child = this.getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				int width = child.getMeasuredWidth();
				int height = child.getMeasuredHeight();
				if (i != 0) {
					x += (width+UnitUtil.dip2px(widthPadding));//控件横向间隔10dip
				} else {
					x += width;
				}
				y = row * height + height;
				if (x > maxWidth) {
					x = width;
					row++;
					y = row * height + height;
				}
						
				child.layout(x - width, y - height+ UnitUtil.dip2px(widthPadding)*(row+1), x, y+UnitUtil.dip2px(widthPadding)*(row+1));
			}
		}

	}
	
	public void setWidthPadding(int width){
		this.widthPadding=width;
	}
	
}
