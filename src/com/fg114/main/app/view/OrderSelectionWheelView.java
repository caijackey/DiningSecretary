package com.fg114.main.app.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.adapter.NumericStepWheelAdapter;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.service.dto.RoomTypeInfoData;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.SessionManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 选择订单信息的滚轮控件，封装了4个滚轮
 * 
 * @author xujianjun,2013-07-23
 * 
 */
public class OrderSelectionWheelView extends LinearLayout {
	WheelView dateWheel;
	WheelView timeWheel;
	WheelView peopleWheel;
	WheelView roomTypeWheel;
	private NumericStepWheelAdapter dateAdapter;
	private NumericStepWheelAdapter timeAdapter;
	private NumericStepWheelAdapter peopleAdapter;
	private NumericStepWheelAdapter roomTypeAdapter;
	private RoomTypeInfoData roomTypeInfoData;
	private OrderSelInfo orderSelInfo;

	private String[] weekday = new String[] { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

	public OrderSelectionWheelView(Context context) {
		this(context, null);
	}

	public OrderSelectionWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.HORIZONTAL);
//		if (roomTypeInfoData == null) {
//			this.roomTypeInfoData = new RoomTypeInfoData();
//		} else {
//			this.roomTypeInfoData = roomTypeInfoData;
//		}
		initWheels();
	}

//	public OrderSelectionWheelView(Context context,RoomTypeInfoData roomTypeInfoData){
//		this(context, null);
//	}
	
	
	private void initWheels() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, 1);
		dateWheel = new WheelView(getContext());
		timeWheel = new WheelView(getContext());
		peopleWheel = new WheelView(getContext());
		roomTypeWheel = new WheelView(getContext());
		// --
		dateWheel.setCyclic(false);
		timeWheel.setCyclic(false);
		peopleWheel.setCyclic(false);
		roomTypeWheel.setCyclic(false);
		// --
		this.addView(dateWheel, params);
		this.addView(timeWheel, params);
		this.addView(peopleWheel, params);
		this.addView(roomTypeWheel, params);

		dateWheel.setOnTouchListener(new ScrollEventListener("滚动日期"));
		timeWheel.setOnTouchListener(new ScrollEventListener("滚动时间"));
		peopleWheel.setOnTouchListener(new ScrollEventListener("滚动人数"));
		roomTypeWheel.setOnTouchListener(new ScrollEventListener("滚动房间"));

		// 获得范围数据
		orderSelInfo = SessionManager.getInstance().getOrderSelInfo();
		// Log.d("++++++++++orderSelInfo控件里",""+orderSelInfo.getMaxPeopleNum());

        roomTypeInfoData = new RoomTypeInfoData();
