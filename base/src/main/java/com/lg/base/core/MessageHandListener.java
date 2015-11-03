package com.lg.base.core;

import android.os.Message;

public interface MessageHandListener {
	void executeEvent(BaseEvent evt);
	void executeMessage(Message msg);
}
