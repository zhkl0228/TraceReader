package cn.banny.trace;

public class TraceMethodCallNode implements MethodCallNode {

    private final TraceThreadInfo threadInfo;
    private final TraceRecord record;
    private final int threadTimeInUsec;
    private final MethodSpec method;
    private final int methodId;

    TraceMethodCallNode(TraceRecord record, RandomAccessTraceFile traceFile, int threadTimeInUsec) {
        this.threadInfo = record.getThreadInfo();
        this.record = record;
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

    public synchronized MethodCallNode[] getChildren() {
        Record[] children = record.getChildren();
        MethodCallNode[] nodes = new MethodCallNode[children.length];
        for(int i = 0; i < children.length; i++) {
            nodes[i] = children[i].toMethodCallNode();
        }
        return nodes;
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
    public boolean matchesStackElement(String keywords, boolean exact) {
        if (keywords == null) {
            throw new NullPointerException("keywords is null");
        }

        MethodCallNode node = this;
        do {
            MethodSpec method = node.getMethod();
            if (method == null) {
                continue;
            }

            String className = method.getClassName().replace('/', '.');
            String methodName = method.getMethodName();
            String fullName = className + "." + methodName;
            if (exact) {
                if (keywords.equals(className) || keywords.equals(methodName) ||
                        keywords.equals(fullName)) {
                    return true;
                }
            } else {
                if (fullName.contains(keywords)) {
                    return true;
                }
            }
        } while ((node = node.getParent()) != null);
        return false;
    }

    @Override
    public String toString() {
        if (method == null) {
            return "0x" + Integer.toHexString(methodId);
        }
        return method.toString();
    }
}
