package com.lg.base.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liguo on 2015/10/16.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
    String columnName() default "";

    DataType dataType() default DataType.UNKNOWN;

    boolean id() default false;
}

