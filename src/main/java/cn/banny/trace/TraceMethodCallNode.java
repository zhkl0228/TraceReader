package cn.banny.trace;

public class TraceMethodCallNode implements MethodCallNode {

    private final TraceThreadInfo threadInfo;
    private final TraceRecord record;
    private final int threadTimeInUsec;
    private final MethodSpec method;
    private final int methodId;

    TraceMethodCallNode(TraceRecord record, MethodSpec method, int threadTimeInUsec) {
        this.threadInfo = record.getThreadInfo();
        this.record = record;
        this.threadTimeInUsec = threadTimeInUsec;
        this.method = method;
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
        sb.append("Thread: ").append(threadInfo.getThreadName()).append("\n");
        MethodCallNode node = this;
        do {
            MethodSpec method = node.getMethod();
            if (method != null) {
                sb.append("    at ").append(method.getClassName().replace('/', '.')).append(".");
                sb.append(method.getMethodName()).append(method.getSignature());
                sb.append(" [").append(method.getPathname()).append(":").append(method.getLineNumber()).append("] {").append(node.getThreadTimeInUsec()).append("us}\n");
            }
        } while ((node = node.getParent()) != null);
        return sb.toString();
    }

    @Override
    public int getStackTraceSize() {
        int size = 0;
        MethodCallNode node = this;
        do {
            size++;
        } while ((node = node.getParent()) != null);
        return size;
    }

    @Override
    public MethodCallNode matchesStackElement(String keywords, boolean exact) {
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
                    return node;
                }
            } else {
                if (fullName.toLowerCase().contains(keywords.toLowerCase())) {
                    return node;
                }
            }
        } while ((node = node.getParent()) != null);
        return null;
    }

    @Override
    public String toString() {
        if (method == null) {
            return "0x" + Integer.toHexString(methodId);
        }
        return method.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceMethodCallNode that = (TraceMethodCallNode) o;

        return methodId == that.methodId;
    }

    @Override
    public int hashCode() {
        return methodId;
    }

}
