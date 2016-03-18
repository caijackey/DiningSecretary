package com.fg114.main.app.activity.resandfood;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.adapter.RestaurantDetailRecommendAdapter;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.PageRestInfo3DTO;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestRecomCommentData;
import com.fg114.main.service.dto.RestRecomInfoData3;
import com.fg114.main.service.dto.RestRecomPicData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 推荐餐厅详情
 * 
 * @author 邓翔宇
 * 
 */
public class RestaurantDetailRecommendActivity extends MainFrameActivity {

	// 缓存数据
	private PageRestInfo3DTO pageRestInfoDTO;
//	private String pageRestInfoDTOjson;

	private LayoutInflater mInflater;
	private View contextView;

	private ListView recomrestListview;
	private View line;
	private LinearLayout mainFrameLay;
	private LinearLayout headerLayout;
	private LinearLayout footerLayout;
	// private LinearLayout addLayout;
	private LinearLayout resRecomCommentLayout;// 动态添加评论

	private String uuid;
	private String restID;
	private int lineY = 0;

	private int titleHight = 0;// 标题栏的高度

	private boolean isFirst = true;
	private boolean isSettingFirst = true;
	private boolean isFav = false;// 表示显示是否收藏了
	private boolean isJump2CommentArea = false;
	private boolean submitComment = false;

	// private RecommendResInfoAdapter recommendResAdapter;
	private RestaurantDetailRecommendAdapter recommendResAdapter;
	private RestRecomInfoData3 restRecomInfoData;
	// 图片列表
	public List<RestRecomPicData> picLists;
	// 评论列表
	public List<RestRecomCommentData> commentList;
	private ListAdapter adapter;

	private String detail;// 菜品评论

	private Button recom_res_zan;// 赞

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Fg114Application app = Fg114Application.getInstance();
//		try {
//			// 初始化百度地图
//			app.initBaidu();
//		} catch (Exception e) {
//			try {
//				// 初始化百度地图
//				app.initBaidu();
//			} catch (Exception e2) {
//				ActivityUtil.saveException(e, "init baidu api fail");
//				Settings.gBaiduAvailable = false;
//			}
//		}

		Bundle bundle = this.getIntent().getExtras();

		// if (bundle == null) {
		// DialogUtil.showToast(ContextUtil.getContext(), "数据请求异常");
		// finish();
		// }
		uuid = bundle.getString(Settings.BUNDLE_REST_ID);

		if (bundle.getBoolean(Settings.JUMP_RESCOMMEND_RES_COMMENT_AREA)) {
			// 自定跳转到评论区域
			isJump2CommentArea = true;
		}
		if (bundle.containsKey(Settings.BUNDLE_pageRestInfoDTO)) {
			pageRestInfoDTO = (PageRestInfo3DTO) bundle.getSerializable(Settings.BUNDLE_pageRestInfoDTO);
		}

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

