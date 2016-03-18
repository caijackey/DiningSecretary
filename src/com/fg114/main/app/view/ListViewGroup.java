package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fg114.main.app.adapter.ListViewGroupAdapter;
import com.fg114.main.service.dto.ResPicData2;

import com.fg114.main.util.ContextUtil;


import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;


public class ListViewGroup extends LinearLayout
{
	
	@SuppressWarnings("hiding")
	public interface onItemSelectListener<ResPicData2>
	 {
		 void onItemSelect(ResPicData2 T);
	 }
	public interface onScrollListener
	{
		void onScroll();
	}
	private int mColumn=2; //总共有多少列listview
	private onItemSelectListener<ResPicData2> mOnIntemSelectListener;//列表项监听器
    private List<MyGroupListView> totalList=new ArrayList<MyGroupListView>();
	private List<ResPicData2> totalDataList=new ArrayList<ResPicData2>();
	private Context mContext;
	private onScrollListener mOnScrollListener;
	private boolean isLast;
	private int PageNo;//当前页面数
	public ListViewGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOrientation(LinearLayout.HORIZONTAL);
		this.mContext=context;
	}
	
	public ListViewGroup(Context context)
	{
		super(context);
		setOrientation(LinearLayout.HORIZONTAL);
		this.mContext=context;
		
	}
	
	public void initBaseView()
	{
		Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
		int item_width = display.getWidth() / mColumn;// 根据屏幕大小计算每列大小
		for( int i=0;i<mColumn;i++)
		{
		    final MyGroupListView eachList=new MyGroupListView(ContextUtil.getContext());
		    eachList.setVerticalScrollBarEnabled(false);
			ViewGroup.LayoutParams lay=new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,1.0f);
			eachList.setLayoutParams(lay);
			eachList.setId(i);
			totalList.add(eachList);
			
			eachList.setOnScrollListener(new OnScrollListener()
			{
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState)
				{
					
					if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) 
							&& eachList.isRefreshFoot()) {
					
						if(isLast == false){
							if(mOnScrollListener!=null)
							{
								mOnScrollListener.onScroll();
							}
							
						}
					}
					
					
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
				{
					
					if (eachList.isScroll()) {
						eachList.setScroll(false);
						return;
					}
				 
					int index = view.getFirstVisiblePosition();
					View v = view.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();
					for(int i=0;i<totalList.size();i++)
					{
						if(eachList.getId()!=totalList.get(i).getId())
						{
							totalList.get(i).setScroll(true);
							totalList.get(i).setSelectionFromTop(index, top);
						}
					}
					if(firstVisibleItem + visibleItemCount == totalItemCount) {
						//当到达列表尾部时
						eachList.setRefreshFoot(true);
						
						
					}else{
						eachList.setRefreshFoot(false);
						
						
					}
					
					Log.e("bug", "onScroll:Id:"+eachList.getId()+"ditance:"+top);
					
					
				}
			});
			eachList.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
				{
					if(mOnIntemSelectListener!=null)
					{
						mOnIntemSelectListener.onItemSelect(((ListViewGroupAdapter)eachList.getAdapter()).getDataList().get(position));
					}
					
				}
			});
			ListViewGroupAdapter mAdapter=new ListViewGroupAdapter(mContext,item_width);
			mAdapter.setDataList(null, false);
			eachList.setAdapter(mAdapter);
			addView(eachList);
		}
		
	}
	
	

	public synchronized final onScrollListener getmOnScrollListener()
	{
		return mOnScrollListener;
	}

	public synchronized final void setmOnScrollListener(onScrollListener mOnScrollListener)
	{
		this.mOnScrollListener = mOnScrollListener;
	}

	public synchronized final List<ResPicData2> getTotalDataList()
	{
		return totalDataList;
	}

	public synchronized final void setTotalDataList(List<ResPicData2> DataList,boolean isLast,int pageNo)
	{
		this.totalDataList.addAll(DataList);
		this.isLast=isLast;
		this.PageNo=pageNo;
		List<List<ResPicData2>> averageList=averageList(DataList, mColumn);
		
		for(int i=0;i<totalList.size();i++)
		{
			//平均分配数据
			((ListViewGroupAdapter)totalList.get(i).getAdapter()).addList(averageList.get(i), isLast);
		}
		
		
		
		
		
	}

	public synchronized final int getmColumn()
	{
		return mColumn;
	}

	public synchronized final void setmColumn(int mColumn)
	{
		this.mColumn = mColumn;
	}

	public synchronized final onItemSelectListener<ResPicData2> getmOnIntemSelectListener()
	{
		return mOnIntemSelectListener;
	}

	public synchronized final void setmOnIntemSelectListener(onItemSelectListener<ResPicData2> mOnIntemSelectListener)
	{
		this.mOnIntemSelectListener = mOnIntemSelectListener;
	}
	//数据平均依次分组
	public  List<List<ResPicData2>> averageList(List<ResPicData2> mlist ,int averageNum)
	{
	
		List<List<ResPicData2>> totalList=new ArrayList<List<ResPicData2>>();
		for(int i=0;i<averageNum;i++)
		{
			List<ResPicData2> tempList=new ArrayList<ResPicData2>();
			totalList.add(tempList);
		}
		for(int i=0;i<mlist.size();i++)
		{
			int m=i%averageNum;
			totalList.get(m).add(mlist.get(i));
		}
			
		return totalList;
		
	}

 
	
}
