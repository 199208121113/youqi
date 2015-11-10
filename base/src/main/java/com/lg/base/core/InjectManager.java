package com.lg.base.core;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;

/**
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
                    Object result = null;
                    if(obj instanceof Activity){
                        Activity act = (Activity)obj;
                        View view = act.findViewById(iv.value());
                        view.setTag(iv.tag());
                        result = view;
                    }else if(obj instanceof Fragment){
                        Fragment fg = (Fragment)obj;
                        View view = fg.getView().findViewById(iv.value());
                        view.setTag(iv.tag());
                        result = view;
                    }
                    field.setAccessible(true);
                    field.set(obj,result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
