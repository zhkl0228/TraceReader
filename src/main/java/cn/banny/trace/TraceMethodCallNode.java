package cn.banny.trace;

import java.io.IOException;
import java.util.Collections;

public class TraceMethodCallNode implements MethodCallNode {

    private final TraceThreadInfo threadInfo;
    private final Record record;
    private final RandomAccessTraceFile traceFile;
    private final MethodCallNode parent;
    private final MethodSpec method;

    TraceMethodCallNode(TraceThreadInfo threadInfo, Record record, RandomAccessTraceFile traceFile, MethodCallNode parent) {
        this.threadInfo = threadInfo;
        this.record = record;
        this.traceFile = traceFile;
        this.parent = parent;
        this.method = traceFile.methodMap.get(record.getMethodId());
    }

    public int getMethodId() {
        return record.getMethodId();
    }

    public MethodCallNode getParent() {
        return parent;
    }

    public MethodCallNode[] getChildren() throws IOException {
        traceFile.getMethodCallNodes(Collections.singletonMap(threadInfo.getThreadId(), threadInfo), record.getFilePointer(), this);
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
                sb.append(" [").append(method.getSource()).append(":").append(method.getLine()).append("]\n");
            }
        } while ((node = node.getParent()) != null);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getStackTraceString();
    }
}
