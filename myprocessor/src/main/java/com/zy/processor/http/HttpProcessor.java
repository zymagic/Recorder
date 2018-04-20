package com.zy.processor.http;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Created by zy on 2017/7/31.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.zy.processor.http.GET","com.zy.processor.http.POST","com.zy.processor.http.FieldName"})
public class HttpProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    System.out.print("process http " + set + ", " + roundEnvironment);
    return false;
  }
}