//		this.roomTypeInfoData = new RoomTypeInfoData();
		initData(orderSelInfo,roomTypeInfoData);
		// initData(orderSelInfo);
	}

	private static class ScrollEventListener implements OnTouchListener {

		public String eventName = "";
		public GestureDetector ges = new GestureDetector(new GestureDetector.OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent motionevent) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent motionevent) {

			}

			@Override
			public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1) {
				if (CheckUtil.isEmpty(eventName) || !scrollOpen) {
					return false;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent(eventName);
				// -----
				scrollOpen = false;
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

		public ScrollEventListener(String eventName) {
			this.eventName = eventName;
		}

		volatile boolean scrollOpen = false;

		@Override
		public boolean onTouch(View view, MotionEvent motionevent) {
			if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {
				scrollOpen = true;
			}
			if (motionevent.getAction() == MotionEvent.ACTION_UP || motionevent.getAction() == MotionEvent.ACTION_CANCEL) {
				scrollOpen = false;
			}
			ges.onTouchEvent(motionevent);
			return false;
		}
	}

	public void initData(OrderSelInfo orderSelInfo,RoomTypeInfoData roomTypeInfoData) {
		// public void initData(OrderSelInfo orderSelInfo) {

		// Log.e("OrderSelInfo=-=",orderSelInfo.getMaxDayNum()+","+orderSelInfo.getMaxPeopleNum());
		// 日期数据----------------------------------------------------
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		// long milis=(now.getTimeInMillis()/86400000)*86400*1000;

		now.set(Calendar.HOUR_OF_DAY, 8);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.MILLISECOND, 0);
		long milis = now.getTimeInMillis();
		dateAdapter = new NumericStepWheelAdapter(getContext(), milis, milis + (orderSelInfo.getMaxDayNum() - 1) * 86400L * 1000, 86400L * 1000, new NumericStepWheelAdapter.Facade() {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));

			@Override
			public CharSequence onGetItemText(long value) {
				c.setTimeInMillis(value);
				boolean isToday = CalendarUtil.isToday(value);
				boolean isWeekEnd = (c.get(Calendar.DAY_OF_WEEK) == 1 || c.get(Calendar.DAY_OF_WEEK) == 7);
				return Html.fromHtml((isToday ? "<big><font color=\"#333333\">今天</font></big>" : String.format("%02d月%02d日", c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)))
						+ "<br><small><font color=\"" + (isWeekEnd ? "#000000" : "#AAAAAA") + "\">" + weekday[c.get(Calendar.DAY_OF_WEEK) - 1] + "</font></small>");
			}
		});
		dateAdapter.setTextGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		dateWheel.setViewAdapter(dateAdapter);
		// 时间数据----------------------------------------------------
		// 本地默认值 11:00 ~ 20:30
		timeAdapter = new NumericStepWheelAdapter(getContext(), 60 * 60 * 1000 * 11 - 8 * 60 * 60 * 1000, 30 * 60 * 1000 * 41 - 8 * 60 * 60 * 1000, 60 * 1000 * 15,
				new NumericStepWheelAdapter.Facade() {
					Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
					{
						c.clear();
					}

					@Override
					public String onGetItemText(long value) {
						c.setTimeInMillis(value);
						return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
					}
				});
		timeWheel.setViewAdapter(timeAdapter);

		// 人数数据----------------------------------------------------
		peopleAdapter = new NumericStepWheelAdapter(getContext(), 1, orderSelInfo.getMaxPeopleNum(), 1, new NumericStepWheelAdapter.Facade() {

			@Override
			public String onGetItemText(long value) {
				return value + "人";
			}
		});
		peopleWheel.setViewAdapter(peopleAdapter);

		// 房间数据----------------------------------------------------
		// 本地默认值 0:只订大厅 1：只订包房 2：优先大厅 3：优先包房
		final String[] room = new String[4];
		int roomCount = 0;
		if (roomTypeInfoData.onlyHallTag) {
			room[roomCount] = "大厅";
			roomCount++;
		}
		if (roomTypeInfoData.onlyRoomTag) {
			room[roomCount] = "包房";
			roomCount++;
		}
		if (roomTypeInfoData.firstHallTag) {
			room[roomCount] = "先大厅";
			roomCount++;
		}
		if (roomTypeInfoData.firstRoomTag) {
			room[roomCount] = "先包房";
		}
		roomTypeAdapter = new NumericStepWheelAdapter(getContext(), 0, roomCount, 1, new NumericStepWheelAdapter.Facade() {

			@Override
			public String onGetItemText(long value) {
				if ((int) value == 0) {
					return room[0];
				} else if ((int) value == 1) {
					return room[1];
				} else if ((int) value == 2) {
					return room[2];
				} else if ((int) value == 3) {
					return room[3];
				} else {
					return room[0];
				}
			}
		});
		roomTypeWheel.setViewAdapter(roomTypeAdapter);
		// 初始化选择
		initSelection();
	}

	private void initSelection() {
		//
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		Calendar time1800 = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		Calendar time2015 = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));

		time1800.set(Calendar.HOUR_OF_DAY, 18);
		time1800.set(Calendar.MINUTE, 0);
		time1800.set(Calendar.SECOND, 0);
		time1800.set(Calendar.MILLISECOND, 0);

		time2015.set(Calendar.HOUR_OF_DAY, 20);
		time2015.set(Calendar.MINUTE, 15);
		time2015.set(Calendar.SECOND, 0);
		time2015.set(Calendar.MILLISECOND, 0);

		// 默认选择 <18:00 为 18:15 >=18:00 为当前时间+15分钟 补上最近刻钟的分钟 如果超出20:30 就设置为下一天 比如
		// 18:35 为 19:00
		if (now.before(time1800)) {
			dateWheel.setCurrentItem(0);
			time1800.add(Calendar.MINUTE, 15);
			// time1800.add(Calendar.HOUR, 8);
			timeWheel.setCurrentItem(timeWheel.getViewAdapter().getIndexByValue(time1800.getTimeInMillis() % 86400000));
		} else if (now.after(time2015)) {
			dateWheel.setCurrentItem(1);
			time1800.add(Calendar.MINUTE, 15);
			// time1800.add(Calendar.HOUR, 8);
			timeWheel.setCurrentItem(timeWheel.getViewAdapter().getIndexByValue(time1800.getTimeInMillis() % 86400000));
		} else {
			now.add(Calendar.MINUTE, 15);
			// now.add(Calendar.HOUR, 8);
			timeWheel.setCurrentItem(timeWheel.getViewAdapter().getIndexByValue(now.getTimeInMillis() % 86400000));
		}
		if (peopleAdapter.getMaxValue() >= 4) {
			peopleWheel.setCurrentItem(3);// 默认选择4人
		} else {
			peopleWheel.setCurrentItem(0);
		}
		roomTypeWheel.setCurrentItem(0);
	}

	// 数据获取和设置方法----------------------------
	public long getDateMilliSeconds() {
		return dateWheel.getCurrentItemValue();
	}

	public void setDateMilliSeconds(long value) {
		dateWheel.setCurrentItemByValue(value);
	}

	public long getTimeMilliSeconds() {
		return timeWheel.getCurrentItemValue();
	}

	public void setTimeMilliSeconds(long value) {
		timeWheel.setCurrentItemByValue(value);
	}

	public long getPeopleNum() {
		return peopleWheel.getCurrentItemValue();
	}

	public void setPeopleNum(long value) {
		peopleWheel.setCurrentItemByValue(value);
	}

	public long getRoomType() {
		return roomTypeWheel.getCurrentItemValue();
	}

	public void setRoomType(long value) {
		roomTypeWheel.setCurrentItemByValue(value);
	}

	public void setRoomTypeInfoData(RoomTypeInfoData roomTypeInfoData){
		this.roomTypeInfoData=roomTypeInfoData;
		initData(orderSelInfo,roomTypeInfoData);
	}
}
