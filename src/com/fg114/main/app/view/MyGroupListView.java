package com.fg114.main.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MyGroupListView extends ListView
{
	private int id;
	private boolean isRefreshFoot = false;
	private boolean isScroll=true;
	private int pageNo=0;
	public MyGroupListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
	}

	public MyGroupListView(Context context)
	{
		super(context);
		
	}
	public MyGroupListView(Context context, AttributeSet attrs, int defStyle) {
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

	public synchronized final boolean isScroll()
	{
		return isScroll;
	}

	public synchronized final void setScroll(boolean isScroll)
	{
		this.isScroll = isScroll;
	}

	public synchronized final int getPageNo()
	{
		return pageNo;
	}

	public synchronized final void setPageNo(int pageNo)
	{
		this.pageNo = pageNo;
	}

	
}
