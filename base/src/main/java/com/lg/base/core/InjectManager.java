package com.lg.base.core;

import android.app.Activity;
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
                    Activity act = (Activity)obj;
                    if(iv.value() == 0){
                        continue;
                    }
                    View view = act.findViewById(iv.value());
                    view.setTag(iv.tag());
                    field.setAccessible(true);
                    field.set(obj,view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
