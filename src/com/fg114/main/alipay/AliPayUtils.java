package com.fg114.main.alipay;

import static com.fg114.main.alipay.AliPayUtils.showProgress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.android.app.IAlixPay;
import com.alipay.android.app.IRemoteServiceCallback;
import com.fg114.main.alipay.AliPayUtils.AliPayListener;
import com.fg114.main.util.CheckUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 从支付宝Demo修改整合而来
 * 
 * @author xujianjun, 2013-06-19 
 * 
 */
public class AliPayUtils {

	public interface AliPayListener {
		void onPayFinish(boolean isSuccessful, String message);
	}

	public static void doPay(Activity ativity, String orderInfo, AliPayListener payListener) {

		//
		// check to see if the MobileSecurePay is already installed.
		// 检测安全支付服务是否安装
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(ativity);
		boolean isMobile_spExist = mspHelper.detectMobile_sp();
		if (!isMobile_spExist) {
			return; // 失败，去下载流程
		}

		// 根据订单信息开始进行支付
		try {
			// 调用pay方法进行支付
			MobileSecurePayer msp = new MobileSecurePayer();
			boolean bRet = msp.pay(orderInfo, payListener, ativity);

			if (bRet) {
				// 显示“正在支付”进度条
				hideProgress();
				//showProgress(ativity, "提示", "正在支付");
			}

		} catch (Exception ex) {
			Log.e("支付异常", ex.getMessage(), ex);
			ex.printStackTrace();
			showMessage(ativity, "支付失败!" + ex.getMessage());
		}
	}

