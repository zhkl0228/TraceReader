package cn.banny.trace;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

class TraceThreadInfo implements ThreadInfo {

    private final int threadId;
    private final String threadName;

    TraceThreadInfo(int threadId, String threadName) {
        super();
        this.threadId = threadId;
        this.threadName = threadName;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    private MethodCallNode[] top;

    void setTop(MethodCallNode[] top) {
        this.top = top;
    }

    public MethodCallNode[] getTop() throws IOException {
        return top;
    }

    List<MethodCallNode> list;
    Stack<TraceRecord> stack;

    MethodCallNode[] getNodes() {
        MethodCallNode[] nodes = list == null ? null : list.toArray(new MethodCallNode[0]);
        list = null;
        stack = null;
        return nodes;
    }

    @Override
    public String toString() {
        return "TraceThreadInfo{" +
                "threadId=" + threadId +
                ", threadName='" + threadName + '\'' +
                '}';
    }
}
