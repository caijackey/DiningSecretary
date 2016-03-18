package com.fg114.main.app.view;

import com.fg114.main.R;
import com.fg114.main.util.UnitUtil;

import android.content.Context;  
import android.content.res.TypedArray;
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.DashPathEffect;  
import android.graphics.Paint;  
import android.graphics.Path;  
import android.graphics.PathEffect;  
import android.util.AttributeSet;  
import android.util.Log;
import android.view.View;  
 
public class LineView extends View {
	
	private AttributeSet attrs=null;
	private float thickness;
	private float dashWidth;
	private float dashGap;
	private int color;
	private int orientation ;

    
    public LineView(Context context, AttributeSet attrs) {  
        super(context, attrs);
        this.attrs=attrs;
        
        TypedArray typeArray = context.obtainStyledAttributes(attrs,R.styleable.line);
        color=typeArray.getColor(R.styleable.line_color, 0xFF000000);
        thickness=typeArray.getDimension(R.styleable.line_thickness, 1);
        dashWidth=typeArray.getDimension(R.styleable.line_dashedWidth, 1);
        dashGap=typeArray.getDimension(R.styleable.line_dashedGap, 0);
        orientation=typeArray.getInt(R.styleable.line_orientation, 1);      
    }  
 
    @Override  
    protected void onDraw(Canvas canvas) {  
          
        super.onDraw(canvas);          
        Paint paint = new Paint(); 
        paint.setStyle(Paint.Style.STROKE);  
        paint.setColor(color);
        paint.setStrokeWidth(thickness);
        float startPointX=0;
        float startPointY=0;
        float stopPointX=0;
        float stopPointY=0;
        float length=0;
        //水平1,垂直0
        if(orientation==1){
        	
        	startPointX=0;
        	startPointY=(this.getMeasuredHeight())/2;
        	
        	length=this.getMeasuredWidth();
        	
            stopPointX=length;
            stopPointY=startPointY;      
        	
        }
        else{
        	startPointX=(this.getMeasuredWidth())/2;
        	startPointY=0;
        	length=this.getMeasuredHeight();
        	
            stopPointX=startPointX;
            stopPointY=length;
        }
        Path path = new Path();    
        path.moveTo(startPointX, startPointY);
        path.lineTo(stopPointX,stopPointY);     
        PathEffect effects = new DashPathEffect(new float[]{dashWidth,dashGap},0);
        paint.setPathEffect(effects);
        canvas.drawPath(path, paint);
        
    }
}