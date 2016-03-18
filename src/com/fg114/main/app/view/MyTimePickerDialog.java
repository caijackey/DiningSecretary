package com.fg114.main.app.view;


import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

/**
 * 时间选取器，限制分钟数只能是１５的倍数
 * @author xujianjun, 2012-09-04
 *
 */
public class MyTimePickerDialog extends TimePickerDialog {
	private int currentMin;
	private int currentHour;
	private boolean isManual=false;
	private boolean is24HourView=false;


	public MyTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
		super(context, callBack, hourOfDay, minute, is24HourView);
		this.currentMin=minute;
		this.currentHour=hourOfDay;
		this.is24HourView=is24HourView;
		this.setTitle("设置时间");
		this.setCancelable(true);
	}

	public MyTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
		super(context, theme, callBack, hourOfDay, minute, is24HourView);
		this.currentMin=minute;
		this.currentHour=hourOfDay;
		this.is24HourView=is24HourView;
		this.setTitle("设置时间");
		this.setCancelable(true);
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		if(!isManual&&minute==currentMin){
			currentHour=hourOfDay;
			return;
		}
		if(isManual&&!(hourOfDay==currentHour&&minute==currentMin)){
			return;
		}else if(hourOfDay==currentHour&&minute==currentMin){
			isManual=false;
			return;
		}
		
		setValidMin(hourOfDay,minute);		
	}

	private void setValidMin(int hour, int newMin){
		boolean isPlus=(newMin-currentMin>0&&!(newMin==59&&currentMin==0))||newMin==0&&currentMin==59;
		int min=isPlus?(newMin+14)%60/15*15:newMin/15*15;
		currentHour=(isPlus && min==0?(hour+1)%(is24HourView?24:12):hour);
//		if(currentHour==hour){
//			currentHour=(!isPlus && min==45 && currentMin==0?(hour+((is24HourView?23:11)))%(is24HourView?24:12):currentHour);
//		}
		currentMin=min;
		isManual=true;
		updateTime(currentHour,currentMin);
	}
	@Override
	public void setView(View view) {
		disableInput(view);
		super.setView(view);
		
	}
	private void disableInput(View v){
		if(v instanceof EditText){
			v.setFocusable(false);
			v.setFocusableInTouchMode(false);
			v.setOnTouchListener(new OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});			
			return;
		}

		if(v instanceof ViewGroup){
			int count=((ViewGroup) v).getChildCount();
			for(int i=0;i<count;i++){
				disableInput(((ViewGroup) v).getChildAt(i));
			}
		}
	}


}
