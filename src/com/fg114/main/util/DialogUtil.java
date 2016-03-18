package com.fg114.main.util;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.Inflater;

import android.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.*;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.activity.resandfood.FoodCommentActivity;
import com.fg114.main.app.activity.resandfood.FoodInfoActivity;
import com.fg114.main.app.activity.resandfood.MyMapActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentActivity;
import com.fg114.main.app.activity.resandfood.RestaurantCommentSubmitActivity;
import com.fg114.main.app.activity.usercenter.UserCenterActivity;
import com.fg114.main.app.activity.usercenter.UserLoginActivity;
import com.fg114.main.app.adapter.ArrayPairWheelAdapter;
import com.fg114.main.app.adapter.ArrayWheelAdapter;
import com.fg114.main.app.adapter.NumericWheelAdapter;
import com.fg114.main.app.adapter.RealTimeResAdapter;
import com.fg114.main.app.data.RealTimeResFilter;
import com.fg114.main.app.data.ValidateData;
import com.fg114.main.app.view.CustomDialog;
import com.fg114.main.app.view.ItemData;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.app.view.MyTimePickerDialog;
import com.fg114.main.app.view.OrderSelectionWheelView;
import com.fg114.main.app.view.SelectionLinkListView;
import com.fg114.main.app.view.SelectionListView;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.app.view.WheelView;
import com.fg114.main.app.view.WheelView.OnWheelChangedListener;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.DishData;
import com.fg114.main.service.dto.ErrorReportTypeData;
import com.fg114.main.service.dto.ErrorReportTypeListDTO;
import com.fg114.main.service.dto.ErrorReportTypeListPackDTO;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.service.dto.ShRegionListDTO;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.task.PostErrorReportTask;
import com.fg114.main.service.task.PostFeedBackTask;
import com.fg114.main.service.task.UserLoginTask;
import com.fg114.main.speech.asr.OnFinishListener;
import com.fg114.main.speech.asr.RecognitionEngine;
import com.fg114.main.speech.asr.RecognitionResult;
import com.unionpay.upomp.bypay.other.bg;

/**
 * 系统提示对话框
 * 
 * @author zhangyifan
 * 
 */
public class DialogUtil {

	private static final String TAG = DialogUtil.class.getName();

	private static final int TOAST_INTERVAL = 2000;
	private static long lastTime = 0;

	private static final String TAG_TYPE_NEARBY = "nearby";
	private static final int DEFAULT_DISTANCE = 5000;
	private static final String[] DAT_OF_WEEK = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
	private static int mYear = 0;
	private static int mMonth = 0;
	private static int mDay = 0;
	private static int mHour = 0;
	private static int mMinute = 0;
	private static int mDayOfWeek = Calendar.SUNDAY;
	private static String strDayName;
	private static Context context;
	private static TextView toastText;;
    private static Toast  toast;
    
    public static void setContext(Context ctx){
    	context=ctx;
    	toast=new Toast(context);
    	toastText=new TextView(context);
    }
    
