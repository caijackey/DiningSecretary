package com.fg114.main.service.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.service.dto.ChkDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.weibo.WeiboUtilFactory;

/**
 * 提交带图片的餐厅评论
 * 
 * @author xujianjun, 2012-02-27
 * 
 */
public class PostCommentAndPicturesTask extends BaseTask {
	private static final int IMAGE_SIZE = UnitUtil.dip2px(500); // 图片边长限制
	private static final int IMAGE_QUALITY = 80; // 图片压缩率

	public ChkDTO dto;
	// 提交评论成功后返回的评论Uuid
	public String commentUuid;

	private String token;
	private String resId;
	private String orderId;
	private String content;
	private int overallNum;
	private int tasteNum;
	private int envNum;
	private int serviceNum;
	private String price;
	private String unit;
	public static String[] pictureUuids; // 图片的uuid，全局的目的是为了: 图片上传不成功时可以重试剩下的
	public static int index = -1; // 记录当前将要放入pictureUuid数组的索引
	public String[] pictureUrls; // 图片的url地址，公开为public供分享微薄时选用
	private LinearLayout imagesLayout; // 图片容器
	private int postTag;
	private String uuids = ""; // 后台接口需要的图片uuid格式
	private Context context;
	// 控制上传和删除的同步关系，上传一张删除一张，删除一张再上传下一张，只允许交错进行
	volatile boolean deletionIsComplished = true;
	private String shareTo = "";
	
	public String msg = "评论提交成功";

	public PostCommentAndPicturesTask(String preDialogMessage, Context context, String token, String resId, String orderId, String content, int overallNum,
			int tasteNum, int envNum, int serviceNum, LinearLayout imagesLayout, String shareTo) {
		super(preDialogMessage, context);
		this.token = token;
		this.resId = resId;
		this.orderId = orderId;
		this.content = content;
		this.overallNum = overallNum;
		this.tasteNum = tasteNum;
		this.envNum = envNum;
		this.serviceNum = serviceNum;
		// 1：从点评页进去 2：从订单页进去 3：从随手拍进去 菜品 4：从随手拍进去 餐馆
		this.postTag = CheckUtil.isEmpty(orderId) ? 1 : 2;
		this.imagesLayout = imagesLayout;
		this.context = context;
		this.shareTo = shareTo;
	}

	@Override
	public JsonPack getData() throws Exception {

		// 第一步，上传图片
		int count = imagesLayout.getChildCount();
		if (pictureUuids == null) {
			pictureUuids = new String[count];
			index = 0;
		}

		pictureUrls = new String[count];

		for (int i = 0; i < count; i++) {
			/*
			 * //获取文件名 Uri uri = (Uri) ((Object[]) ((ImageView)
			 * imagesLayout.getChildAt(i)).getTag())[1]; String path =
			 * parseImgPath(uri); String fileName = new File(path).getName();
			 * //显示当前上传的文件名
			 * ((RestaurantCommentSubmitActivity)this.context).setUploadMessage
			 * ("文件："+fileName);
			 */
			// 上传
			// 等待标志释放
			while (!deletionIsComplished) {
				SystemClock.sleep(20);
			}
			JsonPack result = uploadPicture((ImageView) imagesLayout.getChildAt(0));
			/*
			 * if(i==2){//调试，人工出错 result.setRe(300); }
			 */
			if (result.getRe() != 200) {
				result.setMsg("上传第" + (i + 1) + "张图片时没有成功，您还有" + (count - i) + "张图片没有上传，请尝试继续提交！");

				return result; // 出错则直接返回错误结果，中断执行
			}
			deletionIsComplished = false; // 上传完毕
			// 成功一张删除一张
			imagesLayout.post(new Runnable() {

				@Override
				public void run() {
					// deleteSuccessPicture(0);
					((RestaurantCommentSubmitActivity) context).deletePicture((ImageView) imagesLayout.getChildAt(0));
					deletionIsComplished = true; // 删除完毕
				}

			});

			// 取出图片url
			if (result.getObj() != null && result.getObj().has("picUrl")) {
				pictureUrls[i] = result.getObj().getString("picUrl");
			}
			/*
			 * // 取出餐厅url if (result.getObj() != null &&
			 * result.getObj().has("restUrl")) { restUrl =
			 * result.getObj().getString("restUrl"); }
			 */
			// 取得图片uuid
			if (result.getObj() != null && result.getObj().has("uuid")) {
				pictureUuids[index++] = result.getObj().getString("uuid");
			}

		}
		// 构造picId
		buildUuids();
		((RestaurantCommentSubmitActivity) this.context).setUploadMessage("上传评论");
		// 上传评论
		JsonPack jp = A57HttpApiV3.getInstance().postComment2(
				token, 
				resId, 
				postTag,// 1：从点评页进去																																			// 餐馆
				"", // foodId
				orderId, 
				uuids,// picId为uploadImage2返回的图片或者为空 picId可以是多个111;222;333
				overallNum, 
				tasteNum, 
				envNum, 
				serviceNum, 
				content,
				shareTo);// 评论内容，postTag=3时传递图片描述，可以为空，postTag=其他，不能为空

		return jp;

	}

