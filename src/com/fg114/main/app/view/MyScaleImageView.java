package com.fg114.main.app.view;



import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.FileObject;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ImageUtil;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MyScaleImageView extends ImageView 
{
	@SuppressWarnings("unused")
	private static final String TAG = "ImageViewTouchBase";
    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    private final Matrix mDisplayMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];
    protected Bitmap image = null;
    private volatile String bitmapLocalPath;

	int mThisWidth = -1, mThisHeight = -1;

	float mMaxZoom = 5.0f;// 最大缩放比例
	float mMinZoom= 1f;// 最小缩放比例

	private int imageWidth;// 图片的原始宽度
	private int imageHeight;// 图片的原始高度

	private float scaleRate;// 图片适应屏幕的缩放比例
	
	private static final boolean DEBUG = Settings.DEBUG;
	private static final int MSG_RELOAD_IMAGE = 1;
	private static final int RETRY_INTERVAL = 2000;
	
	
	//图片网络路径
	public String url;
	//图像在父组件中的位置
	public int position;
	//图像在组件中的显示方式
	private ScaleType scaleType;
	//图像在组件中的显示方式
	private boolean isSmallPic;
	//图像对象
	public Bitmap bitmap;
	//获得图像线程
//	private Thread getBitmapThread;
	private MyRunnable getBitmapThread;
