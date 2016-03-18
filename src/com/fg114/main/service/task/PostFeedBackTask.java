package com.fg114.main.service.task;

import android.content.Context;
import android.text.TextUtils;

import com.fg114.main.R;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.util.*;

/**
 * 提交反馈信息
 * @author wufucheng
 *
 */
public class PostFeedBackTask extends BaseTask {

	public ChkDTO dto;
	
	private String name;
	private String email;
	private String feedback;
	private int typeTag;
	private int funcTag;									
	private String typeId;
	private String typeName;
	private Runnable runAfterAlert;
	
	public PostFeedBackTask(					
					int typeTag,
					int funcTag,									
					String typeId,
					String typeName,
					String preDialogMessage, 
					Context context,
					String name,
					String email,
					String feedback) {
		super(preDialogMessage, context);
		this.name = name;
		this.email = email;
		this.feedback = feedback;
		this.typeTag=typeTag;
		this.funcTag=funcTag;
		this.typeId=typeId;
		this.typeName=typeName;
	}
	public PostFeedBackTask(					
					int typeTag,
					int funcTag,									
					String typeId,
					String typeName,
					String preDialogMessage, 
					Context context,
					String name,
					String email,
					String feedback,Runnable runAfterAlert) {
		
		this(typeTag,funcTag,typeId,typeName,preDialogMessage,context,name,email,feedback);
		this.runAfterAlert=runAfterAlert;
	}
	@Override
	public JsonPack getData() throws Exception {
		return A57HttpApiV3.getInstance().postFeedBack(
												name,
												email,
												feedback,
												ActivityUtil.getVersionName(context), 
												ActivityUtil.getDeviceId(context));

	}

	@Override
	public void onPreStart() {		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		closeProgressDialog();
		if (!TextUtils.isEmpty(result.getMsg())) {
			DialogUtil.showAlert(context,context.getString(R.string.text_dialog_error_submit_success),"",runAfterAlert);
		} else {
			DialogUtil.showAlert(context,context.getString(R.string.text_dialog_error_submit_success), "", runAfterAlert);
		}
		
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
