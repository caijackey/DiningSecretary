package com.fg114.main.app.activity.takeaway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import android.widget.AdapterView;

import android.widget.LinearLayout.LayoutParams;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar.OnRatingBarChangeListener;

import android.widget.RatingBar;

import android.widget.TextView;
import android.widget.ToggleButton;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.ShowErrorActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.common.ListViewAdapter;
import com.fg114.main.app.adapter.common.ViewHolder;
import com.fg114.main.app.view.MyListView;
import com.fg114.main.service.dto.PgInfo;
import com.fg114.main.service.dto.SimpleData;
import com.fg114.main.service.dto.TakeoutCommentData;
import com.fg114.main.service.dto.TakeoutMenuData;
import com.fg114.main.service.dto.TakeoutMenuListDTO;
import com.fg114.main.service.dto.TakeoutMenuSelData;
import com.fg114.main.service.dto.TakeoutMenuSelPackDTO;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.CommonObservable;
import com.fg114.main.util.CommonObserver;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ViewUtils;
import com.fg114.main.weibo.WeiboUtilFactory;
import com.fg114.main.weibo.dto.User;
/**
 * 外卖餐厅评论表单
        传入参数  订单ID
 * @author dengxiangyu
 *
 */
public class TakeoutOrderCommentActivity extends MainFrameActivity{
	// 获得传入参数
	private String orderId;//订单ID
	//缓存数据
	private List<TakeoutMenuSelData> takeoutMenuList=new ArrayList<TakeoutMenuSelData>();
	private String commentText; 
	//界面
	LayoutInflater mInflater;
	private View contextView;
	private MyListView takeaway_order_comment_list;
	
	//控件
	private EditText takeaway_comment;
	private ToggleButton takeaway_comment_submit_chkShareSina;
	private ToggleButton takeaway_comment_submit_chkShareTX;
	private Button takeaway_comment_submit_btnUpLoad;
	private LinearLayout takeaway_comment_submit_message_mask;
	private TextView takeaway_upload_message_extra;
//	private LinearLayout takeout_order_commentListLayout;
	
	private RatingBar takeaway_order_menu_overallNum;
	
	private boolean isRefreshFoot = false;
	private boolean isSINAWBbinding;
	private boolean isQQWBbinding;
	
	private ListViewAdapter<TakeoutMenuSelData> adapter;
	private UserInfoDTO infoDTO;
	
	
	private int width;
	private int height;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅评论表单", "");
		// ----------------------------
		// 获得传入参数
		Bundle bundle = this.getIntent().getExtras();
		orderId= bundle.getString(Settings.BUNDLE_ORDER_ID);
		
		// 初始化界面
		initComponent();
		
		infoDTO = SessionManager.getInstance().getUserInfo(this);

		// 检查网络是否连通
		boolean isNetAvailable = ActivityUtil.isNetWorkAvailable(this.getApplicationContext());
		if (!isNetAvailable) {
			// 没有网络的场合，去提示页
			Bundle bund = new Bundle();
			bund.putString(Settings.BUNDLE_KEY_CONTENT, getString(R.string.text_info_net_unavailable));
			ActivityUtil.jump(this, ShowErrorActivity.class, 0, bund);
		}
		
