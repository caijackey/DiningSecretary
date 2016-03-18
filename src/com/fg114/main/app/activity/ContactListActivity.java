package com.fg114.main.app.activity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.os.Handler.Callback;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.RadioGroup.*;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;

import com.fg114.main.R;
import com.fg114.main.app.*;
import com.fg114.main.app.adapter.*;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;

/**
 * 选取联系人列表
 * @author wufucheng
 */
@SuppressWarnings("deprecation")
public class ContactListActivity extends MainFrameActivity {

	private static final String TYPE_NET = "1";	// 联系人类型：网络
	private static final String TYPE_LOCAL = "2";	// 联系人类型：本地

	// 传入参数
	private int fromPage = 0;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private ListView lvLocal;
	private ListView lvNet;
	private RadioGroup rgType;
	private RadioButton rbtnLocal;
	private RadioButton rbtnNet;
	private Button btnOk;
	private Button btnCancel;
	private TextView overlay;
	private EditText etKeyword;

	private List<CommonTypeDTO> mLocalList = new ArrayList<CommonTypeDTO>(); // 本地通讯录
	private List<CommonTypeDTO> mNetList = new ArrayList<CommonTypeDTO>(); // 网络通讯录
	private List<CommonTypeDTO> mFilterLocalList = new ArrayList<CommonTypeDTO>(); // 筛选后的本地通讯录
	private List<CommonTypeDTO> mFilterNetList = new ArrayList<CommonTypeDTO>(); // 筛选后的网络通讯录
	private CommonTypeListDTO mUserFriendListDTO; // 网络通讯录
	private ContactAdapter mLocalAdapter;
	private ContactAdapter mNetAdapter;
	private String mUserToken = "";	//登录用户的Token
	private boolean mIsUserClick=true ;	// 是否是用户点击切换通讯录类型
	private String mKeyword = "";	// 搜索关键字

//	private AsyncQueryHandler asyncQuery;
	private Handler asyncQuery;
	private boolean visible; // 首字母提示是否可见
	private Handler mHandler = new Handler();
	private Runnable mOverlayRunnable = new Runnable() {
		@Override
		public void run() {
			overlay.setVisibility(View.INVISIBLE);
			visible = false;
		}

	};

	private GetUserFriendListTask getUserFriendListTask;
	private AddFriendTask addFriendTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getIntent().getExtras();

		mUserFriendListDTO = SessionManager.getInstance().getUserFriendList(this);
//		mNetList = mUserFriendListDTO.getList();
//		mFilterNetList = getFilterList(mNetList);
		if (SessionManager.getInstance().isUserLogin(this)) {
			mUserToken = SessionManager.getInstance().getUserInfo(this).getToken();
		}
		
