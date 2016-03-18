package com.fg114.main.service.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CipherUtils;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.TestUtil;
import com.google.xiaomishujson.Gson;

/**
 * 获得后台数据方法类
 * 
 */
public class A57HttpApiV3 {

	private static final String TAG = "A57HttpApiV3";
	private static boolean DEBUG = Settings.DEBUG;

	// 单态实例
	private static A57HttpApiV3 instance = null;

	public HttpApi mHttpApi;
	public String mApiBaseUrl;

	/** url */
	// private static final String URL_API_SOFT_FIRST_OPEN = "/softFirstOpen";
	private static final String URL_API_CHK_VERSION = "/chkVersion";
	private static final String URL_API_GET_CITY_BY_GPS = "/getCityByGps";
	private static final String URL_API_GET_PAGE_INDEX_DATA = "/getPageIndexData";
	// 获得城市列表URL
	private static final String URL_API_GET_CITY_LIST = "/getCityList";
	// 获得餐厅与美食列表URL
	private static final String URL_API_GET_RES_AND_FOOD_LIST = "/getResAndFoodList";
	// 获得餐厅美食列表URL
	private static final String URL_API_GET_RES_FOOD_LIST = "/getResFoodList";
	// 获得餐厅详细URL
	private static final String URL_API_GET_PAGE_RES_INFO_DATA = "/getPageResInfoData";
	// 获得餐厅图片URL
	private static final String URL_API_GET_RES_PIC_LIST = "/getResPicList";
	// 获得餐厅评论URL
	private static final String URL_API_GET_RES_COMMENT_LIST = "/getResCommentList";
	// 获得榜单类别URL
	private static final String URL_API_GET_TOP_RES_LIST_TYPE_DATA = "/getTopResListTypeData";
	// 获得榜单餐厅URL
	private static final String URL_API_GET_TOP_RES_LIST = "/getTopResList";
	// 获得热门商圈URL
	private static final String URL_API_GET_PAGE_SELECT_HOT_DISTRICT_DATA = "/getPageSelectHotDistrictData";
	// 获得地域列表URL
	private static final String URL_API_GET_REGION_LIST = "/getRegionList";
	// 获得商区列表URL
	private static final String URL_API_GET_DISTRICT_LIST = "/getDistrictList";
	// 用户登录URL
	private static final String URL_API_USER_LOGIN = "/userLogin";
	// 用户注册URL
	private static final String URL_API_USER_REGIST = "/userReg";
	// 获得天天七折URL
	private static final String URL_API_GET_DAY_DAY_DISCOUNT_LIST = "/getDayDayDiscountList";
	// 提交订单URL
	private static final String URL_API_POST_RES_RESERVE = "/postResReserve";
	// 查询订单URL
	private static final String URL_API_FIND_ORDERS = "/findOrders";
	// 查询订单详细URL
	private static final String URL_API_ORDER = "/order";
	// 查询订单详细URL
	private static final String URL_API_CANCEL_ORDER = "/cancelOrder";
	// 关键字提示URL
	private static final String URL_API_GET_SUGGEST_KEYWORD_LIST = "/getSuggestKeywordList";
	// 上传照片URL
	private static final String URL_API_UPLOAD_IMAGE = "/uploadImage";
	// 提交评论URL
	private static final String URL_API_POST_COMMENT = "/postComment";
	// 提交绑定URL
	private static final String URL_API_BIND_SINA = "/bindSina";
	// 解除绑定URL
	private static final String URL_API_UNBIND_SINA = "/unbindSina";
	// 获得微博状态URL
	private static final String URL_API_WEBBO = "/webbo";
	// 报错URL
	private static final String URL_API_ERROR_LOG = "/errorLog";
	// 提交错误URL
	private static final String URL_API_FEED_BACK = "/feedback";
	// 真实经纬度->谷歌经纬度
	private static final String URL_API_GOOGLEMAP = "/googlemap";
	// 获得电话拨打记录
	private static final String URL_API_CALL = "/call";

	// TODO
	// 添加或更新餐厅
	private static final String URL_API_ADD_OR_UPDATE_REST = "/addOrUpdateRest";
	// 获得主菜系列表
	private static final String URL_API_GET_FOOD_MAIN_TYPE_LIST = "/getFoodMainTypeList";
	// 获得用户好友列表
	private static final String URL_API_GET_USER_FRIEND_LIST = "/getUserFriendList";
	// 添加一个好友
	private static final String URL_API_ADD_FRIEND = "/addFriend";
	// 发送短信
	private static final String URL_API_SEND_SMS = "/sendSms";
	// 获得城市列表
	private static final String URL_API_GET_CITY_LIST2 = "/getCityList2";
	// //获得错误报告类别列表
	// private static final String URL_API_GET_ERROR_REPORT_TYPE_LIST =
	// "/getErrorReportTypeList";
	// 获得错误报告类别列表，版本3.1.29，修改为新接口，2012.2.8
	private static final String URL_API_GET_ERROR_REPORT_TYPE_LIST = "/getErrorReportTypeList2";
	// 添加错误报告
	private static final String URL_API_ADD_ERROR_REPORT = "/addErrorReport";
	// 按小菜系搜索美食
	private static final String URL_API_SEARCH_SMALL_STYLE = "/searchSmallStyle";
	// 修改密码
	private static final String URL_API_CHANGE_USER_PWD = "/changeUserPwd";
	// 获得菜单列表
	private static final String URL_API_GET_DISH_LIST = "/getDishList";
	// 某个菜品的评论列表
	private static final String URL_API_GET_DISH_COMMENT_LIST = "/getDishCommentList";
	// 添加或更新菜单订单
	private static final String URL_API_ADD_OR_UPDATE_DISH_ORDER = "/addOrUpdateDishOrder";
	// 添加菜品评论
	private static final String URL_API_ADD_DISH_COMMENT = "/addDishComment";

	/* v3.1.29 */
	// 注册 (注册页填写完表单提交时调用)
	private static final String URL_API_USER_REG2 = "/userReg2";
	// 发送找回密码短信
	private static final String URL_API_SEND_PWD_SMS_BY_PHONE_NUM = "/sendPwdSmsByPhoneNum";
	// 上传图片新接口，版本3.1.29，修改为新接口，2012.2.9
	// v3.1.35修改，新增参数shareTo
	// private static final String URL_API_UPLOAD_IMAGE2 = "/uploadImage2";
	// 添加菜品数据
	private static final String URL_API_ADD_OR_UPDATE_FOOD = "/addOrUpdateFood";

