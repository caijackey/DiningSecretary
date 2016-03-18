package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.util.JsonUtils;

/**
 * 评论列表DTO
 * @author qianjiefeng
 *
 */
public class CommentReplyListDTO extends BaseDTO { 
	//列表 
	private List<CommentReplyData> list = new ArrayList<CommentReplyData>();
	
	//get,set-------------------------------------------------------------------
	public List<CommentReplyData> getList() {
		return list;
	}

	public void setList(List<CommentReplyData> list) {
		this.list = list;
	}
	
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CommentReplyListDTO toBean(JSONObject jObj) {
		
		CommentReplyListDTO dto = new CommentReplyListDTO();

		try {
			
			if (jObj.has("list")) {
				List<CommentReplyData> list = new ArrayList<CommentReplyData>();
				if (!jObj.isNull("list")) {
					JSONArray jsonArray = jObj.getJSONArray("list");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i ++) {
							list.add(
									CommentReplyData.toBean(
											jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setList(list);
			}
			if (jObj.has("needUpdateTag")) {
				dto.needUpdateTag=jObj.getBoolean("needUpdateTag");
			}
			if (jObj.has("timestamp")) {
				dto.timestamp=jObj.getLong("timestamp");
			}
			if (jObj.has("pgInfo")) {
				dto.pgInfo=JsonUtils.fromJson(jObj.getJSONObject("pgInfo").toString(), PgInfo.class);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return dto;
	}
}
