package cn.banny.trace;

import java.io.IOException;

public interface ThreadInfo {

    int getThreadId();

    String getThreadName();

    MethodCallNode[] getTop() throws IOException;
}
