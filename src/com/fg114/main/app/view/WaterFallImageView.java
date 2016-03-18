package com.fg114.main.app.view;

import java.util.LinkedList;
import java.util.Random;
import java.util.RandomAccess;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.FileObject;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.MyThreadPool;
import com.fg114.main.util.UnitUtil;

/**
 * 瀑布流 图片显示控件
 * @author liwenjie
 *
 */
public class WaterFallImageView extends ImageView {

	private static final String TAG = "MyImageView";
	private static final boolean DEBUG = Settings.DEBUG;
	private static final int MSG_RELOAD_IMAGE = 1;
	private static final int RETRY_INTERVAL = 3000;
	//public static final ThreadLocal<String> th = new ThreadLocal<String>();

	// 图片网络路径
	public volatile String url;
	// 图像在父组件中的位置
	public int position;
	// 图像在组件中的显示方式
	private ScaleType scaleType;
	// 图像在组件中的显示方式
	private boolean isSmallPic;
	// 图像对象
	public Bitmap bitmap;
	// 获得图像线程
	private Thread getBitmapThread;
	private LinkedList<Bitmap> cache;
	// 是否在线程池中
	private boolean isInThreadPool = false;

	private boolean mFirstLoad = true; // 是否第一次加载此图片
	private String oldUrl = ""; // 上次加载的URL
	private volatile long timestamp = 0;

	private boolean isTodayFood = false;
	private volatile boolean drawBackground = true;

