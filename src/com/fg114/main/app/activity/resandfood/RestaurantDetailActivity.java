package com.fg114.main.app.activity.resandfood;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SelectMultiplePictureActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.mealcombo.GroupBuyDetailActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;
import com.fg114.main.app.adapter.SimpleFoodListAdapter;
import com.fg114.main.app.data.Filter;
import com.fg114.main.app.listener.OnProcessPictureListener;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.app.view.ActionItem;
import com.fg114.main.app.view.CommentItem;
import com.fg114.main.app.view.EllipsizeText;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.QuickAction;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.PageRestInfo3DTO;
import com.fg114.main.service.dto.ResFoodCommentData;
import com.fg114.main.service.dto.RestFoodData;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestPromoData;
import com.fg114.main.service.dto.RestTelInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.SpecialRestData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.ProcessPictureTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅详情主页面
 * 
 * @author zhaozuoming
 * 
 *         3种样式 1:餐厅 2：推荐 3：榜单
 * 
 */
public class RestaurantDetailActivity extends MainFrameActivity {
	private static final String TAG = "RestaurantDetailActivity";
	private static final String MAP_TYPE_FIRST_CHOICE = "GoogleMap";
	private static final String[] MAP_TYPE = { "百度", "高德", "Browser" };
	// 传入参数
	private int fromPage; // 返回页面
	private String restaurantId; // 餐厅ID
	private int typeTag = 1;// 餐厅样式
	private String restTypeId = "";// 榜单ID
	// private String leftGoBackBtn; //返回按钮内容
	private String resName;
	private String resLogoUrl;
	private String foodieImgUrl;

	private PageRestInfo3DTO pageRestInfoDTO;

	// 本地缓存数据
	private RestInfoData restaurantInfo;
	private Filter filter;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	// 提示
	private RelativeLayout detailResRecomHintLayout;
	private TextView detailResRecomHintTxt;

	private ScrollView scrollView;
	private LinearLayout plLayout;
	private RelativeLayout tscLayout;
	private ViewGroup addFoodLayout;
	private Button addFood;
	// private TextView tvResName;// 餐厅名字

	// 顶部（榜单）
	private TextView detail_top_res_reason;
	private View detail_top_res_bt;
	private ImageView detail_top_san_jiao;
	// 顶部（非榜单）
	private TextView tvTasteNum;// 口味
	private TextView tvEnvNum;// 环境
	private TextView tvServiceNum;// 服务

	// 喜欢不喜欢
	private Animation gradeAnimation;

	// --
	private MyImageView rest_location_map;
	private TextView tvAvgPrice;// 人均
	private TextView tvAddress;
	private ViewGroup tvAddressLayout;
	private TextView tvTel;
	private MyImageView ivResPic;
	private TextView picNum;
	private EllipsizeText tvFoodList;
	private ViewGroup tvFoodListLayout;
	private TextView specialFoodNum;
	private TextView commentNum;
	private SimpleFoodListAdapter foodAdapter;
	private TextView rest_detail_name;
	// 4.1.4以前是listview，后来改成linearlayout
	private LinearLayout lvCommentList;
	// private CommentAdapter commentAdapter;

	private LinearLayout resInfoLoadingLayout;
	private LinearLayout resOtherInfoLayout;
	private LinearLayout resOtherInfoLoadingLayout;
	// private LinearLayout resCommentListLayout;
	private LinearLayout resCommentLoadingLayout;
	private LinearLayout otherResInfoLayout;
	private RelativeLayout detail_res_dish_order;

	// private LinearLayout shareToFriendLayout;
	/*****************************************/
	// 吃货荐店linerLayout
	private LinearLayout detail_res_layout;
	private View recommendLayout;
	private MyImageView foodie_headImg;
	private TextView tv_recommend_explain;
	private TextView tv_recommend_time;
	private TextView tv_usersname;
	private TextView tv_recommend_address;

	/****************************************/
	private LinearLayout llSpecialFoodTitle;
	private TextView tvDishOnline;
	private LinearLayout discountLayout;
	private LinearLayout discountList;
	
	private TextView noDiscount;

	// 按钮面板
	private View buttonPanelView;
	private PopupWindow buttonPanelDialog;
	private Button btnDialogBookByPhone;
	private Button btnDialogBookByNet;
	private Button btnDialogUploadFromCamera;
	private Button btnDialogUploadFromLocal;
	private Button btnDialogCancle;
	private Button btnAddPhone;
	private Button predetermine;// 预定按钮

	// 底部按钮
	private LinearLayout res_add_photo_button;
	private LinearLayout res_add_comment_button;
	private LinearLayout res_tag_button;
	private LinearLayout res_error_button;

	// 餐厅标签
	private LinearLayout res_tag_layout;
	private LinearLayout res_tag;
	private LinearLayout res_tag_bt;

	// 餐厅附近查找
	private LinearLayout res_search_menu_tag_layout;
	private LinearLayout res_search_menu_tag_bt;
	private LinearLayout res_search_menu_tag;

	private QuickAction mQuickAction;
	// private Button bntNavigation;
	private Button shareFriends;

	private String hlUrl;

	private String mNextPage;
	private List<int[]> startEndPoi = new ArrayList<int[]>();// 特色菜 左右 灰色括号
																// 对应下标数组列表

//为了解决安卓2.3或者4.0应用爆掉bug
private boolean isSimpleVer = false;
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
		int versionId= android.os.Build.VERSION.SDK_INT;

		String release=android.os.Build.VERSION.RELEASE;  // android系统版本号
//		if(release.startsWith("2.3")||release.startsWith("4.0")){
		if(versionId<16){
			isSimpleVer = true;
		}
		// 获得传入参数

		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		this.bundleData.putString(Settings.BUNDLE_KEY_ID, restaurantId);

		// 餐厅样式
		if (bundle.containsKey(Settings.BUNDLE_TPYE_TAG)) {
			typeTag = bundle.getInt(Settings.BUNDLE_TPYE_TAG);
		}
		if (bundle.containsKey(Settings.BUNDLE_REST_TYPE_ID)) {
			restTypeId = bundle.getString(Settings.BUNDLE_REST_TYPE_ID);
		}

		// 跳转页面逻辑
		if (bundle.containsKey(Settings.BUNDLE_RES_DETAIL_NEXT_PAGE)) {
			mNextPage = bundle.getString(Settings.BUNDLE_RES_DETAIL_NEXT_PAGE);
		}

		if (bundle.containsKey(Settings.BUNDLE_pageRestInfoDTO)) {
			pageRestInfoDTO = (PageRestInfo3DTO) bundle.getSerializable(Settings.BUNDLE_pageRestInfoDTO);
		}

