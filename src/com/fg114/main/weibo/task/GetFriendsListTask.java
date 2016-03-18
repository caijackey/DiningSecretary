package com.fg114.main.weibo.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;

import com.fg114.main.app.Settings;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.WeiboUtil;
import com.fg114.main.weibo.dto.User;
import com.google.xiaomishujson.reflect.TypeToken;

/**
 * 获得用户微博好友
 * 
 * @author xujianjun, 2012-08-07
 * 
 */
public class GetFriendsListTask extends BaseTask {

	private static final String TAG = "GetFriendsListTask";
	private static final boolean DEBUG = Settings.DEBUG;
	
	// isRefresh--是否是刷新，如果是刷新，则跳过缓存逻辑，直接去网络取数据
	private boolean isRefresh;
	private WeiboUtil weibo;
	public List<User> friendList;
	//
	String token;

	public GetFriendsListTask(Activity context, String preDialogMessage, boolean isRefresh, WeiboUtil currentWeiboUtil) {
		super(preDialogMessage, context);
		this.friendList = new ArrayList<User>();
		this.isRefresh = isRefresh;
		this.weibo = currentWeiboUtil;
		this.token = SessionManager.getInstance().getUserInfo(context).getToken();
	}

	@Override
	public JsonPack getData() throws Exception {
		
		try {
			// 如果缓存中没有取到，则从网络取
			//		if (isRefresh || !readCache()) {
			//			friendList=weibo.getUserFriendsList(token);
			//			saveCache();
			//		}
			friendList = weibo.getUserFriendsList(token);
		} catch (Exception e) {
			JsonPack r = new JsonPack();
			r.setRe(404);
			r.setMsg(e.getMessage());
			return r;
		}
		JsonPack r = new JsonPack();
		r.setRe(200);
		r.setMsg("OK");
		return r;
	}

	// 将list存入缓存
	private void saveCache() {
		if (friendList == null || friendList.size() == 0) {
			return;
		}
		// ---
		
		// 先删除后存储
		ValueCacheUtil.getInstance(context).remove(weibo.getWeiboName(), "friendList-" + Settings.VERSION_NAME + "-" + token);
		ValueCacheUtil.getInstance(context).add(weibo.getWeiboName(), "friendList-" + Settings.VERSION_NAME + "-" + token, JsonUtils.toJson(friendList), "", "", 10);// 缓存１０分钟
	}

	// 尝试读取缓存，成功返回true，否则false
	private boolean readCache() {
//		FriendListDTO dto = null;
		// 从缓存中读取
		ValueObject value = ValueCacheUtil.getInstance(context).get(weibo.getWeiboName(), "friendList-" + Settings.VERSION_NAME + "-" + token);
		if (value != null && !value.isExpired()) {
			// 缓存中有，并且没有过期
			try {
				friendList = JsonUtils.fromJson(value.getValue(),new TypeToken<List<User>>(){});
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		if (dto != null && dto.getList() != null && dto.getList().size() > 0) {
//			friendList = dto.getList();
//			return true;
//		}
		return false;
	}

	@Override
	public void onPreStart() {

	}

	@Override
	public void onStateFinish(JsonPack result) {

		try {
			// 按首字母排序
			if (friendList != null) {
				Collections.sort(friendList, new Comparator<User>() {
					public int compare(User o1, User o2) {
						return o1.getPinyin().compareToIgnoreCase(o2.getPinyin());
					}
				});
				// 读取最近联系人，添加到最上面
				//addRecentFriend();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 添加最近联系人
	private void addRecentFriend() {
//		FriendListDTO dto = null;
		List<User> recentfriendList=null;
		// 从缓存中读取
		ValueObject value = ValueCacheUtil.getInstance(context).get(weibo.getWeiboName(), "recentFriendList-" + Settings.VERSION_NAME + "-" + token);
		if (value != null && !value.isExpired()) {
			// 缓存中有，并且没有过期
			try {
				recentfriendList = JsonUtils.fromJson(value.getValue(), new TypeToken<List<User>>(){});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (recentfriendList != null &&  recentfriendList.size() > 0) {
			friendList.addAll(0, recentfriendList);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		String msg = result.getMsg();
		DialogUtil.showToast(context, msg);
	}
}
