package com.fg114.main.weibo.tencent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SoftwareCommonData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.AuthUrls;
import com.fg114.main.weibo.BindToReturnData;
import com.fg114.main.weibo.UserInfo;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.activity.AuthWebActivity;
import com.fg114.main.weibo.activity.MediatorActivity;
import com.fg114.main.weibo.activity.SinaSSOAuthActivity;
import com.fg114.main.weibo.dto.User;
import com.fg114.main.weibo.dto.WeiboFriendsListDTO;
import com.fg114.main.weibo.dto.WeiboFriendsListData;

/**
 * 微博工具类
 * 
 * @author xujianjun,2012-03-22
 * 
 */

public class TencentWeiboUtil extends WeiboUtil {

	private static final int ERROR_CODE_UNBIND = 101; // 新浪微博未绑定
	private static final int ERROR_CODE_EXPIRED = 102; // 微博已过期,过期时更新本地缓存的过期时间sinaBindRemainSecs为0

	private Context context;
	private static TencentWeiboUtil instance;
	// ------------------------------------
	static {
		instance = new TencentWeiboUtil();
		instance.init();
	}
	// ------------------------------------
	// private static final String
	// url_friendships_friends="https://api.weibo.com/2/friendships/friends.json";
	private static final String url_friendships_friends = "http://api.t.sina.com.cn/statuses/friends.json";

	// ------------------------------------

	private TencentWeiboUtil() {
	}

	private void init() {
		this.context = ContextUtil.getContext();
	}

	public static TencentWeiboUtil getInstance() {
		return instance;
	}

	/**
	 * 获取用户好友列表
	 * 
	 * @param accessToken
	 * @param uid
	 *            用户id
	 * @param cursor
	 *            要获取的列表的开始位置，-1表示从第一页开始
	 * @return
	 * @throws Exception
	 * 
	 */
	// public void executeGetFriends(String accessToken,String tokenSecret,
	// String uid, int cursor,RequestListener listener) throws Exception {
	//
	// try {
	// Weibo weibo = Weibo.getInstance();
	// AccessToken token = new AccessToken(accessToken, tokenSecret);
	// weibo.setAccessToken(token);
	//
	// WeiboParameters bundle = new WeiboParameters();
	// bundle.add("user_id", uid+"");
	// bundle.add("cursor", cursor+"");
	// bundle.add("count", "200");
	// bundle.add("source", Weibo.APP_KEY);
	//
	// AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
	// weiboRunner.request(this.context, SinaWeiboUtil.url_friendships_friends,
	// bundle, Utility.HTTPMETHOD_GET, listener);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw e;
	// }
	// }

	@Override
	public String getWeiboName() {
		return "腾讯微博";
	}

	@Override
	public AuthUrls getAuthUrls() throws Exception {
		// AuthUrls urls=new AuthUrls();
		// JsonPack jp=A57HttpApiV3.getInstance().getSinaBindUrls();
		// if(jp.getRe()!=200){
		// throw new Exception(jp.getMsg());
		// }
		// SinaBindUrlsData rt=SinaBindUrlsData.toBean(jp.getObj());
		// urls.authWebUrl=rt.getSinaUrl();
		// urls.redirectUrl=rt.getInterceptUrl();
		// return urls;

		SoftwareCommonData someSoftwareCommonData = SessionManager.getInstance().getSoftwareCommonData();
		// String authURl="https://open.weibo.cn/oauth2/authorize"+"?" +
		// URLEncodedUtils.format(qparams, "UTF-8");
		AuthUrls urls = new AuthUrls();
		urls.authWebUrl = someSoftwareCommonData.getQqWeiboWapUrl();
		urls.redirectUrl = someSoftwareCommonData.getQqWeiboInterceptUrl();
		return urls;
	}

	@Override
	public BindToReturnData bindTo(String token, String code, boolean forceBindTag) throws Exception {
		// JsonPack jp=ZyHttpApi.getInstance().bindToWeibo(2,code);//1:新浪 2：qq
		// if(jp.getRe()!=200){
		// throw new Exception(jp.getMsg());
		// }
		// UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(),
		// UserInfoDTO.class);
		// BindToReturnData rt=new BindToReturnData();
		// rt.setProcessTag(1);
		// rt.setUserInfo(userInfo);
		// return rt;
		return null;
	}