	/**
	 * 提示
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, boolean isTwoButton, String msg, OnClickListener... listerner) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setMessage(msg);
			builder.setPositiveButton("确定", listerner[0]);
			if (isTwoButton) {
				builder.setNegativeButton("取消", listerner[1]);
			}
			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 自定义提示
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, boolean isTwoButton, String title, String msg, String firstBtnName, String SecoundBtnName, OnClickListener... listerner) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setMessage(msg);
			builder.setTitle(title);
			// builder.setIcon(0);//去除标题图片
			builder.setPositiveButton(firstBtnName, listerner[0]);
			if (isTwoButton) {
				builder.setNegativeButton(SecoundBtnName, listerner[1]);
			}
			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 自定义提示  无TITLE
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, boolean isTwoButton, String msg, String firstBtnName, String SecoundBtnName, OnClickListener... listerner) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setMessage(msg);
			// builder.setIcon(0);//去除标题图片
			builder.setPositiveButton(firstBtnName, listerner[0]);
			if (isTwoButton) {
				builder.setNegativeButton(SecoundBtnName, listerner[1]);
			}
			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 自定义提示 并且可以点击返回按键退出 主界面弹出没有网络连接的时候点击返回键退出应用
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, boolean isTwoButton,boolean canQuit, String title, String msg, String firstBtnName, String SecoundBtnName,android.content.DialogInterface.OnKeyListener keyListener, OnClickListener... listerner) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setMessage(msg);
			builder.setTitle(title);
			// builder.setIcon(0);//去除标题图片
			builder.setPositiveButton(firstBtnName, listerner[0]);
			if (isTwoButton) {
				builder.setNegativeButton(SecoundBtnName, listerner[1]);
			}
			
			builder.setOnKeyListener(keyListener);

			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 自定义提示
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, String title, String msg, String firstBtnName, String secoundBtnName, String thirdBtnName, OnClickListener... listener) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setMessage(msg);
			builder.setTitle(title);
			// builder.setIcon(0);//去除标题图片
			builder.setPositiveButton(firstBtnName, listener != null && listener.length > 0 ? listener[0] : null);
			builder.setNeutralButton(secoundBtnName, listener != null && listener.length > 1 ? listener[1] : null);
			builder.setNegativeButton(thirdBtnName, listener != null && listener.length > 2 ? listener[2] : null);
			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 简单alert提示
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showAlert(Context ctx, String title, String msg) {
		showAlert(ctx, title, msg, null);
	}

	/**
	 * 简单alert提示
	 * 
	 * @param ctx
	 * @param msg
	 * @param runAfterAlert
	 *            对话框关闭后，需要执行的任务
	 */
	public static void showAlert(Context ctx, String title, String msg, final Runnable runAfterAlert) {
		try {
			// 创建提示框
			Builder builder = new Builder(ctx);
			builder.setCancelable(false);
			builder.setTitle(title);
			builder.setMessage(msg);
			builder.setNegativeButton("确定", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (runAfterAlert != null) {
						runAfterAlert.run();
					}
				}
			});
			builder.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 版本更新提示框
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showVerComfire(final Context ctx, boolean isForceUpdate, final String version, String msg, OnClickListener... listerner) {
		// 创建提示框
		Builder builder = new Builder(ctx);
		builder.setCancelable(true);
		// 初始化显示组件
		LinearLayout dialogLayout = new LinearLayout(ctx);
		dialogLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		dialogLayout.setGravity(Gravity.LEFT);
		dialogLayout.setPadding(10, 10, 10, 10);
		// 对话框显示信息
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 0);
		TextView tvMsg = new TextView(ctx);
		tvMsg.setLayoutParams(layoutParams);
		tvMsg.setTextColor(ctx.getResources().getColor(R.color.text_color_white));
		tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		tvMsg.setText(msg);
		dialogLayout.addView(tvMsg);
		if (!isForceUpdate) {
			// 获得组件状态
			boolean isShow = SharedprefUtil.getBoolean(ctx, Settings.IS_AUTO_SHOW_UPDATE_DIALOG, true);

			CheckBox cbNotShow = new CheckBox(ctx);
			cbNotShow.setLayoutParams(layoutParams);
			cbNotShow.setTextColor(ctx.getResources().getColor(R.color.text_color_white));
			cbNotShow.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			cbNotShow.setText("下次不再提醒");
			cbNotShow.setChecked(!isShow);
			dialogLayout.addView(cbNotShow);

			cbNotShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					SharedprefUtil.saveBoolean(ctx, Settings.IS_AUTO_SHOW_UPDATE_DIALOG, !isChecked);
					SharedprefUtil.save(ctx, Settings.UPDATE_VERSION, version);
				}
			});
		}
		builder.setIcon(null);
		builder.setView(dialogLayout);
		builder.setPositiveButton("赶紧更新", listerner[0]);
		if (!isForceUpdate) {
			builder.setNegativeButton("后悔吧你", listerner[1]);
		} else {
			builder.setNegativeButton("不更新并退出", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityUtil.exitApp((Activity) ctx);
				}
			});
		}
		builder.show();
	}

	/**
	 * 提示确认对话框
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showComfire(final Context ctx, String title, String msg, String button1, final Runnable button1ClickListerner, String button2, final Runnable button2ClickListerner) {
		// 创建提示框
		Builder builder = new Builder(ctx);
		builder.setTitle(title);
		builder.setCancelable(false);
		builder.setMessage(msg);
		builder.setPositiveButton(button1, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) ctx).runOnUiThread(button1ClickListerner);
			}
		});
		builder.setNegativeButton(button2, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) ctx).runOnUiThread(button2ClickListerner);
			}
		});
		builder.show();
	}

	/**
	 * 提示确认对话框
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showComfire(final Context ctx, String title, String msg, String[] button, final Runnable... buttonClickListerner) {
		// 创建提示框
		Builder builder = new Builder(ctx);
		builder.setTitle(title);
		builder.setCancelable(false);
		builder.setMessage(msg);
		if (button.length > 0) {
			builder.setPositiveButton(button[0], new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					((Activity) ctx).runOnUiThread(buttonClickListerner[0]);
				}
			});
		}
		if (button.length > 1) {
			builder.setNegativeButton(button[1], new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					((Activity) ctx).runOnUiThread(buttonClickListerner[1]);
				}
			});
		}
		if (button.length > 2) {
			builder.setNeutralButton(button[2], new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((Activity) ctx).runOnUiThread(buttonClickListerner[2]);
				}
			});
		}

		builder.show();
	}

	/**
	 * 显示提示框，判断时间 防止
	 */
	public static void showToast(Context ctx, String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if(!ActivityUtil.isOnForeground(ctx)){
			return;
		}
		// if (lastTime == 0) {
		// makeToast(ctx, msg);
		// lastTime = System.currentTimeMillis();
		// }
		// long thisTime = System.currentTimeMillis();
		// if (thisTime - lastTime > TOAST_INTERVAL) {
		makeToast(ctx, msg);
		// lastTime = thisTime;
		// }
	}
	/**
	 * 显示提示框，判断时间 防止
	 */
	public static void showToastShort(Context ctx, String msg) {
		// 自定义Toast内容
		
		toastText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		toastText.setBackgroundResource(R.drawable.bg_alert);
		toastText.setPadding(20, 20, 20, 20);
		toastText.setText(msg);
		toastText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		toastText.setTextColor(ctx.getResources().getColor(R.color.text_color_black));
		// 自定义Toast设置
		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(toastText);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * 制作Toast
	 * 
	 * @param ctx
	 * @param msg
	 * @return
	 */
	private static void makeToast(Context ctx, String msg) {
		// 自定义Toast内容
		
		toastText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		toastText.setBackgroundResource(R.drawable.bg_alert);
		toastText.setPadding(20, 20, 20, 20);
		toastText.setText(msg);
		toastText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		toastText.setTextColor(ctx.getResources().getColor(R.color.text_color_black));
		// 自定义Toast设置
		
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(toastText);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * 显示查找对话框
	 */
	public static void showSearchDialog(Context ctx, String title, final View view, OnClickListener listerner) {

		Builder builder = new Builder(ctx);
		builder.setTitle(title);
		builder.setIcon(null);

		// 加入语音搜索按钮。--------added by xu jianjun,2011-12-13
		// 外部容器
		LinearLayout layout = new LinearLayout(ctx);
		layout.setBaselineAligned(true);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		int padding = UnitUtil.dip2px(ctx, 3);
		layout.setPadding(padding, padding, padding, padding);

		// 语音按钮
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button searchButton = (Button) inflater.inflate(R.layout.voice_recognition_button_small, null);
		int side = UnitUtil.dip2px(ctx, 34);
		int margin = UnitUtil.dip2px(3);
		ViewGroup.MarginLayoutParams buttonParams = new LinearLayout.LayoutParams(side, side);
		buttonParams.setMargins(margin, margin, margin, margin);

		// 文本框

		view.setBackgroundResource(R.drawable.text_view_bg);
		((EditText) view).setSingleLine(true);
		((EditText) view).setPadding(UnitUtil.dip2px(5), UnitUtil.dip2px(2), UnitUtil.dip2px(5), UnitUtil.dip2px(2));

		// 加入控件
		layout.addView(view, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
		layout.addView(searchButton, buttonParams);

		// 绑定语音按钮和结果框---------------------------
		RecognitionEngine eng = RecognitionEngine.getEngine(ctx);
		if (eng != null) {
			eng.bindButtonAndEditText(searchButton, (EditText) view, 0, null, new Runnable() {

				@Override
				public void run() {
					// --------ga跟踪----------
					// TraceManager.getInstance().dispatchEvent("语音搜索", "结果搜索",
					// ((EditText) view).getText().toString(), 0);
					// ------------------------
				}
			});
			// eng.bindButtonAndEditText(searchButton, (EditText)view);
		}
		// ----------------------------------------------

		builder.setView(layout);

		builder.setPositiveButton("返回", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setNegativeButton("搜索", listerner);
		builder.show();
		// ---
		view.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					InputMethodManager im = (InputMethodManager) ContextUtil.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					im.showSoftInput(view, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 500);

	}

	/**
	 * 显示图文推送对话框
	 */
	public static void showAdvantageDialog(Activity activity, String url, String text) {

		// 对话框布局定义
		LinearLayout layout = new LinearLayout(activity);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);
		layout.setPadding(10, 10, 10, 10);
		// 组件
		final MyImageView ivImage = new MyImageView(activity);
		final TextView tvText = new TextView(activity);
		final TextView tvTitle = new TextView(activity);

		// 建造对话框
		Builder builder = new Builder(activity);
		// 初始化对话框组件
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 0);
		ivImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 300));
		ivImage.setImageByUrl(url, false, 0, ScaleType.FIT_CENTER);
		tvTitle.setText("新功能说明");
		tvTitle.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		tvTitle.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 80));
		tvTitle.setGravity(Gravity.CENTER);
		layout.addView(tvTitle);
		// layout.addView(ivImage);
		tvText.setLayoutParams(layoutParams);
		tvText.setText(text);
		tvText.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		tvText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		layout.addView(tvText);
		builder.setIcon(null);
		builder.setView(layout);
		builder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {
			/**
			 * 用户登录处理
			 */
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ivImage.recycle(false);
				System.gc();
				dialog.cancel();
			}
		});
		builder.show();
	}

	/**
	 * 如果未登录，订单提交提示对话框
	 */
	public static void showPostOrderDialog(final Activity activity, String phone, final Runnable callBack) {

		final CustomDialog dialog = new CustomDialog(activity);
		// dialog.getLayoutParams().horizontalMargin = 0.5f;
		dialog.setGravity(Gravity.CENTER);
		LinearLayout layoutBg = new LinearLayout(activity);
		layoutBg.setGravity(Gravity.CENTER);
		layoutBg.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));

		// 对话框布局定义
		// LinearLayout layout = new LinearLayout(activity);
		LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.custom_dialog, null);
		// layout.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);
		// layout.setPadding(10, 10, 10, 10);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		layoutBg.addView(layout);
		dialog.setContentView(layoutBg, params);
		// 组件
		// TextView tvLine1 = new TextView(activity);
		TextView tvLine1 = (TextView) layout.findViewById(R.id.custom_dialog_tvMsg);
		TextView tvLine2 = new TextView(activity);
		TextView tvLine3 = new TextView(activity);

		tvLine1.setText(activity.getString(R.string.text_dialog_post_order_line2) + phone + "\n" + activity.getString(R.string.text_dialog_post_order_line3));

		Button btnOk = (Button) layout.findViewById(R.id.custom_dialog_btnButton1);
		Button btnLogin = (Button) layout.findViewById(R.id.custom_dialog_btnButton2);
		Button btnCancel = (Button) layout.findViewById(R.id.custom_dialog_btnButton3);
		btnOk.setText("记入" + phone);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				activity.runOnUiThread(callBack);
				dialog.cancel();
			}
		});
		btnLogin.setText("登录");
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				showUserLoginDialog(activity, callBack, 0);
				dialog.cancel();
			}
		});
		btnCancel.setText(R.string.text_button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();

		// //建造对话框
		// Builder builder = new Builder(activity);
		// //初始化对话框组件
		// LayoutParams layoutParams = new
		// LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		// layoutParams.setMargins(0, 5, 0, 0);
		// // tvLine1.setLayoutParams(layoutParams);
		// //
		// tvLine1.setText(activity.getResources().getText(R.string.text_dialog_post_order_line1));
		// //
		// tvLine1.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		// // tvLine1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		// // tvLine1.setSingleLine();
		// // layout.addView(tvLine1);
		// tvLine2.setLayoutParams(layoutParams);
		// tvLine2.setText(activity.getResources().getText(R.string.text_dialog_post_order_line2)
		// + phone);
		// tvLine2.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		// tvLine2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		// tvLine2.setSingleLine(false);
		// // layout.addView(tvLine2);
		// tvLine3.setLayoutParams(layoutParams);
		// tvLine3.setText(activity.getResources().getText(R.string.text_dialog_post_order_line3));
		// tvLine3.setTextColor(activity.getResources().getColor(R.color.text_color_white));
		// tvLine3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		// tvLine3.setSingleLine();
		// // layout.addView(tvLine3);
		// builder.setIcon(null);
		// builder.setView(layout);
		// builder.setPositiveButton("就记入\n" + phone, new
		// DialogInterface.OnClickListener() {
		// /**
		// * 继续下单处理
		// */
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// activity.runOnUiThread(callBack);
		// dialog.cancel();
		// }
		// });
		// builder.setNegativeButton("登录", new DialogInterface.OnClickListener()
		// {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// showUserLoginDialog(activity, callBack,
		// Settings.BOOKING_FROM_NET_ACTIVITY);
		// dialog.cancel();
		// }
		// });
		//
		// builder.show();
	}

	public static void showBookSuccessDialog(final Activity activity, String text, String msg, boolean isTuan, final Runnable... callBack) {
		final CustomDialog dialog = new CustomDialog(activity);
		// dialog.getLayoutParams().horizontalMargin = 0.5f;
		dialog.setGravity(Gravity.CENTER);
		LinearLayout layoutBg = new LinearLayout(activity);
		layoutBg.setGravity(Gravity.CENTER);
		layoutBg.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10), UnitUtil.dip2px(10));

		// 对话框布局定义
		// LinearLayout layout = new LinearLayout(activity);
		LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.custom_dialog, null);
		// layout.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);
		// layout.setPadding(10, 10, 10, 10);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		layoutBg.addView(layout);
		dialog.setContentView(layoutBg, params);
		// 组件
		// TextView tvLine1 = new TextView(activity);
		TextView tvLine1 = (TextView) layout.findViewById(R.id.custom_dialog_tvMsg);
		TextView tvLine2 = new TextView(activity);
		TextView tvLine3 = new TextView(activity);

		if (CheckUtil.isEmpty(msg)) {
			if (isTuan) {
				tvLine1.setText("预订已提交！该笔订单为团宴订单稍后我们将会联系您。\n您现在可以选择:");
			} else {
				tvLine1.setText("恭喜您，您的订单已经成功提交！\n您现在可以选择:");
			}
		} else {
			tvLine1.setText(msg + "\n您现在可以选择:");
		}

		Button btnViewOrder = (Button) layout.findViewById(R.id.custom_dialog_btnButton1);
		Button btnSendSms = (Button) layout.findViewById(R.id.custom_dialog_btnButton2);
		Button btnBack = (Button) layout.findViewById(R.id.custom_dialog_btnButton3);
		if (isTuan) {
			btnViewOrder.setVisibility(View.GONE);
			btnSendSms.setVisibility(View.GONE);
		}
		btnViewOrder.setText("查看订单");
		btnViewOrder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				activity.runOnUiThread(callBack[0]);
				dialog.cancel();
			}
		});
		btnSendSms.setText("免费短信请柬");
		btnSendSms.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				activity.runOnUiThread(callBack[1]);
				dialog.cancel();
			}
		});
		btnBack.setText("返回" + text);
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				activity.runOnUiThread(callBack[2]);
				dialog.cancel();
			}
		});

		dialog.setCancelable(false);

		dialog.show();
	}

	/**
	 * 如果未登录，显示用户登录对话框，菜品评论列表页转向评论提交页时调用
	 */
	public static void showUserLoginDialogWhenFoodComment(Activity activity, Runnable callBack, int requestCode) {

		// 目前直接跳转，可以不登录发表评论
		activity.runOnUiThread(callBack);

		/*
		 * boolean isLogin = SessionManager.getInstance().isUserLogin(activity);
		 * if (!isLogin) { Bundle bundle = new Bundle();
		 * bundle.putInt(Settings.BUNDLE_KEY_FROM_PAGE,
		 * Settings.FOOD_COMMENT_ACTIVITY);
		 * SessionManager.getInstance().setLastActivity(activity);
		 * SessionManager.getInstance().setLoginSuccessRunnable(callBack);
		 * ActivityUtil.jumpNotForResult(activity, UserLoginActivity.class,
		 * bundle, false); } else { //登录的场合，直接运行回调方法
		 * activity.runOnUiThread(callBack); }
		 */

	}

	/**
	 * 如果未登录，显示用户登录对话框
	 */
	public static void showUserLoginDialog(Activity activity, Runnable callBack, int requestCode) {
		boolean isLogin = SessionManager.getInstance().isUserLogin(activity);
		if (!isLogin) {
			Bundle bundle = new Bundle();
			SessionManager.getInstance().setLastActivity(activity);
			SessionManager.getInstance().setLoginSuccessRunnable(callBack);
			ActivityUtil.jumpNotForResult(activity, UserLoginActivity.class, bundle, false);
		} else {
			// 登录的场合，直接运行回调方法
			activity.runOnUiThread(callBack);
		}
	}

	/**
	 * 显示弹出对话框
	 */
	public static void showPopWindow(Activity activity, View parent, String msg, int bgResId) {

		LayoutInflater layoutInflater = (LayoutInflater) (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		View topWindow = layoutInflater.inflate(R.layout.popup_window, null);

		// 灰色背景遮罩
		View bgWindow = layoutInflater.inflate(R.layout.popup_window_grey_bg, null);
		final PopupWindow popBg = new PopupWindow(bgWindow, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出气泡
		final PopupWindow pop = new PopupWindow(topWindow, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setBackgroundDrawable(activity.getResources().getDrawable(bgResId));
		pop.setOutsideTouchable(true);
		pop.setWidth(300);

		// 弹出的文字信息
		TextView textView = (TextView) topWindow.findViewById(R.id.popup_window_tvInfo);
		textView.setSingleLine(false);
		textView.setText(msg);

		// 气泡关闭按钮
		final Button btnClose = (Button) topWindow.findViewById(R.id.popup_window_btnClose);
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pop.dismiss();
			}
		});

		topWindow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pop.dismiss();
			}
		});

		pop.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				popBg.dismiss();
			}

		});

		// 获得偏移量
		int popWidth = pop.getWidth();
		int xOff;
		if (bgResId == R.drawable.bg_pop_left) {
			xOff = parent.getMeasuredWidth() / 4;
		} else if (bgResId == R.drawable.bg_pop_middle) {
			xOff = parent.getMeasuredWidth() / 2 - popWidth / 2;
		} else {
			xOff = -popWidth + 3 * parent.getMeasuredWidth() / 4;
		}

		pop.showAsDropDown(parent, xOff, 0);

	}

	// These matrices will be used to move and zoom image
	static Matrix matrix = new Matrix();
	static Matrix savedMatrix = new Matrix();
	static PointF start = new PointF();
	static PointF mid = new PointF();
	static float oldDist;

	private static final double ZOOM_IN_SCALE = 1.25;// 放大系数
	private static final double ZOOM_OUT_SCALE = 0.8;// 缩小系数

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	static int mode = NONE;
	static float screenHeight = 0;// 屏幕的高度
	static float screenWidth = 0;// 屏幕的宽度
	static float scaleInit = 0;// 初始化压缩比例
	static float imgCurrentHeight = 0;// 处理后图片的高度
	static float imgCurrentWidth = 0;// 处理后图片的宽度
	static float scale = 0;// 滑动后图片的缩放
	static float addScale = 0;// 累计缩放
	static float imgScrollWidth = 0;// 处理后图片的宽度
	static float imgScrollHeight = 0;// 处理后图片的高度
	static int positionXFirst = 0;
	static int positionYFirst = 0;
	static int positionXSec = 0;
	static int positionYSec = 0;
	static boolean isPicType = false;// 图片的类型 true 横图 false竖图
	static float imgHeight = 0;// 原始图片的高度
	static float imgWidth = 0;// 原始图片的宽度
	static boolean isUp = false;

	/**
	 * 创建图片预览弹出框
	 */
	public static void createImageViewPanel(Activity activity, final View parent, final String url) {

		LayoutInflater layoutInflater = (LayoutInflater) (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		View topWindow = layoutInflater.inflate(R.layout.image_window, null);

		// 灰色背景遮罩
		final View bgWindow = layoutInflater.inflate(R.layout.popup_window_grey_bg, null);
		final PopupWindow popBg = new PopupWindow(bgWindow, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, true);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出气泡
		final PopupWindow imagePop = new PopupWindow(topWindow, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, true);
		imagePop.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.bg_menu));
		imagePop.setOutsideTouchable(true);

		final MyImageView imageView = (MyImageView) topWindow.findViewById(R.id.image_window_ivImage);
		
		// 图片空间
		imagePop.setAnimationStyle(R.style.menuAnimation);
		imagePop.showAtLocation(parent.getRootView(), Gravity.CENTER, 0, 0);
		bgWindow.invalidate();
		topWindow.requestLayout();
		topWindow.invalidate();
		imageView.setImageByUrl(url, false, 0, ScaleType.MATRIX, new Runnable() {

			@Override
			public void run() {
				try {
					imgHeight = imageView.bitmap.getHeight();// 原始图片的高度
					imgWidth = imageView.bitmap.getWidth();// 原始图片的宽度
					screenHeight = UnitUtil.getScreenHeightPixels();// 屏幕的高度
					screenWidth = UnitUtil.getScreenWidthPixels();// 屏幕的宽度
					bgWindow.postDelayed(new Runnable() {

						@Override
						public void run() {
							computePicPosition(imageView, imgHeight, imgWidth);
						}
					}, 10);
				}catch (Exception e) {
				}

			}

		});
		
		imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;

				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					positionXFirst = (int) event.getX();
					positionYFirst = (int) event.getY();
					matrix.set(view.getImageMatrix());
					savedMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					mode = DRAG;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
					}
					break;
				case MotionEvent.ACTION_UP:
					if (isUp) {
						imgScrollWidth = (imgScrollWidth * scale);
						imgScrollHeight = imgScrollHeight * scale;
					}
				case MotionEvent.ACTION_POINTER_UP:
					positionXSec = (int) event.getRawX();
					positionYSec = (int) event.getRawY();
					if (Math.abs(positionYSec - positionYFirst) <= 5 && Math.abs(positionXSec - positionXFirst) <= 5) {
						imagePop.dismiss();
					}
					mode = NONE;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
						isUp = false;

					} else if (mode == ZOOM) {
						float newDist = spacing(event);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							scale = newDist / oldDist;

							// 横图(缩小)
							if (isPicType && (imgScrollWidth * scale) <= imgCurrentWidth) {
								// scale=screenWidth/imgScrollWidth * scale;
								computePicPosition(imageView, imgHeight, imgWidth);
							} else if (!isPicType && (imgScrollWidth * scale) <= imgCurrentWidth) {
								computePicPosition(imageView, imgHeight, imgWidth);
							} else {
								matrix.postScale(scale, scale, mid.x / 2, mid.y / 2);
							}
							isUp = true;
						}
					}
					break;
				}

				view.setImageMatrix(matrix);
				return true;
			}
		});

		imagePop.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (imageView.bitmap != null) {
					imageView.bitmap.recycle();
					System.gc();
				}
				popBg.dismiss();
			}
		});
		
