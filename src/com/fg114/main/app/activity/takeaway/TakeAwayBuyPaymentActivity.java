package com.fg114.main.app.activity.takeaway;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.alipay.AliPayUtils;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.mealcombo.GroupBuyPaymentActivity;
import com.fg114.main.qmoney.QuickMoneyUtils;
import com.fg114.main.service.dto.CommonPostPayResultData;
import com.fg114.main.service.dto.PayTypeListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutOnlinePayResultData;
import com.fg114.main.service.dto.TakeoutPostOrderFormData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JavaScriptInterface;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.UnionpayUtils;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.wxapi.WXPayEntryActivity;
import com.fg114.main.wxapi.WeixinUtils;
/**
 * 外卖支付选择列表
 * 需要传入orderID
 * @author dengxiangyu
 *
 */
public class TakeAwayBuyPaymentActivity extends MainFrameActivity {
	private static final String TAG = "TakeAwayBuyPaymentActivity";
	private LinearLayout takeaway_buy_payment_layout;
	private boolean mFromUnionPay; // 是否曾跳转过银联支付
	private boolean isJumpWeiXinPay = false;// 是否曾跳转过微信支付
//	private String startPage = Settings.PAYMENT_HOST + "/coupon/buy?";
	private WebView webView;
	private String currentUrl = "";
	private JavaScriptInterface mJavaScriptInterface = new JavaScriptInterface();
	private boolean isJumpkQPay = false;// 是否曾跳转过快钱支付
	private String orderId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖在线支付选择", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		}
		
		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			// TODO: 跳至无网提示页
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();

