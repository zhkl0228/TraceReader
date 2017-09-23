package cn.banny.trace;

import java.io.IOException;
import java.util.Collections;

public class TraceMethodCallNode implements MethodCallNode {

    private final TraceThreadInfo threadInfo;
    private final TraceRecord record;
    private final RandomAccessTraceFile traceFile;
    private final int threadTimeInUsec;
    private final MethodSpec method;

    TraceMethodCallNode(TraceRecord record, RandomAccessTraceFile traceFile, int threadTimeInUsec) {
        this.threadInfo = record.getThreadInfo();
        this.record = record;
        this.traceFile = traceFile;
        this.threadTimeInUsec = threadTimeInUsec;
        this.method = traceFile.methodMap.get(record.getMethodId());
    }

    @Override
    public int getThreadTimeInUsec() {
        return threadTimeInUsec;
    }

    public MethodCallNode getParent() {
        Record parent = record.getParent();
        if (parent == null) {
            return null;
        }

        return parent.toMethodCallNode();
    }

    public MethodCallNode[] getChildren() throws IOException {
        traceFile.getMethodCallNodes(Collections.singletonMap(threadInfo.getThreadId(), threadInfo), record.getFilePointer(), record);
        return threadInfo.getNodes();
    }

    public MethodSpec getMethod() {
        return method;
    }

    public String getStackTraceString() {
        StringBuilder sb = new StringBuilder();
        sb.append(threadInfo.getThreadName()).append(": ").append(record).append("\n");
        MethodCallNode node = this;
        do {
            MethodSpec method = node.getMethod();
            if (method != null) {
                sb.append("\tat ").append(method.getClassName().replace('/', '.')).append(".");
                sb.append(method.getMethodName()).append(method.getParameters());
                sb.append(" [").append(method.getSource()).append(":").append(method.getLine()).append("] {").append(node.getThreadTimeInUsec()).append("us}\n");
            }
        } while ((node = node.getParent()) != null);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getStackTraceString();
    }
}
