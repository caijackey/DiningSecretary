package com.fg114.main.app.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.text.*;
import android.text.method.BaseKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantUploadActivity;
import com.fg114.main.app.view.PredicateLayout;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.task.SendSMSInvitationTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.service.task.*;

import static com.fg114.main.util.UnitUtil.*;

public class SendSMSActivity extends MainFrameActivity {

	// 传入参数
	private int fromPage = 0;

	// 界面组件
	private LayoutInflater mInflater;
	private View contextView;
	private PredicateLayout contactList;
	private TableLayout recentPersonList;
	private ScrollView scrollViewOfContactList;
	private ImageButton addPerson;
	private ImageButton sendSms;
	private EditText content;
	private TextView wordCounter;
	private EditText inputPhone;
	private String smsDetail; // 短信模板

	// 定义联系人列表的最大高度,dip
	private int maxHeightDipOfContactList = 83;
	// 短信邀请的订单号，餐厅id，经纬度
	private String orderId = "";
	private String restId = "";
	private boolean havePlaceGpsTag; //是否有参照地标gps
	private String placeLon; //地标经度  可以为空
	private String placeLat; //地标纬度 可以为空
	private String placeName; //地标名称
	private String templetId; //模板id
	
	
	// 短信输入的最大字数
	private int maxLengthOfMessage = 130;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// flag = EDITTEXTFLAG;
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		orderId = bundle.getString(Settings.BUNDLE_ORDER_ID);
		restId =  bundle.getString(Settings.BUNDLE_REST_ID);
		havePlaceGpsTag=bundle.getBoolean(Settings.BUNDLE_SMS_HavePlaceGpsTag,false); //是否有参照地标gps
		placeLon=bundle.getString(Settings.BUNDLE_SMS_PlaceLon); //地标经度 
		placeLat=bundle.getString(Settings.BUNDLE_SMS_PlaceLat); //地标纬度
		placeName=bundle.getString(Settings.BUNDLE_SMS_PlaceName); //地标名称
		templetId=bundle.getString(Settings.BUNDLE_SMS_TempletId); //模板id 
		smsDetail = bundle.getString(Settings.BUNDLE_SMS_DETAIL); // 短信模板

