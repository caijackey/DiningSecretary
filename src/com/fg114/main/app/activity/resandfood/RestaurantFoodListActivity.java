package com.fg114.main.app.activity.resandfood;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.*;
import android.os.Handler.Callback;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.*;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;

import com.fg114.main.app.view.*;

import com.fg114.main.R;
import com.fg114.main.app.*;
import com.fg114.main.app.activity.CitySearchActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;

/**
 * 餐厅菜品列表页
 * 
 * @author xujianjun,2012-08-01
 */
public class RestaurantFoodListActivity extends MainFrameActivity {
	
	//回传最新评论信息的全局变量（从菜评论列表、评论发表页回来的）
	public static ResFoodCommentData recentCommentData;
	//
	private static final String TAG = "RestaurantFoodListActivity";
	// 标识是有奖传图或者是添加菜品
	public int mFlag;
	public static final int ADAPTERFLAG = 1;
	public static final int ACTIVITYFLAG = 0;
	
	// 传入参数
	private int fromPage = 0;

	private ImageView leftArrow;
	private ImageView rightArrow;
	private RadioGroup radioGroup;
	private GetRestaurantFoodListTask task;
	private HorizontalScrollView hScrollView;
	private ListView listview;
	private LinearLayout typeLayout;
	// 餐厅信息
	private LinearLayout resInfoLayout;
	private LinearLayout resInfoLoadingLayout;
	private TextView tvResName;
	private TextView tvOverallNum;
	private TextView tvTasteNum;
	private TextView tvEnvNum;
	private TextView tvServiceNum;
	private RatingBar rbOverallNum;
	private Button btnViewInfo;

	// 分页控制
	private boolean isTaskSafe = true;
	private boolean isLast = true;
	private boolean isRefreshFoot = false;
	private int pageNo = 1;
	private boolean isFirst = true;

	// 当前页的状态
	private String typeId = ""; // 空字符串表示类型是：“全部”
	private boolean isUserCheck = true;

	private RestaurantFoodListAdapter adapter;
	// ---
	String title;
	String leftGoBackBtn;
	String restaurantId;
	String currentFoodId;
	String restaurantName;

