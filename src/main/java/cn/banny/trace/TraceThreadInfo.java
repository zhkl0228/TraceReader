package cn.banny.trace;

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

    @Override
    public MethodCallNode[] getChildren() {
        return top;
    }

    List<MethodCallNode> list;
    Stack<TraceRecord> stack;
    int lastExitDeltaTimeInUsec;

    MethodCallNode[] getNodes() {
        MethodCallNode[] nodes = list == null ? null : list.toArray(new MethodCallNode[0]);
        list = null;
        stack = null;
        lastExitDeltaTimeInUsec = 0;
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(threadId);
        while (sb.length() < 4) {
            sb.insert(0, ' ');
        }
        sb.append(" - ").append(threadName);
        return sb.toString();
    }

    @Override
    public String getStackTraceString() {
        return toString().trim();
    }

    @Override
    public boolean matchesStackElement(String keywords, boolean exact) {
        return false;
    }

    @Override
    public int compareTo(ThreadInfo o) {
        return threadId - o.getThreadId();
    }
}
