package com.fg114.main.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fg114.main.R;
import com.fg114.main.R.styleable;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.SplashActivity;
import com.fg114.main.app.activity.CityMoreActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.data.MainMenuListInfo;
import com.fg114.main.app.data.RealTimeResFilter;
import com.fg114.main.app.data.TakeAwayFilter;
import com.fg114.main.app.data.UploadDataPack;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.ChatMsgData;
import com.fg114.main.service.dto.ChatMsgListDto;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CityListDTO;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.DishListPackDTO;
import com.fg114.main.service.dto.DishOrderDTO;
import com.fg114.main.service.dto.FoodSubListForSelectData;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.LonLat;
import com.fg114.main.service.dto.MainMenuData;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MainPageInfoPack4DTO;
import com.fg114.main.service.dto.MainPageInfoPackDTO;
import com.fg114.main.service.dto.MainPageMsgListDTO;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.MdbFreeOrderFormData;
import com.fg114.main.service.dto.OrderSelInfo;
import com.fg114.main.service.dto.PageRestInfo3DTO;
import com.fg114.main.service.dto.PayTypeData;
import com.fg114.main.service.dto.ResAndFoodList2DTO;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.RoomTypeInfoData;
import com.fg114.main.service.dto.ShRegionListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.SoftwareCommonData;
import com.fg114.main.service.dto.SuggestResultData;
import com.fg114.main.service.dto.TakeoutIndexPageData;
import com.fg114.main.service.dto.TakeoutMenuData;
import com.fg114.main.service.dto.TakeoutMenuData2;
import com.fg114.main.service.dto.TakeoutMenuListDTO;
import com.fg114.main.service.dto.TakeoutMenuListPackDTO;
import com.fg114.main.service.dto.TakeoutRestInfoData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.weibo.dto.User;
import com.google.xiaomishujson.Gson;
import com.google.xiaomishujson.reflect.TypeToken;

/**
 * Session管理
 * 
 * @author zhangyifan
 * 
 */
public class SessionManager {
	// 功能菜单数据
	// 4.1.2 //九宫格数据：找餐厅,叫外卖,个人中心,快捷预订,现金券,订单中心
	private static final HashMap<String, Integer> MENU_NAMES = new HashMap<String, Integer>();
	static {
		MENU_NAMES.put("找餐厅", 0);
		MENU_NAMES.put("叫外卖", 1);
		MENU_NAMES.put("个人中心", 2);
		MENU_NAMES.put("快捷预订", 3);
		MENU_NAMES.put("现金券", 4);
		MENU_NAMES.put("订单中心", 5);
		MENU_NAMES.put("榜单", 6);
	}
	private static final String kCityDefaultMenuList = "1;0;1;1;0;1;1";
	private static final String kCityMenuListShanghai = "1;1;1;1;1;1;1";
	private static final String kCityMenuListBeijing = "1;1;1;1;1;1;1";
	private static final String kCityGzMenuList = "1;0;1;1;0;1;1";

	private static final String kCityDefaultPhone = "10107777";

	private static final int DEFAULT_HOUR = 18;
	private static final int VALVE_HOUR = 18;
	private static final int VALVE_MINUTE = 00;
	private static final int DEFAULT_MINUTE = 15;

	private static SessionManager instance;

	private SessionManager() {

	}

	public static synchronized SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	// ========================================

	private CityInfo cityInfo; // 所选城市信息
	// 列表信息
	private ListManager listMgr = new ListManager();
	// 筛选条件
	private Filter filter = new Filter();
	// 相关美食的筛选条件
	private Filter similarFoodFilter = new Filter();
	// 实时餐厅的筛选条件
	private RealTimeResFilter realTimeResFilter = new RealTimeResFilter();
	//外卖筛选条件
	private TakeAwayFilter takeawayFilter = new TakeAwayFilter();
	
	// 用户登录信息
	private UserInfoDTO userInfo;

	// 自身位置
	private LonLat lonlatMe = null;
	// 起始位置
	private LonLat lonlatOrg = null;
	// 终点位置
	private LonLat lonlatDest = null;

	// 登录后执行的Runnable
	private Runnable loginSuccessRunnable = null;
	// 上一个Activity
	private Activity lastActivity = null;

	// 城市信息
	private CityListDTO cityListDTO;

	// 主菜系
	private CommonTypeListDTO foodMainTypeListDto;

	// 用户好友列表
	private CommonTypeListDTO userFriendListDto;

	// 点菜的订单
	private DishOrderDTO dishOrder;
	// 点菜的菜单
	private DishListPackDTO dishListPackDTO;

	// 热门城市信息
	private CityListDTO hotCityListDTO;

	private List<DishData> dishDataList = new ArrayList<DishData>();

	// 根据算法计算出的当前真实所在城市
	public CityInfo cityInfoByPoly;

	private double longitudeLast;
	private double latitudeLast;

	public ResAndFoodList2DTO cacheResAndFoodList2DTO;

	public static final UploadDataPack uploadData = new UploadDataPack(); // 上传图片数据，用于在上传页面之间传递

	private String pointsHintForShareSoftware = "";

	// 上海
	private ShRegionListDTO shRegionListDTO;

	// 本地缓存的语音订餐的消息
	private ChatMsgListDto chatMsgListDto;

	private boolean chatOnline = false;

	// 普通请求超时时间
	private int normalRequestTimeout = 15;
	// 上传请求超时时间
	private int uploadRequestTimeout = 30;

	public RealTimeResFilter getRealTimeResFilter() {
		return realTimeResFilter;
	}

	public void setRealTimeResFilter(RealTimeResFilter realTimeResFilter) {
		this.realTimeResFilter = realTimeResFilter;
	}
	//
	public TakeAwayFilter getTakeAwayFilter() {
		return takeawayFilter;
	}
	
	public void setTakeAwayFilter(TakeAwayFilter takeawayFilter) {
		this.takeawayFilter = takeawayFilter;
	}

	public Filter getSimilarFoodFilter() {
		return similarFoodFilter;
	}

	public List<DishData> getDishDataList() {
		return dishDataList;
	}

	public void setDishDataList(List<DishData> dishDataList) {
		this.dishDataList = dishDataList;
	}

