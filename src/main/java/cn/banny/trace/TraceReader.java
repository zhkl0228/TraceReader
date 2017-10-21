package cn.banny.trace;

import java.io.*;

public class TraceReader {

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

}
