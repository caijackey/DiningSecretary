package com.fg114.main.service.task;

import android.content.Context;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.service.dto.CommentListDTO;
import com.fg114.main.service.dto.CommentListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.OrderInfoData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 获得榜单列表
 * 
 * @author zhangyifan
 * 
 */
public class GetResCommentListTask extends BaseTask {

	public CommentListDTO dto;

	private String resId = "";
	private String orderId = "";
	private int pageNo = 0;

	public GetResCommentListTask(String preDialogMessage, Context context, String resId, String orderId, int pageNo) {
		super(preDialogMessage, context);
		this.resId = resId;
		this.pageNo = pageNo;
		this.orderId=orderId;
	}

	@Override
	public JsonPack getData() throws Exception {

		return ServiceRequest.getRestCommentList(resId, orderId, Settings.DEFAULT_RES_FOOD_PAGE_SIZE, pageNo);
	}

	@Override
	public void onPreStart() {

	}

	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = JsonUtils.fromJson(result.getObj().toString(), CommentListDTO.class);
			// dto = CommentList2DTO.toBean(result.getObj());
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
