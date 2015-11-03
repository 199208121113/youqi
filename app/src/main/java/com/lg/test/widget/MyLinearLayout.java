package com.lg.test.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.lg.test.MainActivity;

/**
 * Created by liguo on 2015/9/18.
 */
public class MyLinearLayout extends LinearLayout {
    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final String TAG = this.getClass().getSimpleName();

    /**
     * （1）如果直接返回true，则：
     *     a.将不再向本ViewGroup的所有ChildView分发事件
     *     b.本类的onTouchEvent也不会执行
     *     c.[Action_UP]事件-能收到
     * （2）如果直接返回false，则：
     *     a.将不再向本ViewGroup的所有ChildView分发事件
     *     b.本类的onTouchEvent也不会执行
     *     c.[Action_UP]事件-不能收到
     * （3）如果调用super方法(ChildView只要没做处理，默认都是返回false)则：
     *     (a)返回true说明
     *          1.ChildView的dispatchTouchEvent直接返回true,ChildView的OnTouchEvent不会执行
     *          2.ChildView的super.dispatchTouchEvent(evt)返回了true,其实也就是ChildView的OnTouchEvent返回了true
     *          结果：就是本类OnTouchEvent就不会执行
     *     (b)返回false说明
     *          1.ChildView的dispatchTouchEvent直接返回false，ChildView的OnTouchEvent不会执行
     *          2.ChildView的super.dispatchTouchEvent(evt)返回了false,其实也就是ChildView的OnTouchEvent返回了false
     *          结果：就是本类的OnTouchEvent就会执行
     *  总结：
     *      1.如果在OnKeyDown的时候，本方法返回了false，那么后面OnKeyUp，OnKeyMove事件将不会收到
     *      2.super.dispatchTouchEvent(evt)的返回值来源于
     *          (a)ChildView的dispatchTouchEvent()
     *          (b)本类的OnTouchEvent
     *
     * @param evt 事件
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent evt) {
        MainActivity.loge(TAG, "dispatchTouchEvent()", evt);
        boolean result = super.dispatchTouchEvent(evt);
//        MainActivity.loge(TAG,"dispatchTouchEvent()-return="+result,evt);
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
