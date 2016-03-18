package com.fg114.main.service.dto;

import org.json.JSONObject;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 版本检查 DTO
 * @author qianjiefeng
 *
 */
public class SoftwareCommonData  {
	//版本检查dto
	private VersionChkDTO versionChkDto=new VersionChkDTO();
	//城市列表 dto
	private CityListDTO cityDto=new CityListDTO();
	//错误报告 dto
	private ErrorReportTypeListPackDTO errorReportTypeListDto=new ErrorReportTypeListPackDTO();
	//订单选择信息
	private OrderSelInfo orderSelInfo=new OrderSelInfo();
	
	
	
    //讯飞url server_url=http://demo.voicecloud.cn/index.htm,appid=4fc2d4c8
	private String xfUrl;
    //讯飞引擎  secretary
	private String xfEngineName;

	
	private String sinaAndroidSsoUrl="https://open.weibo.cn/oauth2/authorize?redirect_uri=http%3A%2F%2Fwww.xiaomishu.com%2Fpass%2Fbindaccount.aspx&callback_uri=sinaweibosso.732194593%3A%2F%2F&client_id=732194593&display=mobile&response_type=token";
	private String sinaAppKey="732194593";
	private String sinaWapUrl="https://api.weibo.com/oauth2/authorize?client_id=732194593&response_type=code&redirect_uri=http%3a%2f%2fwww.xiaomishu.com%2fpass%2fbindaccount.aspx%3ftype%3d1%26gohref%3dhttp%253a%252f%252fwww.xiaomishu.com%252fmember%252fdo.aspx%253faction%253dguid%2526go%253dhttp%253a%252f%252fwww.xiaomishu.com%252f&display=mobile";
	private String sinaInterceptUrl="http://www.xiaomishu.com/pass/bindaccount.aspx";

	//qq微博
	private String qqWeiboWapUrl="https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=0ea62de5874a42b3852e55a99b2d5895&response_type=code&redirect_uri=http%3a%2f%2fwww.xiaomishu.com%2fpass%2fbindaccount.aspx";
	private String qqWeiboInterceptUrl="http://www.xiaomishu.com/pass/bindaccount.aspx";

	
	//新浪微博
	private String sinaIphoneSsoUrl;
	private String sinaIpadSsoUrl;

	
	//普通请求超时时间
	private int normalRequestTimeout = 15;
	//上传请求超时时间
	private int uploadRequestTimeout = 30;
	
	//服务端时间戳    
	private long serverTimestamp;
	
	//闪屏页图片url
	private String splashPicUrl;
	
	//是否隐藏“您可能喜欢的应用模块”（目前只对android有用）
	private boolean needHideFavorite=false;
	
	//wap功能页地址
	private String wapPageUrl;  
		
   
	
