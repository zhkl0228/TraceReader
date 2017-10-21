package cn.banny.trace;

public interface ThreadInfo extends CallNode {

    int getThreadId();

    String getThreadName();
}
