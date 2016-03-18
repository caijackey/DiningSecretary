package com.fg114.main.util;

import java.lang.ref.WeakReference;

import android.os.Bundle;

/**
 * 所有自定义监听者的定义都在这里
 * 
 * @author xujianjun,2012-09-18
 * 
 */
public class CommonObserver {

	public void update(CommonObservable commonObservable, Object arg) {

	}

	/**
	 * 
	 * 微博认证结果通知类
	 *
	 */
	public static class WeiboAuthResultObserver extends CommonObserver {

		private WeiboAuthResultListener listener;
		/**
		 * 传入成功后的回调
		 */
		public WeiboAuthResultObserver(WeiboAuthResultListener listener) {
			this.listener = listener;
		}
		/**
		 * 完成时，将执行此update
		 */
		public void update(CommonObservable commonObservable, Object result) {
			
			if (listener == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			listener.onComplete((Boolean)result);
			commonObservable.deleteObserver(this);
		}

		public interface WeiboAuthResultListener {

			/**
			 * 通知授权（并且绑定、登录）是否成功
			 * @param isSuccessful
			 */
			public void onComplete(boolean isSuccessful);

		}
		
	}
	
	/** *****************************************************************************************************
	 * 
	 * 系统消息观察者（订单数量，站内信等）
	 *
	 */
	public static class SystemMessageObserver extends CommonObserver {

		private WeakReference<Runnable> reference;
		/**
		 * 传入成功后的回调
		 */
		public SystemMessageObserver(Runnable runner) {
			this.reference = new WeakReference<Runnable>(runner);
		}
		/**
		 * 完成时，将执行此update
		 */
		public void update(CommonObservable commonObservable, Object result) {
			
			if (reference == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			// ---
			Runnable runner = reference.get();
			if (runner == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			runner.run();
		}
	}
	/** *****************************************************************************************************
	 * 
	 * 城市改变观察者
	 *
	 */
	public static class CityChangedObserver extends CommonObserver {
		
		private WeakReference<Runnable> reference;
		/**
		 * 传入回调
		 */
		public CityChangedObserver(Runnable runner) {
			this.reference = new WeakReference<Runnable>(runner);
		}
		/**
		 * 完成时，将执行此update
		 */
		public void update(CommonObservable commonObservable, Object result) {
			
			if (reference == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			// ---
			Runnable runner = reference.get();
			if (runner == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			runner.run();
		}
	}
	/** *****************************************************************************************************
	 * 
	 * 新版本升级观察者
	 *
	 */
	public static class NewVersionObserver extends CommonObserver {
		
		private WeakReference<Runnable> reference;
		/**
		 * 传入成功后的回调
		 */
		public NewVersionObserver(Runnable runner) {
			this.reference = new WeakReference<Runnable>(runner);
		}
		/**
		 * 完成时，将执行此update
		 */
		public void update(CommonObservable commonObservable, Object result) {
			
			if (reference == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			// ---
			Runnable runner = reference.get();
			if (runner == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			runner.run();
		}
	}
	/** *****************************************************************************************************
	 * 
	 * ReturnToActivity方法的观察者，当MainFrameActivity.returnToActivity方法执行完成的时候，将触发此回调
	 *
	 */
	public static class ReturnToActivityFinishedObserver extends CommonObserver {
		
		private Runnable runAfter;
		/**
		 * 传入成功后的回调
		 */
		public ReturnToActivityFinishedObserver(Runnable runner) {
			this.runAfter = runner;
		}
		/**
		 * 完成时，将执行此update
		 */
		public void update(CommonObservable commonObservable, Object result) {
			
			// ---
			if (runAfter == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			runAfter.run();
			commonObservable.deleteObserver(this);
		}
	}
		
	/****************************************************************************************
	 * 例子：监听处理器
	 ****************************************************************************************/
	public static class ExampleObserver extends CommonObserver {

		private WeakReference<Runnable> reference;

		/**
		 * 传入需要接受通知的WebView控件
		 * 
		 */
		public ExampleObserver(Runnable runAfterSuccess) {
			this.reference = new WeakReference<Runnable>(runAfterSuccess);
		}

		/**
		 * 登录完成时，将执行此update方法通知WebView来执行一个js方法。 执行完成后，自己将自己从监听列表里清除
		 */
		public void update(CommonObservable commonObservable, Object arg) {
			if (reference == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			// ---
			Runnable runAfterSuccess = reference.get();
			if (runAfterSuccess == null) {
				commonObservable.deleteObserver(this);
				return;
			}
			//WebUtils.jsCommentAddSuccess(webView, new JavaScriptInterface(""));
			runAfterSuccess.run();
			commonObservable.deleteObserver(this);
		}
	}

}