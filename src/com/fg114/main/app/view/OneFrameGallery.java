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
 * @author xujianjun
 * 
 */
public class OneFrameGallery extends Gallery
{
	private Context ctx;
	public OneFrameGallery(Context context)
	{
		super(context);
		this.ctx = context;

	}

	public OneFrameGallery(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public OneFrameGallery(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.ctx = context;

	}
	/** 
     * 一次滑动只滚动一张图片 
     */  
    @Override  
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
            float velocityY) {  
        if (velocityX > 0) {  
            // 往左边滑动  
            super.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);  
        } else {  
            // 往右边滑动  
            super.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);  
        }  
        return false;  
    }  
}
