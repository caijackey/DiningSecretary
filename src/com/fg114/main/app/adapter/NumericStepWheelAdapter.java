package com.fg114.main.app.adapter;

import android.content.*;
import android.text.TextUtils;
import android.util.Log;

/**
 * 由NumericStepWheelAdapter修改而来，支持数字步长
 * @author xujianjun,2013-07-23
 * Numeric Wheel adapter.
 */
public class NumericStepWheelAdapter extends AbstractWheelTextAdapter {
    
	public interface Facade{
		CharSequence onGetItemText(long value);
	}
    
    // Values
    private long minValue;
    private long maxValue;
    
    //步长
    private long step=1;
    
    //外观
    private Facade facade;
    //---
    public NumericStepWheelAdapter(Context context, long minValue, long maxValue) {
    	this(context, minValue, maxValue, null);
    }
    public NumericStepWheelAdapter(Context context, long minValue, long maxValue, Facade facade) {
    	this(context, minValue, maxValue, 1, facade);
    }
    public NumericStepWheelAdapter(Context context, long minValue, long maxValue, long step, Facade facade) {
        super(context);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step<=0?1:step;
        this.facade = facade;
    }

    @Override
    public CharSequence getItemText(int index) {
    	
        if (index >= 0 && index < getItemsCount()) {
           if(facade!=null){
        	   return facade.onGetItemText(getValue(index));
           }else{
        	   return String.valueOf(getValue(index));
           }
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return (int)((maxValue - minValue)/step) + 1;
    }    

    
    public long getValue(int index) {
    	if (index >= 0 && index < getItemsCount()) {
    		return minValue + index*step;
    	}
    	return minValue;
    }
    
    public long getMinValue() {
    	return minValue;
    }
    
    public long getMaxValue() {
    	return maxValue;
    }
    
	@Override
	public int getIndexByValue(long value) { 
		if(value>=minValue && value<=maxValue){
			long offset=value-minValue;
			long index=offset/step;
			long remainder=offset%step;
			//--
//			if(step-remainder>=remainder){
//				return (int)index;
//			}else{
//				return (int)index+1;
//			}
			//始终取上整
			if(remainder==0){
				return (int)index;
			}else{
				return (int)index+1;
			}
			
		}
		return 0;
	}
	@Override
	public long getValueByIndex(int index) {
		return getValue(index);
	}

}
