package com.zy.processor;

/**
 * Created by zy on 2017/7/31.
 */

public class CodeGenerator {

  public static FieldCode field() {
    return new FieldCode();
  }

  public static MethodCode method() {
    return new MethodCode();
  }

  public static Parameter parameter(String type, String name) {
    return new Parameter(type, name);
  }

  public static ClassCode cls() {
    return new ClassCode();
  }

  public static class FieldCode extends SourceCode {
    String type;

    public FieldCode setType(String type) {
      this.type = type;
      return this;
    }

    @Override
    public String asString() {
      return (accessPolicy == null ? "" : accessPolicy + " ")
          + (isStatic ? "static " : "")
          + (isFinal ? "final " : "")
          + type + " "
          + name + ";";
    }
  }

  public static class ConstructorCode extends MethodCode {
    ConstructorCode() {
      super();
      returnType = "";
    }
  }

  public static class MethodCode extends SourceCode {
    String returnType = "void";
    Parameter[] parameters;
    String[] statements;

    public MethodCode setReturnType(String returnType) {
      this.returnType = returnType;
      return this;
    }

    public MethodCode setParameters(Parameter ... parameters) {
      this.parameters = parameters;
      return this;
    }

    public MethodCode setStatements(String ... statements) {
      this.statements = statements;
      return this;
    }

    @Override
    public String asString() {
      StringBuilder sb = new StringBuilder();
      if (accessPolicy != null) {
        sb.append(accessPolicy);
        sb.append(" ");
      }
      if (isStatic) {
        sb.append("static ");
      }
      if (isFinal) {
        sb.append("final ");
      }
      sb.append(returnType);
      sb.append(" ");
      sb.append(name);
      sb.append("(");
      if (parameters != null) {
        boolean first = true;
        for (Parameter p : parameters) {
          if (first) {
            first = false;
          } else {
            sb.append(", ");
          }
          sb.append(p.asString());
        }
      }
      sb.append(") {\n");
      if (statements != null) {
        for (String s : statements) {
          sb.append(s);
          sb.append("\n");
        }
      }
      sb.append("}\n");
      return sb.toString();
    }
  }

  public static class ClassCode extends SourceCode {
    String packageName;
    String type = "class";
    String superClass;
    String[] interfaces;
    SourceCode[] fields;
    SourceCode[] contructors;
    SourceCode[] methods;
    SourceCode[] subClasses;

    public ClassCode setPackageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public ClassCode setType(String type) {
      this.type = type;
      return this;
    }

    public ClassCode setSuperClass(String superClass) {
      this.superClass = superClass;
      return this;
    }

    public ClassCode setInterfaces(String ... interfaces) {
      this.interfaces = interfaces;
      return this;
    }

    public ClassCode setFields(SourceCode ... fields) {
      this.fields = fields;
      return this;
    }

    public ClassCode setContructors(SourceCode ... contructors) {
      this.contructors = contructors;
      return this;
    }

    public ClassCode setMethods(SourceCode ... methods) {
      this.methods = methods;
      return this;
    }

    public ClassCode setSubClasses(SourceCode ... subClasses) {
      this.subClasses = subClasses;
      return this;
    }

    @Override
    public String asString() {
      StringBuilder sb = new StringBuilder();
      if (packageName != null) {
        sb.append("package ");
        sb.append(packageName);
        sb.append(";\n\n");
      }
      if (accessPolicy != null) {
        sb.append(accessPolicy);
        sb.append(" ");
      }
      if (isStatic) {
        sb.append("static ");
      }
      if (isFinal) {
        sb.append("final ");
      }
      sb.append(type);
      sb.append(" ");
      sb.append(name);
      sb.append(" ");
      if (superClass != null) {
        sb.append("extends ");
        sb.append(superClass);
        sb.append(" ");
      }
      if (interfaces != null) {
        sb.append("implements ");
        boolean first = true;
        for (String i : interfaces) {
          if (first) {
            first = false;
          } else {
            sb.append(", ");
          }
          sb.append(i);
        }
        sb.append(" ");
      }
      sb.append("{\n");
      if (fields != null) {
        sb.append("\n");
        for (SourceCode f : fields) {
          sb.append(f.asString());
          sb.append("\n");
        }
      }
      if (contructors != null) {
        sb.append("\n");
        for (SourceCode c : contructors) {
          sb.append(c.asString());
          sb.append("\n");
        }
      }
      if (methods != null) {
        sb.append("\n");
        for (SourceCode m : methods) {
          sb.append(m.asString());
          sb.append("\n");
        }
      }
      if (subClasses != null) {
        sb.append("\n");
        for (SourceCode c : subClasses) {
          sb.append(c.asString());
          sb.append("\n");
        }
      }
      sb.append("}\n");
      return sb.toString();
    }
  }

  public static abstract class SourceCode {
    String accessPolicy;
    boolean isFinal;
    String name;
    boolean isStatic;

    public abstract String asString();

    public SourceCode setAccessPolicy(String policy) {
      this.accessPolicy = policy;
      return this;
    }

    public SourceCode setFinal(boolean isFinal) {
      this.isFinal = isFinal;
      return this;
    }

    public SourceCode setStatic(boolean isStatic) {
      this.isStatic = isStatic;
      return this;
    }

    public SourceCode setName(String name) {
      this.name = name;
      return this;
    }

  }

  public static class Parameter {
    String type;
    String name;
    boolean isFinal;
    String asString() {
      return (isFinal ? "final " : "") + type + " " + name;
    }

    Parameter(String type, String name) {
      this.type = type;
      this.name = name;
    }

    public Parameter asFinal() {
      isFinal = true;
      return this;
    }
  }

}
