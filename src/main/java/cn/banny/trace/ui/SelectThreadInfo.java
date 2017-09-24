package cn.banny.trace.ui;

import cn.banny.trace.MethodCallNode;
import cn.banny.trace.ThreadInfo;

public class SelectThreadInfo implements ThreadInfo {
    @Override
    public int getThreadId() {
        return -1;
    }

    @Override
    public String getThreadName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodCallNode[] getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStackTraceString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "选择线程";
    }
}
