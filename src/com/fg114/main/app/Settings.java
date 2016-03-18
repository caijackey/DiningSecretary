package com.fg114.main.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.service.dto.VersionChkDTO;


/*****************************************
 * 系统常量
 * Author: zhangyifan 
 * Email: zhangyifan@95171.cn 
 * Date: 2011.4.12
 * */
public class Settings {
	//内部修正版本号，会显示在商圈最下面，也写入提交的错误日志中，这样可以区别同一个大版本里的不同修定版
	public static final String BuildVersion = "build-1";
	public static final boolean DEBUG = true;
	public static final StringBuffer requestLog=new StringBuffer(2048);

	/** 交互接口 */
	    //splashActivity 中 有根据后台改变APP_WAP_BASE_URL
//		public static String APP_WAP_BASE_URL = "http://m.57hao.com/appwap/";
		public static String APP_WAP_BASE_URL = "http://m.xiaomishu.com/appwap/";

	
    public static boolean isOpenPush=true;
	
	/*
	 * 控制是否显示蒙皮，需要显示新蒙皮时，修改此Key
	 * 3.1.35：isShowNewFeature1
	 * 3.1.38：isShowNewFeature2
	 * 3.1.39：isShowNewFeature3
	 * 3.1.40：isShowNewFeature4
	 * 3.1.41：isShowNewFeature5
	 */
	public static final String IS_SHOW_NEW_FEATURE = "isShowNewFeature5";
	
	public static final String[] TEST_ID = {
		"A0000044568D74", // 王鹰
		"357841033712916", // G7
//		"004999010640000", // 季毅华
		"A0000022605DEE", // Moto Droid 2
//		"357988022581115", // G3
		"860173011643669", // 谢四仁
		"358355021491390", // G4
		"351554053498342", // Galaxy Nexus
		"A000003343CEDA", // Huawei C8800
//		"356754049793690" // Samsung GT-S5820
		//"355653040325307" //夏普新测试机(白)
//		"352110051702513", // 李文杰的三星
		"353627055251774" // Galaxy Note 2
		};
	
	/** Asset的路径 */
	public static String ASS_PATH = "";
	/** 存放的DeviceID */
	public static volatile String DEV_ID = "";
	public static String VERSION_NAME = "";
	// 存放当前的Context
	public static String CURRENT_PAGE = "";
	// 版本检查结果全局变量
	public static VersionChkDTO gVersionChkDTO;
	// 百度地图是否可用
	public static boolean gBaiduAvailable;
	//系统屏幕分辨率属性
	public static DisplayMetrics DISPLAY;
	// 点菜订单的超时时间
	public static final long DISH_EXPIRED_TIME = 1000 * 3600 * 4; // 订单超时时间
	
	/** 图吧接口 */
	public static final String MAPBAR_URL = "http://mapbar.xiaomishu.com/";
	
	/** 找回密码接口 */
	public static final String FINDPASS_URL = "http://www.xiaomishu.com/io/fgwap/findpass.aspx";
	
	/** 支付的Host */
	public static final String PAYMENT_HOST = "http://w.xiaomishu.com";
//	public static final String PAYMENT_HOST = "http://192.168.3.65:8000";
//	public static final String PAYMENT_HOST = "http://w.57hao.com";
	/** 支付Web页 */
	public static final String kXmsHostKey = "xiaomishu.com";
	public static final String kBackKey = "/back";
	public static final String kBuySuccKey = "/buysuccess";
	public static final String kStartKey = "cashPay";
	
	// 银联支付约定url
	public static final String TAG_UNIONPAY = "/pay/unionpay";
	
	/** 讯飞Params*/
	public static String XF_Params = "appid=4e96c353";
	/** 讯飞EngineName*/
	public static String XF_ENGINE_NAME = "sms";

	public static int STATUS_BAR_HEIGHT;
	//intent的自定义action
	public static final String INTENT_EXTRA_CREATE_ROOM_RESULT = "createRoomResult";
	public static final String INTENT_EXTRA_NEW_MESSAGES = "newMessages";
	public static final String INTENT_EXTRA_NEW_DATA = "newData";
	public static final String INTENT_EXTRA_GALLERY_IS_ACTIVE = "isActive";
	
	/** 密钥 */
//	public static final String KK = "4PwMCFSL";
	public static final String KK = "6AA89CD986019BC62B81700CD8346906";
	/** 存放加密信息的HTTP头 */
//	public static final String REST_EC_NAME = "restEcName";
	public static final String REST_EC_NAME = "Restecname";
	
	// 联通取号相关
	public static final String UNICOM_SP_ID = "http://www.xiaomishu.com";
	public static final String UNICOM_SP_PWD = "njaction";
//	public static final String UNICOM_NQS_URL = "http://www.ishwap.com/net/";
	public static final String UNICOM_NQS_URL = "http://www.ishwap.com/net/default.aspx";
	
	public static final String DEFAULT_HEAD_PIC = "http://upload1.95171.cn/small/noface.gif";
	
	/** 版本信息 */
	public static String SELL_CHANNEL_NUM = "";				//渠道号 {渠道号}
	public static long TIME_DIFF = 0;				//与服务器之间时间差
	public static int isWifi=0;//是否是WIFI 1： true  0：false
	public static final String CLIENT_TYPE = "android";						//客户端类型
	public static final String UPDATE_SAVENAME = "DiningSec_newVer.apk";	//新版本保存名

