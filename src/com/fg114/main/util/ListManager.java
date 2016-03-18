package com.fg114.main.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.data.BaseData;
import com.fg114.main.app.data.ChannelInfo;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.CityListInfo;
import com.fg114.main.app.data.DistrictListInfo;
import com.fg114.main.app.data.HistoryListInfo;
import com.fg114.main.app.data.HotDistrictInfo;
import com.fg114.main.app.data.MainMenuListInfo;
import com.fg114.main.app.data.RegionListInfo;
import com.fg114.main.app.data.SearchHistoryInfo;
import com.fg114.main.app.data.TopListInfo;
import com.fg114.main.service.dto.CityData;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.ErrorReportTypeListPackDTO;
import com.fg114.main.service.dto.MainMenuData;
import com.fg114.main.service.dto.ResAndFoodData;
import com.fg114.main.service.dto.RestSearchSuggestListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.SuggestResultData;
import com.google.xiaomishujson.Gson;

/**
 * 列表管理
 * @author zhangyifan
 *
 */
public class ListManager{
	private CityListInfo cityListInfo = null;
	private TopListInfo topListInfo = null;
	private HistoryListInfo historyListInfo = null;
	private HotDistrictInfo hotDistrictInfo = null;
	private RegionListInfo regionListInfo = null;
	private DistrictListInfo districtListInfo = null;
	private ChannelInfo channelInfo = null;
	private SearchHistoryInfo searchHistoryInfo = null;
	private SearchHistoryInfo searchPoiHistoryInfo = null;
	
	//缓存错误类型列表
	private ErrorReportTypeListPackDTO errorReportTypeListPack=null;
	
