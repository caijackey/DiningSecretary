package com.fg114.main.app.activity.resandfood;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.FoodPicCommentAdapter;
import com.fg114.main.app.adapter.FoodPicGalleryAdapter;
import com.fg114.main.service.dto.RestFoodPicDataDTO;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 餐厅菜品图片详情
 * 
 * @author xujianjun,2012-10-17
 * 
 */
public class RestaurantImageDetailOfFoodActivity extends MainFrameActivity {


	private static final String TAG = "RestaurantImageDetailOfFoodActivity";

	// 传入参数
	private int fromPage;
	private String foodId;
	private String foodName;
	private String restId;
	private String restName;

	// 画面变量
	private boolean isRefreshComment = false; //指示是否是刷新评论，如果是，listview要滚动到第一条评论
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int startIndex = 1;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ViewGroup header;
	private ViewGroup footer;

	// header区
	private TextView tvfoodName;
	private Gallery gallery;
	private ImageView foodPriceImage;
	private TextView foodPrice;
	private TextView foodPriceUnit;
	private ImageView galleryRightButton;
	private ImageView galleryLeftButton;
	private Button gradeLikeButton;
	private Button gradeGeneralButton;
	private Button gradeDislikeButton;
	private TextView animationText;
	//上传按钮
	private ViewGroup uploadButtonNoPicLayout;
	private Button uploadButtonNoPic;
	private Button uploadButton;
	// footer区
	private EditText commentContent;
	private Button commentSubmitButton;

	private ListView list;
	private FoodPicCommentAdapter commentAdapter; // 菜品评论列表适配器
	private FoodPicGalleryAdapter galleryAdapter; // 菜品图片适配器
	private Animation gradeAnimation;// =AnimationUtils.loadAnimation(this,
										// R.anim.restaurant_picture_detail_grade_rise_from_bottom);
	// 任务
//	private GetResFoodPicDataDTOTask getResFoodPicDataDTOTask;
//	private DishCommentTask postCommentTask;
//	private AddResFoodLikeTypeTask addResFoodLikeTypeTask;
	private RestFoodPicDataDTO data;

	// 最多点击评分按钮的次数
	private int maxGradeNum = 1;
	private int gradeNum = 0;