	/** 系统常量 */
	public static final String DEFAULT_CITY_ID = "200000";					//默认城市ID
	public static final String DEFAULT_CITY_NAME = "上海";					//默认城市
	public static final String DEFAULT_CITY_PHONE_SH = "57575777";			//默认城市电话号码(上海)
	public static final String DEFAULT_CITY_PHONE_OTHER = "10107777";		//其他城市电话号码
	public static final int DEFAULT_RES_AND_FOOD_PAGE_SIZE = 20;			//默认页大小
	public static final int DEFAULT_RES_FOOD_PAGE_SIZE = 25;				//默认菜单页页大小
	public static final int DEFAULT_PIC_PAGE_SIZE = 18;						//默认图片页大小
	public static final int DEFAULT_COMMENT_PAGE_SIZE = 25;					//默认评论页大小
	public static final int DEFAULT_RES_COMMENT_PAGE_SIZE = 3;				//餐厅页评论显示数
	public static final long EXPIRE_TIME= 1000 * 60 * 60 * 24;				//信息的超时时间
	public static final String MY_LOC_INFO="myLocInfo";                     //当前位置缓存KEY
	public static final String ROCK_BTN_KEY="ROCK_BTN_KEY";
	/** 系统约定值 */
	public static final int CONTRL_ITEM_ID = -1;							//列表中的非数据项ID
	public static final int CONTRL_ITEM_ON_ID = -2;							//列表中的非数据项_用来显示更多
	public static final int CONTRL_ITEM_OFF_ID = -3;						//列表中的非数据项_用来隐藏更多
	public static final int CONTRL_ITEM_HISTORY_ID = -4;					//表示是历史关键字
	public static final int STATUTE_ALL = 0;								//全部
	
	/** 餐厅和菜品列表的类型 */
	public static final int RES_AND_FOOD_LIST_TYPE_SEARCH = 0; // 搜索
	public static final int RES_AND_FOOD_LIST_TYPE_NEARBY = 1; // 附近
	public static final int RES_AND_FOOD_LIST_TYPE_TOP = 2; // 榜单
	
	
	//餐厅图片查看类别
	public static final int STATUTE_IMAGE_EVN = 1;							//查看类别_环境
	public static final int STATUTE_IMAGE_FOOD = 2;							//查看类别_菜式
	public static final int STATUTE_IMAGE_UPLOAD = 3;						//查看类别_会员上传
	public static final int STATUTE_IMAGE_OTHER = 4;						//查看类别_其他
	
	//频道区分
	public static final String STATUTE_CHANNEL_RESTAURANT = "1";			//频道区分_餐馆
	public static final String STATUTE_CHANNEL_FOOD = "2";					//频道区分_美食
	public static final String STATUTE_CHANNEL_TAKEAWAY="3";                //频道区分_外卖
	//座位要求区分
	public static final int ROOM_TYPE_TAG_HALL = 0;							//只订大厅
	public static final int ROOM_TYPE_TAG_HALL_THEN_ROOM = 2;				//优先订大厅，包房也可以
	public static final int ROOM_TYPE_TAG_ROOM_THEN_HALL = 3;				//优先订包房，大厅也可以
	public static final int ROOM_TYPE_TAG_ROOM = 1;	                        //只订包房   
	
	//实时餐位情况
	public static final int REAL_TIME_ENOUGH = 1; // 有
	public static final int REAL_TIME_FEW = 2; // 紧张
	public static final int REAL_TIME_FULL = 3;	// 已满
	public static final int REAL_TIME_DISABLE = 4; // 不可预定   
	 
