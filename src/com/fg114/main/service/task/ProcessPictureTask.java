package com.fg114.main.service.task;

import java.util.ArrayList;

import android.content.Context;

import com.fg114.main.app.listener.OnProcessPictureListener;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.util.DialogUtil;

/**
 * 借用task来异步处理图片信息
 * @author dengxiangyu
 *
 */
public class ProcessPictureTask  extends BaseTask{

	ArrayList<String[]> picture_data_selected;
	OnProcessPictureListener listener;
	public ProcessPictureTask(String preDialogMessage, Context context, ArrayList<String[]> picture_data_selected,OnProcessPictureListener listener) {
		super(preDialogMessage, context,false,picture_data_selected.size());
		this.picture_data_selected=picture_data_selected;
		this.listener=listener;
	}

	@Override
	public JsonPack getData() throws Exception {

		JsonPack jp = new JsonPack();
		jp.setRe(200);
		// ------------------------------
		try {
			int i=0;
			for(String[] data : picture_data_selected){
				listener.onProcessPicture(data);
				progressDialog.setProgress(++i);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			jp.setRe(300);
			jp.setMsg(e.getMessage());
		}
		// ------------------------------
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
		DialogUtil.showToast(context, "处理图片数据时出错，请稍后重试!"+(result!=null?result.getMsg():""));
	}

}
