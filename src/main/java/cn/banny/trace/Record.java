package cn.banny.trace;

public interface Record {

    TraceThreadInfo getThreadInfo();

    int getMethodId();

    MethodAction getMethodAction();

    int getThreadId();

    int getDeltaTimeInUsec();

    int getWallTimeInUsec();

    long getFilePointer();

    Record getParent();

    MethodCallNode toMethodCallNode();

}
