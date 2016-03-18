package com.fg114.main.service.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.SystemClock;
import android.util.Log;
import com.fg114.main.app.Settings;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.http.ServiceRequest.API.ParamType;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.google.xiaomishujson.Gson;

/**
 * 封装请求，参数验证 
 * @author xujianjun,2013-07-09
 */
public class ServiceRequest {
	
	public static enum API{
		
		/*--------------------------------------------------------------------------
		|  免单宝
		--------------------------------------------------------------------------*/
		//获取免单宝餐馆列表  返回MdbRestListDTO
		
		getMdbRestList("/getMdbRestList",
				new ParamProtocol()
				.define("distanceMeter",ParamType.LONG)//附近距离                        约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
				.define("regionId",ParamType.STRING)//地域ID 或 地铁线路id  约定"" ：全部地域                其他：所选地域ID
				.define("districtId",ParamType.STRING)//商区ID 或 地铁站id  约定""  :全部商区                   其他：所选商区ID
				.define("mainMenuId",ParamType.STRING)//主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
				.define("subMenuId",ParamType.STRING)//子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
				.define("sortTypeTag",ParamType.LONG)//排序  0：默认
				.define("avgTag",ParamType.LONG)//人均  0：默认
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页	
				),
				


		//获得餐馆页面数据  返回MdbRestInfoData
		getMdbRestInfo("/getMdbRestInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)
				),
        //加入收藏  返回Void
		addMdbRestToFav("/addMdbRestToFav",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//餐厅id
				),
		//删除收藏  返回Void
		delMdbRestFromFav("/delMdbRestFromFav",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//餐厅id
						),
		
		
		//获得查找餐馆页面附近餐馆列表 返回MdbSearchRestListDTO
		getMdbNearRestList("/getMdbNearRestList",
				new ParamProtocol()
						),

		
		//获得查找餐馆页面附近餐馆列表 返回MdbSearchRestListDTO
		getMdbSearchRestList("/getMdbSearchRestList",
				new ParamProtocol()
		.define("keywords",ParamType.STRING)//关键词
						),

		
		//获得免单订单表单数据 MdbFreeOrderFormData
		getMdbFreeOrderFormInfo("/getMdbFreeOrderFormInfo",
				new ParamProtocol()
		.define("typeTag",ParamType.LONG)//1:新增  2：修改
		.define("uuid",ParamType.STRING)//typeTag=1 餐厅id    typeTag=2 订单id

		),

		
//		//提交订单表单  MdbPostOrderResultData
//		postMdbFreeOrder("/postMdbFreeOrder",
//								new ParamProtocol()
//						.define("typeTag",ParamType.LONG)//1:新增  2：修改
//						.define("uuid",ParamType.STRING)//typeTag=1 餐厅id  typeTag=2 订单id
//						.define("payMoney",ParamType.DOUBLE)//支付金额  格式  12.12
//						.define("userTel",ParamType.STRING)//手机号码
//						.define("usedUserRemainMoney",ParamType.DOUBLE)//使用的账户余额
						
//										),
		
		
		//选择支付方式  CommonPostPayResultData
		//如果校验是否通过 chkPassTag:false 客户端需要提示errorHint
		postMdbSelFreeOrderPay("/postMdbSelFreeOrderPay",
								new ParamProtocol()					
						.define("orderId",ParamType.STRING)// 订单id
						.define("payTypeTag",ParamType.LONG)//支付方式												
						),
		
						
		//获得订单的状态  CommonOrderStateData
		getMdbFreeOrderState("/getMdbFreeOrderState",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)
		),
	
		
		//获得免单订单数据  MdbFreeOrderInfoData
		getMdbFreeOrderInfo("/getMdbFreeOrderInfo",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)
		),
		//取消订单//msg:提示
		cancelMdbFreeOrder("/cancelMdbFreeOrder",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)
		),

		
		
		//获得免单订单列表   MdbFreeOrderListDTO
		getMdbFreeOrderList("/getMdbFreeOrderList",
				new ParamProtocol()
		.define("statusId",ParamType.STRING)//状态id  默认为空    空为全部状态   其他为指定状态
		.define("pageSize",ParamType.LONG)//页面大小
		.define("startIndex",ParamType.LONG)//当前页
		),
		
		//清空订单 SimpleData
		operateMdbFreeOrder("/operateMdbFreeOrder",
				new ParamProtocol()
		.define("operateId",ParamType.STRING)//操作id
		),
			
		//订单献花  SimpleData  //msg:返回的提示
		postMdbOrderFlower("/postMdbOrderFlower",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)//订单id
		.define("flowerNum",ParamType.LONG)//1:一朵  3：三朵  5：五朵
		),

		
		//上传订单分享图片 SimpleData //msg:返回的提示
		uploadMdbOrderSharePic("/uploadMdbOrderSharePic",
				new ParamProtocol(true)
		.define("orderId",ParamType.STRING)//订单id
		),
		
		//获得免单宝核对表单数据  //MdbReceiptChkFormData
		getMdbReceiptChkFormInfo("/getMdbReceiptChkFormInfo",
				new ParamProtocol()
		),
		
		//获得免单宝核对表单验证码信息数据  //MdbRfValidCodeData
		getMdbReceiptChkFormValidCodeInfo("/getMdbReceiptChkFormValidCodeInfo",
				new ParamProtocol()
		),
		
		//获得免单宝核对确认表单数据  //MdbReceiptChkConfirmFormData  
		getMdbReceiptChkConfirmFormInfo("/getMdbReceiptChkConfirmFormInfo",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)//订单id
		),
		
		//提交订单表单  //MdbPostOrderResultData
		postMdbFreeOrder("/postMdbFreeOrder",
				new ParamProtocol()
		.define("authNum",ParamType.STRING)//授权号
		.define("cardNum",ParamType.STRING)//卡号
		.define("validCodeId",ParamType.STRING)//验证码Id
		.define("validCodeNum",ParamType.STRING)//验证码
		),
		
		//完善订单表单  //SimpleData //uuid是oiderID
		completeMdbFreeOrder("/completeMdbFreeOrder",
				new ParamProtocol()
		.define("orderId",ParamType.STRING)//订单id
		.define("waiterNum",ParamType.STRING)//服务员号码
		.define("userTel",ParamType.STRING)//用户手机号
		),

		
		/************************************服务接口定义**************************************************/	
		//成功分享到微信
		successShareToWinxin("/successShareToWinxin",
				new ParamProtocol()
		.define("wxTypeTag",ParamType.LONG)//微信类别 1:微信  2：朋友圈
		.define("shareTypeTag",ParamType.LONG)//同 shareTo的typeTag
		.define("shareUuid",ParamType.STRING)//同shareTo的uuid
		),

		//意见反馈
		postAppFeedback("/postAppFeedback",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG)//0:未选择 1：订餐厅问题  2：叫外卖问题   3：秘币问题  4：软件Bug问题  5：其他问题
				.define("detail",ParamType.STRING)
				.define("email",ParamType.STRING)
				),
		
		//绑定百度推送，返回JsonPack code=200表示成功
		bindBaiduPush("/bindBaiduPush",
				new ParamProtocol()
				.define("appid",ParamType.STRING)//百度方给我们的应用id
				.define("userId",ParamType.STRING)//唯一区别一个“客户端应用程序”的id，我们应该使用此值与我们系统用户绑定
				.define("channelId",ParamType.STRING)//唯一区别一个“手机设备”的id
				),
				
		//获得软件中的公共数据 //合并以前的checkVersion//软件打开时调用  ，然后定时每24小时调用，返回SoftwareCommonData
		getSoftwareCommonData("/getSoftwareCommonData",
				new ParamProtocol()
				.define("cityListTimestamp",ParamType.LONG)//时间戳    城市列表
				.define("errorReportTypeListTimestamp",ParamType.LONG)//时间戳   错误报告类别
				),
		//---------------------------------------------------------------------------------
		//通过gps获得城市id
		//软件打开后获取到gps信息后调用  然后定时每15分钟调用  超出1公里，返回SimpleData.uuid=返回城市id
		getCityIdByGps("/getCityIdByGps",
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//上传页面点击流
		//替换原来的  uploadOpenPageData
		//PageStatsData  去掉 菜品id fid  ，奖品id gid ，产品id pid
		//PageEventData  添加了备注字段  可以记录广告位id等
		uploadPageStats("/upload/uploadPageStats",
				new ParamProtocol(true) //需要inputstream数据，传入PageStatsPackData的json串的流
				),
		//---------------------------------------------------------------------------------
		//查询中国的经纬度，返回GpsData
		//替换原来的
		getChineseGps("/getChineseGps",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG)//类别    1:google  2:百度    3：mapbar
				.define("longitude",ParamType.LONG)//经度
				.define("latitude",ParamType.LONG)//纬度
				),		
				
		//---------------------------------------------------------------------------------
		//带关键词高亮的搜索建议，返回CommonTypeListDTO(name:关键词 ,num：结果数), 使用  <b></b>  来标红  比如  上海<b>川</b>菜馆的<b>川</b>菜
		//删除 参数  channelId  voiceInputTag
		getSuggestKeywordList("/getSuggestKeywordList",
				new ParamProtocol()
				.define("keywords",ParamType.STRING)//关键词
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),			
				
		//---------------------------------------------------------------------------------
		//餐馆搜索建议列表，返回RestSearchSuggestListDTO    使用  <b></b>  来标红  比如  上海<b>川</b>菜馆的<b>川</b>菜
		//增加roomTypeInfoData
		getRestSearchSuggestList("/getRestSearchSuggestList",
				new ParamProtocol()
				.define("keywords",ParamType.STRING)//关键词
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//开始索引 从1开始
				),	
		//---------------------------------------------------------------------------------
		//使用历史建议列表，返回UsedHistorySuggestListDTO
		//增加roomTypeInfoData
		getUsedHistorySuggestList("/getUsedHistorySuggestList",
				new ParamProtocol()
				),	
		//---------------------------------------------------------------------------------
		//获得热门商区列表  ，返回CommonTypeListDTO   uuid,name,parentId  商区id ,商区名称 ，地域id
		//代替以前的  getPageSelectHotDistrictData
		//缓存一天
		@Deprecated
		getHotDistrictList("/getHotDistrictList",
					new ParamProtocol()
					),	
		//---------------------------------------------------------------------------------
		//获得地域,商区的数据，返回RfTypeListPackDTO
		//RfTypeListDTO  中  u  n:   地域的uuid,name    list :地域里包含的商区列表
		//RfTypeDTO  中  u  n c : 商区的uuid,name,首字母
		//缓存一天
		@Deprecated
		getRegionDistrictList("/getRegionDistrictList",
				new ParamProtocol()
				),
		getRegionDistrictList2("/getRegionDistrictList2",
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//获取餐馆列表 ，返回RestListDTO
		//代替以前的 getResAndFoodList2
		getRestList("/getRestList",
				new ParamProtocol()
				.define("distanceMeter",ParamType.LONG)//附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
				.define("regionId",ParamType.STRING)//地域ID ,约定"" ：全部地域                其他：所选地域ID
				.define("districtId",ParamType.STRING)//商区ID,约定""  :全部商区                   其他：所选商区ID
				.define("mainMenuId",ParamType.STRING)//主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
				.define("subMenuId",ParamType.STRING)//子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
				.define("mainTopRestTypeId",ParamType.STRING)//主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
				.define("subTopRestTypeId",ParamType.STRING)//子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
				.define("keywords",ParamType.STRING)//搜索关键词
				.define("sortTypeTag",ParamType.LONG)//排序  0：默认
				.define("avgTag",ParamType.LONG)//人均  0：默认
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),		
		//---------------------------------------------------------------------------------
		//获得外卖餐厅列表 ，返回TakeoutRestListDTO
		getTakeoutRestList("/getTakeoutRestList",
				new ParamProtocol()
				.define("distanceMeter",ParamType.LONG)//附近距离  0:不是附近搜索   约定： 3000米等
				.define("haveGpsRectTag",ParamType.BOOL)//是否有gps矩形
				.define("gpsRect",ParamType.STRING)//gps矩形   规则   左下点+右上点  例子：  121.495743,31.252139;121.542435,31.217499
				.define("keywords",ParamType.STRING)//搜索关键词
				.define("typeId",ParamType.STRING)//类别id
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),	
		//---------------------------------------------------------------------------------
		//获得外卖餐厅信息，返回TakeoutRestInfoData
		//takeoutRestId改成uuid
		getTakeoutRestInfo("/getTakeoutRestInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//餐厅id
				),
		//---------------------------------------------------------------------------------
		//获得团购信息，返回CouponInfoData
		getCouponInfo("/getCouponInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//团购id
				),		
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//获得团购报名表单信息，返回CouponApplyFormData
		getCouponApplyFormInfo("/getCouponApplyFormInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//团购id
				),		
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//提交报名，用post请求 返回SimpleData
		postCouponApply("/postCouponApply",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//团购id
				.define("shareTo",ParamType.STRING)//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				.define("formData",ParamType.STRING)//表单信息  jsonData  CouponApplyFormData  json
				),	
		//---------------------------------------------------------------------------------
		//获得现金券购买的状态，返回CouponOrderStateData
		getCouponOrderState2("/getCouponOrderState2",
				new ParamProtocol()
				.define("orderId",ParamType.STRING)//订单id
				),
		//---------------------------------------------------------------------------------
		//获得团购购买表单数据，返回CouponOrderFormData
		getCouponOrderFormInfo("/getCouponOrderFormInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//团购id
				),	
		//---------------------------------------------------------------------------------
		//提交团购表单，返回CommonPostPayResultData
		//如果校验是否通过 chkPassTag:false 客户端需要提示errorHint
		//如果通过校验    余额支付直接跳到下一页   不是余额支付显示支付方式列表
		postCouponOrder2("/postCouponOrder2",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//团购id
				.define("payTypeTag",ParamType.LONG)//支付方式  0:直接余额支付  其他：银行卡
				.define("buyNum",ParamType.STRING)//购买数量
				.define("userTel",ParamType.STRING)//手机号码
				.define("usedRemainMoney",ParamType.DOUBLE)//使用的账户余额
				.define("usedPointNum",ParamType.LONG)//使用的积分数量
				.define("actualPay",ParamType.DOUBLE)//实际支付金额  格式  12.12
				.define("cardId",ParamType.STRING)//礼品卡id
				.define("receiverName",ParamType.STRING)//收货人姓名
				.define("receiverTel",ParamType.STRING)//收货人手机号
				.define("receiverAddress",ParamType.STRING)//收货人地址
				.define("receiverMemo",ParamType.STRING)//收货人备注
				.define("forTestTag",ParamType.BOOL)//是否是测试					
				),	
		//---------------------------------------------------------------------------------
		//获得现金券或特惠套餐列表，返回CashCouponList2DTO
		//增加 advList
		getCashCouponList2("/getCashCouponList2",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG)//类别 1:现金券  2：套餐
				.define("distanceMeter",ParamType.LONG)//附近距离   约定：0：不是附近搜索     默认5000米
				.define("regionId",ParamType.STRING)//地域ID     约定 ""：全部地域    其他：所选地域ID
				.define("menuId",ParamType.STRING)//菜系类别ID   约定 "":为全部菜系  其他：所选菜系ID
				.define("sortTypeTag",ParamType.LONG)//排序  0：默认  其他：所选排序
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),	
		//---------------------------------------------------------------------------------
		//获得现金券或特惠套餐信息 ，返回CashCouponData
		//代替 getMealComboInfo2
		getCashCouponInfo("/getCashCouponInfo",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//现金券或套餐id
				.define("restId",ParamType.STRING)//餐厅id,可以为空
				),	
		//---------------------------------------------------------------------------------
		//获得订单支付数据 ，返回CashCouponPayData
		//代替getBillPayData
		//CashCouponPayData比霸王菜多了 cardList    去掉 userTel和userToken
		//界面是否照霸王菜？？？
		getCashCouponPayData("/getCashCouponPayData",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//现金券或套餐id
				),
		//---------------------------------------------------------------------------------
		//提交现金券表单，返回CashCouponPostResultData
		//到选择支付方式页 需要先判断是否可以余额支付 可以的话直接提交  否则显示支付方式列表
		//如果校验是否通过 chkPassTag:false 客户端需要提示errorHint
		//如果通过校验    余额支付直接跳到下一页   不是余额支付显示支付方式列表
		//CashCouponPostResultData  有些改动
		postCashCoupon("/postCashCoupon",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)//现金券或套餐id
				.define("restId",ParamType.STRING)//餐馆id
				.define("payTypeTag",ParamType.LONG)//支付方式  0:直接余额支付  其他：银行卡
				.define("buyNum",ParamType.LONG)//购买数量
				.define("userTel",ParamType.STRING)//手机号码
				.define("usedRemainMoney",ParamType.DOUBLE)//使用的账户余额
				.define("usedPointNum",ParamType.LONG)//使用的积分数量
				.define("actualPay",ParamType.DOUBLE)//实际支付金额  格式  12.12
				.define("cardId",ParamType.STRING)//礼品卡id
				.define("forTestTag",ParamType.BOOL)//是否是测试
				),
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//获得菜单列表 详情页直接进入，返回DishListPackDTO
		//修改了fromTag的约定
		//删除了 DishData中的canModifyNumTag
		getDishList("/getDishList",
				new ParamProtocol()
				.define("fromTag",ParamType.LONG)//来自哪里  1：餐厅页或订单页  2：我的菜单
				.define("uuid",ParamType.STRING)//uuid 1:restId   2:dishOrderId 
				),	
		//---------------------------------------------------------------------------------
		//添加或更新菜单订单，返回SimpleData //uuid 为返回的菜单id
		//修改了postTag的约定
		postDishOrder("/postDishOrder",
				new ParamProtocol()
				.define("postTag",ParamType.LONG)//提交类型   1:添加(餐馆详细页进来)   2:更新  
				.define("uuid",ParamType.STRING)//uuid  postTag=1: restId   2:dishOrderId; 
				.define("dishList",ParamType.STRING)//菜单列表   dishId:份数|dishId:份数 
				),			
		//---------------------------------------------------------------------------------
		//获得餐馆页面数据 ，返回PageRestInfoDTO
		//替换原来的 getPageResInfoData
		//ResInfoData 添加了 favTag     
		//resPicUrl 改成restPicUrl  resPicNum改成  restPicNum
		//lstTelInfo 改为telList
		getPageRestInfoData("/getPageRestInfoData",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//restId
				),
				
		//获得餐馆页面数据
		//PageRestInfoDTO 增加recomInfo(typeTag=2时有数据)
		//recomInfo 增加recomHintTitle commentPlaceHolder
		//RestInfoData增加  bookingBtnName  detail  totalRecomNum  recomData searchMenuTypeList topRestReason restPicUrlForTopRest
		getPageRestInfoData2("/getPageRestInfoData2",
				new ParamProtocol()
		        .define("typeTag",ParamType.LONG)//1:餐厅  2：推荐  3：榜单
		        .define("uuid",ParamType.STRING)//typeTag=1,3为restId   typeTag=2为recomId
				.define("topRestTypeId",ParamType.STRING)//typeTag=3为榜单id
				),
				
		//获得餐馆页面数据  返回PageRestInfo3DTO
		//RestRecomInfoData3改动较大
		//RestRecomPicData增加restId
		//showTypeTag
		getPageRestInfoData3("/getPageRestInfoData3",
				new ParamProtocol()
		        .define("typeTag",ParamType.LONG)//1:餐厅  2：推荐  3：榜单
		        .define("uuid",ParamType.STRING)//typeTag=1,3为restId   typeTag=2为recomId
				.define("topRestTypeId",ParamType.STRING)//typeTag=3为榜单id
				),	
				
				
		//---------------------------------------------------------------------------------
		//把餐厅加入收藏，返回SimpleData.msg ：返回提示  比如：收藏成功
		addRestToFav("/addRestToFav",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//restId
				),
		//---------------------------------------------------------------------------------
		//把餐厅取消收藏，返回SimpleData.msg ：返回提示  比如：收藏已移除
		delRestFromFav("/delRestFromFav",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//restId
				),		
		//---------------------------------------------------------------------------------
		//获得图片列表，返回RestPicListDTO
		//替换原来的getResPicList
		//去掉了 参数  foodTypeId //菜品类别 约定  空为全部   否则是指定类别 
		//ResPicListDTO  去掉了 foodTypeList
		getRestPicList("/getRestPicList",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//餐馆ID
				.define("picViewTag",ParamType.LONG)//查看类别  约定 0:全部  1：环境  2：菜式  3：会员上传  4:其他
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),	
		//---------------------------------------------------------------------------------
		//获得菜品图片信息 ，返回RestFoodPicDataDTO
		//替换原来的getResFoodPicData
		getRestFoodPicData("/getRestFoodPicData",
				new ParamProtocol()
				.define("foodId",ParamType.STRING)//菜品ID
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),
		//---------------------------------------------------------------------------------
		//添加菜品评论 ，返回SimpleData
		//替换原来的addResFoodComment
		addRestFoodComment("/addRestFoodComment",
				new ParamProtocol()
				.define("foodId",ParamType.STRING)//菜品ID
				.define("likeTypeTag",ParamType.LONG)//喜欢类型  0:忽略喜欢类型  1:好吃  2:一般 3:不好吃
				.define("content",ParamType.STRING)//评论内容 
				.define("shareTo",ParamType.STRING)//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				),
		//---------------------------------------------------------------------------------
		//添加菜品喜欢类型 ，返回SimpleData
		//替换原来的 addResFoodLikeType
		addRestFoodLikeType("/addRestFoodLikeType",
				new ParamProtocol()
				.define("foodId",ParamType.STRING)//菜品ID
				.define("token",ParamType.STRING)//用户id
				.define("likeTypeTag",ParamType.LONG)//喜欢类型   1:好吃  2:一般 3:不好吃
				),
		//---------------------------------------------------------------------------------
		//获得评论列表，返回CommentListDTO
		//替换原来的 getResCommentList
		getRestCommentList("/getRestCommentList",
				new ParamProtocol()
				.define("restId",ParamType.STRING)//餐馆ID
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),
		//---------------------------------------------------------------------------------
		//添加评论，返回SimpleData
		postComment("/upload/postComment",
				new ParamProtocol(true)
				.define("restId",ParamType.STRING)//餐馆ID
				.define("postTag",ParamType.LONG)//1：从餐馆评论页进去    2：从订单页进去   3：从随手拍进去 菜品     4：从随手拍进去 餐馆
				.define("foodId",ParamType.STRING)//菜品id  如果 postTag:3 foodId不能为空     postTag:其他 为空
				.define("orderId",ParamType.STRING)//订单id  如果 postTag:2 orderId不能为空     postTag:其他 为空
				.define("imgSizeList",ParamType.STRING)//图片大小   可以为空   多图为   13242314;29282;29282  
				.define("picId",ParamType.STRING)//图片id，只有在postTag==3的时候需要传图片id
				.define("overallNum",ParamType.LONG)//总体评价      0为未选择  
				.define("tasteNum",ParamType.LONG)//口味      0为未选择
				.define("envNum",ParamType.LONG)//环境       0为未选择
				.define("serviceNum",ParamType.LONG)//服务      0为未选择
				.define("detail",ParamType.STRING)//评论内容        postTag=3时传递图片描述  可以为空    postTag=其他 不能为空
				.define("shareTo",ParamType.STRING)//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				),
		//---------------------------------------------------------------------------------
		//添加评论评分 ，返回SimpleData
		postCommentScore("/postCommentScore",
				new ParamProtocol()
				.define("commentId",ParamType.STRING)//评论id
				.define("likeTag",ParamType.BOOL)//是否喜欢
				.define("tasteNum",ParamType.LONG)//口味      0为未选择
				.define("envNum",ParamType.LONG)//环境       0为未选择
				.define("serviceNum",ParamType.LONG)//服务      0为未选择
				),
		//---------------------------------------------------------------------------------
		//获得评论回复列表，返回CommentReplyListDTO
		//替换原来的 getResCommentReplyList
		getRestCommentReplyList("/getRestCommentReplyList",
				new ParamProtocol()
				.define("commentId",ParamType.STRING)//评论id
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),
		//---------------------------------------------------------------------------------
		//提交评论回复，返回SimpleData
		//参数 replyId  替换 原来的responseId  
		postCommentReply("/postCommentReply",
				new ParamProtocol()
				.define("postTag",ParamType.LONG)//提交类型   1:添加 2:更新
				.define("commentId",ParamType.STRING)//评论id
				.define("replyId",ParamType.STRING)//回复的id postTag:1时为空  postTag:2时为空 对应的回复id
				.define("detail",ParamType.STRING)//内容  1~1000字
				.define("shareTo",ParamType.STRING)//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				),	
		//---------------------------------------------------------------------------------
		//上传图片，返回SimpleData//picUrl:返回图片url  在typeTag:3时会用到
		//去掉了参数token
		uploadImage("/upload/uploadImage",
				new ParamProtocol(true)
				.define("typeTag",ParamType.LONG)//类别  1:餐馆  2:菜品  3：餐馆评论页
				.define("restId",ParamType.STRING)//餐馆id
				.define("foodId",ParamType.STRING)//菜品id  type=2 是菜品id 其他为空
				.define("memo",ParamType.STRING)//备注  可以为空  typeTag=1时 传递
				.define("likeTypeTag",ParamType.LONG)//喜欢类型   0:没有选择任何喜欢类型  1:好吃  2:一般 3:不好吃		当	typeTag：  2时候有效
				.define("shareTo",ParamType.STRING)//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				),	
		//---------------------------------------------------------------------------------
		//获得订单列表 ，返回OrderListDTO
		//去掉typeTag 
		//OrderListData 改成  OrderHintData
		getOrderList("/getOrderList",
				new ParamProtocol()
				.define("statusId",ParamType.STRING)//状态id  默认为0或者空    0或者空为全部状态   其他为特定状态id  
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),
		//---------------------------------------------------------------------------------
		//获得订单列表 ，返回OrderList2DTO  4.1.7版本以后
		getOrderList2("/getOrderList2",
				new ParamProtocol()
				.define("statusId",ParamType.STRING)//状态id  默认为0或者空    0或者空为全部状态   其他为特定状态id  
				.define("pageSize",ParamType.LONG)//页面大小
				.define("startIndex",ParamType.LONG)//当前页
				),
		//---------------------------------------------------------------------------------
		//清空订单 ，返回SimpleData  4.1.7版本以后
		operateOrder("/operateOrder",
				new ParamProtocol()
				.define("operateId",ParamType.STRING)//操作id
				),		
				
		//---------------------------------------------------------------------------------
		//获得订单表单信息  返回OrderFormData
		getOrderFormInfo("/getOrderFormInfo",
				new ParamProtocol()
				.define("restId",ParamType.STRING) 
				),				
		//下单，返回SimpleData，uuid:订单Id	//needUpdateUserInfoTag=true  //userInfo=用户信息
		//参数改动大
		postOrder("/postOrder",
				new ParamProtocol()
				.define("postTag",ParamType.LONG)//提交类型   1:添加   2:更新
				.define("orderId",ParamType.STRING)//订单id  如果postTag=1 为空   postTag=2为修改的订单的id 
				.define("restId",ParamType.STRING)//餐馆ID  
				.define("reserveTime",ParamType.LONG)//预订时间 
				.define("peopleNum",ParamType.LONG)//人数 
				.define("roomTypeTag",ParamType.LONG)//房间要求     0:只订大厅  1:只订包房  2:优先订大厅  3:优先订包房
				.define("bookerName",ParamType.STRING)//姓名
				.define("bookerSexTag",ParamType.LONG)//性别    约定  1：先生  0：女士 
				.define("bookerTel",ParamType.STRING)//手机号 
				.define("memo",ParamType.STRING)//备注
				.define("forOtherTag",ParamType.BOOL)//是否为他人订餐
				.define("eaterName",ParamType.STRING)//姓名
				.define("eaterSexTag",ParamType.LONG)//性别    约定  1：先生  0：女士 
				.define("eaterTel",ParamType.STRING)//手机号
				),
		//---------------------------------------------------------------------------------
		//下单，返回SimpleData，uuid:订单Id	//needUpdateUserInfoTag=true  //userInfo=用户信息
		//增加了activityId
		postOrder2("/postOrder2",
				new ParamProtocol()
				.define("postTag",ParamType.LONG)//提交类型   1:添加   2:更新
				.define("orderId",ParamType.STRING)//订单id  如果postTag=1 为空   postTag=2为修改的订单的id 
				.define("restId",ParamType.STRING)//餐馆ID  
				.define("reserveTime",ParamType.LONG)//预订时间 
				.define("peopleNum",ParamType.LONG)//人数 
				.define("roomTypeTag",ParamType.LONG)//房间要求     0:只订大厅  1:只订包房  2:优先订大厅  3:优先订包房
				.define("bookerName",ParamType.STRING)//姓名
				.define("bookerSexTag",ParamType.LONG)//性别    约定  1：先生  0：女士 
				.define("bookerTel",ParamType.STRING)//手机号 
				.define("memo",ParamType.STRING)//备注
				.define("forOtherTag",ParamType.BOOL)//是否为他人订餐
				.define("eaterName",ParamType.STRING)//姓名
				.define("eaterSexTag",ParamType.LONG)//性别    约定  1：先生  0：女士 
				.define("eaterTel",ParamType.STRING)//手机号
				.define("activityId",ParamType.STRING)//活动id 默认为空	
				),
		//---------------------------------------------------------------------------------
		//获得订单状态，返回OrderStateInfoData
		getOrderStateInfo("/getOrderStateInfo",
				new ParamProtocol()
				.define("orderId",ParamType.STRING)//订单id，
				.define("forAddTag",ParamType.BOOL)//是否是添加
				),
		//---------------------------------------------------------------------------------
		//订单状态页选择了分享得秘币按钮
		postSelectOrderStateShareBtn("/postSelectOrderStateShareBtn",
				new ParamProtocol()
		        .define("orderId",ParamType.STRING)//订单id，
				),						
		//---------------------------------------------------------------------------------
		//获得订单详情，返回OrderInfoData
		getOrderInfo("/getOrderInfo",
				new ParamProtocol()
				.define("queryTypeTag",ParamType.LONG)//查询类别 1:默认  2：需要返回上一个下一个活跃订单
				.define("orderId",ParamType.STRING)//订单id，当queryTypeTag=2 时可以为空，服务端决定是哪个订单；queryTypeTag=1时不为空，获取orderId指定的订单
				),
				
	    //---------------------------------------------------------------------------------
	    //获得订单详情，返回OrderInfoData2  4.1.7版本以后
		getOrderInfo2("/getOrderInfo2",
	    		new ParamProtocol()
	    		.define("queryTypeTag",ParamType.LONG)//查询类别 1:默认  2：需要返回上一个下一个活跃订单
	    		.define("orderId",ParamType.STRING)//订单id，当queryTypeTag=2 时可以为空，服务端决定是哪个订单；queryTypeTag=1时不为空，获取orderId指定的订单
	    		),
		//---------------------------------------------------------------------------------
		//取消订单, 返回 SimpleData.msg是提示信息
		cancelOrder("/cancelOrder",
				new ParamProtocol()
				.define("orderId",ParamType.STRING)//订单id 
				),
		//---------------------------------------------------------------------------------
		//取消订单, 返回 SimpleData.msg是提示信息
		cancelOrder2("/cancelOrder2",
				new ParamProtocol()
				.define("orderId",ParamType.STRING)//订单id 
				.define("reasonTypeTag",ParamType.LONG)//原因类别  0:未选择  1、计划有变  2、更换餐厅 
				.define("reasonMemo",ParamType.STRING)//原因备注
				),
		//---------------------------------------------------------------------------------
		//获得用户中心信息，返回UserCenterInfoDTO   并且JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		//UserInfoDTO 中增加 remainMoney nextLevelPct nextLevelHint
		getUserCenterInfo2("/getUserCenterInfo2",
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//上传用户头像，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		uploadUserPic("/upload/uploadUserPic",
				new ParamProtocol(true) //需要inputStream数据
				),
		//---------------------------------------------------------------------------------
		//发送验证码到手机上，返回SimpleData.msg 字段  设置返回提示   比如  ：验证码已发送到你手机里面
		//按钮disable、发送后开始倒计时60秒,倒计时结束按钮enable
		//成功以后 
		//如果typeTag=2并且服务器返回的是succTag=false 客户端显示确认提示 msg，客户确认后走 typeTag=3的逻辑
		sendVerifyCodeToTel("/sendVerifyCodeToTel",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG) //1:登录时  2：修改手机号时  3：确认要合并
				.define("tel",ParamType.STRING)//手机号
				),
		//---------------------------------------------------------------------------------
		//手机验证，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		verifyTelCode("/verifyTelCode",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG) //1:登录时  2：修改手机号时  3：确认要合并
				.define("tel",ParamType.STRING)//手机号
				.define("code",ParamType.STRING)//验证码
				),
				
		//---------------------------------------------------------------------------------
		//发送验证码（邀请）
		sendVerifyCodeToTelForInvite("/sendVerifyCodeToTelForInvite",
				new ParamProtocol()
				.define("tel",ParamType.STRING)//手机号
				.define("inviteCode",ParamType.STRING)//邀请码
				),
		//---------------------------------------------------------------------------------
		//手机验证（邀请）返回 JsonPack中//needUpdateUserInfoTag=true//userInfo=用户信息
		verifyTelCodeForInvite("/verifyTelCodeForInvite",
				new ParamProtocol()
				.define("tel",ParamType.STRING)//手机号
				.define("inviteCode",ParamType.STRING)//邀请码
				.define("verifyCode",ParamType.STRING)//验证码
				),
		//---------------------------------------------------------------------------------
		//退出
		logout("/logout",
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//修改昵称，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		changeUserNickName("/changeUserNickName",
				new ParamProtocol()
				.define("nickName",ParamType.STRING)//昵称
				),		
		//---------------------------------------------------------------------------------
		//修改性别，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		changeUserSex("/changeUserSex",
				new ParamProtocol()
				.define("sexTag",ParamType.LONG)//1：先生  0：女士  
				),	
		//---------------------------------------------------------------------------------
		//绑定到微博，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		bindToWeibo("/bindToWeibo",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG)//1:新浪  2：qq
				.define("code",ParamType.STRING)//拦截到的字符串
				),	
		//---------------------------------------------------------------------------------
		//绑定到新浪微博 直接绑定，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		bindToSinaWeiboByAccessToken("/bindToSinaWeiboByAccessToken",
				new ParamProtocol()
				.define("uuid",ParamType.STRING)
				.define("accessToken",ParamType.STRING)
				.define("remainSecs",ParamType.LONG)
				),
		//---------------------------------------------------------------------------------
		//解除绑定到微博，返回 JsonPack中//needUpdateUserInfoTag=true  //userInfo=用户信息
		unbindWeibo("/unbindWeibo",
				new ParamProtocol()
				.define("typeTag",ParamType.LONG) //1:新浪  2：qq
				),
		//---------------------------------------------------------------------------------
		//获得用户的菜单列表，返回UserDishOrderListDTO
		getUserDishOrderList("/getUserDishOrderList",
				new ParamProtocol()
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //开始索引
				),
				
		//---------------------------------------------------------------------------------
		//获得短信邀请信息，返回 InviteSmsInfoDTO，还有要带短连接 （点击打开地图）
		getInviteSmsInfo("/getInviteSmsInfo",
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id
				.define("restId", ParamType.STRING) //餐馆id
				),
		//---------------------------------------------------------------------------------
		//上传小票，返回SimpleData.msg是提示信息
		uploadOrderReceipt("/upload/uploadOrderReceipt",
				new ParamProtocol(true)
				.define("orderId", ParamType.STRING) //订单id
				),
		//---------------------------------------------------------------------------------
		//提交就餐金额，返回SimpleData.msg是提示信息
		postOrderPrice("/postOrderPrice",
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id
				.define("price", ParamType.STRING) //金额
				),

		//---------------------------------------------------------------------------------
		//添加菜品数据，返回SimpleData.msg是提示信息
		addOrUpdateFood("/addOrUpdateFood",
				new ParamProtocol()
				.define("restId", ParamType.STRING) //餐馆id
				.define("foodId", ParamType.STRING) //菜品id  foodId为空时为update 不为空时为add
				.define("foodName", ParamType.STRING) //菜品名称  可以为空
				.define("foodPrice", ParamType.STRING) //菜品价格   可以为空
				.define("foodUnit", ParamType.STRING) //菜品单位  可以为空
				.define("memo", ParamType.STRING) //备注  可以为空
				.define("token", ParamType.STRING) //用户token  可以为空
				.define("likeTypeTag", ParamType.LONG) //喜欢类型  0:没有选择任何喜欢类型  1:好吃  2:一般 3:不好吃
				),
				
				
