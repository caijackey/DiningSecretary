package com.fg114.main.util;

import java.util.*;

import com.fg114.main.R;
import com.fg114.main.app.adapter.ArrayWheelAdapter;
import com.fg114.main.app.adapter.NumericWheelAdapter;
import com.fg114.main.app.view.CustomDialog;
import com.fg114.main.app.view.WheelView;

import android.app.*;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

/**
 * 时间选择的滚轮控件
 * @author wufucheng
 */
public class WheelDateTimePicker {

	private static final int BUTTON_MARGIN = 10;
	private static final String LABEL_YEAR = "年";
	private static final String LABEL_MONTH = "月";
	private static final String LABEL_DAY = "日";
	private static final int DEFAULT_START_YEAR = 1900; // 默认起始年
	private static final int DEFAULT_END_YEAR = 2100; // 默认结束年

	private static final String[] ARRAY_MONTHS_31 = { "1", "3", "5", "7", "8", "10", "12" };
	private static final String[] ARRAY_MONTHS_30 = { "4", "6", "9", "11" };
	private static final List<String> LIST_MONTHS_31 = Arrays.asList(ARRAY_MONTHS_31);
	private static final List<String> LIST_MONTHS_30 = Arrays.asList(ARRAY_MONTHS_30);

	private Calendar mStartDate; // 可选择的起始时间
	private Calendar mEndDate; // 可选择的结束时间
	private boolean mCyclic = false; // 滚轮是否循环

