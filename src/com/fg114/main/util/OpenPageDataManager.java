package com.fg114.main.util;

import java.util.LinkedList;
import java.util.List;

import com.fg114.main.app.Settings;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.task.UploadOpenPageDataTask;
import com.google.xiaomishujson.Gson;
import com.google.xiaomishujson.reflect.TypeToken;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

/**
 * 页面统计信息管理器
 * 逻辑：页面统计信息队列中只保存最新的20条，每天只上传一次（满20条后触发上传，不够20条时跨天也不上传）
 * 
 * 格式：
 * 网络情况\tip\t页面名称\t打开的时间\t页面整体打开耗时\t页面http查询耗时\t页面查询url\n
	
	其中网络情况: wifi,3g,2g
	其中页面名称： 搜索页:search  详细页:detail   实时餐位页:real
	打开的时间:	进入onCreate时的时间
	页面整体打开耗时:	(第一次执行完task并且执行完“成功runnable”方法的时间) - 进入onCreate时的时间
	页面http查询耗时:	(第一次开始执行task时的时间) - (第一次执行完task并且进入“成功runnable”方法的时间)
	时间单位全部是毫秒数
 * @author xujianjun,2012-11-28
 *
 */
public class OpenPageDataManager {
	long timestamp=0; //0表示未发送
	int maxRecords=20; //一次上传的最大数量
	Context context=ContextUtil.getContext();
	private final String CACHE_KEY="OpenPageData_CACHE_KEY"+ActivityUtil.getVersionName(context);
	private static OpenPageDataManager instance=new OpenPageDataManager();
	LinkedList<OpenPageData> data=fromCache();
	
	public static OpenPageDataManager getInstance(){
		return instance;
	}
	
	private OpenPageDataManager(){		
	}
	
	//进入onCreate时调用，调用者应该持有该方法返回的OpenPageData对象，并在调用其他方法时传入该对象
	public OpenPageData pageStart(String pageName){
		OpenPageData p=new OpenPageData();
		p.networkState=ActivityUtil.getCurrentNetWork();
		p.ip=ActivityUtil.getLocalIpAddress();
		p.pageName=pageName;
		p.pageStart=System.currentTimeMillis();
		return p;
	}
	//task的runnable执行完毕时
	public void pageEnd(OpenPageData pd){
		if(pd==null){
			return;
		}
		pd.pageEnd=System.currentTimeMillis();
		if(pd.isValid()){
			addOpenPageData(pd);
		}
	}
	//网络task任务开始执行时
	public void taskStart(OpenPageData pd){
		if(pd==null){
			return;
		}
		
		pd.taskStart=System.currentTimeMillis();
		
	}
	//网络task任务执行结束时（runnable刚进入时）
	public void taskEnd(OpenPageData pd,String pageUrl){
		if(pd==null){
			return;
		}
		pd.pageUrl=pageUrl;
		pd.taskEnd=System.currentTimeMillis();
	}
	
	//添加一个数据到缓存中
	private synchronized void addOpenPageData(OpenPageData pd) {
		data.addLast(pd);
		//Log.d("OpenPageDataManager.addOpenPageData","加入"+pd.toString());
		//Log.d("OpenPageDataManager.addOpenPageData","队列大小"+data.size());
		//大于20条
		if(data.size()>=maxRecords){
			//近一天没有上传过
			if(System.currentTimeMillis()-timestamp>86400000){
				final LinkedList<OpenPageData> recentList=pollRecentRecords();
				UploadOpenPageDataTask task=new UploadOpenPageDataTask(null,context,listToString(recentList));
				task.execute(new Runnable() {
					
					@Override
					public void run() {
						//执行成功
						timestamp=System.currentTimeMillis();
						toCache();
					}
				},new Runnable() {
					
					@Override
					public void run() {
						//执行失败
						restoreOpenPageData(recentList);
						timestamp=0;
						toCache();
					}
				});
			}else{
				//清理队列，维持最大数量
				while(data.size()>maxRecords){
					data.removeFirst();
				}
				toCache();
			}
		}
	}
	//取出最近的maxRecords条记录
	private LinkedList<OpenPageData>  pollRecentRecords(){
		LinkedList<OpenPageData> sublist=new LinkedList<OpenPageData>();
		for(int i=0;i<maxRecords;i++){
			sublist.addFirst(data.removeLast());
		}
		return sublist;
	}
	//上传失败时，将没传成功的数据放回列表，以便有机会最大限度的实现重传
	private synchronized void restoreOpenPageData(LinkedList<OpenPageData> list){
		for(int i=0;i<list.size();i++){
			data.addFirst(list.removeLast());
		}
	}
	//将OpenPageData的list格式化成所要求的格式的字符串
	// 格式：
	// 网络情况\tip\t页面名称\t打开的时间\t页面整体打开耗时\t页面http查询耗时\t页面查询url\n
	private String listToString(LinkedList<OpenPageData> list){
		if(list==null){
			return "";
		}
		StringBuilder sb=new StringBuilder(1024*3);
		for(OpenPageData d : list){
			sb.append(d.toString());
		}
		return sb.toString();
	}
	
	//----
	public static class OpenPageData{
		String networkState="";
		String ip="";
		String pageName="";
		String pageUrl="";
		long pageStart=-1;
		long pageEnd=-1;
		long taskStart=-1;
		long taskEnd=-1;
		
		OpenPageData(){
			
		}
		boolean isValid(){
			if(CheckUtil.isEmpty(networkState)
					||CheckUtil.isEmpty(ip)
					||CheckUtil.isEmpty(pageName)
					||CheckUtil.isEmpty(pageUrl)
					||pageStart==-1
					||pageEnd==-1
					||taskStart==-1
					||taskEnd==-1
					){
				return false;
			}
			return true;
		}
		public String toString(){
			StringBuilder sb=new StringBuilder(1024);
			sb.append(networkState+"\t").append(ip+"\t").append(pageName+"\t");
			sb.append(pageStart+"\t").append((pageEnd-pageStart)+"\t").append((taskEnd-taskStart)+"\t");
			sb.append(pageUrl+"\n");
			return sb.toString();
		}
	}
	
	//将当前跟踪数据存入缓存
	private void toCache(){

		String json = new Gson().toJson(data);
		ValueCacheUtil.getInstance(context).remove(CACHE_KEY, CACHE_KEY);
		ValueCacheUtil.getInstance(context).add(CACHE_KEY, CACHE_KEY, json,"","",24*60*30); 

	}
	//将缓存中的数据恢复到内存中
	private LinkedList<OpenPageData> fromCache(){
		LinkedList<OpenPageData> data=null;
		ValueObject vo=ValueCacheUtil.getInstance(context).get(CACHE_KEY, CACHE_KEY); 
		if(vo!=null&&vo.getValue()!=null){
			data=new Gson().fromJson(vo.getValue(),new TypeToken<LinkedList<OpenPageData>>(){}.getType());			
		}else{
			data=new LinkedList<OpenPageData>();
		}
		return data;
	}
}