//		// ---------------------------------------------------------------------------------
//		// 分享餐馆信息到新浪微博等 SimpleData
//		shareTo("/shareTo", 
//				new ParamProtocol()
//				.define("typeTag", ParamType.LONG) // 1:分享餐厅  2：软件分享    3：推荐分享  4：外卖餐厅分享  5:wap分享  6：订单分享
//				.define("token", ParamType.STRING) // 用户token 可以为空
//				.define("uuid", ParamType.STRING) // uuid typeTag：1时为餐馆id  typeTag:3时是推荐id
//				.define("detail", ParamType.STRING) // 内容
//				.define("shareTo", ParamType.STRING) // 分享到微博 sina:1;qq:0
//														// 当前只有sina:1 或者 sina:0
//														// 如果客户端这个字段传递为空
//														// ，容错处理为不分享到任何平台
//		),
		//---------------------------------------------------------------------------------
		// 获得上传（菜品，环境图片上传）前需要的数据，返回UploadData
		getUploadData("/getUploadData", 
						new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//获得整理过的菜品列表，返回SortedFoodListDTO
		getSortedRestFoodList("/getSortedRestFoodList", 
						new ParamProtocol()
						.define("restId", ParamType.STRING) //餐馆ID
				),
		
		//---------------------------------------------------------------------------------
		//添加错误报告，返回200表示成功
		addErrorReport("/addErrorReport", 
					new ParamProtocol()
					.define("typeTag", ParamType.LONG) //大类标志    1：餐馆  2：菜系  3：外卖
					.define("typeId", ParamType.STRING) //错误类别ID 
					.define("uuid", ParamType.STRING) //数据ID 
					.define("errorContent", ParamType.STRING) //错误内容    可以为空
					.define("email", ParamType.STRING) //email    //可以为空
					.define("token", ParamType.STRING) //可以为空
				),
		//---------------------------------------------------------------------------------
		//通知拨打电话
		callTel("/callTel", 
			new ParamProtocol()
			.define("posTag", ParamType.LONG) //位置    1:首页 2:餐馆  3:外卖  4:现金券
			.define("uuid", ParamType.STRING) //uuid  posTag=1,2 时为餐馆id   3:外卖餐厅id   4:现金券id
			.define("tel", ParamType.STRING) //电话
		),
		//---------------------------------------------------------------------------------
		//提交订单功能按钮点击事件，返回jsonPack.code=200表示成功
		postOrderFuncBtnClick("/postOrderFuncBtnClick", 
			new ParamProtocol()
			.define("orderId", ParamType.STRING) //订单id
			.define("typeTag", ParamType.LONG) //按钮类别
		),
		//---------------------------------------------------------------------------------
		/*--------------------------------------------------------------------------
		| 霸王菜首页
		--------------------------------------------------------------------------*/
		//霸王菜首页，返回BwcShakeSelDTO
		getBwcMainPageInfo("/getBwcMainPageInfo", 
				new ParamProtocol()
				.define("selTimestamp", ParamType.LONG) //时间戳  选择信息  默认为 空或者0  需要按城市缓存
			),
		//---------------------------------------------------------------------------------
		//获得随机霸王菜，返回ShakeBwcData
		getShakeBwcInfo("/getShakeBwcInfo", 
				new ParamProtocol()
				.define("leftId", ParamType.STRING) //左侧Id 没锁定为空
				.define("rightId", ParamType.STRING) //右侧Id 没锁定为空
			),
		//---------------------------------------------------------------------------------
		//我的霸王菜列表，返回BwcListDTO
		getUserBwcList("/getUserBwcList", 
				new ParamProtocol()
				.define("sortTypeTag", ParamType.LONG) //排序 1:按时间  2：按距离
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
			),
		
		//---------------------------------------------------------------------------------
		//霸王菜详情，返回BwcDetailData
		getBwcDetailInfo("/getBwcDetailInfo", 
				new ParamProtocol()
				.define("bwcId", ParamType.STRING) //霸王菜id
			),
		
		//---------------------------------------------------------------------------------
		//把霸王菜加入收藏，没有返回DTO，200成功
		addBwcToFav("/addBwcToFav", 
				new ParamProtocol()
				.define("bwcId", ParamType.STRING) //霸王菜id
			),
		//---------------------------------------------------------------------------------
		//把霸王菜删除收藏，没有返回DTO，200成功
		delBwcFromFav("/delBwcFromFav", 
				new ParamProtocol()
				.define("bwcId", ParamType.STRING) //霸王菜id
			),
		
		//---------------------------------------------------------------------------------
		//霸王菜评论列表，返回BwcCommentListDTO
		getBwcCommentList("/getBwcCommentList", 
				new ParamProtocol()
				.define("bwcId", ParamType.STRING) //霸王菜id
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
			),	
		//---------------------------------------------------------------------------------
		//添加霸王菜评论，返回SimpleData.msg返回成功提示
		postBwcComment("/upload/postBwcComment", 
				new ParamProtocol(true)
				.define("bwcId", ParamType.STRING) //霸王菜id
				.define("imgSizeList", ParamType.STRING) //图片大小   可以为空   多图为   13242314;29282;29282 
				.define("detail", ParamType.STRING) //评论内容 
				.define("shareTo", ParamType.STRING) //分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
			),	
		//---------------------------------------------------------------------------------
		//获得快捷预订页面信息，返回SuggestResultData
		//增加 roomTypeInfoData
		getQuickOrderInfo("/getQuickOrderInfo", 
				new ParamProtocol()
			),	
		
		//榜单列表接口
		getTopRestList("/getTopRestList",
				new ParamProtocol()
				.define("typeId", ParamType.STRING) //榜单子类Id
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页 
				),
		
		//---------------------------------------------------------------------------------
			
		//获得榜单类别信息，返回RfTypeListPackDTO
		//RfTypeListDTO  中  u  n:   榜单父类的uuid,name    list :子类列表
		//RfTypeDTO  中  u  n : 商区的uuid,name
		getTopRestListTypeInfo("/getTopRestListTypeInfo", 
				new ParamProtocol()
			),
		
			
			
			


		//---------------------------------------------------------------------------------
		//提交支付宝钱包，目前会和设备绑定，将来可能会返回JsonPack.userInfo更新用户信息
		//app_id  String  是      AOP分配给应用的唯一标识 
		//version  String  是  1.0    API 协议版本，默认值：1.0 
		//alipay_client_version  String  是      支付宝客户端版本 
		//alipay_user_id  String  是      支付宝UserId 
		//auth_code  String  是  授权码 
		postAliPayCode("/postAliPayCode", 
				new ParamProtocol()
				.define("appId", ParamType.STRING) //AOP分配给应用的唯一标识
				.define("version", ParamType.STRING) //API 协议版本
				.define("alipayClientVersion", ParamType.STRING) //支付宝客户端版本 
				.define("alipayUserId", ParamType.STRING) //支付宝UserId
				.define("authCode", ParamType.STRING) //授权码
			),	
			
		/************************************新接口***********************************/
			
		//获得餐厅推荐列表（在首页上滑加载的时候用）restId=空 返回RestRecomListDTO
		//增加吃货荐店页面  topTag=false restId=餐厅id 返回RestRecomListDTO
		getRestRecomList("/getRestRecomList", 
					new ParamProtocol()
					.define("topTag", ParamType.BOOL) //是否是精选
					.define("pageSize", ParamType.LONG) //页面大小
					.define("startIndex", ParamType.LONG) //当前页 
			),	
			//获得餐厅推荐列表。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。增加restId
			//增加吃货荐店页面  topTag=false restId=餐厅id 返回RestRecomListDTO
			getRestRecomList2("/getRestRecomList2", 
					new ParamProtocol()
					.define("topTag", ParamType.BOOL) //是否是精选
					.define("restId", ParamType.STRING)//餐厅id 可以为空
					.define("pageSize", ParamType.LONG) //页面大小
					.define("startIndex", ParamType.LONG) //当前页 
			),	
			
		//---------------------------------------------------------------------------------
		//获得餐厅推荐信息，返回RestRecomInfoData3
		getRestRecomInfo("/getRestRecomInfo", 
					new ParamProtocol()
					.define("uuid", ParamType.STRING) //id
			),	
		
		//---------------------------------------------------------------------------------	
		//加入收藏，返回SimpleData.msg是提示信息
		addRestRecomToFav("/addRestRecomToFav", 
					new ParamProtocol()
					.define("uuid", ParamType.STRING) //uuid
			),
		//---------------------------------------------------------------------------------
		//删除收藏，返回SimpleData.msg是提示信息
		delRestRecomFromFav("/delRestRecomFromFav", 
						new ParamProtocol()
						.define("uuid", ParamType.STRING) //uuid
			),
		//---------------------------------------------------------------------------------
		//加入喜欢
		addRestRecomToLike("/addRestRecomToLike", 
					new ParamProtocol()
					.define("uuid", ParamType.STRING) //uuid
				),
				
				//---------------------------------------------------------------------------------
		//删除喜欢
		delRestRecomFromLike("/delRestRecomFromLike", 
					new ParamProtocol()
					.define("uuid", ParamType.STRING) //uuid
				),
		//---------------------------------------------------------------------------------
		//获得餐厅推荐评论列表，返回RestRecomCommentListDTO
		getRestRecomCommentList("/getRestRecomCommentList", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //餐厅uuid
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页 
				),
		//---------------------------------------------------------------------------------
		//添加“餐厅推荐”评论，返回200就表示成功
		addRestRecomComment("/addRestRecomComment", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //推荐id
				.define("detail", ParamType.STRING) //评论内容
				),
		//---------------------------------------------------------------------------------
		////获得餐厅推荐表单信息 返回 RestRecomFormData
		getRestRecomFormInfo("/getRestRecomFormInfo", 
				new ParamProtocol()
				),
				
		//---------------------------------------------------------------------------------
		//添加餐厅推荐，返回SimpleData.msg是提示信息
		addRestRecom("/upload/addRestRecom", 
				new ParamProtocol(true)
				.define("title", ParamType.STRING) //标题
				.define("restId", ParamType.STRING) //餐厅id
				.define("coverIdx", ParamType.LONG) //封面图片索引，如果用户未选择，取-1， 否则从0开始
				.define("imgSizeList", ParamType.STRING) //图片大小   可以为空   多图为   13242314;29282;29282(注意：第一个数据块存放的是所有的图片描述，用|分隔。字符串使用utf-8字节流)
				),
//		//---------------------------------------------------------------------------------
//		//获得首页的信息，返回MainPageInfoPackDTO
//		getMainPageInfoPack("/getMainPageInfoPack3", 
//				new ParamProtocol()
//				.define("advTimestamp", ParamType.LONG) //时间戳   广告位   默认为 空或者0
//				.define("orderHintTimestamp", ParamType.LONG) //时间戳   订单提示   默认为 空或者0
//				.define("pageSize", ParamType.LONG) //页面大小
//				),
		//获得首页的信息，返回MainPageInfoPack4DTO 4.1.7以后版本
		//因为服务端返回OrderHintPackData速度慢，所以拆分
		//OrderHintPackData中不返回数据,客户端也不要处理这个数据
		getMainPageInfoPack4("/getMainPageInfoPack4", 
						new ParamProtocol()
						.define("advTimestamp", ParamType.LONG) //时间戳   广告位   默认为 空或者0
						.define("pageSize", ParamType.LONG) //页面大小
						.define("firstQueryTag", ParamType.BOOL) //是否是软件第一次打开的请求
		),
		//---------------------------------------------------------------------------------
		//获得订单提示信息，返回MainPageOtherInfoPackDTO
		getMainPageOtherInfoPack("/getMainPageOtherInfoPack", 
				new ParamProtocol()
				.define("orderHintTimestamp", ParamType.LONG) //时间戳   订单提示   默认为 空或者0
				),
		//---------------------------------------------------------------------------------
		//获得首页餐馆列表，返回RestListDTO
		getMainRestList("/getMainRestList", 
				new ParamProtocol()
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
				),
		
		//---------------------------------------------------------------------------------
		//获得热门餐厅类别列表（包括热门商区、热门菜系、地铁站）返回HotRestTypeListDTO
		getHotRestTypeList("/getHotRestTypeList", 
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//获得主菜系子菜系的数据，用于选择列表，返回RfTypeListPackDTO
		//RfTypeListDTO  中  u  n:   主菜系的uuid,name    list :主菜系里包含的子菜系列表
		//RfTypeDTO  中  u  n c : 子菜系的uuid,name,首字母
		getMainMenuSubMenuList("/getMainMenuSubMenuList", 
				new ParamProtocol()
				),
		
		//---------------------------------------------------------------------------------
		//获得地铁线路，站台的数据，用于选择列表，返回RfTypeListPackDTO
		//RfTypeListDTO  中  u  n:   线路的uuid,name    list :线路里包含的站台列表
		//RfTypeDTO  中  u  n c : 站台的uuid,name,首字母
		getSubwayLineStationList("/getSubwayLineStationList", 
				new ParamProtocol()
				),				
		
		/**********************************新版外卖接口*************************************/
				
		//---------------------------------------------------------------------------------
		//搜索外卖餐厅列表，返回TakeoutListDTO
		getTakeoutList("/getTakeoutList", 
				new ParamProtocol()
				.define("gpsTypeTag", ParamType.LONG) //经纬度类别  1:原生 2：百度   3：中国（google）
				.define("longitude", ParamType.DOUBLE) //经度  如果为0的话是全城搜索
				.define("latitude", ParamType.DOUBLE) //纬度  如果为0的话是全城搜索
				.define("keywords", ParamType.STRING) //搜索关键词   如果不为空  忽略longitude，latitude 
				.define("typeId", ParamType.STRING) //外卖餐厅类别id  默认为空
				.define("sendLimitId", ParamType.STRING) //起送类别id 默认为空
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
				),
		//---------------------------------------------------------------------------------
		//获得外卖餐厅列表 地图，返回TakeoutListDTO
		//注意返回值中的：转换后的中国经纬度  chineseLon chineseLat  
		getTakeoutListForMap("/getTakeoutListForMap", 
				new ParamProtocol()
				.define("gpsTypeTag", ParamType.LONG) //经纬度类别  1:原生 2：百度   3：中国（google）
				.define("longitude", ParamType.DOUBLE) //经度 
				.define("latitude", ParamType.DOUBLE) //纬度  
				.define("gpsRect", ParamType.STRING) //gps矩形   规则   左下点+右上点  例子：  121.495743,31.252139;121.542435,31.217499
				.define("typeId", ParamType.STRING) //外卖餐厅类别id  默认为空
				.define("sendLimitId", ParamType.STRING) //起送类别id 默认为空
				),

		//---------------------------------------------------------------------------------	
		//获得外卖餐厅菜单 TakeoutMenuListPackDTO
		getTakeoutMenuList("/getTakeoutMenuList", 
				new ParamProtocol()
				.define("fromTag", ParamType.LONG) //来自哪里  1：点菜页  2：订单页
				.define("uuid", ParamType.STRING) //uuid 1:takeoutId   2:orderId 
				),
				
		//---------------------------------------------------------------------------------			
		//外卖菜品详情 ，返回TakeoutMenuData
		//TakeoutMenuGradeData 增加  numPerct
		getTakeoutMenuInfo("/getTakeoutMenuInfo", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //uuid						),
				),				
				
		//---------------------------------------------------------------------------------			
		//获得外卖餐厅信息，返回TakeoutInfoData
		getTakeoutInfo("/getTakeoutInfo", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) // 外卖餐厅id
				),
		//---------------------------------------------------------------------------------
		//把外卖餐厅加入收藏，返回200成功
		addTakeoutToFav("/addTakeoutToFav", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) // 外卖餐厅id
				),
				
		//---------------------------------------------------------------------------------
		//把外卖餐厅加入收藏，返回200成功
		delTakeoutFromFav("/delTakeoutFromFav", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) // 外卖餐厅id
				),
		//---------------------------------------------------------------------------------		
		//获得外卖评论列表，返回TakeoutCommentListDTO
		getTakeoutCommentList("/getTakeoutCommentList", 
				new ParamProtocol()
				.define("fromTag", ParamType.LONG) //来自哪里  1：详情页  2：订单页
				.define("uuid", ParamType.STRING) //uuid 1:takeoutId（即外卖餐厅id）   2:orderId 
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
				),
		//---------------------------------------------------------------------------------		
		//获得用户外卖收货地址列表，返回UserTkRaListPackDTO 
		getUserTakeoutReceiveAddressList("/getUserTakeoutReceiveAddressList", 
				new ParamProtocol()
				),
		//---------------------------------------------------------------------------------
		//添加收货地址，返回200成功
		addUserTakeoutReceiveAddress("/addUserTakeoutReceiveAddress", 
				new ParamProtocol()
				.define("address", ParamType.STRING) //地址
				.define("tel", ParamType.STRING) //电话
				),
		//---------------------------------------------------------------------------------
		//修改收货地址 ，返回200成功
		editUserTakeoutReceiveAddress("/editUserTakeoutReceiveAddress", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //uuid
				.define("address", ParamType.STRING) //地址
				.define("tel", ParamType.STRING) //电话
				),

		//---------------------------------------------------------------------------------
		//删除收货地址，返回200成功
		delUserTakeoutReceiveAddress("/delUserTakeoutReceiveAddress", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //uuid
				),
		//---------------------------------------------------------------------------------
		//提交外卖订单，返回SimpleData.uuid 为返回的订单id
		postTakeoutOrder("/postTakeoutOrder", 
				new ParamProtocol()
				.define("takeoutId", ParamType.STRING) //外卖餐厅id
				.define("receiveAdressId", ParamType.STRING) //用户收货地址id
				.define("sendTimeId", ParamType.STRING) //用户期望的送餐时间的id
				.define("menuList", ParamType.STRING) //菜单列表   menuId:份数|menuId:份数
				.define("memo", ParamType.STRING) //备注
				),		