	@Override
	public void unbind(String token) throws Exception {
		// JsonPack jp=ZyHttpApi.getInstance().unbindWeibo(2);//1:新浪 2：qq
		// if(jp.getRe()!=200){
		// throw new Exception(jp.getMsg());
		// }
		// UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(),
		// UserInfoDTO.class);
		// if (userInfo != null) {
		// userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		// userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		// // 设置用户信息
		// SessionManager.getInstance().setUserInfo(ContextUtil.getContext(),
		// userInfo);
		// }
	}

	@Override
	public UserInfo weiboLogin(String cityId, String code) throws Exception {
		// JsonPack jp=ZyHttpApi.getInstance().userLoginByWeibo(2,code);
		// if(jp.getRe()!=200){
		// throw new Exception(jp.getMsg());
		// }
		// UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(),
		// UserInfoDTO.class);
		// if (userInfo != null) {
		// userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		// userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		// // 设置用户信息
		// SessionManager.getInstance().setUserInfo(ContextUtil.getContext(),
		// userInfo);
		// }
		return null;
	}

	@Override
	public List<User> getUserFriendsList(String token) throws Exception {
		// JsonPack jp=ZyHttpApi.getInstance().getWeiboFriendsList(2);//1:新浪
		// 2：qq
		// if(jp.getRe()!=200){
		// if (jp.getRe() == 404) {
		// throw new Exception(jp.getMsg());
		// }
		// return new ArrayList<User>();
		// }
		// WeiboFriendsListDTO rt=JsonUtils.fromJson(jp.getObj().toString(),
		// WeiboFriendsListDTO.class);
		// List<WeiboFriendsListData> list=rt.list;
		// List<User> userlist=new ArrayList<User>();
		// for(WeiboFriendsListData data : list){
		// userlist.addAll(data.list); //合并
		// }
		// return userlist;
		return null;
	}

	@Override
	public void requestWeiboShare(Runnable callBack) {// MediatorActivity
	// AuthWebActivity.currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
		AuthWebActivity.currentWeiboUtil = WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO);
		Intent intent = new Intent(ContextUtil.getContext(), MediatorActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("page", 0);
		ContextUtil.getContext().startActivity(intent);

		// 招摇不需要“登录-绑定”合一的逻辑
	}

	@Override
	public void dealWithErrorCode(int errorCode) {
		UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(context);
		if (errorCode == ERROR_CODE_UNBIND) {
			userInfo.setQqBindTag(false);
			userInfo.setQqBindRemainSecs(0);
			userInfo.setQqBindRemainSecsTimestamp(0);
			SessionManager.getInstance().setUserInfo(context, userInfo);
		} else if (errorCode == ERROR_CODE_EXPIRED) {
			userInfo.setQqBindRemainSecs(0);
			userInfo.setQqBindRemainSecsTimestamp(0);
			SessionManager.getInstance().setUserInfo(context, userInfo);
		}
	}

	public void bindSuccess(BindToReturnData returnData) {
		UserInfoDTO userInfo = returnData.getUserInfo();
		if (userInfo != null) {
			userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
			// 设置用户信息
			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
		}
	}

	@Override
	public BindToReturnData ssoBindTo(String uid, String access_token, String expires_in) throws Exception {
		return null;
	}

	@Override
	public UserInfo ssoWeiboLogin(String uid, String access_token, String expires_in) throws Exception {
		return null;
	}

	@Override
	public void loginSuccess() {
		// UserInfo2DTO userInfo =
		// SessionManager.getInstance().getUserInfo(ContextUtil.getContext());
		// if (userInfo != null) {
		// userInfo.setQqBindTag(true);
		// userInfo.setQqBindRemainSecs(1000);//本软件登录后肯定要去首页刷数据，所以这里写个假的，以提供登录成功的判断
		// userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
		// // 设置用户信息
		// SessionManager.getInstance().setUserInfo(ContextUtil.getContext(),
		// userInfo);
		// }

	}

	@Override
	public void postWeiboShare(String detail) throws Exception {
		// JsonPack jp=ZyHttpApi.getInstance().postWeiboShare(2,detail);//1:新浪
		// 2：qq
		// if(jp.getRe()!=200){
		// throw new Exception(jp.getMsg());
		// }
	}
}
