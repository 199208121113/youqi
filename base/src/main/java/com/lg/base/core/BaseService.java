package com.lg.base.core;

import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import com.lg.base.event.NetWorkEvent;
import com.lg.base.event.ServiceStateEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

import roboguice.service.RoboService;

public abstract class BaseService extends RoboService implements MessageHandListener,MessageSendListener {

	protected final String tag = this.getClass().getSimpleName()+"::";
	private BaseApplication app = null;
	private final Location from = new Location(this.getClass().getName());
	private final Location  to = new Location(this.getClass().getName());
	private final ConcurrentLinkedQueue<DoWhat> doWhatQueue = new ConcurrentLinkedQueue<>();
	private Thread3 t3 = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = (BaseApplication) getApplication();
		app.registerTtListener(this);
		t3 = new Thread3("T3-"+this.getClass().getSimpleName());
		t3.start();
		app.sendEvent(new ServiceStateEvent(from, to, ServiceStateEvent.State.create));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		app.sendEvent(new ServiceStateEvent(from, to, ServiceStateEvent.State.destroy));
	}

	@Override
	public final void executeEvent(BaseEvent evt) {
		if (evt instanceof ServiceStateEvent) {
			ServiceStateEvent tes = (ServiceStateEvent) evt;
			if (ServiceStateEvent.State.create == tes.getState()) {
				doCreate();
				//如果将MainThread换回来，则需要将此注释打开
//				app.notifyAllMain();
				return;
			} else if (ServiceStateEvent.State.destroy == tes.getState()) {
				doDestroy();
				app.unRegisterTtListener(this);
				t3.stopRunning();
				return;
			}
		}
		if(evt instanceof NetWorkEvent){
			NetWorkEvent event = (NetWorkEvent)evt;
			onNetworkStateChanged(event);
			return ;
	    }
		doExecuteEvent(evt);
	}

	@Override
	public final void executeMessage(Message msg) {
		doExecuteMessage(msg);
	}

	public final void sendEvent(BaseEvent evt) {
		if(evt.getFrom() == null){
			evt.setFrom(getLocation());
		}
		app.sendEvent(evt);
	}

	public final void sendMessage(Message msg) {
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessage(tmsg);
	}

	public final void sendEmptyMessage(int what) {
		Message msg = Message.obtain();
		msg.what = what;
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessage(tmsg);
	}

	public final void sendMessageDelayed(Message msg, long delayMillis) {
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessageDelayed(tmsg, delayMillis);
	}

	public final void sendEmptyMessageDelayed(int what, long delayMillis) {
		Message msg = Message.obtain();
		msg.what = what;
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessageDelayed(tmsg, delayMillis);
	}
	
	protected final void submitDoWhat(DoWhat doWhat){
		if(doWhat == null)
			return ;
		doWhatQueue.add(doWhat);
		notifyAllT3();
	}
	
	protected final void checkRunOnUI(){
		app.checkRunOnUI();
	}
	
	protected final void checkRunOnMain(){
		app.checkRunOnMain();
	}
	
	protected final void checkRunOnT3(){
		if(Thread.currentThread() != t3)
			throw new IllegalStateException("not run on T3 Thread");
	}
	
	protected final Location getLocation(){
		return new Location(this.getClass().getName());
	}
	
	protected final Location findLocation(Class<?> cls){
		return new Location(cls.getName());
	}

	protected abstract void doCreate();

	protected abstract void doDestroy();

	protected void doExecuteMessage(Message msg){}

	protected void doExecuteEvent(BaseEvent evt){}
	
	protected void doExecuteDoWhat(DoWhat doWhat){}
	
	private void doWhat(DoWhat doWhat){
		checkRunOnT3();
		doExecuteDoWhat(doWhat);
	}
	
	private final Object lockT3 = new Object();
	private void waitT3() {
		synchronized (lockT3) {
			try {
				lockT3.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private void notifyAllT3() {
		synchronized (lockT3) {
			try {
				lockT3.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class Thread3 extends Thread{		
		public Thread3(String threadName) {
			super(threadName);
		}
		@Override
		public void run() {
			while (isRunning()) {
				if (doWhatQueue.isEmpty()) {
					waitT3();
				}
				if(isRunning())
					break;
				DoWhat dw = null;
				try {
					dw = doWhatQueue.remove();
					doWhat(dw); 
				} catch (Exception e) {
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
			notifyAllT3();
		}
	}
	
	/** this method is running on Main-Thread,not on UI-Thread */
	protected void onNetworkStateChanged(NetWorkEvent evt){
		if(evt.isAvailable()){
			LogUtil.d(tag, "当前网络可用,类型:"+evt.getNetWorkType().name());
		}else{
			LogUtil.d(tag, "当前网络不可用");
		}
	}

}
