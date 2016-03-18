package com.fg114.main.util;

import java.util.*;

/**
 * JS交互工具类
 * @author wufucheng
 *
 */
public class JavaScriptInterface {

	private HashMap<String, String> mValueMap = new HashMap<String, String>();

	public void set(String key, String value) {
		mValueMap.put(key, value);
	}

	public String get(String key) {
		return mValueMap.get(key);
	}
}