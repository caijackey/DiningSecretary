package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.List;


/**
 * 评论数据
 * 
 * @author qianjiefeng
 * 
 */
public class CommentData implements Serializable {
	// 评论id
	public String uuid;
	// 评论人
	public String userName;
	// 评论人小头像url
	public String userSmallPicUrl;
	// 评论人大头像url
	public String userPicUrl;
	// 评论时间
	public long createTime;
	// 是否有打分
	public boolean gradeTag;
	// 没打分的说明
	public String noGradeIntro;
	// 是否喜欢
	public boolean likeTag;
	// 总体评价
	public double overallNum;
	// 口味
	public double tasteNum;
	// 环境
	public double envNum;
	// 服务
	public double serviceNum;
	// 评论内容
	public String detail;
	// 评论图片列表
	public List<CommentPicData> picList;
	// 回复数量
	public int replyNum;
	// 来自什么客户端
	public String clientName;

}