	// -----------------------------------
	/**
	 * 从session中获得所选城市信息
	 */
	public CityInfo getCityInfo(Context ctx) {
		try {
			if (cityInfo == null) {
				String json = SharedprefUtil.get(ctx, Settings.CITY_KEY, "{}");
				cityInfo = CityInfo.toBean(new JSONObject(json));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cityInfo;
	}

	/**
	 * 将所选城市信息存入session中
	 * 
	 * @param ctx
	 * @param userInfo
	 */
	public void setCityInfo(Context ctx, CityInfo cityInfo) {
		this.cityInfo = cityInfo;
		String jsonCityInfo = new Gson().toJson(cityInfo);
		SharedprefUtil.save(ctx, Settings.CITY_KEY, jsonCityInfo);
		getResAndFoodListFromCache(true);
	}

	/**
	 * 从session中获得餐厅详细信息
	 * 
	 * @param ctx
	 * @param id
	 * @return
	 */
	public RestInfoData getRestaurantInfo(Context ctx, String id) {

		/*
		 * RestaurantInfo info = resDetailMap.get(id); try { if(info == null){
		 * //当前缓存中不存在，从文件中取的 String json = SharedprefUtil.get(ctx,
		 * Settings.RESTAURANT_KEY+"_"+id, "{}");
		 * 
		 * info = RestaurantInfo.toBean(new JSONObject(json));
		 * resDetailMap.put(id, info); } } catch (JSONException e) {
		 * e.printStackTrace(); }
		 */
		RestInfoData info = null;
		ValueObject vo = ValueCacheUtil.getInstance(ctx).get(Settings.RESTAURANT_KEY + "|" + ActivityUtil.getVersionName(ctx), id);
		String json = vo == null ? "{}" : vo.getValue();
		info = JsonUtils.fromJson(json, RestInfoData.class);
		return info;
	}

	/**
	 * 将餐厅详细信息存入session中
	 * 
	 * @param ctx
	 * @param userInfo
	 */
	public void setRestaurantInfo(Context ctx, RestInfoData info) {
		/*
		 * resDetailMap.put(info.getId(), info); String json = new
		 * Gson().toJson(info);
		 * 
		 * SharedprefUtil.save(ctx, Settings.RESTAURANT_KEY+"_"+info.getId(),
		 * json);
		 */

		// 新缓存
		String json = new Gson().toJson(info);
		ValueCacheUtil.getInstance(ctx).remove(Settings.RESTAURANT_KEY + "|" + ActivityUtil.getVersionName(ctx), info.uuid);
		ValueCacheUtil.getInstance(ctx).add(Settings.RESTAURANT_KEY + "|" + ActivityUtil.getVersionName(ctx), info.uuid, json, "0", "-", 30); // 30分钟超时，并且含有软件版本信息
	}

	/**
	 * 从session中获得用户信息
	 */

	public UserInfoDTO getUserInfo(Context ctx) {
		try {
			if (!SessionManager.getInstance().isUserLogin(ctx)) {
				return new UserInfoDTO();
			}
			if (userInfo == null) {
				userInfo = new UserInfoDTO();
				String jsonUserInfo = SharedprefUtil.get(ctx, Settings.LOGIN_USER_INFO_KEY, "{}");
				userInfo = JsonUtils.fromJson(jsonUserInfo, UserInfoDTO.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userInfo;
	}

	/**
	 * 0 -->nikeName 1 -->tel
	 * 
	 * @param ctx
	 * @return
	 */
	public String[] getOrderUserInfo(Context ctx) {
		String[] msg = new String[3];
		msg[0] = SharedprefUtil.get(ctx, "ORDER_NIKENAME", "");
		msg[1] = SharedprefUtil.get(ctx, "ORDER_PHONENUMBER", "");
		msg[2] = SharedprefUtil.get(ctx, "ORDER_SEX", "");
		return msg;
	}

	// 得到关闭广告栏的时间戳
	public long getAdvCloseTimeStamp(Context ctx) {
		return SharedprefUtil.getLong(ctx, "ADV_CLOSE_TIME_STAMP", 0);
	}

	// 设置关闭广告栏的时间戳
	public void setAdvCloseTimeStamp(Context ctx, long timeStamp) {
		SharedprefUtil.saveLong(ctx, "ADV_CLOSE_TIME_STAMP", timeStamp);
	}

	public void setOrderUserInfo(Context ctx, String nikeName, String tel, String sex) {
		// SharedprefUtil.resetByKey(ctx, "ORDER_NIKENAME");
		// SharedprefUtil.resetByKey(ctx, "ORDER_PHONENUMBER");
		SharedprefUtil.save(ctx, "ORDER_NIKENAME", nikeName);
		SharedprefUtil.save(ctx, "ORDER_PHONENUMBER", tel);
		SharedprefUtil.save(ctx, "ORDER_SEX", sex);
	}

	/**
	 * 将用户信息存入session中
	 * 
	 * @param ctx
	 * @param userInfo
	 */
	public void setUserInfo(Context ctx, UserInfoDTO userInfo) {

		this.userInfo = userInfo;
		String jsonUserInfo = new Gson().toJson(userInfo);
		SharedprefUtil.save(ctx, Settings.LOGIN_USER_INFO_KEY, jsonUserInfo);
	}

	/**
	 * 将按钮移动后相对位子存入session中
	 * 
	 * @param ctx
	 * @param View
	 */
	public void saveViewMoveInfo(Context ctx, String moveInfo, String key) {

		SharedprefUtil.save(ctx, key, moveInfo);
	}

	/**
	 * 获取按钮移动后相对位子
	 * 
	 * @param ctx
	 * @param Key
	 */
	public String getViewMoveInfo(Context ctx, String key) {
		return SharedprefUtil.get(ctx, key, "0");

	}

	/**
	 * 从session中获得用户登录状态
	 */
	public boolean isUserLogin(Context ctx) {
		return SharedprefUtil.getBoolean(ctx, Settings.IS_LOGIN_KEY, false);
	}

	/**
	 * 设置用户登录状态存入session中
	 * 
	 * @param ctx
	 * @param userInfo
	 */
	public void setIsUserLogin(Context ctx, boolean isLogin) {
		SharedprefUtil.saveBoolean(ctx, Settings.IS_LOGIN_KEY, isLogin);
	}

	/**
	 * 从session中获得列表信息
	 * 
	 * @return
	 */
	public ListManager getListManager() {
		return listMgr;
	}

	/**
	 * 从session中获得查询条件
	 * 
	 * @return
	 */
	public Filter getFilter() {
		return filter;
	}

	public LonLat getLonlatMe() {
		return lonlatMe;
	}

	public void setLonlatMe(LonLat lonlatMe) {
		this.lonlatMe = lonlatMe;
	}

	public LonLat getLonlatOrg() {
		return lonlatOrg;
	}

	public void setLonlatOrg(LonLat lonlatOrg) {
		this.lonlatOrg = lonlatOrg;
	}

	public LonLat getLonlatDest() {
		return lonlatDest;
	}

	public void setLonlatDest(LonLat lonlatDest) {
		this.lonlatDest = lonlatDest;
	}

	public Runnable getLoginSuccessRunnable() {
		return loginSuccessRunnable;
	}

	public void setLoginSuccessRunnable(Runnable loginSuccessRunnable) {
		this.loginSuccessRunnable = loginSuccessRunnable;
	}

	public Activity getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Activity lastActivity) {
		this.lastActivity = lastActivity;
	}

	/**
	 * 获得好友列表
	 * 
	 * @param context
	 * @return
	 */
	public CommonTypeListDTO getUserFriendList(Context context) {
		try {
			if (userFriendListDto == null) {
				String jsonStr = SharedprefUtil.get(context, Settings.USER_FRIEND_LIST, "{}");
				userFriendListDto = CommonTypeListDTO.toBean(new JSONObject(jsonStr));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userFriendListDto;
	}

	public boolean isCanRockBtn() {
		return SharedprefUtil.getBoolean(ContextUtil.getContext(), Settings.ROCK_BTN_KEY, true);

	}

	public void setCanRockBtn(boolean canRock) {

		SharedprefUtil.saveBoolean(ContextUtil.getContext(), Settings.ROCK_BTN_KEY, canRock);

	}

	/**
	 * 设置好友列表
	 * 
	 * @param context
	 * @param dto
	 */
	public void setUserFriendList(Context context, CommonTypeListDTO dto) {
		userFriendListDto = dto;
		SharedprefUtil.save(context, Settings.USER_FRIEND_LIST + "_" + SessionManager.getInstance().getUserInfo(context).getToken(), JsonUtils.toJson(userFriendListDto));
	}

	/**
	 * 获得UUID
	 * 
	 * @param context
	 * @return
	 */
	public String getUUID(Context context) {
		String uuid = "";
		try {
			if (ActivityUtil.existSDcard()) {
				// SD卡可用时操作SD卡

				// 检查SD卡中是否有UUID
				uuid = ActivityUtil.readFileFromSD(context, Settings.UUID);
				if (CheckUtil.isEmpty(uuid)) {
					// SD卡中没有时检查缓存中是否有UUID，缓存中存在的情况下使用缓存中的UUID
					uuid = SharedprefUtil.get(context, Settings.UUID, "");
					// 同时写入SD卡备用，下次获取uuid时使用SD卡中的uuid
					ActivityUtil.writeFileToSD(context, uuid, Settings.UUID);

					if (CheckUtil.isEmpty(uuid)) {
						// 缓存中也不存在的情况下创建新的UUID
						uuid = UUID.randomUUID().toString();
						ActivityUtil.writeFileToSD(context, uuid, Settings.UUID);
						// 同时写入缓存备用，下次SD卡拔出时，如应用还未卸载，则可使用此uuid
						SharedprefUtil.save(context, Settings.UUID, uuid);
					}
				} else {
					// SD卡存在UUID时，检查缓存中是否存有UUID
					String uuidInPref = SharedprefUtil.get(context, Settings.UUID, "");
					if (TextUtils.isEmpty(uuidInPref) || !uuidInPref.equals(uuid)) {
						// 如果缓存中不存在UUID，或缓存中的UUID不等于SD卡中的UUID(曾经清除过缓存或卸载过应用，但SD卡还保留了之前的唯一id)
						// 将SD卡中UUID写入缓存，下次SD卡拔出时，如应用还未卸载，则可使用此uuid
						SharedprefUtil.save(context, Settings.UUID, uuid);
					}
				}
			} else {
				// SD卡不可用时操作缓存
				uuid = SharedprefUtil.get(context, Settings.UUID, "");
				if (CheckUtil.isEmpty(uuid)) {
					uuid = UUID.randomUUID().toString();
					SharedprefUtil.save(context, Settings.UUID, uuid);
				}
			}
		} catch (Exception e) {
			// 有异常时使用缓存
			uuid = SharedprefUtil.get(context, Settings.UUID, "");
			if (CheckUtil.isEmpty(uuid)) {
				uuid = UUID.randomUUID().toString();
				SharedprefUtil.save(context, Settings.UUID, uuid);
			}
		}

		return uuid;
	}

	/**
	 * 获得主菜系
	 * 
	 * @param context
	 * @return
	 */
	public CommonTypeListDTO getFoodMainTypeListDTO(Context context) {
		try {
			if (foodMainTypeListDto == null) {
				String jsonStr = SharedprefUtil.get(context, Settings.FOOD_MAIN_TYPE, "{}");
				foodMainTypeListDto = CommonTypeListDTO.toBean(new JSONObject(jsonStr));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foodMainTypeListDto;
	}

	/**
	 * 设置主菜系缓存
	 * 
	 * @param context
	 */
	public void setFoodMainTypeListDTO(Context context, CommonTypeListDTO dto) {
		foodMainTypeListDto = dto;
		SharedprefUtil.save(context, Settings.FOOD_MAIN_TYPE, JsonUtils.toJson(foodMainTypeListDto));
	}

	/**
	 * 取得GPS定位的城市
	 * 
	 * @param context
	 * @return
	 */
	public CityInfo getGpsCity(Context context) {
		CityInfo cityInfo = new CityInfo();
		String strCity = SharedprefUtil.get(context, Settings.GPS_CITY, "{}");
		cityInfo = JsonUtils.fromJson(strCity, CityInfo.class);
		// GPS城市缓存为空时返回null，2012.2.10
		if (CheckUtil.isEmpty(cityInfo.getId())) {
			return null;
		}
		return cityInfo;
	}

	/**
	 * 设置GPS定位的城市
	 * 
	 * @param context
	 * @param cityInfo
	 */
	public void setGpsCity(Context context, CityInfo cityInfo) {
		SharedprefUtil.save(context, Settings.GPS_CITY, JsonUtils.toJson(cityInfo));
	}

	/**
	 * 从Asset初始化城市数据
	 * 
	 * @param context
	 */
	public void initCityData(Context context) {
		cityListDTO = new CityListDTO();
		CityData c;

		// c = new CityData();
		// c.setCityId("200000");
		// c.setCityName("上海");
		// c.setMainMenuInfo("1:附近餐厅:1;2:搜索:1;3:附近的菜:1;4:榜单:1;5:优惠精选:1;6:我的订单:1;7:用户中心:1;8:随手拍:1;9:建议:1");
		// c.setPhone("57575777");
		// c.setShowTag(true);
		// c.setPolygonInfo(kCityGpsshanghai);
		// c.setPinyin("shanghai");
		// c.setFirstLetter("sh");
		// c.setHotTag(true);
		// cityListDTO.getList().add(c);
		//
		// c = new CityData();
		// c.setCityId("100000");
		// c.setCityName("北京");
		// c.setMainMenuInfo("1:附近餐厅:1;2:搜索:1;3:附近的菜:1;4:榜单:1;5:优惠精选:1;6:我的订单:1;7:用户中心:1;8:随手拍:1;9:建议:1");
		// c.setPhone(CITY_DEFAULT_PHONE);
		// c.setShowTag(true);
		// c.setPolygonInfo(kCityGpsbeijing);
		// c.setPinyin("beijing");
		// c.setFirstLetter("bj");
		// c.setHotTag(true);
		// cityListDTO.getList().add(c);

		// 上海-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "200000";
		c.cityName = "上海";
		c.firstLetter = "SH";
		c.pinyin = "SHANGHAI";

		c.mainMenuInfo = kCityMenuListShanghai;
		c.phone = "57575777";
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 北京-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "100000";
		c.cityName = "北京";
		c.firstLetter = "BJ";
		c.pinyin = "BEIJING";

		c.mainMenuInfo = kCityMenuListBeijing;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 广州-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "510000";
		c.cityName = "广州";
		c.firstLetter = "GZ";
		c.pinyin = "GUANGZHOU";

		c.mainMenuInfo = kCityGzMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿坝-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "624000";
		c.cityName = "阿坝";
		c.firstLetter = "AB";
		c.pinyin = "ABA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿克苏地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "842000";
		c.cityName = "阿克苏地区";
		c.firstLetter = "AKSDQ";
		c.pinyin = "AKESUDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿拉尔-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "843301";
		c.cityName = "阿拉尔";
		c.firstLetter = "ALE";
		c.pinyin = "ALAER";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿拉善-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "735400";
		c.cityName = "阿拉善";
		c.firstLetter = "ALS";
		c.pinyin = "ALASHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿勒泰地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "836102";
		c.cityName = "阿勒泰地区";
		c.firstLetter = "ALTDQ";
		c.pinyin = "ALETAIDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阿里-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "859000";
		c.cityName = "阿里";
		c.firstLetter = "AL";
		c.pinyin = "ALI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 安康-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "711600";
		c.cityName = "安康";
		c.firstLetter = "AK";
		c.pinyin = "ANKANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 安庆-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "231482";
		c.cityName = "安庆";
		c.firstLetter = "AQ";
		c.pinyin = "ANQING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 安顺-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "550800";
		c.cityName = "安顺";
		c.firstLetter = "AS";
		c.pinyin = "ANSHUN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 安阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "455000";
		c.cityName = "安阳";
		c.firstLetter = "AY";
		c.pinyin = "ANYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鞍山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "114000";
		c.cityName = "鞍山";
		c.firstLetter = "AS";
		c.pinyin = "ANSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 巴彦淖尔-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "14400";
		c.cityName = "巴彦淖尔";
		c.firstLetter = "BYNE";
		c.pinyin = "BAYANNAOER";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 巴音郭楞-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "841100";
		c.cityName = "巴音郭楞";
		c.firstLetter = "BYGL";
		c.pinyin = "BAYINGUOLENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 巴中-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "636000";
		c.cityName = "巴中";
		c.firstLetter = "BZ";
		c.pinyin = "BAZHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 白城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "137000";
		c.cityName = "白城";
		c.firstLetter = "BC";
		c.pinyin = "BAICHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 白沙-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572800";
		c.cityName = "白沙";
		c.firstLetter = "BS";
		c.pinyin = "BAISHA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 白山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "134300";
		c.cityName = "白山";
		c.firstLetter = "BS";
		c.pinyin = "BAISHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 白银-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "730400";
		c.cityName = "白银";
		c.firstLetter = "BY";
		c.pinyin = "BAIYIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 百色-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "531400";
		c.cityName = "百色";
		c.firstLetter = "BS";
		c.pinyin = "BAISE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 蚌埠-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "233000";
		c.cityName = "蚌埠";
		c.firstLetter = "BB";
		c.pinyin = "BENGBU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 包头-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "14000";
		c.cityName = "包头";
		c.firstLetter = "BT";
		c.pinyin = "BAOTOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宝鸡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "721000";
		c.cityName = "宝鸡";
		c.firstLetter = "BJ";
		c.pinyin = "BAOJI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 保定-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "71000";
		c.cityName = "保定";
		c.firstLetter = "BD";
		c.pinyin = "BAODING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 保山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "678000";
		c.cityName = "保山";
		c.firstLetter = "BS";
		c.pinyin = "BAOSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 保亭-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572300";
		c.cityName = "保亭";
		c.firstLetter = "BT";
		c.pinyin = "BAOTING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 北海-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "536000";
		c.cityName = "北海";
		c.firstLetter = "BH";
		c.pinyin = "BEIHAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 本溪-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "117000";
		c.cityName = "本溪";
		c.firstLetter = "BX";
		c.pinyin = "BENXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 毕节地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "551500";
		c.cityName = "毕节地区";
		c.firstLetter = "BJDQ";
		c.pinyin = "BIJIEDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 滨州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "251700";
		c.cityName = "滨州";
		c.firstLetter = "BZ";
		c.pinyin = "BINZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 亳州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "233501";
		c.cityName = "亳州";
		c.firstLetter = "BZ";
		c.pinyin = "BOZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 博尔塔拉-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "833300";
		c.cityName = "博尔塔拉";
		c.firstLetter = "BETL";
		c.pinyin = "BOERTALA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 沧州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "61000";
		c.cityName = "沧州";
		c.firstLetter = "CZ";
		c.pinyin = "CANGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 昌都地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "854000";
		c.cityName = "昌都地区";
		c.firstLetter = "CDDQ";
		c.pinyin = "CHANGDUDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 昌吉州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "831100";
		c.cityName = "昌吉州";
		c.firstLetter = "CJZ";
		c.pinyin = "CHANGJIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 昌江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572700";
		c.cityName = "昌江";
		c.firstLetter = "CJ";
		c.pinyin = "CHANGJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 常德-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "415000";
		c.cityName = "常德";
		c.firstLetter = "CD";
		c.pinyin = "CHANGDE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 常州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "213000";
		c.cityName = "常州";
		c.firstLetter = "CZ";
		c.pinyin = "CHANGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 巢湖-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "231501";
		c.cityName = "巢湖";
		c.firstLetter = "CH";
		c.pinyin = "CHAOHU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 朝阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "122000";
		c.cityName = "朝阳";
		c.firstLetter = "CY";
		c.pinyin = "CHAOYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 潮州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "521000";
		c.cityName = "潮州";
		c.firstLetter = "CZ";
		c.pinyin = "CHAOZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 郴州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "423000";
		c.cityName = "郴州";
		c.firstLetter = "CZ";
		c.pinyin = "CHENZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 成都-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "610000";
		c.cityName = "成都";
		c.firstLetter = "CD";
		c.pinyin = "CHENGDU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 承德-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "67000";
		c.cityName = "承德";
		c.firstLetter = "CD";
		c.pinyin = "CHENGDE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 澄迈县-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571900";
		c.cityName = "澄迈县";
		c.firstLetter = "CMX";
		c.pinyin = "CHENGMAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 池州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "242800";
		c.cityName = "池州";
		c.firstLetter = "CZ";
		c.pinyin = "CHIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 赤峰-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "24000";
		c.cityName = "赤峰";
		c.firstLetter = "CF";
		c.pinyin = "CHIFENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 崇左-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "532100";
		c.cityName = "崇左";
		c.firstLetter = "CZ";
		c.pinyin = "CHONGZUO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 滁州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "233100";
		c.cityName = "滁州";
		c.firstLetter = "CZ";
		c.pinyin = "CHUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 楚雄州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "651208";
		c.cityName = "楚雄州";
		c.firstLetter = "CXZ";
		c.pinyin = "CHUXIONGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 达州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "635000";
		c.cityName = "达州";
		c.firstLetter = "DZ";
		c.pinyin = "DAZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 大理州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "671000";
		c.cityName = "大理州";
		c.firstLetter = "DLZ";
		c.pinyin = "DALIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 大连-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "116000";
		c.cityName = "大连";
		c.firstLetter = "DL";
		c.pinyin = "DALIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 大庆-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "163000";
		c.cityName = "大庆";
		c.firstLetter = "DQ";
		c.pinyin = "DAQING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 大同-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "34401";
		c.cityName = "大同";
		c.firstLetter = "DT";
		c.pinyin = "DATONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 大兴安岭-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "165000";
		c.cityName = "大兴安岭";
		c.firstLetter = "DXAL";
		c.pinyin = "DAXINGANLING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 丹东-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "118000";
		c.cityName = "丹东";
		c.firstLetter = "DD";
		c.pinyin = "DANDONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 儋州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571700";
		c.cityName = "儋州";
		c.firstLetter = "DZ";
		c.pinyin = "DANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 德宏-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "679200";
		c.cityName = "德宏";
		c.firstLetter = "DH";
		c.pinyin = "DEHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 德阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "618000";
		c.cityName = "德阳";
		c.firstLetter = "DY";
		c.pinyin = "DEYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 德州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "251100";
		c.cityName = "德州";
		c.firstLetter = "DZ";
		c.pinyin = "DEZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 迪庆-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "674400";
		c.cityName = "迪庆";
		c.firstLetter = "DQ";
		c.pinyin = "DIQING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 定安县-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571200";
		c.cityName = "定安县";
		c.firstLetter = "DAX";
		c.pinyin = "DINGAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 定西-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "478301";
		c.cityName = "定西";
		c.firstLetter = "DX";
		c.pinyin = "DINGXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 东方-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572600";
		c.cityName = "东方";
		c.firstLetter = "DF";
		c.pinyin = "DONGFANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 东莞-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "523000";
		c.cityName = "东莞";
		c.firstLetter = "DZ";
		c.pinyin = "DONGGUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 东营-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "257000";
		c.cityName = "东营";
		c.firstLetter = "DY";
		c.pinyin = "DONGYING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鄂尔多斯-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "10300";
		c.cityName = "鄂尔多斯";
		c.firstLetter = "EEDS";
		c.pinyin = "EERDUOSI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鄂州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "436000";
		c.cityName = "鄂州";
		c.firstLetter = "EZ";
		c.pinyin = "EZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 恩施州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "444300";
		c.cityName = "恩施州";
		c.firstLetter = "ESZ";
		c.pinyin = "ENSHIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 防城港-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "535500";
		c.cityName = "防城港";
		c.firstLetter = "FCG";
		c.pinyin = "FANGCHENGGANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 佛山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "528000";
		c.cityName = "佛山";
		c.firstLetter = "FS";
		c.pinyin = "FOSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 福州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "350000";
		c.cityName = "福州";
		c.firstLetter = "FZ";
		c.pinyin = "FUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 抚顺-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "113000";
		c.cityName = "抚顺";
		c.firstLetter = "FS";
		c.pinyin = "FUSHUN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 抚州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "331800";
		c.cityName = "抚州";
		c.firstLetter = "FZ";
		c.pinyin = "FUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阜新-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "123000";
		c.cityName = "阜新";
		c.firstLetter = "FX";
		c.pinyin = "FUXIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阜阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "236000";
		c.cityName = "阜阳";
		c.firstLetter = "FY";
		c.pinyin = "FUYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 甘南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "746300";
		c.cityName = "甘南";
		c.firstLetter = "GN";
		c.pinyin = "GANNANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 甘孜州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "626000";
		c.cityName = "甘孜州";
		c.firstLetter = "GZZ";
		c.pinyin = "GANZI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 赣州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "341000";
		c.cityName = "赣州";
		c.firstLetter = "GZ";
		c.pinyin = "GANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 固原-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "756000";
		c.cityName = "固原";
		c.firstLetter = "GY";
		c.pinyin = "GUYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 广安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "638000";
		c.cityName = "广安";
		c.firstLetter = "GA";
		c.pinyin = "GUANGAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 广元-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "628008";
		c.cityName = "广元";
		c.firstLetter = "GY";
		c.pinyin = "GUANGYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 贵港-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "537100";
		c.cityName = "贵港";
		c.firstLetter = "GG";
		c.pinyin = "GUIGANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 贵阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "550000";
		c.cityName = "贵阳";
		c.firstLetter = "GY";
		c.pinyin = "GUIYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 桂林-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "541000";
		c.cityName = "桂林";
		c.firstLetter = "GL";
		c.pinyin = "GUILIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 果洛-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "624700";
		c.cityName = "果洛";
		c.firstLetter = "GL";
		c.pinyin = "GUOLUO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 哈尔滨-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "150000";
		c.cityName = "哈尔滨";
		c.firstLetter = "HEB";
		c.pinyin = "HAERBIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 哈密地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "839201";
		c.cityName = "哈密地区";
		c.firstLetter = "HMDQ";
		c.pinyin = "HAMIDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 海北-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "810200";
		c.cityName = "海北";
		c.firstLetter = "HB";
		c.pinyin = "HAIBEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 海东-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "810500";
		c.cityName = "海东";
		c.firstLetter = "HD";
		c.pinyin = "HAIDONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 海口-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571000";
		c.cityName = "海口";
		c.firstLetter = "HK";
		c.pinyin = "HAIKOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 海南州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "811700";
		c.cityName = "海南州";
		c.firstLetter = "HNZ";
		c.pinyin = "HAINANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 海西-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "816100";
		c.cityName = "海西";
		c.firstLetter = "HX";
		c.pinyin = "HAIXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 邯郸-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "56000";
		c.cityName = "邯郸";
		c.firstLetter = "HD";
		c.pinyin = "HANDAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 汉中-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "723000";
		c.cityName = "汉中";
		c.firstLetter = "HZ";
		c.pinyin = "HANZHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 杭州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "310000";
		c.cityName = "杭州";
		c.firstLetter = "HZ";
		c.pinyin = "HANGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 合肥-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "230000";
		c.cityName = "合肥";
		c.firstLetter = "HF";
		c.pinyin = "HEFEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 和田地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "845151";
		c.cityName = "和田地区";
		c.firstLetter = "HTDQ";
		c.pinyin = "HETIANDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 河池-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "452730";
		c.cityName = "河池";
		c.firstLetter = "HC";
		c.pinyin = "HECHI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 河源-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "517000";
		c.cityName = "河源";
		c.firstLetter = "HY";
		c.pinyin = "HEYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 菏泽-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "274000";
		c.cityName = "菏泽";
		c.firstLetter = "HZ";
		c.pinyin = "HEZE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 贺州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "542600";
		c.cityName = "贺州";
		c.firstLetter = "HZ";
		c.pinyin = "HEZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鹤壁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "456250";
		c.cityName = "鹤壁";
		c.firstLetter = "HB";
		c.pinyin = "HEBI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鹤岗-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "154100";
		c.cityName = "鹤岗";
		c.firstLetter = "HG";
		c.pinyin = "HEGANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黑河-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "161400";
		c.cityName = "黑河";
		c.firstLetter = "HH";
		c.pinyin = "HEIHE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 衡水-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "53000";
		c.cityName = "衡水";
		c.firstLetter = "HS";
		c.pinyin = "HENGSHUI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 衡阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "421000";
		c.cityName = "衡阳";
		c.firstLetter = "HY";
		c.pinyin = "HENGYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 红河-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "652300";
		c.cityName = "红河";
		c.firstLetter = "HH";
		c.pinyin = "HONGHE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 呼和浩特-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "10000";
		c.cityName = "呼和浩特";
		c.firstLetter = "HHHT";
		c.pinyin = "HUHEHAOTE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 呼伦贝尔-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "21000";
		c.cityName = "呼伦贝尔";
		c.firstLetter = "HLBE";
		c.pinyin = "HULUNBEIER";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 葫芦岛-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "125000";
		c.cityName = "葫芦岛";
		c.firstLetter = "HLD";
		c.pinyin = "HULUDAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 湖州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "313000";
		c.cityName = "湖州";
		c.firstLetter = "HZ";
		c.pinyin = "HUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 怀化-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "418000";
		c.cityName = "怀化";
		c.firstLetter = "HH";
		c.pinyin = "HUAIHUA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 淮安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "211600";
		c.cityName = "淮安";
		c.firstLetter = "HA";
		c.pinyin = "HUAIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 淮北-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "235000";
		c.cityName = "淮北";
		c.firstLetter = "HB";
		c.pinyin = "HUAIBEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 淮南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "232000";
		c.cityName = "淮南";
		c.firstLetter = "HN";
		c.pinyin = "HUAINAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黄冈-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "435300";
		c.cityName = "黄冈";
		c.firstLetter = "HG";
		c.pinyin = "HUANGGANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黄南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "811200";
		c.cityName = "黄南";
		c.firstLetter = "HN";
		c.pinyin = "HUANGNAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黄山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "245000";
		c.cityName = "黄山";
		c.firstLetter = "HS";
		c.pinyin = "HUANGSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黄石-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "435000";
		c.cityName = "黄石";
		c.firstLetter = "HS";
		c.pinyin = "HUANGSHI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 惠州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "516000";
		c.cityName = "惠州";
		c.firstLetter = "HZ";
		c.pinyin = "HUIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鸡西-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "158100";
		c.cityName = "鸡西";
		c.firstLetter = "JX";
		c.pinyin = "JIXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 吉安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "331300";
		c.cityName = "吉安";
		c.firstLetter = "JA";
		c.pinyin = "JIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 吉林-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "132000";
		c.cityName = "吉林";
		c.firstLetter = "JL";
		c.pinyin = "JILIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 济南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "250000";
		c.cityName = "济南";
		c.firstLetter = "JN";
		c.pinyin = "JINAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 济宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "272053";
		c.cityName = "济宁";
		c.firstLetter = "JN";
		c.pinyin = "JINING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 佳木斯-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "154000";
		c.cityName = "佳木斯";
		c.firstLetter = "JMS";
		c.pinyin = "JIAMUSI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 嘉兴-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "314000";
		c.cityName = "嘉兴";
		c.firstLetter = "JX";
		c.pinyin = "JIAXING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 嘉峪关-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "735100";
		c.cityName = "嘉峪关";
		c.firstLetter = "JYG";
		c.pinyin = "JIAYUGUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 江门-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "529000";
		c.cityName = "江门";
		c.firstLetter = "JM";
		c.pinyin = "JIANGMEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 焦作-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "454000";
		c.cityName = "焦作";
		c.firstLetter = "JZ";
		c.pinyin = "JIAOZUO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 揭阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "522000";
		c.cityName = "揭阳";
		c.firstLetter = "JY";
		c.pinyin = "JIEYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 金昌-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "737000";
		c.cityName = "金昌";
		c.firstLetter = "JC";
		c.pinyin = "JINCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 金华-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "321000";
		c.cityName = "金华";
		c.firstLetter = "JH";
		c.pinyin = "JINHUA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 锦州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "121000";
		c.cityName = "锦州";
		c.firstLetter = "JZ";
		c.pinyin = "JINZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 晋城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "48000";
		c.cityName = "晋城";
		c.firstLetter = "JC";
		c.pinyin = "JINCHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 晋中-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "30600";
		c.cityName = "晋中";
		c.firstLetter = "JZ";
		c.pinyin = "JINZHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 荆门-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "431800";
		c.cityName = "荆门";
		c.firstLetter = "JM";
		c.pinyin = "JINMEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 荆州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "433300";
		c.cityName = "荆州";
		c.firstLetter = "JZ";
		c.pinyin = "JINGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 景德镇-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "333000";
		c.cityName = "景德镇";
		c.firstLetter = "JDZ";
		c.pinyin = "JINGDEZHEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 九江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "330300";
		c.cityName = "九江";
		c.firstLetter = "JJ";
		c.pinyin = "JIUJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 酒泉-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "735000";
		c.cityName = "酒泉";
		c.firstLetter = "JQ";
		c.pinyin = "JIUQUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 喀什地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "843800";
		c.cityName = "喀什地区";
		c.firstLetter = "KSDQ";
		c.pinyin = "KASHIDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 开封-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "475000";
		c.cityName = "开封";
		c.firstLetter = "KF";
		c.pinyin = "KAIFENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 克拉玛依-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "833600";
		c.cityName = "克拉玛依";
		c.firstLetter = "KLMY";
		c.pinyin = "KELAMAYI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 克孜勒苏-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "843500";
		c.cityName = "克孜勒苏";
		c.firstLetter = "KZLS";
		c.pinyin = "KEZILESU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 昆明-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "650000";
		c.cityName = "昆明";
		c.firstLetter = "KM";
		c.pinyin = "KUNMING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 拉萨-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "850000";
		c.cityName = "拉萨";
		c.firstLetter = "LS";
		c.pinyin = "LASA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 来宾-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "545700";
		c.cityName = "来宾";
		c.firstLetter = "LB";
		c.pinyin = "LAIBIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 莱芜-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "270006";
		c.cityName = "莱芜";
		c.firstLetter = "LW";
		c.pinyin = "LAIWU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 兰州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "730000";
		c.cityName = "兰州";
		c.firstLetter = "LZ";
		c.pinyin = "LANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 廊坊-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "65000";
		c.cityName = "廊坊";
		c.firstLetter = "LF";
		c.pinyin = "LANGFANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 乐东-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572500";
		c.cityName = "乐东";
		c.firstLetter = "LD";
		c.pinyin = "LEDONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 乐山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "614000";
		c.cityName = "乐山";
		c.firstLetter = "LS";
		c.pinyin = "LESHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 丽江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "674100";
		c.cityName = "丽江";
		c.firstLetter = "LJ";
		c.pinyin = "LIJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 丽水-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "321400";
		c.cityName = "丽水";
		c.firstLetter = "LS";
		c.pinyin = "LISHUI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 连云港-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "222000";
		c.cityName = "连云港";
		c.firstLetter = "LYG";
		c.pinyin = "LIANYUANGANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 凉山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "615100";
		c.cityName = "凉山";
		c.firstLetter = "LS";
		c.pinyin = "LIANGSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 辽阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "111000";
		c.cityName = "辽阳";
		c.firstLetter = "LY";
		c.pinyin = "LIAOYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 辽源-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "136200";
		c.cityName = "辽源";
		c.firstLetter = "LY";
		c.pinyin = "LIAOYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 聊城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "252000";
		c.cityName = "聊城";
		c.firstLetter = "LC";
		c.pinyin = "LIAOCHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 林芝地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "860000";
		c.cityName = "林芝地区";
		c.firstLetter = "LZDQ";
		c.pinyin = "LINZHI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 临沧-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "675800";
		c.cityName = "临沧";
		c.firstLetter = "LC";
		c.pinyin = "LINCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 临汾-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "31500";
		c.cityName = "临汾";
		c.firstLetter = "LF";
		c.pinyin = "LINFEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 临高县-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571800";
		c.cityName = "临高县";
		c.firstLetter = "LGX";
		c.pinyin = "LINGAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 临夏州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "731100";
		c.cityName = "临夏州";
		c.firstLetter = "LXZ";
		c.pinyin = "LINXIAZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 临沂-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "273300";
		c.cityName = "临沂";
		c.firstLetter = "LY";
		c.pinyin = "LINYI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 陵水-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572400";
		c.cityName = "陵水";
		c.firstLetter = "LS";
		c.pinyin = "LINGSHUI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 柳州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "545000";
		c.cityName = "柳州";
		c.firstLetter = "LZ";
		c.pinyin = "LIUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 六安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "231300";
		c.cityName = "六安";
		c.firstLetter = "LA";
		c.pinyin = "LIUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 六盘水-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "553000";
		c.cityName = "六盘水";
		c.firstLetter = "LPS";
		c.pinyin = "LIUPANSHUI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 龙岩-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "362302";
		c.cityName = "龙岩";
		c.firstLetter = "LY";
		c.pinyin = "LONGYAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 陇南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "742100";
		c.cityName = "陇南";
		c.firstLetter = "LN";
		c.pinyin = "LONGNAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 娄底-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "417000";
		c.cityName = "娄底";
		c.firstLetter = "LD";
		c.pinyin = "LOUDI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 泸州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "646000";
		c.cityName = "泸州";
		c.firstLetter = "LZ";
		c.pinyin = "LUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 洛阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "471000";
		c.cityName = "洛阳";
		c.firstLetter = "LY";
		c.pinyin = "LUOYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 漯河-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "462000";
		c.cityName = "漯河";
		c.firstLetter = "LH";
		c.pinyin = "LUOHE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 吕梁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "30500";
		c.cityName = "吕梁";
		c.firstLetter = "LL";
		c.pinyin = "LVLIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 马鞍山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "243000";
		c.cityName = "马鞍山";
		c.firstLetter = "MAS";
		c.pinyin = "MAANSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 茂名-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "525000";
		c.cityName = "茂名";
		c.firstLetter = "MM";
		c.pinyin = "MAOMING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 眉山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "614200";
		c.cityName = "眉山";
		c.firstLetter = "MS";
		c.pinyin = "MEISHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 梅州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "514000";
		c.cityName = "梅州";
		c.firstLetter = "MZ";
		c.pinyin = "MEIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 绵阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "621000";
		c.cityName = "绵阳";
		c.firstLetter = "MY";
		c.pinyin = "MIANYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 牡丹江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "157000";
		c.cityName = "牡丹江";
		c.firstLetter = "MDJ";
		c.pinyin = "MUDANJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 那曲-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "852000";
		c.cityName = "那曲";
		c.firstLetter = "NQ";
		c.pinyin = "NAQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南昌-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "330000";
		c.cityName = "南昌";
		c.firstLetter = "NC";
		c.pinyin = "NANCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南充-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "637000";
		c.cityName = "南充";
		c.firstLetter = "NC";
		c.pinyin = "NANCHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南京-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "210000";
		c.cityName = "南京";
		c.firstLetter = "NJ";
		c.pinyin = "NANJING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "530000";
		c.cityName = "南宁";
		c.firstLetter = "NN";
		c.pinyin = "NANNING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南平-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "353000";
		c.cityName = "南平";
		c.firstLetter = "NP";
		c.pinyin = "NANPING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南通-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "226000";
		c.cityName = "南通";
		c.firstLetter = "NT";
		c.pinyin = "NANTONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 南阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "454500";
		c.cityName = "南阳";
		c.firstLetter = "NY";
		c.pinyin = "NANYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 内江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "641100";
		c.cityName = "内江";
		c.firstLetter = "NJ";
		c.pinyin = "NEIJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宁波-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "315000";
		c.cityName = "宁波";
		c.firstLetter = "NB";
		c.pinyin = "NINGBO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宁德-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "352000";
		c.cityName = "宁德";
		c.firstLetter = "ND";
		c.pinyin = "NINGDE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 怒江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "671401";
		c.cityName = "怒江";
		c.firstLetter = "NJ";
		c.pinyin = "NUJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 攀枝花-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "617000";
		c.cityName = "攀枝花";
		c.firstLetter = "PZH";
		c.pinyin = "PANZHIHUA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 盘锦-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "124000";
		c.cityName = "盘锦";
		c.firstLetter = "PJ";
		c.pinyin = "PANJIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 平顶山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "467000";
		c.cityName = "平顶山";
		c.firstLetter = "PDS";
		c.pinyin = "PINGDINGSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 平凉-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "743400";
		c.cityName = "平凉";
		c.firstLetter = "PL";
		c.pinyin = "PINGLIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 萍乡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "337000";
		c.cityName = "萍乡";
		c.firstLetter = "PX";
		c.pinyin = "PINGXIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 莆田-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "351100";
		c.cityName = "莆田";
		c.firstLetter = "PT";
		c.pinyin = "PUTIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 濮阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "457000";
		c.cityName = "濮阳";
		c.firstLetter = "PY";
		c.pinyin = "PUYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 七台河-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "154500";
		c.cityName = "七台河";
		c.firstLetter = "QTH";
		c.pinyin = "QITAIHE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 齐齐哈尔-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "161000";
		c.cityName = "齐齐哈尔";
		c.firstLetter = "QQHE";
		c.pinyin = "QIQIHAER";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 潜江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "433100";
		c.cityName = "潜江";
		c.firstLetter = "QJ";
		c.pinyin = "QIANJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黔东南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "556100";
		c.cityName = "黔东南";
		c.firstLetter = "QDN";
		c.pinyin = "QIANDONGNAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黔南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "550100";
		c.cityName = "黔南";
		c.firstLetter = "QN";
		c.pinyin = "QIANNAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 黔西南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "522326";
		c.cityName = "黔西南";
		c.firstLetter = "QXN";
		c.pinyin = "QIANXINAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 钦州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "535000";
		c.cityName = "钦州";
		c.firstLetter = "QZ";
		c.pinyin = "QINZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 秦皇岛-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "66000";
		c.cityName = "秦皇岛";
		c.firstLetter = "QHD";
		c.pinyin = "QINGHUANGDAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 青岛-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "266000";
		c.cityName = "青岛";
		c.firstLetter = "QD";
		c.pinyin = "QINGDAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 清远-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "511500";
		c.cityName = "清远";
		c.firstLetter = "QY";
		c.pinyin = "QINGYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 庆阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "744500";
		c.cityName = "庆阳";
		c.firstLetter = "QY";
		c.pinyin = "QINGYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 琼海-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571400";
		c.cityName = "琼海";
		c.firstLetter = "QH";
		c.pinyin = "QIONGHAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 琼中-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572900";
		c.cityName = "琼中";
		c.firstLetter = "QZ";
		c.pinyin = "QIONGZHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 衢州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "324000";
		c.cityName = "衢州";
		c.firstLetter = "QZ";
		c.pinyin = "QUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 曲靖-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "654200";
		c.cityName = "曲靖";
		c.firstLetter = "QJ";
		c.pinyin = "QUJING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 泉州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "362000";
		c.cityName = "泉州";
		c.firstLetter = "QZ";
		c.pinyin = "QUANZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 日喀则地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "857100";
		c.cityName = "日喀则地区";
		c.firstLetter = "RKZDQ";
		c.pinyin = "RIKAZEDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 日照-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "262300";
		c.cityName = "日照";
		c.firstLetter = "RZ";
		c.pinyin = "RIZHAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 三门峡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "472000";
		c.cityName = "三门峡";
		c.firstLetter = "SMX";
		c.pinyin = "SANMENXIA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 三明-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "353300";
		c.cityName = "三明";
		c.firstLetter = "SM";
		c.pinyin = "SANMING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 三亚-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572000";
		c.cityName = "三亚";
		c.firstLetter = "SY";
		c.pinyin = "SANYA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 厦门-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "360000";
		c.cityName = "厦门";
		c.firstLetter = "XM";
		c.pinyin = "XIAMEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 山南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "850700";
		c.cityName = "山南";
		c.firstLetter = "SN";
		c.pinyin = "SHANNAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 汕头-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "515000";
		c.cityName = "汕头";
		c.firstLetter = "ST";
		c.pinyin = "SHANTOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 汕尾-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "516400";
		c.cityName = "汕尾";
		c.firstLetter = "SW";
		c.pinyin = "SHANWEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 商洛-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "711400";
		c.cityName = "商洛";
		c.firstLetter = "SL";
		c.pinyin = "SHANGLUO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 商丘-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "476000";
		c.cityName = "商丘";
		c.firstLetter = "SQ";
		c.pinyin = "SHANGQIU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 上饶-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "333100";
		c.cityName = "上饶";
		c.firstLetter = "SR";
		c.pinyin = "SHANGRAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 韶关-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "511100";
		c.cityName = "韶关";
		c.firstLetter = "SG";
		c.pinyin = "SHAOGUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 邵阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "422000";
		c.cityName = "邵阳";
		c.firstLetter = "SY";
		c.pinyin = "SHAOYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 绍兴-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "312000";
		c.cityName = "绍兴";
		c.firstLetter = "SX";
		c.pinyin = "SHAOXING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 深圳-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "518000";
		c.cityName = "深圳";
		c.firstLetter = "SZ";
		c.pinyin = "SHENZHEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 神农架林区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "442400";
		c.cityName = "神农架林区";
		c.firstLetter = "SNJLQ";
		c.pinyin = "SHENNONGJIA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 沈阳-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "110000";
		c.cityName = "沈阳";
		c.firstLetter = "SY";
		c.pinyin = "SHENYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 十堰-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "442000";
		c.cityName = "十堰";
		c.firstLetter = "SY";
		c.pinyin = "SHIYAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 石河子-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "832000";
		c.cityName = "石河子";
		c.firstLetter = "SHZ";
		c.pinyin = "SHIHEZI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 石家庄-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "50000";
		c.cityName = "石家庄";
		c.firstLetter = "SJZ";
		c.pinyin = "SHIJIAZHUANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 石嘴山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "753000";
		c.cityName = "石嘴山";
		c.firstLetter = "SZS";
		c.pinyin = "SHIZUISHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 双鸭山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "155100";
		c.cityName = "双鸭山";
		c.firstLetter = "SYS";
		c.pinyin = "SHUANGYASHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 朔州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "36000";
		c.cityName = "朔州";
		c.firstLetter = "SZ";
		c.pinyin = "SHUOZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 思茅-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "665000";
		c.cityName = "思茅";
		c.firstLetter = "SM";
		c.pinyin = "SIMAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 四平-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "130701";
		c.cityName = "四平";
		c.firstLetter = "SP";
		c.pinyin = "SIPING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 松原-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "131100";
		c.cityName = "松原";
		c.firstLetter = "SY";
		c.pinyin = "SONGYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 苏州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "215000";
		c.cityName = "苏州";
		c.firstLetter = "SZ";
		c.pinyin = "SUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 绥化-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "151442";
		c.cityName = "绥化";
		c.firstLetter = "SH";
		c.pinyin = "SUIHUA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 随州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "441300";
		c.cityName = "随州";
		c.firstLetter = "SZ";
		c.pinyin = "SUIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 遂宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "629000";
		c.cityName = "遂宁";
		c.firstLetter = "SN";
		c.pinyin = "SUINING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 塔城地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "834300";
		c.cityName = "塔城地区";
		c.firstLetter = "TCDQ";
		c.pinyin = "TACHENGDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 台州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "317100";
		c.cityName = "台州";
		c.firstLetter = "TZ";
		c.pinyin = "TAIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 太原-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "30000";
		c.cityName = "太原";
		c.firstLetter = "TY";
		c.pinyin = "TAIYUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 泰安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "271000";
		c.cityName = "泰安";
		c.firstLetter = "TA";
		c.pinyin = "TAIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 泰州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "225300";
		c.cityName = "泰州";
		c.firstLetter = "TZ";
		c.pinyin = "TAIZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 唐山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "63000";
		c.cityName = "唐山";
		c.firstLetter = "TS";
		c.pinyin = "TANGSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 天津-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "300000";
		c.cityName = "天津";
		c.firstLetter = "TJ";
		c.pinyin = "TIANJIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 天门-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "431700";
		c.cityName = "天门";
		c.firstLetter = "TM";
		c.pinyin = "TIANMEN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 天水-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "741000";
		c.cityName = "天水";
		c.firstLetter = "TS";
		c.pinyin = "TIANSHUI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 铁岭-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "112000";
		c.cityName = "铁岭";
		c.firstLetter = "TL";
		c.pinyin = "TIELING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 通化-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "134000";
		c.cityName = "通化";
		c.firstLetter = "TH";
		c.pinyin = "TONGHUA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 通辽-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "28000";
		c.cityName = "通辽";
		c.firstLetter = "TL";
		c.pinyin = "TONGLIAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 铜川-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "727000";
		c.cityName = "铜川";
		c.firstLetter = "TC";
		c.pinyin = "TONGCHUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 铜陵-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "244000";
		c.cityName = "铜陵";
		c.firstLetter = "TL";
		c.pinyin = "TONGLING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 铜仁地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "554000";
		c.cityName = "铜仁地区";
		c.firstLetter = "TRDQ";
		c.pinyin = "TONGRENDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 图木舒克-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "844000";
		c.cityName = "图木舒克";
		c.firstLetter = "TMSK";
		c.pinyin = "TUMUSHUKE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 吐鲁番地区-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "838100";
		c.cityName = "吐鲁番地区";
		c.firstLetter = "TLFDQ";
		c.pinyin = "TULUFANDIQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 屯昌县-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571600";
		c.cityName = "屯昌县";
		c.firstLetter = "TCX";
		c.pinyin = "TUNCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 万宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571500";
		c.cityName = "万宁";
		c.firstLetter = "WN";
		c.pinyin = "WANNING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 威海-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "264200";
		c.cityName = "威海";
		c.firstLetter = "WH";
		c.pinyin = "WEIHAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 潍坊-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "261000";
		c.cityName = "潍坊";
		c.firstLetter = "WF";
		c.pinyin = "WEIFANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 渭南-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "711700";
		c.cityName = "渭南";
		c.firstLetter = "WN";
		c.pinyin = "WEINAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 温州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "325000";
		c.cityName = "温州";
		c.firstLetter = "WZ";
		c.pinyin = "WENZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 文昌-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "571300";
		c.cityName = "文昌";
		c.firstLetter = "WC";
		c.pinyin = "WENCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 文山州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "663000";
		c.cityName = "文山州";
		c.firstLetter = "WSZ";
		c.pinyin = "WENSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 乌海-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "16000";
		c.cityName = "乌海";
		c.firstLetter = "WH";
		c.pinyin = "WUHAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 乌兰察布-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "11026";
		c.cityName = "乌兰察布";
		c.firstLetter = "WLCB";
		c.pinyin = "WULANCHABU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 乌鲁木齐-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "830000";
		c.cityName = "乌鲁木齐";
		c.firstLetter = "WLMQ";
		c.pinyin = "WULUMUQI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 无锡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "214000";
		c.cityName = "无锡";
		c.firstLetter = "WX";
		c.pinyin = "WUXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 芜湖-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "241000";
		c.cityName = "芜湖";
		c.firstLetter = "WH";
		c.pinyin = "WUHU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 吴忠-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "751100";
		c.cityName = "吴忠";
		c.firstLetter = "WZ";
		c.pinyin = "WUZHONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 梧州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "543000";
		c.cityName = "梧州";
		c.firstLetter = "WZ";
		c.pinyin = "WUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 五家渠-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "831300";
		c.cityName = "五家渠";
		c.firstLetter = "WJQ";
		c.pinyin = "WUJIAQU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 五指山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "572200";
		c.cityName = "五指山";
		c.firstLetter = "WZS";
		c.pinyin = "WUZHISHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 武汉-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "430000";
		c.cityName = "武汉";
		c.firstLetter = "WH";
		c.pinyin = "WUHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 武威-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "733000";
		c.cityName = "武威";
		c.firstLetter = "WW";
		c.pinyin = "WUWEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 西安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "710000";
		c.cityName = "西安";
		c.firstLetter = "XA";
		c.pinyin = "XIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 西宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "810000";
		c.cityName = "西宁";
		c.firstLetter = "XN";
		c.pinyin = "XINING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 西双版纳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "666200";
		c.cityName = "西双版纳";
		c.firstLetter = "XSBN";
		c.pinyin = "XISHUANGBANNA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 锡林郭勒-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "11200";
		c.cityName = "锡林郭勒";
		c.firstLetter = "XLGL";
		c.pinyin = "XILINGUOLE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 仙桃-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "433000";
		c.cityName = "仙桃";
		c.firstLetter = "XT";
		c.pinyin = "XIANTAO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 咸宁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "437000";
		c.cityName = "咸宁";
		c.firstLetter = "XN";
		c.pinyin = "XIANNING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 咸阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "711200";
		c.cityName = "咸阳";
		c.firstLetter = "XY";
		c.pinyin = "XIANYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 湘潭-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "411100";
		c.cityName = "湘潭";
		c.firstLetter = "XT";
		c.pinyin = "XIANGTAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 湘西-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "416100";
		c.cityName = "湘西";
		c.firstLetter = "XX";
		c.pinyin = "XIANGXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 襄阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "441100";
		c.cityName = "襄阳";
		c.firstLetter = "XY";
		c.pinyin = "XIANGYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 孝感-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "432000";
		c.cityName = "孝感";
		c.firstLetter = "XG";
		c.pinyin = "XIAOGAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 忻州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "34000";
		c.cityName = "忻州";
		c.firstLetter = "XZ";
		c.pinyin = "XINZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 新乡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "453000";
		c.cityName = "新乡";
		c.firstLetter = "XX";
		c.pinyin = "XINXIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 新余-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "336600";
		c.cityName = "新余";
		c.firstLetter = "XY";
		c.pinyin = "XINYU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 信阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "464000";
		c.cityName = "信阳";
		c.firstLetter = "XY";
		c.pinyin = "XINYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 邢台-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "54000";
		c.cityName = "邢台";
		c.firstLetter = "XT";
		c.pinyin = "XINGTAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 兴安盟-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "29400";
		c.cityName = "兴安盟";
		c.firstLetter = "XAM";
		c.pinyin = "XINGAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宿迁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "223600";
		c.cityName = "宿迁";
		c.firstLetter = "SQ";
		c.pinyin = "SUQIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宿州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "234000";
		c.cityName = "宿州";
		c.firstLetter = "SZ";
		c.pinyin = "SUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 徐州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "221000";
		c.cityName = "徐州";
		c.firstLetter = "XZ";
		c.pinyin = "XUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 许昌-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "461000";
		c.cityName = "许昌";
		c.firstLetter = "XC";
		c.pinyin = "XUCHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宣城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "242000";
		c.cityName = "宣城";
		c.firstLetter = "XC";
		c.pinyin = "XUANCHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 雅安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "625000";
		c.cityName = "雅安";
		c.firstLetter = "YA";
		c.pinyin = "YAAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 烟台-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "264000";
		c.cityName = "烟台";
		c.firstLetter = "YT";
		c.pinyin = "YANTAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 延安-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "716000";
		c.cityName = "延安";
		c.firstLetter = "YA";
		c.pinyin = "YANAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 延边-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "133200";
		c.cityName = "延边";
		c.firstLetter = "YB";
		c.pinyin = "YANBIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 盐城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "224000";
		c.cityName = "盐城";
		c.firstLetter = "YC";
		c.pinyin = "YANCHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 扬州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "225000";
		c.cityName = "扬州";
		c.firstLetter = "YZ";
		c.pinyin = "YANGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阳江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "529500";
		c.cityName = "阳江";
		c.firstLetter = "YJ";
		c.pinyin = "YANGJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 阳泉-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "45000";
		c.cityName = "阳泉";
		c.firstLetter = "YQ";
		c.pinyin = "YANGQUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 伊春-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "153000";
		c.cityName = "伊春";
		c.firstLetter = "YC";
		c.pinyin = "YICHUN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 伊犁-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "835100";
		c.cityName = "伊犁";
		c.firstLetter = "YL";
		c.pinyin = "YILI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宜宾-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "644000";
		c.cityName = "宜宾";
		c.firstLetter = "YB";
		c.pinyin = "YIBIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宜昌-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "443000";
		c.cityName = "宜昌";
		c.firstLetter = "YC";
		c.pinyin = "YICHANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 宜春-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "330600";
		c.cityName = "宜春";
		c.firstLetter = "YC";
		c.pinyin = "YICHUN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 益阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "413000";
		c.cityName = "益阳";
		c.firstLetter = "YY";
		c.pinyin = "YIYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 银川-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "750000";
		c.cityName = "银川";
		c.firstLetter = "YC";
		c.pinyin = "YINCHUAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 鹰潭-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "335000";
		c.cityName = "鹰潭";
		c.firstLetter = "YT";
		c.pinyin = "YINGTAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 营口-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "115000";
		c.cityName = "营口";
		c.firstLetter = "YK";
		c.pinyin = "YINGKOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 永州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "425000";
		c.cityName = "永州";
		c.firstLetter = "YZ";
		c.pinyin = "YONGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 榆林-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "718000";
		c.cityName = "榆林";
		c.firstLetter = "YL";
		c.pinyin = "YULIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 玉林-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "537000";
		c.cityName = "玉林";
		c.firstLetter = "YL";
		c.pinyin = "YULIN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 玉树-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "815000";
		c.cityName = "玉树";
		c.firstLetter = "YS";
		c.pinyin = "YUSHU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 玉溪-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "651100";
		c.cityName = "玉溪";
		c.firstLetter = "YX";
		c.pinyin = "YUXI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 岳阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "414000";
		c.cityName = "岳阳";
		c.firstLetter = "YY";
		c.pinyin = "YUEYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 云浮-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "527300";
		c.cityName = "云浮";
		c.firstLetter = "YF";
		c.pinyin = "YUNFU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 运城-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "43100";
		c.cityName = "运城";
		c.firstLetter = "YC";
		c.pinyin = "YUNCHENG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 枣庄-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "277000";
		c.cityName = "枣庄";
		c.firstLetter = "ZZ";
		c.pinyin = "ZAOZHUANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 湛江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "524000";
		c.cityName = "湛江";
		c.firstLetter = "ZJ";
		c.pinyin = "ZHANJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 张家界-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "427000";
		c.cityName = "张家界";
		c.firstLetter = "ZJJ";
		c.pinyin = "ZHANGJIAJIE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 张家口-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "75000";
		c.cityName = "张家口";
		c.firstLetter = "ZJK";
		c.pinyin = "ZHANGJIAKOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 张掖-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "733014";
		c.cityName = "张掖";
		c.firstLetter = "ZY";
		c.pinyin = "ZHANGYE";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 漳州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "363000";
		c.cityName = "漳州";
		c.firstLetter = "ZZ";
		c.pinyin = "ZHANGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 长春-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "130000";
		c.cityName = "长春";
		c.firstLetter = "CC";
		c.pinyin = "CHANGCHUN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 长沙-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "410000";
		c.cityName = "长沙";
		c.firstLetter = "CS";
		c.pinyin = "CHANGSHA";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 长治-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "46000";
		c.cityName = "长治";
		c.firstLetter = "CZ";
		c.pinyin = "CHANGZHI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 昭通-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "654600";
		c.cityName = "昭通";
		c.firstLetter = "ZT";
		c.pinyin = "ZHAOTONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 肇庆-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "526000";
		c.cityName = "肇庆";
		c.firstLetter = "ZQ";
		c.pinyin = "ZHAOQING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 镇江-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "212000";
		c.cityName = "镇江";
		c.firstLetter = "ZJ";
		c.pinyin = "ZHENGJIANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 郑州-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "450000";
		c.cityName = "郑州";
		c.firstLetter = "ZZ";
		c.pinyin = "ZHENGZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 中山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "528400";
		c.cityName = "中山";
		c.firstLetter = "ZS";
		c.pinyin = "ZHONGSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 中卫-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "755000";
		c.cityName = "中卫";
		c.firstLetter = "ZW";
		c.pinyin = "ZHONGWEI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 重庆-----
		c = new CityData();
		c.hotTag = true;
		c.cityId = "400000";
		c.cityName = "重庆";
		c.firstLetter = "CQ";
		c.pinyin = "CHONGQING";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 舟山-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "202450";
		c.cityName = "舟山";
		c.firstLetter = "ZS";
		c.pinyin = "ZHOUSHAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 周口-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "461400";
		c.cityName = "周口";
		c.firstLetter = "ZK";
		c.pinyin = "ZHOUKOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 珠海-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "519000";
		c.cityName = "珠海";
		c.firstLetter = "ZH";
		c.pinyin = "ZHUHAI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 株洲-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "412000";
		c.cityName = "株洲";
		c.firstLetter = "ZZ";
		c.pinyin = "ZHUZHOU";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 驻马店-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "463000";
		c.cityName = "驻马店";
		c.firstLetter = "ZMD";
		c.pinyin = "ZHUMADIAN";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 资阳-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "641300";
		c.cityName = "资阳";
		c.firstLetter = "ZY";
		c.pinyin = "ZIYANG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 淄博-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "255000";
		c.cityName = "淄博";
		c.firstLetter = "ZB";
		c.pinyin = "ZIBO";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 自贡-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "643000";
		c.cityName = "自贡";
		c.firstLetter = "ZG";
		c.pinyin = "ZIGONG";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// 遵义-----
		c = new CityData();
		c.hotTag = false;
		c.cityId = "463127";
		c.cityName = "遵义";
		c.firstLetter = "ZY";
		c.pinyin = "ZUNYI";

		c.mainMenuInfo = kCityDefaultMenuList;
		c.phone = kCityDefaultPhone;
		c.showTag = true;
		cityListDTO.getList().add(c);

		// SharedprefUtil.save(context, Settings.CITY_LIST3 + "_" +
		// ActivityUtil.getVersionCode(context), JsonUtils.toJson(cityListDTO));
		setCityListDTO(context, cityListDTO, JsonUtils.toJson(cityListDTO));
		// for (int i=0; i < cityListDTO.getList().size(); i++) {
		// CityData city = cityListDTO.getList().get(i);
		// city.listLines = getLines(city.getPolygonInfo());
		// cityListDTO.getList().set(i, city);
		// }
	}

	/**
	 * 获得缓存的城市列表信息
	 * 
	 * @param context
	 * @return
	 */
	public CityListDTO getCityListDTO(Context context) {
		String jsonStr = "";
		try {
			if (cityListDTO != null) {
				return cityListDTO;
			}
			ValueObject vo = ValueCacheUtil.getInstance(context).get(Settings.CITY_LIST, "" + ActivityUtil.getVersionCode(context));
			// jsonStr = SharedprefUtil.get(context, Settings.CITY_LIST3 + "_" +
			// ActivityUtil.getVersionCode(context), "{}");
			if (vo == null) {
				initCityData(context);
				return cityListDTO;
			} else {
				CityListDTO dto = JsonUtils.fromJson(vo.getValue(), CityListDTO.class);
				if (dto==null || dto.getList()==null || dto.getList().size() == 0) {
					initCityData(context);
					return cityListDTO;
					// jsonStr = SharedprefUtil.get(context, Settings.CITY_LIST3
					// + "_" + ActivityUtil.getVersionCode(context), "{}");
					// dto = CityList3DTO.toBean(new JSONObject(jsonStr));
				} else {
					cityListDTO = dto;
					return cityListDTO;
				}
			}
			// jsonStr = SharedprefUtil.get(context, Settings.CITY_LIST3 + "_" +
			// ActivityUtil.getVersionCode(context), "{}");
			// CityList2DTO dto = JsonUtils.fromJson(jsonStr,
			// CityList2DTO.class);

			// cityListDTO = dto;
			// if (cityListDTO == null) {
			// cityListDTO = new CityList3DTO();
			// }
			// cityListDTO.getList().clear();
			// cityListDTO.setTimestamp(dto.getTimestamp());
			// for (CityData cityData : dto.getList()) {
			// if (cityData.showTag) {
			// try {
			// // cityData.listLines =
			// // getLines(cityData.getPolygonInfo());
			// cityListDTO.getList().add(cityData);
			// } catch (Exception e) {
			// Log.e("getCityList2DTO", cityData.cityId + "," +
			// cityData.cityName);
			// }
			//
			// }
			// }
		} catch (Exception e) {
			ActivityUtil.saveException(e, jsonStr);
			return null;
		}
	}

	/**
	 * 设置缓存的城市列表信息
	 * 
	 * @param context
	 * @param dto
	 */
	public void setCityListDTO(final Context context, CityListDTO dto, final String json) {

		cityListDTO = dto;
		ValueCacheUtil.getInstance(context).remove(Settings.CITY_LIST, "" + ActivityUtil.getVersionCode(context));
		ValueCacheUtil.getInstance(context).add(Settings.CITY_LIST, "" + ActivityUtil.getVersionCode(context), json);

	}

	/**
	 * 获得热门城市列表
	 * 
	 * @param context
	 * @return
	 */
	public CityListDTO getHotCityListDTO(Context context) {
		try {
			if (hotCityListDTO != null) {
				return hotCityListDTO;
			}
			if (cityListDTO == null) {
				cityListDTO = getCityListDTO(context);
			}
			if (hotCityListDTO == null) {
				hotCityListDTO = new CityListDTO();
				for (CityData cityData : cityListDTO.getList()) {
					if (cityData.isHotTag()) {
						hotCityListDTO.getList().add(cityData);
					}
				}
			}
			return hotCityListDTO;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据坐标获取城市
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public CityInfo getCity(Context context, double x, double y) {
		try {
			if (cityListDTO == null) {
				cityListDTO = getCityListDTO(context);
			}
			JsonPack jp = A57HttpApiV3.getInstance().getCityIdByGps(x, y);
			if (jp.getRe() == 200) {
				SimpleData data = JsonUtils.fromJson(jp.getObj().toString(), SimpleData.class);
				if (CheckUtil.isEmpty(data.getUuid()) || data.getUuid().equals("-1")) {
					return null;
				}
				for (int i = 0; i < cityListDTO.getList().size(); i++) {
					CityData city = cityListDTO.getList().get(i);
					if (data.getUuid().equals(city.getCityId())) {
						CityInfo info = new CityInfo();
						info.setId(city.getCityId());
						info.setName(city.getCityName());
						info.setPhone(city.getPhone());
						info.setTimestamp(System.currentTimeMillis());
						return info;
					}
				}
				return null;
			} else {
				return null;
			}
			// for (int i = 0; i < cityListDTO.getList().size(); i++) {
			// CityData city = cityListDTO.getList().get(i);
			// String str = city.getPolygonInfo();
			// // if (!CheckUtil.isEmpty(str) &&
			// // isInPoly(city.getPolygonInfo(), x, y)) {
			// if (!CheckUtil.isEmpty(str) && isInPoly(city, i, x, y)) {
			// CityInfo info = new CityInfo();
			// info.setId(city.getCityId());
			// info.setName(city.getCityName());
			// info.setPhone(city.getPhone());
			// info.setTimestamp(System.currentTimeMillis());
			// return info;
			// }
			// }
			// return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据城市Id在CityList2DTO里获取CityData
	 * 
	 * @param cityId
	 * @return
	 */
	public CityData getCityDataFromCityList2DTO(String cityId) {
		if (cityListDTO == null || cityListDTO.getList().size() == 0) {
			return null;
		}
		for (CityData city : cityListDTO.getList()) {
			if (city.getCityId().equals(cityId)) {
				return city;
			}
		}
		return null;
	}

	/**
	 * 启动更新GPS城市的线程
	 * 
	 * @param activity
	 * @return
	 */
	public Thread updateGpsCity(final Activity activity) {
		Thread thread = null;
		try {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					Bundle bundle = msg.getData();
					if (bundle != null) {
						double longitude = bundle.getDouble("longitude");
						double latitude = bundle.getDouble("latitude");
						// DialogUtil.showToast(activity, "Update GPS City" +
						// longitude + "," + latitude);
						final CityInfo cityInfoByGPS = (CityInfo) bundle.getSerializable("city");
						// final CityInfo cityInfoByGPS =
						// SessionManager.getInstance().getCity(activity,
						// longitude, latitude);
						// final CityInfo cityInfoByGPS = cityInfoByPoly;
						final CityInfo gpsCityFromCache = SessionManager.getInstance().getGpsCity(activity);
						if (cityInfoByGPS == null) {
							return;
						}
						// 当GPS定位城市与之前的GPS定位城市不同时
						if (gpsCityFromCache == null || !cityInfoByGPS.getId().equals(gpsCityFromCache.getId())) {
							if (gpsCityFromCache == null || CheckUtil.isEmpty(gpsCityFromCache.getId())) {
								// 如果之前未缓存过GPS城市，且用户当前选择的城市与GPS城市一样，则不提示，直接设置GPS城市
								if (cityInfoByGPS.getId().equals(SessionManager.getInstance().getCityInfo(activity).getId())) {
									SessionManager.getInstance().setGpsCity(activity, cityInfoByGPS);
									return;
								}
							}
							String alertMsg = DialogUtil.fullMsg(activity.getString(R.string.text_dialog_gps_localed), cityInfoByGPS.getName());
							DialogUtil.showAlert(activity, true, alertMsg, new DialogInterface.OnClickListener() {// 消息提示框确定按钮
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// 若选择确定，则把GPS城市设为当前选择城市
											SessionManager.getInstance().setCityInfo(activity, cityInfoByGPS);
											if (activity instanceof IndexActivity) {
												CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
											} else {
												ActivityUtil.jump(activity, IndexActivity.class, 0, new Bundle(), true);
												activity.finish();
											}
										}
									}, new DialogInterface.OnClickListener() {// 消息提示框取消按钮

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									});
						}
						// GPS城市变化时每次更新缓存中的GPS城市
						SessionManager.getInstance().setGpsCity(activity, cityInfoByGPS);
					}
				}
			};

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					if (Loc.isGpsAvailable()) {
						double longitude = 0;
						double latitude = 0;
						LocInfo myLoc = Loc.getLoc();

						// 重试10次
						if (myLoc == null || myLoc.getLoc() == null) {
							for (int i = 0; i < 10; i++) {
								SystemClock.sleep(1000);
								myLoc = Loc.getLoc();
								if (myLoc != null && myLoc.getLoc() != null) {
									break;
								}
							}
						}

						if (myLoc == null || myLoc.getLoc() == null) {
							return;
						} else {
							longitude = myLoc.getLoc().getLongitude();
							latitude = myLoc.getLoc().getLatitude();
						}
						// Log.e("updateGpsCity", longitudeLast + "," +
						// latitudeLast);
						// Log.e("updateGpsCity", longitude + "," + latitude);
						DecimalFormat decimalFormat = new DecimalFormat("###.##");
						if (decimalFormat.format(longitude).equals(decimalFormat.format(longitudeLast)) && decimalFormat.format(latitude).equals(decimalFormat.format(latitudeLast))) {
							// 坐标与上次缓存的坐标相同，不做任何处理
							// Log.e("updateGpsCity", "do nothing");
						} else {
							Bundle bundle = new Bundle();
							bundle.putDouble("longitude", longitude);
							bundle.putDouble("latitude", latitude);
							CityInfo cityInfoByGPS = SessionManager.getInstance().getCity(activity, longitude, latitude);
							bundle.putSerializable("city", cityInfoByGPS);
							Message msg = new Message();
							msg.setData(bundle);
							handler.sendMessage(msg);
							// Log.e("updateGpsCity", "getCity");
						}
						longitudeLast = longitude;
						latitudeLast = latitude;
					}
				}

			};
			thread = new Thread(runnable);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return thread;
	}

	/**
	 * 判断点是否在多边形中
	 * 
	 * @param strLines
	 * @param x
	 * @param y
	 * @return
	 */
	// private boolean isInPoly(CityData cityData, int index, double x, double
	// y) {
	// try {
	//
	// // long l1 = System.currentTimeMillis();
	//
	// Line objLine = new Line();
	// objLine.X1 = x;
	// objLine.Y1 = y;
	// objLine.X2 = x + 180;
	// objLine.Y2 = y;
	//
	// if (CheckUtil.isEmpty(cityData.getPolygonInfo())) {
	// return false;
	// }
	// List<Line> lstLine = cityData.listLines;
	// if (lstLine == null || lstLine.size() == 0) {
	// lstLine = getLines(cityData.getPolygonInfo());
	// cityData.listLines = lstLine;
	// cityListDTO.getList().set(index, cityData);
	// }
	//
	// int nCrossCount = 0;
	// for (Line objPolyLine : lstLine) {
	// if (doLinesIntersect(objLine, objPolyLine)) {
	// nCrossCount++;
	// LogUtils.logD("", "P1:" + objPolyLine.X1 + "," + objPolyLine.Y1 + "  P2:"
	// + objPolyLine.X2 + "," + objPolyLine.Y2);
	// }
	// }
	//
	// if (nCrossCount / 2 * 2 == nCrossCount) {
	//
	// // long l2 = System.currentTimeMillis();
	// // Log.e("isInPoly", "" + (l2 - l1));
	//
	// return false;
	// } else {
	//
	// // long l2 = System.currentTimeMillis();
	// // Log.e("isInPoly", "" + (l2 - l1));
	//
	// return true;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 判断两线是否相交
	// *
	// * @param L1
	// * @param L2
	// * @return
	// */
	// private boolean doLinesIntersect(Line L1, Line L2) {
	// try {
	// // Denominator for ua and ub are the same, so store this calculation
	// double d = (L2.Y2 - L2.Y1) * (L1.X2 - L1.X1) - (L2.X2 - L2.X1) * (L1.Y2 -
	// L1.Y1);
	//
	// // n_a and n_b are calculated as seperate values for readability
	// double n_a = (L2.X2 - L2.X1) * (L1.Y1 - L2.Y1) - (L2.Y2 - L2.Y1) * (L1.X1
	// - L2.X1);
	//
	// double n_b = (L1.X2 - L1.X1) * (L1.Y1 - L2.Y1) - (L1.Y2 - L1.Y1) * (L1.X1
	// - L2.X1);
	//
	// // Make sure there is not a division by zero - this also indicates
	// // that
	// // the lines are parallel.
	// // If n_a and n_b were both equal to zero the lines would be on top
	// // of each
	// // other (coincidental). This check is not done because it is not
	// // necessary for this implementation (the parallel check accounts
	// // for this).
	// if (d == 0)
	// return false;
	//
	// // Calculate the intermediate fractional point that the lines
	// // potentially intersect.
	// double ua = n_a / d;
	// double ub = n_b / d;
	//
	// // The fractional point will be between 0 and 1 inclusive if the
	// // lines
	// // intersect. If the fractional calculation is larger than 1 or
	// // smaller
	// // than 0 the lines would need to be longer to intersect.
	// if (ua >= 0d && ua <= 1d && ub >= 0d && ub <= 1d) {
	//
	// return true;
	// }
	// return false;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// /**
	// * 根据城市的多边形数据获取边线列表
	// *
	// * @param strLines
	// * @return
	// */
	// public List<Line> getLines(String strLines) {
	// List<Line> lstLines = new ArrayList<Line>();
	// try {
	// // Log.e("getLines", strLines);
	// long l1 = System.currentTimeMillis();
	//
	// if (CheckUtil.isEmpty(strLines)) {
	// return lstLines;
	// }
	// strLines = strLines.trim();
	// String[] sTmp = strLines.split(";");
	// int nIndex = 0;
	// for (String sLoc : sTmp) {
	// // Log.e("getLines", "sLoc:" + sLoc);
	// String[] aLoc = sLoc.split(":");
	// double x = Double.parseDouble(aLoc[0]);
	// double y = Double.parseDouble(aLoc[1]);
	// Line objLine = new Line();
	// objLine.X1 = x;
	// objLine.Y1 = y;
	// lstLines.add(objLine);
	// if (nIndex != 0) {
	// ((Line) lstLines.get(nIndex - 1)).X2 = x;
	// ((Line) lstLines.get(nIndex - 1)).Y2 = y;
	// }
	// nIndex++;
	// }
	// ((Line) lstLines.get(nIndex - 1)).X2 = ((Line) lstLines.get(0)).X1;
	// ((Line) lstLines.get(nIndex - 1)).Y2 = ((Line) lstLines.get(0)).Y1;
	// long l2 = System.currentTimeMillis();
	// // Log.e("getLines", "" + (l2 - l1));
	// return lstLines;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return lstLines;
	// }
	// }

	// public class Line {
	// public double X1, X2, Y1, Y2;
	// }

	public DishOrderDTO getDishOrder(Context context, String resId) {
		String jsonStr = SharedprefUtil.get(context, Settings.DISH_ORDER + "_" + resId, "{}");
		if (jsonStr.equals("{}")) {
			dishOrder = new DishOrderDTO();
		} else {
			dishOrder = JsonUtils.fromJson(jsonStr, DishOrderDTO.class);
		}
		return dishOrder;
	}

	public void setDishOrder(Context context, DishOrderDTO dishOrder, String resId) {
		if (!TextUtils.isEmpty(resId)) {
			dishOrder.setRestId(resId);
		}
		this.dishOrder = dishOrder;
		updateDishDataListByDishOrder();
		SharedprefUtil.save(context, Settings.DISH_ORDER + "_" + dishOrder.getRestId(), JsonUtils.toJson(dishOrder));
	}

	// 菜单列表缓存
	public void setDishListPackDTO(Context context, String json, DishListPackDTO dishListPackDTO) {
		this.dishListPackDTO = dishListPackDTO;
		SharedprefUtil.save(context, Settings.DISH_LIST, json);
	}

	public DishListPackDTO getDishListPackDTO(Context context) {

		// if (dishListPackDTO != null) {
		// // Log.e("", "not null");
		// return dishListPackDTO;
		// }
		// //
		// try {
		// long start = System.currentTimeMillis();
		// // Log.w("获取本地首选项开始------",""+(start));
		// String jsonStr = SharedprefUtil.get(context, Settings.DISH_LIST,
		// "{}");
		// // Log.w("获取本地首选项结束------",""+(System.currentTimeMillis()-start));
		// // Log.e("", jsonStr);
		// if (jsonStr.equals("{}")) {
		// DishListPackDTO dd = new DishListPackDTO();
		// //
		// Log.w("new DishListPackDTO()结束------",""+(System.currentTimeMillis()-start));
		// return dd;
		// } else {
		// // return JsonUtils.fromJson(jsonStr, DishListPackDTO.class);
		//
		// start = System.currentTimeMillis();
		// // Log.w("构造一个缓存对象开始------",""+(start));
		// JSONObject jo = new JSONObject(jsonStr);
		// // Log.w("构造一个JSON对象结束------",""+(System.currentTimeMillis()-start));
		// DishListPackDTO d = DishListPackDTO.toBean(jo);
		// // Log.w("构造一个缓存对象结束------",""+(System.currentTimeMillis()-start));
		// this.dishListPackDTO = d;
		// // Log.e("", "dishListPackDTO size " +
		// // dishListPackDTO.getList().size());
		// return d;
		//
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// return null;
		// }
		return null;
	}

	// 根据订单更新点菜菜单页面的数据
	public void updateDishDataListByDishOrder() {
		if (dishDataList.size() == 0) {
			return;
		}
		Map<String, DishData> map = dishOrder.getDishMap();
		for (int i = 0; i < dishDataList.size(); i++) {
			DishData data = dishDataList.get(i);
			if (map.containsKey(data.getUuid())) {
				DishData tmpData = map.get(data.getUuid());
				data.setNum(tmpData.getNum());
				data.setOldNum(tmpData.getOldNum());
				data.setSelectProcessTypeId(tmpData.getSelectProcessTypeId());
				data.setSelectProcessTypeName(tmpData.getSelectProcessTypeName());
				dishDataList.set(i, data);
			} else {
				data.setNum(0);
				data.setOldNum(0);
				data.setSelectProcessTypeId("");
				data.setSelectProcessTypeName("");
				dishDataList.set(i, data);
			}
		}
	}

	/**
	 * 添加一道菜到购物车中
	 * 
	 * @param uuid
	 *            uuid 餐厅的uuid作为购物车的key
	 * @param typeDTO
	 *            这道菜的类别
	 * @param takeoutData
	 *            一道菜
	 */

	public void addDToTakeoutCartCache(String uuid, TakeoutMenuData2 takeoutMenuData) {
		
		boolean isClearCurrentData=false;
		
		if (takeoutMenuData == null) {
			return;
		}
		ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
		// 通过uuid获得缓存里面的信息
		TakeoutMenuListPackDTO cartDto = getTakeoutCartCache(uuid);
		if (cartDto != null) {
			vc.remove("TAKEOUT_SHOPPING_CART", "TAKEOUT_SHOPPING_CART_" + uuid);
			List<TakeoutMenuListDTO> takeoutMenuListDTOList = cartDto.list;
			List<TakeoutMenuData2> takeoutMenuDataList = null;
			for (int i = 0; i < takeoutMenuListDTOList.size(); i++) {
				TakeoutMenuListDTO takeoutMenuListDTO = takeoutMenuListDTOList.get(i);
				takeoutMenuDataList = takeoutMenuListDTO.list;
				for (int j = 0; j < takeoutMenuDataList.size(); j++) {
					TakeoutMenuData2 menuData = takeoutMenuDataList.get(j);
					if ((takeoutMenuData.uuid).equals(menuData.uuid)) {// 如果发现原来有数据
						if (takeoutMenuData.num==0) {
							isClearCurrentData=true;
						}
						takeoutMenuDataList.remove(j);
					} 
				}
			}
			if (!isClearCurrentData) {
				takeoutMenuDataList.add(takeoutMenuData);
			}
		} else {

			TakeoutMenuData2 data = new TakeoutMenuData2();
			data = takeoutMenuData;
			List<TakeoutMenuData2> takeoutMenuDataList = new ArrayList<TakeoutMenuData2>();
			takeoutMenuDataList.add(data);
			TakeoutMenuListDTO takeoutMenuListDTO = new TakeoutMenuListDTO();
			takeoutMenuListDTO.list = takeoutMenuDataList;
			List<TakeoutMenuListDTO> takeoutMenuListDTOList = new ArrayList<TakeoutMenuListDTO>();
			takeoutMenuListDTOList.add(takeoutMenuListDTO);
			TakeoutMenuListPackDTO takeoutMenuListPackDTO = new TakeoutMenuListPackDTO();
			takeoutMenuListPackDTO.list = takeoutMenuListDTOList;
			cartDto = takeoutMenuListPackDTO;
		}
		String json = JsonUtils.toJson(cartDto);
		vc.add("TAKEOUT_SHOPPING_CART", "TAKEOUT_SHOPPING_CART_" + uuid, json, "", "", 60 * 24 * 7);
	}
	// 清空购物车
	public void removeCartCache(String uuid) {
		ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
		vc.remove("TAKEOUT_SHOPPING_CART", "TAKEOUT_SHOPPING_CART_" + uuid);
	}

	// 取得购物车缓存数据
	public TakeoutMenuListPackDTO getTakeoutCartCache(String uuid) {
		TakeoutMenuListPackDTO dto = getTakeoutCartCacheInDB(uuid);
		return dto;
	}

	// 取得缓存在数据库里面的数据
	private TakeoutMenuListPackDTO getTakeoutCartCacheInDB(String uuid) {
		TakeoutMenuListPackDTO dto = null;
		ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
		ValueObject vo = vc.get("TAKEOUT_SHOPPING_CART", "TAKEOUT_SHOPPING_CART_" + uuid);
		if (vo != null && !vo.isExpired()) {
			dto = JsonUtils.fromJson(vo.getValue(), TakeoutMenuListPackDTO.class);
		} else {
			dto = null;
		}
		return dto;
	}

	public CityListDTO searchCityByKeyword(Context context, String keyword) {
		CityListDTO tmpDTO = new CityListDTO();
		keyword = keyword.trim().toLowerCase();

		if (CheckUtil.isEmpty(keyword)) {
			return tmpDTO;
		}

		// 匹配关键字
		for (CityData cityData : cityListDTO.getList()) {
			if (isMatchCityByKeyword(cityData, keyword)) {
				tmpDTO.getList().add(cityData);
			}
		}

		if (tmpDTO.getList().size() > 0) {
			return tmpDTO;
		}

		// 匹配首拼
		for (CityData cityData : cityListDTO.getList()) {
			if (isMatchCityByFirstLetter(cityData, keyword.toLowerCase())) {
				tmpDTO.getList().add(cityData);
			}
		}

		if (tmpDTO.getList().size() > 0) {
			return tmpDTO;
		}

		// 匹配全部拼音，当关键字全为英文时，认为用户输入了拼音
		if (CheckUtil.isNumOrWord(keyword)) {
			for (CityData cityData : cityListDTO.getList()) {
				if (isMatchCityByPinyin(context, cityData, keyword.toLowerCase())) {
					tmpDTO.getList().add(cityData);
				}
			}
		}

		if (tmpDTO.getList().size() > 0) {
			return tmpDTO;
		}

		// 匹配全部拼音，当关键字不全为英文时，认为用户输入了中文
		String pinyin = HanziUtil.getPinyin(context, keyword).replace(" ", "");
		if (!CheckUtil.isEmpty(pinyin)) {
			for (CityData cityData : cityListDTO.getList()) {
				if (isMatchCityByPinyin(context, cityData, pinyin)) {
					tmpDTO.getList().add(cityData);
				}
			}
		}

		return tmpDTO;
	}

	private boolean isMatchCityByKeyword(CityData cityData, String keyword) {
		return cityData.getCityName().indexOf(keyword) > -1;
	}

	private boolean isMatchCityByFirstLetter(CityData cityData, String keyword) {
		return cityData.getFirstLetter().toLowerCase().indexOf(keyword) > -1;
	}

	private boolean isMatchCityByPinyin(Context context, CityData cityData, String pinyin) {
		if (!CheckUtil.isEmpty(pinyin) && cityData.getPinyin().toLowerCase().startsWith(pinyin)) {
			return true;
		}
		return false;
	}

	// -----------------------------------
	/**
	 * 配置设置，是否在列表中显示餐厅图片
	 */
	public void setShowRestPicture(Context context, boolean showRestPicture) {

		SharedprefUtil.saveBoolean(context, Settings.KEY_SHOW_REST_PICTURE, showRestPicture);

	}

	/**
	 * 获取配置：是否在列表中显示餐厅图片
	 */
	public boolean getShowRestPicture(Context context) {

		return SharedprefUtil.getBoolean(context, Settings.KEY_SHOW_REST_PICTURE, true);

	}

	/*
	 * //--缓存订单和站内信数量 public void setOrderAndMsgNum(Context context,int
	 * orderNum,int msgNum) {
	 * 
	 * String value=orderNum+";"+msgNum; SharedprefUtil.save(context,
	 * Settings.KEY_ORDER_AND_MESSAGE_MUN, value); setChanged();
	 * this.notifyObservers("update"); }
	 * 
	 * //--获取订单和站内信数量 int 数组第一个表示订单数量，第二个表示站内信数量 默认都为0 public int[]
	 * getOrderAndMsgNum(Context ctx,String key) { int[] num=new int[2]; String
	 * value=SharedprefUtil.get(ctx, Settings.KEY_ORDER_AND_MESSAGE_MUN, "0;0");
	 * num[0]=Integer.parseInt(value.split(";")[0]);
	 * num[1]=Integer.parseInt(value.split(";")[1]); return num;
	 * 
	 * }
	 */

	// --广告缓存get
	public List<MainPageAdvData> getMainPageAdvDataList() {

		try {
			// ----
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());

			if (city == null) {
				city = new CityInfo();
			}
//			city=new CityInfo();
//			city.setId("200000");
			// ---
			MainPageMsgListDTO data = null;
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.CACHE_DIR_ADVERTISEMENT,
					Settings.CACHE_DIR_ADVERTISEMENT + "_" + city.getId() + "_" + Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				data = JsonUtils.fromJson(vo.getValue(), MainPageMsgListDTO.class);
			}
			// ----
			if (data == null || data.advList == null) {
				data = new MainPageMsgListDTO();
				data.advList = new ArrayList<MainPageAdvData>();
			}
			// 过滤过期的广告
			List<MainPageAdvData> advDataList = new ArrayList<MainPageAdvData>();
			int i = 0;
			for (MainPageAdvData adv : data.advList) {
				if (i > 19) {
					break;// 保护不能超过20条广告
				}
				if (adv.endDate > System.currentTimeMillis()) {
					advDataList.add(adv);
				}
				i++;
			}
			//
			data.advList = advDataList;
			// Log.d("SessionManager-2-getMainPageAdvDataList()",
			// ""+mainPageMsgListDTO.advList.size());
			//广告位上增加切换城市
			if(data.advList!=null){
				MainPageAdvData changeCity=new MainPageAdvData();
				//广告类别   1:广告链接  2：本地连接   3:普通链接  4:软件连接
				//这里增加了一个切换城市的类型：-9
				changeCity.typeTag=-9;
				changeCity.title="我在"+city.getName()+"，点击切换城市";
				data.advList.add(0,changeCity);
			}
			return advDataList;
		} catch (Exception e) {
			Log.e("SessionManager.getMainPageAdvData()", e.getMessage(), e);
			return null;
		}
	}

	public void setMainPageAdvDataList(List<MainPageAdvData> list) {
		try {
			if (list == null) {
				return;
			}
			// --
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			// Log.d("添加的--",""+list.size());
			// 广告，合并后台取回来的增量广告
			// 1-取缓存
			List<MainPageAdvData> cacheDataList = SessionManager.getInstance().getMainPageAdvDataList();
			if (cacheDataList == null) {
				cacheDataList = new ArrayList<MainPageAdvData>();
			}

			// 2-合并，合并逻辑：检查uuid，如果新来的和旧的有重复的uuid，就替换旧的
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					for (int j = 0; j < cacheDataList.size(); j++) {
						if (cacheDataList.get(j).uuid.equals(list.get(i).uuid)) {
							cacheDataList.remove(j);
							i--;
							break;
						}
					}
				}
				cacheDataList.addAll(0, list);
			}
			// --------------
			// MainPageAdvData temp = new MainPageAdvData();
			// temp.endDate = System.currentTimeMillis() + 10000000000000L;
			// temp.picUrl =
			// "http://upload2.95171.cn/meal/CashPic/500_335/12aa0d0e-f12a-4c00-b1ed-89da37b88d5f.jpg";
			// temp.uuid = "32324";
			// temp.title = "水煮大青蛙";
			// cacheDataList.add(0, temp);
			//
			// temp = new MainPageAdvData();
			// temp.endDate = System.currentTimeMillis() + 10000000000000L;
			// temp.picUrl =
			// "http://upload2.95171.cn/meal/CashPic/500_335/d41d0130-b069-4910-a811-bfe6c41c6cd4.jpg";
			// temp.uuid = "3232d4";
			// temp.title = "万轩人家100元现金券";
			// cacheDataList.add(0, temp);
			// ---------------
			MainPageMsgListDTO mainPageMsgListDTO = new MainPageMsgListDTO();
			mainPageMsgListDTO.advList = cacheDataList;

			// Log.d("SessionManager.setMainPageAdvDataList()",
			// ""+mainPageMsgListDTO.advList.size());
			// ---
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(Settings.CACHE_DIR_ADVERTISEMENT, Settings.CACHE_DIR_ADVERTISEMENT + "_" + city.getId() + "_" + Settings.VERSION_NAME);
			vc.add(Settings.CACHE_DIR_ADVERTISEMENT, Settings.CACHE_DIR_ADVERTISEMENT + "_" + city.getId() + "_" + Settings.VERSION_NAME, JsonUtils.toJson(mainPageMsgListDTO), "", "", 86400); // 60天

		} catch (Exception e) {
			Log.e("SessionManager.setMainPageAdvDataList()", e.getMessage(), e);
		}
	}

	public ResAndFoodList2DTO getResAndFoodListFromCache(boolean forceUpdate) {
		if (cacheResAndFoodList2DTO == null || forceUpdate) {
			String strJson = "";
			cacheResAndFoodList2DTO = new ResAndFoodList2DTO();
			try {
				Context context = ContextUtil.getContext();
				ValueObject vo = ValueCacheUtil.getInstance(context).get(Settings.KEY_RES_AND_FOOD_LIST2_DTO, "" + getCityInfo(ContextUtil.getContext()).getId());
				// strJson = SharedprefUtil.get(ContextUtil.getContext(),
				// Settings.KEY_RES_AND_FOOD_LIST2_DTO + "_" +
				// getCityInfo(ContextUtil.getContext()).getId(), "");
				if (vo != null) {
					// dto = ResAndFoodList2DTO.toBean(new JSONObject(strJson));
					cacheResAndFoodList2DTO = JsonUtils.fromJson(vo.getValue(), ResAndFoodList2DTO.class);
				}
			} catch (Exception e) {
				ActivityUtil.saveException(e, strJson);
				e.printStackTrace();
			}
		}
		return cacheResAndFoodList2DTO;
	}

	public void setResAndFoodListCache(final ResAndFoodList2DTO dto) {

		// 更新内存缓存
		cacheResAndFoodList2DTO = dto;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (dto != null) {
						// 写入Sharedpref
						String strJson = JsonUtils.toJson(dto);
						Context context = ContextUtil.getContext();
						ValueCacheUtil.getInstance(context).remove(Settings.KEY_RES_AND_FOOD_LIST2_DTO, "" + getCityInfo(ContextUtil.getContext()).getId());
						ValueCacheUtil.getInstance(context).add(Settings.KEY_RES_AND_FOOD_LIST2_DTO, "" + getCityInfo(ContextUtil.getContext()).getId(), strJson);
						// SharedprefUtil.save(ContextUtil.getContext(),Settings.KEY_RES_AND_FOOD_LIST2_DTO
						// + "_" +
						// getCityInfo(ContextUtil.getContext()).getId(),
						// strJson);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();

	}

	/**
	 * 判断当前城市是否有某功能菜单项
	 * 
	 * @param menuName
	 *            菜单名称见 SessionManager.MENU_NAMES
	 * @return
	 */
	public boolean doesCurrentCityHaveMainMenuItem(String menuName) {
		return doesCityHaveMainMenuItem(menuName, getCityInfo(ContextUtil.getContext()).getId());
	}

	/**
	 * 判断某城市是否有某功能菜单项
	 * 
	 * @param menuName
	 *            菜单名称见 SessionManager.MENU_NAMES
	 * @param cityId
	 * @return
	 */
	public boolean doesCityHaveMainMenuItem(String menuName, String cityId) {
		try {
			CityData cityData = SessionManager.getInstance().getCityDataFromCityList2DTO(cityId);
			String menuString = "";
			if (cityData != null && !CheckUtil.isEmpty(cityData.getMainMenuInfo())) {
				// 从CityList2DTO中获取数据
				menuString = cityData.getMainMenuInfo();
			} else {
				// 获得默认数据
				menuString = kCityDefaultMenuList;
			}
			return hasMenu(menuName, menuString);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 判断菜单数据中是否有menuName指定的菜单项，0：没有；1：有
	private boolean hasMenu(String menuName, String menuString) {
		String[] menus = menuString.split(";");
		Integer index = MENU_NAMES.get(menuName);
		//
		if (index == null || "0".equals(menus[index.intValue()])) {
			return false;
		} else {
			return true;
		}
	}

	public String getPointsHintForShareSoftware() {
		return pointsHintForShareSoftware;
	}

	public void setPointsHintForShareSoftware(String pointsHintForShareSoftware) {
		this.pointsHintForShareSoftware = pointsHintForShareSoftware;
	}

	public boolean canShowNearAd(String adId) {
		String adIds = SharedprefUtil.get(ContextUtil.getContext(), Settings.KEY_NEAR_SEARCH_ADV, "");
		if (TextUtils.isEmpty(adIds)) {
			return true;
		}
		String[] idArray = adIds.split(",");
		if (idArray == null || idArray.length == 0) {
			return true;
		}
		for (String id : idArray) {
			if (id.equals(adId)) {
				return false;
			}
		}
		return true;
	}

	public void closeNearAd(String adId) {
		String adIds = SharedprefUtil.get(ContextUtil.getContext(), Settings.KEY_NEAR_SEARCH_ADV, "");
		if (TextUtils.isEmpty(adIds)) {
			adIds = adId;
		} else {
			adIds += "," + adId;
		}
		SharedprefUtil.save(ContextUtil.getContext(), Settings.KEY_NEAR_SEARCH_ADV, adIds);
	}

	public String getCacheBookPhone() {
		try {
			if (!SessionManager.getInstance().isUserLogin(ContextUtil.getContext())) {
				return "";
			}
			UserInfoDTO userDto = SessionManager.getInstance().getUserInfo(ContextUtil.getContext());
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.KEY_BOOK_PHONE, userDto.getUuid());
			if (vo != null) {
				return vo.getValue();
			}
			if (TextUtils.isEmpty(userDto.getTel())) {
				return "";
			}
			setCacheBookPhone(userDto.getUuid(), userDto.getTel());
			return userDto.getTel();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public void setCacheBookPhone(String uuid, String phone) {
		try {
			if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(phone)) {
				return;
			}
			ValueCacheUtil.getInstance(ContextUtil.getContext()).remove(Settings.KEY_BOOK_PHONE, uuid);
			ValueCacheUtil.getInstance(ContextUtil.getContext()).add(Settings.KEY_BOOK_PHONE, uuid, phone);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回默认的预订时间
	 * 
	 * @return
	 */
	public Calendar getDefaultBookTime() {
		try {
			int mYear;
			int mMonth;
			int mDay;
			int mHour;
			int mMinute;
			// 获得当前时间
			Calendar cNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			mYear = cNow.get(Calendar.YEAR);
			mMonth = cNow.get(Calendar.MONTH) + 1;
			mDay = cNow.get(Calendar.DAY_OF_MONTH);
			mHour = cNow.get(Calendar.HOUR_OF_DAY);
			mMinute = cNow.get(Calendar.MINUTE);
			// 调整分钟数使得值为0,15,30,45
			if (mMinute > 0 && mMinute < 15) {
				mMinute = 15;
			} else if (mMinute > 15 && mMinute < 30) {
				mMinute = 30;
			} else if (mMinute > 30 && mMinute < 45) {
				mMinute = 45;
			} else if (mMinute > 45 && mMinute <= 59) {
				mMinute = 0;
				cNow.add(Calendar.HOUR_OF_DAY, 1);
			}
			cNow.set(Calendar.MINUTE, mMinute);
			// 比较当前时间是否超过当天18点30分
			Calendar cDeadline = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			cDeadline.set(mYear, mMonth - 1, mDay, VALVE_HOUR, VALVE_MINUTE);
			if (cNow.before(cDeadline)) {
				// 未超过则设置默认预订时间
				mHour = DEFAULT_HOUR;
				mMinute = DEFAULT_MINUTE;
			} else {
				// 超过则固定增加30分钟
				Calendar cNew = (Calendar) cNow.clone();
				cNew.add(Calendar.MINUTE, 30);

				mYear = cNew.get(Calendar.YEAR);
				mMonth = cNew.get(Calendar.MONTH) + 1;
				mDay = cNew.get(Calendar.DAY_OF_MONTH);
				mHour = cNew.get(Calendar.HOUR_OF_DAY);
				mMinute = cNew.get(Calendar.MINUTE);
			}
			Calendar calResult = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			calResult.set(mYear, mMonth - 1, mDay, mHour, mMinute);
			return calResult;
		} catch (Exception e) {
			Calendar calResult = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			calResult.add(Calendar.MINUTE, 30);
			return calResult;
		}
	}

	/**
	 * 调整日期使得分钟间隔为15分钟
	 * 
	 * @param calInput
	 * @return
	 */
	public Calendar fixCalendarToPer15(Calendar calInput) {
		try {
			Calendar calOutput = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			calOutput.setTimeInMillis(calInput.getTimeInMillis());
			int mMinute = calInput.get(Calendar.MINUTE);
			// 调整分钟数使得值为0,15,30,45
			if (mMinute > 0 && mMinute < 15) {
				mMinute = 15;
			} else if (mMinute > 15 && mMinute < 30) {
				mMinute = 30;
			} else if (mMinute > 30 && mMinute < 45) {
				mMinute = 45;
			} else if (mMinute > 45 && mMinute <= 59) {
				mMinute = 0;
				calOutput.add(Calendar.HOUR_OF_DAY, 1);
			}
			calOutput.set(Calendar.MINUTE, mMinute);
			calOutput.set(Calendar.SECOND, 0);
			return calOutput;
		} catch (Exception e) {
			return calInput;
		}
	}

	/**
	 * 是否可以发送Contract
	 * 
	 * @param currentCount
	 * @return
	 */
	public boolean checkCanSendConta(int currentCount) {
		if (currentCount == 0) {
			return false;
		}
		int count = SharedprefUtil.getInt(ContextUtil.getContext(), Settings.KEY_CONTR_COUNT, 0);
		if (currentCount != count) {
			if (SharedprefUtil.contains(ContextUtil.getContext(), Settings.KEY_ALLOW_UPLOAD_CONTACT)) {
				boolean allowUpload = SharedprefUtil.getBoolean(ContextUtil.getContext(), Settings.KEY_ALLOW_UPLOAD_CONTACT, true);
				if (allowUpload) {
					// 允许上传，保存最新的联系人数目
					SharedprefUtil.saveInt(ContextUtil.getContext(), Settings.KEY_CONTR_COUNT, currentCount);
				}
				return allowUpload;
			} else {
				// 不包含是否允许的缓存Key，认为允许上传
				SharedprefUtil.saveInt(ContextUtil.getContext(), Settings.KEY_CONTR_COUNT, currentCount);
				return true;
			}
		}
		return false;
	}

	public boolean isAllowUploadContact() {
		return SharedprefUtil.getBoolean(ContextUtil.getContext(), Settings.KEY_ALLOW_UPLOAD_CONTACT, false);
	}

	public void setAllowUploadContact(boolean allow) {
		SharedprefUtil.saveBoolean(ContextUtil.getContext(), Settings.KEY_ALLOW_UPLOAD_CONTACT, allow);
	}

	/**
	 * 记录询问用户是否导入通讯录的次数，增加一次
	 */
	public void addRequestUploadContactTimes() {
		int times = SharedprefUtil.getInt(ContextUtil.getContext(), Settings.KEY_REQUEST_UPLOAD_CONTACT, 0);
		times++;
		SharedprefUtil.saveInt(ContextUtil.getContext(), Settings.KEY_REQUEST_UPLOAD_CONTACT, times);
	}

	/**
	 * 询问用户是否导入通讯录的次数，是否超过指定次数
	 * 
	 * @return
	 */
	public boolean isRequestUploadContactOverMaxTime() {
		int times = SharedprefUtil.getInt(ContextUtil.getContext(), Settings.KEY_REQUEST_UPLOAD_CONTACT, 0);
		return times >= 5;
	}

	/**
	 * 当前城市是否有实时预订,以当前选择的城市为判断依据
	 * 
	 * @return
	 */
	public boolean hasRealTimeBook() {
		CityInfo city = getCityInfo(ContextUtil.getContext());
		if (city == null) {
			return false;
		}
		return "200000".equals(city.getId());
	}

	/**
	 * 当前城市是否有实时预订,以当前所在的城市为判断依据
	 * 
	 * @return
	 */
	public boolean hasRealTimeBookByCurrentGpsCity() {
		CityInfo city = getGpsCity(ContextUtil.getContext());
		if (city == null || TextUtils.isEmpty(city.getId())) {
			return false;
		}
		return "200000".equals(city.getId());
	}

	public ShRegionListDTO getShRegionListDTO() {
		if (shRegionListDTO == null) {
			String strJson = "";
			shRegionListDTO = new ShRegionListDTO();
			try {
				Context context = ContextUtil.getContext();
				ValueObject vo = ValueCacheUtil.getInstance(context).get(Settings.KEY_SH_ALL_REGION, getCityInfo(ContextUtil.getContext()).getId());
				if (vo != null) {
					shRegionListDTO = ShRegionListDTO.toBean(new JSONObject(vo.getValue()));
				} else {
					initShanghaiRegionData(context);
				}
			} catch (Exception e) {
				ActivityUtil.saveException(e, strJson);
				e.printStackTrace();
			}
		}
		return shRegionListDTO;
	}

	public void setShRegionListDTO(final ShRegionListDTO shRegionListDTO) {
		this.shRegionListDTO = shRegionListDTO;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (shRegionListDTO != null) {
						// 写入Sharedpref
						String strJson = JsonUtils.toJson(shRegionListDTO);
						Context context = ContextUtil.getContext();
						ValueCacheUtil.getInstance(context).remove(Settings.KEY_SH_ALL_REGION, getCityInfo(ContextUtil.getContext()).getId());
						ValueCacheUtil.getInstance(context).add(Settings.KEY_SH_ALL_REGION, getCityInfo(ContextUtil.getContext()).getId(), strJson);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void initShanghaiRegionData(Context context) {
		shRegionListDTO = new ShRegionListDTO();
		RfTypeListDTO region;
		RfTypeDTO district;

		region = new RfTypeListDTO();
		region.u = "SH_PD";
		region.n = "浦东新区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_PD_01";
		district.n = "陆家嘴";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_13";
		district.n = "八佰伴";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_12";
		district.n = "世纪公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_08";
		district.n = "世纪大道";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_06";
		district.n = "金桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_04";
		district.n = "源深体育中心";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_09";
		district.n = "塘桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_14";
		district.n = "上南路沿线";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_22";
		district.n = "碧云社区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_10";
		district.n = "张江";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_07";
		district.n = "花木";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_11";
		district.n = "外高桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_26";
		district.n = "川沙镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_28";
		district.n = "惠南镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_27";
		district.n = "周浦镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_15";
		district.n = "康桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_25";
		district.n = "三林镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_24";
		district.n = "北蔡镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_29";
		district.n = "世博沿线";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PD_30";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_XH";
		region.n = "徐汇区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_XH_01";
		district.n = "徐家汇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_02";
		district.n = "万体馆";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_17";
		district.n = "衡山路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_18";
		district.n = "肇嘉浜路沿线";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_19";
		district.n = "音乐学院";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_04";
		district.n = "田林";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_21";
		district.n = "复兴西路/丁香花园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_06";
		district.n = "淮海路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_07";
		district.n = "漕河泾";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_05";
		district.n = "龙华/新龙华";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_16";
		district.n = "上海南站";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_08";
		district.n = "交大";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_20";
		district.n = "植物园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_XH_22";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_CN";
		region.n = "长宁区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_CN_01";
		district.n = "中山公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_02";
		district.n = "虹桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_07";
		district.n = "上海影城";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_03";
		district.n = "古北";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_05";
		district.n = "仙霞";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_10";
		district.n = "动物园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_04";
		district.n = "天山";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_08";
		district.n = "北新泾";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_CN_11";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_JA";
		region.n = "静安区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_JA_02";
		district.n = "静安寺";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_05";
		district.n = "上海商城";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_04";
		district.n = "电视台";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_07";
		district.n = "曹家渡";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_08";
		district.n = "同乐坊";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_06";
		district.n = "华山医院";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_01";
		district.n = "淮海路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_03";
		district.n = "玉佛寺";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_JA_09";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_HP";
		region.n = "黄浦区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_HP_02";
		district.n = "人民广场";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_03";
		district.n = "南京东路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_01";
		district.n = "外滩";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_07";
		district.n = "淮海路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_08";
		district.n = "城隍庙";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_10";
		district.n = "老码头";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_05";
		district.n = "董家渡";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_04";
		district.n = "南浦大桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_09";
		district.n = "老西门";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_06";
		district.n = "十六铺";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HP_11";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_LW";
		region.n = "卢湾区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_LW_08";
		district.n = "新天地";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_01";
		district.n = "打浦桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_06";
		district.n = "锦江饭店";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_07";
		district.n = "淮海公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_03";
		district.n = "瑞金医院";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_09";
		district.n = "田子坊";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_04";
		district.n = "鲁班路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_05";
		district.n = "第九医院";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_10";
		district.n = "世博沿线";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_LW_11";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_MH";
		region.n = "闵行区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_MH_05";
		district.n = "南方商城";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_06";
		district.n = "莘庄";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_12";
		district.n = "吴中路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_23";
		district.n = "虹梅路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_10";
		district.n = "虹桥镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_08";
		district.n = "七宝";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_21";
		district.n = "龙柏";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_22";
		district.n = "春申地区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_16";
		district.n = "万源城";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_02";
		district.n = "老闵行";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_13";
		district.n = "锦江乐园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_07";
		district.n = "古美东兰";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_01";
		district.n = "漕宝路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_03";
		district.n = "梅陇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_18";
		district.n = "颛桥镇";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_17";
		district.n = "华东理工附近";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_MH_24";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_YP";
		region.n = "杨浦区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_YP_02";
		district.n = "五角场";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_03";
		district.n = "控江";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_05";
		district.n = "杨浦公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_01";
		district.n = "复旦/同济大学区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_07";
		district.n = "黄兴公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_04";
		district.n = "中原小区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_06";
		district.n = "鞍山";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_YP_08";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_HK";
		region.n = "虹口区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_HK_02";
		district.n = "四川北路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_04";
		district.n = "和平公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_03";
		district.n = "虹口足球场站/鲁迅公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_08";
		district.n = "凉城/江湾";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_01";
		district.n = "曲阳";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_13";
		district.n = "北外滩";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_12";
		district.n = "大柏树";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_05";
		district.n = "提篮桥";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_14";
		district.n = "海宁路/七浦路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_HK_15";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_PT";
		region.n = "普陀区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_PT_10";
		district.n = "长寿路沿线";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_20";
		district.n = "梅川路";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_03";
		district.n = "曹杨";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_01";
		district.n = "甘泉地区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_19";
		district.n = "曹家渡";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_02";
		district.n = "真如";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_04";
		district.n = "长风公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_15";
		district.n = "西宫";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_PT_21";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_ZB";
		region.n = "闸北区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_ZB_03";
		district.n = "上海马戏城/大宁";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_01";
		district.n = "火车站/不夜城";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_04";
		district.n = "闸北公园";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_02";
		district.n = "彭浦";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_06";
		district.n = "北区汽车站";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_05";
		district.n = "和田";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_ZB_07";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_BS";
		region.n = "宝山区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_BS_03";
		district.n = "吴淞";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_01";
		district.n = "大华";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_06";
		district.n = "共康地区";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_02";
		district.n = "通河";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_04";
		district.n = "上海大学";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_05";
		district.n = "共富新村";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_BS_07";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_QP";
		region.n = "青浦区";
		shRegionListDTO.getList().add(region);

		district = new RfTypeDTO();
		district.u = "SH_QP_01";
		district.n = "朱家角";
		region.getList().add(district);

		district = new RfTypeDTO();
		district.u = "SH_QP_02";
		district.n = "其他";
		region.getList().add(district);

		region = new RfTypeListDTO();
		region.u = "SH_SJ";
		region.n = "松江区";
		shRegionListDTO.getList().add(region);

		region = new RfTypeListDTO();
		region.u = "SH_CM";
		region.n = "崇明区";
		shRegionListDTO.getList().add(region);

		region = new RfTypeListDTO();
		region.u = "SH_FX";
		region.n = "奉贤区";
		shRegionListDTO.getList().add(region);

		region = new RfTypeListDTO();
		region.u = "SH_JD";
		region.n = "嘉定区";
		shRegionListDTO.getList().add(region);

		region = new RfTypeListDTO();
		region.u = "SH_JS";
		region.n = "金山区";
		shRegionListDTO.getList().add(region);

		setShRegionListDTO(shRegionListDTO);
	}

	public ChatMsgListDto getChatMsgList() {
		if (chatMsgListDto == null) {
			chatMsgListDto = new ChatMsgListDto();
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.KEY_CHAT_MSG_LIST, Settings.KEY_CHAT_MSG_LIST);
			if (vo != null && vo.getValue() != null) {
				chatMsgListDto = JsonUtils.fromJson(vo.getValue(), ChatMsgListDto.class);
			}
		}
		return chatMsgListDto;
	}

	public void setChatMsgList(ChatMsgListDto dto) {
		if (chatMsgListDto == null) {
			return;
		}
		chatMsgListDto = dto;
		final String strJson = JsonUtils.toJson(chatMsgListDto);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Context context = ContextUtil.getContext();
				ValueCacheUtil.getInstance(context).remove(Settings.KEY_CHAT_MSG_LIST, Settings.KEY_CHAT_MSG_LIST);
				ValueCacheUtil.getInstance(context).add(Settings.KEY_CHAT_MSG_LIST, Settings.KEY_CHAT_MSG_LIST, strJson);

			}
		}).start();
	}

	// 获取缓存的最后一条成功接收的"系统"消息Id，用于下一次向服务器确认号码
	public String getLastChatMsgId() {
		String lastId = "";
		try {
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID, Settings.KEY_CHAT_MSG_LIST);
			if (vo != null && vo.getValue() != null) {
				lastId = vo.getValue();
			}
			return lastId;
		} catch (Exception e) {
			return lastId;
		}
	}

	public void addChatMsgListDto(ChatMsgListDto dto) {
		if (dto == null || dto.getList() == null || dto.getList().size() == 0) {
			return;
		}
		if (chatMsgListDto == null) {
			chatMsgListDto = getChatMsgList();
		}
		List<ChatMsgData> listTmp = new ArrayList<ChatMsgData>(chatMsgListDto.getList());
		listTmp.addAll(dto.getList());
		chatMsgListDto.setList(listTmp);
		setChatMsgList(chatMsgListDto);
	}

	public void addChatMsgData(ChatMsgData chatMsgData) {
		if (chatMsgData == null) {
			return;
		}
		if (chatMsgListDto == null) {
			chatMsgListDto = getChatMsgList();
		}
		List<ChatMsgData> listTmp = new ArrayList<ChatMsgData>(chatMsgListDto.getList());
		listTmp.add(chatMsgData);
		chatMsgListDto.setList(listTmp);
		setChatMsgList(chatMsgListDto);
	}

	public void updateChatMsgData(ChatMsgData chatMsgData) {
		if (chatMsgData == null || TextUtils.isEmpty(chatMsgData.getUuid())) {
			return;
		}
		if (chatMsgListDto == null) {
			chatMsgListDto = getChatMsgList();
		}
		if (chatMsgListDto.getList() == null || chatMsgListDto.getList().size() == 0) {
			return;
		}
		for (int i = 0; i < chatMsgListDto.getList().size(); i++) {
			ChatMsgData msg = chatMsgListDto.getList().get(i);
			if (msg.getUuid().equals(chatMsgData.getUuid())) {
				chatMsgListDto.getList().set(i, chatMsgData);
			}
		}
		setChatMsgList(chatMsgListDto);
	}

	public String getHeadPic() {
		if (SessionManager.getInstance().isUserLogin(ContextUtil.getContext()) && !TextUtils.isEmpty(SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getPicUrl())) {
			return SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getPicUrl();
		} else {
			return Settings.DEFAULT_HEAD_PIC;
		}
	}

	public boolean isChatOnline() {
		return chatOnline;
	}

	public void setChatOnline(boolean chatOnline) {
		this.chatOnline = chatOnline;
	}

	public long getCreateChatRoomTimestamp() {
		return SharedprefUtil.getLong(ContextUtil.getContext(), Settings.KEY_CHAT_CREATE_CHAT_ROOM_TIMESTAMP, 0);
	}

	public void setCreateChatRoomTimestamp(long timestamp) {
		SharedprefUtil.saveLong(ContextUtil.getContext(), Settings.KEY_CHAT_CREATE_CHAT_ROOM_TIMESTAMP, timestamp);
	}

	public void setSelectedFood(String resId, List<FoodSubListForSelectData> list) {
		try {
			if (TextUtils.isEmpty(resId)) {
				return;
			}
			if (list == null || list.size() == 0) {
				ValueCacheUtil.getInstance(ContextUtil.getContext()).remove(Settings.VERSION_NAME + "_" + Settings.KEY_SELECTED_FOOD, resId);
				return;
			}
			String jsonStr = JsonUtils.toJson(list);
			ValueCacheUtil.getInstance(ContextUtil.getContext()).remove(Settings.VERSION_NAME + "_" + Settings.KEY_SELECTED_FOOD, resId);
			ValueCacheUtil.getInstance(ContextUtil.getContext()).add(Settings.VERSION_NAME + "_" + Settings.KEY_SELECTED_FOOD, resId, jsonStr, "", Settings.VERSION_NAME, -1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<FoodSubListForSelectData> getSelectedFood(String resId) {
		List<FoodSubListForSelectData> selectedFood = new ArrayList<FoodSubListForSelectData>();
		try {
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.VERSION_NAME + "_" + Settings.KEY_SELECTED_FOOD, resId);
			if (vo != null && vo.getValue() != null) {
				selectedFood = JsonUtils.fromJson(vo.getValue(), new TypeToken<List<FoodSubListForSelectData>>() {
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return selectedFood;
	}

	public int getNormalRequestTimeout() {
		if (normalRequestTimeout >= 15 && normalRequestTimeout < 120) {
			return normalRequestTimeout;
		}
		return 15;
	}

	public void setNormalRequestTimeout(int normalRequestTimeout) {
		this.normalRequestTimeout = normalRequestTimeout;
	}

	public int getUploadRequestTimeout() {
		if (uploadRequestTimeout >= 15 && uploadRequestTimeout < 120) {
			return uploadRequestTimeout;
		}
		return 15;
	}

	public void setUploadRequestTimeout(int uploadRequestTimeout) {
		this.uploadRequestTimeout = uploadRequestTimeout;
	}

	// --
	SoftwareCommonData softwareCommonData;

	public void setSoftwareCommonData(SoftwareCommonData data) {
		try {
			if (data == null) {
				return; // 防止被无效数据刷空
			}
			softwareCommonData = data;
			// 更新城市数据
			if (data.getCityDto() != null && data.getCityDto().needUpdateTag) {
				SessionManager.getInstance().setCityListDTO(ContextUtil.getContext(), data.getCityDto(), JsonUtils.toJson(data.getCityDto()));
			}
			// 更新报错数据
			if (data.getErrorReportTypeListDto() != null && data.getErrorReportTypeListDto().needUpdateTag) {
				SessionManager.getInstance().getListManager().setErrorReportTypeListPack(ContextUtil.getContext(), data.getErrorReportTypeListDto());
			}
			// 更新订单选择信息
			if (data.getOrderSelInfo() != null) {
				SessionManager.getInstance().setOrderSelInfo(data.getOrderSelInfo());
			}
			// 设置讯飞语音参数
			if (!CheckUtil.isEmpty(data.getXfUrl()) && !CheckUtil.isEmpty(data.getXfEngineName())) {
				Settings.XF_Params = data.getXfUrl();
				Settings.XF_ENGINE_NAME = data.getXfEngineName();
			}

			// 当前还未退出软件时处理
			// if (ActivityUtil.isOnForeground(ContextUtil.getContext()) &&
			// data.getVersionChkDto() != null) {
			if (data.getVersionChkDto() != null) {
				Settings.gVersionChkDTO = data.getVersionChkDto();
//				Settings.gVersionChkDTO.haveNewVersionTag=true;
//				Settings.gVersionChkDTO.downloadUrl="http://baidu";
//				Settings.gVersionChkDTO.info="好好好";
//				Settings.gVersionChkDTO.newVersion="5.5.5";
				// 通知版本更新
				CommonObservable.getInstance().notifyObservers(CommonObserver.NewVersionObserver.class);
			}
			// 服务器时间戳
			Settings.TIME_DIFF = data.getServerTimestamp() - System.currentTimeMillis();
			// 更新普通连接超时设置
			AbstractHttpApi.updateHttpClientTimeout(Fg114Application.mHttpClient, data.getNormalRequestTimeout());
			// 更新上传连接超时设置
			setUploadRequestTimeout(data.getUploadRequestTimeout());
			// ---------------------------------------
			String jsonStr = JsonUtils.toJson(data);
			ValueCacheUtil.getInstance(ContextUtil.getContext()).remove(Settings.VERSION_NAME, "SoftwareCommonData");
			ValueCacheUtil.getInstance(ContextUtil.getContext()).add(Settings.VERSION_NAME, "SoftwareCommonData", jsonStr, "", Settings.VERSION_NAME, -1);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public SoftwareCommonData getSoftwareCommonData() {
		SoftwareCommonData data = new SoftwareCommonData();
		try {
			if (softwareCommonData != null) {
				return softwareCommonData;
			}
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.VERSION_NAME, "SoftwareCommonData");
			if (vo != null && vo.getValue() != null) {
				data = JsonUtils.fromJson(vo.getValue(), SoftwareCommonData.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	// --订单选择信息
	private OrderSelInfo orderSelInfo;

	public void setOrderSelInfo(OrderSelInfo orderSelInfo) {
		if (orderSelInfo != null) {
			this.orderSelInfo = orderSelInfo;
		}
	}

	public OrderSelInfo getOrderSelInfo() {
		if (orderSelInfo == null) {
			orderSelInfo = new OrderSelInfo();
		}
		return orderSelInfo;
	}
	// --

	public void setMainPageInfoPackDTO(MainPageInfoPack4DTO dto) {
		try {
			if (dto == null) {
				return; // 防止被无效数据刷空
			}
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			// 存入广告
			setMainPageAdvDataList(dto.mainPageMsgListDTO.advList);
			// ---
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(Settings.CACHE_DIR_MainPageInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME);
			vc.add(Settings.CACHE_DIR_MainPageInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", 30); // 60天

		} catch (Exception e) {
			Log.e("SessionManager.getMainPageAdvData()", e.getMessage(), e);
		}
	}

	public MainPageInfoPack4DTO getMainPageInfoPackDTO() {
		MainPageInfoPack4DTO dto = new MainPageInfoPack4DTO();
		try {
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			// ---
			dto = new MainPageInfoPack4DTO();
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.CACHE_DIR_MainPageInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				dto = JsonUtils.fromJson(vo.getValue(), MainPageInfoPack4DTO.class);
			}

		} catch (Exception e) {
			Log.e("SessionManager.getMainPageAdvData()", e.getMessage(), e);
		}
		return dto;
	}
	
	
	public void setMainPageOtherInfoPackDTO(MainPageOtherInfoPackDTO dto) {
		try {
			if (dto == null) {
				return; // 防止被无效数据刷空
			}
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(Settings.CACHE_DIR_MainPageOtherInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME);
			vc.add(Settings.CACHE_DIR_MainPageOtherInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", 30); // 60天

		} catch (Exception e) {
			Log.e("SessionManager.getMainPageAdvData()", e.getMessage(), e);
		}
	}

	public MainPageOtherInfoPackDTO getMainPageOtherInfoPackDTO() {
		MainPageOtherInfoPackDTO dto = new MainPageOtherInfoPackDTO();
		try {
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			// ---
			dto = new MainPageOtherInfoPackDTO();
			ValueObject vo = ValueCacheUtil.getInstance(ContextUtil.getContext()).get(Settings.CACHE_DIR_MainPageOtherInfoPackDTO, city.getId() + "_" + Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				dto = JsonUtils.fromJson(vo.getValue(), MainPageOtherInfoPackDTO.class);
			}

		} catch (Exception e) {
			Log.e("SessionManager.getMainPageAdvData()", e.getMessage(), e);
		}
		return dto;
	}

	// -------------- 商区浏览记录缓存
	// 商区历史浏览记录key
	private static final String KEY_HOT_DISTRICT_BROWSE_HISTORY_LIST = "hot_district_browse_history_list";

	// 将“商区浏览记录”存入本地缓存
	public void setDistrictBrowseHistoryCache(CommonTypeListDTO dto) {
		try {
			if (dto == null) {
				return;
			}
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(KEY_HOT_DISTRICT_BROWSE_HISTORY_LIST, city.getId() + "|" + Settings.VERSION_NAME);
			vc.add(KEY_HOT_DISTRICT_BROWSE_HISTORY_LIST, city.getId() + "|" + Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", -1);

		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
	}

	// 获取“商区浏览记录”本地缓存
	public CommonTypeListDTO getDistrictBrowseHistoryCache() {
		CommonTypeListDTO commonTypeListDTO = null;
		try {
			CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
			if (city == null) {
				city = new CityInfo();
			}
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			ValueObject vo = vc.get(KEY_HOT_DISTRICT_BROWSE_HISTORY_LIST, city.getId() + "|" + Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				commonTypeListDTO = JsonUtils.fromJson(vo.getValue(), CommonTypeListDTO.class);
			}
		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
		return commonTypeListDTO;
	}

	// 最后选择的餐厅key
	private static final String KEY_LAST_SELECTED_REST = "KEY_LAST_SELECTED_REST";

	public void setLastSelectedRest(SuggestResultData dto) {
		try {
			if (dto == null) {
				return;
			}
			Context context = ContextUtil.getContext();
			CityInfo city = SessionManager.getInstance().getCityInfo(context);
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(KEY_LAST_SELECTED_REST + "|" + city.getId(), Settings.VERSION_NAME);
			vc.add(KEY_LAST_SELECTED_REST + "|" + city.getId(), Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", -1);

		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
	}

	public SuggestResultData getLastSelectedRest() {
		SuggestResultData dto = null;
		try {
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			Context context = ContextUtil.getContext();
			CityInfo city = SessionManager.getInstance().getCityInfo(context);
			ValueObject vo = vc.get(KEY_LAST_SELECTED_REST + "|" + city.getId(), Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				dto = JsonUtils.fromJson(vo.getValue(), SuggestResultData.class);
			}
		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
		return dto;
	}
	
	//外卖首页信息
	private static final String KEY_TAKEOUT_INDEX_PAGET_DATA = "KEY_TakeoutIndexPageData";
	public void setTakeoutIndexPageData(TakeoutIndexPageData dto){
		try {
			if (dto == null) {
				return;
			}
			Context context = ContextUtil.getContext();
			CityInfo city = SessionManager.getInstance().getCityInfo(context);
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			vc.remove(KEY_TAKEOUT_INDEX_PAGET_DATA + "|" + city.getId(), Settings.VERSION_NAME);
			vc.add(KEY_TAKEOUT_INDEX_PAGET_DATA + "|" + city.getId(), Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", -1);

		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
	}
	
	public TakeoutIndexPageData getTakeoutIndexPageData(){
		TakeoutIndexPageData dto = null;
		try {
			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
			Context context = ContextUtil.getContext();
			CityInfo city = SessionManager.getInstance().getCityInfo(context);
			ValueObject vo = vc.get(KEY_TAKEOUT_INDEX_PAGET_DATA + "|" + city.getId(), Settings.VERSION_NAME);
			if (vo != null && vo.getValue() != null) {
				dto = JsonUtils.fromJson(vo.getValue(), TakeoutIndexPageData.class);
			}
		} catch (Exception e) {
			if (Settings.DEBUG)
				Log.e("SessionManager", e.getMessage(), e);
		}
		return dto;
	}
	// 免单保key
//	private static final String KEY_Page_Rest_Info = "KEY_mdb_payTypeList";
//	
//	public void setMdbFreeOrderFormData(MdbFreeOrderFormData dto){
//		try {
//			if (dto == null) {
//				return;
//			}
//			Context context = ContextUtil.getContext();
//			CityInfo city = SessionManager.getInstance().getCityInfo(context);
//			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
//			vc.remove(KEY_Page_Rest_Info + "|" + city.getId(), Settings.VERSION_NAME);
//			vc.add(KEY_Page_Rest_Info + "|" + city.getId(), Settings.VERSION_NAME, JsonUtils.toJson(dto), "", "", -1);
//
//		} catch (Exception e) {
//			if (Settings.DEBUG)
//				Log.e("SessionManager", e.getMessage(), e);
//		}
//	}
//	public MdbFreeOrderFormData getMdbFreeOrderFormData(){
//		MdbFreeOrderFormData dto=null;
//		try {
//			ValueCacheUtil vc = ValueCacheUtil.getInstance(ContextUtil.getContext());
//			Context context = ContextUtil.getContext();
//			CityInfo city = SessionManager.getInstance().getCityInfo(context);
//			ValueObject vo = vc.get(KEY_LAST_SELECTED_REST + "|" + city.getId(), Settings.VERSION_NAME);
//			if (vo != null && vo.getValue() != null) {
//				dto = JsonUtils.fromJson(vo.getValue(), MdbFreeOrderFormData.class);
//			}
//		} catch (Exception e) {
//			if (Settings.DEBUG)
//				Log.e("SessionManager", e.getMessage(), e);
//		}
//		return dto;
//	}
}
