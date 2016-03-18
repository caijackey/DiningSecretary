package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.util.UnitUtil;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 下拉，上拉刷新容器
 * 
 * @author xujianjun,2013-11-11
 * 
 */
public class DragLoadingView extends LinearLayout {
	Context context;
	private ImageView icon;
	private TextView text;
	private LinearLayout topContainer;
	private String topDragHint = "下拉可以刷新";
	private String topReleaseHint = "放开即时刷新";
	private String topLoadingHint = "正在加载...";
	private int dragMaxDistance = 60;
	// ---
	private DragLoadingListener listener;

	public interface DragLoadingListener {
		public boolean isAllowDrag();

		public void onDragReleased();
	}

	public DragLoadingView(Context context) {
		this(context, null);
	}

	public DragLoadingView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		if (this.isInEditMode()) {
			return;
		}
		this.setOrientation(LinearLayout.VERTICAL);
		this.context = ctx;
		dragMaxDistance = UnitUtil.dip2px(60);
		icon = new ImageView(context);
		text = new TextView(context);
		// ---
		topContainer = new LinearLayout(context);
		// ---
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, dragMaxDistance);
		lp.setMargins(0, -dragMaxDistance, 0, 0);
		topContainer.setGravity(Gravity.CENTER);
		topContainer.setBaselineAligned(false);
		topContainer.setLayoutParams(lp);
		topContainer.setBackgroundColor(0x05000000);
		// --图标
		LinearLayout.LayoutParams lpImage = new LinearLayout.LayoutParams(UnitUtil.dip2px(35), UnitUtil.dip2px(40), 0);
		lpImage.setMargins(0, 0, UnitUtil.dip2px(10), 0);
		icon.setLayoutParams(lpImage);
		icon.setPadding(0, 0, 0, 0);
		icon.setScaleType(ScaleType.CENTER_INSIDE);
		// --文字
		LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(UnitUtil.dip2px(100), UnitUtil.dip2px(60), 0);
		lpText.setMargins(0, 0, 0, 0);
		text.setLayoutParams(lpText);
		text.setSingleLine(true);
		text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		text.setTextColor(0xFF666666);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		// --
		topContainer.addView(icon);
		topContainer.addView(text);
		this.addView(topContainer, 0);
		// this.setDragLoadingListener(new DragLoadingListener() {
		//
		// @Override
		// public void onDragReleased() {
		// Toast.makeText(context, "加载开始", Toast.LENGTH_LONG).show();
		//
		// }
		//
		// @Override
		// public boolean isAllowDrag() {
		// return true;
		// }
		// });
	}

	// ---
	public void setDragLoadingListener(DragLoadingListener listener) {
		this.listener = listener;
	}

	private int getTopMargin() {
		LinearLayout.LayoutParams lp = (LayoutParams) topContainer.getLayoutParams();
		if (topContainer != null) {
			return lp.topMargin;
		} else {
			return 0;
		}
	}

	private void setTopMargin(int marginTop) {
		// Log.d("setTopMargin---",""+marginTop);
		if (marginTop < -dragMaxDistance) {
			marginTop = -dragMaxDistance;
		}
		if (marginTop >= 0) {
			marginTop = 0;
		}
		LinearLayout.LayoutParams lp = (LayoutParams) topContainer.getLayoutParams();
		lp.setMargins(0, marginTop, 0, 0);
		topContainer.setLayoutParams(lp);
	}

	// ---
	// 总行程
	private int origin = -1;
	private int max = -1;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (listener == null) {
			return super.dispatchTouchEvent(ev);
		}
		//Log.e(dragMaxDistance + "=dragMaxDistance,origin=" + origin, "ev.getAction()=" + ev.getAction() + "," + listener.isAllowDrag() + ",ev.getRawY()=" + ev.getRawY());
		// ---
		if (listener.isAllowDrag() && ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (!inLoading) {
				icon.setImageResource(R.drawable.refresh_arrow);
				text.setText(topDragHint);
			}else{
				icon.setImageResource(R.drawable.refresh_loading);
				text.setText(topLoadingHint);
			}
			origin = (int) ev.getRawY();
			max=0;
		}
		if (listener.isAllowDrag() && ev.getAction() == MotionEvent.ACTION_MOVE) {
			int distance=(int) (ev.getRawY() - origin);
			if(distance>max){
				max=distance;
			}
			setTopMargin(distance - dragMaxDistance);
			if (ev.getRawY() - origin > dragMaxDistance * 0.9) {
				if (!inLoading) {
					rotateArrowUp();
				}
			} else {
				if (!inLoading) {
					rotateArrowDown();
				}
			}
			if (ev.getRawY() - origin >= 0) {
				return true;
			}
		}
		if (listener.isAllowDrag() && max >= 5 && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
			boolean loadit = ev.getRawY() - origin > dragMaxDistance * 0.9;
			origin = -1;
			max=-1;
			if (loadit) {
				startLoading();// 加载
			} else {
				closeLoading();// 收起
			}
			return true;
		}

		return super.dispatchTouchEvent(ev);
	}

	//
	private boolean hasRotatedUp = false;
	private boolean inLoading = false;

	private void rotateArrowUp() {
		if (hasRotatedUp) {
			return;
		}
		hasRotatedUp = true;
		Animation ani = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ani.setFillAfter(true);
		ani.setDuration(200);
		icon.clearAnimation();
		icon.startAnimation(ani);
		text.setText(topReleaseHint);
	}

	private void rotateArrowDown() {
		if (!hasRotatedUp) {
			return;
		}
		hasRotatedUp = false;
		Animation ani = new RotateAnimation(180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ani.setFillAfter(true);
		ani.setDuration(200);
		icon.clearAnimation();
		icon.startAnimation(ani);
		text.setText(topDragHint);
	}

	private void startLoading() {
		// --
		if (inLoading) {
			closeLoading();
			return;
		}
		inLoading = true;
		// --
		if (listener != null) {
			listener.onDragReleased();
		}

		icon.setImageResource(R.drawable.refresh_loading);
		text.setText(topLoadingHint);
		Animation ani = new RotateAnimation(0f, 359.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ani.setInterpolator(new LinearInterpolator());
		ani.setRepeatMode(Animation.RESTART);
		ani.setRepeatCount(Animation.INFINITE);
		ani.setDuration(700);
		icon.clearAnimation();
		icon.startAnimation(ani);
		closeLoading(500);
	}

	private void closeLoading(int delay) {
		topContainer.postDelayed(new Runnable() {

			@Override
			public void run() {
				closeLoading();
			}
		}, delay);
	}

	private void closeLoading() {
		topContainer.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (origin != -1) {
					return;
				}
				// ---
				int marginTop = getTopMargin();
				marginTop -= 40;
				setTopMargin(marginTop);
				if (marginTop > -dragMaxDistance) {
					topContainer.postDelayed(this, 10);
				}
			}
		}, 10);
	}

	public void reset() {
		inLoading = false;
		origin = -1;
	}
}