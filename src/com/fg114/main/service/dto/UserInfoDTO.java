package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;

import com.fg114.main.util.ActivityUtil;

/**
 * 用户登录信息取得用
 * 
 * @author zhangyifan
 * 
 */
public class UserInfoDTO {
	// 用户ID
	String uuid = "";
	// 用户姓名
	String nickName = "";
	
	//用户姓名(用于下单页面)
	String trueName;
	// 手机号
	String tel = "";
	// 用户token
	String token = "";
	// 头像
	String picUrl = "";
	// 用户性别
	int sexTag; // 性别 选填 默认为 1 约定 1：先生 0：女士
	
	//总计消息数量
	public int totalMailNum;

	// 积分
	int pointNum;
	// 用户级别 1:普通会员 2：白银会员 3：黄金会员 4：铂金会员 5:钻石会员
	int level;
	
	//余额
	double remainMoney;
	//升级百分比  0~100直接
	int nextLevelPct;
	//升级提示
	String nextLevelHint;

	// 是否已经绑定了新浪微博
	boolean sinaBindTag;
	// 新浪微博账号
	String sinaAccount = "";
	// 新浪微博过期时间
	long sinaBindRemainSecs;
	// 时间戳记（毫秒）
	protected long sinaBindRemainSecsTimestamp = 0;

	// 是否已经绑定了qq微博
	boolean qqBindTag;
	// 新浪微博账号
	String qqAccount = "";
	// qq微博过期时间
	long qqBindRemainSecs;
	// 时间戳记（毫秒）
	protected long qqBindRemainSecsTimestamp = 0;

	// get,set-------------------------------------------------------------------
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public int getSexTag() {
		return sexTag;
	}

	public void setSexTag(int sexTag) {
		this.sexTag = sexTag;
	}

	public int getPointNum() {
		return pointNum;
	}

	public void setPointNum(int pointNum) {
		this.pointNum = pointNum;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isSinaBindTag() {
		return sinaBindTag;
	}

	public void setSinaBindTag(boolean sinaBindTag) {
		this.sinaBindTag = sinaBindTag;
	}

	public String getSinaAccount() {
		return sinaAccount;
	}

	public void setSinaAccount(String sinaAccount) {
		this.sinaAccount = sinaAccount;
	}

	public long getSinaBindRemainSecs() {
		return sinaBindRemainSecs;
	}

	public void setSinaBindRemainSecs(long sinaBindRemainSecs) {
		this.sinaBindRemainSecs = sinaBindRemainSecs;
	}

	public long getSinaBindRemainSecsTimestamp() {
		return sinaBindRemainSecsTimestamp;
	}

	public void setSinaBindRemainSecsTimestamp(long sinaBindRemainSecsTimestamp) {
		this.sinaBindRemainSecsTimestamp = sinaBindRemainSecsTimestamp;
	}

	public boolean isQqBindTag() {
		return qqBindTag;
	}

	public void setQqBindTag(boolean qqBindTag) {
		this.qqBindTag = qqBindTag;
	}

	public String getQqAccount() {
		return qqAccount;
	}

	public void setQqAccount(String qqAccount) {
		this.qqAccount = qqAccount;
	}

	public long getQqBindRemainSecs() {
		return qqBindRemainSecs;
	}

	public void setQqBindRemainSecs(long qqBindRemainSecs) {
		this.qqBindRemainSecs = qqBindRemainSecs;
	}

	public long getQqBindRemainSecsTimestamp() {
		return qqBindRemainSecsTimestamp;
	}

	public void setQqBindRemainSecsTimestamp(long qqBindRemainSecsTimestamp) {
		this.qqBindRemainSecsTimestamp = qqBindRemainSecsTimestamp;
	}

	// public boolean isSinaWeiboExpired(){
	// return false;
	// }
	// public boolean isQQWeiboExpired(){
	// return false;
	// }
	public boolean isSinaWeiboExpired() {
		return sinaBindRemainSecs <= (SystemClock.elapsedRealtime() - sinaBindRemainSecsTimestamp) / 1000.0;
	}

	public boolean isQQWeiboExpired() {
		return qqBindRemainSecs <= (SystemClock.elapsedRealtime() - qqBindRemainSecsTimestamp) / 1000.0;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	
	public double getRemainMoney() {
		return remainMoney;
	}

	public void setRemainMoney(double remainMoney) {
		this.remainMoney = remainMoney;
	}

	public int getNextLevelPct() {
		return nextLevelPct;
	}

	public void setNextLevelPct(int nextLevelPct) {
		this.nextLevelPct = nextLevelPct;
	}

	public String getNextLevelHint() {
		return nextLevelHint;
	}

	public void setNextLevelHint(String nextLevelHint) {
		this.nextLevelHint = nextLevelHint;
	}
}