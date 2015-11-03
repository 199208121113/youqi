package com.lg.base.core;

import android.os.Message;

/**
 * Created by liguo on 2015/6/3.
 */
public class DefaultMessageSenderListenerImpl implements MessageSendListener {
    private final Location from = new Location(DefaultMessageSenderListenerImpl.class.getName());
    private BaseApplication app;

    public DefaultMessageSenderListenerImpl(BaseApplication app) {
        this.app = app;
    }

    public final void sendEvent(BaseEvent evt) {
        if (evt.getFrom() == null) {
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

    protected final Location getLocation() {
        return new Location(this.getClass().getName());
    }
}