//		imagePop.setAnimationStyle(R.style.menuAnimation);
//		imagePop.showAtLocation(parent.getRootView(), Gravity.CENTER, 0, 0);
//		bgWindow.invalidate();
//		topWindow.requestLayout();
//		topWindow.invalidate();
	}

	private static void computePicPosition(final MyImageView imageView, float imgHeight, float imgWidth) {
		int paddingLeft;
		int paddingTop;

		// ------------利用原始图片最长的那条边进行压缩或者放大-------------
		// 1中情况不进行图片不处理 <1>、原始图片完全在屏幕内


		float scaleX = screenWidth / imgWidth;
		float scaleY = screenHeight / imgHeight;
		scaleInit = scaleX <= scaleY ? scaleX : scaleY;
		if (scaleX <= scaleY) {
			isPicType = true;
		} else {
			isPicType = false;
		}
		imgCurrentHeight = imgHeight * scaleInit;
		imgCurrentWidth = imgWidth * scaleInit;
		paddingLeft = (int) ((screenWidth - imgCurrentWidth) / 2);
		paddingTop = (int) ((screenHeight - imgCurrentHeight) / 2);
		imgScrollWidth = imgCurrentWidth;
		matrix.setScale(scaleInit, scaleInit);
		imageView.setImageMatrix(matrix);
		imageView.setPadding(paddingLeft, paddingTop, 0, 0);
		imageView.invalidate();
		imageView.requestLayout();
	}

	private static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private static void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/**
	 * 创建时间弹出框
	 */
	public static void createTimeViewPanel(Activity activity, View parent, long dateWheel, long timeWheel, long peopleWheel, long roomTypeWheel) {
		LayoutInflater layoutInflater = (LayoutInflater) (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		View topWindow = layoutInflater.inflate(R.layout.dialog_time, null);
		OrderSelectionWheelView orderSelectionWheelView = (OrderSelectionWheelView) topWindow.findViewById(R.id.order_info_selection_wheel);
		orderSelectionWheelView.setDateMilliSeconds(dateWheel);
		orderSelectionWheelView.setTimeMilliSeconds(timeWheel);
		orderSelectionWheelView.setPeopleNum(peopleWheel);
		orderSelectionWheelView.setRoomType(roomTypeWheel);
		// 灰色背景遮罩
		View bgWindow = layoutInflater.inflate(R.layout.popup_window_grey_bg, null);
		final PopupWindow popBg = new PopupWindow(bgWindow, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, true);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出气泡
		final PopupWindow imagePop = new PopupWindow(topWindow, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		imagePop.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.bg_menu));
		imagePop.setOutsideTouchable(true);

		bgWindow.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				imagePop.dismiss();
				popBg.dismiss();
				System.gc();
				return false;
			}
		});

		// imagePop.setOnDismissListener(new OnDismissListener()
		// {
		//
		// @Override
		// public void onDismiss()
		// {
		// popBg.dismiss();
		// }
		// });
		// imagePop.setTouchInterceptor(new OnTouchListener()
		// {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event)
		// {
		// imagePop.dismiss();
		// return false;
		// }
		// });
		imagePop.setAnimationStyle(R.style.menuAnimation);
		imagePop.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);
	}

	/**
	 * 补全带参数的message
	 * 
	 * @param msg
	 * @param args
	 * @return
	 */
	public static String fullMsg(String msg, String... args) {
		String fullMsg = msg;
		for (int i = 0; i < args.length; i++) {
			fullMsg = fullMsg.replace("{" + i + "}", args[i]);
		}
		return fullMsg;
	}

	public static ProgressDialog showProgressDialog(Context context, String msg, boolean isIndeterminate, int max, DialogInterface.OnCancelListener listerner) {
		ProgressDialog pdlg = new ProgressDialog(context);
		pdlg.setTitle("");
		pdlg.setMessage(msg);
		pdlg.setIndeterminate(isIndeterminate);
		if (isIndeterminate) {
			pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		} else {
			pdlg.setMax(max);
			pdlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		pdlg.setOnCancelListener(listerner);
		try {
			pdlg.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return pdlg;
	}

	public static void dismissProgressDialog(ProgressDialog pdlg) {
		try {
			if (pdlg != null && pdlg.isShowing()) {
				pdlg.dismiss();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static ProgressDialog getProgressDialog(Context context, String msg, boolean isIndeterminate, int max, DialogInterface.OnCancelListener listerner) {
		ProgressDialog pdlg = new ProgressDialog(context);
		pdlg.setTitle("");
		pdlg.setMessage(msg);
		pdlg.setIndeterminate(isIndeterminate);
		if (isIndeterminate) {
			pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		} else {
			pdlg.setMax(max);
			pdlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		pdlg.setOnCancelListener(listerner);
		return pdlg;
	}

	/**
	 * 显示错误类型选择对话框
	 * 
	 * @param bundle
	 * @throws Exception
	 */
	public static void showErrorReportTypeSelectionDialog(final Context context, final Bundle bundle) throws Exception {
		final String uuid = bundle.getString(Settings.UUID);
		final int errorReportType = bundle.getInt(Settings.BUNDLE_KEY_ERROR_REPORT_TYPE);
		
		/*
		 * 构造测试数据 功能 1:默认 2:单行文本框 3:输入多行文本和email 10:商户信息错误
		 */
		ErrorReportTypeListPackDTO errorReportTypeListPack = new ErrorReportTypeListPackDTO();
		List<ErrorReportTypeListDTO> typeList = new ArrayList<ErrorReportTypeListDTO>();

		// 从缓存中获取错误类型列表数据
		errorReportTypeListPack = SessionManager.getInstance().getListManager().getErrorReportTypeListPack(context);
		typeList = errorReportTypeListPack.getList();

		// 根据不同的上下文选择相应的错误类型列表
		// typeTag: 大类标志 1：餐馆 2：菜系
		// 错误列表
		List<ErrorReportTypeData> errorList = null;
		int typeTag = 0;

		// 餐厅报错
		if (errorReportType == Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_RESTAURANT) {
			// 找到餐厅报错list
			for (int i = 0; i < typeList.size(); i++) {
				if (typeList.get(i).getTypeTag() == 1) {
					errorList = typeList.get(i).getList();
					typeTag = typeList.get(i).getTypeTag();
					break;
				}
			}
		}
		// 菜品报错
		else if (errorReportType == Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_FOOD) {

			// 找到菜品报错list
			for (int i = 0; i < typeList.size(); i++) {
				if (typeList.get(i).getTypeTag() == 2) {
					errorList = typeList.get(i).getList();
					typeTag = typeList.get(i).getTypeTag();
					break;
				}

			}

		}
		// 外卖报错
		else if (errorReportType == Settings.BUNDLE_KEY_ERROR_REPORT_TYPE_TAKEAWAY) {

			// 找到外卖报错list
			for (int i = 0; i < typeList.size(); i++) {
				if (typeList.get(i).getTypeTag() == 3) {
					errorList = typeList.get(i).getList();
					typeTag = typeList.get(i).getTypeTag();
					break;
				}

			}

		}

		// 如果没有找到报错列表，转到默认的报错页面——类型３
		if (errorList == null) {
			Bundle data = new Bundle();
			ErrorReportTypeData defaultErrorData = new ErrorReportTypeData();
			defaultErrorData.setTypeId("12"); // 12:默认——其他错误报告
			defaultErrorData.setTypeName("错误报告");
			defaultErrorData.setInputBoxTitle("感谢您报告错误信息");
			defaultErrorData.setFuncTag(3);
			data.putSerializable("ErrorReportTypeData", defaultErrorData);
			data.putInt("typeTag", -99);
			data.putInt(Settings.BUNDLE_KEY_ID, 101);

			// /---------------------------------------------------------------
			ActivityUtil.jump(context, ErrorReportActivity.class, 0, data);
			return;
		}

		// -------对话框------
		Builder bd = new Builder(context);
		// 设置title
		bd.setTitle("报错类型");
		bd.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// ---------------
		// 生成列表项文字
		int length = errorList.size();
		String[] itemTexts = new String[length];
		for (int i = 0; i < length; i++) {
			itemTexts[i] = errorList.get(i).getTypeName();
		}
		// 用于在事件中访问
		final List<ErrorReportTypeData> errors = errorList;
		final int _typeTag = typeTag;
		bd.setItems(itemTexts, new OnClickListener() {

			@Override
			// 定义列表项点击事件
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 获得列表项数据
				final ErrorReportTypeData error = errors.get(which);
				Bundle data = null;

				// 根据不同的功能，调用不同的页面或功能 1:默认 2:单行文本框 3:输入多行文本和email 10:商户信息错误
				switch (error.getFuncTag()) {
				case 1:
					// 默认，没有界面的报错
					//需要确认提示的
					if(error.isNeedConfirmTag()){ 
						DialogUtil.showComfire(context, "提示", error.getComfirmHint(), new String[]{"确定","取消"}, new Runnable() {
							
							@Override
							public void run() {
								final PostErrorReportTask postErrorReportTask = new PostErrorReportTask(
										_typeTag, 
										error.getFuncTag(), 
										error.getTypeId(), 
										error.getTypeName(), 
										context.getString(R.string.text_info_loading), 
										context, "", // name
										"", // email
										uuid, "" // errorInfo
								);
								postErrorReportTask.execute();
							}
						},new Runnable() {						
							@Override
							public void run() {
							}
						});
					}else{
						final PostErrorReportTask postErrorReportTask = new PostErrorReportTask(
								_typeTag, 
								error.getFuncTag(), 
								error.getTypeId(), 
								error.getTypeName(), 
								context.getString(R.string.text_info_loading), 
								context, "", // name
								"", // email
								uuid, "" // errorInfo
						);
						postErrorReportTask.execute();
					}
					
					break;
				case 2:
					// 单行文本框(２和３用的是同一个页面)
				case 3:
					// 输入多行文本和email
					data = new Bundle();
					data.putInt("typeTag", _typeTag);
					data.putSerializable("ErrorReportTypeData", error);
					data.putString(Settings.UUID, uuid);
					ActivityUtil.jump(context, ErrorReportActivity.class, 0, data);
					break;
				case 10:
					// 商户信息错误
					data = new Bundle();
					data.putString(Settings.BUNDLE_KEY_ID, uuid);
					data.putString(Settings.UUID, uuid);
					ActivityUtil.jump(context, AddOrUpdateResActivity.class, 0, data);
					break;

				// 地图报错
				case 11:
					if (Settings.gBaiduAvailable) {// 如果百度地图可用
						data = (Bundle) bundle.clone();
						data.putString(Settings.BUNDLE_KEY_ID, uuid);
						data.putInt(Settings.BUNDLE_BAIDU_MODE, Settings.Baidu_Choose_Loc);
						data.putInt("typeTag", _typeTag);
						data.putSerializable("ErrorReportTypeData", error);
						ActivityUtil.jump(context, MyMapActivity.class, 0, data);

					} else {// 百度地图不可用，直接报错，不跳转到地图页
						{
							String name = "";
							if (SessionManager.getInstance().isUserLogin(context)) {
								name = SessionManager.getInstance().getUserInfo(context).getNickName();
							}
							String restId = bundle.getString(Settings.BUNDLE_REST_ID);
							String restName = bundle.getString(Settings.BUNDLE_REST_NAME);
							String errorInfo = restName + " 的餐厅位置有误!";
							PostErrorReportTask task = new PostErrorReportTask(1, error.getFuncTag(), error.getTypeId(), error.getTypeName(), context.getString(R.string.text_info_loading), context,
									name, "", restId, errorInfo, null);

							task.execute();
						}
					}
					break;
				// 默认跳到信息反馈功能
				default:
					data = new Bundle();
					data.putString(Settings.UUID, uuid);
					ActivityUtil.jump(context, ErrorReportActivity.class, 0, data);
				}
			}

		});
		bd.show();

	}

	/**
	 * 通用的对话框结束时的回调接口。
	 */
	public interface DialogCallback {

		public void onDialogConfirm(DishData result);

		public void onDialogCancel();

		public void onDialogDelete(DishData result);
	}

	/**
	 * 点菜参数编辑对话框
	 * 
	 * @param data
	 *            传送给Dialog的菜品数据。
	 * @param callBack
	 *            Dialog结束后回调的接口。
	 * @param extraData
	 *            传送给Dialog的附加数据， 例如：
	 *            允许“份数”的最大值(amountMax[int]默认99)最小值(amountMin[int]默认0)；
	 *            最大值警告信息(amountMaxWarning
	 *            [String]默认null，不提示)，最小值警告信息(amountMinWarning
	 *            [String]默认null，不提示)
	 * @throws Exception
	 */
	public static void showOrderDishDialog(final Activity context, final DishData data, final DialogCallback callBack, final Bundle extraData) throws Exception {

		// // 对话框窗口
		// View content = View.inflate(context, R.layout.order_dish_dialog,
		// null);
		// final PopupWindow window = new PopupWindow(context);
		// window.setOutsideTouchable(true);
		// window.setFocusable(true);
		// window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
		// window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
		// ColorDrawable color = new ColorDrawable(0x99000000);
		// window.setBackgroundDrawable(color);
		// window.setContentView(content);
		//
		// // 对话框控件
		// TextView name = (TextView)
		// content.findViewById(R.id.order_dish_name);
		// TextView price = (TextView)
		// content.findViewById(R.id.order_dish_price);
		// final com.fg114.main.app.view.DigitalSelector amount =
		// (com.fg114.main.app.view.DigitalSelector)
		// content.findViewById(R.id.order_dish_amount);
		// final Spinner processType = (Spinner)
		// content.findViewById(R.id.order_dish_process_type);
		// View processTypeRow =
		// content.findViewById(R.id.order_dish_process_type_row);
		//
		// // 按钮
		// Button buttonOk = (Button)
		// content.findViewById(R.id.order_dish_button_ok);
		// Button buttonDelete = (Button)
		// content.findViewById(R.id.order_dish_button_delete);
		// Button buttonCancel = (Button)
		// content.findViewById(R.id.order_dish_button_cancel);
		//
		// // 初始化对话框数据及状态
		// name.setText(data.getName());
		// if (!data.isCurrentPriceTag()) {
		// price.setText((new DecimalFormat("0.00")).format(data.getPrice()) +
		// " " + data.getUnit());
		// } else {
		// price.setText("时价");
		// }
		//
		// amount.setValue(data.getNum() + data.getOldNum());
		// amount.setMinValue(extraData.getInt("amountMin"));
		// amount.setMaxValue(extraData.getInt("amountMax") == 0 ? 99 :
		// extraData.getInt("amountMax"));
		// amount.setMaxWarning(extraData.getString("amountMaxWarning"));
		// amount.setMinWarning(extraData.getString("amountMinWarning"));
		//
		// // 做法适配器
		// /*
		// * if(data.getProcessTypeList()!=null){
		// *
		// * //新增一个未选择项 CommonTypeDTO noselect=new CommonTypeDTO();
		// * noselect.setUuid(null); noselect.setName("没有要求");
		// * data.getProcessTypeList().add(0, noselect); }
		// */
		//
		// ArrayAdapter<DishProcessTypeDTO> adapter = new
		// ArrayAdapter<DishProcessTypeDTO>(context,
		// R.layout.spinner_item_simple, data.getProcessTypeList());
		// adapter.setDropDownViewResource(R.layout.spinner_item_simple_dropdown);
		// processType.setAdapter(adapter);
		// // 没有做法时隐藏Sprinner
		// if (data.getProcessTypeList() == null ||
		// data.getProcessTypeList().size() <= 0) {
		// processTypeRow.setVisibility(View.GONE);
		// }
		// // 如果没有已点菜，隐藏"删除"按钮
		// if (data.getNum() == 0) {
		// buttonDelete.setVisibility(View.GONE);
		// }
		// buttonOk.setOnClickListener(new View.OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// if (callBack != null) {
		// data.setNum(amount.getValue() - data.getOldNum());
		// DishProcessTypeDTO selectedItem = (DishProcessTypeDTO)
		// processType.getSelectedItem();
		// if (selectedItem != null) {
		// data.setSelectProcessTypeId(selectedItem.getUuid());
		// data.setSelectProcessTypeName(selectedItem.getName());
		// } else {
		// data.setSelectProcessTypeId(null);
		// data.setSelectProcessTypeName(null);
		// }
		// if (callBack != null) {
		// callBack.onDialogConfirm(data);
		// }
		//
		// window.dismiss();
		// }
		// }
		// });
		//
		// buttonDelete.setOnClickListener(new View.OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// if (callBack != null) {
		// data.setNum(0);
		// // 如果已经点了菜，提示信息
		// if (data.getOldNum() > 0) {
		// DialogUtil.showToast(context,
		// extraData.getString("amountMinWarning"));
		// }
		// if (callBack != null) {
		// callBack.onDialogDelete(data);
		// }
		//
		// window.dismiss();
		// }
		// }
		// });
		// buttonCancel.setOnClickListener(new View.OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// if (callBack != null) {
		// callBack.onDialogCancel();
		// }
		// window.dismiss();
		// }
		// });
		// window.showAtLocation(((Activity)
		// context).getWindow().getDecorView(), Gravity.CENTER, 0, 0);

	}

	/**
	 * 显示弹出层
	 * 
	 * @param context
	 * @param parent
	 *            父View
	 * @param child
	 *            弹出气泡的内容
	 * @param dismissOnTouch
	 *            是否在点击屏幕时消失
	 * @param listener
	 *            弹出层消失的监听
	 */
	public static void showPopupWindow(Context context, View parent, View child, final boolean dismissOnTouch, final PopupWindow.OnDismissListener listener) {

		if (parent == null) {
			parent = ((Activity) context).getWindow().getDecorView();
		}

		// 灰色背景遮罩
		LinearLayout bgView = new LinearLayout(context);
		bgView.setOrientation(LinearLayout.VERTICAL);
		bgView.setBackgroundColor(0xb5555555);
		bgView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		bgView.setLayoutParams(params);
		final PopupWindow popBg = new PopupWindow(bgView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出层显示的内容
		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popMain.setBackgroundDrawable(new BitmapDrawable());
		popMain.setOutsideTouchable(true);
		popMain.setFocusable(true);
		popMain.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
		popMain.setClippingEnabled(true);

		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popBg.isShowing()) {
					popBg.dismiss();
				}
				if (listener != null) {
					listener.onDismiss();
				}
			}
		});

		popMain.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (dismissOnTouch) {
					popMain.dismiss();
				}
				return true;
			}
		});

		popMain.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
	}
	
	/**
	 * 显示弹出层
	 * 
	 * @param context
	 * @param parent
	 *            父View
	 * @param child
	 *            弹出气泡的内容
	 * @param dismissOnTouch
	 *            是否在点击屏幕时消失
	 * @param listener
	 *            弹出层消失的监听
	 * @return 
	 */
	public static PopupWindow showPopupwindow(Context context, View parent, View child) {

		if (parent == null) {
			parent = ((Activity) context).getWindow().getDecorView();
		}

		// 灰色背景遮罩
		LinearLayout bgView = new LinearLayout(context);
		bgView.setOrientation(LinearLayout.VERTICAL);
		bgView.setBackgroundColor(0xb5555555);
		bgView.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		bgView.setLayoutParams(params);
		final PopupWindow popBg = new PopupWindow(bgView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popBg.setOutsideTouchable(true);
		popBg.showAtLocation(parent.getRootView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		// 弹出层显示的内容
		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popMain.setBackgroundDrawable(new BitmapDrawable());
		popMain.setOutsideTouchable(true);
		popMain.setFocusable(true);
		popMain.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
		popMain.setClippingEnabled(true);

		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popBg.isShowing()) {
					popBg.dismiss();
				}
//				if (listener != null) {
//					listener.onDismiss();
//				}
			}
		});

