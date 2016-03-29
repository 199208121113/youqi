package com.lg.base.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.lg.base.core.LogUtil;
import com.lg.base.core.UITask;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by root on 16-3-23
 */
public class EventBus {
    private static final String TAG = "EventBus";
    private static EventBus instance = null;
    private volatile TempHandler tempHandler = null;
    private volatile ConcurrentHashMap<String, WeakReference<EventHandListener>> ttListenerMap = null;
    // ====================================================
    public static final String T2_THREAD_NAME = "T2-MainThread#";
    /** 持久化线程工厂 */
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicLong mCount = new AtomicLong(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, T2_THREAD_NAME + mCount.getAndIncrement());
        }
    };

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;

    private ThreadPoolExecutor executors = null;

    private EventBus(){
        ttListenerMap = new ConcurrentHashMap<>();
        tempHandler = new TempHandler(Looper.getMainLooper());
        executors = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), threadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    @SuppressWarnings("unused")
    public EventLocation findLocation(Class<?> cls) {
        return new EventLocation(cls.getName());
    }

    public static EventBus get(){
        if(instance == null){
            synchronized (EventBus.class) {
                if(instance == null){
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    private EventHandListener getMessageHandlerListener(String from){
        WeakReference<EventHandListener> rf = ttListenerMap.get(from);
        return rf != null ? rf.get() : null;
    }

    private ConcurrentHashMap<String, WeakReference<EventHandListener>> getListenerMap(){
        return ttListenerMap;
    }

    public void sendEvent(BaseEvent evt) {
        if (evt == null) {
            LogUtil.e(TAG, "evt is null");
            return;
        }
        if (evt.getTo() == null) {
            LogUtil.e(TAG,"evt.to is null");
            return;
        }
        String to = evt.getTo().getUri().trim();
        if (!(ttListenerMap.containsKey(to) || EventLocation.any.getUri().equals(to))) {
            LogUtil.e(TAG, "to:" + to + " can't register");
            return;
        }
        EventThread et = evt.getRunOnThread();
        EventWorker worker = new EventWorker(evt);
        if(et == null){
            worker.run();
        }else if(et == EventThread.IO){
            executors.execute(worker);
        }else if(et == EventThread.NEW){
            new Thread(worker).start();
        }else if(et == EventThread.UI){
            getHandler().post(worker);
        }
    }

    public void sendMessage(EventLocation to,Message msg) {
        sendMsg(toMessage(to,msg,msg.what),0);
    }

    @SuppressWarnings("unused")
    public void sendEmptyMessage(EventLocation to,int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(to,msg);
    }

    public void sendMessageDelayed(EventLocation to,Message msg, long delayMillis) {
        sendMsg(toMessage(to,msg,msg.what), delayMillis);
    }

    @SuppressWarnings("unused")
    public void sendEmptyMessageDelayed(EventLocation to,int what,long delayMillis){
        Message msg = Message.obtain();
        msg.what = what;
        sendMessageDelayed(to,msg,delayMillis);
    }

    @SuppressWarnings("unused")
    public void removeMessage(int what){
        getHandler().removeMessages(what);
    }

    public void postRunOnUi(UITask task) {
        getHandler().post(task);
    }

    public void register(EventHandListener tl) {
        ttListenerMap.put(tl.getClass().getName(), new WeakReference<>(tl));
    }

    public void unRegister(EventHandListener tl) {
        ttListenerMap.remove(tl.getClass().getName());
    }

    //===================
    private void sendMsg(Message msg,long delayMillis){
        if(delayMillis <= 0) {
            getHandler().sendMessage(msg);
        }else{
            getHandler().sendMessageDelayed(msg, delayMillis);
        }
    }

    private Message toMessage(EventLocation to,Message msg,int what){
        BaseEvent evt = new BaseEvent(to).setData(msg);
        Message newMsg = new Message();
        newMsg.what = what;
        newMsg.obj = evt;
        return newMsg;
    }

    private Handler getHandler(){
        return this.tempHandler;
    }

    private static class EventWorker implements Runnable {
        private BaseEvent evt;

        public EventWorker(BaseEvent evt) {
            super();
            this.evt = evt;
        }

        @Override
        public void run() {
            String to = evt.getTo().getUri();
            if (to == null || to.trim().length() == 0)
                return;
            ConcurrentHashMap<String, WeakReference<EventHandListener>> result = EventBus.get().getListenerMap();
            if (EventLocation.any.getUri().equals(to)) {
                for (WeakReference<EventHandListener> rf : result.values()) {
                    EventHandListener el = rf.get();
                    if(el != null) {
                        el.executeEvent(evt);
                    }
                }
                return;
            }
            WeakReference<EventHandListener> rf = result.get(to);
            if (rf == null)
                return;
            EventHandListener el = rf.get();
            if(el != null) {
                el.executeEvent(evt);
            }
        }
    }

    private static class TempHandler extends Handler{
        public TempHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            if (msg == null || msg.obj == null)
                return ;
            if(!(msg.obj instanceof BaseEvent))
                return;
            BaseEvent evt = (BaseEvent)msg.obj;
            String to = evt.getTo().getUri();
            EventHandListener ttMsgListener = EventBus.get().getMessageHandlerListener(to);
            if (ttMsgListener != null) {
                ttMsgListener.executeMessage((Message)evt.getData());
            }
        }
    }
}