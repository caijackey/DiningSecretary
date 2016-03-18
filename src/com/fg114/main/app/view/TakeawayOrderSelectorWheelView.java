package com.fg114.main.app.view;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.adapter.NumericStepWheelAdapter;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.SessionManager;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TakeawayOrderSelectorWheelView extends LinearLayout{

	WheelView timeWheel;

	private NumericStepWheelAdapter timeAdapter;
    private List<CommonTypeDTO> timeTypeDTO;
		

	public TakeawayOrderSelectorWheelView(Context context) {
		this(context, null);
	}
	public TakeawayOrderSelectorWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.HORIZONTAL);
		initWheels();
	}
	private void initWheels() {
		LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,LayoutParams.FILL_PARENT,1);
		timeWheel = new WheelView(getContext());

		//--
		timeWheel.setCyclic(false);

		//--
		this.addView(timeWheel,params);

		
//		dateWheel.setOnTouchListener(new ScrollEventListener("滚动日期"));
//		timeWheel.setOnTouchListener(new ScrollEventListener("滚动时间"));

		
//		Log.d("++++++++++orderSelInfo控件里",""+orderSelInfo.getMaxPeopleNum());
//		initData(timeTypeDTO);
	}
	
	private static class ScrollEventListener implements OnTouchListener{
		
		public String eventName="";
		public GestureDetector ges=new GestureDetector(new GestureDetector.OnGestureListener() {
			
			@Override
			public boolean onSingleTapUp(MotionEvent motionevent) {
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent motionevent) {
				
			}
			
			@Override
			public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1) {
				if(CheckUtil.isEmpty(eventName)||!scrollOpen){
					return false;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent(eventName);
				// -----
				scrollOpen=false;
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent motionevent) {
				
			}
			
			@Override
			public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1) {
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent motionevent) {
				return false;
			}
		});
		public ScrollEventListener(String eventName){
			this.eventName=eventName;
		}
		volatile boolean scrollOpen=false;
		@Override
		public boolean onTouch(View view, MotionEvent motionevent) {
			if(motionevent.getAction()==MotionEvent.ACTION_DOWN){
				scrollOpen=true;
			}
			if(motionevent.getAction()==MotionEvent.ACTION_UP||motionevent.getAction()==MotionEvent.ACTION_CANCEL){
				scrollOpen=false;
			}
			ges.onTouchEvent(motionevent);
			return false;
		}
	}
	
	public void initData(final List<CommonTypeDTO> timeTypeDTO) {
		
//		Log.e("OrderSelInfo=-=",orderSelInfo.getMaxDayNum()+","+orderSelInfo.getMaxPeopleNum());

		//时间数据----------------------------------------------------
		//本地默认值 11:00 ~ 20:30
		timeAdapter=new NumericStepWheelAdapter(getContext(),
				0,
				timeTypeDTO.size()-1,
				1,
				new NumericStepWheelAdapter.Facade() {
					@Override
					public String onGetItemText(long value) {
						return timeTypeDTO.get((int)value).getName();
					}
				});
		timeWheel.setViewAdapter(timeAdapter);
		
		//初始化选择
		timeWheel.setCurrentItem(0);
	}
	public long getCurrentItemValue(){
		return timeWheel.getCurrentItemValue();
	}
	public void setCurrentItemByValue(long value){
		timeWheel.setCurrentItemByValue(value);
	}
	
}
