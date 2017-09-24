package cn.banny.trace;

public interface CallNode {

    String getStackTraceString();

    MethodCallNode[] getChildren();

}
