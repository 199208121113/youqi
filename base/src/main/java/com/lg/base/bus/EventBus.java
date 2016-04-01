package com.lg.base.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.lg.base.core.LogUtil;
import com.lg.base.core.UITask;

import java.util.HashMap;
import java.util.Map;
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
    private volatile ConcurrentHashMap<String, Map<String,Future>> futureMap = null;
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

    private void removeFutureFromMap(final String to,final String workKey){
        Map<String, Future> map = futureMap.get(to);
        if (map == null || map.size() == 0) {
            return;
        }
        if (map.containsKey(workKey)) {
            map.remove(workKey);
        }
    }

    private boolean isValid(BaseEvent evt){
        if (evt == null) {
            LogUtil.e(TAG, "evt is null");
            return false;
        }
        if (evt.getTo() == null) {
            LogUtil.e(TAG,"evt.to is null");
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
//        Scheduler scheduler = evt.getScheduler();
//        if(scheduler == null){
//            scheduler = Schedulers.immediate();
//        }
//        scheduler.createWorker().schedule(new RxAction(evt));
        sendEventDelayed(evt, 0, TimeUnit.SECONDS);
    }

    /**
     * @param evt 事件
     * @param delayed 延迟多少秒后执行
     * @param unit 时间单位
     */
    public void sendEventDelayed(BaseEvent evt,long delayed,TimeUnit unit) {
        sendEventAtFixedRate(evt,delayed,0,unit);
    }

    /**
     * @param evt 事件
     * @param delayed 延迟delayed秒后开始执行
     * @param period 从第1次执行后，就每隔period后执行一次
     * @param unit 时间单位
     */
    public void sendEventAtFixedRate(BaseEvent evt,long delayed,long period,TimeUnit unit) {
        if (!isValid(evt)) {
            return;
        }
        EventThread et = evt.getRunOnThread();
        EventWorker worker = new EventWorker(evt);
        Future fu = null;
        if(et == null){
            worker.run();
        }else if(et == EventThread.IO){
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
            getHandler().post(worker);
        }
        if(fu != null){
            String kk = worker.toString();
            String to = evt.getTo().getUri();
            if(futureMap.containsKey(to)){
                futureMap.get(to).put(kk,fu);
            }else{
                HashMap<String,Future> map = new HashMap<>();
                map.put(kk,fu);
                futureMap.put(to,map);
            }
        }
    }

    /**
     * @param evt 事件
     * @param initialDelay 首次延迟时间
     * @param delay 每次执行之间的间隔
     * @param unit 时间单位
     */
    @SuppressWarnings("unused")
    public void sendEventWithFixedDelay(BaseEvent evt,long initialDelay,long delay,TimeUnit unit) {
        if (!isValid(evt)) {
            return;
        }
        EventThread et = evt.getRunOnThread();
        EventWorker worker = new EventWorker(evt);
        Future fu = null;
        if(et == null){
            throw new RuntimeException("un support current thread");
        }else if(et == EventThread.IO){
            fu = executors.scheduleWithFixedDelay(worker, initialDelay, delay, unit);
        }else if(et == EventThread.NEW){
            fu = Executors.newSingleThreadScheduledExecutor(threadFactory).scheduleWithFixedDelay(worker, initialDelay, delay, unit);
        }else if(et == EventThread.UI){
            throw new RuntimeException("un support ui thread");
        }
        if(fu != null){
            String kk = worker.toString();
            String to = evt.getTo().getUri();
            if(futureMap.containsKey(to)){
                futureMap.get(to).put(kk,fu);
            }else{
                HashMap<String,Future> map = new HashMap<>();
                map.put(kk,fu);
                futureMap.put(to,map);
            }
        }
    }

    public void sendMessage(EventLocation to,Message msg) {
        sendMsg(toMessage(to, msg, msg.what), 0);
    }

    @SuppressWarnings("unused")
    public void sendEmptyMessage(EventLocation to,int what) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(to, msg);
    }

    public void sendMessageDelayed(EventLocation to,Message msg, long delayMillis) {
        sendMsg(toMessage(to, msg, msg.what), delayMillis);
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
        String key = tl.getClass().getName();
        eventHandListenerMap.remove(key);
        Map<String,Future> fMap = futureMap.get(key);
        if(fMap == null || fMap.size() == 0){
            LogUtil.e(TAG,"unRegister(),handler="+key+",fuMap.size()="+0);
            return;
        }
        for (Future fu : fMap.values()){
            if(!(fu.isDone() || fu.isCancelled())){
                boolean canceled = fu.cancel(true);
                LogUtil.e(TAG,"unRegister(),handler="+key+",fu.cancel="+canceled);
            }
        }
        fMap.clear();
        futureMap.remove(key);
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
                EventBus.get().removeFutureFromMap(to,this.toString());
            }
        }
    }

   /* private static class RxAction implements Action0 {
        private BaseEvent evt;

        public RxAction(BaseEvent evt) {
            super();
            this.evt = evt;
        }

        @Override
        public void call() {
            new EventWorker(evt).run();
        }
    }*/

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