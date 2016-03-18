package com.fg114.main.weibo;

import java.util.List;

import android.os.SystemClock;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.dto.User;

/**
 * 微博工具类的抽象接口，提供微博各种功能的接口方法。具体的实现由不同平台包名下的类来实现 
 * @author xujianjun,2012-08-05
 *
 */

public abstract class WeiboUtil {
	
	/**
	 * @return 返回微博的名称
	 */
	public abstract String getWeiboName();
	
	/**
	 * @return 返回web auth验证需要的url对象
	 * @throws Exception 
	 */
	public abstract AuthUrls getAuthUrls() throws Exception;
	
	/**
	 * 绑定微博
	 * @param token
	 * @param code
	 * @param forceBindTag 强制绑定到这个用户
	 * @return
	 * @throws Exception 
	 */
	public abstract BindToReturnData bindTo(String token,String code,boolean forceBindTag) throws Exception;
	
	public abstract BindToReturnData ssoBindTo(String uid, String access_token, String expires_in) throws Exception;

	public abstract void bindSuccess(BindToReturnData returnData);
	public abstract void loginSuccess();
	
	
	
	/**
	 * 解绑微博
	 * @param token
	 * @return 成功返回true，失败返回false
	 * @throws Exception 
	 */
	public abstract void unbind(String token) throws Exception; 
	
	/**
	 * 通过微博登录
	 * @param cityId
	 * @param code
	 * @return
	 * @throws Exception 
	 */
	public abstract UserInfo weiboLogin(String cityId, String code) throws Exception;
	public abstract UserInfo ssoWeiboLogin(String uid, String access_token, String expires_in) throws Exception;
	
	/**
	 * 获得微博好友列表 
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	public abstract List<User> getUserFriendsList(String token) throws Exception;

	/**
	 * 请求微博分享。先判断登录，再绑定。
	 * @param token
	 */
	public abstract void requestWeiboShare(Runnable callBack);
	
	/**
	 * 发布微博分享
	 * @param detail
	 */
	public abstract void postWeiboShare(String detail) throws Exception;
	
	/**
	 * 处理微博调用分享后返回的错误代码
	 * @param errorCode
	 */
	public abstract void dealWithErrorCode(int errorCode);
	
	/**
	 * 同步用户信息。
	 * @param token
	 * @return 成功返回true
	 * @throws Exception 
	 */
	public static UserInfo syncUserInfo(String userToken) throws Exception{
		JsonPack jp=A57HttpApiV3.getInstance().syncUserInfo(userToken);
		if(jp.getRe()!=200){
			throw new Exception(jp.getMsg());
		}
		UserInfo rt=JsonUtils.fromJson(jp.getObj().toString(), UserInfo.class);
		rt.setSinaBindRemainSecsTimestamp(SystemClock.elapsedRealtime());//设置时间戳 
		SessionManager.getInstance().setUserInfo(ContextUtil.getContext(), rt);//同步后将信息放入缓存
		return rt;
	}
}
