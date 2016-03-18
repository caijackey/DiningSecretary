package com.fg114.main.service.task;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.service.http.A57HttpApiV3;
import com.fg114.main.service.http.AbstractHttpApi;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.http.ServiceRequest.API;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.SessionManager;
import com.fg114.main.weibo.UserInfo;

/**
 * 通用Task，用于获取网络接口数据，该类不能被继承
 *     
 *   使用说明：应用程序仿照下面（以调用getDishList接口，返回菜单列表为例）
 *   1) 先构造一个ServiceRequest对象，传入需要调用的api枚举实例
 *   ServiceRequest request=new ServiceRequest(ServiceRequest.API.getDishList);
 *   
 *   2) 添加参数到ServiceRequest对象，参数的约定参见枚举类型：ServiceRequest.API
 *   request.addData("fromTag",1).addData("uuid","ABCD000001");
 *   
 *   3) 执行task请求，传入request和listener，获取数据
 *   CommonTask.request(request,"正在获取菜单...",new CommonTask.TaskListener<DishListPackDTO>(){
 *
 *			@Override
 *			protected void onSuccess(DishListPackDTO dto) {
 *				//处理成功逻辑
 *			}
 *
 *			@Override
 *			protected void onError(int code, String message) {
 *				super.onError(code, message);
 *				//处理失败逻辑
 *			}
 *
 *			@Override
 *			protected void onCancel() {
 *				super.onCancel();
 *				//用户中途取消逻辑
 *			}
 *
 *			@Override
 *			protected void onRefresh() {
 *				super.onRefresh();
 *				//网络异常时刷新逻辑
 *			}
 *			@Override
 *			protected void defineCacheKeyAndTime(CacheKeyAndTime keyAndTime){
 *				//需要缓存请求结果时，在这里给keyAndTime对象的属性赋值
 *			}
 *		});
 *
 * @author xujianjun,2013-07-05
 *
 */
public final class CommonTask<T> extends AsyncTask<ServiceRequest, Void, JsonPack> {
	
	private static boolean DEBUG = true;
	protected Context context = null;
	//进度提示框
	protected  ProgressDialog progressDialog = null;
	//进度提示文字
	private String message = null;
	
	// 出错时是否显示浮动的提示框
	private boolean showError = true;
	//是否是静默执行
	private boolean isMute=false;
	//task的回调
	private TaskListener<T> taskListener;
	//当前请求
	private ServiceRequest request;
	
	//--------------------------------------------------------------
	//不显示进度提示
	public CommonTask(){
		this.context = MainFrameActivity.getCurrentTopActivity();
	}