	/* v3.1.30 */
	// 获得推送信息
	private static final String URL_API_GET_PUSH_MSG_LIST = "/getPushMsgList";
	// 获得用户积分列表
	private static final String URL_API_GET_USER_POINTS_LIST = "/getUserPointsList";
	// 获得用户现金券列表
	private static final String URL_API_GET_USER_CASH_COUPON_LIST = "/getUserCashCouponList";
	// 获得餐厅评论列表2，返回的评论信息带头像，图片等
	private static final String URL_API_GET_RES_COMMENT_LIST2 = "/getResCommentList2";
	// 获得餐厅详情，可带评论列表
	private static final String URL_API_GET_PAGE_RES_INFO_DATA2 = "/getPageResInfoData2";
	// 添加评论2，v3.1.35修改，增加参数shareTo
	// private static final String URL_API_POST_COMMENT2 = "/postComment2";
	// 获得优惠精选列表
	private static final String URL_API_GET_SPECIAL_REST_LIST = "/getSpecialRestList";
	// 拨打电话购买现金券时提交现金券数据
	private static final String URL_API_POST_CASH_COUPON_FOR_TEL = "/postCashCouponForTel";
	// 提交拨打电话记录
	private static final String URL_API_CALL2 = "/call2";
	// 获得订单列表
	private static final String URL_API_GET_ORDER_LIST2 = "/getOrderList2";
	// 获得订单信息
	private static final String URL_API_GET_ORDER_INFO = "/getOrderInfo";
	// 报告订单中错误的就餐金额
	private static final String URL_API_REPORT_ORDER_WRONG_PRICE = "/reportOrderWrongPrice";
	// 提交订单
	private static final String URL_API_POST_ORDER = "/postOrder";
	// 新浪用户登录
	private static final String URL_API_USER_SINA_LOGIN = "/userSinaLogin";
	// 验证是否绑定已经绑定过微博
	private static final String URL_API_VALID_IS_BIND_SINA = "/validIsBindSina";
	// 解绑以前的新浪微博，然后会再绑定新的用户
	private static final String URL_API_UNBIND_THEN_BIND = "/unbindThenBind";

	/* v3.1.31 */
	// 获得首页的信息
	private static final String URL_API_GET_MAIN_PAGE_INFO = "/getMainPageInfo";
	// 获得用户帐户收支列表
	private static final String URL_API_GET_USER_ACCOUNT_IN_OUT_LIST = "/getUserAccountInOutList";
	// 获得用户现金券购买列表
	private static final String URL_API_GET_USER_CASH_COUPON_BUY_LIST = "/getUserCashCouponBuyList";

	/* v3.1.32 */
	// 获得评论回复列表
	private static final String URL_API_GET_RES_COMMENT_REPLY_LIST = "/getResCommentReplyList";
	// 提交评论回复 v3.1.35更改，新增参数shareTo
	// private static final String URL_API_POST_COMMENT_REPLY=
	// "/postCommentReply";
	// 获得首页的信息v2
	private static final String URL_API_GET_MAIN_PAGE_INFO_2 = "/getMainPageInfo2";
	// 获得餐厅美食列表v2
	private static final String URL_API_GET_RES_FOOD_LIST2 = "/getResFoodList2";
	// 获得站内信列表
	private static final String URL_API_GET_USER_MSG_LIST = "/getUserMsgList";
	// 获得某一条站内信详细信息
	private static final String URL_API_GET_USER_MSG_INFO = "/getUserMsgInfo";
	// 发送一条站内信
	private static final String URL_API_SEND_USER_MSG = "/sendUserMsg";
	// 删除站内信
	private static final String URL_API_DEL_USER_MSG = "/delUserMsg";
	// 获得订单列表
	private static final String URL_API_GET_ORDER_LIST3 = "/getOrderList3";
	// 获得订单信息
	private static final String URL_API_GET_ORDER_INFO3 = "/getOrderInfo3";
	// 获得城市列表 v3
	private static final String URL_API_GET_CITY_LIST3 = "/getCityList3";
	// 通过gps获得城市id
	private static final String URL_API_GET_CITY_ID_BY_GPS = "/getCityIdByGps";
	// 用户登录 通过联通用户手机号
	private static final String URL_API_USER_LOGIN_BY_PHONE = "/userLoginByPhone";
	// 获得现金券订单支付数据,弃用
	private static final String URL_API_GET_CASH_COUPON_BILL_PAY_DATA = "/getCashCouponBillPayData";
	// 获得现金券订单支付数据2
	private static final String URL_API_GET_CASH_COUPON_BILL_PAY_DATA2 = "/getCashCouponBillPayData2";

	/* v3.1.33 */
	// 获得短信邀请信息
	private static final String URL_API_GET_INVITE_SMS_INFO = "/getInviteSmsInfo";
	// 发送短信2
	private static final String URL_API_SEND_SMS2 = "/sendSms2";
	// 添加评论评分
	private static final String URL_API_POST_COMMENT_SCORE = "/postCommentScore";
	// 删除用户订单记录
	private static final String URL_API_DEL_USER_ORDER = "/delUserOrder";
	// 删除用户所有订单
	private static final String URL_API_DEL_USER_ALL_ORDER = "/delUserAllOrder";

	/* v3.1.34 */
	// 获得外卖餐厅列表
	private static final String URL_API_GET_TAKEOUT_REST_LIST = "/getTakeoutRestList";
	// 获得外卖餐厅信息
	private static final String URL_API_GET_TAKEOUT_REST_INFO = "/getTakeoutRestInfo";
	// 获得首页的信息3
	private static final String URL_API_GET_MAIN_PAGE_INFO3 = "/getMainPageInfo3";
	// 获取餐馆或美食列表 2
	private static final String URL_API_GET_RES_AND_FOOD_LIST2 = "/getResAndFoodList2";
	// 获得现金券详情
	private static final String URL_API_GET_CASH_COUPON_DETAIL = "/getCashCouponDetail";

