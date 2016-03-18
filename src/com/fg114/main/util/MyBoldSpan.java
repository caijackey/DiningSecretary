package com.fg114.main.util;

import android.graphics.*;
import android.text.*;
import android.text.style.*;

/**
 * 重写StyleSpan，强制设置粗体
 * @author wufucheng
 *
 */
public class MyBoldSpan extends StyleSpan {

	public MyBoldSpan(int style) {
		super(style);
	}

	@Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, Typeface.BOLD);
    }

	@Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, Typeface.BOLD);
    }

    private static void apply(Paint paint, int style) {
        try {
			int oldStyle;
			Typeface old = paint.getTypeface();
			if (old == null) {
				oldStyle = 0;
			} else {
				oldStyle = old.getStyle();
			}
			int want = oldStyle | style;
			Typeface tf;
			if (old == null) {
				tf = Typeface.defaultFromStyle(want);
			} else {
				tf = Typeface.create(old, want);
			}
			int fake = want & ~tf.getStyle();
			// 强制设为粗体
			//        if ((fake & Typeface.BOLD) != 0) {
			paint.setFakeBoldText(true);
			//        }
			if ((fake & Typeface.ITALIC) != 0) {
				paint.setTextSkewX(-0.25f);
			}
			paint.setTypeface(tf);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	 
}
