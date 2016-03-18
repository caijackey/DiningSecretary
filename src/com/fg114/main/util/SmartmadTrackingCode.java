package com.fg114.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 广告主Tracking Code
 */
public class SmartmadTrackingCode {
	static final String TAG = "SmartmadTrackingCode";
	//上次程序启动的TaskId
	static int lastTaskId = -1;
	/**
	 * 服务器响应监听器,暂时不需要,先隐藏
	 */
	public static interface SmartmadTrackingCodeListener {
		/**
		 * 服务器返回码-Success
		 */
		int Success = 0;
		/**
		 * 服务器返回码-Failure
		 */
		int Failure = 1;
		/**
		 * 服务器返回码-Closure
		 */
		int Closure = 2;
		
		/**
		 * 服务器响应事件
		 * @param responseCode
		 */
		void onResponse(int responseCode);
	}
	
	private static SmartmadTrackingCodeListener listener;
	/**
	 * 设置服务器响应监听器,暂时不需要,先隐藏
	 * @param l
	 */
	public static final void setListener(SmartmadTrackingCodeListener l) {
		listener = l;
	}
	
	private static final String SERVER_URL = "http://api.madserving.com/tc.m";
	
	/**
	 * 开始Tracking Code
	 * @param context
	 */
	public static final void startTracking(final Context context,final String aid) {
		if (!checkPermission(context)) {
			return;
		}
		int isClosure = isClosure(context);
		if (isClosure>0) {
			Log.e(TAG, "closure! can't start!");
			return;
		}
		int nowTaskId = ((Activity)context).getTaskId();
		if (lastTaskId==nowTaskId) {
			//同一个程序生命周期只能调用一次
			Log.e(TAG, "same life cycle! can't start again!");
			return;
		}
		lastTaskId = nowTaskId;
		new Thread() {
			final int TIME_OUT_MAX = 30*60*1000;
			int timeOut = 30*1000;
			@Override
			public void run() {
				String url = getTrackingCodeUrl(context,aid);
				//Log.i(TAG, "url = "+url);
				while(true) {
					try {
						int responseCode = HttpRequester.request(context, url);
						//Log.i(TAG, "responseCode = "+responseCode);
						if (listener!=null) {
							listener.onResponse(responseCode);
						}
						if (responseCode==SmartmadTrackingCodeListener.Success) {
							break;
						}
						else if (responseCode==SmartmadTrackingCodeListener.Closure) {
							setClosure(context);
							break;
						}
						else {//SmartmadTrackingCodeListener.Failure
							//失败重试,等待时间加倍,如果超出最大等待时间退出
							int sleepTime = timeOut;
							timeOut *= 2;
							if (timeOut>TIME_OUT_MAX) {
								break;
							}
							sleep(sleepTime);
						}
					} catch(Throwable t) {
						t.printStackTrace();
					}
				}
				
			}
			
		}.start();
	}
	
	//权限检查,Tracking Code正常使用必须的权限(访问互联网,网络状态,手机信息)
	private static boolean checkPermission(Context context) {
		try {
			context.enforceCallingOrSelfPermission(
					android.Manifest.permission.READ_PHONE_STATE,
					"Tracking Code requires permission to android.Manifest.permission.READ_PHONE_STATE!");
			context.enforceCallingOrSelfPermission(
					android.Manifest.permission.ACCESS_NETWORK_STATE,
					"Tracking Code requires permission to android.Manifest.permission.ACCESS_NETWORK_STATE!");
			context.enforceCallingOrSelfPermission(
					android.Manifest.permission.INTERNET,
					"Tracking Code requires permission to android.Manifest.permission.INTERNET!");
			return true;
		} catch (Throwable s) {
			Log.e(TAG, s.getMessage());
			return false;
		}
	}
	
	//检查是否是Closure状态
	private static int isClosure(Context context) {
		SharedPreferences preferences = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
		return preferences.getInt("Closure", 0);
	}
	
	//设置Closure状态,以后不再使用
	private static void setClosure(Context context) {
		SharedPreferences preferences = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("Closure", 1);
		editor.commit();
	}
	
