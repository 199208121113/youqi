package com.lg.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventHandListener;
import com.lg.base.bus.EventLocation;
import com.lg.test.MainActivity;
import com.lg.test.R;

/**
 * Created by liguo on 2015/10/14.
 */
public class WelcomeActivity extends Activity implements EventHandListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        EventBus.get().register(this);
        EventLocation loc = EventBus.findLocation(WelcomeActivity.class);
        EventBus.get().sendEmptyMessageDelayed(loc,1,3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.get().unRegister(this);
    }

    @Override
    public void executeEvent(BaseEvent evt) {

    }

    @Override
    public void executeMessage(Message msg) {
        if(msg.what == 1){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