//	private LinkedList<Bitmap> cache;
	//是否在线程池中
	private boolean isInThreadPool = false;
	
	private boolean mFirstLoad = true;	//是否第一次加载此图片
	private String oldUrl = "";	//上次加载的URL
	
	private boolean isTodayFood = false;
	
	//时间戳
	private volatile long timestamp=0;
	Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.frame_loading_rotate);
	

	private Context ctx;
	public MyScaleImageView(Context context) {
		super(context);
		this.ctx=context;
	}

	public MyScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx=context;
	}
	
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_RELOAD_IMAGE:
				// 居中显示载入图片
				Log.e("重新载入",""+url);
				setScaleType(ScaleType.CENTER_INSIDE);
				setImageResource(ImageUtil.loading);
				//startAnimation(rotate);

				runLoadBitmapThread(timestamp);
				break;
			}
		}
	};

	/**
	 * 计算图片要适应屏幕需要缩放的比例
	 */
	private void arithScaleRate() {
		/*float scaleWidth = (float)RestaurantGalleryActivity.dm.widthPixels / (float) imageWidth;
		float scaleHeight = (float)RestaurantGalleryActivity.dm.heightPixels / (float) imageHeight;
		scaleRate = Math.min(scaleWidth, scaleHeight);
		Log.e("bug", "scalewidth"+scaleWidth+"scalehight"+scaleHeight);*/
		scaleRate = Math.min(
				(float) Settings.DISPLAY.widthPixels/(float) imageWidth,
				(float) Settings.DISPLAY.heightPixels/(float) imageHeight);
//		Log.e("bug", "DISPLAY.widthPixels="+Settings.DISPLAY.widthPixels+", DISPLAY.heightPixels="+Settings.DISPLAY.heightPixels);
//		Log.e("bug", "imageWidth="+imageWidth+", imageHeight="+imageHeight);
		/* if (scaleRate < 1.0) {
	            getImageViewMatrix().postScale(scaleRate, scaleRate);
	        }*/
		 mMinZoom=scaleRate;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		try {

			super.onDraw(canvas);

		} catch (Exception e) {
			bitmap = null;
			bitmapLocalPath=null;
			Log.d("MyScaleView onDraw()", "恢复图片!" + e.getMessage() + ", " + this.url);
			//e.printStackTrace();
			setImageByUrl(this.url);
		}
	}

	public float getScaleRate() {
		return scaleRate;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	//设置图片的相关属性
	public void initImageSource(Bitmap bitmap)
	{
		this.imageHeight = bitmap.getHeight();
		this.imageWidth = bitmap.getWidth();
		
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			event.startTracking();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
			if (getScale() > 1.0f) {
				// If we're zoomed in, pressing Back jumps out to show the
				// entire image, otherwise Back returns the user to the gallery.
				zoomTo(1.0f);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	

	
	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);
		
		image = bitmap;
		
		// 计算适应屏幕的比例
		arithScaleRate();
		
		//Log.e("bug", "setImageBitmap->>>>>> scaleRate="+scaleRate);
		
		//缩放到屏幕大小
		zoomTo(scaleRate,Settings.DISPLAY.widthPixels / 2f, Settings.DISPLAY.heightPixels / 2f);
		//center(true, true);
	}

	// Center as much as possible in one or both axis. Centering is
	// defined as follows: if the image is scaled down below the
	// view's dimensions then center it (literally). If the image
	// is scaled larger than the view and is translated out of view
	// then translate it back into view (i.e. eliminate black bars).
	protected void center(boolean horizontal, boolean vertical) {
		
		if (image == null) {
			return;
		}

		
		Matrix m = getImageViewMatrix();

		RectF rect = new RectF(0, 0, image.getWidth(), image.getHeight());
		
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下放留空则往下移
            int screenHeight = Settings.DISPLAY.heightPixels;
            
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
            	
            	
            	deltaY = screenHeight - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = Settings.DISPLAY.widthPixels;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
            	
            	
            	deltaX = screenWidth - rect.right;
            }
        }

        
        
		postTranslate(deltaX, deltaY);
		setImageMatrix(getImageViewMatrix());
		
	}

	
	
	/**
	 * 设置图片居中显示
	 */
	/*public void layoutToCenter()
	{
		//正在显示的图片实际宽高
		float width = imageWidth*getScale();
		float height = imageHeight*getScale();
		
		//空白区域宽高
		float fill_width = Settings.DISPLAY.widthPixels - width;
		float fill_height = Settings.DISPLAY.heightPixels - height;
		
		//需要移动的距离
		float tran_width = 0f;
		float tran_height = 0f;
		
		if(fill_width>0)
			tran_width = fill_width/2;
		if(fill_height>0)
			tran_height = fill_height/2;
			
		
		postTranslate(tran_width, tran_height);
		setImageMatrix(getImageViewMatrix());
	}*/

	protected float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(mMatrixValues);
		/*if(imageWidth==0)
		{
			mMinZoom=1;
		} else
		{
			mMinZoom =( Settings.DISPLAY.widthPixels/2f)/imageWidth;
		}*/
		
		
		return mMatrixValues[whichValue];
	}

	// Get the scale factor out of the matrix.
	protected float getScale(Matrix matrix) {
		return getValue(matrix, Matrix.MSCALE_X);
	}

	protected float getScale() {
		return getScale(mSuppMatrix);
	}

	// Combine the base matrix and the supp matrix to make the final matrix.
	protected Matrix getImageViewMatrix() {
		// The final matrix is computed as the concatentation of the base matrix
		// and the supplementary matrix.
		mDisplayMatrix.set(mBaseMatrix);
		mDisplayMatrix.postConcat(mSuppMatrix);
		return mDisplayMatrix;
	}

	static final float SCALE_RATE = 1.25F;

	// Sets the maximum zoom, which is a scale relative to the base matrix. It
	// is calculated to show the image at 400% zoom regardless of screen or
	// image orientation. If in the future we decode the full 3 megapixel image,
	// rather than the current 1024x768, this should be changed down to 200%.
	protected float maxZoom() {
		if (image == null) {
			return 1F;
		}

		float fw = (float) image.getWidth() / (float) mThisWidth;
		float fh = (float) image.getHeight() / (float) mThisHeight;
		float max = Math.max(fw, fh) * 4;
		return max;
	}

	protected void zoomTo(float scale, float centerX, float centerY) {
		if (scale > mMaxZoom) {
			scale = mMaxZoom;
		} else if (scale < mMinZoom) {
			scale = mMinZoom;
		}

		float oldScale = getScale();
		float deltaScale = scale / oldScale;

		mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
		setImageMatrix(getImageViewMatrix());
		center(true, true);

		
	}

	protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
		final float incrementPerMs = (scale - getScale()) / durationMs;
		final float oldScale = getScale();
		final long startTime = System.currentTimeMillis();

		mHandler.post(new Runnable() {
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float target = oldScale + (incrementPerMs * currentMs);
				zoomTo(target, centerX, centerY);
				if (currentMs < durationMs) {
					mHandler.post(this);
				}
			}
		});
	}
	

	protected void zoomTo(float scale) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		zoomTo(scale, cx, cy);
	}

	protected void zoomToPoint(float scale, float pointX, float pointY) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		panBy(cx - pointX, cy - pointY);
		zoomTo(scale, cx, cy);
	}

	protected void zoomIn() {
		zoomIn(SCALE_RATE);
	}

	protected void zoomOut() {
		zoomOut(SCALE_RATE);
	}

	protected void zoomIn(float rate) {
		if (getScale() >= mMaxZoom) {
			return; // Don't let the user zoom into the molecular level.
		} else if (getScale() <= mMinZoom) {
			return;
		}
		if (image == null) {
			return;
		}

		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		mSuppMatrix.postScale(rate, rate, cx, cy);
		setImageMatrix(getImageViewMatrix());
	}

	protected void zoomOut(float rate) {
		if (image == null) {
			return;
		}

		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		// Zoom out to at most 1x.
		Matrix tmp = new Matrix(mSuppMatrix);
		tmp.postScale(1F / rate, 1F / rate, cx, cy);

		if (getScale(tmp) < 1F) {
			mSuppMatrix.setScale(1F, 1F, cx, cy);
		} else {
			mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
		}
		setImageMatrix(getImageViewMatrix());
		center(true, true);
	}

	public void postTranslate(float dx, float dy) {
		mSuppMatrix.postTranslate(dx, dy);
		setImageMatrix(getImageViewMatrix());
	}
	float _dy=0.0f;
	protected void postTranslateDur( final float dy, final float durationMs) {
		_dy=0.0f;
		final float incrementPerMs = dy / durationMs;
		final long startTime = System.currentTimeMillis();
		mHandler.post(new Runnable() {
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				
				postTranslate(0, incrementPerMs*currentMs-_dy);
				_dy=incrementPerMs*currentMs;

				if (currentMs < durationMs) {
					mHandler.post(this);
				}
			}
		});
	}

	protected void panBy(float dx, float dy) {
		postTranslate(dx, dy);
		setImageMatrix(getImageViewMatrix());
	}
	
	
	/**
	 * 初始化图像组件，添加属性
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
	public void setImageByUrl(String url) {
		
		if (url == null) {
			return;
		}
		
		this.url = url;		
		this.timestamp++;
		
		try {
			// 首先从内存缓存中读，如果有，直接设置，不开线程，忽略这里的错误
			FileObject f = FileCacheUtil.getInstance().getFromMemory("MyGalleryView", url);
			if (f != null) {
				Bitmap bmp = f.getContentAsBitmap();
				if(bmp!=null){
					bitmap = bmp;
					bitmapLocalPath=f.getFullFileName();
					Log.d("快捷"+Thread.currentThread().getId(), ""+bitmapLocalPath);
					showImage(this.timestamp);
					// 显示完就退出
					return;
				}
			} 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//当加载的URL与上次不同时，表示第一次加载该图片
		if (!oldUrl.equals(url)) {
			oldUrl = url;
			mFirstLoad = true;
		}
		
		//居中显示载入图片
    	setScaleType(ScaleType.CENTER_INSIDE);
		setImageResource(ImageUtil.loading);
		
		//startAnimation(rotate);
		
		runLoadBitmapThread(this.timestamp);
	}
	
	/**
	 * 初始化图像组件，添加属性，bitmap载入后，执行回调
	 * @param url
	 * @param isSmallImage
	 * @param parent
	 * @param position
	 */
