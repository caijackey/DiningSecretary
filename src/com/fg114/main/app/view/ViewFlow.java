package com.fg114.main.app.view;

import java.util.*;

import com.fg114.main.R;


import android.content.*;
import android.content.res.*;
import android.database.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * A horizontally scrollable {@link ViewGroup} with items populated from an
 * {@link Adapter}. The ViewFlow uses a buffer to store loaded {@link View}s in.
 * The default size of the buffer is 3 elements on both sides of the currently
 * visible {@link View}, making up a total buffer size of 3 * 2 + 1 = 7. The
 * buffer size can be changed using the {@code sidebuffer} xml attribute.
 * 
 */
public class ViewFlow extends AdapterView<Adapter> {

	private static final int SNAP_VELOCITY = 1000;
	private static final int INVALID_SCREEN = -1;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	private LinkedList<View> mLoadedViews;
	private int mCurrentBufferIndex;
	private int mCurrentAdapterIndex;
	private int mSideBuffer = 2;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionX;
	private int mTouchSlop;
	private int mMaximumVelocity;
	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	private boolean mFirstLayout = true;
	private ViewSwitchListener mViewSwitchListener;
	private Adapter mAdapter;
	private int mLastScrollDirection;
	private AdapterDataSetObserver mDataSetObserver;
	private FlowIndicator mIndicator;
	private int mLastOrientation = -1;

	private ViewTreeObserver.OnGlobalLayoutListener orientationChangeListener = new ViewTreeObserver.OnGlobalLayoutListener() {

		@Override
		public void onGlobalLayout() {
			getViewTreeObserver().removeGlobalOnLayoutListener(orientationChangeListener);
			Log.e("mCurrentAdapterIndex", "mCurrentAdapterIndex=" + mCurrentAdapterIndex);
			setSelection(mCurrentAdapterIndex);
		}
	};

	/**
	 * Receives call backs when a new {@link View} has been scrolled to.
	 */
	public static interface ViewSwitchListener {

		/**
		 * This method is called when a new View has been scrolled to.
		 * 
		 * @param view
		 *            the {@link View} currently in focus.
		 * @param position
		 *            The position in the adapter of the {@link View} currently
		 *            in focus.
		 */
		void onSwitched(View view, int position);

	}

	public ViewFlow(Context context) {
		super(context);
		mSideBuffer = 3;
		init();
	}

	public ViewFlow(Context context, int sideBuffer) {
		super(context);
		mSideBuffer = sideBuffer;
		init();
	}

	public ViewFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ViewFlow);
		mSideBuffer = styledAttrs.getInt(R.styleable.ViewFlow_sidebuffer, 3);
		styledAttrs.recycle();
		init();
	}

	private void init() {
		mLoadedViews = new LinkedList<View>();
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != mLastOrientation) {
			mLastOrientation = newConfig.orientation;
			getViewTreeObserver().addOnGlobalLayoutListener(orientationChangeListener);
		}
	}

	public int getViewsCount() {
		return mAdapter == null ? 0 : mAdapter.getCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			// throw new IllegalStateException(
			// "ViewFlow can only be used in EXACTLY mode.");
			return;
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			// throw new IllegalStateException(
			// "ViewFlow can only be used in EXACTLY mode.");
			return;
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			mScroller.startScroll(0, 0, mCurrentScreen * width, 0, 0);
			mFirstLayout = false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
//					Log.d("onInterceptTouchEvent[left] mCurrentScreen="+mCurrentScreen,"mCurrentScreen-1="+(mCurrentScreen - 1));
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
//					Log.d("onInterceptTouchEvent[right] mCurrentScreen="+mCurrentScreen,"mCurrentScreen+1="+(mCurrentScreen + 1));
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					//Log.d("onInterceptTouchEvent[] mCurrentScreen="+mCurrentScreen,"");
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:	
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();
				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
//					Log.d("onTouchEvent[left] mCurrentScreen="+mCurrentScreen,"mCurrentScreen-1="+(mCurrentScreen - 1));
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
//					Log.d("onTouchEvent[right] mCurrentScreen="+mCurrentScreen,"mCurrentScreen+1="+(mCurrentScreen + 1));
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
//					Log.d("onTouchEvent[] mCurrentScreen="+mCurrentScreen,"");
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
//		case MotionEvent.ACTION_CANCEL:
////			Log.d("onTouchEvent[MotionEvent.ACTION_CANCEL] mCurrentScreen="+mCurrentScreen,"");
//			snapToDestination();
//			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}

	@Override
	protected void onScrollChanged(int h, int v, int oldh, int oldv) {
		super.onScrollChanged(h, v, oldh, oldv);
//		if (h==oldh||mCurrentAdapterIndex>4) {
//			Log.e("onScrollChanged mCurrentAdapterIndex="+mCurrentAdapterIndex+",mCurrentBufferIndex="+mCurrentBufferIndex, "h=" + h + ",v=" + v + ",oldh=" + oldh + ",oldv=" + oldv );
//		}
		if (mIndicator != null&&h!=oldh) {
			/*
			 * The actual horizontal scroll origin does typically not match the
			 * perceived one. Therefore, we need to calculate the perceived
			 * horizontal scroll origin here, since we use a view buffer.
			 */
			int hPerceived = h + (mCurrentAdapterIndex - mCurrentBufferIndex) * getWidth();
//			if (mCurrentAdapterIndex >= 4) {
//				Log.e("onScrollChanged mCurrentAdapterIndex="+mCurrentAdapterIndex+",mCurrentBufferIndex="+mCurrentBufferIndex, "h=" + h + ",v=" + v + ",oldh=" + oldh + ",oldv=" + oldv + ",hPerceived=" + hPerceived);
//			}
			mIndicator.onScrolled(hPerceived, v, oldh, oldv);
		}
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
		//Log.d("snapToDestination[] mCurrentScreen="+mCurrentScreen,"whichScreen="+whichScreen);
		snapToScreen(whichScreen);
	}
	
	private void snapToScreen(int whichScreen) {
		if (!mScroller.isFinished())
			return;
		//to real position
		//Log.e("snapToScreen "+whichScreen,"mCurrentAdapterIndex="+mCurrentAdapterIndex+",mCurrentBufferIndex="+mCurrentBufferIndex);
		setSelection(mCurrentAdapterIndex+(whichScreen-mCurrentBufferIndex));
	}
//	private void snapToScreen(int whichScreen) {
//		if (!mScroller.isFinished())
//			return;
//		mLastScrollDirection = whichScreen - mCurrentScreen;
//
//		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
//
//		mNextScreen = whichScreen;
//
//		final int newX = whichScreen * getWidth();
//		final int delta = newX - getScrollX();
//		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
//		//fixed below
////		mScroller.startScroll(mScroller.getCurrX(), 0, delta, 0, Math.abs(delta) * 2);
//		invalidate();
//	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			//fixed below bug
			scrollTo(mScroller.getCurrX()-(mCurrentAdapterIndex - mCurrentBufferIndex) * getWidth(), mScroller.getCurrY());
			postInvalidate();
		}
