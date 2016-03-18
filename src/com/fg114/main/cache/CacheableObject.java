package com.fg114.main.cache;

import android.util.Log;

/**
 * 该类的实例表示可被缓存在内存中的对象。
 * 通常被MemoryCache类的对象所利用，在内存中缓存数据。
 * 
 * @author xu jian jun ,2011-11-18
 * 
 */
public abstract class CacheableObject {

	/**
	 * @return　该缓存对象占用内存大小的估计值，字节数。
	 */
	public abstract long size();
	
	/**
	 * 表示当前对象是否是有效的。当对象为无效时，内存缓存应该清除该对象，并且视当前的缓存请求为“不命中”
	 */
	public boolean isValid=true;
	
	/**
	 * 返回缓存对象的内容
	 * @return 以byte[] 形式表示的缓存对象的内容
	 */
	public abstract byte[] getContent();
	
	/**
	 * 设置缓存对象的内容
	 * 
	 */
	public abstract void setContent(byte[] bytes);

	/**
	 * @return 能唯一表示当前对象的字符串标识。
	 */
	public abstract String identity();

	/**
	 * 当调用此方法时表示在缓存中命中当前对象一次。
	 * 
	 * @return 表示缓存权重的因子数。在缓存不够用的情况下，权重小的对象会被优先清理出缓存。
	 */
	protected long hit() {
		return weight();
	}

	/**
	 * @return 表示缓存权重的因子数，子类应该实现自己的特定权重算法，以在缓存中实现不同的清除规则。
	 *         默认的实现始终返回相同的权重，缓存将按先进先出的规则清除。
	 */
	protected long weight() {
		return 0;
	}
	/**
	 * 缓存对象清理自己
	 */
	abstract void dump();
	@Override
	public boolean equals(Object obj) {

		try{
			CacheableObject o=(CacheableObject)obj;
			//Log.d("CacheableObject<><>",this.identity().equals(o.identity())+"---"+this.identity()+","+o.identity());
			return this.identity().equals(o.identity());			
		}
		catch(Exception ex){
			
			Log.e("CacheableObject","Error in CacheableObject.equals(...) ("+this.identity()+")"+ex.getMessage(),ex);
			return false;
		}
	}
	
	/**
	 * 
	 * 封装用于构造可缓存对象的identity算法
	 * @param seeds 用于构造identity的字符串数据
	 * @return 表示identity的字符串
	 * 
	 */
	static String constructIdentity(String seeds[]){
		if(seeds==null||seeds.length==0){
			
			return null;
		}
		if(seeds.length==1){
			return seeds[0];
		}
		String identity=seeds[0];
		for(int i=1;i<seeds.length;i++){
			identity+="-"+seeds[i];			
		}
		return identity;
	}
	
	/**
	 * 
	 * 比较器，比较两个CacheableObject的优先级（权重）大小。
	 * @author xu jianjun, 2011-11-21
	 *
	 */
	static class Comparator implements java.util.Comparator<CacheableObject>{

		@Override
		public int compare(CacheableObject o1, CacheableObject o2) {
			if(o1.weight()<o2.weight()){
				
				return -1;
			}
			else if(o1.weight()>o2.weight()){
				
				return 1;
			}
			else{
				return 0;
			}
		}
	}


}
