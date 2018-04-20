package com.zy.processor.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zy on 2017/7/31.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface FieldName {
  String value();
}
