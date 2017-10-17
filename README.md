# TraceReader

Android dmtrace.trace parser.

Usage: <br/>

<pre>
<code>
TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test.trace"));

<br />
for (ThreadInfo threadInfo : traceFile.getThreads()) {
    processCallNode(threadInfo, threadInfo.getChildren(), "createFromParcel");
}

<br />
void processCallNode(CallNode node, MethodCallNode[] children, String keywords) {
    if (children.length < 1) { // leaf
        if (node.matchesStackElement(keywords, true)) {
            System.out.println(node.getStackTraceString());
        }
        return;
    }

    for (MethodCallNode child : children) {
        processCallNode(child, child.getChildren(), keywords);
    }
}
</code>
</pre>
