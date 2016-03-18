package com.fg114.main.service.dto;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author qianjiefeng
 * 
 */
/**
 * @author Administrator
 *
 */
public class UserCashCouponData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8032417123139868024L;
	// uuid
	private String uuid;
	// 餐馆id
	private String restId;
	// 餐馆名称
	private String restName;
	// 总额
	private String totalNum;
	// 剩余
	private String remainNum;
	// 现金券获取时间
	private long couponCreateTime;
	// 使用开始时间
	private long userBeginTime;
	// 使用结束时间
	private long userEndTime;
	// 状态名称
	private String stateName;
	// 面额
	private String couponValue;
	// 序列号
	private String serialNum;
	// 现金券密码
	private String couponPwd;
	// 使用说明
	private String useDescription;
	// 温馨提示
	private String useHint;
	// 订单号
	private String orderNum;
	// 单价
	private String unitPrice;
	// 购买数量
	private int buyNum;
	// 实付
	private String actualPay;
	// 下单时间
	private long orderCreateTime;
	// 付款时间 0为未付款
	private long payTime;
	// 最后一条信息
	private String detail;
	//是否可以查看
	private boolean canViewTag;
	//是否可以付款
	private boolean canPayTag;
	
	// v3.1.35
	//类别 1:现金券  2：套餐
	private int typeTag;
	//套餐名称
	private String name;

	// get,set-------------------------------------------------------------------
	
	public boolean isCanPayTag() {
		return canPayTag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTypeTag() {
		return typeTag;
	}

	public void setTypeTag(int typeTag) {
		this.typeTag = typeTag;
	}

	public void setCanPayTag(boolean canPayTag) {
		this.canPayTag = canPayTag;
	}

	public String getDetail() {
		return detail;
	}

	public boolean isCanViewTag() {
		return canViewTag;
	}

	public void setCanViewTag(boolean canViewTag) {
		this.canViewTag = canViewTag;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getRestId() {
		return restId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public String getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(String totalNum) {
		this.totalNum = totalNum;
	}

	public String getRemainNum() {
		return remainNum;
	}

	public void setRemainNum(String remainNum) {
		this.remainNum = remainNum;
	}

	public long getCouponCreateTime() {
		return couponCreateTime;
	}

	public void setCouponCreateTime(long couponCreateTime) {
		this.couponCreateTime = couponCreateTime;
	}

	public long getUserBeginTime() {
		return userBeginTime;
	}

	public void setUserBeginTime(long userBeginTime) {
		this.userBeginTime = userBeginTime;
	}

	public long getUserEndTime() {
		return userEndTime;
	}

	public void setUserEndTime(long userEndTime) {
		this.userEndTime = userEndTime;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getCouponValue() {
		return couponValue;
	}

	public void setCouponValue(String couponValue) {
		this.couponValue = couponValue;
	}

	public String getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(String serialNum) {
		this.serialNum = serialNum;
	}

	public String getCouponPwd() {
		return couponPwd;
	}

	public void setCouponPwd(String couponPwd) {
		this.couponPwd = couponPwd;
	}

	public String getUseDescription() {
		return useDescription;
	}

	public void setUseDescription(String useDescription) {
		this.useDescription = useDescription;
	}

	public String getUseHint() {
		return useHint;
	}

	public void setUseHint(String useHint) {
		this.useHint = useHint;
	}

	public String getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getBuyNum() {
		return buyNum;
	}

	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}

	public String getActualPay() {
		return actualPay;
	}

	public void setActualPay(String actualPay) {
		this.actualPay = actualPay;
	}

	public long getOrderCreateTime() {
		return orderCreateTime;
	}

	public void setOrderCreateTime(long orderCreateTime) {
		this.orderCreateTime = orderCreateTime;
	}

	public long getPayTime() {
		return payTime;
	}

	public void setPayTime(long payTime) {
		this.payTime = payTime;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static UserCashCouponData toBean(JSONObject jObj) {
		
		UserCashCouponData dto = new UserCashCouponData();
		
		try {
			
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("restId")) {
				dto.setRestId(jObj.getString("restId"));
			}
			if (jObj.has("restName")) {
				dto.setRestName(jObj.getString("restName"));
			}
			if (jObj.has("totalNum")) {
				dto.setTotalNum(jObj.getString("totalNum"));
			}
			if (jObj.has("remainNum")) {
				dto.setRemainNum(jObj.getString("remainNum"));
			}
			if (jObj.has("couponCreateTime")) {
				dto.setCouponCreateTime(jObj.getLong("couponCreateTime"));
			}
			if (jObj.has("userBeginTime")) {
				dto.setUserBeginTime(jObj.getLong("userBeginTime"));
			}
			if (jObj.has("userEndTime")) {
				dto.setUserEndTime(jObj.getLong("userEndTime"));
			}
			if (jObj.has("stateName")) {
				dto.setStateName(jObj.getString("stateName"));
			}
			if (jObj.has("couponValue")) {
				dto.setCouponValue(jObj.getString("couponValue"));
			}
			if (jObj.has("serialNum")) {
				dto.setSerialNum(jObj.getString("serialNum"));
			}
			if (jObj.has("couponPwd")) {
				dto.setCouponPwd(jObj.getString("couponPwd"));
			}
			if (jObj.has("useDescription")) {
				dto.setUseDescription(jObj.getString("useDescription"));
			}
			if (jObj.has("useHint")) {
				dto.setUseHint(jObj.getString("useHint"));
			}
			if (jObj.has("orderNum")) {
				dto.setOrderNum(jObj.getString("orderNum"));
			}
			if (jObj.has("unitPrice")) {
				dto.setUnitPrice(jObj.getString("unitPrice"));
			}
			if (jObj.has("buyNum")) {
				dto.setBuyNum(jObj.getInt("buyNum"));
			}
			if (jObj.has("actualPay")) {
				dto.setActualPay(jObj.getString("actualPay"));
			}
			if (jObj.has("orderCreateTime")) {
				dto.setOrderCreateTime(jObj.getLong("orderCreateTime"));
			}
			if (jObj.has("payTime")) {
				dto.setPayTime(jObj.getLong("payTime"));
			}
			if(jObj.has("canViewTag"))
			{
				dto.setCanViewTag(jObj.getBoolean("canViewTag"));
			}
			if(jObj.has("canPayTag"))
			{
				dto.setCanPayTag(jObj.getBoolean("canPayTag"));
			}
			if(jObj.has("typeTag"))
			{
				dto.setTypeTag(jObj.getInt("typeTag"));
			}
			if(jObj.has("name"))
			{
				dto.setName(jObj.getString("name"));
			}		
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}

}