	//区分优惠套餐和现金卷标志位
	public static final int BUNDLE_DISCOUNT_PACKAGE_TAG=2;                  //优惠套餐
	public static final int BUNDLE_CASH_LIST_TAG=1;                        //现金卷
	public static final String  DISCOUNT_CASH_TYPE="discountCashType";   //优惠套餐和现金卷标志位
	//分享微博标志位
	public static final int BUNDLE_WEIBO_REST=1;                        //分享餐厅
	public static final int BUNDLE_WEIBO_SOFT_WARE=2;                   //分享软件
	/** 文件目录管理 */
	public static final String CONFIG_FILE = "Config57";					//配置信息句柄
	public static final String IMAGE_CACHE_DIRECTORY = "DiningSecretaryV3";	//图片暂存目录句柄
	public static final String VOICE_CACHE="Voice_Cache";                  //录音缓存目录
	//分享微博标志位
	public static final int IMAGE_FOOD_STYLE =0;                        //餐厅图片界面 菜式图片区
	public static final int IMAGE_OTHER_REST=1;                         //餐厅图片界面  其他图片区
	//微信成功分享接口参数
	public static int wxTypeTag=1;//微信类别 1:微信  2：朋友圈
	public static int shareTypeTag=0;// 1:分享餐厅 2：软件分享 3：推荐分享 4:外卖分享 5:wap分享6:秘币分享7:100:其他分享
	public static String shareUuid="";//同shareTo的uuid
	/** 配置文件的内容key */
	public static final String IS_FRIST = "isFirst";							//是否是第一次使用
	public static final String IS_FRIST_SHOW_HINT_BUTTON = "IS_FRIST_SHOW_HINT_BUTTON";	//是否是第一次使用，首页精选按钮
	public static final String IS_FRIST_WITH_NET = "isFirstWihtNet";			//是否是第一次有网络的使用
	public static final String IS_FRIST_REST_DETAIL = "isFirstRestDetail";			//是否是第一次打开详情
	public static final String CITY_LIST_KEY = "cityList";						//城市列表
	public static final String CITY_KEY = "city";								//所选城市
	public static final String MAIN_MENU_LIST_KEY = "mainMenuList";				//主页九宫格项目列表
	public static final String POP_TIME_STAMP = "popTimeStamp";					//pop窗口时间戳
	public static final String DIALOG_TIME_STAMP = "dialogTimeStamp";			//dialog窗口时间戳
//	public static final String RECOMMEND_LIST_KEY = "RecommendList";			//主页推荐餐厅列表
	public static final String TOP_LIST_KEY = "topList";						//榜单列表
	public static final String BUNDLE_REST_TYPEID ="typeId";                    //榜单子列表ID
	public static final String HOT_DISTRICT_KEY = "hotDistrictList";			//热门商区列表
	public static final String REGION_KEY = "regionList";						//地域列表
	public static final String DISTRICT_KEY = "districtList";					//商区列表
	public static final String CHANNEL_KEY = "channelList";						//频道列表
	public static final String HISTORY_LIST_KEY = "historyList";				//浏览历史列表
	public static final String RESTAURANT_KEY = "restaurant";					//餐厅详细
	public static final String SEARCH_RESULTS = "searchResults";					//搜索结果缓存key
	public static final String TAKE_AWAY_REST_LIST_RESULTS = "TAKE_AWAY_REST_LIST_RESULTS";	//外卖餐厅列表缓存key
	public static final String TAKE_AWAY_REST_MENU_RESULTS = "TAKE_AWAY_REST_MENU_RESULTS";	//外卖餐厅菜单缓存key
	public static final String TAKE_AWAY_MOVE_MAP_RESULTS  = "TAKE_AWAY_MOVE_MAP_RESULTS" ; //外卖地图页面缓存KEY
	public static final String IS_FRIST_USE_SUPER57 = "isFirstUseSuper57";		//第一次使用超级小秘书功能
	public static final String HAS_CHECKED_BIND = "hasCheckedBind";				//是否已检查过绑定状态
	public static final String BINDED_PHONE_NO = "bindedPhoneNo";				//超级57用户号码
	public static final String SUPER57_HISTORY_KEY = "super57HistoryList";		//超级57历史列表
	public static final String LOGIN_USER_INFO_KEY = "loginUserInfo";			//登录用户信息
	public static final String IS_LOGIN_KEY = "userIsLogin";					//登录用户标识
	public static final String SEARCH_HISTORY_LIST_KEY = "searchHistoryList";	//查询历史列表
	public static final String SEARCH_POI_HISTORY_LIST_KEY = "searchPoiHistoryList";	//POI查询历史列表
	public static final String REST_SEARCH_SUGGEST_HISTORY_LIST_KEY = "REST_SEARCH_SUGGEST_HISTORY_LIST_KEY";	//查询历史列表（餐厅搜索）
	public static final String ERROR_REPORT_TYPE_LIST_PACK_KEY="errorReportTypeListPack";//错误报告类型，用于缓存键值
	public static final String SMS_INVITE_RECENT_PERSON_KEY="smsInvitationRecentPersons";//短信邀请模块，最近联系人，用于缓存键值
//	public static final String IS_SHARE_COMMENT_BY_SINA_KEY = "isShareCommentBySina";//是否通过新浪微博分享评论
	public static final String IS_SUGGEST_DESKTOP_LINK = "isSuggestDesktopLink";//是否提示快捷方式
	public static final String IS_HAS_DESKTOP_LINK = "isHasDesktopLink";		//是否已有快捷方式
	public static final String IS_AUTO_SHOW_UPDATE_DIALOG = "isAutoShowUpdateDailog";//是否显示更新提示
	public static final String UPDATE_VERSION = "updateVersion";				//上次版本更新版本号
	public static final String LOCAL_VERSION = "localVersion";					//本地系统版本号，如与当前版本不同，可能需要做一些重置处理
	public static final String ANONYMOUS_TEL = "anonymousTel";					//匿名预订时缓存的手机号
	public static final String ANONYMOUS_TRUE_NAME = "anonymousTrueName";		//匿名预订时缓存的姓名
//	public static final String CITY_LIST3 = "cityList3";						//缓存的城市信息
	public static final String CITY_LIST = "cityList4";						//缓存的城市信息v3.1.34
	public static final String GPS_CITY = "gpsCity";							//由GPS定位的城市
	public static final String FOOD_MAIN_TYPE = "foodMainType";					//主菜系
	public static final String UUID = "UUID";									//UUID
	public static final String USER_FRIEND_LIST = "UserFriendList";				//用户的好友列表
	public static final String DISH_ORDER = "dishOrder";						//点菜的订单
	public static final String DISH_LIST = "dishList";							//点菜的菜单
	public static final String IS_SCAN_SUCCESS = "isScanSuccess";				//扫描成功
	public static final String KEY_CHANNEL_NUM = "keyChannelNum";				//缓存渠道号的Key
	public static final String KEY_SHOW_REST_PICTURE = "keyShowRestPicture";	//列表中是否显示餐厅图片
	public static final String KEY_LOGIN_USERNAME = "keyLoginUserName";	//用户登录用户名
	public static final String KEY_LAST_UPDATE_CITY_TIME = "lastUpdateCityTime";	//上次更新城市数据的时间
	public static final String KEY_LAST_UPDATE_ERROR_REPORT_TYPE_TIME = "lastUpdateErrorReportTypeTime";	//上次更新错误类型数据的时间
	public static final String KEY_VOICE_BTN_MOVE_POINT="voiceBtnMovePoint";                         //语音按钮上次移动坐标记录
	public static final String KEY_TAKEPIC_BTN_MOVE_POINT="takePicBtnMovePoint";                     //随手拍按钮上次移动坐标记录
	public static final String KEY_ORDER_AND_MESSAGE_MUN="orderAndMsgNum";                           //订单和站内信数量
	public static final String KEY_RES_AND_FOOD_LIST2_DTO="resAndFoodList2DTO";                      //缓存的餐厅菜品基本信息Key
	public static final String KEY_NEAR_SEARCH_ADV="nearSearchAdv";  //附近搜索时推送的广告id，多个id以逗号形式分隔存储为一个字符串
	public static final String STATUTE_CHANNEL_NEARBY="nearBy";//附近搜索
	public static final String STATUTE_CHANNEL_ALL_REGION="all_Region";//全部地域
	public static final String KEY_BOOK_PHONE="bookPhone";  //缓存的预订手机Key
	public static final String KEY_CONTR_COUNT="contrCount";  //缓存的联系人数目
	public static final String KEY_ALLOW_UPLOAD_CONTACT="allowUploadContact";  //是否允许上传联系人
	public static final String KEY_SH_ALL_REGION="shAllRegion";  //上海全部区域缓存
	public static final String KEY_CHAT_MSG_LIST="chatMsgList";  //语音订餐的本地缓存
	public static final String KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID="chatMsgLastSystemMessageId";  //成功获取的最后一条系统消息的
	public static final String KEY_CHAT_CREATE_CHAT_ROOM_TIMESTAMP="createChatRoomTimestamp";  // 创建房间时获取系统消息的时间戳
	public static final String KEY_SELECTED_FOOD="selected_food";  // 用户选择的菜单
	public static final String KEY_REQUEST_UPLOAD_CONTACT="requestUploadContact";  // 询问是否允许上传联系人的次数
	
