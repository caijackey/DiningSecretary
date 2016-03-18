package com.fg114.main.service.task;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.ResFoodList3DTO;
import com.fg114.main.service.dto.TakeoutRestInfoData;
import com.fg114.main.service.dto.TakeoutRestListDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.GeoUtils;
import com.fg114.main.util.SessionManager;
import com.google.xiaomishujson.Gson;

/**
 * 获得菜品列表
 * 
 * @author xujianjun, 2012-08-02
 * 
 */
public class GetRestaurantFoodListTask extends BaseTask {

	public ResFoodList3DTO dto;
	private String resId;//餐馆ID
	private String currentFoodId;//约定 0：不需要取对象   其他：需要根据id获取该菜品对象 并放到list的第一位
	private String keywords;//关键词  可以为null
	private String typeId;
	private int pageNo;


	public GetRestaurantFoodListTask(String preDialogMessage, Context context, String resId,String currentFoodId,String keywords,String typeId,int pageNo) {
		super(preDialogMessage, context);
		this.resId=resId;
		this.currentFoodId=currentFoodId;
		this.keywords=keywords;
		this.typeId=typeId;
		this.pageNo=pageNo;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		JsonPack jp=A57HttpApiV3.getInstance().getResFoodList3(
				resId,//餐馆ID
				currentFoodId,//约定 0：不需要取对象   其他：需要根据id获取该菜品对象 并放到list的第一位
				keywords,//关键词  可以为null
				typeId,//类别id   默认为0或者空    0或者空为全部状态   其他为特定类别id  
				25,//页面大小
				pageNo);//当前页

		// 测试数据---------------------------------------------------------------
//		SystemClock.sleep(1000);
//		jp = new JsonPack();
//		jp.setRe(200);
//
//		String json = "{\"needUpdateTag\":true,\"pgInfo\":{\"firstTag\":false,\"lastTag\":"+(System.currentTimeMillis()%3==0)+",\"pageNo\":0,\"pageSize\":0,\"sumPage\":0,\"totalNum\":0},\"timestamp\":1328117192520,\"list\":[{\"uuid\":\"SDD1281\",\"name\":\"#pageNo:"+pageNo+"#正院上海公馆#typeId:"+typeId+"#\",\"openTime\":\"09:00~22:00\",\"sendLimit\":\"1份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"127米\"},{\"uuid\":\"SDD1282\",\"name\":\"名轩浦东店\",\"openTime\":\"08:00~23:00\",\"sendLimit\":\"2份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"207米\"},{\"uuid\":\"SDD1283\",\"name\":\"俏江南１８８８店\",\"openTime\":\"09:30~22:30\",\"sendLimit\":\"5份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"35米\"},{\"uuid\":\"SDD1284\",\"name\":\"旋转餐厅\",\"openTime\":\"06:00~24:00\",\"sendLimit\":\"3份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"66米\"},{\"uuid\":\"SDD1285\",\"name\":\"港泰风美食广场\",\"openTime\":\"09:30~23:30\",\"sendLimit\":\"6份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"77米\"}],\"typeList\":[{\"uuid\":\"\",\"name\":\"全部\"},{\"uuid\":\"1\",\"name\":\"中式快餐\"},{\"uuid\":\"2\",\"name\":\"西式快餐\"},{\"uuid\":\"3\",\"name\":\"特色中国菜\"},{\"uuid\":\"4\",\"name\":\"外卖精品\"},{\"uuid\":\"5\",\"name\":\"精品套餐\"}]}";
//		//String json = "{\"needUpdateTag\":true,\"pgInfo\":{\"firstTag\":false,\"lastTag\":"+(System.currentTimeMillis()%3==0)+",\"pageNo\":0,\"pageSize\":0,\"sumPage\":0,\"totalNum\":0},\"timestamp\":1328117192520,\"list\":[{\"uuid\":\"SDD1281\",\"name\":\"#pageNo:"+pageNo+"#正院上海公馆#typeId:"+typeId+"#\",\"openTime\":\"09:00~22:00\",\"sendLimit\":\"1份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"127米\"},{\"uuid\":\"SDD1282\",\"name\":\"名轩浦东店\",\"openTime\":\"08:00~23:00\",\"sendLimit\":\"2份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"207米\"},{\"uuid\":\"SDD1283\",\"name\":\"俏江南１８８８店\",\"openTime\":\"09:30~22:30\",\"sendLimit\":\"5份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"35米\"},{\"uuid\":\"SDD1284\",\"name\":\"旋转餐厅\",\"openTime\":\"06:00~24:00\",\"sendLimit\":\"3份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"66米\"},{\"uuid\":\"SDD1285\",\"name\":\"港泰风美食广场\",\"openTime\":\"09:30~23:30\",\"sendLimit\":\"6份起送\",\"phone\":\"12345674890\",\"distanceMeter\":\"77米\"}],\"typeList\":[]}";
//		JSONObject jo = new JSONObject(json);
//		jp.setObj(jo);
		//------------------------------------------------------------------------
		if (jp != null && jp.getRe() == 200 && jp.getObj() != null) {
			dto = new Gson().fromJson(jp.getObj().toString(), ResFoodList3DTO.class);
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