	//Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.frame_loading_rotate);

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_RELOAD_IMAGE:
					// 居中显示载入图片
					runLoadBitmapThread(WaterFallImageView.this.timestamp);
					if (DEBUG)
						Log.w("重试载入", WaterFallImageView.this.url + "");
					break;
			}
		}
	};
	

	public WaterFallImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaterFallImageView(Context context) {
		super(context);
	}

	public WaterFallImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			
			super.onDraw(canvas);

		} catch (Exception e) {
			bitmap = null;
			Log.d("MyImageView onDraw()", "恢复图片!" + e.getMessage() + ", " + this.url);
			//e.printStackTrace();
			setImageByUrl(this.url, true, this.position, this.scaleType);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		if (bm == null) {
			//startAnimation(rotate);
			//setDrawBackground(true);
		} else {
			//clearAnimation();
			//setDrawBackground(false);
		}
		super.setImageBitmap(bm);
	}

	@Override
	public void setImageResource(int resId) {
		
		setDrawBackground(false);
		super.setImageResource(resId);
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
	}

	public void setImageByUrl(String url, boolean isSmallPic, int position, ScaleType scaleType, LinkedList<Bitmap> cache, boolean isTodayFood) {
		// Log.e("[1]this.url=url "+position+","+(url==null?"null":""+(this.url==url))
		// ,this.url+", "+url);
		// 如果url没有变，就不重复设置图像

		this.url = url;
		this.timestamp++;
		this.position = position;
		this.scaleType = scaleType;
		this.isSmallPic = isSmallPic;
		this.isTodayFood = isTodayFood;

		// 当加载的URL与上次不同时，表示第一次加载该图片
		if (!oldUrl.equals(url)) {
			oldUrl = url;
			mFirstLoad = true;
		}
		

		runLoadBitmapThread(this.timestamp);
	}

	/**
	 * 初始化图像组件，添加属性
	 * 
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
	public void setImageByUrl(String url, boolean isSmallPic, int position, ScaleType scaleType) {
		// Log.e("[2]this.url=url "+position+","+(url==null?"null":""+(this.url==url))
		// ,this.url+", "+url);

		this.url = url;
		this.timestamp++;
		this.position = position;
		this.scaleType = scaleType;
		this.isSmallPic = isSmallPic;
		
		
		
		// 当加载的URL与上次不同时，表示第一次加载该图片
		if (!oldUrl.equals(url)) {
			oldUrl = url;
			mFirstLoad = true;
		}
		// 清除旧图片
		// setImageBitmap(null);
		// startAnimation(rotate);

		runLoadBitmapThread(this.timestamp);
	}

	/**
	 * 初始化图像组件，添加属性，bitmap载入后，执行回调
	 * 
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
	public void setImageByUrl(String url, boolean isSmallPic, int position, ScaleType scaleType, LinkedList<Bitmap> cache) {
		this.cache = cache;
		setImageByUrl(url, isSmallPic, position, scaleType);
	}

	/**
	 * 开始载入图片
	 */
	public void runLoadBitmapThread(long timestamp) {

		try {
			// 首先从内存缓存中读，如果有，直接设置，不开线程，忽略这里的错误
			FileObject f = FileCacheUtil.getInstance().getFromMemory("MyImageView", url);
			if (f != null) {
				bitmap = f.getContentAsBitmap();
			} else {

				bitmap = null;
			}
			// 显示
			if (bitmap != null) {
				showImage(this.timestamp);
				return; // 显示完就退出
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 设置“正在载入”图
		setImageBitmap(null);
		// ---
		if (DEBUG)
			Log.w("提交线程！[池负载：" + ImageUtil.inc() + "]position=" + position, this.url);
		LoadBitmapThread getBitmapThread = new LoadBitmapThread(timestamp, this.url);
		ImageUtil.pool.submit(getBitmapThread);
	}

	private static long start = System.currentTimeMillis();

	private static synchronized long getStamp() {
		return start++;
	}

	/**
	 * 获取图片bitmap线程
	 * 
	 * @author zhangyifan
	 * 
	 */
	private class LoadBitmapThread extends MyThreadPool.Task implements MyThreadPool.Discardable {

		private long timestamp = 0;
		private String url;

		public LoadBitmapThread(long timestamp, String url) {
			this.timestamp = timestamp;
			this.url = url;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			if (DEBUG)
				Log.w("图片线程run开始:position=" + position , "isSmallPic=" + isSmallPic + "," + start + ",url:" + url);
			// 获得Bitmap
			try {
				if (timestamp != WaterFallImageView.this.timestamp) {
					return;
				}
				FileObject f = FileCacheUtil.getInstance().get("MyImageView", url);
				if (f != null) {

					bitmap = f.getContentAsBitmap();
				} else {

					bitmap = null;
				}
				// 显示
				if (bitmap != null) {
					
					showImage(this.timestamp);
				} else {
					
					showNoPictureImageResource(this.timestamp, ImageUtil.nopic);
					if (mFirstLoad) {
						// 如第一次加载该图片失败，则固定时间后再尝试一次
						mFirstLoad = false;
						mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RELOAD_IMAGE), RETRY_INTERVAL);
					}

				}
			} catch (Exception e) {
				Log.e("图片加载异常：position=" + position , "url:" + url, e);
				showNoPictureImageResource(this.timestamp, ImageUtil.nopic);
			} finally {
				// clearAnimation();
				if (DEBUG)
					Log.w("图片线程getBitmap完成:position=" + position, "耗时：" + (System.currentTimeMillis() - start) + ",url:" + url);
			}
		}

		@Override
		public boolean discardMe() {

			if (timestamp != WaterFallImageView.this.timestamp) {
				return true;
			}
			return false;
		}

		public String toString() {
			return "LoadBitmapThread:" + this.timestamp + "," + this.url;
		}
	}

	/**
	 * 主线程显示图片
	 */
	private void showImage(final long timestamp) {
		if (timestamp != WaterFallImageView.this.timestamp) {
			return;
		}

		
		((Activity) getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (timestamp != WaterFallImageView.this.timestamp) {
					return;
				}
				setScaleType(WaterFallImageView.this.scaleType);
				WaterFallImageView.this.setImageBitmap(bitmap);

			}
		});

	}

	/**
	 * 主线程显示图片
	 */
	private void showNoPictureImageResource(final long timestamp, final int resId) {
		if (timestamp != WaterFallImageView.this.timestamp) {
			return;
		}
		((Activity) getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (timestamp != WaterFallImageView.this.timestamp) {
					return;
				}
				setScaleType(ScaleType.CENTER_INSIDE);
				WaterFallImageView.this.setImageResource(resId);
			}
		});
	}

	/**
	 * 回收控件
	 */
	public void recycle(boolean isUseAgain) {
		// if (isInThreadPool) {
		// if (DEBUG) Log.d(TAG, "interrupt position:" + position);
		// getBitmapThread.interrupt();
		// }
		//
		// if (this.bitmap != null) {
		// //this.bitmap.recycle();
		// this.bitmap = null;
		// }
		// if (!isUseAgain) {
		// //取消对控件的应用
		// ViewParent parent = this.getParent();
		// if (parent instanceof LinearLayout) {
		// ((LinearLayout) parent).removeView(this);
		// }
		// }
	}

	public boolean isDrawBackground() {
		return drawBackground;
	}

	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

}