	static void showMessage(Activity activity, String msg) {
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 流转字符串方法
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
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

	/**
	 * 显示dialog
	 * 
	 * @param context
	 *            环境
	 * @param strTitle
	 *            标题
	 * @param strText
	 *            内容
	 * @param icon
	 *            图标
	 */
	public static void showDialog(Activity context, String strTitle, String strText) {
		AlertDialog.Builder tDialog = new AlertDialog.Builder(context);
		// tDialog.setIcon(null);
		tDialog.setTitle(strTitle);
		tDialog.setMessage(strText);
		tDialog.setPositiveButton("确定", null);
		tDialog.show();
	}

	/**
	 * 显示进度条
	 * 
	 * @param context
	 *            环境
	 * @param title
	 *            标题
	 * @param message
	 *            信息
	 * @return
	 */
	private static ProgressDialog dialog = null;

	public static void showProgress(Context context, CharSequence title, CharSequence message) {
		if (dialog != null) {
			dialog.dismiss();
		}
		dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();
	}

	public static void hideProgress() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	/**
	 * 字符串转json对象
	 * 
	 * @param str
	 * @param split
	 * @return
	 */
	public static JSONObject string2JSON(String str, String split) {
		JSONObject json = new JSONObject();
		try {
			String[] arrStr = str.split(split);
			for (int i = 0; i < arrStr.length; i++) {
				String[] arrKeyValue = arrStr[i].split("=");
				json.put(arrKeyValue[0], arrStr[i].substring(arrKeyValue[0].length() + 1));
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

}

/**
 * 网络连接工具类
 * 
 */
class NetworkManager {
	static final String TAG = "NetworkManager";

	private int connectTimeout = 30 * 1000;
	private int readTimeout = 30 * 1000;
	Proxy mProxy = null;
	Context mContext;

	public NetworkManager(Context context) {
		this.mContext = context;
		setDefaultHostnameVerifier();
	}

	/**
	 * 检查代理，是否cnwap接入
	 */
	private void detectProxy() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isAvailable() && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
			String proxyHost = android.net.Proxy.getDefaultHost();
			int port = android.net.Proxy.getDefaultPort();
			if (proxyHost != null) {
				final InetSocketAddress sa = new InetSocketAddress(proxyHost, port);
				mProxy = new Proxy(Proxy.Type.HTTP, sa);
			}
		}
	}

	private void setDefaultHostnameVerifier() {
		//
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	/**
	 * 发送和接收数据
	 * 
	 * @param strReqData
	 *            请求数据
	 * @param strUrl
	 *            请求地址
	 * @return
	 */
	public String SendAndWaitResponse(String strReqData, String strUrl) {
		//
		detectProxy();

		String strResponse = null;
		ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("requestData", strReqData));

		HttpURLConnection httpConnect = null;
		UrlEncodedFormEntity p_entity;
		try {
			p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
			URL url = new URL(strUrl);

			if (mProxy != null) {
				httpConnect = (HttpURLConnection) url.openConnection(mProxy);
			} else {
				httpConnect = (HttpURLConnection) url.openConnection();
			}
			httpConnect.setConnectTimeout(connectTimeout);
			httpConnect.setReadTimeout(readTimeout);
			httpConnect.setDoOutput(true);
			httpConnect.addRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
			httpConnect.connect();

			OutputStream os = httpConnect.getOutputStream();
			p_entity.writeTo(os);
			os.flush();

			InputStream content = httpConnect.getInputStream();
			strResponse = AliPayUtils.convertStreamToString(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpConnect.disconnect();
		}

		return strResponse;
	}

	/**
	 * 下载文件
	 * 
	 * @param context
	 *            上下文环境
	 * @param strurl
	 *            下载地址
	 * @param path
	 *            下载路径
	 * @return
	 */
	public boolean urlDownloadToFile(Context context, String strurl, String path) {
		boolean bRet = false;
		InputStream is = null;
		FileOutputStream fos = null;
		detectProxy();
		try {
			URL url = new URL(strurl);
			HttpURLConnection conn = null;
			if (mProxy != null) {
				conn = (HttpURLConnection) url.openConnection(mProxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setDoInput(true);

			conn.connect();
			is = conn.getInputStream();

			File file = new File(path);
			file.createNewFile();
			fos = new FileOutputStream(file);

			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			bRet = true;

		} catch (IOException e) {
			e.printStackTrace();

		} finally {

			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ---------------------
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bRet;
	}

}

/**
 * 和安全支付服务通信，发送订单信息进行支付，接收支付宝返回信息
 * 
 */
class MobileSecurePayer {
	static String TAG = "MobileSecurePayer";

	Integer lock = 0;
	IAlixPay mAlixPay = null;
	boolean mbPaying = false;

	Activity mActivity = null;

	// 和安全支付服务建立连接
	private ServiceConnection mAlixPayConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			//
			// wake up the binder to continue.
			// 获得通信通道
			synchronized (lock) {
				mAlixPay = IAlixPay.Stub.asInterface(service);
				lock.notify();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mAlixPay = null;
		}
	};

	/**
	 * 向支付宝发送支付请求
	 * 
	 * @param strOrderInfo
	 *            订单信息
	 * @param callback
	 *            回调handler
	 * @param myWhat
	 *            回调信息
	 * @param activity
	 *            目标activity
	 * @return
	 */
	public boolean pay(final String strOrderInfo, final AliPayListener listener, final Activity activity) {
		if (mbPaying)
			return false;
		mbPaying = true;

		//
		mActivity = activity;

		// bind the service.
		// 绑定服务
		if (mAlixPay == null) {
			// 绑定安全支付服务需要获取上下文环境，
			// 如果绑定不成功使用mActivity.getApplicationContext().bindService
			// 解绑时同理
			mActivity.getApplicationContext().bindService(new Intent(IAlixPay.class.getName()), mAlixPayConnection, Context.BIND_AUTO_CREATE);
		}
		// else ok.

		// 实例一个线程来进行支付
		new Thread(new Runnable() {
			public void run() {
				try {
					// wait for the service bind operation to completely
					// finished.
					// Note: this is important,otherwise the next mAlixPay.Pay()
					// will fail.
					// 等待安全支付服务绑定操作结束
					// 注意：这里很重要，否则mAlixPay.Pay()方法会失败
					synchronized (lock) {
						if (mAlixPay == null)
							lock.wait();
					}
					// register a Callback for the service.
					// 为安全支付服务注册一个回调
					mAlixPay.registerCallback(mCallback);

					// call the MobileSecurePay service.
					// 调用安全支付服务的pay方法
					String strRet = mAlixPay.Pay(strOrderInfo);
					Log.d(TAG, "After Pay: " + strRet);

					// set the flag to indicate that we have finished.
					// unregister the Callback, and unbind the service.
					// 将mbPaying置为false，表示支付结束
					// 移除回调的注册，解绑安全支付服务
					mbPaying = false;
					mAlixPay.unregisterCallback(mCallback);
					mActivity.getApplicationContext().unbindService(mAlixPayConnection);
					// send the result back to caller.
					// 处理交易结果
					handlePayResult(strRet, listener);
				} catch (Exception e) {
					e.printStackTrace();

					// send the result back to caller.
					// 发送交易结果
					handlePayResult(e.toString(), listener);
				}
			}
		},"AlipayThread").start();

		return true;
	}

	// -------------------------------------------------------------
	void handlePayResult(String strRet, AliPayListener listener) {
		Log.e("支付结果串", strRet);
		// 处理交易结果
		try {
			AliPayUtils.hideProgress();
			// 获取交易状态码，具体状态代码请参看文档
			//格式：memo={操作已经取消。};resultStatus={6001};result={}
			HashMap<String,String> map=parseAlipayPayResultString(strRet);
			
			//返回码
			ResultCode resultCode=ResultCode.fromCode(map.get("resultStatus"));
			if (resultCode==ResultCode.C9000) {
				// 判断交易状态码，只有9000表示交易成功
				listener.onPayFinish(true, CheckUtil.isEmpty(map.get("memo"))?"支付成功!":map.get("memo"));
			} else {
				listener.onPayFinish(false, CheckUtil.isEmpty(map.get("memo"))?resultCode.getName():map.get("memo"));
			}

			// // //先验签通知，验证由后台负责，我们后面有个拦截页可以确保支付正确
			// ResultChecker resultChecker = new ResultChecker(strRet);
			// int retVal = resultChecker.checkSign();
			// // 验签失败
			// if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) {
			// BaseHelper.showDialog(
			// AlixDemo.this,
			// "提示",
			// getResources().getString(
			// R.string.check_sign_failed),
			// android.R.drawable.ic_dialog_alert);
			// } else {// 验签成功。验签成功后再判断交易状态码
			// if(tradeStatus.equals("9000"))//判断交易状态码，只有9000表示交易成功
			// BaseHelper.showDialog(AlixDemo.this,
			// "提示","支付成功。交易状态码："+tradeStatus, R.drawable.infoicon);
			// else
			// BaseHelper.showDialog(AlixDemo.this, "提示", "支付失败。交易状态码:"
			// + tradeStatus, R.drawable.infoicon);
			// }

		} catch (Exception e) {
			e.printStackTrace();
			listener.onPayFinish(false, strRet + " (" + e.getMessage() + ")");
		}
	}

	private HashMap<String, String> parseAlipayPayResultString(String strRet) {
		HashMap<String, String> map=new HashMap<String, String>();
		//resultStatus
		//memo
		//result
		if(strRet==null){
			map.put("resultStatus", "9999");
			map.put("memo", "支付失败[没有返回数据]");
			return map;
		}
		
		//---
		String nameValues[]=strRet.split(";");
		for(String nv : nameValues){
			if(nv==null){
				continue;
			}
			String[] nameAndValue=nv.split("=");
			if(nameAndValue.length!=2){
				continue;
			}
			map.put(nameAndValue[0].trim(), nameAndValue[1].trim().replaceAll("[\\{\\}]", "").trim());
			//System.out.println("#"+nameAndValue[0].trim()+"=>"+map.get(nameAndValue[0].trim())+"#");
		}
		
		return map;
	}

	/**
	 * This implementation is used to receive callbacks from the remote service.
	 * 实现安全支付的回调
	 */
	private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
		/**
		 * This is called by the remote service regularly to tell us about new
		 * values. Note that IPC calls are dispatched through a thread pool
		 * running in each process, so the code executing here will NOT be
		 * running in our main thread like most other things -- so, to update
		 * the UI, we need to use a Handler to hop over there. 通过IPC机制启动安全支付服务
		 */
		public void startActivity(String packageName, String className, int iCallingPid, Bundle bundle) throws RemoteException {
			Intent intent = new Intent(Intent.ACTION_MAIN, null);

			if (bundle == null)
				bundle = new Bundle();
			// else ok.

			try {
				bundle.putInt("CallingPid", iCallingPid);
				intent.putExtras(bundle);
			} catch (Exception e) {
				e.printStackTrace();
			}

			intent.setClassName(packageName, className);
			mActivity.startActivity(intent);
		}

		/**
		 * when the msp loading dialog gone, call back this method.
		 */
		@Override
		public boolean isHideLoadingScreen() throws RemoteException {
			return false;
		}

		/**
		 * when the current trade is finished or cancelled, call back this
		 * method.
		 */
		@Override
		public void payEnd(boolean arg0, String arg1) throws RemoteException {

		}

	};
}

/**
 * 
 * 检测安全支付服务是否正确安装，如果没有安装进行进行下载安装
 * 
 */
class MobileSecurePayHelper {
	static final String TAG = "MobileSecurePayHelper";

	private ProgressDialog mProgress = null;
	Context mContext = null;

	public MobileSecurePayHelper(Context context) {
		this.mContext = context;
	}

	/**
	 * 检测安全支付服务是否安装
	 * 
	 * @return
	 */
	public boolean detectMobile_sp() {
		boolean isMobile_spExist = isMobile_spExist();
		if (!isMobile_spExist) {
			showProgress(mContext, null, "正在检测安全支付服务版本");

			// 获取将要下载的文件存放的绝对路径 获取/data/data//cache目录
			File cacheDir = mContext.getCacheDir();
			final String cachePath = cacheDir.getAbsolutePath() + "/temp.apk";

			// 实例新线程检测是否有新版本进行下载
			new Thread(new Runnable() {
				public void run() {
					Message msg = new Message();
					msg.what = AlixId.RQF_INSTALL_FAIL;
					try {
						// 检测是否有新的版本。
						// PackageInfo apkInfo = getApkInfo(mContext,
						// cachePath);
						// String newApkdlUrl = checkNewUpdate(apkInfo);
						String newApkdlUrl = checkNewUpdate(null);

						// 动态下载
						if (newApkdlUrl != null && retrieveApkFromNet(mContext, newApkdlUrl, cachePath)) {
							msg.what = AlixId.RQF_INSTALL_CHECK;
						}

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						AliPayUtils.hideProgress();
					}
					// 发送结果
					msg.obj = cachePath;
					mHandler.sendMessage(msg);
				}
			}).start();
		}

		return isMobile_spExist;
	}

	/**
	 * 显示确认安装的提示
	 * 
	 * @param context
	 *            上下文环境
	 * @param cachePath
	 *            安装文件路径
	 */
	public void showInstallConfirmDialog(final Context context, final String cachePath) {
		AlertDialog.Builder tDialog = new AlertDialog.Builder(context);
		// tDialog.setIcon(null);
		tDialog.setTitle("安装提示");
		tDialog.setMessage("为保证您的交易安全，需要您安装支付宝安全支付服务，才能进行付款。\n\n点击确定，立即安装。");

		tDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 修改apk权限
				chmod("777", cachePath);
				// 安装安全支付服务APK
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.parse("file://" + cachePath), "application/vnd.android.package-archive");
				context.startActivity(intent);
			}
		});

		tDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		tDialog.show();
	}

