package com.fg114.main.service.dto;

import java.util.UUID;

import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;

/**
 * xxx
 * @author qianjiefeng
 *
 */
public class ChatMsgData  {
	
	// 消息类型
	public static final int DATA_TYPE_TEXT = 1; // 简单文本
	public static final int DATA_TYPE_TITLE_TEXT = 2; // 带标题的文本
	public static final int DATA_TYPE_VOICE = 3; // 语音
	public static final int DATA_TYPE_SELECT_TIME = 4; // 选择时间
	public static final int DATA_TYPE_SELECT_REST = 5; // 选择分店
	
	// 消息来源
	public static final int SRC_SERVER = 0; // 服务器
	public static final int SRC_CLIENT = 1; // 本地客户端
	
	// 发送的消息类型
	public static final int SEND_DATA_TYPE_VOICE = -1; // 语音 data=ChatMsgVoice
	public static final int SEND_DATA_TYPE_TEXT = 1; // 简单文本 data=ChatMsgSendText
	public static final int SEND_DATA_TYPE_SELECT_TIME = 2; // 选择时间 data=ChatMsgSendSelectTime
	public static final int SEND_DATA_TYPE_SELECT_REST = 3; // 选择分店 data=ChatMsgSendSelectRest
	
	//消息id
	String uuid = "";
	//头像
	String picUrl = "";
	//数据类型 dataTypeTag
	//1:简单文本 data=ChatMsgText
	//2:带标题的文本 ChatMsgTitleText
	//3:语音 ChatMsgVoice
	//4:选择时间 ChatMsgSelectTime
	//5:选择分店 ChatMsgSelectRest
	int dataTypeTag;
	//数据 json字符串
	String data = "";
	//创建时间
	long createTime;
	
	// 自用属性
	private int src; // 消息来源
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public int getDataTypeTag() {
		return dataTypeTag;
	}
	public void setDataTypeTag(int dataTypeTag) {
		this.dataTypeTag = dataTypeTag;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public int getSrc() {
		return src;
	}
	public void setSrc(int src) {
		this.src = src;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	/**
	 * 发送的数据转为Json字符串
	 * @param dataTypeTag
	 * @param objSend
	 * @return
	 */
	public static String sendDataToString(int dataTypeTag, Object objSend) {
		try {
			if (dataTypeTag == ChatMsgData.SEND_DATA_TYPE_TEXT) {
				ChatMsgSendText text = (ChatMsgSendText) objSend;
				return JsonUtils.toJson(text);
			} else if (dataTypeTag == ChatMsgData.SEND_DATA_TYPE_SELECT_TIME) {
				ChatMsgSendSelectTime selectTime = (ChatMsgSendSelectTime) objSend;
				return JsonUtils.toJson(selectTime);
			} else if (dataTypeTag == ChatMsgData.SEND_DATA_TYPE_SELECT_REST) {
				ChatMsgSendSelectRest selectRest = (ChatMsgSendSelectRest) objSend;
				return JsonUtils.toJson(selectRest);
			} else {
				return "{}";
			}
		} catch (Exception e) {
			LogUtils.logE(e);
			return "{}";
		}
	}
	
	/**
	 * 发送的消息转换为ChatMsgData
	 * @param typeTag
	 * @param objSend
	 * @return
	 */
	public static ChatMsgData sendDataToChatMsgData(int typeTag, Object objSend) {
		ChatMsgData dataResult = new ChatMsgData();
		dataResult.setSrc(ChatMsgData.SRC_CLIENT);
		dataResult.setUuid(UUID.randomUUID().toString());
		dataResult.setPicUrl(SessionManager.getInstance().getHeadPic());
		dataResult.setCreateTime(System.currentTimeMillis());
		try {
			if (typeTag == ChatMsgData.SEND_DATA_TYPE_VOICE) {
				// 语音消息
				ChatMsgVoice voice = (ChatMsgVoice) objSend;

				dataResult.setDataTypeTag(ChatMsgData.DATA_TYPE_VOICE);
				dataResult.setData(JsonUtils.toJson(voice));
			} else if (typeTag == ChatMsgData.SEND_DATA_TYPE_TEXT) {
				// 简单文本
				ChatMsgSendText data = (ChatMsgSendText) objSend;
				ChatMsgText text = new ChatMsgText();
				text.setDataTypeTag(ChatMsgData.SEND_DATA_TYPE_TEXT);
				text.setDetail(data.getDetail());

				dataResult.setDataTypeTag(ChatMsgData.DATA_TYPE_TEXT);
				dataResult.setData(JsonUtils.toJson(text));
			} else if (typeTag == ChatMsgData.SEND_DATA_TYPE_SELECT_TIME) {
				// 选择时间
				ChatMsgSendSelectTime data = (ChatMsgSendSelectTime) objSend;
				ChatMsgText text = new ChatMsgText();
				text.setDataTypeTag(ChatMsgData.SEND_DATA_TYPE_SELECT_TIME);
				text.setSelectTime(data.getSelectTime());

				dataResult.setDataTypeTag(ChatMsgData.DATA_TYPE_TEXT);
				dataResult.setData(JsonUtils.toJson(text));
			} else if (typeTag == ChatMsgData.SEND_DATA_TYPE_SELECT_REST) {
				// 选择分店
				ChatMsgSendSelectRest data = (ChatMsgSendSelectRest) objSend;
				ChatMsgText text = new ChatMsgText();
				text.setDataTypeTag(ChatMsgData.SEND_DATA_TYPE_SELECT_REST);
				text.setResId(data.getRestId());
				text.setResName(data.getResName());
				text.setRoomText(data.getRoomText());

				dataResult.setDataTypeTag(ChatMsgData.DATA_TYPE_TEXT);
				dataResult.setData(JsonUtils.toJson(text));
			}
		} catch (Exception e) {
			LogUtils.logE(e);
		}
		return dataResult;
	}
	
	/**
	 * 从用户发送的ChatMsgText提取具体的内容，可能是简单文本，选择时间，选择餐厅
	 * @param text
	 * @return
	 */
	public static String getInfoFromChatMsgText(ChatMsgText text) {
		String info = "";
		try {
			if (text.getDataTypeTag() == SEND_DATA_TYPE_TEXT) {
				info = text.getDetail();
			} else if (text.getDataTypeTag() == SEND_DATA_TYPE_SELECT_TIME) {
				info = "您已选择就餐时间 " + ConvertUtil.convertLongToDateString(text.getSelectTime(), ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMM);
			} else if (text.getDataTypeTag() == SEND_DATA_TYPE_SELECT_REST) {
				info = "您已选择想要预订的餐厅 " + text.getResName() + "(" + text.getRoomText() + ")";
			}
			return info;
		} catch (Exception e) {
			LogUtils.logE(e);
			return info;
		}
	}
	
	/**
	 * 返回确认时间的提示文字
	 * @param selectTime
	 * @return
	 */
	public static String getConfirmInfoFromSelectTime(long selectTime) {
		return ConvertUtil.convertLongToDateString(selectTime, ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMM) + "\n(您已选择就餐时间)";
	}
	
	/**
	 * 返回选择餐厅的提示文字
	 * @param selectTime
	 * @return
	 */
	public static String getConfirmInfoFromSelectRes(ChatMsgSendSelectRest selectRest) {
		return selectRest.getResName() + "(" + selectRest.getRoomText() + ")\n(您已选择想要预订的餐厅)";
	}
}
