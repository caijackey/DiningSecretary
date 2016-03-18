package com.fg114.main.cache;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.*;
import java.util.*;

import android.os.SystemClock;
import android.util.*;
import com.fg114.main.app.Settings;
import static com.fg114.main.app.Settings.*;

/**
 * 
 * 内存缓存，用于进一步优化读取速度。目前只实现FIFO算法。 与两个CacheUtil类配合，在内存中缓存最后读取的那几条数据，并自动清除较老旧的数据。
 * 缓存的最大容量（字节数）由maxCacheSize决定。
 * 
 * 内存缓存作为二级缓存，对最终用户（应用程序）是透明的，最终用户不应该试图使用该类。 最终用户最多只接触到磁盘缓存类（两个CacheUtil）。
 * 
 * @author xu jianjun ,2011-11-21
 * 
 */
public class MemoryCache<T extends CacheableObject> {

	private final static boolean DEBUG=true;
	// 为MemoryCache对象设置一个名字，以方便调试
	private String name = "no name MemoryCache";

	// 决定缓存的最大容量，字节数。当缓存的对象超过这个大小的时候将按一定规则清理缓存
	private long maxSize = 1024 * 1024 * 2;

	// 缓存已占用大小
	private long usedSize = 0;

	// 在内存中缓存的Cacheable对象集合
	private HashMap<String, T> cacheMap = new HashMap<String, T>(100);

	// 使用优先级决定内存中优先低的对象被先清除出缓存队列
	// private PriorityQueue<T> cacheQueue=new PriorityQueue<T>(150,new
	// CacheableObject.Comparator());
	private LinkedBlockingQueue<T> cacheQueue = new LinkedBlockingQueue<T>();

	// 读写锁，同步访问共享资源。对读操作使用共享锁，对修改操作使用排他锁。
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

	// private boolean DEBUG=true;

	public MemoryCache() {

	}

	public MemoryCache(long cacheSize) {
		this.maxSize = cacheSize;
	}

	public MemoryCache(long cacheSize, String name) {
		this.maxSize = cacheSize;
		this.name = name;
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// while(true){
		// SystemClock.sleep(10000);
		// Log.e("MemoryCache",MemoryCache.this.toString());
		// }
		// }
		// }).start();
	}

	/**
	 * 增加一个CacheObject对象到内存缓存中
	 * 如果缓存中已经存在相同的cacheObject（使用cacheObject.identity判断），则使用当前的对象替换先前的对象。
	 * 
	 * @param cacheObject
	 * @return 添加成功返回true，否则返回false
	 */
	public boolean put(T cacheObject) {

		if (cacheObject == null)
			return false;
		Lock writer = this.lock.writeLock();
		writer.lock();
		try {
			if (cacheMap.containsKey(cacheObject.identity())) {
				// 删除队列中的相应元素。防止后面重复数据进入队列。
				Iterator<T> it = cacheQueue.iterator();
				while (it.hasNext()) {
					T t = it.next();
					if (t.equals(cacheObject)) {
						it.remove();
						this.usedSize -= t.size();
						break;
					}
				}
			}
			// 判断是否超出缓存容量，需要dump出足够的空间
			if (usedSize + cacheObject.size() > this.maxSize) {
				dump(cacheObject.size());
			}

			cacheMap.put(cacheObject.identity(), cacheObject);
			cacheQueue.put(cacheObject);
			usedSize += cacheObject.size();

			return true;
		} catch (Exception ex) {
			Log.e("MemoryCache(" + this.name + ")", "Error in put(" + cacheObject + ") " + ex.getMessage(), ex);
			return false;
		} finally {
			writer.unlock();
		}
	}

	/**
	 * 清理缓存，超出缓存容量时，把缓存中老旧的内容清除，直到缓存占用至少小于: 最大容量-atLeast 通过一定的规则，最大程度的避免:
	 * 连续两次put时都进dump
	 * 
	 * @param atLeast
	 * @return
	 */
	private boolean dump(long atLeast) {

		if (DEBUG )
			Log.d("MemoryCache(" + this.name + ")", "<!> start dumping memory cache for atLeast=" + atLeast + "B! [total objects in memory/used/max]=["
					+ cacheMap.size() + "/" + this.usedSize + "/" + this.maxSize + "][" + to100Percent((double) this.usedSize / this.maxSize) + "]");
		Lock writer = this.lock.writeLock();
		writer.lock();
		try {
			CacheableObject dust = null;
			// 估计参数-----------------------------------------
			int M = 150 * 1024; // 缓存对象的最大估计值
			int m = 5 * 1024; // 缓存对象的最小估计值
			int valve = (M + m) / 2; // 平均估计值
			int ratio=(M+(m-1))/m;

			// 算出此次需要dump掉的实际字节数
			if (atLeast > valve) {
				atLeast = atLeast > 2 * M ? atLeast : 2 * M;
			} else {
				atLeast= M + atLeast;
			}

			// ------------------------------------------------
			// 从队列头部移除
			while (atLeast > 0 && (dust = cacheQueue.poll()) != null) {

				cacheMap.remove(dust.identity());
				this.usedSize -= dust.size();
				atLeast -= dust.size();
				dust.dump();
			}

			if (DEBUG)
				Log.d("MemoryCache(" + this.name + ")", "<!> memory dump finished! [total objects in memory/used/max]=[" + cacheMap.size() + "/"
						+ this.usedSize + "/" + this.maxSize + "][" + to100Percent((double) this.usedSize / this.maxSize) + "]");

			return true;
		} catch (Exception ex) {
			Log.e("MemoryCache(" + this.name + ")", "Error in dump() " + ex.getMessage(), ex);
			return false;
		} finally {
			writer.unlock();
			System.gc();
		}
	}

