package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 登录
 * @author zhangyifan
 *
 */
public class UserLoginTask extends BaseTask {

	public UserInfoDTO dto;
	
	private String cityId;
	private String userName;
	private String userPwd;
	
	public UserLoginTask(
					String preDialogMessage, 
					Context context,
					String cityId,
					String userName,
					String userPwd) {
		super(preDialogMessage, context);
		this.cityId = cityId;
		this.userName = userName;
		this.userPwd = userPwd;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		JsonPack jp=A57HttpApiV3.getInstance().userLogin2(
				cityId,//所属城市ID
				userName,//用户名
				userPwd,//密码  
				Settings.CLIENT_TYPE,//客户端类型   约定参考顶部描述
				ActivityUtil.getLocalIpAddress(),
				Settings.SELL_CHANNEL_NUM);//渠道号
				
//		JsonPack jp= A57HttpApiV3.getInstance().userLogin(
//												ActivityUtil.getVersionName(context), 
//												cityId,
//												userName,
//												userPwd,
//												Settings.CLIENT_TYPE,
//												ActivityUtil.getLocalIpAddress(),
//												Settings.SELL_CHANNEL_NUM,
//												ActivityUtil.getDeviceId(context));

		//容错处理，防止dto==null
		//dto = UserInfoDTO.toBean(jp.getObj());
		if(dto==null){
			jp.setMsg("用户登录时出现异常，请稍候重试！");
			jp.setRe(500);
		}
		return jp;
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
//		if (result.getObj() != null) {
//			dto = UserInfoDTO.toBean(result.getObj());
//		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
