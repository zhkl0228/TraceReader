# TraceReader

Android dmtrace.trace parser.

Usage: <br/>

<pre>
<code>
TraceFile traceFile = TraceReader.parseTraceFile(new File("src/test/resources/test1.trace"));
traceFile.findStackTrace("createFromParcel").dump(System.out, true);
</code>
</pre>