	/**
	 * 获取权限
	 * 
	 * @param permission
	 *            权限
	 * @param path
	 *            路径
	 */
	public static void chmod(String permission, String path) {
		try {
			String command = "chmod " + permission + " " + path;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 遍历程序列表，判断是否安装安全支付服务
	 * 
	 * @return
	 */
	public boolean isMobile_spExist() {
		PackageManager manager = mContext.getPackageManager();
		List<PackageInfo> pkgList = manager.getInstalledPackages(0);
		for (int i = 0; i < pkgList.size(); i++) {
			PackageInfo pI = pkgList.get(i);
			if (pI.packageName.equalsIgnoreCase("com.alipay.android.app"))
				return true;
		}

		return false;
	}

	/**
	 * 安装安全支付服务，安装assets文件夹下的apk
	 * 
	 * @param context
	 *            上下文环境
	 * @param fileName
	 *            apk名称
	 * @param path
	 *            安装路径
	 * @return
	 */
	public boolean retrieveApkFromAssets(Context context, String fileName, String path) {
		boolean bRet = false;

		try {
			InputStream is = context.getAssets().open(fileName);

			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);

			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}

			fos.close();
			is.close();

			bRet = true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return bRet;
	}

	/**
	 * 获取未安装的APK信息
	 * 
	 * @param context
	 * @param archiveFilePath
	 *            APK文件的路径。如：/sdcard/download/XX.apk
	 */
	public static PackageInfo getApkInfo(Context context, String archiveFilePath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo apkInfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_META_DATA);
		return apkInfo;
	}

