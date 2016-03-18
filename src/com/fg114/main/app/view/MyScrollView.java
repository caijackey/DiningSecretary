package com.fg114.main.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.*;

public class MyScrollView extends ScrollView {

	private boolean mScrollable = true; // ScrollView是否能处理滚动

	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setScrollingEnabled(boolean enabled) {
		mScrollable = enabled;
	}

	public boolean isScrollable() {
		return mScrollable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mScrollable) {
				return super.onTouchEvent(ev);
			}
			return mScrollable;
		default:
			return super.onTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!mScrollable) {
			return false;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}
}
