package com.xiaomishu.extension.baidu.push;

import com.fg114.main.util.JsonUtils;

import android.graphics.ComposeShader;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

/**
 * 作为抽象，封装了推送过来的内容。可能是消息，可能是其他信息（例如绑定成功之类）
 * @author xujianjun, 2013-12-02
 */
public class PushObject {
	/*
	 * 消息类型，区别是“推送消息”还是“绑定消息”
	 *PushConstants.ACTION_RECEIVE 表示是绑定
	 *PushConstants.ACTION_MESSAGE 表示是消息
	 */
	public String messageType;
	
	//method, PushConstants.METHOD_BIND 表示是“绑定”  
	public String messageMethod;
	//非0表示异常
	public int errorCode;
	//消息，可以是字符串或者json
	public String message;
	
	
	/**
	 * 将message json转为clazz对应的对象，如果转换失败，返回null
	 * @param message
	 * @param clazz
	 * @return
	 */
	public static <T> T getPushObject(String message, Class<T> clazz){
		try{
			return JsonUtils.fromJson(message, clazz);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
//	{
//		float[] outerR = new float[] { 12, 12, 12, 12, 0, 0, 0, 0 }; 
//		RoundRectShape rs=new RoundRectShape(outerR, null, null);
//		ShapeDrawable d=new ShapeDrawable(rs);  
//		ComposeShader cs=new ComposeShader;
//	}
}
