package com.fg114.main.analytics;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.fg114.main.analytics.dto.PageEventData;
import com.fg114.main.analytics.dto.PageStatsData;
import com.fg114.main.analytics.dto.PageStatsPackData;
import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.MainFrameActivity;
import com.fg114.main.app.activity.chat.XiaomishuChat;
import com.fg114.main.cache.ValueCacheUtil;
import com.fg114.main.cache.ValueObject;
import com.fg114.main.service.http.ServiceRequest;
import com.fg114.main.service.task.CommonTask;
import com.fg114.main.service.task.CommonTask.TaskListener;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.FileIOUtil;
import com.fg114.main.util.IOUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.MyThreadPool;
import com.fg114.main.util.SessionManager;
import com.fg114.main.util.ZipUtils;
import com.google.xiaomishujson.Gson;
import com.google.xiaomishujson.reflect.TypeToken;

/**
 * 页面统计信息管理器 逻辑：页面统计信息队列中满maxRecords条时触发一次上传， 如果当时上传不成功，保存为一个文件，
 * 由一个线程在后台负责定期上传和清理这些文件 时间单位全部是毫秒数
 * 
 * @author xujianjun,2013-05-20
 * 
 */
public class OpenPageDataTracer {
	public static boolean upTag = false;
	boolean isDebug = false;
	int maxRecords = 5; // 触发上传的阀值
	Context context = ContextUtil.getContext();
	String filePath = context.getFilesDir().getPath();
	// 上传及缓存维护工作者池，单线程池
	private static MyThreadPool uploadWorkerPool = new MyThreadPool(1, -1,
			6000, "OpenPageDataTracer");
	// ---------------------
	private final String CACHE_KEY = "OpenPageData_CACHE_KEY"
			+ ActivityUtil.getVersionName(context);
	private static OpenPageDataTracer instance = new OpenPageDataTracer();
	ArrayList<PageStatsData> dataList = new ArrayList<PageStatsData>();
	// 点击数据保存的文件扩展名
	private String fileExt = ".event";

	public static OpenPageDataTracer getInstance() {
		return instance;
	}

	private OpenPageDataTracer() {
	}

	// 判断是否在onRestart方法里
	private boolean isInRestartMethod() {
		StackTraceElement[] all = Thread.currentThread().getStackTrace();
		for (int i = 0; i < all.length; i++) {
			if (all[i].getMethodName().equals("enterPage")
					&& (i + 1) < all.length
					&& all[i + 1].getMethodName().equals("onRestart")) {
				return true;
			}
		}
		return false;
	}

	// 进入onCreate时调用，调用者应该持有该方法返回的OpenPageData对象，并在调用其他方法时传入该对象
	// xuuid根据不同的页面传
	public synchronized PageStatsData enterPage(String pageName, String xuuid) {
		return enterPage(pageName, xuuid, true);
	}

