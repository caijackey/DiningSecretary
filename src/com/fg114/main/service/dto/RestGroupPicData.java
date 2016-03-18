package com.fg114.main.service.dto;

/**
 * 餐馆图片对象
 * @author qianjiefeng
 *
 */
public class RestGroupPicData  {
	//图片ID 
	String uuid;
	//大图url 
	String picUrl;
	//上传人
	String uploader;
	//上传时间
	long uploadTime;
	
	// getters andd setters
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getUploader() {
		return uploader;
	}
	public void setUploader(String uploader) {
		this.uploader = uploader;
	}
	public long getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(long uploadTime) {
		this.uploadTime = uploadTime;
	}
	
}
