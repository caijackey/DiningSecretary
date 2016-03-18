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
import android.widget.LinearLayout;
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
import com.fg114.main.app.activity.Mdb.MdbReceiptChkConfirmDataActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.CommonTypeDTO;

import com.fg114.main.service.dto.MdbFreeOrderHintData;
import com.fg114.main.service.dto.MdbFreeOrderListDTO;

import com.fg114.main.service.dto.OrderList2DTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.SimpleData;

import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;

import com.fg114.main.util.ViewUtils;

/**
 * 订单列表
 * 
 * @author dengxiangyu
 * 
 */
public class MdbOrderListActivity extends MainFrameActivity {

	// 界面组件
	private LayoutInflater mdb_mInflater;
	private View mdb_contextView;
	private ListView mdb_orderListView;
	private TextView mdb_orderlistTv;
	private Button mdb_btnGoBack;
	private Button mdb_orderlist_drop_down_box;
	private List<CommonTypeDTO> statusList;
	private List<CommonTypeDTO> operateList;
	private View mdb_title_layout;

	private String statusId = "";
	private String operateId = "";

	private static final String TAG_TYPE_status = "status";
	private static final String TAG_TYPE_operate = "operate";
	private List<RfTypeDTO> mTopList = new ArrayList<RfTypeDTO>();

	private boolean hasLogined;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("订单列表", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {

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
		this.getTvTitle().setText("免单宝订单");
		this.getBtnGoBack().setVisibility(View.VISIBLE);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		// // ---
		// this.getBtnOption().setVisibility(View.INVISIBLE); // 先不显示

		this.getTitleLayout().setVisibility(View.GONE);

		// 内容部分
		mdb_mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mdb_contextView = mdb_mInflater.inflate(R.layout.mdb_order_list_act, null);
		mdb_btnGoBack = (Button) mdb_contextView.findViewById(R.id.mdb_btnGoBack);
		mdb_orderListView = (ListView) mdb_contextView.findViewById(R.id.mdb_order_list_status);
		mdb_orderlistTv = (TextView) mdb_contextView.findViewById(R.id.mdb_orderlist_tv);

		mdb_orderlist_drop_down_box = (Button) mdb_contextView.findViewById(R.id.mdb_orderlist_drop_down_box);
		mdb_title_layout = mdb_contextView.findViewById(R.id.mdb_title_layout);

		this.getMainLayout().addView(mdb_contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		mdb_btnGoBack.setOnClickListener(new OnClickListener() {

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

		mdb_orderlist_drop_down_box.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 500);
				// -----
				showTopFilter();
			}
		});

		mdb_orderListView.setOnItemClickListener(new OnItemClickListener() {

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

		mdb_orderListView.setOnScrollListener(new OnScrollListener() {

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

		initRestOrderListAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 判断是否已登录并调整界面显示
		hasLogined = SessionManager.getInstance().isUserLogin(this);

		Fg114Application.isNeedUpdate = true;
	}

	@Override
	public void onRestart() {
		super.onRestart();

	}

	private void initRestOrderListAdapter() {
		/*
		 * View view = (View)orderListView.getTag(R.id.listviewadapter_key); if
		 * (view!=null) { orderListView.removeFooterView(view); }
		 */
		ListViewAdapter<MdbFreeOrderHintData> adapter = new ListViewAdapter<MdbFreeOrderHintData>(R.layout.new_order_list_item, new ListViewAdapter.OnAdapterListener<MdbFreeOrderHintData>() {
			@Override
			public void onLoadPage(final ListViewAdapter<MdbFreeOrderHintData> adapter, final int startIndex, int pageSize) {
				ServiceRequest request = new ServiceRequest(API.getMdbFreeOrderList);
				request.addData("statusId", statusId);
				request.addData("startIndex", startIndex);
				request.addData("pageSize", pageSize);
				// -----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----
				CommonTask.request(request, "", new CommonTask.TaskListener<MdbFreeOrderListDTO>() {

					@Override
					protected void onSuccess(MdbFreeOrderListDTO dto) {
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----

						List<MdbFreeOrderHintData> orderHintDataList = dto.list;
						statusList = dto.statusList;
						operateList = dto.operateList;
						doData();

						if (startIndex == 1) {
							if (orderHintDataList != null && orderHintDataList.size() != 0) {
								mdb_orderlistTv.setVisibility(View.GONE);
								mdb_orderListView.setVisibility(View.VISIBLE);
							} else {
								mdb_orderlistTv.setVisibility(View.VISIBLE);
								mdb_orderListView.setVisibility(View.GONE);
							}
						}
						ListViewAdapter.AdapterDto<MdbFreeOrderHintData> adapterDto = new ListViewAdapter.AdapterDto<MdbFreeOrderHintData>();
						adapterDto.setList(dto.list);
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);
						

					};

					protected void onError(int code, String message) {
						DialogUtil.showToast(getApplicationContext(), message);
						finish();
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
						// adapter.onTaskFail();
						// onSuccess(getJson());
					};
				});

			}

			@Override
			public void onRenderItem(ListViewAdapter<MdbFreeOrderHintData> adapter, ViewHolder holder, final MdbFreeOrderHintData data) {
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
				if (!CheckUtil.isEmpty(data.orderHint)) {
					order_list_reserve_info.setText(Html.fromHtml(data.orderHint));
				}

				iconUrl.setImageByUrl(data.statusIconUrl, true, 0, ScaleType.CENTER_CROP);

				order_list_bt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// TODO Auto-generated method stub
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_ORDER_ID, data.orderId);
						bundle.putString(Settings.BUNDLE_REST_ID, data.restId);

						if (hasLogined) {
							if(data.needCompleteOrderTag){
								ActivityUtil.jump(MdbOrderListActivity.this, MdbReceiptChkConfirmDataActivity.class, 0, bundle);
							}else{
							ActivityUtil.jump(MdbOrderListActivity.this, MdbOrderDetailActivity.class, 0, bundle);
							}
						} else {
							DialogUtil.showToast(MdbOrderListActivity.this, "您未登录,请先登录");
							ActivityUtil.jump(MdbOrderListActivity.this, UserLoginActivity.class, 0);
						}
					}
				});
			};
		});
		adapter.setExistPage(true);
		adapter.setmCtx(MdbOrderListActivity.this);
		adapter.setListView(mdb_orderListView);
	}

	/**
	 * 清空订单
	 */
	private void executOperateOrder() {
		ServiceRequest request = new ServiceRequest(API.operateMdbFreeOrder);
		request.addData("operateId", operateId);

		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(final SimpleData dto) {
				DialogUtil.showAlert(MdbOrderListActivity.this, false, dto.getMsg(), new DialogInterface.OnClickListener() {

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
		DialogUtil.showSelectionListViewDropDown(mdb_title_layout, mTopList, new OnSelectedListener() {

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
					DialogUtil.showAlert(MdbOrderListActivity.this, true, "确定" + mainData.getName() + "吗", "确定", "取消", new DialogInterface.OnClickListener() {

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
			RfTypeDTO operationDto = new RfTypeDTO();
			operationDto.setUuid("");
			operationDto.setName("选择排序方式");
			operationDto.setList(null);

			mTopList.add(operationDto);
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
