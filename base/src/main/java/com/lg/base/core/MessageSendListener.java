package com.lg.base.core;

import android.os.Message;

public interface MessageSendListener {
	
	void sendEvent(BaseEvent evt);

	void sendMessage(Message msg);

	void sendEmptyMessage(int what);

	void sendMessageDelayed(Message msg, long delayMillis);

	void sendEmptyMessageDelayed(int what, long delayMillis);
}
