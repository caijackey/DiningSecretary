package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 获得用户积分列表
 * @author xu jianjun,2012-02-24
 *
 */
public class GetFoodCommentListTask extends BaseTask {

	public ResFoodCommentListDTO dto;
	
	private String foodId = "";
	private int pageNo = 0;
	private int pageSize=1;
	
	public GetFoodCommentListTask(
					String preDialogMessage, 
					Context context,
					String foodId,
					int pageSize,
					int pageNo) {
		super(preDialogMessage, context);
		this.foodId = foodId;
		this.pageNo = pageNo;
		this.pageSize=pageSize;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		return A57HttpApiV3.getInstance().getResFoodCommentList(
				foodId,//菜品id 
				pageSize,//页面大小
				pageNo);//当前页
				
//		return A57HttpApiV3.getInstance().getDishCommentList(
//												ActivityUtil.getVersionName(context), 
//												ActivityUtil.getDeviceId(context),
//												foodId,
//												//Settings.DEFAULT_RES_FOOD_PAGE_SIZE,
//												pageSize,
//												pageNo);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			//dto = DishCommentListDTO.toBean(result.getObj());
			dto = JsonUtils.fromJson(result.getObj().toString(), ResFoodCommentListDTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
