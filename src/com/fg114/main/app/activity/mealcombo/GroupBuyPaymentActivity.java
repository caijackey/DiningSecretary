package com.fg114.main.app.activity.mealcombo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.alipay.AliPayUtils;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.view.DigitalSelector;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.PresentCardCheckBox;
import com.fg114.main.app.view.DigitalSelector.OnDigitChangeListener;
import com.fg114.main.app.view.PresentCardCheckBox.OnItemSelected;
import com.fg114.main.qmoney.KqActivity;
import com.fg114.main.qmoney.QuickMoneyUtils;
import com.fg114.main.service.dto.CashCouponPayData;
import com.fg114.main.service.dto.CashCouponPostResultData;
import com.fg114.main.service.dto.CommonPostPayResultData;
import com.fg114.main.service.dto.CouponOrderFormData;
import com.fg114.main.service.dto.CouponPostResultData;
import com.fg114.main.service.dto.GiftCardData;
import com.fg114.main.service.dto.PayTypeData;
import com.fg114.main.service.dto.SpecialRestData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JavaScriptInterface;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.UnionpayUtils;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.wxapi.WXPayEntryActivity;
import com.fg114.main.wxapi.WeixinUtils;

/**
 * 手机购买支付页面 说明：本页面是一个android界面和WebView页面结合的页面
 * 支付流程第一个步骤“订单确认”页面使用android方式做（因为里面的输入控制等需要android平台的兼容性） 第二步骤开始转到WebView中进行。
 * 
 * 当支付成功后，跳转至一个中间页（CashInterceptActivity）。
 * 
 * @author nieyinyin
 * @since 2013-07-31
 * 
 */
public class GroupBuyPaymentActivity extends MainFrameActivity {
	private static final String TAG = "GroupBuyPaymentActivity";
	private static final boolean DEBUG = Settings.DEBUG;
	private DecimalFormat decimalFormat = new DecimalFormat("#########.##");
	private Context ctx = GroupBuyPaymentActivity.this;

	// 传入参数获得
	private int fromPage;
	private String uuid;

	// 测试地址
	// private String startPage = "http://w.57hao.com/coupon/buy?";
	// private String startPage = Settings.PAYMENT_HOST + "/coupon/buy?";
	// private String startPage="http://w.xiaomishu.com/coupon/buy?";
	// http://w.57hao.com/coupon/buy?resid=C02C26S03727&buycount=1&typeTag=2&mobilephone=13800000000&platform=iphone&couponid=1047&screenHeight=480.000000&version=3_52_1&payamount=0.00&accountamount=11.00&orderId=223F35E14P05FE&deviceNumber=4aa0ee05f686bfb915a58a96209db66a&paybonus=6700&screenWith=320&deviceType=Simulator&appTag=bwc&token=5d14c459b0bf4d59a7850a42cb3dcdba&isTest=true#cashPay

	private View contentView;

	// 支付网页
	private WebView webView;
	private ScrollView svPaymentView;
	private View paymentView;
	private ViewGroup paymentTypeView;
	private ViewGroup paymentTypeContainer;
	// 标题
	private TextView tvTitle;
	// 购买数量
	private DigitalSelector buyNum;
	// 用户手机号
	private TextView etPhone;
	// 剩余现金券抵扣
	private TextView tvRemainingCashHint;
	private EditText etShouldPay;
	// 剩余秘币币抵扣
	private LinearLayout llRemainMibi;
	private TextView tvRemainingBWB;
	private EditText etAccountBalance;

	// 当前已经选择现金券提示
	// private TextView tvCashBuyedHint;
	private TextView tvCashBuySuccessHint;

	private TextView group_buy_payment_Hint;
	private TextView group_buy_payment_total;

	// 操作按钮
	private Button btnBuyByPhone; // 电话购买
	private Button btnSubmit; // 手机购买

	private RelativeLayout mgiftLayout;
	private PresentCardCheckBox mpresentBox;
	private TextView giftInfo;
	private LineView giftCardLine;
	private LineView mibiLine;

	private JavaScriptInterface mJavaScriptInterface = new JavaScriptInterface();

	private CouponOrderFormData data;
	private CommonPostPayResultData couponPostResult;

	// 进度提示框
	private ProgressDialog progressDialog = null;
	private boolean isWebPageLoading = true;

	// 静态值（服务端传过来的）
	private BigDecimal mUserRemainMoney = new BigDecimal(0); // 帐户余额
	private BigDecimal mOnePointPrice = new BigDecimal(0); // 秘币单价
	private int mMaxMibiCount; // 最大可使用的秘币币数量
	private TextView cash_buy_payment_price;// 单价
	private BigDecimal mdeliverPrice = new BigDecimal(12); // 运费金额
	private BigDecimal totalPrice = new BigDecimal(0);// 总计 =单价*数量

	// 动态值 （根据现金券的数量而变化的）
	private BigDecimal mShouldPay = new BigDecimal(0); // 应付金额
	private BigDecimal mPayFromAccount = new BigDecimal(0); // 使用账户余额支付的金额
	private BigDecimal mActualPay = new BigDecimal(0); // 实际支付金额
	private int mMibiCountUsed; // 使用的秘币
	private BigDecimal mUnitPrice = new BigDecimal(0); // 优惠券单价
	// private BigDecimal couponValue = new BigDecimal(0); // 现金券面额
	private BigDecimal mAllCouponValue = new BigDecimal(0); // 所购买现金券的总面额
	private BigDecimal mGiftCardPrice = new BigDecimal(0); // 礼品卡单价
	private BigDecimal mDeductionPrice = new BigDecimal(0); // 抵扣金额
	private String mGiftCardId = "";
	private String currentUrl = "";
	private boolean mFromUnionPay; // 是否曾跳转过银联支付
	private boolean isJumpWeiXinPay = false;// 是否曾跳转过微信支付
	private boolean isJumpkQPay = false;// 是否曾跳转过快钱支付
	private String orderId;
	// private String restId;
	private List<GiftCardData> cardlist;

