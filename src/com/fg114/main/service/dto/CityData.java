package com.fg114.main.service.dto;

/**
 * 城市信息
 * @author qianjiefeng
 *
 */
public class CityData  {
    //城市ID 
	public String cityId;
    //城市名称
	public String cityName;
    //首字母
	public String firstLetter;
    //拼音
	public String pinyin;

    //九宫格数据//九宫格数据  首页;外卖;优惠;附近;商圈;榜单;快捷预订
	public String mainMenuInfo; 
    //电话
	public String phone;
	//是否显示
	public boolean showTag;
	//是否是热门城市
	public boolean hotTag;



	
	//get,set-------------------------------------------------------------------
	public String getCityId() {
		return cityId;
	}
	public void setCityId(String cityId) {
		this.cityId = cityId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getFirstLetter() {
		return firstLetter;
	}
	public void setFirstLetter(String firstLetter) {
		this.firstLetter = firstLetter;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public String getMainMenuInfo() {
		return mainMenuInfo;
	}
	public void setMainMenuInfo(String mainMenuInfo) {
		this.mainMenuInfo = mainMenuInfo;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public boolean isShowTag() {
		return showTag;
	}
	public void setShowTag(boolean showTag) {
		this.showTag = showTag;
	}
	public boolean isHotTag() {
		return hotTag;
	}
	public void setHotTag(boolean hotTag) {
		this.hotTag = hotTag;
	}




	
}
