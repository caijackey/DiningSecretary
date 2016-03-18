package com.fg114.main.app.activity.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fg114.main.R;
import com.fg114.main.analytics.dto.PageStatsData;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.view.ChatWebView;
import com.fg114.main.app.view.DialogRemindFloatWindow;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.ViewUtils;

/**
 * 聊天
 * 
 * @author caijie
 * 
 */
public class XiaomishuChat {
	private Button closeBtn;
	private EditText contentText;
	private Button sendMsgBtn;
	private ChatWebView mWebView;
	private String webSite;
	private Context context;
	private PopviewChooseDialog chooseDialog;

	// 聊天状态 1：正在显示 2：最小化 3：退出
	private int dialogState;
	public static final int DIALOG_STATE_SHOWING = 1;
	public static final int DIALOG_STATE_HIDING = 2;
	public static final int DIALOG_STATE_CLOSING = 3;
	// 是否被服务器强制退出房间
	private boolean roomIsClosedByServer = false;

	// private final String ASSETS_PATH = "localWeb";
	private final String WEB_INDEX = "index.htm";

	private static XiaomishuChat chatDialog;

	private final int PAGECLICK_COUNTS = 5;// 点击流
	private static boolean initMark;

	private boolean isShowing = false;

	private Dialog dialog;
	private LayoutParams params;
	private static RelativeLayout layout;

	/**
	 * 饿汉式
	 * 
	 * @param ctx
	 * @return
	 */
	public static XiaomishuChat getInstance(Context ctx) {
		if (chatDialog == null
				|| chatDialog.dialogState == DIALOG_STATE_CLOSING
				|| initMark == false) {
			chatDialog = new XiaomishuChat(ctx);
		}

		initMark = true;

		return chatDialog;
	}

	public static XiaomishuChat getInstance() {
		return chatDialog;
	}

	public static boolean setIsInit(boolean isInit) {
		return initMark = isInit;
	}

	public static boolean getIsInit() {
		return initMark;
	}

	/**
	 * construct
	 * 
	 * @param context
	 */
	private XiaomishuChat(Context context) {

		this.context = context;
		layout = (RelativeLayout) LinearLayout.inflate(context,
				R.layout.xiaomishu_chat_layout, null);

		initControl();

		initDialog();
	}

	private void initDialog() {
		dialog = new Dialog(MainFrameActivity.getCurrentTopActivity(),
				R.style.Custom_Dialog);
		dialog.getWindow().setGravity(Gravity.TOP);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		dialog.setContentView(layout);
		// 全屏
		LayoutParams lay = dialog.getWindow().getAttributes();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		lay.width = dm.widthPixels;
	}

	public interface FscadeListen {
		void hideChatdialog();

		void exitChatDialog();

		void getPageState();
	}

