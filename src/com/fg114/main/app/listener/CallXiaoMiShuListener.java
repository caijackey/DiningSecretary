package com.fg114.main.app.listener;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.fg114.main.R;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.SharedprefUtil;

/**
 * 呼叫订餐小秘书
 * @author zhangyifan
 *
 */
public class CallXiaoMiShuListener implements OnClickListener {
	
	private static int HANDLE_POST_OVER = 99;
	
	private Activity activity;
	private String resId;
	private PopupWindow win;
	private Call57DialogListener dialogListener;
	
	public CallXiaoMiShuListener(Activity activity,Call57DialogListener dialogListener) {
		this.activity = activity;
		this.resId = "";
		this.dialogListener=dialogListener;
	}
	
	public CallXiaoMiShuListener(Activity activity, PopupWindow win, String RestaurantId,Call57DialogListener dialogListener) {
		this.activity = activity;
		this.resId = RestaurantId;
		this.win = win;
		this.dialogListener=dialogListener;
	}
	
	@Override
	public void onClick(View v) {
		if (this.win != null) {
			this.win.dismiss();
		}
		
		
		if(dialogListener!=null){
			dialogListener.beforeShow();
		}
		DialogUtil.showAlert(activity, true, activity.getString(R.string.text_info_call_xiaomishu), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						ActivityUtil.callSuper57(activity,
								Fg114Application.super57PhoneNumber);
						
//						final Handler callPostOverListener = new Handler() {
//							@Override
//							public void handleMessage(Message msg) {
//								if (msg.what == HANDLE_POST_OVER) {
//									//拨打小秘书
//									ActivityUtil.callSuper57(activity,
//											Fg114Application.super57PhoneNumber);
//								}
//								super.handleMessage(msg);
//							}
//						};
						
//						callPostOverListener.sendEmptyMessage(HANDLE_POST_OVER);
						
						final String userPhone;
						// 确定拨打的用户手机号
						if (SessionManager.getInstance().isUserLogin(activity)) {
							userPhone = SessionManager.getInstance().getUserInfo(activity).getTel();
						}
						else {
							userPhone = SharedprefUtil.get(activity, Settings.ANONYMOUS_TEL, "");
						}
						
						final String invokeLocation;
						if (win != null) {
							invokeLocation = Settings.POST_RES_RESERVE;
						}
						else {
							invokeLocation = Settings.BOTTOM;
						}
						
						new Runnable() {
							public void run() {
								try {
									ServiceRequest.callTel(1,resId,Fg114Application.super57PhoneNumber);
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
//									DialogUtil.showToast(activity, "Finish call api");
//									callPostOverListener.sendEmptyMessage(HANDLE_POST_OVER);
								}
							}
						}.run();

						dialog.cancel();
						if(dialogListener!=null){
							dialogListener.afterClose();
						}
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(dialogListener!=null){
							dialogListener.afterClose();
						}
						dialog.cancel();
					}
				});
	}
	public interface Call57DialogListener{
		public void beforeShow();
		public void afterClose();
	}

}
