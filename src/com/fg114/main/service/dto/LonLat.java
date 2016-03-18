package com.fg114.main.service.dto;

import java.io.Serializable;

/**
 * 地点类，记录了经纬度和地点名称
 * @author wufucheng
 *
 */

public class LonLat implements Serializable {
	
	public double longitude;
	public double latitude;
	public String name;
	public String address;
	
	public LonLat() {
		
	}
	
	public LonLat(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public LonLat(double longitude, double latitude, String name, String address) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.name = name;
		this.address = address;
	}
	
	public boolean equalLonLat(LonLat lonlat) {
		if (this.longitude == lonlat.longitude && this.latitude == lonlat.latitude) {
			return true;
		}
		return false;
	}
	
	public String toPair() {
		return this.longitude + "," + this.latitude;
	}
}
