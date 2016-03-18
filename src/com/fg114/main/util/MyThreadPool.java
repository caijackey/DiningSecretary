package com.fg114.main.util;

import java.util.LinkedList;
import java.util.Stack;

import com.fg114.main.app.Fg114Application;

import android.util.Log;

//守护进程：监视工作线程的状态，维护工作线程的数量和状态
//工作线程：由守护进程创建，从任务队列中取任务并“有条件的执行”
//author: xu jianjun,2012-01-30

public class MyThreadPool {

	private String name = "MyThreadPool";
	private volatile boolean isRunning;
	private boolean debug = false;
	// 时间阀(毫秒)
	private int keeperInterval = 1000;
	private int workerInterval = 0;
	private final Object locker = new Object();
	private int defalutWorkerPriority = Thread.NORM_PRIORITY;
	private int id = 0;

	// 是否通过当前任务的优先级动态调整worker的优先级
	private boolean adjustWorkerPriorityReferToTask = false;

	// 池容量
	private int poolSize = 0;

	// 工作线程池
	LinkedList<Thread> threadPool = new LinkedList<Thread>();

	// 任务队列
	Stack<Task> taskList = new Stack<Task>();
	// LinkedBlockingQueue<Thread> taskList=new LinkedBlockingQueue<Thread>();


