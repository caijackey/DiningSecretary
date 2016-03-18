package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 获得地域列表数据 
 * @author zhangyifan
 *
 */
public class GetRegionListTask extends BaseTask {

	public CommonTypeListDTO dto;
	
	private String cityId = "";
	long timestamp = 0;
	
	public GetRegionListTask(
					String preDialogMessage, 
					Context context,
					String cityId,
					long timestamp) {
		super(preDialogMessage, context);
		this.cityId = cityId;
		this.timestamp = timestamp;
	}

	@Override
	public JsonPack getData() throws Exception {
		return ServiceRequest.getRegionList();
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = CommonTypeListDTO.toBean(result.getObj());
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
