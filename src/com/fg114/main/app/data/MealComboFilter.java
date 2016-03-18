package com.fg114.main.app.data;


/**
 * 筛选条件缓存
 * @author zhangyifan
 *
 */
public class MealComboFilter{

	private int distanceMeter = 0;		//附近距离   		约定：0：不是附近搜索    其他：  500米 ，1000米 ，2000米等
	private String regionId = "";		//地域ID  		约定 0：全部地域  其他：所选地域ID 可传空
	private String districtId = "";	//商区ID   		约定0:全部商区  其他：所选商区ID 可传空
	private String channelId = "1";		//频道ID    		约定: 1：餐馆  2：美食
	private String mainMenuId = "0";	//菜系类别ID		约定 0:为全部菜系  其他：所选菜系ID
	private String subMenuId = "0";		//菜系类别ID		约定 0:为全部子菜系  其他：所选菜
	private String keywords;			//搜索关键词
	private String sortTypeTag = "";		//排序类别     		约定:1:距离  2：人均  3:热订  4:商务宴请合适度  5:情侣约会合适度  6:家庭聚会合适度
	private String avgTag = "0"; 		//按人均筛选 		约定 0：所有 1...
	private String mainTopRestTypeId = ""; //主榜单类别 约定 "":为全部主榜单  其他：所选主榜单ID
	private String subTopRestTypeId = ""; //子榜单类别 约定 "":为全部子榜单类别  其他：所选子榜单类别ID

	public MealComboFilter() {}
	
	/**
	 * 重置
	 */
	public void reset(){
		distanceMeter = 0;
		regionId = "";
		districtId = "";
		channelId = "1";
		mainMenuId = "0";
		subMenuId = "0";
		keywords = "";
		sortTypeTag = "";
		avgTag = "0";
		mainTopRestTypeId = "";
		subTopRestTypeId = "";
	}
	
	
	public String getMainTopRestTypeId() {
		return mainTopRestTypeId;
	}

	public void setMainTopRestTypeId(String mainTopRestTypeId) {
		this.mainTopRestTypeId = mainTopRestTypeId;
	}

	public String getSubTopRestTypeId() {
		return subTopRestTypeId;
	}

	public void setSubTopRestTypeId(String subTopRestTypeId) {
		this.subTopRestTypeId = subTopRestTypeId;
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

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public synchronized final String getSortTypeTag()
	{
		return sortTypeTag;
	}

	public synchronized final void setSortTypeTag(String sortTypeTag)
	{
		this.sortTypeTag = sortTypeTag;
	}

	
}
