package com.fg114.main.app.view;

import java.util.Observable;
import java.util.Observer;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.Gallery;

/**
 * 
 * @author zhangyifan
 * 
 */
public class MyGalleryView extends Gallery
{

	private GestureDetector gestureScanner;
	private MyScaleImageView imageView;
	private Context ctx;
	private float width, height; // 图片的实时宽，高
	private float left, right; // 图片实时的上下左右坐标

	private static final int NONE = 0;// 初始状态

	private static final int ZOOM = 1;// 缩放
	int mode = NONE;

	private boolean isOnScroll = false;
	private PointF prev = new PointF();
	private PointF mid = new PointF();
	private float dist = 1f;
	public MyGalleryView(Context context)
	{
		super(context);
		this.ctx = context;

	}

	public MyGalleryView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public MyGalleryView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.ctx = context;
		gestureScanner = new GestureDetector(new MySimpleGesture());
		this.setOnTouchListener(new OnTouchListener()
		{

			float baseValue;
			float originalScale;

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				View view = MyGalleryView.this.getSelectedView();
				if (view instanceof MyScaleImageView) {
					imageView = (MyScaleImageView) view;

					/*
					 * if(event.getActionMasked()==MotionEvent.ACTION_POINTER_2_DOWN
					 * ) { if(spacing(event)<baseValue) { return false; } } if
					 * (event.getAction() == MotionEvent.ACTION_DOWN) {
					 * baseValue = 0; originalScale = imageView.getScale();
					 * 
					 * } if (event.getAction() == MotionEvent.ACTION_MOVE) { if
					 * (event.getPointerCount() == 2) { float x = event.getX(0)
					 * - event.getX(1); float y = event.getY(0) - event.getY(1);
					 * float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
					 * Log.e("bug", value+"value????valuevaluevalue"); //
					 * System.out.println("value:" + value); if (baseValue == 0)
					 * { baseValue = value; } else { float scale = value /
					 * baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。 // scale the
					 * image imageView.zoomTo(originalScale * scale, x +
					 * event.getX(1), y + event.getY(1));
					 * 
					 * 
					 * } } }
					 */
					switch (event.getAction() & MotionEvent.ACTION_MASK) {
					// 主点按下
						case MotionEvent.ACTION_DOWN :

							prev.set(event.getX(), event.getY());
							isOnScroll = false;

							break;
						// 副点按下
						case MotionEvent.ACTION_POINTER_DOWN :
							dist = spacing(event);
							// 如果连续两点距离大于10，则判定为多点模式
							if (spacing(event) > 10f) {
								originalScale = imageView.getScale();
								midPoint(mid, event);
								mode = ZOOM;
								
							}
							break;
						
						case MotionEvent.ACTION_POINTER_UP :
                            
							mode = NONE;
							break;
						case MotionEvent.ACTION_MOVE :
							if (mode == ZOOM && !isOnScroll) {
								float newDist = spacing(event);
								if (newDist > 10f) {

									float tScale = newDist / dist;
									imageView.zoomTo(tScale * originalScale,
											mid.x, mid.y);
								}
							}
							break;
					}

				}
				return false;
			}

		});
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY)
	{

		View view = MyGalleryView.this.getSelectedView();
		if (view instanceof MyScaleImageView) {
			imageView = (MyScaleImageView) view;

			float v[] = new float[9];
			Matrix m = imageView.getImageMatrix();
			m.getValues(v);

			width = imageView.getScale() * imageView.getImageWidth();
			height = imageView.getScale() * imageView.getImageHeight();

			// 一下逻辑为移动图片和滑动gallery换屏的逻辑。如果没对整个框架了解的非常清晰，改动以下的代码前请三思！！！！！！
			if ((int) width <= Settings.DISPLAY.widthPixels
					&& (int) height <= Settings.DISPLAY.heightPixels)// 如果图片当前大小<屏幕大小，直接处理滑屏事件
			{

				
				
				//判断两次手指拖拽的距离大于100的时候 执行gallery的拖拽功能
				if(Math.abs(e2.getX()-e1.getX())>=100)
				{
					isOnScroll = true;
					super.onScroll(e1, e2, distanceX, distanceY);
				}

				
			} else {
				left = v[Matrix.MTRANS_X];
				right = left + width;
				Rect r = new Rect();
				imageView.getGlobalVisibleRect(r);

				isOnScroll = true;
				if (distanceX > 0)// 向左滑动
				{
					if (r.left > 0) {// 判断当前ImageView是否显示完全

						//判断两次手指拖拽的距离大于100的时候 执行gallery的拖拽功能
						if(Math.abs(e2.getX()-e1.getX())>=100)
						{
							isOnScroll = true;
							super.onScroll(e1, e2, distanceX, distanceY);
						}
					} else if (right < Settings.DISPLAY.widthPixels) {

						//判断两次手指拖拽的距离大于100的时候 执行gallery的拖拽功能
						if(Math.abs(e2.getX()-e1.getX())>=100)
						{
							isOnScroll = true;
							super.onScroll(e1, e2, distanceX, distanceY);
						}
					} else {
						isOnScroll = false;
						imageView.postTranslate(-distanceX, -distanceY);
					}
				} else if (distanceX < 0)// 向右滑动
				{
					if (r.right < Settings.DISPLAY.widthPixels) {

						if(Math.abs(e2.getX()-e1.getX())>=100)
						{
							isOnScroll = true;
							super.onScroll(e1, e2, distanceX, distanceY);
						}
					} else if (left > 0) {

						if(Math.abs(e2.getX()-e1.getX())>=100)
						{
							isOnScroll = true;
							super.onScroll(e1, e2, distanceX, distanceY);
						}
					} else {
						isOnScroll = false;
						imageView.postTranslate(-distanceX, -distanceY);
					}
				}

			}

		} else {

			super.onScroll(e1, e2, distanceX, distanceY);
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		gestureScanner.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP :
				// 判断上下边界是否越界
				View view = MyGalleryView.this.getSelectedView();
				if (view instanceof MyScaleImageView) {
					imageView = (MyScaleImageView) view;
					width = imageView.getScale() * imageView.getImageWidth();
					height = imageView.getScale() * imageView.getImageHeight();
					if ((int) height > Settings.DISPLAY.heightPixels)// 如果图片当前大小<屏幕大小，判断边界
					{
						float v[] = new float[9];
						Matrix m = imageView.getImageMatrix();
						m.getValues(v);
						float top = v[Matrix.MTRANS_Y];
						float bottom = top + height;
						//Log.i("manga", "top:" +"top+Main.screenHeight" +Settings.DISPLAY.heightPixels);
						if (top > 0) {
							imageView.postTranslateDur(-top, 200f);
						}
						//Log.i("manga", "bottom:" + bottom);
						if (bottom < Settings.DISPLAY.heightPixels) {
							imageView.postTranslateDur(
									Settings.DISPLAY.heightPixels - bottom,
									200f);
						}
					} else if ((int) width > Settings.DISPLAY.widthPixels) {
						float v[] = new float[9];
						Matrix m = imageView.getImageMatrix();
						m.getValues(v);
						float top = v[Matrix.MTRANS_Y];
						float bottom = top + height;
						//Log.i("manga", "top:" + top);
						if (top < 0) {
							imageView.postTranslateDur(-top, 200f);
						}
						//Log.i("manga", "bottom:" + bottom);
						if (bottom > Settings.DISPLAY.heightPixels) {
							imageView.postTranslateDur(
									Settings.DISPLAY.heightPixels - bottom,
									200f);
						}
					}

					/*
					 * if ((int) width <= Settings.DISPLAY.widthPixels && (int)
					 * height <= Settings.DISPLAY.heightPixels)//
					 * 如果图片当前大小<屏幕大小，判断边界 { break; } float v[] = new float[9];
					 * Matrix m = imageView.getImageMatrix(); m.getValues(v);
					 * float top = v[Matrix.MTRANS_Y]; float bottom = top +
					 * height; Log.i("manga", "top:" + top); if (top > 0) {
					 * imageView.postTranslateDur(-top, 200f); } Log.i("manga",
					 * "bottom:" + bottom); if (bottom <
					 * Settings.DISPLAY.heightPixels) {
					 * imageView.postTranslateDur(Settings.DISPLAY.heightPixels
					 * - bottom, 200f); }
					 */
				}
				break;
			case MotionEvent.ACTION_DOWN :
				isOnScroll = false;
				break;
		}
		return super.onTouchEvent(event);
	}

	private class MySimpleGesture extends SimpleOnGestureListener
	{
		// 按两下的第二下Touch down时触发

		public boolean onDoubleTap(MotionEvent e)
		{
			/*// 设置标题栏不可见
			RestaurantGalleryActivity main = (RestaurantGalleryActivity) ctx;
			main.getmMyTitleLayout().setVisibility(View.GONE);*/
			View view = MyGalleryView.this.getSelectedView();
			if (view instanceof MyScaleImageView) {
				imageView = (MyScaleImageView) view;

				if (imageView.getScale() > imageView.getScaleRate()) {
					imageView.zoomTo(imageView.getScaleRate(), e.getRawX(),
							e.getRawY(), 200f);

				} else {
					imageView.zoomTo(5.0f, e.getRawX(), e.getRawY(), 200f);

				}

			} else {

			}
			// return super.onDoubleTap(e);
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			// 单击一次显示标题栏
			RestaurantGalleryActivity main = (RestaurantGalleryActivity) ctx;
			if (main.getmMyTitleLayout().isShown()) {
				main.getmMyTitleLayout().setVisibility(View.GONE);
			} else {
				main.getmMyTitleLayout().setVisibility(View.VISIBLE);
			}

			main.getmMyTitleLayout().bringToFront();
			return super.onSingleTapConfirmed(e);
		}

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		isOnScroll = false;

		// 内存回收

		System.gc();
		// return false;

		int kEvent;
		if (isScrollingLeft(e1, e2)) {
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;
	}

	/**
	 * 计算两点间的距离
	 */
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(event.getPointerCount()-1);
		float y = event.getY(0) - event.getY(event.getPointerCount()-1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 两点的中点
	 */
	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) { 
        return e2.getX() > e1.getX(); 
    } 

	
}
