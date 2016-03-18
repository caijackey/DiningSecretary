/**
 * 圆角图片的使用
 * 在value/attrs.xml已经定义了两个属性 roundType和roundRadius,此控件初始化是会获取这两个参数，见构造函数
 * roundType 0:默认无圆角 1:四周圆角  2:只有上面是圆角
 * roundRadius 圆角半径 传入的是dip，会自动转成px（已经乘了density）
 * 
 * 请注意此圆角对背景无效！
 * 
 * XML中的使用方法
 * XML里面最外面的控件里面定义命名空间xmlns:local="http://schemas.android.com/apk/res/com.fg114.main"
 * 然后直接修改roundtype和roundradius数据
 * <LinearLayout
 *	xmlns:android="http://schemas.android.com/apk/res/android"
 *  xmlns:local="http://schemas.android.com/apk/res/com.fg114.main"
 *	android:layout_width="wrap_content"
 *	android:layout_height="wrap_content"
 *	android:orientation="vertical"
 *	>
 *  .
 *  .
 *  .
 *  <com.fg114.main.app.view.MyImageView
 *			android:id="@+id/foodpicture"
 *			local:roundType="2"
 *			local:roundRadius="10dip"
 *			android:layout_width="fill_parent"
 *			android:layout_height="fill_parent"  />
 *  .
 *  .
 *  .
 *  </LinearLayout>
 */

package com.fg114.main.app.view;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.fg114.main.R;
import com.fg114.main.cache.FileCacheUtil;
import com.fg114.main.cache.FileObject;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.MyThreadPool;

/**
 * 
 * @author zhangyifan
 * 
 */
public class MyImageView extends ImageView {

	private static final String TAG = "MyImageView";
	private static final boolean DEBUG = true;
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

	private Bitmap roundBitmap;
	private volatile String bitmapLocalPath;
	// 获得图像线程
	private Thread getBitmapThread;
	private LinkedList<Bitmap> cache;
	// 是否在线程池中
	private boolean isInThreadPool = false;

	private boolean mFirstLoad = true; // 是否第一次加载此图片

	private boolean needRefresh = false; //是否需要刷新
	private String oldUrl = ""; // 上次加载的URL
	private volatile long timestamp = 0;

	private boolean isTodayFood = false;
	private static Bitmap background;
	private volatile boolean drawBackground = true;

	//是否走本地缩略图的逻辑，见缓存里的get(...)方法
	public boolean isThumbnail = false;

