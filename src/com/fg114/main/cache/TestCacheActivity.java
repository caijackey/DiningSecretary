package com.fg114.main.cache;
import java.net.URL;
import java.util.Random;

import javax.crypto.Cipher;  
import javax.crypto.spec.SecretKeySpec;

import com.fg114.main.util.ViewUtils;

import android.view.*;
import android.view.View.*;
import android.graphics.*;
import android.app.Activity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import java.io.*;


public class TestCacheActivity extends Activity{
	
	public void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);

		String text="测试缓存功能";
		
		final LinearLayout L = new LinearLayout(this);
		//LinearLayout L=(LinearLayout)this.findViewById(R.id.mainLayout);
		L.setOrientation(LinearLayout.VERTICAL);

		TextView tv = new TextView(this);
		tv.setText(text);
		tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		L.addView(tv);

		
/*		TextView tv2 = new TextView(this);
		tv2.setText(text);
		tv2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		L.addView(tv2);*/
		
/*		//URL测试
		
		String url="http://java.sun.com/webapps/bugreport/hello.jsp";
		String dis="error";
		try {
			
			URL u=new URL(url);	
			dis=u.getFile();
		}
		catch(Exception ex){
			dis="error";
			
		}
		tv2.setText(String.valueOf("132retR-_%.".matches(".*[^\\w\\-%].*")));
		
		
		BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	byte[] bytes=new byte[255];
        Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes),null, options);
        tv2.setText("是图片？"+ String.valueOf(bitmap!=null));
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
        //tv2.setTypeface(Typeface)
        
        this.setContentView(L);*/
   
        //////////////////////////////value_cache test////////////////////////////////////////
/*        String dir=this.getClass().getName();
        String key="myfirst";
        ValueCacheUtil vc=ValueCacheUtil.getInstance(this);
        
        tv2.setText(String.valueOf(vc.exists(dir, key)));
        vc.add(dir, key, "你好啊,世界！，hello world");
        tv.setText(vc.get(dir, key).value);
        
       //vc.update(dir, key, "你好hello world","100");
        
        
        vc.add(dir, key+2, "我成功了！","222","DiningSecretary_V3",15);

        tv.setText(vc.get(dir, key+2).toString());
        Log.d("value object",vc.get(dir, key+2).toString());

        
        vc.cleanCache();*/
        //////////////////////////////file_cache test//////////////////////////////////////////
        //String furl="http://java.sun.com/webapps/bugreport/hello.jsp";
 /***********************************************************************
  * 
  * 图片缓存测试 
  * 
  * *********************************************************************/ 
		Button file_btn=new Button(this);
		file_btn.setText("点我依次显示图片");
		file_btn.setOnClickListener(new View.OnClickListener(){
			int count=0;
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				
				if(count>5) count=0;
		        
				String[] url = new String[6];
				// 大
				url[0] = "http://upload3.95171.cn/albumpicimages/20101221/04a20f7b-e518-4690-914a-9576707fd955.jpg";
				// 大
				url[1] = "http://upload2.95171.cn/albumpicimages/20101221/7fe9e249-87ef-4ba3-9c21-aae96e82b327.jpg";
				// 中
				url[2] = "http://upload2.95171.cn/Topics/20110316/C52I14O57253/04ab049c-738a-4850-804c-4062541c3f0a.jpg";
				// 中
				url[3] = "http://upload1.95171.cn/Topics/20110316/C52I14O57253/fc7bfb35-a75a-4162-bdbb-d6a3299fb433.jpg";
				// 中
				url[4] = "http://upload2.95171.cn/Topics/20110316/C52I14O57253/006ab02e-c7a1-4658-aa38-29a33c8068b0.jpg";
				// 中
				url[5] = "http://upload2.95171.cn/albumpicimages/20110104/261fdb97-814e-4663-997c-16f399a251cb.jpg";
		        
				String furl=url[count];
		        
		        FileCacheUtil fc=FileCacheUtil.getInstance();
		        String dir=v.getContext().getClass().getName();		        
		        FileObject fo=fc.get(dir, furl);		        
		        //Log.d("file object","刚开始存在吗?"+(fo==null?"null不存在":( fo.toString())));
		        //Log.d("file object","get完了。判断存在否？"+String.valueOf(fc.exists(dir, furl)));
		        

		        ImageView iv=new ImageView(v.getContext());
		        LayoutParams lp=new LayoutParams(50,50);
		        iv.setLayoutParams(lp);
		        
		        Bitmap bmp=fo.getContentAsBitmap();
		        iv.setImageBitmap(bmp);
		        
		        Log.d("file object","图片大小"+fo.size());
		        L.addView(iv);
		        count++;
		        Log.d("MMMM",fc.mmCache+"");
		        
		        //bmp.recycle();
		        
		        
			}
			
			
		});
		 /***********************************************************************
		  * 
		  * 文字缓存测试 
		  * 
		  * *********************************************************************/ 
		Button file_value=new Button(this);
		file_value.setText("点我依次读取文字");
		file_value.setOnClickListener(new View.OnClickListener(){
			int count=1;
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Random ran=new Random();
				if(count>5) count=0;
		        
				String text="测试用文字";
				String value="[value " + count + "]"+text +"的值。"+ran.nextInt(1000);
				String key="key_"+count;
		        
		        ValueCacheUtil vc=ValueCacheUtil.getInstance(v.getContext());
		        
		        String dir=v.getContext().getClass().getName();		        
		        ValueObject vo=vc.get(dir, key);
		        if(vo==null){
		        	vc.add(dir, key, value);
		        	vo=vc.get(dir, key);
		        }
		        if(count==3){
		        	
		        	vc.add(dir, key, value);
		        	vc.update(dir, key, value+"　更新！", "2", "beta 1.0", 2);
		        	 Log.d("update",vc.mmCache+"");
		        	
		        	vo=vc.get(dir, key);
		        }
		        

		        TextView tv=new TextView(v.getContext());
		        LayoutParams lp=new LayoutParams(LayoutParams.FILL_PARENT,50);
		        tv.setLayoutParams(lp);
		        
		        tv.setText("->"+vo.getValue());
		        
		        Log.d("value object","文字大小(bytes)"+vo.size());
		        L.addView(tv);
		        count++;
		        Log.d("VVVVVV",vc.mmCache+"");
		        System.out.println(vo);
		        
		        //bmp.recycle();
		        
		        
			}
			
			
		});
		////////////////////////////////////////
		
		L.addView(file_btn);
		L.addView(file_value);
		this.setContentView(L);
	}

}
