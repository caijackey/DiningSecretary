package com.fg114.main.app.view;

import com.fg114.main.R;
import com.fg114.main.service.dto.MealComboData;
import com.fg114.main.util.ViewUtils;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class TimeSwitcher extends LinearLayout implements ViewSwitcher.ViewFactory
{

	private LayoutInflater mlayoutinf;
	private Context mContext;
	// 页面组件
	private ViewGroup mMainLayout;
	private TextView mRestInfoTv;
	private TextSwitcher mHourSwitcher_top;
	private TextSwitcher mHourSwitcher_1;
	private TextSwitcher mHourSwitcher_2;
	private TextSwitcher mMinuteSwitcher_1;
	private TextSwitcher mMinuteSwitcher_2;
	private TextSwitcher mSecondSwitcher_1;
	private TextSwitcher mSecondSwitcher_2;
	private ImageView X_imageView;
    private OnStateFinishListener OnStateFinishListener;
    private OnCloseListener OnCloseListener;
	private long remainSeconds;// 剩余总的时间、秒为单位
	private int[] hour = new int[]{-1, -1, -1};
	private int[] minute = new int[]{-1, -1};
	private int[] second = new int[]{-1, -1};
	private Thread timer;
	private boolean isGone=false;

	public TimeSwitcher(Context context)
	{
		super(context);
		mlayoutinf = LayoutInflater.from(context);
		mContext = context;
		initComponent();

	}
	public TimeSwitcher(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		mlayoutinf = LayoutInflater.from(context);
		initComponent();
	}
	public void initComponent()
	{

		View contentView = mlayoutinf.inflate(R.layout.time_switcher_item, null);
		mMainLayout = (ViewGroup) contentView.findViewById(R.id.time_switcher_mian_layout);
		mRestInfoTv = (TextView) contentView.findViewById(R.id.time_switcher_tv);
		mHourSwitcher_top = (TextSwitcher) contentView.findViewById(R.id.time_switcher_hour_NO_top);
		mHourSwitcher_1 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_hour_NO1);
		mHourSwitcher_2 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_hour_NO2);
		mMinuteSwitcher_1 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_minute_NO1);
		mMinuteSwitcher_2 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_minute_NO2);
		mSecondSwitcher_1 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_second_NO1);
		mSecondSwitcher_2 = (TextSwitcher) contentView.findViewById(R.id.time_switcher_second_NO2);
		X_imageView = (ImageView) contentView.findViewById(R.id.time_switcher_X_btn);
		X_imageView.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 1000);
				setVisibility(View.GONE);
				pause();
				isGone=true;
			}
		});
		String restinfo=mContext.getString(R.string.text_time_switcher_rest_info);
		ViewUtils.setSpan(mRestInfoTv, restinfo, 0, 2, Color.RED);
		//ViewUtils.setSpan(mRestInfoTv, restinfo, 2, restinfo.length(), Color.GREEN);
		initSwitcher();
		addView(contentView);

	}
	
	
	// 启动倒计时
	public void start()
	{
		if(!isGone)
		{
		  setRemainderTime(360011);
		}
		
	}
	
	public synchronized final boolean isGone()
	{
		return isGone;
	}
	public synchronized final void setGone(boolean isGone)
	{
		this.isGone = isGone;
	}
	// 暂停
	public void pause()
	{
		
		if (timer != null) {
			timer.interrupt();
		}
	}
	// 取消倒计时
	public void cancel()
	{

	}
	public void initSwitcher()
	{
		Animation in = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_bottom);
		Animation out = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_top);
		mHourSwitcher_top.setInAnimation(in);
		mHourSwitcher_top.setOutAnimation(out);
		mHourSwitcher_1.setInAnimation(in);
		mHourSwitcher_1.setOutAnimation(out);
		mHourSwitcher_2.setInAnimation(in);
		mHourSwitcher_2.setOutAnimation(out);

		mMinuteSwitcher_1.setInAnimation(in);
		mMinuteSwitcher_1.setOutAnimation(out);
		mMinuteSwitcher_2.setInAnimation(in);
		mMinuteSwitcher_2.setOutAnimation(out);

		mSecondSwitcher_1.setInAnimation(in);
		mSecondSwitcher_1.setOutAnimation(out);
		mSecondSwitcher_2.setInAnimation(in);
		mSecondSwitcher_2.setOutAnimation(out);

		mHourSwitcher_top.setFactory(this);
		mHourSwitcher_1.setFactory(this);
		mHourSwitcher_2.setFactory(this);
		mMinuteSwitcher_1.setFactory(this);
		mMinuteSwitcher_2.setFactory(this);
		mSecondSwitcher_1.setFactory(this);
		mSecondSwitcher_2.setFactory(this);

	}
	@Override
	public View makeView()
	{
		TextView t = new TextView(mContext);
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		t.setTextColor(Color.WHITE);
		t.setTextSize(18);
		return t;
	}

	/**
	 * 获取此刻真实剩余时间(秒)
	 * 
	 * @param dto
	 * @return
	 */
	private long getRealRemainderTime(MealComboData dto)
	{
		if (dto.getTimestamp() > 0) {
			return dto.getRemainSeconds() - (SystemClock.elapsedRealtime() - dto.getTimestamp()) / 1000;
		} else {
			return dto.getRemainSeconds();
		}
	}

	private void getTimeFromSeconds(long seconds)
	{
		if (seconds < 0) {
			seconds = 0;
		}

		if (seconds / 3600 >= 0) {
			if (seconds / 3600 / 100 > 0) {
				mHourSwitcher_top.setVisibility(View.VISIBLE);
				int tempHour0 = (int) (seconds / 3600 / 100);
				if (tempHour0 != hour[0]) {
					mHourSwitcher_top.setText(String.valueOf(tempHour0));
					hour[0] = tempHour0;
				}
			} else {
				mHourSwitcher_top.setVisibility(View.INVISIBLE);
			}
			int tempHour1 = (int) (seconds / 3600 % 100 / 10);
			int tempHour2 = (int) (seconds / 3600 % 100 % 10);
			if (tempHour1 != hour[1]) {
				mHourSwitcher_1.setText(String.valueOf(tempHour1));
				hour[1] = tempHour1;
			}

			if (tempHour2 != hour[2]) {
				mHourSwitcher_2.setText(String.valueOf(tempHour2));
				hour[2] = tempHour2;
			}

		}

		if (seconds % 3600 / 60 >= 0) {
			int tempMinute0 = (int) (seconds % 3600 / 60 / 10);
			int tempMinute1 = (int) (seconds % 3600 / 60 % 10);

			if (tempMinute0 != minute[0]) {
				mMinuteSwitcher_1.setText(String.valueOf(tempMinute0));
				minute[0] = tempMinute0;
			}

			if (tempMinute1 != minute[1]) {
				mMinuteSwitcher_2.setText(String.valueOf(tempMinute1));
				minute[1] = tempMinute1;
			}

		}

		if (seconds % 3600 % 60 >= 0) {

			int tempSecond0 = (int) (seconds % 3600 % 60 / 10);
			int tempSecond1 = (int) (seconds % 3600 % 60 % 10);

			if (tempSecond0 != second[0]) {
				mSecondSwitcher_1.setText(String.valueOf(tempSecond0));
				second[0] = tempSecond0;
			}

			if (tempSecond1 != second[1]) {
				mSecondSwitcher_2.setText(String.valueOf(tempSecond1));
				second[1] = tempSecond1;
			}

		}

	}
	// 开始计时
	private void setRemainderTime(final long remainSeconds)
	{
		pause();
		if (remainSeconds <= 0) {
			return;
		}
		// --
		timer = new Thread(new Runnable()
		{
			volatile long initSeconds = remainSeconds;

			@Override
			public void run()
			{
				try {
					while (true) {
						initSeconds--;
						if (initSeconds < 0) {
                            if(OnStateFinishListener!=null)
                            {
                            	OnStateFinishListener.onFinish();
                            }
							break;
						}
						((Activity) mContext).runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								// Log.e("remainderTime.setText","remainderTime.setText "+isFirst+" "+initSeconds);
								getTimeFromSeconds(initSeconds);
								if (initSeconds == 0) {

								}
							}
						});

						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			}

		});
		timer.start();
	}
	
	
	


	public synchronized final OnStateFinishListener getOnStateFinishListener()
	{
		return OnStateFinishListener;
	}
	public synchronized final void setOnStateFinishListener(OnStateFinishListener onStateFinishListener)
	{
		OnStateFinishListener = onStateFinishListener;
	}
	public synchronized final OnCloseListener getOnCloseListener()
	{
		return OnCloseListener;
	}
	public synchronized final void setOnCloseListener(OnCloseListener onCloseListener)
	{
		OnCloseListener = onCloseListener;
	}





	public interface OnStateFinishListener
	{
		public void onFinish();
	}
	public interface OnCloseListener
	{
		public void onClose();
	}
}
