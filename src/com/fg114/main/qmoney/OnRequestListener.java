package com.fg114.main.qmoney;
/**********************************************************
 * 网络请求监听接口 监听快钱返回的银行列表数据
 * @author wangjun
 * 2014-3-12
 */
public interface OnRequestListener {
	public void onFinish(String[] credit , String[] debit) ;
}
