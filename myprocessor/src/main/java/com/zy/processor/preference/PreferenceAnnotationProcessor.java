package com.zy.processor.preference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Created by zy on 2017/11/6.
 */

public class PreferenceAnnotationProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(new String[] {
        Pref.class.getName(),
        IntPref.class.getName(),
        LongPref.class.getName(),
        FloatPref.class.getName(),
        BooleanPref.class.getName(),
        StringPref.class.getName()
    }));
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    return false;
  }
}
