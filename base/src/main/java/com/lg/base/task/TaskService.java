package com.lg.base.task;

import android.os.Message;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.BaseService;
import com.lg.base.core.DoWhat;
import com.lg.base.core.Location;
import com.lg.base.core.LogUtil;
import com.lg.base.core.MessageSendListener;
import com.lg.base.task.Task.Progress;
import com.lg.base.utils.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lg.base.task.TaskEvent.Operate.FLAG_CANCEL;
import static com.lg.base.task.TaskEvent.Operate.FLAG_CREATE;
import static com.lg.base.task.TaskEvent.Operate.FLAG_START;
import static com.lg.base.task.TaskEvent.Operate.FLAG_START_FROM_FILE;
import static com.lg.base.task.TaskEvent.Operate.FLAG_WATCH;

public class TaskService extends BaseService implements Task.OnStateChangeListener {

	public static final Location LOCATION = new Location(TaskService.class.getName());
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 10;
	private static final AtomicInteger mCount = new AtomicInteger(1);

	/** 持久化线程工厂 */
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "POOL-PERSISTENCE#" + mCount.getAndIncrement());
		}
	};

	/** 临时性线程工厂 */
	private static final ThreadFactory sThreadFactoryTimely = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "POOL-TIMELY#" + mCount.getAndIncrement());
		}
	};

	/** 持久化的任务线程池 */
	private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE/2), sThreadFactory);

	/** 临时性任务线程池 */
	private static final ThreadPoolExecutor sExecutorTimely = new ThreadPoolExecutor(6, 6, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE/2), sThreadFactoryTimely);

	/** 已经加入到线程池中的线程 */
	private static final ConcurrentHashMap<String, Task> sPollMap = new ConcurrentHashMap<String, Task>();

	/** 直接创建一个新的线程 */
	private static final Thread newThread(Runnable r) {
		return new Thread(r, "TIMELY#" + mCount.getAndIncrement());
	}

	/** Watcher 点对点的 (即一个watcher只观察一个任务) */
	private static final ConcurrentHashMap<String, IWatcherCallback<?>> watcherMap = new ConcurrentHashMap<String, IWatcherCallback<?>>();
	
	/** Watcher 点对多的 (即一个watcher观察所有任务) */
	private static final ArrayList<IWatcherCallback<?>> watcherList = new ArrayList<IWatcherCallback<?>>();
	
	private static final int WHAT_TASK_SUC = 0x1;
	private static final int WHAT_TASK_ERR = 0x2;
	private static final int WHAT_TASK_CANCEL = 0x3;

	private static final int WHAT_TASK_SAVE_TO_FILE = 0x4;
	private static final int WHAT_TASK_DELETE_TO_FILE = 0X5;
	private static final int WHAT_TASK_ADD_FROM_FILE = 0X6;

	private static final int WHAT_TASK_START_FROM_POLL = 0X7;
	private static final int WHAT_TASK_LOOP_ADD = 0X8;

	public static ConcurrentHashMap<String,Task> getPollTaskMap(TaskType taskType){
		if(taskType == TaskType.all) {
			return sPollMap;
		}else{
			ConcurrentHashMap<String,Task> map = new ConcurrentHashMap<>();
			for (String key : sPollMap.keySet()){
				Task task = sPollMap.get(key);
				if(task.getTaskType() != taskType){
					continue;
				}
				map.put(key,task);
			}
			return map;
		}
	}

	public static Task getTaskById(String taskId){
		if(sPollMap.containsKey(taskId)){
			return sPollMap.get(taskId);
		}
		return null;
	}
	/** 检测该任务列表中是否包含该任务 */
	public static boolean isExistsFromPollTaskMap(String taskId){
		return sPollMap.containsKey(taskId);
	}
	@Override
	protected void doCreate() {
		submitDoWhat(new DoWhat(WHAT_TASK_ADD_FROM_FILE));
	}

	@Override
	protected void doDestroy() {
		try {
			sExecutorTimely.shutdownNow();
			sExecutor.shutdown();
			watcherMap.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doExecuteMessage(Message msg) {
		if (msg.what == WHAT_TASK_LOOP_ADD)
			sendEmptyEventWaht(WHAT_TASK_START_FROM_POLL);
	}

	@Override
	protected void doExecuteEvent(BaseEvent evt) {
		if (evt instanceof MsgTaskEvent) {
			MsgTaskEvent mte = (MsgTaskEvent) evt;
			int what = mte.getWhat();
			if (what == WHAT_TASK_START_FROM_POLL) {
				startTasksFromPool();
				return;
			}
			String taskId = mte.getTaskId();
			Task t = sPollMap.get(taskId);
			if (what == WHAT_TASK_SUC) {
				removeTaskFromTool(taskId);
				removeWatcher(taskId);
				LogUtil.d(tag, "doExecuteMessage() taskId:" + taskId + " finished suc");
				if (t != null && t.isNotTimely()) {
					if ((t.getFlags() & Task.FLAG_AUTO_DELETE) == Task.FLAG_AUTO_DELETE) {
						submitDoWhatForDeleteTask(t);
					}
				}
			} else if (what == WHAT_TASK_ERR) {
				LogUtil.d(tag, "doExecuteMessage() taskId:" + taskId + " finished err");
				if (t != null && t.isNotTimely()) {
					if (t.getCurrentRetryCount() > t.getMaxRetryCount()) {
						removeTaskFromTool(taskId);
						removeWatcher(taskId);
						submitDoWhatForDeleteTask(t);
					} else {
						LogUtil.d(tag, "sExecutor.destoryFuture() obj=" + t.getFuture().toString());
						t.destoryFuture();
					}
				} else {
					removeTaskFromTool(taskId);
					removeWatcher(taskId);
				}
			} else if (what == WHAT_TASK_CANCEL) {
				removeTaskFromTool(taskId);
				removeWatcher(taskId);
				LogUtil.d(tag, "doExecuteMessage() taskId:" + taskId + " finished cancel");
				if (t != null && t.isNotTimely()) {
					if ((t.getFlags() & Task.FLAG_AUTO_DELETE) == Task.FLAG_AUTO_DELETE) {
						submitDoWhatForDeleteTask(t);
					}
				}
			}
			return;
		}
		if (evt instanceof TaskEvent) {
			try {
				procressTaskEvent((TaskEvent) evt);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doExecuteDoWhat(DoWhat doWhat) {
		super.doExecuteDoWhat(doWhat);
		if (doWhat.getWhat() == WHAT_TASK_SAVE_TO_FILE) {
			Task t = (Task) doWhat.getObj();
			saveTaskToFile(t);
		} else if (doWhat.getWhat() == WHAT_TASK_DELETE_TO_FILE) {
			Task t = (Task) doWhat.getObj();
			deleteTaskFromFile(t);
		} else if ((doWhat.getWhat() == WHAT_TASK_ADD_FROM_FILE)) {
			startTasksFromFile();
		}
	}

	// 任务回调----------begin---------------

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onSuccess(Object result, Task t) {
		ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
		for (IWatcherCallback ic : ls) {
			if (ic.isDisabled()) {
				removeWatcherFromList(ic);
			}else{
				if(ic.getTaskType() == TaskType.all || t.getTaskType() == ic.getTaskType()) {
					ic.onSuccess(t.getId(),result);
				}
			}
		}
		
		String tid = t.getId();
		IWatcherCallback watcher = watcherMap.get(tid);
		if (watcher != null) {
			if(watcher.isDisabled()){
				watcherMap.remove(tid);
			}else{
				watcher.onSuccess(t.getId(),result);
			}
		}
		ls.clear();
		ls = null;
		sendEventWhat(t.getId(), WHAT_TASK_SUC);
	}

	@Override
	public void onError(Throwable e, Task t) {
		ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
		for (IWatcherCallback<?> ic : ls) {
			if (ic.isDisabled()) {
				removeWatcherFromList(ic);
			}else{
				if(ic.getTaskType() == TaskType.all || t.getTaskType() == ic.getTaskType()) {
					ic.onError(t.getId(),e);
				}
			}
		}
		String tid = t.getId();
		IWatcherCallback<?> watcher = watcherMap.get(tid);
		if (watcher != null) {
			if(watcher.isDisabled()){
				watcherMap.remove(tid);
			}else{
				watcher.onError(t.getId(),e);
			}
		}
		ls.clear();
		ls = null;
		sendEventWhat(t.getId(), WHAT_TASK_ERR);
	}

	@Override
	public void onProgressUpdate(Progress progress, Task t) {
		ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
		for (IWatcherCallback<?> watcher : ls) {
			if (watcher.isDisabled()) {
				removeWatcherFromList(watcher);
			}else{
				if(watcher.getTaskType() == TaskType.all || t.getTaskType() == watcher.getTaskType()) {
					watcher.onProgressUpdate(t.getId(), progress);
				}
			}
		}
		ls.clear();
		ls = null;
		String tid = t.getId();
		IWatcherCallback<?> watcher = watcherMap.get(tid);
		if (watcher != null) {
			if(watcher.isDisabled()){
				watcherMap.remove(tid);
			}else{
				watcher.onProgressUpdate(t.getId(),progress);
			}
		}
	}

	@Override
	public void onCanceled(Task t) {
		ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
		for (IWatcherCallback<?> ic : ls) {
			if (ic.isDisabled()) {
				removeWatcherFromList(ic);
			}else{
				if(ic.getTaskType() == TaskType.all || t.getTaskType() == ic.getTaskType()) {
					ic.onCanceled(t.getId());
				}
			}
		}
		String tid = t.getId();
		IWatcherCallback<?> watcher = watcherMap.get(tid);
		if (watcher != null) {
			if(watcher.isDisabled()){
				watcherMap.remove(tid);
			}else{
				watcher.onCanceled(t.getId());
			}
		}
		ls.clear();
		ls = null;
		sendEventWhat(t.getId(), WHAT_TASK_CANCEL);
	}

	@Override
    public void onCreated(Task t) {
        ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
        for (IWatcherCallback<?> ic : ls) {
            if (ic.isDisabled()) {
                removeWatcherFromList(ic);
            }else{
				if(ic.getTaskType() == TaskType.all || t.getTaskType() == ic.getTaskType()) {
					ic.onCreated(t);
				}
            }
        }
        String tid = t.getId();
        IWatcherCallback<?> watcher = watcherMap.get(tid);
        if (watcher != null) {
            if(watcher.isDisabled()){
                watcherMap.remove(tid);
            }else{
                watcher.onCreated(t);
            }
        }
        ls.clear();
        ls = null;
        //sendEventWhat(t.getId(), WHAT_TASK_CANCEL);
    }

    @Override
	public void onStatusChanged(Task t, Status newStatus, Status oldStatus) {
		if (t.isNotTimely()) {
			submitDoWhatForSaveTask(t);
		}
		ArrayList<IWatcherCallback<?>> ls = getCloneWatcherList();
		for (IWatcherCallback<?> ic : ls) {
			if (ic.isDisabled()) {
				removeWatcherFromList(ic);
			}else{
				if(ic.getTaskType() == TaskType.all || t.getTaskType() == ic.getTaskType()) {
					ic.onStatusChanged(t.getId(), t, newStatus, oldStatus);
				}
			}
		}
		ls.clear();
		ls = null;
		IWatcherCallback<?> watcher = watcherMap.get(t.getId());
		if (watcher != null) {
			if(watcher.isDisabled()){
				watcherMap.remove(t.getId());
			}else{
				watcher.onStatusChanged(t.getId(), t, newStatus, oldStatus);
			}
		}
//		sendEventWhat(t.getId(),);
		
	}
	private ArrayList<IWatcherCallback<?>> getCloneWatcherList(){
		ArrayList<IWatcherCallback<?>> ll =  new ArrayList<IWatcherCallback<?>>();
		ll.addAll(watcherList);
		return ll;
	}
	
	private void addWatcherToList(IWatcherCallback<?> watcher){
		if(!watcherList.contains(watcher)){
			watcherList.add(watcher);
		}
	}
	
	private void removeWatcherFromList(IWatcherCallback<?> watcher){
		watcherList.remove(watcher);
	}
	
	// 任务回调----------end---------------

	private void procressTaskEvent(TaskEvent evt) throws Throwable {
		checkRunOnMain();
        final String taskId = evt.getTaskId();
		Task t = sPollMap.get(taskId);
		int flags = evt.getOperatorFlags();
		if ((FLAG_WATCH & flags) == FLAG_WATCH) {
			IWatcherCallback<?> watcher = evt.getWatcher();
			if(watcher != null){
				if(watcher.getType() == 1){ //只关注1个任务
					addWatcher(taskId, watcher);
				}else if(watcher.getType() == 2){ //关注多个 任务
					addWatcherToList(watcher);
				}
			}
		}
        if ((FLAG_CREATE & flags) == FLAG_CREATE) {
            if (t != null) {
                LogUtil.w(tag, "task : " + t.getId() + " has alreadly in thread pool");
                return;
            }
            t = createTask(evt);
            if(t != null) {
                this.onCreated(t);
            }
        }
		if ((FLAG_START & flags) == FLAG_START) {
			if (t != null)
				addTaskToTool(t);
		}
		if ((FLAG_CANCEL & flags) == FLAG_CANCEL) {
			if (t == null) {
				LogUtil.e(tag, "task : " + evt.getTaskId() + " not in thread pool");
				return;
			}
			t.cancel(true);
		}
		if ((FLAG_START_FROM_FILE & flags) == FLAG_START_FROM_FILE) {
			t = evt.getFromFileTask();
			if (t != null)
				addTaskToTool(t);
		}
	}

	private Task createTask(TaskEvent evt) throws Throwable {
		checkRunOnMain();
		Task task = (Task) evt.getClazz().newInstance();
		task.setId(evt.getTaskId());
		task.setParams(evt.getParams());
		task.setFlags(evt.getTaskFlags());
		task.setMaxRetryCount(evt.getMaxRetryCount());
        task.setName(evt.getTaskName());
		if (task.isNotTimely()) {
			submitDoWhatForSaveTask(task);
		}
		return task;
	}

	private void addTaskToTool(Task t) {
		checkRunOnMain();
		int flags = t.getFlags();
		t.setOnStateChangeListener(this);
		if ((flags & Task.FLAG_TIMELY) == Task.FLAG_TIMELY) {
			newThread(t.getFuture()).start();
		} else if ((flags & Task.FLAG_TIMELY_POOL) == Task.FLAG_TIMELY_POOL) {
			sExecutorTimely.execute(t.getFuture());
		} else if (t.isNotTimely()) {
			sExecutor.execute(t.getFuture());
			LogUtil.d(tag, "sExecutor.execute() obj=" + t.getFuture().toString());
		}
		sPollMap.put(t.getId(), t);
		t.setSender(this);
	}

	private void removeTaskFromTool(String taskId) {
		checkRunOnMain();
		sPollMap.remove(taskId);
	}

	private void addWatcher(String taskId, IWatcherCallback<?> watcher) {
		checkRunOnMain();
		watcherMap.put(taskId, watcher);
	}

	private void removeWatcher(String taskId) {
		checkRunOnMain();
		watcherMap.remove(taskId);
	}

	private void saveTaskToFile(Task t) {
		checkRunOnT3();
		String fullFileName = new File(this.getApplicationContext().getCacheDir(), t.getId() + ".tsk").getAbsolutePath();
		LogUtil.d(tag, "saveTaskToFile() status=" + t.getStatus());
		try {
			IOUtil.saveFileForBytes(fullFileName, IOUtil.serialize(t));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteTaskFromFile(Task t) {
		checkRunOnT3();
		String fullFileName = new File(this.getApplicationContext().getCacheDir(), t.getId() + ".tsk").getAbsolutePath();
		LogUtil.d(tag, "deleteTaskFromFile() status=" + t.getStatus());
		try {
			IOUtil.deleteByFilePath(fullFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startTasksFromFile() {
		checkRunOnT3();
		LogUtil.d(tag, "startTasksFromFile()");
		File file = this.getApplicationContext().getCacheDir();
		File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".tsk");
			}
		});
		for (File f : files) {
			Task t = null;
			try {
				t = (Task) IOUtil.unSerialize(new FileInputStream(f.getAbsolutePath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (t == null)
				continue;
			if (sPollMap.containsKey(t.getId()))
				continue;
			if (t.getStatus() == Status.FINISHED || t.getStatus() == Status.CANCELED) {
				continue;
			}
			if (t.getStatus() == Status.ERROR_STOPED) {
				TaskEvent te = new TaskEvent(LOCATION);
				te.setOperatorFlags(TaskEvent.Operate.FLAG_START_FROM_FILE);
				te.setTaskId(t.getId());
				te.setFromFileTask(t);
				sendEvent(te);
				LogUtil.d(tag, "startTasksFromFile() add taskId:" + t.getId() + " from file");
			}
		}
		sendEmptyMessageDelayed(WHAT_TASK_LOOP_ADD, 1000L);
	}

	private void startTasksFromPool() {
		checkRunOnMain();
		LogUtil.d(tag, "startTasksFromPool()");
		for (Task t : sPollMap.values()) {
			if (t.getStatus() == Status.ERROR_STOPED && t.isNotTimely()) {
				LogUtil.d(tag, "startTasksFromPool() execute(" + t.getId() + ")");
				sExecutor.execute(t.getFuture());
			}
		}
		sendEmptyMessageDelayed(WHAT_TASK_LOOP_ADD, 1000 * 60 * 60);
	}

	private void submitDoWhatForSaveTask(Task t) {
		DoWhat doWhat = new DoWhat(WHAT_TASK_SAVE_TO_FILE, t);
		submitDoWhat(doWhat);
	}

	private void submitDoWhatForDeleteTask(Task t) {
		DoWhat doWhat = new DoWhat(WHAT_TASK_DELETE_TO_FILE, t);
		submitDoWhat(doWhat);
	}

	private void sendEventWhat(String taskId, int what) {
		MsgTaskEvent mte = new MsgTaskEvent(LOCATION, what);
		mte.setTaskId(taskId);
		sendEvent(mte);
	}

	private void sendEmptyEventWaht(int what) {
		MsgTaskEvent mte = new MsgTaskEvent(LOCATION, what);
		sendEvent(mte);
	}
	
	public static <T> void addWatcher(MessageSendListener sender, IWatcherCallback<T> watcher,String taskId) {
		TaskEvent te = new TaskEvent(TaskService.LOCATION);
		te.setTaskId(taskId);
		te.setOperatorFlags(TaskEvent.Operate.FLAG_WATCH);
		te.setWatcher(watcher);
		sender.sendEvent(te);
	}
	private static final String TAG_ALL_WATHER = "#ALL";
	public static <T> void addWatcher(MessageSendListener sender, IWatcherCallback<T> watcher) {
		TaskEvent te = new TaskEvent(TaskService.LOCATION);
		te.setTaskId(TAG_ALL_WATHER+System.currentTimeMillis());
		te.setOperatorFlags(TaskEvent.Operate.FLAG_WATCH);
		te.setWatcher(watcher);
		sender.sendEvent(te);
	}
}
