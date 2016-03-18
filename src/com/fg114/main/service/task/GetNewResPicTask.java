package com.fg114.main.service.task;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.RestPicListDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

import android.content.Context;

public class GetNewResPicTask extends BaseTask
{

	public RestPicListDTO dto;

	private String restId = "";
	private int picViewTag = 0;
	private int startIndex;
	private int pageSize = Settings.DEFAULT_PIC_PAGE_SIZE;
	
	public GetNewResPicTask(String preDialogMessage, Context context, String restId, int picViewTag, int startIndex)
	{
		super(preDialogMessage, context);
		this.restId = restId;
		this.picViewTag = picViewTag;
		this.startIndex = startIndex;
	}

	@Override
	public JsonPack getData() throws Exception
	{
		return ServiceRequest.getRestPicList(restId, picViewTag, pageSize, startIndex);
	}

	@Override
	public void onPreStart()
	{

	}

	@Override
	public void onStateFinish(JsonPack result)
	{
		if (result != null && result.getObj() != null) {
			dto = JsonUtils.fromJson(result.getObj().toString(), RestPicListDTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result)
	{
		DialogUtil.showToast(context, result.getMsg());
	}
}
