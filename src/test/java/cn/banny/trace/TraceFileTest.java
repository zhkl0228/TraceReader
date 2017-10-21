package cn.banny.trace;

import junit.framework.TestCase;

import java.io.*;

public class TraceFileTest extends TestCase {

    public void testParseFile() throws Exception {
        long startTime = System.currentTimeMillis();
        TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test.trace"));
        doTestTraceFile(traceFile);
        System.out.println("testParseFile offset=" + (System.currentTimeMillis() - startTime));
    }

    public void testParseBufferedInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/test.trace")));
            TraceFile traceFile = TraceReader.parseTraceFile(inputStream);
            doTestTraceFile(traceFile);
            System.out.println("testParseBufferedInputStream offset=" + (System.currentTimeMillis() - startTime));
        } finally {
            TraceReader.closeQuietly(inputStream);
        }
    }

    public void testParseFileInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("src/test/resources/test.trace"));
            TraceFile traceFile = TraceReader.parseTraceFile(inputStream);
            doTestTraceFile(traceFile);
            System.out.println("testParseFileInputStream offset=" + (System.currentTimeMillis() - startTime));
        } finally {
            TraceReader.closeQuietly(inputStream);
        }
    }

    private void doTestTraceFile(TraceFile traceFile) throws IOException {
        assertFalse(traceFile.getThreads().isEmpty());

        boolean dumped = traceFile.dumpStackTrace(System.out, "createFromParcel");
        assertTrue(dumped);
    }

}