	/** Bundle对象传值用 */
	public static final String FROM_TAG = "fromTag";							//来自哪里
	public final static String BUNDLE_KEY_ID = "keyId"; 					//ID
	public final static String BUNDLE_REST_TYPE_ID = "topRestTypeId"; 					//topRestTypeId  榜单ID
	public final static String BUNDLE_KEY_NEED_JUMP_TO_NAV_MAP = "BUNDLE_KEY_NEED_JUMP_TO_NAV_MAP"; 
	public final static String BUNDLE_KEY_WEB_URL = "BUNDLE_KEY_WEB_URL"; 			//要加载的url
	public final static String BUNDLE_KEY_WEB_TITLE = "BUNDLE_KEY_WEB_TITLE"; 			//要加载的页面显示的title
	public final static String BUNDLE_KEY_WEB_HIDE_TITLE = "BUNDLE_KEY_WEB_HIDE_TITLE"; //跳到web页后是否要隐藏应用本身的title
	public final static String BUNDLE_KEY_FROM_PAGE_2 = "fromPage2"; 			//来自画面，第２
	public final static String BUNDLE_KEY_TITLE = "title"; 					//标题
	public final static String BUNDLE_KEY_TEL = "tel"; 					//手机号码
	public final static String BUNDLE_KEY_LEFT_BUTTON = "leftGoBackBtn"; 	//标题栏左边按钮
	public final static String BUNDLE_KEY_CONTENT = "content"; 				//内容
	public final static String BUNDLE_NEW_VERSION  = "newVersion"; 				//内容
	public final static String BUNDLE_APK_NAME  = "apkName"; 				//内容
	public final static String BUNDLE_KEY_MUST_GPS = "mustGps";				//是否必须使用定位功能
	public final static String BUNDLE_KEY_VERSION_CHK_DTO = "VersionChkDTO";//版本检查DTO
	public final static String BUNDLE_KEY_KEYWORD = "Keyword";				//搜索关键字
	public final static String BUNDLE_LOGIN_SUCCESS_LISTENER = "LoginSuccessListener";	//登录成功后的处理
	public final static String BUNDLE_FINDPASS_NAME = "FindpassName";		//跳转至找回密码页面时传递的用户名
	public static final String BUNDLE_CONTACT_DATA = "contactData";			//发送短信时，从联系人列表选择联系人后返回的联系人信息
	public static final String BUNDLE_ORDER_ID = "orderId";					//发送短信时的订单ID
	public static final String BUNDLE_ORDER_DETAIL = "orderDetail";			//编辑订单时传输订单数据
	public static final String BUNDLE_SMS_DETAIL = "smsDetail";				// 发送短信时的模板
	public static final String BUNDLE_SMALL_STYLE_ID = "smallStyleId";		// 小菜系ID
	public static final String BUNDLE_SMALL_STYLE_NAME = "smallStyleName";	// 小菜系名称
	public static final String BUNDLE_KEY_DISHORDERDTO = "DishOrderDTO";	// 得到点菜信息
	public static final String BUNDLE_REST_ID = "restId";					// 餐厅ID
	public static final String BUNDLE_Page_REST = "PageRestInfoDTO";					// 餐厅PageRestInfoDTO
	public static final String BUNDLE_menuSelPack = "menuSelPack";					// menuSelPack json 字符串
	public static final String BUNDLE_typeId = "typeId";					// typeId
	public static final String BUNDLE_showTypeTag = "showTypeTag";					// showTypeTag
	public static final String BUNDLE_forAddTag = "forAddTag";            //是否是添加订单
	public static final String BUNDLE_pageRestInfoDTO = "pageRestInfoDTO";            //餐厅详情
	public static final String BUNDLE_issameCity = "BUNDLE_issameCity";            //是否是相同城市
	
