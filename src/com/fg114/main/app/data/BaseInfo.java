package com.fg114.main.app.data;


public class BaseInfo {
	
	//时间戳(本地上次更新时间)
	private long lastUpdateTime = 0;
	//时间戳
	private long timestamp = 0;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

}
