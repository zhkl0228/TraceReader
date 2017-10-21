# TraceReader

Android dmtrace.trace parser.

Usage: <br/>

<pre>
<code>
TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test.trace"));
traceFile.dumpStackTrace(System.out, "createFromParcel");
</code>
</pre>
