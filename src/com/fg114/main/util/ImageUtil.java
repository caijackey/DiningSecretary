package com.fg114.main.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.util.TypedValue;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import static com.fg114.main.app.Settings.*;

/**
 * 图片处理
 * @author zhangyifan
 *
 */
public class ImageUtil {
	
	private static final String TAG = "ImageUtil";
	//private static final boolean DEBUG = Settings.DEBUG; 
	private static final boolean DEBUG = Settings.DEBUG;
	public static final int loading = R.drawable.loading;
	public static final int nopic = R.drawable.nopic;
    
    //生成缩略图用参数
	private static final int OPTIONS_NONE = 0x0;
	private static final int OPTIONS_SCALE_UP = 0x1;
	private static final int OPTIONS_RECYCLE_INPUT = 0x2;
	
	//线程池大小
	private static final int THREAD_POOL_SIZE = 2; 
	//图片载入线程池
    public ExecutorService photosLoadThreadPool;
    
    public static int poolLoad=0;
    public static Object lk=new Object(); 
    public static synchronized int inc(){
    	poolLoad++;
    	return poolLoad;
    }
    public static synchronized int dec(){
    	poolLoad--;
    	return poolLoad;
    }
    
    //图片载入timeout时间
    private static final int TIME_OUT = 10000;
//    //缓存
//    private static final int MAX_CACHE_SIZE = 20;
//    private HashMap<String, Bitmap> cache;

    private File cacheDir;
    private static Context context;
	private static ImageUtil instance;
	//added by xu jianjun,2011-11-25
	private static Object locker=new Object();
	
	//图片线程池
	public final static MyThreadPool pool=new MyThreadPool(THREAD_POOL_SIZE);
	
	
/*	
	public static class ImagePool{
		
		interface Doer {
			public void doing();
		}
		class MyThread extends Thread{
			boolean isWorking=true;
			Doer doer;
			MyThread(Doer doer){
				this.doer=doer;
			}
			public void run(){
				while(doer!=null&&this.isWorking){
					doer.doing();
				}
			}			
		}
		//////
		private int poolsize=0;
		private ConcurrentLinkedQueue<Thread> queue=null;
		
		private MyThread worker=null;
		
		public ImagePool(int poolsize){
			queue=new ConcurrentLinkedQueue<Thread>();
			reset(poolsize);
		}
		public void setPoolSize(int poolsize){
			this.poolsize=poolsize;
		}
		public int getPoolSize(){
			return poolsize;
		}
		public void submit(Thread t){
			queue.add(t);
		}

		public void reset(int poolsize){
			if(worker!=null){
				worker.isWorking=false;
			}
			queue.clear();
			this.poolsize=poolsize;			
			worker=new MyThread(new Doer(){

				@Override
				public void doing() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					Thread t=queue.poll();
					while(t!=null){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
						if(queue.size()<=ImagePool.this.poolsize){
							
							ImageUtil.getInstance(ContextUtil.getContext()).submitThread(t);
							//t.start();
							if(DEBUG) Log.i("%%%%%%%","run"+queue.size());
						}
						else{
							if(DEBUG) Log.i("*******","discarded"+queue.size());
						}
						t=queue.poll();
						
					}							
					
				}
				
			});
			worker.start();
			
		}
		public void shutdown(){
			if(worker!=null){
				worker.isWorking=false;
			}
		}
	}
	public static ImagePool pool=new ImagePool(8);*/
	//
	public static ImageUtil getInstance(Context ctx){
		
		if(instance ==  null){
			
			synchronized(ImageUtil.locker){
				if(instance ==  null){
					instance = new ImageUtil(ContextUtil.getContext());
				}		
			}
		}
		return instance;
	}
	
	public static ImageUtil getInstance(){
		
		if(instance ==  null){
			
			synchronized(ImageUtil.locker){
				if(instance ==  null){
					instance = new ImageUtil(ContextUtil.getContext());
				}		
			}
		}
		return instance;
	}
	
	/**
	 * 提交任务到线程池中
	 */
    public void submitThread(Runnable task) {
    	photosLoadThreadPool.submit(task);    	
    }
    