	public boolean isNeedHideFavoriteApp() {
		return needHideFavorite;
	}
	public String getWapPageUrl() {
		if(CheckUtil.isEmpty(wapPageUrl)){
			wapPageUrl="http://m.xiaomishu.com/appwap/";
		}
		return wapPageUrl;
	}
	public void setWapPageUrl(String wapPageUrl) {
		this.wapPageUrl = wapPageUrl;
	}
	public void setNeedHideFavoriteApp(boolean needHideFavoriteApp) {
		this.needHideFavorite = needHideFavoriteApp;
	}
	public String getSinaAndroidSsoUrl() {
		if(CheckUtil.isEmpty(sinaAndroidSsoUrl)){
			sinaAndroidSsoUrl="https://open.weibo.cn/oauth2/authorize?redirect_uri=http%3A%2F%2Fwww.xiaomishu.com%2Fpass%2Fbindaccount.aspx&callback_uri=sinaweibosso.732194593%3A%2F%2F&client_id=732194593&display=mobile&response_type=token";
		}
		return sinaAndroidSsoUrl;
	}
	public void setSinaAndroidSsoUrl(String sinaAndroidSsoUrl) {
		this.sinaAndroidSsoUrl = sinaAndroidSsoUrl;
	}
	public String getSinaIphoneSsoUrl() {
		return sinaIphoneSsoUrl;
	}
	public void setSinaIphoneSsoUrl(String sinaIphoneSsoUrl) {
		this.sinaIphoneSsoUrl = sinaIphoneSsoUrl;
	}
	public String getSinaIpadSsoUrl() {
		return sinaIpadSsoUrl;
	}
	public void setSinaIpadSsoUrl(String sinaIpadSsoUrl) {
		this.sinaIpadSsoUrl = sinaIpadSsoUrl;
	}
	public String getSinaWapUrl() {
		if(CheckUtil.isEmpty(sinaWapUrl)){
			sinaWapUrl="https://api.weibo.com/oauth2/authorize?client_id=732194593&response_type=code&redirect_uri=http%3a%2f%2fwww.xiaomishu.com%2fpass%2fbindaccount.aspx%3ftype%3d1%26gohref%3dhttp%253a%252f%252fwww.xiaomishu.com%252fmember%252fdo.aspx%253faction%253dguid%2526go%253dhttp%253a%252f%252fwww.xiaomishu.com%252f&display=mobile";
		}
		return sinaWapUrl;
	}
	public void setSinaWapUrl(String sinaWapUrl) {
		this.sinaWapUrl = sinaWapUrl;
	}
	public String getSinaInterceptUrl() {
		if(CheckUtil.isEmpty(sinaInterceptUrl)){
			sinaInterceptUrl="http://www.xiaomishu.com/pass/bindaccount.aspx";
		}
		return sinaInterceptUrl;
	}
	public void setSinaInterceptUrl(String sinaInterceptUrl) {
		this.sinaInterceptUrl = sinaInterceptUrl;
	}
	public String getQqWeiboWapUrl() {
		if(CheckUtil.isEmpty(qqWeiboWapUrl)){
			qqWeiboWapUrl="https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=0ea62de5874a42b3852e55a99b2d5895&response_type=code&redirect_uri=http%3a%2f%2fwww.xiaomishu.com%2fpass%2fbindaccount.aspx";
		}
		return qqWeiboWapUrl;
	}
	public void setQqWeiboWapUrl(String qqWeiboWapUrl) {
		this.qqWeiboWapUrl = qqWeiboWapUrl;
	}
	public String getQqWeiboInterceptUrl() {
		if(CheckUtil.isEmpty(qqWeiboInterceptUrl)){
			qqWeiboInterceptUrl="http://www.xiaomishu.com/pass/bindaccount.aspx";
		}
		return qqWeiboInterceptUrl;
	}
	public void setQqWeiboInterceptUrl(String qqWeiboInterceptUrl) {
		this.qqWeiboInterceptUrl = qqWeiboInterceptUrl;
	}

	public String getSinaAppKey() {
		if(CheckUtil.isEmpty(sinaAppKey)){
			sinaAppKey= "732194593";
		}
		return sinaAppKey;
	}
	public void setSinaAppKey(String sinaAppKey) {
		this.sinaAppKey = sinaAppKey;
	}
	public int getNormalRequestTimeout() {
		if(normalRequestTimeout<15){
			return 15;
		}
		return normalRequestTimeout;
	}
	public void setNormalRequestTimeout(int normalRequestTimeout) {
		this.normalRequestTimeout = normalRequestTimeout;
	}
	public int getUploadRequestTimeout() {
		if(uploadRequestTimeout<20){
			return 20;
		}
		return uploadRequestTimeout;
	}
	public void setUploadRequestTimeout(int uploadRequestTimeout) {
		this.uploadRequestTimeout = uploadRequestTimeout;
	}
	public VersionChkDTO getVersionChkDto() {
		return versionChkDto;
	}
	public void setVersionChkDto(VersionChkDTO versionChkDto) {
		this.versionChkDto = versionChkDto;
	}
	public CityListDTO getCityDto() {
		return cityDto;
	}
	public void setCityDto(CityListDTO cityDto) {
		this.cityDto = cityDto;
	}
	public ErrorReportTypeListPackDTO getErrorReportTypeListDto() {
		return errorReportTypeListDto;
	}
	public void setErrorReportTypeListDto(ErrorReportTypeListPackDTO errorReportTypeListDto) {
		this.errorReportTypeListDto = errorReportTypeListDto;
	}
	public OrderSelInfo getOrderSelInfo() {
		return orderSelInfo;
	}
	public void setOrderSelInfo(OrderSelInfo orderSelInfo) {
		this.orderSelInfo = orderSelInfo;
	}
	public String getXfUrl() {
		return xfUrl;
	}
	public void setXfUrl(String xfUrl) {
		this.xfUrl = xfUrl;
	}
	public String getXfEngineName() {
		return xfEngineName;
	}
	public void setXfEngineName(String xfEngineName) {
		this.xfEngineName = xfEngineName;
	}
	public long getServerTimestamp() {
		return serverTimestamp;
	}
	public void setServerTimestamp(long serverTimestamp) {
		this.serverTimestamp = serverTimestamp;
	}
	public String getSplashPicUrl() {
		return splashPicUrl;
	}
	public void setSplashPicUrl(String splashPicUrl) {
		this.splashPicUrl = splashPicUrl;
	}
}