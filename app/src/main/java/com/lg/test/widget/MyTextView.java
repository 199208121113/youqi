package com.lg.test.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.lg.test.MainActivity;

/**
 * Created by liguo on 2015/9/18.
 */
public class MyTextView extends TextView {

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final String TAG = this.getClass().getSimpleName();
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MainActivity.loge(TAG, "dispatchTouchEvent()", ev);
        boolean result = super.dispatchTouchEvent(ev);
//        MainActivity.loge(TAG,"dispatchTouchEvent()-return="+result,ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MainActivity.loge(TAG, "onTouchEvent()", event);
        boolean result = super.onTouchEvent(event);
//        MainActivity.loge(TAG,"onTouchEvent()-return="+result,event);
        return result;
    }

}
