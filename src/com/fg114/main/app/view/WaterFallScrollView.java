package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.WaterFallBaseView.onWaterFallClickListener;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.util.WaterFallUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ScrollView;

public class WaterFallScrollView extends ScrollView
{

	public static final String TAG = "LazyScrollView";

	// 图片显示相关的图片
	public int columnCount = 0;
	public int itemWidth;
	public int pageCount = 10;
	public int loadedCount = 0;
	public ArrayList<LinearLayout> waterfall_items;
	public Handler handler;
	public WaterFallUtils waterFallUtils;
	public View view;
	public ViewGroup waterfallContainer;

	public int current_page = 0;// 当前页数
	public int pictureTotalCount = 10000;//加载上限

	public int[] topIndex;
	public int[] bottomIndex;
	public int[] lineIndex;
	public int[] column_height;// 每列的高度

	public SparseArray<String> pins;
	public int loaded_count = 0;// 已加载数量
	public SparseIntArray[] pin_mark;
	private Context mCtx;
	private boolean isScroll = true;
    private List<WaterFallBaseView> parentView=new ArrayList<WaterFallBaseView>(); 
	private onWaterFallClickListener<RestPicData> onWaterFallClickListener;
	// 数据
	public SparseArray<WaterFallView> iviews;
	private WaterFallView.FlowViewHandler flowViewhandler;
	private List<RestPicData> totalDataList = new ArrayList<RestPicData>();
	public List<View> RecyleList = Collections.synchronizedList(new ArrayList<View>());

	public WaterFallScrollView(Context context)
	{
		super(context);
		this.mCtx = context;

	}