	/**
	 * 设置圆角, 0:无圆角 默认值 1:四个角全部圆角 2:只有上面两个角圆角
	 */
	public int roundType = 0;
	/**
	 * 设置圆角半径, 默认10.0f
	 */
	public float roundRadius = 10.0f;

	Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.frame_loading_rotate);

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_RELOAD_IMAGE:
					// 居中显示载入图片
					runLoadBitmapThread(MyImageView.this.timestamp);
					if (DEBUG)
						Log.w("重试载入", MyImageView.this.url + "");
					break;
			}
		}
	};
	private Runnable callbackWhenDone;
	static {
		background = BitmapFactory.decodeResource(ContextUtil.getContext().getResources(), ImageUtil.loading);
	}
	{
		setScaleType(ScaleType.CENTER_INSIDE);
	}

	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.RoundCorner);
		roundType = attr.getInt(R.styleable.RoundCorner_roundType, 0);
		roundRadius = (float) attr.getDimensionPixelSize(R.styleable.RoundCorner_roundRadius, 10);
	}

	public MyImageView(Context context) {
		super(context);
	}

	public MyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.RoundCorner);
		roundType = attr.getInt(R.styleable.RoundCorner_roundType, 0);
		roundRadius = (float) attr.getDimensionPixelSize(R.styleable.RoundCorner_roundRadius, 10);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (roundBitmap != null) {
			roundBitmap.recycle();
			roundBitmap = null;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			if (isDrawBackground()) {
//				Log.v("TAG", background+"   "+this.getWidth() +"  "+ background.getWidth()+"  "+this.getHeight()+"  "+background.getHeight());
				canvas.drawBitmap(background, (int) (this.getWidth() - background.getWidth()) / 2, (int) (this.getHeight() - background.getHeight()) / 2, null);
				return;
			}
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
			startAnimation(rotate);
			setDrawBackground(true);
		} else {
			clearAnimation();
			setDrawBackground(false);
		}
		super.setImageBitmap(bm);
	}

	@Override
	public void setImageResource(int resId) {
		clearAnimation();
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
		//Log.i(TAG, "old url:"+oldUrl+" new url:"+url+"  firstLoad:"+mFirstLoad);
		// 当加载的URL与上次不同时，表示第一次加载该图片
		if (!oldUrl.equals(url)) {
			oldUrl = url;
			mFirstLoad = true;
			needRefresh = true;
		} else
			needRefresh = false;
		// 清除旧图片
		// setImageBitmap(null);

		// startAnimation(rotate);

		runLoadBitmapThread(this.timestamp);
	}

	//可以设置加载完成后的回调通知
	public void setImageByUrl(String url, boolean isSmallPic, int position, ScaleType scaleType, Runnable callbackWhenDone) {
		this.callbackWhenDone = callbackWhenDone;
		setImageByUrl(url, isSmallPic, position, scaleType);
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

		//Log.i(TAG, "oldUrl is:"+oldUrl+" url is:"+url+" firstlod is:"+mFirstLoad);
		// Log.e("[2]this.url=url "+position+","+(url==null?"null":""+(this.url==url))
		// ,this.url+", "+url);
		if (url == null) {
			return;
		}

		this.url = url;
		this.timestamp++;
		this.position = position;
		this.scaleType = scaleType;
		this.isSmallPic = isSmallPic;

		// 当加载的URL与上次不同时，表示第一次加载该图片
		if (!oldUrl.equals(url)) {
			oldUrl = url;
			mFirstLoad = true;
			needRefresh = true;
		} else{
			needRefresh = false;
		}
//		Log.v("TAG", needRefresh+"needRefresh");
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
				bitmapLocalPath = f.getFullFileName();
			} else {
				bitmapLocalPath = null;
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
		//if (DEBUG||true) Log.w("提交线程！ ",this.url);
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
				Log.w("图片线程run开始:position=" + position, "isSmallPic=" + isSmallPic + "," + start + ",url:" + url);
			// 获得Bitmap
			try {
				if (timestamp != MyImageView.this.timestamp) {
					return;
				}
				FileObject f = FileCacheUtil.getInstance().get("MyImageView", url, isThumbnail);
				if (f != null) {
					bitmap = f.getContentAsBitmap();
				} else {
					bitmap = null;
					bitmapLocalPath = null;
				}
				// 显示
				if (bitmap != null) {
					showImage(this.timestamp);
					bitmapLocalPath = f.getFullFileName();
				} else {

					showNoPictureImageResource(this.timestamp, ImageUtil.nopic);
					if (mFirstLoad) {
						// 如第一次加载该图片失败，则固定时间后再尝试一次
						mFirstLoad = false;
						mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RELOAD_IMAGE), RETRY_INTERVAL);
					}

				}
			} catch (Exception e) {
				Log.e("图片加载异常：position=" + position, "url:" + url, e);
				showNoPictureImageResource(this.timestamp, ImageUtil.nopic);
			} finally {
				// clearAnimation();
				if (DEBUG)
					Log.w("图片线程getBitmap完成:position=" + position, "耗时：" + (System.currentTimeMillis() - start) + ",url:" + url);
			}
		}

		@Override
		public boolean discardMe() {

			if (timestamp != MyImageView.this.timestamp) {
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
		if (timestamp != MyImageView.this.timestamp) {
			return;
		}

		((Activity) getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (timestamp != MyImageView.this.timestamp) {
					return;
				}
				setScaleType(MyImageView.this.scaleType);
				if (roundType > 0) {
					//Log.i(TAG, "need refresh:"+needRefresh+" roundBitmap:"+roundBitmap);
					if (needRefresh == false && roundBitmap != null)
						bitmap = roundBitmap;
					else
						bitmap = GetRoundedCornerBitmap(bitmap, roundType, roundRadius);

				}
				MyImageView.this.setImageBitmap(bitmap);

			}
		});
		//完成
		if (callbackWhenDone != null) {
			try {
				callbackWhenDone.run();

			} catch (Exception e) {
			}
		}

	}

	/**
	 * 主线程显示图片
	 */
	private void showNoPictureImageResource(final long timestamp, final int resId) {
		bitmapLocalPath = null;
		if (timestamp != MyImageView.this.timestamp) {
			return;
		}
		((Activity) getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (timestamp != MyImageView.this.timestamp) {
					return;
				}
				setScaleType(ScaleType.CENTER_INSIDE);
				MyImageView.this.setImageResource(resId);
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

	/**
	 * 钝化此控件，使图片内存可以得到释放，只用于手动控制图片内存优化时
	 */
	private volatile boolean isActive = false;

	public void inactivate() {
		isActive = false;
		if (CheckUtil.isEmpty(url) || this.bitmap == null || this.bitmap.isRecycled()) {
			return;
		}
		if (DEBUG)
			Log.e("MyImageView.inactivate()", "url:" + url);
		setScaleType(ScaleType.CENTER_INSIDE);
		MyImageView.this.setImageResource(ImageUtil.loading);
	}

	/**
	 * 激活此控件，使图片重新加载进来，只用于手动控制图片内存优化时
	 */
	public void activate() {
		if (CheckUtil.isEmpty(url)) {
			return;
		}
		if (isActive) {
			return;
		}
		//
		if (this.bitmap != null && !this.bitmap.isRecycled()) {
			if (DEBUG)
				Log.e("MyImageView.activate()", "直接恢复url:" + url);
			showImage(this.timestamp);
		} else {
			setImageByUrl(this.url, true, 0, this.scaleType);
			if (DEBUG)
				Log.e("MyImageView.activate()", "重新加载url:" + url);
		}
		isActive = true;
	}

	/**
	 * 返回此ImageView显示的图片的本地文件名。如果是无效图片返回null
	 * 
	 * @return
	 */
	public String getImageLocalFileName() {
		return bitmapLocalPath;
	}

	/**
	 * 生成圆角图片
	 */
	public Bitmap GetRoundedCornerBitmap(Bitmap bitmap, int roundTy, float roundPx) {
		try {
			if (roundBitmap != null) {
				roundBitmap.recycle();
				roundBitmap = null;
			}

			roundBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(roundBitmap);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			if (roundTy == 2) {
				//final Rect topRightRect = new Rect(bitmap.getWidth()/2, 0, bitmap.getWidth(), bitmap.getHeight()/2);
				//Fill in upper right corner
				//canvas.drawRect(topRightRect, paint);
				// Fill in bottom corners
				final Rect bottomRect = new Rect(0, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight());
				canvas.drawRect(bottomRect, paint);
			}
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			return roundBitmap;

		} catch (Exception e) {
			return bitmap;
		}
	}

}
