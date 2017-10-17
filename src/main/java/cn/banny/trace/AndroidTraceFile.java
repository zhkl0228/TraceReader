package cn.banny.trace;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

class AndroidTraceFile implements TraceFile {

    private final boolean dataFileOverflow;
    private final String clock;
    private final int elapsedTimeInUsec;
    private final int numMethodCalls;
    private final int clockCallOverheadInNsec;
    private final String vm;

    private final List<ThreadInfo> threadInfos;

    private final int version;
    private final long startTimeInUsec;

    private synchronized void readMethodCallNodes(DataInput traceFile, Map<Integer, TraceThreadInfo> threadMap, Map<Integer, MethodSpec> methodMap) throws IOException {
        while (true) {
            TraceRecord record = TraceReader.readRecord(traceFile, threadMap, version, methodMap);
            if (record == null) {
                break;
            }

            TraceThreadInfo threadInfo = record.getThreadInfo();
            if (threadInfo == null) {
                continue;
            }

            if (threadInfo.list == null || threadInfo.stack == null) {
                threadInfo.list = new ArrayList<>();
                threadInfo.stack = new Stack<>();

                threadInfo.stack.push(null);
            }

            if (record.getMethodAction() == MethodAction.ENTER) {
                if (!threadInfo.stack.isEmpty()) {
                    record.setParent(threadInfo.stack.peek());
                }
                threadInfo.stack.push(record);
            } else {
                if (threadInfo.stack.peek() == null) {
                    continue;
                }

                TraceRecord exit = threadInfo.stack.pop();
                if (exit.getMethodId() != record.getMethodId()) {
                    throw new IllegalStateException("exit method invalid: record=" + record + ", exit=" + exit);
                }

                threadInfo.lastExitDeltaTimeInUsec = record.getDeltaTimeInUsec();
                exit.setThreadTimeInUsec(record.getDeltaTimeInUsec() - exit.getDeltaTimeInUsec());
                if (threadInfo.stack.size() == 1) {
                    threadInfo.list.add(exit.toMethodCallNode());
                } else if (threadInfo.stack.isEmpty()) {
                    break;
                }
            }
        }

        for (TraceThreadInfo threadInfo : threadMap.values()) {
            if (threadInfo.list == null || threadInfo.stack == null) {
                continue;
            }

            while (threadInfo.stack.size() > 2) {
                threadInfo.stack.pop();
            }
            if (threadInfo.stack.size() == 2) {
                TraceRecord record = threadInfo.stack.pop();
                if (threadInfo.lastExitDeltaTimeInUsec > record.getDeltaTimeInUsec()) {
                    record.setThreadTimeInUsec(threadInfo.lastExitDeltaTimeInUsec - record.getDeltaTimeInUsec());
                }
                threadInfo.list.add(record.toMethodCallNode());
            }
        }
    }

    AndroidTraceFile(DataInput input) throws IOException {
        super();

        if (!"*version".equals(input.readLine())) {
            throw new IOException("It's not android trace file.");
        }

        input.readLine(); // version number

        String line;

        boolean dataFileOverflow = false;
        String clock = null;
        int elapsedTimeInUsec = 0;
        int numMethodCalls = 0;
        int clockCallOverheadInNsec = 0;
        String vm = null;
        while (!"*threads".equals(line = input.readLine())) {
            int index = line.indexOf('=');
            if (index != -1) {
                String name = line.substring(0, index);
                String value = line.substring(index + 1);

                if ("data-file-overflow".equals(name)) {
                    dataFileOverflow = Boolean.parseBoolean(value);
                } else if("clock".equals(name)) {
                    clock = value;
                } else if ("elapsed-time-usec".equals(name)) {
                    elapsedTimeInUsec = Integer.parseInt(value);
                } else if ("num-method-calls".equals(name)) {
                    numMethodCalls = Integer.parseInt(value);
                } else if ("clock-call-overhead-nsec".equals(name)) {
                    clockCallOverheadInNsec = Integer.parseInt(value);
                } else if ("vm".equals(name)) {
                    vm = value;
                }
            }
        }
        this.dataFileOverflow = dataFileOverflow;
        this.clock = clock;
        this.elapsedTimeInUsec = elapsedTimeInUsec;
        this.numMethodCalls = numMethodCalls;
        this.clockCallOverheadInNsec = clockCallOverheadInNsec;
        this.vm = vm;

        List<ThreadInfo> threadInfoList = new ArrayList<>();
        Map<Integer, TraceThreadInfo> threadMap = new HashMap<>();
        while (!"*methods".equals(line = input.readLine())) {
            int index = line.indexOf('\t');
            if (index != -1) {
                int threadId = Integer.parseInt(line.substring(0, index));
                String threadName = line.substring(index + 1);
                TraceThreadInfo threadInfo = new TraceThreadInfo(threadId, threadName);
                threadInfoList.add(threadInfo);
                threadMap.put(threadId, threadInfo);
            }
        }
        Collections.sort(threadInfoList);
        this.threadInfos = Collections.unmodifiableList(threadInfoList);

        Map<Integer, MethodSpec> methodMap = new HashMap<>();
        while (!"*end".equals(line = input.readLine())) {
            String[] values = line.split("\t");
            if (values.length != 6) {
                throw new IOException("Parse android trace file failed: " + line);
            }

            int methodId = Integer.parseInt(values[0].substring(2), 16);
            String className = values[1];
            String methodName = values[2];
            String parameters = values[3];
            String source = values[4];
            int lineNum = Integer.parseInt(values[5]);
            methodMap.put(methodId, new MethodSpec(className, methodName, parameters, source, lineNum));
        }

        byte[] magic = new byte[4];
        input.readFully(magic);
        if (!"SLOW".equals(new String(magic))) {
            throw new IOException("Magic mismatch.");
        }

        this.version = Short.reverseBytes(input.readShort()) & 0xffff;
        int offset = Short.reverseBytes(input.readShort()) & 0xffff;
        offset -= 8;

        this.startTimeInUsec = Long.reverseBytes(input.readLong()) & 0x7fffffffffffffffL;
        offset -= 8;

        input.skipBytes(offset);
        readMethodCallNodes(input, threadMap, methodMap);
        for (TraceThreadInfo threadInfo : threadMap.values()) {
            threadInfo.setTop(threadInfo.getNodes());
        }
    }

    public boolean isDataFileOverflow() {
        return dataFileOverflow;
    }

    public String getClock() {
        return clock;
    }

    public int getElapsedTimeInUsec() {
        return elapsedTimeInUsec;
    }

    public int getNumMethodCalls() {
        return numMethodCalls;
    }

    public int getClockCallOverheadInNsec() {
        return clockCallOverheadInNsec;
    }

    public String getVm() {
        return vm;
    }

    public List<ThreadInfo> getThreads() {
        return threadInfos;
    }

    public long getStartTimeInUsec() {
        return startTimeInUsec;
    }

}
