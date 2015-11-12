package com.lg.base.core;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.lg.base.utils.StringUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 注入管理者
 * Created by liguo on 2015/11/10.
 */
public class InjectManager {
    public static void init(Object obj){
        try {
            Class<?> cls = obj.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields){
                InjectView iv = field.getAnnotation(InjectView.class);
                if(iv != null){
                    View view = null;
                    if(obj instanceof Activity){
                        Activity act = (Activity)obj;
                        view = act.findViewById(iv.value());
                    }else if(obj instanceof Fragment){
                        Fragment fg = (Fragment)obj;
                        if(fg.getView() != null) {
                            view = fg.getView().findViewById(iv.value());
                        }
                    }
                    if(view != null){
                        view.setTag(iv.tag());
                        field.setAccessible(true);
                        field.set(obj,view);
                        String clickMethod = iv.click();
                        if(StringUtil.isNotEmpty(clickMethod)){
                            view.setOnClickListener(new InjectClickListener(obj,clickMethod));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class InjectClickListener implements View.OnClickListener{
        private WeakReference<Object> objRef;
        private final String methodName;
        public InjectClickListener(Object obj,String clickMethodName) {
            this.objRef = new WeakReference<>(obj);
            this.methodName = StringUtil.replaceTrim_R_N(clickMethodName);
        }

        @Override
        public void onClick(View v) {
            Object obj = objRef.get();
            if(obj == null)
                return;
            Method m = getMethod(obj.getClass(), methodName, View.class);
            if(m == null)
                return;
            m.setAccessible(true);
            try {
                m.invoke(obj,v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Method getMethod(Class<?> cls,String method,Class<?>... params){
        Method m = null;
        try {
            m = cls.getDeclaredMethod(method,params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return m;
    }
}
