package com.fg114.main.app.data;


/**
 * 筛选条件缓存
 * @author zhangyifan
 *
 */
public class RealTimeResFilter{

	private int distanceMeter = 0;		//附近距离   		约定：0：不是附近搜索    其他：  500米 ，1000米 ，2000米等
	private String regionId = "";		//地域ID  		约定 0：全部地域  其他：所选地域ID 可传空
	private String districtId = "";	//商区ID   		约定0:全部商区  其他：所选商区ID 可传空
	private String mainMenuId = "0";	//菜系类别ID		约定 0:为全部菜系  其他：所选菜系ID
	private String subMenuId = "0";		//菜系类别ID		约定 0:为全部子菜系  其他：所选菜
	private int sortTypeTag = 0;		//排序类别     		约定:1:距离  2：人均  3:热订  4:商务宴请合适度  5:情侣约会合适度  6:家庭聚会合适度
	private String avgTag = "0"; 		//按人均筛选 		约定 0：所有 1...
	private long selectTime = System.currentTimeMillis();

	public RealTimeResFilter() {}
	
	/**
	 * 重置
	 */
	public void reset(){
		distanceMeter = 0;
		regionId = "";
		districtId = "";
		mainMenuId = "0";
		subMenuId = "0";
		sortTypeTag = 0;
		avgTag = "0";
		selectTime = System.currentTimeMillis();
	}
	
	public long getSelectTime() {
		return selectTime;
	}

	public void setSelectTime(long selectTime) {
		this.selectTime = selectTime;
	}

	public String getAvgTag() {
		return avgTag;
	}

	public void setAvgTag(String avgTag) {
		this.avgTag = avgTag;
	}

	public String getMainMenuId() {
		return mainMenuId;
	}

	public void setMainMenuId(String mainMenuId) {
		this.mainMenuId = mainMenuId;
	}

	public String getSubMenuId() {
		return subMenuId;
	}

	public void setSubMenuId(String subMenuId) {
		this.subMenuId = subMenuId;
	}

	public int getDistanceMeter() {
		return distanceMeter;
	}

	public void setDistanceMeter(int distanceMeter) {
		this.distanceMeter = distanceMeter;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getDistrictId() {
		return districtId;
	}

	public void setDistrictId(String districtId) {
		this.districtId = districtId;
	}

	public int getSortTypeTag() {
		return sortTypeTag;
	}

	public void setSortTypeTag(int sortTypeTag) {
		this.sortTypeTag = sortTypeTag;
	}
}
