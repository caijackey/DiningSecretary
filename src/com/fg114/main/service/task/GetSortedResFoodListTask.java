package com.fg114.main.service.task;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResFoodData;
import com.fg114.main.service.dto.SortedFoodListDTO;
import com.fg114.main.service.dto.SortedFoodListData;
import com.fg114.main.service.dto.SortedFoodSubListData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.weibo.dto.User;

/**
 * 随手拍获得餐厅美食列表
 * @author xujianjun,2012-09-14
 *
 */
public class GetSortedResFoodListTask extends BaseTask {

	public List<ResFoodData> list=new ArrayList<ResFoodData>();
	
	private String restaurantId;
	
	public GetSortedResFoodListTask(
					String preDialogMessage, 
					Context context,
					String restaurantId){
		super(preDialogMessage, context);
		this.restaurantId = restaurantId;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		
		JsonPack jp= A57HttpApiV3.getInstance().getSortedResFoodList(restaurantId);
		if(jp.getRe()!=200){
			if (jp.getRe() == 404) {
				throw new Exception(jp.getMsg());
			}
		}
		SortedFoodListDTO rt=JsonUtils.fromJson(jp.getObj().toString(), SortedFoodListDTO.class);
		List<SortedFoodListData> sortedFoodListData=rt.getList();
		//合并菜品
		for(SortedFoodListData foodListdata : sortedFoodListData){
			List<SortedFoodSubListData> sortedFoodList=foodListdata.getList();
			for(SortedFoodSubListData subFoodListdata : sortedFoodList){
				ResFoodData tempFoodData=new ResFoodData();
				tempFoodData.setUuid(subFoodListdata.getUuid());
				tempFoodData.setName(subFoodListdata.getName());
				tempFoodData.setFirstLetter(foodListdata.getFirstLetter());
				list.add(tempFoodData); 
			}
		}
		return jp;
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
