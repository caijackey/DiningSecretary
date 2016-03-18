//package com.fg114.main.analytics;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import android.util.Log;
//
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.NewFeatureActivity;
//import com.fg114.main.app.data.CityInfo;
//import com.fg114.main.util.CheckUtil;
//import com.fg114.main.util.ContextUtil;
//import com.fg114.main.util.MyThreadPool;
//import com.fg114.main.util.SessionManager;
//import com.google.android.apps.analytics.GoogleAnalyticsTracker;
//
///**
// * 用于跟踪页面浏览信息的工具类。
// * @deprecated
// * @author xujianjun, 2012-03-13
// * 
// */
//public class TraceManager {
//
////	private static TraceManager instance = null;
////	private GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
////	//private String UA = "UA-29889287-1";   //测试帐号
////	private String UA = "UA-3214743-31";	//产品帐号
////	private static MyThreadPool traceThread;
////	private static boolean DEBUG = false;
////
////	// ---跟踪模式，使用ga的按时间间隔发送或者手动发送（使用自定义线程池）
////	private static boolean isManualMode = false;
////	private static int intervalSeconds = 5; // 当是按时间间隔发送时，间隔的秒数
////
////	// 是否开启跟踪功能
////	private static boolean ENABLED = true;
////	//
////	static {
////		// google说，GoogleAnalyticsTracker的方法要在相同的线程里运行，否则会有诡异的问题，因此搞一个单线程的池达到此目的
////		// 设置发送间隔是6秒，发现google ga不是很稳定，间隔太短的话容易失败
////		traceThread = new MyThreadPool(1, -1, 6000);
////	}
////
////	private TraceManager() {
////
////		start();
////		//自定义变量，渠道号
////		//index		name	value	scope
////		//1			渠道号	版本号	1
////		/**
////		 * 设置自定义变量时的参数：index, name, value, opt_scope[1,2,3]
////		 * 
////		 * index — 自定义变量的位置。必填。此变量是一个数字，其值范围为 1 - 5（包含 1 和5）。
////		 * 			一个自定义变量只能放在一个位置，且不得在不同位置重复使用。
////		 * 
////		 * name — 自定义变量的名称。必填。此变量是一个字符串，用于标识自定义变量并显示在 
////		 * 			Google Analytics（分析）报告的顶级“自定义变量”报告中。 
////		 * 
////		 * value — 自定义变量的值。必填。此变量是一个字符串，与名称配对。您可以将多个值
////		 * 			与一个自定义变量名称配对。此值显示在所选变量名称的用户界面表格列表中。
////		 * 			通常，一个给定名称会具有两个或多个值。例如，您可以定义一个自定义变量
////		 * 			名称“gender”，并提供“male”和“female”作为两个可能的值。 
////		 * opt_scope — 自定义变量的范围。可选。如上所述，范围定义用户对您网站的参与级别。
////		 * 			此变量是一个数字，其可能的值包括 1（访问者级）、2（会话级）和3（网页级）。
////		 * 			当未定义时，自定义变量范围默认为网页级的互动。
////		 */
////		if (tracker.getVisitorCustomVar(1) == null) {
////			if (DEBUG)
////				Log.d("*****Var 设置", "OK");
////			tracker.setCustomVar(1, Settings.SELL_CHANNEL_NUM, Settings.VERSION_NAME, 1);
////		} else {
////			if (DEBUG)
////				Log.d("*****Var 读取", "值：" + tracker.getVisitorCustomVar(1));
////		}
////	}
////
////	synchronized public static TraceManager getInstance() {
////		if (instance == null) {
////			instance = new TraceManager();
////		}
////		return instance;
////	}
////
////	public void start() {
////		//
////		if (isManualMode) {
////			tracker.startNewSession(UA, ContextUtil.getContext());
////
////		} else {
////			tracker.startNewSession(UA, intervalSeconds, ContextUtil.getContext());
////
////		}
////	}
////
////	public void stop() {
////		tracker.stopSession();
////	}
////
////	public void reset() {
////		stop();
////		start();
////	}
////
////	/**
////	 * @param url
////	 *            如果不是空字符串，必须以"/"开头
////	 */
////	public void enterPage(String url) {
////		try {
////
////			if (!ENABLED){
////				return;
////			}
////				
////			if(url==null){
////				return;
////			}
////			//----
////			final String realUrl=getBaseUrl()+url;
////
////			
////			// 手动模式时使用线程池工作
////			if (isManualMode) {
////				traceThread.submit(new MyThreadPool.Task() {
////
////					@Override
////					public void run() {
////
////						tracker.trackPageView(realUrl);
////						if (DEBUG)
////							Log.d("** TraceManager **", "enter page: " + realUrl);
////						
////						boolean result = tracker.dispatch();
////						if (!result) {
////							if (DEBUG)
////								Log.e("@@ TraceManager @@", "error: dispatch() returned false!");
////						} else {
////							if (DEBUG)
////								Log.d("@@ TraceManager @@", "dispatch() returned true!");
////						}
////					}
////
////				});
////			} else {
////				tracker.trackPageView(realUrl);
////				if (DEBUG)
////					Log.d("** TraceManager **", "enter page: " + realUrl);
////			}
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
////
////	// additionalUrl是用来直接附加在生成的url后面的字符串，目的是拼装成一个完整的url，通常用来构造搜索型url的查询参数
////	/**@deprecated
////	 * @param key
////	 * @param additionalUrl
////	 */
////	public void enterPage(Object key, String additionalUrl) {
////		if (key == null) {
////			return;
////		}
////		String oname = key.getClass().getCanonicalName();
////		String url = this.getUrlByObjectName(oname);
////		if (CheckUtil.isEmpty(url)) {
////			return;
////		}
////		if (additionalUrl == null) {
////			additionalUrl = "";
////		}
////		url = url + additionalUrl;
////		enterPage(url);
////	}
////
////	/**@deprecated
////	 * @param key
////	 */
////	public void enterPage(Object key) {
////		enterPage(key, "");
////	}
////
////	// 获取基础url，当前规则是："/城市名"
////	private String getBaseUrl() {
////		String cityName=null;
////		cityName=getCityName();
////		if (cityName != null) {
////			return "/" + cityName;
////		} else {
////			return "";
////		}
////	}
////	// 获取城市名
////	private String getCityName() {
////		CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
////		if (city != null) {
////			return city.getName();
////		} else {
////			return null;
////		}
////	}
////
////	// 工具方法，返回需要跟踪的Object（通常是个Activity）的全名的对应url，用来做为enterPage(url)的参数
////	/**@deprecated
////	 * @param objectName
////	 * @return
////	 */
////	public String getUrlByObjectName(String objectName) {
////		return map.get(objectName);
////	}
////
////	// 发送一个事件
////	public void dispatchEvent(final String category, final String action, final String label, final int value) {
////		try {
////			if (!ENABLED)
////				return;
////			final String realCategory=getCityName()+"-"+category;
////			if (isManualMode) {
////				traceThread.submit(new MyThreadPool.Task() {
////
////					@Override
////					public void run() {
////						tracker.trackEvent(realCategory, action, label, value);
////						if (DEBUG)
////							Log.d("** TraceManager **", "trackEvent: category=" + realCategory + ", action=" + action + ", label=" + label + ", value=" + value);
////					}
////
////				});
////			} else {
////				tracker.trackEvent(realCategory, action, label, value);
////				if (DEBUG)
////					Log.d("** TraceManager **", "trackEvent: category=" + realCategory + ", action=" + action + ", label=" + label + ", value=" + value);
////			}
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
////
////	// 下面的map可以充当需要跟踪的页面的过滤器和object名到url的转换器
////	/**
////	 * @deprecated
////	 */
////	private HashMap<String, String> map = new HashMap<String, String>();
////	{
////		// 榜单列表 /toplist
////		// 附近餐厅列表 /nearby
////		// 搜索列表 /search
////		// 优惠精选列表 /promote
////		// 餐厅详情页面 /rest/detail
////
////		map.put("com.fg114.main.app.activity.top.TopRestaurantListActivity", "/test1/search/toplist/?type=res");
////		map.put("com.fg114.main.app.activity.resandfood.NearbyResAndFoodListActivity", "/test1/search/nearby/?");
////		map.put("com.fg114.main.app.activity.resandfood.ResAndFoodListActivity", "/test1/search/search/?");
////		map.put("com.fg114.main.app.activity.resandfood.FavourableActivity", "/test1/search/promote/?");
////		map.put("com.fg114.main.app.activity.resandfood.RestaurantDetailActivity", "/test1/rest/detail");
////		// 测试用
////		map.put("java.lang.String", "/shop/search/?type=nearby_res");
////	}
//}
