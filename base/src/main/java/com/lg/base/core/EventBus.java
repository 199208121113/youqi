package com.lg.base.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by root on 16-3-23.
 */
public class EventBus {
    private static final String TAG = "EventBus";
    private static EventBus instance = null;
    private volatile HandlerWorker worker = null;
    private volatile ConcurrentHashMap<String, MessageHandListener> ttListenerMap = null;
    // ====================================================
    public static final String T2_THREAD_NAME = "T2-MainThread#";
    /** 持久化线程工厂 */
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicLong mCount = new AtomicLong(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, T2_THREAD_NAME + mCount.getAndIncrement());
        }
    };
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;
    private final ThreadPoolExecutor executors = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), threadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());

    private EventBus(){
        ttListenerMap = new ConcurrentHashMap<>();
        worker = new HandlerWorker();
        new Thread(worker).start();
    }

    public static Location findLocation(Class<?> cls) {
        return new Location(cls.getName());
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

    private MessageHandListener getMessageHandlerListener(String from){
        return ttListenerMap.get(from);
    }

    private ConcurrentHashMap<String, MessageHandListener> getListenerMap(){
        return ttListenerMap;
    }

    public void sendEvent(BaseEvent evt) {
        if (evt == null) {
            LogUtil.e(TAG,"evt is null");
            return;
        }
//        if (evt.getFrom() == null) {
//            LogUtil.e(TAG,"evt.from is null");
//            return;
//        }
        if (evt.getTo() == null) {
            LogUtil.e(TAG,"evt.to is null");
            return;
        }
        String to = evt.getTo().getUri().trim();
        executors.execute(new EventWorker(evt));
        if (!(ttListenerMap.containsKey(to) || Location.any.getUri().equals(to))) {
            LogUtil.e(TAG, "to:" + to + " can't register");
        }
    }

    public void sendMessage(Location to,Message msg) {
        msg.obj = to;
        getHandler().sendMessage(msg);
    }

    public void sendMessageDelayed(Location to,Message msg, long delayMillis) {
        msg.obj = to;
        getHandler().sendMessageDelayed(msg, delayMillis);
    }

    public void sendEmptyMessageDelayed(Location to,int what,long delayMillis){
        Message msg = getHandler().obtainMessage();
        msg.obj = to;
        msg.what = what;
        getHandler().sendMessageDelayed(msg, delayMillis);
    }

    public void removeMessage(int what){
        getHandler().removeMessages(what);
    }

    public void postRunOnUi(UITask task) {
        getHandler().post(task);
    }

    private Handler getHandler(){
        return worker.getTmpHandler();
    }

    public void register(MessageHandListener tl) {
        ttListenerMap.put(tl.getClass().getName(), tl);
    }

    public void unRegister(MessageHandListener tl) {
        ttListenerMap.remove(tl.getClass().getName());
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
            ConcurrentHashMap<String, MessageHandListener> result = EventBus.get().getListenerMap();
            if (Location.any.getUri().equals(to)) {
                for (MessageHandListener lt : result.values()) {
                    lt.executeEvent(evt);
                }
                return;
            }
            MessageHandListener lt = result.get(to);
            if (lt == null)
                return;
            lt.executeEvent(evt);
        }
    }

    public static class HandlerWorker implements Runnable {

        private static Handler tmpHandler = null;

        @Override
        public void run() {
            Looper.prepare();
            tmpHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg == null || msg.obj == null)
                        return ;
                    Location loc = null;
                    if(!(msg.obj instanceof Location))
                        return;
                    loc = (Location)msg.obj;
                    String from = loc.getUri();
                    MessageHandListener ttMsgListener = EventBus.get().getMessageHandlerListener(from);
                    if (ttMsgListener != null) {
                        ttMsgListener.executeMessage(msg);
                    }
                }
            };
            Looper.loop();
        }

        public Handler getTmpHandler() {
            return tmpHandler;
        }
    }
}
