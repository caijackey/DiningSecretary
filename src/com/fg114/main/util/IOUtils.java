package com.fg114.main.util;

import java.io.*;
import java.net.*;

import com.fg114.main.app.Settings;

import android.content.*;
import android.os.Environment;

/**
 * IO操作
 * @author wfc
 * 
 */
public class IOUtils {
	
	private static final String TAG = IOUtils.class.getName();

	private static final int BUFFER_SIZE = 1024; // 流转换的缓存大小
	private static final int CONNECT_TIMEOUT = 3000; // 从网络下载文件时的连接超时时间
	
	/**
	 * 从Assets读取文字
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String readStringFromAssets(Context context, String fileName) {
		return readStringFromAssets(context, fileName, Encoding.UTF8);
	}
	/**
	
	 * 从Assets读取文字
	 * @param context
	 * @param fileName
	 * @param encoding
	 * @return
	 */
	public static String readStringFromAssets(Context context, String fileName, String encoding) {
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			is = context.getAssets().open(fileName);
			byte[] buffer = new byte[BUFFER_SIZE];

			baos = new ByteArrayOutputStream();
			while (true) {
				int read = is.read(buffer);
				if (read == -1) {
					break;
				}
				baos.write(buffer, 0, read);
			}
			String result = baos.toString(encoding);
			return result;
		} catch (Exception e) {
			return "";
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 从资源中读取文字
	 * @param context
	 * @param resId
	 * @return
	 */
	public static String readStringFromRes(Context context, int resId) {
		return readStringFromRes(context, resId, Encoding.UTF8);
	}
	
	/**
	 * 从资源中读取文字
	 * @param context
	 * @param resId
	 * @param encoding
	 * @return
	 */
	public static String readStringFromRes(Context context, int resId, String encoding) {
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			is = context.getResources().openRawResource(resId);
			byte[] buffer = new byte[BUFFER_SIZE];

			baos = new ByteArrayOutputStream();
			while (true) {
				int read = is.read(buffer);
				if (read == -1) {
					break;
				}
				baos.write(buffer, 0, read);
			}
			String result = baos.toString(encoding);
			return result;
		} catch (Exception e) {
			return "";
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 从指定路径的文件中读取Bytes
	 */
	public static byte[] readBytes(String path) {
		File file = new File(path);
		return readBytes(file);
	}

	/**
	 * 从指定资源中读取Bytes
	 */
	public static byte[] readBytes(Context context, int resId) {
		InputStream is = null;
		try {
			is = context.getResources().openRawResource(resId);
			return readBytes(is);
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}

	/**
	 * 从File中读取Bytes
	 */
	public static byte[] readBytes(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return readBytes(fis);
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return null;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}

	/**
	 * 从Url中读取Bytes
	 */
	public static byte[] readBytes(URL url) {
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.connect();
			is = conn.getInputStream();
			return readBytes(is);
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return null;
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}

	/**
	 * 从InputStream中读取Bytes
	 */
	public static byte[] readBytes(InputStream is) {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = 0;
			while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				baos.write(buffer, 0, length);
				baos.flush();
			}
			return baos.toByteArray();
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return null;
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}

	/**
	 * 将InputStream写入File
	 */
	public static boolean writeToFile(File file, InputStream is) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = 0;
			while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				fos.write(buffer, 0, length);
				fos.flush();
			}
			return true;
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}
	
	public static boolean writeToFile(File file, String text) {
		return writeToFile(file, text, Encoding.UTF8);
	}
	
	public static boolean writeToFile(File file, String text, String encoding) {
		try {
			return writeToFile(file, (text+"\r\n").getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			LogUtils.logD(TAG, e.getMessage());
			return false;
		}
	}
	
	public static boolean writeToFile(File file, byte[] buffer) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
			fos.write(buffer);
			return true;
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}
	
	public static boolean writeToSD(String fileName, String text) {
		try {
			String strPath = Environment.getExternalStorageDirectory() + "/" + fileName;
			File fFile = new File(strPath);
			if (!fFile.exists()) {
				fFile.createNewFile();
			}
			return writeToFile(fFile, text);
		} catch (IOException e) {
			LogUtils.logD(TAG, e.getMessage());
			return false;
		}
	}
	
	/**
	 * 下载文件至存储卡
	 */
	public static File downloadFileToSD(String strUrl, String dirPath) {
		return downloadFile(strUrl, android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + dirPath, null);
	}
	
	/**
	 * 下载文件至存储卡
	 */
	public static File downloadFileToSD(String strUrl, String dirPath, String saveName) {
		return downloadFile(strUrl, android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + dirPath, saveName);
	}
	
	/**
	 * 下载文件至指定目录
	 */
	public static File downloadFile(String strUrl, String dirPath) {
		return downloadFile(strUrl, dirPath, null);
	}

	/**
	 * 下载文件至指定目录
	 * @param strUrl 文件的url
	 * @param dirPath 存储文件的目录
	 * @param saveName 存储的文件名
	 */
	public static File downloadFile(String strUrl, String dirPath, String saveName) {
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			String fileEx = strUrl.substring(strUrl.lastIndexOf(".") + 1, strUrl.length()).toLowerCase();
			String fileName = strUrl.substring(strUrl.lastIndexOf("/") + 1, strUrl.lastIndexOf("."));

			URL myURL = new URL(strUrl);
			conn = (HttpURLConnection) myURL.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.connect();
			is = conn.getInputStream();

			if (saveName == null) {
				saveName = fileName + "." + fileEx;
			}
			File file = new File(dirPath, saveName);
			writeToFile(file, is);
			return file;
		} catch (Exception e) {
			LogUtils.logD(TAG, e.getMessage());
			return null;
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				LogUtils.logD(TAG, e.getMessage());
			}
		}
	}
	
	public static void writeTestInfo(Context context, String fileName, String text) {
		boolean isTest = ActivityUtil.isTestDev(context);
		if (isTest) {
			IOUtils.writeToSD(fileName, text);
		}
	}
}
