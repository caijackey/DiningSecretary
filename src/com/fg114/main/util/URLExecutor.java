package com.fg114.main.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.mealcombo.GroupBuyDetailActivity;
import com.fg114.main.app.activity.mealcombo.MealComboListActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.activity.order.MyNewTakeAwayOrderDetailActivity;
import com.fg114.main.app.activity.order.NewMyOrderDetailActivity;
import com.fg114.main.app.activity.order.NewOrderListAcitivy;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.DishListActivity;
import com.fg114.main.app.activity.resandfood.RecommandRestaurantSubmitActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.activity.resandfood.RestaurantHotsaleActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayFoodDetailActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayIndexActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayRestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwaySearchRestListActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.activity.usercenter.OpinionErronReportActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.service.dto.RestInfoData;

/*
 * 处理url匹配的工具类
 * 内部链接导航的地址  
 * xms://promotions   (优惠页面) 
 * xms://restaurant/<rest's UUID>  (餐厅详情) 
 * xms://search   (全部内容搜索)
 * xms://order/{订单id} (跳转到订单)
 * xms://download/{下载链接} (转到软件下载)
 * 
 * xms://custom/pagename?aa=1&bb=2  (可配置的自定义url)
 * 自定义url的Key对应的参数类型约定，以前缀为标识:i_xxx-Integer, s_xxx-String, b_xxx-Boolean, d_xxx-Double
 * 
 * xms://restaurant/jump/<rest's UUID>/<下一子页面> (转到餐厅详情下的某个子页面)
 * 地图  map
 * 餐厅图片列表 picture
 * 餐馆描述 describe
 * 优惠信息 discount
 * 菜单列表 food
 * 评论列表 comment
 * 
 */

//		
//	$	//搜索(兼容以前)  
//	$	xms://search
//		
//		//搜索和附近搜索
//		//distanceMeter
//		//regionId
//		//districtId
//		//mainMenuId
//		//subMenuId
//		//mainTopRestTypeId
//		//subTopRestTypeId
//		//keywords
//		//sortTypeTag
//		//avgTag
//		subwayTag
//	$	xms://search/q?xxx=xxx&xxx=xxx
//		  
//	$	//餐厅详情页(兼容以前)
//	$	xms://restaurant/{restId}
//		
//		
//		//餐厅详情页跳转(兼容以前)
//		//地图  map
//		//餐厅图片列表 picture
//		//餐馆描述 describe
//		//优惠信息 discount
//		//菜单列表 food
//		//评论列表 comment
//		//预点菜 dish   (这个是新加的)
//		//下单 order   (这个是新加的)
//	$	xms://restaurant/jump/{restId}/{参数}
//		
//	$	//订单列表
//	$	xms://orderlist
//		
//	$	//订单详情(兼容以前)
//	$	xms://order/{orderId}
//		
//	$	//订单详情-提醒用,就是页面左下角功能
//	$	xms://order
//		
//	$	//现金券列表(兼容以前)
//	$	xms://promotions
//		  
//	$	//现金券详情
//	$	xms://coupondetail/{couponId}/{restId}
//		  
//	$	//用户中心
//	$	xms://usercenter
//		  
//	$	//用户登录
//	$	xms://userlogin 
//
//	$	//我的预点菜
//	$	xms://userdishorder/{dishorderId}
//		  
//	$	//关闭页面
//	$	xms://do/close  

//		//短信模板页
//	$	xms://shortmessage/{orderId}/{restId}
//		
//		//去发表评论
//	$	xms://commentsubmit/{orderId}/{restId}/{restName}
//		
//		//去看评论，可以带orderId
//	$	xms://commentlist/{orderId}/{restId}/{restName}
//		
//		//显示一段提示信息的alert对话框
//	$	xms://dialogalert/{message}
//		
//		//跳转到内部wap页
//	$	xms://innerwap/{url}
//
//      跳转到内部wap页 （带各种控制） 4.2.6。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
//      xms://innerwap/{showtitle|hidetitle}/{plusparams|noparams}/{url}
//		
//		//跳转到外部wap页
//	$	xms://outerwap/{url}

//		//餐厅地图页
//	$	xms://restmap/{restId}/{restName}/{restAddress}/{lon}/{lat}
//
//		//餐厅预点菜页
//	$	xms://dishorder/{restId}
//
//		//餐厅菜品图片页
//	$	xms://restfoodpic/{restId}/{restName}

//		//叫出租
//		xms://resttaxi/{orderId}
//
//		//叫代驾
//		xms://restdrive/{orderId}

