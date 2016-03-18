package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fg114.main.app.view.ItemData;
import com.fg114.main.util.JsonUtils;

/**
 * 通用类别列表DTO
 * @author qianjiefeng
 *
 */
public class CommonTypeListDTO extends BaseDTO  implements ItemData{
	private String uuid = "";
	private String name = "";
	private boolean selectTag;
	//列表 
	private List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
	
	// 自用属性
	// 主dto对象，根据uuid,name,selectTag三个属性构造
	private CommonTypeDTO mainDto;
	private String memo = "";
	private boolean needGroupBy;
	private String firstLetter;
	private String parentId;
	
	//本地属性，用于缓存
	private String cityName;
	private String keyword;
	private int pageCapacity;
	private int totalPage;
	private int currentPageIndex;
	
	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	

	public int getPageCapacity() {
		return pageCapacity;
	}

	public void setPageCapacity(int pageCapacity) {
		this.pageCapacity = pageCapacity;
	}

	public int getCurrentPageIndex() {
		return currentPageIndex;
	}

	public void setCurrentPageIndex(int currentPageIndex) {
		this.currentPageIndex = currentPageIndex;
	}

	//get,set-------------------------------------------------------------------
	public List<CommonTypeDTO> getList() {
		return list;
	}

	public void setList(List<? extends ItemData> list) {
		this.list = (List<CommonTypeDTO>)list;
	}
	
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isSelectTag() {
		return selectTag;
	}

	public void setSelectTag(boolean selectTag) {
		this.selectTag = selectTag;
	}
	
	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public CommonTypeDTO getMainDto() {
		if (mainDto == null) {
			mainDto = new CommonTypeDTO();
			mainDto.setUuid(uuid);
			mainDto.setName(name);
			mainDto.setMemo(memo);
			mainDto.setSelectTag(selectTag);
		}
		return mainDto;
	}

	public void setMainDto(CommonTypeDTO mainDto) {
		this.mainDto = mainDto;
	}

	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static CommonTypeListDTO toBean(JSONObject jObj) {
		
		CommonTypeListDTO dto = new CommonTypeListDTO();

		try {
			
			if (jObj.has("list")) {
				List<CommonTypeDTO> list = new ArrayList<CommonTypeDTO>();
				if (!jObj.isNull("list")) {
					JSONArray jsonArray = jObj.getJSONArray("list");
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); i ++) {
							list.add(
									CommonTypeDTO.toBean(
											jsonArray.getJSONObject(i)));
						}
					}
				}
				dto.setList(list);
			}
			if (jObj.has("uuid")) {
				dto.setUuid(jObj.getString("uuid"));
			}
			if (jObj.has("name")) {
				dto.setName(jObj.getString("name"));
			}
			if (jObj.has("selectTag")) {
				dto.setSelectTag(jObj.getBoolean("selectTag"));
			}
			if (jObj.has("needUpdateTag")) {
				dto.needUpdateTag=jObj.getBoolean("needUpdateTag");
			}
			if (jObj.has("timestamp")) {
				dto.timestamp=jObj.getLong("timestamp");
			}
			if (jObj.has("pgInfo")) {
				if (!jObj.isNull("pgInfo")) {
					dto.pgInfo=JsonUtils.fromJson(jObj.getJSONObject("pgInfo").toString(), PgInfo.class);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			
		}
		return dto;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonTypeListDTO) {
			return uuid.equals(((CommonTypeListDTO) o).getUuid());
		}
		return false;
	}

	@Override
	public String getParentId() {
		return this.parentId;
	}

	@Override
	public void setParentId(String parentId) {
		this.parentId=parentId;
	}

	@Override
	public void setIsNeedGroupBy(boolean needGroupBy) {
		this.needGroupBy=needGroupBy;
	}

	@Override
	public boolean isNeedGroupBy() {
		return needGroupBy;
	}

	@Override
	public void setFirstLetter(String firstLetter) {
		this.firstLetter=firstLetter;			
	}

	@Override
	public String getFirstLetter() {
		return this.firstLetter;
	}
}
