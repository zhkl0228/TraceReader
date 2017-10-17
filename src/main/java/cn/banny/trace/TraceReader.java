package cn.banny.trace;

import java.io.*;
import java.util.Map;

public class TraceReader {

    private static final int kTraceMethodActionMask = 0x3;

    static TraceRecord readRecord(DataInput input, Map<Integer, TraceThreadInfo> threadMap, int version, Map<Integer, MethodSpec> methodMap) throws IOException {
        try {
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
        } catch (EOFException e) {
            return null;
        }
    }

    public static TraceFile parseTraceFile(File traceFile) throws IOException {
        RandomAccessFile input = null;
        try {
            input = new RandomAccessFile(traceFile, "r");
            return new AndroidTraceFile(input);
        } finally {
            closeQuietly(input);
        }
    }

    public static TraceFile parseTraceFile(InputStream inputStream) throws IOException {
        return new AndroidTraceFile(new DataInputStream(inputStream));
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try { closeable.close(); } catch(Exception ignored) {}
        }
    }

    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

}
