package com.lg.base.bus;

import android.os.Message;

public interface EventHandListener {
	void executeEvent(BaseEvent evt);
	void executeMessage(Message msg);
}
