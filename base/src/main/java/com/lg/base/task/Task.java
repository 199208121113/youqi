package com.lg.base.task;

import android.os.Process;

import com.lg.base.core.LogUtil;
import com.lg.base.core.MessageSendListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class Task<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final String tag = this.getClass().getSimpleName();

	private final String lock = new String("TASKLOCK");

	private String id = null;

    private String name = null;

    /** 状态 */
	private volatile Status mStatus = Status.PENDING;

    /** 进度 */
    private volatile Progress mProgress = null;

	private transient Callable<Object> mCallable = null;

	private transient FutureTask<Object> mFuture = null;

	private transient OnStateChangeListener listener = null;

    /** 参数 */
	private HashMap<String, Serializable> params = null;

	private int flags = 0;

	/** 当前是第几次重试 ，但这次还没有执行 */
	private volatile int currentRetryCount = 0;

	private volatile int maxRetryCount = 0;
	
	private transient MessageSendListener sender = null;

	protected abstract T doInBackground() throws Exception;

	private Task getThis() {
		return this;
	}

	public boolean isNotTimely() {
		return (getFlags() & FLAG_TIMELY) == 0 && (getFlags() & FLAG_TIMELY_POOL) == 0;
	}

	public Task() {
		initFuture();
	}

	private void initWorker() {
        mCallable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (getStatus() != Status.PENDING) {
					setStatus(Status.PENDING);
				}
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				setStatus(Status.RUNNING);
				// (1) 重试次数为<=0的
				if (getMaxRetryCount() <= 0) {
					return doInBackground();
				}
				if (getCurrentRetryCount() > getMaxRetryCount()) {
					throw new Exception("Has been reached maxRetryCount:" + getMaxRetryCount());
				}
				// (2)重试次数大于0的，首先先把自己的那一次先执行了来，然后再看是否需要重试
				Object obj = null;
				boolean needRetry = false;
				Exception firstThrowable = null;
				try {
					obj = doInBackground();
				} catch (Exception e) {
					firstThrowable = e;
					needRetry = true;
				}
				if (!needRetry)
					return obj;
				final int maxRetryCount = getThis().getMaxRetryCount();
				if (isNotTimely()) { // 持久化任务
					LogUtil.e(tag, "task[Id:" + getId() + ",name="+getName()+"] retry " + getCurrentRetryCount() + " failed");
					setCurrentRetryCount(getCurrentRetryCount() + 1);
					throw firstThrowable;
				}
				// 临时性任务
				int retryCount = 0;
				while (retryCount < maxRetryCount) {
					try {
						Thread.sleep(10L);
						retryCount++;
						obj = doInBackground();
						LogUtil.e(tag, "taskId:" + getId() + " retry " + retryCount + " sucessed");
						break;
					} catch (Exception e) {
						LogUtil.e(tag, "taskId:" + getId() + " retry " + retryCount + " failed");
						getThis().setCurrentRetryCount(retryCount);
						if (retryCount >= maxRetryCount) {
							throw new Exception("Has been reached maxRetryCount:" + maxRetryCount, e);
						}
					}
				}
				return obj;
			}
		};
	}

	private void initFuture() {
		if (mCallable == null) {
			initWorker();
		}
		mFuture = new FutureTask<Object>(mCallable) {
			@Override
			protected void done() {
				Object result = null;
				try {
					result = get();
				} catch (InterruptedException e) {
					android.util.Log.w(tag, e);
				} catch (ExecutionException e) {
					setStatus(Status.ERROR_STOPED);
					listener.onError(e, getThis());
					return;
				} catch (CancellationException e) {
					setMaxRetryCount(0);
					setStatus(Status.CANCELED);
					listener.onCanceled(getThis());
					return;
				} catch (Throwable t) {
					setStatus(Status.ERROR_STOPED);
					listener.onError(t, getThis());
					return;
				}

				setStatus(Status.FINISHED);
				listener.onSuccess(result, getThis());
			}
		};
	}

	public void destoryFuture() {
        if(mFuture == null)
            return;
        if(mFuture.isDone()){
           mFuture =null;
           return;
        }
        if(!mFuture.isCancelled()) {
            mFuture.cancel(true);
        }
        mFuture.run();
		this.mFuture = null;
	}

	public final boolean isCancelled() {
		return mFuture.isCancelled();
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		boolean canceled = mFuture.cancel(mayInterruptIfRunning);
		return canceled;
	}

	protected final void setProgress(Progress progress) {
        if(progress != null){
            this.mProgress = progress;
            this.listener.onProgressUpdate(progress, this);
        }
	}

    public final Progress getProgress(){
        return this.mProgress;
    }

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public FutureTask<Object> getFuture() {
		if (this.mFuture == null)
			initFuture();
		return this.mFuture;
	}

	public final Status getStatus() {
		return mStatus;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setOnStateChangeListener(OnStateChangeListener oscl) {
		this.listener = oscl;
	}

	public void setParams(HashMap<String, Serializable> params) {
		this.params = params;
	}

	protected HashMap<String, Serializable> getParams() {
		return this.params;
	}

	public int getCurrentRetryCount() {
		synchronized (lock) {
			return currentRetryCount;
		}
	}

	public void setCurrentRetryCount(int currentRetryCount) {
		synchronized (lock) {
			this.currentRetryCount = currentRetryCount;
		}
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	private void setStatus(Status mNewStatus) {
		Status oldStatus = this.mStatus;
		this.mStatus = mNewStatus;
		if (mNewStatus.ordinal() != oldStatus.ordinal()) {
			listener.onStatusChanged(this, mNewStatus, oldStatus);
		}
	}

	public void setSender(MessageSendListener sender) {
		this.sender = sender;
	}
	public MessageSendListener getSender() {
		return sender;
	}

    // flags
    public static final int FLAG_AUTO_DELETE = 1 << 1;
    public static final int FLAG_TIMELY = 1 << 2;
    public static final int FLAG_VISIBLE = 1 << 3;
    public static final int FLAG_TIMELY_POOL = 1 << 4;

    /** 状态发生变化后的回调接口 */
    public interface OnStateChangeListener {

        void onSuccess(Object result, Task t);

        void onError(Throwable e, Task t);

        void onProgressUpdate(Progress progress, Task t);

        void onCanceled(Task t);

        void onCreated(Task t);

        void onStatusChanged(Task t, Status newStatus, Status oldStatus);
    }

    /** 任务的执行进度 */
    public static class Progress implements Serializable {
        private static final long serialVersionUID = 1L;
        private int total;
        private int current;
        private int scale;
        /** 1:下载 */
        private TaskType taskType=TaskType.download;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getScale() {
            return Math.min(scale,100);
        }

        public void setScale(int scale) {
            this.scale = scale;
        }
        private String taskId;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

		public TaskType getTaskType() {
			return taskType;
		}

		public void setTaskType(TaskType taskType) {
			this.taskType = taskType;
		}

		private String logMsg="";

		public static long getSerialVersionUID() {
			return serialVersionUID;
		}

		public String getLogMsg() {
			return logMsg;
		}

		public void setLogMsg(String logMsg) {
			this.logMsg = logMsg;
		}
	}

	public TaskType getTaskType() {
		return TaskType.download;
	}

}
