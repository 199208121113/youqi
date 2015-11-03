package com.lg.base.core;

import android.app.Application;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.lg.base.receiver.NetWorkReceiver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseApplicationBak extends Application {
	protected final String tag = this.getClass().getSimpleName();
	private final MainThread mainThread = new MainThread("T2-MainThread");
	private static final ConcurrentLinkedQueue<BaseEvent> eventQueue = new ConcurrentLinkedQueue<BaseEvent>();
	private static final ConcurrentHashMap<String, MessageHandListener> ttListenerMap = new ConcurrentHashMap<String, MessageHandListener>();
	/** UI线程ID */
	private static volatile long uiTid = -1;

	private static final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (uiTid <= 0) {
				uiTid = Thread.currentThread().getId();
				Thread.currentThread().setName("T1-UI");
			}
			if (msg == null)
				return;
			if (msg.obj == null || (msg.obj instanceof BaseMessage) == false) {
				return;
			}
			BaseMessage ttMsg = (BaseMessage) msg.obj;
			String from = ttMsg.getFrom().getUri();
			MessageHandListener ttMsgListener = ttListenerMap.get(from);
			if (ttMsgListener == null)
				return;
			ttMsgListener.executeMessage(ttMsg.getMsg());
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mainThread.start();
		initConfigs();
		init();
	}
	/**
	 * 是否支持任务管理
	 * 是否支持天气预报
	 */
	protected abstract void initConfigs();

	private void init() {
		registReceivers();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mainThread.stopRunning();
		this.unregisterReceiver(mNetWorkReceiver);
	}

	void sendMessage(BaseMessage tmsg) {
		Message msg = handler.obtainMessage();
		msg.obj = tmsg;
		handler.sendMessage(msg);
	}

	void sendMessageDelayed(BaseMessage tmsg, long delayMillis) {
		Message msg = handler.obtainMessage();
		msg.obj = tmsg;
		handler.sendMessageDelayed(msg, delayMillis);
	}

	void sendEvent(BaseEvent evt) throws BaseException {
		if (evt == null)
			throw new BaseException("evt is null");
		if (evt.getFrom() == null)
			throw new BaseException("evt.from is null");
		if (evt.getTo() == null)
			throw new BaseException("evt.to is null");
		// String from = evt.getFrom().getUri().trim();
		// if (!ttListenerMap.containsKey(from)) {
		// throw new BaseException("from:" + from + " can't register");
		// }
		String to = evt.getTo().getUri().trim();
		eventQueue.add(evt);
		notifyAllMain();
		if (ttListenerMap.containsKey(to) || Location.any.getUri().equals(to)) {
		} else {
			// throw new BaseException("to:" + to + " can't register");
			LogUtil.e(tag, "to:" + to + " can't register");
		}
	}

	void registerTtListener(MessageHandListener tl) {
		ttListenerMap.put(tl.getClass().getName(), tl);
	}

	void unRegisterTtListener(MessageHandListener tl) {
		ttListenerMap.remove(tl.getClass().getName());
	}

	private final Object lockMain = new Object();

	private void waitMain() {
		synchronized (lockMain) {
			try {
				lockMain.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void notifyAllMain() {
		synchronized (lockMain) {
			try {
				lockMain.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class MainThread extends Thread {
		@Override
		public void run() {
			while (isRunning()) {
				if (eventQueue.isEmpty())
					waitMain();
				BaseEvent evt = eventQueue.peek();
				if (evt == null)
					continue;
				// BaseEvent evtClone = (BaseEvent) evt.clone();
				String to = evt.getTo().getUri();
				if (to == null || to.trim().length() == 0)
					continue;
				if (Location.any.getUri().equals(to)) {
					eventQueue.poll();
					for (MessageHandListener lt : ttListenerMap.values()) {
						lt.executeEvent(evt);
					}
					continue;
				}
				MessageHandListener lt = ttListenerMap.get(to);
				if (lt != null) {
					eventQueue.poll();
					lt.executeEvent(evt);
					continue;
				}
				if (eventQueue.size() > 1) {
					try {
						eventQueue.remove();
						eventQueue.add(evt);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}

		private final Object lockRunning = new Object();
		private boolean running = true;

		private boolean isRunning() {
			synchronized (lockRunning) {
				return this.running;
			}
		}

		private void stopRunning() {
			synchronized (lockRunning) {
				this.running = false;
			}
			notifyAllMain();
		}

		public MainThread(String threadName) {
			super(threadName);
		}
	}

	void checkRunOnUI() {
		if (Thread.currentThread().getId() != uiTid)
			throw new IllegalStateException("not run on UI Thread");
	}

	void checkRunOnMain() {
		if (Thread.currentThread() != mainThread)
			throw new IllegalStateException("not run on Main Thread");
	}

	void postRunOnUi(UITask task) {
		handler.post(task);
	}
	
	//-------------------------------------------------------
	NetWorkReceiver mNetWorkReceiver = null;
	private void registReceivers(){
		initNetWorkReceiver();
		IntentFilter mNetWrokFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(mNetWorkReceiver,mNetWrokFilter);
	}
	protected void initNetWorkReceiver(){
		mNetWorkReceiver = new NetWorkReceiver();
	}

}
