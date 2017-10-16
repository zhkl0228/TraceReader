package cn.banny.trace;

import java.io.*;
import java.util.Map;

public class TraceReader {

    private static final int kTraceMethodActionMask = 0x3;

    static TraceRecord readRecord(DataInput input, Map<Integer, TraceThreadInfo> threadMap, int version, Map<Integer, MethodSpec> methodMap) throws IOException {
        switch (version) {
            case 3:
                int threadId = Short.reverseBytes(input.readShort()) & 0xffff;
                int method = Integer.reverseBytes(input.readInt());
                int deltaTimeInUsec = Integer.reverseBytes(input.readInt());
                int wallTimeInUsec = Integer.reverseBytes(input.readInt());

                TraceThreadInfo threadInfo = threadMap.get(threadId);
                TraceRecord record = new TraceRecord(threadId, method & ~kTraceMethodActionMask, MethodAction.decodeAction(method & kTraceMethodActionMask), deltaTimeInUsec, threadInfo, methodMap);
                record.setWallTimeInUsec(wallTimeInUsec);
                return record;
            case 1:
            case 2:
            default:
                throw new UnsupportedOperationException("readRecord version: " + version);
        }
    }

    public static TraceFile parseTraceFile(File traceFile) throws IOException {
        return new RandomAccessTraceFile(traceFile);
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try { closeable.close(); } catch(Exception ignored) {}
        }
    }

}
