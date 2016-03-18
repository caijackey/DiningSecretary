package com.fg114.main.app.activity.order;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract.Contacts.Data;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SendSMSActivity;
import com.fg114.main.app.activity.ShowErrorActivity;

import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;

import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.OrderFuncBtnData;
import com.fg114.main.service.dto.OrderHintBtnData;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderHintData2;
import com.fg114.main.service.dto.OrderInfoData;
import com.fg114.main.service.dto.OrderInfoData2;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.OrderMoreReserveInfoData;
import com.fg114.main.service.dto.ShareInfoData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 订单详细界面
 * 
 * @author lijian
 * 
 */
public class NewMyOrderDetailActivity extends MainFrameActivity {

	private static final String TAG = "MyOrderDetailActivity";

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	// -----下拉刷新----
	private ScrollView sv;
	private RelativeLayout topLayout;
	private RelativeLayout buttomLayout;
	private ImageView imgTopLoading;
	private ImageView imgButtomLoading;
	private TextView tvTop;
	private TextView tvButtom;

	private LinearLayout content;
	private View line;
	private int height;
	private int screenHeight;
	private int loadDataTop;
	private int loadDataButtom;
	private View inviteFriendLayout;
	private View order_list_bt;

	boolean isLoadTopData = false;
	boolean isLoadButtomData = false;
	boolean isUp = true;// 是否能够实现UP动作
	boolean isDragTop = false;
	boolean isDragButtom = false;
	boolean isOperateTop = true;
	boolean isOperateButtom = true;
	boolean isDrag = false;

	// -----订单详细信息----
	private MyImageView icon_url;
	private TextView order_detail_status_name;
	private TextView order_detail_rest_name;
	private TextView order_detail_reserve_info;
	private ImageView san_jiao;

	// -----订餐的描述----
	private LinearLayout func_hint_layout;
	private TextView func_hint;
	// -----分享之类 ----
	private LinearLayout inviteLayout;
	// -----操作提示 ----
	private TextView operate_hint;

	private LinearLayout more_reserve_info_layout;
	// -----金额面板 ----
	private LinearLayout showPricePanelTagTrue;// 金额面板
	private LinearLayout canUploadReceiptTagLayout;// 上传小票的面板
	private TextView receiptHint;// 上传小票提示
	private Button canUploadReceiptTag;
	private LinearLayout showPricePanelTagFalse;// 金额面板
	private MyImageView detailResIvResPic;// 小票
	private LinearLayout canInputPriceTagTrue;// 是否可以输入金额
	private EditText price;
	private Button priceConfirm;
	private LinearLayout canInputPriceTagFalse;// 是否可以输入金额
	private TextView repastPrice;
	private Button canReportPriceErrorTag;
	private TextView inputPriceHint;
	// // -----评论----
	// private LinearLayout showCommentPanelTag;// 评论面板
	// private LinearLayout canCommentTag;
	// private TextView commentHint;
	// private Button canCommentTagBnt;
	// -----按钮----
	private Button canEditTag;
	private Button canCancelTag;

	private LinearLayout childLayout;
	private LinearLayout layoutCenter;

	private String restaurantId;
	// private String cashCouponId;

	private long reserveTimes;
	private String restNameAndAddress;
	private long peopleNums;
	private long restTypes;

	// -----配置参数--------------
	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 80; // 图片压缩率
	private Handler handler = new Handler();
	private String orderId;
	private int reasonTypeTag = 0;
	private String reasonMemo = "";

	private String prevOrderId;
	// 下一个活跃订单id
	private String nextOrderId;

	private boolean hasPrevOrderId = true;
	private boolean hasNextOrderId = true;

	private long queryTypeTag;

	private boolean fromActivityResult;

	private boolean OpenMoreReserveInfoPanelTag;

