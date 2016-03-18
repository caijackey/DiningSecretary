package com.fg114.main.service.task;

import android.content.Context;

import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.CommentListDTO;
import com.fg114.main.service.dto.CommentListDTO;
import com.fg114.main.service.dto.CommentReplyListDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;

/**
 * 获得某评论的回复列表数据
 * @author xujianjun
 *
 */
public class GetResCommentReplyListTask extends BaseTask {

	public CommentReplyListDTO dto;
	
	private String commentId = "";
	private int pageNo = 0;
	
	public GetResCommentReplyListTask(
					String preDialogMessage, 
					Context context,
					String commentId,
					int pageNo) {
		super(preDialogMessage, context);
		this.commentId = commentId;
		this.pageNo = pageNo;
	}

	@Override
	public JsonPack getData() throws Exception {
		return ServiceRequest.getRestCommentReplyList(commentId, 200, pageNo);
	}

	@Override
	public void onPreStart() {
		
	}
	
	@Override
	public void onStateFinish(JsonPack result) {
		if (result.getObj() != null) {
			dto = CommentReplyListDTO.toBean(result.getObj());

		}
	}

	@Override
	public void onStateError(JsonPack result) {
		DialogUtil.showToast(context, result.getMsg());
	}
}
