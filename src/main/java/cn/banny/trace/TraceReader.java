package cn.banny.trace;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class TraceReader {

    public static TraceFile parseTraceFile(File traceFile) throws IOException {
        return new RandomAccessTraceFile(traceFile);
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try { closeable.close(); } catch(Exception ignored) {}
        }
    }

}
