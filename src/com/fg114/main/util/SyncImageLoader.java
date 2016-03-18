package com.fg114.main.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import com.fg114.main.cache.FileCacheUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class SyncImageLoader {

	private Object lock = new Object();
	
	private boolean mAllowLoad = true;
	
	private boolean firstLoad = true;
	
	private int mStartLoadLimit = 0;
	
	private int mStopLoadLimit = 0;
	
	final Handler handler = new Handler();
	
//	private HashMap<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();   
	
	private static Context context;
	
	public SyncImageLoader(Context ctx) {
		context = ctx;
	}
	
	public interface OnImageLoadListener {
		public void onImageLoad(Integer t, Drawable drawable, View view);
		public void onError(Integer t, View view);
	}
	
	public void setLoadLimit(int startLoadLimit,int stopLoadLimit){
		if(startLoadLimit > stopLoadLimit){
			return;
		}
		mStartLoadLimit = startLoadLimit;
		mStopLoadLimit = stopLoadLimit;
	}
	
	public void restore(){
		mAllowLoad = true;
		firstLoad = true;
	}
		
	public void lock(){
		synchronized (lock) {
			mAllowLoad = false;
			firstLoad = false;
			lock.notifyAll();
		}
	}
	
	public void unlock(){
		
		synchronized (lock) {
			mAllowLoad = true;
			lock.notifyAll();
		}
	}

	public void loadImage(Integer t, String imageUrl,
			OnImageLoadListener listener, final View view) {
		final OnImageLoadListener mListener = listener;
		final String mImageUrl = imageUrl;
		final Integer mt = t;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(!mAllowLoad){
//					DebugUtil.debug("prepare to load");
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				long t=System.currentTimeMillis();
				if(mAllowLoad && firstLoad){
					loadImage(mImageUrl, mt, mListener, view);
					Log.i("111111 "+t,"mAllowLoad="+mAllowLoad+",firstLoad="+firstLoad);
				}
				
				if(mAllowLoad && mt <= mStopLoadLimit && mt >= mStartLoadLimit){
					loadImage(mImageUrl, mt, mListener, view);
					Log.i("222222 "+t,"mAllowLoad="+mAllowLoad+",firstLoad="+firstLoad+"mt="+mt+",mStopLoadLimit="+mStopLoadLimit+",mStartLoadLimit="+mStartLoadLimit);
				}
			}

		}).start();
	}
	
	private void loadImage(final String mImageUrl,final Integer mt,final OnImageLoadListener mListener, final View view){
		
//		if (imageCache.containsKey(mImageUrl)) {  
//            SoftReference<Drawable> softReference = imageCache.get(mImageUrl);  
//            final Drawable d = softReference.get();  
//            if (d != null) {  
//            	handler.post(new Runnable() {
//    				@Override
//    				public void run() {
//    					if(mAllowLoad){
//    						mListener.onImageLoad(mt, d, view);
//    					}
//    				}
//    			});
//                return;  
//            }  
//        }  
		try {
			final Drawable d = loadImageFromUrl(mImageUrl);
//			if(d != null){
//                imageCache.put(mImageUrl, new SoftReference<Drawable>(d));
//			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(mAllowLoad){
						mListener.onImageLoad(mt, d, view);
					}
				}
			});
		} catch (Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mListener.onError(mt, view);
				}
			});
			e.printStackTrace();
		}
	}

	public static Drawable loadImageFromUrl(String url) throws IOException {
		//return new BitmapDrawable(ImageUtil.getInstance(context).getBitmap(url, false, 0));
		Bitmap bmp = FileCacheUtil.getInstance().get("MyImageView", url).getContentAsBitmap();
		if (bmp != null) {
			return new BitmapDrawable(bmp);
		}
		return null;
////		DebugUtil.debug(url);
//		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//			File f = new File(Environment.getExternalStorageDirectory()+"/TestSyncListView/"+url.hashCode());
//			if(f.exists()){
//				FileInputStream fis = new FileInputStream(f);
//				Drawable d = Drawable.createFromStream(fis, "src");
//				return d;
//			}
//			URL m = new URL(url);
//			InputStream i = (InputStream) m.getContent();
//			DataInputStream in = new DataInputStream(i);
//			FileOutputStream out = new FileOutputStream(f);
//			byte[] buffer = new byte[1024];
//			int   byteread=0;
//			while ((byteread = in.read(buffer)) != -1) {
//				out.write(buffer, 0, byteread);
//			}
//			in.close();
//			out.close();
//			Drawable d = Drawable.createFromStream(i, url);
//			return loadImageFromUrl(url);
//		}else{
//			URL m = new URL(url);
//			InputStream i = (InputStream) m.getContent();
//			Drawable d = Drawable.createFromStream(i, url);
//			return d;
//		}
//		
	}
}

