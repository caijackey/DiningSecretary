package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.ActivityUtil;

/**
 * xxx
 * @author qianjiefeng
 */
public class RoomStateAndOrderInfo {
	// 房间状态

	RoomState roomState;

	// 是否可以参加航空活动
	boolean canJoinAirPlanePromotionTag;
	// 活动id
	String apId;
	// 活动标题
	String apTitle;
	// 活动的输入提示
	String apPlaceHolder;
	// 活动说明
	String apDetail;

	// 是否有房间提示
	boolean haveRoomHintTag;
	// 房间提示
	String roomHint = "";

	public RoomState getRoomState() {
		return roomState;
	}

	public void setRoomState(RoomState roomState) {
		this.roomState = roomState;
	}

	public boolean isCanJoinAirPlanePromotionTag() {
		return canJoinAirPlanePromotionTag;
	}

	public void setCanJoinAirPlanePromotionTag(boolean canJoinAirPlanePromotionTag) {
		this.canJoinAirPlanePromotionTag = canJoinAirPlanePromotionTag;
	}

	public String getApId() {
		return apId;
	}

	public void setApId(String apId) {
		this.apId = apId;
	}

	public String getApTitle() {
		return apTitle;
	}

	public void setApTitle(String apTitle) {
		this.apTitle = apTitle;
	}

	public String getApPlaceHolder() {
		return apPlaceHolder;
	}

	public void setApPlaceHolder(String apPlaceHolder) {
		this.apPlaceHolder = apPlaceHolder;
	}

	public String getApDetail() {
		return apDetail;
	}

	public void setApDetail(String apDetail) {
		this.apDetail = apDetail;
	}

	public boolean isHaveRoomHintTag() {
		return haveRoomHintTag;
	}

	public void setHaveRoomHintTag(boolean haveRoomHintTag) {
		this.haveRoomHintTag = haveRoomHintTag;
	}

	public String getRoomHint() {
		return roomHint;
	}

	public void setRoomHint(String roomHint) {
		this.roomHint = roomHint;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static RoomStateAndOrderInfo toBean(JSONObject jObj) {

		RoomStateAndOrderInfo dto = new RoomStateAndOrderInfo();

		try {

			if (jObj.has("roomState")) {
				dto.setRoomState(RoomState.toBean(jObj.getJSONObject("roomState")));
			}
			if (jObj.has("canJoinAirPlanePromotionTag")) {
				dto.setCanJoinAirPlanePromotionTag(jObj.getBoolean("canJoinAirPlanePromotionTag"));
			}
			if (jObj.has("apId")) {
				dto.setApId(jObj.getString("apId"));
			}
			if (jObj.has("apTitle")) {
				dto.setApTitle(jObj.getString("apTitle"));
			}
			if (jObj.has("apPlaceHolder")) {
				dto.setApPlaceHolder(jObj.getString("apPlaceHolder"));
			}
			if (jObj.has("apDetail")) {
				dto.setApDetail(jObj.getString("apDetail"));
			}
			if (jObj.has("haveRoomHintTag")) {
				dto.setHaveRoomHintTag(jObj.getBoolean("haveRoomHintTag"));
			}
			if (jObj.has("roomHint")) {
				dto.setRoomHint(jObj.getString("roomHint"));
			}
		} catch (JSONException e) {
			ActivityUtil.saveException(e);
			return null;
		}
		return dto;
	}

}
