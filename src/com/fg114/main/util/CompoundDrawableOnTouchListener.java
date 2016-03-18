package com.fg114.main.util;

import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/**
 * TextView四周Drawable的点击监听
 * @author wufucheng
 *
 */
public abstract class CompoundDrawableOnTouchListener implements View.OnTouchListener {

	public static final int COMPOUND_DRAWABLE_LEFT = 0;
	public static final int COMPOUND_DRAWABLE_TOP = 1;
	public static final int COMPOUND_DRAWABLE_RIGHT = 2;
	public static final int COMPOUND_DRAWABLE_BOTTOM = 3;

	private static final int FUZZ = 10;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && v instanceof TextView) {
			Drawable[] drawables = ((TextView) v).getCompoundDrawables();
			if (drawables == null || drawables.length != 4) {
				return false;
			}
			int x = (int) event.getX();
			int y = (int) event.getY();
			boolean isConsumed = false; // 事件是否被消费
			Rect bounds;
			int left, top, right, bottom;

			// Left
			if (drawables[COMPOUND_DRAWABLE_LEFT] != null) {
				bounds = drawables[COMPOUND_DRAWABLE_LEFT].getBounds();
				left = v.getPaddingLeft() - FUZZ;
				right = v.getPaddingLeft() + bounds.width() + FUZZ;
				top = v.getPaddingTop() + (v.getHeight() - v.getPaddingTop() - v.getPaddingBottom()) / 2 - bounds.height() / 2 - FUZZ;
				bottom = v.getPaddingTop() + (v.getHeight() - v.getPaddingTop() - v.getPaddingBottom()) / 2 + bounds.height() / 2 + FUZZ;
				if (x >= left && x <= right && y >= top && y <= bottom) {
					onDrawableTouch(event, COMPOUND_DRAWABLE_LEFT);
					isConsumed = true;
				}
			}

			// Top
			if (drawables[COMPOUND_DRAWABLE_TOP] != null) {
				bounds = drawables[COMPOUND_DRAWABLE_TOP].getBounds();
				left = v.getLeft() + v.getPaddingLeft() + (v.getWidth() - v.getPaddingLeft() - v.getPaddingRight()) / 2 - bounds.width() / 2 - FUZZ;
				right = v.getLeft() + v.getPaddingLeft() + (v.getWidth() - v.getPaddingLeft() - v.getPaddingRight()) / 2 + bounds.width() / 2 + FUZZ;
				top = v.getPaddingTop() - FUZZ;
				bottom = v.getPaddingTop() + bounds.height() + FUZZ;
				if (x >= left && x <= right && y >= top && y <= bottom) {
					onDrawableTouch(event, COMPOUND_DRAWABLE_TOP);
					isConsumed = true;
				}
			}

			// Right
			if (drawables[COMPOUND_DRAWABLE_RIGHT] != null) {
				bounds = drawables[COMPOUND_DRAWABLE_RIGHT].getBounds();
				left = v.getWidth() - v.getPaddingRight() - bounds.width() - FUZZ;
				right = v.getWidth() - v.getPaddingRight() + FUZZ;
				top = v.getPaddingTop() + (v.getHeight() - v.getPaddingTop() - v.getPaddingBottom()) / 2 - bounds.height() / 2 - FUZZ;
				bottom = v.getPaddingTop() + (v.getHeight() - v.getPaddingTop() - v.getPaddingBottom()) / 2 + bounds.height() / 2 + FUZZ;
				if (x >= left && x <= right && y >= top && y <= bottom) {
					onDrawableTouch(event, COMPOUND_DRAWABLE_RIGHT);
					isConsumed = true;
				}
			}

			// Bottom
			if (drawables[COMPOUND_DRAWABLE_BOTTOM] != null) {
				bounds = drawables[COMPOUND_DRAWABLE_BOTTOM].getBounds();
				left = v.getLeft() + v.getPaddingLeft() + (v.getWidth() - v.getPaddingLeft() - v.getPaddingRight()) / 2 - bounds.width() / 2 - FUZZ;
				right = v.getLeft() + v.getPaddingLeft() + (v.getWidth() - v.getPaddingLeft() - v.getPaddingRight()) / 2 + bounds.width() / 2 + FUZZ;
				top = v.getHeight() - v.getPaddingBottom() - bounds.height() - FUZZ;
				bottom = v.getHeight() - v.getPaddingBottom() + FUZZ;
				if (x >= left && x <= right && y >= top && y <= bottom) {
					onDrawableTouch(event, COMPOUND_DRAWABLE_BOTTOM);
					isConsumed = true;
				}
			}

			return isConsumed;
		}
		return false;
	}

	/**
	 * 触发Drawable的触摸事件
	 * @param event 触摸事件
	 * @param which 被点击的Drawable COMPOUND_DRAWABLE_LEFT，COMPOUND_DRAWABLE_TOP...
	 * @return
	 */
	public abstract boolean onDrawableTouch(MotionEvent event, int which);
}