		excuteOnlinePayList();
	}

	
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖在线支付选择", "");
		// ----------------------------
	}



	@Override
	protected void onResume() {

		super.onResume();
		
		
		
		if (mFromUnionPay) {
			String result = UnionpayUtils.getUnionpayResult();
			if (!TextUtils.isEmpty(result)) {
				// Log.e("msh", "result = " + result);
				if (result.contains("成功") || result.contains("<respCode>0000</respCode>")) {
					DialogUtil.showToast(this, "支付成功");
					// 跳至外卖支付处理拦截页面
					gotoTakeAwayCashIntercept();
				} else if (result.contains("<respCode>9001</respCode>")) {
					DialogUtil.showToast(this, "支付已取消");
				} else {
					int start = result.indexOf("<respDesc>");
					int end = result.indexOf("</respDesc>");
					String desc = "";
					if (start != -1 && end != -1 && start + 10 < end) {
						desc = ":" + result.substring(start + 10, end);
					}
					DialogUtil.showToast(this, "支付失败" + desc);
				}
			}
			finish();
		}
		
		
		if (isJumpWeiXinPay) {
			int WXPayResult = WXPayEntryActivity.getWeiXinPayResult();
			// Log.e("msh", "result = " + result);
			if (WXPayResult == 1) {
				DialogUtil.showToast(this, "支付成功");
				// 跳至现金券支付处理拦截页面
				gotoTakeAwayCashIntercept();
				
			} else if (WXPayResult == 2) {
				DialogUtil.showToast(this, "支付已取消");
			} else {
				DialogUtil.showToast(this, "支付失败");
			}
			finish();

		}
		
	}
	
	/**
	 * 接收快钱回调
	 */
	protected void onNewIntent(Intent intent) {
		if (isJumpkQPay) {
			String orderId = intent.getStringExtra("orderId");
			String payResult = intent.getStringExtra("payResult");

			if (!TextUtils.isEmpty(orderId) && !TextUtils.isEmpty(payResult)) {
				int payResultCode = Integer.parseInt(payResult);
				String payResultStr = "";

				// 1：支付成功 2:支付失败 0： 交易取消
				switch (payResultCode) {
				case 0:
					DialogUtil.showToast(this, "支付已取消");
					break;

				case 1:
					
					DialogUtil.showToast(this, "支付成功");
					
					// 跳至现金券支付处理拦截页面
					gotoTakeAwayCashIntercept();
					break;

				case 2:
					DialogUtil.showToast(this, "支付失败");
					break;

				default:
					DialogUtil.showToast(this, "支付失败");
					break;
				}

			}
			
			finish();

		}
		isJumpkQPay = false;
		super.onNewIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeProgressDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (webView != null) {
			webView.stopLoading();
			webView.clearCache(true);
			webView.clearHistory();
			webView.clearFocus();
			webView.clearView();
			webView.destroy();
		}
	}

	@Override
	public void onBackPressed() {
		doBackButtonAction();
	}

	private void initComponent() {
		this.getTvTitle().setText("在线支付");
		// 返回按钮事件
		this.getBtnGoBack().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				doBackButtonAction();
			}
		});

		LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contextView = (View) mInflater.inflate(R.layout.takeaway_buy_payment_act, null);
		takeaway_buy_payment_layout = (LinearLayout) contextView.findViewById(R.id.takeaway_buy_payment_layout);
		webView = (WebView) contextView.findViewById(R.id.takeaway_webView);

		// webView设置
		initWebView();

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// 获得外卖在线支付类别列表，返回PayTypeListDTO
	private void excuteOnlinePayList() {
		ServiceRequest request = new ServiceRequest(API.getTakeoutOnlinePayTypeList);
		request.addData("orderId", orderId);// 外卖订单id
		
		// ----------------------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// ----------------------------

		CommonTask.request(request, "", new CommonTask.TaskListener<PayTypeListDTO>() {
			@Override
			protected void onSuccess(PayTypeListDTO dto) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				addView(dto);
			}

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
//				doTest_confirm();
				DialogUtil.showToast(ContextUtil.getContext(), message);
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
				finish();
			}

			private void doTest_confirm() {
				String s = "{\"list\":[{\"typeTag\":\"1\",\"title\":\"银联 (客户端)\",\"hint\":\"客户端\"},{\"typeTag\":\"2\",\"title\":\"支付宝(客户端)\",\"hint\":\"客户端\"},{\"typeTag\":\"3\",\"title\":\"支付宝(wap)\",\"hint\":\"wap\"},{\"typeTag\":\"4\",\"title\":\"支付宝信用卡(wap)\",\"hint\":\"wap\"}]}";
				PayTypeListDTO data = JsonUtils.fromJson(s, PayTypeListDTO.class);
				onSuccess(data);

			}

		});
	}

	private void addView(final PayTypeListDTO dto) {
		if (dto == null) {
			return;
		}

		if (takeaway_buy_payment_layout.getChildCount() != 0) {
			takeaway_buy_payment_layout.removeAllViews();
		}
		for (int i = 0; i < dto.list.size(); i++) {
			View view = LayoutInflater.from(TakeAwayBuyPaymentActivity.this).inflate(R.layout.takeaway_buy_payment_item, null);
			TextView takeaway_buy_payment_title = (TextView) view.findViewById(R.id.takeaway_buy_payment_title);
			TextView takeaway_buy_payment_hint = (TextView) view.findViewById(R.id.takeaway_buy_payment_hint);
			View takeaway_buy_payment_bt = view.findViewById(R.id.takeaway_buy_payment_bt);
			if (!CheckUtil.isEmpty(dto.list.get(i).title)) {
				takeaway_buy_payment_title.setText(dto.list.get(i).title);
			}
			if (!CheckUtil.isEmpty(dto.list.get(i).hint)) {
				takeaway_buy_payment_hint.setText(dto.list.get(i).hint);
			}

			final int j = i;
			takeaway_buy_payment_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(view, 1000);
					// ----------------------------
					OpenPageDataTracer.getInstance().addEvent("选择行", dto.list.get(j).typeTag+"");
					// ----------------------------
					excuteOnlinePayResultData(dto.list.get(j).typeTag,view);
				}
			});
			takeaway_buy_payment_layout.addView(view);
		}

	}

	// 获得支付参数，返回TakeoutOnlinePayResultData
	private void excuteOnlinePayResultData(final int payTypeTag,final View v) {
		ServiceRequest request = new ServiceRequest(API.getTakeoutOnlinePayResultData2);
		request.addData("orderId", orderId);// 外卖订单id
		request.addData("payTypeTag", payTypeTag);// 支付方式
		// ----------------------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// ----------------------------
		CommonTask.request(request, "", new CommonTask.TaskListener<CommonPostPayResultData>() {
			@Override
			protected void onSuccess(CommonPostPayResultData dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----
				
				if (dto == null) {
					finish();
					return;
				}

				// 数据校验是否通过
				if (dto.chkPassTag) {
					switch (payTypeTag) {
					// 类别 1:银联 (客户端)
					case 1:
						doUniPay(dto.unionPayXml);
						break;
					// 2：支付宝(客户端)
					case 2:
						doAliPay(dto.aliPayInfo);
						break;
					// 3:支付宝(wap)
					case 3:
						jumbToweb(dto.wapAliPayUrl);
						break;
					// 4:支付宝信用卡(wap)
					case 4:
						jumbToweb(dto.wapAliPayCreditCardUrl);
						break;
					// 5:微信支付
					case 5:
						doWeixinPay(dto.weixinInfo);
						break;
					// 6:快钱支付
					case 6:
						doKqPay(dto.kqInfo, v);
						break;
					// 其他wap url
					default:
					if (payTypeTag >= 100) { // 大于等于100是“其他wap支付方式”
						jumbToweb(dto.wapUrl);
					}
						break;
					}
				} else {
					DialogUtil.showAlert(TakeAwayBuyPaymentActivity.this, "提示", dto.errorHint);
				}
			}

			protected void onError(int code, String message) {
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----				
	
//				 doTest_confirm();
				DialogUtil.showToast(ContextUtil.getContext(), message);
				// svFoodCategory.setVisibility(View.GONE);
				// TakeAwayNewFoodListActivity.this.finish();
			}

//			private void doTest_confirm() {
//				String s = "{\"unionPayXml\":\"http://www.baidu.com/\",\"aliPayInfo\":\"http://www.baidu.com/\",\"wapAliPayUrl\":\"http://www.baidu.com/\",\"wapAliPayCreditCardUrl\":\"http://www.baidu.com/\",\"wapUrl\":\"http://www.baidu.com/\",\"chkPassTag\":\"true\",\"errorHint\":\"错误提示\"}";
//				TakeoutOnlinePayResultData data = JsonUtils.fromJson(s, TakeoutOnlinePayResultData.class);
//				 onSuccess(data);
//
//			}

		});
	}

	/**
	 * 银联客户端支付
	 */
	private void doUniPay(String xml) {
		mFromUnionPay = true;
		UnionpayUtils.doPay(this, xml);
	}

	/** 
	 * 支付宝客户端支付
	 */
	private void doAliPay(String orderInfo) {
		AliPayUtils.doPay(this, orderInfo, new AliPayUtils.AliPayListener() {

			@Override
			public void onPayFinish(final boolean isSuccessful, final String message) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (isSuccessful) {
							Toast.makeText(TakeAwayBuyPaymentActivity.this, message, Toast.LENGTH_SHORT).show();
							// 跳至外卖支付处理拦截页面
							gotoTakeAwayCashIntercept();
						} else {
							Toast.makeText(TakeAwayBuyPaymentActivity.this, message, Toast.LENGTH_SHORT).show();
						}
						finish();
					}
				});

			}
		});
	}
	
	/**
	 * 跳转到微信支付确认界面
	 * 
	 * @param weixinInfo
	 */
	private void doWeixinPay(String weixinInfo) {
		isJumpWeiXinPay = true;
		WeixinUtils.doPay(TakeAwayBuyPaymentActivity.this, weixinInfo);
		// gotoCashIntercept();
	}
	
	private void doKqPay(String qMoneyInfo, View view) {
		isJumpkQPay = true;
		QuickMoneyUtils.doPay(TakeAwayBuyPaymentActivity.this, TakeAwayBuyPaymentActivity.class, qMoneyInfo, view,"om.fg114.main.app.activity.takeaway.TakeAwayBuyPaymentActivity");
	}

	private void jumbToweb(String url) {
		// 跳转到WebView支付确认界面

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		takeaway_buy_payment_layout.setVisibility(View.GONE);
		webView.setVisibility(View.VISIBLE);
		webView.clearView();
		webView.loadUrl(url);
		webView.requestFocus();
		webView.requestFocusFromTouch();

		getBtnOption().setVisibility(View.INVISIBLE);

	}

	/**
	 * 跳至外卖支付处理拦截页面
	 */
	private void gotoTakeAwayCashIntercept() {
		 Bundle mBundle = new Bundle();
		 mBundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
		 ActivityUtil.jump(TakeAwayBuyPaymentActivity.this,TakeAwayBuyOrderInterceptActivity.class, 0, mBundle);

		finish();
	}

	// ----------------------------------------------------------------------
	// 返回键的控制(very important)
	// 返回按钮事件处理
	private void doBackButtonAction() {
		if (DEBUG)
			Log.d("webView.canGoBack()", webView.canGoBack() + "," + webView.getUrl());

		if (webView == null || webView.getUrl() == null) {
			this.finish();
			return;
		}
		// 目前直接结束当前页
		// this.finish();
		// --主页的uri
//		Uri baseUri = Uri.parse(startPage);
//		String baseHost = baseUri.getHost();

		// --当前页的uri
		Uri uri = Uri.parse(webView.getUrl());
		String fragment = uri.getFragment();
		String host = uri.getHost();

		String url=uri.toString();
		// 如果到了支付成功页，或者是支付首页，则直接finish；如果到了站外，则提示是否离开页面。
		// 否则，如果webView有历史记录，则返回历史记录中的上一条，
		// 如果没有历史记录，返回paymentView
		if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBuySuccKey))

		 {

			this.finish();

		} else if (!url.contains(Settings.kXmsHostKey)) {
			// 如果到了站外，提示： 注意离开支付页面可能会造成当前支付失败．您是否要离开？
			DialogUtil.showComfire(this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {

				@Override
				public void run() {
					finish();

				}
			}, "否", new Runnable() {

				@Override
				public void run() {
				}
			});
		}
