package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import android.content.*;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;

import com.fg114.main.app.*;
import com.fg114.main.app.location.*;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.*;
import com.fg114.main.util.*;

/**
 * 获得语音订餐消息列表
 * @author wufucheng
 *
 */
public class GetChatMsgTask extends BaseTask {
	
	private static final String TAG = GetChatMsgTask.class.getSimpleName();
	public static final boolean DEBUG = Settings.DEBUG;

	public ChatMsgListDto dto;
	
	public GetChatMsgTask(
					String preDialogMessage, 
					Context context) {
		super(preDialogMessage, context);
	}

	@Override
	public JsonPack getData() throws Exception {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		} else {
			//继续执行
			if (DEBUG) Log.d(TAG, "looper已存在");
		}
		
		JsonPack jpResult = A57HttpApiV3.getInstance().getChatMsg(
								SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken(),
								SessionManager.getInstance().getLastChatMsgId());
		
		// 测试数据 start
//		JsonPack jpResult = new JsonPack();
//		jpResult.setRe(200);
//		ChatMsgListDto data = new ChatMsgListDto();
//		List<ChatMsgData> list = new ArrayList<ChatMsgData>();
//		data.setList(list);
//		// 文本
//		ChatMsgData text = new ChatMsgData();
//		text.setDataTypeTag(ChatMsgData.DATA_TYPE_TEXT);
//		ChatMsgText t = new ChatMsgText();
//		t.setDetail("这是一个单文本");
//		text.setData(JsonUtils.toJson(t));
//		text.setPicUrl("http://upload2.95171.cn/albumpicimages/20110114/59ce375f-1c97-427a-b022-5b54a74c725b.jpg");
//		text.setUuid(UUID.randomUUID().toString());
//		list.add(text);
//		// 标题文本
//		ChatMsgData text2 = new ChatMsgData();
//		text2.setDataTypeTag(ChatMsgData.DATA_TYPE_TITLE_TEXT);
//		ChatMsgTitleText t2 = new ChatMsgTitleText();
//		t2.setTitle("这是多文本的标题");
//		t2.setDetail("这是多文本的内容");
//		text2.setData(JsonUtils.toJson(t2));
//		text2.setPicUrl("http://upload2.95171.cn/albumpicimages/20110114/59ce375f-1c97-427a-b022-5b54a74c725b.jpg");
//		text2.setUuid(UUID.randomUUID().toString());
//		list.add(text2);
//		// 选择时间
//		ChatMsgData text3 = new ChatMsgData();
//		text3.setDataTypeTag(ChatMsgData.DATA_TYPE_SELECT_TIME);
//		ChatMsgSelectTime t3 = new ChatMsgSelectTime();
//		t3.setSelectTime(System.currentTimeMillis());
//		t3.setTitle("您预订的时间是: ");
//		text3.setData(JsonUtils.toJson(t3));
//		text3.setPicUrl("http://upload2.95171.cn/albumpicimages/20110114/59ce375f-1c97-427a-b022-5b54a74c725b.jpg");
//		text3.setUuid(UUID.randomUUID().toString());
//		list.add(text3);
//		// 选择餐厅
//		ChatMsgData text4 = new ChatMsgData();
//		text4.setDataTypeTag(ChatMsgData.DATA_TYPE_SELECT_REST);
//		ChatMsgSelectRest t4 = new ChatMsgSelectRest();
//		
//		JsonPack jpSearchResults = A57HttpApiV3.getInstance().getRealTimeTableRestList(
//				SessionManager.getInstance().getCityInfo(context).getId(),
//				false,
//				0, 
//				0, 
//				0, 
//				SessionManager.getInstance().getRealTimeResFilter().getRegionId(), 
//				SessionManager.getInstance().getRealTimeResFilter().getDistrictId(),
//				SessionManager.getInstance().getRealTimeResFilter().getMainMenuId(),
//				SessionManager.getInstance().getRealTimeResFilter().getSubMenuId(),
//				SessionManager.getInstance().getRealTimeResFilter().getSelectTime(),
//				SessionManager.getInstance().getRealTimeResFilter().getSortTypeTag(),
//				SessionManager.getInstance().getRealTimeResFilter().getAvgTag(),
//				Settings.DEFAULT_RES_AND_FOOD_PAGE_SIZE,
//				1,
//				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllRegionListTimestamp(),
//				SessionManager.getInstance().getResAndFoodListFromCache(false).getAllMenuTypeListTimestamp());
//		
//		RealTimeTableRestListDTO realTimeDto = RealTimeTableRestListDTO.toBeanFromSearchResult(jpSearchResults.getObj());
//		
//		t4.setList(realTimeDto.getList());
//		t4.setTitle("您选择的餐厅");
//		t4.setDetail("俏江南");
//		text4.setData(JsonUtils.toJson(t4));
//		text4.setPicUrl("http://upload2.95171.cn/albumpicimages/20110114/59ce375f-1c97-427a-b022-5b54a74c725b.jpg");
//		text4.setUuid(UUID.randomUUID().toString());
//		list.add(text4);
//		
//		jpResult.setObj(new JSONObject(JsonUtils.toJson(data)));
		// 测试数据 end
		
		if (jpResult.getRe() == 200) {
			dto = JsonUtils.fromJson(jpResult.getObj().toString(), ChatMsgListDto.class);
			
			// 对选择时间的对象，本地根据时间自己构造具体的Detail
			if (dto != null && dto.getList() != null && dto.getList().size() > 0) {
				for (ChatMsgData msg : dto.getList()) {
					try {
						if (msg.getDataTypeTag() == ChatMsgData.DATA_TYPE_SELECT_TIME && !TextUtils.isEmpty(msg.getData())) {
							ChatMsgSelectTime selectTime = JsonUtils.fromJson(msg.getData(), ChatMsgSelectTime.class);
							if (TextUtils.isEmpty(selectTime.getDetail())) {
								selectTime.setDetail(ConvertUtil.convertLongToDateString(selectTime.getSelectTime(), ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMM));
								msg.setData(JsonUtils.toJson(selectTime));
							}
						}
					} catch (Exception e) {
						LogUtils.logE(TAG, e);
					}
				}
				//存储最后一条成功接收的"系统"消息Id，用于下一次向服务器确认号码
				if(dto.getList().get(dto.getList().size()-1)!=null){
					String uuid=dto.getList().get(dto.getList().size()-1).getUuid();
					if(uuid!=null){
						ValueCacheUtil.getInstance(context).remove(Settings.KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID, Settings.KEY_CHAT_MSG_LIST);
						ValueCacheUtil.getInstance(context).add(Settings.KEY_CHAT_MSG_LAST_SYSTEM_MESSAGE_ID, Settings.KEY_CHAT_MSG_LIST, uuid);
					}
				}
			}
		}
		return jpResult;
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
