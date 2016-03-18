package com.fg114.main.app.activity.resandfood;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.CommentAdapter;
import com.fg114.main.app.adapter.CommentDetailAdapter;
import com.fg114.main.app.view.CommentImageHorizontalScrollView;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.task.GetResCommentListTask;
import com.fg114.main.service.task.GetResCommentReplyListTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅评论详细界面 餐厅详情主页面下跳餐厅评论第2个页面
 * 
 * @author xujianjun,2012-04-09
 * 
 */
public class RestaurantCommentDetailActivity extends MainFrameActivity {

	private static final String TAG = "RestaurantCommentDetailActivity";

	// 传入参数
	private CommentData commentData; // 评论数据
	private String restId; // 餐厅id
	private String restName; // 餐厅name
	RestInfoData restInfo;

	// 控制变量
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int pageIndex = 1;

	int frompage = 1;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private View headerView;
	private ListView replyList;
	private LinearLayout replyLayout;
	private Button replyButton;
	private CommentDetailAdapter adapter;
	//
	private Set<MyImageView> imageList = new HashSet<MyImageView>();

	// 任务
	private GetResCommentReplyListTask getResCommentReplyListTask;
	private List<CommentPicData> picDatas;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅评论详情", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		commentData = (CommentData) bundle.getSerializable(Settings.BUNDLE_REST_COMMENT_DATA);
		restId = bundle.getString(Settings.BUNDLE_REST_ID);
		restName = "";
		if (bundle!=null&&bundle.getString(Settings.BUNDLE_REST_NAME)!=null) {
			restName=bundle.getString(Settings.BUNDLE_REST_NAME);
		}
		// 从缓存中取餐厅信息
		restInfo = SessionManager.getInstance().getRestaurantInfo(this, restId);
//		Log.e(TAG, "restId-->"+restId+" restInfo-->"+restInfo.name);
		if (restInfo.name != null) {
			restName = restInfo.name;
		}

