package com.fg114.main.service.dto;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 首页菜单对象
 * @author qianjiefeng
 *
 */
public class MainMenuData  {
	 //功能标志  约定  1：超级小秘书  2：附件餐馆  3：查询餐厅  4：周边好吃的 5：榜单  6:今日七折  7登录  8:最近浏览  9我的订单
	//4.1.2 九宫格数据  首页;外卖;优惠;附近;商圈;榜单;快捷预订
	private int tag;
	//名称
	private String name;
	//图标url (默认)
	private String iconUrl;
	//图标url (按下时)
	private String iconMouseDownUrl;
	//是否显示 
	private boolean showTag;

	private String OrderAndMsgNum;
	
	//get,set-------------------------------------------------------------------
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getIconMouseDownUrl() {
		return iconMouseDownUrl;
	}
	public void setIconMouseDownUrl(String iconMouseDownUrl) {
		this.iconMouseDownUrl = iconMouseDownUrl;
	}
	public boolean isShowTag() {
		return showTag;
	}
	public void setShowTag(boolean showTag) {
		this.showTag = showTag;
	}
	
	
	
	public String getOrderAndMsgNum() {
		return OrderAndMsgNum;
	}
	public void setOrderAndMsgNum(String orderAndMsgNum) {
		OrderAndMsgNum = orderAndMsgNum;
	}
	/**
	 * json to bean
	 * @param jObj
	 * @return
	 */
	public static MainMenuData toBean(JSONObject jObj) {
		
		MainMenuData data = new MainMenuData();
		
		try {
			
			if (jObj.has("tag")) {
				data.setTag(jObj.getInt("tag"));
			}
			if (jObj.has("name")) {
				data.setName(jObj.getString("name"));
			}
			if (jObj.has("iconUrl")) {
				data.setIconUrl(jObj.getString("iconUrl"));
			}
			if (jObj.has("iconMouseDownUrl")) {
				data.setIconMouseDownUrl(jObj.getString("iconMouseDownUrl"));
			}
			if (jObj.has("showTag")) {
				data.setShowTag(jObj.getBoolean("showTag"));
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return data;
	}
}
