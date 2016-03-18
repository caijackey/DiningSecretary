package com.fg114.main.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.SelectMultiplePictureActivity;
import com.fg114.main.app.activity.resandfood.BookingFromNetActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailMainActivity;
import com.fg114.main.app.listener.CallXiaoMiShuListener;
import com.fg114.main.service.dto.ResInfo2Data;
import com.fg114.main.service.dto.RestInfoData;
import com.fg114.main.service.dto.RestInfoData;

public class ButtonPanelUtil {

	// private static ButtonPanelUtil instance;
	private PopupWindow buttonPanelDialog;
	private Button btnDialogBookByPhone;
	private Button btnDialogBookByNet;
	private Button btnDialogUploadFromCamera;
	private Button btnDialogUploadFromLocal;

	private OnGetUriListener mListener;
	private Runnable cancelListener;
	private boolean isCancelled = true; // 表明弹出框关闭是否是被取消而关闭的

	// 用于推荐餐厅上传图片时，表示批量上传
	private boolean isBatch = false;
	private Bundle data;

	public ButtonPanelUtil() {
		this(false, null);
	}

	public ButtonPanelUtil(boolean isBatch, Bundle data) {
		this.isBatch = isBatch;
		this.data = data;
	}

	public void setOnGetUriListener(OnGetUriListener listener) {
		mListener = listener;
	}

	public void setOnCancelListener(Runnable cancelListener) {
		this.cancelListener = cancelListener;
	}