//		else if (mNextScreen != INVALID_SCREEN) {
//			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
//			mNextScreen = INVALID_SCREEN;
//			//postViewSwitched(mLastScrollDirection);
//			Log.i("computeScroll","mLastScrollDirection="+mLastScrollDirection+",x="+mScroller.getCurrX()+",y="+mScroller.getCurrY());
//		}
	}

	/**
	 * Scroll to the {@link View} in the view buffer specified by the index.
	 * 
	 * @param indexInBuffer
	 *            Index of the view in the view buffer.
	 */
	private void setVisibleView(int indexInBuffer, boolean uiThread,int startX) {
		mCurrentScreen = Math.max(0, Math.min(indexInBuffer, getChildCount() - 1));
		
		//int dx = (mCurrentScreen * getWidth()) - mScroller.getCurrX(); //bug fixed on below
//		Log.d("", "getChildCount="+getChildCount());
//		Log.d("", "before getStartX="+mScroller.getStartX());
		int dx = (mCurrentAdapterIndex * getWidth()) - startX; 
		mScroller.startScroll(startX, mScroller.getCurrY(), dx, 0, 1000);
//		Log.d("", "after getStartX="+mScroller.getStartX());
//		if (dx == 0)
//			onScrollChanged(mScroller.getCurrX() + dx, mScroller.getCurrY(), mScroller.getCurrX() + dx, mScroller.getCurrY());
		if (uiThread)
			invalidate();
		else
			postInvalidate();
	}

	/**
	 * Set the listener that will receive notifications every time the {code
	 * ViewFlow} scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnViewSwitchListener(ViewSwitchListener l) {
		mViewSwitchListener = l;
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		setAdapter(adapter, 0);
	}

	public void setAdapter(Adapter adapter, int initialPosition) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;

		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

		}
		if (mAdapter == null || mAdapter.getCount() == 0)
			return;

		setSelection(initialPosition);
	}

	@Override
	public View getSelectedView() {
		return (mCurrentBufferIndex < mLoadedViews.size() ? mLoadedViews.get(mCurrentBufferIndex) : null);
	}

	@Override
	public int getSelectedItemPosition() {
		return mCurrentAdapterIndex;
	}

	/**
	 * Set the FlowIndicator
	 * 
	 * @param flowIndicator
	 */
	public void setFlowIndicator(FlowIndicator flowIndicator) {
		mIndicator = flowIndicator;
		mIndicator.setViewFlow(this);
	}

	@Override
	public void setSelection(int position) {
		mNextScreen = INVALID_SCREEN;
		mScroller.forceFinished(true);
		if (mAdapter == null)
			return;
		//Log.e("setSelection "+position,"mCurrentBufferIndex="+mCurrentBufferIndex+",mCurrentAdapterIndex="+mCurrentAdapterIndex+",mLoadedViews="+mLoadedViews.size());
		if (position < 0 || position > mAdapter.getCount()-1) {
			//Log.e("setSelection aborted","什么都不干");
			return; //如果越界，什么都不干
			
		}
		position = Math.max(position, 0);
		position = Math.min(position, mAdapter.getCount() - 1);
		//------
		int startX=(mCurrentAdapterIndex-mCurrentBufferIndex)*getWidth()+getScrollX();
		//------
		ArrayList<View> recycleViews = new ArrayList<View>();
		View recycleView;
		while (!mLoadedViews.isEmpty()) {
			recycleViews.add(recycleView = mLoadedViews.remove());
			detachViewFromParent(recycleView);
		}

		View currentView = makeAndAddView(position, true, (recycleViews.isEmpty() ? null : recycleViews.remove(0)));
		mLoadedViews.addLast(currentView);

		for (int offset = 1; mSideBuffer - offset >= 0; offset++) {
			int leftIndex = position - offset;
			int rightIndex = position + offset;
			if (leftIndex >= 0)
				mLoadedViews.addFirst(makeAndAddView(leftIndex, false, (recycleViews.isEmpty() ? null : recycleViews.remove(0))));
			if (rightIndex < mAdapter.getCount())
				mLoadedViews.addLast(makeAndAddView(rightIndex, true, (recycleViews.isEmpty() ? null : recycleViews.remove(0))));
		}
		mCurrentBufferIndex = mLoadedViews.indexOf(currentView);
		mCurrentAdapterIndex = position;

		for (View view : recycleViews) {
			removeDetachedView(view, false);
		}
		requestLayout();
		setVisibleView(mCurrentBufferIndex, false,startX);
		if (mIndicator != null) {
			mIndicator.onSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
		}
		if (mViewSwitchListener != null) {
			mViewSwitchListener.onSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
		}
		//---
		if(getOnItemSelectedListener()!=null){
			getOnItemSelectedListener().onItemSelected(this, currentView, position, 0);
		}
	}

	private void resetFocus() {
		logBuffer();
		mLoadedViews.clear();
		removeAllViewsInLayout();

		for (int i = Math.max(0, mCurrentAdapterIndex - mSideBuffer); i < Math.min(mAdapter.getCount(), mCurrentAdapterIndex + mSideBuffer + 1); i++) {
			mLoadedViews.addLast(makeAndAddView(i, true, null));
			if (i == mCurrentAdapterIndex)
				mCurrentBufferIndex = mLoadedViews.size() - 1;
		}
		logBuffer();
		requestLayout();
	}

