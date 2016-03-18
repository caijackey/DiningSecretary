package com.fg114.main.weibo.sina;

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

public class SinaWeiboUtil extends WeiboUtil{
	
	private static final int ERROR_CODE_UNBIND = 101; // 新浪微博未绑定
	private static final int ERROR_CODE_EXPIRED = 102; // 微博已过期,过期时更新本地缓存的过期时间sinaBindRemainSecs为0
	
	private Context context;
	private static SinaWeiboUtil instance;
	//------------------------------------
	static {
		instance = new SinaWeiboUtil();
		instance.init();
	}
	//------------------------------------
	//private static final String url_friendships_friends="https://api.weibo.com/2/friendships/friends.json";
	private static final String url_friendships_friends="http://api.t.sina.com.cn/statuses/friends.json";
														 
	//------------------------------------
	
	private SinaWeiboUtil(){		
	}
	private void init(){
		this.context = ContextUtil.getContext();
	}
	public static SinaWeiboUtil getInstance() {
		return instance;
	}

	@Override
	public String getWeiboName() {
		return "新浪微博";
	}
	@Override
	public AuthUrls getAuthUrls() throws Exception {
		SoftwareCommonData someSoftwareCommonData=SessionManager.getInstance().getSoftwareCommonData();
		//String authURl="https://open.weibo.cn/oauth2/authorize"+"?" + URLEncodedUtils.format(qparams, "UTF-8");
		AuthUrls urls=new AuthUrls();
		urls.authWebUrl=someSoftwareCommonData.getSinaWapUrl();
		urls.redirectUrl=someSoftwareCommonData.getSinaInterceptUrl();
		return urls;
	}

	@Override
	public BindToReturnData bindTo(String token, String code, boolean forceBindTag) throws Exception {
//		JsonPack jp=ZyHttpApi.getInstance().bindToWeibo(1,code);//1:新浪  2：qq
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
//		UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(), UserInfoDTO.class);
//		BindToReturnData rt=new BindToReturnData();
//		rt.setProcessTag(1);
//		rt.setUserInfo(userInfo);
//		return rt;
		return null;
	}
	@Override
	public void unbind(String token) throws Exception {
//		JsonPack jp=ZyHttpApi.getInstance().unbindWeibo(1);//1:新浪  2：qq
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
//		UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(), UserInfoDTO.class);
//		if (userInfo != null) {
//			userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			// 设置用户信息
//			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
//		}
	}
	@Override
	public UserInfo weiboLogin(String cityId, String code) throws Exception {
//		JsonPack jp=ZyHttpApi.getInstance().userLoginByWeibo(1,code);
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
//		UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(), UserInfoDTO.class);
//		if (userInfo != null) {
//			userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			// 设置用户信息
//			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
//		}
		return null;
	}
	@Override
	public List<User> getUserFriendsList(String token) throws Exception {
		
//		
//		JsonPack jp=ZyHttpApi.getInstance().getWeiboFriendsList(1);//1:新浪  2：qq
//		if(jp.getRe()!=200){
//			if (jp.getRe() == 404) {
//				throw new Exception(jp.getMsg());
//			}
//			return new ArrayList<User>();
//		}
//		WeiboFriendsListDTO rt=JsonUtils.fromJson(jp.getObj().toString(), WeiboFriendsListDTO.class);
//		List<WeiboFriendsListData> list=rt.list;
//		List<User> userlist=new ArrayList<User>();
//		for(WeiboFriendsListData data : list){
//			userlist.addAll(data.list); //合并
//		}
//		
//		return userlist;
		return null;
	}
	
	@Override
	public void requestWeiboShare(Runnable callBack) {//MediatorActivity
//		AuthWebActivity.currentWeiboUtil=WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
		Intent intent=new Intent(ContextUtil.getContext(), MediatorActivity.class);
		SinaSSOAuthActivity.currentWeiboUtil = WeiboUtilFactory
				.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("page", 1);
		ContextUtil.getContext().startActivity(intent);
		//招摇不需要“登录-绑定”合一的逻辑
	}
	
	@Override
	public void dealWithErrorCode(int errorCode) {
//		UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(context);
//		if (errorCode == ERROR_CODE_UNBIND) {
//			userInfo.setSinaBindTag(false);
//			userInfo.setSinaBindRemainSecs(0);
//			userInfo.setSinaBindRemainSecsTimestamp(0);
//			SessionManager.getInstance().setUserInfo(context, userInfo);
//		} else if (errorCode == ERROR_CODE_EXPIRED) {
//			userInfo.setSinaBindRemainSecs(0);
//			userInfo.setSinaBindRemainSecsTimestamp(0);
//			SessionManager.getInstance().setUserInfo(context, userInfo);
//		}
	}
	public void bindSuccess(BindToReturnData returnData) {
//		UserInfoDTO userInfo = returnData.getUserInfo();
//		if (userInfo != null) {
//			userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			// 设置用户信息
//			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
//		}
	}
	@Override
	public BindToReturnData ssoBindTo(String uid, String access_token, String expires_in) throws Exception {
//		JsonPack jp=ZyHttpApi.getInstance().bindToSinaWeiboByAccessToken(uid, access_token, Integer.parseInt(expires_in));
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
//		//--
//		UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(), UserInfoDTO.class);
//		BindToReturnData rt=new BindToReturnData();
//		rt.setProcessTag(1);
//		rt.setUserInfo(userInfo);
//		return rt;
		return null;
	}
	
	@Override
	public UserInfo ssoWeiboLogin(String uid, String access_token, String expires_in) throws Exception {
//		JsonPack jp=ZyHttpApi.getInstance().userLoginBySinaWeiboAccessToken(uid, access_token, Integer.parseInt(expires_in));
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
//		UserInfoDTO userInfo=JsonUtils.fromJson(jp.getObj().toString(), UserInfoDTO.class);
//		if (userInfo != null) {
//			userInfo.setQqBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			// 设置用户信息
//			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
//		}
		return null;
	}
	@Override
	public void loginSuccess() {
//		UserInfo2DTO userInfo = SessionManager.getInstance().getUserInfo(ContextUtil.getContext());
//		if (userInfo != null) {
//			userInfo.setSinaBindTag(true);
//			userInfo.setSinaBindRemainSecs(1000);//本软件登录后肯定要去首页刷数据，所以这里写个假的，以提供登录成功的判断
//			userInfo.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());
//			// 设置用户信息
//			SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), userInfo);
//		}
	}
	@Override
	public void postWeiboShare(String detail) throws Exception{
//		JsonPack jp=ZyHttpApi.getInstance().postWeiboShare(1,detail);//1:新浪  2：qq
//		if(jp.getRe()!=200){
//			throw new Exception(jp.getMsg());
//		}
	}

}

