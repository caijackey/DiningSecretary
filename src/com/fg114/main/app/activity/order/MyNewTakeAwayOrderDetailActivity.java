package com.fg114.main.app.activity.order;

import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.resandfood.RecommendRestaurantCommentListActivity;
import com.fg114.main.app.activity.takeaway.NewTakeAwayRestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayBuyPaymentActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayRestaurantCommentActivity;
import com.fg114.main.app.activity.takeaway.TakeoutOrderCommentActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.RestTelInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutInfoData2;
import com.fg114.main.service.dto.TakeoutMenuSelData;
import com.fg114.main.service.dto.TakeoutOrderInfoData;
import com.fg114.main.service.dto.TakeoutOrderInfoData2;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.ViewUtils;
import com.google.xiaomishujson.Gson;

/**
 * 订单详情 orderId
 * 
 * @author sunquan1
 */
public class MyNewTakeAwayOrderDetailActivity extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	private TextView orderhint;

	private TextView reserveTime;
	private TextView restName;
	private Button callBnt;
	private Button ordercommentbtn;

	private TextView priceHint;
	private TextView statusName;
	private LinearLayout new_take_away_order_takeout_info_layout;
	private TextView orderpeople;
	private TextView address;
	private TextView tel;
	private LinearLayout delicaciesFoodLayout;

	// 评论
	private LinearLayout newtakeaway_detail_res_plLayout;
	private LinearLayout newtakeaway_comment_infoLayout;
	private MyImageView newtakeaway_comment_userphoto;
	private TextView newtakeaway_comment_tvUser;
	private TextView newtakeaway_comment_tvTime;
	private TextView newtakeaway_comment_detail;
	private TextView newtakeaway_comment_back;
	private RatingBar newtakeaway_comment_rating;
	private LinearLayout newtakeaway_comment_backlayout;
	private LinearLayout new_takeaway_order_list_layout;
	private View commentbackline;

	public List<TakeoutMenuSelData> list=null;
	private RelativeLayout new_take_away_order_bottom_layout;
	private Button cancelorderbtn;
	private Button orderpaybtn;

	private String orderId;
	private String takeoutId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖订单详情", "");
		// ----------------------------

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null && bundle.containsKey(Settings.BUNDLE_ORDER_ID)) {
			orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		} else {
			finish();
		}

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 初始化界面
		initComponent();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖订单详情", "");
		// ----------------------------
	}

	// TODO 初始化
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("外卖订单详情");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("餐厅详情");
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.new_take_away_orderdetail, null);
		orderhint = (TextView) contextView.findViewById(R.id.new_take_away_top_hint);

		reserveTime = (TextView) contextView.findViewById(R.id.new_take_away_reserve_time);
		restName = (TextView) contextView.findViewById(R.id.new_take_away_order_restname);
		callBnt = (Button) contextView.findViewById(R.id.new_take_away_btn_call);

		ordercommentbtn = (Button) contextView.findViewById(R.id.new_take_away_btn_comment);

		priceHint = (TextView) contextView.findViewById(R.id.new_take_away_order_price);
		statusName = (TextView) contextView.findViewById(R.id.new_take_away_order_status_name);
		// 美食列表
		new_takeaway_order_list_layout = (LinearLayout) contextView.findViewById(R.id.new_takeaway_order_dis_listinfo);
		delicaciesFoodLayout = (LinearLayout) contextView.findViewById(R.id.new_take_away_order_food_list);
		// 外送信息
		new_take_away_order_takeout_info_layout = (LinearLayout) contextView.findViewById(R.id.new_take_away_order_takeout_info_layout);
		orderpeople = (TextView) contextView.findViewById(R.id.new_take_away_order_name);
		address = (TextView) contextView.findViewById(R.id.new_take_away_order_address);
		tel = (TextView) contextView.findViewById(R.id.new_take_away_order_tel);

		// 评论
		newtakeaway_detail_res_plLayout = (LinearLayout) contextView.findViewById(R.id.new_take_away_order_plLayout);
		newtakeaway_comment_infoLayout = (LinearLayout) contextView.findViewById(R.id.new_take_away_order_newresCommentLoadingLayout);

		newtakeaway_comment_userphoto = (MyImageView) contextView.findViewById(R.id.new_take_away_order_comment_userphoto);
		newtakeaway_comment_tvUser = (TextView) contextView.findViewById(R.id.new_take_away_order_comment_tvUser);
		newtakeaway_comment_tvTime = (TextView) contextView.findViewById(R.id.new_take_away_order_comment_tvTime);

		newtakeaway_comment_detail = (TextView) contextView.findViewById(R.id.new_take_away_order_comment_tvComment);
		newtakeaway_comment_rating = (RatingBar) contextView.findViewById(R.id.new_take_away_order_commentrating_bar);
		commentbackline = (View) contextView.findViewById(R.id.new_take_away_order_comment_line);
		newtakeaway_comment_backlayout = (LinearLayout) contextView.findViewById(R.id.new_take_away_order_comment_backlayout);
		newtakeaway_comment_back = (TextView) contextView.findViewById(R.id.new_take_away_order_comment_back);
		new_take_away_order_bottom_layout = (RelativeLayout) contextView.findViewById(R.id.new_take_away_order_bottom_layout);
		orderpaybtn = (Button) contextView.findViewById(R.id.new_take_away_order_pay_btn);
		cancelorderbtn = (Button) contextView.findViewById(R.id.new_take_away_order_cancel_btn);

		contextView.setVisibility(View.GONE);
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// ----
				OpenPageDataTracer.getInstance().addEvent("餐厅详情按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, takeoutId);
				ActivityUtil.jump(MyNewTakeAwayOrderDetailActivity.this, NewTakeAwayRestaurantDetailActivity.class, 0, bundle);
			}
		});

		// delicaciesFoodLayout.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// // ----
		// OpenPageDataTracer.getInstance().addEvent("美食列表按钮");
		// // -----
		//
		// Bundle bundle = new Bundle();
		// bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
		// bundle.putInt(Settings.FROM_TAG, 2);
		// ActivityUtil.jump(MyNewTakeAwayOrderDetailActivity.this,
		// TakeAwayDelicaciesFoodListActivity.class, 0, bundle);
		// }
		// });

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		excuteTakeoutOrderInfoTask();
	}

	// 获得外卖订单详情，返回TakeoutOrderInfoData
	private void excuteTakeoutOrderInfoTask() {

		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		ServiceRequest request = new ServiceRequest(API.getTakeoutOrderInfo2);
		request.addData("orderId", orderId);
		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<TakeoutOrderInfoData2>() {

			@Override
			protected void onSuccess(TakeoutOrderInfoData2 dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				contextView.setVisibility(View.VISIBLE);
				takeoutId = dto.orderHintData.takeoutId;

				refreshUI(dto);
			};

			protected void onError(int code, String message) {
                super.onError(code, message);
				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				finish();
//				textTakeoutOrderInfoData();
				// MyNewTakeAwayOrderDetailActivity.this.finish();
				// DialogUtil.showToast(getApplicationContext(), message);
			};

			// --------------------测试------------------------------
			// 获得外卖订单详情，返回TakeoutOrderInfoData
			private void textTakeoutOrderInfoData() {
				// String json =
				// "{\"hintOrderNum\":\"10\",\"orderHintData\":{\"orderId\":\"o111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"takeoutId\":\"t111\",\"takeoutName\":\"湘味餐馆\",\"reserveTime\":\"06月10日 18:50\",\"statusName\":\"xxxxx<font color=#111111>xxx</font>xxx\",\"hint\":\"2份 共30元\"},\"menuInfo\":\"牛肉面(￥21/1份)\",\"userReceiveAdressData\":{\"uuid\":\"111\",\"parentId\":\"111\",\"name\":\"浦东大道117号\",\"num\":\"1\",\"succTag\":\"true\",\"phone\":\"13000000000\",\"memo\":\"备注\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"关键词\",\"firstLetters\":\"首字母\",\"firstLetter\":\"firstLetter\"},\"canTelTag\":\"true\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"}],\"operateBtnTag\":\"1\"}";
				String json = "{\"hintOrderNum\":\"2\",\"orderHintData\":{\"orderId\":\"o111\",\"iconUrl\":\"http: //h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"takeoutId\":\"t111\",\"takeoutName\":\"湘味餐馆\",\"reserveTime\":\"06月10日18: 50\",\"statusName\":\"xxxxx<fontcolor=#111111>xxx</font>xxx\",\"hint\":\"2份共30元\"},\"canTelTag\":\"true\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"},{\"isTelCanCall\":\"true\",\"tel\":\"13000000000\",\"cityPrefix\":\"010\",\"branch\":\"57575777\"}],\"canCommentTag\":\"true\",\"hint\":\"餐馆信息中的电话号码列表餐馆信息中的电话号码列表餐馆信息中的电话号码列表餐馆信息中的电话号码列表\",\"menuSelPack\":{\"list\":[{\"dataIdentifer\":\"1212\",\"typeTag\":\"1\",\"canSelGiftTag\":\"true\",\"giftTypeId\":\"1313\",\"uuid\":\"010\",\"name\":\"最高级霜降雪花和牛四喜锅贴香辣麻辣锅\",\"nameColor\":\"\",\"price\":\"200\",\"num\":\"10\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[{\"uuid\":\"1212\",\"name\":\"好吃\",\"list\":[{\"uuid\":\"010\",\"name\":\"麻辣锅\",\"price\":\"100\"}]}],\"selPropertyHint\":\"(可乐+冰块可乐+可乐+冰块可乐+可乐+冰块可乐+可乐+冰块可乐\"},{\"dataIdentifer\":\"1212\",\"typeTag\":\"1\",\"canSelGiftTag\":\"true\",\"giftTypeId\":\"1313\",\"uuid\":\"010\",\"name\":\"最高级霜降雪花和牛四喜锅贴香辣麻辣锅\",\"nameColor\":\"\",\"price\":\"200\",\"num\":\"10\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[{\"uuid\":\"1212\",\"name\":\"好吃\",\"list\":[{\"uuid\":\"010\",\"name\":\"麻辣锅\",\"price\":\"100\"}]}],\"selPropertyHint\":\"\"},{\"dataIdentifer\":\"1212\",\"typeTag\":\"1\",\"canSelGiftTag\":\"true\",\"giftTypeId\":\"1313\",\"uuid\":\"010\",\"name\":\"最高级霜降雪花和牛四喜锅贴香辣麻辣锅\",\"nameColor\":\"\",\"price\":\"200\",\"num\":\"10\",\"canShowNumTag\":\"true\",\"canChangeNumTag\":\"true\",\"propertyTypeList\":[{\"uuid\":\"1212\",\"name\":\"好吃\",\"list\":[{\"uuid\":\"010\",\"name\":\"麻辣锅\",\"price\":\"100\"}]}],\"selPropertyHint\":\"\"}]},\"userReceiveAdressData\":{\"uuid\":\"111\",\"name\":\"少仲\",\"address\":\"上海市浦东区东方路133号订餐小秘书2楼技术部\",\"tel\":\"13000000000\"},\"commentData\":{\"uuid\":\"o111\",\"userName\":\"a111\",\"userPicUrl\":\"http: //h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"createTime\":\"2014-12-1212: 12\",\"overallNum\":\"12\",\"detail\":\"真的超级好吃好喝好玩，快来玩吧吗，一起嗨上天吧\",\"replyInfo\":\"谢谢评价\"},\"canCancelTag\":\"true\",\"needOnlinePayTag\":\"true\"}";
				TakeoutOrderInfoData2 dto = new Gson().fromJson(json, TakeoutOrderInfoData2.class);
				onSuccess(dto);

			}
		});

		// TakeoutOrderInfoData2 dto=textTakeoutOrderInfoData();
		// if(dto!=null){
		// Log.i("sunquan1", "adja akjdaw我就科技爱好");
		// }
		// contextView.setVisibility(View.VISIBLE);
		// takeoutId = dto.orderHintData.takeoutId;
		//
		// refreshUI(dto);
	}

	private void refreshUI(final TakeoutOrderInfoData2 dto) {
		if (dto != null) {
            if(dto.hint!=null){
            	if (!TextUtils.isEmpty(dto.hint)) {
            		orderhint.setText(Html.fromHtml(dto.hint));
				} else {
					orderhint.setVisibility(View.GONE);
				}
            }else{
            	orderhint.setVisibility(View.GONE);
            }
			
			reserveTime.setText(dto.orderHintData.reserveTime);
			restName.setText(dto.orderHintData.takeoutName);
			priceHint.setText(dto.orderHintData.hint);
			statusName.setText(Html.fromHtml(dto.orderHintData.statusName));

			// 外送信息
			if (dto.userReceiveAdressData == null) {
				new_take_away_order_takeout_info_layout.setVisibility(View.GONE);
			} else {
				address.setText(dto.userReceiveAdressData.address);
				tel.setText(dto.userReceiveAdressData.tel);
				orderpeople.setText(dto.userReceiveAdressData.name);
			}

			// 取消订单
			if (dto.canCancelTag) {
				cancelorderbtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						excuteCancelTakeoutOrderTask();
					}
				});
			} else {
				cancelorderbtn.setVisibility(View.GONE);
			}
			// 立刻付款
			if (dto.needOnlinePayTag) {
				orderpaybtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// excuteCancelTakeoutOrderTask();
						ViewUtils.preventViewMultipleClick(v, 1000);
						// -----
						OpenPageDataTracer.getInstance().addEvent("立刻付款按钮");
						// -----

						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
						ActivityUtil.jump(MyNewTakeAwayOrderDetailActivity.this, TakeAwayBuyPaymentActivity.class, 0, bundle);
					}
				});
			} else {
				orderpaybtn.setVisibility(View.GONE);
			}
			if (!dto.canCancelTag && !dto.needOnlinePayTag) {
				new_take_away_order_bottom_layout.setVisibility(View.GONE);
			}

			if (dto.canTelTag) {
				callBnt.setVisibility(View.VISIBLE);
			} else {
				callBnt.setVisibility(View.GONE);
			}

			if (dto.canCommentTag) {
				ordercommentbtn.setVisibility(View.VISIBLE);
				ordercommentbtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						// ----
						OpenPageDataTracer.getInstance().addEvent("点评按钮");
						// -----
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, orderId);
						ActivityUtil.jump(MyNewTakeAwayOrderDetailActivity.this, TakeoutOrderCommentActivity.class, 0, bundle);

					}
				});
			} else {
				ordercommentbtn.setVisibility(View.GONE);
			}

			if (dto != null) {
				// 评论部位
				if (dto.commentData != null) {
					if(CheckUtil.isEmpty(dto.commentData.uuid)&&CheckUtil.isEmpty(dto.commentData.userName)&&CheckUtil.isEmpty(dto.commentData.detail)&&CheckUtil.isEmpty(dto.commentData.createTime)){
						newtakeaway_detail_res_plLayout.setVisibility(View.GONE);
					}else{
					newtakeaway_detail_res_plLayout.setVisibility(View.VISIBLE);
					}

					newtakeaway_comment_userphoto.setImageByUrl(dto.commentData.userPicUrl, true, 0, ScaleType.CENTER_CROP);

					newtakeaway_comment_tvUser.setText(dto.commentData.userName);

					newtakeaway_comment_tvTime.setText(dto.commentData.createTime);
					// 评论内容
					if (CheckUtil.isEmpty(dto.commentData.detail)) {
						newtakeaway_comment_detail.setText(R.string.text_layout_dish_no_comment);
					} else {
						newtakeaway_comment_detail.setText(dto.commentData.detail);
					}
					// 餐厅回复

					if (dto.commentData.showOverallNumTag) {
						newtakeaway_comment_rating.setVisibility(View.VISIBLE);
						newtakeaway_comment_rating.setMinimumHeight(1);
						newtakeaway_comment_rating.setRating((float) dto.commentData.overallNum);
					} else {
						newtakeaway_comment_rating.setVisibility(View.GONE);
					}

					if (TextUtils.isEmpty(dto.commentData.replyInfo)) {
						commentbackline.setVisibility(View.GONE);
						newtakeaway_comment_backlayout.setVisibility(View.GONE);
					} else {
						newtakeaway_comment_backlayout.setVisibility(View.VISIBLE);
						newtakeaway_comment_back.setText(dto.commentData.replyInfo);
					}
					
					
					

				} else {					
					newtakeaway_detail_res_plLayout.setVisibility(View.GONE);
				}

			}
			// 去评论页面
			newtakeaway_comment_infoLayout.setOnClickListener(new AdapterView.OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(view, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("查看点评按钮");
					// -----

					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_ID, orderId);
					bundle.putString("fromTag", 2 + "");
					ActivityUtil.jump(MyNewTakeAwayOrderDetailActivity.this, TakeAwayRestaurantCommentActivity.class, 0, bundle);
				}

			});
			callBnt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					final List<RestTelInfo> phoneList = dto.telList;

					// 没有电话时，相当于“有奖报错”
					if (phoneList == null) {
						getBtnOption().performClick();
						return;
					}
					// 只有一个电话，直接拨打
					if (phoneList.size() == 1) {

						// ----
						OpenPageDataTracer.getInstance().addEvent("电话按钮", phoneList.get(0).tel);
						// -----

						if (CheckUtil.isEmpty(phoneList.get(0).cityPrefix)) {
							dialPhone(phoneList.get(0).tel);
						} else {
							dialPhone(phoneList.get(0).cityPrefix + phoneList.get(0).tel);
						}
						return;
					}
					// 有多个电话，弹出拨打列表供用户选择
					Builder bd = new Builder(MyNewTakeAwayOrderDetailActivity.this);
					// 设置title
					bd.setTitle("拨打餐厅电话");
					bd.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

					// ---------------
					// 生成列表项文字
					int length = phoneList.size();
					String[] itemTexts = new String[length];
					for (int i = 0; i < length; i++) {
						if (CheckUtil.isEmpty(phoneList.get(i).branch)) {
							itemTexts[i] = "电话" + (i + 1) + ": " + phoneList.get(i).tel;
						} else {
							itemTexts[i] = "电话" + (i + 1) + ": " + phoneList.get(i).tel + "-" + phoneList.get(i).branch;
						}
					}
					bd.setItems(itemTexts, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							// ----
							OpenPageDataTracer.getInstance().addEvent("电话按钮", phoneList.get(which).tel);
							// -----

							if (CheckUtil.isEmpty(phoneList.get(which).cityPrefix)) {
								dialPhone(phoneList.get(which).tel);
							} else {
								dialPhone(phoneList.get(which).cityPrefix + phoneList.get(which).tel);
							}
							dialog.dismiss();
						}
					});
					bd.show();

				}
			});
			// 构建美食列表
			buildOrderFoodList(dto);
		} else {
			return;
		}
	}

	/**
	 * 构建美食列表
	 */
	private void buildOrderFoodList(TakeoutOrderInfoData2 takeoutInfo2) {
		delicaciesFoodLayout.removeAllViews();
		if(takeoutInfo2.menuSelPack.list==null||takeoutInfo2.menuSelPack==null||takeoutInfo2==null){
			return;
		}
		
		list = takeoutInfo2.menuSelPack.list;
		if (list.size() == 0) {
			new_takeaway_order_list_layout.setVisibility(View.GONE);
		}
		for (int i = 0; i < list.size(); i++) {
			TakeoutMenuSelData temp = list.get(i);
			addPromotion(temp, i);
		}
	}

	// 动态添加美食视图
	private void addPromotion(final TakeoutMenuSelData temp, int currentPosition) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.new_take_away_orderfood_list_item, null);
		View line = (View) item.findViewById(R.id.new_takeaway_horizontal_line);
		TextView title = (TextView) item.findViewById(R.id.new_order_foodlist_foodname);
		TextView num = (TextView) item.findViewById(R.id.new_order_foodnum);
		TextView price = (TextView) item.findViewById(R.id.new_order_foodprice);
		TextView transcation = (TextView) item.findViewById(R.id.new_order_food_transcation);
		// 美食信息
		title.setText(temp.name);
		num.setText(temp.num + "");
		price.setText("￥" + temp.price);
		if (TextUtils.isEmpty(temp.selPropertyHint)) {
			transcation.setVisibility(View.GONE);
		} else {
			transcation.setVisibility(View.VISIBLE);
			transcation.setText(temp.selPropertyHint);
		}

		// blockbuttonLayout.setPadding(0, 0, 0, 0);
		line.setVisibility(currentPosition == 0 ? View.GONE : View.VISIBLE);
		delicaciesFoodLayout.addView(item);

	}

	// 拨打一个电话，并向后台传输拨打信息
	private void dialPhone(final String dialPhone) {
		final String userPhone = SessionManager.getInstance().getUserInfo(MyNewTakeAwayOrderDetailActivity.this).getTel();
		// 拨打电话
		ActivityUtil.callSuper57(MyNewTakeAwayOrderDetailActivity.this, dialPhone);
		// 向后台传拨打数据
		new Thread(new Runnable() {
			public void run() {
				try {
					ServiceRequest.callTel(3, orderId, dialPhone);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
		}).start();
	}

	// DialogUtil.showToast(getApplicationContext(), dto.getMsg());

	private String dealWithMenuInfo(String menuInfo) {
		return menuInfo.replace("、", "、\n");

	}

	private void excuteCancelTakeoutOrderTask() {
		// ----
		OpenPageDataTracer.getInstance().addEvent("取消订单按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.cancelTakeoutOrder);
		request.addData("orderId", orderId);
		CommonTask.request(request, "正在取消...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("取消订单按钮");
				// -----
				excuteTakeoutOrderInfoTask();
				DialogUtil.showToast(getApplicationContext(), dto.getMsg());
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("取消订单按钮");
				// -----

				DialogUtil.showToast(getApplicationContext(), message);
			};
		});
	}

}