	public interface OnDateTimeSetListener {
		public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute);
	}

	public static WheelDateTimePicker create() {
		WheelDateTimePicker picker = new WheelDateTimePicker();
		return picker;
	}

	private WheelDateTimePicker() {
		mStartDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		mStartDate.set(DEFAULT_START_YEAR, 0, 1, 0, 0, 0);
		mEndDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		mEndDate.set(DEFAULT_END_YEAR, 11, 31, 23, 59, 59);
	}

	public Calendar getStartDate() {
		return mStartDate;
	}

	public void setStartDate(Calendar startDate) {
		mStartDate = startDate;
	}

	public Calendar getEndDate() {
		return mEndDate;
	}

	public void setEndDate(Calendar endDate) {
		mEndDate = endDate;
	}

	public boolean isCyclic() {
		return mCyclic;
	}

	public void setCyclic(boolean cyclic) {
		mCyclic = cyclic;
	}

	/**
	 * 显示日期选择框，包括年，月，日的设置(月份从1开始计算)
	 * @param activity
	 * @param currentYear
	 * @param currentMonth
	 * @param currentDay
	 * @param listener
	 */
	public void showDatePicker(final Activity activity, int currentYear, int currentMonth, int currentDay, final OnDateTimeSetListener listener) {
		try {
			if (mStartDate == null || mEndDate == null || mStartDate.after(mEndDate)) {
				return;
			}
			// 初始化各组数据
			final int startYear = mStartDate.get(Calendar.YEAR);
			final int endYear = mEndDate.get(Calendar.YEAR);
			// 年
			final WheelView wvYear = new WheelView(activity);
			wvYear.setCyclic(mCyclic);
			setYearAdapter(wvYear, startYear, endYear, currentYear);
			// 月
			final WheelView wvMonth = new WheelView(activity);
			wvMonth.setCyclic(mCyclic);
			setMonthAdapter(wvMonth, mStartDate, mEndDate, currentYear, currentMonth);
			// 日
			final WheelView wvDay = new WheelView(activity);
			wvDay.setCyclic(mCyclic);
			setDayAdapter(wvDay, mStartDate, mEndDate, currentYear, currentMonth, currentDay);

			// 添加"年"监听
			WheelView.OnWheelChangedListener wheelListenerYear = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"月"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					setMonthAdapter(wvMonth, mStartDate, mEndDate, selectedYear, selectedMonth);

					// 更新"日"选项的数据
					selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);
				}
			};
			// 添加"月"监听
			WheelView.OnWheelChangedListener wheelListenerMonth = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					// 更新"日"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);
				}
			};
			wvYear.addChangingListener(wheelListenerYear);
			wvMonth.addChangingListener(wheelListenerMonth);

			// 创建Dialog
			final CustomDialog dialog = createDialog(activity);
			// 设置Wheel的布局参数
			LinearLayout.LayoutParams paramsWheelYear = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
			wvYear.setLayoutParams(paramsWheelYear);
			LinearLayout.LayoutParams paramsWheelMonth = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvMonth.setLayoutParams(paramsWheelMonth);
			LinearLayout.LayoutParams paramsWheelDay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvDay.setLayoutParams(paramsWheelDay);
			// 创建Wheel布局
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN * 2));
			llWheel.addView(wvYear, paramsWheelYear);
			llWheel.addView(wvMonth, paramsWheelMonth);
			llWheel.addView(wvDay, paramsWheelDay);
			// 创建监听
			View.OnClickListener okListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if (listener != null) {
						int selectedYear = getSelectedValue(wvYear);
						int selectedMonth = getSelectedValue(wvMonth);
						int selectedDay = getSelectedValue(wvDay);
						listener.onDateTimeSet(selectedYear, selectedMonth, selectedDay, 0, 0);
					}
				}
			};
			View.OnClickListener cancelListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			};
			// 创建主布局
			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(createButtonLayout(activity, okListener, cancelListener));
			llMain.addView(llWheel);
			// 显示Dialog
			dialog.setContentView(llMain);
			dialog.show();
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 显示时间选择框，包括时和分的设置(24小时制)
	 * @param activity
	 * @param currentHour
	 * @param currentMinute
	 * @param listener
	 */
	public void showTimePicker(final Activity activity, int currentHour, int currentMinute, final OnDateTimeSetListener listener) {
		try {
			if (mStartDate == null || mEndDate == null || mStartDate.after(mEndDate)) {
				return;
			}
			// 初始化各组数据
			final int startHour = mStartDate.get(Calendar.HOUR_OF_DAY);
			final int endHour = mEndDate.get(Calendar.HOUR_OF_DAY);
			if (startHour > endHour) {
				return;
			}
			// 小时
			final WheelView wvHour = new WheelView(activity);
			wvHour.setCyclic(mCyclic);
			setHourAdapter(wvHour, startHour, endHour, currentHour);
			// 分钟
			final WheelView wvMinute = new WheelView(activity);
			wvMinute.setCyclic(mCyclic);
			setMinuteAdapter(wvMinute, mStartDate, mEndDate, currentHour, currentMinute);

			// 添加"小时"监听
			WheelView.OnWheelChangedListener wheelListenerYear = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					int selectedHour = getSelectedValue(wvHour);
					int selectedMinute = getSelectedValue(wvMinute);
					setMinuteAdapter(wvMinute, mStartDate, mEndDate, selectedHour, selectedMinute);
				}
			};
			wvHour.addChangingListener(wheelListenerYear);

			// 创建Dialog
			final CustomDialog dialog = createDialog(activity);
			// 设置Wheel的布局参数
			LinearLayout.LayoutParams paramsWheelHour = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvHour.setLayoutParams(paramsWheelHour);
			LinearLayout.LayoutParams paramsWheelMinute = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvMinute.setLayoutParams(paramsWheelMinute);
			// 创建Wheel布局
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN * 2));
			llWheel.addView(wvHour, paramsWheelHour);
			llWheel.addView(wvMinute, paramsWheelMinute);
			// 创建监听
			View.OnClickListener okListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if (listener != null) {
						int selectedHour = getSelectedValue(wvHour);
						int selectedMinute = getSelectedValue(wvMinute);
						listener.onDateTimeSet(0, 0, 0, selectedHour, selectedMinute);
					}
				}
			};
			View.OnClickListener cancelListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			};
			// 创建主布局
			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(createButtonLayout(activity, okListener, cancelListener));
			llMain.addView(llWheel);
			// 显示Dialog
			dialog.setContentView(llMain);
			dialog.show();
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 显示完整的日期选择框，包括年，月，日，时，分(月份从1开始计算)
	 * @param activity
	 * @param currentDate
	 * @param listener
	 */
	public void showDateTimePicker(final Activity activity, Calendar currentDate, final OnDateTimeSetListener listener) {
		try {
			if (mStartDate == null || mEndDate == null || mStartDate.after(mEndDate)) {
				return;
			}
			final int textSize = 18;
			final int visibleItems = 7;
			int currentYear = currentDate.get(Calendar.YEAR);
			int currentMonth = currentDate.get(Calendar.MONTH) + 1;
			int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
			int currentHour = currentDate.get(Calendar.HOUR_OF_DAY);
			int currentMinute = currentDate.get(Calendar.MINUTE);
			// 初始化各组数据
			final int startYear = mStartDate.get(Calendar.YEAR);
			final int endYear = mEndDate.get(Calendar.YEAR);
			// 年
			final WheelView wvYear = new WheelView(activity);
			wvYear.setCyclic(mCyclic);
			wvYear.setVisibleItems(visibleItems);
			setYearAdapter(wvYear, startYear, endYear, currentYear);
			// 月
			final WheelView wvMonth = new WheelView(activity);
			wvMonth.setCyclic(mCyclic);
			wvMonth.setVisibleItems(visibleItems);
			setMonthAdapter(wvMonth, mStartDate, mEndDate, currentYear, currentMonth);
			// 日
			final WheelView wvDay = new WheelView(activity);
			wvDay.setCyclic(mCyclic);
			wvDay.setVisibleItems(visibleItems);
			setDayAdapter(wvDay, mStartDate, mEndDate, currentYear, currentMonth, currentDay);
			// 小时
			final WheelView wvHour = new WheelView(activity);
			wvHour.setCyclic(mCyclic);
			wvHour.setVisibleItems(visibleItems);
			setHourAdapter(wvHour, mStartDate, mEndDate, currentYear, currentMonth, currentDay, currentHour);
			// 分钟
			final WheelView wvMinute = new WheelView(activity);
			wvMinute.setCyclic(mCyclic);
			wvMinute.setVisibleItems(visibleItems);
			setMinuteAdapter(wvMinute, mStartDate, mEndDate, currentYear, currentMonth, currentDay, currentHour, currentMinute);

			// 设置字体大小
			setTextSize(wvYear, textSize);
			setTextSize(wvMonth, textSize);
			setTextSize(wvDay, textSize);
			setTextSize(wvHour, textSize);
			setTextSize(wvMinute, textSize);

			// 添加"年"监听
			WheelView.OnWheelChangedListener wheelListenerYear = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"月"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					setMonthAdapter(wvMonth, mStartDate, mEndDate, selectedYear, selectedMonth);

					// 更新"日"选项的数据
					selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);

					// 更新"小时"选项的数据
					selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 更新"分钟"选项的数据
					selectedHour = getSelectedValue(wvHour);
					int selectedMinute = getSelectedValue(wvMinute);
					setMinuteAdapter(wvMinute, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
					setTextSize(wvMinute, textSize);
				}
			};
			// 添加"月"监听
			WheelView.OnWheelChangedListener wheelListenerMonth = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"日"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);

					// 更新"小时"选项的数据
					selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 更新"分钟"选项的数据
					selectedHour = getSelectedValue(wvHour);
					int selectedMinute = getSelectedValue(wvMinute);
					setMinuteAdapter(wvMinute, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
					setTextSize(wvMinute, textSize);
				}
			};
			// 添加"日"监听
			WheelView.OnWheelChangedListener wheelListenerDay = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"小时"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 更新"分钟"选项的数据
					selectedHour = getSelectedValue(wvHour);
					int selectedMinute = getSelectedValue(wvMinute);
					setMinuteAdapter(wvMinute, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
					setTextSize(wvMinute, textSize);
				}
			};
			// 添加"小时"监听
			WheelView.OnWheelChangedListener wheelListenerHour = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"分钟"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					int selectedMinute = getSelectedValue(wvMinute);
					setMinuteAdapter(wvMinute, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
					setTextSize(wvMinute, textSize);
				}
			};
			wvYear.addChangingListener(wheelListenerYear);
			wvMonth.addChangingListener(wheelListenerMonth);
			wvDay.addChangingListener(wheelListenerDay);
			wvHour.addChangingListener(wheelListenerHour);

			// 创建Dialog
			final CustomDialog dialog = createDialog(activity);
			// 设置Wheel的布局参数
			LinearLayout.LayoutParams paramsWheelYear = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
			wvYear.setLayoutParams(paramsWheelYear);
			LinearLayout.LayoutParams paramsWheelMonth = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvMonth.setLayoutParams(paramsWheelMonth);
			LinearLayout.LayoutParams paramsWheelDay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvDay.setLayoutParams(paramsWheelDay);
			LinearLayout.LayoutParams paramsWheelHour = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvHour.setLayoutParams(paramsWheelHour);
			LinearLayout.LayoutParams paramsWheelMinute = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvMinute.setLayoutParams(paramsWheelMinute);
			// 创建Wheel布局
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN * 2));
			llWheel.addView(wvYear, paramsWheelYear);
			llWheel.addView(wvMonth, paramsWheelMonth);
			llWheel.addView(wvDay, paramsWheelDay);
			llWheel.addView(wvHour, paramsWheelHour);
			llWheel.addView(wvMinute, paramsWheelMinute);
			// 创建监听
			View.OnClickListener okListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if (listener != null) {
						int selectedYear = getSelectedValue(wvYear);
						int selectedMonth = getSelectedValue(wvMonth);
						int selectedDay = getSelectedValue(wvDay);
						int selectedHour = getSelectedValue(wvHour);
						int selectedMinute = getSelectedValue(wvMinute);
						listener.onDateTimeSet(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
					}
				}
			};
			View.OnClickListener cancelListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			};
			// 创建主布局
			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(createButtonLayout(activity, okListener, cancelListener));
			llMain.addView(llWheel);
			// 显示Dialog
			dialog.setContentView(llMain);
			dialog.show();
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}
	
	public void showDateTimePickerForXiaomishu(final Activity activity, Calendar currentDate, final OnDateTimeSetListener listener) {
		try {
			if (mStartDate == null || mEndDate == null || mStartDate.after(mEndDate)) {
				return;
			}
			final int textSize = 17;
			final int visibleItems = 5;
			final String[] MINUTE_DATA = {"00", "15", "30", "45"};
			int currentYear = currentDate.get(Calendar.YEAR);
			int currentMonth = currentDate.get(Calendar.MONTH) + 1;
			int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
			int currentHour = currentDate.get(Calendar.HOUR_OF_DAY);
			int currentMinute = currentDate.get(Calendar.MINUTE);
			// 初始化各组数据
			final int startYear = mStartDate.get(Calendar.YEAR);
			final int endYear = mEndDate.get(Calendar.YEAR);
			// 年
			final WheelView wvYear = new WheelView(activity);
			wvYear.setCyclic(mCyclic);
			wvYear.setVisibleItems(visibleItems);
			setYearAdapter(wvYear, startYear, endYear, currentYear);
			// 月
			final WheelView wvMonth = new WheelView(activity);
			wvMonth.setCyclic(mCyclic);
			wvMonth.setVisibleItems(visibleItems);
			setMonthAdapter(wvMonth, mStartDate, mEndDate, currentYear, currentMonth);
			// 日
			final WheelView wvDay = new WheelView(activity);
			wvDay.setCyclic(mCyclic);
			wvDay.setVisibleItems(visibleItems);
			setDayAdapter(wvDay, mStartDate, mEndDate, currentYear, currentMonth, currentDay);
			// 小时
			final WheelView wvHour = new WheelView(activity);
			wvHour.setCyclic(mCyclic);
			wvHour.setVisibleItems(visibleItems);
			setHourAdapter(wvHour, mStartDate, mEndDate, currentYear, currentMonth, currentDay, currentHour);
			// 分钟
			final WheelView wvMinute = new WheelView(activity);
			wvMinute.setCyclic(mCyclic);
			wvMinute.setVisibleItems(visibleItems);
			ArrayWheelAdapter<String> minuteAdapter = new ArrayWheelAdapter<String>(activity, MINUTE_DATA);
			minuteAdapter.setTextSize(textSize);
			wvMinute.setViewAdapter(minuteAdapter);
			// 调整分钟数使得值为0,15,30,45
			if (currentMinute > 0 && currentMinute < 15) {
				currentMinute = 15;
			} else if (currentMinute > 15 && currentMinute < 30) {
				currentMinute = 30;
			} else if (currentMinute > 30 && currentMinute < 45) {
				currentMinute = 45;
			} else if (currentMinute > 45 && currentMinute <= 59) {
				currentMinute = 0;
			}
			for (int i=0; i<MINUTE_DATA.length; i++) {
				if (Integer.parseInt(MINUTE_DATA[i]) == currentMinute) {
					wvMinute.setCurrentItem(i);
					break;
				}
			}
//			setMinuteAdapter(wvMinute, mStartDate, mEndDate, currentYear, currentMonth, currentDay, currentHour, currentMinute);

			// 设置字体大小
			setTextSize(wvYear, textSize);
			setTextSize(wvMonth, textSize);
			setTextSize(wvDay, textSize);
			setTextSize(wvHour, textSize);

			// 添加"年"监听
			WheelView.OnWheelChangedListener wheelListenerYear = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"月"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					setMonthAdapter(wvMonth, mStartDate, mEndDate, selectedYear, selectedMonth);

					// 更新"日"选项的数据
					selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);

					// 更新"小时"选项的数据
					selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
				}
			};
			// 添加"月"监听
			WheelView.OnWheelChangedListener wheelListenerMonth = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"日"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					setDayAdapter(wvDay, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay);

					// 更新"小时"选项的数据
					selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
				}
			};
			// 添加"日"监听
			WheelView.OnWheelChangedListener wheelListenerDay = new WheelView.OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {

					// 更新"小时"选项的数据
					int selectedYear = getSelectedValue(wvYear);
					int selectedMonth = getSelectedValue(wvMonth);
					int selectedDay = getSelectedValue(wvDay);
					int selectedHour = getSelectedValue(wvHour);
					setHourAdapter(wvHour, mStartDate, mEndDate, selectedYear, selectedMonth, selectedDay, selectedHour);

					// 设置字体大小
					setTextSize(wvYear, textSize);
					setTextSize(wvMonth, textSize);
					setTextSize(wvDay, textSize);
					setTextSize(wvHour, textSize);
					setTextSize(wvMinute, textSize);
				}
			};
			wvYear.addChangingListener(wheelListenerYear);
			wvMonth.addChangingListener(wheelListenerMonth);
			wvDay.addChangingListener(wheelListenerDay);

			// 创建Dialog
			final CustomDialog dialog = createDialog(activity);
			// 设置Wheel的布局参数
			LinearLayout.LayoutParams paramsWheelYear = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
			wvYear.setLayoutParams(paramsWheelYear);
			LinearLayout.LayoutParams paramsWheelMonth = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f);
			wvMonth.setLayoutParams(paramsWheelMonth);
			LinearLayout.LayoutParams paramsWheelDay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f);
			wvDay.setLayoutParams(paramsWheelDay);
			LinearLayout.LayoutParams paramsWheelHour = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
			wvHour.setLayoutParams(paramsWheelHour);
			LinearLayout.LayoutParams paramsWheelMinute = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
			wvMinute.setLayoutParams(paramsWheelMinute);
			// 创建Wheel布局
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN * 2));
			llWheel.addView(wvYear, paramsWheelYear);
			llWheel.addView(wvMonth, paramsWheelMonth);
			llWheel.addView(wvDay, paramsWheelDay);
			llWheel.addView(wvHour, paramsWheelHour);
			llWheel.addView(wvMinute, paramsWheelMinute);
			// 创建监听
			View.OnClickListener okListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if (listener != null) {
						int selectedYear = getSelectedValue(wvYear);
						int selectedMonth = getSelectedValue(wvMonth);
						int selectedDay = getSelectedValue(wvDay);
						int selectedHour = getSelectedValue(wvHour);
						String minute = ((ArrayWheelAdapter<?>) wvMinute.getViewAdapter()).getItemText(wvMinute.getCurrentItem()).toString();
						int selectedMinute = Integer.parseInt(minute);
						listener.onDateTimeSet(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
					}
				}
			};
			View.OnClickListener cancelListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			};
			// 创建主布局
			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(createButtonLayout(activity, okListener, cancelListener));
			llMain.addView(llWheel);
			// 显示Dialog
			dialog.setContentView(llMain);
			dialog.setWidth(WindowManager.LayoutParams.FILL_PARENT);
			dialog.show();
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 返回WheelView当前选择项的值
	 * @param wheelView
	 * @return
	 */
	private int getSelectedValue(WheelView wheelView) {
		if (wheelView.getViewAdapter() instanceof NumericWheelAdapter) {
			return ((NumericWheelAdapter) wheelView.getViewAdapter()).getValue(wheelView.getCurrentItem());
		}
		return 0;
	}

	/**
	 * 设置WheelView的字体大小
	 * @param wheelView
	 * @param textSize
	 */
	private void setTextSize(WheelView wheelView, int textSize) {
		if (wheelView.getViewAdapter() instanceof NumericWheelAdapter) {
			((NumericWheelAdapter) wheelView.getViewAdapter()).setTextSize(textSize);
		}
	}

	private CustomDialog createDialog(Activity activity) {
		CustomDialog dialog = new CustomDialog(activity);
		dialog.setWidth(WindowManager.LayoutParams.FILL_PARENT);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setGravity(Gravity.BOTTOM);
		dialog.setLocation(0, 0);
		dialog.setAnimation(R.style.Animation_Bottom);
		return dialog;
	}

	private ViewGroup createButtonLayout(Activity activity, View.OnClickListener okListener, View.OnClickListener cancelListener) {
		final Button btCancel = new Button(activity);
		btCancel.setText("取消");
		btCancel.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
		btCancel.setBackgroundResource(R.drawable.button_black);
		btCancel.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		btCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		btCancel.setOnClickListener(cancelListener);
		RelativeLayout.LayoutParams paramsCancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		final Button btOk = new Button(activity);
		btOk.setText("确认");
		btOk.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
		btOk.setBackgroundResource(R.drawable.button_red);
		btOk.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		btOk.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		btOk.setOnClickListener(okListener);
		RelativeLayout.LayoutParams paramsOk = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsOk.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		RelativeLayout rlButton = new RelativeLayout(activity);
		rlButton.setBackgroundResource(R.drawable.bg_wheel_dialog_top);
		rlButton.addView(btCancel, paramsCancel);
		rlButton.addView(btOk, paramsOk);
		rlButton.setPadding(UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN), UnitUtil.dip2px(BUTTON_MARGIN));
		return rlButton;
	}

	private void setYearAdapter(WheelView wheelView, int startYear, int endYear, int currentYear) {
		wheelView.setViewAdapter(new NumericWheelAdapter(wheelView.getContext(), startYear, endYear, null, LABEL_YEAR));
		if (currentYear < startYear) {
			wheelView.setCurrentItem(0);
		} else if (currentYear > endYear) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = startYear; i <= endYear; i++) {
				if (i == currentYear) {
					wheelView.setCurrentItem(i - startYear);
					break;
				}
			}
		}
	}

	private void setMonthAdapter(WheelView wheelView, Calendar startDate, Calendar endDate, int currentYear, int currentMonth) {
		NumericWheelAdapter adapter = createMonthAdapter(wheelView.getContext(), startDate, endDate, currentYear);
		wheelView.setViewAdapter(adapter);
		int minValue = adapter.getMinValue();
		int maxValue = adapter.getMaxValue();
		if (currentMonth < minValue) {
			wheelView.setCurrentItem(0);
		} else if (currentMonth > maxValue) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = minValue; i <= maxValue; i++) {
				if (i == currentMonth) {
					wheelView.setCurrentItem(i - minValue);
					break;
				}
			}
		}
	}

	private void setDayAdapter(WheelView wheelView, Calendar startDate, Calendar endDate, int currentYear, int currentMonth, int currentDay) {
		NumericWheelAdapter adapter = createDayAdapter(wheelView.getContext(), startDate, endDate, currentYear, currentMonth);
		wheelView.setViewAdapter(adapter);
		int minValue = adapter.getMinValue();
		int maxValue = adapter.getMaxValue();
		if (currentDay < minValue) {
			wheelView.setCurrentItem(0);
		} else if (currentDay > maxValue) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = minValue; i <= maxValue; i++) {
				if (i == currentDay) {
					wheelView.setCurrentItem(i - minValue);
					break;
				}
			}
		}
	}

	private void setHourAdapter(WheelView wheelView, int startHour, int endHour, int currentHour) {
		wheelView.setViewAdapter(new NumericWheelAdapter(wheelView.getContext(), startHour, endHour));
		if (currentHour < startHour) {
			wheelView.setCurrentItem(0);
		} else if (currentHour > endHour) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = startHour; i <= endHour; i++) {
				if (i == currentHour) {
					wheelView.setCurrentItem(i - startHour);
					break;
				}
			}
		}
	}

	private void setHourAdapter(WheelView wheelView, Calendar startDate, Calendar endDate, int currentYear, int currentMonth, int currentDay, int currentHour) {
		NumericWheelAdapter adapter = createHourAdapter(wheelView.getContext(), startDate, endDate, currentYear, currentMonth, currentDay);
		wheelView.setViewAdapter(adapter);
		int minValue = adapter.getMinValue();
		int maxValue = adapter.getMaxValue();
		if (currentHour < minValue) {
			wheelView.setCurrentItem(0);
		} else if (currentHour > maxValue) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = minValue; i <= maxValue; i++) {
				if (i == currentHour) {
					wheelView.setCurrentItem(i - minValue);
					break;
				}
			}
		}
	}

	private void setMinuteAdapter(WheelView wheelView, Calendar startDate, Calendar endDate, int currentHour, int currentMinute) {
		NumericWheelAdapter adapter = createMinuteAdapter(wheelView.getContext(), startDate, endDate, currentHour);
		wheelView.setViewAdapter(adapter);
		int minValue = adapter.getMinValue();
		int maxValue = adapter.getMaxValue();
		if (currentMinute < minValue) {
			wheelView.setCurrentItem(0);
		} else if (currentMinute > maxValue) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = minValue; i <= maxValue; i++) {
				if (i == currentMinute) {
					wheelView.setCurrentItem(i - minValue);
					break;
				}
			}
		}
	}

	private void setMinuteAdapter(WheelView wheelView, Calendar startDate, Calendar endDate, int currentYear, int currentMonth, int currentDay, int currentHour, int currentMinute) {
		NumericWheelAdapter adapter = createMinuteAdapter(wheelView.getContext(), startDate, endDate, currentYear, currentMonth, currentDay, currentHour);
		wheelView.setViewAdapter(adapter);
		int minValue = adapter.getMinValue();
		int maxValue = adapter.getMaxValue();
		if (currentMinute < minValue) {
			wheelView.setCurrentItem(0);
		} else if (currentMinute > maxValue) {
			wheelView.setCurrentItem(wheelView.getViewAdapter().getItemsCount() - 1);
		} else {
			for (int i = minValue; i <= maxValue; i++) {
				if (i == currentMinute) {
					wheelView.setCurrentItem(i - minValue);
					break;
				}
			}
		}
	}

	/**
	 * 根据起始，结束时间和当前时间创建月份数据
	 * @param context
	 * @param startDate
	 * @param endDate
	 * @param currentYear
	 * @return
	 */
	private NumericWheelAdapter createMonthAdapter(Context context, Calendar startDate, Calendar endDate, int currentYear) {
		// 判断当前时间是否处于上下限
		final int startYear = startDate.get(Calendar.YEAR);
		int startMonth = startDate.get(Calendar.MONTH) + 1;
		final int endYear = endDate.get(Calendar.YEAR);
		int endMonth = endDate.get(Calendar.MONTH) + 1;
		if (currentYear != startYear) {
			startMonth = 1;
		}
		if (currentYear != endYear) {
			endMonth = 12;
		}
		return new NumericWheelAdapter(context, startMonth, endMonth, null, LABEL_MONTH);
	}

	/**
	 * 根据起始，结束时间和当前时间创建日期数据
	 * @param context
	 * @param startDate
	 * @param endDate
	 * @param currentYear
	 * @param currentMonth
	 * @return
	 */
	private NumericWheelAdapter createDayAdapter(Context context, Calendar startDate, Calendar endDate, int currentYear, int currentMonth) {
		// 判断当前时间是否处于上下限
		final int startYear = startDate.get(Calendar.YEAR);
		final int startMonth = startDate.get(Calendar.MONTH) + 1;
		int startDay = startDate.get(Calendar.DAY_OF_MONTH);
		final int endYear = endDate.get(Calendar.YEAR);
		final int endMonth = endDate.get(Calendar.MONTH) + 1;
		int endDay = endDate.get(Calendar.DAY_OF_MONTH);
		if (currentYear != startYear || currentMonth != startMonth) {
			startDay = 1;
		}
		if (currentYear != endYear || currentMonth != endMonth) {
			endDay = 31;
		}

		// 判断大小月及是否闰年,用来确定"日"的数据
		int endOfMonth = 28;
		int minDay = 1;
		int maxDay = 28;
		if (LIST_MONTHS_31.contains(String.valueOf(currentMonth))) {
			// 大月
			endOfMonth = 31;
		} else if (LIST_MONTHS_30.contains(String.valueOf(currentMonth))) {
			// 小月
			endOfMonth = 30;
		} else {
			if ((currentYear % 4 == 0 && currentYear % 100 != 0) || currentYear % 400 == 0) {
				// 闰年二月
				endOfMonth = 29;
			} else {
				// 平年二月
				endOfMonth = 28;
			}
		}
		minDay = Math.max(1, Math.min(endOfMonth, startDay));
		maxDay = Math.min(Math.max(1, endDay), endOfMonth);
		return new NumericWheelAdapter(context, minDay, maxDay, null, LABEL_DAY);
	}

	/**
	 * 根据起始，结束时间和当前时间创建小时数据
	 * @param context
	 * @param startDate
	 * @param endDate
	 * @param currentYear
	 * @param currentMonth
	 * @param currentDay
	 * @return
	 */
	private NumericWheelAdapter createHourAdapter(Context context, Calendar startDate, Calendar endDate, int currentYear, int currentMonth, int currentDay) {
		// 判断当前时间是否处于上下限
		final int startYear = startDate.get(Calendar.YEAR);
		final int startMonth = startDate.get(Calendar.MONTH) + 1;
		final int startDay = startDate.get(Calendar.DAY_OF_MONTH);
		int startHour = startDate.get(Calendar.HOUR_OF_DAY);
		final int endYear = endDate.get(Calendar.YEAR);
		final int endMonth = endDate.get(Calendar.MONTH) + 1;
		final int endDay = endDate.get(Calendar.DAY_OF_MONTH);
		int endHour = endDate.get(Calendar.HOUR_OF_DAY);
		if (currentYear != startYear || currentMonth != startMonth || currentDay != startDay) {
			startHour = 0;
		}
		if (currentYear != endYear || currentMonth != endMonth || currentDay != endDay) {
			endHour = 23;
		}
		return new NumericWheelAdapter(context, startHour, endHour);
	}

	/**
	 * 根据起始，结束时间和当前时间创建分钟数据(对于起始和结束时间，不考虑年，月，日，只考虑小时)
	 * @param context
	 * @param startDate
	 * @param endDate
	 * @param currentHour
	 * @return
	 */
	private NumericWheelAdapter createMinuteAdapter(Context context, Calendar startDate, Calendar endDate, int currentHour) {
		// 判断当前时间是否处于上下限
		final int startHour = startDate.get(Calendar.HOUR_OF_DAY);
		int startMinute = startDate.get(Calendar.MINUTE);
		final int endHour = endDate.get(Calendar.HOUR_OF_DAY);
		int endMinute = endDate.get(Calendar.MINUTE);
		if (currentHour != startHour) {
			startMinute = 0;
		}
		if (currentHour != endHour) {
			endMinute = 59;
		}
		return new NumericWheelAdapter(context, startMinute, endMinute, "%02d");
	}

	/**
	 * 根据起始，结束时间和当前时间创建分钟数据(对于起始和结束时间，考虑全部字段)
	 * @param context
	 * @param startDate
	 * @param endDate
	 * @param currentYear
	 * @param currentMonth
	 * @param currentDay
	 * @param currentHour
	 * @return
	 */
	private NumericWheelAdapter createMinuteAdapter(Context context, Calendar startDate, Calendar endDate, int currentYear, int currentMonth, int currentDay, int currentHour) {
		// 判断当前时间是否处于上下限
		final int startYear = startDate.get(Calendar.YEAR);
		final int startMonth = startDate.get(Calendar.MONTH) + 1;
		final int startDay = startDate.get(Calendar.DAY_OF_MONTH);
		final int startHour = startDate.get(Calendar.HOUR_OF_DAY);
		int startMinute = startDate.get(Calendar.MINUTE);
		final int endYear = endDate.get(Calendar.YEAR);
		final int endMonth = endDate.get(Calendar.MONTH) + 1;
		final int endDay = endDate.get(Calendar.DAY_OF_MONTH);
		final int endHour = endDate.get(Calendar.HOUR_OF_DAY);
		int endMinute = endDate.get(Calendar.MINUTE);
		if (currentYear != startYear || currentMonth != startMonth || currentDay != startDay || currentHour != startHour) {
			startMinute = 0;
		}
		if (currentYear != endYear || currentMonth != endMonth || currentDay != endDay || currentHour != endHour) {
			endMinute = 59;
		}
		return new NumericWheelAdapter(context, startMinute, endMinute, "%02d");
	}
}
