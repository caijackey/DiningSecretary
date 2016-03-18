package com.fg114.main.app.activity.order;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.BubbleHintData;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CouponOrderStateData;
import com.fg114.main.service.dto.MainPageInfoPackDTO;
import com.fg114.main.service.dto.MainPageOtherInfoPackDTO;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderHintData2;
import com.fg114.main.service.dto.OrderList2DTO;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutOrderHintData;
import com.fg114.main.service.dto.TakeoutOrderHintData2;
import com.fg114.main.service.dto.TakeoutOrderList2DTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 订单 Settings.BUNDLE_FROM_TAG 外卖 传1 其他传0
 * 
 * @author dengxiangyu
 * 
 */
public class NewOrderListAcitivy extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView orderListView;
	private TextView orderlistTv;
	private Button restOrderBnt;
	private Button takeAwayOrderBnt;
	private TextView userRestOrderNum;
	private TextView userTakeAwayOrderNum;
	private Button btnGoBack;
	private Button orderlist_drop_down_box;
	private List<CommonTypeDTO> statusList;
	private List<CommonTypeDTO> operateList;
	private View title_layout;

	private int fromTag;

	private String statusId = "";
	private String operateId = "";

	private static final String TAG_TYPE_status = "status";
	private static final String TAG_TYPE_operate = "operate";
	private List<RfTypeDTO> mTopList = new ArrayList<RfTypeDTO>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单列表", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(Settings.BUNDLE_FROM_TAG)) {
				fromTag = bundle.getInt(Settings.BUNDLE_FROM_TAG);
			} else {
				fromTag = 0;
			}
		}

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
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// // 设置标题栏
		// this.getTvTitle().setText("订单");
		// this.getBtnGoBack().setText("返回");
		// // ---
		// this.getBtnOption().setVisibility(View.INVISIBLE); // 先不显示

		this.getTitleLayout().setVisibility(View.GONE);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.new_orderlist, null);
		btnGoBack = (Button) contextView.findViewById(R.id.btnGoBack);
		orderListView = (ListView) contextView.findViewById(R.id.order_list_status);
		orderlistTv = (TextView) contextView.findViewById(R.id.orderlist_tv);
		restOrderBnt = (Button) contextView.findViewById(R.id.orderlist_rest_order_bnt);
		takeAwayOrderBnt = (Button) contextView.findViewById(R.id.orderlist_take_away_order_bnt);
		userRestOrderNum = (TextView) contextView.findViewById(R.id.orderlist_rest_user_order_number);
		userTakeAwayOrderNum = (TextView) contextView.findViewById(R.id.orderlist_take_away_user_order_number);
		orderlist_drop_down_box = (Button) contextView.findViewById(R.id.orderlist_drop_down_box);
		title_layout = contextView.findViewById(R.id.title_layout);

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		btnGoBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				finish();
			}
		});

		orderlist_drop_down_box.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 500);
				// -----
				showTopFilter();
			}
		});

		restOrderBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅订单按钮");
				// -----

				fromTag = 0;

				orderlist_drop_down_box.setVisibility(View.VISIBLE);
				restOrderBnt.setBackgroundResource(R.drawable.order_list_white_left);
				restOrderBnt.setTextColor(getResources().getColor(R.color.new_text_color_red));
				takeAwayOrderBnt.setBackgroundResource(R.drawable.order_list_red_right);
				takeAwayOrderBnt.setTextColor(getResources().getColor(R.color.text_color_white));
				initRestOrderListAdapter();
			}
		});
		takeAwayOrderBnt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("外卖订单按钮");
				// -----

				fromTag = 1;
				orderlist_drop_down_box.setVisibility(View.INVISIBLE);
				restOrderBnt.setBackgroundResource(R.drawable.order_list_red_left);
				restOrderBnt.setTextColor(getResources().getColor(R.color.text_color_white));
				takeAwayOrderBnt.setBackgroundResource(R.drawable.order_list_white_right);
				takeAwayOrderBnt.setTextColor(getResources().getColor(R.color.new_text_color_red));
				initTakeAwayOrderListAdapter();
			}
		});

		orderListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// -----
				OpenPageDataTracer.getInstance().addEvent("选择行");
				// -----

				// if (parent.getAdapter().getItem(position) != null) {
				// if (fromTag == 1) {
				// Bundle bundle = new Bundle();
				// bundle.putString(Settings.BUNDLE_ORDER_ID,
				// ((TakeoutOrderHintData)
				// parent.getAdapter().getItem(position)).orderId);
				// ActivityUtil.jump(OrderListActivity.this,
				// MyTakeAwayOrderDetailActivity.class, 0, bundle);
				// } else {
				// Bundle bundle = new Bundle();
				// bundle.putString(Settings.BUNDLE_ORDER_ID, ((OrderHintData)
				// parent.getAdapter().getItem(position)).orderId);
				// ActivityUtil.jump(OrderListActivity.this,
				// MyOrderDetailActivity.class, 0, bundle);
				// }
				// }
			}
		});

		orderListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (fromTag == 0) {
			orderlist_drop_down_box.setVisibility(View.VISIBLE);
			restOrderBnt.setBackgroundResource(R.drawable.order_list_white_left);
			restOrderBnt.setTextColor(getResources().getColor(R.color.new_text_color_red));
			takeAwayOrderBnt.setBackgroundResource(R.drawable.order_list_red_right);
			takeAwayOrderBnt.setTextColor(getResources().getColor(R.color.text_color_white));
			initRestOrderListAdapter();
		} else {
			orderlist_drop_down_box.setVisibility(View.INVISIBLE);
			restOrderBnt.setBackgroundResource(R.drawable.order_list_red_left);
			restOrderBnt.setTextColor(getResources().getColor(R.color.text_color_white));
			takeAwayOrderBnt.setBackgroundResource(R.drawable.order_list_white_right);
			takeAwayOrderBnt.setTextColor(getResources().getColor(R.color.new_text_color_red));
			initTakeAwayOrderListAdapter();
		}
		Fg114Application.isNeedUpdate = true;
		showUserOrderNumMessage();
	}

	// 更新功能系统消息（订单数量，站内信等）
	protected void updateSystemMessage() {
		showUserOrderNumMessage();
	}
	int orderNum=0 ;
	int takeoutOrderNum=0;
	private void showUserOrderNumMessage() {

		MainPageOtherInfoPackDTO mainPageInfoPackDTO = SessionManager.getInstance().getMainPageOtherInfoPackDTO();
		BubbleHintData bubbleHintData = mainPageInfoPackDTO.bubbleHintData;		 
		if(bubbleHintData!=null){
		orderNum = bubbleHintData.orderNum;
		takeoutOrderNum = bubbleHintData.takeoutOrderNum;
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (orderNum > 0) {
					userRestOrderNum.setVisibility(View.VISIBLE);
					userRestOrderNum.setText(orderNum + "");
				} else {
					userRestOrderNum.setVisibility(View.INVISIBLE);
				}
				if (takeoutOrderNum > 0) {
					userTakeAwayOrderNum.setVisibility(View.VISIBLE);
					userTakeAwayOrderNum.setText(takeoutOrderNum + "");
				} else {
					userTakeAwayOrderNum.setVisibility(View.INVISIBLE);
				}
			}
		}, 100);

	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单列表", "");
		// ----------------------------
	}

	private void initRestOrderListAdapter() {
		/*
		 * View view = (View)orderListView.getTag(R.id.listviewadapter_key); if
		 * (view!=null) { orderListView.removeFooterView(view); }
		 */
		ListViewAdapter<OrderHintData2> adapter = new ListViewAdapter<OrderHintData2>(R.layout.new_order_list_item, new ListViewAdapter.OnAdapterListener<OrderHintData2>() {
			@Override
			public void onLoadPage(final ListViewAdapter<OrderHintData2> adapter, final int startIndex, int pageSize) {
				ServiceRequest request = new ServiceRequest(API.getOrderList2);
				request.addData("statusId", statusId);
				request.addData("startIndex", startIndex);
				request.addData("pageSize", pageSize);
				// -----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----
				CommonTask.request(request, "", new CommonTask.TaskListener<OrderList2DTO>() {

					@Override
					protected void onSuccess(OrderList2DTO dto) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----

						List<OrderHintData2> orderHintDataList = dto.list;
						statusList = dto.statusList;
						operateList = dto.operateList;
						doData();

						if (startIndex == 1) {
							if (orderHintDataList != null && orderHintDataList.size() != 0) {
								orderlistTv.setVisibility(View.GONE);
								orderListView.setVisibility(View.VISIBLE);
							} else {
								orderlistTv.setVisibility(View.VISIBLE);
								orderListView.setVisibility(View.GONE);
							}
						}
						ListViewAdapter.AdapterDto<OrderHintData2> adapterDto = new ListViewAdapter.AdapterDto<OrderHintData2>();
						adapterDto.setList(dto.list);
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);

					};

					protected void onError(int code, String message) {
						DialogUtil.showToast(getApplicationContext(), message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
						// adapter.onTaskFail();
						// onSuccess(getJson());
					};
				});

			}

			@Override
			public void onRenderItem(ListViewAdapter<OrderHintData2> adapter, ViewHolder holder, final OrderHintData2 data) {

				MyImageView iconUrl = holder.$myIv(R.id.icon_url);
				TextView restName = holder.$tv(R.id.order_list_rest_name);
				TextView statusName = holder.$tv(R.id.order_list_status_name);
				View order_list_bt = holder.$(R.id.order_list_bt);
				TextView order_list_reserve_info = holder.$tv(R.id.order_list_reserve_info);

				if (!CheckUtil.isEmpty(data.restName)) {
					restName.setText(data.restName);
				}
				if (!CheckUtil.isEmpty(data.statusName)) {
					statusName.setText(Html.fromHtml(data.statusName));
				}
				if (!CheckUtil.isEmpty(data.reserveInfo)) {
					order_list_reserve_info.setText(Html.fromHtml(data.reserveInfo));
				}

				iconUrl.setImageByUrl(data.statusIconUrl, true, 0, ScaleType.CENTER_CROP);

				order_list_bt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, data.orderId);
						ActivityUtil.jump(NewOrderListAcitivy.this, NewMyOrderDetailActivity.class, 0, bundle);
					}
				});

			};
		});
		adapter.setExistPage(true);
		adapter.setmCtx(NewOrderListAcitivy.this);
		adapter.setListView(orderListView);
	}

	// 获得外卖订单列表，返回TakeoutOrderListDTO
	private void initTakeAwayOrderListAdapter() {
		/*
		 * View view = (View)orderListView.getTag(R.id.listviewadapter_key); if
		 * (view!=null) { orderListView.removeFooterView(view); }
		 */

		ListViewAdapter<TakeoutOrderHintData2> adapter = new ListViewAdapter<TakeoutOrderHintData2>(R.layout.new_order_list_item, new ListViewAdapter.OnAdapterListener<TakeoutOrderHintData2>() {
			@Override
			public void onLoadPage(final ListViewAdapter<TakeoutOrderHintData2> adapter, final int startIndex, int pageSize) {
				ServiceRequest request = new ServiceRequest(API.getTakeoutOrderList2);
				request.addData("startIndex", startIndex);
				request.addData("pageSize", pageSize);
				// -----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----
				CommonTask.request(request, "", new CommonTask.TaskListener<TakeoutOrderList2DTO>() {

					@Override
					protected void onSuccess(TakeoutOrderList2DTO dto) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----

						List<TakeoutOrderHintData2> orderHintDataList = dto.list;

						if (startIndex == 1) {
							if (orderHintDataList != null && orderHintDataList.size() != 0) {
								orderlistTv.setVisibility(View.GONE);
								orderListView.setVisibility(View.VISIBLE);
							} else {
								orderlistTv.setVisibility(View.VISIBLE);
								orderListView.setVisibility(View.GONE);
							}
						}
						ListViewAdapter.AdapterDto<TakeoutOrderHintData2> adapterDto = new ListViewAdapter.AdapterDto<TakeoutOrderHintData2>();
						adapterDto.setList(dto.list);
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);
					};

					protected void onError(int code, String message) {
						DialogUtil.showToast(getApplicationContext(), message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
						// adapter.onTaskFail();
						// onSuccess(getTakeoutOrderListDTO());
					};
				});

			}

			@Override
			public void onRenderItem(ListViewAdapter<TakeoutOrderHintData2> adapter, ViewHolder holder, final TakeoutOrderHintData2 data) {

				MyImageView iconUrl = holder.$myIv(R.id.icon_url);
				TextView restName = holder.$tv(R.id.order_list_rest_name);
				TextView statusName = holder.$tv(R.id.order_list_status_name);
				View order_list_bt = holder.$(R.id.order_list_bt);
				TextView order_list_reserve_info = holder.$tv(R.id.order_list_reserve_info);

				if (!CheckUtil.isEmpty(data.takeoutName)) {
					restName.setText(data.takeoutName);
				}
				if (!CheckUtil.isEmpty(data.statusName)) {
					statusName.setText(Html.fromHtml(data.statusName));
				}
				if (!CheckUtil.isEmpty(data.reserveInfo)) {
					order_list_reserve_info.setText(Html.fromHtml(data.reserveInfo));
				}

				iconUrl.setImageByUrl(data.statusIconUrl, true, 0, ScaleType.CENTER_CROP);

				order_list_bt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ViewUtils.preventViewMultipleClick(v, 1000);
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, data.orderId);
						ActivityUtil.jump(NewOrderListAcitivy.this, MyNewTakeAwayOrderDetailActivity.class, 0, bundle);
					}
				});

			};
		});
		// adapter.getFooterView().setVisibility(View.GONE);
		adapter.setExistPage(true);
		adapter.setmCtx(NewOrderListAcitivy.this);
		adapter.setListView(orderListView);
	}

	/**
	 * 清空订单
	 */
	private void executOperateOrder() {
		ServiceRequest request = new ServiceRequest(API.operateOrder);
		request.addData("operateId", operateId);

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {
				DialogUtil.showAlert(NewOrderListAcitivy.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialoginterface, int i) {
						// TODO Auto-generated method stub
						initRestOrderListAdapter();
					}
				});

			}

			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	/**
	 * 显示榜单条件的筛选框
	 */
	private void showTopFilter() {
		if (mTopList == null || mTopList.size() == 0) {
			DialogUtil.showAlert(this, "提示", "您选择的条件下没有订单类别");
			return;
		}
		DialogUtil.showSelectionListViewDropDown(title_layout, mTopList, new OnSelectedListener() {

			@Override
			public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
				// 获得查询结果

				if (mainData.getMemo().equals(TAG_TYPE_status)) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("选择排序");
					// -----
					statusId = mainData.getUuid();
					initRestOrderListAdapter();
				}
				if (mainData.getMemo().equals(TAG_TYPE_operate)) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("选择操作");
					// -----
					operateId = mainData.getUuid();
					DialogUtil.showAlert(NewOrderListAcitivy.this, true, "确定" + mainData.getName() + "吗", "确定", "取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialoginterface, int i) {
							// TODO Auto-generated method stub
							executOperateOrder();
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialoginterface, int i) {
							// TODO Auto-generated method stub
							dialoginterface.dismiss();
						}
					});

				}
			}
		}, new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
	}

	/**
	 * 数据转换
	 * 
	 * @return
	 */
	private void doData() {
		// if (statusList == null&&operateList==null) {
		// return;
		// }
		if (mTopList.size() != 0) {
			mTopList.clear();
		}

		if (statusList != null) {
			for (int i = 0; i < statusList.size(); i++) {
				RfTypeDTO rfTypeListDTO = new RfTypeDTO();
				rfTypeListDTO.n = statusList.get(i).getName();
				rfTypeListDTO.u = statusList.get(i).getUuid();
				rfTypeListDTO.setSelectTag(statusList.get(i).isSelectTag());
				rfTypeListDTO.setMemo(TAG_TYPE_status);
				mTopList.add(rfTypeListDTO);
			}
		}
		if (operateList != null && operateList.size() != 0) {
			RfTypeDTO operationDto = new RfTypeDTO();
			operationDto.setUuid("");
			operationDto.setName("操作");
			operationDto.setList(null);

			mTopList.add(operationDto);
			for (int i = 0; i < operateList.size(); i++) {
				RfTypeDTO rfTypeListDTO = new RfTypeDTO();
				rfTypeListDTO.n = operateList.get(i).getName();
				rfTypeListDTO.u = operateList.get(i).getUuid();
				rfTypeListDTO.setSelectTag(operateList.get(i).isSelectTag());
				rfTypeListDTO.setMemo(TAG_TYPE_operate);
				mTopList.add(rfTypeListDTO);
			}
		}

	}

	private OrderList2DTO getJson() {
		String json = "{\"hintOrderNum\":\"1\",\"statusList\":[{\"uuid\":\"111\",\"parentId\":\"111\",\"name\":\"全部订单\",\"num\":\"0\",\"selectTag\":\"true\",\"isFirst\":\"true\",\"keywords\":\"\",\"firstLetters\":\"\",\"firstLetter\":\"\",\"memo\":\"111\"},{\"uuid\":\"111\",\"parentId\":\"111\",\"name\":\"全部无效订单\",\"num\":\"0\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"\",\"firstLetters\":\"\",\"firstLetter\":\"\",\"memo\":\"111\"},{\"uuid\":\"111\",\"parentId\":\"111\",\"name\":\"全部失败订单\",\"num\":\"0\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"\",\"firstLetters\":\"\",\"firstLetter\":\"\",\"memo\":\"111\"},{\"uuid\":\"111\",\"parentId\":\"111\",\"name\":\"全部成功订单\",\"num\":\"0\",\"selectTag\":\"false\",\"isFirst\":\"false\",\"keywords\":\"\",\"firstLetters\":\"\",\"firstLetter\":\"\",\"memo\":\"111\"}],\"list\":[{\"orderId\":\"d111\",\"statusIconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"restId\":\"re111\",\"restName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"}]}";
		OrderList2DTO dto = JsonUtils.fromJson(json, OrderList2DTO.class);
		return dto;
	}
	//
	// private TakeoutOrderListDTO getTakeoutOrderListDTO() {
	// String json =
	// "{\"hintOrderNum\":\"1\",\"list\":[{\"orderId\":\"d111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"takeoutId\":\"re111\",\"takeoutName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"hint\":\"2份 共30元\",\"reserveTime\":\"06月10日 18:50\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"},{\"orderId\":\"d111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"takeoutId\":\"re111\",\"takeoutName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"hint\":\"2份 共30元\",\"reserveTime\":\"06月10日 18:50\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"},{\"orderId\":\"d111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"takeoutId\":\"re111\",\"takeoutName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"hint\":\"2份 共30元\",\"reserveTime\":\"06月10日 18:50\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"},{\"orderId\":\"d111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"takeoutId\":\"re111\",\"takeoutName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"reserveTime\":\"06月10日 18:50\",\"hint\":\"2份 共30元\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"},{\"orderId\":\"d111\",\"iconUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=d002209f43a7d933bfa8e3739973d313/8718367adab44aedf1c2a46fb21c8701a08bfba9.jpg\",\"takeoutId\":\"re111\",\"takeoutName\":\"好吃点餐厅\",\"reserveInfo\":\"06月10日 18:50\",\"reserveTime\":\"06月10日 18:50\",\"hint\":\"2份 共30元\",\"statusName\":\"xxxxx<font color=#000000>xxx</font>xxx\"}]}";
	// TakeoutOrderListDTO dto = JsonUtils.fromJson(json,
	// TakeoutOrderListDTO.class);
	// return dto;
	// }

}