	//生成URL
	private static String getTrackingCodeUrl(Context context,String aid) {
		StringBuffer buf = new StringBuffer(1024);
		buf.append(SERVER_URL);
		format(buf, true, "uid", encode(getUUID(context)));
		format(buf, false, "aid", aid);
		format(buf, false, "wma", encode(getWMA(context)));
		format(buf, false, "mod", getDeviceModel());
		format(buf, false, "nt", ""+APN.getCurrentNetworkType(context));
		format(buf, false, "os", "0");// Android
		format(buf, false, "osv", getOSV());
		format(buf, false, "lng", getLanguage());
		format(buf, false, "apn", context.getPackageName());
		format(buf, false, "av", getApplicationVersion(context));
		format(buf, false, "pv", getSDKVersion());
		return buf.toString();
	}
	
	// 添加URL参数
	private static void format(StringBuffer sb, boolean isFirst, String key, String value) {
		if (value == null || value.length() == 0)
			return;
		String param = "";
		try {
			param = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
		} catch (Throwable e) {
			param = "";
		}
		if (param == null || param.length() == 0)
			return;

		sb.append(isFirst ? "?" : "&").append(param);
	}
	
	//获取IMEI(UUID)
	private static String getUUID(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		if (imei==null||imei.length()==0) {
			imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		}
		return imei;
	}
	
	//WIFI权限检查
	private static boolean isSupportedWifiAccess(Context context) {
		try {
			context.enforceCallingOrSelfPermission(
					android.Manifest.permission.ACCESS_WIFI_STATE,
					"Tracking Code may require permission to android.Manifest.permission.ACCESS_WIFI_STATE!");
			return true;
		} catch (Throwable s) {
			Log.w(TAG, s.getMessage());
			return false;
		}
	}
	