	private ShareInfoData shareInfoData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		} else {
			orderId = "";

		}

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单详情", orderId);
		// ----------------------------

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		queryTypeTag = CheckUtil.isEmpty(orderId) ? 2 : 1;
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshUI();
		refreshUIResume();
	}

	private void refreshUIResume() {
		executeOrderTask(queryTypeTag, orderId);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				screenHeight = sv.getHeight();
				loadDataTop = imgTopLoading.getHeight();
				loadDataButtom = imgButtomLoading.getHeight();
				startLoadAnimation(imgTopLoading);
				startLoadAnimation(imgButtomLoading);
			}
		}, 200);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		if (!fromActivityResult) {// 不是拍照回来的
			OpenPageDataTracer.getInstance().enterPage("订单详情", orderId);
		} else {
			fromActivityResult = false;
		}
		// ----------------------------
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return shareInfoData == null ? "" : shareInfoData.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return shareInfoData == null ? "" : shareInfoData.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return shareInfoData == null ? "" : shareInfoData.shareWeiboUuid;
	}
	
	@Override
	protected String getRestaurantId() {
		return restaurantId;
	}
	@Override
	protected String getRestaurantName() {
		return restNameAndAddress;
	}

	/**
	 * 初始化
	 */
	// TODO 初始化
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("订单详情");
		this.getBtnGoBack().setText("返回");
		// ---
		this.getBtnOption().setText("餐厅详情");
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅详情按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
				bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
				ActivityUtil.jump(NewMyOrderDetailActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.new_order_detail, null);
		init(contextView);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void refreshUI() {
		content = (LinearLayout) contextView.findViewById(R.id.content);
		sv = (ScrollView) contextView.findViewById(R.id.scrollview);
		line = contextView.findViewById(R.id.line);
		topLayout = (RelativeLayout) contextView.findViewById(R.id.top_layout);
		buttomLayout = (RelativeLayout) contextView.findViewById(R.id.buttom_layout);
		imgTopLoading = (ImageView) contextView.findViewById(R.id.img_top_loading);
		imgButtomLoading = (ImageView) contextView.findViewById(R.id.img_buttom_loading);

		sv.setVerticalScrollBarEnabled(false);
		sv.setOnTouchListener(new OnTouchListener() {
			float sy = 0;

			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				boolean isScrollTop = false;
				boolean isScrollButtom = true;

				if (event.getAction() == MotionEvent.ACTION_MOVE) {

					if (event.getAction() == MotionEvent.ACTION_MOVE) {
						if (!isDrag) {
							sy = event.getRawY();
							isDrag = true;
						} else {
							int s = (int) (sy - event.getRawY());
							if (s > 0) {
								isScrollTop = false;
								isScrollButtom = true;
							}
							if (s < 0) {
								isScrollTop = true;
								isScrollButtom = false;

							}
						}
					}

					if (hasPrevOrderId) {
						if (isOperateTop && isScrollTop) {
							if (isLoadTopData) {
								isUp = false;
								if (!isDragTop) {
									sy = event.getRawY();
									isDragTop = true;
								} else {
									int s = (int) (sy - event.getRawY());
									content.scrollTo(0, s > 0 ? 0 : s);
								}
								return true;
							} else {
								if (sv.getScrollY() == 0 || isDragTop) {
									if (!isDragTop) {
										sy = event.getRawY();
										isDragTop = true;
									} else {
										int s = (int) (sy - event.getRawY());
										content.scrollTo(0, s > 0 ? 0 : s);
									}
									return true;
								}
							}
						}
					} else {
						// positionBack();
					}
					// 在底部

					if (hasNextOrderId) {
						if (isOperateButtom && isScrollButtom) {

							if (isLoadButtomData) {
								isUp = false;
								if (!isDragButtom) {
									sy = event.getRawY();
									isDragButtom = true;
								} else {
									int s = (int) (sy - event.getRawY());
									content.scrollTo(0, s >= 0 ? s : 0);

								}
								return true;
							} else {
								if (sv.getScrollY() == (height - screenHeight - 1) || sv.getScrollY() == (height - screenHeight) || isDragButtom) {
									if (!isDragButtom) {
										sy = event.getRawY();
										isDragButtom = true;
									} else {
										int s = (int) (sy - event.getRawY());
										content.scrollTo(0, s >= 0 ? s : 0);

									}
									return true;
								}
							}
						}

					} else {
						// positionBack();
					}
					// if ((-content.getScrollY()) >= UnitUtil.dip2px(60)) {
					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// tvTop.setText("松开进行加载...");
					// }
					// }, 100);
					// }
					// if ((content.getScrollY()) >= UnitUtil.dip2px(60)) {
					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// tvButtom.setText("松开进行加载...");
					//
					// }
					// }, 100);
					// }
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (isUp) {
						isDragButtom = false;
						isDragTop = false;
						isDrag = false;
						if ((-content.getScrollY()) >= UnitUtil.dip2px(60) && isOperateTop) {
							executeData(0);
						} else if (isOperateButtom && (content.getScrollY()) >= UnitUtil.dip2px(60)) {
							executeData(1);
						} else {
							if (isOperateTop) {
								positionBack();
							}
							if (isOperateButtom) {
								positionBack();
							}

						}
					}
					if (event.getAction() == MotionEvent.ACTION_UP) {
						isDragButtom = false;
						isDragTop = false;
						isDrag = false;
					}

				}
				return false;
			}
		});
	}

	/**
	 * 获得餐厅详细
	 */

	private void executeOrderTask(long queryTypeTag, String orderIds) {

		ServiceRequest request = new ServiceRequest(API.getOrderInfo2);
		request.addData("queryTypeTag", queryTypeTag);
		request.addData("orderId", orderIds);
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<OrderInfoData2>() {

			@Override
			protected void onSuccess(OrderInfoData2 dto) {
				// TODO 测试
				// OrderInfoData dto =doTest();
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				initData(dto);
				showDialog(dto);
				orderId = dto.orderHintData.orderId;
				OpenMoreReserveInfoPanelTag = dto.needOpenMoreReserveInfoPanelTag;

				reserveTimes = dto.reserveTime;
				nextOrderId = dto.nextOrderId;
				prevOrderId = dto.prevOrderId;

				if (CheckUtil.isEmpty(prevOrderId)) {
					hasPrevOrderId = false;
				} else {
					hasPrevOrderId = true;

				}
				if (CheckUtil.isEmpty(nextOrderId)) {
					hasNextOrderId = false;
				} else {
					hasNextOrderId = true;

				}

				restaurantId = dto.orderHintData.restId;
				restNameAndAddress = dto.orderHintData.restName;
				peopleNums = dto.peopleNum;
				restTypes = dto.roomTypeTag;
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						height = line.getTop() + 1;
					}
				}, 1);
			};

			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				DialogUtil.showToast(NewMyOrderDetailActivity.this, message);

