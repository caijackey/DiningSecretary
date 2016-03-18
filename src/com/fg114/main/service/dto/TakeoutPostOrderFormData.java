package com.fg114.main.service.dto;

import java.util.List;

/**
 * ####外卖表单信息####
 *
 */
public class TakeoutPostOrderFormData {
	//外送提示(标红)
    public String hintForSend;
	//用户默认收货地址  
    public UserTkRaData userReceiveAdressData;
    //送餐时间列表   uuid:uuid  name:时间  selectTag:是否选中  (都没选中，客户端默认选第一个)
    public List<CommonTypeDTO> sendTimeList=null;
	//是否可以在线支付
    public boolean canOnlinePayTag;
    //是否可以货到付款
    public boolean canOfflinePayTag;
    //选择的菜品
    public TakeoutMenuSelPackDTO menuSelPack;
    //是否可以下单
    public boolean canOrderTag;    
	//菜品总计或无法下单提示(标红)
    public String hintForSum;
}
