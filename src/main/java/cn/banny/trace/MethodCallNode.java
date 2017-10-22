package cn.banny.trace;

public interface MethodCallNode extends CallNode {

    MethodCallNode getParent();

    MethodSpec getMethod();

    int getThreadTimeInUsec();

    String getStackTraceString();

    MethodCallNode matchesStackElement(String keywords, boolean exact);

    int getStackTraceSize();

}
