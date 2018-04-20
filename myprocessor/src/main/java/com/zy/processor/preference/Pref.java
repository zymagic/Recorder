package com.zy.processor.preference;

/**
 * Created by zy on 2017/11/6.
 */

public @interface Pref {
  String name() default "";
  String key();
  Type type();
  enum Type {
    INTEGER,
    FLOAT,
    LONG,
    BOOLEAN,
    STRING
  }
}
