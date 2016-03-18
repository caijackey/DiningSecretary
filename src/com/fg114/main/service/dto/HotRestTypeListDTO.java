package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class HotRestTypeListDTO  {
	
	//CommonTypeDTO   uuid,name,parentId  商区id ,商区名称 ，地域id
	public CommonTypeListDTO districtDTO=new CommonTypeListDTO();
	//CommonTypeDTO   uuid,name,parentId  菜系id ,菜系名称 ，主菜系id
	public CommonTypeListDTO menuDTO=new CommonTypeListDTO();
	//CommonTypeDTO   uuid,name  地铁线路id ,地铁线路名称 
	public CommonTypeListDTO subwayDTO=new CommonTypeListDTO();
	//CommonTypeDTO   uuid,name,parentId  子榜单id ,子榜单名称 ，主榜单id
	public CommonTypeListDTO topRestTypeDTO;
	
}
