package com.fg114.main.app.activity.takeaway;

import java.util.List;

import org.xml.sax.DTDHandler;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.RatingBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;

import com.fg114.main.app.activity.resandfood.RestaurantCommentDetailActivity;

import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;

import com.fg114.main.service.dto.CommentData;

import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.TakeoutCommentData;
import com.fg114.main.service.dto.TakeoutCommentListDTO;
import com.fg114.main.service.dto.TakeoutRestListDTO;

import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;

import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.JsonUtils;

import com.fg114.main.util.SessionManager;

/**
 * 外卖餐厅评论列表界面 来自哪里：（ 1 外卖餐厅详情页 2订单页） 传入:1 外卖餐厅ID 2 订单Id
 * 
 * @author dengxiangyu
 * 
 */
public class TakeAwayRestaurantCommentActivity extends MainFrameActivity {

	private static final String TAG = "TakeAwayRestaurantCommentActivity";
	// 传入参数
	private String takeAwayRestaurantId; // 餐厅ID
	private String takeAwayOrderId; // orderID
	private String fromTag;
	// 本地缓存数据
	private RestInfoData takeAwayRestaurantInfo;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView lvCommentList;
	// private TakeAwayCommentAdapter adapter;
	private ListViewAdapter<TakeoutCommentData> adapter;

	// 控制变量
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;

	// 任务

	private List<TakeoutCommentData> commentDataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅评论列表", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		takeAwayRestaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		fromTag = bundle.getString("fromTag");
		takeAwayOrderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		this.bundleData.putString(Settings.BUNDLE_KEY_ID, takeAwayRestaurantId);
		// 获得缓存的餐厅信息
		takeAwayRestaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, takeAwayRestaurantId);

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

		initListView();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅评论列表", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		String title = null;
		if(takeAwayRestaurantInfo!=null){
		title = takeAwayRestaurantInfo.name;
		}
		if (TextUtils.isEmpty(title)) {
			title = "看点评";
		}
		this.getTvTitle().setText(title);
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_comment, null);
		lvCommentList = (ListView) contextView.findViewById(R.id.res_comment_lvCommentList);

		lvCommentList.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("滚动");
					// -----

					// if (isLast == false) {
					// executeGetResCommentListTask(startIndex,10);//定死了 页面大小10
					// }
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// if (firstVisibleItem + visibleItemCount == totalItemCount) {
				// // 当到达列表尾部时
				// isRefreshFoot = true;
				// startIndex = startIndex + 1;
				// } else {
				// isRefreshFoot = false;
				// }
			}
		});
//		// 点击进入评论详细页
//		lvCommentList.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				// CommentData data = (CommentData)
				// arg0.getItemAtPosition(arg2);
				// if (data != null
				// && !String.valueOf(Settings.CONTRL_ITEM_ID).equals(
				// data.uuid)) {
				// -----
//				OpenPageDataTracer.getInstance().addEvent("选择行");
				// -----

				// Bundle bundle = new Bundle();
				// bundle.putSerializable(Settings.BUNDLE_REST_COMMENT_DATA,
				// data);
				// bundle.putString(Settings.BUNDLE_REST_ID,
				// takeAwayRestaurantId);
				// ActivityUtil.jump(TakeAwayRestaurantCommentActivity.this,
				// RestaurantCommentDetailActivity.class, 0, bundle);
//			}
			// }
