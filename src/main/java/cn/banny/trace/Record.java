package cn.banny.trace;

public interface Record {

    int getThreadId();

    int getMethodId();

    MethodAction getMethodAction();

    int getDeltaTimeInUsec();

    int getWallTimeInUsec();

    long getFilePointer();

}
