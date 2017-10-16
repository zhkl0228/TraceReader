package cn.banny.trace;

import java.util.List;

public interface TraceFile {

    boolean isDataFileOverflow();

    String getClock();

    int getElapsedTimeInUsec();

    int getNumMethodCalls();

    int getClockCallOverheadInNsec();

    String getVm();

    List<ThreadInfo> getThreads();

    long getStartTimeInUsec();

}
