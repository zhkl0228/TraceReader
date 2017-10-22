package cn.banny.trace;

import java.io.PrintStream;

public interface StackTraces {

    int dump(PrintStream out, boolean distinct);

    String toString(boolean distinct);

}
