package com.fg114.main.qmoney;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;


import com.fg114.main.qmoney.tools.RSATool;
import com.fg114.main.service.dto.KqReqData;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.JsonUtils;
import com.qmoney.third.OrderInfo;
import com.qmoney.third.PayRequest;
import com.qmoney.tools.CommonUtils;
import com.qmoney.tools.FusionField;
import com.qmoney.ui.OnRequestListener;
import com.qmoney.ui.PayService;

/**
 * 快钱支付
 * @author dengxiangyu
 *
 */
public class QuickMoneyUtils {
	/**
	 * 初始化快钱
	 * 
	 * @param context
	 */
	public static void initQuickMoneyUtils(Context context) {
		try {

			 RSATool.init(ContextUtil.getContext().getAssets().open(Config.PRIVATE_KEY_PATH)) ;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 快钱支付
	 * @param ativity
	 * @param weiXinInfo
	 */
	public static void doPay(Activity cxt,Class<?> activity, String qMoneyInfo,View view,String callbackClassName) {
		
		PayRequest request = getPayRequest(cxt,activity,qMoneyInfo,view,callbackClassName) ;
		  
        if(null != request){
       	   PayService.pay(request) ;           //调用支付接口
        }  
	}
	
	
	/***************************************************
	 * 得到支付响应
	 * 2013-12-2
	 */ 
	private static PayRequest getPayRequest(Activity cxt,Class<?> activity, String qMoneyInfo,View view,String callbackClassName) {
         PayRequest request = null ;

			  KqReqData kqReqData= JsonUtils.fromJson(qMoneyInfo, KqReqData.class);
			  
			  OrderInfo orderInfo=new OrderInfo();
        	  
			  orderInfo.setAmt(kqReqData.payOrder.amt);
			  orderInfo.setMerchantName(kqReqData.payOrder.merchantName);
			  orderInfo.setOrderId(kqReqData.payOrder.orderId);
			  orderInfo.setProductName(kqReqData.payOrder.productName);
			  orderInfo.setTotal(kqReqData.payOrder.total);
			  orderInfo.setMerchantOrderTime(kqReqData.payOrder.merchantOrderTime);
			  orderInfo.setUnitPrice(kqReqData.payOrder.unitPrice);
			  orderInfo.setOrderSign(kqReqData.orderSign);
			  orderInfo.setQuerySign(kqReqData.querySign);
        		// 支付后回调商户的intent包名
			  String callbackPackageName = "com.fg114.main";
              
              String callbackClassName1="com.fg114.main.qmoney.KqActivity";
              FusionField.orderInfo = orderInfo ;
        	  request = new PayRequest(cxt,
        			    activity, view,
						callbackPackageName, callbackClassName1,
						kqReqData.mebCode, kqReqData.merchantId,       //商户自己的会员号，商户号
						kqReqData.partnerUserId, "", "", null,"" ,
						kqReqData.javaUrl, orderInfo);
		 
         
		 return request ;
	}
	
	
	/************************************************************
	 * 监听快钱受理能力的银行数据
	 * @credit 快钱支持的受理能力的信用卡的银行 名称
	 * @debit  快钱支持的受理能力的借记卡的银行名称
	 * @HashMap<String, String> credit 快钱支持的受理能力的信用卡的银行
	 * 与对应的银行Id
	 * @HashMap<String, String> debit 快钱支持的受理能力的借记卡的银行
	 * 与对应的银行Id
	 * 
	 */
	OnRequestListener requestListener = new OnRequestListener() {
		public void onFinish(String[] credit, String[] debit) {
			//快钱支持的银行名称
		}

		public void onFinish(HashMap<String, String> credit,
				HashMap<String, String> debit) {
			
			//快钱支持的银行名称与银行Id集合
			
			if(null != credit){
				   
				  Set<Map.Entry<String, String>> set = credit.entrySet() ;
				  Iterator<Map.Entry<String, String>> iterator = set.iterator() ;
				  
				  while(iterator.hasNext()){
					  Map.Entry<String, String> entry = iterator.next() ;
					  System.out.println(" 键是 = " + entry.getKey() + " 值是 = " + entry.getValue());
				  }
				
			}
			
			if(null != debit){
				
				 //TODO
				
			}
			
			if(null == credit && debit == null){
				   
				   CommonUtils.closeDialog() ;      //关闭对话框
				
			}
		}
	};
	
	
//	/**
//	 * 接收回调
//	 */
//	protected void onNewIntent(Intent intent) {
//		String orderId = intent.getStringExtra("orderId") ;
//		String payResult = intent.getStringExtra("payResult") ;
//		
//		if(!TextUtils.isEmpty(orderId) && ! TextUtils.isEmpty(payResult)){
//			   int payResultCode = Integer.parseInt(payResult) ;
//			   String payResultStr = "" ;
//			   
//			   // 1：支付成功 2:支付失败 0： 交易取消
//			   switch (payResultCode) {
//			case 0:
//				payResultStr = "交易取消" ;
//				break;
//				
//			case 1 :
//				payResultStr = "支付成功" ;
//				break ;
//				
//			case 2 :
//				payResultStr = "支付失败" ;
//				break ;
//
//			default:
//				break;
//			}
//			
//		   Toast.makeText(this, "订单号：" + orderId + "，支付结果：" + payResultStr,
//				 Toast.LENGTH_SHORT).show();   
//		}
//
//		super.onNewIntent(intent);
//	}
}
