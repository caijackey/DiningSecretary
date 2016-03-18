package com.fg114.main.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市列表DTO
 * @author qianjiefeng
 *
 */
public class RestTelInfo  {
    //根据是否是特约商户决定能否从电话链接处拨打, 特约商户:NO, 非特约商户:YES
   public  boolean isTelCanCall;
    // 电话号码
   public  String tel;
    // 区号
   public  String cityPrefix;
    // 分机号
   public  String branch;
	
	
	
	
	
}
