package com.fg114.main.app.activity.resandfood;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.AutoUpdateActivity;
import com.fg114.main.app.activity.CityActivity;
import com.fg114.main.app.activity.ErrorReportActivity;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.SelectMultiplePictureActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.SimpleWebViewActivity;

import com.fg114.main.app.activity.MainFrameActivity.OnShowUploadImageListener;
import com.fg114.main.app.activity.order.MyShortMessageOrderListActivity;
import com.fg114.main.app.activity.order.SelectSMSActivity;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantSearchActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.AutoCompleteAdapter;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.data.ImageData;
import com.fg114.main.app.listener.OnProcessPictureListener;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.BubbleHintData;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.MainPageAdvData;
import com.fg114.main.service.dto.MainPageInfoPackDTO;
import com.fg114.main.service.dto.OrderHintData;
import com.fg114.main.service.dto.OrderListDTO;
import com.fg114.main.service.dto.ResFoodData3;
import com.fg114.main.service.dto.RestRecomAddHintData;
import com.fg114.main.service.dto.RestRecomFormData;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.AddDebugAccountTask;
import com.fg114.main.service.task.BaseTask;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.ProcessPictureTask;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.service.task.VerifyTestUserTask;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.MutipleFileInputStream;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.TrackTool;
import com.fg114.main.util.URLExecutor;
import com.fg114.main.util.UnitUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 我要荐店
 * 
 * @author xujianjun, 2013-10-12
 * 
 */
public class RecommandRestaurantSubmitActivity extends MainFrameActivity {

	private static final int IMAGE_SIZE = 700; // 图片边长限制
	private static final int IMAGE_QUALITY = 80; // 图片压缩率
	public static String newRestId;
	public static String newRestName;
	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView list_view;
	private View header_view;
	private View footer_view;
	private BaseAdapter adapter;
	private ArrayList<ImageData> imageDataList = new ArrayList<ImageData>();
	private int selectedCoverIndex = -1;

	private String restId;
	private String restName;
	private TextView hint;
	private EditText title;
	private TextView tv_rest_name;
	private Button button_select_rest;
	private Button button_add_more;
	private Button button_ok;

	private RestRecomFormData restRecomFormData;
	
