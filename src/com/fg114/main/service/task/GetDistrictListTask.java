package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 获得商区列表数据 
 * @author zhangyifan
 *
 */
public class GetDistrictListTask extends BaseTask {

	public CommonTypeListDTO dto;
	
	private String cityId = "";
	private String regionId = "";
	long timestamp = 0;
	
	public GetDistrictListTask(
					String preDialogMessage, 
					Context context,
					String cityId,
					String regionId,
					long timestamp) {
		super(preDialogMessage, context);
		this.cityId = cityId;
		this.regionId = regionId;
		this.timestamp = timestamp;
	}

	@Override
	public JsonPack getData() throws Exception {
		return ServiceRequest.getDistrictList(regionId);
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