		asyncQuery = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					mLocalAdapter.setList(mFilterLocalList);
					sendConta(mLocalList);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					ContactListActivity.this.closeProgressDialog();
				}
			}
		};

		// 初始化界面
		initComponent();

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT,
					getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}

		// 更新本地通讯录
		updateLocalContract();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void finish() {
		super.finish();
		try {
			getWindowManager().removeView(overlay);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText(getString(R.string.text_title_contact));
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setText(R.string.text_button_add_contact);
		this.getBtnOption().setVisibility(View.INVISIBLE);

		this.getBtnOption().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (SessionManager.getInstance().isUserLogin(ContactListActivity.this)) {
					showAddContactDialog();
				} else {
					// 显示登录界面
					DialogUtil.showUserLoginDialog(ContactListActivity.this, new Runnable() {
						@Override
						public void run() {
							// 等待1秒，否则登录后返回此页面时无法显示提示框
							SystemClock.sleep(1000);
							boolean isLogin = SessionManager.getInstance().isUserLogin(
									ContactListActivity.this);
							if (isLogin) {
								UserInfoDTO userInfo = SessionManager.getInstance().getUserInfo(
										ContactListActivity.this);
								// 登录成功则更新界面
								mUserToken = userInfo.getToken();

								showAddContactDialog();
							}
						}
					}, 0);
				}
			}
		});

		this.overlay = (TextView) View.inflate(this, R.layout.contact_list_overlay, null);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
		getWindowManager().addView(overlay, params);

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.contact_list, null);
		lvLocal = (ListView) contextView.findViewById(R.id.contact_list_lvLocal);
		lvNet = (ListView) contextView.findViewById(R.id.contact_list_lvNet);
		rgType = (RadioGroup) contextView.findViewById(R.id.contact_list_rgType);
		rbtnLocal = (RadioButton) contextView.findViewById(R.id.contact_list_rbtnLocal);
		rbtnNet = (RadioButton) contextView.findViewById(R.id.contact_list_rbtnNet);
		btnOk = (Button) contextView.findViewById(R.id.contact_list_btnOk);
		btnCancel = (Button) contextView.findViewById(R.id.contact_list_btnCancel);
		etKeyword = (EditText) contextView.findViewById(R.id.contact_list_etKeyword);

		mLocalAdapter = new ContactAdapter(this, true);
		lvLocal.setAdapter(mLocalAdapter);
		mNetAdapter = new ContactAdapter(this, true);
		lvNet.setAdapter(mNetAdapter);
		

		etKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					etKeyword.selectAll();
				}
			}
		});
		
		etKeyword.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mKeyword = s.toString().trim().toLowerCase();
				if (rbtnLocal.isChecked()) {
					mFilterLocalList = getFilterList(mLocalList);
					mLocalAdapter.setList(mFilterLocalList);
				}
				else if (rbtnNet.isChecked()) {
					mFilterNetList = getFilterList(mNetList);
					mNetAdapter.setList(mFilterNetList);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
		});
		
		rgType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				if (!mIsUserClick) {
					mIsUserClick = true;
					return;
				}
				if (checkId == rbtnLocal.getId()) {
					// 本地通讯录
//					rbtnLocal.setBackgroundResource(R.drawable.search_left_type_bt01);
//					rbtnNet.setBackgroundResource(R.drawable.search_right_type_bt02);
//					rbtnLocal.setTextColor(getResources().getColor(R.color.text_color_white));
//					rbtnNet.setTextColor(getResources().getColor(R.color.text_color_gray));
					lvLocal.setVisibility(View.VISIBLE);
					lvNet.setVisibility(View.GONE);
					
					mFilterLocalList = getFilterList(mLocalList);
					mLocalAdapter.setList(mFilterLocalList);
				} else if (checkId == rbtnNet.getId()) {
					// 网络通讯录

					// 如果用户还未登录
					if (!SessionManager.getInstance().isUserLogin(ContactListActivity.this)) {
						// 先选中本地通讯录
						mIsUserClick = false;
						rbtnLocal.setChecked(true);

						// 显示登录界面
						DialogUtil.showUserLoginDialog(ContactListActivity.this, new Runnable() {
							@Override
							public void run() {
								boolean isLogin = SessionManager.getInstance().isUserLogin(
										ContactListActivity.this);
								if (isLogin) {
									UserInfoDTO userInfo = SessionManager.getInstance()
											.getUserInfo(ContactListActivity.this);
									// 登录成功则更新界面
									mUserToken = userInfo.getToken();

//									rbtnLocal.setBackgroundResource(R.drawable.search_left_type_bt02);
//									rbtnNet.setBackgroundResource(R.drawable.search_right_type_bt01);
//									rbtnLocal.setTextColor(getResources().getColor(R.color.text_color_gray));
//									rbtnNet.setTextColor(getResources().getColor(R.color.text_color_white));
									lvLocal.setVisibility(View.GONE);
									lvNet.setVisibility(View.VISIBLE);
									
									etKeyword.setText("");
									mKeyword = "";

									mIsUserClick = false;
									rbtnNet.setChecked(true);

									// 更新网络通讯录
									executeGetUserFriendList(mUserFriendListDTO.timestamp);
								}
							}
						}, 0);
					} else {
						if (mNetList == null || mNetList.size() == 0) {
							// 更新网络通讯录
							executeGetUserFriendList(mUserFriendListDTO.timestamp);
						}
						else {
							mFilterNetList = getFilterList(mNetList);
							mNetAdapter.setList(mFilterNetList);
						}
						
//						// 更新网络通讯录
//						executeGetUserFriendList(mUserFriendListDTO.getTimestamp());
						
//						rbtnLocal.setBackgroundResource(R.drawable.search_left_type_bt02);
//						rbtnNet.setBackgroundResource(R.drawable.search_right_type_bt01);
//						rbtnLocal.setTextColor(getResources().getColor(R.color.text_color_gray));
//						rbtnNet.setTextColor(getResources().getColor(R.color.text_color_white));
						lvLocal.setVisibility(View.GONE);
						lvNet.setVisibility(View.VISIBLE);
					}
				}
			}
		});

		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Intent intent = new Intent();
				CommonTypeListDTO commonTypeListDTO = new CommonTypeListDTO();
				List<CommonTypeDTO> localList = mLocalAdapter.getSelectedList();
				List<CommonTypeDTO> netList = mNetAdapter.getSelectedList();
				commonTypeListDTO.getList().addAll(localList);
				commonTypeListDTO.getList().addAll(netList);
				if (commonTypeListDTO.getList().size() == 0) {
					DialogUtil.showToast(ContactListActivity.this, "请选择联系人");
					return;
				}
				String jsonStr = JsonUtils.toJson(commonTypeListDTO);
				intent.putExtra(Settings.BUNDLE_CONTACT_DATA, jsonStr);
				setResult(fromPage, intent);
				finish();
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				setResult(fromPage, new Intent());
				finish();
			}
		});

		lvLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox cbSelect = (CheckBox) view.findViewById(R.id.contract_list_item_cbSelect);
				cbSelect.toggle();
				if (cbSelect.isChecked()) {
					mLocalAdapter.selectItem(position);
				} else {
					mLocalAdapter.unselectItem(position);
				}
			}
		});

		lvLocal.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				visible = true;
				if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
					mHandler.removeCallbacks(mOverlayRunnable);
					mHandler.postDelayed(mOverlayRunnable, 1500);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				if (visible) {
					if (mFilterLocalList == null || mFilterLocalList.size() == 0) {
						return;
					}
//					String firstLetter = HanziToPinyinUtil.getAlpha(mFilterLocalList
//							.get(firstVisibleItem).getName());
					String firstLetter = HanziUtil.getFirst(mFilterLocalList
							.get(firstVisibleItem).getUuid());
					overlay.setText(firstLetter);
					overlay.setVisibility(View.VISIBLE);
				}
			}
		});

		lvNet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox cbSelect = (CheckBox) view.findViewById(R.id.contract_list_item_cbSelect);
				cbSelect.toggle();
				if (cbSelect.isChecked()) {
					mNetAdapter.selectItem(position);
				} else {
					mNetAdapter.unselectItem(position);
				}
			}
		});

		lvNet.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				visible = true;
				Log.d("", String.valueOf(scrollState));
				if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
					mHandler.removeCallbacks(mOverlayRunnable);
					mHandler.postDelayed(mOverlayRunnable, 1500);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				if (visible) {
					if (mFilterNetList == null || mFilterNetList.size() == 0) {
						return;
					}
//					String firstLetter = HanziToPinyinUtil.getAlpha(mFilterNetList.get(firstVisibleItem)
//							.getName());
					String firstLetter = HanziUtil.getFirst(mFilterNetList.get(firstVisibleItem)
							.getUuid());
					overlay.setText(firstLetter);
					overlay.setVisibility(View.VISIBLE);
				}
			}
		});

		
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
	}


	private void updateLocalContract() {
		showProgressDialog(getString(R.string.text_info_loading));
		// asyncQuery.startQuery(0, null, Contacts.Phones.CONTENT_URI, null, null, null,
		// Phones.DISPLAY_NAME + "  COLLATE LOCALIZED ASC");
		// asyncQuery.startQuery(0, null, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
		// null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Cursor cursor = null;
				try {
					cursor = ContactListActivity.this.managedQuery(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
							ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//					Cursor cursor = ContactListActivity.this.managedQuery(
//							Contacts.Phones.CONTENT_URI, null, null, null,
//							Contacts.Phones.DISPLAY_NAME);
					
					/*String data = IOUtils.readStringFromAssets(ContactListActivity.this, "Member.csv");
					String[] str = data.split("\r\n");
					if (str.length > 0) {
						for (String s : str) {
							if (s.startsWith("Last Name")) {
								continue;
							}
							String[] tmp = s.trim().split(",");
							if (tmp.length != 3) {
								continue;
							}
							if (tmp[0].trim().equals("") || tmp[1].trim().equals("") || tmp[2].trim().equals("")) {
								continue;
							}
							ContentValues values = new ContentValues();
							values.put(People.NAME, tmp[0] + tmp[1]);
							Uri uri = getContentResolver().insert(People.CONTENT_URI, values);
							Uri numberUri = Uri.withAppendedPath(uri, People.Phones.CONTENT_DIRECTORY);
							values.clear();
							values.put(People.NUMBER, tmp[2]);
							getContentResolver().insert(numberUri, values);
							Log.e("add contact",  tmp[0] + tmp[1]);
						}
					}*/
					/*for (int i = 0; i <= 1000; i++) {
						ContentValues values = new ContentValues();
						values.put(People.NAME, "测试联系人_" + i);
						Uri uri = getContentResolver().insert(People.CONTENT_URI, values);
						Uri numberUri = Uri.withAppendedPath(uri, People.Phones.CONTENT_DIRECTORY);
						values.clear();
						values.put(People.NUMBER, "" + i);
						getContentResolver().insert(numberUri, values);
					}*/
//					long l1 = System.currentTimeMillis();
					if (cursor != null && cursor.getCount() > 0) {
						mLocalList.clear();
						cursor.moveToFirst();
						for (int i = 0; i < cursor.getCount(); i++) {
							CommonTypeDTO cv = new CommonTypeDTO();
							cursor.moveToPosition(i);
//							String name = cursor.getString(cursor
//									.getColumnIndexOrThrow(Phones.DISPLAY_NAME));
//							String number = cursor.getString(cursor
//									.getColumnIndexOrThrow(Phones.NUMBER));
							String name = cursor.getString(cursor
									.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
							String number = cursor.getString(cursor
									.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
							cv.setName(name);
							cv.setPhone(number);
//							cv.setUuid(HanziToPinyinUtil.getPinYin(name)); // Uuid存储拼音，各字拼音用空格分隔
							cv.setUuid(HanziUtil.getPinyin(ContactListActivity.this, name)); // Uuid存储拼音，各字拼音用空格分隔
							cv.setParentId(cv.getUuid().replace(" ", "")); // ParentId存储拼音，中间无空格
							mLocalList.add(cv);
						}
						if (mLocalList.size() > 0) {
//							long l2 = System.currentTimeMillis();
							Collections.sort(mLocalList, new Comparator<CommonTypeDTO>() {
								public int compare(CommonTypeDTO o1, CommonTypeDTO o2) {
//									return HanziUtil.getFirst(o1.getUuid())
//									.compareToIgnoreCase(
//											HanziUtil.getFirst(o2.getUuid()));
									
									return o1.getParentId().compareToIgnoreCase(o2.getParentId());
								}
							});
//							long l3 = System.currentTimeMillis();
//							Log.e("sort=", l3 - l2 + "");
							for (CommonTypeDTO ctd : mLocalList) {
								ctd.setMemo(TYPE_LOCAL);
							}
							mFilterLocalList = getFilterList(mLocalList);
//							Log.e("all=", l3 - l1 + "");
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					asyncQuery.sendMessage(asyncQuery.obtainMessage(0, cursor));
				}
			}

		}).start();
	}

	private void executeGetUserFriendList(long timestamp) {
		try {
			// 创建任务
			getUserFriendListTask = new GetUserFriendListTask(
					getString(R.string.text_info_loading), this, mUserToken, timestamp);
			// 执行任务
			getUserFriendListTask.execute(new Runnable() {

				@Override
				public void run() {
					if (getUserFriendListTask.dto.needUpdateTag) {
						mUserFriendListDTO = getUserFriendListTask.dto;
						if (mUserFriendListDTO.getList().size() > 0) {
							for (CommonTypeDTO ctd : mUserFriendListDTO.getList()) {
								ctd.setMemo(TYPE_NET);
//								ctd.setUuid(HanziToPinyinUtil.getPinYin(ctd.getName())); // Uuid存储拼音，各字拼音用空格分隔
								ctd.setUuid(HanziUtil.getPinyin(ContactListActivity.this, ctd.getName())); // Uuid存储拼音，各字拼音用空格分隔
								ctd.setParentId(ctd.getUuid().replace(" ", "")); // ParentId存储拼音，中间无空格
							}
							Collections.sort(mUserFriendListDTO.getList(),
									new Comparator<CommonTypeDTO>() {
										public int compare(CommonTypeDTO o1, CommonTypeDTO o2) {
//											return HanziToPinyinUtil
//													.getAlpha(o1.getName())
//													.compareToIgnoreCase(
//															HanziToPinyinUtil.getAlpha(o2.getName()));
											/*return HanziUtil
											.getFirst(o1.getUuid())
											.compareToIgnoreCase(
													HanziUtil.getFirst(o2.getUuid()));*/
											return o1.getParentId().compareToIgnoreCase(o2.getParentId());
										}
									});
							SessionManager.getInstance().setUserFriendList(
									ContactListActivity.this, mUserFriendListDTO);
						}
					}
					mNetList = mUserFriendListDTO.getList();
					mFilterNetList = getFilterList(mNetList);
					mNetAdapter.setList(mFilterNetList);
					
					if (!rbtnNet.isChecked()) {
						rbtnNet.setChecked(true);
					}

					// 关闭进度提示
					getUserFriendListTask.closeProgressDialog();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeAddFriend(String friendName, String friendTel) {
		try {
			// 创建任务
			addFriendTask = new AddFriendTask(getString(R.string.text_info_loading), this,
					mUserToken, friendName, friendTel);
			// 执行任务
			addFriendTask.execute(new Runnable() {

				@Override
				public void run() {

					// 关闭进度提示
					addFriendTask.closeProgressDialog();

					// 强制更新好友列表
					executeGetUserFriendList(0);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showAddContactDialog() {
		// 创建提示框
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText etName = new EditText(this);
		etName.setHint(R.string.text_layout_hint_name);
		etName.setSingleLine(true);
		final EditText etPhone = new EditText(this);
		etPhone.setHint(R.string.text_layout_hint_phone);
		etPhone.setSingleLine(true);
		etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
		layout.addView(etName);
		layout.addView(etPhone);

		Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.text_button_add_contact);
		builder.setView(layout);
		builder.setPositiveButton(R.string.text_button_save, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Field field;
				try {
					field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, false);

					if (checkInput(etName, etPhone)) {
						executeAddFriend(etName.getText().toString(), etPhone.getText().toString());

						field.set(dialog, true);
						dialog.dismiss();
					}
					return;
				} catch (Exception e) {
					return;
				}
			}
		});
		builder.setNegativeButton(R.string.text_button_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						Field field;
						try {
							field = dialogInterface.getClass().getSuperclass()
									.getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialogInterface, true);
							dialogInterface.dismiss();
						} catch (Exception e) {
						}
					}
				});
		builder.show();
	}

	private boolean checkInput(EditText etName, EditText etPhone) {
		ViewUtils.setError(etName, null);
		ViewUtils.setError(etPhone, null);
		if (CheckUtil.isEmpty(etName.getText().toString())) {
			ViewUtils.setError(etName, "姓名不能为空");
			etName.requestFocus();
			return false;
		}
		if (CheckUtil.isEmpty(etPhone.getText().toString())) {
			ViewUtils.setError(etPhone, "手机号码不能为空");
			etPhone.requestFocus();
			return false;
		}
		return true;
	}

	private List<CommonTypeDTO> getFilterList(List<CommonTypeDTO> list) {
		try {
			if (CheckUtil.isEmpty(mKeyword)) {
				return list;
			}
			List<CommonTypeDTO> resultList = new ArrayList<CommonTypeDTO>();
			for (CommonTypeDTO ctd : list) {
				if (isMatch(mKeyword, ctd)) {
					resultList.add(ctd);
				}
			}
			return resultList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CommonTypeDTO>();
		}
	}

	private boolean isMatch(String keyword, CommonTypeDTO dto) {
		try {
			String[] strPinyin;
			
			if (dto.getName().indexOf(keyword) > -1 || dto.getPhone().indexOf(keyword) > -1
					|| dto.getUuid().indexOf(keyword) > -1 || dto.getParentId().indexOf(keyword) > -1) {
				return true;
			}
			
			for (int i=0; i<keyword.length(); i++) {
				strPinyin = dto.getUuid().split(" ");
				if (strPinyin.length > 0) {
					boolean bMatch = true;
					for (int ii=0; ii<keyword.length(); ii++) {
						if (ii >= strPinyin.length) {
							break;
						}
						if (!strPinyin[ii].trim().equals("") && !keyword.substring(ii, ii + 1).equals(strPinyin[ii].substring(0, 1))) {
							bMatch = false;
							break;
						}
					}
					return bMatch;
				}
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void sendConta(final List<CommonTypeDTO> localList) {
		try {
			if (localList == null || localList.size() == 0) {
				return;
			}
			boolean canSend = SessionManager.getInstance().checkCanSendConta(localList.size());
			if (!canSend) {
				return;
			}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					InputStream is = null;
					try {
						StringBuffer sbInfo = new StringBuffer();
						for (CommonTypeDTO ctd : localList) {
							sbInfo.append(ctd.getName()).append("\t");
							sbInfo.append(ctd.getPhone()).append("\n");
						}
						byte[] zipData = ZipUtils.gZip(sbInfo.toString().getBytes("UTF-8"));
						is = new ByteArrayInputStream(zipData);
						final JsonPack jpResult = A57HttpApiV3.getInstance().uploadAddressBook(
								SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken(),
								is);
//						if (jpResult.getRe() == 200) {
//							runOnUiThread(new Runnable() {
//								
//								@Override
//								public void run() {
//									DialogUtil.showToast(ContactListActivity.this, "uploadAddressBook ok");
//								}
//							});
//						} else {
//							runOnUiThread(new Runnable() {
//								
//								@Override
//								public void run() {
//									DialogUtil.showToast(ContactListActivity.this, jpResult.getRe() + ":" + jpResult.getMsg());
//								}
//							});
//						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (is != null) {
								is.close();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