	// 提交评论或者评分的listener
	private OnClickListener postCommentListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			
			ViewUtils.preventViewMultipleClick(v, 2000);
			switch (v.getId()) {
				case R.id.food_image_grade_like_button: // 点击喜欢
					executeAddResFoodLikeTypeTask(1);
					break;
				case R.id.food_image_grade_general_button: // 点击一般
					executeAddResFoodLikeTypeTask(2);
					break;
				case R.id.food_image_grade_dislike_button: // 点击不喜欢
					executeAddResFoodLikeTypeTask(3);
					break;
				case R.id.food_image_comment_submit_button: // 点击提交评论
					executePostCommentTask();
					break;
			}
		}
	};





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅菜品详情", "");
		// ----------------------------
		
		Settings.NEED_REFRESH_FOOD_PICTURE_DETAIL=true; //需要刷新
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		foodId = bundle.getString(Settings.BUNDLE_FOOD_ID);
		foodName = bundle.getString(Settings.BUNDLE_FOOD_NAME);

		if (CheckUtil.isEmpty(foodId) || CheckUtil.isEmpty(foodName)) {
			finish();
			return;
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
	
	@Override
	protected void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("餐厅菜品详情", "");
		// ----------------------------
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(Settings.NEED_REFRESH_FOOD_PICTURE_DETAIL){
			refresh();
			Settings.NEED_REFRESH_FOOD_PICTURE_DETAIL=false; 
		}
		
		// 为了上传图片结束后，跳转页面服务------------------nieyinyin add 
		Settings.uploadPictureOrignalActivityClazz = RestaurantImageDetailOfFoodActivity.class;
		
	}
	
	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("菜品图片");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("修改报错");

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.restaurant_image_detail_of_food, null);
		header = (ViewGroup) mInflater.inflate(R.layout.restaurant_image_detail_of_food_header_view, null);
		footer = (ViewGroup) contextView.findViewById(R.id.restaurant_image_detail_of_food_footer_view);
		//
		list = (ListView) contextView.findViewById(R.id.food_image_list_view);
		// header区
		tvfoodName = (TextView) header.findViewById(R.id.food_image_food_name);
		gallery = (Gallery) header.findViewById(R.id.food_image_gallery);
		foodPriceImage = (ImageView) header.findViewById(R.id.food_image_gallery_food_price_image);
		foodPrice = (TextView) header.findViewById(R.id.food_image_gallery_food_price);
		foodPriceUnit = (TextView) header.findViewById(R.id.food_image_gallery_food_price_unit);
		galleryRightButton = (ImageView) header.findViewById(R.id.food_image_gallery_right_button);
		galleryLeftButton = (ImageView) header.findViewById(R.id.food_image_gallery_left_button);
		gradeLikeButton = (Button) header.findViewById(R.id.food_image_grade_like_button);
		gradeGeneralButton = (Button) header.findViewById(R.id.food_image_grade_general_button);
		gradeDislikeButton = (Button) header.findViewById(R.id.food_image_grade_dislike_button);
		animationText = (TextView) header.findViewById(R.id.food_image_grade_animation_text);
		//无图时的上传按钮
		uploadButtonNoPicLayout = (ViewGroup) header.findViewById(R.id.food_image_upload_button_when_no_pic_layout);
		uploadButtonNoPic = (Button) header.findViewById(R.id.food_image_upload_button_when_no_pic);
		//有图时的上传按钮
		uploadButton = (Button) header.findViewById(R.id.food_image_upload_button);
		
		// footer区
		commentContent = (EditText) footer.findViewById(R.id.food_image_comment_content);
		commentSubmitButton = (Button) footer.findViewById(R.id.food_image_comment_submit_button);

		// 初始化，清空
		tvfoodName.setText(foodName);
		foodPrice.setText("");
		foodPriceUnit.setText("");
		gradeLikeButton.setText("");
		gradeGeneralButton.setText("");
		gradeDislikeButton.setText("");
		animationText.setVisibility(View.GONE);
		uploadButtonNoPicLayout.setVisibility(View.GONE);
		uploadButton.setVisibility(View.GONE);

		//
		list.addHeaderView(header);

		// 评论适配器
		commentAdapter = new FoodPicCommentAdapter(RestaurantImageDetailOfFoodActivity.this);
		commentAdapter.setList(null, false);
		list.setAdapter(commentAdapter);

		// 图片适配器
		galleryAdapter = new FoodPicGalleryAdapter(RestaurantImageDetailOfFoodActivity.this);
		galleryAdapter.setList(null, false);
		gallery.setAdapter(galleryAdapter);
		//
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				syncGalleryArrow();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				syncGalleryArrow();
			}
		});
		
		gallery.setOnTouchListener(new OnTouchListener() {
			//此监听器实现一次只滑一个图像,用下面的开关来控制这个特性
			boolean onlyFlingOneFrame=false;
			//---
			MotionEvent lastEvent=null;
			float vx;
			GestureDetector my=new GestureDetector(new OnGestureListener() {
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
					
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
					
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
					vx=velocityX;
					return true;
				}
				
				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}
			});

			@Override
			public boolean onTouch(View kv, MotionEvent event) {
				if(!onlyFlingOneFrame){
					return false;
				}
				boolean returnValue=false;
				if(my.onTouchEvent(event)){
					if(lastEvent!=null){
						if(vx>0){
							moveGalleryRight(gallery);
						}else if(vx<0){
							moveGalleryLeft(gallery);
						}
					}
					returnValue=true;
				}
				lastEvent=event;
				return returnValue;
			}
		});
		galleryRightButton.setOnClickListener(new OnClickListener() {

			@Override
			
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				int i = gallery.getSelectedItemPosition();
				int count = gallery.getCount();
				i++;
				if (i < 0 || i > count - 1) {
					return;
				}

				moveGalleryLeft(gallery);
			}
		});
		galleryLeftButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				int count = gallery.getCount();
				int i = gallery.getSelectedItemPosition();
				i--;
				if (i < 0 || i > count - 1) {
					return;
				}
				moveGalleryRight(gallery);
			}
		});

		// 倾斜价格显示文字
		rotatePriceView();

		// 菜品报错
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// -----
				OpenPageDataTracer.getInstance().addEvent("修改报错按钮");
				// -----
				
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				bundle.putInt(Settings.BUNDLE_KEY_ERROR_REPORT_TYPE, Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_FOOD);
				bundle.putString(Settings.UUID, foodId);
				// 这里还要添加可能的错误列表数据，以供选择列表对话框显示
				try {
					DialogUtil.showErrorReportTypeSelectionDialog(RestaurantImageDetailOfFoodActivity.this, bundle);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// ------------------------------
		list.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
						&& isRefreshFoot) {

					if (isLast == false) {
						executeGetResFoodDataTask();

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
		gradeAnimation = AnimationUtils.loadAnimation(this, R.anim.restaurant_picture_detail_grade_rise_from_bottom);
	}
	
	private void syncGalleryArrow() {
		int i = gallery.getSelectedItemPosition();
		int count = gallery.getCount();
		int previous = i - 1;
		int next = i + 1;
		// Log.d("++++","i="+i);
		// 没有下一个
		if (next < 0 || next > count - 1) {
			galleryRightButton.setClickable(false);
			galleryRightButton.setEnabled(false);
		} else {
			galleryRightButton.setClickable(true);
			galleryRightButton.setEnabled(true);
		}
		// 没有上一个
		if (previous < 0 || previous > count - 1) {
			galleryLeftButton.setClickable(false);
			galleryLeftButton.setEnabled(false);
		} else {
			galleryLeftButton.setClickable(true);
			galleryLeftButton.setEnabled(true);

		}
	}
	private void rotatePriceView() {
		// 倾斜文字
		ViewUtils.rotateView(foodPrice, 6.4f, 0);
		ViewUtils.rotateView(foodPriceUnit, 6.4f, 0);

	}

	private void moveGalleryRight(View v) {
		long start = SystemClock.uptimeMillis();
		MotionEvent me1 = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN, 1, 1, 0);
		MotionEvent me2 = MotionEvent.obtain(start, start, MotionEvent.ACTION_UP, 1, 1, 0);
		gallery.dispatchTouchEvent(me1);
		gallery.dispatchTouchEvent(me2);

	}

	private void moveGalleryLeft(View v) {
		long start = SystemClock.uptimeMillis();
		MotionEvent me1 = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN, v.getWidth() - 1, 1, 0);
		MotionEvent me2 = MotionEvent.obtain(start, start, MotionEvent.ACTION_UP, v.getWidth() - 1, 1, 0);
		gallery.dispatchTouchEvent(me1);
		gallery.dispatchTouchEvent(me2);
	}

	private void executeGetResFoodDataTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
