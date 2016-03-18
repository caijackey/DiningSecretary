package com.fg114.main.service.dto;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;




public class MealComboData implements Serializable  {

	//ID 
	private String uuid;
	//套餐名称
	private String name;
	//简短描述
	private String shortDescribe;
	//小图片url
	private String smallPicUrl;
	//大图片url
	private String bigPicUrl;
	//销售价
	private double unitPriceNum;
	//原价
	private double oldUnitPriceNum;
	//餐馆id
	private String restId;
	//餐馆名称
	private String restName;
	 //剩余张数
    private int remainNum;
    //地点
    private String place;
    //剩余秒数
    private long remainSeconds;
    //详细信息页面url
    private String detailUrl;
    //提示
    private String hint;
	//状态
    private int stateTag;  //1:可以购买  2：已过期  3：已卖光
    //购买须知
    private String buyInfo;
    
    // v3.1.38
    //适用餐厅
    private String forRests;


	//时间戳，客户端使用来控制团购倒计时的准确性（与remainSeconds作一定的运算，算出真正的倒计时间）
    //规定：大于０的时间戳才有效，记录了从系统启动到某个时刻的毫秒数
    private long timestamp=-1;
    private String strUnitPriceNum = "";
    private String strOldUnitPriceNum = "";
	
	//get,set-------------------------------------------------------------------
    
    
	public String getUuid() {
		return uuid;
	}
	public String getForRests() {
		return forRests;
	}
	public void setForRests(String forRests) {
		this.forRests = forRests;
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
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public String getBuyInfo() {
		return buyInfo;
	}
	public void setBuyInfo(String buyInfo) {
		this.buyInfo = buyInfo;
	}
	public int getStateTag() {
		return stateTag;
	}
	public void setStateTag(int stateTag) {
		this.stateTag = stateTag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	public int getRemainNum() {
		return remainNum;
	}
	public void setRemainNum(int remainNum) {
		this.remainNum = remainNum;
	}
	public String getShortDescribe() {
		return shortDescribe;
	}
	public void setShortDescribe(String shortDescribe) {
		this.shortDescribe = shortDescribe;
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
	public String getDetailUrl() {
		return detailUrl;
	}
	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static MealComboData toBean(JSONObject jObj) {
		
		MealComboData dto = new MealComboData();

		try {
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("shortDescribe")) {
				dto.setShortDescribe(jObj.getString("shortDescribe"));
			}
			if (jObj.has("smallPicUrl")) {
				dto.setSmallPicUrl(jObj.getString("smallPicUrl"));
			}
			if (jObj.has("bigPicUrl")) {
				dto.setBigPicUrl(jObj.getString("bigPicUrl"));
			}
			if (jObj.has("unitPriceNum")) {
				dto.setUnitPriceNum(jObj.getDouble("unitPriceNum"));
			}
			if (jObj.has("oldUnitPriceNum")) {
				dto.setOldUnitPriceNum(jObj.getDouble("oldUnitPriceNum"));
			}
			if (jObj.has("restId")) {
				dto.setRestId(jObj.getString("restId"));
			}
			if (jObj.has("restName")) {
				dto.setRestName(jObj.getString("restName"));
			}
		    if (jObj.has("remainNum")) {
		    	dto.setRemainNum(jObj.getInt("remainNum"));
		    }
		    if (jObj.has("place")) {
				dto.setPlace(jObj.getString("place"));
			}
		    if (jObj.has("remainSeconds")) {
		    	dto.setRemainSeconds(jObj.getLong("remainSeconds"));
		    }
		    if (jObj.has("detailUrl")) {
				dto.setDetailUrl(jObj.getString("detailUrl"));
			}
		    if (jObj.has("hint")) {
				dto.setHint(jObj.getString("hint"));
			}			
		    if (jObj.has("stateTag")) {
		    	dto.setStateTag(jObj.getInt("stateTag"));
		    }
			if (jObj.has("buyInfo")) {
				dto.setBuyInfo(jObj.getString("buyInfo"));
			}
			if (jObj.has("forRests")) {
				dto.setForRests(jObj.getString("forRests"));
			}
			if (jObj.has("forRests")) {
				dto.setForRests(jObj.getString("forRests"));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dto;
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
