package cn.banny.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TraceRecord implements Record {

    private final int threadId;
    private final int methodId;
    private final MethodAction methodAction;
    private final int deltaTimeInUsec;
    private final TraceThreadInfo threadInfo;
    private final MethodSpec method;

    TraceRecord(int threadId, int methodId, MethodAction methodAction, int deltaTimeInUsec, TraceThreadInfo threadInfo, Map<Integer, MethodSpec> methodMap) {
        this.threadId = threadId;
        this.methodId = methodId;
        this.methodAction = methodAction;
        this.deltaTimeInUsec = deltaTimeInUsec;

        this.threadInfo = threadInfo;
        this.method = methodMap.get(methodId);
    }

    public int getThreadId() {
        return threadId;
    }

    @Override
    public TraceThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public int getMethodId() {
        return methodId;
    }

    public MethodAction getMethodAction() {
        return methodAction;
    }

    public int getDeltaTimeInUsec() {
        return deltaTimeInUsec;
    }

    public int getWallTimeInUsec() {
        return wallTimeInUsec;
    }

    private int wallTimeInUsec;

    void setWallTimeInUsec(int wallTimeInUsec) {
        this.wallTimeInUsec = wallTimeInUsec;
    }

    private TraceRecord parent;

    @Override
    public Record getParent() {
        return parent;
    }

    @Override
    public Record[] getChildren() {
        return children.toArray(new Record[0]);
    }

    void setParent(TraceRecord parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    private final List<TraceRecord> children = new ArrayList<>();

    private int threadTimeInUsec;

    void setThreadTimeInUsec(int threadTimeInUsec) {
        this.threadTimeInUsec = threadTimeInUsec;
    }

    @Override
    public String toString() {
        return "Record{" +
                "threadId=" + threadId +
                ", methodId=0x" + Integer.toHexString(methodId) +
                ", methodAction=" + methodAction +
                '}';
    }

    @Override
    public MethodCallNode toMethodCallNode() {
        return new TraceMethodCallNode(this, method, threadTimeInUsec);
    }
}