	// --上传图片时，保存当前菜
	public String uploadPicFoodId = "";
	public String uploadPicFoodName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		title = bundle.getString(Settings.BUNDLE_KEY_TITLE);
		leftGoBackBtn = bundle.getString(Settings.BUNDLE_KEY_LEFT_BUTTON);
		String[] idArray = bundle.getStringArray(Settings.BUNDLE_KEY_ID);
		restaurantId = idArray[0];
		currentFoodId = idArray[1];
		if (idArray.length > 2) {
			restaurantName = idArray[2];
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
		excuteTask();

	}

	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//--更新某菜品的最新评论信息
		if(adapter!=null && recentCommentData!=null){
			adapter.updateRecentComment(recentCommentData);
			recentCommentData=null;
		}
	}

	@Override
	public void finish() {
		super.finish();
		resetTask();
	}

	// ////////////-------------------------------------------------------
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText(CheckUtil.isEmpty(restaurantName)?"餐厅菜单":restaurantName);
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.setFunctionLayoutGone();
		// 内容部分
		View main = View.inflate(this, R.layout.restaurant_food_list, null);
		leftArrow = (ImageView) main.findViewById(R.id.arrow_left);
		rightArrow = (ImageView) main.findViewById(R.id.arrow_right);
		radioGroup = (RadioGroup) main.findViewById(R.id.radio_group);
		hScrollView = (HorizontalScrollView) main.findViewById(R.id.horizontal_scroll_view);
		listview = (ListView) main.findViewById(R.id.listview);
		typeLayout = (LinearLayout) main.findViewById(R.id.type_layout); // 餐厅类型容器

		// 餐厅信息
		resInfoLayout = (LinearLayout) main.findViewById(R.id.rest_layout);
		// resInfoLoadingLayout = (LinearLayout)
		// main.findViewById(R.id.restaurant_menu_resInfoLoadingLayout);
		tvResName = (TextView) main.findViewById(R.id.restaurant_menu_tvResName);
		tvOverallNum = (TextView) main.findViewById(R.id.restaurant_menu_tvOverallNum);
		tvTasteNum = (TextView) main.findViewById(R.id.restaurant_menu_tvTasteNum);
		tvEnvNum = (TextView) main.findViewById(R.id.restaurant_menu_tvEnvNum);
		tvServiceNum = (TextView) main.findViewById(R.id.restaurant_menu_tvServiceNum);
		rbOverallNum = (RatingBar) main.findViewById(R.id.restaurant_menu_rbStar);
		btnViewInfo = (Button) main.findViewById(R.id.detail_res_btnResInfoMore);

		// 这里初始化为不显示，避免加载数据的时候显示无效内容
		resInfoLayout.setVisibility(View.GONE);
		// 初始不显示
		typeLayout.setVisibility(View.GONE);

		// 根据滚动位置设置左右箭头的颜色
		hScrollView.setOnTouchListener(new View.OnTouchListener() {
			volatile boolean isDetecting = false;
			Runnable run = new Runnable() {
				int x = -1;

				@Override
				public void run() {
					try {
						while (true) {
							SystemClock.sleep(50);
							if (x != hScrollView.getScrollX()) {
								x = hScrollView.getScrollX();
								continue;
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// 滚动到左端
									if (hScrollView.getScrollX() <= 5) {
										leftArrow.setBackgroundResource(R.drawable.left_light);
									} else {
										leftArrow.setBackgroundResource(R.drawable.left_deep);
									}

									// 滚动到右端
									if (hScrollView.getScrollX() + hScrollView.getWidth() >= radioGroup.getWidth() - 5) {
										rightArrow.setBackgroundResource(R.drawable.right_light);
									} else {
										rightArrow.setBackgroundResource(R.drawable.right_deep);
									}
								}
							});

							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						isDetecting = false;
					}

				}

			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Log.e("onTouch",""+event.getAction());
				if (isDetecting) {
					return false;
				}
				isDetecting = true;
				new Thread(run).start();
				return false;
			}
		});

		// 选中某分类时的事件
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (!isUserCheck) {
					isUserCheck = true;
					// --------ga跟踪----------全部
					// TraceManager.getInstance().enterPage("/takeout/list/全部");
					// ------------------------
					return; // 如果不是人工选的直接返回
				}

				// ---
				int id = group.getCheckedRadioButtonId();
				if (id == -1) {
					return;
				}
				// ---
				View checkedButton = group.findViewById(id);
				CommonTypeDTO data = (CommonTypeDTO) checkedButton.getTag();
				setListViewData(data);
				// --------ga跟踪----------
				// TraceManager.getInstance().enterPage("/takeout/list/"+data.getName());
				// ------------------------
			}
		});
		//
		listview.setOnScrollListener(new OnScrollListener() {

			/**
			 * 添加滚动条滚到最底部，加载余下的元素
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && isRefreshFoot) {
					if (isLast == false) {
						// 获得餐厅列表
						excuteTask();
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

		this.getMainLayout().addView(main, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	}

	// 获取餐厅外卖数据
	private void excuteTask() {

		if (isTaskSafe) {
			// 线程安全的场合
			if (isLast == false) {
				// 线程安全且不是最后一页的场合，获得餐厅列表
				pageNo = pageNo + 1;
			}
			// 设置线程不安全
			this.isTaskSafe = false;
		} else {
			return;
		}
		
		// 当取的数据是第一页时，表示新的类型被选中，需要先清空数据
		if (pageNo == 1) {
			adapter = new RestaurantFoodListAdapter(RestaurantFoodListActivity.this, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					excuteTask();
				}
			});
			adapter.restName=restaurantName;
			resetTask();
			adapter.setList(null, false);
			listview.setAdapter(adapter);
		}
		// ---
		task = new GetRestaurantFoodListTask(null, this, restaurantId, currentFoodId, null, typeId, pageNo);
		task.execute(new Runnable() {

			@Override
			public void run() {
				task.closeProgressDialog();
				// ---
				if (task.dto == null) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							DialogUtil.showToast(RestaurantFoodListActivity.this, "没有菜品数据！");
						}
					});
					return;
				}
				// ----
				if (task.dto.getList().size() == 0) {
					isLast = true;
				} else {
					isLast = task.dto.pgInfo.lastTag;
				}
				// 如果是“全部”类型并且是第一页，则认为是第一次进页面，需要填充类型数据，并且自动选中类型的第一项
				if (task.dto.getTypeList() != null && task.dto.getTypeList().size() > 0) {
					typeLayout.setVisibility(View.VISIBLE);
					if (CheckUtil.isEmpty(typeId) && pageNo == 1) {
						fillTypeData(task.dto);
						if (radioGroup.getChildCount() > 0) {
							isUserCheck = false;
							((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
						}
					}
				} else {
					// 如果没有类型，则隐藏类型容器
					typeLayout.setVisibility(View.GONE);
				}
				//---如果取的数据是第一页，并且currentFoodId有效，则需要默认展开该菜的详情
				if(pageNo==1 && !CheckUtil.isEmpty(currentFoodId) && !"0".equals(currentFoodId) && task.dto.getList()!=null){
					for(ResFoodData3 data : task.dto.getList()){
						if(currentFoodId.equals(data.getUuid())){
							data.isExpanded=true;
							break;
						}
					}
				}
				//---------------------------------------------------------------------
				// 添加列表数据
				adapter.addList(task.dto.getList(), isLast);
				isTaskSafe = true;

				// 设置餐厅信息
				setRestInfoView(task.dto);

			}
		}, new Runnable() {

			@Override
			public void run() {
				task.closeProgressDialog();
				isTaskSafe = true;
			}
		});
	}

	// 填充类型数据
	private void fillTypeData(ResFoodList3DTO dto) {
		if (dto == null) {
			return;
		}

		// 外卖餐厅类型
		if (dto.getTypeList() == null || dto.getTypeList().size() == 0) {
			return;
		}
		// 先清除所有类型
		radioGroup.removeAllViews();

		for (CommonTypeDTO type : dto.getTypeList()) {
			RadioButton rb = (RadioButton) View.inflate(this, R.layout.radio_button, null);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
			lp.setMargins(UnitUtil.dip2px(2), UnitUtil.dip2px(0), UnitUtil.dip2px(2), UnitUtil.dip2px(0));
			rb.setText(type.getName());
			rb.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			rb.setTextColor(getResources().getColorStateList(R.drawable.take_away_menu_list_type_button_text_color));
			rb.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(0), UnitUtil.dip2px(10), UnitUtil.dip2px(0));
			radioGroup.addView(rb, lp);
			rb.setTag(type);
		}

		try {
			// 模拟触发事件
			MotionEvent e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0);
			hScrollView.dispatchTouchEvent(e);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 填充餐厅数据到listview中
	private void setListViewData(CommonTypeDTO data) {

		if (task != null) {
			task.cancel(true);
		}
		// 重置状态
		isTaskSafe = true;
		isLast = true;
		isRefreshFoot = false;
		pageNo = 1;
		isFirst = true;

		// 当前页的状态
		typeId = data.getUuid(); // 空字符串表示类型是：“全部”
		isUserCheck = true;
		excuteTask();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
					if (takePhotoUri != null) {
						getContentResolver().delete(takePhotoUri, null, null);
					}
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (DEBUG)
				Log.d(TAG, "uri" + path);

			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_FOOD);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID,restaurantId);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME,restaurantName);
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_ID,mFlag == ADAPTERFLAG ? uploadPicFoodId : "");
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME,mFlag == ADAPTERFLAG ? uploadPicFoodName : "");	

			Settings.uploadPictureUri = path;
			Settings.uploadPictureOrignalActivityId = 0;
			ActivityUtil.jump(RestaurantFoodListActivity.this, RestaurantUploadActivity.class, 0, bundle);

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
	private String parseImgPath(Uri uri) {
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

	/**
	 * 设置餐厅信息内容
	 */
	private void setRestInfoView(ResFoodList3DTO restaurantInfo) {
		//如果是丛餐厅图片过来的
		if (getLastActivityClass() == RestaurantPicActivity.class) {
			resInfoLayout.setVisibility(View.GONE);
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText("图片列表");
			this.getBtnOption().setOnClickListener(new OnClickListener() {
				/**
				 * 回上一页
				 */
				@Override
				public void onClick(View v) {
					finish();					
				}
			});
		} else {// 否则，右上按钮去餐厅详情
			resInfoLayout.setVisibility(View.VISIBLE);
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText("餐厅详情");
			this.getBtnOption().setOnClickListener(new OnClickListener() {
				/**
				 * 去餐厅详细页
				 */
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					// 去餐厅详细页面
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, getString(R.string.text_button_back));
					String[] nameAndLogoUrl = { restaurantName, "" };
					bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
					bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
					ActivityUtil.jump(RestaurantFoodListActivity.this, RestaurantDetailMainActivity.class, 0, bundle,true);
				}
			});
		}

		// resInfoLoadingLayout.setVisibility(View.GONE);

		if (restaurantInfo != null) {
			tvResName.setText(restaurantInfo.getRestName());
			float rate = (float) restaurantInfo.getOverallNum();
			rbOverallNum.setRating(rate);
			tvOverallNum.setText(String.valueOf(rate));
			tvTasteNum.setText(String.valueOf(restaurantInfo.getTasteNum()));
			tvEnvNum.setText(String.valueOf(restaurantInfo.getEnvNum()));
			tvServiceNum.setText(String.valueOf(restaurantInfo.getServiceNum()));

			btnViewInfo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					ViewUtils.preventViewMultipleClick(arg0, 1000);
					// 去餐厅详细页面
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_REST_ID, restaurantId);
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, getString(R.string.text_button_back));
					String[] nameAndLogoUrl = { restaurantName, "" };
					bundle.putStringArray(Settings.BUNDLE_KEY_CONTENT, nameAndLogoUrl);
					bundle.putInt(Settings.BUNDLE_showTypeTag, 1);
					ActivityUtil.jump(RestaurantFoodListActivity.this, RestaurantDetailMainActivity.class, 0, bundle);
				}
			});
		}
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
			System.gc();
		}
	}

	/**
	 * 重设列表内容取得任务
	 */
	private void resetTask(){
		recycle();
		if (task != null) {
			task.cancel(true);
			adapter.setList(null, false);
			listview.setAdapter(adapter);
			// 设置线程安全
			isTaskSafe = true;
		}
		System.gc();
	}
}
