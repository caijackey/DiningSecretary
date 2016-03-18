package com.fg114.main.service.task;

import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

import android.content.Context;

public class FoodTypeTask extends BaseTask{

	
public CommonTypeListDTO dto;
	
	private String cityId = "";
	long timestamp = 0;
	
	public FoodTypeTask(String preDialogMessage, 
			Context context,
			String cityId,
			long timestamp) {
		super(preDialogMessage,context);
		this.cityId = cityId;
		this.timestamp = timestamp;
	
	}

	@Override
	public JsonPack getData() throws Exception {
		return ServiceRequest.getFoodMainTypeList(); 
		
	}

	@Override
	public void onStateFinish(JsonPack result) {
		// TODO Auto-generated method stub
		if(result.getObj()!=null){
			dto = CommonTypeListDTO.toBean(result.getObj());
		}
		
	}

	@Override
	public void onStateError(JsonPack result) {
		// TODO Auto-generated method stub
		DialogUtil.showToast(context, result.getMsg());
	}

	@Override
	public void onPreStart() {
		// TODO Auto-generated method stub
		
	}

}