	//添加或者修改图片时，临时图片存放目录
	String targetPicPath = android.os.Environment.getExternalStorageDirectory() + File.separator + Settings.IMAGE_CACHE_DIRECTORY + File.separator;
	// 记录拍照的文件名生成因子，每次一个新进入页面都重新从1开始，为了防止上传的临时图片泛滥，后生成的图片会重用以前的文件名
	int fileNameCount = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("推荐表单", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		restId = bundle.getString(Settings.BUNDLE_REST_ID);
		restName = bundle.getString(Settings.BUNDLE_REST_NAME);
		//
		// restId = "s";
		// restName = "大黄鸦餐厅";
		// ---
//		restRecomAddHintData = SessionManager.getInstance().getMainPageInfoPackDTO().restRecomAddHintData;
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
		
		executeRestRecomFormInfo();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("推荐表单", "");
		// ----------------------------
		//新增餐厅后带过来的
		if(!CheckUtil.isEmpty(newRestId)){
			restId=newRestId;
			restName=newRestName;
			newRestId="";
			newRestName="";
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Fg114Application.isNeedUpdate = true;
		if (!SessionManager.getInstance().isUserLogin(this)) {
			this.getBtnOption().setVisibility(View.VISIBLE);
		} else {
			this.getBtnOption().setVisibility(View.INVISIBLE);
		}
		// 已有餐厅信息的，先显示出来
		if (!CheckUtil.isEmpty(restId) && !CheckUtil.isEmpty(restName)) {
			tv_rest_name.setText(restName);
		}

	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		this.getTvTitle().setText("我要荐店");
		this.getBtnOption().setText("登录");
		this.getBtnGoBack().setText("返回");
		this.getBtnGoBack().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.recommand_restaurant_submit, null);
		list_view = (ListView) contextView.findViewById(R.id.list_view);
		header_view = View.inflate(this, R.layout.list_item_recommand_restaurant_submit_header, null);
		footer_view = View.inflate(this, R.layout.list_item_recommand_restaurant_submit_footer, null);

		// 头部初始化
		hint = (TextView) header_view.findViewById(R.id.hint);
		title = (EditText) header_view.findViewById(R.id.title);
		tv_rest_name = (TextView) header_view.findViewById(R.id.rest_name);
		button_select_rest = (Button) header_view.findViewById(R.id.button_select_rest);

		// 底部初始化
		button_add_more = (Button) footer_view.findViewById(R.id.button_add_more);
		button_ok = (Button) footer_view.findViewById(R.id.button_ok);

		list_view.addHeaderView(header_view);
		list_view.addFooterView(footer_view);
		
		// 添加
		button_add_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 300);
				// -----
				OpenPageDataTracer.getInstance().addEvent("上传照片按钮");
				// -----
				if(imageDataList.size()>=20){
					DialogUtil.showAlert(RecommandRestaurantSubmitActivity.this, "提示", "已达到图片最大上传数量！");
					return;
				}
				takeBatchPicture();

			}
		});
		// 提交
		button_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (checkInput()) {
					executeUpload();
				}
			}
		});
		// 选择餐厅
		button_select_rest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("选择餐厅按钮");
				// -----
				ViewUtils.preventViewMultipleClick(v, 1000);
				AutoCompleteAdapter.isRecomRest=true;
				ActivityUtil.jump(RecommandRestaurantSubmitActivity.this, RestaurantSearchActivity.class, 1, new Bundle());
			}
		});
		// 点击去登录
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("登录按钮");
				// -----
				Bundle bundle = new Bundle();
				ActivityUtil.jump(RecommandRestaurantSubmitActivity.this, UserLoginActivity.class, 0, bundle);
			}
		});
		
		hint.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				if(!CheckUtil.isEmpty(restRecomFormData.addHintActionXmsUrl)){
					// -----
					OpenPageDataTracer.getInstance().addEvent("提示按钮");
					// -----
//					ActivityUtil.jumpToWebNoParam(restRecomFormData.addHintActionXmsUrl, "",false, null);
					URLExecutor.execute(restRecomFormData.addHintActionXmsUrl,RecommandRestaurantSubmitActivity.this, 0);
				}
			}
		});

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

	}

	private boolean isEmptyImage(ImageData imageData) {
		if (imageData == null || CheckUtil.isEmpty(imageData.imagePath) || CheckUtil.isEmpty(imageData.description)) {
			return true;
		}
		return false;
	}

	// 初始化adapter
	private void initAdapter() {
		adapter = new MyAdapter();
		//imageDataList.add(new ImageData()); // 初次进入页面，默认有一个要空的上传的位置
		list_view.setAdapter(adapter);
	}

	private void executeRestRecomFormInfo(){
		ServiceRequest request = new ServiceRequest(API.getRestRecomFormInfo);
		// --------------
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// --------------
		CommonTask.request(request, "获取数据中，请稍候...", new CommonTask.TaskListener<RestRecomFormData>() {

			@Override
			protected void onSuccess(RestRecomFormData dto) {
				// 初始化adapter
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				restRecomFormData=dto;
				initAdapter();
				

				// 设置提示信息
				if (dto != null) {

					if (!CheckUtil.isEmpty(dto.addHint)) {
						hint.setText(Html.fromHtml(dto.addHint));
					}
					// --
					if (!CheckUtil.isEmpty(dto.titleHint)) {
						title.setHint(dto.titleHint);
					}
					// --
					if (!CheckUtil.isEmpty(dto.selectRestHint)) {
						tv_rest_name.setHint(dto.selectRestHint);
					}
					
					

				}

			};

			protected void onError(int code, String message) {
				// --------------
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// --------------
				super.onError(code, message);
				finish();
			};
		});
	}
	private void executeUpload() {

		// 准备数据-------------
		// 文件名
		final MutipleFileInputStream inputStream;
		MutipleFileInputStream tin = null;
		final ServiceRequest request = new ServiceRequest(API.addRestRecom);
		try {
			tin = inputStream = new MutipleFileInputStream(imageDataList);
			request.addData("title", title.getText().toString());// 标题
			request.addData("restId", restId);// 餐厅id
			request.addData("coverIdx", selectedCoverIndex);// 封面图片索引，如果用户未选择，取-1，
															// 否则从0开始
			// 图片大小 可以为空 多图为
			// 13242314;29282;29282(注意：第一个数据块存放的是所有的图片描述，用|分隔。字符串使用utf-8字节流)
			request.addData("imgSizeList", inputStream.getImageSizeList());
			request.addData(inputStream);
			// -----
			OpenPageDataTracer.getInstance().addEvent("完成按钮");
			// -----
		} catch (Exception e) {
			DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "提交没有成功，请稍后再次尝试");
			// -----
			OpenPageDataTracer.getInstance().endEvent("完成按钮");
			// -----
			e.printStackTrace();
			if (tin != null) {
				try {
					tin.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			return;
		}
		// -----
		// OpenPageDataTracer.getInstance().addEvent("头像按钮");
		// -----
		CommonTask.request(request, "正在提交...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("完成按钮");
				// -----
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				// OpenPageDataTracer.getInstance().endEvent("头像按钮");
				// -----
				DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "提交成功!");
				//返回餐厅详情页 刷新
				Settings.NEED_TAG_REST_RECOMMEND=true;
				Settings.COMMENT_RES_ID=restId;
				
				RecommandRestaurantSubmitActivity.super.finish();

			};

			protected void onError(int code, String message) {
				// -----
				OpenPageDataTracer.getInstance().endEvent("完成按钮");
				// -----
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----
				// OpenPageDataTracer.getInstance().endEvent("头像按钮");
				// -----
				DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, message);
			};
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean checkInput() {

		if (CheckUtil.isEmpty(title.getText().toString())) {
			DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "请填写推荐标题!");
			return false;
		}
		if (CheckUtil.isEmpty(restId) || CheckUtil.isEmpty(restName)) {
			DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "请选择要推荐的餐厅!");
			return false;
		}
		// ----
		if (imageDataList == null || imageDataList.size() == 0) {
			DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "请至少上传一张图片!");
			return false;
		}
		//
		for (int i = 0; i < imageDataList.size(); i++) {
			if (isEmptyImage(imageDataList.get(i))) {
				DialogUtil.showToast(RecommandRestaurantSubmitActivity.this, "图片信息填写不完整!");
				return false;
			}
		}
		if (!SessionManager.getInstance().isUserLogin(this)) {
			// DialogUtil.showToast(RecommandRestaurantSubmitActivity.this,
			// "您还未登录，请先登录!");
			Bundle bundle = new Bundle();
			ActivityUtil.jump(RecommandRestaurantSubmitActivity.this, UserLoginActivity.class, 0, bundle);
			return false;
		}
		return true;
	}

	void print() {
		Log.d("--------------", "---------------------");
		for (ImageData data : imageDataList) {
			Log.w("data", data.imagePath + "," + data.description);
		}
	}

	boolean isChanged() {
		if (!CheckUtil.isEmpty(title.getText().toString())) {
			return true;
		}
		if (!CheckUtil.isEmpty(restId) || !CheckUtil.isEmpty(restName)) {
			return true;
		}
		// ----
		if (imageDataList == null) {
			return false;
		}
		//
		for (int i = 0; i < imageDataList.size(); i++) {
			if (!CheckUtil.isEmpty(imageDataList.get(i).imagePath) || !CheckUtil.isEmpty(imageDataList.get(i).description)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (isChanged()) {
			DialogUtil.showComfire(this, "提示", "您还没有提交，是否要退出页面？", new String[] { "退出", "取消" }, new Runnable() {

				@Override
				public void run() {
					// -----
					OpenPageDataTracer.getInstance().addEvent("返回按钮");
					// -----
					finish();
				}
			}, new Runnable() {

				@Override
				public void run() {
				}
			});
		} else {
			// -----
			OpenPageDataTracer.getInstance().addEvent("返回按钮");
			// -----
			finish();
		}
	}

	class MyAdapter extends BaseAdapter {
		// 记录获取焦点的item的index
		int focusedIndex = -1;
		int cursorPosition = -1;

		class ViewHolder {
			public MyImageView picture;
			public CheckBox be_cover;
			public EditText description;
			public Button delete;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			convertView = mInflater.inflate(R.layout.list_item_recommand_restaurant_submit_item, null);
			holder = new ViewHolder();
			holder.picture = (MyImageView) convertView.findViewById(R.id.picture);
			holder.be_cover = (CheckBox) convertView.findViewById(R.id.be_cover);
			holder.description = (EditText) convertView.findViewById(R.id.description);
			holder.delete = (Button) convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
			final ImageData data = imageDataList.get(position);

			// --默认提示
			if (restRecomFormData != null) {
				if (!CheckUtil.isEmpty(restRecomFormData.detailHint)) {
					holder.description.setHint(restRecomFormData.detailHint);
				}
				if (!CheckUtil.isEmpty(restRecomFormData.coverHint)) {
					holder.be_cover.setText(restRecomFormData.coverHint);
				}
			}
			// -----
			if (selectedCoverIndex == position) {
				holder.be_cover.setChecked(true);
			} else {
				holder.be_cover.setChecked(false);
			}
			holder.description.setText(data.description);
			// ------------
			
			if (!CheckUtil.isEmpty(data.imagePath)) {
				holder.picture.setImageByUrl(data.imagePath, true, position, ScaleType.FIT_XY);
			} else {
				holder.picture.setImageResource(R.drawable.bg_add_picture);
				holder.picture.setScaleType(ScaleType.FIT_XY);
			}

			// ------
			holder.be_cover.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						selectedCoverIndex = position;
					} else {
						selectedCoverIndex = -1;
					}
					adapter.notifyDataSetChanged();// 刷新list
				}
			});
			// --
			holder.description.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					data.description = holder.description.getText().toString();
					// print();

				}
			});
			holder.description.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						focusedIndex = position;
						cursorPosition = holder.description.getSelectionStart();
						// Log.e("cursorPosition set"+position,holder.description.getSelectionEnd()+"cursorPosition="+cursorPosition);
