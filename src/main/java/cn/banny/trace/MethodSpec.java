package cn.banny.trace;

class MethodSpec {

    private final String className;
    private final String methodName;
    private final String signature;
    private final String pathname;
    private final int lineNumber;

    MethodSpec(String className, String methodName, String signature, String pathname, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
        this.pathname = pathname;
        this.lineNumber = lineNumber;
    }

    String getClassName() {
        return className;
    }

    String getMethodName() {
        return methodName;
    }

    String getSignature() {
        return signature;
    }

    String getPathname() {
        return pathname;
    }

    int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return className + "." + methodName + signature + " (" + pathname + ":" + lineNumber + ")";
    }
}
