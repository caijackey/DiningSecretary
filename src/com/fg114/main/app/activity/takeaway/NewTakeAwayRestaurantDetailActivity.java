package com.fg114.main.app.activity.takeaway;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IInterface;

import android.text.Html;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.RatingBar;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;
import com.fg114.main.app.activity.order.MyBookRestaurantActivity;

import com.fg114.main.app.activity.resandfood.RestaurantCommentActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDiscountActivity;
import com.fg114.main.app.activity.resandfood.RestaurantInfoActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;

import com.fg114.main.app.view.CommentImageHorizontalScrollView;
import com.fg114.main.app.view.CommentItem;
import com.fg114.main.app.view.LineView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MyListView;
import com.fg114.main.app.view.NewTakeawayCerDataItem;
import com.fg114.main.service.dto.ErrorReportTypeData;

import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.RestPromoData;
import com.fg114.main.service.dto.RestTelInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.SpecialRestData;
import com.fg114.main.service.dto.TakeoutCerData;
import com.fg114.main.service.dto.TakeoutCommentData;
import com.fg114.main.service.dto.TakeoutInfoData;
import com.fg114.main.service.dto.TakeoutInfoData2;

import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;

import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ImageUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.URLExecutor;

import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;
import com.google.xiaomishujson.Gson;

/**
 * 外卖餐厅详情的主页面 传入参数
 * 餐厅id FROM_TAG
 * @author sunquan
 * 
 */
public class NewTakeAwayRestaurantDetailActivity extends MainFrameActivity {
	private static final String[] MAP_TYPE = { "百度", "高德", "Browser" };
	// 传入参数
	private int fromPage; // 返回页面
	private String restaurantId; // 餐厅ID
	private String resLogoUrl;// 餐厅图片地址
	private String resName;// 餐厅名
	// 本地缓存数据
	private TakeoutInfoData2 takeoutInfo2;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	// 内容部分
	private ScrollView takeaway_detail_res_scrollBar;
	private MyImageView takeaway_detail_res_ivResPic;// 餐厅图片
	private MyImageView takeaway_detail_location_map;//餐厅位置图片
	private TextView takeaway_detail_res_tvResName;// 餐厅名称
	private RatingBar takeaway_res_detail_overall_num;// 总体评价
	private TextView takeaway_predeter;// 接受预定
	private TextView takeaway_detail_res_tvAddress;// 地址
	private TextView detail_res_tvtel;// 餐厅电话
	private Button detail_res_add_phone;// 添加电话按钮
	private TextView detail_res_time;// 营业时间
	private TextView takeaway_detail_res_introduction;// 餐厅简介
	private TextView takeaway_detail_res_comment_num;// 评论人数
	private TextView takeaway_detail_res_menutype;//风味
	private LinearLayout takeaway_newdetail_takeout_info_layout;//外送须知布局
	private TextView takeaway_detail_takeout_introduction;//外送须知
	private LinearLayout takeaway_discount_layout;
	private LinearLayout newdiscountLayout;
	private TextView takeaway_no_newdiscount;
	private TextView newtakeaway_detail_takeoutmsg;
	private LinearLayout takeaway_predeter_layout;
	private LinearLayout newtakeaway_newpredetermine_layout;
	// 评论
	private LinearLayout takeaway_comment_infoLayout;
	private LinearLayout takeaway_newlvcomment_layout;
	private MyImageView takeaway_comment_userphoto;
	private TextView takeaway_comment_tvUser;
	private TextView takeaway_comment_tvTime;
	private TextView takeaway_comment_detail;
	private RatingBar takeaway_comment_rating;
	private LinearLayout takeaway_comment_backlayout;
	private TextView  takeaway_comment_back;
	private View commentline;
	//餐厅资质
	private LinearLayout  res_newtag_layout;
	private LinearLayout newtakeaway_img_list;



	private LinearLayout takeaway_detail_res_resInfoLoadingLayout;// 提示信息
	private LinearLayout takeaway_detail_res_resOtherInfoLayout;
	private ViewGroup detail_res_tvAddress_layout;// 地址按钮
	private LinearLayout takeaway_detail_res_plLayout;// 评论
	private LinearLayout takeaway_detail_res_resCommentLoadingLayout;// 评论提示
	private LinearLayout takeaway_detail_res_resOtherInfoLoadingLayout;
	private LinearLayout newtakeaway_detail_top_layout;

