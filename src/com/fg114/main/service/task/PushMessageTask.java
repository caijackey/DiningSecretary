package com.fg114.main.service.task;

import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.PushMsgListDTO;
import com.fg114.main.service.dto.PushMsgListDTO;
import com.fg114.main.service.dto.VersionChkDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 信息推送
 * @author zhangyifan
 *
 */
public class PushMessageTask extends BaseTask {

	public PushMsgListDTO dto;
	public JsonPack jsonPack;
	
	private String cityId;
	private String token;
	
	public PushMessageTask(Context context, String cityId, String token) {
		super(context);
		this.token = token;
		this.cityId = cityId;
	}

	@Override
	public JsonPack getData() throws Exception {

		JsonPack jp=new JsonPack();
		jp=ServiceRequest.getPushMsgList(cityId,token);
		
		//--------------测试数据
//		String str="{\"list\":[{\"typeTag\":1,\"msg\":{\"typeTag\":1,\"title\":\"最新最炫的游戏广告，查看广告链接\",\"advUrl\":\"http://www.xiaomishu.com\",\"okButtonName\":\"去看广告\",\"cancelButtonName\":\"算了\"}},{\"typeTag\":2,\"msg\":{\"typeTag\":2,\"title\":\"小秘书开心果上市了\",\"advUrl\":\"xms://order/223E06F07P50FG\",\"okButtonName\":\"去看看\",\"cancelButtonName\":\"别烦我\"}},{\"typeTag\":3,\"msg\":{\"typeTag\":3,\"title\":\"小秘书开心果上市了，上google看看\",\"advUrl\":\"http://www.google.com\",\"okButtonName\":\"去浏览器看看\",\"cancelButtonName\":\"别烦我\"}}],\"nextVisitSeconds\":100}";
//		JSONObject jo=new JSONObject(str);
//		jp.setObj(jo);
		//------------------------
		return jp;
	}
	
	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		jsonPack = result;
		if (result.getObj() != null) {
			dto = JsonUtils.fromJson(result.getObj().toString(), PushMsgListDTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		jsonPack = result;
		
	}
}