//		 else if (!webView.canGoBack()
//		 || baseUri.getHost().equals(uri.getHost())
//		 && uri.getPath().equals("/coupon/buy")
//		 && "cashPay".equals(fragment)) {
//		 jumpToPaymentView();
//		 }
		else {
			webView.goBack();
		}

	}

	/**
	 * ------------------------------------------------------------------------
	 * ------- WebView 初始化
	 */
	private void initWebView() {
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);

		// 添加JS处理
		webView.addJavascriptInterface(mJavaScriptInterface, "jsinterface");
		webView.setWebViewClient(new WebViewClient() {

			/**
			 * 开始载入页面
			 */
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				if (DEBUG)
					Log.d("onPageStarted:" + TAG, view + " - " + url);
				showProgressDialog(getString(R.string.text_info_loading));

//				Uri uri = Uri.parse(url);
//				Uri baseUri = Uri.parse(startPage);
//				if (!uri.getHost().equals(baseUri.getHost())) {
//					getBtnGoBack().setText("现金券详情");
//				}
				// 如果是支付成功，则直接finish本activity
				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBuySuccKey)) {
					// 跳至外卖支付处理拦截页面
					gotoTakeAwayCashIntercept();
					finish();
				}
				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBackKey)) {

					DialogUtil.showComfire(TakeAwayBuyPaymentActivity.this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {

						@Override
						public void run() {
							finish();

						}
					}, "否", new Runnable() {

						@Override
						public void run() {
						}
					});
				}

				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
				if (DEBUG)
					Log.d(TAG + " doUpdateVisitedHistory", url);
				super.doUpdateVisitedHistory(view, url, isReload);
			}

			/**
			 * 页面载入结束
			 */
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				closeProgressDialog();
				if (DEBUG)
					Log.d(TAG + " onPageFinished", url + " title=" + webView.getTitle());

				if (currentUrl.equals(url)) {
					webView.clearHistory();
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (DEBUG)
					Log.d(TAG, "onReceivedError:" + description);
				if (DEBUG)
					Log.d(TAG, "onReceivedError:" + failingUrl);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Uri uri = Uri.parse(url);
//				Uri baseUri = Uri.parse(startPage);

				// 如果是支付成功，则直接finish本activity
				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBuySuccKey)) {
					// 跳至现金券支付处理拦截页面
					gotoTakeAwayCashIntercept();
					finish();
					return true;
				}
				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
				// （http://w.xiaomishu.com/coupon/back）
				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBackKey)) {

					DialogUtil.showComfire(TakeAwayBuyPaymentActivity.this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {

						@Override
						public void run() {
							finish();

						}
					}, "否", new Runnable() {

						@Override
						public void run() {
						}
					});
					return true;
				}

				return super.shouldOverrideUrlLoading(view, url);
			}

		});

		webView.setWebChromeClient(new WebChromeClient() {

			/**
			 * 页面关闭
			 */
			@Override
			public void onCloseWindow(WebView window) {
				super.onCloseWindow(window);
				finish();
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				if (DEBUG)
					Log.d(TAG, "alert");
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				if (DEBUG)
					Log.d(TAG, "Confirm");
				return super.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				if (DEBUG)
					Log.d(TAG, "Prompt");
				return super.onJsPrompt(view, url, message, defaultValue, result);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (DEBUG)
					Log.d(TAG + ",onReceivedTitle", "title=" + title);
				getTvTitle().setText(webView.getTitle());
			}

		});
	}
}
