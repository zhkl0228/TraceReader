package cn.banny.trace;

public interface CallNode {

    String getStackTraceString();

    boolean matchesStackElement(String keywords, boolean exact);

    MethodCallNode[] getChildren();

}
