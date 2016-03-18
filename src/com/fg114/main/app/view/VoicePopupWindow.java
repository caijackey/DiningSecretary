package com.fg114.main.app.view;

import com.fg114.main.R;
import com.fg114.main.app.Settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class VoicePopupWindow
{

	private PopupWindow window;
	private LayoutInflater mInflater;
	private View anchor;
	private View mVoiceContentView;
	private View mCanCelContentView;
	private ImageView mVoiceView_1;
	private LinearLayout mCancel;
	private TextView mCancelTv;
	private ImageView mCancelImage;
	private ImageView mVoiceView_2;
	private Context mContent;
	private int mFlag = 0;
	private PopupWindow.OnDismissListener mDismissListener;
	private AnimationDrawable mAnimation,mAnimation_2;

	public VoicePopupWindow(Context context, View anchor)
	{
		mInflater = LayoutInflater.from(context);
		this.anchor = anchor;
		this.mContent = context;
		initPopupWindow();
	}
	// 初始化自定义的弹出窗口
	private void initPopupWindow()
	{
		View mainContent = mInflater.inflate(R.layout.pop_voice_item, null);
		mVoiceContentView = mainContent.findViewById(R.id.voice_content_view_1);
		mCanCelContentView = mainContent.findViewById(R.id.voice_content_view_2);
		mVoiceView_1 = (ImageView) mainContent.findViewById(R.id.voice);
		mCancel = (LinearLayout) mainContent.findViewById(R.id.voice_cancle);
		mCancelTv=(TextView) mainContent.findViewById(R.id.voice_cancel_tv);
		mCancelImage=(ImageView) mainContent.findViewById(R.id.voice_cancel_iv);
		mVoiceView_2=(ImageView) mainContent.findViewById(R.id.voice_image_low_high_2);
		if (anchor == null) {
			anchor = ((Activity) mContent).getWindow().getDecorView();
		}
		
		// 弹出层显示的内容
		window = new PopupWindow(mainContent, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		window.setBackgroundDrawable(new BitmapDrawable());
		window.setOutsideTouchable(true);
		window.setFocusable(true);

		window.setClippingEnabled(true);

		window.setOnDismissListener(new PopupWindow.OnDismissListener()
		{
			@Override
			public void onDismiss()
			{

				if (mDismissListener != null) {
					mDismissListener.onDismiss();
				}
			}
		});
		mVoiceView_1.setBackgroundResource(R.anim.voice_low_high);
		mVoiceView_2.setBackgroundResource(R.anim.voice_low_high);
		mAnimation = (AnimationDrawable) mVoiceView_1.getBackground();
		mAnimation_2=(AnimationDrawable) mVoiceView_2.getBackground();
		mAnimation.setOneShot(false);
		mAnimation_2.setOneShot(false);
		

	}
	
	
	public void showPopupWindow()
	{
		mVoiceContentView.setVisibility(View.VISIBLE);
		mCanCelContentView.setVisibility(View.INVISIBLE);
		mFlag = Settings.VOICE_CONTENT_STYLE_01;
		window.showAtLocation(anchor, Gravity.CENTER | Gravity.CENTER, 0, 0);
		//开始播放动画
		mVoiceView_1.post(new Runnable()  
		  
		 {  
		  
		            @Override  
		  
		            public void run()  
		  
		            {  
		  
		            	if (mAnimation.isRunning())// 是否正在运行？
		    			{
		    				mAnimation.stop();// 停止
		    			}
		            	mAnimation.start();  
		  
		            }  
		  
		        });  
		mVoiceView_2.post(new Runnable()
		{
			
			@Override
			public void run()
			{

            	if (mAnimation_2.isRunning())// 是否正在运行？
    			{
    				mAnimation_2.stop();// 停止
    			}
            	mAnimation_2.start();  
  
				
			}
		});

	}
	public boolean isPopShowing()
	{
		return window.isShowing();

	}

	public void switchContentView(int Flag)
	{
		if (Flag == Settings.VOICE_CONTENT_STYLE_01) {
			mFlag = Flag;
			mVoiceContentView.setVisibility(View.VISIBLE);
			mCanCelContentView.setVisibility(View.INVISIBLE);
		
		} else {
			mFlag = Flag;
			mVoiceContentView.setVisibility(View.INVISIBLE);
			mCanCelContentView.setVisibility(View.VISIBLE);
			

		}
	}
	
	public boolean isContains(int x, int y)
	{

		Rect rect = new Rect(mCancel.getLeft(), mCancel.getTop(), mCancel.getRight(), mCancel.getBottom());
		if (rect.contains(x, y)) {

			return true;
		}
		return false;

	}
	public synchronized final int getmFlag()
	{
		return mFlag;
	}
	public void disMissPop()
	{
		if (window != null && window.isShowing()) {
			window.dismiss();
			
		}
	}
	public synchronized final LinearLayout getmCancel()
	{
		return mCancel;
	}
	
	public synchronized final TextView getmCancelTv()
	{
		return mCancelTv;
	}
	public synchronized final PopupWindow.OnDismissListener getmDismissListener()
	{
		return mDismissListener;
	}
	public synchronized final void setmDismissListener(PopupWindow.OnDismissListener mDismissListener)
	{
		this.mDismissListener = mDismissListener;
	}
	

}
