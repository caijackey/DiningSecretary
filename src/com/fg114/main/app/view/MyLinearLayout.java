/**
 * 圆角LinearLayout的使用
 * 
 * 必须在构造函数里面添加setWillNotDraw(false); 否则不会调用onDraw();  同时也注意，如果不需要圆角，不要用此控件，因为onDraw调用会消耗cpu
 * 在value/attrs.xml已经定义了两个属性 roundType和roundRadius,此控件初始化是会获取这两个参数，见构造函数
 * roundType 0:默认无圆角 1:四周圆角  2:只有上面是圆角
 * roundRadius 圆角半径 传入的是dip 会自动转成px（已经乘了density）
 * 
 * 请注意此圆角对背景无效！
 * 
 * XML中的使用方法
 * XML里面最外面的控件里面定义命名空间xmlns:local="http://schemas.android.com/apk/res/com.fg114.main"
 * 然后直接修改roundtype和roundradius数据
 * <LinearLayout
 *	xmlns:android="http://schemas.android.com/apk/res/android"
 *  xmlns:local="http://schemas.android.com/apk/res/com.fg114.main"
 *	android:layout_width="fill_parent"
 *	android:layout_height="fill_parent"
 *	android:orientation="vertical"
 *	>
 *  .
 *  .
 *  .
 *  <com.fg114.main.app.view.MyLinearLayout
 *			android:id="@+id/foodlayout"
 *			local:roundType="2"
 *			local:roundRadius="10dip"
 *			android:layout_width="fill_parent"
 *			android:layout_height="fill_parent"  />
 *  .
 *  .
 *  .
 *  </LinearLayout>
 */

package com.fg114.main.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.fg114.main.R;

public class MyLinearLayout extends LinearLayout {
	
	/**
	 * 设置圆角, 0:无圆角 默认值 1:四个角全部圆角 2:只有上面两个角圆角 
	 */
	public int roundType = 0;
	/**
	 * 设置圆角半径, 默认10.0f
	 */
	public float roundRadius = 10.0f; 
	
	public MyLinearLayout(Context context) {
		super(context);
		
		//必须添加，否则不调用onDraw
		setWillNotDraw(false);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.RoundCorner);  
        roundType = attr.getInt(R.styleable.RoundCorner_roundType, 0);
        roundRadius = (float) attr.getDimensionPixelSize(R.styleable.RoundCorner_roundRadius, 10);
        
        //必须添加，否则不调用onDraw
        setWillNotDraw(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(roundType==1){
			Path clipPath = new Path();
			RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
			clipPath.addRoundRect(rect, roundRadius, roundRadius, Path.Direction.CW);
			canvas.clipPath(clipPath);
		}
		else if(roundType==2){
			Path clipPath = new Path();
			RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
			float [] corner = {roundRadius, roundRadius, roundRadius, roundRadius, 0, 0, 0, 0};
			clipPath.addRoundRect(rect, corner, Path.Direction.CW);
			canvas.clipPath(clipPath);
		}
		super.onDraw(canvas);

	}
	

}