//		});
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// 设置Mainframe中的Bundle信息，用于弹出餐厅报错时使用
		if (takeAwayRestaurantInfo != null) {
			bundleData.putString(Settings.BUNDLE_REST_NAME, takeAwayRestaurantInfo.name);
			bundleData.putDouble(Settings.BUNDLE_REST_LONGITUDE, takeAwayRestaurantInfo.longitude);
			bundleData.putDouble(Settings.BUNDLE_REST_LATITUDE, takeAwayRestaurantInfo.latitude);
		}

	}

	/**
	 * 获得外卖餐厅评论列表
	 */
	private void initListView() {
		if (lvCommentList != null && adapter != null) {
			lvCommentList.removeFooterView(adapter.getFooterView());
		}
		ListViewAdapter.OnAdapterListener<TakeoutCommentData> adapterListener = new ListViewAdapter.OnAdapterListener<TakeoutCommentData>() {

			@Override
			public void onRenderItem(ListViewAdapter<TakeoutCommentData> adapter, ViewHolder holder, TakeoutCommentData data) {
				// TODO Auto-generated method stub
				// 设置评论人
				if ("".equals(data.userName.trim())) {
					holder.$tv(R.id.list_item_comment_tvUser).setText(R.string.text_null_hanzi);
				} else {
					holder.$tv(R.id.list_item_comment_tvUser).setText(data.userName.trim());
				}
				// 添加用户头像
				if (CheckUtil.isEmpty(data.userPicUrl)) {
					holder.$myIv(R.id.list_item_comment_userphoto1).setImageResource(ImageUtil.loading);
				} else {
					holder.$myIv(R.id.list_item_comment_userphoto1).setImageByUrl(data.userPicUrl, true, 0, ScaleType.FIT_XY);
				}

				// 设置评论时间
				if (data.createTime != null) {
					holder.$tv(R.id.list_item_comment_tvTime).setText(data.createTime);
				} else {
					holder.$tv(R.id.list_item_comment_tvTime).setText("");
				}

				// 设置评论内容
				if (TextUtils.isEmpty(data.detail)) {
					holder.$tv(R.id.list_item_takeaway_comment_detail).setText(R.string.text_layout_dish_no_comment);
					holder.$(R.id.list_item_comment_replyInfo).setVisibility(View.GONE);
				} else
					holder.$tv(R.id.list_item_takeaway_comment_detail).setText(data.detail);

				// 设置星级
				// holder.list_item_takeaway_star.setProgress((int)
				// data.overallNum);
				RatingBar overallNum = (RatingBar) holder.$(R.id.list_item_takeaway_star);
				if(data.showOverallNumTag){
					overallNum.setVisibility(View.VISIBLE);
					overallNum.setProgress((int) data.overallNum);
				}else{
					overallNum.setVisibility(View.GONE);
				}
				

				// 回复内容
				if (TextUtils.isEmpty(data.replyInfo))
					holder.$(R.id.list_item_comment_replyInfo).setVisibility(View.GONE);
				else
					holder.$tv(R.id.list_item_takeaway_comment_replyInfo).setText(data.replyInfo);

			}

			@Override
			public void onLoadPage(final ListViewAdapter<TakeoutCommentData> adapter, int startIndex, int pageSize) {
				// TODO Auto-generated method stub
				// ----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----

				// 任务
				ServiceRequest request = new ServiceRequest(ServiceRequest.API.getTakeoutCommentList);

				// 判断来自哪里 1：详情页 2：订单页
				if (fromTag.equals("1")) {
					request.addData("fromTag", 1);// 来自哪里 1：详情页
					request.addData("uuid", takeAwayRestaurantId);// uuid
																	// 1:takeoutId（即外卖餐厅id）
				} else {
					request.addData("fromTag", 2);// 来自哪里 2：订单页
					request.addData("uuid", takeAwayOrderId);// uuid //
																// 2:orderId（即订单ID）
				}

				request.addData("pageSize", pageSize);// 页面大小
				request.addData("startIndex", startIndex); // 当前页
				CommonTask.request(request, "", new CommonTask.TaskListener<TakeoutCommentListDTO>() {

					@Override
					protected void onSuccess(TakeoutCommentListDTO dto) {
						// TODO Auto-generated method stub
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
						ListViewAdapter.AdapterDto<TakeoutCommentData> adapterDto = new ListViewAdapter.AdapterDto<TakeoutCommentData>();
						adapterDto.setList(dto.list);
						adapterDto.setPageInfo(dto.pgInfo);
						adapter.onTaskSucceed(adapterDto);
					}

					@Override
					protected void onError(int code, String message) {
						// TODO Auto-generated method stub
						super.onError(code, message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
						
						
//						doTest();
					}

					void doTest() {
						String json = "{\"list\":[{ 							\"uuid\":\"123456\", 							\"userName\":\"刘1\", 								\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\", 							\"createTime\":\"2012-02-08 12:12\", 							\"overallNum\":\"1.1\", 							\"detail\":\"aaaaaaaaa\", 							\"replyInfo\":\"\"},{ 							\"uuid\":\"123456\", 							\"userName\":\"刘2\", 								\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\", 							\"createTime\":\"2012-02-09 12:12\", 							\"overallNum\":\"2.1\", 							\"detail\":\"好好好好好好\", 							\"replyInfo\":\"\"},{ 							\"uuid\":\"123456\", 							\"userName\":\"刘3\", 								\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\", 							\"createTime\":\"2012-02-09 12:12\", 							\"overallNum\":\"3.1\", 							\"detail\":\"\", 							\"replyInfo\":\"好好好好好\"},{ 							\"uuid\":\"123456\", 							\"userName\":\"刘4\", 								\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\", 							\"createTime\":\"2012-02-09 12:12\", 							\"overallNum\":\"4.1\", 							\"detail\":\"好好好好好好\", 							\"replyInfo\":\"好好好好好\"},{ 							\"uuid\":\"123456\", 							\"userName\":\"刘5\", 								\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\", 							\"createTime\":\"2012-02-09 12:12\", 							\"overallNum\":\"5.1\", 							\"detail\":\"好好好好好好\", 							\"replyInfo\":\"好好好好好\"} ]	 	 	  }";
						TakeoutCommentListDTO dto = JsonUtils.fromJson(json, TakeoutCommentListDTO.class);
						onSuccess(dto);

					}
				});
			}
		};
		adapter = new ListViewAdapter<TakeoutCommentData>(R.layout.list_item_takeaway_restaurant_comment, adapterListener);
		adapter.setExistPage(true);
		adapter.setmCtx(TakeAwayRestaurantCommentActivity.this);
		adapter.setListView(lvCommentList);
	}
}
