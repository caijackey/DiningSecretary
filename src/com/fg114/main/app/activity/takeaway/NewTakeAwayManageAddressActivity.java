package com.fg114.main.app.activity.takeaway;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.view.PopupWindow;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.UserTkRaData;
import com.fg114.main.service.dto.UserTkRaListPack2DTO;
import com.fg114.main.service.dto.UserTkRaListPackDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.util.DialogUtil.DialogEventListenerNew;
import com.fg114.main.util.UnitUtil;
import com.google.xiaomishujson.Gson;
/** 外卖地址管理  uuid
 * @author sunquan 
 */
public class NewTakeAwayManageAddressActivity extends MainFrameActivity {

	private static final String TAG = "NewTakeAwayAddAddressActivity";

	private LayoutInflater mInflater;
	private View contextView;

	private LinearLayout contentLayout;

	private String uuid = "";

	private Intent intent;
	
	private String backUUID="";
	private String backAddress="";
	private String backPhoneNumber="";
	private String backpersonname="";
	
	private boolean isFirst=true;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖地址管理", "");
		// ----------------------------

		// 获得传入参数
		intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			uuid = bundle.getString(Settings.UUID);
		}

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this);
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			// TODO: 跳至无网提示页
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		// 初始化界面
		initComponent();
		excuteUserTakeoutReceiveAddressListTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖地址管理", "");
		// ----------------------------
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置Header标题栏
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getTvTitle().setText("地址管理");
		this.getBtnOption().setVisibility(View.VISIBLE);
		this.getBtnOption().setText("新增");
		this.setFunctionLayoutGone();

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.newtake_away_manage_address_layout, null);

		contentLayout = (LinearLayout) contextView.findViewById(R.id.take_wawy_order_address_layout);
		contextView.setVisibility(View.GONE);
		this.getBtnOption().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				openAddressManagerDialog("", "", "","");
			}
		});

		this.getBtnGoBack().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// -----
				OpenPageDataTracer.getInstance().addEvent("返回按钮");
				// -----
				backActivity();
			}
		});
		
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}

	// 获得用户外卖收货地址列表，返回UserTkRaListPackDTO
	private void excuteUserTakeoutReceiveAddressListTask() {

		// ----
		OpenPageDataTracer.getInstance().addEvent("页面查询");
		// -----
        
		ServiceRequest request = new ServiceRequest(API.getUserTakeoutReceiveAddressList2);
		CommonTask.request(request, "正在加载...", new CommonTask.TaskListener<UserTkRaListPack2DTO>() {

			@Override
			protected void onSuccess(UserTkRaListPack2DTO dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				contextView.setVisibility(View.VISIBLE);
				contentLayout.removeAllViews();
				// 列表 uuid:uuid name:地址 phone:电话
				List<UserTkRaData> list = dto.list;
				if ((list==null||list.size()==0)&&isFirst) {
					NewTakeAwayManageAddressActivity.this.getBtnOption().performClick();
					isFirst=false;
				}
				
				for (int i = 0; i < list.size(); i++) {
					UserTkRaData userTkRaData = list.get(i);

					// 判断选中列表
					if (CheckUtil.isEmpty(uuid)) {
						addLayout(userTkRaData.uuid,userTkRaData.address, userTkRaData.tel,userTkRaData.name, false);
					} else {
						addLayout(userTkRaData.uuid,userTkRaData.address, userTkRaData.tel,userTkRaData.name,uuid.equals(userTkRaData.uuid) ? true : false);
					}

				}
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("页面查询");
				// -----
				contextView.setVisibility(View.GONE);
				DialogUtil.showToast(ContextUtil.getContext(), message);
				NewTakeAwayManageAddressActivity.this.finish();
			}
			
			private void getUserTkRaListPackDTO() {
				String json = "{\"list\":[{\"uuid\":\"u111\",\"name\":\"111\",\"address\":\"117号\",\"tel\":\"1234567890\"},{\"uuid\":\"u222\",\"name\":\"222\",\"address\":\"118号\",\"tel\":\"1234566789\"},{\"uuid\":\"u333\",\"name\":\"333\",\"address\":\"119号\",\"tel\":\"123445467\"}]}";
				Gson gson = new Gson();
				UserTkRaListPack2DTO dto = gson.fromJson(json, UserTkRaListPack2DTO.class);
				onSuccess(dto);
			}
		});

	
	}

	// 添加收货地址，返回200成功
	private void excuteAddUserTakeoutReceiveAddressTask(final PopupWindow dialog ,final String address, final String tel,final String person) {

		// ----
		OpenPageDataTracer.getInstance().addEvent("新增按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.addUserTakeoutReceiveAddress2);
		request.addData("address", address);// 地址 ParamType.STRING
		request.addData("tel", tel);// 电话 ParamType.STRING
		request.addData("name", person);
		CommonTask.request(request, "正在提交...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("新增按钮");
				// -----
				dialog.dismiss();
				excuteUserTakeoutReceiveAddressListTask();
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("新增按钮");
				// -----

				DialogUtil.showToast(ContextUtil.getContext(), message);
			}
		});
	}

	// 删除收货地址，返回200成功
	private void excuteDelUserTakeoutReceiveAddressTask(String uuid) {

		
		if (uuid.equals(this.uuid)) {
			backUUID="";
		}
		
		// ----
		OpenPageDataTracer.getInstance().addEvent("删除按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.delUserTakeoutReceiveAddress);
		request.addData("uuid", uuid);
		CommonTask.request(request, "正在删除...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("删除按钮");
				// -----

				excuteUserTakeoutReceiveAddressListTask();
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("删除按钮");
				// -----

				DialogUtil.showToast(ContextUtil.getContext(), message);
			}
		});
	}

	// 修改收货地址，返回200成功
	private void excuteEditUserTakeoutReceiveAddressTask(final PopupWindow dialog ,String uuid, String address, String tel,String person) {

		// ----
		OpenPageDataTracer.getInstance().addEvent("修改按钮");
		// -----

		ServiceRequest request = new ServiceRequest(API.editUserTakeoutReceiveAddress2);
		request.addData("uuid", uuid);
		request.addData("address", address);
		request.addData("tel", tel);
		request.addData("name", person);
		CommonTask.request(request, "正在修改...", new CommonTask.TaskListener<Void>() {

			@Override
			protected void onSuccess(Void dto) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("修改按钮");
				// -----

				dialog.dismiss();
				DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "修改成功");
				excuteUserTakeoutReceiveAddressListTask();
			};

			protected void onError(int code, String message) {

				// ----
				OpenPageDataTracer.getInstance().endEvent("修改按钮");
				// -----

				DialogUtil.showToast(ContextUtil.getContext(), message);
			}
		});
	}

	private void addLayout(String uuid, String address, String phoneNumber, String personname,boolean defaultClick) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(UnitUtil.dip2px(10), UnitUtil.dip2px(20), UnitUtil.dip2px(10), UnitUtil.dip2px(0));
		LinearLayout layoutItem = (LinearLayout) addAddressItemLayout(uuid, address, phoneNumber,personname, defaultClick);
		layoutItem.setLayoutParams(layoutParams);
		contentLayout.addView(layoutItem);
	}

	private View addAddressItemLayout(final String uuid, final String address,final String phoneNumber,final String personname, boolean defaultClick) {
		final View view = LayoutInflater.from(NewTakeAwayManageAddressActivity.this).inflate(R.layout.newlist_item_take_away_address_manager, null);
		RelativeLayout contentLayout = (RelativeLayout) view.findViewById(R.id.newtake_away_manage_address_layout);
		final TextView edAddress = (TextView) view.findViewById(R.id.newtake_away_manage_address);
		final TextView edPhoneNumber = (TextView) view.findViewById(R.id.newtake_away_manage_phone);
		final TextView edorderperson=(TextView) view.findViewById(R.id.newtake_away_manage_person);
		ImageView bntSelect = (ImageView) view.findViewById(R.id.newtake_away_address_select);
		Button bntDelete = (Button) view.findViewById(R.id.newtake_away_address_delete);
		Button bntEdite = (Button) view.findViewById(R.id.newtake_away_address_edite);
		edAddress.setText(address);
		edAddress.setTag(uuid);
		edPhoneNumber.setText(phoneNumber);
		edorderperson.setText(personname);
		if (defaultClick) {
			bntSelect.setVisibility(View.VISIBLE);
			backUUID=uuid;
			backAddress=address;
			backPhoneNumber=phoneNumber;
			backpersonname=personname;
		} else {
			bntSelect.setVisibility(View.GONE);
		}
		bntDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showAlert(NewTakeAwayManageAddressActivity.this, true, "", "是否删除该地址", "确定", "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						excuteDelUserTakeoutReceiveAddressTask(uuid);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

			}
		});

		// 修改地址信息
		bntEdite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				String address = edAddress.getText().toString();
				String tel = edPhoneNumber.getText().toString();
				String uuid = edAddress.getTag().toString();
				String person=edorderperson.getText().toString();
				openAddressManagerDialog(uuid, address,tel,person);
			}
		});

		contentLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				backUUID=uuid;
				backAddress=address;
				backPhoneNumber=phoneNumber;
				backpersonname=personname;
				backActivity();
			}
		});

		return view;
	}

	private void openAddressManagerDialog(final String addressUuid, final String soAddress, final String soTel,final String soPerson) {
		DialogUtil.showDialogNew(NewTakeAwayManageAddressActivity.this, R.layout.newtake_away_operate_address_message, new DialogEventListenerNew() {

			@Override
			public void onInit(View contentView, final PopupWindow dialog) {
				final EditText etAddress = (EditText) contentView.findViewById(R.id.newtake_away_add_address);
				final EditText etPhoneNumber = (EditText) contentView.findViewById(R.id.newtake_away_add_phonenumber);
				final EditText etOrderPerson = (EditText) contentView.findViewById(R.id.newtake_away_add_person);
				Button bntCancel = (Button) contentView.findViewById(R.id.newtake_away_add_cancel);
				Button bntConfirm = (Button) contentView.findViewById(R.id.newtake_away_add_confirm);
				if (!("").equals(soAddress)) {
					etAddress.setText(soAddress);
				}
				if (!("").equals(soTel)) {
					etPhoneNumber.setText(soTel);
				}
				if (!("").equals(soPerson)) {
					etOrderPerson.setText(soPerson);
				}
				bntCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				bntConfirm.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ViewUtils.preventViewMultipleClick(v, 1000);
						String address = etAddress.getText().toString();
						String phone = etPhoneNumber.getText().toString();
						String person=etOrderPerson.getText().toString();
						if (CheckUtil.isEmpty(address)) {
							DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "送餐地址为空");
							return;
						}
						if (CheckUtil.isEmpty(phone)) {
							DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "联系人电话号码为空");
							return;
						}
						if (CheckUtil.isEmpty(person)) {
							DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "联系人为空");
							return;
						}
						if (!CheckUtil.isCellPhone(phone)) {
							DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "请输入正确的手机号码");
							return;
						}
						if (CheckUtil.isEmpty(soAddress) && CheckUtil.isEmpty(soTel)&&CheckUtil.isEmpty(soPerson)) {
							excuteAddUserTakeoutReceiveAddressTask(dialog,address, phone,person);
						} else {
							excuteEditUserTakeoutReceiveAddressTask(dialog,addressUuid, address, phone,person);
						}
					}
				});
			}
		});
	}

	private void backActivity() {
		if (CheckUtil.isEmpty(backAddress)) {
			DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "送餐地址为空");
			return;
		}
		if (CheckUtil.isEmpty(backPhoneNumber)) {
			DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "联系人电话号码为空");
			return;
		}
		if (CheckUtil.isEmpty(backpersonname)) {
			DialogUtil.showToast(NewTakeAwayManageAddressActivity.this, "联系人为空");
			return;
		}
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Settings.UUID, backUUID);
		bundle.putString(Settings.BUNDLE_TAKEAWAY_ADDRESS, backAddress);
		bundle.putString(Settings.BUNDLE_TAKEAWAY_TEL, backPhoneNumber);
		bundle.putString(Settings.BUNDLE_TAKEAWAY_NAME, backpersonname);
		intent.putExtras(bundle);
		NewTakeAwayManageAddressActivity.this.setResult(200, intent);
		NewTakeAwayManageAddressActivity.this.finish();
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK) {
			backActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