	public WaterFallScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.mCtx = context;

	}

	public WaterFallScrollView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.mCtx = context;

	}

	public void addList(List<RestPicData> list)
	{
		
		this.totalDataList.addAll(list);
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		// onScrollListener.onAutoScroll(l, t, oldl, oldt);
		if (isScroll) {
			// -----
			//OpenPageDataTracer.getInstance().addEvent("滚动");
			// -----
			waterFallUtils.autoReload(l, t, oldl, oldt);
			if (view != null && onScrollListener != null) {
				handler.sendMessageDelayed(handler.obtainMessage(1), 200);
			}

		}

	}

	
	// 根据标志位判断加载什么内容
	public void AddItemToContainer(int pageindex, int pagecount, int flag)
	{
		isScroll=true;
		int currentIndex = pageindex * pagecount;
		int imagecount = this.pictureTotalCount;// image_filenames.size();

		for (int i = currentIndex; i < pagecount * (pageindex + 1) && i < imagecount; i++) {
			RestPicData dto = totalDataList.get(this.loaded_count);
			if (flag == 0) {
				WaterFallBaseView view = new WaterFallView(mCtx);
				parentView.add(view);
				AddChild(view, dto, (int) Math.ceil(this.loaded_count / (double) Settings.COLUMN_COUNT), this.loaded_count);
				this.loaded_count++;
				
			}  else {
				WaterFallBaseView view = new WaterFallPicView(mCtx);
				parentView.add(view);
				AddChild(view, dto, (int) Math.ceil(this.loaded_count / (double) Settings.PIC_COLUMN_COUNT), this.loaded_count);
				this.loaded_count++;
			}

		}

	}

	private void AddChild(WaterFallBaseView view, RestPicData dto, int rowIndex, int id)
	{

		view.setWaterFall_Scroll(this);
		view.setId(id);
		view.setPadding(1, 1, 1, 1);
		if (!this.RecyleList.isEmpty()) {
			// Log.e("bug","RecyleList.get(0):"+this.RecyleList.get(0).getParent()
			// + ", " + this.RecyleList.get(0) +
			// ", RecyleList.size():"+this.RecyleList.size());
			view.setContentView(this.RecyleList.get(0));
			this.RecyleList.remove(0);
		}
		if (onWaterFallClickListener != null) {
			view.setTag(onWaterFallClickListener);
		}
		view.initBaseView(rowIndex, dto, itemWidth, flowViewhandler, true);

	}
	public void resetScrollData()
	{
		waterfallContainer.removeAllViews();
		loaded_count = 0;
		isScroll=false;
		totalDataList.clear();
		RecyleList.clear();
		current_page = 0;
		columnCount = 0;
		itemWidth = 0;
	    for(int i=0;i<parentView.size();i++)
	    {
	    	if(parentView.get(i).getContentView()!=null)
	    	{
	    		parentView.get(i).setContentView(null);
	    	}
	    }
	    parentView.clear();
		java.lang.System.gc();

	}

	public synchronized final onWaterFallClickListener<RestPicData> getOnWaterFallClickListener()
	{
		return onWaterFallClickListener;
	}

	public synchronized final void setOnWaterFallClickListener(onWaterFallClickListener<RestPicData> onWaterFallClickListener)
	{
		this.onWaterFallClickListener = onWaterFallClickListener;
	}

	private void init()
	{
		//处理每个child的handler
	    flowViewhandler = new WaterFallView.FlowViewHandler(this);
        this.setOnTouchListener(onTouchListener);
		column_height = new int[columnCount];
		iviews = new SparseArray<WaterFallView>();
		pins = new SparseArray<String>();
		pin_mark = new SparseIntArray[columnCount];
		this.lineIndex = new int[columnCount];
		this.bottomIndex = new int[columnCount];
		this.topIndex = new int[columnCount];
		for (int i = 0; i < columnCount; i++) {
			lineIndex[i] = -1;
			bottomIndex[i] = -1;
		}
		// 初始化话waterfall_items 用于加载图片
		waterfall_items = new ArrayList<LinearLayout>();
		for (int i = 0; i < columnCount; i++) {
			LinearLayout itemLayout = new LinearLayout(getContext());
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

			// 用于加载单列的显示
			/*
			 * LinearLayout.LayoutParams itemParam1 = new
			 * LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT,
			 * LayoutParams.WRAP_CONTENT);
			 */
			// itemParam.weight=1;
			itemParam.weight = 1;
			itemParam.setMargins(2, 2, 2, 2);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setLayoutParams(itemParam);
			waterfall_items.add(itemLayout);
			waterfallContainer.addView(itemLayout, itemParam);
			pin_mark[i] = new SparseIntArray();
		}
	}

	// 获得参考的View，主要是为了获得它的MeasuredHeight，然后和滚动条的ScrollY+getHeight作比较。

	public void commitWaterFall(WaterFallOption options, WaterFallScrollView currentFallView)
	{
		this.columnCount = options.column_count;
		this.itemWidth = options.itemWidth;
		this.waterfallContainer = options.waterFallContainer;
		this.pageCount = options.pageCount;
		// 一共加载的图片
		this.pictureTotalCount = options.pictureTotalCount;
		waterFallUtils = new WaterFallUtils(currentFallView);
		this.view = getChildAt(0);
		if (view != null) {
			handler = new WaterFallHandler(view, this);
			init();
		}

	}

	OnTouchListener onTouchListener = new OnTouchListener()
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					Log.d(TAG, "ACTION_DOWN" + "Y->" + event.getY() + "X->" + event.getX());
					break;
				case MotionEvent.ACTION_UP :
					if (view != null && onScrollListener != null) {
						handler.sendMessageDelayed(handler.obtainMessage(1), 200);
					}
					break;
				case MotionEvent.ACTION_MOVE :
					// Log.d(TAG,"ACTION_MOVE"+"Y->"+
					// event.getY()+"X->"+event.getX());
					break;
				default :
					break;
			}
			return false;
		}

	};

	public interface OnScrollListener
	{
		void onBottom();

		void onTop();

		void onScroll();

		void onAutoScroll(int l, int t, int oldl, int oldt);
	}

	protected OnScrollListener onScrollListener;

	public void setOnScrollListener(OnScrollListener onScrollListener)
	{
		this.onScrollListener = onScrollListener;
	}

	public static class WaterFallOption
	{
		// 显示列数
		public int column_count = 3;
		// 每次加载的多少张图片
		public int pageCount = 30;
		// 允许加载的最多图片数
		public int pictureTotalCount = 1000;
		// 用于handle 通讯的常量
		// 消息发送的延迟时间
		public int message_delay = 200;
		// 每列的宽度
		public int itemWidth;
		public int ScreenWidth;
		private static int DEX = 21; // 瀑布流边框间距
		public ViewGroup waterFallContainer;

		public WaterFallOption(ViewGroup container, int screenWidth, int columnCount)
		{
			this.waterFallContainer = container;
			this.ScreenWidth = screenWidth;
			this.column_count = columnCount;
			this.itemWidth = ScreenWidth / columnCount - DEX;
		}

	}

	public synchronized final List<RestPicData> getTotalDataList()
	{
		return totalDataList;
	}

	public synchronized final void setTotalDataList(List<RestPicData> totalDataList)
	{
		this.totalDataList = totalDataList;
	}

	public class WaterFallHandler extends Handler
	{
		private View childView;
		private WaterFallScrollView waterFallView;

		public WaterFallHandler(View ls, WaterFallScrollView waterFallView)
		{
			this.childView = ls;
			this.waterFallView = waterFallView;
		}

		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what) {
				case 1 :
					if (childView.getMeasuredHeight() - 20 <= waterFallView.getScrollY() + waterFallView.getHeight()) {

						if (waterFallView.onScrollListener != null) {
							waterFallView.onScrollListener.onBottom();

						}

					} else if (waterFallView.getScrollY() <= 0) {
						if (waterFallView.onScrollListener != null) {

							waterFallView.onScrollListener.onTop();
						}
					} else {
						if (waterFallView.onScrollListener != null) {

							waterFallView.onScrollListener.onScroll();
						}
					}
					break;

				default :
					break;
			}
		}

	}
}
