package com.fg114.main.util;

/**
 * 坐标计算工具
 * @author wufucheng
 *
 */
public class GeoUtils {

	private static final double EARTH_RADIUS = 6378137.0;
	private static final double COORDINATE_ACCURACY = 10000.0;
	private static final String STRING_NORTH = "北";
	private static final String STRING_EAST = "东";
	private static final String STRING_SOUTH = "南";
	private static final String STRING_WEST = "西";

	/**
	 * 计算两点距离，单位为米
	 * @param latA
	 * @param lonA
	 * @param latB
	 * @param lonB
	 * @return
	 */
	public static double getDistance(double latA, double lonA, double latB, double lonB) {
		double radLat1 = (latA * Math.PI / 180.0);
		double radLat2 = (latB * Math.PI / 180.0);
		double latDis = radLat1 - radLat2;
		double lonDis = (lonA - lonB) * Math.PI / 180.0;
		double dis = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latDis / 2), 2) 
				+ Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(lonDis / 2), 2)));
		dis = dis * EARTH_RADIUS;
		dis = Math.round(dis * 1000.0) / 1000.0;
		return dis;
	}

	/**
	 * 计算目标点之于源点的方位
	 * @param latSrc 源点纬度
	 * @param lonSrc 源点经度
	 * @param latDest 目标点纬度
	 * @param lonDest 目标点经度
	 * @return
	 */
	public static String getDirection(double latSrc, double lonSrc, double latDest, double lonDest) {
		latSrc = Math.round(latSrc * COORDINATE_ACCURACY + 0.5) / COORDINATE_ACCURACY;
		lonSrc = Math.round(lonSrc * COORDINATE_ACCURACY + 0.5) / COORDINATE_ACCURACY;
		latDest = Math.round(latDest * COORDINATE_ACCURACY + 0.5) / COORDINATE_ACCURACY;
		lonDest = Math.round(lonDest * COORDINATE_ACCURACY + 0.5) / COORDINATE_ACCURACY;
		String vertical = "";
		String horizontal = "";
		StringBuffer sbResult = new StringBuffer();
		if (latDest > latSrc) {
			vertical = STRING_NORTH;
		} else if (latDest < latSrc) {
			vertical = STRING_SOUTH;
		}
		if (lonDest > lonSrc) {
			horizontal = STRING_EAST;
		} else if (lonDest < lonSrc) {
			horizontal = STRING_WEST;
		}
		sbResult.append(horizontal);
		sbResult.append(vertical);
		return sbResult.toString();
	}
	//从某个经纬值算出一个以３０米为误差的舍入值
	public static int formatLongLat(double dIn)
    {
    	int nTmp=(int)(dIn*100000);
    	int n1=nTmp/30;
    	int n2=nTmp%30;
    	if(n2<=15)
    		return n1;
    	else
    		return n1+1;
    }	
}