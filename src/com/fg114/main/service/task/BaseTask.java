package com.fg114.main.service.task;

import java.net.SocketTimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.mealcombo.MealComboListActivity;
import com.fg114.main.app.activity.resandfood.ResAndFoodListActivity;
import com.fg114.main.app.activity.resandfood.RestaurantDetailActivity;
import com.fg114.main.app.activity.takeaway.TakeAwayNewFoodListActivity;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;

/**
 * 网络连接任务
 * @author zhangyifan
 *
 */
public abstract class BaseTask extends AsyncTask<Runnable, Void, JsonPack> {
	
	//进度提示框
	protected  ProgressDialog progressDialog = null;
	//进度提示文字
	private String preDialogMessage = null;
	//是否需要返回值
	private boolean isReturn = true;
	//error处理
	private Runnable errorRunnable = null;
	
	protected Context context = null;
	
	private static boolean DEBUG = Settings.DEBUG;
	
	private Callback mCallback;
	
	//进度指示是否可以取消，及其回调监听
	private boolean canCancel=false;
	private OnCancelListener cancelListener=null;
	//进度条样式
	private boolean isIndeterminate=true;
	private int maxProgressValue=100;
	
	private String requestUrl = "";
	
	private boolean showError = true; // 出错时是否显示浮动的提示框
	
	
	//显示进度提示
	public BaseTask(String preDialogMessage, Context context){
		this.preDialogMessage = preDialogMessage;
		this.context = context;
		this.isReturn = true;
	}
	//显示进度提示
	public BaseTask(String preDialogMessage, Context context,boolean isIndeterminate,int maxProgressValue){
		this.preDialogMessage = preDialogMessage;
		this.context = context;
		this.isReturn = true;
		this.isIndeterminate=isIndeterminate;
		this.maxProgressValue=maxProgressValue;
	}
	
	//不显示进度提示
	public BaseTask(Context context){
		this.preDialogMessage = null;
		this.context = context;
		this.isReturn = true;
	}
	
	public BaseTask(Context context, boolean isReturn){
		this.preDialogMessage = null;
		this.context = context;
		this.isReturn = isReturn;
	}

	/**
	 * 该方法将在执行实际的后台操作前被UI thread调用。
	 * 可以在该方法中做一些准备工作，如在界面上显示一个进度条
	 */
	@Override
	protected void onPreExecute() {
		if(preDialogMessage != null){
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("");
			progressDialog.setMessage(preDialogMessage);
			if(!isIndeterminate){
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			}
			progressDialog.setIndeterminate(isIndeterminate);
			progressDialog.setMax(maxProgressValue);
			progressDialog.setCancelable(canCancel);
			if(cancelListener!=null){
				progressDialog.setOnCancelListener(cancelListener);
			}
			if (context != null && !((Activity)context).isFinishing()) {
				try{
					progressDialog.show(); 
				}catch (Exception e) {
					Log.e("onPreExecute:progressDialog.show()",e.getMessage(),e);
				}				
			}
		}
		onPreStart();
	}