	/**
	 * 返回标识等于identity
	 * 
	 * @param identity
	 *            　要返回CacheObject的identity
	 * @return CacheObject的对象
	 */
	public T get(String identity) {
		if (identity == null)
			return null;
		// 修改为使用写锁
		Lock reader = this.lock.writeLock();
		reader.lock();
		try {
			T o = cacheMap.get(identity);
			if (o != null) {
				// 如果已经失效，则主动清除
				if (!o.isValid) {
					remove(identity);
					return null;
				}
				// ---
				o.hit();// 命中
				if (DEBUG)
					Log.d("MemoryCache(" + this.name + ")", "got from memory! [total objects in memory/used/max]=[" + cacheMap.size() + "/" + this.usedSize
							+ "/" + this.maxSize + "][" + to100Percent((double) this.usedSize / this.maxSize) + "]");
				// 从队列中移到最后队尾，减小该对象在缓存满的状态下被dump掉的机率（缓存容纳的对象个数要足够大，一般大于40个）
				// 删除队列中的相应元素
				Iterator<T> it = cacheQueue.iterator();
				while (it.hasNext()) {
					T t = it.next();
					if (t.equals(o)) {
						it.remove();// 遍历所有元素，以防有重复
					}
				}
				// 放队尾
				cacheQueue.put(o);
			}
			// Log.d("cache get",
			// "cacheQueue.size="+cacheQueue.size()+",cacheMap.size="+cacheMap.size()+", "+this);
			return o;
		} catch (Exception ex) {
			Log.e("MemoryCache(" + this.name + ")", "Error in get(" + identity + ") " + ex.getMessage(), ex);
			return null;
		} finally {
			reader.unlock();
		}
	}

	/**
	 * 从缓存中清除identity指定的对象
	 * 
	 * @param identity
	 * @return 清除成功返回true，否则返回false
	 */
	public boolean remove(String identity) {
		if (identity == null)
			return false;

		Lock writer = this.lock.writeLock();
		writer.lock();
		try {
			T o = cacheMap.remove(identity);
			if (o == null) {
				return false;
			}
			o.dump();
			if (o != null) {
				// 删除队列中的相应元素
				Iterator<T> it = cacheQueue.iterator();
				while (it.hasNext()) {
					T t = it.next();
					if (t.equals(o)) {
						it.remove();// 遍历所有元素，以防有重复
						this.usedSize -= t.size();
					}
				}

			}
			return true;
		} catch (Exception ex) {
			Log.e("MemoryCache(" + this.name + ")", "Error in remove(" + identity + ") " + ex.getMessage(), ex);
			return false;
		} finally {
			writer.unlock();
		}

	}

	/**
	 * 设置当前缓存对象的容量
	 * 
	 * @param size
	 *            表示缓存容量的字节数
	 */
	public void setMaxSize(long size) {

		this.maxSize = size;
	}

	/**
	 * 返回表示缓存容量的字节数
	 */
	public long getMaxSize() {

		return this.maxSize;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		Set<Map.Entry<String, T>> all = this.cacheMap.entrySet();
		int count = all.size();

		sb.append("------- MemoryCache Infomation [" + name + "] -------\n");
		sb.append("(total " + count + " objects)[used/max]=[" + this.usedSize + "/" + this.maxSize + "][mapSize/queueSize]=[" + this.cacheMap.size() + "/"
				+ this.cacheQueue.size() + "][" + to100Percent((double) this.usedSize / this.maxSize) + "]\n");
		/*
		 * for(Map.Entry<String,T> o : all){
		 * sb.append("--> "+o.getValue().identity
		 * ()+",size="+o.getValue().size()+" \n"); }
		 */
		sb.append("------- End MemoryCache Infomation ------- \n");
		return sb.toString();
	}

	private String to100Percent(double n) {
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumFractionDigits(4);
		return nf.format(n);
	}

}
