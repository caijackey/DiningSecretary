package com.fg114.main.app.view;

import com.fg114.main.*;
import com.fg114.main.app.view.PredicateLayout.LayoutParams;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.ViewUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.MeasureSpec;
import android.widget.*;

/**
 * 
 * 数字选择控件
 * 
 * @author xu jianjun, 2012-01-05
 * 
 */
public class DigitalSelector extends ViewGroup {

	Context context;
	ImageButton minus;
	EditText digital;
	ImageButton plus;
	int value = 1;
	int min = 1;
	int max = 99;
	// 达到最大最小值时的提示
	String maxWarning;
	String minWarning;
	LinearLayout layout;
	LayoutParams lp;
	volatile boolean longClick = false;
	public static boolean isEnableClick = true;
	public static boolean isEnableLongClick = true;

	private OnDigitChangeListener listener;

	public DigitalSelector(Context context) {
		super(context);
		this.context = context;
		init();

	}

	public DigitalSelector(Context context, int minValue, int maxValue) {
		super(context);
		this.min = minValue;
		this.max = maxValue;
		this.context = context;
		init();

	}

	public DigitalSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();

	}

	public DigitalSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();

	}

	private void init() {
		layout = (LinearLayout) View.inflate(this.context, R.layout.digital_selector, null);
		minus = (ImageButton) layout.getChildAt(0);
		digital = (EditText) layout.getChildAt(1);
		plus = (ImageButton) layout.getChildAt(2);
		this.addView(layout);
		digital.setText(value + "");

		minus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isEnableClick) {
					clickMinus();
				}
			}
		});

		minus.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (isEnableLongClick) {

					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								while (minus.isPressed()) {
									((Activity) context).runOnUiThread(new Runnable() {

										@Override
										public void run() {
											clickMinus();
										}
									});
									Thread.sleep(120);
								}

							} catch (Exception ex) {
								Log.e("XXX", "", ex);
							}
						}
					}).start();

				}
				return false;
			}
		});

		plus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isEnableClick) {
					clickPlus();
				}
				

			}
		});

		plus.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (isEnableLongClick) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								while (plus.isPressed()) {
									((Activity) context).runOnUiThread(new Runnable() {

										@Override
										public void run() {
											clickPlus();
										}
									});
									Thread.sleep(120);
								}

							} catch (Exception ex) {
								Log.e("YYY", "", ex);
							}
						}
					}).start();
				}
				return false;
			}
		});

	}

	/**
	 * @return 返回选择的数字
	 */
	public int getValue() {
		return Integer.parseInt(digital.getText().toString(), 10);
	}

	/**
	 * @return 设置显示的数字
	 */
	public void setValue(int value) {

		if (this.value == value) {
			return;
		}
		// 不允许超过范围
		if (value < min) {
			if (this.minWarning != null) {
				DialogUtil.showToast(this.context, this.minWarning);
			}
			return;
		}
		if (value > max) {
			if (this.maxWarning != null) {
				DialogUtil.showToast(this.context, this.maxWarning);
//				DialogUtil.showAlert(this.context, "", this.maxWarning);
			}
			return;
		}
		this.value = value;
		digital.setText(value + "");
		if (listener != null) {
			listener.onChange(this, this.value, value);
		}
	}

	/**
	 * @return 模拟点击＋按钮
	 */
	public void clickPlus() {
		setValue(this.value + 1);
	}

	/**
	 * @return 模拟点击－按钮
	 */
	public void clickMinus() {
		setValue(this.value - 1);
	}

	//
	public int getMinValue() {
		return min;
	}

	public void setMinValue(int minValue) {
		this.min = minValue;
		if (this.getValue() < this.min) {
			this.setValue(this.min);
		}

	}

	public int getMaxValue() {
		return max;
	}

	public void setMaxValue(int maxValue) {
		this.max = maxValue;
		if (this.getValue() > this.max) {
			this.setValue(this.max);
		}
	}

	//
	public void setMinWarning(String minWarning) {
		this.minWarning = minWarning;
	}

	public void setMaxWarning(String maxWarning) {
		this.maxWarning = maxWarning;
	}

	//
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		if (!changed) {
			return;
		}

		final int width = r - l;
		final int height = b - t;

		int x = 0;
		int y = 0;

		//
		minus.setMaxWidth(height);
		minus.setMinimumWidth(height);

		plus.setMaxWidth(height);
		plus.setMinimumWidth(height);
		//
		digital.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (height * 4.0 / 10 +1));

		layout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		layout.layout(0, 0, width, height);

	}

	public void setOnDigitChangeListener(OnDigitChangeListener listener) {
		this.listener = listener;
	}

	public interface OnDigitChangeListener {
		/**
		 * @param selector
		 *            发生事件的数字选择控件
		 * @param digit
		 *            变化后的数字
		 * @param previousValue
		 *            变化前的数字
		 */
		void onChange(DigitalSelector selector, int digit, int previousValue);
	}

	/**
	 * 设置控件初始化的时候时，默认值
	 * 
	 * @param defaultValue
	 *            需要设置的默认值
	 * @author nieyinyin
	 */
	public void setDefaultValue(int defaultValue) {
		try {
			this.value = defaultValue;
			digital.setText(String.valueOf(defaultValue));
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 设置控件的值
	 * 
	 * @param value
	 *            需要设置数字
	 * @author nieyinyin
	 */
	public void setDigitalValue(int value) {
		try {
			this.value = value;
			digital.setText(String.valueOf(value));
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 设置加号的背景
	 * 
	 * @param resid
	 */
	public void setPlusBackgroundResource(int resid) {
		plus.setBackgroundResource(resid);
		plus.setImageResource(0);
	}

	/**
	 * 设置加号的背景颜色
	 * 
	 * @param color
	 * @author nieyinyin
	 */
	public void setPlusBackgroundColor(int color) {
		plus.setBackgroundColor(color);
	}

	/**
	 * 设置减号的背景
	 * 
	 * @param resid
	 * @author nieyinyin
	 */
	public void setminusBackgroundResource(int resid) {
		plus.setBackgroundResource(resid);
	}

	/**
	 * 设置减号的背景颜色
	 * 
	 * @param resid
	 * @author nieyinyin
	 */
	public void setminusBackgroundColor(int color) {
		plus.setBackgroundColor(color);
	}

	/**
	 * 设置数字的背景颜色
	 * 
	 * @param resid
	 * @author nieyinyin
	 */
	public void setDigitalBackgroundColor(int color) {
		digital.setBackgroundColor(color);
	}

	// getters and setters
	public ImageButton getMinus() {
		return minus;
	}

	public void setMinus(ImageButton minus) {
		this.minus = minus;
	}

	public ImageButton getPlus() {
		return plus;
	}

	public void setPlus(ImageButton plus) {
		this.plus = plus;
	}

	public EditText getDigital() {
		return digital;
	}

	public void setDigital(EditText digital) {
		this.digital = digital;
	}

}
