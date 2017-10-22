package cn.banny.trace;

import java.util.List;

public interface TraceFile {

    List<ThreadInfo> getThreads();

    StackTraces findStackTrace(String keywords);

}
