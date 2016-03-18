package com.fg114.main.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * 用于测试的工具类
 * @author xujianjun, 2012-04-25
 */
public class TestUtil {

	/**
	 * 向本机cache目录里保存字符串日志，noPathFileName是不含路径的文件名
	 * 日志总是追加到文件的末尾。
	 * @param noPathFileName 不含路径的文件名
	 * @param message 要计入文件的字符串
	 */
	public static void save(String noPathFileName,String message){
		String cachePath = ContextUtil.getContext().getCacheDir().getPath();
		save(cachePath, noPathFileName,message);
		
	}
	
	/**
	 * 向本机cachePath参数指定的目录里保存字符串日志，noPathFileName是不含路径的文件名
	 * 
	 * 日志总是追加到文件的末尾。
	 * @param cachePath 文件的路径名
	 * @param noPathFileName 不含路径的文件名
	 * @param message 要计入文件的字符串
	 */
	public static synchronized void save(String cachePath, String noPathFileName,String message){
		
		FileWriter fout =null;
		try {
			fout=new FileWriter(cachePath+File.separator+noPathFileName,true);
			fout.write(message);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		finally{
			if(fout!=null){
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	/**
	 * 向本机cache目录里保存字符串日志，文件名格式自动生成为：log_<当前日期>.log
	 * 
	 * 日志总是追加到文件的末尾。
	 * @param message 要计入文件的字符串
	 */
	public static void log(String message){		
		log("log_"+CalendarUtil.getSpecialDateString(4)+".log",message);
	}
	/**
	 * 向本机cache目录里保存字符串日志，每次调用时将写入一行，行首自动添加日期时间信息
	 * 
	 * 日志总是追加到文件的末尾。
	 * @param noPathFileName 不含路径的文件名
	 * @param message 要计入文件的字符串
	 */
	public static void log(String noPathFileName,String message){
		save(noPathFileName,"["+CalendarUtil.getSpecialDateString()+"] "+message+System.getProperty("line.separator"));
	}
	/**
	 * 向本机cache目录里保存字符串日志，每次调用时将写入一行，行首自动添加日期时间信息
	 * 
	 * 日志总是追加到文件的末尾。
	 * @param cachePath 文件的路径名
	 * @param noPathFileName 不含路径的文件名
	 * @param message 要计入文件的字符串
	 */
	public static void log(String path,String noPathFileName,String message){
		save(path,noPathFileName,"["+CalendarUtil.getSpecialDateString()+"] "+message+System.getProperty("line.separator"));
	}
}