	private void deleteSuccessPicture(int i) {
		for (int j = 0; j < i; j++) {
			((RestaurantCommentSubmitActivity) context).deletePicture((ImageView) imagesLayout.getChildAt(j));
		}
	}

	/**
	 * 构造上传评论接口中需要的picId，来源为uploadImage2返回的图片uuid 可以为空。 picId可以是多个，之间用分号分隔
	 * 
	 * @return
	 */
	private void buildUuids() {

		if (pictureUuids == null || pictureUuids.length <= 0) {
			return;
		}
		for (int i = 0; i < this.pictureUuids.length; i++) {
			if (i != 0) {
				this.uuids += ";";
			}
			this.uuids += this.pictureUuids[i];
		}
	}

	/**
	 * 获得路径
	 * 
	 * @param data
	 * @return
	 */
	private String parseImgPath(Uri uri) {
		String path = null;
		if (uri != null) {
			ContentResolver localContentResolver = ContextUtil.getContext().getContentResolver();
			// 查询图片真实路径
			Cursor cursor = localContentResolver.query(uri, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
					path = cursor.getString(index);
					cursor.close();
				}
			}
		}
		return path;
	}

	// 返回值:returnCode,returnMessage,picUrl,restUrl,pictureUuid
	private JsonPack uploadPicture(ImageView imageView) {

		// 获得文件名
		// Uri uri = (Uri) ((Object[]) imageView.getTag())[1];
		// String path = parseImgPath(uri);
		// String fileName = new File(path).getName();
		// String picUrl = "";
		// String restUrl = "";
		// String pictureUuid = "";
		// String returnCode = "200";
		// String returnMessage = "";
		Bitmap bmp = null;
		ByteArrayOutputStream baos = null;
		InputStream in = null;
		JsonPack result = new JsonPack();

		try {

			// --
			bmp = (Bitmap) ((Object[]) imageView.getTag())[0];

			// 获得数据流
			baos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
			// bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, new
			// FileOutputStream("/mnt/sdcard/tppp.jpg"));

			// 转输入流
			in = new ByteArrayInputStream(baos.toByteArray());
			// Log.d("uploadPicture","in.available()="+in.available());
			
			
			result = A57HttpApiV3.getInstance().uploadImage2(
					3,//类别  1:餐馆  2:菜品  3：点评页
					resId,//
					"", "",// 备注 可以为空，这里不用传内容
					token, // 用户token 可以为空
					"",
					0,
					in);
		} catch (Exception e) {
			result.setRe(500);
			result.setMsg(e.getMessage());
			Log.e("PostCommentAndPictureTask.uploadPicture(...)", e.getMessage(), e);
		}
		return result;

	}

	@Override
	public void onPreStart() {
	}

	@Override
	public void onStateFinish(JsonPack result) {
		try {

			// 设置评论uuid
			if (result == null || result.getObj() == null) {
				return;
			}
			Log.e("onStateFinish", result.getObj().toString());
			SimpleData data = JsonUtils.fromJson(result.getObj().toString(), SimpleData.class);
			commentUuid = data.getUuid();
			if (!TextUtils.isEmpty(data.getMsg())) {
				msg = data.getMsg();
			}
			WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).dealWithErrorCode(data.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStateError(JsonPack result) {

		DialogUtil.showToast(context, result.getMsg());
	}
}
