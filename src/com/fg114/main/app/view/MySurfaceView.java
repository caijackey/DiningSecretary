package com.fg114.main.app.view;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.fg114.main.app.Settings;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.UnitUtil;

/**
 * 360度环视图
 * @author zhangyifan
 *
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String TAG = "MySurfaceView";
	private static final boolean DEBUG = Settings.DEBUG;
	
	//每秒帧数
	private static final int FRAME_SPEED = 15;
	//持续秒数
	private static final double TIMER = 30.0;
	//每帧持续时间
	private static final long period = 1000 / FRAME_SPEED;
	//播放一张图需要绘制的次数
	private static final double updateCount = 2.5 * TIMER * FRAME_SPEED;
	 
    private Timer mTimer;    
    private MyTimerTask mTimerTask;
	private SurfaceHolder mHolder;
    private int mWidth;
    private int mHeight;
    
	private Bitmap bitmap;
	//当前绘制的次数
	private int count = 0;
	//当前偏移量
	private float offset = 0.0f;
	
	private AtomicBoolean mIsShow = new AtomicBoolean(false);
	
	public MySurfaceView(Context context, Bitmap pic, int width, int height) {
		super(context, null);
		
		this.bitmap = pic;
		this.mWidth = (int)ImageUtil.getPX(context, width);
		this.mHeight = (int)ImageUtil.getPX(context, height);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setLayoutParams(new LinearLayout.LayoutParams(this.mWidth, this.mHeight));
		mHolder = this.getHolder();
		mHolder.addCallback(this);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(DEBUG) Log.d(TAG,"surfaceCreated sufaceView=" + this);
		mTimer = new Timer();    
        mTimerTask = new MyTimerTask(); 
		mTimer.schedule(mTimerTask, 0, period);
		mIsShow.set(true);
	}
	
	/**
	 * 当surface结束时，停止播放线程
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(DEBUG) Log.d(TAG,"surfaceDestroyed sufaceView=" + this);
		
		//结束播放
		//将当前的surfacelayout的标志位设为不能滚动状态
	    //mySurfacelayout.setStopScroll(true);
		
//		Log.e("surfaceDestroyed", "surfaceDestroyed");
		
		clearMy360Draw();
		mIsShow.set(false);
		mTimer.cancel();
		mTimerTask.cancel();

	}    

	/**
	 * 环视图效果线程
	 * @author zhangyifan
	 *
	 */
    class MyTimerTask extends TimerTask {    
        @Override    
        public void run() {  
        	try {
				if (!mIsShow.get()) {
					return;
				}
				if (DEBUG)
					Log.d(TAG, "360 isRuning");
				//获取画布    
				Canvas canvas = mHolder.lockCanvas(); 
				
				// 运用Matrix将图片缩放2.5倍
				Matrix matrix = new Matrix();
				matrix.setScale(2.5f, 2.5f);
		        matrix.preTranslate(0, UnitUtil.dip2px(25));
		        canvas.setMatrix(matrix);
		        
				if (count < updateCount) {

//					synchronized (mHolder) {
						my360Draw(canvas, offset);
						count++;
						//计算下一帧的起始位置
						offset = (float) (0 - count * (bitmap.getWidth() / updateCount));
//					}

					if (DEBUG)
						Log.d(TAG, "offset：" + offset);
				} else {
					count = 0;
				}
				// 解锁画布，提交画好的图像    
				mHolder.unlockCanvasAndPost(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }    
    
    }    
    
    /**
     * 绘制环视图   
     * @param length
     */
    private void my360Draw(Canvas canvas, float offset) {
    	try {
			if (bitmap != null && bitmap.isRecycled()) {
			} else {
//				Log.e("my360Draw", "mIsShow=" + mIsShow.get());
				if (!mIsShow.get()) {
					return;
				}
				canvas.drawBitmap(bitmap, offset, 0, null);
				canvas.drawBitmap(bitmap, offset + bitmap.getWidth(), 0, null);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
    }    
	
    private void clearMy360Draw() {  
        try {
        	if (mHolder == null) {
        		return;
        	}
//			Canvas canvas = mHolder.lockCanvas();
//			canvas.drawColor(Color.BLACK);// 清除画布    
//			mHolder.unlockCanvasAndPost(canvas);
			mHolder.removeCallback(this);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
}
