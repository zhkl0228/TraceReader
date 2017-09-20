package cn.banny.trace;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

class RandomAccessTraceFile implements TraceFile {

    private final boolean dataFileOverflow;
    private final String clock;
    private final int elapsedTimeInUsec;
    private final int numMethodCalls;
    private final int clockCallOverheadInNsec;
    private final String vm;

    private final List<ThreadInfo> threadInfos;
    final Map<Integer, MethodSpec> methodMap;

    private final int version;
    private final long startTimeInUsec;
    private final int recordSize;

    private final RandomAccessFile traceFile;

    private static final int kTraceMethodActionMask = 0x3;

    private Record readRecord() throws IOException {
        long filePointer = traceFile.getFilePointer();
        switch (version) {
            case 3:
                int threadId = Short.reverseBytes(traceFile.readShort()) & 0xffff;
                int method = Integer.reverseBytes(traceFile.readInt());
                int deltaTimeInUsec = Integer.reverseBytes(traceFile.readInt());
                int wallTimeInUsec = Integer.reverseBytes(traceFile.readInt());
                return new TraceRecord(filePointer, threadId, method & ~kTraceMethodActionMask, MethodAction.decodeAction(method & kTraceMethodActionMask), deltaTimeInUsec).setWallTimeInUsec(wallTimeInUsec);
            case 1:
            case 2:
            default:
                throw new UnsupportedOperationException("readRecord version: " + version);
        }
    }

    synchronized void getMethodCallNodes(Map<Integer, TraceThreadInfo> threadMap, long offset, MethodCallNode parent) throws IOException {
        traceFile.seek(offset);

        long length = traceFile.length();
        while (traceFile.getFilePointer() + recordSize < length) {
            Record record = readRecord();

            TraceThreadInfo threadInfo = threadMap.get(record.getThreadId());
            if (threadInfo == null) {
                continue;
            }

            if (threadInfo.list == null || threadInfo.stack == null) {
                threadInfo.list = new ArrayList<MethodCallNode>();
                threadInfo.stack = new Stack<Record>();
            }

            if (parent != null && record.getMethodId() == parent.getMethodId()) {
                if (record.getMethodAction() == MethodAction.ENTER) {
                    continue;
                } else if (threadInfo.stack.isEmpty()){
                    break;
                }
            }

            if (threadInfo.stack.isEmpty() && record.getMethodAction() == MethodAction.EXIT) {
                continue;
            }

            switch (record.getMethodAction()) {
                case ENTER:
                    threadInfo.stack.push(record);
                    break;
                case EXIT:
                    Record exitRecord = threadInfo.stack.pop();
                    if (exitRecord.getMethodId() != record.getMethodId() || exitRecord.getMethodAction() != MethodAction.ENTER) {
                        throw new IllegalStateException("exit method invalid: record=" + record + ", exit=" + exitRecord);
                    }
                    if (threadInfo.stack.isEmpty()) {
                        threadInfo.list.add(new TraceMethodCallNode(threadInfo, exitRecord, this, parent));
                    }
                    break;
                case EXCEPTION:
                    Record exceptionRecord = threadInfo.stack.pop();
                    if (exceptionRecord.getMethodId() != record.getMethodId() || exceptionRecord.getMethodAction() != MethodAction.ENTER) {
                        throw new IllegalStateException("exception method invalid: record=" + record + ", exception=" + exceptionRecord);
                    }
                    if (threadInfo.stack.isEmpty()) {
                        threadInfo.list.add(new TraceMethodCallNode(threadInfo, exceptionRecord, this, parent));
                    }
                    break;
            }
        }

        for (TraceThreadInfo threadInfo : threadMap.values()) {
            if (threadInfo.list == null || threadInfo.stack == null) {
                continue;
            }

            while (threadInfo.stack.size() > 1) {
                threadInfo.stack.pop();
            }
            if (!threadInfo.stack.isEmpty()) {
                threadInfo.list.add(new TraceMethodCallNode(threadInfo, threadInfo.stack.pop(), this, parent));
            }
        }
    }

    RandomAccessTraceFile(File traceFile) throws IOException {
        super();

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(traceFile, "r");

            if (!"*version".equals(randomAccessFile.readLine())) {
                throw new IOException("It's not android trace file.");
            }

            randomAccessFile.readLine(); // version number

            String line;

            boolean dataFileOverflow = false;
            String clock = null;
            int elapsedTimeInUsec = 0;
            int numMethodCalls = 0;
            int clockCallOverheadInNsec = 0;
            String vm = null;
            while (!"*threads".equals(line = randomAccessFile.readLine())) {
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

            List<ThreadInfo> threadInfoList = new ArrayList<ThreadInfo>();
            Map<Integer, TraceThreadInfo> threadMap = new HashMap<Integer, TraceThreadInfo>();
            while (!"*methods".equals(line = randomAccessFile.readLine())) {
                int index = line.indexOf('\t');
                if (index != -1) {
                    int threadId = Integer.parseInt(line.substring(0, index));
                    String threadName = line.substring(index + 1);
                    TraceThreadInfo threadInfo = new TraceThreadInfo(threadId, threadName);
                    threadInfoList.add(threadInfo);
                    threadMap.put(threadId, threadInfo);
                }
            }
            this.threadInfos = Collections.unmodifiableList(threadInfoList);

            Map<Integer, MethodSpec> methodMap = new HashMap<Integer, MethodSpec>();
            while (!"*end".equals(line = randomAccessFile.readLine())) {
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
            this.methodMap = Collections.unmodifiableMap(methodMap);

            long pos = randomAccessFile.getFilePointer();

            byte[] magic = new byte[4];
            if (randomAccessFile.read(magic) != 4) {
                throw new IOException("Read magic failed.");
            }
            if (!"SLOW".equals(new String(magic))) {
                throw new IOException("Magic mismatch.");
            }

            this.version = Short.reverseBytes(randomAccessFile.readShort()) & 0xffff;
            int offset = Short.reverseBytes(randomAccessFile.readShort()) & 0xffff;
            this.startTimeInUsec = Long.reverseBytes(randomAccessFile.readLong()) & 0x7fffffffffffffffL;
            this.recordSize = version >= 2 ? Short.reverseBytes(randomAccessFile.readShort()) & 0xffff : 10;

            this.traceFile = randomAccessFile;

            getMethodCallNodes(threadMap, pos + offset, null);
            for (TraceThreadInfo threadInfo : threadMap.values()) {
                threadInfo.setTop(threadInfo.getNodes());
            }
        } catch (IOException e) {
            TraceReader.closeQuietly(randomAccessFile);
            throw e;
        } catch (RuntimeException e) {
            TraceReader.closeQuietly(randomAccessFile);
            throw e;
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

    public void close() throws IOException {
        TraceReader.closeQuietly(traceFile);
    }

}