//	public void setImageByUrl(String url, boolean isSmallPic, int position, ScaleType scaleType, LinkedList<Bitmap> cache) {
//		this.cache = cache;
//		setImageByUrl(url, isSmallPic, position, scaleType);
//	}

	
	/**
	 * 开始载入图片
	 */
	public void runLoadBitmapThread(long timestamp) {
		//图片载入线程放入线程池
		if (getBitmapThread == null) {
//			getBitmapThread = new Runnable() {
//				
//				@Override
//				public void run() {
//					loadImage(timestamp);
//				}
//			};
			getBitmapThread = new MyRunnable(timestamp); 
//			getBitmapThread = new LoadBitmapThread(timestamp);
//			getBitmapThread.setPriority(Thread.NORM_PRIORITY-1);
		}
//		((LoadBitmapThread)getBitmapThread).timestamp=timestamp;
		getBitmapThread.timestamp = timestamp;
		/*if (!isInThreadPool) {
			//当bitmap未载入或者已被回收并且不在线程池中的场合
			isInThreadPool = true;
//			ImageUtil.getInstance(getContext()).submitThread(getBitmapThread);
			ImageUtil.getInstance().submitThread(getBitmapThread);
		} else {
			if (DEBUG) Log.d(TAG, "该线程已加入线程池 view:" + this);
		}*/
		
		ImageUtil.getInstance().submitThread(getBitmapThread);
	}
	
	private class MyRunnable implements Runnable {
		
		public long timestamp;
		
		public MyRunnable(long timestamp){
			this.timestamp=timestamp;
		} 

		@Override
		public void run() {
			loadImage(timestamp);
		}
		
	}
	
	private void loadImage(long timestamp) {
		if (DEBUG) Log.d("LoadBitmapThread", "LoadBitmapThread start position:" + position + "," + "url:" + url);
		//如果不是最新图片，取消显示
		long id=Thread.currentThread().getId();
		Log.w("loadImage!"+id,this.timestamp+","+timestamp);
		//获得Bitmap
		try {
			if(MyScaleImageView.this.timestamp!=timestamp){
				//Log.w("1 returned!",MyImageView.this.timestamp+","+timestamp);
				return;
			}
			
			int width = ActivityUtil.getWindowsPixels((Activity)getContext()).widthPixels;
			//if (DEBUG) Log.d(TAG, "width:" + width + "position" + position);
//			bitmap = ImageUtil.getInstance().getBitmap(url, true, width);
			FileObject f = FileCacheUtil.getInstance().get("MyGalleryView", url);
			if (f != null) {
				bitmapLocalPath=f.getFullFileName();
				bitmap = f.getContentAsBitmap();
			} else {
				bitmap = null;
				bitmapLocalPath=null;
				Log.d("LoadBitmapThread-"+id, "bitmap= null position-:" + position+","+timestamp+",url="+this.url);
			}
			
			//如果不是最新图片，取消显示
			if(MyScaleImageView.this.timestamp!=timestamp){
				//Log.w("2 returned!",MyImageView.this.timestamp+","+timestamp);
				return;
			}
			if(bitmap != null) 
			{
				bitmapLocalPath=f.getFullFileName();
				
				//如果不是最新图片，取消显示
				if(MyScaleImageView.this.timestamp!=timestamp){
					//Log.w("3 returned!",MyImageView.this.timestamp+","+timestamp);
					return;
				}
				
                showImage(timestamp);
            } else {
            	if (DEBUG||true) Log.d("LoadBitmapThread-"+id, "bitmap is null position-:" + position+","+timestamp+",url="+this.url);
            	//如果不是最新图片，取消显示
    			if(MyScaleImageView.this.timestamp!=timestamp){
    				//Log.w("4 returned!",MyImageView.this.timestamp+","+timestamp);
    				return;
    			}
    			bitmapLocalPath=null;
            	showImageResource(timestamp,ImageUtil.nopic);
            	if (mFirstLoad) {
            		//如第一次加载该图片失败，则固定时间后再尝试一次
            		mFirstLoad = false;
            		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RELOAD_IMAGE), RETRY_INTERVAL);  
            	}
            	
            }
		} catch (Exception e) {
			if (bitmap != null) {
				bitmap.recycle();
			}
			e.printStackTrace();
			//如果不是最新图片，取消显示
			if(MyScaleImageView.this.timestamp!=timestamp){
				return;
			}
			showImageResource(timestamp,ImageUtil.nopic);
		} finally { 
			//clearAnimation();
			//执行结束，退出线程池
			isInThreadPool = false;
			
		}
	}
	/**
	 * 主线程显示图片
	 */
	private void showImage(final long timestamp) {

		final long id=Thread.currentThread().getId();
		Log.e("showImage!"+id,this.timestamp+","+timestamp+",bitmapLocalPath="+bitmapLocalPath);
		if(timestamp!=MyScaleImageView.this.timestamp){
			//Log.w("6 returned!",MyImageView.this.timestamp+","+timestamp);
			return;
		}
		((Activity)getContext()).runOnUiThread(
				new Runnable() {
			
					@Override
					public void run() {
						clearAnimation();
						Log.e("showImage!!!"+id,MyScaleImageView.this.timestamp+","+timestamp+",bitmapLocalPath="+bitmapLocalPath);
						if(timestamp!=MyScaleImageView.this.timestamp){
							//Log.w("7 returned!",MyImageView.this.timestamp+","+timestamp);
							return;
						}
						
						initImageSource(bitmap);
						setScaleType(ScaleType.MATRIX);
						setImageBitmap(bitmap);
						
					}
				});

	}

	/**
	 * 主线程显示图片
	 */
	private void showImageResource(final long timestamp,final int resId) {
		
		if(timestamp!=MyScaleImageView.this.timestamp){
			return;
		}
		((Activity)getContext()).runOnUiThread(
				new Runnable() {
			
					@Override
					public void run() {
						clearAnimation();
						if(timestamp!=MyScaleImageView.this.timestamp){
							return;
						}
						
						MyScaleImageView.this.setImageResource(resId);
					}
				});
	}
	
	public void resetImageView() {
		// 计算适应屏幕的比例
		arithScaleRate();
		
		//Log.e("bug", "resetImageView->>>>>> scaleRate="+scaleRate);
		//缩放到屏幕大小
		zoomTo(scaleRate,Settings.DISPLAY.widthPixels / 2f, Settings.DISPLAY.heightPixels / 2f);
		
	}
	/**
	 * 返回此ImageView显示的图片的本地文件名。如果是无效图片返回null
	 * @return
	 */
	public String getImageLocalFileName(){
		return bitmapLocalPath;
	}
}
