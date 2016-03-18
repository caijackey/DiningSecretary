package com.fg114.main.util;

import java.lang.ref.WeakReference;
import java.util.*;

import android.util.Log;


/**
 * 被监听器者，该类与系统类的不同之处在于，可以传递一个hint 做通知提示
 * 
 * @author xujianjun,2012-09-17
 * 
 */
public class CommonObservable {
	private static class ObserverItem {
		String id;
		WeakReference<CommonObserver> wObserver;
		CommonObserver ref;

		ObserverItem(String id, CommonObserver observer) {
			this.id = id;
			this.ref=observer;
			this.wObserver = new WeakReference<CommonObserver>(observer);
		}
	}

	private static final String DEFAULT_ID = "default-default-id";
	// ---
	private Vector<ObserverItem> obs;
	private volatile static CommonObservable mObservable;
	static {
		mObservable = new CommonObservable();
	}

	private CommonObservable() {
		obs = new Vector<ObserverItem>();
	}

	public static CommonObservable getInstance() {
		return mObservable;
	}
	
	public synchronized void addObserver(CommonObserver o) {
		addObserver(DEFAULT_ID, o);
	}

	public synchronized void addObserver(String id, CommonObserver o) {
		if (o == null || id == null) {
			throw new NullPointerException();
		}
		ObserverItem item = new ObserverItem(id, o);
		// 不允许重复的监听者，监听者用id和类名称组合起来识别
		Iterator<ObserverItem> it = obs.iterator();
		while (it.hasNext()) {
			ObserverItem temp = it.next();
			CommonObserver tempObserver = temp.wObserver.get();
			if (temp == null || temp.id == null || tempObserver == null) {
				it.remove();
				continue;
			}
			// ---
			if (temp.id.equals(item.id) && tempObserver.getClass() == o.getClass()) {
				it.remove();
			}
		}
		obs.addElement(item); 
	}
//	private void printHaha(CommonObserver o) {
//		Class clazz=o.getClass().getEnclosingClass();
//		while(clazz!=null){
//			Log.d(o+"============>",""+clazz);
//			clazz=clazz.getEnclosingClass();
//		}
//	}

	//删除的时候,如果只提供了CommonObserver对象，则只寻找该对象删除，不考虑id
	public synchronized void deleteObserver(CommonObserver o) {
		deleteObserver(DEFAULT_ID, o);
	}

	public synchronized void deleteObserver(String id, CommonObserver o) {
		if (o == null || id == null) {
			throw new NullPointerException();
		}
		//监听者用id和类名称组合起来识别
		Iterator<ObserverItem> it = obs.iterator();
		while (it.hasNext()) {
			ObserverItem temp = it.next();
			CommonObserver tempObserver = temp.wObserver.get();
			if (temp == null || temp.id == null || tempObserver == null) {
				it.remove();
				continue;
			}
			// ---
			if (temp.id.equals(id) && tempObserver.getClass() == o.getClass()) {
				it.remove();
			}
		}
	}


	/**
	 * 通知某个特定observer，该observer是hint类的实例
	 * 
	 * @param hint
	 */
	public void notifyObservers(Class<?> hint) { 
		notifyObservers(hint, null);
	}

	/**
	 * 通知某个特定observer，该observer是hint类的实例
	 * 
	 * @param hint
	 */
	public synchronized void notifyObservers(Class<?> hint, Object data) {  
		if (hint == null) {
			return;
		}
		//监听者用类名称来识别
		ObserverItem[] copy=obs.toArray(new ObserverItem[0]);
		for(ObserverItem temp:copy){
			CommonObserver tempObserver = temp.wObserver.get();
			if (temp == null || temp.id == null || tempObserver == null) {
				obs.remove(temp);
				continue;
			}
			// ---
			if (hint.isInstance(tempObserver)) {
				tempObserver.update(this, data);
			}
		}
	}
}