//	private void postViewSwitched(int direction) {
//		if (direction == 0)
//			return;
//
//		if (direction > 0) { // to the right
//			mCurrentAdapterIndex++;
//			mCurrentBufferIndex++;
//
//			View recycleView = null;
//
//			// Remove view outside buffer range
//			if (mCurrentAdapterIndex > mSideBuffer) {
//				recycleView = mLoadedViews.removeFirst();
//				detachViewFromParent(recycleView);
//				// removeView(recycleView);
//				mCurrentBufferIndex--;
//			}
//
//			// Add new view to buffer
//			int newBufferIndex = mCurrentAdapterIndex + mSideBuffer;
//			if (newBufferIndex < mAdapter.getCount())
//				mLoadedViews.addLast(makeAndAddView(newBufferIndex, true, recycleView));
//
//		} else { // to the left
//			mCurrentAdapterIndex--;
//			mCurrentBufferIndex--;
//			View recycleView = null;
//
//			// Remove view outside buffer range
//			if (mAdapter.getCount() - 1 - mCurrentAdapterIndex > mSideBuffer) {
//				recycleView = mLoadedViews.removeLast();
//				detachViewFromParent(recycleView);
//			}
//
//			// Add new view to buffer
//			int newBufferIndex = mCurrentAdapterIndex - mSideBuffer;
//			if (newBufferIndex > -1) {
//				mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false, recycleView));
//				mCurrentBufferIndex++;
//			}
//
//		}
//
//		requestLayout();
//		setVisibleView(mCurrentBufferIndex, true);
//		if (mIndicator != null) {
//			mIndicator.onSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
//		}
//		if (mViewSwitchListener != null) {
//			mViewSwitchListener.onSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
//		}
//		logBuffer();
//	}

	private View setupChild(View child, boolean addToEnd, boolean recycle) {
		ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		}
		if (recycle)
			attachViewToParent(child, (addToEnd ? -1 : 0), p);
		else
			addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
		return child;
	}

	private View makeAndAddView(int position, boolean addToEnd, View convertView) {
		View view = mAdapter.getView(position, convertView, this);
		return setupChild(view, addToEnd, convertView != null);
	}

	class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			View v = getChildAt(mCurrentBufferIndex);
			if (v != null) {
				for (int index = 0; index < mAdapter.getCount(); index++) {
					if (v.equals(mAdapter.getItem(index))) {
						mCurrentAdapterIndex = index;
						break;
					}
				}
			}
			resetFocus();
		}

		@Override
		public void onInvalidated() {
			// Not yet implemented!
		}

	}

	private void logBuffer() {
//		LogUtils.logD("viewflow", "Size of mLoadedViews: " + mLoadedViews.size() + "X: " + mScroller.getCurrX() + ", Y: " + mScroller.getCurrY());
//		LogUtils.logD("viewflow", "IndexInAdapter: " + mCurrentAdapterIndex + ", IndexInBuffer: " + mCurrentBufferIndex);
	}
}
