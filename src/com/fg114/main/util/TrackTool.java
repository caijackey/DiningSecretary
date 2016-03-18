package com.fg114.main.util;

import java.util.*;


import com.fg114.main.app.Settings;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * 跟踪方法
 * @author wufucheng
 *
 */
public class TrackTool {
	
	private static final String TAG = TrackTool.class.getName();
	
	private static final int appId = 1027;
	
//	private static final int appId = 10010;
	
	private static HashMap<String, String> mChannelUnicom = new HashMap<String, String>();
	private static List<String> mChannel91 = new ArrayList<String>();
	private static HashMap<String, String> mChannelDomob = new HashMap<String, String>();
	
	/*
		1
		 1a2651c27c9c8241
		 Unicom（Tel） 电话营销渠道： http://95171.cn/down?f=600041
		 
		2
		 fd403ce93bc0a3a8
		 Unicom（MadHouse1） Madhouse渠道1 http://95171.cn/down?f=600044
		 
		3
		 f5faf35315e64aa4
		 Unicom（MadHouse1） Madhouse渠道2 http://95171.cn/down?f=600047
		 
		4
		 c5d3d93c37581cff
		 Unicom（Wooboo） 哇棒渠道 http://95171.cn/down?f=600045
		 
		5
		 618ced0c11127937
		 Unicom（AXON） 安迅渠道 http://95171.cn/down?f=600046
		 
		6
		 24ea15f47947df47
		 Unicom 联通自由渠道 http://95171.cn/down?f=600048
	 */
	
//	static {
//		mChannelMap.put("600041", "1a2651c27c9c8241");
//		mChannelMap.put("600044", "fd403ce93bc0a3a8");
//		mChannelMap.put("600047", "f5faf35315e64aa4");
//		mChannelMap.put("600045", "c5d3d93c37581cff");
//		mChannelMap.put("600046", "618ced0c11127937");
//		mChannelMap.put("600048", "24ea15f47947df47");
//		
//		mChannelMap.put("600052", "9412bc2f7fe9ba23");
//		mChannelMap.put("600053", "662029f6bc600b65");
//		mChannelMap.put("600054", "e70889d9525231de");
//		mChannelMap.put("600055", "bdf8aeed6e44fc57");
//		mChannelMap.put("600056", "d3aa870193427e01");
//		mChannelMap.put("600057", "82932deceaf69da2");
//		mChannelMap.put("600058", "810fd6af04b64d79");
//	}
	
	static  {
		init(ContextUtil.getContext());
	}
	
	/**
	 * 跟踪联通渠道
	 * @param activity
	 */
	public static void trackUnicom(Activity activity) {
		try {
			if (!ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
				// 网络不可用
				return;
			}
			String channel = ActivityUtil.getChannelId(ContextUtil.getContext());
//			Log.e("trackUnicom", "channel=" + channel);
			String promotionId = getUnicomChannel(channel);
//			Log.e(TAG, "promotionId=" + promotionId);
			if (!CheckUtil.isEmpty(promotionId)) {
				// 参数必须用Activity，跟踪代码内部用到了强制转换
				SmartmadTrackingCode.startTracking(activity, promotionId);
				SmartmadTrackingCode.setListener(new SmartmadTrackingCode.SmartmadTrackingCodeListener() {

					@Override
					public void onResponse(int responseCode) {
//						Log.e(TAG, "responseCode=" + responseCode);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	
//	/**
//	 * 跟踪91代码
//	 */
//	public static void track91() {
//		try {
//			if (!ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
//				// 网络不可用
//				return;
//			}
//			if (SharedprefUtil.contains(ContextUtil.getContext(), Settings.IS_FRIST_WITH_NET)) {
//				// 非第一次打开
//				return;
//			}
//			String channel = ActivityUtil.getChannelId(ContextUtil.getContext());
////			Log.e("track91", "channel=" + channel);
//			if (mChannel91.size() == 0 || !mChannel91.contains(channel)) {
//				return;
//			}
//			NDChannel.ndUploadChannelId(appId, ContextUtil.getContext(), new NDChannel.NdChannelCallbackListener() {
//
//				@Override
//				public void callback(int responseCode) {
//					if (responseCode == 0) {
////						Log.e(TAG, "上传成功");
//					} else {
////						Log.e(TAG, "上传失败");
//					}
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 追踪多盟代码
	 * @param activity
	 */
//	public static void trackDomob(Activity activity) {
//		try {
//			if (!ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())) {
//				// 网络不可用
//				return;
//			}
//			String channel = ActivityUtil.getChannelId(ContextUtil.getContext());
////			Log.e("trackDomob", "channel=" + channel);
//			String appId = getDomobChannel(channel);
////			Log.e(TAG, "appId=" + appId);
//			if (!CheckUtil.isEmpty(appId)) {
////				Log.e(TAG, "trackDomob trackActivation, appId=" + appId);
//				DomobTracker.trackActivation(activity, appId);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static String getUnicomChannel(String myChannel) {
		try {
			if (CheckUtil.isEmpty(myChannel)) {
				return "";
			}
			if (mChannelUnicom.containsKey(myChannel)) {
				return mChannelUnicom.get(myChannel);
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String getDomobChannel(String myChannel) {
		try {
			if (CheckUtil.isEmpty(myChannel)) {
				return "";
			}
			if (mChannelDomob.containsKey(myChannel)) {
				return mChannelDomob.get(myChannel);
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}
	
	private static void init(Context context) {
		// 获取联通渠道号信息
		try {
			String data = IOUtils.readStringFromAssets(context, "unich");
			String[] str = data.split("\r\n");
			if (str.length > 0) {
				for (String s : str) {
					String[] tmp = s.trim().split(",");
					if (tmp.length != 2) {
						continue;
					}
					if (tmp[0].trim().equals("") || tmp[1].trim().equals("")) {
						continue;
					}
					mChannelUnicom.put(tmp[0].trim(), tmp[1].trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 获取91渠道号信息
		try {
			String data = IOUtils.readStringFromAssets(context, "91ch");
			String[] str = data.split("\r\n");
			if (str.length > 0) {
				for (String s : str) {
					mChannel91.add(s);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 获取多盟渠道号信息
		try {
			String data = IOUtils.readStringFromAssets(context, "domobch");
			String[] str = data.split("\r\n");
			if (str.length > 0) {
				for (String s : str) {
					String[] tmp = s.trim().split(",");
					if (tmp.length != 2) {
						continue;
					}
					if (tmp[0].trim().equals("") || tmp[1].trim().equals("")) {
						continue;
					}
					mChannelDomob.put(tmp[0].trim(), tmp[1].trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
