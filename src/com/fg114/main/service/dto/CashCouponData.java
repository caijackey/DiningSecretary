package com.fg114.main.service.dto;

import java.io.Serializable;
import java.text.DecimalFormat;

import android.text.TextUtils;




public class CashCouponData  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	//ID 
	public String uuid;
	//套餐名称
	public String name;
	//简短描述 
	public String shortDescribe;
	//小图片url
	public String smallPicUrl;
	//大图片url
	public String bigPicUrl;
	//销售价
	public double unitPriceNum;
	//原价
	public double oldUnitPriceNum;
	//餐馆id
	public String restId;
	//餐馆名称
	public String restName;
    //剩余张数
	public Integer remainNum;
	//卖出的张数
    public int soldNum;
    //地点  (列表页显示的 餐厅对应的商区)
	public String place;
    //剩余秒数
	public long remainSeconds;
    //详细信息页面url  （这个字段作废了）
	public String detailUrl;
    //提示 （详情页图片上面的提示文字）
	public String hint;
	//状态
	public int stateTag;  //1:可以购买  2：已过期  3：已卖光  4:即将开始
    //购买须知   (详情页展示，列表页不返回)
	public String buyInfo;
    //适用餐厅  (详情页展示，列表页不返回)
	public String forRests;
	
	
	//时间戳，客户端使用来控制团购倒计时的准确性（与remainSeconds作一定的运算，算出真正的倒计时间）
    //规定：大于０的时间戳才有效，记录了从系统启动到某个时刻的毫秒数
    transient private long timestamp=-1;
    transient private String strUnitPriceNum = "";
    transient private String strOldUnitPriceNum = "";
    
    
	// ------------------------------
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortDescribe() {
		return shortDescribe;
	}
	public void setShortDescribe(String shortDescribe) {
		this.shortDescribe = shortDescribe;
	}
	public String getSmallPicUrl() {
		return smallPicUrl;
	}
	public void setSmallPicUrl(String smallPicUrl) {
		this.smallPicUrl = smallPicUrl;
	}
	public String getBigPicUrl() {
		return bigPicUrl;
	}
	public void setBigPicUrl(String bigPicUrl) {
		this.bigPicUrl = bigPicUrl;
	}
	public double getUnitPriceNum() {
		return unitPriceNum;
	}
	public void setUnitPriceNum(double unitPriceNum) {
		this.unitPriceNum = unitPriceNum;
		setStrUnitPriceNum(formatPrice(unitPriceNum));
	}
	public double getOldUnitPriceNum() {
		return oldUnitPriceNum;
	}
	public void setOldUnitPriceNum(double oldUnitPriceNum) {
		this.oldUnitPriceNum = oldUnitPriceNum;
		setStrOldUnitPriceNum(formatPrice(oldUnitPriceNum));
	}
	public String getRestId() {
		return restId;
	}
	public void setRestId(String restId) {
		this.restId = restId;
	}
	public String getRestName() {
		return restName;
	}
	public void setRestName(String restName) {
		this.restName = restName;
	}
	public Integer getRemainNum() {
		return remainNum;
	}
	public void setRemainNum(Integer remainNum) {
		this.remainNum = remainNum;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public long getRemainSeconds() {
		return remainSeconds;
	}
	public void setRemainSeconds(long remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	public String getDetailUrl() {
		return detailUrl;
	}
	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public int getStateTag() {
		return stateTag;
	}
	public void setStateTag(int stateTag) {
		this.stateTag = stateTag;
	}
	public String getBuyInfo() {
		return buyInfo;
	}
	public void setBuyInfo(String buyInfo) {
		this.buyInfo = buyInfo;
	}
	public String getForRests() {
		return forRests;
	}
	public void setForRests(String forRests) {
		this.forRests = forRests;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getStrUnitPriceNum() {
		if (TextUtils.isEmpty(strUnitPriceNum)) {
			setStrUnitPriceNum(formatPrice(unitPriceNum));
		}
		return strUnitPriceNum;
	}
	public void setStrUnitPriceNum(String strUnitPriceNum) {
		this.strUnitPriceNum = strUnitPriceNum;
	}
	public String getStrOldUnitPriceNum() {
		if (TextUtils.isEmpty(strOldUnitPriceNum)) {
			setStrOldUnitPriceNum(formatPrice(oldUnitPriceNum));
		}
		return strOldUnitPriceNum;
	}
	public void setStrOldUnitPriceNum(String strOldUnitPriceNum) {
		this.strOldUnitPriceNum = strOldUnitPriceNum;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	private String formatPrice(double price) {
		DecimalFormat decimalFormatDouble = new DecimalFormat("#########.##");
		String strPrice = decimalFormatDouble.format(price);
		if (strPrice.endsWith(".00")) {
			strPrice = String.valueOf(Double.valueOf(price).intValue());
		}
		return strPrice;
	}
}