	public static final String BUNDLE_COUPON_ID = "couponId";					// 优惠券id
	public static final String BUNDLE_REST_NAME = "restName";					// 餐厅name
	public static final String BUNDLE_REST_IMAGE_URL = "BUNDLE_REST_IMAGE_URL";					// 餐厅图片url
	public static final String BUNDLE_REST_LINK_URL = "BUNDLE_REST_LINK_URL";					// 餐厅链接地址
	public static final String BUNDLE_ORDER_TIME = "BUNDLE_ORDER_TIME";					// 预订时间
	public static final String BUNDLE_ORDER_PEOPLE_NUM = "BUNDLE_ORDER_PEOPLE_NUM";		// 预订人数
	public static final String BUNDLE_ORDER_ROOM_TYPE = "BUNDLE_ORDER_ROOM_TYPE";		// 预订包房类型
	public static final String BUNDLE_BOOK_ORDER_TAG = "BUNDLE_BOOK_ORDER_TAG";		// 订单表单：1修改表单  2新预定
	public static final String BUNDLE_REST_URL = "restUrl";					// 餐厅url
	public static final String BUNDLE_REST_COMMENT_ID = "restCommentId";	// 餐厅评论id
	public static final String BUNDLE_REST_COMMENT_DATA = "restCommentData";	// 餐厅评论数据
	public static final String BUNDLE_TABLE_ID = "tableId";					// 桌号
	public static final String BUNDLE_TPYE_TAG = "typeTag";					// 餐厅详情样式
	public static final String BUNDLE_Auth_Num = "authNum";                 //授权号
	public static final String BUNDLE_Card_Num = "cardNum";                 //卡号
	public static final String BUNDLE_FOOD_INFO = "foodInfo";				// 菜品详细信息
	public static final String BUNDLE_FOOD_ID = "foodId";				// 菜品id
	public static final String BUNDLE_FOOD_NAME = "foodName";				// 菜品名称
	public static final String BUNDLE_DISH_TAG = "dishTag";					// 菜品评价
	public static final String BUNDLE_DISH_COMMENT = "dishComment";			// 菜品评价信息
	public static final String BUNDLE_FOODANDRES_COMMENT = "foodandrescomment";			// 菜品评价信息
	public static final String BUNDLE_DISH_SRC_PAGE = "dishSrcPage";		// 点菜模块的来源页面
	public static final String BUNDLE_BAIDU_MODE = "baiduMode";				// 百度地图的显示模式
	public static final String BUNDLE_CURRENT_FOOD_ID = "currentFoodId";	// 跳转至菜品页面时，当前选择的菜品Id
	public static final String BUNDLE_CURRENT_FOOD_NAME = "currentFoodName";	// 跳转至菜品页面时，当前选择的菜品名称
	public static final String BUNDLE_RES_AND_FOOD_LIST_TYPE = "resAndFoodListType"; // 餐厅和菜品列表的类型 0：搜索 1:附近
	public static final String BUNDLE_REST_LONGITUDE = "restLongitude";					// 餐厅经度
	public static final String BUNDLE_REST_LATITUDE = "restLatitude";					// 餐厅纬度
	//public static final String BUNDLE_HAS_PIC = "hasPic";					// 从非首页的页面点击随手拍，如选择了图片，则此key为true
	public static final String BUNDLE_DEFAULT_BOOK_TIME = "defaultBookTime"; // 默认预订时间 类型为Calendar
	public static final String BUNDLE_DEFAULT_ROOM_TYPE = "defaultRoomType"; // 默认预订餐位类型
	public static final String BUNDLE_FUNC_NAME = "funcName"; // 需要使用的功能名称(使用附近相关的功能时，需要先检查gps是否在当前城市)
	public static String BUNDLE_UPDATE_URL = "updateUrl";
	public static String BUNDLE_UPDATE_SAVE_NAME = "updateSaveName";
	public static String BUNDLE_UPDATE_APP_NAME = "updateAppName";
	public static String BUNDLE_SELECTED_IDS = "selectedIds";
	public static String BUNDLE_Activity_ID = "activityId";
	public static String BUNDLE_Activity_Detail = "activityDetail";
	
