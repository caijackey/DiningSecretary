package com.fg114.main.service.task;

import org.json.JSONException;

import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

import android.content.Context;

public class AddOrUpdateRestTask extends BaseTask{
	
	private String res_name;//商户名称
	private String resId;
	private String regionId;//行政区ID
	private String districtId;//热门商区ID
	private String cityId;//城市ID
	private String restAddress;
    private String mainMenuId;//菜系ID
	private String restTel;
	private String email;
	private String token;
	
	private int postTag;
	
	public String uuid = "";

	public AddOrUpdateRestTask(String preDialogMessage,
			                    Context context,
			                    String resname,
			                    String resId,
			                    String regionId,
			                    String districtId,
			                    String cityId,
			                    String restAdress,
			                    String mainMenuId,
			                    String restTel,
			                    String email,
			                    String token,
			                    int postTag) {
		super(preDialogMessage,context);
		// TODO Auto-generated constructor stub
		this.res_name = resname ;
		this.resId =  resId;
		this.regionId = regionId;
		this.districtId = districtId;
		this.cityId = cityId;
		this.restAddress = restAdress;
		this.mainMenuId = mainMenuId;
		this.restTel = restTel;
		this.email = email;
		this.token = token;
		this.postTag = postTag;
		
	}

	@Override
	public JsonPack getData() throws Exception {
		// TODO Auto-generated method stub
		//return null;
		return A57HttpApiV3.getInstance().addOrUpdateRest(ActivityUtil.getVersionName(context),
				                                          ActivityUtil.getDeviceId(context),
				                                          "", 
				                                          postTag, 
				                                          resId, 
				                                          res_name,
				                                          cityId,
				                                          regionId,
				                                          districtId, 
				                                          mainMenuId, 
				                                          restAddress,
				                                          restTel, 
				                                          email, 
				                                          token);
	}

	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null && result.getObj().has("uuid")) {
			try {
				uuid = result.getObj().getString("uuid");
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
