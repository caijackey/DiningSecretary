package com.fg114.main.app.view;

import com.fg114.main.util.UnitUtil;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ParabolaAnimation extends Animation {
	private int mFromXType = ABSOLUTE;
	private int mToXType = ABSOLUTE;

	private int mFromYType = ABSOLUTE;
	private int mToYType = ABSOLUTE;

	private float mFromXValue = 0.0f;
	private float mToXValue = 0.0f;

	private float mFromYValue = 0.0f;
	private float mToYValue = 0.0f;

	private float mFromXDelta;
	private float mToXDelta;
	private float mFromYDelta;
	private float mToYDelta;
	private float heightParabola;
	private float y;
	private float x;

	public ParabolaAnimation(float fromXDelta, float fromYDelta, float toXDelta, float toYDelta, float heightParabola) {
		this.mFromXValue = fromXDelta;
		this.mFromYValue = -fromYDelta;
		this.mToXValue = toXDelta;
		this.mToYValue = -toYDelta;
		this.heightParabola = heightParabola + mFromYValue;
		
		if (fromYDelta<toYDelta) {//向下
			this.heightParabola = heightParabola + mFromYValue;
			x=(mToXValue+2*mFromXValue)/3;
		}else{//向上
			this.heightParabola = heightParabola + mToYValue;
			x=(fromXDelta-toXDelta)/3+toXDelta;
		}
		Log.e("heightParabola", this.heightParabola+"");
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float dx = mFromXDelta;
		float dy = mFromYDelta;
		if (mFromXDelta != mToXDelta) {
			dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
		}
		if (mFromYDelta != mToYDelta) {
			dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
		}
		y = computeY(dx);
		t.getMatrix().setTranslate(dx, -y);
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth);
		mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
		mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight);
		mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);
	}

	private float computeA(){
    	return ((mFromYValue-mToYValue)*(mToXValue-x)-(mToYValue-(heightParabola))*(mFromXValue-mToXValue))/((mFromXValue-mToXValue)*(mToXValue-x)*(mFromXValue-x));
//    	return ((mFromYValue-mToYValue)*(mToXValue-(mToXValue+2*mFromXValue)/3)-(mToYValue-(heightParabola))*(mFromXValue-mToXValue))/((mFromXValue-mToXValue)*(mToXValue-(mToXValue+2*mFromXValue)/3)*(mFromXValue-(mToXValue+2*mFromXValue)/3));
    }

	private float computeB() {
		return (mFromYValue-mToYValue)/(mFromXValue-mToXValue)- computeA()*(mFromXValue+mToXValue);
	}

	private float computeY(float x) {
		float a = computeA();
		float b = computeB();
		return a * x * x + b * x + (mFromYValue-a*mFromXValue*mFromXValue-b*mFromXValue);
	}
}
