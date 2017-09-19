package cn.banny.trace;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public interface TraceFile extends Closeable {

    boolean isDataFileOverflow();

    String getClock();

    int getElapsedTimeInUsec();

    int getNumMethodCalls();

    int getClockCallOverheadInNsec();

    String getVm();

    List<ThreadInfo> getThreads();

    long getStartTimeInUsec();

}
