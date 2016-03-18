package com.fg114.main.util;

import java.util.*;

import android.graphics.*;

import android.view.*;

public class DragController {

	private static final int GRID_CELL_WIDTH = 15; // 网格边长
	private static final int VALID_OFFSET = 3; // 有效拖动距离

	private HashMap<Integer, Rect> mRectMap = new HashMap<Integer, Rect>(); // 当不可重叠时，存储各控件的位置
	private Rect mRect; // 可拖动的区域
	private boolean mCanCover; // 控件之间是否可覆盖
	private boolean mAlign; // 是否自动对齐网格线

	public interface OnDragListener {
		public void onDragEnd(View view, int x, int y);
	}

	public DragController() {

	}

	public DragController(boolean canCover, boolean align, Rect rect) {
		mCanCover = canCover;
		mAlign = align;
		mRect = rect;
	}

	public void setDragable(final View view, boolean dragable, final OnDragListener listener) {
		try {
			if (!mCanCover) {
				
				mRectMap.put(view.getId(), new Rect(view.getLeft(), view.getTop(), view.getLeft() + view.getWidth(), view.getTop() + view.getHeight()));
			}
			if (!dragable) {
				view.setDrawingCacheEnabled(false);
				view.setOnTouchListener(null);
				return;
			}
			view.setDrawingCacheEnabled(true);
			view.setOnTouchListener(new View.OnTouchListener() {
				boolean isDrag; // 是否拖动
				int[] temp = new int[] { 0, 0 }; // 按下时的坐标位置
				int[] lastLoc = new int[] { 0, 0 }; // 按下时的left和top

				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();
					int x = (int) event.getRawX();
					int y = (int) event.getRawY();

					
					try {
						switch (action) {
						case MotionEvent.ACTION_DOWN:
							
							temp[0] = (int) event.getX();
							temp[1] = (int) event.getY();
							lastLoc[0] = v.getLeft();
							lastLoc[1] = v.getTop();
							
							v.bringToFront();
							return false;
						case MotionEvent.ACTION_MOVE:
							
							if (v.isPressed()) 
							{
								isDrag=true;
								
							}
							
							
							
							int left = x - temp[0];
							int top = y - temp[1] - v.getHeight() / 2;
							int right = left + v.getWidth();
							int bottom = top + v.getHeight();

							if(!isValidOffset(lastLoc[0], lastLoc[1], left, top))
							{
								isDrag=false;
							}
							if (!isDrag) {
								break;
							}

							/* 控制可移动范围 */
							if (mRect != null) {
								if (left < mRect.left) {
									left = mRect.left;
									right = left + v.getWidth();
								}
								if (top < mRect.top) {
									top = mRect.top;
									bottom = top + v.getHeight();
								}
								if (right > mRect.right) {
									right = mRect.right;
									left = right - v.getWidth();
								}
								if (bottom > mRect.bottom) {
									bottom = mRect.bottom;
									top = bottom - v.getHeight();
								}
							}
							v.layout(left, top, right, bottom);
							
							v.postInvalidate();
							return true;
						case MotionEvent.ACTION_UP:
							
							if (!isDrag) {
								return false;
							}
							if (mAlign) {
								int interval = 0;

								/* 计算横轴上最近的网格中心点 */
								int centerX = v.getLeft() + v.getWidth() / 2;
								int leftGridX = centerX / GRID_CELL_WIDTH * GRID_CELL_WIDTH - GRID_CELL_WIDTH / 2;
								int rightGridX = leftGridX + GRID_CELL_WIDTH;
								if (Math.abs(centerX - leftGridX) <= Math.abs(centerX - rightGridX)) {
									interval = centerX - leftGridX;
								} else {
									interval = centerX - rightGridX;
								}
								/* 靠向较近的中心点 */
								left = v.getLeft() - interval;
								right = left + v.getWidth();

								/* 计算纵轴上最近的网格中心点 */
								int centerY = v.getTop() + v.getHeight() / 2;
								int leftGridY = centerY / GRID_CELL_WIDTH * GRID_CELL_WIDTH - GRID_CELL_WIDTH / 2;
								int rightGridY = leftGridY + GRID_CELL_WIDTH;
								if (Math.abs(centerY - leftGridY) <= Math.abs(centerY - rightGridY)) {
									interval = centerY - leftGridY;
								} else {
									interval = centerY - rightGridY;
								}
								/* 靠向较近的中心点 */
								top = v.getTop() - interval;
								bottom = top + v.getHeight();

								/* 控制可移动范围 */
								if (mRect != null) {
									if (left < mRect.left) {
										left = mRect.left;
										right = left + v.getWidth();
									}
									if (top < mRect.top) {
										top = mRect.top;
										bottom = top + v.getHeight();
									}
									if (right > mRect.right) {
										right = mRect.right;
										left = right - v.getWidth();
									}
									if (bottom > mRect.bottom) {
										bottom = mRect.bottom;
										top = bottom - v.getHeight();
									}
								}

								v.layout(left, top, right, bottom);
								v.postInvalidate();
							}

							/* 不可重叠的处理，有重叠时将拖动项放回原位置 */
							if (!mCanCover) {
								if (mRectMap.size() > 0) {
									for (int key : mRectMap.keySet()) {
										if (key == view.getId()) {
											continue;
										}
										Rect rect = mRectMap.get(key);
										if (rect.intersects(v.getLeft(), v.getTop(), v.getLeft() + v.getWidth(), v.getTop() + v.getHeight())) {
											left = lastLoc[0];
											top = lastLoc[1];
											right = left + v.getWidth();
											bottom = top + v.getHeight();
											v.layout(left, top, right, bottom);
											v.postInvalidate();
											break;
										}
									}
								}
								mRectMap.put(view.getId(), new Rect(v.getLeft(), v.getTop(), v.getLeft() + v.getWidth(), v.getTop() + v.getHeight()));
							}
							
							
							
							// 设置按钮为非按下状态
							view.setPressed(false);
							isDrag = false;
							if (listener != null) {
								listener.onDragEnd(view, x, y);
							}
							return true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isValidOffset(int oldLeft, int oldTop, int left, int top) {
		if (Math.abs(oldLeft - left) >= VALID_OFFSET && Math.abs(oldTop - top) >= VALID_OFFSET) {
			return true;
		}
		return false;
	}
}