package com.fg114.main.app.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.activity.chat.XiaomishuChat;

public class DialogRemindFloatWindow {

	private final String TAG = DialogRemindFloatWindow.class.getName();

	private Context context;

	public static int currMsgNumber = 0;

	private static final int msgNumberWidth = 0;

	private boolean isShowing = false;

	public static WindowManager wm;
	public static WindowManager.LayoutParams params;

	int lastX;
	int lastY;
	int paramX;
	int paramY;
	int dx;
	int dy;
	boolean positionMovedFlag = false;

	public static RelativeLayout layout;

	private Button mainBtn;

	private TextView msgNumber;

	private static DialogRemindFloatWindow floatWindow;

	public static DialogRemindFloatWindow getInstance(Context ctx) {
		if (floatWindow == null) {
			floatWindow = new DialogRemindFloatWindow(ctx);
		}

		return floatWindow;
	}

	private DialogRemindFloatWindow(Context ctx) {
		context = ctx;

		layout = (RelativeLayout) LinearLayout.inflate(context,
				R.layout.dialog_remind_float_layout, null);

		mainBtn = (Button) layout.findViewById(R.id.main_btn);

		// mainBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ViewUtils.preventViewMultipleClick(v, 1000);
		// showDialog();
		// }
		// });

		msgNumber = (TextView) layout.findViewById(R.id.message_number);
		msgNumber.setOnTouchListener(touchListener);

		wm = (WindowManager) context.getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();

		// 设置window type
		params.type = WindowManager.LayoutParams.TYPE_PHONE;

		params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

		// 设置Window flag
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		params.gravity = Gravity.CENTER;
		params.x = 0;
		params.y = 0;

		// 设置悬浮窗的长得宽
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;

		// 加入OnTouchListener后，则可以自由拖动悬浮窗
		mainBtn.setOnTouchListener(touchListener);
	}

	private View.OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:

				lastX = (int) event.getRawX();

				lastY = (int) event.getRawY();

				paramX = params.x;

				paramY = params.y;

				Log.w("down:", "lastX:" + lastX + " lastY:" + lastY
						+ " paramX:" + paramX + " paramY:" + paramY);

				break;

			case MotionEvent.ACTION_MOVE:

				dx = (int) event.getRawX() - lastX;

				dy = (int) event.getRawY() - lastY;

				if ((Math.abs(dx) > 15) || (Math.abs(dy) > 15)) {

					positionMovedFlag = true;

					params.x = paramX + dx;

					params.y = paramY + dy;

					// 更新悬浮窗位置

					wm.updateViewLayout(layout, params);

				}

				break;

			case MotionEvent.ACTION_UP:

				if ((Math.abs(dx) < 15) && (Math.abs(dy) < 15)
						&& (!positionMovedFlag)) {// 如果移动距离太小，则视为点击

					showDialog();

				}

				positionMovedFlag = false;

				break;

			}

			return true;

		}

	};

	public void showMessageNumber(int number) {
		if (number < 1) {
			return;
		}
		if (XiaomishuChat.getInstance(context).getDialogState() != XiaomishuChat.DIALOG_STATE_HIDING) {
			// 只有当dialog处于最小化状态，即新消息提醒控件显示的时候才刷新，否则会报错
			return;
		}
		currMsgNumber += number;
		if (msgNumber.getVisibility() == View.GONE) {
			params.x += msgNumberWidth / 2;
		}
		msgNumber.setVisibility(View.VISIBLE);
		msgNumber.setText(currMsgNumber + "");

		wm.updateViewLayout(layout, params);
	}

	public void hideMessageNumber() {
		if (XiaomishuChat.getInstance(context).getDialogState() != XiaomishuChat.DIALOG_STATE_HIDING) {
			// 只有当dialog处于最小化状态，即新消息提醒控件显示的时候才刷新，否则会报错
			return;
		}
		currMsgNumber = 0;
		if (msgNumber.getVisibility() == View.VISIBLE) {
			params.x -= msgNumberWidth / 2;
		}
		msgNumber.setVisibility(View.GONE);
		wm.updateViewLayout(layout, params);
	}

	private void showDialog() {
		if (!XiaomishuChat.getInstance(context).isShowing()) {
			this.hide();
			XiaomishuChat.getInstance(context).maximizeChatDialog(false);
		}
	}

	public void show() {
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.sendToTarget();
	}

	public void hide() {
		Message msg = mHandler.obtainMessage();
		msg.what = 2;
		msg.sendToTarget();
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				if (!isShowing) {
					wm.addView(layout, params);
					isShowing = true;
				}
			} else {
				if (isShowing) {
					currMsgNumber = 0;
					wm.removeView(layout);
					isShowing = false;
				}
			}

			super.handleMessage(msg);
		}
	};
}
