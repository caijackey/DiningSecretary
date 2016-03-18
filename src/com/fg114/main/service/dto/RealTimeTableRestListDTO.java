package com.fg114.main.service.dto;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.fg114.main.service.task.GetResAndFoodTask;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

/**
 * 实时预订餐厅列表
 * @author qianjiefeng
 *
 */
public class RealTimeTableRestListDTO extends BaseDTO {
	//列表 
	private List<RealTimeTableRestData> list = new ArrayList<RealTimeTableRestData>();
	
	//所在城市地域,商区列表 可以为null
	private List<RfTypeListDTO> allRegionList = new ArrayList<RfTypeListDTO>();
	private boolean allRegionListNeedUpdateTag = true;
	private long allRegionListTimestamp;
	//所在城市菜系类别列表 可以为null
	private List<RfTypeListDTO> allMenuTypeList = new ArrayList<RfTypeListDTO>();
	private boolean allMenuTypeListNeedUpdateTag = true;
	private long allMenuTypeListTimestamp;
	//地域,商区列表
	private List<RfTypeListDTO> regionList = new ArrayList<RfTypeListDTO>();
	//菜系类别列表  大类 小类
	private List<RfTypeListDTO> menuTypeList = new ArrayList<RfTypeListDTO>();
	//排序列表 可以为null
	private List<RfTypeDTO> sortList = new ArrayList<RfTypeDTO>();
	//人均列表 可以为null
	private List<RfTypeDTO> avgList = new ArrayList<RfTypeDTO>();
	
	
	//get,set-------------------------------------------------------------------
	public List<RealTimeTableRestData> getList() {
		return list;
	}
	public void setList(List<RealTimeTableRestData> list) {
		this.list = list;
	}
	public List<RfTypeListDTO> getAllRegionList() {
		return allRegionList;
	}
	public void setAllRegionList(List<RfTypeListDTO> allRegionList) {
		this.allRegionList = allRegionList;
	}
	public boolean isAllRegionListNeedUpdateTag() {
		return allRegionListNeedUpdateTag;
	}
	public void setAllRegionListNeedUpdateTag(boolean allRegionListNeedUpdateTag) {
		this.allRegionListNeedUpdateTag = allRegionListNeedUpdateTag;
	}
	public long getAllRegionListTimestamp() {
		return allRegionListTimestamp;
	}
	public void setAllRegionListTimestamp(long allRegionListTimestamp) {
		this.allRegionListTimestamp = allRegionListTimestamp;
	}
	public List<RfTypeListDTO> getAllMenuTypeList() {
		return allMenuTypeList;
	}
	public void setAllMenuTypeList(List<RfTypeListDTO> allMenuTypeList) {
		this.allMenuTypeList = allMenuTypeList;
	}
	public boolean isAllMenuTypeListNeedUpdateTag() {
		return allMenuTypeListNeedUpdateTag;
	}
	public void setAllMenuTypeListNeedUpdateTag(boolean allMenuTypeListNeedUpdateTag) {
		this.allMenuTypeListNeedUpdateTag = allMenuTypeListNeedUpdateTag;
	}
	public long getAllMenuTypeListTimestamp() {
		return allMenuTypeListTimestamp;
	}
	public void setAllMenuTypeListTimestamp(long allMenuTypeListTimestamp) {
		this.allMenuTypeListTimestamp = allMenuTypeListTimestamp;
	}
	public List<RfTypeListDTO> getRegionList() {
		return regionList;
	}
	public void setRegionList(List<RfTypeListDTO> regionList) {
		this.regionList = regionList;
	}
	public List<RfTypeListDTO> getMenuTypeList() {
		return menuTypeList;
	}
	public void setMenuTypeList(List<RfTypeListDTO> menuTypeList) {
		this.menuTypeList = menuTypeList;
	}
	public List<RfTypeDTO> getSortList() {
		return sortList;
	}
	public void setSortList(List<RfTypeDTO> sortList) {
		this.sortList = sortList;
	}
	public List<RfTypeDTO> getAvgList() {
		return avgList;
	}
	public void setAvgList(List<RfTypeDTO> avgList) {
		this.avgList = avgList;
	}
	
	public static RealTimeTableRestListDTO toBean(JSONObject jObj) {

		return JsonUtils.fromJson(jObj.toString(), RealTimeTableRestListDTO.class);
	}
	
