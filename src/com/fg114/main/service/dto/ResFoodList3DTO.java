package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品列表 DTO
 * 
 * @author qianjiefeng
 * 
 */
public class ResFoodList3DTO extends BaseDTO {
	// 列表
	private List<ResFoodData3> list = new ArrayList<ResFoodData3>();
	// 类别列表
	private List<CommonTypeDTO> typeList = new ArrayList<CommonTypeDTO>();

	// 餐馆id
	private String restId;
	// 餐馆名称
	private String restName;
	// 总体评价
	private double overallNum;
	// 口味
	private double tasteNum;
	// 环境
	private double envNum;
	// 服务
	private double serviceNum;

	// get,set-------------------------------------------------------------------

	public List<ResFoodData3> getList() {
		return list;
	}

	public List<CommonTypeDTO> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<CommonTypeDTO> typeList) {
		this.typeList = typeList;
	}

	public void setList(List<ResFoodData3> list) {
		this.list = list;
	}

	public String getRestId() {
		return restId;
	}

	public void setRestId(String restId) {
		this.restId = restId;
	}

	public String getRestName() {
		return restName;
	}

	public void setRestName(String restName) {
		this.restName = restName;
	}

	public double getOverallNum() {
		return overallNum;
	}

	public void setOverallNum(double overallNum) {
		this.overallNum = overallNum;
	}

	public double getTasteNum() {
		return tasteNum;
	}

	public void setTasteNum(double tasteNum) {
		this.tasteNum = tasteNum;
	}

	public double getEnvNum() {
		return envNum;
	}

	public void setEnvNum(double envNum) {
		this.envNum = envNum;
	}

	public double getServiceNum() {
		return serviceNum;
	}

	public void setServiceNum(double serviceNum) {
		this.serviceNum = serviceNum;
	}

}
