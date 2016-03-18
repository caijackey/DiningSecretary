package com.fg114.main.util;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

public class HanziUtil {

	private static final String PATTERN = "^[A-Za-z0-9]+$";
	private static final String SHARP = "#";
	

	private static HashMap<String, String> mMap = new HashMap<String, String>(5000);     
	public static synchronized void init(Context context) {
		if (mMap.size() > 0) {
			return;
		}
		String data = IOUtils.readStringFromAssets(context, "d");
//		data = data.replace("@", "");
		String[] str = data.split("\r\n");
		if (str.length > 0) {
			for (String s : str) {
//				String str1 = s.substring(2, s.indexOf(",") - 1);
//				String str2 = s.substring(s.indexOf(",") + 2, s.lastIndexOf("}") - 1);
				String[] tmp = s.trim().split(",");
				if (tmp.length != 2) {
					continue;
				}
				if (!tmp[1].matches(PATTERN)) {
					continue;
				}
				mMap.put(tmp[0], tmp[1]);
			}
		}
	}
	
	public static String getPinyin(Context context, String input) {
		if (mMap.size() == 0) {
			init(ContextUtil.getContext());
		}
		StringBuffer sb = new StringBuffer();
		String pinyin = "";
		for (char ch : input.toCharArray()) {
			if (String.valueOf(ch).matches(PATTERN)) {
				pinyin = String.valueOf(ch);
			}
			else {
				pinyin = mMap.get(String.valueOf(ch));
			}
			if (CheckUtil.isEmpty(pinyin)) {
				pinyin = String.valueOf(ch);
			}
			if (pinyin != null) {
				if (!sb.toString().equals("")) {
					sb.append(" ");
				}
				sb.append(pinyin);
			}
		}
		return sb.toString().toLowerCase();
	}
	
    public static String getFirst(String pinyin) {
    	if (pinyin==null||pinyin.length() < 1) {
    		return SHARP;
    	}
    	String fistLetter = pinyin.substring(0, 1);
    	if (!fistLetter.matches(PATTERN)) {
    		return SHARP;
    	}
    	return fistLetter;
    }
    //判断一个string 是否和一个keyword相匹配，考虑拼音匹配，首字母匹配等
    //xujianjun,2012-03-28
	public static boolean doesStringMatchKeywords(String str, String keywords) {
		
		if(str==null||keywords==null||str.trim().equals("")||keywords.trim().equals("")){
			return false;
		}
		//普通包含匹配
		if(str.toUpperCase().contains(keywords.trim().toUpperCase())){
			return true;
		}
		//拼音包含匹配
		if(getPinyin(str.trim()).contains(getPinyin(keywords.trim()))){
			return true;
		}
		//拼音首字母匹配，关键字是字母时才应该倾向于这种匹配
		//关键词必须可以取到两个以上的首字符才执行首字符匹配，防止只输入一个字时产生过多的匹配
		String firstLettersOfKeywords = getPinyinFirstLetters(keywords.trim());
		if (firstLettersOfKeywords != null 
				&& firstLettersOfKeywords.length() > 1
				&& getPinyinFirstLetters(str.trim()).contains(firstLettersOfKeywords)) {
			
			return true;
	
		}
		if (getPinyinFirstLetters(str.trim()).contains(getPinyin(keywords.trim()))) {
			return true;
		}

		return false;
	}
	//获得字符串的拼音首字母串，英文数字原封不动
	public static String getPinyinFirstLetters(String str){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<str.length();i++){
			String pinyin=getPinyin(str.charAt(i));	
			if(pinyin!=null&&pinyin.length()>0){
				sb.append(pinyin.charAt(0));			
			}		
		}
		return sb.toString();
	}
	//获得一个字符的拼音，英文数字原封不动
	public static String getPinyin(char ch){
		if (mMap.size() == 0) {
			init(ContextUtil.getContext());
		}
		if (String.valueOf(ch).matches(PATTERN)) {
			return String.valueOf(ch);
		}
		else {
			return mMap.get(String.valueOf(ch));
		}
	}
	//获得一个字符串的拼音，英文数字原封不动
	public static String getPinyin(String input) {
		if (mMap.size() == 0) {
			init(ContextUtil.getContext());
		}
		StringBuffer sb = new StringBuffer();
		String pinyin = "";
		for (char ch : input.toCharArray()) {
			if (String.valueOf(ch).matches(PATTERN)) {
				pinyin = String.valueOf(ch);
			}
			else {
				pinyin = mMap.get(String.valueOf(ch));
			}
			if (CheckUtil.isEmpty(pinyin)) {
				pinyin = String.valueOf(ch);
			}
			if (pinyin != null) {
				sb.append(pinyin);
			}
		}
		return sb.toString().toLowerCase();
	}
}