    private ImageUtil(Context context) {
    	this.context = context;
//    	cache = new HashMap<String, Bitmap>();
    	photosLoadThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    	
//        //Make the background thead low priority. This way it will not affect the UI performance
//        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
        
        //判断存储卡是否存在
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	//在sd卡上建立图片存放空间
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),Settings.IMAGE_CACHE_DIRECTORY);
        } else {
        	//如没有存储卡，则在私有存储路径中开辟空间
            cacheDir = context.getCacheDir();
        }
        if(!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }
    
    /**
     * 获得bitmap对象
     */
    public Bitmap getBitmap(String url, boolean isScaleByWidth, int widthOrHeight) {
    	
    	Bitmap bitmap = null;
		//缓存中不存在的场合
		//从sdcard中获得bitmap对象
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        //from SD cache
        if (f.exists()) {
        	
        	bitmap = decodeFile(url, f, isScaleByWidth, widthOrHeight);
        	if (DEBUG) Log.d(TAG, "从本地读取! the bitmap="+bitmap);
        }
        URL imgUrl;
        HttpURLConnection conn=null;
        if (bitmap == null) {
        	//sdcard中不存在的场合
        	//从服务器获得bitmap对象
            PatchInputStream pis = null;
            long start=System.currentTimeMillis();
            double rand=Math.random();
            try {
            	if (DEBUG) Log.w(TAG, "从网络下载["+rand+"]"+start);
            	imgUrl = new URL(url);
            	conn = (HttpURLConnection) imgUrl.openConnection();
            	conn.setConnectTimeout(TIME_OUT);
            	conn.setReadTimeout(TIME_OUT);
            	
            	
                InputStream is = conn.getInputStream();
                pis = new PatchInputStream(is);
                OutputStream os = new FileOutputStream(f);
                StreamUtils.CopyStream(pis, os);
                os.close();
                if (DEBUG) Log.w(TAG, "从网络下载完成["+rand+"]"+(System.currentTimeMillis()-start));
                bitmap = decodeFile(url, f, isScaleByWidth, widthOrHeight);
                if (DEBUG) Log.w(TAG, "从下载完成的文件到bitmap="+bitmap+"["+rand+"]"+(System.currentTimeMillis()-start));
            } catch (SocketTimeoutException e) {
            	if (DEBUG) Log.e("getBitmap出错[socket]:["+rand+"][池负载："+ImageUtil.poolLoad+"]","--->",e);
            	//载入图片超时
            	if (bitmap != null) {
            		bitmap.recycle();
            	}
            	bitmap = null;
            } catch (Exception ex){
            	if (DEBUG) Log.e("getBitmap出错:["+rand+"][池负载："+ImageUtil.poolLoad+"]","--->",ex);
            	/*com.fg114.main.cache.FileCacheUtil.getInstance(context).saveInputStreamToFile(conn.getErrorStream(), android.os.Environment
						.getExternalStorageDirectory()+"", "errorDS_"+conn.getURL().getHost()+".txt");
            	*/
            	try{
            		if (DEBUG) Log.e("XXX",conn.getURL()+","+conn.getResponseCode()+","+conn.getResponseMessage());
            	}catch(Exception e){
            		if (DEBUG) Log.e("XXX","再出错",e);
            		
            	}
            	if (bitmap != null) {
            		bitmap.recycle();
            	}
            }
        }
    	return bitmap;
    }
    
    /**
     * 图片自适应view大小(徐健君注：目前此方法没有使用。2011-11-23)
     * @param orgBitmap
     * @return
     */
    public static Bitmap getScaleBitmap(Bitmap orgBitmap, int viewHeight) {
    	Bitmap resizeBitmap = null;
		if (orgBitmap != null) {
			int scaleHeight = viewHeight;
			int scaleWidth = scaleHeight * orgBitmap.getWidth() / orgBitmap.getHeight();
			//生成自适应后的图片
			resizeBitmap = ImageUtil.extractThumbnail(orgBitmap, scaleWidth, scaleHeight);
		}
		return resizeBitmap;
    }
    
    /**
     * decodes image and scales it to reduce memory consumption
     * @param f
     * @return
     */
    private Bitmap decodeFile(String url, File f, boolean isScaleByWidth, int widthOrHeight){
    	Bitmap bitmap = null;
        try {
        	BitmapFactory.Options options = new BitmapFactory.Options();
        	options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高
            bitmap = BitmapFactory.decodeFile(f.getPath(), options); //此时返回bm为空
            options.inJustDecodeBounds = false;
            //计算缩放比
            int scale = 0;
            if (widthOrHeight != 0) {
            	if (DEBUG) Log.d(TAG, "make thumbnail");
	            if (isScaleByWidth) {
	            	scale = (int)(options.outWidth / (float)widthOrHeight);
	            } else {
	            	scale = (int)(options.outHeight / (float)widthOrHeight);
	            }
            } else {
            	if (DEBUG) Log.d(TAG, "use org");
            }
            if (scale <= 0)
            	scale = 1;
            options.inSampleSize = scale;
            //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
           
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        }  catch (OutOfMemoryError e) {
        	Log.e("decodeFile出错:[池负载："+ImageUtil.poolLoad+"]","--->",e);
        	if (bitmap != null) {
        		bitmap.recycle();
        	}
        } catch (Exception e) {
        	Log.e("decodeFile出错:[池负载："+ImageUtil.poolLoad+"]","--->",e);
        	if (bitmap != null) {
        		bitmap.recycle();
        	}
        }
        
        return bitmap;
    }
    
	/**
	 * 缩略图生成
	 * @param source 源
	 * @param width  宽
	 * @param height 高
	 * @return
	 */
	public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
		return extractThumbnail(source, width, height, OPTIONS_NONE);
	}
    
    /**
     * 缩略图生成
     * @param source
     * @param width
     * @param height
     * @param options
     * @return
     */
    private static Bitmap extractThumbnail(
    								Bitmap source, 
    								int width, 
    								int height,
    								int options) {
		if (source == null) {
			return null;
		}
		float scale = height / (float) source.getHeight();
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap thumbnail = transform(matrix, source, width, height,
				OPTIONS_SCALE_UP | options);
		return thumbnail;
	}
    
    
    /**
     * 将目标图片转换到指定的宽度和高度
     * @param scaler
     * @param source
     * @param targetWidth
     * @param targetHeight
     * @param options
     * @return
     */
	private static Bitmap transform(
							Matrix scaler, 
							Bitmap source,
							int targetWidth, 
							int targetHeight, 
							int options) {
		//是否缩放
		boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
		//是否回收
		boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			if (DEBUG) Log.d(TAG, "width:" + source.getWidth() + ", height:" + source.getHeight());
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source
					.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		if (recycle && b1 != source) {
			source.recycle();
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);

		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}

		return b2;
	}
	
	/**
	 * dip to px
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static float getPX(Context context, int dipValue){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
	}
	
	/**
	 * px to dip
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static float px2dip(Context context, float pxValue){
		float scale = context.getResources().getDisplayMetrics().density;
		return pxValue / scale + 0.5f;
    }
	
	/**
	 * Bitmap转为Drawable
	 */
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}

	/**
	 * Drawable转为Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, width, height);
			drawable.draw(canvas);
			return bitmap;
		} else {
			return null;
		}
	}
	
	public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.setScale(scaleWidth, scaleHeight);
		Bitmap thumbnail = transform(matrix, bm, width, height,
				OPTIONS_SCALE_UP);
//		matrix.postScale(scaleWidth, scaleHeight);
//		// 得到新的图片
//		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		return thumbnail;
	}
	
	public Bitmap getBitmapFromSD(String url, boolean isScaleByWidth, int widthOrHeight) { 
		Bitmap bitmap = null;
    	try {
			
			//从sdcard中获得bitmap对象
			String filename = String.valueOf(url.hashCode());
			File f = new File(cacheDir, filename);
			//from SD cache
			if (f.exists()) {

				bitmap = decodeFile(url, f, isScaleByWidth, widthOrHeight);
				if (DEBUG)
					Log.d(TAG, "从本地读取! the bitmap=" + bitmap);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return bitmap;
		}
	}
}