	public static RealTimeTableRestListDTO toBeanFromSearchResult(JSONObject jObj) {
		RealTimeTableRestListDTO dto = new RealTimeTableRestListDTO();
		try {
			dto = toBean(jObj);
			fillDataWithCache(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}
	
	private static void fillDataWithCache(RealTimeTableRestListDTO dto) {
		long l1 = System.currentTimeMillis();
//		// 从缓存中读取当前城市的筛选条件
		ResAndFoodList2DTO cache = SessionManager.getInstance().getResAndFoodListFromCache(false);
		boolean hasUpdate = false; // 是否需要更新缓存
		
		long l2 = System.currentTimeMillis();
		
		// 处理地区信息
		if (dto.isAllRegionListNeedUpdateTag()) {
			// 需要更新地区信息，将当前返回的dto信息写入缓存
			cache.setAllRegionList(dto.getAllRegionList());
			cache.setAllRegionListTimestamp(dto.getAllRegionListTimestamp());
			hasUpdate = true;
		} else {
			// 不需要更新地区信息，将缓存信息写入dto
			dto.setAllRegionList(cache.getAllRegionList());
		}
		if (cache.getAllRegionMap() == null || cache.getAllRegionMap().size() == 0) {
			// 构造Region的"id-dto"映射
			if (cache.getAllRegionList() != null && cache.getAllRegionList().size() > 0) {
				for (RfTypeListDTO ctld : cache.getAllRegionList()) {
					cache.getAllRegionMap().put(ctld.getUuid(), ctld);
					if (ctld.getList() != null && ctld.getList().size() > 0) {
						// 构造District的"id-dto"映射
						for (RfTypeDTO ctd : ctld.getList()) {
							cache.getAllDistrictMap().put(ctd.getUuid(), ctd);
						}
					}
				}
				hasUpdate = true;
			}
		}
		
		// 处理菜系信息
		if (dto.isAllMenuTypeListNeedUpdateTag()) {
			// 需要更新菜系信息，将当前返回的dto信息写入缓存
			cache.setAllMenuTypeList(dto.getAllMenuTypeList());
			cache.setAllMenuTypeListTimestamp(dto.getAllMenuTypeListTimestamp());
			hasUpdate = true;
		} else {
			// 不需要更新菜系信息，将缓存信息写入dto
			dto.setAllMenuTypeList(cache.getAllMenuTypeList());
		}
		if (cache.getAllMainMenuMap() == null || cache.getAllMainMenuMap().size() == 0) {
			// 构造MainMenu的"id-dto"映射
			if (cache.getAllMenuTypeList() != null && cache.getAllMenuTypeList().size() > 0) {
				for (RfTypeListDTO ctld : cache.getAllMenuTypeList()) {
					cache.getAllMainMenuMap().put(ctld.getUuid(), ctld);
					if (ctld.getList() != null && ctld.getList().size() > 0) {
						// 构造SubMenu的"id-dto"映射
						for (RfTypeDTO ctd : ctld.getList()) {
							cache.getAllSubMenuMap().put(ctd.getUuid(), ctd);
						}
					}
				}
				hasUpdate = true;
			}
		}
		
		long l3 = System.currentTimeMillis();
		
		if (hasUpdate) {
			// 需要更新缓存
			SessionManager.getInstance().setResAndFoodListCache(cache);
		}
		
		long l4 = System.currentTimeMillis();
		
		// 补充地区列表的Name
		if (dto.getRegionList() != null && dto.getRegionList().size() > 0) {
			// 补充Region列表的Name
			for (int i=0; i<dto.getRegionList().size(); i++) {
				RfTypeListDTO ctld = dto.getRegionList().get(i);
				if (TextUtils.isEmpty(ctld.getName()) && cache.allRegionMap.containsKey(ctld.getUuid())) {
					ctld.setName(cache.allRegionMap.get(ctld.getUuid()).getName());
				}
				if (ctld.getList() != null && ctld.getList().size() > 0) {
					// 补充District列表的Name
					for (int j=0; j<ctld.getList().size(); j++) {
						RfTypeDTO ctd = ctld.getList().get(j);
						if (TextUtils.isEmpty(ctd.getName()) && cache.allDistrictMap.containsKey(ctd.getUuid())) {
							ctd.setName(cache.allDistrictMap.get(ctd.getUuid()).getName());
						}
					}
				}
			}
		}
		
		// 补充菜单列表的Name
		if (dto.getMenuTypeList() != null && dto.getMenuTypeList().size() > 0) {
			// 补充MainMenu列表的Name
			for (int i=0; i<dto.getMenuTypeList().size(); i++) {
				RfTypeListDTO ctld = dto.getMenuTypeList().get(i);
				if (TextUtils.isEmpty(ctld.getName()) && cache.allMainMenuMap.containsKey(ctld.getUuid())) {
					ctld.setName(cache.allMainMenuMap.get(ctld.getUuid()).getName());
				}
				if (ctld.getList() != null && ctld.getList().size() > 0) {
					// 补充SubMenu列表的Name
					for (int j=0; j<ctld.getList().size(); j++) {
						RfTypeDTO ctd = ctld.getList().get(j);
						if (TextUtils.isEmpty(ctd.getName()) && cache.allSubMenuMap.containsKey(ctd.getUuid())) {
							ctd.setName(cache.allSubMenuMap.get(ctd.getUuid()).getName());
						}
					}
				}
			}
		}
		
		long l5 = System.currentTimeMillis();
	}
}
