package com.fg114.main.app.activity.resandfood;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.app.activity.usercenter.UserAccessSettingActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.data.BaseData;
import com.fg114.main.app.data.DistrictListInfo;
import com.fg114.main.app.data.RegionListInfo;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.task.AddOrUpdateRestTask;
import com.fg114.main.service.task.FoodTypeTask;
import com.fg114.main.service.task.GetDistrictListTask;
import com.fg114.main.service.task.GetRegionListTask;
import com.fg114.main.service.task.PostFeedBackTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ButtonPanelUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;

/**
 * 错误提交 添加餐厅 or 修改基本信息
 * 
 * @author wufucheng
 * 
 */
public class AddOrUpdateResActivity extends MainFrameActivity {

	private static final String TAG = "AddOrUpdateResActivity";
	// 本地缓存数据

	private RegionListInfo regionListInfo; // 行政区
	private String selectedCityId;
	private DistrictListInfo districtListInfo;// 热门商区
	private String selectedRegionId; // 行政区的id
	private String selectedDistrictId;// 热门商区的id
	private String selectedMainMenuId;// 菜系的Id
	private CommonTypeListDTO foodtypeListInfo;
	private String restaurantId; // 餐厅id
	private RestInfoData restaurantInfo;// 餐厅的信息
	// 传入参数
	private int fromPage = 0;
	private String mImageUrl;

	private int postTag = 1;// 用来区分是1 add还是 2 change
	private String mRestaurantName = "";

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;

	private static boolean isfirst_footype = true;
	private GetRegionListTask getRegionListTask;
	private GetDistrictListTask getDistrictListTask;
	private FoodTypeTask getFoodTypeTask;

	private LinearLayout mfoodtypeLayout;
	private Spinner regionSpinner;
	private Spinner districtSpinner;
	private Spinner foodtypeSpinner;

	private EditText Res_Name;
	private EditText Address_Name;
	private EditText Telephone_Number;
	private EditText Email;

	private TextView InfoTip;// 信息提示

	private Button Submit;
	private boolean islogin = false;
	private boolean iscontain = false;
	private boolean fromTakeway = false;

	// 用来保存用户输入的信息
	private String res_name;
	private String address_name;
	private String telephone_number;
	private String email;

	// 拍照上传保存路径
	private Uri takePhotoUri;

	// 用来存储行政区的名字
	private static List<String> reg_list = new ArrayList<String>();
	private static List<String> dis_list = new ArrayList<String>();
	private static List<String> foodtype_list = new ArrayList<String>();

