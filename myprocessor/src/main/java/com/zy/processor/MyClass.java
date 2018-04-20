package com.zy.processor;

import com.zy.processor.preference.IntPref;
import com.zy.processor.preference.Pref;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.zy.processor.ViewBind"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MyClass extends AbstractProcessor {
  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    System.out.println(">>>>>>>>>>>>>>>process annotation<<<<<<<<<<<<<<<<<");
    Map<TypeElement, List<Element>> map = map(roundEnvironment);
    for (Map.Entry<TypeElement, List<Element>> entry : map.entrySet()) {
      TypeElement acls = entry.getKey();
      String genClass = acls.getSimpleName() + "_Bind";
      String code = CodeGenerator.cls()
          .setPackageName(getPackageName(acls.getQualifiedName().toString()))
          .setMethods(
              CodeGenerator.method()
              .setParameters(CodeGenerator.parameter(acls.getQualifiedName().toString(), "q"), CodeGenerator.parameter("android.view.View", "p"))
              .setStatements(selectView(entry.getValue(), "q", "p"))
              .setName("bind")
              .setAccessPolicy("public")
              .setStatic(true),
              CodeGenerator.method()
              .setParameters(CodeGenerator.parameter(acls.getQualifiedName().toString(), "q"), CodeGenerator.parameter("android.app.Activity", "p"))
              .setStatements("bind(q, p.getWindow().getDecorView());")
              .setName("bind")
              .setAccessPolicy("public")
              .setStatic(true)
          )
          .setAccessPolicy("public")
          .setName(genClass)
          .asString();
      try {
        JavaFileObject jFile = processingEnv.getFiler().createSourceFile(acls.getQualifiedName() + "_Bind");
        Writer writer = jFile.openWriter();
        writer.write(code);
        writer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  private String getPackageName(String cls) {
    return cls.substring(0, cls.lastIndexOf('.'));
  }

  private String selectView(Name elementName, TypeMirror elementType, int value, String p, String q) {
    return p + "." + elementName + "=(" + elementType + ") " + q + ".findViewById(" + value + ");";
  }

  String[] selectView(List<Element> elements, String p, String q) {
    String[] statements = new String[elements.size()];
    for (int i = 0; i < statements.length; i++) {
      statements[i] = selectView(elements.get(i).getSimpleName(), elements.get(i).asType(), elements.get(i).getAnnotation(ViewBind.class).value(), p, q);
    }
    return statements;
  }

  Map<TypeElement, List<Element>> map(RoundEnvironment env) {
    Map<TypeElement, List<Element>> mp = new HashMap<>();
    for (Element element : env.getElementsAnnotatedWith(ViewBind.class)) {
      TypeElement typeElement = (TypeElement) element.getEnclosingElement();
      List<Element> elements = mp.get(typeElement);
      if (elements == null) {
        elements = new ArrayList<>();
        mp.put(typeElement, elements);
      }
      elements.add(element);
    }
    return mp;
  }
}
