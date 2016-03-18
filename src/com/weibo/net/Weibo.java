/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weibo.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;

/**
 * Encapsulation main Weibo APIs, Include: 1. getRquestToken , 2. getAccessToken, 3. url request.
 * Used as a single instance class. Implements a weibo api as a synchronized way.
 *
 * @author  ZhangJie (zhangjie2@staff.sina.com.cn)
 */
public class Weibo {
	

	//微博绑定返回页
	public static final String URL_BIND_SUCCESS_FLAG = "bindSuccess";
	//解析返回url的正则表达式
	public static final String URL_PATTERN = "[\\S]*" + URL_BIND_SUCCESS_FLAG + "\\?oauth_token=([^\\&]+)?\\&oauth_verifier=([\\w]+)?";
	public static final String USERID_PATTERN = "user_id=([\\w]+)?";
	
	public static final String FROM = "xweibo";
	
	public static String SERVER = "http://api.t.sina.com.cn/";
	private static String URL_OAUTH_TOKEN = "http://api.t.sina.com.cn/oauth/request_token";
//	public static String URL_AUTHORIZE = "http://api.t.sina.com.cn/oauth/authorize";
	public static String URL_ACCESS_TOKEN = "http://api.t.sina.com.cn/oauth/access_token";
	public static String URL_AUTHENTICATION = "http://api.t.sina.com.cn/oauth/authenticate";
	
//	http://api.t.sina.com.cn/oauth/authorize?oauth_token=";
	
	//微博认证信息
//	public static String APP_KEY = "2833461481";
//	public static String APP_SECRET = "54428dab9b42121c7256c552c4e9571f";
	
	// new
//	public static String APP_KEY = "732194593";
//	public static String APP_SECRET = "3b3c3ddda2016eb9562bde2761d1687f";
	
	public static String APP_KEY = "";
	public static String APP_SECRET = "";
	
	private static Weibo mWeiboInstance = null;
	private AccessToken mAccessToken = null;
	private String mUserId = "";
	private RequestToken mRequestToken = null;
	
	
	private Weibo(){
		Utility.setRequestHeader("Accept-Encoding","gzip");	
		Utility.setTokenObject(this.mRequestToken);
	}
	
	
	
	public static Weibo getInstance(){	
		if(mWeiboInstance == null){
			mWeiboInstance = new Weibo();
		}
		return mWeiboInstance;
	}
	
	//设置accessToken
	public void setAccessToken(AccessToken token){
		mAccessToken = token;
	}
	
	public AccessToken getAccessToken(){
		return this.mAccessToken;
	}
	
	public String getUserId(){
		return this.mUserId;
	}
	
	public void setupConsumerConfig(String consumer_key, String consumer_secret){
		Weibo.APP_KEY = consumer_key;
		Weibo.APP_SECRET = consumer_secret;
	}
	
	public void setRequestToken(RequestToken token){
		this.mRequestToken = token;
	}
	
	//设置oauth_verifier
	public void addOauthverifier(String verifier){
		mRequestToken.setVerifier(verifier);
	}
	
	
    /**
     * Requst sina weibo open api by get or post
     *
     * @param url
     *            Openapi request URL.
     * @param params
     *            http get or post parameters . e.g. gettimeling?max=max_id&min=min_id
     *            max and max_id is a pair of key and value for params, also the min and min_id
     * @param httpMethod
     *            http verb: e.g. "GET", "POST", "DELETE" 
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws WeiboException 
     */
	public String request(Context context, String url, WeiboParameters params, String httpMethod, AccessToken token) 
		throws WeiboException{
			Utility.setAuthorization(new RequestHeader());
			String rlt = Utility.openUrl(context, url, httpMethod, params, this.mAccessToken);
			return rlt;
	}
	
	
	/**
	 * 获取request token
	 * @param context
	 * @param key
	 * @param secret
	 * @param callback_url
	 * @return
	 * @throws WeiboException
	 */
	public RequestToken getRequestToken(Context context, String key, String secret, String callback_url) 
		throws WeiboException{
		Utility.setAuthorization(new RequestTokenHeader());
		WeiboParameters postParams = new WeiboParameters();
		postParams.add("oauth_callback", callback_url);
		String rlt;
		rlt = Utility.openUrl(context, Weibo.URL_OAUTH_TOKEN, "POST", postParams, null);
		RequestToken request = new RequestToken(rlt);
		this.mRequestToken = request;
		return request;
	}
	
	/**
	 * 获得AccessToken
	 * @param context
	 * @param requestToken
	 * @return
	 * @throws WeiboException
	 */
	public AccessToken generateAccessToken(Context context, RequestToken requestToken) 
		throws WeiboException{
		Utility.setAuthorization(new AccessTokenHeader());
		WeiboParameters authParam = new WeiboParameters();
		authParam.add("oauth_verifier", this.mRequestToken.getVerifier()/*"605835"*/);
		authParam.add("source", APP_KEY);
		String rlt = Utility.openUrl(context, Weibo.URL_ACCESS_TOKEN, "POST", authParam, this.mRequestToken);
		AccessToken accessToken = new AccessToken(rlt);
		this.mAccessToken = accessToken;
		//获得userId
		Pattern pattern = Pattern.compile(USERID_PATTERN, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(rlt);
		if (matcher.find()) {
			this.mUserId = matcher.group(1);
		}
		return accessToken;
	}
	
	/**
	 * 通过XAuth认证获取用户身份
	 * @param context
	 * @param requestToken
	 * @return
	 * @throws WeiboException
	 */
	public AccessToken getXauthAccessToken(Context context, String app_key, String app_secret, String usrname, String password)
		throws WeiboException{
		Utility.setAuthorization(new XAuthHeader());
		WeiboParameters postParams = new WeiboParameters();
		postParams.add("x_auth_username", usrname);
		postParams.add("x_auth_password", password);
		postParams.add("oauth_consumer_key", APP_KEY);
		String rlt = Utility.openUrl(context, Weibo.URL_ACCESS_TOKEN, "POST", postParams, null);
		AccessToken accessToken = new AccessToken(rlt);
		this.mAccessToken = accessToken;
		return accessToken;
	}

	public void authorizeCallBack(int requestCode, int resultCode , Intent data){
		
		
	}
	
}