		// 初始化界面
		initComponent();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText(getString(R.string.text_title_sms_invite));
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.setFunctionLayoutGone();

		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.send_sms, null);

		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		//
		scrollViewOfContactList = (ScrollView) contextView.findViewById(R.id.sms_invite_scroll_container_of_contact_list);
		contactList = (PredicateLayout) contextView.findViewById(R.id.sms_invite_contract_list);
		addPerson = (ImageButton) contextView.findViewById(R.id.sms_invite_add_person_button);
		sendSms = (ImageButton) contextView.findViewById(R.id.sms_invite_send_sms_button);
		content = (EditText) contextView.findViewById(R.id.sms_invite_content);
		wordCounter = (TextView) contextView.findViewById(R.id.sms_invite_word_counter);
		recentPersonList = (TableLayout) contextView.findViewById(R.id.sms_invite_recent_person);
		// 添加输入电话号码的文本框
		addInputPhoneText();

		// 点击调用选择联系人页面
		addPerson.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				Bundle bundle = new Bundle();
				ActivityUtil.jump(SendSMSActivity.this, ContactListActivity.class, 0, bundle);
			}
		});

		// 点击发送短信
		sendSms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// Log.i("in ===",""+i+"listH="+contactList.getHeight()+"==sC="+scrollViewOfContactList.getHeight());
				sendSMS();
			}
		});
		// 输入短信字数计数
		content.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				wordCounter.setText(content.getText().length() + "");
				if (content.getText().length() > maxLengthOfMessage) {
					// content.setTextColor(getResources().getColor(R.color.text_color_red));
					wordCounter.setTextColor(getResources().getColor(R.color.text_color_red));
					// Log.e("CCC0","Red");
				} else {
					// content.setTextColor(getResources().getColor(R.color.text_color_gray));
					wordCounter.setTextColor(getResources().getColor(R.color.text_color_gray));
					// Log.e("CCC0","Gray");
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		// 短信模板
		content.setText(smsDetail == null ? "" : smsDetail);

		// 从缓存读取最近联系人，填入控件
		fillRecentPersonList();
		/*
		 * CommonTypeDTO tempPerson = new CommonTypeDTO();
		 * tempPerson.setName("张鑫鑫"); tempPerson.setPhone("123456789");
		 * tempPerson.setMemo("1"); addRecentPerson(tempPerson); tempPerson =
		 * new CommonTypeDTO(); tempPerson.setName("2临时人员是我吗");
		 * tempPerson.setPhone("115522213"); tempPerson.setMemo("1");
		 * addRecentPerson(tempPerson); tempPerson = new CommonTypeDTO();
		 * tempPerson.setName("3临时人员"); tempPerson.setPhone("55668888");
		 * tempPerson.setMemo("1"); addRecentPerson(tempPerson); tempPerson =
		 * new CommonTypeDTO(); tempPerson.setName("上官无极");
		 * tempPerson.setPhone("33556666"); tempPerson.setMemo("1");
		 * addRecentPerson(tempPerson);
		 */
	}

	// 在联系人列表里添加一个输入手机号码的文本框
	private void addInputPhoneText() {

		inputPhone = new EditText(this);
		InputFilter[] filters = { new InputFilter.LengthFilter(16) };
		inputPhone.setWidth(dip2px(155));
		inputPhone.setHeight(dip2px(22));
		inputPhone.setSingleLine(true);
		inputPhone.setFilters(filters);
		inputPhone.setGravity(Gravity.LEFT);
		inputPhone.setHint("请输入联系人");
		inputPhone.setPadding(dip2px(3), dip2px(3), dip2px(3), dip2px(3));
		inputPhone.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		// inputPhone.setTextSize(dip2px(16));
		inputPhone.setImeOptions(EditorInfo.IME_ACTION_NONE);
		inputPhone.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		// inputPhone.setBackgroundResource(R.color.background_color_gray);
		inputPhone.setBackgroundColor(0x00000000);
		//
		// InputMethodManager inputMethodManager =
		// (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		// inputMethodManager.hideSoftInputFromWindow(inputPhone.getWindowToken(),
		// InputMethodManager.RESULT_UNCHANGED_SHOWN);
		// inputPhone.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
		inputPhone.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					personInputComplete(inputPhone.getText().toString());
					return true;
				}
				return false;
			}

		});

		/*
		 * inputPhone.setOnLongClickListener(new View.OnLongClickListener() {
		 * 
		 * @Override public boolean onLongClick(View v) { // TODO Auto-generated
		 * method stub return true; } });
		 */

		// 控制只能输入数字，删除键。输入其他键（例如回车）时，表示输入结束，自动添加一个联系人
		inputPhone.addTextChangedListener(new TextWatcher() {

			private String oldValue = "";

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				if (Pattern.matches("\\d*", s.toString())) {
					oldValue = s.toString();
				}

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!Pattern.matches("\\d*", s.toString())) {
					Log.e("afterTextChanged", s.toString());
					// 新增联系人
					personInputComplete(oldValue);
				}
			}
		});

		/*
		 * inputPhone.setKeyListener(new BaseKeyListener() {
		 * 
		 * @Override public boolean onKeyDown(View view, Editable content, int
		 * keyCode, KeyEvent event) { System.out.println("+++> "+keyCode); if
		 * (!( keyCode>=KeyEvent.KEYCODE_0 && keyCode<=KeyEvent.KEYCODE_9 ||
		 * keyCode==KeyEvent.KEYCODE_DEL)) { CommonTypeDTO person = new
		 * CommonTypeDTO(); person.setName(inputPhone.getText().toString());
		 * person.setPhone(inputPhone.getText().toString());
		 * person.setMemo("1"); addPerson(person); inputPhone.setText("");
		 * return true; }
		 * 
		 * return super.onKeyDown(view, content, keyCode, event); }
		 * 
		 * @Override public int getInputType() {
		 * 
		 * return InputType.TYPE_CLASS_TEXT; }
		 * 
		 * });
		 */
		/*
		 * inputPhone.setKeyListener(new KeyListener() {
		 * 
		 * @Override public boolean onKeyUp(View view, Editable text, int
		 * keyCode, KeyEvent event) { if (!(keyCode
		 * >=KeyEvent.KEYCODE_0&&keyCode <=KeyEvent.KEYCODE_9)) { CommonTypeDTO
		 * person = new CommonTypeDTO();
		 * person.setName(inputPhone.getText().toString());
		 * person.setPhone(inputPhone.getText().toString());
		 * person.setMemo("1"); addPerson(person); inputPhone.setText("");
		 * return true; } return false; }
		 * 
		 * @Override public boolean onKeyOther(View view, Editable text,
		 * KeyEvent event) { // TODO Auto-generated method stub return false; }
		 * 
		 * @Override public boolean onKeyDown(View view, Editable text, int
		 * keyCode, KeyEvent event) { // TODO Auto-generated method stub return
		 * false; }
		 * 
		 * @Override public int getInputType() { return
		 * android.text.InputType.TYPE_CLASS_NUMBER; }
		 * 
		 * @Override public void clearMetaKeyState(View view, Editable content,
		 * int states) { // TODO Auto-generated method stub
		 * 
		 * } });
		 */

		contactList.addView(inputPhone, new PredicateLayout.LayoutParams(dip2px(4), dip2px(4)));

	}

	// 输入联系人完成
	private void personInputComplete(String personPhone) {
		CommonTypeDTO person = new CommonTypeDTO();
		person.setName(personPhone);
		person.setPhone(personPhone);
		person.setMemo("1");
		addPerson(person);
		// 开新线程置空，防止HTC G7程序崩溃的bug
		inputPhone.post(new Runnable() {
			@Override
			public void run() {
				inputPhone.setText("");

			}
		});
	}

	// 从缓存读取最近联系人，填入控件
	private void fillRecentPersonList() {
		List<CommonTypeDTO> list = SessionManager.getInstance().getListManager().getRecentPersonList(this);

		for (int i = 0; list != null && i < list.size(); i++) {
			addRecentPerson(list.get(i));
		}
	}

	// 添加一个最近联系人
	private void addRecentPerson(final CommonTypeDTO person) {
		if (person == null) {
			return;
		}

		final TextView tv = new TextView(this);
		// 名字为空，则显示手机号码
		String name = person.getName();
		if (name == null || name.trim().equals("")) {
			name = person.getPhone();
		}
		tv.setSingleLine(true);
		tv.setText(name);
		tv.setWidth(dip2px(103));
		tv.setHeight(dip2px(30));
		tv.setEllipsize(TextUtils.TruncateAt.END);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		tv.setGravity(Gravity.CENTER);
		tv.setPadding(dip2px(3), dip2px(4), dip2px(3), dip2px(4));
		tv.setTag(person);
		// tv.setBackgroundResource(R.color.background_color_gray);
		tv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				addPerson(person);
				tv.requestFocus();
			}
		});

		// 得到最后一行
		ViewGroup tr = null;
		if (recentPersonList.getChildCount() > 0) {

			tr = (ViewGroup) recentPersonList.getChildAt(recentPersonList.getChildCount() - 1);
		}
		// 是否另起一行
		if (tr == null || (tr.getChildCount() > 0 && tr.getChildCount() % 5 == 0)) {
			// 另起一行
			tr = new TableRow(this);
			recentPersonList.addView(tr);
		}

		TableLayout.LayoutParams lp = (TableLayout.LayoutParams) tr.getLayoutParams();
		lp.gravity = Gravity.CENTER_VERTICAL;
		tr.setLayoutParams(lp);

		tr.addView(tv);
		// 添加一个竖线分隔，每行３个，最后一个不添加竖线
		if (tr.getChildCount() % 5 != 0) {

			TextView line = new TextView(this);
			line.setSingleLine(true);
			line.setWidth(dip2px(1));
			line.setHeight(dip2px(20));
			line.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			line.setPadding(0, 0, 0, 3);

			line.setBackgroundColor(getResources().getColor(R.color.border_color_light_gray));
			tr.addView(line);
		}

	}

	// 发送短信
	protected void sendSMS() {

		if (!validate()) {
			return;
		}
		// Log.i("SMS content","send to --> "+getAllPerson());

		// 向输入框中发送一个回车
		inputPhone.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
		// 发送任务
		final SendSMSInvitationTask task = new SendSMSInvitationTask("正在发送短信请柬...", 
				getAllPerson(), 
				content.getText().toString(), 
				orderId,
				restId,
				havePlaceGpsTag,
				placeLon,
				placeLat,
				placeName,
				templetId,
				SessionManager.getInstance().getCityInfo(this).getId(),
				this);

		task.execute(new Runnable() {
			@Override
			public void run() {

				// 成功发送
				task.closeProgressDialog();
				//根据是否有订单id，显示不同的提示信息
				DialogUtil.showToast(SendSMSActivity.this, CheckUtil.isEmpty(orderId)?"短信请柬已提交，稍后将由系统自动发送":"短信请柬已提交，预订成功后将由系统自动发送");

				/*
				 * //成功后跳转到订单详情页面 Bundle data=new Bundle();
				 * data.putString(Settings.BUNDLE_KEY_ID,orderId);
				 * data.putInt(Settings
				 * .BUNDLE_KEY_FROM_PAGE,Settings.SEND_SMS_ACTIVITY);
				 * ActivityUtil.jump(SendSMSActivity.this,
				 * MyOrderDetailActivity.class, Settings.SEND_SMS_ACTIVITY,
				 * data);
				 */
				// 成功后结束当前页面
				//设置返回页为最初来源页面
				setResult(Settings.sendShortMessageOrignalActivityId);
				Settings.sendShortMessageOrignalActivityId=0;
				finish();
			}
		},new Runnable() {
			
			@Override
			public void run() {
					
			}
		});
		// 更新最近联系人缓存
		updateRecentPersonsCache();

	}

	// 更新最近联系人缓存
	private void updateRecentPersonsCache() {
		// 最多６个人
		int max = 6;
		List<CommonTypeDTO> oldList = SessionManager.getInstance().getListManager().getRecentPersonList(this);
		List<CommonTypeDTO> newList = new ArrayList<CommonTypeDTO>();

		HashMap<String, Object> hashPhones = new HashMap<String, Object>(); // 用来记录已添加的电话号码，以防止重复数据

		int count = contactList.getChildCount();
		int i;
		for (i = 0; i < count && newList.size() < max; i++) {
			CommonTypeDTO person = (CommonTypeDTO) contactList.getChildAt(i).getTag();
			if (person != null && !hashPhones.containsKey(person.getPhone())) {
				newList.add(person);
				hashPhones.put(person.getPhone(), null);
			}
		}
		// 新列表中小于６个人时，将前面的缓存中较新的也加进来
		count = oldList.size();
		for (i = 0; i < count && newList.size() < max; i++) {
			CommonTypeDTO person = oldList.get(i);
			if (person != null && !hashPhones.containsKey(person.getPhone())) {
				newList.add(person);
				hashPhones.put(person.getPhone(), null);
			}
		}
		SessionManager.getInstance().getListManager().setRecentPersonList(this, newList);

	}

	// 验证
	private boolean validate() {

		if (contactList.getChildCount() <= 1 && CheckUtil.isEmpty(inputPhone.getText().toString())) {
			DialogUtil.showToast(this, "请添加联系人！");
			return false;
		}
		//
		if (content.getText().length() == 0) {
			DialogUtil.showToast(this, "请输入短信内容！");
			return false;
		}
		//
		if (content.getText().length() > maxLengthOfMessage) {
			DialogUtil.showToast(this, "短信内容不能超过 " + maxLengthOfMessage + " 个字符！");
			return false;
		}
		return true;
	}

	// 接受回传的联系人列表，添加到界面中的发送列表里
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		// 获得数据
		String json = data.getStringExtra(Settings.BUNDLE_CONTACT_DATA);
		if (json == null) {
			return;
		}
		//
		CommonTypeListDTO commonTypeListDTO = JsonUtils.fromJson(json, CommonTypeListDTO.class);
		if (commonTypeListDTO == null) {

			return;
		}
		addPerson(commonTypeListDTO.getList());
	}

	/*
	 * CommonTypeDTO的对象里存储了联系人的三个信息: name——联系人名字 phone——联系人电话
	 * memo——联系人类型：1:代表网站好友 2:代表本地好友
	 */
	private void addPerson(List<CommonTypeDTO> list) {

		if (list == null) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			addPerson(list.get(i));
		}
	}

	private void addPerson(CommonTypeDTO person) {
		if (person == null || person.getPhone() == null || person.getPhone().trim().equals("")) {
			return;
		}
		// 已经存在，不添加
		if (hasPerson(person)) {
			return;
		}

		// 添加联系人到联系人列表控件中
		TextView btn = new TextView(this);
		String name = person.getName();

		// 名字为空，则显示手机号码
		if (name == null || name.trim().equals("")) {
			name = person.getPhone();
		}

		btn.setSingleLine(true);
		btn.setText(name);
		btn.setBackgroundResource(R.drawable.sms_invite_person_button);
		btn.setHeight(dip2px(22));
		btn.setEllipsize(TextUtils.TruncateAt.END);
		btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setPadding(dip2px(4), dip2px(4), dip2px(20), dip2px(4));
		btn.setTag(person);

		// 点击时删除自己
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				removePerson(v);
			}
		});
		// 添加到控件
		contactList.addView(btn, contactList.getChildCount() - 1, new PredicateLayout.LayoutParams(dip2px(4), dip2px(4)));
		contactList.measure(0, 0);

		int contentHeight = contactList.getMeasuredHeight();
		// Log.i("in 2",""+i+"["+contactList.getLastMeasuredHeight()+"],listH="+contentHeight+","+px2dip(contentHeight)+"==sC="+scrollViewOfContactList.getHeight());

		// 超过最大高度时设置高度为最大高度
		if (px2dip(contentHeight) > maxHeightDipOfContactList) {
			ViewGroup.LayoutParams params = scrollViewOfContactList.getLayoutParams();
			params.height = dip2px(maxHeightDipOfContactList);
			scrollViewOfContactList.setLayoutParams(params);
			// Log.i("new H",""+contentHeight+"px"+"==sC="+scrollViewOfContactList.getHeight());
		}
		// Log.i("in 3",""+i+"==sC="+scrollViewOfContactList.getHeight());
		scrollViewOfContactList.scrollTo(0, contentHeight);

	}

	// 从联系人列表中删除一个控件
	protected void removePerson(View v) {
		contactList.removeView(v);
		contactList.measure(0, 0);

		// 删除时如果高度小于最大高度，则列表高度设置为WRAP_CONTENT
		int contentHeight = contactList.getMeasuredHeight();
		if (px2dip(contentHeight) < maxHeightDipOfContactList) {
			ViewGroup.LayoutParams params = scrollViewOfContactList.getLayoutParams();
			params.height = LayoutParams.WRAP_CONTENT;
			scrollViewOfContactList.setLayoutParams(params);
		}
	}

	// 判断发送到列表中是否已经存在该person
	private boolean hasPerson(CommonTypeDTO person) {
		int count = contactList.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = contactList.getChildAt(i);
			CommonTypeDTO tempPerson = (CommonTypeDTO) v.getTag();
			if (tempPerson != null && tempPerson.getPhone() != null && tempPerson.getPhone().equals(person.getPhone())) {
				return true;
			}
		}
		return false;
	}

	// 以后台要求的格式返回全部添加的联系人信息
	private String getAllPerson() {
		StringBuffer all = new StringBuffer();
		int count = contactList.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = contactList.getChildAt(i);
			CommonTypeDTO tempPerson = (CommonTypeDTO) v.getTag();
			if (tempPerson != null) {
				// 格式：[联系人类型]:[联系人名字]:[手机号码];
				all.append(tempPerson.getMemo()).append(":");
				all.append(tempPerson.getName()).append(":");
				all.append(tempPerson.getPhone()).append(";");
			}
		}
		if (all.length() > 0 && all.charAt(all.length() - 1) == ';') {
			all.deleteCharAt(all.length() - 1);
		}
		return all.toString();
	}
}