		// 设置返回页
		this.setResult(frompage);

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

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅评论详情", "");
		// ----------------------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillHeaderView(headerView);
		// 取得最新回复
		adapter.setList(null, false);
		replyList.setAdapter(adapter);
		isLast = true;
		executeGetReplyListTask();
	}

	@Override
	protected void onPause() {
		super.onPause();
		resetTask();
	}

	@Override
	public void finish() {
		super.finish();
		resetTask();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText(restName);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		// 评论内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_comment_detail, null);
		replyList = (ListView) contextView.findViewById(R.id.res_comment_replyList);
		replyLayout = (LinearLayout) contextView.findViewById(R.id.res_comment_replyLayout);
		replyButton = (Button) contextView.findViewById(R.id.res_comment_replyButton);

		// 我要回复按钮，跳转到回复提交页面
		replyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("我要回复按钮");
				// -----

				Bundle bundle = new Bundle();
				bundle.putSerializable(Settings.BUNDLE_REST_COMMENT_DATA, commentData);
				bundle.putString(Settings.BUNDLE_KEY_ID, restId);
				bundle.putLong(Settings.FROM_TAG, 1);
				bundle.putString(Settings.BUNDLE_REST_NAME, restName);
				ActivityUtil.jump(RestaurantCommentDetailActivity.this, RestaurantCommentSubmitActivity.class, 0,
						bundle);

			}
		});

		// listView的header
		headerView = mInflater.inflate(R.layout.list_item_comment_detail_header, null);
		replyList.addHeaderView(headerView);
		adapter = new CommentDetailAdapter(RestaurantCommentDetailActivity.this);
		replyList.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && isRefreshFoot) {

					if (isLast == false) {
						executeGetReplyListTask();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					// 当到达列表尾部时
					isRefreshFoot = true;
				} else {
					isRefreshFoot = false;
				}
			}
		});
		this.setFunctionLayoutGone();

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	private void fillHeaderView(View headerView) {

		TextView tvUserName;
		TextView tvSendTime;
		TextView tvLikeOrDislike;
		TextView tvTasteRating;
		TextView tvEnvironmentRating;
		TextView tvSerivceRating;
		TextView tvCommentContent;
		ProgressBar pbBar;
		TextView tvMsg;
		MyImageView mUserPicture;
		LinearLayout mStarLayout;// 评分栏
		LinearLayout mPhotoLayout;// 来自随手拍
//		TableLayout mTableLayout;// 底部图片列表
//		MyImageView[] mPicture = new MyImageView[3];// 三张图片
//		TextView[] mFoodName = new TextView[3];// 三个菜品名字
//		TextView[] mFoodPrice = new TextView[3];// 三个菜品价格
//		TableRow[] mTaRows = new TableRow[3];
		CommentImageHorizontalScrollView imageHorizontalScrollView;
		TextView mNoRate;
		LinearLayout replyLayout;
		TextView replyNum;
		ImageView replyClientIcon;
		TextView replyClient;

		// ----
		tvUserName = (TextView) headerView.findViewById(R.id.list_item_comment_tvUser);
		mUserPicture = (MyImageView) headerView.findViewById(R.id.list_item_comment_userphoto);
		mStarLayout = (LinearLayout) headerView.findViewById(R.id.list_item_comment_rbStarlayout);
		tvSendTime = (TextView) headerView.findViewById(R.id.list_item_comment_tvTime);
		tvLikeOrDislike = (TextView) headerView.findViewById(R.id.list_item_comment_he_likes);
		tvTasteRating = (TextView) headerView.findViewById(R.id.list_item_comment_tvTaste);
		tvEnvironmentRating = (TextView) headerView.findViewById(R.id.list_item_comment_tvEnvironment);
		tvSerivceRating = (TextView) headerView.findViewById(R.id.list_item_comment_tvSerivce);
		tvCommentContent = (TextView) headerView.findViewById(R.id.list_item_comment_tvComment);
		tvMsg = (TextView) headerView.findViewById(R.id.list_item_comment_tvMsg);
		pbBar = (ProgressBar) headerView.findViewById(R.id.list_item_comment_pBar);
//		mTableLayout = (TableLayout) headerView.findViewById(R.id.list_item_comment_tablelayout);
//		mPicture[0] = (MyImageView) headerView.findViewById(R.id.list_item_comment_firstpitcure);
//		mPicture[1] = (MyImageView) headerView.findViewById(R.id.list_item_comment_secondpitcure);
//		mPicture[2] = (MyImageView) headerView.findViewById(R.id.list_item_comment_thirdpitcure);
//		mFoodName[0] = (TextView) headerView.findViewById(R.id.list_item_comment_firstfoodname);
//		mFoodName[1] = (TextView) headerView.findViewById(R.id.list_item_comment_secondfoodname);
//		mFoodName[2] = (TextView) headerView.findViewById(R.id.list_item_comment_thirdfoodname);
//		mFoodPrice[0] = (TextView) headerView.findViewById(R.id.list_item_comment_firstfoodprice);
//		mFoodPrice[1] = (TextView) headerView.findViewById(R.id.list_item_comment_secondfoodprice);
//		mFoodPrice[2] = (TextView) headerView.findViewById(R.id.list_item_comment_thirdfoodprice);
//		mTaRows[0] = (TableRow) headerView.findViewById(R.id.list_item_comment_first_row);
//		mTaRows[1] = (TableRow) headerView.findViewById(R.id.list_item_comment_second_row);
//		mTaRows[2] = (TableRow) headerView.findViewById(R.id.list_item_comment_third_row);
		imageHorizontalScrollView = (CommentImageHorizontalScrollView) headerView.findViewById(R.id.list_item_comment_imageScrollView);
		mPhotoLayout = (LinearLayout) headerView.findViewById(R.id.list_item_comment_photolayout);
		mNoRate = (TextView) headerView.findViewById(R.id.list_item_comment_tvNoRate);
		replyLayout = (LinearLayout) headerView.findViewById(R.id.list_item_comment_replyLayout);
		replyNum = (TextView) headerView.findViewById(R.id.list_item_comment_replyNum);
		replyClientIcon = (ImageView) headerView.findViewById(R.id.list_item_comment_replyClientIcon);
		replyClient = (TextView) headerView.findViewById(R.id.list_item_comment_replyClient);
		
		//imageHorizontalScrollView.setVisibility(View.GONE);
		// // --填充数据
		if (commentData != null) {

			// 设置评论人
			if ("".equals(commentData.userName.trim())) {
				tvUserName.setText(R.string.text_null_hanzi);
			} else {
				tvUserName.setText(commentData.userName.trim());
			}
			// 添加用户头像
			imageList.add(mUserPicture);
			mUserPicture.setImageByUrl(commentData.userSmallPicUrl, true, -1, ScaleType.CENTER_CROP);
			// 设置喜欢，不喜欢
			if (commentData.likeTag) {
				tvLikeOrDislike.setText("他喜欢");
				tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(getResources()
						.getDrawable(R.drawable.attention_recommen_restaurant_2), null, null, null);
			} else {
				tvLikeOrDislike.setText("他不喜欢");
				tvLikeOrDislike.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.attention_recommen_restaurant_1), null, null, null);
			}
			// 添加底部菜品图片
			imageHorizontalScrollView.clearImageData();
			picDatas = commentData.picList;
			if (picDatas.size() > 0 && imageHorizontalScrollView.getImageCount()==0) {
				imageHorizontalScrollView.setVisibility(View.VISIBLE);
				if (picDatas.size() > 8) {
					picDatas = picDatas.subList(0, 8);
				}
				imageHorizontalScrollView.setImageData(picDatas);
//				mTableLayout.setVisibility(View.VISIBLE);
//
//				if (picDatas.size() > 3) {
//					picDatas = picDatas.subList(0, 3);
//				}
//				boolean isHasFoodName = false;// 是否有菜名
//				boolean isHasFoodPrice = false;// 是否有菜价格
//				for (int i = 0; i < picDatas.size(); i++) {
//					// 添加底部图片
//					imageList.add(mPicture[i]);
//					mPicture[i].setVisibility(View.VISIBLE);
//					mPicture[i].setImageByUrl(picDatas.get(i).smallPicUrl, true, 0, ScaleType.CENTER_CROP);
//
//					View.OnClickListener listener = new callPicView(headerView, picDatas.get(i).picUrl);
//					mPicture[i].setOnClickListener(listener);
//
//					if (!CheckUtil.isEmpty(picDatas.get(i).title)) {
//						mFoodName[i].setVisibility(View.VISIBLE);
//						mFoodName[i].setText(picDatas.get(i).title);
//						if (!isHasFoodName) {
//							isHasFoodName = true;
//						}
//					} else {
//						mFoodName[i].setVisibility(View.GONE);
//					}
//
//					if (!CheckUtil.isEmpty(picDatas.get(i).priceInfo)) {
//						mFoodPrice[i].setVisibility(View.VISIBLE);
//						mFoodPrice[i].setText(picDatas.get(i).priceInfo);
//						if (!isHasFoodPrice) {
//							isHasFoodPrice = true;
//						}
//					} else {
//						mFoodPrice[i].setVisibility(View.GONE);
//					}
//				}
//
//				if (isHasFoodName) {
//					mTaRows[1].setVisibility(View.VISIBLE);
//				} else {
//					mTaRows[1].setVisibility(View.GONE);
//				}
//
//				if (isHasFoodPrice) {
//					mTaRows[2].setVisibility(View.VISIBLE);
//				} else {
//					mTaRows[2].setVisibility(View.GONE);
//				}

			} else {
				//mTableLayout.setVisibility(View.GONE);
				imageHorizontalScrollView.setVisibility(View.GONE);
			}

			// 设置评论时间
			if (commentData.createTime > 0) {
				tvSendTime.setText(ConvertUtil.convertLongToDateString(commentData.createTime,
						ConvertUtil.DATE_FORMAT_YYYYMMDD_HHMI));
			}

			// 是否有评分
			if (commentData.gradeTag) {
				mStarLayout.setVisibility(View.VISIBLE);
				mPhotoLayout.setVisibility(View.GONE);
				// 设置餐馆星级
				float rate = (float) commentData.overallNum;

				// 设置口味
				tvTasteRating.setText(String.valueOf((int) commentData.tasteNum));
				// 设置环境
				tvEnvironmentRating.setText(String.valueOf((int) commentData.envNum));
				// 设置服务
				tvSerivceRating.setText(String.valueOf((int) commentData.serviceNum));
			} else {
				mStarLayout.setVisibility(View.GONE);
				mPhotoLayout.setVisibility(View.VISIBLE);
				mNoRate.setText(commentData.noGradeIntro);
			}

			// 设置评论内容
			if (TextUtils.isEmpty(commentData.detail)) {
				tvCommentContent.setText(R.string.text_layout_dish_no_comment);
			} else {
				tvCommentContent.setText(commentData.detail);

				/*
				 * 处理@关键字，几种情况需要处理特殊样式，关键字(包括@)长度须不大于15字 1： @xxx xxx @与空格之间的文字
				 * 2：xxx@xxx @到文字内容结束之间的文字 3：@xxx@xxx 两个@相邻
				 */
				processKeywords(tvCommentContent);
			}

			// ---
			setReplyNumString(commentData.replyNum);
			replyClient.setText(commentData.clientName);
			if (CheckUtil.isEmpty(commentData.clientName)) {
				replyClientIcon.setVisibility(View.INVISIBLE);
				replyClient.setVisibility(View.INVISIBLE);
			} else {
				replyClientIcon.setVisibility(View.VISIBLE);
				replyClient.setVisibility(View.VISIBLE);
			}
		}

	}

	private void setReplyNumString(int replyNum) {
		TextView tvReplyNum = (TextView) headerView.findViewById(R.id.list_item_comment_replyNum);
		tvReplyNum.setText("回复 (" + replyNum + ")");
	}

	/**
	 * 获得餐厅详细
	 */
	private void executeGetReplyListTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			this.isTaskSafe = false;
		} else {
			return;
		}

		if (DEBUG)
