package com.fg114.main.service.dto;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fg114.main.service.task.GetResAndFoodTask;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

import android.text.TextUtils;
import android.util.Log;

/**
 * 餐馆，菜品列表DTO
 * @author qianjiefeng
 *
 */
public class ResAndFoodList2DTO extends BaseDTO {
	//列表 
	private List<ResAndFoodData> list = new ArrayList<ResAndFoodData>();
	
	//所在城市地域,商区列表 可以为null
	private List<RfTypeListDTO> allRegionList = new ArrayList<RfTypeListDTO>();
	private boolean allRegionListNeedUpdateTag = true;
	private long allRegionListTimestamp;
	//所在城市菜系类别列表 可以为null
	private List<RfTypeListDTO> allMenuTypeList = new ArrayList<RfTypeListDTO>();
	private boolean allMenuTypeListNeedUpdateTag = true;
	private long allMenuTypeListTimestamp;
	//所在城市榜单类别列表 可以为null
	private List<RfTypeListDTO> allTopRestTypeList = new ArrayList<RfTypeListDTO>();
	private boolean allTopRestTypeListNeedUpdateTag = true;
	private long allTopRestTypeListTimestamp;
	
	
	//地域,商区列表
	private List<RfTypeListDTO> regionList = new ArrayList<RfTypeListDTO>();
	//菜系类别列表  大类 小类
	private List<RfTypeListDTO> menuTypeList = new ArrayList<RfTypeListDTO>();
	//榜单类别列表  大类和小类
	private List<RfTypeListDTO> topRestTypeList = new ArrayList<RfTypeListDTO>();
	//排序列表 可以为null
	private List<RfTypeDTO> sortList = new ArrayList<RfTypeDTO>();
	//人均列表 可以为null
	private List<RfTypeDTO> avgList = new ArrayList<RfTypeDTO>();
	
	// v3.1.37新增
	//是否需要打开附近搜索广告面板
	private boolean needOpenNearSearchAdvTag;
	//附近搜索广告面板文字说明
	private String nearSearchAdvTitle = "";
	//附近搜索广告url
	private String nearSearchAdvUrl = "";
	//附近搜索广告id
	private String nearSearchAdvId = "";

	
	/* 自用属性 */
	// 主商区的"id-对象"映射
	public HashMap<String, RfTypeListDTO> allRegionMap = new HashMap<String, RfTypeListDTO>();
	// 子商区的"id-对象"映射
	public HashMap<String, RfTypeDTO> allDistrictMap = new HashMap<String, RfTypeDTO>();
	// 主菜系的"id-对象"映射
	public HashMap<String, RfTypeListDTO> allMainMenuMap = new HashMap<String, RfTypeListDTO>();
	// 子菜系的"id-对象"映射
	public HashMap<String, RfTypeDTO> allSubMenuMap = new HashMap<String, RfTypeDTO>();
	// 主榜单的"id-对象"映射
	public HashMap<String, RfTypeListDTO> allMainTopMap = new HashMap<String, RfTypeListDTO>();
	// 子榜单的"id-对象"映射
	public HashMap<String, RfTypeDTO> allSubTopMap = new HashMap<String, RfTypeDTO>();
	
	
	//get,set-------------------------------------------------------------------
	