	//短信邀请，最近联系人列表
	private CommonTypeListDTO recentPersons=null;
	private RestSearchSuggestListDTO restSearchSuggestHistory;
	//搜索历史列表====================================================================================
	/**
	 * 从本地缓存中获得搜索历史列表
	 */
	public List<SuggestResultData> getRestSearchSuggestHistoryList() {
		List<SuggestResultData> list=new ArrayList<SuggestResultData>();
		Context context=ContextUtil.getContext();
		CityInfo city=SessionManager.getInstance().getCityInfo(context);
		String jsonStr = SharedprefUtil.get(context, Settings.REST_SEARCH_SUGGEST_HISTORY_LIST_KEY, "{}");
		restSearchSuggestHistory = JsonUtils.fromJson(jsonStr,RestSearchSuggestListDTO.class);
		if(restSearchSuggestHistory!=null && restSearchSuggestHistory.list!=null){
			list=restSearchSuggestHistory.list;
		}
		return list;
	}
	/**
	 * 将搜索历史列表设置为本地缓存
	 */
	public void setRestSearchSuggestHistoryListDTO(RestSearchSuggestListDTO dto) {
		Context context=ContextUtil.getContext();
		CityInfo city=SessionManager.getInstance().getCityInfo(context);
		this.restSearchSuggestHistory = dto;
		String jsonStr = null;
		if (restSearchSuggestHistory != null) {
			jsonStr = new Gson().toJson(restSearchSuggestHistory, RestSearchSuggestListDTO.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.REST_SEARCH_SUGGEST_HISTORY_LIST_KEY, jsonStr);
	}
	/**
	 * 加入搜索历史
	 * @param info
	 */
	public void addSearchHistoryInfo(SuggestResultData dto) {
		Context context=ContextUtil.getContext();
			List<SuggestResultData> list = restSearchSuggestHistory.list;
			if (list == null) {
				list = new ArrayList<SuggestResultData>();
			}
			//如果列表中存在该项则删除该项，保持唯一性
			int idx = -1;
			for (int i=0; i<list.size(); i++) {
				if (dto.restName!=null && dto.restName.equals(list.get(i).restName)) {
					idx = i;
					break;
				}
			}
			if (idx > -1) {
				list.remove(idx);
			}
			//将该项添加到列顶
			list.add(0, dto);
			
			//将结果与缓存同步
			restSearchSuggestHistory.list=list.subList(0, list.size()>30?30:list.size());	//最多30条
			setRestSearchSuggestHistoryListDTO(restSearchSuggestHistory);

	}
	/**
	 * 清空浏览历史列表
	 */
	public void removeAllRestSearchSuggestHistory() {
		Context context=ContextUtil.getContext();
		if (this.restSearchSuggestHistory != null && restSearchSuggestHistory.list!=null) {
			this.restSearchSuggestHistory.list.clear();
		}
		String jsonStr = "{}";
		SharedprefUtil.save(context, Settings.REST_SEARCH_SUGGEST_HISTORY_LIST_KEY, jsonStr);
	}
	//------------------------------------------------------------------------------
	//------------------------------------------------------------------------------
	/**
	 * 从本地缓存中获得搜索历史列表
	 */
	public SearchHistoryInfo getSearchHistoryListInfo(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.SEARCH_HISTORY_LIST_KEY, "{}");
			searchHistoryInfo = SearchHistoryInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return searchHistoryInfo;
	}
	/**
	 * 将搜索历史列表设置为本地缓存
	 */
	public void setSearchHistoryInfo(Context context, SearchHistoryInfo searchHistoryInfo) {
		this.searchHistoryInfo = searchHistoryInfo;
		String jsonStr = null;
		if (searchHistoryInfo != null) {
			jsonStr = new Gson().toJson(searchHistoryInfo, SearchHistoryInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.SEARCH_HISTORY_LIST_KEY, jsonStr);
	}
	/**
	 * 加入搜索历史
	 * @param info
	 */
	public void addSearchHistoryInfo(Context context, CommonTypeDTO dto, String channelId) {
		
		if (channelId.equals("1")) {
			List<CommonTypeDTO> list = searchHistoryInfo.getResList();
			if (list == null) {
				list = new ArrayList<CommonTypeDTO>();
			}
			//如果列表中存在该项则删除该项，保持唯一性
			int idx = -1;
			for (int i=0; i<list.size(); i++) {
				if (list.get(i).getName().equals(dto.getName())) {
					idx = i;
					break;
				}
			}
			if (idx > -1) {
				list.remove(idx);
			}
			//将该项添加到列顶
			list.add(0, dto);
			//将结果与缓存同步
			searchHistoryInfo.setResList(list.subList(0, list.size()>30?30:list.size()));	//最多30条
			setSearchHistoryInfo(context, searchHistoryInfo);
		}
		else if (channelId.equals("2")) {
			List<CommonTypeDTO> list = searchHistoryInfo.getFoodList();
			if (list == null) {
				list = new ArrayList<CommonTypeDTO>();
			}
			//如果列表中存在该项则删除该项，保持唯一性
			int idx = -1;
			for (int i=0; i<list.size(); i++) {
				if (list.get(i).getName().equals(dto.getName())) {
					idx = i;
					break;
				}
			}
			if (idx > -1) {
				list.remove(idx);
			}
			//将该项添加到列顶
			list.add(0, dto);
			//将结果与缓存同步
			searchHistoryInfo.setFoodList(list);
			setSearchHistoryInfo(context, searchHistoryInfo);
		}
	}
	/**
	 * 清空浏览历史列表
	 */
	public void removeAllSearchHistoryInfo(Context context) {
		if (this.searchHistoryInfo != null) {
			this.searchHistoryInfo.getResList().clear();
			this.searchHistoryInfo.getFoodList().clear();
		}
		String jsonStr = "{}";
		SharedprefUtil.save(context, Settings.SEARCH_HISTORY_LIST_KEY, jsonStr);
	}
	
	//城市列表======================================================================================
	/**
	 * 从本地缓存中获得城市列表
	 */
	public CityListInfo getCityListInfo(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.CITY_LIST_KEY, "{}");
			cityListInfo = CityListInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cityListInfo;
	}
	