//				doTest();
				 finish();
			};

			private void doTest() {
				String json = "{\"hintOrderNum\":\"1\",\"prevOrderId\":\"222\",\"nextOrderId\":\"11\",\"orderHintData\":{\"orderId\":\"11\",\"statusIconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"statusName\":\"订餐完\",\"restId\":\"11\",\"restName\":\"餐厅名\",\"reserveInfo\":\"2013-06-22\"},\"needOpenMoreReserveInfoPanelTag\":\"true\",\"moreReserveInfoList\":[{\"name\":\"姓名\",\"detail\":\"XXXX\"},{\"name\":\"备注\",\"detail\":\"无订单\"},{\"name\":\"电话\",\"detail\":\"13028595895\"}],\"funcHint\":\"亲亲\",\"funcBtnList\":[{\"typeTag\":\"1\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"日历\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"2\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"收藏\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"3\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"分享\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"4\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"出租\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"5\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"酒店\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"6\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"参看餐厅\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"},{\"typeTag\":\"7\",\"iconUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"name\":\"添加餐厅\",\"actionXmsUrl\":\"\",\"needFlashTag\":\"true\",\"enableTag\":\"true\",\"uploadReceiptHint\":\"啊啊\"}],\"showPricePanelTag\":\"true\",\"receiptHint\":\"上传小票\",\"canUploadReceiptTag\":\"true\",\"receiptUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"bigReceiptUrl\":\"http://s1.95171.cn/b0/sp/app/order/5.png?2013\",\"inputPriceHint\":\"啊\",\"canInputPriceTag\":\"true\",\"priceHint\":\"啊\",\"canReportPriceErrorTag\":\"true\",\"needOpenConfirmDlgTag\":\"true\",\"cdMsg\":\"对话框提示\",\"cdCancelBtnName\":\"取消\",\"cdOkBtnName\":\"确定\",\"cdActionXmsUrl\":\"111111111\",\"operateHint\":\"取消订单\",\"canCancelTag\":\"true\",\"canEditTag\":\"true\",\"reserveTime\":\"21213\",\"peopleNum\":\"1\",\"roomTypeTag\":\"0\",\"bookerName\":\"\",\"bookerSexTag\":\"0\",\"bookerTel\":\"13000000000\",\"memo\":\"1111\",\"forOtherTag\":\"true\",\"eaterName\":\"1\",\"eaterSexTag\":\"0\",\"eaterTel\":\"111\"}";
				OrderInfoData2 data = JsonUtils.fromJson(json, OrderInfoData2.class);
				onSuccess(data);
			}
		});

	}

	// 初始化
	private void init(View contextView) {
		sv = (ScrollView) contextView.findViewById(R.id.scrollview);
		// -----订单详细信息----
		order_list_bt = contextView.findViewById(R.id.order_list_bt);
		icon_url = (MyImageView) contextView.findViewById(R.id.icon_url);
		order_detail_status_name = (TextView) contextView.findViewById(R.id.order_detail_status_name);
		order_detail_rest_name = (TextView) contextView.findViewById(R.id.order_detail_rest_name);
		order_detail_reserve_info = (TextView) contextView.findViewById(R.id.order_detail_reserve_info);
		san_jiao = (ImageView) contextView.findViewById(R.id.san_jiao);
		// -----预订信息----
		more_reserve_info_layout = (LinearLayout) contextView.findViewById(R.id.more_reserve_info_layout);
		// -----功能提示----
		func_hint_layout = (LinearLayout) contextView.findViewById(R.id.func_hint_layout);
		func_hint = (TextView) contextView.findViewById(R.id.func_hint);
		// -----分享之类 ----
		inviteLayout = (LinearLayout) contextView.findViewById(R.id.invite_layout);
		// -----操作提示----
		operate_hint = (TextView) contextView.findViewById(R.id.operate_hint);
		// -----金额面板 ----
		// 整个大的金额面板
		showPricePanelTagTrue = (LinearLayout) contextView.findViewById(R.id.show_price_panel_tag_true);
		// 需要上传小票的面板
		canUploadReceiptTagLayout = (LinearLayout) contextView.findViewById(R.id.can_upload_receipt_tag_layout);
		// 上传小票的按钮
		canUploadReceiptTag = (Button) contextView.findViewById(R.id.can_upload_receipt_tag);
		// 上传小票按钮上面的文字
		receiptHint = (TextView) contextView.findViewById(R.id.receipt_hint);
		// 上传小票成功 或者不需要上传小票的面板
		showPricePanelTagFalse = (LinearLayout) contextView.findViewById(R.id.can_upload_receipt_tag_false);
		// 小票的图片
		detailResIvResPic = (MyImageView) contextView.findViewById(R.id.detail_res_ivResPic);
		// 可以输入金额的面板
		canInputPriceTagTrue = (LinearLayout) contextView.findViewById(R.id.can_input_price_tag_true);
		// 金额输入的文本框
		price = (EditText) contextView.findViewById(R.id.price);
		// 金额文本框的确定按钮
		priceConfirm = (Button) contextView.findViewById(R.id.price_confirm);
		// 报错按钮的父控件面板
		canInputPriceTagFalse = (LinearLayout) contextView.findViewById(R.id.can_input_price_tag_false);
		// 金额,秘币提示
		repastPrice = (TextView) contextView.findViewById(R.id.repast_price);
		// 报错按钮
		canReportPriceErrorTag = (Button) contextView.findViewById(R.id.can_report_price_error_tag);
		// 输入金额文本框上面的文字
		inputPriceHint = (TextView) contextView.findViewById(R.id.input_price_pint);

		// -----按钮----
		canEditTag = (Button) contextView.findViewById(R.id.can_edit_tag);
		canCancelTag = (Button) contextView.findViewById(R.id.can_cancel_tag);
		layoutCenter = (LinearLayout) contextView.findViewById(R.id.layout_center);
		tvTop = (TextView) contextView.findViewById(R.id.tv_top);
		tvButtom = (TextView) contextView.findViewById(R.id.tv_buttom);

		order_list_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("折叠按钮");
				// -----
				if (!OpenMoreReserveInfoPanelTag) {
					more_reserve_info_layout.setVisibility(View.VISIBLE);
					san_jiao.setBackgroundResource(R.drawable.san_jiao_up);
					OpenMoreReserveInfoPanelTag = true;
				} else {
					more_reserve_info_layout.setVisibility(View.GONE);
					san_jiao.setBackgroundResource(R.drawable.san_jiao_down);
					OpenMoreReserveInfoPanelTag = false;
				}
			}
		});

		canReportPriceErrorTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("小票面板-报错按钮");
				// -----

				Bundle data = new Bundle();
				ErrorReportTypeData defaultErrorData = new ErrorReportTypeData();
				defaultErrorData.setTypeId("-999"); // -999就餐金额报错
				defaultErrorData.setTypeName("就餐金额报错");
				defaultErrorData.setInputBoxTitle("请输入正确的就餐金额");
				defaultErrorData.setFuncTag(-999); // -999表示就餐金额报错
				defaultErrorData.setKeyboardTypeTag(2);// 输入方式是数字键盘
				data.putSerializable("ErrorReportTypeData", defaultErrorData);
				data.putInt("typeTag", 1);
				// data.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
				// Settings.MY_ORDER_DETAIL_ACTIVITY);
				data.putString(Settings.UUID, orderId);// orderId

				// /---------------------------------------------------------------
				// ActivityUtil.jump(context, UserCenterActivity.class,
				// fromPage, data);
				// ActivityUtil.jump(context, SendSMSActivity.class,
				// fromPage, data);
				ActivityUtil.jump(NewMyOrderDetailActivity.this, ErrorReportActivity.class, 0, data);
			}
		});

		canEditTag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("修改订单按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
				bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
				bundle.putString(Settings.BUNDLE_REST_NAME, restNameAndAddress);
				bundle.putLong(Settings.BUNDLE_ORDER_TIME, reserveTimes);
				bundle.putLong(Settings.BUNDLE_ORDER_PEOPLE_NUM, peopleNums);
				bundle.putLong(Settings.BUNDLE_ORDER_ROOM_TYPE, restTypes);
				bundle.putInt(Settings.BUNDLE_BOOK_ORDER_TAG, 1);
				ActivityUtil.jump(NewMyOrderDetailActivity.this, MyBookRestaurantActivity.class, 0, bundle);
			}
		});
		canCancelTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showDialog(NewMyOrderDetailActivity.this, R.layout.order_cancel_dialog_item, new DialogUtil.DialogEventListener() {

					@Override
					public void onInit(View contentView, final PopupWindow dialog) {
						// TODO Auto-generated method stub
						final Button plane_change = (Button) contentView.findViewById(R.id.plane_change);
						final Button change_rest = (Button) contentView.findViewById(R.id.change_rest);
						final EditText order_cancel_content = (EditText) contentView.findViewById(R.id.order_cancel_content);
						Button order_cancel_no = (Button) contentView.findViewById(R.id.order_cancel_no);
						Button order_cancel_ok = (Button) contentView.findViewById(R.id.order_cancel_ok);

						plane_change.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								if (reasonTypeTag == 1) {
									reasonTypeTag = 0;
									plane_change.setBackgroundResource(R.drawable.cancel_order_no_kuang);
								} else {
									reasonTypeTag = 1;
									plane_change.setBackgroundResource(R.drawable.cancel_order_gou_kuang);
								}
								change_rest.setBackgroundResource(R.drawable.cancel_order_no_kuang);
							}
						});

						change_rest.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								if (reasonTypeTag == 2) {
									reasonTypeTag = 0;
									change_rest.setBackgroundResource(R.drawable.cancel_order_no_kuang);
								} else {
									reasonTypeTag = 2;
									change_rest.setBackgroundResource(R.drawable.cancel_order_gou_kuang);
								}
								plane_change.setBackgroundResource(R.drawable.cancel_order_no_kuang);
							}
						});

						order_cancel_ok.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								reasonMemo = order_cancel_content.getText().toString();
								ServiceRequest request = new ServiceRequest(API.cancelOrder2);
								request.addData("orderId", orderId);
								request.addData("reasonTypeTag", reasonTypeTag);// 原因类别////
																				// 0:未选择1、计划有变////
																				// 2、更换餐厅
								request.addData("reasonMemo", reasonMemo);// 原因备注
								// -----
								OpenPageDataTracer.getInstance().addEvent("撤消订单按钮");
								// -----
								CommonTask.request(request, "正在取消订单，请等待...", new CommonTask.TaskListener<SimpleData>() {

									@Override
									protected void onSuccess(SimpleData dto) {
										// -----
										OpenPageDataTracer.getInstance().endEvent("撤消订单按钮");
										// -----
										DialogUtil.showToast(NewMyOrderDetailActivity.this, "取消订单成功!");
										Fg114Application.isNeedUpdate = true;
										refreshUI();
										refreshUIResume();
									};

									protected void onError(int code, String message) {
										// -----
										OpenPageDataTracer.getInstance().endEvent("撤消订单按钮");
										// -----
										DialogUtil.showToast(NewMyOrderDetailActivity.this, message);
									};
								});
								dialog.dismiss();
							}
						});

						order_cancel_no.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View view) {
								// TODO Auto-generated method stub
								ViewUtils.preventViewMultipleClick(view, 1000);
								dialog.dismiss();

							}
						});
					}
				});
			}

		});

	}

	private void initData(final OrderInfoData2 dto) {
		// --------------是否显示金额面板------------------
       if(dto==null){
	      return;
       }
		// 显示金额面板
		if (dto.showPricePanelTag) {
			// 显示整个大的金额面板的布局
			showPricePanelTagTrue.setVisibility(View.VISIBLE);
			// 可以上传小票
			if (dto.canUploadReceiptTag) {
				// 显示上传小票的布局
				canUploadReceiptTagLayout.setVisibility(View.VISIBLE);
				// 隐藏预览小票的布局
				showPricePanelTagFalse.setVisibility(View.GONE);
				// 设置上传小票的提示
				receiptHint.setText(dto.receiptHint);
				// 点击上传小票
				canUploadReceiptTag.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						uploadTicketPicture();
					}
				});
			}
			// 不用上传小票
			else {
				canUploadReceiptTagLayout.setVisibility(View.GONE);
				showPricePanelTagFalse.setVisibility(View.VISIBLE);

				detailResIvResPic.setImageByUrl(dto.receiptUrl, true, 0, ScaleType.FIT_CENTER);
				detailResIvResPic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						DialogUtil.createImageViewPanel(NewMyOrderDetailActivity.this, showPricePanelTagFalse, dto.bigReceiptUrl);
					}
				});
			}

			// 可以输入金额
			if (dto.canInputPriceTag) {
				canInputPriceTagTrue.setVisibility(View.VISIBLE);
				canInputPriceTagFalse.setVisibility(View.GONE);
				inputPriceHint.setText(dto.inputPriceHint);
				// TODO 提交金额按钮
				priceConfirm.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// ewr
						ViewUtils.preventViewMultipleClick(v, 1000);
						String prices = price.getText().toString().trim();
						if (CheckUtil.isEmpty(prices)) {
							DialogUtil.showToast(NewMyOrderDetailActivity.this, "请输入就餐金额");
						} else {

							ServiceRequest request = new ServiceRequest(API.postOrderPrice);
							request.addData("orderId", orderId);
							request.addData("price", prices);

							// -----
							OpenPageDataTracer.getInstance().addEvent("小票面板-提交金额按钮");
							// -----

							CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<SimpleData>() {
								// TODO
								@Override
								protected void onSuccess(SimpleData dto) {
									// -----
									OpenPageDataTracer.getInstance().endEvent("小票面板-提交金额按钮");
									// -----
									refreshUIResume();
									DialogUtil.showToast(NewMyOrderDetailActivity.this, "提交就餐金额成功");
								};

								protected void onError(int code, String message) {
									// -----
									OpenPageDataTracer.getInstance().endEvent("小票面板-提交金额按钮");
									// -----
									DialogUtil.showToast(NewMyOrderDetailActivity.this, message);
								};
							});
						}
					}
				});

			}
			// 不可以输入金额
			else {
				canInputPriceTagTrue.setVisibility(View.GONE);
				canInputPriceTagFalse.setVisibility(View.VISIBLE);
				repastPrice.setText(Html.fromHtml(getColor(dto.priceHint)));
				canReportPriceErrorTag.setVisibility(dto.canReportPriceErrorTag ? View.VISIBLE : View.GONE);
			}
		}
		// 不显示金额面板
		else {
			// 隐藏整个大的金额面板的布局
			showPricePanelTagTrue.setVisibility(View.GONE);
		}

		if (dto.orderHintData != null) {
			OrderHintData2 orderHintData = dto.orderHintData;
			// -----订单详细信息----
			if (!CheckUtil.isEmpty(orderHintData.statusIconUrl)) {
				icon_url.setImageByUrl(orderHintData.statusIconUrl, true, 0, ScaleType.FIT_XY);
			}
			if (!CheckUtil.isEmpty(orderHintData.statusName)) {
				order_detail_status_name.setText(Html.fromHtml(orderHintData.statusName));
			} else {
				order_detail_status_name.setText("");
			}
			if (!CheckUtil.isEmpty(orderHintData.restName)) {
				order_detail_rest_name.setText(orderHintData.restName);
			} else {
				order_detail_rest_name.setText("");
			}
			if (!CheckUtil.isEmpty(orderHintData.reserveInfo)) {
				order_detail_reserve_info.setText(Html.fromHtml(orderHintData.reserveInfo));
			} else {
				order_detail_reserve_info.setText("");
			}

		} else {
			finish();
			return;
		}
		// -----订餐的描述----
		if (CheckUtil.isEmpty(dto.funcHint)) {
			func_hint_layout.setVisibility(View.GONE);
			func_hint.setText("");
		} else {
			func_hint_layout.setVisibility(View.VISIBLE);
			func_hint.setText(Html.fromHtml(dto.funcHint.toString()));
		}

		// 按钮之类
		if (dto.canCancelTag) {
			canCancelTag.setVisibility(View.VISIBLE);
		} else {
			canCancelTag.setVisibility(View.GONE);
		}
		// TODO DEBUG
		if (dto.canEditTag) {
			// if (true) {
			canEditTag.setVisibility(View.VISIBLE);
		} else {
			canEditTag.setVisibility(View.GONE);
		}
		if (CheckUtil.isEmpty(dto.operateHint)) {
			operate_hint.setVisibility(View.GONE);
		} else {
			operate_hint.setVisibility(View.VISIBLE);
			operate_hint.setText(Html.fromHtml(dto.operateHint));
		}

		// 预订信息
		if (dto.moreReserveInfoList != null || dto.moreReserveInfoList.size() != 0) {
			more_reserve_info_layout.setVisibility(View.VISIBLE);
			if (more_reserve_info_layout.getChildCount() != 0) {
				more_reserve_info_layout.removeAllViews();
			}
			List<OrderMoreReserveInfoData> orderMoreReserveInfoData = dto.moreReserveInfoList;

			for (int i = 0; i < orderMoreReserveInfoData.size(); i++) {
				LayoutInflater inflater = LayoutInflater.from(NewMyOrderDetailActivity.this);
				View view = inflater.inflate(R.layout.more_reserve_info_item, null);
				TextView name = (TextView) view.findViewById(R.id.name);
				TextView detail = (TextView) view.findViewById(R.id.detail);

				name.setText(orderMoreReserveInfoData.get(i).name);

				detail.setText(orderMoreReserveInfoData.get(i).detail);

				more_reserve_info_layout.addView(view);
			}

		} else {
			more_reserve_info_layout.setVisibility(View.GONE);
		}

		if (dto.needOpenMoreReserveInfoPanelTag) {
			more_reserve_info_layout.setVisibility(View.VISIBLE);
			san_jiao.setBackgroundResource(R.drawable.san_jiao_up);
			OpenMoreReserveInfoPanelTag = true;
		} else {
			more_reserve_info_layout.setVisibility(View.GONE);
			san_jiao.setBackgroundResource(R.drawable.san_jiao_down);
			OpenMoreReserveInfoPanelTag = false;
		}

		// --------------------------------------------
		if (dto.funcBtnList == null || dto.funcBtnList.size() == 0) {
			// Log.e(TAG, "View.GONE");
			inviteLayout.clearAnimation();
			inviteLayout.removeAllViews();
			inviteLayout.setVisibility(View.GONE);
		} else {
			inviteLayout.setVisibility(View.VISIBLE);
			inviteLayout.removeAllViews();
			List<OrderFuncBtnData> list = dto.funcBtnList;

			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					LayoutInflater inflater = LayoutInflater.from(NewMyOrderDetailActivity.this);
					View view = inflater.inflate(R.layout.my_order_share_item, null);
					MyImageView myOrderDetailImg = (MyImageView) view.findViewById(R.id.my_order_detail_img);
					inviteFriendLayout = (View) view.findViewById(R.id.invite_friend_layout);
					TextView myOrderDetailTxt = (TextView) view.findViewById(R.id.my_order_detail_txt);
					View myOrderDetailLine = view.findViewById(R.id.my_order_detail_line);
					ImageView inviteFriendImgArrow = (ImageView) view.findViewById(R.id.invite_friend_img_arrow);
					final OrderFuncBtnData orderFuncBtnData = list.get(i);
					// orderHintBtnData.enableTag=true;
					// orderHintBtnData.typeTag=1;
					switch (orderFuncBtnData.typeTag) {
					// 1：添加到日历 2:收藏餐厅 3：分享 100以上：后台自定义
					// 1：添加到日历 android 隐藏
					case 1:
						//
						// myOrderDetailTxt.setTag(1);
						// // 后台传过来的URL不为空
						// if (!CheckUtil.isEmpty(orderFuncBtnData.iconUrl)) {
						// myOrderDetailImg.setImageByUrl(orderFuncBtnData.iconUrl,
						// true, 0, ScaleType.FIT_CENTER);
						// }
						break;
					// 2:收藏餐厅
					case 2:
						myOrderDetailTxt.setTag(orderFuncBtnData.typeTag);
						if (!CheckUtil.isEmpty(orderFuncBtnData.iconUrl)) {
							myOrderDetailImg.setImageByUrl(orderFuncBtnData.iconUrl, true, 0, ScaleType.FIT_CENTER);
						} else {
							// 显示本地的图片
							myOrderDetailImg.setVisibility(View.INVISIBLE);
						}
						
						inviteFriendLayout.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								ServiceRequest request = new ServiceRequest(API.addRestToFav);
								request.addData("restId", restaurantId);// 餐馆ID
								// -----
								OpenPageDataTracer.getInstance().addEvent("收藏按钮");
								// -----
								CommonTask.request(request, "收藏中...", new CommonTask.TaskListener<SimpleData>() {

									@Override
									protected void onSuccess(SimpleData dto) {
										// -----
										OpenPageDataTracer.getInstance().endEvent("收藏按钮");
										// -----
										NewMyOrderDetailActivity.this.getBtnOption().setSelected(true);
										DialogUtil.showToast(NewMyOrderDetailActivity.this, "收藏成功");
									}

									@Override
									protected void onError(int code, String message) {
										super.onError(code, message);
										// -----
										OpenPageDataTracer.getInstance().endEvent("收藏按钮");
										// -----
									}

									// private void doTest_confirm() {
									// String json =
									// "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
									// SimpleData data =
									// JsonUtils.fromJson(json,
									// SimpleData.class);
									// onSuccess(data);
									//
									// }
								});
							}
						});
						break;
					// 3：分享
					case 3:
						myOrderDetailTxt.setTag(orderFuncBtnData.typeTag);
						if (!CheckUtil.isEmpty(orderFuncBtnData.iconUrl)) {
							myOrderDetailImg.setImageByUrl(orderFuncBtnData.iconUrl, true, 0, ScaleType.FIT_CENTER);
						} else {
							// 显示本地的图片
							myOrderDetailImg.setVisibility(View.INVISIBLE);
						}
						inviteFriendLayout.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								shareInfoData = orderFuncBtnData.shareInfoData;
								showShareDialog(100);
							}
						});
						break;
					default:
						// 100以上：后台自定义
						myOrderDetailTxt.setTag(orderFuncBtnData.typeTag);
						if (!CheckUtil.isEmpty(orderFuncBtnData.iconUrl)) {
							myOrderDetailImg.setImageByUrl(orderFuncBtnData.iconUrl, true, 0, ScaleType.FIT_CENTER);
						} else {
							// 显示本地的图片
							myOrderDetailImg.setVisibility(View.INVISIBLE);
						}
						inviteFriendLayout.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								performOrderDetailBtnClick(orderFuncBtnData);
							}
						});
						break;
					}

					inviteFriendLayout.setClickable(orderFuncBtnData.enableTag);
					inviteFriendImgArrow.setVisibility(orderFuncBtnData.enableTag ? View.VISIBLE : View.INVISIBLE);
					if (orderFuncBtnData.enableTag && orderFuncBtnData.needFlashTag) {
						Animation animation = AnimationUtils.loadAnimation(NewMyOrderDetailActivity.this, R.anim.flicker);
						myOrderDetailImg.setAnimation(animation);
						animation.start();
					}
					// myOrderDetailTxt.setTextColor(orderHintBtnData.enableTag
					// ?getResources().getColor(R.color.text_color_black):getResources().getColor(R.color.text_color_gray));
					myOrderDetailTxt.setText(Html.fromHtml(orderFuncBtnData.name));
					if (orderFuncBtnData.typeTag != 1) {
						// 不需要添加日历 android不用
						inviteLayout.addView(view);
					}
					if (i == (list.size() - 1)) {
						myOrderDetailLine.setVisibility(View.GONE);
					}
				}
			}
		}

	}

	private void executePostOrderFuncBtnClick(int typeTag) {
		ServiceRequest request = new ServiceRequest(API.postOrderFuncBtnClick);
		request.addData("typeTag", typeTag);
		request.addData("orderId", orderId);
		CommonTask.requestMutely(request, new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
			};
		});
	}

	/**
	 * 格式 2013年7月25日 9点23
	 * 
	 * @return
	 */
	private String[] getData(long reserveTime) {
		String[] data = new String[5];
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		Date date = new Date(reserveTime);
		calendar.setTime(date);
		data[0] = calendar.get(Calendar.YEAR) + "";
		data[1] = calendar.get(Calendar.MONTH) + "";
		data[2] = calendar.get(Calendar.DAY_OF_MONTH) + "";
		data[3] = calendar.get(Calendar.HOUR_OF_DAY) + "";
		data[4] = calendar.get(Calendar.MINUTE) + "";
		return data;
	}

	private String getColor(String msg) {
		String color = null;
		Pattern pattern = Pattern.compile("#+\\w{6}");
		Matcher matcher = pattern.matcher(msg);
		while (matcher.find()) {
			color = msg.replaceAll(matcher.group(), "\'" + matcher.group() + "\'");
		}
		return color;
	}

	/**
	 * 上传图片
	 * 
	 * @param imageView
	 */
	public void uploadTicketPicture() {

		takePic(new OnShowUploadImageListener() {

			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				if (!CheckUtil.isEmpty(path)) {
					com.fg114.main.app.Settings.uploadPictureUri = "";
					executeUploadTicketPictureTask(path);
				}
			}

		}, false);
	}

	Bitmap tempUserPicture;
	private FileInputStream input = null;

	private void executeUploadTicketPictureTask(final String path) {
		ServiceRequest request = new ServiceRequest(API.uploadOrderReceipt);
		try {
			String tempPath = ActivityUtil.getGPSPicturePath(path);
			input = new FileInputStream(tempPath);
			request.addData(input);
		} catch (IOException e) {
			DialogUtil.showToast(NewMyOrderDetailActivity.this, "请稍后再次尝试");
			e.printStackTrace();
			if (input != null) {
				try {
					input.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			return;
		}
		request.addData("orderId", orderId);
		// ----------------------------
		fromActivityResult = true;
		OpenPageDataTracer.getInstance().enterPage("订单详情", orderId, false);
		// ----------------------------
		OpenPageDataTracer.getInstance().addEvent("小票面板-上传小票按钮");
		// -----

		CommonTask.request(request, "正在上传...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				OpenPageDataTracer.getInstance().endEvent("小票面板-上传小票按钮");
				// -----
				canUploadReceiptTagLayout.setVisibility(View.GONE);
				showPricePanelTagFalse.setVisibility(View.VISIBLE);

				// detailResIvResPic.setImageByUrl(dto.getPicUrl(), true, 0,
				// ScaleType.FIT_CENTER);
				refreshUIResume();
				detailResIvResPic.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						DialogUtil.createImageViewPanel(NewMyOrderDetailActivity.this, showPricePanelTagFalse, dto.getPicUrl());
					}
				});
			};

			protected void onError(int code, String message) {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				OpenPageDataTracer.getInstance().endEvent("小票面板-上传小票按钮");
				// -----
				DialogUtil.showToast(NewMyOrderDetailActivity.this, message);
			};
		});
	}

	private void positionBack() {
		content.postDelayed(new Runnable() {

			@Override
			public void run() {
				int c = content.getScrollY();
				content.scrollTo(0, (0 - c) / 50);
				if (c != 0) {
					content.postDelayed(this, 50);
				}
			}
		}, 50);
	}

	private void startLoadAnimation(ImageView imageView) {
		Animation animation = AnimationUtils.loadAnimation(NewMyOrderDetailActivity.this, R.anim.animloading);
		imageView.setAnimation(animation);
		animation.start();
	}

	private ImageView imageView;
	private int type;

	private void executeData(int type) {

		switch (type) {
		case 0:
			// Toast.makeText(MyOrderDetailActivity.this, "开始加载上面的数据",
			// 50).show();

			isLoadTopData = true;
			isLoadButtomData = false;
			isOperateTop = true;
			isDragTop = false;
			isOperateButtom = false;
			isUp = false;
			if (CheckUtil.isEmpty(prevOrderId)) {
				DialogUtil.showToast(NewMyOrderDetailActivity.this, "您没有上一张订单");
				isLoadTopData = false;
				isUp = true;
				isDragTop = false;
				isOperateTop = true;
				isOperateButtom = true;
				isLoadButtomData = false;
				positionBack();
			} else {
				content.scrollTo(0, -(int) UnitUtil.dip2px(60));
				ServiceRequest request = new ServiceRequest(API.getOrderInfo2);
				request.addData("queryTypeTag", 2);
				request.addData("orderId", prevOrderId);
				CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<OrderInfoData2>() {

					@Override
					protected void onSuccess(OrderInfoData2 dto) {
						if(dto!=null){
						isLoadTopData = false;
						isUp = true;
						isDragTop = false;
						isOperateTop = true;
						isOperateButtom = true;
						isLoadButtomData = false;
						orderId = dto.orderHintData.orderId;
						OpenMoreReserveInfoPanelTag = dto.needOpenMoreReserveInfoPanelTag;
						prevOrderId = dto.prevOrderId;
						nextOrderId = dto.nextOrderId;

						if (CheckUtil.isEmpty(prevOrderId)) {
							hasPrevOrderId = false;
						} else {
							hasPrevOrderId = true;

						}
						if (CheckUtil.isEmpty(nextOrderId)) {
							hasNextOrderId = false;
						} else {
							hasNextOrderId = true;

						}
						initData(dto);
						showDialog(dto);
						reserveTimes = dto.reserveTime;
						restaurantId = dto.orderHintData.restId;
						restNameAndAddress = dto.orderHintData.restName;
						peopleNums = dto.peopleNum;
						restTypes = dto.roomTypeTag;
						positionBack();
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								height = line.getTop() + 1;
							}
						}, 1);
					}
					};
					protected void onError(int code, String message) {
						// doTest();
						DialogUtil.showToast(NewMyOrderDetailActivity.this, message);
						positionBack();
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								height = line.getTop() + 1;
							}
						}, 1);
					};
				});
			}
			break;
		case 1:
			// Toast.makeText(MyOrderDetailActivity.this, "开始加载下面的数据",
			// 50).show();
			// content.scrollTo(0, content.getScrollY());
			isLoadTopData = false;
			isLoadButtomData = true;
			isOperateTop = false;
			isOperateButtom = true;
			isDragButtom = false;
			isUp = false;
			if (CheckUtil.isEmpty(nextOrderId)) {
				DialogUtil.showToast(NewMyOrderDetailActivity.this, "您没有下一张订单");
				isLoadTopData = false;
				isUp = true;
				isDragTop = false;
				isOperateTop = true;
				isOperateButtom = true;
				isLoadButtomData = false;
				positionBack();
			} else {
				content.scrollTo(0, (int) UnitUtil.dip2px(70));
				ServiceRequest request1 = new ServiceRequest(API.getOrderInfo);
				request1.addData("queryTypeTag", 2);
				request1.addData("orderId", nextOrderId);
				CommonTask.request(request1, "正在加载...", new CommonTask.TaskListener<OrderInfoData2>() {

					@Override
					protected void onSuccess(OrderInfoData2 dto) {
						if(dto!=null){
						isUp = true;
						isLoadTopData = false;
						isDragButtom = false;
						isLoadButtomData = false;
						isOperateTop = true;
						isOperateButtom = true;
						initData(dto);
						showDialog(dto);
						prevOrderId = dto.prevOrderId;
						nextOrderId = dto.nextOrderId;
						if (CheckUtil.isEmpty(prevOrderId)) {
							hasPrevOrderId = false;
						} else {
							hasPrevOrderId = true;

						}
						if (CheckUtil.isEmpty(nextOrderId)) {
							hasNextOrderId = false;
						} else {
							hasNextOrderId = true;
						}
						orderId = dto.orderHintData.orderId;
						OpenMoreReserveInfoPanelTag = dto.needOpenMoreReserveInfoPanelTag;
						reserveTimes = dto.reserveTime;
						restaurantId = dto.orderHintData.restId;
						restNameAndAddress = dto.orderHintData.restName;
						peopleNums = dto.peopleNum;
						restTypes = dto.roomTypeTag;
						positionBack();
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								height = line.getTop() + 1;
							}
						}, 1);
					}
					};
					protected void onError(int code, String message) {
						DialogUtil.showToast(NewMyOrderDetailActivity.this, message);
						positionBack();
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								height = line.getTop() + 1;
							}
						}, 1);
					};

				});
			}
			break;
		}

	}

	private class callPicView implements OnClickListener {
		private View parentView;
		private String url;

		public callPicView(View parentView, String url) {
			this.parentView = parentView;
			this.url = url;
		}

		@Override
		public void onClick(View v) {
			DialogUtil.createImageViewPanel(NewMyOrderDetailActivity.this, this.parentView, url);
		}

	}

	private void performOrderDetailBtnClick(final OrderFuncBtnData orderFuncBtnData) {
		// -----
		OpenPageDataTracer.getInstance().addEvent(" 功能面板-按钮点击", orderFuncBtnData.typeTag + "");
		// -----
		executePostOrderFuncBtnClick(orderFuncBtnData.typeTag);
		URLExecutor.execute(orderFuncBtnData.actionXmsUrl, NewMyOrderDetailActivity.this, 0);
	}

	private void showDialog(final OrderInfoData2 orderInfoData2) {
		if (orderInfoData2.needOpenConfirmDlgTag) {
			DialogUtil.showAlert(NewMyOrderDetailActivity.this, true, orderInfoData2.cdMsg, orderInfoData2.cdCancelBtnName, orderInfoData2.cdOkBtnName, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}

			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					URLExecutor.execute(orderInfoData2.cdActionXmsUrl, NewMyOrderDetailActivity.this, 0);
					//
					dialog.dismiss();
				}

			});
		}
	}
}
