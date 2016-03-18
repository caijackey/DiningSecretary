package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.PushMsgListDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 获得订单列表
 * @author zhangyifan
 *
 */
public class FindOrdersTask extends BaseTask {

	public OrderListDTO dto;
	
	private String token;
	private String orderStatus;
	private int pageNo = 0;
	private int mtypeId=0;
	
	public FindOrdersTask(
					String preDialogMessage, 
					Context context,
					int typeId,
					String token,
					String orderStatus,
					int pageNo) {
		super(preDialogMessage, context);

		this.token = token;
		this.orderStatus = orderStatus;
		this.pageNo = pageNo;
		this.mtypeId=typeId;
	}

	@Override
	public JsonPack getData() throws Exception {

		return A57HttpApiV3.getInstance().getOrderList3(
													mtypeId,
													token,
													orderStatus,
													Settings.DEFAULT_COMMENT_PAGE_SIZE, 
													pageNo);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	protected void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		//closeProgressDialog();
	}

	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = JsonUtils.fromJson(result.getObj().toString(), OrderListDTO.class);
		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
