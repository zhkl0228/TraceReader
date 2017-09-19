package cn.banny.trace;

public class TraceRecord implements Record {

    private final long filePointer;
    private final int threadId;
    private final int methodId;
    private final MethodAction methodAction;
    private final int deltaTimeInUsec;

    TraceRecord(long filePointer, int threadId, int methodId, MethodAction methodAction, int deltaTimeInUsec) {
        this.filePointer = filePointer;
        this.threadId = threadId;
        this.methodId = methodId;
        this.methodAction = methodAction;
        this.deltaTimeInUsec = deltaTimeInUsec;
    }

    public long getFilePointer() {
        return filePointer;
    }

    public int getThreadId() {
        return threadId;
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

    Record setWallTimeInUsec(int wallTimeInUsec) {
        this.wallTimeInUsec = wallTimeInUsec;
        return this;
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
}
