package com.fg114.main.service.dto;


/**
 * 首页菜单对象
 * @author qianjiefeng
 *
 */
public class MainPageAdvData  {
	//广告id
	public String uuid="";
	//广告类别   1:广告链接  2：本地连接   3:普通链接  4:软件连接
	public int typeTag;
	//广告标题
	public String title="";
	//广告url
	public String advUrl="";
	//过期时间
	public long endDate;
	//图片url
	public String picUrl="";
	//软件名称 typeTag:4时  有用
	public String appName="";
	//软件下载地址  typeTag:4时  有用
	public String appDownloadUrl="";
	

}