	// 进入onCreate时调用，调用者应该持有该方法返回的OpenPageData对象，并在调用其他方法时传入该对象
	// xuuid根据不同的页面传
	public synchronized PageStatsData enterPage(String pageName, String xuuid,
			boolean inTag) {
		if (CheckUtil.isEmpty(pageName)) {
			return null;
		}
		if (isDebug)
			Thread.dumpStack();
		PageStatsData page = new PageStatsData();
		try {
			if (isInRestartMethod() || !inTag) {
				page.inTag = false; // 表示是返回页面，而不是进入页面
				// Log.d("返回页面拉","返回页面拉");
			}
			page.n = pageName; // 页面名称
			page.ctid = SessionManager.getInstance().getCityInfo(context)
					.getId(); // 城市id
			page.et = "" + (System.currentTimeMillis() + Settings.TIME_DIFF); // 进入时间
			// 如果软件曾经切换出去的，当再进入页面时，标示此页面是已上传过的
			if (upTag) {
				page.upTag = true;
				upTag = false;
			}
			// 设置id
			if ("餐厅详情".equals(pageName)) {
				page.rid = xuuid; // 餐馆id
			} else if ("餐厅图片".equals(pageName)) {
				page.rid = xuuid; // 餐馆id
			} else if ("餐厅评论".equals(pageName)) {
				page.rid = xuuid; // 餐馆id
			} else if ("餐厅基本信息".equals(pageName)) {
				page.rid = xuuid; // 餐馆id
			} else if ("餐厅地图".equals(pageName)) {
				page.rid = xuuid; // 餐馆id
			} else if ("订单详情".equals(pageName)) {
				page.oid = xuuid; // 订单id
			} else if ("现金券详情".equals(pageName)) {
				page.cid = xuuid; // 现金券id
			} else if ("现金券表单".equals(pageName)) {
				page.cid = xuuid; // 现金券id
			} else if ("现金券成功提示".equals(pageName)) {
				page.cid = xuuid; // 现金券id
			}
			// ------------------------------------------
			// page.fid = ""; // 菜品id
			// page.rid = ""; // 餐馆id
			// page.gid = ""; // 奖品id
			// page.cid = ""; // 现金券id
			// page.pid = ""; // 产品id
			// page.oid = ""; // 订单id
			// ------------------------------------------
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// --加入队列
		addPageStatsData(page);
		return page;
	}

	public synchronized void addEvent(String eventName) {
		addEvent(eventName, "");
	}

	public synchronized void addEvent(String eventName, String memo) {
		try {
			if (CheckUtil.isEmpty(eventName)) {
				return;
			}
			String etime = (System.currentTimeMillis() + Settings.TIME_DIFF)
					+ "";
			PageEventData event = new PageEventData();
			event.n = eventName;
			event.st = etime;
			event.et = etime;
			event.me = memo;
			if (dataList.size() > 0) {
				dataList.get(dataList.size() - 1).el.add(event);// 事件入最后一个PageEventData
			}
			if (isDebug)
				Log.w("跟踪>>>添加事件:" + eventName, event.hashCode() + "--" + event);
			// ------------------------------------------
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public synchronized void endEvent(String eventName) {
		try {

			if (CheckUtil.isEmpty(eventName)) {
				return;
			}
			long timestamp = System.currentTimeMillis() + Settings.TIME_DIFF;
			PageEventData selectedEvent = null;
			if (dataList.size() > 0) {
				// 从后向前查找事件名称，如果匹配则设置结束时间，找最前面的
				for (int i = dataList.size() - 1; i >= 0; i--) {
					PageStatsData pageData = dataList.get(i);
					for (int j = pageData.el.size() - 1; j >= 0; j--) {
						PageEventData event = pageData.el.get(j);
						if (event.n.equals(eventName)
								&& Long.parseLong(event.et) == Long
										.parseLong(event.st)
								&& Long.parseLong(event.st) < timestamp) {
							selectedEvent = event;
						}
					}
					// 在本页面找到了，就使用，没有找到的话，可以到前个页面去找
					if (selectedEvent != null) {
						selectedEvent.et = String.valueOf(timestamp);
						break;
					}
				}
			}
			if (isDebug)
				Log.w("跟踪>>>结束事件:" + eventName, selectedEvent.hashCode() + "--"
						+ selectedEvent);
			// ------------------------------------------
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// 添加一个数据到缓存中
	private synchronized void addPageStatsData(PageStatsData page) {
		try {
			if (isDebug) {
				Log.w("跟踪>>>页面进入" + page.n, "[" + dataList.size() + ","
						+ maxRecords + "]" + page);
			}
			// 大于20条page，将当前队列中所有数据交给后台工作线程池去上传
			if (dataList.size() >= maxRecords) {
				uploadImmediately();
			}
			dataList.add(page);
			if (XiaomishuChat.getIsInit()) {
				XiaomishuChat.getInstance(context).addPageStateData(page);
			}

			// ------------------------------------------
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	Handler uploadPoolDataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			final String data = msg.getData().getString("data");
			final String[] uploadedFilenames = msg.getData().getStringArray(
					"uploadedFilenames");
			// ---
			ServiceRequest request = new ServiceRequest(
					ServiceRequest.API.uploadPageStats);
			try {
				request.addData(toInputStream(data));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}

			CommonTask.requestMutely(request, new TaskListener<Void>() {

				@Override
				protected void onSuccess(Void dto) {
					if (!ActivityUtil.isOnForeground(Fg114Application
							.getInstance().getApplicationContext())) {
						IOUtils.writeTestInfo(
								context,
								"log_OpenPageDataTracer.txt",
								"内存点击流上传成功---------"
										+ CalendarUtil.getDateTimeString()
										+ "\r\n");
					}
					if (isDebug)
						Log.w("跟踪>>>上传成功", "---" + data);

					// 执行成功，删除已上传的文件
					for (String f : uploadedFilenames) {
						try {
							new File(f).delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}

				@Override
				protected void onError(int code, String message) {
					if (!ActivityUtil.isOnForeground(Fg114Application
							.getInstance().getApplicationContext())) {
						IOUtils.writeTestInfo(
								context,
								"log_OpenPageDataTracer.txt",
								"内存点击流上传失败---------"
										+ CalendarUtil.getDateTimeString()
										+ "\r\n");
					}
					if (isDebug)
						Log.w("跟踪>>>上传失败", "---" + data);
					// 执行失败，删除上传失败文件
					for (String f : uploadedFilenames) {
						try {
							new File(f).delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// 执行失败，写入文件内容和缓存内容
					saveToFile(data);
				}

			});
		}

	};

	// Handler uploadFileDataHandler=new Handler(){
	//
	// @Override
	// public void handleMessage(Message msg) {
	// final String data=msg.getData().getString("data");
	// final String[]
	// uploadedFilenames=msg.getData().getStringArray("uploadedFilenames");
	// //---
	// ServiceRequest request=new
	// ServiceRequest(ServiceRequest.API.uploadPageStats);
	// Log.v("TAG", "文件点击流上传成功");
	// try {
	// request.addData(toInputStream(data));
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// return;
	// }
	// //---
	// CommonTask.requestMutely(request, new TaskListener<Void>() {
	//
	// @Override
	// protected void onSuccess(Void dto) {
	// if
	// (!ActivityUtil.isOnForeground(Fg114Application.getInstance().getApplicationContext()))
	// {
	// IOUtils.writeTestInfo(context, "log_OpenPageDataTracer.txt",
	// "文件点击流上传成功---------"+CalendarUtil.getDateTimeString()+"\r\n");
	// }
	// if(isDebug) Log.w("跟踪>>>上传文件数据","成功");
	// // 执行成功，删除已上传的文件
	// for (String f : uploadedFilenames) {
	// try {
	// new File(f).delete();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// @Override
	// protected void onError(int code, String message) {
	// // 执行失败，文件仍然保留
	// if
	// (!ActivityUtil.isOnForeground(Fg114Application.getInstance().getApplicationContext()))
	// {
	// IOUtils.writeTestInfo(context, "log_OpenPageDataTracer.txt",
	// "文件点击流上传失败---------"+CalendarUtil.getDateTimeString()+"\r\n");
	// }
	// if(isDebug) Log.w("跟踪>>>上传文件数据","失败");
	// }
	//
	// } );
	// }
	// };
	// 上传内存中点击数据-----------------------------
	class UploadPoolDataTask extends MyThreadPool.Task {
		PageStatsPackData packData;
		String data;

		public UploadPoolDataTask(PageStatsPackData packData) {
			if (!ActivityUtil.isOnForeground(Fg114Application.getInstance()
					.getApplicationContext())) {
				IOUtils.writeTestInfo(context, "log_OpenPageDataTracer.txt",
						"开始内存点击流上传---------" + CalendarUtil.getDateTimeString()
								+ "\r\n");
			}
			if (isDebug)
				Log.w("跟踪>>>上传", "---" + packData);
			this.packData = packData;
		}

		public void run() {
			if (packData == null) {
				return;
			}
			// data = JsonUtils.toJson(packData);

			// Message msg=uploadPoolDataHandler.obtainMessage(0);
			// Bundle bundle=new Bundle();
			// bundle.putString("data", data);
			// msg.setData(bundle);
			// uploadPoolDataHandler.sendMessage(msg);

			doUploadFileData(packData);
		}
	}

	// 处理上传数据 文件数据 加内存 数据 poolData为内存数据
	private void doUploadFileData(PageStatsPackData poolData) {
		PageStatsPackData packData = new PageStatsPackData();
		String data;
		// //上传文件数据时，每次最大处理的文件个数（把maxFilePerTime文件中的数据打包成一个包，一次上传）
		int maxFilePerTime = 3;

		// 收集最多maxFilePerTime个文件中的数据，打成一个包
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// ---------取出指定扩展名的文件
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File f, String name) {
				if (name.endsWith(fileExt)) {
					return true;
				}
				return false;
			}
		});
		// ---------合并数据
		int i = 0;
		final ArrayList<String> uploadedFilenames = new ArrayList<String>();

		packData.list.addAll(poolData.list);// 添加内存数据

		for (File f : files) { // 合并最多maxFilePerTime个文件中的数据
			if (i >= 5) {
				break;
			}
			PageStatsPackData pack = readFromFile(f.getAbsolutePath());
			uploadedFilenames.add(f.getAbsolutePath());// 记下文件名
			packData.list.addAll(pack.list);
			i++;
		}
		if (!ActivityUtil.isOnForeground(Fg114Application.getInstance()
				.getApplicationContext())) {
			IOUtils.writeTestInfo(context, "log_OpenPageDataTracer.txt",
					"开始文件点击流上传---------" + CalendarUtil.getDateTimeString()
							+ "\r\n");
		}
		if (isDebug)
			Log.w("跟踪>>>上传文件数据(" + i + ")", "---" + packData);
		// ---------
		data = JsonUtils.toJson(packData);
		// ---
		Message msg = uploadPoolDataHandler.obtainMessage(0);
		Bundle bundle = new Bundle();
		bundle.putString("data", data);
		bundle.putStringArray("uploadedFilenames",
				uploadedFilenames.toArray(new String[] {}));
		msg.setData(bundle);
		uploadPoolDataHandler.sendMessage(msg);
	}

	// // 上传文件中点击数据-----------------------------
	// class UploadFileDataTask extends MyThreadPool.Task {
	// PageStatsPackData packData;
	// String data;
	// // //上传文件数据时，每次最大处理的文件个数（把maxFilePerTime文件中的数据打包成一个包，一次上传）
	// int maxFilePerTime = 3;
	//
	// public UploadFileDataTask() {
	// packData = new PageStatsPackData();
	// }
	//
	// public void run() {
	// // 收集最多maxFilePerTime个文件中的数据，打成一个包
	// File dir = new File(filePath);
	// if (!dir.exists()) {
	// dir.mkdirs();
	// }
	// // ---------取出指定扩展名的文件
	// File[] files = dir.listFiles(new FilenameFilter() {
	//
	// @Override
	// public boolean accept(File f, String name) {
	// if (name.endsWith(fileExt)) {
	// return true;
	// }
	// return false;
	// }
	// });
	// // ---------合并数据
	// int i = 0;
	// final ArrayList<String> uploadedFilenames = new ArrayList<String>();
	// for (File f : files) { // 合并最多maxFilePerTime个文件中的数据
	// if (i >= 5) {
	// break;
	// }
	// PageStatsPackData pack = readFromFile(f.getAbsolutePath());
	// uploadedFilenames.add(f.getAbsolutePath());// 记下文件名
	// packData.list.addAll(pack.list);
	// i++;
	// }
	// if
	// (!ActivityUtil.isOnForeground(Fg114Application.getInstance().getApplicationContext()))
	// {
	// IOUtils.writeTestInfo(context, "log_OpenPageDataTracer.txt",
	// "开始文件点击流上传---------"+CalendarUtil.getDateTimeString()+"\r\n");
	// }
	// if(isDebug) Log.w("跟踪>>>上传文件数据("+i+")", "---"+packData);
	// // ---------
	// data = JsonUtils.toJson(packData);
	// //---
	// Message msg=uploadFileDataHandler.obtainMessage(0);
	// Bundle bundle=new Bundle();
	// bundle.putString("data", data);
	// bundle.putStringArray("uploadedFilenames", uploadedFilenames.toArray(new
	// String[]{}));
	// msg.setData(bundle);
	// uploadFileDataHandler.sendMessage(msg);
	// }
	// }

	// 保存到文件
	private void saveToFile(String data) {
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (isDebug)
			Log.w("跟踪>>>保存到文件", data);
		FileOutputStream writer = null;
		String fileName = filePath + File.separator
				+ SystemClock.currentThreadTimeMillis() + fileExt;
		try {
			writer = new FileOutputStream(fileName, false);
			writer.write(data.getBytes("utf-8"));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 读取文件
	private PageStatsPackData readFromFile(String fileName) {
		PageStatsPackData packData = new PageStatsPackData();
		FileInputStream reader = null;
		try {
			File f = new File(fileName);
			if (!f.exists() || f.isDirectory()) {
				return packData;
			}
			// ----
			if (isDebug)
				Log.w("跟踪>>>读取文件", fileName);
			byte[] buffer = new byte[(int) f.length()];
			reader = new FileInputStream(fileName);
			reader.read(buffer, 0, buffer.length);
			// FileIOUtil.fileWrite(context, "OKOKOK", new String(buffer));
			packData = JsonUtils.fromJson(new String(buffer, "utf-8"),
					PageStatsPackData.class);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return packData;
	}

	// 压缩字符串
	private InputStream toInputStream(String data)
			throws UnsupportedEncodingException {
		if (CheckUtil.isEmpty(data)) {
			data = "";
		}
		return new ByteArrayInputStream(ZipUtils.gZip(data.getBytes("UTF-8")));
	}

	/**
	 * 立即上传点击流事件。 目的，为了不丢失点击流，如果一退出软件界面（退出、跳转到别的软件、home键），就立即触发上传
	 */
	public void uploadImmediately() {

		// 将当前数据打包，提交到线程池
		PageStatsPackData packData = new PageStatsPackData();
		packData.list.addAll(dataList);
		dataList.clear();// 清空队列
		uploadWorkerPool.submit(new UploadPoolDataTask(packData));
	}
	// public void uploadFileData(){
	// uploadWorkerPool.submit(new UploadFileDataTask());
	// }
}