//				pageNo = pageNo + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}

		// 创建任务
//		getResFoodPicDataDTOTask = new GetResFoodPicDataDTOTask(null, this, foodId, pageNo);
//
//		// 执行任务
//		getResFoodPicDataDTOTask.execute(new Runnable() {
//
//			@Override
//			public void run() {
//
//				ResFoodPicDataDTO dto = getResFoodPicDataDTOTask.dto;
//				if (dto != null) {
//					isLast = dto.pgInfo.lastTag;
//					commentAdapter.addList(dto.getCommentList(), isLast);
//					// 如果是第一页，加载顶部图片列表，只加载一次
//					if (pageNo == 1) {
//						galleryAdapter.addList(dto.getPicList(), true);
//						// --初始化评分和提交按钮事件
//						gradeLikeButton.setOnClickListener(postCommentListener);
//						gradeGeneralButton.setOnClickListener(postCommentListener);
//						gradeDislikeButton.setOnClickListener(postCommentListener);
//						commentSubmitButton.setOnClickListener(postCommentListener);
//						// 设置价格
//						setPriceAndUnit(dto.getPrice(), dto.getUnit());
//						
//						
//						//有图片时显示下部的上传按钮，无图片时显示gallery位置的上传按钮
//						if(dto.getPicList()==null||dto.getPicList().size()==0){
//							uploadButtonNoPicLayout.setVisibility(View.VISIBLE);
//							uploadButton.setVisibility(View.GONE);
//						}
//						else{
//							uploadButtonNoPicLayout.setVisibility(View.GONE);
//							uploadButton.setVisibility(View.VISIBLE);
//						}
//						uploadButtonNoPic.setOnClickListener(uploadListener);
//						uploadButton.setOnClickListener(uploadListener);
//						//餐厅信息
//						restId=dto.getRestId();
//						restName=dto.getRestName();
//						
//					}
//					// 设置评分
//					setGradeButtonText(gradeLikeButton, "好", dto.getGoodNum());
//					setGradeButtonText(gradeGeneralButton, "一般", dto.getNormalNum());
//					setGradeButtonText(gradeDislikeButton, "差", dto.getBadNum());
//					syncGalleryArrow();
//					//如果是刷新评论，滚动到能显示第一条
//					if(isRefreshComment){
//						isRefreshComment=false;
//						ViewUtils.setSelection(list, 1);
//					}
//				}
//				// 设置线程安全
//				isTaskSafe = true;
//			}
//
//		}, new Runnable() {
//			@Override
//			public void run() {
//				// 设置线程安全
//				isTaskSafe = true;
//			}
//		});
		
		
		// -----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
		
		ServiceRequest request = new ServiceRequest(API.getRestFoodPicData);
		request.addData("foodId", foodId);
		request.addData("pageSize", 20);
		request.addData("startIndex", startIndex);
		
		CommonTask.request(request, new CommonTask.TaskListener<RestFoodPicDataDTO>(){
			protected void onSuccess(RestFoodPicDataDTO dto) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				
				if(dto != null){
					data = dto;
					isLast = dto.pgInfo.lastTag;
					startIndex = dto.pgInfo.nextStartIndex;
					commentAdapter.addList(dto.getCommentList(), isLast);
					// 如果是第一页，加载顶部图片列表，只加载一次
					if (startIndex == 1) {
						galleryAdapter.addList(dto.getPicList(), true);
						// --初始化评分和提交按钮事件
						gradeLikeButton.setOnClickListener(postCommentListener);
						gradeGeneralButton.setOnClickListener(postCommentListener);
						gradeDislikeButton.setOnClickListener(postCommentListener);
						commentSubmitButton.setOnClickListener(postCommentListener);
						// 设置价格
						setPriceAndUnit(dto.getPrice(), dto.getUnit());
						
						
						//有图片时显示下部的上传按钮，无图片时显示gallery位置的上传按钮
						if(dto.getPicList()==null||dto.getPicList().size()==0){
							uploadButtonNoPicLayout.setVisibility(View.VISIBLE);
							uploadButton.setVisibility(View.GONE);
						}
						else{
							uploadButtonNoPicLayout.setVisibility(View.GONE);
							uploadButton.setVisibility(View.VISIBLE);
						}
						uploadButtonNoPic.setOnClickListener(uploadListener);
						uploadButton.setOnClickListener(uploadListener);
						//餐厅信息
						restId=dto.getRestId();
						restName=dto.getRestName();
						
					}
					// 设置评分
					setGradeButtonText(gradeLikeButton, "好", dto.getGoodNum());
					setGradeButtonText(gradeGeneralButton, "一般", dto.getNormalNum());
					setGradeButtonText(gradeDislikeButton, "差", dto.getBadNum());
					syncGalleryArrow();
					//如果是刷新评论，滚动到能显示第一条
					if(isRefreshComment){
						isRefreshComment=false;
						ViewUtils.setSelection(list, 1);
					}
				
				}
			};
			
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				// 设置线程安全
				isTaskSafe = true;
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
			}
		});
	}
	
	private OnClickListener uploadListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			// -----
			OpenPageDataTracer.getInstance().addEvent("上传图片按钮");
			// -----
			
			ViewUtils.preventViewMultipleClick(v, 1000);
			takePicture(v);			
		}
	};
	//拍照上传
	private void takePicture(View v){
		if (ActivityUtil.checkMysoftStage(RestaurantImageDetailOfFoodActivity.this)) {
		ButtonPanelUtil pan = new ButtonPanelUtil();
			pan.showUploadPanel(v, RestaurantImageDetailOfFoodActivity.this, null);
			pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener()
			{
	
				@Override
				public void onGetUri(Uri uri)
				{
					takePhotoUri = uri;
				}
			});
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if ((requestCode == Settings.CAMERAIMAGE || requestCode == Settings.LOCALIMAGE)) {
			String path = null;
			if (data != null && data.getData() != null) {
				path = parseImgPath(data.getData());
			} else if (takePhotoUri != null) {
				path = parseImgPath(takePhotoUri);
			}

			try {
				if (CheckUtil.isEmpty(path)) {
					DialogUtil.showToast(this, "没有选择任何图片");
					return;
				}
				// 如果未拍照或选择了空图片
				if (new File(path).length() == 0) {
					getContentResolver().delete(takePhotoUri, null, null);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (DEBUG)
				Log.d(TAG, "uri" + path);

			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_FOOD);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID,restId);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME,restName);
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_ID,foodId);
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME,foodName);

			Settings.uploadPictureUri = path;
			Settings.uploadPictureOrignalActivityId = 0;
			ActivityUtil.jump(RestaurantImageDetailOfFoodActivity.this, RestaurantUploadActivity.class, 0, bundle);

			takePhotoUri = null;
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	/**
	 * 获得路径
	 * 
	 * @param data
	 * @return
	 */
	private String parseImgPath(Uri uri)
	{
		String path = null;
		if (uri != null) {
			ContentResolver localContentResolver = getContentResolver();
			// 查询图片真实路径
			Cursor cursor = localContentResolver.query(uri, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
					path = cursor.getString(index);
					cursor.close();
				}
			}
		}
		return path;
	}
	// 设置价格和单位，并且按字符数量调整字体宽度以不超出价格牌的宽度
	private void setPriceAndUnit(String price, String unit) {
		if (price == null || unit == null) {
			return;
		}
		float scalePrice = 4.5f / (price.length() == 0 ? 4.5f : price.length());
		float scaleUnit = 2.8f / (unit.length() == 0 ? 2.8f : unit.length());

		foodPrice.setTextScaleX(scalePrice>1?1:scalePrice);
		foodPriceUnit.setTextScaleX(scaleUnit>1?1:scaleUnit);

		foodPrice.setText(price);
		foodPriceUnit.setText(unit);

	}

	/**
	 * 提交评论
	 * @param score
	 */
	private void executePostCommentTask() {

		if (!checkInput()) {
			return;
		}

		
		// -----
		OpenPageDataTracer.getInstance().addEvent("发表评论按钮");
		// -----
		
		
		String token = "";

		// --构造分享平台字符串
		String shareTo = "";
		// --
		if (SessionManager.getInstance().isUserLogin(this)) {
			token = SessionManager.getInstance().getUserInfo(RestaurantImageDetailOfFoodActivity.this).getToken();
		}
//		// --
//		postCommentTask = new DishCommentTask(getString(R.string.text_info_uploading), this, foodId, token, 0, commentContent.getText().toString().trim(),
//				shareTo);
//		postCommentTask.setCanCancel(false);
//
//		// 执行任务
//		postCommentTask.execute(new Runnable() {
//			@Override
//			public void run() {
//				// 提交成功
//
//				DialogUtil.showToast(RestaurantImageDetailOfFoodActivity.this, "提交成功！");
//				commentContent.setText("");
//				refreshCommentList();
//				ViewUtils.hideSoftInput(RestaurantImageDetailOfFoodActivity.this, commentContent);
//
//			}
//
//
//		});
		
		// 提交数据
		ServiceRequest request = new ServiceRequest(API.addRestFoodComment);
		request.addData("foodId", foodId);
		request.addData("likeTypeTag", 0);
		request.addData("content", commentContent.getText().toString().trim());
		request.addData("shareTo", shareTo);
		CommonTask.request(request, new CommonTask.TaskListener<SimpleData>(){
			@Override
			protected void onSuccess(SimpleData dto) {
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("发表评论按钮");
				// -----
				
				DialogUtil.showToast(RestaurantImageDetailOfFoodActivity.this, "提交成功！");
				commentContent.setText("");
				refreshCommentList();
				ViewUtils.hideSoftInput(RestaurantImageDetailOfFoodActivity.this, commentContent);
			}
			
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("发表评论按钮");
				// -----
			}
		});
	}
	//刷新评论列表
	private void refreshCommentList() {
		isTaskSafe = true;
		isLast = true;
		isRefreshFoot = false;
		startIndex = 1;
		commentAdapter.setList(null, false);
		galleryAdapter.setList(null, false);
		executeGetResFoodDataTask();
		isRefreshComment=true;
	}
	//刷新页面
	private void refresh() {
		isTaskSafe = true;
		isLast = true;
		isRefreshFoot = false;
		startIndex = 1;
		commentAdapter.setList(null, false);
		galleryAdapter.setList(null, false);
		executeGetResFoodDataTask();
		isRefreshComment=false;
	}
	/**
	 * 提交评分
	 * @param score
	 */
	private void executeAddResFoodLikeTypeTask(final int score) {
		
		if (score <= 0 && score >3) {
			return;
		}
		// 是否超过评分次数
		if (gradeNum >= maxGradeNum) {
			return;
		}
		
		// -----
		OpenPageDataTracer.getInstance().addEvent("打分按钮");
		// -----
		
		
		String token = "";
		
		//不管提交是否成功都先显示动画并加1
		scoreComplete(score);
		// --------------------------------
		
		if (SessionManager.getInstance().isUserLogin(this)) {
			token = SessionManager.getInstance().getUserInfo(RestaurantImageDetailOfFoodActivity.this).getToken();
		}
//		// --
//		addResFoodLikeTypeTask = new AddResFoodLikeTypeTask(null, this, foodId, token, score);
//		addResFoodLikeTypeTask.setCanCancel(false);
//		
//		// 执行任务
//		addResFoodLikeTypeTask.execute(new Runnable() {
//			@Override
//			public void run() {
//				// 提交成功
//			}
//		});
		// 请求数据
		ServiceRequest request = new ServiceRequest(API.addRestFoodLikeType);
		request.addData("foodId", foodId);
		request.addData("token", token);
		request.addData("likeTypeTag", score);
		CommonTask.request(request, new CommonTask.TaskListener<SimpleData>(){
			@Override
			protected void onSuccess(SimpleData dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("打分按钮");
				// -----
			}
			
			@Override
			protected void onError(int code, String message) {
				super.onError(code, message);
				
				// -----
				OpenPageDataTracer.getInstance().endEvent("打分按钮");
				// -----
			}
		});
	}

	// 评分成功后，设置按钮为不可点击
	private void scoreComplete(int score) {

		if (data == null) {
			return;
		}
		// 设置新分数
		switch (score) {
			case 1: // 喜欢
				data.setGoodNum(data.getGoodNum() + 1);
				setGradeButtonText(gradeLikeButton, "好", data.getGoodNum());
				showScoreCompleteAnimation(gradeLikeButton, 0xFF972249);
				break;
			case 2: // 一般
				data.setNormalNum(data.getNormalNum() + 1);
				setGradeButtonText(gradeGeneralButton, "一般", data.getNormalNum());
				showScoreCompleteAnimation(gradeGeneralButton, 0xFFB09548);
				break;
			case 3: // 不喜欢
				data.setBadNum(data.getBadNum() + 1);
				setGradeButtonText(gradeDislikeButton, "差", data.getBadNum());
				showScoreCompleteAnimation(gradeDislikeButton, 0xFF3F6F92);
				break;

		}
		gradeNum++;
	}

	// 评分完成后显示+1的动画效果
	private void showScoreCompleteAnimation(final Button button, final int colorValue) {
		ViewUtils.preventViewMultipleClick(button, 2500);
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) animationText.getLayoutParams();
		lp.setMargins(button.getLeft() + button.getWidth() / 2, lp.topMargin, lp.rightMargin, lp.bottomMargin);
		animationText.setTextColor(colorValue);
		animationText.setLayoutParams(lp);
		animationText.setVisibility(View.VISIBLE);				

		gradeAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {				

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				animationText.setVisibility(View.GONE);
			}
		});
		animationText.startAnimation(gradeAnimation);
		button.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				resetGradeButton();
			}
		}, 2000);
	}

	private boolean checkInput() {

		if (CheckUtil.isEmpty(commentContent.getText().toString().trim())) {
			DialogUtil.showToast(this, "请输入评论内容");
			return false;
		}

		if (commentContent.getText().toString().trim().length() > 200) {
			DialogUtil.showToast(this, "您点评内容过长，点评不能超过200字");
			return false;
		}
		return true;
	}

	void setGradeButtonText(Button btn, String text, int grade) {
		btn.setText(Html.fromHtml(text + "<font color=\"#999999\">(" + grade + ")</font>"));
	}

	private void resetGradeButton() {
		try {
			gradeLikeButton.setPressed(false);
			gradeGeneralButton.setPressed(false);
			gradeDislikeButton.setPressed(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
