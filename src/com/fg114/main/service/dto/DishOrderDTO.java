package com.fg114.main.service.dto;

import java.io.Serializable;
import java.util.*;

/**
 *  本地所用，缓存点菜单信息。
 * @author nieyinyin
 *
 */
public class DishOrderDTO implements Serializable {

	private static final long serialVersionUID = 1359564817005529201L;

	// 点菜单的Id（dishOrderId）
	private String orderId = "";  
	
	// 餐厅ID
	private String restId = "";
	// 桌号
	private String tableId = "";
	// 餐厅名称
	private String restName = "";

	// 已点的菜
	private Map<String, DishData> dishMap = new LinkedHashMap<String, DishData>();

	// 总共点的菜的数量
	private double totalNum;
	// 总共点的菜的价格
	private double totalPrice;
	// 总共点的时价菜的数量
	private double currentPriceDishNum;

	// 点菜的历史集合
	private HashMap<String, DishData> dishMapHistory = new HashMap<String, DishData>();
	// 时间戳
	private long timeStamp;

	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getRestName() {
		return restName;
	}

	public void setRestName(String restName) {
		this.restName = restName;
	}

	public String getRestId() {
		return restId;
	}

	public void setRestId(String restId) {
		this.restId = restId;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public HashMap<String, DishData> getDishMapHistory() {
		return dishMapHistory;
	}

	public void setDishMapHistory(HashMap<String, DishData> dishMapHistory) {
		this.dishMapHistory = dishMapHistory;
	}

	public Map<String, DishData> getDishMap() {
		return dishMap;
	}

	public void setDishMap(Map<String, DishData> dishMap) {
		this.dishMap = dishMap;
	}

	public double getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(double totalNum) {
		this.totalNum = totalNum;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public double getCurrentPriceDishNum() {
		return currentPriceDishNum;
	}

	public void setCurrentPriceDishNum(double currentPriceDishNum) {
		this.currentPriceDishNum = currentPriceDishNum;
	}

	/**
	 * 清空缓存中所有的数据
	 */
	public void clearAll() {
		List<String> list = getDishDataIdList();
		for (int i = 0; i < list.size(); i++) {
			dishMap.get(list.get(i)).setNum(0);
			dishMap.remove(list.get(i));
		}

		setTimeStamp(System.currentTimeMillis());
	}
	
	/**
	 * 清空缓存数据，除了免费菜
	 */
	public void clearAllExceptFreeDish(){
		List<String> list = getDishDataIdList();
		for (int i = 0; i < list.size(); i++) {
			DishData temp = dishMap.get(list.get(i));
			temp.setNum(0);
			dishMap.remove(list.get(i));
		}
	}

	/**
	 * 更新DishData的信息
	 * 
	 * @param data
	 */
	public void updateDishOrder(DishData data) {
		if (!dishMap.containsKey(data.getUuid())) {
			if (data.getNum() != 0) {
				dishMap.put(data.getUuid(), data);
			}
		}else {
			if (data.getNum() == 0) {
				dishMap.remove(data.getUuid());
			}else {
				dishMap.put(data.getUuid(), data);
			}
		}
		setTimeStamp(System.currentTimeMillis());
	}

	/**
	 * 返回List形式的DishData
	 * 
	 * @return
	 */
	public List<DishData> getDishDataList() {
		List<DishData> list = new ArrayList<DishData>();
		if (dishMap.size() == 0) {
			return list;
		}
//		for (String key : dishMap.keySet()) {
//			list.add(dishMap.get(key));
//		}
		list.addAll(dishMap.values());
		return list;
	}

	/**
	 * 返回List形式的DishData的Id集合
	 * 
	 * @return
	 */
	public List<String> getDishDataIdList() {
		List<String> list = new ArrayList<String>();
		if (dishMap.size() == 0) {
			return list;
		}
		for (String key : dishMap.keySet()) {
			list.add(dishMap.get(key).getUuid());
		}
		return list;
	}

	/**
	 * 返回List形式的点菜历史DishData
	 * 
	 * @return
	 */
	public List<DishData> getDishDataHistoryList() {
		List<DishData> list = new ArrayList<DishData>();
		if (dishMapHistory.size() == 0) {
			return list;
		}
		for (String key : dishMapHistory.keySet()) {
			list.add(dishMapHistory.get(key));
		}
		return list;
	}

	/**
	 * 返回List形式的点菜历史DishData的Id集合
	 * 
	 * @return
	 */
	public List<String> getDishDataHistoryIdList() {
		List<String> list = new ArrayList<String>();
		if (dishMapHistory.size() == 0) {
			return list;
		}
		for (String key : dishMapHistory.keySet()) {
			list.add(dishMapHistory.get(key).getUuid());
		}
		return list;
	}
	
	public void reset() {
		 dishMap.clear();
		 totalNum = 0;
		 totalPrice = 0;
		 currentPriceDishNum = 0;
		 dishMapHistory.clear();
		 timeStamp = 0;
		 orderId = "";
	}
}
