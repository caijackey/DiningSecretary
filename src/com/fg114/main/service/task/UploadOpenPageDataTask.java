package com.fg114.main.service.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ZipUtils;

/**
 * 提交页面统计信息
 * 格式：
 * 网络情况\tip\t页面名称\t打开的时间\t页面整体打开耗时\t页面http查询耗时\t页面查询url\n
	其中网络情况分别为 wifi,3g,2g
	其中页面名称分别为  搜索页:search  详细页:detail   实时餐位页:real	
 * @author xujianjun,2012-11-28
 *
 */
public class UploadOpenPageDataTask extends BaseTask {

	private String data;
	
	public UploadOpenPageDataTask(
					String preDialogMessage, 
					Context context,
					String data) {
		super(preDialogMessage, context);
		this.data = data;
	}

	@Override
	public JsonPack getData() throws Exception {
		
		return A57HttpApiV3.getInstance().uploadOpenPageData(toInputStream(data));
	}
	//压缩字符串
	private InputStream toInputStream(String data) throws UnsupportedEncodingException {
		if(CheckUtil.isEmpty(data)){
			data="";
		}
		return new ByteArrayInputStream(ZipUtils.gZip(data.getBytes("UTF-8")));
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		Log.d("UploadOpenPageDateTask.onStateFinish","上传数据成功");
	}

	@Override
	public void onStateError(JsonPack result) {
		Log.e("UploadOpenPageDateTask.onStateError","上传数据失败");
	}
}
