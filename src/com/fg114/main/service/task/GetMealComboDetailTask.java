package com.fg114.main.service.task;

import android.content.Context;
import android.os.SystemClock;

import com.fg114.main.R;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.*;

/**
 * 获取套餐详细
 * 
 * @author xujianjun,2012-07-24
 * 
 */
public class GetMealComboDetailTask extends BaseTask {

	public MealComboData dto;
	private String mealComboId;


	public GetMealComboDetailTask( String preDialogMessage, Context context, String mealComboId) {
		super(preDialogMessage, context);
		this.mealComboId = mealComboId;

	}


	@Override
	public JsonPack getData() throws Exception {

		//SystemClock.sleep(3000);
//		String token;
//		token=SessionManager.getInstance().getUserInfo(context).getToken();
		JsonPack jp=A57HttpApiV3.getInstance().getMealComboInfo2(mealComboId);
		dto = MealComboData.toBean(jp.getObj());
//		//------测试
//		JsonPack jp=new JsonPack();
//		jp.setRe(200);
//		
//		//--测试数据--------------------------------------------------------
//		dto=new MealComboData();
//		dto.setBigPicUrl("http://f2.xiaomishu.com/pic/AESH10010905/33050.jpg");
//		dto.setDetailUrl("http://www.xiaomishu.com/");
//		dto.setOldUnitPriceNum(915.00);
//		dto.setPlace("蓝精灵村");
//		dto.setHint("蓝精灵村的大青蛙，绿色食品美味擋不住哦，吃之前跳一跳！！！！不后悔！！！看看大青蛙这美味的绿色小吉普！！");
//		dto.setName("蓝精灵村的大青蛙");
//		dto.setRemainSeconds(86966);
//		dto.setRestId("AAAASSSS0887");
//		dto.setRestName("一个好餐厅");
//		dto.setShortDescribe("精致美味的咖喱饭，加上一个大青蛙，好吃啊");
//		dto.setSmallPicUrl("http://f2.xiaomishu.com/pic/AESH10010905/33050.jpg");
//		dto.setRemainNum(235);
//		dto.setUnitPriceNum(298.00);
//		dto.setUuid("REEE0290289");
//		
//		//------------------------------------------------------------------
		return jp;
	}

	@Override
	public void onPreStart() {
		
	}

	@Override
	protected void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}


	@Override
	public void onStateFinish(JsonPack result) {
		
	}
}