	// 守护线程
	Thread keeper = new Thread(new Runnable() {

		@Override
		public void run() {
			while (isRunning) {
				Log.i(name, "mythreadpool running ...");
				try {
					Thread.sleep(keeperInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 检查线程池有效线程数量
				int count = 0;
				for (int i = 0; i < threadPool.size(); i++) {
					if (threadPool.get(i) == null || !threadPool.get(i).isAlive()) {
						threadPool.remove(i);
					} else {
						count++;
					}
				}
				// 维护线程池中线程数量
				for (int i = 0; i < poolSize - count; i++) {
					Worker worker = new Worker();
					worker.setName(name + "#worker#" + (id++));
					worker.setPriority(defalutWorkerPriority);
					worker.start();
					threadPool.add(worker);
				}
			}
		}
	});

/*	// 工作线程
	class Worker extends Thread {

		public void run() {
			while (isRunning) {
				Task task = null;
				
				// ---取任务---//
				try {
					if(debug) Log.d("MyThreadPool",this.getName()+", "+taskList.size());
					if (taskList.size() == 0) {
						waitTask();
					}
					task = taskList.pop();
					// task=getOneTask();
					if (adjustWorkerPriorityReferToTask) {
						this.setPriority(task.getPriority());
					}
				} catch (Exception e1) {
					//e1.printStackTrace();
					waitTask();
					if(debug) Log.d("MyThreadPool","waitTask! "+this.getName());
					continue;
				}
				//Log.d("---------预备１","创建到[ 此时 ]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
				
				// ---间隔---//
				if (workerInterval > 0) {
					try {
						Thread.sleep(workerInterval);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// ---如果可放弃，则不执行---//
				if (task instanceof Discardable && ((Discardable) task).discardMe()) {
					if (debug) {
						Log.w(this.getName(), "discarded the thread! " + task);
					}
					continue;
				}
				// ---执行任务---//
				try {
					
					//Log.d("---------开始执行任务了","创建到[开始执行任务]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
					task.run();
					//Log.d("---------执行完毕","创建到[执行完毕]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// shutdown logic
			if (!isRunning) { 
				taskList.clear();
				synchronized (locker) {
					locker.notifyAll();
				}
			}
		}
	}*/

	public void waitTask() {
		synchronized (locker) {
			try {
				locker.wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// “可丢弃”线程需实现的接口，一种可以告知线程池在准备执行前可以放弃的线程。
	public interface Discardable {
		public boolean discardMe();
	}

	//
	public MyThreadPool(int poolSize) {
		this(poolSize, false, -1, 0,null);
	}

	public MyThreadPool(int poolSize, boolean adjustWorkerPriorityReferToTask, int defalutWorkerPriority, int workerInterval, String poolName) {
		if(poolName!=null && !"".equals(poolName)){
			this.name=this.name+" ["+poolName+"] ";
		}
		init(poolSize);
		taskList.ensureCapacity(100);
		this.adjustWorkerPriorityReferToTask = adjustWorkerPriorityReferToTask;
		if (defalutWorkerPriority >= Thread.MIN_PRIORITY && defalutWorkerPriority <= Thread.MAX_PRIORITY) {
			this.defalutWorkerPriority = defalutWorkerPriority;
		}
		this.workerInterval = workerInterval;
	}

	public MyThreadPool(int poolSize, int defalutWorkerPriority) {
		this(poolSize, false, defalutWorkerPriority, 0,null);
	}

	public MyThreadPool(int poolSize, int defalutWorkerPriority, int workerInterval) {
		this(poolSize, false, defalutWorkerPriority, workerInterval,null);
	}
	public MyThreadPool(int poolSize, int defalutWorkerPriority, int workerInterval, String poolName) {
		this(poolSize, false, defalutWorkerPriority, workerInterval,poolName);
	}

	private void init(int poolSize) {
		this.poolSize = poolSize;
		this.isRunning = true;
		/*keeper.setDaemon(true);
		keeper.setName(name + "#keeper");
		keeper.setPriority(Thread.MIN_PRIORITY);
		keeper.start();*/
	}

	//
	public int getPoolSize() {
		return poolSize;
	}

/*	public boolean submit(Task t) {
		taskList.push(t);
		synchronized (locker) {
			locker.notifyAll();
		}
		//Log.d("---------提交完毕","创建到提交的时间间隔："+(System.currentTimeMillis()-t.getTimestamp())+" ms ------> "+t);
		return true;
	}*/

	private Worker workerThread=null;
	
	public boolean submit(Task t) {
		taskList.push(t);
		if(workerThread ==null){
			workerThread = new Worker();
			workerThread.start();
		}
		return true;
	}
	
	// 工作线程
	class Worker extends Thread {

			public void run() {
				
				while (taskList.size()>0) {
					if (!ActivityUtil.isOnForeground(Fg114Application.getInstance().getApplicationContext())) {
					IOUtils.writeTestInfo(Fg114Application.getInstance().getApplicationContext(), "log_OpenPageDataTracer.txt", "跟踪>>>上传--任务数量"+taskList.size()+"---------"+CalendarUtil.getDateTimeString()+"\r\n");
					}
					Task task = null;
					Log.i("MyThreadPool",this.getName()+", "+taskList.size());
					// ---取任务---//
					try {
						if(debug) Log.d("MyThreadPool",this.getName()+", "+taskList.size());
						task = taskList.pop();
						// task=getOneTask();
						if (adjustWorkerPriorityReferToTask) {
							this.setPriority(task.getPriority());
						}
					} catch (Exception e1) {
						if(debug) Log.d("MyThreadPool","waitTask! "+this.getName());
						continue;
					}
					//Log.d("---------预备１","创建到[ 此时 ]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
					
					// ---如果可放弃，则不执行---//
					if (task instanceof Discardable && ((Discardable) task).discardMe()) {
						if (debug) {
							Log.w(this.getName(), "discarded the thread! " + task);
						}
						continue;
					}
					// ---执行任务---//
					try {
						
						//Log.d("---------开始执行任务了","创建到[开始执行任务]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
						task.run();
						//Log.d("---------执行完毕","创建到[执行完毕]的时间间隔："+(System.currentTimeMillis()-task.getTimestamp())+" ms ------> "+task);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(workerInterval);
				} catch (Exception e) {
					e.printStackTrace();
				}
				workerThread = null;
			}
			
		}
	
	
	/**
	 * @deprecated
	 */
	public void shutdown() {
		this.isRunning = false;
		synchronized (locker) {
			locker.notifyAll();
		}
	}
	
	/**
	 * 向线程池提交的任务
	 * 
	 */
	public static abstract class Task implements Runnable {
		
		// ------------------------------ Fields
		private String name = "task";
		private int priority = Thread.NORM_PRIORITY;
		private long timestamp = System.currentTimeMillis();
		private String keywords;

		// ------------------------------ Construtors
		public Task() {
			super();
		}
		
		public Task(long timestamp) {
			super();
			this.timestamp = timestamp;
		}
		
		// ------------------------------ getters and setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
				priority = Thread.NORM_PRIORITY;
			}
			this.priority = priority;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}

	/**
	 * 向线程池提交的任务,实现Discardable接口
	 * 
	 */
	public static abstract class Task0 extends Task implements Discardable {
		
		// ------------------------------ Fields
		private String name = "Task0";
		private int priority = Thread.NORM_PRIORITY;
		private long timestamp = System.currentTimeMillis();
		private String keywords;

		// ------------------------------ Construtors
		public Task0() {
			super();
		}
		
		public Task0(long timestamp) {
			super();
			this.timestamp = timestamp;
		}
		
		// ------------------------------ getters and setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
				priority = Thread.NORM_PRIORITY;
			}
			this.priority = priority;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}

}