	// 任务
	private AddOrUpdateRestTask addorupdaterestTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		// Bundle bundle = this.getIntent().getExtras();
		// fromPage = bundle.getInt(Settings.BUNDLE_KEY_FROM_PAGE);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null) {
			// 则是添加餐厅
			postTag = 1;
			// restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
			selectedCityId = SessionManager.getInstance().getCityInfo(this).getId();
		} else {
			iscontain = bundle.containsKey(Settings.BUNDLE_KEY_ID);

			if (bundle.containsKey(Settings.BUNDLE_KEY_CONTENT)) {// 如果有图片url
				mImageUrl = bundle.getString(Settings.BUNDLE_KEY_CONTENT);
			}

			if (bundle.containsKey(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME))
				mRestaurantName = bundle.getString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME);
			// 来至外卖列表页，隐藏菜系一栏，且提交不提醒传图片
			// if (getLastActivityClass() ==
			// TakeAwayRestaurantListActivity.class) {
				if (getLastActivityClass() == TakeAwayNewFoodListActivity.class) {
				fromTakeway = true;
			}
			// 获取餐厅的id
			// restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
			if (!iscontain) {
				// 则是添加餐厅
				postTag = 1;
				// restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
				selectedCityId = SessionManager.getInstance().getCityInfo(this).getId();
			} else {
				postTag = 2;// 修改基本信息
				restaurantId = bundle.getString(Settings.BUNDLE_KEY_ID);
				restaurantInfo = SessionManager.getInstance().getRestaurantInfo(this, restaurantId);
				selectedCityId = restaurantInfo.cityId;
				selectedRegionId = restaurantInfo.regionId;
				selectedDistrictId = restaurantInfo.districtId;
				selectedMainMenuId = restaurantInfo.mainMenuId;

			}

		}

		// 设置返回页
		this.setResult(fromPage);
		// 获得缓存的城市列表信息

		// selectedCityId =
		// SessionManager.getInstance().getCityInfo(this).getId();
		regionListInfo = SessionManager.getInstance().getListManager().getRegionListInfo(this, selectedCityId);
		// selectedRegionId =
		// SessionManager.getInstance().getFilter().getRegionId();
		districtListInfo = SessionManager.getInstance().getListManager().getDistrictListInfo(this, selectedRegionId);
		foodtypeListInfo = SessionManager.getInstance().getFoodMainTypeListDTO(this);
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
		// 获取行政区列表
		if (regionListInfo != null && regionListInfo.regionList != null) {
			setRegionAdapter();
		} else {
			executeGetRegionListTask();
		}

		if (foodtypeListInfo != null && foodtypeListInfo.getList() != null && foodtypeListInfo.getList().size() > 0) {
			setFoodTypeAdapter();
		} else {
			executeFoodTypeListTask();
		}
	}

	/**
	 * 初始化
	 */
	private void initComponent() {

		// 设置标题栏
		if (postTag == 1) {//
			this.getTvTitle().setText(getString(R.string.text_title_add_res));
		} else {
			this.getTvTitle().setText(getString(R.string.text_title_update_resinfo));
		}

		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		// 获取是否登录状态
		islogin = SessionManager.getInstance().isUserLogin(getApplicationContext());
		if (islogin == true) {
			// 如果已经登录，则按钮不显示
			this.getBtnOption().setVisibility(View.INVISIBLE);
		} else {
			// 如果未登录则显示登录按钮
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText(R.string.text_button_login);
			this.getBtnOption().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ViewUtils.preventViewMultipleClick(v, 1000);
					DialogUtil.showUserLoginDialog(AddOrUpdateResActivity.this, new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

						}

					}, 0);
				}

			});
		}
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.add_or_update_resinfo, null);
		regionSpinner = (Spinner) contextView.findViewById(R.id.add_or_update_resInfo_regionSpinner);
		districtSpinner = (Spinner) contextView.findViewById(R.id.add_or_update_resInfo_districtSpinner);
		foodtypeSpinner = (Spinner) contextView.findViewById(R.id.add_or_update_resInfo_foodtypeSpinner);
		mfoodtypeLayout = (LinearLayout) contextView.findViewById(R.id.add_or_update_resInfo_foodtype_layout);
		Res_Name = (EditText) contextView.findViewById(R.id.add_or_update_resInfo_etResName);
		Address_Name = (EditText) contextView.findViewById(R.id.add_or_update_resInfo_detailaddress);
		Telephone_Number = (EditText) contextView.findViewById(R.id.add_or_update_resInfo_ettelephone);

		Email = (EditText) contextView.findViewById(R.id.add_or_update_resInfo_etEmail);

		InfoTip = (TextView) contextView.findViewById(R.id.add_or_update_resInfo_tvTip);

		// 以下用来区分添加餐厅还是修改基本信息的提示 1 添加 2 报错
		if (postTag == 1) {
			if (!CheckUtil.isEmpty(mRestaurantName)) {
				Res_Name.setText(mRestaurantName);
			}
			InfoTip.setText(R.string.text_layout_addorupdate_tip1);
		} else {
			InfoTip.setText(R.string.text_layout_addorupdate_tip1);
			Res_Name.setText(restaurantInfo.name);
			Address_Name.setText(restaurantInfo.address);
			Telephone_Number.setText(restaurantInfo.telForEdit);

		}
		if (fromTakeway) {
			mfoodtypeLayout.setVisibility(View.GONE);
			selectedMainMenuId = "";
		} else {
			mfoodtypeLayout.setVisibility(View.VISIBLE);
		}
		Submit = (Button) contextView.findViewById(R.id.add_or_update_resInfo_btnSubmit);

		// 用户点击提交按钮
		Submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (checkInput() == false) {
					return;
				}
				executeAddOrUpdate();
			}

		});

		regionSpinner.setClickable(true);
		regionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

				districtSpinner.setClickable(false);

				int index = regionSpinner.getSelectedItemPosition();
				if (reg_list != null) {
					String region_name = reg_list.get(index);
					if (region_name != null) {
						for (int i = 0; i < regionListInfo.regionList.size(); i++) {
							if (regionListInfo.regionList.get(i).getName().equals(region_name)) {
								selectedRegionId = regionListInfo.regionList.get(i).getId();
								// 在这里去请求相对应的商区
								districtListInfo = SessionManager.getInstance().getListManager().getDistrictListInfo(getApplicationContext(), selectedRegionId);
								if (districtListInfo.districtList != null) {
									setDistricAdapter();
									return;
								}
								executeGetDistricListTask();
								return;
							}
						}

					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		districtSpinner.setClickable(true);
		districtSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

				int index = districtSpinner.getSelectedItemPosition();
				if (dis_list != null) {
					String dis_name = dis_list.get(index);
					if (dis_name != null) {
						for (int i = 0; i < districtListInfo.districtList.size(); i++) {
							if (districtListInfo.districtList.get(i).getName().equals(dis_name)) {
								// 用来保存选择的热门商区
								selectedDistrictId = districtListInfo.districtList.get(i).getId();

							}
						}
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		foodtypeSpinner.setClickable(false);
		foodtypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				int index = foodtypeSpinner.getSelectedItemPosition();
				if (foodtype_list != null) {
					String foodtype_name = foodtype_list.get(index);
					for (int i = 0; i < foodtypeListInfo.getList().size(); i++) {
						if (foodtypeListInfo.getList().get(i).getName().equals(foodtype_name)) {
							selectedMainMenuId = foodtypeListInfo.getList().get(i).getUuid();
						}
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	/**
	 * 获得地区列表
	 */
	private void executeGetRegionListTask() {
		// 缓存未过期，一星期
		if (regionListInfo != null && regionListInfo.getRegionList() != null && regionListInfo.getRegionList().size() > 0
				&& (System.currentTimeMillis() - regionListInfo.getTimestamp()) < 7 * 86400000) {
			// 设置列表适配器
			setRegionAdapter();
		} else {

			// 创建任务
			getRegionListTask = new GetRegionListTask(null, this, selectedCityId, regionListInfo.getTimestamp());
			// 执行任务
			getRegionListTask.execute(new Runnable() {

				@Override
				public void run() {

					CommonTypeListDTO dto = getRegionListTask.dto;
					if (dto != null) {
						// 需要更新列表的场合，将结果装入缓存
						regionListInfo = new RegionListInfo();
						regionListInfo.setTimestamp(System.currentTimeMillis());
						regionListInfo.setLastUpdateTime(new Date().getTime());

						List<BaseData> regionList = ConvertUtil.convertDTOListToDataList(dto.getList(), 0);
						regionListInfo.setRegionList(regionList);
						SessionManager.getInstance().getListManager().setRegionListInfo(AddOrUpdateResActivity.this, selectedCityId, regionListInfo);

						// 设置列表适配器
						setRegionAdapter();
					}
				}
			});
		}
	}

	/**
	 * 获得商区列表
	 */
	private void executeGetDistricListTask() {
		// 取数据时禁止点击
		districtSpinner.setEnabled(false);
		// 缓存未过期，一星期
		if (districtListInfo != null && districtListInfo.getDistrictList() != null && districtListInfo.getDistrictList().size() > 0
				&& (System.currentTimeMillis() - districtListInfo.getTimestamp()) < 7 * 86400000) {
			// 设置列表适配器
			setDistricAdapter();
			// 恢复
			districtSpinner.setEnabled(true);
		} else {
			// 创建任务
			getDistrictListTask = new GetDistrictListTask(null, this, selectedCityId, selectedRegionId, districtListInfo.getTimestamp());
			// 执行任务
			getDistrictListTask.execute(new Runnable() {

				@Override
				public void run() {

					CommonTypeListDTO dto = getDistrictListTask.dto;
					if (dto != null) {
						// if (true) {
						// 需要更新列表的场合，将结果装入缓存
						if (DEBUG)
							Log.d(TAG, "update regionList");

						districtListInfo = new DistrictListInfo();
						districtListInfo.setTimestamp(System.currentTimeMillis());
						districtListInfo.setLastUpdateTime(new Date().getTime());
						List<BaseData> districtList = ConvertUtil.convertDTOListToDataList(dto.getList(), 0);
						districtListInfo.setDistrictList(districtList);
						SessionManager.getInstance().getListManager().setDistrictListInfo(AddOrUpdateResActivity.this, selectedRegionId, districtListInfo);

						// 设置列表适配器
						setDistricAdapter();
						// 恢复
						districtSpinner.setEnabled(true);
					}
				}
			}, new Runnable() {
				@Override
				public void run() {
					// 恢复
					districtSpinner.setEnabled(true);
				}
			});
		}
	}

	// 获取菜系列表
	private void executeFoodTypeListTask() {

		getFoodTypeTask = new FoodTypeTask(null, this, selectedCityId, foodtypeListInfo.timestamp);

		getFoodTypeTask.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				foodtypeListInfo = getFoodTypeTask.dto;
				if (foodtypeListInfo != null) {
					if (foodtypeListInfo.needUpdateTag || isfirst_footype) {
						SessionManager.getInstance().setFoodMainTypeListDTO(AddOrUpdateResActivity.this, foodtypeListInfo);
						isfirst_footype = false;
					}
				}
				setFoodTypeAdapter();
			}

		});
	}

	// 设置菜系适配器
	private void setFoodTypeAdapter() {
		foodtype_list.clear();
		for (int i = 0; i < foodtypeListInfo.getList().size(); i++) {
			foodtype_list.add(foodtypeListInfo.getList().get(i).getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_flexible_simple, foodtype_list);
		adapter.setDropDownViewResource(R.layout.spinner_item);
		foodtypeSpinner.setAdapter(adapter);
		if (foodtype_list.size() > 0) {
			foodtypeSpinner.setClickable(true);
			// foodtypeSpinner.setSelection(0,true);
			if (postTag == 2) {
				for (int i = 0; i < foodtype_list.size(); i++) {
					if (foodtype_list.get(i).equals(restaurantInfo.mainMenuName)) {
						foodtypeSpinner.setSelection(i, true);
					}
				}
			}else {
				foodtypeSpinner.setSelection(0, true);
			}
		} else {
			foodtypeSpinner.setClickable(false);
		}

	}

	private void setRegionAdapter() {

		// 在这处理
		reg_list.clear();
		for (int i = 0; i < regionListInfo.regionList.size(); i++) {
			reg_list.add(regionListInfo.regionList.get(i).getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_flexible_simple, reg_list);
		// 设置下拉菜单风格
		adapter.setDropDownViewResource(R.layout.spinner_item);

		regionSpinner.setAdapter(adapter);
		// isFirstUseRegionSpinner = true;
		if (reg_list.size() > 0) {
			regionSpinner.setClickable(true);
		} else {
			regionSpinner.setClickable(false);
		}
		if (reg_list != null && reg_list.size() > 0) {
			if (postTag == 2) {
				for (int i = 0; i < reg_list.size(); i++) {
					if (reg_list.get(i).equals(restaurantInfo.regionName)) {
						regionSpinner.setSelection(i, true);
					}
				}
			} else {
				regionSpinner.setSelection(0, true);
			}
		}
		// regionSpinner.setSelection(1,true);
	}

	private void setDistricAdapter() {
		dis_list.clear();
		for (int i = 0; i < districtListInfo.districtList.size(); i++) {
			dis_list.add(districtListInfo.districtList.get(i).getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_flexible_simple, dis_list);
		// 设置下拉菜单风格
		adapter.setDropDownViewResource(R.layout.spinner_item);
		districtSpinner.setAdapter(adapter);

		if (dis_list.size() > 0) {
			districtSpinner.setClickable(true);
		} else {
			districtSpinner.setClickable(false);
		}
		if (dis_list != null && dis_list.size() > 0) {
			if (postTag == 2) {
				for (int i = 0; i < dis_list.size(); i++) {
					if (dis_list.get(i).equals(restaurantInfo.districtName)) {
						districtSpinner.setSelection(i, true);
					}
				}
			} else {
				districtSpinner.setSelection(0, true);
			}
		}
		// districtSpinner.setSelection(0,true);
	}

	// 用来检查用户输入的合法性
	private boolean checkInput() {
		res_name = Res_Name.getText().toString().trim();
		if (CheckUtil.isEmpty(res_name)) {
			ViewUtils.setError(Res_Name, "商户名不能为空");
			Res_Name.requestFocus();
			return false;
		}
		if (res_name.length() > 200) {
			ViewUtils.setError(Res_Name, "不能超过100个字符");
			Res_Name.requestFocus();
			return false;
		}
		address_name = Address_Name.getText().toString().trim();

		telephone_number = Telephone_Number.getText().toString().trim();

		email = Email.getText().toString().trim();
		// if(CheckUtil.isEmpty(address_name)){
		// Address_Name.requestFocus();
		// return false;
		// }
		if (address_name.length() > 200) {
			ViewUtils.setError(Address_Name, "不能超过100个字符");
			Address_Name.requestFocus();
			return false;
		}
		
		if(CheckUtil.isEmpty(address_name)){
			DialogUtil.showToast(AddOrUpdateResActivity.this, "请输入地址");
			return false;
		}
		return true;
	}

	// 用来检查Spinner的id是否为空
	private boolean checkSpinner() {

		return true;

	}

	// 用来提交用户信息
	private void executeAddOrUpdate() {

		String cityId = SessionManager.getInstance().getCityInfo(this).getId();

		String token = "";

		if (islogin) {
			token = SessionManager.getInstance().getUserInfo(AddOrUpdateResActivity.this).getToken();
		}

		if (fromTakeway) {
			postTag = 3;
		}

		addorupdaterestTask = new AddOrUpdateRestTask(getString(R.string.text_info_uploading), AddOrUpdateResActivity.this, res_name, restaurantId == null ? "" : restaurantId, selectedRegionId,
				selectedDistrictId, selectedCityId, address_name, selectedMainMenuId, telephone_number, email, token, postTag);

		ViewUtils.hideSoftInput(this, Email);

		addorupdaterestTask.execute(new Runnable() {
			// 在这里对返回进行相关的处理
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// if(postTag==1){
				// DialogUtil.showToast(AddOrUpdateResActivity.this,
				// getString(R.string.text_dialog_add_res_success));
				// }else{
				// DialogUtil.showToast(AddOrUpdateResActivity.this,
				// getString(R.string.text_dialog_error_submit_success));
				// }
				String text = "";
				if (postTag == 1)
					text = "继续上传照片，赚取秘币吧！";
				else
					text = "继续上传照片，赚取秘币吧！";
				addorupdaterestTask.closeProgressDialog();

				if (postTag == 1) {
					// 新增的情况返回uuid
					restaurantId = addorupdaterestTask.uuid;
					//下面设置id和名字，供从推荐餐厅来的页面使用新增加的餐厅
					RecommandRestaurantSubmitActivity.newRestId=restaurantId;
					RecommandRestaurantSubmitActivity.newRestName=res_name;
				}

				if (fromTakeway) {
					DialogUtil.showToast(AddOrUpdateResActivity.this, "提交成功");
					finish();
				} else {
					DialogUtil.showAlert(AddOrUpdateResActivity.this, true, "提交成功", text, "上传照片", "以后再传", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (ActivityUtil.checkMysoftStage(AddOrUpdateResActivity.this)) {
								ButtonPanelUtil pan = new ButtonPanelUtil();
								pan.showUploadPanel(Submit, AddOrUpdateResActivity.this, restaurantInfo);
								pan.setOnGetUriListener(new ButtonPanelUtil.OnGetUriListener() {

									@Override
									public void onGetUri(Uri uri) {
										takePhotoUri = uri;
									}
								});
							}
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
				}
			}
		});
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
					getContentResolver().delete(takePhotoUri, null, null);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			mImageUrl = path;
			Settings.uploadPictureUri = path;

			// 为了上传图片结束后，跳转页面服务------------------nieyinyin add
			Settings.uploadPictureOrignalActivityClazz = getLastActivityClass();

			Bundle bundle = new Bundle();
			bundle.putString(Settings.BUNDLE_UPLOAD_TYPE, Settings.UPLOAD_TYPE_RESTAURANT);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_ID, restaurantId);
			bundle.putString(Settings.BUNDLE_UPLOAD_RESTAURANT_NAME, res_name);
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_ID, "");
			bundle.putString(Settings.BUNDLE_UPLOAD_FOOD_NAME, "");
			ActivityUtil.jump(AddOrUpdateResActivity.this, RestaurantUploadActivity.class, 0, bundle);

			// bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE, fromPage);
			// bundle.putString(Settings.BUNDLE_KEY_ID, restaurantId);
			// bundle.putString(Settings.BUNDLE_KEY_CONTENT, path);
			// ActivityUtil.jump(AddOrUpdateResActivity.this,
			// RestaurantUploadActivity.class,
			// Settings.ADD_OR_UPDATE_RES_ACTIVITY, bundle);
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

	@Override
	protected void onResume() {
		super.onResume();
		islogin = SessionManager.getInstance().isUserLogin(getApplicationContext());
		if (islogin == true) {
			// 如果已经登录，则按钮不显示
			this.getBtnOption().setVisibility(View.INVISIBLE);
			// 如果登录了，则默认显示用户邮箱
			// Email.setText(SessionManager.getInstance().getUserInfo(this).getEmail());
		} else {
			// 如果未登录则显示登录按钮
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText(R.string.text_button_login);
			this.getBtnOption().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					DialogUtil.showUserLoginDialog(AddOrUpdateResActivity.this, new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
						}

					}, 0);
				}

			});
		}
	}
}
