package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgChkData implements Serializable  {
	//是否有新的消息
    boolean haveMsgTag;
    //标题
	String title = "";
	//客户端刷新时间间隔  毫秒   3000
	int clientRefreshInterval;
	
	public boolean isHaveMsgTag() {
		return haveMsgTag;
	}
	public void setHaveMsgTag(boolean haveMsgTag) {
		this.haveMsgTag = haveMsgTag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getClientRefreshInterval() {
		return clientRefreshInterval;
	}
	public void setClientRefreshInterval(int clientRefreshInterval) {
		this.clientRefreshInterval = clientRefreshInterval;
	}
}
