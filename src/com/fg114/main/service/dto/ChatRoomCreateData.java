package com.fg114.main.service.dto;

import java.util.List;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatRoomCreateData  {
	
	//是否可以提供在线服务
	boolean canOnlineServiceTag;
	//不能在线服务给出提示
	String msg = "";
	
	//客户端刷新时间间隔  毫秒   3000
	int clientRefreshInterval;
	//消息列表
	ChatMsgListDto msgListDto;
	//时间戳
	long timestamp;
	
	public boolean isCanOnlineServiceTag() {
		return canOnlineServiceTag;
	}
	public void setCanOnlineServiceTag(boolean canOnlineServiceTag) {
		this.canOnlineServiceTag = canOnlineServiceTag;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getClientRefreshInterval() {
		return clientRefreshInterval;
	}
	public void setClientRefreshInterval(int clientRefreshInterval) {
		this.clientRefreshInterval = clientRefreshInterval;
	}
	public ChatMsgListDto getMsgListDto() {
		return msgListDto;
	}
	public void setMsgListDto(ChatMsgListDto msgListDto) {
		this.msgListDto = msgListDto;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