	private void initControl() {
		closeBtn = (Button) layout.findViewById(R.id.close_chat);
		sendMsgBtn = (Button) layout.findViewById(R.id.send_sms);
		contentText = (EditText) layout.findViewById(R.id.sms_content);
		// contentText.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// if (!hasFocus) {
		// ViewUtils.hideSoftInput(context, v);
		// }
		// }
		// });

		mWebView = (ChatWebView) layout.findViewById(R.id.simple_webview);

		// 复制所有网页
		// try {
		// copyToSdcard(context, ASSETS_PATH, LOCAL_HTML_PATH);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }

		// copyZipFile(zipPackageName,
		// getClass().getResourceAsStream("/assets/" + zipPackageName),
		// LOCAL_HTML_PATH);

		String str = "http://222.73.28.48/tech.down.sina.com.cn/20131031/59c2d797/winzip175zh-32.msi?fn=&ssig=8HGHNqzFiq&Expires=1408155873&KID=sae,230kw3wk15&ip=1408076673,222.66.142.230&corp=1";
		// downloadZip(str);
		// DownloadAsyncTask downloadTask = new DownloadAsyncTask();
		// downloadTask.execute(str);

		// close
		closeBtn.setOnClickListener(getClickListener());

		// send
		sendMsgBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				String txt = contentText.getText().toString();
				if (!CheckUtil.isEmpty(txt)) {
					if (txt.contains("\\")) {
						DialogUtil.showAlert(context, "警告", "输入中有非法字符，请检查！");
						return;
					}
					mWebView.sendMessage(txt);
					contentText.setText("");
				}
			}
		});
	}

	private View.OnClickListener getClickListener() {
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (roomIsClosedByServer) {// 如果房间已经被服务端关闭，则点击关闭按钮时直接关闭对话框
					exitChatdialog();
					return;
				}
				chooseDialog = new PopviewChooseDialog(
						MainFrameActivity.getCurrentTopActivity(),
						new FscadeListen() {

							@Override
							public void hideChatdialog() {
								// 最小化dialog
								XiaomishuChat.this.hideChatdialog();
							}

							@Override
							public void exitChatDialog() {
								// 退出聊天
								XiaomishuChat.this.exitChatdialog();
							}

							@Override
							public void getPageState() {

							}
						});
				chooseDialog.show();
			}
		};
	}

	/**
	 * 判断键盘是否弹出，如果弹出则将聊天记录面板滚到最底部
	 */
	private ViewTreeObserver.OnGlobalLayoutListener globaleLayoutListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			int heightDiff = ((Activity) context).getWindowManager()
					.getDefaultDisplay().getHeight()
					- contentText.getRootView().getHeight();

			if (heightDiff > 100) { // 如果高度差超过100像素，就很有可能是有软键盘...
				mWebView.scrollToBottom();
			}
		}
	};

	/**
	 * 复制单个zip文件（大文件可考虑FileChannel）
	 * 
	 * @param fileName
	 *            文件名
	 * @param in
	 *            输入文件流
	 * @param toPath
	 *            目标路径
	 */
	public final void copyZipFile(String fileName, InputStream in, String toPath) {
		File toFile = new File(toPath);
		if (!toFile.exists()) {
			toFile.mkdirs();
		}

		try {
			File outfile = new File(toFile, fileName);
			if (outfile.exists()) {
				outfile.delete();
			}

			OutputStream out = new FileOutputStream(outfile);
			byte[] buffer = new byte[1024]; // 创建byte数组
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);

			}
			in.close();
			out.close();
		} catch (IOException e) {

		}
	}

	/**
	 * 复制assets下文件夹到SD卡(迭代效率差)
	 * 
	 * @param fromPath
	 *            路径assets下目录
	 * @param toPath
	 *            SD卡路径
	 * @return 文件名String[]
	 * 
	 */
	public final String[] copyToSdcard(Context context, String fromPath,
			String toPath) throws IOException {
		String[] fromHtmlFiles = null;

		File toFile = new File(toPath);
		// 如果不存在，创建文件夹
		if (!toFile.exists()) {
			toFile.mkdirs();
		}

		fromHtmlFiles = context.getResources().getAssets().list(fromPath);

		for (int i = 0; i < fromHtmlFiles.length; i++) {
			String filename = fromHtmlFiles[i];
			if (!filename.contains(".")) {
				if (0 == fromPath.length()) {
					copyToSdcard(context, filename, toPath + filename + "/");
				} else {
					copyToSdcard(context, fromPath + "/" + filename, toPath
							+ filename + "/");
				}
				continue;
			}
			copyFiletoSdcard(toFile, fromPath, filename);
		}

		return fromHtmlFiles;
	}

	public final void copyFiletoSdcard(File toFile, String fromPath,
			String toFileName) throws IOException {
		if (toFile == null) {
			return;
		}

		File outfile = new File(toFile, toFileName);
		if (outfile.exists()) {
			outfile.delete();
		}
		InputStream in = null;
		if (0 != fromPath.length()) {
			in = context.getAssets().open(fromPath + "/" + toFileName);
		} else {
			in = context.getAssets().open(toFileName);
		}
		OutputStream out = new FileOutputStream(outfile);

		byte[] buffer = new byte[1024]; // 创建byte数组
		int len;
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);

		}
		in.close();
		out.close();

	}

	/**
	 * load url
	 * 
	 * @param name
	 */
	public void loadWebviewSite(String name) {
		StringBuilder pathName = new StringBuilder();
		pathName.append("file://");
		pathName.append(Settings.LOCAL_HTML_PATH);
		pathName.append(Settings.ZIP_PATH);
		pathName.append(name);

		webSite = pathName.toString();
		mWebView.loadUrl(webSite);
	}

	private ArrayList<PageStatsData> pageList = new ArrayList<PageStatsData>();

	public void addPageStateData(PageStatsData page) {
		if (dialogState == DIALOG_STATE_HIDING)
			pageList.add(page);
	}

	/**
	 * 去前5条和最后一条点击流
	 * 
	 * @param maxCounts
	 * @return
	 */
	private String getMorePageStateData(int maxCounts) {
		PageStatsData page = null;

		StringBuilder content = new StringBuilder(200);
		for (int i = 0; i < maxCounts; i++) {
			page = pageList.get(i);
			if (i != maxCounts - 1) {
				// 详情页(restId:1212,couponId:,orderId:,inTag:true)
				content.append(page.n);
				content.append("(restId:").append(page.rid).append(",");
				content.append("couponId:").append(page.cid).append(",");
				content.append("orderId:").append(page.oid).append(",");
				content.append("inTag:").append(page.inTag);
				content.append(");");
			} else {
				content.append(page.n);
				content.append("(restId:").append(page.rid).append(",");
				content.append("couponId:").append(page.cid).append(",");
				content.append("orderId:").append(page.oid).append(",");
				content.append("inTag:").append(page.inTag);
				content.append(")|");
			}

		}

		// 最后一条
		page = pageList.get(pageList.size() - 1);
		content.append(page.n);
		content.append("(restId:").append(page.rid).append(",");
		content.append("couponId:").append(page.cid).append(",");
		content.append("orderId:").append(page.oid).append(",");
		content.append("inTag:").append(page.inTag);
		content.append(")");

		return content.toString();
	}

	
	/**
	 * 小于5条的点击流
	 * @param size
	 * @return
	 */
	private String getLessPageStateData(int size) {
		PageStatsData page = null;

		StringBuilder content = new StringBuilder(200);

		// 前几条点击流
		for (int i = 0; i < size; i++) {
			page = pageList.get(i);
			if (i != size - 1) {
				// 详情页(restId:1212,couponId:,orderId:,inTag:true)
				content.append(page.n);
				content.append("(restId:").append(page.rid).append(",");
				content.append("couponId:").append(page.cid).append(",");
				content.append("orderId:").append(page.oid).append(",");
				content.append("inTag:").append(page.inTag);
				content.append(");");
			} else {
				content.append(page.n);
				content.append("(restId:").append(page.rid).append(",");
				content.append("couponId:").append(page.cid).append(",");
				content.append("orderId:").append(page.oid).append(",");
				content.append("inTag:").append(page.inTag);
				content.append(")|");
			}

		}
		// 最后一条
		page = pageList.get(size - 1);
		content.append(page.n);
		content.append("(restId:").append(page.rid).append(",");
		content.append("couponId:").append(page.cid).append(",");
		content.append("orderId:").append(page.oid).append(",");
		content.append("inTag:").append(page.inTag);
		content.append(")");

		return content.toString();

	}

	/**
	 * 处理点击流（取前面n点击流和最后一个）
	 * 
	 * @param n
	 */
	public String dealPageStateData(int n) {
		if (pageList == null)
			return "";
		int size = pageList.size();
		if (size == 0) {
			return "";
		}

		String pageStates = "";
		if (size > n) {
			pageStates = getMorePageStateData(n);
		} else {
			pageStates = getLessPageStateData(size);
		}

		return pageStates;
	}

	/**
	 * 显示本控件
	 */
	private void show() {
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.sendToTarget();
	}

	/**
	 * 隐藏本控件
	 */
	private void hide() {
		Message msg = mHandler.obtainMessage();
		msg.what = 2;
		msg.sendToTarget();
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				if (!isShowing) {
					dialog.show();
					isShowing = true;
				}
			} else {
				if (isShowing) {
					dialog.dismiss();
					isShowing = false;
				}
			}

			super.handleMessage(msg);
		}
	};

	/**
	 * 最大化对话框
	 */
	public final void maximizeChatDialog(boolean forceInit) {
		if (!isShowing()) {
			contentText.getViewTreeObserver().addOnGlobalLayoutListener(
					globaleLayoutListener);
			dialogState = DIALOG_STATE_SHOWING;
			if (!roomIsClosedByServer) {
				enableSendMsg();
			}
			mWebView.sendMsgMaximizeRoom(dealPageStateData(PAGECLICK_COUNTS));
			// 开始记录点击流
			pageList.clear();
			if (forceInit
					|| context != MainFrameActivity.getCurrentTopActivity()) {// 如果已经切到别的activity，则需要新建一个dialog
				((ViewGroup) (layout.getParent())).removeAllViews();
				// closeBtn.setOnClickListener(getClickListener());
				initDialog();
			}
			this.show();
		}
	}

	/**
	 * 最小化对话框
	 */
	public final void hideChatdialog() {
		if (isShowing()) {
			// 最小化的时候要调这个接口对服务器发送最小化消息，否则接收不到新消息提醒
			minimizeDialog();
			dialogState = DIALOG_STATE_HIDING;
			disableSendMsg();
			hide();
			// 开始记录点击流
			pageList.clear();
			DialogRemindFloatWindow.getInstance(context).show();
		}
	}

	/**
	 * 退出房间
	 */
	public final void exitChatdialog() {
		if (isShowing()) {
			// 退出的时候要调这个接口对服务器发送退出消息
			exitRoom();
			dialogState = DIALOG_STATE_CLOSING;
			roomIsClosedByServer = false;
			disableSendMsg();
			hide();
			initMark = false;
		}
	}

	/**
	 * 初始化显示对话框
	 */
	public final void showChatDialog() {
		if (isShowing())
			return;

		loadWebviewSite(WEB_INDEX);

		dealPageStateData(PAGECLICK_COUNTS);
		// pageList.clear();

		contentText.getViewTreeObserver().addOnGlobalLayoutListener(
				globaleLayoutListener);

		dialogState = DIALOG_STATE_SHOWING;
		show();
	}

	public void enableSendMsg() {
		if (roomIsClosedByServer) {
			return;
		}
		contentText.setEnabled(true);
		sendMsgBtn.setClickable(true);
	}

	public void disableSendMsg() {
		contentText.setEnabled(false);
		sendMsgBtn.setClickable(false);
	}

	/**
	 * 当对话窗口最小化的时候要向服务器发最小化请求
	 */
	public void minimizeDialog() {
		mWebView.sendMsgMinimizeRoom();
	}

	/**
	 * 当退出房间(主动)的时候要向服务器发退出房间请求
	 */
	public void exitRoom() {
		mWebView.sendMsgLeaveRoom();
	}

	public boolean rooIsClosedByServer() {
		return roomIsClosedByServer;
	}

	public void setRoomIsClosedByServer(boolean roomIsClosedByServer) {
		this.roomIsClosedByServer = roomIsClosedByServer;
	}

	public int getDialogState() {
		return dialogState;
	}

	public void setDialogState(int dialogState) {
		this.dialogState = dialogState;
	}

	public void loadBlankUrl() {
		mWebView.loadUrl("");
	}

	public boolean isShowing() {
		return isShowing;
	}

}
