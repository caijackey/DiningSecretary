package com.fg114.main.service.dto;



/**
 * 评论数据
 * @author qianjiefeng
 *
 */
public class VersionChkDTO  {
	//是否有新版本
	public boolean haveNewVersionTag;
	//是否需要强制更新
	public boolean needForceUpdateTag;
	//新版本号
	public String newVersion;
	//新特性
	public String info;
	//最新版下载地址
	public String downloadUrl;
}