	/**
	 * 检查是否有新版本，如果有，返回apk下载地址
	 * 
	 * @param packageInfo
	 *            {@link PackageInfo}
	 * @return
	 */
	public String checkNewUpdate(PackageInfo packageInfo) {
		String url = null;

		try {
			// JSONObject resp = sendCheckNewUpdate(packageInfo.versionName);
			// Log.e("packageInfo.versionName=" + packageInfo.versionName, "" +
			// resp.toString());
			JSONObject resp = sendCheckNewUpdate("1.0.0");
			if (resp.getString("needUpdate").equalsIgnoreCase("true")) {
				url = resp.getString("updateUrl");
			}
			// else ok.
		} catch (Exception e) {
			e.printStackTrace();
		}

		return url;
	}

	/**
	 * 发送当前版本信息，返回是否需要升级 如果需要升级返回更新apk地址
	 * 
	 * @param versionName
	 *            当前版本号
	 * @return
	 */
	public JSONObject sendCheckNewUpdate(String versionName) {
		JSONObject objResp = null;
		try {
			JSONObject req = new JSONObject();
			req.put(AlixDefine.action, AlixDefine.actionUpdate);

			JSONObject data = new JSONObject();
			data.put(AlixDefine.platform, "android");
			data.put(AlixDefine.VERSION, versionName);
			data.put(AlixDefine.partner, "");
			req.put(AlixDefine.data, data);

			objResp = sendRequest(req.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return objResp;
	}

	/**
	 * 发送json数据
	 * 
	 * @param content
	 * @return
	 */
	public JSONObject sendRequest(final String content) {
		NetworkManager nM = new NetworkManager(this.mContext);

		//
		JSONObject jsonResponse = null;
		try {
			String response = null;

			synchronized (nM) {
				response = nM.SendAndWaitResponse(content, Constant.server_url);
			}
			jsonResponse = new JSONObject(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	/**
	 * 动态下载apk
	 * 
	 * @param context
	 *            上下文环境
	 * @param strurl
	 *            下载地址
	 * @param filename
	 *            文件名称
	 * @return
	 */
	public boolean retrieveApkFromNet(Context context, String strurl, String filename) {
		boolean bRet = false;

		try {
			NetworkManager nM = new NetworkManager(this.mContext);
			bRet = nM.urlDownloadToFile(context, strurl, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bRet;
	}

	void closeProgress() {
		try {
			if (mProgress != null) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 此处接收安装检测结果
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
					case AlixId.RQF_INSTALL_CHECK: {
						//
						closeProgress();
						String cachePath = (String) msg.obj;

						showInstallConfirmDialog(mContext, cachePath);
					}
						break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
enum ResultCode {
	C9000(9000, "操作成功"),
	C4000(4000, "系统异常"),
	C4001(4001, "数据格式不正确"),
	C4003(4003, "该用户绑定的支付宝账户被冻结或不允许支付"),
	C4004(4004, "该用户已解除绑定"),
	C4005(4005, "绑定失败或没有绑定"),
	C4006(4006, "订单支付失败"),
	C4010(4010, "重新绑定账户"),
	C6000(6000, "支付服务正在进行升级操作"),
	C6001(6001, "用户中途取消支付操作"),
	C6002(6002, "网络连接异常"),
	C9999(9999, "支付失败");
	// -------
	private int code;
	private String name;
	
	private ResultCode(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ResultCode fromCode(String code) {
		try {
			return ResultCode.valueOf("C" + code);
		} catch (Throwable e) {
			return C9999;
		}
	}
}
class Constant {
	public final static String server_url = "https://msp.alipay.com/x.htm";
}

final class AlixId {
	public static final int BASE_ID = 0;
	public static final int RQF_PAY = BASE_ID + 1;
	public static final int RQF_INSTALL_CHECK = RQF_PAY + 1;
	public static final int RQF_INSTALL_FAIL = RQF_PAY + 2;
}

final class AlixDefine {
	public static final String IMEI = "imei";
	public static final String IMSI = "imsi";
	public static final String KEY = "key";
	public static final String USER_AGENT = "user_agent";
	public static final String VERSION = "version";
	public static final String DEVICE = "device";
	public static final String SID = "sid";
	public static final String partner = "partner";
	public static final String charset = "charset";
	public static final String sign_type = "sign_type";
	public static final String sign = "sign";

	public static final String URL = "URL";
	public static final String split = "&";

	public static final String AlixPay = "AlixPay";

	public static final String action = "action";
	public static final String actionUpdate = "update";
	public static final String data = "data";
	public static final String platform = "platform";
}