	//获取Wifi MAC地址
	private static String getWMA(Context context) {
		try {
			if (!isSupportedWifiAccess(context)) return null;
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager==null) return null;
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo==null) return null;
			return wifiInfo.getMacAddress().replaceAll(":", "");
		} catch (Throwable e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	//编码
	private static String encode(String param) {
		//System.out.println("param = "+param);
		if (param==null||param.length()==0) return null;
		String encodedParam = Base64.encodeToString(param.getBytes(), Base64.NO_WRAP);
		//System.out.println("encodedParam = "+encodedParam);
		char [] chsEncodedParam = encodedParam.toCharArray();
		for (int i=0;i<chsEncodedParam.length;i=i+2) {
			char ch = chsEncodedParam[i];
			chsEncodedParam[i] = chsEncodedParam[i+1];
			chsEncodedParam[i+1] = ch;
		}
		encodedParam = new String(chsEncodedParam); 
		//System.out.println("changed encodedParam = "+encodedParam);
		return encodedParam;
	}
	
	//获取手机型号
	private static String getDeviceModel() {
		return Build.BRAND+" "+Build.MODEL;
	}
	
	//获取系统版本
	private static String getOSV() {
		return Build.VERSION.RELEASE;
	}
	
	//获取语言信息
	private static String getLanguage() {
		Locale loc = Locale.getDefault();
		return loc.getLanguage()+"_"+loc.getCountry();
	}
	
	//获取应用程序版本
	private static String getApplicationVersion(Context context) {
		try {
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pkgInfo.versionName;
		} catch (Exception e) {
			return null;
		}
	}
	
	//获取Android SDK 版本
	private static String getSDKVersion() {
		return Build.VERSION.SDK;
	}
	
	//当前网络类型信息
	static class CurrentNetworkInfo {
		int apnType;
		String apnProxy;
		String apnProxyPort;
	}
	
	static class APN {
		//APN Item
		private static final String PROXY = "proxy";
		private static final String PORT = "port";
		private static final String MMSPROXY = "mmsproxy";
		private static final String MMSPORT = "mmsport";
		
		//中国电信APN列表
		private static final String APN_CTNET = "ctnet";
		
		//中国移动APN列表
		private static final String APN_CMNET = "cmnet";
		
		//中国联通APN列表
		private static final String APN_UNINET = "uninet";
		
		//中国联通WCDMA APN列表
		private static final String APN_UNI3GNET = "3gnet";
	    
		//常量定义
		static final int NETWORK_NONE = -1;
		static final int NETWORK_2G = 0;
		//static final int NETWORK_WWAN = 1;//IOS使用
		static final int NETWORK_3G = 2;
		static final int NETWORK_WIFI = 3;

		//构造析构函数
		private APN() {}
		
		//获得当前网络状态、代理服务器地址和端口号
		static CurrentNetworkInfo getCurrentNetwork(Context context){
			CurrentNetworkInfo cni = new CurrentNetworkInfo();
			int networkType = getCurrentNetworkType(context);
			cni.apnType = networkType;
			if(networkType == NETWORK_NONE || networkType == NETWORK_WIFI){
				return cni;
			}
			Cursor cur = context.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
			cur.moveToFirst();
			//如果APN是NET类型
			if((cur.getColumnIndex(APN_CMNET) != -1) || 
			   (cur.getColumnIndex(APN_CTNET) != -1) || 
			   (cur.getColumnIndex(APN_UNI3GNET) != -1) || 
			   (cur.getColumnIndex(APN_UNINET) != -1)){
			}
			else{
				//APN是WAP类型
				if(cur.getInt(cur.getColumnIndex(PROXY)) > 0){
					cni.apnProxy = cur.getString(cur.getColumnIndex(PROXY));
					cni.apnProxyPort = cur.getString(cur.getColumnIndex(PORT));
				}else if(cur.getInt(cur.getColumnIndex(MMSPROXY)) > 0){
					cni.apnProxy = cur.getString(cur.getColumnIndex(MMSPROXY));
					cni.apnProxyPort = cur.getString(cur.getColumnIndex(MMSPORT));
				}
			}
			if(cur != null){
				cur.close();
			}
			return cni;
		}
		
		static String[] NetworkTypes = { "UNKNOWN", "GPRS", "EDGE", "UMTS", "CDMA",
			"EVDO_0", "EVDO_A", "1xRTT", "HSDPA", "HSUPA", "HSPA", "IDEN", "EVDO_B", "LTE", "EHRPD", "HSPAP" };

		//当前网络类型
		static int getCurrentNetworkType(Context context){
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			
			if (ni==null||!ni.isConnected()) {
				return NETWORK_NONE;
			}
			else if (ni.getType()==ConnectivityManager.TYPE_WIFI){
				return NETWORK_WIFI;
			}
			else if (ni.getType()==ConnectivityManager.TYPE_MOBILE) {
				TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				int type = telephonyManager.getNetworkType();
				if (type<0||type>NetworkTypes.length-1) return NETWORK_WIFI;//未知类型
				String NetworkType = NetworkTypes[type];
				for (int i=1;i<3;i++) {
					if (NetworkTypes[i].equals(NetworkType)) {
						return NETWORK_2G;
					}
				}
				for (int i=3;i<NetworkTypes.length;i++) {
					if (NetworkTypes[i].equals(NetworkType)) {
						return NETWORK_3G;
					}
				}
			}
			
			return NETWORK_WIFI;
		}
	}
	
	static class HttpRequester {
		private static final int TIME_OUT = 10000; // 超时设置
		private static final String HTTP_HOST = "Host"; // HTTP HOST
		private static final String HTTP_DEFAULT_METHOD = "GET"; // HTTP GET方法
		private static final String CMWAP1 = "application/vnd.wap.wmlc"; // 收费提示1
		private static final String CMWAP2 = "text/vnd.wap.wml"; // 收费提示2

		// 发出HTTP请求
		static int request(Context context, String url) {
			// 检测接入点状况
			CurrentNetworkInfo info = APN.getCurrentNetwork(context);
			if (info.apnType != APN.NETWORK_NONE) {
				return request(url,info.apnProxy,info.apnProxyPort);
			}
			else {
				return SmartmadTrackingCodeListener.Failure;
			}
		}

		// 发出HTTP请求
		private static int request(final String url,final String proxy, final String proxyport) {
			InputStream read = null; // 输入流对象
			HttpURLConnection connection = null; // 连接对象
			try {
				URL requesturl = new URL(url); // 请求地址
				// 设置代理
				if ((proxy != null) && (proxy.length() >= 7) && (proxyport != null)) {
					// 连接系统属性
					Properties prop = System.getProperties();
					// 通知系统要通过代理连接
					System.getProperties().put("proxySet", "true");
					// 指定代理服务地址
					prop.setProperty("http.proxyHost", proxy);
					// 指定代理监听端口
					prop.setProperty("http.proxyPort", proxyport);
					// 打开连接
					connection = (HttpURLConnection) requesturl.openConnection();
				} else {
					// 打开连接
					connection = (HttpURLConnection) requesturl.openConnection();
					connection.setRequestProperty(HTTP_HOST, requesturl.getHost());
				}
				connection.setRequestMethod(HTTP_DEFAULT_METHOD);// 设置连接方法
				connection.setConnectTimeout(TIME_OUT); // 设置连接超时时间
				connection.setReadTimeout(TIME_OUT); // 设置读超时时间
				connection.setInstanceFollowRedirects(true); // 在这个实例中使用重定向
				connection.connect();// 启动连接
				int responseCode = connection.getResponseCode();
				String contentType = connection.getContentType();
				// 数据接收
				if (responseCode == 200) {
					if (contentType.startsWith(CMWAP1) || contentType.startsWith(CMWAP2)) {
						// 打开连接
						connection = (HttpURLConnection) requesturl.openConnection();
						connection.setRequestProperty(HTTP_HOST, requesturl.getHost());
						connection.setRequestMethod(HTTP_DEFAULT_METHOD);// 设置连接方法
						connection.setConnectTimeout(TIME_OUT); // 设置连接超时时间
						connection.setReadTimeout(TIME_OUT); // 设置读超时时间
						connection.setInstanceFollowRedirects(true); // 在这个实例中使用重定向
						connection.connect();// 启动连接
						responseCode = connection.getResponseCode();
						if (responseCode == 200) {
							return SmartmadTrackingCodeListener.Success;
						}
						else if (responseCode == 403) {
							return SmartmadTrackingCodeListener.Closure;
						}
						else {
							return SmartmadTrackingCodeListener.Failure;
						}
					} else {
						return SmartmadTrackingCodeListener.Success;
					}
				}
				else if (responseCode == 403) {
					return SmartmadTrackingCodeListener.Closure;
				}
				else {
					return SmartmadTrackingCodeListener.Failure;
				}
			} catch(Throwable t) {
				t.printStackTrace();
			} finally {
				if (read != null) {
					try {
						read.close();
					} catch (IOException e) {
					}
				}
			}

			return SmartmadTrackingCodeListener.Failure;
		}
	}


	static class Base64 {
	    /**
	     * Default values for encoder/decoder flags.
	     */
	    public static final int DEFAULT = 0;

	    /**
	     * Encoder flag bit to omit the padding '=' characters at the end
	     * of the output (if any).
	     */
	    public static final int NO_PADDING = 1;

	    /**
	     * Encoder flag bit to omit all line terminators (i.e., the output
	     * will be on one long line).
	     */
	    public static final int NO_WRAP = 2;

	    /**
	     * Encoder flag bit to indicate lines should be terminated with a
	     * CRLF pair instead of just an LF.  Has no effect if {@code
	     * NO_WRAP} is specified as well.
	     */
	    public static final int CRLF = 4;

	    /**
	     * Encoder/decoder flag bit to indicate using the "URL and
	     * filename safe" variant of Base64 (see RFC 3548 section 4) where
	     * {@code -} and {@code _} are used in place of {@code +} and
	     * {@code /}.
	     */
	    public static final int URL_SAFE = 8;

	    /**
	     * Flag to pass to {@link Base64OutputStream} to indicate that it
	     * should not close the output stream it is wrapping when it
	     * itself is closed.
	     */
	    public static final int NO_CLOSE = 16;

	    //  --------------------------------------------------------
	    //  shared code
	    //  --------------------------------------------------------

	    /* package */ static abstract class Coder {
	        public byte[] output;
	        public int op;

	        /**
	         * Encode/decode another block of input data.  this.output is
	         * provided by the caller, and must be big enough to hold all
	         * the coded data.  On exit, this.opwill be set to the length
	         * of the coded data.
	         *
	         * @param finish true if this is the final call to process for
	         *        this object.  Will finalize the coder state and
	         *        include any final bytes in the output.
	         *
	         * @return true if the input so far is good; false if some
	         *         error has been detected in the input stream..
	         */
	        public abstract boolean process(byte[] input, int offset, int len, boolean finish);

	        /**
	         * @return the maximum number of bytes a call to process()
	         * could produce for the given number of input bytes.  This may
	         * be an overestimate.
	         */
	        public abstract int maxOutputSize(int len);
	    }

	    //  --------------------------------------------------------
	    //  decoding
	    //  --------------------------------------------------------

	    /**
	     * Decode the Base64-encoded data in input and return the data in
	     * a new byte array.
	     *
	     * <p>The padding '=' characters at the end are considered optional, but
	     * if any are present, there must be the correct number of them.
	     *
	     * @param str    the input String to decode, which is converted to
	     *               bytes using the default charset
	     * @param flags  controls certain features of the decoded output.
	     *               Pass {@code DEFAULT} to decode standard Base64.
	     *
	     * @throws IllegalArgumentException if the input contains
	     * incorrect padding
	     */
	    public static byte[] decode(String str, int flags) {
	        return decode(str.getBytes(), flags);
	    }

	    /**
	     * Decode the Base64-encoded data in input and return the data in
	     * a new byte array.
	     *
	     * <p>The padding '=' characters at the end are considered optional, but
	     * if any are present, there must be the correct number of them.
	     *
	     * @param input the input array to decode
	     * @param flags  controls certain features of the decoded output.
	     *               Pass {@code DEFAULT} to decode standard Base64.
	     *
	     * @throws IllegalArgumentException if the input contains
	     * incorrect padding
	     */
	    public static byte[] decode(byte[] input, int flags) {
	        return decode(input, 0, input.length, flags);
	    }

	    /**
	     * Decode the Base64-encoded data in input and return the data in
	     * a new byte array.
	     *
	     * <p>The padding '=' characters at the end are considered optional, but
	     * if any are present, there must be the correct number of them.
	     *
	     * @param input  the data to decode
	     * @param offset the position within the input array at which to start
	     * @param len    the number of bytes of input to decode
	     * @param flags  controls certain features of the decoded output.
	     *               Pass {@code DEFAULT} to decode standard Base64.
	     *
	     * @throws IllegalArgumentException if the input contains
	     * incorrect padding
	     */
	    public static byte[] decode(byte[] input, int offset, int len, int flags) {
	        // Allocate space for the most data the input could represent.
	        // (It could contain less if it contains whitespace, etc.)
	        Decoder decoder = new Decoder(flags, new byte[len*3/4]);

	        if (!decoder.process(input, offset, len, true)) {
	            throw new IllegalArgumentException("bad base-64");
	        }

	        // Maybe we got lucky and allocated exactly enough output space.
	        if (decoder.op == decoder.output.length) {
	            return decoder.output;
	        }

	        // Need to shorten the array, so allocate a new one of the
	        // right size and copy.
	        byte[] temp = new byte[decoder.op];
	        System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
	        return temp;
	    }

	    /* package */ static class Decoder extends Coder {
	        /**
	         * Lookup table for turning bytes into their position in the
	         * Base64 alphabet.
	         */
	        private static final int DECODE[] = {
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
	            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
	            -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
	            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
	            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	        };

	        /**
	         * Decode lookup table for the "web safe" variant (RFC 3548
	         * sec. 4) where - and _ replace + and /.
	         */
	        private static final int DECODE_WEBSAFE[] = {
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
	            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1,
	            -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
	            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63,
	            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	        };

	        /** Non-data values in the DECODE arrays. */
	        private static final int SKIP = -1;
	        private static final int EQUALS = -2;

	        /**
	         * States 0-3 are reading through the next input tuple.
	         * State 4 is having read one '=' and expecting exactly
	         * one more.
	         * State 5 is expecting no more data or padding characters
	         * in the input.
	         * State 6 is the error state; an error has been detected
	         * in the input and no future input can "fix" it.
	         */
	        private int state;   // state number (0 to 6)
	        private int value;

	        final private int[] alphabet;

	        public Decoder(int flags, byte[] output) {
	            this.output = output;

	            alphabet = ((flags & URL_SAFE) == 0) ? DECODE : DECODE_WEBSAFE;
	            state = 0;
	            value = 0;
	        }

	        /**
	         * @return an overestimate for the number of bytes {@code
	         * len} bytes could decode to.
	         */
	        public int maxOutputSize(int len) {
	            return len * 3/4 + 10;
	        }

	        /**
	         * Decode another block of input data.
	         *
	         * @return true if the state machine is still healthy.  false if
	         *         bad base-64 data has been detected in the input stream.
	         */
	        public boolean process(byte[] input, int offset, int len, boolean finish) {
	            if (this.state == 6) return false;

	            int p = offset;
	            len += offset;

	            // Using local variables makes the decoder about 12%
	            // faster than if we manipulate the member variables in
	            // the loop.  (Even alphabet makes a measurable
	            // difference, which is somewhat surprising to me since
	            // the member variable is final.)
	            int state = this.state;
	            int value = this.value;
	            int op = 0;
	            final byte[] output = this.output;
	            final int[] alphabet = this.alphabet;

	            while (p < len) {
	                // Try the fast path:  we're starting a new tuple and the
	                // next four bytes of the input stream are all data
	                // bytes.  This corresponds to going through states
	                // 0-1-2-3-0.  We expect to use this method for most of
	                // the data.
	                //
	                // If any of the next four bytes of input are non-data
	                // (whitespace, etc.), value will end up negative.  (All
	                // the non-data values in decode are small negative
	                // numbers, so shifting any of them up and or'ing them
	                // together will result in a value with its top bit set.)
	                //
	                // You can remove this whole block and the output should
	                // be the same, just slower.
	                if (state == 0) {
	                    while (p+4 <= len &&
	                           (value = ((alphabet[input[p] & 0xff] << 18) |
	                                     (alphabet[input[p+1] & 0xff] << 12) |
	                                     (alphabet[input[p+2] & 0xff] << 6) |
	                                     (alphabet[input[p+3] & 0xff]))) >= 0) {
	                        output[op+2] = (byte) value;
	                        output[op+1] = (byte) (value >> 8);
	                        output[op] = (byte) (value >> 16);
	                        op += 3;
	                        p += 4;
	                    }
	                    if (p >= len) break;
	                }

	                // The fast path isn't available -- either we've read a
	                // partial tuple, or the next four input bytes aren't all
	                // data, or whatever.  Fall back to the slower state
	                // machine implementation.

	                int d = alphabet[input[p++] & 0xff];

	                switch (state) {
	                case 0:
	                    if (d >= 0) {
	                        value = d;
	                        ++state;
	                    } else if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;

	                case 1:
	                    if (d >= 0) {
	                        value = (value << 6) | d;
	                        ++state;
	                    } else if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;

	                case 2:
	                    if (d >= 0) {
	                        value = (value << 6) | d;
	                        ++state;
	                    } else if (d == EQUALS) {
	                        // Emit the last (partial) output tuple;
	                        // expect exactly one more padding character.
	                        output[op++] = (byte) (value >> 4);
	                        state = 4;
	                    } else if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;

	                case 3:
	                    if (d >= 0) {
	                        // Emit the output triple and return to state 0.
	                        value = (value << 6) | d;
	                        output[op+2] = (byte) value;
	                        output[op+1] = (byte) (value >> 8);
	                        output[op] = (byte) (value >> 16);
	                        op += 3;
	                        state = 0;
	                    } else if (d == EQUALS) {
	                        // Emit the last (partial) output tuple;
	                        // expect no further data or padding characters.
	                        output[op+1] = (byte) (value >> 2);
	                        output[op] = (byte) (value >> 10);
	                        op += 2;
	                        state = 5;
	                    } else if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;

	                case 4:
	                    if (d == EQUALS) {
	                        ++state;
	                    } else if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;

	                case 5:
	                    if (d != SKIP) {
	                        this.state = 6;
	                        return false;
	                    }
	                    break;
	                }
	            }

	            if (!finish) {
	                // We're out of input, but a future call could provide
	                // more.
	                this.state = state;
	                this.value = value;
	                this.op = op;
	                return true;
	            }

	            // Done reading input.  Now figure out where we are left in
	            // the state machine and finish up.

	            switch (state) {
	            case 0:
	                // Output length is a multiple of three.  Fine.
	                break;
	            case 1:
	                // Read one extra input byte, which isn't enough to
	                // make another output byte.  Illegal.
	                this.state = 6;
	                return false;
	            case 2:
	                // Read two extra input bytes, enough to emit 1 more
	                // output byte.  Fine.
	                output[op++] = (byte) (value >> 4);
	                break;
	            case 3:
	                // Read three extra input bytes, enough to emit 2 more
	                // output bytes.  Fine.
	                output[op++] = (byte) (value >> 10);
	                output[op++] = (byte) (value >> 2);
	                break;
	            case 4:
	                // Read one padding '=' when we expected 2.  Illegal.
	                this.state = 6;
	                return false;
	            case 5:
	                // Read all the padding '='s we expected and no more.
	                // Fine.
	                break;
	            }

	            this.state = state;
	            this.op = op;
	            return true;
	        }
	    }

	    //  --------------------------------------------------------
	    //  encoding
	    //  --------------------------------------------------------

	    /**
	     * Base64-encode the given data and return a newly allocated
	     * String with the result.
	     *
	     * @param input  the data to encode
	     * @param flags  controls certain features of the encoded output.
	     *               Passing {@code DEFAULT} results in output that
	     *               adheres to RFC 2045.
	     */
	    public static String encodeToString(byte[] input, int flags) {
	        try {
	            return new String(encode(input, flags), "US-ASCII");
	        } catch (UnsupportedEncodingException e) {
	            // US-ASCII is guaranteed to be available.
	            throw new AssertionError(e);
	        }
	    }

	    /**
	     * Base64-encode the given data and return a newly allocated
	     * String with the result.
	     *
	     * @param input  the data to encode
	     * @param offset the position within the input array at which to
	     *               start
	     * @param len    the number of bytes of input to encode
	     * @param flags  controls certain features of the encoded output.
	     *               Passing {@code DEFAULT} results in output that
	     *               adheres to RFC 2045.
	     */
	    public static String encodeToString(byte[] input, int offset, int len, int flags) {
	        try {
	            return new String(encode(input, offset, len, flags), "US-ASCII");
	        } catch (UnsupportedEncodingException e) {
	            // US-ASCII is guaranteed to be available.
	            throw new AssertionError(e);
	        }
	    }

	    /**
	     * Base64-encode the given data and return a newly allocated
	     * byte[] with the result.
	     *
	     * @param input  the data to encode
	     * @param flags  controls certain features of the encoded output.
	     *               Passing {@code DEFAULT} results in output that
	     *               adheres to RFC 2045.
	     */
	    public static byte[] encode(byte[] input, int flags) {
	        return encode(input, 0, input.length, flags);
	    }

	    /**
	     * Base64-encode the given data and return a newly allocated
	     * byte[] with the result.
	     *
	     * @param input  the data to encode
	     * @param offset the position within the input array at which to
	     *               start
	     * @param len    the number of bytes of input to encode
	     * @param flags  controls certain features of the encoded output.
	     *               Passing {@code DEFAULT} results in output that
	     *               adheres to RFC 2045.
	     */
	    public static byte[] encode(byte[] input, int offset, int len, int flags) {
	        Encoder encoder = new Encoder(flags, null);

	        // Compute the exact length of the array we will produce.
	        int output_len = len / 3 * 4;

	        // Account for the tail of the data and the padding bytes, if any.
	        if (encoder.do_padding) {
	            if (len % 3 > 0) {
	                output_len += 4;
	            }
	        } else {
	            switch (len % 3) {
	                case 0: break;
	                case 1: output_len += 2; break;
	                case 2: output_len += 3; break;
	            }
	        }

	        // Account for the newlines, if any.
	        if (encoder.do_newline && len > 0) {
	            output_len += (((len-1) / (3 * Encoder.LINE_GROUPS)) + 1) *
	                (encoder.do_cr ? 2 : 1);
	        }

	        encoder.output = new byte[output_len];
	        encoder.process(input, offset, len, true);

	        assert encoder.op == output_len;

	        return encoder.output;
	    }

	    /* package */ static class Encoder extends Coder {
	        /**
	         * Emit a new line every this many output tuples.  Corresponds to
	         * a 76-character line length (the maximum allowable according to
	         * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>).
	         */
	        public static final int LINE_GROUPS = 19;

	        /**
	         * Lookup table for turning Base64 alphabet positions (6 bits)
	         * into output bytes.
	         */
	        private static final byte ENCODE[] = {
	            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
	            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
	            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
	            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
	        };

	        /**
	         * Lookup table for turning Base64 alphabet positions (6 bits)
	         * into output bytes.
	         */
	        private static final byte ENCODE_WEBSAFE[] = {
	            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
	            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
	            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
	            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
	        };

	        final private byte[] tail;
	        /* package */ int tailLen;
	        private int count;

	        final public boolean do_padding;
	        final public boolean do_newline;
	        final public boolean do_cr;
	        final private byte[] alphabet;

	        public Encoder(int flags, byte[] output) {
	            this.output = output;

	            do_padding = (flags & NO_PADDING) == 0;
	            do_newline = (flags & NO_WRAP) == 0;
	            do_cr = (flags & CRLF) != 0;
	            alphabet = ((flags & URL_SAFE) == 0) ? ENCODE : ENCODE_WEBSAFE;

	            tail = new byte[2];
	            tailLen = 0;

	            count = do_newline ? LINE_GROUPS : -1;
	        }

	        /**
	         * @return an overestimate for the number of bytes {@code
	         * len} bytes could encode to.
	         */
	        public int maxOutputSize(int len) {
	            return len * 8/5 + 10;
	        }

	        public boolean process(byte[] input, int offset, int len, boolean finish) {
	            // Using local variables makes the encoder about 9% faster.
	            final byte[] alphabet = this.alphabet;
	            final byte[] output = this.output;
	            int op = 0;
	            int count = this.count;

	            int p = offset;
	            len += offset;
	            int v = -1;

	            // First we need to concatenate the tail of the previous call
	            // with any input bytes available now and see if we can empty
	            // the tail.

	            switch (tailLen) {
	                case 0:
	                    // There was no tail.
	                    break;

	                case 1:
	                    if (p+2 <= len) {
	                        // A 1-byte tail with at least 2 bytes of
	                        // input available now.
	                        v = ((tail[0] & 0xff) << 16) |
	                            ((input[p++] & 0xff) << 8) |
	                            (input[p++] & 0xff);
	                        tailLen = 0;
	                    };
	                    break;

	                case 2:
	                    if (p+1 <= len) {
	                        // A 2-byte tail with at least 1 byte of input.
	                        v = ((tail[0] & 0xff) << 16) |
	                            ((tail[1] & 0xff) << 8) |
	                            (input[p++] & 0xff);
	                        tailLen = 0;
	                    }
	                    break;
	            }

	            if (v != -1) {
	                output[op++] = alphabet[(v >> 18) & 0x3f];
	                output[op++] = alphabet[(v >> 12) & 0x3f];
	                output[op++] = alphabet[(v >> 6) & 0x3f];
	                output[op++] = alphabet[v & 0x3f];
	                if (--count == 0) {
	                    if (do_cr) output[op++] = '\r';
	                    output[op++] = '\n';
	                    count = LINE_GROUPS;
	                }
	            }

	            // At this point either there is no tail, or there are fewer
	            // than 3 bytes of input available.

	            // The main loop, turning 3 input bytes into 4 output bytes on
	            // each iteration.
	            while (p+3 <= len) {
	                v = ((input[p] & 0xff) << 16) |
	                    ((input[p+1] & 0xff) << 8) |
	                    (input[p+2] & 0xff);
	                output[op] = alphabet[(v >> 18) & 0x3f];
	                output[op+1] = alphabet[(v >> 12) & 0x3f];
	                output[op+2] = alphabet[(v >> 6) & 0x3f];
	                output[op+3] = alphabet[v & 0x3f];
	                p += 3;
	                op += 4;
	                if (--count == 0) {
	                    if (do_cr) output[op++] = '\r';
	                    output[op++] = '\n';
	                    count = LINE_GROUPS;
	                }
	            }

	            if (finish) {
	                // Finish up the tail of the input.  Note that we need to
	                // consume any bytes in tail before any bytes
	                // remaining in input; there should be at most two bytes
	                // total.

	                if (p-tailLen == len-1) {
	                    int t = 0;
	                    v = ((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 4;
	                    tailLen -= t;
	                    output[op++] = alphabet[(v >> 6) & 0x3f];
	                    output[op++] = alphabet[v & 0x3f];
	                    if (do_padding) {
	                        output[op++] = '=';
	                        output[op++] = '=';
	                    }
	                    if (do_newline) {
	                        if (do_cr) output[op++] = '\r';
	                        output[op++] = '\n';
	                    }
	                } else if (p-tailLen == len-2) {
	                    int t = 0;
	                    v = (((tailLen > 1 ? tail[t++] : input[p++]) & 0xff) << 10) |
	                        (((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 2);
	                    tailLen -= t;
	                    output[op++] = alphabet[(v >> 12) & 0x3f];
	                    output[op++] = alphabet[(v >> 6) & 0x3f];
	                    output[op++] = alphabet[v & 0x3f];
	                    if (do_padding) {
	                        output[op++] = '=';
	                    }
	                    if (do_newline) {
	                        if (do_cr) output[op++] = '\r';
	                        output[op++] = '\n';
	                    }
	                } else if (do_newline && op > 0 && count != LINE_GROUPS) {
	                    if (do_cr) output[op++] = '\r';
	                    output[op++] = '\n';
	                }

	                assert tailLen == 0;
	                assert p == len;
	            } else {
	                // Save the leftovers in tail to be consumed on the next
	                // call to encodeInternal.

	                if (p == len-1) {
	                    tail[tailLen++] = input[p];
	                } else if (p == len-2) {
	                    tail[tailLen++] = input[p];
	                    tail[tailLen++] = input[p+1];
	                }
	            }

	            this.op = op;
	            this.count = count;

	            return true;
	        }
	    }

	    private Base64() { }   // don't instantiate
	}
}