	/* v3.1.35 */
	// 带关键词高亮的搜索建议 使用 <b></b> 来标红 比如 上海<b>川</b>菜馆的<b>川</b>菜
	private static final String URL_API_GET_SUGGEST_KEYWORD_LIST2 = "/getSuggestKeywordList2";
	// 获得特惠套餐列表
	private static final String URL_API_GET_MEAL_COMBO_LIST = "/getMealComboList";
	// 获得特惠套餐信息
	private static final String URL_API_GET_MEAL_COMBO_INFO = "/getMealComboInfo";
	// 获得订单支付数据
	private static final String URL_API_GET_BILL_PAY_DATA = "/getBillPayData";
	// 获得餐厅菜品列表
	private static final String URL_API_GET_RES_FOOD_LIST3 = "/getResFoodList3";
	// 某个菜品的评论列表
	private static final String URL_API_GET_RES_FOOD_COMMENT_LIST = "/getResFoodCommentList";
	// 添加菜品评论
	private static final String URL_API_ADD_RES_FOOD_COMMENT = "/addResFoodComment";
	// 获得用户现金券,套餐列表
	private static final String URL_API_GET_USER_CASH_COUPON_LIST2 = "/getUserCashCouponList2";
	// 获得用户现金券购买列表,套餐列表
	private static final String URL_API_GET_USER_CASH_COUPON_BUY_LIST2 = "/getUserCashCouponBuyList2";
	// 同步用户信息
	private static final String URL_API_SYNC_USER_INFO = "/syncUserInfo";
	// 获得新浪绑定的url
	private static final String URL_API_GET_SINA_BIND_URLS = "/getSinaBindUrls";
	// 绑定到新浪
	private static final String URL_API_BIND_TO_SINA = "/bindToSina";
	// 通过新浪账户登录
	private static final String URL_API_USER_LOGIN_BY_SINA = "/userLoginBySina";
	// 获得新浪用户列表
	private static final String URL_API_GET_USER_SINA_FRIENDS_LIST = "/getUserSinaFriendsList";
	// 登录
	private static final String URL_API_USER_LOGIN2 = "/userLogin2";
	// 用户登录 通过联通用户手机号
	private static final String URL_API_USER_LOGIN_BY_PHONE2 = "/userLoginByPhone2";
	// 注册 (注册页填写完表单提交时调用)
	private static final String URL_API_USER_REG3 = "/userReg3";
	// 添加评论
	private static final String URL_API_POST_COMMENT2 = "/postComment2";
	// 上传图片
	private static final String URL_API_UPLOAD_IMAGE2 = "/uploadImage2";
	// 提交评论回复
	private static final String URL_API_POST_COMMENT_REPLY = "/postCommentReply";
	// 点击了语音按钮
	private static final String URL_API_CLICK_VOICE_INPUT = "/clickVoiceInput";
	// 分享餐馆信息到新浪微博等
	private static final String URL_API_SHARE_TO = "/shareTo";
	// 获得软件中的公共数据
	private static final String URL_API_GET_SOFTWARE_COMMONDATA = "/getSoftwareCommonData";

	/* v3.1.37 */
	// 添加菜品数据2
	private static final String URL_API_ADD_OR_UPDATE_FOOD2 = "/addOrUpdateFood2";
	// 获得推送信息2
	private static final String URL_API_GET_PUSH_MSG_LIST2 = "/getPushMsgList2";
	// 获得外卖餐厅列表 2
	private static final String URL_API_GET_TAKEOUT_REST_LIST2 = "/getTakeoutRestList2";
	// 获得外卖餐厅列表 3
	private static final String URL_API_GET_TAKEOUT_REST_LIST3 = "/getTakeoutRestList3";
	// 获得整理过的菜品列表
	private static final String URL_API_GET_SORTED_RES_FOOD_LIST = "/getSortedResFoodList";
	// 登出
	private static final String URL_API_LOGOUT = "/logout";

	/* v3.1.38 */
	// 获得上传图片数据
	private static final String URL_API_GET_UPLOAD_DATA = "/getUploadData";
	// 获得现金券列表 新接口
	private static final String URL_API_GET_SPECIAL_REST_LIST_2 = "/getSpecialRestList2";
	// 获得特惠套餐列表 新接口
	private static final String URL_API_GET_MEAL_COMBO_LIST_2 = "/getMealComboList2";
	// 获得图片列表
	private static final String URL_API_GET_RES_PIC_LIST2 = "/getResPicList2";
	// 获得菜品图片信息
	private static final String URL_API_GET_RES_FOOD_PIC_DATA = "/getResFoodPicData";
	// 获得特惠套餐信息2
	private static final String URL_API_GET_MEAL_COMBO_INFO2 = "/getMealComboInfo2";
	// 添加菜品喜欢类型
	private static final String URL_API_ADD_RES_FOOD_LIKE_TYPE = "/addResFoodLikeType";

	/* v3.1.39 */
	// 获得现金券或特惠套餐列表3
	private static final String URL_API_GET_MEAL_COMBO_LIST3 = "/getMealComboList3";
	// 获得订单支付数据2
	private static final String URL_API_GET_BILL_PAY_DATA2 = "/getBillPayData2";
	// 实时餐位查询
	private static final String URL_API_GET_REAL_TIME_TABLE_REST_LIST = "/getRealTimeTableRestList";
	// 获得餐馆页面数据3
	private static final String URL_API_GET_PAGE_RES_INFO_DATA3 = "/getPageResInfoData3";
	// 上传通讯录
	private static final String URL_API_UPLOAD_ADDRESS_BOOK = "/uploadAddressBook";
	// 添加评论评分2
	private static final String URL_API_POST_COMMENT_SCORE2 = "/postCommentScore2";
	// 添加是否喜欢餐厅
	private static final String URL_API_POST_REST_LIKE = "/postRestLike";
	// 获得餐厅房间状态 返回RoomSate
	private static final String URL_API_GET_REST_ROOM_STATE = "/getRestRoomState";
	// 后台拿到后 需要解压 然后记录 版本号，设备号，设备类型,页面名称,打开的时间,页面整体打开耗时,页面http查询耗时,页面查询url
	private static final String URL_API_UPLOAD_OPEN_PAGE_DATA = "/uploadOpenPageData";
	// 获得软件中的公共数据2
	private static final String URL_API_GET_SOFTWARE_COMMON_DATA2 = "/getSoftwareCommonData2";

	/* v3.1.40 */
	// 创建聊天室
	private static final String URL_API_CREATE_CHAT_ROOM = "/createChatRoom";
	// 关闭聊天室
	private static final String URL_API_CLOSE_CHAT_ROOM = "/closeChatRoom";
	// 获得聊天室聊天内容
	private static final String URL_API_GET_CHAT_MSG = "/getChatMsg";
	// 发送聊天信息
	private static final String URL_API_SEND_CHAT_MSG = "/sendChatMsg";
	// 发送聊天语音或者图片
	private static final String URL_API_SEND_CHAT_MSG_STREAM = "/sendChatMsgStream";
	// 检查是否有新的消息
	private static final String URL_API_CHK_HAVE_CHAT_MSG = "/chkHaveChatMsg";

	/* v3.1.41 */
	// 获得整理过的菜品列表
	private static final String URL_API_GET_FOOD_LIST_FOR_SELECT = "/getFoodListForSelect";
	// 获得用户的菜单列表
	private static final String URL_API_GET_USER_DISH_ORDER_LIST = "/getUserDishOrderList";
	// 获得菜单列表2
	private static final String URL_API_GET_DISH_LIST2 = "/getDishList2";
	// private static final String URL_API_GET_DISH_LIST2= "/getDishList"; //
	// for test
	// 新下订单页
	private static final String URL_API_GET_REST_ROOM_STATE_AND_ORDER_INFO = "/getRestRoomStateAndOrderInfo";
	// 新提交订单页
	private static final String URL_API_POST_ORDER_2 = "/postOrder2";

