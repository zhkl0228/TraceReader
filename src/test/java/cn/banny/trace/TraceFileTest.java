package cn.banny.trace;

import junit.framework.TestCase;

import java.io.*;

public class TraceFileTest extends TestCase {

    public void testParseFile() throws Exception {
        long startTime = System.currentTimeMillis();
        TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test1.trace"));
        doTestTraceFile(traceFile, "createFromParcel");
        System.out.println("testParseFile offset=" + (System.currentTimeMillis() - startTime));
    }

    public void testParseFileInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("src/test/resources/test2.trace"));
            TraceFile traceFile = TraceReader.parseTraceFile(inputStream);
            doTestTraceFile(traceFile, "getInstalledPackages");
            System.out.println("testParseFileInputStream offset=" + (System.currentTimeMillis() - startTime));
        } finally {
            TraceReader.closeQuietly(inputStream);
        }
    }

    public void testParseBufferedInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/test3.trace")));
            TraceFile traceFile = TraceReader.parseTraceFile(inputStream);
            doTestTraceFile(traceFile, "getExternalStorageState");
            System.out.println("testParseBufferedInputStream offset=" + (System.currentTimeMillis() - startTime));
        } finally {
            TraceReader.closeQuietly(inputStream);
        }
    }

    private void doTestTraceFile(TraceFile traceFile, String keywords) throws IOException {
        assertFalse(traceFile.getThreads().isEmpty());

        boolean dumped = traceFile.dumpStackTrace(System.out, keywords);
        assertTrue(dumped);
    }

}