//						holder.description.setFocusable(true);
//						holder.description.requestFocus();
//						holder.description.setFocusableInTouchMode(true);
					}
				}
			});
			holder.description.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					if(arg1.getAction()==MotionEvent.ACTION_UP){
					focusedIndex = position;
					}
					return false;
				}
			});
			holder.description.clearFocus();
			if(focusedIndex!=-1&&focusedIndex==position){
				holder.description.requestFocus();
			}
			holder.description.setSelection(holder.description.getText().length());
			// --删除当前图片项
			holder.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					DialogUtil.showComfire(RecommandRestaurantSubmitActivity.this, "确认删除", "是否删除此图片？", new String[] { "删除", "取消" }, new Runnable() {

						@Override
						public void run() {
							imageDataList.remove(position);
							adapter.notifyDataSetChanged();// 刷新list
							list_view.setSelection(imageDataList.size());
							if (selectedCoverIndex == position) {
								selectedCoverIndex = -1;// 同步封面索引
							}
						}
					}, new Runnable() {
						@Override
						public void run() {
						}
					});

				}
			});
			// --点击上传
			holder.picture.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v){
					ViewUtils.preventViewMultipleClick(v, 500);
					takePicture(data);
				}
			});
//			// 焦点处理
			try {
				if (focusedIndex == position) {
					holder.description.requestFocusFromTouch();
					// Log.e("cursorPosition"+position,"cursorPosition="+cursorPosition);
					holder.description.setSelection(cursorPosition);
					//holder.description.requestFocusFromTouch();
				}
			} catch (Exception ex) {
				Log.e("focusedIndex", ex.getMessage(), ex);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			if (position >= 0 && position < imageDataList.size()) {
				imageDataList.get(position);
			}
			return null;
		}

		@Override
		public int getCount() {
			return imageDataList.size();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//是选择餐厅
		if (resultCode == 12345 && data != null) {
			newRestId="";
			newRestName="";
			restId = data.getStringExtra(Settings.BUNDLE_REST_ID);
			restName = data.getStringExtra(Settings.BUNDLE_REST_NAME);
			tv_rest_name.setText(restName);
		}
	}
	//添加图片，批量添加
	private void takeBatchPicture() {
		//控制最大可选择数量
		int maxAllowedCount=20-imageDataList.size();
		Bundle data=new Bundle();
		data.putInt(SelectMultiplePictureActivity.KEY_MAX_ALLOWED_COUNT, maxAllowedCount);
		//
		takeBatchPic(new OnShowUploadImageListener() {
			
			//添加单张
			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				
				if (!CheckUtil.isEmpty(path)) {
					String tempPath = ActivityUtil.getGPSPicturePath(path, targetPicPath + "tempRecommendPic" + fileNameCount++);
					com.fg114.main.app.Settings.uploadPictureUri = "";
					ImageData data=new ImageData();
					data.imagePath = tempPath;
					imageDataList.add(data);
					adapter.notifyDataSetChanged();
				}
			}
			//添加批量
			@Override
			public void onGetBatchPic(ArrayList<String[]> picture_data_selected) {
				if (picture_data_selected==null || picture_data_selected.size()==0) {
					return;
				}
				new ProcessPictureTask("正在处理图片...", 
						RecommandRestaurantSubmitActivity.this,
						picture_data_selected,
						new OnProcessPictureListener() {
					
					@Override
					public void onProcessPicture(String[] picture_data) {
						String tempPath = ActivityUtil.getGPSPicturePath(picture_data[1], targetPicPath + "tempRecommendPic" + fileNameCount++);
						ImageData data=new ImageData();
						data.imagePath = tempPath;
						imageDataList.add(data);
					}
				}).execute(new Runnable() {
					
					@Override
					public void run() {
						//成功后刷新列表
						adapter.notifyDataSetChanged();
						list_view.setSelection(imageDataList.size() - 1);
					}
				});
					
			}
		},data);
	}
	private void takePicture(final ImageData data) {
		
		takePic(new OnShowUploadImageListener() {

			@Override
			public void onGetPic(Bundle bundle) {
				String path = com.fg114.main.app.Settings.uploadPictureUri;
				if (!CheckUtil.isEmpty(path)) {
					String tempPath = ActivityUtil.getGPSPicturePath(path, targetPicPath + "tempRecommendPic" + fileNameCount++);
					com.fg114.main.app.Settings.uploadPictureUri = "";
					data.imagePath = tempPath;
					adapter.notifyDataSetChanged();// 刷新list
				}
			}

		}, false);
	}

}


