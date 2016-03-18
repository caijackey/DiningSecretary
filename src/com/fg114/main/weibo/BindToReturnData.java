package com.fg114.main.weibo;

import com.fg114.main.service.dto.UserInfoDTO;


/**
 * 绑定微博后返回对象
 * @author xujianjun,2012-08-05
 *
 */
public class BindToReturnData {
	//处理类别    1：绑定成功     2：该微博已经和其他帐号绑定过
	protected int processTag;
	//提示
	protected String hint;
	//微博过期时间  如果 processTag=1 时返回
	protected long bindRemainSecs;
	
	protected UserInfoDTO userInfo;
	
	//get,set-------------------------------------------------------------------

	public int getProcessTag() {
		return processTag;
	}
	public void setProcessTag(int processTag) {
		this.processTag = processTag;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public long getBindRemainSecs() {
		return bindRemainSecs;
	}
	public void setBindRemainSecs(long bindRemainSecs) {
		this.bindRemainSecs = bindRemainSecs;
	}
	public UserInfoDTO getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfoDTO userInfo) {
		this.userInfo = userInfo;
	}
}
