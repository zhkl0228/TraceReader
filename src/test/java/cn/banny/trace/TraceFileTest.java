package cn.banny.trace;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class TraceFileTest extends TestCase {

    private long startTime;
    private TraceFile traceFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        startTime = System.currentTimeMillis();
        traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test.trace"));
    }

    public void testParse() throws Exception {
        assertFalse(traceFile.getThreads().isEmpty());
        assertTrue(traceFile.getNumMethodCalls() > 0);

        for (ThreadInfo threadInfo : traceFile.getThreads()) {
            if (threadInfo.getTop() != null) {
                assertTrue("top is empty: " + threadInfo, threadInfo.getTop().length > 0);

                if (threadInfo.getThreadId() == 10) {
                    dumpChildren(threadInfo.getTop(), "+");
                }
            }
        }
    }

    private void dumpChildren(MethodCallNode[] nodes, String prefix) throws IOException {
        if (nodes == null) {
            return;
        }

        for (MethodCallNode node : nodes) {
            System.out.println(prefix + node.getMethod());

            dumpChildren(node.getChildren(), " " + prefix);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        TraceReader.closeQuietly(traceFile);
        System.out.println("offset=" + (System.currentTimeMillis() - startTime));
    }
}
