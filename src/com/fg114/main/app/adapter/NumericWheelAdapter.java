package com.fg114.main.app.adapter;

import android.content.*;
import android.text.TextUtils;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {
    
    /** The default min value */
    public static final int DEFAULT_MAX_VALUE = 9;

    /** The default max value */
    private static final int DEFAULT_MIN_VALUE = 0;
    
    // Values
    private int minValue;
    private int maxValue;
    
    // format
    private String format;
    private String label;
    
    /**
     * Constructor
     * @param context the current context
     */
    public NumericWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }
    
    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
    	this(context, minValue, maxValue, format, null);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     * @param format the format string
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format, String label) {
        super(context);
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
        this.label = label;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            String text = format != null ? String.format(format, value) : Integer.toString(value);
            return TextUtils.isEmpty(label) ? text : text + label;
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }    
    
    public void setLabel(String label) {
    	this.label = label;
    }
    
    public int getValue(int index) {
    	if (index >= 0 && index < getItemsCount()) {
    		return minValue + index;
    	}
    	return minValue;
    }
    
    public int getMinValue() {
    	return minValue;
    }
    
    public int getMaxValue() {
    	return maxValue;
    }

	@Override
	public int getIndexByValue(long value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getValueByIndex(int index) {
		// TODO Auto-generated method stub
		return 0;
	}
}