	/** /图吧url */
	// 获取驾车路线
	private static final String Url_MapBar_getDriveByLatLon = "route/getDriveByLatLon.jsp";
	// 根据关键字查询地标点名称列表
	private static final String Url_MapBar_getPoiNameByKeyword = "search/getPoiNameByKeyword.jsp";
	// 根据经纬度及城市代码查询大城市公交换乘
	private static final String Url_MapBar_getBusBigCity = "bus/getBusBigCity.jsp";

	/*****************************************************************************************
	 * 新接口从这里开始
	 *****************************************************************************************/

	private static final String GET_REST_SEARCH_SUGGEST_LIST = "/getRestSearchSuggestList";
	private static final String GET_USED_HISTORY_SUGGEST_LIST = "/getUsedHistorySuggestList";
	private static final String GET_MAIN_PAGE_INFO_PACK = "/getMainPageInfoPack";

	static {
		System.loadLibrary("zip");
		System.loadLibrary("chk");
	}

	private native static String get(String ass);

	/**
	 * 实例化
	 * 
	 * @param domain
	 * @param port
	 * @param clientVersion
	 */
	private A57HttpApiV3(String url) {
		mApiBaseUrl = url;
//		mApiBaseUrl = "http://mainapp.xiaomishu.com/mainapp";
		mApiBaseUrl = "http://tmainapp.xiaomishu.com/mainapp";
		mHttpApi = new HttpApiWithOAuth(Fg114Application.mHttpClient);
	}

	public static A57HttpApiV3 getInstance() {
		if (instance == null) {
			String sss = get(Settings.ASS_PATH);
			instance = new A57HttpApiV3("http://" + sss);
		}
		return instance;
	}

	public String getBaseParamsString() {
		if (mHttpApi == null || !(mHttpApi instanceof HttpApiWithOAuth)) {
			return "";
		}
		HttpApiWithOAuth httpApi = (HttpApiWithOAuth) mHttpApi;
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		httpApi.addBaseParams(paramList);
		String params = URLEncodedUtils.format(paramList, HTTP.UTF_8);
		return params;
	}

