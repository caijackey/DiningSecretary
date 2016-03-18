package com.fg114.main.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.*;

import android.text.TextUtils;

/**
 * 输入数据check
 * 
 * @author zhangyifan
 * 
 */
public class CheckUtil {

	private static final String[] invalidChar = { "&", "?", "=", "/", ":", ";", ".", "\\", ",", "<", ">", "~", "`", "!", "@", "#", "$", "%", "^", "。", "，", "！", "？", "：" };
	private static List<String> invalidCharList = new ArrayList<String>();
	private static char[] numbersAndLetters = null;
	private static Object initLock = new Object();
	private static Random randGen = null;

	static {
		invalidCharList = java.util.Arrays.asList(invalidChar);
	}

	public static boolean isInvalidChar(String text) {
		return invalidCharList.contains(text);
	}

	/**
	 * 是否是字母或数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumOrWord(String str) {
		String regularExpression = "[a-z0-9A-Z]*";

		if (isEmpty(str)) {
			return false;
		} else if (!str.matches(regularExpression)) {
			return false;
		}
		return true;
	}

	/**
	 * 判断email格式
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmail(String str) {
		String regularExpression = "^([a-z0-9A-Z]+[-|//.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?//.)+[a-zA-Z]{2,}$";

		if (isEmpty(str)) {
			return false;
		} else if (!str.matches(regularExpression)) {
			return false;
		}
		return true;
	}

	/**
	 * 字符数是否在范围之间
	 * 
	 * @param str
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isLengthBetween(String str, int begin, int end) {
		if (isEmpty(str)) {
			return false;
		} else if (str.length() < begin || str.length() > end) {
			return false;
		}
		return true;
	}

	/**
	 * 是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str != null && !"".equals(str.trim())) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是同一天
	 * 
	 * @param day1
	 * @param day2
	 * @return
	 */
	public static boolean isSameDay(long day1, long day2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String str1 = sdf.format(new Date(day1));
		String str2 = sdf.format(new Date(day2));
		if (str1.equals(str2)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是电话号码
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isPhone(String phone) {
		if (Pattern.matches("\\d+\\-?\\d+", phone == null ? "" : phone)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是double
	 * 
	 * @param doubleNumber
	 * @return
	 */
	public static boolean isDouble(String doubleNumber) {
		try {
			Double.parseDouble(doubleNumber);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 是否是手机号码
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isCellPhone(String phone) {
		if (Pattern.matches("\\d{11}", phone == null ? "" : phone)) {
			return true;
		}
		return false;
	}

	/**
	 * 产生不重复随机数
	 * 
	 * @param length
	 * @return String类型随机数
	 */
	public static final String randomString(int length) {
		if (length < 1) {
			return null;
		}
		// Init of pseudo random number generator.
		if (randGen == null) {
			synchronized (initLock) {
				if (randGen == null) {
					randGen = new Random();
					// Also initialize the numbersAndLetters array
					numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
				}
			}
		}
		// Create a char buffer to put random letters and numbers in.
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
		}
		return new String(randBuffer);
	}

}
