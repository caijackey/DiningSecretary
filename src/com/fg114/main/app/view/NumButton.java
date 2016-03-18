package com.fg114.main.app.view;

import java.util.*;

import com.fg114.main.R;
import com.fg114.main.util.ImageUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RadioButton;

public class NumButton extends RadioButton {

	private int textMargin = 6;
	
	
	private Paint mPaint = new Paint();
	private int mNum = 0;
	private List<String> mIdList = new ArrayList<String>();

	public NumButton(Context paramContext) {
		super(paramContext);
		initPaint();
	}

	public NumButton(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initPaint();
	}
	
	public NumButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public int getNum() {
		return mNum;
	}

	public void setNum(int num) {
		mNum = num;
		invalidate();
	}
	
	public List<String> getIdList() {
		return mIdList;
	}

	public void setIdList(List<String> list) {
		mIdList = list;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mNum == 0) {
			return;
		}
		int width = 0;
		int height = 0;

		String strNum = String.valueOf(mNum);
		Drawable drawableOrg = getResources().getDrawable(R.drawable.dish_list_type_bt_circle);
		int textWidth = (int) (mPaint.measureText(strNum) + 0.5f);
		FontMetrics fm = mPaint.getFontMetrics();
		int textHeight = (int) Math.ceil(fm.descent - fm.ascent);
		Drawable drawableNew;
		if (textWidth <= drawableOrg.getIntrinsicWidth()
				&& textHeight <= drawableOrg.getIntrinsicHeight()) {
			width = drawableOrg.getIntrinsicWidth();
			height = drawableOrg.getIntrinsicHeight();
			drawableNew = drawableOrg;
		} else {
			width = textWidth > drawableOrg.getIntrinsicWidth() ? textWidth : drawableOrg
					.getIntrinsicWidth();
			height = textHeight > drawableOrg.getIntrinsicHeight() ? textHeight : drawableOrg
					.getIntrinsicHeight();
			if (width < height) {
				width = height;
			}
			Bitmap bmp = ImageUtil.drawableToBitmap(drawableOrg);
			drawableNew = ImageUtil.bitmapToDrawable(ImageUtil.zoomImg(bmp, textWidth, height));
			bmp = null;
		}

//		int top = getCompoundPaddingTop();
//		int left = getWidth() - getCompoundPaddingRight() - width;
		int top = 0;
		int left = getMeasuredWidth() - width;
		drawableNew.setBounds(left, top, left + width, top + height);
		drawableNew.draw(canvas);
		Rect rectText = new Rect();
		mPaint.getTextBounds(strNum, 0, strNum.length(), rectText);
		canvas.drawText(strNum, left + width / 2,
				top + height - (height / 2 - Math.abs(rectText.top - rectText.bottom) / 2),
				this.mPaint);
	}

	private void initPaint() {
		int textSize = (int) ImageUtil.getPX(getContext(), 12);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setColor(Color.WHITE);
		textMargin = (int) ImageUtil.getPX(getContext(), textMargin);
	}
}
