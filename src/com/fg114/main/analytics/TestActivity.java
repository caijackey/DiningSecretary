//package com.fg114.main.analytics;
//import java.net.URL;
//import java.util.Random;
//
//import javax.crypto.Cipher;  
//import javax.crypto.spec.SecretKeySpec;
//import android.view.*;
//import android.view.View.*;
//import android.graphics.*;
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.*;
//import java.io.*;
//import com.google.android.apps.analytics.*;
//
//
//public class TestActivity extends Activity{
//	
//	public void onCreate(Bundle savedInstanceState)  {
//		super.onCreate(savedInstanceState);
//
//		String text="测试ga功能";
//		GoogleAnalyticsTracker tracker=GoogleAnalyticsTracker.getInstance();
//		tracker.startNewSession("UA-29889287-1", this);
//		tracker.setReferrer("");
//		tracker.trackPageView("/pathGirl/test1.htm");
//		tracker.trackPageView("/pathGirl/test2.htm");
//		tracker.trackPageView("/pathBoy/testA.page");
//		tracker.trackPageView("/pathBoy/testB.page");
//		boolean result=tracker.dispatch();
//		tracker.stopSession();
//
//		
//		final LinearLayout L = new LinearLayout(this);
//		//LinearLayout L=(LinearLayout)this.findViewById(R.id.mainLayout);
//		L.setOrientation(LinearLayout.VERTICAL);
//
//		TextView tv = new TextView(this);
//		tv.setText(text+result);
//		tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//		L.addView(tv);
//
//
//		this.setContentView(L);
//	}
//
//}