//			Log.e(TAG, "now page no is：" + pageIndex);

		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----

		// 创建任务
		getResCommentReplyListTask = new GetResCommentReplyListTask(null, this, commentData.uuid, pageIndex);

		// 执行任务
		getResCommentReplyListTask.execute(new Runnable() {

			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				CommentReplyListDTO dto = getResCommentReplyListTask.dto;
				if (dto != null) {
//					Log.e(TAG, "dto");
					pageIndex = dto.pgInfo.nextStartIndex;
					isLast = dto.pgInfo.lastTag;

					adapter.addList(dto.getList(), isLast);
					setReplyNumString(adapter.getCount());
				}

				// 设置线程安全
				isTaskSafe = true;
			}
		}, new Runnable() {
			@Override
			public void run() {
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				// 设置线程安全
				isTaskSafe = true;
//				Log.e(TAG, "xxxx");
				isLast = true;
				adapter.addList(new ArrayList<CommentReplyData>(), isLast);
			}
		});
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask() {
		recycle();
		if (getResCommentReplyListTask != null) {
			getResCommentReplyListTask.cancel(true);
			adapter.setList(null, false);
			replyList.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}

	/**
	 * 回收内存
	 */
	private void recycle() {
		// 回收内存
		if (adapter != null) {
			Iterator<MyImageView> iterator = adapter.viewList.iterator();
			while (iterator.hasNext()) {
				iterator.next().recycle(true);
			}
			adapter.viewList.clear();
		}

		if (imageList != null && imageList.size() > 0) {
			Iterator<MyImageView> iterator = imageList.iterator();
			while (iterator.hasNext()) {
				iterator.next().recycle(true);
			}
			imageList.clear();
		}
		System.gc();
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
			DialogUtil.createImageViewPanel((Activity) RestaurantCommentDetailActivity.this, parentView, url);
		}

	}

	private void processKeywords(TextView view) {
		try {
			String regEx = "@([^(@\\s)]+)";
			String text = view.getText().toString();
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(text);
			while (m.find()) {
				MatchResult mr = m.toMatchResult();
				if (mr.groupCount() > 0) {
					String matchStr = mr.group(0);
					if (matchStr.length() <= 15) {
						int index = text.indexOf(matchStr);
						if (index > -1) {
							ViewUtils.setBold(view, index, index + matchStr.length());
						}
					}
				}
			}
		} catch (Exception e) {
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
}