	// 收货地址
	private TextView receiver_panel_tvtitle;
	private View receiver_panel_layout;
	private EditText receiver_panel_name_ed;
	private EditText receiver_panel_phone_ed;
	private EditText receiver_panel_address_ed;
	private EditText receiver_panel_hint_ed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		uuid = bundle.getString(Settings.UUID);
		// restId = bundle.getString(Settings.BUNDLE_REST_ID);

		// 设置返回页
		this.setResult(fromPage);

		initComponent();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购表单", uuid);
		// ----------------------------

		// 获取页面信息数据
		executeGetBillPayDataTask();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("团购表单", uuid);
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
					// 跳至现金券支付处理拦截页面
					gotoCashIntercept();
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
				gotoCashIntercept();
			} else if (WXPayResult == 2) {
				DialogUtil.showToast(this, "支付已取消");
			} else {
				DialogUtil.showToast(this, "支付失败");
			}
			finish();

		}
	}

	/**
	 * 接收回调
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
					gotoCashIntercept();
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

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("下单页");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.getBtnOption().setText("确认订单");

		// 界面组件
		contentView = View.inflate(this, R.layout.cash_buy_payment, null);

		// ----
		webView = (WebView) contentView.findViewById(R.id.webView);
		svPaymentView = (ScrollView) contentView.findViewById(R.id.svPaymentView);
		paymentView = contentView.findViewById(R.id.paymentView);
		paymentTypeView = (ViewGroup) contentView.findViewById(R.id.paymentTypeView);
		paymentTypeContainer = (ViewGroup) contentView.findViewById(R.id.paymentTypeContainer);

		// ----
		tvTitle = (TextView) contentView.findViewById(R.id.cash_buy_payment_tvTitle);

		// ----
		cash_buy_payment_price = (TextView) contentView.findViewById(R.id.cash_buy_payment_price);
		buyNum = (DigitalSelector) contentView.findViewById(R.id.cash_buy_payment_buyNum);
		etPhone = (TextView) contentView.findViewById(R.id.cash_buy_payment_etPhone);

		// ----
		tvRemainingCashHint = (TextView) contentView.findViewById(R.id.cash_buy_payment_tvRemainingCashHint);
		etShouldPay = (EditText) contentView.findViewById(R.id.cash_buy_payment_etShouldPay);

		// ----
		llRemainMibi = (LinearLayout) contentView.findViewById(R.id.cash_buy_payment_llRemainMibi);
		tvRemainingBWB = (TextView) contentView.findViewById(R.id.cash_buy_payment_tvRemainingMibi);
		etAccountBalance = (EditText) contentView.findViewById(R.id.cash_buy_payment_etAccountBalance);

		// ----
		// tvCashBuyedHint = (TextView)
		// contentView.findViewById(R.id.cash_buy_payment_tvHint);
		tvCashBuySuccessHint = (TextView) contentView.findViewById(R.id.cash_buy_payment_tvCashBuyHint);

		group_buy_payment_Hint = (TextView) contentView.findViewById(R.id.group_buy_payment_Hint);
		group_buy_payment_total = (TextView) contentView.findViewById(R.id.group_buy_payment_total);
		// ----
		btnBuyByPhone = (Button) contentView.findViewById(R.id.btnBuyByPhone);
		btnSubmit = (Button) contentView.findViewById(R.id.btnSubmit);

		// ----
		btnSubmit.setEnabled(false);
		svPaymentView.setVisibility(View.GONE);
		paymentTypeView.setVisibility(View.GONE);
		ViewUtils.setClearable(etPhone);

		mgiftLayout = (RelativeLayout) contentView.findViewById(R.id.cash_buy_payment_llGiftCard);
		mpresentBox = (PresentCardCheckBox) contentView.findViewById(R.id.cash_buy_payment_llGiftbox);
		giftInfo = (TextView) contentView.findViewById(R.id.cash_buy_payment_llGiftinfo);

		giftCardLine = (LineView) contentView.findViewById(R.id.giftCardLine);
		mibiLine = (LineView) contentView.findViewById(R.id.mibiLine);

		// 收货地址
		receiver_panel_tvtitle = (TextView) contentView.findViewById(R.id.receiver_panel_tvtitle);
		receiver_panel_layout = (View) contentView.findViewById(R.id.receiver_panel_layout);
		receiver_panel_name_ed = (EditText) contentView.findViewById(R.id.receiver_panel_name_ed);
		receiver_panel_phone_ed = (EditText) contentView.findViewById(R.id.receiver_panel_phone_ed);
		receiver_panel_address_ed = (EditText) contentView.findViewById(R.id.receiver_panel_address_ed);
		receiver_panel_hint_ed = (EditText) contentView.findViewById(R.id.receiver_panel_hint_ed);

		// 绑定监听到View上
		bindListener2View();

		// webView设置
		initWebView();

		// ---
		this.getMainLayout().addView(contentView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 绑定监听到控件上
	 */
	private void bindListener2View() {
		// 返回按钮事件
		this.getBtnGoBack().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				doBackButtonAction();
			}
		});

		// “确定订单”(the same to the btnSubmit)
		this.getBtnOption().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----

				btnSubmit.performClick();
			}
		});

		mpresentBox.setOnItemSelectedListener(new OnItemSelected() {

			@Override
			public void onSelected(int position, boolean isSelected) {

				// 将已经输入的秘币数量清零
				// mBWBCountUsed = 0;
				// inputComplete(0);
				if (isSelected) {
					mGiftCardPrice = BigDecimal.valueOf(cardlist.get(position).getPrice());
					mGiftCardId = cardlist.get(position).getUuid();
				} else {
					mGiftCardPrice = BigDecimal.valueOf(0);
					mGiftCardId = "";
				}
				doChangeByCard();

			}
		});
		// “手机购买”
		btnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (checkOrder()) {
					try {
						// -----
						OpenPageDataTracer.getInstance().addEvent("手机购买按钮");
						// -----

						// 收起键盘
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);

						// ----
						if (((int) (mActualPay.doubleValue() * 100)) == 0) {
							executePostCouponTask(0, v); // 实际支付为0，表示直接用余额支付
						} else {
							jumpToPaymentType(); // 选择支付方式
						}
						// jumpToPaymentView();
					} catch (Exception e) {

					}
				}
			}

		});

		btnBuyByPhone.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);

				// -----
				OpenPageDataTracer.getInstance().addEvent("电话购买按钮");
				// -----

				ActivityUtil.callSuper57(ctx, "57575777");
				// -----
				new Runnable() {
					public void run() {
						try {
							ServiceRequest.callTel(4, uuid, "57575777");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.run();
			}
		});

		buyNum.setOnDigitChangeListener(new OnDigitChangeListener() {

			@Override
			public void onChange(DigitalSelector selector, int digit, int previousValue) {
				// 刷新界面数据
				doInit();
			}
		});

		// // TODO:
		// etPhone.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start, int before, int
		// count) {
		//
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start, int count,
		// int after) {
		//
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// if (s.toString().length() == 11) {
		// tvCashBuySuccessHint.setText(String.format(getString(R.string.text_input_cash_buy_success),
		// s.toString()));
		// }
		// }
		// });

		etShouldPay.addTextChangedListener(new TextWatcher() {
			private String oldValue;

			@Override
			public void onTextChanged(CharSequence s, int i, int j, int k) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int i, int j, int k) {
				oldValue = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					if (etShouldPay.isEnabled() && oldValue != null && !oldValue.equals(s.toString())) {

						String newValue = s.toString();
						if (TextUtils.isEmpty(newValue)) {
							newValue = "0.0";
						}
						if (TextUtils.isEmpty(oldValue)) {
							oldValue = "0.0";
						}
						if (new BigDecimal(oldValue).compareTo(new BigDecimal(newValue)) == 0) {
							return; // 界面无需任何变化
						}

						doChangeByPayFromAccount(oldValue, s.toString());

						if (Settings.DEBUG)
							Log.e(TAG, "etShouldPay change =======================================");
					}
				} catch (Exception e) {
					doInit();
				}
			}
		});

		etAccountBalance.addTextChangedListener(new TextWatcher() {
			private String oldValue;

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				oldValue = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					if (etAccountBalance.isEnabled() && oldValue != null && !oldValue.equals(s.toString())) {

						String newValue = s.toString();
						if (TextUtils.isEmpty(newValue)) {
							newValue = "0.0";
						}
						if (TextUtils.isEmpty(oldValue)) {
							oldValue = "0.0";
						}
						if (new BigDecimal(oldValue).compareTo(new BigDecimal(newValue)) == 0) { // 两次值相等，则无需刷新
							return; // 界面无需任何变化
						}
						doChangeByBWBUsed(oldValue, s.toString());
						if (Settings.DEBUG)
							Log.e(TAG, "etAccountBalance change =======================================");
					}
				} catch (Exception e) {
					doInit();
				}
			}
		});
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

				Uri uri = Uri.parse(url);
				// Uri baseUri = Uri.parse(startPage);
				if (!url.contains(Settings.kXmsHostKey)) {
					getBtnGoBack().setText("现金券详情");
				}
				// 如果是支付成功，则直接finish本activity
				if (url.contains(Settings.kXmsHostKey) && url.contains(Settings.kBuySuccKey)) {
					// 跳至现金券支付处理拦截页面
					gotoCashIntercept();
					finish();
				}
				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
				// （http://w.xiaomishu.com/coupon/back）
				if (url.contains(Settings.kXmsHostKey) && url.contains(Settings.kBackKey)) {

					DialogUtil.showComfire(GroupBuyPaymentActivity.this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {

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
				// 一旦到了站外，返回按钮变成“现金券详情”
				Uri uri = Uri.parse(url);
				// Uri baseUri = Uri.parse(startPage);

				if (!url.contains(Settings.kXmsHostKey)) {
					getBtnGoBack().setText("现金券详情");
				}
				// 如果是支付成功，则直接finish本activity
				if (url.contains(Settings.kXmsHostKey) && url.contains(Settings.kBuySuccKey)) {
					// 跳至现金券支付处理拦截页面
					gotoCashIntercept();
					finish();
					return true;
				}
				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
				// （http://w.xiaomishu.com/coupon/back）
				if (url.contains(Settings.kXmsHostKey) && url.contains(Settings.kBackKey)) {

					DialogUtil.showComfire(GroupBuyPaymentActivity.this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {

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
		// Uri baseUri = Uri.parse(startPage);
		// String baseHost = baseUri.getHost();

		// --当前页的uri
		Uri uri = Uri.parse(webView.getUrl());
		String fragment = uri.getFragment();
		String host = uri.getHost();

		String uriString = uri.toString();
		// 如果到了支付成功页，或者是支付首页，则直接finish；如果到了站外，则提示是否离开页面。
		// 否则，如果webView有历史记录，则返回历史记录中的上一条，
		// 如果没有历史记录，返回paymentView
		if (uriString.contains(Settings.kXmsHostKey) && uriString.contains(Settings.kBuySuccKey) || paymentView.getVisibility() == View.VISIBLE

		) {

			this.finish();

		} else if (!uriString.contains(Settings.kXmsHostKey)) {
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
		} else if (!webView.canGoBack() || uriString.contains(Settings.kXmsHostKey) && uriString.contains(Settings.kStartKey)) {
			jumpToPaymentView();
		} else {
			webView.goBack();
		}

	}

	// ------------------------------------------------------------- 画面之间的切换
	// 返回到到支付方式选择界面
	private void jumpToPaymentType() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		svPaymentView.setVisibility(View.GONE);
		paymentView.setVisibility(View.GONE);
		paymentTypeView.setVisibility(View.VISIBLE);
		webView.setVisibility(View.GONE);
		getBtnOption().setVisibility(View.INVISIBLE);
	}

	// 跳转到WebView支付确认界面
	private void jumpToWebView(String url) {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		svPaymentView.setVisibility(View.GONE);
		paymentView.setVisibility(View.GONE);
		paymentTypeView.setVisibility(View.GONE);
		webView.setVisibility(View.VISIBLE);
		webView.clearView();
		webView.loadUrl(url);
		webView.requestFocus();
		webView.requestFocusFromTouch();

		getBtnOption().setVisibility(View.INVISIBLE);
	}

	// 跳转到paymentView支付定单页面
	private void jumpToPaymentView() {
		svPaymentView.setVisibility(View.VISIBLE);
		paymentTypeView.setVisibility(View.GONE);
		paymentView.setVisibility(View.VISIBLE);
		webView.setVisibility(View.GONE);
		this.getTvTitle().setText("填写现金券购买信息");
		svPaymentView.requestFocus();
		svPaymentView.requestFocusFromTouch();
	}

	// ---------------------------------------------------------------------------
	// 页面工具方法
	// 输入完成
	private void inputComplete(final double BWBCount) {

		// 开新线程置空，防止HTC G7程序崩溃的bug
		etAccountBalance.post(new Runnable() {
			@Override
			public void run() {
				try {
					etAccountBalance.setText("" + BWBCount);
					etAccountBalance.setSelection(etAccountBalance.getText().length());
				} catch (Exception e) {

				}
			}
		});
	}

	// 返回BWB框的合法数字值
	public int getValidBWB() {

		int mBWBCount = 0;
		try {
			mBWBCount = new BigDecimal(etAccountBalance.getText().toString()).divide(mOnePointPrice).intValue();
			if (mBWBCount > mMaxMibiCount) {
				mBWBCount = mMaxMibiCount;
			}
		} catch (Exception e) {
			mBWBCount = 0;
		}
		return mBWBCount;

	}

	// 检查支付定单数据是否合法
	private boolean checkOrder() {
		// 手机号
		if (etPhone.getText().toString().length() != 11) {
			DialogUtil.showToast(this, "请输入正确的手机号！");
			return false;
		}
		// 秘币不能超过用户拥有的数量
		if (data.userPointNum < getValidBWB()) {
			DialogUtil.showToast(this, "您没有这么多秘币币！请重新输入！");
			return false;
		}
		return true;
	}

	// -------------------------------------------------------------------
	// 页面请求Task
	/**
	 * 获取页面信息Task
	 */
	private void executeGetBillPayDataTask() {
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		ServiceRequest request = new ServiceRequest(API.getCouponOrderFormInfo);
		request.addData("uuid", uuid);

		CommonTask.request(request, new CommonTask.TaskListener<CouponOrderFormData>() {
			@Override
			protected void onSuccess(CouponOrderFormData dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				if (dto == null) {
					finish();
					return;
				}
				//
				data = dto;
				/*------------------------------------------------------------
				  					测试数据开始
				--------------------------------------------------------------*/
				// GiftCardData card0 = new GiftCardData();
				// card0.setName("礼品卡0--价格:1");
				// card0.setPrice(1);
				// card0.setUuid(UUID.randomUUID().toString());
				//
				// GiftCardData card1 = new GiftCardData();
				// card1.setName("礼品卡1--价格:10");
				// card1.setPrice(10);
				// card1.setUuid(UUID.randomUUID().toString());
				//
				// GiftCardData card2 = new GiftCardData();
				// card2.setName("礼品卡2--价格:20");
				// card2.setPrice(20);
				// card2.setUuid(UUID.randomUUID().toString());
				//
				// List<GiftCardData> giftCardDatas = new
				// ArrayList<GiftCardData>();
				// giftCardDatas.add(card0);
				// giftCardDatas.add(card1);
				// giftCardDatas.add(card2);
				//
				// data.cardList = giftCardDatas;
				//
				// data.bookTel = "15618290761";
				// data.userRemainMoney = 80.0;
				// ---------------------------------------------------------------------------测试数据结束

				mOnePointPrice = BigDecimal.valueOf(data.onePointPrice);
				if (data.userPointNum < 0) {
					data.userPointNum = 0;
				}
				mUserRemainMoney = BigDecimal.valueOf(data.userRemainMoney);
				mMaxMibiCount = data.userPointNum;
				mUnitPrice = BigDecimal.valueOf(data.unitPrice);
				// couponValue = BigDecimal.valueOf(data.couponValue);
				mdeliverPrice = BigDecimal.valueOf(data.deliverPrice);
				setView(data);
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				finish();
			}
		});
	}

	// 获取支付数据
	private void executePostCouponTask(final int payTypeTag, final View v) {
		ServiceRequest request = new ServiceRequest(API.postCouponOrder2);
		request.addData("uuid", uuid); // 团购id
		request.addData("payTypeTag", payTypeTag); // 支付方式 0:直接余额支付 其他：银行卡
		request.addData("buyNum", String.valueOf(buyNum.getValue())); // 购买数量
		request.addData("userTel", etPhone.getText().toString()); // 手机号码
		request.addData("usedRemainMoney", mPayFromAccount.doubleValue()); // 使用的账户余额
		request.addData("usedPointNum", getValidBWB()); // 使用的积分数量
		request.addData("actualPay", mActualPay.doubleValue()); // 实际支付金额 格式
																// 12.12
		request.addData("cardId", mGiftCardId); // 礼品卡id

		if (data.showReceiverPanel) {
			if (CheckUtil.isEmpty(receiver_panel_name_ed.getText().toString())) {
				DialogUtil.showToast(ctx, "姓名不能为空");
				return;
			}
			if (CheckUtil.isEmpty(receiver_panel_phone_ed.getText().toString())) {
				DialogUtil.showToast(ctx, "手机号不能为空");
				return;
			}
			if (CheckUtil.isEmpty(receiver_panel_address_ed.getText().toString())) {
				DialogUtil.showToast(ctx, "地址不能为空");
				return;
			}
		}
		request.addData("receiverName", receiver_panel_name_ed.getText().toString()); // 收货人姓名
		request.addData("receiverTel", receiver_panel_phone_ed.getText().toString()); // 收货人手机号
		request.addData("receiverAddress", receiver_panel_address_ed.getText().toString()); // 收货人地址
		request.addData("receiverMemo", receiver_panel_hint_ed.getText().toString()); // 收货人备注

		request.addData("forTestTag", Settings.DEBUG); // 是否是测试
		// request.setCanUsePost(true);
		CommonTask.request(request, new CommonTask.TaskListener<CommonPostPayResultData>() {
			@Override
			protected void onSuccess(CommonPostPayResultData dto) {
				if (dto == null) {
					finish();
					return;
				}
				couponPostResult = dto;
				if (!couponPostResult.chkPassTag) {
					DialogUtil.showAlert(ctx, "提示", CheckUtil.isEmpty(couponPostResult.errorHint) ? "暂时无法使用此种支付方式" : couponPostResult.errorHint);
					return;
				}
				// 类别 0：直接余额支付 1:银联 (客户端) 2：支付宝(客户端) 3:支付宝(wap) 4:支付宝信用卡(wap)//
				// 5:微信支付
				switch (payTypeTag) {
				case 0:
					doDirectPay(couponPostResult);
					break;
				case 1:
					doUniPay(couponPostResult.unionPayXml);
					break;
				case 2:
					doAliPay(couponPostResult.aliPayInfo);
					break;
				case 3:
					jumpToWebView(couponPostResult.wapAliPayUrl);
					break;
				case 4:
					jumpToWebView(couponPostResult.wapAliPayCreditCardUrl);
					break;
				// 5:微信支付
				case 5:
					doWeixinPay(couponPostResult.weixinInfo);
					break;
				// 快钱支付
				case 6:
					doKqPay(couponPostResult.kqInfo, v);
				default:
					if (payTypeTag >= 100) { // 大于等于100是“其他wap支付方式”
						jumpToWebView(couponPostResult.wapAliPayUrl);
					}
					break;
				}
			}
		});
	}

	// ----------------------------------------------------------------------------
	// 页面渲染方法
	// 将数据设置到定单界面中
	private void setView(CouponOrderFormData data) {
		svPaymentView.setVisibility(View.VISIBLE);
		paymentTypeView.setVisibility(View.GONE);
		buyNum.setEnabled(true);
		btnSubmit.setEnabled(true);
		tvTitle.setText(data.title);
		buyNum.setValue(1);
		buyNum.setMinValue(1);
		buyNum.setMaxValue(data.maxBuyNum);

		cash_buy_payment_price.setText(data.unitPrice + "元");

		String phone = data.defaultUserTel;
		if (TextUtils.isEmpty(phone)) {
			phone = SessionManager.getInstance().getUserInfo(this).getTel();
		}
		etPhone.setText(phone);
		tvCashBuySuccessHint.setText(String.format(this.getString(R.string.text_input_cash_buy_success), TextUtils.isEmpty(phone) ? "" : phone));
		tvRemainingCashHint.setText(String.format(this.getString(R.string.text_input_remaining_cash_hint), String.valueOf(data.userRemainMoney)));
		tvRemainingBWB.setText(String.format(this.getString(R.string.text_input_remaining_bwb_hint), String.valueOf(data.userPointNum)));

		// 控制“mibi布局”按钮的显隐
		if (data.userPointNum != 0) {
			llRemainMibi.setVisibility(View.VISIBLE);
		} else {
			llRemainMibi.setVisibility(View.GONE);
			mibiLine.setVisibility(View.GONE);
		}
		// 控制“电话购买”按钮的显隐
		if (data.showTelBtnTag) {
			btnBuyByPhone.setVisibility(View.VISIBLE);
		} else {
			btnBuyByPhone.setVisibility(View.GONE);
		}

		// 是否显示下单按钮
		if (data.showOrderBtnTag) {
			btnSubmit.setVisibility(View.VISIBLE);
		} else {
			btnSubmit.setVisibility(View.GONE);
		}
		// 是否显示收货信息面板
		if (data.showReceiverPanel) {
			receiver_panel_tvtitle.setVisibility(View.VISIBLE);
			receiver_panel_layout.setVisibility(View.VISIBLE);
		} else {
			receiver_panel_tvtitle.setVisibility(View.GONE);
			receiver_panel_layout.setVisibility(View.GONE);
		}
		if (!CheckUtil.isEmpty(data.defaultReceiverName)) {
			receiver_panel_name_ed.setText(data.defaultReceiverName);
		}
		if (!CheckUtil.isEmpty(data.defaultReceiverTel)) {
			receiver_panel_phone_ed.setText(data.defaultReceiverTel);
		}
		if (!CheckUtil.isEmpty(data.defaultReceiverAddress)) {
			receiver_panel_address_ed.setText(data.defaultReceiverAddress);
		}
		// 初始化礼品卡信息
		initGiftCardInfo();

		buildPaymentTypeItems(paymentTypeContainer, data.payTypeList);

		// 动态设置一些值
		doInit();
	}

	/**
	 * 初始化礼品卡
	 */
	private void initGiftCardInfo() {
		if (data != null && data.cardList != null && data.cardList.size() != 0) {
			mpresentBox.setVisibility(View.VISIBLE);
			mgiftLayout.setVisibility(View.VISIBLE);
			cardlist = data.cardList;
			List<String> namelist = new ArrayList<String>();
			for (int i = 0; i < cardlist.size(); i++) {
				namelist.add(cardlist.get(i).getName());
			}
			mpresentBox.setNameList(namelist);

		} else {
			mgiftLayout.setVisibility(View.GONE);
			mpresentBox.setVisibility(View.GONE);
			giftCardLine.setVisibility(View.GONE);
		}
	}

	// ---------------------------------------------------------------------------------
	// 页面数据联动计算
	/**
	 * 优先使用账户余额支付
	 * 
	 * 初始化界面的时候，默认不选中礼品卡；当用户选中礼品卡时，重新计算账户余额和秘币
	 */

	/**
	 * 初次进入页面，初始化数据
	 */
	void doInit() {
		try {
			// 抵扣余额至0
			mDeductionPrice = BigDecimal.valueOf(0);
			// 计算总计
			totalPrice = mUnitPrice.multiply(BigDecimal.valueOf(buyNum.getValue()));
			// 计算购买现金券的所有总面额
			// mAllCouponValue =
			// couponValue.multiply(BigDecimal.valueOf(buyNum.getValue()));
			// 购买的现金券，总共需要支付的 RMB(实际售价)
			mShouldPay = mUnitPrice.multiply(BigDecimal.valueOf(buyNum.getValue())).add(mdeliverPrice);
			// 减去账户余额，还需要支付的RMB
			BigDecimal mStillNeedToPay = mShouldPay.subtract(mUserRemainMoney);

			// Log.v("TAG",
			// mDeductionPrice.doubleValue()+"=mDeductionPrice"+mUserRemainMoney.doubleValue()+"=mUserRemainMoney");

			if (mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == -1 || mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == 0) {// 用户余额足够，直接用余额支付
				mPayFromAccount = mShouldPay;
				mMibiCountUsed = 0;
				mActualPay = BigDecimal.valueOf(0);
				// 计算抵扣金额
				mDeductionPrice = mDeductionPrice.add(mPayFromAccount);

			} else { // 不够支付，使用秘币币支付
				mPayFromAccount = mUserRemainMoney;
				mMaxMibiCount = data.userPointNum;
				int tempBWB = mStillNeedToPay.divide(mOnePointPrice).intValue();
				if (tempBWB <= mMaxMibiCount) { // 需要的秘币币小于或等于最大的秘币币时
					mMibiCountUsed = tempBWB;

					BigDecimal BWB2RMB = mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)); // 当前需要使用的BWB转为人民币
					mDeductionPrice = mDeductionPrice.add(BWB2RMB);// 抵扣余额
																	// 余额和秘币相加抵扣金额
					mActualPay = BigDecimal.valueOf(0);

				} else { // 账户剩余BWB数量不足以支付剩余价格
					mMibiCountUsed = mMaxMibiCount;
					BigDecimal BWB2RMB = mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)); // 当前需要使用的BWB转为人民币
					mDeductionPrice = mDeductionPrice.add(BWB2RMB);// 抵扣余额
																	// 余额和秘币相加抵扣金额
					mActualPay = mStillNeedToPay.subtract(BWB2RMB); // 剩余现金和BWB抵扣完成后，还需要实际支付的现金
				}
			}

			// 根据现金券的数量而变化，动态设置“需要支付的账户余额，需要支付的BWB数量，以及现金购买提示”
			etShouldPay.setText(String.valueOf(decimalFormat.format(mPayFromAccount.doubleValue())));
			etAccountBalance.setText(String.valueOf(mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed))));
			// tvCashBuyedHint.setText(Html.fromHtml(
			// String.format
			// (
			// this.getString(R.string.text_input_cash_hint),
			// String.valueOf(decimalFormat.format(mAllCouponValue.doubleValue())),
			// String.valueOf(decimalFormat.format(mActualPay.doubleValue()))
			// )
			// )

			if (mdeliverPrice.doubleValue() != 0) {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "+运费" + String.valueOf(decimalFormat.format(mdeliverPrice.doubleValue())) + "-抵扣"
								+ String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			} else {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "-抵扣" + String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			}
			group_buy_payment_total.setText(Html.fromHtml("实际需支付：<font color=\"#FF6666\">" + String.valueOf(decimalFormat.format(mActualPay.doubleValue())) + "元</font>"));
		} catch (Exception e) {
			ActivityUtil.saveException(e, "GroupBuyPaymentActivity updateData error 1");
			Log.e(TAG, "", e);
		}
	}

	/**
	 * 因为所使用的账户余额变换，而变换BWB的数量，以及刷新界面
	 */
	void doChangeByPayFromAccount(String oldValue, String newValue) {
		try {
			// 抵扣余额至0
			mDeductionPrice = BigDecimal.valueOf(0);
			// 计算购买现金券的所有总面额
			// mAllCouponValue =
			// couponValue.multiply(BigDecimal.valueOf(buyNum.getValue()));
			// 购买的现金券，总共需要支付的 RMB
			mShouldPay = mUnitPrice.multiply(BigDecimal.valueOf(buyNum.getValue())).add(mdeliverPrice);
			mShouldPay = mShouldPay.subtract(mGiftCardPrice);
			if (TextUtils.isEmpty(newValue)) {
				newValue = "0.0";
			}
			mPayFromAccount = new BigDecimal(newValue); // 用户输入
			if (mPayFromAccount.compareTo(mUserRemainMoney) == 1) {
				mPayFromAccount = mUserRemainMoney;
			}
			BigDecimal mStillNeedToPay = mShouldPay.subtract(mPayFromAccount);
			mDeductionPrice = mDeductionPrice.add(mPayFromAccount);// 抵扣金额=余额抵扣金额
			if (mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == -1 || mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == 0) {// 用户输入足够，直接支付
				mMibiCountUsed = 0;
				mPayFromAccount = mShouldPay;
				mActualPay = BigDecimal.valueOf(0);
			} else { // 不够支付，使用秘币币支付
				mMaxMibiCount = data.userPointNum;
				int tempBWB = mStillNeedToPay.divide(mOnePointPrice).intValue();
				if (tempBWB <= mMaxMibiCount) { // 需要的秘币币小于或等于最大的秘币币时
					mMibiCountUsed = tempBWB;

					BigDecimal BWB2RMB = mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)); // 当前需要使用的BWB转为人民币
					mDeductionPrice = mDeductionPrice.add(BWB2RMB);//
					mActualPay = BigDecimal.valueOf(0);

				} else { // 账户剩余BWB数量不足以支付剩余价格
					mMibiCountUsed = mMaxMibiCount;
					BigDecimal BWB2RMB = mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)); // 当前需要使用的BWB转为人民币
					mDeductionPrice = mDeductionPrice.add(BWB2RMB);// //抵扣金额=余额抵扣金额+秘币抵扣金额
					mActualPay = mStillNeedToPay.subtract(BWB2RMB); // 剩余现金和BWB抵扣完成后，还需要实际支付的现金
				}
			}

			if (mPayFromAccount.doubleValue() < 0) {
				mPayFromAccount = new BigDecimal(0);
			}
			etShouldPay.setText(String.valueOf(mPayFromAccount.doubleValue()));
			etShouldPay.setSelection(etShouldPay.getText().length());

			String content = String.valueOf(mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)));
			etAccountBalance.setText(content);
			etAccountBalance.setSelection(etAccountBalance.getText().length());
			// tvCashBuyedHint.setText(Html.fromHtml(String.format(this.getString(R.string.text_input_cash_hint),
			// String.valueOf(decimalFormat.format(mAllCouponValue.doubleValue())),
			// String.valueOf(decimalFormat.format(mActualPay.doubleValue())))));

			if (mdeliverPrice.doubleValue() != 0) {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "+运费" + String.valueOf(decimalFormat.format(mdeliverPrice.doubleValue())) + "-抵扣"
								+ String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			} else {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "-抵扣" + String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			}
			group_buy_payment_total.setText(Html.fromHtml("实际需支付：<font color=\"#FF6666\">" + String.valueOf(decimalFormat.format(mActualPay.doubleValue())) + "元</font>"));

		} catch (Exception e) {
			ActivityUtil.saveException(e, "GroupBuyPaymentActivity updateData error 2");
			Log.e(TAG, "", e);
			doInit();
		}
	}

	/**
	 * 因为BWB的数量变化而刷新界面
	 */
	void doChangeByBWBUsed(String oldValue, String newValue) {
		try {
			String shouldPayStr = etShouldPay.getText().toString();
			// 抵扣余额至0
			mDeductionPrice = BigDecimal.valueOf(0);
			if (TextUtils.isEmpty(shouldPayStr)) {
				shouldPayStr = "0";
			}
			mPayFromAccount = new BigDecimal(shouldPayStr); // 用户输入
			if (mPayFromAccount.compareTo(mShouldPay) == -1) {
				mMibiCountUsed = getValidBWB();
				if (mMibiCountUsed >= mMaxMibiCount) {
					mMibiCountUsed = mMaxMibiCount;
				}
				mActualPay = mShouldPay.subtract(mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed))).subtract(mPayFromAccount);
				mDeductionPrice = mDeductionPrice.add(mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed))).add(mPayFromAccount);// 抵扣金额=余额抵扣金额+秘币抵扣金额
				if (mActualPay.compareTo(new BigDecimal(0)) == -1) {
					mMibiCountUsed = mShouldPay.subtract(mPayFromAccount).divide(mOnePointPrice).intValue();
					mActualPay = new BigDecimal(0);
				}
			} else {
				mMibiCountUsed = 0;
				mActualPay = mActualPay.multiply(BigDecimal.valueOf(0.0));
			}

			// ----
			if (!TextUtils.isEmpty(newValue)) { // 用户输入不为空
				String content = String.valueOf(mOnePointPrice.multiply(BigDecimal.valueOf(mMibiCountUsed)).doubleValue());
				etAccountBalance.setText(content);
				etAccountBalance.setSelection(etAccountBalance.getText().length());
			}
			// tvCashBuyedHint.setText(Html.fromHtml(String.format(this.getString(R.string.text_input_cash_hint),
			// String.valueOf(decimalFormat.format(mAllCouponValue.doubleValue())),
			// String.valueOf(decimalFormat.format(mActualPay.doubleValue())))));

			if (mdeliverPrice.doubleValue() != 0) {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "+运费" + String.valueOf(decimalFormat.format(mdeliverPrice.doubleValue())) + "-抵扣"
								+ String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			} else {
				group_buy_payment_Hint.setText(Html.fromHtml("总计"
						+ String.valueOf(decimalFormat.format(totalPrice.doubleValue()) + "-抵扣" + String.valueOf(decimalFormat.format(mDeductionPrice.doubleValue())))));
			}
			group_buy_payment_total.setText(Html.fromHtml("实际需支付：<font color=\"#FF6666\">" + String.valueOf(decimalFormat.format(mActualPay.doubleValue())) + "元</font>"));
		} catch (Exception e) {
			ActivityUtil.saveException(e, "GroupBuyPaymentActivity updateData error 3");
			Log.e(TAG, "", e);
			doInit();
		}
	}

	/**
	 * 因礼品卡被选中变化而刷新界面
	 */
	void doChangeByCard() {
		// mGiftCardPrice;
		try {
			// 计算购买现金券的所有总面额
			// mAllCouponValue =
			// couponValue.multiply(BigDecimal.valueOf(buyNum.getValue()));
			// 购买的现金券，总共需要支付的 RMB(实际售价)
			mShouldPay = mUnitPrice.multiply(BigDecimal.valueOf(buyNum.getValue())).add(mdeliverPrice);
			// 减去所选礼品卡金额，还需要支付的RMB
			BigDecimal mStillNeedToPay = mShouldPay.subtract(mGiftCardPrice);
			if (mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == -1 || mStillNeedToPay.compareTo(BigDecimal.valueOf(0)) == 0) {// 礼品卡足够支付，直接支付
				// mBWBCountUsed = 0;
				// mPayFromAccount = mActualPay = BigDecimal.valueOf(0);
				etShouldPay.setText("");
			} else {
				etShouldPay.setText(String.valueOf(mStillNeedToPay.doubleValue()));
			}

		} catch (Exception e) {
			Log.e(TAG, "", e);
			doInit();
		}

	}

	// ---------------------------------------------------------------------
	// 不同支付方式处理
	/**
	 * 银联客户端支付
	 */
	private void doDirectPay(CommonPostPayResultData result) {
		Toast.makeText(ctx, "支付成功", Toast.LENGTH_SHORT).show();
		// 跳至现金券支付处理拦截页面
		gotoCashIntercept();
	}

	/**
	 * 银联客户端支付
	 */
	private void doUniPay(String xml) {
		mFromUnionPay = true;
		UnionpayUtils.doPay(this, xml);
		// UnionpayUtils.doPay(this, UnionpayUtils.testXml);
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
							Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
							// 跳至现金券支付处理拦截页面
							gotoCashIntercept();
						} else {
							Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
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
		WeixinUtils.doPay(GroupBuyPaymentActivity.this, weixinInfo);
		// gotoCashIntercept();

	}

	private void doKqPay(String qMoneyInfo, View view) {
		isJumpkQPay = true;
		QuickMoneyUtils.doPay(GroupBuyPaymentActivity.this, GroupBuyPaymentActivity.class, qMoneyInfo, view,"com.fg114.main.app.activity.mealcombo.GroupBuyPaymentActivity");
	}

	/**
	 * 构建出所有的支付方式
	 */
	void buildPaymentTypeItems(ViewGroup vg, List<PayTypeData> payTypeList) {

		vg.removeAllViews();
		if (vg == null || payTypeList == null || payTypeList.size() == 0) {
			return;
		}
		View lastTypeItemLine = null;
		// list_item_payment_type
		for (PayTypeData type : payTypeList) {
			ViewGroup typeItem = (ViewGroup) View.inflate(this, R.layout.list_item_payment_type, null);
			vg.addView(typeItem);
			TextView title = (TextView) typeItem.findViewById(R.id.payment_type_title);
			TextView description = (TextView) typeItem.findViewById(R.id.payment_type_description);
			View h_line = (View) typeItem.findViewById(R.id.h_line);
			title.setText(type.title);
			description.setText(type.hint);
			typeItem.setOnClickListener(paymentTypeListener);
			lastTypeItemLine = h_line;
			typeItem.setTag(type.typeTag);
		}
		if (lastTypeItemLine != null) {
			lastTypeItemLine.setVisibility(View.INVISIBLE);
		}

	}

	View.OnClickListener paymentTypeListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			ViewUtils.preventViewMultipleClick(v, 1000);
			// 类别 1:银联 (客户端) 2：支付宝(客户端) 3:支付宝(wap) 4:支付宝信用卡(wap)
			int typeTag = Integer.parseInt(v.getTag().toString());
			executePostCouponTask(typeTag, v);
		}
	};

	/**
	 * 跳至团购支付处理拦截页面
	 */
	void gotoCashIntercept() {
		Bundle mBundle = new Bundle();
		mBundle.putString(Settings.BUNDLE_ORDER_ID, couponPostResult.orderId);
		ActivityUtil.jump(ctx, GroupBuySubmitSuccessActivity.class, 0, mBundle);

		finish();
	}

}
