package com.fg114.main.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 解决ListView与scrollView嵌套问题，重载ListView
 * @author zhangyifan
 *
 */
public class MyListView extends ListView {
	private int id;
	private boolean isRefreshFoot = false;
	public MyListView(Context context, AttributeSet attrs) {
	  super(context, attrs);
	}

	public MyListView(Context context) {
	  super(context);
	}
	
	public MyListView(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	}

	public synchronized final int getId()
	{
		return id;
	}

	public synchronized final void setId(int id)
	{
		this.id = id;
	}

	public synchronized final boolean isRefreshFoot()
	{
		return isRefreshFoot;
	}

	public synchronized final void setRefreshFoot(boolean isRefreshFoot)
	{
		this.isRefreshFoot = isRefreshFoot;
	}

	@Override     
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	   
	   int expandSpec = 
		   MeasureSpec.makeMeasureSpec(
				   Integer.MAX_VALUE >> 2,
				   MeasureSpec.AT_MOST);            
	   super.onMeasure(widthMeasureSpec, expandSpec);
	} 
}