	/**
	 * 将城市列表设置为本地缓存
	 */
	public void setCityListInfo(Context context, CityListInfo cityListInfo) {
		this.cityListInfo = cityListInfo;
		String jsonStr = null;
		if (cityListInfo != null) {
			jsonStr = new Gson().toJson(cityListInfo, CityListInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.CITY_LIST_KEY, jsonStr);
	}
	
	/**
	 * 从本地缓存中获得榜单列表
	 */
	public TopListInfo getTopListInfo(Context context, String cityId) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.TOP_LIST_KEY+"_"+cityId, "{}");
			topListInfo = TopListInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return topListInfo;
	}
	/**
	 * 将榜单列表设置为本地缓存
	 */
	public void setTopListInfo(Context context, String cityId, TopListInfo topListInfo) {
		this.topListInfo = topListInfo;
		String jsonStr = null;
		if (topListInfo != null) {
			jsonStr = new Gson().toJson(topListInfo, TopListInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.TOP_LIST_KEY+"_"+cityId, jsonStr);
	}
	
	//热门商区列表======================================================================================
	/**
	 * 从本地缓存中获得热门商区列表
	 */
	public HotDistrictInfo getHotDistrictInfo(Context context, String cityId) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.HOT_DISTRICT_KEY+"_"+cityId, "{}");
			hotDistrictInfo = HotDistrictInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return hotDistrictInfo;
	}
	/**
	 * 将热门商区列表设置为本地缓存
	 */
	public void setHotDistrictInfo(Context context, String cityId, HotDistrictInfo hotDistrictInfo) {
		this.hotDistrictInfo = hotDistrictInfo;
		String jsonStr = null;
		if (hotDistrictInfo != null) {
			jsonStr = new Gson().toJson(hotDistrictInfo, HotDistrictInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.HOT_DISTRICT_KEY+"_"+cityId, jsonStr);
	}
	
	//地域列表======================================================================================
	/**
	 * 从本地缓存中获得地域列表
	 */
	public RegionListInfo getRegionListInfo(Context context, String cityId) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.REGION_KEY+"_"+cityId, "{}");
			regionListInfo = RegionListInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return regionListInfo;
	}
	/**
	 * 将地域列表设置为本地缓存
	 */
	public void setRegionListInfo(Context context, String cityId, RegionListInfo regionListInfo) {
		this.regionListInfo = regionListInfo;
		String jsonStr = null;
		if (regionListInfo != null) {
			jsonStr = new Gson().toJson(regionListInfo, RegionListInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.REGION_KEY+"_"+cityId, jsonStr);
	}
	
	//商区列表======================================================================================
	/**
	 * 从本地缓存中获得商区列表
	 */
	public DistrictListInfo getDistrictListInfo(Context context, String regionId) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.DISTRICT_KEY+"_"+regionId, "{}");
			districtListInfo = DistrictListInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return districtListInfo;
	}
	/**
	 * 将商区列表设置为本地缓存
	 */
	public void setDistrictListInfo(Context context, String regionId, DistrictListInfo districtListInfo) {
		this.districtListInfo = districtListInfo;
		String jsonStr = null;
		if (districtListInfo != null) {
			jsonStr = new Gson().toJson(districtListInfo, DistrictListInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.DISTRICT_KEY+"_"+regionId, jsonStr);
	}
	
	//频道列表======================================================================================
	/**
	 * 从本地缓存中获得频道列表
	 */
	public ChannelInfo getChannelInfo(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.CHANNEL_KEY, "{}");
			channelInfo = ChannelInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return channelInfo;
	}
	/**
	 * 将频道列表设置为本地缓存
	 */
	public void setChannelInfo(Context context, ChannelInfo channelInfo) {
		this.channelInfo = channelInfo;
		String jsonStr = null;
		if (channelInfo != null) {
			jsonStr = new Gson().toJson(channelInfo, ChannelInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.CHANNEL_KEY, jsonStr);
	}
	
	//浏览历史列表====================================================================================
	/**
	 * 从本地缓存中获得浏览历史列表
	 */
	public HistoryListInfo getHistoryListInfo(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.HISTORY_LIST_KEY, "{}");
			historyListInfo = HistoryListInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return historyListInfo;
	}
	/**
	 * 将浏览历史列表设置为本地缓存
	 */
	public void setHistoryListInfo(Context context, HistoryListInfo historyListInfo) {
		String jsonStr = null;
		try {
			this.historyListInfo = historyListInfo;
			if (historyListInfo != null) {
				jsonStr = new Gson().toJson(historyListInfo, HistoryListInfo.class);
			} else {
				jsonStr = "{}";
			}
			SharedprefUtil.save(context, Settings.HISTORY_LIST_KEY, jsonStr);
		} catch (Exception e) {
			ActivityUtil.saveException(e, jsonStr);
		}
	}
	/**
	 * 加入浏览历史
	 * @param info
	 */
	public void addHistoryList(Context context, ResAndFoodData data) {
		
		if (historyListInfo == null) {
			historyListInfo = this.getHistoryListInfo(context);
		}
		List<ResAndFoodData> list = historyListInfo.getHistoryList();
		if (list == null) {
			list = new ArrayList<ResAndFoodData>();
		}
		//获得ID
		String id = (data.getTag() == Integer.parseInt(Settings.STATUTE_CHANNEL_RESTAURANT) ? 
											data.getResId() : data.getFoodId());
		
		if (id == null || "".equals(id))	{
			return;
		}
		
		//如果列表中存在该项则删除该项，保持唯一性
		String HistoryId  = "";
		for(ResAndFoodData d : list){
			HistoryId = (d.getTag() == Integer.parseInt(Settings.STATUTE_CHANNEL_RESTAURANT) ? 
											d.getResId() : d.getFoodId());
			if(d.getTag() == data.getTag() && id.equals(HistoryId)){
				list.remove(d);
				break;
			}
		}
		
		//将该项添加到列顶
		list.add(0, data.clone(false));
		
		//如果超过50项，则删除最后一项
		if (list.size() >= 50) {
			list.remove(list.size() - 1);
		}
		//将结果与缓存同步
		historyListInfo.setHistoryList(list);
		setHistoryListInfo(context, historyListInfo);
	}
	/**
	 * 清空浏览历史列表
	 */
	public void removeAllHistoryListInfo(Context context, HistoryListInfo historyListInfo) {
		if (historyListInfo != null) {
			historyListInfo.getHistoryList().clear();
		}
		this.historyListInfo = historyListInfo;
		String jsonStr = "{}";
		SharedprefUtil.save(context, Settings.HISTORY_LIST_KEY, jsonStr);
	}
	



	
	/**
	 * 获得距离列表
	 */
	public List<RfTypeDTO> getDistanceList(Context context) {
		List<RfTypeDTO> list = new ArrayList<RfTypeDTO>();
		String[] distanceArray = context.getResources().getStringArray(R.array.distance_list);
		RfTypeDTO data;
		for (String distance : distanceArray) {
			data = new RfTypeDTO();
			data.setUuid(distance);
			data.setName(distance + "米");
//			data.setRank(0);
			list.add(data);
		}
		return list;
	}
	//错误类型列表====================================================================================
	/**
	 * 从本地缓存中获得搜索历史列表
	 */
	public ErrorReportTypeListPackDTO getErrorReportTypeListPack(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.ERROR_REPORT_TYPE_LIST_PACK_KEY, "{}");
			errorReportTypeListPack = ErrorReportTypeListPackDTO.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errorReportTypeListPack;
	}
	/**
	 * 将错误类型列表设置为本地缓存
	 */
	public void setErrorReportTypeListPack(Context context, ErrorReportTypeListPackDTO errorReportTypeListPack) {
		this.errorReportTypeListPack = errorReportTypeListPack;
		String jsonStr = null;
		if (errorReportTypeListPack != null) {
			jsonStr = new Gson().toJson(errorReportTypeListPack, ErrorReportTypeListPackDTO.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.ERROR_REPORT_TYPE_LIST_PACK_KEY, jsonStr);
	}
	//短信邀请模块中最近联系人缓存====================================================================================
	/**
	 * 从本地缓存中获得最近联系人列表
	 */
	public List<CommonTypeDTO> getRecentPersonList(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.SMS_INVITE_RECENT_PERSON_KEY, "{}");
			recentPersons = CommonTypeListDTO.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return recentPersons.getList();
	}
	/**
	 * 将最近联系人存储到本地缓存
	 */
	public void setRecentPersonList(Context context, List<CommonTypeDTO> personList) {
		String jsonStr = null;
		if(personList==null){
			jsonStr = "{}";
		}
		else{
			this.recentPersons.setList(personList);
			jsonStr = new Gson().toJson(recentPersons, CommonTypeListDTO.class);
		}
		SharedprefUtil.save(context, Settings.SMS_INVITE_RECENT_PERSON_KEY, jsonStr);
	}
	//-------------------------------------------------------------
	//---------------------------外卖poi搜索缓存
	/**
	 * 从本地缓存中获得搜索历史列表
	 */
	public SearchHistoryInfo getPoiSearchHistoryListInfo(Context context) {
		try {
			String jsonStr = SharedprefUtil.get(context, Settings.SEARCH_POI_HISTORY_LIST_KEY, "{}");
			searchPoiHistoryInfo = SearchHistoryInfo.toBean(new JSONObject(jsonStr));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return searchPoiHistoryInfo;
	}
	/**
	 * 将搜索历史列表设置为本地缓存
	 */
	public void setPoiSearchHistoryInfo(Context context, SearchHistoryInfo searchHistoryInfo) {
		this.searchPoiHistoryInfo = searchHistoryInfo;
		String jsonStr = null;
		if (searchHistoryInfo != null) {
			jsonStr = new Gson().toJson(searchHistoryInfo, SearchHistoryInfo.class);
		} else {
			jsonStr = "{}";
		}
		SharedprefUtil.save(context, Settings.SEARCH_POI_HISTORY_LIST_KEY, jsonStr);
	}
	/**
	 * 加入搜索历史
	 * @param info
	 */
	public void addPoiSearchHistoryInfo(Context context, CommonTypeDTO dto) {
		
			List<CommonTypeDTO> list = searchPoiHistoryInfo.getResList();
			if (list == null) {
				list = new ArrayList<CommonTypeDTO>();
			}
			//如果列表中存在该项则删除该项，保持唯一性
			int idx = -1;
			for (int i=0; i<list.size(); i++) {
				if (list.get(i).getName().equals(dto.getName())) {
					idx = i;
					break;
				}
			}
			if (idx > -1) {
				list.remove(idx);
			}
			//将该项添加到列顶
			list.add(0, dto);
			//将结果与缓存同步
			searchPoiHistoryInfo.setResList(list.subList(0, list.size()>30?30:list.size()));	//最多30条
			setPoiSearchHistoryInfo(context, searchPoiHistoryInfo);
	}
	/**
	 * 清空浏览历史列表
	 */
	public void removeAllPoiSearchHistoryInfo(Context context) {
		if (this.searchPoiHistoryInfo != null) {
			this.searchPoiHistoryInfo.getResList().clear();
		}
		String jsonStr = "{}";
		SharedprefUtil.save(context, Settings.SEARCH_POI_HISTORY_LIST_KEY, jsonStr);
	}
	
}