//		//---------------------------------------------------------------------------------	
//		//获得外卖订单列表，返回TakeoutOrderListDTO
//		getTakeoutOrderList("/getTakeoutOrderList", 
//				new ParamProtocol()
//				.define("pageSize", ParamType.LONG) //页面大小
//				.define("startIndex", ParamType.LONG) //当前页
//				),
		//---------------------------------------------------------------------------------	
		//获得外卖订单列表，返回TakeoutOrderList2DTO
		getTakeoutOrderList2("/getTakeoutOrderList2", 
				new ParamProtocol()
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
				),	
		
		//---------------------------------------------------------------------------------	
		//获得外卖订单详情，返回TakeoutOrderInfoData
		getTakeoutOrderInfo("/getTakeoutOrderInfo", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id 
				),

		//---------------------------------------------------------------------------------
		//催促订单，返回SimpleData.msg是成功提示
		urgeTakeoutOrder("/urgeTakeoutOrder", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id 
		),
		//---------------------------------------------------------------------------------
		//确认订单，返回SimpleData.msg是成功提示
		confirmTakeoutOrder("/confirmTakeoutOrder", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id 
				),
		//---------------------------------------------------------------------------------
		//获得外卖订单菜单列表，返回TakeoutMenuListDTO
		getTakeoutOrderMenuList("/getTakeoutOrderMenuList", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id 
				),
		//---------------------------------------------------------------------------------
		//提交订单评论 ，返回SimpleData.msg是成功提示
		postTakeoutOrderComment("/postTakeoutOrderComment", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //id 
				.define("detail", ParamType.STRING) //评论内容  不能为空 
				.define("gradeList", ParamType.STRING) //评分列表   menuId:评分|menuId:评分   可以为空
				.define("shareTo", ParamType.STRING) //分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
				),

		
		//---------------------------------------------------------------------------------
		//获得餐厅标签列表   返回CommonTypeListDTO ：uuid  name num selectTag
		getRestLabelList("/getRestLabelList", 
				new ParamProtocol()
				.define("forPostTag", ParamType.BOOL) //是否用于添加修改标签
				.define("restId", ParamType.STRING) //restId
				),
		//---------------------------------------------------------------------------------
		//添加餐馆标签，返回SimpleData.uuid是新标签uuid
		addRestLabel("/addRestLabel", 
				new ParamProtocol()
				.define("labelName", ParamType.STRING) //标签文字
				.define("restId", ParamType.STRING) //restId
				),
		//提交所选的餐馆标签返回SimpleData.msg是成功提示
		postSelectRestLabel("/postSelectRestLabel", 
				new ParamProtocol()
				.define("labelIdList", ParamType.STRING) // 111;222;333
				.define("restId", ParamType.STRING) //restId
				),
		
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
				
		/**********************************4.1.6版本以后使用新外卖接口*************************************/
		//获得外卖首页数据  返回TakeoutIndexPageData
		getTakeoutIndexPageInfo("/getTakeoutIndexPageInfo", 
				new ParamProtocol()
				),
		
		//搜索外卖餐厅列表，返回TakeoutList2DTO
		getTakeoutList2("/getTakeoutList2", 
				new ParamProtocol()
				.define("gpsTypeTag", ParamType.LONG) //经纬度类别  1:原生 2：百度   3：中国（google）
				.define("longitude", ParamType.DOUBLE) //经度  如果为0的话是全城搜索
				.define("latitude", ParamType.DOUBLE) //纬度  如果为0的话是全城搜索
				.define("keywords", ParamType.STRING) //搜索关键词   如果不为空  忽略longitude，latitude 
				.define("typeId", ParamType.STRING) //外卖餐厅类别id  默认为空
				.define("sendLimitId", ParamType.STRING) //起送类别id 默认为空
				.define("pageSize", ParamType.LONG) //页面大小
				.define("startIndex", ParamType.LONG) //当前页
				),
		
		//---------------------------------------------------------------------------------
		//获得外卖餐厅列表 地图，返回TakeoutList2DTO
		//注意返回值中的：转换后的中国经纬度  chineseLon chineseLat  
		getTakeoutListForMap2("/getTakeoutListForMap2", 
				new ParamProtocol()
				.define("gpsTypeTag", ParamType.LONG) //经纬度类别  1:原生 2：百度   3：中国（google）
				.define("longitude", ParamType.DOUBLE) //经度 
				.define("latitude", ParamType.DOUBLE) //纬度  
				.define("gpsRect", ParamType.STRING) //gps矩形   规则   左下点+右上点  例子：  121.495743,31.252139;121.542435,31.217499
				.define("typeId", ParamType.STRING) //外卖餐厅类别id  默认为空
				.define("sendLimitId", ParamType.STRING) //起送类别id 默认为空
				.define("pageSize", ParamType.LONG) //页面大小
		        .define("startIndex", ParamType.LONG) //当前页
				),
				
				
		//---------------------------------------------------------------------------------	
		//获得外卖餐厅菜单 TakeoutMenuListPack2DTO
		getTakeoutMenuList2("/getTakeoutMenuList2", 
				new ParamProtocol()
				.define("takeoutId", ParamType.STRING) //餐厅id
				),
				
		//---------------------------------------------------------------------------------			
		//外卖菜品详情 ，返回TakeoutMenuData2
		getTakeoutMenuInfo2("/getTakeoutMenuInfo2", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //菜品id						),
				),	
				
				
		//---------------------------------------------------------------------------------			
		//把菜品加入收藏
		addTakeoutMenuToFav("/addTakeoutMenuToFav", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //菜品id						),
				),
				
		//---------------------------------------------------------------------------------			
		//删除菜品收藏
		delTakeoutMenuFromFav("/delTakeoutMenuFromFav", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //菜品id						),
				),
						
						
		//---------------------------------------------------------------------------------			
		//获得外卖餐厅信息，返回TakeoutInfoData2
		getTakeoutInfo2("/getTakeoutInfo2", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) // 外卖餐厅id
				),	
				
		//---------------------------------------------------------------------------------		
		//获得用户外卖收货地址列表，返回UserTkRaListPack2DTO 
		getUserTakeoutReceiveAddressList2("/getUserTakeoutReceiveAddressList2", 
				new ParamProtocol()
				),
				
		//---------------------------------------------------------------------------------
		//添加收货地址，返回200成功
		addUserTakeoutReceiveAddress2("/addUserTakeoutReceiveAddress2", 
				new ParamProtocol()
		        .define("name", ParamType.STRING)//姓名
				.define("address", ParamType.STRING) //地址
				.define("tel", ParamType.STRING) //电话
				),
		
		//---------------------------------------------------------------------------------
		//修改收货地址 ，返回200成功
		editUserTakeoutReceiveAddress2("/editUserTakeoutReceiveAddress2", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) //uuid
				.define("name", ParamType.STRING) //姓名
				.define("address", ParamType.STRING) //地址
				.define("tel", ParamType.STRING) //电话
				),
				
				
		//---------------------------------------------------------------------------------
		//获得提交外卖订单的表单信息  返回TakeoutPostOrderFormData
		//用post请求
		getTakeoutPostOrderFormInfo("/getTakeoutPostOrderFormInfo", 
				new ParamProtocol()
		        .define("takeoutId", ParamType.STRING)//外卖餐厅id
				.define("menuSelPack", ParamType.STRING) //TakeoutMenuSelPackDTO menuSelPack//选择的菜品json 字符串
				),
		
		//---------------------------------------------------------------------------------
		//获得赠品列表  返回TakeoutMenuList2DTO
		getTakeoutGiftMenuList("/getTakeoutGiftMenuList", 
				new ParamProtocol()
		        .define("takeoutId", ParamType.STRING)//外卖餐厅id
				.define("typeId", ParamType.STRING) //类别id
				),
				
				
		//---------------------------------------------------------------------------------
		//提交外卖订单，返回SimpleData.uuid 为返回的订单id
		//用post请求
		postTakeoutOrder2("/postTakeoutOrder2", 
				new ParamProtocol()
				.define("takeoutId", ParamType.STRING) //外卖餐厅id
				.define("sendTimeId", ParamType.STRING) //用户期望的送餐时间的id
				.define("userReceiveAdressId", ParamType.STRING) //用户收货地址id						
				.define("memo", ParamType.STRING) //备注 100字
				.define("menuSelPack", ParamType.STRING) //TakeoutMenuSelPackDTO menuSelPack//选择的菜品json 字符串
				.define("payTypeTag", ParamType.LONG)//1：货到付款  2：在线支付				
				),		
		
				
		//---------------------------------------------------------------------------------
		//获得外卖在线支付类别列表，返回PayTypeListDTO
		getTakeoutOnlinePayTypeList("/getTakeoutOnlinePayTypeList", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //外卖订单id
				),	
		
				
		//---------------------------------------------------------------------------------
		//获得支付参数，返回CommonPostPayResultData
		getTakeoutOnlinePayResultData2("/getTakeoutOnlinePayResultData2", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //外卖订单id
				.define("payTypeTag", ParamType.LONG) //支付方式
				),	
				
		//---------------------------------------------------------------------------------
		//获得订单在线支付的状态，返回CommonOrderStateData
		//msg  返回的提示
		//succTag  是否成功     不成功继续等5秒刷新  成功 显示按钮   
		//btnName:按钮文字
		getTakeoutOrderOnlinePayState2("/getTakeoutOrderOnlinePayState2", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //外卖订单id
				),	
				
		
		//---------------------------------------------------------------------------------
		//获得外卖订单详情，返回TakeoutOrderInfoData2
		getTakeoutOrderInfo2("/getTakeoutOrderInfo2", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id
				),
				
		//---------------------------------------------------------------------------------
		//取消外卖订单，返回SimpleData //msg
		cancelTakeoutOrder("/cancelTakeoutOrder", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id
				),
				
		//---------------------------------------------------------------------------------
		//获得外卖订单菜单列表，返回TakeoutMenuSelPackDTO
        getTakeoutOrderMenuSelInfo("/getTakeoutOrderMenuSelInfo", 
				new ParamProtocol()
				.define("orderId", ParamType.STRING) //订单id
				),
				
		//---------------------------------------------------------------------------------
		//获得wap分享信息，返回ShareInfoData
		getWapShareInfo("/getWapShareInfo", 
				new ParamProtocol()
				.define("uuid", ParamType.STRING) 
				),
				
				//---------------------------------------------------------------------------------
		//获得最后一个推送消息，返回PushMsgDTO
		getLastNoticeMessage("/getLastNoticeMessage", 
				new ParamProtocol()
				),		
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//假接口
		noUse(null,null);
		//---------------------------------------------------------------------------------
		
		private String path = ""; //服务地址
		private ParamProtocol protocol; //参数协议
		/**
		 * @param path 服务地址
		 * @param protocol 服务参数协议
		 */
		private API(String path,ParamProtocol protocol) {
			this.path = path;
			this.protocol = protocol;
		}
		
		//参数类型
		enum ParamType{
			LONG,
			DOUBLE,
			STRING,
			BOOL,
			STREAM
			;
		}
		//参数协议,参数不可重名
		static class ParamProtocol{
			//--
			HashMap<String,ParamType> params=new HashMap<String,ParamType>(8);
			boolean haveInputStream;
			ParamProtocol(){
			}
			
			//需要inputStream
			ParamProtocol(boolean haveInputStream){
				this.haveInputStream=haveInputStream;
			}
			ParamProtocol define(String paramName, ParamType type){
				params.put(paramName, type);
				return this;
			}
			int size(){
				return params.size();
			}
		}
	}


	
	/**************************************************************************************/
	private API api; //服务地址
	private boolean usePost = false; // 使用post请求
	private ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	private InputStream is;
	private boolean canUsePost=false;

	public boolean isCanUsePost() {
		return canUsePost;
	}

	public void setCanUsePost(boolean canUsePost) {
		this.canUsePost = canUsePost;
	}

	/**
	 * @param path 服务地址
	 * @param protocol 服务参数协议
	 */
	public ServiceRequest(API api) {
		this.api = api;
	}

	public String getUrl() {
		return A57HttpApiV3.getInstance().fullUrl(api.path);
	}
	public API getAPI(){
		return this.api;
	}
	public boolean isPost() {
		return usePost;
	}
	
	public InputStream getInputStreamData() {
		return is;
	}

	public BasicNameValuePair[] getParamArray() { 
		return params.toArray(new BasicNameValuePair[]{}); 
	}

	public ServiceRequest addData(String name, long value) {
		params.add(new BasicNameValuePair(name, String.valueOf(value)));
		return this;
	}

	public ServiceRequest addData(String name, double value) {
		params.add(new BasicNameValuePair(name, String.valueOf(value)));
		return this;
	}

	public ServiceRequest addData(String name, boolean value) {
		params.add(new BasicNameValuePair(name, String.valueOf(value)));
		return this;
	}

	public ServiceRequest addData(InputStream inputStream) {
		usePost = true;
		is = inputStream;
		return this;
	}

	public ServiceRequest addData(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
		return this;
	}
	public String toString(){
		return api.path;
	}
	/**
	 * 检查当前请求实例是否有效（是否缺失参数，类型是否正确等）
	 * @throws RuntimeException
	 */
	public void validate() throws RuntimeException {
		//---
		if(params.size()!=api.protocol.size()){
			throw new RuntimeException("["+api.name()+"]参数数目不对:当前"+params.size()+"个，需要"+api.protocol.size()+"个");
		}
		//---
		if(api.protocol.haveInputStream && !(usePost && is!=null) ){
			throw new RuntimeException("["+api.name()+"]需要InputStream");
		}
		Set<Entry<String,ParamType>> set=api.protocol.params.entrySet();
		for(Entry<String,ParamType> pEntry:set){
			BasicNameValuePair p=null;
			for(BasicNameValuePair tp:params){
				//名字找到
				if(tp.getName().equals(pEntry.getKey())){
					p=tp;
					break;
				}
			}
			if(p==null){
				throw new RuntimeException("["+api.name()+"]参数未找到："+pEntry.getKey());
			}
			//检查类型
			try{
				switch(pEntry.getValue()){
					case LONG: Long.parseLong(p.getValue());
						break;
					case DOUBLE: Double.parseDouble(p.getValue());
						break;
					case BOOL: 
						if(p.getValue().equals("true")||p.getValue().equals("false")){
							break;
						}else{
							throw new Exception("not a boolean value");
						}
				}
			}catch(Exception ex){
				throw new RuntimeException("["+api.name()+"]参数类型不对："+pEntry.getKey()+"必须是"+pEntry.getValue().name()+"类型");
			}
		}
	}
	//------------------------------------以下是测试用接口---------------------------------
