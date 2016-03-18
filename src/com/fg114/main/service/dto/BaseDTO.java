package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * DTO基类  
 * @author qianjiefeng
 *
 */
public class BaseDTO {
	//是否需要更新
	public boolean needUpdateTag = true;
	//时间戳
	public long timestamp;
	//页面信息  可以为null
	public PgInfo pgInfo = new PgInfo();

}
