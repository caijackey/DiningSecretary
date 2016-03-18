package com.fg114.main.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class ScrollTopStopView extends LinearLayout{
	private Scroller scroller;  
    private Context mContext;  
      
    private View stayView;  
    private StayViewListener stayViewListener;  
    private ScrollView scrollView;  
      
    public void setStayView(View stayview,ScrollView scrollview,StayViewListener stayViewListener){  
        this.stayView = stayview;  
        this.scrollView = scrollview;  
        this.stayViewListener = stayViewListener;  
    }  
      
    public ScrollTopStopView(Context context) {  
        super(context);  
        mContext = context;  
          
    }  
    public ScrollTopStopView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        mContext = context;  
        init();  
          
    }  
    private void init() {  
        setOrientation(LinearLayout.VERTICAL);  
        scroller = new Scroller(mContext);  
    }  
  
      
    /** 
     *  
     */  
    boolean up = true;  
    @Override  
    public void computeScroll() {  
        if(stayView!=null&&scrollView!=null&&stayViewListener!=null){  
            int y = scrollView.getScrollY();  
            if(up){  
                int top = stayView.getTop();  
                if(y>=top){  
                    stayViewListener.onStayViewShow();  
                    up = false;  
                }  
            }  
            if(!up){  
                int bottom = stayView.getBottom();  
                if(y<=bottom-stayView.getHeight()){  
                    stayViewListener.onStayViewGone();  
                    up = true;  
                }  
            }  
        }  
    }  
      
      
    public interface StayViewListener{  
        public void onStayViewShow();  
        public void onStayViewGone();  
    }  
}