	/** 
	 * 执行那些很耗时的后台计算工作。
	 * 可以调用publishProgress方法来更新实时的任务进度
	 */
	@Override
	protected JsonPack doInBackground(Runnable... runnables) {
		JsonPack result = new JsonPack();
		try {
			if (isReturn) {
				//需要返回值的场合，获得json数据
				result = getData();
				
				// 保存当前请求的url
				if (result != null && !TextUtils.isEmpty(result.getUrl())) {
					requestUrl = result.getUrl();
				}
				
				if (result.getRe() == 200) {
					//设置回调函数
					if(runnables.length > 0){
						setCallBack(runnables[0]);
					}
				}
			} else {
				getData();
				if(runnables.length >0){
					setCallBack(runnables[0]);
				}
			}
		} catch (SocketTimeoutException e) {
			if(DEBUG)e.printStackTrace();
			result.setRe(400);
			result.setMsg("抱歉,网络查询超时");
			return result;
		} catch (Exception e) {
//			Log.e("BaseTask",e.getMessage(),e);
			if (ActivityUtil.isNetWorkAvailable(ContextUtil.getContext())
					&& !(e instanceof org.apache.http.conn.ConnectTimeoutException)
					&& !(e instanceof java.io.InterruptedIOException)) {
				String msg = "error_net from Exception ";
				if (result != null && result.getObj() != null) {
					msg += result.getObj().toString();
				}
				ActivityUtil.saveException(e, msg);
				
				result.setMsg("网络查询出现错误");
			} else {
				result.setMsg("似乎已断开与互联网的连接");
			}
			result.setRe(400);
			return result;
		} 
		catch (OutOfMemoryError e) {
			ActivityUtil.saveOutOfMemoryError(e);
			result.setRe(400);
			result.setMsg(context.getString(R.string.text_error_net));
			return result;
		} 
		finally {
			if (runnables.length > 1) {
				errorRunnable = runnables[1];
			}
			if (TextUtils.isEmpty(result.getMsg())) {
				result.setMsg("");
			}
		}
		return result;
	}
	Runnable runnable=null;
	private void setCallBack(Runnable runnable) {
		this.runnable=runnable;
		
	}
	private Runnable getCallBack() {
		return runnable;
	}
	/**
	 * 在doInBackground 执行完成后，onPostExecute 方法将被UI thread调用
	 * 后台的计算结果将通过该方法传递到UI thread.
	 */
	@Override
	protected void onPostExecute(final JsonPack result) {

		if (!this.isCancelled()) {
			if(result.getRe() == 200){
				closeProgressDialog();
				onStateFinish(result);
				if(getCallBack() != null){
					getCallBack().run();
				}
			}else{
				closeProgressDialog();
				if (context instanceof Activity && result.getRe() == 400 && showError) {
					if ((context instanceof ResAndFoodListActivity 
							|| context instanceof RestaurantDetailActivity
							|| context instanceof TakeAwayNewFoodListActivity
							|| context instanceof MealComboListActivity
							)
							&& mCallback != null) {
						// 餐厅和菜品列表页，餐厅详情页，做特殊处理，可点击重试
						DialogUtil.showErrorNetPopWind((Activity) context, new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								if (mCallback != null) {
									mCallback.onNetworkFail(result);
								}
							}
						}, result.getMsg());
					} else {
						DialogUtil.showErrorNetPopWind((Activity) context, null, result.getMsg());
					}
				} else {
					onStateError(result);
				}
				if (errorRunnable != null) {
					errorRunnable.run();
				}
			}
		}
	}
	

	/**
	 * 获取数据
	 */
	abstract public JsonPack getData() throws Exception;
	
	/**
	 * 正常结束，调用回调函数
	 */
	abstract public void onStateFinish(JsonPack result);
	
	/**
	 * error
	 */
	abstract public void onStateError(JsonPack result);
	/**
	 * 
	 */
	abstract public void onPreStart();
	
	//关闭进度提示
	public void closeProgressDialog() {
		try {
			if (preDialogMessage != null && progressDialog.isShowing()) {
				if (context != null && !((Activity) context).isFinishing()) {
					progressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//得到资源文件的值
	public String getString(int resId){
		return context.getResources().getString(resId);
	}
	
	public void setCallBack(Callback callback) {
		mCallback = callback;
	}
	
	public interface Callback {
		public void onNetworkFail(JsonPack result);
	}

	public boolean canCancel() {
		return canCancel;
	}

	public void setCanCancel(boolean canCancel) {
		this.canCancel = canCancel;
	}

	public OnCancelListener getCancelListener() {
		return cancelListener;
	}

	public void setCancelListener(OnCancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}
	
	public String getRequestUrl() {
		return requestUrl;
	}
	
	public boolean isShowError() {
		return showError;
	}
	
	public void setShowError(boolean showError) {
		this.showError = showError;
	}
}
