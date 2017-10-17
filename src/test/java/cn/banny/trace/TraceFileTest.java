package cn.banny.trace;

import junit.framework.TestCase;

import java.io.*;

public class TraceFileTest extends TestCase {

    public void testParseFile() throws Exception {
        long startTime = System.currentTimeMillis();
        TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test.trace"));
        doTestTraceFile(traceFile, true);
        System.out.println("testParseFile offset=" + (System.currentTimeMillis() - startTime));
    }

    public void testParseBufferedInputStream() throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/test.trace")));
            TraceFile traceFile = TraceReader.parseTraceFile(inputStream);
            doTestTraceFile(traceFile, false);
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
            doTestTraceFile(traceFile, false);
            System.out.println("testParseFileInputStream offset=" + (System.currentTimeMillis() - startTime));
        } finally {
            TraceReader.closeQuietly(inputStream);
        }
    }

    private void doTestTraceFile(TraceFile traceFile, boolean exact) throws IOException {
        assertFalse(traceFile.getThreads().isEmpty());
        assertTrue(traceFile.getNumMethodCalls() > 0);

        System.out.println();
        for (ThreadInfo threadInfo : traceFile.getThreads()) {
            if (threadInfo.getChildren() != null) {
                assertTrue("top is empty: " + threadInfo, threadInfo.getChildren().length > 0);

                if (threadInfo.getThreadId() == 10) {
                    dumpChildren(threadInfo.getChildren(), "+", exact);
                }
            }
        }
    }

    private void dumpChildren(MethodCallNode[] nodes, String prefix, boolean exact) throws IOException {
        if (nodes == null) {
            return;
        }

        for (MethodCallNode node : nodes) {
            System.out.println(prefix + node.getMethod());

            MethodCallNode[] children = node.getChildren();
            dumpChildren(children, " " + prefix, exact);

            if (children == null || children.length < 1) {
                System.out.println("matchesStackElement createFromParcel: " + node.matchesStackElement("createFromParcel", exact));
                System.out.println(node.getStackTraceString());
            }
        }
    }

}