	private Button savebutton;// 标题栏分享按钮
	private Button Orderbutton;//立即点菜按钮
	//优惠列表
	public List<MainPageAdvData> advList;    
	//点评
	public TakeoutCommentData commentData;
	//餐厅资质列表
	public List<TakeoutCerData> cerList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
		fromPage=bundle.getInt(Settings.BUNDLE_FROM_TAG,0);
		// 向父传递restaurantId，供公共报错页面中的餐厅报错使用
		//		// 测试ID
		//		restaurantId = "C30L22K17960";
		//		this.bundleData.putString(Settings.BUNDLE_KEY_ID, restaurantId);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅详情", restaurantId);
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
		} else {
			executeGetPageResInfoDataTask();
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅详情", restaurantId);
		// ----------------------------
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	// 拼接短信信息-----------------
	protected String makeSMSinfo() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareSmsDetail;
	}

	// 拼接邮件信息
	protected String makeEmailInfo() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareEmailDetail;

	}

	// 拼接微博信息
	protected String makeWeiboInfo() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeiboDetail;

	}
	// 拼接微信信息
	protected String  makeWeiXinInfo() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeixinDetail;
	}

	protected String getRestaurantUrl() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeixinIconUrl;
	}

	protected String getRestaurantLinkUrl() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeixinDetailUrl;
	}
	
	
	protected String getWeixinName() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeixinName;
	}

	protected String getWeiboUuid() {
		return takeoutInfo2.shareInfo == null ? "" :takeoutInfo2.shareInfo.shareWeiboUuid;
	}
	
	@Override
	protected String getRestaurantId() {
		return takeoutInfo2.uuid;
	}
	
	@Override
	protected String getRestaurantName() {
		return takeoutInfo2.name;
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
		this.getTvTitle().setText(R.string.text_title_restaurant_detail);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnTitle().setVisibility(View.GONE);
		savebutton = new Button(this);
		savebutton.setBackgroundResource(R.drawable.share2friend);
		savebutton.setWidth(UnitUtil.dip2px(25));
		savebutton.setHeight(UnitUtil.dip2px(25));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(savebutton.getLeft() - UnitUtil.dip2px(20), savebutton.getTop(), savebutton.getRight() + UnitUtil.dip2px(20), savebutton.getBottom());
		savebutton.setLayoutParams(lp);
		this.getTitleLayout().addView(savebutton);

		this.getBtnOption().setBackgroundResource(R.drawable.res_share_friends);

		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lps.setMargins(this.getBtnOption().getLeft() - UnitUtil.dip2px(40), this.getBtnOption().getTop(), this.getBtnOption().getRight() + UnitUtil.dip2px(40), this.getBtnOption().getBottom());
		this.getBtnOption().setLayoutParams(lps);
		this.getBtnOption().setWidth(UnitUtil.dip2px(25));
		this.getBtnOption().setHeight(UnitUtil.dip2px(25));

		this.getBottomLayout().setVisibility(View.GONE);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.takeaway_restaurant_newdetail, null);
		takeaway_detail_res_scrollBar = (ScrollView) contextView.findViewById(R.id.newtakeaway_detail_res_scrollBar);
		takeaway_detail_res_ivResPic = (MyImageView) contextView.findViewById(R.id.newtakeaway_detail_res_ivResPic);
		// takeaway_detail_res_lvCommentList = (MyListView) contextView
		// .findViewById(R.id.takeaway_detail_res_lvCommentList);
		newtakeaway_detail_top_layout=(LinearLayout) contextView.findViewById(R.id.newtakeaway_detail_top_layout);
		takeaway_detail_res_tvResName = (TextView) contextView.findViewById(R.id.newtakeaway_detail_res_tvResName);
		takeaway_predeter = (TextView) contextView.findViewById(R.id.newtakeaway_predeter);
		takeaway_predeter_layout=(LinearLayout) contextView.findViewById(R.id.newtakeaway_predeter_layout);
		newtakeaway_detail_takeoutmsg = (TextView) contextView.findViewById(R.id.newtakeaway_detail_takeoutmsg);
		newtakeaway_newpredetermine_layout=(LinearLayout) contextView.findViewById(R.id.newtakeaway_newpredetermine_layout);
		Orderbutton=(Button) contextView.findViewById(R.id.newpredetermine);

		takeaway_detail_location_map=(MyImageView)contextView.findViewById(R.id.newrest_location_map);

		takeaway_detail_res_tvAddress = (TextView) contextView.findViewById(R.id.newtakeaway_detail_res_tvaddress);
		detail_res_tvtel = (TextView) contextView.findViewById(R.id.newdetail_res_tvtel);
		detail_res_time = (TextView) contextView.findViewById(R.id.newdetail_res_time);

		takeaway_detail_res_introduction = (TextView) contextView.findViewById(R.id.newtakeaway_detail_res_introduction);
		takeaway_detail_res_menutype = (TextView) contextView.findViewById(R.id.newtakeaway_detail_res_menutype);
		
		takeaway_newdetail_takeout_info_layout=(LinearLayout) contextView.findViewById(R.id.takeaway_newdetail_takeout_info);
		takeaway_detail_takeout_introduction=(TextView)contextView.findViewById(R.id.newtakeaway_detail_takeout_introduction);
		// 优惠信息
		newdiscountLayout = (LinearLayout) contextView.findViewById(R.id.takeaway_newdetail_dis_info);
		takeaway_discount_layout=(LinearLayout) contextView.findViewById(R.id.newdetail_restaurant_discount_list);
		takeaway_no_newdiscount=(TextView) contextView.findViewById(R.id.detail_restaurant_no_newdiscount);





		detail_res_add_phone = (Button) contextView.findViewById(R.id.takeaway_newdetail_res_add_phone);

		takeaway_res_detail_overall_num = (RatingBar) contextView.findViewById(R.id.newtakeaway_res_detail_overall_num);

		takeaway_detail_res_resInfoLoadingLayout = (LinearLayout) contextView.findViewById(R.id.newtakeaway_detail_res_resInfoLoadingLayout);
		detail_res_tvAddress_layout = (ViewGroup) contextView.findViewById(R.id.takeaway_newdetail_res_tvaddress_layout);

		takeaway_detail_res_resCommentLoadingLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_newresCommentLoadingLayout);

		takeaway_detail_res_resOtherInfoLoadingLayout = (LinearLayout) contextView.findViewById(R.id.detail_res_newresOtherInfoLoadingLayout);

		// 评论
		takeaway_detail_res_plLayout=(LinearLayout) contextView.findViewById(R.id.newdetail_res_plLayout);
		takeaway_comment_infoLayout = (LinearLayout) contextView.findViewById(R.id.takeaway_detail_comment_infoLayout);
		takeaway_detail_res_comment_num = (TextView) contextView.findViewById(R.id.newdetail_res_comment_num);
		takeaway_comment_userphoto = (MyImageView) contextView.findViewById(R.id.takeaway_detail_comment_userphoto);
		takeaway_comment_tvUser = (TextView) contextView.findViewById(R.id.takeaway_detail_comment_tvUser);
		takeaway_comment_tvTime = (TextView) contextView.findViewById(R.id.takeaway_detail_comment_tvTime);
		takeaway_comment_detail = (TextView) contextView.findViewById(R.id.takeaway_detail_comment_tvComment);
		takeaway_comment_rating=(RatingBar) contextView.findViewById(R.id.newtakeaway_detail_commentrating_bar);
		commentline=(View)contextView.findViewById(R.id.newtakeaway_comment_line);
		takeaway_comment_backlayout=(LinearLayout) contextView.findViewById(R.id.newtakeaway_detail_comment_backlayout);
		takeaway_comment_back=(TextView) contextView.findViewById(R.id.newtakeaway_detail_comment_back);
		//餐厅资质
		res_newtag_layout=(LinearLayout) contextView.findViewById(R.id.new_res_newtag_layout);

		newtakeaway_img_list=(LinearLayout) contextView.findViewById(R.id.newtakeaway_img_list);
		//分享
		this.getBtnOption().setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("分享面板-分享");
				// -----

				showShareDialog(4);
			}
		});
		// TODO 收藏

		savebutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (!NewTakeAwayRestaurantDetailActivity.this.savebutton.isSelected()) {

					NewTakeAwayRestaurantDetailActivity.this.savebutton.setSelected(false);
					ServiceRequest request = new ServiceRequest(ServiceRequest.API.addTakeoutToFav);
					request.addData("uuid", restaurantId);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "收藏中...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// ----- 
							NewTakeAwayRestaurantDetailActivity.this.savebutton.setSelected(true);
							DialogUtil.showToast(NewTakeAwayRestaurantDetailActivity.this, "收藏成功");

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
							Void data = JsonUtils.fromJson(json, Void.class);
							onSuccess(data);

						}
					});

				} else {
					NewTakeAwayRestaurantDetailActivity.this.savebutton.setSelected(true);
					ServiceRequest request = new ServiceRequest(ServiceRequest.API.delTakeoutFromFav);
					request.addData("uuid", restaurantId);// 餐馆ID
					// -----
					OpenPageDataTracer.getInstance().addEvent("收藏按钮");
					// -----
					CommonTask.request(request, "取消收藏...", new CommonTask.TaskListener<Void>() {

						@Override
						protected void onSuccess(Void dto) {
							// -----
							OpenPageDataTracer.getInstance().endEvent("收藏按钮");
							// -----
							NewTakeAwayRestaurantDetailActivity.this.savebutton.setSelected(false);
							DialogUtil.showToast(NewTakeAwayRestaurantDetailActivity.this, "取消收藏成功");
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
							Void data = JsonUtils.fromJson(json, Void.class);
							onSuccess(data);

						}
					});

				}

			}
		});


		//		// 餐厅图片点击事件
		//		takeaway_detail_res_ivResPic.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				// -----
		//				OpenPageDataTracer.getInstance().addEvent("缩略图按钮");
		//				// -----
		//				Bundle bundle = new Bundle();
		//				bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
		//				bundle.putInt(Settings.BUNDLE_KEY_CONTENT, Settings.STATUTE_IMAGE_EVN);
		//				ActivityUtil.jump(TakeAwayRestaurantDetailActivity.this, RestaurantPicActivity.class, 0, bundle);
		//			}
		//		});
		//		// 餐厅名
		//		if (!CheckUtil.isEmpty(resName)) {
		//			takeaway_detail_res_tvResName.setText(resName);
		//		}

		// takeaway_detail_res_resInfoLoadingLayout.setVisibility(View.VISIBLE);
		// takeaway_detail_res_resOtherInfoLayout.setVisibility(View.GONE);
		//		takeaway_comment_infoLayout.setVisibility(View.GONE);
		// takeaway_detail_res_resCommentLoadingLayout.setVisibility(View.VISIBLE);
		// takeaway_detail_res_resOtherInfoLoadingLayout
		// .setVisibility(View.VISIBLE);

		this.setFunctionLayoutGone();

		// gradeAnimation = AnimationUtils.loadAnimation(this,
		// R.anim.restaurant_picture_detail_grade_rise_from_bottom);
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 获得外卖餐厅详细
	 */
	// TODO
	private void executeGetPageResInfoDataTask() {
		// 设置列表适配器
		takeaway_detail_res_scrollBar.post(new Runnable() {
			@Override
			public void run() {
				takeaway_detail_res_scrollBar.scrollTo(0, 0);
			}
		});



		ServiceRequest request = new ServiceRequest(ServiceRequest.API.getTakeoutInfo2);
		request.addData("uuid", restaurantId);// 外卖餐厅类别id 默认为空

		OpenPageDataTracer.getInstance().addEvent("页面查询");

		CommonTask.request(request, "正在获取餐厅信息，请等待...", new CommonTask.TaskListener<TakeoutInfoData2>() {

			@Override
			protected void onSuccess(TakeoutInfoData2 dto) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				takeoutInfo2 = new TakeoutInfoData2();
				takeoutInfo2 = dto;
				// 设置列表适配器
				setView();

				takeaway_detail_res_scrollBar.post(new Runnable() {
					@Override
					public void run() {
						takeaway_detail_res_scrollBar.scrollTo(0, 0);
					}
				});

				// List<TakeoutInfoData> resInfoData = dto.list;
				if (dto != null) {
					if (dto.favTag) {
						NewTakeAwayRestaurantDetailActivity.this.getBtnOption().setSelected(true);
					} else {
						NewTakeAwayRestaurantDetailActivity.this.getBtnOption().setSelected(false);
					}
				}

				// 将餐厅
				// SessionManager.getInstance().setRestaurantInfo(ContextUtil.getContext(),
				// takeoutInfo);
				// --
				if (dto != null) {
					if (dto.totalCommentNum == 0) {
						// 没有评论的场合
						takeaway_detail_res_plLayout.setVisibility(View.GONE);
					} else {
						takeaway_detail_res_plLayout.setVisibility(View.VISIBLE);
						takeaway_comment_infoLayout.setVisibility(View.VISIBLE);
						takeaway_detail_res_resCommentLoadingLayout.setVisibility(View.GONE);

						takeaway_detail_res_comment_num.setText("(" + dto.totalCommentNum + ")");
						//

						// 评论部位
						if (dto.commentData != null) {

							takeaway_comment_userphoto.setImageByUrl(dto.commentData.userPicUrl, true, 0, ScaleType.FIT_XY);

							takeaway_comment_tvUser.setText(dto.commentData.userName);

							takeaway_comment_tvTime.setText(dto.commentData.createTime);
							//评论内容
							if (TextUtils.isEmpty(dto.commentData.detail)) {
								takeaway_comment_detail.setText(R.string.text_layout_dish_no_comment);
							} else {
								takeaway_comment_detail.setText(dto.commentData.detail);
							}
							//餐厅回复
							takeaway_comment_rating.setMinimumHeight(1);
							takeaway_comment_rating.setRating((float) dto.commentData.overallNum);
							if(TextUtils.isEmpty(dto.commentData.replyInfo)){
								commentline.setVisibility(View.GONE);
								takeaway_comment_backlayout.setVisibility(View.GONE);
							}else {
								takeaway_comment_backlayout.setVisibility(View.VISIBLE);
								takeaway_comment_back.setText(dto.commentData.replyInfo);
							}

						}
						// }
					}
					// 去评论页面
					takeaway_comment_infoLayout.setOnClickListener(new AdapterView.OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							ViewUtils.preventViewMultipleClick(view, 1000);
							// -----
							OpenPageDataTracer.getInstance().addEvent("点评按钮");
							// -----

							Bundle bundle = new Bundle();
							bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
							bundle.putString("fromTag", "1");
							ActivityUtil.jump(NewTakeAwayRestaurantDetailActivity.this, TakeAwayRestaurantCommentActivity.class, 0, bundle);
						}

					});

				}
				//
				//餐厅资质
				if (dto != null) {
					if(dto.cerList.size()==0){
						//无餐厅资质则隐藏
						res_newtag_layout.setVisibility(View.GONE);																		
					}else {
					  res_newtag_layout.setVisibility(View.VISIBLE);					
							newtakeaway_img_list.removeAllViews();															
						NewTakeawayCerDataItem commentItem = new NewTakeawayCerDataItem(NewTakeAwayRestaurantDetailActivity.this, dto, restaurantId);
						newtakeaway_img_list.addView(commentItem);
					}

				}


			}
			//
			@Override
			protected void onError(int code, String message) {
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// DialogUtil.showToast(RestaurantDetailActivity.this,
				// message);
				super.onError(code, message);
//				finish();
//				doTest();


			}
			//
			private void doTest() {
				TakeoutInfoData2 dtto=new TakeoutInfoData2();
				String s = "{\"uuid\":\"C30L22K17960\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"kfc\",\"overallNum\":\"1.1\",\"stateName\":\"营业中\",\"stateColor\":\"#0401CE\",\"hint\":\"起送30 送达40分\",\"canOrderTag\":\"true\",\"address\":\"pudong\",\"longitude\":\"0.0\",\"latitude\":\"0.0\",\"telList\":[{\"isTelCanCall\":\"true\",\"tel\":\"13002179513\",\"cityPrefix\":\"0915\",\"branch\":\"18710800765\"}],\"openTimeInfo\":\"10:00-22:00\",\"detail\":\"我真的还想再吃五百年，臊子面，烧鸡，青椒肉丝，水煮鱼，蚂蚁上树，排骨汤，鸡汤，饭钱炒蛋，冒菜，烤串，啤酒，大盘鸡，烤鸭好多好多狂次狂次\",\"menuType\":\"家乡菜\",\"sendHint\":\"傻了吧\",\"advList\":[{\"uuid\":\"1111\",\"typeTag\":\"1\",\"title\":\"广告1\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"},{\"uuid\":\"1151\",\"typeTag\":\"1\",\"title\":\"广告5\",\"advUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"endDate\":\"20140501\",\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"appName\":\"weichat\",\"appDownloadUrl\":\"http://www.baidu.com\"}],\"totalCommentNum\":\"40\",\"commentData\":{\"uuid\":\"1212\",\"userName\":\"sunquan\",\"userPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"createTime\":\"2014-04-03 10:12\",\"overallNum\":\"4.0\",\"detail\":\"真的超级豪华豪华爱的借口按键大啊看得见啊嬶尖的空间按等级卡德加\",\"replyInfo\":\"谢谢\"},\"cerList\":[{\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},{\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},{\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"},{\"picUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\",\"bigPicUrl\":\"http://h.hiphotos.baidu.com/album/w%3D2048/sign=730e7fdf95eef01f4d141fc5d4c69825/94cad1c8a786c917b8bf9482c83d70cf3ac757c9.jpg\"}],\"shareInfo\":{\"shareSmsDetail\":\"woaikajd\",\"shareEmailDetail\":\"woaikajd\",\"shareWeiboDetail\":\"woaikajd\",\"shareWeixinIconUrl\":\"woaikajd\",\"shareWeixinDetailUrl\":\"woaikajd\",\"shareWeixinDetail\":\"woaikajd\"}}";
				// String json
				// ="{\"uuid\":\"12345\",\"favTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"name\":\"kfc\",\"overallNum\":\"1.1\",\"sendLimitPrice\":\"20\",\"sendReachMins\":\"30\",\"address\":\"pudong\",\"longitude\":\"0.00\",\"latitude\":\"0.00\",\"openTimeInfo\":\"10:00-22:00\",\"telList\":[{\"isTelCanCall\":\"true\"},{\"tel\":\"13002179513\"},{\"cityPrefix\":\"\"},{\"branch\":\"\"}],\"totalCommentNum\":\"1.1\",\"commentData\":{\"uuid\":\"123\",\"userName\":\"liu\",\"userPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"createTime\":\"2012-02-20 18:00\",\"overallNum\":\"1.1\",\"detail\":\"aaaaaaaaaaaa\",\"replyInfo\":\"aaaaaaaaaa\"},\"sendTimeList\":[{\"uuid\":\"\"},{\"parentId\":\"\"},{\"name\":\"\"},{\"num\":\"\"},{\"succTag\":\"true\"},{\"phone\":\"\"},{\"memo\":\"\"},{\"selectTag\":\"true\"},{\"isFirst\":\"false\"},{\"keywords\":\"\"},{\"firstLetters\":\"a\"},{\"firstLetter\":\"a\"}],\"stateName\":\"xxxxxx\",\"stateColor\":\"xxxxx\",\"detail\":\"xxxxxxxxxxx\",\"menuType\":\"zhongcang\",\"canOrderTag\":\"true\",\"hintForCanNotOrder\":\"no\",\"vipTag\":\"true\",\"canShowConfrimBtnTag\":\"true\",\"hintForCanNotConfirm\":\"yes/no\"}";
				dtto =new Gson().fromJson(s, TakeoutInfoData2.class);
				onSuccess(dtto);
				//dtto = JsonUtils.fromJson(s, TakeoutInfoData2.class);


			}
			//
		});		
	}

	private void setView() {
		if (takeoutInfo2 == null) {
			return;
		}

		// 餐厅电话
		detail_res_tvtel.setText("");
		if (takeoutInfo2.telList != null && takeoutInfo2.telList.size() > 0) {//
			final List<RestTelInfo> tels = takeoutInfo2.telList;

			for (int i = 0; i < tels.size(); i++) {
				final RestTelInfo tempTelInfo = tels.get(i);
				if (CheckUtil.isPhone(tempTelInfo.tel)) {
					final int ii = i;
					if (tempTelInfo.isTelCanCall) {
						ViewUtils.appendSpanToTextView(detail_res_tvtel, tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch),
								new ClickableSpan() {

							@Override
							public void onClick(View widget) {

								// -----
								OpenPageDataTracer.getInstance().addEvent("电话按钮", tempTelInfo.tel);
								// -----

								//
								ActivityUtil.callSuper57(NewTakeAwayRestaurantDetailActivity.this, tempTelInfo.cityPrefix + tempTelInfo.tel);
								//
								final String userPhone;
								// 确定拨打的用户手机号
								if (SessionManager.getInstance().isUserLogin(NewTakeAwayRestaurantDetailActivity.this)) {
									userPhone = SessionManager.getInstance().getUserInfo(NewTakeAwayRestaurantDetailActivity.this).getTel();
								} else {
									userPhone = SharedprefUtil.get(NewTakeAwayRestaurantDetailActivity.this, Settings.ANONYMOUS_TEL, "");
								}

								// 向后台传拨打数据
								new Thread(new Runnable() {
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
						detail_res_tvtel.append(tempTelInfo.tel + (tempTelInfo.branch == null || tempTelInfo.branch.trim().equals("") ? "" : "-" + tempTelInfo.branch));
					}
					// ViewUtils.setURL(tvTel, position, position+end,
					// "tel:"+tels[i],true);
				}
				// 加逗号
				if (i < tels.size() - 1) {
					detail_res_tvtel.append("　");
				}
			}

		} else {
			// 没有电话时显示“添加号码”按钮
			detail_res_add_phone.setVisibility(View.GONE);
			detail_res_tvtel.setVisibility(View.GONE);
		}
		// 餐厅名字
		if (CheckUtil.isEmpty(takeoutInfo2.name)) {
			takeaway_detail_res_tvResName.setText("");
		} else {
			takeaway_detail_res_tvResName.setText(takeoutInfo2.name);
		}

		// 餐厅图片
		if (CheckUtil.isEmpty(takeoutInfo2.picUrl)) {
			takeaway_detail_res_ivResPic.setImageResource(ImageUtil.loading);
		} else {

			takeaway_detail_res_ivResPic.setImageByUrl(takeoutInfo2.picUrl, true, 0, ScaleType.FIT_CENTER);
		}
		// 餐厅状态
		if (!CheckUtil.isEmpty(takeoutInfo2.stateName)&&!CheckUtil.isEmpty(takeoutInfo2.stateColor)) {
			takeaway_predeter_layout.setVisibility(View.VISIBLE);
			takeaway_predeter.setText(takeoutInfo2.stateName);
			try{
//				int color=Integer.parseInt(takeoutInfo2.stateColor.replace("#", ""),16)|0xFF000000;
//				takeaway_predeter.setBackgroundColor(color);
				takeaway_predeter.setBackgroundColor(Color.parseColor(takeoutInfo2.stateColor));
			}catch(Exception e){
				e.printStackTrace();
			}

		} else {
			takeaway_predeter_layout.setVisibility(View.GONE);
		}


		// 可否立即点菜状态-----------------------------------------------------------------------------------------------
		if (!takeoutInfo2.canOrderTag) {
			// 不接受立即点菜
			newtakeaway_newpredetermine_layout.setVisibility(View.GONE);
			newtakeaway_detail_top_layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 200));

		} else {
			newtakeaway_newpredetermine_layout.setVisibility(View.VISIBLE);
			Orderbutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("立即点菜按钮");
					// -----
					//跳转外卖预定界面
					Bundle bundle = new Bundle();
					bundle.putString(Settings.UUID, restaurantId);
					bundle.putString(Settings.BUNDLE_REST_NAME,takeoutInfo2.name);
					if(fromPage==11){
						NewTakeAwayRestaurantDetailActivity.this.finish();
					}else {
						ActivityUtil.jump(NewTakeAwayRestaurantDetailActivity.this,
								TakeAwayNewFoodListActivity.class, 0, bundle);
					}

				}
			});

		}		

		// 总体评价
		takeaway_res_detail_overall_num.setMinimumHeight(1);
		takeaway_res_detail_overall_num.setRating((float) takeoutInfo2.overallNum);
		//外送介绍
		if (!TextUtils.isEmpty(Html.fromHtml(takeoutInfo2.hint))) {
			newtakeaway_detail_takeoutmsg.setText(Html.fromHtml(takeoutInfo2.hint));
		}
		/************************************************/
		// 位置地图
		if (takeoutInfo2.bdLat > 0 && takeoutInfo2.bdLon > 0) {
//			GeoPoint gcj = new GeoPoint((int) (takeoutInfo2.latitude * 1E6), (int) (takeoutInfo2.longitude * 1E6));
//			GeoPoint baidu = CoordinateConvert.fromGcjToBaidu(gcj);
			String lola = takeoutInfo2.bdLon+ "," + takeoutInfo2.bdLat;
			String url = "http://api.map.baidu.com/staticimage?width=500&height=120&center=" + lola + "&markers=" + lola + "&zoom=17&markerStyles=s,A,0xff0000";
			takeaway_detail_location_map.setVisibility(View.VISIBLE);
			takeaway_detail_location_map.setImageByUrl(url, true, 0, ScaleType.FIT_XY);
			takeaway_detail_location_map.setOnClickListener(new OnClickListener() {
				// 地图查看
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// -----
					OpenPageDataTracer.getInstance().addEvent("地图按钮");
					// -----

					if (!showMap(takeoutInfo2,NewTakeAwayRestaurantDetailActivity.this)) {
						DialogUtil.showToast(NewTakeAwayRestaurantDetailActivity.this, "无法打开地图模式");
					}
				}
			});
		} else {
			takeaway_detail_location_map.setVisibility(View.GONE);

		}
		// 地址
		if (!TextUtils.isEmpty(takeoutInfo2.address)) {
			takeaway_detail_res_tvAddress.setText(takeoutInfo2.address);

		} else {
			takeaway_detail_res_tvAddress.setText("暂无地址");
		}

		// 营业时间
		if (!TextUtils.isEmpty(takeoutInfo2.openTimeInfo)) {
			detail_res_time.setText(takeoutInfo2.openTimeInfo);
		} else {
			detail_res_time.setText("");
		}
		//外送须知
		if (!TextUtils.isEmpty(Html.fromHtml(takeoutInfo2.sendHint))) {
			takeaway_detail_takeout_introduction.setText(Html.fromHtml(takeoutInfo2.sendHint));
		} else {
			takeaway_newdetail_takeout_info_layout.setVisibility(View.GONE);
			//takeaway_detail_takeout_introduction.setText("");
		}

		// 餐厅简介
		if (!TextUtils.isEmpty(takeoutInfo2.detail)) {
			takeaway_detail_res_introduction.setText(takeoutInfo2.detail);
		} else {
			takeaway_detail_res_introduction.setText("暂无");
		}
		// 风味
		if (!TextUtils.isEmpty(takeoutInfo2.menuType)) {
			takeaway_detail_res_menutype.setText(takeoutInfo2.menuType);
		} else {
			takeaway_detail_res_menutype.setText("-");
		}

		detail_res_tvAddress_layout.setOnClickListener(new OnClickListener() {
			// 地图查看
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("地图按钮");
				// -----

				if (!showMap(takeoutInfo2, NewTakeAwayRestaurantDetailActivity.this)) {
					DialogUtil.showToast(NewTakeAwayRestaurantDetailActivity.this, "无法打开地图模式");
				}
			}
		});
		takeaway_detail_res_resInfoLoadingLayout.setVisibility(View.GONE);
		takeaway_detail_res_resOtherInfoLoadingLayout.setVisibility(View.GONE);
		// 构建优惠信息
		buildPromotionList(takeoutInfo2);

	}
	/**
	 * 构建优惠信息列表
	 */
	private void buildPromotionList(TakeoutInfoData2 takeoutInfo2) {
		takeaway_discount_layout.removeAllViews();
		advList= takeoutInfo2.advList;		
		if (advList.size() == 0) {
			takeaway_no_newdiscount.setVisibility(View.VISIBLE);
			// 无优惠信息时直接隐藏整个layout，不显示任何内容
			newdiscountLayout.setVisibility(View.GONE);
			return;
		} else {
			takeaway_no_newdiscount.setVisibility(View.GONE);
		}
		for (int i = 0; i < advList.size(); i++) {
			MainPageAdvData temp =advList.get(i);				
			addPromotion(temp, i);			
		}		
	}

	// 动态添加优惠视图
	private void addPromotion(final MainPageAdvData temp, int currentPosition) {
		LinearLayout item = (LinearLayout) View.inflate(this, R.layout.restaurant_discount_new_list_item, null);
		LinearLayout blockbuttonLayout = (LinearLayout) item.findViewById(R.id.restautant_discount_block_button);
		View line = (View) item.findViewById(R.id.newtakeaway_horizontal_line);
		TextView title = (TextView) item.findViewById(R.id.newrestautant_discount_title);

		// 优惠信息			
		title.setText(temp.title);	
		//点击监听
		blockbuttonLayout.setBackgroundResource(R.drawable.button_light_color_effect);
		// blockbuttonLayout.setPadding(0, 0, 0, 0);
		line.setVisibility(currentPosition == 0 ? View.GONE : View.VISIBLE);

		blockbuttonLayout.setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v){
				ViewUtils.preventViewMultipleClick(v, 1000);
				try {
					// -----
					OpenPageDataTracer.getInstance().addEvent("优惠按钮", temp.uuid);
					// -----
					//广告类别  1:广告链接  2：本地连接  3:普通链接
					//普通链接跳转到webview页面, 本地链接使用url处理器
					if (temp.typeTag == 1) {
						// 广告链接，使用内嵌的WebView打开
						Bundle bd = new Bundle();
						bd.putString(Settings.BUNDLE_KEY_WEB_URL, temp.advUrl);
						bd.putString(Settings.BUNDLE_KEY_WEB_TITLE, temp.title);
						ActivityUtil.jump(NewTakeAwayRestaurantDetailActivity.this, SimpleWebViewActivity.class, 0, bd);

					} else if (temp.typeTag == 2) {
						// 本地链接，跳转本地界面
						URLExecutor.execute(temp.advUrl, NewTakeAwayRestaurantDetailActivity.this, 0);
					} else if (temp.typeTag == 3) {
						// 普通链接，使用系统浏览器打开
						ActivityUtil.jumbToWeb(NewTakeAwayRestaurantDetailActivity.this, temp.advUrl);
					} else if (temp.typeTag ==4) {
						Bundle bundle=new Bundle();
						bundle.putString(Settings.BUNDLE_KEY_CONTENT, temp.appDownloadUrl);
						bundle.putString(Settings.BUNDLE_UPDATE_APP_NAME, temp.appName);
						ActivityUtil.jump(NewTakeAwayRestaurantDetailActivity.this, AutoUpdateActivity.class, 0,bundle);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		takeaway_discount_layout.addView(item);

	}
	/**
	 * 显示地图模式
	 * 
	 * @return
	 */
	public static boolean showMap(TakeoutInfoData2 restInfo, Activity activity) {
		// Bundle bundle = new Bundle();
		// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
		// Settings.RESTAURANT_DETAIL_ACTIVITY);
		// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantInfo.getId());
		// ActivityUtil.jump(this, MyMapActivity.class,
		// Settings.RESTAURANT_DETAIL_ACTIVITY, bundle);
		// return true;

		for (String type : MAP_TYPE) {
			if (RestaurantDetailActivity.launchMap(type, restInfo.name, restInfo.latitude, restInfo.longitude, activity)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 回收内存
	 * 
	 */
	private void recycle() {
		// 回收内存
		// unbindDrawables(getWindow().getDecorView());
		System.gc();
	}
}