//	private static final String ADD_DEBUG_ACCOUNT = "http://www.xiaomishu.com/zy/addtestuser";
	private static final String ADD_DEBUG_ACCOUNT = "/addTestUser";
	private static final String VERIFY_TEST_USER = "/verifyTestUser";
	
	public static JsonPack addDebugAccount(
			String debugName,
			String debugPwd
			) throws Exception {
		DefaultHttpClient client = null;
			try {
				client = AbstractHttpApi.createHttpClientForUpload();
				HttpGet get = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(ADD_DEBUG_ACCOUNT),
						new BasicNameValuePair("debugName", debugName),
						new BasicNameValuePair("debugPwd", debugPwd),
						new BasicNameValuePair("appId", ContextUtil.getContext().getPackageName()),
						new BasicNameValuePair("debugTag", String.valueOf(ActivityUtil.isDebug())));
				JsonPack ret = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, get);
				return ret;
			}
			catch (Exception e) {
				throw new Exception("发生错误："+e.getMessage());
			}
			finally {
				if (client != null) {
					try {
						client.getConnectionManager().shutdown();
					}
					catch (Exception e) {
					}
				}
			}
	}
	//验证切换测试机 /verifyTestUser?pwd={pwd}
	public static JsonPack verifyTestUser(
			String name,//用户名  ,目前后台只使用了pwd，没有使用名字
			String pwd//密码
			)  throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(VERIFY_TEST_USER),
				new BasicNameValuePair("name", name),
				new BasicNameValuePair("pwd", pwd));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//------------------------------------测试接口完---------------------------------
	/********************************** 遗留接口，项目时间进度问题，这些接口使用旧架构 ****************************************/
	private static final String URL_API_GET_SUGGEST_KEYWORD_LIST= "/getSuggestKeywordList";
	private static final String URL_API_GET_REST_SEARCH_SUGGEST_LIST= "/getRestSearchSuggestList";
	private static final String URL_API_GET_USED_HISTORY_SUGGEST_LIST= "/getUsedHistorySuggestList";
	private static final String URL_API_GET_CASH_COUPON_LIST = "/getCashCouponList2";
	private static final String URL_API_GET_REST_LIST = "/getRestList";
	private static final String URL_API_GET_REST_LIST2 = "/getRestList2";
	private static final String URL_API_GET_REST_LIST3 = "/getRestList3";
	private static final String URL_API_GET_REST_LIST4 = "/getRestList4";
	private static final String URL_API_GET_RES_PIC_LIST = "/getRestPicList";
	private static final String URL_API_GET_REST_COMMENTLIST_LIST = "/getRestCommentList";
	private static final String URL_API_GET_REST_COMMENT_REPLY_LIST = "/getRestCommentReplyList";
	private static final String URL_API_SHARE_TO = "/shareTo";
	//上传图片
	private static final String URL_API_UPLOAD_IMAGE= "/upload/uploadImage";
	//添加评论
	private static final String URL_API_POST_COMMENT= "/upload/postComment";
	//添加菜品数据
	private static final String URL_API_ADD_OR_UPDATE_FOOD = "/addOrUpdateFood";
	
	private static final String URL_API_GET_REGION_LIST= "/getRegionList";
	private static final String URL_API_GET_DISTRICT_LIST= "/getDistrictList";
	private static final String URL_API_GET_FOOD_MAIN_TYPE_LIST= "/getFoodMainTypeList";
	private static final String URL_API_CALL_TEL= "/callTel";
	private static final String URL_API_SEND_SMS= "/sendSms";
	private static final String URL_API_GET_PUSH_MSG_LIST= "/getPushMsgList";
	private static final String URL_API_GET_BWC_LIST= "/getBwcList";
	
	//带关键词高亮的搜索建议    使用  <b></b>  来标红  比如  上海<b>川</b>菜馆的<b>川</b>菜
	public static JsonPack getSuggestKeywordList(
			String keywords,//关键词
			int pageSize,//页面大小
			int startIndex 
	) throws Exception {
		//long start=SystemClock.elapsedRealtime();
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_SUGGEST_KEYWORD_LIST), 
				new BasicNameValuePair("keywords", keywords),
				new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
				new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		//Log.e("===>>",""+(SystemClock.elapsedRealtime()-start));
		return jsonPack;
	}
	//餐馆搜索建议列表，返回RestSearchSuggestListDTO    使用  <b></b>  来标红  比如  上海<b>川</b>菜馆的<b>川</b>菜
	public static JsonPack getRestSearchSuggestList(
			String keywords,//关键词
			int pageSize,//页面大小
			int startIndex 
			) throws Exception {
		
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_REST_SEARCH_SUGGEST_LIST), 
				new BasicNameValuePair("keywords", keywords),
				new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
				new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	
	//使用历史建议列表，返回UsedHistorySuggestListDTO
	public static JsonPack getUsedHistorySuggestList(
			) throws Exception {
		
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_USED_HISTORY_SUGGEST_LIST));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	
	// 获得现金券或特惠套餐列表，返回CashCouponListDTO
	//CashCouponData 增加 soldNum
	public static JsonPack getCashCouponList2(int typeTag, // 类别 1:现金券 2：套餐
			int distanceMeter, // 附近距离 约定：0：不是附近搜索 默认5000米
			String regionId, // 地域ID 约定 ""：全部地域 其他：所选地域ID
			String menuId, // 菜系类别ID 约定 "":为全部菜系 其他：所选菜系ID
			int sortTypeTag, // 排序 0：默认 其他：所选排序
			int pageSize,// 页面大小
			int startIndex // 当前页
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(URL_API_GET_CASH_COUPON_LIST),
						new BasicNameValuePair("typeTag", String.valueOf(typeTag)),
						new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
						new BasicNameValuePair("regionId", regionId),
						new BasicNameValuePair("menuId", menuId),
						new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi
				.doHttpRequest(httpGet);
		return jsonPack;
	}
	//------------------------------------------------------------------------------
	//获取餐馆列表 ，返回RestListDTO
						//代替以前的 getResAndFoodList2
	
	/**
	 * @deprecated
	 */
	public static JsonPack getRestList(
			int distanceMeter, //附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
			String regionId, //地域ID ,约定"" ：全部地域                其他：所选地域ID
			String districtId, //商区ID,约定""  :全部商区                   其他：所选商区ID
			String mainMenuId, //主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
			String subMenuId, //子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
			String mainTopRestTypeId, //主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
			String subTopRestTypeId, //子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
			String keywords, //搜索关键词
			int avgTag, //人均  0：默认
			int sortTypeTag, //排序  0：默认
			int pageSize,// 页面大小
			int startIndex // 当前页
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(	URL_API_GET_REST_LIST),
						new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
						new BasicNameValuePair("regionId", regionId),
						new BasicNameValuePair("districtId", districtId),
						new BasicNameValuePair("mainMenuId", mainMenuId),
						new BasicNameValuePair("subMenuId", subMenuId),
						new BasicNameValuePair("mainTopRestTypeId", mainTopRestTypeId),
						new BasicNameValuePair("subTopRestTypeId", subTopRestTypeId),
						new BasicNameValuePair("keywords", keywords),
						new BasicNameValuePair("avgTag", String.valueOf(avgTag)),
						new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//------------------------------------------------------------------------------
	//获取餐馆列表 ，返回RestListDTO
	public static JsonPack getRestList2(
			int distanceMeter, //附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
			String regionId, //地域ID ,约定"" ：全部地域                其他：所选地域ID
			String districtId, //商区ID,约定""  :全部商区                   其他：所选商区ID
			String mainMenuId, //主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
			String subMenuId, //子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
			String mainTopRestTypeId, //主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
			String subTopRestTypeId, //子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
			String keywords, //搜索关键词
			int avgTag, //人均  0：默认
			int sortTypeTag, //排序  0：默认
			int pageSize,// 页面大小
			int startIndex // 当前页
			) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(	URL_API_GET_REST_LIST2),
						new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
						new BasicNameValuePair("regionId", regionId),
						new BasicNameValuePair("districtId", districtId),
						new BasicNameValuePair("mainMenuId", mainMenuId),
						new BasicNameValuePair("subMenuId", subMenuId),
						new BasicNameValuePair("mainTopRestTypeId", mainTopRestTypeId),
						new BasicNameValuePair("subTopRestTypeId", subTopRestTypeId),
						new BasicNameValuePair("keywords", keywords),
						new BasicNameValuePair("avgTag", String.valueOf(avgTag)),
						new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//获取餐馆列表 ，返回RestListDTO
	public static JsonPack getRestList3(
			boolean subwayTag,//是否是地铁站搜索
			int distanceMeter, //附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
			String regionId, //地域ID ,约定"" ：全部地域                其他：所选地域ID
			String districtId, //商区ID,约定""  :全部商区                   其他：所选商区ID
			String mainMenuId, //主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
			String subMenuId, //子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
			String mainTopRestTypeId, //主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
			String subTopRestTypeId, //子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
			String keywords, //搜索关键词
			int avgTag, //人均  0：默认
			int sortTypeTag, //排序  0：默认
			int pageSize,// 页面大小
			int startIndex // 当前页
			) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(	URL_API_GET_REST_LIST3),
						new BasicNameValuePair("subwayTag", String.valueOf(subwayTag)),
						new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
						new BasicNameValuePair("regionId", regionId),
						new BasicNameValuePair("districtId", districtId),
						new BasicNameValuePair("mainMenuId", mainMenuId),
						new BasicNameValuePair("subMenuId", subMenuId),
						new BasicNameValuePair("mainTopRestTypeId", mainTopRestTypeId),
						new BasicNameValuePair("subTopRestTypeId", subTopRestTypeId),
						new BasicNameValuePair("keywords", keywords),
						new BasicNameValuePair("avgTag", String.valueOf(avgTag)),
						new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	
	//获取餐馆列表 ，返回RestListDTO
	public static JsonPack getRestList4(
			boolean subwayTag,//是否是地铁站搜索
			int distanceMeter, //附近距离,约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米      默认1000米
			String restId,//餐馆id 可以为空
			String regionId, //地域ID ,约定"" ：全部地域                其他：所选地域ID
			String districtId, //商区ID,约定""  :全部商区                   其他：所选商区ID
			String mainMenuId, //主菜系类别ID       约定 "" :为全部主菜系          其他：所选菜系ID
			String subMenuId, //子菜系类别ID        约定 "" :为全部子菜系          其他：所选菜系ID
			String mainTopRestTypeId, //主榜单类别    约定 "" :为全部主榜单           其他：所选主榜单ID
			String subTopRestTypeId, //子榜单类别       约定 "" :为全部子榜单类别  其他：所选子榜单类别ID
			String keywords, //搜索关键词
			int avgTag, //人均  0：默认
			int sortTypeTag, //排序  0：默认
			int pageSize,// 页面大小
			int startIndex // 当前页
			) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(	URL_API_GET_REST_LIST4),
						new BasicNameValuePair("subwayTag", String.valueOf(subwayTag)),
						new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
						new BasicNameValuePair("restId", restId),
						new BasicNameValuePair("regionId", regionId),
						new BasicNameValuePair("districtId", districtId),
						new BasicNameValuePair("mainMenuId", mainMenuId),
						new BasicNameValuePair("subMenuId", subMenuId),
						new BasicNameValuePair("mainTopRestTypeId", mainTopRestTypeId),
						new BasicNameValuePair("subTopRestTypeId", subTopRestTypeId),
						new BasicNameValuePair("keywords", keywords),
						new BasicNameValuePair("avgTag", String.valueOf(avgTag)),
						new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	

	//获得图片列表  返回ResPicList2DTO
	//替换原来的getResPicList
	//去掉了 参数  foodTypeId //菜品类别 约定  空为全部   否则是指定类别 
	//ResPicListDTO  去掉了 foodTypeList
	public static JsonPack getRestPicList(
			String restId,// 餐馆ID
			int picViewTag,// 查看类别 约定 0:全部 1：环境 2：菜式 3：会员上传 4:其他
			int pageSize,// 页面大小
			int startIndex// 下一页索引
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(URL_API_GET_RES_PIC_LIST),
						new BasicNameValuePair("restId", restId),
						new BasicNameValuePair("picViewTag", String.valueOf(picViewTag)),
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	
	// 获得评论列表，返回CommentListDTO
	// 替换原来的 getResCommentList
	public static JsonPack getRestCommentList(
			String restId,// 餐馆ID
			String orderId,// 订单ID，可以为空
			long pageSize, // 页面大小
			long startIndex // 当前页
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(URL_API_GET_REST_COMMENTLIST_LIST), 
						new BasicNameValuePair("restId",restId), 
						new BasicNameValuePair("orderId",orderId), 
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)), 
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	// // 获得评论回复列表，返回CommentReplyListDTO
	// // 替换原来的 getResCommentReplyList
	public static JsonPack getRestCommentReplyList(
			String commentId,// 评论id
			long pageSize, // 页面大小
			long startIndex // 当前页
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi
				.createHttpGet(
						A57HttpApiV3.getInstance().fullUrl(URL_API_GET_REST_COMMENT_REPLY_LIST), 
						new BasicNameValuePair("commentId", commentId), 
						new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
						new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	// ---------------------------------------------------------------------------------
	// 分享餐馆信息到新浪微博等 SimpleData
	public static JsonPack shareTo(
			long typeTag,// 1:分享餐厅 2：软件分享  3:是推荐餐厅
			String token,// 用户token 可以为空
			String uuid, // uuid typeTag：1时为餐馆id typeTag:3时是推荐id
			String detail, // 内容
			String shareTo // 分享到微博 sina:1;qq:0 当前只有sina:1 或者 sina:0
							// 如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_SHARE_TO), 
				new BasicNameValuePair("typeTag", String.valueOf(typeTag)), 
				new BasicNameValuePair("token", token),
				new BasicNameValuePair("uuid", uuid), 
				new BasicNameValuePair("detail", detail), 
				new BasicNameValuePair("shareTo", shareTo));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}

	//上传图片
	public static JsonPack uploadImage(
			int typeTag,//类别  1:餐馆  2:菜品  3：点评页
			String restId,//餐馆id
			String foodId,//菜品id  type=2 是菜品id 其他为空
			String memo,//备注  可以为空  typeTag=1时 传递
			String token, //用户token  可以为空 typeTag=1时 传递
			String shareTo,//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
			int likeTypeTag, //喜欢类型   0:没有选择任何喜欢类型  1:好吃  2:一般 3:不好吃		当	typeTag：  2时候有效	
			InputStream pic
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			// 设置上传图片的超时为30秒
			int timeout = 30;
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
	        HttpConnectionParams.setSoTimeout(params, timeout * 1000);
	        client.setParams(params);
			
			HttpPost httpPost;
			JsonPack jsonPack;
			try {
				httpPost = A57HttpApiV3.getInstance().mHttpApi.createHttpPost(A57HttpApiV3.getInstance().fullUrl(URL_API_UPLOAD_IMAGE), 
						pic,
						new BasicNameValuePair("typeTag", String.valueOf(typeTag)),
						new BasicNameValuePair("restId", restId), 
						new BasicNameValuePair("foodId", foodId), 
						new BasicNameValuePair("memo", memo),
						new BasicNameValuePair("token", token), 
						new BasicNameValuePair("shareTo", shareTo),
						new BasicNameValuePair("likeTypeTag", String.valueOf(likeTypeTag)));
				jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, httpPost);
				return jsonPack;
			} catch (Exception e) {
				throw e;
			}
			
		}
		catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		}
		finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				}
				catch (Exception e) {
					
				}
			}
		}
	}
	
	//添加评论,返回SimpleData
	public static JsonPack postComment(
			String restId,//餐馆id
			int postTag,//1：从点评页进去    2：从订单页进去   3：从随手拍进去 菜品     4：从随手拍进去 餐馆
			String foodId,//菜品id  如果 postTag:3 foodId不能为空     postTag:其他 为空
			String orderId,//订单id  如果 postTag:2 orderId不能为空     postTag:其他 为空
			String imgSizeList, //图片大小   可以为空   多图为   13242314;29282;29282  
			String picId,//picId为uploadImage2返回的图片或者为空  picId可以是多个 111;222;333   //图片id，只有在postTag==3的时候需要传图片id
			int overallNum,//总体评价      0为未选择
			int tasteNum,//口味      0为未选择
			int envNum,//环境       0为未选择
			int serviceNum,//服务      0为未选择
			String detail, //评论内容        postTag=3时传递图片描述  可以为空    postTag=其他 不能为空
			String shareTo //分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
	) throws Exception {
		HttpPost httpPost = A57HttpApiV3.getInstance().mHttpApi.createHttpPost(				
				A57HttpApiV3.getInstance().fullUrl(URL_API_POST_COMMENT), 				
				new BasicNameValuePair("restId", restId),
				new BasicNameValuePair("postTag", String.valueOf(postTag)),
				new BasicNameValuePair("foodId", foodId),
				new BasicNameValuePair("orderId", orderId),
				new BasicNameValuePair("imgSizeList", imgSizeList),
				new BasicNameValuePair("picId", picId),
				new BasicNameValuePair("overallNum", String.valueOf(overallNum)),
				new BasicNameValuePair("tasteNum", String.valueOf(tasteNum)),
				new BasicNameValuePair("envNum", String.valueOf(envNum)),
				new BasicNameValuePair("serviceNum", String.valueOf(serviceNum)),
				new BasicNameValuePair("detail", detail),
				new BasicNameValuePair("shareTo", shareTo));
				
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpPost);
		return jsonPack;
	}
	
	//添加菜品数据
	static public JsonPack addOrUpdateFood(
			String restId,//餐馆id
			String foodId,//菜品id  foodId为空时为update 不为空时为add
			String foodName,//菜品名称  可以为空
			String foodPrice,//菜品价格   可以为空
			String foodUnit,//菜品单位  可以为空
			String memo,//备注  可以为空
			String token, //用户token  可以为空
			int likeTypeTag //喜欢类型  1:好吃  2:一般 3:不好吃
	) throws Exception {
		HttpGet httpPost = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_ADD_OR_UPDATE_FOOD), 
				new BasicNameValuePair("restId", restId),
				new BasicNameValuePair("foodId", foodId),
				new BasicNameValuePair("foodName", foodName),
				new BasicNameValuePair("foodPrice", foodPrice),
				new BasicNameValuePair("foodUnit", foodUnit),
				new BasicNameValuePair("memo", memo),
				new BasicNameValuePair("token", token),
				new BasicNameValuePair("likeTypeTag", String.valueOf(likeTypeTag)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpPost);
		return jsonPack;
	}
	
	//获得地域列表 ，返回CommonTypeListDTO(uuid,name)
	//缓存一周
	public static JsonPack getRegionList(
			
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_REGION_LIST));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//获得商区列表 ，返回CommonTypeListDTO(uuid,name)
	//缓存一周
	public static JsonPack getDistrictList(
			String regionId//所选地域ID
			) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_DISTRICT_LIST),
				new BasicNameValuePair("regionId", regionId));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//获得主菜系列表 ，返回CommonTypeListDTO(uuid,name)
	public static JsonPack getFoodMainTypeList(
			
	) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_FOOD_MAIN_TYPE_LIST));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//通知拨打电话
	public static JsonPack callTel(
			int posTag,		//位置    1:首页 2:餐馆  3:外卖  4:现金券
			String uuid,	//uuid  posTag=1,2 时为餐馆id   3:外卖餐厅id   4:现金券id
			String tel		//电话
			) throws Exception {
		HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_CALL_TEL),
				new BasicNameValuePair("posTag", posTag+""),
				new BasicNameValuePair("uuid", uuid),
				new BasicNameValuePair("tel", tel));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
		return jsonPack;
	}
	//发送短信
	public static JsonPack sendSms(	
			boolean havePlaceGpsTag,//是否有参照地标gps
			String cityId,//城市id
			String placeLon,//地标经度  可以为空
			String placeLat,//地标纬度 可以为空
			String placeName,//地标名称
			String restId,//餐馆id
			String orderId,//订单id  可以为空
			String templetId,//模板id
			String friendList,//好友列表    1:na:13874657685;2:李四:13566666666    其中  1:代表网站好友  2:代表本地好友
			String content//短信内容  不超过70字
	) throws Exception {
		HttpGet http = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_SEND_SMS), 
				new BasicNameValuePair("havePlaceGpsTag", String.valueOf(havePlaceGpsTag)),
				new BasicNameValuePair("cityId", cityId),
				new BasicNameValuePair("placeLon", placeLon),
				new BasicNameValuePair("placeLat", placeLat),
				new BasicNameValuePair("placeName", placeName),
				new BasicNameValuePair("restId", restId),
				new BasicNameValuePair("orderId", orderId),
				new BasicNameValuePair("templetId", templetId),
				new BasicNameValuePair("friendList", friendList),
				new BasicNameValuePair("content", content));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(http);
		return jsonPack;
	}
	
	//获得推送消息列表,返回PushMsgPack2DTO
	public static JsonPack getPushMsgList(
			String cityId,//城市id
			String token //用户token  可以为空
	) throws Exception {
		DefaultHttpClient client = null;
		try {
			client = AbstractHttpApi.createHttpClient();
			HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
					A57HttpApiV3.getInstance().fullUrl(URL_API_GET_PUSH_MSG_LIST), 
					new BasicNameValuePair("token", token),
					new BasicNameValuePair("cityId", cityId));
			JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, httpGet);
			return jsonPack;
		}
		catch (Exception e) {
			JsonPack jsonPack = new JsonPack();
			jsonPack.setRe(-1);
			return jsonPack;
		}
		finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				}
				catch (Exception e) {
					
				}
			}
		}
	}
	
	
	/*--------------------------------------------------------------------------
	|  霸王菜接口
	--------------------------------------------------------------------------*/
	//霸王菜列表，返回BwcListDTO
	public static JsonPack getBwcList(
			int distanceMeter,//附近距离                        约定 0    ：不是附近搜索        其他：  500米 ，1000米 ，2000米, 5000米 
			String regionId,//地域ID              约定"" ：全部地域                其他：所选地域ID
			String districtId,//商区ID            约定""  :全部商区                   其他：所选商区ID
			String typeId,//类别ID                约定""  :默认                          其他：所选类别ID  
			String keywords,//搜索关键词
			int sortTypeTag,//排序  0：默认
			int avgTag,//人均  0：默认
			int pageSize,//页面大小
			int startIndex//当前页
	) throws Exception {
		
		HttpGet http = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(
				A57HttpApiV3.getInstance().fullUrl(URL_API_GET_BWC_LIST), 
				new BasicNameValuePair("distanceMeter", String.valueOf(distanceMeter)),
				new BasicNameValuePair("regionId", regionId),
				new BasicNameValuePair("districtId", districtId),
				new BasicNameValuePair("typeId", typeId),
				new BasicNameValuePair("keywords", keywords),
				new BasicNameValuePair("avgTag", String.valueOf(avgTag)),
				new BasicNameValuePair("sortTypeTag", String.valueOf(sortTypeTag)),
				new BasicNameValuePair("pageSize", String.valueOf(pageSize)),
				new BasicNameValuePair("startIndex", String.valueOf(startIndex)));
		JsonPack jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(http);
		return jsonPack;

	}
}






