	@Override
	protected void onPreExecute() {
		//设置静默
		taskListener.isMute=this.isMute;
		//需要显示进度
		if(!CheckUtil.isEmpty(message)){
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("");
			progressDialog.setMessage(message);
			if(request.isPost()){
				//上传增加进度指示
				progressDialog.setIndeterminate(false);
				try {
					progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMax(request.getInputStreamData().available());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				progressDialog.setIndeterminate(true);
			}
			progressDialog.setCancelable(taskListener.isCancelable);
			progressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					taskListener.onCancel();
				}
			});
			//----
			if (context != null && !((Activity)context).isFinishing()) {
				
				try{
					progressDialog.show(); 
				}catch (Exception e) {
					Log.e("onPreExecute:progressDialog.show()",e.getMessage(),e);
				}				
			}
		}
	}

	@Override
	protected JsonPack doInBackground(ServiceRequest... request) {
		
		JsonPack result = new JsonPack();
		try {
//				if(API.getHotRestTypeList==request[0].getAPI()&&SystemClock.elapsedRealtime()%5!=0){
//					//测试错误
//					throw new NullPointerException();
//				}
				result = getData(request[0]);
				
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			result.setRe(400);
			result.setMsg("抱歉,网络查询超时");
		} catch (Exception e) {
			e.printStackTrace();
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
				result.setMsg("似乎已断开网络连接");
			}
			result.setRe(400);
		} 
		catch (OutOfMemoryError e) {
			ActivityUtil.saveOutOfMemoryError(e);
			result.setRe(400);
			result.setMsg(context.getString(R.string.text_error_net));
		} 
		return result;
	}
	
	//如果是Post提交或者静默处理的，用新创建的client，否则使用共用的client，避免相互阻塞
	private JsonPack getData(ServiceRequest request) throws Exception {
		
		DefaultHttpClient client = null;
		JsonPack jsonPack;
		try {
			//----------------------------
			jsonPack=getCacheData();
			if(jsonPack!=null){
				return jsonPack;
			}
			
			//-----------------------------
			if (request.isPost()) {
				ProgressListener plistener=new ProgressListener() {
					
					@Override
					public void updateProgress(int readedCount) {
						if(progressDialog!=null){
							if(progressDialog.getMax()==readedCount){
								progressDialog.getWindow().getDecorView().post(new Runnable() {
									@Override
									public void run() {
										progressDialog.setMessage("等待数据处理完成...");
									}
								});
							}
							progressDialog.setProgress(readedCount);
						}
					}
				};	
				client = AbstractHttpApi.createHttpClientForUpload();
				HttpPost httpPost = A57HttpApiV3.getInstance().mHttpApi
						.createHttpPost(request.getUrl(), new ProgressInputStream(request.getInputStreamData(),plistener), request.getParamArray());
				jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, httpPost);
			} else if (isMute) {
				client = AbstractHttpApi.createHttpClient();
				HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(request.getUrl(), request.getParamArray());
				jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, httpGet);
			}
			else if(request.isCanUsePost()){
				client = AbstractHttpApi.createHttpClientForUpload();
				HttpPost httpPost = A57HttpApiV3.getInstance().mHttpApi
						.createHttpPostWithoutParams2(request.getUrl(), request.getParamArray());
				jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(client, httpPost);
			}else {
				HttpGet httpGet = A57HttpApiV3.getInstance().mHttpApi.createHttpGet(request.getUrl(), request.getParamArray());
				jsonPack = A57HttpApiV3.getInstance().mHttpApi.doHttpRequest(httpGet);
			}
			return jsonPack;
						
		} finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	//根据缓存逻辑获取数据，在去网络获取数据前会尝试缓存读取
	private JsonPack getCacheData(){
		try {
			if (!taskListener.isNeedCache) {
				if(DEBUG) Log.d("数据不需要缓存[获取时]",""+taskListener.isNeedCache);
				return null;
			}
			JsonPack jp = new JsonPack();
			taskListener.defineCacheKeyAndTime(taskListener.cacheKeyAndTime);
			String key = taskListener.cacheKeyAndTime.cacheKey;
			//
			ValueObject vo = ValueCacheUtil.getInstance(context).get(request.getUrl(), key);
			if (vo != null && !vo.isExpired()) {
				jp.setObj(new JSONObject(vo.getValue()));
				if(DEBUG) Log.d("数据缓存命中",""+request);
				return jp;
			}else{
				if(DEBUG) Log.d("数据缓存没有命中",""+request);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	//
	private void saveCacheData(String data){
		if(!taskListener.isNeedCache||CheckUtil.isEmpty(data)){
			if(DEBUG) Log.d("数据不需要缓存",""+taskListener.isNeedCache);
			return;
		}
		taskListener.defineCacheKeyAndTime(taskListener.cacheKeyAndTime);
		String key = taskListener.cacheKeyAndTime.cacheKey;
		int time=taskListener.cacheKeyAndTime.cacheTimeMinute;
		//
		//存入缓存
		ValueCacheUtil.getInstance(context).remove(request.getUrl(), key);
		ValueCacheUtil.getInstance(context).add(request.getUrl(), key, data, "0", "-", time); 
		if(DEBUG) Log.d("数据被缓存",""+request);
	}

	@Override
	protected void onPostExecute(final JsonPack result) {
		
		//执行成功，返回dto
		if (result.getRe() == 200) {
			Throwable e=null;
			T dto=null;
			String json="";
			//转成dto
			try{
				json=result.getObj().toString();
				dto=JsonUtils.fromJson(json,(Class<T>)taskListener.dtoType);
				//TODO
				//还要处理userinfo逻辑
			}catch(Throwable ex){
				e=ex;
			}
			//转dto成功
			if(dto!=null){
				dealUserInfo(result);
				saveCacheData(json);
				taskListener.onSuccess(dto);
				
			}else if(Void.class==taskListener.dtoType){//不需要返回dto
				dealUserInfo(result);
				taskListener.onSuccess(null);
			}
			//转dto失败
			else{
				if(e!=null){
					Log.e("CommonTask", request.toString());
					e.printStackTrace();
				}
				taskListener.onError(999, "数据转换错误!"+request);
			}
			closeProgressDialog();
		} else {
			closeProgressDialog();
			// 可点击重试
			if (context instanceof Activity && result.getRe() == 400 && taskListener.isCanRefresh) {
				
				DialogUtil.showErrorNetPopWind((Activity) context, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						taskListener.onRefresh();
					}
				}, result.getMsg());

			}
			taskListener.onError(result.getRe(), result.getMsg());
		}
	}
	

	private void dealUserInfo(JsonPack result) {
		if(result!=null && result.getRe()==200 && result.isNeedUpdateUserInfoTag() && result.getUserInfo()!=null){
			UserInfoDTO user=result.getUserInfo();
			SessionManager.getInstance().setUserInfo(context, user);
//			KeepAliveService.bindBaiduPush();
		}
	}

	
	private static int delayTime = 0;
	public static void setCloseProgressDialogDelay(int delay){
		delayTime = delay;
	}
	
	//关闭进度提示
	public void closeProgressDialog() {
		if(delayTime>0){
			Thread thread =  new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delayTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					delayTime = 0;
					try {
						if (progressDialog != null && progressDialog.isShowing()) {
							if (context != null && !((Activity) context).isFinishing()) {
								progressDialog.dismiss();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
		else{
			try {
				if (progressDialog != null && progressDialog.isShowing()) {
					if (context != null && !((Activity) context).isFinishing()) {
						progressDialog.dismiss();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 执行后台请求的listener
	 * 使用注意事项：
	 * 		1) 如果子类覆盖了onCancel方法，则被执行的task是可被取消的（即进度指示可以响应返回键，不挡住用户）
	 * 		2) 如果子类覆盖了onRefresh方法，则可以在onRefresh中提供网络异常刷新逻辑
	 * 		3) 如果子类覆盖了defineCacheKeyAndTime方法，说明需要缓存结果数据
	 * 		      子类需要给此方法的参数CacheKeyAndTime赋值，以定义缓存的key和缓存的超时时间（分钟）
	 * @param <DTO>
	 */
	public static abstract class TaskListener<DTO>{
		//默认不能被cancel（当有进度对话框的话）
		private boolean isCancelable=false; 
		//当网络异常时是否可刷新，默认：否
		private boolean isCanRefresh=false; 
		//需要缓存结果
		private boolean isNeedCache=false; 
		//缓存定义
		private CacheKeyAndTime cacheKeyAndTime=new CacheKeyAndTime(); 
		//是否是静默方式
		private boolean isMute=false; 
		
		private Type dtoType;
		
		protected TaskListener(){
			Method m=null;
			try {
				m=this.getClass().getDeclaredMethod("onCancel");
			}catch(Throwable e){}
			//如果当前listener实例定义了onCancel方法，有cancel功能：即有进度框，但可以不挡住用户
			if(m!=null && this.getClass()!=TaskListener.class){ 
				isCancelable=true;
			}
			//---
			m=null;
			try {
				m=this.getClass().getDeclaredMethod("onRefresh");
			}catch(Throwable e){}
			//如果当前listener实例定义了onRefresh方法，有onRefresh功能
			if(m!=null && this.getClass()!=TaskListener.class){ 
				isCanRefresh=true;
			}
			//---
			m=null;
			try {
				m=this.getClass().getDeclaredMethod("defineCacheKeyAndTime",CacheKeyAndTime.class);
			}catch(Throwable e){}
			//如果当前listener实例定义了defineCacheKeyAndTime方法，说明需要缓存结果数据
			if(m!=null && this.getClass()!=TaskListener.class){ 
				isNeedCache=true;
			}
			//泛型
			try{
				dtoType=((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			}catch(Throwable e){
				throw new RuntimeException("没有提供DTO的类型参数!",e);
			}
		}
		/**
		 * task执行成功后的回调方法
		 * @param dto
		 * @param JsonPack 
		 */
		protected abstract void onSuccess(DTO dto);
		
		/**
		 * task执行出错后的回调方法
		 * @param code 错误代码
		 * @param message 错误的描述
		 */
		protected void onError(int code, String message){
			if(!isMute){
				DialogUtil.showToast(ContextUtil.getContext(), message);
			}
		}
		/**
		 * 当子类定义了此方法并且有加载进度框时，表示此task是可以取消的，用户按返回键时会调用此方法
		 */
		protected void onCancel(){}
		/**
		 * 当子类定义了此方法时，表示需要“刷新”操作。当网络异常时，会自动弹出一个“刷新”提示框，点击后会调用此方法。
		 */
		protected void onRefresh(){}
		/**
		 * 当子类定义了此方法时，表示需要“缓存结果数据”。
		 */
		protected void defineCacheKeyAndTime(CacheKeyAndTime keyAndTime){
			
		}
		//存储缓存的定义key和时间
		public static class CacheKeyAndTime {
			public String cacheKey="";
			public int cacheTimeMinute=5;
		}
	}
	
	/**
	 * 执行任务，通过listner获取返回的DTO，执行时显示默认提示信息
	 * @param url
	 * @param taskListener
	 */
	public static <DTO> void request(ServiceRequest request,TaskListener<DTO> taskListener){
		request(request,"请求中，请稍候...",taskListener,false);
	}
	
	/**
	 * 执行任务，通过listner获取返回的DTO，执行时显示参数message指定的提示信息。当message为空时，不显示加载进度
	 * @param url
	 * @param message
	 * @param taskListener
	 */
	public static <DTO> void request(ServiceRequest request,String message,TaskListener<DTO> taskListener){
		request(request, message, taskListener,false);
	}

	/**
	 * 静默执行任务（后台任务），通过listner获取返回的DTO。请求、返回、错误都不显式提示任何信息
	 * @param url
	 * @param taskListener
	 */
	public static <DTO> void requestMutely(ServiceRequest request,TaskListener<DTO> taskListener){
		request(request, "", taskListener,true);
	}
	
	//内部使用
	private static <DTO> void request(ServiceRequest request,String message,TaskListener<DTO> taskListener,boolean isMute){
		//验证不通过抛出异常
		request.validate();
		
		//---
		CommonTask<DTO> task=new CommonTask<DTO>();
		task.taskListener=taskListener;
		task.isMute=isMute;
		task.message=message;
		task.request=request;
		task.execute(request);
	}
	
	//----------------------------------------------------------------------
	//进度通知listener
	interface ProgressListener{
		void updateProgress(int readedCount);
	}
	//支持进度通知的inputStream
	class ProgressInputStream extends InputStream{
		InputStream inner=null;
		int readedCount=0;
		ProgressListener plistener;
		
		public ProgressInputStream(InputStream inner){
			this(inner, null);
		}
		public ProgressInputStream(InputStream inner,ProgressListener plistener){
			this.inner=inner;
			this.plistener=plistener;
		}
		@Override
		public int read() throws IOException {
			int r=inner.read();
			doProgress(r==-1?-1:1);
			return r;
		}
		@Override
		public int read(byte[] b) throws IOException {
			int r=inner.read(b);
			doProgress(r);
			return r;
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int r=inner.read(b, off, len);
			doProgress(r);
			return r;
		}
		@Override
		public long skip(long n) throws IOException {
			return inner.skip(n);
		}
		@Override
		public int available() throws IOException {
			return inner.available();
		}
		@Override
		public void close() throws IOException {
			inner.close();
		}
		@Override
		public void mark(int readlimit) {
			inner.mark(readlimit);
		}
		@Override
		public synchronized void reset() throws IOException {
			inner.reset();
		}
		@Override
		public boolean markSupported() {
			return inner.markSupported();
		}
		private void doProgress(int c){
			try {
				//Log.e("读"+inner.available(), "读 " + readedCount + " +" + c + " ->" + (readedCount + c));
				if (c != -1) {
					if (readedCount % 1024 + c >= 1024||inner.available()==0) {
						//每过于1K，触发一次
						//Log.d("读了1K 剩"+available(), "读了1K " + readedCount + " +" + c + " ->" + (readedCount + c));
						plistener.updateProgress(readedCount + c);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			readedCount+=c;
		}
	}
}
