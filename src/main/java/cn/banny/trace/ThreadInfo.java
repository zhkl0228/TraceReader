package cn.banny.trace;

public interface ThreadInfo extends CallNode, Comparable<ThreadInfo> {

    int getThreadId();

    String getThreadName();
}
