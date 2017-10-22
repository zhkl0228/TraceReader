package cn.banny.trace;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ListStackTraces implements StackTraces {

    private final List<MatchesPair> list;

    ListStackTraces(List<MatchesPair> list) {
        this.list = list;
    }

    @Override
    public int dump(PrintStream out, boolean distinct) {
        if (distinct) {
            Collections.sort(list);
        }
        int size = 0;
        Set<MatchesPair> set = new HashSet<>(list.size());
        for (MatchesPair pair : list) {
            if (!distinct || set.add(pair)) {
                size++;
                out.println(pair.leaf.getStackTraceString());
            }
        }
        return size;
    }

    @Override
    public String toString(boolean distinct) {
        if (distinct) {
            Collections.sort(list);
        }
        StringBuilder sb = new StringBuilder();
        Set<MatchesPair> set = new HashSet<>(list.size());
        for (MatchesPair pair : list) {
            if (!distinct || set.add(pair)) {
                sb.append(pair.leaf.getStackTraceString()).append('\n');
            }
        }
        return sb.toString();
    }
}
