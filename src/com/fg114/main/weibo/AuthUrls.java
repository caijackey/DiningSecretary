package com.fg114.main.weibo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Administrator
 *
 */
public class AuthUrls {
	/**
	 * 去webview授权的url
	 */
	public String authWebUrl="";
	/**
	 * 授权后重定向回来的url
	 */
	public String redirectUrl="";
	/**
	 * json to bean
	 * 
	 * @param jObj
	 * @return
	 */
	public static AuthUrls toBean(JSONObject jObj) {

		AuthUrls dto = new AuthUrls();

		try {

			if (jObj.has("authWebUrl")) {
				dto.authWebUrl=jObj.getString("authWebUrl");
			}
			if (jObj.has("redirectUrl")) {
				dto.redirectUrl=jObj.getString("redirectUrl");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dto;
	}
}
