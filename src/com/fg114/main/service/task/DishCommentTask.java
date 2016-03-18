package com.fg114.main.service.task;

import android.content.Context;
import android.text.TextUtils;

import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.weibo.WeiboUtilFactory;

/**
 * 提交评论
 * @author zhangyifan
 *
 */
public class DishCommentTask extends BaseTask {
	public static final int VERYGOOD = 1;//喜欢
	public static final int GOOD = 2;//一般
	public static final int BAD = 3;//不喜欢
	
	private String mToken;
	private String mDishId;
	private String mContent;
	private int mLikeTypeTag;
	private String shareTo="";
	public String msg = "评论提交成功";
	
	public DishCommentTask(
					String preDialogMessage, 
					Context context,
					String dishId,
					String token,
					int likeTypeTag,
					String content,
					String shareTo
					) {
		super(preDialogMessage, context);
		this.mToken = token;
		this.mDishId = dishId;
		this.mLikeTypeTag = likeTypeTag;
		this.mContent = content;
		this.shareTo = shareTo;
	}

	@Override
	public JsonPack getData() throws Exception {
		

		JsonPack jp= A57HttpApiV3.getInstance().addResFoodComment(	
				mDishId,//菜品id  
				mToken,//用户id
				mLikeTypeTag,//喜欢类型  1:喜欢  2:一般 3:不喜欢   默认传 1，传0表示只提交评论，不提交分数
				mContent,//评论内容
				shareTo);//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
		return jp;
//		
//		return A57HttpApiV3.getInstance().addDishComment(
//												ActivityUtil.getVersionName(context), 
//												ActivityUtil.getDeviceId(context),
//												mDishId,//菜品id ,
//												mToken,//用户id
//												mLikeTypeTag,//喜欢类型  1:喜欢  2:一般 3:不喜欢
//												mContent//评论内容
//												);
	}

	@Override
	public void onPreStart() {
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result == null || result.getObj() == null) {
			return;
		}
		SimpleData data = JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
		if (!TextUtils.isEmpty(data.getMsg())) {
			msg = data.getMsg();
		}
		WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).dealWithErrorCode(data.getErrorCode());
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}

	@Override
	protected void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}
	
}
