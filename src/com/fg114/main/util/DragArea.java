package com.fg114.main.util;

import android.content.Context;
import android.graphics.Rect;

public class DragArea 
{
	public static Rect mRect;
	public static Rect getDragArea(int left,int top,int right,int bottom)
	{
		if(mRect==null)
		{
			mRect=new Rect(left, top, right, bottom);
		}
		return mRect ;
		
	}
	 /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
	public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  


}
