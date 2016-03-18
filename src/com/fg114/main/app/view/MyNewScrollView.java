package com.fg114.main.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
/**  
 * 能够兼容ViewPager的ScrollView  
 * @Description: 解决了ViewPager在ScrollView中的滑动反弹问题  
 */  

public class MyNewScrollView extends ScrollView{
	// 滑动距离及坐标   
    private float xDistance, yDistance, xLast, yLast;   
  
    public MyNewScrollView(Context context, AttributeSet attrs) {   
        super(context, attrs);   
    }   
  
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {   
        switch (ev.getAction()) {   
            case MotionEvent.ACTION_DOWN:   
                xDistance = yDistance = 0f;   
                xLast = ev.getX();   
                yLast = ev.getY();   
                break;   
            case MotionEvent.ACTION_MOVE:   
                final float curX = ev.getX();   
                final float curY = ev.getY();   
                   
                xDistance += Math.abs(curX - xLast);   
                yDistance += Math.abs(curY - yLast);   
                xLast = curX;   
                yLast = curY;   
                   
                if(xDistance > yDistance){   
                    return false;   
                }     
        }   
  
        return super.onInterceptTouchEvent(ev);   
    }   
	
	
	
	
	
	
//	  private GestureDetector mGestureDetector;
//	    View.OnTouchListener mGestureListener;
//
//	    public MyNewScrollView(Context context, AttributeSet attrs) {
//	        super(context, attrs);
//	        mGestureDetector = new GestureDetector(new YScrollDetector());
//	        setFadingEdgeLength(0);
//	        
//	    }
//
//	    @Override
//	    public boolean onInterceptTouchEvent(MotionEvent ev) {
//	        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
//	    }
//	    
//	    class YScrollDetector extends SimpleOnGestureListener {
//	        @Override
//	        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//	                if(distanceY!=0&&distanceX!=0){
//	                        
//	                }
//	            if(Math.abs(distanceY) >= Math.abs(distanceX)) {
//	                return true;
//	            }
//	            return false;
//	        }
//	    }
	
	


}