	//
	public static final String BUNDLE_UPLOAD_RESTAURANT_NAME = "uploadRestaurantName";	// 上传图片页面，餐厅名称
	public static final String BUNDLE_UPLOAD_RESTAURANT_ID = "uploadRestaurantId";	// 上传图片页面，餐厅id
	public static final String BUNDLE_UPLOAD_FOOD_NAME = "uploadFoodName";	// 上传图片页面，菜品名称
	public static final String BUNDLE_UPLOAD_FOOD_ID = "uploadFoodId";		// 上传图片页面，菜品id
	public static final String BUNDLE_UPLOAD_TYPE = "uploadType";		// 上传图片页面，标志上传图片的类型，取值本类中以"UPLOAD_TYPE_"开头的枚举键
	public static final String BUNDLE_UPLOAD_PRICE = "uploadPrice";		// 上传图片页面，价格
	public static final String BUNDLE_UPLOAD_UNIT = "uploadUnit";		// 上传图片页面，价格单位
	public static final String BUNDLE_UPLOAD_COMMENT = "uploadComment";		// 上传图片页面，评论内容
	public static final String BUNDLE_UPLOAD_SHARE_TO = "uploadShareTo";		// 上传图片页面，分享到字符串 
	public static final String BUNDLE_UPLOAD_PICTURE_UUID = "uploadPictureUUID";		// 上传图片后的图片UUID

	public static final String BUNDLE_UPLOAD_FOOD_GRADE = "BUNDLE_UPLOAD_FOOD_GRADE";		// 上传图片菜品评分
	public static final String BUNDLE_UPLOAD_RESTAURANT_GRADE_TASTE = "BUNDLE_UPLOAD_RESTAURANT_GRADE_TASTE";	// 上传图片餐厅评分：口味
	public static final String BUNDLE_UPLOAD_RESTAURANT_GRADE_ENVIRONMENT = "BUNDLE_UPLOAD_RESTAURANT_GRADE_ENVIRONMENT";	// 上传图片餐厅评分：环境
	public static final String BUNDLE_UPLOAD_RESTAURANT_GRADE_SERVICE = "BUNDLE_UPLOAD_RESTAURANT_GRADE_SERVICE";	// 上传图片餐厅评分：服务
	public static final String BUNDLE_USER_MESSAGE_DATA = "BUNDLE_USER_MESSAGE_DATA";		// 传输用户站内信数据
	public static final String BUNDLE_USER_MESSAGE_TYPE_TAG = "BUNDLE_USER_MESSAGE_TYPE_TAG";		// 传输用户站内信数据的类型id
	public static final String BUNDLE_OBJECT_DATA = "BUNDLE_OBJECT_DATA";		// 通用对象传输
	public static final String BUNDLE_USER_MESSAGE_READ_TAG="userMessageReadTag"; //站内信是否以读标志
	public static final String BUNDLE_SMS_HavePlaceGpsTag = "BUNDLE_SMS_HavePlaceGpsTag";		//发送短信邀请页面接受的参数，是否有参照地标gps
	public static final String BUNDLE_SMS_PlaceLon = "BUNDLE_SMS_PlaceLon";		// 发送短信邀请页面接受的参数，地标经度，可以为空
	public static final String BUNDLE_SMS_PlaceLat = "BUNDLE_SMS_PlaceLat";		// 发送短信邀请页面接受的参数，地标纬度，可以为空
	public static final String BUNDLE_SMS_PlaceName = "BUNDLE_SMS_PlaceName";		// 发送短信邀请页面接受的参数，地标名称
	public static final String BUNDLE_SMS_TempletId = "BUNDLE_SMS_TempletId";		// 发送短信邀请页面接受的参数，模板id
	public final static String BUNDLE_RES_DETAIL_NEXT_PAGE = "resDetailNextPage"; 	//跳转到餐厅详情下的某一子页
	public final static String BUNDLE_MEAL_COMBO_DATA = "BUNDLE_MEAL_COMBO_DATA"; 	//传递套餐数据
    public final static String BUNDLE_PARCELABLE_KEY="parcelableKey";               //传递数据的key
    
    public final static String isSplashActivity="isSplashActivity";//是否来自splashActivity
	//
	public static final String UPLOAD_TYPE_RESTAURANT = "uploadType01";	// 上传图片类型，餐厅图片
	public static final String UPLOAD_TYPE_FOOD = "uploadType02";		// 上传图片类型，菜品图片
	
	public final static String BUNDLE_ORDER_ID_FROM_INDEX_ACTIVITY_TO_ORDER_DETAIL = "orderIdFromIndexActivityToOrderDetail"; //从首页跳转到订单详情时传输的orderId
	public final static String BUNDLE_ADVERTISEMENT_DATA = "BUNDLE_ADVERTISEMENT_DATA"; //从首页跳转到广告页时传输的广告对象
	
	public final static String BUNDLE_KEY_IS_MEALCOMBO = "BUNDLE_KEY_IS_MEALCOMBO"; 	//标识是否是套餐
	public final static String BUNDLE_KEY_IS_QUICK_JUMP = "BUNDLE_KEY_IS_QUICK_JUMP"; 	//标识是否是首页套餐提醒快速跳转的
	public final static String BUNDLE_KEY_NEED_HIDE_BACK_BUTTON = "BUNDLE_KEY_NEED_HIDE_BACK_BUTTON"; 	//是否需要隐藏返回按钮，多文档切换时需要设为false
	//
	
