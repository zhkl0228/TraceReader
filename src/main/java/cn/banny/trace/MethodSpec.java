package cn.banny.trace;

class MethodSpec {

    private final String className;
    private final String methodName;
    private final String parameters;
    private final String source;
    private final int line;

    MethodSpec(String className, String methodName, String parameters, String source, int line) {
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
        this.source = source;
        this.line = line;
    }

    String getClassName() {
        return className;
    }

    String getMethodName() {
        return methodName;
    }

    String getParameters() {
        return parameters;
    }

    String getSource() {
        return source;
    }

    int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return className + "." + methodName + parameters + " (" + source + ":" + line + ")";
    }
}
