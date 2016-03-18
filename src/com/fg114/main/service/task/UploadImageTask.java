package com.fg114.main.service.task;

import java.io.InputStream;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantUploadConfirmActivity;
import com.fg114.main.app.data.UploadDataPack;
import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.WeiboUtilFactory;

/**
 * 文件上传
 * @author zhangyifan
 *  		recode by xujianjun,2012-10-12
 */
public class UploadImageTask extends BaseTask {

	public ChkDTO dto;
	
	private InputStream inputStreamPicture;
	public String restUrl = ""; //餐厅url
	public String picUrl = ""; //图片url
	public String pictureUuid = ""; //图片uuid
	public String msg = "";
	private UploadDataPack uploadData;
	Context context;
	
	private static final boolean DEBUG=false;

	public UploadImageTask(
					String preDialogMessage, 
					Context context,
					UploadDataPack uploadData,
					InputStream inputStreamPicture
					) {

		
		super(preDialogMessage, context);
		this.uploadData = uploadData;
		this.inputStreamPicture = inputStreamPicture;
		this.context = context;
		
	}
	public void setProgress(int value){
		progressDialog.setProgress(value);
		Log.e("setProgress",value+"");
	}
	@Override
	public JsonPack getData() throws Exception {
		
		String uploadType=uploadData.uploadType;
		String resId=uploadData.restId;
		String comments=uploadData.comment;
		String token=SessionManager.getInstance().isUserLogin(context)?SessionManager.getInstance().getUserInfo(context).getToken():"";
		String shareTo=uploadData.shareString;
		String foodId=uploadData.foodId;
		String foodName=uploadData.foodName;
		String price=uploadData.price;
		String unitName=uploadData.unit;
		int foodScore=uploadData.foodScore;
		int overallNum=uploadData.overallNum;
		int tasteNum=uploadData.tasteNum;
		int envNum=uploadData.envNum;
		int serviceNum=uploadData.serviceNum;
		//--
		JsonPack result;
		// 如果是餐厅环境图上传，先上传图片，再提交评论评分
		if (Settings.UPLOAD_TYPE_RESTAURANT.equals(uploadType)) {
			/**
			 * 餐厅环境图上传 ，分为两个步骤，
			 * step1、上传图片
			 * step2、提交评论评分
			 */
			//第一步，上传图片
//			result=A57HttpApiV3.getInstance().uploadImage2(
//					1,//类别  1:餐馆  2:菜品
//					resId,
//					"",
//					comments,//备注  可以为空
//					token, //用户token  可以为空
//					shareTo,
//					0,
//					inputStreamPicture);
			result = ServiceRequest.uploadImage(
					1,//类别  1:餐馆  2:菜品
					resId,
					"",
					comments,//备注  可以为空
					token, //用户token  可以为空
					shareTo,
					0,
					inputStreamPicture);
			
			if (result.getRe() != 200 || result.getObj() == null) {
				return result;
			}
			
			SimpleData data = JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
			//取出图片url
			picUrl = data.getPicUrl();
			//取出餐厅url
			restUrl = data.getRestUrl();
			pictureUuid = "";
			//取得图片uuid
			pictureUuid = data.getUuid();
			//弹框信息
			msg = data.getMsg();
			WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).dealWithErrorCode(data.getErrorCode());
			
			//第二步，提交评论评分
//			result=A57HttpApiV3.getInstance().postComment2(
//					ActivityUtil.getVersionName(context), 
//					ActivityUtil.getDeviceId(context),
//					token,
//					resId,
//					CheckUtil.isEmpty(foodId)?4:3,//1：从点评页进去    2：从订单页进去    3：从随手拍进去 菜品     4：从随手拍进去 餐馆
//					foodId,
//					"",
//					pictureUuid,//picId为uploadImage2返回的图片或者为空  picId可以是多个 111;222;333
//					overallNum,
//					tasteNum,
//					envNum,
//					serviceNum,
//					comments//评论内容        postTag=3时传递图片描述  可以为空    postTag=其他 不能为空
//					);
			
			result = ServiceRequest.postComment(
					resId,
					CheckUtil.isEmpty(foodId) ? 4 : 3, //1：从点评页进去    2：从订单页进去    3：从随手拍进去 菜品     4：从随手拍进去 餐馆
					foodId,
					"",
					"", // 图片大小   可以为空   多图为   13242314;29282;29282    // --------- new BasicNameValuePair("imgSizeList", imgSizeList),
					pictureUuid,//picId为uploadImage2返回的图片或者为空  picId可以是多个 111;222;333
					overallNum,
					tasteNum,
					envNum,
					serviceNum,
					comments,//评论内容        postTag=3时传递图片描述  可以为空    postTag=其他 不能为空
					shareTo
					);
			
			return result;

		} else {
			/**
			 * 菜品上传图片 ，分为两个步骤，
			 * step1、先添加或更新菜品数据，
			 * step2、再上传图片
			 */

			// step1 ----------------------------------------------------
			// 首先添加菜品数据			
			result = ServiceRequest.addOrUpdateFood(
					resId,// 餐馆id
					foodId,// 菜品id foodId为空时为update 不为空时为add
					foodName,// 菜品名称 可以为空
					price,// 菜品价格 可以为空
					unitName,// 菜品单位 可以为空
					comments,// 备注 可以为空
					token,
					foodScore); // 用户token 可以为空
			//----
			if(result.getRe() != 200){
				//出错直接返回
				return result;
			}
			//----
			//取得菜品uuid
			String uuid = "";
			if (result.getObj() != null && result.getObj().has("uuid")) {
				uuid = result.getObj().getString("uuid");
			}
			// step2 ------------------------------------------------------
			//上传菜品
			result= ServiceRequest.uploadImage(
					2,//类别  1:餐馆  2:菜品
					resId,
					uuid,
					comments,//备注  可以为空
					token, //用户token  可以为空
					shareTo,
					foodScore,
					inputStreamPicture);
			
			SimpleData data = JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
			//取出图片url
			picUrl = data.getPicUrl();
			//取出餐厅url
			restUrl = data.getRestUrl();
			//取得图片uuid
			pictureUuid = "";
			pictureUuid = data.getUuid();
			//弹框信息
			msg = data.getMsg();
			
			WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).dealWithErrorCode(data.getErrorCode());
			
//			result.setRe(200);
//			picUrl="http://f1.xiaomishu.com/pic/aba/D17K17A48697/small/14e7a27e-ae10-466f-b86e-25b516900ac2.jpg";
//			restUrl="http://www.xiaomishu.com/shop/C58G29L36757/";
//			pictureUuid="10861";
			return result;
		}

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

	@Override
	protected void onPostExecute(JsonPack result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}
}
