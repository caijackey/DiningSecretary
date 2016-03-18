package com.fg114.main.app.view;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.fg114.main.app.Settings;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.FileObject;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ImageUtil;

/**
 * 360度环视图View显示用
 * @author zhangyifan
 *
 */
public class MySurfaceLayout extends LinearLayout {
	
	private static final String TAG = "MySurfaceLayout";
	private static final boolean DEBUG = Settings.DEBUG;
	
	public MySurfaceView mySurfaceView;    
	private ImageView imageView;  
    
    private int mWidth;
    private int mHeight;
	
	//图片网络路径
	public String url;
	//图像对象
	public Bitmap bitmap;
	//图像在父组件中的位置
	public int position;
	//获得图像线程
	private MyRunnable getBitmapThread;
	private LinkedList<Bitmap> cache;
	//是否在线程池中
	private boolean isInThreadPool = false;
	
	//判断该组件的特定 gallery外层是否在滚动中
	private boolean isStopScroll=true;
	
	public boolean isStopScroll()
	{
		return isStopScroll;
	}

	public void setStopScroll(boolean isStopScroll)
	{
		this.isStopScroll = isStopScroll;
	}

	/**
	 * 
	 * @param context
	 * @param url
	 * @param width  (unit:dip)
	 * @param height
	 */
//	public MySurfaceLayout(Context context, int position, String url, int width, int height, ViewGroup fatherView) {
	public MySurfaceLayout(Context context, int width, int height, ViewGroup fatherView) {
		super(context);
		this.mWidth = width;
		this.mHeight = height;
		if (fatherView instanceof Gallery) {
			this.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.FILL_PARENT));
		} else if (fatherView instanceof LinearLayout) {
			this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		
		this.setGravity(Gravity.CENTER);
		init();
	}
	
	/**
	 * 初始化图像组件，添加属性
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
	public void setImageByUrl(String url,  int position) {
		this.url = url;
		this.position = position;
		
		if (CheckUtil.isEmpty(url)) {
			imageView.setImageResource(ImageUtil.nopic);
		} else {
			imageView.setImageResource(ImageUtil.loading);
		}
		//载入bitmap
		runLoadBitmapThread();
	}
	
	/**
	 * 初始化图像组件，添加属性，bitmap载入后，执行回调
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
	public void setImageByUrl(String url, int position, LinkedList<Bitmap> cache) {
		this.cache = cache;
		setImageByUrl(url, position);
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		this.setPadding(10, 10, 10, 10);
		this.setOrientation(LinearLayout.VERTICAL);
		initImageView();
		
	}

	/**
	 * 初始化ImageView
	 */
	private void initImageView() {
		imageView = new ImageView(this.getContext());
		imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		imageView.setVisibility(View.VISIBLE);
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		this.addView(imageView);
	}
	
	/**
	 * 初始化surfaceView
	 */
	private void initSurfaceView(Bitmap pic) {
		mySurfaceView = new MySurfaceView(this.getContext(), pic, this.mWidth, this.mHeight);
		mySurfaceView.setVisibility(View.VISIBLE);
		this.addView(mySurfaceView);
	}
	
	/**
	 * 开始载入图片
	 */
	public void runLoadBitmapThread() {
		//图片载入线程放入线程池
		if (getBitmapThread == null) {
			getBitmapThread = new MyRunnable(); 
		}
		ImageUtil.getInstance().submitThread(getBitmapThread);
	}
	
	private class MyRunnable implements Runnable {
		
		public MyRunnable(){
		} 

		@Override
		public void run() {
			loadImage();
		}
	}
	
	private void loadImage() {
		if (DEBUG) Log.d("LoadBitmapThread", "LoadBitmapThread start position:" + position + "," + "url:" + url);
		//获得Bitmap
		try {
//			bitmap = ImageUtil.getInstance(getContext()).getBitmap(url, false, mHeight);
//			bitmap = ImageUtil.getInstance().getBitmap(url, false, mHeight);
			FileObject f = FileCacheUtil.getInstance().get("MyGalleryView", url);
			if (f != null) {

				bitmap = f.getContentAsBitmap();
			} else {
				bitmap = null;
			}
		} catch (Exception e) {
        	if (DEBUG||true) Log.d("LoadBitmapThread", "FileNotFoundException position:" + position);
			showImageResource(ImageUtil.nopic);
			return;
		}
		
		if(bitmap != null) {
			if (cache != null) {
				synchronized (cache) {
					cache.addFirst(bitmap);
				}
			}
			play360Pic();
        } else {
        	if (DEBUG||true) Log.d("LoadBitmapThread", "bitmap is null position:" + position);
        	showImageResource(ImageUtil.loading);
        }
		//执行结束，退出线程池
		isInThreadPool = false;
	}
	
	/**
	 * 播放
	 * @param pic
	 */
	public void play360Pic() {
		((Activity)getContext()).runOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						if (bitmap == null) {
							//没有该图片的场合
							imageView.setImageResource(ImageUtil.nopic);
						} else {
							if (DEBUG) Log.d(TAG, "play360Pic");
							imageView.setVisibility(View.GONE);
							initSurfaceView(bitmap);
						}
					}
				});
	}
	
	/**
	 * 主线程显示图片
	 */
	private void showImageResource(final int resId) {
		((Activity)getContext()).runOnUiThread(
				new Runnable() {
			
					@Override
					public void run() {
						imageView.setImageResource(resId);
					}
				});
	}
	
	/**
	 * 回收控件
	 */
	public void recycle(boolean isUseAgain) {
		if (this.bitmap != null) {
			this.bitmap.recycle();
		}
		if (!isUseAgain) {
			ViewParent parent = this.getParent();
			if (parent instanceof LinearLayout) {
				((LinearLayout) parent).removeView(this);
			}
		}
		System.gc();
	}

	
	
}
