package com.fg114.main.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.fg114.main.app.data.BaseData;
import com.fg114.main.service.dto.CommonTypeDTO;

/**
 * @author Sanvi E-mail:sanvibyfish@gmail.com
 * @version 创建时间：2010-8-31 下午01:22:13
 */
public class CalendarUtil {
	
	public static final String DATE_FORMAT_YYYYMMDD_HHMI = "yyyy-MM-dd HH:mm"; 
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public static List<BaseData> convertDTOListToDataList(List<CommonTypeDTO> list) {
		
		List<BaseData> newList = new ArrayList<BaseData>();
		if (list != null) {
			BaseData data = null;
			for (CommonTypeDTO dto : list) {
				data = new BaseData();
				data.setId(dto.getUuid());
				data.setName(dto.getName());
				newList.add(data);
			}
		}
		return newList;
	}
	
	/**
	 * 返回日期的字符串表示形式：yyyy-MM-dd HH:mm:ss
	 * @param date
	 */
	//added by 徐健君，2011-11-11，考虑性能，自己实现日期固定格式表示
	public static String getDateTimeString(Date date){
		int year=date.getYear()+1900;
		int month=date.getMonth()+1;
		int day=date.getDate();
		int hour=date.getHours();
		int minute=date.getMinutes();
		int second=date.getSeconds();
		
		String timeString=year+"-"
						+(month<10?"0"+month:month)+"-"
						+(day<10?"0"+day:day)+" "
						+(hour<10?"0"+hour:hour)+":"
						+(minute<10?"0"+minute:minute)+":"
						+(second<10?"0"+second:second);
		
		return timeString;
	}
	public static String getDateTimeString(Calendar c){
		String s=String.format("%02d月%02d日 %02d:%02d:%02d", 
				c.get(Calendar.MONTH)+1, 
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND));
		
		return s;
	}
	/**
	 * 返回当前日期的字符串表示形式：yyyy-MM-dd HH:mm:ss
	 * @param date
	 */
	//added by 徐健君，2011-11-11
	public static String getDateTimeString(){
		
		return getDateTimeString(new Date());
	}
	/**
	 * 将形式为：yyyy-MM-dd HH:mm:ss　的日期格式字符串转换为Date对象
	 * @param datetimeString 形式为：yyyy-MM-dd HH:mm:ss 的表示日期时间的字符串
	 */
	//added by 徐健君，2011-11-16
	public static Date getDateFromDateTimeString(String datetimeString) {
		
		try {			
			return format.parse(datetimeString);
		}
		catch(Exception ex){
			
			Log.e("error in getDateFromDateTimeString()",datetimeString+" ---> "+ex.getMessage(),ex);
			return new Date(0);
		}
	}
	/**
	 * 返回特定格式的日期时间字符串表示形式，返回值可以作为文件名的一部分：
	 * 
	 * 参数format取值为：0,1,2
	 * 支持以下格式输出日期时间:
	 * 	0. yyyy-MM-dd HH.mm.ss.ms  （日期时间到毫秒）
	 * 	1. yyyy-MM-dd HH.mm.ss  （日期时间到秒）
	 * 	2. yyyy-MM-dd HH.mm  （日期时间到分钟）
	 *  3. yyyy-MM-dd HH  （日期时间到小时）
	 *  4. yyyy-MM-dd   （日期）
	 *  5. yyyy-MM   （日期到月）
	 *  6. yyyy   （日期年）
	 * @param date 需要格式化的日期
	 * @param format 控制返回的格式
	 *  
	 */
	 /*added by 徐健君，2012-01-13，考虑性能，自己实现日期固定格式表示*/
	public static String getSpecialDateString(Date date,int format){	
		
		
		int year=date.getYear()+1900;
		int month=date.getMonth()+1;
		int day=date.getDate();
		int hour=date.getHours();
		int minute=date.getMinutes();
		int second=date.getSeconds();
		int msecond=(int)(date.getTime()%1000);
		
		
		StringBuilder timeString=new StringBuilder();
		switch(format){
			default:
			case 0: timeString.insert(0,"."+(msecond>100?msecond:(msecond<10?"00"+msecond:"0"+msecond)));
			case 1:	timeString.insert(0,"."+(second<10?"0"+second:second));
			case 2: timeString.insert(0,"."+(minute<10?"0"+minute:minute));
			case 3: timeString.insert(0," "+(hour<10?"0"+hour:hour));
			case 4: timeString.insert(0,"-"+(day<10?"0"+day:day));
			case 5: timeString.insert(0,"-"+(month<10?"0"+month:month));
			case 6: timeString.insert(0,year);
		}
		
		return timeString.toString();
	}
	//返回当前日期的特殊格式串
	public static String getSpecialDateString(int format){
		return getSpecialDateString((new Date()),format);
	}
	public static String getSpecialDateString(Date date){
		return getSpecialDateString(date,0);
	}
	public static String getSpecialDateString(){
		return getSpecialDateString((new Date()),0);
	}

	/**
	 * 判断毫秒数是不是在今天，只支持GMT+8时区
	 * @param value
	 * @return
	 */
	public static boolean isToday(long value) {
		long today=System.currentTimeMillis();
		long offset=8*60*60*1000;
		return (today+offset)/86400000==(value+offset)/86400000;
	}
}