	public final static String BUNDLE_KEY_TYPE = "BUNDLE_KEY_IS_LOGIN"; 	//进入分享页面的类型
	public final static String BUNDLE_KEY_IS_LOGIN = "BUNDLE_KEY_IS_LOGIN"; 	//微博授权页，标识是否是登录
	public final static String BUNDLE_KEY_WEIBO_TYPE_TAG="weiboTypeTag";        //分享微博类型key
	public final static String BUNDLE_KEY_WEIBO_DETAIL="weiboDetail";           //分享内容
	public final static String BUNDLE_KEY_WEIBO_SHARE_INFO="shareInfo";         //分享类别
	public final static String BUNDLE_KEY_SHARE_DETAIL="BUNDLE_KEY_SHARE_DETAIL";         //分享内容
	public final static String BUNDLE_KEY_SHARE_TYPE_TAG="BUNDLE_KEY_SHARE_TYPE_TAG";        //分享类型key
	//
	public final static String BUNDLE_KEY_ERROR_REPORT_TYPE="BUNDLE_KEY_ERROR_REPORT_TYPE";//报错类型
	public final static int BUNDLE_KEY_ERROR_REPORT_TYPE_RESTAURANT=1;//报错类型-餐厅报错
	public final static int BUNDLE_KEY_ERROR_REPORT_TYPE_FOOD=2;//报错类型-菜品报错
	public final static int BUNDLE_KEY_ERROR_REPORT_TYPE_TAKEAWAY=3;//报错类型-外卖报错
	
	public final static String BUNDLE_CASH_COUPON_ORDER_ID = "cashCouponOrderId";//现金券订单Id
	public final static String BUNDLE_TAKEAWAY_COMMON_TYPE_DTO = "commonTypeDTO";
	public final static String BUNDLE_TAKEAWAY_ADDRESS = "takeawayAddress";//送餐地址
	public final static String BUNDLE_TAKEAWAY_TEL = "takeawayTel";//手机号码
	public final static String BUNDLE_TAKEAWAY_NAME = "takeawayname";
	public final static String BUNDLE_FROM_TAG = "fromtag";//判断rest takeaway
	
	
	public static final int Mapbar_Code_Default= 0;
	public static final int Mapbar_Code_GetPoiNameByKeyword = 1;
	public static final int Mapbar_Code_GetDriveByLatLon = 2;
	public static final int Mapbar_Code_GetBusBigCity = 3;
	public static final int Mapbar_Set_MyLoc_To_Center = 4;
	public static final int Mapbar_Close_Map = 5;
	public static final int Mapbar_Search_By_Keyword = 6;
	public static final int Mapbar_Search_By_Myloc = 7;
	public static final int BACK_TO_INDEX=5757;
	public static final int COLUMN_COUNT=2;//新图库菜式显示列数 2
	public static final int PIC_COLUMN_COUNT=3;//新图库 其他图片显示列数 3
	//************缓存设置。added by xu jianjun, 2011-11-21。***************************************************
	public static final long CACHE_MEMORY_FILE_TOTAL_SIZE = 1024*500;	//控制内存缓存中文件的总字节数上限，不能太大，这里是经过测试后的极限值
	public static final long CACHE_MEMORY_VALUE_TOTAL_SIZE = 1024*1024;	//控制内存缓存中文字的总字节数上限
	public static final int CACHE_FILESYSTEM_FILE_MAX_NUMBER = 4000;		//在本地文件系统中可缓存的最大文件数

	public static final String CACHE_DIR_MainPageInfoPackDTO = "CACHE_DIR_MainPageInfoPackDTO";	//首页对象
	public static final String CACHE_DIR_MainPageOtherInfoPackDTO = "CACHE_DIR_MainPageOtherInfoPackDTO";	//首页其它对象
	public static final String CACHE_DIR_ADVERTISEMENT = "CACHE_DIR_ADVERTISEMENT";	//存储广告对象
	public static final String CACHE_DIR_ADVERTISEMENT_CLOSED = "CACHE_DIR_ADVERTISEMENT_CLOSED";	//存储广告对象是否关闭的状态
	
	public static final int INDEX_FIRST_LAYOUT=1;                          //首页第一种布局方式   常规的带有可拖拽的图标的
	public static final int INDEX_SECOND_LAYOUT=2;                         //首页第二种布局方式    优惠倒计时面板
	
	public static final int TOP_LIST_AND_DISCOUNT=-1;                               //榜单和优惠精选都在情况下
	public static final int TOP_LIST_OR_DISCOUNT=-2;                               //只有榜单或优惠精选的情况
	public static final int NO_TOP_LIST_AND_DISCOUNT=-3;                               //榜单和优惠精选都没有的情况
	
	public static final int VOICE_CONTENT_STYLE_01=1;                               //语音订餐界面弹出POP样式1
	public static final int VOICE_CONTENT_STYLE_02=2;                            //语音订餐界面弹出POP样式2
	//**********************************************************************************************************
	
	// 拨打电话的位置附加说明
	public static final String BOTTOM = "bottom"; // 底部
	public static final String POST_RES_RESERVE = "postResReserve"; // 下单
	public static final String REST_TEL = "restTel"; // 餐厅详情
	public static final String CASH_COUPON = "cashCoupon"; // 现金券
	public static final String ABOUT_XMS_BOTTOM = "AboutXMS-bottom"; // 关于我们-客服电话
	public static final String TAKEOUT = "takeout"; // 外卖
	public static final String NO_BOOKING_TEL = "noBookingTel"; // 非特商，预订时直接拨打餐厅号码
	
	public static final int Baidu_Empty = 0;
	public static final int Baidu_Choose_Loc = 1;
	public static final int Baidu_Show_Res = 2;
	public static final int Baidu_Show_Route = 3;
	
	//餐厅详情页面中添加电话号码时的title，用于报错页面中判断，以在提交时显示不同的提示信息
	public static final String ERROR_REPORT_TITLE_ADD_PHONE_IN_REST_DETAIL = "添加号码";
	public static final String ERROR_REPORT_TITLE_WRONG_PHONE_NO_IN_REST_DETAIL = "联系电话错误";
	