	public boolean isNeedOpenNearSearchAdvTag() {
		return needOpenNearSearchAdvTag;
	}
	public void setNeedOpenNearSearchAdvTag(boolean needOpenNearSearchAdvTag) {
		this.needOpenNearSearchAdvTag = needOpenNearSearchAdvTag;
	}
	public String getNearSearchAdvTitle() {
		return nearSearchAdvTitle;
	}
	public void setNearSearchAdvTitle(String nearSearchAdvTitle) {
		this.nearSearchAdvTitle = nearSearchAdvTitle;
	}
	public String getNearSearchAdvUrl() {
		return nearSearchAdvUrl;
	}
	public void setNearSearchAdvUrl(String nearSearchAdvUrl) {
		this.nearSearchAdvUrl = nearSearchAdvUrl;
	}
	public String getNearSearchAdvId() {
		return nearSearchAdvId;
	}
	public void setNearSearchAdvId(String nearSearchAdvId) {
		this.nearSearchAdvId = nearSearchAdvId;
	}
	public List<ResAndFoodData> getList() {
		return list;
	}
	public void setList(List<ResAndFoodData> list) {
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
	public List<RfTypeListDTO> getAllTopRestTypeList() {
		return allTopRestTypeList;
	}
	public void setAllTopRestTypeList(List<RfTypeListDTO> allTopRestTypeList) {
		this.allTopRestTypeList = allTopRestTypeList;
	}
	public boolean isAllTopRestTypeListNeedUpdateTag() {
		return allTopRestTypeListNeedUpdateTag;
	}
	public void setAllTopRestTypeListNeedUpdateTag(
			boolean allTopRestTypeListNeedUpdateTag) {
		this.allTopRestTypeListNeedUpdateTag = allTopRestTypeListNeedUpdateTag;
	}
	public long getAllTopRestTypeListTimestamp() {
		return allTopRestTypeListTimestamp;
	}
	public void setAllTopRestTypeListTimestamp(long allTopRestTypeListTimestamp) {
		this.allTopRestTypeListTimestamp = allTopRestTypeListTimestamp;
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
	public List<RfTypeListDTO> getTopRestTypeList() {
		return topRestTypeList;
	}
	public void setTopRestTypeList(List<RfTypeListDTO> topRestTypeList) {
		this.topRestTypeList = topRestTypeList;
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

	public HashMap<String, RfTypeListDTO> getAllRegionMap() {
		return allRegionMap;
	}
	public void setAllRegionMap(HashMap<String, RfTypeListDTO> allRegionMap) {
		this.allRegionMap = allRegionMap;
	}
	public HashMap<String, RfTypeDTO> getAllDistrictMap() {
		return allDistrictMap;
	}
	public void setAllDistrictMap(HashMap<String, RfTypeDTO> allDistrictMap) {
		this.allDistrictMap = allDistrictMap;
	}
	public HashMap<String, RfTypeListDTO> getAllMainMenuMap() {
		return allMainMenuMap;
	}
	public void setAllMainMenuMap(HashMap<String, RfTypeListDTO> allMainMenuMap) {
		this.allMainMenuMap = allMainMenuMap;
	}
	public HashMap<String, RfTypeDTO> getAllSubMenuMap() {
		return allSubMenuMap;
	}
	public void setAllSubMenuMap(HashMap<String, RfTypeDTO> allSubMenuMap) {
		this.allSubMenuMap = allSubMenuMap;
	}
	public HashMap<String, RfTypeListDTO> getAllMainTopMap() {
		return allMainTopMap;
	}
	public void setAllMainTopMap(HashMap<String, RfTypeListDTO> allMainTopMap) {
		this.allMainTopMap = allMainTopMap;
	}
	public HashMap<String, RfTypeDTO> getAllSubTopMap() {
		return allSubTopMap;
	}
	public void setAllSubTopMap(HashMap<String, RfTypeDTO> allSubTopMap) {
		this.allSubTopMap = allSubTopMap;
	}
	
	public static ResAndFoodList2DTO toBean(JSONObject jObj) {

		return JsonUtils.fromJson(jObj.toString(), ResAndFoodList2DTO.class);
	}
	
	public static ResAndFoodList2DTO toBeanFromSearchResult(JSONObject jObj) {
		ResAndFoodList2DTO dto = new ResAndFoodList2DTO();
		try {
			dto = toBean(jObj);
			fillDataWithCache(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}
	
	private static void fillDataWithCache(ResAndFoodList2DTO dto) {
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
		
		// 处理榜单信息
		if (dto.isAllTopRestTypeListNeedUpdateTag()) {
			// 需要更新榜单信息，将当前返回的dto信息写入缓存
			cache.setAllTopRestTypeList(dto.getAllTopRestTypeList());
			cache.setAllTopRestTypeListTimestamp(dto.getAllTopRestTypeListTimestamp());
			hasUpdate = true;
		} else {
			// 不需要更新榜单信息，将缓存信息写入dto
			dto.setAllTopRestTypeList(cache.getAllTopRestTypeList());
		}
		if (cache.getAllMainTopMap() == null || cache.getAllMainTopMap().size() == 0) {
			// 构造MainTop的"id-dto"映射
			if (cache.getAllTopRestTypeList() != null && cache.getAllTopRestTypeList().size() > 0) {
				for (RfTypeListDTO ctld : cache.getAllTopRestTypeList()) {
					cache.getAllMainTopMap().put(ctld.getUuid(), ctld);
					if (ctld.getList() != null && ctld.getList().size() > 0) {
						// 构造SubTop的"id-dto"映射
						for (RfTypeDTO ctd : ctld.getList()) {
							cache.getAllSubTopMap().put(ctd.getUuid(), ctd);
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
		
		// 补充榜单列表的Name
		if (dto.getTopRestTypeList() != null && dto.getTopRestTypeList().size() > 0) {
			// 补充MainTop列表的Name
			for (int i=0; i<dto.getTopRestTypeList().size(); i++) {
				RfTypeListDTO ctld = dto.getTopRestTypeList().get(i);
				if (TextUtils.isEmpty(ctld.getName()) && cache.allMainTopMap.containsKey(ctld.getUuid())) {
					ctld.setName(cache.allMainTopMap.get(ctld.getUuid()).getName());
				}
				if (ctld.getList() != null && ctld.getList().size() > 0) {
					// 补充SubTop列表的Name
					for (int j=0; j<ctld.getList().size(); j++) {
						RfTypeDTO ctd = ctld.getList().get(j);
						if (TextUtils.isEmpty(ctd.getName()) && cache.allSubTopMap.containsKey(ctd.getUuid())) {
							ctd.setName(cache.allSubTopMap.get(ctd.getUuid()).getName());
						}
					}
				}
			}
		}
		
		long l5 = System.currentTimeMillis();
	}
}