//		popMain.setTouchInterceptor(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if (dismissOnTouch) {
//					popMain.dismiss();
//				}
//				return true;
//			}
//		});

		popMain.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		return popMain;
	}
	
	/**
	 * 显示弹出层
	 * 
	 * @param context
	 * @param parent
	 *            父View
	 * @param child
	 *            弹出气泡的内容
	 */
	public static PopupWindow showPopupwindow2(Context context, View parent, View child) {

		if (parent == null) {
			parent = ((Activity) context).getWindow().getDecorView();
		}


		// 弹出层显示的内容
		final PopupWindow popMain = new PopupWindow(child, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popMain.setBackgroundDrawable(new BitmapDrawable());
		popMain.setOutsideTouchable(true);
		popMain.setFocusable(true);
		popMain.setAnimationStyle(R.style.Animations_PopDownMenu_Center);
		popMain.setClippingEnabled(true);

		popMain.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {

			}
		});
		
		popMain.setTouchInterceptor(new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
//			if (dismissOnTouch) {
//				popMain.dismiss();
//			}
			return true;
		}
	});

		popMain.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
		return popMain;
	}

	/**
	 * 显示全屏朦皮
	 * 
	 * @param context
	 * @param drawableResourceId
	 *            要显示的朦皮的resourceId
	 */
	public static void showVeilPicture(final Activity context, int drawableResourceId) {

		try {
			LinearLayout content = new LinearLayout(context);
			final PopupWindow window = new PopupWindow(context);
			window.setOutsideTouchable(true);
			window.setFocusable(true);
			window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
			window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
			window.setContentView(content);
			window.setBackgroundDrawable(context.getResources().getDrawable(drawableResourceId));
			window.showAtLocation(context.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
			/*
			 * //校准顶部位置 Window parentWindow=context.getWindow(); Rect r = new
			 * Rect(); parentWindow.getDecorView().getRootView().
			 * getWindowVisibleDisplayFrame(r); int contentViewTop=r.top;
			 * content.setPadding(0, contentViewTop, 0, 0); View v=new
			 * View(context); v.setBackgroundColor(0xFF0000FF);
			 * v.setLayoutParams(new
			 * android.view.ViewGroup.LayoutParams(126,126));
			 * content.addView(v);
			 */
			// ----
			window.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					window.dismiss();
					return true;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 只会显示一次的全屏朦皮，朦皮以键值keyname来区分
	 * 
	 * @param context
	 * @param drawableResourceId
	 *            要显示的朦皮的resourceId
	 * @param keyname
	 *            用来标识朦皮的唯一名称
	 */
	public static void showVeilPictureOnce(final Activity context, final int drawableResourceId, final String keyname) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (SharedprefUtil.getBoolean(context, keyname, true)) {
						SystemClock.sleep(500);

						context.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								try {

									DialogUtil.showVeilPicture(context, drawableResourceId);
									SharedprefUtil.saveBoolean(context, keyname, false);
								} catch (Exception ex) {
									Log.e("showVeilPictureOnce " + keyname, ex.getMessage(), ex);
								}
							}
						});
					}
				} catch (Exception ex) {
					Log.e("showVeilPictureOnce " + keyname, ex.getMessage(), ex);
				}

			}

		}).start();
	}

	/*
	 * 适用于通用对话框的初始化事件和销毁事件监听接口
	 */
	public interface DialogEventListener {
		/**
		 * @param contentView
		 *            对话框的内容区
		 * @param dialog
		 *            对话框
		 */
		public void onInit(View contentView, PopupWindow dialog);
	}

	/**
	 * 通用对话框，具有固定的半通明全屏背景，使用一个自定义的layout资源作为对话框的外观。 内部通过popwindow实现。
	 * 
	 * @param layoutResourceId
	 *            对话框要显示的内容。
	 * @param listener
	 *            一个监听对话框生命期的监听器。
	 */
	public static void showDialog(Activity activity, int layoutResourceId, final DialogEventListener listener) {

		Context context = ContextUtil.getContext();
		Window parentWindow = activity.getWindow();

		// 对话框内容
		View content = View.inflate(context, layoutResourceId, null);
		PopupWindow window = new PopupWindow(context, null, 0);
		window.setOutsideTouchable(true);
		window.setFocusable(true);
		window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
		window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
		ColorDrawable color = new ColorDrawable(0x99000000);
		window.setBackgroundDrawable(color);
		window.setContentView(content);
		window.showAtLocation(parentWindow.getDecorView(), Gravity.CENTER, 0, 0);
		// 校正内容区域的位置
		int contentViewTop = parentWindow.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		Rect r = new Rect();
		parentWindow.getDecorView().getRootView().getWindowVisibleDisplayFrame(r);
		contentViewTop = r.top;

		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
		content.setPadding(0, contentViewTop, 0, 0);
		// ---
		if (listener != null) {
			listener.onInit(content, window);
		}
	}

	// -----------------------------------------
	public static void showDialogNew(Activity activity, int layoutResourceId, final DialogEventListenerNew listener) {

		Context context = ContextUtil.getContext();
		Window parentWindow = activity.getWindow();

		// 对话框内容
		View content = View.inflate(context, layoutResourceId, null);
		com.fg114.main.app.view.PopupWindow window = new com.fg114.main.app.view.PopupWindow(context, null, 0);
		window.setOutsideTouchable(true);
		window.setFocusable(true);
		window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
		window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
		ColorDrawable color = new ColorDrawable(0x99000000);
		window.setBackgroundDrawable(color);
		window.setContentView(content);
		window.showAtLocation(parentWindow.getDecorView(), Gravity.CENTER, 0, 0);
		// 校正内容区域的位置
		int contentViewTop = parentWindow.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		Rect r = new Rect();
		parentWindow.getDecorView().getRootView().getWindowVisibleDisplayFrame(r);
		contentViewTop = r.top;

		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
		content.setPadding(0, contentViewTop, 0, 0);
		// ---
		if (listener != null) {
			listener.onInit(content, window);
		}
	}

	public interface DialogEventListenerNew {
		/**
		 * @param contentView
		 *            对话框的内容区
		 * @param dialog
		 *            对话框
		 */
		public void onInit(View contentView, com.fg114.main.app.view.PopupWindow dialog);
	}

	// -----------------------------------------

	public interface OnWheelSelectedListener {
		public void onSelected(RfTypeDTO[] values);
	}

	public static void showOneWheelDialog(Activity activity, List<RfTypeDTO> list, int currentItem, final OnWheelSelectedListener listener) {
		try {
			if (list == null || list.size() == 0) {
				return;
			}

			int margin = 10;

			List<MyPair<String, RfTypeDTO>> listOne = new ArrayList<MyPair<String, RfTypeDTO>>();
			for (RfTypeDTO dto : list) {
				listOne.add(new MyPair<String, RfTypeDTO>(dto.getName(), dto));
			}

			final WheelView wvMain = new WheelView(activity);
			final ArrayPairWheelAdapter<RfTypeDTO> adapter = new ArrayPairWheelAdapter<RfTypeDTO>(activity, listOne);
			adapter.setTextSize(16);
			adapter.setTextGravity(Gravity.CENTER);
			adapter.setMaxTextLength(50);
			wvMain.setViewAdapter(adapter);
			wvMain.setVisibleItems(5);
			wvMain.setCurrentItem(currentItem);
			wvMain.addScrollingListener(new WheelView.OnWheelScrollListener() {

				@Override
				public void onScrollingStarted(WheelView wheel) {

				}

				@Override
				public void onScrollingFinished(WheelView wheel) {
					if (adapter.getValue(wheel.getCurrentItem()) == null || TextUtils.isEmpty(adapter.getValue(wheel.getCurrentItem()).getUuid())) {
						if (wheel.getCurrentItem() + 1 < adapter.getItemsCount()) {
							wheel.setCurrentItem(wheel.getCurrentItem() + 1, true);
						} else if (wheel.getCurrentItem() - 1 > -1) {
							wheel.setCurrentItem(wheel.getCurrentItem() - 1, true);
						}
					}
				}
			});

			LinearLayout.LayoutParams paramsWheel = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			paramsWheel.setMargins(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin * 2));

			final Button btCancel = new Button(activity);
			btCancel.setText("取消");
			btCancel.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btCancel.setBackgroundResource(R.drawable.button_black);
			btCancel.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsCancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			final Button btOk = new Button(activity);
			btOk.setText("确认");
			btOk.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btOk.setBackgroundResource(R.drawable.button_red);
			btOk.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btOk.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsOk = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsOk.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			RelativeLayout rlButton = new RelativeLayout(activity);
			rlButton.setBackgroundResource(R.drawable.bg_wheel_dialog_top);
			rlButton.addView(btCancel, paramsCancel);
			rlButton.addView(btOk, paramsOk);
			rlButton.setPadding(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin));
			LinearLayout llMain = new LinearLayout(activity);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.addView(rlButton);
			llMain.addView(wvMain, paramsWheel);

			final CustomDialog dialog = new CustomDialog(activity);
			dialog.setContentView(llMain);
			dialog.setWidth(LayoutParams.FILL_PARENT);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setGravity(Gravity.BOTTOM);
			dialog.setLocation(0, 0);
			dialog.setAnimation(R.style.Animation_Bottom);

			btCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					dialog.dismiss();
					if (listener != null && adapter.getValue(wvMain.getCurrentItem()) != null) {
						RfTypeDTO[] values = new RfTypeDTO[1];
						values[0] = adapter.getValue(wvMain.getCurrentItem());
						listener.onSelected(values);
					}
				}
			});

			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showTwoWheelsDialog(final Activity activity, List<RfTypeListDTO> list, int currentItem, int currentSubItem, final OnWheelSelectedListener listener) {
		try {
			if (list == null || list.size() == 0) {
				return;
			}

			int margin = 10;
			final int textSize = 16;

			final HashMap<Integer, List<MyPair<String, RfTypeDTO>>> mapSub = new HashMap<Integer, List<MyPair<String, RfTypeDTO>>>();
			List<MyPair<String, RfTypeDTO>> listMain = new ArrayList<MyPair<String, RfTypeDTO>>();
			for (int i = 0; i < list.size(); i++) {
				RfTypeListDTO dtoMain = list.get(i);
				listMain.add(new MyPair<String, RfTypeDTO>(dtoMain.getName(), dtoMain.getMainDto()));
				List<MyPair<String, RfTypeDTO>> listSub = new ArrayList<MyPair<String, RfTypeDTO>>();
				if (dtoMain.getList() != null && dtoMain.getList().size() > 0) {
					for (RfTypeDTO dtoSub : dtoMain.getList()) {
						listSub.add(new MyPair<String, RfTypeDTO>(dtoSub.getName(), dtoSub));
					}
				}
				mapSub.put(i, listSub);
			}

			final WheelView wvSub = new WheelView(activity);
			final ArrayPairWheelAdapter<RfTypeDTO> adapterSub = new ArrayPairWheelAdapter<RfTypeDTO>(activity, mapSub.get(currentItem));
			adapterSub.setTextSize(textSize);
			adapterSub.setTextGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			adapterSub.setMaxTextLength(8);
			wvSub.setViewAdapter(adapterSub);
			wvSub.setVisibleItems(5);
			if (adapterSub.getItemsCount() > 0) {
				wvSub.setCurrentItem(currentSubItem);
			}
			wvSub.addScrollingListener(new WheelView.OnWheelScrollListener() {

				@Override
				public void onScrollingStarted(WheelView wheel) {

				}

				@Override
				public void onScrollingFinished(WheelView wheel) {
					ArrayPairWheelAdapter<RfTypeDTO> adapterSub = (ArrayPairWheelAdapter<RfTypeDTO>) wheel.getViewAdapter();
					if (adapterSub.getValue(wheel.getCurrentItem()) == null) {
						if (wheel.getCurrentItem() + 1 < adapterSub.getItemsCount()) {
							wheel.setCurrentItem(wheel.getCurrentItem() + 1, true);
						} else if (wheel.getCurrentItem() - 1 > -1) {
							wheel.setCurrentItem(wheel.getCurrentItem() - 1, true);
						}
					}
				}
			});

			final WheelView wvMain = new WheelView(activity);
			final ArrayPairWheelAdapter<RfTypeDTO> adapter = new ArrayPairWheelAdapter<RfTypeDTO>(activity, listMain);
			adapter.setTextSize(textSize);
			adapter.setTextGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			adapter.setMaxTextLength(5);
			wvMain.setViewAdapter(adapter);
			wvMain.setVisibleItems(5);
			wvMain.setCurrentItem(currentItem);
			wvMain.addScrollingListener(new WheelView.OnWheelScrollListener() {

				@Override
				public void onScrollingStarted(WheelView wheel) {

				}

				@Override
				public void onScrollingFinished(WheelView wheel) {
					if (adapter.getValue(wheel.getCurrentItem()) == null) {
						if (wheel.getCurrentItem() + 1 < adapter.getItemsCount()) {
							wheel.setCurrentItem(wheel.getCurrentItem() + 1, true);
						} else if (wheel.getCurrentItem() - 1 > -1) {
							wheel.setCurrentItem(wheel.getCurrentItem() - 1, true);
						}
					}
					List<MyPair<String, RfTypeDTO>> listSub = mapSub.get(wheel.getCurrentItem());
					final ArrayPairWheelAdapter<RfTypeDTO> adapterSub = new ArrayPairWheelAdapter<RfTypeDTO>(activity, listSub);
					adapterSub.setTextSize(textSize);
					adapterSub.setTextGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					adapterSub.setMaxTextLength(8);
					wvSub.setViewAdapter(adapterSub);
					wvSub.setVisibleItems(5);
					if (adapterSub.getItemsCount() > 0) {
						wvSub.setCurrentItem(0);
					}
				}
			});

			LinearLayout.LayoutParams paramsWheel = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
			wvMain.setLayoutParams(paramsWheel);
			LinearLayout.LayoutParams paramsWheelSub = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
			wvSub.setLayoutParams(paramsWheelSub);

			final Button btCancel = new Button(activity);
			btCancel.setText("取消");
			btCancel.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btCancel.setBackgroundResource(R.drawable.button_black);
			btCancel.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsCancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			final Button btOk = new Button(activity);
			btOk.setText("确认");
			btOk.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btOk.setBackgroundResource(R.drawable.button_red);
			btOk.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btOk.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsOk = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsOk.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			RelativeLayout rlButton = new RelativeLayout(activity);
			rlButton.setBackgroundResource(R.drawable.bg_wheel_dialog_top);
			rlButton.addView(btCancel, paramsCancel);
			rlButton.addView(btOk, paramsOk);
			rlButton.setPadding(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin));
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin * 2));
			llWheel.addView(wvMain, paramsWheel);
			llWheel.addView(wvSub, paramsWheelSub);

			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(rlButton);
			llMain.addView(llWheel);

			final CustomDialog dialog = new CustomDialog(activity);
			dialog.setContentView(llMain);
			dialog.setWidth(LayoutParams.FILL_PARENT);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setGravity(Gravity.BOTTOM);
			dialog.setLocation(0, 0);
			dialog.setAnimation(R.style.Animation_Bottom);

			btCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					dialog.dismiss();
					if (listener != null && adapter.getValue(wvMain.getCurrentItem()) != null) {
						RfTypeDTO[] values = new RfTypeDTO[2];
						values[0] = adapter.getValue(wvMain.getCurrentItem());
						if (wvSub.getViewAdapter().getItemsCount() > 0) {
							ArrayPairWheelAdapter<RfTypeDTO> adapterSub = (ArrayPairWheelAdapter<RfTypeDTO>) wvSub.getViewAdapter();
							values[1] = adapterSub.getValue(wvSub.getCurrentItem());
						}
						listener.onSelected(values);
					}
				}
			});

			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int errorPopCount = 0;

	// 显示错误网络信息的提示框
	public static void showErrorNetPopWind(final Activity activity, final View.OnClickListener listener, String Msg) {
		// 控制最多只显示一个出错框
		if (errorPopCount > 0) {
			return;
		}
		// -----------------------
		try {

			LayoutInflater layoutInflater = (LayoutInflater) (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
			View contentWindow = layoutInflater.inflate(R.layout.error_net_popwind, null);
			TextView tv = (TextView) contentWindow.findViewById(R.id.error_net_tv);
			tv.setText(Msg);
			if (listener == null) {
				contentWindow.findViewById(R.id.error_net_click_tv).setVisibility(View.GONE);
			}
			final PopupWindow pop = new PopupWindow(contentWindow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOutsideTouchable(true);
			pop.setFocusable(true);
			pop.setClippingEnabled(true);
			contentWindow.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					pop.dismiss();
					if (listener != null) {
						listener.onClick(v);
					}
				}
			});
			pop.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					errorPopCount--;
				}
			});
			errorPopCount++;
			pop.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER | Gravity.CENTER, 0, 0);

		} catch (Exception e) {
			errorPopCount = 0;
		}

	}

	// 菜品评分对话框
	// 返回菜品评分结果， 1:喜欢 2:一般 3:不喜欢
	public static void showFoodScoreDialog(Activity activity, final GeneralCallback callBack) {
		DialogUtil.showDialog(activity, R.layout.dialog_food_score, new DialogUtil.DialogEventListener() {

			@Override
			public void onInit(View contentView, final PopupWindow dialog) {
				Button like = (Button) contentView.findViewById(R.id.food_like);
				Button normal = (Button) contentView.findViewById(R.id.food_normal);
				Button dislike = (Button) contentView.findViewById(R.id.food_dislike);

				// 喜欢
				like.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						returnScore(1);
						dialog.dismiss();
					}
				});
				// 一般
				normal.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						returnScore(2);
						dialog.dismiss();
					}
				});
				// 不喜欢
				dislike.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						returnScore(3);
						dialog.dismiss();
					}
				});
				// //---
				// dialog.setFocusable(false);
				// contentView.setFocusable(true);
				// contentView.setFocusableInTouchMode(true);
				// Log.e("----------***","*******"+contentView.requestFocus());
				// contentView.setOnKeyListener(new OnKeyListener() {
				//
				// @Override
				// public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Log.e("----------***","*******");
				// return true;
				// }
				// });

			}

			void returnScore(int score) {
				Bundle data = new Bundle();
				data.putInt("score", score);
				callBack.run(data);

			}
		});
	}

	// 网络错误提示toast
	private static void makeErrorNetToast(Context ctx, String Msg) {
		// 自定义Toast内容
		LayoutInflater layoutInflater = (LayoutInflater) (ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		View contentView = layoutInflater.inflate(R.layout.error_net_popwind, null);
		// 自定义Toast设置
		TextView tv = (TextView) contentView.findViewById(R.id.error_net_tv);
		TextView clickTv = (TextView) contentView.findViewById(R.id.error_net_click_tv);
		clickTv.setVisibility(View.GONE);
		tv.setText(Msg);
		Toast toast = new Toast(ctx);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(contentView);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showErrorNetToast(Context ctx, String Msg) {
		if (lastTime == 0) {
			makeErrorNetToast(ctx, Msg);
			lastTime = System.currentTimeMillis();
		}
		long thisTime = System.currentTimeMillis();
		if (thisTime - lastTime > TOAST_INTERVAL) {
			makeErrorNetToast(ctx, Msg);
			lastTime = thisTime;
		}
	}

	/**
	 * 一个通用的callback，使用者双方可以根据约定来使用不同功能的回调方法
	 */
	public static abstract class GeneralCallback {
		public void run() {
		}

		//
		public void run(Bundle data) {
		}
	}

	/*
	 * //显示当前位置的图层 public static void showLocationPopWindow(final Context
	 * context,View parent,Runnable callback) { MyLocationInfoView
	 * locContentView=new MyLocationInfoView(context);
	 * locContentView.setCallback(callback);
	 * 
	 * PopupWindow popMain = new
	 * PopupWindow(locContentView,LayoutParams.FILL_PARENT,
	 * UnitUtil.dip2px(45)); popMain.setBackgroundDrawable(new
	 * BitmapDrawable());
	 * 
	 * 
	 * popMain.setFocusable(false); popMain.setClippingEnabled(true);
	 * popMain.setOutsideTouchable(true); popMain.showAtLocation(parent,
	 * Gravity.CENTER | Gravity.BOTTOM,0, 80);
	 * 
	 * }
	 */

	/**
	 * 显示一个简单的列表对话框，可以点击其中一个项。只有取消按钮
	 */
	public static void showListDialog(Activity context, String title, String[] items, android.content.DialogInterface.OnClickListener listener) {
		// -------对话框------
		Builder bd = new Builder(context);

		// 设置title
		bd.setTitle(title);
		bd.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		bd.setItems(items, listener);
		bd.show();
	}

	public static void showDatePickerDlg(final Activity activity, int initYear, int initMonth, int initDay, final OnDateSetListener listener) {
		try {
			int margin = 10;
			final int textSize = 16;

			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
			final int year = calendar.get(Calendar.YEAR);
			final int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);

			final int startYear = year;
			final int endYear = year + 1;

			// 年
			final WheelView wvYear = new WheelView(activity);
			wvYear.setViewAdapter(new NumericWheelAdapter(activity, startYear, endYear, null, "年"));// 设置"年"的显示数据
			if (initYear < startYear || initYear > endYear) {
				wvYear.setCurrentItem(year - startYear);// 初始化时显示的数据
			} else {
				for (int i = startYear; i <= endYear; i++) {
					if (i == initYear) {
						wvYear.setCurrentItem(i - startYear);
						break;
					}
				}
			}

			// 月
			final WheelView wvMonth = new WheelView(activity);
			int minMonth = 1;
			if (initYear == year) {
				minMonth = month + 1;
			}
			wvMonth.setViewAdapter(new NumericWheelAdapter(activity, minMonth, 12, null, "月"));
			// wvMonth.setCyclic(true);
			if (initMonth < minMonth || initMonth > 12) {
				wvMonth.setCurrentItem(minMonth - 1);
			} else {
				for (int i = minMonth; i <= 12; i++) {
					if (i == initMonth) {
						wvMonth.setCurrentItem(i - minMonth);
						break;
					}
				}
			}

			// 日
			final WheelView wvDay = new WheelView(activity);
			// wvDay.setCyclic(true);
			// 判断大小月及是否闰年,用来确定"日"的数据
			wvDay.setViewAdapter(getFutureDayNumericWheelAdapter(activity, initYear, initMonth));
			int minValue = ((NumericWheelAdapter) wvDay.getViewAdapter()).getMinValue();
			int maxValue = ((NumericWheelAdapter) wvDay.getViewAdapter()).getMaxValue();
			if (initDay < minValue || initDay > maxValue) {
				wvDay.setCurrentItem(day - 1);
			} else {
				for (int i = minValue; i <= maxValue; i++) {
					if (i == initDay) {
						wvDay.setCurrentItem(i - minValue);
						break;
					}
				}
			}

			// 添加"年"监听
			OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					int year_num = newValue + startYear;
					if (year_num == year) {
						wvMonth.setViewAdapter(new NumericWheelAdapter(activity, month + 1, 12, null, "月"));
					} else {
						wvMonth.setViewAdapter(new NumericWheelAdapter(activity, 1, 12, null, "月"));
					}
					int month_num = ((NumericWheelAdapter) wvMonth.getViewAdapter()).getValue(wvMonth.getCurrentItem());
					// 判断大小月及是否闰年,用来确定"日"的数据
					wvDay.setViewAdapter(getFutureDayNumericWheelAdapter(activity, year_num, month_num));
					if (wvMonth.getCurrentItem() >= wvMonth.getViewAdapter().getItemsCount()) {
						wvMonth.setCurrentItem(wvMonth.getViewAdapter().getItemsCount() - 1);
					}
					if (wvDay.getCurrentItem() >= wvDay.getViewAdapter().getItemsCount()) {
						wvDay.setCurrentItem(wvDay.getViewAdapter().getItemsCount() - 1);
					}
				}
			};
			// 添加"月"监听
			OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					int year_num = ((NumericWheelAdapter) wvYear.getViewAdapter()).getValue(wvYear.getCurrentItem());
					int month_num = ((NumericWheelAdapter) wheel.getViewAdapter()).getValue(newValue);
					// 判断大小月及是否闰年,用来确定"日"的数据
					wvDay.setViewAdapter(getFutureDayNumericWheelAdapter(activity, year_num, month_num));
					if (wvDay.getCurrentItem() >= wvDay.getViewAdapter().getItemsCount()) {
						wvDay.setCurrentItem(wvDay.getViewAdapter().getItemsCount() - 1);
					}
				}
			};
			wvYear.addChangingListener(wheelListener_year);
			wvMonth.addChangingListener(wheelListener_month);

			LinearLayout.LayoutParams paramsWheelYear = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
			wvYear.setLayoutParams(paramsWheelYear);
			LinearLayout.LayoutParams paramsWheelMonth = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvMonth.setLayoutParams(paramsWheelMonth);
			LinearLayout.LayoutParams paramsWheelDay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			wvDay.setLayoutParams(paramsWheelDay);

			final Button btCancel = new Button(activity);
			btCancel.setText("取消");
			btCancel.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btCancel.setBackgroundResource(R.drawable.button_black);
			btCancel.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsCancel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsCancel.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			final Button btOk = new Button(activity);
			btOk.setText("确认");
			btOk.setPadding(UnitUtil.dip2px(10), UnitUtil.dip2px(5), UnitUtil.dip2px(10), UnitUtil.dip2px(6));
			btOk.setBackgroundResource(R.drawable.button_red);
			btOk.setTextColor(activity.getResources().getColor(R.color.text_color_white));
			btOk.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			RelativeLayout.LayoutParams paramsOk = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			paramsOk.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			RelativeLayout rlButton = new RelativeLayout(activity);
			rlButton.setBackgroundResource(R.drawable.bg_wheel_dialog_top);
			rlButton.addView(btCancel, paramsCancel);
			rlButton.addView(btOk, paramsOk);
			rlButton.setPadding(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin));
			LinearLayout llWheel = new LinearLayout(activity);
			llWheel.setOrientation(LinearLayout.HORIZONTAL);
			llWheel.setPadding(UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin), UnitUtil.dip2px(margin * 2));
			llWheel.addView(wvYear, paramsWheelYear);
			llWheel.addView(wvMonth, paramsWheelMonth);
			llWheel.addView(wvDay, paramsWheelDay);

			LinearLayout llMain = new LinearLayout(activity);
			llMain.setOrientation(LinearLayout.VERTICAL);
			llMain.setBackgroundResource(R.drawable.bg_wheel_dialog);
			llMain.addView(rlButton);
			llMain.addView(llWheel);

			final CustomDialog dialog = new CustomDialog(activity);
			dialog.setContentView(llMain);
			dialog.setWidth(LayoutParams.FILL_PARENT);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setGravity(Gravity.BOTTOM);
			dialog.setLocation(0, 0);
			dialog.setAnimation(R.style.Animation_Bottom);

			btCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					dialog.dismiss();
					if (listener != null) {
						int year = ((NumericWheelAdapter) wvYear.getViewAdapter()).getValue(wvYear.getCurrentItem());
						int monthOfYear = ((NumericWheelAdapter) wvMonth.getViewAdapter()).getValue(wvMonth.getCurrentItem());
						int dayOfMonth = ((NumericWheelAdapter) wvDay.getViewAdapter()).getValue(wvDay.getCurrentItem());
						listener.onDateSet(null, year, monthOfYear, dayOfMonth);
					}
				}
			});

			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static NumericWheelAdapter getFutureDayNumericWheelAdapter(Activity activity, int year, int month) {
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		final int yearCurrent = calendar.get(Calendar.YEAR);
		final int monthCurrent = calendar.get(Calendar.MONTH);
		int dayCurrent = calendar.get(Calendar.DATE);

		// 判断大小月及是否闰年,用来确定"日"的数据
		int minDay = 1;
		if (year == yearCurrent && month == (monthCurrent + 1)) {
			minDay = dayCurrent;
		}
		if (list_big.contains(String.valueOf(month))) {
			return new NumericWheelAdapter(activity, minDay, 31, null, "日");
		} else if (list_little.contains(String.valueOf(month))) {
			return new NumericWheelAdapter(activity, minDay, 30, null, "日");
		} else {
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
				return new NumericWheelAdapter(activity, minDay, 29, null, "日");
			} else {
				return new NumericWheelAdapter(activity, minDay, 28, null, "日");
			}
		}
	}

	public interface OnShRegionSelectedListener {
		public void onShRegionSelected(boolean isNearby, String regionName, String districtName);
	}

	public static void showShRegionSelector(final Activity activity, final OnShRegionSelectedListener listener) {
		ShRegionListDTO dto = SessionManager.getInstance().getShRegionListDTO().clone();
		if (dto == null) {
			return;
		}
		final RealTimeResFilter filter = SessionManager.getInstance().getRealTimeResFilter();
		List<RfTypeListDTO> mAreaList = new ArrayList<RfTypeListDTO>();
		int mSelectedRegion;
		int mSelectedDistrict;

		// 全部地域
		RfTypeListDTO allRegionDto = new RfTypeListDTO();
		allRegionDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		allRegionDto.setName("全部地域");

		// 附近搜索
		RfTypeListDTO nearbyDto = new RfTypeListDTO();
		nearbyDto.setUuid(TAG_TYPE_NEARBY);
		nearbyDto.setName("附近");

		RfTypeDTO emptyDto = new RfTypeDTO();
		emptyDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
		emptyDto.setName("");

		if (dto.getList() == null) {
			dto.setList(new ArrayList<RfTypeListDTO>());
		}
		dto.getList().add(0, allRegionDto);
		dto.getList().add(0, nearbyDto);

		for (RfTypeListDTO ctld : dto.getList()) {
			if (ctld.getList() == null) {
				ctld.setList(new ArrayList<RfTypeDTO>());
			}
			if (ctld.getUuid().equals(String.valueOf(Settings.STATUTE_ALL)) || ctld.getUuid().equals(TAG_TYPE_NEARBY)) {
				ctld.getList().add(0, emptyDto);
			} else {
				// 全部子商区
				RfTypeDTO allSubDto = new RfTypeDTO();
				allSubDto.setUuid(String.valueOf(Settings.STATUTE_ALL));
				allSubDto.setName("全部" + ctld.getName());
				ctld.getList().add(0, allSubDto);
			}
			mAreaList.add(ctld);
		}

		if (filter.getDistanceMeter() != 0) {
			mSelectedRegion = 0;
			mSelectedDistrict = 0;
		} else {
			if (filter.getRegionId().equals(String.valueOf(Settings.STATUTE_ALL))) {
				// 不存在选中的区域
				mSelectedRegion = 1;
				mSelectedDistrict = 0;
			} else {
				mSelectedRegion = getPositionInRfTypeListDTOList(mAreaList, filter.getRegionId());
				if (filter.getDistrictId().equals(String.valueOf(Settings.STATUTE_ALL))) {
					// 不存在选中的子商区
					mSelectedDistrict = 0;
				} else {
					mSelectedDistrict = getPositionInRfTypeDTOList(mAreaList.get(mSelectedRegion).getList(), filter.getDistrictId());
				}
			}
		}

		DialogUtil.showTwoWheelsDialog(activity, mAreaList, mSelectedRegion, mSelectedDistrict, new DialogUtil.OnWheelSelectedListener() {

			@Override
			public void onSelected(final RfTypeDTO[] values) {
				try {
					if (values == null || values.length != 2 || values[0] == null) {
						return;
					}
					if (values[0].getUuid().equals(TAG_TYPE_NEARBY)) {
						filter.setDistanceMeter(DEFAULT_DISTANCE);
						filter.setRegionId(String.valueOf(Settings.STATUTE_ALL));
						filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
						if (listener != null) {
							listener.onShRegionSelected(true, "", "");
						}
					} else {
						if (values[1] == null) {
							filter.setDistanceMeter(0);
							filter.setRegionId(values[0].getUuid());
							filter.setDistrictId(String.valueOf(Settings.STATUTE_ALL));
							if (listener != null) {
								listener.onShRegionSelected(false, values[0].getName(), "");
							}
						} else {
							filter.setDistanceMeter(0);
							filter.setRegionId(values[0].getUuid());
							filter.setDistrictId(values[1].getUuid());
							if (listener != null) {
								if (filter.getDistrictId().equals(String.valueOf(Settings.STATUTE_ALL))) {
									listener.onShRegionSelected(false, values[0].getName(), "");
								} else {
									listener.onShRegionSelected(false, values[0].getName(), values[1].getName());
								}
							}
						}
					}
				} catch (Exception e) {
					LogUtils.logE(TAG, e);
				}
			}
		});
	}

	private static int getPositionInRfTypeDTOList(List<RfTypeDTO> list, String id) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUuid().equals(id)) {
				return i;
			}
		}
		return 0;
	}

	private static int getPositionInRfTypeListDTOList(List<RfTypeListDTO> list, String id) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUuid().equals(id)) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * 日期时间选择对话框
	 * 
	 * @param ctx
	 * @param mCalendar
	 *            传入时间
	 * @param listener
	 *            回调监听
	 */
	public static void showDateTimeSelectDialog(final Context context, Calendar mCalendar, final DateTimeSelectDialogListener listener) {
		// 时间与日期相关
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH) + 1;
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
		mMinute = mCalendar.get(Calendar.MINUTE);
		mDayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
		strDayName = DAT_OF_WEEK[mDayOfWeek - 1];

		final CustomDialog dialog = new CustomDialog(context);
		dialog.setGravity(Gravity.CENTER);
		View layout = LayoutInflater.from(context).inflate(R.layout.dilag_select_date_item, null);
		dialog.setContentView(layout);

		// 组件
		final TextView weekTv = (TextView) layout.findViewById(R.id.dialog_select_date_name_TV);
		final TextView dateTv = (TextView) layout.findViewById(R.id.dialog_select_date_info_TV);
		final TextView timeTv = (TextView) layout.findViewById(R.id.dialog_select_timeTv);
		ViewGroup dateSelect = (ViewGroup) layout.findViewById(R.id.dialog_select_date);
		ViewGroup timeSelect = (ViewGroup) layout.findViewById(R.id.dialog_select_right_time);
		Button submitBtn = (Button) layout.findViewById(R.id.dialog_select_Btn);
		ImageView canCelBtn = (ImageView) layout.findViewById(R.id.dialog_select_X_btn);
		// 日期选择
		dateSelect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				DialogUtil.showDatePickerDlg((Activity) context, mYear, mMonth, mDay, new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						mYear = year;
						mMonth = monthOfYear;
						mDay = dayOfMonth;

						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
						calendar.set(mYear, mMonth - 1, mDay);
						mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
						String tempDayName = DAT_OF_WEEK[mDayOfWeek - 1];
						if (tempDayName.equals(strDayName)) {
							weekTv.setText("今天");
						} else {
							weekTv.setText(tempDayName);
						}

						dateTv.setText(getAddZeroTime(mMonth) + "月" + getAddZeroTime(mDay) + "日");

					}
				});
			}
		});
		// 时间选择
		timeSelect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				new MyTimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						mHour = hourOfDay;
						mMinute = minute;
						timeTv.setText(getAddZeroTime(mHour) + ":" + getAddZeroTime(mMinute));
					}
				}, mHour, mMinute, true).show();

			}
		});
		// 确定按钮点击事件
		submitBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				dialog.dismiss();
				Calendar calResult = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
				calResult.set(mYear, mMonth - 1, mDay, mHour, mMinute);
				if (listener != null) {
					listener.onFinishSelect(calResult);
				}

			}
		});
		// 取消按钮事件
		canCelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onCancel();
				}

			}
		});

		dateTv.setText(getAddZeroTime(mMonth) + "月" + getAddZeroTime(mDay) + "日");
		timeTv.setText(getAddZeroTime(mHour) + ":" + getAddZeroTime(mMinute));
		Calendar mRealcal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		int year = mRealcal.get(Calendar.YEAR);
		int month = mRealcal.get(Calendar.MONTH) + 1;
		int day = mRealcal.get(Calendar.DAY_OF_MONTH);
		if (year == mYear && month == mMonth && day == mDay) {
			weekTv.setText("今天");
		} else {
			weekTv.setText(DAT_OF_WEEK[mDayOfWeek - 1]);
		}
		dialog.show();
	}

	public interface DateTimeSelectDialogListener {
		public void onFinishSelect(Calendar calendar);

		public void onCancel();
	}

	/**
	 * 格式化时间
	 */
	private static String getAddZeroTime(int time) {
		String strTime;
		if (time < 10) {
			strTime = "0" + time;
		} else {
			strTime = String.valueOf(time);
		}
		return strTime;
	}

	public static Dialog showChooseRestDialog(final Context context, RealTimeResAdapter adapter, final DialogInterface.OnCancelListener onCancelListener) {
		final CustomDialog dialog = new CustomDialog(context);
		dialog.setGravity(Gravity.CENTER);
		WindowManager.LayoutParams dialogParams = dialog.getLayoutParams();
		dialogParams.y = dialog.getY() - 40;
		dialog.setLayoutParams(dialogParams);
		View layout = LayoutInflater.from(context).inflate(R.layout.dilog_chose_rest, null);
		dialog.setContentView(layout);
		// 组件
		ListView chooseList = (ListView) layout.findViewById(R.id.dilog_chose_rest_list);
		ImageView canCelBtn = (ImageView) layout.findViewById(R.id.dialog_chose_rest_X_btn);
		canCelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (onCancelListener != null) {
					onCancelListener.onCancel(dialog);
				}
			}
		});
		if (adapter != null) {
			chooseList.setAdapter(adapter);
		}
		dialog.show();
		return dialog;
	}

	/**
	 * 显示筛选双列表下拉框
	 * 
	 */
	public static void showSelectionListViewDropDown(View anchor, List<? extends ItemData> listData, final SelectionListView.OnSelectedListener selectedListener,
			PopupWindow.OnDismissListener dismissListener) {
		final Context context = ContextUtil.getContext();
		try {
			SelectionListView slv = new SelectionListView(context);
			LinearLayout content = new LinearLayout(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			lp.setMargins(0, 0, 0, UnitUtil.dip2px(80));
			content.addView(slv, lp);
			// --
			final PopupWindow window = new PopupWindow(context);
			window.setOutsideTouchable(false);
			window.setFocusable(true);
			window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
			window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
			window.setContentView(content);
			window.setBackgroundDrawable(new ColorDrawable(0x66000000));
			window.setAnimationStyle(R.style.animation_drop_down);
			window.showAsDropDown(anchor, 0, -UnitUtil.dip2px(2));
			window.setOnDismissListener(dismissListener);
			// --
			slv.setData(listData);
			slv.setOnSelectedListener(new OnSelectedListener() {

				@Override
				public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
					window.dismiss();
					if (selectedListener != null) {
						// DialogUtil.showToast(context,mainData.getUuid()+","+mainData.getName()+" | "+(subData==null?null:(subData.getUuid()+","+subData.getName())));
						selectedListener.onSelected(mainData, subData, mainPosition, subPosition);
					}
				}
			});

		} catch (Exception e) {
		}
	}

	/**
	 * 显示筛选双列表下拉框
	 * 
	 */
	public static void showSelectionLinkListViewDropDown(View anchor, List<? extends ItemData> listData, final SelectionListView.OnSelectedListener selectedListener,
			PopupWindow.OnDismissListener dismissListener) {
		final Context context = ContextUtil.getContext();
		try {
			SelectionLinkListView slv = new SelectionLinkListView(context);
			LinearLayout content = new LinearLayout(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

			lp.setMargins(0, 0, 0, UnitUtil.dip2px(80));
			content.addView(slv, lp);
			// --
			final PopupWindow window = new PopupWindow(context);
			window.setOutsideTouchable(false);
			window.setFocusable(true);
			window.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
			window.setHeight(ViewGroup.LayoutParams.FILL_PARENT);
			window.setContentView(content);
			window.setBackgroundDrawable(new ColorDrawable(0x66000000));
			window.setAnimationStyle(R.style.animation_drop_down);
			window.showAsDropDown(anchor, 0, -UnitUtil.dip2px(2));
			window.setOnDismissListener(dismissListener);
			// --
			slv.setData(listData);
			slv.setOnSelectedListener(new OnSelectedListener() {

				@Override
				public void onSelected(ItemData mainData, ItemData subData, int mainPosition, int subPosition) {
					window.dismiss();
					if (selectedListener != null) {
						// DialogUtil.showToast(context,(mainData==null?null:(mainData.getUuid()+","+mainData.getName()))+" | "+(subData==null?null:(subData.getUuid()+","+subData.getName())));
						selectedListener.onSelected(mainData, subData, mainPosition, subPosition);
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
