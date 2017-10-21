package cn.banny.trace;

import java.io.PrintStream;
import java.util.List;

public interface TraceFile {

    List<ThreadInfo> getThreads();

    /**
     * @param out 找到关键对应的堆栈以后，通过out输出
     * @param keywords 查找关键词，可为类名或者方法名
     * @return 是否有找到指定关键词的调用堆栈
     */
    boolean dumpStackTrace(PrintStream out, String keywords);

}
