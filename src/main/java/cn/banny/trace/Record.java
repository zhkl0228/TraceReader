package cn.banny.trace;

public interface Record {

    int getThreadId();

    TraceThreadInfo getThreadInfo();

    int getMethodId();

    MethodAction getMethodAction();

    int getDeltaTimeInUsec();

    int getWallTimeInUsec();

    long getFilePointer();

    Record getParent();

    MethodCallNode toMethodCallNode();

}