		initListView();
		
		
	}
	@Override
	public void onRestart() {
		super.onRestart();
		// ----------------------------
		OpenPageDataTracer.getInstance().enterPage("外卖餐厅评论表单", "");
		// ----------------------------
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();

		infoDTO = SessionManager.getInstance().getUserInfo(this);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//		reloadPicture();
		// --------------------------------------------------------------
		// 如果未登录，显示登录按钮
		if (!SessionManager.getInstance().isUserLogin(this)) {
			this.getBtnOption().setVisibility(View.VISIBLE);
			this.getBtnOption().setText("登录");
			this.getBtnOption().setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bund = new Bundle();
					ActivityUtil.jump(TakeoutOrderCommentActivity.this, UserLoginActivity.class, 0, bund);
				}
			});
		} else {
			this.getBtnOption().setVisibility(View.INVISIBLE);
		}

		if (infoDTO.isSinaBindTag() && !infoDTO.isSinaWeiboExpired()) {
			// 绑定没有过期
			takeaway_comment_submit_chkShareSina.setBackgroundResource(R.drawable.sina_check_weibo);
			takeaway_comment_submit_chkShareSina.setChecked(false);
			isSINAWBbinding = true;

		} else {
			// 绑定无效
			takeaway_comment_submit_chkShareSina.setBackgroundResource(R.drawable.sina_web_check);
			isSINAWBbinding = false;
		}
		if (infoDTO.isQqBindTag() && !infoDTO.isQQWeiboExpired()) {
			takeaway_comment_submit_chkShareTX.setBackgroundResource(R.drawable.tx_check_weibo);
			takeaway_comment_submit_chkShareTX.setChecked(false);
			isQQWBbinding = true;
		} else {
			// 绑定无效
			takeaway_comment_submit_chkShareTX.setBackgroundResource(R.drawable.tx_web_check);
			isQQWBbinding = false;
		}

	}
	

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		commentText = takeaway_comment.getText().toString().trim();
		if (!CheckUtil.isEmpty(commentText)) {
			// 提示是否放弃评论内容
			DialogUtil.showAlert(TakeoutOrderCommentActivity.this, true, getString(R.string.text_dialog_comment_finish), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finishThis();
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

		} else {
			finishThis();
		}
	}
	
	private void finishThis() {
		super.finish();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// 如果是＠新浪好友页面回来的
		if (data != null && data.getSerializableExtra("sinaUser") != null) {

			User user = (User) data.getSerializableExtra("sinaUser");
			insertAtSinaWeiboUser(user);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void insertAtSinaWeiboUser(User user) {
		// 保存文本的选择当前状态
		final Editable text = takeaway_comment.getText();
		final int start = takeaway_comment.getSelectionStart();
		final int end = takeaway_comment.getSelectionEnd();
		final boolean isFocused = takeaway_comment.isFocused();
		final boolean hasSelection = takeaway_comment.hasSelection();

		String result = "@" + user.getName() + " ";
		if (user == null || CheckUtil.isEmpty(user.getName())) {

			return;
		}
		// 没有焦点时，获取焦点，并且自动设置为追加模式。
		if (!isFocused) {
			takeaway_comment.requestFocus();
		}
		// ----
		// 插入模式
		int oldLength = takeaway_comment.getText().length();
		int newLength = oldLength + result.length();
		int realInsertedLength = result.length();
		text.insert(end, result);
		takeaway_comment.setText(text);
		// 真正插入的字符数
		int newRealLength = takeaway_comment.getText().length();
		if (newRealLength != newLength) {

			realInsertedLength = newRealLength - oldLength;
		}
		// 光标重新定位
		takeaway_comment.setSelection(end + realInsertedLength);

	}
	

	/**
	 * 初始化
	 */
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("点评一下");
		this.getBtnGoBack().setText("返回");
		this.getBtnOption().setVisibility(View.INVISIBLE);
		
		// 内容部分
		mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contextView = mInflater.inflate(R.layout.takeout_order_comment, null);
		takeaway_order_comment_list=(MyListView) contextView.findViewById(R.id.takeaway_order_comment_list);
		
		takeaway_comment=(EditText) contextView.findViewById(R.id.takeaway_comment);
		takeaway_comment_submit_chkShareSina=(ToggleButton) contextView.findViewById(R.id.takeaway_comment_submit_chkShareSina);
		takeaway_comment_submit_chkShareTX=(ToggleButton) contextView.findViewById(R.id.takeaway_comment_submit_chkShareTX);
		takeaway_comment_submit_btnUpLoad=(Button) contextView.findViewById(R.id.takeaway_comment_submit_btnUpLoad);
		takeaway_comment_submit_message_mask=(LinearLayout)contextView.findViewById(R.id.takeaway_comment_submit_message_mask);
		takeaway_upload_message_extra=(TextView) contextView.findViewById(R.id.takeaway_upload_message_extra);
//		takeout_order_commentListLayout= (LinearLayout) contextView.findViewById(R.id.takeaway_upload_message_extra);
		
		
		
		takeaway_comment_submit_chkShareSina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (isSINAWBbinding) {
					// 如果sina绑定了就开始分享
					// chbShareToSina.setChecked(true);
				} else {
					// 开始绑定
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) { // 绑定成功
								isSINAWBbinding = true;
							} else {
								isSINAWBbinding = false;
								// doTest_sina();
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_SINA_WEIBO).requestWeiboShare(null);
				}
			}
		});
		
		takeaway_comment_submit_chkShareTX.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 绑定微博
				if (isQQWBbinding) {
					// 腾讯微博绑定了
					// Toast.makeText(RestaurantCommentSubmitActivity.this,
					// "true", 500).show();
				} else {
					// Toast.makeText(RestaurantCommentSubmitActivity.this,
					// "false", 500).show();
					Bundle bundle = new Bundle();
					bundle.putBoolean(Settings.BUNDLE_KEY_IS_LOGIN, false);
					CommonObservable.getInstance().addObserver(new CommonObserver.WeiboAuthResultObserver(new CommonObserver.WeiboAuthResultObserver.WeiboAuthResultListener() {

						@Override
						public void onComplete(boolean isSuccessful) {
							if (isSuccessful) {
								isQQWBbinding = true;
							} else {
								isQQWBbinding = false;
								// doTest_tencent();
							}
						}
					}));
					WeiboUtilFactory.getWeiboUtil(WeiboUtilFactory.PLATFORM_TENCENT_WEIBO).requestWeiboShare(null);
				}

			}
		});
		
		takeaway_comment_submit_btnUpLoad.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				ViewUtils.preventViewMultipleClick(view, 1000);
				executePostTakeawayCommentOrderTask();
				
			}
		});
		
		
		this.setFunctionLayoutGone();
		this.getMainLayout().addView(contextView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		
	}
	/**
	 * 获取外卖菜单列表
	 * 
	 */
	
	private void initListView() {
		if (takeaway_order_comment_list != null && adapter != null) {
			takeaway_order_comment_list.removeFooterView(adapter.getFooterView());
		}
		
		ListViewAdapter.OnAdapterListener<TakeoutMenuSelData> adapterListener = new ListViewAdapter.OnAdapterListener<TakeoutMenuSelData>() {

			@Override
			public void onRenderItem(final ListViewAdapter<TakeoutMenuSelData> adapter, ViewHolder holder, final TakeoutMenuSelData data) {
				// TODO Auto-generated method stub
				// 菜品名
				if ("".equals(data.
						name.
						trim())) {
					holder.$tv(R.id.takeaway_order_menu_name).setText(R.string.text_null_hanzi);
				} else {
					holder.$tv(R.id.takeaway_order_menu_name).setText(data.name.trim());
				}
				
				
				takeaway_order_menu_overallNum = (RatingBar) holder.$(R.id.takeaway_order_menu_overallNum);
				
				
//				takeaway_order_menu_overallNum.set
//				takeaway_order_menu_overallNum.measure(width,50);
//				data.overallNum=0.0;//清空评分
				takeaway_order_menu_overallNum.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					
					@Override
					public void onRatingChanged(RatingBar ratingbar, float f, boolean flag) {
						data.overallNum=f;
						//获取最新ListView 列表数据
						takeoutMenuList=adapter.getmList();
						

						
						
					}
				});
				
				}

			@Override
			public void onLoadPage(final ListViewAdapter<TakeoutMenuSelData> adapter, int startIndex, int pageSize) {
				// TODO Auto-generated method stub
				// ----
				OpenPageDataTracer.getInstance().addEvent("页面查询");
				// -----

				// 任务
				ServiceRequest request = new ServiceRequest(ServiceRequest.API.getTakeoutOrderMenuSelInfo);
				request.addData("orderId", orderId);
				
				CommonTask.request(request, "", new CommonTask.TaskListener<TakeoutMenuSelPackDTO>() {

					@Override
					protected void onSuccess(TakeoutMenuSelPackDTO dto) {
						// TODO Auto-generated method stub
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
//						takeout_order_commentListLayout.setVisibility(View.VISIBLE);
						ListViewAdapter.AdapterDto<TakeoutMenuSelData> adapterDto = new ListViewAdapter.AdapterDto<TakeoutMenuSelData>();
						adapterDto.setList(dto.list);
						adapter.onTaskSucceed(adapterDto);
//						takeoutMenuList=dto.list;
						
					}

					@Override
					protected void onError(int code, String message) {
						// TODO Auto-generated method stub
						super.onError(code, message);
						// -----
						OpenPageDataTracer.getInstance().endEvent("页面查询");
						// -----
//						doTest();
					}
//
//					void doTest() {
//						String json ="{\"list\":[{\"uuid\":\"123\",\"name\":\"菜品名1\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名2\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名3\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名4\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名5\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名6\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名7\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名8\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名9\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名10\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名11\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名12\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名13\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名14\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名15\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名16\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名17\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名18\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名19\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]}],\"typeDTO\":{\"uuid\":\"12345\",\"parentId\":\"12345\",\"name\":\"123\",\"num\":\"123\",\"succTag\":\"true\",\"phone\":\"123456\",\"memo\":\"11111111\",\"selectTag\":\"true\",\"isFirst\":\"false\",\"keywords\":\"12\",\"firstLetters\":\"aa\",\"firstLetter\":\"a\"}}";		
//							//"{\"list\":[{\"uuid\":\"123\",\"name\":\"菜品名\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]}],\"typeDTO\":{\"uuid\":\"12345\",\"parentId\":\"12345\",\"name\":\"123\",\"num\":\"123\",\"succTag\":\"true\",\"phone\":\"123456\",\"memo\":\"11111111\",\"selectTag\":\"true\",\"isFirst\":\"false\",\"keywords\":\"12\",\"firstLetters\":\"aa\",\"firstLetter\":\"a\"}}";
//						TakeoutMenuSelData dto = JsonUtils.fromJson(json, TakeoutMenuSelData.class);
//						onSuccess(dto);
//						
//
//					}
				});
			}
		};

		adapter = new ListViewAdapter<TakeoutMenuSelData>(R.layout.list_item_takeout_order_comment, adapterListener);
		adapter.setExistPage(false);
		adapter.setmCtx(TakeoutOrderCommentActivity.this);
		adapter.setListView(takeaway_order_comment_list);
		
		
		
	}
	
	
	/**
	 * 提交订单评论
	 */
	private void executePostTakeawayCommentOrderTask() {
		if (!checkInput()) {
			return;
		}
		ServiceRequest request = new ServiceRequest(API.postTakeoutOrderComment);
		request.addData("orderId", orderId);//id 
		request.addData("detail", commentText);//评论内容  不能为空 
		request.addData("gradeList", getGradeList());//评分列表   menuId:评分|menuId:评分   可以为空
	    request.addData("shareTo", getShareString());//分享到微博  sina:1;qq:0   当前只有sina:1  或者   sina:0  如果客户端这个字段传递为空 ，容错处理为不分享到任何平台
	    // -----
		OpenPageDataTracer.getInstance().addEvent("提交按钮");
		// -----
	    CommonTask.request(request, "", new CommonTask.TaskListener<SimpleData>() {

			@Override
			protected void onSuccess(SimpleData dto) {
				// TODO Auto-generated method stub
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
//				takeout_order_commentListLayout.setVisibility(View.VISIBLE);
				//提交是否成功
					finishThis();	
			}

			@Override
			protected void onError(int code, String message) {
				// TODO Auto-generated method stub
				super.onError(code, message);
				// -----
				OpenPageDataTracer.getInstance().endEvent("提交按钮");
				// -----
				DialogUtil.showAlert(TakeoutOrderCommentActivity.this, "", message + "");
//				doTest();
			}

			void doTest() {
				String json ="{\"list\":[{\"uuid\":\"123\",\"name\":\"菜品名1\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名2\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名3\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名4\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名5\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名6\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名7\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名8\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名9\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名10\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名11\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名12\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名13\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名14\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名15\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名16\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名17\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名18\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]},{\"uuid\":\"123\",\"name\":\"菜品名19\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]}],\"typeDTO\":{\"uuid\":\"12345\",\"parentId\":\"12345\",\"name\":\"123\",\"num\":\"123\",\"succTag\":\"true\",\"phone\":\"123456\",\"memo\":\"11111111\",\"selectTag\":\"true\",\"isFirst\":\"false\",\"keywords\":\"12\",\"firstLetters\":\"aa\",\"firstLetter\":\"a\"}}";		
					//"{\"list\":[{\"uuid\":\"123\",\"name\":\"菜品名\",\"spicyTag\":\"true\",\"specialPriceTag\":\"true\",\"picUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"bigPicUrl\":\"http://www.baidu.com/img/bdlogo.gif\",\"price\":\"43.1\",\"currentPriceTag\":\"true\",\"priceUnit\":\"个\",\"overallNum\":\"1.1\",\"selectedNum\":\"0\",\"gradeList\":[{\"starNum\":\"1.1\",\"gradeNum\":\"2\",\"numPerct\":\"2\"}]}],\"typeDTO\":{\"uuid\":\"12345\",\"parentId\":\"12345\",\"name\":\"123\",\"num\":\"123\",\"succTag\":\"true\",\"phone\":\"123456\",\"memo\":\"11111111\",\"selectTag\":\"true\",\"isFirst\":\"false\",\"keywords\":\"12\",\"firstLetters\":\"aa\",\"firstLetter\":\"a\"}}";
				SimpleData dto = JsonUtils.fromJson(json, SimpleData.class);
				onSuccess(dto);
				

			}
		});
	}
	/**
	 * 构建分享参数
	 * 
	 * @return
	 */
	// TODO
	private String getShareString() {
		StringBuffer sbShare = new StringBuffer();
		if (takeaway_comment_submit_chkShareSina.isChecked()) {
			sbShare.append("sina:1;");
		} else {
			sbShare.append("sina:0;");
		}
		if (takeaway_comment_submit_chkShareTX.isChecked()) {
			sbShare.append("qq:1");
		} else {
			sbShare.append("qq:0");
		}

		return sbShare.toString();
	}
	
	/**
	 * check 上传时 点评字段长度限制10-200
	 */
	private boolean checkInput() {
		commentText = takeaway_comment.getText().toString().trim();

		if (CheckUtil.isEmpty(commentText)) {
			ViewUtils.setError(takeaway_comment, "请输入评价内容");
			takeaway_comment.requestFocus();
			return false;
		}
		commentText = takeaway_comment.getText().toString();
		 if (commentText.length() < 10) {
		 ViewUtils.setError(takeaway_comment, "点评请至少输入10个字");
		 takeaway_comment.requestFocus();
		 return false;
		 }
		if (commentText.length() > 200) {
			ViewUtils.setError(takeaway_comment, "您点评内容过长，点评不能超过100字");
			takeaway_comment.requestFocus();
			return false;
		}

		return true;
	}
	
	
	/**
	 * 上传时  构建评分列表   menuId:评分|menuId:评分   未评分可以为NULL 1分到5分
	 */
	private String getGradeList(){
		StringBuffer stringBuffer = new StringBuffer();
		int i=0;
		while(i<takeoutMenuList.size()){
			if((int)takeoutMenuList.get(i).overallNum!=0){
			stringBuffer.append(takeoutMenuList.get(i).uuid);
			stringBuffer.append(":");
			stringBuffer.append((int)takeoutMenuList.get(i).overallNum);
			stringBuffer.append("|");
			i++;
			}
			else{
				i++;
			}
		}
		if(!stringBuffer.toString().equals("")){
			stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());
		}
		return stringBuffer.toString();
	}
}