	//全局标志，发表评论成功后，如果返回餐厅详情，需要刷新评论
	public static boolean NEED_REFRESH_REST_COMMENT = false;
	//全局标志，刷新餐厅Tag,如果返回餐厅详情，需要刷新标签
	public static boolean NEED_TAG_REST_COMMENT = false;
	//全局标志，刷新餐厅荐店,如果返回餐厅详情，需要刷新荐店
	public static boolean NEED_TAG_REST_RECOMMEND = false;
	//全局标志，刷新外卖列表,如果返回外卖列表，需要刷新外卖列表
	public static boolean NEED_TAKEAWAY_LIST = false;
	public static String COMMENT_RES_ID = ""; // 评论或荐店餐厅的id
	//全局标志，提交图片成功后，如果返回菜品图片详情页，需要刷新整个页面
	public static boolean NEED_REFRESH_FOOD_PICTURE_DETAIL = false;
	//全局标志,是否重推送通知进入app前台
	public static Boolean Is_Push_Notification_to_activity=false;
	
	
	// 拍照类型
	public static final int CAMERAIMAGE = 9999; // 拍照上传
	public static final int LOCALIMAGE = 9998; // 本地上传
	public static final int LOCALIMAGE_BATCH = 9997; // 批量上传
	
	public static final String SHOW_WHEN_FIRST_IN_FOODSERACH = "meedShowFirstAlertWhenSelectFood"; //第一次进入随手拍菜品选择标志
	
	/****************************** 图片上传模块公共变量***********************************/
	//上传图片模块中使用的图片uri，或者是拍照设置的或者是选择图片后设置的，上传页面中读取该值 
	public static String  uploadPictureUri= "";
	public static int uploadPictureOrignalActivityId=0;
	public static Class<? extends Activity> uploadPictureOrignalActivityClazz = IndexActivity.class;
	// 任意地方随手拍完成后，控制回首页然后直接去选择餐厅的标志和相关数据
	public static boolean  JUMP_RIGHT_NOW_FOR_CAPTURE_IS_DONE=false;//标识是否已经成功完成了随手拍，需要回首页后跳转选择餐厅
//	public static Bundle JUMP_RIGHT_NOW_CAPTURE_DATA=null;//随手拍完成后的数据
	
	public static boolean isRestaurantRecommentDetail=false;//是否是推荐餐厅详情使用图片上传
	public static Uri RestaurantRecommentDetailUri=null;//推荐餐厅详情使用图片uri
	/****************************** 发送短信邀请公共变量***********************************/
	public static int sendShortMessageOrignalActivityId=0; //用于发送短信邀请后返回到最初页面
	/**************************************************************************************/
	/****************************** 团购模块公共变量***********************************/
	public static Class<? extends Activity> groupBuyActivityClazz= IndexActivity.class;
//	public static Class<? extends Activity> mdbActivityClazz= IndexActivity.class;
	/* 用于RestaurantSearchActivity跳转至AutoCompleteActivity ,获取关键字*/
	public static final int REQUEST_CODE_GET_KEYWORD = 8000; // 请求Code
	public static final String REQUEST_BUNDLE_KEYWORD = "keyword"; // 存放keyword的Bundle Key
	//订单详情页返回页面
	public static Class<? extends Activity> mdbConsumeActivityClazz= IndexActivity.class;

	/****************************** 首页优惠套餐弹出窗口显隐控制***********************************/
	
	
	public static final String CURRENT_TIME="currentTime";//当前系统时间戳  缓存key
	
	public static final String JUMP_RESCOMMEND_RES_COMMENT_AREA="jump";//推荐餐厅跳转评论区域

	/****************************** webview广告跳转到外卖搜索，urlexecutor解析里面用到***********************************/
	public static final String WAIMAI_BUNDLE_URLFROMAD = "urlfromad";
	public static final String WAIMAI_BUNDLE_TYPEID = "typeid";
	public static final String WAIMAI_BUNDLE_KEYWORDS = "keywords";
	public static final String WAIMAI_BUNDLE_LONGITUDE = "longitude";
	public static final String WAIMAI_BUNDLE_LATITUDE = "latitude";
	public static final String WAIMAI_BUNDLE_POSITIONNAME = "positionname";
	public static final String WAIMAI_BUNDLE_SENDLIMITID = "sendlimitid";
	
	/***************************** 聊天室功能参数 ***********************************/
	public static final String CHAT_ENTER_ROOM_SUCCESS_FLAG = "chat://enterRoomSuccess";                       
	public static final String CHAT_NEED_SEND_GPS_FLAG = "chat://needGps";                       
	public static final String CHAT_ROOM_CLOSED_FLAG = "chat://roomClosed";                       
	public static final String CHAT_NEED_UPDATE= "chat://enterRoomNeedUpgrade";                       
	public static final String CHAT_HAVE_NEW_MESSAGES = "chat://newMsgNumWhenMinimize";
	public static final String CHAT_JUMP_TO_RESTAURANT = "xms://restaurant";   
	
	public static final String LOCAL_HTML_PATH = Environment
			.getExternalStorageDirectory().toString() + "/xiaomishu";
	
	public static final String zipPackageName = "/chat.zip";
	// 相对路径
	public static final String ZIP_PATH = "/chat/";
	
}
