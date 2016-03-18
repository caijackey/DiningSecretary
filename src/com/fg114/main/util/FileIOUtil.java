package com.fg114.main.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

/**
 * 文件读写操作
 * @author zhangyifan
 *
 */
public class FileIOUtil {

	/**
	 * 写入文件
	 * @param fos
	 * @param json
	 */
	public static void fileWrite(Context context, String id, String json) {
		
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(id, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		byte[] jsonByteArray = json.getBytes();
		
		try {
			fos.write(jsonByteArray);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读文件
	 * @param fis
	 * @return
	 */
	public static String fileRead(Context context, String id, String defaultStr) {
		
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(id);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return defaultStr;
		}
		
		String result = defaultStr;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		try {
			while ((length = (fis.read(buffer))) != -1) {
				baos.write(buffer, 0, length);
			}
			result = new String(baos.toByteArray());
			baos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
