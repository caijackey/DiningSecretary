package com.fg114.main.app.view;

import com.fg114.main.R;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;

/**
 * 可自定义布局的Dialog
 * @author wfc
 *
 */
public class CustomDialog extends Dialog {
	
	public CustomDialog(Context context) {
		super(context, R.style.Custom_Dialog);
	}
	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    /**
	 * 获得Dialog的LayoutParams
	 * @return
	 */
	public WindowManager.LayoutParams getLayoutParams() {
		return getWindow().getAttributes();
	}
	
	/**
	 * 获得Dialog宽度
	 * @return
	 */
	public int getWidth() {
		Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	return layoutParams.width;
	}
	
	/**
	 * 获得Dialog高度
	 * @return
	 */
	public int getHeight() {
		Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	return layoutParams.height;
	}
	
	/**
	 * 获得Dialog的X
	 * @return
	 */
	public int getX() {
		Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	return layoutParams.x;
	}
	
	/**
	 * 获得Dialog的Y
	 * @return
	 */
	public int getY() {
		Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	return layoutParams.y;
	}
	
	/**
	 * 获得Dialog的对齐方式
	 * @return
	 */
	public int getGravity() {
		Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	return layoutParams.gravity;
	}
	
    /**
	 * 设置Dialog的LayoutParams
	 * @return
	 */
	public void setLayoutParams(WindowManager.LayoutParams layoutParams) {
		getWindow().setAttributes(layoutParams);
	}
	
	/**
	 * 设置Dialog的宽度
	 * @return
	 */
    public void setWidth(int width) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.width = width;
    	window.setAttributes(layoutParams);
    }
    
    /**
	 * 设置Dialog的高度
	 * @return
	 */
    public void setHeight(int height) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.height = height;
    	window.setAttributes(layoutParams);
    }
    
    /**
	 * 设置Dialog的位置
	 * @return
	 */
    public void setLocation(int x, int y) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.x = x;
    	layoutParams.y = y;
    	window.setAttributes(layoutParams);
    }
    
    /**
	 * 设置Dialog的对齐方式
	 * @return
	 */
    public void setGravity(int gravity) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.gravity = gravity;
    	window.setAttributes(layoutParams);
    }
    
    /**
     * 设置Dialog的动画
     * @param resId
     */
    public void setAnimation(int resId) {
    	Window window = getWindow();
    	window.setWindowAnimations(resId);
    }
    
    /**
     * 设置Dialog的背景
     * @param animId
     */
    public void setBackground(int resid) {
    	Window window = getWindow();
    	window.setBackgroundDrawableResource(resid);
    }
    
    /**
     * 设置Dialog下的背景模糊
     */
    public void setBlurBehind() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }
    
    /**
     * 设置窗体本身的透明度 alpha在0.0f到1.0f之间。1.0完全不透明，0.0f完全透明
     * @param alpha 
     */
    public void setAlpha(float alpha) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.alpha = alpha;
    	window.setAttributes(layoutParams);
    }
    
    /**
     * 设置黑暗度 dimAmount在0.0f和1.0f之间，0.0f完全不暗，1.0f全暗
     * @param dimAmount
     */
    public void setDimAmount(float dimAmount) {
    	Window window = getWindow();
    	WindowManager.LayoutParams layoutParams = window.getAttributes();
    	layoutParams.dimAmount = dimAmount;
    	window.setAttributes(layoutParams);
    }
}
