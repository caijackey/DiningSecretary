package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 地图信息类
 * @author wufucheng
 *
 */
public class MapInfo implements Serializable {
	
	public LonLat center;	//全部结果同时显示的适宜中心经纬度
	public int scale;	//全部结果同时显示的适宜缩放比例
}