	// 不能删除!!!否则会异常退出
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11.
	}

	@Override
	protected void onResume() {
		initView1();
		super.onResume();

		// if (isFresh || isFirst) {
		// excuteRestRecomInfo();
		// isFresh = false;
		// }
	}

	// 框架页调用的 刷新
	public void invisibleOnScreen1() {
		initView1();

	}

	private void initView1() {
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅详情", 2 + "-" + uuid + "-" + 0);
		// ----------------------------
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		this.getTitleLayout().setVisibility(View.GONE);
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.recommend_restaurant_detail, null);
		recomrestListview = (ListView) contextView.findViewById(R.id.recommend_restaurant_listview);
		mainFrameLay = (LinearLayout) contextView.findViewById(R.id.add_item_view_layout);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		if (pageRestInfoDTO==null){
			excuteRestRecomInfo();
		} else {
			toPageRestInfo3DTO(pageRestInfoDTO);
		}

		recomrestListview.setOnScrollListener(new OnScrollListener() {

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
////		final  int mMotionY=0;
//		recomrestListview.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				final int y = (int) event.getY();
//				
//				switch(event.getAction()){
//				case MotionEvent.ACTION_UP:
////					Log.v("TAG", "向上滑动");
////					 mMotionY = y;
//					 Log.v("TAG", "ACTION_UP");
//				break;
//				case MotionEvent.ACTION_DOWN:
//					  mMotionY = y;
////					 Log.v("TAG", "向下滑动");
//					  Log.v("TAG", "ACTION_DOWN");
//				break;
//				case MotionEvent.ACTION_MOVE:
//					int deltaY = y - mMotionY; //delta的正负就表示往下或往上
//					Log.v("TAG", y+"=y");
//					Log.v("TAG", mMotionY+"=mMotionY");
//					if(deltaY>0){
//						Log.v("TAG", "向下滑动");
//					}else{
//						Log.v("TAG", "向上滑动");
//					}
//				}
//				return false;
//			}
//		});

		// 分享
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				showShareDialog(3);
			}
		});
	}

	View layout_add;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (isFirst) {
			if (line != null) {
				lineY = line.getTop();// 160
				titleHight = getRealTimeY() - lineY;// 300
			}
			isFirst = false;
		}

	}

	// 实时获得line距离上面的距离
	private int getRealTimeY() {
		if (line != null) {
			int[] location = new int[2];
			line.getLocationOnScreen(location);
			return location[1];
		}
		return 1;
	}

	// 获得餐厅推荐信息，返回RestRecomInfoData
	private void excuteRestRecomInfo() {
		ServiceRequest request = new ServiceRequest(API.getPageRestInfoData3);
		request.addData("uuid", uuid);// typeTag=1,3为restId typeTag=2为recomId
		request.addData("typeTag", 2);// 1:餐厅 2：推荐 3：榜单
		request.addData("topRestTypeId", 0);// typeTag=3为榜单id
		// -----------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----------------
		CommonTask.request(request, new CommonTask.TaskListener<PageRestInfo3DTO>() {

			@Override
			protected void onSuccess(PageRestInfo3DTO dto) {
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				// RestaurantDetailRecommendActivity.this.getTvTitle().setText(dto.restName);

				recommendResAdapter = new RestaurantDetailRecommendAdapter(RestaurantDetailRecommendActivity.this);
				mainFrameLay.removeAllViews();

				restID = dto.restInfo.uuid;
				if (footerLayout != null) {
					recomrestListview.removeFooterView(footerLayout);
				}

				restRecomInfoData = dto.recomInfo;
				picLists = restRecomInfoData.picList;
				// addLayout = addListViewHeader(restRecomInfoData, addLayout,
				// false);
				//
				if (headerLayout != null && headerLayout.getChildCount() > 0) {
					headerLayout.removeAllViews();
				}
				headerLayout = addListViewHeader(headerLayout, restRecomInfoData);

				footerLayout = addListViewFooter(restRecomInfoData);

				recomrestListview.addHeaderView(headerLayout);
				// if (isFirstRefresh) {
				//
				// isFirstRefresh = false;
				// }
				// mainFrameLay.addView(addLayout);
				recomrestListview.addFooterView(footerLayout);
				recommendResAdapter.setList(restRecomInfoData);

				recomrestListview.setAdapter(recommendResAdapter);
				contextView.invalidate();
			}

			@Override
			protected void onError(int code, String message) {
				// ----------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// ----------------
				super.onError(code, message);
				// showErrorMessage(message, true);

				// doTest();
			}

			private void doTest() {
				String json = "{\"restInfo\":{\"uuid\":\"123456\",\"name\":\"牛肉粉馆\",\"favTag\":\"true\",\"restPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"restPicNum\":\"25\",\"avgPrice\":\"200\",\"tasteNum\":\"好\",\"envNum\":\"好\",\"serviceNum\":\"好\",\"canBookingTag\":\"true\",\"telForBooking\":\"57575777\",\"telForEdit\":\"57575777\",\"address\":\"浦东大道\",\"longitude\":\"0.00\",\"latitude\":\"0.00\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"},{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"}],\"openTimeInfo\":\"2\",\"menuTypeInfo\":\"湘菜\",\"trafficLine\":\"交通路线\",\"busInfo\":\"公交信息\",\"consumeType\":\"消费方式\",\"parkingPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"promoList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"couponList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"mealComboList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"totalSpecialFoodNum\":\"20\",\"specialFoodList\":[{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}},{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}}],\"cityId\":\"13456\",\"regionId\":\"132456\",\"regionName\":\"浦东新区\",\"districtId\":\"123456\",\"districtName\":\"上海\",\"mainMenuId\":\"123456\",\"mainMenuName\":\"菜单名字\",\"ydzkDetail\":\"预订折扣信息\",\"xjqDetail\":\"现金券信息\",\"cxDetail\":\"促销信息\",\"recomData\":{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"linkUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"recomInfo\":{\"uuid\":\"123456\",\"shareInfo\":{\"shareSmsDetail\":\"shareSmsDetail\",\"shareEmailDetail\":\"shareEmailDetail\",\"shareWeiboDetail\":\"shareWeiboDetail\",\"shareWeixinIconUrl\":\"shareWeixinIconUrl\",\"shareWeixinDetailUrl\":\"shareWeixinDetailUrl\",\"shareWeixinDetail\":\"shareWeixinDetail\",\"shareWeixinName\":\"shareWeixinName\",\"shareWeiboUuid\":\"shareWeiboUuid\"},\"favTag\":\"true\",\"title\":\"title\",\"createTime\":\"2014-12-23\",\"restId\":\"123456\",\"restName\":\"restName\",\"userId\":\"userId\",\"userNickName\":\"userNickName\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"userIsVipTag\":\"true\",\"picList\":[{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"},{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"}],\"totalLikeNum\":\"2000\",\"likedTag\":\"true\",\"tryRecomHint\":\"我要挑战说明\",\"relateRecomList\":[{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}]},\"showTypeTag\":\"1\"}";
				PageRestInfo3DTO dto = JsonUtils.fromJson(json, PageRestInfo3DTO.class);
				onSuccess(dto);
			}
		});
	}

	private void toPageRestInfo3DTO(PageRestInfo3DTO dto) {
//		PageRestInfo3DTO dto = JsonUtils.fromJson(json, PageRestInfo3DTO.class);

		recommendResAdapter = new RestaurantDetailRecommendAdapter(RestaurantDetailRecommendActivity.this);
		mainFrameLay.removeAllViews();

		restID = dto.restInfo.uuid;
		if (footerLayout != null) {
			recomrestListview.removeFooterView(footerLayout);
		}

		restRecomInfoData = dto.recomInfo;
		picLists = restRecomInfoData.picList;
		// addLayout = addListViewHeader(restRecomInfoData, addLayout,
		// false);
		//
		if (headerLayout != null && headerLayout.getChildCount() > 0) {
			headerLayout.removeAllViews();
		}
		headerLayout = addListViewHeader(headerLayout, restRecomInfoData);

		footerLayout = addListViewFooter(restRecomInfoData);

		recomrestListview.addHeaderView(headerLayout);
		// if (isFirstRefresh) {
		//
		// isFirstRefresh = false;
		// }
		// mainFrameLay.addView(addLayout);
		recomrestListview.addFooterView(footerLayout);
		recommendResAdapter.setList(restRecomInfoData);

		recomrestListview.setAdapter(recommendResAdapter);
		contextView.invalidate();
	}

	// 添加“餐厅推荐”评论
	private void excuteAddRestRecomComment() {
		ServiceRequest request = new ServiceRequest(API.addRestRecomComment);
		request.addData("uuid", uuid);
		request.addData("detail", detail);// 评论内容
		// -----
		OpenPageDataTracer.getInstance().addEvent("推荐提交评论按钮");
		// -----
		CommonTask.request(request, "正在提交评论...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("推荐提交评论按钮");
				// -----

				Fg114Application.isNeedUpdate = true;// 通知数据已经改变

				DialogUtil.showAlert(RestaurantDetailRecommendActivity.this, "提示", "成功提交评论内容", new Runnable() {

					@Override
					public void run() {

						submitComment = true;

						excuteRestRecomInfo();
					}
				});
			}

			@Override
			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("推荐提交评论按钮");
				// -----
				showErrorMessage(message, false);
			}
		});
	}

	private void showErrorMessage(String message, boolean isFinish) {
		DialogUtil.showToast(RestaurantDetailRecommendActivity.this, message);
		if (isFinish)
			finish();
	}

	//
	// // 给listview添加头布局
	private LinearLayout addListViewHeader(LinearLayout layout, final RestRecomInfoData3 data) {
		layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.res_detail_header, null);
		MyImageView res_recom_hint_imageview = (MyImageView) layout.findViewById(R.id.res_recom_hint_imageview);
		ImageView res_user_vip = (ImageView) layout.findViewById(R.id.res_user_vip);
		TextView rest_detail_name = (TextView) layout.findViewById(R.id.rest_detail_name);
		TextView res_recom_hint_titile = (TextView) layout.findViewById(R.id.res_recom_hint_titile);

		res_recom_hint_titile.setText(Html.fromHtml("<font color=\"#000000\">" + data.userNickName + "</font>" + "<font color=\"#777777\">" + "推荐   " + data.createTime + "</font>"));

		rest_detail_name.setText(data.title);

		if (data.userIsVipTag) {
			res_user_vip.setVisibility(View.VISIBLE);
		} else {
			res_user_vip.setVisibility(View.GONE);
		}

		res_recom_hint_imageview.setImageByUrl(data.userPicUrl, true, 0, ScaleType.FIT_XY);

		res_recom_hint_imageview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("推荐-头像按钮");
				// -----
				String path = "member/resrecommend";
				String title = data.userNickName + "的主页";
				NameValuePair pair = new BasicNameValuePair("userId", data.userId);
				ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + path, title, pair);

			}
		});

		return layout;
	}

	// 给listview添加尾布局
	private LinearLayout addListViewFooter(final RestRecomInfoData3 data) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.recommend_res_listview_footer, null);
		View recom_res_zan_bt = layout.findViewById(R.id.recom_res_zan_bt);
		recom_res_zan = (Button) layout.findViewById(R.id.recom_res_zan);
		TextView other_look = (TextView) layout.findViewById(R.id.other_look);
		TextView recom_res_zan_num = (TextView) layout.findViewById(R.id.recom_res_zan_num);
		View recom_res_try = layout.findViewById(R.id.recom_res_try);
		LinearLayout other_relate_recom = (LinearLayout) layout.findViewById(R.id.other_relate_recom);

		if (data.likedTag) {
			recom_res_zan.setBackgroundResource(R.drawable.zan_red);
		} else {
			recom_res_zan.setBackgroundResource(R.drawable.zan_gray);
		}

		recom_res_zan_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("推荐-赞按钮");
				// -----

				if (data.likedTag) {
					excuteCancelZan();
				} else {
					excuteZan();
				}
			}
		});

		recom_res_zan_num.setText("赞(" + data.totalLikeNum + ")");

		// 我要挑战
		recom_res_try.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("推荐-我要挑战按钮");
				// -----
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (CheckUtil.isEmpty(data.tryRecomHint)) {
					ActivityUtil.jump(RestaurantDetailRecommendActivity.this, RecommandRestaurantSubmitActivity.class, 0, new Bundle());
				} else {
					DialogUtil.showAlert(RestaurantDetailRecommendActivity.this, false, "挑战说明", data.tryRecomHint, "我已了解", "", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							ActivityUtil.jump(RestaurantDetailRecommendActivity.this, RecommandRestaurantSubmitActivity.class, 0, new Bundle());
						}
					});
				}
			}
		});

		if (other_relate_recom.getChildCount() != 0) {
			other_relate_recom.removeAllViews();
		}
		for (int i = 0; i < data.relateRecomList.size(); i++) {
			other_look.setVisibility(View.VISIBLE);
			View view = (View) LayoutInflater.from(this).inflate(R.layout.relate_recom_item, null);
			MyImageView rest_recom_pic = (MyImageView) view.findViewById(R.id.rest_recom_pic);
			ImageView same_store_view = (ImageView) view.findViewById(R.id.same_store_view);
			TextView rest_recom_title = (TextView) view.findViewById(R.id.rest_recom_title);
			TextView rest_recom_name_and_data = (TextView) view.findViewById(R.id.rest_recom_name_and_data);
			TextView rest_recom_info = (TextView) view.findViewById(R.id.rest_recom_info);
			View rest_recom_bt = view.findViewById(R.id.rest_recom_bt);

			rest_recom_pic.setImageByUrl(data.relateRecomList.get(i).picUrl, true, 0, ScaleType.FIT_XY);
			if (data.relateRecomList.get(i).restId.equals(restID)) {
				same_store_view.setVisibility(View.VISIBLE);
			} else {
				same_store_view.setVisibility(View.GONE);
			}
			rest_recom_title.setText(data.relateRecomList.get(i).title);
			rest_recom_name_and_data.setText(data.relateRecomList.get(i).userNickName + "  " + data.relateRecomList.get(i).createTime);
			rest_recom_info.setText(data.relateRecomList.get(i).detail);

			final int j = i;
			rest_recom_bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("推荐-其他人还看了");
					// -----
					
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, data.relateRecomList.get(j).uuid);
					bundle.putInt(Settings.BUNDLE_showTypeTag, 2);
					ActivityUtil.jump(RestaurantDetailRecommendActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
				}
			});
			other_relate_recom.addView(view);

		}
		return layout;
	}

	// 请求赞
	private void excuteZan() {
		ServiceRequest request = new ServiceRequest(API.addRestRecomToLike);
		request.addData("uuid", uuid);

		CommonTask.request(request, "正在提交评论...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				recom_res_zan.setBackgroundResource(R.drawable.zan_red);
				restRecomInfoData.likedTag = true;
				// 刷新
				excuteRestRecomInfo();
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	// 取消赞
	private void excuteCancelZan() {
		ServiceRequest request = new ServiceRequest(API.delRestRecomFromLike);
		request.addData("uuid", uuid);

		CommonTask.request(request, "正在提交评论...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				recom_res_zan.setBackgroundResource(R.drawable.zan_gray);
				restRecomInfoData.likedTag = false;
				// 刷新
				excuteRestRecomInfo();
			}

			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
			}
		});
	}

	// // 动态添加推荐餐厅的评论列表
	// private void addRestRecomCommendList(LinearLayout resRecomCommentLayout,
	// List<RestRecomCommentData> commentList, String commentNum) {
	// // 判断是否有评论数据
	// if (commentList == null || commentList.size() == 0) {
	// noCommentaTsgTv.setVisibility(View.VISIBLE);
	// resRecomLookAllBnt.setVisibility(View.GONE);
	// res_recommend_layout.setVisibility(View.GONE);
	// res_recommend.setVisibility(View.GONE);
	// } else {
	// res_recommend.setVisibility(View.VISIBLE);
	// noCommentaTsgTv.setVisibility(View.GONE);
	// res_detail_num.setText("(" + commentNum + ")");
	// resRecomLookAllBnt.setVisibility(View.VISIBLE);
	// res_recommend_layout.setVisibility(View.VISIBLE);
	// for (int i = 0; i < commentList.size(); i++) {
	// final RestRecomCommentData commentData = commentList.get(i);
	// LinearLayout addLayout = (LinearLayout)
	// LayoutInflater.from(this).inflate(R.layout.list_item_res_rescommed_comment,
	// null);
	// MyImageView userPicUrlImg = (MyImageView)
	// addLayout.findViewById(R.id.user_pic_url_img);
	// TextView userNickName = (TextView)
	// addLayout.findViewById(R.id.user_nick_name_tv);
	// TextView createTime = (TextView)
	// addLayout.findViewById(R.id.create_time_tv);
	// TextView detail = (TextView) addLayout.findViewById(R.id.detail_tv);
	// userPicUrlImg.setImageByUrl(commentData.userPicUrl, true, 0,
	// ScaleType.FIT_XY);
	// userNickName.setText(commentData.userNickName);
	// createTime.setText(commentData.createTime);
	// detail.setText(commentData.detail);
	// resRecomCommentLayout.addView(addLayout);
	// userPicUrlImg.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // -----
	// OpenPageDataTracer.getInstance().addEvent("评论头像");
	// // -----
	// ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + "/member/index",
	// commentData.userNickName + "的主页", new BasicNameValuePair("userId",
	// commentData.userId));
	//
	// }
	// });
	// }
	// }
	// }
	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return restRecomInfoData.shareInfo == null ? "" : restRecomInfoData.shareInfo.shareWeiboUuid;
	}

	@Override
	protected String getRestaurantId() {
		return restRecomInfoData.restId;
	}

	@Override
	protected String getRestaurantName() {
		return restRecomInfoData.restName;
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// Log.v("TAG", "rdR");
	// }

}
