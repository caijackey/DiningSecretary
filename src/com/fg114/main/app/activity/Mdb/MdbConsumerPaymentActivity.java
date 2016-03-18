//package com.fg114.main.app.activity.Mdb;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.Selection;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.View.OnClickListener;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.view.animation.Animation.AnimationListener;
//import android.webkit.JsPromptResult;
//import android.webkit.JsResult;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.webkit.WebSettings.LayoutAlgorithm;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.RatingBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.fg114.main.R;
//import com.fg114.main.alipay.AliPayUtils;
//import com.fg114.main.analytics.OpenPageDataTracer;
//import com.fg114.main.app.Settings;
//import com.fg114.main.app.activity.MainFrameActivity;
//import com.fg114.main.app.activity.ShowErrorActivity;
//import com.fg114.main.app.activity.mealcombo.GroupBuyPaymentActivity;
//import com.fg114.main.app.activity.mealcombo.GroupBuySubmitSuccessActivity;
//import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
//import com.fg114.main.app.adapter.MdbFreeRecordAdapter;
//import com.fg114.main.qmoney.QuickMoneyUtils;
//import com.fg114.main.service.dto.CommonPostPayResultData;
//import com.fg114.main.service.dto.CouponPostResultData;
//import com.fg114.main.service.dto.MdbFreeOrderFormData;
//import com.fg114.main.service.dto.MdbFreeRecordData;
//import com.fg114.main.service.dto.MdbPostOrderResultData;
//import com.fg114.main.service.dto.MdbRestInfoData;
//import com.fg114.main.service.dto.PayTypeData;
//import com.fg114.main.service.http.ServiceRequest;
//import com.fg114.main.service.http.ServiceRequest.API;
//import com.fg114.main.service.task.CommonTask;
//import com.fg114.main.util.ActivityUtil;
//import com.fg114.main.util.CheckUtil;
//import com.fg114.main.util.DialogUtil;
//import com.fg114.main.util.JavaScriptInterface;
//import com.fg114.main.util.SessionManager;
//import com.fg114.main.util.UnionpayUtils;
//import com.fg114.main.util.ViewUtils;
//import com.fg114.main.wxapi.WXPayEntryActivity;
//import com.fg114.main.wxapi.WeixinUtils;
//
///**
// * 免担保消费付款页面 需要传入参数 //typeTag：1:新增 2：修改 //typeTag=1 餐厅id typeTag=2 订单id
// * 
// * @author dengxiangyu
// * 
// */
//public class MdbConsumerPaymentActivity extends MainFrameActivity {
//	private static final String TAG = "MdbConsumerPaymentActivity";
//	private Context ctx = MdbConsumerPaymentActivity.this;
//	// 传入参数
//	private int typeTag = 1;
//	private String uuid = "";
//	private String orderId="";
//	// 缓存数据
//	private MdbFreeOrderFormData mdbFreeOrderFormData;
//	private List<PayTypeData> payTypeList = null;
//	private CommonPostPayResultData commonPostPayResultData;
//
////	private String startPage = Settings.PAYMENT_HOST + "/coupon/buy?";
//
//	private JavaScriptInterface mJavaScriptInterface = new JavaScriptInterface();
//
//	private LayoutInflater mInflater;
//	private View contextView;
//	// 界面组件
//	private RelativeLayout mdb_vonsumer_payment_layout;
//	private LinearLayout consumer_info_layout;
//	private TextView rest_name_title;
//	private EditText pay_money;
//	private EditText booker_tel;
//	private LinearLayout use_remain_money_layout;
//	private CheckBox use_remain_money_iv;
//	private TextView user_remain_money;
//	private TextView user_pay_money;
//	private Button bnt_submit;
//	private LinearLayout flower_panel_layout;
//	private TextView free_pct;
//	private TextView free_num;
//	private TextView free_money;
//	private RatingBar flower_num;
//	private Button flower_panel_more_bt;
//
//	// 弹出框
//	private android.widget.PopupWindow popupWindow;
//	private View popupWindowView;
//	private LinearLayout free_record_layout;
//	private ListView free_record_list;
//	private TextView free_record_free_pct;
//	private TextView free_record_free_num;
//	private TextView free_record_free_money;
//	private RatingBar free_record_flower_num;
//	private Button free_record_cancel_bt;
//
//	// 动态值 （根据用户输入的价格而变化的）
//	private BigDecimal mShouldPay = new BigDecimal(0); // 应付金额
//	private BigDecimal mPayFromAccount = new BigDecimal(0); // 使用账户余额（静态）
//	private BigDecimal mActualPay = new BigDecimal(0); // 实际支付金额
//	private BigDecimal usedUserRemainMoney= new BigDecimal(0);//实际支付余额
//	private boolean isUseRemainMoneyPay = false;
//
//	// 支付方式
//	private LinearLayout paymentTypeView;
//	private ViewGroup paymentTypeContainer;
//
//	// webView
//	private WebView webView;
//
//	private boolean mFromUnionPay; // 是否曾跳转过银联支付
//	private boolean isJumpWeiXinPay = false;// 是否曾跳转过微信支付
//	private boolean isJumpkQPay = false;// 是否曾跳转过快钱支付
//
//	private String currentUrl = "";
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//         
//		Bundle bundle=this.getIntent().getExtras();
//		if(bundle.containsKey(Settings.BUNDLE_TPYE_TAG)){
//			typeTag=bundle.getInt(Settings.BUNDLE_TPYE_TAG);
//		}
//
//		if(bundle.containsKey(Settings.UUID)){
//			uuid=bundle.getString(Settings.UUID);
//		}
//		// 初始化界面
//		initComponent();
//
//		// 检查网络是否连通
//		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
//		if (!isNetAvailable) {
//			// 没有网络的场合，去提示页
//			Bundle bund = new Bundle();
//			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
//			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
//		}
//
//		// 获得查询结果
//		executeGetConsumerPaymentTask();
//	}
//
//	@Override
//	protected void onResume() {
//
//		super.onResume();
//
//		if (mFromUnionPay) {
//			String result = UnionpayUtils.getUnionpayResult();
//			if (!TextUtils.isEmpty(result)) {
//				// Log.e("msh", "result = " + result);
//				if (result.contains("成功") || result.contains("<respCode>0000</respCode>")) {
//					DialogUtil.showToast(this, "支付成功");
//					gotoMdbIntercept();
//				} else if (result.contains("<respCode>9001</respCode>")) {
//					DialogUtil.showToast(this, "支付已取消");
//				} else {
//					int start = result.indexOf("<respDesc>");
//					int end = result.indexOf("</respDesc>");
//					String desc = "";
//					if (start != -1 && end != -1 && start + 10 < end) {
//						desc = ":" + result.substring(start + 10, end);
//					}
//					DialogUtil.showToast(this, "支付失败" + desc);
//				}
//			}
//			finish();
//		}
//
//		if (isJumpWeiXinPay) {
//			int WXPayResult = WXPayEntryActivity.getWeiXinPayResult();
//			// Log.e("msh", "result = " + result);
//			if (WXPayResult == 1) {
//				DialogUtil.showToast(this, "支付成功");
//				gotoMdbIntercept();
//				
//			} else if (WXPayResult == 2) {
//				DialogUtil.showToast(this, "支付已取消");
//			} else {
//				DialogUtil.showToast(this, "支付失败");
//			}
//			finish();
//		}
//	}
//	
//	
//	/**
//	 * 接收回调
//	 */
//	protected void onNewIntent(Intent intent) {
//		if (isJumpkQPay) {
//			String orderId = intent.getStringExtra("orderId");
//			String payResult = intent.getStringExtra("payResult");
//
//			if (!TextUtils.isEmpty(orderId) && !TextUtils.isEmpty(payResult)) {
//				int payResultCode = Integer.parseInt(payResult);
//				String payResultStr = "";
//
//				// 1：支付成功 2:支付失败 0： 交易取消
//				switch (payResultCode) {
//				case 0:
//					DialogUtil.showToast(this, "支付已取消");
//					break;
//
//				case 1:
//					
//					DialogUtil.showToast(this, "支付成功");
//					
//					// 跳至现金券支付处理拦截页面
//					gotoMdbIntercept();
//					break;
//
//				case 2:
//					DialogUtil.showToast(this, "支付失败");
//					break;
//
//				default:
//					DialogUtil.showToast(this, "支付失败");
//					break;
//				}
//
//			}
//			
//			finish();
//
//		}
//		isJumpkQPay = false;
//		super.onNewIntent(intent);
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		if (webView != null) {
//			webView.stopLoading();
//			webView.clearCache(true);
//			webView.clearHistory();
//			webView.clearFocus();
//			webView.clearView();
//			webView.destroy();
//		}
//	}
//
//	private void initComponent() {
//		this.getBtnGoBack().setVisibility(View.VISIBLE);
//		this.getTvTitle().setText("消费付款");
//		this.getBtnOption().setVisibility(View.INVISIBLE);
//		// 返回按钮事件
//		this.getBtnGoBack().setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// -----
//				OpenPageDataTracer.getInstance().addEvent("返回按钮");
//				// -----
//				doBackButtonAction();
//			}
//		});
//
//		// 初始化
//		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		contextView = mInflater.inflate(R.layout.mdb_consumer_payment_activity, null);
//		consumer_info_layout = (LinearLayout) contextView.findViewById(R.id.consumer_info_layout);
//		rest_name_title = (TextView) contextView.findViewById(R.id.rest_name_title);
//		pay_money = (EditText) contextView.findViewById(R.id.pay_money);
//		booker_tel = (EditText) contextView.findViewById(R.id.booker_tel);
//		use_remain_money_layout = (LinearLayout) contextView.findViewById(R.id.use_remain_money_layout);
//		use_remain_money_iv = (CheckBox) contextView.findViewById(R.id.use_remain_money_iv);
//		user_remain_money = (TextView) contextView.findViewById(R.id.user_remain_money);
//		user_pay_money = (TextView) contextView.findViewById(R.id.user_pay_money);
//		bnt_submit = (Button) contextView.findViewById(R.id.bnt_submit);
//		flower_panel_layout = (LinearLayout) contextView.findViewById(R.id.flower_panel_layout);
//		free_pct = (TextView) contextView.findViewById(R.id.free_pct);
//		free_num = (TextView) contextView.findViewById(R.id.free_num);
//		free_money = (TextView) contextView.findViewById(R.id.free_money);
//		flower_num = (RatingBar) contextView.findViewById(R.id.flower_num);
//		flower_panel_more_bt = (Button) contextView.findViewById(R.id.flower_panel_more_bt);
//		mdb_vonsumer_payment_layout = (RelativeLayout) contextView.findViewById(R.id.mdb_vonsumer_payment_layout);
//		paymentTypeView = (LinearLayout) contextView.findViewById(R.id.paymentTypeView);
//		paymentTypeContainer = (ViewGroup) contextView.findViewById(R.id.paymentTypeContainer);
//		webView = (WebView) contextView.findViewById(R.id.webView);
//
//		// 弹出框初始化
//		popupWindowView = mInflater.inflate(R.layout.mdb_free_record_layout, null);
//		free_record_layout = (LinearLayout) popupWindowView.findViewById(R.id.free_record_layout);
//		free_record_list = (ListView) popupWindowView.findViewById(R.id.free_record_list);
//		free_record_free_pct = (TextView) popupWindowView.findViewById(R.id.free_record_free_pct);
//		free_record_free_num = (TextView) popupWindowView.findViewById(R.id.free_record_free_num);
//		free_record_free_money = (TextView) popupWindowView.findViewById(R.id.free_record_free_money);
//		free_record_flower_num = (RatingBar) popupWindowView.findViewById(R.id.free_record_flower_num);
//		free_record_cancel_bt = (Button) popupWindowView.findViewById(R.id.free_record_cancel_bt);
//
//		use_remain_money_iv.setChecked(false);
//
//		use_remain_money_iv.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
//				// TODO Auto-generated method stub
//				if (flag) {
//					// 使用余额
//					doChangeByPayFromAccount(pay_money.getText().toString());
//				} else {
//					// 不使用余额
//					doPayAccount(pay_money.getText().toString());
//				}
//
//			}
//		});
//
//		pay_money.addTextChangedListener(new TextWatcher() {
//			private String oldValue;
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//				oldValue = s.toString();
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				try {
//					if (oldValue != null && !oldValue.equals(s.toString())) {
//						if (mdbFreeOrderFormData.canUseRemainMoneyTag && use_remain_money_iv.isChecked()) {
//							// 使用余额
//							String newValue = s.toString();
//							if (TextUtils.isEmpty(newValue)) {
//								newValue = "0.0";
//								DialogUtil.showToastShort(MdbConsumerPaymentActivity.this, "请输入消费金额");
//								return;
//							}
//							if (TextUtils.isEmpty(oldValue)) {
//								oldValue = "0.0";
//							}
//							if (new BigDecimal(oldValue).compareTo(new BigDecimal(newValue)) == 0) {
//								return; // 界面无需任何变化
//							}
//
//							doChangeByPayFromAccount(s.toString());
//
//						} else {
//							String newValue = s.toString();
//							if (TextUtils.isEmpty(newValue)) {
//								newValue = "0.0";
//								doPayAccount("");
//								DialogUtil.showToastShort(MdbConsumerPaymentActivity.this, "请输入消费金额");
//								return;
//							}
//							if (TextUtils.isEmpty(oldValue)) {
//								oldValue = "0.0";
//							}
//							if (new BigDecimal(oldValue).compareTo(new BigDecimal(newValue)) == 0) {
//								return; // 界面无需任何变化
//							}
//							// 不使用余额
//							doPayAccount(s.toString());
//						}
//					}
//				} catch (Exception e) {
//					setView(mdbFreeOrderFormData);
//				}
//			}
//		});
//
//		bnt_submit.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				executePostCouponTask();
//					
//			}
//			
//		});
//
//		flower_panel_more_bt.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				showSubTabMenu();
//			}
//		});
//
//		free_record_cancel_bt.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				popupWindow.dismiss();
//			}
//		});
//
//		// webView设置
//		initWebView();
//
//		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
//	}
//
//	private void executeGetConsumerPaymentTask() {
//		ServiceRequest request = new ServiceRequest(API.getMdbFreeOrderFormInfo);
//		request.addData("typeTag", typeTag);
//		request.addData("uuid", uuid);
//
//		CommonTask.request(request, "数据加载中，请稍候...", new CommonTask.TaskListener<MdbFreeOrderFormData>() {
//			@Override
//			protected void onSuccess(MdbFreeOrderFormData dto) {
//				mdbFreeOrderFormData = dto;
//				// 缓存免担保支付数据
//				// SessionManager.getInstance().setMdbFreeOrderFormData(dto);
//				// payTypeList=dto.payTypeList;
//				setView(dto);
//				setPopupWindowView(dto);
//			}
//
//			@Override
//			protected void onError(int code, String message) {
//               super.onError(code, message);
//               finish();
//			}
//		});
//	}
//	
//	//选择支付方式
//	private void postMdbSelFreeOrderPayType(final int payTypeTag, final View v) {
//
//		ServiceRequest request = new ServiceRequest(API.postMdbSelFreeOrderPay);
//		request.addData("orderId", orderId); // 订单id
//		request.addData("payTypeTag", payTypeTag); //支付方式	
//
//
//		// request.setCanUsePost(true);
//		CommonTask.request(request, new CommonTask.TaskListener<CommonPostPayResultData>() {
//			@Override
//			protected void onSuccess(CommonPostPayResultData dto) {
//				if (dto == null) {
//					finish();
//					return;
//				}
//				
//				orderId=dto.orderId;
//				
//				commonPostPayResultData = dto;
//				if (!commonPostPayResultData.chkPassTag) {
//					DialogUtil.showAlert(ctx, "提示", CheckUtil.isEmpty(commonPostPayResultData.errorHint) ? "暂时无法使用此种支付方式" : commonPostPayResultData.errorHint);
//					return;
//				}
//				// 类别 0：直接余额支付 1:银联 (客户端) 2：支付宝(客户端) 3:支付宝(wap) 4:支付宝信用卡(wap)//
//				// 5:微信支付
//				switch (payTypeTag) {
//				case 0:
//					doDirectPay(commonPostPayResultData);
//					break;
//				case 1:
//					doUniPay(commonPostPayResultData.unionPayXml);
//					break;
//				case 2:
//					doAliPay(commonPostPayResultData.aliPayInfo);
//					break;
//				case 3:
//					jumpToWebView(commonPostPayResultData.wapAliPayUrl);
//					break;
//				case 4:
//					jumpToWebView(commonPostPayResultData.wapAliPayCreditCardUrl);
//					break;
//				// 5:微信支付
//				case 5:
//					doWeixinPay(commonPostPayResultData.weixinInfo);
//					break;
//				// 6:快钱支付
//				case 6:
//					doKqPay(commonPostPayResultData.kqInfo,v);
//					break;
//				default:
//					if (payTypeTag >= 100) { // 大于等于100是“其他wap支付方式”
//						jumpToWebView(commonPostPayResultData.wapAliPayUrl);
//					}
//					break;
//				}
//			}
//			@Override
//			protected void onError(int code, String message) {
//               super.onError(code, message);
//			}
//			
//		});
//	}
//
//	// 提交订单表单
//	private void executePostCouponTask() {
//		String tel = booker_tel.getText().toString();
//		if (!CheckUtil.isCellPhone(tel)) {
//			DialogUtil.showToast(ctx, "请输入正确手机号");
//			return;
//		}
//		ServiceRequest request = new ServiceRequest(API.postMdbFreeOrder);
//		request.addData("typeTag", typeTag); // 1:新增 2：修改
//		request.addData("uuid", uuid); //typeTag=1 餐厅id    typeTag=2 订单id
//		request.addData("payMoney", mShouldPay.doubleValue()); // 支付金额 格式 12.12
//		request.addData("userTel", tel); // 手机号码
//		request.addData("usedUserRemainMoney", usedUserRemainMoney.doubleValue()); // 使用的账户余额
//		// request.setCanUsePost(true);
//		CommonTask.request(request, new CommonTask.TaskListener<MdbPostOrderResultData>() {
//			@Override
//			protected void onSuccess(MdbPostOrderResultData dto) {
//			      orderId=dto.orderId;
//			      
//			      if(dto.needSelPayTypeTag){
//			    	  jumpToPaymentType();
//			      }else{
//			    	  gotoMdbIntercept();
//			      }
//			}
//			
//			@Override
//			protected void onError(int code, String message) {
//               super.onError(code, message);
//			}
//			
//			
//		});
//	}
//
//	private void setView(MdbFreeOrderFormData dto) {
//		if (dto == null) {
//			return;
//		}
//		
//		rest_name_title.setText(dto.restName);
//
//		if (dto.payMoney != 0) {
//			pay_money.setText(dto.payMoney + "");
//			Editable etext = pay_money.getText();
//			Selection.setSelection(etext, etext.length());
//		} else {
//			pay_money.setText("");
//		}
//
//		if (!CheckUtil.isEmpty(dto.userTel)) {
//			booker_tel.setText(dto.userTel);
//			Editable etext1 = booker_tel.getText();
//			Selection.setSelection(etext1, etext1.length());
//		}
//
//		if (dto.canUseRemainMoneyTag) {
//			use_remain_money_layout.setVisibility(View.VISIBLE);
//		} else {
//			use_remain_money_layout.setVisibility(View.GONE);
//		}
//		if (dto.userRemainMoney != 0) {
//			user_remain_money.setText("余额 ￥" + dto.userRemainMoney);
//			mPayFromAccount = BigDecimal.valueOf(dto.userRemainMoney);
//		}
//
//		if (dto.showFlowerPanelTag) {
//			flower_panel_layout.setVisibility(View.VISIBLE);
//			flower_panel_more_bt.setVisibility(View.VISIBLE);
//		} else {
//			flower_panel_layout.setVisibility(View.GONE);
//			flower_panel_more_bt.setVisibility(View.GONE);
//		}
//
//		free_pct.setText(dto.freePct);
//		free_num.setText(dto.freeNum);
//		free_money.setText(dto.freeMoney);
//
//		flower_num.setRating(dto.flowerNum);
//
//		buildPaymentTypeItems(paymentTypeContainer, dto.payTypeList);
//	}
//
//	private void setPopupWindowView(MdbFreeOrderFormData dto) {
//		if (dto == null) {
//			return;
//		}
//		free_record_free_pct.setText(dto.freePct);
//		free_record_free_num.setText(dto.freeNum);
//		free_record_free_money.setText(dto.freeMoney);
//
//		free_record_flower_num.setRating(dto.flowerNum);
//
//		if (dto.freeRecordList != null && dto.freeRecordList.size() != 0) {
//			MdbFreeRecordAdapter mdbFreeRecordAdapter = new MdbFreeRecordAdapter(MdbConsumerPaymentActivity.this, dto.freeRecordList);
//			free_record_list.setAdapter(mdbFreeRecordAdapter);
//		} 
//
//	}
//
//	// 使用余额
//	private void doChangeByPayFromAccount(String newValue) {
//		// 2种情况 1. 使用余额支付 2.余额不足 使用其它方式支付
//		if (CheckUtil.isEmpty(newValue)) {
//			newValue = "0";
//		}
//		mShouldPay = BigDecimal.valueOf(Double.valueOf(newValue));
//		mActualPay = mShouldPay.subtract(mPayFromAccount);
//		if (mActualPay.compareTo(BigDecimal.valueOf(0)) == -1 || mActualPay.compareTo(BigDecimal.valueOf(0)) == 0) {
//			// 用户余额足够，直接支付
//			usedUserRemainMoney=mShouldPay;
//			mActualPay=BigDecimal.valueOf(0);
//			user_pay_money.setText("￥0.0");
//			isUseRemainMoneyPay = true;	
//		} else {			
//			//使用余额等于用户静态金额
//			usedUserRemainMoney=mPayFromAccount;
//			user_pay_money.setText("￥" + mActualPay.doubleValue());
//			isUseRemainMoneyPay = false;
//		}
//	}
//
//	// 不使用余额
//	private void doPayAccount(String newValue) {
//		if (CheckUtil.isEmpty(newValue)) {
//			newValue = "0";
//		}
//		//使用余额等于0
//		usedUserRemainMoney= BigDecimal.valueOf(0);
//		mShouldPay = BigDecimal.valueOf(Double.valueOf(newValue));
//		mActualPay = mShouldPay;
//		user_pay_money.setText("￥" + mActualPay.doubleValue());
//	}
//
//	// private void showMdbFreeRecordData(List<MdbFreeRecordData>
//	// freeRecordList){
//	// if(freeRecordList==null&&freeRecordList.size()==0){
//	// return;
//	// }
//
//	/**
//	 * 显示弹出层
//	 * 
//	 * @param context
//	 * @param parent
//	 *            父View
//	 * @param child
//	 *            弹出气泡的内容
//	 * @param dismissOnTouch
//	 *            是否在点击屏幕时消失
//	 * @param listener
//	 *            弹出层消失的监听
//	 * @return
//	 */
//	private PopupWindow showPopupwindow(Context context, View parent, View child) {
//
//		if (parent == null) {
//			parent = ((Activity) context).getWindow().getDecorView();
//		}
//
//		// 灰色背景遮罩
//		LinearLayout bgView = new LinearLayout(context);
//		bgView.setOrientation(LinearLayout.VERTICAL);
//		bgView.setBackgroundColor(0xb5555555);
//		bgView.setGravity(Gravity.CENTER);
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
//		bgView.setLayoutParams(params);
//		final PopupWindow popBg = new PopupWindow(bgView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//		popBg.setOutsideTouchable(true);
//		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);
//
//		// 弹出层显示的内容
//		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//		popMain.setBackgroundDrawable(new BitmapDrawable());
//		popMain.setOutsideTouchable(true);
//		popMain.setFocusable(true);
//		// popMain.setAnimationStyle(R.style.Animations_PopDownMenu);
//
//		popMain.setClippingEnabled(true);
//
//		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
//			@Override
//			public void onDismiss() {
//				if (popBg.isShowing()) {
//					popBg.dismiss();
//				}
//
//			}
//		});
//
//		popMain.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
//		return popMain;
//	}
//
//	private void showSubTabMenu() {
//
//		Animation in = AnimationUtils.loadAnimation(this, R.anim.index_slide_in_bottom_self);
//		// group_buy.clearAnimation();
//		in.setAnimationListener(new AnimationListener() {
//
//			@Override
//			public void onAnimationStart(Animation animation) {
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				// TODO Auto-generated method stub
//				// if(order_bubble_clone.getText().toString().equals("0")){
//				// order_bubble_clone.setVisibility(View.GONE);
//				// }else{
//				// order_bubble_clone.setVisibility(View.VISIBLE);
//				// }
//				popupWindowView.clearAnimation();
//			}
//		});
//		popupWindowView.setAnimation(in);
//		popupWindowView.startAnimation(in);
//		popupWindow = showPopupwindow(MdbConsumerPaymentActivity.this, null, popupWindowView);
//	}
//
//	/**
//	 * ------------------------------------------------------------------------
//	 * ------- WebView 初始化
//	 */
//	private void initWebView() {
//		WebSettings webSettings = webView.getSettings();
//		webSettings.setJavaScriptEnabled(true);
//		webSettings.setAllowFileAccess(true);
//		webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
//
//		// 添加JS处理
//		webView.addJavascriptInterface(mJavaScriptInterface, "jsinterface");
//		webView.setWebViewClient(new WebViewClient() {
//
//			/**
//			 * 开始载入页面
//			 */
//			@Override
//			public void onPageStarted(WebView view, String url, Bitmap favicon) {
//
//				if (DEBUG)
//					Log.d("onPageStarted:" + TAG, view + " - " + url);
//				showProgressDialog(getString(R.string.text_info_loading));
//
//				Uri uri = Uri.parse(url);
////				Uri baseUri = Uri.parse(startPage);
//				if (!url.contains(Settings.kXmsHostKey)) {
//					getBtnGoBack().setText("现金券详情");
//				}
//				// 如果是支付成功，则直接finish本activity
//				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBuySuccKey)) {
//					gotoMdbIntercept();
//					finish();
//				}
//				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
//				// （http://w.xiaomishu.com/coupon/back）
//				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBackKey)) {
//
//					DialogUtil.showComfire(ctx, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {
//
//						@Override
//						public void run() {
//							finish();
//
//						}
//					}, "否", new Runnable() {
//
//						@Override
//						public void run() {
//						}
//					});
//				}
//
//				super.onPageStarted(view, url, favicon);
//			}
//
//			@Override
//			public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
//				if (DEBUG)
//					Log.d(TAG + " doUpdateVisitedHistory", url);
//				super.doUpdateVisitedHistory(view, url, isReload);
//			}
//
//			/**
//			 * 页面载入结束
//			 */
//			@Override
//			public void onPageFinished(WebView view, String url) {
//				super.onPageFinished(view, url);
//				closeProgressDialog();
//				if (DEBUG)
//					Log.d(TAG + " onPageFinished", url + " title=" + webView.getTitle());
//
//				if (currentUrl.equals(url)) {
//					webView.clearHistory();
//				}
//			}
//
//			@Override
//			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//				if (DEBUG)
//					Log.d(TAG, "onReceivedError:" + description);
//				if (DEBUG)
//					Log.d(TAG, "onReceivedError:" + failingUrl);
//				super.onReceivedError(view, errorCode, description, failingUrl);
//			}
//
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				// 一旦到了站外，返回按钮变成“现金券详情”
//				Uri uri = Uri.parse(url);
////				Uri baseUri = Uri.parse(startPage);
//
//				if (!url.contains(Settings.kXmsHostKey)) {
//					getBtnGoBack().setText("现金券详情");
//				}
//				// 如果是支付成功，则直接finish本activity
//				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBuySuccKey)) {
//					gotoMdbIntercept();
//					finish();
//					return true;
//				}
//				// 如果是页面中返回按钮，则提示一下，由用户决定是否离开
//				// （http://w.xiaomishu.com/coupon/back）
//				if (url.contains(Settings.kXmsHostKey)&& url.contains(Settings.kBackKey)) {
//
//					DialogUtil.showComfire(ctx, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {
//
//						@Override
//						public void run() {
//							finish();
//
//						}
//					}, "否", new Runnable() {
//
//						@Override
//						public void run() {
//						}
//					});
//					return true;
//				}
//
//				return super.shouldOverrideUrlLoading(view, url);
//			}
//
//		});
//
//		webView.setWebChromeClient(new WebChromeClient() {
//
//			/**
//			 * 页面关闭
//			 */
//			@Override
//			public void onCloseWindow(WebView window) {
//				super.onCloseWindow(window);
//				finish();
//			}
//
//			@Override
//			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//				if (DEBUG)
//					Log.d(TAG, "alert");
//				return super.onJsAlert(view, url, message, result);
//			}
//
//			@Override
//			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//				if (DEBUG)
//					Log.d(TAG, "Confirm");
//				return super.onJsConfirm(view, url, message, result);
//			}
//
//			@Override
//			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
//				if (DEBUG)
//					Log.d(TAG, "Prompt");
//				return super.onJsPrompt(view, url, message, defaultValue, result);
//			}
//
//			@Override
//			public void onReceivedTitle(WebView view, String title) {
//				super.onReceivedTitle(view, title);
//				if (DEBUG)
//					Log.d(TAG + ",onReceivedTitle", "title=" + title);
//				getTvTitle().setText(webView.getTitle());
//			}
//
//		});
//	}
//
//	// ----------------------------------------------------------------------
//	// 返回键的控制(very important)
//	// 返回按钮事件处理
//	private void doBackButtonAction() {
//		if (DEBUG)
//			Log.d("webView.canGoBack()", webView.canGoBack() + "," + webView.getUrl());
//
//		if (webView == null || webView.getUrl() == null) {
//			this.finish();
//			return;
//		}
//		// 目前直接结束当前页
//		// this.finish();
//		// --主页的uri
////		Uri baseUri = Uri.parse(startPage);
////		String baseHost = baseUri.getHost();
//
//		// --当前页的uri
//		Uri uri = Uri.parse(webView.getUrl());
//		String fragment = uri.getFragment();
//		String host = uri.getHost();
//		
//		String uriString=uri.toString();
//
//		// 如果到了支付成功页，或者是支付首页，则直接finish；如果到了站外，则提示是否离开页面。
//		// 否则，如果webView有历史记录，则返回历史记录中的上一条，
//		// 如果没有历史记录，返回paymentView
//		if (uriString.contains(Settings.kXmsHostKey) && uriString.contains(Settings.kBuySuccKey) || mdb_vonsumer_payment_layout.getVisibility() == View.VISIBLE) {
//
//			this.finish();
//
//		} else if (!uriString.contains(Settings.kXmsHostKey)) {
//			// 如果到了站外，提示： 注意离开支付页面可能会造成当前支付失败．您是否要离开？
//			DialogUtil.showComfire(this, "离开页面", " 注意！离开支付页面可能会造成当前支付失败．您是否要离开？", "是", new Runnable() {
//
//				@Override
//				public void run() {
//					finish();
//
//				}
//			}, "否", new Runnable() {
//
//				@Override
//				public void run() {
//				}
//			});
//		} else if (!webView.canGoBack() ||uriString.contains(Settings.kXmsHostKey) && uriString.contains(Settings.kStartKey) ) {
//			jumpToPaymentView();
//		} else {
//			webView.goBack();
//		}
//
//	}
//
//	@Override
//	public void onBackPressed() {
//		doBackButtonAction();
//	}
//
//	// ------------------------------------------------------------- 画面之间的切换
//	// 返回到到支付方式选择界面
//	private void jumpToPaymentType() {
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//		mdb_vonsumer_payment_layout.setVisibility(View.GONE);
//		paymentTypeView.setVisibility(View.VISIBLE);
//		webView.setVisibility(View.GONE);
//		getBtnOption().setVisibility(View.INVISIBLE);
//	}
//
//	// 跳转到WebView支付确认界面
//	private void jumpToWebView(String url) {
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//		mdb_vonsumer_payment_layout.setVisibility(View.GONE);
//		paymentTypeView.setVisibility(View.GONE);
//		webView.setVisibility(View.VISIBLE);
//		webView.clearView();
//		webView.loadUrl(url);
//		webView.requestFocus();
//		webView.requestFocusFromTouch();
//
//		getBtnOption().setVisibility(View.INVISIBLE);
//	}
//
//	// 跳转到paymentView支付定单页面
//	private void jumpToPaymentView() {
//		mdb_vonsumer_payment_layout.setVisibility(View.VISIBLE);
//		paymentTypeView.setVisibility(View.GONE);
//		webView.setVisibility(View.GONE);
//
//		getBtnOption().setVisibility(View.INVISIBLE);
//		this.getTvTitle().setText("消费付款");
//
//	}
//
//	// ---------------------------------------------------------------------
//	// 不同支付方式处理
//
//	private void doDirectPay(CommonPostPayResultData result) {
//		Toast.makeText(ctx, "支付成功", Toast.LENGTH_SHORT).show();
//		gotoMdbIntercept();
//	}
//
//	/**
//	 * 银联客户端支付
//	 */
//	private void doUniPay(String xml) {
//		mFromUnionPay = true;
//		UnionpayUtils.doPay(this, xml);
//		// UnionpayUtils.doPay(this, UnionpayUtils.testXml);
//	}
//
//	/**
//	 * 支付宝客户端支付
//	 */
//	private void doAliPay(String orderInfo) {
//		AliPayUtils.doPay(this, orderInfo, new AliPayUtils.AliPayListener() {
//
//			@Override
//			public void onPayFinish(final boolean isSuccessful, final String message) {
//				runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						if (isSuccessful) {
//							Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
//							gotoMdbIntercept();
//						} else {
//							Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
//						}
//						finish();
//					}
//				});
//
//			}
//		});
//	}
//
//	/**
//	 * 跳转到微信支付确认界面
//	 * 
//	 * @param weixinInfo
//	 */
//	private void doWeixinPay(String weixinInfo) {
//		isJumpWeiXinPay = true;
//		WeixinUtils.doPay(MdbConsumerPaymentActivity.this, weixinInfo);
//		// gotoCashIntercept();
//
//	}
//	
//	private void doKqPay(String qMoneyInfo, View view) {
//		isJumpkQPay = true;
//		QuickMoneyUtils.doPay(MdbConsumerPaymentActivity.this, MdbConsumerPaymentActivity.class, qMoneyInfo, view,"com.fg114.main.app.activity.Mdb.MdbConsumerPaymentActivity");
//	}
//
//
//	/**
//	 * 构建出所有的支付方式
//	 */
//	void buildPaymentTypeItems(ViewGroup vg, List<PayTypeData> payTypeList) {
//
//		vg.removeAllViews();
//		if (vg == null || payTypeList == null || payTypeList.size() == 0) {
//			return;
//		}
//		View lastTypeItemLine = null;
//		// list_item_payment_type
//		for (PayTypeData type : payTypeList) {
//			ViewGroup typeItem = (ViewGroup) View.inflate(this, R.layout.list_item_payment_type, null);
//			vg.addView(typeItem);
//			TextView title = (TextView) typeItem.findViewById(R.id.payment_type_title);
//			TextView description = (TextView) typeItem.findViewById(R.id.payment_type_description);
//			View h_line = (View) typeItem.findViewById(R.id.h_line);
//			title.setText(type.title);
//			description.setText(type.hint);
//			typeItem.setOnClickListener(paymentTypeListener);
//			lastTypeItemLine = h_line;
//			typeItem.setTag(type.typeTag);
//		}
//		if (lastTypeItemLine != null) {
//			lastTypeItemLine.setVisibility(View.INVISIBLE);
//		}
//	}
//
//	View.OnClickListener paymentTypeListener = new View.OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			ViewUtils.preventViewMultipleClick(v, 1000);
//			// 类别 1:银联 (客户端) 2：支付宝(客户端) 3:支付宝(wap) 4:支付宝信用卡(wap)
//			int typeTag = Integer.parseInt(v.getTag().toString());
//			postMdbSelFreeOrderPayType(typeTag,v);
//		}
//	};
//
//	void gotoMdbIntercept() {
//		Bundle mBundle = new Bundle();
//		mBundle.putString(Settings.BUNDLE_ORDER_ID,orderId);
//		mBundle.putString(Settings.BUNDLE_REST_ID,uuid);
////		Settings.mdbActivityClazz = getLastActivityClass();
//		ActivityUtil.jump(ctx, MdbSubmitSuccessActivity.class, 0, mBundle);
//
//		finish();
//	}
//
//}
