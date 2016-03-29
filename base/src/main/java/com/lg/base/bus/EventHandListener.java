package com.lg.base.bus;

import android.os.Message;

import com.lg.base.bus.BaseEvent;

public interface EventHandListener {
	void executeEvent(BaseEvent evt);
	void executeMessage(Message msg);
}
