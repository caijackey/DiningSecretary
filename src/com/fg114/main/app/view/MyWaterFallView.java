package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.WaterFallBaseView.onWaterFallClickListener;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.WaterFallUtils;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * 瀑布流控件
 * 
 * @author xujianjun, 2013-10-15
 */
public class MyWaterFallView extends ScrollView {
	private static final int MAGIC_KEY = R.id.tag_waterfall_view;
	private static final int MAGIC_KEY_TOP = R.id.tag_waterfall_view_top;
	//
	public interface OnScrollListener {
		void onScroll();
		void onBottom();
	}
	// --------
	private Context context;
	private BaseAdapter adapter;

	// 显示列数
	private int columnCount = 2;
	private ColumnManager cm;
	public ViewGroup container; // 根容器

	private OnScrollListener onScrollListener;
	// -----
	DataSetObserver dataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			super.onChanged();
			dataChanged();
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
		}

	};

	public MyWaterFallView(Context context) {
		this(context, null);
	}

	public MyWaterFallView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyWaterFallView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
		initView();
		adapter.registerDataSetObserver(dataObserver);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (oldt <= t) {
			cm.fillTail();
		} else {
			cm.fillHead();
		}
		//Log.e("&&&&&&",((ViewGroup)container.getChildAt(0)).getChildCount()+" = "+((ViewGroup)container.getChildAt(1)).getChildCount());
		if(onScrollListener!=null){
			onScrollListener.onScroll();
			//Log.e(t+"++++"+getHeight(),(t+getHeight())+" > container.getMeasuredHeight()="+container.getMeasuredHeight());
			if(t+getHeight()>=container.getMeasuredHeight()){
				onScrollListener.onBottom();
			}
		}
	}

	private void dataChanged() {
		cm.fillTail();
	}

	private void initView() {
		container = (ViewGroup) getChildAt(0);
		container.removeAllViews();
		cm = new ColumnManager(columnCount);
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if(visibility==View.VISIBLE){
			this.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(cm!=null){
						cm.fillTail();
					}					
				}
			}, 300);
		}
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	// 列管理器
	private class ColumnManager {

		final int GAP=UnitUtil.dip2px(5);
		
		// 初始化列
		ColumnManager(int columnCount) {
			for (int i = 0; i < columnCount; i++) {
				LinearLayout colContainer = new LinearLayout(context);
				LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				itemParam.weight = 1;
				itemParam.setMargins(i==0?GAP:0, 0, GAP, 0);
				itemParam.gravity=Gravity.TOP|Gravity.RIGHT;
				colContainer.setOrientation(LinearLayout.VERTICAL);
				colContainer.setLayoutParams(itemParam);
				colContainer.setBackgroundColor(0x00FF0000); 
				colContainer.setPadding(0, 0, 0, 0);
				container.addView(colContainer, itemParam);
			}
			container.measure(MeasureSpec.makeMeasureSpec(UnitUtil.getScreenWidthPixels(), MeasureSpec.EXACTLY), 0); 
		}

		// 返回View与父控件之间top值
		int getViewTop(View v) {
			if (v.getTag(MAGIC_KEY_TOP) != null) {
				return Integer.parseInt(v.getTag(MAGIC_KEY_TOP).toString());
			}
			return 0;
		}

		void setViewTop(View v, int top) {
			v.setTag(MAGIC_KEY_TOP, top < 0 ? 0 : top);
		}

		// 添加到某最短尾的列的后面
		void addToTail(View v) {
			int col = 0;
			int colLength = Integer.MAX_VALUE;
			
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					col = i;
					break;
				} else {
					View tailView = colView.getChildAt(colView.getChildCount() - 1);
					// Log.d("c.getTop() "+c.getTop(),c.getMeasuredWidth()+"+"+c.getMeasuredHeight()+"<"+colLength);
					if (getViewTop(tailView) + tailView.getMeasuredHeight() < colLength) {
						colLength = getViewTop(tailView) + tailView.getMeasuredHeight();
						col = i;
					}
				}
			}
			ViewGroup colView = (ViewGroup) container.getChildAt(col);
			LinearLayout.LayoutParams vp=(android.widget.LinearLayout.LayoutParams) v.getLayoutParams();
			//如果未指定大小
			if(vp==null){
				v.measure(0,0);
				vp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,v.getMeasuredHeight());
				
			}else{
				float scale=colView.getMeasuredWidth()*1.0f/vp.width;
				vp.width=(int) (scale*vp.width);		
				vp.height=(int) (scale*vp.height);		
				v.measure(MeasureSpec.makeMeasureSpec(vp.width, MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(vp.height, MeasureSpec.EXACTLY));
			}
			
			//Log.e(((ViewGroup)getParent()).getWidth()+","+getWidth()+","+getMeasuredWidth()+"colView----"+colView.getWidth(),""+colView.getMeasuredWidth());
			vp.setMargins(0, GAP, 0, 0);
			v.setLayoutParams(vp);
			//---
			if (colView.getChildCount() > 0) {
				View lastView = colView.getChildAt(colView.getChildCount() - 1);
				setViewTop(v, lastView.getMeasuredHeight() + getViewTop(lastView)+GAP);
			}
			LinearLayout.LayoutParams colParam = (LinearLayout.LayoutParams) colView.getLayoutParams();
			if (colView.getHeight() < getViewTop(v) + v.getMeasuredHeight()+GAP) {
				colParam.height = getViewTop(v) + v.getMeasuredHeight()+GAP;
				colView.setLayoutParams(colParam);
			}
			//Log.d(v.getHeight()+"colView.getWidth()"+v.getLayoutParams().height,+v.getMeasuredHeight()+"v.getMeasuredWidth()"+v.getMeasuredWidth());
			colView.addView(v);

		}

		// 添加到某最短头的列的前面---------------------------------------------------
		void addToHead(View v) {
			int col = 0;
			int colTop = Integer.MIN_VALUE;
			v.measure(0, 0);
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					col = i;
					break;
				} else {
					View c = colView.getChildAt(0);
					if (getViewTop(c) > colTop) {
						colTop = getViewTop(c);
						col = i;
					}
				}
			}
			ViewGroup colView = (ViewGroup) container.getChildAt(col);
			if (colView.getChildCount() > 0) {
				int top = getViewTop(colView.getChildAt(0));
				setViewTop(v, top - v.getMeasuredHeight());
			}
			colView.setPadding(colView.getPaddingLeft(), colView.getPaddingTop() - v.getMeasuredHeight() <= 0 ? 0 : colView.getPaddingTop() - v.getMeasuredHeight(), colView.getPaddingRight(),
					colView.getPaddingBottom());
			colView.addView(v, 0);
		}

		// 判断尾部是否有空隙
		boolean needFillTail() {
			boolean isNeeded = false;
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					isNeeded = true;// 空列
					break;
				} else {
					View v = colView.getChildAt(colView.getChildCount() - 1);
					//Log.e(colView.getChildCount()+" needFillTail v.getTop()"+getViewTop(v),v.getMeasuredHeight()+",getScrollY:"+getScrollY()+",getHeight()"+getHeight());
					if (getViewTop(v) + v.getMeasuredHeight() <= getScrollY() + getHeight()) {
						isNeeded = true;
						break;
					}
				}
			}
			//Log.d("needFillTail",isNeeded+"------------------------------------------");
			return isNeeded;
		}

		// 判断头部是否有空隙
		boolean needFillHead() {
			boolean isNeeded = false;

			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					isNeeded = true;
					break;
				} else {
					View v = colView.getChildAt(0);
					// Log.e("needFill Head ("+i+")"+v.getTag(MAGIC_KEY),getViewTop(v)+">="+getScrollY()+"------------------------------------------");
					if (getViewTop(v) >= getScrollY()) {
						isNeeded = true;
						break;
					}
				}
			}
			// Log.d("needFill Head",isNeeded+"------------------------------------------");
			return isNeeded;

		}

		// 如果尾部有空隙，则填充
		void fillTail() {
			int i;
			boolean added=false;
			for (i = nextDataIndex(); needFillTail() && i < adapter.getCount(); i++) {
				View v = adapter.getView(i, pickOffHeadForReuse(), null);
				v.setTag(MAGIC_KEY, i);
				addToTail(v);
				added=true;
			}
			//调整容器高度
			if(added){
				adjustHeight(i);
			}
		}
		void adjustHeight(int pos){
			int count=adapter.getCount();
			int maxHeight=Integer.MIN_VALUE;
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					continue;
				} else {
					View tailView = colView.getChildAt(colView.getChildCount() - 1);
					int height = getViewTop(tailView)+tailView.getMeasuredHeight();
					//Log.e(count+"---adjustHeight"+pos+","+i,maxHeight+",tailView:"+height);
					if (height > maxHeight) {
						maxHeight = height;
					}
				}
			}
			double avgHeight=(1.0*maxHeight*columnCount)/count;
			double deltaHeight=avgHeight*(count-pos)/columnCount;
			//Log.e(count+"---adjustHeight"+pos,maxHeight+",avgHeight:"+avgHeight+",deltaHeight:"+deltaHeight);
			
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					continue;
				} else {
					View tailView = colView.getChildAt(colView.getChildCount() - 1);
					int height = getViewTop(tailView)+tailView.getMeasuredHeight();
					LinearLayout.LayoutParams colParam = (LinearLayout.LayoutParams) colView.getLayoutParams();
					//Log.e(count+"---adjustHeight"+pos,height+"<,maxHeight+deltaHeight:"+(maxHeight+deltaHeight));
					if(pos!=count && height < maxHeight+deltaHeight) {
						colParam.height = (int)(maxHeight+deltaHeight);
						colView.setLayoutParams(colParam);
					}
				}
			}
		}

		// 如果头部有空隙，则填充
		void fillHead() {
			int dataIndex = -1;
			while (needFillHead() && (dataIndex = previousDataIndex()) > -1) {
				View convertView=pickOffTailForReuse();
				if(convertView!=null){
					//Log.d("复用了",""+convertView.getTag(MAGIC_KEY));
				}
				View v = adapter.getView(dataIndex, convertView, null);
				v.setTag(MAGIC_KEY, dataIndex);
				addToHead(v);
			}
		}

		// 找出屏幕下方将要添加的数据view的序号
		int nextDataIndex() {
			int dataIndex = -1;
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					continue;
				} else {
					View tailView = colView.getChildAt(colView.getChildCount() - 1);
					int idx = Integer.parseInt(tailView.getTag(MAGIC_KEY).toString());
					if (idx > dataIndex) {
						dataIndex = idx;
					}
				}
			}
			return dataIndex + 1;
		}

		Stack<Integer> headSeq = new Stack<Integer>();

		// 找出屏幕上方将要添加的数据view的序号
		int previousDataIndex() {
			if (headSeq.empty()) {
				return -1;
			} else {
				return headSeq.pop();
			}
		}

		// 摘取尾部可被复用的view
		View pickOffTailForReuse() {
			View v = null;
			int maxDataIndex = -1;
			int col = -1;
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					return null;
				}
				View tailView = colView.getChildAt(colView.getChildCount() - 1);
				// 是否可见
				boolean inSight = inSight(tailView);
				if (inSight) {
					continue;
				} else {
					int dataIndex = Integer.parseInt(tailView.getTag(MAGIC_KEY).toString());
					if (dataIndex > maxDataIndex) {
						maxDataIndex = dataIndex;
						v = tailView;
						col = i;
					}
				}
			}
			if (v != null) {
				((ViewGroup) container.getChildAt(col)).removeView(v);
				//Log.e("摘取尾---",""+v.getTag(MAGIC_KEY));
			}
			return v;
		}

		// 摘取头部可被复用的view
		View pickOffHeadForReuse() {

			View v = null;
			int minViewBottom = Integer.MAX_VALUE;
			int col = -1;
			for (int i = 0; i < columnCount; i++) {
				ViewGroup colView = (ViewGroup) container.getChildAt(i);
				if (colView.getChildCount() == 0) {
					return null;
				}
				View headView = colView.getChildAt(0);
				// 是否可见
				boolean inSight = inSight(headView);
				if (inSight) {
					continue;
				} else {
					int bottom = getViewTop(headView) + headView.getMeasuredHeight();
					if (bottom <= minViewBottom) {
						minViewBottom = bottom;
						v = headView;
						col = i;
					}
				}
			}
			if (v != null) {
				ViewGroup colView = ((ViewGroup) container.getChildAt(col));
				colView.removeView(v);
				colView.setPadding(colView.getPaddingLeft(), colView.getPaddingTop() + v.getMeasuredHeight(), colView.getPaddingRight(), colView.getPaddingBottom());
				headSeq.push(Integer.parseInt(v.getTag(MAGIC_KEY).toString()));
			}
			return v;
		}

		//
		boolean inSight(View v) {
			boolean rtn = true;
			int valve=100000;
			float s1 = getScrollY() + getHeight() / 2.0f;
			float s2 = getViewTop(v) + v.getMeasuredHeight() / 2.0f;
			rtn = Math.abs(s1 - s2) < getHeight() / 2.0f + v.getMeasuredHeight() / 2.0f + valve;
			return rtn;
		}
	}

}