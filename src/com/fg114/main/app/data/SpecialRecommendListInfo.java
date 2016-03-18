//package com.fg114.main.app.data;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.fg114.main.service.dto.ResAndFoodData;
//
///**
// * 本地缓存主页推荐餐厅列表
// * @author zhangyifan
// *
// */
//public class SpecialRecommendListInfo extends BaseInfo {
//	private List<ResAndFoodData> specialRecommendList;
//	
//	private double lastLongitude = 0;
//	private double lastLatitude = 0;
//	
//	public SpecialRecommendListInfo() {}
//
//	public double getLastLongitude() {
//		return lastLongitude;
//	}
//
//	public void setLastLongitude(double lastLongitude) {
//		this.lastLongitude = lastLongitude;
//	}
//
//	public double getLastLatitude() {
//		return lastLatitude;
//	}
//
//	public void setLastLatitude(double lastLatitude) {
//		this.lastLatitude = lastLatitude;
//	}
//
//	public List<ResAndFoodData> getSpecialRecommendList() {
//		return specialRecommendList;
//	}
//
//	public void setSpecialRecommendList(List<ResAndFoodData> specialRecommendList) {
//		this.specialRecommendList = specialRecommendList;
//	}
//
//	/**
//	 * json to bean
//	 * @param jObj
//	 * @return
//	 */
//	public static SpecialRecommendListInfo toBean(JSONObject jObj) {
//		
//		SpecialRecommendListInfo info = new SpecialRecommendListInfo();
//		
//		try {
//			
//			if (jObj.has("specialRecommendList")) {
//				List<ResAndFoodData> specialRecommendList = new ArrayList<ResAndFoodData>();
//				JSONArray jsonArray = jObj.getJSONArray("specialRecommendList");
//				if (jsonArray.length() > 0) {
//					for (int i = 0; i < jsonArray.length(); i ++) {
//						specialRecommendList.add(
//								ResAndFoodData.toBean(
//										jsonArray.getJSONObject(i)));
//					}
//				}
//				info.setSpecialRecommendList(specialRecommendList);
//			}
//			if (jObj.has("lastLongitude")) {
//				info.setLastLongitude(jObj.getDouble("lastLongitude"));
//			}
//			if (jObj.has("lastLatitude")) {
//				info.setLastLatitude(jObj.getDouble("lastLatitude"));
//			}
//			if (jObj.has("lastUpdateTime")) {
//				info.setLastUpdateTime(jObj.getLong("lastUpdateTime"));
//			}
//			if (jObj.has("timestamp")) {
//				info.setTimestamp(jObj.getLong("timestamp"));
//			}
//			
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return null;
//		}
//		
//		return info;
//	}
//}