//      App意见反馈 4.2.6。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
//      xms://appfeedback
public class URLExecutor {

	public static final String NEXT_PAGE_MAP = "map";
	public static final String NEXT_PAGE_PICTURE = "picture";
	public static final String NEXT_PAGE_PICTURE_FOOD = "picture_food";
	public static final String NEXT_PAGE_DESCRIBE = "describe";
	public static final String NEXT_PAGE_DISCOUNT = "discount";
	public static final String NEXT_PAGE_FOOD = "food";
	public static final String NEXT_PAGE_COMMENT = "comment";
	public static final String NEXT_PAGE_DISH = "dish";
	public static final String NEXT_PAGE_ORDER = "order";

	private static List<UrlMatcher> list = new ArrayList<UrlMatcher>();
	static {
		// 使用自带浏览器打开页面
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url.startsWith("link://")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				if (context instanceof Activity) {
					ActivityUtil.jumbToWeb((Activity) context, url.replace("link://", "http://"));
				}
			}
		});
		// 转到餐厅详情下的某个子页面
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (!TextUtils.isEmpty(url) && url.startsWith("xms://restaurant/jump/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				try {
					// 格式：xms://restaurant/jump/{restId}/{参数}
					// //地图 map
					// //餐厅图片列表 picture
					// //餐馆描述 describe
					// //优惠信息 discount
					// //菜单列表 food
					// //评论列表 comment
					// //预点菜 dish (这个是新加的)
					// //下单 order (这个是新加的)
					int start = url.indexOf("xms://restaurant/jump/") + "xms://restaurant/jump/".length();
					int end = url.lastIndexOf("/");
					String restId = ConvertUtil.subString(url, start, end);
					String page = ConvertUtil.subString(url, end + 1);
					
					restId=URLDecoder.decode(restId, "utf-8");
					page=URLDecoder.decode(page, "utf-8");
					
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_ID, restId);
					bundle.putString(Settings.BUNDLE_RES_DETAIL_NEXT_PAGE, page);
					ActivityUtil.jump(context, RestaurantDetailActivity.class, frompage, bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// 执行随手拍功能
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if ("xms://takephoto".equals(url)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// ((MainFrameActivity) activity).takePic(new
				// MainFrameActivity.OnShowUploadImageListener()
				// {
				//
				// @Override
				// public void onGetPic(Bundle bundle)
				// {
				// if (bundle != null) {
				// Settings.JUMP_RIGHT_NOW_FOR_CAPTURE_IS_DONE = true;
				// }
				// }
				// });
			}
		});
		// 优惠页面
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if ("xms://promotions".equals(url)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				Bundle bundle = new Bundle();
				ActivityUtil.jump(context, MealComboListActivity.class, frompage, bundle);

			}
		});
		// 用户中心
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if ("xms://usercenter".equals(url)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				Bundle bundle = new Bundle();
				ActivityUtil.jump(context, UserCenterActivity.class, frompage, bundle);

			}
		});
		// 用户登录
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if ("xms://userlogin".equals(url)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				Bundle bundle = new Bundle();
				ActivityUtil.jump(context, UserLoginActivity.class, frompage, bundle);

			}
		});

		// 全部内容搜索，跳转到搜索列表页
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if ("xms://search".equals(url)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				SessionManager.getInstance().getFilter().reset();
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "返回");
				ActivityUtil.jump(context, ResAndFoodListActivity.class, frompage, bundle);

			}
		});

		// 内容搜索（带参数），跳转到搜索列表页
		registerUrlMatcher(new UrlMatcher() {
			// //搜索和附近搜索
			// //distanceMeter
			// //regionId
			// //districtId
			// //mainMenuId
			// //subMenuId
			// //mainTopRestTypeId
			// //subTopRestTypeId
			// //keywords
			// //sortTypeTag
			// //avgTag
			// xms://search/q?xxx=xxx&xxx=xxx

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://search/q?")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				try {
					Filter filter = SessionManager.getInstance().getFilter();

					String actionUrl = ConvertUtil.subString(url, url.lastIndexOf("/") + 1);
					actionUrl=URLDecoder.decode(actionUrl, "utf-8");
					
					Bundle param = getBundleFromUrl(actionUrl);
					String distanceMeter = param.getString("distanceMeter");
					String regionId = param.getString("regionId");
					String districtId = param.getString("districtId");
					String mainMenuId = param.getString("mainMenuId");
					String subMenuId = param.getString("subMenuId");
					String mainTopRestTypeId = param.getString("mainTopRestTypeId");
					String subTopRestTypeId = param.getString("subTopRestTypeId");
					String keywords = param.getString("keywords");
					String sortTypeTag = param.getString("sortTypeTag");
					String avgTag = param.getString("avgTag");
					String subwayTag = param.getString("subwayTag");

//					distanceMeter=URLDecoder.decode(distanceMeter, "utf-8");
//					regionId=URLDecoder.decode(regionId, "utf-8");
//					districtId=URLDecoder.decode(districtId, "utf-8");
//					mainMenuId=URLDecoder.decode(mainMenuId, "utf-8");
//					subMenuId=URLDecoder.decode(subMenuId, "utf-8");
//					mainTopRestTypeId=URLDecoder.decode(mainTopRestTypeId, "utf-8");
//					subTopRestTypeId=URLDecoder.decode(subTopRestTypeId, "utf-8");
//					keywords=URLDecoder.decode(keywords, "utf-8");
//					sortTypeTag=URLDecoder.decode(sortTypeTag, "utf-8");
//					avgTag=URLDecoder.decode(avgTag, "utf-8");
//					subwayTag=URLDecoder.decode(subwayTag, "utf-8");
					
					filter.setDistanceMeter(distanceMeter == null ? 0 : Integer.parseInt(distanceMeter));
					filter.setRegionId(regionId == null ? "0" : regionId);
					filter.setDistrictId(districtId == null ? "0" : districtId);
					filter.setMainMenuId(mainMenuId == null ? "0" : mainMenuId);
					filter.setSubMenuId(subMenuId == null ? "0" : subMenuId);
					filter.setMainTopRestTypeId(mainTopRestTypeId == null ? "" : mainTopRestTypeId);
					filter.setSubTopRestTypeId(subTopRestTypeId == null ? "" : subTopRestTypeId);
					filter.setKeywords(keywords == null ? "" : keywords);
					filter.setSortTypeTag(sortTypeTag == null ? 1 : Integer.parseInt(sortTypeTag));
					filter.setAvgTag(avgTag == null ? "0" : avgTag);
					filter.setSubwayTag(subwayTag == null || "false".equals(subwayTag) ? false : true);

					Bundle bundle = new Bundle();
					if (filter.getDistanceMeter() != 0) {
						bundle.putInt(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE, Settings.RES_AND_FOOD_LIST_TYPE_NEARBY);
					} else {
						bundle.putInt(Settings.BUNDLE_RES_AND_FOOD_LIST_TYPE, Settings.RES_AND_FOOD_LIST_TYPE_SEARCH);
					}
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "返回");
					ActivityUtil.jump(context, ResAndFoodListActivity.class, frompage, bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		// 餐厅详情
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://restaurant/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 17;
				Bundle bundle = new Bundle();
				String restID=url.substring(start);
				try {
					restID=URLDecoder.decode(restID, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_KEY_ID, restID);
				ActivityUtil.jump(context, RestaurantDetailActivity.class, frompage, bundle);

			}
		});

		// 我的预点菜
		registerUrlMatcher(new UrlMatcher() {
			// xms://userdishorder/{dishorderId}
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://userdishorder/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 20;
				Bundle bundle = new Bundle();
				String uuid=url.substring(start);
				bundle.putString(Settings.UUID, uuid);
				bundle.putInt(Settings.FROM_TAG, 2); // 来自哪里 1：餐厅页或订单页 2：我的菜单
				ActivityUtil.jump(context, DishListActivity.class, frompage, bundle);

			}
		});
		// 跳转到订单详情（带订单号）
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://order/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 12;
				Bundle bundle = new Bundle();
				String uuid=url.substring(start);
				try {
					uuid=URLDecoder.decode(uuid, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_ORDER_ID, uuid);
				ActivityUtil.jump(context, NewMyOrderDetailActivity.class, frompage, bundle);

			}
		});
		// 跳转到订单详情（不带订单号）
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.equals("xms://order")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_ORDER_ID, "");
				ActivityUtil.jump(context, NewMyOrderDetailActivity.class, frompage, bundle);

			}
		});
		// 跳转到订单列表
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.equals("xms://orderlist")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				Bundle bundle = new Bundle();
				ActivityUtil.jump(context, NewOrderListAcitivy.class, frompage, bundle);

			}
		});
		// 跳转到订单表单
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://orderpost/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 16;
				Bundle bundle = new Bundle();
				// 格式：xms://orderpost/{restId}/{restName}
				// 格式：xms://orderpost/{restId}/{restName}/{activityId}/{activityName}(带活动)
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				String[] data = temp.split("/");
				if (data.length != 2 && data.length != 4) {
					return;
				}

				try {
					data[0] = URLDecoder.decode(data[0], "utf-8");
					data[1] = URLDecoder.decode(data[1], "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				bundle.putString(Settings.BUNDLE_REST_ID, data[0]);
				bundle.putString(Settings.BUNDLE_REST_NAME, data[1]);
				if (data.length == 4) {
					try {
						data[2] = URLDecoder.decode(data[2], "utf-8");
						data[3] = URLDecoder.decode(data[3], "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					bundle.putString(Settings.BUNDLE_Activity_ID, data[2]);
					bundle.putString(Settings.BUNDLE_Activity_Detail, data[3]);
				}
				ActivityUtil.jump(context, MyBookRestaurantActivity.class, frompage, bundle);

			}
		});
		// // 跳转到订单表单(带活动)
		// registerUrlMatcher(new UrlMatcher() {
		//
		// @Override
		// public boolean isMatched(String url, Context context) {
		// if (url != null && url.startsWith("xms://orderpost/")) {
		// return true;
		// }
		// return false;
		// }
		//
		// @Override
		// public void doAction(String url, Context context, int frompage) {
		// int start = 16;
		// Bundle bundle = new Bundle();
		// // 格式：xms://orderpost/{restId}/{restName}/{activityId}/{activityName}
		// String temp = url.substring(start);
		// if (CheckUtil.isEmpty(temp)) {
		// return;
		// }
		// String[] data = temp.split("/");
		// if (data.length != 4) {
		// return;
		// }
		//
		// try {
		// data[1] = URLDecoder.decode(data[1], "utf-8");
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		//
		// try {
		// data[3] = URLDecoder.decode(data[3], "utf-8");
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		//
		// bundle.putString(Settings.BUNDLE_REST_ID, data[0]);
		// bundle.putString(Settings.BUNDLE_REST_NAME, data[1]);
		// bundle.putString(Settings.BUNDLE_Activity_ID, data[2]);
		// bundle.putString(Settings.BUNDLE_Activity_Detail, data[3]);
		// ActivityUtil.jump(context, MyBookRestaurantActivity.class, frompage,
		// bundle);
		//
		// }
		// });

		// 跳转到现金券详情页
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://coupondetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 19;
				Bundle bundle = new Bundle();
				// 格式：xms://coupondetail/{couponId}/{restId}
				//xms://coupondetail/{couponId}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				String[] data = temp.split("/");
				if (data.length != 2&&data.length!=1) {
					return;
				}
				
				if(data.length==1){
				bundle.putString(Settings.BUNDLE_REST_ID, "");
				}else{
					try {
						data[1]=URLDecoder.decode(data[1], "utf-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				bundle.putString(Settings.BUNDLE_REST_ID, data[1]);	
				}
				try {
					data[0]=URLDecoder.decode(data[0], "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.UUID, data[0]);
				ActivityUtil.jump(context, GroupBuyDetailActivity.class, frompage, bundle);

			}
		});
		// 拨打电话
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url.startsWith("tel://")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				String phoneNo = url.replace("tel://", "");
				ActivityUtil.callSuper57(context, phoneNo);
			}
		});
		// 跳转到下载软件
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://download/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 15;
				Bundle bundle = new Bundle();
				// bundle.putString(Settings.BUNDLE_KEY_ID,
				// url.substring(start));
				// if(Settings.gVersionChkDTO==null||!Settings.gVersionChkDTO.isHaveNewVersionTag()){
				// return;
				// }
				// bundle.putString(Settings.BUNDLE_KEY_CONTENT,
				// Settings.gVersionChkDTO.getDownloadUrl());
				bundle.putString(Settings.BUNDLE_KEY_CONTENT, url.substring(start));
				ActivityUtil.jump(context, AutoUpdateActivity.class, frompage, bundle);

			}
		});
		// 跳转到自定义页面
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://custom/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				try {
					url = ConvertUtil.subString(url, url.lastIndexOf("/") + 1);
					int start = url.indexOf("/") + 1;
					int end = url.indexOf("?");
					String pageName = ConvertUtil.subString(url, start, end);
					Class<?> cls = Class.forName(pageName);
					String actionUrl = ConvertUtil.subString(url, start);
					Bundle bundle = getBundleFromUrl(actionUrl);
					ActivityUtil.jump(context, cls, frompage, bundle);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		//
		registerUrlMatcher(new UrlMatcher() {
			// //短信模板页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://shortmessage/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 19;
				Bundle bundle = new Bundle();
				// 格式：xms://shortmessage/{orderId}/{restId}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				String[] data = temp.split("/");
				if (data.length != 2) {
					return;
				}
				try {
					data[0]=URLDecoder.decode(data[0], "utf-8");
					data[1]=URLDecoder.decode(data[1], "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_ORDER_ID, data[0]);
				bundle.putString(Settings.BUNDLE_REST_ID, data[1]);
				ActivityUtil.jump(context, SelectSMSActivity.class, 0, bundle);

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 去发表评论
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://commentsubmit/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 20;
				Bundle bundle = new Bundle();
				// 格式：xms://commentsubmit/{orderId}/{restId}/{restName}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				String[] data = temp.split("/");
				if (data.length != 3) {
					return;
				}
				try {
					data[0] = URLDecoder.decode(data[0], "utf-8");
					data[1] = URLDecoder.decode(data[1], "utf-8");
					data[2] = URLDecoder.decode(data[2], "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_ORDER_ID, data[0]);
				bundle.putString(Settings.BUNDLE_KEY_ID, data[1]);
				bundle.putString(Settings.BUNDLE_REST_NAME, data[2]);
				bundle.putLong(Settings.FROM_TAG, 2);
				ActivityUtil.jump(context, RestaurantCommentSubmitActivity.class, 0, bundle);

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 去看评论，可以带orderId
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://commentlist/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 18;
				Bundle bundle = new Bundle();
				// 格式：xms://commentlist/{orderId}/{restId}/{restName}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				String[] data = temp.split("/");
				if (data.length != 3) {
					return;
				}
				try {
					data[0] = URLDecoder.decode(data[0], "utf-8");
					data[1] = URLDecoder.decode(data[1], "utf-8");
					data[2] = URLDecoder.decode(data[2], "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_ORDER_ID, data[0]);
				bundle.putString(Settings.BUNDLE_KEY_ID, data[1]);
				bundle.putString(Settings.BUNDLE_REST_NAME, data[2]);
				ActivityUtil.jump(context, RestaurantCommentActivity.class, 0, bundle);

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 显示一段提示信息的alert对话框
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://dialogalert/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 18;
				Bundle bundle = new Bundle();
				// 格式：xms://dialogalert/{message}
				String temp = url.substring(start);
				try {
					temp = URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (!CheckUtil.isEmpty(temp)) {
					DialogUtil.showAlert(context, "提示", temp);
				}
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 跳转到内部wap页 xms://innerwap/
			// 跳转到内部wap页 （带各种控制） 4.2.6。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
			// xms://innerwap/{showtitle|hidetitle}/{plusparams|noparams}/{url}
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://innerwap/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 15;
				Bundle bundle = new Bundle();
				// 格式：xms://innerwap/{url}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}

				String[] data = temp.split("/");
 
				if (data.length != 3 && data.length != 1) {
					return;
				}

				if (data.length == 1) {
					try {
						data[0] = URLDecoder.decode(data[0], "utf-8");
						
						ActivityUtil.jumpToWebNoParam(data[0], "", false);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						}
					
				}

				if (data.length == 3) {

					try {
						data[2] = URLDecoder.decode(data[2], "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					if (data[0].equals("hidetitle") && data[1].equals("plusparams")) {
						ActivityUtil.jumpToWeb(data[2], "", true);
					}
					if (data[0].equals("showtitle") && data[1].equals("plusparams")) {
						ActivityUtil.jumpToWeb(data[2], "", false);
					}
					if (data[0].equals("hidetitle") && data[1].equals("noparams")) {
						ActivityUtil.jumpToWebNoParam(data[2], "", true);
					}
					if (data[0].equals("showtitle") && data[1].equals("noparams")) {
						ActivityUtil.jumpToWebNoParam(data[2], "", false);
					}
				}
			}
		});


		registerUrlMatcher(new UrlMatcher() {
			// 跳转到外部wap页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://outerwap/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 15;
				Bundle bundle = new Bundle();
				// 格式：xms://outerwap/{url}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp = URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ActivityUtil.jumbToWeb((Activity) context, temp);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 餐厅地图页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://restmap/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 14;
				Bundle bundle = new Bundle();
				// 格式：xms://restmap/{restId}/{restName}/{restAddress}/{lon}/{lat}
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}

				String[] data = temp.split("/");
				if (data.length != 5) {
					return;
				}
				//
				RestInfoData restInfo = new RestInfoData();
				try {
					restInfo.uuid =URLDecoder.decode(data[0], "utf-8");
					restInfo.name = URLDecoder.decode(data[1], "utf-8");
					restInfo.address = URLDecoder.decode(data[2], "utf-8");
					restInfo.longitude = Double.parseDouble(data[3]);
					restInfo.latitude = Double.parseDouble(data[4]);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (!RestaurantDetailActivity.showMap(restInfo, (Activity) context)) {
					DialogUtil.showToast(context, "无法打开地图模式");
				}
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 餐厅预点菜页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://dishorder/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://dishorder/{restId}
				int start = 16;
				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, url.substring(start));
				bundle.putInt(Settings.FROM_TAG, 1); // 来自哪里 1：餐厅页或订单页 2：我的菜单
				ActivityUtil.jump(context, DishListActivity.class, frompage, bundle);
			}
		});

		// 意见反馈页
		registerUrlMatcher(new UrlMatcher() {

			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://appfeedback")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				int start = 17;
				Bundle bundle = new Bundle();
				ActivityUtil.jump(context, OpinionErronReportActivity.class, frompage, bundle);

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 餐厅菜品图片页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://restfoodpic/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://restfoodpic/{restId}/{restName}
				int start = 18;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				// --
				String[] data = temp.split("/");
				if (data.length != 2) {
					return;
				}
				// --
				try {
					data[0] = URLDecoder.decode(data[0], "utf-8");
					data[1] = URLDecoder.decode(data[1], "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_KEY_ID, data[0]);
				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_FOOD);
				ActivityUtil.jump(context, RestaurantPicActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 叫出租
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://resttaxi/")) {
					return true;
				}
				return false;
			}

			// http://m.xiaomishu.com/appwap/rent?orderId={orderId}
			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://resttaxi/{orderId}
				int start = 15;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				ActivityUtil.jumpToWeb("http://m.xiaomishu.com/appwap/rent", "叫出租", true, new BasicNameValuePair("orderId", temp));
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 叫代驾
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://restdrive/")) {
					return true;
				}
				return false;
			}

			// http://m.xiaomishu.com/appwap/drive?orderId={orderId}
			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://restdrive/{orderId}
				int start = 16;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				ActivityUtil.jumpToWeb("http://m.xiaomishu.com/appwap/drive", "叫代驾", true, new BasicNameValuePair("orderId", temp));
			}
		});
		registerUrlMatcher(new UrlMatcher() {
			// 推荐详情
			@Override
			public boolean isMatched(String url, Context context) {

				if (url != null && url.startsWith("xms://restrecomdetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://restrecomdetail/{recomId}
				// 格式：xms://restrecomdetail/{recomId}/anchorcomment

				int start = 22;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp = URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (temp.contains("/anchorcomment")) {
					// 跳详情区域
					temp = temp.substring(0, temp.indexOf("/"));

					bundle.putBoolean(Settings.JUMP_RESCOMMEND_RES_COMMENT_AREA, true);
				}
				
				bundle.putString(Settings.BUNDLE_REST_ID, temp);
				bundle.putInt(Settings.BUNDLE_showTypeTag, 2);
				ActivityUtil.jump(context, RestaurantDetailMainActivity.class, 0, bundle);
			}
		});
		registerUrlMatcher(new UrlMatcher() {
			// 添加推荐
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://addrestrecom")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// 格式：xms://addrestrecom
				// 格式：xms://addrestrecom/{restId}/{restName}
				if (url.length() == 18) {
					url = url + "/";
				}
				int start = 19;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);

				if (!CheckUtil.isEmpty(temp)) {
					String[] data = temp.split("/");
					if (data.length != 2) {
						return;
					}
					try {
						data[0] = URLDecoder.decode(data[0], "utf-8");
						data[1] = URLDecoder.decode(data[1], "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					bundle.putString(Settings.BUNDLE_REST_ID, data[0]);
					bundle.putString(Settings.BUNDLE_REST_NAME, data[1]);
					ActivityUtil.jump(context, RecommandRestaurantSubmitActivity.class, 0, bundle);
				}else{
					bundle.putString(Settings.BUNDLE_REST_ID,"");
					bundle.putString(Settings.BUNDLE_REST_NAME, "");
					ActivityUtil.jump(context, RecommandRestaurantSubmitActivity.class, 0, bundle);
				}
				

			}
		});
		

		registerUrlMatcher(new UrlMatcher() {
			// 外卖首页 4.2.6
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutindex")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutindex

				ActivityUtil.jump(context, NewTakeAwayIndexActivity.class, 0, new Bundle());

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 需要登录
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://do/needLogin")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://do/needLogin
				SimpleWebViewActivity.needCallFinishLogin = true;
				ActivityUtil.jump(context, UserLoginActivity.class, 0, new Bundle());

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖列表页
			@Override
			public boolean isMatched(String url, Context context) {
				// xms://takeoutlist 开头但不能以xms://takeoutlist/q?开头
				if (url != null && (url.startsWith("xms://takeoutlist") && url.startsWith("xms://takeoutlist/q?") == false)) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutlist
				ActivityUtil.jump(context, NewTakeAwaySearchRestListActivity.class, 0, new Bundle());

			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖餐厅详情页 4.2.6
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutdetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutdetail/{takeoutId}
				int start = 20;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp=URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_KEY_ID, temp);
				ActivityUtil.jump(context, NewTakeAwayRestaurantDetailActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖点菜页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutmenulist/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutmenulist/{takeoutId}
				int start = 22;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp=URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.UUID, temp);
				ActivityUtil.jump(context, TakeAwayNewFoodListActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖菜品详情页 4.2.6
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutmenudetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutmenudetail/{uuid}
				int start = 24;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp=URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_FOOD_ID, temp);
				ActivityUtil.jump(context, NewTakeAwayFoodDetailActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖订单列表页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutorderlist")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutorderlist
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_FROM_TAG, 1);
				ActivityUtil.jump(context, NewOrderListAcitivy.class, 0, bundle);
			}
		});
		
		registerUrlMatcher(new UrlMatcher() {
			 //添加新餐厅
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://reportnewrest")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				//xms://reportnewrest
				ActivityUtil.jumpNotForResult(context, AddOrUpdateResActivity.class, new Bundle(), false);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 外卖订单详情页
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutorderdetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://takeoutorderdetail/{orderId}
				int start = 25;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp=URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_ORDER_ID, temp);
				ActivityUtil.jump(context, MyNewTakeAwayOrderDetailActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 榜单餐厅列表
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://toprestlist/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://toprestlist/{typeId}
				int start = 18;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);
				if (CheckUtil.isEmpty(temp)) {
					return;
				}
				try {
					temp=URLDecoder.decode(temp, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bundle.putString(Settings.BUNDLE_REST_TYPEID, temp);
				ActivityUtil.jump(context, RestaurantHotsaleActivity.class, 0, bundle);
			}
		});

		registerUrlMatcher(new UrlMatcher() {
			// 榜单餐厅详情
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://toprestdetail/")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				// xms://toprestdetail/{typeId}/{restId}

				if (url.length() == 19) {
					url = url + "/";
				}
				int start = 20;
				Bundle bundle = new Bundle();
				String temp = url.substring(start);

				if (!CheckUtil.isEmpty(temp)) {

					String[] data = temp.split("/");
					if (data.length != 2) {
						return;
					}
					try {
						data[0] = URLDecoder.decode(data[0], "utf-8");
						data[1] = URLDecoder.decode(data[1], "utf-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					bundle.putString(Settings.BUNDLE_REST_TYPE_ID, data[0]);
					bundle.putString(Settings.BUNDLE_KEY_ID, data[1]);
					bundle.putInt(Settings.BUNDLE_TPYE_TAG, 3);
				}
				ActivityUtil.jump(context, RestaurantDetailActivity.class, 0, bundle);
			}
		});

		// 外卖url解析 by yan
		registerUrlMatcher(new UrlMatcher() {
			// 外卖餐厅列表页面
			// typeId 餐厅类别
			// sendLimitId 起送费
			// keywords 关键词
			// userGpsTag //默认为true 如果为false 请指定用户当前longitude latitude
			// positionName 来进行指定位置搜索
			// longitude
			// latitude
			// positionName
			// xms://takeoutlist/q?xxx=xxx&xxx=xxx
			@Override
			public boolean isMatched(String url, Context context) {
				if (url != null && url.startsWith("xms://takeoutlist/q?")) {
					return true;
				}
				return false;
			}

			@Override
			public void doAction(String url, Context context, int frompage) {
				try {
					TakeAwayFilter filter = SessionManager.getInstance().getTakeAwayFilter();
					String savePosName = filter.getPoiName();
					String saveKeyWords = filter.getKeywords();
					String saveTypeId = filter.getTypeId();
					String saveSendLimitId = filter.getSendLimitId();
					double saveLongitude = filter.getLongitude();
					double saveLatitude = filter.getLatitude();

					String actionUrl = ConvertUtil.subString(url, url.lastIndexOf("/") + 1);
					actionUrl=URLDecoder.decode(actionUrl, "utf-8");
					
					Bundle param = getBundleFromUrl(actionUrl);
					String typeId = param.getString("typeId");
					String sendLimitId = param.getString("sendLimitId");
					String keywords = param.getString("keywords");
					String userGpsTag = param.getString("userGpsTag");
					String longitude = param.getString("longitude");
					String latitude = param.getString("latitude");
					String positionName = param.getString("positionName");
					

					filter.setTypeId(typeId == null ? "" : typeId);
					filter.setSendLimitId(sendLimitId == null ? "" : sendLimitId);
					filter.setKeywords(keywords == null ? "" : keywords);
					boolean userGpsTagVal = (userGpsTag == null || "true".equals(userGpsTag)) ? true : false;
					// 如果使用用户自己的位置信息清空
					if (userGpsTagVal == true) {
						filter.setLatitude(0);
						filter.setLongitude(0);
						filter.setPoiName("");
					} else {
						filter.setLatitude(latitude == null ? 0 : Double.parseDouble(latitude));
						filter.setLongitude(longitude == null ? 0 : Double.parseDouble(longitude));
						filter.setPoiName(positionName == null ? "" : positionName);
					}

					Bundle bundle = new Bundle();
					bundle.putInt(Settings.WAIMAI_BUNDLE_URLFROMAD, 1);
					// 把需要保存的数据放入bundle带到activity，以便后面恢复用
					bundle.putString(Settings.WAIMAI_BUNDLE_POSITIONNAME, savePosName);
					bundle.putString(Settings.WAIMAI_BUNDLE_KEYWORDS, saveKeyWords);
					bundle.putString(Settings.WAIMAI_BUNDLE_TYPEID, saveTypeId);
					bundle.putString(Settings.WAIMAI_BUNDLE_SENDLIMITID, saveSendLimitId);
					bundle.putDouble(Settings.WAIMAI_BUNDLE_LATITUDE, saveLatitude);
					bundle.putDouble(Settings.WAIMAI_BUNDLE_LONGITUDE, saveLongitude);
					ActivityUtil.jump(context, NewTakeAwaySearchRestListActivity.class, frompage, bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	private static void registerUrlMatcher(UrlMatcher matcher) {
		list.add(matcher);
	}

	/**
	 * 执行一个url，如果有已注册的url处理器，则会执行处理，并且返回true，否则返回false
	 * 
	 * @param url
	 * @param context
	 * @param frompage
	 * @return 返回true表示有匹配的url处理器
	 */

	public static boolean execute(String url, Context context, int frompage) {

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isMatched(url, context)) {
				list.get(i).doAction(url, context, frompage);
				return true;
			}
		}

		return false;
	}

	private abstract static class UrlMatcher {
		public abstract boolean isMatched(String url, Context context);

		public abstract void doAction(String url, Context context, int frompage);
	}

	/**
	 * 根据url获得自定义跳转的Bundle
	 * 
	 * @param url
	 * @return
	 */
	private static Bundle getBundleFromUrl(String url) {
		Bundle bundle = new Bundle();
		String params = ConvertUtil.subString(url, url.indexOf("?") + 1);
		String[] paramsArray = params.split("&");
		if (paramsArray == null || paramsArray.length == 0) {
			return bundle;
		}
		for (String param : paramsArray) {
			if (TextUtils.isEmpty(param) || param.indexOf("=") < 1) {
				continue;
			}
			String[] result = param.split("=");
			if (result == null || result.length != 2 || TextUtils.isEmpty(result[1])) {
				continue;
			}
			if (result[0].startsWith("s_")) {
				bundle.putString(ConvertUtil.subString(result[0], result[0].indexOf("s_") + 2), result[1]);
			} else if (result[0].startsWith("i_")) {
				bundle.putInt(ConvertUtil.subString(result[0], result[0].indexOf("i_") + 2), Integer.parseInt(result[1]));
			} else if (result[0].startsWith("b_")) {
				bundle.putBoolean(ConvertUtil.subString(result[0], result[0].indexOf("b_") + 2), Boolean.parseBoolean(result[1]));
			} else if (result[0].startsWith("d_")) {
				bundle.putDouble(ConvertUtil.subString(result[0], result[0].indexOf("d_") + 2), Double.parseDouble(result[1]));
			} else {
				bundle.putString(result[0], result[1]);
			}
		}
		return bundle;
	}
}