		// 获得缓存数据
		filter = SessionManager.getInstance().getFilter();

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		} else {
			if (pageRestInfoDTO==null) {
				executeGetPageResInfoDataTask();
			} else {
				toPageRestInfo3DTO(pageRestInfoDTO);
			}
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// // ----------------------------
		// OpenPageDataTracer.getInstance().enterPage("餐厅详情", typeTag + "-" +
		// restaurantId + "-" + restTypeId);
		// // ----------------------------
	}

	private void toPageRestInfo3DTO(PageRestInfo3DTO dto) {
		// 设置列表适配器
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
//		PageRestInfo3DTO dto = JsonUtils.fromJson(json, PageRestInfo3DTO.class);

		if (dto != null) {
			restaurantInfo = dto.restInfo;
		}
		if (typeTag != 2) {
			RestaurantDetailActivity.this.getTvTitle().setText(dto.restInfo.name);
		}
		// 设置列表适配器
		setView();
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});

		RestInfoData resInfoData = dto.restInfo;

		if (resInfoData.favTag) {
			RestaurantDetailActivity.this.getBtnOption().setSelected(true);
		} else {
			RestaurantDetailActivity.this.getBtnOption().setSelected(false);
		}

		SessionManager.getInstance().setRestaurantInfo(ContextUtil.getContext(), resInfoData);

		// 提示
		if (CheckUtil.isEmpty(resInfoData.hlHint)) {
			detailResRecomHintLayout.setVisibility(View.GONE);
		} else {
			hlUrl = resInfoData.hlUrl;
			detailResRecomHintLayout.setVisibility(View.VISIBLE);
			detailResRecomHintTxt.setText(Html.fromHtml(resInfoData.hlHint));
		}

		// --
		if(!isSimpleVer){
			
		
		if (dto.totalCommentNum == 0) {
			// 没有评论的场合
			plLayout.setVisibility(View.GONE);
		} else {
			plLayout.setVisibility(View.VISIBLE);
			resCommentLoadingLayout.setVisibility(View.GONE);
			commentNum.setText("(" + dto.totalCommentNum + ")");

			// 动态添加用户评论,如果已经有东西，西安清除以前的
			if (lvCommentList.getChildCount() > 0) {
				lvCommentList.removeAllViews();
			}
			int count = dto.commentList.size();
			for (int i = 0; i < count; i++) {
				CommentItem commentItem = new CommentItem(RestaurantDetailActivity.this, dto.commentList.get(i), restaurantId);
				lvCommentList.addView(commentItem);
				commentItem.setClickable(true);
				commentItem.setOnClickListener(new View.OnClickListener() {
					// // 去餐厅评论页面
					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						// -----
						OpenPageDataTracer.getInstance().addEvent("网友评论面板-更多评论");
						// -----
						Bundle bundle = new Bundle();
						bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
						ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantCommentActivity.class, 0, bundle);
					}
				});
			}

		}
		}else{
			plLayout.setVisibility(View.GONE);
			resCommentLoadingLayout.setVisibility(View.GONE);
		}
		// 获取标签列表 判断是否能打标签
		if (dto.restInfo.canAddLabelTag) {
			getResTagList(dto.restInfo.labelList);
		} else {
			res_tag_layout.setVisibility(View.GONE);// 取消标签框
			res_tag_button.setVisibility(View.GONE);// 取消底部面板 打标签
		}

		// 获取这家餐厅附近查找标签
		if (dto.restInfo.searchMenuTypeList != null) {
			getResSerachTagList(dto.restInfo.searchMenuTypeList);
		} else {
			res_search_menu_tag_layout.setVisibility(View.GONE);
		}

		// 跳转逻辑
		jumpToNextPage();
	}

	// /* start 测试代码 -----------*/
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU) {
	//
	// return false;
	// } else {
	// return super.onKeyDown(keyCode, event);
	// }
	// }
	//
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// menu.clear();
	// for (int i=0; i < MAP_TYPE.length; i++) {
	// menu.add(0, i, i, MAP_TYPE[i]);
	// }
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (!launchMap(MAP_TYPE[item.getItemId()])) {
	// DialogUtil.showToast(RestaurantDetailActivity.this, "无法打开地图模式");
	// }
	// return super.onOptionsItemSelected(item);
	// }
	// /* end 测试代码 -----------*/
	//
	@Override
	protected void onResume() {

		initView();

		super.onResume();

	}

	// 框架页调用的 刷新
	public void invisibleOnScreen() {
		initView();

	}

	private void initView() {
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅详情", typeTag + "-" + restaurantId + "-" + restTypeId);
		// ----------------------------
		// 如果需要刷新评论，执行刷新评论
		if (Settings.NEED_REFRESH_REST_COMMENT && restaurantInfo != null && !CheckUtil.isEmpty(restaurantInfo.uuid) && restaurantInfo.uuid.equals(Settings.COMMENT_RES_ID)) {
			res_search_menu_tag.removeAllViews();
			res_tag.removeAllViews();
			executeGetPageResInfoDataTask();
			Settings.NEED_REFRESH_REST_COMMENT = false;
			Settings.COMMENT_RES_ID = "";

		}
		if (Settings.NEED_TAG_REST_COMMENT || Settings.NEED_TAG_REST_RECOMMEND) {
			res_search_menu_tag.removeAllViews();
			res_tag.removeAllViews();
			executeGetPageResInfoDataTask();
			Settings.NEED_TAG_REST_COMMENT = false;
			Settings.NEED_TAG_REST_RECOMMEND = false;
		}

		if (Settings.NEED_TAG_REST_RECOMMEND && restaurantInfo != null && !CheckUtil.isEmpty(restaurantInfo.uuid) && restaurantInfo.uuid.equals(Settings.COMMENT_RES_ID)) {
			res_search_menu_tag.removeAllViews();
			res_tag.removeAllViews();
			executeGetPageResInfoDataTask();
			Settings.NEED_TAG_REST_RECOMMEND = false;
			Settings.COMMENT_RES_ID = "";
		}

	}

	/*
	 * @Override protected void onPause() { super.onPause();
	 * 
	 * visibleCache=plLayout.getVisibility(); plLayout.setVisibility(View.GONE);
	 * 
	 * } private int visibleCache=-1; //此变量是为了解决listview滚动bug
	 */
	public void scrollAdjust() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// Log.d("ivResPic.requestFocus()",""+ivResPic.requestFocus());
				/*
				 * if(visibleCache!=-1){ plLayout.setVisibility(visibleCache); }
				 */
				scrollView.scrollTo(0, 0);

			}
		});
	}

	@Override
	public void finish() {
		recycle();
		super.finish();
	}

	// 拼接短信信息-----------------
	@Override
	protected String makeSMSinfo() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareSmsDetail;
	}

	// 拼接邮件信息
	@Override
	protected String makeEmailInfo() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareEmailDetail;

	}

	// 拼接微博信息
	@Override
	protected String makeWeiboInfo() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeiboDetail;

	}

	// 拼接微信信息
	@Override
	protected String makeWeiXinInfo() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeixinDetail;
	}

	@Override
	protected String getRestaurantUrl() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeixinIconUrl;
	}

	@Override
	protected String getRestaurantLinkUrl() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeixinDetailUrl;
	}

	@Override
	protected String getWeixinName() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeixinName;
	}

	@Override
	protected String getWeiboUuid() {
		return restaurantInfo.shareInfo == null ? "" : restaurantInfo.shareInfo.shareWeiboUuid;
	}

	@Override
	protected String getRestaurantId() {
		return restaurantInfo.uuid;
	}

	@Override
	protected String getRestaurantName() {
		return restaurantInfo.name;
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		if (typeTag == 2) {
			this.getTitleLayout().setVisibility(View.GONE);
		} else {
			// 设置标题栏
			this.getTitleLayout().setVisibility(View.VISIBLE);
			this.getTvTitle().setText(R.string.text_title_restaurant_detail);
			// this.getBtnGoBack().setText(leftGoBackBtn);
			this.getBtnGoBack().setText(R.string.text_button_back);
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnTitle().setVisibility(View.GONE);
			shareFriends = new Button(this);
			shareFriends.setBackgroundResource(R.drawable.res_share_friends);
			shareFriends.setWidth(UnitUtil.dip2px(25));
			shareFriends.setHeight(UnitUtil.dip2px(25));
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			lp.setMargins(shareFriends.getLeft() - UnitUtil.dip2px(50), shareFriends.getTop(), shareFriends.getRight() + UnitUtil.dip2px(50), shareFriends.getBottom());
			shareFriends.setLayoutParams(lp);
			this.getTitleLayout().addView(shareFriends);

			this.getBtnOption().setBackgroundResource(R.drawable.share2friend);

			LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			lps.setMargins(this.getBtnOption().getLeft() - UnitUtil.dip2px(-40), this.getBtnOption().getTop(), this.getBtnOption().getRight() + UnitUtil.dip2px(-40), this.getBtnOption().getBottom());
			this.getBtnOption().setLayoutParams(lps);
			this.getBtnOption().setWidth(UnitUtil.dip2px(25));
			this.getBtnOption().setHeight(UnitUtil.dip2px(25));

			this.getBottomLayout().setVisibility(View.GONE);

			// TODO 收藏
			this.getBtnOption().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!RestaurantDetailActivity.this.getBtnOption().isSelected()) {

						RestaurantDetailActivity.this.getBtnOption().setSelected(false);
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
								RestaurantDetailActivity.this.getBtnOption().setSelected(true);
								DialogUtil.showToast(RestaurantDetailActivity.this, "收藏成功");
							}

							@Override
							protected void onError(int code, String message) {
								super.onError(code, message);
								// -----
								OpenPageDataTracer.getInstance().endEvent("收藏按钮");
								// -----
							}

							private void doTest_confirm() {
								String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
								SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
								onSuccess(data);

							}
						});

					} else {
						RestaurantDetailActivity.this.getBtnOption().setSelected(true);
						ServiceRequest request = new ServiceRequest(API.delRestFromFav);
						request.addData("restId", restaurantId);// 餐馆ID
						// -----
						OpenPageDataTracer.getInstance().addEvent("收藏按钮");
						// -----
						CommonTask.request(request, "取消收藏...", new CommonTask.TaskListener<SimpleData>() {

							@Override
							protected void onSuccess(SimpleData dto) {
								// -----
								OpenPageDataTracer.getInstance().endEvent("收藏按钮");
								// -----
								RestaurantDetailActivity.this.getBtnOption().setSelected(false);
								DialogUtil.showToast(RestaurantDetailActivity.this, "取消收藏成功");
							}

							@Override
							protected void onError(int code, String message) {
								super.onError(code, message);
								// -----
								OpenPageDataTracer.getInstance().endEvent("收藏按钮");
								// -----
							}

							private void doTest_cancel() {
								String json = "{\"uuid\":\"123456\",\"restUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"msg\":\"取消收藏成功\",\"errorCode\":\"404\",\"succTag\":\"true\",\"needToDishPageTag\":\"false\"}";
								SimpleData data = JsonUtils.fromJson(json, SimpleData.class);
								onSuccess(data);

							}
						});

					}

				}
			});
			shareFriends.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("分享面板-分享");
					// -----

					showShareDialog(1);
				}
			});
		}

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (typeTag == 3) {
			contextView = mInflater.inflate(R.layout.restaurant_detail_list_act, null);
			detail_top_res_reason = (TextView) contextView.findViewById(R.id.detail_top_res_reason);
			detail_top_res_bt = contextView.findViewById(R.id.detail_top_res_bt);
			detail_top_san_jiao = (ImageView) contextView.findViewById(R.id.detail_top_san_jiao);
			ivResPic = (MyImageView) contextView.findViewById(R.id.detail_res_ivResPic);

		} else {
			contextView = mInflater.inflate(R.layout.new_restaurant_detail, null);
			tvTasteNum = (TextView) contextView.findViewById(R.id.taste_num);
			tvEnvNum = (TextView) contextView.findViewById(R.id.env_num);
			tvServiceNum = (TextView) contextView.findViewById(R.id.service_um);
			tvAvgPrice = (TextView) contextView.findViewById(R.id.avg_price);
			picNum = (TextView) contextView.findViewById(R.id.detail_res_pic_num);
			ivResPic = (MyImageView) contextView.findViewById(R.id.detail_res_ivResPic);
			rest_detail_name = (TextView) contextView.findViewById(R.id.rest_detail_name);
		}
		scrollView = (ScrollView) contextView.findViewById(R.id.detail_res_scrollBar);

		// 地址信息
		rest_location_map = (MyImageView) contextView.findViewById(R.id.rest_location_map);
		tvAddressLayout = (ViewGroup) contextView.findViewById(R.id.detail_res_tvAddress_layout);
		tvAddress = (TextView) contextView.findViewById(R.id.detail_res_tvAddress);
		tvTel = (TextView) contextView.findViewById(R.id.detail_res_tvTel);

		btnAddPhone = (Button) contextView.findViewById(R.id.detail_res_add_phone);
		specialFoodNum = (TextView) contextView.findViewById(R.id.detail_res_special_food_num);
		commentNum = (TextView) contextView.findViewById(R.id.detail_res_comment_num);

		tvFoodList = (EllipsizeText) contextView.findViewById(R.id.detail_res_tvFoodList);
		tvFoodListLayout = (ViewGroup) contextView.findViewById(R.id.detail_res_tvFoodList_layout);
		lvCommentList = (LinearLayout) contextView.findViewById(R.id.detail_res_lvCommentList);
		plLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_plLayout);
		tscLayout = (RelativeLayout) contextView.findViewById(R.id.detail_res_tscLayout);
		addFoodLayout = (ViewGroup) contextView.findViewById(R.id.detail_res_add_food_layout);
		addFood = (Button) contextView.findViewById(R.id.detail_res_add_food);
		resInfoLoadingLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_resInfoLoadingLayout);
		resOtherInfoLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_resOtherInfoLayout);
		resOtherInfoLoadingLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_resOtherInfoLoadingLayout);
		// resCommentListLayout = (LinearLayout)
		// contextView.findViewById(R.id.detail_res_commentListLayout);
		resCommentLoadingLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_resCommentLoadingLayout);
		otherResInfoLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_otherResInfoLayouts);
		detail_res_dish_order = (RelativeLayout) contextView.findViewById(R.id.detail_res_dish_order);
		detail_res_layout = (LinearLayout) contextView.findViewById(R.id.detail_res_layout);
		// 由导航页替换为吃货荐店页面
		recommendLayout = (View) contextView.findViewById(R.id.detail_res_routeLayout);
		foodie_headImg = (MyImageView) contextView.findViewById(R.id.foodie_headImg);
		tv_recommend_address = (TextView) contextView.findViewById(R.id.tv_recommend_address);
		tv_recommend_time = (TextView) contextView.findViewById(R.id.tv_recommend_time);
		tv_recommend_explain = (TextView) contextView.findViewById(R.id.tv_recommend_explain);
		tv_usersname = (TextView) contextView.findViewById(R.id.tv_usersname);

		llSpecialFoodTitle = (LinearLayout) contextView.findViewById(R.id.detail_res_llSpecialFoodTitle);

		// 提示
		detailResRecomHintLayout = (RelativeLayout) contextView.findViewById(R.id.detail_res_recom_hint_layout);
		detailResRecomHintTxt = (TextView) contextView.findViewById(R.id.detail_res_recom_hint_txt);

		// 优惠信息
		discountLayout = (LinearLayout) contextView.findViewById(R.id.detail_restaurant_discount_layout);
		discountList = (LinearLayout) contextView.findViewById(R.id.detail_restaurant_discount_list);
		noDiscount = (TextView) contextView.findViewById(R.id.detail_restaurant_no_discount);
		//
		tvDishOnline = (TextView) contextView.findViewById(R.id.detail_res_tvDishOnline);

		predetermine = (Button) contextView.findViewById(R.id.predetermine);

	
		// 底部
		res_add_photo_button = (LinearLayout) contextView.findViewById(R.id.res_add_photo_button);
		res_add_comment_button = (LinearLayout) contextView.findViewById(R.id.res_add_comment_button);
		res_tag_button = (LinearLayout) contextView.findViewById(R.id.res_tag_button);
		res_error_button = (LinearLayout) contextView.findViewById(R.id.res_error_button);

		// 标签
		res_tag_layout = (LinearLayout) contextView.findViewById(R.id.res_tag_layout);
		res_tag = (LinearLayout) contextView.findViewById(R.id.res_tag);
		res_tag_bt = (LinearLayout) contextView.findViewById(R.id.res_tag_bt);

		// 餐厅附近查找
		res_search_menu_tag_layout = (LinearLayout) contextView.findViewById(R.id.res_search_menu_tag_layout);
		res_search_menu_tag_bt = (LinearLayout) contextView.findViewById(R.id.res_search_menu_tag_bt);
		res_search_menu_tag = (LinearLayout) contextView.findViewById(R.id.res_search_menu_tag);

		// 吃货荐店图片
		if (CheckUtil.isEmpty(foodieImgUrl)) {
			foodie_headImg.setImageResource(ImageUtil.loading);
		} else {
			foodie_headImg.setImageByUrl(foodieImgUrl, true, 0, ScaleType.CENTER_CROP);
		}

		// 推荐餐厅
		detailResRecomHintLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("提示信息面板-点击行");
				// -----
				URLExecutor.execute(hlUrl, RestaurantDetailActivity.this, 0);
			}
		});
		

		resInfoLoadingLayout.setVisibility(View.VISIBLE);
		resOtherInfoLayout.setVisibility(View.GONE);
		resOtherInfoLoadingLayout.setVisibility(View.VISIBLE);
		// resCommentListLayout.setVisibility(View.GONE);
		resCommentLoadingLayout.setVisibility(View.VISIBLE);
		if (typeTag != 3) {
			picNum.setVisibility(View.GONE);
		}

		res_tag_layout.setVisibility(View.GONE);
		res_search_menu_tag_layout.setVisibility(View.GONE);

		this.setFunctionLayoutGone();

		gradeAnimation = AnimationUtils.loadAnimation(this, R.anim.restaurant_picture_detail_grade_rise_from_bottom);
		this.getMainLayout().addView(contextView, android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
	}

	// /**
	// * 去预订
	// */
	// private void goBookOrder(View v, int roomType) {
	// // 防止重复点击
	// ViewUtils.preventViewMultipleClick(v, 1000);
	// // 去预订页
	// Bundle bundle = new Bundle();
	// bundle.putInt(Settings.BUNDLE_DEFAULT_ROOM_TYPE, roomType);
	// bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, "餐厅详情");
	// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
	// ActivityUtil.jump(RestaurantDetailActivity.this,
	// BookingFromNetActivity.class, 0, bundle);
	// }

	/**
	 * 获得餐厅详细
	 */
	// TODO
	private void executeGetPageResInfoDataTask() {
		// 设置列表适配器
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getPageRestInfoData3);
		request.addData("uuid", restaurantId);// typeTag=1,3为restId
												// typeTag=2为recomId
		if (typeTag == 3) {
			request.addData("typeTag", typeTag);// 1:餐厅 2：推荐 3：榜单
		} else {
			request.addData("typeTag", 1);// 1:餐厅 2：推荐 3：榜单
		}

		request.addData("topRestTypeId", restTypeId);// typeTag=3为榜单id

		OpenPageDataTracer.getInstance().addEvent("页面查询");

		CommonTask.request(request, new CommonTask.TaskListener<PageRestInfo3DTO>() {

			@Override
			protected void onSuccess(PageRestInfo3DTO dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				if (dto != null) {
					restaurantInfo = dto.restInfo;
				}
				if (typeTag != 2) {
					RestaurantDetailActivity.this.getTvTitle().setText(dto.restInfo.name);
				}
				// 设置列表适配器
				setView();
				scrollView.post(new Runnable() {
					@Override
					public void run() {
						scrollView.scrollTo(0, 0);
					}
				});

				RestInfoData resInfoData = dto.restInfo;

				if (resInfoData.favTag) {
					RestaurantDetailActivity.this.getBtnOption().setSelected(true);
				} else {
					RestaurantDetailActivity.this.getBtnOption().setSelected(false);
				}

				SessionManager.getInstance().setRestaurantInfo(ContextUtil.getContext(), resInfoData);

				// 提示
				if (CheckUtil.isEmpty(resInfoData.hlHint)) {
					detailResRecomHintLayout.setVisibility(View.GONE);
				} else {
					hlUrl = resInfoData.hlUrl;
					detailResRecomHintLayout.setVisibility(View.VISIBLE);
					detailResRecomHintTxt.setText(Html.fromHtml(resInfoData.hlHint));
				}

				// --
				if(!isSimpleVer){
					
				
				if (dto.totalCommentNum == 0) {
					// 没有评论的场合
					plLayout.setVisibility(View.GONE);
				} else {
					plLayout.setVisibility(View.VISIBLE);
					resCommentLoadingLayout.setVisibility(View.GONE);
					commentNum.setText("(" + dto.totalCommentNum + ")");

					// 动态添加用户评论,如果已经有东西，西安清除以前的
					if (lvCommentList.getChildCount() > 0) {
						lvCommentList.removeAllViews();
					}
					int count = dto.commentList.size();
					for (int i = 0; i < count; i++) {
						CommentItem commentItem = new CommentItem(RestaurantDetailActivity.this, dto.commentList.get(i), restaurantId);
						lvCommentList.addView(commentItem);
						commentItem.setClickable(true);
						commentItem.setOnClickListener(new View.OnClickListener() {
							// // 去餐厅评论页面
							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								// -----
								OpenPageDataTracer.getInstance().addEvent("网友评论面板-更多评论");
								// -----
								Bundle bundle = new Bundle();
								bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
								ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantCommentActivity.class, 0, bundle);
							}
						});
					}

				}
				}else{
					plLayout.setVisibility(View.GONE);
					resCommentLoadingLayout.setVisibility(View.GONE);
				}
				
				// 获取标签列表 判断是否能打标签
				if (dto.restInfo.canAddLabelTag) {
					getResTagList(dto.restInfo.labelList);
				} else {
					res_tag_layout.setVisibility(View.GONE);// 取消标签框
					res_tag_button.setVisibility(View.GONE);// 取消底部面板 打标签
				}

				// 获取这家餐厅附近查找标签
				if (dto.restInfo.searchMenuTypeList != null) {
					getResSerachTagList(dto.restInfo.searchMenuTypeList);
				} else {
					res_search_menu_tag_layout.setVisibility(View.GONE);
				}
           
				// 跳转逻辑
				jumpToNextPage();
			}

			@Override
			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				DialogUtil.showToast(RestaurantDetailActivity.this, message);
				// doTest();
			}

			// @Override
			// protected void defineCacheKeyAndTime(CacheKeyAndTime keyAndTime)
			// {
			// keyAndTime.cacheKey = restaurantId;
			// keyAndTime.cacheTimeMinute = 30;
			// }

			private void doTest() {
				String json1 = "";
				String json = "{\"restInfo\":{\"uuid\":\"123456\",\"name\":\"牛肉粉馆\",\"favTag\":\"true\",\"restPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"restPicNum\":\"25\",\"avgPrice\":\"200\",\"tasteNum\":\"好\",\"envNum\":\"好\",\"serviceNum\":\"好\",\"canBookingTag\":\"true\",\"telForBooking\":\"57575777\",\"telForEdit\":\"57575777\",\"address\":\"浦东大道\",\"longitude\":\"0.00\",\"latitude\":\"0.00\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"},{\"isTelCanCall\":\"true\",\"tel\":\"57575777\",\"cityPrefix\":\"010\",\"branch\":\"123\"}],\"openTimeInfo\":\"2\",\"menuTypeInfo\":\"湘菜\",\"trafficLine\":\"交通路线\",\"busInfo\":\"公交信息\",\"consumeType\":\"消费方式\",\"parkingPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"promoList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"couponList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"mealComboList\":[{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"},{\"typeTag\":\"1\",\"title\":\"标题\",\"content\":\"内容\",\"couponId\":\"123456\",\"couponValue\":\"100\",\"couponUnitPrice\":\"500\",\"couponDiscount\":\"100\",\"couponUseHint\":\"温馨提示\",\"couponUserBeginTime\":\"10\",\"couponUserEndTime\":\"20\"}],\"totalSpecialFoodNum\":\"20\",\"specialFoodList\":[{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}},{\"uuid\":\"123456\",\"name\":\"特色菜名称\",\"price\":\"100\",\"hotNum\":\"200\",\"intro\":\"介绍 \",\"unit\":\"单位\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picOriginalUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"smallStyleId\":\"123456\",\"smallStyleName;\":\"小菜系名称\",\"totalCommentNum\":\"10\",\"commentData\":{\"uuid\":\"123456\",\"userName\":\"评论人\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"createTime\":\"2012\",\"detail\":\"评论内容\",\"likeTypeTag\":\"1\",\"likeTypeName\":\"喜欢类型名\",\"totalCommentNum\":\"5\",\"foodId\":\"123456\"}}],\"cityId\":\"13456\",\"regionId\":\"132456\",\"regionName\":\"浦东新区\",\"districtId\":\"123456\",\"districtName\":\"上海\",\"mainMenuId\":\"123456\",\"mainMenuName\":\"菜单名字\",\"ydzkDetail\":\"预订折扣信息\",\"xjqDetail\":\"现金券信息\",\"cxDetail\":\"促销信息\",\"recomData\":{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"linkUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"},\"recomInfo\":{\"uuid\":\"123456\",\"shareInfo\":{\"shareSmsDetail\":\"shareSmsDetail\",\"shareEmailDetail\":\"shareEmailDetail\",\"shareWeiboDetail\":\"shareWeiboDetail\",\"shareWeixinIconUrl\":\"shareWeixinIconUrl\",\"shareWeixinDetailUrl\":\"shareWeixinDetailUrl\",\"shareWeixinDetail\":\"shareWeixinDetail\",\"shareWeixinName\":\"shareWeixinName\",\"shareWeiboUuid\":\"shareWeiboUuid\"},\"favTag\":\"true\",\"title\":\"title\",\"createTime\":\"2014-12-23\",\"restId\":\"123456\",\"restName\":\"restName\",\"userId\":\"userId\",\"userNickName\":\"userNickName\",\"userPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"userIsVipTag\":\"true\",\"picList\":[{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"},{\"smallPicUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\",\"title\":\"标题\",\"priceInfo\":\"价格描述\"}],\"totalLikeNum\":\"2000\",\"likedTag\":\"true\",\"tryRecomHint\":\"我要挑战说明\",\"relateRecomList\":[{\"title\":\"title\",\"detail\":\"detaildetaildetaildetail\",\"userNickName\":\"Name\",\"createTime\":\"2013-02-2\",\"restId\":\"123456\",\"picUrl\":\"http://f3.95171.cn/pic/AESH10000743/small/c7446f01-d287-4468-963a-343bf750782c.jpg\"}]},\"showTypeTag\":\"1\"}";
				PageRestInfo3DTO dto = JsonUtils.fromJson(json, PageRestInfo3DTO.class);
				onSuccess(dto);
			}
		});
	}

	private void setView() {

		if (restaurantInfo == null) {
			return;
		}
	
			
		// 顶部面板
		if (typeTag == 3) {
			// 榜单餐厅图片
			if (CheckUtil.isEmpty(restaurantInfo.restPicUrlForTopRest)) {
				ivResPic.setImageResource(ImageUtil.loading);
			} else {
				ivResPic.setImageByUrl(restaurantInfo.restPicUrlForTopRest, true, 0, ScaleType.CENTER_CROP);
			}

			detail_top_res_reason.setText(restaurantInfo.topRestReason);
			int textNum = (int) (UnitUtil.px2dip(detail_top_res_reason.getWidth()) / 16);
			int textTotal = textNum * 3;
			if (textTotal < restaurantInfo.topRestReason.length()) {
				detail_top_san_jiao.setVisibility(View.VISIBLE);
				detail_top_res_bt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// -----
						ViewUtils.preventViewMultipleClick(v, 1000);
						OpenPageDataTracer.getInstance().addEvent("榜单介绍-展开按钮");
						// -----

						detail_top_res_reason.setMaxLines(1000);
						detail_top_res_reason.setText(restaurantInfo.topRestReason);
						detail_top_san_jiao.setVisibility(View.GONE);
					}
				});
			} else {
				detail_top_san_jiao.setVisibility(View.GONE);
			}
		} else {

			rest_detail_name.setText(restaurantInfo.name + "");
			/**
			 * 图片赋值
			 */
			resLogoUrl = restaurantInfo.restPicUrl;
			// 餐厅图片
			if (CheckUtil.isEmpty(resLogoUrl)) {
				ivResPic.setImageResource(ImageUtil.loading);
			} else {
				ivResPic.setImageByUrl(resLogoUrl, true, 0, ScaleType.CENTER_CROP);
			}

			tvTasteNum.setText(restaurantInfo.tasteNum);
			tvEnvNum.setText(restaurantInfo.envNum);
			tvServiceNum.setText(restaurantInfo.serviceNum);
			tvAvgPrice.setText(restaurantInfo.avgPrice);

			if (CheckUtil.isEmpty(resLogoUrl) || restaurantInfo.restPicNum == 0) {
				// 无图片时
				picNum.setVisibility(View.GONE);
				ivResPic.setImageResource(R.drawable.no_pic_of_rest);
			} else {
				// 有图片时，显示图片和图片数量
				picNum.setVisibility(View.VISIBLE);
				picNum.setText("共" + restaurantInfo.restPicNum + "张");
				ivResPic.setImageByUrl(restaurantInfo.restPicUrl, true, 0, ScaleType.CENTER_CROP);
			}
		}

		// 餐厅图片点击事件
		ivResPic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (typeTag == 3) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("榜单介绍-更多图片");
					// -----
				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("顶部面板-缩略图按钮");
					// -----
				}

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_EVN);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantPicActivity.class, 0, bundle);
			}
		});

		if (restaurantInfo.address != null) {
			tvAddress.setText(restaurantInfo.address);
		}
		/**********************************************/
		// 吃货荐店Item内容设置

		if (restaurantInfo.totalRecomNum > 0) {
			detail_res_layout.setVisibility(View.VISIBLE);
			tv_recommend_address.setText(restaurantInfo.recomData.detail);// 设置内容
			tv_recommend_explain.setText(restaurantInfo.recomData.title);// 设置标题
			if ("".equals(restaurantInfo.recomData.createTime.trim())) {
				tv_recommend_time.setText(R.string.text_foodie_txt);

			} else {
				tv_recommend_time.setText(restaurantInfo.recomData.createTime);// 设置创建时间
			}
			tv_usersname.setText(restaurantInfo.recomData.userNickName);
		} else {
			detail_res_layout.setVisibility(View.GONE);
		}
		/************************************************/
		// 位置地图
		if (restaurantInfo.bdLat > 0 && restaurantInfo.bdLon > 0) {
//			GeoPoint gcj = new GeoPoint((int) (restaurantInfo.latitude * 1E6), (int) (restaurantInfo.longitude * 1E6));
//			GeoPoint baidu = CoordinateConvert.fromGcjToBaidu(gcj);
			String lola = restaurantInfo.bdLon + "," + restaurantInfo.bdLat;
			String url = "http://api.map.baidu.com/staticimage?width=500&height=120&center=" + lola + "&markers=" + lola + "&zoom=17&markerStyles=s,A,0xff0000";
			if(!isSimpleVer){
				
			
			rest_location_map.setVisibility(View.VISIBLE);
			rest_location_map.setImageByUrl(url, true, 0, ScaleType.FIT_XY);
			rest_location_map.setOnClickListener(new OnClickListener() {
				// 地图查看
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-地图");
					// -----

					if (!showMap(restaurantInfo, RestaurantDetailActivity.this)) {
						DialogUtil.showToast(RestaurantDetailActivity.this, "无法打开地图模式");
					}
				}
			});
		} else {
			rest_location_map.setVisibility(View.GONE);
		}
			}else{
				rest_location_map.setVisibility(View.GONE);
			}
		// 添加号码按钮事件，跳转到单行文本框的报错页面提交号码
		btnAddPhone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-添加电话");
				// -----
				Bundle data = new Bundle();
				ErrorReportTypeData defaultErrorData = new ErrorReportTypeData();
				defaultErrorData.setTypeId("13"); // 错误id，13：联系电话错误
				defaultErrorData.setTypeName(Settings.ERROR_REPORT_TITLE_ADD_PHONE_IN_REST_DETAIL);
				defaultErrorData.setInputBoxTitle("请输入号码");
				defaultErrorData.setFuncTag(2);
				defaultErrorData.setKeyboardTypeTag(2); // 默认是输入数字
				data.putSerializable("ErrorReportTypeData", defaultErrorData);
				data.putInt("typeTag", 1); // 1-餐厅报错 2-菜品
				data.putString(Settings.UUID, restaurantId);
				data.putInt(Settings.BUNDLE_KEY_ID, 101);
				ActivityUtil.jump(RestaurantDetailActivity.this, ErrorReportActivity.class, fromPage, data);

			}
		});

		// ---
		tvTel.setText("");
		if (restaurantInfo.telList != null && restaurantInfo.telList.size() > 0) {//

			final List<RestTelInfo> tels = restaurantInfo.telList;
			// tvTel.setText(restaurantInfo.getTel());

			for (int i = 0; i < tels.size(); i++) {
				final RestTelInfo tempTelInfo = tels.get(i);
				if (CheckUtil.isPhone(tempTelInfo.tel)) {
					final int ii = i;
					if (tempTelInfo.isTelCanCall) {
						ViewUtils.appendSpanToTextView(tvTel, tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch),
								new ClickableSpan() {

									@Override
									public void onClick(View widget) {

										// -----
										OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-电话按钮", tempTelInfo.tel);
										// -----

										//
										ActivityUtil.callSuper57(RestaurantDetailActivity.this, tempTelInfo.cityPrefix + tempTelInfo.tel);
										//
										final String userPhone;
										// 确定拨打的用户手机号
										if (SessionManager.getInstance().isUserLogin(RestaurantDetailActivity.this)) {
											userPhone = SessionManager.getInstance().getUserInfo(RestaurantDetailActivity.this).getTel();
										} else {
											userPhone = SharedprefUtil.get(RestaurantDetailActivity.this, Settings.ANONYMOUS_TEL, "");
										}

										// 向后台传拨打数据
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													ServiceRequest.callTel(2, restaurantId, "(" + tempTelInfo.cityPrefix + ")" + tempTelInfo.tel
															+ (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch));
												} catch (Exception e) {
													e.printStackTrace();
												} finally {
													// DialogUtil.showToast(activity,
													// "Finish call api");
													// callPostOverListener.sendEmptyMessage(HANDLE_POST_OVER);
												}
											}
										}).start();

									}

								});
					} else {
						// 只显示电话和分机号
						tvTel.append(tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch));
					}
					// ViewUtils.setURL(tvTel, position, position+end,
					// "tel:"+tels[i],true);
				}
				// 加逗号
				if (i < tels.size() - 1) {
					tvTel.append("　");
				}
			}

		} else {
			// tvTel.setText("暂无");
			// 没有电话时显示“添加号码”按钮
			btnAddPhone.setVisibility(View.VISIBLE);
			tvTel.setVisibility(View.GONE);

		}

		// 吃货荐店图片加载
		foodieImgUrl = restaurantInfo.recomData.picUrl;
		if (CheckUtil.isEmpty(foodieImgUrl)) {
			foodie_headImg.setImageResource(R.drawable.no_pic_of_rest);
		} else {
			foodie_headImg.setImageByUrl(restaurantInfo.recomData.picUrl, true, 0, ScaleType.CENTER_CROP);
		}

		llSpecialFoodTitle.setVisibility(View.VISIBLE);
		tvDishOnline.setVisibility(View.GONE);

		// 可否预订状态-----------------------------------------------------------------------------------------------

		// 不能预订，但是有预订电话，就显示电话预订的按钮
		if (!CheckUtil.isEmpty(restaurantInfo.bookingBtnName)) {
			predetermine.setText(restaurantInfo.bookingBtnName);
		}

		if (restaurantInfo.canBookingTag) {
			// if (false) {
			// TODO 预定按钮
			predetermine.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
                     ViewUtils.preventViewMultipleClick(v,1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("顶部面板-预订按钮");
					// -----
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
					ActivityUtil.jump(RestaurantDetailActivity.this, MyBookRestaurantActivity.class, 0, bundle);
				}
			});

		} else if (!TextUtils.isEmpty(restaurantInfo.telForBooking)) {
			// if (true) {
			predetermine.setText("电话预定");
			predetermine.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// DialogUtil.showAlert(RestaurantDetailActivity.this, true,
					// "电话预定", restaurantInfo.telForBooking, "取消", "呼叫",
					DialogUtil.showAlert(RestaurantDetailActivity.this, true, "电话预定", restaurantInfo.telForBooking, "取消", "呼叫", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// -----
							OpenPageDataTracer.getInstance().addEvent("顶部面板-电话按钮", "");
							// -----
							ActivityUtil.callSuper57(RestaurantDetailActivity.this, restaurantInfo.telForBooking);

							// 向后台传拨打数据
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										ServiceRequest.callTel(2, restaurantId, restaurantInfo.telForBooking);
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
									}
								}
							}).start();
							//
							dialog.dismiss();
						}

					});
				}
			});

		} else {
			// 不支持订餐的场合
			// predetermine.setBackgroundResource(R.drawable.res_no_predetermine);
			predetermine.setVisibility(View.GONE);
			predetermine.setEnabled(false);
		}

		if (restaurantInfo.totalSpecialFoodNum == 0 || restaurantInfo.specialFoodList == null || restaurantInfo.specialFoodList.size() == 0) {
			// 没有菜品的场合
			tvFoodListLayout.setVisibility(View.GONE);
			addFoodLayout.setVisibility(View.VISIBLE);
			tscLayout.setVisibility(View.GONE);
			addFood.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {// 添加菜品，随手拍功能
					ViewUtils.preventViewMultipleClick(v, 1000);
					try {
						takePic(new OnShowUploadImageListener() {

							@Override
							public void onGetPic(Bundle bundle) {

								onFinishTakePic(Settings.UPLOAD_TYPE_FOOD, restaurantId, resName);// 菜品上传
							}
						}, false);
					} catch (Exception ex) {
						Log.e("OnShowUploadImageListener: Error report", ex.getMessage(), ex);
					}
				}
			});
		} else {
			tscLayout.setVisibility(View.VISIBLE);
			tvFoodListLayout.setVisibility(View.VISIBLE);
			addFoodLayout.setVisibility(View.GONE);
			// 拼接关键字
			String resFood = initBeforeEllipsize();
			tvFoodList.setStartEndPoi(startEndPoi);
			tvFoodList.setMaxLines(3);
			tvFoodList.setText(resFood);
			specialFoodNum.setText("(" + restaurantInfo.totalSpecialFoodNum + ")");

			// tscLayout.setOnClickListener(new OnClickListener() {
			tvFoodListLayout.setOnClickListener(new OnClickListener() {
				// 去餐厅美食页面
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("特色菜面板-更多菜品");
					// -----

					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
					bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_FOOD);
					ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantPicActivity.class, 0, bundle);

				}
			});

		}
		// 地址切换地图
		tvAddressLayout.setOnClickListener(new OnClickListener() {
			// 去餐厅介绍页面
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-地图");
				// -----

				if (!showMap(restaurantInfo, RestaurantDetailActivity.this)) {
					DialogUtil.showToast(RestaurantDetailActivity.this, "无法打开地图模式");
				}

				// -----
				// OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-地址");
				// // -----
				//
				// Bundle bundle = new Bundle();
				// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				// ActivityUtil.jump(RestaurantDetailActivity.this,
				// RestaurantInfoActivity.class, 0, bundle);
			}
		});

		otherResInfoLayout.setOnClickListener(new OnClickListener() {
			// 去餐厅介绍页面
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅基本信息面板-餐厅介绍");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantInfoActivity.class, 0, bundle);
			}
		});

		detail_res_dish_order.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("特色菜面板-预点菜");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, restaurantId);
				bundle.putInt(Settings.FROM_TAG, 1);
				ActivityUtil.jump(RestaurantDetailActivity.this, DishListActivity.class, 0, bundle);

			}
		});
		// 吃货荐店跳转页面
		recommendLayout.setOnClickListener(new OnClickListener() {
			// 去地图页面
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("吃货荐店面板-更多");
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantFoodieListActivity.class, 0, bundle);
				// -----
				// showRoutePanel();
			}
		});
		res_error_button.setOnClickListener(new OnClickListener() {
			// 报错
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("底部面板-报错");
				// -----

				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_ERROR_REPORT_TYPE, Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_RESTAURANT);
				bundle.putString(Settings.UUID, restaurantId);
				bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
				bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo != null ? restaurantInfo.name : "-");
				bundle.putDouble(Settings.BUNDLE_REST_LONGITUDE, restaurantInfo.longitude);
				bundle.putDouble(Settings.BUNDLE_REST_LATITUDE, restaurantInfo.latitude);
				try {
					DialogUtil.showErrorReportTypeSelectionDialog(RestaurantDetailActivity.this, bundle);
				} catch (Exception ex) {
					Log.e("RestaurantDetailActivity: Error report", ex.getMessage(), ex);
					// e.printStackTrace();
				}
			}
		});

		res_add_photo_button.setOnClickListener(new OnClickListener() {
			// 去上传图片页面
			@Override
			public void onClick(View view) {
				ViewUtils.preventViewMultipleClick(view, 1000);
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().addEvent("底部面板-上传图片");
				// -----
//				if (typeTag == 2) {
					// 为了上传图片结束后，跳转页面服务
					Settings.uploadPictureOrignalActivityClazz = RestaurantDetailMainActivity.class;
					Settings.isRestaurantRecommentDetail = true;
//				}
//				else {
//					// 为了上传图片结束后，跳转页面服务
//					Settings.uploadPictureOrignalActivityClazz = RestaurantDetailActivity.class;
//					Settings.isRestaurantRecommentDetail = false;
//				}
				try {
					takePic(new OnShowUploadImageListener() {

						@Override
						public void onGetPic(Bundle bundle) {
							onFinishTakePic(Settings.UPLOAD_TYPE_RESTAURANT, restaurantId, restaurantInfo.name);

						}
					}, false);

				} catch (Exception ex) {
					Log.e("MainFrameActivity[option button]: Error report", ex.getMessage(), ex);
				}
			}

		});

		res_add_comment_button.setOnClickListener(new OnClickListener() {
			// 去添加评论页面
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				// -----
				ViewUtils.preventViewMultipleClick(view, 1000);
				OpenPageDataTracer.getInstance().addEvent("底部面板-添加点评");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putLong(Settings.FROM_TAG, 1);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantCommentSubmitActivity.class, 0, bundle);

			}
		});

		res_tag_button.setOnClickListener(new OnClickListener() {
			// 去打标签页面
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				// -----
				ViewUtils.preventViewMultipleClick(view, 1000);
				OpenPageDataTracer.getInstance().addEvent("底部面板-打标签");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putInt("toPage", 2);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantTagActivity.class, 0, bundle);

			}
		});

		res_tag_bt.setOnClickListener(new OnClickListener() {
			// 去查看标签
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("餐厅标签面板-更多标签");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putInt("toPage", 1);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantTagActivity.class, 0, bundle);
			}
		});

		resInfoLoadingLayout.setVisibility(View.GONE);
		resOtherInfoLayout.setVisibility(View.VISIBLE);
		resOtherInfoLoadingLayout.setVisibility(View.GONE);
		// --------------
		// 构建优惠信息
		buildPromotionList();

		// 设置Mainframe中的Bundle信息，用于弹出餐厅报错时使用
		bundleData.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
		bundleData.putDouble(Settings.BUNDLE_REST_LONGITUDE, restaurantInfo.longitude);
		bundleData.putDouble(Settings.BUNDLE_REST_LATITUDE, restaurantInfo.latitude);

	}

	/**
	 * 构建优惠信息列表
	 */
	private void buildPromotionList() {
	
		discountList.removeAllViews();
		if(!isSimpleVer){

		List<RestPromoData> all = new ArrayList<RestPromoData>();
		List<RestPromoData> promoList = restaurantInfo.promoList; // 优惠列表
		List<RestPromoData> mibiList = new ArrayList<RestPromoData>(); // 秘币列表
		List<RestPromoData> couponList = restaurantInfo.couponList; // 现金券列表
		List<RestPromoData> mealComboList = restaurantInfo.mealComboList; // 套餐列表

		// //先分离出秘币列表
		// Iterator<ResPromoData> it=promoList.iterator();
		// while(it.hasNext()){
		// ResPromoData temp=it.next();
		// if(temp.getTypeTag()==4){
		// mibiList.add(temp);
		// it.remove();
		// }
		// }

		// 按顺序加入：券，套餐，惠，币
		if (mealComboList != null) {
			all.addAll(mealComboList);
		}
		if (couponList != null) {
			all.addAll(couponList);
		}
		if (promoList != null) {
			all.addAll(promoList);
		}
		if (all.size() == 0) {
			noDiscount.setVisibility(View.VISIBLE);
			// 无优惠信息时直接隐藏整个layout，不显示任何内容
			discountLayout.setVisibility(View.GONE);
			return;
		} else {
			noDiscount.setVisibility(View.GONE);
		}
		// --
		for (int i = 0; i < all.size(); i++) {
			RestPromoData temp = all.get(i);
			// 1：券 2：惠 3：币 4：币(高亮)
			if (temp.getTypeTag() == 2 || temp.getTypeTag() == 3 || temp.getTypeTag() == 4) {
				addPromotion1(temp, i);
			} else if (temp.getTypeTag() == 2) {// 返
				addPromotion2(temp, i);
			} else if (temp.getTypeTag() == 1) {// 券
				addPromotion3(temp, i);
			}
		}
		}else{
			
			discountLayout.setVisibility(View.GONE);
		}
	}

	

	// 券
	private void addPromotion3(final RestPromoData temp, int currentPosition) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);

		// --
		icon.setImageResource(R.drawable.coupon);
		price.setVisibility(View.VISIBLE);
		blockbuttonLayout.setBackgroundResource(R.drawable.button_light_color_effect);
		// blockbuttonLayout.setPadding(0, 0, 0,0);
		line.setVisibility(currentPosition == 0 ? View.GONE : View.VISIBLE);
		arrowRight.setPadding(0, 0, UnitUtil.dip2px(0), 0);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice() + "元");
		title.setText(temp.getTitle());
		content.setText("查看详情并购买");
		blockbuttonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("优惠信息面板-券");
				// -----

				SpecialRestData data = new SpecialRestData();
				data.setRestId(restaurantInfo.uuid);
				data.setRestName(restaurantInfo.name);
				data.setCouponId(temp.getCouponId());
				data.setCouponValue(temp.getCouponValue());
				data.setCouponUnitPrice(temp.getCouponUnitPrice());
				data.setCouponUseDescription(temp.getContent());
				data.setCouponUseHint(temp.getCouponUseHint());
				data.setCouponUserBeginTime(temp.getCouponUserBeginTime());
				data.setCouponUserEndTime(temp.getCouponUserEndTime());

				// 跳转到现金巻详情
				Bundle bundle = new Bundle();
				bundle.putString(Settings.UUID, data.getCouponId());
				bundle.putString(Settings.BUNDLE_REST_ID, data.getRestId());

				ActivityUtil.jump(RestaurantDetailActivity.this, GroupBuyDetailActivity.class, 0, bundle);
			}
		});
	
		discountList.addView(item);
	}

	// 返
	private void addPromotion2(RestPromoData temp, int currentPosition) {
		                                             
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);

		// --
		icon.setImageResource(R.drawable.refund);
		price.setVisibility(View.GONE);
		content.setVisibility(View.GONE);
		blockbuttonLayout.setBackgroundResource(R.drawable.button_light_color_effect);
		// blockbuttonLayout.setPadding(0, 0, 0, 0);
		line.setVisibility(currentPosition == 0 ? View.GONE : View.VISIBLE);
		arrowRight.setPadding(0, 0, UnitUtil.dip2px(0), 0);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice());
		title.setText(temp.getTitle());
		title.setTextColor(0xFFFF3300);
		content.setText(temp.getContent());
		blockbuttonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----

				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantDiscountActivity.class, 0, bundle);

			}
		});
		
		discountList.addView(item);
	}

	// 惠
	private void addPromotion1(final RestPromoData temp, int currentPosition) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		LineView line = (LineView) item.findViewById(R.id.horizontal_line);
		ImageView icon = (ImageView) item.findViewById(R.id.discount_icon);
		TextView promotion_mibi_text = (TextView) item.findViewById(R.id.promotion_mibi_text);
		TextView title = (TextView) item.findViewById(R.id.restautant_discount_title);
		TextView price = (TextView) item.findViewById(R.id.restautant_discount_price);
		TextView content = (TextView) item.findViewById(R.id.restautant_discount_content);
		ImageView arrowRight = (ImageView) item.findViewById(R.id.arrow_right);

		// 2：惠 3：币 4：币(高亮)
		if (temp.getTypeTag() == 4) {
			// 币高亮
			icon.setImageResource(R.drawable.icon_mibi_2);
			promotion_mibi_text.setVisibility(View.VISIBLE);
			promotion_mibi_text.setText(temp.pct);
		} else if (temp.getTypeTag() == 3) {
			// 币
			icon.setImageResource(R.drawable.icon_mibi_1);
			promotion_mibi_text.setVisibility(View.VISIBLE);
			promotion_mibi_text.setText(temp.pct);
		} else {
			// 惠
			icon.setImageResource(R.drawable.discount);
			// 惠需要把打折的字样高亮放大
			if (!CheckUtil.isEmpty(temp.getTitle())) {
				String s = temp.getTitle().replaceFirst("(\\d+((\\.{0,1}\\d+)|(\\d*))折)", "<font color=\"#FF0000\"><big><b>$1</b></big></font>");
				temp.setTitle(s);
			}
		}
		price.setVisibility(View.GONE);
		content.setVisibility(View.GONE);
		blockbuttonLayout.setBackgroundResource(R.drawable.button_light_color_effect);
		// blockbuttonLayout.setPadding(0, 0, 0, 0);
		line.setVisibility(currentPosition == 0 ? View.GONE : View.VISIBLE);
		arrowRight.setPadding(0, 0, UnitUtil.dip2px(0), 0);
		// nested.setPadding(0, 0, 0, 0);

		price.setText(temp.getCouponUnitPrice());
		title.setText(Html.fromHtml(temp.getTitle()));
		content.setText(temp.getContent());
		blockbuttonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (temp.getTypeTag() == 3 || temp.getTypeTag() == 4) {
					// -----
					OpenPageDataTracer.getInstance().addEvent("优惠信息面板-币");
					// -----
					ActivityUtil.jumpToWeb(Settings.APP_WAP_BASE_URL + "coin", "积分兑换");
				} else {
					// -----
					OpenPageDataTracer.getInstance().addEvent("优惠信息面板-惠");
					// -----
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
					ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantDiscountActivity.class, 0, bundle);
				}
			}
		});
		
		discountList.addView(item);

	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		ivResPic.recycle(false);
		foodie_headImg.recycle(false);

		System.gc();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	enum RouteType {
		drive, // 驾车
		ride, // 公交
		walk; // 步行
	}

	private void showRoutePanel() {
		ActionItem driveAction = new ActionItem();
		driveAction.setTitle("自驾");
		driveAction.setIcon(getResources().getDrawable(R.drawable.res_route_drive));
		ActionItem busAction = new ActionItem();
		busAction.setTitle("公共交通");
		busAction.setIcon(getResources().getDrawable(R.drawable.res_route_bus));
		ActionItem walkAction = new ActionItem();
		walkAction.setTitle("步行");
		walkAction.setIcon(getResources().getDrawable(R.drawable.res_route_walk));

		driveAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				routeMap(RouteType.drive);
				mQuickAction.dismiss();
			}
		});

		busAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				routeMap(RouteType.ride);
				mQuickAction.dismiss();
			}
		});

		walkAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				routeMap(RouteType.walk);
				mQuickAction.dismiss();
			}
		});

		mQuickAction = new QuickAction(recommendLayout);
		mQuickAction.addActionItem(driveAction);
		mQuickAction.addActionItem(busAction);
		mQuickAction.addActionItem(walkAction);
		mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
		mQuickAction.show();
	}

	// 按照：高德，百度，谷歌的优先级顺序调用地图路线导航
	private void routeMap(final RouteType routeType) {
		// 如果既有百度又有高德，让用户选择
		if (needSelectMap()) {
			Builder bd = new Builder(this);
			// 设置title
			bd.setTitle("选择地图");
			bd.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			// ---------------
			// 生成列表项文字
			String[] itemTexts = new String[] { "高德地图", "百度地图" };
			// 用于在事件中访问
			bd.setItems(itemTexts, new android.content.DialogInterface.OnClickListener() {

				@Override
				// 定义列表项点击事件
				public void onClick(DialogInterface dialog, int which) {
					try {
						if (which == 0) {
							routeMapAutoNavi(routeType);
						} else if (which == 1) {
							routeMapBaidu(routeType);
						}
						dialog.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			bd.show();
			return;
		}

		// 高德320有bug
		if (ActivityUtil.isSoftwareAvailable("com.baidu.BaiduMap", 320) && routeMapBaidu(routeType)) {
			return;
		} else if (ActivityUtil.isSoftwareAvailable("com.google.android.apps.maps") && routeMapGoogle(routeType)) {
			return;
		} else if (ActivityUtil.isSoftwareAvailable("com.autonavi.minimap", 161) && ActivityUtil.getSoftwareVersionCode("com.autonavi.minimap") != 320 && routeMapAutoNavi(routeType)) {
			return;
		} else {
			DialogUtil.showToast(this, "没有可用的地图!");
		}
	}

	private boolean routeMapAutoNavi(RouteType routeType) {
		try {
			// V4.2.1以上(versionCode>=161) 注：320有bug
			String type = "0";
			switch (routeType) {
			case drive:
				type = "0";
				break;
			case ride:
				type = "1";
				break;
			case walk:
				type = "4";
				break;
			}
			Location loc = Loc.getLoc().getLoc();
			DecimalFormat decimalFormat = new DecimalFormat("###.########");
			StringBuilder sbParams = new StringBuilder("androidamap://route?sourceApplication=xms");
			sbParams.append("&t=" + type).append("&showType=1").append("&m=0").append("&dev=0");
			sbParams.append("&sname=我的位置&dname=" + restaurantInfo.name);
			sbParams.append("&slat=" + decimalFormat.format(loc.getLatitude()));
			sbParams.append("&slon=" + decimalFormat.format(loc.getLongitude()));
			sbParams.append("&dlat=" + decimalFormat.format(restaurantInfo.latitude));
			sbParams.append("&dlon=" + decimalFormat.format(restaurantInfo.longitude));
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setData(Uri.parse(sbParams.toString()));
			intent.setPackage("com.autonavi.minimap");
			startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean routeMapBaidu(RouteType routeType) {
		try {
			// V4.3.0以上(versionCode>=320)
			String type = "driving";
			switch (routeType) {
			case drive:
				type = "driving";
				break;
			case ride:
				type = "transit";
				break;
			case walk:
				type = "walking";
				break;
			}
			Location loc = Loc.getLoc().getLoc();
			DecimalFormat decimalFormat = new DecimalFormat("###.########");
			StringBuilder sbParams = new StringBuilder("intent://map/direction?");
			sbParams.append("src=xiaomishu");
			sbParams.append("&mode=" + type);
			sbParams.append("&coord_type=gcj02");
			sbParams.append("&origin=latlng:" + decimalFormat.format(loc.getLatitude()) + "," + decimalFormat.format(loc.getLongitude()) + "|name:我的位置");
			sbParams.append("&destination=latlng:" + decimalFormat.format(restaurantInfo.latitude) + "," + decimalFormat.format(restaurantInfo.longitude) + "|name:" + restaurantInfo.name);
			sbParams.append("#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			Intent intent = Intent.getIntent(sbParams.toString());

			startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean routeMapGoogle(RouteType routeType) {
		try {
			String type = "d";
			switch (routeType) {
			case drive:
				type = "d";
				break;
			case ride:
				type = "r";
				break;
			case walk:
				type = "w";
				break;
			}
			DecimalFormat decimalFormat = new DecimalFormat("###.########");
			StringBuilder sbParams = new StringBuilder("http://maps.google.com/?myl=saddr&dirflg=");
			sbParams.append(type);
			sbParams.append("&f=d&daddr=");
			sbParams.append(decimalFormat.format(restaurantInfo.latitude));
			sbParams.append(',');
			sbParams.append(decimalFormat.format(restaurantInfo.longitude));
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(sbParams.toString()));
			intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 显示地图模式
	 * 
	 * @return
	 */
	public static boolean showMap(RestInfoData restInfo, Activity activity) {
		// Bundle bundle = new Bundle();
		// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
		// Settings.RESTAURANT_DETAIL_ACTIVITY);
		// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantInfo.getId());
		// ActivityUtil.jump(this, MyMapActivity.class,
		// Settings.RESTAURANT_DETAIL_ACTIVITY, bundle);
		// return true;

		for (String type : MAP_TYPE) {
			if (launchMap(type, restInfo.name, restInfo.latitude, restInfo.longitude, activity)) {
				return true;
			}
		}
		return false;
	}

	private static boolean needSelectMap() {
		return ActivityUtil.isSoftwareAvailable("com.baidu.BaiduMap", 320) && ActivityUtil.isSoftwareAvailable("com.autonavi.minimap", 161)
				&& ActivityUtil.getSoftwareVersionCode("com.autonavi.minimap") != 320;
	}

	/**
	 * 调用指定的地图
	 * 
	 * @param type
	 * @return
	 */
	public static boolean launchMap(String type, final String restName, final double latitude, final double longitude, final Activity activity) {
		try {
			final DecimalFormat decimalFormat = new DecimalFormat("###.########");
			// 如果既有百度又有高德，让用户选择
			if (needSelectMap()) {
				Builder bd = new Builder(activity);
				// 设置title
				bd.setTitle("选择地图");
				bd.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				// ---------------
				// 生成列表项文字
				String[] itemTexts = new String[] { "高德地图", "百度地图" };
				// 用于在事件中访问
				bd.setItems(itemTexts, new android.content.DialogInterface.OnClickListener() {

					@Override
					// 定义列表项点击事件
					public void onClick(DialogInterface dialog, int which) {
						try {
							if (which == 0) {
								StringBuffer sbParams = new StringBuffer("androidamap://viewMap?");
								sbParams.append("sourceApplication=xiaomishu");
								sbParams.append("&poiname=" + restName);
								sbParams.append("&dev=0");
								sbParams.append("&lat=" + decimalFormat.format(latitude));
								sbParams.append("&lon=" + decimalFormat.format(longitude));
								//
								Intent intent = new Intent("android.intent.action.VIEW");
								intent.setData(Uri.parse(sbParams.toString()));
								intent.addCategory("android.intent.category.DEFAULT");
								intent.setPackage("com.autonavi.minimap");
								activity.startActivity(intent);
							} else if (which == 1) {
								StringBuffer sbParams = new StringBuffer("intent://map/marker?");
								sbParams.append("location=" + decimalFormat.format(latitude) + "," + decimalFormat.format(longitude));
								sbParams.append("&title=" + restName);
								sbParams.append("&content=" + restName);
								sbParams.append("&coord_type=gcj02");
								sbParams.append("&src=xiaomishu");
								sbParams.append("#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
								//
								Intent intent;

								intent = Intent.getIntent(sbParams.toString());

								intent.setPackage("com.baidu.BaiduMap");
								activity.startActivity(intent);
							}
							dialog.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				bd.show();
				return true;
			}
			// ----------
			StringBuffer sbParams = new StringBuffer("geo:");
			sbParams.append(decimalFormat.format(latitude));
			sbParams.append(',');
			sbParams.append(decimalFormat.format(longitude));
//			if (type.equals("谷歌")) {
//				sbParams.append("?q=");
//				sbParams.append(decimalFormat.format(latitude));
//				sbParams.append(',');
//				sbParams.append(decimalFormat.format(longitude));
//				sbParams.append("(").append(URLEncoder.encode(restName)).append(")");
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setData(Uri.parse(sbParams.toString()));
//				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//				activity.startActivity(intent);
//			} else 
			if (type.equals("百度")) {

				// Intent intent = new Intent(Intent.ACTION_VIEW);
				// sbParams.append(","+restaurantInfo.name);
				// Uri uri = Uri.parse(sbParams.toString());
				// intent.setData(uri);
				// intent.setPackage("com.baidu.BaiduMap");
				// this.startActivity(intent);

				// V4.3.0以上(versionCode>=320)
				if (!ActivityUtil.isSoftwareAvailable("com.baidu.BaiduMap", 320)) {
					return false;
				}
				sbParams = new StringBuffer("intent://map/marker?");
				sbParams.append("location=" + decimalFormat.format(latitude) + "," + decimalFormat.format(longitude));
				sbParams.append("&title=" + restName);
				sbParams.append("&content=" + restName);
				sbParams.append("&coord_type=gcj02");
				sbParams.append("&src=xiaomishu");
				sbParams.append("#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
				//
				Intent intent = Intent.getIntent(sbParams.toString());
				intent.setPackage("com.baidu.BaiduMap");
				activity.startActivity(intent);

			} else if (type.equals("高德")) {
				// V4.2.1以上(versionCode>=161)
				if (!ActivityUtil.isSoftwareAvailable("com.autonavi.minimap", 161)) {
					return false;
				}

				sbParams = new StringBuffer("androidamap://viewMap?");
				sbParams.append("sourceApplication=xiaomishu");
				sbParams.append("&poiname=" + restName);
				sbParams.append("&dev=0");
				sbParams.append("&lat=" + decimalFormat.format(latitude));
				sbParams.append("&lon=" + decimalFormat.format(longitude));
				//
				Intent intent = new Intent("android.intent.action.VIEW");
				intent.setData(Uri.parse(sbParams.toString()));
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setPackage("com.autonavi.minimap");
				activity.startActivity(intent);

			} else if (type.equals("Browser")) {
//				sbParams.delete(0, sbParams.length());
//				sbParams.append("http://ditu.google.cn/maps?q=");
//				sbParams.append(decimalFormat.format(latitude));
//				sbParams.append(',');
//				sbParams.append(decimalFormat.format(longitude));
//				sbParams.append("(").append(URLEncoder.encode(restName)).append(")");
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setData(Uri.parse(sbParams.toString()));
//				intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
//				activity.startActivity(intent);
				
				sbParams.delete(0, sbParams.length());
				sbParams.append("http://api.map.baidu.com/marker?");
				sbParams.append("location=" + decimalFormat.format(latitude) + "," + decimalFormat.format(longitude));
				sbParams.append("&title=" + URLEncoder.encode(restName));
				sbParams.append("&content=" + URLEncoder.encode(restName));
				sbParams.append("&output=" + "html");
				sbParams.append("&coord_type=gcj02");
				sbParams.append("&src=" + "订餐小秘书");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(sbParams.toString()));
				intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
				activity.startActivity(intent);		
//				ActivityUtil.jumbToWeb(activity, sbParams.toString());
			} else {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// private void jumpToDishList() {
	// DishOrderDTO dishOrder = SessionManager.getInstance().getDishOrder(this,
	// restaurantId);
	// if (dishOrder.getTimeStamp() + Settings.DISH_EXPIRED_TIME <
	// System.currentTimeMillis()) {
	// dishOrder.clearAll();
	// SessionManager.getInstance().setDishOrder(this, dishOrder, restaurantId);
	// }
	//
	// Bundle bundle = new Bundle();
	// if (CheckUtil.isEmpty(dishOrder.getTableId())) {
	// bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
	// bundle.putString(Settings.BUNDLE_TABLE_ID, "");
	// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
	// } else {
	// bundle.putString(Settings.BUNDLE_REST_ID, dishOrder.getRestId());
	// bundle.putString(Settings.BUNDLE_TABLE_ID, dishOrder.getTableId());
	// bundle.putString(Settings.BUNDLE_KEY_ID, dishOrder.getRestId());
	// }
	// // ActivityUtil.jump(this, DishListActivity.class,0 , bundle);
	// }

	// 在此拼接价格
	public String initBeforeEllipsize() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < restaurantInfo.specialFoodList.size(); i++) {
			int[] tempInteger = new int[2];
			sb.append(restaurantInfo.specialFoodList.get(i).getName());

			String price = restaurantInfo.specialFoodList.get(i).getPrice();

			if (price.equals("不详")) {
				// sb.append("(");
				// tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append("");
				// sb.append(price);
				// sb.append(")");
				// tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			} else if (price.equals("时价")) {
				sb.append("(");
				tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append(price);
				sb.append(")");
				tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			} else {
				sb.append("(");
				tempInteger[0] = sb.toString().length() - 1; // 取左括号下标
				sb.append("￥ ");
				sb.append(price);
				sb.append(")");
				tempInteger[1] = sb.toString().length() - 1; // 取右括号下标
			}
			if (tempInteger[0] != 0 && tempInteger[1] != 0) {
				startEndPoi.add(tempInteger);
			}

			if (i < restaurantInfo.specialFoodList.size() - 1) {
				sb.append(",");
			}

		}
		return sb.toString();

	}

	/**
	 * 自动跳转处理，从餐厅详情跳转到下一子页面
	 */
	private void jumpToNextPage() {
		try {
			if (TextUtils.isEmpty(mNextPage)) {
				return;
			}
			if (mNextPage.equals(URLExecutor.NEXT_PAGE_MAP)) {
				// 地图
				if (restaurantInfo.latitude == 0 || restaurantInfo.longitude == 0) {
					// 没有经纬度
					return;
				}
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅地图");
				// -----
				if (!showMap(restaurantInfo, RestaurantDetailActivity.this)) {
					DialogUtil.showToast(RestaurantDetailActivity.this, "无法打开地图模式");
				}
			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_PICTURE)) {
				// 餐厅图片列表
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅图片列表");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_EVN);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantPicActivity.class, 0, bundle);
			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_PICTURE_FOOD) || mNextPage.equals(URLExecutor.NEXT_PAGE_FOOD)) {
				// 餐厅图片列表
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅图片列表-菜式");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_FOOD);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantPicActivity.class, 0, bundle);
			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_DESCRIBE)) {
				// 餐馆描述
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅基本信息");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantInfoActivity.class, 0, bundle);
			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_DISCOUNT)) {
				// 优惠信息
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅优惠详情");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				// bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME,
				// getBookTime());
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantDiscountActivity.class, 0, bundle);
			}
			// else if (mNextPage.equals(URLExecutor.NEXT_PAGE_FOOD)) {
			// // -----
			// OpenPageDataTracer.getInstance().addEvent("跳转","餐厅菜品列表");
			// // -----
			// Bundle bundle = new Bundle();
			// bundle.putString(Settings.BUNDLE_KEY_TITLE, resName);
			// bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON,
			// getString(R.string.text_title_restaurant_detail));
			// String[] idArray = { restaurantId, "0", resName };
			// bundle.putStringArray(Settings.BUNDLE_KEY_ID, idArray);
			// ActivityUtil.jump(RestaurantDetailActivity.this,
			// RestaurantFoodListActivity.class, 0, bundle);
			// }
			else if (mNextPage.equals(URLExecutor.NEXT_PAGE_COMMENT)) {
				// 评论列表
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "餐厅评论列表");
				// -----
				Bundle bundle = new Bundle();
				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
				// bundle.putSerializable(Settings.BUNDLE_DEFAULT_BOOK_TIME,
				// getBookTime());
				ActivityUtil.jump(RestaurantDetailActivity.this, RestaurantCommentActivity.class, 0, bundle);

			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_DISH)) {
				// 点菜页
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "预点菜");
				// -----
				detail_res_dish_order.performClick();

			} else if (mNextPage.equals(URLExecutor.NEXT_PAGE_ORDER)) {
				// 预订
				// -----
				OpenPageDataTracer.getInstance().addEvent("跳转", "订单表单");
				// -----
				predetermine.performClick();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mNextPage = "";
	}

	/**
	 * 获得标签列表
	 */
	private void getResTagList(List<CommonTypeDTO> labelList) {
		if(!isSimpleVer){
			
		
		if (labelList.size() == 0) {
			res_tag_layout.setVisibility(View.GONE);// 标签列表为空 取消标签框

		} else {
			res_tag.removeAllViews();
			res_tag_layout.setVisibility(View.VISIBLE);
			int length = labelList.size();

			if (length % 2 == 0) {// 判断标签是偶
				for (int i = 0; i < length / 2; i++) {
					LayoutInflater inflater = LayoutInflater.from(RestaurantDetailActivity.this);

					View view = inflater.inflate(R.layout.res_tag, null);
					TextView res_one_tag1_name = (TextView) view.findViewById(R.id.res_one_tag1_name);
					TextView res_one_tag1_num = (TextView) view.findViewById(R.id.res_one_tag1_num);
					TextView res_one_tag2_name = (TextView) view.findViewById(R.id.res_one_tag2_name);
					TextView res_one_tag2_num = (TextView) view.findViewById(R.id.res_one_tag2_num);
					if (labelList.get(i * 2).getName() != null) {
						res_one_tag1_name.setText(textNamesize(labelList.get(i * 2).getName()));
						res_one_tag1_num.setTextScaleX(textNumsize("+" + labelList.get(i * 2).getNum()));
						res_one_tag1_num.setText("+" + labelList.get(i * 2).getNum());
						// res_one_tag1_num.setTextScaleX(size)
						//

					}
					if (labelList.get(i * 2 + 1).getName() != null) {
						res_one_tag2_name.setText(textNamesize(labelList.get(i * 2 + 1).getName()));
						res_one_tag2_num.setTextScaleX(textNumsize("+" + labelList.get(i * 2 + 1).getNum()));
						// res_one_tag2_num.setTextScaleX(textNumsize("+"+dto.getList().get(i
						// * 2+1).getNum()));
						res_one_tag2_num.setText("+" + labelList.get(i * 2 + 1).getNum());
						//
					}
					res_tag.addView(view, i);

				}

			} else {// 判断标签是单
				for (int i = 0; i < length / 2 + 1; i++) {
					View view = mInflater.inflate(R.layout.res_tag, null);
					TextView res_one_tag1_name = (TextView) view.findViewById(R.id.res_one_tag1_name);
					TextView res_one_tag1_num = (TextView) view.findViewById(R.id.res_one_tag1_num);
					TextView res_one_tag2_name = (TextView) view.findViewById(R.id.res_one_tag2_name);
					TextView res_one_tag2_num = (TextView) view.findViewById(R.id.res_one_tag2_num);
					LinearLayout layout2 = (LinearLayout) view.findViewById(R.id.res_one_tag2_bt);

					// 最后一排只有一个标签
					if (i == length / 2) {
						layout2.setVisibility(View.INVISIBLE);
						if (labelList.get(i * 2).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 2).getName()));
							res_one_tag2_num.setTextScaleX(textNumsize("+" + labelList.get(i * 2).getNum()));
							res_one_tag1_num.setText("+" + labelList.get(i * 2).getNum());

						}

					} else {
						// 一排有2个标签
						if (labelList.get(i * 2).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 2).getName()));
							res_one_tag2_num.setTextScaleX(textNumsize("+" + labelList.get(i * 2).getNum()));
							res_one_tag1_num.setText("+" + labelList.get(i * 2).getNum());

						}
						if (labelList.get(i * 2 + 1).getName() != null) {
							res_one_tag2_name.setText(textNamesize(labelList.get(i * 2 + 1).getName()));
							res_one_tag2_num.setTextScaleX(textNumsize("+" + labelList.get(i * 2 + 1).getNum()));
							res_one_tag2_num.setText("+" + labelList.get(i * 2 + 1).getNum());

						}
					}
					res_tag.addView(view, i);
				}
			}
		}
		}else{
			res_tag_layout.setVisibility(View.GONE);
		}

	}

	/**
	 * 餐厅附近查找标签
	 * 
	 * @param name
	 * @return
	 */

	private void getResSerachTagList(final List<CommonTypeDTO> labelList) {
		if(!isSimpleVer){
			
		
		if (labelList.size() == 0) {
			res_search_menu_tag_layout.setVisibility(View.GONE);// 标签列表为空 取消标签框

		} else {
			res_search_menu_tag.removeAllViews();

			res_search_menu_tag_layout.setVisibility(View.VISIBLE);
			int length = labelList.size();

			if (length % 3 == 0) {// 判断标签是一排是3个
				for (int i = 0; i < length / 3; i++) {
					final int j = i;
					LayoutInflater inflater = LayoutInflater.from(RestaurantDetailActivity.this);

					View view = inflater.inflate(R.layout.res_search_tag, null);
					TextView res_one_tag1_name = (TextView) view.findViewById(R.id.res_one_tag1_name);
					TextView res_one_tag2_name = (TextView) view.findViewById(R.id.res_one_tag2_name);
					TextView res_one_tag3_name = (TextView) view.findViewById(R.id.res_one_tag3_name);
					LinearLayout layout1 = (LinearLayout) view.findViewById(R.id.res_one_tag1_bt);
					LinearLayout layout2 = (LinearLayout) view.findViewById(R.id.res_one_tag2_bt);
					LinearLayout layout3 = (LinearLayout) view.findViewById(R.id.res_one_tag3_bt);
					if (labelList.get(i * 3).getName() != null) {
						res_one_tag1_name.setText(textNamesize(labelList.get(i * 3).getName()));

					}
					if (labelList.get(i * 3 + 1).getName() != null) {
						res_one_tag2_name.setText(textNamesize(labelList.get(i * 3 + 1).getName()));

					}

					if (labelList.get(i * 3 + 2).getName() != null) {
						res_one_tag3_name.setText(textNamesize(labelList.get(i * 3 + 2).getName()));

					}
					layout1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3).getParentId() + labelList.get(j * 3).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3).getParentId(), labelList.get(j * 3).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);

						}
					});
					layout2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 1).getParentId() + labelList.get(j * 3 + 1).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 1).getParentId(), labelList.get(j * 3 + 1).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					layout3.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 2).getParentId() + labelList.get(j * 3 + 2).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 2).getParentId(), labelList.get(j * 3 + 2).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					res_search_menu_tag.addView(view, i);

				}

			} else if (length % 3 == 2) {// 判断标签是最后一排是2个
				for (int i = 0; i < length / 3 + 1; i++) {
					final int j = i;
					View view = mInflater.inflate(R.layout.res_search_tag, null);
					TextView res_one_tag1_name = (TextView) view.findViewById(R.id.res_one_tag1_name);
					TextView res_one_tag2_name = (TextView) view.findViewById(R.id.res_one_tag2_name);
					TextView res_one_tag3_name = (TextView) view.findViewById(R.id.res_one_tag3_name);
					LinearLayout layout1 = (LinearLayout) view.findViewById(R.id.res_one_tag1_bt);
					LinearLayout layout2 = (LinearLayout) view.findViewById(R.id.res_one_tag2_bt);
					LinearLayout layout3 = (LinearLayout) view.findViewById(R.id.res_one_tag3_bt);

					// 最后一排只有2个标签
					if (i == length / 3) {
						layout3.setVisibility(View.INVISIBLE);
						if (labelList.get(i * 3).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 3).getName()));

						}

						if (labelList.get(i * 3 + 1).getName() != null) {
							res_one_tag2_name.setText(textNamesize(labelList.get(i * 3 + 1).getName()));

						}

					} else {
						// 一排有3个标签
						if (labelList.get(i * 3).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 3).getName()));

						}
						if (labelList.get(i * 3 + 1).getName() != null) {
							res_one_tag2_name.setText(textNamesize(labelList.get(i * 3 + 1).getName()));

						}
						if (labelList.get(i * 3 + 2).getName() != null) {
							res_one_tag3_name.setText(textNamesize(labelList.get(i * 3 + 2).getName()));

						}
					}

					layout1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3).getParentId() + labelList.get(j * 3).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3).getParentId(), labelList.get(j * 3).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					layout2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 1).getParentId() + labelList.get(j * 3 + 1).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 1).getParentId(), labelList.get(j * 3 + 1).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					layout3.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							// -----
							ViewUtils.preventViewMultipleClick(view, 1000);
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 2).getParentId() + labelList.get(j * 3 + 2).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 2).getParentId(), labelList.get(j * 3 + 2).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					res_search_menu_tag.addView(view, i);
				}
			} else if (length % 3 == 1) {// 判断标签是最后一排是1个
				for (int i = 0; i < length / 3 + 1; i++) {
					final int j = i;
					View view = mInflater.inflate(R.layout.res_search_tag, null);
					TextView res_one_tag1_name = (TextView) view.findViewById(R.id.res_one_tag1_name);
					TextView res_one_tag2_name = (TextView) view.findViewById(R.id.res_one_tag2_name);
					TextView res_one_tag3_name = (TextView) view.findViewById(R.id.res_one_tag3_name);
					LinearLayout layout1 = (LinearLayout) view.findViewById(R.id.res_one_tag1_bt);
					LinearLayout layout2 = (LinearLayout) view.findViewById(R.id.res_one_tag2_bt);
					LinearLayout layout3 = (LinearLayout) view.findViewById(R.id.res_one_tag3_bt);

					// 最后一排只有1个标签
					if (i == length / 3) {
						layout2.setVisibility(View.INVISIBLE);
						layout3.setVisibility(View.INVISIBLE);
						if (labelList.get(i * 3).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 3).getName()));

						}

					} else {
						// 一排有3个标签
						if (labelList.get(i * 3).getName() != null) {
							res_one_tag1_name.setText(textNamesize(labelList.get(i * 3).getName()));

						}
						if (labelList.get(i * 3 + 1).getName() != null) {
							res_one_tag2_name.setText(textNamesize(labelList.get(i * 3 + 1).getName()));

						}
						if (labelList.get(i * 3 + 2).getName() != null) {
							res_one_tag3_name.setText(textNamesize(labelList.get(i * 3 + 2).getName()));

						}
					}
					layout1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3).getParentId() + labelList.get(j * 3).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3).getParentId(), labelList.get(j * 3).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					layout2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							// -----
							ViewUtils.preventViewMultipleClick(view, 1000);
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 1).getParentId() + labelList.get(j * 3 + 1).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 1).getParentId(), labelList.get(j * 3 + 1).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);
						}
					});
					layout3.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							
							OpenPageDataTracer.getInstance().addEvent("附近搜索面板-按钮", labelList.get(j * 3 + 2).getParentId() + labelList.get(j * 3 + 2).getUuid());
							// -----

							setSearchFilter(labelList.get(j * 3 + 2).getParentId(), labelList.get(j * 3 + 2).getUuid());
							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_REST_NAME, restaurantInfo.name);
							ActivityUtil.jump(RestaurantDetailActivity.this, ResAndFoodListActivity.class, 0, bundle);

						}
					});

					res_search_menu_tag.addView(view, i);
				}
			}
		}
		}else{
			res_search_menu_tag_layout.setVisibility(View.GONE);// 标签列表为空 取消标签框
		}
		
	}

	// 判断字体不超过4个字符 超过后面用...表示
	private String textNamesize(String name) {
		StringBuffer stringBuffer = new StringBuffer();
		if (name.length() > 4) {
			stringBuffer.append(name.substring(0, 4));
			stringBuffer.append("...");
		} else {
			stringBuffer.append(name);
		}
		return stringBuffer.toString();
	}

	// 当num是2位数 缩放比例1.0f num为3位数0.8f 4位数0.6
	private float textNumsize(String num) {
		float size = 1.0f;
		if (num.length() <= 3) {
			size = 1.0f;
		} else if (num.length() == 4) {
			size = 0.8f;
		} else if (num.length() == 5) {
			size = 0.6f;
		}
		return size;
	}

	// 设置搜索的筛选条件
	private void setSearchFilter(String mainMenuId, String subMenuId) {
		filter.reset();
		filter.setRestId(restaurantId);
		filter.setMainMenuId(mainMenuId);
		filter.setSubMenuId(subMenuId);
		filter.setDistanceMeter(1000);

	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// Log.v("TAG", "rd");
	// }
	//
}
