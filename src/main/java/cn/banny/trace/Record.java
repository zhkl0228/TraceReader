package cn.banny.trace;

public interface Record {

    TraceThreadInfo getThreadInfo();

    int getMethodId();

    MethodAction getMethodAction();

    int getThreadId();

    int getDeltaTimeInUsec();

    int getWallTimeInUsec();

    Record getParent();

    Record[] getChildren();

    MethodCallNode toMethodCallNode();

}
