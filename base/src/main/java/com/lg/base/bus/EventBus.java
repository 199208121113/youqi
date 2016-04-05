package com.lg.base.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.lg.base.core.LogUtil;
import com.lg.base.core.UITask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by root on 16-3-23
 */
public class EventBus {
    private static final String TAG = "EventBus";
    private volatile TempHandler tempHandler = null;
    private volatile ConcurrentHashMap<String, EventHandListener> eventHandListenerMap = null;
    private volatile ConcurrentHashMap<String, List<Runnable>> futureMap = null;
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

    private ScheduledExecutorService executors = null;

    private EventBus(){
        eventHandListenerMap = new ConcurrentHashMap<>();
        tempHandler = new TempHandler(Looper.getMainLooper());
        executors = Executors.newScheduledThreadPool(4,threadFactory);
        futureMap = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unused")
    public EventLocation findLocation(Class<?> cls) {
        return new EventLocation(cls.getName());
    }

    private static class EventBusHelper{
        private static final EventBus INSTANCE = new EventBus();
    }
    public static EventBus get(){
        return EventBusHelper.INSTANCE;
    }

    private EventHandListener getEventHandlerListener(String to){
        return eventHandListenerMap.get(to);
    }

    private ConcurrentHashMap<String, EventHandListener> getEventHandListenerMap(){
        return eventHandListenerMap;
    }

    private boolean isValid(BaseEvent evt){
        if (evt == null) {
            LogUtil.e(TAG, "evt is null");
            return false;
        }
        if (evt.getTo() == null) {
            LogUtil.e(TAG, "evt.to is null");
            return false;
        }
        String to = evt.getTo().getUri().trim();
        if (!(eventHandListenerMap.containsKey(to) || EventLocation.any.getUri().equals(to))) {
            LogUtil.e(TAG, "to:" + to + " can't register");
            return false;
        }
        return true;
    }

    public void sendEvent(BaseEvent evt) {
        sendEvent(evt, 0, TimeUnit.SECONDS);
    }

    /**
     * @param evt 事件
     * @param delayed 延迟多少秒后执行
     * @param unit 时间单位
     */
    public void sendEvent(BaseEvent evt,long delayed,TimeUnit unit) {
        sendEvent(evt, delayed, 0, unit);
    }

    /**
     * @param evt 事件
     * @param delayed 延迟delayed秒后开始执行
     * @param period 从第1次执行后，就每隔period后执行一次
     * @param unit 时间单位
     */
    public Future sendEvent(BaseEvent evt,long delayed,long period,TimeUnit unit) {
        if (!isValid(evt)) {
            return null;
        }
        EventThread et = evt.getRunOnThread();
        EventWorker worker = new EventWorker(evt);
        Future fu = null;
        if(et == null || et == EventThread.IO){
            if(period > 0) {
               fu = executors.scheduleAtFixedRate(worker, delayed, period, unit);
            }else if(delayed > 0){
               fu = executors.schedule(worker, delayed, unit);
            }else{
               fu = executors.submit(worker);
            }
        }else if(et == EventThread.NEW){
            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(threadFactory);
            if(period > 0) {
                fu = ses.scheduleAtFixedRate(worker, delayed, period, unit);
            }else if(delayed > 0){
                fu = ses.schedule(worker, delayed, unit);
            }else{
                fu = ses.submit(worker);
            }
        }else if(et == EventThread.UI){
            if(period > 0) {
                EventWorkerByUI ewb = new EventWorkerByUI(evt,unit.toMillis(period),1);
                addRunnableToFutureMap(evt, ewb);
                getHandler().postDelayed(ewb,unit.toMillis(delayed));
            }else if(delayed > 0){
                getHandler().postDelayed(worker,unit.toMillis(delayed));
            }else{
                getHandler().post(worker);
            }
        }
        return fu;
    }

    /**
     * @param evt 事件
     * @param initialDelay 首次延迟时间
     * @param delay 每次执行之间的间隔
     * @param unit 时间单位
     */
    @SuppressWarnings("unused")
    public Future sendEventWithFixedDelay(BaseEvent evt,long initialDelay,long delay,TimeUnit unit) {
        if (!isValid(evt)) {
            return null;
        }
        EventThread et = evt.getRunOnThread();
        EventWorker worker = new EventWorker(evt);
        Future fu = null;
        if(et == null || et == EventThread.IO){
            fu = executors.scheduleWithFixedDelay(worker, initialDelay, delay, unit);
        }else if(et == EventThread.NEW){
            fu = Executors.newSingleThreadScheduledExecutor(threadFactory).scheduleWithFixedDelay(worker, initialDelay, delay, unit);
        }else if(et == EventThread.UI){
            EventWorkerByUI ewb = new EventWorkerByUI(evt,unit.toMillis(delay),2);
            addRunnableToFutureMap(evt, ewb);
            getHandler().postDelayed(ewb,unit.toMillis(initialDelay));
        }
        return fu;
    }

    public void sendMessage(EventLocation to,Message msg) {
        sendMsg(toMessage(to, msg, msg.what), 0);
    }

    public void sendMessageDelayed(EventLocation to,Message msg, long delayMillis) {
        sendMsg(toMessage(to, msg, msg.what), delayMillis);
    }


    @SuppressWarnings("unused")
    public void sendEmptyMessage(EventLocation to,int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(to, msg);
    }

    @SuppressWarnings("unused")
    public void sendEmptyMessageDelayed(EventLocation to,int what,long delayMillis){
        Message msg = Message.obtain();
        msg.what = what;
        sendMessageDelayed(to, msg, delayMillis);
    }

    @SuppressWarnings("unused")
    public void removeMessage(int what){
        getHandler().removeMessages(what);
    }

    public void postRunOnUiThread(UITask task) {
        getHandler().post(task);
    }

    public void register(EventHandListener tl) {
        eventHandListenerMap.put(tl.getClass().getName(), tl);
    }

    public void unRegister(EventHandListener tl) {
        final String key = tl.getClass().getName();
        if(eventHandListenerMap.containsKey(key)) {
            eventHandListenerMap.remove(key);
        }
        List<Runnable> rl = futureMap.get(key);
        if(rl != null && rl.size() > 0){
            for (Runnable rr : rl){
                EventBus.get().getHandler().removeCallbacks(rr);
            }
            futureMap.remove(key);
        }
    }

    //===================

    private void addRunnableToFutureMap(BaseEvent evt,Runnable ewb){
        String to = evt.getTo().getUri();
        if(futureMap.containsKey(to)){
            futureMap.get(to).add(ewb);
        }else{
            List<Runnable> rl = new ArrayList<>();
            rl.add(ewb);
            futureMap.put(to,rl);
        }
    }
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
            try {
                ConcurrentHashMap<String, EventHandListener> result = EventBus.get().getEventHandListenerMap();
                if (EventLocation.any.getUri().equals(to)) {
                    for (EventHandListener el : result.values()) {
                        if(el != null) {
                            el.executeEvent(evt);
                        }
                    }
                    return;
                }
                EventHandListener el = result.get(to);
                if(el != null) {
                    el.executeEvent(evt);
                }
            } finally {
                // nothing to do
            }
        }
    }

    private static class EventWorkerByUI extends EventWorker {
        long delay;
        /** 1:每隔多少秒执行一次  2:每次执行的间隔是多少 */
        int mode = 1;
        public EventWorkerByUI(BaseEvent evt,long delay, int mode) {
            super(evt);

            this.delay = delay;
            this.mode = mode;
        }

        @Override
        public void run() {
            if(mode == 1){
                EventBus.get().getHandler().postDelayed(this, delay);
            }
            super.run();
            if(mode == 2){
                EventBus.get().getHandler().postDelayed(this, delay);
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
            BaseEvent evt = (BaseEvent)msg.obj;
            String to = evt.getTo().getUri();
            EventHandListener ttMsgListener = EventBus.get().getEventHandlerListener(to);
            if (ttMsgListener != null) {
                ttMsgListener.executeMessage((Message)evt.getData());
            }
        }
    }
}