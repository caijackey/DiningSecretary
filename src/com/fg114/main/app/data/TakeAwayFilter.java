package com.fg114.main.app.data;


/**
 * 筛选条件缓存
 * @author zhangyifan
 *
 */
public class TakeAwayFilter{
	
	private boolean haveGpsRectTag=false; //是否有gps矩形
	private int gpsTypeTag=1; //经纬度类别  1:原生 2：百度 3：google
	private String gpsRect = "";		//gps矩形   规则   左下点+右上点  例子：  121.495743,31.252139;121.542435,31.217499
	private double longitude = 0;	//经度  如果为0的话是全城搜索
	private double latitude = 0;	//纬度  如果为0的话是全城搜索
	
	private String keywords="";		//搜索关键词   如果不为空  忽略longitude，latitude 
	private String typeId = ""; 	//外卖餐厅类别id  默认为空
	private String sendLimitId = "";  //起送类别id 默认为空
	//
	private boolean isSelectedPoi=false;//是否是手动选择的位置
	private String poiName=""; //poi名称

	public boolean isSelectedPoi() {
		return isSelectedPoi;
	}

	public void setSelectedPoi(boolean isSelectedPoi) {
		this.isSelectedPoi = isSelectedPoi;
	}

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}

	public TakeAwayFilter() {}
	
	/**
	 * 重置
	 */
	public void reset(){
		haveGpsRectTag=false; //是否有gps矩形
		gpsTypeTag=1;//经纬度类别  1:原生 2：百度 3：google
		gpsRect = "";		//gps矩形   规则   左下点+右上点  例子：  121.495743,31.252139;121.542435,31.217499
		longitude = 0;	//经度  如果为0的话是全城搜索
		latitude = 0;	//纬度  如果为0的话是全城搜索
		keywords="";		//搜索关键词   如果不为空  忽略longitude，latitude 
		typeId = ""; 	//外卖餐厅类别id  默认为空
		sendLimitId = "";  //起送类别id 默认为空
		isSelectedPoi=false;//是否是手动选择的位置
		poiName=""; //poi名称
	}

	public boolean isHaveGpsRectTag() {
		return haveGpsRectTag;
	}

	public void setHaveGpsRectTag(boolean haveGpsRectTag) {
		this.haveGpsRectTag = haveGpsRectTag;
	}

	public String getGpsRect() {
		return gpsRect;
	}

	public void setGpsRect(String gpsRect) {
		this.gpsRect = gpsRect;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getGpsTypeTag() {
		return gpsTypeTag;
	}

	public void setGpsTypeTag(int gpsTypeTag) {
		this.gpsTypeTag = gpsTypeTag;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getSendLimitId() {
		return sendLimitId;
	}

	public void setSendLimitId(String sendLimitId) {
		this.sendLimitId = sendLimitId;
	}
}