	/**
	 * 检查是否有新的版本 (在首页 软件刚打开时调用)
	 * 
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public JsonPack chkVersion(String version,// 软件版本号
			boolean isFirstOpen, String deviceType, // 设备类型
			String sellChannelNumber,// 渠道号
			String deviceNumber// 设备号
	) {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHK_VERSION), new BasicNameValuePair("version", version), new BasicNameValuePair("isFirstOpen", String.valueOf(isFirstOpen)),
					new BasicNameValuePair("deviceType", deviceType), new BasicNameValuePair("sellChannelNumber", sellChannelNumber), new BasicNameValuePair("deviceNumber", deviceNumber),
					new BasicNameValuePair("devInfo", ActivityUtil.getDevString(ContextUtil.getContext())),
					new BasicNameValuePair("isEmulator", String.valueOf(ActivityUtil.isEmulator(ContextUtil.getContext()))));
			if (DEBUG)
				Log.d(TAG, "chkVersion start");
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			if (DEBUG)
				Log.d(TAG, "chkVersion result:" + new Gson().toJson(jsonPack.getObj()));
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * 获得微博状态
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonPack webbo(String version,// 版本号
			String deviceNumber,// 设备号
			String token) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_WEBBO), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber), new BasicNameValuePair(
				"token", token));
		if (DEBUG)
			Log.d(TAG, "webbo start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "webbo result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	/**
	 * 用户提交错误
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonPack postFeedBack(String name,// 用户名
			String email,// 用户Email或手机
			String feedBack,// 错误msg
			String version,// 版本
			String deviceNumber// 手机设备号
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_FEED_BACK), new BasicNameValuePair("name", name), new BasicNameValuePair("email", email),
				new BasicNameValuePair("feedBack", feedBack), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber));
		if (DEBUG)
			Log.d(TAG, "postFeedBack start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "postFeedBack result:" + new Gson().toJson(jsonPack.getObj()));
		// Log.d(TAG, "postFeedBack result:" + new
		// Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	/**
	 * 错误提交
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonPack errorLog(String version,// 版本
			String deviceNumber,// 设备号
			String uuid,// 手机设备号
			String error,// 错误msg
			String description// 描述
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPostWithoutParams(fullUrl(URL_API_ERROR_LOG), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("uuid", uuid), new BasicNameValuePair("error", error), new BasicNameValuePair("description", description));
		if (DEBUG)
			Log.d(TAG, "errorLog start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "errorLog result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	public String fullUrl(String url, String... args) {
		String fullUrl = mApiBaseUrl + url;
		for (int i = 0; i < args.length; i++) {
			fullUrl = fullUrl.replace("{" + i + "}", args[i]);
		}
		return fullUrl;
	}

	/**
	 * 真实经纬度->谷歌经纬度
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonPack googlemap(String version,// 版本
			String deviceNumber,// 设备号
			double longitude, double latitude) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GOOGLEMAP), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber), new BasicNameValuePair(
				"longitude", String.valueOf(longitude)), new BasicNameValuePair("latitude", String.valueOf(latitude)));
		if (DEBUG)
			Log.d(TAG, "googlemap start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "googlemap result:" + new Gson().toJson(jsonPack.getObj()));
		// Log.d(TAG, "postFeedBack result:" + new
		// Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	/**
	 * 检查是否有新的版本 (在首页 软件刚打开时调用)
	 * 
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public JsonPack addOrUpdateRest(String version,// 软件版本号
			String deviceNumber,// 设备号
			String restEcName,// 设备号+时间戳
			int postTag,// 提交类型 1:餐厅添加 2:餐厅报错 3:添加外卖餐厅
			String restId,// 所属餐馆id add时可以为空 update时候为所属餐馆id
			String restName,// 商户名称
			String cityId,// 城市id
			String regionId,// 地域id 可以为空
			String districtId,// 商区id 可以为空
			String mainMenuId,// 主菜系类别ID 可以为空
			String restAddress,// 餐厅地址 可以为空
			String restTel,// 餐厅电话 可以为空
			String email, // 用户留的email
			String token// 用户id
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_ADD_OR_UPDATE_REST), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("restEcName", restEcName), new BasicNameValuePair("postTag", String.valueOf(postTag)), new BasicNameValuePair("restId", restId), new BasicNameValuePair(
						"restName", restName), new BasicNameValuePair("cityId", cityId), new BasicNameValuePair("regionId", regionId), new BasicNameValuePair("districtId", districtId),
				new BasicNameValuePair("mainMenuId", mainMenuId), new BasicNameValuePair("restAddress", restAddress), new BasicNameValuePair("restTel", restTel),
				new BasicNameValuePair("email", email), new BasicNameValuePair("token", token));
		if (DEBUG)
			Log.d(TAG, "addOrUpdateRest start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "addOrUpdateRest result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	/**
	 * 获得用户好友列表
	 * 
	 * @param version
	 * @param deviceNumber
	 * @param restEcName
	 * @param token
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	public JsonPack getUserFriendList(String version,// 软件版本号
			String deviceNumber,// 设备号
			String restEcName,// 设备号+时间戳
			String token,// 用户token
			long timestamp// 时间戳
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_USER_FRIEND_LIST), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("restEcName", restEcName), new BasicNameValuePair("token", token), new BasicNameValuePair("timestamp", String.valueOf(timestamp)));
		if (DEBUG)
			Log.d(TAG, "getUserFriendList start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getUserFriendList result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 添加一个好友
	public JsonPack addFriend(String version,// 软件版本号
			String deviceNumber,// 设备号
			String restEcName,// 设备号+时间戳
			String token,// 用户token
			String friendName,// 好友姓名
			String friendTel// 好友电话
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_ADD_FRIEND), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber), new BasicNameValuePair(
				"restEcName", restEcName), new BasicNameValuePair("token", token), new BasicNameValuePair("friendName", friendName), new BasicNameValuePair("friendTel", friendTel));
		if (DEBUG)
			Log.d(TAG, "addFriend start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "addFriend result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 添加错误报告
	public JsonPack addErrorReport(String version,// 软件版本号
			String deviceNumber,// 设备号
			String restEcName,// 设备号+时间戳
			int typeTag,// 大类标志 1：餐馆 2：菜系 3：外卖
			String typeId,// 错误类别ID
			String errorContent,// 错误内容 可以为空
			String email,// email //可以为空
			String uuid, // 餐馆或菜系ID，非空
			String token // 可以为空
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_ADD_ERROR_REPORT), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("restEcName", restEcName), new BasicNameValuePair("typeTag", String.valueOf(typeTag)), new BasicNameValuePair("typeId", typeId), new BasicNameValuePair(
						"errorContent", errorContent), new BasicNameValuePair("email", email), new BasicNameValuePair("uuid", uuid), new BasicNameValuePair("token", token));
		if (DEBUG)
			Log.d(TAG, "addErrorReport start:" + " version=" + version + " deviceNumber=" + deviceNumber + " restEcName=" + restEcName + " typeTag=" + typeTag + " typeId=" + typeId + " errorContent="
					+ errorContent + " email=" + email + " token=" + token);
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "addErrorReport result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 发送找回密码短信
	public JsonPack sendPwdSmsByPhoneNum(String version,// 软件版本号
			String deviceNumber,// 设备号
			String phoneNum// 手机号
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_SEND_PWD_SMS_BY_PHONE_NUM), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("phoneNum", phoneNum));
		if (DEBUG)
			Log.d(TAG, "sendPwdSmsByPhoneNum start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "sendPwdSmsByPhoneNum result:" + jsonPack.getObj().toString());
		return jsonPack;
	}

	// 报告订单中错误的就餐金额
	public JsonPack reportOrderWrongPrice(String version,// 软件版本号
			String deviceNumber,// 设备号
			String orderId,// 订单id
			String token,// 用户token
			double newPrice// 新的就餐金额
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_REPORT_ORDER_WRONG_PRICE), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("orderId", orderId), new BasicNameValuePair("token", token), new BasicNameValuePair("newPrice", String.valueOf(newPrice)));
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	// 获得订单列表
	public JsonPack getOrderList3(int typeTag,// 类别 1:普通订单 2：团宴订单 3:可以发送短信请柬的订单
			String token, // 用户token
			String statusId,// 状态id 默认为0或者空 0或者空为全部状态 其他为特定状态id
			int pageSize,// 页面大小
			int pageNo// 当前页
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_ORDER_LIST3), new BasicNameValuePair("typeTag", String.valueOf(typeTag)), new BasicNameValuePair("token", token),
				new BasicNameValuePair("statusId", statusId), new BasicNameValuePair("pageSize", String.valueOf(pageSize)), new BasicNameValuePair("pageNo", String.valueOf(pageNo)));
		if (DEBUG)
			Log.d(TAG, "getOrderList3 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getOrderList3 result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 通过gps获得城市id
	public JsonPack getCityIdByGps(double longitude,// 经度
			double latitude// 纬度
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_CITY_ID_BY_GPS), new BasicNameValuePair("longitude", String.valueOf(longitude)),
					new BasicNameValuePair("latitude", String.valueOf(latitude)));
			if (DEBUG)
				Log.d(TAG, "getCityIdByGps start");
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			if (DEBUG)
				Log.d(TAG, "getCityIdByGps result:" + jsonPack.getObj().toString());
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 获取餐馆或美食列表
	public JsonPack getResAndFoodList2(String cityId,// 所属城市ID
			boolean haveGpsTag,// 是否有gps
			double longitude,// 经度
			double latitude,// 纬度
			int distanceMeter,// 附近距离 约定：0：不是附近搜索 其他： 500米 ，1000米 ，2000米, 5000等
			String regionId,// 地域ID 约定 ""：全部地域 其他：所选地域ID
			String districtId,// 商区ID 约定"":全部商区 其他：所选商区ID
			String channelId,// 频道ID 1：餐馆 2：美食
			String mainMenuId,// 主菜系类别ID 约定 "":为全部主菜系 其他：所选菜系ID
			String subMenuId,// 子菜系类别ID 约定 "":为全部子菜系 其他：所选菜系ID
			String mainTopRestTypeId,// 主榜单类别 约定 "":为全部主榜单 其他：所选主榜单ID
			String subTopRestTypeId,// 子榜单类别 约定 "":为全部子榜单类别 其他：所选子榜单类别ID
			String keywords,// 搜索关键词
			int sortTypeTag,// 排序类别 约定:参看方法说明
			String avgTag, // 按人均筛选 0：所有 1...
			int pageSize,// 页面大小
			int pageNo,// 当前页
			long regionTimestamp,// 时间戳 所在城市的地域，商区数据时间戳
			long topRestTypeTimestamp,// 时间戳 所在城市的榜单类别数据时间戳
			long foodMenuTimestamp// 时间戳 所在城市的菜系类别数据时间戳
	) throws Exception {
		// long l1 = System.currentTimeMillis();
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_RES_AND_FOOD_LIST2), new BasicNameValuePair("cityId", cityId), new BasicNameValuePair("haveGpsTag", String.valueOf(haveGpsTag)),
				new BasicNameValuePair("longitude", String.valueOf(longitude)), new BasicNameValuePair("latitude", String.valueOf(latitude)),
				new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)), new BasicNameValuePair("regionId", regionId), new BasicNameValuePair("districtId", districtId),
				new BasicNameValuePair("channelId", channelId), new BasicNameValuePair("mainMenuId", mainMenuId), new BasicNameValuePair("subMenuId", subMenuId), new BasicNameValuePair(
						"mainTopRestTypeId", mainTopRestTypeId), new BasicNameValuePair("subTopRestTypeId", subTopRestTypeId), new BasicNameValuePair("keywords", keywords), new BasicNameValuePair(
						"sortTypeTag", String.valueOf(sortTypeTag)), new BasicNameValuePair("avgTag", avgTag), new BasicNameValuePair("regionTimestamp", String.valueOf(regionTimestamp)),
				new BasicNameValuePair("topRestTypeTimestamp", String.valueOf(topRestTypeTimestamp)), new BasicNameValuePair("foodMenuTimestamp", String.valueOf(foodMenuTimestamp)),
				new BasicNameValuePair("pageSize", String.valueOf(pageSize)), new BasicNameValuePair("pageNo", String.valueOf(pageNo)));
		if (DEBUG)
			Log.d(TAG, "getResAndFoodList2 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		// long l2 = System.currentTimeMillis();
		// Log.e(TAG, "getResAndFoodList2 time=" + (l2 - l1));
		if (DEBUG)
			Log.d(TAG, "getResAndFoodList2 result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 获得餐厅菜品列表
	public JsonPack getResFoodList3(String resId,// 餐馆ID
			String currentFoodId,// 约定 0：不需要取对象 其他：需要根据id获取该菜品对象 并放到list的第一位
			String keywords,// 关键词 可以为null
			String typeId,// 类别id 默认为0或者空 0或者空为全部状态 其他为特定类别id
			int pageSize,// 页面大小
			int pageNo// 当前页
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_RES_FOOD_LIST3), new BasicNameValuePair("resId", resId), new BasicNameValuePair("currentFoodId", currentFoodId),
				new BasicNameValuePair("keywords", keywords), new BasicNameValuePair("typeId", typeId), new BasicNameValuePair("pageSize", String.valueOf(pageSize)), new BasicNameValuePair("pageNo",
						String.valueOf(pageNo)));
		if (DEBUG)
			Log.d(TAG, "getResFoodList3 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getResFoodList3 result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 某个菜品的评论列表
	public JsonPack getResFoodCommentList(String foodId,// 菜品id
			int pageSize,// 页面大小
			int pageNo// 当前页
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_RES_FOOD_COMMENT_LIST), new BasicNameValuePair("foodId", foodId), new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
				new BasicNameValuePair("pageNo", String.valueOf(pageNo)));
		if (DEBUG)
			Log.d(TAG, "getResFoodCommentList start,");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getResFoodCommentList result:" + jsonPack.getObj().toString());
		return jsonPack;
	}

	// 添加菜品评论 v3.1.38增加新约定
	public JsonPack addResFoodComment(String foodId,// 菜品id
			String token,// 用户id
			int likeTypeTag,// 喜欢类型 1:喜欢 2:一般 3:不喜欢 默认传 1 v3.1.38：喜欢类型 0:忽略喜欢类型
							// 1:好吃 2:一般 3:不好吃
			String content,// 评论内容 v3.1.38：评论内容 如果内容为空 忽略评论，只添加喜欢类型
			String shareTo// 分享到微博 sina:1;qq:0 当前只有sina:1 或者 sina:0
							// 如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPost(new BasicNameValuePair("content", content), fullUrl(URL_API_ADD_RES_FOOD_COMMENT), new BasicNameValuePair("foodId", foodId),
				new BasicNameValuePair("token", token), new BasicNameValuePair("likeTypeTag", String.valueOf(likeTypeTag)), new BasicNameValuePair("shareTo", shareTo));
		if (DEBUG)
			Log.d(TAG, "addResFoodComment start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "addResFoodComment result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 同步用户信息//返回UserInfo2DTO
	public JsonPack syncUserInfo(String token// 用户token
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_SYNC_USER_INFO), new BasicNameValuePair("token", token));
			if (DEBUG)
				Log.d(TAG, "syncUserInfo start");
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			if (DEBUG)
				Log.d(TAG, "syncUserInfo result:" + new Gson().toJson(jsonPack.getObj()));
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 登录
	public JsonPack userLogin2(String cityId,// 所属城市ID
			String userName,// 用户名
			String userPwd,// 密码
			String clientType,// 客户端类型 约定参考顶部描述
			String ip, String sellChannelNumber// 渠道号
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_USER_LOGIN2), new BasicNameValuePair("cityId", cityId), new BasicNameValuePair("userName", userName), new BasicNameValuePair(
				"userPwd", userPwd), new BasicNameValuePair("clientTag", clientType), new BasicNameValuePair("ip", ip), new BasicNameValuePair("sellChannelNumber", sellChannelNumber));
		if (DEBUG)
			Log.d(TAG, "userLogin2 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "userLogin2 result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 注册 (注册页填写完表单提交时调用)
	public JsonPack userReg3(String sellChannelNumber,// 渠道号
			String cityId,// 所属城市ID
			String userName,// 手机号或email
			String userPwd,// 密码
			boolean regByPhoneNumTag// 是否是通过手机号注册
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_USER_REG3), new BasicNameValuePair("sellChannelNumber", sellChannelNumber), new BasicNameValuePair("cityId", cityId),
				new BasicNameValuePair("userName", userName), new BasicNameValuePair("userPwd", userPwd), new BasicNameValuePair("regByPhoneNumTag", String.valueOf(regByPhoneNumTag)));
		if (DEBUG)
			Log.d(TAG, "userReg3 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "userReg3 result:" + jsonPack.getObj().toString());
		return jsonPack;
	}

	// 添加评论
	public JsonPack postComment2(String token, // 用户token
			String restId,// 餐馆id
			int postTag,// 1：从点评页进去 2：从订单页进去 3：从随手拍进去 菜品 4：从随手拍进去 餐馆
			String foodId,// 菜品id 如果 postTag:3 foodId不能为空 postTag:其他 为空
			String orderId,// 订单id 如果 postTag:2 orderId不能为空 postTag:其他 为空
			String picId,// picId为uploadImage2返回的图片或者为空 picId可以是多个 111;222;333
			int overallNum,// 总体评价 0为未选择
			int tasteNum,// 口味 0为未选择
			int envNum,// 环境 0为未选择
			int serviceNum,// 服务 0为未选择
			String detail,// 评论内容 postTag=3时传递图片描述 可以为空 postTag=其他 不能为空
			String shareTo// 分享到微博 sina:1;qq:0 当前只有sina:1 或者 sina:0
							// 如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPost(new BasicNameValuePair("detail", detail), fullUrl(URL_API_POST_COMMENT2), new BasicNameValuePair("token", token), new BasicNameValuePair("restId",
				restId), new BasicNameValuePair("postTag", String.valueOf(postTag)), new BasicNameValuePair("foodId", foodId), new BasicNameValuePair("orderId", orderId), new BasicNameValuePair(
				"picId", picId), new BasicNameValuePair("overallNum", String.valueOf(overallNum)), new BasicNameValuePair("tasteNum", String.valueOf(tasteNum)), new BasicNameValuePair("envNum",
				String.valueOf(envNum)), new BasicNameValuePair("serviceNum", String.valueOf(serviceNum)), new BasicNameValuePair("shareTo", shareTo));
		if (DEBUG)
			Log.d(TAG, "postComment3 start:" + httpPost.getURI());
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "postComment3 result:" + (jsonPack.getObj() == null ? "null" : jsonPack.getObj().toString()));
		return jsonPack;
	}

	// 上传图片
	public JsonPack uploadImage2(int typeTag,// 类别 1:餐馆 2:菜品 3：点评页
			String restId,// 餐馆id
			String foodId,// 菜品id type=2 是菜品id 其他为空
			String memo,// 备注 可以为空 typeTag=1时 传递
			String token, // 用户token 可以为空 typeTag=1时 传递
			String shareTo,// 分享到微博 sina:1;qq:0 当前只有sina:1 或者 sina:0
							// 如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
			int likeTypeTag, // 喜欢类型 0:没有选择任何喜欢类型 1:好吃 2:一般 3:不好吃 当 typeTag：
								// 2时候有效
			InputStream pic) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			// 设置上传图片的超时为30秒
			int timeout = 30;
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
			HttpConnectionParams.setSoTimeout(params, timeout * 1000);
			client.setParams(params);

			HttpPost httpPost;
			JsonPack jsonPack;
			try {
				httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_UPLOAD_IMAGE2), pic, new BasicNameValuePair("typeTag", String.valueOf(typeTag)), new BasicNameValuePair("restId", restId),
						new BasicNameValuePair("foodId", foodId), new BasicNameValuePair("memo", memo), new BasicNameValuePair("token", token), new BasicNameValuePair("shareTo", shareTo),
						new BasicNameValuePair("likeTypeTag", String.valueOf(likeTypeTag)));
				if (DEBUG)
					Log.d(TAG + "[" + pic.available() + "]", "uploadImage3 start:" + httpPost.getURI());
				jsonPack = mHttpApi.doHttpRequest(client, httpPost);
				if (DEBUG)
					Log.d(TAG, "uploadImage3 result:" + (jsonPack.getObj() == null ? "null" : jsonPack.getObj().toString()));
				return jsonPack;
			} catch (Exception e) {
				throw e;
			}

		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 点击了语音按钮
	public JsonPack clickVoiceInput(String cityId, int voiceInputTag // 点击了哪个语音按钮
																		// 1：首页左上
																		// 2：首页左下
																		// 3：功能菜单中
																		// 4：搜索建议页
																		// 5:订餐厅页
																		// 6：热门商圈页
																		// 7：选择餐厅
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CLICK_VOICE_INPUT), new BasicNameValuePair("cityId", cityId),
					new BasicNameValuePair("voiceInputTag", String.valueOf(voiceInputTag)));
			if (DEBUG)
				Log.d(TAG, "clickVoiceInput start");
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			if (DEBUG)
				Log.d(TAG, "clickVoiceInput result:" + new Gson().toJson(jsonPack.getObj()));
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 获得整理过的菜品列表 SortedFoodListDTO
	public JsonPack getSortedResFoodList(String resId// 餐馆ID
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_SORTED_RES_FOOD_LIST), new BasicNameValuePair("resId", resId));
		if (DEBUG)
			Log.d(TAG, "getSortedResFoodList start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getSortedResFoodList result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 获得特惠套餐信息
	public JsonPack getMealComboInfo2(String mealComboId// 套餐id
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_MEAL_COMBO_INFO2), new BasicNameValuePair("mealComboId", mealComboId));
		if (DEBUG)
			Log.d(TAG, "getMealComboInfo2 start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getMealComboInfo2 result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 实时餐位查询 返回RealTimeTableRestListDTO
	public JsonPack getRealTimeTableRestList(String cityId,// 所属城市ID
			boolean haveGpsTag,// 是否有gps
			double longitude,// 经度
			double latitude,// 纬度
			int distanceMeter,// 附近距离 约定：0：不是附近搜索 其他： 500米 ，1000米 ，2000米, 5000等
			String regionId,// 地域ID 约定 ""：全部地域 其他：所选地域ID
			String districtId,// 商区ID 约定"":全部商区 其他：所选商区ID
			String mainMenuId,// 主菜系类别ID 约定 "":为全部主菜系 其他：所选菜系ID
			String subMenuId,// 子菜系类别ID 约定 "":为全部子菜系 其他：所选菜系ID
			long selectTime, // 所选择的时间
			int sortTypeTag,// 排序 0：默认
			String avgTag,// 人均 0：默认
			int pageSize,// 页面大小
			int pageNo,// 当前页
			long regionTimestamp,// 时间戳 所在城市的地域，商区数据时间戳
			long foodMenuTimestamp// 时间戳 所在城市的菜系类别数据时间戳
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_REAL_TIME_TABLE_REST_LIST), new BasicNameValuePair("cityId", cityId),
				new BasicNameValuePair("haveGpsTag", String.valueOf(haveGpsTag)), new BasicNameValuePair("longitude", String.valueOf(longitude)),
				new BasicNameValuePair("latitude", String.valueOf(latitude)), new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)), new BasicNameValuePair("regionId", regionId),
				new BasicNameValuePair("districtId", districtId), new BasicNameValuePair("mainMenuId", mainMenuId), new BasicNameValuePair("subMenuId", subMenuId), new BasicNameValuePair(
						"selectTime", String.valueOf(selectTime)), new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)), new BasicNameValuePair("avgTag", avgTag),
				new BasicNameValuePair("regionTimestamp", String.valueOf(regionTimestamp)), new BasicNameValuePair("foodMenuTimestamp", String.valueOf(foodMenuTimestamp)), new BasicNameValuePair(
						"pageSize", String.valueOf(pageSize)), new BasicNameValuePair("pageNo", String.valueOf(pageNo)));
		if (DEBUG)
			Log.d(TAG, "getRealTimeTableRestList start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		// long l2 = System.currentTimeMillis();
		// Log.e(TAG, "getResAndFoodList2 time=" + (l2 - l1));
		if (DEBUG)
			Log.d(TAG, "getRealTimeTableRestList result:" + new Gson().toJson(jsonPack.getObj()));
		return jsonPack;
	}

	// 上传通讯录
	public JsonPack uploadAddressBook(String token, // 用户token
			InputStream data) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();

			try {
				HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_UPLOAD_ADDRESS_BOOK), data, new BasicNameValuePair("token", token));
				if (DEBUG)
					Log.d(TAG + "[" + data.available() + "]", "uploadAddressBook start:" + httpPost.getURI());
				JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpPost);
				if (DEBUG)
					Log.d(TAG, "uploadAddressBook result:" + (jsonPack.getObj() == null ? "null" : jsonPack.getObj().toString()));
				return jsonPack;
			} catch (Exception e) {
				throw e;
			}

		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 后台拿到后 需要解压 然后记录 版本号，设备号，设备类型,页面名称,打开的时间,页面整体打开耗时,页面http查询耗时,页面查询url
	// 上传页面打开时间 3.2.13
	// 网络情况\tip\t页面名称\t打开的时间\t页面整体打开耗时\t页面http查询耗时\t页面查询url\n
	// 其中网络情况分别为 wifi,3g,2g
	// 其中页面名称分别为 搜索页:search 详细页:detail 实时餐位页:real
	// 打开的时间 为时间戳
	// 耗时为毫秒数
	//
	public JsonPack uploadOpenPageData(InputStream data) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();

			try {
				HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_UPLOAD_OPEN_PAGE_DATA), data);
				if (DEBUG)
					Log.d(TAG + "[" + data.available() + "]", "uploadOpenPageData start:" + httpPost.getURI());
				JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpPost);
				if (DEBUG)
					Log.d(TAG, "uploadOpenPageData result:" + (jsonPack.getObj() == null ? "null" : jsonPack.getObj().toString()));
				return jsonPack;
			} catch (Exception e) {
				throw e;
			}

		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 创建聊天室 返回ChatRoomCreateData
	public JsonPack createChatRoom(String token,// 用户token
			String lastMsgId, // 最后一条成功接收的系统消息的id
			long timestamp// 最后一条记录的时间戳
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CREATE_CHAT_ROOM), new BasicNameValuePair("token", token), new BasicNameValuePair("timestamp", String.valueOf(timestamp)));
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	// 关闭聊天室
	public JsonPack closeChatRoom(String token// 用户token
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CLOSE_CHAT_ROOM), new BasicNameValuePair("token", token));
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	// 获得聊天室聊天内容 返回ChatMsgListDto
	public JsonPack getChatMsg(String token,// 用户token
			String lastMsgId // 最后一条成功接收的系统消息的id
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_CHAT_MSG), new BasicNameValuePair("token", token), new BasicNameValuePair("lastMsgId", lastMsgId));
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}

	}

	// 发送聊天信息
	public JsonPack sendChatMsg(String token,// 用户token
			int dataTypeTag,
			// 数据类型
			// 1:简单文本 data=ChatMsgSendText
			// 2:选择时间 data=ChatMsgSendSelectTime
			// 3:选择餐馆 data=ChatMsgSendSelectRest
			String data // 对应的json字符串
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_SEND_CHAT_MSG), new BasicNameValuePair("token", token), new BasicNameValuePair("dataTypeTag", String.valueOf(dataTypeTag)),
				new BasicNameValuePair("data", data));
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	// 发送聊天语音
	public JsonPack sendChatMsgStream(int dataTypeTag,// 标志位 1:语音 2：图片
			String token,// 用户token
			InputStream data // 上传的数据
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			try {
				client = AbstractHttpApi.createHttpClient();
				// 设置上传图片的超时为30秒
				int timeout = 30;
				HttpParams params = client.getParams();
				HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
				HttpConnectionParams.setSoTimeout(params, timeout * 1000);
				client.setParams(params);

				HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_SEND_CHAT_MSG_STREAM), data, new BasicNameValuePair("dataTypeTag", String.valueOf(dataTypeTag)), new BasicNameValuePair(
						"token", token));
				JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpPost);
				return jsonPack;
			} catch (Exception e) {
				throw e;
			}
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {
					throw e;
				}
			}
		}
	}

	// 检查是否有新的消息 返回ChatMsgChkData
	public JsonPack chkHaveChatMsg(String token// 用户token
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHK_HAVE_CHAT_MSG), new BasicNameValuePair("token", token));
			JsonPack jsonPack = mHttpApi.doHttpRequest(client, httpGet);
			return jsonPack;
		} catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {

				}
			}
		}
	}

	// 获得餐厅房间状态和订单信息（用于下单页面） 3.1.41 ，返回RoomStateAndOrderInfo
	public JsonPack getRestRoomStateAndOrderInfo(String restId,// 餐馆id
			long selectTime// 所选择的时间
	) throws Exception {
		HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_GET_REST_ROOM_STATE_AND_ORDER_INFO), new BasicNameValuePair("restId", restId), new BasicNameValuePair("selectTime", selectTime + ""));
		if (DEBUG)
			Log.d(TAG, "getRestRoomStateAndOrderInfo start");
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpGet);
		if (DEBUG)
			Log.d(TAG, "getRestRoomStateAndOrderInfo result:" + jsonPack.getObj().toString());
		// LogUtils.log(jsonPack.getObj().toString());
		return jsonPack;
	}

	// 提交订单 3.1.41 如果成功的话 obj中返回一个对象 对象中包含一个字段uuid
	public JsonPack postOrder2(String version,// 软件版本号
			String deviceNumber,// 设备号
			String sellChannelNumber,// 渠道号
			int postTag,// 提交类型 1:添加 2:更新
			String orderId,// 订单id 如果postTag=1 为空 postTag=2为修改的订单的id
			String token,// 用户token
			String restId,// 餐馆ID
			String tel,// 手机号
			long reserveTime,// 预订时间
			int peopleNum,// 人数
			String bookerName,// 姓名
			int sexTag,// 性别 选填 默认为 1 约定 1：先生 0：女士
			int roomTypeTag,// 房间要求 0:只订大厅 1:只订包房 2:优先订大厅 3:优先订包房
			String memo,// 备注
			boolean selectApTag,// 是否选择了航空活动
			String apId,// 参加的航空活动id
			String apNumber// 输入的航空会员号
	) throws Exception {
		HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_POST_ORDER_2), new BasicNameValuePair("version", version), new BasicNameValuePair("deviceNumber", deviceNumber),
				new BasicNameValuePair("sellChannelNumber", sellChannelNumber), new BasicNameValuePair("postTag", String.valueOf(postTag)), new BasicNameValuePair("orderId", orderId),
				new BasicNameValuePair("token", token), new BasicNameValuePair("restId", restId), new BasicNameValuePair("tel", tel),
				new BasicNameValuePair("reserveTime", String.valueOf(reserveTime)), new BasicNameValuePair("peopleNum", String.valueOf(peopleNum)), new BasicNameValuePair("bookerName", bookerName),
				new BasicNameValuePair("sexTag", String.valueOf(sexTag)), new BasicNameValuePair("roomTypeTag", String.valueOf(roomTypeTag)), new BasicNameValuePair("memo", memo),
				new BasicNameValuePair("selectApTag", selectApTag + ""), new BasicNameValuePair("apId", apId), new BasicNameValuePair("apNumber", apNumber));
		if (DEBUG)
			Log.d(TAG, "postOrder start:" + httpPost);
		JsonPack jsonPack = mHttpApi.doHttpRequest(httpPost);
		if (DEBUG)
			Log.d(TAG, "postOrder result:" + (jsonPack.getObj() == null ? "null" : jsonPack.getObj().toString()));
		return jsonPack;
	}

	/***********************************************************************************************************************
	 * 
	 * 新版接口从这里开始
	 * 
	 ***********************************************************************************************************************/

}
