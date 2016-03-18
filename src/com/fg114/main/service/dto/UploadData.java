package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author qianjiefeng
 *
 */
public class UploadData implements Serializable  {
    //积分 上传图片
	private String pointNumForUploadPic;
    //积分 发表点评
	private String pointNumForComment;
	//积分  对菜打分
	private String pointNumForGradeFood;
	//积分  对餐馆打分
	private String pointNumForGradeRest;
	
	//预设的菜品描述
	private List<String> foodDescribeList;
	//预设的环境描述
	private List<String> envDescribeList;
	
	
	//get,set-------------------------------------------------------------------
	
	public String getPointNumForUploadPic() {
		return pointNumForUploadPic;
	}
	public String getPointNumForGradeRest() {
		return pointNumForGradeRest;
	}
	public void setPointNumForGradeRest(String pointNumForGradeRest) {
		this.pointNumForGradeRest = pointNumForGradeRest;
	}
	public void setPointNumForUploadPic(String pointNumForUploadPic) {
		this.pointNumForUploadPic = pointNumForUploadPic;
	}
	public String getPointNumForComment() {
		return pointNumForComment;
	}
	public void setPointNumForComment(String pointNumForComment) {
		this.pointNumForComment = pointNumForComment;
	}
	public String getPointNumForGradeFood() {
		return pointNumForGradeFood;
	}
	public void setPointNumForGradeFood(String pointNumForGradeFood) {
		this.pointNumForGradeFood = pointNumForGradeFood;
	}
	public List<String> getFoodDescribeList() {
		return foodDescribeList;
	}
	public void setFoodDescribeList(List<String> foodDescribeList) {
		this.foodDescribeList = foodDescribeList;
	}
	public List<String> getEnvDescribeList() {
		return envDescribeList;
	}
	public void setEnvDescribeList(List<String> envDescribeList) {
		this.envDescribeList = envDescribeList;
	}


	




	
}
