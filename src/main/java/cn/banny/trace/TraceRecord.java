package cn.banny.trace;

import java.util.ArrayList;
import java.util.List;

public class TraceRecord implements Record {

    private final long filePointer;
    private final int threadId;
    private final int methodId;
    private final MethodAction methodAction;
    private final int deltaTimeInUsec;
    private final TraceThreadInfo threadInfo;
    private final RandomAccessTraceFile traceFile;

    TraceRecord(long filePointer, int threadId, int methodId, MethodAction methodAction, int deltaTimeInUsec, TraceThreadInfo threadInfo, RandomAccessTraceFile traceFile) {
        this.filePointer = filePointer;
        this.threadId = threadId;
        this.methodId = methodId;
        this.methodAction = methodAction;
        this.deltaTimeInUsec = deltaTimeInUsec;

        this.threadInfo = threadInfo;
        this.traceFile = traceFile;
    }

    public long getFilePointer() {
        return filePointer;
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
                "filePointer=" + filePointer +
                ", threadId=" + threadId +
                ", methodId=0x" + Integer.toHexString(methodId) +
                ", methodAction=" + methodAction +
                '}';
    }

    @Override
    public MethodCallNode toMethodCallNode() {
        return new TraceMethodCallNode(this, traceFile, threadTimeInUsec);
    }
}
