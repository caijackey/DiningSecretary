package com.fg114.main.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fg114.main.app.Settings;

/**
 * 共享数据存取类
 * @author zhangyifan
 * @email  zhangyifan@95171.cn 
 * @date   2011.4.13
 */
public class SharedprefUtil {
	
	private static SharedPreferences pref;
	
	static {
		pref = ContextUtil.getContext().getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
	}
	
	public static boolean contains(Context ctx, String key) {
		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		return settings.contains(key);
	}
	
	/**
	 * 重设指定字段
	 * @param ctx
	 * @param key
	 */
	public static void resetByKey(Context ctx, String key) {
		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
		editor.commit(); 
	}

	/**
	 * 共享数据中存储数据
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void save(Context ctx,String key,String value){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
	    editor.putString(key,value);
	    editor.commit(); 
	}
	
	/**
	 * 共享数据中获取数据
	 * @param ctx
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static String get(Context ctx,String key,String defVal){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
	    return pref.getString(key,defVal);
	}
	
	/**
	 * 共享数据中存储数据
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void saveLong(Context ctx, String key, long value){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
	    editor.putLong(key, value);
	    editor.commit(); 
	}
	
	/**
	 * 共享数据中获取数据
	 * @param ctx
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static long getLong(Context ctx, String key, long defVal){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
	    return pref.getLong(key, defVal);
	}
	
	/**
	 * 共享数据中存储数据
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void saveInt(Context ctx, String key, int value){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
	    editor.putInt(key, value);
	    editor.commit(); 
	}
	
	/**
	 * 共享数据中获取数据
	 * @param ctx
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static int getInt(Context ctx, String key, int defVal){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
	    return pref.getInt(key, defVal);
	}
	
	/**
	 * 共享数据中存储布尔型数据
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void saveBoolean(Context ctx,String key,boolean value){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = pref.edit();
	    editor.putBoolean(key,value);
	    editor.commit(); 
	}
	
	/**
	 * 共享数据中获取布尔型数据
	 * @param ctx
	 * @param key
	 * @param defVal
	 * @return
	 */
	public static boolean getBoolean(Context ctx,String key,boolean defVal){
//		SharedPreferences settings = ctx.getSharedPreferences(Settings.CONFIG_FILE, Context.MODE_WORLD_READABLE);
	    return pref.getBoolean(key,defVal);
	}
	
	/*public static int getLocalVersion(int ver){
	    return pref.getInt(Settings.LOCAL_VERSION, ver);
	}
	
	public static void setLocalVersion(int ver) {
		SharedPreferences.Editor editor = pref.edit();
	    editor.putInt(Settings.LOCAL_VERSION, ver);
	    editor.commit(); 
	}*/
}