	/**
	 * 创建按钮面板
	 */
	private void createButtonPanel(final Activity activity, final RestInfoData restaurantInfo) {
		// 创建Dialog
		View buttonPanelView = View.inflate(activity, R.layout.button_panel, null);
		buttonPanelDialog = new PopupWindow(buttonPanelView, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, true);
		buttonPanelDialog.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.bg_button_panel));
		buttonPanelDialog.setOutsideTouchable(false);
		buttonPanelDialog.setAnimationStyle(R.style.panelAnimation);

		// 获得按钮
		btnDialogBookByPhone = (Button) buttonPanelView.findViewById(R.id.button_panel_btnBookByPhone);
		btnDialogBookByNet = (Button) buttonPanelView.findViewById(R.id.button_panel_btnBookByNet);
		btnDialogUploadFromCamera = (Button) buttonPanelView.findViewById(R.id.button_panel_btnUploadFromCamera);
		btnDialogUploadFromLocal = (Button) buttonPanelView.findViewById(R.id.button_panel_btnUploadFromLocal);
		Button btnDialogCancle = (Button) buttonPanelView.findViewById(R.id.button_panel_btnCancle);
		// ---
		buttonPanelDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (isCancelled && cancelListener != null) {
					cancelListener.run();
				}
			}
		});
		// ---
		if (restaurantInfo != null) {
			// 处理在线订餐的折扣和积分逻辑
			String strBookByNet = activity.getString(R.string.text_button_book_by_net);
			String strBookByPhone = activity.getString(R.string.text_button_book_by_phone);
			if (!TextUtils.isEmpty(restaurantInfo.districtName)) {
				strBookByPhone = String.format(strBookByPhone, "(" + restaurantInfo.districtName + ")");
			} else {
				strBookByPhone = String.format(strBookByPhone, "");
			}
			if (!TextUtils.isEmpty(restaurantInfo.ydzkDetail)) {
				strBookByNet = "   " + String.format(strBookByNet, "(" + restaurantInfo.ydzkDetail + ")");
			} else {
				strBookByNet = String.format(strBookByNet, "");
			}

			SpannableStringBuilder styleNet = new SpannableStringBuilder(strBookByNet);
			int start = strBookByNet.indexOf("(");
			if (start > -1) {
				styleNet.setSpan(new ForegroundColorSpan(Color.RED), start, strBookByNet.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			btnDialogBookByNet.setText(styleNet);
			SpannableStringBuilder stylePhone = new SpannableStringBuilder(strBookByPhone);
			start = strBookByPhone.indexOf("(");
			if (start > -1) {
				stylePhone.setSpan(new ForegroundColorSpan(Color.RED), start, strBookByPhone.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			btnDialogBookByPhone.setText(stylePhone);

			// 电话订餐
			btnDialogBookByPhone.setOnClickListener(new CallXiaoMiShuListener(activity, buttonPanelDialog, restaurantInfo.uuid, null));
			// 网上订餐
			btnDialogBookByNet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					Bundle bundle = new Bundle();
					bundle.putString(Settings.BUNDLE_KEY_LEFT_BUTTON, activity.getString(R.string.text_title_restaurant_detail));
					bundle.putString(Settings.BUNDLE_KEY_ID, restaurantInfo.uuid);
					isCancelled = false;
					buttonPanelDialog.dismiss();
					ActivityUtil.jump(activity, BookingFromNetActivity.class, 0, bundle);
				}
			});
		}

		// 拍照上传
		btnDialogUploadFromCamera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 以拍摄时间命名照片
				ViewUtils.preventViewMultipleClick(v, 1000);
				// String path =
				// Environment.getExternalStorageDirectory().getName()
				// + File.separatorChar + "Android/data/"
				// + getPackageName()
				// + "/files/";
				// takePhotoPath = path + fileName;

				// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);
				// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				// buttonPanelDialog.dismiss();
				// activity.startActivityForResult(intent, CAMERAIMAGE);
				isCancelled = false;
				buttonPanelDialog.dismiss();
				try {
					String fileName = System.currentTimeMillis() + ".jpg";
					Uri takePhotoUri;
					if (Settings.isRestaurantRecommentDetail) {
						takePhotoUri = ActivityUtil.captureImage(activity.getParent(), Settings.CAMERAIMAGE, fileName, "Image capture by camera for " + activity.getParent().getString(R.string.app_name));
						Settings.RestaurantRecommentDetailUri=takePhotoUri;
					} else {
						takePhotoUri = ActivityUtil.captureImage(activity, Settings.CAMERAIMAGE, fileName, "Image capture by camera for " + activity.getString(R.string.app_name));
					}
					if (takePhotoUri != null && mListener != null) {
						mListener.onGetUri(takePhotoUri);
					}
				} catch (Exception e) {
					DialogUtil.showToast(activity, "对不起，你的手机不支持拍照上传图片");
				}
			}
		});
		// 本地上传
		btnDialogUploadFromLocal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// 单张上传
				if (!isBatch) {
					isCancelled = false;
					buttonPanelDialog.dismiss();
					try {
						if (Settings.isRestaurantRecommentDetail) {
							ActivityUtil.pickImage(activity.getParent(), Settings.LOCALIMAGE);
						} else {
							ActivityUtil.pickImage(activity, Settings.LOCALIMAGE);
						}
					} catch (Exception e) {
						DialogUtil.showToast(activity, "对不起，你的手机不支持本地上传图片");

					}
				} else {
					// 批量上传
					Intent intent = new Intent();
					intent.setClass(ContextUtil.getContext(), SelectMultiplePictureActivity.class);
					intent.putExtras(data);
					buttonPanelDialog.dismiss();
					// com.fg114.main.app.activity.SelectMultiplePictureActivity.MyBroadcastReceiver

					activity.startActivityForResult(intent, Settings.LOCALIMAGE_BATCH);
				}

			}
		});
		// 取消
		btnDialogCancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				// if (uri != null) {
				// activity.getContentResolver().delete(uri, null, null);
				// }
				// 关闭对话框
				isCancelled = true;
				buttonPanelDialog.dismiss();
			}
		});
	}

	/**
	 * 显示订餐按钮面板
	 */
	public void showBookPanel(View parent, Activity activity, RestInfoData restaurantInfo) {
		// if (buttonPanelDialog == null) {
		createButtonPanel(activity, restaurantInfo);
		// }
		btnDialogBookByPhone.setVisibility(View.VISIBLE);
		btnDialogBookByNet.setVisibility(View.VISIBLE);
		btnDialogUploadFromCamera.setVisibility(View.GONE);
		btnDialogUploadFromLocal.setVisibility(View.GONE);
		// 显示RoundCorner对话框
		buttonPanelDialog.showAtLocation(parent, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
	}

	/**
	 * 显示上传按钮面板
	 */
	public void showUploadPanel(View parent, Activity activity, RestInfoData restaurantInfo) {
		// if (buttonPanelDialog == null) {
		createButtonPanel(activity, restaurantInfo);
		// }

		// if (uri == null) {
		// String fileName = System.currentTimeMillis() + ".jpg";
		// //设置文件参数
		// ContentValues values = new ContentValues();
		// values.put(MediaStore.Images.Media.TITLE, fileName);
		// values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera for "
		// + activity.getString(R.string.app_name));
		// //获得uri
		// takePhotoUri = activity.getContentResolver().insert(
		// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// }
		// else {
		// takePhotoUri = uri;
		// }

		btnDialogBookByPhone.setVisibility(View.GONE);
		btnDialogBookByNet.setVisibility(View.GONE);
		btnDialogUploadFromCamera.setVisibility(View.VISIBLE);
		btnDialogUploadFromLocal.setVisibility(View.VISIBLE);
		try {
			// 显示RoundCorner对话框
			buttonPanelDialog.showAtLocation(parent, Gravity.CENTER | Gravity.BOTTOM, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface OnGetUriListener {
		public void onGetUri(Uri uri);
	}
}
