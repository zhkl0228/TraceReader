package cn.banny.trace;

import java.io.IOException;
import java.util.Collections;

public class TraceMethodCallNode implements MethodCallNode {

    private final TraceThreadInfo threadInfo;
    private final TraceRecord record;
    private final RandomAccessTraceFile traceFile;
    private final int threadTimeInUsec;
    private final MethodSpec method;
    private final int methodId;

    TraceMethodCallNode(TraceRecord record, RandomAccessTraceFile traceFile, int threadTimeInUsec) {
        this.threadInfo = record.getThreadInfo();
        this.record = record;
        this.traceFile = traceFile;
        this.threadTimeInUsec = threadTimeInUsec;
        this.method = traceFile.methodMap.get(record.getMethodId());
        this.methodId = record.getMethodId();
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

    private MethodCallNode[] children;

    public synchronized MethodCallNode[] getChildren() {
        if (children != null) {
            return children;
        }
        try {
            traceFile.readMethodCallNodes(Collections.singletonMap(threadInfo.getThreadId(), threadInfo), record.getFilePointer(), record);
            children = threadInfo.getNodes();
            return children;
        } catch (IOException e) {
            e.printStackTrace();
            children = new MethodCallNode[0];
            return children;
        }
    }

    public MethodSpec getMethod() {
        return method;
    }

    public String getStackTraceString() {
        StringBuilder sb = new StringBuilder();
        sb.append(threadInfo.getThreadId()).append(" - ").append(threadInfo.getThreadName()).append("\n");
        MethodCallNode node = this;
        do {
            MethodSpec method = node.getMethod();
            if (method != null) {
                sb.append("    at ").append(method.getClassName().replace('/', '.')).append(".");
                sb.append(method.getMethodName()).append(method.getParameters());
                sb.append(" [").append(method.getSource()).append(":").append(method.getLine()).append("] {").append(node.getThreadTimeInUsec()).append("us}\n");
            }
        } while ((node = node.getParent()) != null);
        return sb.toString();
    }

    @Override
    public String toString() {
        if (method == null) {
            return "0x" + Integer.toHexString(methodId);
        }
        return method.toString();
    }
}
